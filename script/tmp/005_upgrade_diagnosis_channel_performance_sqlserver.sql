-- diagnosis 渠道业绩预计算结果层升级脚本 (SQL Server)

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.diag_result_channel_contribution', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_channel_contribution
    (
        id                            BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id                     NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash                    NVARCHAR(64)  NOT NULL,
        data_version                  NVARCHAR(64)  NOT NULL,
        sale_channel                  TINYINT       NOT NULL,
        online_type                   NVARCHAR(40)  NULL,
        online_name                   NVARCHAR(100) NULL,
        channel_name                  NVARCHAR(120) NOT NULL DEFAULT N'',
        retail_type_id                NVARCHAR(32)  NULL,
        dept_id                       BIGINT        NULL,
        business_circle_id            NVARCHAR(32)  NULL,
        dept_group_id                 NVARCHAR(32)  NULL,
        store_no                      NVARCHAR(32)  NULL,
        period_start                  DATE          NOT NULL,
        period_end                    DATE          NOT NULL,
        compare_start                 DATE          NULL,
        compare_end                   DATE          NULL,
        current_sales                 DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_sales_per             DECIMAL(12,2) NOT NULL DEFAULT 0,
        current_gross                 DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_gross_per             DECIMAL(12,2) NOT NULL DEFAULT 0,
        current_gross_rate            DECIMAL(12,2) NOT NULL DEFAULT 0,
        current_customer_count        DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_customer_price        DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_sales                 DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_sales_per             DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_sales_inc             DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_gross                 DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_gross_per             DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_gross_inc             DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_gross_rate            DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_customer_count        DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_customer_count_inc    DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_customer_price        DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_customer_price_inc    DECIMAL(12,2) NOT NULL DEFAULT 0,
        snapshot_time                 DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time                   DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time                   DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.diag_result_channel_trend', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_channel_trend
    (
        id                            BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id                     NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash                    NVARCHAR(64)  NOT NULL,
        data_version                  NVARCHAR(64)  NOT NULL,
        sale_channel                  TINYINT       NOT NULL,
        online_type                   NVARCHAR(40)  NULL,
        online_name                   NVARCHAR(100) NULL,
        channel_name                  NVARCHAR(120) NOT NULL DEFAULT N'',
        retail_type_id                NVARCHAR(32)  NULL,
        dept_id                       BIGINT        NULL,
        business_circle_id            NVARCHAR(32)  NULL,
        dept_group_id                 NVARCHAR(32)  NULL,
        store_no                      NVARCHAR(32)  NULL,
        point_index                   INT           NOT NULL,
        point_date                    DATE          NOT NULL,
        current_sales                 DECIMAL(20,2) NOT NULL DEFAULT 0,
        snapshot_time                 DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time                   DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time                   DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.diag_result_channel_contribution', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH(N'dbo.diag_result_channel_contribution', N'channel_name') IS NULL
        ALTER TABLE dbo.diag_result_channel_contribution ADD channel_name NVARCHAR(120) NOT NULL CONSTRAINT DF_diag_channel_contrib_channel_name DEFAULT N'' WITH VALUES;
    IF COL_LENGTH(N'dbo.diag_result_channel_contribution', N'current_customer_count') IS NULL
        ALTER TABLE dbo.diag_result_channel_contribution ADD current_customer_count DECIMAL(20,2) NOT NULL CONSTRAINT DF_diag_channel_contrib_curr_customer_cnt DEFAULT 0 WITH VALUES;
    IF COL_LENGTH(N'dbo.diag_result_channel_contribution', N'current_customer_price') IS NULL
        ALTER TABLE dbo.diag_result_channel_contribution ADD current_customer_price DECIMAL(20,2) NOT NULL CONSTRAINT DF_diag_channel_contrib_curr_customer_price DEFAULT 0 WITH VALUES;
    IF COL_LENGTH(N'dbo.diag_result_channel_contribution', N'compare_customer_count') IS NULL
        ALTER TABLE dbo.diag_result_channel_contribution ADD compare_customer_count DECIMAL(20,2) NOT NULL CONSTRAINT DF_diag_channel_contrib_cmp_customer_cnt DEFAULT 0 WITH VALUES;
    IF COL_LENGTH(N'dbo.diag_result_channel_contribution', N'compare_customer_count_inc') IS NULL
        ALTER TABLE dbo.diag_result_channel_contribution ADD compare_customer_count_inc DECIMAL(12,2) NOT NULL CONSTRAINT DF_diag_channel_contrib_cmp_customer_cnt_inc DEFAULT 0 WITH VALUES;
    IF COL_LENGTH(N'dbo.diag_result_channel_contribution', N'compare_customer_price') IS NULL
        ALTER TABLE dbo.diag_result_channel_contribution ADD compare_customer_price DECIMAL(20,2) NOT NULL CONSTRAINT DF_diag_channel_contrib_cmp_customer_price DEFAULT 0 WITH VALUES;
    IF COL_LENGTH(N'dbo.diag_result_channel_contribution', N'compare_customer_price_inc') IS NULL
        ALTER TABLE dbo.diag_result_channel_contribution ADD compare_customer_price_inc DECIMAL(12,2) NOT NULL CONSTRAINT DF_diag_channel_contrib_cmp_customer_price_inc DEFAULT 0 WITH VALUES;
    IF COL_LENGTH(N'dbo.diag_result_channel_contribution', N'compare_sales_inc') IS NULL
        ALTER TABLE dbo.diag_result_channel_contribution ADD compare_sales_inc DECIMAL(12,2) NOT NULL CONSTRAINT DF_diag_channel_contrib_cmp_sales_inc DEFAULT 0 WITH VALUES;
    IF COL_LENGTH(N'dbo.diag_result_channel_contribution', N'compare_gross_inc') IS NULL
        ALTER TABLE dbo.diag_result_channel_contribution ADD compare_gross_inc DECIMAL(12,2) NOT NULL CONSTRAINT DF_diag_channel_contrib_cmp_gross_inc DEFAULT 0 WITH VALUES;
END
GO

IF OBJECT_ID(N'dbo.diag_result_channel_trend', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH(N'dbo.diag_result_channel_trend', N'channel_name') IS NULL
        ALTER TABLE dbo.diag_result_channel_trend ADD channel_name NVARCHAR(120) NOT NULL CONSTRAINT DF_diag_channel_trend_channel_name DEFAULT N'' WITH VALUES;
    IF COL_LENGTH(N'dbo.diag_result_channel_trend', N'current_sales') IS NULL
        ALTER TABLE dbo.diag_result_channel_trend ADD current_sales DECIMAL(20,2) NOT NULL CONSTRAINT DF_diag_channel_trend_current_sales DEFAULT 0 WITH VALUES;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_channel_contrib_query_ver_channel'
      AND object_id = OBJECT_ID(N'dbo.diag_result_channel_contribution')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_channel_contrib_query_ver_channel
        ON dbo.diag_result_channel_contribution
        (
            tenant_id,
            query_hash,
            data_version,
            sale_channel,
            online_type
        );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_channel_contrib_query_ver'
      AND object_id = OBJECT_ID(N'dbo.diag_result_channel_contribution')
)
BEGIN
    CREATE INDEX ix_diag_result_channel_contrib_query_ver
        ON dbo.diag_result_channel_contribution
        (
            tenant_id,
            query_hash,
            data_version,
            sale_channel,
            online_type
        );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_channel_trend_query_ver_channel_point'
      AND object_id = OBJECT_ID(N'dbo.diag_result_channel_trend')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_channel_trend_query_ver_channel_point
        ON dbo.diag_result_channel_trend
        (
            tenant_id,
            query_hash,
            data_version,
            sale_channel,
            online_type,
            point_index
        );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_channel_trend_query_ver_point'
      AND object_id = OBJECT_ID(N'dbo.diag_result_channel_trend')
)
BEGIN
    CREATE INDEX ix_diag_result_channel_trend_query_ver_point
        ON dbo.diag_result_channel_trend
        (
            tenant_id,
            query_hash,
            data_version,
            point_index,
            point_date,
            sale_channel,
            online_type
        );
END
GO

PRINT N'[OK] diagnosis channel performance schema upgrade done';
GO
