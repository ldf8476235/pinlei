-- ----------------------------
-- 流程spel表达式定义表
-- ----------------------------
CREATE TABLE flow_spel (
    id NUMBER(20) NOT NULL,
    component_name VARCHAR2(255),
    method_name VARCHAR2(255),
    method_params VARCHAR2(255),
    view_spel VARCHAR2(255),
    remark VARCHAR2(255),
    status CHAR(1) DEFAULT '0',
    del_flag CHAR(1) DEFAULT '0',
    create_dept NUMBER(20),
    create_by NUMBER(20),
    create_time DATE,
    update_by NUMBER(20),
    update_time DATE
);

alter table flow_spel add constraint pk_flow_spel primary key (id);

COMMENT ON TABLE flow_spel IS '流程spel表达式定义表';
COMMENT ON COLUMN flow_spel.id IS '主键id';
COMMENT ON COLUMN flow_spel.component_name IS '组件名称';
COMMENT ON COLUMN flow_spel.method_name IS '方法名';
COMMENT ON COLUMN flow_spel.method_params IS '参数';
COMMENT ON COLUMN flow_spel.view_spel IS '预览spel表达式';
COMMENT ON COLUMN flow_spel.remark IS '备注';
COMMENT ON COLUMN flow_spel.status IS '状态（0正常 1停用）';
COMMENT ON COLUMN flow_spel.del_flag IS '删除标志';
COMMENT ON COLUMN flow_spel.create_dept IS '创建部门';
COMMENT ON COLUMN flow_spel.create_by IS '创建者';
COMMENT ON COLUMN flow_spel.create_time IS '创建时间';
COMMENT ON COLUMN flow_spel.update_by IS '更新者';
COMMENT ON COLUMN flow_spel.update_time IS '更新时间';

INSERT INTO flow_spel VALUES (1, 'spelRuleComponent', 'selectDeptLeaderById', 'initiatorDeptId', '#{@spelRuleComponent.selectDeptLeaderById(#initiatorDeptId)}', '根据部门id获取部门负责人', '0', '0', 103, 1, SYSDATE, 1, SYSDATE);
INSERT INTO flow_spel VALUES (2, NULL, NULL, 'initiator', '${initiator}', '流程发起人', '0', '0', 103, 1, SYSDATE, 1, SYSDATE);

INSERT INTO sys_menu VALUES ('11801', '流程表达式', '11616', 2, 'spel', 'workflow/spel/index', '', 1, 0, 'C', '0', '0', 'workflow:spel:list', 'input', 103, 1, SYSDATE, 1, SYSDATE, '流程达式定义菜单');
INSERT INTO sys_menu VALUES ('11802', '流程spel达式定义查询', '11801', 1, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:query', '#', 103, 1, SYSDATE, NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11803', '流程spel达式定义新增', '11801', 2, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:add', '#', 103, 1, SYSDATE, NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11804', '流程spel达式定义修改', '11801', 3, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:edit', '#', 103, 1, SYSDATE, NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11805', '流程spel达式定义删除', '11801', 4, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:remove', '#', 103, 1, SYSDATE, NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11806', '流程spel达式定义导出', '11801', 5, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:export', '#', 103, 1, SYSDATE, NULL, NULL, '');


-- ----------------------------
-- SnailJob 升级 1.6.0
-- ----------------------------

-- drop 号段模式序号ID分配表 用户自行决定是否drop
-- DROP TABLE sj_sequence_alloc purge;

-- create 任务执行器信息表
CREATE TABLE sj_job_executor
(
    id            number GENERATED ALWAYS AS IDENTITY,
    namespace_id  varchar2(64) DEFAULT '764d604ec6fc45f68cd92514c40e9e1a' NULL,
    group_name    varchar2(64)                                            NULL,
    executor_info varchar2(256)                                           NULL,
    executor_type varchar2(3)                                             NULL,
    create_dt     date         DEFAULT CURRENT_TIMESTAMP                  NOT NULL,
    update_dt     date         DEFAULT CURRENT_TIMESTAMP                  NOT NULL
);

ALTER TABLE sj_job_executor
    ADD CONSTRAINT pk_sj_job_executor PRIMARY KEY (id);

CREATE INDEX idx_sj_job_executor_01 ON sj_job_executor (namespace_id, group_name);
CREATE INDEX idx_sj_job_executor_02 ON sj_job_executor (create_dt);

COMMENT ON COLUMN sj_job_executor.id IS '主键';
COMMENT ON COLUMN sj_job_executor.namespace_id IS '命名空间id';
COMMENT ON COLUMN sj_job_executor.group_name IS '组名称';
COMMENT ON COLUMN sj_job_executor.executor_info IS '任务执行器名称';
COMMENT ON COLUMN sj_job_executor.executor_type IS '1:java 2:python 3:go';
COMMENT ON COLUMN sj_job_executor.create_dt IS '创建时间';
COMMENT ON COLUMN sj_job_executor.update_dt IS '修改时间';
COMMENT ON TABLE sj_job_executor IS '任务执行器信息';

-- 新增字段
ALTER TABLE sj_retry_dead_letter ADD (serializer_name varchar2(32) NOT NULL DEFAULT 'jackson') COMMENT '执行方法参数序列化器名称';
COMMENT ON COLUMN sj_retry_dead_letter.serializer_name IS '执行方法参数序列化器名称';
ALTER TABLE sj_retry ADD (serializer_name varchar2(32) NOT NULL DEFAULT 'jackson') COMMENT '执行方法参数序列化器名称';
COMMENT ON COLUMN sj_retry.serializer_name IS '执行方法参数序列化器名称';
ALTER TABLE sj_retry_scene_config ADD (owner_id number(20) NULL DEFAULT NULL) COMMENT '负责人id';
COMMENT ON COLUMN sj_retry_scene_config.owner_id IS '负责人id';
ALTER TABLE sj_retry_scene_config ADD (labels varchar2(512) NULL DEFAULT '') COMMENT '标签';
COMMENT ON COLUMN sj_retry_scene_config.labels IS '标签';
ALTER TABLE sj_server_node ADD (labels varchar2(512) NULL DEFAULT '') COMMENT '标签';
COMMENT ON COLUMN sj_server_node.labels IS '标签';
ALTER TABLE sj_job ADD (labels varchar2(512) NULL DEFAULT '') COMMENT '标签';
COMMENT ON COLUMN sj_job.labels IS '标签';
ALTER TABLE sj_workflow ADD (owner_id number(20) NULL DEFAULT NULL) COMMENT '负责人id';
COMMENT ON COLUMN sj_workflow.owner_id IS '负责人id';
