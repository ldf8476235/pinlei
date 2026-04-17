IF OBJECT_ID(N'dbo.diag_result_tag_metric', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_tag_metric
    (
        id                      BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id               NVARCHAR(20)   NOT NULL DEFAULT N'000000',
        query_hash              NVARCHAR(64)   NOT NULL,
        data_version            NVARCHAR(64)   NOT NULL,
        tag_type                NVARCHAR(20)   NOT NULL,
        tag_type_name           NVARCHAR(64)   NULL,
        tag_no                  NVARCHAR(128)  NULL,
        tag_name                NVARCHAR(255)  NULL,
        metric_key              NVARCHAR(160)  NOT NULL,
        is_untagged             BIT            NOT NULL DEFAULT 0,
        sku_count               INT            NOT NULL DEFAULT 0,
        compare_sku_count       INT            NOT NULL DEFAULT 0,
        sku_change              INT            NOT NULL DEFAULT 0,
        sku_inc                 DECIMAL(20,6)  NULL,
        sku_per                 DECIMAL(20,6)  NOT NULL DEFAULT 0,
        sale_quantity           DECIMAL(20,4)  NOT NULL DEFAULT 0,
        compare_sale_quantity   DECIMAL(20,4)  NOT NULL DEFAULT 0,
        sale_quantity_change    DECIMAL(20,4)  NOT NULL DEFAULT 0,
        sale_quantity_inc       DECIMAL(20,6)  NULL,
        sale_quantity_per       DECIMAL(20,6)  NOT NULL DEFAULT 0,
        sale_quantity_psd       DECIMAL(20,6)  NOT NULL DEFAULT 0,
        sales                   DECIMAL(20,4)  NOT NULL DEFAULT 0,
        compare_sales           DECIMAL(20,4)  NOT NULL DEFAULT 0,
        sales_change            DECIMAL(20,4)  NOT NULL DEFAULT 0,
        sales_inc               DECIMAL(20,6)  NULL,
        sales_per               DECIMAL(20,6)  NOT NULL DEFAULT 0,
        sales_psd               DECIMAL(20,6)  NOT NULL DEFAULT 0,
        gross                   DECIMAL(20,4)  NOT NULL DEFAULT 0,
        compare_gross           DECIMAL(20,4)  NOT NULL DEFAULT 0,
        gross_change            DECIMAL(20,4)  NOT NULL DEFAULT 0,
        gross_inc               DECIMAL(20,6)  NULL,
        gross_per               DECIMAL(20,6)  NOT NULL DEFAULT 0,
        gross_psd               DECIMAL(20,6)  NOT NULL DEFAULT 0,
        gross_rate              DECIMAL(20,6)  NULL,
        compare_gross_rate      DECIMAL(20,6)  NULL,
        gross_rate_inc          DECIMAL(20,6)  NULL,
        stock_quantity          DECIMAL(20,6)  NOT NULL DEFAULT 0,
        compare_stock_quantity  DECIMAL(20,6)  NOT NULL DEFAULT 0,
        sales_cost              DECIMAL(20,4)  NOT NULL DEFAULT 0,
        compare_sales_cost      DECIMAL(20,4)  NOT NULL DEFAULT 0,
        turnover_rate           DECIMAL(20,6)  NULL,
        turnover_days           DECIMAL(20,6)  NULL,
        stock_sales_rate        DECIMAL(20,6)  NULL,
        contribution_rate       DECIMAL(20,6)  NULL,
        gmroi                   DECIMAL(20,6)  NULL,
        sales_rate              DECIMAL(20,6)  NULL,
        activity_sku            INT            NOT NULL DEFAULT 0,
        active_store_count      INT            NOT NULL DEFAULT 0,
        period_days             INT            NOT NULL DEFAULT 0,
        snapshot_time           DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time             DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time             DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_tag_metric_qvk'
      AND object_id = OBJECT_ID(N'dbo.diag_result_tag_metric')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_tag_metric_qvk
        ON dbo.diag_result_tag_metric(tenant_id, query_hash, data_version, metric_key);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_tag_metric_qvt_sales'
      AND object_id = OBJECT_ID(N'dbo.diag_result_tag_metric')
)
BEGIN
    CREATE INDEX ix_diag_result_tag_metric_qvt_sales
        ON dbo.diag_result_tag_metric(tenant_id, query_hash, data_version, tag_type, sales DESC, metric_key ASC);
END
GO

IF OBJECT_ID(N'dbo.diag_result_tag_json', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_tag_json
    (
        id            BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id     NVARCHAR(20)   NOT NULL DEFAULT N'000000',
        query_hash    NVARCHAR(64)   NOT NULL,
        data_version  NVARCHAR(64)   NOT NULL,
        payload_code  NVARCHAR(64)   NOT NULL,
        payload_name  NVARCHAR(128)  NULL,
        payload_json  NVARCHAR(MAX)  NOT NULL,
        snapshot_time DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time   DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time   DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_tag_json_qvp'
      AND object_id = OBJECT_ID(N'dbo.diag_result_tag_json')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_tag_json_qvp
        ON dbo.diag_result_tag_json(tenant_id, query_hash, data_version, payload_code);
END
GO
