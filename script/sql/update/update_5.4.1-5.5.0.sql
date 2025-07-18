-- ----------------------------
-- 流程spel表达式定义表
-- ----------------------------
CREATE TABLE flow_spel (
    id bigint(20) NOT NULL COMMENT '主键id',
    component_name varchar(255) DEFAULT NULL COMMENT '组件名称',
    method_name varchar(255) DEFAULT NULL COMMENT '方法名',
    method_params varchar(255) DEFAULT NULL COMMENT '参数',
    view_spel varchar(255) DEFAULT NULL COMMENT '预览spel表达式',
    remark varchar(255) DEFAULT NULL COMMENT '备注',
    status char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    del_flag char(1) DEFAULT '0' COMMENT '删除标志',
    create_dept bigint(20) DEFAULT NULL COMMENT '创建部门',
    create_by bigint(20) DEFAULT NULL COMMENT '创建者',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_by bigint(20) DEFAULT NULL COMMENT '更新者',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT='流程spel表达式定义表';

INSERT INTO flow_spel VALUES (1, 'spelRuleComponent', 'selectDeptLeaderById', 'initiatorDeptId', '#{@spelRuleComponent.selectDeptLeaderById(#initiatorDeptId)}', '根据部门id获取部门负责人', '0', '0', 103, 1, sysdate(), 1, sysdate());
INSERT INTO flow_spel VALUES (2, NULL, NULL, 'initiator', '${initiator}', '流程发起人', '0', '0', 103, 1, sysdate(), 1, sysdate());

INSERT INTO sys_menu VALUES ('11801', '流程表达式', '11616', '2', 'spel',    'workflow/spel/index', '', 1, 0, 'C', '0', '0', 'workflow:spel:list', 'input', 103, 1, sysdate(), 1, sysdate(), '流程达式定义菜单');
INSERT INTO sys_menu VALUES ('11802', '流程spel达式定义查询', '11801', 1, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:query', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11803', '流程spel达式定义新增', '11801', 2, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:add', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11804', '流程spel达式定义修改', '11801', 3, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:edit', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11805', '流程spel达式定义删除', '11801', 4, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:remove', '#', 103, 1, sysdate(), NULL, NULL, '');
INSERT INTO sys_menu VALUES ('11806', '流程spel达式定义导出', '11801', 5, '#', '', NULL, 1, 0, 'F', '0', '0', 'workflow:spel:export', '#', 103, 1, sysdate(), NULL, NULL, '');


-- ----------------------------
-- SnailJob 升级 1.6.0
-- ----------------------------

-- drop 号段模式序号ID分配表 用户自行决定是否drop
-- DROP TABLE IF EXISTS sj_sequence_alloc;

-- create 任务执行器信息表
CREATE TABLE `sj_job_executor`
(
    `id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `namespace_id`  varchar(64)         NOT NULL DEFAULT '764d604ec6fc45f68cd92514c40e9e1a' COMMENT '命名空间id',
    `group_name`    varchar(64)         NOT NULL COMMENT '组名称',
    `executor_info` varchar(256)        NOT NULL COMMENT '任务执行器名称',
    `executor_type` varchar(3)          NOT NULL COMMENT '1:java 2:python 3:go',
    `create_dt`     datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_dt`     datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_namespace_id_group_name` (`namespace_id`, `group_name`),
    KEY `idx_create_dt` (`create_dt`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务执行器信息';

-- 新增字段
ALTER TABLE sj_retry_dead_letter
    ADD COLUMN serializer_name VARCHAR(32) NOT NULL DEFAULT 'jackson' COMMENT '执行方法参数序列化器名称' AFTER `executor_name`;
ALTER TABLE sj_retry
    ADD COLUMN serializer_name VARCHAR(32) NOT NULL DEFAULT 'jackson' COMMENT '执行方法参数序列化器名称' AFTER `ext_attrs`;
ALTER TABLE sj_retry_scene_config
    ADD COLUMN owner_id BIGINT(20) NULL DEFAULT NULL COMMENT '负责人id' AFTER `cb_trigger_interval`,
    ADD COLUMN labels VARCHAR(512) NULL DEFAULT '' COMMENT '标签' AFTER `owner_id`;
ALTER TABLE sj_server_node
    ADD COLUMN labels VARCHAR(512) NULL DEFAULT '' COMMENT '标签' AFTER `ext_attrs`;
ALTER TABLE sj_job
    ADD COLUMN labels VARCHAR(512) NULL DEFAULT '' COMMENT '标签' AFTER `owner_id`;
ALTER TABLE sj_workflow
    ADD COLUMN owner_id BIGINT(20) NULL DEFAULT NULL COMMENT '负责人id' AFTER `version`;
