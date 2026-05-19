SET NOCOUNT ON;

BEGIN TRAN;

DELETE FROM dbo.sh7_saleplu
WHERE sale_no LIKE N'DEMO_ROLE_%'
   OR product_no LIKE N'DEMO_ROLE_%';

DELETE FROM dbo.fact_sales_day
WHERE product_no LIKE N'DEMO_ROLE_%';

WITH demo_source AS (
    SELECT *
    FROM (VALUES
        (N'001', N'生鲜日配', CAST(1800.000000 AS DECIMAL(20, 6)), CAST(2400.000000 AS DECIMAL(20, 6)), CAST(324.000000 AS DECIMAL(20, 6)), CAST(456.000000 AS DECIMAL(20, 6)), CAST(120.000 AS DECIMAL(12, 3)), CAST(150.000 AS DECIMAL(12, 3)), 1),
        (N'002', N'粮油调味', CAST(2300.000000 AS DECIMAL(20, 6)), CAST(2100.000000 AS DECIMAL(20, 6)), CAST(414.000000 AS DECIMAL(20, 6)), CAST(357.000000 AS DECIMAL(20, 6)), CAST(95.000 AS DECIMAL(12, 3)), CAST(84.000 AS DECIMAL(12, 3)), 2),
        (N'003', N'食品杂货', CAST(1500.000000 AS DECIMAL(20, 6)), CAST(2650.000000 AS DECIMAL(20, 6)), CAST(270.000000 AS DECIMAL(20, 6)), CAST(503.500000 AS DECIMAL(20, 6)), CAST(80.000 AS DECIMAL(12, 3)), CAST(110.000 AS DECIMAL(12, 3)), 3),
        (N'005', N'百货部',   CAST(2800.000000 AS DECIMAL(20, 6)), CAST(2500.000000 AS DECIMAL(20, 6)), CAST(700.000000 AS DECIMAL(20, 6)), CAST(575.000000 AS DECIMAL(20, 6)), CAST(60.000 AS DECIMAL(12, 3)), CAST(58.000 AS DECIMAL(12, 3)), 4),
        (N'006', N'针纺服饰部', CAST(900.000000 AS DECIMAL(20, 6)), CAST(1600.000000 AS DECIMAL(20, 6)), CAST(225.000000 AS DECIMAL(20, 6)), CAST(480.000000 AS DECIMAL(20, 6)), CAST(24.000 AS DECIMAL(12, 3)), CAST(32.000 AS DECIMAL(12, 3)), 5),
        (N'007', N'家电部',   CAST(3200.000000 AS DECIMAL(20, 6)), CAST(2900.000000 AS DECIMAL(20, 6)), CAST(960.000000 AS DECIMAL(20, 6)), CAST(783.000000 AS DECIMAL(20, 6)), CAST(18.000 AS DECIMAL(12, 3)), CAST(16.000 AS DECIMAL(12, 3)), 6)
    ) AS t(class_no, class_name, compare_sales, current_sales, compare_gross, current_gross, compare_qty, current_qty, seq_no)
),
demo_periods AS (
    SELECT
        class_no,
        class_name,
        seq_no,
        N'COMPARE' AS period_flag,
        DATEADD(DAY, seq_no - 1, CAST('2024-04-18' AS DATE)) AS sale_date,
        compare_sales AS sales,
        compare_gross AS gross,
        compare_qty AS sale_qty
    FROM demo_source
    UNION ALL
    SELECT
        class_no,
        class_name,
        seq_no,
        N'CURRENT' AS period_flag,
        DATEADD(DAY, seq_no - 1, CAST('2025-04-18' AS DATE)) AS sale_date,
        current_sales AS sales,
        current_gross AS gross,
        current_qty AS sale_qty
    FROM demo_source
)
INSERT INTO dbo.fact_sales_day (
    store_no,
    store_name,
    sale_channel,
    online_type,
    online_name,
    one_class_no,
    one_class_name,
    two_class_no,
    two_class_name,
    three_class_no,
    three_class_name,
    four_class_no,
    four_class_name,
    five_class_no,
    five_class_name,
    div_no,
    div_name,
    subdiv_no,
    subdiv_name,
    bclass_no,
    bclass_name,
    mclass_no,
    mclass_name,
    sclass_no,
    sclass_name,
    product_no,
    product_barcode,
    product_name,
    now_product_saleprice,
    sale_quantity,
    sales,
    sale_cost,
    gross,
    customer_return_quantity,
    customer_return,
    cx_flag,
    sale_date,
    updatetime,
    tenant_id
)
SELECT
    N'0000',
    N'全部门店',
    1,
    NULL,
    NULL,
    p.class_no,
    p.class_name,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    N'DEMO_ROLE_' + p.class_no,
    N'DEMO_ROLE_BAR_' + p.class_no,
    p.class_name + N'演示销售',
    CAST(ROUND(p.sales / NULLIF(p.sale_qty, 0), 2) AS DECIMAL(13, 2)),
    CAST(p.sale_qty AS DECIMAL(13, 2)),
    p.sales,
    p.sales - p.gross,
    p.gross,
    0,
    0,
    N'0',
    p.sale_date,
    DATEADD(MINUTE, p.seq_no, CAST('2026-04-26T20:00:00' AS DATETIME2(7))),
    N'000000'
FROM demo_periods p;

WITH demo_source AS (
    SELECT *
    FROM (VALUES
        (N'001', N'生鲜日配', CAST(1800.000 AS DECIMAL(12, 3)), CAST(2400.000 AS DECIMAL(12, 3)), CAST(120.000 AS DECIMAL(12, 3)), CAST(150.000 AS DECIMAL(12, 3)), 1),
        (N'002', N'粮油调味', CAST(2300.000 AS DECIMAL(12, 3)), CAST(2100.000 AS DECIMAL(12, 3)), CAST(95.000 AS DECIMAL(12, 3)), CAST(84.000 AS DECIMAL(12, 3)), 2),
        (N'003', N'食品杂货', CAST(1500.000 AS DECIMAL(12, 3)), CAST(2650.000 AS DECIMAL(12, 3)), CAST(80.000 AS DECIMAL(12, 3)), CAST(110.000 AS DECIMAL(12, 3)), 3),
        (N'005', N'百货部',   CAST(2800.000 AS DECIMAL(12, 3)), CAST(2500.000 AS DECIMAL(12, 3)), CAST(60.000 AS DECIMAL(12, 3)), CAST(58.000 AS DECIMAL(12, 3)), 4),
        (N'006', N'针纺服饰部', CAST(900.000 AS DECIMAL(12, 3)), CAST(1600.000 AS DECIMAL(12, 3)), CAST(24.000 AS DECIMAL(12, 3)), CAST(32.000 AS DECIMAL(12, 3)), 5),
        (N'007', N'家电部',   CAST(3200.000 AS DECIMAL(12, 3)), CAST(2900.000 AS DECIMAL(12, 3)), CAST(18.000 AS DECIMAL(12, 3)), CAST(16.000 AS DECIMAL(12, 3)), 6)
    ) AS t(class_no, class_name, compare_sales, current_sales, compare_qty, current_qty, seq_no)
),
demo_periods AS (
    SELECT
        class_no,
        class_name,
        seq_no,
        N'COMPARE' AS period_flag,
        DATEADD(HOUR, 8, CAST(DATEADD(DAY, seq_no - 1, CAST('2024-04-18' AS DATE)) AS DATETIME2(7))) AS sale_ts,
        compare_sales AS sales,
        compare_qty AS sale_qty
    FROM demo_source
    UNION ALL
    SELECT
        class_no,
        class_name,
        seq_no,
        N'CURRENT' AS period_flag,
        DATEADD(HOUR, 8, CAST(DATEADD(DAY, seq_no - 1, CAST('2025-04-18' AS DATE)) AS DATETIME2(7))) AS sale_ts,
        current_sales AS sales,
        current_qty AS sale_qty
    FROM demo_source
)
INSERT INTO dbo.sh7_saleplu (
    store_no,
    sale_channel,
    online_type,
    online_name,
    card_no,
    xsdate,
    sale_no,
    one_class_no,
    one_class_name,
    two_class_no,
    two_class_name,
    three_class_no,
    three_class_name,
    four_class_no,
    four_class_name,
    five_class_no,
    five_class_name,
    div_no,
    div_name,
    subdiv_no,
    subdiv_name,
    bclass_no,
    bclass_name,
    mclass_no,
    mclass_name,
    sclass_no,
    sclass_name,
    product_no,
    product_barcode,
    sale_quantity,
    nomal_price,
    price,
    vip_price,
    taxin_money,
    act_money,
    settlement_date,
    updatetime,
    tenant_id
)
SELECT
    N'0000',
    1,
    NULL,
    NULL,
    N'DEMO_CARD_' + p.class_no,
    p.sale_ts,
    N'DEMO_ROLE_' + p.class_no + N'_' + p.period_flag,
    p.class_no,
    p.class_name,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    N'DEMO_ROLE_' + p.class_no,
    N'DEMO_ROLE_BAR_' + p.class_no,
    p.sale_qty,
    CAST(ROUND(p.sales / NULLIF(p.sale_qty, 0), 3) AS DECIMAL(12, 3)),
    CAST(ROUND(p.sales / NULLIF(p.sale_qty, 0), 3) AS DECIMAL(12, 3)),
    CAST(ROUND(p.sales / NULLIF(p.sale_qty, 0), 3) AS DECIMAL(12, 3)),
    p.sales,
    p.sales,
    CAST(p.sale_ts AS DATE),
    DATEADD(MINUTE, p.seq_no, CAST('2026-04-26T20:10:00' AS DATETIME2(7))),
    N'000000'
FROM demo_periods p;

COMMIT TRAN;
