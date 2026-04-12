SET NOCOUNT ON;

SELECT DB_NAME() AS current_db;

SELECT
    t.name AS table_name,
    c.name AS column_name
FROM sys.tables t
LEFT JOIN sys.columns c
    ON c.object_id = t.object_id
   AND c.name = 'tenant_id'
WHERE t.name IN ('base_class', 'base_class_sku', 'base_dept_products', 'base_department')
ORDER BY t.name;

