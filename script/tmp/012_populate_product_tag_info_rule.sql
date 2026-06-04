SET NOCOUNT ON;

DECLARE @apply bit = $(Apply);
DECLARE @tenantId nvarchar(20) = N'000000';
DECLARE @tagTypeNo nvarchar(20) = N'FUNCTION';
DECLARE @tagTypeName nvarchar(40) = N'功能';

IF OBJECT_ID('tempdb..#classified_product_tags') IS NOT NULL
    DROP TABLE #classified_product_tags;

WITH product_source AS (
    SELECT
        LTRIM(RTRIM(product_no)) AS product_no,
        ISNULL(NULLIF(LTRIM(RTRIM(product_barcode)), N''), LTRIM(RTRIM(product_no))) AS product_barcode,
        ISNULL(NULLIF(LTRIM(RTRIM(product_name)), N''), LTRIM(RTRIM(product_no))) AS product_name,
        ISNULL(one_class_name, N'') AS one_class_name,
        ISNULL(two_class_name, N'') AS two_class_name,
        ISNULL(three_class_name, N'') AS three_class_name,
        ISNULL(four_class_name, N'') AS four_class_name,
        ISNULL(five_class_name, N'') AS five_class_name,
        ISNULL(product_spec, N'') AS product_spec,
        updatetime,
        id,
        ROW_NUMBER() OVER (
            PARTITION BY LTRIM(RTRIM(product_no))
            ORDER BY
                CASE WHEN store_no = N'0000' THEN 0 ELSE 1 END,
                updatetime DESC,
                id DESC
        ) AS rn
    FROM dbo.base_dept_products
    WHERE ISNULL(LTRIM(RTRIM(product_no)), N'') <> N''
),
selected_products AS (
    SELECT
        product_no,
        product_barcode,
        product_name,
        one_class_name,
        two_class_name,
        three_class_name,
        four_class_name,
        five_class_name,
        CONCAT(
            product_name, N'|',
            one_class_name, N'|',
            two_class_name, N'|',
            three_class_name, N'|',
            four_class_name, N'|',
            five_class_name, N'|',
            product_spec
        ) AS rule_text
    FROM product_source
    WHERE rn = 1
)
SELECT
    product_no,
    product_barcode,
    product_name,
    one_class_name,
    two_class_name,
    three_class_name,
    four_class_name,
    CASE
        WHEN rule_text LIKE N'%香烟%' OR rule_text LIKE N'%烟草%' OR rule_text LIKE N'%白酒%' OR rule_text LIKE N'%啤酒%' OR rule_text LIKE N'%葡萄酒%' OR rule_text LIKE N'%威士忌%' OR rule_text LIKE N'%黄酒%' THEN N'SMOKE_ALCOHOL'
        WHEN rule_text LIKE N'%牙刷%' OR rule_text LIKE N'%牙膏%' OR rule_text LIKE N'%漱口%' OR rule_text LIKE N'%牙线%' OR rule_text LIKE N'%口腔%' THEN N'ORAL_CARE'
        WHEN rule_text LIKE N'%卫生巾%' OR rule_text LIKE N'%纸尿%' OR rule_text LIKE N'%尿裤%' OR rule_text LIKE N'%湿巾%' OR rule_text LIKE N'%手帕纸%' OR rule_text LIKE N'%抽纸%' OR rule_text LIKE N'%卷纸%' OR rule_text LIKE N'%面纸%' THEN N'HYGIENE'
        WHEN rule_text LIKE N'%洗衣%' OR rule_text LIKE N'%柔顺%' OR rule_text LIKE N'%衣物%' OR rule_text LIKE N'%漂白%' THEN N'LAUNDRY'
        WHEN rule_text LIKE N'%洗洁%' OR rule_text LIKE N'%洁厕%' OR rule_text LIKE N'%油污%' OR rule_text LIKE N'%除菌%' OR rule_text LIKE N'%消毒%' OR rule_text LIKE N'%清洁%' OR rule_text LIKE N'%洗手液%' OR rule_text LIKE N'%肥皂%' OR rule_text LIKE N'%香皂%' THEN N'CLEANING'
        WHEN rule_text LIKE N'%洗发%' OR rule_text LIKE N'%护发%' OR rule_text LIKE N'%沐浴%' OR rule_text LIKE N'%洗面奶%' OR rule_text LIKE N'%护肤%' OR rule_text LIKE N'%面膜%' OR rule_text LIKE N'%身体乳%' OR rule_text LIKE N'%剃须%' THEN N'PERSONAL_CARE'
        WHEN rule_text LIKE N'%饮料%' OR rule_text LIKE N'%椰子水%' OR rule_text LIKE N'%矿泉%' OR rule_text LIKE N'%纯净水%' OR rule_text LIKE N'%凉茶%' OR rule_text LIKE N'%茶饮%' OR rule_text LIKE N'%果汁%' OR rule_text LIKE N'%可乐%' OR rule_text LIKE N'%雪碧%' OR rule_text LIKE N'%咖啡%' OR rule_text LIKE N'%奶茶%' THEN N'HYDRATION'
        WHEN rule_text LIKE N'%奶粉%' OR rule_text LIKE N'%牛奶%' OR rule_text LIKE N'%鲜奶%' OR rule_text LIKE N'%酸奶%' OR rule_text LIKE N'%羊奶%' OR rule_text LIKE N'%乳制%' OR rule_text LIKE N'%奶类%' OR rule_text LIKE N'%奶酪%' OR rule_text LIKE N'%蛋白%' OR rule_text LIKE N'%豆浆%' OR rule_text LIKE N'%豆腐%' OR rule_text LIKE N'%鸡蛋%' OR rule_text LIKE N'%鸭蛋%' OR rule_text LIKE N'%肉类%' OR rule_text LIKE N'%鸡肉%' OR rule_text LIKE N'%牛肉%' OR rule_text LIKE N'%猪肉%' OR rule_text LIKE N'%鱼%' OR rule_text LIKE N'%虾%' THEN N'PROTEIN'
        WHEN rule_text LIKE N'%维生素%' OR rule_text LIKE N'%水果%' OR rule_text LIKE N'%蔬菜%' OR rule_text LIKE N'%蔬果%' OR rule_text LIKE N'%山楂%' OR rule_text LIKE N'%红枣%' OR rule_text LIKE N'%柠檬%' OR rule_text LIKE N'%苹果%' OR rule_text LIKE N'%橙%' OR rule_text LIKE N'%葡萄%' OR rule_text LIKE N'%芦荟%' OR rule_text LIKE N'%番茄%' THEN N'VITAMIN'
        WHEN rule_text LIKE N'%糖果%' OR rule_text LIKE N'%巧克力%' OR rule_text LIKE N'%蜜饯%' OR rule_text LIKE N'%坚果%' OR rule_text LIKE N'%薯片%' OR rule_text LIKE N'%休闲食品%' OR rule_text LIKE N'%凤爪%' OR rule_text LIKE N'%牛肉干%' OR rule_text LIKE N'%果条%' OR rule_text LIKE N'%奶贝%' THEN N'SNACK'
        WHEN rule_text LIKE N'%面包%' OR rule_text LIKE N'%糕点%' OR rule_text LIKE N'%饼干%' OR rule_text LIKE N'%吐司%' OR rule_text LIKE N'%米%' OR rule_text LIKE N'%面%' OR rule_text LIKE N'%粉%' OR rule_text LIKE N'%粥%' OR rule_text LIKE N'%饭%' OR rule_text LIKE N'%方便食品%' OR rule_text LIKE N'%罐头%' OR rule_text LIKE N'%榨菜%' THEN N'SATIETY'
        WHEN rule_text LIKE N'%调味%' OR rule_text LIKE N'%咖喱%' OR rule_text LIKE N'%酱%' OR rule_text LIKE N'%醋%' OR rule_text LIKE N'%盐%' OR rule_text LIKE N'%香油%' OR rule_text LIKE N'%料酒%' OR rule_text LIKE N'%味精%' OR rule_text LIKE N'%鸡精%' THEN N'SEASONING'
        WHEN rule_text LIKE N'%婴%' OR rule_text LIKE N'%儿童%' OR rule_text LIKE N'%宝宝%' OR rule_text LIKE N'%母婴%' THEN N'BABY_CARE'
        WHEN rule_text LIKE N'%宠物%' OR rule_text LIKE N'%猫粮%' OR rule_text LIKE N'%狗粮%' THEN N'PET_CARE'
        WHEN rule_text LIKE N'%玩具%' OR rule_text LIKE N'%文具%' OR rule_text LIKE N'%文化用品%' OR rule_text LIKE N'%本册%' OR rule_text LIKE N'%书%' OR rule_text LIKE N'%彩泥%' THEN N'EDUCATION_PLAY'
        WHEN rule_text LIKE N'%家电%' OR rule_text LIKE N'%电器%' OR rule_text LIKE N'%电风扇%' OR rule_text LIKE N'%电池%' OR rule_text LIKE N'%灯%' OR rule_text LIKE N'%插座%' THEN N'ELECTRIC_TOOL'
        WHEN rule_text LIKE N'%针纺%' OR rule_text LIKE N'%服饰%' OR rule_text LIKE N'%鞋%' OR rule_text LIKE N'%袜%' OR rule_text LIKE N'%内衣%' OR rule_text LIKE N'%箱包%' THEN N'WEAR'
        WHEN rule_text LIKE N'%百货%' OR rule_text LIKE N'%居家%' OR rule_text LIKE N'%厨房%' OR rule_text LIKE N'%收纳%' OR rule_text LIKE N'%一次性%' OR rule_text LIKE N'%杯%' OR rule_text LIKE N'%碗%' OR rule_text LIKE N'%盆%' THEN N'HOUSEHOLD'
        WHEN rule_text LIKE N'%保健%' OR rule_text LIKE N'%钙%' OR rule_text LIKE N'%益生%' OR rule_text LIKE N'%药%' THEN N'HEALTH'
        ELSE N'DAILY'
    END AS tag_no,
    CASE
        WHEN rule_text LIKE N'%香烟%' OR rule_text LIKE N'%烟草%' OR rule_text LIKE N'%白酒%' OR rule_text LIKE N'%啤酒%' OR rule_text LIKE N'%葡萄酒%' OR rule_text LIKE N'%威士忌%' OR rule_text LIKE N'%黄酒%' THEN N'烟酒饮品'
        WHEN rule_text LIKE N'%牙刷%' OR rule_text LIKE N'%牙膏%' OR rule_text LIKE N'%漱口%' OR rule_text LIKE N'%牙线%' OR rule_text LIKE N'%口腔%' THEN N'口腔护理'
        WHEN rule_text LIKE N'%卫生巾%' OR rule_text LIKE N'%纸尿%' OR rule_text LIKE N'%尿裤%' OR rule_text LIKE N'%湿巾%' OR rule_text LIKE N'%手帕纸%' OR rule_text LIKE N'%抽纸%' OR rule_text LIKE N'%卷纸%' OR rule_text LIKE N'%面纸%' THEN N'卫生防护'
        WHEN rule_text LIKE N'%洗衣%' OR rule_text LIKE N'%柔顺%' OR rule_text LIKE N'%衣物%' OR rule_text LIKE N'%漂白%' THEN N'衣物护理'
        WHEN rule_text LIKE N'%洗洁%' OR rule_text LIKE N'%洁厕%' OR rule_text LIKE N'%油污%' OR rule_text LIKE N'%除菌%' OR rule_text LIKE N'%消毒%' OR rule_text LIKE N'%清洁%' OR rule_text LIKE N'%洗手液%' OR rule_text LIKE N'%肥皂%' OR rule_text LIKE N'%香皂%' THEN N'清洁去污'
        WHEN rule_text LIKE N'%洗发%' OR rule_text LIKE N'%护发%' OR rule_text LIKE N'%沐浴%' OR rule_text LIKE N'%洗面奶%' OR rule_text LIKE N'%护肤%' OR rule_text LIKE N'%面膜%' OR rule_text LIKE N'%身体乳%' OR rule_text LIKE N'%剃须%' THEN N'个人护理'
        WHEN rule_text LIKE N'%饮料%' OR rule_text LIKE N'%椰子水%' OR rule_text LIKE N'%矿泉%' OR rule_text LIKE N'%纯净水%' OR rule_text LIKE N'%凉茶%' OR rule_text LIKE N'%茶饮%' OR rule_text LIKE N'%果汁%' OR rule_text LIKE N'%可乐%' OR rule_text LIKE N'%雪碧%' OR rule_text LIKE N'%咖啡%' OR rule_text LIKE N'%奶茶%' THEN N'补充水分'
        WHEN rule_text LIKE N'%奶粉%' OR rule_text LIKE N'%牛奶%' OR rule_text LIKE N'%鲜奶%' OR rule_text LIKE N'%酸奶%' OR rule_text LIKE N'%羊奶%' OR rule_text LIKE N'%乳制%' OR rule_text LIKE N'%奶类%' OR rule_text LIKE N'%奶酪%' OR rule_text LIKE N'%蛋白%' OR rule_text LIKE N'%豆浆%' OR rule_text LIKE N'%豆腐%' OR rule_text LIKE N'%鸡蛋%' OR rule_text LIKE N'%鸭蛋%' OR rule_text LIKE N'%肉类%' OR rule_text LIKE N'%鸡肉%' OR rule_text LIKE N'%牛肉%' OR rule_text LIKE N'%猪肉%' OR rule_text LIKE N'%鱼%' OR rule_text LIKE N'%虾%' THEN N'补充蛋白质'
        WHEN rule_text LIKE N'%维生素%' OR rule_text LIKE N'%水果%' OR rule_text LIKE N'%蔬菜%' OR rule_text LIKE N'%蔬果%' OR rule_text LIKE N'%山楂%' OR rule_text LIKE N'%红枣%' OR rule_text LIKE N'%柠檬%' OR rule_text LIKE N'%苹果%' OR rule_text LIKE N'%橙%' OR rule_text LIKE N'%葡萄%' OR rule_text LIKE N'%芦荟%' OR rule_text LIKE N'%番茄%' THEN N'补充维生素'
        WHEN rule_text LIKE N'%糖果%' OR rule_text LIKE N'%巧克力%' OR rule_text LIKE N'%蜜饯%' OR rule_text LIKE N'%坚果%' OR rule_text LIKE N'%薯片%' OR rule_text LIKE N'%休闲食品%' OR rule_text LIKE N'%凤爪%' OR rule_text LIKE N'%牛肉干%' OR rule_text LIKE N'%果条%' OR rule_text LIKE N'%奶贝%' THEN N'休闲零食'
        WHEN rule_text LIKE N'%面包%' OR rule_text LIKE N'%糕点%' OR rule_text LIKE N'%饼干%' OR rule_text LIKE N'%吐司%' OR rule_text LIKE N'%米%' OR rule_text LIKE N'%面%' OR rule_text LIKE N'%粉%' OR rule_text LIKE N'%粥%' OR rule_text LIKE N'%饭%' OR rule_text LIKE N'%方便食品%' OR rule_text LIKE N'%罐头%' OR rule_text LIKE N'%榨菜%' THEN N'快速饱腹'
        WHEN rule_text LIKE N'%调味%' OR rule_text LIKE N'%咖喱%' OR rule_text LIKE N'%酱%' OR rule_text LIKE N'%醋%' OR rule_text LIKE N'%盐%' OR rule_text LIKE N'%香油%' OR rule_text LIKE N'%料酒%' OR rule_text LIKE N'%味精%' OR rule_text LIKE N'%鸡精%' THEN N'调味增香'
        WHEN rule_text LIKE N'%婴%' OR rule_text LIKE N'%儿童%' OR rule_text LIKE N'%宝宝%' OR rule_text LIKE N'%母婴%' THEN N'婴童关怀'
        WHEN rule_text LIKE N'%宠物%' OR rule_text LIKE N'%猫粮%' OR rule_text LIKE N'%狗粮%' THEN N'宠物关怀'
        WHEN rule_text LIKE N'%玩具%' OR rule_text LIKE N'%文具%' OR rule_text LIKE N'%文化用品%' OR rule_text LIKE N'%本册%' OR rule_text LIKE N'%书%' OR rule_text LIKE N'%彩泥%' THEN N'文教娱乐'
        WHEN rule_text LIKE N'%家电%' OR rule_text LIKE N'%电器%' OR rule_text LIKE N'%电风扇%' OR rule_text LIKE N'%电池%' OR rule_text LIKE N'%灯%' OR rule_text LIKE N'%插座%' THEN N'电器工具'
        WHEN rule_text LIKE N'%针纺%' OR rule_text LIKE N'%服饰%' OR rule_text LIKE N'%鞋%' OR rule_text LIKE N'%袜%' OR rule_text LIKE N'%内衣%' OR rule_text LIKE N'%箱包%' THEN N'服饰穿戴'
        WHEN rule_text LIKE N'%百货%' OR rule_text LIKE N'%居家%' OR rule_text LIKE N'%厨房%' OR rule_text LIKE N'%收纳%' OR rule_text LIKE N'%一次性%' OR rule_text LIKE N'%杯%' OR rule_text LIKE N'%碗%' OR rule_text LIKE N'%盆%' THEN N'家居日用'
        WHEN rule_text LIKE N'%保健%' OR rule_text LIKE N'%钙%' OR rule_text LIKE N'%益生%' OR rule_text LIKE N'%药%' THEN N'健康保健'
        ELSE N'日常消费'
    END AS tag_name
INTO #classified_product_tags
FROM selected_products;

SELECT tag_no, tag_name, COUNT(1) AS product_count
FROM #classified_product_tags
GROUP BY tag_no, tag_name
ORDER BY product_count DESC, tag_no ASC;

SELECT one_class_name, tag_name, COUNT(1) AS product_count
FROM #classified_product_tags
GROUP BY one_class_name, tag_name
ORDER BY one_class_name ASC, product_count DESC;

IF @apply = 1
BEGIN
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.product_tag_info_backup_before_rule_20260604', N'U') IS NULL
    BEGIN
        SELECT *
        INTO dbo.product_tag_info_backup_before_rule_20260604
        FROM dbo.product_tag_info;
    END;

    DELETE FROM dbo.product_tag_info
    WHERE tenant_id = @tenantId
      AND tag_type_no = @tagTypeNo;

    INSERT INTO dbo.product_tag_info (
        product_no,
        product_barcode,
        tag_type_no,
        tag_type_name,
        tag_no,
        tag_name,
        updatetime,
        tenant_id
    )
    SELECT
        product_no,
        product_barcode,
        @tagTypeNo,
        @tagTypeName,
        tag_no,
        tag_name,
        GETDATE(),
        @tenantId
    FROM #classified_product_tags;

    COMMIT TRANSACTION;

    SELECT N'APPLIED' AS status, COUNT(1) AS inserted_count
    FROM dbo.product_tag_info
    WHERE tenant_id = @tenantId
      AND tag_type_no = @tagTypeNo;
END
ELSE
BEGIN
    SELECT N'DRY_RUN_ONLY' AS status, COUNT(1) AS candidate_count
    FROM #classified_product_tags;
END;
