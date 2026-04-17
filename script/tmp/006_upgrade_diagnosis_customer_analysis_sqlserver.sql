-- diagnosis 瀹㈡埛鍒嗘瀽棰勮绠楃粨鏋滃眰鍗囩骇鑴氭湰 (SQL Server)
-- 鍖呭惈:
-- 1) diag_result_customer_contribution 缁撴灉琛?-- 2) 骞撮緞娈靛瓧鍏?diag_customer_age_bucket

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.diag_result_customer_contribution', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.diag_result_customer_contribution
    (
        id                       BIGINT IDENTITY(1,1) PRIMARY KEY,
        tenant_id                NVARCHAR(20)  NOT NULL DEFAULT N'000000',
        query_hash               NVARCHAR(64)  NOT NULL,
        data_version             NVARCHAR(64)  NOT NULL,
        gender                   TINYINT       NOT NULL,
        age_bucket_code          NVARCHAR(32)  NOT NULL,
        age_bucket_name          NVARCHAR(64)  NOT NULL,
        age_start                INT           NULL,
        age_end                  INT           NULL,
        age_order                INT           NOT NULL DEFAULT 0,
        retail_type_id           NVARCHAR(32)  NULL,
        dept_id                  BIGINT        NULL,
        business_circle_id       NVARCHAR(32)  NULL,
        dept_group_id            NVARCHAR(32)  NULL,
        store_no                 NVARCHAR(32)  NULL,
        period_start             DATE          NOT NULL,
        period_end               DATE          NOT NULL,
        compare_start            DATE          NULL,
        compare_end              DATE          NULL,
        current_sales            DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_customer_count   DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_customer_price   DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_unit_price       DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_count_ave        DECIMAL(20,2) NOT NULL DEFAULT 0,
        current_sale_quantity    DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_sales            DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_customer_count   DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_customer_price   DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_unit_price       DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_count_ave        DECIMAL(20,2) NOT NULL DEFAULT 0,
        compare_sale_quantity    DECIMAL(20,2) NOT NULL DEFAULT 0,
        sales_growth             DECIMAL(12,2) NOT NULL DEFAULT 0,
        customer_growth          DECIMAL(12,2) NOT NULL DEFAULT 0,
        customer_price_growth    DECIMAL(12,2) NOT NULL DEFAULT 0,
        unit_price_growth        DECIMAL(12,2) NOT NULL DEFAULT 0,
        count_ave_growth         DECIMAL(12,2) NOT NULL DEFAULT 0,
        sale_quantity_growth     DECIMAL(12,2) NOT NULL DEFAULT 0,
        snapshot_time            DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        create_time              DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME(),
        update_time              DATETIME2(0)  NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ux_diag_result_customer_contrib_query_ver_bucket_gender'
      AND object_id = OBJECT_ID(N'dbo.diag_result_customer_contribution')
)
BEGIN
    CREATE UNIQUE INDEX ux_diag_result_customer_contrib_query_ver_bucket_gender
        ON dbo.diag_result_customer_contribution
        (
            tenant_id,
            query_hash,
            data_version,
            age_bucket_code,
            gender
        );
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = N'ix_diag_result_customer_contrib_query_ver'
      AND object_id = OBJECT_ID(N'dbo.diag_result_customer_contribution')
)
BEGIN
    CREATE INDEX ix_diag_result_customer_contrib_query_ver
        ON dbo.diag_result_customer_contribution
        (
            tenant_id,
            query_hash,
            data_version,
            age_order,
            gender
        );
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys_dict_type
    WHERE tenant_id = N'000000'
      AND dict_type = N'diag_customer_age_bucket'
)
BEGIN
    DECLARE @nextDictTypeId BIGINT;
    SELECT @nextDictTypeId = ISNULL(MAX(dict_id), 0) + 1 FROM sys_dict_type;

    INSERT INTO sys_dict_type
    (
        dict_id,
        tenant_id,
        dict_name,
        dict_type,
        create_dept,
        create_by,
        create_time,
        remark
    )
    VALUES
    (
        @nextDictTypeId,
        N'000000',
        N'诊断客户年龄段',
        N'diag_customer_age_bucket',
        103,
        1,
        GETDATE(),
        N'客户分析年龄段配置，格式为 start-end；空值表示无界'
    );
END
GO

DECLARE @dictType NVARCHAR(100) = N'diag_customer_age_bucket';
DECLARE @nextDictCode BIGINT;
SELECT @nextDictCode = ISNULL(MAX(dict_code), 0) + 1 FROM sys_dict_data;

IF NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = N'000000' AND dict_type = @dictType AND dict_value = N'-20')
BEGIN
    INSERT INTO sys_dict_data
    (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
    VALUES
    (@nextDictCode, N'000000', 1, N'<=20', N'-20', @dictType, N'', N'default', N'N', 103, 1, GETDATE(), N'左闭右闭: age<=20');
    SET @nextDictCode = @nextDictCode + 1;
END

IF NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = N'000000' AND dict_type = @dictType AND dict_value = N'21-30')
BEGIN
    INSERT INTO sys_dict_data
    (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
    VALUES
    (@nextDictCode, N'000000', 2, N'21-30', N'21-30', @dictType, N'', N'default', N'N', 103, 1, GETDATE(), N'左闭右闭: 21<=age<=30');
    SET @nextDictCode = @nextDictCode + 1;
END

IF NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = N'000000' AND dict_type = @dictType AND dict_value = N'31-40')
BEGIN
    INSERT INTO sys_dict_data
    (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
    VALUES
    (@nextDictCode, N'000000', 3, N'31-40', N'31-40', @dictType, N'', N'default', N'N', 103, 1, GETDATE(), N'左闭右闭: 31<=age<=40');
    SET @nextDictCode = @nextDictCode + 1;
END

IF NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = N'000000' AND dict_type = @dictType AND dict_value = N'41-50')
BEGIN
    INSERT INTO sys_dict_data
    (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
    VALUES
    (@nextDictCode, N'000000', 4, N'41-50', N'41-50', @dictType, N'', N'default', N'N', 103, 1, GETDATE(), N'左闭右闭: 41<=age<=50');
    SET @nextDictCode = @nextDictCode + 1;
END

IF NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = N'000000' AND dict_type = @dictType AND dict_value = N'51-60')
BEGIN
    INSERT INTO sys_dict_data
    (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
    VALUES
    (@nextDictCode, N'000000', 5, N'51-60', N'51-60', @dictType, N'', N'default', N'N', 103, 1, GETDATE(), N'左闭右闭: 51<=age<=60');
    SET @nextDictCode = @nextDictCode + 1;
END

IF NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE tenant_id = N'000000' AND dict_type = @dictType AND dict_value = N'61-')
BEGIN
    INSERT INTO sys_dict_data
    (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
    VALUES
    (@nextDictCode, N'000000', 6, N'>=61', N'61-', @dictType, N'', N'default', N'N', 103, 1, GETDATE(), N'左闭右闭: age>=61');
    SET @nextDictCode = @nextDictCode + 1;
END
GO

PRINT N'[OK] diagnosis customer analysis schema upgrade done';
GO

