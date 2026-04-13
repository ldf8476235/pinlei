-- Step3 迭代批次 2/5 增量脚本（SQL Server）
-- 目标：补充幂等与重试字段

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF COL_LENGTH('dbo.diag_precompute_job', 'request_hash') IS NULL
BEGIN
    ALTER TABLE dbo.diag_precompute_job
        ADD request_hash NVARCHAR(64) NULL;
END
GO

IF COL_LENGTH('dbo.diag_precompute_job', 'max_retry') IS NULL
BEGIN
    ALTER TABLE dbo.diag_precompute_job
        ADD max_retry INT NOT NULL CONSTRAINT DF_diag_precompute_job_max_retry DEFAULT (1);
END
GO

IF COL_LENGTH('dbo.diag_precompute_job', 'retry_count') IS NULL
BEGIN
    ALTER TABLE dbo.diag_precompute_job
        ADD retry_count INT NOT NULL CONSTRAINT DF_diag_precompute_job_retry_count DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.diag_precompute_window', 'retry_count') IS NULL
BEGIN
    ALTER TABLE dbo.diag_precompute_window
        ADD retry_count INT NOT NULL CONSTRAINT DF_diag_precompute_window_retry_count DEFAULT (0);
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

PRINT N'[OK] step3-iter2 增量脚本执行完成';

