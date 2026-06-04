-- base_class_sku 增加独立建议 SKU 数字段 (SQL Server)

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF COL_LENGTH(N'dbo.base_class_sku', N'sys_suggest_sale_sku') IS NULL
BEGIN
    ALTER TABLE dbo.base_class_sku
        ADD sys_suggest_sale_sku VARCHAR(40) NULL;
END
GO

UPDATE dbo.base_class_sku
SET sys_suggest_sale_sku = class_sku
WHERE sys_suggest_sale_sku IS NULL
  AND class_sku IS NOT NULL;
GO
