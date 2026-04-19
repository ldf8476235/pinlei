-- diagnosis vendor result schema upgrade (SQL Server)

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.diag_result_vendor_metric', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_vendor_metric
    (
        id                         BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id                  NVARCHAR(32)   NOT NULL,
        query_hash                 NVARCHAR(128)  NOT NULL,
        data_version               NVARCHAR(64)   NOT NULL,
        product_vendor_no          NVARCHAR(80)   NOT NULL,
        product_vendor_name        NVARCHAR(200)  NOT NULL,
        product_vendor_no_name     NVARCHAR(300)  NOT NULL,
        product_vendor_status_no   NVARCHAR(40)   NULL,
        product_vendor_status_name NVARCHAR(80)   NULL,
        sku_count                  INT            NOT NULL,
        new_sku_count              INT            NOT NULL,
        sales                      DECIMAL(20,4)  NOT NULL,
        sales_per                  DECIMAL(20,4)  NOT NULL,
        gross                      DECIMAL(20,4)  NOT NULL,
        gross_rate                 DECIMAL(20,4)  NOT NULL,
        oi_amount                  DECIMAL(20,4)  NOT NULL,
        all_gross                  DECIMAL(20,4)  NOT NULL,
        all_gross_rate             DECIMAL(20,4)  NOT NULL,
        net_order_amount           DECIMAL(20,4)  NOT NULL,
        diff_order_amount          DECIMAL(20,4)  NOT NULL,
        diff_order_amount_rate     DECIMAL(20,4)  NOT NULL,
        unit_output                DECIMAL(20,4)  NOT NULL,
        snapshot_time              DATETIME2(0)   NOT NULL,
        create_time                DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time                DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_vendor_metric_qvv'
      AND object_id = OBJECT_ID(N'dbo.diag_result_vendor_metric')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_vendor_metric_qvv
        ON dbo.diag_result_vendor_metric(tenant_id, query_hash, data_version, product_vendor_no);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_vendor_metric_qv_sales'
      AND object_id = OBJECT_ID(N'dbo.diag_result_vendor_metric')
)
BEGIN
    CREATE INDEX ix_diag_result_vendor_metric_qv_sales
        ON dbo.diag_result_vendor_metric(tenant_id, query_hash, data_version, sales DESC, product_vendor_no ASC);
END
GO

IF OBJECT_ID(N'dbo.diag_result_vendor_json', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_vendor_json
    (
        id            BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id     NVARCHAR(32)   NOT NULL,
        query_hash    NVARCHAR(128)  NOT NULL,
        data_version  NVARCHAR(64)   NOT NULL,
        payload_code  NVARCHAR(64)   NOT NULL,
        payload_name  NVARCHAR(128)  NULL,
        payload_json  NVARCHAR(MAX)  NOT NULL,
        snapshot_time DATETIME2(0)   NOT NULL,
        create_time   DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time   DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_vendor_json_qvp'
      AND object_id = OBJECT_ID(N'dbo.diag_result_vendor_json')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_vendor_json_qvp
        ON dbo.diag_result_vendor_json(tenant_id, query_hash, data_version, payload_code);
END
GO

PRINT N'[OK] diagnosis vendor schema upgrade done';
GO
