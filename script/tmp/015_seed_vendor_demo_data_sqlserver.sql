-- Development-only vendor demo seed for diagnosis precompute.
-- Run in a test/dev SQL Server database only.

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;

DECLARE @now DATETIME2(7) = SYSDATETIME();
DECLARE @storeNo NVARCHAR(40) = N'0000';
DECLARE @storeName NVARCHAR(40) = N'Demo Store';

IF NOT EXISTS (SELECT 1 FROM dbo.base_vendor_product_info WHERE product_no = N'VND-P-001')
BEGIN
    INSERT INTO dbo.base_vendor_product_info
    (product_no, product_barcode, vendor_no, vendor_name, status_no, status_name, updatetime)
    VALUES
    (N'VND-P-001', N'6909000000011', N'V001', N'Supplier A', N'1', N'Normal', @now),
    (N'VND-P-002', N'6909000000028', N'V001', N'Supplier A', N'1', N'Normal', @now),
    (N'VND-P-003', N'6909000000035', N'V002', N'Supplier B', N'1', N'Normal', @now),
    (N'VND-P-004', N'6909000000042', N'V003', N'Supplier C', N'1', N'Normal', @now);
END

IF NOT EXISTS (SELECT 1 FROM dbo.base_dept_products WHERE product_no = N'VND-P-001' AND store_no = @storeNo)
BEGIN
    INSERT INTO dbo.base_dept_products
    (
        store_no, store_name,
        one_class_no, one_class_name,
        two_class_no, two_class_name,
        three_class_no, three_class_name,
        four_class_no, four_class_name,
        five_class_no, five_class_name,
        div_no, div_name, subdiv_no, subdiv_name,
        bclass_no, bclass_name, mclass_no, mclass_name,
        sclass_no, sclass_name,
        product_no, product_barcode, product_name,
        brand_no, product_brand, product_spec,
        expireddate, expiredun, pspack_unit, product_unit,
        product_taxin_inprice, product_saleprice,
        status_no, status_name, sale_type_no, sale_type, logistic_type,
        seasonable_flag, seasonable_start_date, seasonable_end_date, seasonable_info,
        display_position, sequence_num, face_num, depth_num, full_display_quantity,
        first_order_date, updatetime
    )
    VALUES
    (@storeNo, @storeName, N'004', N'Level1', N'00401', N'Level2', N'0040101', N'Level3', N'004010101', N'Level4', NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
     N'VND-P-001', N'6909000000011', N'Demo Product 1',
     N'B001', N'Brand A', N'400ml',
     NULL, NULL, N'Bottle', N'Bottle',
     10.00, 19.90,
     1, N'Normal', 1, N'Wholesale', N'Delivery',
     N'0', NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL,
     '2025-04-02', @now),
    (@storeNo, @storeName, N'004', N'Level1', N'00401', N'Level2', N'0040101', N'Level3', N'004010102', N'Level4', NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
     N'VND-P-002', N'6909000000028', N'Demo Product 2',
     N'B001', N'Brand A', N'500ml',
     NULL, NULL, N'Bottle', N'Bottle',
     12.00, 22.90,
     1, N'Normal', 1, N'Wholesale', N'Delivery',
     N'0', NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL,
     '2025-04-08', @now),
    (@storeNo, @storeName, N'004', N'Level1', N'00401', N'Level2', N'0040101', N'Level3', N'004010103', N'Level4', NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
     N'VND-P-003', N'6909000000035', N'Demo Product 3',
     N'B002', N'Brand B', N'300ml',
     NULL, NULL, N'Bottle', N'Bottle',
     8.00, 15.90,
     1, N'Normal', 1, N'Wholesale', N'Delivery',
     N'0', NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL,
     '2025-04-12', @now),
    (@storeNo, @storeName, N'004', N'Level1', N'00401', N'Level2', N'0040101', N'Level3', N'004010104', N'Level4', NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
     N'VND-P-004', N'6909000000042', N'Demo Product 4',
     N'B003', N'Brand C', N'200ml',
     NULL, NULL, N'Bottle', N'Bottle',
     6.00, 12.90,
     1, N'Normal', 1, N'Wholesale', N'Delivery',
     N'0', NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL,
     '2025-04-15', @now);
END

IF NOT EXISTS (SELECT 1 FROM dbo.fact_sales_day WHERE product_no = N'VND-P-001' AND sale_date = '2025-04-02')
BEGIN
    INSERT INTO dbo.fact_sales_day
    (
        store_no, store_name, sale_channel, online_type, online_name,
        one_class_no, one_class_name, two_class_no, two_class_name,
        three_class_no, three_class_name, four_class_no, four_class_name,
        five_class_no, five_class_name, div_no, div_name, subdiv_no, subdiv_name,
        bclass_no, bclass_name, mclass_no, mclass_name, sclass_no, sclass_name,
        product_no, product_barcode, product_name, now_product_saleprice,
        sale_quantity, sales, sale_cost, gross, customer_return_quantity, customer_return,
        cx_flag, sale_date, updatetime
    )
    VALUES
    (@storeNo, @storeName, 1, NULL, NULL, N'004', N'Level1', N'00401', N'Level2', N'0040101', N'Level3', N'004010101', N'Level4',
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
     N'VND-P-001', N'6909000000011', N'Demo Product 1', 19.90, 300.00, 5970.000000, 3300.000000, 2670.000000, NULL, NULL, NULL, '2025-04-02', @now),
    (@storeNo, @storeName, 1, NULL, NULL, N'004', N'Level1', N'00401', N'Level2', N'0040101', N'Level3', N'004010102', N'Level4',
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
     N'VND-P-002', N'6909000000028', N'Demo Product 2', 22.90, 180.00, 4122.000000, 2380.000000, 1742.000000, NULL, NULL, NULL, '2025-04-08', @now),
    (@storeNo, @storeName, 1, NULL, NULL, N'004', N'Level1', N'00401', N'Level2', N'0040101', N'Level3', N'004010103', N'Level4',
     NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
     N'VND-P-003', N'6909000000035', N'Demo Product 3', 15.90, 18.00, 286.200000, 144.000000, 142.200000, NULL, NULL, NULL, '2025-04-12', @now);
END

IF NOT EXISTS (SELECT 1 FROM dbo.fact_vendor_order_month WHERE product_no = N'VND-P-001' AND order_date = '2025-04-01')
BEGIN
    INSERT INTO dbo.fact_vendor_order_month
    (
        order_no, order_month, vendor_no, final_store_no, final_store_name,
        product_no, product_barcode, product_name, order_date, order_quantity, arrival_date, arrival_quantity, updatetime
    )
    VALUES
    (N'ORD-V001-001', 202504, N'V001', @storeNo, @storeName, N'VND-P-001', N'6909000000011', N'Demo Product 1', '2025-04-01', 100.00, '2025-04-15', 95.00, @now),
    (N'ORD-V001-002', 202504, N'V001', @storeNo, @storeName, N'VND-P-002', N'6909000000028', N'Demo Product 2', '2025-04-03', 80.00, '2025-04-16', 78.00, @now),
    (N'ORD-V002-001', 202504, N'V002', @storeNo, @storeName, N'VND-P-003', N'6909000000035', N'Demo Product 3', '2025-04-05', 30.00, '2025-04-18', 30.00, @now),
    (N'ORD-V003-001', 202504, N'V003', @storeNo, @storeName, N'VND-P-004', N'6909000000042', N'Demo Product 4', '2025-04-06', 50.00, '2025-04-20', 20.00, @now);
END

IF NOT EXISTS (SELECT 1 FROM dbo.product_vendor_cost_info WHERE product_no = N'VND-P-001' AND cost_date = '2025-04-01')
BEGIN
    INSERT INTO dbo.product_vendor_cost_info
    (
        vendor_no, final_store_no, final_store_name, product_no, product_barcode,
        cost_no, cost_name, cost_money, cost_date, cost_type, updatetime
    )
    VALUES
    (N'V001', @storeNo, @storeName, N'VND-P-001', N'6909000000011', N'COST-001', N'Standard cost', 1000.00, '2025-04-01', 1, @now),
    (N'V001', @storeNo, @storeName, N'VND-P-002', N'6909000000028', N'COST-002', N'Standard cost', 900.00, '2025-04-03', 1, @now),
    (N'V002', @storeNo, @storeName, N'VND-P-003', N'6909000000035', N'COST-003', N'Standard cost', 180.00, '2025-04-05', 1, @now),
    (N'V003', @storeNo, @storeName, N'VND-P-004', N'6909000000042', N'COST-004', N'Standard cost', 600.00, '2025-04-06', 1, @now);
END

PRINT N'[OK] vendor demo seed script ready.';
