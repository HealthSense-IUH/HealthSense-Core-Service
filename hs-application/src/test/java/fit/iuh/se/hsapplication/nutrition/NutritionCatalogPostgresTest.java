package fit.iuh.se.hsapplication.nutrition;

import fit.iuh.se.hsnutrition.dto.NutritionFoodGroupResponse;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse;
import fit.iuh.se.hsnutrition.dto.NutritionFoodResponse.NutrientValue;
import fit.iuh.se.hsnutrition.dto.NutritionReferenceCategoryResponse;
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
        assertEquals(2, flyway.migrate().migrationsExecuted);
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
        assertEquals(5431, jdbc.queryForObject("select count(*) from nutrition_foods", Long.class));
        assertEquals(0, jdbc.queryForObject(
                "select count(*) from nutrition_guidance_foods where nutrition_food_id is null", Long.class));
    }

    @Test void groupsKeepDisplayOrderAndCountTheirFoods() {
        List<NutritionFoodGroupResponse> groups = catalog.getGroups();
        assertEquals(List.of("FISH", "DAIRY", "VEGETABLE", "FRUIT", "LEGUMES_NUTS", "BEVERAGES_CAUTION",
                "BEVERAGES_ALCOHOL", "PROCESSED_FOODS"), groups.stream().map(NutritionFoodGroupResponse::id).toList());
        assertEquals(29, groups.stream().mapToLong(NutritionFoodGroupResponse::foodCount).sum());
        assertEquals(7, groups.get(1).foodCount());
        assertEquals("BALANCED", groups.get(1).dietaryPattern());
    }

    @Test void groupIsFoundByIdOrSlugAndUnknownGroupIsNotFound() {
        assertEquals("FISH", catalog.getGroup("FISH").id());
        assertEquals("FISH", catalog.getGroup("fish-seafood").id());
        assertEquals(4, catalog.getGroup("fish-seafood").foodCount());
        error(ErrorCode.ENTITY_NOT_FOUND, () -> catalog.getGroup("no-such-group"));
        error(ErrorCode.ENTITY_NOT_FOUND, () -> catalog.getGroupFoods("no-such-group"));
    }

    @Test void groupFoodsComeInDisplayOrderAndBelongToTheGroup() {
        List<NutritionFoodResponse> dairy = catalog.getGroupFoods("dairy");
        assertEquals(7, dairy.size());
        assertEquals("milk-low-fat-1", dairy.getFirst().id());
        assertTrue(dairy.stream().allMatch(f -> f.group().equals("DAIRY")));
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
        return page.getContent().stream().map(NutritionReferenceFoodSummaryResponse::name).toList();
    }

    @Test void browsingWithoutAQueryListsEveryFoodByName() {
        var first = reference.searchFoods("", null, 1, 20);
        assertEquals(5431, first.getTotalElements());
        assertEquals(272, first.getTotalPages());
        assertTrue(first.isHasMore());
        assertEquals(jdbc.queryForList("select name from nutrition_foods order by name, id limit 20", String.class),
                names(first));
        var last = reference.searchFoods(null, null, 272, 20);
        assertEquals(11, last.getContent().size());
        assertFalse(last.isHasMore());
        assertTrue(reference.searchFoods("", null, 273, 20).getContent().isEmpty());
    }

    @Test void searchMatchesWordPrefixesInAnyOrder() {
        var page = reference.searchFoods("salm bak", null, 1, 50);
        assertFalse(page.getContent().isEmpty());
        assertTrue(names(page).contains("Fish, salmon, baked or broiled"));
        assertTrue(names(page).stream().map(String::toLowerCase).allMatch(n -> n.contains("salm") && n.contains("bak")),
                "every token must match: " + names(page));
        assertEquals(page.getTotalElements(), reference.searchFoods("BAKED salmon", null, 1, 50).getTotalElements());
    }

    @Test void searchStripsVietnameseDiacritics() {
        var pho = reference.searchFoods("phở", null, 1, 20);
        assertEquals(Set.of("Soup, pho, with meat", "Soup, pho, no meat"), new HashSet<>(names(pho)));
    }

    @Test void categoryFilterWorksAloneAndWithASearch() {
        String category = "Ramen and Asian broth-based soups";
        long inCategory = jdbc.queryForObject("select count(*) from nutrition_foods where category = ?", Long.class, category);
        var all = reference.searchFoods("", category, 1, 50);
        assertEquals(inCategory, all.getTotalElements());
        assertTrue(all.getContent().stream().allMatch(f -> category.equals(f.category())));
        assertEquals(2, reference.searchFoods("pho", category, 1, 20).getTotalElements());
        assertEquals(0, reference.searchFoods("pho", "Milk, whole", 1, 20).getTotalElements());
        assertEquals(0, reference.searchFoods("", "No such category", 1, 20).getTotalElements());
    }

    @Test void searchInputCannotInjectTsquerySyntax() {
        assertEquals(reference.searchFoods("salmon baked", null, 1, 20).getTotalElements(),
                reference.searchFoods("salmon & (baked | !", null, 1, 20).getTotalElements());
        assertEquals(0, reference.searchFoods("&&& :* |", null, 1, 20).getTotalElements());
        assertEquals(0, reference.searchFoods("with the", null, 1, 20).getTotalElements(), "stop words only");
        assertEquals(0, reference.searchFoods("'; drop table nutrition_foods; --", null, 1, 20).getTotalElements());
        assertEquals(5431, jdbc.queryForObject("select count(*) from nutrition_foods", Long.class));
    }

    @Test void summaryCarriesTheFourMainNutrientsPer100Grams() {
        // "with" is an English stop word, so this also matches "Soup, pho, no meat"; pick the row by code.
        var pho = reference.searchFoods("pho with meat", null, 1, 5).getContent().stream()
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
        assertEquals("Soup, pho, with meat", pho.name());
        assertEquals("USDA_FNDDS", pho.source());
        assertEquals(18, pho.nutrients().size());
        assertEquals(List.of(new NutritionReferenceFoodResponse.Portion("1 cup", 245, false),
                new NutritionReferenceFoodResponse.Portion("Quantity not specified", 245, true)), pho.portions());
        error(ErrorCode.ENTITY_NOT_FOUND, () -> reference.getFood("999999999"));
        error(ErrorCode.ENTITY_NOT_FOUND, () -> reference.getFood("not-a-number"));
    }

    @Test void categoriesCoverEveryFood() {
        List<NutritionReferenceCategoryResponse> categories = reference.getCategories();
        assertEquals(jdbc.queryForObject("select count(distinct category) from nutrition_foods", Long.class),
                categories.size());
        assertEquals(5431, categories.stream().mapToLong(NutritionReferenceCategoryResponse::foodCount).sum());
    }

    @Test void invalidPagingIsRejected() {
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", null, 0, 20));
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", null, 1, 0));
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("", null, 1, 51));
        error(ErrorCode.INVALID_PARAMETER, () -> reference.searchFoods("x".repeat(101), null, 1, 20));
    }

    private static Set<String> ids(List<NutritionFoodResponse> foods) {
        return new HashSet<>(foods.stream().map(NutritionFoodResponse::id).toList());
    }
}
