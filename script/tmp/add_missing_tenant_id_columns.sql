/*
用途:
1) 给当前数据库中所有缺少 tenant_id 的用户表补充 tenant_id 字段
2) tenant_id 统一为 NVARCHAR(20) NOT NULL，默认值 '000000'
3) 仅补缺失字段，不改已存在 tenant_id 的表

执行前:
- 请先确认当前数据库是 diagnosis
*/

SET NOCOUNT ON;

DECLARE @schema_name SYSNAME;
DECLARE @table_name SYSNAME;
DECLARE @object_id INT;
DECLARE @default_name SYSNAME;
DECLARE @sql NVARCHAR(MAX);

DECLARE table_cursor CURSOR FAST_FORWARD FOR
SELECT
    s.name AS schema_name,
    t.name AS table_name,
    t.object_id
FROM sys.tables t
INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
WHERE t.is_ms_shipped = 0
  AND NOT EXISTS (
      SELECT 1
      FROM sys.columns c
      WHERE c.object_id = t.object_id
        AND c.name = 'tenant_id'
  )
ORDER BY s.name, t.name;

OPEN table_cursor;

FETCH NEXT FROM table_cursor INTO @schema_name, @table_name, @object_id;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @default_name = CONCAT('DF_', @table_name, '_tenant_id_', @object_id);

    SET @sql = N'ALTER TABLE '
        + QUOTENAME(@schema_name) + N'.' + QUOTENAME(@table_name)
        + N' ADD [tenant_id] NVARCHAR(20) NOT NULL'
        + N' CONSTRAINT ' + QUOTENAME(@default_name)
        + N' DEFAULT (N''000000'') WITH VALUES;';

    PRINT N'>> ADD tenant_id: ' + QUOTENAME(@schema_name) + N'.' + QUOTENAME(@table_name);
    EXEC sp_executesql @sql;

    FETCH NEXT FROM table_cursor INTO @schema_name, @table_name, @object_id;
END

CLOSE table_cursor;
DEALLOCATE table_cursor;

PRINT N'------ CHECK RESULT ------';
SELECT
    s.name AS schema_name,
    t.name AS table_name
FROM sys.tables t
INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
WHERE t.is_ms_shipped = 0
  AND NOT EXISTS (
      SELECT 1
      FROM sys.columns c
      WHERE c.object_id = t.object_id
        AND c.name = 'tenant_id'
  )
ORDER BY s.name, t.name;

