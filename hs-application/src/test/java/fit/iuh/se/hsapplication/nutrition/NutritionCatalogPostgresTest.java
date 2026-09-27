package fit.iuh.se.hsapplication.nutrition;

import fit.iuh.se.hsnutrition.dto.NutritionFoodGroupResponse;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.NutrientValue;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceFoodSummaryResponse;
import fit.iuh.se.hsnutrition.repository.NutritionGuidanceFoodRepository;
import fit.iuh.se.hsnutrition.service.NutritionCatalogService;
import fit.iuh.se.hsnutrition.service.NutritionReferenceService;
import fit.iuh.se.hsnutrition.service.impl.NutritionCatalogServiceImpl;
import fit.iuh.se.hsnutrition.service.impl.NutritionReferenceServiceImpl;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Opt in with NUTRITION_TEST_JDBC_URL/USER/PASSWORD; creates and drops ONLY its own random schema. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NutritionCatalogPostgresTest {
    private AnnotationConfigApplicationContext context;
    private DriverManagerDataSource admin;
    private JdbcTemplate jdbc;
    private Flyway flyway;
    private String schema;
    private NutritionCatalogService catalog;
    private NutritionReferenceService reference;

    @BeforeAll
    void start() {
        String url = System.getenv("NUTRITION_TEST_JDBC_URL");
        Assumptions.assumeTrue(url != null && !url.isBlank(), "Set NUTRITION_TEST_JDBC_URL for PostgreSQL integration tests");
        assertTrue(url.startsWith("jdbc:postgresql:"), "These tests require PostgreSQL, not H2");
        admin = datasource(url);
        schema = "nutrition_test_" + UUID.randomUUID().toString().replace("-", "");
        new JdbcTemplate(admin).execute("CREATE SCHEMA " + schema);
        var scoped = datasource(url);
        Properties properties = new Properties();
        properties.setProperty("currentSchema", schema);
        scoped.setConnectionProperties(properties);
        jdbc = new JdbcTemplate(scoped);
        assertEquals(schema, jdbc.queryForObject("select current_schema()", String.class));
        // The real migration folder; V1-V20 need tables owned by other modules, so start after them.
        flyway = Flyway.configure().dataSource(scoped).schemas(schema).defaultSchema(schema)
                .locations("classpath:db/migration").baselineVersion("20").load();
        flyway.baseline();
        assertEquals(5, flyway.migrate().migrationsExecuted);
        context = new AnnotationConfigApplicationContext();
        context.registerBean(DataSource.class, () -> scoped);
        context.register(TestConfiguration.class);
        context.refresh();
        catalog = context.getBean(NutritionCatalogService.class);
        reference = context.getBean(NutritionReferenceService.class);
    }

    private DriverManagerDataSource datasource(String url) {
        var ds = new DriverManagerDataSource();
        ds.setUrl(url);
        ds.setUsername(System.getenv("NUTRITION_TEST_JDBC_USER"));
        ds.setPassword(System.getenv("NUTRITION_TEST_JDBC_PASSWORD"));
        return ds;
    }

    @AfterAll
    void stop() {
        if (context != null) context.close();
        if (admin != null && schema != null && schema.matches("nutrition_test_[a-f0-9]{32}"))
            new JdbcTemplate(admin).execute("DROP SCHEMA " + schema + " CASCADE");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = NutritionGuidanceFoodRepository.class)
    @Import({NutritionCatalogServiceImpl.class, NutritionReferenceServiceImpl.class})
    static class TestConfiguration {
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(ds);
            factory.setPackagesToScan("fit.iuh.se.hsnutrition.entity");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "validate"));
            return factory;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
    }

    private void error(ErrorCode code, Runnable action) {
        assertEquals(code, assertThrows(AppException.class, action::run).getErrorCode());
    }

    private double column(String sourceFoodCode, String column) {
        return jdbc.queryForObject("select " + column + " from nutrition_foods where source_food_code = ?",
                BigDecimal.class, sourceFoodCode).doubleValue();
    }

    private NutrientValue nutrient(NutritionFoodResponse food, String code) {
        return food.nutrients().stream().filter(n -> n.nutrientCode().equals(code)).findFirst().orElseThrow();
    }

    @Test void migrationsRunOnceAndHibernateValidatesTheSchema() {
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertNotNull(context.getBean(EntityManagerFactory.class));
        assertEquals(5957, jdbc.queryForObject("select count(*) from nutrition_foods", Long.class));
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from nutrition_guidance_foods where nutrition_food_id is null", Long.class));
    }

    // ---------------------------------------------------------------- common food groups (V24)

    private static final List<String> GROUP_ORDER = List.of("CEREAL", "TUBER", "LEGUMES_NUTS", "VEGETABLE", "FRUIT",
            "MEAT", "FISH", "EGG", "DAIRY", "FAT_OIL", "SWEET", "CONDIMENT", "BEVERAGE", "MIXED_DISH", "OTHER");

    @Test void everyFoodBelongsToOneCommonGroupAndTheOldGuidanceGroupsAreGone() {
        assertEquals(0, jdbc.queryForObject("select count(*) from nutrition_foods where group_id is null", Long.class));
        assertEquals(GROUP_ORDER, jdbc.queryForList(
                "select id from nutrition_food_groups order by display_order", String.class));
        List<NutritionFoodGroupResponse> groups = catalog.getGroups();
        assertEquals(GROUP_ORDER, groups.stream().map(NutritionFoodGroupResponse::id).toList());
        assertEquals(5957, groups.stream().mapToLong(NutritionFoodGroupResponse::foodCount).sum());
        assertEquals(29, groups.stream().mapToLong(NutritionFoodGroupResponse::guidanceFoodCount).sum());
        assertTrue(groups.stream().allMatch(g -> g.foodCount()
                == g.sourceCounts().values().stream().mapToLong(Long::longValue).sum()));
        assertTrue(groups.stream().allMatch(g -> g.icon() != null && g.slug() != null));
    }

    @Test void groupCountsAreSplitBySourceWithVietnamFirst() {
        NutritionFoodGroupResponse cereal = catalog.getGroup("cereals");
        assertEquals(List.of("VN_FCT", "USDA_FNDDS"), List.copyOf(cereal.sourceCounts().keySet()));
        assertEquals(23, cereal.sourceCounts().get("VN_FCT"));
        assertEquals(679, cereal.sourceCounts().get("USDA_FNDDS"));
        // Mixed dishes only exist in USDA: no key for a source without foods
        assertEquals(Map.of("USDA_FNDDS", 1621L), catalog.getGroup("MIXED_DISH").sourceCounts());
        assertEquals(0, catalog.getGroup("CEREAL").guidanceFoodCount());
    }

    @Test void theBookCannedGroupIsSplitByFoodType() {
        Map<String, String> byCode = new HashMap<>();
        jdbc.query("select source_food_code, group_id from nutrition_foods where source = 'VN_FCT' and category = 'Đồ hộp'",
                (java.sql.ResultSet rs) -> { byCode.put(rs.getString(1), rs.getString(2)); });
        assertEquals(21, byCode.size());
        assertEquals("FRUIT", byCode.get("11003"), "Dứa hộp");
        assertEquals("FISH", byCode.get("11015"), "Cá thu hộp");
        assertEquals("MEAT", byCode.get("11017"), "Thịt bò hộp");
        assertEquals("SWEET", byCode.get("11011"), "Mứt đu đủ");
    }

    @Test void usdaExceptionsFollowTheVietnameseGrouping() {
        assertEquals("TUBER", groupOf("73401000"), "sweet potato is a tuber, not a vegetable");
        assertEquals("TUBER", groupOf("71930120"), "cassava");
        assertEquals("FRUIT", groupOf("63105010"), "avocado");
        assertEquals("CONDIMENT", groupOf("83102000"), "Caesar dressing");
        assertEquals("FAT_OIL", groupOf("82104000"), "olive oil");
        assertEquals("CEREAL", groupOf("75216111"), "corn is a cereal in the Vietnamese table");
        assertEquals("OTHER", groupOf("95220000"), "nutritional powder");
    }

    private String groupOf(String sourceFoodCode) {
        return jdbc.queryForObject("select group_id from nutrition_foods where source = 'USDA_FNDDS' and source_food_code = ?",
                String.class, sourceFoodCode);
    }

    @Test void groupIsFoundByIdOrSlugAndUnknownGroupIsNotFound() {
        assertEquals("FISH", catalog.getGroup("FISH").id());
        assertEquals("FISH", catalog.getGroup("fish-seafood").id());
        assertEquals(4, catalog.getGroup("fish-seafood").guidanceFoodCount());
        assertEquals(213, catalog.getGroup("fish-seafood").foodCount());
        error(ErrorCode.ENTITY_NOT_FOUND, () -> catalog.getGroup("no-such-group"));
        error(ErrorCode.ENTITY_NOT_FOUND, () -> catalog.getGroup("BEVERAGES_ALCOHOL"));
        error(ErrorCode.ENTITY_NOT_FOUND, () -> catalog.getGroupFoods("no-such-group"));
    }

    @Test void groupFoodsComeInDisplayOrderAndBelongToTheGroup() {
        List<NutritionFoodResponse> dairy = catalog.getGroupFoods("dairy");
        assertEquals(7, dairy.size());
        assertEquals("milk-low-fat-1", dairy.getFirst().id());
        assertTrue(dairy.stream().allMatch(f -> f.group().equals("DAIRY")));
        assertEquals("Sữa và sản phẩm từ sữa", dairy.getFirst().groupName());
    }

    @Test void guidanceFoodsTakeTheGroupOfTheFoodTheyPointAt() {
        assertEquals("FRUIT", catalog.getFood("avocado-raw").group());
        assertEquals("MEAT", catalog.getFood("beef-sausage").group());
        // Coffee, tea, beer and wine used to sit in two guidance-only groups; now they are all beverages
        List<NutritionFoodResponse> beverages = catalog.getGroupFoods("beverages");
        assertEquals(4, beverages.size());
        assertTrue(beverages.stream().allMatch(f -> f.group().equals("BEVERAGE")));
    }

    @Test void foodDetailReadsNutrientValuesFromFnddsNotFromTheGuidanceRow() {
        NutritionFoodResponse milk = catalog.getFood("milk-low-fat-1");
        assertEquals("11112210", milk.sourceFoodCode());
        assertEquals("Milk, low fat (1%)", milk.sourceDescription());
        assertEquals("Sữa ít béo 1%", milk.foodNameSpecific());
        assertEquals("PRIORITIZE", milk.guidance());
        assertEquals(new NutritionFoodResponse.ServingReference(100, "g"), milk.servingReference());
        assertEquals(18, milk.nutrients().size());
        assertEquals(column("11112210", "energy_kcal"), nutrient(milk, "energy").amount());
        assertEquals(column("11112210", "sodium_mg"), nutrient(milk, "sodium").amount());
        assertTrue(nutrient(milk, "energy").isKey());
        assertFalse(nutrient(milk, "fiber").isKey());
        assertEquals(List.of("protein", "fat_saturated", "sodium", "potassium"), milk.highlightNutrientCodes());
        assertFalse(milk.evidenceSources().isEmpty());
        error(ErrorCode.ENTITY_NOT_FOUND, () -> catalog.getFood("no-such-food"));
    }

    @Test void omegaThreeIsConvertedFromGramsToMilligrams() {
        NutritionFoodResponse salmon = catalog.getFood("salmon-baked");
        String code = salmon.sourceFoodCode();
        assertEquals("mg", nutrient(salmon, "dha").unit());
        assertEquals(column(code, "dha_g") * 1000, nutrient(salmon, "dha").amount(), 1e-9);
        assertEquals(column(code, "epa_g") * 1000, nutrient(salmon, "epa").amount(), 1e-9);
        assertTrue(nutrient(salmon, "dha").amount() > 100, "DHA of baked salmon is over 100 mg per 100 g");
    }

    @Test void evidenceSourcesKeepTheirOrder() {
        NutritionFoodResponse food = catalog.getGroupFoods("fish-seafood").stream()
                .filter(f -> f.evidenceSources().size() > 1).findFirst().orElseThrow();
        List<String> expected = jdbc.queryForList("""
                select evidence_source_id from nutrition_guidance_food_evidence
                where guidance_food_id = ? order by display_order
                """, String.class, food.id());
        assertEquals(expected, food.evidenceSources().stream().map(NutritionFoodResponse.EvidenceSource::id).toList());
    }

    @Test void greenTeaUsesTheCorrectedFnddsCode() {
        NutritionFoodResponse tea = catalog.getFood("tea-brewed");
        assertEquals("92303010", tea.sourceFoodCode());
        assertEquals("Tea, hot, leaf, green", tea.sourceDescription());
        assertEquals(18, tea.nutrients().size());
    }

    @Test void searchIgnoresCaseAndVietnameseDiacritics() {
        Set<String> salmon = Set.of("salmon-baked", "salmon-fried");
        assertEquals(salmon, ids(catalog.searchFoods("Cá hồi")));
        assertEquals(salmon, ids(catalog.searchFoods("  CA   HOI ")));
        assertFalse(catalog.searchFoods("sua").isEmpty(), "\"sua\" matches \"Sữa\"");
        assertFalse(catalog.searchFoods("dau phong").isEmpty(), "\"dau\" matches \"Đậu\"");
        assertFalse(catalog.searchFoods("salmon").isEmpty(), "USDA source name is searchable too");
    }

    @Test void searchTreatsLikeWildcardsAsPlainText() {
        // "%" must match only foods whose text really contains "%" (e.g. "Milk, low fat (1%)"), not everything.
        List<NutritionFoodResponse> percent = catalog.searchFoods("%");
        assertFalse(percent.isEmpty());
        assertTrue(percent.size() < 20);
        assertTrue(percent.stream().allMatch(f -> (f.foodNameSpecific() + f.sourceDescription()
                + Objects.toString(f.description(), "")).contains("%")));
        assertTrue(catalog.searchFoods("_").isEmpty());
        assertTrue(catalog.searchFoods("   ").isEmpty());
        assertTrue(catalog.searchFoods(null).isEmpty());
        error(ErrorCode.INVALID_PARAMETER, () -> catalog.searchFoods("x".repeat(101)));
    }

    // ---------------------------------------------------------------- reference lookup (all FNDDS foods)

    private List<String> names(PageResponse<NutritionReferenceFoodSummaryResponse> page) {
        return page.getContent().stream().map(NutritionReferenceFoodSummaryResponse::displayName).toList();
    }

    @Test void browsingWithoutAQueryListsEveryFoodByName() {
        var first = reference.searchFoods("", null, null, 1, 20);
        assertEquals(5957, first.getTotalElements());
        assertEquals(298, first.getTotalPages());
        assertTrue(first.isHasMore());
        assertEquals(jdbc.queryForList("select cast(id as text) from nutrition_foods order by case when source = 'VN_FCT' then coalesce(name_vi, name) else name end, id limit 20",
                String.class), first.getContent().stream().map(NutritionReferenceFoodSummaryResponse::id).toList());
        var last = reference.searchFoods(null, null, null, 298, 20);
        assertEquals(17, last.getContent().size());
        assertFalse(last.isHasMore());
        assertTrue(reference.searchFoods("", null, null, 299, 20).getContent().isEmpty());
    }

    @Test void searchMatchesWordPrefixesInAnyOrder() {
        var page = reference.searchFoods("salm bak", null, null, 1, 50);
        assertFalse(page.getContent().isEmpty());
        assertTrue(names(page).contains("Fish, salmon, baked or broiled"));
        assertTrue(names(page).stream().map(String::toLowerCase).allMatch(n -> n.contains("salm") && n.contains("bak")),
                "every token must match: " + names(page));
        assertEquals(page.getTotalElements(), reference.searchFoods("BAKED salmon", null, null, 1, 50).getTotalElements());
    }

    @Test void searchStripsVietnameseDiacriticsButRanksTheExactSpellingFirst() {
        // "pho" also matches "phô mai" (cheese) once accents are stripped; the foods spelled "phở" come first
        var pho = reference.searchFoods("phở", null, "USDA_FNDDS", 1, 50);
        assertTrue(pho.getTotalElements() > 100, "prefix search without accents also finds phô mai");
        int exact = jdbc.queryForObject("select count(*) from nutrition_foods where source = 'USDA_FNDDS' and lower(name_vi) like '%phở%'",
                Integer.class);
        assertTrue(exact >= 2 && exact < 50, "exact = " + exact);
        var content = pho.getContent();
        assertTrue(content.subList(0, exact).stream().allMatch(f -> f.localName().toLowerCase().contains("phở")), names(pho).toString());
        assertTrue(content.subList(exact, content.size()).stream().noneMatch(f -> f.localName().toLowerCase().contains("phở")));
        assertTrue(names(pho).subList(0, exact).containsAll(List.of("Soup, pho, with meat", "Soup, pho, no meat")));
        // Without accents the English name "pho" is the exact match
        assertEquals(Set.of("Soup, pho, with meat", "Soup, pho, no meat"),
                new HashSet<>(names(reference.searchFoods("pho", null, "USDA_FNDDS", 1, 20)).subList(0, 2)));
    }

    @Test void groupFilterWorksAloneWithASearchAndWithASource() {
        long mixed = jdbc.queryForObject("select count(*) from nutrition_foods where group_id = 'MIXED_DISH'", Long.class);
        var all = reference.searchFoods("", "MIXED_DISH", null, 1, 50);
        assertEquals(mixed, all.getTotalElements());
        assertTrue(all.getContent().stream().allMatch(f -> "MIXED_DISH".equals(f.group()) && "Món ăn hỗn hợp".equals(f.groupName())));
        assertEquals(mixed, reference.searchFoods("", "mixed-dishes", null, 1, 50).getTotalElements(), "slug works too");
        assertEquals(2, reference.searchFoods("pho", "MIXED_DISH", "USDA_FNDDS", 1, 20).getContent().stream()
                .filter(f -> f.displayName().startsWith("Soup, pho")).count());
        assertEquals(0, reference.searchFoods("soup pho", "DAIRY", null, 1, 20).getTotalElements());
        assertEquals(62, reference.searchFoods("", "FRUIT", "VN_FCT", 1, 20).getTotalElements());
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", "No such group", null, 1, 20));
    }

    @Test void searchInputCannotInjectTsquerySyntax() {
        assertEquals(reference.searchFoods("salmon baked", null, null, 1, 20).getTotalElements(),
                reference.searchFoods("salmon & (baked | !", null, null, 1, 20).getTotalElements());
        assertEquals(0, reference.searchFoods("&&& :* |", null, null, 1, 20).getTotalElements());
        assertEquals(0, reference.searchFoods("with the", null, null, 1, 20).getTotalElements(), "stop words only");
        assertEquals(0, reference.searchFoods("'; drop table nutrition_foods; --", null, null, 1, 20).getTotalElements());
        assertEquals(5957, jdbc.queryForObject("select count(*) from nutrition_foods", Long.class));
    }

    @Test void summaryCarriesTheFourMainNutrientsPer100Grams() {
        // "with" is an English stop word, so this also matches "Soup, pho, no meat"; pick the row by code.
        var pho = reference.searchFoods("pho with meat", "MIXED_DISH", null, 1, 5).getContent().stream()
                .filter(f -> f.sourceFoodCode().equals("28310330")).findFirst().orElseThrow();
        assertEquals(column("28310330", "energy_kcal"), pho.energyKcal());
        assertEquals(column("28310330", "protein_g"), pho.proteinG());
        assertEquals(column("28310330", "carbohydrate_g"), pho.carbohydrateG());
        assertEquals(column("28310330", "fat_total_g"), pho.fatTotalG());
    }

    @Test void referenceDetailHasAllNutrientsAndPortionsInOrder() {
        String id = String.valueOf(jdbc.queryForObject(
                "select id from nutrition_foods where source_food_code = '28310330'", Long.class));
        NutritionReferenceFoodResponse pho = reference.getFood(id);
        assertEquals("Soup, pho, with meat", pho.displayName());
        assertEquals("Phở có thịt", pho.localName());
        assertEquals("MIXED_DISH", pho.group());
        assertEquals("Món ăn hỗn hợp", pho.groupName());
        assertEquals("Ramen and Asian broth-based soups", pho.sourceCategory());
        assertEquals("USDA_FNDDS", pho.source());
        assertEquals(18, pho.nutrients().size());
        assertEquals(List.of(new NutritionReferenceFoodResponse.Portion("1 cup", 245, false),
                new NutritionReferenceFoodResponse.Portion("Quantity not specified", 245, true)), pho.portions());
        error(ErrorCode.ENTITY_NOT_FOUND, () -> reference.getFood("999999999"));
        error(ErrorCode.ENTITY_NOT_FOUND, () -> reference.getFood("not-a-number"));
    }

    @Test void invalidPagingIsRejected() {
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", null, null, 0, 20));
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", null, null, 1, 0));
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", null, null, 1, 51));
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("x".repeat(101), null, null, 1, 20));
    }

    // ---------------------------------------------------------------- Vietnamese food composition table (V23)

    @Test void vietnameseFoodsAreSearchableWithOrWithoutDiacritics() {
        for (String q : List.of("rau muống", "rau muong", "RAU MUONG")) {
            var page = reference.searchFoods(q, null, null, 1, 20);
            assertTrue(page.getContent().stream().anyMatch(f -> "Rau muống".equals(f.displayName()) && "VN_FCT".equals(f.source())),
                    q + " -> " + page.getContent());
        }
        assertTrue(reference.searchFoods("gio lua", null, null, 1, 20).getContent().stream()
                .anyMatch(f -> "Giò lụa".equals(f.displayName())));
        assertEquals(2, reference.searchFoods("mam tom", null, "VN_FCT", 1, 20).getTotalElements());
        // English name printed in the book is searchable too
        assertTrue(reference.searchFoods("water spinach", null, "VN_FCT", 1, 20).getContent().stream()
                .anyMatch(f -> "Rau muống".equals(f.displayName())));
    }

    @Test void sourceFilterSeparatesTheTwoDatabases() {
        assertEquals(526, reference.searchFoods("", null, "VN_FCT", 1, 20).getTotalElements());
        assertEquals(5431, reference.searchFoods("", null, "USDA_FNDDS", 1, 20).getTotalElements());
        assertTrue(reference.searchFoods("pho", null, "VN_FCT", 1, 20).getContent().stream()
                .allMatch(f -> f.source().equals("VN_FCT")));
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", null, "OTHER", 1, 20));
    }

    @Test void vietnameseDetailKeepsTheBookValuesAndCrudeFiberSeparately() {
        NutritionReferenceFoodResponse rau = reference.getFood("900004083");
        assertEquals("Rau muống", rau.displayName());
        assertEquals("Rau muống", rau.localName());
        assertEquals("VN_FCT", rau.source());
        assertEquals("2007", rau.sourceVersion());
        assertEquals("Rau, quả, củ dùng làm rau", rau.sourceCategory());
        assertEquals("VEGETABLE", rau.group());
        assertEquals(37.5, rau.wastePct());
        assertTrue(rau.portions().isEmpty());
        var byCode = rau.nutrients().stream().collect(java.util.stream.Collectors.toMap(NutrientValue::nutrientCode, n -> n));
        assertEquals(25, byCode.get("energy").amount());
        assertEquals(3.2, byCode.get("protein").amount());
        assertEquals(37, byCode.get("sodium").amount());
        assertEquals(331, byCode.get("potassium").amount());
        assertEquals(1, byCode.get("fiber_crude").amount());
        assertFalse(byCode.containsKey("fiber"), "the book has no dietary fiber, only crude fiber");
        assertFalse(byCode.containsKey("caffeine"), "missing values are omitted, not shown as 0");
        // A book value of "-" (no data) is NULL, not 0: Giò lụa has no sodium in the table
        assertFalse(reference.getFood("900007069").nutrients().stream().anyMatch(n -> n.nutrientCode().equals("sodium")));
    }

    @Test void v24FixesTheNamesSplitWrongInV23() {
        assertEquals("Mứt đu đủ", reference.getFood("900011011").displayName());
        assertEquals("Quả cóc", jdbc.queryForObject("select name from nutrition_foods where id = 900005043", String.class));
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from nutrition_foods where name = '-' or name_vi like 'Tên thực phẩm%'", Long.class));
    }

    // ---------------------------------------------------------------- Vietnamese names of USDA foods (V25)

    @Test void everyUsdaFoodHasAVietnameseNameAndKeepsItsEnglishDisplayName() {
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from nutrition_foods where name_vi is null or trim(name_vi) = ''", Long.class));
        var chicken = reference.searchFoods("ức gà nướng", null, "USDA_FNDDS", 1, 50);
        assertTrue(chicken.getContent().stream().anyMatch(f -> f.sourceFoodCode().equals("24122131")), names(chicken).toString());
        assertTrue(chicken.getContent().stream().allMatch(f -> !f.displayName().equals(f.localName())),
                "USDA foods show the English name, the translation is only localName");
        assertEquals(chicken.getTotalElements(), reference.searchFoods("uc ga nuong", null, "USDA_FNDDS", 1, 50).getTotalElements());
    }

    @Test void guidanceCatalogStillPointsAtUsdaFoods() {
        assertEquals(29, jdbc.queryForObject("""
                select count(*) from nutrition_guidance_foods g join nutrition_foods f on f.id = g.nutrition_food_id
                where f.source = 'USDA_FNDDS'""", Long.class));
    }

    private static Set<String> ids(List<NutritionFoodResponse> foods) {
        return new HashSet<>(foods.stream().map(NutritionFoodResponse::id).toList());
    }
}
