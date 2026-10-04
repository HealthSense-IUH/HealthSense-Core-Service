-- =====================================================================
-- V30: Noi dung tieng Anh cho du lieu dinh duong
--
-- Ung dung song ngu (Viet/Anh) nhung du lieu dinh duong da nap (V22-V28) chi co tieng Viet.
-- Migration nay:
--  1. Them cac cot *_en (nullable) ben canh cot tieng Viet tuong ung. Additive: khong doi, khong
--     xoa cot nao; dong nao chua co ban dich thi cot *_en de NULL.
--  2. Dien ban dich tieng Anh cua gia tri tieng Viet HIEN TAI (sau V24, V28) cho:
--     nhom thuc pham, mon co khuyen nghi, tom tat nguon bang chung, ten + nguon cua quy tac
--     cham mau, va ten chuong (category) cua Bang thanh phan thuc pham Viet Nam 2007 (VN_FCT).
--     category cua USDA da la tieng Anh nen khong dien category_en.
-- API: khi ngon ngu cua request (header `lang`) la tieng Anh thi tra cot *_en (neu NULL thi
-- quay ve cot tieng Viet); con lai tra cot tieng Viet nhu truoc.
-- Ban dich do nhom du an lam, giu nguyen y nghia va muc do than trong y khoa cua ban tieng Viet;
-- trich dan, URL va con so giu nguyen.
-- Khong doi updated_at/updated_by: day chi la ban dich, khong phai admin sua noi dung.
-- =====================================================================

ALTER TABLE nutrition_food_groups
    ADD COLUMN name_en VARCHAR(120),
    ADD COLUMN description_en VARCHAR(500);

ALTER TABLE nutrition_guidance_foods
    ADD COLUMN food_name_en VARCHAR(120),
    ADD COLUMN food_name_specific_en VARCHAR(160),
    ADD COLUMN description_en VARCHAR(1000),
    ADD COLUMN guidance_title_en VARCHAR(200),
    ADD COLUMN guidance_reason_en VARCHAR(1000),
    ADD COLUMN cardiovascular_context_en VARCHAR(1000),
    ADD COLUMN af_context_en VARCHAR(1000),
    ADD COLUMN medication_context_en VARCHAR(1000);

ALTER TABLE nutrition_evidence_sources
    ADD COLUMN summary_en VARCHAR(1000);

ALTER TABLE nutrition_diet_rules
    ADD COLUMN name_en VARCHAR(120),
    ADD COLUMN evidence_en TEXT;

ALTER TABLE nutrition_foods
    ADD COLUMN category_en VARCHAR(160);

COMMENT ON COLUMN nutrition_foods.category_en IS 'Ten tieng Anh cua category (chi VN_FCT); USDA da la tieng Anh';

-- ---------------------------------------------------------------------
-- 1. Nhom thuc pham (gia tri sau V24)
-- ---------------------------------------------------------------------
UPDATE nutrition_food_groups SET name_en = 'Cereals and grain products', description_en = 'Rice, corn, wheat, oats and products such as cooked rice, bread, noodles, rice vermicelli, pho noodles, breakfast cereals and savory crackers.' WHERE id = 'CEREAL';
UPDATE nutrition_food_groups SET name_en = 'Starchy roots and tubers', description_en = 'Potatoes, sweet potatoes, taro, cassava, arrowroot and products such as glass noodles, root starches and fries.' WHERE id = 'TUBER';
UPDATE nutrition_food_groups SET name_en = 'Legumes, nuts and seeds', description_en = 'Beans, peas, peanuts, sesame, cashews, almonds and products such as tofu, soy milk and peanut butter.' WHERE id = 'LEGUMES_NUTS';
UPDATE nutrition_food_groups SET name_en = 'Vegetables', description_en = 'Leafy vegetables, herbs, roots and fruits used as vegetables, mushrooms, seaweed and pickled vegetables.' WHERE id = 'VEGETABLE';
UPDATE nutrition_food_groups SET name_en = 'Fruits', description_en = 'Fresh ripe fruits, dried fruits and fruits preserved in sugar.' WHERE id = 'FRUIT';
UPDATE nutrition_food_groups SET name_en = 'Meat and meat products', description_en = 'Meat from livestock and poultry, organ meats and processed products such as Vietnamese pork rolls, sausages and bacon.' WHERE id = 'MEAT';
UPDATE nutrition_food_groups SET name_en = 'Fish and seafood', description_en = 'Fish, shrimp, crab, squid, molluscs and processed seafood products.' WHERE id = 'FISH';
UPDATE nutrition_food_groups SET name_en = 'Eggs', description_en = 'Poultry eggs and dishes made mainly from eggs.' WHERE id = 'EGG';
UPDATE nutrition_food_groups SET name_en = 'Milk and dairy products', description_en = 'Milk, yogurt, cheese, cream and infant formula.' WHERE id = 'DAIRY';
UPDATE nutrition_food_groups SET name_en = 'Oils, fats and butter', description_en = 'Vegetable oils, animal fats, butter and margarine.' WHERE id = 'FAT_OIL';
UPDATE nutrition_food_groups SET name_en = 'Sugars and sweets', description_en = 'Sugar, honey, candy, jams, cakes, ice cream and desserts.' WHERE id = 'SWEET';
UPDATE nutrition_food_groups SET name_en = 'Seasonings, dipping sauces and sauces', description_en = 'Seasonings, fish sauce, soy sauce, chili sauce, sauces and salad dressings.' WHERE id = 'CONDIMENT';
UPDATE nutrition_food_groups SET name_en = 'Beverages', description_en = 'Water, juices, soft drinks, coffee, tea, beer, wine and spirits, and nutritional drinks.' WHERE id = 'BEVERAGE';
UPDATE nutrition_food_groups SET name_en = 'Mixed dishes', description_en = 'Dishes made from several food groups: sandwiches, pizza, soups, stir-fries, and ready-made rice and noodle dishes.' WHERE id = 'MIXED_DISH';
UPDATE nutrition_food_groups SET name_en = 'Other', description_en = 'Foods that do not fit any of the groups above, such as nutritional powders.' WHERE id = 'OTHER';

-- ---------------------------------------------------------------------
-- 2. Mon co khuyen nghi (V22)
-- ---------------------------------------------------------------------
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Milk',
    food_name_specific_en = 'Low-fat milk (1%)',
    description_en = 'Cow''s milk with about 1% fat, lower in saturated fat than whole milk while still rich in calcium and potassium.',
    guidance_title_en = 'Suitable for a heart-healthy diet',
    guidance_reason_en = 'Low in saturated fat (0.6g/100g), helping to limit "bad" LDL cholesterol while still providing enough protein and minerals.',
    cardiovascular_context_en = 'Choosing low-fat or fat-free milk helps limit saturated fat intake and supports stable blood lipid levels.',
    af_context_en = 'Contains potassium and magnesium, electrolytes that play a role in the heart''s normal electrical activity.'
WHERE id = 'milk-low-fat-1';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Milk',
    food_name_specific_en = 'Skim milk (fat-free)',
    description_en = 'Cow''s milk with all of the fat removed (fat-free), the lowest in calories and saturated fat.',
    guidance_title_en = 'The leanest choice for people managing blood lipids and weight',
    guidance_reason_en = 'Almost no saturated fat (under 0.1g/100g), suitable for people who need tight control of their blood lipids.',
    cardiovascular_context_en = 'Helps meet calcium and potassium needs without adding to your daily saturated fat intake.',
    af_context_en = 'Provides electrolyte minerals that support nerve and muscle conduction in the heart.'
WHERE id = 'milk-skim';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Milk',
    food_name_specific_en = 'Reduced-fat milk (2%)',
    description_en = 'Moderately reduced-fat milk that keeps a light natural creaminess, with a moderate amount of saturated fat.',
    guidance_title_en = 'A sensible transition option',
    guidance_reason_en = 'Lower in saturated fat than whole milk (about 1.1g/100g); can be used in between before switching to 1% or skim milk.',
    cardiovascular_context_en = 'Take into account the saturated fat from your other meals of the day to keep it below the recommended 10% of total energy.'
WHERE id = 'milk-reduced-fat-2';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Milk',
    food_name_specific_en = 'Whole milk',
    description_en = 'Whole cow''s milk that keeps its natural fat content (about 3.2g fat/100g).',
    guidance_title_en = 'Watch your portions if you need to control blood lipids',
    guidance_reason_en = 'Higher in saturated fat than low-fat or skim milk (about 1.9g/100g). People with dyslipidemia should balance their intake.',
    cardiovascular_context_en = 'The American Heart Association (AHA) recommends that adults choose low-fat or fat-free dairy products to help control LDL cholesterol.'
WHERE id = 'milk-whole';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Milk',
    food_name_specific_en = 'Lactose-free low-fat milk (1%)',
    description_en = 'Low-fat milk in which the lactose has been broken down, gentle on sensitive digestive systems.',
    guidance_title_en = 'A good choice for people with lactose intolerance',
    guidance_reason_en = 'Keeps saturated fat low and avoids bloating and indigestion, without reducing calcium and potassium.'
WHERE id = 'milk-lactose-free-low-fat';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Milk',
    food_name_specific_en = 'Lactose-free skim milk',
    description_en = 'Fully skimmed milk combined with a process that removes lactose.',
    guidance_title_en = 'Low in fat, lactose-free and rich in minerals',
    guidance_reason_en = 'Almost no saturated fat, making it a safe choice for both the heart and the digestive system.'
WHERE id = 'milk-lactose-free-skim';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Milk',
    food_name_specific_en = 'Lactose-free whole milk',
    description_en = 'Lactose-free whole milk that keeps all of its natural fat.',
    guidance_title_en = 'Easy to digest, but watch the fat content',
    guidance_reason_en = 'Suitable for people who cannot tolerate lactose, but contains about as much saturated fat as regular whole milk.'
WHERE id = 'milk-lactose-free-whole';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Salmon',
    food_name_specific_en = 'Baked salmon',
    description_en = 'Salmon grilled plain or pan-seared with a little oil, preserving as much of its valuable omega-3 fatty acids as possible.',
    guidance_title_en = 'Highly recommended for heart health',
    guidance_reason_en = 'Rich in long-chain omega-3 fatty acids (EPA and DHA) and high-quality protein, and naturally low in sodium.',
    cardiovascular_context_en = 'A diet rich in oily fish (2 meals per week) is recommended by major cardiovascular guidelines to lower the risk of coronary heart disease and help regulate blood lipids.',
    af_context_en = 'Epidemiological studies and Mediterranean diet trials suggest that a diet rich in natural omega-3s helps protect heart muscle tissue and the lining of blood vessels.'
WHERE id = 'salmon-baked';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Salmon',
    food_name_specific_en = 'Deep-fried salmon',
    description_en = 'Battered, deep-fried salmon has significantly more total calories, trans fat and sodium.',
    guidance_title_en = 'Limit deep-fried preparations',
    guidance_reason_en = 'Deep-frying reduces the value of the omega-3s and adds large amounts of unhealthy fats and sodium from the batter.',
    cardiovascular_context_en = 'Instead of deep-frying, experts recommend light pan-searing, steaming or baking with natural herbs and spices.'
WHERE id = 'salmon-fried';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Mackerel',
    food_name_specific_en = 'Grilled mackerel',
    description_en = 'Plain grilled sea mackerel, a source of minerals and highly concentrated omega-3s.',
    guidance_title_en = 'A rich source of deep-sea omega-3s',
    guidance_reason_en = 'Very high in EPA and DHA, along with natural magnesium that is good for the heart muscle.',
    cardiovascular_context_en = 'One of the ideal oily fish in an eating pattern that benefits the circulatory system.'
WHERE id = 'mackerel-grilled';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Tuna',
    food_name_specific_en = 'Canned tuna (in water)',
    description_en = 'Tuna canned in water provides lean protein, is low in fat and is convenient for everyday meals.',
    guidance_title_en = 'A convenient source of lean protein',
    guidance_reason_en = 'Very low in saturated fat, but check the label to avoid varieties packed in brine with too much sodium.'
WHERE id = 'tuna-canned';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Spinach',
    food_name_specific_en = 'Boiled spinach',
    description_en = 'Spinach cooked plain without added fat, rich in potassium, magnesium and fiber.',
    guidance_title_en = 'A star food for a heart-healthy diet',
    guidance_reason_en = 'Naturally high in potassium and magnesium, low in calories and rich in fiber.',
    cardiovascular_context_en = 'A diet rich in dark leafy greens is a cornerstone of the DASH and Mediterranean diets and helps support blood pressure control.',
    af_context_en = 'Potassium and magnesium are electrolytes that play a role in the heart''s normal electrical activity.',
    medication_context_en = 'If you take warfarin, keep the amount of vitamin K in your diet relatively consistent. You do not need to cut out vitamin K-rich foods entirely on your own; instead, talk to your doctor so your dose can be adjusted to match your eating habits.'
WHERE id = 'spinach-cooked';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Spinach',
    food_name_specific_en = 'Raw spinach',
    description_en = 'Fresh, crisp young leaves, ideal for salads or green smoothies.',
    guidance_title_en = 'Rich in natural micronutrients that have not been heated',
    guidance_reason_en = 'Retains the most vitamin C, folate and natural antioxidants.',
    medication_context_en = 'People taking the anticoagulant warfarin should keep their intake consistent, just as with cooked spinach.'
WHERE id = 'spinach-raw';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Broccoli',
    food_name_specific_en = 'Boiled broccoli',
    description_en = 'Broccoli boiled until just tender, rich in fiber, vitamin C, potassium and sulforaphane.',
    guidance_title_en = 'An abundant source of fiber and natural micronutrients',
    guidance_reason_en = 'Very low in sodium, supporting healthy blood vessel walls and digestion.'
WHERE id = 'broccoli-cooked';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Tomato',
    food_name_specific_en = 'Fresh tomatoes',
    description_en = 'Ripe, juicy tomatoes eaten raw or cooked in soups, rich in lycopene and potassium.',
    guidance_title_en = 'Rich in potassium and natural antioxidants',
    guidance_reason_en = 'Very low in energy and free of saturated fat, suitable for everyday eating.'
WHERE id = 'tomato-raw';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Carrot',
    food_name_specific_en = 'Fresh carrots',
    description_en = 'Naturally sweet, crunchy carrots, rich in fiber and beta-carotene.',
    guidance_title_en = 'A healthy root vegetable with a mild natural sweetness',
    guidance_reason_en = 'A good source of fiber that helps regulate blood sugar and digestion.'
WHERE id = 'carrot-raw';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Banana',
    food_name_specific_en = 'Fresh ripe banana',
    description_en = 'A mildly sweet tropical fruit, well known as an easily absorbed source of natural potassium and magnesium.',
    guidance_title_en = 'A natural source of electrolyte minerals',
    guidance_reason_en = 'Provides plenty of natural potassium and magnesium, supporting electrolyte balance as part of everyday eating.',
    cardiovascular_context_en = 'Getting enough potassium from vegetables and fruits helps the kidneys excrete sodium and maintain healthy blood pressure.',
    af_context_en = 'Potassium and magnesium are electrolytes that play a role in the heart''s normal electrical activity.'
WHERE id = 'banana-raw';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Apple',
    food_name_specific_en = 'Fresh apple with skin',
    description_en = 'Crisp, sweet fresh apples, rich in soluble pectin fiber and flavonoid compounds.',
    guidance_title_en = 'Rich in soluble fiber that is good for blood vessels',
    guidance_reason_en = 'The pectin fiber in apples helps reduce the absorption of cholesterol from the digestive tract.'
WHERE id = 'apple-raw';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Orange',
    food_name_specific_en = 'Fresh orange',
    description_en = 'Fresh, juicy oranges, rich in vitamin C and natural antioxidants.',
    guidance_title_en = 'Provides fluids, vitamins and potassium',
    guidance_reason_en = 'Eat whole orange segments rather than only drinking the juice to get all of the soluble fiber.'
WHERE id = 'orange-raw';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Avocado',
    food_name_specific_en = 'Fresh avocado',
    description_en = 'Avocados are rich in healthy monounsaturated fatty acids (oleic acid), potassium and magnesium.',
    guidance_title_en = 'Healthy plant-based fat',
    guidance_reason_en = 'High in monounsaturated fat, combined with more potassium than most other fruits.',
    cardiovascular_context_en = 'Monounsaturated fats can help improve blood lipid levels when they replace animal fats rich in saturated fat.'
WHERE id = 'avocado-raw';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Almonds',
    food_name_specific_en = 'Plain dry-roasted almonds',
    description_en = 'Plain dry-roasted almonds with no added salt or sugar, rich in magnesium, vitamin E and plant protein.',
    guidance_title_en = 'A snack rich in magnesium and plant protein',
    guidance_reason_en = 'Naturally high in magnesium (over 250mg/100g), along with fiber and healthy fats.',
    af_context_en = 'Magnesium helps stabilize neuromuscular conduction and supports the heart''s normal electrical activity.'
WHERE id = 'almonds';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Peanuts',
    food_name_specific_en = 'Dry-roasted peanuts (unsalted)',
    description_en = 'Peanuts roasted without added salt, rich in plant protein and niacin.',
    guidance_title_en = 'An affordable source of plant protein and minerals',
    guidance_reason_en = 'Rich in plant protein and minerals. Choose unsalted roasted peanuts to avoid taking in too much sodium.'
WHERE id = 'peanuts';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Chickpeas',
    food_name_specific_en = 'Cooked chickpeas',
    description_en = 'Chickpeas cooked until tender, rich in soluble fiber and lean plant protein.',
    guidance_title_en = 'Excellent plant protein for heart health',
    guidance_reason_en = 'Low glycemic index and rich in soluble fiber, which helps control blood lipids.'
WHERE id = 'chickpeas';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Soybeans',
    food_name_specific_en = 'Boiled soybeans',
    description_en = 'A complete plant protein source that contains all the essential amino acids.',
    guidance_title_en = 'Complete protein to replace red meat',
    guidance_reason_en = 'High in potassium and protein, well suited to a diet that cuts back on fatty red meat.'
WHERE id = 'soybeans';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Coffee',
    food_name_specific_en = 'Pure brewed coffee (phin filter or machine)',
    description_en = 'Pure black coffee without added sugar or milk, containing caffeine and natural antioxidants.',
    guidance_title_en = 'Listen to how your heart rate responds',
    guidance_reason_en = 'Not everyone with atrial fibrillation needs to avoid caffeine completely. Many studies show that moderate caffeine intake (1-2 cups a day) usually does not trigger episodes of rapid heartbeat, although some people may be more sensitive.',
    cardiovascular_context_en = 'Pure coffee without added sugar or milk contains natural antioxidants and is generally safe for the cardiovascular system in reasonable amounts.',
    af_context_en = 'If you notice a racing heart, palpitations or a pounding in your chest after drinking coffee, cut back or switch to decaffeinated (decaf) coffee.'
WHERE id = 'coffee-brewed';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Green tea',
    food_name_specific_en = 'Hot brewed green tea',
    description_en = 'Green tea is rich in polyphenols (EGCG) and contains less caffeine than coffee.',
    guidance_title_en = 'Mild caffeine content; watch your personal sensitivity',
    guidance_reason_en = 'Green tea contains only about 1/4 of the caffeine in coffee and is rich in antioxidants, but people with a sensitive heart rhythm should still drink it in moderation.'
WHERE id = 'tea-brewed';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Beer',
    food_name_specific_en = 'Regular beer',
    description_en = 'An alcoholic drink fermented from grains and hops, containing about 4.5 - 5% alcohol.',
    guidance_title_en = 'Limit as much as possible',
    guidance_reason_en = 'Drinking alcohol is associated with the onset or recurrence of atrial fibrillation, especially in people who drink regularly or heavily.',
    cardiovascular_context_en = 'Alcohol can raise blood pressure, impair left ventricular contraction and interact adversely with cardiovascular medications.',
    af_context_en = 'The American College of Cardiology/American Heart Association guideline (ACC/AHA 2023) recommends that patients with atrial fibrillation abstain from or minimize alcohol consumption.'
WHERE id = 'beer-regular';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Wine',
    food_name_specific_en = 'Red wine',
    description_en = 'Wine fermented from grapes, with an alcohol content of about 12 - 14%.',
    guidance_title_en = 'Limit if you have heart rhythm symptoms',
    guidance_reason_en = 'Although it contains polyphenols, the alcohol in wine is still a trigger that can bring on paroxysmal atrial fibrillation in susceptible people.',
    cardiovascular_context_en = 'The cardiovascular benefits of its antioxidants do not outweigh the risk of arrhythmia and high blood pressure caused by alcohol.'
WHERE id = 'wine-red';
UPDATE nutrition_guidance_foods SET
    food_name_en = 'Sausage',
    food_name_specific_en = 'Smoked beef sausage',
    description_en = 'Processed meat that has been cured and smoked, very high in sodium (salt) and saturated fat.',
    guidance_title_en = 'Limit in your daily diet',
    guidance_reason_en = 'Very high sodium (over 800mg/100g) together with high saturated fat increases the burden on blood pressure.',
    cardiovascular_context_en = 'Eating a lot of processed meat is directly linked to high blood pressure and atherosclerosis.'
WHERE id = 'beef-sausage';

-- ---------------------------------------------------------------------
-- 3. Tom tat nguon bang chung (V22); title/authors/journal da la tieng Anh
-- ---------------------------------------------------------------------
UPDATE nutrition_evidence_sources SET summary_en = 'Recommends comprehensive management of lifestyle factors, limiting alcohol and maintaining a heart-healthy diet in patients with atrial fibrillation.' WHERE id = 'acc-aha-2023';
UPDATE nutrition_evidence_sources SET summary_en = 'Summarizes the evidence on weight loss, blood pressure control, the Mediterranean diet and triggers of paroxysmal AF.' WHERE id = 'aha-lifestyle-2020';
UPDATE nutrition_evidence_sources SET summary_en = 'A randomized controlled trial showing that a Mediterranean diet rich in unsaturated fats was associated with a lower risk of AF.' WHERE id = 'predimed-trial';
UPDATE nutrition_evidence_sources SET summary_en = 'Moderate caffeine intake (1-3 cups of coffee per day) is not associated with an increased risk of atrial fibrillation in the general population.' WHERE id = 'coffee-af-meta-2021';
UPDATE nutrition_evidence_sources SET summary_en = 'The risk of new-onset or recurrent AF rises with the amount of alcohol consumed, even at moderate to heavy drinking levels.' WHERE id = 'alcohol-af-review';
UPDATE nutrition_evidence_sources SET summary_en = 'Practical clinical guidance for patients with atrial fibrillation on sodium, potassium, magnesium, caffeine and interactions with anticoagulant drugs.' WHERE id = 'cleveland-dietary-af';

-- ---------------------------------------------------------------------
-- 4. Quy tac cham mau (V27, V28)
-- ---------------------------------------------------------------------
UPDATE nutrition_diet_rules SET name_en = 'Alcohol',
    evidence_en = 'Marcus GM, et al. Individualized Triggers of Paroxysmal Atrial Fibrillation: The I-STOP-AFib Randomized Clinical Trial. JAMA Cardiology 2022;7(2):167-174. Alcohol was the only factor that clearly increased the number of atrial fibrillation episodes.'
WHERE code = 'ALCOHOL';
UPDATE nutrition_diet_rules SET name_en = 'Caffeine',
    evidence_en = 'Precautionary threshold set by the system. The I-STOP-AFib trial (Marcus GM, et al. JAMA Cardiology 2022;7(2):167-174) did not find that caffeine increased atrial fibrillation episodes.'
WHERE code = 'CAFFEINE';
UPDATE nutrition_diet_rules SET name_en = 'Sugars (total sugars)',
    evidence_en = 'Precautionary threshold set by the system, based on total sugars (USDA FNDDS does not report added sugars); not applied to fruit and dairy. Reference: Lichtenstein AH, et al. 2021 Dietary Guidance to Improve Cardiovascular Health. Circulation 2021;144:e472-e487.'
WHERE code = 'SUGARS';
UPDATE nutrition_diet_rules SET name_en = 'Sodium/potassium ratio',
    evidence_en = 'Kieneker LM, et al. Low potassium excretion but not high sodium excretion is associated with increased risk of developing atrial fibrillation: the PREVEND study. Europace 2014. Supporting evidence: Cook NR, et al. Joint effects of sodium and potassium intake on subsequent cardiovascular disease. Arch Intern Med 2009;169(1):32-40.'
WHERE code = 'NA_K_RATIO';
UPDATE nutrition_diet_rules SET name_en = 'Salt (sodium)',
    evidence_en = 'Fung TT, et al. Adherence to a DASH-Style Diet and Risk of Coronary Heart Disease and Stroke in Women. Arch Intern Med 2008;168(7):713-720. Lichtenstein AH, et al. Circulation 2021;144:e472-e487. Per-100 g cut-offs follow nutrition labelling conventions (low sodium <= 140 mg).'
WHERE code = 'SODIUM';
UPDATE nutrition_diet_rules SET name_en = 'Saturated fat',
    evidence_en = 'Fung TT, et al. Arch Intern Med 2008;168(7):713-720 (DASH). Lichtenstein AH, et al. Circulation 2021;144:e472-e487. Per-100 g cut-offs follow the UK FSA nutrition label: low <= 1.5 g, high > 5 g.'
WHERE code = 'SATURATED_FAT';
UPDATE nutrition_diet_rules SET name_en = 'Magnesium',
    evidence_en = 'Khan AM, et al. Low Serum Magnesium and the Development of Atrial Fibrillation in the Community: The Framingham Heart Study. Circulation 2013;127(1):33-38.'
WHERE code = 'MAGNESIUM';
UPDATE nutrition_diet_rules SET name_en = 'Vitamin K',
    evidence_en = 'Warfarin is a vitamin K antagonist: people taking warfarin should keep their daily vitamin K intake stable.'
WHERE code = 'VITAMIN_K';

-- ---------------------------------------------------------------------
-- 5. Ten chuong cua Bang thanh phan thuc pham Viet Nam 2007 (V23, 14 nhom)
-- ---------------------------------------------------------------------
UPDATE nutrition_foods SET category_en = CASE category
    WHEN 'Ngũ cốc và sản phẩm chế biến' THEN 'Cereals and cereal products'
    WHEN 'Khoai củ và sản phẩm chế biến' THEN 'Starchy roots, tubers and their products'
    WHEN 'Hạt, quả giàu đạm, béo và sản phẩm chế biến' THEN 'Protein- and fat-rich seeds and nuts, and their products'
    WHEN 'Rau, quả, củ dùng làm rau' THEN 'Vegetables (leaves, fruits and roots used as vegetables)'
    WHEN 'Quả chín' THEN 'Ripe fruits'
    WHEN 'Dầu, mỡ, bơ' THEN 'Oils, fats and butter'
    WHEN 'Thịt và sản phẩm chế biến' THEN 'Meat and meat products'
    WHEN 'Thủy sản và sản phẩm chế biến' THEN 'Fish, seafood and their products'
    WHEN 'Trứng và sản phẩm chế biến' THEN 'Eggs and egg products'
    WHEN 'Sữa và sản phẩm chế biến' THEN 'Milk and dairy products'
    WHEN 'Đồ hộp' THEN 'Canned foods'
    WHEN 'Đồ ngọt (đường, bánh, mứt, kẹo)' THEN 'Sweets (sugar, cakes, jams, candies)'
    WHEN 'Gia vị, nước chấm' THEN 'Seasonings and dipping sauces'
    WHEN 'Nước giải khát, bia, rượu' THEN 'Soft drinks, beer and alcoholic beverages'
    END
WHERE source = 'VN_FCT';
