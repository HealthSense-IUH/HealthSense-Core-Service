-- =========================================================================
-- V37: Gán ảnh riêng, đúng món, đã kiểm chứng (HTTP 200) và không trùng lặp
--      cho toàn bộ 629 thực phẩm trong nutrition_foods và đồng bộ sang
--      29 món khuyến nghị tim mạch trong nutrition_guidance_foods.
-- =========================================================================

-- Nhóm BEVERAGE (27 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2710375 THEN 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600&auto=format&fit=crop&q=80' -- Cà phê pha nguyên chất
    WHEN 2710452 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/45/A_small_cup_of_coffee.JPG/500px-A_small_cup_of_coffee.JPG' -- Cà phê đã khử caffeine
    WHEN 2710490 THEN 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=600&auto=format&fit=crop&q=80' -- Trà xanh pha nóng
    WHEN 2710610 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bc/Clausthaler%2C_alkoholfritt_%C3%B8l.jpg/500px-Clausthaler%2C_alkoholfritt_%C3%B8l.jpg' -- Bia không cồn
    WHEN 2710616 THEN 'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=600&auto=format&fit=crop&q=80' -- Bia truyền thống
    WHEN 2710617 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/99/Pilsner_Glass_%28119273517%29.jpeg/500px-Pilsner_Glass_%28119273517%29.jpeg' -- Bia nhẹ
    WHEN 2710688 THEN 'https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=600&auto=format&fit=crop&q=80' -- Rượu vang đỏ
    WHEN 2710689 THEN 'https://upload.wikimedia.org/wikipedia/commons/7/71/White_Wine_Glas.jpg' -- Rượu vang trắng
    WHEN 2710707 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cd/Hands_pour_water_from_a_bottle_into_a_glass.jpg/500px-Hands_pour_water_from_a_bottle_into_a_glass.jpg' -- Nước lọc
    WHEN 2710708 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/59/Pure_Water_Bottling_Company_DSC02845.jpg/500px-Pure_Water_Bottling_Company_DSC02845.jpg' -- Nước tinh khiết đóng chai
    WHEN 900011013 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b2/Pineapple_Juice_in_Glass_Jar.jpg/500px-Pineapple_Juice_in_Glass_Jar.jpg' -- Nước dứa hộp
    WHEN 900014001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d7/Lager_beer_in_glass.jpg/500px-Lager_beer_in_glass.jpg' -- Bia (cồn: 4,5 g)
    WHEN 900014002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/Cognac_glass.jpg/500px-Cognac_glass.jpg' -- Cô nhắc (cồn 32 g)
    WHEN 900014003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/de/Manhattan_Cocktail2.jpg/500px-Manhattan_Cocktail2.jpg' -- Cốc tai (cồn 13 g)
    WHEN 900014004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e8/15-09-26-RalfR-WLC-0098_-_Coca-Cola_glass_bottle_%28Germany%29.jpg/500px-15-09-26-RalfR-WLC-0098_-_Coca-Cola_glass_bottle_%28Germany%29.jpg' -- Coca cola
    WHEN 900014005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/67/Orange_juice_1_edit1.jpg/500px-Orange_juice_1_edit1.jpg' -- Nước cam tươi
    WHEN 900014006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/78/Young_Coconut_Drink.jpg/500px-Young_Coconut_Drink.jpg' -- Nước dừa non tươi
    WHEN 900014007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e2/Tomato_Juice.jpg/500px-Tomato_Juice.jpg' -- Nước ép cà chua
    WHEN 900014008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cf/Sparkling_Water_with_Mint_in_Glass_Cup.jpg/500px-Sparkling_Water_with_Mint_in_Glass_Cup.jpg' -- Nước khoáng
    WHEN 900014009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/ff/Tangerine_juicers.jpg/500px-Tangerine_juicers.jpg' -- Nước quít tươi
    WHEN 900014010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/03/Grand_Marnier_Bottle.jpg/500px-Grand_Marnier_Bottle.jpg' -- Rượu cam, chanh (cồn 24,2 g)
    WHEN 900014011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e3/Comruou.jpg/500px-Comruou.jpg' -- Rượu nếp (80g/ 24 ml) (cồn 5 g)
    WHEN 900014012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/30/Tito%27s_Vodka_bottle_1.75L_size.jpg/500px-Tito%27s_Vodka_bottle_1.75L_size.jpg' -- Rượu trắng (cồn 39 g)
    WHEN 900014013 THEN 'https://upload.wikimedia.org/wikipedia/en/c/c0/Red_Wine_Glass.jpg' -- Rượu vang đỏ (cồn 9,5 g)
    WHEN 900014014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8d/Glass_of_White_Wine_shot_with_a_bottle_of_white_wine_-_Evan_Swigart.jpg/500px-Glass_of_White_Wine_shot_with_a_bottle_of_white_wine_-_Evan_Swigart.jpg' -- Rượu vang trắng (cồn 9,5 g)
    WHEN 900014015 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/Sauternes_Ch%C3%A2teau_Lamothe_Guignard.jpg/500px-Sauternes_Ch%C3%A2teau_Lamothe_Guignard.jpg' -- Rượu vang trắng ngọt (cồn 10.2 g)
    WHEN 900014016 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/47/Drambuie_bottle.jpg/500px-Drambuie_bottle.jpg' -- Rượu Whisky (cồn 35,2 g)
    ELSE image_url
END
WHERE id IN (2710375, 2710452, 2710490, 2710610, 2710616, 2710617, 2710688, 2710689, 2710707, 2710708, 900011013, 900014001, 900014002, 900014003, 900014004, 900014005, 900014006, 900014007, 900014008, 900014009, 900014010, 900014011, 900014012, 900014013, 900014014, 900014015, 900014016);

-- Nhóm CEREAL (31 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2707598 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2c/Wei%C3%9Fbrot-1.jpg/500px-Wei%C3%9Fbrot-1.jpg' -- Bánh mì trắng
    WHEN 2707709 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/46/Vegan_no-knead_whole_wheat_bread_loaf%2C_September_2010.jpg/500px-Vegan_no-knead_whole_wheat_bread_loaf%2C_September_2010.jpg' -- Bánh mì lúa mì nguyên cám
    WHEN 2708356 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a4/Thin_rice_noodles_cooked_in_small_pot_in_Kunming_%2820210322091628%29.jpg/500px-Thin_rice_noodles_cooked_in_small_pot_in_Kunming_%2820210322091628%29.jpg' -- Sợi bún / phở (đã nấu)
    WHEN 2708408 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/White_steamed_red_bean_rice_pudding_3.jpg/500px-White_steamed_red_bean_rice_pudding_3.jpg' -- Cơm trắng
    WHEN 2708414 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9c/Par_cooked_brown_rice_-_stonesoup_cropped.jpg/500px-Par_cooked_brown_rice_-_stonesoup_cropped.jpg' -- Cơm gạo lứt
    WHEN 2708422 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ee/Sticky_Rice_%2885950%29.jpg/500px-Sticky_Rice_%2885950%29.jpg' -- Xôi trắng
    WHEN 2708489 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/df/Rolled_Oats.jpg/500px-Rolled_Oats.jpg' -- Yến mạch sống
    WHEN 2709910 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f5/CookedCornOnTheCob.JPG/500px-CookedCornOnTheCob.JPG' -- Ngô ngọt luộc
    WHEN 900001001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/aa/Mum%27s_Oily_Rice_-_Glutinous_Rice_Rice_with_mushroom%2C_Chinese_Sausage_%282770474966%29.jpg/500px-Mum%27s_Oily_Rice_-_Glutinous_Rice_Rice_with_mushroom%2C_Chinese_Sausage_%282770474966%29.jpg' -- Gạo nếp cái
    WHEN 900001002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/aa/Chapssal_%28glutinous_rice%29.jpg/500px-Chapssal_%28glutinous_rice%29.jpg' -- Gạo nếp máy (Loại thường)
    WHEN 900001003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f8/Basmati_Rice_India%2C_raw.jpg/500px-Basmati_Rice_India%2C_raw.jpg' -- Gạo tẻ giã
    WHEN 900001004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/ce/Plain_white_rice-min-min.jpg/500px-Plain_white_rice-min-min.jpg' -- Gạo tẻ máy
    WHEN 900001005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5c/Brown_Rice.jpg/500px-Brown_Rice.jpg' -- Gạo lứt
    WHEN 900001006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/30/Setaria_italica_MHNT.BOT.2015.34.19.jpg/500px-Setaria_italica_MHNT.BOT.2015.34.19.jpg' -- Kê
    WHEN 900001007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4d/B%E1%BA%AFp_m%E1%BB%B9_%28ng%C3%B4_ng%E1%BB%8Dt%29_h%E1%BA%A5p_s%E1%BB%AFa_nh%C3%A0_m%C3%ACnh_ng%C3%A0y_21_th%C3%A1ng_8_n%C4%83m_2019_%281%29.jpg/500px-B%E1%BA%AFp_m%E1%BB%B9_%28ng%C3%B4_ng%E1%BB%8Dt%29_h%E1%BA%A5p_s%E1%BB%AFa_nh%C3%A0_m%C3%ACnh_ng%C3%A0y_21_th%C3%A1ng_8_n%C4%83m_2019_%281%29.jpg' -- Ngô bắp tươi
    WHEN 900001008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/05/Corn_kernels.jpg/500px-Corn_kernels.jpg' -- Ngô vàng hạt khô
    WHEN 900001009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f4/Baozi_Chengdu.JPG/500px-Baozi_Chengdu.JPG' -- Bánh bao nhân thịt
    WHEN 900001010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/36/B%C3%A1nh_tr%C3%A1ng_1.jpg/500px-B%C3%A1nh_tr%C3%A1ng_1.jpg' -- Bánh đa nem
    WHEN 900001011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/00/Banhduc-northern.jpg/500px-Banhduc-northern.jpg' -- Bánh đúc
    WHEN 900001012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/20/Baguette-246424.jpg/500px-Baguette-246424.jpg' -- Bánh mỳ
    WHEN 900001013 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/30/Thick_rice_noodles_cooked_in_small_pot_in_Kunming_%2820210323074612%29.jpg/500px-Thick_rice_noodles_cooked_in_small_pot_in_Kunming_%2820210323074612%29.jpg' -- Bánh phở
    WHEN 900001014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/78/Youtiao.jpg/500px-Youtiao.jpg' -- Bánh quẩy
    WHEN 900001015 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d6/Popcorn_-_Studio_-_2011.jpg/500px-Popcorn_-_Studio_-_2011.jpg' -- Bỏng ngô
    WHEN 900001016 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/53/Glutinous_rice_flour.jpg/500px-Glutinous_rice_flour.jpg' -- Bột gạo nếp
    WHEN 900001017 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/19/Two_kinds_of_rice_flour.jpg/500px-Two_kinds_of_rice_flour.jpg' -- Bột gạo tẻ
    WHEN 900001018 THEN 'https://upload.wikimedia.org/wikipedia/commons/1/11/Wheat-flour.jpg' -- Bột mì
    WHEN 900001019 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f8/Preparation_of_cornmeal.jpg/500px-Preparation_of_cornmeal.jpg' -- Bột ngô vàng
    WHEN 900001020 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/95/Whole-grain_rice_vermicelli_noodles_%28Thai%29.jpg/500px-Whole-grain_rice_vermicelli_noodles_%28Thai%29.jpg' -- Bún
    WHEN 900001021 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/C%E1%BB%91m_M%E1%BB%85_Tr%C3%AC_-_NKS.jpg/500px-C%E1%BB%91m_M%E1%BB%85_Tr%C3%AC_-_NKS.jpg' -- Cốm
    WHEN 900001022 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0b/Noodles_2.png/500px-Noodles_2.png' -- Mỳ sợi
    WHEN 900001023 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/ca/Choclo_hervido_%28boiled_corn%29.jpg/500px-Choclo_hervido_%28boiled_corn%29.jpg' -- Ngô nếp luộc
    ELSE image_url
END
WHERE id IN (2707598, 2707709, 2708356, 2708408, 2708414, 2708422, 2708489, 2709910, 900001001, 900001002, 900001003, 900001004, 900001005, 900001006, 900001007, 900001008, 900001009, 900001010, 900001011, 900001012, 900001013, 900001014, 900001015, 900001016, 900001017, 900001018, 900001019, 900001020, 900001021, 900001022, 900001023);

-- Nhóm CONDIMENT (26 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2707402 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e2/Rouhi_AlAqili_hummus_restaurant2.png/500px-Rouhi_AlAqili_hummus_restaurant2.png' -- Sốt đậu gà Hummus
    WHEN 2707442 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2a/Soy_Sauce_Bulk.jpg/500px-Soy_Sauce_Bulk.jpg' -- Nước tương
    WHEN 2710199 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f7/Caesar_Salad_-_Purezza_2023-11-22.jpg/500px-Caesar_Salad_-_Purezza_2023-11-22.jpg' -- Sốt trộn salad Caesar
    WHEN 900013001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/70/Curry_powder.jpg/500px-Curry_powder.jpg' -- Cary bột
    WHEN 900013002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/16/Ginger_powder_%281%29.jpg/500px-Ginger_powder_%281%29.jpg' -- Gừng khô (bột)
    WHEN 900013003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/Liat_Portal_for_Foodie_Disorder_-_Fresh_Ginger_Root.jpg/500px-Liat_Portal_for_Foodie_Disorder_-_Fresh_Ginger_Root.jpg' -- Gừng tươi
    WHEN 900013004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3f/Peppercorns_seeds.jpg/500px-Peppercorns_seeds.jpg' -- Hạt tiêu
    WHEN 900013005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ad/Table_salt_with_salt_shaker_V1.jpg/500px-Table_salt_with_salt_shaker_V1.jpg' -- Muối
    WHEN 900013006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/Drying_turmeric%2C_sliced_turmeric%2C_medicinal_plants.jpg/500px-Drying_turmeric%2C_sliced_turmeric%2C_medicinal_plants.jpg' -- Nghệ khô, bột
    WHEN 900013007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5b/Curcuma_longa_roots.jpg/500px-Curcuma_longa_roots.jpg' -- Nghệ tươi
    WHEN 900013008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bd/Dried_Capsicum_annuum-Red_Chilli_Pepper_on_Nanglo.jpg/500px-Dried_Capsicum_annuum-Red_Chilli_Pepper_on_Nanglo.jpg' -- ớt khô bột
    WHEN 900013009 THEN 'https://upload.wikimedia.org/wikipedia/commons/6/69/Galangal_ready_for_preparation.png' -- Riềng
    WHEN 900013010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7b/Soja_Sauce_zum_Sushi_%2825966205154%29.jpg/500px-Soja_Sauce_zum_Sushi_%2825966205154%29.jpg' -- Magi
    WHEN 900013011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8c/Terasi-dari-lombok.jpg/500px-Terasi-dari-lombok.jpg' -- Mắm tôm đặc
    WHEN 900013012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c0/Mam_tom_chua_di_Can.jpg/500px-Mam_tom_chua_di_Can.jpg' -- Mắm tôm loãng
    WHEN 900013013 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c5/Shrimp_paste_manufacturing_in_Tai_O.jpg/500px-Shrimp_paste_manufacturing_in_Tai_O.jpg' -- Mắm tép chua
    WHEN 900013014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/70/Thaifishsauce0609.jpg/500px-Thaifishsauce0609.jpg' -- Nước mắm cá (loại đặc biệt)
    WHEN 900013015 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c3/N%C6%B0%E1%BB%9Bc_m%E1%BA%AFm_Gio_H%E1%BA%A3i_c%E1%BB%A7a_ch%C3%BA_Trong%2C_t%E1%BA%BFt_2018.jpg/500px-N%C6%B0%E1%BB%9Bc_m%E1%BA%AFm_Gio_H%E1%BA%A3i_c%E1%BB%A7a_ch%C3%BA_Trong%2C_t%E1%BA%BFt_2018.jpg' -- Nước mắm cá loại I
    WHEN 900013016 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bc/Sauce_of_sour_fish.png/500px-Sauce_of_sour_fish.png' -- Nước mắm loại II
    WHEN 900013017 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a9/Cooked_yam_and_fish_sauce.jpg/500px-Cooked_yam_and_fish_sauce.jpg' -- Nước mắm cá
    WHEN 900013018 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/63/Boiled_yam_with_vegetable_sauce_dry_fish_and_Boiled_egg_and_a_cup_of_coco_tea.jpg/500px-Boiled_yam_with_vegetable_sauce_dry_fish_and_Boiled_egg_and_a_cup_of_coco_tea.jpg' -- Nước mắm cô
    WHEN 900013019 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/87/Doenjang.jpg/500px-Doenjang.jpg' -- Tương ngô
    WHEN 900013020 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4b/Haitian_fermented_bean_paste_on_a_shelf.jpg/500px-Haitian_fermented_bean_paste_on_a_shelf.jpg' -- Tương nếp
    WHEN 900013021 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e2/Ch%C3%A9n_t%C6%B0%C6%A1ng_%E1%BB%9Bt_ng%C3%A0y_20_th%C3%A1ng_3_n%C4%83m_2021_%281%29.jpg/500px-Ch%C3%A9n_t%C6%B0%C6%A1ng_%E1%BB%9Bt_ng%C3%A0y_20_th%C3%A1ng_3_n%C4%83m_2021_%281%29.jpg' -- Tương ớt
    WHEN 900013022 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/63/Soy_sauce_2.jpg/500px-Soy_sauce_2.jpg' -- Xì dầu
    WHEN 900013023 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/60/Mayonnaise_%281%29.jpg/500px-Mayonnaise_%281%29.jpg' -- Sốt mayonnaise
    ELSE image_url
END
WHERE id IN (2707402, 2707442, 2710199, 900013001, 900013002, 900013003, 900013004, 900013005, 900013006, 900013007, 900013008, 900013009, 900013010, 900013011, 900013012, 900013013, 900013014, 900013015, 900013016, 900013017, 900013018, 900013019, 900013020, 900013021, 900013022, 900013023);

-- Nhóm DAIRY (19 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2705385 THEN 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=600&auto=format&fit=crop&q=80' -- Sữa tươi nguyên kem
    WHEN 2705386 THEN 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop&q=80' -- Sữa giảm béo 2%
    WHEN 2705387 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b5/72Magnolia_Low_Fat_Milk.jpg/500px-72Magnolia_Low_Fat_Milk.jpg' -- Sữa ít béo 1%
    WHEN 2705388 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bf/Skim_milk_poured_into_cereal_bowl.jpg/500px-Skim_milk_poured_into_cereal_bowl.jpg' -- Sữa tách béo
    WHEN 2705389 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b1/A_glass_of_milk.jpg/500px-A_glass_of_milk.jpg' -- Sữa không lactose ít béo 1%
    WHEN 2705390 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a3/Glass_of_milk_on_table.jpg/500px-Glass_of_milk_on_table.jpg' -- Sữa không lactose tách béo
    WHEN 2705392 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a5/Glass_of_Milk_%2833657535532%29.jpg/500px-Glass_of_Milk_%2833657535532%29.jpg' -- Sữa không lactose nguyên kem
    WHEN 2705709 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/18/Somerset-Cheddar.jpg/500px-Somerset-Cheddar.jpg' -- Phô mai Cheddar
    WHEN 2705723 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/87/2020-11-04_21_54_30_A_package_of_Tropical_Low_Moisture_Part_Skim_Fancy_Shredded_Mozzarella_Natural_Cheese_at_the_Ramada_by_Wyndham_in_Rochelle_Park_Township%2C_Bergen_County%2C_New_Jersey.jpg/500px-thumbnail.jpg' -- Phô mai Mozzarella tách béo
    WHEN 2705756 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/16/Cottagecheese200px.jpg/500px-Cottagecheese200px.jpg' -- Phô mai tươi Cottage ít béo
    WHEN 900010001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7f/Fresh_Cow_milk_and_cookies_for_lunch.jpg/500px-Fresh_Cow_milk_and_cookies_for_lunch.jpg' -- Sữa bò tươi
    WHEN 900010002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/91/Goat_milk_in_Perekrestok_shop_Perm_2023.jpg/500px-Goat_milk_in_Perekrestok_shop_Perm_2023.jpg' -- Sữa dê tươi
    WHEN 900010003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/da/Human_Breastmilk_-_Foremilk_and_Hindmilk.png/500px-Human_Breastmilk_-_Foremilk_and_Hindmilk.png' -- Sữa mẹ (sữa người)
    WHEN 900010004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b8/Joghurt.jpg/500px-Joghurt.jpg' -- Sữa chua (từ sữa bò)
    WHEN 900010005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1b/Plain_Yogurt.jpg/500px-Plain_Yogurt.jpg' -- Sữa chua vớt béo
    WHEN 900010006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b9/Milk_powder_cropped.jpg/500px-Milk_powder_cropped.jpg' -- Sữa bột toàn phần
    WHEN 900010007 THEN 'https://upload.wikimedia.org/wikipedia/commons/8/8b/PowderedMilk.jpg' -- Sữa bột tách béo
    WHEN 900010008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/55/HK_Kai_Bo_food_drink_%E5%A3%BD%E6%98%9F%E5%85%AC_Longevity_brand_%E7%94%9C%E7%82%BC%E5%A5%B6_Sweetened_Milk_November_2022_Px3_04.jpg/500px-HK_Kai_Bo_food_drink_%E5%A3%BD%E6%98%9F%E5%85%AC_Longevity_brand_%E7%94%9C%E7%82%BC%E5%A5%B6_Sweetened_Milk_November_2022_Px3_04.jpg' -- Sữa đặc có đường Việt Nam
    WHEN 900010009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a8/Cheese_platter.jpg/500px-Cheese_platter.jpg' -- Pho mát
    ELSE image_url
END
WHERE id IN (2705385, 2705386, 2705387, 2705388, 2705389, 2705390, 2705392, 2705709, 2705723, 2705756, 900010001, 900010002, 900010003, 900010004, 900010005, 900010006, 900010007, 900010008, 900010009);

-- Nhóm EGG (14 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2707154 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/52/Hard_boiled_egg_2.jpg/500px-Hard_boiled_egg_2.jpg' -- Trứng gà luộc
    WHEN 2707168 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e3/Cracked_Egg_%28NIH_BioArt_621_-_660256%29.png/500px-Cracked_Egg_%28NIH_BioArt_621_-_660256%29.png' -- Lòng trắng trứng tươi
    WHEN 2707170 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/Boiled_egg_holder.jpg/500px-Boiled_egg_holder.jpg' -- Lòng trắng trứng chín
    WHEN 900009001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Chicken_Egg_without_Eggshell_5859.jpg/500px-Chicken_Egg_without_Eggshell_5859.jpg' -- Trứng gà
    WHEN 900009002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bc/Double_and_single_yolk_hen_eggs.png/500px-Double_and_single_yolk_hen_eggs.png' -- Lòng đỏ trứng gà
    WHEN 900009003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/44/Chicken_egg01_monovular.jpg/500px-Chicken_egg01_monovular.jpg' -- Lòng trắng trứng gà
    WHEN 900009004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7e/Turkey.duck.goose-EGGS.jpg/500px-Turkey.duck.goose-EGGS.jpg' -- Trứng vịt
    WHEN 900009005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/01/Salted_duck_egg_yolk.jpg/500px-Salted_duck_egg_yolk.jpg' -- Lòng đỏ trứng vịt
    WHEN 900009006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6d/Salted_duck_eggs_in_store.jpg/500px-Salted_duck_eggs_in_store.jpg' -- Lòng trắng trứng vịt
    WHEN 900009007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/23/Quail_Egg_Roast_Alappuzha.jpg/500px-Quail_Egg_Roast_Alappuzha.jpg' -- Trứng chim cút
    WHEN 900009008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c4/Fish_roe_bags.jpg/500px-Fish_roe_bags.jpg' -- Trứng cá
    WHEN 900009009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/11/Seven_types_of_caviar.jpg/500px-Seven_types_of_caviar.jpg' -- Trứng cá muối
    WHEN 900009010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/eb/Hotvitlon.jpg/500px-Hotvitlon.jpg' -- Trứng vịt lộn
    WHEN 900009011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4d/Pure_dried_whole_eggs_from_the_United_States_of_America.jpg/500px-Pure_dried_whole_eggs_from_the_United_States_of_America.jpg' -- Bột trứng
    ELSE image_url
END
WHERE id IN (2707154, 2707168, 2707170, 900009001, 900009002, 900009003, 900009004, 900009005, 900009006, 900009007, 900009008, 900009009, 900009010, 900009011);

-- Nhóm FAT_OIL (15 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2710186 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/79/2023_Oliwa_z_oliwek_Garda_DOP_%281%29.jpg/500px-2023_Oliwa_z_oliwek_Garda_DOP_%281%29.jpg' -- Dầu ô liu
    WHEN 900006001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7d/Lurpak_Butter_250g_unsalted_UK_market.jpg/500px-Lurpak_Butter_250g_unsalted_UK_market.jpg' -- Bơ
    WHEN 900006002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/49/Olive_oil_from_Oneglia.jpg/500px-Olive_oil_from_Oneglia.jpg' -- Dầu thảo mộc (Lạc, vừng, cám...)
    WHEN 900006003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f3/Sa%C5%82o.JPG/500px-Sa%C5%82o.JPG' -- Mỡ lợn muối
    WHEN 900006004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/af/Homelard.jpg/500px-Homelard.jpg' -- Mỡ lợn nước
    WHEN 900006005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5c/Margarine.jpg/500px-Margarine.jpg' -- Bơ thực vật
    WHEN 900006006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6e/CSIRO_ScienceImage_3443_Cottonseed_Oil.jpg/500px-CSIRO_ScienceImage_3443_Cottonseed_Oil.jpg' -- Dầu bông
    WHEN 900006007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bc/Rice_Bran_Oil.jpeg/500px-Rice_Bran_Oil.jpeg' -- Dầu cám gạo
    WHEN 900006008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/ba/Palm_oil_in_a_white_bowl.jpg/500px-Palm_oil_in_a_white_bowl.jpg' -- Dầu cọ
    WHEN 900006009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6d/Coconut_oil_2.jpg/500px-Coconut_oil_2.jpg' -- Dầu dừa
    WHEN 900006010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/ff/Soybean_Oil_%2810059657806%29.jpg/500px-Soybean_Oil_%2810059657806%29.jpg' -- Dầu đậu tương
    WHEN 900006011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/16/Peanut_oil_bottle.jpg/500px-Peanut_oil_bottle.jpg' -- Dầu lạc
    WHEN 900006012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/22/Sesame_oil_label.jpg/500px-Sesame_oil_label.jpg' -- Dầu mè
    WHEN 900006013 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1f/Corn_oil_%28mais%29.jpg/500px-Corn_oil_%28mais%29.jpg' -- Dầu ngô
    WHEN 900006014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/41/Olive_oil_bottle_Bertolli_Riserva_Premium.jpg/500px-Olive_oil_bottle_Bertolli_Riserva_Premium.jpg' -- Dầu oliu
    ELSE image_url
END
WHERE id IN (2710186, 900006001, 900006002, 900006003, 900006004, 900006005, 900006006, 900006007, 900006008, 900006009, 900006010, 900006011, 900006012, 900006013, 900006014);

-- Nhóm FISH (71 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2706241 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f3/Baked_cod_fillet.jpg/500px-Baked_cod_fillet.jpg' -- Cá tuyết nướng
    WHEN 2706245 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f7/Frozen_fresh_cod_fillet.jpg/500px-Frozen_fresh_cod_fillet.jpg' -- Cá tuyết hấp
    WHEN 2706264 THEN 'https://images.unsplash.com/photo-1534604973900-c43ab4c2e0ab?w=600&auto=format&fit=crop&q=80' -- Cá thu nướng
    WHEN 2706284 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5e/Bristol_Bay_Salmon_Tartare_0440_%287564711524%29.jpg/500px-Bristol_Bay_Salmon_Tartare_0440_%287564711524%29.jpg' -- Cá hồi tươi
    WHEN 2706286 THEN 'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=600&auto=format&fit=crop&q=80' -- Cá hồi nướng
    WHEN 2706289 THEN 'https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=600&auto=format&fit=crop&q=80' -- Cá hồi chiên
    WHEN 2706293 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fc/Image_of_canned_sardines_by_year_at_the_Fantastic_World_of_Portuguese_Sardines.jpg/500px-Image_of_canned_sardines_by_year_at_the_Fantastic_World_of_Portuguese_Sardines.jpg' -- Cá mòi đóng hộp
    WHEN 2706311 THEN 'https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=600&auto=format&fit=crop&q=80' -- Cá ngừ đóng hộp
    WHEN 2706323 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/54/Steamed_tilapia.jpg/500px-Steamed_tilapia.jpg' -- Cá rô phi hấp
    WHEN 2706363 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/71/Shrimp_Boil.jpg/500px-Shrimp_Boil.jpg' -- Tôm hấp / luộc
    WHEN 900008001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9e/Goby_caught_in_Negombo_Sri_Lanka.jpg/500px-Goby_caught_in_Negombo_Sri_Lanka.jpg' -- Cá bống
    WHEN 900008002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/77/Mountain_Minnow_Freshwater_fish.jpg/500px-Mountain_Minnow_Freshwater_fish.jpg' -- Cá chày
    WHEN 900008003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d7/Cyprinus_carpio_%28Carpe_commune%29_-_426.jpg/500px-Cyprinus_carpio_%28Carpe_commune%29_-_426.jpg' -- Cá chép
    WHEN 900008004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/57/FMIB_36387_Sharp_nosed_eel_%281%29%3B_Conger_eel_%282%29.jpeg/500px-FMIB_36387_Sharp_nosed_eel_%281%29%3B_Conger_eel_%282%29.jpeg' -- Cá dưa
    WHEN 900008005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2c/Whitebait_Fritter.JPG/500px-Whitebait_Fritter.JPG' -- Cá dầu
    WHEN 900008006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e2/Mulets_%28Mugilidae%29_%28Ifremer_00761-87341%29.jpg/500px-Mulets_%28Mugilidae%29_%28Ifremer_00761-87341%29.jpg' -- Cá diếc
    WHEN 900008007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2b/Trichiurus_lepturus_mangaluru.jpg/500px-Trichiurus_lepturus_mangaluru.jpg' -- Cá đao
    WHEN 900008008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/08/Elongate_ilisha.jpg/500px-Elongate_ilisha.jpg' -- Cá đé
    WHEN 900008009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1e/Cleaned_Gray_Mullet_cut_pieces.jpg/500px-Cleaned_Gray_Mullet_cut_pieces.jpg' -- Cá đối
    WHEN 900008010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/23/Rhinogobius_flumineus%28Hamamatsu%2CShizuoka%2CJapan%29.jpg/500px-Rhinogobius_flumineus%28Hamamatsu%2CShizuoka%2CJapan%29.jpg' -- Cá đồng tiền
    WHEN 900008011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/63/Oncorhynchus_nerka.jpg/500px-Oncorhynchus_nerka.jpg' -- Cá hồi
    WHEN 900008012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/06/Dried_fish_fillets.jpg/500px-Dried_fish_fillets.jpg' -- Cá khô (chim, thu, nụ, đé)
    WHEN 900008013 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/73/Indian_bartail_flatheaf%28platycephalus_indicus%29.jpg/500px-Indian_bartail_flatheaf%28platycephalus_indicus%29.jpg' -- Cá lác
    WHEN 900008014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/af/Barbon_gonion_120127-22830_tsm.JPG/500px-Barbon_gonion_120127-22830_tsm.JPG' -- Cá mè
    WHEN 900008015 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6a/Sardina_pilchardus_2011.jpg/500px-Sardina_pilchardus_2011.jpg' -- Cá mòi
    WHEN 900008016 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a9/Frozen_pangasius_fish_cubes.jpg/500px-Frozen_pangasius_fish_cubes.jpg' -- Cá mỡ
    WHEN 900008017 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/92/Saurida_undosquamis.jpg/500px-Saurida_undosquamis.jpg' -- Cá mối
    WHEN 900008018 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7d/Fish_Fillet_Fingers.jpg/500px-Fish_Fillet_Fingers.jpg' -- Cá nạc
    WHEN 900008019 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/Yellowfin_tuna_nurp.jpg/500px-Yellowfin_tuna_nurp.jpg' -- Cá ngừ
    WHEN 900008020 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bb/Decapterus_koheru_%28koheru%29.jpg/500px-Decapterus_koheru_%28koheru%29.jpg' -- Cá nục
    WHEN 900008021 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Mulloidichthys_vanicolensis%2C_Jemeluk_Wall%2C_Amed%2C_Bali%2C_Indonesia_imported_from_iNaturalist_photo_429994681.jpg/500px-Mulloidichthys_vanicolensis%2C_Jemeluk_Wall%2C_Amed%2C_Bali%2C_Indonesia_imported_from_iNaturalist_photo_429994681.jpg' -- Cá phèn
    WHEN 900008022 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f8/Snakehead_-_Channa_argus_2.jpg/500px-Snakehead_-_Channa_argus_2.jpg' -- Cá quả
    WHEN 900008023 THEN 'https://upload.wikimedia.org/wikipedia/commons/f/f0/Anabas_testudineus.png' -- Cá rô đồng
    WHEN 900008024 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fc/Poisson_carpe_noire.jpg/500px-Poisson_carpe_noire.jpg' -- Cá rô phi
    WHEN 900008025 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/01/Peacock_Flounder_Bothus_mancus_in_Kona.jpg/500px-Peacock_Flounder_Bothus_mancus_in_Kona.jpg' -- Cá thờn bơn (cá bơn)
    WHEN 900008026 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/92/Trachurus_declivis.jpg/500px-Trachurus_declivis.jpg' -- Cá thu
    WHEN 900008027 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f7/Cololabis_saira_in_a_tank.jpg/500px-Cololabis_saira_in_a_tank.jpg' -- Cá thu đao
    WHEN 900008028 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/86/Misgurnus_anguillicaudatus.jpg/500px-Misgurnus_anguillicaudatus.jpg' -- Cá trạch (cá chạch)
    WHEN 900008029 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/64/Amur_Carp.jpg/500px-Amur_Carp.jpg' -- Cá trắm cỏ
    WHEN 900008030 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/15/Clarias_batrachus.jpg/500px-Clarias_batrachus.jpg' -- Cá trê
    WHEN 900008031 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/97/Catch_of_Atlantic_herring.jpg/500px-Catch_of_Atlantic_herring.jpg' -- Cá trích
    WHEN 900008032 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/19/Cirrhinus_cirrhosus_from_the_Nandu_River.jpg/500px-Cirrhinus_cirrhosus_from_the_Nandu_River.jpg' -- Cá trôi
    WHEN 900008033 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/42/Mud_crab_male.jpg/500px-Mud_crab_male.jpg' -- Cua bể
    WHEN 900008034 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/95/A_fresh_water_crab_in_Kannur%2C_Kerala_%281%29.jpg/500px-A_fresh_water_crab_in_Kannur%2C_Kerala_%281%29.jpg' -- Cua đồng
    WHEN 900008035 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b2/Portunus_pelagicus_male.jpg/500px-Portunus_pelagicus_male.jpg' -- ghẹ
    WHEN 900008036 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/66/Dried_sea_cucumber.jpg/500px-Dried_sea_cucumber.jpg' -- Hải sâm
    WHEN 900008037 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a1/Masijimi_01.jpg/500px-Masijimi_01.jpg' -- Hến
    WHEN 900008038 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2c/Monopterus_albus_2.jpg/500px-Monopterus_albus_2.jpg' -- Lươn
    WHEN 900008039 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7c/Dried_squid_3.png/500px-Dried_squid_3.png' -- Mực khô
    WHEN 900008040 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/08/Frozen_fresh_squid_head.jpg/500px-Frozen_fresh_squid_head.jpg' -- Mực tươi
    WHEN 900008041 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/10/Pila_ampullacea_01.JPG/500px-Pila_ampullacea_01.JPG' -- ốc bươu
    WHEN 900008042 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a9/Escargot_on_fork.jpg/500px-Escargot_on_fork.jpg' -- ốc đá
    WHEN 900008043 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/db/Helix_pomatia_89a.jpg/500px-Helix_pomatia_89a.jpg' -- ốc nhồi
    WHEN 900008044 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c3/Viviparidae_image.jpg/500px-Viviparidae_image.jpg' -- ốc vặn
    WHEN 900008045 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a9/Crab_Masala_%28Steamed%29.JPG/500px-Crab_Masala_%28Steamed%29.JPG' -- Rạm (muối, đồ)
    WHEN 900008046 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/17/Carcinus_maenas.jpg/500px-Carcinus_maenas.jpg' -- Rạm tươi
    WHEN 900008047 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5f/Alitta_succinea_%28epitoke%29.jpg/500px-Alitta_succinea_%28epitoke%29.jpg' -- Rươi
    WHEN 900008048 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/37/Oysters_p1040741.jpg/500px-Oysters_p1040741.jpg' -- Sò
    WHEN 900008049 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0a/9042Alamang_Acetes_sibogae_textures_11.jpg/500px-9042Alamang_Acetes_sibogae_textures_11.jpg' -- Tép gạo
    WHEN 900008050 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e4/Senna_Kunni_%28in_Tamil%29_dried_tiny_shrimps_JEG6149.jpg/500px-Senna_Kunni_%28in_Tamil%29_dried_tiny_shrimps_JEG6149.jpg' -- Tép khô
    WHEN 900008051 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e0/Prawn_raw_cleaned_2021b.jpg/500px-Prawn_raw_cleaned_2021b.jpg' -- Tôm biển
    WHEN 900008052 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0f/Fresh_water_shrimp.jpg/500px-Fresh_water_shrimp.jpg' -- Tôm đồng
    WHEN 900008053 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/22/Dried_river_shrimp.JPG/500px-Dried_river_shrimp.JPG' -- Tôm khô
    WHEN 900008054 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ec/Usfws-fws-biologist-holds-purple-bankclimber-freshwater-mussel-large.jpg/500px-Usfws-fws-biologist-holds-purple-bankclimber-freshwater-mussel-large.jpg' -- Trai
    WHEN 900008055 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4d/Prawn_Cracker.jpg/500px-Prawn_Cracker.jpg' -- Bánh phồng tôm rán
    WHEN 900008056 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3a/Menjemur_Kerupuk_Ikan.jpg/500px-Menjemur_Kerupuk_Ikan.jpg' -- Bánh phồng tôm sống
    WHEN 900008057 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/72/Minced_fish_meal_from_Morocco.jpg/500px-Minced_fish_meal_from_Morocco.jpg' -- Bột cá
    WHEN 900008058 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/89/Fish_floss.JPG/500px-Fish_floss.JPG' -- Ruốc cá quả
    WHEN 900008059 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b5/HK_food_%E4%B9%BE%E8%9D%A6%E7%B1%B3%E7%B2%92_dried_peeled_shrimp_prawn_January_2026_N13P.jpg/500px-HK_food_%E4%B9%BE%E8%9D%A6%E7%B1%B3%E7%B2%92_dried_peeled_shrimp_prawn_January_2026_N13P.jpg' -- Ruốc tôm
    WHEN 900011015 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/31/20250817_can_mackerel_fillets.jpg/500px-20250817_can_mackerel_fillets.jpg' -- Cá thu hộp
    WHEN 900011016 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ef/Midsummer_pickled_herring.jpg/500px-Midsummer_pickled_herring.jpg' -- Cá trích hộp
    ELSE image_url
END
WHERE id IN (2706241, 2706245, 2706264, 2706284, 2706286, 2706289, 2706293, 2706311, 2706323, 2706363, 900008001, 900008002, 900008003, 900008004, 900008005, 900008006, 900008007, 900008008, 900008009, 900008010, 900008011, 900008012, 900008013, 900008014, 900008015, 900008016, 900008017, 900008018, 900008019, 900008020, 900008021, 900008022, 900008023, 900008024, 900008025, 900008026, 900008027, 900008028, 900008029, 900008030, 900008031, 900008032, 900008033, 900008034, 900008035, 900008036, 900008037, 900008038, 900008039, 900008040, 900008041, 900008042, 900008043, 900008044, 900008045, 900008046, 900008047, 900008048, 900008049, 900008050, 900008051, 900008052, 900008053, 900008054, 900008055, 900008056, 900008057, 900008058, 900008059, 900011015, 900011016);

-- Nhóm FRUIT (81 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2709171 THEN 'https://images.unsplash.com/photo-1547514701-42782101795e?w=600&auto=format&fit=crop&q=80' -- Cam tươi
    WHEN 2709215 THEN 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600&auto=format&fit=crop&q=80' -- Táo tươi
    WHEN 2709223 THEN 'https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=600&auto=format&fit=crop&q=80' -- Quả bơ tươi
    WHEN 2709224 THEN 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600&auto=format&fit=crop&q=80' -- Chuối chín tươi
    WHEN 2709231 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a5/Sweet_cherries_in_basket_2018_G1.jpg/500px-Sweet_cherries_in_basket_2018_G1.jpg' -- Quả anh đào tươi
    WHEN 2709237 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e9/Blue_raw_grapes_in_vineyard_closeup.jpg/500px-Blue_raw_grapes_in_vineyard_closeup.jpg' -- Nho tươi
    WHEN 2709239 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4d/Kiwifruit_%27Gold%27_cross_section.jpg/500px-Kiwifruit_%27Gold%27_cross_section.jpg' -- Quả kiwi tươi
    WHEN 2709242 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/Raw_mango_%E2%9C%A8.jpg/500px-Raw_mango_%E2%9C%A8.jpg' -- Xoài tươi
    WHEN 2709246 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/36/Carica_papaya_%28Papayer%29_-_105.jpg/500px-Carica_papaya_%28Papayer%29_-_105.jpg' -- Đu đủ tươi
    WHEN 2709249 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/45/Peach_Hijiri.JPG/500px-Peach_Hijiri.JPG' -- Đào tươi
    WHEN 2709254 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2b/Fresh_Pear.jpg/500px-Fresh_Pear.jpg' -- Lê tươi
    WHEN 2709260 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cd/Fresh_pineapple%2C_Palawan%2C_Philippines.jpg/500px-Fresh_pineapple%2C_Palawan%2C_Philippines.jpg' -- Dứa tươi
    WHEN 2709265 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0a/Fresh_Plums_in_Yellow_Dish_%2848405358152%29.jpg/500px-Fresh_Plums_in_Yellow_Dish_%2848405358152%29.jpg' -- Mận tươi
    WHEN 2709267 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/85/Pomegranate_arils.jpg/500px-Pomegranate_arils.jpg' -- Quả lựu tươi
    WHEN 2709270 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/60/Watermelon_slice%2C_May_2024.jpg/500px-Watermelon_slice%2C_May_2024.jpg' -- Dưa hấu tươi
    WHEN 2709273 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/66/Ripe_and_raw_blackberries_%285322447967%29.jpg/500px-Ripe_and_raw_blackberries_%285322447967%29.jpg' -- Quả mâm xôi đen tươi
    WHEN 2709275 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7d/Dish_of_blueberries.jpg/500px-Dish_of_blueberries.jpg' -- Quả việt quất tươi
    WHEN 2709281 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a7/Raspberry_-_halved_%28Rubus_idaeus%29.jpg/500px-Raspberry_-_halved_%28Rubus_idaeus%29.jpg' -- Quả mâm xôi đỏ tươi
    WHEN 2709283 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/Bowl_of_fresh_strawberries_on_a_tray.jpg/500px-Bowl_of_fresh_strawberries_on_a_tray.jpg' -- Dâu tây tươi
    WHEN 900005001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f3/Pomelo_fruit.jpg/500px-Pomelo_fruit.jpg' -- Bưởi
    WHEN 900005002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e3/Oranges_-_whole-halved-segment.jpg/500px-Oranges_-_whole-halved-segment.jpg' -- Cam
    WHEN 900005003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e4/Lemon.jpg/500px-Lemon.jpg' -- Chanh
    WHEN 900005004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/83/Rambutan_white_background_alt.jpg/500px-Rambutan_white_background_alt.jpg' -- Chôm chôm
    WHEN 900005005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9e/Kerala_banana_chips_Upperi_varuthath.jpg/500px-Kerala_banana_chips_Upperi_varuthath.jpg' -- Chuối khô
    WHEN 900005006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/31/Cavendish_banana_from_Maracaibo.jpg/500px-Cavendish_banana_from_Maracaibo.jpg' -- Chuối tây
    WHEN 900005007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d5/Dwarf_Cavendish.jpg/500px-Dwarf_Cavendish.jpg' -- Chuối tiêu
    WHEN 900005008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cc/Blackberries_%28Rubus_fruticosus%29.jpg/500px-Blackberries_%28Rubus_fruticosus%29.jpg' -- Dâu gia
    WHEN 900005009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ef/Garden_strawberry_%28Fragaria_%C3%97_ananassa%29_halved.jpg/500px-Garden_strawberry_%28Fragaria_%C3%97_ananassa%29_halved.jpg' -- Dâu tây
    WHEN 900005010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c3/Cantaloupe_Melon_cross_section.png/500px-Cantaloupe_Melon_cross_section.png' -- Dưa bở
    WHEN 900005011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0c/Citrullus_lanatus_03.JPG/500px-Citrullus_lanatus_03.JPG' -- Dưa hấu
    WHEN 900005012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f5/Honeydew.jpg/500px-Honeydew.jpg' -- Dưa hồng
    WHEN 900005013 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/53/Starr-100611-6922-Cucumis_melo-Korean_melon_fruit-Olinda-Maui_%2824413100703%29.jpg/500px-Starr-100611-6922-Cucumis_melo-Korean_melon_fruit-Olinda-Maui_%2824413100703%29.jpg' -- Dưa lê
    WHEN 900005014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/40/Wild_pineapple.jpg/500px-Wild_pineapple.jpg' -- Dứa ta
    WHEN 900005015 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/75/AnanasComosusOnPlant.jpg/500px-AnanasComosusOnPlant.jpg' -- Dứa tây
    WHEN 900005016 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9e/Autumn_Red_peaches.jpg/500px-Autumn_Red_peaches.jpg' -- Đào
    WHEN 900005017 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/ce/Ripe_papaya_with_seeds.jpg/500px-Ripe_papaya_with_seeds.jpg' -- Đu đủ chín
    WHEN 900005018 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/15/Wax_apple_%28Syzygium_samarangense%29.jpg/500px-Wax_apple_%28Syzygium_samarangense%29.jpg' -- Gioi
    WHEN 900005019 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/79/Clausena_lansium_%282%29.jpg/500px-Clausena_lansium_%282%29.jpg' -- Hồng bì
    WHEN 900005020 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/22/DIOSPYROS_VIRGINIANA_calyx_atop_ripe_fruit.jpg/500px-DIOSPYROS_VIRGINIANA_calyx_atop_ripe_fruit.jpg' -- Hồng đỏ
    WHEN 900005021 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/97/Persimmon-oliv2.jpg/500px-Persimmon-oliv2.jpg' -- Hồng ngâm
    WHEN 900005022 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a9/ManilZapot_070624_188_ckup.jpg/500px-ManilZapot_070624_188_ckup.jpg' -- Hồng xiêm
    WHEN 900005023 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/99/Four_pears.jpg/500px-Four_pears.jpg' -- Lê
    WHEN 900005024 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fa/Pomegranate_fruit_-_whole_and_piece_with_arils.jpg/500px-Pomegranate_fruit_-_whole_and_piece_with_arils.jpg' -- Lựu
    WHEN 900005025 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Matured_Soursop.jpg/500px-Matured_Soursop.jpg' -- Mãng cầu xiêm
    WHEN 900005026 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/28/Pyrus_granulosa.jpg/500px-Pyrus_granulosa.jpg' -- Mắc coọc
    WHEN 900005027 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6c/Plums.jpg/500px-Plums.jpg' -- Mận
    WHEN 900005028 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c6/Jackfruit_Cheena02_Asit.jpg/500px-Jackfruit_Cheena02_Asit.jpg' -- Mít dai
    WHEN 900005029 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/72/Freeze-dried_jackfruit.jpg/500px-Freeze-dried_jackfruit.jpg' -- Mít khô
    WHEN 900005030 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c3/Jackfruits_growing_on_a_jackfruit_tree_in_Kanungu_District%2C_Uganda.jpg/500px-Jackfruits_growing_on_a_jackfruit_tree_in_Kanungu_District%2C_Uganda.jpg' -- Mít mật
    WHEN 900005031 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/Apricots.jpg/500px-Apricots.jpg' -- Mơ
    WHEN 900005032 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a9/Ab_food_04.jpg/500px-Ab_food_04.jpg' -- Mơ khô
    WHEN 900005033 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/82/Green_Mangoes_%28Mangifera_indica%29.jpg/500px-Green_Mangoes_%28Mangifera_indica%29.jpg' -- Muỗm, quéo
    WHEN 900005034 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/42/Sugar_apple_on_tree.jpg/500px-Sugar_apple_on_tree.jpg' -- Na
    WHEN 900005035 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a4/Dimocarpus_longan_%282%29.jpg/500px-Dimocarpus_longan_%282%29.jpg' -- Nhãn
    WHEN 900005036 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/58/Dried_longan_%2820240619%29.jpg/500px-Dried_longan_%2820240619%29.jpg' -- Nhãn khô
    WHEN 900005037 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/Green_Grape_3.jpg/500px-Green_Grape_3.jpg' -- Nho ngọt
    WHEN 900005038 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7f/Sour_grape_juice2.jpg/500px-Sour_grape_juice2.jpg' -- Nho ta (nho chua)
    WHEN 900005039 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/Elaeagnus_angustifolia_branch.JPG/500px-Elaeagnus_angustifolia_branch.JPG' -- Nhót
    WHEN 900005040 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/dc/GUAVA_FRUIT.jpg/500px-GUAVA_FRUIT.jpg' -- ổi
    WHEN 900005041 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b3/Ugandan_avocado_in_green_and_purple_1.jpg/500px-Ugandan_avocado_in_green_and_purple_1.jpg' -- Quả bơ vỏ tím
    WHEN 900005042 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5d/Avocado_with_cross_section_edit.jpg/500px-Avocado_with_cross_section_edit.jpg' -- Quả bơ vỏ xanh
    WHEN 900005043 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7c/Spondias_mombin_MS4005.JPG/500px-Spondias_mombin_MS4005.JPG' -- Quả cóc
    WHEN 900005044 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/55/Dragon_Fruit_01.jpg/500px-Dragon_Fruit_01.jpg' -- Quả thanh long
    WHEN 900005045 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f6/Lucuma.png/500px-Lucuma.png' -- Quả trứng gà
    WHEN 900005046 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0e/Kumquat_Trees_%28Corfu%29.JPG/500px-Kumquat_Trees_%28Corfu%29.JPG' -- Quất chín (cả vỏ)
    WHEN 900005047 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/49/Mandarin_Oranges_%28Citrus_Reticulata%29.jpg/500px-Mandarin_Oranges_%28Citrus_Reticulata%29.jpg' -- Quít
    WHEN 900005048 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/02/Durian.jpg/500px-Durian.jpg' -- Sầu riêng
    WHEN 900005049 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0c/Dracontomelon_dao_6.JPG/500px-Dracontomelon_dao_6.JPG' -- Sấu chín
    WHEN 900005050 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2e/Jujubes_%285194455076%29.jpg/500px-Jujubes_%285194455076%29.jpg' -- Táo ta
    WHEN 900005051 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Apple.jpg/500px-Red_Apple.jpg' -- Táo tây
    WHEN 900005052 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5c/Lychee_fruits_and_seed.jpg/500px-Lychee_fruits_and_seed.jpg' -- Vải
    WHEN 900005053 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a5/Dried_lychee_%281%29.jpg/500px-Dried_lychee_%281%29.jpg' -- Vải khô
    WHEN 900005054 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/80/Tr%C3%A1i_v%C3%BA_s%E1%BB%AFa_c%C3%B2n_xanh.jpg/500px-Tr%C3%A1i_v%C3%BA_s%E1%BB%AFa_c%C3%B2n_xanh.jpg' -- Vú sữa
    WHEN 900005055 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/03/A_mango_when_ripe.jpg/500px-A_mango_when_ripe.jpg' -- Xoài chín
    WHEN 900005056 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b8/Kiwi_%28Actinidia_chinensis%29_1_Luc_Viatour.jpg/500px-Kiwi_%28Actinidia_chinensis%29_1_Luc_Viatour.jpg' -- Quả kiwi
    WHEN 900011001 THEN 'https://upload.wikimedia.org/wikipedia/commons/c/c1/Chu%E1%BB%91i_ng%E1%BB%B1.jpg' -- Chuối nước đường
    WHEN 900011003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a8/Canned_Pineapple_Chunks.jpg/500px-Canned_Pineapple_Chunks.jpg' -- Dứa hộp
    WHEN 900011005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cf/Pears.jpg/500px-Pears.jpg' -- Mắc coọc nước đường
    WHEN 900011006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ec/Plums_African_Rose_-_whole%2C_halved_and_slice.jpg/500px-Plums_African_Rose_-_whole%2C_halved_and_slice.jpg' -- Mận nước đường
    WHEN 900011012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a1/Owoce_Longan.jpg/500px-Owoce_Longan.jpg' -- Nhãn nước đường
    WHEN 900011014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/24/Lychee_fruit_peeled.jpg/500px-Lychee_fruit_peeled.jpg' -- Vải nước đường
    ELSE image_url
END
WHERE id IN (2709171, 2709215, 2709223, 2709224, 2709231, 2709237, 2709239, 2709242, 2709246, 2709249, 2709254, 2709260, 2709265, 2709267, 2709270, 2709273, 2709275, 2709281, 2709283, 900005001, 900005002, 900005003, 900005004, 900005005, 900005006, 900005007, 900005008, 900005009, 900005010, 900005011, 900005012, 900005013, 900005014, 900005015, 900005016, 900005017, 900005018, 900005019, 900005020, 900005021, 900005022, 900005023, 900005024, 900005025, 900005026, 900005027, 900005028, 900005029, 900005030, 900005031, 900005032, 900005033, 900005034, 900005035, 900005036, 900005037, 900005038, 900005039, 900005040, 900005041, 900005042, 900005043, 900005044, 900005045, 900005046, 900005047, 900005048, 900005049, 900005050, 900005051, 900005052, 900005053, 900005054, 900005055, 900005056, 900011001, 900011003, 900011005, 900011006, 900011012, 900011014);

-- Nhóm LEGUMES_NUTS (52 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2705404 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fc/004-soymilk.jpg/500px-004-soymilk.jpg' -- Sữa đậu nành có đường
    WHEN 2705405 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7b/Soy_milk_with_glutinous_rice_balls%2C_August_2013.jpg/500px-Soy_milk_with_glutinous_rice_balls%2C_August_2013.jpg' -- Sữa đậu nành không đường
    WHEN 2705409 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/de/Home-made_almond_milk%2C_November_2012.jpg/500px-Home-made_almond_milk%2C_November_2012.jpg' -- Sữa hạnh nhân không đường
    WHEN 2705412 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/82/Oatmilk_1.jpg/500px-Oatmilk_1.jpg' -- Sữa yến mạch
    WHEN 2705413 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bb/Coconut_milk_from_can01.JPG/500px-Coconut_milk_from_can01.JPG' -- Sữa dừa
    WHEN 2707388 THEN 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=600&auto=format&fit=crop&q=80' -- Đậu nành nấu chín
    WHEN 2707414 THEN 'https://images.unsplash.com/photo-1515543904379-3d757afe72e4?w=600&auto=format&fit=crop&q=80' -- Đậu gà nấu chín
    WHEN 2707436 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e3/Liat_Portal_for_Foodie_Disorder_-_Edamame_with_Walnuts.jpg/500px-Liat_Portal_for_Foodie_Disorder_-_Edamame_with_Walnuts.jpg' -- Đậu nành lông luộc
    WHEN 2707485 THEN 'https://images.unsplash.com/photo-1508061253366-f7da158b6d46?w=600&auto=format&fit=crop&q=80' -- Hạt hạnh nhân
    WHEN 2707497 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f8/Cashew_nuts_in_West_Bengal_of_India.jpg/500px-Cashew_nuts_in_West_Bengal_of_India.jpg' -- Hạt điều không muối
    WHEN 2707512 THEN 'https://images.unsplash.com/photo-1608797178974-15b35a64ede9?w=600&auto=format&fit=crop&q=80' -- Đậu phộng (Lạc)
    WHEN 2707530 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b5/Pistachio_vera.jpg/500px-Pistachio_vera.jpg' -- Hạt dẻ cười không muối
    WHEN 2707531 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ec/Shelled_English_Walnuts_2331px.jpg/500px-Shelled_English_Walnuts_2331px.jpg' -- Quả óc chó
    WHEN 2707533 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/eb/Breville_Boss-_Almond_Butter.jpg/500px-Breville_Boss-_Almond_Butter.jpg' -- Bơ hạnh nhân
    WHEN 2707537 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a2/Peanut_butter_glass.jpg/500px-Peanut_butter_glass.jpg' -- Bơ đậu phộng
    WHEN 2707568 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cc/Coconut_Milk_%28.jpg/500px-Coconut_Milk_%28.jpg' -- Nước cốt dừa
    WHEN 2707588 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3d/Linum_usitatissimum_qtl1.jpg/500px-Linum_usitatissimum_qtl1.jpg' -- Hạt lanh
    WHEN 2707590 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/83/Semillas_de_Ch%C3%ADa.jpg/500px-Semillas_de_Ch%C3%ADa.jpg' -- Hạt chia
    WHEN 900003001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e6/Shredded_Coconut_Meat%2C_Aug_2026.jpg/500px-Shredded_Coconut_Meat%2C_Aug_2026.jpg' -- Cùi dừa già
    WHEN 900003002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d6/Young_coconut_with_special_knife.jpg/500px-Young_coconut_with_special_knife.jpg' -- Cùi dừa non
    WHEN 900003003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/83/Fava_Beans_Dried.JPG/500px-Fava_Beans_Dried.JPG' -- Đậu cô ve (hạt)
    WHEN 900003004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b4/Black_Beans_2.JPG/500px-Black_Beans_2.JPG' -- Đậu đen (hạt)
    WHEN 900003005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5c/Black_eyed_peas_food.jpg/500px-Black_eyed_peas_food.jpg' -- Đậu đũa (hạt)
    WHEN 900003006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6e/Dried_Green_Peas.jpg/500px-Dried_Green_Peas.jpg' -- Đậu Hà lan (hạt)
    WHEN 900003007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fd/Soybeans_and_dried_chillies.jpg/500px-Soybeans_and_dried_chillies.jpg' -- Đậu tương (đậu nành)
    WHEN 900003008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/93/Phaseolus_vulgaris_white_beans%2C_witte_boon.jpg/500px-Phaseolus_vulgaris_white_beans%2C_witte_boon.jpg' -- Đậu trắng hạt (Đậu Tây)
    WHEN 900003009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3f/Pinto_Beans_Seeds.jpg/500px-Pinto_Beans_Seeds.jpg' -- Đậu trứng cuốc
    WHEN 900003010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/86/Mung_beans_%28Vigna_radiata%29.jpg/500px-Mung_beans_%28Vigna_radiata%29.jpg' -- Đậu xanh (đậu tắt)
    WHEN 900003011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2c/Ch%C3%A2taignier_120807_1.jpg/500px-Ch%C3%A2taignier_120807_1.jpg' -- Hạt dẻ to
    WHEN 900003012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/28/Castanea_sativa_-_Anatolian_sweet_chestnut_04.jpg/500px-Castanea_sativa_-_Anatolian_sweet_chestnut_04.jpg' -- Hạt dẻ tươi
    WHEN 900003013 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a1/Castagne_secche_dried_chestnut_italy.JPG/500px-Castagne_secche_dried_chestnut_italy.JPG' -- Hạt dẻ khô
    WHEN 900003014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a4/Black_Sesame_Bingsu_Terrytown_LA.JPG/500px-Black_Sesame_Bingsu_Terrytown_LA.JPG' -- Hạt đen
    WHEN 900003015 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a5/Bowl_of_Cashews_and_Almonds_%2848140118636%29.jpg/500px-Bowl_of_Cashews_and_Almonds_%2848140118636%29.jpg' -- Hạt điều
    WHEN 900003016 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f2/Seeds_of_Jackfruit_001.jpg/500px-Seeds_of_Jackfruit_001.jpg' -- Hạt mít
    WHEN 900003017 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c2/Raw_peanuts_with_skin_on_white_plate.jpg/500px-Raw_peanuts_with_skin_on_white_plate.jpg' -- Lạc hạt
    WHEN 900003018 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/45/Mature_Oil_Palm_Bearing_Fresh_Fruit_Bunch_%28Elaeis_guineensis%29.jpg/500px-Mature_Oil_Palm_Bearing_Fresh_Fruit_Bunch_%28Elaeis_guineensis%29.jpg' -- Quả cọ tươi
    WHEN 900003019 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b4/Hodgsonia_heteroclita_fruit.jpg/500px-Hodgsonia_heteroclita_fruit.jpg' -- Quả đại hái tươi
    WHEN 900003020 THEN 'https://upload.wikimedia.org/wikipedia/commons/6/6a/Sesamum_indicum.jpg' -- Vừng (đen, trắng)
    WHEN 900003021 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/Soya_Chunks_and_Roti.jpg/500px-Soya_Chunks_and_Roti.jpg' -- Bột đậu tương đã loại béo (đậu nành)
    WHEN 900003022 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0e/Soy_powder.jpg/500px-Soy_powder.jpg' -- Bột đậu tương rang chín
    WHEN 900003023 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d2/Mung_bean_cake_at_Bac_Ha_Sunday_Market_in_Vietnam.jpg/500px-Mung_bean_cake_at_Bac_Ha_Sunday_Market_in_Vietnam.jpg' -- Bột đậu xanh
    WHEN 900003024 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0b/Si_with_Peanut_Flour.jpg/500px-Si_with_Peanut_Flour.jpg' -- Bột lạc
    WHEN 900003025 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/03/Japanese_SilkyTofu_%28Kinugoshi_Tofu%29.JPG/500px-Japanese_SilkyTofu_%28Kinugoshi_Tofu%29.JPG' -- Đậu phụ
    WHEN 900003026 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8c/Tofu_skin.png/500px-Tofu_skin.png' -- Đậu phụ chúc
    WHEN 900003027 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/65/HK_food_%E8%B1%86%E8%85%90%E5%8D%9C_Fried_Bean_Curd_product_Tofu_Puff_March_2020_SS2_02.jpg/500px-HK_food_%E8%B1%86%E8%85%90%E5%8D%9C_Fried_Bean_Curd_product_Tofu_Puff_March_2020_SS2_02.jpg' -- Đậu phụ nướng
    WHEN 900003028 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/da/Shelled_and_fried_pumpkin_seeds.jpg/500px-Shelled_and_fried_pumpkin_seeds.jpg' -- Hạt bí đỏ rang
    WHEN 900003029 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fe/Roasted_watermelon_seeds_2.jpg/500px-Roasted_watermelon_seeds_2.jpg' -- Hạt dưa đỏ rang (dưa hấu)
    WHEN 900003030 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cb/Charcoal_roasted_cashews.jpg/500px-Charcoal_roasted_cashews.jpg' -- Hạt điều khô, chiên dầu
    WHEN 900003031 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0f/001-soymilk.jpg/500px-001-soymilk.jpg' -- Sữa bột đậu nành
    WHEN 900003032 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/04/Soymilk_can_and_glass_2.jpg/500px-Soymilk_can_and_glass_2.jpg' -- Sữa đậu nành (100g đậu/lít)
    WHEN 900003033 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/75/Dofuhua-lamma.jpg/500px-Dofuhua-lamma.jpg' -- Tào phớ
    WHEN 900011004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2e/Roasted_peanuts_or_Nzungu_dzakakangiwa.jpg/500px-Roasted_peanuts_or_Nzungu_dzakakangiwa.jpg' -- Lạc chao dầu
    ELSE image_url
END
WHERE id IN (2705404, 2705405, 2705409, 2705412, 2705413, 2707388, 2707414, 2707436, 2707485, 2707497, 2707512, 2707530, 2707531, 2707533, 2707537, 2707568, 2707588, 2707590, 900003001, 900003002, 900003003, 900003004, 900003005, 900003006, 900003007, 900003008, 900003009, 900003010, 900003011, 900003012, 900003013, 900003014, 900003015, 900003016, 900003017, 900003018, 900003019, 900003020, 900003021, 900003022, 900003023, 900003024, 900003025, 900003026, 900003027, 900003028, 900003029, 900003030, 900003031, 900003032, 900003033, 900011004);

-- Nhóm MEAT (91 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2705841 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f6/Tenderloin_hot_plate%2C_Steak_Moen-Moen%2C_Lt_2%2C_BG_Junction%2C_2025_%2801%29.jpg/500px-Tenderloin_hot_plate%2C_Steak_Moen-Moen%2C_Lt_2%2C_BG_Junction%2C_2025_%2801%29.jpg' -- Bít tết thăn nội bò
    WHEN 2705868 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0a/White_asparagus_and_pork_chop%2C_Oberauroff.jpg/500px-White_asparagus_and_pork_chop%2C_Oberauroff.jpg' -- Cốt lết lợn nạc
    WHEN 2705956 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9d/Grilled_Chicken_Breasts_%2828905381261%29.jpg/500px-Grilled_Chicken_Breasts_%2828905381261%29.jpg' -- Ức gà nướng bỏ da
    WHEN 2706171 THEN 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=600&auto=format&fit=crop&q=80' -- Xúc xích bò
    WHEN 900007001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/Veal_chop%2C_Steckenroth.jpg/500px-Veal_chop%2C_Steckenroth.jpg' -- Thịt bê mỡ
    WHEN 900007002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f6/Veal_cutlet_Marechale.jpg/500px-Veal_cutlet_Marechale.jpg' -- Thịt bê nạc
    WHEN 900007003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d6/4_Kobe_Beef%2C_Kobe_Japan.jpg/500px-4_Kobe_Beef%2C_Kobe_Japan.jpg' -- Thịt bò loại I
    WHEN 900007004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bc/Raw_Beef_at_Barkin_Dogo_Market_04.jpg/500px-Raw_Beef_at_Barkin_Dogo_Market_04.jpg' -- Thịt bò loại II
    WHEN 900007005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/82/Beef_Strip_Sirloin_Steak_with_Potato_Fries.jpg/500px-Beef_Strip_Sirloin_Steak_with_Potato_Fries.jpg' -- Thịt bò, lưng, nạc
    WHEN 900007006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/54/Raw_Texas_Roadhouse_ribeye_steaks.jpg/500px-Raw_Texas_Roadhouse_ribeye_steaks.jpg' -- Thịt bò, lưng, nạc và mỡ
    WHEN 900007007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/Freshippo_salt-baked_squab.jpg/500px-Freshippo_salt-baked_squab.jpg' -- Thịt bồ câu ra ràng
    WHEN 900007008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Dog_meat_Shanghai.jpg/500px-Dog_meat_Shanghai.jpg' -- Thịt chó sấn
    WHEN 900007009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/72/Braised_Tenderloin_of_Beef.jpg/500px-Braised_Tenderloin_of_Beef.jpg' -- Thịt chó vai
    WHEN 900007010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/46/A_delicacy_at_the_farm_lamb_meat.jpg/500px-A_delicacy_at_the_farm_lamb_meat.jpg' -- Thịt cừu, nạc
    WHEN 900007011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/be/Goat_meat.jpg/500px-Goat_meat.jpg' -- Thịt dê, nạc
    WHEN 900007012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/56/Dusky_Grouse_-Dendragapus_obscurus_%2852843918400%29.jpg/500px-Dusky_Grouse_-Dendragapus_obscurus_%2852843918400%29.jpg' -- Thịt gà rừng
    WHEN 900007013 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/44/20221218_boiled_chicken_meat_potatoes_white_cabbage_salad.jpg/500px-20221218_boiled_chicken_meat_potatoes_white_cabbage_salad.jpg' -- Thịt gà ta
    WHEN 900007014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/11/RoastTurkey.jpg/500px-RoastTurkey.jpg' -- Thịt gà tây
    WHEN 900007015 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5f/Viande_de_cervid%C3%A9.JPG/500px-Viande_de_cervid%C3%A9.JPG' -- Thịt hươu
    WHEN 900007016 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/89/Grilled_kurobuta_pork_belly_02.jpg/500px-Grilled_kurobuta_pork_belly_02.jpg' -- Thịt lợn mỡ
    WHEN 900007017 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e2/Pork_Tenderloin_on_cutting_board.jpg/500px-Pork_Tenderloin_on_cutting_board.jpg' -- Thịt lợn nạc
    WHEN 900007018 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9b/Pork_Shoulder_picnic_roast.jpg/500px-Pork_Shoulder_picnic_roast.jpg' -- Thịt lợn nửa nạc, nửa mỡ
    WHEN 900007019 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Domestic_Goose.jpg/500px-Domestic_Goose.jpg' -- Thịt ngỗng
    WHEN 900007020 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/db/Sate_Jaran_%28Horse_Satay%29.jpg/500px-Sate_Jaran_%28Horse_Satay%29.jpg' -- Thịt ngựa
    WHEN 900007021 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1a/Jumbo_rabbit_meat.jpg/500px-Jumbo_rabbit_meat.jpg' -- Thịt thỏ nhà
    WHEN 900007022 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1f/Oryctolagus_cuniculus_Rcdo.jpg/500px-Oryctolagus_cuniculus_Rcdo.jpg' -- Thịt thỏ rừng
    WHEN 900007023 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/ce/A_Butcher_cutting_buffalo_meat.jpg/500px-A_Butcher_cutting_buffalo_meat.jpg' -- Thịt trâu
    WHEN 900007024 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/90/Buffalo_meat_sliced_%28Kathmandu%2C_Nepal%29.jpg/500px-Buffalo_meat_sliced_%28Kathmandu%2C_Nepal%29.jpg' -- Thịt trâu bắp
    WHEN 900007025 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/26/Cutting_Beef_Chuck.jpg/500px-Cutting_Beef_Chuck.jpg' -- Thịt trâu cổ
    WHEN 900007026 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8d/Beef_round_top_round_steak_in_package.jpg/500px-Beef_round_top_round_steak_in_package.jpg' -- Thịt trâu đùi
    WHEN 900007027 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/ba/Pemmican.jpg/500px-Pemmican.jpg' -- Thịt trâu thăn
    WHEN 900007028 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1a/Roast_leg_of_duck%2C_Ehrenbach.jpg/500px-Roast_leg_of_duck%2C_Ehrenbach.jpg' -- Thịt vịt
    WHEN 900007029 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8d/BeefKidney-nyrky.jpg/500px-BeefKidney-nyrky.jpg' -- Bầu dục bò
    WHEN 900007030 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Pork_kidney_slices.jpg/500px-Pork_kidney_slices.jpg' -- Bầu dục lợn
    WHEN 900007031 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c1/Braised_pork_skin.jpg/500px-Braised_pork_skin.jpg' -- Bì lợn
    WHEN 900007032 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ad/Pork_knuckle_with_roasted_potatoes%2C_Zabrze%2C_Silesian_Voivodeship%2C_Poland%2C_March_2024.jpg/500px-Pork_knuckle_with_roasted_potatoes%2C_Zabrze%2C_Silesian_Voivodeship%2C_Poland%2C_March_2024.jpg' -- Chân giò lợn
    WHEN 900007033 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f1/Trippa2.jpg/500px-Trippa2.jpg' -- Dạ dày bò
    WHEN 900007034 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d3/Hog_maw_%28cropped%29.jpg/500px-Hog_maw_%28cropped%29.jpg' -- Dạ dày lợn
    WHEN 900007035 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Braised_Beef_Cheek_Casserole_with_Seasonal_Vegetables_-_La_Terrasse%2C_Rouen_2026-05-10_%281%29.jpg/500px-Braised_Beef_Cheek_Casserole_with_Seasonal_Vegetables_-_La_Terrasse%2C_Rouen_2026-05-10_%281%29.jpg' -- Đầu bò
    WHEN 900007036 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/09/Pork_Jowl_Bao_-piggypiggypiggy_%2814199967622%29.jpg/500px-Pork_Jowl_Bao_-piggypiggypiggy_%2814199967622%29.jpg' -- Đầu lợn
    WHEN 900007037 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0c/Savory_Oxtail_feast.jpg/500px-Savory_Oxtail_feast.jpg' -- Đuôi bò
    WHEN 900007038 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/26/HK_SYP_%E8%A5%BF%E7%92%B0_Sai_Ying_Pun_%E6%AD%A3%E8%A1%97_Centre_Street_shop_Third_Street_Kai_Bo_Food_Supermarket_frozen_pork_tail_August_2021_SS2.jpg/500px-HK_SYP_%E8%A5%BF%E7%92%B0_Sai_Ying_Pun_%E6%AD%A3%E8%A1%97_Centre_Street_shop_Third_Street_Kai_Bo_Food_Supermarket_frozen_pork_tail_August_2021_SS2.jpg' -- Đuôi lợn
    WHEN 900007039 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c4/Beef_liver_nicely_cooked_at_Phakwe_Cattle_post.jpg/500px-Beef_liver_nicely_cooked_at_Phakwe_Cattle_post.jpg' -- Gan bò
    WHEN 900007040 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8b/Barbeque_chicken_liver_-_Lucknow%2C_U.P.jpg/500px-Barbeque_chicken_liver_-_Lucknow%2C_U.P.jpg' -- Gan gà
    WHEN 900007041 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/43/Pork_liver_31.jpg/500px-Pork_liver_31.jpg' -- Gan lợn
    WHEN 900007042 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0d/Foie_gras_IMGP2349.jpg/500px-Foie_gras_IMGP2349.jpg' -- Gan vịt
    WHEN 900007043 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/44/Braised_beef_tendon_%2820240925%29.jpg/500px-Braised_beef_tendon_%2820240925%29.jpg' -- Gân chân bò
    WHEN 900007044 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a1/Beef_tongue_preparation.jpg/500px-Beef_tongue_preparation.jpg' -- Lưỡi bò
    WHEN 900007045 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/ce/Pork_tongue_don_of_Gyukaku_03.jpg/500px-Pork_tongue_don_of_Gyukaku_03.jpg' -- Lưỡi lợn
    WHEN 900007046 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/49/Tr%C3%A0ng_l%E1%BB%A3n_lu%E1%BB%99c.jpg/500px-Tr%C3%A0ng_l%E1%BB%A3n_lu%E1%BB%99c.jpg' -- Lòng lợn (ruột già)
    WHEN 900007047 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e5/Pork_Jowls_and_Intestine_Adobo.jpg/500px-Pork_Jowls_and_Intestine_Adobo.jpg' -- Lòng lợn (ruột non)
    WHEN 900007048 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/74/Chicken_gizzard_slices.jpg/500px-Chicken_gizzard_slices.jpg' -- Mề gà
    WHEN 900007049 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c6/Homemade_beef_brain_bhuna.jpg/500px-Homemade_beef_brain_bhuna.jpg' -- óc bò
    WHEN 900007050 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9e/Pork_brain.jpg/500px-Pork_brain.jpg' -- óc lợn
    WHEN 900007051 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/60/Kam_Wah_Beef_Offal_15-10-2023.jpg/500px-Kam_Wah_Beef_Offal_15-10-2023.jpg' -- Phổi bò
    WHEN 900007052 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bf/Mixed_pork_offal%2C_Cantonese_lou_mei_style.JPG/500px-Mixed_pork_offal%2C_Cantonese_lou_mei_style.JPG' -- Phổi lợn
    WHEN 900007053 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d0/Pork_ribs_%281%29.jpg/500px-Pork_ribs_%281%29.jpg' -- Sườn lợn
    WHEN 900007054 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/23/Pig%27s_ear_%28food%29.jpg/500px-Pig%27s_ear_%28food%29.jpg' -- Tai lợn
    WHEN 900007055 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/67/Beef_heart_1.jpg/500px-Beef_heart_1.jpg' -- Tim bò
    WHEN 900007056 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0d/Chicken_hearts.jpg/500px-Chicken_hearts.jpg' -- Tim gà
    WHEN 900007057 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3b/Raw_pork_heart_for_sale_-_Kanagawa_Japan_-_2024_May_2.jpeg/500px-Raw_pork_heart_for_sale_-_Kanagawa_Japan_-_2024_May_2.jpeg' -- Tim lợn
    WHEN 900007058 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/df/Chongqing-style_boiled_blood_curd_%28Mao_Xuewang%29.jpg/500px-Chongqing-style_boiled_blood_curd_%28Mao_Xuewang%29.jpg' -- Tiết bò
    WHEN 900007059 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a9/Pig_blood_curd_-_01.jpg/500px-Pig_blood_curd_-_01.jpg' -- Tiết lợn luộc
    WHEN 900007060 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/42/Stornoway_Black_Pudding.jpg/500px-Stornoway_Black_Pudding.jpg' -- Tiết lợn sống
    WHEN 900007061 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/04/Beef_bone_marrow_%2828951978874%29.jpg/500px-Beef_bone_marrow_%2828951978874%29.jpg' -- Tủy xương bò
    WHEN 900007062 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/af/Markkloesschensuppe.jpg/500px-Markkloesschensuppe.jpg' -- Tủy xương lợn
    WHEN 900007063 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b3/Pates_p1150435.jpg/500px-Pates_p1150435.jpg' -- Ba tê
    WHEN 900007064 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8b/B%C3%BAn_ch%E1%BA%A3_Th%E1%BB%A5y_Khu%C3%AA.jpg/500px-B%C3%BAn_ch%E1%BA%A3_Th%E1%BB%A5y_Khu%C3%AA.jpg' -- Chả lợn
    WHEN 900007065 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/03/Banh_cuon_nhan_thit_cha_lua%2C_Banh_Cuon_Thang_Long.jpg/500px-Banh_cuon_nhan_thit_cha_lua%2C_Banh_Cuon_Thang_Long.jpg' -- Chả quế lợn
    WHEN 900007066 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b6/Kosher_foods_and_ham.jpg/500px-Kosher_foods_and_ham.jpg' -- Dăm bông lợn
    WHEN 900007067 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/34/D%E1%BB%93i.jpg/500px-D%E1%BB%93i.jpg' -- Dồi lợn
    WHEN 900007068 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/22/A_New_Zealand_beef_steak_with_corn_and_sausage_with_potatoes_with_dinner_set_in_caf%C3%A9_de_coral.jpg/500px-A_New_Zealand_beef_steak_with_corn_and_sausage_with_potatoes_with_dinner_set_in_caf%C3%A9_de_coral.jpg' -- Giò bò
    WHEN 900007069 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a5/Gi%C3%B2_l%E1%BB%A5a_2.jpg/500px-Gi%C3%B2_l%E1%BB%A5a_2.jpg' -- Giò lụa
    WHEN 900007070 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/ff/Head_cheese-2009.jpg/500px-Head_cheese-2009.jpg' -- Giò thủ lợn
    WHEN 900007071 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/60/Chinese_dried_sausages_in_Hong_Kong.jpg/500px-Chinese_dried_sausages_in_Hong_Kong.jpg' -- Lạp xường
    WHEN 900007072 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6f/Nh%C3%A0_m%C3%ACnh_L%E1%BB%85_30th4n2023_%28ch%E1%BA%A1o_nem_l%E1%BB%A5i%29_%281%29.jpg/500px-Nh%C3%A0_m%C3%ACnh_L%E1%BB%85_30th4n2023_%28ch%E1%BA%A1o_nem_l%E1%BB%A5i%29_%281%29.jpg' -- Nem chạo
    WHEN 900007073 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ed/Fermented_pork_wrapped_in_leaves.jpg/500px-Fermented_pork_wrapped_in_leaves.jpg' -- Nem chua
    WHEN 900007074 THEN 'https://upload.wikimedia.org/wikipedia/commons/f/f4/Fragrance_Pork_Floss.jpg' -- Ruốc thịt lợn
    WHEN 900007075 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/46/Beef_sticks_drying_in_the_sun.jpg/500px-Beef_sticks_drying_in_the_sun.jpg' -- Thịt bò khô
    WHEN 900007076 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1f/Beef-jerky.jpg/500px-Beef-jerky.jpg' -- Thịt trâu khô
    WHEN 900007077 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/45/Sausage_making-H-5-edited2.jpg/500px-Sausage_making-H-5-edited2.jpg' -- Xúc xích
    WHEN 900007078 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a3/Seaweed_pork_floss.jpg/500px-Seaweed_pork_floss.jpg' -- Bột cóc
    WHEN 900007079 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/37/Garden_locust_%28Acanthacris_ruficornis%29.jpg/500px-Garden_locust_%28Acanthacris_ruficornis%29.jpg' -- Châu chấu
    WHEN 900007080 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bc/Roasted_Frog_Legs_at_Le_Crocodile.jpg/500px-Roasted_Frog_Legs_at_Le_Crocodile.jpg' -- ếch (thịt đùi)
    WHEN 900007081 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/56/Beondegi_1.jpg/500px-Beondegi_1.jpg' -- Nhộng
    WHEN 900007082 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ec/Chicken_Hearts_01.JPG/500px-Chicken_Hearts_01.JPG' -- Lòng gà (cả bộ)
    WHEN 900011017 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/21/Canned_corned_beef_20201018_085330.jpg/500px-Canned_corned_beef_20201018_085330.jpg' -- Thịt bò hộp
    WHEN 900011018 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/41/Shredded_grilled_chicken_by_Chef_Phizzle._Native_Delight._09.jpg/500px-Shredded_grilled_chicken_by_Chef_Phizzle._Native_Delight._09.jpg' -- Thịt gà hộp
    WHEN 900011019 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/ba/Maling_B2_Canned_Pork_Luncheon_Meat%2C_340g_%2820200714133012%29.jpg/500px-Maling_B2_Canned_Pork_Luncheon_Meat%2C_340g_%2820200714133012%29.jpg' -- Thịt lợn hộp
    WHEN 900011020 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a7/Crumbling_ground_beef_in_water.jpg/500px-Crumbling_ground_beef_in_water.jpg' -- Thịt lợn, thịt bò xay hộp
    WHEN 900011021 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/83/CM_food_court_duck_and_pork.JPG/500px-CM_food_court_duck_and_pork.JPG' -- Thịt vịt hầm
    ELSE image_url
END
WHERE id IN (2705841, 2705868, 2705956, 2706171, 900007001, 900007002, 900007003, 900007004, 900007005, 900007006, 900007007, 900007008, 900007009, 900007010, 900007011, 900007012, 900007013, 900007014, 900007015, 900007016, 900007017, 900007018, 900007019, 900007020, 900007021, 900007022, 900007023, 900007024, 900007025, 900007026, 900007027, 900007028, 900007029, 900007030, 900007031, 900007032, 900007033, 900007034, 900007035, 900007036, 900007037, 900007038, 900007039, 900007040, 900007041, 900007042, 900007043, 900007044, 900007045, 900007046, 900007047, 900007048, 900007049, 900007050, 900007051, 900007052, 900007053, 900007054, 900007055, 900007056, 900007057, 900007058, 900007059, 900007060, 900007061, 900007062, 900007063, 900007064, 900007065, 900007066, 900007067, 900007068, 900007069, 900007070, 900007071, 900007072, 900007073, 900007074, 900007075, 900007076, 900007077, 900007078, 900007079, 900007080, 900007081, 900007082, 900011017, 900011018, 900011019, 900011020, 900011021);

-- Nhóm MIXED_DISH (3 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2707124 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/72/Pho_soup_and_noodles_with_meat.jpg/500px-Pho_soup_and_noodles_with_meat.jpg' -- Phở có thịt
    WHEN 2707125 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/06/Vegetarian_Pho_%285541416031%29.jpg/500px-Vegetarian_Pho_%285541416031%29.jpg' -- Phở chay (không thịt)
    WHEN 2709831 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/25/Classic_spinach_salad.jpg/500px-Classic_spinach_salad.jpg' -- Salad rau chân vịt
    ELSE image_url
END
WHERE id IN (2707124, 2707125, 2709831);

-- Nhóm OTHER (1 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2710740 THEN 'https://upload.wikimedia.org/wikipedia/commons/8/8e/Fish-protein-powder-500x500_%281%29.jpg' -- Bột dinh dưỡng pha uống
    ELSE image_url
END
WHERE id IN (2710740);

-- Nhóm SWEET (33 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2710336 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/Little_dark_chocolate_candies_%282026%29.jpg/500px-Little_dark_chocolate_candies_%282026%29.jpg' -- Sô-cô-la đen
    WHEN 900011007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1a/Turkish_pumpkin_dessert_%286383619319%29.jpg/500px-Turkish_pumpkin_dessert_%286383619319%29.jpg' -- Mứt bí ngô
    WHEN 900011008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/14/Orange_marmalade-3.jpg/500px-Orange_marmalade-3.jpg' -- Mứt cam có vỏ
    WHEN 900011009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Deep_fried_banana_dessert_at_Rice_and_Noodles_Thai_Restaurant.jpg/500px-Deep_fried_banana_dessert_at_Rice_and_Noodles_Thai_Restaurant.jpg' -- Mứt chuối
    WHEN 900011010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a6/Pineapple_jam.jpg/500px-Pineapple_jam.jpg' -- Mứt dứa
    WHEN 900011011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/64/Papaya_Jam-Kounkane.jpg/500px-Papaya_Jam-Kounkane.jpg' -- Mứt đu đủ
    WHEN 900012001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/79/Zwieback-1.jpg/500px-Zwieback-1.jpg' -- Bánh bích cốt
    WHEN 900012002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5d/Gurabija_%28%D0%B3%D1%83%D1%80%D0%B0%D0%B1%D0%B8%D1%98%D0%B0%2CQurabiya%3B_Cuisine_of_Serbia%29.jpg/500px-Gurabija_%28%D0%B3%D1%83%D1%80%D0%B0%D0%B1%D0%B8%D1%98%D0%B0%2CQurabiya%3B_Cuisine_of_Serbia%29.jpg' -- Bánh bích quy
    WHEN 900012003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ed/Custard_mooncake_-_Under_The_Same_Moon_2025-10-06.jpg/500px-Custard_mooncake_-_Under_The_Same_Moon_2025-10-06.jpg' -- Bánh chả
    WHEN 900012004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/94/Taiyaki_005.jpg/500px-Taiyaki_005.jpg' -- Bánh con cá
    WHEN 900012005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8e/Banh-Dau-Xanh.jpg/500px-Banh-Dau-Xanh.jpg' -- Bánh đậu xanh
    WHEN 900012006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6a/Wafers_chocolate.png/500px-Wafers_chocolate.png' -- Bánh kem xốp
    WHEN 900012007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ec/Biko_%28sticky_rice_cake%29.jpg/500px-Biko_%28sticky_rice_cake%29.jpg' -- Bánh khảo chay
    WHEN 900012008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0c/Necco_Chocolate_Wafer_Roll_%2819380565642%29.jpg/500px-Necco_Chocolate_Wafer_Roll_%2819380565642%29.jpg' -- Bánh quế
    WHEN 900012009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f0/Dubai_chocolate_on_a_plate_02.jpg/500px-Dubai_chocolate_on_a_plate_02.jpg' -- Bánh sô cô la
    WHEN 900012010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ef/Piece_of_chocolate_cake_on_a_white_plate_decorated_with_chocolate_sauce.jpg/500px-Piece_of_chocolate_cake_on_a_white_plate_decorated_with_chocolate_sauce.jpg' -- Bánh thỏi sô cô la
    WHEN 900012011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/75/Ayigbe_Biscuit_01.jpg/500px-Ayigbe_Biscuit_01.jpg' -- Bánh trứng nhện
    WHEN 900012012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fe/Cocoa_Powder_and_Chocolate_on_Marble_Background.jpg/500px-Cocoa_Powder_and_Chocolate_on_Marble_Background.jpg' -- Bột ca cao
    WHEN 900012013 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/59/Granulated_White_Sugar_with_Large_Crystals%2C_Light_Through_the_Crystals.jpg/500px-Granulated_White_Sugar_with_Large_Crystals%2C_Light_Through_the_Crystals.jpg' -- Đường cát
    WHEN 900012014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5b/Granulated_White_Sugar_with_Large_Crystals%2C_Bright_Front_Light.jpg/500px-Granulated_White_Sugar_with_Large_Crystals%2C_Bright_Front_Light.jpg' -- Đường kính
    WHEN 900012015 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ac/Carnival%21_Mackintosh%27s_toffee_de_luxe.jpg/500px-Carnival%21_Mackintosh%27s_toffee_de_luxe.jpg' -- Kẹo bơ cứng
    WHEN 900012016 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e0/001_2017_03_30_Haagsche_Hopjes.jpg/500px-001_2017_03_30_Haagsche_Hopjes.jpg' -- Kẹo cà phê
    WHEN 900012017 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/25/Ryukakusan_Medicated_Drops_Candy_Lemon_Honey_Ginger_Flavor_%283%29.jpg/500px-Ryukakusan_Medicated_Drops_Candy_Lemon_Honey_Ginger_Flavor_%283%29.jpg' -- Kẹo cam chanh
    WHEN 900012018 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/Cocnout_candy_8.jpg/500px-Cocnout_candy_8.jpg' -- Kẹo dừa mềm
    WHEN 900012019 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a4/Gummy_candy_in_vampire_shape_%2813912244523%29.jpg/500px-Gummy_candy_in_vampire_shape_%2813912244523%29.jpg' -- Kẹo dứa mềm
    WHEN 900012020 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/Candy_Food_Baguio%27s_Peanut_brittle_8.JPG/500px-Candy_Food_Baguio%27s_Peanut_brittle_8.JPG' -- Kẹo lạc
    WHEN 900012021 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bd/Peppermint_candy_cane_02.jpg/500px-Peppermint_candy_cane_02.jpg' -- Kẹo Pastille (kẹo ngậm bạc hà)
    WHEN 900012022 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/Rocky_Mountain_chocolates.jpg/500px-Rocky_Mountain_chocolates.jpg' -- Kẹo sô cô la
    WHEN 900012023 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/Galatine_Sperlari_Italian_milk_tablet_candy.png/500px-Galatine_Sperlari_Italian_milk_tablet_candy.png' -- Kẹo sữa
    WHEN 900012024 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Sesame_Seed_Candy.jpg/500px-Sesame_Seed_Candy.jpg' -- Kẹo vừng viên
    WHEN 900012025 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/60/Golden_syrup_tin_%2852689960070%29.jpg/500px-Golden_syrup_tin_%2852689960070%29.jpg' -- Mạch nha
    WHEN 900012026 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/Antique_Glass_Honey_Jar_With_Rusted_Metal_Cap_%2831009798197%29.jpg/500px-Antique_Glass_Honey_Jar_With_Rusted_Metal_Cap_%2831009798197%29.jpg' -- Mật ong
    WHEN 900012027 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ad/Sugar_coated_candied_peanuts.jpg/500px-Sugar_coated_candied_peanuts.jpg' -- Mứt lạc
    ELSE image_url
END
WHERE id IN (2710336, 900011007, 900011008, 900011009, 900011010, 900011011, 900012001, 900012002, 900012003, 900012004, 900012005, 900012006, 900012007, 900012008, 900012009, 900012010, 900012011, 900012012, 900012013, 900012014, 900012015, 900012016, 900012017, 900012018, 900012019, 900012020, 900012021, 900012022, 900012023, 900012024, 900012025, 900012026, 900012027);

-- Nhóm TUBER (28 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2709564 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c1/Traditional_Boiled_Cassava.jpg/500px-Traditional_Boiled_Cassava.jpg' -- Sắn luộc
    WHEN 2709699 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/51/Baked_potato%2C_baked_sweet_potato%2C_and_steamed_kale.jpg/500px-Baked_potato%2C_baked_sweet_potato%2C_and_steamed_kale.jpg' -- Khoai lang nướng
    WHEN 900002001 THEN 'https://upload.wikimedia.org/wikipedia/commons/f/f3/Water-caltrops.jpg' -- Củ ấu
    WHEN 900002002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7b/Dioscorea_alata_shoot_%28India%29.jpg/500px-Dioscorea_alata_shoot_%28India%29.jpg' -- Củ cái
    WHEN 900002003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/dd/Arrowroot_%28Maranta_arundinacea%29_Boiled.jpg/500px-Arrowroot_%28Maranta_arundinacea%29_Boiled.jpg' -- Củ dong
    WHEN 900002004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1c/Manihot_esculenta_Bitter_Cassava_%E1%83%99%E1%83%90%E1%83%A1%E1%83%90%E1%83%95%E1%83%90.JPG/500px-Manihot_esculenta_Bitter_Cassava_%E1%83%99%E1%83%90%E1%83%A1%E1%83%90%E1%83%95%E1%83%90.JPG' -- Củ sắn
    WHEN 900002005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6c/Gray_kudzu_root_block.jpg/500px-Gray_kudzu_root_block.jpg' -- Củ sắn dây
    WHEN 900002006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/24/Dried_lotus_root_slices.jpg/500px-Dried_lotus_root_slices.jpg' -- Củ súng khô (đã bỏ vỏ)
    WHEN 900002007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/94/Dioscorea_polystachya_UGA2307129.jpg/500px-Dioscorea_polystachya_UGA2307129.jpg' -- Củ từ
    WHEN 900002008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c8/Pile_of_Sweet_Potatoes_at_Barkin_Dogo_Market%2C_Kaduna_North_02.jpg/500px-Pile_of_Sweet_Potatoes_at_Barkin_Dogo_Market%2C_Kaduna_North_02.jpg' -- Khoai lang
    WHEN 900002009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/99/Chicken_piccata%2C_rainbow_salad_with_beets_and_blood_orange%2C_sweet_potato_puree%2C_and_roasted_cauliflower_with_pesto_-_Boston%2C_MA.jpg/500px-Chicken_piccata%2C_rainbow_salad_with_beets_and_blood_orange%2C_sweet_potato_puree%2C_and_roasted_cauliflower_with_pesto_-_Boston%2C_MA.jpg' -- Khoai lang nghệ
    WHEN 900002010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/30/Songe-R%C3%A9union.JPG/500px-Songe-R%C3%A9union.JPG' -- Khoai môn
    WHEN 900002011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a9/Red_taro_in_sugar_water.jpg/500px-Red_taro_in_sugar_water.jpg' -- Khoai nước
    WHEN 900002012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f2/Canna-indica-Sitia-Crete-Greece.jpg/500px-Canna-indica-Sitia-Crete-Greece.jpg' -- Khoai riềng
    WHEN 900002013 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/12/Taro_a_tuber_food_crop_04.jpg/500px-Taro_a_tuber_food_crop_04.jpg' -- Khoai sọ
    WHEN 900002014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/47/Russet_potato_cultivar_with_sprouts.jpg/500px-Russet_potato_cultivar_with_sprouts.jpg' -- Khoai tây
    WHEN 900002015 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/df/Dongfen.JPG/500px-Dongfen.JPG' -- Miến dong
    WHEN 900002016 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d5/Cornstarch_mixed_with_water.jpg/500px-Cornstarch_mixed_with_water.jpg' -- Bột dong lọc
    WHEN 900002017 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/30/Sweet_potato_flour.jpg/500px-Sweet_potato_flour.jpg' -- Bột khoai lang
    WHEN 900002018 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0a/HK_food_%E6%9C%A8%E8%96%AF%E7%B2%89_Pondan_brand_Tapioca_Starch_November_2022_Px3_04.jpg/500px-HK_food_%E6%9C%A8%E8%96%AF%E7%B2%89_Pondan_brand_Tapioca_Starch_November_2022_Px3_04.jpg' -- Bột khoai riềng (bột đao)
    WHEN 900002019 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/91/Potato_starch.jpg/500px-Potato_starch.jpg' -- Bột khoai tây (lọc)
    WHEN 900002020 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/59/Cassava_flour_-_making_dough_in_mortar.jpg/500px-Cassava_flour_-_making_dough_in_mortar.jpg' -- Bột sắn
    WHEN 900002021 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/dd/Corn_Starch_mixture.jpg/500px-Corn_Starch_mixture.jpg' -- Bột sắn dây
    WHEN 900002022 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ab/Kinko-imo.jpg/500px-Kinko-imo.jpg' -- Khoai lang khô
    WHEN 900002023 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1b/Dry_potatoes.jpg/500px-Dry_potatoes.jpg' -- Khoai tây khô
    WHEN 900002024 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Potato-Chips.jpg/500px-Potato-Chips.jpg' -- Khoai tây lát chiên
    WHEN 900002025 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/91/Tortasdecasabe.JPG/500px-Tortasdecasabe.JPG' -- Sắn khô
    WHEN 900002026 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e4/%D7%9E%D7%A9%D7%A7%D7%94_%D7%A2%D7%9D_%D7%9B%D7%93%D7%95%D7%A8%D7%99_%D7%98%D7%A4%D7%99%D7%95%D7%A7%D7%94_%28cropped%29.jpg/500px-%D7%9E%D7%A9%D7%A7%D7%94_%D7%A2%D7%9D_%D7%9B%D7%93%D7%95%D7%A8%D7%99_%D7%98%D7%A4%D7%99%D7%95%D7%A7%D7%94_%28cropped%29.jpg' -- Trân châu sắn
    ELSE image_url
END
WHERE id IN (2709564, 2709699, 900002001, 900002002, 900002003, 900002004, 900002005, 900002006, 900002007, 900002008, 900002009, 900002010, 900002011, 900002012, 900002013, 900002014, 900002015, 900002016, 900002017, 900002018, 900002019, 900002020, 900002021, 900002022, 900002023, 900002024, 900002025, 900002026);

-- Nhóm VEGETABLE (137 món)
UPDATE nutrition_foods
SET image_url = CASE id
    WHEN 2709599 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/20/Boerenkool.jpg/500px-Boerenkool.jpg' -- Cải xoăn tươi
    WHEN 2709614 THEN 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600&auto=format&fit=crop&q=80' -- Rau chân vịt tươi
    WHEN 2709615 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/40/Ipomoea_aquatica_cooked.jpg/500px-Ipomoea_aquatica_cooked.jpg' -- Rau chân vịt luộc
    WHEN 2709643 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/40/Raw_broccoli_bunches.JPG/500px-Raw_broccoli_bunches.JPG' -- Bông cải xanh tươi
    WHEN 2709644 THEN 'https://images.unsplash.com/photo-1459411621453-7b03977f4bfc?w=600&auto=format&fit=crop&q=80' -- Bông cải xanh luộc
    WHEN 2709660 THEN 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=600&auto=format&fit=crop&q=80' -- Cà rốt tươi
    WHEN 2709719 THEN 'https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=600&auto=format&fit=crop&q=80' -- Cà chua tươi
    WHEN 2709777 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2f/Chou-fleur_02.jpg/500px-Chou-fleur_02.jpg' -- Súp lơ trắng tươi
    WHEN 2709793 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/eb/Raw_stuffed_mushrooms%2C_April_2008.jpg/500px-Raw_stuffed_mushrooms%2C_April_2008.jpg' -- Nấm tươi
    WHEN 2709929 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/76/Solanum_melongena_24_08_2012_%281%29.JPG/500px-Solanum_melongena_24_08_2012_%281%29.JPG' -- Cà tím luộc
    WHEN 900004001 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/21/Lagenaria_siceraria_var_peregrina_MHNT.BOT.2013.22.54.jpg/500px-Lagenaria_siceraria_var_peregrina_MHNT.BOT.2013.22.54.jpg' -- Bầu
    WHEN 900004002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/bb/W_tougan4091.jpg/500px-W_tougan4091.jpg' -- Bí đao (bí xanh)
    WHEN 900004003 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Cucurbita_2011_G1.jpg/500px-Cucurbita_2011_G1.jpg' -- Bí ngô
    WHEN 900004004 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f6/Solanum_melongena_%27Thai_Yellow%27_%28eggplant%2C_aubergine%29%2C_fruit_%28SaatPilot%29.jpg/500px-Solanum_melongena_%27Thai_Yellow%27_%28eggplant%2C_aubergine%29%2C_fruit_%28SaatPilot%29.jpg' -- Cà bát
    WHEN 900004005 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/23/Tomato_plant_2026_G2.jpg/500px-Tomato_plant_2026_G2.jpg' -- Cà chua
    WHEN 900004006 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c0/Small_egg_plant_on_market.jpg/500px-Small_egg_plant_on_market.jpg' -- Cà pháo
    WHEN 900004007 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/32/Carrots_of_many_colors.jpg/500px-Carrots_of_many_colors.jpg' -- Cà rốt (củ đỏ, vàng)
    WHEN 900004008 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7d/Joint_venture_dehydrated_dried_vegetables.jpg/500px-Joint_venture_dehydrated_dried_vegetables.jpg' -- Cà rốt khô
    WHEN 900004009 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/60/Aubergine-Varianten.JPG/500px-Aubergine-Varianten.JPG' -- Cà tím
    WHEN 900004010 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/Starr-070730-7854-Brassica_oleracea_var_capitata-green_and_red-Foodland_Pukalani-Maui_%2824890472445%29.jpg/500px-Starr-070730-7854-Brassica_oleracea_var_capitata-green_and_red-Foodland_Pukalani-Maui_%2824890472445%29.jpg' -- Cải bắp
    WHEN 900004011 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/85/Brassica_oleracea_var_capitata_Rubyball.jpg/500px-Brassica_oleracea_var_capitata_Rubyball.jpg' -- Cải bắp đỏ
    WHEN 900004012 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/24/Napa_Cabbage.jpg/500px-Napa_Cabbage.jpg' -- Cải bắp khô
    WHEN 900004013 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/Shungiku.jpg/500px-Shungiku.jpg' -- Cải cúc
    WHEN 900004014 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/dd/Watercress_%282%29.JPG/500px-Watercress_%282%29.JPG' -- Cải soong
    WHEN 900004015 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/60/Bok_choy%2C_sauteed_-_Massachusetts.jpg/500px-Bok_choy%2C_sauteed_-_Massachusetts.jpg' -- Cải thìa (cải trắng)
    WHEN 900004016 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3a/%E0%AE%95%E0%AE%9F%E0%AF%81%E0%AE%95%E0%AF%81%E0%AE%9A%E0%AF%8D%E0%AE%9A%E0%AF%86%E0%AE%9F%E0%AE%BF_1%28Brassica_juncea%29.jpg/500px-%E0%AE%95%E0%AE%9F%E0%AF%81%E0%AE%95%E0%AF%81%E0%AE%9A%E0%AF%8D%E0%AE%9A%E0%AF%86%E0%AE%9F%E0%AE%BF_1%28Brassica_juncea%29.jpg' -- Cải xanh
    WHEN 900004017 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b8/Oenanthe_javanica_iNat-138244285.jpg/500px-Oenanthe_javanica_iNat-138244285.jpg' -- Cần ta
    WHEN 900004018 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/58/Chinese_celery.jpg/500px-Chinese_celery.jpg' -- Cần tây
    WHEN 900004019 THEN 'https://upload.wikimedia.org/wikipedia/commons/d/de/Bananavarieties.jpg' -- Chuối xanh
    WHEN 900004020 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a1/Raphanus_sativus%2C_red_radish_variant.jpg/500px-Raphanus_sativus%2C_red_radish_variant.jpg' -- Củ cải đỏ
    WHEN 900004021 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/cb/Daikon.Japan.jpg/500px-Daikon.Japan.jpg' -- Củ cải trắng
    WHEN 900004022 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1d/Drying_sliced_White_radish_at_the_Ba-yan_Neighborhood_2014-05-17.jpg/500px-Drying_sliced_White_radish_at_the_Ba-yan_Neighborhood_2014-05-17.jpg' -- Củ cải trắng khô
    WHEN 900004023 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/43/Caja_de_j%C3%ADcamas.jpg/500px-Caja_de_j%C3%ADcamas.jpg' -- Củ đậu
    WHEN 900004024 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8c/Water_bamboo_%28stem%29_and_Manchurian_wild_rice_%28stem%29.jpg/500px-Water_bamboo_%28stem%29_and_Manchurian_wild_rice_%28stem%29.jpg' -- Củ niễng
    WHEN 900004025 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a8/Okra%2C_turnips_with_greens%2C_potatos%2C_a_bag_of_mushrooms_%284053892935%29.jpg/500px-Okra%2C_turnips_with_greens%2C_potatos%2C_a_bag_of_mushrooms_%284053892935%29.jpg' -- Dọc củ cải (non)
    WHEN 900004026 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/48/Alocasia_indica_var._Metallica_3zz.jpg/500px-Alocasia_indica_var._Metallica_3zz.jpg' -- Dọc mùng
    WHEN 900004027 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/86/Cucumber_in_jaffna.JPG/500px-Cucumber_in_jaffna.JPG' -- Dưa chuột
    WHEN 900004028 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f0/Cucumber%2C_01.jpg/500px-Cucumber%2C_01.jpg' -- Dưa gang
    WHEN 900004029 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8b/Four_Different_Varieties_of_Green_Beans_%28Phaseolus_vulgaris%29.jpg/500px-Four_Different_Varieties_of_Green_Beans_%28Phaseolus_vulgaris%29.jpg' -- Đậu cô ve
    WHEN 900004030 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/38/Yardlong_bean_and_Carrot.jpg/500px-Yardlong_bean_and_Carrot.jpg' -- Đậu đũa
    WHEN 900004031 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7a/India_-_Varanasi_green_peas_-_2714.jpg/500px-India_-_Varanasi_green_peas_-_2714.jpg' -- Đậu Hà Lan
    WHEN 900004032 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f3/%C4%90%E1%BA%ADu_r%E1%BB%93ng.jpg/500px-%C4%90%E1%BA%ADu_r%E1%BB%93ng.jpg' -- Đậu rồng (quả non)
    WHEN 900004033 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/99/Raw_papaya_fruits_on_tree.jpg/500px-Raw_papaya_fruits_on_tree.jpg' -- Đu đủ xanh
    WHEN 900004034 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d4/Qua_Gac.JPG/500px-Qua_Gac.JPG' -- Gấc
    WHEN 900004035 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4f/Kongnamul_%28soybean_sprout%29_2.jpg/500px-Kongnamul_%28soybean_sprout%29_2.jpg' -- Giá đậu tương
    WHEN 900004036 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/73/Moyashi.jpg/500px-Moyashi.jpg' -- Giá đậu xanh
    WHEN 900004037 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/93/Allium_fistulosum_Ypey26.jpg/500px-Allium_fistulosum_Ypey26.jpg' -- Hành củ tươi
    WHEN 900004038 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6f/Allium_fistulosum_MHNT.BOT.2011.3.23.jpg/500px-Allium_fistulosum_MHNT.BOT.2011.3.23.jpg' -- Hành lá (hành hoa)
    WHEN 900004039 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0e/Common_onion_harvested_at_Goodnestone_Park_Kent_England.jpg/500px-Common_onion_harvested_at_Goodnestone_Park_Kent_England.jpg' -- Hành tây
    WHEN 900004040 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/88/Nelumbonaceae-_Nelumbo_nucifera_%28lotus%29.jpg/500px-Nelumbonaceae-_Nelumbo_nucifera_%28lotus%29.jpg' -- Hạt sen tươi
    WHEN 900004041 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9e/Lotus_Nelumbo_%27Mrs._Perry_D._Slocum%27_Dried_Seed_Head_2000px.jpg/500px-Lotus_Nelumbo_%27Mrs._Perry_D._Slocum%27_Dried_Seed_Head_2000px.jpg' -- Hạt sen khô
    WHEN 900004042 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/76/Allium_tuberosum_0zz.jpg/500px-Allium_tuberosum_0zz.jpg' -- Hẹ lá
    WHEN 900004043 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/21/A_banana_blossom_or_a_banana_flower.jpg/500px-A_banana_blossom_or_a_banana_flower.jpg' -- Hoa chuối
    WHEN 900004044 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d2/Hemerocallis_lilioasphodelus_%281%29.jpg/500px-Hemerocallis_lilioasphodelus_%281%29.jpg' -- Hoa lý
    WHEN 900004045 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3a/Carambola_Starfruit.jpg/500px-Carambola_Starfruit.jpg' -- Khế
    WHEN 900004046 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/%28MHNT%29_Piper_sarmentosum_%28B%C3%A9tel_sauvage%29_-_Feuilles_et_infrutescence_immature.jpg/500px-%28MHNT%29_Piper_sarmentosum_%28B%C3%A9tel_sauvage%29_-_Feuilles_et_infrutescence_immature.jpg' -- Lá lốt
    WHEN 900004047 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/24/Tamarind_leaves.jpg/500px-Tamarind_leaves.jpg' -- Lá me
    WHEN 900004048 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/88/L%C3%A1_m%C6%A1_l%C3%B4ng.jpg/500px-L%C3%A1_m%C6%A1_l%C3%B4ng.jpg' -- Lá mơ lông
    WHEN 900004049 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d6/Sombe_sauce_with_meat_and_ovacado_01.jpg/500px-Sombe_sauce_with_meat_and_ovacado_01.jpg' -- Lá sắn tươi
    WHEN 900004050 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/M%C4%83ng_chua_ng%C3%A2m_qu%E1%BA%A3_m%C3%B3c_m%E1%BA%ADt.jpg/500px-M%C4%83ng_chua_ng%C3%A2m_qu%E1%BA%A3_m%C3%B3c_m%E1%BA%ADt.jpg' -- Măng chua
    WHEN 900004051 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ad/Dried_bamboo_shoots_%282%29.jpg/500px-Dried_bamboo_shoots_%282%29.jpg' -- Măng khô
    WHEN 900004052 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e7/Steam-boiling_green_asparagus.jpg/500px-Steam-boiling_green_asparagus.jpg' -- Măng tây
    WHEN 900004053 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e1/Bamboo_shoot_pickles_with_%28left%29_and_without_%28right%29_chilli.jpg/500px-Bamboo_shoot_pickles_with_%28left%29_and_without_%28right%29_chilli.jpg' -- Măng tre
    WHEN 900004054 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7d/Luffa_aegyptica.jpg/500px-Luffa_aegyptica.jpg' -- Mướp
    WHEN 900004055 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/27/Cooking_Bitter_Melons.jpg/500px-Cooking_Bitter_Melons.jpg' -- Mướp đắng
    WHEN 900004056 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d5/Luffa_acutangula_Chinese_okra.jpg/500px-Luffa_acutangula_Chinese_okra.jpg' -- Mướp Nhật bản
    WHEN 900004057 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/64/Artemisia_ludoviciana_%27Valerie_Finnis%27_%2828770805916%29.jpg/500px-Artemisia_ludoviciana_%27Valerie_Finnis%27_%2828770805916%29.jpg' -- Ngải cứu
    WHEN 900004058 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/29/Stir_fry_%C3%A0_la_Gabi-showcasingbabycorn.jpg/500px-Stir_fry_%C3%A0_la_Gabi-showcasingbabycorn.jpg' -- Ngô bao tử
    WHEN 900004059 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d3/Lotus_root-Hubei_style.jpg/500px-Lotus_root-Hubei_style.jpg' -- Ngó sen
    WHEN 900004060 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8b/Luffa_acutangula_-_%E0%B4%95%E0%B4%95%E0%B5%8D%E0%B4%95%E0%B4%BF%E0%B4%B0%E0%B4%95%E0%B5%8D%E0%B4%95%E0%B4%AF%E0%B5%81%E0%B4%9F%E0%B5%86_%E0%B4%AA%E0%B5%82%E0%B4%B5%E0%B5%8D.jpg/500px-Luffa_acutangula_-_%E0%B4%95%E0%B4%95%E0%B5%8D%E0%B4%95%E0%B4%BF%E0%B4%B0%E0%B4%95%E0%B5%8D%E0%B4%95%E0%B4%AF%E0%B5%81%E0%B4%9F%E0%B5%86_%E0%B4%AA%E0%B5%82%E0%B4%B5%E0%B5%8D.jpg' -- Nụ mướp
    WHEN 900004061 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/da/Red_capsicum_and_cross_section.jpg/500px-Red_capsicum_and_cross_section.jpg' -- ớt đỏ to
    WHEN 900004062 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Peppers_in_water.jpg/500px-Peppers_in_water.jpg' -- ớt vàng to
    WHEN 900004063 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f6/Green_peppers_03.jpg/500px-Green_peppers_03.jpg' -- ớt xanh to
    WHEN 900004064 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1b/Garcinia_multiflora.jpg/500px-Garcinia_multiflora.jpg' -- Quả dọc
    WHEN 900004065 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a3/Dialium_guineense_%28Velvet_Tamarind%29.jpg/500px-Dialium_guineense_%28Velvet_Tamarind%29.jpg' -- Quả me chua
    WHEN 900004066 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/11/Pumpkin_Leaves_or_Muboora.jpg/500px-Pumpkin_Leaves_or_Muboora.jpg' -- Rau bí
    WHEN 900004067 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3f/Long_ogo_%28Gracilaria_parvispora%29.jpg/500px-Long_ogo_%28Gracilaria_parvispora%29.jpg' -- Rau câu khô
    WHEN 900004068 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e2/Fresh_vegetables_and_seaweed_%2814582741916%29.jpg/500px-Fresh_vegetables_and_seaweed_%2814582741916%29.jpg' -- Rau câu tươi
    WHEN 900004069 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/02/Lactuca_sativa_%27Lollo_Bionda%27.jpg/500px-Lactuca_sativa_%27Lollo_Bionda%27.jpg' -- Rau diếp
    WHEN 900004070 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a0/Corchorus_olitorius_%282%29.JPG/500px-Corchorus_olitorius_%282%29.JPG' -- Rau đay
    WHEN 900004071 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ee/Houttuynia_cordata.jpg/500px-Houttuynia_cordata.jpg' -- Rau giấp cá, diếp cá
    WHEN 900004072 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ec/Amaranthus_viridis_kz03.jpg/500px-Amaranthus_viridis_kz03.jpg' -- Rau giền cơm
    WHEN 900004073 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/aa/Amaranthus_tricolor_kz01.jpg/500px-Amaranthus_tricolor_kz01.jpg' -- Rau giền đỏ
    WHEN 900004074 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/91/Amaranthus_tricolor0.jpg/500px-Amaranthus_tricolor0.jpg' -- Rau giền trắng
    WHEN 900004075 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6e/Ocimum_basilicum_Sweet_Basil%2C_Common_Basil%2C_Thai_Basil_at_Wayanad_2019_%2810%29.jpg/500px-Ocimum_basilicum_Sweet_Basil%2C_Common_Basil%2C_Thai_Basil_at_Wayanad_2019_%2810%29.jpg' -- Rau húng
    WHEN 900004076 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1f/Edible_sweet_potato_leaves_%28matembele%29.jpg/500px-Edible_sweet_potato_leaves_%28matembele%29.jpg' -- Rau khoai lang
    WHEN 900004077 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/30/Sweet_marjoram_%28Origanum_syriacum%29.jpg/500px-Sweet_marjoram_%28Origanum_syriacum%29.jpg' -- Rau kinh giới
    WHEN 900004078 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/Centella_asiatica_6121.jpg/500px-Centella_asiatica_6121.jpg' -- Rau má rừng
    WHEN 900004079 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d5/Starr_020803-0094_Centella_asiatica.jpg/500px-Starr_020803-0094_Centella_asiatica.jpg' -- Rau má, má mơ
    WHEN 900004080 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ab/Basella_alba-2.JPG/500px-Basella_alba-2.JPG' -- Rau mồng tơi
    WHEN 900004081 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/11/Cilantro_and_garlic_paste.jpg/500px-Cilantro_and_garlic_paste.jpg' -- Rau mùi
    WHEN 900004082 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/77/Hombay3.jpg/500px-Hombay3.jpg' -- Rau mùi tàu
    WHEN 900004083 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/Ipomoea_aquatica_%282%29.JPG/500px-Ipomoea_aquatica_%282%29.JPG' -- Rau muống
    WHEN 900004084 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/05/Spinach_farming_in_Cameroon_%28Water_leave_gardening%29_01.jpg/500px-Spinach_farming_in_Cameroon_%28Water_leave_gardening%29_01.jpg' -- Rau muống khô
    WHEN 900004085 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/44/Ng%E1%BB%95_%28Limnophila_aromatica%29.JPG/500px-Ng%E1%BB%95_%28Limnophila_aromatica%29.JPG' -- Rau ngổ
    WHEN 900004086 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9b/SauropusAndrogynus.jpg/500px-SauropusAndrogynus.jpg' -- Rau ngót
    WHEN 900004087 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c4/Sauropus_androgynus_at_Kadavoor.jpg/500px-Sauropus_androgynus_at_Kadavoor.jpg' -- Rau ngót khô
    WHEN 900004088 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1f/%E8%B6%8A%E5%8D%97%E9%A6%99%E8%8F%9C_Polygonum_odoratum_-%E6%B3%B0%E5%9C%8B%E6%B8%85%E9%82%81%E8%8A%B1%E5%B1%95_Royal_Flora_Ratchaphruek%2C_Thailand-_%289240252668%29.jpg/500px-%E8%B6%8A%E5%8D%97%E9%A6%99%E8%8F%9C_Polygonum_odoratum_-%E6%B3%B0%E5%9C%8B%E6%B8%85%E9%82%81%E8%8A%B1%E5%B1%95_Royal_Flora_Ratchaphruek%2C_Thailand-_%289240252668%29.jpg' -- Rau răm
    WHEN 900004089 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7e/An-updated-checklist-of-aquatic-plants-of-Myanmar-and-Thailand-biodiversity_data_journal-2-e1019-g008.jpg/500px-An-updated-checklist-of-aquatic-plants-of-Myanmar-and-Thailand-biodiversity_data_journal-2-e1019-g008.jpg' -- Rau rút
    WHEN 900004090 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/Romaine_lettuce.jpg/500px-Romaine_lettuce.jpg' -- Rau sà lách
    WHEN 900004091 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2a/Portulaca_oleracea_MHNT.jpg/500px-Portulaca_oleracea_MHNT.jpg' -- Rau sam
    WHEN 900004092 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c1/Melientha_suavis.jpg/500px-Melientha_suavis.jpg' -- Rau sắng (chùa Hương)
    WHEN 900004093 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b8/Crassocephalum_crepidioides_08765.jpg/500px-Crassocephalum_crepidioides_08765.jpg' -- Rau tàu bay
    WHEN 900004094 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e1/Mint_leaves_%28Mentha_spicata%29.jpg/500px-Mint_leaves_%28Mentha_spicata%29.jpg' -- Rau thơm
    WHEN 900004095 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5e/Fruits_of_Dracontomelon_duperreanum.JPG/500px-Fruits_of_Dracontomelon_duperreanum.JPG' -- Sấu xanh
    WHEN 900004096 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/98/Brassica_oleracea_var._gongylodes_%28kohlrabi%29.jpg/500px-Brassica_oleracea_var._gongylodes_%28kohlrabi%29.jpg' -- Su hào
    WHEN 900004097 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f7/2018-06-01_%28131%29_Leaves_of_young_Brassica_oleracea_var._gongylodes_%28Kohlrabi%29_at_Bichlh%C3%A4usl_in_Frankenfels%2C_Austria.jpg/500px-2018-06-01_%28131%29_Leaves_of_young_Brassica_oleracea_var._gongylodes_%28Kohlrabi%29_at_Bichlh%C3%A4usl_in_Frankenfels%2C_Austria.jpg' -- Su hào khô
    WHEN 900004098 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/da/Chouchou_in_Reunion.jpg/500px-Chouchou_in_Reunion.jpg' -- Su su, quả
    WHEN 900004099 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/70/Cauliflower_white.jpg/500px-Cauliflower_white.jpg' -- Súp lơ trắng
    WHEN 900004100 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5e/Romanesco_broccoli_%28Brassica_oleracea%29.jpg/500px-Romanesco_broccoli_%28Brassica_oleracea%29.jpg' -- Súp lơ xanh
    WHEN 900004101 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3a/Aneth_FR_2012.jpg/500px-Aneth_FR_2012.jpg' -- Thìa là
    WHEN 900004102 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e0/Perilla_frutescens_var._acuta.JPG/500px-Perilla_frutescens_var._acuta.JPG' -- Tía tô
    WHEN 900004103 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/Garlic_bulbs_and_cloves.jpg/500px-Garlic_bulbs_and_cloves.jpg' -- Tỏi ta
    WHEN 900004104 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b6/Allium_tuberosum_2zz.jpg/500px-Allium_tuberosum_2zz.jpg' -- Tỏi tây (cả lá)
    WHEN 900004105 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d7/Green_and_Black_Olives.jpg/500px-Green_and_Black_Olives.jpg' -- Trám đen chín
    WHEN 900004106 THEN 'https://upload.wikimedia.org/wikipedia/commons/b/b1/%E6%A9%84%E6%A6%84%E6%A0%91.jpg' -- Trám xanh sống, trám trắng
    WHEN 900004107 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/27/Blumea_lanceolaria.JPG/500px-Blumea_lanceolaria.JPG' -- Xương sông
    WHEN 900004108 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0a/Pickled_Tomatoes.jpg/500px-Pickled_Tomatoes.jpg' -- Cà chua muối
    WHEN 900004109 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f0/Ca_muoi.jpg/500px-Ca_muoi.jpg' -- Cà muối nén
    WHEN 900004110 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ea/Solanum_torvum_2.jpg/500px-Solanum_torvum_2.jpg' -- Cà muối sổi
    WHEN 900004111 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/68/Preserved_Tianjin_vegetables_%28%E5%A4%A9%E6%B4%A5%E5%86%AC%E8%8F%9C%29_in_clay_crock.jpg/500px-Preserved_Tianjin_vegetables_%28%E5%A4%A9%E6%B4%A5%E5%86%AC%E8%8F%9C%29_in_clay_crock.jpg' -- Dưa cải bắp
    WHEN 900004112 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/46/202308_Pickled_Mustard_Green_Noodle_with_Braised_Dapai_and_Fried_Tofu_Toppings.jpg/500px-202308_Pickled_Mustard_Green_Noodle_with_Braised_Dapai_and_Fried_Tofu_Toppings.jpg' -- Dưa cải bẹ
    WHEN 900004113 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/05/Dua_gop_2.jpg/500px-Dua_gop_2.jpg' -- Dưa cải sen
    WHEN 900004114 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/22/Pickled_cucumbers.jpg/500px-Pickled_cucumbers.jpg' -- Dưa chuột muối
    WHEN 900004115 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fe/Mung_bean_sprouts%2C_close-up.jpg/500px-Mung_bean_sprouts%2C_close-up.jpg' -- Dưa giá (đậu xanh)
    WHEN 900004116 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/70/Silberzwiebeln_%289373-75%29.jpg/500px-Silberzwiebeln_%289373-75%29.jpg' -- Hành củ muối
    WHEN 900004117 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3e/D%C6%B0a_m%C3%B3n_C%C3%B4ng_%C4%90o%C3%A0n_Qu%E1%BA%A3ng_Tr%E1%BB%8B%2C_T%E1%BA%BFt_2018_%281%29.jpg/500px-D%C6%B0a_m%C3%B3n_C%C3%B4ng_%C4%90o%C3%A0n_Qu%E1%BA%A3ng_Tr%E1%BB%8B%2C_T%E1%BA%BFt_2018_%281%29.jpg' -- Kiệu muối
    WHEN 900004118 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7c/Artocarpus_heterophyllus_04_-_Young_fruit.jpg/500px-Artocarpus_heterophyllus_04_-_Young_fruit.jpg' -- Nhút (muối từ mít non, ngọn đậu xanh non...)
    WHEN 900004119 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/44/Nutritional_Yeast_in_Bag_%2832664238868%29.jpg/500px-Nutritional_Yeast_in_Bag_%2832664238868%29.jpg' -- Men bia khô
    WHEN 900004120 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5e/Fresh_Yeast_Breads.jpg/500px-Fresh_Yeast_Breads.jpg' -- Men bia tươi
    WHEN 900004121 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/84/Echt_judasoor_%28Auricularia_auricula-judae%29_op_een_stam_van_een_vlier._15-02-2024._%28actm.%29_15-02-2024._%28actm.%29.jpg/500px-Echt_judasoor_%28Auricularia_auricula-judae%29_op_een_stam_van_een_vlier._15-02-2024._%28actm.%29_15-02-2024._%28actm.%29.jpg' -- Mộc nhĩ
    WHEN 900004122 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b7/Dried_shiitake_mushrooms_%28Lentinula_edodes%29_from_Taiwan%2C_2026-11-07.jpg/500px-Dried_shiitake_mushrooms_%28Lentinula_edodes%29_from_Taiwan%2C_2026-11-07.jpg' -- Nấm hương khô
    WHEN 900004123 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5a/Fresh_shiitake_mushrooms.jpg/500px-Fresh_shiitake_mushrooms.jpg' -- Nấm hương tươi
    WHEN 900004124 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/26/Edible_fungi_in_basket_2012_G1.jpg/500px-Edible_fungi_in_basket_2012_G1.jpg' -- Nấm mỡ (Nấm tây)
    WHEN 900004125 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/92/Volvariella_bombycina1.jpg/500px-Volvariella_bombycina1.jpg' -- Nấm rơm
    WHEN 900004126 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/94/2016-01_Agaricus_bisporus_06.jpg/500px-2016-01_Agaricus_bisporus_06.jpg' -- Nấm thường tươi
    WHEN 900011002 THEN 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/af/Single_gherkin_pickle_jar%2C_2026-09-30.jpeg/500px-Single_gherkin_pickle_jar%2C_2026-09-30.jpeg' -- Dưa chuột hộp
    ELSE image_url
END
WHERE id IN (2709599, 2709614, 2709615, 2709643, 2709644, 2709660, 2709719, 2709777, 2709793, 2709929, 900004001, 900004002, 900004003, 900004004, 900004005, 900004006, 900004007, 900004008, 900004009, 900004010, 900004011, 900004012, 900004013, 900004014, 900004015, 900004016, 900004017, 900004018, 900004019, 900004020, 900004021, 900004022, 900004023, 900004024, 900004025, 900004026, 900004027, 900004028, 900004029, 900004030, 900004031, 900004032, 900004033, 900004034, 900004035, 900004036, 900004037, 900004038, 900004039, 900004040, 900004041, 900004042, 900004043, 900004044, 900004045, 900004046, 900004047, 900004048, 900004049, 900004050, 900004051, 900004052, 900004053, 900004054, 900004055, 900004056, 900004057, 900004058, 900004059, 900004060, 900004061, 900004062, 900004063, 900004064, 900004065, 900004066, 900004067, 900004068, 900004069, 900004070, 900004071, 900004072, 900004073, 900004074, 900004075, 900004076, 900004077, 900004078, 900004079, 900004080, 900004081, 900004082, 900004083, 900004084, 900004085, 900004086, 900004087, 900004088, 900004089, 900004090, 900004091, 900004092, 900004093, 900004094, 900004095, 900004096, 900004097, 900004098, 900004099, 900004100, 900004101, 900004102, 900004103, 900004104, 900004105, 900004106, 900004107, 900004108, 900004109, 900004110, 900004111, 900004112, 900004113, 900004114, 900004115, 900004116, 900004117, 900004118, 900004119, 900004120, 900004121, 900004122, 900004123, 900004124, 900004125, 900004126, 900011002);

-- Đồng bộ link ảnh riêng (không trùng) từ nutrition_foods sang 29 món trong nutrition_guidance_foods
UPDATE nutrition_guidance_foods gf
SET image_url = f.image_url
FROM nutrition_foods f
WHERE gf.nutrition_food_id = f.id
  AND f.image_url IS NOT NULL;
