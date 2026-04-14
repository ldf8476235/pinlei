-- 诊断模块增量升级脚本（SQL Server）
-- 用途：新增“子类贡献”结果层表，支撑以下旧版兼容接口：
-- 1) /salesStoreClass/sonClassSalesPer
-- 2) /salesStoreClass/sonClassSalesTrendChart
-- 3) /salesStoreClass/sonClassSalesList

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.diag_result_subclass_contribution', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_subclass_contribution
    (
        id                           BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id                    NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash                   NVARCHAR(64)  NOT NULL,
        data_version                 NVARCHAR(64)  NOT NULL,
        parent_class_level           INT           NULL,
        parent_class_no              NVARCHAR(32)  NULL,
        sub_class_level              INT           NULL,
        sub_class_no                 NVARCHAR(32)  NOT NULL,
        sub_class_name               NVARCHAR(100) NULL,
        retail_type_id               NVARCHAR(32)  NULL,
        dept_id                      BIGINT        NULL,
        business_circle_id           NVARCHAR(32)  NULL,
        dept_group_id                NVARCHAR(32)  NULL,
        store_no                     NVARCHAR(32)  NULL,
        period_start                 DATE          NOT NULL,
        period_end                   DATE          NOT NULL,
        compare_start                DATE          NULL,
        compare_end                  DATE          NULL,
        current_sales                DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_sales_per            DECIMAL(12,2) NOT NULL DEFAULT 0,
        current_gross                DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_gross_per            DECIMAL(12,2) NOT NULL DEFAULT 0,
        current_gross_rate           DECIMAL(12,2) NOT NULL DEFAULT 0,
        current_sale_quantity        DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_customer_count       DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_customer_price       DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_avg_inventory        DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_turnover_rate        DECIMAL(12,2) NOT NULL DEFAULT 0,
        current_turnover_days        DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_gmroi                DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_sale_cost            DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_total_sku            INT           NOT NULL DEFAULT 0,
        current_active_sku           INT           NOT NULL DEFAULT 0,
        compare_sales                DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_sales_per            DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_sales_add_rate       DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_gross                DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_gross_per            DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_gross_add_rate       DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_gross_rate           DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_sale_quantity        DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_sale_quantity_add_rate DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_customer_count       DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_customer_price       DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_customer_price_add_rate DECIMAL(12,2) NOT NULL DEFAULT 0,
        snapshot_time                DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time                  DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time                  DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.diag_result_subclass_trend', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_subclass_trend
    (
        id                           BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id                    NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash                   NVARCHAR(64)  NOT NULL,
        data_version                 NVARCHAR(64)  NOT NULL,
        parent_class_level           INT           NULL,
        parent_class_no              NVARCHAR(32)  NULL,
        sub_class_level              INT           NULL,
        sub_class_no                 NVARCHAR(32)  NOT NULL,
        sub_class_name               NVARCHAR(100) NULL,
        retail_type_id               NVARCHAR(32)  NULL,
        dept_id                      BIGINT        NULL,
        business_circle_id           NVARCHAR(32)  NULL,
        dept_group_id                NVARCHAR(32)  NULL,
        store_no                     NVARCHAR(32)  NULL,
        point_index                  INT           NOT NULL,
        point_date                   DATE          NOT NULL,
        sales                        DECIMAL(20,2) NOT NULL DEFAULT 0,
        snapshot_time                DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time                  DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time                  DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_subclass_contrib_query_ver_subclass'
      AND object_id = OBJECT_ID(N'dbo.diag_result_subclass_contribution')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_subclass_contrib_query_ver_subclass
        ON dbo.diag_result_subclass_contribution
        (
            tenant_id,
            query_hash,
            data_version,
            sub_class_no
        );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_subclass_contrib_query_ver'
      AND object_id = OBJECT_ID(N'dbo.diag_result_subclass_contribution')
)
BEGIN
    CREATE INDEX ix_diag_result_subclass_contrib_query_ver
        ON dbo.diag_result_subclass_contribution
        (
            tenant_id,
            query_hash,
            data_version,
            parent_class_no,
            sub_class_no
        );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_subclass_trend_query_ver_subclass_point'
      AND object_id = OBJECT_ID(N'dbo.diag_result_subclass_trend')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_subclass_trend_query_ver_subclass_point
        ON dbo.diag_result_subclass_trend
        (
            tenant_id,
            query_hash,
            data_version,
            sub_class_no,
            point_index
        );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_subclass_trend_query_ver_point'
      AND object_id = OBJECT_ID(N'dbo.diag_result_subclass_trend')
)
BEGIN
    CREATE INDEX ix_diag_result_subclass_trend_query_ver_point
        ON dbo.diag_result_subclass_trend
        (
            tenant_id,
            query_hash,
            data_version,
            point_index,
            point_date,
            sub_class_no
        );
END
GO

PRINT N'[OK] diagnosis subclass contribution schema upgrade done';
GO
