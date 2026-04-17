-- 诊断模块第1步建表脚本（SQL Server）
-- 用途：新环境/新库一次性初始化 diagnosis 结果层与任务中心结构
-- 说明：
-- 1) 本脚本已吸收早期 iter2 / iter3 的结构变更，不需要再额外执行对应脚本
-- 2) 当前建议执行顺序：
--    第一步：执行本脚本
--    第二步：执行 002_upgrade_diagnosis_overview_metrics_sqlserver.sql
-- 3) 历史增量脚本已归档到 script/tmp/archive，仅用于老环境追溯或特殊补丁
-- 包含：
-- 1) diag_precompute_job
-- 2) diag_precompute_window
-- 3) diag_precompute_event
-- 3.1) diag_result_publish_version
-- 4) diag_result_overview
-- 5) diag_result_trends
-- 6) diag_result_role_distribution
-- 7) diag_result_insights
-- 8) diag_result_brand_overview
-- 9) diag_result_brand_metric
-- 10) diag_result_brand_json
-- 11) diag_result_spec_overview
-- 12) diag_result_spec_metric
-- 13) diag_result_spec_json
-- 14) diag_result_tag_metric
-- 15) diag_result_tag_json

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

-- =========================
-- 1. 预计算任务主表
-- =========================
IF OBJECT_ID(N'dbo.diag_precompute_job', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_precompute_job
    (
        job_id           BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id        NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        job_code         NVARCHAR(64)  NOT NULL,
        module_code      NVARCHAR(32)  NOT NULL,
        status_code      NVARCHAR(32)  NOT NULL,
        priority         INT           NOT NULL DEFAULT 5,
        request_json     NVARCHAR(MAX) NULL,
        request_hash     NVARCHAR(64)  NULL,
        read_range_type  NVARCHAR(32)  NULL,
        read_start       DATE          NULL,
        read_end         DATE          NULL,
        window_types     NVARCHAR(200) NULL,
        force_rebuild    CHAR(1)       NOT NULL DEFAULT 'N',
        max_retry        INT           NOT NULL DEFAULT 1,
        retry_count      INT           NOT NULL DEFAULT 0,
        progress_percent DECIMAL(5,2)  NOT NULL DEFAULT 0,
        current_stage    NVARCHAR(32)  NULL,
        total_windows    INT           NOT NULL DEFAULT 0,
        done_windows     INT           NOT NULL DEFAULT 0,
        rows_read        BIGINT        NOT NULL DEFAULT 0,
        rows_written     BIGINT        NOT NULL DEFAULT 0,
        orchestrator_status NVARCHAR(32)  NOT NULL DEFAULT N'PENDING',
        module_progress_json NVARCHAR(MAX) NULL,
        submitted_by     BIGINT        NULL,
        submitted_time   DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        started_time     DATETIME2(0)  NULL,
        finished_time    DATETIME2(0)  NULL,
        error_message    NVARCHAR(1000) NULL,
        create_time      DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time      DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_precompute_job_code'
      AND object_id = OBJECT_ID(N'dbo.diag_precompute_job')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_precompute_job_code
        ON dbo.diag_precompute_job(tenant_id, job_code);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_precompute_job_status_time'
      AND object_id = OBJECT_ID(N'dbo.diag_precompute_job')
)
BEGIN
    CREATE INDEX ix_diag_precompute_job_status_time
        ON dbo.diag_precompute_job(tenant_id, status_code, submitted_time DESC);
END
GO

-- =========================
-- 2. 预计算窗口明细表
-- =========================
IF OBJECT_ID(N'dbo.diag_precompute_window', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_precompute_window
    (
        window_id         BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id         NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        job_id            BIGINT        NOT NULL,
        window_key        NVARCHAR(120) NOT NULL,
        window_type       NVARCHAR(32)  NOT NULL,
        period_start      DATE          NOT NULL,
        period_end        DATE          NOT NULL,
        compare_start     DATE          NULL,
        compare_end       DATE          NULL,
        status_code       NVARCHAR(32)  NOT NULL,
        progress_percent  DECIMAL(5,2)  NOT NULL DEFAULT 0,
        current_stage     NVARCHAR(32)  NULL,
        data_version      NVARCHAR(64)  NULL,
        retry_count       INT           NOT NULL DEFAULT 0,
        rows_read         BIGINT        NOT NULL DEFAULT 0,
        rows_written      BIGINT        NOT NULL DEFAULT 0,
        started_time      DATETIME2(0)  NULL,
        finished_time     DATETIME2(0)  NULL,
        error_message     NVARCHAR(1000) NULL,
        create_time       DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time       DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_precompute_window_job_key'
      AND object_id = OBJECT_ID(N'dbo.diag_precompute_window')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_precompute_window_job_key
        ON dbo.diag_precompute_window(tenant_id, job_id, window_key);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_precompute_job_req_hash_status'
      AND object_id = OBJECT_ID(N'dbo.diag_precompute_job')
)
BEGIN
    CREATE INDEX ix_diag_precompute_job_req_hash_status
        ON dbo.diag_precompute_job(tenant_id, request_hash, status_code, submitted_time DESC);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_precompute_window_status'
      AND object_id = OBJECT_ID(N'dbo.diag_precompute_window')
)
BEGIN
    CREATE INDEX ix_diag_precompute_window_status
        ON dbo.diag_precompute_window(tenant_id, status_code, update_time DESC);
END
GO

-- =========================
-- 3. 预计算事件日志表
-- =========================
IF OBJECT_ID(N'dbo.diag_precompute_event', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_precompute_event
    (
        event_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id       NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        job_id          BIGINT        NOT NULL,
        window_id       BIGINT        NULL,
        event_time      DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        event_level     NVARCHAR(16)  NOT NULL,
        event_stage     NVARCHAR(32)  NULL,
        event_message   NVARCHAR(1000) NOT NULL,
        payload_json    NVARCHAR(MAX) NULL,
        create_time     DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_precompute_event_job_time'
      AND object_id = OBJECT_ID(N'dbo.diag_precompute_event')
)
BEGIN
    CREATE INDEX ix_diag_precompute_event_job_time
        ON dbo.diag_precompute_event(tenant_id, job_id, event_time DESC);
END
GO

-- =========================
-- 3.1 结果版本发布表
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
-- 4. 诊断概览快照表
-- =========================
IF OBJECT_ID(N'dbo.diag_result_overview', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_overview
    (
        id                    BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id             NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash            NVARCHAR(64)  NOT NULL,
        class_level           INT           NULL,
        class_no              NVARCHAR(32)  NULL,
        class_name            NVARCHAR(100) NULL,
        retail_type_id        NVARCHAR(32)  NULL,
        dept_id               BIGINT        NULL,
        business_circle_id    NVARCHAR(32)  NULL,
        dept_group_id         NVARCHAR(32)  NULL,
        store_no              NVARCHAR(32)  NULL,
        period_start          DATE          NOT NULL,
        period_end            DATE          NOT NULL,
        compare_start         DATE          NULL,
        compare_end           DATE          NULL,
        metric_total_sales    DECIMAL(20,4) NOT NULL DEFAULT 0,
        metric_total_profit   DECIMAL(20,4) NOT NULL DEFAULT 0,
        metric_profit_margin  DECIMAL(12,6) NOT NULL DEFAULT 0,
        metric_total_sku      INT           NOT NULL DEFAULT 0,
        metric_active_sku     INT           NOT NULL DEFAULT 0,
        metric_sales_rate     DECIMAL(12,6) NOT NULL DEFAULT 0,
        data_version          NVARCHAR(64)  NOT NULL,
        snapshot_time         DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time           DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time           DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_overview_hash_ver'
      AND object_id = OBJECT_ID(N'dbo.diag_result_overview')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_overview_hash_ver
        ON dbo.diag_result_overview(tenant_id, query_hash, data_version);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_overview_period'
      AND object_id = OBJECT_ID(N'dbo.diag_result_overview')
)
BEGIN
    CREATE INDEX ix_diag_result_overview_period
        ON dbo.diag_result_overview(tenant_id, period_start, period_end, snapshot_time DESC);
END
GO

-- =========================
-- 5. 诊断趋势快照表
-- =========================
IF OBJECT_ID(N'dbo.diag_result_trends', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_trends
    (
        id                 BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id          NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash         NVARCHAR(64)  NOT NULL,
        metric_code        NVARCHAR(64)  NOT NULL,
        point_date         DATE          NOT NULL,
        current_value      DECIMAL(20,6) NOT NULL DEFAULT 0,
        compare_value      DECIMAL(20,6) NULL,
        growth_rate        DECIMAL(12,6) NULL,
        period_label       NVARCHAR(32)  NULL,
        data_version       NVARCHAR(64)  NOT NULL,
        snapshot_time      DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time        DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time        DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_trends_hash_metric_date_ver'
      AND object_id = OBJECT_ID(N'dbo.diag_result_trends')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_trends_hash_metric_date_ver
        ON dbo.diag_result_trends(tenant_id, query_hash, metric_code, point_date, data_version);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_trends_query'
      AND object_id = OBJECT_ID(N'dbo.diag_result_trends')
)
BEGIN
    CREATE INDEX ix_diag_result_trends_query
        ON dbo.diag_result_trends(tenant_id, query_hash, metric_code, point_date);
END
GO

-- =========================
-- 6. 角色分布快照表
-- =========================
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

-- 完成提示
PRINT N'[OK] diag 第1步基础表脚本执行完成';

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

-- =========================
-- 7. 洞察快照表
-- =========================
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

-- =========================
-- 8. 品牌分析概览结果表
-- =========================
IF OBJECT_ID(N'dbo.diag_result_brand_overview', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_brand_overview
    (
        id            BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id     NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash    NVARCHAR(64)  NOT NULL,
        data_version  NVARCHAR(64)  NOT NULL,
        total_num     INT           NOT NULL DEFAULT 0,
        new_num       INT           NOT NULL DEFAULT 0,
        own_num       INT           NOT NULL DEFAULT 0,
        snapshot_time DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time   DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time   DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_brand_overview_qv'
      AND object_id = OBJECT_ID(N'dbo.diag_result_brand_overview')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_brand_overview_qv
        ON dbo.diag_result_brand_overview(tenant_id, query_hash, data_version);
END
GO

-- =========================
-- 9. 品牌分析主结果表
-- =========================
IF OBJECT_ID(N'dbo.diag_result_brand_metric', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_brand_metric
    (
        id                      BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id               NVARCHAR(20)   NOT NULL DEFAULT N'000000',
        query_hash              NVARCHAR(64)   NOT NULL,
        data_version            NVARCHAR(64)   NOT NULL,
        brand_no                NVARCHAR(128)  NOT NULL,
        product_brand           NVARCHAR(255)  NULL,
        brand_type              NVARCHAR(8)    NULL,
        brand_type_name         NVARCHAR(32)   NULL,
        new_brand_type          NVARCHAR(8)    NOT NULL DEFAULT N'0',
        new_brand_type_name     NVARCHAR(32)   NULL,
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
    WHERE name = N'ux_diag_result_brand_metric_qvb'
      AND object_id = OBJECT_ID(N'dbo.diag_result_brand_metric')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_brand_metric_qvb
        ON dbo.diag_result_brand_metric(tenant_id, query_hash, data_version, brand_no);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_brand_metric_qv_sales'
      AND object_id = OBJECT_ID(N'dbo.diag_result_brand_metric')
)
BEGIN
    CREATE INDEX ix_diag_result_brand_metric_qv_sales
        ON dbo.diag_result_brand_metric(tenant_id, query_hash, data_version, sales DESC, brand_no ASC);
END
GO

-- =========================
-- 10. 品牌分析 JSON 结果表
-- =========================
IF OBJECT_ID(N'dbo.diag_result_brand_json', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_brand_json
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
    WHERE name = N'ux_diag_result_brand_json_qvp'
      AND object_id = OBJECT_ID(N'dbo.diag_result_brand_json')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_brand_json_qvp
        ON dbo.diag_result_brand_json(tenant_id, query_hash, data_version, payload_code);
END
GO

-- =========================
-- 11. 规格分析概览结果表
-- =========================
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

-- =========================
-- 12. 规格分析主结果表
-- =========================
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

-- =========================
-- 13. 规格分析 JSON 结果表
-- =========================
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

-- =========================
-- 14. 标签分析主结果表
-- =========================
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

-- =========================
-- 15. 标签分析 JSON 结果表
-- =========================
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
