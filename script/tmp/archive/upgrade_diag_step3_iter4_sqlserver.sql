-- 诊断模块 Step3 迭代4：结果表补字段（classPerformance 对齐）
-- 说明：
-- 1) 本脚本仅调整结果层表结构，不改业务代码
-- 2) 目标：补齐 /api/v1/diagnosis/overview 所需字段，避免大量 null
-- 3) 执行库：diagnosis（SQL Server）

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

-- =========================
-- A. 概览快照表补字段
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

-- =========================
-- B. 趋势快照表补索引（为 gross/customer_count 趋势预留）
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

PRINT N'[OK] diag Step3 iter4 schema upgrade done';
GO

