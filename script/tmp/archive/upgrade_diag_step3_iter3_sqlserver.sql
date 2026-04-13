-- Step3 迭代批次 3/5 增量脚本（SQL Server）
-- 目标：扩展结果层（角色分布/洞察）

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.diag_result_role_distribution', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_role_distribution
    (
        id              BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id       NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash      NVARCHAR(64)  NOT NULL,
        role_code       NVARCHAR(64)  NOT NULL,
        role_name       NVARCHAR(100) NULL,
        sales_amount    DECIMAL(20,4) NOT NULL DEFAULT 0,
        sku_count       INT           NOT NULL DEFAULT 0,
        sales_ratio     DECIMAL(12,6) NOT NULL DEFAULT 0,
        data_version    NVARCHAR(64)  NOT NULL,
        snapshot_time   DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time     DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time     DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_role_dist_hash_role_ver'
      AND object_id = OBJECT_ID(N'dbo.diag_result_role_distribution')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_role_dist_hash_role_ver
        ON dbo.diag_result_role_distribution(tenant_id, query_hash, role_code, data_version);
END
GO

IF OBJECT_ID(N'dbo.diag_result_insights', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_insights
    (
        id              BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id       NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash      NVARCHAR(64)  NOT NULL,
        insight_type    NVARCHAR(64)  NOT NULL,
        insight_code    NVARCHAR(64)  NOT NULL,
        title           NVARCHAR(200) NOT NULL,
        content         NVARCHAR(2000) NULL,
        severity        NVARCHAR(16)  NULL,
        sort_no         INT           NOT NULL DEFAULT 0,
        data_version    NVARCHAR(64)  NOT NULL,
        snapshot_time   DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time     DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time     DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_insights_hash_code_ver'
      AND object_id = OBJECT_ID(N'dbo.diag_result_insights')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_insights_hash_code_ver
        ON dbo.diag_result_insights(tenant_id, query_hash, insight_code, data_version);
END
GO

PRINT N'[OK] step3-iter3 增量脚本执行完成';

