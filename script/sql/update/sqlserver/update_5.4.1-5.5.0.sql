-- ----------------------------
-- 流程spel表达式定义表
-- ----------------------------
CREATE TABLE flow_spel (
    id BIGINT NOT NULL,
    component_name VARCHAR(255),
    method_name VARCHAR(255),
    method_params VARCHAR(255),
    view_spel VARCHAR(255),
    remark VARCHAR(255),
    status CHAR(1) DEFAULT ('0'),
    del_flag CHAR(1) DEFAULT ('0'),
    create_dept BIGINT,
    create_by BIGINT,
    create_time DATETIME,
    update_by BIGINT,
    update_time DATETIME,
    CONSTRAINT PK_flow_spel PRIMARY KEY (id)
);
GO

EXEC sp_addextendedproperty
    'MS_Description', N'流程spel表达式定义表',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'主键id',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'组件名称',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'component_name'
GO

-- method_name 字段注释
    'MS_Description', N'方法名',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'method_name'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'参数',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'method_params'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'预览spel表达式',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'view_spel'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'备注',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'remark'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'状态（0正常 1停用）',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'status'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'删除标志',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'del_flag'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'创建部门',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'create_dept'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'创建者',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'create_by'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'创建时间',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'更新者',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'update_by'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'更新时间',
    'SCHEMA', N'dbo',
    'TABLE', N'flow_spel',
    'COLUMN', N'update_time'
GO

INSERT flow_spel VALUES (1, N'spelRuleComponent', N'selectDeptLeaderById', N'initiatorDeptId', N'#{@spelRuleComponent.selectDeptLeaderById(#initiatorDeptId)}', N'根据部门id获取部门负责人', N'0', N'0', 103, 1, GETDATE(), 1, GETDATE());
GO
INSERT flow_spel VALUES (2, NULL, NULL, N'initiator', N'${initiator}', N'流程发起人', N'0', N'0', 103, 1, GETDATE(), 1, GETDATE());
GO

INSERT sys_menu VALUES (N'11801', N'流程表达式', N'11616', 2, N'spel', N'workflow/spel/index', N'', 1, 0, N'C', N'0', N'0', N'workflow:spel:list', N'input', 103, 1, GETDATE(), 1, GETDATE(), N'流程达式定义菜单');
GO
INSERT sys_menu VALUES (N'11802', N'流程spel达式定义查询', N'11801', 1, N'#', N'', NULL, 1, 0, N'F', N'0', N'0', N'workflow:spel:query', N'#', 103, 1, GETDATE(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (N'11803', N'流程spel达式定义新增', N'11801', 2, N'#', N'', NULL, 1, 0, N'F', N'0', N'0', N'workflow:spel:add', N'#', 103, 1, GETDATE(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (N'11804', N'流程spel达式定义修改', N'11801', 3, N'#', N'', NULL, 1, 0, N'F', N'0', N'0', N'workflow:spel:edit', N'#', 103, 1, GETDATE(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (N'11805', N'流程spel达式定义删除', N'11801', 4, N'#', N'', NULL, 1, 0, N'F', N'0', N'0', N'workflow:spel:remove', N'#', 103, 1, GETDATE(), NULL, NULL, N'');
GO
INSERT sys_menu VALUES (N'11806', N'流程spel达式定义导出', N'11801', 5, N'#', N'', NULL, 1, 0, N'F', N'0', N'0', N'workflow:spel:export', N'#', 103, 1, GETDATE(), NULL, NULL, N'');
GO


-- ----------------------------
-- SnailJob 升级 1.6.0
-- ----------------------------

-- drop 号段模式序号ID分配表 用户自行决定是否drop
-- DROP TABLE sj_sequence_alloc;

-- create 任务执行器信息表
CREATE TABLE sj_job_executor
(
    id            bigint        NOT NULL PRIMARY KEY IDENTITY,
    namespace_id  nvarchar(64)  NOT NULL DEFAULT '764d604ec6fc45f68cd92514c40e9e1a',
    group_name    nvarchar(64)  NOT NULL,
    executor_info nvarchar(256) NOT NULL,
    executor_type nvarchar(3)   NOT NULL,
    create_dt     datetime2     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_dt     datetime2     NOT NULL DEFAULT CURRENT_TIMESTAMP
)
    GO

CREATE INDEX idx_sj_job_executor_01 ON sj_job_executor (namespace_id, group_name)
    GO
CREATE INDEX idx_sj_job_executor_02 ON sj_job_executor (create_dt)
    GO

EXEC sp_addextendedproperty
     'MS_Description', N'主键',
     'SCHEMA', N'dbo',
     'TABLE', N'sj_job_executor',
     'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'命名空间id',
     'SCHEMA', N'dbo',
     'TABLE', N'sj_job_executor',
     'COLUMN', N'namespace_id'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'组名称',
     'SCHEMA', N'dbo',
     'TABLE', N'sj_job_executor',
     'COLUMN', N'group_name'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'任务执行器名称',
     'SCHEMA', N'dbo',
     'TABLE', N'sj_job_executor',
     'COLUMN', N'executor_info'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'1:java 2:python 3:go',
     'SCHEMA', N'dbo',
     'TABLE', N'sj_job_executor',
     'COLUMN', N'executor_type'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'创建时间',
     'SCHEMA', N'dbo',
     'TABLE', N'sj_job_executor',
     'COLUMN', N'create_dt'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'修改时间',
     'SCHEMA', N'dbo',
     'TABLE', N'sj_job_executor',
     'COLUMN', N'update_dt'
GO

EXEC sp_addextendedproperty
     'MS_Description', N'任务执行器信息',
     'SCHEMA', N'dbo',
     'TABLE', N'sj_job_executor'
GO

-- 新增字段
ALTER TABLE sj_retry_dead_letter ADD serializer_name nvarchar(32) NOT NULL DEFAULT 'jackson'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'执行方法参数序列化器名称',
    'SCHEMA', N'dbo',
    'TABLE', N'sj_retry_dead_letter',
    'COLUMN', N'serializer_name'
GO

ALTER TABLE sj_retry ADD serializer_name nvarchar(32) NOT NULL DEFAULT 'jackson'
GO

EXEC sp_addextendedproperty
    'MS_Description', N'执行方法参数序列化器名称',
    'SCHEMA', N'dbo',
    'TABLE', N'sj_retry',
    'COLUMN', N'serializer_name'
GO

ALTER TABLE sj_retry_scene_config ADD owner_id bigint DEFAULT NULL
GO

EXEC sp_addextendedproperty
    'MS_Description', N'负责人id',
    'SCHEMA', N'dbo',
    'TABLE', N'sj_retry_scene_config',
    'COLUMN', N'owner_id'
GO

ALTER TABLE sj_retry_scene_config ADD labels nvarchar(512) DEFAULT '' NULL
GO

EXEC sp_addextendedproperty
    'MS_Description', N'标签',
    'SCHEMA', N'dbo',
    'TABLE', N'sj_retry_scene_config',
    'COLUMN', N'labels'
GO

ALTER TABLE sj_server_node ADD labels nvarchar(512) DEFAULT '' NULL
GO

EXEC sp_addextendedproperty
    'MS_Description', N'标签',
    'SCHEMA', N'dbo',
    'TABLE', N'sj_server_node',
    'COLUMN', N'labels'
GO

ALTER TABLE sj_job ADD labels nvarchar(512) DEFAULT '' NULL
GO

EXEC sp_addextendedproperty
    'MS_Description', N'标签',
    'SCHEMA', N'dbo',
    'TABLE', N'sj_job',
    'COLUMN', N'labels'
GO

ALTER TABLE sj_workflow ADD owner_id bigint DEFAULT NULL
    GO

EXEC sp_addextendedproperty
    'MS_Description', N'负责人id',
    'SCHEMA', N'dbo',
    'TABLE', N'sj_workflow',
    'COLUMN', N'owner_id'
GO
