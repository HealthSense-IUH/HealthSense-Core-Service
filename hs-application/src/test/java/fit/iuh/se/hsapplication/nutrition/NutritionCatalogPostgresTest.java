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
import fit.iuh.se.hsnutrition.service.impl.DietPrescriptionServiceImpl;
import fit.iuh.se.hsnutrition.service.DietPrescriptionService;
import fit.iuh.se.hsnutrition.service.DietProfile;
import fit.iuh.se.hsnutrition.service.DietRuleService;
import fit.iuh.se.hsnutrition.service.DietThreshold;
import fit.iuh.se.hsnutrition.service.impl.DietRuleServiceImpl;
import fit.iuh.se.hsnutrition.dto.DietRuleResponse;
import fit.iuh.se.hsnutrition.dto.DietThresholdRequest;
import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;
import fit.iuh.se.hsnutrition.dto.DietAdvice;
import fit.iuh.se.hsnutrition.dto.NutritionDietPrescriptionResponse;
import fit.iuh.se.hsnutrition.dto.UpdateDietPrescriptionRequest;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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

/**
 * Opt in with NUTRITION_TEST_JDBC_URL/USER/PASSWORD; creates and drops ONLY its own random schema.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NutritionCatalogPostgresTest {
    private AnnotationConfigApplicationContext context;
    private DriverManagerDataSource admin;
    private JdbcTemplate jdbc;
    private Flyway flyway;
    private String schema;
    private NutritionCatalogService catalog;
    private NutritionReferenceService reference;
    private DietPrescriptionService prescriptions;
    private DietRuleService rules;

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
        flyway.migrate();
        // Chỉ kiểm tra các migration dinh dưỡng; module khác có thể thêm migration vào cùng thư mục quét
        List<String> applied = Arrays.stream(flyway.info().applied()).map(i -> i.getVersion().getVersion()).toList();
        assertTrue(applied.containsAll(List.of("21", "22", "23", "24", "25", "26", "27", "28", "29", "30")), applied.toString());
        context = new AnnotationConfigApplicationContext();
        context.registerBean(DataSource.class, () -> scoped);
        context.register(TestConfiguration.class);
        context.refresh();
        catalog = context.getBean(NutritionCatalogService.class);
        reference = context.getBean(NutritionReferenceService.class);
        prescriptions = context.getBean(DietPrescriptionService.class);
        rules = context.getBean(DietRuleService.class);
    }

    private DriverManagerDataSource datasource(String url) {
        var ds = new DriverManagerDataSource();
        ds.setUrl(url);
        ds.setUsername(System.getenv("NUTRITION_TEST_JDBC_USER"));
        ds.setPassword(System.getenv("NUTRITION_TEST_JDBC_PASSWORD"));
        return ds;
    }

    /** Các test kiểm nội dung tiếng Việt: giả request tiếng Việt (ngoài request thì mặc định tiếng Anh). */
    @BeforeEach
    void vietnameseRequest() {
        RequestLanguageScope.use("vi");
    }

    @AfterEach
    void clearRequest() {
        RequestLanguageScope.clear();
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
    @Import({NutritionCatalogServiceImpl.class, NutritionReferenceServiceImpl.class, DietPrescriptionServiceImpl.class,
            DietRuleServiceImpl.class})
    static class TestConfiguration {
        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(ds);
            factory.setPackagesToScan("fit.iuh.se.hsnutrition.entity");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "validate"));
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
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

    @Test
    void migrationsRunOnceAndHibernateValidatesTheSchema() {
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertNotNull(context.getBean(EntityManagerFactory.class));
        assertEquals(5957, jdbc.queryForObject("select count(*) from nutrition_foods", Long.class));
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from nutrition_guidance_foods where nutrition_food_id is null", Long.class));
    }

    // ---------------------------------------------------------------- common food groups (V24)

    private static final List<String> GROUP_ORDER = List.of("CEREAL", "TUBER", "LEGUMES_NUTS", "VEGETABLE", "FRUIT",
            "MEAT", "FISH", "EGG", "DAIRY", "FAT_OIL", "SWEET", "CONDIMENT", "BEVERAGE", "MIXED_DISH", "OTHER");

    @Test
    void everyFoodBelongsToOneCommonGroupAndTheOldGuidanceGroupsAreGone() {
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

    @Test
    void groupCountsAreSplitBySourceWithVietnamFirst() {
        NutritionFoodGroupResponse cereal = catalog.getGroup("cereals");
        assertEquals(List.of("VN_FCT", "USDA_FNDDS"), List.copyOf(cereal.sourceCounts().keySet()));
        assertEquals(23, cereal.sourceCounts().get("VN_FCT"));
        assertEquals(679, cereal.sourceCounts().get("USDA_FNDDS"));
        // Mixed dishes only exist in USDA: no key for a source without foods
        assertEquals(Map.of("USDA_FNDDS", 1621L), catalog.getGroup("MIXED_DISH").sourceCounts());
        assertEquals(0, catalog.getGroup("CEREAL").guidanceFoodCount());
    }

    @Test
    void theBookCannedGroupIsSplitByFoodType() {
        Map<String, String> byCode = new HashMap<>();
        jdbc.query("select source_food_code, group_id from nutrition_foods where source = 'VN_FCT' and category = 'Đồ hộp'",
                (java.sql.ResultSet rs) -> {
                    byCode.put(rs.getString(1), rs.getString(2));
                });
        assertEquals(21, byCode.size());
        assertEquals("FRUIT", byCode.get("11003"), "Dứa hộp");
        assertEquals("FISH", byCode.get("11015"), "Cá thu hộp");
        assertEquals("MEAT", byCode.get("11017"), "Thịt bò hộp");
        assertEquals("SWEET", byCode.get("11011"), "Mứt đu đủ");
    }

    @Test
    void usdaExceptionsFollowTheVietnameseGrouping() {
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

    @Test
    void groupIsFoundByIdOrSlugAndUnknownGroupIsNotFound() {
        assertEquals("FISH", catalog.getGroup("FISH").id());
        assertEquals("FISH", catalog.getGroup("fish-seafood").id());
        assertEquals(4, catalog.getGroup("fish-seafood").guidanceFoodCount());
        assertEquals(213, catalog.getGroup("fish-seafood").foodCount());
        error(ErrorCode.ENTITY_NOT_FOUND, () -> catalog.getGroup("no-such-group"));
        error(ErrorCode.ENTITY_NOT_FOUND, () -> catalog.getGroup("BEVERAGES_ALCOHOL"));
        error(ErrorCode.ENTITY_NOT_FOUND, () -> catalog.getGroupFoods("no-such-group"));
    }

    @Test
    void groupFoodsComeInDisplayOrderAndBelongToTheGroup() {
        List<NutritionFoodResponse> dairy = catalog.getGroupFoods("dairy");
        assertEquals(7, dairy.size());
        assertEquals("milk-low-fat-1", dairy.getFirst().id());
        assertTrue(dairy.stream().allMatch(f -> f.group().equals("DAIRY")));
        assertEquals("Sữa và sản phẩm từ sữa", dairy.getFirst().groupName());
    }

    @Test
    void guidanceFoodsTakeTheGroupOfTheFoodTheyPointAt() {
        assertEquals("FRUIT", catalog.getFood("avocado-raw").group());
        assertEquals("MEAT", catalog.getFood("beef-sausage").group());
        // Coffee, tea, beer and wine used to sit in two guidance-only groups; now they are all beverages
        List<NutritionFoodResponse> beverages = catalog.getGroupFoods("beverages");
        assertEquals(4, beverages.size());
        assertTrue(beverages.stream().allMatch(f -> f.group().equals("BEVERAGE")));
    }

    @Test
    void foodDetailReadsNutrientValuesFromFnddsNotFromTheGuidanceRow() {
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

    @Test
    void omegaThreeIsConvertedFromGramsToMilligrams() {
        NutritionFoodResponse salmon = catalog.getFood("salmon-baked");
        String code = salmon.sourceFoodCode();
        assertEquals("mg", nutrient(salmon, "dha").unit());
        assertEquals(column(code, "dha_g") * 1000, nutrient(salmon, "dha").amount(), 1e-9);
        assertEquals(column(code, "epa_g") * 1000, nutrient(salmon, "epa").amount(), 1e-9);
        assertTrue(nutrient(salmon, "dha").amount() > 100, "DHA of baked salmon is over 100 mg per 100 g");
    }

    @Test
    void evidenceSourcesKeepTheirOrder() {
        NutritionFoodResponse food = catalog.getGroupFoods("fish-seafood").stream()
                .filter(f -> f.evidenceSources().size() > 1).findFirst().orElseThrow();
        List<String> expected = jdbc.queryForList("""
                select evidence_source_id from nutrition_guidance_food_evidence
                where guidance_food_id = ? order by display_order
                """, String.class, food.id());
        assertEquals(expected, food.evidenceSources().stream().map(NutritionFoodResponse.EvidenceSource::id).toList());
    }

    @Test
    void greenTeaUsesTheCorrectedFnddsCode() {
        NutritionFoodResponse tea = catalog.getFood("tea-brewed");
        assertEquals("92303010", tea.sourceFoodCode());
        assertEquals("Tea, hot, leaf, green", tea.sourceDescription());
        assertEquals(18, tea.nutrients().size());
    }

    @Test
    void searchIgnoresCaseAndVietnameseDiacritics() {
        Set<String> salmon = Set.of("salmon-baked", "salmon-fried");
        assertEquals(salmon, ids(catalog.searchFoods("Cá hồi")));
        assertEquals(salmon, ids(catalog.searchFoods("  CA   HOI ")));
        assertFalse(catalog.searchFoods("sua").isEmpty(), "\"sua\" matches \"Sữa\"");
        assertFalse(catalog.searchFoods("dau phong").isEmpty(), "\"dau\" matches \"Đậu\"");
        assertFalse(catalog.searchFoods("salmon").isEmpty(), "USDA source name is searchable too");
    }

    @Test
    void searchTreatsLikeWildcardsAsPlainText() {
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

    @Test
    void browsingWithoutAQueryListsEveryFoodByName() {
        var first = reference.searchFoods("", null, null, 1, 20, null);
        assertEquals(5957, first.getTotalElements());
        assertEquals(298, first.getTotalPages());
        assertTrue(first.isHasMore());
        assertEquals(jdbc.queryForList("select cast(id as text) from nutrition_foods order by case when source = 'VN_FCT' then coalesce(name_vi, name) else name end, id limit 20",
                String.class), first.getContent().stream().map(NutritionReferenceFoodSummaryResponse::id).toList());
        var last = reference.searchFoods(null, null, null, 298, 20, null);
        assertEquals(17, last.getContent().size());
        assertFalse(last.isHasMore());
        assertTrue(reference.searchFoods("", null, null, 299, 20, null).getContent().isEmpty());
    }

    @Test
    void searchMatchesWordPrefixesInAnyOrder() {
        var page = reference.searchFoods("salm bak", null, null, 1, 50, null);
        assertFalse(page.getContent().isEmpty());
        assertTrue(names(page).contains("Fish, salmon, baked or broiled"));
        assertTrue(names(page).stream().map(String::toLowerCase).allMatch(n -> n.contains("salm") && n.contains("bak")),
                "every token must match: " + names(page));
        assertEquals(page.getTotalElements(), reference.searchFoods("BAKED salmon", null, null, 1, 50, null).getTotalElements());
    }

    @Test
    void searchStripsVietnameseDiacriticsButRanksTheExactSpellingFirst() {
        // "pho" also matches "phô mai" (cheese) once accents are stripped; the foods spelled "phở" come first
        var pho = reference.searchFoods("phở", null, "USDA_FNDDS", 1, 50, null);
        assertTrue(pho.getTotalElements() > 100, "prefix search without accents also finds phô mai");
        int exact = jdbc.queryForObject("select count(*) from nutrition_foods where source = 'USDA_FNDDS' and lower(name_vi) like '%phở%'",
                Integer.class);
        assertTrue(exact >= 2 && exact < 50, "exact = " + exact);
        var content = pho.getContent();
        assertTrue(content.stream().anyMatch(f -> f.sourceFoodCode().equals("56117090")),
                "\"Sợi mì gạo (bún / phở)\": a slash must not glue two words into one search token");
        assertTrue(content.subList(0, exact).stream().allMatch(f -> f.localName().toLowerCase().contains("phở")), names(pho).toString());
        assertTrue(content.subList(exact, content.size()).stream().noneMatch(f -> f.localName().toLowerCase().contains("phở")));
        assertTrue(names(pho).subList(0, exact).containsAll(List.of("Soup, pho, with meat", "Soup, pho, no meat")));
        // Without accents the English name "pho" is the exact match
        assertEquals(Set.of("Soup, pho, with meat", "Soup, pho, no meat"),
                new HashSet<>(names(reference.searchFoods("pho", null, "USDA_FNDDS", 1, 20, null)).subList(0, 2)));
    }

    @Test
    void groupFilterWorksAloneWithASearchAndWithASource() {
        long mixed = jdbc.queryForObject("select count(*) from nutrition_foods where group_id = 'MIXED_DISH'", Long.class);
        var all = reference.searchFoods("", "MIXED_DISH", null, 1, 50, null);
        assertEquals(mixed, all.getTotalElements());
        assertTrue(all.getContent().stream().allMatch(f -> "MIXED_DISH".equals(f.group()) && "Món ăn hỗn hợp".equals(f.groupName())));
        assertEquals(mixed, reference.searchFoods("", "mixed-dishes", null, 1, 50, null).getTotalElements(), "slug works too");
        assertEquals(2, reference.searchFoods("pho", "MIXED_DISH", "USDA_FNDDS", 1, 20, null).getContent().stream()
                .filter(f -> f.displayName().startsWith("Soup, pho")).count());
        assertEquals(0, reference.searchFoods("soup pho", "DAIRY", null, 1, 20, null).getTotalElements());
        assertEquals(62, reference.searchFoods("", "FRUIT", "VN_FCT", 1, 20, null).getTotalElements());
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", "No such group", null, 1, 20, null));
    }

    @Test
    void searchInputCannotInjectTsquerySyntax() {
        assertEquals(reference.searchFoods("salmon baked", null, null, 1, 20, null).getTotalElements(),
                reference.searchFoods("salmon & (baked | !", null, null, 1, 20, null).getTotalElements());
        assertEquals(0, reference.searchFoods("&&& :* |", null, null, 1, 20, null).getTotalElements());
        assertEquals(0, reference.searchFoods("with the", null, null, 1, 20, null).getTotalElements(), "stop words only");
        assertEquals(0, reference.searchFoods("'; drop table nutrition_foods; --", null, null, 1, 20, null).getTotalElements());
        assertEquals(5957, jdbc.queryForObject("select count(*) from nutrition_foods", Long.class));
    }

    @Test
    void summaryCarriesTheFourMainNutrientsPer100Grams() {
        // "with" is an English stop word, so this also matches "Soup, pho, no meat"; pick the row by code.
        var pho = reference.searchFoods("pho with meat", "MIXED_DISH", null, 1, 5, null).getContent().stream()
                .filter(f -> f.sourceFoodCode().equals("28310330")).findFirst().orElseThrow();
        assertEquals(column("28310330", "energy_kcal"), pho.energyKcal());
        assertEquals(column("28310330", "protein_g"), pho.proteinG());
        assertEquals(column("28310330", "carbohydrate_g"), pho.carbohydrateG());
        assertEquals(column("28310330", "fat_total_g"), pho.fatTotalG());
    }

    @Test
    void referenceDetailHasAllNutrientsAndPortionsInOrder() {
        String id = String.valueOf(jdbc.queryForObject(
                "select id from nutrition_foods where source_food_code = '28310330'", Long.class));
        NutritionReferenceFoodResponse pho = reference.getFood(id, null);
        assertEquals("Soup, pho, with meat", pho.displayName());
        assertEquals("Phở có thịt", pho.localName());
        assertEquals("MIXED_DISH", pho.group());
        assertEquals("Món ăn hỗn hợp", pho.groupName());
        assertEquals("Ramen and Asian broth-based soups", pho.sourceCategory());
        assertEquals("USDA_FNDDS", pho.source());
        assertEquals(18, pho.nutrients().size());
        assertEquals(List.of(new NutritionReferenceFoodResponse.Portion("1 cup", 245, false),
                new NutritionReferenceFoodResponse.Portion("Quantity not specified", 245, true)), pho.portions());
        error(ErrorCode.ENTITY_NOT_FOUND, () -> reference.getFood("999999999", null));
        error(ErrorCode.ENTITY_NOT_FOUND, () -> reference.getFood("not-a-number", null));
    }

    @Test
    void invalidPagingIsRejected() {
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", null, null, 0, 20, null));
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", null, null, 1, 0, null));
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", null, null, 1, 51, null));
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("x".repeat(101), null, null, 1, 20, null));
    }

    // ---------------------------------------------------------------- Vietnamese food composition table (V23)

    @Test
    void vietnameseFoodsAreSearchableWithOrWithoutDiacritics() {
        for (String q : List.of("rau muống", "rau muong", "RAU MUONG")) {
            var page = reference.searchFoods(q, null, null, 1, 20, null);
            assertTrue(page.getContent().stream().anyMatch(f -> "Rau muống".equals(f.displayName()) && "VN_FCT".equals(f.source())),
                    q + " -> " + page.getContent());
        }
        assertTrue(reference.searchFoods("gio lua", null, null, 1, 20, null).getContent().stream()
                .anyMatch(f -> "Giò lụa".equals(f.displayName())));
        assertEquals(2, reference.searchFoods("mam tom", null, "VN_FCT", 1, 20, null).getTotalElements());
        // English name printed in the book is searchable too
        assertTrue(reference.searchFoods("water spinach", null, "VN_FCT", 1, 20, null).getContent().stream()
                .anyMatch(f -> "Rau muống".equals(f.displayName())));
    }

    @Test
    void sourceFilterSeparatesTheTwoDatabases() {
        assertEquals(526, reference.searchFoods("", null, "VN_FCT", 1, 20, null).getTotalElements());
        assertEquals(5431, reference.searchFoods("", null, "USDA_FNDDS", 1, 20, null).getTotalElements());
        assertTrue(reference.searchFoods("pho", null, "VN_FCT", 1, 20, null).getContent().stream()
                .allMatch(f -> f.source().equals("VN_FCT")));
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", null, "OTHER", 1, 20, null));
    }

    @Test
    void vietnameseDetailKeepsTheBookValuesAndCrudeFiberSeparately() {
        NutritionReferenceFoodResponse rau = reference.getFood("900004083", null);
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
        assertFalse(reference.getFood("900007069", null).nutrients().stream().anyMatch(n -> n.nutrientCode().equals("sodium")));
    }

    @Test
    void v24FixesTheNamesSplitWrongInV23() {
        assertEquals("Mứt đu đủ", reference.getFood("900011011", null).displayName());
        assertEquals("Quả cóc", jdbc.queryForObject("select name from nutrition_foods where id = 900005043", String.class));
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from nutrition_foods where name = '-' or name_vi like 'Tên thực phẩm%'", Long.class));
    }

    // ---------------------------------------------------------------- Vietnamese names of USDA foods (V25)

    @Test
    void everyUsdaFoodHasAVietnameseNameAndKeepsItsEnglishDisplayName() {
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from nutrition_foods where name_vi is null or trim(name_vi) = ''", Long.class));
        var chicken = reference.searchFoods("ức gà nướng", null, "USDA_FNDDS", 1, 50, null);
        assertTrue(chicken.getContent().stream().anyMatch(f -> f.sourceFoodCode().equals("24122131")), names(chicken).toString());
        assertTrue(chicken.getContent().stream().allMatch(f -> !f.displayName().equals(f.localName())),
                "USDA foods show the English name, the translation is only localName");
        assertEquals(chicken.getTotalElements(), reference.searchFoods("uc ga nuong", null, "USDA_FNDDS", 1, 50, null).getTotalElements());
    }

    @Test
    void guidanceCatalogStillPointsAtUsdaFoods() {
        assertEquals(29, jdbc.queryForObject("""
                select count(*) from nutrition_guidance_foods g join nutrition_foods f on f.id = g.nutrition_food_id
                where f.source = 'USDA_FNDDS'""", Long.class));
    }

    private static Set<String> ids(List<NutritionFoodResponse> foods) {
        return new HashSet<>(foods.stream().map(NutritionFoodResponse::id).toList());
    }

    // ---------------------------------------------------------------- diet prescription (V26) + AF rules (V28)

    /** Ngưỡng khởi tạo của V28 (bộ quy tắc rung nhĩ). */
    private static final Map<DietRuleCode, DietThreshold> V28_DEFAULTS = DietAdvisorTest.V28_DEFAULTS;
    private static final DietProfile WARFARIN = new DietProfile(java.util.Set.of(DietRuleCode.SODIUM, DietRuleCode.VITAMIN_K, DietRuleCode.ALCOHOL,
            DietRuleCode.CAFFEINE), true, V28_DEFAULTS);
    private static final DietProfile GENERAL = DietProfile.general(V28_DEFAULTS);
    private static final DietProfile AF = DietAdvisorTest.AF;

    private DietAdvice advice(String id, DietProfile diet) {
        return reference.getFood(id, diet).advice();
    }

    private List<String> reasonCodes(DietAdvice advice) {
        return advice.reasons().stream().map(DietAdvice.Reason::code).toList();
    }

    private String firstFood(String where) {
        return String.valueOf(jdbc.queryForObject("select id from nutrition_foods where " + where + " order by id limit 1",
                Long.class));
    }

    @Test
    void v26FillsAlcoholOfVietnameseDrinksFromTheBook() {
        assertEquals(4.5, column("14001", "alcohol_g"), "Bia (cồn: 4,5 g)");
        assertEquals(39, column("14012", "alcohol_g"), "Rượu trắng (cồn 39 g)");
        assertEquals(0, column("14005", "alcohol_g"), "Nước cam tươi");
        assertEquals(0, jdbc.queryForObject("select count(*) from nutrition_foods where source = 'VN_FCT' "
                + "and group_id = 'BEVERAGE' and alcohol_g is null", Long.class));
    }

    @Test
    void v28SeedsTheAfRulesInPriorityOrderWithTheirSources() {
        assertEquals(V28_DEFAULTS, rules.defaults());
        List<DietRuleResponse> list = rules.list();
        assertEquals(List.of("ALCOHOL", "CAFFEINE", "SUGARS", "NA_K_RATIO", "SODIUM", "SATURATED_FAT", "MAGNESIUM",
                "VITAMIN_K"), list.stream().map(DietRuleResponse::code).toList());
        assertTrue(list.stream().allMatch(r -> r.evidence() != null && !r.evidence().isBlank()), list.toString());
        DietRuleResponse sodium = list.get(4);
        assertTrue(sodium.evidence().contains("Arch Intern Med 2008;168(7):713-720"), sodium.evidence());
        assertEquals("https://pubmed.ncbi.nlm.nih.gov/18413553/", sodium.evidenceUrl());
        assertEquals(3, sodium.priority());
        assertTrue(sodium.overridable());
        assertTrue(list.stream().allMatch(DietRuleResponse::overridable), "doctors can tune every rule per member");
    }

    @Test
    void prescriptionDefaultsToTheAfBaseThenIsOverwrittenByTheDoctor() {
        long member = 9_000_001L;
        NutritionDietPrescriptionResponse none = prescriptions.get(member);
        assertFalse(none.personalized());
        assertFalse(none.limitSodium(), "no doctor flags without a prescription");
        assertNull(none.prescribedBy());
        assertEquals(GENERAL, prescriptions.profileOf(member));
        var noneSodium = rule(none, "SODIUM");
        assertFalse(noneSodium.enabled(), "rules only apply once the doctor ticks them");
        assertFalse(noneSodium.prescribed());
        assertEquals(400.0, noneSodium.effectiveLimit());
        assertFalse(rule(none, "VITAMIN_K").enabled());
        assertEquals(1.0, rule(none, "NA_K_RATIO").effectiveGood());

        prescriptions.save(member, 77L, 555L, new UpdateDietPrescriptionRequest(true, true, false, true, "  Ăn thêm cá  ", null));
        NutritionDietPrescriptionResponse saved = prescriptions.get(member);
        assertTrue(saved.personalized());
        assertTrue(saved.onWarfarin());
        assertFalse(saved.avoidAlcohol());
        assertEquals("Ăn thêm cá", saved.note());
        assertEquals(77L, saved.prescribedBy());
        assertEquals(555L, saved.consultationSessionId());
        assertEquals(new DietProfile(java.util.Set.of(DietRuleCode.SODIUM, DietRuleCode.VITAMIN_K, DietRuleCode.CAFFEINE), true,
                V28_DEFAULTS), prescriptions.profileOf(member));
        assertFalse(rule(saved, "ALCOHOL").enabled(), "alcohol was not ticked");
        assertTrue(rule(saved, "SODIUM").enabled());
        assertTrue(rule(saved, "VITAMIN_K").enabled());

        prescriptions.save(member, 88L, 556L, new UpdateDietPrescriptionRequest(false, false, true, false, " ", null));
        NutritionDietPrescriptionResponse updated = prescriptions.get(member);
        assertFalse(updated.onWarfarin());
        assertNull(updated.note(), "blank note is cleared");
        assertEquals(88L, updated.prescribedBy());
        assertEquals(1, jdbc.queryForObject("select count(*) from nutrition_diet_prescriptions where member_id = ?",
                Long.class, member), "one row per member");
        error(ErrorCode.INVALID_PARAMETER, () -> prescriptions.save(member, 77L, 555L,
                new UpdateDietPrescriptionRequest(true, false, false, false, "x".repeat(1001), null)));
    }

    private static NutritionDietPrescriptionResponse.Rule rule(NutritionDietPrescriptionResponse prescription, String code) {
        return prescription.rules().stream().filter(r -> r.code().equals(code)).findFirst().orElseThrow();
    }

    @Test
    void realFoodsAreRatedByTheAfRules() {
        // Chưa có đơn: không chấm màu
        assertNull(advice("900014001", GENERAL));

        // Bia của sách (cồn điền ở V26): có cồn -> đỏ khi bác sĩ tick quy tắc cồn
        DietAdvice beer = advice("900014001", AF);
        assertEquals("LIMIT", beer.level());
        assertEquals("ALCOHOL_LIMIT", beer.reasons().getFirst().code());

        // Món USDA mặn, ít kali, không ngọt, ít chất béo bão hòa -> đỏ vì Na/K (ưu tiên 2) rồi natri (ưu tiên 3)
        String salty = firstFood("""
                source = 'USDA_FNDDS' and sodium_mg > 400 and potassium_mg > 0 and sodium_mg > 2 * potassium_mg
                  and sugars_g <= 2.5 and fat_saturated_g <= 1.5 and alcohol_g = 0 and caffeine_mg = 0""");
        assertEquals(List.of("NA_K_RATIO_LIMIT", "SODIUM_LIMIT"), reasonCodes(advice(salty, AF)));

        // Giàu magie, ít muối, kali hơn natri, không điểm xấu -> xanh với cả hai điểm tốt
        String magnesiumRich = firstFood("""
                source = 'USDA_FNDDS' and magnesium_mg >= 50 and sodium_mg <= 140 and potassium_mg >= sodium_mg
                  and sugars_g <= 2.5 and fat_saturated_g <= 1.5 and alcohol_g = 0 and caffeine_mg <= 80""");
        DietAdvice good = advice(magnesiumRich, AF);
        assertEquals("GOOD", good.level());
        assertEquals(List.of("NA_K_RATIO_GOOD", "MAGNESIUM_GOOD"), reasonCodes(good));

        // Trái cây nhiều đường tự nhiên: không bị chấm đường; kẹo thì có
        String sweetFruit = firstFood("source = 'USDA_FNDDS' and group_id = 'FRUIT' and sugars_g > 10");
        assertFalse(reasonCodes(advice(sweetFruit, AF)).stream().anyMatch(c -> c.startsWith("SUGARS")));
        String candy = firstFood("source = 'USDA_FNDDS' and group_id = 'SWEET' and sugars_g > 10");
        assertTrue(reasonCodes(advice(candy, AF)).contains("SUGARS_LIMIT"));

        // Rau muống: vitamin K chỉ tính khi bác sĩ ghi đang dùng warfarin
        DietAdvice spinach = advice("900004083", WARFARIN);
        assertTrue(reasonCodes(spinach).contains("VITAMIN_K_CAUTION"), spinach.toString());
        assertTrue(spinach.personalized());
        assertFalse(reasonCodes(advice("900004083", AF)).contains("VITAMIN_K_CAUTION"));

        // Mắm tôm: sách không có số liệu natri -> không tô xanh bừa
        String shrimpPaste = firstFood("source = 'VN_FCT' and name_vi like 'Mắm tôm%' and sodium_mg is null");
        DietAdvice unknown = advice(shrimpPaste, AF);
        assertTrue(reasonCodes(unknown).contains("SODIUM_UNKNOWN"), unknown.toString());
        assertNotEquals("GOOD", unknown.level());

        // Không truyền đơn (bác sĩ, quản trị) -> không chấm màu
        assertNull(advice("900004083", null));
    }

    @Test
    void searchResultsCarryTheSameAdviceAsTheDetail() {
        var page = reference.searchFoods("rau muong", null, "VN_FCT", 1, 20, WARFARIN);
        var spinach = page.getContent().stream().filter(f -> f.id().equals("900004083")).findFirst().orElseThrow();
        assertEquals(advice("900004083", WARFARIN), spinach.advice());
        assertTrue(reference.searchFoods("rau muong", null, "VN_FCT", 1, 20, null).getContent().stream()
                .allMatch(f -> f.advice() == null));
    }

    // ---------------------------------------------------------------- configurable thresholds (V27, V28)

    private void restoreDefaultRules() {
        rules.update(List.of(new DietThresholdRequest("SODIUM", 400.0, 140.0),
                new DietThresholdRequest("ALCOHOL", 0.0, null), new DietThresholdRequest("CAFFEINE", null, 80.0),
                new DietThresholdRequest("SUGARS", 10.0, 2.5), new DietThresholdRequest("NA_K_RATIO", 2.0, null, 1.0),
                new DietThresholdRequest("SATURATED_FAT", 5.0, 1.5),
                new DietThresholdRequest("MAGNESIUM", null, null, 50.0), new DietThresholdRequest("VITAMIN_K", null, 100.0)));
    }

    @Test
    void adminDefaultsDriveTheRatingAndAreValidated() {
        assertEquals(V28_DEFAULTS, rules.defaults());
        try {
            // Rau muống có 37 mg natri: đỏ khi admin hạ ngưỡng đỏ xuống 30 mg
            List<DietRuleResponse> updated = rules.update(List.of(new DietThresholdRequest("SODIUM", 30.0, 20.0),
                    new DietThresholdRequest("MAGNESIUM", null, null, 60.0)));
            assertEquals(30.0, updated.stream().filter(r -> r.code().equals("SODIUM")).findFirst().orElseThrow().limit());
            DietProfile general = prescriptions.profileOf(9_000_002L);
            assertEquals(new DietThreshold(30.0, 20.0), general.threshold(DietRuleCode.SODIUM));
            assertEquals(new DietThreshold(null, null, 60.0), general.threshold(DietRuleCode.MAGNESIUM));
            // Ngưỡng mặc định mới áp cho đơn tick quy tắc muối
            DietAdvice spinach = advice("900004083", new DietProfile(AF.prescribedRules(), true, general.thresholds()));
            assertEquals("LIMIT", spinach.level());
            assertTrue(reasonCodes(spinach).contains("SODIUM_LIMIT"), spinach.toString());
            assertTrue(spinach.reasons().stream().anyMatch(r -> r.message().contains("đỏ khi trên 30 mg")), spinach.toString());
        } finally {
            restoreDefaultRules();
        }
        assertEquals(V28_DEFAULTS, rules.defaults());

        error(ErrorCode.INVALID_PARAMETER, () -> rules.update(List.of(new DietThresholdRequest("SODIUM", 100.0, 200.0))));
        error(ErrorCode.INVALID_PARAMETER, () -> rules.update(List.of(new DietThresholdRequest("SODIUM", -1.0, null))));
        error(ErrorCode.INVALID_PARAMETER, () -> rules.update(List.of(new DietThresholdRequest("SODIUM", null, null))));
        error(ErrorCode.INVALID_PARAMETER, () -> rules.update(List.of(new DietThresholdRequest("SUGAR", 1.0, null))));
        // Mức không thuộc quy tắc: natri không có mức tốt, magie chỉ có mức tốt; mức tốt Na/K không vượt ngưỡng đỏ
        error(ErrorCode.INVALID_PARAMETER, () -> rules.update(List.of(new DietThresholdRequest("SODIUM", 400.0, 140.0, 10.0))));
        error(ErrorCode.INVALID_PARAMETER, () -> rules.update(List.of(new DietThresholdRequest("MAGNESIUM", 100.0, null, 50.0))));
        error(ErrorCode.INVALID_PARAMETER, () -> rules.update(List.of(new DietThresholdRequest("NA_K_RATIO", 2.0, null, 3.0))));
        error(ErrorCode.INVALID_PARAMETER, () -> rules.update(List.of(new DietThresholdRequest("SODIUM", 400.0, 140.0),
                new DietThresholdRequest("SODIUM", 500.0, 100.0))));
        error(ErrorCode.INVALID_PARAMETER, () -> rules.update(List.of()));
        assertEquals(V28_DEFAULTS, rules.defaults(), "rejected updates change nothing");
    }

    @Test
    void doctorOverridesPerMemberWinOverDefaultsAndFallBackWhenBlank() {
        long member = 9_000_003L;
        // Riêng bệnh nhân này: muối đỏ khi trên 300 mg; vàng để trống -> vẫn 140 mg mặc định
        var saved = prescriptions.save(member, 77L, 555L, new UpdateDietPrescriptionRequest(true, false, false, false,
                null, List.of(new DietThresholdRequest("SODIUM", 300.0, null))));
        var sodium = rule(saved, "SODIUM");
        assertEquals(300.0, sodium.limit());
        assertNull(sodium.caution());
        assertEquals(400.0, sodium.defaultLimit());
        assertEquals(300.0, sodium.effectiveLimit());
        assertEquals(140.0, sodium.effectiveCaution());
        assertTrue(sodium.prescribed());
        assertEquals(new DietThreshold(300.0, 140.0), prescriptions.profileOf(member).threshold(DietRuleCode.SODIUM));

        // Món 320-380 mg natri, không điểm xấu nào khác: đỏ với bệnh nhân này, chỉ vàng với ngưỡng mặc định
        String mid = firstFood("""
                source = 'USDA_FNDDS' and sodium_mg > 320 and sodium_mg < 380 and sugars_g <= 2.5
                  and fat_saturated_g <= 1.5 and alcohol_g = 0 and caffeine_mg = 0""");
        assertEquals("LIMIT", advice(mid, prescriptions.profileOf(member)).level());
        assertEquals("CAUTION", advice(mid, AF).level());

        // Không gửi ngưỡng -> về mặc định
        prescriptions.save(member, 77L, 555L, new UpdateDietPrescriptionRequest(true, false, false, false, null, null));
        assertEquals(new DietThreshold(400.0, 140.0), prescriptions.profileOf(member).threshold(DietRuleCode.SODIUM));

        // Ngưỡng vàng riêng cao hơn ngưỡng đỏ mặc định -> cặp ngưỡng ngược nhau, từ chối
        error(ErrorCode.INVALID_PARAMETER, () -> prescriptions.save(member, 77L, 555L, new UpdateDietPrescriptionRequest(
                true, false, false, false, null, List.of(new DietThresholdRequest("SODIUM", null, 800.0)))));
        error(ErrorCode.INVALID_PARAMETER, () -> prescriptions.save(member, 77L, 555L, new UpdateDietPrescriptionRequest(
                true, false, false, false, null, List.of(new DietThresholdRequest("SUGAR", 1.0, null)))));
        // Mức không thuộc quy tắc: đường không có mức tốt, Na/K không có mức vàng; mức tốt Na/K không vượt ngưỡng đỏ
        error(ErrorCode.INVALID_PARAMETER, () -> prescriptions.save(member, 77L, 555L, new UpdateDietPrescriptionRequest(
                true, false, false, false, null, List.of(new DietThresholdRequest("SUGARS", null, null, 1.0)))));
        error(ErrorCode.INVALID_PARAMETER, () -> prescriptions.save(member, 77L, 555L, new UpdateDietPrescriptionRequest(
                true, false, false, false, null, List.of(new DietThresholdRequest("NA_K_RATIO", null, 1.5)))));
        error(ErrorCode.INVALID_PARAMETER, () -> prescriptions.save(member, 77L, 555L, new UpdateDietPrescriptionRequest(
                true, false, false, false, null, List.of(new DietThresholdRequest("NA_K_RATIO", null, null, 2.5)))));
    }

    @Test
    void doctorCanPrescribeTheFourV28RulesWithTheirOwnThresholds() {
        long member = 9_000_004L;
        var saved = prescriptions.save(member, 77L, 555L, new UpdateDietPrescriptionRequest(false, false, false, false,
                true, true, true, true, null, List.of(new DietThresholdRequest("SUGARS", 8.0, 2.0),
                new DietThresholdRequest("NA_K_RATIO", 1.5, null, 0.8),
                new DietThresholdRequest("SATURATED_FAT", null, 1.0),
                new DietThresholdRequest("MAGNESIUM", null, null, 80.0))));
        assertTrue(saved.limitSugars() && saved.watchSodiumPotassium() && saved.limitSaturatedFat()
                && saved.encourageMagnesium());
        assertEquals(0.8, rule(saved, "NA_K_RATIO").good());
        assertEquals(1.0, rule(saved, "NA_K_RATIO").defaultGood());
        assertEquals(1.5, rule(saved, "NA_K_RATIO").effectiveLimit());
        assertEquals(5.0, rule(saved, "SATURATED_FAT").effectiveLimit(), "blank red keeps the default");
        assertEquals(1.0, rule(saved, "SATURATED_FAT").effectiveCaution());
        assertTrue(rule(saved, "MAGNESIUM").prescribed());

        DietProfile profile = prescriptions.profileOf(member);
        assertEquals(new DietThreshold(8.0, 2.0), profile.threshold(DietRuleCode.SUGARS));
        assertEquals(new DietThreshold(1.5, null, 0.8), profile.threshold(DietRuleCode.NA_K_RATIO));
        assertEquals(new DietThreshold(null, null, 80.0), profile.threshold(DietRuleCode.MAGNESIUM));
        assertTrue(profile.enabled(DietRuleCode.SUGARS));
        assertFalse(profile.enabled(DietRuleCode.SODIUM));

        // Kẹo nhiều đường: lời nhắn ghi bác sĩ dặn
        String candy = firstFood("source = 'USDA_FNDDS' and group_id = 'SWEET' and sugars_g > 10");
        assertTrue(advice(candy, profile).reasons().stream()
                .anyMatch(r -> r.code().equals("SUGARS_LIMIT") && r.message().contains("Bác sĩ dặn bạn hạn chế đồ ngọt")));
    }

    @Test
    void englishRequestsGetEnglishContent() {
        // V30 dịch đủ: mọi nhóm, món có khuyến nghị, quy tắc và phân loại của bảng Việt Nam đều có bản tiếng Anh
        assertEquals(0, jdbc.queryForObject("select count(*) from nutrition_food_groups where name_en is null", Long.class));
        assertEquals(0, jdbc.queryForObject("select count(*) from nutrition_guidance_foods where food_name_en is null "
                + "or food_name_specific_en is null or guidance_title_en is null or guidance_reason_en is null", Long.class));
        assertEquals(0, jdbc.queryForObject("select count(*) from nutrition_diet_rules where name_en is null", Long.class));
        assertEquals(0, jdbc.queryForObject("select count(*) from nutrition_foods where source = 'VN_FCT' "
                + "and category_en is null", Long.class));

        RequestLanguageScope.use("en");
        String vietnameseLetters = ".*[àáảãạăâđèéẻẽẹêìíỉĩịòóỏõọôơùúủũụưỳýỷỹỵ].*";
        assertTrue(catalog.getGroups().stream().noneMatch(g -> g.name().matches(vietnameseLetters)),
                catalog.getGroups().toString());
        NutritionFoodResponse milk = catalog.getGroupFoods("dairy").getFirst();
        assertFalse(milk.groupName().matches(vietnameseLetters), milk.groupName());
        assertFalse(milk.foodNameSpecific().matches(vietnameseLetters), milk.foodNameSpecific());
        assertFalse(milk.guidanceReason().matches(vietnameseLetters), milk.guidanceReason());
        assertTrue(milk.nutrients().stream().noneMatch(n -> n.name().matches(vietnameseLetters)), milk.nutrients().toString());

        // Món Việt Nam hiện tên tiếng Anh của sách, tên tiếng Việt giữ ở localName; món USDA không cần localName
        var rau = reference.getFood("900004083", null);
        assertFalse(rau.displayName().matches(vietnameseLetters), rau.displayName());
        assertEquals("Rau muống", rau.localName());
        assertFalse(rau.sourceCategory().matches(vietnameseLetters), rau.sourceCategory());
        var usda = reference.searchFoods("pho", null, "USDA_FNDDS", 1, 5, null).getContent();
        assertTrue(usda.stream().allMatch(f -> f.localName() == null), usda.toString());

        DietAdvice spinach = advice("900004083", WARFARIN);
        assertTrue(spinach.reasons().stream().noneMatch(r -> r.message().matches(vietnameseLetters)), spinach.toString());
        assertTrue(rules.list().stream().noneMatch(r -> r.name().matches(vietnameseLetters)));
    }
}
