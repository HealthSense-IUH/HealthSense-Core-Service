-- =========================================================================
-- V36: Chuẩn hóa dữ liệu thực phẩm hoàn toàn sang tiếng Việt, xóa các món
--      thừa/rác khảo sát USDA & trùng lặp với bảng Việt Nam, đồng bộ danh
--      mục/khẩu phần tiếng Việt và bổ sung link ảnh chất lượng cao.
-- =========================================================================

-- 1. Thêm cột ảnh cho bảng thực phẩm tham chiếu (an toàn chạy lại)
ALTER TABLE nutrition_foods
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(512);

-- 2. Xóa toàn bộ món thừa/rác của USDA_FNDDS (NFS, NS as to, đồ ăn nhanh Mỹ,
--    thức ăn trẻ sơ sinh, biến thể chất béo nhỏ nhặt và các món trùng bảng Việt Nam).
--    Giữ lại 100% bảng VN_FCT (526 món Việt Nam) + toàn bộ 29 món được
--    nutrition_guidance_foods trỏ tới + các món phổ biến sạch bổ sung cho VN_FCT.
DELETE FROM nutrition_foods f
WHERE f.source = 'USDA_FNDDS'
  AND f.id NOT IN (SELECT nutrition_food_id FROM nutrition_guidance_foods)
  AND f.id NOT IN (
      -- Ngũ cốc (CEREAL)
      2708489, -- Yến mạch sống
      2708414, -- Cơm gạo lứt
      2708408, -- Cơm trắng
      2708422, -- Xôi trắng
      2708356, -- Bún / Phở (đã nấu)
      2707709, -- Bánh mì lúa mì nguyên cám
      2707598, -- Bánh mì trắng
      2709910, -- Ngô ngọt luộc
      -- Khoai củ (TUBER)
      2709699, -- Khoai lang nướng
      2709564, -- Sắn luộc
      -- Đậu & hạt (LEGUMES_NUTS)
      2707590, -- Hạt chia
      2707588, -- Hạt lanh
      2707531, -- Quả óc chó
      2707497, -- Hạt điều không muối
      2707530, -- Hạt dẻ cười không muối
      2707436, -- Đậu nành lông (Edamame) luộc
      2707533, -- Bơ hạnh nhân
      2707537, -- Bơ đậu phộng
      2705409, -- Sữa hạnh nhân không đường
      2705412, -- Sữa yến mạch
      2705405, -- Sữa đậu nành không đường
      2705404, -- Sữa đậu nành có đường
      2705413, -- Sữa dừa
      2707568, -- Nước cốt dừa
      -- Rau củ (VEGETABLE)
      2709643, -- Bông cải xanh tươi
      2709777, -- Súp lơ trắng tươi
      2709599, -- Cải xoăn tươi
      2709793, -- Nấm tươi
      2709929, -- Cà tím luộc
      -- Trái cây (FRUIT)
      2709275, -- Quả việt quất tươi
      2709283, -- Dâu tây tươi
      2709281, -- Quả mâm xôi đỏ tươi
      2709273, -- Quả mâm xôi đen tươi
      2709239, -- Quả kiwi tươi
      2709231, -- Quả anh đào tươi
      2709267, -- Quả lựu tươi
      2709237, -- Nho tươi
      2709242, -- Xoài tươi
      2709246, -- Đu đủ tươi
      2709249, -- Đào tươi
      2709254, -- Lê tươi
      2709260, -- Dứa tươi
      2709265, -- Mận tươi
      2709270, -- Dưa hấu tươi
      -- Thịt (MEAT)
      2705956, -- Ức gà nướng bỏ da
      2705841, -- Bít tết thăn nội bò
      2705868, -- Cốt lết lợn nạc
      -- Cá & hải sản (FISH)
      2706284, -- Cá hồi tươi
      2706245, -- Cá tuyết hấp
      2706241, -- Cá tuyết nướng
      2706293, -- Cá mòi đóng hộp
      2706323, -- Cá rô phi hấp
      2706363, -- Tôm luộc / hấp
      -- Trứng (EGG)
      2707154, -- Trứng gà luộc
      2707168, -- Lòng trắng trứng tươi
      2707170, -- Lòng trắng trứng chín
      -- Sữa & phô mai (DAIRY)
      2705709, -- Phô mai Cheddar
      2705723, -- Phô mai Mozzarella tách béo một phần
      2705756, -- Phô mai tươi Cottage ít béo
      -- Dầu, mỡ, bơ (FAT_OIL)
      2710186, -- Dầu ô liu
      -- Đồ ngọt (SWEET)
      2710336, -- Sô-cô-la đen
      -- Gia vị & sốt (CONDIMENT)
      2707442, -- Nước tương
      2710199, -- Sốt trộn salad Caesar
      2707402, -- Sốt đậu gà Hummus
      -- Đồ uống (BEVERAGE)
      2710708, -- Nước tinh khiết đóng chai
      2710707, -- Nước lọc
      2710452, -- Cà phê đã khử caffeine
      2710689, -- Rượu vang trắng
      2710617, -- Bia nhẹ
      2710610, -- Bia không cồn
      -- Món hỗn hợp (MIXED_DISH)
      2707124, -- Phở có thịt
      2707125, -- Phở chay (không thịt)
      2709831, -- Salad rau chân vịt
      -- Khác (OTHER)
      2710740  -- Bột dinh dưỡng pha uống
  );

-- 3. Làm sạch tên tiếng Việt tự nhiên cho các món USDA được giữ lại (bỏ đuôi dịch máy xấu)
UPDATE nutrition_foods SET name_vi = CASE id
    WHEN 2707485 THEN 'Hạt hạnh nhân'
    WHEN 2709215 THEN 'Táo tươi'
    WHEN 2709223 THEN 'Quả bơ tươi'
    WHEN 2709224 THEN 'Chuối chín tươi'
    WHEN 2706171 THEN 'Xúc xích bò'
    WHEN 2710616 THEN 'Bia truyền thống'
    WHEN 2709644 THEN 'Bông cải xanh luộc'
    WHEN 2709660 THEN 'Cà rốt tươi'
    WHEN 2707414 THEN 'Đậu gà nấu chín'
    WHEN 2710375 THEN 'Cà phê pha nguyên chất'
    WHEN 2706264 THEN 'Cá thu nướng'
    WHEN 2705389 THEN 'Sữa không lactose ít béo 1%'
    WHEN 2705390 THEN 'Sữa không lactose tách béo'
    WHEN 2705392 THEN 'Sữa không lactose nguyên kem'
    WHEN 2705387 THEN 'Sữa ít béo 1%'
    WHEN 2705386 THEN 'Sữa giảm béo 2%'
    WHEN 2705388 THEN 'Sữa tách béo'
    WHEN 2705385 THEN 'Sữa tươi nguyên kem'
    WHEN 2709171 THEN 'Cam tươi'
    WHEN 2707512 THEN 'Đậu phộng (Lạc)'
    WHEN 2706286 THEN 'Cá hồi nướng'
    WHEN 2706289 THEN 'Cá hồi chiên'
    WHEN 2707388 THEN 'Đậu nành nấu chín'
    WHEN 2709615 THEN 'Rau chân vịt luộc'
    WHEN 2709614 THEN 'Rau chân vịt tươi'
    WHEN 2710490 THEN 'Trà xanh pha nóng'
    WHEN 2709719 THEN 'Cà chua tươi'
    WHEN 2706311 THEN 'Cá ngừ đóng hộp'
    WHEN 2710688 THEN 'Rượu vang đỏ'
    WHEN 2708489 THEN 'Yến mạch sống'
    WHEN 2708414 THEN 'Cơm gạo lứt'
    WHEN 2708408 THEN 'Cơm trắng'
    WHEN 2708422 THEN 'Xôi trắng'
    WHEN 2708356 THEN 'Sợi bún / phở (đã nấu)'
    WHEN 2707709 THEN 'Bánh mì lúa mì nguyên cám'
    WHEN 2707598 THEN 'Bánh mì trắng'
    WHEN 2709910 THEN 'Ngô ngọt luộc'
    WHEN 2709699 THEN 'Khoai lang nướng'
    WHEN 2709564 THEN 'Sắn luộc'
    WHEN 2707590 THEN 'Hạt chia'
    WHEN 2707588 THEN 'Hạt lanh'
    WHEN 2707531 THEN 'Quả óc chó'
    WHEN 2707497 THEN 'Hạt điều không muối'
    WHEN 2707530 THEN 'Hạt dẻ cười không muối'
    WHEN 2707436 THEN 'Đậu nành lông luộc'
    WHEN 2707533 THEN 'Bơ hạnh nhân'
    WHEN 2707537 THEN 'Bơ đậu phộng'
    WHEN 2705409 THEN 'Sữa hạnh nhân không đường'
    WHEN 2705412 THEN 'Sữa yến mạch'
    WHEN 2705405 THEN 'Sữa đậu nành không đường'
    WHEN 2705404 THEN 'Sữa đậu nành có đường'
    WHEN 2705413 THEN 'Sữa dừa'
    WHEN 2707568 THEN 'Nước cốt dừa'
    WHEN 2709643 THEN 'Bông cải xanh tươi'
    WHEN 2709777 THEN 'Súp lơ trắng tươi'
    WHEN 2709599 THEN 'Cải xoăn tươi'
    WHEN 2709793 THEN 'Nấm tươi'
    WHEN 2709929 THEN 'Cà tím luộc'
    WHEN 2709275 THEN 'Quả việt quất tươi'
    WHEN 2709283 THEN 'Dâu tây tươi'
    WHEN 2709281 THEN 'Quả mâm xôi đỏ tươi'
    WHEN 2709273 THEN 'Quả mâm xôi đen tươi'
    WHEN 2709239 THEN 'Quả kiwi tươi'
    WHEN 2709231 THEN 'Quả anh đào tươi'
    WHEN 2709267 THEN 'Quả lựu tươi'
    WHEN 2709237 THEN 'Nho tươi'
    WHEN 2709242 THEN 'Xoài tươi'
    WHEN 2709246 THEN 'Đu đủ tươi'
    WHEN 2709249 THEN 'Đào tươi'
    WHEN 2709254 THEN 'Lê tươi'
    WHEN 2709260 THEN 'Dứa tươi'
    WHEN 2709265 THEN 'Mận tươi'
    WHEN 2709270 THEN 'Dưa hấu tươi'
    WHEN 2705956 THEN 'Ức gà nướng bỏ da'
    WHEN 2705841 THEN 'Bít tết thăn nội bò'
    WHEN 2705868 THEN 'Cốt lết lợn nạc'
    WHEN 2706284 THEN 'Cá hồi tươi'
    WHEN 2706245 THEN 'Cá tuyết hấp'
    WHEN 2706241 THEN 'Cá tuyết nướng'
    WHEN 2706293 THEN 'Cá mòi đóng hộp'
    WHEN 2706323 THEN 'Cá rô phi hấp'
    WHEN 2706363 THEN 'Tôm hấp / luộc'
    WHEN 2707154 THEN 'Trứng gà luộc'
    WHEN 2707168 THEN 'Lòng trắng trứng tươi'
    WHEN 2707170 THEN 'Lòng trắng trứng chín'
    WHEN 2705709 THEN 'Phô mai Cheddar'
    WHEN 2705723 THEN 'Phô mai Mozzarella tách béo'
    WHEN 2705756 THEN 'Phô mai tươi Cottage ít béo'
    WHEN 2710186 THEN 'Dầu ô liu'
    WHEN 2710336 THEN 'Sô-cô-la đen'
    WHEN 2707442 THEN 'Nước tương'
    WHEN 2710199 THEN 'Sốt trộn salad Caesar'
    WHEN 2707402 THEN 'Sốt đậu gà Hummus'
    WHEN 2710708 THEN 'Nước tinh khiết đóng chai'
    WHEN 2710707 THEN 'Nước lọc'
    WHEN 2710452 THEN 'Cà phê đã khử caffeine'
    WHEN 2710689 THEN 'Rượu vang trắng'
    WHEN 2710617 THEN 'Bia nhẹ'
    WHEN 2710610 THEN 'Bia không cồn'
    WHEN 2707124 THEN 'Phở có thịt'
    WHEN 2707125 THEN 'Phở chay (không thịt)'
    WHEN 2709831 THEN 'Salad rau chân vịt'
    WHEN 2710740 THEN 'Bột dinh dưỡng pha uống'
    ELSE name_vi
END
WHERE source = 'USDA_FNDDS';

-- Bỏ chữ tiếng Anh thừa trong khuyến nghị sữa tách béo
UPDATE nutrition_guidance_foods
SET food_name_specific = 'Sữa tách béo'
WHERE id = 'milk-skim';

-- 4. Đồng bộ cột category (tiếng Việt) & category_en (tiếng Anh) cho toàn bộ món USDA
--    khớp hoàn toàn quy ước với VN_FCT (category = VI, category_en = EN)
UPDATE nutrition_foods
SET category_en = COALESCE(category_en, category),
    category = CASE category
        WHEN 'Apples' THEN 'Táo'
        WHEN 'Bananas' THEN 'Chuối'
        WHEN 'Beans, peas, legumes' THEN 'Đậu, đỗ và cây họ đậu'
        WHEN 'Beef, excludes ground' THEN 'Thịt bò'
        WHEN 'Beer' THEN 'Bia'
        WHEN 'Blueberries and other berries' THEN 'Quả mọng'
        WHEN 'Bottled water' THEN 'Nước đóng chai'
        WHEN 'Broccoli' THEN 'Bông cải xanh'
        WHEN 'Candy containing chocolate' THEN 'Sô-cô-la và bánh kẹo'
        WHEN 'Carrots' THEN 'Cà rốt'
        WHEN 'Cheese' THEN 'Phô mai'
        WHEN 'Chicken, whole pieces' THEN 'Thịt gà'
        WHEN 'Citrus fruits' THEN 'Trái cây họ cam quýt'
        WHEN 'Coffee' THEN 'Cà phê'
        WHEN 'Corn' THEN 'Ngô (bắp)'
        WHEN 'Cottage/ricotta cheese' THEN 'Phô mai tươi'
        WHEN 'Dips, gravies, other sauces' THEN 'Nước chấm và sốt'
        WHEN 'Eggs and omelets' THEN 'Trứng và món trứng'
        WHEN 'Fish' THEN 'Cá'
        WHEN 'Fruit drinks' THEN 'Đồ uống không cồn'
        WHEN 'Grapes' THEN 'Nho'
        WHEN 'Mango and papaya' THEN 'Xoài và đu đủ'
        WHEN 'Melons' THEN 'Họ dưa'
        WHEN 'Milk, lowfat' THEN 'Sữa ít béo'
        WHEN 'Milk, nonfat' THEN 'Sữa tách béo'
        WHEN 'Milk, reduced fat' THEN 'Sữa giảm béo'
        WHEN 'Milk, whole' THEN 'Sữa nguyên kem'
        WHEN 'Not included in a food category' THEN 'Ngũ cốc nguyên hạt'
        WHEN 'Nuts and seeds' THEN 'Các loại hạt'
        WHEN 'Other dark green vegetables' THEN 'Rau lá xanh đậm'
        WHEN 'Other fruits and fruit salads' THEN 'Trái cây tươi'
        WHEN 'Other red and orange vegetables' THEN 'Khoai củ'
        WHEN 'Other starchy vegetables' THEN 'Khoai củ giàu tinh bột'
        WHEN 'Other vegetables and combinations' THEN 'Rau củ tươi'
        WHEN 'Pasta, noodles, cooked grains' THEN 'Bún, phở, mì'
        WHEN 'Peaches and nectarines' THEN 'Đào'
        WHEN 'Pears' THEN 'Lê'
        WHEN 'Pineapple' THEN 'Dứa'
        WHEN 'Plant-based milk' THEN 'Sữa hạt / thực vật'
        WHEN 'Pork' THEN 'Thịt lợn'
        WHEN 'Protein and nutritional powders' THEN 'Bột dinh dưỡng'
        WHEN 'Ramen and Asian broth-based soups' THEN 'Món nước châu Á'
        WHEN 'Rice' THEN 'Cơm và gạo'
        WHEN 'Salad dressings and vegetable oils' THEN 'Dầu thực vật và sốt salad'
        WHEN 'Sausages' THEN 'Xúc xích và thịt chế biến'
        WHEN 'Shellfish' THEN 'Tôm, cua, hải sản có vỏ'
        WHEN 'Soy-based condiments' THEN 'Gia vị từ đậu nành'
        WHEN 'Spinach' THEN 'Rau chân vịt'
        WHEN 'Strawberries' THEN 'Dâu tây'
        WHEN 'Tap water' THEN 'Nước lọc'
        WHEN 'Tea' THEN 'Trà'
        WHEN 'Tomatoes' THEN 'Cà chua'
        WHEN 'Vegetable dishes' THEN 'Món rau và salad'
        WHEN 'Wine' THEN 'Rượu vang'
        WHEN 'Yeast breads' THEN 'Bánh mì'
        ELSE category
    END
WHERE source = 'USDA_FNDDS';

-- 5. Cột search_vi là generated column (GENERATED ALWAYS AS ... STORED) nên PostgreSQL
--    tự động tính lại ngay khi name_vi được cập nhật ở bước 3.

-- 6. Xóa các khẩu phần kỹ thuật khó hiểu và dịch các khẩu phần còn lại sang tiếng Việt
DELETE FROM nutrition_food_portions
WHERE description IN (
    '1 cubic inch',
    '1 surface inch',
    '1 fl oz (NFS)',
    '1 fl oz (with ice)',
    '1 fl oz (no ice)',
    '1 miniature/slider',
    '1 miniature/bite size',
    '1 100 calorie package'
);

UPDATE nutrition_food_portions
SET description = CASE
    WHEN description = 'Quantity not specified' THEN 'Khẩu phần tiêu chuẩn'
    WHEN description = '1 cup' THEN '1 cốc (chén)'
    WHEN description = '1 cup, NFS' THEN '1 cốc (chén)'
    WHEN description = '1 cup (8 fl oz)' THEN '1 cốc (240 ml)'
    WHEN description = '1 cup, cooked' THEN '1 chén đã nấu'
    WHEN description = '1 cup, cooked, diced' THEN '1 chén cắt hạt lựu'
    WHEN description = '1 cup, raw' THEN '1 chén tươi'
    WHEN description = '1 cup, chopped' THEN '1 chén cắt nhỏ'
    WHEN description = '1 cup, sliced' THEN '1 chén thái lát'
    WHEN description = '1 cup, whole' THEN '1 chén nguyên quả'
    WHEN description = '1 tablespoon' THEN '1 thìa canh'
    WHEN description = '1 teaspoon' THEN '1 thìa cà phê'
    WHEN description = '1 fl oz' THEN '1 fl oz (30 ml)'
    WHEN description = '1 oz' THEN '1 oz (28 g)'
    WHEN description = '1 oz, cooked' THEN '1 oz (28 g) đã nấu'
    WHEN description = '1 oz yields' THEN '1 oz (28 g)'
    WHEN description = '1 large' THEN '1 phần lớn'
    WHEN description = '1 medium' THEN '1 phần vừa'
    WHEN description = '1 small' THEN '1 phần nhỏ'
    WHEN description = '1 regular' THEN '1 phần tiêu chuẩn'
    WHEN description = '1 extra large' THEN '1 phần rất lớn'
    WHEN description = '1 piece' THEN '1 miếng'
    WHEN description = '1 piece, NFS' THEN '1 miếng'
    WHEN description = '1 slice' THEN '1 lát'
    WHEN description = '1 large or thick slice' THEN '1 lát dày'
    WHEN description = '1 medium or regular slice' THEN '1 lát vừa'
    WHEN description = '1 small or thin/very thin slice' THEN '1 lát mỏng'
    WHEN description = '1 slice, crust not eaten' THEN '1 lát (bỏ vỏ)'
    WHEN description = '1 large fillet' THEN '1 miếng phi-lê lớn'
    WHEN description = '1 small/regular fillet' THEN '1 miếng phi-lê vừa'
    WHEN description = '1 fillet' THEN '1 miếng phi-lê'
    WHEN description = '1 egg' THEN '1 quả trứng'
    WHEN description = '1 can' THEN '1 lon'
    WHEN description = '1 can (12 fl oz)' THEN '1 lon (355 ml)'
    WHEN description = '1 bottle' THEN '1 chai'
    WHEN description = '1 bottle (12 fl oz)' THEN '1 chai (355 ml)'
    WHEN description = '1 bottle (16.9 fl oz)' THEN '1 chai (500 ml)'
    WHEN description = '1 glass' THEN '1 ly'
    WHEN description = '1 wine glass (5 fl oz)' THEN '1 ly rượu vang (150 ml)'
    WHEN description = '1 mug (8 fl oz)' THEN '1 tách (240 ml)'
    WHEN description = '1 bowl' THEN '1 bát'
    WHEN description = '1 packet' THEN '1 gói'
    WHEN description = '1 scoop' THEN '1 muỗng đong'
    WHEN description = '1 link' THEN '1 cây'
    WHEN description = '1 patty' THEN '1 miếng'
    WHEN description = '1 breast' THEN '1 miếng ức'
    WHEN description = '1 steak' THEN '1 miếng bít tết'
    WHEN description = '1 chop' THEN '1 miếng cốt lết'
    WHEN description = '1 fruit' THEN '1 quả'
    WHEN description = '1 spear' THEN '1 nhánh'
    WHEN description = '1 stalk' THEN '1 cọng'
    WHEN description = '1 leaf' THEN '1 lá'
    WHEN description = '1 individual container' THEN '1 hộp nhỏ'
    WHEN description = '1 serving' THEN '1 khẩu phần'
    ELSE regexp_replace(
             regexp_replace(
                 regexp_replace(
                     regexp_replace(
                         regexp_replace(
                             regexp_replace(description, '\ycups?\y', 'cốc', 'gi'),
                             '\ytablespoons?\y', 'thìa canh', 'gi'
                         ),
                         '\yteaspoons?\y', 'thìa cà phê', 'gi'
                     ),
                     '\yslices?\y', 'lát', 'gi'
                 ),
                 '\ypieces?\y', 'miếng', 'gi'
             ),
             '\yfillets?\y', 'miếng phi-lê', 'gi'
         )
END;

-- 7. Bổ sung link ảnh Unsplash đã kiểm chứng (HTTP 200) cho toàn bộ 15 nhóm thực phẩm
UPDATE nutrition_food_groups
SET image_url = CASE id
    WHEN 'CEREAL'       THEN 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop&q=80'
    WHEN 'TUBER'        THEN 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=600&auto=format&fit=crop&q=80'
    WHEN 'LEGUMES_NUTS' THEN 'https://images.unsplash.com/photo-1508061253366-f7da158b6d46?w=600&auto=format&fit=crop&q=80'
    WHEN 'VEGETABLE'    THEN 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600&auto=format&fit=crop&q=80'
    WHEN 'FRUIT'        THEN 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=600&auto=format&fit=crop&q=80'
    WHEN 'MEAT'         THEN 'https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=600&auto=format&fit=crop&q=80'
    WHEN 'FISH'         THEN 'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=600&auto=format&fit=crop&q=80'
    WHEN 'EGG'          THEN 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=600&auto=format&fit=crop&q=80'
    WHEN 'DAIRY'        THEN 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop&q=80'
    WHEN 'FAT_OIL'      THEN 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop&q=80'
    WHEN 'SWEET'        THEN 'https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=600&auto=format&fit=crop&q=80'
    WHEN 'CONDIMENT'    THEN 'https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=600&auto=format&fit=crop&q=80'
    WHEN 'BEVERAGE'     THEN 'https://images.unsplash.com/photo-1544145945-f90425340c7e?w=600&auto=format&fit=crop&q=80'
    WHEN 'MIXED_DISH'   THEN 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=600&auto=format&fit=crop&q=80'
    WHEN 'OTHER'        THEN 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=600&auto=format&fit=crop&q=80'
    ELSE image_url
END;

-- 8. Bổ sung link ảnh Unsplash đã kiểm chứng (HTTP 200) cho toàn bộ 29 món có khuyến nghị tim mạch
UPDATE nutrition_guidance_foods
SET image_url = CASE id
    WHEN 'almonds'                   THEN 'https://images.unsplash.com/photo-1508061253366-f7da158b6d46?w=600&auto=format&fit=crop&q=80'
    WHEN 'apple-raw'                 THEN 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600&auto=format&fit=crop&q=80'
    WHEN 'avocado-raw'               THEN 'https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=600&auto=format&fit=crop&q=80'
    WHEN 'banana-raw'                THEN 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600&auto=format&fit=crop&q=80'
    WHEN 'beef-sausage'              THEN 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=600&auto=format&fit=crop&q=80'
    WHEN 'beer-regular'              THEN 'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=600&auto=format&fit=crop&q=80'
    WHEN 'broccoli-cooked'           THEN 'https://images.unsplash.com/photo-1459411621453-7b03977f4bfc?w=600&auto=format&fit=crop&q=80'
    WHEN 'carrot-raw'                THEN 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=600&auto=format&fit=crop&q=80'
    WHEN 'chickpeas'                 THEN 'https://images.unsplash.com/photo-1515543904379-3d757afe72e4?w=600&auto=format&fit=crop&q=80'
    WHEN 'coffee-brewed'             THEN 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600&auto=format&fit=crop&q=80'
    WHEN 'mackerel-grilled'          THEN 'https://images.unsplash.com/photo-1534604973900-c43ab4c2e0ab?w=600&auto=format&fit=crop&q=80'
    WHEN 'milk-lactose-free-low-fat' THEN 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=600&auto=format&fit=crop&q=80'
    WHEN 'milk-lactose-free-skim'    THEN 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop&q=80'
    WHEN 'milk-lactose-free-whole'   THEN 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=600&auto=format&fit=crop&q=80'
    WHEN 'milk-low-fat-1'            THEN 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=600&auto=format&fit=crop&q=80'
    WHEN 'milk-reduced-fat-2'        THEN 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop&q=80'
    WHEN 'milk-skim'                 THEN 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop&q=80'
    WHEN 'milk-whole'                THEN 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=600&auto=format&fit=crop&q=80'
    WHEN 'orange-raw'                THEN 'https://images.unsplash.com/photo-1547514701-42782101795e?w=600&auto=format&fit=crop&q=80'
    WHEN 'peanuts'                   THEN 'https://images.unsplash.com/photo-1608797178974-15b35a64ede9?w=600&auto=format&fit=crop&q=80'
    WHEN 'salmon-baked'              THEN 'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=600&auto=format&fit=crop&q=80'
    WHEN 'salmon-fried'              THEN 'https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=600&auto=format&fit=crop&q=80'
    WHEN 'soybeans'                  THEN 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=600&auto=format&fit=crop&q=80'
    WHEN 'spinach-cooked'            THEN 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600&auto=format&fit=crop&q=80'
    WHEN 'spinach-raw'               THEN 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600&auto=format&fit=crop&q=80'
    WHEN 'tea-brewed'                THEN 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=600&auto=format&fit=crop&q=80'
    WHEN 'tomato-raw'                THEN 'https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=600&auto=format&fit=crop&q=80'
    WHEN 'tuna-canned'               THEN 'https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=600&auto=format&fit=crop&q=80'
    WHEN 'wine-red'                  THEN 'https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=600&auto=format&fit=crop&q=80'
    ELSE image_url
END;

-- 9. Kế thừa link ảnh riêng cho 29 món trong nutrition_foods (các món còn lại tự dùng ảnh nhóm khi trả API)
UPDATE nutrition_foods f
SET image_url = gf.image_url
FROM nutrition_guidance_foods gf
WHERE gf.nutrition_food_id = f.id
  AND gf.image_url IS NOT NULL;
