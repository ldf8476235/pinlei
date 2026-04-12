-- 品类树筛选项字典补充脚本（SQL Server）
-- 目标：
-- 1) sys_dict_type 新增/更新:
--    - class_role_type (dict_id=222, 描述=品类角色分类)
--    - class_sales_status_no (dict_id=251, 描述=在售sku所属的商品状态)
-- 2) sys_dict_data 新增/更新:
--    - 222: 明星品类(1), 幼童品类(2), 结构品类(3), 金牛品类(4)
--    - 251: 上架(0)

-- =========================
-- dict_type: class_role_type (222)
-- =========================
IF EXISTS (
    SELECT 1
    FROM sys_dict_type
    WHERE tenant_id = N'000000'
      AND (dict_id = 222 OR dict_type = N'class_role_type')
)
BEGIN
    UPDATE sys_dict_type
    SET dict_id = 222,
        dict_name = N'品类角色分类',
        dict_type = N'class_role_type',
        create_dept = ISNULL(create_dept, 103),
        update_by = 1,
        update_time = GETDATE(),
        remark = N'品类角色分类'
    WHERE tenant_id = N'000000'
      AND (dict_id = 222 OR dict_type = N'class_role_type');
END
ELSE
BEGIN
    INSERT INTO sys_dict_type
    (
        dict_id, tenant_id, dict_name, dict_type,
        create_dept, create_by, create_time, update_by, update_time, remark
    )
    VALUES
    (
        222, N'000000', N'品类角色分类', N'class_role_type',
        103, 1, GETDATE(), NULL, NULL, N'品类角色分类'
    );
END
GO

-- =========================
-- dict_type: class_sales_status_no (251)
-- =========================
IF EXISTS (
    SELECT 1
    FROM sys_dict_type
    WHERE tenant_id = N'000000'
      AND (dict_id = 251 OR dict_type = N'class_sales_status_no')
)
BEGIN
    UPDATE sys_dict_type
    SET dict_id = 251,
        dict_name = N'在售sku所属的商品状态',
        dict_type = N'class_sales_status_no',
        create_dept = ISNULL(create_dept, 103),
        update_by = 1,
        update_time = GETDATE(),
        remark = N'在售sku所属的商品状态'
    WHERE tenant_id = N'000000'
      AND (dict_id = 251 OR dict_type = N'class_sales_status_no');
END
ELSE
BEGIN
    INSERT INTO sys_dict_type
    (
        dict_id, tenant_id, dict_name, dict_type,
        create_dept, create_by, create_time, update_by, update_time, remark
    )
    VALUES
    (
        251, N'000000', N'在售sku所属的商品状态', N'class_sales_status_no',
        103, 1, GETDATE(), NULL, NULL, N'在售sku所属的商品状态'
    );
END
GO

-- =========================
-- dict_data: class_role_type(222)
-- =========================

-- 明星品类 value=1
IF EXISTS (
    SELECT 1
    FROM sys_dict_data
    WHERE tenant_id = N'000000'
      AND dict_type = N'class_role_type'
      AND dict_value = N'1'
)
BEGIN
    UPDATE sys_dict_data
    SET dict_sort = 1,
        dict_label = N'明星品类',
        dict_type = N'class_role_type',
        css_class = N'',
        list_class = N'',
        is_default = N'N',
        create_dept = ISNULL(create_dept, 103),
        update_by = 1,
        update_time = GETDATE(),
        remark = N'关联字典ID:222'
    WHERE tenant_id = N'000000'
      AND dict_type = N'class_role_type'
      AND dict_value = N'1';
END
ELSE
BEGIN
    INSERT INTO sys_dict_data
    (
        dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type,
        css_class, list_class, is_default, create_dept, create_by, create_time, update_by, update_time, remark
    )
    VALUES
    (
        22201, N'000000', 1, N'明星品类', N'1', N'class_role_type',
        N'', N'', N'N', 103, 1, GETDATE(), NULL, NULL, N'关联字典ID:222'
    );
END
GO

-- 幼童品类 value=2
IF EXISTS (
    SELECT 1
    FROM sys_dict_data
    WHERE tenant_id = N'000000'
      AND dict_type = N'class_role_type'
      AND dict_value = N'2'
)
BEGIN
    UPDATE sys_dict_data
    SET dict_sort = 2,
        dict_label = N'幼童品类',
        dict_type = N'class_role_type',
        css_class = N'',
        list_class = N'',
        is_default = N'N',
        create_dept = ISNULL(create_dept, 103),
        update_by = 1,
        update_time = GETDATE(),
        remark = N'关联字典ID:222'
    WHERE tenant_id = N'000000'
      AND dict_type = N'class_role_type'
      AND dict_value = N'2';
END
ELSE
BEGIN
    INSERT INTO sys_dict_data
    (
        dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type,
        css_class, list_class, is_default, create_dept, create_by, create_time, update_by, update_time, remark
    )
    VALUES
    (
        22202, N'000000', 2, N'幼童品类', N'2', N'class_role_type',
        N'', N'', N'N', 103, 1, GETDATE(), NULL, NULL, N'关联字典ID:222'
    );
END
GO

-- 结构品类 value=3
IF EXISTS (
    SELECT 1
    FROM sys_dict_data
    WHERE tenant_id = N'000000'
      AND dict_type = N'class_role_type'
      AND dict_value = N'3'
)
BEGIN
    UPDATE sys_dict_data
    SET dict_sort = 3,
        dict_label = N'结构品类',
        dict_type = N'class_role_type',
        css_class = N'',
        list_class = N'',
        is_default = N'N',
        create_dept = ISNULL(create_dept, 103),
        update_by = 1,
        update_time = GETDATE(),
        remark = N'关联字典ID:222'
    WHERE tenant_id = N'000000'
      AND dict_type = N'class_role_type'
      AND dict_value = N'3';
END
ELSE
BEGIN
    INSERT INTO sys_dict_data
    (
        dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type,
        css_class, list_class, is_default, create_dept, create_by, create_time, update_by, update_time, remark
    )
    VALUES
    (
        22203, N'000000', 3, N'结构品类', N'3', N'class_role_type',
        N'', N'', N'N', 103, 1, GETDATE(), NULL, NULL, N'关联字典ID:222'
    );
END
GO

-- 金牛品类 value=4
IF EXISTS (
    SELECT 1
    FROM sys_dict_data
    WHERE tenant_id = N'000000'
      AND dict_type = N'class_role_type'
      AND dict_value = N'4'
)
BEGIN
    UPDATE sys_dict_data
    SET dict_sort = 4,
        dict_label = N'金牛品类',
        dict_type = N'class_role_type',
        css_class = N'',
        list_class = N'',
        is_default = N'N',
        create_dept = ISNULL(create_dept, 103),
        update_by = 1,
        update_time = GETDATE(),
        remark = N'关联字典ID:222'
    WHERE tenant_id = N'000000'
      AND dict_type = N'class_role_type'
      AND dict_value = N'4';
END
ELSE
BEGIN
    INSERT INTO sys_dict_data
    (
        dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type,
        css_class, list_class, is_default, create_dept, create_by, create_time, update_by, update_time, remark
    )
    VALUES
    (
        22204, N'000000', 4, N'金牛品类', N'4', N'class_role_type',
        N'', N'', N'N', 103, 1, GETDATE(), NULL, NULL, N'关联字典ID:222'
    );
END
GO

-- =========================
-- dict_data: class_sales_status_no(251)
-- =========================
IF EXISTS (
    SELECT 1
    FROM sys_dict_data
    WHERE tenant_id = N'000000'
      AND dict_type = N'class_sales_status_no'
      AND dict_value = N'0'
)
BEGIN
    UPDATE sys_dict_data
    SET dict_sort = 0,
        dict_label = N'上架',
        dict_type = N'class_sales_status_no',
        css_class = N'',
        list_class = N'',
        is_default = N'N',
        create_dept = ISNULL(create_dept, 103),
        update_by = 1,
        update_time = GETDATE(),
        remark = N'关联字典ID:251'
    WHERE tenant_id = N'000000'
      AND dict_type = N'class_sales_status_no'
      AND dict_value = N'0';
END
ELSE
BEGIN
    INSERT INTO sys_dict_data
    (
        dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type,
        css_class, list_class, is_default, create_dept, create_by, create_time, update_by, update_time, remark
    )
    VALUES
    (
        25101, N'000000', 0, N'上架', N'0', N'class_sales_status_no',
        N'', N'', N'N', 103, 1, GETDATE(), NULL, NULL, N'关联字典ID:251'
    );
END
GO

