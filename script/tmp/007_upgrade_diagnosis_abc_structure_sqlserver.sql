-- diagnosis ABC 结构预计算结果层升级脚本 (SQL Server)

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

-- 1) ABC 参数配置表（全局）
IF OBJECT_ID(N'dbo.diag_abc_param_config', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_abc_param_config
    (
        id                 BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id          NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        abc_type           NVARCHAR(32)  NOT NULL,
        abc_type_name      NVARCHAR(64)  NOT NULL,
        sales_per          DECIMAL(10,4) NULL,
        gross_per          DECIMAL(10,4) NULL,
        sale_quantity_per  DECIMAL(10,4) NULL,
        a_rate             DECIMAL(10,2) NOT NULL DEFAULT 50,
        b_rate             DECIMAL(10,2) NOT NULL DEFAULT 40,
        c_rate             DECIMAL(10,2) NOT NULL DEFAULT 10,
        a_sku_rate         DECIMAL(10,2) NOT NULL DEFAULT 10,
        b_sku_rate         DECIMAL(10,2) NOT NULL DEFAULT 30,
        c_sku_rate         DECIMAL(10,2) NOT NULL DEFAULT 60,
        create_time        DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time        DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_abc_param_config_type'
      AND object_id = OBJECT_ID(N'dbo.diag_abc_param_config')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_abc_param_config_type
        ON dbo.diag_abc_param_config(tenant_id, abc_type);
END
GO

-- 2) ABC 参数快照（按 query_hash + data_version）
IF OBJECT_ID(N'dbo.diag_result_abc_params', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_abc_params
    (
        id                 BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id          NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash         NVARCHAR(64)  NOT NULL,
        data_version       NVARCHAR(64)  NOT NULL,
        abc_type           NVARCHAR(32)  NOT NULL,
        abc_type_name      NVARCHAR(64)  NOT NULL,
        sales_per          DECIMAL(10,4) NULL,
        gross_per          DECIMAL(10,4) NULL,
        sale_quantity_per  DECIMAL(10,4) NULL,
        a_rate             DECIMAL(10,2) NOT NULL DEFAULT 50,
        b_rate             DECIMAL(10,2) NOT NULL DEFAULT 40,
        c_rate             DECIMAL(10,2) NOT NULL DEFAULT 10,
        a_sku_rate         DECIMAL(10,2) NOT NULL DEFAULT 10,
        b_sku_rate         DECIMAL(10,2) NOT NULL DEFAULT 30,
        c_sku_rate         DECIMAL(10,2) NOT NULL DEFAULT 60,
        snapshot_time      DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time        DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time        DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

-- 3) ABC 分桶汇总
IF OBJECT_ID(N'dbo.diag_result_abc_bucket', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_abc_bucket
    (
        id                    BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id             NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash            NVARCHAR(64)  NOT NULL,
        data_version          NVARCHAR(64)  NOT NULL,
        abc_type              NVARCHAR(32)  NOT NULL,
        bucket                NVARCHAR(2)   NOT NULL,
        current_sales_per     DECIMAL(12,2) NOT NULL DEFAULT 0,
        current_sku_per       DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_sales_per     DECIMAL(12,2) NOT NULL DEFAULT 0,
        compare_sku_per       DECIMAL(12,2) NOT NULL DEFAULT 0,
        set_sales_per         DECIMAL(12,2) NOT NULL DEFAULT 0,
        set_sku_per           DECIMAL(12,2) NOT NULL DEFAULT 0,
        current_sku           INT           NOT NULL DEFAULT 0,
        compare_sku           INT           NOT NULL DEFAULT 0,
        change_sku            INT           NOT NULL DEFAULT 0,
        current_sales         DECIMAL(20,2) NOT NULL DEFAULT 0,
        stock_quantity        DECIMAL(20,2) NOT NULL DEFAULT 0,
        stock_quantity_per    DECIMAL(12,2) NOT NULL DEFAULT 0,
        snapshot_time         DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time           DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time           DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

-- 4) ABC 迁移矩阵
IF OBJECT_ID(N'dbo.diag_result_abc_matrix', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_abc_matrix
    (
        id             BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id      NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash     NVARCHAR(64)  NOT NULL,
        data_version   NVARCHAR(64)  NOT NULL,
        abc_type       NVARCHAR(32)  NOT NULL,
        aa_num         INT           NOT NULL DEFAULT 0,
        ab_num         INT           NOT NULL DEFAULT 0,
        ac_num         INT           NOT NULL DEFAULT 0,
        an_num         INT           NOT NULL DEFAULT 0,
        at_num         INT           NOT NULL DEFAULT 0,
        ba_num         INT           NOT NULL DEFAULT 0,
        bb_num         INT           NOT NULL DEFAULT 0,
        bc_num         INT           NOT NULL DEFAULT 0,
        bn_num         INT           NOT NULL DEFAULT 0,
        bt_num         INT           NOT NULL DEFAULT 0,
        ca_num         INT           NOT NULL DEFAULT 0,
        cb_num         INT           NOT NULL DEFAULT 0,
        cc_num         INT           NOT NULL DEFAULT 0,
        cn_num         INT           NOT NULL DEFAULT 0,
        ct_num         INT           NOT NULL DEFAULT 0,
        snapshot_time  DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time    DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time    DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

-- 5) ABC SKU 明细
IF OBJECT_ID(N'dbo.diag_result_abc_sku', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_abc_sku
    (
        id                    BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id             NVARCHAR(20)   NOT NULL DEFAULT N'000000',
        query_hash            NVARCHAR(64)   NOT NULL,
        data_version          NVARCHAR(64)   NOT NULL,
        abc_type              NVARCHAR(32)   NOT NULL,
        product_no            NVARCHAR(40)   NOT NULL,
        product_name          NVARCHAR(200)  NULL,
        product_status        NVARCHAR(40)   NULL,
        product_status_no     NVARCHAR(20)   NULL,
        store_num             INT            NOT NULL DEFAULT 0,
        current_abc           NVARCHAR(2)    NULL,
        compare_abc           NVARCHAR(2)    NULL,
        contribution          DECIMAL(20,6)  NOT NULL DEFAULT 0,
        contribution_per      DECIMAL(20,6)  NOT NULL DEFAULT 0,
        sale_quantity         DECIMAL(20,2)  NOT NULL DEFAULT 0,
        sale_quantity_psd     DECIMAL(20,4)  NOT NULL DEFAULT 0,
        sales                 DECIMAL(20,2)  NOT NULL DEFAULT 0,
        sales_per             DECIMAL(20,6)  NOT NULL DEFAULT 0,
        sales_psd             DECIMAL(20,4)  NOT NULL DEFAULT 0,
        gross                 DECIMAL(20,2)  NOT NULL DEFAULT 0,
        gross_per             DECIMAL(20,6)  NOT NULL DEFAULT 0,
        gross_psd             DECIMAL(20,4)  NOT NULL DEFAULT 0,
        gross_rate            DECIMAL(20,6)  NOT NULL DEFAULT 0,
        stock_quantity        DECIMAL(20,2)  NOT NULL DEFAULT 0,
        turnover_rate         DECIMAL(20,4)  NOT NULL DEFAULT 0,
        turnover_days         DECIMAL(20,4)  NOT NULL DEFAULT 0,
        stock_sales_rate      DECIMAL(20,4)  NOT NULL DEFAULT 0,
        contribution_rate     DECIMAL(20,6)  NOT NULL DEFAULT 0,
        gmroi                 DECIMAL(20,4)  NOT NULL DEFAULT 0,
        sales_rate            DECIMAL(20,4)  NOT NULL DEFAULT 0,
        activity              NVARCHAR(20)   NULL,
        first_sale_date       DATE           NULL,
        new_product           NVARCHAR(20)   NULL,
        key_product           NVARCHAR(20)   NULL,
        seasonable_flag       NVARCHAR(20)   NULL,
        seasonable_flag_name  NVARCHAR(40)   NULL,
        seasonable_start_date DATE           NULL,
        seasonable_end_date   DATE           NULL,
        class_no              NVARCHAR(40)   NULL,
        class_name            NVARCHAR(100)  NULL,
        class_level           INT            NULL,
        product_barcode       NVARCHAR(80)   NULL,
        brand_name            NVARCHAR(100)  NULL,
        spec                  NVARCHAR(100)  NULL,
        in_price              DECIMAL(20,2)  NOT NULL DEFAULT 0,
        sales_price           DECIMAL(20,2)  NOT NULL DEFAULT 0,
        product_vendor_no     NVARCHAR(80)   NULL,
        product_vendor_name   NVARCHAR(200)  NULL,
        product_vendor_no_name NVARCHAR(300) NULL,
        promotion_flag        NVARCHAR(20)   NULL,
        snapshot_time         DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time           DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time           DATETIME2(0)   NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_abc_params_query_ver_type'
      AND object_id = OBJECT_ID(N'dbo.diag_result_abc_params')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_abc_params_query_ver_type
        ON dbo.diag_result_abc_params(tenant_id, query_hash, data_version, abc_type);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_abc_bucket_query_ver_type_bucket'
      AND object_id = OBJECT_ID(N'dbo.diag_result_abc_bucket')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_abc_bucket_query_ver_type_bucket
        ON dbo.diag_result_abc_bucket(tenant_id, query_hash, data_version, abc_type, bucket);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_abc_matrix_query_ver_type'
      AND object_id = OBJECT_ID(N'dbo.diag_result_abc_matrix')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_abc_matrix_query_ver_type
        ON dbo.diag_result_abc_matrix(tenant_id, query_hash, data_version, abc_type);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_abc_sku_query_ver_type_product'
      AND object_id = OBJECT_ID(N'dbo.diag_result_abc_sku')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_abc_sku_query_ver_type_product
        ON dbo.diag_result_abc_sku(tenant_id, query_hash, data_version, abc_type, product_no);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_abc_sku_query_ver_type'
      AND object_id = OBJECT_ID(N'dbo.diag_result_abc_sku')
)
BEGIN
    CREATE INDEX ix_diag_result_abc_sku_query_ver_type
        ON dbo.diag_result_abc_sku(tenant_id, query_hash, data_version, abc_type, current_abc, compare_abc, product_status_no);
END
GO

-- 默认参数种子
IF NOT EXISTS (SELECT 1 FROM dbo.diag_abc_param_config WHERE tenant_id = N'000000' AND abc_type = N'sales')
BEGIN
    INSERT INTO dbo.diag_abc_param_config
    (tenant_id, abc_type, abc_type_name, sales_per, gross_per, sale_quantity_per, a_rate, b_rate, c_rate, a_sku_rate, b_sku_rate, c_sku_rate)
    VALUES
    (N'000000', N'sales', N'销售额ABC', NULL, NULL, NULL, 50, 40, 10, 10, 30, 60);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.diag_abc_param_config WHERE tenant_id = N'000000' AND abc_type = N'gross')
BEGIN
    INSERT INTO dbo.diag_abc_param_config
    (tenant_id, abc_type, abc_type_name, sales_per, gross_per, sale_quantity_per, a_rate, b_rate, c_rate, a_sku_rate, b_sku_rate, c_sku_rate)
    VALUES
    (N'000000', N'gross', N'毛利额ABC', NULL, NULL, NULL, 50, 40, 10, 10, 30, 60);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.diag_abc_param_config WHERE tenant_id = N'000000' AND abc_type = N'contribution')
BEGIN
    INSERT INTO dbo.diag_abc_param_config
    (tenant_id, abc_type, abc_type_name, sales_per, gross_per, sale_quantity_per, a_rate, b_rate, c_rate, a_sku_rate, b_sku_rate, c_sku_rate)
    VALUES
    (N'000000', N'contribution', N'综合业绩ABC', 0.40, 0.30, 0.30, 50, 40, 10, 10, 30, 60);
END
GO

PRINT N'[OK] diagnosis abc structure schema upgrade done';
GO
