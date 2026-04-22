-- diagnosis class sales list result schema upgrade (SQL Server)

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.diag_result_category_sales_sku', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_category_sales_sku
    (
        id                     BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id              NVARCHAR(32)   NOT NULL,
        query_hash             NVARCHAR(128)  NOT NULL,
        data_version           NVARCHAR(64)   NOT NULL,
        promotion_flag         NVARCHAR(8)    NOT NULL,
        product_no             NVARCHAR(64)   NOT NULL,
        product_name           NVARCHAR(255)  NULL,
        product_status         NVARCHAR(64)   NULL,
        product_status_no      NVARCHAR(32)   NULL,
        store_num              INT            NOT NULL,
        sale_quantity          DECIMAL(20,6)  NOT NULL,
        sale_quantity_psd      DECIMAL(20,6)  NOT NULL,
        sales                  DECIMAL(20,6)  NOT NULL,
        sales_per              DECIMAL(20,6)  NOT NULL,
        sales_psd              DECIMAL(20,6)  NOT NULL,
        gross                  DECIMAL(20,6)  NOT NULL,
        gross_per              DECIMAL(20,6)  NOT NULL,
        gross_psd              DECIMAL(20,6)  NOT NULL,
        gross_rate             DECIMAL(20,6)  NOT NULL,
        stock_quantity         DECIMAL(20,6)  NOT NULL,
        turnover_rate          DECIMAL(20,6)  NOT NULL,
        turnover_days          DECIMAL(20,6)  NOT NULL,
        stock_sales_rate       DECIMAL(20,6)  NOT NULL,
        contribution_rate      DECIMAL(20,6)  NOT NULL,
        gmroi                  DECIMAL(20,6)  NOT NULL,
        sales_rate             DECIMAL(20,6)  NULL,
        activity               NVARCHAR(16)   NULL,
        first_sale_date        DATE           NULL,
        new_product            NVARCHAR(16)   NULL,
        key_product            NVARCHAR(16)   NULL,
        seasonable_flag        NVARCHAR(16)   NULL,
        seasonable_flag_name   NVARCHAR(32)   NULL,
        seasonable_start_date  DATE           NULL,
        seasonable_end_date    DATE           NULL,
        class_no               NVARCHAR(64)   NULL,
        class_name             NVARCHAR(255)  NULL,
        product_barcode        NVARCHAR(128)  NULL,
        brand_name             NVARCHAR(255)  NULL,
        spec                   NVARCHAR(255)  NULL,
        in_price               DECIMAL(20,6)  NULL,
        sales_price            DECIMAL(20,6)  NULL,
        product_vendor_no      NVARCHAR(64)   NULL,
        product_vendor_name    NVARCHAR(255)  NULL,
        product_vendor_no_name NVARCHAR(512)  NULL,
        snapshot_time          DATETIME2(0)   NOT NULL,
        create_time            DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time            DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_category_sales_sku_qvvp'
      AND object_id = OBJECT_ID(N'dbo.diag_result_category_sales_sku')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_category_sales_sku_qvvp
        ON dbo.diag_result_category_sales_sku(tenant_id, query_hash, data_version, product_no);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_category_sales_sku_qv_status_promo'
      AND object_id = OBJECT_ID(N'dbo.diag_result_category_sales_sku')
)
BEGIN
    CREATE INDEX ix_diag_result_category_sales_sku_qv_status_promo
        ON dbo.diag_result_category_sales_sku(tenant_id, query_hash, data_version, product_status_no, promotion_flag, product_no);
END
GO

PRINT N'[OK] diagnosis class sales list schema upgrade done';
GO
