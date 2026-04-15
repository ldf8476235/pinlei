-- diagnosis 毛利贡献率预计算结果层升级脚本 (SQL Server)

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.diag_result_gross_sku', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_gross_sku
    (
        id                    BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id             NVARCHAR(20)   NOT NULL DEFAULT N'000000',
        query_hash            NVARCHAR(64)   NOT NULL,
        data_version          NVARCHAR(64)   NOT NULL,
        product_no            NVARCHAR(40)   NOT NULL,
        product_name          NVARCHAR(200)  NULL,
        product_status        NVARCHAR(40)   NULL,
        product_status_no     NVARCHAR(20)   NULL,
        store_num             INT            NOT NULL DEFAULT 0,
        current_gross_role    NVARCHAR(2)    NULL,
        compare_gross_role    NVARCHAR(2)    NULL,
        current_gmroi_role    NVARCHAR(2)    NULL,
        compare_gmroi_role    NVARCHAR(2)    NULL,
        sale_quantity         DECIMAL(20,2)  NOT NULL DEFAULT 0,
        sale_quantity_psd     DECIMAL(20,4)  NOT NULL DEFAULT 0,
        sales                 DECIMAL(20,2)  NOT NULL DEFAULT 0,
        sales_per             DECIMAL(20,2)  NOT NULL DEFAULT 0,
        sales_psd             DECIMAL(20,4)  NOT NULL DEFAULT 0,
        gross                 DECIMAL(20,2)  NOT NULL DEFAULT 0,
        gross_per             DECIMAL(20,2)  NOT NULL DEFAULT 0,
        gross_psd             DECIMAL(20,4)  NOT NULL DEFAULT 0,
        gross_rate            DECIMAL(20,2)  NOT NULL DEFAULT 0,
        stock_quantity        DECIMAL(20,2)  NOT NULL DEFAULT 0,
        turnover_rate         DECIMAL(20,4)  NOT NULL DEFAULT 0,
        turnover_days         DECIMAL(20,4)  NOT NULL DEFAULT 0,
        stock_sales_rate      DECIMAL(20,2)  NOT NULL DEFAULT 0,
        contribution_rate     DECIMAL(20,2)  NOT NULL DEFAULT 0,
        gmroi                 DECIMAL(20,4)  NOT NULL DEFAULT 0,
        sales_rate            DECIMAL(20,2)  NOT NULL DEFAULT 0,
        activity              NVARCHAR(20)   NULL,
        first_sale_date       DATE           NULL,
        new_product           NVARCHAR(20)   NULL,
        key_product           NVARCHAR(20)   NULL,
        seasonable_flag       NVARCHAR(20)   NULL,
        seasonable_flag_name  NVARCHAR(40)   NULL,
        seasonable_start_date DATE           NULL,
        seasonable_end_date   DATE           NULL,
        class_no              NVARCHAR(40)   NULL,
        class_name            NVARCHAR(100)  NULL,
        class_level           INT            NULL,
        product_barcode       NVARCHAR(80)   NULL,
        brand_name            NVARCHAR(100)  NULL,
        spec                  NVARCHAR(100)  NULL,
        in_price              DECIMAL(20,2)  NOT NULL DEFAULT 0,
        sales_price           DECIMAL(20,2)  NOT NULL DEFAULT 0,
        product_vendor_no     NVARCHAR(80)   NULL,
        product_vendor_name   NVARCHAR(200)  NULL,
        product_vendor_no_name NVARCHAR(300) NULL,
        promotion_flag        NVARCHAR(20)   NULL,
        compare_sales         DECIMAL(20,2)  NOT NULL DEFAULT 0,
        compare_gross         DECIMAL(20,2)  NOT NULL DEFAULT 0,
        current_growth_rate   DECIMAL(20,2)  NOT NULL DEFAULT 0,
        compare_growth_rate   DECIMAL(20,2)  NOT NULL DEFAULT 0,
        snapshot_time         DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time           DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time           DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_gross_sku_query_ver_product'
      AND object_id = OBJECT_ID(N'dbo.diag_result_gross_sku')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_gross_sku_query_ver_product
        ON dbo.diag_result_gross_sku(tenant_id, query_hash, data_version, product_no);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_gross_sku_filter'
      AND object_id = OBJECT_ID(N'dbo.diag_result_gross_sku')
)
BEGIN
    CREATE INDEX ix_diag_result_gross_sku_filter
        ON dbo.diag_result_gross_sku(tenant_id, query_hash, data_version, current_gross_role, compare_gross_role, product_status_no, promotion_flag);
END
GO

PRINT N'[OK] diagnosis gross contribution schema upgrade done';
GO

