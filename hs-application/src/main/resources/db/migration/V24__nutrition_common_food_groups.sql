-- =====================================================================
-- V24: Một bộ nhóm thực phẩm chung cho mọi thực phẩm
--
-- Trước đây nutrition_food_groups chỉ có 8 nhóm của danh mục khuyến nghị (V22), còn
-- mỗi nguồn dữ liệu có cách phân loại riêng trong nutrition_foods.category (USDA: 171
-- nhóm WWEIA tiếng Anh; Việt Nam: 14 nhóm của sách). Migration này:
--  1. Biến nutrition_food_groups thành bộ nhóm chung, theo khung nhóm của Bảng thành phần
--     thực phẩm Việt Nam, thêm "Món ăn hỗn hợp" cho món nhiều thành phần và "Khác".
--     Nhóm chỉ là phân loại; mức khuyến nghị nằm ở từng món (nutrition_guidance_foods.guidance)
--     nên bỏ cột dietary_pattern.
--  2. Mọi dòng nutrition_foods có group_id. category giữ nguyên làm phân loại gốc của nguồn.
--  3. Món có khuyến nghị lấy nhóm theo thực phẩm nó trỏ tới, nên bỏ group_id của
--     nutrition_guidance_foods để không có hai chỗ lưu lệch nhau.
--  4. Sửa 3 tên bị tách sai ở V23.
-- Giữ id + slug của 5 nhóm cũ trùng khái niệm (FISH, DAIRY, VEGETABLE, FRUIT, LEGUMES_NUTS).
-- =====================================================================

ALTER TABLE nutrition_food_groups DROP COLUMN dietary_pattern;

-- 1. Bộ nhóm chung
UPDATE nutrition_food_groups SET slug = 'legumes-nuts', name = 'Đậu, đỗ và các loại hạt', description = 'Các loại đậu, đỗ, lạc, vừng, hạt điều, hạnh nhân và sản phẩm như đậu phụ, sữa đậu nành, bơ lạc.', icon = 'Nut', display_order = 3, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-food-groups-v24' WHERE id = 'LEGUMES_NUTS';
UPDATE nutrition_food_groups SET slug = 'vegetables', name = 'Rau củ', description = 'Rau lá, rau gia vị, củ và quả dùng làm rau, nấm, rong biển, rau muối chua.', icon = 'Carrot', display_order = 4, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-food-groups-v24' WHERE id = 'VEGETABLE';
UPDATE nutrition_food_groups SET slug = 'fruits', name = 'Trái cây', description = 'Quả chín ăn tươi, quả sấy khô và quả ngâm đường.', icon = 'Apple', display_order = 5, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-food-groups-v24' WHERE id = 'FRUIT';
UPDATE nutrition_food_groups SET slug = 'fish-seafood', name = 'Cá và hải sản', description = 'Cá, tôm, cua, mực, nhuyễn thể và sản phẩm chế biến từ thủy sản.', icon = 'Fish', display_order = 7, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-food-groups-v24' WHERE id = 'FISH';
UPDATE nutrition_food_groups SET slug = 'dairy', name = 'Sữa và sản phẩm từ sữa', description = 'Sữa, sữa chua, phô mai, kem sữa và sữa công thức cho trẻ nhỏ.', icon = 'Milk', display_order = 9, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-food-groups-v24' WHERE id = 'DAIRY';

INSERT INTO nutrition_food_groups (id, slug, name, description, icon, image_url, display_order, created_at, updated_at, created_by, updated_by) VALUES
    ('CEREAL', 'cereals', 'Ngũ cốc và sản phẩm', 'Gạo, ngô, lúa mì, yến mạch và các sản phẩm như cơm, bánh mì, mì, bún, phở, ngũ cốc ăn sáng, bánh quy mặn.', 'Wheat', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-food-groups-v24', 'migration:nutrition-food-groups-v24'),
    ('TUBER', 'tubers', 'Khoai củ', 'Khoai tây, khoai lang, khoai môn, sắn, củ dong và sản phẩm như miến, bột củ, khoai chiên.', 'Sprout', NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-food-groups-v24', 'migration:nutrition-food-groups-v24'),
    ('MEAT', 'meat', 'Thịt và sản phẩm từ thịt', 'Thịt gia súc, gia cầm, nội tạng và sản phẩm chế biến như giò, chả, xúc xích, thịt xông khói.', 'Beef', NULL, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-food-groups-v24', 'migration:nutrition-food-groups-v24'),
    ('EGG', 'eggs', 'Trứng', 'Trứng gia cầm và các món chủ yếu từ trứng.', 'Egg', NULL, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-food-groups-v24', 'migration:nutrition-food-groups-v24'),
    ('FAT_OIL', 'fats-oils', 'Dầu, mỡ, bơ', 'Dầu thực vật, mỡ động vật, bơ và bơ thực vật.', 'Droplet', NULL, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-food-groups-v24', 'migration:nutrition-food-groups-v24'),
    ('SWEET', 'sweets', 'Đường, bánh kẹo', 'Đường, mật ong, kẹo, mứt, bánh ngọt, kem lạnh và các món tráng miệng.', 'Candy', NULL, 11, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-food-groups-v24', 'migration:nutrition-food-groups-v24'),
    ('CONDIMENT', 'condiments', 'Gia vị, nước chấm, nước sốt', 'Gia vị, nước mắm, nước tương, tương ớt, nước sốt và sốt trộn salad.', 'Soup', NULL, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-food-groups-v24', 'migration:nutrition-food-groups-v24'),
    ('BEVERAGE', 'beverages', 'Đồ uống', 'Nước, nước ép, nước giải khát, cà phê, trà, bia, rượu và đồ uống dinh dưỡng.', 'CupSoda', NULL, 13, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-food-groups-v24', 'migration:nutrition-food-groups-v24'),
    ('MIXED_DISH', 'mixed-dishes', 'Món ăn hỗn hợp', 'Món nấu từ nhiều nhóm thực phẩm: bánh mì kẹp, pizza, súp, món xào, cơm và mì trộn sẵn.', 'CookingPot', NULL, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-food-groups-v24', 'migration:nutrition-food-groups-v24'),
    ('OTHER', 'other', 'Khác', 'Thực phẩm chưa xếp được vào nhóm nào ở trên, ví dụ bột dinh dưỡng.', 'Package', NULL, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-food-groups-v24', 'migration:nutrition-food-groups-v24');

-- 2. Nhóm của từng thực phẩm
ALTER TABLE nutrition_foods ADD COLUMN group_id VARCHAR(40) REFERENCES nutrition_food_groups(id);
COMMENT ON COLUMN nutrition_foods.group_id IS 'Nhom chung (nutrition_food_groups). category la phan loai goc cua nguon';

-- 2a. Bảng thành phần thực phẩm Việt Nam: theo nhóm của sách
UPDATE nutrition_foods f SET group_id = m.group_id, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-food-groups-v24'
FROM (VALUES
    ('Ngũ cốc và sản phẩm chế biến', 'CEREAL'),
    ('Khoai củ và sản phẩm chế biến', 'TUBER'),
    ('Hạt, quả giàu đạm, béo và sản phẩm chế biến', 'LEGUMES_NUTS'),
    ('Rau, quả, củ dùng làm rau', 'VEGETABLE'),
    ('Quả chín', 'FRUIT'),
    ('Dầu, mỡ, bơ', 'FAT_OIL'),
    ('Thịt và sản phẩm chế biến', 'MEAT'),
    ('Thủy sản và sản phẩm chế biến', 'FISH'),
    ('Trứng và sản phẩm chế biến', 'EGG'),
    ('Sữa và sản phẩm chế biến', 'DAIRY'),
    ('Đồ ngọt (đường, bánh, mứt, kẹo)', 'SWEET'),
    ('Gia vị, nước chấm', 'CONDIMENT'),
    ('Nước giải khát, bia, rượu', 'BEVERAGE')
) AS m (category, group_id)
WHERE f.source = 'VN_FCT' AND f.category = m.category;

-- Nhóm "Đồ hộp" của sách là cách bảo quản, không phải loại thực phẩm: xếp từng món theo loại
UPDATE nutrition_foods f SET group_id = m.group_id, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-food-groups-v24'
FROM (VALUES
    ('11001', 'FRUIT'),
    ('11002', 'VEGETABLE'),
    ('11003', 'FRUIT'),
    ('11004', 'LEGUMES_NUTS'),
    ('11005', 'FRUIT'),
    ('11006', 'FRUIT'),
    ('11007', 'SWEET'),
    ('11008', 'SWEET'),
    ('11009', 'SWEET'),
    ('11010', 'SWEET'),
    ('11011', 'SWEET'),
    ('11012', 'FRUIT'),
    ('11013', 'BEVERAGE'),
    ('11014', 'FRUIT'),
    ('11015', 'FISH'),
    ('11016', 'FISH'),
    ('11017', 'MEAT'),
    ('11018', 'MEAT'),
    ('11019', 'MEAT'),
    ('11020', 'MEAT'),
    ('11021', 'MEAT')
) AS m (source_food_code, group_id)
WHERE f.source = 'VN_FCT' AND f.source_food_code = m.source_food_code;

-- 2b. USDA FNDDS: theo nhóm WWEIA
UPDATE nutrition_foods f SET group_id = m.group_id, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-food-groups-v24'
FROM (VALUES
    ('Apple juice', 'BEVERAGE'),
    ('Apples', 'FRUIT'),
    ('Baby food: cereals', 'CEREAL'),
    ('Baby food: fruit', 'FRUIT'),
    ('Baby food: meat and dinners', 'MIXED_DISH'),
    ('Baby food: mixtures', 'MIXED_DISH'),
    ('Baby food: snacks and sweets', 'SWEET'),
    ('Baby food: vegetables', 'VEGETABLE'),
    ('Baby food: yogurt', 'DAIRY'),
    ('Baby juice', 'BEVERAGE'),
    ('Baby water', 'BEVERAGE'),
    ('Bacon', 'MEAT'),
    ('Bagels and English muffins', 'CEREAL'),
    ('Bananas', 'FRUIT'),
    ('Bean, pea, legume dishes', 'MIXED_DISH'),
    ('Beans, peas, legumes', 'LEGUMES_NUTS'),
    ('Beef, excludes ground', 'MEAT'),
    ('Beer', 'BEVERAGE'),
    ('Biscuits, muffins, quick breads', 'CEREAL'),
    ('Blueberries and other berries', 'FRUIT'),
    ('Bottled water', 'BEVERAGE'),
    ('Broccoli', 'VEGETABLE'),
    ('Burgers', 'MIXED_DISH'),
    ('Burritos and tacos', 'MIXED_DISH'),
    ('Butter and animal fats', 'FAT_OIL'),
    ('Cabbage', 'VEGETABLE'),
    ('Cakes and pies', 'SWEET'),
    ('Candy containing chocolate', 'SWEET'),
    ('Candy not containing chocolate', 'SWEET'),
    ('Carrots', 'VEGETABLE'),
    ('Cereal bars', 'CEREAL'),
    ('Cheese', 'DAIRY'),
    ('Cheese sandwiches', 'MIXED_DISH'),
    ('Chicken fillet sandwiches', 'MIXED_DISH'),
    ('Chicken patties, nuggets and tenders', 'MEAT'),
    ('Chicken, whole pieces', 'MEAT'),
    ('Citrus fruits', 'FRUIT'),
    ('Citrus juice', 'BEVERAGE'),
    ('Coffee', 'BEVERAGE'),
    ('Cold cuts and cured meats', 'MEAT'),
    ('Coleslaw, non-lettuce salads', 'VEGETABLE'),
    ('Cookies and brownies', 'SWEET'),
    ('Corn', 'CEREAL'),
    ('Cottage/ricotta cheese', 'DAIRY'),
    ('Crackers, excludes saltines', 'CEREAL'),
    ('Cream and cream substitutes', 'DAIRY'),
    ('Cream cheese, sour cream, whipped cream', 'DAIRY'),
    ('Deli and cured meat sandwiches', 'MIXED_DISH'),
    ('Diet soft drinks', 'BEVERAGE'),
    ('Diet sport and energy drinks', 'BEVERAGE'),
    ('Dips, gravies, other sauces', 'CONDIMENT'),
    ('Doughnuts, sweet rolls, pastries', 'SWEET'),
    ('Dried fruits', 'FRUIT'),
    ('Egg rolls, dumplings, sushi', 'MIXED_DISH'),
    ('Egg/breakfast sandwiches', 'MIXED_DISH'),
    ('Eggs and omelets', 'EGG'),
    ('Enhanced water', 'BEVERAGE'),
    ('Fish', 'FISH'),
    ('Flavored milk, lowfat', 'DAIRY'),
    ('Flavored milk, nonfat', 'DAIRY'),
    ('Flavored milk, reduced fat', 'DAIRY'),
    ('Flavored milk, whole', 'DAIRY'),
    ('Flavored or carbonated water', 'BEVERAGE'),
    ('Formula, prepared from powder', 'DAIRY'),
    ('Formula, ready-to-feed', 'DAIRY'),
    ('Frankfurter sandwiches', 'MIXED_DISH'),
    ('Frankfurters', 'MEAT'),
    ('French fries and other fried white potatoes', 'TUBER'),
    ('Fried rice and lo/chow mein', 'MIXED_DISH'),
    ('Fried vegetables', 'VEGETABLE'),
    ('Fruit drinks', 'BEVERAGE'),
    ('Gelatins, ices, sorbets', 'SWEET'),
    ('Grapes', 'FRUIT'),
    ('Grits and other cooked cereals', 'CEREAL'),
    ('Ground beef', 'MEAT'),
    ('Ice cream and frozen dairy desserts', 'SWEET'),
    ('Jams, syrups, toppings', 'SWEET'),
    ('Lamb, goat, game', 'MEAT'),
    ('Lettuce and lettuce salads', 'VEGETABLE'),
    ('Liquor and cocktails', 'BEVERAGE'),
    ('Liver and organ meats', 'MEAT'),
    ('Macaroni and cheese', 'MIXED_DISH'),
    ('Mango and papaya', 'FRUIT'),
    ('Margarine', 'FAT_OIL'),
    ('Mashed potatoes and white potato mixtures', 'TUBER'),
    ('Mayonnaise', 'CONDIMENT'),
    ('Meat and BBQ sandwiches', 'MIXED_DISH'),
    ('Meat mixed dishes', 'MIXED_DISH'),
    ('Melons', 'FRUIT'),
    ('Milk shakes and other dairy drinks', 'DAIRY'),
    ('Milk, lowfat', 'DAIRY'),
    ('Milk, nonfat', 'DAIRY'),
    ('Milk, reduced fat', 'DAIRY'),
    ('Milk, whole', 'DAIRY'),
    ('Mustard and other condiments', 'CONDIMENT'),
    ('Nachos', 'MIXED_DISH'),
    ('Nutrition bars', 'CEREAL'),
    ('Nutritional beverages', 'BEVERAGE'),
    ('Nuts and seeds', 'LEGUMES_NUTS'),
    ('Oatmeal', 'CEREAL'),
    ('Olives, pickles, pickled vegetables', 'VEGETABLE'),
    ('Onions', 'VEGETABLE'),
    ('Other Mexican mixed dishes', 'MIXED_DISH'),
    ('Other dark green vegetables', 'VEGETABLE'),
    ('Other diet drinks', 'BEVERAGE'),
    ('Other fruit juice', 'BEVERAGE'),
    ('Other fruits and fruit salads', 'FRUIT'),
    ('Other red and orange vegetables', 'VEGETABLE'),
    ('Other starchy vegetables', 'VEGETABLE'),
    ('Other vegetables and combinations', 'VEGETABLE'),
    ('Pancakes, waffles, French toast', 'CEREAL'),
    ('Pasta mixed dishes, excludes macaroni and cheese', 'MIXED_DISH'),
    ('Pasta sauces, tomato-based', 'CONDIMENT'),
    ('Pasta, noodles, cooked grains', 'CEREAL'),
    ('Peaches and nectarines', 'FRUIT'),
    ('Peanut butter and jelly sandwiches', 'MIXED_DISH'),
    ('Pears', 'FRUIT'),
    ('Pineapple', 'FRUIT'),
    ('Pizza', 'MIXED_DISH'),
    ('Plant-based milk', 'LEGUMES_NUTS'),
    ('Plant-based yogurt', 'LEGUMES_NUTS'),
    ('Popcorn', 'CEREAL'),
    ('Pork', 'MEAT'),
    ('Potato chips', 'TUBER'),
    ('Poultry mixed dishes', 'MIXED_DISH'),
    ('Pretzels/snack mix', 'CEREAL'),
    ('Protein and nutritional powders', 'OTHER'),
    ('Pudding', 'SWEET'),
    ('Ramen and Asian broth-based soups', 'MIXED_DISH'),
    ('Ready-to-eat cereal, higher sugar (>21.2g/100g)', 'CEREAL'),
    ('Ready-to-eat cereal, lower sugar (=<21.2g/100g)', 'CEREAL'),
    ('Rice', 'CEREAL'),
    ('Rice mixed dishes', 'MIXED_DISH'),
    ('Rolls and buns', 'CEREAL'),
    ('Salad dressings and vegetable oils', 'FAT_OIL'),
    ('Saltine crackers', 'CEREAL'),
    ('Sausages', 'MEAT'),
    ('Seafood mixed dishes', 'MIXED_DISH'),
    ('Seafood sandwiches', 'MIXED_DISH'),
    ('Shellfish', 'FISH'),
    ('Smoothies and grain drinks', 'BEVERAGE'),
    ('Soft drinks', 'BEVERAGE'),
    ('Soups, broth-based', 'MIXED_DISH'),
    ('Soups, cream-based', 'MIXED_DISH'),
    ('Soy and meat-alternative products', 'LEGUMES_NUTS'),
    ('Soy-based condiments', 'CONDIMENT'),
    ('Spinach', 'VEGETABLE'),
    ('Sport and energy drinks', 'BEVERAGE'),
    ('Stir-fry and soy-based sauce mixtures', 'MIXED_DISH'),
    ('Strawberries', 'FRUIT'),
    ('String beans', 'VEGETABLE'),
    ('Sugar substitutes', 'SWEET'),
    ('Sugars and honey', 'SWEET'),
    ('Tap water', 'BEVERAGE'),
    ('Tea', 'BEVERAGE'),
    ('Tomato-based condiments', 'CONDIMENT'),
    ('Tomatoes', 'VEGETABLE'),
    ('Tortilla, corn, other chips', 'CEREAL'),
    ('Tortillas', 'CEREAL'),
    ('Turkey, duck, other poultry', 'MEAT'),
    ('Turnovers and other grain-based items', 'MIXED_DISH'),
    ('Vegetable dishes', 'MIXED_DISH'),
    ('Vegetable juice', 'BEVERAGE'),
    ('Vegetable sandwiches/burgers', 'MIXED_DISH'),
    ('Vegetables on a sandwich', 'VEGETABLE'),
    ('White potatoes, baked or boiled', 'TUBER'),
    ('Wine', 'BEVERAGE'),
    ('Yeast breads', 'CEREAL'),
    ('Yogurt, Greek', 'DAIRY'),
    ('Yogurt, regular', 'DAIRY')
) AS m (category, group_id)
WHERE f.source = 'USDA_FNDDS' AND f.category = m.category;

-- Ngoại lệ theo mã FNDDS:
--  * khoai lang, sắn, khoai môn, chuối lá (mã 719x, 734x) USDA xếp vào rau -> Khoai củ, như sách Việt Nam
--  * quả bơ (mã 6x) USDA xếp vào rau -> Trái cây; sốt trộn salad (mã 83x) -> Gia vị, nước sốt
--  * 77 dòng USDA không xếp nhóm (bột pha, nguyên liệu) -> theo mã và tên
UPDATE nutrition_foods SET group_id = 'CEREAL' WHERE source = 'USDA_FNDDS' AND source_food_code IN (
    '57412000', '57601100', '57602100', '57602500', '99995000', '99995130', '99995135', '99995620', '99995625');
UPDATE nutrition_foods SET group_id = 'TUBER' WHERE source = 'USDA_FNDDS' AND source_food_code IN (
    '71900100', '71900200', '71905000', '71905008', '71905100', '71930120', '71930190', '71962040', '71970200', '73401000',
    '73403000', '73403010', '73403020', '73405000', '73405010', '73405020', '73406000', '73407000', '73407050', '73407060',
    '73409000', '73410200', '73410320', '73410340', '73410400', '73410500', '73420020', '73420100', '73420200', '99997100',
    '99997340');
UPDATE nutrition_foods SET group_id = 'VEGETABLE' WHERE source = 'USDA_FNDDS' AND source_food_code IN (
    '75109400', '75109500', '75109550', '75119000', '75232000', '75365000', '99997210', '99997220', '99997310', '99997410',
    '99997510', '99997515', '99997520', '99997525', '99997530', '99997535', '99997540', '99997545', '99997550', '99997555',
    '99997800', '99997802', '99997804', '99997805', '99997810', '99997815', '99997820');
UPDATE nutrition_foods SET group_id = 'FRUIT' WHERE source = 'USDA_FNDDS' AND source_food_code IN (
    '63105010');
UPDATE nutrition_foods SET group_id = 'MEAT' WHERE source = 'USDA_FNDDS' AND source_food_code IN (
    '89901000', '89901002', '89901004', '89901006', '89902100', '99992100', '99992230', '99992405');
UPDATE nutrition_foods SET group_id = 'FISH' WHERE source = 'USDA_FNDDS' AND source_food_code IN (
    '99992600', '99992610');
UPDATE nutrition_foods SET group_id = 'DAIRY' WHERE source = 'USDA_FNDDS' AND source_food_code IN (
    '11810000', '11825000', '99991400', '99991410');
UPDATE nutrition_foods SET group_id = 'FAT_OIL' WHERE source = 'USDA_FNDDS' AND source_food_code IN (
    '99998210');
UPDATE nutrition_foods SET group_id = 'CONDIMENT' WHERE source = 'USDA_FNDDS' AND source_food_code IN (
    '83100100', '83101000', '83101600', '83102000', '83103000', '83104000', '83105500', '83106000', '83109000', '83112000',
    '83112400', '83112950', '83112990', '83113500', '83114000', '83115000', '83200100', '83201000', '83201500', '83202020',
    '83203000', '83204500', '83205450', '83205560', '83206500', '83207000', '83208500', '83300100', '83300200', '83300250',
    '83300400', '83300500', '83300600', '83300750', '83300900', '83301000', '89901010', '89901020', '89901030', '89901040',
    '89901050', '99998130');
UPDATE nutrition_foods SET group_id = 'BEVERAGE' WHERE source = 'USDA_FNDDS' AND source_food_code IN (
    '11830150', '11830160', '11830165', '11830260', '11830400', '92191100', '92191200', '92191400', '92193000', '92193005',
    '92193025', '92307000', '92307400', '92900100', '92900110', '92900200', '92900300');
UPDATE nutrition_foods SET group_id = 'OTHER' WHERE source = 'USDA_FNDDS' AND source_food_code IN (
    '75236000');

-- Phòng hờ dòng nào ngoài dữ liệu của V21/V23 (thêm tay) để cột NOT NULL không làm hỏng migration
UPDATE nutrition_foods SET group_id = 'OTHER' WHERE group_id IS NULL;
ALTER TABLE nutrition_foods ALTER COLUMN group_id SET NOT NULL;
CREATE INDEX idx_nutrition_food_group ON nutrition_foods (group_id);

-- 3. Món có khuyến nghị lấy nhóm từ thực phẩm nó trỏ tới (index của cột này tự xóa theo)
ALTER TABLE nutrition_guidance_foods DROP COLUMN group_id;
DELETE FROM nutrition_food_groups WHERE id IN ('BEVERAGES_CAUTION', 'BEVERAGES_ALCOHOL', 'PROCESSED_FOODS');

-- 4. Sửa tên bị tách sai ở V23: dính chữ tiêu đề cột; sách không có tên tiếng Anh ("-") thì dùng tên Việt như các món khác
UPDATE nutrition_foods SET name_vi = 'Mứt đu đủ', updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-food-groups-v24'
WHERE source = 'VN_FCT' AND source_food_code = '11011';
UPDATE nutrition_foods SET name = name_vi, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-food-groups-v24'
WHERE source = 'VN_FCT' AND name = '-';
