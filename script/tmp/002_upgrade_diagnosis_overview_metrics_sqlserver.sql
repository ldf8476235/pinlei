-- 诊断模块增量升级脚本（SQL Server）
-- 用途：在已存在 diagnosis 基础结构的环境上，补齐 overview 扩展指标与查询索引
-- 说明：
-- 1) 本脚本合并自历史 iter4 / iter5
-- 2) 适用于已执行过 001_init_diagnosis_schema_sqlserver.sql 的环境
-- 3) 若是历史老环境追溯，请参考 script/tmp/archive 中的原始迭代脚本

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

-- =========================
-- 0. precompute job orchestrator 扩展
-- =========================
IF COL_LENGTH('dbo.diag_precompute_job', 'orchestrator_status') IS NULL
BEGIN
    ALTER TABLE dbo.diag_precompute_job
        ADD orchestrator_status NVARCHAR(32) NOT NULL
            CONSTRAINT DF_diag_precompute_job_orchestrator_status DEFAULT (N'PENDING');
END
GO

IF COL_LENGTH('dbo.diag_precompute_job', 'module_progress_json') IS NULL
BEGIN
    ALTER TABLE dbo.diag_precompute_job
        ADD module_progress_json NVARCHAR(MAX) NULL;
END
GO

-- =========================
-- 0.1 result publish version
-- =========================
IF OBJECT_ID(N'dbo.diag_result_publish_version', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_publish_version
    (
        id                 BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id          NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash         NVARCHAR(64)  NOT NULL,
        data_version       NVARCHAR(64)  NOT NULL,
        publish_status     NVARCHAR(32)  NOT NULL,
        job_id             BIGINT        NULL,
        error_summary      NVARCHAR(1000) NULL,
        published_time     DATETIME2(0)  NULL,
        create_time        DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time        DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_publish_ver'
      AND object_id = OBJECT_ID(N'dbo.diag_result_publish_version')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_publish_ver
        ON dbo.diag_result_publish_version(tenant_id, query_hash, data_version);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_publish_query_status'
      AND object_id = OBJECT_ID(N'dbo.diag_result_publish_version')
)
BEGIN
    CREATE INDEX ix_diag_result_publish_query_status
        ON dbo.diag_result_publish_version(tenant_id, query_hash, publish_status, update_time DESC);
END
GO

-- =========================
-- A. overview 补充业务字段
-- =========================
IF COL_LENGTH('dbo.diag_result_overview', 'store_scope') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD store_scope NVARCHAR(1000) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'period_type') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD period_type NVARCHAR(32) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_sale_quantity') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_sale_quantity DECIMAL(20,6) NOT NULL CONSTRAINT DF_diag_result_overview_metric_sale_quantity DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_sales_cost') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_sales_cost DECIMAL(20,6) NOT NULL CONSTRAINT DF_diag_result_overview_metric_sales_cost DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_customer_count') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_customer_count DECIMAL(20,6) NOT NULL CONSTRAINT DF_diag_result_overview_metric_customer_count DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_customer_count_total') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_customer_count_total DECIMAL(20,6) NOT NULL CONSTRAINT DF_diag_result_overview_metric_customer_count_total DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_customer_price') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_customer_price DECIMAL(20,6) NOT NULL CONSTRAINT DF_diag_result_overview_metric_customer_price DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_customer_avg_quantity') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_customer_avg_quantity DECIMAL(20,6) NOT NULL CONSTRAINT DF_diag_result_overview_metric_customer_avg_quantity DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_piece_avg_price') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_piece_avg_price DECIMAL(20,6) NOT NULL CONSTRAINT DF_diag_result_overview_metric_piece_avg_price DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_avg_inventory') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_avg_inventory DECIMAL(20,6) NOT NULL CONSTRAINT DF_diag_result_overview_metric_avg_inventory DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_inventory_sales_ratio') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_inventory_sales_ratio DECIMAL(20,6) NOT NULL CONSTRAINT DF_diag_result_overview_metric_inventory_sales_ratio DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_inventory_turnover_days') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_inventory_turnover_days DECIMAL(20,6) NOT NULL CONSTRAINT DF_diag_result_overview_metric_inventory_turnover_days DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_penetrate_rate') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_penetrate_rate DECIMAL(20,6) NOT NULL CONSTRAINT DF_diag_result_overview_metric_penetrate_rate DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_sales') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_sales DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_gross') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_gross DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_sale_quantity') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_sale_quantity DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_sales_cost') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_sales_cost DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_customer_count') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_customer_count DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_customer_count_total') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_customer_count_total DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_avg_inventory') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_avg_inventory DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_inventory_sales_ratio') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_inventory_sales_ratio DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_inventory_turnover_days') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_inventory_turnover_days DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_penetrate_rate') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_penetrate_rate DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_customer_price') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_customer_price DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_customer_avg_quantity') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_customer_avg_quantity DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_piece_avg_price') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_piece_avg_price DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_profit_margin') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_profit_margin DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_sales_rate') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_sales_rate DECIMAL(20,6) NULL;
END
GO

IF COL_LENGTH('dbo.diag_result_overview', 'metric_compare_total_sku') IS NULL
BEGIN
    ALTER TABLE dbo.diag_result_overview
        ADD metric_compare_total_sku INT NULL;
END
GO

-- =========================
-- B. 查询索引补充
-- =========================
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_trends_query_ver'
      AND object_id = OBJECT_ID(N'dbo.diag_result_trends')
)
BEGIN
    CREATE INDEX ix_diag_result_trends_query_ver
        ON dbo.diag_result_trends(tenant_id, query_hash, data_version, metric_code, point_date);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_overview_query_ver'
      AND object_id = OBJECT_ID(N'dbo.diag_result_overview')
)
BEGIN
    CREATE INDEX ix_diag_result_overview_query_ver
        ON dbo.diag_result_overview(tenant_id, query_hash, data_version, snapshot_time DESC);
END
GO

PRINT N'[OK] diagnosis overview metrics upgrade done';
GO

