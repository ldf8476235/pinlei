-- diagnosis 价格带预计算结果层升级脚本 (SQL Server)

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.diag_price_band_config', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_price_band_config
    (
        id             BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id      NVARCHAR(32)   NOT NULL,
        query_hash     NVARCHAR(128)  NOT NULL,
        sort_no        INT            NOT NULL,
        price_band_min DECIMAL(20,2)  NOT NULL,
        price_band_max DECIMAL(20,2)  NULL,
        is_open_ended  CHAR(1)        NOT NULL DEFAULT '0',
        is_active      CHAR(1)        NOT NULL DEFAULT '1',
        created_by     NVARCHAR(64)   NULL,
        created_time   DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_by     NVARCHAR(64)   NULL,
        updated_time   DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'idx_diag_price_band_config_qh'
      AND object_id = OBJECT_ID(N'dbo.diag_price_band_config')
)
BEGIN
    CREATE INDEX idx_diag_price_band_config_qh
        ON dbo.diag_price_band_config(tenant_id, query_hash, is_active, sort_no);
END
GO

IF OBJECT_ID(N'dbo.diag_result_price_band_range', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_price_band_range
    (
        id                 BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id          NVARCHAR(32)   NOT NULL,
        query_hash         NVARCHAR(128)  NOT NULL,
        data_version       NVARCHAR(64)   NOT NULL,
        price_band_min     DECIMAL(20,2)  NOT NULL,
        price_band_max     DECIMAL(20,2)  NULL,
        price_band_label   NVARCHAR(64)   NOT NULL,
        sku                INT            NOT NULL,
        sku_per            DECIMAL(20,2)  NOT NULL,
        sale_quantity      DECIMAL(20,2)  NOT NULL,
        sale_quantity_per  DECIMAL(20,2)  NOT NULL,
        sale_quantity_unit DECIMAL(20,2)  NOT NULL,
        sales              DECIMAL(20,2)  NOT NULL,
        sales_per          DECIMAL(20,2)  NOT NULL,
        activity_sku       INT            NOT NULL,
        suggest_sku        INT            NOT NULL,
        suggest_sku_per    DECIMAL(20,2)  NOT NULL,
        sale_price         DECIMAL(20,2)  NOT NULL,
        band_level         NVARCHAR(32)   NOT NULL,
        strategy_code      NVARCHAR(64)   NOT NULL,
        snapshot_time      DATETIME2(0)   NOT NULL,
        create_time        DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time        DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'idx_diag_result_price_band_range_qv'
      AND object_id = OBJECT_ID(N'dbo.diag_result_price_band_range')
)
BEGIN
    CREATE INDEX idx_diag_result_price_band_range_qv
        ON dbo.diag_result_price_band_range(tenant_id, query_hash, data_version, price_band_min);
END
GO

IF OBJECT_ID(N'dbo.diag_result_price_band_line', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_price_band_line
    (
        id            BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id     NVARCHAR(32)   NOT NULL,
        query_hash    NVARCHAR(128)  NOT NULL,
        data_version  NVARCHAR(64)   NOT NULL,
        price_line    DECIMAL(20,2)  NOT NULL,
        sku           INT            NOT NULL,
        sales         DECIMAL(20,2)  NOT NULL,
        snapshot_time DATETIME2(0)   NOT NULL,
        create_time   DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time   DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'idx_diag_result_price_band_line_qv'
      AND object_id = OBJECT_ID(N'dbo.diag_result_price_band_line')
)
BEGIN
    CREATE INDEX idx_diag_result_price_band_line_qv
        ON dbo.diag_result_price_band_line(tenant_id, query_hash, data_version, price_line);
END
GO

IF OBJECT_ID(N'dbo.diag_result_price_band_point', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_price_band_point
    (
        id             BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id      NVARCHAR(32)   NOT NULL,
        query_hash     NVARCHAR(128)  NOT NULL,
        data_version   NVARCHAR(64)   NOT NULL,
        price_band_min DECIMAL(20,2)  NOT NULL,
        price_band_max DECIMAL(20,2)  NULL,
        sale_price     DECIMAL(20,2)  NOT NULL,
        sku            INT            NOT NULL,
        sales          DECIMAL(20,2)  NOT NULL,
        sale_quantity  DECIMAL(20,2)  NOT NULL,
        total_sales    DECIMAL(20,2)  NOT NULL,
        wave_type      NVARCHAR(16)   NOT NULL,
        snapshot_time  DATETIME2(0)   NOT NULL,
        create_time    DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time    DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'idx_diag_result_price_band_point_qv'
      AND object_id = OBJECT_ID(N'dbo.diag_result_price_band_point')
)
BEGIN
    CREATE INDEX idx_diag_result_price_band_point_qv
        ON dbo.diag_result_price_band_point(tenant_id, query_hash, data_version, price_band_min);
END
GO

IF OBJECT_ID(N'dbo.diag_result_price_band_sku', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_price_band_sku
    (
        id                     BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id              NVARCHAR(32)   NOT NULL,
        query_hash             NVARCHAR(128)  NOT NULL,
        data_version           NVARCHAR(64)   NOT NULL,
        price_band_min         DECIMAL(20,2)  NOT NULL,
        price_band_max         DECIMAL(20,2)  NULL,
        price_band_label       NVARCHAR(64)   NOT NULL,
        promotion_flag         NVARCHAR(8)    NULL,
        product_status         NVARCHAR(64)   NULL,
        product_status_no      NVARCHAR(32)   NULL,
        product_no             NVARCHAR(64)   NOT NULL,
        product_name           NVARCHAR(255)  NULL,
        store_num              INT            NULL,
        sale_quantity          DECIMAL(20,2)  NOT NULL,
        sale_quantity_psd      DECIMAL(20,4)  NOT NULL,
        sales                  DECIMAL(20,2)  NOT NULL,
        sales_per              DECIMAL(20,2)  NOT NULL,
        sales_psd              DECIMAL(20,4)  NOT NULL,
        gross                  DECIMAL(20,2)  NOT NULL,
        gross_per              DECIMAL(20,2)  NOT NULL,
        gross_psd              DECIMAL(20,4)  NOT NULL,
        gross_rate             DECIMAL(20,2)  NOT NULL,
        stock_quantity         DECIMAL(20,2)  NOT NULL,
        turnover_rate          DECIMAL(20,4)  NOT NULL,
        turnover_days          DECIMAL(20,4)  NOT NULL,
        stock_sales_rate       DECIMAL(20,2)  NOT NULL,
        contribution_rate      DECIMAL(20,2)  NOT NULL,
        gmroi                  DECIMAL(20,4)  NOT NULL,
        sales_rate             DECIMAL(20,2)  NOT NULL,
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
        class_level            INT            NULL,
        product_barcode        NVARCHAR(64)   NULL,
        brand_name             NVARCHAR(255)  NULL,
        spec                   NVARCHAR(255)  NULL,
        in_price               DECIMAL(20,2)  NOT NULL,
        sales_price            DECIMAL(20,2)  NOT NULL,
        avg_deal_price         DECIMAL(20,2)  NOT NULL,
        product_vendor_no      NVARCHAR(64)   NULL,
        product_vendor_name    NVARCHAR(255)  NULL,
        product_vendor_no_name NVARCHAR(255)  NULL,
        snapshot_time          DATETIME2(0)   NOT NULL,
        create_time            DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time            DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'idx_diag_result_price_band_sku_qv'
      AND object_id = OBJECT_ID(N'dbo.diag_result_price_band_sku')
)
BEGIN
    CREATE INDEX idx_diag_result_price_band_sku_qv
        ON dbo.diag_result_price_band_sku(tenant_id, query_hash, data_version, price_band_label, product_no);
END
GO

PRINT N'[OK] diagnosis price band schema upgrade done';
GO
