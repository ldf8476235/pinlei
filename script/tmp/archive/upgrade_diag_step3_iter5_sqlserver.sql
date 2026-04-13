-- Step3 迭代5 增量脚本（SQL Server）
-- 目标：补齐 classPerformance 对比衍生指标存储列，支撑 Step3 完整落库

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
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

PRINT N'[OK] step3-iter5 schema upgrade done';
GO
