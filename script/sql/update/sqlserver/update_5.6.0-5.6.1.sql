-- 新增字典类型: class_role_type
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
        remark = N'品类角色分类(0没有分类；222 第一种分类；223 等等)'
    WHERE tenant_id = N'000000'
      AND (dict_id = 222 OR dict_type = N'class_role_type');
END
ELSE
BEGIN
    INSERT INTO sys_dict_type
    (
        dict_id,
        tenant_id,
        dict_name,
        dict_type,
        create_dept,
        create_by,
        create_time,
        update_by,
        update_time,
        remark
    )
    VALUES
    (
        222,
        N'000000',
        N'品类角色分类',
        N'class_role_type',
        103,
        1,
        GETDATE(),
        NULL,
        NULL,
        N'品类角色分类(0没有分类；222 第一种分类；223 等等)'
    );
END
GO

-- 字典项: 明星品类
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
        dict_code,
        tenant_id,
        dict_sort,
        dict_label,
        dict_value,
        dict_type,
        css_class,
        list_class,
        is_default,
        create_dept,
        create_by,
        create_time,
        update_by,
        update_time,
        remark
    )
    VALUES
    (
        22201,
        N'000000',
        1,
        N'明星品类',
        N'1',
        N'class_role_type',
        N'',
        N'',
        N'N',
        103,
        1,
        GETDATE(),
        NULL,
        NULL,
        N'关联字典ID:222'
    );
END
GO

-- 字典项: 幼童品类
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
        dict_code,
        tenant_id,
        dict_sort,
        dict_label,
        dict_value,
        dict_type,
        css_class,
        list_class,
        is_default,
        create_dept,
        create_by,
        create_time,
        update_by,
        update_time,
        remark
    )
    VALUES
    (
        22202,
        N'000000',
        2,
        N'幼童品类',
        N'2',
        N'class_role_type',
        N'',
        N'',
        N'N',
        103,
        1,
        GETDATE(),
        NULL,
        NULL,
        N'关联字典ID:222'
    );
END
GO

-- 字典项: 结构品类
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
        dict_code,
        tenant_id,
        dict_sort,
        dict_label,
        dict_value,
        dict_type,
        css_class,
        list_class,
        is_default,
        create_dept,
        create_by,
        create_time,
        update_by,
        update_time,
        remark
    )
    VALUES
    (
        22203,
        N'000000',
        3,
        N'结构品类',
        N'3',
        N'class_role_type',
        N'',
        N'',
        N'N',
        103,
        1,
        GETDATE(),
        NULL,
        NULL,
        N'关联字典ID:222'
    );
END
GO

-- 字典项: 金牛品类
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
        dict_code,
        tenant_id,
        dict_sort,
        dict_label,
        dict_value,
        dict_type,
        css_class,
        list_class,
        is_default,
        create_dept,
        create_by,
        create_time,
        update_by,
        update_time,
        remark
    )
    VALUES
    (
        22204,
        N'000000',
        4,
        N'金牛品类',
        N'4',
        N'class_role_type',
        N'',
        N'',
        N'N',
        103,
        1,
        GETDATE(),
        NULL,
        NULL,
        N'关联字典ID:222'
    );
END
GO

