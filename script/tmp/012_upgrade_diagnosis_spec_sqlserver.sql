-- diagnosis 规格分析预计算结果层升级脚本 (SQL Server)

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.diag_result_spec_overview', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_spec_overview
    (
        id            BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id     NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash    NVARCHAR(64)  NOT NULL,
        data_version  NVARCHAR(64)  NOT NULL,
        total_num     INT           NOT NULL DEFAULT 0,
        new_num       INT           NOT NULL DEFAULT 0,
        snapshot_time DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time   DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time   DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_spec_overview_qv'
      AND object_id = OBJECT_ID(N'dbo.diag_result_spec_overview')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_spec_overview_qv
        ON dbo.diag_result_spec_overview(tenant_id, query_hash, data_version);
END
GO

IF OBJECT_ID(N'dbo.diag_result_spec_metric', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_spec_metric
    (
        id                      BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id               NVARCHAR(20)   NOT NULL DEFAULT N'000000',
        query_hash              NVARCHAR(64)   NOT NULL,
        data_version            NVARCHAR(64)   NOT NULL,
        spec_no                 NVARCHAR(255)  NOT NULL,
        spec_name               NVARCHAR(255)  NULL,
        spec_type               NVARCHAR(8)    NULL,
        spec_type_name          NVARCHAR(32)   NULL,
        new_spec_type           NVARCHAR(8)    NOT NULL DEFAULT N'0',
        new_spec_type_name      NVARCHAR(32)   NULL,
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
    WHERE name = N'ux_diag_result_spec_metric_qvs'
      AND object_id = OBJECT_ID(N'dbo.diag_result_spec_metric')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_spec_metric_qvs
        ON dbo.diag_result_spec_metric(tenant_id, query_hash, data_version, spec_no);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_spec_metric_qv_sales'
      AND object_id = OBJECT_ID(N'dbo.diag_result_spec_metric')
)
BEGIN
    CREATE INDEX ix_diag_result_spec_metric_qv_sales
        ON dbo.diag_result_spec_metric(tenant_id, query_hash, data_version, sales DESC, spec_no ASC);
END
GO

IF OBJECT_ID(N'dbo.diag_result_spec_json', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_spec_json
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
    WHERE name = N'ux_diag_result_spec_json_qvp'
      AND object_id = OBJECT_ID(N'dbo.diag_result_spec_json')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_spec_json_qvp
        ON dbo.diag_result_spec_json(tenant_id, query_hash, data_version, payload_code);
END
GO

PRINT N'[OK] diagnosis spec schema upgrade done';
GO
