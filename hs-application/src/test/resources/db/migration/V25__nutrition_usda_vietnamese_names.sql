-- =====================================================================
-- V25: Tên tiếng Việt cho 5.431 thực phẩm USDA FNDDS
--
-- Quy ước tên của API tra cứu:
--  * USDA:     displayName = name (tên gốc tiếng Anh), localName = name_vi (tên dịch).
--  * Việt Nam: displayName = localName = name_vi.
-- Bản dịch do nhóm dự án làm theo một bảng thuật ngữ chung (NFS = "loại chung", fat added =
-- "có thêm chất béo"...), không phải bản dịch chính thức của USDA; tên gốc vẫn là chuẩn.
-- search_vi là cột sinh tự động từ name_vi (V23) nên món USDA tìm được bằng tiếng Việt
-- có dấu hoặc không dấu mà không cần đổi index.
-- Mỗi dòng ghi kèm tên gốc ở chú thích để dễ soát.
-- =====================================================================

UPDATE nutrition_foods f SET name_vi = t.name_vi, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:usda-name-vi-v25'
FROM (VALUES
    ('11100000', 'Sữa, loại chung'), -- Milk, NFS
    ('11111000', 'Sữa nguyên kem'), -- Milk, whole
    ('11112110', 'Sữa giảm béo (2%)'), -- Milk, reduced fat (2%)
    ('11112210', 'Sữa ít béo (1%)'), -- Milk, low fat (1%)
    ('11113000', 'Sữa không béo (tách béo)'), -- Milk, fat free (skim)
    ('11114300', 'Sữa không lactose, ít béo (1%)'), -- Milk, lactose free, low fat (1%)
    ('11114320', 'Sữa không lactose, không béo (tách béo)'), -- Milk, lactose free, fat free (skim)
    ('11114330', 'Sữa không lactose, giảm béo (2%)'), -- Milk, lactose free, reduced fat (2%)
    ('11114350', 'Sữa không lactose, nguyên kem'), -- Milk, lactose free, whole
    ('11115300', 'Sữa bơ (buttermilk)'), -- Buttermilk
    ('11115400', 'Sữa kefir'), -- Kefir
    ('11116000', 'Sữa dê'), -- Goat milk
    ('11120000', 'Sữa bột không béo, đã pha'), -- Milk, dry, reconstituted, nonfat
    ('11121100', 'Sữa bột nguyên kem, đã pha'), -- Milk, dry, reconstituted, whole
    ('11210050', 'Sữa cô đặc không đường (evaporated), không rõ hàm lượng chất béo'), -- Milk, evaporated, NS as to fat content
    ('11211050', 'Sữa cô đặc không đường (evaporated), nguyên kem'), -- Milk, evaporated, whole
    ('11211400', 'Sữa cô đặc không đường (evaporated), giảm béo (2%)'), -- Milk, evaporated, reduced fat (2%)
    ('11212050', 'Sữa cô đặc không đường (evaporated), không béo (tách béo)'), -- Milk, evaporated, fat free (skim)
    ('11220000', 'Sữa đặc có đường'), -- Milk, condensed, sweetened
    ('11300100', 'Sữa thực vật, loại chung'), -- Non-dairy milk, NFS
    ('11320000', 'Sữa đậu nành, có đường'), -- Soy milk, sweetened
    ('11320100', 'Sữa đậu nành, không đường'), -- Soy milk, unsweetened
    ('11321000', 'Sữa đậu nành vị sô-cô-la'), -- Soy milk, chocolate
    ('11350000', 'Sữa hạnh nhân, có đường'), -- Almond milk, sweetened
    ('11350010', 'Sữa hạnh nhân vị sô-cô-la'), -- Almond milk, chocolate
    ('11350020', 'Sữa hạnh nhân, không đường'), -- Almond milk, unsweetened
    ('11350040', 'Sữa hạnh nhân, loại chung'), -- Almond milk, NFS
    ('11360000', 'Sữa gạo'), -- Rice milk
    ('11360200', 'Sữa yến mạch'), -- Oat milk
    ('11370000', 'Sữa dừa'), -- Coconut milk
    ('11400000', 'Sữa chua, loại chung'), -- Yogurt, NFS
    ('11400010', 'Sữa chua Hy Lạp, không rõ loại sữa hay hương vị'), -- Yogurt, Greek, NS as to type of milk or flavor
    ('11410000', 'Sữa chua, không rõ loại sữa hay hương vị'), -- Yogurt, NS as to type of milk or flavor
    ('11411010', 'Sữa chua không hương vị, không rõ loại sữa'), -- Yogurt, NS as to type of milk, plain
    ('11411100', 'Sữa chua từ sữa nguyên kem, không hương vị'), -- Yogurt, whole milk, plain
    ('11411200', 'Sữa chua từ sữa ít béo, không hương vị'), -- Yogurt, low fat milk, plain
    ('11411300', 'Sữa chua từ sữa không béo, không hương vị'), -- Yogurt, nonfat milk, plain
    ('11411390', 'Sữa chua Hy Lạp không hương vị, không rõ loại sữa'), -- Yogurt, Greek, NS as to type of milk, plain
    ('11411400', 'Sữa chua Hy Lạp từ sữa nguyên kem, không hương vị'), -- Yogurt, Greek, whole milk, plain
    ('11411410', 'Sữa chua Hy Lạp từ sữa ít béo, không hương vị'), -- Yogurt, Greek, low fat milk, plain
    ('11411420', 'Sữa chua Hy Lạp từ sữa không béo, không hương vị'), -- Yogurt, Greek, nonfat milk, plain
    ('11430000', 'Sữa chua trái cây, không rõ loại sữa'), -- Yogurt, NS as to type of milk, fruit
    ('11431000', 'Sữa chua trái cây từ sữa nguyên kem'), -- Yogurt, whole milk, fruit
    ('11432000', 'Sữa chua trái cây từ sữa ít béo'), -- Yogurt, low fat milk, fruit
    ('11433000', 'Sữa chua trái cây từ sữa không béo'), -- Yogurt, nonfat milk, fruit
    ('11433990', 'Sữa chua Hy Lạp trái cây, không rõ loại sữa'), -- Yogurt, Greek, NS as to type of milk, fruit
    ('11434000', 'Sữa chua Hy Lạp trái cây từ sữa nguyên kem'), -- Yogurt, Greek, whole milk, fruit
    ('11434010', 'Sữa chua Hy Lạp trái cây từ sữa ít béo'), -- Yogurt, Greek, low fat milk, fruit
    ('11434020', 'Sữa chua Hy Lạp trái cây từ sữa không béo'), -- Yogurt, Greek, nonfat milk, fruit
    ('11434090', 'Sữa chua hương vị khác ngoài trái cây, không rõ loại sữa'), -- Yogurt, NS as to type of milk, flavors other than fruit
    ('11434100', 'Sữa chua hương vị khác ngoài trái cây, từ sữa nguyên kem'), -- Yogurt, whole milk, flavors other than fruit
    ('11434200', 'Sữa chua hương vị khác ngoài trái cây, từ sữa ít béo'), -- Yogurt, low fat milk, flavors other than fruit
    ('11434300', 'Sữa chua hương vị khác ngoài trái cây, từ sữa không béo'), -- Yogurt, nonfat milk, flavors other than fruit
    ('11435000', 'Sữa chua Hy Lạp hương vị khác ngoài trái cây, không rõ loại sữa'), -- Yogurt, Greek, NS as to type of milk, flavors other than fruit
    ('11435010', 'Sữa chua Hy Lạp hương vị khác ngoài trái cây, từ sữa nguyên kem'), -- Yogurt, Greek, whole milk, flavors other than fruit
    ('11435020', 'Sữa chua Hy Lạp hương vị khác ngoài trái cây, từ sữa ít béo'), -- Yogurt, Greek, low fat milk, flavors other than fruit
    ('11435030', 'Sữa chua Hy Lạp hương vị khác ngoài trái cây, từ sữa không béo'), -- Yogurt, Greek, nonfat milk, flavors other than fruit
    ('11435100', 'Sữa chua Hy Lạp với yến mạch'), -- Yogurt, Greek, with oats
    ('11436000', 'Sữa chua uống'), -- Yogurt, liquid
    ('11436100', 'Sữa chua dạng tuýp'), -- Yogurt tube
    ('11440010', 'Sốt chấm chipotle, nền sữa chua'), -- Chipotle dip, yogurt based
    ('11440030', 'Sốt chấm hành tây, nền sữa chua'), -- Onion dip, yogurt based
    ('11440040', 'Sốt chấm ranch, nền sữa chua'), -- Ranch dip, yogurt based
    ('11440050', 'Sốt chấm rau chân vịt, nền sữa chua'), -- Spinach dip, yogurt based
    ('11440060', 'Sốt chấm tzatziki'), -- Tzatziki dip
    ('11440070', 'Sốt chấm rau củ, nền sữa chua'), -- Vegetable dip, yogurt based
    ('11446000', 'Sữa chua parfait với trái cây'), -- Yogurt parfait, with fruit
    ('11459990', 'Sữa chua đông lạnh (frozen yogurt), loại chung'), -- Frozen yogurt, NFS
    ('11460000', 'Sữa chua đông lạnh vị vani'), -- Frozen yogurt, vanilla
    ('11460100', 'Sữa chua đông lạnh vị sô-cô-la'), -- Frozen yogurt, chocolate
    ('11460500', 'Sữa chua đông lạnh dạng mềm (soft serve), vị vani'), -- Frozen yogurt, soft serve, vanilla
    ('11460510', 'Sữa chua đông lạnh dạng mềm (soft serve), vị sô-cô-la'), -- Frozen yogurt, soft serve, chocolate
    ('11461200', 'Bánh kẹp sữa chua đông lạnh'), -- Frozen yogurt sandwich
    ('11461210', 'Sữa chua đông lạnh dạng que, vị vani'), -- Frozen yogurt bar, vanilla
    ('11461220', 'Sữa chua đông lạnh dạng que, vị sô-cô-la'), -- Frozen yogurt bar, chocolate
    ('11461250', 'Ốc quế sữa chua đông lạnh, vị sô-cô-la'), -- Frozen yogurt cone, chocolate
    ('11461260', 'Ốc quế sữa chua đông lạnh, vị vani'), -- Frozen yogurt cone, vanilla
    ('11461300', 'Ốc quế sữa chua đông lạnh, vị vani, vỏ bánh waffle'), -- Frozen yogurt cone, vanilla, waffle cone
    ('11461320', 'Ốc quế sữa chua đông lạnh, vị sô-cô-la, vỏ bánh waffle'), -- Frozen yogurt cone, chocolate, waffle cone
    ('11480000', 'Thức ăn cho trẻ nhỏ, loại chung'), -- Baby Toddler food, NFS
    ('11480010', 'Sữa chua cho trẻ nhỏ, không hương vị'), -- Baby Toddler yogurt, plain
    ('11480100', 'Sữa chua cho trẻ nhỏ, có trái cây'), -- Baby Toddler yogurt, with fruit
    ('11511000', 'Sữa sô-cô-la, loại chung'), -- Chocolate milk, NFS
    ('11511100', 'Sữa sô-cô-la nguyên kem'), -- Chocolate milk, whole
    ('11511200', 'Sữa sô-cô-la giảm béo (2%)'), -- Chocolate milk, reduced fat (2%)
    ('11511300', 'Sữa sô-cô-la không béo (tách béo)'), -- Chocolate milk, fat free (skim)
    ('11511400', 'Sữa sô-cô-la ít béo (1%)'), -- Chocolate milk, low fat (1%)
    ('11511550', 'Sữa sô-cô-la giảm đường, loại chung'), -- Chocolate milk, reduced sugar, NFS
    ('11512005', 'Sô-cô-la nóng/ca cao, loại chung'), -- Hot chocolate / cocoa, NFS
    ('11512010', 'Sô-cô-la nóng/ca cao, pha với sữa nguyên kem hoặc sữa giảm béo (2%)'), -- Hot chocolate / cocoa, made with whole or reduced fat (2%) milk
    ('11512020', 'Sô-cô-la nóng/ca cao, pha với sữa ít béo (1%) hoặc sữa không béo (tách béo)'), -- Hot chocolate / cocoa, made with lowfat (1%) or fat free (skim) milk
    ('11512030', 'Sô-cô-la nóng/ca cao, pha với sữa thực vật'), -- Hot chocolate / cocoa, made with non-dairy milk
    ('11512100', 'Sô-cô-la nóng/ca cao, có kem tươi đánh bông'), -- Hot chocolate / cocoa, with whipped cream
    ('11513380', 'Sữa sô-cô-la Nesquik, không rõ loại sữa'), -- Chocolate milk, Nesquik, NS as to type of milk
    ('11513385', 'Sữa sô-cô-la Nesquik, pha với sữa thực vật'), -- Chocolate milk, Nesquik, made with non-dairy milk
    ('11513801', 'Sữa sô-cô-la giảm đường, nguyên kem'), -- Chocolate milk, reduced sugar, whole
    ('11513802', 'Sữa sô-cô-la giảm đường, giảm béo (2%)'), -- Chocolate milk, reduced sugar, reduced fat (2%)
    ('11513803', 'Sữa sô-cô-la giảm đường, ít béo (1%)'), -- Chocolate milk, reduced sugar, low fat (1%)
    ('11513804', 'Sữa sô-cô-la giảm đường, không béo (tách béo)'), -- Chocolate milk, reduced sugar, fat free (skim)
    ('11514100', 'Sô-cô-la nóng/ca cao từ bột pha sẵn, pha với nước'), -- Hot chocolate / cocoa, dry mix, made with water
    ('11514110', 'Sô-cô-la nóng/ca cao từ bột pha sẵn, pha với sữa nguyên kem hoặc sữa giảm béo (2%)'), -- Hot chocolate / cocoa, dry mix, made with whole or reduced fat (2%) milk
    ('11514130', 'Sô-cô-la nóng/ca cao từ bột pha sẵn, pha với sữa ít béo (1%) hoặc sữa không béo (tách béo)'), -- Hot chocolate / cocoa, dry mix, made with lowfat (1%) or fat free (skim) milk
    ('11514150', 'Sô-cô-la nóng/ca cao từ bột pha sẵn, pha với sữa thực vật'), -- Hot chocolate / cocoa, dry mix , made with non-dairy milk
    ('11514160', 'Sô-cô-la nóng/ca cao từ bột pha sẵn, pha với nước, có kem tươi đánh bông'), -- Hot chocolate / cocoa, dry mix, made with water, with whipped cream
    ('11514310', 'Sô-cô-la nóng/ca cao từ bột pha sẵn, giảm đường, pha với nước'), -- Hot chocolate / cocoa, dry mix, reduced sugar, made with water
    ('11514320', 'Sô-cô-la nóng/ca cao từ bột pha sẵn, giảm đường, pha với sữa nguyên kem hoặc sữa giảm béo (2%)'), -- Hot chocolate / cocoa, dry mix, reduced sugar, made with whole or reduced fat (2%) milk
    ('11514325', 'Sô-cô-la nóng/ca cao giảm đường, pha với sữa thực vật'), -- Hot chocolate / cocoa, reduced sugar, made with non-dairy milk
    ('11514330', 'Sô-cô-la nóng/ca cao giảm đường, pha với sữa nguyên kem hoặc sữa giảm béo (2%)'), -- Hot chocolate / cocoa, reduced sugar, made with whole or reduced fat (2%) milk
    ('11514340', 'Sô-cô-la nóng/ca cao từ bột pha sẵn, giảm đường, pha với sữa ít béo (1%) hoặc sữa không béo (tách béo)'), -- Hot chocolate / cocoa, dry mix, reduced sugar, made with lowfat (1%) or fat free (skim) milk
    ('11514350', 'Sô-cô-la nóng/ca cao giảm đường, pha với sữa ít béo (1%) hoặc sữa không béo (tách béo)'), -- Hot chocolate / cocoa, reduced sugar, made with lowfat (1%) or fat free (skim) milk
    ('11514360', 'Sô-cô-la nóng/ca cao từ bột pha sẵn, giảm đường, pha với sữa thực vật'), -- Hot chocolate / cocoa, dry mix, reduced sugar, made with non-dairy milk
    ('11519040', 'Sữa dâu, loại chung'), -- Strawberry milk, NFS
    ('11519050', 'Sữa dâu nguyên kem'), -- Strawberry milk, whole
    ('11519105', 'Sữa dâu giảm béo (2%)'), -- Strawberry milk, reduced fat (2%)
    ('11519200', 'Sữa dâu ít béo (1%)'), -- Strawberry milk, low fat (1%)
    ('11519205', 'Sữa dâu không béo (tách béo)'), -- Strawberry milk, fat free  (skim)
    ('11519210', 'Sữa dâu giảm đường'), -- Strawberry milk, reduced sugar
    ('11526000', 'Sữa mạch nha'), -- Milk, malted
    ('11531000', 'Sữa trứng (eggnog)'), -- Eggnog
    ('11541110', 'Sữa lắc tự làm tại nhà, vị sô-cô-la'), -- Milk shake, home recipe, chocolate
    ('11541120', 'Sữa lắc tự làm tại nhà, vị khác ngoài sô-cô-la'), -- Milk shake, home recipe, flavors other than chocolate
    ('11541130', 'Sữa lắc tự làm tại nhà, vị sô-cô-la, loại nhẹ'), -- Milk shake, home recipe, chocolate, light
    ('11541135', 'Sữa lắc tự làm tại nhà, vị khác ngoài sô-cô-la, loại nhẹ'), -- Milk shake, home recipe, flavors other than chocolate, light
    ('11541400', 'Sữa lắc có mạch nha'), -- Milk shake with malt
    ('11542100', 'Sữa lắc đồ ăn nhanh, vị sô-cô-la'), -- Milk shake, fast food, chocolate
    ('11542200', 'Sữa lắc đồ ăn nhanh, vị khác ngoài sô-cô-la'), -- Milk shake, fast food, flavors other than chocolate
    ('11543000', 'Sữa lắc đóng chai, vị sô-cô-la'), -- Milk shake, bottled, chocolate
    ('11543010', 'Sữa lắc đóng chai, vị khác ngoài sô-cô-la'), -- Milk shake, bottled, flavors other than chocolate
    ('11551050', 'Sinh tố licuado hoặc batido (Mỹ Latinh)'), -- Licuado or Batido
    ('11553100', 'Sinh tố trái cây, loại chung'), -- Fruit smoothie, NFS
    ('11553110', 'Sinh tố trái cây, từ trái cây nguyên quả và sữa'), -- Fruit smoothie, with whole fruit and dairy
    ('11553120', 'Sinh tố trái cây, từ trái cây nguyên quả và sữa, bổ sung protein'), -- Fruit smoothie, with whole fruit and dairy, added protein
    ('11553130', 'Đồ uống sinh tố nước ép trái cây, có sữa'), -- Fruit smoothie juice drink, with dairy
    ('11560000', 'Đồ uống sữa sô-cô-la'), -- Chocolate milk drink
    ('11710000', 'Sữa công thức cho trẻ sơ sinh, loại chung'), -- Infant formula, NFS
    ('11710030', 'Sữa công thức cho trẻ sơ sinh Similac, loại chung'), -- Infant formula, Similac, NFS
    ('11710051', 'Sữa công thức cho trẻ sơ sinh Similac Alimentum, dạng nước pha sẵn'), -- Infant formula, Similac Alimentum, ready-to-feed
    ('11710056', 'Sữa công thức cho trẻ sơ sinh Similac Alimentum, dạng bột, pha với nước'), -- Infant formula, Similac Alimentum, powder, made with water
    ('11710351', 'Sữa công thức cho trẻ sơ sinh Similac Advance, dạng nước pha sẵn'), -- Infant formula, Similac Advance, ready-to-feed
    ('11710357', 'Sữa công thức cho trẻ sơ sinh Similac Advance, dạng bột, pha với nước máy'), -- Infant formula, Similac Advance, powder, made with tap water
    ('11710358', 'Sữa công thức cho trẻ sơ sinh Similac Advance, dạng bột, pha với nước đóng chai'), -- Infant formula, Similac Advance, powder, made with bottled water
    ('11710359', 'Sữa công thức cho trẻ sơ sinh Similac Advance, dạng bột, pha với nước dành cho trẻ sơ sinh'), -- Infant formula, Similac Advance, powder, made with baby water
    ('11710371', 'Sữa công thức cho trẻ sơ sinh Similac Sensitive, dạng nước pha sẵn'), -- Infant formula, Similac Sensitive, ready-to-feed
    ('11710377', 'Sữa công thức cho trẻ sơ sinh Similac Sensitive, dạng bột, pha với nước máy'), -- Infant formula, Similac Sensitive, powder, made with tap water
    ('11710378', 'Sữa công thức cho trẻ sơ sinh Similac Sensitive, dạng bột, pha với nước đóng chai'), -- Infant formula, Similac Sensitive, powder, made with bottled water
    ('11710379', 'Sữa công thức cho trẻ sơ sinh Similac Sensitive, dạng bột, pha với nước dành cho trẻ sơ sinh'), -- Infant formula, Similac Sensitive, powder, made with baby water
    ('11710381', 'Sữa công thức cho trẻ sơ sinh Similac for Spit-Up (chống trớ), dạng nước pha sẵn'), -- Infant formula, Similac for Spit-Up, ready-to-feed
    ('11710383', 'Sữa công thức cho trẻ sơ sinh Similac for Spit-Up (chống trớ), dạng bột, pha với nước'), -- Infant formula, Similac for Spit-Up, powder, made with water
    ('11710481', 'Sữa công thức cho trẻ tập đi Similac Go and Grow'), -- Toddler formula, Similac Go and Grow
    ('11710605', 'Sữa công thức cho trẻ sơ sinh Enfamil, loại chung'), -- Infant formula, Enfamil, NFS
    ('11710631', 'Sữa công thức cho trẻ sơ sinh Enfamil Infant, dạng nước pha sẵn'), -- Infant formula, Enfamil Infant, ready-to-feed
    ('11710637', 'Sữa công thức cho trẻ sơ sinh Enfamil Infant, dạng bột, pha với nước máy'), -- Infant formula, Enfamil Infant, powder, made with tap water
    ('11710638', 'Sữa công thức cho trẻ sơ sinh Enfamil Infant, dạng bột, pha với nước đóng chai'), -- Infant formula, Enfamil Infant, powder, made with bottled water
    ('11710639', 'Sữa công thức cho trẻ sơ sinh Enfamil Infant, dạng bột, pha với nước dành cho trẻ sơ sinh'), -- Infant formula, Enfamil Infant, powder, made with baby water
    ('11710661', 'Sữa công thức cho trẻ sơ sinh Enfamil AR, dạng nước pha sẵn'), -- Infant formula, Enfamil AR, ready-to-feed
    ('11710669', 'Sữa công thức cho trẻ sơ sinh Enfamil AR, dạng bột, pha với nước'), -- Infant formula, Enfamil AR, powder, made with water
    ('11710671', 'Sữa công thức cho trẻ sơ sinh Enfamil Gentlease, dạng nước pha sẵn'), -- Infant formula, Enfamil Gentlease, ready-to-feed
    ('11710677', 'Sữa công thức cho trẻ sơ sinh Enfamil Gentlease, dạng bột, pha với nước máy'), -- Infant formula, Enfamil Gentlease, powder, made with tap water
    ('11710678', 'Sữa công thức cho trẻ sơ sinh Enfamil Gentlease, dạng bột, pha với nước đóng chai'), -- Infant formula, Enfamil Gentlease, powder, made with bottled water
    ('11710679', 'Sữa công thức cho trẻ sơ sinh Enfamil Gentlease, dạng bột, pha với nước dành cho trẻ sơ sinh'), -- Infant formula, Enfamil Gentlease, powder, made with baby water
    ('11710689', 'Sữa công thức cho trẻ tập đi Enfamil Enfagrow'), -- Toddler formula, Enfamil Enfagrow
    ('11710801', 'Sữa công thức cho trẻ tập đi PediaSure'), -- Toddler formula, PediaSure
    ('11710807', 'Sữa công thức cho trẻ tập đi Nido Kinder'), -- Toddler formula, Nido Kinder
    ('11710810', 'Sữa công thức cho trẻ tập đi, nhãn hàng siêu thị, giai đoạn đầu hoặc giai đoạn tiếp theo'), -- Toddler formula, store brand, beginning or next stage
    ('11710815', 'Sữa công thức cho trẻ tập đi, nhãn hàng siêu thị, dạng sữa lắc dinh dưỡng nhi khoa'), -- Toddler formula, store brand, pediatric shake
    ('11710905', 'Sữa công thức cho trẻ sơ sinh Gerber, loại chung'), -- Infant formula, Gerber, NFS
    ('11710911', 'Sữa công thức cho trẻ sơ sinh Gerber Good Start Gentle, giai đoạn 1, dạng nước pha sẵn'), -- Infant formula, Gerber Good Start Gentle, Stage 1, ready-to-feed
    ('11710917', 'Sữa công thức cho trẻ sơ sinh Gerber Good Start Gentle, giai đoạn 1, dạng bột, pha với nước máy'), -- Infant formula, Gerber Good Start Gentle, Stage 1, powder, made with tap water
    ('11710918', 'Sữa công thức cho trẻ sơ sinh Gerber Good Start Gentle, giai đoạn 1, dạng bột, pha với nước đóng chai'), -- Infant formula, Gerber Good Start Gentle, Stage 1, powder, made with bottled water
    ('11710919', 'Sữa công thức cho trẻ sơ sinh Gerber Good Start Gentle, giai đoạn 1, dạng bột, pha với nước dành cho trẻ sơ sinh'), -- Infant formula, Gerber Good Start Gentle, Stage 1, powder, made with baby water
    ('11710921', 'Sữa công thức cho trẻ sơ sinh Gerber Good Start Gentle, giai đoạn 2'), -- Infant formula, Gerber Good Start Gentle, Stage 2
    ('11710930', 'Sữa công thức cho trẻ tập đi Gerber Good Start, giai đoạn 3'), -- Toddler formula, Gerber Good Start, Stage 3
    ('11710935', 'Sữa công thức cho trẻ sinh non, dạng bột, pha với nước'), -- Infant formula, premature, powder, made with water
    ('11710945', 'Sữa công thức cho trẻ sinh non, dạng nước pha sẵn'), -- Infant formula, premature, ready-to-feed
    ('11710954', 'Sữa công thức hữu cơ cho trẻ sơ sinh, dạng bột, pha với nước'), -- Infant formula, organic, powder, made with water
    ('11710955', 'Sữa công thức hữu cơ cho trẻ sơ sinh, dạng nước pha sẵn'), -- Infant formula, organic, ready-to-feed
    ('11710967', 'Sữa công thức cho trẻ sơ sinh, nhãn hàng siêu thị, loại advantage hoặc tender, dạng bột, pha với nước máy'), -- Infant formula, store brand, advantage or tender, powder, made with tap water
    ('11710968', 'Sữa công thức cho trẻ sơ sinh, nhãn hàng siêu thị, loại advantage hoặc tender, dạng bột, pha với nước đóng chai'), -- Infant formula, store brand, advantage or tender, powder, made with bottled water
    ('11710969', 'Sữa công thức cho trẻ sơ sinh, nhãn hàng siêu thị, loại advantage hoặc tender, dạng bột, pha với nước dành cho trẻ sơ sinh'), -- Infant formula, store brand, advantage or tender, powder, made with baby water
    ('11710970', 'Sữa công thức cho trẻ sơ sinh, nhãn hàng siêu thị, loại gentle hoặc sensitivity (dịu nhẹ/nhạy cảm)'), -- Infant formula, store brand, gentle or sensitivity
    ('11710980', 'Sữa công thức cho trẻ sơ sinh, nhãn hàng siêu thị, có bổ sung bột gạo'), -- Infant formula, store brand, added rice
    ('11720311', 'Sữa công thức cho trẻ sơ sinh Enfamil ProSobee, dạng nước pha sẵn'), -- Infant formula, Enfamil ProSobee, ready-to-feed
    ('11720317', 'Sữa công thức cho trẻ sơ sinh Enfamil ProSobee, dạng bột, pha với nước máy'), -- Infant formula, Enfamil ProSobee, powder, made with tap water
    ('11720318', 'Sữa công thức cho trẻ sơ sinh Enfamil ProSobee, dạng bột, pha với nước đóng chai'), -- Infant formula, Enfamil ProSobee, powder, made with bottled water
    ('11720319', 'Sữa công thức cho trẻ sơ sinh Enfamil ProSobee, dạng bột, pha với nước dành cho trẻ sơ sinh'), -- Infant formula, Enfamil ProSobee, powder, made with baby water
    ('11720411', 'Sữa công thức cho trẻ sơ sinh Similac Isomil Soy, dạng nước pha sẵn'), -- Infant formula, Similac Isomil Soy, ready-to-feed
    ('11720417', 'Sữa công thức cho trẻ sơ sinh Similac Isomil Soy, dạng bột, pha với nước máy'), -- Infant formula, Similac Isomil Soy, powder, made with tap water
    ('11720418', 'Sữa công thức cho trẻ sơ sinh Similac Isomil Soy, dạng bột, pha với nước đóng chai'), -- Infant formula, Similac Isomil Soy, powder, made with bottled water
    ('11720419', 'Sữa công thức cho trẻ sơ sinh Similac Isomil Soy, dạng bột, pha với nước dành cho trẻ sơ sinh'), -- Infant formula, Similac Isomil Soy, powder, made with baby water
    ('11720430', 'Sữa công thức cho trẻ sơ sinh Similac for Diarrhea (cho trẻ tiêu chảy)'), -- Infant formula, Similac for Diarrhea
    ('11720611', 'Sữa công thức cho trẻ sơ sinh Gerber Good Start Soy, giai đoạn 1, dạng nước pha sẵn'), -- Infant formula, Gerber Good Start Soy, Stage 1, ready-to-feed
    ('11720617', 'Sữa công thức cho trẻ sơ sinh Gerber Good Start Soy, giai đoạn 1, dạng bột, pha với nước máy'), -- Infant formula, Gerber Good Start Soy, Stage 1, powder, made with tap water
    ('11720618', 'Sữa công thức cho trẻ sơ sinh Gerber Good Start Soy, giai đoạn 1, dạng bột, pha với nước đóng chai'), -- Infant formula, Gerber Good Start Soy, Stage 1, powder, made with bottled water
    ('11720619', 'Sữa công thức cho trẻ sơ sinh Gerber Good Start Soy, giai đoạn 1, dạng bột, pha với nước dành cho trẻ sơ sinh'), -- Infant formula, Gerber Good Start Soy, Stage 1, powder, made with baby water
    ('11720809', 'Sữa công thức cho trẻ sơ sinh, nhãn hàng siêu thị, từ đậu nành'), -- Infant formula, store brand, soy
    ('11740311', 'Sữa công thức cho trẻ sơ sinh Enfamil Nutramigen, dạng nước pha sẵn'), -- Infant formula, Enfamil Nutramigen, ready-to-feed
    ('11740313', 'Sữa công thức cho trẻ sơ sinh Enfamil Nutramigen, dạng bột, pha với nước'), -- Infant formula, Enfamil Nutramigen, powder, made with water
    ('11740401', 'Sữa công thức cho trẻ sơ sinh Enfamil Pregestimil, dạng nước pha sẵn'), -- Infant formula, Enfamil Pregestimil, ready-to-feed
    ('11740403', 'Sữa công thức cho trẻ sơ sinh Enfamil Pregestimil, dạng bột, pha với nước'), -- Infant formula, Enfamil Pregestimil, powder, made with water
    ('11740405', 'Sữa công thức cho trẻ sơ sinh, gốc axit amin'), -- Infant formula, amino acids
    ('11740560', 'Sữa công thức cho trẻ sơ sinh, ít sắt'), -- Infant formula, low iron
    ('11810000', 'Sữa bột, chưa pha'), -- Milk, dry, not reconstituted
    ('11825000', 'Váng sữa whey ngọt, dạng bột'), -- Whey, sweet, dry
    ('11830150', 'Bột ca cao, chưa pha'), -- Cocoa powder, not reconstituted
    ('11830160', 'Bột pha đồ uống sô-cô-la, chưa pha'), -- Chocolate beverage powder, dry mix, not reconstituted
    ('11830165', 'Bột pha đồ uống sô-cô-la loại nhẹ, chưa pha'), -- Chocolate beverage powder, light, dry mix, not reconstituted
    ('11830260', 'Bột sữa mạch nha, chưa pha'), -- Milk, malted, dry mix, not reconstituted
    ('11830400', 'Bột pha đồ uống vị dâu, chưa pha'), -- Strawberry beverage powder, dry mix, not reconstituted
    ('12100100', 'Kem sữa, không rõ loại nhẹ, loại béo hay half and half'), -- Cream, NS as to light, heavy, or half and half
    ('12110100', 'Kem sữa loại nhẹ (light cream)'), -- Cream, light
    ('12120100', 'Kem sữa half and half (nửa sữa nửa kem)'), -- Cream, half and half
    ('12120106', 'Kem sữa half and half, có hương vị'), -- Cream, half and half, flavored
    ('12120110', 'Kem sữa half and half, không béo'), -- Cream, half and half, fat free
    ('12130100', 'Kem sữa béo (heavy cream)'), -- Cream, heavy
    ('12140000', 'Kem tươi đánh bông'), -- Cream, whipped
    ('12200100', 'Kem pha cà phê (creamer), loại chung'), -- Coffee creamer, NFS
    ('12210200', 'Kem pha cà phê, dạng lỏng'), -- Coffee creamer, liquid
    ('12210210', 'Kem pha cà phê, dạng lỏng, có hương vị'), -- Coffee creamer, liquid, flavored
    ('12210260', 'Kem pha cà phê, dạng lỏng, không béo'), -- Coffee creamer, liquid, fat free
    ('12210270', 'Kem pha cà phê, dạng lỏng, không béo, có hương vị'), -- Coffee creamer, liquid, fat free, flavored
    ('12210280', 'Kem pha cà phê, dạng lỏng, không béo, không đường, có hương vị'), -- Coffee creamer, liquid, fat free, sugar free, flavored
    ('12210310', 'Kem pha cà phê, dạng lỏng, không đường, có hương vị'), -- Coffee creamer, liquid, sugar free, flavored
    ('12210400', 'Kem pha cà phê, dạng bột'), -- Coffee creamer, powder
    ('12210420', 'Kem pha cà phê, dạng bột, có hương vị'), -- Coffee creamer, powder, flavored
    ('12210430', 'Kem pha cà phê, dạng bột, không béo'), -- Coffee creamer, powder, fat free
    ('12210505', 'Kem pha cà phê, dạng bột, không đường, có hương vị'), -- Coffee creamer,powder, sugar free, flavored
    ('12210520', 'Kem pha cà phê từ đậu nành, dạng lỏng'), -- Coffee creamer, soy, liquid
    ('12220200', 'Kem phủ đánh bông (whipped topping)'), -- Whipped topping
    ('12220270', 'Kem phủ đánh bông (whipped topping), không béo'), -- Whipped topping, fat free
    ('12220280', 'Kem phủ đánh bông (whipped topping), không đường'), -- Whipped topping, sugar free
    ('12310100', 'Kem chua, loại thường'), -- Sour cream, regular
    ('12310350', 'Kem chua, loại nhẹ'), -- Sour cream, light
    ('12310370', 'Kem chua, không béo'), -- Sour cream, fat free
    ('12320100', 'Kem chua nhân tạo'), -- Sour cream, imitation
    ('12350010', 'Sốt chấm, loại chung'), -- Dip, NFS
    ('12350200', 'Sốt chấm chipotle, loại thường'), -- Chipotle dip, regular
    ('12350205', 'Sốt chấm chipotle, loại nhẹ'), -- Chipotle dip, light
    ('12350220', 'Sốt chấm hành tây, loại thường'), -- Onion dip, regular
    ('12350225', 'Sốt chấm hành tây, loại nhẹ'), -- Onion dip, light
    ('12350230', 'Sốt chấm ranch, loại thường'), -- Ranch dip, regular
    ('12350235', 'Sốt chấm ranch, loại nhẹ'), -- Ranch dip, light
    ('12350240', 'Sốt chấm rau chân vịt, loại thường'), -- Spinach dip, regular
    ('12350245', 'Sốt chấm rau chân vịt, loại nhẹ'), -- Spinach dip, light
    ('12350250', 'Sốt chấm rau củ, loại thường'), -- Vegetable dip, regular
    ('12350255', 'Sốt chấm rau củ, loại nhẹ'), -- Vegetable dip, light
    ('13110000', 'Kem lạnh, loại chung'), -- Ice cream, NFS
    ('13110100', 'Kem lạnh vị vani'), -- Ice cream, vanilla
    ('13110102', 'Kem lạnh vị vani, có thêm nguyên liệu khác'), -- Ice cream, vanilla, with additional ingredients
    ('13110110', 'Kem lạnh vị sô-cô-la'), -- Ice cream, chocolate
    ('13110112', 'Kem lạnh vị sô-cô-la, có thêm nguyên liệu khác'), -- Ice cream, chocolate, with additional ingredients
    ('13110200', 'Kem lạnh dạng mềm (soft serve), vị vani'), -- Ice cream, soft serve, vanilla
    ('13110210', 'Kem lạnh dạng mềm (soft serve), vị sô-cô-la'), -- Ice cream, soft serve, chocolate
    ('13110460', 'Kem gelato vị vani'), -- Gelato, vanilla
    ('13110470', 'Kem gelato vị sô-cô-la'), -- Gelato, chocolate
    ('13120050', 'Kem que vị vani'), -- Ice cream bar, vanilla
    ('13120100', 'Kem que vị vani, phủ sô-cô-la'), -- Ice cream bar, vanilla, chocolate coated
    ('13120110', 'Kem que dạng thanh kẹo'), -- Ice cream candy bar
    ('13120140', 'Kem que vị sô-cô-la'), -- Ice cream bar, chocolate
    ('13120500', 'Bánh kẹp kem lạnh, vị vani'), -- Ice cream sandwich, vanilla
    ('13120510', 'Bánh kẹp kem lạnh, vị sô-cô-la'), -- Ice cream sandwich, chocolate
    ('13120550', 'Bánh quy kẹp kem lạnh'), -- Ice cream cookie sandwich
    ('13120730', 'Kem ốc quế múc, vị vani'), -- Ice cream cone, scooped, vanilla
    ('13120735', 'Kem ốc quế múc, vị vani, vỏ bánh waffle'), -- Ice cream cone, scooped, vanilla, waffle cone
    ('13120740', 'Kem ốc quế, loại chung'), -- Ice cream cone, NFS
    ('13120770', 'Kem ốc quế múc, vị sô-cô-la'), -- Ice cream cone, scooped, chocolate
    ('13120775', 'Kem ốc quế múc, vị sô-cô-la, vỏ bánh waffle'), -- Ice cream cone, scooped, chocolate, waffle cone
    ('13120782', 'Kem ốc quế dạng mềm (soft serve), vị vani'), -- Ice cream cone, soft serve, vanilla
    ('13120784', 'Kem ốc quế dạng mềm (soft serve), vị sô-cô-la'), -- Ice cream cone, soft serve, chocolate
    ('13120786', 'Kem ốc quế dạng mềm (soft serve), vị vani, vỏ bánh waffle'), -- Ice cream cone, soft serve, vanilla, waffle cone
    ('13120788', 'Kem ốc quế dạng mềm (soft serve), vị sô-cô-la, vỏ bánh waffle'), -- Ice cream cone, soft serve, chocolate, waffle cone
    ('13120790', 'Kem ốc quế vị vani, đóng gói sẵn'), -- Ice cream cone, vanilla, prepackaged
    ('13120792', 'Kem ốc quế vị sô-cô-la, đóng gói sẵn'), -- Ice cream cone, chocolate, prepackaged
    ('13120800', 'Nước ngọt có ga thả kem (ice cream soda), vị khác ngoài sô-cô-la'), -- Ice cream soda, flavors other than chocolate
    ('13120810', 'Nước ngọt có ga thả kem (ice cream soda), vị sô-cô-la'), -- Ice cream soda, chocolate
    ('13121000', 'Kem sundae, loại chung'), -- Ice cream sundae, NFS
    ('13121100', 'Kem sundae phủ trái cây'), -- Ice cream sundae, fruit topping
    ('13121120', 'Kem chuối (banana split)'), -- Banana split
    ('13121300', 'Kem sundae phủ sốt sô-cô-la nóng (hot fudge)'), -- Ice cream sundae, hot fudge topping
    ('13121400', 'Kem sundae phủ sốt caramel'), -- Ice cream sundae, caramel topping
    ('13126000', 'Kem lạnh chiên'), -- Ice cream, fried
    ('13130100', 'Kem lạnh loại nhẹ, loại chung'), -- Light ice cream, NFS
    ('13130300', 'Kem lạnh loại nhẹ, vị vani'), -- Light ice cream, vanilla
    ('13130310', 'Kem lạnh loại nhẹ, vị sô-cô-la'), -- Light ice cream, chocolate
    ('13130700', 'Kem mềm (soft serve) trộn kẹo hoặc bánh quy, đồ ăn nhanh'), -- Soft serve, blended with candy or cookies, from fast food
    ('13135000', 'Bánh kẹp kem lạnh loại nhẹ, vị vani'), -- Light ice cream sandwich, vanilla
    ('13135010', 'Bánh kẹp kem lạnh loại nhẹ, vị sô-cô-la'), -- Light ice cream sandwich, chocolate
    ('13140000', 'Kem que loại nhẹ, vị vani'), -- Light ice cream bar, vanilla
    ('13140100', 'Kem que loại nhẹ, vị vani, phủ sô-cô-la'), -- Light ice cream bar, vanilla, chocolate coated
    ('13140115', 'Kem que loại nhẹ, vị sô-cô-la'), -- Light ice cream bar, chocolate
    ('13140700', 'Kem que Creamsicle (vỏ đá trái cây, nhân kem vani)'), -- Creamsicle
    ('13140900', 'Kem que sô-cô-la Fudgesicle'), -- Fudgesicle
    ('13142100', 'Kem ốc quế loại nhẹ, vị vani, đóng gói sẵn'), -- Light ice cream cone, vanilla, prepackaged
    ('13142110', 'Kem ốc quế loại nhẹ, vị sô-cô-la, đóng gói sẵn'), -- Light ice cream cone, chocolate, prepackaged
    ('13150000', 'Kem sherbet, mọi hương vị'), -- Sherbet, all flavors
    ('13161600', 'Kem que sô-cô-la Fudgesicle, loại nhẹ'), -- Fudgesicle, light
    ('13200110', 'Bánh pudding sô-cô-la, loại chung'), -- Pudding, chocolate, NFS
    ('13210110', 'Bánh pudding bánh mì'), -- Pudding, bread
    ('13210280', 'Bánh pudding vị khác ngoài sô-cô-la, loại chung'), -- Pudding, flavors other than chocolate, NFS
    ('13210300', 'Kem trứng (custard)'), -- Custard
    ('13210350', 'Bánh flan'), -- Flan
    ('13210370', 'Bánh crème brûlée'), -- Creme brulee
    ('13210410', 'Bánh pudding gạo'), -- Pudding, rice
    ('13210450', 'Bánh pudding firni (Ấn Độ)'), -- Firni, Indian pudding
    ('13210520', 'Bánh pudding bột sắn (tapioca), từ bột pha sẵn'), -- Pudding, tapioca, made from dry mix
    ('13220110', 'Bánh pudding vị khác ngoài sô-cô-la, từ bột pha sẵn'), -- Pudding, flavors other than chocolate, made from dry mix
    ('13220120', 'Bánh pudding sô-cô-la, từ bột pha sẵn'), -- Pudding, chocolate, made from dry mix
    ('13220210', 'Bánh pudding vị khác ngoài sô-cô-la, từ bột pha sẵn, không đường'), -- Pudding, flavors other than chocolate, made from dry mix, sugar free
    ('13220220', 'Bánh pudding sô-cô-la, từ bột pha sẵn, không đường'), -- Pudding, chocolate, made from dry mix, sugar free
    ('13230110', 'Bánh pudding vị khác ngoài sô-cô-la, ăn liền'), -- Pudding, flavors other than chocolate, ready-to-eat
    ('13230120', 'Bánh pudding vị khác ngoài sô-cô-la, ăn liền, không đường'), -- Pudding, flavors other than chocolate, ready-to-eat, sugar free
    ('13230130', 'Bánh pudding sô-cô-la, ăn liền'), -- Pudding, chocolate, ready-to-eat
    ('13230140', 'Bánh pudding sô-cô-la, ăn liền, không đường'), -- Pudding, chocolate, ready-to-eat, sugar free
    ('13230500', 'Bánh pudding bột sắn (tapioca), ăn liền'), -- Pudding, tapioca, ready-to-eat
    ('13241000', 'Bánh pudding chuối'), -- Banana pudding
    ('13250000', 'Bánh mousse'), -- Mousse
    ('13252200', 'Sốt sữa caramel (dulce de leche)'), -- Dulce de leche
    ('13252500', 'Bánh barfi hoặc burfi (món tráng miệng Ấn Độ)'), -- Barfi or Burfi, Indian dessert
    ('13252590', 'Bánh trifle'), -- Trifle
    ('13252600', 'Bánh tiramisu'), -- Tiramisu
    ('13411000', 'Sốt trắng hoặc nước sốt gravy'), -- White sauce or gravy
    ('14010000', 'Phô mai, loại chung'), -- Cheese, NFS
    ('14101010', 'Phô mai xanh hoặc Roquefort'), -- Cheese, Blue or Roquefort
    ('14102010', 'Phô mai Brick'), -- Cheese, Brick
    ('14103010', 'Phô mai Camembert'), -- Cheese, Camembert
    ('14103020', 'Phô mai Brie'), -- Cheese, Brie
    ('14104100', 'Phô mai Cheddar'), -- Cheese, Cheddar
    ('14104110', 'Phô mai Cheddar giảm béo'), -- Cheese, Cheddar, reduced fat
    ('14104115', 'Phô mai Cheddar không béo'), -- Cheese, Cheddar, nonfat or fat free
    ('14104200', 'Phô mai Colby'), -- Cheese, Colby
    ('14104250', 'Phô mai Colby Jack'), -- Cheese, Colby Jack
    ('14104400', 'Phô mai Feta'), -- Cheese, Feta
    ('14104600', 'Phô mai Fontina'), -- Cheese, Fontina
    ('14104700', 'Phô mai dê'), -- Cheese, goat
    ('14105010', 'Phô mai Gouda hoặc Edam'), -- Cheese, Gouda or Edam
    ('14105200', 'Phô mai Gruyère'), -- Cheese, Gruyere
    ('14106010', 'Phô mai Limburger'), -- Cheese, Limburger
    ('14106200', 'Phô mai Monterey'), -- Cheese, Monterey
    ('14106500', 'Phô mai Monterey giảm béo'), -- Cheese, Monterey, reduced fat
    ('14107010', 'Phô mai Mozzarella, loại chung'), -- Cheese, Mozzarella, NFS
    ('14107030', 'Phô mai Mozzarella, sữa tách béo một phần'), -- Cheese, Mozzarella, part skim
    ('14107040', 'Phô mai Mozzarella giảm muối'), -- Cheese, Mozzarella, reduced sodium
    ('14107060', 'Phô mai Mozzarella không béo'), -- Cheese, Mozzarella, nonfat or fat free
    ('14107200', 'Phô mai Muenster'), -- Cheese, Muenster
    ('14107250', 'Phô mai Muenster giảm béo'), -- Cheese, Muenster, reduced fat
    ('14108010', 'Phô mai Parmesan bào khô'), -- Cheese, Parmesan, dry grated
    ('14108015', 'Phô mai Parmesan bào khô, giảm béo'), -- Cheese, Parmesan, dry grated, reduced fat
    ('14108020', 'Phô mai Parmesan cứng'), -- Cheese, Parmesan, hard
    ('14108060', 'Phô mai Parmesan bào khô, không béo'), -- Cheese, Parmesan, dry grated, fat free
    ('14108200', 'Phô mai Port du Salut'), -- Cheese, Port du Salut
    ('14108400', 'Phô mai Provolone'), -- Cheese, Provolone
    ('14108420', 'Phô mai Provolone giảm béo'), -- Cheese, provolone, reduced fat
    ('14109010', 'Phô mai Thụy Sĩ'), -- Cheese, Swiss
    ('14109020', 'Phô mai Thụy Sĩ giảm muối'), -- Cheese, Swiss, reduced sodium
    ('14109030', 'Phô mai Thụy Sĩ giảm béo'), -- Cheese, Swiss, reduced fat
    ('14109040', 'Phô mai Thụy Sĩ không béo'), -- Cheese, Swiss, nonfat or fat free
    ('14110010', 'Phô mai Cheddar giảm muối'), -- Cheese, Cheddar, reduced sodium
    ('14110050', 'Phô mai paneer (Ấn Độ)'), -- Cheese, paneer
    ('14120010', 'Phô mai hỗn hợp kiểu Mexico'), -- Cheese, Mexican blend
    ('14120020', 'Phô mai hỗn hợp kiểu Mexico, giảm béo'), -- Cheese, Mexican blend, reduced fat
    ('14131000', 'Phô mai Queso Añejo (phô mai Mexico ủ lâu)'), -- Queso Anejo, aged Mexican cheese
    ('14131500', 'Phô mai Queso Asadero'), -- Queso Asadero
    ('14133000', 'Phô mai tươi Queso Fresco'), -- Queso Fresco
    ('14134000', 'Phô mai Queso Cotija'), -- Queso cotija
    ('14200100', 'Phô mai cottage, loại chung'), -- Cheese, cottage, NFS
    ('14201010', 'Phô mai cottage trộn kem, hạt to hoặc hạt nhỏ'), -- Cheese, cottage, creamed, large or small curd
    ('14201200', 'Phô mai cottage kiểu nông trại (farmer''s)'), -- Cottage cheese, farmer's
    ('14201500', 'Phô mai Ricotta'), -- Cheese, Ricotta
    ('14202010', 'Phô mai cottage với trái cây'), -- Cheese, cottage, with fruit
    ('14202020', 'Phô mai cottage với rau'), -- Cheese, cottage, with vegetables
    ('14203010', 'Phô mai cottage hạt khô'), -- Cheese, cottage, dry curd
    ('14203020', 'Phô mai cottage hạt khô, có muối'), -- Cheese, cottage, salted, dry curd
    ('14203510', 'Phô mai trắng kiểu Puerto Rico'), -- Puerto Rican white cheese
    ('14204010', 'Phô mai cottage ít béo'), -- Cheese, cottage, low fat
    ('14204020', 'Phô mai cottage ít béo, với trái cây'), -- Cheese, cottage, lowfat, with fruit
    ('14206010', 'Phô mai cottage ít béo, ít muối'), -- Cheese, cottage, lowfat, low sodium
    ('14207010', 'Phô mai cottage ít béo, giảm lactose'), -- Cheese, cottage, lowfat, lactose reduced
    ('14301010', 'Phô mai kem, loại thường, không hương vị'), -- Cream cheese, regular, plain
    ('14301100', 'Phô mai kem, loại thường, có hương vị'), -- Cream cheese, regular, flavored
    ('14303010', 'Phô mai kem, loại nhẹ'), -- Cream cheese, light
    ('14410100', 'Phô mai hỗn hợp kiểu Mỹ và Thụy Sĩ'), -- Cheese, American and Swiss blends
    ('14410110', 'Phô mai kiểu Mỹ (American cheese)'), -- Cheese, American
    ('14410120', 'Phô mai kiểu Mỹ, giảm béo'), -- Cheese, American, reduced fat
    ('14410130', 'Phô mai kiểu Mỹ, không béo'), -- Cheese, American, nonfat or fat free
    ('14410210', 'Phô mai kiểu Mỹ, giảm muối'), -- Cheese, American, reduced sodium
    ('14410330', 'Phô mai phết, nền phô mai kiểu Mỹ hoặc Cheddar, giảm béo'), -- Cheese spread, American or Cheddar cheese base, reduced fat
    ('14410380', 'Phô mai kem, không béo'), -- Cream cheese, fat free
    ('14410500', 'Phô mai chế biến dạng thực phẩm (processed cheese food)'), -- Cheese, processed cheese food
    ('14410600', 'Phô mai chế biến, có rau'), -- Cheese, processed, with vegetables
    ('14410620', 'Phô mai có rượu vang'), -- Cheese,  with wine
    ('14420100', 'Phô mai phết, nền phô mai kiểu Mỹ hoặc Cheddar'), -- Cheese spread, American or Cheddar cheese base
    ('14420160', 'Phô mai phết, nền phô mai Thụy Sĩ'), -- Cheese spread, Swiss cheese base
    ('14420200', 'Phô mai phết, từ phô mai kem'), -- Cheese spread, cream cheese
    ('14420300', 'Phô mai phết, dạng bình xịt'), -- Cheese spread, pressurized can
    ('14502000', 'Phô mai nhân tạo'), -- Imitation cheese
    ('14610200', 'Phô mai cottage với thạch (gelatin)'), -- Cheese, cottage cheese, with gelatin dessert
    ('14610210', 'Phô mai cottage với thạch (gelatin) và trái cây'), -- Cheese, cottage cheese, with gelatin dessert and fruit
    ('14610250', 'Phô mai cottage với thạch (gelatin) và rau'), -- Cheese, cottage cheese, with gelatin dessert and vegetables
    ('14610520', 'Phô mai viên (cheese ball)'), -- Cheese ball
    ('14620110', 'Sốt chấm atisô'), -- Artichoke dip
    ('14620115', 'Sốt chấm rau chân vịt và atisô'), -- Spinach and artichoke dip
    ('14620130', 'Sốt dip trộn hải sản'), -- Seafood dip
    ('14620150', 'Sốt chấm phô mai có ớt'), -- Cheese dip with chili pepper
    ('14620200', 'Sốt chấm phô mai'), -- Cheese dip
    ('14620300', 'Phần nhân phủ của pizza phô mai'), -- Topping from cheese pizza
    ('14620310', 'Phần nhân phủ của pizza rau'), -- Topping from vegetable pizza
    ('14620320', 'Phần nhân phủ của pizza thịt'), -- Topping from meat pizza
    ('14620330', 'Phần nhân phủ của pizza thịt và rau'), -- Topping from meat and vegetable pizza
    ('14630100', 'Phô mai fondue'), -- Cheese fondue
    ('14630200', 'Bánh soufflé phô mai'), -- Cheese souffle
    ('14630300', 'Món Welsh rarebit (bánh mì phủ sốt phô mai)'), -- Welsh rarebit
    ('14640000', 'Bánh mì kẹp phô mai, loại chung'), -- Cheese sandwich, NFS
    ('14640002', 'Bánh mì kẹp phô mai, phô mai kiểu Mỹ, bánh mì trắng'), -- Cheese sandwich, American cheese, on white bread
    ('14640004', 'Bánh mì kẹp phô mai, phô mai kiểu Mỹ, bánh mì lúa mì'), -- Cheese sandwich, American cheese, on wheat bread
    ('14640008', 'Bánh mì kẹp phô mai, phô mai Cheddar, bánh mì trắng'), -- Cheese sandwich, cheddar cheese, on white bread
    ('14640010', 'Bánh mì kẹp phô mai, phô mai Cheddar, bánh mì lúa mì'), -- Cheese sandwich, cheddar cheese, on wheat bread
    ('14640014', 'Bánh mì kẹp phô mai, phô mai giảm béo, bánh mì trắng'), -- Cheese sandwich, reduced fat cheese, on white bread
    ('14640016', 'Bánh mì kẹp phô mai, phô mai giảm béo, bánh mì lúa mì'), -- Cheese sandwich, reduced fat cheese, on wheat bread
    ('14640100', 'Bánh mì kẹp phô mai nướng, loại chung'), -- Grilled cheese sandwich, NFS
    ('14640105', 'Bánh mì kẹp phô mai nướng, phô mai kiểu Mỹ, bánh mì trắng'), -- Grilled cheese sandwich, American cheese, on white bread
    ('14640110', 'Bánh mì kẹp phô mai nướng, phô mai kiểu Mỹ, bánh mì lúa mì'), -- Grilled cheese sandwich, American cheese, on wheat bread
    ('14640125', 'Bánh mì kẹp phô mai nướng, phô mai Cheddar, bánh mì trắng'), -- Grilled cheese sandwich, cheddar cheese, on white bread
    ('14640130', 'Bánh mì kẹp phô mai nướng, phô mai Cheddar, bánh mì lúa mì'), -- Grilled cheese sandwich, cheddar cheese, on wheat bread
    ('14640155', 'Bánh mì kẹp phô mai nướng, phô mai giảm béo, bánh mì trắng'), -- Grilled cheese sandwich, reduced fat cheese, on white bread
    ('14640160', 'Bánh mì kẹp phô mai nướng, phô mai giảm béo, bánh mì lúa mì'), -- Grilled cheese sandwich, reduced fat cheese, on wheat bread
    ('14650100', 'Sốt phô mai'), -- Cheese sauce
    ('14650160', 'Sốt Alfredo'), -- Alfredo sauce
    ('14650165', 'Sốt Alfredo có thêm rau'), -- Alfredo sauce with added vegetables
    ('14650170', 'Sốt Alfredo có thịt'), -- Alfredo sauce with meat
    ('14650175', 'Sốt Alfredo có thịt và thêm rau'), -- Alfredo sauce with meat and added vegetables
    ('14650180', 'Sốt Alfredo có thịt gia cầm'), -- Alfredo sauce with poultry
    ('14650185', 'Sốt Alfredo có thịt gia cầm và thêm rau'), -- Alfredo sauce with poultry and added vegetables
    ('14650190', 'Sốt Alfredo có hải sản'), -- Alfredo sauce with seafood
    ('14650195', 'Sốt Alfredo có hải sản và thêm rau'), -- Alfredo sauce with seafood and added vegetables
    ('14660200', 'Phô mai que Mozzarella tẩm bột chiên xù, nướng lò hoặc chiên'), -- Mozzarella sticks, breaded, baked, or fried
    ('14670000', 'Phô mai Mozzarella, cà chua và húng quế, sốt trộn dầu giấm'), -- Mozzarella cheese, tomato, and basil, with oil and vinegar dressing
    ('20000000', 'Thịt, loại chung'), -- Meat, NFS
    ('20000200', 'Thịt xay, loại chung'), -- Meat, ground, NFS
    ('20000300', 'Thịt cho trẻ nhỏ, loại chung'), -- Baby Toddler meat, NFS
    ('21000100', 'Thịt bò, loại chung'), -- Beef, NFS
    ('21001000', 'Bít tết, không rõ loại thịt, không rõ có ăn mỡ hay không'), -- Steak, NS as to type of meat, NS as to fat eaten
    ('21101000', 'Bít tết bò, loại chung'), -- Beef, steak, NFS
    ('21101140', 'Bít tết bò, thịt vai (chuck)'), -- Beef, steak, chuck
    ('21101150', 'Bít tết bò dần mềm (cube steak)'), -- Beef, steak, cube
    ('21101160', 'Bít tết bò, thăn bụng (flank)'), -- Beef, steak, flank
    ('21101170', 'Bít tết bò thăn lưng (ribeye), không rõ có ăn mỡ hay không'), -- Beef, steak, ribeye, NS as to fat eaten
    ('21101180', 'Bít tết bò thăn lưng (ribeye), ăn cả nạc và mỡ'), -- Beef, steak, ribeye, lean and fat eaten
    ('21101190', 'Bít tết bò thăn lưng (ribeye), chỉ ăn phần nạc'), -- Beef, steak, ribeye, lean only eaten
    ('21102140', 'Bít tết bò, thịt đùi (round)'), -- Beef, steak, round
    ('21102150', 'Bít tết bò thăn hông (sirloin), không rõ có ăn mỡ hay không'), -- Beef, steak, sirloin, NS as to fat eaten
    ('21102160', 'Bít tết bò thăn hông (sirloin), ăn cả nạc và mỡ'), -- Beef, steak, sirloin, lean and fat eaten
    ('21102170', 'Bít tết bò thăn hông (sirloin), chỉ ăn phần nạc'), -- Beef, steak, sirloin, lean only eaten
    ('21102180', 'Bít tết bò thăn ngoại (strip), không rõ có ăn mỡ hay không'), -- Beef, steak, strip, NS as to fat eaten
    ('21102190', 'Bít tết bò thăn ngoại (strip), ăn cả nạc và mỡ'), -- Beef, steak, strip, lean and fat eaten
    ('21103140', 'Bít tết bò thăn ngoại (strip), chỉ ăn phần nạc'), -- Beef, steak, strip, lean only eaten
    ('21103150', 'Bít tết bò T-bone, không rõ có ăn mỡ hay không'), -- Beef, steak, T-bone, NS as to fat eaten
    ('21103160', 'Bít tết bò T-bone, ăn cả nạc và mỡ'), -- Beef, steak, T-bone,  lean and fat eaten
    ('21103170', 'Bít tết bò T-bone, chỉ ăn phần nạc'), -- Beef, steak, T-bone, lean only eaten
    ('21103180', 'Bít tết bò thăn nội (tenderloin)'), -- Beef, steak, tenderloin
    ('21104110', 'Bít tết bò chiên tẩm bột kiểu đồng quê (country fried)'), -- Beef, steak, country fried
    ('21301000', 'Đuôi bò'), -- Beef, oxtails
    ('21302000', 'Xương cổ bò'), -- Beef, neck bones
    ('21304200', 'Sườn bò (short ribs)'), -- Beef, shortribs
    ('21305000', 'Đầu bò'), -- Beef, cow head
    ('21401000', 'Thịt bò quay'), -- Beef, roast
    ('21407000', 'Thịt bò om nồi (pot roast)'), -- Beef, pot roast
    ('21410000', 'Thịt bò hầm'), -- Beef, stew meat
    ('21416000', 'Thịt bò muối (corned beef)'), -- Beef, corned
    ('21417100', 'Thịt bò ức (brisket)'), -- Beef, brisket
    ('21420100', 'Thịt bò lát kẹp bánh mì (sandwich steak)'), -- Beef, sandwich steak
    ('21500000', 'Thịt bò xay, sống'), -- Beef, ground, raw
    ('21500100', 'Thịt bò xay'), -- Beef, ground
    ('21500310', 'Thịt bò xay, miếng chả (patty)'), -- Beef, ground, patty
    ('21601000', 'Thịt xông khói bò, nấu chín'), -- Beef, bacon, cooked
    ('21601010', 'Thịt xông khói bò, giảm muối, nấu chín'), -- Beef, bacon, reduced sodium, cooked
    ('21602000', 'Thịt bò khô thái lát, chưa nấu'), -- Beef, dried, chipped, uncooked
    ('21602010', 'Thịt bò khô thái lát, nấu với chất béo'), -- Beef, dried, chipped, cooked in fat
    ('21602100', 'Bò khô (jerky)'), -- Beef jerky
    ('21701010', 'Thịt bò cho trẻ nhỏ'), -- Baby Toddler beef
    ('22000100', 'Thịt lợn, loại chung'), -- Pork, NFS
    ('22002000', 'Thịt lợn xay'), -- Pork, ground
    ('22002050', 'Thịt lợn carnitas (Mexico)'), -- Pork, carnitas
    ('22002800', 'Thịt lợn khô (jerky)'), -- Pork jerky
    ('22101000', 'Cốt lết lợn, không rõ có ăn mỡ hay không'), -- Pork, chop, NS as to fat eaten
    ('22101010', 'Cốt lết lợn, ăn cả nạc và mỡ'), -- Pork, chop, lean and fat eaten
    ('22101020', 'Cốt lết lợn, chỉ ăn phần nạc'), -- Pork, chop, lean only eaten
    ('22101300', 'Cốt lết lợn tẩm bột, không rõ có ăn mỡ hay không'), -- Pork, chop, coated, NS as to fat eaten
    ('22101310', 'Cốt lết lợn tẩm bột, ăn cả nạc và mỡ'), -- Pork, chop, coated, lean and fat eaten
    ('22101320', 'Cốt lết lợn tẩm bột, chỉ ăn phần nạc'), -- Pork, chop, coated, lean only eaten
    ('22101330', 'Cốt lết lợn nhồi'), -- Pork, chop, stuffed
    ('22201100', 'Bít tết lợn, không rõ có ăn mỡ hay không'), -- Pork, steak, NS as to fat eaten
    ('22201110', 'Bít tết lợn, ăn cả nạc và mỡ'), -- Pork, steak, lean and fat eaten
    ('22201120', 'Bít tết lợn, chỉ ăn phần nạc'), -- Pork, steak, lean only eaten
    ('22201400', 'Bít tết lợn tẩm bột'), -- Pork, steak, coated
    ('22210300', 'Thăn nội lợn (tenderloin)'), -- Pork, tenderloin
    ('22311000', 'Giăm bông'), -- Ham
    ('22311450', 'Giăm bông prosciutto (Ý)'), -- Ham, prosciutto
    ('22311500', 'Giăm bông đóng hộp'), -- Ham, canned
    ('22321110', 'Giăm bông xay'), -- Ham, ground
    ('22400100', 'Thịt lợn quay'), -- Pork, roast
    ('22431000', 'Thịt lợn cuộn'), -- Pork, roll
    ('22501010', 'Thịt xông khói kiểu Canada, nấu chín'), -- Canadian bacon, cooked
    ('22600100', 'Thịt xông khói, không rõ loại thịt, nấu chín'), -- Bacon, NS as to type of meat, cooked
    ('22600110', 'Thịt xông khói, không rõ loại thịt, giảm muối, nấu chín'), -- Bacon, NS as to type of meat, reduced sodium, cooked
    ('22600200', 'Thịt lợn xông khói, không rõ loại tươi, hun khói hay ướp muối, nấu chín'), -- Pork bacon, NS as to fresh, smoked or cured, cooked
    ('22600210', 'Thịt lợn xông khói, không rõ loại tươi, hun khói hay ướp muối, giảm muối, nấu chín'), -- Pork bacon, NS as to fresh, smoked or cured, reduced sodium, cooked
    ('22601000', 'Thịt lợn xông khói, hun khói hoặc ướp muối, nấu chín'), -- Pork bacon, smoked or cured, cooked
    ('22601040', 'Thịt xông khói hoặc thịt ba chỉ lợn, loại tươi, nấu chín'), -- Bacon or side pork, fresh, cooked
    ('22602010', 'Thịt lợn xông khói, hun khói hoặc ướp muối, giảm muối, nấu chín'), -- Pork bacon, smoked or cured, reduced sodium, cooked
    ('22621100', 'Mỡ lưng lợn, nấu chín'), -- Fat back, cooked
    ('22700000', 'Sườn, loại chung'), -- Ribs, NFS
    ('22701000', 'Sườn lợn'), -- Pork, ribs
    ('22704010', 'Tóp mỡ lợn'), -- Pork, cracklings
    ('22705010', 'Tai lợn'), -- Pork, ears
    ('22706010', 'Xương lợn'), -- Pork, bones
    ('22707010', 'Chân giò lợn'), -- Pork, pig's feet
    ('22707020', 'Chân giò lợn ngâm chua'), -- Pork, pig's feet, pickled
    ('22708010', 'Khuỷu giò lợn (ham hock)'), -- Pork, ham hocks
    ('22708020', 'Thịt ba chỉ lợn'), -- Pork, belly
    ('22709010', 'Da lợn chiên giòn'), -- Pork skin rinds
    ('22810010', 'Giăm bông cho trẻ nhỏ'), -- Baby Toddler ham
    ('22820000', 'Xúc xích que cho trẻ nhỏ'), -- Baby Toddler meat stick
    ('23000100', 'Thịt cừu, không rõ phần thịt'), -- Lamb, NS as to cut
    ('23101000', 'Cốt lết cừu'), -- Lamb, chop
    ('23132000', 'Thịt cừu xay'), -- Lamb, ground
    ('23150100', 'Thịt dê'), -- Goat
    ('23201010', 'Cốt lết bê'), -- Veal, chop
    ('23220010', 'Thịt bê xay'), -- Veal, ground
    ('23220020', 'Đùi gà giả (thịt lợn/bê xiên que tạo hình đùi gà)'), -- Mock chicken legs
    ('23310000', 'Thịt thỏ'), -- Rabbit
    ('23321000', 'Thịt nai, loại chung'), -- Venison, NFS
    ('23321200', 'Bít tết thịt nai'), -- Venison, steak
    ('23321900', 'Thịt nai khô (jerky)'), -- Venison/deer jerky
    ('23322100', 'Xúc xích thịt nai'), -- Venison sausage
    ('23323100', 'Thịt nai sừng tấm'), -- Moose
    ('23323500', 'Thịt gấu'), -- Bear
    ('23324100', 'Thịt tuần lộc'), -- Caribou
    ('23326100', 'Thịt bò rừng bison'), -- Bison
    ('23331100', 'Thịt sóc đất (groundhog)'), -- Groundhog
    ('23332100', 'Thịt thú có túi opossum'), -- Opossum
    ('23333100', 'Thịt sóc'), -- Squirrel
    ('23334100', 'Thịt hải ly'), -- Beaver
    ('23335100', 'Thịt gấu mèo'), -- Raccoon
    ('23340100', 'Thịt tatu'), -- Armadillo
    ('23345100', 'Thịt lợn rừng'), -- Wild pig
    ('23350100', 'Thịt đà điểu'), -- Ostrich
    ('24100000', 'Gà, không rõ phần và cách chế biến, không rõ có ăn da hay không'), -- Chicken, NS as to part and cooking method, NS as to skin eaten
    ('24100010', 'Gà, không rõ phần và cách chế biến, ăn cả da'), -- Chicken, NS as to part and cooking method, skin eaten
    ('24100020', 'Gà, không rõ phần và cách chế biến, bỏ da'), -- Chicken, NS as to part and cooking method, skin not eaten
    ('24102000', 'Gà nướng lò, nướng hoặc quay, không rõ phần, không rõ có ăn da hay không'), -- Chicken, NS as to part, baked, broiled, or roasted, NS as to skin eaten
    ('24102010', 'Gà nướng lò, nướng hoặc quay, không rõ phần, ăn cả da'), -- Chicken, NS as to part, baked, broiled, or roasted, skin eaten
    ('24102020', 'Gà nướng lò, nướng hoặc quay, không rõ phần, bỏ da'), -- Chicken, NS as to part, baked, broiled, or roasted, skin not eaten
    ('24102050', 'Gà quay xiên (rotisserie), không rõ phần, không rõ có ăn da hay không'), -- Chicken, NS as to part, rotisserie, NS as to skin eaten
    ('24102060', 'Gà quay xiên (rotisserie), không rõ phần, ăn cả da'), -- Chicken, NS as to part, rotisserie, skin eaten
    ('24102070', 'Gà quay xiên (rotisserie), không rõ phần, bỏ da'), -- Chicken, NS as to part, rotisserie, skin not eaten
    ('24103000', 'Gà hầm, không rõ phần, không rõ có ăn da hay không'), -- Chicken, NS as to part, stewed, NS as to skin eaten
    ('24103010', 'Gà hầm, không rõ phần, ăn cả da'), -- Chicken, NS as to part, stewed, skin eaten
    ('24103020', 'Gà hầm, không rõ phần, bỏ da'), -- Chicken, NS as to part, stewed, skin not eaten
    ('24103050', 'Gà nướng vỉ không sốt, không rõ phần, không rõ có ăn da hay không'), -- Chicken, NS as to part, grilled without sauce, NS as to skin eaten
    ('24103055', 'Gà nướng vỉ không sốt, không rõ phần, ăn cả da'), -- Chicken, NS as to part, grilled without sauce, skin eaten
    ('24103060', 'Gà nướng vỉ không sốt, không rõ phần, bỏ da'), -- Chicken, NS as to part, grilled without sauce, skin not eaten
    ('24103070', 'Gà nướng vỉ có sốt, không rõ phần, không rõ có ăn da hay không'), -- Chicken, NS as to part, grilled with sauce, NS as to skin eaten
    ('24103075', 'Gà nướng vỉ có sốt, không rõ phần, ăn cả da'), -- Chicken, NS as to part, grilled with sauce, skin eaten
    ('24103080', 'Gà nướng vỉ có sốt, không rõ phần, bỏ da'), -- Chicken, NS as to part, grilled with sauce, skin not eaten
    ('24104049', 'Gà xào, không rõ phần, ăn cả da'), -- Chicken, NS as to part, sauteed, skin eaten
    ('24104051', 'Gà xào, không rõ phần, bỏ da'), -- Chicken, NS as to part, sauteed, skin not eaten
    ('24107070', 'Gà chiên tẩm bột, không rõ phần, ăn cả da/lớp tẩm bột'), -- Chicken, NS as to part, fried, coated, skin / coating eaten
    ('24107071', 'Gà chiên tẩm bột, không rõ phần, bỏ da/lớp tẩm bột'), -- Chicken, NS as to part, fried, coated, skin / coating not eaten
    ('24107080', 'Gà nướng lò tẩm bột, không rõ phần, ăn cả da/lớp tẩm bột'), -- Chicken, NS as to part, baked, coated, skin / coating eaten
    ('24107081', 'Gà nướng lò tẩm bột, không rõ phần, bỏ da/lớp tẩm bột'), -- Chicken, NS as to part, baked, coated, skin / coating not eaten
    ('24120110', 'Ức gà, không rõ cách chế biến, ăn cả da'), -- Chicken breast, NS as to cooking method, skin eaten
    ('24120120', 'Ức gà, không rõ cách chế biến, bỏ da'), -- Chicken breast, NS as to cooking method, skin not eaten
    ('24122130', 'Ức gà nướng lò, nướng hoặc quay, ăn cả da, chế biến từ gà sống'), -- Chicken breast, baked, broiled, or roasted, skin eaten, from raw
    ('24122131', 'Ức gà nướng lò, nướng hoặc quay, bỏ da, chế biến từ gà sống'), -- Chicken breast, baked, broiled, or roasted, skin not eaten, from raw
    ('24122140', 'Ức gà nướng lò hoặc nướng, ăn cả da, từ loại nấu sẵn'), -- Chicken breast, baked or broiled, skin eaten, from pre-cooked
    ('24122141', 'Ức gà nướng lò hoặc nướng, bỏ da, từ loại nấu sẵn'), -- Chicken breast, baked or broiled, skin not eaten, from pre-cooked
    ('24122150', 'Ức gà nướng lò hoặc nướng, ăn cả da, từ đồ ăn nhanh/nhà hàng'), -- Chicken breast, baked or broiled, skin eaten, from fast food / restaurant
    ('24122151', 'Ức gà nướng lò hoặc nướng, bỏ da, từ đồ ăn nhanh/nhà hàng'), -- Chicken breast, baked or broiled, skin not eaten, from fast food / restaurant
    ('24122160', 'Ức gà ướp, nướng lò, nướng hoặc quay, ăn cả da, chế biến từ gà sống'), -- Chicken breast, baked, broiled, or roasted with marinade, skin eaten, from raw
    ('24122161', 'Ức gà ướp, nướng lò, nướng hoặc quay, bỏ da, chế biến từ gà sống'), -- Chicken breast, baked, broiled, or roasted with marinade, skin not eaten, from raw
    ('24122170', 'Ức gà quay xiên (rotisserie), ăn cả da'), -- Chicken breast, rotisserie, skin eaten
    ('24122171', 'Ức gà quay xiên (rotisserie), bỏ da'), -- Chicken breast, rotisserie, skin not eaten
    ('24123110', 'Ức gà hầm, ăn cả da'), -- Chicken breast, stewed, skin eaten
    ('24123120', 'Ức gà hầm, bỏ da'), -- Chicken breast, stewed, skin not eaten
    ('24123300', 'Ức gà nướng vỉ không sốt, ăn cả da'), -- Chicken breast, grilled without sauce, skin eaten
    ('24123301', 'Ức gà nướng vỉ không sốt, bỏ da'), -- Chicken breast, grilled without sauce, skin not eaten
    ('24123310', 'Ức gà nướng vỉ có sốt, ăn cả da'), -- Chicken breast, grilled with sauce, skin eaten
    ('24123311', 'Ức gà nướng vỉ có sốt, bỏ da'), -- Chicken breast, grilled with sauce, skin not eaten
    ('24124200', 'Ức gà xào, ăn cả da'), -- Chicken breast, sauteed, skin eaten
    ('24124201', 'Ức gà xào, bỏ da'), -- Chicken breast, sauteed, skin not eaten
    ('24127200', 'Ức gà chiên tẩm bột, ăn cả da/lớp tẩm bột, chế biến từ gà sống'), -- Chicken breast, fried, coated, skin / coating eaten, from raw
    ('24127201', 'Ức gà chiên tẩm bột, bỏ da/lớp tẩm bột, chế biến từ gà sống'), -- Chicken breast, fried, coated, skin / coating not eaten, from raw
    ('24127202', 'Ức gà chiên tẩm bột, lột da trước khi chế biến, ăn cả lớp tẩm bột, chế biến từ gà sống'), -- Chicken breast, fried, coated, prepared skinless, coating eaten, from raw
    ('24127210', 'Ức gà chiên tẩm bột, ăn cả da/lớp tẩm bột, từ loại nấu sẵn'), -- Chicken breast, fried, coated, skin / coating eaten, from pre-cooked
    ('24127211', 'Ức gà chiên tẩm bột, bỏ da/lớp tẩm bột, từ loại nấu sẵn'), -- Chicken breast, fried, coated, skin / coating not eaten, from pre-cooked
    ('24127220', 'Ức gà chiên tẩm bột, ăn cả da/lớp tẩm bột, từ đồ ăn nhanh/nhà hàng'), -- Chicken breast, fried, coated, skin / coating eaten, from fast food / restaurant
    ('24127221', 'Ức gà chiên tẩm bột, bỏ da/lớp tẩm bột, từ đồ ăn nhanh/nhà hàng'), -- Chicken breast, fried, coated, skin / coating not eaten, from fast food / restaurant
    ('24127500', 'Ức gà nướng lò tẩm bột, ăn cả da/lớp tẩm bột'), -- Chicken breast, baked, coated, skin / coating eaten
    ('24127501', 'Ức gà nướng lò tẩm bột, bỏ da/lớp tẩm bột'), -- Chicken breast, baked, coated, skin / coating not eaten
    ('24130210', 'Đùi gà (tỏi và má đùi), không rõ cách chế biến, ăn cả da'), -- Chicken leg, drumstick and thigh, NS as to cooking method, skin eaten
    ('24130220', 'Đùi gà (tỏi và má đùi), không rõ cách chế biến, bỏ da'), -- Chicken leg, drumstick and thigh, NS as to cooking method, skin not eaten
    ('24132230', 'Đùi gà (tỏi và má đùi) nướng lò hoặc nướng, ăn cả da'), -- Chicken leg, drumstick and thigh, baked or broiled, skin eaten
    ('24132231', 'Đùi gà (tỏi và má đùi) nướng lò hoặc nướng, bỏ da'), -- Chicken leg, drumstick and thigh, baked or broiled, skin not eaten
    ('24132240', 'Đùi gà (tỏi và má đùi) quay xiên (rotisserie), ăn cả da'), -- Chicken leg, drumstick and thigh, rotisserie, skin eaten
    ('24132241', 'Đùi gà (tỏi và má đùi) quay xiên (rotisserie), bỏ da'), -- Chicken leg, drumstick and thigh, rotisserie, skin not eaten
    ('24133210', 'Đùi gà (tỏi và má đùi) hầm, ăn cả da'), -- Chicken leg, drumstick and thigh, stewed, skin eaten
    ('24133220', 'Đùi gà (tỏi và má đùi) hầm, bỏ da'), -- Chicken leg, drumstick and thigh, stewed, skin not eaten
    ('24134100', 'Đùi gà (tỏi và má đùi) nướng vỉ không sốt, ăn cả da'), -- Chicken leg, drumstick and thigh, grilled without sauce, skin eaten
    ('24134101', 'Đùi gà (tỏi và má đùi) nướng vỉ không sốt, bỏ da'), -- Chicken leg, drumstick and thigh, grilled without sauce, skin not eaten
    ('24134150', 'Đùi gà (tỏi và má đùi) nướng vỉ có sốt, ăn cả da'), -- Chicken leg, drumstick and thigh, grilled with sauce, skin eaten
    ('24134151', 'Đùi gà (tỏi và má đùi) nướng vỉ có sốt, bỏ da'), -- Chicken leg, drumstick and thigh, grilled with sauce, skin not eaten
    ('24134300', 'Đùi gà (tỏi và má đùi) xào, ăn cả da'), -- Chicken leg, drumstick and thigh, sauteed, skin eaten
    ('24134301', 'Đùi gà (tỏi và má đùi) xào, bỏ da'), -- Chicken leg, drumstick and thigh, sauteed, skin not eaten
    ('24137300', 'Đùi gà (tỏi và má đùi) chiên tẩm bột, ăn cả da/lớp tẩm bột'), -- Chicken leg, drumstick and thigh, fried, coated, skin / coating eaten
    ('24137301', 'Đùi gà (tỏi và má đùi) chiên tẩm bột, bỏ da/lớp tẩm bột'), -- Chicken leg, drumstick and thigh, fried, coated, skin / coating not eaten
    ('24137310', 'Đùi gà (tỏi và má đùi) nướng lò tẩm bột, ăn cả da/lớp tẩm bột'), -- Chicken leg, drumstick and thigh, baked, coated, skin / coating eaten
    ('24137311', 'Đùi gà (tỏi và má đùi) nướng lò tẩm bột, bỏ da/lớp tẩm bột'), -- Chicken leg, drumstick and thigh, baked, coated, skin / coating not eaten
    ('24140210', 'Tỏi gà, không rõ cách chế biến, ăn cả da'), -- Chicken drumstick, NS as to cooking method, skin eaten
    ('24140220', 'Tỏi gà, không rõ cách chế biến, bỏ da'), -- Chicken drumstick, NS as to cooking method, skin not eaten
    ('24142300', 'Tỏi gà nướng lò, nướng hoặc quay, ăn cả da, chế biến từ gà sống'), -- Chicken drumstick, baked, broiled, or roasted, skin eaten, from raw
    ('24142301', 'Tỏi gà nướng lò, nướng hoặc quay, bỏ da, chế biến từ gà sống'), -- Chicken drumstick, baked, broiled, or roasted, skin not eaten, from raw
    ('24142310', 'Tỏi gà nướng lò hoặc nướng, ăn cả da, từ loại nấu sẵn'), -- Chicken drumstick, baked or broiled, skin eaten, from pre-cooked
    ('24142311', 'Tỏi gà nướng lò hoặc nướng, bỏ da, từ loại nấu sẵn'), -- Chicken drumstick, baked or broiled, skin not eaten, from pre-cooked
    ('24142320', 'Tỏi gà nướng lò hoặc nướng, ăn cả da, từ đồ ăn nhanh/nhà hàng'), -- Chicken drumstick, baked or broiled, skin eaten, from fast food / restaurant
    ('24142321', 'Tỏi gà nướng lò hoặc nướng, bỏ da, từ đồ ăn nhanh/nhà hàng'), -- Chicken drumstick, baked or broiled, skin not eaten, from fast food / restaurant
    ('24142400', 'Tỏi gà quay xiên (rotisserie), ăn cả da'), -- Chicken drumstick, rotisserie, skin eaten
    ('24142401', 'Tỏi gà quay xiên (rotisserie), bỏ da'), -- Chicken drumstick, rotisserie, skin not eaten
    ('24142500', 'Tỏi gà nướng vỉ không sốt, ăn cả da'), -- Chicken drumstick, grilled without sauce, skin eaten
    ('24142501', 'Tỏi gà nướng vỉ không sốt, bỏ da'), -- Chicken drumstick, grilled without sauce, skin not eaten
    ('24142510', 'Tỏi gà nướng vỉ có sốt, ăn cả da'), -- Chicken drumstick, grilled with sauce, skin eaten
    ('24142511', 'Tỏi gà nướng vỉ có sốt, bỏ da'), -- Chicken drumstick, grilled with sauce, skin not eaten
    ('24143210', 'Tỏi gà hầm, ăn cả da'), -- Chicken drumstick, stewed, skin eaten
    ('24143220', 'Tỏi gà hầm, bỏ da'), -- Chicken drumstick, stewed, skin not eaten
    ('24144300', 'Tỏi gà xào, ăn cả da'), -- Chicken drumstick, sauteed, skin eaten
    ('24144301', 'Tỏi gà xào, bỏ da'), -- Chicken drumstick, sauteed, skin not eaten
    ('24147300', 'Tỏi gà chiên tẩm bột, ăn cả da/lớp tẩm bột, chế biến từ gà sống'), -- Chicken drumstick, fried, coated, skin / coating eaten, from raw
    ('24147301', 'Tỏi gà chiên tẩm bột, bỏ da/lớp tẩm bột, chế biến từ gà sống'), -- Chicken drumstick, fried, coated, skin / coating not eaten, from raw
    ('24147302', 'Tỏi gà chiên tẩm bột, lột da trước khi chế biến, ăn cả lớp tẩm bột, chế biến từ gà sống'), -- Chicken drumstick, fried, coated, prepared skinless, coating eaten, from raw
    ('24147310', 'Tỏi gà chiên tẩm bột, ăn cả da/lớp tẩm bột, từ loại nấu sẵn'), -- Chicken drumstick, fried, coated, skin / coating eaten, from pre-cooked
    ('24147311', 'Tỏi gà chiên tẩm bột, bỏ da/lớp tẩm bột, từ loại nấu sẵn'), -- Chicken drumstick, fried, coated, skin / coating not eaten, from pre-cooked
    ('24147320', 'Tỏi gà chiên tẩm bột, ăn cả da/lớp tẩm bột, từ đồ ăn nhanh/nhà hàng'), -- Chicken drumstick, fried, coated, skin / coating eaten, from fast food / restaurant
    ('24147321', 'Tỏi gà chiên tẩm bột, bỏ da/lớp tẩm bột, từ đồ ăn nhanh/nhà hàng'), -- Chicken drumstick, fried, coated, skin / coating not eaten, from fast food / restaurant
    ('24147400', 'Tỏi gà nướng lò tẩm bột, ăn cả da/lớp tẩm bột'), -- Chicken drumstick, baked, coated, skin / coating eaten
    ('24147401', 'Tỏi gà nướng lò tẩm bột, bỏ da/lớp tẩm bột'), -- Chicken drumstick, baked, coated, skin / coating not eaten
    ('24150210', 'Má đùi gà, không rõ cách chế biến, ăn cả da'), -- Chicken thigh, NS as to cooking method, skin eaten
    ('24150220', 'Má đùi gà, không rõ cách chế biến, bỏ da'), -- Chicken thigh, NS as to cooking method, skin not eaten
    ('24152230', 'Má đùi gà nướng lò, nướng hoặc quay, ăn cả da, chế biến từ gà sống'), -- Chicken thigh, baked, broiled, or roasted, skin eaten, from raw
    ('24152231', 'Má đùi gà nướng lò, nướng hoặc quay, bỏ da, chế biến từ gà sống'), -- Chicken thigh, baked, broiled, or roasted, skin not eaten, from raw
    ('24152240', 'Má đùi gà nướng lò hoặc nướng, ăn cả da, từ loại nấu sẵn'), -- Chicken thigh, baked or broiled, skin eaten, from pre-cooked
    ('24152241', 'Má đùi gà nướng lò hoặc nướng, bỏ da, từ loại nấu sẵn'), -- Chicken thigh, baked or broiled, skin not eaten, from pre-cooked
    ('24152250', 'Má đùi gà nướng lò hoặc nướng, ăn cả da, từ đồ ăn nhanh/nhà hàng'), -- Chicken thigh, baked or broiled, skin eaten, from fast food / restaurant
    ('24152251', 'Má đùi gà nướng lò hoặc nướng, bỏ da, từ đồ ăn nhanh/nhà hàng'), -- Chicken thigh, baked or broiled, skin not eaten, from fast food / restaurant
    ('24152300', 'Má đùi gà quay xiên (rotisserie), ăn cả da'), -- Chicken thigh, rotisserie, skin eaten
    ('24152301', 'Má đùi gà quay xiên (rotisserie), bỏ da'), -- Chicken thigh, rotisserie, skin not eaten
    ('24153210', 'Má đùi gà hầm, ăn cả da'), -- Chicken thigh, stewed, skin eaten
    ('24153220', 'Má đùi gà hầm, bỏ da'), -- Chicken thigh, stewed, skin not eaten
    ('24154010', 'Má đùi gà nướng vỉ không sốt, ăn cả da'), -- Chicken thigh, grilled without sauce, skin eaten
    ('24154011', 'Má đùi gà nướng vỉ không sốt, bỏ da'), -- Chicken thigh, grilled without sauce, skin not eaten
    ('24154020', 'Má đùi gà nướng vỉ có sốt, ăn cả da'), -- Chicken thigh, grilled with sauce, skin eaten
    ('24154021', 'Má đùi gà nướng vỉ có sốt, bỏ da'), -- Chicken thigh, grilled with sauce, skin not eaten
    ('24154300', 'Má đùi gà xào, ăn cả da'), -- Chicken thigh, sauteed, skin eaten
    ('24154301', 'Má đùi gà xào, bỏ da'), -- Chicken thigh, sauteed, skin not eaten
    ('24157300', 'Má đùi gà chiên tẩm bột, ăn cả da/lớp tẩm bột, chế biến từ gà sống'), -- Chicken thigh, fried, coated, skin / coating eaten, from raw
    ('24157301', 'Má đùi gà chiên tẩm bột, bỏ da/lớp tẩm bột, chế biến từ gà sống'), -- Chicken thigh, fried, coated, skin / coating not eaten, from raw
    ('24157302', 'Má đùi gà chiên tẩm bột, lột da trước khi chế biến, ăn cả lớp tẩm bột, chế biến từ gà sống'), -- Chicken thigh, fried, coated, prepared skinless, coating eaten, from raw
    ('24157310', 'Má đùi gà chiên tẩm bột, ăn cả da/lớp tẩm bột, từ loại nấu sẵn'), -- Chicken thigh, fried, coated, skin / coating eaten, from pre-cooked
    ('24157311', 'Má đùi gà chiên tẩm bột, bỏ da/lớp tẩm bột, từ loại nấu sẵn'), -- Chicken thigh, fried, coated, skin / coating not eaten, from pre-cooked
    ('24157320', 'Má đùi gà chiên tẩm bột, ăn cả da/lớp tẩm bột, từ đồ ăn nhanh'), -- Chicken thigh, fried, coated, skin / coating eaten, from fast food
    ('24157321', 'Má đùi gà chiên tẩm bột, bỏ da/lớp tẩm bột, từ đồ ăn nhanh'), -- Chicken thigh, fried, coated, skin / coating not eaten, from fast food
    ('24157330', 'Má đùi gà chiên tẩm bột, ăn cả da/lớp tẩm bột, từ nhà hàng'), -- Chicken thigh, fried, coated, skin / coating eaten, from restaurant
    ('24157331', 'Má đùi gà chiên tẩm bột, bỏ da/lớp tẩm bột, từ nhà hàng'), -- Chicken thigh, fried, coated, skin / coating not eaten, from restaurant
    ('24157400', 'Má đùi gà nướng lò tẩm bột, ăn cả da/lớp tẩm bột'), -- Chicken thigh, baked, coated, skin / coating eaten
    ('24157401', 'Má đùi gà nướng lò tẩm bột, bỏ da/lớp tẩm bột'), -- Chicken thigh, baked, coated, skin / coating not eaten
    ('24160110', 'Cánh gà, không rõ cách chế biến'), -- Chicken wing, NS as to cooking method
    ('24162130', 'Cánh gà nướng lò, nướng hoặc quay, chế biến từ gà sống'), -- Chicken wing, baked, broiled, or roasted, from raw
    ('24162140', 'Cánh gà nướng lò hoặc nướng, từ loại nấu sẵn'), -- Chicken wing, baked or broiled, from pre-cooked
    ('24162150', 'Cánh gà nướng lò hoặc nướng, từ đồ ăn nhanh/nhà hàng'), -- Chicken wing, baked or broiled, from fast food / restaurant
    ('24162200', 'Cánh gà quay xiên (rotisserie)'), -- Chicken wing, rotisserie
    ('24163110', 'Cánh gà hầm'), -- Chicken wing, stewed
    ('24164000', 'Cánh gà nướng vỉ không sốt'), -- Chicken wing, grilled without sauce
    ('24164010', 'Cánh gà nướng vỉ có sốt'), -- Chicken wing, grilled with sauce
    ('24164200', 'Cánh gà xào'), -- Chicken wing, sauteed
    ('24167200', 'Cánh gà chiên tẩm bột, chế biến từ gà sống'), -- Chicken wing, fried, coated, from raw
    ('24167210', 'Cánh gà chiên tẩm bột, từ loại nấu sẵn'), -- Chicken wing, fried, coated, from pre-cooked
    ('24167220', 'Cánh gà chiên tẩm bột, từ đồ ăn nhanh'), -- Chicken wing, fried, coated, from fast food
    ('24167230', 'Cánh gà chiên tẩm bột, từ nhà hàng'), -- Chicken wing, fried, coated, from restaurant
    ('24167300', 'Cánh gà nướng lò tẩm bột'), -- Chicken wing, baked, coated
    ('24168000', 'Món cánh gà wings với tương ớt, từ đồ ăn nhanh/nhà hàng'), -- Chicken "wings" with hot sauce, from fast food / restaurant
    ('24168001', 'Món cánh gà wings với sốt hoặc gia vị khác, từ đồ ăn nhanh/nhà hàng'), -- Chicken "wings" with other sauces or seasoning, from fast food / restaurant
    ('24168002', 'Món cánh gà wings không sốt, từ đồ ăn nhanh/nhà hàng'), -- Chicken "wings", plain, from fast food / restaurant
    ('24168010', 'Món cánh gà wings với tương ớt, từ loại nấu sẵn'), -- Chicken "wings" with hot sauce, from precooked
    ('24168011', 'Món cánh gà wings với sốt hoặc gia vị khác, từ loại nấu sẵn'), -- Chicken "wings" with other sauces or seasoning, from precooked
    ('24168012', 'Món cánh gà wings không sốt, từ loại nấu sẵn'), -- Chicken "wings", plain, from precooked
    ('24168020', 'Món cánh gà wings với tương ớt, từ nguồn khác'), -- Chicken "wings" with hot sauce, from other sources
    ('24168021', 'Món cánh gà wings với sốt hoặc gia vị khác, từ nguồn khác'), -- Chicken "wings" with other sauces or seasoning, from other sources
    ('24168022', 'Món cánh gà wings không sốt, từ nguồn khác'), -- Chicken "wings", plain, from other sources
    ('24168030', 'Món cánh gà wings rút xương, với tương ớt, từ đồ ăn nhanh/nhà hàng'), -- Chicken "wings", boneless, with hot sauce, from fast food / restaurant
    ('24168031', 'Món cánh gà wings rút xương, với tương ớt, từ nguồn khác'), -- Chicken "wings", boneless, with hot sauce, from other sources
    ('24170200', 'Lưng gà'), -- Chicken, back
    ('24180200', 'Cổ hoặc sườn gà'), -- Chicken, neck or ribs
    ('24198340', 'Phao câu gà'), -- Chicken, tail
    ('24198440', 'Da gà'), -- Chicken skin
    ('24198500', 'Chân gà'), -- Chicken feet
    ('24198570', 'Thịt gà đóng hộp, chỉ phần thịt'), -- Chicken, canned, meat only
    ('24198670', 'Gà cuộn quay'), -- Chicken, chicken roll, roasted
    ('24198671', 'Miếng chả gà (patty) tẩm bột chiên xù'), -- Chicken patty, breaded
    ('24198677', 'Phi lê gà tẩm bột chiên xù'), -- Chicken fillet, breaded
    ('24198683', 'Phi lê gà nướng vỉ'), -- Chicken fillet, grilled
    ('24198720', 'Thịt gà xay'), -- Chicken, ground
    ('24198729', 'Miếng nugget gà, loại chung'), -- Chicken nuggets, NFS
    ('24198731', 'Miếng nugget gà, từ đồ ăn nhanh'), -- Chicken nuggets, from fast food
    ('24198732', 'Miếng nugget gà, từ nhà hàng'), -- Chicken nuggets, from restaurant
    ('24198735', 'Miếng nugget gà, từ bữa trưa trường học'), -- Chicken nuggets, from school lunch
    ('24198736', 'Miếng nugget gà, từ loại đông lạnh'), -- Chicken nuggets, from frozen
    ('24198737', 'Miếng nugget gà, từ nguồn khác'), -- Chicken nuggets, from other sources
    ('24198739', 'Gà thanh (tenders hoặc strips), loại chung'), -- Chicken tenders or strips, NFS
    ('24198741', 'Gà thanh (tenders hoặc strips) tẩm bột chiên xù, từ đồ ăn nhanh'), -- Chicken tenders or strips, breaded, from fast food
    ('24198742', 'Gà thanh (tenders hoặc strips) tẩm bột chiên xù, từ nhà hàng'), -- Chicken tenders or strips, breaded, from restaurant
    ('24198745', 'Gà thanh (tenders hoặc strips) tẩm bột chiên xù, từ bữa trưa trường học'), -- Chicken tenders or strips, breaded, from school lunch
    ('24198746', 'Gà thanh (tenders hoặc strips) tẩm bột chiên xù, từ loại đông lạnh'), -- Chicken tenders or strips, breaded, from frozen
    ('24198747', 'Gà thanh (tenders hoặc strips) tẩm bột chiên xù, từ nguồn khác'), -- Chicken tenders or strips, breaded, from other sources
    ('24201000', 'Gà tây, loại chung'), -- Turkey, NFS
    ('24201020', 'Gà tây, thịt trắng, bỏ da'), -- Turkey, light meat, skin not eaten
    ('24201030', 'Gà tây, thịt trắng, ăn cả da'), -- Turkey, light meat, skin eaten
    ('24201060', 'Gà tây, thịt trắng, tẩm bột chiên xù, nướng lò hoặc chiên, bỏ da'), -- Turkey, light meat, breaded, baked or fried, skin not eaten
    ('24201070', 'Gà tây, thịt trắng, tẩm bột chiên xù, nướng lò hoặc chiên, ăn cả da'), -- Turkey, light meat, breaded, baked or fried, skin eaten
    ('24201120', 'Gà tây quay, thịt trắng, bỏ da'), -- Turkey, light meat, roasted, skin not eaten
    ('24201130', 'Gà tây quay, thịt trắng, ăn cả da'), -- Turkey, light meat, roasted, skin eaten
    ('24201220', 'Gà tây quay, thịt sẫm, bỏ da'), -- Turkey, dark meat, roasted, skin not eaten
    ('24201230', 'Gà tây quay, thịt sẫm, ăn cả da'), -- Turkey, dark meat, roasted, skin eaten
    ('24201320', 'Gà tây quay, thịt trắng và thịt sẫm, bỏ da'), -- Turkey, light and dark meat, roasted, skin not eaten
    ('24201330', 'Gà tây quay, thịt trắng và thịt sẫm, ăn cả da'), -- Turkey, light and dark meat, roasted, skin eaten
    ('24201360', 'Gà tây chiên tẩm bột, thịt trắng hoặc thịt sẫm, bỏ da'), -- Turkey, light or dark meat, fried, coated, skin not eaten
    ('24201370', 'Gà tây chiên tẩm bột, thịt trắng hoặc thịt sẫm, ăn cả da'), -- Turkey, light or dark meat, fried, coated, skin eaten
    ('24201410', 'Gà tây hầm, thịt trắng hoặc thịt sẫm, bỏ da'), -- Turkey, light or dark meat, stewed, skin not eaten
    ('24201420', 'Gà tây hầm, thịt trắng hoặc thịt sẫm, ăn cả da'), -- Turkey light or dark meat, stewed, skin eaten
    ('24201510', 'Gà tây hun khói, thịt trắng hoặc thịt sẫm, ăn cả da'), -- Turkey, light or dark meat, smoked, skin eaten
    ('24201520', 'Gà tây hun khói, thịt trắng hoặc thịt sẫm, bỏ da'), -- Turkey, light or dark meat, smoked, skin not eaten
    ('24202010', 'Tỏi gà tây, nấu chín, bỏ da'), -- Turkey, drumstick, cooked, skin not eaten
    ('24202020', 'Tỏi gà tây, nấu chín, ăn cả da'), -- Turkey, drumstick, cooked, skin eaten
    ('24202060', 'Tỏi gà tây quay, bỏ da'), -- Turkey, drumstick, roasted, skin not eaten
    ('24202070', 'Tỏi gà tây quay, ăn cả da'), -- Turkey, drumstick, roasted, skin eaten
    ('24202460', 'Má đùi gà tây, nấu chín, ăn cả da'), -- Turkey, thigh, cooked, skin eaten
    ('24202500', 'Má đùi gà tây, nấu chín, bỏ da'), -- Turkey, thigh, cooked, skin not eaten
    ('24202600', 'Cổ gà tây'), -- Turkey, neck
    ('24203010', 'Cánh gà tây, nấu chín, bỏ da'), -- Turkey, wing, cooked, skin not eaten
    ('24203020', 'Cánh gà tây, nấu chín, ăn cả da'), -- Turkey, wing, cooked, skin eaten
    ('24205000', 'Phao câu gà tây'), -- Turkey, tail
    ('24205100', 'Lưng gà tây'), -- Turkey, back
    ('24206000', 'Gà tây đóng hộp'), -- Turkey, canned
    ('24207000', 'Thịt gà tây xay'), -- Turkey, ground
    ('24208000', 'Miếng nugget gà tây'), -- Turkey, nuggets
    ('24208500', 'Thịt xông khói gà tây, nấu chín'), -- Turkey bacon, cooked
    ('24208510', 'Thịt xông khói gà tây, giảm muối, nấu chín'), -- Turkey bacon, reduced sodium, cooked
    ('24300110', 'Vịt, nấu chín, ăn cả da'), -- Duck, cooked, skin eaten
    ('24300120', 'Vịt, nấu chín, bỏ da'), -- Duck, cooked, skin not eaten
    ('24301010', 'Vịt quay, ăn cả da'), -- Duck, roasted, skin eaten
    ('24301020', 'Vịt quay, bỏ da'), -- Duck, roasted, skin not eaten
    ('24302010', 'Vịt quay Bắc Kinh'), -- Duck, Peking
    ('24311010', 'Ngỗng trời quay'), -- Goose, wild, roasted
    ('24400010', 'Gà mái tơ Cornish, nấu chín, ăn cả da'), -- Cornish game hen, cooked, skin eaten
    ('24400020', 'Gà mái tơ Cornish, nấu chín, bỏ da'), -- Cornish game hen, cooked, skin not eaten
    ('24401010', 'Gà mái tơ Cornish quay, ăn cả da'), -- Cornish game hen, roasted, skin eaten
    ('24401020', 'Gà mái tơ Cornish quay, bỏ da'), -- Cornish game hen, roasted, skin not eaten
    ('24402100', 'Chim bồ câu, nấu chín, không rõ cách chế biến'), -- Dove, cooked, NS as to cooking method
    ('24402110', 'Chim bồ câu chiên'), -- Dove, fried
    ('24403100', 'Chim cút, nấu chín'), -- Quail, cooked
    ('24404100', 'Gà lôi, nấu chín'), -- Pheasant, cooked
    ('24701010', 'Thịt gà cho trẻ nhỏ'), -- Baby Toddler chicken
    ('24703010', 'Thịt gà tây cho trẻ nhỏ'), -- Baby Toddler turkey
    ('25110140', 'Gan bò'), -- Liver, beef
    ('25110450', 'Gan gà'), -- Liver, chicken
    ('25112200', 'Pa tê gan'), -- Liver, paste or pate
    ('25120000', 'Tim động vật'), -- Heart
    ('25130000', 'Thận (cật) động vật'), -- Kidney
    ('25140110', 'Tuyến ức/tụy động vật (sweetbreads)'), -- Sweetbreads
    ('25150000', 'Óc động vật'), -- Brains
    ('25160000', 'Lưỡi động vật'), -- Tongue
    ('25160130', 'Lưỡi om nồi kiểu Puerto Rico'), -- Tongue pot roast, Puerto Rican style
    ('25170110', 'Dạ dày bò (tripe)'), -- Tripe
    ('25170210', 'Lòng non lợn'), -- Chitterlings
    ('25170310', 'Dạ dày lợn'), -- Hog maws
    ('25170420', 'Mề gia cầm'), -- Gizzard
    ('25210110', 'Xúc xích hot dog, loại chung'), -- Hot dog, NFS
    ('25210210', 'Xúc xích hot dog bò'), -- Hot dog, beef
    ('25210280', 'Xúc xích hot dog thịt và thịt gia cầm'), -- Hot dog, meat and poultry
    ('25210290', 'Xúc xích hot dog giảm béo'), -- Hot dog, reduced fat
    ('25210410', 'Xúc xích hot dog gà tây'), -- Hot dog, turkey
    ('25220105', 'Xúc xích bò'), -- Beef sausage
    ('25220150', 'Xúc xích bò có phô mai'), -- Beef sausage with cheese
    ('25220210', 'Dồi huyết (blood sausage)'), -- Blood sausage
    ('25220350', 'Xúc xích Bratwurst (Đức)'), -- Bratwurst
    ('25220360', 'Xúc xích Bratwurst có phô mai'), -- Bratwurst, with cheese
    ('25220410', 'Xúc xích Bologna'), -- Bologna
    ('25220425', 'Xúc xích Bologna giảm béo'), -- Bologna, reduced fat
    ('25220435', 'Xúc xích Bologna giảm muối'), -- Bologna, reduced sodium
    ('25220710', 'Xúc xích Chorizo'), -- Chorizo
    ('25220910', 'Thịt thủ đông (head cheese)'), -- Head cheese
    ('25221110', 'Xúc xích Knockwurst'), -- Knockwurst
    ('25221210', 'Xúc xích Mortadella'), -- Mortadella
    ('25221215', 'Thịt pastrami, loại chung'), -- Pastrami, NFS
    ('25221220', 'Thịt pastrami, làm từ bất kỳ loại thịt nào, giảm béo'), -- Pastrami, made from any kind of meat, reduced fat
    ('25221250', 'Xúc xích pepperoni, loại chung'), -- Pepperoni, NFS
    ('25221255', 'Xúc xích pepperoni, giảm béo'), -- Pepperoni, reduced fat
    ('25221260', 'Xúc xích pepperoni, giảm muối'), -- Pepperoni, reduced sodium
    ('25221310', 'Xúc xích Ba Lan'), -- Polish sausage
    ('25221350', 'Xúc xích Ý'), -- Italian sausage
    ('25221400', 'Xúc xích, loại chung'), -- Sausage, NFS
    ('25221405', 'Xúc xích thịt lợn'), -- Pork sausage
    ('25221406', 'Xúc xích thịt lợn, giảm béo'), -- Pork sausage, reduced fat
    ('25221408', 'Xúc xích thịt lợn, giảm muối'), -- Pork sausage, reduced sodium
    ('25221460', 'Xúc xích thịt lợn và thịt bò'), -- Pork and beef sausage
    ('25221500', 'Xúc xích salami, loại chung'), -- Salami, NFS
    ('25221505', 'Xúc xích salami, làm từ bất kỳ loại thịt nào, giảm béo'), -- Salami, made from any type of meat, reduced fat
    ('25221515', 'Xúc xích salami, làm từ bất kỳ loại thịt nào, giảm muối'), -- Salami, made from any type of meat, reduced sodium
    ('25221610', 'Chả scrapple (thịt lợn vụn trộn bột ngô), nấu chín'), -- Scrapple, cooked
    ('25221810', 'Xúc xích Thuringer'), -- Thuringer
    ('25221830', 'Xúc xích gà tây hoặc gà'), -- Turkey or chicken sausage
    ('25221855', 'Xúc xích gà tây hoặc gà, giảm muối'), -- Turkey or chicken sausage, reduced sodium
    ('25221870', 'Xúc xích gà tây hoặc gà và thịt lợn'), -- Turkey or chicken and pork sausage
    ('25221910', 'Xúc xích Vienna, đóng hộp'), -- Vienna sausage, canned
    ('25221950', 'Xúc xích ngâm chua'), -- Pickled sausage
    ('25230110', 'Thịt nguội, loại chung'), -- Luncheon meat, NFS
    ('25230210', 'Giăm bông nguội thái lát, đóng gói sẵn hoặc cắt tại quầy deli'), -- Ham, prepackaged or deli, luncheon meat
    ('25230220', 'Giăm bông nguội thái lát, đóng gói sẵn hoặc cắt tại quầy deli, giảm muối'), -- Ham, prepackaged or deli, luncheon meat, reduced sodium
    ('25230320', 'Thịt gà nguội thái lát, đóng gói sẵn hoặc cắt tại quầy deli'), -- Chicken, prepackaged or deli, luncheon meat
    ('25230340', 'Thịt gà nguội thái lát, đóng gói sẵn hoặc cắt tại quầy deli, giảm muối'), -- Chicken, prepackaged or deli, luncheon meat, reduced sodium
    ('25230420', 'Thịt nguội giăm bông, dạng khối ép'), -- Ham luncheon meat, loaf type
    ('25230530', 'Thịt hộp Spam'), -- Spam
    ('25230550', 'Thịt hộp Spam, giảm muối'), -- Spam, reduced sodium
    ('25230560', 'Xúc xích gan (liverwurst)'), -- Liverwurst
    ('25230610', 'Thịt nguội, dạng khối ép'), -- Luncheon meat, loaf type
    ('25230780', 'Thịt gà tây nguội thái lát, đóng gói sẵn hoặc cắt tại quầy deli'), -- Turkey, prepackaged or deli, luncheon meat
    ('25230785', 'Thịt gà tây nguội thái lát, đóng gói sẵn hoặc cắt tại quầy deli, giảm muối'), -- Turkey, prepackaged or deli, luncheon meat, reduced sodium
    ('25230800', 'Giăm bông gà tây nguội thái lát, đóng gói sẵn hoặc cắt tại quầy deli'), -- Turkey ham, prepackaged or deli, luncheon meat
    ('25231110', 'Thịt bò nguội thái lát, đóng gói sẵn hoặc cắt tại quầy deli'), -- Beef, prepackaged or deli, luncheon meat
    ('25231120', 'Thịt bò nguội thái lát, đóng gói sẵn hoặc cắt tại quầy deli, giảm muối'), -- Beef, prepackaged or deli, luncheon meat, reduced sodium
    ('25240000', 'Thịt phết bánh mì hoặc thịt nghiền đóng hộp, loại chung'), -- Meat spread or potted meat, NFS
    ('25240110', 'Salad gà dạng phết bánh mì'), -- Chicken salad spread
    ('25240220', 'Salad giăm bông dạng phết bánh mì'), -- Ham salad spread
    ('26100100', 'Cá sống'), -- Fish, raw
    ('26100110', 'Cá, loại chung'), -- Fish, NFS
    ('26100120', 'Cá không rõ loại, nướng lò hoặc nướng'), -- Fish, NS as to type, baked or broiled
    ('26100130', 'Cá không rõ loại, tẩm bột, nướng lò hoặc nướng'), -- Fish, NS as to type, baked or broiled, coated
    ('26100140', 'Cá không rõ loại, chiên'), -- Fish, NS as to type, fried
    ('26100160', 'Cá không rõ loại, hấp'), -- Fish, NS as to type, steamed
    ('26100180', 'Cá đóng hộp'), -- Fish, canned
    ('26100190', 'Cá hun khói'), -- Fish, smoked
    ('26100270', 'Cá thanh tẩm bột (fish stick)'), -- Fish, stick
    ('26101110', 'Cá cơm'), -- Fish, anchovy
    ('26105110', 'Cá chép'), -- Fish, carp
    ('26107110', 'Cá da trơn, loại chung'), -- Fish, catfish, NFS
    ('26107120', 'Cá da trơn nướng lò hoặc nướng'), -- Fish, catfish, baked or broiled
    ('26107123', 'Cá da trơn nướng vỉ'), -- Fish, catfish, grilled
    ('26107130', 'Cá da trơn tẩm bột, nướng lò hoặc nướng'), -- Fish, catfish, baked or broiled, coated
    ('26107140', 'Cá da trơn chiên'), -- Fish, catfish, fried
    ('26107160', 'Cá da trơn hấp'), -- Fish, catfish, steamed
    ('26109110', 'Cá tuyết, loại chung'), -- Fish, cod, NFS
    ('26109120', 'Cá tuyết nướng lò hoặc nướng'), -- Fish, cod, baked or broiled
    ('26109123', 'Cá tuyết nướng vỉ'), -- Fish, cod, grilled
    ('26109130', 'Cá tuyết tẩm bột, nướng lò hoặc nướng'), -- Fish, cod, baked or broiled, coated
    ('26109140', 'Cá tuyết chiên'), -- Fish, cod, fried
    ('26109160', 'Cá tuyết hấp'), -- Fish, cod, steamed
    ('26111110', 'Cá đù'), -- Fish, croaker
    ('26113110', 'Cá chình'), -- Fish, eel
    ('26115110', 'Cá bơn, loại chung'), -- Fish, flounder, NFS
    ('26115120', 'Cá bơn nướng lò hoặc nướng'), -- Fish, flounder, baked or broiled
    ('26115123', 'Cá bơn nướng vỉ'), -- Fish, flounder, grilled
    ('26115130', 'Cá bơn tẩm bột, nướng lò hoặc nướng'), -- Fish, flounder, baked or broiled, coated
    ('26115140', 'Cá bơn chiên'), -- Fish, flounder, fried
    ('26115160', 'Cá bơn hấp'), -- Fish, flounder, steamed
    ('26117110', 'Cá tuyết chấm đen (haddock), loại chung'), -- Fish, haddock, NFS
    ('26117120', 'Cá tuyết chấm đen (haddock) nướng lò hoặc nướng'), -- Fish, haddock, baked or broiled
    ('26117121', 'Cá tuyết chấm đen (haddock) nướng vỉ'), -- Fish, haddock, grilled
    ('26117130', 'Cá tuyết chấm đen (haddock) tẩm bột, nướng lò hoặc nướng'), -- Fish, haddock, baked or broiled, coated
    ('26117140', 'Cá tuyết chấm đen (haddock) chiên'), -- Fish, haddock, fried
    ('26117160', 'Cá tuyết chấm đen (haddock) hấp'), -- Fish, haddock, steamed
    ('26118010', 'Cá bơn lưỡi ngựa (halibut)'), -- Fish, halibut
    ('26119110', 'Cá trích'), -- Fish, herring
    ('26119180', 'Cá ngâm chua'), -- Fish, pickled
    ('26121110', 'Cá thu, loại chung'), -- Fish, mackerel, NFS
    ('26121120', 'Cá thu nướng lò hoặc nướng'), -- Fish, mackerel, baked or broiled
    ('26121121', 'Cá thu nướng vỉ'), -- Fish, mackerel, grilled
    ('26121131', 'Cá thu tẩm bột, nướng lò hoặc nướng'), -- Fish, mackerel, baked or broiled, coated
    ('26121140', 'Cá thu chiên'), -- Fish, mackerel, fried
    ('26121180', 'Cá thu đóng hộp'), -- Fish, mackerel, canned
    ('26123110', 'Cá đối'), -- Fish, mullet
    ('26127110', 'Cá rô (perch), loại chung'), -- Fish, perch, NFS
    ('26127120', 'Cá rô (perch) nướng lò hoặc nướng'), -- Fish, perch, baked or broiled
    ('26127123', 'Cá rô (perch) nướng vỉ'), -- Fish, perch, grilled
    ('26127130', 'Cá rô (perch) tẩm bột, nướng lò hoặc nướng'), -- Fish, perch, baked or broiled, coated
    ('26127140', 'Cá rô (perch) chiên'), -- Fish, perch, fried
    ('26127160', 'Cá rô (perch) hấp'), -- Fish, perch, steamed
    ('26129110', 'Cá chó (pike)'), -- Fish, pike
    ('26131110', 'Cá chim vây vàng (pompano), loại chung'), -- Fish, pompano, NFS
    ('26131120', 'Cá chim vây vàng (pompano) nướng lò hoặc nướng'), -- Fish, pompano, baked or broiled
    ('26131121', 'Cá chim vây vàng (pompano) nướng vỉ'), -- Fish, pompano, grilled
    ('26131130', 'Cá chim vây vàng (pompano) tẩm bột, nướng lò hoặc nướng'), -- Fish, pompano, baked or broiled, coated
    ('26131140', 'Cá chim vây vàng (pompano) chiên'), -- Fish, pompano, fried
    ('26131160', 'Cá chim vây vàng (pompano) hấp'), -- Fish, pompano, steamed
    ('26133110', 'Cá hồng'), -- Fish, snapper
    ('26137100', 'Cá hồi sống'), -- Fish, salmon, raw
    ('26137110', 'Cá hồi, loại chung'), -- Fish, salmon, NFS
    ('26137120', 'Cá hồi nướng lò hoặc nướng'), -- Fish, salmon, baked or broiled
    ('26137123', 'Cá hồi nướng vỉ'), -- Fish, salmon, grilled
    ('26137130', 'Cá hồi tẩm bột, nướng lò hoặc nướng'), -- Fish, salmon, baked or broiled, coated
    ('26137140', 'Cá hồi chiên'), -- Fish, salmon, fried
    ('26137160', 'Cá hồi hấp'), -- Fish, salmon, steamed
    ('26137180', 'Cá hồi đóng hộp'), -- Fish, salmon, canned
    ('26137190', 'Cá hồi hun khói'), -- Fish, salmon, smoked
    ('26139180', 'Cá mòi đóng hộp'), -- Fish, sardines, canned
    ('26141110', 'Cá vược, loại chung'), -- Fish, bass, NFS
    ('26141120', 'Cá vược nướng lò hoặc nướng'), -- Fish, bass, baked or broiled
    ('26141121', 'Cá vược nướng vỉ'), -- Fish, bass, grilled
    ('26141130', 'Cá vược tẩm bột, nướng lò hoặc nướng'), -- Fish, bass, baked or broiled, coated
    ('26141140', 'Cá vược chiên'), -- Fish, bass, fried
    ('26141160', 'Cá vược hấp'), -- Fish, bass, steamed
    ('26143110', 'Cá mập'), -- Fish, shark
    ('26149110', 'Cá kiếm'), -- Fish, swordfish
    ('26151110', 'Cá hồi vân, loại chung'), -- Fish, trout, NFS
    ('26151120', 'Cá hồi vân nướng lò hoặc nướng'), -- Fish, trout, baked or broiled
    ('26151123', 'Cá hồi vân nướng vỉ'), -- Fish, trout, grilled
    ('26151130', 'Cá hồi vân tẩm bột, nướng lò hoặc nướng'), -- Fish, trout, baked or broiled, coated
    ('26151140', 'Cá hồi vân chiên'), -- Fish, trout, fried
    ('26151160', 'Cá hồi vân hấp'), -- Fish, trout, steamed
    ('26153100', 'Cá ngừ sống'), -- Fish, tuna, raw
    ('26153110', 'Cá ngừ, loại chung'), -- Fish, tuna, NFS
    ('26153120', 'Cá ngừ nấu chín'), -- Fish, tuna, cooked
    ('26155110', 'Cá ngừ đóng hộp'), -- Fish, tuna, canned
    ('26157110', 'Cá tuyết trắng (whiting), loại chung'), -- Fish, whiting, NFS
    ('26157120', 'Cá tuyết trắng (whiting) nướng lò hoặc nướng'), -- Fish, whiting, baked or broiled
    ('26157123', 'Cá tuyết trắng (whiting) nướng vỉ'), -- Fish, whiting, grilled
    ('26157130', 'Cá tuyết trắng (whiting) tẩm bột, nướng lò hoặc nướng'), -- Fish, whiting, baked or broiled, coated
    ('26157140', 'Cá tuyết trắng (whiting) chiên'), -- Fish, whiting, fried
    ('26157160', 'Cá tuyết trắng (whiting) hấp'), -- Fish, whiting, steamed
    ('26158000', 'Cá rô phi, loại chung'), -- Fish, tilapia, NFS
    ('26158010', 'Cá rô phi nướng lò hoặc nướng'), -- Fish, tilapia, baked or broiled
    ('26158013', 'Cá rô phi nướng vỉ'), -- Fish, tilapia, grilled
    ('26158020', 'Cá rô phi tẩm bột, nướng lò hoặc nướng'), -- Fish, tilapia, baked or broiled, coated
    ('26158030', 'Cá rô phi chiên'), -- Fish, tilapia, fried
    ('26158050', 'Cá rô phi hấp'), -- Fish, tilapia, steamed
    ('26158100', 'Cá thịt trắng hỗn hợp nhiều loài, loại chung'), -- Fish, white, mixed species, NFS
    ('26158110', 'Cá thịt trắng hỗn hợp nhiều loài, nướng lò hoặc nướng'), -- Fish, white, mixed species, baked or broiled
    ('26158120', 'Cá thịt trắng hỗn hợp nhiều loài, tẩm bột, nướng lò hoặc nướng'), -- Fish, white, mixed species, baked or broiled, coated
    ('26158130', 'Cá thịt trắng hỗn hợp nhiều loài, chiên'), -- Fish, white, mixed species, fried
    ('26158140', 'Cá thịt trắng hỗn hợp nhiều loài, hấp'), -- Fish, white, mixed species, steamed
    ('26158150', 'Cá thịt trắng hỗn hợp nhiều loài, nướng vỉ'), -- Fish, white, mixed species, grilled
    ('26203110', 'Đùi ếch'), -- Frog legs
    ('26205110', 'Bạch tuộc'), -- Octopus
    ('26211100', 'Trứng cá muối (caviar)'), -- Caviar
    ('26213120', 'Mực ống (calamari) nấu chín'), -- Calamari, cooked
    ('26213140', 'Mực ống (calamari) chiên'), -- Calamari, fried
    ('26215120', 'Thịt rùa'), -- Turtle
    ('26300000', 'Hải sản có vỏ, loại chung'), -- Shellfish, NFS
    ('26301110', 'Bào ngư'), -- Abalone
    ('26303100', 'Nghêu sống'), -- Clams, raw
    ('26303110', 'Nghêu, loại chung'), -- Clams, NFS
    ('26303120', 'Nghêu nướng lò hoặc nướng'), -- Clams, baked or broiled
    ('26303140', 'Nghêu chiên'), -- Clams, fried
    ('26303160', 'Nghêu hấp hoặc luộc'), -- Clams, steamed or boiled
    ('26303180', 'Nghêu đóng hộp'), -- Clams, canned
    ('26305160', 'Cua'), -- Crab
    ('26305180', 'Cua đóng hộp'), -- Crab, canned
    ('26307140', 'Cua lột (cua vỏ mềm)'), -- Crab, soft shell
    ('26309140', 'Tôm càng nước ngọt chiên'), -- Crayfish, fried
    ('26309160', 'Tôm càng nước ngọt nấu chín'), -- Crayfish, cooked
    ('26311110', 'Tôm hùm'), -- Lobster
    ('26313110', 'Vẹm'), -- Mussels
    ('26315100', 'Hàu sống'), -- Oysters, raw
    ('26315120', 'Hàu nướng lò hoặc nướng'), -- Oysters, baked or broiled
    ('26315130', 'Hàu hấp'), -- Oysters, steamed
    ('26315140', 'Hàu chiên'), -- Oysters, fried
    ('26315180', 'Hàu đóng hộp'), -- Oysters, canned
    ('26317120', 'Sò điệp nướng lò hoặc nướng'), -- Scallops, baked or broiled
    ('26317121', 'Sò điệp nướng vỉ'), -- Scallops, grilled
    ('26317130', 'Sò điệp hấp hoặc luộc'), -- Scallops, steamed or boiled
    ('26317140', 'Sò điệp chiên'), -- Scallops, fried
    ('26319110', 'Tôm, loại chung'), -- Shrimp, NFS
    ('26319120', 'Tôm nướng lò hoặc nướng'), -- Shrimp, baked or broiled
    ('26319123', 'Tôm nướng vỉ'), -- Shrimp, grilled
    ('26319130', 'Tôm hấp hoặc luộc'), -- Shrimp, steamed or boiled
    ('26319140', 'Tôm chiên'), -- Shrimp, fried
    ('26319160', 'Tôm tẩm bột, nướng lò hoặc nướng'), -- Shrimp, baked or broiled, coated
    ('26319170', 'Tôm khô'), -- Shrimp, dried
    ('26319180', 'Tôm đóng hộp'), -- Shrimp, canned
    ('26321110', 'Ốc sên (escargot)'), -- Escargot
    ('27100100', 'Thịt BBQ, loại chung'), -- Barbecue meat, NFS
    ('27111000', 'Thịt bò sốt nền cà chua'), -- Beef with tomato-based sauce
    ('27111200', 'Bò hầm rượu vang (beef burgundy)'), -- Beef burgundy
    ('27111300', 'Món guisada bò (bò hầm kiểu Mỹ Latinh)'), -- Guisada, beef
    ('27111400', 'Món chili, loại chung'), -- Chili, NFS
    ('27111405', 'Món chili có thịt, nhà hàng'), -- Chili with meat, from restaurant
    ('27111406', 'Món chili có thịt và đậu'), -- Chili with meat and beans
    ('27111407', 'Món chili có thịt, đóng hộp'), -- Chili with meat, canned
    ('27111420', 'Món chili có thịt'), -- Chili with meat
    ('27111500', 'Món sloppy joe (thịt bò xay sốt), không kèm bánh mì'), -- Sloppy joe, no bun
    ('27112000', 'Thịt bò với nước sốt gravy'), -- Beef with gravy
    ('27112010', 'Bít tết Salisbury với nước sốt gravy'), -- Salisbury steak with gravy
    ('27113000', 'Thịt bò sốt kem hoặc sốt trắng'), -- Beef with cream or white sauce
    ('27113100', 'Bò stroganoff'), -- Beef stroganoff
    ('27113200', 'Thịt bò khô thái mỏng sốt kem') -- Creamed chipped or dried beef
) AS t (source_food_code, name_vi)
WHERE f.source = 'USDA_FNDDS' AND f.source_food_code = t.source_food_code;

UPDATE nutrition_foods f SET name_vi = t.name_vi, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:usda-name-vi-v25'
FROM (VALUES
    ('27113300', 'Thịt viên kiểu Thụy Điển sốt kem hoặc sốt trắng'), -- Swedish meatballs with cream or white sauce
    ('27114000', 'Thịt bò sốt nấm'), -- Beef with mushroom sauce
    ('27115000', 'Thịt bò sốt nền nước tương'), -- Beef with soy-based sauce
    ('27115100', 'Bít tết sốt teriyaki'), -- Steak teriyaki
    ('27116100', 'Cà ri bò'), -- Beef curry
    ('27116110', 'Cà ri bò với cơm'), -- Beef curry with rice
    ('27116200', 'Thịt bò BBQ, có sốt'), -- Barbecue beef, with sauce
    ('27116210', 'Thịt bò BBQ, không sốt'), -- Barbecue beef, no sauce
    ('27116300', 'Thịt bò sốt chua ngọt'), -- Beef with sweet and sour sauce
    ('27116350', 'Thịt bò xay tẩm gia vị hầm, kiểu Mexico'), -- Stewed seasoned ground beef, Mexican style
    ('27116400', 'Bò sống băm (steak tartare)'), -- Steak tartare
    ('27118110', 'Thịt viên, kiểu Puerto Rico'), -- Meatballs, Puerto Rican style
    ('27118120', 'Thịt bò xay tẩm gia vị hầm, kiểu Puerto Rico'), -- Stewed seasoned ground beef, Puerto Rican style
    ('27118130', 'Thịt bò khô hầm, kiểu Puerto Rico'), -- Stewed dried beef, Puerto Rican style
    ('27120020', 'Giăm bông hoặc thịt lợn với nước sốt gravy'), -- Ham or pork with gravy
    ('27120030', 'Thịt lợn BBQ, có sốt'), -- Barbecue pork, with sauce
    ('27120040', 'Thịt lợn BBQ, không sốt'), -- Barbecue pork, no sauce
    ('27120060', 'Thịt lợn xào chua ngọt'), -- Sweet and sour pork
    ('27120080', 'Giăm bông stroganoff'), -- Ham stroganoff
    ('27120090', 'Giăm bông hoặc thịt lợn sốt nấm'), -- Ham or pork with mushroom sauce
    ('27120100', 'Giăm bông hoặc thịt lợn sốt nền cà chua'), -- Ham or pork with tomato-based sauce
    ('27120110', 'Xúc xích sốt nền cà chua'), -- Sausage with tomato-based sauce
    ('27120120', 'Nước sốt gravy xúc xích'), -- Sausage gravy
    ('27120130', 'Món guisada thịt lợn'), -- Guisada, pork
    ('27120150', 'Thịt lợn hoặc giăm bông sốt nền nước tương'), -- Pork or ham with soy-based sauce
    ('27120210', 'Xúc xích hot dog sốt chili, không kèm bánh mì'), -- Chili hot dog, no bun
    ('27120250', 'Xúc xích frankfurter hoặc hot dog sốt nền cà chua'), -- Frankfurters or hot dogs with tomato-based sauce
    ('27121000', 'Thịt lợn nấu ớt chili và cà chua'), -- Pork with chili and tomatoes
    ('27121010', 'Thịt lợn hầm, kiểu Puerto Rico'), -- Stewed pork, Puerto Rican style
    ('27130010', 'Thịt cừu non hoặc cừu già với nước sốt gravy'), -- Lamb or mutton with gravy
    ('27133010', 'Thịt dê hầm, kiểu Puerto Rico'), -- Stewed goat, Puerto Rican style
    ('27135020', 'Thịt bê scallopini (thái mỏng áp chảo)'), -- Veal scallopini
    ('27135050', 'Thịt bê sốt rượu Marsala'), -- Veal Marsala
    ('27135110', 'Thịt bê parmigiana (tẩm bột, phủ phô mai)'), -- Veal parmigiana
    ('27135150', 'Thịt bê cordon bleu'), -- Veal cordon bleu
    ('27136050', 'Thịt nai/hươu sốt nền cà chua'), -- Venison or deer with tomato-based sauce
    ('27136080', 'Thịt nai/hươu với nước sốt gravy'), -- Venison or deer with gravy
    ('27141000', 'Gà hoặc gà tây cacciatore (hầm kiểu Ý)'), -- Chicken or turkey cacciatore
    ('27141030', 'Sốt mì spaghetti với thịt gia cầm'), -- Spaghetti sauce with poultry
    ('27141035', 'Sốt mì spaghetti với thịt gia cầm và thêm rau'), -- Spaghetti sauce with poultry and added vegetables
    ('27141050', 'Gà hầm sốt nền cà chua, kiểu Mexico'), -- Stewed chicken with tomato-based sauce, Mexican style
    ('27141500', 'Món chili gà và đậu'), -- Chili with chicken and beans
    ('27141505', 'Món chili gà'), -- Chili with chicken
    ('27141510', 'Món chili trắng'), -- Chili, white
    ('27142000', 'Gà với nước sốt gravy'), -- Chicken with gravy
    ('27142100', 'Gà hoặc gà tây hầm fricassee'), -- Chicken or turkey fricassee
    ('27142200', 'Gà tây với nước sốt gravy'), -- Turkey with gravy
    ('27143000', 'Gà hoặc gà tây sốt kem'), -- Chicken or turkey with cream sauce
    ('27144000', 'Gà hoặc gà tây sốt nấm'), -- Chicken or turkey with mushroom sauce
    ('27145000', 'Gà hoặc gà tây sốt teriyaki'), -- Chicken or turkey with teriyaki
    ('27146011', 'Gà BBQ'), -- Barbecue chicken
    ('27146100', 'Gà hoặc gà tây sốt chua ngọt'), -- Sweet and sour chicken or turkey
    ('27146110', 'Gà hoặc gà tây sốt chua ngọt, không có rau'), -- Sweet and sour chicken or turkey, without vegetables
    ('27146150', 'Cà ri gà'), -- Chicken curry
    ('27146155', 'Cà ri gà với cơm'), -- Chicken curry with rice
    ('27146160', 'Gà sốt mole (Mexico)'), -- Chicken with mole sauce
    ('27146200', 'Gà hoặc gà tây sốt phô mai'), -- Chicken or turkey with cheese sauce
    ('27146250', 'Gà hoặc gà tây cordon bleu'), -- Chicken or turkey cordon bleu
    ('27146300', 'Gà hoặc gà tây parmigiana'), -- Chicken or turkey parmigiana
    ('27146350', 'Gà sốt cam'), -- Orange chicken
    ('27146360', 'Gà sốt vừng'), -- Sesame chicken
    ('27146400', 'Gà Kiev (cuộn bơ tẩm bột chiên)'), -- Chicken kiev
    ('27148010', 'Gà nhồi, tỏi gà hoặc ức gà, kiểu Puerto Rico'), -- Stuffed chicken, drumstick or breast, Puerto Rican style
    ('27150010', 'Cá sốt kem hoặc sốt trắng, không phải cá ngừ hay tôm hùm'), -- Fish with cream or white sauce, not tuna or lobster
    ('27150050', 'Cá hấp khuôn (timbale) hoặc mousse cá'), -- Fish timbale or mousse
    ('27150110', 'Tôm cocktail (tôm luộc chấm sốt)'), -- Shrimp cocktail
    ('27150120', 'Cá ngừ sốt kem hoặc sốt trắng'), -- Tuna with cream or white sauce
    ('27150130', 'Hải sản thermidor (sốt kem đút lò)'), -- Seafood thermidor
    ('27150140', 'Sốt chấm hải sản'), -- Seafood sauce
    ('27150151', 'Sốt mì spaghetti với hải sản'), -- Spaghetti sauce with seafood
    ('27150155', 'Sốt mì spaghetti với hải sản và thêm rau'), -- Spaghetti sauce with seafood and added vegetables
    ('27150160', 'Tôm sốt tôm hùm'), -- Shrimp with lobster sauce
    ('27150170', 'Tôm sốt chua ngọt'), -- Sweet and sour shrimp
    ('27150210', 'Nước mắm'), -- Fish sauce
    ('27150230', 'Tôm scampi (xào bơ tỏi)'), -- Shrimp scampi
    ('27150310', 'Cá sốt nền cà chua'), -- Fish with tomato-based sauce
    ('27150320', 'Cà ri cá'), -- Fish curry
    ('27150325', 'Cà ri cá với cơm'), -- Fish curry with rice
    ('27150410', 'Tôm sốt teriyaki'), -- Shrimp teriyaki
    ('27151030', 'Món ceviche (hải sản sống ngâm chanh)'), -- Ceviche
    ('27151040', 'Cua sốt nền cà chua, kiểu Puerto Rico'), -- Crabs in tomato-based sauce, Puerto Rican style
    ('27151050', 'Tôm sốt tỏi, kiểu Puerto Rico'), -- Shrimp in garlic sauce, Puerto Rican style
    ('27151070', 'Món hầm cá'), -- Stew, fish
    ('27160100', 'Thịt viên, không rõ loại thịt, có sốt'), -- Meatballs, NS as to type of meat, with sauce
    ('27161010', 'Thịt xay nướng khối (meatloaf), kiểu Puerto Rico'), -- Meat loaf, Puerto Rican style
    ('27162010', 'Thịt sốt nền cà chua'), -- Meat with tomato-based sauce
    ('27162040', 'Sốt mì spaghetti với thịt'), -- Spaghetti sauce with meat
    ('27162060', 'Sốt mì spaghetti với thịt và thêm rau'), -- Spaghetti sauce with meat and added vegetables
    ('27162500', 'Thịt bò và thịt lợn xay tẩm gia vị hầm, kiểu Mexico'), -- Stewed, seasoned, ground beef and pork, Mexican style
    ('27163010', 'Thịt với nước sốt gravy, không rõ loại thịt'), -- Meat with gravy, NS as to type of meat,
    ('27211000', 'Thịt bò và khoai tây, không sốt'), -- Beef and potatoes, no sauce
    ('27211190', 'Thịt bò và khoai tây sốt kem, sốt trắng hoặc sốt nấm'), -- Beef and potatoes with cream sauce, white sauce or mushroom sauce
    ('27211300', 'Món hash thịt bò quay (băm xào)'), -- Beef, roast, hash
    ('27211400', 'Món hash thịt bò muối (corned beef)'), -- Corned beef hash
    ('27211500', 'Thịt bò và khoai tây sốt phô mai'), -- Beef and potatoes with cheese sauce
    ('27211550', 'Thịt bò xay tẩm gia vị hầm khoai tây, kiểu Mexico'), -- Stewed, seasoned, ground beef with potatoes, Mexican style
    ('27212000', 'Thịt bò và mì sợi, không sốt'), -- Beef and noodles, no sauce
    ('27212050', 'Thịt bò và nui sốt phô mai'), -- Beef and macaroni with cheese sauce
    ('27212100', 'Thịt bò và mì sợi sốt nền cà chua'), -- Beef and noodles with tomato-based sauce
    ('27212120', 'Món chili với nui'), -- Chili with macaroni
    ('27212200', 'Thịt bò và mì sợi với nước sốt gravy'), -- Beef and noodles with gravy
    ('27212300', 'Thịt bò và mì sợi sốt kem hoặc sốt trắng'), -- Beef and noodles with cream or white sauce
    ('27212350', 'Bò stroganoff với mì sợi'), -- Beef stroganoff with noodles
    ('27212400', 'Thịt bò và mì sợi sốt nấm'), -- Beef and noodles with mushroom sauce
    ('27212500', 'Thịt bò và mì sợi sốt nền nước tương'), -- Beef and noodles with soy-based sauce
    ('27213000', 'Thịt bò và cơm, không sốt'), -- Beef and rice, no sauce
    ('27213010', 'Cơm biryani thịt'), -- Biryani with meat
    ('27213100', 'Thịt bò và cơm sốt nền cà chua'), -- Beef and rice with tomato-based sauce
    ('27213120', 'Thịt viên trộn gạo (porcupine balls) sốt nền cà chua'), -- Porcupine balls with tomato-based sauce
    ('27213200', 'Thịt bò và cơm với nước sốt gravy'), -- Beef and rice with gravy
    ('27213300', 'Thịt bò và cơm sốt kem'), -- Beef and rice with cream sauce
    ('27213400', 'Thịt bò và cơm sốt nấm'), -- Beef and rice with mushroom sauce
    ('27213420', 'Thịt viên trộn gạo (porcupine balls) sốt nấm'), -- Porcupine balls with mushroom sauce
    ('27213500', 'Thịt bò và cơm sốt nền nước tương'), -- Beef and rice with soy-based sauce
    ('27213600', 'Thịt bò và cơm sốt phô mai'), -- Beef and rice with cheese sauce
    ('27214100', 'Thịt xay nướng khối (meatloaf) làm từ thịt bò'), -- Meat loaf made with beef
    ('27214110', 'Thịt xay nướng khối (meatloaf) làm từ thịt bò, sốt nền cà chua'), -- Meat loaf made with beef, with tomato-based sauce
    ('27214300', 'Bò Wellington'), -- Beef wellington
    ('27214500', 'Miếng chả (patty) thịt bò muối'), -- Corned beef patty
    ('27218210', 'Bò hầm khoai tây, kiểu Puerto Rico'), -- Beef stew with potatoes, Puerto Rican style
    ('27220010', 'Thịt xay nướng khối (meatloaf) làm từ giăm bông'), -- Meat loaf made with ham
    ('27220020', 'Giăm bông và mì sợi sốt kem hoặc sốt trắng'), -- Ham and noodles with cream or white sauce
    ('27220030', 'Giăm bông và cơm sốt nấm'), -- Ham and rice with mushroom sauce
    ('27220050', 'Giăm bông hoặc thịt lợn với nhân nhồi (stuffing)'), -- Ham or pork with stuffing
    ('27220080', 'Bánh croquette giăm bông'), -- Ham croquette
    ('27220110', 'Thịt lợn và cơm sốt nền cà chua'), -- Pork and rice with tomato-based sauce
    ('27220120', 'Xúc xích và cơm sốt nền cà chua'), -- Sausage and rice with tomato-based sauce
    ('27220150', 'Xúc xích và cơm sốt nấm'), -- Sausage and rice with mushroom sauce
    ('27220170', 'Xúc xích và cơm sốt phô mai'), -- Sausage and rice with cheese sauce
    ('27220190', 'Xúc xích và mì sợi sốt kem hoặc sốt trắng'), -- Sausage and noodles with cream or white sauce
    ('27220210', 'Giăm bông và mì sợi, không sốt'), -- Ham and noodles, no sauce
    ('27220310', 'Giăm bông hoặc thịt lợn và cơm, không sốt'), -- Ham or pork and rice, no sauce
    ('27220510', 'Giăm bông hoặc thịt lợn và khoai tây với nước sốt gravy'), -- Ham or pork and potatoes with gravy
    ('27220520', 'Giăm bông hoặc thịt lợn và khoai tây sốt phô mai'), -- Ham or pork and potatoes with cheese sauce
    ('27221100', 'Chân giò lợn hầm, kiểu Puerto Rico'), -- Stewed pig's feet, Puerto Rican style
    ('27221150', 'Món hầm thịt lợn'), -- Stew, pork
    ('27221170', 'Món hầm thịt lợn, có mì Ý (pasta)'), -- Stew, pork, with pasta
    ('27230010', 'Thịt cừu non hoặc cừu già xay nướng khối'), -- Lamb or mutton loaf
    ('27231000', 'Thịt cừu non hoặc cừu già và khoai tây với nước sốt gravy'), -- Lamb or mutton and potatoes with gravy
    ('27232000', 'Thịt cừu non hoặc cừu già và khoai tây sốt nền cà chua'), -- Lamb or mutton and potatoes with tomato-based sauce
    ('27233000', 'Thịt cừu non hoặc cừu già và mì sợi với nước sốt gravy'), -- Lamb or mutton and noodles with gravy
    ('27235000', 'Thịt xay nướng khối (meatloaf) làm từ thịt nai/hươu'), -- Meat loaf made with venison/deer
    ('27236000', 'Thịt nai/hươu và mì sợi sốt kem hoặc sốt trắng'), -- Venison or deer and noodles with cream or white sauce
    ('27241000', 'Món hash gà hoặc gà tây'), -- Chicken or turkey hash
    ('27241010', 'Gà hoặc gà tây và khoai tây với nước sốt gravy'), -- Chicken or turkey and potatoes with gravy
    ('27242000', 'Gà hoặc gà tây và mì sợi, không sốt'), -- Chicken or turkey and noodles, no sauce
    ('27242200', 'Gà hoặc gà tây và mì sợi với nước sốt gravy'), -- Chicken or turkey and noodles with gravy
    ('27242250', 'Gà hoặc gà tây và mì sợi sốt nấm'), -- Chicken or turkey and noodles with mushroom sauce
    ('27242300', 'Gà hoặc gà tây và mì sợi sốt kem hoặc sốt trắng'), -- Chicken or turkey and noodles with cream or white sauce
    ('27242310', 'Gà hoặc gà tây và mì sợi sốt phô mai'), -- Chicken or turkey and noodles with cheese sauce
    ('27242350', 'Gà hoặc gà tây tetrazzini (mì sốt kem đút lò)'), -- Chicken or turkey tetrazzini
    ('27242400', 'Gà hoặc gà tây và mì sợi sốt nền cà chua'), -- Chicken or turkey and noodles with tomato-based sauce
    ('27242500', 'Gà hoặc gà tây và mì sợi sốt nền nước tương'), -- Chicken or turkey and noodles with soy-based sauce
    ('27243000', 'Gà hoặc gà tây và cơm, không sốt'), -- Chicken or turkey and rice, no sauce
    ('27243100', 'Cơm biryani gà'), -- Biryani with chicken
    ('27243300', 'Gà hoặc gà tây và cơm sốt kem'), -- Chicken or turkey and rice with cream sauce
    ('27243400', 'Gà hoặc gà tây và cơm sốt nấm'), -- Chicken or turkey and rice with mushroom sauce
    ('27243500', 'Gà hoặc gà tây và cơm sốt nền cà chua'), -- Chicken or turkey and rice with tomato-based sauce
    ('27243600', 'Gà hoặc gà tây và cơm sốt nền nước tương'), -- Chicken or turkey and rice with soy-based sauce
    ('27246100', 'Gà hoặc gà tây với bánh bột (dumpling)'), -- Chicken or turkey with dumplings
    ('27246200', 'Gà hoặc gà tây với nhân nhồi (stuffing)'), -- Chicken or turkey with stuffing
    ('27246300', 'Chả gà hoặc gà tây, dạng bánh, miếng (patty) hoặc croquette'), -- Chicken or turkey cake, patty, or croquette
    ('27246400', 'Bánh souffle gà hoặc gà tây'), -- Chicken or turkey souffle
    ('27246500', 'Thịt xay nướng khối (meatloaf) làm từ gà hoặc gà tây'), -- Meat loaf made with chicken or turkey
    ('27246505', 'Thịt xay nướng khối (meatloaf) làm từ gà hoặc gà tây, sốt nền cà chua'), -- Meat loaf made with chicken or turkey, with tomato-based sauce
    ('27250040', 'Chả cua (crab cake)'), -- Crab, cake
    ('27250050', 'Chả cá, dạng bánh hoặc miếng (patty)'), -- Fish, cake or patty
    ('27250060', 'Chả cá gefilte (kiểu Do Thái)'), -- Gefilte fish
    ('27250070', 'Chả cá hồi, dạng bánh hoặc miếng (patty)'), -- Fish, salmon cake or patty
    ('27250120', 'Tôm và mì sợi, không sốt'), -- Shrimp and noodles, no sauce
    ('27250122', 'Tôm và mì sợi với nước sốt gravy'), -- Shrimp and noodles with gravy
    ('27250124', 'Tôm và mì sợi sốt nấm'), -- Shrimp and noodles with mushroom sauce
    ('27250126', 'Tôm và mì sợi sốt kem hoặc sốt trắng'), -- Shrimp and noodles with cream or white sauce
    ('27250128', 'Tôm và mì sợi sốt nền nước tương'), -- Shrimp and noodles with soy-based sauce
    ('27250130', 'Tôm và mì sợi sốt phô mai'), -- Shrimp and noodles with cheese sauce
    ('27250132', 'Tôm và mì sợi sốt cà chua'), -- Shrimp and noodles with tomato sauce
    ('27250160', 'Chả cá ngừ, dạng bánh hoặc miếng (patty)'), -- Tuna cake or patty
    ('27250210', 'Chả nghêu, dạng bánh hoặc miếng (patty)'), -- Clam cake or patty
    ('27250250', 'Cá bơn nhồi cua'), -- Flounder with crab stuffing
    ('27250260', 'Tôm hùm nhồi bánh mì, nướng lò'), -- Lobster with bread stuffing, baked
    ('27250270', 'Nghêu Casino (nướng với thịt xông khói)'), -- Clams Casino
    ('27250400', 'Chả tôm, dạng bánh hoặc miếng (patty)'), -- Shrimp, cake or patty
    ('27250410', 'Tôm nhồi cua'), -- Shrimp with crab stuffing
    ('27250450', 'Bánh mì chiên phết tôm (shrimp toast)'), -- Shrimp toast
    ('27250520', 'Thanh cua (thịt cua giả)'), -- Imitation crab meat
    ('27250550', 'Bánh souffle hải sản'), -- Seafood souffle
    ('27250610', 'Món đút lò (casserole) cá ngừ và mì sợi, sốt kem hoặc sốt trắng'), -- Tuna noodle casserole with cream or white sauce
    ('27250630', 'Món đút lò (casserole) cá ngừ và mì sợi, sốt nấm'), -- Tuna noodle casserole with mushroom sauce
    ('27250710', 'Cá ngừ và cơm sốt nấm'), -- Tuna and rice with mushroom sauce
    ('27250810', 'Cá và cơm sốt nền cà chua'), -- Fish and rice with tomato-based sauce
    ('27250820', 'Cá và cơm sốt kem'), -- Fish and rice with cream sauce
    ('27250830', 'Cá và cơm sốt nấm'), -- Fish and rice with mushroom sauce
    ('27250900', 'Cá và mì sợi sốt nấm'), -- Fish and noodles with mushroom sauce
    ('27250950', 'Hải sản có vỏ và mì sợi sốt nền cà chua'), -- Shellfish and noodles with tomato-based sauce
    ('27251010', 'Cá hồi hầm, kiểu Puerto Rico'), -- Stewed salmon, Puerto Rican style
    ('27260010', 'Thịt xay nướng khối (meatloaf), không rõ loại thịt'), -- Meat loaf, NS as to type of meat
    ('27260050', 'Thịt viên có vụn bánh mì, không rõ loại thịt, với nước sốt gravy'), -- Meatballs, with breading, NS as to type of meat, with gravy
    ('27260080', 'Thịt xay nướng khối (meatloaf) làm từ thịt bò và thịt lợn'), -- Meat loaf made with beef and pork
    ('27260090', 'Thịt xay nướng khối (meatloaf) làm từ thịt bò, thịt bê và thịt lợn'), -- Meat loaf made with beef, veal and pork
    ('27260100', 'Thịt xay nướng khối (meatloaf) làm từ thịt bò và thịt lợn, sốt nền cà chua'), -- Meat loaf made with beef and pork, with tomato-based sauce
    ('27260110', 'Món hash thịt, không rõ loại thịt'), -- Hash, NS as to type of meat
    ('27260500', 'Xúc xích Vienna hầm khoai tây, kiểu Puerto Rico'), -- Vienna sausages stewed with potatoes, Puerto Rican style
    ('27261500', 'Thịt bò và thịt lợn xay tẩm gia vị hầm khoai tây, kiểu Mexico'), -- Stewed, seasoned, ground beef and pork with potatoes, Mexican style
    ('27311110', 'Bò, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không sốt'), -- Beef, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; no sauce
    ('27311120', 'Bò, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không sốt'), -- Beef, potatoes, and vegetables, excluding carrots, broccoli, and dark-green leafy; no sauce
    ('27311210', 'Bò muối (corned beef), khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không sốt'), -- Corned beef, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; no sauce
    ('27311220', 'Bò muối (corned beef), khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không sốt'), -- Corned beef, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; no sauce
    ('27311310', 'Món hầm thịt bò, đóng hộp'), -- Stew, beef, canned
    ('27311410', 'Món hầm thịt bò'), -- Stew, beef
    ('27311430', 'Món hầm thịt bò, có mì Ý (pasta)'), -- Stew, beef, with pasta
    ('27311510', 'Bánh shepherd''s pie (thịt băm phủ khoai tây nghiền)'), -- Shepherd's pie
    ('27311600', 'Bò, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Beef, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; gravy
    ('27311605', 'Bò, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Beef, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27311610', 'Bò, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt kem, sốt trắng hoặc sốt nấm'), -- Beef, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; cream sauce, white sauce, or mushroom sauce
    ('27311620', 'Bò, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt kem, sốt trắng hoặc sốt nấm'), -- Beef, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; cream sauce, white sauce, or mushroom sauce
    ('27311625', 'Bò, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Beef, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27311630', 'Bò, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền cà chua'), -- Beef, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; tomato-based sauce
    ('27311635', 'Bò, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt phô mai'), -- Beef, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; cheese sauce
    ('27311640', 'Bò, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt phô mai'), -- Beef, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; cheese sauce
    ('27311645', 'Bò, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền nước tương'), -- Beef, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; soy-based sauce
    ('27311650', 'Bò, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền nước tương'), -- Beef, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; soy-based sauce
    ('27313010', 'Bò, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không sốt'), -- Beef, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; no sauce
    ('27313020', 'Bò, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không sốt'), -- Beef, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; no sauce
    ('27313110', 'Bò xào chow mein hoặc chop suey, có mì sợi'), -- Beef chow mein or chop suey with noodles
    ('27313150', 'Bò, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền nước tương'), -- Beef, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; soy-based sauce
    ('27313160', 'Bò, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền nước tương'), -- Beef, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; soy-based sauce
    ('27313210', 'Bò, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Beef, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27313220', 'Bò, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền cà chua'), -- Beef, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; tomato-based sauce
    ('27313310', 'Bò, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nấm'), -- Beef, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; mushroom sauce
    ('27313320', 'Bò, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nấm'), -- Beef, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; mushroom sauce
    ('27313410', 'Bò, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Beef, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; gravy
    ('27313420', 'Bò, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Beef, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27315010', 'Bò, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không sốt'), -- Beef, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; no sauce
    ('27315020', 'Bò, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không sốt'), -- Beef, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; no sauce
    ('27315210', 'Bò, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Beef, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27315220', 'Bò, cơm và rau (trừ cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Beef, rice, and vegetables excluding carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27315250', 'Cải bắp cuộn nhồi thịt bò và gạo'), -- Stuffed cabbage rolls with beef and rice
    ('27315270', 'Lá nho cuộn nhồi thịt bò và gạo'), -- Stuffed grape leaves with beef and rice
    ('27315310', 'Bò, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nấm'), -- Beef, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; mushroom sauce
    ('27315320', 'Bò, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nấm'), -- Beef, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; mushroom sauce
    ('27315330', 'Bò, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt phô mai'), -- Beef, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; cheese sauce
    ('27315340', 'Bò, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt phô mai'), -- Beef, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; cheese sauce
    ('27315410', 'Bò, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Beef, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; gravy
    ('27315420', 'Bò, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Beef, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27315510', 'Bò, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền nước tương'), -- Beef, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; soy-based sauce
    ('27315520', 'Bò, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền nước tương'), -- Beef, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; soy-based sauce
    ('27317010', 'Bánh pot pie thịt bò'), -- Pot pie, beef
    ('27317100', 'Bò, bánh bột (dumpling) và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Beef, dumplings, and vegetables including carrots, broccoli, and/or dark-green leafy; gravy
    ('27317110', 'Bò, bánh bột (dumpling) và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Beef, dumplings, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27319010', 'Ớt chuông xanh nhồi, kiểu Puerto Rico'), -- Stuffed green pepper, Puerto Rican style
    ('27320025', 'Giăm bông hoặc thịt lợn, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không sốt'), -- Ham or pork, noodles and vegetables excluding carrots, broccoli, and dark-green leafy; no sauce
    ('27320027', 'Giăm bông hoặc thịt lợn, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không sốt'), -- Ham or pork, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; no sauce
    ('27320030', 'Giăm bông hoặc thịt lợn, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt phô mai'), -- Ham or pork, noodles and vegetables excluding carrots, broccoli, and dark-green leafy; cheese sauce
    ('27320040', 'Thịt lợn, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không sốt'), -- Pork, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; no sauce
    ('27320070', 'Giăm bông hoặc thịt lợn, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Ham or pork, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27320080', 'Xúc xích, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền cà chua'), -- Sausage, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; tomato-based sauce
    ('27320090', 'Xúc xích, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Sausage, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27320100', 'Thịt lợn, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Pork, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27320110', 'Thịt lợn, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền cà chua'), -- Pork, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; tomato-based sauce
    ('27320120', 'Xúc xích, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Sausage, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; gravy
    ('27320130', 'Xúc xích, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Sausage, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27320140', 'Thịt lợn, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Pork, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; gravy
    ('27320150', 'Thịt lợn, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Pork, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27320210', 'Thịt lợn, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không sốt'), -- Pork, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; no sauce
    ('27320310', 'Thịt lợn xào chow mein hoặc chop suey, có mì sợi'), -- Pork chow mein or chop suey with noodles
    ('27320320', 'Thịt lợn, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền nước tương'), -- Pork, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; soy-based sauce
    ('27320330', 'Thịt lợn, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền nước tương'), -- Pork, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; soy-based sauce
    ('27320340', 'Thịt lợn, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Pork, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27320350', 'Thịt lợn, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền cà chua'), -- Pork, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; tomato-based sauce
    ('27320410', 'Giăm bông, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không sốt'), -- Ham, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; no sauce
    ('27320450', 'Giăm bông, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không sốt'), -- Ham, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; no sauce
    ('27320500', 'Thịt lợn xào chua ngọt với cơm'), -- Sweet and sour pork with rice
    ('27330050', 'Thịt cừu non hoặc cừu già, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Lamb or mutton, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27330060', 'Thịt cừu non hoặc cừu già, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Lamb or mutton, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27330080', 'Thịt cừu non hoặc cừu già, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Lamb or mutton, rice, and vegetables  including carrots, broccoli, and/or dark-green leafy; gravy
    ('27330170', 'Lá nho cuộn nhồi thịt cừu và gạo'), -- Stuffed grape leaves with lamb and rice
    ('27330210', 'Món hầm thịt cừu'), -- Stew, lamb
    ('27335500', 'Thịt thỏ hầm, kiểu Puerto Rico'), -- Stewed rabbit, Puerto Rican style,
    ('27336200', 'Thịt nai/hươu, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Venison or deer, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; gravy
    ('27336250', 'Thịt nai/hươu, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Venison or deer, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27336300', 'Thịt nai/hươu, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Venison or deer, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27336310', 'Thịt nai/hươu, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền cà chua'), -- Venison or deer, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; tomato-based sauce
    ('27341000', 'Gà hoặc gà tây, khoai tây, ngô và phô mai, với nước sốt gravy'), -- Chicken or turkey, potatoes, corn, and cheese, with gravy
    ('27341010', 'Gà hoặc gà tây, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không sốt'), -- Chicken or turkey, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; no sauce
    ('27341020', 'Gà hoặc gà tây, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không sốt'), -- Chicken or turkey, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; no sauce
    ('27341025', 'Gà hoặc gà tây, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Chicken or turkey, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; gravy
    ('27341030', 'Gà hoặc gà tây, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Chicken or turkey, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27341035', 'Gà hoặc gà tây, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt kem, sốt trắng hoặc sốt nấm'), -- Chicken or turkey, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; cream sauce, white sauce, or mushroom sauce
    ('27341040', 'Gà hoặc gà tây, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt kem, sốt trắng hoặc sốt nấm'), -- Chicken or turkey, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; cream sauce, white sauce, or mushroom sauce
    ('27341045', 'Gà hoặc gà tây, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt phô mai'), -- Chicken or turkey, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; cheese sauce
    ('27341050', 'Gà hoặc gà tây, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt phô mai'), -- Chicken or turkey, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; cheese sauce
    ('27341055', 'Gà hoặc gà tây, khoai tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Chicken or turkey, potatoes, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27341060', 'Gà hoặc gà tây, khoai tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền cà chua'), -- Chicken or turkey, potatoes, and vegetables excluding carrots, broccoli, and dark-green leafy; tomato-based sauce
    ('27341330', 'Món hầm gà, có mì Ý (pasta)'), -- Stew, chicken, with pasta
    ('27341520', 'Món hầm gà'), -- Stew, chicken
    ('27343010', 'Gà hoặc gà tây, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không sốt'), -- Chicken or turkey, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; no sauce
    ('27343020', 'Gà hoặc gà tây, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không sốt'), -- Chicken or turkey, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; no sauce
    ('27343410', 'Gà hoặc gà tây, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Chicken or turkey, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; gravy
    ('27343420', 'Gà hoặc gà tây, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Chicken or turkey, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27343470', 'Gà hoặc gà tây, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt kem, sốt trắng hoặc sốt nấm'), -- Chicken or turkey, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; cream sauce, white sauce, or mushroom sauce
    ('27343480', 'Gà hoặc gà tây, mì sợi và rau (trừ cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt kem, sốt trắng hoặc sốt nấm'), -- Chicken or turkey, noodles, and vegetables excluding carrots, broccoli, and/or dark-green leafy; cream sauce, white sauce, or mushroom sauce
    ('27343510', 'Gà hoặc gà tây, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Chicken or turkey, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27343520', 'Gà hoặc gà tây, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền cà chua'), -- Chicken or turkey, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; tomato-based sauce
    ('27343910', 'Gà hoặc gà tây xào chow mein hoặc chop suey, có mì sợi'), -- Chicken or turkey chow mein or chop suey with noodles
    ('27343950', 'Gà hoặc gà tây, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt phô mai'), -- Chicken or turkey, noodles, and vegetables including carrots, broccoli, and/or dark-green leafy; cheese sauce
    ('27343960', 'Gà hoặc gà tây, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt phô mai'), -- Chicken or turkey, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; cheese sauce
    ('27345010', 'Gà hoặc gà tây, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không sốt'), -- Chicken or turkey, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; no sauce
    ('27345020', 'Gà hoặc gà tây, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không sốt'), -- Chicken or turkey, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; no sauce
    ('27345210', 'Gà hoặc gà tây, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Chicken or turkey, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; gravy
    ('27345220', 'Gà hoặc gà tây, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Chicken or turkey, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27345230', 'Gà hoặc gà tây, cơm, ngô và phô mai, với nước sốt gravy'), -- Chicken or turkey, rice, corn, and cheese, with gravy
    ('27345310', 'Gà hoặc gà tây, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền nước tương'), -- Chicken or turkey, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; soy-based sauce
    ('27345320', 'Gà hoặc gà tây, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền nước tương'), -- Chicken or turkey, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; soy-based sauce
    ('27345410', 'Gà hoặc gà tây, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt kem, sốt trắng hoặc sốt nấm'), -- Chicken or turkey, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; cream sauce, white sauce, or mushroom sauce
    ('27345420', 'Gà hoặc gà tây, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt kem, sốt trắng hoặc sốt nấm'), -- Chicken or turkey, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; cream sauce, white sauce, or mushroom sauce
    ('27345440', 'Gà hoặc gà tây, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt phô mai'), -- Chicken or turkey, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; cheese sauce
    ('27345450', 'Gà hoặc gà tây, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt phô mai'), -- Chicken or turkey, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; cheese sauce
    ('27345510', 'Gà hoặc gà tây, cơm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt nền cà chua'), -- Chicken or turkey, rice, and vegetables including carrots, broccoli, and/or dark-green leafy; tomato-based sauce
    ('27345520', 'Gà hoặc gà tây, cơm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt nền cà chua'), -- Chicken or turkey, rice, and vegetables excluding carrots, broccoli, and dark-green leafy; tomato-based sauce
    ('27347100', 'Bánh pot pie gà'), -- Pot pie, chicken
    ('27347200', 'Gà hoặc gà tây, nhân nhồi (stuffing) và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không sốt'), -- Chicken or turkey, stuffing, and vegetables including carrots, broccoli, and/or dark-green leafy; no sauce
    ('27347210', 'Gà hoặc gà tây, nhân nhồi (stuffing) và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không sốt'), -- Chicken or turkey,stuffing, and vegetables excluding carrots, broccoli, and dark green leafy; no sauce
    ('27347220', 'Gà hoặc gà tây, nhân nhồi (stuffing) và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Chicken or turkey, stuffing, and vegetables including carrots, broccoli, and/or dark-green leafy; gravy
    ('27347230', 'Gà hoặc gà tây, nhân nhồi (stuffing) và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Chicken or turkey, stuffing, and vegetables excluding carrots, broccoli, and dark-green leafy; gravy
    ('27347240', 'Gà hoặc gà tây, bánh bột (dumpling) và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), nước sốt gravy'), -- Chicken or turkey, dumplings, and vegetables including carrots, broccoli, and/or dark green leafy; gravy
    ('27347250', 'Gà hoặc gà tây, bánh bột (dumpling) và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), nước sốt gravy'), -- Chicken or turkey, dumplings, and vegetables excluding carrots, broccoli, and dark green leafy; gravy
    ('27348100', 'Gà hầm fricassee, kiểu Puerto Rico'), -- Chicken fricassee, Puerto Rican style
    ('27350020', 'Cơm paella hải sản'), -- Paella with seafood
    ('27350050', 'Tôm xào chow mein hoặc chop suey, có mì sợi'), -- Shrimp chow mein or chop suey with noodles
    ('27350060', 'Tôm sốt creole, có cơm'), -- Shrimp creole, with rice
    ('27350080', 'Món đút lò (casserole) cá ngừ, mì sợi và rau, sốt kem hoặc sốt trắng'), -- Tuna noodle casserole with vegetables, cream or white sauce
    ('27350090', 'Cá, mì sợi và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), sốt phô mai'), -- Fish, noodles, and vegetables including carrots, broccoli, and/or dark green leafy; cheese sauce
    ('27350100', 'Cá, mì sợi và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), sốt phô mai'), -- Fish, noodles, and vegetables excluding carrots, broccoli, and dark-green leafy; cheese sauce
    ('27350110', 'Súp bouillabaisse (súp hải sản kiểu Pháp)'), -- Soup, bouillabaisse
    ('27350410', 'Món đút lò (casserole) cá ngừ, mì sợi và rau, sốt nấm'), -- Tuna noodle casserole with vegetables and mushroom sauce
    ('27351010', 'Cá tuyết với rau củ nhiều tinh bột, kiểu Puerto Rico'), -- Codfish with starchy vegetables, Puerto Rican style
    ('27351040', 'Cá tuyết Biscayne, kiểu Puerto Rico'), -- Biscayne codfish, Puerto Rican style
    ('27360000', 'Món hầm, loại chung'), -- Stew, NFS
    ('27360080', 'Món xào chow mein hoặc chop suey, không rõ loại thịt, có mì sợi'), -- Chow mein or chop suey, NS as to type of meat, with noodles
    ('27360090', 'Cơm paella, loại chung'), -- Paella, NFS
    ('27360120', 'Món xào chow mein hoặc chop suey, nhiều loại thịt, có mì sợi'), -- Chow mein or chop suey, various types of meat, with noodles
    ('27362000', 'Dạ dày bò (tripe) hầm với khoai tây, kiểu Puerto Rico'), -- Stewed tripe, with potatoes, Puerto Rican style
    ('27363000', 'Súp gumbo với cơm'), -- Gumbo with rice
    ('27363100', 'Món jambalaya có thịt và cơm'), -- Jambalaya with meat and rice
    ('27410210', 'Bò và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, không sốt'), -- Beef and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, no sauce
    ('27410220', 'Bò và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, không sốt'), -- Beef and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, no sauce
    ('27410250', 'Bò xiên nướng (shish kabob) với rau, trừ khoai tây'), -- Beef shish kabob with vegetables, excluding potatoes
    ('27411100', 'Bò với rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền cà chua'), -- Beef with vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, tomato-based sauce
    ('27411120', 'Bít tết kiểu Thụy Sĩ (Swiss steak)'), -- Swiss steak
    ('27411150', 'Bò cuộn nhồi rau hoặc thịt trộn, sốt nền cà chua'), -- Beef rolls, stuffed with vegetables or meat mixture, tomato-based sauce
    ('27411200', 'Bò với rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền cà chua'), -- Beef with vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, tomato-based sauce
    ('27414100', 'Bò với rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nấm'), -- Beef with vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, mushroom sauce
    ('27414200', 'Bò với rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nấm'), -- Beef with vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, mushroom sauce
    ('27415100', 'Bò và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Beef and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, soy-based sauce
    ('27415110', 'Bò xào súp lơ xanh'), -- Beef and broccoli
    ('27415120', 'Bò, đậu phụ và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Beef, tofu, and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, soy-based sauce
    ('27415130', 'Bò kiểu Tứ Xuyên (Szechuan)'), -- Szechuan beef
    ('27415140', 'Bò kiểu Hồ Nam (Hunan)'), -- Hunan beef
    ('27415150', 'Bò xào chow mein hoặc chop suey, không có mì sợi'), -- Beef chow mein or chop suey, no noodles
    ('27415170', 'Bò xào Cung Bảo (Kung Pao)'), -- Kung Pao beef
    ('27415200', 'Bò và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Beef and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, soy-based sauce
    ('27415220', 'Bò, đậu phụ và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Beef, tofu, and vegetables excluding carrots, broccoli,  and dark-green leafy; no potatoes, soy-based sauce
    ('27416100', 'Bò và rau, kiểu Hawaii'), -- Beef and vegetables, Hawaiian style
    ('27416150', 'Bò xào ớt chuông (pepper steak)'), -- Pepper steak
    ('27416200', 'Thịt bò xay với trứng và hành tây'), -- Beef, ground, with egg and onion
    ('27416250', 'Salad thịt bò'), -- Beef salad
    ('27416300', 'Nhân bánh taco bò: thịt bò, phô mai, cà chua, sốt taco'), -- Beef taco filling: beef, cheese, tomato, taco sauce
    ('27416400', 'Bò xào rau với nước tương'), -- Stir fried beef and vegetables in soy sauce
    ('27416450', 'Bò và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, nước sốt gravy'), -- Beef and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, gravy
    ('27416500', 'Bò và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, nước sốt gravy'), -- Beef and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, gravy
    ('27418410', 'Bít tết bò với hành tây, kiểu Puerto Rico'), -- Beef steak with onions, Puerto Rican style
    ('27420010', 'Cải bắp nấu khoanh giò lợn (ham hock)'), -- Cabbage with ham hocks
    ('27420020', 'Salad giăm bông hoặc thịt lợn'), -- Ham or pork salad
    ('27420040', 'Xúc xích frankfurter hoặc hot dog với dưa cải bắp muối chua (sauerkraut)'), -- Frankfurters or hot dogs and sauerkraut
    ('27420060', 'Thịt lợn và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, không sốt'), -- Pork and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, no sauce
    ('27420080', 'Rau lá xanh nấu giăm bông hoặc thịt lợn'), -- Greens with ham or pork
    ('27420100', 'Thịt lợn, đậu phụ và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Pork, tofu, and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, soy-base sauce
    ('27420110', 'Thịt lợn và rau, kiểu Hawaii'), -- Pork and vegetables, Hawaiian style
    ('27420120', 'Thịt lợn và cải xoong sốt nền nước tương'), -- Pork and watercress with soy-based sauce
    ('27420150', 'Thịt lợn xào Cung Bảo (Kung Pao)'), -- Kung Pao pork
    ('27420160', 'Thịt lợn Moo Shu (mộc tu), không kèm bánh tráng mỏng kiểu Trung Quốc'), -- Moo Shu pork, without Chinese pancake
    ('27420170', 'Thịt lợn và hành tây sốt nền nước tương'), -- Pork and onions with soy-based sauce
    ('27420200', 'Món hash thịt lợn'), -- Pork hash
    ('27420250', 'Giăm bông và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, không sốt'), -- Ham and vegetables including carrots broccoli, and/or dark- green leafy; no potatoes, no sauce
    ('27420270', 'Giăm bông và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, không sốt'), -- Ham and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, no sauce
    ('27420350', 'Thịt lợn và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, không sốt'), -- Pork and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, no sauce
    ('27420370', 'Thịt lợn, đậu phụ và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Pork, tofu, and vegetables, excluding carrots, broccoli, and dark-green leafy; no potatoes, soy-based sauce
    ('27420390', 'Thịt lợn xào chow mein hoặc chop suey, không có mì sợi'), -- Pork chow mein or chop suey, no noodles
    ('27420400', 'Thịt lợn và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền cà chua'), -- Pork and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, tomato-based sauce
    ('27420410', 'Thịt lợn và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền cà chua'), -- Pork and vegetables excluding  carrots, broccoli, and dark-green leafy; no potatoes, tomato-based sauce
    ('27420450', 'Xúc xích và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền cà chua'), -- Sausage and vegetables including  carrots, broccoli, and/or dark-green leafy; no potatoes, tomato-based sauce
    ('27420460', 'Xúc xích và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền cà chua'), -- Sausage and vegetables, excluding carrots, broccoli, and dark-green leafy; no potatoes, tomato-based sauce
    ('27420470', 'Xúc xích và ớt chuông, không sốt'), -- Sausage and peppers, no sauce
    ('27420500', 'Thịt lợn và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Pork and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, soy-based sauce
    ('27420510', 'Thịt lợn và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Pork and vegetables excluding carrots, broccoli, and dark- green leafy; no potatoes, soy-based sauce
    ('27420520', 'Thịt lợn xiên nướng (shish kabob) với rau, trừ khoai tây'), -- Pork shish kabob with vegetables, excluding potatoes
    ('27430610', 'Thịt cừu xiên nướng (shish kabob) với rau, trừ khoai tây'), -- Lamb shish kabob with vegetables, excluding potatoes
    ('27440110', 'Gà hoặc gà tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, không sốt'), -- Chicken or turkey and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, no sauce
    ('27440120', 'Gà hoặc gà tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, không sốt'), -- Chicken or turkey and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, no sauce
    ('27440130', 'Gà hoặc gà tây xiên nướng (shish kabob) với rau, trừ khoai tây'), -- Chicken or turkey shish kabob with vegetables, excluding potatoes
    ('27441120', 'Gà hoặc gà tây sốt creole, không có cơm'), -- Chicken or turkey creole, without rice
    ('27442110', 'Gà hoặc gà tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, nước sốt gravy'), -- Chicken or turkey and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, gravy
    ('27442120', 'Gà hoặc gà tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, nước sốt gravy'), -- Chicken or turkey and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, gravy
    ('27443110', 'Gà hoặc gà tây à la king với rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt kem, sốt trắng hoặc sốt nền súp'), -- Chicken or turkey a la king with vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, cream, white, or soup-based sauce
    ('27443120', 'Gà hoặc gà tây à la king với rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt kem, sốt trắng hoặc sốt nền súp'), -- Chicken or turkey a la king with vegetables excluding carrorts, broccoli, and dark-green leafy; no potatoes, cream, white, or soup-based sauce
    ('27443150', 'Gà hoặc gà tây divan (đút lò với súp lơ xanh)'), -- Chicken or turkey divan
    ('27445110', 'Gà hoặc gà tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Chicken or turkey and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, soy-based sauce
    ('27445120', 'Gà hoặc gà tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Chicken or turkey and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, soy-based sauce
    ('27445125', 'Gà hoặc gà tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền cà chua'), -- Chicken or turkey and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, tomato-based sauce
    ('27445130', 'Gà hoặc gà tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền cà chua'), -- Chicken or turkey and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, tomato-based sauce
    ('27445150', 'Gà Tả Tông Đường (General Tso)'), -- General Tso chicken
    ('27445180', 'Gà xào nấm Moo Goo Gai Pan'), -- Moo Goo Gai Pan
    ('27445220', 'Gà xào Cung Bảo (Kung Pao)'), -- Kung pao chicken
    ('27445250', 'Gà hạnh nhân'), -- Almond chicken
    ('27446100', 'Gà hoặc gà tây xào chow mein hoặc chop suey, không có mì sợi'), -- Chicken or turkey chow mein or chop suey, no noodles
    ('27446200', 'Salad gà hoặc gà tây, làm với sốt mayonnaise'), -- Chicken or turkey salad, made with mayonnaise
    ('27446205', 'Salad gà hoặc gà tây với hạt và/hoặc hoa quả'), -- Chicken or turkey salad with nuts and/or fruits
    ('27446220', 'Salad gà hoặc gà tây với trứng'), -- Chicken or turkey salad with egg
    ('27446225', 'Salad gà hoặc gà tây, làm với sốt mayonnaise loại nhẹ'), -- Chicken or turkey salad, made with light mayonnaise
    ('27446230', 'Salad gà hoặc gà tây, làm với sốt trộn salad kiểu mayonnaise'), -- Chicken or turkey salad, made with mayonnaise-type salad dressing
    ('27446235', 'Salad gà hoặc gà tây, làm với sốt trộn salad kiểu mayonnaise loại nhẹ'), -- Chicken or turkey salad, made with light mayonnaise-type salad dressing
    ('27446240', 'Salad gà hoặc gà tây, làm với sốt trộn dạng kem'), -- Chicken or turkey salad, made with creamy dressing
    ('27446245', 'Salad gà hoặc gà tây, làm với sốt trộn dạng kem loại nhẹ'), -- Chicken or turkey salad, made with light creamy dressing
    ('27446250', 'Salad gà hoặc gà tây, làm với sốt trộn kiểu Ý'), -- Chicken or turkey salad, made with Italian dressing
    ('27446255', 'Salad gà hoặc gà tây, làm với sốt trộn kiểu Ý loại nhẹ'), -- Chicken or turkey salad, made with light Italian dressing
    ('27446260', 'Salad gà hoặc gà tây, làm với bất kỳ loại sốt trộn không béo nào'), -- Chicken or turkey salad, made with any type of fat free dressing
    ('27446300', 'Salad rau tươi với gà hoặc gà tây, gồm gà và/hoặc gà tây, cà chua và/hoặc cà rốt, rau khác, không sốt trộn'), -- Chicken or turkey garden salad, chicken and/or turkey, tomato and/or carrots, other vegetables, no dressing
    ('27446310', 'Salad rau tươi với gà hoặc gà tây, gồm gà và/hoặc gà tây, rau khác trừ cà chua và cà rốt, không sốt trộn'), -- Chicken or turkey garden salad, chicken and/or turkey, other vegetables excluding tomato and carrots, no dressing
    ('27446315', 'Salad rau tươi với gà hoặc gà tây, thịt xông khói và phô mai, gồm gà và/hoặc gà tây, thịt xông khói, phô mai, xà lách và/hoặc rau lá, cà chua và/hoặc cà rốt, rau khác, không sốt trộn'), -- Chicken or turkey garden salad with bacon and cheese, chicken and/or turkey, bacon, cheese, lettuce and/or greens, tomato and/or carrots, other vegetables, no dressing
    ('27446320', 'Salad rau tươi với gà hoặc gà tây tẩm bột chiên xù, thịt xông khói và phô mai, gồm gà và/hoặc gà tây, thịt xông khói, phô mai, xà lách và/hoặc rau lá, cà chua và/hoặc cà rốt, rau khác, không sốt trộn'), -- Chicken or turkey, breaded, fried, garden salad with bacon and cheese, chicken and/or turkey, bacon, cheese, lettuce and/or greens, tomato and/or carrots, other vegetables, no dressing
    ('27446330', 'Salad rau tươi với gà hoặc gà tây và phô mai, gồm gà và/hoặc gà tây, phô mai, xà lách và/hoặc rau lá, cà chua và/hoặc cà rốt, rau khác, không sốt trộn'), -- Chicken or turkey garden salad with cheese, chicken and/or turkey, cheese, lettuce and/or greens, tomato and/or carrots, other vegetables, no dressing
    ('27446332', 'Salad rau tươi với gà hoặc gà tây tẩm bột chiên xù và phô mai, gồm gà và/hoặc gà tây, phô mai, xà lách và/hoặc rau lá, cà chua và/hoặc cà rốt, rau khác, không sốt trộn'), -- Chicken or turkey, breaded, fried, garden salad with cheese, chicken and/or turkey, cheese, lettuce and/or greens, tomato and/or carrots, other vegetables, no dressing
    ('27446350', 'Salad rau tươi kiểu châu Á với gà hoặc gà tây, gồm gà và/hoặc gà tây, xà lách, hoa quả, hạt, không sốt trộn'), -- Asian chicken or turkey garden salad, chicken and/or turkey, lettuce, fruit, nuts, no dressing
    ('27446355', 'Salad rau tươi kiểu châu Á với gà hoặc gà tây và mì giòn, gồm gà và/hoặc gà tây, xà lách, hoa quả, hạt, mì giòn, không sốt trộn'), -- Asian chicken or turkey garden salad with crispy noodles, chicken and/or turkey, lettuce, fruit, nuts, crispy noodles, no dressing
    ('27446360', 'Salad Caesar với gà hoặc gà tây, gồm gà và/hoặc gà tây, xà lách, cà chua, phô mai, không sốt trộn'), -- Chicken or turkey caesar garden salad, chicken and/or turkey, lettuce, tomato, cheese, no dressing
    ('27446362', 'Salad Caesar với gà hoặc gà tây tẩm bột chiên xù, gồm gà và/hoặc gà tây, xà lách, cà chua, phô mai, không sốt trộn'), -- Chicken or turkey, breaded, fried, caesar garden salad, chicken and/or turkey, lettuce, tomatoes, cheese, no dressing
    ('27446400', 'Gà hoặc gà tây và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt phô mai'), -- Chicken or turkey and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, cheese sauce
    ('27446410', 'Gà hoặc gà tây và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt phô mai'), -- Chicken or turkey and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, cheese sauce
    ('27448020', 'Gà hoặc gà tây hầm fricassee, có sốt, không khoai tây (khoai tây tính riêng), kiểu Puerto Rico'), -- Chicken or turkey fricassee, with sauce, no potatoes, potatoes reported separately, Puerto Rican style
    ('27448030', 'Gà hoặc gà tây hầm fricassee, không sốt, không khoai tây, kiểu Puerto Rico'), -- Chicken or turkey fricassee, no sauce, no potatoes, Puerto Rican style
    ('27450010', 'Salad cua'), -- Crab salad
    ('27450020', 'Salad tôm hùm'), -- Lobster salad
    ('27450030', 'Salad cá hồi'), -- Salmon salad
    ('27450040', 'Tôm xào chow mein hoặc chop suey, không có mì sợi'), -- Shrimp chow mein or chop suey, no noodles
    ('27450060', 'Salad cá ngừ, làm với sốt mayonnaise'), -- Tuna salad, made with mayonnaise
    ('27450061', 'Salad cá ngừ, làm với sốt mayonnaise loại nhẹ'), -- Tuna salad, made with light mayonnaise
    ('27450062', 'Salad cá ngừ, làm với sốt trộn salad kiểu mayonnaise'), -- Tuna salad, made with mayonnaise-type salad dressing
    ('27450063', 'Salad cá ngừ, làm với sốt trộn salad kiểu mayonnaise loại nhẹ'), -- Tuna salad, made with light mayonnaise-type salad dressing
    ('27450064', 'Salad cá ngừ, làm với sốt trộn dạng kem'), -- Tuna salad, made with creamy dressing
    ('27450065', 'Salad cá ngừ, làm với sốt trộn dạng kem loại nhẹ'), -- Tuna salad, made with light creamy dressing
    ('27450066', 'Salad cá ngừ, làm với sốt trộn kiểu Ý'), -- Tuna salad, made with Italian dressing
    ('27450067', 'Salad cá ngừ, làm với sốt trộn kiểu Ý loại nhẹ'), -- Tuna salad, made with light Italian dressing
    ('27450068', 'Salad cá ngừ, làm với bất kỳ loại sốt trộn không béo nào'), -- Tuna salad, made with any type of fat free dressing
    ('27450070', 'Salad tôm'), -- Shrimp salad
    ('27450080', 'Salad hải sản'), -- Seafood salad
    ('27450090', 'Salad cá ngừ với phô mai'), -- Tuna salad with cheese
    ('27450100', 'Salad cá ngừ với trứng'), -- Tuna salad with egg
    ('27450110', 'Salad rau tươi với tôm, gồm tôm, xà lách, trứng, cà chua và/hoặc cà rốt, rau khác, không sốt trộn'), -- Shrimp garden salad, shrimp, lettuce, eggs, tomato and/or carrots, other vegetables, no dressing
    ('27450120', 'Salad rau tươi với tôm, gồm tôm, xà lách, trứng, rau trừ cà chua và cà rốt, không sốt trộn'), -- Shrimp garden salad, shrimp, lettuce, eggs, vegetables excluding tomato and carrots, no dressing
    ('27450130', 'Salad cua làm từ thanh cua (cua giả)'), -- Crab salad made with imitation crab
    ('27450150', 'Cá, đậu phụ và rau chiên tempura'), -- Fish, tofu, and vegetables, tempura
    ('27450180', 'Salad rau tươi với hải sản, gồm hải sản, xà lách, rau trừ cà chua và cà rốt, không sốt trộn'), -- Seafood garden salad with seafood, lettuce, vegetables excluding tomato and carrots, no dressing
    ('27450190', 'Salad rau tươi với hải sản, gồm hải sản, xà lách, cà chua và/hoặc cà rốt, rau khác, không sốt trộn'), -- Seafood garden salad with seafood, lettuce, tomato and/or carrots, other vegetables, no dressing
    ('27450200', 'Salad rau tươi với hải sản, gồm hải sản, xà lách, trứng, rau trừ cà chua và cà rốt, không sốt trộn'), -- Seafood garden salad with seafood, lettuce, eggs, vegetables excluding tomato and carrots, no dressing
    ('27450210', 'Salad rau tươi với hải sản, gồm hải sản, xà lách, trứng, cà chua và/hoặc cà rốt, rau khác, không sốt trộn'), -- Seafood garden salad with seafood, lettuce, eggs, tomato and/or carrots, other vegetables, no dressing
    ('27450250', 'Hàu Rockefeller (nướng phủ rau và bơ)'), -- Oysters Rockefeller
    ('27450310', 'Cá hồi lomi (món Hawaii)'), -- Lomi salmon
    ('27450400', 'Tôm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, không sốt'), -- Shrimp and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, no sauce
    ('27450405', 'Tôm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, không sốt'), -- Shrimp and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, no sauce
    ('27450410', 'Tôm và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Shrimp and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, soy-based sauce
    ('27450420', 'Tôm và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Shrimp and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, soy-based sauce
    ('27450430', 'Tôm xiên nướng (shish kabob) với rau, trừ khoai tây'), -- Shrimp shish kabob with vegetables, excluding potatoes
    ('27450450', 'Tôm sốt creole, không có cơm'), -- Shrimp creole, no rice
    ('27450470', 'Tôm xào Cung Bảo (Kung Pao)'), -- Kung Pao shrimp
    ('27450510', 'Món đút lò (casserole) cá ngừ và rau, sốt nấm, không có mì sợi'), -- Tuna casserole with vegetables and mushroom sauce, no noodles
    ('27450600', 'Hải sản có vỏ hỗn hợp và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Shellfish mixture and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, soy-based sauce
    ('27450610', 'Hải sản có vỏ hỗn hợp và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Shellfish mixture and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, soy-based sauce
    ('27450650', 'Hải sản có vỏ hỗn hợp và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nấm'), -- Shellfish mixture and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, mushroom sauce
    ('27450660', 'Hải sản có vỏ hỗn hợp và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nấm'), -- Shellfish mixture and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, mushroom sauce
    ('27450700', 'Cá và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền cà chua'), -- Fish and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, tomato-based sauce
    ('27450710', 'Cá và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền cà chua'), -- Fish and vegetables excluding carrots, broccoli, and dark- green leafy; no potatoes, tomato-based sauce
    ('27450740', 'Cá và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Fish and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, soy-based sauce
    ('27450750', 'Cá và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Fish and vegetables excluding  carrots, broccoli, and dark-green leafy; no potatoes, soy-based sauce
    ('27450760', 'Cá xiên nướng (shish kabob) với rau, trừ khoai tây'), -- Fish shish kabob with vegetables, excluding potatoes
    ('27451010', 'Cá chiên có sốt, kiểu Puerto Rico'), -- Fried fish with sauce, Puerto Rican style
    ('27451030', 'Tôm hùm có sốt, kiểu Puerto Rico'), -- Lobster with sauce, Puerto Rican style
    ('27451060', 'Salad bạch tuộc, kiểu Puerto Rico'), -- Octopus salad, Puerto Rican style
    ('27451070', 'Salad cá tuyết, kiểu Puerto Rico (Serenata)'), -- Codfish salad, Puerto Rican style, Serenata
    ('27460010', 'Món xào chow mein hoặc chop suey, không rõ loại thịt, không có mì sợi'), -- Chow mein or chop suey, NS as to type of meat, no noodles
    ('27460100', 'Món lau lau (Hawaii, thịt gói lá hấp)'), -- Lau lau
    ('27460510', 'Đĩa khai vị antipasto với giăm bông, cá, phô mai, rau'), -- Antipasto with ham, fish, cheese, vegetables
    ('27460710', 'Gan gà băm nhỏ với trứng và hành tây'), -- Livers, chicken, chopped, with eggs and onion
    ('27460750', 'Gan bò hoặc gan bê với hành tây'), -- Liver, beef or calves, and onions
    ('27462000', 'Lòng lợn hầm, kiểu Puerto Rico'), -- Stewed chitterlings, Puerto Rican style
    ('27463000', 'Mề gà hầm, kiểu Puerto Rico'), -- Stewed gizzards, Puerto Rican style
    ('27464000', 'Súp gumbo, không có cơm'), -- Gumbo, no rice
    ('27500050', 'Bánh mì kẹp, loại chung'), -- Sandwich, NFS
    ('27500300', 'Bánh cuốn wrap, loại chung'), -- Sandwich wrap, NFS
    ('27500400', 'Bánh mì kẹp thịt BBQ, loại chung'), -- Barbecue sandwich, NFS
    ('27510100', 'Bánh mì kẹp sloppy joe, bánh mì bun trắng'), -- Sloppy joe sandwich, on white bun
    ('27510105', 'Bánh mì kẹp sloppy joe, bánh mì bun lúa mì'), -- Sloppy joe sandwich, on wheat bun
    ('27510130', 'Bánh mì kẹp thịt bò BBQ, bánh mì bun trắng'), -- Barbecue beef sandwich, on white bun
    ('27510135', 'Bánh mì kẹp thịt bò BBQ, bánh mì bun lúa mì'), -- Barbecue beef sandwich, on wheat bun
    ('27510140', 'Bánh cheeseburger cỡ nhỏ (slider), đồ ăn nhanh'), -- Cheeseburger slider, from fast food
    ('27510155', 'Bánh cheeseburger, loại chung'), -- Cheeseburger, NFS
    ('27510160', 'Bánh cheeseburger, đồ ăn nhanh, 1 miếng chả nhỏ'), -- Cheeseburger, from fast food, 1 small patty
    ('27510170', 'Bánh cheeseburger (Burger King)'), -- Cheeseburger (Burger King)
    ('27510171', 'Bánh Whopper Jr kẹp phô mai (Burger King)'), -- Whopper Jr with cheese (Burger King)
    ('27510172', 'Bánh cheeseburger (McDonalds)'), -- Cheeseburger (McDonalds)
    ('27510190', 'Bánh cheeseburger, căng tin trường học'), -- Cheeseburger, from school cafeteria
    ('27510191', 'Bánh cheeseburger cỡ nhỏ (slider)'), -- Cheeseburger slider
    ('27510195', 'Bánh cheeseburger, bánh mì bun trắng, 1 miếng chả nhỏ'), -- Cheeseburger, on white bun, 1 small patty
    ('27510196', 'Bánh cheeseburger, bánh mì bun lúa mì, 1 miếng chả nhỏ'), -- Cheeseburger, on wheat bun, 1 small patty
    ('27510215', 'Bánh cheeseburger, đồ ăn nhanh, 1 miếng chả vừa'), -- Cheeseburger, from fast food, 1 medium patty
    ('27510229', 'Bánh Quarter Pounder (McDonalds)'), -- Quarter Pounder (McDonalds)
    ('27510231', 'Bánh Whopper kẹp phô mai (Burger King)'), -- Whopper with cheese (Burger King)
    ('27510232', 'Bánh Quarter Pounder kẹp phô mai (McDonalds)'), -- Quarter Pounder with cheese (McDonalds)
    ('27510241', 'Bánh cheeseburger, bánh mì bun trắng, 1 miếng chả vừa'), -- Cheeseburger, on white bun, 1 medium patty
    ('27510242', 'Bánh cheeseburger, bánh mì bun lúa mì, 1 miếng chả vừa'), -- Cheeseburger, on wheat bun, 1 medium patty
    ('27510245', 'Bánh cheeseburger, bánh mì bun trắng, 1 miếng chả lớn'), -- Cheeseburger, on white bun, 1 large patty
    ('27510246', 'Bánh cheeseburger, bánh mì bun lúa mì, 1 miếng chả lớn'), -- Cheeseburger, on wheat bun, 1 large patty
    ('27510254', 'Bánh cheeseburger kép, bánh mì bun trắng, 2 miếng chả nhỏ'), -- Double cheeseburger, on white bun, 2 small patties
    ('27510255', 'Bánh cheeseburger kép, bánh mì bun lúa mì, 2 miếng chả nhỏ'), -- Double cheeseburger, on wheat bun, 2 small patties
    ('27510257', 'Bánh cheeseburger kép, bánh mì bun trắng, 2 miếng chả vừa'), -- Double cheeseburger, on white bun, 2 medium patties
    ('27510258', 'Bánh cheeseburger kép, bánh mì bun lúa mì, 2 miếng chả vừa'), -- Double cheeseburger, on wheat bun, 2 medium patties
    ('27510261', 'Bánh cheeseburger, đồ ăn nhanh, 1 miếng chả lớn'), -- Cheeseburger, from fast food, 1 large patty
    ('27510262', 'Bánh cheeseburger kép, bánh mì bun trắng, 2 miếng chả lớn'), -- Double cheeseburger, on white bun, 2 large patties
    ('27510263', 'Bánh cheeseburger kép, bánh mì bun lúa mì, 2 miếng chả lớn'), -- Double cheeseburger, on wheat bun, 2 large patties
    ('27510371', 'Bánh cheeseburger kép, đồ ăn nhanh, 2 miếng chả nhỏ'), -- Double cheeseburger, from fast food, 2 small patties
    ('27510386', 'Bánh cheeseburger kép (Burger King)'), -- Double cheeseburger (Burger King)
    ('27510387', 'Bánh cheeseburger kép (McDonalds)'), -- Double cheeseburger (McDonalds)
    ('27510388', 'Bánh McDouble (McDonalds)'), -- McDouble (McDonalds)
    ('27510389', 'Bánh Big Mac (McDonalds)'), -- Big Mac (McDonalds)
    ('27510401', 'Bánh cheeseburger kép, đồ ăn nhanh, 2 miếng chả vừa'), -- Double cheeseburger, from fast food, 2 medium patties
    ('27510405', 'Bánh cheeseburger kép, đồ ăn nhanh, 2 miếng chả lớn'), -- Double cheeseburger, from fast food, 2 large patties
    ('27510501', 'Bánh hamburger cỡ nhỏ (slider), đồ ăn nhanh'), -- Hamburger slider, from fast food
    ('27510521', 'Bánh hamburger, loại chung'), -- Hamburger, NFS
    ('27510531', 'Bánh hamburger, đồ ăn nhanh, 1 miếng chả nhỏ'), -- Hamburger, from fast food, 1 small patty
    ('27510551', 'Bánh hamburger (Burger King)'), -- Hamburger (Burger King)
    ('27510552', 'Bánh Whopper Jr (Burger King)'), -- Whopper Jr (Burger King)
    ('27510553', 'Bánh hamburger (McDonalds)'), -- Hamburger (McDonalds)
    ('27510565', 'Bánh hamburger, căng tin trường học'), -- Hamburger, from school cafeteria
    ('27510573', 'Bánh hamburger cỡ nhỏ (slider)'), -- Hamburger slider
    ('27510575', 'Bánh hamburger, bánh mì bun trắng, 1 miếng chả nhỏ'), -- Hamburger, on white bun, 1 small patty
    ('27510576', 'Bánh hamburger, bánh mì bun lúa mì, 1 miếng chả nhỏ'), -- Hamburger, on wheat bun, 1 small patty
    ('27510601', 'Bánh hamburger, đồ ăn nhanh, 1 miếng chả vừa'), -- Hamburger, from fast food, 1 medium patty
    ('27510605', 'Bánh hamburger, đồ ăn nhanh, 1 miếng chả lớn'), -- Hamburger, from fast food, 1 large patty
    ('27510615', 'Bánh Whopper (Burger King)'), -- Whopper (Burger King)
    ('27510631', 'Bánh hamburger, bánh mì bun trắng, 1 miếng chả vừa'), -- Hamburger, on white bun, 1 medium patty
    ('27510632', 'Bánh hamburger, bánh mì bun lúa mì, 1 miếng chả vừa'), -- Hamburger, on wheat bun, 1 medium patty
    ('27510635', 'Bánh hamburger, bánh mì bun trắng, 1 miếng chả lớn'), -- Hamburger, on white bun, 1 large patty
    ('27510636', 'Bánh hamburger, bánh mì bun lúa mì, 1 miếng chả lớn'), -- Hamburger, on wheat bun, 1 large patty
    ('27510649', 'Bánh hamburger kép, bánh mì bun trắng, 2 miếng chả nhỏ'), -- Double hamburger, on white bun, 2 small patties
    ('27510652', 'Bánh hamburger kép, bánh mì bun lúa mì, 2 miếng chả nhỏ'), -- Double hamburger, on wheat bun, 2 small patties
    ('27510655', 'Bánh hamburger kép, bánh mì bun trắng, 2 miếng chả vừa'), -- Double hamburger, on white bun, 2 medium patties
    ('27510657', 'Bánh hamburger kép, bánh mì bun lúa mì, 2 miếng chả vừa'), -- Double hamburger, on wheat bun, 2 medium patties
    ('27510658', 'Bánh hamburger kép, bánh mì bun trắng, 2 miếng chả lớn'), -- Double hamburger, on white bun, 2 large patties
    ('27510659', 'Bánh hamburger kép, bánh mì bun lúa mì, 2 miếng chả lớn'), -- Double hamburger, on wheat bun, 2 large patties
    ('27510661', 'Bánh hamburger kép, đồ ăn nhanh, 2 miếng chả nhỏ'), -- Double hamburger, from fast food, 2 small patties
    ('27510671', 'Bánh hamburger kép, đồ ăn nhanh, 2 miếng chả vừa'), -- Double hamburger, from fast food, 2 medium patties
    ('27510675', 'Bánh hamburger kép, đồ ăn nhanh, 2 miếng chả lớn'), -- Double hamburger, from fast food, 2 large patties
    ('27510700', 'Bánh mì kẹp thịt viên (loại thường hoặc sub)'), -- Meatball sandwich or sub
    ('27510702', 'Bánh mì kẹp thịt xay nướng khối (meatloaf)'), -- Meatloaf sandwich
    ('27510705', 'Bánh chiliburger (hamburger sốt chili), có hoặc không có phô mai, kẹp bánh mì bun'), -- Chiliburger, with or without cheese, on bun
    ('27510910', 'Bánh mì kẹp bò muối (corned beef), bánh mì trắng'), -- Corned beef sandwich on white
    ('27510920', 'Bánh mì kẹp bò muối (corned beef), bánh mì trắng, có phô mai'), -- Corned beef sandwich on white, with cheese
    ('27510930', 'Bánh mì kẹp bò muối (corned beef), bánh mì lúa mì'), -- Corned beef sandwich on wheat
    ('27510940', 'Bánh mì kẹp bò muối (corned beef), bánh mì lúa mì, có phô mai'), -- Corned beef sandwich on wheat, with cheese
    ('27510950', 'Bánh mì kẹp Reuben'), -- Reuben sandwich
    ('27513010', 'Bánh mì kẹp thịt bò quay, bánh mì trắng'), -- Roast beef sandwich on white
    ('27513050', 'Bánh mì kẹp thịt bò quay, bánh mì trắng, có phô mai'), -- Roast beef sandwich on white, with cheese
    ('27513055', 'Bánh mì kẹp thịt bò quay, bánh mì lúa mì'), -- Roast beef sandwich on wheat
    ('27513065', 'Bánh mì kẹp thịt bò quay, bánh mì lúa mì, có phô mai'), -- Roast beef sandwich on wheat, with cheese
    ('27513070', 'Bánh mì kẹp French dip (thịt bò chấm nước dùng)'), -- French dip sandwich
    ('27514010', 'Bánh mì kẹp bít tết (loại thường hoặc sub), bánh mì trắng'), -- Steak sandwich or sub on white
    ('27514020', 'Bánh mì kẹp bít tết (loại thường hoặc sub), bánh mì lúa mì'), -- Steak sandwich or sub on wheat
    ('27514030', 'Bánh mì kẹp bít tết phô mai (loại thường hoặc sub), bánh mì trắng'), -- Cheese steak sandwich or sub on white
    ('27514040', 'Bánh mì kẹp bít tết phô mai (loại thường hoặc sub), bánh mì lúa mì'), -- Cheese steak sandwich or sub on wheat
    ('27516010', 'Bánh mì kẹp gyro'), -- Gyro sandwich
    ('27517000', 'Bánh cuốn wrap hamburger, đồ ăn nhanh'), -- Hamburger wrap sandwich, from fast food
    ('27520210', 'Bánh mì kẹp giăm bông, bánh mì trắng'), -- Ham sandwich on white
    ('27520220', 'Bánh mì kẹp giăm bông, bánh mì trắng, có phô mai'), -- Ham sandwich on white, with cheese
    ('27520230', 'Bánh mì kẹp giăm bông, bánh mì lúa mì'), -- Ham sandwich on wheat
    ('27520240', 'Bánh mì kẹp giăm bông, bánh mì lúa mì, có phô mai'), -- Ham sandwich on wheat, with cheese
    ('27520255', 'Bánh mì kẹp giăm bông (loại thường hoặc sub), nhà hàng'), -- Ham sandwich or sub, restaurant
    ('27520260', 'Bánh mì kẹp giăm bông (loại thường hoặc sub), có phô mai, nhà hàng'), -- Ham sandwich or sub, with cheese, restaurant
    ('27520270', 'Bánh cuốn wrap giăm bông'), -- Ham sandwich wrap
    ('27520340', 'Bánh mì kẹp salad giăm bông, bánh mì trắng'), -- Ham salad sandwich on white
    ('27520345', 'Bánh mì kẹp salad giăm bông, bánh mì lúa mì'), -- Ham salad sandwich on wheat
    ('27520410', 'Bánh mì kẹp kiểu Cuba'), -- Cuban sandwich
    ('27520500', 'Bánh mì kẹp sườn BBQ'), -- Barbecue rib sandwich
    ('27520510', 'Bánh mì kẹp thịt lợn BBQ, bánh mì bun trắng'), -- Barbecue pork sandwich, on white bun
    ('27520515', 'Bánh mì kẹp thịt lợn BBQ, bánh mì bun lúa mì'), -- Barbecue pork sandwich, on wheat bun
    ('27520520', 'Bánh mì kẹp thịt lợn'), -- Pork sandwich
    ('27520525', 'Bánh mì kẹp thịt lợn, có phô mai'), -- Pork sandwich, with cheese
    ('27520610', 'Bánh mì kẹp thịt xông khói, xà lách, cà chua (BLT), bánh mì trắng'), -- Bacon, lettuce, tomato sandwich on white
    ('27520620', 'Bánh mì kẹp thịt xông khói, xà lách, cà chua (BLT), bánh mì lúa mì'), -- Bacon, lettuce, tomato sandwich on wheat
    ('27540010', 'Bánh mì kẹp gà tây, bánh mì trắng'), -- Turkey sandwich on white
    ('27540020', 'Bánh mì kẹp gà tây, bánh mì trắng, có phô mai'), -- Turkey sandwich on white, with cheese
    ('27540030', 'Bánh mì kẹp gà tây, bánh mì lúa mì'), -- Turkey sandwich on wheat
    ('27540040', 'Bánh mì kẹp gà tây, bánh mì lúa mì, có phô mai'), -- Turkey sandwich on wheat, with cheese
    ('27540050', 'Bánh mì kẹp gà tây hoặc bánh mì kẹp dài (sub), nhà hàng'), -- Turkey sandwich or sub, restaurant
    ('27540060', 'Bánh mì kẹp gà tây hoặc bánh mì kẹp dài (sub), có phô mai, nhà hàng'), -- Turkey sandwich or sub, with cheese, restaurant
    ('27540070', 'Bánh cuốn wrap gà tây'), -- Turkey sandwich wrap
    ('27540080', 'Bánh mì kẹp thịt gà nguội (deli) hoặc bánh mì kẹp dài (sub), nhà hàng'), -- Chicken deli sandwich or sub, restaurant
    ('27540090', 'Bánh mì kẹp thịt gà nguội (deli) hoặc bánh mì kẹp dài (sub), có phô mai, nhà hàng'), -- Chicken deli sandwich or sub, with cheese, restaurant
    ('27540120', 'Bánh mì kẹp salad gà, bánh mì trắng'), -- Chicken salad sandwich on white
    ('27540121', 'Bánh mì kẹp salad gà, bánh mì lúa mì'), -- Chicken salad sandwich on wheat
    ('27540122', 'Bánh cuốn wrap salad gà'), -- Chicken salad sandwich wrap
    ('27540123', 'Bánh mì kẹp club, bánh mì trắng'), -- Club sandwich on white
    ('27540124', 'Bánh mì kẹp club, bánh mì trắng, có phô mai'), -- Club sandwich on white, with cheese
    ('27540125', 'Bánh mì kẹp club, bánh mì lúa mì'), -- Club sandwich on wheat
    ('27540126', 'Bánh mì kẹp club, bánh mì lúa mì, có phô mai'), -- Club sandwich on wheat, with cheese
    ('27540127', 'Bánh mì kẹp club hoặc bánh mì kẹp dài (sub), nhà hàng'), -- Club sandwich or sub, restaurant
    ('27540130', 'Bánh mì kẹp gà sốt BBQ, bánh bun trắng'), -- Barbecue chicken sandwich, on white bun
    ('27540131', 'Bánh mì kẹp gà sốt BBQ, bánh bun lúa mì'), -- Barbecue chicken sandwich, on wheat bun
    ('27540132', 'Bánh mì kẹp phi lê gà, loại chung'), -- Chicken fillet sandwich, NFS
    ('27540139', 'Bánh mì kẹp phi lê gà, từ căng tin trường học'), -- Chicken fillet sandwich, from school cafeteria
    ('27540145', 'Bánh biscuit kẹp phi lê gà, đồ ăn nhanh'), -- Chicken fillet biscuit, from fast food
    ('27540146', 'Bánh mì kẹp phi lê gà chiên, đồ ăn nhanh'), -- Chicken fillet sandwich, fried, from fast food
    ('27540147', 'Bánh mì kẹp phi lê gà chiên, đồ ăn nhanh, có phô mai'), -- Chicken fillet sandwich, fried, from fast food, with cheese
    ('27540152', 'Bánh mì kẹp phi lê gà nướng vỉ, đồ ăn nhanh'), -- Chicken fillet sandwich, grilled, from fast food
    ('27540153', 'Bánh mì kẹp phi lê gà nướng vỉ, đồ ăn nhanh, có phô mai'), -- Chicken fillet sandwich, grilled, from fast food, with cheese
    ('27540160', 'Bánh mì kẹp phi lê gà, không rõ chiên hay nướng vỉ, đồ ăn nhanh'), -- Chicken fillet sandwich, NS as to fried or grilled, from fast food
    ('27540175', 'Bánh mì kẹp phi lê gà chiên, bánh bun trắng'), -- Chicken fillet sandwich, fried, on white bun
    ('27540176', 'Bánh mì kẹp phi lê gà chiên, bánh bun trắng, có phô mai'), -- Chicken fillet sandwich, fried, on white bun; with cheese
    ('27540185', 'Bánh mì kẹp phi lê gà chiên, bánh bun lúa mì'), -- Chicken fillet sandwich, fried, on wheat bun
    ('27540186', 'Bánh mì kẹp phi lê gà chiên, bánh bun lúa mì, có phô mai'), -- Chicken fillet sandwich, fried, on wheat bun, with cheese
    ('27540195', 'Bánh mì kẹp phi lê gà nướng vỉ, bánh bun trắng'), -- Chicken fillet sandwich, grilled, on white bun
    ('27540196', 'Bánh mì kẹp phi lê gà nướng vỉ, bánh bun trắng, có phô mai'), -- Chicken fillet sandwich, grilled, on white bun, with cheese
    ('27540205', 'Bánh mì kẹp phi lê gà nướng vỉ, bánh bun lúa mì'), -- Chicken fillet sandwich, grilled, on wheat bun
    ('27540206', 'Bánh mì kẹp phi lê gà nướng vỉ, bánh bun lúa mì, có phô mai'), -- Chicken fillet sandwich, grilled, on wheat bun, with cheese
    ('27540210', 'Bánh cuốn wrap phi lê gà chiên, đồ ăn nhanh'), -- Chicken fillet wrap sandwich, fried, from fast food
    ('27540300', 'Bánh cuốn wrap phi lê gà nướng vỉ, đồ ăn nhanh'), -- Chicken fillet wrap sandwich, grilled, from fast food
    ('27545100', 'Bánh burger gà tây hoặc gà, bánh bun trắng'), -- Turkey or chicken burger, on white bun
    ('27545110', 'Bánh burger gà tây hoặc gà, bánh bun lúa mì'), -- Turkey or chicken burger, on wheat bun
    ('27550000', 'Bánh mì kẹp cá chiên, đồ ăn nhanh'), -- Fish sandwich, fried, from fast food
    ('27550100', 'Bánh mì kẹp cá chiên, đồ ăn nhanh, có phô mai'), -- Fish sandwich, fried, from fast food, with cheese
    ('27550110', 'Bánh mì kẹp chả cua (crab cake)'), -- Crab cake sandwich
    ('27550120', 'Bánh mì kẹp chả cá hồi'), -- Salmon cake sandwich
    ('27550150', 'Bánh mì kẹp hải sản chiên'), -- Fried seafood sandwich
    ('27550200', 'Bánh mì kẹp cá, từ căng tin trường học'), -- Fish sandwich, from school cafeteria
    ('27550300', 'Bánh mì kẹp cá, loại chung'), -- Fish sandwich, NFS
    ('27550400', 'Bánh mì kẹp cá chiên, bánh bun trắng'), -- Fish sandwich, fried, on white bun
    ('27550405', 'Bánh mì kẹp cá chiên, bánh bun trắng, có phô mai'), -- Fish sandwich, fried, on white bun, with cheese
    ('27550410', 'Bánh mì kẹp cá chiên, bánh bun lúa mì'), -- Fish sandwich, fried, on wheat bun
    ('27550415', 'Bánh mì kẹp cá chiên, bánh bun lúa mì, có phô mai'), -- Fish sandwich, fried, on wheat bun, with cheese
    ('27550420', 'Bánh mì kẹp cá nướng vỉ'), -- Fish sandwich, grilled
    ('27550425', 'Bánh cuốn wrap cá'), -- Fish wrap sandwich
    ('27550510', 'Bánh mì kẹp cá mòi'), -- Sardine sandwich
    ('27550720', 'Bánh mì kẹp salad cá ngừ, bánh mì trắng'), -- Tuna salad sandwich on white
    ('27550730', 'Bánh mì kẹp salad cá ngừ, bánh mì trắng, có phô mai'), -- Tuna salad sandwich on white, with cheese
    ('27550735', 'Bánh mì kẹp salad cá ngừ, bánh mì lúa mì'), -- Tuna salad sandwich on wheat
    ('27550737', 'Bánh mì kẹp salad cá ngừ, bánh mì lúa mì, có phô mai'), -- Tuna salad sandwich on wheat, with cheese
    ('27550755', 'Bánh cuốn wrap salad cá ngừ'), -- Tuna salad sandwich wrap
    ('27550800', 'Bánh mì kẹp salad hải sản'), -- Seafood salad sandwich
    ('27560130', 'Bánh mì kẹp xúc xích bologna, bánh mì trắng'), -- Bologna sandwich on white
    ('27560140', 'Bánh mì kẹp xúc xích bologna, bánh mì trắng, có phô mai'), -- Bologna sandwich on white, with cheese
    ('27560150', 'Bánh mì kẹp xúc xích bologna, bánh mì lúa mì'), -- Bologna sandwich on wheat
    ('27560160', 'Bánh mì kẹp xúc xích bologna, bánh mì lúa mì, có phô mai'), -- Bologna sandwich on wheat, with cheese
    ('27560300', 'Xúc xích tẩm bột ngô chiên (corn dog)'), -- Corn dog
    ('27560350', 'Xúc xích cuộn bánh (pig in a blanket)'), -- Pig in a blanket
    ('27560520', 'Bánh mì kẹp salami, bánh mì trắng'), -- Salami sandwich on white
    ('27560530', 'Bánh mì kẹp salami, bánh mì trắng, có phô mai'), -- Salami sandwich on white, with cheese
    ('27560540', 'Bánh mì kẹp salami, bánh mì lúa mì'), -- Salami sandwich on wheat
    ('27560550', 'Bánh mì kẹp salami, bánh mì lúa mì, có phô mai'), -- Salami sandwich on wheat, with cheese
    ('27560610', 'Bánh mì kẹp kiểu Ý, bánh mì trắng'), -- Italian sandwich on white
    ('27560620', 'Bánh mì kẹp kiểu Ý, bánh mì lúa mì'), -- Italian sandwich on wheat
    ('27560630', 'Bánh mì kẹp kiểu Ý hoặc bánh mì kẹp dài (sub), nhà hàng'), -- Italian sandwich or sub, restaurant
    ('27560710', 'Bánh mì kẹp xúc xích Ý, bánh mì trắng'), -- Italian sausage sandwich on white
    ('27560715', 'Bánh mì kẹp xúc xích Ý, bánh mì lúa mì'), -- Italian sausage sandwich on wheat
    ('27560920', 'Bánh mì kẹp thịt hộp Spam'), -- Spam sandwich
    ('27564000', 'Bánh mì kẹp xúc xích (hot dog), loại chung, bánh bun trắng'), -- Hot dog sandwich, NFS, on white bun
    ('27564001', 'Bánh mì kẹp xúc xích (hot dog), loại chung, bánh bun lúa mì'), -- Hot dog sandwich, NFS, on wheat bun
    ('27564010', 'Bánh mì kẹp xúc xích (hot dog), loại chung, bánh mì trắng'), -- Hot dog sandwich, NFS, on white bread
    ('27564020', 'Bánh mì kẹp xúc xích (hot dog), loại chung, bánh mì lúa mì'), -- Hot dog sandwich, NFS, on wheat bread
    ('27564060', 'Bánh mì kẹp xúc xích bò (hot dog), bánh bun trắng'), -- Hot dog sandwich, beef, on white bun
    ('27564061', 'Bánh mì kẹp xúc xích bò (hot dog), bánh bun lúa mì'), -- Hot dog sandwich, beef, on wheat bun
    ('27564070', 'Bánh mì kẹp xúc xích bò (hot dog), bánh mì trắng'), -- Hot dog sandwich, beef, on white bread
    ('27564080', 'Bánh mì kẹp xúc xích bò (hot dog), bánh mì lúa mì'), -- Hot dog sandwich, beef, on wheat bread
    ('27564180', 'Bánh mì kẹp xúc xích thịt và thịt gia cầm (hot dog), bánh bun trắng'), -- Hot dog sandwich, meat and poultry, on white bun
    ('27564181', 'Bánh mì kẹp xúc xích thịt và thịt gia cầm (hot dog), bánh bun lúa mì'), -- Hot dog sandwich, meat and poultry, on wheat bun
    ('27564190', 'Bánh mì kẹp xúc xích thịt và thịt gia cầm (hot dog), bánh mì trắng'), -- Hot dog sandwich, meat and poultry, on white bread
    ('27564200', 'Bánh mì kẹp xúc xích thịt và thịt gia cầm (hot dog), bánh mì lúa mì'), -- Hot dog sandwich, meat and poultry, on wheat bread
    ('27564240', 'Bánh mì kẹp xúc xích gà tây (hot dog), bánh bun trắng'), -- Hot dog sandwich, turkey, on white bun
    ('27564241', 'Bánh mì kẹp xúc xích gà tây (hot dog), bánh bun lúa mì'), -- Hot dog sandwich, turkey, on wheat bun
    ('27564250', 'Bánh mì kẹp xúc xích gà tây (hot dog), bánh mì trắng'), -- Hot dog sandwich, turkey, on white bread
    ('27564260', 'Bánh mì kẹp xúc xích gà tây (hot dog), bánh mì lúa mì'), -- Hot dog sandwich, turkey, on wheat bread
    ('27564300', 'Bánh mì kẹp xúc xích (hot dog), giảm béo, bánh bun trắng'), -- Hot dog sandwich, reduced fat, on white bun
    ('27564301', 'Bánh mì kẹp xúc xích (hot dog), giảm béo, bánh bun lúa mì'), -- Hot dog sandwich, reduced fat, on wheat bun
    ('27564310', 'Bánh mì kẹp xúc xích (hot dog), giảm béo, bánh mì trắng'), -- Hot dog sandwich, reduced fat, on white bread
    ('27564320', 'Bánh mì kẹp xúc xích (hot dog), giảm béo, bánh mì lúa mì'), -- Hot dog sandwich, reduced fat, on wheat bread
    ('27564420', 'Bánh mì kẹp xúc xích chay (hot dog), bánh bun'), -- Hot dog sandwich, vegetarian, on bun
    ('27564430', 'Bánh mì kẹp xúc xích chay (hot dog), bánh mì'), -- Hot dog sandwich, vegetarian, on bread
    ('27564440', 'Bánh mì kẹp xúc xích (hot dog) sốt chili, bánh bun trắng'), -- Chili hot dog sandwich, on white bun
    ('27564441', 'Bánh mì kẹp xúc xích (hot dog) sốt chili, bánh bun lúa mì'), -- Chili hot dog sandwich, on wheat bun
    ('27564450', 'Bánh mì kẹp xúc xích (hot dog) sốt chili, bánh mì trắng'), -- Chili hot dog sandwich, on white bread
    ('27564460', 'Bánh mì kẹp xúc xích (hot dog) sốt chili, bánh mì lúa mì'), -- Chili hot dog sandwich, on wheat bread
    ('27580010', 'Bánh mì kẹp nhiều loại thịt, bánh mì trắng'), -- Multiple meat sandwich on white
    ('27580020', 'Bánh mì kẹp nhiều loại thịt, bánh mì trắng, có phô mai'), -- Multiple meat sandwich on white, with cheese
    ('27580030', 'Bánh mì kẹp nhiều loại thịt, bánh mì lúa mì'), -- Multiple meat sandwich on wheat
    ('27580040', 'Bánh mì kẹp nhiều loại thịt, bánh mì lúa mì, có phô mai'), -- Multiple meat sandwich on wheat, with cheese
    ('27580050', 'Bánh mì kẹp nhiều loại thịt hoặc bánh mì kẹp dài (sub), nhà hàng'), -- Multiple meat sandwich or sub, restaurant
    ('27580060', 'Bánh mì kẹp nhiều loại thịt hoặc bánh mì kẹp dài (sub), có phô mai, nhà hàng'), -- Multiple meat sandwich or sub, with cheese, restaurant
    ('27580070', 'Bánh cuốn wrap nhiều loại thịt'), -- Multiple meat sandwich wrap
    ('27580110', 'Bánh mì kẹp gà tây và giăm bông, bánh mì trắng'), -- Turkey and ham sandwich on white
    ('27580120', 'Bánh mì kẹp gà tây và giăm bông, bánh mì trắng, có phô mai'), -- Turkey and ham sandwich on white, with cheese
    ('27580130', 'Bánh mì kẹp gà tây và giăm bông, bánh mì lúa mì'), -- Turkey and ham sandwich on wheat
    ('27580140', 'Bánh mì kẹp gà tây và giăm bông, bánh mì lúa mì, có phô mai'), -- Turkey and ham sandwich on wheat, with cheese
    ('27580150', 'Bánh mì kẹp gà tây và giăm bông hoặc bánh mì kẹp dài (sub), nhà hàng'), -- Turkey and ham sandwich or sub, restaurant
    ('27580160', 'Bánh mì kẹp gà tây và giăm bông hoặc bánh mì kẹp dài (sub), có phô mai, nhà hàng'), -- Turkey and ham sandwich or sub, with cheese, restaurant
    ('28101000', 'Suất ăn đông lạnh, loại chung'), -- Frozen dinner, NFS
    ('28110000', 'Suất ăn thịt bò đông lạnh, loại chung'), -- Beef dinner, NFS, frozen meal
    ('28110150', 'Thịt bò với rau, suất ăn đông lạnh ăn kiêng'), -- Beef with vegetable, diet frozen meal
    ('28110300', 'Suất ăn bít tết Salisbury đông lạnh, loại chung'), -- Salisbury steak dinner, NFS, frozen meal
    ('28110310', 'Bít tết Salisbury với nước sốt gravy, khoai tây, rau, suất ăn đông lạnh'), -- Salisbury steak with gravy, potatoes, vegetable, frozen meal
    ('28110330', 'Bít tết Salisbury với nước sốt gravy, khoai tây nghiền đánh bông, rau, món tráng miệng, suất ăn đông lạnh'), -- Salisbury steak with gravy, whipped potatoes, vegetable, dessert, frozen meal
    ('28110380', 'Bít tết Salisbury với nước sốt gravy, nui phô mai, suất ăn đông lạnh'), -- Salisbury steak with gravy, macaroni and cheese, frozen meal
    ('28110510', 'Thịt bò thái lát với nước sốt gravy, khoai tây, rau, suất ăn đông lạnh'), -- Beef, sliced, with gravy, potatoes, vegetable, frozen meal
    ('28110660', 'Thịt viên kiểu Thụy Điển sốt gravy, với mì sợi, suất ăn đông lạnh ăn kiêng'), -- Meatballs, Swedish, in gravy, with noodles, diet frozen meal
    ('28113140', 'Thịt bò với mì spaetzle hoặc cơm, rau, suất ăn đông lạnh'), -- Beef with spaetzle or rice, vegetable, frozen meal
    ('28140100', 'Suất ăn gà đông lạnh, loại chung'), -- Chicken dinner, NFS, frozen meal
    ('28140710', 'Gà chiên với khoai tây, rau, suất ăn đông lạnh'), -- Chicken, fried, with potatoes, vegetable, frozen meal
    ('28140720', 'Miếng chả gà (patty) hoặc miếng nugget gà, không xương, tẩm bột chiên xù, khoai tây, rau, suất ăn đông lạnh'), -- Chicken patty, or nuggets, boneless, breaded, potatoes, vegetable, frozen meal
    ('28141010', 'Gà chiên với khoai tây, rau, món tráng miệng, suất ăn đông lạnh, phần thịt lớn'), -- Chicken, fried, with potatoes, vegetable, dessert, frozen meal, large meat portion
    ('28141201', 'Gà sốt teriyaki với cơm và rau, suất ăn đông lạnh ăn kiêng'), -- Teriyaki chicken with rice and vegetable, diet frozen meal
    ('28141250', 'Gà với cơm và rau, suất ăn đông lạnh ăn kiêng'), -- Chicken with rice and vegetable, diet frozen meal
    ('28141650', 'Gà và rau đút lò phủ phô mai (au gratin) với cơm, món chính đông lạnh ăn kiêng'), -- Chicken and vegetables au gratin with rice, diet frozen entree
    ('28143020', 'Món chính gà và rau với cơm, suất ăn đông lạnh ăn kiêng'), -- Chicken and vegetable entree with rice, diet frozen meal
    ('28143080', 'Gà với mì sợi và sốt phô mai, suất ăn đông lạnh ăn kiêng'), -- Chicken with noodles and cheese sauce, diet frozen meal
    ('28143150', 'Món chính gà và rau với mì sợi, suất ăn đông lạnh ăn kiêng'), -- Chicken and vegetable entree with noodles, diet frozen meal
    ('28143200', 'Gà sốt nền nước tương, cơm và rau, suất ăn đông lạnh'), -- Chicken in soy-based sauce, rice and vegetables, frozen meal
    ('28144100', 'Món chính gà và rau với mì sợi và sốt kem, suất ăn đông lạnh'), -- Chicken and vegetable entree with noodles and cream sauce, frozen meal
    ('28145110', 'Gà tây với rau, nhân nhồi (stuffing), suất ăn đông lạnh ăn kiêng'), -- Turkey with vegetable, stuffing, diet frozen meal
    ('28145210', 'Gà tây với nước sốt gravy, nhân nhồi bánh mì (dressing), khoai tây, rau, suất ăn đông lạnh'), -- Turkey with gravy, dressing, potatoes, vegetable, frozen meal
    ('28150510', 'Cá sốt bơ chanh với món tinh bột, rau, suất ăn đông lạnh'), -- Fish in lemon-butter sauce with starch item, vegetable, frozen meal
    ('28154010', 'Tôm và rau sốt với mì sợi, suất ăn đông lạnh ăn kiêng'), -- Shrimp and vegetables in sauce with noodles, diet frozen meal
    ('28160300', 'Suất ăn thịt xay nướng khối (meatloaf) đông lạnh, loại chung'), -- Meat loaf dinner, NFS, frozen meal
    ('28160310', 'Thịt xay nướng khối (meatloaf) với khoai tây, rau, suất ăn đông lạnh'), -- Meat loaf with potatoes, vegetable, frozen meal
    ('28310230', 'Súp thịt viên'), -- Soup, meatball
    ('28310330', 'Phở có thịt'), -- Soup, pho, with meat
    ('28310335', 'Phở không thịt'), -- Soup, pho, no meat
    ('28311010', 'Súp pepperpot'), -- Soup, pepperpot
    ('28315050', 'Súp thịt bò, đóng hộp'), -- Soup, beef, canned
    ('28315140', 'Súp bò (sopa hoặc caldo de res)'), -- Soup, sopa or caldo de res
    ('28315150', 'Súp pozole'), -- Soup, pozole
    ('28315160', 'Súp đám cưới kiểu Ý (Italian wedding)'), -- Soup, Italian wedding
    ('28320160', 'Súp thịt lợn hoặc giăm bông'), -- Soup, pork or ham
    ('28340120', 'Súp nước dùng'), -- Soup, broth
    ('28340600', 'Súp gà, đóng hộp'), -- Soup, chicken, canned
    ('28340660', 'Súp gà'), -- Soup, chicken
    ('28340670', 'Súp gà (sopa hoặc caldo de pollo)'), -- Soup, sopa or caldo de pollo
    ('28340750', 'Súp chua cay'), -- Soup, hot and sour
    ('28345110', 'Súp kem gà'), -- Soup, cream of chicken
    ('28350220', 'Súp chowder nghêu kiểu Manhattan'), -- Soup, Manhattan clam chowder
    ('28355110', 'Súp chowder nghêu kiểu New England'), -- Soup, New England clam chowder
    ('28355250', 'Súp bisque'), -- Soup, bisque
    ('28355260', 'Súp gumbo tôm hùm'), -- Lobster gumbo
    ('28355440', 'Súp gumbo tôm'), -- Shrimp gumbo
    ('28355480', 'Súp cá hoặc tôm'), -- Soup, fish or shrimp
    ('28500000', 'Nước sốt gravy gia cầm'), -- Gravy, poultry
    ('28500040', 'Nước sốt gravy thịt bò'), -- Gravy, beef
    ('28501010', 'Nước sốt gravy thịt bò, không béo'), -- Gravy, beef, fat free
    ('28501110', 'Nước sốt gravy gia cầm, không béo'), -- Gravy, poultry, fat free
    ('28520000', 'Nước sốt gravy, nấu với nước tương'), -- Gravy, made with soy sauce
    ('28520010', 'Nước sốt gravy, loại chung'), -- Gravy, NFS
    ('28520100', 'Dầu hào'), -- Oyster sauce
    ('28522000', 'Sốt mole (Mexico)'), -- Mole sauce
    ('31101010', 'Trứng nguyên quả, sống'), -- Egg, whole, raw
    ('31102000', 'Trứng nguyên quả, nấu chín, không rõ cách chế biến'), -- Egg, whole, cooked, NS as to cooking method
    ('31103010', 'Trứng nguyên quả, luộc hoặc chần'), -- Egg, whole, boiled or poached
    ('31105005', 'Trứng nguyên quả chiên, không rõ có thêm chất béo'), -- Egg, whole, fried, NS as to fat
    ('31105010', 'Trứng nguyên quả chiên, không thêm chất béo'), -- Egg, whole, fried no added fat
    ('31105020', 'Trứng nguyên quả chiên với bơ thực vật'), -- Egg, whole, fried with margarine
    ('31105030', 'Trứng nguyên quả chiên với dầu'), -- Egg, whole, fried with oil
    ('31105040', 'Trứng nguyên quả chiên với bơ'), -- Egg, whole, fried with butter
    ('31105060', 'Trứng nguyên quả chiên với mỡ động vật hoặc mỡ chảy từ thịt'), -- Egg, whole, fried with animal fat or meat drippings
    ('31105080', 'Trứng nguyên quả chiên với dầu xịt chống dính'), -- Egg, whole, fried with cooking spray
    ('31105085', 'Trứng nguyên quả chiên, không rõ loại chất béo'), -- Egg, whole, fried, NS as to fat type
    ('31105090', 'Trứng nguyên quả chiên, đồ ăn nhanh/nhà hàng'), -- Egg, whole, fried, from fast food / restaurant
    ('31106000', 'Trứng nguyên quả nướng lò, không rõ có thêm chất béo'), -- Egg, whole, baked, NS as to fat
    ('31106010', 'Trứng nguyên quả nướng lò, không thêm chất béo'), -- Egg, whole, baked, no added fat
    ('31106020', 'Trứng nguyên quả nướng lò, có thêm chất béo'), -- Egg, whole, baked, fat added
    ('31107000', 'Trứng nguyên quả ngâm chua'), -- Egg, whole, pickled
    ('31108010', 'Lòng trắng trứng, sống'), -- Egg, white only, raw
    ('31108100', 'Lòng trắng trứng nấu chín, không rõ có thêm chất béo'), -- Egg, white, cooked, NS as to fat
    ('31108110', 'Lòng trắng trứng nấu chín, không thêm chất béo'), -- Egg, white, cooked, no added fat
    ('31108120', 'Lòng trắng trứng nấu chín, có thêm chất béo'), -- Egg, white, cooked, fat added
    ('31110010', 'Lòng đỏ trứng, sống'), -- Egg, yolk only, raw
    ('31111000', 'Lòng đỏ trứng nấu chín, không rõ có thêm chất béo'), -- Egg, yolk only, cooked, NS as to fat
    ('31111010', 'Lòng đỏ trứng nấu chín, không thêm chất béo'), -- Egg, yolk only, cooked, no added fat
    ('31111020', 'Lòng đỏ trứng nấu chín, có thêm chất béo'), -- Egg, yolk only, cooked, fat added
    ('31201000', 'Trứng vịt, nấu chín'), -- Duck egg, cooked
    ('31202000', 'Trứng ngỗng, nấu chín'), -- Goose egg, cooked
    ('31203000', 'Trứng cút, đóng hộp'), -- Quail egg, canned
    ('32101000', 'Trứng sốt kem'), -- Egg, creamed
    ('32101500', 'Trứng Benedict'), -- Egg, Benedict
    ('32102000', 'Trứng nhồi sốt (deviled egg)'), -- Egg, deviled
    ('32103000', 'Salad trứng, trộn sốt mayonnaise'), -- Egg salad, made with mayonnaise
    ('32103015', 'Salad trứng, trộn sốt mayonnaise loại nhẹ'), -- Egg salad, made with light mayonnaise
    ('32103020', 'Salad trứng, trộn sốt trộn salad kiểu mayonnaise'), -- Egg salad, made with mayonnaise-type salad dressing
    ('32103025', 'Salad trứng, trộn sốt trộn salad kiểu mayonnaise loại nhẹ'), -- Egg salad, made with light mayonnaise-type salad dressing
    ('32103030', 'Salad trứng, trộn sốt trộn salad dạng kem'), -- Egg salad, made with creamy dressing
    ('32103035', 'Salad trứng, trộn sốt trộn salad dạng kem loại nhẹ'), -- Egg salad, made with light creamy dressing
    ('32103040', 'Salad trứng, trộn sốt trộn kiểu Ý'), -- Egg salad, made with Italian dressing
    ('32103045', 'Salad trứng, trộn sốt trộn kiểu Ý loại nhẹ'), -- Egg salad, made with light Italian dressing
    ('32103050', 'Salad trứng, trộn bất kỳ loại sốt trộn salad không béo nào'), -- Egg Salad, made with any type of fat free dressing
    ('32105180', 'Trứng kiểu nông trại Mexico (huevos rancheros)'), -- Huevos rancheros
    ('32105190', 'Món đút lò (casserole) trứng với bánh mì, phô mai, sữa và thịt'), -- Egg casserole with bread, cheese, milk and meat
    ('32105200', 'Trứng chiên phù dung (egg foo yung), loại chung'), -- Egg foo yung, NFS
    ('32105210', 'Trứng chiên phù dung (egg foo yung) nhân gà'), -- Chicken egg foo yung
    ('32105220', 'Trứng chiên phù dung (egg foo yung) nhân thịt lợn'), -- Pork egg foo yung
    ('32105230', 'Trứng chiên phù dung (egg foo yung) nhân tôm'), -- Shrimp egg foo yung
    ('32105240', 'Trứng chiên phù dung (egg foo yung) nhân thịt bò'), -- Beef egg foo yung
    ('32129990', 'Trứng tráng (omelet) hoặc trứng bác, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, NS as to fat
    ('32130000', 'Trứng tráng (omelet) hoặc trứng bác, nấu với bơ thực vật'), -- Egg omelet or scrambled egg, made with margarine
    ('32130010', 'Trứng tráng (omelet) hoặc trứng bác, nấu với dầu'), -- Egg omelet or scrambled egg, made with oil
    ('32130020', 'Trứng tráng (omelet) hoặc trứng bác, nấu với bơ'), -- Egg omelet or scrambled egg, made with butter
    ('32130040', 'Trứng tráng (omelet) hoặc trứng bác, nấu với mỡ động vật hoặc mỡ chảy từ thịt'), -- Egg omelet or scrambled egg, made with animal fat or meat drippings
    ('32130060', 'Trứng tráng (omelet) hoặc trứng bác, nấu với dầu xịt chống dính'), -- Egg omelet or scrambled egg, made with cooking spray
    ('32130065', 'Trứng tráng (omelet) hoặc trứng bác, không rõ loại chất béo'), -- Egg omelet or scrambled egg, NS as to fat type
    ('32130070', 'Trứng tráng (omelet) hoặc trứng bác, không thêm chất béo'), -- Egg omelet or scrambled egg, no added fat
    ('32130080', 'Trứng tráng (omelet) hoặc trứng bác, đồ ăn nhanh/nhà hàng'), -- Egg omelet or scrambled egg, from fast food / restaurant
    ('32130100', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, nấu với bơ thực vật'), -- Egg omelet or scrambled egg, with cheese, made with margarine
    ('32130110', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, nấu với dầu'), -- Egg omelet or scrambled egg, with cheese, made with oil
    ('32130120', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, nấu với bơ'), -- Egg omelet or scrambled egg, with cheese, made with butter
    ('32130140', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, nấu với mỡ động vật hoặc mỡ chảy từ thịt'), -- Egg omelet or scrambled egg, with cheese, made with animal fat or meat drippings
    ('32130160', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, nấu với dầu xịt chống dính'), -- Egg omelet or scrambled egg, with cheese, made with cooking spray
    ('32130170', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, không thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, no added fat
    ('32130190', 'Trứng tráng (omelet) hoặc trứng bác, có thịt, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with meat, NS as to fat
    ('32130200', 'Trứng tráng (omelet) hoặc trứng bác, có thịt, nấu với bơ thực vật'), -- Egg omelet or scrambled egg, with meat, made with margarine
    ('32130210', 'Trứng tráng (omelet) hoặc trứng bác, có thịt, nấu với dầu'), -- Egg omelet or scrambled egg, with meat, made with oil
    ('32130220', 'Trứng tráng (omelet) hoặc trứng bác, có thịt, nấu với bơ'), -- Egg omelet or scrambled egg, with meat, made with butter
    ('32130240', 'Trứng tráng (omelet) hoặc trứng bác, có thịt, nấu với mỡ động vật hoặc mỡ chảy từ thịt'), -- Egg omelet or scrambled egg, with meat, made with animal fat or meat drippings
    ('32130260', 'Trứng tráng (omelet) hoặc trứng bác, có thịt, nấu với dầu xịt chống dính'), -- Egg omelet or scrambled egg, with meat, made with cooking spray
    ('32130265', 'Trứng tráng (omelet) hoặc trứng bác, có thịt, không rõ loại chất béo'), -- Egg omelet or scrambled egg, with meat, NS as to fat type
    ('32130270', 'Trứng tráng (omelet) hoặc trứng bác, có thịt, không thêm chất béo'), -- Egg omelet or scrambled egg, with meat, no added fat
    ('32130290', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và thịt, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese and meat, NS as to fat
    ('32130300', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và thịt, nấu với bơ thực vật'), -- Egg omelet or scrambled egg, with cheese and meat, made with margarine
    ('32130310', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và thịt, nấu với dầu'), -- Egg omelet or scrambled egg, with cheese and meat, made with oil
    ('32130320', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và thịt, nấu với bơ'), -- Egg omelet or scrambled egg, with cheese and meat, made with butter
    ('32130340', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và thịt, nấu với mỡ động vật hoặc mỡ chảy từ thịt'), -- Egg omelet or scrambled egg, with cheese and meat, made with animal fat or meat drippings
    ('32130360', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và thịt, nấu với dầu xịt chống dính'), -- Egg omelet or scrambled egg, with cheese and meat, made with cooking spray
    ('32130365', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và thịt, không rõ loại chất béo'), -- Egg omelet or scrambled egg, with cheese and meat, NS as to fat type
    ('32130370', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và thịt, không thêm chất béo'), -- Egg omelet or scrambled egg, with cheese and meat, no added fat
    ('32130400', 'Trứng tráng (omelet) hoặc trứng bác, có cà chua, có thêm chất béo'), -- Egg omelet or scrambled egg, with tomatoes, fat added
    ('32130410', 'Trứng tráng (omelet) hoặc trứng bác, có cà chua, không thêm chất béo'), -- Egg omelet or scrambled egg, with tomatoes, no added fat
    ('32130420', 'Trứng tráng (omelet) hoặc trứng bác, có cà chua, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with tomatoes, NS as to fat
    ('32130430', 'Trứng tráng (omelet) hoặc trứng bác, có rau xanh đậm, có thêm chất béo'), -- Egg omelet or scrambled egg, with dark-green vegetables, fat added
    ('32130440', 'Trứng tráng (omelet) hoặc trứng bác, có rau xanh đậm, không thêm chất béo'), -- Egg omelet or scrambled egg, with dark-green vegetables, no added fat
    ('32130450', 'Trứng tráng (omelet) hoặc trứng bác, có rau xanh đậm, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with dark-green vegetables, NS as to fat
    ('32130460', 'Trứng tráng (omelet) hoặc trứng bác, có cà chua và rau xanh đậm, có thêm chất béo'), -- Egg omelet or scrambled egg, with tomatoes and dark-green vegetables, fat added
    ('32130470', 'Trứng tráng (omelet) hoặc trứng bác, có cà chua và rau xanh đậm, không thêm chất béo'), -- Egg omelet or scrambled egg, with tomatoes and dark-green vegetables, no fat added
    ('32130480', 'Trứng tráng (omelet) hoặc trứng bác, có cà chua và rau xanh đậm, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with tomatoes and dark-green vegetables, NS as to fat
    ('32130490', 'Trứng tráng (omelet) hoặc trứng bác, có rau khác (trừ rau xanh đậm và/hoặc cà chua), có thêm chất béo'), -- Egg omelet or scrambled egg, with vegetables other than dark green and/or tomatoes, fat added
    ('32130500', 'Trứng tráng (omelet) hoặc trứng bác, có rau khác (trừ rau xanh đậm và/hoặc cà chua), không thêm chất béo'), -- Egg omelet or scrambled egg, with vegetables other than dark green and/or tomatoes, no added fat
    ('32130510', 'Trứng tráng (omelet) hoặc trứng bác, có rau khác (trừ rau xanh đậm và/hoặc cà chua), không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with vegetables other than dark green and/or tomatoes, NS as to fat
    ('32130600', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và cà chua, có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese and tomatoes, fat added
    ('32130610', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và cà chua, không thêm chất béo'), -- Egg omelet or scrambled egg, with cheese and tomatoes, no added fat
    ('32130620', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và cà chua, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese and tomatoes, NS as to fat
    ('32130630', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và rau xanh đậm, có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese and dark-green vegetables, fat added
    ('32130640', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và rau xanh đậm, không thêm chất béo'), -- Egg omelet or scrambled egg, with cheese and dark-green vegetables, no added fat
    ('32130650', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và rau xanh đậm, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese and dark-green vegetables, NS as to fat
    ('32130660', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, cà chua và rau xanh đậm, có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, tomatoes, and dark-green vegetables, fat added
    ('32130670', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, cà chua và rau xanh đậm, không thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, tomatoes, and dark-green vegetables, no added fat
    ('32130680', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, cà chua và rau xanh đậm, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, tomatoes, and dark-green vegetables, NS as to fat
    ('32130690', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và rau khác (trừ rau xanh đậm và/hoặc cà chua), có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese and vegetables other than dark green and/or tomatoes, fat added
    ('32130700', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và rau khác (trừ rau xanh đậm và/hoặc cà chua), không thêm chất béo'), -- Egg omelet or scrambled egg, with cheese and vegetables other than dark green and/or tomatoes, no added fat
    ('32130710', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai và rau khác (trừ rau xanh đậm và/hoặc cà chua), không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese and vegetables other than dark green and/or tomatoes, NS as to fat
    ('32130800', 'Trứng tráng (omelet) hoặc trứng bác, có thịt và cà chua, có thêm chất béo'), -- Egg omelet or scrambled egg, with meat and tomatoes, fat added
    ('32130810', 'Trứng tráng (omelet) hoặc trứng bác, có thịt và cà chua, không thêm chất béo'), -- Egg omelet or scrambled egg, with meat and tomatoes, no added fat
    ('32130820', 'Trứng tráng (omelet) hoặc trứng bác, có thịt và cà chua, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with meat and tomatoes, NS as to fat
    ('32130830', 'Trứng tráng (omelet) hoặc trứng bác, có thịt và rau xanh đậm, có thêm chất béo'), -- Egg omelet or scrambled egg, with meat and dark-green vegetables, fat added
    ('32130840', 'Trứng tráng (omelet) hoặc trứng bác, có thịt và rau xanh đậm, không thêm chất béo'), -- Egg omelet or scrambled egg, with meat and dark-green vegetables, no added fat
    ('32130850', 'Trứng tráng (omelet) hoặc trứng bác, có thịt và rau xanh đậm, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with meat and dark-green vegetables, NS as to fat
    ('32130860', 'Trứng tráng (omelet) hoặc trứng bác, có thịt, cà chua và rau xanh đậm, có thêm chất béo'), -- Egg omelet or scrambled egg, with meat, tomatoes, and dark-green vegetables, fat added
    ('32130870', 'Trứng tráng (omelet) hoặc trứng bác, có thịt, cà chua và rau xanh đậm, không thêm chất béo'), -- Egg omelet or scrambled egg, with meat, tomatoes, and dark-green vegetables, no added fat
    ('32130880', 'Trứng tráng (omelet) hoặc trứng bác, có thịt, cà chua và rau xanh đậm, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with meat, tomatoes, and dark-green vegetables, NS as to fat
    ('32130890', 'Trứng tráng (omelet) hoặc trứng bác, có thịt và rau khác (trừ rau xanh đậm và/hoặc cà chua), có thêm chất béo'), -- Egg omelet or scrambled egg, with meat and vegetables other than dark-green and/or tomatoes, fat added
    ('32130900', 'Trứng tráng (omelet) hoặc trứng bác, có thịt và rau khác (trừ rau xanh đậm và/hoặc cà chua), không thêm chất béo'), -- Egg omelet or scrambled egg, with meat and vegetables other than dark-green and/or tomatoes, no added fat
    ('32130910', 'Trứng tráng (omelet) hoặc trứng bác, có thịt và rau khác (trừ rau xanh đậm và/hoặc cà chua), không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with meat and vegetables other than dark-green and/or tomatoes, NS as to fat
    ('32131000', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt và cà chua, có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, and tomatoes, fat added
    ('32131010', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt và cà chua, không thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, and tomatoes, no added fat
    ('32131020', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt và cà chua, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, and tomatoes, NS as to fat
    ('32131030', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt và rau xanh đậm, có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, and dark-green vegetables, fat added
    ('32131040', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt và rau xanh đậm, không thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, and dark-green vegetables, no added fat
    ('32131050', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt và rau xanh đậm, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, and dark-green vegetables, NS as to fat
    ('32131060', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt, cà chua và rau xanh đậm, có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, tomatoes, and dark-green vegetables, fat added
    ('32131070', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt, cà chua và rau xanh đậm, không thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, tomatoes, and dark-green vegetables, no added fat
    ('32131080', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt, cà chua và rau xanh đậm, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, tomatoes, and dark-green vegetables, NS as to fat
    ('32131090', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt và rau khác (trừ rau xanh đậm và/hoặc cà chua), có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, and vegetables other than dark-green and/or tomatoes, fat added
    ('32131100', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt và rau khác (trừ rau xanh đậm và/hoặc cà chua), không thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, and vegetables other than dark-green and/or tomatoes, no added fat
    ('32131110', 'Trứng tráng (omelet) hoặc trứng bác, có phô mai, thịt và rau khác (trừ rau xanh đậm và/hoặc cà chua), không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with cheese, meat, and vegetables other than dark-green and/or tomatoes, NS as to fat
    ('32131200', 'Trứng tráng (omelet) hoặc trứng bác, có khoai tây và/hoặc hành tây, có thêm chất béo'), -- Egg omelet or scrambled egg, with potatoes and/or onions, fat added
    ('32131210', 'Trứng tráng (omelet) hoặc trứng bác, có khoai tây và/hoặc hành tây, không thêm chất béo'), -- Egg omelet or scrambled egg, with potatoes and/or onions, no added fat
    ('32131220', 'Trứng tráng (omelet) hoặc trứng bác, có khoai tây và/hoặc hành tây, không rõ có thêm chất béo'), -- Egg omelet or scrambled egg, with potatoes and/or onions, NS as to fat
    ('32203010', 'Bánh mì kẹp salad trứng, bánh mì trắng'), -- Egg salad sandwich on white
    ('32203020', 'Bánh mì kẹp salad trứng, bánh mì lúa mì'), -- Egg salad sandwich on wheat
    ('32300100', 'Súp trứng đánh tan (egg drop)'), -- Soup, egg drop
    ('32400055', 'Lòng trắng trứng tráng, bác hoặc chiên, không rõ có thêm chất béo'), -- Egg white omelet, scrambled, or fried, NS as to fat
    ('32400060', 'Lòng trắng trứng tráng, bác hoặc chiên, nấu với bơ thực vật'), -- Egg white omelet, scrambled, or fried, made with margarine
    ('32400065', 'Lòng trắng trứng tráng, bác hoặc chiên, nấu với dầu'), -- Egg white omelet, scrambled, or fried, made with oil
    ('32400070', 'Lòng trắng trứng tráng, bác hoặc chiên, nấu với bơ'), -- Egg white omelet, scrambled, or fried, made with butter
    ('32400075', 'Lòng trắng trứng tráng, bác hoặc chiên, nấu với dầu xịt chống dính'), -- Egg white omelet, scrambled, or fried, made with cooking spray
    ('32400078', 'Lòng trắng trứng tráng, bác hoặc chiên, không rõ loại chất béo'), -- Egg white omelet, scrambled, or fried, NS as to fat type
    ('32400080', 'Lòng trắng trứng tráng, bác hoặc chiên, không thêm chất béo'), -- Egg white omelet, scrambled, or fried, no added fat
    ('32400100', 'Lòng trắng trứng tráng, bác hoặc chiên, có phô mai'), -- Egg white, omelet, scrambled, or fried, with cheese
    ('32400200', 'Lòng trắng trứng tráng, bác hoặc chiên, có thịt'), -- Egg white, omelet, scrambled, or fried, with meat
    ('32400300', 'Lòng trắng trứng tráng, bác hoặc chiên, có rau'), -- Egg white, omelet, scrambled, or fried, with vegetables
    ('32400400', 'Lòng trắng trứng tráng, bác hoặc chiên, có phô mai và thịt'), -- Egg white, omelet, scrambled, or fried, with cheese and meat
    ('32400500', 'Lòng trắng trứng tráng, bác hoặc chiên, có phô mai và rau'), -- Egg white, omelet, scrambled, or fried, with cheese and vegetables
    ('32400600', 'Lòng trắng trứng tráng, bác hoặc chiên, có thịt và rau'), -- Egg white, omelet, scrambled, or fried, with meat and vegetables
    ('32400700', 'Lòng trắng trứng tráng, bác hoặc chiên, có phô mai, thịt và rau'), -- Egg white, omelet, scrambled, or fried, with cheese, meat, and vegetables
    ('32401000', 'Bánh meringue (lòng trắng trứng đánh đường nướng)'), -- Meringues
    ('33001010', 'Trứng thay thế (egg substitute) tráng, bác hoặc chiên, có thêm chất béo'), -- Egg substitute, omelet, scrambled, or fried, fat added
    ('33001050', 'Trứng thay thế (egg substitute) tráng, bác hoặc chiên, không thêm chất béo'), -- Egg substitute, omelet, scrambled, or fried, no added fat
    ('33401000', 'Trứng thay thế (egg substitute) tráng, bác hoặc chiên, có phô mai'), -- Egg substitute, omelet, scrambled, or fried, with cheese
    ('33401100', 'Trứng thay thế (egg substitute) tráng, bác hoặc chiên, có thịt'), -- Egg substitute, omelet, scrambled, or fried, with meat
    ('33401200', 'Trứng thay thế (egg substitute) tráng, bác hoặc chiên, có rau'), -- Egg substitute, omelet, scrambled, or fried, with vegetables
    ('33401300', 'Trứng thay thế (egg substitute) tráng, bác hoặc chiên, có phô mai và thịt'), -- Egg substitute, omelet, scrambled, or fried, with cheese and meat
    ('33401400', 'Trứng thay thế (egg substitute) tráng, bác hoặc chiên, có phô mai và rau'), -- Egg substitute, omelet, scrambled, or fried, with cheese and vegetables
    ('33401500', 'Trứng thay thế (egg substitute) tráng, bác hoặc chiên, có thịt và rau'), -- Egg substitute, omelet, scrambled, or fried, with meat and vegetables
    ('33401600', 'Trứng thay thế (egg substitute) tráng, bác hoặc chiên, có phô mai, thịt và rau'), -- Egg substitute, omelet, scrambled, or fried, with cheese, meat, and vegetables
    ('34001100', 'Bánh mì kẹp trứng, bánh mì trắng'), -- Egg sandwich on white bread
    ('34001110', 'Bánh mì kẹp trứng, bánh mì trắng, có phô mai'), -- Egg sandwich on white bread, with cheese
    ('34001120', 'Bánh mì kẹp trứng, bánh mì trắng, có thịt'), -- Egg sandwich on white bread, with meat
    ('34001130', 'Bánh mì kẹp trứng, bánh mì trắng, có thịt và phô mai'), -- Egg sandwich on white bread, with meat and cheese
    ('34001200', 'Bánh mì kẹp trứng, bánh mì lúa mì'), -- Egg sandwich on wheat bread
    ('34001210', 'Bánh mì kẹp trứng, bánh mì lúa mì, có phô mai'), -- Egg sandwich on wheat bread, with cheese
    ('34001220', 'Bánh mì kẹp trứng, bánh mì lúa mì, có thịt'), -- Egg sandwich on wheat bread, with meat
    ('34001230', 'Bánh mì kẹp trứng, bánh mì lúa mì, có thịt và phô mai'), -- Egg sandwich on wheat bread, with meat and cheese
    ('34001300', 'Bánh mì kẹp trứng, bánh muffin kiểu Anh'), -- Egg sandwich on English muffin
    ('34001310', 'Bánh mì kẹp trứng, bánh muffin kiểu Anh, có xúc xích'), -- Egg sandwich on English muffin, with sausage
    ('34001320', 'Bánh mì kẹp trứng, bánh muffin kiểu Anh, có thịt xông khói'), -- Egg sandwich on English muffin, with bacon
    ('34001330', 'Bánh mì kẹp trứng, bánh muffin kiểu Anh, có giăm bông'), -- Egg sandwich on English muffin, with ham
    ('34001400', 'Bánh mì kẹp trứng, bánh sừng bò'), -- Egg sandwich on croissant
    ('34001410', 'Bánh mì kẹp trứng, bánh sừng bò, có xúc xích'), -- Egg sandwich on croissant, with sausage
    ('34001420', 'Bánh mì kẹp trứng, bánh sừng bò, có thịt xông khói'), -- Egg sandwich on croissant, with bacon
    ('34001430', 'Bánh mì kẹp trứng, bánh sừng bò, có giăm bông'), -- Egg sandwich on croissant, with ham
    ('34001500', 'Bánh mì kẹp trứng, bánh biscuit'), -- Egg sandwich on biscuit
    ('34001510', 'Bánh mì kẹp trứng, bánh biscuit, có xúc xích'), -- Egg sandwich on biscuit, with sausage
    ('34001520', 'Bánh mì kẹp trứng, bánh biscuit, có xúc xích và phô mai'), -- Egg sandwich on biscuit, with sausage and cheese
    ('34001530', 'Bánh mì kẹp trứng, bánh biscuit, có thịt xông khói'), -- Egg sandwich on biscuit, with bacon
    ('34001540', 'Bánh mì kẹp trứng, bánh biscuit, có thịt xông khói và phô mai'), -- Egg sandwich on biscuit, with bacon and cheese
    ('34001600', 'Bánh mì kẹp trứng, bánh bagel'), -- Egg sandwich on bagel
    ('34001610', 'Bánh mì kẹp trứng, bánh bagel, có xúc xích'), -- Egg sandwich on bagel, with sausage
    ('34001620', 'Bánh mì kẹp trứng, bánh bagel, có thịt xông khói'), -- Egg sandwich on bagel, with bacon
    ('34001630', 'Bánh mì kẹp trứng, bánh bagel, có giăm bông'), -- Egg sandwich on bagel, with ham
    ('34001700', 'Bánh mì kẹp trứng, bánh kếp (pancake)'), -- Egg sandwich on griddle/pancake
    ('34001710', 'Bánh mì kẹp trứng, bánh kếp (pancake), có thịt'), -- Egg sandwich on griddle/pancake, with meat
    ('34002000', 'Bánh mì kẹp lòng trắng trứng'), -- Egg white sandwich
    ('34002010', 'Bánh mì kẹp lòng trắng trứng, có phô mai'), -- Egg white sandwich, with cheese
    ('34002020', 'Bánh mì kẹp lòng trắng trứng, có thịt'), -- Egg white sandwich, with meat
    ('34002110', 'Bánh biscuit kẹp thịt xông khói'), -- Bacon biscuit sandwich
    ('34002120', 'Bánh biscuit kẹp giăm bông'), -- Ham biscuit sandwich
    ('34002130', 'Bánh biscuit kẹp xúc xích'), -- Sausage biscuit sandwich
    ('34002140', 'Bánh biscuit kẹp xúc xích, có phô mai'), -- Sausage biscuit sandwich, with cheese
    ('34002150', 'Bánh muffin kiểu Anh kẹp xúc xích'), -- Sausage English muffin sandwich
    ('34002160', 'Bánh kếp (pancake) kẹp xúc xích'), -- Sausage griddle/pancake sandwich
    ('34003100', 'Bánh burrito trứng'), -- Egg burrito
    ('34003120', 'Bánh burrito trứng, có xúc xích'), -- Egg burrito, with sausage
    ('34003130', 'Bánh burrito trứng, có thịt xông khói'), -- Egg burrito, with bacon
    ('34003140', 'Bánh burrito trứng, có giăm bông'), -- Egg burrito, with ham
    ('41100990', 'Đậu, loại chung'), -- Beans, NFS
    ('41101010', 'Đậu, từ loại khô, không rõ loại, có thêm chất béo'), -- Beans, from dried, NS as to type, fat added
    ('41101020', 'Đậu, từ loại khô, không rõ loại, không thêm chất béo'), -- Beans, from dried, NS as to type, no added fat
    ('41101060', 'Đậu, từ đồ hộp, không rõ loại, có thêm chất béo'), -- Beans, from canned, NS as to type, fat added
    ('41101070', 'Đậu, từ đồ hộp, không rõ loại, không thêm chất béo'), -- Beans, from canned, NS as to type, no added fat
    ('41101080', 'Đậu, đồ ăn nhanh/nhà hàng, không rõ loại'), -- Beans, from fast food / restaurant, NS as to type
    ('41101090', 'Đậu trắng, loại chung'), -- White beans, NFS
    ('41101110', 'Đậu trắng, từ loại khô, có thêm chất béo'), -- White beans, from dried, fat added
    ('41101120', 'Đậu trắng, từ loại khô, không thêm chất béo'), -- White beans, from dried, no added fat
    ('41101140', 'Đậu trắng, từ đồ hộp, có thêm chất béo'), -- White beans, from canned, fat added
    ('41101180', 'Đậu trắng, từ đồ hộp, không thêm chất béo'), -- White beans, from canned, no added fat
    ('41101210', 'Đậu trắng, từ đồ hộp, giảm muối'), -- White beans, from canned, reduced sodium
    ('41101990', 'Đậu đen, loại chung'), -- Black beans, NFS
    ('41102010', 'Đậu đen, từ loại khô, có thêm chất béo'), -- Black beans, from dried, fat added
    ('41102020', 'Đậu đen, từ loại khô, không thêm chất béo'), -- Black beans, from dried, no added fat
    ('41102040', 'Đậu đen, từ đồ hộp, có thêm chất béo'), -- Black beans, from canned, fat added
    ('41102080', 'Đậu đen, từ đồ hộp, không thêm chất béo'), -- Black beans, from canned, no added fat
    ('41102110', 'Đậu đen, từ đồ hộp, giảm muối'), -- Black beans, from canned, reduced sodium
    ('41102150', 'Đậu đen, đồ ăn nhanh/nhà hàng'), -- Black beans, from fast food / restaurant
    ('41102170', 'Đậu đen nấu thịt'), -- Black beans with meat
    ('41102210', 'Đậu tằm, nấu chín'), -- Fava beans, cooked
    ('41102990', 'Đậu lima, loại chung'), -- Lima beans, NFS
    ('41103010', 'Đậu lima, từ loại khô'), -- Lima beans, from dried
    ('41103070', 'Đậu hồng, nấu chín'), -- Pink beans, cooked
    ('41103990', 'Đậu pinto, loại chung'), -- Pinto beans, NFS
    ('41104010', 'Đậu pinto, từ loại khô, có thêm chất béo'), -- Pinto beans, from dried, fat added
    ('41104020', 'Đậu pinto, từ loại khô, không thêm chất béo'), -- Pinto beans, from dried, no added fat
    ('41104040', 'Đậu pinto, từ đồ hộp, có thêm chất béo'), -- Pinto beans, from canned, fat added
    ('41104080', 'Đậu pinto, từ đồ hộp, không thêm chất béo'), -- Pinto beans, from canned, no added fat
    ('41104110', 'Đậu pinto, từ đồ hộp, giảm muối'), -- Pinto beans, from canned, reduced sodium
    ('41104200', 'Đậu pinto, đồ ăn nhanh/nhà hàng'), -- Pinto beans, from fast food / restaurant
    ('41104250', 'Đậu pinto nấu thịt'), -- Pinto beans with meat
    ('41105990', 'Đậu tây đỏ, loại chung'), -- Kidney beans, NFS
    ('41106010', 'Đậu tây đỏ, từ loại khô, có thêm chất béo'), -- Kidney beans, from dried, fat added
    ('41106020', 'Đậu tây đỏ, từ loại khô, không thêm chất béo'), -- Kidney beans, from dried, no added fat
    ('41106040', 'Đậu tây đỏ, từ đồ hộp, có thêm chất béo'), -- Kidney beans, from canned, fat added
    ('41106080', 'Đậu tây đỏ, từ đồ hộp, không thêm chất béo') -- Kidney beans, from canned, no added fat
) AS t (source_food_code, name_vi)
WHERE f.source = 'USDA_FNDDS' AND f.source_food_code = t.source_food_code;

UPDATE nutrition_foods f SET name_vi = t.name_vi, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:usda-name-vi-v25'
FROM (VALUES
    ('41106110', 'Đậu tây đỏ, từ đồ hộp, giảm muối'), -- Kidney beans, from canned, reduced sodium
    ('41106150', 'Đậu tây đỏ, đồ ăn nhanh/nhà hàng'), -- Kidney beans, from fast food / restaurant
    ('41106170', 'Đậu tây đỏ nấu thịt'), -- Kidney beans with meat
    ('41106510', 'Đậu Peru, từ loại khô'), -- Peruvian beans, from dried
    ('41107010', 'Đậu tương, nấu chín'), -- Soybeans, cooked
    ('41108010', 'Đậu xanh, nấu chín'), -- Mung beans, cooked
    ('41201010', 'Đậu nướng lò (baked beans)'), -- Baked beans
    ('41201020', 'Đậu nướng lò (baked beans), chay'), -- Baked beans, vegetarian
    ('41201050', 'Đậu nướng lò (baked beans), đồ ăn nhanh/nhà hàng'), -- Baked beans from fast food / restaurant
    ('41202505', 'Đậu và cà chua, không thêm chất béo'), -- Beans and tomatoes, no added fat
    ('41202510', 'Đậu và cà chua, có thêm chất béo'), -- Beans and tomatoes, fat added
    ('41203030', 'Salad đậu đen'), -- Black bean salad
    ('41205010', 'Đậu nghiền chiên lại (refried beans)'), -- Refried beans
    ('41205017', 'Đậu nghiền chiên lại (refried beans), đồ ăn nhanh/nhà hàng'), -- Refried beans, from fast food / restaurant
    ('41205030', 'Đậu nghiền chiên lại (refried beans) có thịt'), -- Refried beans with meat
    ('41205040', 'Đậu nghiền chiên lại (refried beans), từ đồ hộp, giảm muối'), -- Refried beans, from canned, reduced sodium
    ('41205050', 'Sốt chấm đậu, làm từ đậu nghiền chiên lại'), -- Bean dip, made with refried beans
    ('41205055', 'Sốt chấm nhiều lớp (layer dip)'), -- Layer dip
    ('41205070', 'Sốt đậu gà hummus, không hương vị'), -- Hummus, plain
    ('41205075', 'Sốt đậu gà hummus, có hương vị'), -- Hummus, flavored
    ('41205100', 'Sốt đậu đen'), -- Black bean sauce
    ('41206030', 'Đậu và xúc xích hot dog'), -- Beans and franks
    ('41208030', 'Đậu nấu thịt lợn (pork and beans)'), -- Pork and beans
    ('41208100', 'Đậu nấu thịt, không rõ loại'), -- Beans with meat, NS as to type
    ('41209000', 'Viên chiên đậu gà (falafel)'), -- Falafel
    ('41210000', 'Bánh đậu (bean cake)'), -- Bean cake
    ('41221000', 'Đậu nướng lò (baked beans), giảm muối'), -- Baked beans, reduced sodium
    ('41221020', 'Món chili chay'), -- Chili, vegetarian
    ('41300990', 'Đậu mắt đen, loại chung'), -- Blackeyed peas, NFS
    ('41301010', 'Đậu mắt đen, từ loại khô'), -- Blackeyed peas, from dried
    ('41301990', 'Đậu gà, loại chung'), -- Chickpeas, NFS
    ('41302010', 'Đậu gà, từ loại khô, có thêm chất béo'), -- Chickpeas, from dried, fat added
    ('41302020', 'Đậu gà, từ loại khô, không thêm chất béo'), -- Chickpeas, from dried, no added fat
    ('41302040', 'Đậu gà, từ đồ hộp, có thêm chất béo'), -- Chickpeas, from canned, fat added
    ('41302080', 'Đậu gà, từ đồ hộp, không thêm chất béo'), -- Chickpeas, from canned, no added fat
    ('41302110', 'Đậu gà, từ đồ hộp, giảm muối'), -- Chickpeas, from canned, reduced sodium
    ('41303000', 'Đậu Hà Lan tách đôi, từ loại khô, không thêm chất béo'), -- Split peas, from dried, no added fat
    ('41303010', 'Đậu Hà Lan tách đôi, từ loại khô, có thêm chất béo'), -- Split peas, from dried, fat added
    ('41304000', 'Đậu Hà Lan tẩm wasabi'), -- Wasabi peas
    ('41304970', 'Đậu lăng, loại chung'), -- Lentils, NFS
    ('41304990', 'Đậu lăng, từ loại khô, có thêm chất béo'), -- Lentils, from dried, fat added
    ('41305000', 'Đậu lăng, từ loại khô, không thêm chất béo'), -- Lentils, from dried, no added fat
    ('41305020', 'Đậu lăng, từ đồ hộp'), -- Lentils, from canned
    ('41305050', 'Món dal (đậu hầm Ấn Độ)'), -- Dal
    ('41310900', 'Snack đậu chiên giòn'), -- Bean chips
    ('41311000', 'Bánh papad (Ấn Độ), nướng vỉ hoặc nướng'), -- Papad, grilled or broiled
    ('41311020', 'Món hầm rau sambar (Ấn Độ)'), -- Sambar, vegetable stew
    ('41311030', 'Cà ri đậu lăng'), -- Lentil curry
    ('41311040', 'Cà ri đậu lăng với cơm'), -- Lentil curry with rice
    ('41410010', 'Hạt đậu tương rang'), -- Soy nuts
    ('41410015', 'Snack đậu tương chiên giòn'), -- Soy chips
    ('41420010', 'Đậu phụ (soybean curd)'), -- Soybean curd
    ('41420020', 'Đậu tương non (edamame), nấu chín'), -- Edamame, cooked
    ('41420050', 'Phô mai từ đậu phụ (soybean curd cheese)'), -- Soybean curd cheese
    ('41420100', 'Sốt miso'), -- Miso sauce
    ('41420110', 'Tương miso'), -- Miso
    ('41420200', 'Đậu tương lên men natto (Nhật)'), -- Natto
    ('41420250', 'Tương đen (sốt hoisin)'), -- Hoisin sauce
    ('41420300', 'Nước tương'), -- Soy sauce
    ('41420350', 'Nước tương, giảm muối'), -- Soy sauce, reduced sodium
    ('41420380', 'Sữa chua đậu tương'), -- Yogurt, soy
    ('41420400', 'Sốt teriyaki'), -- Teriyaki sauce
    ('41420410', 'Sốt teriyaki, giảm muối'), -- Teriyaki sauce, reduced sodium
    ('41420450', 'Sốt Worcestershire'), -- Worcestershire sauce
    ('41421010', 'Đậu phụ chiên ngập dầu'), -- Soybean curd, deep fried
    ('41421020', 'Đậu phụ tẩm bột chiên xù'), -- Soybean curd, breaded, fried
    ('41425010', 'Miến làm từ đậu tương'), -- Vermicelli, made from soybeans
    ('41440000', 'Protein thực vật dạng sợi (TVP), khô'), -- Textured vegetable protein, dry
    ('41480020', 'Món tráng miệng đông lạnh, không sữa'), -- Frozen dessert, non-dairy
    ('41601010', 'Súp đậu'), -- Soup, bean
    ('41601030', 'Súp đậu, đóng hộp'), -- Soup, bean, canned
    ('41601070', 'Súp miso hoặc súp đậu phụ'), -- Soup, miso or tofu
    ('41601160', 'Súp đậu, đóng hộp, giảm muối'), -- Soup, bean, canned, reduced sodium
    ('41601180', 'Súp đậu có thịt'), -- Soup, bean, with meat
    ('41602030', 'Súp đậu Hà Lan tách đôi có thịt'), -- Soup, split pea, with meat
    ('41602050', 'Súp đậu Hà Lan tách đôi'), -- Soup, split pea
    ('41603000', 'Súp đậu lăng, đóng hộp'), -- Soup, lentil, canned
    ('41603005', 'Súp đậu lăng, đóng hộp, giảm muối'), -- Soup, lentil, canned, reduced sodium
    ('41603010', 'Súp đậu lăng'), -- Soup, lentil
    ('41603020', 'Súp đậu lăng có thịt'), -- Soup, lentil, with meat
    ('41603030', 'Súp mulligatawny (cà ri Ấn Độ)'), -- Soup, mulligatawany
    ('41810200', 'Thịt xông khói chay, dạng lát'), -- Bacon strip, meatless
    ('41810250', 'Thịt xông khói vụn (bacon bits)'), -- Bacon bits
    ('41810400', 'Xúc xích, miếng chả hoặc lát ăn sáng chay'), -- Breakfast link, pattie, or slice, meatless
    ('41810600', 'Gà chay, loại chung'), -- Chicken, meatless, NFS
    ('41810610', 'Gà chay tẩm bột chiên xù'), -- Chicken, meatless, breaded, fried
    ('41811400', 'Xúc xích hot dog chay'), -- Hot dog, vegetarian
    ('41811600', 'Thịt nguội lát chay (kiểu bò, gà, salami hoặc gà tây)'), -- Luncheon slice, meatless-beef, chicken, salami or turkey
    ('41811800', 'Thịt viên chay'), -- Meatball, meatless
    ('41811890', 'Miếng chả (patty) burger chay, không bánh'), -- Veggie burger patty, no bun
    ('41811950', 'Bít tết kiểu Thụy Sĩ chay, với nước sốt gravy'), -- Swiss steak, with gravy, meatless
    ('41812400', 'Bánh pie nhân mặn (pot pie), không thịt'), -- Pot pie, no meat
    ('41812500', 'Đậu phụ và rau (gồm cà rốt, súp lơ xanh và/hoặc rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Tofu and vegetables including carrots, broccoli, and/or dark-green leafy; no potatoes, with soy-based sauce
    ('41812510', 'Đậu phụ và rau (trừ cà rốt, súp lơ xanh và rau lá xanh đậm), không khoai tây, sốt nền nước tương'), -- Tofu and vegetables excluding carrots, broccoli, and dark-green leafy; no potatoes, with soy-based sauce
    ('41812600', 'Phi lê chay'), -- Vegetarian, fillet
    ('41812800', 'Món hầm chay'), -- Vegetarian stew
    ('41812850', 'Món stroganoff chay'), -- Vegetarian stroganoff
    ('41901010', 'Bánh burger chay, có bánh bun'), -- Veggie burger, on bun
    ('41901020', 'Bánh burger chay, có bánh bun, có phô mai'), -- Veggie burger, on bun, with cheese
    ('41901030', 'Bánh mì kẹp falafel'), -- Falafel sandwich
    ('42100050', 'Các loại hạt, loại chung'), -- Nuts, NFS
    ('42100100', 'Hạnh nhân, loại chung'), -- Almonds, NFS
    ('42101000', 'Hạnh nhân, chưa rang'), -- Almonds, unroasted
    ('42101110', 'Hạnh nhân, có muối'), -- Almonds, salted
    ('42101120', 'Hạnh nhân, rắc ít muối'), -- Almonds, lightly salted
    ('42101130', 'Hạnh nhân, không muối'), -- Almonds, unsalted
    ('42101300', 'Hạnh nhân, có hương vị'), -- Almonds, flavored
    ('42101350', 'Hạnh nhân rang mật ong'), -- Almonds, honey roasted
    ('42102000', 'Hạt Brazil'), -- Brazil nuts
    ('42104000', 'Hạt điều, loại chung'), -- Cashews, NFS
    ('42104050', 'Hạt điều, chưa rang'), -- Cashews, unroasted
    ('42104100', 'Hạt điều, có muối'), -- Cashews, salted
    ('42104105', 'Hạt điều, rắc ít muối'), -- Cashews, lightly salted
    ('42104110', 'Hạt điều, không muối'), -- Cashews, unsalted
    ('42104500', 'Hạt điều rang mật ong'), -- Cashews, honey roasted
    ('42105000', 'Hạt dẻ'), -- Chestnuts
    ('42106000', 'Dừa tươi'), -- Coconut, fresh
    ('42106020', 'Dừa đóng gói'), -- Coconut, packaged
    ('42107000', 'Hạt phỉ'), -- Hazelnuts
    ('42109100', 'Hạt mắc ca'), -- Macadamia nuts
    ('42110000', 'Hạt hỗn hợp (mixed nuts), loại chung'), -- Mixed nuts, NFS
    ('42110050', 'Hạt hỗn hợp (mixed nuts), chưa rang'), -- Mixed nuts, unroasted
    ('42110100', 'Hạt hỗn hợp (mixed nuts) có lạc, có muối'), -- Mixed nuts, with peanuts, salted
    ('42110110', 'Hạt hỗn hợp (mixed nuts) có lạc, rắc ít muối'), -- Mixed nuts, with peanuts, lightly salted
    ('42110120', 'Hạt hỗn hợp (mixed nuts) có lạc, không muối'), -- Mixed nuts, with peanuts, unsalted
    ('42110150', 'Hạt hỗn hợp (mixed nuts) không có lạc, có muối'), -- Mixed nuts, without peanuts, salted
    ('42110160', 'Hạt hỗn hợp (mixed nuts) không có lạc, không muối'), -- Mixed nuts, without peanuts, unsalted
    ('42110300', 'Hạt hỗn hợp (mixed nuts) rang mật ong'), -- Mixed nuts, honey roasted
    ('42111000', 'Lạc, loại chung'), -- Peanuts, NFS
    ('42111030', 'Lạc luộc'), -- Peanuts, boiled
    ('42111040', 'Lạc, chưa rang'), -- Peanuts, unroasted
    ('42111100', 'Lạc rang, có muối'), -- Peanuts, roasted, salted
    ('42111110', 'Lạc rang, không muối'), -- Peanuts, roasted, unsalted
    ('42111200', 'Lạc rang khô, có muối'), -- Peanuts, dry roasted, salted
    ('42111205', 'Lạc rang khô, rắc ít muối'), -- Peanuts, dry roasted, lightly salted
    ('42111210', 'Lạc rang khô, không muối'), -- Peanuts, dry roasted, unsalted
    ('42111500', 'Lạc rang mật ong'), -- Peanuts, honey roasted
    ('42112000', 'Hạt hồ đào pecan, loại chung'), -- Pecans, NFS
    ('42112100', 'Hạt hồ đào pecan, chưa rang'), -- Pecans, unroasted
    ('42112200', 'Hạt hồ đào pecan, có muối'), -- Pecans, salted
    ('42112210', 'Hạt hồ đào pecan, không muối'), -- Pecans, unsalted
    ('42112300', 'Hạt hồ đào pecan rang mật ong'), -- Pecans, honey roasted
    ('42113000', 'Hạt thông'), -- Pine nuts
    ('42114130', 'Hạt dẻ cười, loại chung'), -- Pistachio nuts, NFS
    ('42114140', 'Hạt dẻ cười, có muối'), -- Pistachio nuts, salted
    ('42114142', 'Hạt dẻ cười, rắc ít muối'), -- Pistachio nuts, lightly salted
    ('42114145', 'Hạt dẻ cười, không muối'), -- Pistachio nuts, unsalted
    ('42116000', 'Quả óc chó, trừ loại rang mật ong'), -- Walnuts, excluding honey roasted
    ('42116100', 'Quả óc chó rang mật ong'), -- Walnuts, honey roasted
    ('42200500', 'Bơ hạnh nhân'), -- Almond butter
    ('42200510', 'Bơ hạnh nhân, giảm muối'), -- Almond butter, lower sodium
    ('42200600', 'Bột nhão hạnh nhân (almond paste)'), -- Almond paste
    ('42201000', 'Bơ hạt điều'), -- Cashew butter
    ('42202000', 'Bơ lạc'), -- Peanut butter
    ('42202010', 'Bơ lạc, giảm muối'), -- Peanut butter, lower sodium
    ('42202100', 'Bơ lạc, giảm muối và giảm đường'), -- Peanut butter, lower sodium and lower sugar
    ('42202130', 'Bơ lạc, giảm đường'), -- Peanut butter, lower sugar
    ('42202150', 'Bơ lạc, giảm béo'), -- Peanut butter, reduced fat
    ('42202200', 'Bơ lạc, bổ sung vitamin và khoáng chất'), -- Peanut butter, vitamin and mineral fortified
    ('42203000', 'Bơ lạc và mứt'), -- Peanut butter and jelly
    ('42203100', 'Bơ lạc sô-cô-la dạng phết'), -- Peanut butter and chocolate spread
    ('42203200', 'Bơ hạt đậu tương'), -- Soy nut butter
    ('42204050', 'Sốt lạc'), -- Peanut sauce
    ('42204060', 'Súp lạc'), -- Soup, peanut
    ('42204100', 'Nước sốt gravy chay'), -- Gravy, vegetarian
    ('42301010', 'Bánh mì kẹp bơ lạc, loại chung'), -- Peanut butter sandwich, NFS
    ('42301015', 'Bánh mì kẹp bơ lạc, bơ lạc thường, bánh mì trắng'), -- Peanut butter sandwich, with regular peanut butter, on white bread
    ('42301020', 'Bánh mì kẹp bơ lạc, bơ lạc thường, bánh mì lúa mì'), -- Peanut butter sandwich, with regular peanut butter, on wheat bread
    ('42301115', 'Bánh mì kẹp bơ lạc, bơ lạc giảm béo, bánh mì trắng'), -- Peanut butter sandwich, with reduced fat peanut butter, on white bread
    ('42301120', 'Bánh mì kẹp bơ lạc, bơ lạc giảm béo, bánh mì lúa mì'), -- Peanut butter sandwich, with reduced fat peanut butter, on wheat bread
    ('42302010', 'Bánh mì kẹp bơ lạc và mứt, loại chung'), -- Peanut butter and jelly sandwich, NFS
    ('42302015', 'Bánh mì kẹp bơ lạc và mứt, bơ lạc thường, mứt thường, bánh mì trắng'), -- Peanut butter and jelly sandwich, with regular peanut butter, regular jelly, on white bread
    ('42302020', 'Bánh mì kẹp bơ lạc và mứt, bơ lạc thường, mứt thường, bánh mì lúa mì'), -- Peanut butter and jelly sandwich, with regular peanut butter, regular jelly, on wheat bread
    ('42302055', 'Bánh mì kẹp bơ lạc và mứt, bơ lạc giảm béo, bánh mì trắng'), -- Peanut butter and jelly sandwich, with reduced fat peanut butter, on white bread
    ('42302060', 'Bánh mì kẹp bơ lạc và mứt, bơ lạc giảm béo, bánh mì lúa mì'), -- Peanut butter and jelly sandwich, with reduced fat peanut butter, on wheat bread
    ('42302105', 'Bánh mì kẹp bơ lạc và mứt, bơ lạc thường, mứt giảm đường, bánh mì trắng'), -- Peanut butter and jelly sandwich, with regular peanut butter, reduced sugar jelly, on white bread
    ('42302110', 'Bánh mì kẹp bơ lạc và mứt, bơ lạc thường, mứt giảm đường, bánh mì lúa mì'), -- Peanut butter and jelly sandwich, with regular peanut butter, reduced sugar jelly, on wheat bread
    ('42303100', 'Bánh mì kẹp bơ lạc và mứt, sản phẩm đông lạnh đóng gói sẵn, bỏ viền bánh'), -- Peanut butter and jelly sandwich, frozen commercial product without crusts
    ('42304010', 'Bánh mì kẹp bơ hạnh nhân, bánh mì trắng'), -- Almond butter sandwich, on white bread
    ('42304020', 'Bánh mì kẹp bơ hạnh nhân, bánh mì lúa mì'), -- Almond butter sandwich, on wheat bread
    ('42304030', 'Bánh mì kẹp bơ hạnh nhân và mứt, bánh mì trắng'), -- Almond butter and jelly sandwich, on white bread
    ('42304040', 'Bánh mì kẹp bơ hạnh nhân và mứt, bánh mì lúa mì'), -- Almond butter and jelly sandwich, on wheat bread
    ('42305010', 'Bánh mì kẹp Nutella, bánh mì trắng'), -- Nutella sandwich on white bread
    ('42305020', 'Bánh mì kẹp Nutella, bánh mì lúa mì'), -- Nutella sandwich on wheat bread
    ('42401010', 'Nước cốt dừa, dùng nấu ăn'), -- Coconut milk, used in cooking
    ('42401100', 'Sữa chua từ nước cốt dừa'), -- Yogurt, coconut milk
    ('42401200', 'Sữa chua từ sữa hạnh nhân'), -- Yogurt, almond milk
    ('42402010', 'Kem dừa, đóng hộp, có đường'), -- Coconut cream, canned, sweetened
    ('42403010', 'Nước dừa, không đường'), -- Coconut water, unsweetened
    ('42404010', 'Nước dừa, có đường'), -- Coconut water, sweetened
    ('42500000', 'Hỗn hợp ăn vặt (trail mix), loại chung'), -- Trail mix, NFS
    ('42500100', 'Hỗn hợp ăn vặt (trail mix) có hạt'), -- Trail mix with nuts
    ('42501000', 'Hỗn hợp ăn vặt (trail mix) có hạt và trái cây'), -- Trail mix with nuts and fruit
    ('42501500', 'Hỗn hợp ăn vặt (trail mix) có sô-cô-la'), -- Trail mix with chocolate
    ('42502100', 'Hỗn hợp ăn vặt (trail mix) có bánh quy xoắn (pretzel), ngũ cốc hoặc granola'), -- Trail mix with pretzels, cereal, or granola
    ('43101050', 'Hạt bí, loại chung'), -- Pumpkin seeds, NFS
    ('43101100', 'Hạt bí, có muối'), -- Pumpkin seeds, salted
    ('43101150', 'Hạt bí, không muối'), -- Pumpkin seeds, unsalted
    ('43102000', 'Hạt hướng dương, không hương vị, không muối'), -- Sunflower seeds, plain, unsalted
    ('43102100', 'Hạt hướng dương, không hương vị, có muối'), -- Sunflower seeds, plain, salted
    ('43102300', 'Hạt hướng dương, có hương vị'), -- Sunflower seeds, flavored
    ('43102400', 'Hạt hướng dương, loại chung'), -- Sunflower seeds, NFS
    ('43103000', 'Hạt vừng'), -- Sesame seeds
    ('43103300', 'Sốt vừng tahini'), -- Tahini
    ('43104000', 'Hạt lanh'), -- Flax seeds
    ('43107000', 'Hỗn hợp các loại hạt nhỏ (seeds)'), -- Mixed seeds
    ('43108010', 'Hạt chia'), -- Chia seeds
    ('51000100', 'Bánh mì, không rõ loại bột chính'), -- Bread, NS as to major flour
    ('51000110', 'Bánh mì, không rõ loại bột chính, nướng giòn'), -- Bread, NS as to major flour, toasted
    ('51000180', 'Bánh mì tự làm tại nhà hoặc mua ở tiệm bánh, không rõ loại bột chính'), -- Bread, made from home recipe or purchased at a bakery, NS as to major flour
    ('51000190', 'Bánh mì tự làm tại nhà hoặc mua ở tiệm bánh, nướng giòn, không rõ loại bột chính'), -- Bread, made from home recipe or purchased at a bakery, toasted, NS as to major flour
    ('51000200', 'Bánh mì cuộn nhỏ, không rõ loại bột chính'), -- Roll, NS as to major flour
    ('51000300', 'Bánh mì cuộn nhỏ vỏ cứng, không rõ loại bột chính'), -- Roll, hard, NS as to major flour
    ('51000400', 'Bánh mì cuộn nhỏ cám, không rõ loại cám'), -- Roll, bran, NS as to type of bran
    ('51101000', 'Bánh mì trắng'), -- Bread, white
    ('51101010', 'Bánh mì trắng, nướng giòn'), -- Bread, white, toasted
    ('51101050', 'Bánh mì trắng tự làm tại nhà hoặc mua ở tiệm bánh'), -- Bread, white, made from home recipe or purchased at a bakery
    ('51101060', 'Bánh mì trắng tự làm tại nhà hoặc mua ở tiệm bánh, nướng giòn'), -- Bread, white, made from home recipe or purchased at a bakery, toasted
    ('51102010', 'Bánh mì trắng xoắn vân lúa mì nguyên cám'), -- Bread, white with whole wheat swirl
    ('51102020', 'Bánh mì trắng xoắn vân lúa mì nguyên cám, nướng giòn'), -- Bread, white with whole wheat swirl, toasted
    ('51105010', 'Bánh mì Cuba'), -- Bread, Cuban
    ('51105040', 'Bánh mì Cuba, nướng giòn'), -- Bread, Cuban, toasted
    ('51106010', 'Bánh mì nước truyền thống, kiểu Puerto Rico'), -- Bread, native, water, Puerto Rican style
    ('51106020', 'Bánh mì nước truyền thống, nướng giòn, kiểu Puerto Rico'), -- Bread, native, water, toasted, Puerto Rican style
    ('51106200', 'Bánh mì mỡ lợn, kiểu Puerto Rico'), -- Bread, lard, Puerto Rican style
    ('51106210', 'Bánh mì mỡ lợn, nướng giòn, kiểu Puerto Rico'), -- Bread, lard, toasted, Puerto Rican style
    ('51107010', 'Bánh mì Pháp hoặc Vienna'), -- Bread, French or Vienna
    ('51107040', 'Bánh mì Pháp hoặc Vienna, nướng giòn'), -- Bread, French or Vienna, toasted
    ('51108010', 'Bánh mì focaccia Ý, không hương vị'), -- Focaccia, Italian, plain
    ('51108100', 'Bánh mì naan'), -- Bread, naan
    ('51109010', 'Bánh mì Ý, Hy Lạp, Armenia'), -- Bread, Italian, Grecian, Armenian
    ('51109040', 'Bánh mì Ý, Hy Lạp, Armenia, nướng giòn'), -- Bread, Italian, Grecian, Armenian, toasted
    ('51109100', 'Bánh mì pita'), -- Bread, pita
    ('51109150', 'Bánh mì pita có trái cây'), -- Bread, pita with fruit
    ('51111010', 'Bánh mì phô mai'), -- Bread, cheese
    ('51111040', 'Bánh mì phô mai, nướng giòn'), -- Bread, cheese, toasted
    ('51113010', 'Bánh mì quế'), -- Bread, cinnamon
    ('51113100', 'Bánh mì quế, nướng giòn'), -- Bread, cinnamon, toasted
    ('51115010', 'Bánh mì bột ngô và mật mía'), -- Bread, cornmeal and molasses
    ('51115020', 'Bánh mì bột ngô và mật mía, nướng giòn'), -- Bread, cornmeal and molasses, toasted
    ('51119010', 'Bánh mì trứng Challah'), -- Bread, egg, Challah
    ('51119040', 'Bánh mì trứng Challah, nướng giòn'), -- Bread, egg, Challah, toasted
    ('51121015', 'Bánh mì bơ tỏi, loại chung'), -- Garlic bread, NFS
    ('51121025', 'Bánh mì bơ tỏi, đồ ăn nhanh/nhà hàng'), -- Garlic bread, from fast food / restaurant
    ('51121035', 'Bánh mì bơ tỏi, từ loại đông lạnh'), -- Garlic bread, from frozen
    ('51121045', 'Bánh mì bơ tỏi phủ phô mai parmesan, đồ ăn nhanh/nhà hàng'), -- Garlic bread, with parmesan cheese, from fast food / restaurant
    ('51121055', 'Bánh mì bơ tỏi phủ phô mai parmesan, từ loại đông lạnh'), -- Garlic bread, with parmesan cheese, from frozen
    ('51121065', 'Bánh mì bơ tỏi phủ phô mai tan chảy, đồ ăn nhanh/nhà hàng'), -- Garlic bread, with melted cheese, from fast food / restaurant
    ('51121075', 'Bánh mì bơ tỏi phủ phô mai tan chảy, từ loại đông lạnh'), -- Garlic bread, with melted cheese, from frozen
    ('51121110', 'Bánh mì hành tây'), -- Bread, onion
    ('51121120', 'Bánh mì hành tây, nướng giòn'), -- Bread, onion, toasted
    ('51122000', 'Bánh mì giảm calo và/hoặc giàu chất xơ, trắng hoặc loại chung'), -- Bread, reduced calorie and/or high fiber, white or NFS
    ('51122010', 'Bánh mì giảm calo và/hoặc giàu chất xơ, trắng hoặc loại chung, nướng giòn'), -- Bread, reduced calorie and/or high fiber, white or NFS, toasted
    ('51122100', 'Bánh mì giảm calo và/hoặc giàu chất xơ, trắng hoặc loại chung, có trái cây và/hoặc hạt'), -- Bread, reduced calorie and/or high fiber, white or NFS, with fruit and/or nuts
    ('51122110', 'Bánh mì giảm calo và/hoặc giàu chất xơ, trắng hoặc loại chung, có trái cây và/hoặc hạt, nướng giòn'), -- Bread, reduced calorie and/or high fiber, white or NFS, with fruit and/or nuts, toasted
    ('51122300', 'Bánh mì trắng, công thức đặc biệt, bổ sung chất xơ'), -- Bread, white, special formula, added fiber
    ('51122310', 'Bánh mì trắng, công thức đặc biệt, bổ sung chất xơ, nướng giòn'), -- Bread, white, special formula, added fiber, toasted
    ('51123020', 'Bánh mì giàu protein, nướng giòn'), -- Bread, high protein, toasted
    ('51127010', 'Bánh mì khoai tây'), -- Bread, potato
    ('51127020', 'Bánh mì khoai tây, nướng giòn'), -- Bread, potato, toasted
    ('51129010', 'Bánh mì nho khô'), -- Bread, raisin
    ('51129020', 'Bánh mì nho khô, nướng giòn'), -- Bread, raisin, toasted
    ('51133010', 'Bánh mì men chua (sourdough)'), -- Bread, sour dough
    ('51133020', 'Bánh mì men chua (sourdough), nướng giòn'), -- Bread, sour dough, toasted
    ('51134000', 'Bánh mì khoai lang'), -- Bread, sweet potato
    ('51134010', 'Bánh mì khoai lang, nướng giòn'), -- Bread, sweet potato, toasted
    ('51135000', 'Bánh mì rau củ'), -- Bread, vegetable
    ('51135010', 'Bánh mì rau củ, nướng giòn'), -- Bread, vegetable, toasted
    ('51136000', 'Bánh mì nướng bruschetta (Ý)'), -- Bruschetta
    ('51140100', 'Bột bánh mì chiên'), -- Bread, dough, fried
    ('51150000', 'Bánh mì cuộn nhỏ trắng, mềm'), -- Roll, white, soft
    ('51153000', 'Bánh mì cuộn nhỏ trắng, vỏ cứng'), -- Roll, white, hard
    ('51154010', 'Bánh mì cuộn nhỏ trắng, bánh bun hot dog'), -- Roll, white, hot dog bun
    ('51154100', 'Bánh mì cuộn nhỏ trắng, bánh bun hamburger'), -- Roll, white, hamburger bun
    ('51154510', 'Bánh mì cuộn nhỏ ăn kiêng'), -- Roll, diet
    ('51154550', 'Bánh mì cuộn nhỏ, bánh mì trứng'), -- Roll, egg bread
    ('51154600', 'Bánh mì cuộn nhỏ phô mai'), -- Roll, cheese
    ('51155000', 'Bánh mì cuộn nhỏ kiểu Pháp hoặc Vienna'), -- Roll, French or Vienna
    ('51156500', 'Bánh mì cuộn nhỏ tỏi'), -- Roll, garlic
    ('51157000', 'Bánh mì cuộn nhỏ trắng, loại dài (hoagie, submarine)'), -- Roll, white, hoagie, submarine
    ('51158100', 'Bánh mì cuộn nhỏ Mexico (bolillo)'), -- Roll, Mexican, bolillo
    ('51159000', 'Bánh mì cuộn nhỏ men chua (sourdough)'), -- Roll, sour dough
    ('51160000', 'Bánh mì cuộn nhỏ ngọt, không phủ kem đường'), -- Roll, sweet, no frosting
    ('51160100', 'Bánh mì cuộn nhỏ ngọt, bánh cuộn quế, không phủ kem đường'), -- Roll, sweet, cinnamon bun, no frosting
    ('51160110', 'Bánh mì cuộn nhỏ ngọt, bánh cuộn quế, phủ kem đường'), -- Roll, sweet, cinnamon bun, frosted
    ('51160200', 'Bánh mì ngọt Mexico (pan dulce), loại chung'), -- Pan dulce, NFS
    ('51161000', 'Bánh mì ngọt Mexico (pan dulce), có trái cây, không phủ kem đường'), -- Pan Dulce, with fruit, no frosting
    ('51161020', 'Bánh mì cuộn nhỏ ngọt, có trái cây, phủ kem đường'), -- Roll, sweet, with fruit, frosted
    ('51161030', 'Bánh mì cuộn nhỏ ngọt, có trái cây, phủ kem đường, ăn kiêng'), -- Roll, sweet, with fruit, frosted, diet
    ('51161050', 'Bánh mì cuộn nhỏ ngọt, phủ kem đường'), -- Roll, sweet, frosted
    ('51161250', 'Bánh mì ngọt Mexico (pan dulce), không phủ mặt'), -- Pan Dulce, no topping
    ('51161270', 'Bánh mì ngọt Mexico (pan dulce), phủ đường'), -- Pan Dulce, with sugar topping
    ('51161280', 'Bánh mì ngọt Mexico (pan dulce), có nho khô và phủ kem đường'), -- Pan Dulce, with raisins and icing
    ('51165000', 'Bánh ngọt ăn kèm cà phê (coffee cake), loại dùng men'), -- Coffee cake, yeast type
    ('51166000', 'Bánh sừng bò'), -- Croissant
    ('51166100', 'Bánh sừng bò phô mai'), -- Croissant, cheese
    ('51166200', 'Bánh sừng bò sô-cô-la'), -- Croissant, chocolate
    ('51166500', 'Bánh sừng bò trái cây'), -- Croissant, fruit
    ('51167000', 'Bánh mì brioche'), -- Brioche
    ('51168000', 'Bánh mì ăn kèm cà phê kiểu Tây Ban Nha'), -- Bread, Spanish coffee
    ('51180010', 'Bánh bagel'), -- Bagel
    ('51180030', 'Bánh bagel, có nho khô'), -- Bagel, with raisins
    ('51180080', 'Bánh bagel, có trái cây (trừ nho khô)'), -- Bagel, with fruit other than raisins
    ('51182010', 'Nhân nhồi bánh mì (stuffing)'), -- Bread stuffing
    ('51182020', 'Nhân nhồi bánh mì (stuffing), làm với trứng'), -- Bread stuffing made with egg
    ('51183990', 'Bánh mì que, loại chung'), -- Breadsticks, NFS
    ('51184000', 'Bánh mì que giòn, loại chung'), -- Breadsticks, hard, NFS
    ('51184100', 'Bánh mì que giòn, giảm muối'), -- Breadsticks, hard, reduced sodium
    ('51184200', 'Bánh mì que mềm, loại chung'), -- Breadsticks, soft, NFS
    ('51184210', 'Bánh mì que mềm, đồ ăn nhanh/nhà hàng'), -- Breadsticks, soft, fast food / restaurant
    ('51184220', 'Bánh mì que mềm, đông lạnh'), -- Breadsticks, soft, frozen
    ('51184230', 'Bánh mì que mềm, phủ phô mai parmesan, đồ ăn nhanh/nhà hàng'), -- Breadsticks, soft, with parmesan cheese, fast food / restaurant
    ('51184260', 'Bánh mì que mềm, nhồi hoặc phủ phô mai tan chảy'), -- Breadsticks, soft, stuffed or topped with melted cheese
    ('51185000', 'Bánh mì nướng giòn cắt hạt lựu (crouton)'), -- Croutons
    ('51186010', 'Bánh muffin kiểu Anh'), -- Muffin, English
    ('51186100', 'Bánh muffin kiểu Anh, có nho khô'), -- Muffin, English, with raisins
    ('51186130', 'Bánh muffin kiểu Anh, phô mai'), -- Muffin, English, cheese
    ('51186160', 'Bánh muffin kiểu Anh, có trái cây (trừ nho khô)'), -- Muffin, English, with fruit other than raisins
    ('51187000', 'Bánh mì nướng giòn Melba'), -- Melba toast
    ('51187020', 'Bánh mì nướng giòn hương hồi (anisette)'), -- Anisette toast
    ('51188100', 'Bánh panettone (Ý)'), -- Pannetone
    ('51188500', 'Bánh mì nướng giòn zwieback'), -- Zwieback toast
    ('51300050', 'Bánh mì trắng nguyên hạt'), -- Bread, whole grain white
    ('51300060', 'Bánh mì trắng nguyên hạt, nướng giòn'), -- Bread, whole grain white, toasted
    ('51300100', 'Bánh bagel trắng nguyên hạt'), -- Bagel, whole grain white
    ('51300110', 'Bánh mì lúa mì nguyên cám'), -- Bread, whole wheat
    ('51300120', 'Bánh mì lúa mì nguyên cám, nướng giòn'), -- Bread, whole wheat, toasted
    ('51300140', 'Bánh mì lúa mì nguyên cám tự làm tại nhà hoặc mua ở tiệm bánh'), -- Bread, whole wheat, made from home recipe or purchased at bakery
    ('51300150', 'Bánh mì lúa mì nguyên cám tự làm tại nhà hoặc mua ở tiệm bánh, nướng giòn'), -- Bread, whole wheat, made from home recipe or purchased at bakery, toasted
    ('51300175', 'Bánh chapati hoặc roti (Ấn Độ)'), -- Bread, chappatti or roti
    ('51300180', 'Bánh puri (Ấn Độ)'), -- Bread, puri
    ('51300185', 'Bánh paratha (Ấn Độ)'), -- Bread, paratha
    ('51300210', 'Bánh mì lúa mì nguyên cám, có nho khô'), -- Bread, whole wheat, with raisins
    ('51300220', 'Bánh mì lúa mì nguyên cám, có nho khô, nướng giòn'), -- Bread, whole wheat, with raisins, toasted
    ('51300300', 'Bánh mì lúa mì nảy mầm'), -- Bread, sprouted wheat
    ('51300310', 'Bánh mì lúa mì nảy mầm, nướng giòn'), -- Bread, sprouted wheat, toasted
    ('51301010', 'Bánh mì lúa mì hoặc lúa mì xay vỡ'), -- Bread, wheat or cracked wheat
    ('51301020', 'Bánh mì lúa mì hoặc lúa mì xay vỡ, nướng giòn'), -- Bread, wheat or cracked wheat, toasted
    ('51301040', 'Bánh mì lúa mì hoặc lúa mì xay vỡ, tự làm tại nhà hoặc mua ở tiệm bánh'), -- Bread, wheat or cracked wheat, made from home recipe or purchased at bakery
    ('51301050', 'Bánh mì lúa mì hoặc lúa mì xay vỡ, tự làm tại nhà hoặc mua ở tiệm bánh, nướng giòn'), -- Bread, wheat or cracked wheat, made from home recipe or purchased at bakery, toasted
    ('51301120', 'Bánh mì lúa mì hoặc lúa mì xay vỡ, có nho khô'), -- Bread, wheat or cracked wheat, with raisins
    ('51301130', 'Bánh mì lúa mì hoặc lúa mì xay vỡ, có nho khô, nướng giòn'), -- Bread, wheat or cracked wheat, with raisins, toasted
    ('51301510', 'Bánh mì lúa mì hoặc lúa mì xay vỡ, giảm calo và/hoặc giàu chất xơ'), -- Bread, wheat or cracked wheat, reduced calorie and/or high fiber
    ('51301520', 'Bánh mì lúa mì hoặc lúa mì xay vỡ, giảm calo và/hoặc giàu chất xơ, nướng giòn'), -- Bread, wheat or cracked wheat, reduced calorie and/or high fiber, toasted
    ('51301540', 'Bánh mì Pháp hoặc Vienna, lúa mì nguyên cám'), -- Bread, French or Vienna, whole wheat
    ('51301550', 'Bánh mì Pháp hoặc Vienna, lúa mì nguyên cám, nướng giòn'), -- Bread, French or Vienna, whole wheat, toasted
    ('51301600', 'Bánh mì pita lúa mì nguyên cám'), -- Bread, pita, whole wheat
    ('51301620', 'Bánh mì pita lúa mì hoặc lúa mì xay vỡ'), -- Bread, pita, wheat or cracked wheat
    ('51301700', 'Bánh bagel lúa mì'), -- Bagel, wheat
    ('51301750', 'Bánh bagel lúa mì nguyên cám'), -- Bagel, whole wheat
    ('51301800', 'Bánh bagel lúa mì, có nho khô'), -- Bagel, wheat, with raisins
    ('51301805', 'Bánh bagel lúa mì nguyên cám, có nho khô'), -- Bagel, whole wheat, with raisins
    ('51301820', 'Bánh bagel lúa mì, có trái cây và hạt'), -- Bagel, wheat, with fruit and nuts
    ('51301900', 'Bánh bagel cám lúa mì'), -- Bagel, wheat bran
    ('51302500', 'Bánh muffin kiểu Anh, cám lúa mì'), -- Muffin, English, wheat bran
    ('51302520', 'Bánh muffin kiểu Anh, cám lúa mì, có nho khô'), -- Muffin, English, wheat bran, with raisins
    ('51303010', 'Bánh muffin kiểu Anh, lúa mì hoặc lúa mì xay vỡ'), -- Muffin, English, wheat or cracked wheat
    ('51303030', 'Bánh muffin kiểu Anh, lúa mì nguyên cám'), -- Muffin, English, whole wheat
    ('51303050', 'Bánh muffin kiểu Anh, lúa mì hoặc lúa mì xay vỡ, có nho khô'), -- Muffin, English, wheat or cracked wheat, with raisins
    ('51303070', 'Bánh muffin kiểu Anh, lúa mì nguyên cám, có nho khô'), -- Muffin, English, whole wheat, with raisins
    ('51303100', 'Bánh muffin kiểu Anh, trắng nguyên hạt'), -- Muffin, English, whole grain white
    ('51306000', 'Bánh mì que giòn, lúa mì nguyên cám'), -- Breadsticks, hard, whole wheat
    ('51320010', 'Bánh mì cuộn nhỏ lúa mì hoặc lúa mì xay vỡ'), -- Roll, wheat or cracked wheat
    ('51320060', 'Bánh mì cuộn nhỏ lúa mì hoặc lúa mì xay vỡ, bánh bun hot dog'), -- Roll, wheat or cracked wheat, hot dog bun
    ('51320070', 'Bánh mì cuộn nhỏ lúa mì hoặc lúa mì xay vỡ, bánh bun hamburger'), -- Roll, wheat or cracked wheat, hamburger bun
    ('51320500', 'Bánh mì cuộn nhỏ lúa mì nguyên cám'), -- Roll, whole wheat
    ('51320550', 'Bánh mì cuộn nhỏ lúa mì nguyên cám, bánh bun hot dog'), -- Roll, whole wheat, hot dog bun
    ('51320560', 'Bánh mì cuộn nhỏ lúa mì nguyên cám, bánh bun hamburger'), -- Roll, whole wheat, hamburger bun
    ('51320700', 'Bánh mì cuộn nhỏ trắng nguyên hạt'), -- Roll, whole grain white
    ('51320710', 'Bánh mì cuộn nhỏ trắng nguyên hạt, bánh bun hot dog'), -- Roll, whole grain white, hot dog bun
    ('51320720', 'Bánh mì cuộn nhỏ trắng nguyên hạt, bánh bun hamburger'), -- Roll, whole grain white, hamburger bun
    ('51401010', 'Bánh mì lúa mạch đen'), -- Bread, rye
    ('51401020', 'Bánh mì lúa mạch đen, nướng giòn'), -- Bread, rye, toasted
    ('51401030', 'Bánh mì vân lúa mạch đen và pumpernickel'), -- Bread, marble rye and pumpernickel
    ('51401040', 'Bánh mì vân lúa mạch đen và pumpernickel, nướng giòn'), -- Bread, marble rye and pumpernickel, toasted
    ('51401200', 'Bánh muffin kiểu Anh, lúa mạch đen'), -- Muffin, English, rye
    ('51404010', 'Bánh mì đen pumpernickel'), -- Bread, pumpernickel
    ('51404020', 'Bánh mì đen pumpernickel, nướng giòn'), -- Bread, pumpernickel, toasted
    ('51404500', 'Bánh bagel pumpernickel'), -- Bagel, pumpernickel
    ('51404550', 'Bánh muffin kiểu Anh, pumpernickel'), -- Muffin, English, pumpernickel
    ('51407010', 'Bánh mì đen'), -- Bread, black
    ('51407020', 'Bánh mì đen, nướng giòn'), -- Bread, black, toasted
    ('51420000', 'Bánh mì cuộn nhỏ lúa mạch đen'), -- Roll, rye
    ('51421000', 'Bánh mì cuộn nhỏ pumpernickel'), -- Roll, pumpernickel
    ('51501010', 'Bánh mì yến mạch'), -- Bread, oatmeal
    ('51501020', 'Bánh mì yến mạch, nướng giòn'), -- Bread, oatmeal, toasted
    ('51501040', 'Bánh mì cám yến mạch'), -- Bread, oat bran
    ('51501050', 'Bánh mì cám yến mạch, nướng giòn'), -- Bread, oat bran, toasted
    ('51501080', 'Bánh bagel cám yến mạch'), -- Bagel, oat bran
    ('51502010', 'Bánh mì cuộn nhỏ yến mạch'), -- Roll, oatmeal
    ('51503000', 'Bánh muffin kiểu Anh, cám yến mạch'), -- Muffin, English, oat bran
    ('51503040', 'Bánh muffin kiểu Anh, cám yến mạch, có nho khô'), -- Muffin, English, oat bran, with raisins
    ('51601010', 'Bánh mì đa ngũ cốc, nướng giòn'), -- Bread, multigrain, toasted
    ('51601020', 'Bánh mì đa ngũ cốc'), -- Bread, multigrain
    ('51601210', 'Bánh mì đa ngũ cốc, có nho khô'), -- Bread, multigrain, with raisins
    ('51601220', 'Bánh mì đa ngũ cốc, có nho khô, nướng giòn'), -- Bread, multigrain, with raisins, toasted
    ('51602010', 'Bánh mì đa ngũ cốc, giảm calo và/hoặc giàu chất xơ'), -- Bread, multigrain, reduced calorie and/or high fiber
    ('51602020', 'Bánh mì đa ngũ cốc, giảm calo và/hoặc giàu chất xơ, nướng giòn'), -- Bread, multigrain, reduced calorie and/or high fiber, toasted
    ('51620000', 'Bánh mì cuộn nhỏ đa ngũ cốc'), -- Roll, multigrain
    ('51620020', 'Bánh mì cuộn nhỏ đa ngũ cốc, bánh bun hot dog'), -- Roll, multigrain, hot dog bun
    ('51620030', 'Bánh mì bun hamburger, đa ngũ cốc'), -- Roll, multigrain, hamburger bun
    ('51630000', 'Bánh bagel đa ngũ cốc'), -- Bagel, multigrain
    ('51630100', 'Bánh bagel đa ngũ cốc, có nho khô'), -- Bagel, multigrain, with raisins
    ('51630200', 'Bánh muffin kiểu Anh, đa ngũ cốc'), -- Muffin, English, multigrain
    ('51801010', 'Bánh mì lúa mạch'), -- Bread, barley
    ('51801020', 'Bánh mì lúa mạch, nướng giòn'), -- Bread, barley, toasted
    ('51804010', 'Bánh mì đậu tương'), -- Bread, soy
    ('51804020', 'Bánh mì đậu tương, nướng giòn'), -- Bread, soy, toasted
    ('51805010', 'Bánh mì bột hạt hướng dương'), -- Bread, sunflower meal
    ('51805020', 'Bánh mì bột hạt hướng dương, nướng giòn'), -- Bread, sunflower meal, toasted
    ('51806010', 'Bánh mì bột gạo'), -- Bread, rice
    ('51806020', 'Bánh mì bột gạo, nướng giòn'), -- Bread, rice, toasted
    ('51807000', 'Bánh injera (bánh mì Ethiopia)'), -- Injera, Ethiopian bread
    ('51808000', 'Bánh mì không gluten'), -- Bread, gluten free
    ('51808010', 'Bánh mì không gluten, nướng giòn'), -- Bread, gluten free, toasted
    ('51808050', 'Bánh mì que giòn, không gluten'), -- Breadsticks, hard, gluten free
    ('51808100', 'Bánh mì cuộn nhỏ không gluten'), -- Roll, gluten free
    ('52101000', 'Bánh biscuit, loại chung'), -- Biscuit, NFS
    ('52102040', 'Bánh biscuit, từ bột nhào làm sẵn bảo quản lạnh'), -- Biscuit, from refrigerated dough
    ('52103000', 'Bánh biscuit, đồ ăn nhanh/nhà hàng'), -- Biscuit, from fast food / restaurant
    ('52104010', 'Bánh biscuit, tự làm tại nhà'), -- Biscuit, home recipe
    ('52104040', 'Bánh biscuit lúa mì'), -- Biscuit, wheat
    ('52104100', 'Bánh biscuit phô mai'), -- Biscuit, cheese
    ('52104200', 'Bánh biscuit có trái cây'), -- Biscuit with fruit
    ('52105100', 'Bánh scone'), -- Scone
    ('52105200', 'Bánh scone, có trái cây'), -- Scone, with fruit
    ('52201000', 'Bánh mì ngô, từ bột pha sẵn'), -- Cornbread, prepared from mix
    ('52202060', 'Bánh mì ngô, tự làm tại nhà'), -- Cornbread, made from home recipe
    ('52204000', 'Món nhồi bánh mì ngô (stuffing)'), -- Cornbread stuffing
    ('52206010', 'Bánh mì ngô dạng muffin, que hoặc tròn'), -- Cornbread muffin, stick, round
    ('52206060', 'Bánh mì ngô dạng muffin, que hoặc tròn, tự làm tại nhà'), -- Cornbread muffin, stick, round, made from home recipe
    ('52207010', 'Bánh bột ngô dạng miếng hoặc tart, chiên'), -- Corn flour patty or tart, fried
    ('52208010', 'Bánh bột ngô corn pone, nướng lò'), -- Corn pone, baked
    ('52208020', 'Bánh bột ngô corn pone, chiên'), -- Corn pone, fried
    ('52208760', 'Vỏ bánh gordita/sope, không hương vị, không nhân'), -- Gordita/sope shell, plain, no filling
    ('52209010', 'Bánh hush puppy (viên bột ngô chiên)'), -- Hush puppy
    ('52211010', 'Bánh johnnycake (bánh bột ngô áp chảo)'), -- Johnnycake
    ('52213010', 'Bánh spoonbread (bánh bột ngô mềm)'), -- Spoonbread
    ('52215000', 'Bánh tortilla, loại chung'), -- Tortilla, NFS
    ('52215100', 'Bánh tortilla ngô'), -- Tortilla, corn
    ('52215200', 'Bánh tortilla bột mì'), -- Tortilla, flour
    ('52215260', 'Bánh tortilla lúa mì nguyên cám'), -- Tortilla, whole wheat
    ('52215300', 'Vỏ bánh taco ngô'), -- Taco shell, corn
    ('52215350', 'Vỏ bánh taco bột mì'), -- Taco shell, flour
    ('52220110', 'Bánh arepa kiểu Dominica'), -- Arepa Dominicana
    ('52301000', 'Bánh muffin, loại chung'), -- Muffin, NFS
    ('52302010', 'Bánh muffin trái cây'), -- Muffin, fruit
    ('52302020', 'Bánh muffin trái cây, ít béo'), -- Muffin, fruit, low fat
    ('52302500', 'Bánh muffin sô-cô-la chip'), -- Muffin, chocolate chip
    ('52302600', 'Bánh muffin sô-cô-la'), -- Muffin, chocolate
    ('52303010', 'Bánh muffin lúa mì nguyên cám'), -- Muffin, whole wheat
    ('52303500', 'Bánh muffin lúa mì'), -- Muffin, wheat
    ('52304000', 'Bánh muffin ngũ cốc nguyên hạt'), -- Muffin, whole grain
    ('52304010', 'Bánh muffin cám lúa mì'), -- Muffin, wheat bran
    ('52304040', 'Bánh muffin cám có trái cây, ít béo'), -- Muffin, bran with fruit, lowfat
    ('52304100', 'Bánh muffin yến mạch'), -- Muffin, oatmeal
    ('52304150', 'Bánh muffin cám yến mạch'), -- Muffin, oat bran
    ('52306010', 'Bánh muffin không hương vị'), -- Muffin, plain
    ('52306300', 'Bánh muffin phô mai'), -- Muffin, cheese
    ('52306500', 'Bánh muffin bí đỏ'), -- Muffin, pumpkin
    ('52306550', 'Bánh muffin bí ngòi'), -- Muffin, zucchini
    ('52306700', 'Bánh muffin cà rốt'), -- Muffin, carrot
    ('52311010', 'Bánh popover (bánh nướng rỗng ruột)'), -- Popover
    ('52401000', 'Bánh mì nâu Boston'), -- Bread, Boston Brown
    ('52403000', 'Bánh mì các loại hạt'), -- Bread, nut
    ('52404060', 'Bánh mì bí đỏ'), -- Bread, pumpkin
    ('52405010', 'Bánh mì trái cây'), -- Bread, fruit
    ('52407000', 'Bánh mì bí ngòi'), -- Bread, zucchini
    ('52408000', 'Bánh mì soda Ireland'), -- Bread, Irish soda
    ('53100100', 'Bánh ngọt hoặc bánh cupcake, loại chung'), -- Cake or cupcake, NFS
    ('53101100', 'Bánh ngọt angel food (bông lan lòng trắng trứng)'), -- Cake, angel food
    ('53102200', 'Bánh ngọt hoặc bánh cupcake táo'), -- Cake or cupcake, apple
    ('53102700', 'Bánh ngọt hoặc bánh cupcake chuối'), -- Cake or cupcake, banana
    ('53102800', 'Bánh ngọt hoặc bánh cupcake Rừng Đen (Black Forest)'), -- Cake or cupcake, Black Forest
    ('53103000', 'Bánh ngọt Boston cream pie'), -- Cake, Boston cream pie
    ('53104260', 'Bánh ngọt hoặc bánh cupcake cà rốt'), -- Cake or cupcake, carrot
    ('53104400', 'Bánh ngọt hoặc bánh cupcake dừa'), -- Cake or cupcake, coconut
    ('53104500', 'Bánh cheesecake, không hương vị'), -- Cheesecake, plain
    ('53104550', 'Bánh cheesecake trái cây'), -- Cheesecake, fruit
    ('53104600', 'Bánh cheesecake sô-cô-la'), -- Cheesecake, chocolate
    ('53105262', 'Bánh ngọt hoặc bánh cupcake sô-cô-la phủ kem trắng, mua ở tiệm bánh'), -- Cake or cupcake, chocolate with white icing, bakery
    ('53105264', 'Bánh ngọt hoặc bánh cupcake sô-cô-la phủ kem trắng, từ bột pha sẵn'), -- Cake or cupcake, chocolate with white icing, from mix
    ('53105270', 'Bánh ngọt hoặc bánh cupcake sô-cô-la phủ kem sô-cô-la, mua ở tiệm bánh'), -- Cake or cupcake, chocolate with chocolate icing, bakery
    ('53105272', 'Bánh ngọt hoặc bánh cupcake sô-cô-la phủ kem sô-cô-la, từ bột pha sẵn'), -- Cake or cupcake, chocolate with chocolate icing, from mix
    ('53105275', 'Bánh ngọt hoặc bánh cupcake sô-cô-la, không phủ kem'), -- Cake or cupcake, chocolate, no icing
    ('53105300', 'Bánh ngọt hoặc bánh cupcake sô-cô-la kiểu Đức (German chocolate)'), -- Cake or cupcake, German chocolate
    ('53105310', 'Bánh ngọt hoặc bánh cupcake không gluten'), -- Cake or cupcake, gluten free
    ('53105396', 'Bánh ngọt sô-cô-la không bột'), -- Cake, chocolate, flourless
    ('53106500', 'Bánh ngọt kem'), -- Cake, cream
    ('53108200', 'Bánh ngọt ăn vặt sô-cô-la'), -- Snack cake, chocolate
    ('53109200', 'Bánh ngọt ăn vặt cốt trắng'), -- Snack cake, white
    ('53110000', 'Bánh ngọt trái cây (fruitcake)'), -- Cake, fruit cake
    ('53111000', 'Bánh ngọt hoặc bánh cupcake gừng'), -- Cake or cupcake, gingerbread
    ('53112100', 'Bánh kem lạnh (ice cream cake)'), -- Ice cream cake
    ('53113000', 'Bánh cuộn mứt (jelly roll)'), -- Cake, jelly roll
    ('53114100', 'Bánh ngọt hoặc bánh cupcake chanh vàng'), -- Cake or cupcake, lemon
    ('53115200', 'Bánh ngọt hoặc bánh cupcake vân cẩm thạch (marble)'), -- Cake or cupcake, marble
    ('53115450', 'Bánh ngọt hoặc bánh cupcake bơ lạc'), -- Cake or cupcake, peanut butter
    ('53116000', 'Bánh bông lan bơ (pound cake)'), -- Cake, pound
    ('53116510', 'Bánh ngọt hoặc bánh cupcake bí đỏ'), -- Cake or cupcake, pumpkin
    ('53116520', 'Bánh ngọt hoặc bánh cupcake red velvet (nhung đỏ)'), -- Cake or cupcake, red velvet
    ('53117200', 'Bánh ngọt hoặc bánh cupcake gia vị'), -- Cake or cupcake, spice
    ('53118100', 'Bánh bông lan (sponge cake)'), -- Cake, sponge
    ('53118110', 'Bánh ngọt hoặc bánh cupcake dâu tây'), -- Cake or cupcake, strawberry
    ('53118500', 'Bánh torte (bánh ngọt nhiều lớp)'), -- Cake, torte
    ('53118550', 'Bánh tres leches (bánh ba loại sữa)'), -- Cake, tres leche
    ('53119000', 'Bánh ngọt dứa úp ngược (upside down)'), -- Cake, pineapple, upside down
    ('53120270', 'Bánh ngọt hoặc bánh cupcake cốt trắng phủ kem trắng, mua ở tiệm bánh'), -- Cake or cupcake, white with white icing, bakery
    ('53120272', 'Bánh ngọt hoặc bánh cupcake cốt trắng phủ kem trắng, từ bột pha sẵn'), -- Cake or cupcake, white with white icing, from mix
    ('53121270', 'Bánh ngọt hoặc bánh cupcake cốt trắng phủ kem sô-cô-la, mua ở tiệm bánh'), -- Cake or cupcake, white with chocolate icing, bakery
    ('53121272', 'Bánh ngọt hoặc bánh cupcake cốt trắng phủ kem sô-cô-la, từ bột pha sẵn'), -- Cake or cupcake, white with chocolate icing, from mix
    ('53121275', 'Bánh ngọt hoặc bánh cupcake cốt trắng, không phủ kem'), -- Cake or cupcake, white, no icing
    ('53123070', 'Bánh shortcake dâu tây'), -- Cake, strawberry shortcake
    ('53124110', 'Bánh ngọt hoặc bánh cupcake bí ngòi'), -- Cake or cupcake, zucchini
    ('53200100', 'Bột bánh quy sống, dạng bột lỏng hoặc bột nhào'), -- Cookie, batter or dough, raw
    ('53201000', 'Bánh quy, loại chung'), -- Cookie, NFS
    ('53202000', 'Bánh quy hạnh nhân'), -- Cookie, almond
    ('53203000', 'Bánh quy táo nghiền (applesauce)'), -- Cookie, applesauce
    ('53203500', 'Bánh quy biscotti'), -- Cookie, biscotti
    ('53204000', 'Bánh brownie, không rõ có phủ kem'), -- Cookie, brownie, NS as to icing
    ('53204010', 'Bánh brownie, không phủ kem'), -- Cookie, brownie, without icing
    ('53204100', 'Bánh brownie, có phủ kem hoặc nhân'), -- Cookie, brownie, with icing or filling
    ('53204840', 'Bánh brownie, giảm béo, không rõ có phủ kem'), -- Cookie, brownie, reduced fat, NS as to icing
    ('53205250', 'Bánh brownie vị butterscotch (kẹo bơ đường)'), -- Cookie, butterscotch, brownie
    ('53205260', 'Bánh quy thanh, có sô-cô-la'), -- Cookie, bar, with chocolate
    ('53206000', 'Bánh quy sô-cô-la chip'), -- Cookie, chocolate chip
    ('53206020', 'Bánh quy sô-cô-la chip, tự làm tại nhà hoặc mua ở tiệm bánh'), -- Cookie, chocolate chip, made from home recipe or purchased at a bakery
    ('53206030', 'Bánh quy sô-cô-la chip, giảm béo'), -- Cookie, chocolate chip, reduced fat
    ('53206100', 'Bánh quy kẹp sô-cô-la chip'), -- Cookie, chocolate chip sandwich
    ('53206500', 'Bánh quy sô-cô-la, làm với ngũ cốc gạo giòn'), -- Cookie, chocolate, made with rice cereal
    ('53206550', 'Bánh quy sô-cô-la, làm với yến mạch và dừa, không nướng'), -- Cookie, chocolate, made with oatmeal and coconut, no bake
    ('53207000', 'Bánh quy sô-cô-la hoặc fudge'), -- Cookie, chocolate or fudge
    ('53208000', 'Bánh quy marshmallow, phủ sô-cô-la'), -- Cookie, marshmallow, chocolate-covered
    ('53208200', 'Bánh quy kẹp marshmallow kiểu pie, phủ sô-cô-la'), -- Cookie, marshmallow pie, chocolate covered
    ('53209005', 'Bánh quy sô-cô-la, có phủ kem hoặc lớp phủ'), -- Cookie, chocolate, with icing or coating
    ('53209010', 'Bánh quy xốp kẹp kem, phủ sô-cô-la'), -- Cookie, sugar wafer, chocolate-covered
    ('53209015', 'Bánh quy kẹp sô-cô-la'), -- Cookie, chocolate sandwich
    ('53209020', 'Bánh quy kẹp sô-cô-la, giảm béo'), -- Cookie, chocolate sandwich, reduced fat
    ('53209100', 'Bánh quy kẹp sô-cô-la, thêm nhân'), -- Cookie, chocolate, sandwich, with extra filling
    ('53209500', 'Bánh quy kẹp sô-cô-la và vani'), -- Cookie, chocolate and vanilla sandwich
    ('53210000', 'Bánh quy xốp sô-cô-la'), -- Cookie, chocolate wafer
    ('53210900', 'Bánh quy graham với sô-cô-la và marshmallow'), -- Cookie, graham cracker with chocolate and marshmallow
    ('53211000', 'Bánh quy thanh, có sô-cô-la, các loại hạt và bánh quy graham'), -- Cookie bar, with chocolate, nuts, and graham crackers
    ('53215500', 'Bánh quy dừa'), -- Cookie, coconut
    ('53220000', 'Bánh quy thanh nhân trái cây'), -- Cookie, fruit-filled bar
    ('53220030', 'Bánh quy thanh nhân sung'), -- Cookie, fig bar
    ('53222010', 'Bánh quy may mắn'), -- Cookie, fortune
    ('53222020', 'Vỏ ốc quế đựng kem, loại bánh xốp hoặc bánh ngọt'), -- Cookie, cone shell, ice cream type, wafer or cake
    ('53223000', 'Bánh quy gừng giòn'), -- Cookie, gingersnaps
    ('53223100', 'Bánh quy granola'), -- Cookie, granola
    ('53224000', 'Bánh quy sâm panh (ladyfinger)'), -- Cookie, ladyfinger
    ('53224250', 'Bánh quy thanh chanh vàng'), -- Cookie, lemon bar
    ('53225000', 'Bánh quy macaroon'), -- Cookie, macaroon
    ('53226000', 'Bánh quy marshmallow, có dừa'), -- Cookie, marshmallow, with coconut
    ('53226500', 'Bánh quy marshmallow, có ngũ cốc gạo giòn, không nướng'), -- Cookie, marshmallow, with rice cereal, no bake
    ('53226550', 'Bánh quy marshmallow, có ngũ cốc gạo giòn và sô-cô-la chip'), -- Cookie, marshmallow, with rice cereal and chocolate chips
    ('53226600', 'Bánh quy marshmallow và bơ lạc, có ngũ cốc yến mạch, không nướng'), -- Cookie, marshmallow and peanut butter, with oat cereal, no bake
    ('53228000', 'Bánh quy meringue (lòng trắng trứng đánh bông)'), -- Cookie, meringue
    ('53230000', 'Bánh quy mật mía'), -- Cookie, molasses
    ('53231000', 'Bánh quy Lebkuchen (Đức)'), -- Cookie, Lebkuchen
    ('53231400', 'Bánh quy đa ngũ cốc, nhiều chất xơ'), -- Cookie, multigrain, high fiber
    ('53233000', 'Bánh quy yến mạch'), -- Cookie, oatmeal
    ('53233010', 'Bánh quy yến mạch, có nho khô'), -- Cookie, oatmeal, with raisins
    ('53233040', 'Bánh quy yến mạch, giảm béo, không rõ có nho khô'), -- Cookie, oatmeal, reduced fat, NS as to raisins
    ('53233050', 'Bánh quy yến mạch kẹp, nhân kem'), -- Cookie, oatmeal sandwich, with creme filling
    ('53233060', 'Bánh quy yến mạch, có sô-cô-la chip'), -- Cookie, oatmeal, with chocolate chips
    ('53233080', 'Bánh quy yến mạch kẹp, nhân bơ lạc và mứt'), -- Cookie, oatmeal sandwich, with peanut butter and jelly filling
    ('53233100', 'Bánh quy yến mạch, có sô-cô-la và bơ lạc, không nướng'), -- Cookie, oatmeal, with chocolate and peanut butter, no bake
    ('53234000', 'Bánh quy bơ lạc'), -- Cookie, peanut butter
    ('53234100', 'Bánh quy bơ lạc, có sô-cô-la'), -- Cookie, peanut butter, with chocolate
    ('53234250', 'Bánh quy bơ lạc có ngũ cốc gạo giòn, không nướng'), -- Cookie, peanut butter with rice cereal, no bake
    ('53235000', 'Bánh quy kẹp bơ lạc'), -- Cookie, peanut butter sandwich
    ('53235500', 'Bánh quy nhân bơ lạc, phủ sô-cô-la'), -- Cookie, with peanut butter filling, chocolate-coated
    ('53235600', 'Bánh quy Pfeffernusse (Đức)'), -- Cookie, Pfeffernusse
    ('53236000', 'Bánh quy Pizzelle (Ý)'), -- Cookie, Pizzelle
    ('53236100', 'Bánh quy bí đỏ'), -- Cookie, pumpkin
    ('53237000', 'Bánh quy nho khô'), -- Cookie, raisin
    ('53237010', 'Bánh quy kẹp nho khô, nhân kem'), -- Cookie, raisin sandwich, cream-filled
    ('53237500', 'Bánh quy viên rượu rum (rum ball), không nướng'), -- Cookie, rum ball, no bake
    ('53238000', 'Bánh quy kẹp, không phải vị sô-cô-la hay vani'), -- Cookie, sandwich-type, not chocolate or vanilla
    ('53239000', 'Bánh quy bơ giòn (shortbread)'), -- Cookie, shortbread
    ('53239010', 'Bánh quy bơ giòn (shortbread), giảm béo'), -- Cookie, shortbread, reduced fat
    ('53239050', 'Bánh quy bơ giòn (shortbread), có phủ kem hoặc nhân'), -- Cookie, shortbread, with icing or filling
    ('53239100', 'Bánh que Pocky'), -- Pocky
    ('53240000', 'Bánh quy hình thú'), -- Cookie, animal
    ('53240010', 'Bánh quy hình thú, có phủ kem hoặc phủ đường'), -- Cookie, animal, with frosting or icing
    ('53241500', 'Bánh quy bơ hoặc bánh quy đường'), -- Cookie, butter or sugar
    ('53241510', 'Bánh quy Marie'), -- Marie biscuit
    ('53241600', 'Bánh quy bơ hoặc bánh quy đường, có trái cây và/hoặc các loại hạt'), -- Cookie, butter or sugar, with fruit and/or nuts
    ('53242000', 'Bánh quy xốp kẹp kem'), -- Cookie, sugar wafer
    ('53242500', 'Bánh quy thanh kẹo bơ cứng (toffee)'), -- Cookie, toffee bar
    ('53243000', 'Bánh quy kẹp vani'), -- Cookie, vanilla sandwich
    ('53243010', 'Bánh quy kẹp vani, thêm nhân'), -- Cookie, vanilla sandwich, extra filling
    ('53243050', 'Bánh quy kẹp vani, giảm béo'), -- Cookie, vanilla sandwich, reduced fat
    ('53244010', 'Bánh quy bơ hoặc bánh quy đường, có phủ kem hoặc nhân sô-cô-la'), -- Cookie, butter or sugar, with chocolate icing or filling
    ('53244020', 'Bánh quy bơ hoặc bánh quy đường, có phủ kem hoặc nhân không phải sô-cô-la'), -- Cookie, butter or sugar, with icing or filling other than chocolate
    ('53246000', 'Bánh quy trà kiểu Nhật'), -- Cookie, tea, Japanese
    ('53247000', 'Bánh quy xốp vani'), -- Cookie, vanilla wafer
    ('53247050', 'Bánh quy xốp vani, giảm béo'), -- Cookie, vanilla wafer, reduced fat
    ('53247500', 'Bánh quy vani phủ caramel, dừa và sô-cô-la'), -- Cookie, vanilla with caramel, coconut, and chocolate coating
    ('53251100', 'Bánh quy rugelach'), -- Cookie, rugelach
    ('53260030', 'Bánh quy sô-cô-la chip, giảm đường'), -- Cookie, chocolate chip, reduced sugar
    ('53260200', 'Bánh quy yến mạch, giảm đường'), -- Cookie, oatmeal, reduced sugar
    ('53260300', 'Bánh quy kẹp, giảm đường'), -- Cookie, sandwich, reduced sugar
    ('53260400', 'Bánh quy đường (sugar cookie) hoặc không hương vị, không đường'), -- Cookie, sugar or plain, sugar free
    ('53260500', 'Bánh quy xốp kẹp kem, không đường'), -- Cookie, sugar wafer, sugar free
    ('53260600', 'Bánh quy bơ lạc, không đường'), -- Cookie, peanut butter, sugar free
    ('53261000', 'Bánh quy không gluten'), -- Cookie, gluten free
    ('53270100', 'Bánh quy kiểu Puerto Rico'), -- Cookies, Puerto Rican style
    ('53300100', 'Bánh pie, loại chung'), -- Pie, NFS
    ('53300200', 'Bánh tart, mọi loại'), -- Tart, all types
    ('53301000', 'Bánh pie táo'), -- Pie, apple
    ('53301070', 'Bánh pie táo, đồ ăn nhanh'), -- Pie, apple, fast food
    ('53303500', 'Bánh pie quả mọng'), -- Pie, berry
    ('53304000', 'Bánh pie việt quất'), -- Pie, blueberry
    ('53305000', 'Bánh pie anh đào'), -- Pie, cherry
    ('53305700', 'Bánh pie chanh vàng'), -- Pie, lemon
    ('53305800', 'Bánh pie chanh xanh (key lime)'), -- Pie, key lime
    ('53307000', 'Bánh pie đào'), -- Pie, peach
    ('53312000', 'Bánh pie dâu tây'), -- Pie, strawberry
    ('53341000', 'Bánh pie kem chuối'), -- Pie, banana cream
    ('53342000', 'Bánh pie kem sô-cô-la'), -- Pie, chocolate cream
    ('53343000', 'Bánh pie kem dừa'), -- Pie, coconut cream
    ('53344000', 'Bánh pie custard (kem trứng)'), -- Pie, custard
    ('53344300', 'Pizza tráng miệng'), -- Dessert pizza
    ('53346000', 'Bánh pie kem bơ lạc'), -- Pie, peanut butter cream
    ('53346500', 'Bánh pie kem dứa'), -- Pie, pineapple cream
    ('53347000', 'Bánh pie bí đỏ'), -- Pie, pumpkin
    ('53360000', 'Bánh pie khoai lang'), -- Pie, sweet potato
    ('53365000', 'Bánh pie kem vani'), -- Pie, vanilla cream
    ('53381000', 'Bánh pie chanh vàng phủ meringue'), -- Pie, lemon meringue
    ('53385000', 'Bánh pie hồ đào (pecan)'), -- Pie, pecan
    ('53385500', 'Bánh pie yến mạch'), -- Pie, oatmeal
    ('53391000', 'Vỏ bánh pie'), -- Pie shell
    ('53391100', 'Vỏ bánh pie làm từ bánh quy graham'), -- Pie shell, graham cracker
    ('53410100', 'Bánh cobbler táo'), -- Cobbler, apple
    ('53410300', 'Bánh cobbler quả mọng'), -- Cobbler, berry
    ('53410500', 'Bánh cobbler anh đào'), -- Cobbler, cherry
    ('53410800', 'Bánh cobbler đào'), -- Cobbler, peach
    ('53415100', 'Bánh crisp táo'), -- Crisp, apple
    ('53415110', 'Bánh rán fritter, không hương vị'), -- Fritter, plain
    ('53415120', 'Bánh rán fritter trái cây'), -- Fritter, fruit
    ('53415300', 'Bánh crisp quả mọng'), -- Crisp, berry
    ('53415400', 'Bánh crisp anh đào'), -- Crisp, cherry
    ('53415500', 'Bánh crisp đào'), -- Crisp, peach
    ('53420000', 'Bánh su kem, bánh eclair, nhân custard hoặc kem, không rõ có lớp phủ'), -- Cream puff, eclair, custard or cream filled, NS as to icing
    ('53420100', 'Bánh su kem, bánh eclair, nhân custard hoặc kem, không có lớp phủ'), -- Cream puff, eclair, custard or cream filled, not iced
    ('53420200', 'Bánh su kem, bánh eclair, nhân custard hoặc kem, có lớp phủ'), -- Cream puff, eclair, custard or cream filled, iced
    ('53420210', 'Bánh su kem, bánh eclair, nhân custard hoặc kem, có lớp phủ, giảm béo'), -- Cream puff, eclair, custard or cream filled, iced, reduced fat
    ('53420250', 'Vỏ bánh su, không nhân và không lớp phủ'), -- Cream puff, no filling or icing
    ('53420400', 'Bánh sopaipilla, không siro hoặc mật ong'), -- Sopaipilla, without syrup or honey
    ('53420410', 'Bánh sopaipilla có siro hoặc mật ong'), -- Sopaipilla with syrup or honey
    ('53430100', 'Bánh crepe nhân sô-cô-la'), -- Crepe, chocolate filled
    ('53430200', 'Bánh crepe nhân trái cây'), -- Crepe, fruit filled
    ('53430700', 'Bánh tamale ngọt'), -- Tamale, sweet
    ('53440000', 'Bánh strudel táo'), -- Strudel, apple
    ('53440300', 'Bánh strudel quả mọng'), -- Strudel, berry
    ('53440500', 'Bánh strudel anh đào'), -- Strudel, cherry
    ('53440600', 'Bánh strudel phô mai'), -- Strudel, cheese
    ('53440700', 'Bánh strudel đào'), -- Strudel, peach
    ('53441110', 'Bánh baklava'), -- Baklava
    ('53441210', 'Bánh basbousa (Trung Đông)'), -- Basbousa
    ('53450000', 'Bánh gối nhân trái cây (turnover)'), -- Turnover, fruit
    ('53452100', 'Bánh ngọt nướng nhân trái cây'), -- Pastry, fruit-filled
    ('53452120', 'Bánh ngọt nướng lò, nhân đậu nghiền hoặc hạt sen nghiền'), -- Pastry, made with bean or lotus seed paste filling, baked
    ('53452130', 'Bánh ngọt nướng lò, nhân đậu nghiền và lòng đỏ trứng muối'), -- Pastry, made with bean paste and salted egg yolk filling, baked
    ('53452150', 'Bánh ngọt kiểu Trung Quốc, làm từ bột gạo'), -- Pastry, Chinese, made with rice flour
    ('53452170', 'Bánh ngọt kiểu bánh quy, chiên'), -- Pastry, cookie type, fried
    ('53452200', 'Bánh ngọt nướng kiểu Ý, có phô mai'), -- Pastry, Italian, with cheese
    ('53452400', 'Bánh ngàn lớp (puff pastry)'), -- Pastry, puff
    ('53452420', 'Bánh ngàn lớp, nhân custard hoặc kem, có hoặc không phủ kem'), -- Pastry, puff, custard or cream filled, iced or not iced
    ('53452450', 'Bánh ngàn lớp phô mai viên nhỏ (cheese puffs)'), -- Cheese pastry puffs
    ('53452500', 'Bánh bột chiên, chủ yếu từ bột mì và nước'), -- Pastry, mainly flour and water, fried
    ('53453150', 'Bánh empanada nhân trái cây'), -- Empanada, fruit
    ('53500100', 'Bánh ngọt ăn sáng, loại chung'), -- Breakfast pastry, NFS
    ('53510000', 'Bánh Đan Mạch (Danish pastry), không hương vị hoặc vị gia vị'), -- Danish pastry, plain or spice
    ('53510100', 'Bánh Đan Mạch (Danish pastry), có trái cây'), -- Danish pastry, with fruit
    ('53511000', 'Bánh Đan Mạch (Danish pastry), có phô mai'), -- Danish pastry, with cheese
    ('53520000', 'Bánh donut, loại chung'), -- Doughnut, NFS
    ('53520100', 'Bánh donut kiểu bánh ngọt, không hương vị'), -- Doughnut, cake type, plain
    ('53520120', 'Bánh donut sô-cô-la'), -- Doughnut, chocolate
    ('53520130', 'Bánh donut kiểu bánh ngọt, rắc đường bột'), -- Doughnut, cake type, powdered sugar
    ('53520135', 'Bánh donut kiểu bánh ngọt, có phủ kem'), -- Doughnut, cake type, with icing
    ('53520140', 'Bánh donut kiểu bánh ngọt, phủ kem sô-cô-la'), -- Doughnut, cake type, chocolate icing
    ('53520160', 'Bánh donut sô-cô-la, phủ kem sô-cô-la'), -- Doughnut, chocolate, with chocolate icing
    ('53520170', 'Bánh donut viên (doughnut holes)'), -- Doughnut holes
    ('53520200', 'Bánh churros'), -- Churros
    ('53520510', 'Bánh beignet (bánh rán kiểu Pháp)'), -- Beignet
    ('53521110', 'Bánh donut bột men'), -- Doughnut, yeast type
    ('53521130', 'Bánh donut bột men, phủ kem sô-cô-la'), -- Doughnut, yeast type, with chocolate icing
    ('53521140', 'Bánh donut nhân mứt'), -- Doughnut, jelly
    ('53521210', 'Bánh donut nhân custard'), -- Doughnut, custard-filled
    ('53521230', 'Bánh donut nhân custard, có phủ kem'), -- Doughnut, custard-filled, with icing
    ('53530000', 'Bánh tart ăn sáng'), -- Breakfast tart
    ('53530010', 'Bánh tart ăn sáng, ít béo'), -- Breakfast tart, lowfat
    ('53610100', 'Bánh coffee cake, loại phủ vụn bánh hoặc kiểu bánh mì nhanh'), -- Coffee cake, crumb or quick-bread type
    ('53610170', 'Bánh coffee cake, loại phủ vụn bánh hoặc kiểu bánh mì nhanh, có trái cây'), -- Coffee cake, crumb or quick-bread type, with fruit
    ('53610200', 'Bánh coffee cake, loại phủ vụn bánh hoặc kiểu bánh mì nhanh, nhân phô mai'), -- Coffee cake, crumb or quick-bread type, cheese-filled
    ('53710400', 'Thanh ngũ cốc hoặc granola (General Mills Fiber One Chewy Bar)'), -- Cereal or granola bar (General Mills Fiber One Chewy Bar)
    ('53710500', 'Thanh ngũ cốc hoặc granola (Kellogg''s Nutri-Grain Cereal Bar)'), -- Cereal or granola bar (Kellogg's Nutri-Grain Cereal Bar)
    ('53710502', 'Thanh ngũ cốc hoặc granola (Kellogg''s Nutri-Grain Yogurt Bar)'), -- Cereal or granola bar (Kellogg's Nutri-Grain Yogurt Bar)
    ('53710504', 'Thanh ngũ cốc hoặc granola (Kellogg''s Nutri-Grain Fruit and Nut Bar)'), -- Cereal or granola bar (Kellogg's Nutri-Grain Fruit and Nut Bar)
    ('53710600', 'Thanh ngũ cốc sữa (Milk ''n Cereal bar)'), -- Milk 'n Cereal bar
    ('53710700', 'Thanh ngũ cốc hoặc granola (Kellogg''s Special K bar)'), -- Cereal or granola bar (Kellogg's Special K bar)
    ('53710800', 'Thanh ngũ cốc hoặc granola (Kashi Chewy)'), -- Cereal or granola bar (Kashi Chewy)
    ('53710802', 'Thanh ngũ cốc hoặc granola (Kashi Crunchy)'), -- Cereal or granola bar (Kashi Crunchy)
    ('53710810', 'Thanh ngũ cốc hoặc granola (KIND Fruit and Nut Bar)'), -- Cereal or granola bar (KIND Fruit and Nut Bar)
    ('53710900', 'Thanh ngũ cốc hoặc granola (General Mills Nature Valley Chewy Trail Mix)'), -- Cereal or granola bar (General Mills Nature Valley Chewy Trail Mix)
    ('53710902', 'Thanh ngũ cốc hoặc granola, phủ sữa chua (General Mills Nature Valley Chewy Granola Bar)'), -- Cereal or granola bar, with yogurt coating (General Mills Nature Valley Chewy Granola Bar)
    ('53710904', 'Thanh ngũ cốc hoặc granola (General Mills Nature Valley Sweet and Salty Granola Bar)'), -- Cereal or granola bar (General Mills Nature Valley Sweet and Salty Granola Bar)
    ('53710906', 'Thanh ngũ cốc hoặc granola (General Mills Nature Valley Crunchy Granola Bar)'), -- Cereal or granola bar (General Mills Nature Valley Crunchy Granola Bar)
    ('53711000', 'Thanh ngũ cốc hoặc granola (Quaker Chewy Granola Bar)'), -- Cereal or granola bar (Quaker Chewy Granola Bar)
    ('53711002', 'Thanh ngũ cốc hoặc granola (Quaker Chewy 90 Calorie Granola Bar)'), -- Cereal or granola bar (Quaker Chewy 90 Calorie Granola Bar)
    ('53711004', 'Thanh ngũ cốc hoặc granola (Quaker Chewy 25% Less Sugar Granola Bar)'), -- Cereal or granola bar (Quaker Chewy 25% Less Sugar Granola Bar)
    ('53711006', 'Thanh ngũ cốc hoặc granola (Quaker Chewy Dipps Granola Bar)'), -- Cereal or granola bar (Quaker Chewy Dipps Granola Bar)
    ('53711100', 'Thanh ngũ cốc hoặc granola (Quaker Granola Bites)'), -- Cereal or granola bar (Quaker Granola Bites)
    ('53712000', 'Thanh ăn vặt yến mạch'), -- Snack bar, oatmeal
    ('53712100', 'Thanh ngũ cốc hoặc granola, loại chung'), -- Cereal or Granola bar, NFS
    ('53712200', 'Thanh ngũ cốc hoặc granola, ít béo, loại chung'), -- Cereal or granola bar, lowfat, NFS
    ('53712210', 'Thanh ngũ cốc hoặc granola, không béo'), -- Cereal or granola bar, nonfat
    ('53713000', 'Thanh ngũ cốc hoặc granola, giảm đường, loại chung'), -- Cereal or granola bar, reduced sugar, NFS
    ('53713010', 'Thanh ngũ cốc hoặc granola, trái cây và các loại hạt'), -- Cereal or granola bar, fruit and nut
    ('53713100', 'Thanh ngũ cốc hoặc granola, lạc, yến mạch, đường, mầm lúa mì'), -- Cereal or granola bar, peanuts , oats, sugar, wheat germ
    ('53714200', 'Thanh ngũ cốc hoặc granola, phủ sô-cô-la, loại chung'), -- Cereal or granola bar, chocolate coated, NFS
    ('53714210', 'Thanh ngũ cốc hoặc granola, có dừa, phủ sô-cô-la'), -- Cereal or granola bar, with coconut, chocolate coated
    ('53714220', 'Thanh ngũ cốc hoặc granola có các loại hạt, phủ sô-cô-la'), -- Cereal or granola bar with nuts, chocolate coated
    ('53714230', 'Thanh ngũ cốc hoặc granola, yến mạch, các loại hạt, lớp phủ không phải sô-cô-la'), -- Cereal or granola bar, oats, nuts, coated with non-chocolate coating
    ('53714250', 'Thanh ngũ cốc hoặc granola, có lớp phủ không phải sô-cô-la'), -- Cereal or granola bar, coated with non-chocolate coating
    ('53714300', 'Thanh ngũ cốc hoặc granola, nhiều chất xơ, phủ lớp sữa chua không phải sô-cô-la'), -- Cereal or granola bar, high fiber, coated with non-chocolate yogurt coating
    ('53714400', 'Thanh ngũ cốc hoặc granola, có ngũ cốc gạo giòn'), -- Cereal or granola bar, with rice cereal
    ('53714500', 'Thanh ăn sáng, loại chung'), -- Breakfast bar, NFS
    ('53714510', 'Thanh ăn sáng chà là, phủ sữa chua'), -- Breakfast bar, date, with yogurt coating
    ('53714520', 'Thanh ăn sáng, vỏ ngũ cốc nhân trái cây, ít béo'), -- Breakfast bar, cereal crust with fruit filling, lowfat
    ('53720100', 'Thanh dinh dưỡng (Balance Original Bar)'), -- Nutrition bar (Balance Original Bar)
    ('53720200', 'Thanh dinh dưỡng (Clif Bar)'), -- Nutrition bar (Clif Bar)
    ('53720210', 'Thanh dinh dưỡng (Clif Kids Organic Zbar)'), -- Nutrition bar (Clif Kids Organic Zbar)
    ('53720300', 'Thanh dinh dưỡng (PowerBar)'), -- Nutrition bar (PowerBar)
    ('53720400', 'Thanh dinh dưỡng (Slim Fast Original Meal Bar)'), -- Nutrition bar (Slim Fast Original Meal Bar)
    ('53720500', 'Thanh dinh dưỡng (Snickers Marathon Protein Bar)'), -- Nutrition bar (Snickers Marathon Protein Bar)
    ('53720600', 'Thanh dinh dưỡng (South Beach Living Meal Bar)'), -- Nutrition bar (South Beach Living Meal Bar)
    ('53720610', 'Thanh dinh dưỡng (South Beach Living High Protein Bar)'), -- Nutrition bar (South Beach Living High Protein Bar)
    ('53720700', 'Thanh dinh dưỡng (Tiger''s Milk)'), -- Nutrition bar (Tiger's Milk)
    ('53720800', 'Thanh dinh dưỡng (Zone Perfect Classic Crunch)'), -- Nutrition bar (Zone Perfect Classic Crunch)
    ('53729000', 'Thanh dinh dưỡng hoặc thanh thay thế bữa ăn, loại chung'), -- Nutrition bar or meal replacement bar, NFS
    ('53800000', 'Đồ ăn vặt cho trẻ nhỏ, loại chung'), -- Baby Toddler snack, NFS
    ('53801000', 'Thanh ăn vặt cho trẻ nhỏ'), -- Baby Toddler bar
    ('53803100', 'Bánh quy cho trẻ nhỏ'), -- Baby Toddler cookie
    ('53803300', 'Bánh biscuit cho trẻ nhỏ'), -- Baby Toddler biscuit
    ('54001000', 'Bánh quy giòn (cracker), loại chung'), -- Crackers, NFS
    ('54102010', 'Bánh quy graham'), -- Graham crackers
    ('54102015', 'Bánh quy graham (Teddy Grahams)'), -- Graham crackers (Teddy Grahams)
    ('54102020', 'Bánh quy graham, phủ sô-cô-la'), -- Graham crackers, chocolate covered
    ('54102050', 'Bánh quy giòn yến mạch'), -- Crackers, oatmeal
    ('54102060', 'Bánh quy giòn kiểu Cuba'), -- Crackers, Cuban
    ('54102100', 'Bánh quy graham, giảm béo'), -- Graham crackers, reduced fat
    ('54102200', 'Bánh quy graham kẹp nhân'), -- Graham crackers, sandwich, with filling
    ('54103000', 'Bánh quy giòn ăn sáng (breakfast biscuit)'), -- Crackers, breakfast biscuit
    ('54200100', 'Bánh quy giòn bơ, giảm muối'), -- Crackers, butter, reduced sodium
    ('54201010', 'Bánh quy giòn matzo, giảm muối'), -- Crackers, matzo, reduced sodium
    ('54202020', 'Bánh quy giòn mặn saltine, giảm muối'), -- Crackers, saltine, reduced sodium
    ('54204020', 'Bánh quy giòn lúa mì, giảm muối'), -- Crackers, wheat, reduced sodium
    ('54204030', 'Bánh quy giòn lúa mì đan sợi, giảm muối'), -- Crackers, woven wheat, reduced sodium
    ('54301010', 'Bánh quy giòn bơ, không hương vị'), -- Crackers, butter, plain
    ('54301020', 'Bánh quy giòn bơ, có hương vị'), -- Crackers, butter, flavored
    ('54301030', 'Bánh quy giòn bơ (Ritz)'), -- Crackers, butter (Ritz)
    ('54301100', 'Bánh quy giòn bơ, giảm béo'), -- Crackers, butter, reduced fat
    ('54304000', 'Bánh quy giòn phô mai'), -- Crackers, cheese
    ('54304005', 'Bánh quy giòn phô mai (Cheez-It)'), -- Crackers, cheese (Cheez-It)
    ('54304020', 'Bánh quy giòn phô mai (Goldfish)'), -- Crackers, cheese (Goldfish)
    ('54304100', 'Bánh quy giòn phô mai, giảm béo'), -- Crackers, cheese, reduced fat
    ('54304110', 'Bánh quy giòn phô mai, giảm muối'), -- Crackers, cheese, reduced sodium
    ('54304150', 'Bánh quy giòn phô mai, ngũ cốc nguyên hạt'), -- Crackers, cheese, whole grain
    ('54305010', 'Bánh quy giòn dạng bánh mì giòn (crispbread)'), -- Crackers, crispbread
    ('54305020', 'Bánh quy giòn dạng bánh mì dẹt (flatbread)'), -- Crackers, flatbread
    ('54307000', 'Bánh quy giòn matzo'), -- Crackers, matzo
    ('54308000', 'Bánh quy giòn sữa'), -- Crackers, milk
    ('54313000', 'Bánh quy giòn oyster (loại nhỏ ăn kèm súp)'), -- Crackers, oyster
    ('54318000', 'Snack gạo lát giòn (rice chips)'), -- Chips, rice
    ('54318500', 'Bánh gạo phồng (rice cake)'), -- Rice cake
    ('54319000', 'Bánh quy giòn gạo'), -- Crackers, rice
    ('54319005', 'Bánh quy giòn gạo và các loại hạt'), -- Crackers, rice and nuts
    ('54319020', 'Bánh bỏng ngô ép (popcorn cake)'), -- Popcorn cake
    ('54319500', 'Bánh tráng (rice paper)'), -- Rice paper
    ('54325000', 'Bánh quy giòn mặn saltine'), -- Crackers, saltine
    ('54325010', 'Bánh quy giòn mặn saltine, giảm béo'), -- Crackers, saltine, reduced fat
    ('54325060', 'Bánh quy giòn mặn saltine, đa ngũ cốc'), -- Crackers, saltine, multigrain
    ('54326000', 'Bánh quy giòn đa ngũ cốc'), -- Crackers, multigrain
    ('54328000', 'Bánh quy giòn kẹp nhân'), -- Crackers, sandwich
    ('54328100', 'Bánh quy giòn kẹp nhân bơ lạc'), -- Crackers, sandwich, peanut butter filled
    ('54328105', 'Bánh quy giòn kẹp nhân bơ lạc (Ritz)'), -- Crackers, sandwich, peanut butter filled (Ritz)
    ('54328110', 'Bánh quy giòn kẹp nhân bơ lạc, giảm béo'), -- Crackers, sandwich, reduced fat, peanut butter filled
    ('54328120', 'Bánh quy giòn ngũ cốc nguyên hạt, kẹp nhân bơ lạc'), -- Crackers, whole grain, sandwich, peanut butter filled
    ('54328200', 'Bánh quy giòn kẹp nhân phô mai'), -- Crackers, sandwich, cheese filled
    ('54328210', 'Bánh quy giòn kẹp nhân phô mai (Ritz)'), -- Crackers, sandwich, cheese filled (Ritz)
    ('54336000', 'Bánh quy giòn nước (water cracker)'), -- Crackers, water
    ('54336100', 'Bánh quy giòn vỏ hoành thánh (wonton)'), -- Crackers, wonton
    ('54337010', 'Bánh quy giòn lúa mì đan sợi'), -- Crackers, woven wheat
    ('54337020', 'Bánh quy giòn lúa mì đan sợi, không hương vị (Triscuit)'), -- Crackers, woven wheat, plain (Triscuit)
    ('54337030', 'Bánh quy giòn lúa mì đan sợi, có hương vị (Triscuit)'), -- Crackers, woven wheat, flavored (Triscuit)
    ('54337060', 'Bánh quy giòn lúa mì đan sợi, giảm béo'), -- Crackers, woven wheat, reduced fat
    ('54338000', 'Bánh quy giòn lúa mì'), -- Crackers, wheat
    ('54338010', 'Bánh quy giòn lúa mì, không hương vị (Wheat Thins)'), -- Crackers, wheat, plain (Wheat Thins)
    ('54338020', 'Bánh quy giòn lúa mì, có hương vị (Wheat Thins)'), -- Crackers, wheat, flavored (Wheat Thins)
    ('54338100', 'Bánh quy giòn lúa mì, giảm béo'), -- Crackers, wheat, reduced fat
    ('54340100', 'Bánh quy giòn không gluten, không hương vị'), -- Crackers, gluten free, plain
    ('54340110', 'Bánh quy giòn không gluten, có hương vị'), -- Crackers, gluten free, flavored
    ('54350000', 'Bánh quy giòn cho trẻ nhỏ'), -- Baby Toddler crackers
    ('54350100', 'Bánh phồng cho trẻ nhỏ, vị trái cây'), -- Baby Toddler puffs, fruit
    ('54350200', 'Bánh phồng cho trẻ nhỏ, vị rau củ'), -- Baby Toddler puffs, vegetable
    ('54360000', 'Snack giòn cho trẻ nhỏ (crunchies)'), -- Baby Toddler crunchies
    ('54360100', 'Bánh hình bánh xe cho trẻ nhỏ (wheels)'), -- Baby Toddler wheels
    ('54401011', 'Hạt ngô rang giòn (corn nuts)'), -- Corn nuts
    ('54401021', 'Snack ngô, không hương vị'), -- Corn chips, plain
    ('54401026', 'Snack ngô, có hương vị'), -- Corn chips, flavored
    ('54401031', 'Snack ngô, không hương vị (Fritos)'), -- Corn chips, plain (Fritos)
    ('54401035', 'Snack ngô, có hương vị (Fritos)'), -- Corn chips, flavored (Fritos)
    ('54401055', 'Snack ngô vị phô mai'), -- Cheese flavored corn snacks
    ('54401065', 'Snack ngô vị phô mai, giảm béo'), -- Cheese flavored corn snacks, reduced fat
    ('54401075', 'Bánh tortilla chiên giòn, không hương vị'), -- Tortilla chips, plain
    ('54401081', 'Snack ngô vị phô mai (Cheetos)'), -- Cheese flavored corn snacks (Cheetos)
    ('54401085', 'Bánh tortilla chiên giòn, có hương vị'), -- Tortilla chips, flavored
    ('54401090', 'Snack ngô, giảm muối'), -- Corn chips, reduced sodium
    ('54401110', 'Bánh tortilla chiên giòn, vị phô mai nacho (Doritos)'), -- Tortilla chips, nacho cheese flavor (Doritos)
    ('54401111', 'Bánh tortilla chiên giòn, vị cool ranch (Doritos)'), -- Tortilla chips, cool ranch flavor (Doritos)
    ('54401112', 'Bánh tortilla chiên giòn, vị khác (Doritos)'), -- Tortilla chips, other flavors (Doritos)
    ('54401121', 'Bánh tortilla chiên giòn, giảm béo, không hương vị'), -- Tortilla chips, reduced fat, plain
    ('54401122', 'Bánh tortilla chiên giòn, giảm béo, có hương vị'), -- Tortilla chips, reduced fat, flavored
    ('54401170', 'Bánh tortilla chiên giòn, ít béo, không muối'), -- Tortilla chips, low fat, unsalted
    ('54402080', 'Bánh tortilla chiên giòn, giảm muối'), -- Tortilla chips, reduced sodium
    ('54402200', 'Snack trộn'), -- Snack mix
    ('54402610', 'Khoai tây chiên lát (snack) làm từ bột ép, đa ngũ cốc'), -- Potato chips, restructured, multigrain
    ('54402700', 'Bánh mì pita chiên giòn (pita chips)'), -- Pita chips
    ('54403001', 'Bỏng ngô, loại chung'), -- Popcorn, NFS
    ('54403005', 'Bỏng ngô rạp chiếu phim, có thêm bơ'), -- Popcorn, movie theater, with added butter
    ('54403006', 'Bỏng ngô rạp chiếu phim, không thêm bơ'), -- Popcorn, movie theater, no butter added
    ('54403010', 'Bỏng ngô nổ bằng khí nóng, không thêm bơ'), -- Popcorn, air-popped, no butter added
    ('54403040', 'Bỏng ngô nổ bằng khí nóng, có thêm bơ'), -- Popcorn, air-popped, with added butter
    ('54403045', 'Bỏng ngô nổ bằng dầu, không thêm bơ'), -- Popcorn, popped in oil, no butter added
    ('54403046', 'Bỏng ngô nổ bằng dầu, có thêm bơ'), -- Popcorn, popped in oil, with added butter
    ('54403051', 'Bỏng ngô lò vi sóng, loại chung'), -- Popcorn, microwave, NFS
    ('54403052', 'Bỏng ngô lò vi sóng, không hương vị'), -- Popcorn, microwave, plain
    ('54403053', 'Bỏng ngô lò vi sóng, không hương vị, loại nhẹ'), -- Popcorn, microwave, plain, light
    ('54403054', 'Bỏng ngô lò vi sóng, ít muối'), -- Popcorn, microwave, low sodium
    ('54403056', 'Bỏng ngô lò vi sóng, vị bơ'), -- Popcorn, microwave, butter flavored
    ('54403057', 'Bỏng ngô lò vi sóng, có hương vị, loại nhẹ'), -- Popcorn, microwave, flavored, light
    ('54403058', 'Bỏng ngô lò vi sóng, vị phô mai'), -- Popcorn, microwave, cheese flavored
    ('54403059', 'Bỏng ngô lò vi sóng, vị ngọt mặn kiểu kettle'), -- Popcorn, microwave, kettle
    ('54403080', 'Bỏng ngô ăn liền, loại chung'), -- Popcorn, ready-to-eat, NFS
    ('54403081', 'Bỏng ngô ăn liền, không hương vị'), -- Popcorn, ready-to-eat, plain
    ('54403082', 'Bỏng ngô ăn liền, không hương vị, loại nhẹ'), -- Popcorn, ready-to-eat, plain, light
    ('54403083', 'Bỏng ngô ăn liền, ít muối'), -- Popcorn, ready-to-eat, low sodium
    ('54403085', 'Bỏng ngô ăn liền, vị bơ'), -- Popcorn, ready-to-eat, butter flavored
    ('54403087', 'Bỏng ngô ăn liền, vị phô mai'), -- Popcorn, ready-to-eat, cheese flavored
    ('54403088', 'Bỏng ngô ăn liền, có hương vị, loại nhẹ'), -- Popcorn, ready-to-eat, flavored, light
    ('54403089', 'Bỏng ngô ăn liền, vị ngọt mặn kiểu kettle'), -- Popcorn, ready-to-eat, kettle
    ('54403110', 'Bỏng ngô phủ caramel'), -- Popcorn, caramel coated
    ('54403120', 'Bỏng ngô phủ caramel, có các loại hạt'), -- Popcorn, caramel coated, with nuts
    ('54403160', 'Bỏng ngô phủ sô-cô-la'), -- Popcorn, chocolate coated
    ('54404000', 'Snack bỏng ngô dạng lát (popcorn chips), không hương vị'), -- Popcorn chips, plain
    ('54404010', 'Snack bỏng ngô dạng lát (popcorn chips), vị khác'), -- Popcorn chips, other flavors
    ('54404020', 'Snack bỏng ngô dạng lát (popcorn chips), vị ngọt'), -- Popcorn chips, sweet flavors
    ('54406010', 'Snack vòng vị hành tây'), -- Onion flavored rings
    ('54406200', 'Bánh phồng tôm'), -- Shrimp chips
    ('54408000', 'Bánh quy xoắn (pretzel), loại chung'), -- Pretzels, NFS
    ('54408015', 'Bánh quy xoắn cứng, loại chung'), -- Pretzels, hard, NFS
    ('54408016', 'Bánh quy xoắn cứng, không hương vị, có muối'), -- Pretzels, hard, plain, salted
    ('54408017', 'Bánh quy xoắn cứng, không hương vị, rắc ít muối'), -- Pretzels, hard, plain, lightly salted
    ('54408030', 'Bánh quy xoắn cứng, không hương vị, không muối'), -- Pretzels, hard, plain, unsalted
    ('54408035', 'Bánh quy xoắn cứng, có hương vị'), -- Pretzels, hard, flavored
    ('54408070', 'Bánh quy xoắn cứng, đa ngũ cốc'), -- Pretzels, hard, multigrain
    ('54408081', 'Bánh quy xoắn cứng, không hương vị, không gluten'), -- Pretzels, hard, plain, gluten free
    ('54408082', 'Bánh quy xoắn cứng, có hương vị, không gluten'), -- Pretzels, hard, flavored, gluten free
    ('54408105', 'Bánh quy xoắn dạng lát, cứng, không hương vị'), -- Pretzel chips, hard, plain
    ('54408110', 'Bánh quy xoắn dạng lát, cứng, có hương vị'), -- Pretzel chips, hard, flavored
    ('54408115', 'Bánh quy xoắn dạng lát, cứng, không gluten'), -- Pretzel chips, hard, gluten free
    ('54408190', 'Bánh quy xoắn cứng, có lớp phủ, loại chung'), -- Pretzels, hard, coated, NFS
    ('54408200', 'Bánh quy xoắn cứng, phủ sô-cô-la'), -- Pretzels, hard, chocolate coated
    ('54408210', 'Bánh quy xoắn cứng, phủ sô-cô-la trắng'), -- Pretzels, hard, white chocolate coated
    ('54408250', 'Bánh quy xoắn cứng, phủ sữa chua'), -- Pretzels, hard, yogurt coated
    ('54408260', 'Bánh quy xoắn cứng, có lớp phủ, không gluten'), -- Pretzels, hard, coated, gluten free
    ('54408290', 'Bánh quy xoắn cứng, có nhân, loại chung'), -- Pretzels, hard, filled, NFS
    ('54408300', 'Bánh quy xoắn cứng, nhân phô mai'), -- Pretzels, hard, cheese filled
    ('54408310', 'Bánh quy xoắn cứng, nhân bơ lạc'), -- Pretzels, hard, peanut butter filled
    ('54408400', 'Bánh quy xoắn mềm, loại chung'), -- Pretzels, soft, NFS
    ('54408405', 'Bánh quy xoắn mềm, ăn liền, loại chung'), -- Pretzels, soft, ready-to-eat, NFS
    ('54408410', 'Bánh quy xoắn mềm, ăn liền, có muối, có phết bơ'), -- Pretzels, soft, ready-to-eat, salted, buttered
    ('54408411', 'Bánh quy xoắn mềm, ăn liền, không muối, có phết bơ'), -- Pretzels, soft, ready-to-eat, unsalted, buttered
    ('54408415', 'Bánh quy xoắn mềm, ăn liền, có muối, không bơ'), -- Pretzels, soft, ready-to-eat, salted, no butter
    ('54408416', 'Bánh quy xoắn mềm, ăn liền, không muối, không bơ'), -- Pretzels, soft, ready-to-eat, unsalted, no butter
    ('54408420', 'Bánh quy xoắn mềm, ăn liền, phủ đường quế'), -- Pretzels, soft, ready-to-eat, cinnamon sugar coated
    ('54408422', 'Bánh quy xoắn mềm, ăn liền, có lớp phủ hoặc có hương vị'), -- Pretzels, soft, ready-to-eat, coated or flavored
    ('54408430', 'Bánh quy xoắn mềm, ăn liền, phủ thịt bên trên'), -- Pretzels, soft, ready-to-eat, topped with meat
    ('54408432', 'Bánh quy xoắn mềm, ăn liền, phủ phô mai bên trên'), -- Pretzels, soft, ready-to-eat, topped with cheese
    ('54408450', 'Bánh quy xoắn mềm, từ loại đông lạnh, loại chung'), -- Pretzels, soft, from frozen, NFS
    ('54408455', 'Bánh quy xoắn mềm, từ loại đông lạnh, có muối'), -- Pretzels, soft, from frozen, salted
    ('54408456', 'Bánh quy xoắn mềm, từ loại đông lạnh, không muối'), -- Pretzels, soft, from frozen, unsalted
    ('54408460', 'Bánh quy xoắn mềm, từ loại đông lạnh, phủ đường quế'), -- Pretzels, soft, from frozen, cinnamon sugar coated
    ('54408462', 'Bánh quy xoắn mềm, từ loại đông lạnh, có lớp phủ hoặc có hương vị'), -- Pretzels, soft, from frozen, coated or flavored
    ('54408465', 'Bánh quy xoắn mềm, từ loại đông lạnh, phủ thịt bên trên'), -- Pretzels, soft, from frozen, topped with meat
    ('54408466', 'Bánh quy xoắn mềm, từ loại đông lạnh, phủ phô mai bên trên'), -- Pretzels, soft, from frozen, topped with cheese
    ('54408470', 'Bánh quy xoắn mềm, nhân phô mai'), -- Pretzels, soft, filled with cheese
    ('54408475', 'Bánh quy xoắn mềm, bữa trưa trường học'), -- Pretzels, soft, from school lunch
    ('54408480', 'Bánh quy xoắn mềm, đa ngũ cốc'), -- Pretzels, soft, multigrain
    ('54408485', 'Bánh quy xoắn mềm, không gluten'), -- Pretzels, soft, gluten free
    ('54408486', 'Bánh quy xoắn mềm, không gluten, phủ đường quế'), -- Pretzels, soft, gluten free, cinnamon sugar coated
    ('54408487', 'Bánh quy xoắn mềm, không gluten, có lớp phủ hoặc có hương vị'), -- Pretzels, soft, gluten free, coated or flavored
    ('54420210', 'Snack đa ngũ cốc (Sun Chips)'), -- Multigrain chips (Sun Chips)
    ('54420220', 'Snack trộn, không hương vị (Chex Mix)'), -- Snack mix, plain (Chex Mix)
    ('54440010', 'Bánh bagel cắt lát nướng giòn (bagel chips)'), -- Bagel chips
    ('54440020', 'Snack bánh quy giòn (cracker chips)'), -- Cracker chips
    ('55100005', 'Bánh kếp (pancake), loại chung'), -- Pancakes, NFS
    ('55100010', 'Bánh kếp (pancake), không hương vị, đông lạnh'), -- Pancakes, plain, frozen
    ('55100020', 'Bánh kếp (pancake) trái cây, đông lạnh'), -- Pancakes, fruit, frozen
    ('55100025', 'Bánh kếp (pancake) sô-cô-la, đông lạnh'), -- Pancakes, chocolate, frozen
    ('55100030', 'Bánh kếp (pancake) ngũ cốc nguyên hạt, đông lạnh'), -- Pancakes, whole grain, frozen
    ('55100050', 'Bánh kếp (pancake), không hương vị, đồ ăn nhanh/nhà hàng'), -- Pancakes, plain, fast food / restaurant
    ('55100055', 'Bánh kếp (pancake) trái cây, đồ ăn nhanh/nhà hàng'), -- Pancakes, fruit, fast food / restaurant
    ('55100060', 'Bánh kếp (pancake) sô-cô-la, đồ ăn nhanh/nhà hàng'), -- Pancakes, chocolate, fast food / restaurant
    ('55100065', 'Bánh kếp (pancake) ngũ cốc nguyên hạt, đồ ăn nhanh/nhà hàng'), -- Pancakes, whole grain, fast food / restaurant
    ('55100080', 'Bánh kếp (pancake), trường học'), -- Pancakes, school
    ('55101000', 'Bánh kếp (pancake), không hương vị'), -- Pancakes, plain
    ('55101015', 'Bánh kếp (pancake), không hương vị, giảm béo'), -- Pancakes, plain, reduced fat
    ('55103000', 'Bánh kếp (pancake) trái cây'), -- Pancakes, fruit
    ('55103020', 'Bánh kếp (pancake) bí đỏ'), -- Pancakes, pumpkin
    ('55103100', 'Bánh kếp (pancake) sô-cô-la'), -- Pancakes, chocolate
    ('55105200', 'Bánh kếp (pancake) ngũ cốc nguyên hạt'), -- Pancakes, whole grain
    ('55105205', 'Bánh kếp (pancake) ngũ cốc nguyên hạt, giảm béo'), -- Pancakes, whole grain, reduced fat
    ('55106000', 'Bánh kếp (pancake) không gluten'), -- Pancakes, gluten free
    ('55200010', 'Bánh waffle, loại chung'), -- Waffle, NFS
    ('55200020', 'Bánh waffle, không hương vị, đông lạnh'), -- Waffle, plain, frozen
    ('55200030', 'Bánh waffle, không hương vị, giảm béo'), -- Waffle, plain, reduced fat
    ('55200040', 'Bánh waffle trái cây, đông lạnh'), -- Waffle, fruit, frozen
    ('55200050', 'Bánh waffle sô-cô-la, đông lạnh'), -- Waffle, chocolate, frozen
    ('55200060', 'Bánh waffle ngũ cốc nguyên hạt, đông lạnh'), -- Waffle, whole grain, frozen
    ('55200070', 'Bánh waffle ngũ cốc nguyên hạt, giảm béo'), -- Waffle, whole grain, reduced fat
    ('55200080', 'Bánh waffle ngũ cốc nguyên hạt, trái cây, đông lạnh'), -- Waffle, whole grain, fruit, frozen
    ('55200100', 'Bánh waffle, không hương vị, đồ ăn nhanh/nhà hàng'), -- Waffle, plain, fast food / restaurant
    ('55200110', 'Bánh waffle sô-cô-la, đồ ăn nhanh/nhà hàng'), -- Waffle, chocolate, fast food / restaurant
    ('55200120', 'Bánh waffle trái cây, đồ ăn nhanh/nhà hàng'), -- Waffle, fruit, fast food / restaurant
    ('55200130', 'Bánh waffle ngũ cốc nguyên hạt, đồ ăn nhanh/nhà hàng'), -- Waffle, whole grain, fast food / restaurant
    ('55200200', 'Bánh waffle, trường học'), -- Waffle, school
    ('55201000', 'Bánh waffle, không hương vị'), -- Waffle, plain
    ('55203000', 'Bánh waffle trái cây'), -- Waffle, fruit
    ('55203600', 'Bánh waffle sô-cô-la'), -- Waffle, chocolate
    ('55203700', 'Bánh waffle quế'), -- Waffle, cinnamon
    ('55205000', 'Bánh waffle ngũ cốc nguyên hạt'), -- Waffle, whole grain
    ('55208000', 'Bánh waffle không gluten'), -- Waffle, gluten free
    ('55300010', 'Bánh mì nướng trứng (French toast), loại chung'), -- French toast, NFS
    ('55300020', 'Bánh mì nướng trứng (French toast), đông lạnh'), -- French toast, frozen
    ('55300050', 'Bánh mì nướng trứng (French toast), đồ ăn nhanh/nhà hàng'), -- French toast, fast food / restaurant
    ('55300060', 'Bánh mì nướng trứng (French toast), trường học'), -- French toast, school
    ('55301000', 'Bánh mì nướng trứng (French toast), không hương vị'), -- French toast, plain
    ('55301015', 'Bánh mì nướng trứng (French toast), ngũ cốc nguyên hạt'), -- French toast, whole grain
    ('55301025', 'Bánh mì nướng trứng (French toast), không gluten'), -- French toast, gluten free
    ('55301031', 'Bánh mì nướng trứng (French toast) dạng que'), -- French toast sticks
    ('55301040', 'Bánh mì nướng trứng (French toast) dạng que, đồ ăn nhanh/nhà hàng'), -- French toast sticks, fast food / restaurant
    ('55301048', 'Bánh mì nướng trứng (French toast) dạng que, trường học'), -- French toast sticks, school
    ('55400010', 'Bánh crepe, loại chung'), -- Crepe, NFS
    ('55401000', 'Bánh crepe, không hương vị'), -- Crepe, plain
    ('55501000', 'Bánh kếp kiểu Trung Quốc'), -- Chinese pancake
    ('55610300', 'Bánh bao/há cảo không thịt (dumpling)'), -- Dumpling, no meat
    ('55701000', 'Bánh làm từ gạo nếp'), -- Cake made with glutinous rice
    ('55702000', 'Bánh idli (Ấn Độ)'), -- Idli
    ('55702100', 'Bánh dosa (Ấn Độ), không nhân'), -- Dosa, plain
    ('55703000', 'Bánh làm từ gạo nếp và đậu khô'), -- Cake made with glutinous rice and dried beans
    ('55801000', 'Bánh funnel cake rắc đường'), -- Funnel cake with sugar
    ('55801010', 'Bánh funnel cake rắc đường và trái cây'), -- Funnel cake with sugar and fruit
    ('56104000', 'Mì Ý (pasta) rau củ, nấu chín'), -- Pasta, vegetable, cooked
    ('56112000', 'Mì sợi, nấu chín'), -- Noodles, cooked
    ('56113000', 'Mì sợi ngũ cốc nguyên hạt, nấu chín'), -- Noodles, whole grain, cooked
    ('56116000', 'Mì sợi chow mein (chiên giòn)'), -- Noodles, chow mein
    ('56116990', 'Miến đậu xanh, nấu chín'), -- Long rice noodles, made from mung beans, cooked
    ('56117090', 'Sợi mì gạo (bún/phở), nấu chín'), -- Rice noodles, cooked
    ('56130000', 'Mì Ý (pasta), nấu chín'), -- Pasta, cooked
    ('56132990', 'Mì Ý (pasta) ngũ cốc nguyên hạt, nấu chín'), -- Pasta, whole grain, cooked
    ('56140100', 'Mì Ý (pasta) không gluten'), -- Pasta, gluten free
    ('56200300', 'Ngũ cốc nấu chín, loại chung'), -- Cereal, cooked, NFS
    ('56200410', 'Lúa mạch'), -- Barley
    ('56200510', 'Hạt kiều mạch xay vỡ'), -- Buckwheat groats
    ('56200990', 'Cháo bột ngô (grits), loại chung'), -- Grits, NFS
    ('56201050', 'Cháo bột ngô (grits) loại thường hoặc nấu nhanh, nấu với nước, không rõ có thêm chất béo'), -- Grits, regular or quick, made with water, NS as to fat
    ('56201051', 'Cháo bột ngô (grits) loại thường hoặc nấu nhanh, nấu với nước, không thêm chất béo'), -- Grits, regular or quick, made with water, no added fat
    ('56201052', 'Cháo bột ngô (grits) loại thường hoặc nấu nhanh, nấu với nước, có thêm chất béo'), -- Grits, regular or quick, made with water, fat added
    ('56201090', 'Cháo bột ngô (grits) có phô mai, không rõ có thêm chất béo'), -- Grits, with cheese, NS as to fat
    ('56201091', 'Cháo bột ngô (grits) có phô mai, không thêm chất béo'), -- Grits, with cheese, no added fat
    ('56201092', 'Cháo bột ngô (grits) có phô mai, có thêm chất béo'), -- Grits, with cheese, fat added
    ('56201210', 'Cháo bột ngô (grits) ăn liền, nấu với nước, không thêm chất béo'), -- Grits, instant, made with water, no added fat
    ('56201220', 'Cháo bột ngô (grits) ăn liền, nấu với nước, có thêm chất béo'), -- Grits, instant, made with water, fat added
    ('56201230', 'Cháo bột ngô (grits) ăn liền, nấu với nước, không rõ có thêm chất béo'), -- Grits, instant, made with water, NS as to fat
    ('56201515', 'Cháo bột ngô đặc (cornmeal mush), không rõ có thêm chất béo'), -- Cornmeal mush, NS as to fat
    ('56201516', 'Cháo bột ngô đặc (cornmeal mush), không thêm chất béo'), -- Cornmeal mush, no added fat
    ('56201517', 'Cháo bột ngô đặc (cornmeal mush), có thêm chất béo'), -- Cornmeal mush, fat added
    ('56201600', 'Bột ngô masa harina, nấu chín'), -- Masa harina, cooked
    ('56202000', 'Hạt kê'), -- Millet
    ('56202900', 'Cháo yến mạch, đồ ăn nhanh, không hương vị'), -- Oatmeal, fast food, plain
    ('56202905', 'Cháo yến mạch, đồ ăn nhanh, có hương vị'), -- Oatmeal, fast food, flavored
    ('56202960', 'Cháo yến mạch, loại chung'), -- Oatmeal, NFS
    ('56203056', 'Cháo yến mạch loại thường hoặc nấu nhanh, nấu với nước, không thêm chất béo'), -- Oatmeal, regular or quick, made with water, no added fat
    ('56203057', 'Cháo yến mạch loại thường hoặc nấu nhanh, nấu với nước, có thêm chất béo'), -- Oatmeal, regular or quick, made with water, fat added
    ('56203066', 'Cháo yến mạch loại thường hoặc nấu nhanh, nấu với sữa, không thêm chất béo') -- Oatmeal, regular or quick, made with milk, no added fat
) AS t (source_food_code, name_vi)
WHERE f.source = 'USDA_FNDDS' AND f.source_food_code = t.source_food_code;

UPDATE nutrition_foods f SET name_vi = t.name_vi, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:usda-name-vi-v25'
FROM (VALUES
    ('56203067', 'Cháo yến mạch loại thường hoặc nấu nhanh, nấu với sữa, có thêm chất béo'), -- Oatmeal, regular or quick, made with milk, fat added
    ('56203076', 'Cháo yến mạch loại thường hoặc nấu nhanh, nấu với sữa thực vật, không thêm chất béo'), -- Oatmeal, regular or quick, made with non-dairy milk, no added fat
    ('56203077', 'Cháo yến mạch loại thường hoặc nấu nhanh, nấu với sữa thực vật, có thêm chất béo'), -- Oatmeal, regular or quick, made with non-dairy milk, fat added
    ('56203086', 'Cháo yến mạch ăn liền, không hương vị, nấu với nước, không thêm chất béo'), -- Oatmeal, instant, plain, made with water, no added fat
    ('56203087', 'Cháo yến mạch ăn liền, không hương vị, nấu với nước, có thêm chất béo'), -- Oatmeal, instant, plain, made with water, fat added
    ('56203096', 'Cháo yến mạch ăn liền, không hương vị, nấu với sữa, không thêm chất béo'), -- Oatmeal, instant, plain, made with milk, no added fat
    ('56203097', 'Cháo yến mạch ăn liền, không hương vị, nấu với sữa, có thêm chất béo'), -- Oatmeal, instant, plain, made with milk, fat added
    ('56203106', 'Cháo yến mạch ăn liền, không hương vị, nấu với sữa thực vật, không thêm chất béo'), -- Oatmeal, instant, plain, made with non-dairy milk, no added fat
    ('56203107', 'Cháo yến mạch ăn liền, không hương vị, nấu với sữa thực vật, có thêm chất béo'), -- Oatmeal, instant, plain, made with non-dairy milk, fat added
    ('56203130', 'Cháo yến mạch ăn liền, vị siro phong (maple), không thêm chất béo'), -- Oatmeal, instant, maple flavored, no added fat
    ('56203135', 'Cháo yến mạch ăn liền, vị siro phong (maple), có thêm chất béo'), -- Oatmeal, instant, maple flavored, fat added
    ('56203155', 'Cháo yến mạch ăn liền, vị trái cây, không thêm chất béo'), -- Oatmeal, instant, fruit flavored, no added fat
    ('56203160', 'Cháo yến mạch ăn liền, vị trái cây, có thêm chất béo'), -- Oatmeal, instant, fruit flavored, fat added
    ('56203555', 'Cháo yến mạch, giảm đường'), -- Oatmeal, reduced sugar
    ('56203610', 'Cháo yến mạch đa ngũ cốc'), -- Oatmeal, multigrain
    ('56204000', 'Hạt diêm mạch (quinoa), không rõ có thêm chất béo'), -- Quinoa, NS as to fat
    ('56204005', 'Hạt diêm mạch (quinoa), không thêm chất béo'), -- Quinoa, no added fat
    ('56204010', 'Hạt diêm mạch (quinoa), có thêm chất béo'), -- Quinoa, fat added
    ('56205000', 'Cơm, loại chung'), -- Rice, cooked, NFS
    ('56205001', 'Cơm trắng, không rõ có thêm chất béo'), -- Rice, white, cooked, NS as to fat
    ('56205002', 'Cơm trắng, nấu với dầu'), -- Rice, white, cooked, made with oil
    ('56205004', 'Cơm trắng, nấu với bơ'), -- Rice, white, cooked, made with butter
    ('56205006', 'Cơm trắng, nấu với bơ thực vật'), -- Rice, white, cooked, made with margarine
    ('56205007', 'Cơm trắng, có thêm chất béo, không rõ loại chất béo'), -- Rice, white, cooked, fat added, NS as to fat type
    ('56205008', 'Cơm trắng, không thêm chất béo'), -- Rice, white, cooked, no added fat
    ('56205011', 'Cơm gạo lứt, không rõ có thêm chất béo'), -- Rice, brown, cooked, NS as to fat
    ('56205012', 'Cơm gạo lứt, có thêm chất béo, nấu với dầu'), -- Rice, brown, cooked, fat added, made with oil
    ('56205014', 'Cơm gạo lứt, nấu với bơ'), -- Rice, brown, cooked, made with butter
    ('56205016', 'Cơm gạo lứt, nấu với bơ thực vật'), -- Rice, brown, cooked, made with margarine
    ('56205017', 'Cơm gạo lứt, có thêm chất béo, không rõ loại chất béo'), -- Rice, brown, cooked, fat added, NS as to fat type
    ('56205018', 'Cơm gạo lứt, không thêm chất béo'), -- Rice, brown, cooked, no added fat
    ('56205050', 'Cháo bột gạo (cream of rice)'), -- Cream of rice
    ('56205060', 'Cơm nấu với sữa'), -- Rice, cooked, with milk
    ('56205070', 'Cơm ngọt, nấu với mật ong'), -- Rice, sweet, cooked with honey
    ('56205101', 'Cháo gạo (congee)'), -- Congee
    ('56205130', 'Cơm vàng, không rõ có thêm chất béo'), -- Yellow rice, cooked, NS as to fat
    ('56205150', 'Cơm vàng, không thêm chất béo'), -- Yellow rice, cooked, no added fat
    ('56205170', 'Cơm vàng, có thêm chất béo'), -- Yellow rice, cooked, fat added
    ('56205190', 'Xôi trắng (cơm gạo nếp)'), -- Rice, white, cooked, glutinous
    ('56205205', 'Cơm gạo hoang (wild rice) 100%, không rõ có thêm chất béo'), -- Rice, wild, 100%, cooked, NS as to fat
    ('56205210', 'Cơm gạo hoang (wild rice) 100%, không thêm chất béo'), -- Rice, wild, 100%, cooked, no added fat
    ('56205215', 'Cơm gạo hoang (wild rice) 100%, có thêm chất béo'), -- Rice, wild, 100%, cooked, fat added
    ('56205300', 'Cơm gạo trắng và gạo hoang, không thêm chất béo'), -- Rice, white and wild, cooked, no added fat
    ('56205310', 'Cơm gạo lứt và gạo hoang, không thêm chất béo'), -- Rice, brown and wild, cooked, no added fat
    ('56205320', 'Cơm gạo trắng và gạo hoang, có thêm chất béo'), -- Rice, white and wild, cooked, fat added
    ('56205330', 'Cơm gạo trắng và gạo hoang, không rõ có thêm chất béo'), -- Rice, white and wild, cooked, NS as to fat
    ('56205340', 'Cơm gạo lứt và gạo hoang, có thêm chất béo'), -- Rice, brown and wild, cooked, fat added
    ('56205350', 'Cơm gạo lứt và gạo hoang, không rõ có thêm chất béo'), -- Rice, brown and wild, cooked, NS as to fat
    ('56205410', 'Cơm trắng nấu với chất béo, kiểu Puerto Rico'), -- Rice, white, cooked with fat, Puerto Rican style
    ('56206990', 'Cháo bột mì (cream of wheat), loại chung'), -- Cream of wheat, NFS
    ('56207016', 'Cháo bột mì (cream of wheat) loại thường hoặc nấu nhanh, nấu với nước, không thêm chất béo'), -- Cream of wheat, regular or quick, made with water, no added fat
    ('56207017', 'Cháo bột mì (cream of wheat) loại thường hoặc nấu nhanh, nấu với nước, có thêm chất béo'), -- Cream of wheat, regular or quick, made with water, fat added
    ('56207030', 'Cháo bột mì (cream of wheat) ăn liền, nấu với nước, không thêm chất béo'), -- Cream of wheat, instant, made with water, no added fat
    ('56207060', 'Cháo bột mì (cream of wheat) ăn liền, nấu với nước, có thêm chất béo'), -- Cream of wheat, instant, made with water, fat added
    ('56207110', 'Lúa mì bulgur, không thêm chất béo'), -- Bulgur, no added fat
    ('56207120', 'Lúa mì bulgur, có thêm chất béo'), -- Bulgur, fat added
    ('56207130', 'Lúa mì bulgur, không rõ có thêm chất béo'), -- Bulgur, NS as to fat
    ('56207160', 'Hạt couscous, không hương vị, nấu chín'), -- Couscous, plain, cooked
    ('56207200', 'Ngũ cốc lúa mì nguyên cám, nấu chín'), -- Whole wheat cereal, cooked
    ('56207370', 'Ngũ cốc lúa mì vị sô-cô-la, nấu chín'), -- Wheat cereal, chocolate flavored, cooked
    ('56208500', 'Ngũ cốc cám yến mạch, nấu chín'), -- Oat bran cereal, cooked
    ('57100100', 'Ngũ cốc ăn sáng, loại chung'), -- Cereal, ready-to-eat, NFS
    ('57103100', 'Ngũ cốc ăn sáng hình chữ O, có hương vị'), -- Cereal, O's, flavored
    ('57119000', 'Ngũ cốc ăn sáng giòn (crunch)'), -- Cereal, crunch
    ('57123000', 'Ngũ cốc ăn sáng hình chữ O, không hương vị'), -- Cereal, O's, plain
    ('57124200', 'Ngũ cốc ăn sáng dạng phồng vị sô-cô-la'), -- Cereal, chocolate puffs
    ('57125000', 'Ngũ cốc ăn sáng vị bánh mì nướng quế'), -- Cereal, cinnamon toast
    ('57126000', 'Ngũ cốc ăn sáng giòn xốp vị sô-cô-la'), -- Cereal, chocolate crispy
    ('57132000', 'Ngũ cốc ăn sáng ngô dạng ô vuông'), -- Cereal, corn squares
    ('57134000', 'Ngũ cốc ăn sáng ngô mảnh (corn flakes), không hương vị'), -- Cereal, corn flakes, plain
    ('57137000', 'Ngũ cốc ăn sáng ngô dạng phồng'), -- Cereal, corn puffs
    ('57151000', 'Ngũ cốc ăn sáng cốm gạo giòn, không hương vị'), -- Cereal, rice crispy, plain
    ('57207000', 'Ngũ cốc ăn sáng cám mảnh, không hương vị'), -- Cereal, bran flakes, plain
    ('57214000', 'Ngũ cốc ăn sáng lúa mì sợi ép (shredded wheat), có hương vị'), -- Cereal, shredded wheat, flavored
    ('57216000', 'Ngũ cốc ăn sáng cốm gạo giòn, có hương vị'), -- Cereal, rice crispy, flavored
    ('57221700', 'Ngũ cốc ăn sáng dạng vòng vị trái cây'), -- Cereal, fruit rings
    ('57223000', 'Ngũ cốc ăn sáng giòn xốp vị trái cây'), -- Cereal, fruit crispy
    ('57227000', 'Ngũ cốc ăn sáng granola'), -- Cereal, granola
    ('57237100', 'Ngũ cốc ăn sáng cụm yến mạch'), -- Cereal, oat bunches
    ('57240100', 'Ngũ cốc ăn sáng ngô dạng ô vuông, có hương vị'), -- Cereal, corn squares, flavored
    ('57241000', 'Ngũ cốc ăn sáng hình chữ O, vị mật ong và hạt'), -- Cereal, O's, honey nut
    ('57301600', 'Ngũ cốc ăn sáng đa ngũ cốc'), -- Cereal, multigrain
    ('57304100', 'Ngũ cốc ăn sáng yến mạch dạng ô vuông'), -- Cereal, oat squares
    ('57305150', 'Ngũ cốc ăn sáng yến mạch phủ đường, có marshmallow'), -- Cereal, frosted oats with marshmallows
    ('57308400', 'Ngũ cốc ăn sáng hình chữ O, đa ngũ cốc'), -- Cereal, O's, multigrain
    ('57329000', 'Ngũ cốc ăn sáng cám mảnh, có hương vị'), -- Cereal, bran flakes, flavored
    ('57336000', 'Ngũ cốc ăn sáng gạo dạng ô vuông'), -- Cereal, rice squares
    ('57344000', 'Ngũ cốc ăn sáng dạng chữ K, không hương vị'), -- Cereal, K's, plain
    ('57344010', 'Ngũ cốc ăn sáng dạng chữ K, có hương vị'), -- Cereal, K's, flavored
    ('57347000', 'Ngũ cốc ăn sáng dạng phồng, có hương vị'), -- Cereal, flavored puffs
    ('57348000', 'Ngũ cốc ăn sáng ngô mảnh (corn flakes), có hương vị'), -- Cereal, corn flakes, flavored
    ('57401100', 'Ngũ cốc ăn sáng hình chữ O, loại chung'), -- Cereal, O's, NFS
    ('57411000', 'Ngũ cốc ăn sáng lúa mì dạng ô vuông'), -- Cereal, wheat squares
    ('57412000', 'Mầm lúa mì'), -- Wheat germ
    ('57416000', 'Ngũ cốc ăn sáng dạng phồng, không hương vị'), -- Cereal, plain puffs
    ('57417000', 'Ngũ cốc ăn sáng lúa mì sợi ép (shredded wheat), không hương vị'), -- Cereal, shredded wheat, plain
    ('57418000', 'Ngũ cốc ăn sáng lúa mì mảnh'), -- Cereal, wheat flakes
    ('57420100', 'Ngũ cốc ăn sáng khác, loại chung'), -- Cereal, other, NFS
    ('57420110', 'Ngũ cốc ăn sáng khác, không hương vị'), -- Cereal, other, plain
    ('57420120', 'Ngũ cốc ăn sáng khác, vị trái cây'), -- Cereal, other, fruit flavored
    ('57420130', 'Ngũ cốc ăn sáng khác, vị sô-cô-la'), -- Cereal, other, chocolate
    ('57420140', 'Ngũ cốc ăn sáng khác, vị bơ lạc'), -- Cereal, other, peanut butter
    ('57420150', 'Ngũ cốc ăn sáng khác, vị mật ong'), -- Cereal, other, honey
    ('57420160', 'Ngũ cốc ăn sáng, giảm đường'), -- Cereal, reduced sugar
    ('57601100', 'Cám lúa mì'), -- Wheat bran
    ('57602100', 'Yến mạch, sống'), -- Oats, raw
    ('57602500', 'Ngũ cốc ăn sáng cám yến mạch, ăn liền'), -- Cereal, oat bran, ready-to-eat
    ('57801000', 'Bột ngũ cốc cho trẻ nhỏ, lúa mạch, dạng khô'), -- Baby Toddler cereal, barley, dry
    ('57804000', 'Bột ngũ cốc cho trẻ nhỏ, yến mạch, dạng khô'), -- Baby Toddler cereal, oatmeal, dry
    ('57805000', 'Bột ngũ cốc cho trẻ nhỏ, gạo, dạng khô'), -- Baby Toddler cereal, rice, dry
    ('57805090', 'Bột ngũ cốc cho trẻ nhỏ, gạo và trái cây, dạng khô'), -- Baby Toddler cereal, rice with fruit, dry
    ('57806000', 'Bột ngũ cốc cho trẻ nhỏ, đa ngũ cốc và trái cây, dạng khô'), -- Baby Toddler cereal, multigrain with fruit, dry
    ('57806050', 'Bột ngũ cốc cho trẻ nhỏ, đa ngũ cốc, dạng khô'), -- Baby Toddler cereal, multigrain, dry
    ('57806100', 'Bột ngũ cốc cho trẻ nhỏ, yến mạch và trái cây, dạng khô'), -- Baby Toddler cereal, oatmeal with fruit, dry
    ('57820000', 'Bột ngũ cốc cho trẻ nhỏ, loại chung'), -- Baby Toddler cereal, NFS
    ('57820100', 'Bột ngũ cốc cho trẻ nhỏ, gạo, ăn liền'), -- Baby Toddler cereal, rice, ready-to-eat
    ('57820200', 'Bột ngũ cốc cho trẻ nhỏ, yến mạch, ăn liền'), -- Baby Toddler cereal, oatmeal, ready-to-eat
    ('57820300', 'Bột ngũ cốc cho trẻ nhỏ, đa ngũ cốc, ăn liền'), -- Baby Toddler cereal, multigrain, ready-to-eat
    ('57822000', 'Bột ngũ cốc cho trẻ nhỏ, đa ngũ cốc và trái cây, ăn liền'), -- Baby Toddler cereal, multigrain with fruit, ready-to-eat
    ('57823000', 'Bột ngũ cốc cho trẻ nhỏ, yến mạch và trái cây, ăn liền'), -- Baby Toddler cereal, oatmeal with fruit, ready-to-eat
    ('57824000', 'Bột ngũ cốc cho trẻ nhỏ, gạo và trái cây, ăn liền'), -- Baby Toddler cereal, rice with fruit, ready-to-eat
    ('58100360', 'Món chilaquiles (Mexico)'), -- Chilaquiles
    ('58101830', 'Món Frito pie (snack ngô phủ chili)'), -- Frito pie
    ('58101835', 'Pizza kiểu Mexico'), -- Mexican pizza
    ('58101930', 'Salad taco hoặc tostada có thịt'), -- Taco or tostada salad with meat
    ('58101935', 'Salad taco hoặc tostada có gà'), -- Taco or tostada salad with chicken
    ('58101940', 'Salad taco hoặc tostada, không thịt'), -- Taco or tostada salad, meatless
    ('58101945', 'Salad taco hoặc tostada có thịt và kem chua'), -- Taco or tostada salad with meat and sour cream
    ('58101950', 'Salad taco hoặc tostada có gà và kem chua'), -- Taco or tostada salad with chicken and sour cream
    ('58101955', 'Salad taco hoặc tostada, không thịt, có kem chua'), -- Taco or tostada salad, meatless with sour cream
    ('58102000', 'Bánh taco, loại chung'), -- Taco, NFS
    ('58102010', 'Bánh taco vỏ tortilla ngô, thịt bò, phô mai'), -- Taco, corn tortilla, beef, cheese
    ('58102020', 'Bánh taco vỏ tortilla ngô, thịt bò, có đậu, phô mai'), -- Taco, corn tortilla, beef, with beans, cheese
    ('58102030', 'Bánh taco vỏ tortilla ngô, thịt lợn, phô mai'), -- Taco, corn tortilla, pork, cheese
    ('58102040', 'Bánh taco vỏ tortilla ngô, thịt lợn, có đậu, phô mai'), -- Taco, corn tortilla, pork, with beans, cheese
    ('58102050', 'Bánh taco vỏ tortilla ngô, gà, phô mai'), -- Taco, corn tortilla, chicken, cheese
    ('58102060', 'Bánh taco vỏ tortilla ngô, gà, có đậu, phô mai'), -- Taco, corn tortilla, chicken, with beans, cheese
    ('58102070', 'Bánh taco vỏ tortilla ngô, có đậu, phô mai'), -- Taco, corn tortilla, with beans, cheese
    ('58102110', 'Bánh taco vỏ tortilla bột mì, thịt bò, phô mai'), -- Taco, flour tortilla, beef, cheese
    ('58102120', 'Bánh taco vỏ tortilla bột mì, thịt bò, có đậu, phô mai'), -- Taco, flour tortilla, beef, with beans, cheese
    ('58102130', 'Bánh taco vỏ tortilla bột mì, thịt lợn, phô mai'), -- Taco, flour tortilla, pork, cheese
    ('58102140', 'Bánh taco vỏ tortilla bột mì, thịt lợn, có đậu, phô mai'), -- Taco, flour tortilla, pork, with beans, cheese
    ('58102150', 'Bánh taco vỏ tortilla bột mì, gà, phô mai'), -- Taco, flour tortilla, chicken, cheese
    ('58102160', 'Bánh taco vỏ tortilla bột mì, gà, có đậu, phô mai'), -- Taco, flour tortilla, chicken, with beans, cheese
    ('58102170', 'Bánh taco vỏ tortilla bột mì, có đậu, phô mai'), -- Taco, flour tortilla, with beans, cheese
    ('58102210', 'Bánh taco cá'), -- Taco, fish
    ('58102220', 'Bánh taco thịt, không phô mai'), -- Taco, meat, no cheese
    ('58102230', 'Bánh taco thịt, có đậu, không phô mai'), -- Taco, meat, with beans, no cheese
    ('58102240', 'Bánh taco có đậu, không phô mai'), -- Taco, with beans, no cheese
    ('58102250', 'Bánh taco chỉ có phô mai'), -- Taco, cheese only
    ('58102300', 'Bánh burrito, loại chung'), -- Burrito, NFS
    ('58102310', 'Bánh burrito thịt bò, phô mai'), -- Burrito, beef, cheese
    ('58102320', 'Bánh burrito thịt bò, có cơm, phô mai'), -- Burrito, beef, with rice, cheese
    ('58102330', 'Bánh burrito thịt bò, có đậu, phô mai'), -- Burrito, beef, with beans, cheese
    ('58102340', 'Bánh burrito thịt bò, có đậu và cơm, phô mai'), -- Burrito, beef, with beans and rice, cheese
    ('58102410', 'Bánh burrito thịt lợn, phô mai'), -- Burrito, pork, cheese
    ('58102420', 'Bánh burrito thịt lợn, có cơm, phô mai'), -- Burrito, pork, with rice, cheese
    ('58102430', 'Bánh burrito thịt lợn, có đậu, phô mai'), -- Burrito, pork, with beans, cheese
    ('58102440', 'Bánh burrito thịt lợn, có đậu và cơm, phô mai'), -- Burrito, pork, with beans and rice, cheese
    ('58102510', 'Bánh burrito gà, phô mai'), -- Burrito, chicken, cheese
    ('58102520', 'Bánh burrito gà, có cơm, phô mai'), -- Burrito, chicken, with rice, cheese
    ('58102530', 'Bánh burrito gà, có đậu, phô mai'), -- Burrito, chicken, with beans, cheese
    ('58102540', 'Bánh burrito gà, có đậu và cơm, phô mai'), -- Burrito, chicken, with beans and rice, cheese
    ('58102605', 'Bánh burrito có đậu, không phô mai'), -- Burrito, with beans, no cheese
    ('58102610', 'Bánh burrito có đậu, phô mai'), -- Burrito, with beans, cheese
    ('58102620', 'Bánh burrito có đậu và cơm, phô mai'), -- Burrito, with beans and rice, cheese
    ('58102630', 'Bánh burrito thịt, không phô mai'), -- Burrito, meat, no cheese
    ('58102640', 'Bánh burrito thịt, có cơm, không phô mai'), -- Burrito, meat, with rice, no cheese
    ('58102650', 'Bánh burrito thịt, có đậu, không phô mai'), -- Burrito, meat, with beans, no cheese
    ('58102660', 'Bánh burrito thịt, có đậu và cơm, không phô mai'), -- Burrito, meat, with beans and rice, no cheese
    ('58102680', 'Bánh burrito chỉ có phô mai'), -- Burrito, cheese only
    ('58102700', 'Bát burrito (burrito bowl), loại chung'), -- Burrito bowl, NFS
    ('58102710', 'Bát burrito (burrito bowl), thịt bò hoặc thịt lợn'), -- Burrito bowl, beef or pork
    ('58102720', 'Bát burrito (burrito bowl), thịt bò hoặc thịt lợn, có cơm'), -- Burrito bowl, beef or pork, with rice
    ('58102730', 'Bát burrito (burrito bowl), thịt bò hoặc thịt lợn, có đậu'), -- Burrito bowl, beef or pork, with beans
    ('58102740', 'Bát burrito (burrito bowl), thịt bò hoặc thịt lợn, có đậu và cơm'), -- Burrito bowl, beef or pork, with beans and rice
    ('58102750', 'Bát burrito (burrito bowl), gà'), -- Burrito bowl, chicken
    ('58102760', 'Bát burrito (burrito bowl), gà, có cơm'), -- Burrito bowl, chicken, with rice
    ('58102770', 'Bát burrito (burrito bowl), gà, có đậu'), -- Burrito bowl, chicken, with beans
    ('58102780', 'Bát burrito (burrito bowl), gà, có đậu và cơm'), -- Burrito bowl, chicken, with beans and rice
    ('58102790', 'Bát burrito (burrito bowl), có đậu'), -- Burrito bowl, with beans
    ('58102800', 'Bánh enchilada, loại chung'), -- Enchilada, NFS
    ('58102810', 'Bánh enchilada thịt bò'), -- Enchilada, beef
    ('58102820', 'Bánh enchilada thịt lợn'), -- Enchilada, pork
    ('58102830', 'Bánh enchilada gà'), -- Enchilada, chicken
    ('58102840', 'Bánh enchilada không thịt'), -- Enchilada, no meat
    ('58103100', 'Bánh tamale, loại chung'), -- Tamale, NFS
    ('58103120', 'Bánh tamale thịt bò'), -- Tamale, beef
    ('58103125', 'Bánh tamale thịt lợn'), -- Tamale, pork
    ('58103130', 'Bánh tamale gà'), -- Tamale, chicken
    ('58103250', 'Bánh tamale không thịt'), -- Tamale, no meat
    ('58103310', 'Món đút lò (casserole) bánh tamale có thịt'), -- Tamale casserole with meat
    ('58104105', 'Món nachos, loại chung'), -- Nachos, NFS
    ('58104120', 'Món nachos chỉ có phô mai'), -- Nachos, cheese only
    ('58104130', 'Món nachos thịt bò hoặc thịt lợn'), -- Nachos, beef or pork
    ('58104135', 'Món nachos thịt bò hoặc thịt lợn, có đậu'), -- Nachos, beef or pork, with beans
    ('58104150', 'Món nachos gà'), -- Nachos, chicken
    ('58104165', 'Món nachos gà, có đậu'), -- Nachos, chicken, with beans
    ('58104170', 'Món nachos có đậu'), -- Nachos, with beans
    ('58104255', 'Bánh gordita chỉ có phô mai'), -- Gordita, cheese only
    ('58104260', 'Bánh gordita nhân đậu'), -- Gordita, with beans
    ('58104290', 'Bánh gordita nhân thịt'), -- Gordita, meat
    ('58104295', 'Bánh gordita nhân thịt và đậu'), -- Gordita, meat, with beans
    ('58104500', 'Bánh chimichanga nhân thịt'), -- Chimichanga, meat
    ('58104520', 'Bánh chimichanga nhân đậu'), -- Chimichanga, with beans
    ('58104530', 'Bánh chimichanga nhân gà'), -- Chimichanga, chicken
    ('58104700', 'Bánh quesadilla, loại chung'), -- Quesadilla, NFS
    ('58104710', 'Bánh quesadilla, chỉ có phô mai'), -- Quesadilla, cheese only
    ('58104730', 'Bánh quesadilla thịt bò hoặc thịt lợn'), -- Quesadilla, beef or pork
    ('58104740', 'Bánh quesadilla gà'), -- Quesadilla, chicken
    ('58104750', 'Bánh quesadilla có rau'), -- Quesadilla, with vegetables
    ('58104760', 'Bánh quesadilla thịt bò hoặc thịt lợn, có rau'), -- Quesadilla, beef or pork, with vegetables
    ('58104770', 'Bánh quesadilla gà, có rau'), -- Quesadilla, chicken, with vegetables
    ('58104780', 'Bánh quesadilla trứng'), -- Quesadilla, egg
    ('58104790', 'Bánh quesadilla trứng, có thịt'), -- Quesadilla, egg, with meat
    ('58104800', 'Bánh taquito, chỉ có phô mai'), -- Taquito, cheese only
    ('58104825', 'Bánh taquito thịt bò hoặc thịt lợn'), -- Taquito, beef or pork
    ('58104835', 'Bánh taquito gà'), -- Taquito, chicken
    ('58104900', 'Bánh taquito trứng'), -- Taquito, egg
    ('58104990', 'Món fajita, loại chung'), -- Fajita, NFS
    ('58105000', 'Món fajita gà'), -- Fajita, chicken
    ('58105050', 'Món fajita thịt bò hoặc thịt lợn'), -- Fajita, beef or pork
    ('58105060', 'Món fajita tôm'), -- Fajita, shrimp
    ('58105075', 'Món fajita rau'), -- Fajita, vegetable
    ('58105100', 'Bánh pupusa, chỉ có phô mai'), -- Pupusa, cheese only
    ('58105105', 'Bánh pupusa nhân đậu'), -- Pupusa, with beans
    ('58105110', 'Bánh pupusa nhân thịt'), -- Pupusa, meat
    ('58105120', 'Bánh pupusa nhân thịt và đậu'), -- Pupusa, meat, with beans
    ('58106200', 'Pizza phô mai, từ loại đông lạnh, đế mỏng'), -- Pizza, cheese, from frozen, thin crust
    ('58106205', 'Pizza phô mai, từ loại đông lạnh, đế dày'), -- Pizza, cheese, from frozen, thick crust
    ('58106210', 'Pizza phô mai, nhà hàng hoặc đồ ăn nhanh, không rõ loại đế'), -- Pizza, cheese, from restaurant or fast food, NS as to type of crust
    ('58106220', 'Pizza phô mai, nhà hàng hoặc đồ ăn nhanh, đế mỏng'), -- Pizza, cheese, from restaurant or fast food, thin crust
    ('58106225', 'Pizza phô mai, nhà hàng hoặc đồ ăn nhanh, đế vừa'), -- Pizza, cheese, from restaurant or fast food, medium crust
    ('58106230', 'Pizza phô mai, nhà hàng hoặc đồ ăn nhanh, đế dày'), -- Pizza, cheese, from restaurant or fast food, thick crust
    ('58106233', 'Pizza phô mai, viền nhồi phô mai'), -- Pizza, cheese, stuffed crust
    ('58106234', 'Pizza phô mai, bữa trưa trường học, đế vừa'), -- Pizza, cheese, from school lunch, medium crust
    ('58106235', 'Pizza phô mai, bữa trưa trường học, đế mỏng'), -- Pizza, cheese, from school lunch, thin crust
    ('58106236', 'Pizza phô mai, bữa trưa trường học, đế dày'), -- Pizza, cheese, from school lunch, thick crust
    ('58106250', 'Pizza thêm nhiều phô mai, đế mỏng'), -- Pizza, extra cheese, thin crust
    ('58106260', 'Pizza thêm nhiều phô mai, đế dày'), -- Pizza, extra cheese, thick crust
    ('58106300', 'Pizza phô mai, có rau, từ loại đông lạnh, đế mỏng'), -- Pizza, cheese, with vegetables, from frozen, thin crust
    ('58106305', 'Pizza phô mai, có rau, từ loại đông lạnh, đế dày'), -- Pizza, cheese with vegetables, from frozen, thick crust
    ('58106320', 'Pizza phô mai, có rau, nhà hàng hoặc đồ ăn nhanh, đế mỏng'), -- Pizza, cheese, with vegetables, from restaurant or fast food, thin crust
    ('58106325', 'Pizza phô mai, có rau, nhà hàng hoặc đồ ăn nhanh, đế vừa'), -- Pizza, cheese, with vegetables, from restaurant or fast food, medium crust
    ('58106330', 'Pizza phô mai, có rau, nhà hàng hoặc đồ ăn nhanh, đế dày'), -- Pizza, cheese, with vegetables, from restaurant or fast food, thick crust
    ('58106345', 'Pizza phô mai, thêm nhiều rau, đế mỏng'), -- Pizza with cheese and extra vegetables, thin crust
    ('58106347', 'Pizza phô mai, thêm nhiều rau, đế vừa'), -- Pizza with cheese and extra vegetables, medium crust
    ('58106350', 'Pizza phô mai, thêm nhiều rau, đế dày'), -- Pizza with cheese and extra vegetables, thick crust
    ('58106358', 'Pizza phô mai, có hoa quả, đế mỏng'), -- Pizza, cheese, with fruit, thin crust
    ('58106359', 'Pizza phô mai, có hoa quả, đế vừa'), -- Pizza, cheese, with fruit, medium crust
    ('58106360', 'Pizza phô mai, có hoa quả, đế dày'), -- Pizza, cheese, with fruit, thick crust
    ('58106512', 'Pizza xúc xích pepperoni, từ loại đông lạnh, đế mỏng'), -- Pizza with pepperoni, from frozen, thin crust
    ('58106514', 'Pizza xúc xích pepperoni, từ loại đông lạnh, đế vừa'), -- Pizza with pepperoni, from frozen, medium crust
    ('58106516', 'Pizza xúc xích pepperoni, từ loại đông lạnh, đế dày'), -- Pizza with pepperoni, from frozen, thick crust
    ('58106540', 'Pizza xúc xích pepperoni, nhà hàng hoặc đồ ăn nhanh, không rõ loại đế'), -- Pizza with pepperoni, from restaurant or fast food, NS as to type of crust
    ('58106550', 'Pizza xúc xích pepperoni, nhà hàng hoặc đồ ăn nhanh, đế mỏng'), -- Pizza with pepperoni, from restaurant or fast food, thin crust
    ('58106555', 'Pizza xúc xích pepperoni, nhà hàng hoặc đồ ăn nhanh, đế vừa'), -- Pizza with pepperoni, from restaurant or fast food,  medium crust
    ('58106560', 'Pizza xúc xích pepperoni, nhà hàng hoặc đồ ăn nhanh, đế dày'), -- Pizza with pepperoni, from restaurant or fast food, thick crust
    ('58106565', 'Pizza xúc xích pepperoni, viền nhồi phô mai'), -- Pizza with pepperoni, stuffed crust
    ('58106570', 'Pizza xúc xích pepperoni, bữa trưa trường học, đế mỏng'), -- Pizza with pepperoni, from school lunch, thin crust
    ('58106578', 'Pizza xúc xích pepperoni, bữa trưa trường học, đế vừa'), -- Pizza, with pepperoni, from school lunch, medium crust
    ('58106580', 'Pizza xúc xích pepperoni, bữa trưa trường học, đế dày'), -- Pizza with pepperoni, from school lunch, thick crust
    ('58106602', 'Pizza thịt (không phải pepperoni), từ loại đông lạnh, đế mỏng'), -- Pizza with meat other than pepperoni, from frozen, thin crust
    ('58106604', 'Pizza thịt (không phải pepperoni), từ loại đông lạnh, đế vừa'), -- Pizza with meat other than pepperoni, from frozen, medium crust
    ('58106606', 'Pizza thịt (không phải pepperoni), từ loại đông lạnh, đế dày'), -- Pizza with meat other than pepperoni, from frozen, thick crust
    ('58106610', 'Pizza thịt (không phải pepperoni), nhà hàng hoặc đồ ăn nhanh, không rõ loại đế'), -- Pizza with meat other than pepperoni, from restaurant or fast food, NS as to type of crust
    ('58106620', 'Pizza thịt (không phải pepperoni), nhà hàng hoặc đồ ăn nhanh, đế mỏng'), -- Pizza with meat other than pepperoni, from restaurant or fast food, thin crust
    ('58106625', 'Pizza thịt (không phải pepperoni), nhà hàng hoặc đồ ăn nhanh, đế vừa'), -- Pizza with meat other than pepperoni, from restaurant or fast food, medium crust
    ('58106630', 'Pizza thịt (không phải pepperoni), nhà hàng hoặc đồ ăn nhanh, đế dày'), -- Pizza with meat other than pepperoni, from restaurant or fast food, thick crust
    ('58106633', 'Pizza thịt (không phải pepperoni), viền nhồi phô mai'), -- Pizza, with meat other than pepperoni, stuffed crust
    ('58106634', 'Pizza thịt (không phải pepperoni), bữa trưa trường học, đế vừa'), -- Pizza, with meat other than pepperoni, from school lunch, medium crust
    ('58106635', 'Pizza thịt (không phải pepperoni), bữa trưa trường học, đế mỏng'), -- Pizza, with meat other than pepperoni, from school lunch, thin crust
    ('58106636', 'Pizza thịt (không phải pepperoni), bữa trưa trường học, đế dày'), -- Pizza, with meat other than pepperoni, from school lunch, thick crust
    ('58106650', 'Pizza thêm nhiều thịt, đế mỏng'), -- Pizza with extra meat, thin crust
    ('58106655', 'Pizza thêm nhiều thịt, đế vừa'), -- Pizza with extra meat, medium crust
    ('58106660', 'Pizza thêm nhiều thịt, đế dày'), -- Pizza with extra meat, thick crust
    ('58106700', 'Pizza thịt và rau, từ loại đông lạnh, đế mỏng'), -- Pizza with meat and vegetables, from frozen, thin crust
    ('58106702', 'Pizza thịt và rau, từ loại đông lạnh, đế vừa'), -- Pizza with meat and vegetables, from frozen, medium crust
    ('58106705', 'Pizza thịt và rau, từ loại đông lạnh, đế dày'), -- Pizza with meat and vegetables, from frozen, thick crust
    ('58106720', 'Pizza thịt và rau, nhà hàng hoặc đồ ăn nhanh, đế mỏng'), -- Pizza with meat and vegetables, from restaurant or fast food, thin crust
    ('58106725', 'Pizza thịt và rau, nhà hàng hoặc đồ ăn nhanh, đế vừa'), -- Pizza with meat and vegetables, from restaurant or fast food, medium crust
    ('58106730', 'Pizza thịt và rau, nhà hàng hoặc đồ ăn nhanh, đế dày'), -- Pizza with meat and vegetables, from restaurant or fast food, thick crust
    ('58106736', 'Pizza thêm nhiều thịt và nhiều rau, đế mỏng'), -- Pizza with extra meat and extra vegetables, thin crust
    ('58106737', 'Pizza thêm nhiều thịt và nhiều rau, đế dày'), -- Pizza with extra meat and extra vegetables, thick crust
    ('58106738', 'Pizza thêm nhiều thịt và nhiều rau, đế vừa'), -- Pizza with extra meat and extra vegetables, medium crust
    ('58106750', 'Pizza thịt và hoa quả, đế mỏng'), -- Pizza with meat and fruit, thin crust
    ('58106755', 'Pizza thịt và hoa quả, đế vừa'), -- Pizza with meat and fruit, medium crust
    ('58106760', 'Pizza thịt và hoa quả, đế dày'), -- Pizza with meat and fruit, thick crust
    ('58106820', 'Pizza đậu và rau, đế mỏng'), -- Pizza with beans and vegetables, thin crust
    ('58106830', 'Pizza đậu và rau, đế dày'), -- Pizza with beans and vegetables, thick crust
    ('58107050', 'Pizza không phô mai, đế mỏng'), -- Pizza, no cheese, thin crust
    ('58107100', 'Pizza không phô mai, đế dày'), -- Pizza, no cheese, thick crust
    ('58107205', 'Pizza trắng (không sốt cà chua), phô mai, đế mỏng'), -- White pizza, cheese, thin crust
    ('58107207', 'Pizza trắng (không sốt cà chua), phô mai, đế dày'), -- White pizza, cheese, thick crust
    ('58107212', 'Pizza trắng (không sốt cà chua), phô mai, có rau, đế mỏng'), -- White pizza, cheese, with vegetables, thin crust
    ('58107214', 'Pizza trắng (không sốt cà chua), phô mai, có rau, đế dày'), -- White pizza, cheese, with vegetables, thick crust
    ('58107222', 'Pizza trắng (không sốt cà chua), phô mai, có thịt, đế mỏng'), -- White pizza, cheese, with meat, thin crust
    ('58107224', 'Pizza trắng (không sốt cà chua), phô mai, có thịt, đế dày'), -- White pizza, cheese, with meat, thick crust
    ('58107232', 'Pizza trắng (không sốt cà chua), phô mai, có thịt và rau, đế mỏng'), -- White pizza, cheese, with meat and vegetables, thin crust
    ('58107234', 'Pizza trắng (không sốt cà chua), phô mai, có thịt và rau, đế dày'), -- White pizza, cheese, with meat and vegetables, thick crust
    ('58108000', 'Bánh calzone nhân phô mai, không thịt'), -- Calzone, with cheese, meatless
    ('58108010', 'Bánh calzone nhân thịt và phô mai'), -- Calzone, with meat and cheese
    ('58108050', 'Bánh pizza cuộn (pizza rolls)'), -- Pizza rolls
    ('58109015', 'Pizza phô mai, đế mỏng lúa mì nguyên cám'), -- Pizza, cheese, whole wheat thin crust
    ('58109020', 'Pizza phô mai, đế dày lúa mì nguyên cám'), -- Pizza, cheese, whole wheat thick crust
    ('58109030', 'Pizza thịt, đế mỏng lúa mì nguyên cám'), -- Pizza, with meat, whole wheat thin crust
    ('58109040', 'Pizza thịt, đế dày lúa mì nguyên cám'), -- Pizza, with meat, whole wheat thick crust
    ('58109050', 'Pizza phô mai và rau, đế mỏng lúa mì nguyên cám'), -- Pizza, cheese and vegetables, whole wheat thin crust
    ('58109060', 'Pizza phô mai và rau, đế dày lúa mì nguyên cám'), -- Pizza, cheese and vegetables, whole wheat thick crust
    ('58109100', 'Pizza phô mai, đế mỏng không gluten'), -- Pizza, cheese, gluten-free thin crust
    ('58109110', 'Pizza phô mai, đế dày không gluten'), -- Pizza, cheese, gluten-free thick crust
    ('58109120', 'Pizza thịt, đế mỏng không gluten'), -- Pizza, with meat, gluten-free thin crust
    ('58109130', 'Pizza thịt, đế dày không gluten'), -- Pizza, with meat, gluten-free thick crust
    ('58109140', 'Pizza phô mai và rau, đế mỏng không gluten'), -- Pizza, cheese and vegetables, gluten-free thin crust
    ('58109150', 'Pizza phô mai và rau, đế dày không gluten'), -- Pizza, cheese and vegetables, gluten-free thick crust
    ('58109210', 'Pizza bữa sáng có trứng'), -- Breakfast pizza with egg
    ('58110110', 'Cuốn chiên egg roll, không thịt'), -- Egg roll, meatless
    ('58110120', 'Cuốn chiên egg roll, nhân tôm'), -- Egg roll, with shrimp
    ('58110130', 'Cuốn chiên egg roll, nhân thịt bò và/hoặc thịt lợn'), -- Egg roll, with beef and/or pork
    ('58110170', 'Cuốn chiên egg roll, nhân gà hoặc gà tây'), -- Egg roll, with chicken or turkey
    ('58110200', 'Gỏi cuốn bánh tráng nhân thịt và/hoặc tôm, rau, không chiên'), -- Roll with meat and/or shrimp, vegetables and rice paper, not fried
    ('58111110', 'Hoành thánh, há cảo hoặc bánh xếp (pot sticker), chiên'), -- Wonton, dumpling or pot sticker, fried
    ('58111120', 'Hoành thánh, há cảo hoặc bánh xếp (pot sticker), chiên, không thịt'), -- Wonton, dumpling or pot sticker, fried, no meat
    ('58111200', 'Bánh xốp chiên nhân thịt cua và phô mai kem'), -- Puffs, fried, crab meat and cream cheese filled
    ('58112510', 'Hoành thánh, há cảo hoặc bánh xếp (pot sticker), hấp'), -- Wonton, dumpling or pot sticker, steamed
    ('58116100', 'Bánh empanada, loại chung'), -- Empanada, NFS
    ('58116115', 'Bánh empanada, không thịt'), -- Empanada, no meat
    ('58116117', 'Bánh empanada nhân thịt bò'), -- Empanada, beef
    ('58116120', 'Bánh empanada nhân thịt bò và rau'), -- Empanada, beef, with vegetables
    ('58116125', 'Bánh empanada nhân gà'), -- Empanada, chicken
    ('58116130', 'Bánh empanada nhân gà và rau'), -- Empanada, chicken, with vegetables
    ('58117210', 'Que bột ngô kiểu Puerto Rico'), -- Cornmeal stick, Puerto Rican style
    ('58117310', 'Bánh kibby kiểu Puerto Rico'), -- Kibby, Puerto Rican style
    ('58117410', 'Bánh cá tuyết muối chiên (bacalaitos fritos)'), -- Bacalaitos fritos
    ('58117510', 'Bánh hayacas kiểu Puerto Rico'), -- Hayacas, Puerto Rican style
    ('58120110', 'Bánh crepe nhân thịt'), -- Crepe, with meat
    ('58121610', 'Bánh pierogi (Ba Lan)'), -- Pierogi
    ('58122210', 'Bánh gnocchi phô mai'), -- Gnocchi, cheese
    ('58122220', 'Bánh gnocchi khoai tây'), -- Gnocchi, potato
    ('58122320', 'Bánh knish'), -- Knish
    ('58123110', 'Bánh bao (bao bun)'), -- Bao bun
    ('58123120', 'Bánh bao (bao bun), không thịt'), -- Bao bun, no meat
    ('58124210', 'Bánh ngọt nướng nhân phô mai'), -- Pastry, cheese-filled
    ('58124220', 'Bánh ngọt nướng nhân trứng và phô mai'), -- Pastry, egg and cheese filled
    ('58124230', 'Bánh ngọt nướng nhân thịt/gia cầm'), -- Pastry, meat / poultry-filled
    ('58124250', 'Bánh spanakopita (Hy Lạp)'), -- Spanakopita
    ('58124500', 'Bánh samosa (Ấn Độ)'), -- Samosa
    ('58125110', 'Bánh quiche nhân thịt, gia cầm hoặc cá'), -- Quiche with meat, poultry or fish
    ('58125120', 'Bánh quiche rau chân vịt, không thịt'), -- Spinach quiche, meatless
    ('58125180', 'Bánh quiche phô mai, không thịt'), -- Cheese quiche, meatless
    ('58126100', 'Bánh turnover hoặc hot pocket, loại chung'), -- Turnover or hot pocket, NFS
    ('58126105', 'Bánh turnover hoặc hot pocket, không thịt'), -- Turnover or hot pocket, meatless
    ('58126125', 'Bánh turnover hoặc hot pocket nhân thịt bò'), -- Turnover or hot pocket, beef
    ('58126130', 'Bánh turnover hoặc hot pocket nhân giăm bông'), -- Turnover or hot pocket, ham
    ('58126150', 'Bánh turnover hoặc hot pocket, kiểu pizza pocket, có thịt'), -- Turnover or hot pocket, pizza pocket, meat
    ('58126160', 'Bánh turnover hoặc hot pocket, kiểu pizza pocket'), -- Turnover or hot pocket, pizza pocket
    ('58126270', 'Bánh turnover hoặc hot pocket nhân gà'), -- Turnover or hot pocket, chicken
    ('58126275', 'Bánh turnover nhân hải sản'), -- Turnover, seafood
    ('58126290', 'Bánh turnover hoặc lean pocket'), -- Turnover or lean pocket
    ('58126400', 'Bánh turnover hoặc breakfast pocket, nhân trứng'), -- Turnover or breakfast pocket, egg
    ('58128000', 'Bánh biscuit với nước sốt gravy'), -- Biscuit with gravy
    ('58128110', 'Bánh mì ngô gà'), -- Chicken cornbread
    ('58128120', 'Nhân nhồi (dressing) bột ngô với gà hoặc gà tây và rau'), -- Cornmeal dressing with chicken or turkey and vegetables
    ('58128210', 'Nhân nhồi (dressing) với hàu'), -- Dressing with oysters
    ('58128220', 'Nhân nhồi (dressing) với gà hoặc gà tây và rau'), -- Dressing with chicken or turkey and vegetables
    ('58128250', 'Nhân nhồi (dressing) với thịt và rau'), -- Dressing with meat and vegetables
    ('58130011', 'Lasagna thịt'), -- Lasagna with meat
    ('58130013', 'Lasagna thịt, đóng hộp'), -- Lasagna with meat, canned
    ('58130014', 'Lasagna thịt, nhà hàng'), -- Lasagna with meat, from restaurant
    ('58130015', 'Lasagna thịt, tự làm tại nhà'), -- Lasagna with meat, home recipe
    ('58130016', 'Lasagna thịt, đông lạnh'), -- Lasagna with meat, frozen
    ('58130020', 'Lasagna thịt và rau chân vịt'), -- Lasagna with meat and spinach
    ('58130140', 'Lasagna gà hoặc gà tây'), -- Lasagna with chicken or turkey
    ('58130150', 'Lasagna gà hoặc gà tây, có rau chân vịt'), -- Lasagna, with chicken or turkey, and spinach
    ('58130310', 'Lasagna không thịt'), -- Lasagna, meatless
    ('58130320', 'Lasagna không thịt, có rau'), -- Lasagna, meatless, with vegetables
    ('58131100', 'Ravioli không rõ nhân, không sốt'), -- Ravioli, NS as to filling, no sauce
    ('58131110', 'Ravioli không rõ nhân, sốt cà chua'), -- Ravioli, NS as to filling, with tomato sauce
    ('58131120', 'Ravioli không rõ nhân, sốt kem'), -- Ravioli, NS as to filling, with cream sauce
    ('58131310', 'Ravioli nhân thịt, không sốt'), -- Ravioli, meat-filled, no sauce
    ('58131320', 'Ravioli nhân thịt, sốt cà chua hoặc sốt thịt'), -- Ravioli, meat-filled, with tomato sauce or meat sauce
    ('58131323', 'Ravioli nhân thịt, sốt cà chua hoặc sốt thịt, đóng hộp'), -- Ravioli, meat-filled, with tomato sauce or meat sauce, canned
    ('58131330', 'Ravioli nhân thịt, sốt kem'), -- Ravioli, meat-filled, with cream sauce
    ('58131510', 'Ravioli nhân phô mai, không sốt'), -- Ravioli, cheese-filled, no sauce
    ('58131520', 'Ravioli nhân phô mai, sốt cà chua'), -- Ravioli, cheese-filled, with tomato sauce
    ('58131523', 'Ravioli nhân phô mai, sốt cà chua, đóng hộp'), -- Ravioli, cheese-filled, with tomato sauce, canned
    ('58131530', 'Ravioli nhân phô mai, sốt thịt'), -- Ravioli, cheese-filled, with meat sauce
    ('58131535', 'Ravioli nhân phô mai, sốt kem'), -- Ravioli, cheese-filled, with cream sauce
    ('58131590', 'Ravioli nhân phô mai và rau chân vịt, không sốt'), -- Ravioli, cheese and spinach-filled, no sauce
    ('58131600', 'Ravioli nhân phô mai và rau chân vịt, sốt kem'), -- Ravioli, cheese and spinach-filled, with cream sauce
    ('58131610', 'Ravioli nhân phô mai và rau chân vịt, sốt cà chua'), -- Ravioli, cheese and spinach filled, with tomato sauce
    ('58133110', 'Mì ống manicotti nhân phô mai, không sốt'), -- Manicotti, cheese-filled, no sauce
    ('58133120', 'Mì ống manicotti nhân phô mai, sốt cà chua, không thịt'), -- Manicotti, cheese-filled, with tomato sauce, meatless
    ('58133130', 'Mì ống manicotti nhân phô mai, sốt thịt'), -- Manicotti, cheese-filled, with meat sauce
    ('58133140', 'Mì ống manicotti nhân rau và phô mai, sốt cà chua, không thịt'), -- Manicotti, vegetable- and cheese-filled, with tomato sauce, meatless
    ('58134110', 'Mì vỏ sò nhồi phô mai, không sốt'), -- Stuffed shells, cheese-filled, no sauce
    ('58134120', 'Mì vỏ sò nhồi phô mai, sốt cà chua, không thịt'), -- Stuffed shells, cheese-filled, with tomato sauce, meatless
    ('58134130', 'Mì vỏ sò nhồi phô mai, sốt thịt'), -- Stuffed shells, cheese-filled, with meat sauce
    ('58134160', 'Mì vỏ sò nhồi phô mai và rau chân vịt, không sốt'), -- Stuffed shells, cheese- and spinach- filled, no sauce
    ('58134210', 'Mì vỏ sò nhồi gà, sốt cà chua'), -- Stuffed shells, with chicken, with tomato sauce
    ('58134310', 'Mì vỏ sò nhồi cá và/hoặc hải sản có vỏ, sốt cà chua'), -- Stuffed shells, with fish and/or shellfish, with tomato sauce
    ('58134610', 'Mì tortellini nhân thịt, sốt cà chua'), -- Tortellini, meat-filled, with tomato sauce
    ('58134613', 'Mì tortellini nhân thịt, sốt cà chua, đóng hộp'), -- Tortellini, meat-filled, with tomato sauce, canned
    ('58134620', 'Mì tortellini nhân phô mai, không thịt, sốt cà chua'), -- Tortellini, cheese-filled, meatless, with tomato sauce
    ('58134623', 'Mì tortellini nhân phô mai, không thịt, sốt cà chua, đóng hộp'), -- Tortellini, cheese-filled, meatless, with tomato sauce, canned
    ('58134640', 'Mì tortellini nhân phô mai, không thịt, sốt dầu giấm (vinaigrette)'), -- Tortellini, cheese-filled, meatless, with vinaigrette dressing
    ('58134650', 'Mì tortellini nhân thịt, không sốt'), -- Tortellini, meat-filled, no sauce
    ('58134660', 'Mì tortellini nhân phô mai, sốt kem'), -- Tortellini, cheese-filled, with cream sauce
    ('58134680', 'Mì tortellini nhân phô mai, không sốt'), -- Tortellini, cheese-filled, no sauce
    ('58134710', 'Mì tortellini nhân rau chân vịt, sốt cà chua'), -- Tortellini, spinach-filled, with tomato sauce
    ('58134720', 'Mì tortellini nhân rau chân vịt, không sốt'), -- Tortellini, spinach-filled, no sauce
    ('58134810', 'Mì ống cannelloni nhân phô mai và rau chân vịt, không sốt'), -- Cannelloni, cheese- and spinach-filled, no sauce
    ('58135110', 'Phở xào chow fun với thịt và rau'), -- Chow fun noodles with meat and vegetables
    ('58135120', 'Phở xào chow fun với rau, không thịt'), -- Chow fun noodles with vegetables, meatless
    ('58136110', 'Mì xào lo mein, loại chung'), -- Lo mein, NFS
    ('58136120', 'Mì xào lo mein, không thịt'), -- Lo mein, meatless
    ('58136130', 'Mì xào lo mein tôm'), -- Lo mein, with shrimp
    ('58136140', 'Mì xào lo mein thịt lợn'), -- Lo mein, with pork
    ('58136150', 'Mì xào lo mein thịt bò'), -- Lo mein, with beef
    ('58136160', 'Mì xào lo mein gà'), -- Lo mein, with chicken
    ('58137210', 'Pad Thái, loại chung'), -- Pad Thai, NFS
    ('58137220', 'Pad Thái, không thịt'), -- Pad Thai, meatless
    ('58137230', 'Pad Thái gà'), -- Pad Thai with chicken
    ('58137240', 'Pad Thái hải sản'), -- Pad Thai with seafood
    ('58137250', 'Pad Thái thịt'), -- Pad Thai with meat
    ('58137300', 'Món adobo (Philippines) với mì sợi'), -- Adobo, with noodles
    ('58140310', 'Nui với cá ngừ kiểu Puerto Rico'), -- Macaroni with tuna, Puerto Rican style
    ('58145110', 'Nui hoặc mì sợi trộn phô mai'), -- Macaroni or noodles with cheese
    ('58145111', 'Nui hoặc mì sợi trộn phô mai, nhà hàng'), -- Macaroni or noodles with cheese, from restaurant
    ('58145112', 'Nui hoặc mì sợi trộn phô mai, làm từ gói pha sẵn'), -- Macaroni or noodles with cheese, made from packaged mix
    ('58145113', 'Nui hoặc mì sợi trộn phô mai, đóng hộp'), -- Macaroni or noodles with cheese, canned
    ('58145117', 'Nui hoặc mì sợi trộn phô mai, loại Easy Mac'), -- Macaroni or noodles with cheese, Easy Mac type
    ('58145119', 'Nui hoặc mì sợi trộn phô mai, làm từ gói pha sẵn giảm béo'), -- Macaroni or noodles with cheese, made from reduced fat packaged mix
    ('58145120', 'Nui hoặc mì sợi trộn phô mai và cá ngừ'), -- Macaroni or noodles with cheese and tuna
    ('58145135', 'Nui hoặc mì sợi trộn phô mai và thịt'), -- Macaroni or noodles with cheese and meat
    ('58145136', 'Nui hoặc mì sợi trộn phô mai và thịt, làm từ gói pha sẵn (Hamburger Helper)'), -- Macaroni or noodles with cheese and meat, prepared from Hamburger Helper mix
    ('58145140', 'Nui hoặc mì sợi trộn phô mai và cà chua'), -- Macaroni or noodles with cheese and tomato
    ('58145160', 'Nui hoặc mì sợi trộn phô mai và xúc xích hot dog'), -- Macaroni or noodles with cheese and frankfurters or hot dogs
    ('58145170', 'Nui hoặc mì sợi trộn phô mai và trứng'), -- Macaroni or noodles with cheese and egg
    ('58145190', 'Nui hoặc mì sợi trộn phô mai và gà hoặc gà tây'), -- Macaroni or noodles with cheese and chicken or turkey
    ('58145300', 'Nui hoặc mì sợi trộn phô mai, loại nguyên hạt'), -- Macaroni or noodles with cheese, whole grain
    ('58146120', 'Mì Ý (pasta) sốt nền cà chua, có phô mai và thịt'), -- Pasta with tomato-based sauce, cheese and meat
    ('58146150', 'Mì Ý (pasta) sốt nền cà chua, có phô mai'), -- Pasta with tomato-based sauce and cheese
    ('58146160', 'Mì Ý (pasta) với rau, không sốt, không sốt trộn'), -- Pasta with vegetables, no sauce or dressing
    ('58146210', 'Mì Ý (pasta) có sốt, loại chung'), -- Pasta with sauce, NFS
    ('58146215', 'Mì Ý (pasta) có sốt, không thịt, bữa trưa trường học'), -- Pasta with sauce, meatless, school lunch
    ('58146221', 'Mì Ý (pasta) sốt nền cà chua, nhà hàng'), -- Pasta with tomato-based sauce, restaurant
    ('58146222', 'Mì Ý (pasta) sốt nền cà chua, tự làm tại nhà'), -- Pasta with tomato-based sauce, home recipe
    ('58146223', 'Mì Ý (pasta) sốt nền cà chua, chỉ cần hâm nóng'), -- Pasta with tomato-based sauce, ready-to-heat
    ('58146301', 'Mì Ý (pasta) sốt nền cà chua, có thêm rau, nhà hàng'), -- Pasta with tomato-based sauce, and added vegetables, restaurant
    ('58146302', 'Mì Ý (pasta) sốt nền cà chua, có thêm rau, tự làm tại nhà'), -- Pasta with tomato-based sauce, and added vegetables, home recipe
    ('58146303', 'Mì Ý (pasta) sốt nền cà chua, có thêm rau, chỉ cần hâm nóng'), -- Pasta with tomato-based sauce, and added vegetables, ready-to-heat
    ('58146315', 'Mì Ý (pasta) có sốt và thịt, bữa trưa trường học'), -- Pasta with sauce and meat, from school lunch
    ('58146321', 'Mì Ý (pasta) sốt nền cà chua, có thịt, nhà hàng'), -- Pasta with tomato-based sauce and meat, restaurant
    ('58146322', 'Mì Ý (pasta) sốt nền cà chua, có thịt, tự làm tại nhà'), -- Pasta with tomato-based sauce and meat, home recipe
    ('58146323', 'Mì Ý (pasta) sốt nền cà chua, có thịt, chỉ cần hâm nóng'), -- Pasta with tomato-based sauce and meat, ready-to-heat
    ('58146331', 'Mì Ý (pasta) sốt nền cà chua, có thịt và thêm rau, nhà hàng'), -- Pasta with tomato-based sauce, meat, and added vegetables, restaurant
    ('58146332', 'Mì Ý (pasta) sốt nền cà chua, có thịt và thêm rau, tự làm tại nhà'), -- Pasta with tomato-based sauce, meat, and added vegetables, home recipe
    ('58146333', 'Mì Ý (pasta) sốt nền cà chua, có thịt và thêm rau, chỉ cần hâm nóng'), -- Pasta with tomato-based sauce, meat, and added vegetables, ready-to-heat
    ('58146341', 'Mì Ý (pasta) sốt nền cà chua, có gia cầm, nhà hàng'), -- Pasta with tomato-based sauce and poultry, restaurant
    ('58146342', 'Mì Ý (pasta) sốt nền cà chua, có gia cầm, tự làm tại nhà'), -- Pasta with tomato-based sauce and poultry, home recipe
    ('58146343', 'Mì Ý (pasta) sốt nền cà chua, có gia cầm, chỉ cần hâm nóng'), -- Pasta with tomato-based sauce and poultry, ready-to-heat
    ('58146351', 'Mì Ý (pasta) sốt nền cà chua, có gia cầm và thêm rau, nhà hàng'), -- Pasta with tomato-based sauce, poultry, and added vegetables, restaurant
    ('58146352', 'Mì Ý (pasta) sốt nền cà chua, có gia cầm và thêm rau, tự làm tại nhà'), -- Pasta with tomato-based sauce, poultry, and added vegetables, home recipe
    ('58146353', 'Mì Ý (pasta) sốt nền cà chua, có gia cầm và thêm rau, chỉ cần hâm nóng'), -- Pasta with tomato-based sauce, poultry, and added vegetables, ready-to-heat
    ('58146361', 'Mì Ý (pasta) sốt nền cà chua, có hải sản, nhà hàng'), -- Pasta with tomato-based sauce and seafood, restaurant
    ('58146362', 'Mì Ý (pasta) sốt nền cà chua, có hải sản, tự làm tại nhà'), -- Pasta with tomato-based sauce and seafood, home recipe
    ('58146363', 'Mì Ý (pasta) sốt nền cà chua, có hải sản, chỉ cần hâm nóng'), -- Pasta with tomato-based sauce and seafood, ready-to-heat
    ('58146371', 'Mì Ý (pasta) sốt nền cà chua, có hải sản và thêm rau, nhà hàng'), -- Pasta with tomato-based sauce, seafood, and added vegetables, restaurant
    ('58146372', 'Mì Ý (pasta) sốt nền cà chua, có hải sản và thêm rau, tự làm tại nhà'), -- Pasta with tomato-based sauce, seafood, and added vegetables, home recipe
    ('58146373', 'Mì Ý (pasta) sốt nền cà chua, có hải sản và thêm rau, chỉ cần hâm nóng'), -- Pasta with tomato-based sauce, seafood, and added vegetables, ready-to-heat
    ('58146381', 'Mì Ý (pasta) sốt kem, nhà hàng'), -- Pasta with cream sauce, restaurant
    ('58146382', 'Mì Ý (pasta) sốt kem, tự làm tại nhà'), -- Pasta with cream sauce, home recipe
    ('58146383', 'Mì Ý (pasta) sốt kem, chỉ cần hâm nóng'), -- Pasta with cream sauce, ready-to-heat
    ('58146391', 'Mì Ý (pasta) sốt kem, có thêm rau, nhà hàng'), -- Pasta with cream sauce and added vegetables, restaurant
    ('58146392', 'Mì Ý (pasta) sốt kem, có thêm rau, tự làm tại nhà'), -- Pasta with cream sauce and added vegetables, from home recipe
    ('58146393', 'Mì Ý (pasta) sốt kem, có thêm rau, chỉ cần hâm nóng'), -- Pasta with cream sauce and added vegetables, ready-to-heat
    ('58146401', 'Mì Ý (pasta) sốt kem, có thịt, nhà hàng'), -- Pasta with cream sauce and meat, restaurant
    ('58146402', 'Mì Ý (pasta) sốt kem, có thịt, tự làm tại nhà'), -- Pasta with cream sauce and meat, home recipe
    ('58146403', 'Mì Ý (pasta) sốt kem, có thịt, chỉ cần hâm nóng'), -- Pasta with cream sauce and meat, ready-to-heat
    ('58146411', 'Mì Ý (pasta) sốt kem, có thịt và thêm rau, nhà hàng'), -- Pasta with cream sauce, meat, and added vegetables, restaurant
    ('58146412', 'Mì Ý (pasta) sốt kem, có thịt và thêm rau, tự làm tại nhà'), -- Pasta with cream sauce, meat, and added vegetables, home recipe
    ('58146413', 'Mì Ý (pasta) sốt kem, có thịt và thêm rau, chỉ cần hâm nóng'), -- Pasta with cream sauce, meat, and added vegetables, ready-to-heat
    ('58146421', 'Mì Ý (pasta) sốt kem, có gia cầm, nhà hàng'), -- Pasta with cream sauce and poultry, restaurant
    ('58146422', 'Mì Ý (pasta) sốt kem, có gia cầm, tự làm tại nhà'), -- Pasta with cream sauce and poultry, home recipe
    ('58146423', 'Mì Ý (pasta) sốt kem, có gia cầm, chỉ cần hâm nóng'), -- Pasta with cream sauce and poultry, ready-to-heat
    ('58146431', 'Mì Ý (pasta) sốt kem, có gia cầm và thêm rau, nhà hàng'), -- Pasta with cream sauce, poultry, and added vegetables, restaurant
    ('58146432', 'Mì Ý (pasta) sốt kem, có gia cầm và thêm rau, tự làm tại nhà'), -- Pasta with cream sauce, poultry, and added vegetables, home recipe
    ('58146433', 'Mì Ý (pasta) sốt kem, có gia cầm và thêm rau, chỉ cần hâm nóng'), -- Pasta with cream sauce, poultry, and added vegetables, ready-to-heat
    ('58146441', 'Mì Ý (pasta) sốt kem, có hải sản, nhà hàng'), -- Pasta with cream sauce and seafood, restaurant
    ('58146442', 'Mì Ý (pasta) sốt kem, có hải sản, tự làm tại nhà'), -- Pasta with cream sauce and seafood, home recipe
    ('58146443', 'Mì Ý (pasta) sốt kem, có hải sản, chỉ cần hâm nóng'), -- Pasta with cream sauce and seafood, ready-to-heat
    ('58146451', 'Mì Ý (pasta) sốt kem, có hải sản và thêm rau, nhà hàng'), -- Pasta with cream sauce, seafood, and added vegetables, restaurant
    ('58146452', 'Mì Ý (pasta) sốt kem, có hải sản và thêm rau, tự làm tại nhà'), -- Pasta with cream sauce, seafood, and added vegetables, home recipe
    ('58146453', 'Mì Ý (pasta) sốt kem, có hải sản và thêm rau, chỉ cần hâm nóng'), -- Pasta with cream sauce, seafood, and added vegetables, ready-to-heat
    ('58146601', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, nhà hàng'), -- Pasta, whole grain, with tomato-based sauce, restaurant
    ('58146602', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, tự làm tại nhà'), -- Pasta, whole grain, with tomato-based sauce, home recipe
    ('58146603', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, chỉ cần hâm nóng'), -- Pasta, whole grain, with tomato-based sauce, ready-to-heat
    ('58146611', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có thêm rau, nhà hàng'), -- Pasta, whole grain, with tomato-based sauce and added vegetables, restaurant
    ('58146612', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có thêm rau, tự làm tại nhà'), -- Pasta, whole grain, with tomato-based sauce and added vegetables, home recipe
    ('58146613', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có thêm rau, chỉ cần hâm nóng'), -- Pasta, whole grain, with tomato-based sauce and added vegetables, ready-to-heat
    ('58146621', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có thịt, nhà hàng'), -- Pasta, whole grain, with tomato-based sauce and meat, restaurant
    ('58146622', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có thịt, tự làm tại nhà'), -- Pasta, whole grain, with tomato-based sauce and meat, home recipe
    ('58146623', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có thịt, chỉ cần hâm nóng'), -- Pasta, whole grain, with tomato-based sauce and meat, ready-to-heat
    ('58146631', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có thịt và thêm rau, nhà hàng'), -- Pasta, whole grain, with tomato-based sauce, meat, and added vegetables, restaurant
    ('58146632', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có thịt và thêm rau, tự làm tại nhà'), -- Pasta, whole grain, with tomato-based sauce, meat, and added vegetables, home recipe
    ('58146633', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có thịt và thêm rau, chỉ cần hâm nóng'), -- Pasta, whole grain, with tomato-based sauce, meat, and added vegetables, ready-to-heat
    ('58146641', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có gia cầm, nhà hàng'), -- Pasta, whole grain, with tomato-based sauce and poultry, restaurant
    ('58146642', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có gia cầm, tự làm tại nhà'), -- Pasta, whole grain, with tomato-based sauce and poultry, home recipe
    ('58146643', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có gia cầm, chỉ cần hâm nóng'), -- Pasta, whole grain, with tomato-based sauce and poultry, ready-to-heat
    ('58146651', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có gia cầm và thêm rau, nhà hàng'), -- Pasta, whole grain, with tomato-based sauce, poultry, and added vegetables, restaurant
    ('58146652', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có gia cầm và thêm rau, tự làm tại nhà'), -- Pasta, whole grain, with tomato-based sauce, poultry, and added vegetables, home recipe
    ('58146653', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có gia cầm và thêm rau, chỉ cần hâm nóng'), -- Pasta, whole grain, with tomato-based sauce, poultry, and added vegetables, ready-to-heat
    ('58146661', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có hải sản, nhà hàng'), -- Pasta, whole grain, with tomato-based sauce and seafood, restaurant
    ('58146662', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có hải sản, tự làm tại nhà'), -- Pasta, whole grain, with tomato-based sauce and seafood, home recipe
    ('58146663', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có hải sản, chỉ cần hâm nóng'), -- Pasta, whole grain, with tomato-based sauce and seafood, ready-to-heat
    ('58146671', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có hải sản và thêm rau, nhà hàng'), -- Pasta, whole grain, with tomato-based sauce, seafood, and added vegetables, restaurant
    ('58146672', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có hải sản và thêm rau, tự làm tại nhà'), -- Pasta, whole grain, with tomato-based sauce, seafood, and added vegetables, home recipe
    ('58146673', 'Mì Ý (pasta) nguyên hạt, sốt nền cà chua, có hải sản và thêm rau, chỉ cần hâm nóng'), -- Pasta, whole grain, with tomato-based sauce, seafood, and added vegetables, ready-to-heat
    ('58146681', 'Mì Ý (pasta) nguyên hạt, sốt kem, nhà hàng'), -- Pasta, whole grain, with cream sauce, restaurant
    ('58146682', 'Mì Ý (pasta) nguyên hạt, sốt kem, tự làm tại nhà'), -- Pasta, whole grain, with cream sauce, home recipe
    ('58146683', 'Mì Ý (pasta) nguyên hạt, sốt kem, chỉ cần hâm nóng'), -- Pasta, whole grain, with cream sauce, ready-to-heat
    ('58146691', 'Mì Ý (pasta) nguyên hạt, sốt kem, có thêm rau, nhà hàng'), -- Pasta, whole grain, with cream sauce, and added vegetables, restaurant
    ('58146692', 'Mì Ý (pasta) nguyên hạt, sốt kem, có thêm rau, tự làm tại nhà'), -- Pasta, whole grain, with cream sauce, and added vegetables, home recipe
    ('58146693', 'Mì Ý (pasta) nguyên hạt, sốt kem, có thêm rau, chỉ cần hâm nóng'), -- Pasta, whole grain, with cream sauce, and added vegetables, ready-to-heat
    ('58146701', 'Mì Ý (pasta) nguyên hạt, sốt kem, có thịt, nhà hàng'), -- Pasta, whole grain, with cream sauce and meat, restaurant
    ('58146702', 'Mì Ý (pasta) nguyên hạt, sốt kem, có thịt, tự làm tại nhà'), -- Pasta, whole grain, with cream sauce and meat, home recipe
    ('58146703', 'Mì Ý (pasta) nguyên hạt, sốt kem, có thịt, chỉ cần hâm nóng'), -- Pasta, whole grain, with cream sauce and meat, ready-to-heat
    ('58146711', 'Mì Ý (pasta) nguyên hạt, sốt kem, có thịt và thêm rau, nhà hàng'), -- Pasta, whole grain, with cream sauce, meat, and added vegetables, restaurant
    ('58146712', 'Mì Ý (pasta) nguyên hạt, sốt kem, có thịt và thêm rau, tự làm tại nhà'), -- Pasta, whole grain, with cream sauce, meat, and added vegetables, home recipe
    ('58146713', 'Mì Ý (pasta) nguyên hạt, sốt kem, có thịt và thêm rau, chỉ cần hâm nóng'), -- Pasta, whole grain, with cream sauce, meat, and added vegetables, ready-to-heat
    ('58146721', 'Mì Ý (pasta) nguyên hạt, sốt kem, có gia cầm, nhà hàng'), -- Pasta, whole grain, with cream sauce and poultry, restaurant
    ('58146722', 'Mì Ý (pasta) nguyên hạt, sốt kem, có gia cầm, tự làm tại nhà'), -- Pasta, whole grain, with cream sauce and poultry, home recipe
    ('58146723', 'Mì Ý (pasta) nguyên hạt, sốt kem, có gia cầm, chỉ cần hâm nóng'), -- Pasta, whole grain, with cream sauce and poultry, ready-to-heat
    ('58146731', 'Mì Ý (pasta) nguyên hạt, sốt kem, có gia cầm và thêm rau, nhà hàng'), -- Pasta, whole grain, with cream sauce, poultry, and added vegetables, restaurant
    ('58146732', 'Mì Ý (pasta) nguyên hạt, sốt kem, có gia cầm và thêm rau, tự làm tại nhà'), -- Pasta, whole grain, with cream sauce, poultry, and added vegetables, home recipe
    ('58146733', 'Mì Ý (pasta) nguyên hạt, sốt kem, có gia cầm và thêm rau, chỉ cần hâm nóng'), -- Pasta, whole grain, with cream sauce, poultry, and added vegetables, ready-to-heat
    ('58146741', 'Mì Ý (pasta) nguyên hạt, sốt kem, có hải sản, nhà hàng'), -- Pasta, whole grain, with cream sauce and seafood, restaurant
    ('58146742', 'Mì Ý (pasta) nguyên hạt, sốt kem, có hải sản, tự làm tại nhà'), -- Pasta, whole grain, with cream sauce and seafood, home recipe
    ('58146743', 'Mì Ý (pasta) nguyên hạt, sốt kem, có hải sản, chỉ cần hâm nóng'), -- Pasta, whole grain, with cream sauce and seafood, ready-to-heat
    ('58146751', 'Mì Ý (pasta) nguyên hạt, sốt kem, có hải sản và thêm rau, nhà hàng'), -- Pasta, whole grain, with cream sauce, seafood, and added vegetables, restaurant
    ('58146752', 'Mì Ý (pasta) nguyên hạt, sốt kem, có hải sản và thêm rau, tự làm tại nhà'), -- Pasta, whole grain, with cream sauce, seafood, and added vegetables, home recipe
    ('58146753', 'Mì Ý (pasta) nguyên hạt, sốt kem, có hải sản và thêm rau, chỉ cần hâm nóng'), -- Pasta, whole grain, with cream sauce, seafood, and added vegetables, ready-to-heat
    ('58147110', 'Mì Ý (pasta) sốt nền cà chua, có đậu hoặc đậu lăng'), -- Pasta with tomato-based sauce and beans or lentils
    ('58147330', 'Nui hoặc mì sợi sốt kem, với phô mai'), -- Macaroni or noodles, creamed, with cheese
    ('58147340', 'Nui hoặc mì sợi sốt kem, với phô mai và cá ngừ'), -- Macaroni or noodles, creamed, with cheese and tuna
    ('58147510', 'Mì Ý (pasta) có hương vị'), -- Flavored pasta
    ('58147520', 'Mì Yat Ga Mein với thịt, cá hoặc gia cầm'), -- Yat Ga Mein with meat, fish, or poultry
    ('58148110', 'Salad nui hoặc mì Ý (pasta), trộn với sốt mayonnaise'), -- Macaroni or pasta salad, made with mayonnaise
    ('58148111', 'Salad nui hoặc mì Ý (pasta), trộn với sốt mayonnaise loại nhẹ'), -- Macaroni or pasta salad, made with light mayonnaise
    ('58148112', 'Salad nui hoặc mì Ý (pasta), trộn với sốt trộn salad kiểu mayonnaise'), -- Macaroni or pasta salad, made with mayonnaise-type salad dressing
    ('58148113', 'Salad nui hoặc mì Ý (pasta), trộn với sốt trộn salad kiểu mayonnaise loại nhẹ'), -- Macaroni or pasta salad, made with light mayonnaise-type salad dressing
    ('58148114', 'Salad nui hoặc mì Ý (pasta), trộn với sốt trộn kiểu Ý'), -- Macaroni or pasta salad, made with Italian dressing
    ('58148115', 'Salad nui hoặc mì Ý (pasta), trộn với sốt trộn kiểu Ý loại nhẹ'), -- Macaroni or pasta salad, made with light Italian dressing
    ('58148116', 'Salad nui hoặc mì Ý (pasta), trộn với sốt trộn dạng kem'), -- Macaroni or pasta salad, made with creamy dressing
    ('58148117', 'Salad nui hoặc mì Ý (pasta), trộn với sốt trộn dạng kem loại nhẹ'), -- Macaroni or pasta salad, made with light creamy dressing
    ('58148118', 'Salad nui hoặc mì Ý (pasta), trộn với sốt trộn không béo (mọi loại)'), -- Macaroni or pasta salad, made with any type of fat free dressing
    ('58148120', 'Salad nui hoặc mì Ý (pasta) với trứng'), -- Macaroni or pasta salad with egg
    ('58148130', 'Salad nui hoặc mì Ý (pasta) với cá ngừ'), -- Macaroni or pasta salad with tuna
    ('58148140', 'Salad nui hoặc mì Ý (pasta) với thịt cua'), -- Macaroni or pasta salad with crab meat
    ('58148150', 'Salad nui hoặc mì Ý (pasta) với tôm'), -- Macaroni or pasta salad with shrimp
    ('58148160', 'Salad nui hoặc mì Ý (pasta) với cá ngừ và trứng'), -- Macaroni or pasta salad with tuna and egg
    ('58148170', 'Salad nui hoặc mì Ý (pasta) với gà'), -- Macaroni or pasta salad with chicken
    ('58148180', 'Salad nui hoặc mì Ý (pasta) với phô mai'), -- Macaroni or pasta salad with cheese
    ('58148550', 'Salad nui hoặc mì Ý (pasta) với thịt'), -- Macaroni or pasta salad with meat
    ('58149110', 'Bánh pudding mì sợi'), -- Noodle pudding
    ('58150100', 'Cơm trộn bibimbap (Hàn Quốc)'), -- Bibimbap, Korean
    ('58150110', 'Cơm chiên, không thịt'), -- Rice, fried, meatless
    ('58150310', 'Cơm chiên, loại chung'), -- Rice, fried, NFS
    ('58150320', 'Cơm chiên gà'), -- Rice, fried, with chicken
    ('58150330', 'Cơm chiên thịt lợn'), -- Rice, fried, with pork
    ('58150340', 'Cơm chiên thịt bò'), -- Rice, fried, with beef
    ('58150510', 'Cơm chiên tôm'), -- Rice, fried, with shrimp
    ('58150520', 'Bánh gạo tteokbokki (Hàn Quốc)'), -- Dukboki or Tteokbokki, Korean
    ('58150530', 'Món adobo (Philippines) với cơm'), -- Adobo, with rice
    ('58151100', 'Sushi, loại chung'), -- Sushi, NFS
    ('58151170', 'Sushi cuộn quả bơ'), -- Sushi roll, avocado
    ('58151180', 'Sushi cuộn California'), -- Sushi roll, California
    ('58151190', 'Sushi cuộn lươn'), -- Sushi roll, eel
    ('58151200', 'Sushi cuộn cá hồi'), -- Sushi roll, salmon
    ('58151210', 'Sushi cuộn tôm'), -- Sushi roll, shrimp
    ('58151220', 'Sushi cuộn cá ngừ'), -- Sushi roll tuna
    ('58151230', 'Sushi cuộn rau'), -- Sushi roll, vegetable
    ('58151400', 'Sushi phủ cua'), -- Sushi, topped with crab
    ('58151410', 'Sushi phủ lươn'), -- Sushi, topped with eel
    ('58151420', 'Sushi phủ cá hồi'), -- Sushi, topped with salmon
    ('58151430', 'Sushi phủ tôm'), -- Sushi, topped with shrimp
    ('58151440', 'Sushi phủ cá ngừ'), -- Sushi, topped with tuna
    ('58151450', 'Sushi phủ trứng'), -- Sushi, topped with egg
    ('58155320', 'Cơm paella hải sản kiểu Puerto Rico'), -- Seafood paella, Puerto Rican style
    ('58155410', 'Cơm nấu nhiều nước với gà kiểu Puerto Rico'), -- Soupy rice with chicken, Puerto Rican style
    ('58155510', 'Hỗn hợp cơm nấu nhiều nước với gà và khoai tây kiểu Puerto Rico'), -- Soupy rice mixture with chicken and potatoes, Puerto Rican style
    ('58155810', 'Cơm hầm kiểu Puerto Rico'), -- Stewed rice, Puerto Rican style
    ('58155910', 'Cơm với mực kiểu Puerto Rico'), -- Rice with squid, Puerto Rican style
    ('58156210', 'Cơm với xúc xích Vienna kiểu Puerto Rico'), -- Rice with vienna sausage, Puerto Rican style
    ('58156310', 'Cơm với xúc xích Tây Ban Nha kiểu Puerto Rico'), -- Rice with Spanish sausage, Puerto Rican style
    ('58156710', 'Cơm với đậu hầm kiểu Puerto Rico'), -- Rice with stewed beans, Puerto Rican style
    ('58157300', 'Cháo với thịt, gia cầm và/hoặc hải sản'), -- Congee, with meat, poultry, and/or seafood
    ('58157310', 'Cháo với thịt, gia cầm và/hoặc hải sản, có rau'), -- Congee, with meat, poultry, and/or seafood, and vegetables
    ('58157320', 'Cháo với rau'), -- Congee, with vegetables
    ('58157330', 'Cháo với trứng'), -- Congee, with egg
    ('58160000', 'Cơm biryani với rau'), -- Biryani with vegetables
    ('58160100', 'Đậu và cơm, đồ ăn nhanh/nhà hàng'), -- Beans and rice, from fast food / restaurant
    ('58160102', 'Đậu tây đỏ và cơm, đồ ăn nhanh/nhà hàng'), -- Kidney beans and rice, from fast food / restaurant
    ('58160104', 'Đậu đen và cơm, đồ ăn nhanh/nhà hàng'), -- Black beans and rice, from fast food / restaurant
    ('58160106', 'Đậu pinto và cơm, đồ ăn nhanh/nhà hàng'), -- Pinto beans and rice, from fast food / restaurant
    ('58160110', 'Đậu và cơm trắng'), -- Beans and white rice
    ('58160120', 'Đậu và cơm, có cà chua'), -- Beans and rice, with tomatoes
    ('58160132', 'Đậu và cơm, có thịt'), -- Beans and rice, with meat
    ('58160150', 'Đậu tây đỏ và cơm trắng'), -- Kidney beans and white rice
    ('58160154', 'Đậu đen và cơm trắng'), -- Black beans and white rice
    ('58160156', 'Đậu pinto và cơm trắng'), -- Pinto beans and white rice
    ('58160400', 'Cơm trắng với ngô, không rõ có thêm chất béo'), -- Rice, white, with corn, NS as to fat
    ('58160410', 'Cơm trắng với ngô, không thêm chất béo'), -- Rice, white, with corn, no added fat
    ('58160420', 'Cơm trắng với ngô, có thêm chất béo'), -- Rice, white, with corn, fat added
    ('58160430', 'Cơm trắng với đậu Hà Lan, không rõ có thêm chất béo'), -- Rice, white, with peas, NS as to fat
    ('58160440', 'Cơm trắng với đậu Hà Lan, không thêm chất béo'), -- Rice, white, with peas, no added fat
    ('58160450', 'Cơm trắng với đậu Hà Lan, có thêm chất béo'), -- Rice, white, with peas, fat added
    ('58160460', 'Cơm trắng với cà rốt, không rõ có thêm chất béo'), -- Rice, white, with carrots, NS as to fat
    ('58160470', 'Cơm trắng với cà rốt, không thêm chất béo'), -- Rice, white, with carrots, no added fat
    ('58160480', 'Cơm trắng với cà rốt, có thêm chất béo'), -- Rice, white, with carrots, fat added
    ('58160490', 'Cơm trắng với đậu Hà Lan và cà rốt, không rõ có thêm chất béo'), -- Rice, white, with peas and carrots, NS as to fat
    ('58160500', 'Cơm trắng với đậu Hà Lan và cà rốt, không thêm chất béo'), -- Rice, white, with peas and carrots, no added fat
    ('58160510', 'Cơm trắng với đậu Hà Lan và cà rốt, có thêm chất béo'), -- Rice, white, with peas and carrots, fat added
    ('58160520', 'Cơm trắng với cà chua và/hoặc sốt nền cà chua, không rõ có thêm chất béo'), -- Rice, white, with tomatoes and/or tomato-based sauce, NS as to fat
    ('58160530', 'Cơm trắng với cà chua và/hoặc sốt nền cà chua, không thêm chất béo'), -- Rice, white, with tomatoes and/or tomato-based sauce, no added fat
    ('58160540', 'Cơm trắng với cà chua và/hoặc sốt nền cà chua, có thêm chất béo'), -- Rice, white, with tomatoes and/or tomato-based sauce, fat added
    ('58160550', 'Cơm trắng với rau xanh đậm, không rõ có thêm chất béo'), -- Rice, white, with dark green vegetables, NS as to fat
    ('58160560', 'Cơm trắng với rau xanh đậm, không thêm chất béo'), -- Rice, white, with dark green vegetables, no added fat
    ('58160570', 'Cơm trắng với rau xanh đậm, có thêm chất béo'), -- Rice, white, with dark green vegetables, fat added
    ('58160580', 'Cơm trắng với cà rốt và cà chua và/hoặc sốt nền cà chua, không rõ có thêm chất béo'), -- Rice, white, with carrots and tomatoes and/or tomato-based sauce, NS as to fat
    ('58160590', 'Cơm trắng với cà rốt và cà chua và/hoặc sốt nền cà chua, không thêm chất béo'), -- Rice, white, with carrots and tomatoes and/or tomato-based sauce, no added fat
    ('58160600', 'Cơm trắng với cà rốt và cà chua và/hoặc sốt nền cà chua, có thêm chất béo'), -- Rice, white, with carrots and tomatoes and/or tomato-based sauce, fat added
    ('58160610', 'Cơm trắng với rau xanh đậm và cà chua và/hoặc sốt nền cà chua, không rõ có thêm chất béo'), -- Rice, white, with dark green vegetables and tomatoes and/or tomato-based sauce, NS as to fat
    ('58160620', 'Cơm trắng với rau xanh đậm và cà chua và/hoặc sốt nền cà chua, không thêm chất béo'), -- Rice, white, with dark green vegetables and tomatoes and/or tomato-based sauce, no added fat
    ('58160630', 'Cơm trắng với rau xanh đậm và cà chua và/hoặc sốt nền cà chua, có thêm chất béo'), -- Rice, white, with dark green vegetables and tomatoes and/or tomato-based sauce, fat added
    ('58160640', 'Cơm trắng với cà rốt và rau xanh đậm, không rõ có thêm chất béo'), -- Rice, white, with carrots and dark green vegetables, NS as to fat
    ('58160650', 'Cơm trắng với cà rốt và rau xanh đậm, không thêm chất béo'), -- Rice, white, with carrots and dark green vegetables, no added fat
    ('58160660', 'Cơm trắng với cà rốt và rau xanh đậm, có thêm chất béo'), -- Rice, white, with carrots and dark green vegetables, fat added
    ('58160670', 'Cơm trắng với cà rốt, rau xanh đậm và cà chua và/hoặc sốt nền cà chua, không rõ có thêm chất béo'), -- Rice, white, with carrots, dark green vegetables, and tomatoes and/or tomato-based sauce, NS as to fat
    ('58160680', 'Cơm trắng với cà rốt, rau xanh đậm và cà chua và/hoặc sốt nền cà chua, không thêm chất béo'), -- Rice, white, with carrots, dark green vegetables, and tomatoes and/or tomato-based sauce, no added fat
    ('58160690', 'Cơm trắng với cà rốt, rau xanh đậm và cà chua và/hoặc sốt nền cà chua, có thêm chất béo'), -- Rice, white, with carrots, dark green vegetables, and tomatoes and/or tomato-based sauce, fat added
    ('58160700', 'Cơm trắng với rau khác, không rõ có thêm chất béo'), -- Rice, white, with other vegetables, NS as to fat
    ('58160710', 'Cơm trắng với rau khác, không thêm chất béo'), -- Rice, white, with other vegetables, no added fat
    ('58160720', 'Cơm trắng với rau khác, có thêm chất béo'), -- Rice, white, with other vegetables, fat added
    ('58160800', 'Cơm trắng với đậu lăng, không rõ có thêm chất béo'), -- Rice, white, with lentils, NS as to fat
    ('58160805', 'Cơm trắng với đậu lăng, có thêm chất béo'), -- Rice, white, with lentils, fat added
    ('58160810', 'Cơm trắng với đậu lăng, không thêm chất béo'), -- Rice, white, with lentils, no added fat
    ('58161200', 'Cơm nấu với nước cốt dừa'), -- Rice, cooked with coconut milk
    ('58161320', 'Đậu và cơm gạo lứt'), -- Beans and brown rice
    ('58161321', 'Đậu tây đỏ và cơm gạo lứt'), -- Kidney beans and brown rice
    ('58161322', 'Đậu đen và cơm gạo lứt'), -- Black beans and brown rice
    ('58161323', 'Đậu pinto và cơm gạo lứt'), -- Pinto beans and brown rice
    ('58161420', 'Cơm gạo lứt với ngô, không rõ có thêm chất béo'), -- Rice, brown, with corn, NS as to fat
    ('58161422', 'Cơm gạo lứt với ngô, không thêm chất béo'), -- Rice, brown, with corn, no added fat
    ('58161424', 'Cơm gạo lứt với ngô, có thêm chất béo'), -- Rice, brown, with corn, fat added
    ('58161430', 'Cơm gạo lứt với đậu Hà Lan, không rõ có thêm chất béo'), -- Rice, brown, with peas, NS as to fat
    ('58161432', 'Cơm gạo lứt với đậu Hà Lan, không thêm chất béo'), -- Rice, brown, with peas, no added fat
    ('58161434', 'Cơm gạo lứt với đậu Hà Lan, có thêm chất béo'), -- Rice, brown, with peas, fat added
    ('58161435', 'Cơm gạo lứt với cà rốt, không rõ có thêm chất béo'), -- Rice, brown, with carrots, NS as to fat
    ('58161437', 'Cơm gạo lứt với cà rốt, không thêm chất béo'), -- Rice, brown, with carrots, no added fat
    ('58161439', 'Cơm gạo lứt với cà rốt, có thêm chất béo'), -- Rice, brown, with carrots, fat added
    ('58161440', 'Cơm gạo lứt với đậu Hà Lan và cà rốt, không rõ có thêm chất béo'), -- Rice, brown, with peas and carrots, NS as to fat
    ('58161442', 'Cơm gạo lứt với đậu Hà Lan và cà rốt, không thêm chất béo'), -- Rice, brown, with peas and carrots, no added fat
    ('58161444', 'Cơm gạo lứt với đậu Hà Lan và cà rốt, có thêm chất béo'), -- Rice, brown, with peas and carrots, fat added
    ('58161460', 'Cơm gạo lứt với cà chua và/hoặc sốt nền cà chua, không rõ có thêm chất béo'), -- Rice, brown, with tomatoes and/or tomato based sauce, NS as to fat
    ('58161462', 'Cơm gạo lứt với cà chua và/hoặc sốt nền cà chua, không thêm chất béo'), -- Rice, brown, with tomatoes and/or tomato based sauce, no added fat
    ('58161464', 'Cơm gạo lứt với cà chua và/hoặc sốt nền cà chua, có thêm chất béo'), -- Rice, brown, with tomatoes and/or tomato based sauce, fat added
    ('58161470', 'Cơm gạo lứt với rau xanh đậm, không rõ có thêm chất béo'), -- Rice, brown, with dark green vegetables, NS as to fat
    ('58161472', 'Cơm gạo lứt với rau xanh đậm, không thêm chất béo'), -- Rice, brown, with dark green vegetables, no added fat
    ('58161474', 'Cơm gạo lứt với rau xanh đậm, có thêm chất béo'), -- Rice, brown, with dark green vegetables, fat added
    ('58161480', 'Cơm gạo lứt với cà rốt và cà chua và/hoặc sốt nền cà chua, không rõ có thêm chất béo'), -- Rice, brown, with carrots and tomatoes and/or tomato-based sauce, NS as to fat
    ('58161482', 'Cơm gạo lứt với cà rốt và cà chua và/hoặc sốt nền cà chua, không thêm chất béo'), -- Rice, brown, with carrots and tomatoes and/or tomato-based sauce, no added fat
    ('58161484', 'Cơm gạo lứt với cà rốt và cà chua và/hoặc sốt nền cà chua, có thêm chất béo'), -- Rice, brown, with carrots and tomatoes and/or tomato-based sauce, fat added
    ('58161490', 'Cơm gạo lứt với rau xanh đậm và cà chua và/hoặc sốt nền cà chua, không rõ có thêm chất béo'), -- Rice, brown, with dark green vegetables and tomatoes and/or tomato-based sauce, NS as to fat
    ('58161492', 'Cơm gạo lứt với rau xanh đậm và cà chua và/hoặc sốt nền cà chua, không thêm chất béo'), -- Rice, brown, with dark green vegetables and tomatoes and/or tomato-based sauce, no added fat
    ('58161494', 'Cơm gạo lứt với rau xanh đậm và cà chua và/hoặc sốt nền cà chua, có thêm chất béo'), -- Rice, brown, with dark green vegetables and tomatoes and/or tomato-based sauce, fat added
    ('58161500', 'Cơm gạo lứt với cà rốt và rau xanh đậm, không rõ có thêm chất béo'), -- Rice, brown, with carrots and dark green vegetables, NS as to fat
    ('58161502', 'Cơm gạo lứt với cà rốt và rau xanh đậm, không thêm chất béo'), -- Rice, brown, with carrots and dark green vegetables, no added fat
    ('58161504', 'Cơm gạo lứt với cà rốt và rau xanh đậm, có thêm chất béo'), -- Rice, brown, with carrots and dark green vegetables, fat added
    ('58161510', 'Lá nho cuộn cơm'), -- Grape leaves stuffed with rice
    ('58161520', 'Cơm gạo lứt với cà rốt, rau xanh đậm và cà chua và/hoặc sốt nền cà chua, không rõ có thêm chất béo'), -- Rice, brown, with carrots, dark green vegetables, and tomatoes and/or tomato-based sauce, NS as to fat
    ('58161522', 'Cơm gạo lứt với cà rốt, rau xanh đậm và cà chua và/hoặc sốt nền cà chua, không thêm chất béo'), -- Rice, brown, with carrots, dark green vegetables, and tomatoes and/or tomato-based sauce, no added fat
    ('58161524', 'Cơm gạo lứt với cà rốt, rau xanh đậm và cà chua và/hoặc sốt nền cà chua, có thêm chất béo'), -- Rice, brown, with carrots, dark green vegetables, and tomatoes and/or tomato-based sauce, fat added
    ('58161530', 'Cơm gạo lứt với rau khác, không rõ có thêm chất béo'), -- Rice, brown, with other vegetables, NS as to fat
    ('58161532', 'Cơm gạo lứt với rau khác, không thêm chất béo'), -- Rice, brown, with other vegetables, no added fat
    ('58161534', 'Cơm gạo lứt với rau khác, có thêm chất béo'), -- Rice, brown, with other vegetables, fat added
    ('58161710', 'Cơm viên chiên xù (croquette)'), -- Rice croquette
    ('58162090', 'Ớt chuông nhồi thịt'), -- Stuffed pepper, with meat
    ('58162110', 'Ớt chuông nhồi cơm và thịt'), -- Stuffed pepper, with rice and meat
    ('58162120', 'Ớt chuông nhồi cơm, không thịt'), -- Stuffed pepper, with rice, meatless
    ('58162130', 'Cà chua nhồi cơm và thịt'), -- Stuffed tomato, with rice and meat
    ('58162140', 'Cà chua nhồi cơm, không thịt'), -- Stuffed tomato, with rice, meatless
    ('58162310', 'Cơm pilaf'), -- Rice pilaf
    ('58163130', 'Cơm dirty rice (kiểu Cajun)'), -- Dirty rice
    ('58163310', 'Hỗn hợp cơm có hương vị'), -- Flavored rice mixture
    ('58163330', 'Hỗn hợp cơm có hương vị với phô mai'), -- Flavored rice mixture with cheese
    ('58163360', 'Cơm có hương vị, gạo lứt và gạo hoang'), -- Flavored rice, brown and wild
    ('58163380', 'Hỗn hợp cơm và mì Ý (pasta) có hương vị'), -- Flavored rice and pasta mixture
    ('58163400', 'Hỗn hợp cơm và mì Ý (pasta) có hương vị, giảm muối'), -- Flavored rice and pasta mixture, reduced sodium
    ('58163405', 'Cơm kiểu Tây Ban Nha, nhà hàng'), -- Spanish rice, from restaurant
    ('58163410', 'Cơm kiểu Tây Ban Nha, có thêm chất béo'), -- Spanish rice, fat added
    ('58163420', 'Cơm kiểu Tây Ban Nha, không thêm chất béo'), -- Spanish rice, no added fat
    ('58163430', 'Cơm kiểu Tây Ban Nha, không rõ có thêm chất béo'), -- Spanish rice, NS as to fat
    ('58163450', 'Cơm kiểu Tây Ban Nha với thịt bò xay'), -- Spanish rice with ground beef
    ('58163510', 'Nhân nhồi (dressing) làm từ cơm'), -- Rice dressing
    ('58164110', 'Cơm với nho khô'), -- Rice with raisins
    ('58164210', 'Món tráng miệng hoặc salad cơm với hoa quả'), -- Rice dessert or salad with fruit
    ('58164500', 'Cơm trắng với sốt nền phô mai và/hoặc kem, không rõ có thêm chất béo'), -- Rice, white, with cheese and/or cream based sauce, NS as to fat
    ('58164510', 'Cơm trắng với sốt nền phô mai và/hoặc kem, không thêm chất béo'), -- Rice, white, with cheese and/or cream based sauce, no added fat
    ('58164520', 'Cơm trắng với sốt nền phô mai và/hoặc kem, có thêm chất béo'), -- Rice, white, with cheese and/or cream based sauce, fat added
    ('58164530', 'Cơm trắng với nước sốt gravy, không rõ có thêm chất béo'), -- Rice, white, with gravy, NS as to fat
    ('58164540', 'Cơm trắng với nước sốt gravy, không thêm chất béo'), -- Rice, white, with gravy, no added fat
    ('58164550', 'Cơm trắng với nước sốt gravy, có thêm chất béo'), -- Rice, white, with gravy, fat added
    ('58164560', 'Cơm trắng với sốt nền nước tương, không rõ có thêm chất béo'), -- Rice, white, with soy-based sauce, NS as to fat
    ('58164570', 'Cơm trắng với sốt nền nước tương, không thêm chất béo'), -- Rice, white, with soy-based sauce, no added fat
    ('58164580', 'Cơm trắng với sốt nền nước tương, có thêm chất béo'), -- Rice, white, with soy-based sauce, fat added
    ('58164800', 'Cơm gạo lứt với sốt nền phô mai và/hoặc kem, không rõ có thêm chất béo'), -- Rice, brown, with cheese and/or cream based sauce, NS as to fat
    ('58164810', 'Cơm gạo lứt với sốt nền phô mai và/hoặc kem, không thêm chất béo'), -- Rice, brown, with cheese and/or cream based sauce, no added fat
    ('58164820', 'Cơm gạo lứt với sốt nền phô mai và/hoặc kem, có thêm chất béo'), -- Rice, brown, with cheese and/or cream based sauce, fat added
    ('58164830', 'Cơm gạo lứt với nước sốt gravy, không rõ có thêm chất béo'), -- Rice, brown, with gravy, NS as to fat
    ('58164840', 'Cơm gạo lứt với nước sốt gravy, không thêm chất béo'), -- Rice, brown, with gravy, no added fat
    ('58164850', 'Cơm gạo lứt với nước sốt gravy, có thêm chất béo'), -- Rice, brown, with gravy, fat added
    ('58164860', 'Cơm gạo lứt với sốt nền nước tương, không rõ có thêm chất béo'), -- Rice, brown, with soy-based sauce, NS as to fat
    ('58164870', 'Cơm gạo lứt với sốt nền nước tương, không thêm chất béo'), -- Rice, brown, with soy-based sauce, no added fat
    ('58164880', 'Cơm gạo lứt với sốt nền nước tương, có thêm chất béo'), -- Rice, brown, with soy-based sauce, fat added
    ('58165000', 'Cơm trắng với rau, sốt nền phô mai và/hoặc kem, không rõ có thêm chất béo'), -- Rice, white, with vegetables, cheese and/or cream based sauce, NS as to fat
    ('58165010', 'Cơm trắng với rau, sốt nền phô mai và/hoặc kem, không thêm chất béo'), -- Rice, white, with vegetables, cheese and/or cream based sauce, no added fat
    ('58165020', 'Cơm trắng với rau, sốt nền phô mai và/hoặc kem, có thêm chất béo'), -- Rice, white, with vegetables, cheese and/or cream based sauce, fat added
    ('58165030', 'Cơm trắng với rau và nước sốt gravy, không rõ có thêm chất béo'), -- Rice, white, with vegetables and gravy, NS as to fat
    ('58165040', 'Cơm trắng với rau và nước sốt gravy, không thêm chất béo'), -- Rice, white, with vegetables and gravy, no added fat
    ('58165050', 'Cơm trắng với rau và nước sốt gravy, có thêm chất béo'), -- Rice, white, with vegetables and gravy, fat added
    ('58165060', 'Cơm trắng với rau, sốt nền nước tương, không rõ có thêm chất béo'), -- Rice, white, with vegetables, soy-based sauce, NS as to fat
    ('58165070', 'Cơm trắng với rau, sốt nền nước tương, không thêm chất béo'), -- Rice, white, with vegetables, soy-based sauce, no added fat
    ('58165080', 'Cơm trắng với rau, sốt nền nước tương, có thêm chất béo'), -- Rice, white, with vegetables, soy-based sauce, fat added
    ('58165400', 'Cơm gạo lứt với rau, sốt nền phô mai và/hoặc kem, không rõ có thêm chất béo'), -- Rice, brown, with vegetables, cheese and/or cream based sauce, NS as to fat
    ('58165410', 'Cơm gạo lứt với rau, sốt nền phô mai và/hoặc kem, không thêm chất béo'), -- Rice, brown, with vegetables, cheese and/or cream based sauce, no added fat
    ('58165420', 'Cơm gạo lứt với rau, sốt nền phô mai và/hoặc kem, có thêm chất béo'), -- Rice, brown, with vegetables, cheese and/or cream based sauce, fat added
    ('58165430', 'Cơm gạo lứt với rau và nước sốt gravy, không rõ có thêm chất béo'), -- Rice, brown, with vegetables and gravy, NS as to fat
    ('58165440', 'Cơm gạo lứt với rau và nước sốt gravy, không thêm chất béo'), -- Rice, brown, with vegetables and gravy, no added fat
    ('58165450', 'Cơm gạo lứt với rau và nước sốt gravy, có thêm chất béo'), -- Rice, brown, with vegetables and gravy, fat added
    ('58165460', 'Cơm gạo lứt với rau, sốt nền nước tương, không rõ có thêm chất béo'), -- Rice, brown, with vegetables, soy-based sauce, NS as to fat
    ('58165470', 'Cơm gạo lứt với rau, sốt nền nước tương, không thêm chất béo'), -- Rice, brown, with vegetables, soy-based sauce, no added fat
    ('58165480', 'Cơm gạo lứt với rau, sốt nền nước tương, có thêm chất béo'), -- Rice, brown, with vegetables, soy-based sauce, fat added
    ('58174000', 'Món upma (Ấn Độ)'), -- Upma
    ('58174100', 'Bánh dosa có nhân (Ấn Độ)'), -- Dosa, with filling
    ('58175000', 'Bánh vada (Ấn Độ)'), -- Vada
    ('58175110', 'Salad tabbouleh'), -- Tabbouleh
    ('58200210', 'Bánh mì kẹp rau, bánh mì trắng'), -- Vegetable sandwich on white
    ('58200220', 'Bánh mì kẹp rau, bánh mì trắng, có phô mai'), -- Vegetable sandwich on white, with cheese
    ('58200230', 'Bánh mì kẹp rau, bánh mì lúa mì'), -- Vegetable sandwich on wheat
    ('58200240', 'Bánh mì kẹp rau, bánh mì lúa mì, có phô mai'), -- Vegetable sandwich on wheat, with cheese
    ('58200250', 'Bánh cuốn wrap rau'), -- Vegetable sandwich wrap
    ('58201000', 'Bánh mì kẹp mứt, loại chung'), -- Jelly sandwich, NFS
    ('58201005', 'Bánh mì kẹp mứt, bánh mì trắng'), -- Jelly sandwich, on white bread
    ('58201015', 'Bánh mì kẹp mứt, bánh mì lúa mì'), -- Jelly sandwich, on wheat bread
    ('58302080', 'Mì sợi với rau sốt nền cà chua, suất ăn đông lạnh ăn kiêng'), -- Noodles with vegetables in tomato-based sauce, diet frozen meal
    ('58304010', 'Mì spaghetti thịt viên (bữa tối), loại chung, suất ăn đông lạnh'), -- Spaghetti and meatballs dinner, NFS, frozen meal
    ('58304060', 'Mì spaghetti sốt thịt, suất ăn đông lạnh ăn kiêng'), -- Spaghetti with meat sauce, diet frozen meal
    ('58310310', 'Bánh kếp (pancake) và xúc xích, suất ăn đông lạnh'), -- Pancakes and sausage, frozen meal
    ('58400000', 'Súp, loại chung'), -- Soup, NFS
    ('58400100', 'Súp mì sợi, loại chung'), -- Soup, noodle, NFS
    ('58400200', 'Súp gạo'), -- Soup, rice
    ('58401010', 'Súp lúa mạch'), -- Soup, barley
    ('58403010', 'Súp gà mì sợi, đóng hộp'), -- Soup, chicken noodle, canned
    ('58403040', 'Súp gà mì sợi'), -- Soup, chicken noodle
    ('58403060', 'Súp gà, đóng hộp, giảm muối'), -- Soup, chicken, canned, reduced sodium
    ('58404500', 'Súp viên bột matzo'), -- Soup, Matzo ball
    ('58407030', 'Súp mì ramen, có thêm nước'), -- Soup, ramen noodles, water added
    ('58407034', 'Tô mì ramen, loại chung'), -- Ramen bowl, NFS
    ('58407038', 'Tô mì ramen thịt bò'), -- Ramen bowl with beef
    ('58407042', 'Tô mì ramen gà'), -- Ramen bowl with chicken
    ('58407046', 'Tô mì ramen cá'), -- Ramen bowl with fish
    ('58407052', 'Tô mì ramen chay'), -- Ramen bowl, vegetarian
    ('58407056', 'Tô mì ramen thịt và trứng'), -- Ramen bowl with meat and egg
    ('58407060', 'Tô mì ramen chay có trứng'), -- Ramen bowl, vegetarian with egg
    ('58408010', 'Súp hoành thánh'), -- Soup, wonton
    ('58421020', 'Súp mì sợi sopa de fideo aguada (Mexico)'), -- Soup, sopa de fideo aguada
    ('58421080', 'Súp tortilla'), -- Soup, tortilla
    ('59003000', 'Thực phẩm thay thế thịt, làm từ ngũ cốc và đạm thực vật, chiên'), -- Meat substitute, cereal- and vegetable protein-based, fried
    ('61100600', 'Quýt clementine, tươi'), -- Clementine, raw
    ('61101010', 'Bưởi chùm, tươi'), -- Grapefruit, raw
    ('61101200', 'Bưởi chùm, đóng hộp'), -- Grapefruit, canned
    ('61110010', 'Quất, tươi'), -- Kumquat, raw
    ('61113010', 'Chanh vàng, tươi'), -- Lemon, raw
    ('61113500', 'Nhân bánh pie chanh vàng'), -- Lemon pie filling
    ('61116010', 'Chanh xanh, tươi'), -- Lime, raw
    ('61119010', 'Cam, tươi'), -- Orange, raw
    ('61122300', 'Cam, đóng hộp, loại chung'), -- Orange, canned, NFS
    ('61122320', 'Cam, đóng hộp, ngâm nước ép'), -- Orange, canned, juice pack
    ('61122330', 'Cam, đóng hộp, ngâm siro'), -- Orange, canned, in syrup
    ('61125010', 'Quýt, tươi'), -- Tangerine, raw
    ('61201010', 'Nước ép bưởi chùm 100%, vắt tươi'), -- Grapefruit juice, 100%, freshly squeezed
    ('61201020', 'Nước ép bưởi chùm 100%, không rõ dạng'), -- Grapefruit juice, 100%, NS as to form
    ('61201220', 'Nước ép bưởi chùm 100%, đóng hộp, đóng chai hoặc hộp giấy'), -- Grapefruit juice, 100%, canned, bottled or in a carton
    ('61201225', 'Nước ép bưởi chùm 100%, bổ sung canxi'), -- Grapefruit juice, 100%, with calcium added
    ('61204000', 'Nước ép chanh vàng 100%, không rõ dạng'), -- Lemon juice, 100%, NS as to form
    ('61204010', 'Nước ép chanh vàng 100%, vắt tươi'), -- Lemon juice, 100%, freshly squeezed
    ('61204200', 'Nước ép chanh vàng 100%, đóng hộp hoặc đóng chai'), -- Lemon juice, 100%, canned or bottled
    ('61207000', 'Nước ép chanh xanh 100%, không rõ dạng'), -- Lime juice, 100%, NS as to form
    ('61207010', 'Nước ép chanh xanh 100%, vắt tươi'), -- Lime juice, 100%, freshly squeezed
    ('61207200', 'Nước ép chanh xanh 100%, đóng hộp hoặc đóng chai'), -- Lime juice, 100%, canned or bottled
    ('61210000', 'Nước ép cam 100%, loại chung'), -- Orange juice, 100%, NFS
    ('61210010', 'Nước ép cam 100%, vắt tươi'), -- Orange juice, 100%,  freshly squeezed
    ('61210220', 'Nước ép cam 100%, đóng hộp, đóng chai hoặc hộp giấy'), -- Orange juice, 100%, canned, bottled or in a carton
    ('61210250', 'Nước ép cam 100%, bổ sung canxi, đóng hộp, đóng chai hoặc hộp giấy'), -- Orange juice, 100%, with calcium added, canned, bottled or in a carton
    ('61210620', 'Nước ép cam 100%, đông lạnh, đã pha'), -- Orange juice, 100%, frozen, reconstituted
    ('61210720', 'Nước ép cam 100%, đông lạnh, chưa pha'), -- Orange juice, 100%, frozen, not reconstituted
    ('61210820', 'Nước ép cam 100%, bổ sung canxi, đông lạnh, đã pha'), -- Orange juice, 100%, with calcium added, frozen, reconstituted
    ('61213220', 'Nước ép quýt 100%'), -- Tangerine juice, 100%
    ('61213800', 'Nước ép hoa quả hỗn hợp họ cam quýt, 100% nước ép'), -- Fruit juice blend, citrus, 100% juice
    ('62101050', 'Hoa quả sấy khô, loại chung'), -- Dried, fruit, NFS
    ('62101100', 'Táo sấy khô'), -- Apple, dried
    ('62104100', 'Mơ sấy khô'), -- Apricot, dried
    ('62105000', 'Việt quất sấy khô'), -- Blueberries, dried
    ('62106000', 'Anh đào sấy khô'), -- Cherries, dried
    ('62107200', 'Chuối sấy giòn (banana chips)'), -- Banana chips
    ('62108100', 'Nho khô loại nhỏ (currant)'), -- Currants, dried
    ('62109100', 'Nam việt quất sấy khô'), -- Cranberries, dried
    ('62110100', 'Chà là'), -- Date
    ('62113100', 'Quả sung sấy khô'), -- Fig, dried
    ('62114050', 'Xoài sấy khô'), -- Mango, dried
    ('62114110', 'Đu đủ sấy khô'), -- Papaya, dried
    ('62116100', 'Đào sấy khô'), -- Peach, dried
    ('62119100', 'Lê sấy khô'), -- Pear, dried
    ('62120000', 'Hồng sấy khô'), -- Persimmon, dried
    ('62120100', 'Dứa sấy khô'), -- Pineapple, dried
    ('62122100', 'Mận khô (prune)'), -- Prune, dried
    ('62125100', 'Nho khô'), -- Raisins
    ('63100100', 'Hoa quả, loại chung'), -- Fruit, NFS
    ('63100110', 'Hoa quả ngâm chua'), -- Fruit, pickled
    ('63101000', 'Táo, tươi'), -- Apple, raw
    ('63101110', 'Sốt táo nghiền, loại thường'), -- Applesauce, regular
    ('63101120', 'Sốt táo nghiền, không đường'), -- Applesauce, unsweetened
    ('63101150', 'Sốt táo nghiền, có hương vị'), -- Applesauce, flavored
    ('63101210', 'Nhân bánh pie táo'), -- Apple pie filling
    ('63101310', 'Táo nướng lò'), -- Apple, baked
    ('63103010', 'Mơ, tươi'), -- Apricot, raw
    ('63103110', 'Mơ, đóng hộp'), -- Apricot, canned
    ('63105010', 'Quả bơ, tươi'), -- Avocado, raw
    ('63107010', 'Chuối, tươi'), -- Banana, raw
    ('63107410', 'Chuối nướng lò'), -- Banana, baked
    ('63109010', 'Dưa lưới (cantaloupe), tươi'), -- Cantaloupe, raw
    ('63109015', 'Dưa (melon), đông lạnh'), -- Melon, frozen
    ('63109700', 'Khế, tươi'), -- Starfruit, raw
    ('63111010', 'Anh đào ngâm maraschino'), -- Cherries, maraschino
    ('63113030', 'Nhân bánh pie anh đào'), -- Cherry pie filling
    ('63115010', 'Anh đào, tươi'), -- Cherries, raw
    ('63115110', 'Anh đào, đóng hộp'), -- Cherries, canned
    ('63115200', 'Anh đào, đông lạnh'), -- Cherries, frozen
    ('63116010', 'Thanh long'), -- Dragon fruit
    ('63119010', 'Quả sung, tươi'), -- Fig, raw
    ('63119110', 'Quả sung, đóng hộp'), -- Fig, canned
    ('63123000', 'Nho, tươi'), -- Grapes, raw
    ('63125010', 'Ổi, tươi'), -- Guava, raw
    ('63126500', 'Kiwi, tươi'), -- Kiwi fruit, raw
    ('63126510', 'Vải'), -- Lychee
    ('63127010', 'Dưa honeydew, tươi'), -- Honeydew melon, raw
    ('63129010', 'Xoài, tươi'), -- Mango, raw
    ('63129030', 'Xoài, đóng hộp'), -- Mango, canned
    ('63129050', 'Xoài, đông lạnh'), -- Mango, frozen
    ('63131010', 'Quả xuân đào (nectarine), tươi'), -- Nectarine, raw
    ('63133010', 'Đu đủ, tươi'), -- Papaya, raw
    ('63133100', 'Đu đủ, đóng hộp'), -- Papaya, canned
    ('63134010', 'Chanh leo, tươi'), -- Passion fruit, raw
    ('63135010', 'Đào, tươi'), -- Peach, raw
    ('63135110', 'Đào, đóng hộp, loại chung'), -- Peach, canned, NFS
    ('63135140', 'Đào, đóng hộp, ngâm siro'), -- Peach, canned, in syrup
    ('63135170', 'Đào, đóng hộp, ngâm nước ép'), -- Peach, canned, juice pack
    ('63135620', 'Đào, đông lạnh'), -- Peach, frozen
    ('63137010', 'Lê, tươi'), -- Pear, raw
    ('63137050', 'Lê châu Á, tươi'), -- Pear, Asian, raw
    ('63137110', 'Lê, đóng hộp, loại chung'), -- Pear, canned, NFS
    ('63137140', 'Lê, đóng hộp, ngâm siro'), -- Pear, canned, in syrup
    ('63137170', 'Lê, đóng hộp, ngâm nước ép'), -- Pear, canned, juice pack
    ('63139010', 'Hồng, tươi'), -- Persimmon, raw
    ('63141010', 'Dứa, tươi'), -- Pineapple, raw
    ('63141110', 'Dứa, đóng hộp, loại chung'), -- Pineapple, canned, NFS
    ('63141140', 'Dứa, đóng hộp, ngâm siro'), -- Pineapple, canned, in syrup
    ('63141170', 'Dứa, đóng hộp, ngâm nước ép'), -- Pineapple, canned, juice pack
    ('63141200', 'Dứa, đông lạnh'), -- Pineapple, frozen
    ('63143010', 'Mận, tươi'), -- Plum, raw
    ('63143110', 'Mận, đóng hộp'), -- Plum, canned
    ('63145010', 'Lựu, tươi'), -- Pomegranate, raw
    ('63147110', 'Đại hoàng (rhubarb)'), -- Rhubarb
    ('63148750', 'Me'), -- Tamarind
    ('63149010', 'Dưa hấu, tươi'), -- Watermelon, raw
    ('63200100', 'Quả mọng, loại chung'), -- Berries, NFS
    ('63200110', 'Quả mọng, đông lạnh'), -- Berries, frozen
    ('63201010', 'Mâm xôi đen, tươi'), -- Blackberries, raw
    ('63201600', 'Mâm xôi đen, đông lạnh'), -- Blackberries, frozen
    ('63203010', 'Việt quất, tươi'), -- Blueberries, raw
    ('63203110', 'Việt quất, đóng hộp'), -- Bluberries, canned
    ('63203600', 'Việt quất, đông lạnh'), -- Blueberries, frozen
    ('63203700', 'Nhân bánh pie việt quất'), -- Blueberry pie filling
    ('63207010', 'Nam việt quất, tươi'), -- Cranberries, raw
    ('63207110', 'Sốt nam việt quất'), -- Cranberry sauce
    ('63219000', 'Mâm xôi, tươi'), -- Raspberries, raw
    ('63219610', 'Mâm xôi, đông lạnh'), -- Raspberries, frozen
    ('63223020', 'Dâu tây, tươi'), -- Strawberries, raw
    ('63223110', 'Dâu tây, đóng hộp'), -- Strawberries, canned
    ('63223610', 'Dâu tây, đông lạnh'), -- Strawberries, frozen
    ('63301010', 'Salad hoa quả ambrosia'), -- Ambrosia
    ('63311000', 'Salad hoa quả tươi hoặc sống, trừ quả họ cam quýt, không sốt trộn'), -- Fruit salad, fresh or raw, excluding citrus fruits, no dressing
    ('63311050', 'Salad hoa quả tươi hoặc sống, gồm quả họ cam quýt, không sốt trộn'), -- Fruit salad, fresh or raw, including citrus fruits, no dressing
    ('63311110', 'Hoa quả trộn, đóng hộp, loại chung'), -- Fruit cocktail, canned, NFS
    ('63311140', 'Hoa quả trộn, đóng hộp, ngâm siro'), -- Fruit cocktail, canned, in syrup
    ('63311170', 'Hoa quả trộn, đóng hộp, ngâm nước ép'), -- Fruit cocktail, canned, juice pack
    ('63311180', 'Hỗn hợp hoa quả, đông lạnh'), -- Fruit mixture, frozen
    ('63401010', 'Salad táo có sốt trộn'), -- Apple salad with dressing
    ('63401060', 'Táo phủ kẹo đường'), -- Apple, candied
    ('63401070', 'Hoa quả phủ sô-cô-la'), -- Fruit, chocolate covered
    ('63402950', 'Salad hoa quả, trừ quả họ cam quýt, với sốt trộn salad hoặc sốt mayonnaise'), -- Fruit salad, excluding citrus fruits, with salad dressing or mayonnaise
    ('63402960', 'Salad hoa quả, trừ quả họ cam quýt, với kem tươi đánh bông'), -- Fruit salad, excluding citrus fruits, with whipped cream
    ('63402970', 'Salad hoa quả, trừ quả họ cam quýt, với kem phủ đánh bông không từ sữa'), -- Fruit salad, excluding citrus fruits, with nondairy whipped topping
    ('63402980', 'Salad hoa quả, trừ quả họ cam quýt, với kẹo dẻo marshmallow'), -- Fruit salad, excluding citrus fruits, with marshmallows
    ('63402990', 'Salad hoa quả, gồm quả họ cam quýt, với bánh pudding'), -- Fruit salad, including citrus fruits, with pudding
    ('63403000', 'Salad hoa quả, trừ quả họ cam quýt, với bánh pudding'), -- Fruit salad, excluding citrus fruits, with pudding
    ('63403010', 'Salad hoa quả, gồm quả họ cam quýt, với sốt trộn salad hoặc sốt mayonnaise'), -- Fruit salad, including citrus fruits, with salad dressing or mayonnaise
    ('63403020', 'Salad hoa quả, gồm quả họ cam quýt, với kem tươi đánh bông'), -- Fruit salad, including citrus fruit, with whipped cream
    ('63403030', 'Salad hoa quả, gồm quả họ cam quýt, với kem phủ đánh bông không từ sữa'), -- Fruit salad, including citrus fruits, with nondairy whipped topping
    ('63403040', 'Salad hoa quả, gồm quả họ cam quýt, với kẹo dẻo marshmallow'), -- Fruit salad, including citrus fruits, with marshmallows
    ('63403150', 'Bánh souffle chanh xanh'), -- Lime souffle
    ('63409010', 'Sốt bơ guacamole, loại chung'), -- Guacamole, NFS
    ('63409015', 'Sốt bơ guacamole với cà chua'), -- Guacamole with tomatoes
    ('63409020', 'Tương chutney'), -- Chutney
    ('63413010', 'Salad dứa có sốt trộn'), -- Pineapple salad with dressing
    ('63415100', 'Súp hoa quả'), -- Soup, fruit
    ('63420105', 'Kem que nước ép hoa quả'), -- Frozen fruit juice bar
    ('63420205', 'Kem que nước ép hoa quả, không thêm đường'), -- Frozen fruit juice bar, no sugar added
    ('63430150', 'Kem sorbet'), -- Sorbet
    ('64100100', 'Nước ép hoa quả, loại chung'), -- Fruit juice, NFS
    ('64100110', 'Nước ép hoa quả hỗn hợp, 100% nước ép'), -- Fruit juice blend, 100% juice
    ('64100200', 'Nước ép nam việt quất hỗn hợp, 100% nước ép'), -- Cranberry juice blend, 100% juice
    ('64100220', 'Nước ép nam việt quất hỗn hợp, 100% nước ép, bổ sung canxi'), -- Cranberry juice blend, 100% juice, with calcium added
    ('64101010', 'Nước táo ép cider'), -- Apple cider
    ('64104010', 'Nước ép táo 100%'), -- Apple juice, 100%
    ('64104030', 'Nước ép táo 100%, bổ sung canxi'), -- Apple juice, 100%, with calcium added
    ('64104600', 'Nước ép mâm xôi đen 100%'), -- Blackberry juice, 100%
    ('64104610', 'Nước ép việt quất'), -- Blueberry juice
    ('64105400', 'Nước ép nam việt quất 100%, không pha trộn'), -- Cranberry juice, 100%, not a blend
    ('64116020', 'Nước ép nho 100%'), -- Grape juice, 100%
    ('64116060', 'Nước ép nho 100%, bổ sung canxi'), -- Grape juice, 100%, with calcium added
    ('64120010', 'Nước ép đu đủ 100%'), -- Papaya juice, 100%
    ('64121000', 'Nước ép chanh leo 100%'), -- Passion fruit juice, 100%
    ('64124020', 'Nước ép dứa 100%'), -- Pineapple juice, 100%
    ('64126000', 'Nước ép lựu 100%'), -- Pomegranate juice, 100%
    ('64132010', 'Nước ép mận khô 100%'), -- Prune juice, 100%
    ('64132500', 'Nước ép dâu tây 100%'), -- Strawberry juice, 100%
    ('64133100', 'Nước ép dưa hấu 100%'), -- Watermelon juice, 100%
    ('64134015', 'Sinh tố hoa quả, từ nguyên quả, không có sữa'), -- Fruit smoothie, with whole fruit, no dairy
    ('64134020', 'Sinh tố hoa quả, từ nguyên quả, không có sữa, bổ sung đạm (protein)'), -- Fruit smoothie, with whole fruit, no dairy, added protein
    ('64134025', 'Sinh tố hoa quả, từ nguyên quả, với sữa thực vật (non-dairy)'), -- Fruit smoothie, with whole fruit, non-dairy
    ('64134030', 'Đồ uống sinh tố nước ép hoa quả, không có sữa'), -- Fruit smoothie juice drink, no dairy
    ('64134100', 'Sinh tố hoa quả, loại nhẹ'), -- Fruit smoothie, light
    ('64134200', 'Sinh tố hoa quả, đóng chai'), -- Fruit smoothie, bottled
    ('64200100', 'Nước ép nectar hoa quả, loại chung'), -- Fruit nectar, NFS
    ('64201010', 'Nước ép nectar mơ'), -- Apricot nectar
    ('64201500', 'Nước ép nectar chuối'), -- Banana nectar
    ('64202010', 'Nước ép nectar dưa lưới (cantaloupe)'), -- Cantaloupe nectar
    ('64203020', 'Nước ép nectar ổi'), -- Guava nectar
    ('64204010', 'Nước ép nectar xoài'), -- Mango nectar
    ('64205010', 'Nước ép nectar đào'), -- Peach nectar
    ('64210010', 'Nước ép nectar đu đủ'), -- Papaya nectar
    ('64213010', 'Nước ép nectar chanh leo'), -- Passion fruit nectar
    ('64215010', 'Nước ép nectar lê'), -- Pear nectar
    ('64221010', 'Nước ép nectar mãng cầu xiêm'), -- Soursop, nectar
    ('64401000', 'Giấm'), -- Vinegar
    ('67100000', 'Thức ăn cho trẻ nhỏ, hoa quả, loại chung'), -- Baby Toddler fruit, NFS
    ('67100100', 'Thức ăn cho trẻ nhỏ, nhiều loại hoa quả, giai đoạn 2'), -- Baby Toddler multiple fruit, Stage 2
    ('67100105', 'Thức ăn cho trẻ nhỏ, nhiều loại hoa quả, giai đoạn 3'), -- Baby Toddler multiple fruit, Stage 3
    ('67100250', 'Thức ăn cho trẻ nhỏ, hoa quả với ngũ cốc'), -- Baby Toddler fruit, with grain
    ('67100260', 'Thức ăn cho trẻ nhỏ, hoa quả với sữa chua'), -- Baby Toddler fruit, with yogurt
    ('67100350', 'Thức ăn cho trẻ nhỏ, hoa quả và rau, giai đoạn 2'), -- Baby Toddler fruit and vegetables, Stage 2
    ('67100360', 'Thức ăn cho trẻ nhỏ, hoa quả và rau, giai đoạn 3'), -- Baby Toddler fruit and vegetables, Stage 3
    ('67100450', 'Thức ăn cho trẻ nhỏ, hoa quả và rau với ngũ cốc'), -- Baby Toddler fruit and vegetables, with grain
    ('67100460', 'Thức ăn cho trẻ nhỏ, hoa quả và rau với sữa chua'), -- Baby Toddler fruit and vegetables, with yogurt
    ('67100470', 'Thức ăn cho trẻ nhỏ, hoa quả, rau và thịt'), -- Baby Toddler fruit, vegetables, and meat
    ('67100480', 'Thức ăn cho trẻ nhỏ, hoa quả và thịt'), -- Baby Toddler fruit and meat
    ('67102010', 'Thức ăn cho trẻ nhỏ, táo, giai đoạn 1'), -- Baby Toddler apples, Stage 1
    ('67102030', 'Thức ăn cho trẻ nhỏ, táo, giai đoạn 2'), -- Baby Toddler apples, Stage 2
    ('67105030', 'Thức ăn cho trẻ nhỏ, chuối, giai đoạn 1'), -- Baby Toddler bananas, Stage 1
    ('67105040', 'Thức ăn cho trẻ nhỏ, chuối, giai đoạn 2'), -- Baby Toddler bananas, Stage 2
    ('67108010', 'Thức ăn cho trẻ nhỏ, đào, giai đoạn 1'), -- Baby Toddler peaches, Stage 1
    ('67108040', 'Thức ăn cho trẻ nhỏ, đào, giai đoạn 2'), -- Baby Toddler peaches, Stage 2
    ('67109010', 'Thức ăn cho trẻ nhỏ, lê, giai đoạn 1'), -- Baby Toddler pears, Stage 1
    ('67109040', 'Thức ăn cho trẻ nhỏ, lê, giai đoạn 2'), -- Baby Toddler pears, Stage 2
    ('67110000', 'Thức ăn cho trẻ nhỏ, mận khô'), -- Baby Toddler prunes
    ('67110100', 'Thức ăn cho trẻ nhỏ, xoài'), -- Baby Toddler mangoes
    ('67201000', 'Thức ăn cho trẻ nhỏ, nước ép, loại chung'), -- Baby Toddler juice, NFS
    ('67202000', 'Thức ăn cho trẻ nhỏ, nước ép táo'), -- Baby Toddler juice, apple
    ('67203800', 'Thức ăn cho trẻ nhỏ, nước ép nho'), -- Baby Toddler juice, grape
    ('67204000', 'Thức ăn cho trẻ nhỏ, nước ép hoa quả hỗn hợp'), -- Baby Toddler juice, mixed fruit
    ('67212000', 'Thức ăn cho trẻ nhỏ, nước ép lê'), -- Baby Toddler juice, pear
    ('67230600', 'Thức ăn cho trẻ nhỏ, nước ép hoa quả và rau'), -- Baby Toddler juice, fruit and vegetable
    ('67250150', 'Thức ăn cho trẻ nhỏ, nước ép hoa quả pha sữa chua'), -- Baby Toddler juice, fruit and yogurt blend
    ('67408010', 'Thức ăn cho trẻ nhỏ, bánh pudding'), -- Baby Toddler pudding
    ('67430500', 'Thức ăn cho trẻ nhỏ, sữa chua sấy tan (yogurt melts)'), -- Baby Toddler yogurt melts
    ('71000100', 'Khoai tây, loại chung'), -- Potato, NFS
    ('71100100', 'Khoai tây nướng lò, loại chung') -- Potato, baked, NFS
) AS t (source_food_code, name_vi)
WHERE f.source = 'USDA_FNDDS' AND f.source_food_code = t.source_food_code;

UPDATE nutrition_foods f SET name_vi = t.name_vi, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:usda-name-vi-v25'
FROM (VALUES
    ('71101000', 'Khoai tây nướng lò, bỏ vỏ'), -- Potato, baked, peel not eaten
    ('71102980', 'Khoai tây luộc, loại chung'), -- Potato, boiled, NFS
    ('71102990', 'Khoai tây luộc, chỉ cần hâm nóng'), -- Potato, boiled, ready-to-heat
    ('71103000', 'Khoai tây luộc, từ loại tươi, bỏ vỏ, không rõ có thêm chất béo'), -- Potato, boiled, from fresh, peel not eaten, NS as to fat
    ('71103010', 'Khoai tây luộc, từ loại tươi, bỏ vỏ, không thêm chất béo'), -- Potato, boiled, from fresh, peel not eaten, no added fat
    ('71103020', 'Khoai tây luộc, từ loại tươi, bỏ vỏ, có thêm chất béo, không rõ loại chất béo'), -- Potato, boiled, from fresh, peel not eaten, fat added, NS as to fat type
    ('71103030', 'Khoai tây luộc, từ loại tươi, bỏ vỏ, nấu với dầu'), -- Potato, boiled, from fresh, peel not eaten, made with oil
    ('71103040', 'Khoai tây luộc, từ loại tươi, bỏ vỏ, nấu với bơ'), -- Potato, boiled, from fresh, peel not eaten, made with butter
    ('71103050', 'Khoai tây luộc, từ loại tươi, bỏ vỏ, nấu với bơ thực vật'), -- Potato, boiled, from fresh, peel not eaten, made with margarine
    ('71103105', 'Khoai tây luộc, từ loại tươi, ăn cả vỏ, không rõ có thêm chất béo'), -- Potato, boiled, from fresh, peel eaten, NS as to fat
    ('71103115', 'Khoai tây luộc, từ loại tươi, ăn cả vỏ, có thêm chất béo, không rõ loại chất béo'), -- Potato, boiled, from fresh, peel eaten, fat added, NS as to fat type
    ('71103125', 'Khoai tây luộc, từ loại tươi, ăn cả vỏ, không thêm chất béo'), -- Potato, boiled, from fresh,  peel eaten, no added fat
    ('71103135', 'Khoai tây luộc, từ loại tươi, ăn cả vỏ, nấu với dầu'), -- Potato, boiled, from fresh, peel eaten, made with oil
    ('71103140', 'Khoai tây luộc, từ loại tươi, ăn cả vỏ, nấu với bơ'), -- Potato, boiled, from fresh, peel eaten, made with butter
    ('71103150', 'Khoai tây luộc, từ loại tươi, ăn cả vỏ, nấu với bơ thực vật'), -- Potato, boiled, from fresh, peel eaten, made with margarine
    ('71103300', 'Khoai tây đóng hộp, không rõ có thêm chất béo'), -- Potato, canned, NS as to fat
    ('71103310', 'Khoai tây đóng hộp, có thêm chất béo, không rõ loại chất béo'), -- Potato, canned, fat added, NS as to fat type
    ('71103320', 'Khoai tây đóng hộp, không thêm chất béo'), -- Potato, canned, no added fat
    ('71104030', 'Khoai tây quay, loại chung'), -- Potato, roasted, NFS
    ('71104040', 'Khoai tây quay, từ loại tươi, ăn cả vỏ, không rõ có thêm chất béo'), -- Potato, roasted, from fresh, peel eaten, NS as to fat
    ('71104050', 'Khoai tây quay, từ loại tươi, ăn cả vỏ, không thêm chất béo'), -- Potato, roasted, from fresh, peel eaten, no added fat
    ('71104060', 'Khoai tây quay, từ loại tươi, ăn cả vỏ, có thêm chất béo, không rõ loại chất béo'), -- Potato, roasted, from fresh, peel eaten, fat added, NS as to fat type
    ('71104070', 'Khoai tây quay, từ loại tươi, ăn cả vỏ, nấu với dầu'), -- Potato, roasted, from fresh, peel eaten, made with oil
    ('71104080', 'Khoai tây quay, từ loại tươi, ăn cả vỏ, nấu với bơ'), -- Potato, roasted, from fresh, peel eaten, made with butter
    ('71104090', 'Khoai tây quay, từ loại tươi, ăn cả vỏ, nấu với bơ thực vật'), -- Potato, roasted, from fresh, peel eaten, made with margarine
    ('71104100', 'Khoai tây quay, từ loại tươi, bỏ vỏ, không rõ có thêm chất béo'), -- Potato, roasted, from fresh, peel not eaten, NS as to fat
    ('71104110', 'Khoai tây quay, từ loại tươi, bỏ vỏ, có thêm chất béo, không rõ loại chất béo'), -- Potato, roasted, from fresh, peel not eaten, fat added, NS as to fat type
    ('71104120', 'Khoai tây quay, từ loại tươi, bỏ vỏ, không thêm chất béo'), -- Potato, roasted, from fresh, peel not eaten, no added fat
    ('71104130', 'Khoai tây quay, từ loại tươi, bỏ vỏ, nấu với dầu'), -- Potato, roasted, from fresh, peel not eaten, made with oil
    ('71104140', 'Khoai tây quay, từ loại tươi, bỏ vỏ, nấu với bơ'), -- Potato, roasted, from fresh, peel not eaten, made with butter
    ('71104150', 'Khoai tây quay, từ loại tươi, bỏ vỏ, nấu với bơ thực vật'), -- Potato, roasted, from fresh, peel not eaten, made with margarine
    ('71104200', 'Khoai tây quay, chỉ cần hâm nóng'), -- Potato, roasted, ready-to-heat
    ('71106000', 'Khoai tây hầm kiểu Puerto Rico'), -- Stewed potatoes, Puerto Rican style
    ('71106010', 'Phần khoai tây trong món hỗn hợp kiểu Puerto Rico, nước sốt gravy và thành phần khác tính riêng'), -- Potato only from Puerto Rican mixed dishes, gravy and other components reported separately
    ('71106020', 'Khoai tây trong món thịt bò nhồi om kiểu Puerto Rico, có nước sốt gravy'), -- Potato from Puerto Rican style stuffed pot roast, with gravy
    ('71106050', 'Khoai tây trong món bò hầm kiểu Puerto Rico, có nước sốt gravy'), -- Potato from Puerto Rican beef stew, with gravy
    ('71106070', 'Khoai tây trong món gà fricassee kiểu Puerto Rico, có nước sốt'), -- Potato from Puerto Rican chicken fricassee, with sauce
    ('71200010', 'Khoai tây chiên lát, loại chung'), -- Potato chips, NFS
    ('71200100', 'Khoai tây chiên lát, không hương vị'), -- Potato chips, plain
    ('71200110', 'Khoai tây chiên lát, vị BBQ'), -- Potato chips, barbecue flavored
    ('71200120', 'Khoai tây chiên lát, vị kem chua và hành'), -- Potato chips, sour cream and onion flavored
    ('71200130', 'Khoai tây chiên lát, vị phô mai'), -- Potato chips, cheese flavored
    ('71200140', 'Khoai tây chiên lát, vị khác'), -- Potato chips, other flavored
    ('71200200', 'Khoai tây chiên lát gợn sóng, không hương vị'), -- Potato chips, ruffled, plain
    ('71200210', 'Khoai tây chiên lát gợn sóng, vị BBQ'), -- Potato chips, ruffled, barbecue flavored
    ('71200220', 'Khoai tây chiên lát gợn sóng, vị kem chua và hành'), -- Potato chips, ruffled, sour cream and onion flavored
    ('71200230', 'Khoai tây chiên lát gợn sóng, vị phô mai'), -- Potato chips, ruffled, cheese flavored
    ('71200240', 'Khoai tây chiên lát gợn sóng, vị khác'), -- Potato chips, ruffled, other flavored
    ('71200300', 'Khoai tây chiên lát ép định hình, không hương vị'), -- Potato chips, restructured, plain
    ('71200310', 'Khoai tây chiên lát ép định hình, có hương vị'), -- Potato chips, restructured, flavored
    ('71200400', 'Khoai tây chiên lát loại nướng lò, không hương vị'), -- Potato chips, baked, plain
    ('71200410', 'Khoai tây chiên lát loại nướng lò, có hương vị'), -- Potato chips, baked, flavored
    ('71201050', 'Khoai tây chiên lát, giảm béo'), -- Potato chips, reduced fat
    ('71201200', 'Khoai tây chiên lát ép định hình, giảm béo, rắc ít muối'), -- Potato chips, restructured, reduced fat, lightly salted
    ('71202000', 'Khoai tây chiên lát, không muối'), -- Potato chips, unsalted
    ('71202500', 'Khoai tây chiên lát, rắc ít muối'), -- Potato chips, lightly salted
    ('71202510', 'Khoai tây chiên lát ép định hình, rắc ít muối'), -- Potato chips, restructured, lightly salted
    ('71203010', 'Khoai tây chiên lát loại nổ phồng (popped), không hương vị'), -- Potato chips, popped, plain
    ('71203020', 'Khoai tây chiên lát loại nổ phồng (popped), có hương vị'), -- Potato chips, popped, flavored
    ('71203030', 'Khoai tây chiên lát loại nổ phồng (popped), loại chung'), -- Potato chips, popped, NFS
    ('71205020', 'Khoai tây que giòn, không hương vị'), -- Potato sticks, plain
    ('71205030', 'Khoai tây que giòn, có hương vị'), -- Potato sticks, flavored
    ('71205040', 'Khoai tây que giòn, dạng thanh như khoai tây chiên'), -- Potato sticks, fry shaped
    ('71220000', 'Rau củ chiên lát (snack)'), -- Vegetable chips
    ('71305015', 'Khoai tây đút lò sốt kem (scalloped), loại chung'), -- Potato, scalloped, NFS
    ('71305020', 'Khoai tây đút lò sốt kem (scalloped), đồ ăn nhanh hoặc nhà hàng'), -- Potato, scalloped, from fast food or restaurant
    ('71305030', 'Khoai tây đút lò sốt kem (scalloped), từ loại tươi'), -- Potato, scalloped, from fresh
    ('71305040', 'Khoai tây đút lò sốt kem (scalloped), từ loại tươi, có thịt'), -- Potato, scalloped, from fresh, with meat
    ('71305050', 'Khoai tây đút lò sốt kem (scalloped), từ bột pha sẵn'), -- Potato, scalloped, from dry mix
    ('71305060', 'Khoai tây đút lò sốt kem (scalloped), từ bột pha sẵn, có thịt'), -- Potato, scalloped, from dry mix, with meat
    ('71305070', 'Khoai tây đút lò sốt kem (scalloped), chỉ cần hâm nóng'), -- Potato, scalloped, ready-to-heat
    ('71305080', 'Khoai tây đút lò sốt kem (scalloped), chỉ cần hâm nóng, có thịt'), -- Potato, scalloped, ready-to-heat, with meat
    ('71400990', 'Khoai tây chiên, loại chung'), -- Potato, french fries, NFS
    ('71401000', 'Khoai tây chiên, không rõ tươi hay đông lạnh'), -- Potato, french fries, NS as to fresh or frozen
    ('71401010', 'Khoai tây chiên, từ loại tươi, chiên dầu'), -- Potato, french fries, from fresh, fried
    ('71401015', 'Khoai tây chiên, từ loại tươi, nướng lò'), -- Potato, french fries, from fresh, baked
    ('71401020', 'Khoai tây chiên, từ loại đông lạnh, nướng lò'), -- Potato, french fries, from frozen, baked
    ('71401030', 'Khoai tây chiên, đồ ăn nhanh'), -- Potato, french fries, fast food
    ('71401031', 'Khoai tây chiên, nhà hàng'), -- Potato, french fries, restaurant
    ('71401032', 'Khoai tây chiên, từ loại đông lạnh, chiên dầu'), -- Potato, french fries, from frozen, fried
    ('71401033', 'Khoai tây chiên, trường học'), -- Potato, french fries, school
    ('71401039', 'Khoai tây chiên phủ phô mai, đồ ăn nhanh/nhà hàng'), -- Potato, french fries, with cheese, fast food / restaurant
    ('71401041', 'Khoai tây chiên phủ phô mai, trường học'), -- Potato, french fries, with cheese, school
    ('71401045', 'Khoai tây chiên phủ món chili, đồ ăn nhanh/nhà hàng'), -- Potato, french fries, with chili, fast food / restaurant
    ('71401050', 'Khoai tây chiên phủ món chili và phô mai, đồ ăn nhanh/nhà hàng'), -- Potato, french fries, with chili and cheese, fast food / restaurant
    ('71402500', 'Khoai tây chiên phủ phô mai'), -- Potato, french fries, with cheese
    ('71402510', 'Khoai tây chiên phủ món chili và phô mai'), -- Potato, french fries, with chili and cheese
    ('71402520', 'Khoai tây chiên phủ món chili'), -- Potato, french fries, with chili
    ('71403020', 'Khoai tây áp chảo (home fries), loại chung'), -- Potato, home fries, NFS
    ('71403030', 'Khoai tây áp chảo (home fries), nhà hàng/đồ ăn nhanh'), -- Potato, home fries, from restaurant / fast food
    ('71403040', 'Khoai tây áp chảo (home fries), từ loại tươi'), -- Potato, home fries, from fresh
    ('71403050', 'Khoai tây áp chảo (home fries), chỉ cần hâm nóng'), -- Potato, home fries, ready-to-heat
    ('71403500', 'Khoai tây áp chảo (home fries), có rau củ'), -- Potato, home fries, with vegetables
    ('71404000', 'Bánh khoai tây bào chiên (hash brown), loại chung'), -- Potato, hash brown, NFS
    ('71404010', 'Bánh khoai tây bào chiên (hash brown), đồ ăn nhanh'), -- Potato, hash brown, from fast food
    ('71404020', 'Bánh khoai tây bào chiên (hash brown), đồ ăn nhanh, có phô mai'), -- Potato, hash brown, from fast food, with cheese
    ('71404030', 'Bánh khoai tây bào chiên (hash brown), nhà hàng'), -- Potato, hash brown, from restaurant
    ('71404040', 'Bánh khoai tây bào chiên (hash brown), nhà hàng, có phô mai'), -- Potato, hash brown, from restaurant, with cheese
    ('71404050', 'Bánh khoai tây bào chiên (hash brown), bữa trưa trường học'), -- Potato, hash brown, from school lunch
    ('71405010', 'Bánh khoai tây bào chiên (hash brown), từ loại tươi'), -- Potato, hash brown, from fresh
    ('71405019', 'Bánh khoai tây bào chiên (hash brown), từ loại tươi, có phô mai'), -- Potato, hash brown, from fresh, with cheese
    ('71405030', 'Bánh khoai tây bào chiên (hash brown), từ bột pha sẵn'), -- Potato, hash brown, from dry mix
    ('71405040', 'Bánh khoai tây bào chiên (hash brown), chỉ cần hâm nóng'), -- Potato, hash brown, ready-to-heat
    ('71405050', 'Bánh khoai tây bào chiên (hash brown), chỉ cần hâm nóng, có phô mai'), -- Potato, hash brown, ready-to-heat, with cheese
    ('71410000', 'Vỏ khoai tây nướng (potato skins), không phủ nhân'), -- Potato skins without topping
    ('71410500', 'Vỏ khoai tây nướng (potato skins), phủ phô mai'), -- Potato skins, with cheese
    ('71411000', 'Vỏ khoai tây nướng (potato skins), phủ phô mai và thịt xông khói'), -- Potato skins, with cheese and bacon
    ('71411100', 'Vỏ khoai tây nướng (potato skins), loại chung'), -- Potato skins, NFS
    ('71501000', 'Khoai tây nghiền, loại chung'), -- Potato, mashed, NFS
    ('71501005', 'Khoai tây nghiền, đồ ăn nhanh'), -- Potato, mashed, from fast food
    ('71501006', 'Khoai tây nghiền, đồ ăn nhanh, có nước sốt gravy'), -- Potato, mashed, from fast food, with gravy
    ('71501007', 'Khoai tây nghiền, chỉ cần hâm nóng'), -- Potato, mashed, ready-to-heat
    ('71501010', 'Khoai tây nghiền, từ loại tươi, làm với sữa'), -- Potato, mashed, from fresh, made with milk
    ('71501011', 'Khoai tây nghiền, từ loại tươi, làm với sữa, có phô mai'), -- Potato, mashed, from fresh, made with milk, with cheese
    ('71501012', 'Khoai tây nghiền, từ loại tươi, làm với sữa, có nước sốt gravy'), -- Potato, mashed, from fresh, made with milk, with gravy
    ('71501013', 'Khoai tây nghiền, từ loại tươi, loại chung'), -- Potato, mashed, from fresh, NFS
    ('71501016', 'Khoai tây nghiền, nhà hàng'), -- Potato, mashed, from restaurant
    ('71501017', 'Khoai tây nghiền, nhà hàng, có nước sốt gravy'), -- Potato, mashed, from restaurant, with gravy
    ('71501018', 'Khoai tây nghiền, bữa trưa trường học'), -- Potato, mashed, from school lunch
    ('71501035', 'Khoai tây nghiền, từ bột pha sẵn, loại chung'), -- Potato, mashed, from dry mix, NFS
    ('71501040', 'Khoai tây nghiền, từ bột pha sẵn, làm với sữa'), -- Potato, mashed, from dry mix, made with milk
    ('71501045', 'Khoai tây nghiền, từ bột pha sẵn, làm với sữa, có phô mai'), -- Potato, mashed, from dry mix, made with milk, with cheese
    ('71501054', 'Khoai tây nghiền, từ bột pha sẵn, làm với sữa, có nước sốt gravy'), -- Potato, mashed, from dry mix, made with milk, with gravy
    ('71501061', 'Khoai tây nghiền, chỉ cần hâm nóng, loại chung'), -- Potato, mashed, ready-to-heat, NFS
    ('71501071', 'Khoai tây nghiền, chỉ cần hâm nóng, có phô mai'), -- Potato, mashed, ready-to-heat, with cheese
    ('71501075', 'Khoai tây nghiền, chỉ cần hâm nóng, có nước sốt gravy'), -- Potato, mashed, ready-to-heat, with gravy
    ('71503010', 'Miếng chả khoai tây (patty)'), -- Potato patty
    ('71505000', 'Khoai tây viên chiên (tots), loại chung'), -- Potato tots, NFS
    ('71505010', 'Khoai tây viên chiên (tots), đồ ăn nhanh/nhà hàng'), -- Potato tots, fast food / restaurant
    ('71505020', 'Khoai tây viên chiên (tots), trường học'), -- Potato tots, school
    ('71505030', 'Khoai tây viên chiên (tots), từ loại tươi, chiên dầu hoặc nướng lò'), -- Potato tots, from fresh, fried or baked
    ('71505040', 'Khoai tây viên chiên (tots), đông lạnh, nướng lò'), -- Potato tots, frozen, baked
    ('71505050', 'Khoai tây viên chiên (tots), đông lạnh, chiên dầu'), -- Potato tots, frozen, fried
    ('71505060', 'Khoai tây viên chiên (tots), đông lạnh, không rõ chiên dầu hay nướng lò'), -- Potato tots, frozen, NS as to fried or baked
    ('71507005', 'Khoai tây nướng lò, bỏ vỏ, với bơ'), -- Potato, baked, peel not eaten, with butter
    ('71507010', 'Khoai tây nướng lò, bỏ vỏ, với kem chua'), -- Potato, baked, peel not eaten, with sour cream
    ('71507020', 'Khoai tây nướng lò, bỏ vỏ, với phô mai'), -- Potato, baked, peel not eaten, with cheese
    ('71507025', 'Khoai tây nướng lò, bỏ vỏ, với thịt'), -- Potato, baked, peel not eaten, with meat
    ('71507030', 'Khoai tây nướng lò, bỏ vỏ, với món chili'), -- Potato, baked, peel not eaten, with chili
    ('71507035', 'Khoai tây nướng lò, bỏ vỏ, với rau củ'), -- Potato, baked, peel not eaten, with vegetables
    ('71508001', 'Khoai tây nướng lò, ăn cả vỏ'), -- Potato, baked, peel eaten
    ('71508005', 'Khoai tây nướng lò, ăn cả vỏ, với bơ'), -- Potato, baked, peel eaten, with butter
    ('71508010', 'Khoai tây nướng lò, ăn cả vỏ, với kem chua'), -- Potato, baked, peel eaten, with sour cream
    ('71508020', 'Khoai tây nướng lò, ăn cả vỏ, với phô mai'), -- Potato, baked, peel eaten, with cheese
    ('71508025', 'Khoai tây nướng lò, ăn cả vỏ, với thịt'), -- Potato, baked, peel eaten, with meat
    ('71508030', 'Khoai tây nướng lò, ăn cả vỏ, với món chili'), -- Potato, baked, peel eaten, with chili
    ('71508035', 'Khoai tây nướng lò, ăn cả vỏ, với rau củ'), -- Potato, baked, peel eaten, with vegetables
    ('71600950', 'Salad khoai tây có trứng, nhà hàng'), -- Potato salad with egg, from restaurant
    ('71601010', 'Salad khoai tây có trứng, làm với sốt mayonnaise'), -- Potato salad with egg, made with mayonnaise
    ('71601015', 'Salad khoai tây có trứng, làm với sốt mayonnaise loại nhẹ'), -- Potato salad with egg, made with light mayonnaise
    ('71601020', 'Salad khoai tây có trứng, làm với sốt trộn salad kiểu mayonnaise'), -- Potato salad with egg, made with mayonnaise-type salad dressing
    ('71601025', 'Salad khoai tây có trứng, làm với sốt trộn salad kiểu mayonnaise loại nhẹ'), -- Potato salad with egg, made with light mayonnaise-type salad dressing
    ('71601030', 'Salad khoai tây có trứng, làm với sốt trộn dạng kem'), -- Potato salad with egg, made with creamy dressing
    ('71601035', 'Salad khoai tây có trứng, làm với sốt trộn dạng kem loại nhẹ'), -- Potato salad with egg, made with light creamy dressing
    ('71601040', 'Salad khoai tây có trứng, làm với sốt trộn kiểu Ý'), -- Potato salad with egg, made with Italian dressing
    ('71601045', 'Salad khoai tây có trứng, làm với sốt trộn kiểu Ý loại nhẹ'), -- Potato salad with egg, made with light Italian dressing
    ('71601050', 'Salad khoai tây có trứng, làm với sốt trộn không béo (mọi loại)'), -- Potato salad with egg, made with any type of fat free dressing
    ('71602010', 'Salad khoai tây kiểu Đức'), -- Potato salad, German style
    ('71602950', 'Salad khoai tây, nhà hàng'), -- Potato salad, from restaurant
    ('71603010', 'Salad khoai tây, làm với sốt mayonnaise'), -- Potato salad, made with mayonnaise
    ('71603015', 'Salad khoai tây, làm với sốt mayonnaise loại nhẹ'), -- Potato salad, made with light mayonnaise
    ('71603020', 'Salad khoai tây, làm với sốt trộn salad kiểu mayonnaise'), -- Potato salad, made with mayonnaise-type salad dressing
    ('71603025', 'Salad khoai tây, làm với sốt trộn salad kiểu mayonnaise loại nhẹ'), -- Potato salad, made with light mayonnaise-type salad dressing
    ('71603030', 'Salad khoai tây, làm với sốt trộn dạng kem'), -- Potato salad, made with creamy dressing
    ('71603035', 'Salad khoai tây, làm với sốt trộn dạng kem loại nhẹ'), -- Potato salad, made with light creamy dressing
    ('71603040', 'Salad khoai tây, làm với sốt trộn kiểu Ý'), -- Potato salad, made with Italian dressing
    ('71603045', 'Salad khoai tây, làm với sốt trộn kiểu Ý loại nhẹ'), -- Potato salad, made with light Italian dressing
    ('71603050', 'Salad khoai tây, làm với sốt trộn không béo (mọi loại)'), -- Potato salad, made with any type of fat free dressing
    ('71701000', 'Bánh kếp khoai tây (pancake)'), -- Potato pancake
    ('71701500', 'Bánh lefse (bánh khoai tây Na Uy)'), -- Lefse
    ('71703990', 'Khoai tây hầm'), -- Stewed potatoes
    ('71704000', 'Khoai tây hầm cà chua'), -- Stewed potatoes with tomatoes
    ('71801000', 'Súp khoai tây'), -- Soup, potato
    ('71801005', 'Súp khoai tây có thịt'), -- Soup, potato with meat
    ('71900100', 'Chuối lá, nấu chín, không thêm chất béo'), -- Plantain, cooked, no added fat
    ('71900200', 'Chuối lá, nấu với dầu'), -- Plantain, cooked with oil
    ('71905000', 'Chuối lá, sống'), -- Plantain, raw
    ('71905008', 'Chuối lá, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Plantain, cooked, fat added, NS as to fat type
    ('71905100', 'Chuối lá, nấu với bơ hoặc bơ thực vật'), -- Plantain, cooked with butter or margarine
    ('71905410', 'Chuối lá chiên lát (snack)'), -- Plantain chips
    ('71930120', 'Sắn, nấu chín'), -- Cassava, cooked
    ('71930190', 'Sắn chiên que (yuca fries)'), -- Yuca fries
    ('71930200', 'Bánh casabe, bánh làm từ sắn'), -- Casabe, cassava bread
    ('71962040', 'Khoai môn, nấu chín'), -- Taro, cooked
    ('71970200', 'Món fufu (bột củ nghiền, châu Phi)'), -- Fufu
    ('71980200', 'Khoai môn chiên lát (snack)'), -- Taro chips
    ('72101100', 'Lá củ dền, sống'), -- Beet greens, raw
    ('72101220', 'Lá củ dền, nấu chín'), -- Beet greens, cooked
    ('72103000', 'Cải rapini (broccoli raab), sống'), -- Broccoli raab, raw
    ('72103030', 'Cải rapini (broccoli raab), nấu chín'), -- Broccoli raab, cooked
    ('72104100', 'Cải cầu vồng (chard), sống'), -- Chard, raw
    ('72104220', 'Cải cầu vồng (chard), nấu chín'), -- Chard, cooked
    ('72107100', 'Cải xoăn collard, sống'), -- Collards, raw
    ('72107211', 'Cải xoăn collard, tươi, nấu chín, không thêm chất béo'), -- Collards, fresh, cooked, no added fat
    ('72107212', 'Cải xoăn collard, đông lạnh, nấu chín, không thêm chất béo'), -- Collards, frozen, cooked, no added fat
    ('72107220', 'Cải xoăn collard, không rõ dạng, nấu chín'), -- Collards, NS as to form, cooked
    ('72107221', 'Cải xoăn collard, tươi, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Collards, fresh, cooked, fat added, NS as to fat type
    ('72107222', 'Cải xoăn collard, đông lạnh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Collards, frozen, cooked, fat added, NS as to fat type
    ('72107227', 'Cải xoăn collard, tươi, nấu với dầu'), -- Collards, fresh, cooked with oil
    ('72107228', 'Cải xoăn collard, tươi, nấu với bơ hoặc bơ thực vật'), -- Collards, fresh, cooked with butter or margarine
    ('72107230', 'Cải xoăn collard, đông lạnh, nấu với dầu'), -- Collards, frozen, cooked with oil
    ('72107231', 'Cải xoăn collard, đông lạnh, nấu với bơ hoặc bơ thực vật'), -- Collards, frozen, cooked with butter or margarine
    ('72110100', 'Cải xoong cạn (cress), sống'), -- Cress, raw
    ('72110221', 'Cải xoong cạn (cress), nấu chín'), -- Cress, cooked
    ('72113100', 'Lá bồ công anh, sống'), -- Dandelion greens, raw
    ('72113220', 'Lá bồ công anh, nấu chín'), -- Dandelion greens, cooked
    ('72116000', 'Xà lách romaine, sống'), -- Romaine lettuce, raw
    ('72116150', 'Salad Caesar với xà lách romaine, không sốt trộn'), -- Caesar salad, with romaine, no dressing
    ('72116220', 'Rau diếp xoăn escarole, nấu chín'), -- Escarole, cooked
    ('72118211', 'Rau lá xanh, tươi, nấu chín, không thêm chất béo'), -- Greens, fresh, cooked, no added fat
    ('72118212', 'Rau lá xanh, đông lạnh, nấu chín, không thêm chất béo'), -- Greens, frozen, cooked, no added fat
    ('72118220', 'Rau lá xanh, không rõ dạng, nấu chín'), -- Greens, NS as to form, cooked
    ('72118221', 'Rau lá xanh, tươi, nấu chín, có thêm chất béo'), -- Greens, fresh, cooked, fat added
    ('72118222', 'Rau lá xanh, đông lạnh, nấu chín, có thêm chất béo'), -- Greens, frozen, cooked, fat added
    ('72118223', 'Rau lá xanh, đóng hộp, nấu chín'), -- Greens, canned, cooked
    ('72119190', 'Cải xoăn kale, sống'), -- Kale, raw
    ('72119211', 'Cải xoăn kale, tươi, nấu chín, không thêm chất béo'), -- Kale, fresh, cooked, no added fat
    ('72119212', 'Cải xoăn kale, đông lạnh, nấu chín, không thêm chất béo'), -- Kale, frozen, cooked, no added fat
    ('72119220', 'Cải xoăn kale, không rõ dạng, nấu chín'), -- Kale, NS as to form, cooked
    ('72119221', 'Cải xoăn kale, tươi, nấu chín, có thêm chất béo'), -- Kale, fresh, cooked, fat added
    ('72119222', 'Cải xoăn kale, đông lạnh, nấu chín, có thêm chất béo'), -- Kale, frozen, cooked, fat added
    ('72120220', 'Rau muối (lambsquarter), nấu chín'), -- Lambsquarter, cooked
    ('72122100', 'Cải bẹ xanh (mustard greens), sống'), -- Mustard greens, raw
    ('72122211', 'Cải bẹ xanh (mustard greens), tươi, nấu chín, không thêm chất béo'), -- Mustard greens, fresh, cooked, no added fat
    ('72122212', 'Cải bẹ xanh (mustard greens), đông lạnh, nấu chín, không thêm chất béo'), -- Mustard greens, frozen, cooked, no added fat
    ('72122220', 'Cải bẹ xanh (mustard greens), không rõ dạng, nấu chín'), -- Mustard greens, NS as to form, cooked
    ('72122221', 'Cải bẹ xanh (mustard greens), tươi, nấu chín, có thêm chất béo'), -- Mustard greens, fresh, cooked, fat added
    ('72122222', 'Cải bẹ xanh (mustard greens), đông lạnh, nấu chín, có thêm chất béo'), -- Mustard greens, frozen, cooked, fat added
    ('72123020', 'Rau thương lục (poke greens), nấu chín'), -- Poke greens, cooked
    ('72124100', 'Diếp xoăn đỏ (radicchio), sống'), -- Radicchio, raw
    ('72125100', 'Rau chân vịt, sống'), -- Spinach, raw
    ('72125211', 'Rau chân vịt, tươi, nấu chín, không thêm chất béo'), -- Spinach, fresh, cooked, no added fat
    ('72125212', 'Rau chân vịt, đông lạnh, nấu chín, không thêm chất béo'), -- Spinach, frozen, cooked, no added fat
    ('72125213', 'Rau chân vịt, đóng hộp, nấu chín, không thêm chất béo'), -- Spinach, canned, cooked, no added fat
    ('72125217', 'Rau chân vịt, tươi, nấu với dầu'), -- Spinach, fresh, cooked with oil
    ('72125218', 'Rau chân vịt, tươi, nấu với bơ hoặc bơ thực vật'), -- Spinach, fresh, cooked with butter or margarine
    ('72125220', 'Rau chân vịt, không rõ dạng, nấu chín'), -- Spinach, NS as to form, cooked
    ('72125221', 'Rau chân vịt, tươi, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Spinach, fresh, cooked, fat added, NS as to fat type
    ('72125222', 'Rau chân vịt, đông lạnh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Spinach, frozen, cooked, fat added, NS as to fat type
    ('72125223', 'Rau chân vịt, đóng hộp, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Spinach, canned, cooked, fat added, NS as to fat type
    ('72125224', 'Rau chân vịt, đông lạnh, nấu với dầu'), -- Spinach, frozen, cooked with oil
    ('72125225', 'Rau chân vịt, đông lạnh, nấu với bơ hoặc bơ thực vật'), -- Spinach, frozen, cooked with butter or margarine
    ('72125227', 'Rau chân vịt, đóng hộp, nấu với dầu'), -- Spinach, canned, cooked with oil
    ('72125228', 'Rau chân vịt, đóng hộp, nấu với bơ hoặc bơ thực vật'), -- Spinach, canned, cooked with butter or margarine
    ('72125230', 'Rau chân vịt sốt kem'), -- Spinach, creamed
    ('72125240', 'Bánh souffle rau chân vịt'), -- Spinach souffle
    ('72125260', 'Món đút lò (casserole) rau chân vịt và phô mai'), -- Spinach and cheese casserole
    ('72125310', 'Món Palak Paneer (rau chân vịt với phô mai tươi, Ấn Độ)'), -- Palak Paneer
    ('72125500', 'Món Channa Saag (đậu gà nấu rau lá, Ấn Độ)'), -- Channa Saag
    ('72126001', 'Lá khoai môn, nấu chín'), -- Taro leaves, cooked
    ('72128211', 'Lá củ cải, tươi, nấu chín, không thêm chất béo'), -- Turnip greens, fresh, cooked, no added fat
    ('72128212', 'Lá củ cải, đông lạnh, nấu chín, không thêm chất béo'), -- Turnip greens, frozen, cooked, no added fat
    ('72128220', 'Lá củ cải, không rõ dạng, nấu chín'), -- Turrnip greens, NS as to form, cooked
    ('72128221', 'Lá củ cải, tươi, nấu chín, có thêm chất béo'), -- Turnip greens, fresh, cooked, fat added
    ('72128222', 'Lá củ cải, đông lạnh, nấu chín, có thêm chất béo'), -- Turnip greens, frozen, cooked, fat added
    ('72130100', 'Cải xoong, sống'), -- Watercress, raw
    ('72130201', 'Cải xoong, nấu chín'), -- Watercress, cooked
    ('72132201', 'Lá mướp đắng, lá cải ngựa, rau đay hoặc lá củ cải, nấu chín'), -- Bitter melon, horseradish, jute, or radish leaves, cooked
    ('72133201', 'Lá khoai lang, lá bí, lá bí đỏ, cải cúc hoặc lá đậu, nấu chín'), -- Sweet potato, squash, pumpkin, chrysanthemum, or bean leaves, cooked
    ('72201100', 'Súp lơ xanh, sống'), -- Broccoli, raw
    ('72201190', 'Súp lơ xanh, nấu chín, nhà hàng'), -- Broccoli, cooked, from restaurant
    ('72201211', 'Súp lơ xanh, tươi, nấu chín, không thêm chất béo'), -- Broccoli, fresh, cooked, no added fat
    ('72201212', 'Súp lơ xanh, đông lạnh, nấu chín, không thêm chất béo'), -- Broccoli, frozen, cooked, no added fat
    ('72201220', 'Súp lơ xanh, không rõ dạng, nấu chín'), -- Broccoli, NS as to form, cooked
    ('72201221', 'Súp lơ xanh, tươi, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Broccoli, fresh, cooked, fat added, NS as to fat type
    ('72201222', 'Súp lơ xanh, đông lạnh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Broccoli, frozen, cooked, fat added, NS as to fat type
    ('72201223', 'Súp lơ xanh, tươi, nấu với dầu'), -- Broccoli, fresh, cooked with oil
    ('72201224', 'Súp lơ xanh, tươi, nấu với bơ hoặc bơ thực vật'), -- Broccoli, fresh, cooked with butter or margarine
    ('72201226', 'Súp lơ xanh, đông lạnh, nấu với dầu'), -- Broccoli, frozen, cooked with oil
    ('72201227', 'Súp lơ xanh, đông lạnh, nấu với bơ hoặc bơ thực vật'), -- Broccoli, frozen, cooked with butter or margarine
    ('72202010', 'Món đút lò (casserole) súp lơ xanh với mì sợi'), -- Broccoli casserole with noodles
    ('72202020', 'Món đút lò (casserole) súp lơ xanh với cơm'), -- Broccoli casserole with rice
    ('72202030', 'Súp lơ xanh chiên'), -- Fried broccoli
    ('72203000', 'Cải làn (súp lơ Trung Quốc), sống'), -- Broccoli, chinese, raw
    ('72203070', 'Cải làn (súp lơ Trung Quốc), nấu chín'), -- Broccoli, Chinese, cooked
    ('72302100', 'Súp súp lơ xanh phô mai'), -- Soup, broccoli cheese
    ('73101010', 'Cà rốt, sống'), -- Carrots, raw
    ('73101110', 'Cà rốt, sống, trộn salad'), -- Carrots, raw, salad
    ('73101210', 'Cà rốt, sống, trộn salad với táo'), -- Carrots, raw, salad with apples
    ('73102190', 'Cà rốt, nấu chín, nhà hàng'), -- Carrots, cooked, from restaurant
    ('73102211', 'Cà rốt, tươi, nấu chín, không thêm chất béo'), -- Carrots, fresh, cooked, no added fat
    ('73102212', 'Cà rốt, đông lạnh, nấu chín, không thêm chất béo'), -- Carrots, frozen, cooked, no added fat
    ('73102213', 'Cà rốt, đóng hộp, nấu chín, không thêm chất béo'), -- Carrots, canned, cooked, no added fat
    ('73102217', 'Cà rốt, tươi, nấu với dầu'), -- Carrots, fresh, cooked with oil
    ('73102218', 'Cà rốt, tươi, nấu với bơ hoặc bơ thực vật'), -- Carrots, fresh, cooked with butter or margarine
    ('73102220', 'Cà rốt, không rõ dạng, nấu chín'), -- Carrots, NS as to form, cooked
    ('73102221', 'Cà rốt, tươi, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Carrots, fresh, cooked, fat added, NS as to fat type
    ('73102222', 'Cà rốt, đông lạnh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Carrots, frozen, cooked, fat added, NS as to fat type
    ('73102223', 'Cà rốt, đóng hộp, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Carrots, canned, cooked, fat added, NS as to fat type
    ('73102224', 'Cà rốt, đông lạnh, nấu với dầu'), -- Carrots, frozen, cooked with oil
    ('73102225', 'Cà rốt, đông lạnh, nấu với bơ hoặc bơ thực vật'), -- Carrots, frozen, cooked with butter or margarine
    ('73102227', 'Cà rốt, đóng hộp, nấu với dầu'), -- Carrots, canned, cooked with oil
    ('73102228', 'Cà rốt, đóng hộp, nấu với bơ hoặc bơ thực vật'), -- Carrots, canned, cooked with butter or margarine
    ('73102241', 'Cà rốt phủ bóng đường (glazed), nấu chín'), -- Carrots, glazed, cooked
    ('73103010', 'Cà rốt, đóng hộp, giảm muối, nấu chín, không thêm chất béo'), -- Carrots, canned, reduced sodium, cooked, no added fat
    ('73103020', 'Cà rốt, đóng hộp, giảm muối, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Carrots, canned, reduced sodium, cooked, fat added, NS as to fat type
    ('73103021', 'Cà rốt, đóng hộp, giảm muối, nấu với dầu'), -- Carrots, canned, reduced sodium, cooked with oil
    ('73103022', 'Cà rốt, đóng hộp, giảm muối, nấu với bơ hoặc bơ thực vật'), -- Carrots, canned, reduced sodium, cooked with butter or margarine
    ('73105000', 'Nước ép củ dền'), -- Beet juice
    ('73105010', 'Nước ép cà rốt, 100%'), -- Carrot juice, 100%
    ('73111211', 'Đậu Hà Lan và cà rốt, tươi, nấu chín, không thêm chất béo'), -- Peas and carrots, fresh, cooked, no added fat
    ('73111212', 'Đậu Hà Lan và cà rốt, đông lạnh, nấu chín, không thêm chất béo'), -- Peas and carrots, frozen, cooked, no added fat
    ('73111213', 'Đậu Hà Lan và cà rốt, đóng hộp, nấu chín, không thêm chất béo'), -- Peas and carrots, canned, cooked, no added fat
    ('73111220', 'Đậu Hà Lan và cà rốt, nấu chín, không rõ dạng'), -- Peas and carrots, cooked, NS as to form
    ('73111221', 'Đậu Hà Lan và cà rốt, tươi, nấu chín, có thêm chất béo'), -- Peas and carrots, fresh, cooked, fat added
    ('73111222', 'Đậu Hà Lan và cà rốt, đông lạnh, nấu chín, có thêm chất béo'), -- Peas and carrots, frozen, cooked, fat added
    ('73111223', 'Đậu Hà Lan và cà rốt, đóng hộp, nấu chín, có thêm chất béo'), -- Peas and carrots, canned, cooked, fat added
    ('73201013', 'Bí đỏ, đóng hộp, nấu chín'), -- Pumpkin, canned, cooked
    ('73201020', 'Bí đỏ, nấu chín'), -- Pumpkin, cooked
    ('73302010', 'Bí mùa đông (winter squash), sống'), -- Winter squash, raw
    ('73303010', 'Bí mùa đông (winter squash), nấu chín, không thêm chất béo'), -- Winter squash, cooked, no added fat
    ('73303020', 'Bí mùa đông (winter squash), nấu chín, có thêm chất béo'), -- Winter squash, cooked, fat added
    ('73305020', 'Bánh souffle bí mùa đông'), -- Squash, winter, souffle
    ('73401000', 'Khoai lang, loại chung'), -- Sweet potato, NFS
    ('73403000', 'Khoai lang nướng lò, không rõ có thêm chất béo'), -- Sweet potato, baked, NS as to fat
    ('73403010', 'Khoai lang nướng lò, không thêm chất béo'), -- Sweet potato, baked, no added fat
    ('73403020', 'Khoai lang nướng lò, có thêm chất béo'), -- Sweet potato, baked, fat added
    ('73405000', 'Khoai lang luộc, không rõ có thêm chất béo'), -- Sweet potato, boiled, NS as to fat
    ('73405010', 'Khoai lang luộc, không thêm chất béo'), -- Sweet potato, boiled, no added fat
    ('73405020', 'Khoai lang luộc, có thêm chất béo'), -- Sweet potato, boiled, fat added
    ('73406000', 'Khoai lang rim đường'), -- Sweet potato, candied
    ('73407000', 'Khoai lang đóng hộp, không rõ có thêm chất béo'), -- Sweet potato, canned, NS as to fat
    ('73407050', 'Khoai lang đóng hộp, không thêm chất béo'), -- Sweet potato, canned, no added fat
    ('73407060', 'Khoai lang đóng hộp, có thêm chất béo'), -- Sweet potato, canned, fat added
    ('73409000', 'Khoai lang đút lò (casserole) hoặc nghiền'), -- Sweet potato, casserole or mashed
    ('73410200', 'Khoai lang chiên, loại chung'), -- Sweet potato fries, NFS
    ('73410210', 'Khoai lang chiên lát (snack)'), -- Sweet potato chips
    ('73410320', 'Khoai lang chiên, đông lạnh'), -- Sweet potato fries, frozen
    ('73410340', 'Khoai lang chiên, từ loại tươi'), -- Sweet potato fries, from fresh
    ('73410400', 'Khoai lang chiên, đồ ăn nhanh/nhà hàng'), -- Sweet potato fries, fast food / restaurant
    ('73410500', 'Khoai lang chiên, trường học'), -- Sweet potato fries, school
    ('73420020', 'Khoai lang viên chiên (tots)'), -- Sweet potato tots
    ('73420100', 'Khoai lang viên chiên (tots), đồ ăn nhanh/nhà hàng'), -- Sweet potato tots, fast food / restaurant
    ('73420200', 'Khoai lang viên chiên (tots), trường học'), -- Sweet potato tots, school
    ('73502000', 'Súp bí đỏ'), -- Soup, pumpkin
    ('74101000', 'Cà chua, sống'), -- Tomatoes, raw
    ('74201000', 'Cà chua, không rõ dạng, nấu chín'), -- Tomatoes, NS as to form, cooked
    ('74201001', 'Cà chua, tươi, nấu chín'), -- Tomatoes, fresh, cooked
    ('74201003', 'Cà chua, đóng hộp, nấu chín'), -- Tomatoes, canned, cooked
    ('74203010', 'Cà chua đút lò với vụn bánh mì (scalloped)'), -- Tomatoes, scalloped
    ('74204500', 'Cà chua, đóng hộp, giảm muối, nấu chín'), -- Tomatoes, canned, reduced sodium, cooked
    ('74205010', 'Cà chua xanh chiên'), -- Fried green tomatoes
    ('74205020', 'Cà chua xanh, ngâm chua'), -- Tomato, green, pickled
    ('74206000', 'Cà chua phơi khô'), -- Sun-dried tomatoes
    ('74301100', 'Nước ép cà chua, 100%'), -- Tomato juice, 100%
    ('74301150', 'Nước ép cà chua, 100%, ít muối'), -- Tomato juice, 100%, low sodium
    ('74302000', 'Nước cà chua pha trộn (tomato juice cocktail)'), -- Tomato juice cocktail
    ('74303000', 'Nước ép cà chua và rau củ, 100%'), -- Tomato and vegetable juice, 100%
    ('74303100', 'Nước ép cà chua và rau củ, 100%, ít muối'), -- Tomato and vegetable juice, 100%, low sodium
    ('74401010', 'Tương cà'), -- Ketchup
    ('74401110', 'Tương cà, giảm muối'), -- Ketchup, reduced sodium
    ('74402010', 'Sốt ớt cà chua (chili sauce)'), -- Tomato chili sauce
    ('74402100', 'Sốt salsa, loại chung'), -- Salsa, NFS
    ('74402110', 'Sốt salsa pico de gallo'), -- Salsa, pico de gallo
    ('74402150', 'Sốt salsa đỏ'), -- Salsa, red
    ('74402200', 'Sốt salsa đỏ, tự làm tại nhà'), -- Salsa, red, homemade
    ('74402210', 'Sốt taco'), -- Taco sauce
    ('74402250', 'Sốt enchilada đỏ'), -- Enchilada sauce, red
    ('74402260', 'Sốt enchilada xanh'), -- Enchilada sauce, green
    ('74402350', 'Sốt salsa verde hoặc salsa xanh'), -- Salsa verde or salsa, green
    ('74403000', 'Tương ớt cay kiểu Thái'), -- Hot Thai sauce
    ('74404010', 'Sốt mì spaghetti'), -- Spaghetti sauce
    ('74404020', 'Sốt mì spaghetti, có thêm rau củ'), -- Spaghetti sauce with added vegetables
    ('74404050', 'Sốt mì spaghetti, giảm muối'), -- Spaghetti sauce, reduced sodium
    ('74404060', 'Sốt mì spaghetti, không béo'), -- Spaghetti sauce, fat free
    ('74404090', 'Sốt vodka với cà chua và kem sữa'), -- Vodka sauce with tomatoes and cream
    ('74406010', 'Sốt BBQ'), -- Barbecue sauce
    ('74406060', 'Sốt Buffalo (sốt cay kiểu cánh gà Buffalo)'), -- Buffalo sauce
    ('74406100', 'Sốt bít tết'), -- Steak sauce
    ('74406500', 'Sốt cocktail (chấm hải sản)'), -- Cocktail sauce
    ('74410110', 'Gia vị nêm kiểu Puerto Rico, có giăm bông'), -- Puerto Rican seasoning with ham
    ('74420110', 'Gia vị nêm kiểu Puerto Rico, không có giăm bông và sốt cà chua'), -- Puerto Rican seasoning without ham and tomato sauce
    ('74506000', 'Salad cà chua và dưa chuột, làm từ cà chua, dưa chuột, dầu và giấm'), -- Tomato and cucumber salad made with tomato, cucumber, oil, and vinegar
    ('74601000', 'Súp cà chua'), -- Soup, tomato
    ('74601010', 'Súp kem cà chua'), -- Soup, cream of tomato
    ('74602010', 'Súp cà chua, đóng hộp'), -- Soup, tomato, canned
    ('74602200', 'Súp cà chua, đóng hộp/hộp giấy, giảm muối'), -- Soup, tomato, canned / carton, reduced sodium
    ('74701000', 'Bánh mì kẹp cà chua, bánh mì trắng'), -- Tomato sandwich on white
    ('74701010', 'Bánh mì kẹp cà chua, bánh mì lúa mì'), -- Tomato sandwich on wheat
    ('75100250', 'Rau củ sống, loại chung'), -- Raw vegetable, NFS
    ('75100300', 'Rau mầm, loại chung'), -- Sprouts, NFS
    ('75100500', 'Mầm cỏ linh lăng (alfalfa), sống'), -- Alfalfa sprouts, raw
    ('75100750', 'Atisô'), -- Artichoke
    ('75100800', 'Măng tây, sống'), -- Asparagus, raw
    ('75101000', 'Giá đỗ, sống'), -- Bean sprouts, raw
    ('75101800', 'Đậu cô ve, sống'), -- Green beans, raw
    ('75102500', 'Củ dền, sống'), -- Beets, raw
    ('75102600', 'Súp lơ lai (broccoflower), sống'), -- Broccoflower, raw
    ('75102750', 'Cải Brussels, sống'), -- Brussels sprouts, raw
    ('75103000', 'Cải bắp xanh, sống'), -- Cabbage, green, raw
    ('75104000', 'Cải thảo, sống'), -- Cabbage, Chinese, raw
    ('75105000', 'Cải bắp tím, sống'), -- Cabbage, red, raw
    ('75105500', 'Xương rồng ăn được (nopal), sống'), -- Cactus, raw
    ('75107000', 'Súp lơ trắng, sống'), -- Cauliflower, raw
    ('75109000', 'Cần tây, sống'), -- Celery, raw
    ('75109010', 'Củ thì là (fennel), sống'), -- Fennel bulb, raw
    ('75109400', 'Húng quế, sống'), -- Basil, raw
    ('75109500', 'Hẹ, sống'), -- Chives, raw
    ('75109550', 'Rau mùi, sống'), -- Cilantro, raw
    ('75109600', 'Ngô, sống'), -- Corn, raw
    ('75111000', 'Dưa chuột, sống'), -- Cucumber, raw
    ('75111200', 'Cà tím, sống'), -- Eggplant, raw
    ('75111500', 'Tỏi, sống'), -- Garlic, raw
    ('75111800', 'Củ đậu, sống'), -- Jicama, raw
    ('75112000', 'Su hào, sống'), -- Kohlrabi, raw
    ('75113000', 'Xà lách, sống'), -- Lettuce, raw
    ('75113060', 'Xà lách Boston (xà lách mỡ), sống'), -- Lettuce, Boston, raw
    ('75113080', 'Xà lách arugula (rau rocket), sống'), -- Lettuce, arugula, raw
    ('75114000', 'Rau xà lách trộn, sống'), -- Mixed salad greens, raw
    ('75115000', 'Nấm, sống'), -- Mushrooms, raw
    ('75117010', 'Hành lá, sống'), -- Onions, green, raw
    ('75117020', 'Hành tây, sống'), -- Onions, raw
    ('75119000', 'Mùi tây, sống'), -- Parsley, raw
    ('75120000', 'Đậu Hà Lan, sống'), -- Green peas, raw
    ('75121000', 'Ớt cay, sống'), -- Peppers, hot, raw
    ('75122000', 'Ớt, sống, loại chung'), -- Peppers, raw, NFS
    ('75122100', 'Ớt chuông xanh, sống'), -- Peppers, sweet, green, raw
    ('75122200', 'Ớt chuông đỏ, sống'), -- Peppers, sweet, red, raw
    ('75124000', 'Ớt chuối (banana pepper), sống'), -- Peppers, banana, raw
    ('75125000', 'Củ cải đỏ'), -- Radish
    ('75127000', 'Củ cải Thụy Điển (rutabaga), sống'), -- Rutabaga, raw
    ('75127500', 'Rong biển, sống'), -- Seaweed, raw
    ('75127750', 'Đậu Hà Lan cả vỏ (snow peas), sống'), -- Snowpeas, raw
    ('75128000', 'Bí mùa hè vàng (summer squash), sống'), -- Summer squash, yellow, raw
    ('75128010', 'Bí mùa hè xanh (summer squash), sống'), -- Summer squash, green, raw
    ('75129000', 'Củ cải tròn (turnip), sống'), -- Turnip, raw
    ('75132000', 'Nước ép rau củ hỗn hợp'), -- Mixed vegetable juice
    ('75132100', 'Nước ép cần tây'), -- Celery juice
    ('75140500', 'Salad súp lơ xanh với súp lơ trắng, phô mai, thịt xông khói vụn và sốt trộn'), -- Broccoli salad with cauliflower, cheese, bacon bits, and dressing
    ('75140510', 'Salad súp lơ xanh bào sợi (broccoli slaw)'), -- Broccoli slaw salad
    ('75140990', 'Salad cải bắp (coleslaw), đồ ăn nhanh/nhà hàng'), -- Coleslaw, fast food / restaurant
    ('75141000', 'Salad cải bắp (coleslaw)'), -- Coleslaw
    ('75141040', 'Salad cải bắp, loại chung'), -- Cabbage salad, NFS
    ('75141100', 'Salad cải bắp (coleslaw), có hoa quả'), -- Coleslaw, with fruit
    ('75142000', 'Dưa chuột và rau củ trộn giấm namasu (Nhật Bản)'), -- Cucumber and vegetable namasu
    ('75142500', 'Salad dưa chuột, làm với sốt trộn kem chua'), -- Cucumber salad, made with sour cream dressing
    ('75142550', 'Salad dưa chuột, làm với sốt trộn kiểu Ý'), -- Cucumber salad, made with Italian dressing
    ('75142600', 'Salad dưa chuột, làm từ dưa chuột và giấm'), -- Cucumber salad made with cucumber and vinegar
    ('75143000', 'Salad xà lách với rau củ các loại gồm cà chua và/hoặc cà rốt, không sốt trộn'), -- Lettuce, salad with assorted vegetables including tomatoes and/or carrots, no dressing
    ('75143050', 'Salad xà lách với rau củ các loại trừ cà chua và cà rốt, không sốt trộn'), -- Lettuce, salad with assorted vegetables excluding tomatoes and carrots, no dressing
    ('75143100', 'Salad xà lách với quả bơ, cà chua và/hoặc cà rốt, có hoặc không có rau khác, không sốt trộn'), -- Lettuce, salad with avocado, tomato, and/or carrots, with or without other vegetables, no dressing
    ('75143200', 'Salad xà lách với phô mai, cà chua và/hoặc cà rốt, có hoặc không có rau khác, không sốt trộn'), -- Lettuce, salad with cheese, tomato and/or carrots, with or without other vegetables, no dressing
    ('75143300', 'Salad xà lách với trứng, cà chua và/hoặc cà rốt, có hoặc không có rau khác, không sốt trộn'), -- Lettuce, salad with egg, tomato, and/or carrots, with or without other vegetables, no dressing
    ('75143350', 'Salad xà lách với trứng, phô mai, cà chua và/hoặc cà rốt, có hoặc không có rau khác, không sốt trộn'), -- Lettuce, salad with egg, cheese, tomato, and/or carrots, with or without other vegetables, no dressing
    ('75144100', 'Xà lách trụng héo, với sốt trộn thịt xông khói'), -- Lettuce, wilted, with bacon dressing
    ('75145000', 'Salad bảy lớp, salad xà lách gồm hành tây, cần tây, ớt chuông xanh, đậu Hà Lan, sốt mayonnaise, phô mai, trứng và/hoặc thịt xông khói'), -- Seven-layer salad, lettuce salad made with a combination of onion, celery, green pepper, peas, mayonnaise, cheese, eggs, and/or bacon
    ('75146000', 'Salad Hy Lạp, không sốt trộn'), -- Greek Salad, no dressing
    ('75147000', 'Salad rau chân vịt, không sốt trộn'), -- Spinach salad, no dressing
    ('75148010', 'Salad Cobb, không sốt trộn'), -- Cobb salad, no dressing
    ('75200700', 'Nước uống nha đam'), -- Aloe vera juice drink
    ('75202011', 'Măng tây, tươi, nấu chín, không thêm chất béo'), -- Asparagus, fresh, cooked, no added fat
    ('75202012', 'Măng tây, đông lạnh, nấu chín, không thêm chất béo'), -- Asparagus, frozen, cooked, no added fat
    ('75202013', 'Măng tây, đóng hộp, nấu chín, không thêm chất béo'), -- Asparagus, canned, cooked, no added fat
    ('75202020', 'Măng tây, không rõ dạng, nấu chín'), -- Asparagus, NS as to form, cooked
    ('75202021', 'Măng tây, tươi, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Asparagus, fresh, cooked, fat added, NS as to fat type
    ('75202022', 'Măng tây, đông lạnh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Asparagus, frozen, cooked, fat added, NS as to fat type
    ('75202023', 'Măng tây, đóng hộp, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Asparagus, canned, cooked, fat added, NS as to fat type
    ('75202027', 'Măng tây, tươi, nấu với dầu'), -- Asparagus, fresh, cooked with oil
    ('75202028', 'Măng tây, tươi, nấu với bơ hoặc bơ thực vật'), -- Asparagus, fresh, cooked with butter or margarine
    ('75202031', 'Măng tây, đông lạnh, nấu với dầu'), -- Asparagus, frozen, cooked with oil
    ('75202032', 'Măng tây, đông lạnh, nấu với bơ hoặc bơ thực vật'), -- Asparagus, frozen, cooked with butter or margarine
    ('75202034', 'Măng tây, đóng hộp, nấu với dầu'), -- Asparagus, canned, cooked with oil
    ('75202035', 'Măng tây, đóng hộp, nấu với bơ hoặc bơ thực vật'), -- Asparagus, canned, cooked with butter or margarine
    ('75203028', 'Măng, nấu chín'), -- Bamboo shoots, cooked
    ('75204012', 'Đậu lima, từ loại đông lạnh, không thêm chất béo'), -- Lima beans, from frozen, no added fat
    ('75204022', 'Đậu lima, từ loại đông lạnh, có thêm chất béo'), -- Lima beans, from frozen, fat added
    ('75204023', 'Đậu lima, từ đồ hộp'), -- Lima beans, from canned
    ('75205005', 'Đậu cô ve, nấu chín, nhà hàng'), -- Green beans, cooked, from restaurant
    ('75205021', 'Đậu cô ve, tươi, nấu chín, không thêm chất béo'), -- Green beans, fresh, cooked, no added fat
    ('75205022', 'Đậu cô ve, đông lạnh, nấu chín, không thêm chất béo'), -- Green beans, frozen, cooked, no added fat
    ('75205023', 'Đậu cô ve, đóng hộp, nấu chín, không thêm chất béo'), -- Green beans, canned, cooked, no added fat
    ('75205030', 'Đậu cô ve, không rõ dạng, nấu chín'), -- Green beans, NS as to form, cooked
    ('75205031', 'Đậu cô ve, tươi, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Green beans, fresh, cooked, fat added, NS as to fat type
    ('75205032', 'Đậu cô ve, đông lạnh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Green beans, frozen, cooked, fat added, NS as to fat type
    ('75205033', 'Đậu cô ve, đóng hộp, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Green beans, canned, cooked, fat added, NS as to fat type
    ('75205044', 'Đậu cô ve, tươi, nấu với dầu'), -- Green beans, fresh, cooked with oil
    ('75205045', 'Đậu cô ve, tươi, nấu với bơ hoặc bơ thực vật'), -- Green beans, fresh, cooked with butter or margarine
    ('75205047', 'Đậu cô ve, đông lạnh, nấu với dầu'), -- Green beans, frozen, cooked with oil
    ('75205048', 'Đậu cô ve, đông lạnh, nấu với bơ hoặc bơ thực vật'), -- Green beans, frozen, cooked with butter or margarine
    ('75205050', 'Đậu cô ve, đóng hộp, nấu với dầu'), -- Green beans, canned, cooked with oil
    ('75205051', 'Đậu cô ve, đóng hộp, nấu với bơ hoặc bơ thực vật'), -- Green beans, canned, cooked with butter or margarine
    ('75205120', 'Đậu cô ve, đóng hộp, giảm muối, nấu chín, không thêm chất béo'), -- Green beans, canned, reduced sodium, cooked, no added fat
    ('75205130', 'Đậu cô ve, đóng hộp, giảm muối, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Green beans, canned, reduced sodium, cooked, fat added, NS as to fat type
    ('75205131', 'Đậu cô ve, đóng hộp, giảm muối, nấu với dầu'), -- Green beans, canned, reduced sodium, cooked with oil
    ('75205132', 'Đậu cô ve, đóng hộp, giảm muối, nấu với bơ hoặc bơ thực vật'), -- Green beans, canned, reduced sodium, cooked with butter or margarine
    ('75205200', 'Đậu cô ve chiên'), -- Fried green beans
    ('75206020', 'Đậu que vàng, nấu chín'), -- Yellow string beans, cooked
    ('75207021', 'Giá đỗ, nấu chín'), -- Bean sprouts, cooked
    ('75208011', 'Củ dền, tươi, nấu chín, không thêm chất béo'), -- Beets, fresh, cooked, no added fat
    ('75208013', 'Củ dền, đóng hộp, nấu chín, không thêm chất béo'), -- Beets, canned, cooked, no added fat
    ('75208020', 'Củ dền, không rõ dạng, nấu chín'), -- Beets, NS as to form, cooked
    ('75208021', 'Củ dền, tươi, nấu chín, có thêm chất béo'), -- Beets, fresh, cooked, fat added
    ('75208023', 'Củ dền, đóng hộp, nấu chín, có thêm chất béo'), -- Beets, canned, cooked, fat added
    ('75208110', 'Củ dền, đóng hộp, giảm muối, nấu chín'), -- Beets, canned, reduced sodium, cooked
    ('75208310', 'Mướp đắng, nấu chín'), -- Bitter melon, cooked
    ('75208501', 'Sa kê, nấu chín'), -- Breadfruit, cooked
    ('75208720', 'Súp lơ lai (broccoflower), nấu chín'), -- Broccoflower, cooked
    ('75209011', 'Cải Brussels, tươi, nấu chín, không thêm chất béo'), -- Brussels sprouts, fresh, cooked, no added fat
    ('75209012', 'Cải Brussels, đông lạnh, nấu chín, không thêm chất béo'), -- Brussels sprouts, frozen, cooked, no added fat
    ('75209020', 'Cải Brussels, không rõ dạng, nấu chín'), -- Brussels sprouts, NS as to form, cooked
    ('75209021', 'Cải Brussels, tươi, nấu chín, có thêm chất béo'), -- Brussels sprouts, fresh, cooked, fat added
    ('75209022', 'Cải Brussels, đông lạnh, nấu chín, có thêm chất béo'), -- Brussels sprouts, frozen, cooked, fat added
    ('75209501', 'Ngưu bàng, nấu chín'), -- Burdock, cooked
    ('75210010', 'Cải thảo, nấu chín, không thêm chất béo'), -- Cabbage, Chinese, cooked, no added fat
    ('75210020', 'Cải thảo, nấu chín, có thêm chất béo'), -- Cabbage, Chinese, cooked, fat added
    ('75211020', 'Cải bắp xanh, nấu chín, không thêm chất béo'), -- Cabbage, green, cooked, no added fat
    ('75211030', 'Cải bắp xanh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Cabbage, green, cooked, fat added, NS as to fat type
    ('75211031', 'Cải bắp xanh, nấu với dầu'), -- Cabbage, green, cooked with oil
    ('75211032', 'Cải bắp xanh, nấu với bơ hoặc bơ thực vật'), -- Cabbage, green, cooked with butter or margarine
    ('75212020', 'Cải bắp tím, nấu chín'), -- Cabbage, red, cooked
    ('75213020', 'Cải bắp savoy (cải bắp lá xoăn), nấu chín'), -- Cabbage, savoy, cooked
    ('75213110', 'Xương rồng ăn được (nopal), nấu chín, không thêm chất béo'), -- Cactus, cooked, no added fat
    ('75213120', 'Xương rồng ăn được (nopal), nấu chín, có thêm chất béo'), -- Cactus, cooked, fat added
    ('75214011', 'Súp lơ trắng, tươi, nấu chín, không thêm chất béo'), -- Cauliflower, fresh, cooked, no added fat
    ('75214012', 'Súp lơ trắng, đông lạnh, nấu chín, không thêm chất béo'), -- Cauliflower, frozen, cooked, no added fat
    ('75214020', 'Súp lơ trắng, không rõ dạng, nấu chín'), -- Cauliflower, NS as to form, cooked
    ('75214021', 'Súp lơ trắng, tươi, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Cauliflower, fresh, cooked, fat added, NS as to fat type
    ('75214022', 'Súp lơ trắng, đông lạnh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Cauliflower, frozen, cooked, fat added, NS as to fat type
    ('75214027', 'Súp lơ trắng, tươi, nấu với dầu'), -- Cauliflower, fresh, cooked with oil
    ('75214028', 'Súp lơ trắng, tươi, nấu với bơ hoặc bơ thực vật'), -- Cauliflower, fresh, cooked with butter or margarine
    ('75214030', 'Súp lơ trắng, đông lạnh, nấu với dầu'), -- Cauliflower, frozen, cooked with oil
    ('75214031', 'Súp lơ trắng, đông lạnh, nấu với bơ hoặc bơ thực vật'), -- Cauliflower, frozen, cooked with butter or margarine
    ('75215020', 'Cần tây, nấu chín'), -- Celery, cooked
    ('75215120', 'Củ thì là (fennel), nấu chín'), -- Fennel bulb, cooked
    ('75215511', 'Su su, nấu chín'), -- Christophine, cooked
    ('75215990', 'Ngô, nấu chín, nhà hàng'), -- Corn, cooked, from restaurant
    ('75216111', 'Ngô, tươi, nấu chín, không thêm chất béo'), -- Corn, fresh, cooked, no added fat
    ('75216112', 'Ngô, đông lạnh, nấu chín, không thêm chất béo'), -- Corn, frozen, cooked, no added fat
    ('75216113', 'Ngô, đóng hộp, nấu chín, không thêm chất béo'), -- Corn, canned, cooked, no added fat
    ('75216120', 'Ngô, không rõ dạng, nấu chín'), -- Corn, NS as to form, cooked
    ('75216121', 'Ngô, tươi, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Corn, fresh, cooked, fat added, NS as to fat type
    ('75216122', 'Ngô, đông lạnh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Corn, frozen, cooked, fat added, NS as to fat type
    ('75216123', 'Ngô, đóng hộp, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Corn, canned, cooked, fat added, NS as to fat type
    ('75216134', 'Ngô, tươi, nấu với dầu'), -- Corn, fresh, cooked with oil
    ('75216135', 'Ngô, tươi, nấu với bơ hoặc bơ thực vật'), -- Corn, fresh, cooked with butter or margarine
    ('75216137', 'Ngô, đông lạnh, nấu với dầu'), -- Corn, frozen, cooked with oil
    ('75216138', 'Ngô, đông lạnh, nấu với bơ hoặc bơ thực vật'), -- Corn, frozen, cooked with butter or margarine
    ('75216141', 'Ngô, đóng hộp, nấu với dầu'), -- Corn, canned, cooked with oil
    ('75216142', 'Ngô, đóng hộp, nấu với bơ hoặc bơ thực vật'), -- Corn, canned, cooked with butter or margarine
    ('75216153', 'Ngô sốt kem'), -- Corn, creamed
    ('75216310', 'Ngô, đóng hộp, giảm muối, nấu chín, không thêm chất béo'), -- Corn, canned, reduced sodium, cooked, no added fat
    ('75216320', 'Ngô, đóng hộp, giảm muối, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Corn, canned, reduced sodium, cooked, fat added, NS as to fat type
    ('75216321', 'Ngô, đóng hộp, giảm muối, nấu với dầu'), -- Corn, canned, reduced sodium, cooked with oil
    ('75216322', 'Ngô, đóng hộp, giảm muối, nấu với bơ hoặc bơ thực vật'), -- Corn, canned, reduced sodium, cooked with butter or margarine
    ('75216720', 'Dưa chuột, nấu chín'), -- Cucumber, cooked
    ('75217010', 'Cà tím, nấu chín, không thêm chất béo'), -- Eggplant, cooked, no added fat
    ('75217020', 'Cà tím, nấu chín, có thêm chất béo'), -- Eggplant, cooked, fat added
    ('75217301', 'Hoa điên điển, hoa bí hoặc hoa kim châm, nấu chín'), -- Flowers or blossoms of sesbania, squash, or lily, cooked
    ('75217400', 'Tỏi, nấu chín'), -- Garlic, cooked
    ('75217520', 'Ngô hominy (ngô xử lý nước vôi), nấu chín'), -- Hominy, cooked
    ('75218011', 'Su hào, nấu chín'), -- Kohlrabi, cooked
    ('75218400', 'Tỏi tây'), -- Leeks
    ('75218501', 'Củ sen, nấu chín'), -- Lotus root, cooked
    ('75219011', 'Nấm, tươi, nấu chín, không thêm chất béo'), -- Mushrooms, fresh, cooked, no added fat
    ('75219020', 'Nấm, không rõ dạng, nấu chín'), -- Mushrooms, NS as to form, cooked
    ('75219021', 'Nấm, tươi, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Mushrooms, fresh, cooked, fat added, NS as to fat type
    ('75219023', 'Nấm, đóng hộp, nấu chín'), -- Mushrooms, canned, cooked
    ('75219033', 'Nấm, tươi, nấu với dầu'), -- Mushrooms, fresh, cooked with oil
    ('75219034', 'Nấm, tươi, nấu với bơ hoặc bơ thực vật'), -- Mushrooms, fresh, cooked with butter or margarine
    ('75219100', 'Nấm châu Á, nấu chín, từ loại khô'), -- Mushroom, Asian, cooked, from dried
    ('75220011', 'Đậu bắp, tươi, nấu chín, không thêm chất béo'), -- Okra, fresh, cooked, no added fat
    ('75220012', 'Đậu bắp, đông lạnh, nấu chín, không thêm chất béo'), -- Okra, frozen, cooked, no added fat
    ('75220020', 'Đậu bắp, không rõ dạng, nấu chín'), -- Okra, NS as to form, cooked
    ('75220021', 'Đậu bắp, tươi, nấu chín, có thêm chất béo'), -- Okra, fresh, cooked, fat added
    ('75220022', 'Đậu bắp, đông lạnh, nấu chín, có thêm chất béo'), -- Okra, frozen, cooked, fat added
    ('75220051', 'Xà lách, nấu chín'), -- Lettuce, cooked
    ('75221011', 'Hành tây, nấu chín, không thêm chất béo'), -- Onions, cooked, no added fat
    ('75221021', 'Hành tây, nấu chín, có thêm chất béo'), -- Onions, cooked, fat added
    ('75221030', 'Hành tây bi (hành ngọc trai), nấu chín'), -- Onions, pearl, cooked
    ('75221061', 'Hành lá, nấu chín'), -- Onions, green, cooked
    ('75221160', 'Củ hũ dừa (lõi thân cọ), nấu chín'), -- Palm hearts, cooked
    ('75222020', 'Củ cải vàng (parsnip), nấu chín'), -- Parsnips, cooked
    ('75223022', 'Đậu mắt đen, từ loại đông lạnh'), -- Blackeyed peas, from frozen
    ('75223023', 'Đậu mắt đen, từ đồ hộp'), -- Blackeyed peas, from canned
    ('75224000', 'Đậu Hà Lan, nấu chín, nhà hàng'), -- Green peas, cooked, from restaurant
    ('75224021', 'Đậu Hà Lan, tươi, nấu chín, không thêm chất béo'), -- Green peas, fresh, cooked, no added fat
    ('75224022', 'Đậu Hà Lan, đông lạnh, nấu chín, không thêm chất béo'), -- Green peas, frozen, cooked, no added fat
    ('75224023', 'Đậu Hà Lan, đóng hộp, nấu chín, không thêm chất béo'), -- Green peas, canned, cooked, no added fat
    ('75224030', 'Đậu Hà Lan, không rõ dạng, nấu chín'), -- Green peas, NS as to form, cooked
    ('75224031', 'Đậu Hà Lan, tươi, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Green peas, fresh, cooked, fat added, NS as to fat type
    ('75224032', 'Đậu Hà Lan, đông lạnh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Green peas, frozen, cooked, fat added, NS as to fat type
    ('75224033', 'Đậu Hà Lan, đóng hộp, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Green peas, canned, cooked, fat added, NS as to fat type
    ('75224043', 'Đậu Hà Lan, tươi, nấu với dầu'), -- Green peas, fresh, cooked with oil
    ('75224044', 'Đậu Hà Lan, tươi, nấu với bơ hoặc bơ thực vật'), -- Green peas, fresh, cooked with butter or margarine
    ('75224046', 'Đậu Hà Lan, đông lạnh, nấu với dầu'), -- Green peas, frozen, cooked with oil
    ('75224047', 'Đậu Hà Lan, đông lạnh, nấu với bơ hoặc bơ thực vật'), -- Green peas, frozen, cooked with butter or margarine
    ('75224049', 'Đậu Hà Lan, đóng hộp, nấu với dầu'), -- Green peas, canned, cooked with oil
    ('75224050', 'Đậu Hà Lan, đóng hộp, nấu với bơ hoặc bơ thực vật'), -- Green peas, canned, cooked with butter or margarine
    ('75224120', 'Đậu Hà Lan, đóng hộp, giảm muối, nấu chín, không thêm chất béo'), -- Green peas, canned, reduced sodium, cooked, no added fat
    ('75224130', 'Đậu Hà Lan, đóng hộp, giảm muối, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Green peas, canned, reduced sodium, cooked, fat added, NS as to fat type
    ('75224131', 'Đậu Hà Lan, đóng hộp, giảm muối, nấu với dầu'), -- Green peas, canned, reduced sodium, cooked with oil
    ('75224132', 'Đậu Hà Lan, đóng hộp, giảm muối, nấu với bơ hoặc bơ thực vật'), -- Green peas, canned, reduced sodium, cooked with butter or margarine
    ('75226020', 'Ớt chuông xanh, nấu chín'), -- Peppers, green, cooked
    ('75226060', 'Ớt chuông đỏ, nấu chín'), -- Peppers, red, cooked
    ('75226111', 'Ớt cay, nấu chín'), -- Peppers, hot, cooked
    ('75226700', 'Ớt pimiento'), -- Pimiento
    ('75228020', 'Củ cải Thụy Điển (rutabaga), nấu chín'), -- Rutabaga, cooked
    ('75229011', 'Củ salsify, nấu chín'), -- Salsify, cooked
    ('75230000', 'Dưa cải bắp muối chua (sauerkraut)'), -- Sauerkraut
    ('75231011', 'Đậu Hà Lan cả vỏ (snow peas), tươi, nấu chín, không thêm chất béo'), -- Snowpeas, fresh, cooked, no added fat
    ('75231012', 'Đậu Hà Lan cả vỏ (snow peas), đông lạnh, nấu chín, không thêm chất béo'), -- Snowpeas, frozen, cooked, no added fat
    ('75231020', 'Đậu Hà Lan cả vỏ (snow peas), không rõ dạng, nấu chín'), -- Snowpeas, NS as to form, cooked
    ('75231021', 'Đậu Hà Lan cả vỏ (snow peas), tươi, nấu chín, có thêm chất béo'), -- Snowpeas, fresh, cooked, fat added
    ('75231022', 'Đậu Hà Lan cả vỏ (snow peas), đông lạnh, nấu chín, có thêm chất béo'), -- Snowpeas, frozen, cooked, fat added
    ('75232000', 'Rong biển, khô'), -- Seaweed, dried
    ('75232110', 'Rong biển, nấu chín, không thêm chất béo'), -- Seaweed, cooked, no added fat
    ('75232120', 'Rong biển, nấu chín, có thêm chất béo'), -- Seaweed, cooked, fat added
    ('75233011', 'Bí mùa hè vàng hoặc xanh, tươi, nấu chín, không thêm chất béo'), -- Summer squash, yellow or green, fresh, cooked, no added fat
    ('75233012', 'Bí mùa hè vàng hoặc xanh, đông lạnh, nấu chín, không thêm chất béo'), -- Summer squash, yellow or green, frozen, cooked, no added fat
    ('75233020', 'Bí mùa hè vàng hoặc xanh, không rõ dạng, nấu chín'), -- Summer squash, yellow or green, NS as to form, cooked
    ('75233021', 'Bí mùa hè vàng hoặc xanh, tươi, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Summer squash, yellow or green, fresh, cooked, fat added, NS as to fat type
    ('75233022', 'Bí mùa hè vàng hoặc xanh, đông lạnh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Summer squash, yellow or green, frozen, cooked, fat added, NS as to fat type
    ('75233023', 'Bí mùa hè vàng hoặc xanh, đóng hộp, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Summer squash, yellow or green, canned, cooked, fat added, NS as to fat type
    ('75233027', 'Bí mùa hè vàng hoặc xanh, tươi, nấu với dầu'), -- Summer squash, yellow or green, fresh, cooked with oil
    ('75233028', 'Bí mùa hè vàng hoặc xanh, tươi, nấu với bơ hoặc bơ thực vật'), -- Summer squash, yellow or green, fresh, cooked with butter or margarine
    ('75233030', 'Bí mùa hè vàng hoặc xanh, đông lạnh, nấu với dầu'), -- Summer squash, yellow or green, frozen, cooked with oil
    ('75233031', 'Bí mùa hè vàng hoặc xanh, đông lạnh, nấu với bơ hoặc bơ thực vật'), -- Summer squash, yellow or green, frozen, cooked with butter or margarine
    ('75233220', 'Bí mì (spaghetti squash), nấu chín'), -- Spaghetti squash, cooked
    ('75234021', 'Củ cải tròn (turnip), nấu chín'), -- Turnip, cooked
    ('75235000', 'Củ năng'), -- Water Chesnut
    ('75235750', 'Bí đao, nấu chín'), -- Winter melon, cooked
    ('75236000', 'Nấm men'), -- Yeast
    ('75236500', 'Chiết xuất nấm men dạng phết'), -- Yeast extract spread
    ('75301110', 'Đậu lima và ngô, nấu chín, không thêm chất béo'), -- Lima beans and corn, cooked, no added fat
    ('75301120', 'Đậu lima và ngô, nấu chín, có thêm chất béo'), -- Lima beans and corn, cooked, fat added
    ('75302080', 'Salad đậu que vàng và/hoặc đậu cô ve'), -- Bean salad, yellow and/or green string beans
    ('75306998', 'Ớt chuông và hành tây, nấu chín, không thêm chất béo'), -- Peppers and onions, cooked, no added fat
    ('75307000', 'Ớt chuông và hành tây, nấu chín, có thêm chất béo'), -- Peppers and onions, cooked, fat added
    ('75310990', 'Rau củ trộn cổ điển, nấu chín, nhà hàng'), -- Classic mixed vegetables, cooked, from restaurant
    ('75311012', 'Rau củ trộn cổ điển, đông lạnh, nấu chín, không thêm chất béo'), -- Classic mixed vegetables, frozen, cooked, no added fat
    ('75311013', 'Rau củ trộn cổ điển, đóng hộp, nấu chín, không thêm chất béo'), -- Classic mixed vegetables, canned, cooked, no added fat
    ('75311020', 'Rau củ trộn cổ điển, không rõ dạng, nấu chín'), -- Classic mixed vegetables, NS as to form, cooked
    ('75311022', 'Rau củ trộn cổ điển, đông lạnh, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Classic mixed vegetables, frozen, cooked, fat added, NS as to fat type
    ('75311023', 'Rau củ trộn cổ điển, đóng hộp, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Classic mixed vegetables, canned, cooked, fat added, NS as to fat type
    ('75311027', 'Rau củ trộn cổ điển, đông lạnh, nấu với dầu'), -- Classic mixed vegetables, frozen, cooked with oil
    ('75311028', 'Rau củ trộn cổ điển, đông lạnh, nấu với bơ hoặc bơ thực vật'), -- Classic mixed vegetables, frozen, cooked with butter or margarine
    ('75311030', 'Rau củ trộn cổ điển, đóng hộp, nấu với dầu'), -- Classic mixed vegetables, canned, cooked with oil
    ('75311031', 'Rau củ trộn cổ điển, đóng hộp, nấu với bơ hoặc bơ thực vật'), -- Classic mixed vegetables, canned, cooked with butter or margarine
    ('75311110', 'Rau củ trộn cổ điển, đóng hộp, giảm muối, nấu chín, không thêm chất béo'), -- Classic mixed vegetables, canned, reduced sodium, cooked, no added fat
    ('75311120', 'Rau củ trộn cổ điển, đóng hộp, giảm muối, nấu chín, có thêm chất béo, không rõ loại chất béo'), -- Classic mixed vegetables, canned, reduced sodium, cooked, fat added, NS as to fat type
    ('75311121', 'Rau củ trộn cổ điển, đóng hộp, giảm muối, nấu với dầu'), -- Classic mixed vegetables, canned, reduced sodium, cooked with oil
    ('75311122', 'Rau củ trộn cổ điển, đóng hộp, giảm muối, nấu với bơ hoặc bơ thực vật'), -- Classic mixed vegetables, canned, reduced sodium, cooked with butter or margarine
    ('75315010', 'Đậu Hà Lan và ngô, nấu chín, không thêm chất béo'), -- Peas and corn, cooked, no added fat
    ('75315020', 'Đậu Hà Lan và ngô, nấu chín, có thêm chất béo'), -- Peas and corn, cooked, fat added
    ('75316050', 'Rau củ hầm ratatouille (Pháp)'), -- Ratatouille
    ('75317010', 'Rau củ kiểu món hầm, nấu chín, có thêm chất béo'), -- Vegetables, stew type, cooked, fat added
    ('75317020', 'Rau củ kiểu món hầm, nấu chín, không thêm chất béo'), -- Vegetables, stew type, cooked, no added fat
    ('75330050', 'Súp lơ xanh và súp lơ trắng, nấu chín, không thêm chất béo'), -- Broccoli and cauliflower, cooked, no added fat
    ('75330060', 'Súp lơ xanh và súp lơ trắng, nấu chín, có thêm chất béo'), -- Broccoli and cauliflower, cooked, fat added
    ('75330080', 'Súp lơ xanh, súp lơ trắng và cà rốt, nấu chín, không thêm chất béo'), -- Broccoli, cauliflower and carrots, cooked, no added fat
    ('75330090', 'Súp lơ xanh, súp lơ trắng và cà rốt, nấu chín, có thêm chất béo'), -- Broccoli, cauliflower and carrots, cooked, fat added
    ('75340010', 'Rau củ xào kiểu châu Á, nấu chín, không thêm chất béo'), -- Asian stir fry vegetables, cooked, no added fat
    ('75340020', 'Rau củ xào kiểu châu Á, nấu chín, có thêm chất béo'), -- Asian stir fry vegetables, cooked, fat added
    ('75340200', 'Món chay La Hán (Jai, Monk''s Food)'), -- Jai, Monk's Food
    ('75365000', 'Rau củ hỗn hợp, sấy khô'), -- Vegetable mixture, dried
    ('75400500', 'Atisô nhồi'), -- Artichokes, stuffed
    ('75403020', 'Món đút lò (casserole) đậu cô ve'), -- Green bean casserole
    ('75403200', 'Đậu cô ve, nấu chín, kiểu Tứ Xuyên'), -- Green beans, cooked, Szechuan-style
    ('75409020', 'Súp lơ trắng chiên'), -- Fried cauliflower
    ('75410500', 'Ớt nhồi chiles rellenos, nhân phô mai'), -- Chiles rellenos, cheese-filled
    ('75410530', 'Ớt nhồi chiles rellenos, nhân thịt và phô mai'), -- Chiles rellenos, filled with meat and cheese
    ('75410550', 'Ớt jalapeno nhồi'), -- Stuffed jalapeno pepper
    ('75411010', 'Ngô đút lò (scalloped) hoặc bánh pudding ngô'), -- Corn, scalloped or pudding
    ('75411020', 'Bánh ngô chiên (fritter)'), -- Fritter, corn
    ('75412010', 'Cà tím chiên'), -- Fried eggplant
    ('75412030', 'Sốt chấm cà tím'), -- Eggplant dip
    ('75412060', 'Món đút lò (casserole) cà tím phô mai parmesan, loại thường'), -- Eggplant parmesan casserole, regular
    ('75412070', 'Cà tím với phô mai và sốt cà chua'), -- Eggplant with cheese and tomato sauce
    ('75414020', 'Nấm nhồi'), -- Mushrooms, stuffed
    ('75414030', 'Nấm chiên'), -- Fried mushrooms
    ('75414500', 'Đậu bắp chiên'), -- Fried okra
    ('75415022', 'Hành tây chiên vòng'), -- Fried onion rings
    ('75416500', 'Salad đậu Hà Lan'), -- Pea salad
    ('75416600', 'Salad đậu Hà Lan có phô mai'), -- Pea salad with cheese
    ('75418010', 'Bí mùa hè vàng hoặc xanh chiên'), -- Fried summer squash, yellow or green
    ('75418020', 'Món đút lò (casserole) bí mùa hè với cà chua và phô mai'), -- Squash, summer, casserole with tomato and cheese
    ('75418030', 'Món đút lò (casserole) bí mùa hè với cơm và sốt cà chua'), -- Squash, summer, casserole, with rice and tomato sauce
    ('75418040', 'Món đút lò (casserole) bí mùa hè với sốt phô mai'), -- Squash, summer, casserole, with cheese sauce
    ('75418060', 'Bánh souffle bí mùa hè'), -- Squash, summer, souffle
    ('75439010', 'Món hầm rau củ'), -- Stew, vegetable
    ('75439500', 'Món chow mein hoặc chop suey (rau xào kiểu Hoa), không thịt, không mì'), -- Chow mein or chop suey, meatless, no noodles
    ('75440200', 'Rau củ tẩm bột chiên tempura'), -- Vegetable tempura
    ('75440400', 'Bánh pakora (rau củ chiên bột, Ấn Độ)'), -- Pakora
    ('75440600', 'Cà ri rau củ'), -- Vegetable curry
    ('75440610', 'Cà ri rau củ với cơm'), -- Vegetable curry with rice
    ('75460900', 'Món chow mein hoặc chop suey (rau xào kiểu Hoa), không thịt, có mì sợi'), -- Chow mein or chop suey, meatless, with noodles
    ('75500110', 'Đậu cô ve, ngâm chua'), -- Green beans, pickled
    ('75500210', 'Củ dền, ngâm chua'), -- Beets, pickled
    ('75500510', 'Cần tây, ngâm chua'), -- Celery, pickled
    ('75501010', 'Ngô ngâm chua băm nhỏ (relish)'), -- Relish, corn
    ('75502010', 'Súp lơ trắng, ngâm chua'), -- Cauliflower, pickled
    ('75502500', 'Cải bắp xanh, ngâm chua'), -- Cabbage, green, pickled
    ('75502510', 'Cải bắp tím, ngâm chua'), -- Cabbage, red, pickled
    ('75502520', 'Kim chi'), -- Kimchi
    ('75503010', 'Dưa chuột muối thì là'), -- Pickles, dill
    ('75503020', 'Dưa chuột muối băm nhỏ (relish)'), -- Relish, pickle
    ('75503040', 'Dưa chuột muối ngọt'), -- Pickles, sweet
    ('75503080', 'Cà tím, ngâm chua'), -- Eggplant, pickled
    ('75503085', 'Gừng, ngâm chua'), -- Ginger root, pickled
    ('75503090', 'Cải ngựa (horseradish)'), -- Horseradish
    ('75505000', 'Nấm, ngâm chua'), -- Mushrooms, pickled
    ('75506010', 'Mù tạt'), -- Mustard
    ('75506100', 'Sốt chấm mù tạt mật ong'), -- Honey mustard dip
    ('75507000', 'Đậu bắp, ngâm chua'), -- Okra, pickled
    ('75510000', 'Quả ô liu, loại chung'), -- Olives, NFS
    ('75510010', 'Quả ô liu xanh'), -- Olives, green
    ('75510020', 'Quả ô liu đen'), -- Olives, black
    ('75510030', 'Quả ô liu nhồi'), -- Olives, stuffed
    ('75510050', 'Sốt ô liu nghiền (tapenade)'), -- Olive tapenade
    ('75511010', 'Tương ớt'), -- Hot pepper sauce
    ('75511020', 'Ớt chuông, ngâm chua'), -- Peppers, sweet, pickled
    ('75511040', 'Ớt cay, ngâm chua'), -- Peppers, hot, pickled
    ('75511050', 'Ớt jalapeno'), -- Peppers, jalapenos
    ('75511100', 'Dưa chuột muối, loại chung'), -- Pickles, NFS
    ('75511300', 'Dưa chuột muối chiên'), -- Pickles, fried
    ('75512010', 'Củ cải đỏ, ngâm chua'), -- Radishes, pickled
    ('75513010', 'Rong biển, ngâm chua'), -- Seaweed, pickled
    ('75515100', 'Rau củ, ngâm chua'), -- Vegetables, pickled
    ('75534030', 'Củ cải tròn (turnip), ngâm chua'), -- Turnip, pickled
    ('75534550', 'Mù tạt wasabi'), -- Wasabi paste
    ('75535000', 'Bí ngòi, ngâm chua'), -- Zucchini, pickled
    ('75601100', 'Súp củ dền borscht'), -- Soup, borscht
    ('75604600', 'Súp lạnh gazpacho'), -- Soup, gazpacho
    ('75607060', 'Súp kem nấm'), -- Soup, cream of mushroom
    ('75608100', 'Súp hành kiểu Pháp'), -- Soup, French onion
    ('75611010', 'Súp kem rau củ'), -- Soup, cream of vegetable
    ('75647000', 'Súp rong biển'), -- Soup, seaweed
    ('75649010', 'Súp rau củ, đóng hộp'), -- Soup, vegetable, canned
    ('75649040', 'Súp rau củ, đóng hộp, giảm muối'), -- Soup, vegetable, canned, reduced sodium
    ('75649110', 'Súp rau củ'), -- Soup, vegetable
    ('75651000', 'Súp minestrone (Ý)'), -- Soup, minestrone
    ('75652010', 'Súp thịt bò'), -- Soup, beef
    ('75656060', 'Súp rau củ có thịt'), -- Soup, vegetable, with meat
    ('76000000', 'Rau củ cho trẻ nhỏ, loại chung'), -- Baby Toddler vegetable, NFS
    ('76201010', 'Cà rốt cho trẻ nhỏ, giai đoạn 1'), -- Baby Toddler carrots, Stage 1
    ('76201040', 'Cà rốt cho trẻ nhỏ, giai đoạn 2'), -- Baby Toddler carrots, Stage 2
    ('76205010', 'Bí cho trẻ nhỏ, giai đoạn 1'), -- Baby Toddler squash, Stage 1
    ('76205040', 'Bí cho trẻ nhỏ, giai đoạn 2'), -- Baby Toddler squash, Stage 2
    ('76209010', 'Khoai lang cho trẻ nhỏ, giai đoạn 1'), -- Baby Toddler sweet potatoes, Stage 1
    ('76209040', 'Khoai lang cho trẻ nhỏ, giai đoạn 2'), -- Baby Toddler sweet potatoes, Stage 2
    ('76401010', 'Đậu cô ve cho trẻ nhỏ, giai đoạn 1'), -- Baby Toddler green beans, Stage 1
    ('76401040', 'Đậu cô ve cho trẻ nhỏ, giai đoạn 2'), -- Baby Toddler green beans, Stage 2
    ('76403010', 'Củ dền cho trẻ nhỏ'), -- Baby Toddler beets
    ('76407010', 'Rau củ hỗn hợp cho trẻ nhỏ, giai đoạn 2'), -- Baby Toddler multiple vegetables, Stage 2
    ('76407020', 'Rau củ hỗn hợp cho trẻ nhỏ, giai đoạn 3'), -- Baby Toddler multiple vegetables, Stage 3
    ('76407050', 'Rau củ cho trẻ nhỏ, có ngũ cốc'), -- Baby Toddler vegetables, with grain
    ('76407080', 'Rau củ và thịt cho trẻ nhỏ'), -- Baby Toddler vegetables and meat
    ('76409010', 'Đậu Hà Lan cho trẻ nhỏ, giai đoạn 1'), -- Baby Toddler peas, Stage 1
    ('76409040', 'Đậu Hà Lan cho trẻ nhỏ, giai đoạn 2'), -- Baby Toddler peas, Stage 2
    ('76700000', 'Suất ăn cho trẻ tập đi, loại chung'), -- Toddler meal, NFS
    ('76702100', 'Suất ăn cho trẻ tập đi, thịt và rau củ'), -- Toddler meal, meat and vegetables
    ('76702200', 'Suất ăn cho trẻ tập đi, cơm và rau củ'), -- Toddler meal, rice and vegetables
    ('76702300', 'Suất ăn cho trẻ tập đi, mì Ý (pasta)'), -- Toddler meal, pasta
    ('76702400', 'Suất ăn cho trẻ tập đi, mì Ý (pasta) và rau củ'), -- Toddler meal, pasta and vegetables
    ('77121010', 'Khoai tây nhồi chiên kiểu Puerto Rico'), -- Fried stuffed potatoes, Puerto Rican style
    ('77201210', 'Chuối lá xanh với tóp mỡ, kiểu Puerto Rico'), -- Green plantain with cracklings, Puerto Rican style
    ('77205610', 'Bánh pie thịt với chuối lá chín, kiểu Puerto Rico'), -- Ripe plantain meat pie, Puerto Rican style
    ('77272010', 'Bánh pasteles kiểu Puerto Rico'), -- Puerto Rican pasteles
    ('77316010', 'Cải bắp nhồi thịt, kiểu Puerto Rico'), -- Stuffed cabbage, with meat, Puerto Rican style
    ('77316510', 'Cải bắp nhồi thịt và cơm, món Syria, kiểu Puerto Rico'), -- Stuffed cabbage, with meat and rice, Syrian dish, Puerto Rican style
    ('77316600', 'Món đút lò (casserole) cà tím và thịt'), -- Eggplant and meat casserole
    ('78101000', 'Nước ép rau củ và trái cây, 100% nước ép, giàu vitamin C'), -- Vegetable and fruit juice, 100% juice, with high vitamin C
    ('78101100', 'Sinh tố trái cây và rau củ, có sữa'), -- Fruit and vegetable smoothie, with dairy
    ('78101110', 'Sinh tố trái cây và rau củ, có thêm đạm (protein)'), -- Fruit and vegetable smoothie, added protein
    ('78101115', 'Sinh tố trái cây và rau củ, dùng sữa thực vật (non-dairy)'), -- Fruit and vegetable smoothie, non-dairy
    ('78101118', 'Sinh tố trái cây và rau củ, dùng sữa thực vật (non-dairy), có thêm đạm (protein)'), -- Fruit and vegetable smoothie, non-dairy, added protein
    ('78101120', 'Sinh tố trái cây và rau củ, đóng chai'), -- Fruit and vegetable smoothie, bottled
    ('78101125', 'Sinh tố trái cây và rau củ, không có sữa'), -- Fruit and vegetable smoothie, no dairy
    ('78101130', 'Sinh tố rau củ'), -- Vegetable smoothie
    ('81100000', 'Chất béo phết bánh (bơ/bơ thực vật), loại chung'), -- Table fat, NFS
    ('81100500', 'Bơ, loại chung'), -- Butter, NFS
    ('81101000', 'Bơ, dạng thỏi'), -- Butter, stick
    ('81101010', 'Bơ, dạng hộp'), -- Butter, tub
    ('81101520', 'Bơ, loại nhẹ'), -- Butter, light
    ('81102000', 'Bơ thực vật, loại chung'), -- Margarine, NFS
    ('81102010', 'Bơ thực vật, dạng thỏi'), -- Margarine, stick
    ('81102020', 'Bơ thực vật, dạng hộp'), -- Margarine, tub
    ('81103090', 'Chất thay thế bơ, dạng lỏng'), -- Butter replacement, liquid
    ('81104010', 'Bơ thực vật, loại nhẹ'), -- Margarine, light
    ('81106010', 'Chất thay thế bơ, dạng bột'), -- Butter replacement, powder
    ('81200100', 'Dầu hoặc chất béo phết bánh, loại chung'), -- Oil or table fat, NFS
    ('81201000', 'Mỡ động vật hoặc mỡ chảy ra khi nấu thịt'), -- Animal fat or drippings
    ('81202000', 'Mỡ lợn'), -- Lard
    ('81203000', 'Mỡ shortening, không rõ gốc thực vật hay động vật'), -- Shortening, NS as to vegetable or animal
    ('81204000', 'Bơ ghee, bơ tinh luyện'), -- Ghee, clarified butter
    ('81301000', 'Sốt tỏi'), -- Garlic sauce
    ('81301020', 'Sốt bơ chanh'), -- Lemon-butter sauce
    ('81302010', 'Sốt hollandaise'), -- Hollandaise sauce
    ('81302040', 'Sốt phết bánh mì kẹp'), -- Sandwich spread
    ('81302050', 'Sốt tartar'), -- Tartar sauce
    ('81302060', 'Sốt cải ngựa'), -- Horseradish sauce
    ('81302070', 'Sốt pesto'), -- Pesto sauce
    ('81308100', 'Sốt chấm khoai tây chiên (fry sauce)'), -- Fry sauce
    ('81308200', 'Nước sốt, loại chung'), -- Sauce, NFS
    ('81312100', 'Sốt cà ri'), -- Curry sauce
    ('81322000', 'Bơ mật ong'), -- Honey butter
    ('82101000', 'Dầu thực vật, loại chung'), -- Vegetable oil, NFS
    ('82101300', 'Dầu hạnh nhân'), -- Almond oil
    ('82101500', 'Dầu dừa'), -- Coconut oil
    ('82102000', 'Dầu ngô'), -- Corn oil
    ('82103000', 'Dầu hạt bông'), -- Cottonseed oil
    ('82103500', 'Dầu hạt lanh'), -- Flaxseed oil
    ('82104000', 'Dầu ô liu'), -- Olive oil
    ('82105000', 'Dầu lạc'), -- Peanut oil
    ('82105500', 'Dầu cải (canola)'), -- Canola oil
    ('82106000', 'Dầu hạt rum (safflower)'), -- Safflower oil
    ('82107000', 'Dầu vừng'), -- Sesame oil
    ('82108000', 'Dầu đậu tương'), -- Soybean oil
    ('82108500', 'Dầu hướng dương'), -- Sunflower oil
    ('82108700', 'Dầu óc chó'), -- Walnut oil
    ('82109000', 'Dầu mầm lúa mì'), -- Wheat germ oil
    ('83100100', 'Sốt trộn salad, loại chung, dùng cho salad'), -- Salad dressing, NFS, for salads
    ('83100200', 'Sốt trộn salad, loại chung, dùng cho bánh mì kẹp'), -- Salad dressing, NFS, for sandwiches
    ('83101000', 'Sốt trộn salad phô mai xanh hoặc roquefort'), -- Blue or roquefort cheese dressing
    ('83101600', 'Sốt trộn salad thịt xông khói và cà chua'), -- Bacon and tomato dressing
    ('83102000', 'Sốt trộn salad Caesar'), -- Caesar dressing
    ('83103000', 'Sốt trộn salad cải bắp (coleslaw)'), -- Coleslaw dressing
    ('83104000', 'Sốt trộn salad kiểu Pháp hoặc Catalina'), -- French or Catalina dressing
    ('83105500', 'Sốt trộn salad mật ong mù tạt'), -- Honey mustard dressing
    ('83106000', 'Sốt trộn kiểu Ý, làm từ giấm và dầu'), -- Italian dressing, made with vinegar and oil
    ('83107000', 'Sốt mayonnaise, loại thường'), -- Mayonnaise, regular
    ('83108000', 'Sốt mayonnaise thuần chay'), -- Vegan mayonnaise
    ('83109000', 'Sốt trộn salad kiểu Nga'), -- Russian dressing
    ('83110000', 'Sốt trộn salad dạng mayonnaise'), -- Mayonnaise-type salad dressing
    ('83112000', 'Sốt trộn salad quả bơ'), -- Avocado dressing
    ('83112400', 'Sốt trộn kiểu Ý dạng kem'), -- Creamy Italian dressing
    ('83112950', 'Sốt trộn salad hạt anh túc'), -- Poppy seed dressing
    ('83112990', 'Sốt trộn salad vừng'), -- Sesame dressing
    ('83113500', 'Sốt trộn salad ranch'), -- Ranch dressing
    ('83114000', 'Sốt trộn salad Thousand Island'), -- Thousand Island dressing
    ('83115000', 'Sốt trộn salad trái cây'), -- Fruit dressing
    ('83200100', 'Sốt trộn salad, loại nhẹ, loại chung'), -- Salad dressing, light, NFS
    ('83201000', 'Sốt trộn salad phô mai xanh hoặc roquefort, loại nhẹ'), -- Blue or roquefort cheese dressing, light
    ('83201500', 'Sốt trộn kiểu Ý dạng kem, loại nhẹ'), -- Creamy Italian dressing, light
    ('83202020', 'Sốt trộn salad kiểu Pháp hoặc Catalina, loại nhẹ'), -- French or Catalina dressing, light
    ('83203000', 'Sốt trộn salad Caesar, loại nhẹ'), -- Caesar dressing, light
    ('83204000', 'Sốt mayonnaise, loại nhẹ'), -- Mayonnaise, light
    ('83204030', 'Sốt mayonnaise, giảm béo, có dầu ô liu'), -- Mayonnaise, reduced fat,  with olive oil
    ('83204050', 'Sốt trộn salad dạng mayonnaise, loại nhẹ'), -- Mayonnaise-type salad dressing, light
    ('83204500', 'Sốt trộn salad mật ong mù tạt, loại nhẹ'), -- Honey mustard dressing, light
    ('83205450', 'Sốt trộn kiểu Ý, loại nhẹ'), -- Italian dressing, light
    ('83205560', 'Sốt trộn salad ranch, loại nhẹ'), -- Ranch dressing, light
    ('83206500', 'Sốt trộn salad vừng, loại nhẹ'), -- Sesame dressing, light
    ('83207000', 'Sốt trộn salad Thousand Island, loại nhẹ'), -- Thousand Island dressing, light
    ('83208500', 'Sốt trộn hoặc nước ướp kiểu Hàn Quốc'), -- Korean dressing or marinade
    ('83300100', 'Sốt trộn salad phô mai xanh hoặc roquefort, không béo'), -- Blue or roquefort cheese dressing, fat free
    ('83300200', 'Sốt trộn salad Caesar, không béo'), -- Caesar dressing, fat free
    ('83300250', 'Sốt trộn kiểu Ý dạng kem, không béo'), -- Creamy Italian dressing, fat free
    ('83300400', 'Sốt trộn salad kiểu Pháp hoặc Catalina, không béo'), -- French or Catalina dressing, fat free
    ('83300500', 'Sốt trộn salad mật ong mù tạt, không béo'), -- Honey mustard dressing, fat free
    ('83300600', 'Sốt trộn kiểu Ý, không béo'), -- Italian dressing, fat free
    ('83300700', 'Sốt mayonnaise, không béo'), -- Mayonnaise, fat free
    ('83300750', 'Sốt trộn salad ranch, không béo'), -- Ranch dressing, fat free
    ('83300900', 'Sốt trộn salad, không béo, loại chung'), -- Salad dressing, fat free, NFS
    ('83301000', 'Sốt trộn salad Thousand Island, không béo'), -- Thousand Island dressing, fat free
    ('89901000', 'Thịt xông khói, dùng kèm rau'), -- Bacon, for use with vegetables
    ('89901002', 'Giăm bông, dùng kèm rau'), -- Ham, for use with vegetables
    ('89901004', 'Thịt bò, dùng kèm rau'), -- Beef, for use with vegetables
    ('89901006', 'Thịt gà, dùng kèm rau'), -- Chicken, for use with vegetables
    ('89901010', 'Sốt kem, dùng kèm rau'), -- Cream sauce, for use with vegetables
    ('89901020', 'Sốt phô mai, dùng kèm rau'), -- Cheese sauce, for use with vegetables
    ('89901030', 'Nước sốt gravy, dùng kèm rau'), -- Gravy, for use with vegetables
    ('89901040', 'Sốt nền nước tương, dùng kèm rau'), -- Soy based sauce, for use with vegetables
    ('89901050', 'Sốt cà chua, dùng kèm rau'), -- Tomato sauce, for use with vegetables
    ('89902000', 'Quả bơ, dùng cho bánh mì kẹp'), -- Avocado, for use on a sandwich
    ('89902010', 'Dưa chuột, dùng cho bánh mì kẹp'), -- Cucumber, for use on a sandwich
    ('89902020', 'Xà lách, dùng cho bánh mì kẹp'), -- Lettuce, for use on a sandwich
    ('89902030', 'Nấm, dùng cho bánh mì kẹp'), -- Mushrooms, for use on a sandwich
    ('89902040', 'Hành tây, dùng cho bánh mì kẹp'), -- Onions, for use on a sandwich
    ('89902050', 'Ớt, dùng cho bánh mì kẹp'), -- Pepper, for use on a sandwich
    ('89902060', 'Rau chân vịt, dùng cho bánh mì kẹp'), -- Spinach, for use on a sandwich
    ('89902070', 'Cà chua, dùng cho bánh mì kẹp'), -- Tomatoes, for use on a sandwich
    ('89902100', 'Thịt xông khói, dùng cho bánh mì kẹp'), -- Bacon, for use on a sandwich
    ('91101000', 'Đường, loại chung'), -- Sugar, NFS
    ('91101010', 'Đường trắng, dạng hạt hoặc viên'), -- Sugar, white, granulated or lump
    ('91101020', 'Đường trắng, dạng bột mịn làm bánh'), -- Sugar, white, confectioner's, powdered
    ('91102010', 'Đường nâu'), -- Sugar, brown
    ('91104100', 'Đường quế'), -- Sugar, cinnamon
    ('91106010', 'Hỗn hợp chất tạo ngọt thay thế đường và đường'), -- Sugar substitute and sugar blend
    ('91107000', 'Chất tạo ngọt thay thế đường, sucralose, dạng bột'), -- Sugar substitute, sucralose, powder
    ('91108000', 'Chất tạo ngọt thay thế đường, cỏ ngọt (stevia), dạng bột'), -- Sugar substitute, stevia, powder
    ('91108010', 'Chất tạo ngọt thay thế đường, cỏ ngọt (stevia), dạng lỏng'), -- Sugar substitute, stevia, liquid
    ('91108020', 'Chất tạo ngọt thay thế đường, la hán quả, dạng bột'), -- Sugar substitute, monk fruit, powder
    ('91200000', 'Chất tạo ngọt thay thế đường, dạng bột, loại chung'), -- Sugar substitute, powder, NFS
    ('91200005', 'Chất tạo ngọt thay thế đường, dạng lỏng, loại chung'), -- Sugar substitute, liquid, NFS
    ('91200040', 'Chất tạo ngọt thay thế đường, saccharin, dạng bột'), -- Sugar substitute, saccharin, powder
    ('91200110', 'Chất tạo ngọt thay thế đường, saccharin, dạng lỏng'), -- Sugar substitute, saccharin, liquid
    ('91201010', 'Chất tạo ngọt thay thế đường, aspartame, dạng bột'), -- Sugar substitute, aspartame, powder
    ('91300010', 'Siro, loại chung'), -- Syrup, NFS
    ('91300100', 'Siro ăn bánh kếp (pancake)'), -- Pancake syrup
    ('91301030', 'Siro ngô'), -- Corn syrup
    ('91301050', 'Siro việt quất'), -- Blueberry syrup
    ('91301080', 'Siro sô-cô-la'), -- Chocolate syrup
    ('91301081', 'Siro sô-cô-la, loại nhẹ'), -- Chocolate syrup, light
    ('91301100', 'Siro đường đơn (simple syrup)'), -- Simple syrup
    ('91301130', 'Siro dâu tây pha đồ uống'), -- Strawberry drink syrup
    ('91301510', 'Siro ăn bánh kếp (pancake), loại nhẹ'), -- Pancake syrup, light
    ('91302010', 'Mật ong'), -- Honey
    ('91302020', 'Chất tạo ngọt lỏng từ cây thùa (agave)'), -- Agave liquid sweetener
    ('91303000', 'Mật mía'), -- Molasses
    ('91304010', 'Sốt phủ butterscotch hoặc caramel'), -- Topping, butterscotch or caramel
    ('91304020', 'Sốt phủ sô-cô-la'), -- Topping, chocolate
    ('91304030', 'Sốt phủ trái cây'), -- Topping, fruit
    ('91304040', 'Sốt phủ marshmallow'), -- Topping, marshmallow
    ('91304060', 'Sốt phủ hạt và siro'), -- Topping, nuts and syrup
    ('91304090', 'Kem phết sô-cô-la hạt phỉ'), -- Chocolate hazelnut spread
    ('91305010', 'Kem phủ bánh sô-cô-la'), -- Icing, chocolate
    ('91305020', 'Kem phủ bánh màu trắng'), -- Icing, white
    ('91306020', 'Sốt chấm caramel, loại thường'), -- Caramel dip, regular
    ('91306025', 'Sốt chấm caramel, loại nhẹ'), -- Caramel dip, light
    ('91306030', 'Sốt chấm sô-cô-la'), -- Chocolate dip
    ('91306040', 'Sốt chấm tráng miệng'), -- Dessert dip
    ('91361010', 'Sốt chua ngọt'), -- Sweet and sour sauce
    ('91361040', 'Sốt tráng miệng'), -- Dessert sauce
    ('91361050', 'Sốt mận chua ngọt (duck sauce)'), -- Duck sauce
    ('91400000', 'Mứt hoặc mứt đông (jam/jelly), loại chung'), -- Jam or jelly, NFS
    ('91401000', 'Mứt đông (jelly)'), -- Jelly
    ('91402000', 'Mứt (jam)'), -- Jam
    ('91403000', 'Mứt trái cây nghiền (fruit butter)'), -- Fruit butter
    ('91404000', 'Mứt cam (marmalade)'), -- Marmalade
    ('91405000', 'Mứt hoặc mứt đông, không đường'), -- Jam or jelly, sugar free
    ('91405500', 'Mứt hoặc mứt đông, giảm đường'), -- Jam or jelly, reduced sugar
    ('91406500', 'Mứt hoặc mứt đông, tạo ngọt bằng nước ép trái cây'), -- Jam or jelly, fruit juice sweetened
    ('91407100', 'Mứt ổi dạng thỏi (guava paste)'), -- Guava paste
    ('91407120', 'Mứt khoai lang dạng thỏi (sweet potato paste)'), -- Sweet potato paste
    ('91407150', 'Đậu nghiền nhuyễn (bean paste), có đường'), -- Bean paste, sweetened
    ('91501010', 'Thạch (gelatin)'), -- Gelatin dessert
    ('91501020', 'Thạch (gelatin) có trái cây'), -- Gelatin dessert with fruit
    ('91501100', 'Salad thạch (gelatin) có rau'), -- Gelatin salad with vegetables
    ('91511010', 'Thạch (gelatin), không đường'), -- Gelatin dessert, sugar free
    ('91511020', 'Thạch (gelatin), không đường, có trái cây'), -- Gelatin dessert, sugar free, with fruit
    ('91520100', 'Thạch đậu đỏ yokan (Nhật Bản)'), -- Yokan
    ('91560100', 'Bánh pudding dừa haupia (Hawaii)'), -- Haupia
    ('91601000', 'Đá bào kiểu Ý (Italian ice)'), -- Italian Ice
    ('91601010', 'Đá bào kiểu Ý (Italian ice), không thêm đường'), -- Italian Ice, no sugar added
    ('91610900', 'Kem que đá, loại chung'), -- Popsicle, NFS
    ('91611000', 'Kem que đá'), -- Popsicle
    ('91611100', 'Kem que đá, không thêm đường'), -- Popsicle, no sugar added
    ('91612000', 'Kem đá ống (freezer pop)'), -- Freezer pop
    ('91621000', 'Đá bào siro (snow cone)'), -- Snow cone
    ('91621050', 'Đá bào siro (snow cone), không thêm đường'), -- Snow cone, no sugar added
    ('91700010', 'Kẹo, loại chung'), -- Candy, NFS
    ('91701010', 'Kẹo hạnh nhân bọc sô-cô-la'), -- Almonds, chocolate covered candy
    ('91705005', 'Kẹo sô-cô-la loại khác, loại chung'), -- Chocolate candy, other, NFS
    ('91705010', 'Kẹo sô-cô-la'), -- Chocolate candy
    ('91705012', 'Kẹo sô-cô-la có hạt loại khác, loại chung'), -- Chocolate candy with nuts, other, NFS
    ('91705015', 'Kẹo sô-cô-la có hạt'), -- Chocolate candy with nuts
    ('91705020', 'Kẹo sô-cô-la có ngũ cốc'), -- Chocolate candy with cereal
    ('91705080', 'Kẹo sô-cô-la nhân bánh quy'), -- Chocolate candy, cookie filled
    ('91705200', 'Sô-cô-la chip (hạt sô-cô-la)'), -- Chocolate chips
    ('91705250', 'Kẹo cốm rắc trang trí (sprinkles)'), -- Candy, sprinkles
    ('91705290', 'Kẹo sô-cô-la đen loại khác, loại chung'), -- Dark chocolate candy, other, NFS
    ('91705300', 'Kẹo sô-cô-la đen'), -- Dark chocolate candy
    ('91705312', 'Kẹo sô-cô-la đen có hạt loại khác, loại chung'), -- Dark chocolate candy with nuts, other, NFS
    ('91705315', 'Kẹo sô-cô-la đen có hạt'), -- Dark chocolate candy with nuts
    ('91705400', 'Kẹo sô-cô-la trắng'), -- White chocolate candy
    ('91705440', 'Kẹo mềm sô-cô-la (fudge)'), -- Chocolate candy, fudge
    ('91705450', 'Kẹo sô-cô-la nhân caramel'), -- Chocolate candy, caramel filled
    ('91705460', 'Kẹo sô-cô-la nhân caramel có hạt'), -- Chocolate candy, caramel filled with nuts
    ('91705470', 'Kẹo sô-cô-la nhân dừa'), -- Chocolate candy, coconut filled
    ('91705480', 'Kẹo sô-cô-la nhân kem'), -- Chocolate candy, cream filled
    ('91705510', 'Kẹo sô-cô-la nhân nougat'), -- Chocolate candy, nougat filled
    ('91705520', 'Kẹo sô-cô-la nhân nougat có hạt'), -- Chocolate candy, nougat filled with nuts
    ('91705530', 'Kẹo sô-cô-la nhân bơ lạc'), -- Chocolate candy, peanut butter filled
    ('91705550', 'Kẹo sô-cô-la có trái cây khô'), -- Chocolate candy with dried fruit
    ('91706010', 'Kẹo sô-cô-la, không đường'), -- Chocolate candy, sugar free
    ('91706020', 'Kẹo không sô-cô-la loại khác, loại chung'), -- Candy, non chocolate, other, NFS
    ('91718300', 'Bánh ladoo (Ấn Độ), viên tròn'), -- Ladoo, round ball
    ('91721000', 'Kẹo cam thảo'), -- Candy, licorice
    ('91723000', 'Kẹo dẻo marshmallow'), -- Candy, marshmallow
    ('91723030', 'Kẹo caramel'), -- Candy, caramel
    ('91728000', 'Kẹo nougat có hạt'), -- Candy, nougat with nuts
    ('91731000', 'Kẹo lạc bọc sô-cô-la'), -- Peanuts, chocolate covered candy
    ('91733000', 'Kẹo lạc giòn (peanut brittle)'), -- Candy, peanut brittle
    ('91745000', 'Kẹo bạc hà'), -- Candy, mint
    ('91745010', 'Kẹo dẻo gummy'), -- Candy, gummy
    ('91745025', 'Kẹo cứng'), -- Candy, hard
    ('91745030', 'Kẹo mút'), -- Candy, lollipop
    ('91745035', 'Kẹo ngậm ho'), -- Cough drops
    ('91745050', 'Kẹo bông'), -- Candy, cotton
    ('91745110', 'Kẹo viên hương trái cây'), -- Candy, fruit flavored pieces
    ('91746100', 'Kẹo sô-cô-la bọc vỏ đường'), -- Chocolate candy, candy shell
    ('91746110', 'Kẹo sô-cô-la bọc vỏ đường có hạt'), -- Chocolate candy, candy shell with nuts
    ('91746300', 'Kẹo dẻo trái cây (fruit snacks)'), -- Candy, fruit snacks
    ('91746350', 'Kẹo trái cây ép dẻo (fruit leather)'), -- Candy, fruit leather
    ('91750000', 'Kẹo kéo (taffy)'), -- Candy, taffy
    ('91770060', 'Kẹo không sô-cô-la, không đường'), -- Candy, non chocolate, sugar free
    ('91801000', 'Kẹo cao su'), -- Chewing gum
    ('91802000', 'Kẹo cao su, không đường'), -- Chewing gum, sugar free
    ('92100000', 'Cà phê, không rõ loại'), -- Coffee, NS as to type
    ('92100500', 'Cà phê, không rõ pha hay hòa tan'), -- Coffee, NS as to brewed or instant
    ('92101000', 'Cà phê pha'), -- Coffee, brewed
    ('92101500', 'Cà phê pha, trộn loại thường và loại đã khử caffeine'), -- Coffee, brewed, blend of regular and decaffeinated
    ('92101600', 'Cà phê kiểu Thổ Nhĩ Kỳ'), -- Coffee, Turkish
    ('92101610', 'Cà phê espresso'), -- Coffee, espresso
    ('92101630', 'Cà phê espresso, đã khử caffeine'), -- Coffee, espresso, decaffeinated
    ('92101700', 'Cà phê pha, có hương vị'), -- Coffee, brewed, flavored
    ('92101800', 'Cà phê kiểu Cuba'), -- Coffee, Cuban
    ('92101810', 'Cà phê macchiato'), -- Coffee, macchiato
    ('92101820', 'Cà phê macchiato, có đường') -- Coffee, macchiato, sweetened
) AS t (source_food_code, name_vi)
WHERE f.source = 'USDA_FNDDS' AND f.source_food_code = t.source_food_code;

UPDATE nutrition_foods f SET name_vi = t.name_vi, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:usda-name-vi-v25'
FROM (VALUES
    ('92101850', 'Cà phê sữa cafe con leche'), -- Coffee, cafe con leche
    ('92101851', 'Cà phê sữa cafe con leche, đã khử caffeine'), -- Coffee, cafe con leche, decaffeinated
    ('92101900', 'Cà phê latte'), -- Coffee, Latte
    ('92101901', 'Cà phê latte, không béo'), -- Coffee, Latte, nonfat
    ('92101903', 'Cà phê latte, dùng sữa thực vật'), -- Coffee, Latte, with non-dairy milk
    ('92101904', 'Cà phê latte, có hương vị'), -- Coffee, Latte, flavored
    ('92101905', 'Cà phê latte, không béo, có hương vị'), -- Coffee, Latte, nonfat, flavored
    ('92101906', 'Cà phê latte, dùng sữa thực vật, có hương vị'), -- Coffee, Latte, with non-dairy milk, flavored
    ('92101910', 'Cà phê latte, đã khử caffeine'), -- Coffee, Latte, decaffeinated
    ('92101911', 'Cà phê latte, đã khử caffeine, không béo'), -- Coffee, Latte, decaffeinated, nonfat
    ('92101913', 'Cà phê latte, đã khử caffeine, dùng sữa thực vật'), -- Coffee, Latte, decaffeinated, with non-dairy milk
    ('92101917', 'Cà phê latte, đã khử caffeine, có hương vị'), -- Coffee, Latte, decaffeinated, flavored
    ('92101918', 'Cà phê latte, đã khử caffeine, không béo, có hương vị'), -- Coffee, Latte, decaffeinated, nonfat, flavored
    ('92101919', 'Cà phê latte, đã khử caffeine, dùng sữa thực vật, có hương vị'), -- Coffee, Latte, decaffeinated, with non-dairy milk, flavored
    ('92101920', 'Cà phê đá xay'), -- Frozen coffee drink
    ('92101921', 'Cà phê đá xay, không béo'), -- Frozen coffee drink, nonfat
    ('92101923', 'Cà phê đá xay, dùng sữa thực vật'), -- Frozen coffee drink, with non-dairy milk
    ('92101925', 'Cà phê đá xay, có kem tươi đánh bông'), -- Frozen coffee drink, with whipped cream
    ('92101926', 'Cà phê đá xay, không béo, có kem tươi đánh bông'), -- Frozen coffee drink, nonfat, with whipped cream
    ('92101928', 'Cà phê đá xay, dùng sữa thực vật, có kem tươi đánh bông'), -- Frozen coffee drink, with non-dairy milk and whipped cream
    ('92101930', 'Cà phê đá xay, đã khử caffeine'), -- Frozen coffee drink, decaffeinated
    ('92101931', 'Cà phê đá xay, đã khử caffeine, không béo'), -- Frozen coffee drink, decaffeinated, nonfat
    ('92101933', 'Cà phê đá xay, đã khử caffeine, dùng sữa thực vật'), -- Frozen coffee drink, decaffeinated, with non-dairy milk
    ('92101935', 'Cà phê đá xay, đã khử caffeine, có kem tươi đánh bông'), -- Frozen coffee drink, decaffeinated, with whipped cream
    ('92101936', 'Cà phê đá xay, đã khử caffeine, không béo, có kem tươi đánh bông'), -- Frozen coffee drink, decaffeinated, nonfat, with whipped cream
    ('92101938', 'Cà phê đá xay, đã khử caffeine, dùng sữa thực vật, có kem tươi đánh bông'), -- Frozen coffee drink, decaffeinated, with non-dairy milk and whipped cream
    ('92101950', 'Cà phê mocha'), -- Coffee, Cafe Mocha
    ('92101955', 'Cà phê mocha, không béo'), -- Coffee, Cafe Mocha, nonfat
    ('92101960', 'Cà phê mocha, dùng sữa thực vật'), -- Coffee, Cafe Mocha, with non-dairy milk
    ('92101965', 'Cà phê mocha, đã khử caffeine'), -- Coffee, Cafe Mocha, decaffeinated
    ('92101970', 'Cà phê mocha, đã khử caffeine, không béo'), -- Coffee, Cafe Mocha, decaffeinated, nonfat
    ('92101975', 'Cà phê mocha, đã khử caffeine, dùng sữa thực vật'), -- Coffee, Cafe Mocha, decaffeinated, with non-dairy milk
    ('92102000', 'Cà phê mocha đá xay'), -- Frozen mocha coffee drink
    ('92102010', 'Cà phê mocha đá xay, không béo'), -- Frozen mocha coffee drink, nonfat
    ('92102020', 'Cà phê mocha đá xay, dùng sữa thực vật'), -- Frozen mocha coffee drink, with non-dairy milk
    ('92102030', 'Cà phê mocha đá xay, có kem tươi đánh bông'), -- Frozen mocha coffee drink, with whipped cream
    ('92102040', 'Cà phê mocha đá xay, không béo, có kem tươi đánh bông'), -- Frozen mocha coffee drink, nonfat, with whipped cream
    ('92102050', 'Cà phê mocha đá xay, dùng sữa thực vật, có kem tươi đánh bông'), -- Frozen mocha coffee drink, with non-dairy milk and whipped cream
    ('92102060', 'Cà phê mocha đá xay, đã khử caffeine'), -- Frozen mocha coffee drink, decaffeinated
    ('92102070', 'Cà phê mocha đá xay, đã khử caffeine, không béo'), -- Frozen mocha coffee drink, decaffeinated, nonfat
    ('92102080', 'Cà phê mocha đá xay, đã khử caffeine, dùng sữa thực vật'), -- Frozen mocha coffee drink, decaffeinated, with non-dairy milk
    ('92102090', 'Cà phê mocha đá xay, đã khử caffeine, có kem tươi đánh bông'), -- Frozen mocha coffee drink, decaffeinated, with whipped cream
    ('92102100', 'Cà phê mocha đá xay, đã khử caffeine, không béo, có kem tươi đánh bông'), -- Frozen mocha coffee drink, decaffeinated, nonfat, with whipped cream
    ('92102110', 'Cà phê mocha đá xay, đã khử caffeine, dùng sữa thực vật, có kem tươi đánh bông'), -- Frozen mocha coffee drink, decaffeinated, with non-dairy milk and whipped cream
    ('92102400', 'Cà phê đá, pha'), -- Iced Coffee, brewed
    ('92102401', 'Cà phê đá, pha, đã khử caffeine'), -- Iced Coffee, brewed, decaffeinated
    ('92102450', 'Cà phê đá, có sẵn sữa và chất tạo ngọt'), -- Iced Coffee, pre-lightened and pre-sweetened
    ('92102500', 'Cà phê latte đá'), -- Coffee, Iced Latte
    ('92102501', 'Cà phê latte đá, không béo'), -- Coffee, Iced Latte, nonfat
    ('92102502', 'Cà phê latte đá, dùng sữa thực vật'), -- Coffee, Iced Latte, with non-dairy milk
    ('92102503', 'Cà phê latte đá, có hương vị'), -- Coffee, Iced Latte, flavored
    ('92102504', 'Cà phê latte đá, không béo, có hương vị'), -- Coffee, Iced Latte, nonfat, flavored
    ('92102505', 'Cà phê latte đá, dùng sữa thực vật, có hương vị'), -- Coffee, Iced Latte, with non-dairy milk, flavored
    ('92102510', 'Cà phê latte đá, đã khử caffeine'), -- Coffee, Iced Latte, decaffeinated
    ('92102511', 'Cà phê latte đá, đã khử caffeine, không béo'), -- Coffee, Iced Latte, decaffeinated, nonfat
    ('92102512', 'Cà phê latte đá, đã khử caffeine, dùng sữa thực vật'), -- Coffee, Iced Latte, decaffeinated, with non-dairy milk
    ('92102513', 'Cà phê latte đá, đã khử caffeine, có hương vị'), -- Coffee, Iced Latte, decaffeinated, flavored
    ('92102514', 'Cà phê latte đá, đã khử caffeine, không béo, có hương vị'), -- Coffee, Iced Latte, decaffeinated, nonfat, flavored
    ('92102515', 'Cà phê latte đá, đã khử caffeine, dùng sữa thực vật, có hương vị'), -- Coffee, Iced Latte, decaffeinated, with non-dairy milk, flavored
    ('92102600', 'Cà phê mocha đá'), -- Coffee, Iced Cafe Mocha
    ('92102601', 'Cà phê mocha đá, không béo'), -- Coffee, Iced Cafe Mocha, nonfat
    ('92102602', 'Cà phê mocha đá, dùng sữa thực vật'), -- Coffee, Iced Cafe Mocha, with non-dairy milk
    ('92102610', 'Cà phê mocha đá, đã khử caffeine'), -- Coffee, Iced Cafe Mocha, decaffeinated
    ('92102611', 'Cà phê mocha đá, đã khử caffeine, không béo'), -- Coffee, Iced Cafe Mocha, decaffeinated, nonfat
    ('92102612', 'Cà phê mocha đá, đã khử caffeine, dùng sữa thực vật'), -- Coffee, Iced Cafe Mocha, decaffeinated, with non-dairy milk
    ('92103000', 'Cà phê hòa tan, đã pha'), -- Coffee, instant, reconstituted
    ('92104000', 'Cà phê hòa tan, giảm 50% caffeine, đã pha'), -- Coffee, instant, 50% less caffeine, reconstituted
    ('92111000', 'Cà phê, không rõ pha hay hòa tan, đã khử caffeine'), -- Coffee, NS as to brewed or instant, decaffeinated
    ('92111010', 'Cà phê pha, đã khử caffeine'), -- Coffee, brewed, decaffeinated
    ('92114000', 'Cà phê hòa tan, đã khử caffeine, đã pha'), -- Coffee, instant, decaffeinated, reconstituted
    ('92121000', 'Cà phê hòa tan, có sẵn sữa và đường, đã pha'), -- Coffee, instant, pre-lightened and pre-sweetened with sugar, reconstituted
    ('92121001', 'Cà phê hòa tan, đã khử caffeine, có sẵn sữa và đường, đã pha'), -- Coffee, instant, decaffeinated, pre-lightened and pre-sweetened with sugar, reconsitituted
    ('92121010', 'Cà phê hòa tan, có sẵn đường, đã pha'), -- Coffee, instant, pre-sweetened with sugar, reconstituted
    ('92121030', 'Cà phê mocha hòa tan, có sẵn sữa và chất tạo ngọt ít calo, đã pha'), -- Coffee, mocha, instant, pre-lightened and pre-sweetened with low calorie sweetener, reconstituted
    ('92121040', 'Cà phê hòa tan, có sẵn sữa và chất tạo ngọt ít calo, đã pha'), -- Coffee, instant, pre-lightened and pre-sweetened with low calorie sweetener, reconstituted
    ('92121041', 'Cà phê hòa tan, đã khử caffeine, có sẵn sữa và chất tạo ngọt ít calo, đã pha'), -- Coffee, instant, decaffeinated, pre-lightened and pre-sweetened with low calorie sweetener, reconstituted
    ('92130000', 'Cà phê, có sẵn sữa và đường'), -- Coffee, pre-lightened and pre-sweetened with sugar
    ('92130001', 'Cà phê, đã khử caffeine, có sẵn sữa và đường'), -- Coffee, decaffeinated, pre-lightened and pre-sweetened with sugar
    ('92130005', 'Cà phê, có sẵn sữa và chất tạo ngọt ít calo'), -- Coffee, pre-lightened and pre-sweetened with low calorie sweetener
    ('92130006', 'Cà phê, đã khử caffeine, có sẵn sữa và chất tạo ngọt ít calo'), -- Coffee, decaffeinated, pre-lightened and pre-sweetened with low calorie sweetener
    ('92130010', 'Cà phê, có sẵn sữa'), -- Coffee, pre-lightened
    ('92130011', 'Cà phê, đã khử caffeine, có sẵn sữa'), -- Coffee, decaffeinated, pre-lightened
    ('92130020', 'Cà phê, có sẵn đường'), -- Coffee, pre-sweetened with sugar
    ('92130021', 'Cà phê, đã khử caffeine, có sẵn đường'), -- Coffee, decaffeinated, pre-sweetened with sugar
    ('92130030', 'Cà phê, có sẵn chất tạo ngọt ít calo'), -- Coffee, pre-sweetened with low calorie sweetener
    ('92130031', 'Cà phê, đã khử caffeine, có sẵn chất tạo ngọt ít calo'), -- Coffee, decaffeinated, pre-sweetened with low calorie sweetener
    ('92152000', 'Cà phê và rễ diếp xoăn (chicory), pha'), -- Coffee and chicory, brewed
    ('92152010', 'Cà phê và rễ diếp xoăn (chicory), pha, đã khử caffeine'), -- Coffee and chicory, brewed, decaffeinated
    ('92161000', 'Cà phê cappuccino'), -- Coffee, Cappuccino
    ('92161001', 'Cà phê cappuccino, không béo'), -- Coffee, Cappuccino, nonfat
    ('92161002', 'Cà phê cappuccino, dùng sữa thực vật'), -- Coffee, Cappuccino, with non-dairy milk
    ('92162000', 'Cà phê cappuccino, đã khử caffeine'), -- Coffee, Cappuccino, decaffeinated
    ('92162001', 'Cà phê cappuccino, đã khử caffeine, không béo'), -- Coffee, Cappuccino, decaffeinated, nonfat
    ('92162002', 'Cà phê cappuccino, đã khử caffeine, dùng sữa thực vật'), -- Coffee, Cappuccino, decaffeinated, with non-dairy milk
    ('92171000', 'Cà phê đóng chai/lon'), -- Coffee, bottled/canned
    ('92171010', 'Cà phê đóng chai/lon, loại nhẹ'), -- Coffee, bottled/canned, light
    ('92191100', 'Cà phê hòa tan, chưa pha'), -- Coffee, instant, not reconstituted
    ('92191200', 'Cà phê hòa tan, đã khử caffeine, chưa pha'), -- Coffee, instant, decaffeinated, not reconstituted
    ('92191400', 'Cà phê hòa tan, có sẵn đường, chưa pha'), -- Coffee, instant, pre-sweetened with sugar, not reconstituted
    ('92193000', 'Cà phê hòa tan, có sẵn sữa và đường, chưa pha'), -- Coffee, instant, pre-lightened and pre-sweetened with sugar, not reconstituted
    ('92193005', 'Cà phê hòa tan, đã khử caffeine, có sẵn sữa và đường, chưa pha'), -- Coffee, instant, decaffeinated, pre-lightened and pre-sweetened with sugar, not reconstituted
    ('92193025', 'Cà phê hòa tan, đã khử caffeine, có sẵn sữa và chất tạo ngọt ít calo, chưa pha'), -- Coffee, instant, decaffeinated, pre-lightened and pre-sweetened with low calorie sweetener, not reconstituted
    ('92201010', 'Đồ uống thay thế cà phê'), -- Coffee substitute
    ('92202010', 'Đồ uống rễ diếp xoăn (chicory)'), -- Chicory beverage
    ('92302000', 'Trà đen pha từ lá, uống nóng'), -- Tea, hot, leaf, black
    ('92302500', 'Trà đen pha từ lá, uống nóng, đã khử caffeine'), -- Tea, hot, leaf, black, decaffeinated
    ('92303010', 'Trà xanh pha từ lá, uống nóng'), -- Tea, hot, leaf, green
    ('92303100', 'Trà xanh pha từ lá, uống nóng, đã khử caffeine'), -- Tea, hot, leaf, green, decaffeinated
    ('92304100', 'Trà ô long pha từ lá, uống nóng'), -- Tea, hot, leaf, oolong
    ('92305010', 'Trà đen hòa tan, uống lạnh, không đường'), -- Tea, iced, instant, black, unsweetened
    ('92305040', 'Trà đen hòa tan, uống lạnh, có sẵn đường'), -- Tea, iced, instant, black, pre-sweetened with sugar
    ('92305050', 'Trà đen hòa tan, uống lạnh, đã khử caffeine, có sẵn đường'), -- Tea, iced, instant, black, decaffeinated, pre-sweetened with sugar
    ('92305090', 'Trà đen hòa tan, uống lạnh, có sẵn chất tạo ngọt ít calo'), -- Tea, iced, instant, black, pre-sweetened with low calorie sweetener
    ('92305110', 'Trà đen hòa tan, uống lạnh, đã khử caffeine, có sẵn chất tạo ngọt ít calo'), -- Tea, iced, instant, black, decaffeinated, pre-sweetened with low calorie sweetener
    ('92305180', 'Trà đen hòa tan, uống lạnh, đã khử caffeine, không đường'), -- Tea, iced, instant, black, decaffeinated, unsweetened
    ('92305900', 'Trà xanh hòa tan, uống lạnh, không đường'), -- Tea, iced, instant, green, unsweetened
    ('92305910', 'Trà xanh hòa tan, uống lạnh, có sẵn đường'), -- Tea, iced, instant, green, pre-sweetened with sugar
    ('92305920', 'Trà xanh hòa tan, uống lạnh, có sẵn chất tạo ngọt ít calo'), -- Tea, iced, instant, green, pre-sweetened with low calorie sweetener
    ('92306000', 'Trà thảo mộc, uống nóng'), -- Tea, hot, herbal
    ('92306090', 'Trà hoa bụp giấm (hibiscus), uống nóng'), -- Tea, hot, hibiscus
    ('92306100', 'Đồ uống từ ngô'), -- Corn beverage
    ('92306700', 'Trà hoa cúc (chamomile), uống nóng'), -- Tea, hot, chamomile
    ('92306800', 'Trà pha sữa, uống nóng'), -- Tea, hot, with milk
    ('92306850', 'Trà gừng'), -- Tea, ginger
    ('92306910', 'Trà sữa trân châu (bubble tea)'), -- Tea, bubble
    ('92306920', 'Trà kombucha'), -- Tea, kombucha
    ('92307000', 'Trà đen hòa tan, uống lạnh, không đường, dạng bột khô'), -- Tea, iced, instant, black, unsweetened, dry
    ('92307400', 'Trà đen hòa tan, uống lạnh, có sẵn chất tạo ngọt, dạng bột khô'), -- Tea, iced, instant, black, pre-sweetened, dry
    ('92307500', 'Trà đá / nước chanh, dạng nước ép trái cây pha'), -- Iced Tea / Lemonade juice drink
    ('92307510', 'Trà đá / nước chanh, dạng nước ép trái cây pha, loại nhẹ'), -- Iced Tea / Lemonade juice drink, light
    ('92307520', 'Trà đá / nước chanh, dạng nước ép trái cây pha, ăn kiêng'), -- Iced Tea / Lemonade juice drink, diet
    ('92308000', 'Trà đen pha, uống lạnh, có sẵn đường'), -- Tea, iced, brewed, black, pre-sweetened with sugar
    ('92308010', 'Trà đen pha, uống lạnh, có sẵn chất tạo ngọt ít calo'), -- Tea, iced, brewed, black, pre-sweetened with low calorie sweetener
    ('92308020', 'Trà đen pha, uống lạnh, không đường'), -- Tea, iced, brewed, black, unsweetened
    ('92308030', 'Trà đen pha, uống lạnh, đã khử caffeine, có sẵn đường'), -- Tea, iced, brewed, black, decaffeinated, pre-sweetened with sugar
    ('92308040', 'Trà đen pha, uống lạnh, đã khử caffeine, có sẵn chất tạo ngọt ít calo'), -- Tea, iced, brewed, black, decaffeinated, pre-sweetened with low calorie sweetener
    ('92308050', 'Trà đen pha, uống lạnh, đã khử caffeine, không đường'), -- Tea, iced, brewed, black, decaffeinated, unsweetened
    ('92308500', 'Trà xanh pha, uống lạnh, có sẵn đường'), -- Tea, iced, brewed, green, pre-sweetened with sugar
    ('92308510', 'Trà xanh pha, uống lạnh, có sẵn chất tạo ngọt ít calo'), -- Tea, iced, brewed, green, pre-sweetened with low calorie sweetener
    ('92308520', 'Trà xanh pha, uống lạnh, không đường'), -- Tea, iced, brewed, green, unsweetened
    ('92308530', 'Trà xanh pha, uống lạnh, đã khử caffeine, có sẵn đường'), -- Tea, iced, brewed, green, decaffeinated, pre-sweetened with sugar
    ('92308540', 'Trà xanh pha, uống lạnh, đã khử caffeine, có sẵn chất tạo ngọt ít calo'), -- Tea, iced, brewed, green, decaffeinated, pre-sweetened with low calorie sweetener
    ('92308550', 'Trà xanh pha, uống lạnh, đã khử caffeine, không đường'), -- Tea, iced, brewed, green, decaffeinated, unsweetened
    ('92309000', 'Trà đen đóng chai, uống lạnh'), -- Tea, iced, bottled, black
    ('92309010', 'Trà đen đóng chai, uống lạnh, đã khử caffeine'), -- Tea, iced, bottled, black, decaffeinated
    ('92309020', 'Trà đen đóng chai, uống lạnh, ăn kiêng'), -- Tea, iced, bottled, black, diet
    ('92309030', 'Trà đen đóng chai, uống lạnh, đã khử caffeine, ăn kiêng'), -- Tea, iced, bottled, black, decaffeinated, diet
    ('92309040', 'Trà đen đóng chai, uống lạnh, không đường'), -- Tea, iced, bottled, black, unsweetened
    ('92309050', 'Trà đen đóng chai, uống lạnh, đã khử caffeine, không đường'), -- Tea, iced, bottled, black, decaffeinated, unsweetened
    ('92309500', 'Trà xanh đóng chai, uống lạnh'), -- Tea, iced, bottled, green
    ('92309510', 'Trà xanh đóng chai, uống lạnh, ăn kiêng'), -- Tea, iced, bottled, green, diet
    ('92309520', 'Trà xanh đóng chai, uống lạnh, không đường'), -- Tea, iced, bottled, green, unsweetened
    ('92400000', 'Nước ngọt có ga, loại chung'), -- Soft drink, NFS
    ('92400100', 'Nước ngọt có ga, loại chung, ăn kiêng'), -- Soft drink, NFS, diet
    ('92410110', 'Nước tonic'), -- Water, tonic
    ('92410210', 'Nước có ga, không hương vị'), -- Water, carbonated, plain
    ('92410250', 'Nước có ga, có hương vị'), -- Water, carbonated, flavored
    ('92410310', 'Nước ngọt có ga, cola'), -- Soft drink, cola
    ('92410320', 'Nước ngọt có ga, cola, ăn kiêng'), -- Soft drink, cola, diet
    ('92410340', 'Nước ngọt có ga, cola, đã khử caffeine'), -- Soft drink, cola, decaffeinated
    ('92410350', 'Nước ngọt có ga, cola, đã khử caffeine, ăn kiêng'), -- Soft drink, cola, decaffeinated, diet
    ('92410360', 'Nước ngọt có ga, loại pepper (kiểu Dr Pepper)'), -- Soft drink, pepper type
    ('92410370', 'Nước ngọt có ga, loại pepper (kiểu Dr Pepper), ăn kiêng'), -- Soft drink, pepper type, diet
    ('92410390', 'Nước ngọt có ga, loại pepper (kiểu Dr Pepper), đã khử caffeine'), -- Soft drink, pepper type, decaffeinated
    ('92410400', 'Nước ngọt có ga, loại pepper (kiểu Dr Pepper), đã khử caffeine, ăn kiêng'), -- Soft drink, pepper type, decaffeinated, diet
    ('92410410', 'Nước ngọt có ga, cream soda'), -- Soft drink, cream soda
    ('92410420', 'Nước ngọt có ga, cream soda, ăn kiêng'), -- Soft drink, cream soda, diet
    ('92410510', 'Nước ngọt có ga, hương trái cây, không caffeine'), -- Soft drink, fruit flavored, caffeine free
    ('92410520', 'Nước ngọt có ga, hương trái cây, ăn kiêng, không caffeine'), -- Soft drink, fruit flavored, diet, caffeine free
    ('92410550', 'Nước ngọt có ga, hương trái cây, có caffeine'), -- Soft drink, fruit flavored, caffeine containing
    ('92410560', 'Nước ngọt có ga, hương trái cây, có caffeine, ăn kiêng'), -- Soft drink, fruit flavored, caffeine containing, diet
    ('92410610', 'Nước ngọt có ga, ginger ale (vị gừng)'), -- Soft drink, ginger ale
    ('92410620', 'Nước ngọt có ga, ginger ale (vị gừng), ăn kiêng'), -- Soft drink, ginger ale, diet
    ('92410710', 'Nước ngọt có ga, root beer'), -- Soft drink, root beer
    ('92410720', 'Nước ngọt có ga, root beer, ăn kiêng'), -- Soft drink, root beer, diet
    ('92410810', 'Nước ngọt có ga, hương sô-cô-la'), -- Soft drink, chocolate flavored
    ('92410820', 'Nước ngọt có ga, hương sô-cô-la, ăn kiêng'), -- Soft drink, chocolate flavored, diet
    ('92411510', 'Nước ngọt có ga, cola, hương trái cây hoặc vani'), -- Soft drink, cola, fruit or vanilla flavored
    ('92411520', 'Nước ngọt có ga, cola, hương sô-cô-la'), -- Soft drink, cola, chocolate flavored
    ('92411610', 'Nước ngọt có ga, cola, hương trái cây hoặc vani, ăn kiêng'), -- Soft drink, cola, fruit or vanilla flavored, diet
    ('92411620', 'Nước ngọt có ga, cola, hương sô-cô-la, ăn kiêng'), -- Soft drink, cola, chocolate flavored, diet
    ('92432000', 'Nước ép trái cây pha, họ cam quýt, có ga'), -- Fruit juice drink, citrus, carbonated
    ('92433000', 'Nước ép trái cây pha, không thuộc họ cam quýt, có ga'), -- Fruit juice drink, noncitrus, carbonated
    ('92510610', 'Nước ép trái cây pha'), -- Fruit juice drink
    ('92510650', 'Nước me'), -- Tamarind drink
    ('92510720', 'Nước punch trái cây, pha từ nước ép trái cây và soda'), -- Fruit punch, made with fruit juice and soda
    ('92510955', 'Nước chanh, dạng nước ép trái cây pha'), -- Lemonade, fruit juice drink
    ('92510960', 'Nước chanh, dạng nước hương trái cây'), -- Lemonade, fruit flavored drink
    ('92511000', 'Nước chanh, cô đặc đông lạnh, chưa pha'), -- Lemonade, frozen concentrate, not reconstituted
    ('92511015', 'Nước hương trái cây'), -- Fruit flavored drink
    ('92512090', 'Cocktail Pina Colada, không cồn'), -- Pina Colada, nonalcoholic
    ('92512110', 'Hỗn hợp pha margarita, không cồn'), -- Margarita mix, nonalcoholic
    ('92513000', 'Đồ uống đá xay (slush)'), -- Slush frozen drink
    ('92513010', 'Đồ uống đá xay (slush), không thêm đường'), -- Slush frozen drink, no sugar added
    ('92530410', 'Nước hương trái cây, giàu vitamin C'), -- Fruit flavored drink, with high vitamin C
    ('92530510', 'Nước ép nam việt quất pha, giàu vitamin C'), -- Cranberry juice drink, with high vitamin C
    ('92530610', 'Nước ép trái cây pha, giàu vitamin C'), -- Fruit juice drink, with high vitamin C
    ('92530950', 'Nước ép rau và trái cây pha, giàu vitamin C'), -- Vegetable and fruit juice drink, with high vitamin C
    ('92531030', 'Nước ép trái cây pha (Sunny D)'), -- Fruit juice drink (Sunny D)
    ('92541010', 'Nước hương trái cây, dạng bột, đã pha'), -- Fruit flavored drink, powdered, reconstituted
    ('92542000', 'Nước hương trái cây, giàu vitamin C, dạng bột, đã pha'), -- Fruit flavored drink, with high vitamin C, powdered, reconstituted
    ('92550030', 'Nước ép trái cây pha, giàu vitamin C, loại nhẹ'), -- Fruit juice drink, with high vitamin C, light
    ('92550035', 'Nước ép trái cây pha, loại nhẹ'), -- Fruit juice drink, light
    ('92550040', 'Nước ép trái cây pha, ăn kiêng'), -- Fruit juice drink, diet
    ('92550110', 'Nước ép nam việt quất pha, giàu vitamin C, loại nhẹ'), -- Cranberry juice drink, with high vitamin C, light
    ('92550200', 'Nước ép nho pha, loại nhẹ'), -- Grape juice drink, light
    ('92550350', 'Đồ uống nước cam, 40-50% nước ép, loại nhẹ'), -- Orange juice beverage, 40-50% juice, light
    ('92550360', 'Đồ uống nước táo, 40-50% nước ép, loại nhẹ'), -- Apple juice beverage, 40-50% juice, light
    ('92550370', 'Nước chanh, dạng nước ép trái cây pha, loại nhẹ'), -- Lemonade, fruit juice drink, light
    ('92550380', 'Đồ uống nước lựu, 40-50% nước ép, loại nhẹ'), -- Pomegranate juice beverage, 40-50% juice, light
    ('92550400', 'Nước ép rau và trái cây pha, giàu vitamin C, ăn kiêng'), -- Vegetable and fruit juice drink, with high vitamin C, diet
    ('92550405', 'Nước ép rau và trái cây pha, giàu vitamin C, loại nhẹ'), -- Vegetable and fruit juice drink, with high vitamin C, light
    ('92550610', 'Nước hương trái cây, giàu vitamin C, ăn kiêng'), -- Fruit flavored drink, with high vitamin C, diet
    ('92550620', 'Nước hương trái cây, ăn kiêng'), -- Fruit flavored drink, diet
    ('92552000', 'Nước hương trái cây, giàu vitamin C, dạng bột, đã pha, ăn kiêng'), -- Fruit flavored drink, with high vitamin C, powdered, reconstituted, diet
    ('92552010', 'Nước hương trái cây, dạng bột, đã pha, ăn kiêng'), -- Fruit flavored drink, powdered, reconstituted, diet
    ('92552030', 'Nước ép trái cây pha (Capri Sun)'), -- Fruit juice drink (Capri Sun)
    ('92582110', 'Nước ép trái cây pha, bổ sung canxi (Sunny D)'), -- Fruit juice drink, added calcium (Sunny D)
    ('92610020', 'Nước gạo horchata (Mexico), pha với nước'), -- Horchata, made with water
    ('92610030', 'Nước gạo horchata (Mexico), pha với sữa'), -- Horchata, made with milk
    ('92611010', 'Đồ uống yến mạch Frescavena'), -- Frescavena
    ('92611100', 'Đồ uống atole yến mạch (atole de avena)'), -- Atole de avena
    ('92612010', 'Nước mía'), -- Sugar cane beverage
    ('92613010', 'Đồ uống atole (bột ngô, Mexico)'), -- Atole
    ('92613510', 'Đồ uống atole sô-cô-la (atole de chocolate)'), -- Atole de chocolate
    ('92801000', 'Rượu vang, không cồn'), -- Wine, nonalcoholic
    ('92803000', 'Bia, không cồn'), -- Beer, nonalcoholic
    ('92804000', 'Đồ uống không cồn Shirley Temple'), -- Shirley Temple
    ('92900100', 'Nước hương trái cây, giàu vitamin C, dạng bột, chưa pha'), -- Fruit flavored drink, with high vitamin C, powdered, not reconstituted
    ('92900110', 'Nước hương trái cây, dạng bột, chưa pha'), -- Fruit flavored drink, powdered, not reconstituted
    ('92900200', 'Nước hương trái cây, dạng bột, chưa pha, ăn kiêng'), -- Fruit flavored drink, powdered, not reconstituted, diet
    ('92900300', 'Nước uống thể thao, dạng bột cô đặc, chưa pha'), -- Sports drink, dry concentrate, not reconstituted
    ('93101000', 'Bia'), -- Beer
    ('93102000', 'Bia, loại nhẹ'), -- Beer, light
    ('93102300', 'Bia, nồng độ cồn cao'), -- Beer, higher alcohol
    ('93106000', 'Đồ uống mạch nha có cồn, có đường'), -- Alcoholic malt beverage, sweetened
    ('93106100', 'Đồ uống mạch nha có cồn'), -- Alcoholic malt beverage
    ('93106500', 'Rượu táo lên men (hard cider)'), -- Hard cider
    ('93106600', 'Nước có ga có cồn (hard seltzer)'), -- Hard seltzer
    ('93201000', 'Rượu mùi (liqueur)'), -- Liqueur
    ('93201010', 'Rượu mùi kem (cream liqueur)'), -- Liqueur, cream
    ('93202000', 'Rượu mùi, hương cà phê'), -- Liqueur, coffee flavored
    ('93301000', 'Cocktail, loại chung'), -- Cocktail, NFS
    ('93301010', 'Cocktail Brandy Alexander'), -- Brandy Alexander
    ('93301030', 'Cocktail Bloody Mary'), -- Bloody Mary
    ('93301032', 'Cocktail Cape Cod'), -- Cape Cod
    ('93301040', 'Cocktail Daiquiri'), -- Daiquiri
    ('93301045', 'Thạch (gelatin) có cồn, dạng shot'), -- Gelatin shot, alcoholic
    ('93301050', 'Cocktail Gimlet'), -- Gimlet
    ('93301060', 'Rượu gin pha nước tonic'), -- Gin and tonic
    ('93301075', 'Cocktail Greyhound'), -- Greyhound
    ('93301083', 'Cocktail Jagerbomb'), -- Jagerbomb
    ('93301085', 'Cocktail Kamikaze'), -- Kamikaze
    ('93301090', 'Cocktail Manhattan'), -- Manhattan
    ('93301100', 'Cocktail Margarita'), -- Margarita
    ('93301110', 'Cocktail Martini'), -- Martini
    ('93301111', 'Cocktail Martini, có hương vị'), -- Martini, flavored
    ('93301113', 'Cocktail bia Michelada'), -- Michelada
    ('93301115', 'Cocktail Mimosa'), -- Mimosa
    ('93301120', 'Cocktail Mint julep'), -- Mint julep
    ('93301125', 'Cocktail Mojito'), -- Mojito
    ('93301127', 'Cocktail Moscow mule'), -- Moscow mule
    ('93301130', 'Cocktail Old fashioned'), -- Old fashioned
    ('93301132', 'Cocktail Orange Blossom'), -- Orange Blossom
    ('93301135', 'Cocktail Rob Roy'), -- Rob Roy
    ('93301136', 'Cocktail Rusty Nail'), -- Rusty Nail
    ('93301140', 'Cocktail Screwdriver'), -- Screwdriver
    ('93301141', 'Cocktail Seabreeze'), -- Seabreeze
    ('93301142', 'Cocktail Seven and Seven'), -- Seven and Seven
    ('93301150', 'Cocktail Tom Collins'), -- Tom Collins
    ('93301160', 'Cocktail Whiskey sour'), -- Whiskey sour
    ('93301170', 'Rượu whiskey pha soda'), -- Whiskey and soda
    ('93301181', 'Rượu whiskey pha nước'), -- Whiskey and water
    ('93301182', 'Rượu whiskey pha cola'), -- Whiskey and cola
    ('93301183', 'Rượu whiskey pha cola ăn kiêng'), -- Whiskey and diet cola
    ('93301184', 'Rượu whiskey pha ginger ale'), -- Whiskey and ginger ale
    ('93301190', 'Rượu rum pha cola'), -- Rum and cola
    ('93301191', 'Rượu rum pha cola ăn kiêng'), -- Rum and diet cola
    ('93301200', 'Cocktail Pina Colada'), -- Pina Colada
    ('93301205', 'Rượu brandy pha cola'), -- Brandy and cola
    ('93301211', 'Rượu vodka pha soda'), -- Vodka and soda
    ('93301213', 'Rượu vodka pha nước chanh'), -- Vodka and lemonade
    ('93301214', 'Rượu vodka pha cola'), -- Vodka and cola
    ('93301215', 'Rượu vodka pha cola ăn kiêng'), -- Vodka and diet cola
    ('93301216', 'Rượu vodka pha nước tăng lực'), -- Vodka and energy drink
    ('93301217', 'Rượu vodka pha nước'), -- Vodka and water
    ('93301218', 'Rượu vodka pha nước tonic'), -- Vodka and tonic
    ('93301230', 'Cocktail Sloe gin fizz'), -- Sloe gin fizz
    ('93301240', 'Cocktail Black Russian'), -- Black Russian
    ('93301250', 'Cocktail White Russian'), -- White Russian
    ('93301270', 'Nước punch trái cây, có cồn'), -- Fruit punch, alcoholic
    ('93301275', 'Nước punch sâm panh'), -- Champagne punch
    ('93301280', 'Cocktail Singapore Sling'), -- Singapore Sling
    ('93301310', 'Cocktail Mai Tai'), -- Mai Tai
    ('93301320', 'Cocktail Tequila Sunrise'), -- Tequila Sunrise
    ('93301360', 'Cocktail Long Island iced tea'), -- Long Island iced tea
    ('93301370', 'Cocktail Fuzzy Navel'), -- Fuzzy Navel
    ('93301400', 'Đồ uống cà phê có cồn'), -- Alcoholic coffee drink
    ('93301500', 'Cocktail Daiquiri đá xay'), -- Frozen daiquiri
    ('93301510', 'Cocktail Margarita đá xay'), -- Frozen margarita
    ('93301550', 'Đồ uống trứng sữa (eggnog), có cồn'), -- Eggnog, alcoholic
    ('93301600', 'Cocktail Gin fizz'), -- Gin fizz
    ('93302000', 'Rượu rum nóng pha bơ (hot buttered rum)'), -- Rum, hot buttered
    ('93401005', 'Rượu vang nổ (sủi bọt)'), -- Wine, sparkling
    ('93401010', 'Rượu vang đỏ'), -- Wine, red
    ('93401020', 'Rượu vang trắng'), -- Wine, white
    ('93401030', 'Rượu vang hồng'), -- Wine, rose
    ('93401100', 'Rượu gạo'), -- Wine, rice
    ('93402000', 'Rượu vang tráng miệng, ngọt'), -- Wine, dessert, sweet
    ('93403000', 'Rượu vang, loại nhẹ'), -- Wine, light
    ('93404000', 'Rượu vang pha nước trái cây (wine cooler)'), -- Wine cooler
    ('93404550', 'Rượu sangria, vang đỏ'), -- Sangria, red
    ('93404560', 'Rượu sangria, vang trắng'), -- Sangria, white
    ('93405000', 'Rượu vang pha soda (spritzer)'), -- Wine spritzer
    ('93406000', 'Rượu vang nóng pha gia vị (glug, Bắc Âu)'), -- Glug
    ('93501000', 'Rượu brandy'), -- Brandy
    ('93502000', 'Rượu whiskey'), -- Whiskey
    ('93502100', 'Rượu whisky Scotch'), -- Scotch
    ('93503000', 'Rượu gin'), -- Gin
    ('93504000', 'Rượu rum'), -- Rum
    ('93505000', 'Rượu vodka'), -- Vodka
    ('93505100', 'Rượu tequila'), -- Tequila
    ('94000010', 'Nước, loại chung'), -- Water, NFS
    ('94000100', 'Nước máy'), -- Water, tap
    ('94100100', 'Nước đóng chai, không hương vị'), -- Water, bottled, plain
    ('94100200', 'Nước không ga, có hương vị'), -- Water, non-carbonated, flavored
    ('94100300', 'Nước lọc pha hương trái cây'), -- Water beverage, fruit flavored
    ('94200100', 'Nước bổ sung vi chất (enhanced water), loại thường'), -- Water, enhanced, regular
    ('94200200', 'Nước bổ sung vi chất (enhanced water), ăn kiêng'), -- Water, enhanced, diet
    ('94300100', 'Nước dành cho trẻ nhỏ'), -- Water, baby
    ('95101000', 'Đồ uống dinh dưỡng, uống liền (Boost)'), -- Nutritional drink or shake, ready-to-drink (Boost)
    ('95101010', 'Đồ uống dinh dưỡng, uống liền (Boost Plus)'), -- Nutritional drink or shake, ready-to-drink (Boost Plus)
    ('95102000', 'Đồ uống dinh dưỡng, uống liền (Carnation Instant Breakfast)'), -- Nutritional drink or shake, ready-to-drink (Carnation Instant Breakfast)
    ('95103000', 'Đồ uống dinh dưỡng, uống liền (Ensure)'), -- Nutritional drink or shake, ready-to-drink (Ensure)
    ('95103010', 'Đồ uống dinh dưỡng, uống liền (Ensure Plus)'), -- Nutritional drink or shake, ready-to-drink (Ensure Plus)
    ('95104000', 'Đồ uống dinh dưỡng, uống liền, không đường (Glucerna)'), -- Nutritional drink or shake, ready-to-drink, sugar free (Glucerna)
    ('95106000', 'Đồ uống dinh dưỡng, uống liền (Muscle Milk)'), -- Nutritional drink or shake, ready-to-drink (Muscle Milk)
    ('95106010', 'Đồ uống dinh dưỡng, uống liền, loại nhẹ (Muscle Milk)'), -- Nutritional drink or shake, ready-to-drink, light (Muscle Milk)
    ('95110000', 'Đồ uống dinh dưỡng, uống liền (Slim Fast)'), -- Nutritional drink or shake, ready-to-drink (Slim Fast)
    ('95110010', 'Đồ uống dinh dưỡng, uống liền, không đường (Slim Fast)'), -- Nutritional drink or shake, ready-to-drink, sugar free (Slim Fast)
    ('95110020', 'Đồ uống dinh dưỡng, giàu đạm, uống liền (Slim Fast)'), -- Nutritional drink or shake, high protein, ready-to-drink (Slim Fast)
    ('95120000', 'Đồ uống dinh dưỡng, uống liền, loại chung'), -- Nutritional drink or shake, ready-to-drink, NFS
    ('95120010', 'Đồ uống dinh dưỡng, giàu đạm, uống liền, loại chung'), -- Nutritional drink or shake, high protein, ready-to-drink, NFS
    ('95120020', 'Đồ uống dinh dưỡng, giàu đạm, loại nhẹ, uống liền, loại chung'), -- Nutritional drink or shake, high protein, light, ready-to-drink, NFS
    ('95120050', 'Đồ uống dinh dưỡng, dạng lỏng, nền đậu tương'), -- Nutritional drink or shake, liquid, soy-based
    ('95201000', 'Bột pha dinh dưỡng (Carnation Instant Breakfast)'), -- Nutritional powder mix (Carnation Instant Breakfast)
    ('95201010', 'Bột pha dinh dưỡng, không đường (Carnation Instant Breakfast)'), -- Nutritional powder mix, sugar free (Carnation Instant Breakfast)
    ('95201200', 'Bột pha dinh dưỡng (EAS Whey Protein Powder)'), -- Nutritional powder mix (EAS Whey Protein Powder)
    ('95201300', 'Bột pha dinh dưỡng (EAS Soy Protein Powder)'), -- Nutritional powder mix (EAS Soy Protein Powder)
    ('95201500', 'Bột pha dinh dưỡng, giàu đạm (Herbalife)'), -- Nutritional powder mix, high protein (Herbalife)
    ('95201600', 'Bột pha dinh dưỡng (Isopure)'), -- Nutritional powder mix (Isopure)
    ('95202000', 'Bột pha dinh dưỡng (Muscle Milk)'), -- Nutritional powder mix (Muscle Milk)
    ('95202010', 'Bột pha dinh dưỡng, loại nhẹ (Muscle Milk)'), -- Nutritional powder mix, light (Muscle Milk)
    ('95210000', 'Bột pha dinh dưỡng (Slim Fast)'), -- Nutritional powder mix (Slim Fast)
    ('95210010', 'Bột pha dinh dưỡng, không đường (Slim Fast)'), -- Nutritional powder mix, sugar free (Slim Fast)
    ('95210020', 'Bột pha dinh dưỡng, giàu đạm (Slim Fast)'), -- Nutritional powder mix, high protein (Slim Fast)
    ('95220000', 'Bột pha dinh dưỡng, loại chung'), -- Nutritional powder mix, NFS
    ('95220010', 'Bột pha dinh dưỡng, giàu đạm, loại chung'), -- Nutritional powder mix, high protein, NFS
    ('95230000', 'Bột pha dinh dưỡng, nền whey, loại chung'), -- Nutritional powder mix, whey based, NFS
    ('95230010', 'Bột pha dinh dưỡng, bột protein, nền đậu tương, loại chung'), -- Nutritional powder mix, protein, soy based, NFS
    ('95230020', 'Bột pha dinh dưỡng, bột protein, loại nhẹ, loại chung'), -- Nutritional powder mix, protein, light, NFS
    ('95230030', 'Bột pha dinh dưỡng, bột protein, loại chung'), -- Nutritional powder mix, protein, NFS
    ('95310200', 'Nước tăng lực (Full Throttle)'), -- Energy drink (Full Throttle)
    ('95310400', 'Nước tăng lực (Monster)'), -- Energy drink (Monster)
    ('95310500', 'Nước tăng lực (Mountain Dew AMP)'), -- Energy drink (Mountain Dew AMP)
    ('95310550', 'Nước tăng lực (No Fear)'), -- Energy drink (No Fear)
    ('95310555', 'Nước tăng lực (No Fear Motherload)'), -- Energy drink (No Fear Motherload)
    ('95310560', 'Nước tăng lực (NOS)'), -- Energy drink (NOS)
    ('95310600', 'Nước tăng lực (Red Bull)'), -- Energy drink (Red Bull)
    ('95310700', 'Nước tăng lực (Rockstar)'), -- Energy drink (Rockstar)
    ('95310750', 'Nước tăng lực (SoBe Energize Energy Juice Drink)'), -- Energy drink (SoBe Energize Energy Juice Drink)
    ('95310800', 'Nước tăng lực (Vault)'), -- Energy drink (Vault)
    ('95311000', 'Nước tăng lực'), -- Energy Drink
    ('95312400', 'Nước tăng lực, ít calo (Monster)'), -- Energy drink, low calorie (Monster)
    ('95312410', 'Nước tăng lực, không đường (Monster)'), -- Energy drink, sugar free (Monster)
    ('95312500', 'Nước tăng lực, không đường (Mountain Dew AMP)'), -- Energy drink, sugar free (Mountain Dew AMP)
    ('95312550', 'Nước tăng lực, không đường (No Fear)'), -- Energy drink, sugar free (No Fear)
    ('95312555', 'Nước tăng lực, không đường (NOS)'), -- Energy drink, sugar-free (NOS)
    ('95312560', 'Nước tăng lực (Ocean Spray Cran-Energy Juice Drink)'), -- Energy drink (Ocean Spray Cran-Energy Juice Drink)
    ('95312600', 'Nước tăng lực, không đường (Red Bull)'), -- Energy drink, sugar-free (Red Bull)
    ('95312700', 'Nước tăng lực, không đường (Rockstar)'), -- Energy drink, sugar free (Rockstar)
    ('95312800', 'Nước tăng lực, không đường (Vault)'), -- Energy drink, sugar free (Vault)
    ('95312900', 'Nước tăng lực (XS)'), -- Energy drink (XS)
    ('95312905', 'Nước tăng lực (XS Gold Plus)'), -- Energy drink (XS Gold Plus)
    ('95313200', 'Nước tăng lực, không đường'), -- Energy drink, sugar free
    ('95320200', 'Nước uống thể thao (Gatorade G)'), -- Sports drink (Gatorade G)
    ('95320500', 'Nước uống thể thao (Powerade)'), -- Sports drink (Powerade)
    ('95321000', 'Nước uống thể thao, loại chung'), -- Sports drink, NFS
    ('95322200', 'Nước uống thể thao, ít calo (Gatorade G2)'), -- Sports drink, low calorie (Gatorade G2)
    ('95322500', 'Nước uống thể thao, ít calo (Powerade Zero)'), -- Sports drink, low calorie (Powerade Zero)
    ('95323000', 'Nước uống thể thao, ít calo'), -- Sports drink, low calorie
    ('95330100', 'Dung dịch bù nước, dung dịch điện giải'), -- Fluid replacement, electrolyte solution
    ('95330500', 'Dung dịch bù nước, glucose 5% trong nước'), -- Fluid replacement, 5% glucose in water
    ('95342000', 'Nước ép trái cây, pha trộn với acai'), -- Fruit juice, acai blend
    ('99991400', 'Phô mai làm nguyên liệu trong bánh mì kẹp'), -- Cheese as ingredient in sandwiches
    ('99991410', 'Phô mai và phô mai queso làm nguyên liệu'), -- Cheese and Queso as ingredient
    ('99992100', 'Thịt bò làm nguyên liệu trong món ăn'), -- Beef as ingredient in recipes
    ('99992230', 'Thịt ăn sáng làm nguyên liệu trong trứng tráng (omelet)'), -- Breakfast meat as ingredient in omelet
    ('99992405', 'Thịt gà làm nguyên liệu trong món ăn'), -- Chicken as ingredient in recipes
    ('99992600', 'Cá nấu chín, làm nguyên liệu'), -- Fish, cooked, as ingredient
    ('99992610', 'Phi lê cá chiên, làm nguyên liệu trong bánh mì kẹp'), -- Fish fillet, fried as ingredient in sandwiches
    ('99995000', 'Lớp bột tẩm chiên xù hoặc bột nhão làm nguyên liệu trong món ăn'), -- Breading or batter as ingredient in food
    ('99995130', 'Bánh mì lúa mì làm nguyên liệu trong bánh mì kẹp'), -- Wheat bread as ingredient in sandwiches
    ('99995135', 'Bánh mì bun lúa mì làm nguyên liệu trong bánh mì kẹp'), -- Wheat bun as ingredient in sandwiches
    ('99995620', 'Cơm trắng, làm nguyên liệu'), -- Rice, white, cooked, as ingredient
    ('99995625', 'Cơm gạo lứt, làm nguyên liệu'), -- Rice, brown, cooked, as ingredient
    ('99997100', 'Khoai tây nấu chín, làm nguyên liệu'), -- Potato, cooked, as ingredient
    ('99997210', 'Rau chân vịt nấu chín, làm nguyên liệu'), -- Spinach, cooked, as ingredient
    ('99997220', 'Súp lơ xanh nấu chín, làm nguyên liệu'), -- Broccoli, cooked, as ingredient
    ('99997310', 'Cà rốt nấu chín, làm nguyên liệu'), -- Carrots, cooked, as ingredient
    ('99997340', 'Khoai lang nấu chín, làm nguyên liệu'), -- Sweet potato, cooked, as ingredient
    ('99997410', 'Cà chua nấu chín, làm nguyên liệu'), -- Tomatoes, cooked, as ingredient
    ('99997510', 'Hành tây nấu chín, làm nguyên liệu'), -- Onions, cooked, as ingredient
    ('99997515', 'Nấm nấu chín, làm nguyên liệu'), -- Mushrooms, cooked, as ingredient
    ('99997520', 'Ớt chuông xanh nấu chín, làm nguyên liệu'), -- Green pepper, cooked, as ingredient
    ('99997525', 'Ớt chuông đỏ nấu chín, làm nguyên liệu'), -- Red pepper, cooked, as ingredient
    ('99997530', 'Cải bắp nấu chín, làm nguyên liệu'), -- Cabbage, cooked, as ingredient
    ('99997535', 'Súp lơ trắng nấu chín, làm nguyên liệu'), -- Cauliflower, cooked, as ingredient
    ('99997540', 'Cà tím nấu chín, làm nguyên liệu'), -- Eggplant, cooked, as ingredient
    ('99997545', 'Đậu cô ve nấu chín, làm nguyên liệu'), -- Green beans, cooked, as ingredient
    ('99997550', 'Bí mùa hè (summer squash) nấu chín, làm nguyên liệu'), -- Summer squash, cooked, as ingredient
    ('99997555', 'Cần tây nấu chín, làm nguyên liệu'), -- Celery, cooked, as ingredient
    ('99997800', 'Rau xanh đậm làm nguyên liệu trong trứng tráng (omelet)'), -- Dark green vegetables as ingredient in omelet
    ('99997802', 'Cà chua làm nguyên liệu trong trứng tráng (omelet)'), -- Tomatoes as ingredient in omelet
    ('99997804', 'Rau khác làm nguyên liệu trong trứng tráng (omelet)'), -- Other vegetables as ingredient in omelet
    ('99997805', 'Hỗn hợp rau mirepoix (hành tây, cà rốt, cần tây) nấu chín, làm nguyên liệu'), -- Mirepoix, cooked, as ingredient
    ('99997810', 'Rau làm nguyên liệu trong món cà ri'), -- Vegetables as ingredient in curry
    ('99997815', 'Rau làm nguyên liệu trong súp'), -- Vegetables as ingredient in soups
    ('99997820', 'Rau làm nguyên liệu trong món hầm'), -- Vegetables as ingredient in stews
    ('99998130', 'Nước sốt làm nguyên liệu trong bánh hamburger'), -- Sauce as ingredient in hamburgers
    ('99998210', 'Dầu ăn công nghiệp làm nguyên liệu trong thực phẩm') -- Industrial oil as ingredient in food
) AS t (source_food_code, name_vi)
WHERE f.source = 'USDA_FNDDS' AND f.source_food_code = t.source_food_code;
