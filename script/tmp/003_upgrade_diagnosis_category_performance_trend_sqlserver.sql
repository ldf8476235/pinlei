-- 诊断模块增量升级脚本（SQL Server）
-- 用途：新增 `/salesStoreClass/trendChanges` 专用趋势宽表结果层

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.diag_result_category_performance_trend', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_category_performance_trend
    (
        id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id           NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash          NVARCHAR(64)  NOT NULL,
        data_version        NVARCHAR(64)  NOT NULL,
        period_flag         CHAR(1)       NOT NULL,
        point_index         INT           NOT NULL,
        point_date          DATE          NOT NULL,
        class_level         INT           NULL,
        class_no            NVARCHAR(32)  NULL,
        class_name          NVARCHAR(100) NULL,
        retail_type_id      NVARCHAR(32)  NULL,
        dept_id             BIGINT        NULL,
        business_circle_id  NVARCHAR(32)  NULL,
        dept_group_id       NVARCHAR(32)  NULL,
        store_no            NVARCHAR(32)  NULL,
        sales               DECIMAL(20,2) NOT NULL DEFAULT 0,
        sale_quantity       DECIMAL(20,2) NOT NULL DEFAULT 0,
        gross               DECIMAL(20,2) NOT NULL DEFAULT 0,
        gross_rate          DECIMAL(12,2) NOT NULL DEFAULT 0,
        customer_count      DECIMAL(20,2) NOT NULL DEFAULT 0,
        customer_price      DECIMAL(20,2) NOT NULL DEFAULT 0,
        sale_cost           DECIMAL(20,2) NOT NULL DEFAULT 0,
        stock_cost          DECIMAL(20,2) NOT NULL DEFAULT 0,
        stock_cost_rate     DECIMAL(12,2) NOT NULL DEFAULT 0,
        snapshot_time       DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time         DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time         DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_cat_perf_trend_hash_ver_flag_idx'
      AND object_id = OBJECT_ID(N'dbo.diag_result_category_performance_trend')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_cat_perf_trend_hash_ver_flag_idx
        ON dbo.diag_result_category_performance_trend
        (
            tenant_id,
            query_hash,
            data_version,
            period_flag,
            point_index
        );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_cat_perf_trend_query_ver'
      AND object_id = OBJECT_ID(N'dbo.diag_result_category_performance_trend')
)
BEGIN
    CREATE INDEX ix_diag_result_cat_perf_trend_query_ver
        ON dbo.diag_result_category_performance_trend
        (
            tenant_id,
            query_hash,
            data_version,
            period_flag,
            point_index,
            point_date
        );
END
GO

PRINT N'[OK] diagnosis category performance trend schema upgrade done';
GO
