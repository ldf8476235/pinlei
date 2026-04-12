/*
 Navicat Premium Data Transfer

 Source Server         : 中间同步库Sql server-ssh
 Source Server Type    : SQL Server
 Source Server Version : 11002100
 Source Host           : 172.16.0.17:2866
 Source Catalog        : Datainterface_YY
 Source Schema         : dbo

 Target Server Type    : SQL Server
 Target Server Version : 11002100
 File Encoding         : 65001

 Date: 02/02/2026 16:07:31
*/


-- ----------------------------
-- Table structure for base_class
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_class]') AND type IN ('U'))
	DROP TABLE [dbo].[base_class]
GO

CREATE TABLE [dbo].[base_class] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[base_class] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'base_class',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for base_class_sku
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_class_sku]') AND type IN ('U'))
	DROP TABLE [dbo].[base_class_sku]
GO

CREATE TABLE [dbo].[base_class_sku] (
  [id] bigint  IDENTITY(1,1) NOT NULL,
  [store_no] varchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_org_no] varchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_org_name] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_format_no] varchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_format_name] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NULL,
  [business_circle_no] varchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [business_circle_name] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_group_no] varchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_group_name] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NULL,
  [class_no] varchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [class_name] nvarchar(200) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [class_sku] varchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL,
  [class_role] nvarchar(100) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[base_class_sku] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for base_department
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_department]') AND type IN ('U'))
	DROP TABLE [dbo].[base_department]
GO

CREATE TABLE [dbo].[base_department] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_format_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_format_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [business_circle_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [business_circle_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_group_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_group_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_type_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_type_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [preorgcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [preorgname] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [status_no] tinyint  NOT NULL,
  [status_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_staff_num] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_area] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [longitude] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [latitude] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [start_date] date  NULL,
  [end_date] date  NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[base_department] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店/组织编码',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店/组织名称',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'业态编码(例:1-标超,2-便利店,3-社区店等)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'store_format_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'业态名称(例:标超,便利店,社区店等)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'store_format_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商圈编码(例:1-商业区,2-住宅区,3-学校周围等)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'business_circle_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商圈名称(例:商业区,住宅区,学校周围等)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'business_circle_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'店组编码(例:1-城区,2-北区,3-南区等)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'store_group_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'店组名称(例:城区,北区,南区等)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'store_group_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'类型编码(1-门店,2-组织区域/分公司/其他)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'store_type_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'类型名称(门店,组织区域/分公司/其他)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'store_type_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'上级组织/所属组织编码',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'preorgcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'上级组织/所属组织名称',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'preorgname'
GO

EXEC sp_addextendedproperty
'MS_Description', N'状态编码(0-闭店,1-正常营业,2-其他等)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'status_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'状态名称(闭店,正常营业,其他等)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'status_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'员工数量',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'store_staff_num'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店面积（㎡）',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'store_area'
GO

EXEC sp_addextendedproperty
'MS_Description', N'经度(门店所在地的经度)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'longitude'
GO

EXEC sp_addextendedproperty
'MS_Description', N'维度(门店所在地的纬度)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'latitude'
GO

EXEC sp_addextendedproperty
'MS_Description', N'开业时间',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'start_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'闭店时间(指门店停止运营的日期)',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'end_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'base_department',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for base_department_tmp
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_department_tmp]') AND type IN ('U'))
	DROP TABLE [dbo].[base_department_tmp]
GO

CREATE TABLE [dbo].[base_department_tmp] (
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_format_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_format_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [business_circle_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [business_circle_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_group_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_group_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_type_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_type_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [preorgcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [preorgname] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [status_no] tinyint  NOT NULL,
  [status_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_staff_num] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_area] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [longitude] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [latitude] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [start_date] date  NULL,
  [end_date] date  NULL
)
GO

ALTER TABLE [dbo].[base_department_tmp] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for base_dept_products
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_dept_products]') AND type IN ('U'))
	DROP TABLE [dbo].[base_dept_products]
GO

CREATE TABLE [dbo].[base_dept_products] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [brand_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_brand] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_spec] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [expireddate] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [expiredun] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [pspack_unit] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_unit] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_taxin_inprice] decimal(12,2)  NULL,
  [product_saleprice] decimal(12,2)  NULL,
  [status_no] tinyint  NULL,
  [status_name] nvarchar(10) COLLATE Chinese_PRC_CI_AS  NULL,
  [sale_type_no] tinyint  NULL,
  [sale_type] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [logistic_type] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [seasonable_flag] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [seasonable_start_date] datetime2(7)  NULL,
  [seasonable_end_date] datetime2(7)  NULL,
  [seasonable_info] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [display_position] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [sequence_num] decimal(13,2)  NULL,
  [face_num] decimal(13,2)  NULL,
  [depth_num] decimal(13,2)  NULL,
  [full_display_quantity] decimal(13,2)  NULL,
  [first_order_date] datetime2(7)  NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[base_dept_products] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'product_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'品牌编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'brand_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'品牌名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'product_brand'
GO

EXEC sp_addextendedproperty
'MS_Description', N'规格',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'product_spec'
GO

EXEC sp_addextendedproperty
'MS_Description', N'保质期(例:365,90)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'expireddate'
GO

EXEC sp_addextendedproperty
'MS_Description', N'保质期单位(例:日,月,年,小时)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'expiredun'
GO

EXEC sp_addextendedproperty
'MS_Description', N'配送单位名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'pspack_unit'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售单位名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'product_unit'
GO

EXEC sp_addextendedproperty
'MS_Description', N'进价',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'product_taxin_inprice'
GO

EXEC sp_addextendedproperty
'MS_Description', N'当前售价',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'product_saleprice'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品状态编码',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'status_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品状态名称',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'status_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售类型编码(例:1-经销,2-代销,3-租赁,4-联营等)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'sale_type_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售类型名称(例:经销,代销,租赁,联营等)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'sale_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'配送类型名称(例:配送,直送,直通)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'logistic_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'季节性标志(0-否,1-是)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'seasonable_flag'
GO

EXEC sp_addextendedproperty
'MS_Description', N'季节性开始日期',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'seasonable_start_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'季节性结束日期',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'seasonable_end_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'季节性月份信息(例:001110011100,以1代表该月份销售,以0代表该月份不销售)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'seasonable_info'
GO

EXEC sp_addextendedproperty
'MS_Description', N'陈列位置(非生鲜部门)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'display_position'
GO

EXEC sp_addextendedproperty
'MS_Description', N'层(非生鲜部门)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'sequence_num'
GO

EXEC sp_addextendedproperty
'MS_Description', N'面(非生鲜部门)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'face_num'
GO

EXEC sp_addextendedproperty
'MS_Description', N'深(非生鲜部门)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'depth_num'
GO

EXEC sp_addextendedproperty
'MS_Description', N'满陈列量(非生鲜部门)',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'full_display_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'首次进货日期',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'first_order_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'base_dept_products',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for base_division
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_division]') AND type IN ('U'))
	DROP TABLE [dbo].[base_division]
GO

CREATE TABLE [dbo].[base_division] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [updatetime] datetime2(7)  NOT NULL
)
GO

ALTER TABLE [dbo].[base_division] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'base_division',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for base_product
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_product]') AND type IN ('U'))
	DROP TABLE [dbo].[base_product]
GO

CREATE TABLE [dbo].[base_product] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [brand_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_brand] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_spec] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [expireddate] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [expiredun] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [pspack_unit] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_unit] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_taxin_inprice] decimal(12,2)  NULL,
  [product_saleprice] decimal(12,2)  NULL,
  [status_no] tinyint  NULL,
  [status_name] nvarchar(10) COLLATE Chinese_PRC_CI_AS  NULL,
  [sale_type_no] tinyint  NULL,
  [sale_type] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [logistic_type] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [seasonable_flag] tinyint  NULL,
  [seasonable_start_date] datetime2(7)  NULL,
  [seasonable_end_date] datetime2(7)  NULL,
  [seasonable_info] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [width] decimal(12,2)  NULL,
  [height] decimal(12,2)  NULL,
  [depth] decimal(12,2)  NULL,
  [color] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [first_order_date] datetime2(7)  NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[base_product] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'product_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'品牌编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'brand_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'品牌名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'product_brand'
GO

EXEC sp_addextendedproperty
'MS_Description', N'规格',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'product_spec'
GO

EXEC sp_addextendedproperty
'MS_Description', N'保质期(例:365,90)',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'expireddate'
GO

EXEC sp_addextendedproperty
'MS_Description', N'保质期单位(例:日,月,年,小时)',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'expiredun'
GO

EXEC sp_addextendedproperty
'MS_Description', N'配送单位名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'pspack_unit'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售单位名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'product_unit'
GO

EXEC sp_addextendedproperty
'MS_Description', N'进价',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'product_taxin_inprice'
GO

EXEC sp_addextendedproperty
'MS_Description', N'售价',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'product_saleprice'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品状态编码',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'status_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品状态名称',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'status_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售类型编码(例:1-经销,2-代销,3-租赁,4-联营等)',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'sale_type_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售类型名称(例:经销,代销,租赁,联营等)',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'sale_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'配送类型名称(例:配送,直送,直通)',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'logistic_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'季节性标志(0-否,1-是)',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'seasonable_flag'
GO

EXEC sp_addextendedproperty
'MS_Description', N'季节性开始日期',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'seasonable_start_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'季节性结束日期',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'seasonable_end_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'季节性月份信息(例:001110011100,以1代表该月份销售,以0代表该月份不销售)',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'seasonable_info'
GO

EXEC sp_addextendedproperty
'MS_Description', N'宽度(单位:cm;可视化系统使用)',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'width'
GO

EXEC sp_addextendedproperty
'MS_Description', N'高度(单位:cm;可视化系统使用)',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'height'
GO

EXEC sp_addextendedproperty
'MS_Description', N'深度(单位:cm;可视化系统使用)',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'depth'
GO

EXEC sp_addextendedproperty
'MS_Description', N'颜色(可视化系统使用)',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'color'
GO

EXEC sp_addextendedproperty
'MS_Description', N'首次进货日期',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'first_order_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'base_product',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for base_vendor_contract_info
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_vendor_contract_info]') AND type IN ('U'))
	DROP TABLE [dbo].[base_vendor_contract_info]
GO

CREATE TABLE [dbo].[base_vendor_contract_info] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [vendor_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [contract_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [contract_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [contract_start_date] datetime2(7)  NOT NULL,
  [contract_end_date] datetime2(7)  NOT NULL,
  [account_days] int  NULL,
  [status_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [status_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[base_vendor_contract_info] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_contract_info',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'供应商编码',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_contract_info',
'COLUMN', N'vendor_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'合同编码',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_contract_info',
'COLUMN', N'contract_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'合同名称',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_contract_info',
'COLUMN', N'contract_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'合同开始时间',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_contract_info',
'COLUMN', N'contract_start_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'合同结束时间',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_contract_info',
'COLUMN', N'contract_end_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'账期',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_contract_info',
'COLUMN', N'account_days'
GO

EXEC sp_addextendedproperty
'MS_Description', N'供应商合同状态编码',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_contract_info',
'COLUMN', N'status_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'供应商合同状态名称',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_contract_info',
'COLUMN', N'status_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_contract_info',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for base_vendor_contract_info_tmp
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_vendor_contract_info_tmp]') AND type IN ('U'))
	DROP TABLE [dbo].[base_vendor_contract_info_tmp]
GO

CREATE TABLE [dbo].[base_vendor_contract_info_tmp] (
  [vendor_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [contract_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [contract_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [contract_start_date] datetime2(7)  NOT NULL,
  [contract_end_date] datetime2(7)  NOT NULL,
  [account_days] int  NULL,
  [status_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [status_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL
)
GO

ALTER TABLE [dbo].[base_vendor_contract_info_tmp] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for base_vendor_info
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_vendor_info]') AND type IN ('U'))
	DROP TABLE [dbo].[base_vendor_info]
GO

CREATE TABLE [dbo].[base_vendor_info] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [vendor_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [vendor_name] nvarchar(128) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [vendor_info] int  NOT NULL,
  [status_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [status_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[base_vendor_info] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_info',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'供应商编码',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_info',
'COLUMN', N'vendor_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'供应商名称',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_info',
'COLUMN', N'vendor_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'供应商级别：0：主供应商；1：次供应商',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_info',
'COLUMN', N'vendor_info'
GO

EXEC sp_addextendedproperty
'MS_Description', N'供应商状态编码',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_info',
'COLUMN', N'status_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'供应商状态名称',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_info',
'COLUMN', N'status_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'base_vendor_info',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for base_vendor_info_tmp
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_vendor_info_tmp]') AND type IN ('U'))
	DROP TABLE [dbo].[base_vendor_info_tmp]
GO

CREATE TABLE [dbo].[base_vendor_info_tmp] (
  [vendor_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [vendor_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [vendor_info] int  NOT NULL,
  [status_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [status_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL
)
GO

ALTER TABLE [dbo].[base_vendor_info_tmp] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for base_vendor_product_info
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_vendor_product_info]') AND type IN ('U'))
	DROP TABLE [dbo].[base_vendor_product_info]
GO

CREATE TABLE [dbo].[base_vendor_product_info] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [vendor_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [vendor_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [status_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [status_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[base_vendor_product_info] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for base_vendor_store_product_info
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[base_vendor_store_product_info]') AND type IN ('U'))
	DROP TABLE [dbo].[base_vendor_store_product_info]
GO

CREATE TABLE [dbo].[base_vendor_store_product_info] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [vendor_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [vendor_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [status_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [status_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL,
  [store_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL
)
GO

ALTER TABLE [dbo].[base_vendor_store_product_info] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for fact_loss_day
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[fact_loss_day]') AND type IN ('U'))
	DROP TABLE [dbo].[fact_loss_day]
GO

CREATE TABLE [dbo].[fact_loss_day] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_loss] decimal(13,2)  NOT NULL,
  [product_loss_quantity] decimal(13,2)  NOT NULL,
  [loss_date] date  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL,
  [loss_type_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [loss_type_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[fact_loss_day] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'product_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'损耗金额',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'product_loss'
GO

EXEC sp_addextendedproperty
'MS_Description', N'损耗数量',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'product_loss_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发生日期',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'loss_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'updatetime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'损耗类型编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'loss_type_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'损耗类型名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_loss_day',
'COLUMN', N'loss_type_name'
GO


-- ----------------------------
-- Table structure for fact_order_day
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[fact_order_day]') AND type IN ('U'))
	DROP TABLE [dbo].[fact_order_day]
GO

CREATE TABLE [dbo].[fact_order_day] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [order_date] date  NOT NULL,
  [order_quantity] decimal(13,2)  NOT NULL,
  [estimated_arrival_date] date  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[fact_order_day] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'fact_order_day',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_order_day',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_order_day',
'COLUMN', N'store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_order_day',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'fact_order_day',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_order_day',
'COLUMN', N'product_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'订购时间',
'SCHEMA', N'dbo',
'TABLE', N'fact_order_day',
'COLUMN', N'order_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'订购数量',
'SCHEMA', N'dbo',
'TABLE', N'fact_order_day',
'COLUMN', N'order_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'预计到货日期',
'SCHEMA', N'dbo',
'TABLE', N'fact_order_day',
'COLUMN', N'estimated_arrival_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'fact_order_day',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for fact_sales_day
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[fact_sales_day]') AND type IN ('U'))
	DROP TABLE [dbo].[fact_sales_day]
GO

CREATE TABLE [dbo].[fact_sales_day] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [sale_channel] tinyint  NOT NULL,
  [online_type] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [online_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_name] nvarchar(128) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [now_product_saleprice] decimal(13,2)  NULL,
  [sale_quantity] decimal(13,2)  NOT NULL,
  [sales] decimal(20,6)  NOT NULL,
  [sale_cost] decimal(20,6)  NOT NULL,
  [gross] decimal(20,6)  NOT NULL,
  [customer_return_quantity] decimal(13,2)  NULL,
  [customer_return] decimal(20,6)  NULL,
  [cx_flag] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [sale_date] date  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[fact_sales_day] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售渠道类型',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'sale_channel'
GO

EXEC sp_addextendedproperty
'MS_Description', N'线上销售渠道类型',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'online_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'线上销售渠道名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'online_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'product_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'当日售价',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'now_product_saleprice'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售量',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'sale_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售额',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'sales'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售成本',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'sale_cost'
GO

EXEC sp_addextendedproperty
'MS_Description', N'毛利额',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'gross'
GO

EXEC sp_addextendedproperty
'MS_Description', N'顾客退货数量',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'customer_return_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'顾客退货金额',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'customer_return'
GO

EXEC sp_addextendedproperty
'MS_Description', N'参加促销活动标志(0-否,1-是)',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'cx_flag'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发生日期',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'sale_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'fact_sales_day',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for fact_stock_day
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[fact_stock_day]') AND type IN ('U'))
	DROP TABLE [dbo].[fact_stock_day]
GO

CREATE TABLE [dbo].[fact_stock_day] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [stock_quantity] decimal(13,2)  NOT NULL,
  [stock_unit] nvarchar(64) COLLATE Chinese_PRC_CI_AS  NULL,
  [onroad_quantity] decimal(13,2)  NULL,
  [stock_cost] decimal(20,6)  NULL,
  [stock_sale_money] decimal(20,6)  NULL,
  [last_order_date] date  NULL,
  [stock_date] date  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[fact_stock_day] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'product_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存量',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'stock_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存单位',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'stock_unit'
GO

EXEC sp_addextendedproperty
'MS_Description', N'在途数',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'onroad_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存总成本',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'stock_cost'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存售价总额',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'stock_sale_money'
GO

EXEC sp_addextendedproperty
'MS_Description', N'最近进货日期',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'last_order_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发生日期',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'stock_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_day',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for fact_stock_live
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[fact_stock_live]') AND type IN ('U'))
	DROP TABLE [dbo].[fact_stock_live]
GO

CREATE TABLE [dbo].[fact_stock_live] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_name] nvarchar(128) COLLATE Chinese_PRC_CI_AS  NULL,
  [stock_quantity] decimal(13,2)  NOT NULL,
  [stock_unit] nvarchar(64) COLLATE Chinese_PRC_CI_AS  NULL,
  [onroad_quantity] decimal(13,2)  NULL,
  [stock_cost] decimal(20,6)  NULL,
  [stock_sale_money] decimal(20,6)  NULL,
  [stock_date] datetime2(7)  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[fact_stock_live] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'product_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存量',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'stock_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存单位',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'stock_unit'
GO

EXEC sp_addextendedproperty
'MS_Description', N'在途数',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'onroad_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存总成本',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'stock_cost'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存售价总额',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'stock_sale_money'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发生日期',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'stock_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'fact_stock_live',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for fact_vendor_order_month
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[fact_vendor_order_month]') AND type IN ('U'))
	DROP TABLE [dbo].[fact_vendor_order_month]
GO

CREATE TABLE [dbo].[fact_vendor_order_month] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [order_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [order_month] int  NOT NULL,
  [vendor_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [final_store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [final_store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [order_date] date  NOT NULL,
  [order_quantity] decimal(13,2)  NOT NULL,
  [arrival_date] date  NULL,
  [arrival_quantity] decimal(13,2)  NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[fact_vendor_order_month] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'订单编号',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'order_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'订单月份',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'order_month'
GO

EXEC sp_addextendedproperty
'MS_Description', N'供应商编号',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'vendor_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'订购对象编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'final_store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'订购对象名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'final_store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品名称',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'product_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'订购时间',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'order_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'订购数量',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'order_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'到货时间',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'arrival_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'到货数量',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'arrival_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'fact_vendor_order_month',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for history_fact_loss_day
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[history_fact_loss_day]') AND type IN ('U'))
	DROP TABLE [dbo].[history_fact_loss_day]
GO

CREATE TABLE [dbo].[history_fact_loss_day] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_loss] decimal(13,2)  NOT NULL,
  [product_loss_quantity] decimal(13,2)  NOT NULL,
  [loss_date] date  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL,
  [loss_type_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [loss_type_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[history_fact_loss_day] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'product_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'损耗金额',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'product_loss'
GO

EXEC sp_addextendedproperty
'MS_Description', N'损耗数量',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'product_loss_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发生日期',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'loss_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'updatetime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'损耗类型编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'loss_type_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'损耗类型名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_loss_day',
'COLUMN', N'loss_type_name'
GO


-- ----------------------------
-- Table structure for history_fact_sales_day
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[history_fact_sales_day]') AND type IN ('U'))
	DROP TABLE [dbo].[history_fact_sales_day]
GO

CREATE TABLE [dbo].[history_fact_sales_day] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [sale_channel] tinyint  NOT NULL,
  [online_type] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [online_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [now_product_saleprice] decimal(13,2)  NOT NULL,
  [sale_quantity] decimal(13,2)  NOT NULL,
  [sales] decimal(20,6)  NOT NULL,
  [sale_cost] decimal(20,6)  NOT NULL,
  [gross] decimal(20,6)  NOT NULL,
  [customer_return_quantity] decimal(13,2)  NULL,
  [customer_return] decimal(20,6)  NULL,
  [cx_flag] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [sale_date] date  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NULL
)
GO

ALTER TABLE [dbo].[history_fact_sales_day] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售渠道类型',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'sale_channel'
GO

EXEC sp_addextendedproperty
'MS_Description', N'线上销售渠道类型',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'online_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'线上销售渠道名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'online_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'product_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'当日售价',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'now_product_saleprice'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售量',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'sale_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售额',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'sales'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售成本',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'sale_cost'
GO

EXEC sp_addextendedproperty
'MS_Description', N'毛利额',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'gross'
GO

EXEC sp_addextendedproperty
'MS_Description', N'顾客退货数量',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'customer_return_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'顾客退货金额',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'customer_return'
GO

EXEC sp_addextendedproperty
'MS_Description', N'参加促销活动标志(0-否,1-是)',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'cx_flag'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发生日期',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'sale_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_sales_day',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for history_fact_stock_day
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[history_fact_stock_day]') AND type IN ('U'))
	DROP TABLE [dbo].[history_fact_stock_day]
GO

CREATE TABLE [dbo].[history_fact_stock_day] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_name] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [stock_quantity] decimal(13,2)  NOT NULL,
  [stock_unit] nvarchar(10) COLLATE Chinese_PRC_CI_AS  NULL,
  [onroad_quantity] decimal(13,2)  NOT NULL,
  [stock_cost] decimal(20,6)  NOT NULL,
  [stock_sale_money] decimal(20,6)  NOT NULL,
  [last_order_date] date  NULL,
  [stock_date] date  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[history_fact_stock_day] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品名称',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'product_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存量',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'stock_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存单位',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'stock_unit'
GO

EXEC sp_addextendedproperty
'MS_Description', N'在途数',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'onroad_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存总成本',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'stock_cost'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存售价总额',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'stock_sale_money'
GO

EXEC sp_addextendedproperty
'MS_Description', N'最近进货日期',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'last_order_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发生日期',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'stock_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'history_fact_stock_day',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for history_sh7_saleplu
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[history_sh7_saleplu]') AND type IN ('U'))
	DROP TABLE [dbo].[history_sh7_saleplu]
GO

CREATE TABLE [dbo].[history_sh7_saleplu] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [sale_channel] tinyint  NOT NULL,
  [online_type] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [online_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [card_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [xsdate] datetime2(7)  NOT NULL,
  [sale_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sale_quantity] decimal(12,3)  NOT NULL,
  [nomal_price] decimal(12,3)  NULL,
  [price] decimal(12,3)  NULL,
  [vip_price] decimal(12,3)  NULL,
  [taxin_money] decimal(12,3)  NULL,
  [act_money] decimal(12,3)  NOT NULL,
  [settlement_date] date  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[history_sh7_saleplu] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售渠道类型',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'sale_channel'
GO

EXEC sp_addextendedproperty
'MS_Description', N'线上销售渠道类型',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'online_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'线上销售渠道名称',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'online_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'会员卡号',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'card_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售日期时间',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'xsdate'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售流水号',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'sale_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售数量',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'sale_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'正常售价',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'nomal_price'
GO

EXEC sp_addextendedproperty
'MS_Description', N'实际售价',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'price'
GO

EXEC sp_addextendedproperty
'MS_Description', N'vip售价',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'vip_price'
GO

EXEC sp_addextendedproperty
'MS_Description', N'应收金额',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'taxin_money'
GO

EXEC sp_addextendedproperty
'MS_Description', N'实收金额',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'act_money'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发生日期',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'settlement_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'history_sh7_saleplu',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for product_tag_info
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[product_tag_info]') AND type IN ('U'))
	DROP TABLE [dbo].[product_tag_info]
GO

CREATE TABLE [dbo].[product_tag_info] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [tag_type_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [tag_type_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [tag_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [tag_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [updatetime] datetime DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[product_tag_info] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for product_vendor_cost_info
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[product_vendor_cost_info]') AND type IN ('U'))
	DROP TABLE [dbo].[product_vendor_cost_info]
GO

CREATE TABLE [dbo].[product_vendor_cost_info] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [vendor_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [final_store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [final_store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [cost_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [cost_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [cost_money] decimal(13,2)  NOT NULL,
  [cost_date] date  NOT NULL,
  [cost_type] int  NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[product_vendor_cost_info] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'product_vendor_cost_info',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'供应商编码',
'SCHEMA', N'dbo',
'TABLE', N'product_vendor_cost_info',
'COLUMN', N'vendor_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'收取对象编码',
'SCHEMA', N'dbo',
'TABLE', N'product_vendor_cost_info',
'COLUMN', N'final_store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'收取对象名称',
'SCHEMA', N'dbo',
'TABLE', N'product_vendor_cost_info',
'COLUMN', N'final_store_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'product_vendor_cost_info',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'product_vendor_cost_info',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'费用编号',
'SCHEMA', N'dbo',
'TABLE', N'product_vendor_cost_info',
'COLUMN', N'cost_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'费用名称',
'SCHEMA', N'dbo',
'TABLE', N'product_vendor_cost_info',
'COLUMN', N'cost_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'费用金额',
'SCHEMA', N'dbo',
'TABLE', N'product_vendor_cost_info',
'COLUMN', N'cost_money'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发生日期',
'SCHEMA', N'dbo',
'TABLE', N'product_vendor_cost_info',
'COLUMN', N'cost_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'product_vendor_cost_info',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for product_vendor_cost_info_tmp
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[product_vendor_cost_info_tmp]') AND type IN ('U'))
	DROP TABLE [dbo].[product_vendor_cost_info_tmp]
GO

CREATE TABLE [dbo].[product_vendor_cost_info_tmp] (
  [vendor_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [final_store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [final_store_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [cost_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [cost_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [cost_money] decimal(13,2)  NOT NULL,
  [cost_date] date  NOT NULL,
  [cost_type] int  NULL
)
GO

ALTER TABLE [dbo].[product_vendor_cost_info_tmp] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for sh10_procxorg
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[sh10_procxorg]') AND type IN ('U'))
	DROP TABLE [dbo].[sh10_procxorg]
GO

CREATE TABLE [dbo].[sh10_procxorg] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [cx_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[sh10_procxorg] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'sh10_procxorg',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'促销单号',
'SCHEMA', N'dbo',
'TABLE', N'sh10_procxorg',
'COLUMN', N'cx_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'sh10_procxorg',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'sh10_procxorg',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'sh10_procxorg',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'sh10_procxorg',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for sh10_procxorg_tmp
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[sh10_procxorg_tmp]') AND type IN ('U'))
	DROP TABLE [dbo].[sh10_procxorg_tmp]
GO

CREATE TABLE [dbo].[sh10_procxorg_tmp] (
  [cx_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL
)
GO

ALTER TABLE [dbo].[sh10_procxorg_tmp] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for sh7_saleplu
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[sh7_saleplu]') AND type IN ('U'))
	DROP TABLE [dbo].[sh7_saleplu]
GO

CREATE TABLE [dbo].[sh7_saleplu] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [sale_channel] tinyint  NOT NULL,
  [online_type] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [online_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [card_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [xsdate] datetime2(7)  NOT NULL,
  [sale_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sale_quantity] decimal(12,3)  NOT NULL,
  [nomal_price] decimal(12,3)  NULL,
  [price] decimal(12,3)  NULL,
  [vip_price] decimal(12,3)  NULL,
  [taxin_money] decimal(12,3)  NULL,
  [act_money] decimal(12,3)  NOT NULL,
  [settlement_date] date  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[sh7_saleplu] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售渠道类型',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'sale_channel'
GO

EXEC sp_addextendedproperty
'MS_Description', N'线上销售渠道类型',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'online_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'线上销售渠道名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'online_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'会员卡号',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'card_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售日期时间',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'xsdate'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售流水号',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'sale_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售数量',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'sale_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'正常售价',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'nomal_price'
GO

EXEC sp_addextendedproperty
'MS_Description', N'实际售价',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'price'
GO

EXEC sp_addextendedproperty
'MS_Description', N'vip售价',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'vip_price'
GO

EXEC sp_addextendedproperty
'MS_Description', N'应收金额',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'taxin_money'
GO

EXEC sp_addextendedproperty
'MS_Description', N'实收金额',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'act_money'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发生日期',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'settlement_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for sh7_saleplu_live
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[sh7_saleplu_live]') AND type IN ('U'))
	DROP TABLE [dbo].[sh7_saleplu_live]
GO

CREATE TABLE [dbo].[sh7_saleplu_live] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [sale_channel] tinyint  NOT NULL,
  [online_type] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [online_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [card_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [xsdate] datetime2(7)  NOT NULL,
  [sale_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [one_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [one_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [two_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [three_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [four_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [five_class_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [div_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [subdiv_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [bclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [mclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [sclass_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [act_money] decimal(12,3)  NOT NULL,
  [sale_quantity] decimal(12,3)  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[sh7_saleplu_live] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售渠道类型',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'sale_channel'
GO

EXEC sp_addextendedproperty
'MS_Description', N'线上销售渠道类型',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'online_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'线上销售渠道名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'online_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'会员卡号',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'card_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售日期时间',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'xsdate'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售流水号',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'sale_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'one_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'one_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'two_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'two_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'three_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'three_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'four_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'four_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'five_class_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级品类名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'five_class_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'div_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'一级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'div_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'subdiv_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'二级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'subdiv_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'bclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'三级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'bclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'mclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'四级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'mclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'sclass_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'五级部门名称',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'sclass_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'实收金额',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'act_money'
GO

EXEC sp_addextendedproperty
'MS_Description', N'销售数量',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'sale_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'sh7_saleplu_live',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for sh9_procxbill
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[sh9_procxbill]') AND type IN ('U'))
	DROP TABLE [dbo].[sh9_procxbill]
GO

CREATE TABLE [dbo].[sh9_procxbill] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [cx_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [cx_title] nvarchar(128) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [begin_date] datetime2(7)  NOT NULL,
  [end_date] datetime2(7)  NOT NULL,
  [cx_type] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [cx_type_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL,
  [cx_theme_no] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL,
  [cx_theme_name] varchar(50) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[sh9_procxbill] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'sh9_procxbill',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'促销单号',
'SCHEMA', N'dbo',
'TABLE', N'sh9_procxbill',
'COLUMN', N'cx_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'促销标题',
'SCHEMA', N'dbo',
'TABLE', N'sh9_procxbill',
'COLUMN', N'cx_title'
GO

EXEC sp_addextendedproperty
'MS_Description', N'开始日期',
'SCHEMA', N'dbo',
'TABLE', N'sh9_procxbill',
'COLUMN', N'begin_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'结束日期',
'SCHEMA', N'dbo',
'TABLE', N'sh9_procxbill',
'COLUMN', N'end_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'促销类型',
'SCHEMA', N'dbo',
'TABLE', N'sh9_procxbill',
'COLUMN', N'cx_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'促销类型名称',
'SCHEMA', N'dbo',
'TABLE', N'sh9_procxbill',
'COLUMN', N'cx_type_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'sh9_procxbill',
'COLUMN', N'updatetime'
GO

EXEC sp_addextendedproperty
'MS_Description', N'促销主题编号',
'SCHEMA', N'dbo',
'TABLE', N'sh9_procxbill',
'COLUMN', N'cx_theme_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'促销主题名称',
'SCHEMA', N'dbo',
'TABLE', N'sh9_procxbill',
'COLUMN', N'cx_theme_name'
GO


-- ----------------------------
-- Table structure for sh9_procxbill_tmp
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[sh9_procxbill_tmp]') AND type IN ('U'))
	DROP TABLE [dbo].[sh9_procxbill_tmp]
GO

CREATE TABLE [dbo].[sh9_procxbill_tmp] (
  [cx_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [cx_title] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [begin_date] datetime2(7)  NOT NULL,
  [end_date] datetime2(7)  NOT NULL,
  [cx_type] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [cx_type_name] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NULL,
  [cx_status] nvarchar(10) COLLATE Chinese_PRC_CI_AS  NULL,
  [cx_zzdate] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[sh9_procxbill_tmp] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for tmp_store
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[tmp_store]') AND type IN ('U'))
	DROP TABLE [dbo].[tmp_store]
GO

CREATE TABLE [dbo].[tmp_store] (
  [store_id] varchar(10) COLLATE Chinese_PRC_CI_AS  NULL,
  [store_name] nvarchar(100) COLLATE Chinese_PRC_CI_AS  NULL
)
GO

ALTER TABLE [dbo].[tmp_store] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for vip_base_info
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[vip_base_info]') AND type IN ('U'))
	DROP TABLE [dbo].[vip_base_info]
GO

CREATE TABLE [dbo].[vip_base_info] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [card_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [card_type] tinyint  NULL,
  [gender] tinyint  NOT NULL,
  [age] tinyint  NULL,
  [birthday] date  NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [create_time] datetime2(7)  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL
)
GO

ALTER TABLE [dbo].[vip_base_info] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'vip_base_info',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'会员卡号',
'SCHEMA', N'dbo',
'TABLE', N'vip_base_info',
'COLUMN', N'card_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'会员卡类型',
'SCHEMA', N'dbo',
'TABLE', N'vip_base_info',
'COLUMN', N'card_type'
GO

EXEC sp_addextendedproperty
'MS_Description', N'性别(1-男,2-女,3-未知)',
'SCHEMA', N'dbo',
'TABLE', N'vip_base_info',
'COLUMN', N'gender'
GO

EXEC sp_addextendedproperty
'MS_Description', N'年龄(“年龄”与“出生日期”有一项即可)',
'SCHEMA', N'dbo',
'TABLE', N'vip_base_info',
'COLUMN', N'age'
GO

EXEC sp_addextendedproperty
'MS_Description', N'出生日期(“年龄”与“出生日期”有一项即可)',
'SCHEMA', N'dbo',
'TABLE', N'vip_base_info',
'COLUMN', N'birthday'
GO

EXEC sp_addextendedproperty
'MS_Description', N'注册门店编码',
'SCHEMA', N'dbo',
'TABLE', N'vip_base_info',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'注册时间',
'SCHEMA', N'dbo',
'TABLE', N'vip_base_info',
'COLUMN', N'create_time'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'vip_base_info',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Table structure for vip_base_info_tmp
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[vip_base_info_tmp]') AND type IN ('U'))
	DROP TABLE [dbo].[vip_base_info_tmp]
GO

CREATE TABLE [dbo].[vip_base_info_tmp] (
  [card_no] nvarchar(50) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [card_type] tinyint  NULL,
  [gender] tinyint  NOT NULL,
  [age] tinyint  NULL,
  [birthday] date  NULL,
  [store_no] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [create_time] datetime2(7)  NOT NULL
)
GO

ALTER TABLE [dbo].[vip_base_info_tmp] SET (LOCK_ESCALATION = TABLE)
GO


-- ----------------------------
-- Table structure for warehouse_product_stock_info
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[warehouse_product_stock_info]') AND type IN ('U'))
	DROP TABLE [dbo].[warehouse_product_stock_info]
GO

CREATE TABLE [dbo].[warehouse_product_stock_info] (
  [id] int  IDENTITY(1,1) NOT NULL,
  [warehouse_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [warehouse_name] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [store_no] nvarchar(max) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_no] nvarchar(20) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [product_barcode] nvarchar(40) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [stock_quantity] char(10) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [stock_date] date  NOT NULL,
  [updatetime] datetime2(7) DEFAULT (getdate()) NOT NULL,
  [stock_cost] decimal(13,2)  NULL
)
GO

ALTER TABLE [dbo].[warehouse_product_stock_info] SET (LOCK_ESCALATION = TABLE)
GO

EXEC sp_addextendedproperty
'MS_Description', N'ID ',
'SCHEMA', N'dbo',
'TABLE', N'warehouse_product_stock_info',
'COLUMN', N'id'
GO

EXEC sp_addextendedproperty
'MS_Description', N'大仓编号',
'SCHEMA', N'dbo',
'TABLE', N'warehouse_product_stock_info',
'COLUMN', N'warehouse_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'大仓名称',
'SCHEMA', N'dbo',
'TABLE', N'warehouse_product_stock_info',
'COLUMN', N'warehouse_name'
GO

EXEC sp_addextendedproperty
'MS_Description', N'门店编码',
'SCHEMA', N'dbo',
'TABLE', N'warehouse_product_stock_info',
'COLUMN', N'store_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品编码',
'SCHEMA', N'dbo',
'TABLE', N'warehouse_product_stock_info',
'COLUMN', N'product_no'
GO

EXEC sp_addextendedproperty
'MS_Description', N'商品条码',
'SCHEMA', N'dbo',
'TABLE', N'warehouse_product_stock_info',
'COLUMN', N'product_barcode'
GO

EXEC sp_addextendedproperty
'MS_Description', N'库存数量',
'SCHEMA', N'dbo',
'TABLE', N'warehouse_product_stock_info',
'COLUMN', N'stock_quantity'
GO

EXEC sp_addextendedproperty
'MS_Description', N'发生日期',
'SCHEMA', N'dbo',
'TABLE', N'warehouse_product_stock_info',
'COLUMN', N'stock_date'
GO

EXEC sp_addextendedproperty
'MS_Description', N'数据更新时间',
'SCHEMA', N'dbo',
'TABLE', N'warehouse_product_stock_info',
'COLUMN', N'updatetime'
GO


-- ----------------------------
-- Procedure structure for OPER_DB_Shrink
-- ----------------------------
IF EXISTS (SELECT * FROM sys.all_objects WHERE object_id = OBJECT_ID(N'[dbo].[OPER_DB_Shrink]') AND type IN ('P', 'PC', 'RF', 'X'))
	DROP PROCEDURE[dbo].[OPER_DB_Shrink]
GO

CREATE PROCEDURE [dbo].[OPER_DB_Shrink]
	@DataBaseName nvarchar(200)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

	-- 切换数据库
	exec( N'use ' +  @DataBaseName);

	-- 取得目标数据库LOG名称
	DECLARE @db_log_name NVARCHAR(200);

	SELECT @db_log_name = name FROM sys.database_files WHERE type_desc = 'LOG';

	-- 收缩主数据文件
	DBCC SHRINKDATABASE (@DataBaseName, 10);  
	
	-- 收缩日志文件
	EXEC(N'ALTER DATABASE ' + @DataBaseName + ' SET RECOVERY SIMPLE');

	DBCC SHRINKFILE (@db_log_name, 5);

	EXEC(N'ALTER DATABASE ' + @DataBaseName + ' SET RECOVERY FULL');

END
GO


-- ----------------------------
-- Primary Key structure for table base_class
-- ----------------------------
ALTER TABLE [dbo].[base_class] ADD CONSTRAINT [PK__base_cla__3213E83F03317E3D] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table base_class_sku
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_base_class_sku]
ON [dbo].[base_class_sku] (
  [store_no] ASC,
  [class_no] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table base_class_sku
-- ----------------------------
ALTER TABLE [dbo].[base_class_sku] ADD CONSTRAINT [PK__base_cla__3213E83F235BE367] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table base_department
-- ----------------------------
ALTER TABLE [dbo].[base_department] ADD CONSTRAINT [PK__base_dep__3213E83F07020F21] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table base_dept_products
-- ----------------------------
ALTER TABLE [dbo].[base_dept_products] ADD CONSTRAINT [PK__base_dep__3213E83F0AD2A005] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table base_division
-- ----------------------------
ALTER TABLE [dbo].[base_division] ADD CONSTRAINT [PK__base_div__3213E83F0EA330E9] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table base_product
-- ----------------------------
ALTER TABLE [dbo].[base_product] ADD CONSTRAINT [PK__base_pro__3213E83F1273C1CD] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table base_vendor_contract_info
-- ----------------------------
ALTER TABLE [dbo].[base_vendor_contract_info] ADD CONSTRAINT [PK__base_ven__3213E83F164452B1] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table base_vendor_info
-- ----------------------------
ALTER TABLE [dbo].[base_vendor_info] ADD CONSTRAINT [PK__base_ven__3213E83F1A14E395] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table base_vendor_product_info
-- ----------------------------
ALTER TABLE [dbo].[base_vendor_product_info] ADD CONSTRAINT [PK__base_ven__3213E83F1DE574802] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table base_vendor_store_product_info
-- ----------------------------
ALTER TABLE [dbo].[base_vendor_store_product_info] ADD CONSTRAINT [PK__base_ven__3213E83F1DE57480] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table fact_loss_day
-- ----------------------------
ALTER TABLE [dbo].[fact_loss_day] ADD CONSTRAINT [PK__fact_los__3213E83F21B6055D] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table fact_order_day
-- ----------------------------
ALTER TABLE [dbo].[fact_order_day] ADD CONSTRAINT [PK__fact_ord__3213E83F25869641] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table fact_sales_day
-- ----------------------------
ALTER TABLE [dbo].[fact_sales_day] ADD CONSTRAINT [PK__fact_sal__3213E83F29572725] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table fact_stock_day
-- ----------------------------
ALTER TABLE [dbo].[fact_stock_day] ADD CONSTRAINT [PK__fact_sto__3213E83F2D27B809] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table fact_stock_live
-- ----------------------------
ALTER TABLE [dbo].[fact_stock_live] ADD CONSTRAINT [PK__fact_sto__3213E83F30F848ED] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table fact_vendor_order_month
-- ----------------------------
ALTER TABLE [dbo].[fact_vendor_order_month] ADD CONSTRAINT [PK__fact_ven__3213E83F34C8D9D1] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table history_fact_loss_day
-- ----------------------------
ALTER TABLE [dbo].[history_fact_loss_day] ADD CONSTRAINT [PK__history___3213E83F38996AB5] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table history_fact_sales_day
-- ----------------------------
ALTER TABLE [dbo].[history_fact_sales_day] ADD CONSTRAINT [PK__history___3213E83F3C69FB99] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Indexes structure for table history_fact_stock_day
-- ----------------------------
CREATE NONCLUSTERED INDEX [IX_history_fact_stock_day_stock_date]
ON [dbo].[history_fact_stock_day] (
  [stock_date] ASC
)
GO


-- ----------------------------
-- Primary Key structure for table history_fact_stock_day
-- ----------------------------
ALTER TABLE [dbo].[history_fact_stock_day] ADD CONSTRAINT [PK__history___3213E83F403A8C7D] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table history_sh7_saleplu
-- ----------------------------
ALTER TABLE [dbo].[history_sh7_saleplu] ADD CONSTRAINT [PK__history___3213E83F440B1D61] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table product_tag_info
-- ----------------------------
ALTER TABLE [dbo].[product_tag_info] ADD CONSTRAINT [PK__product___3213E83F803DF598] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table product_vendor_cost_info
-- ----------------------------
ALTER TABLE [dbo].[product_vendor_cost_info] ADD CONSTRAINT [PK__product___3213E83F47DBAE45] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table sh10_procxorg
-- ----------------------------
ALTER TABLE [dbo].[sh10_procxorg] ADD CONSTRAINT [PK__sh10_pro__3213E83F4BAC3F29] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table sh7_saleplu
-- ----------------------------
ALTER TABLE [dbo].[sh7_saleplu] ADD CONSTRAINT [PK__sh7_sale__3213E83F4F7CD00D] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table sh7_saleplu_live
-- ----------------------------
ALTER TABLE [dbo].[sh7_saleplu_live] ADD CONSTRAINT [PK__sh7_sale__3213E83F534D60F1] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table sh9_procxbill
-- ----------------------------
ALTER TABLE [dbo].[sh9_procxbill] ADD CONSTRAINT [PK__sh9_proc__3213E83F571DF1D5] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table vip_base_info
-- ----------------------------
ALTER TABLE [dbo].[vip_base_info] ADD CONSTRAINT [PK__vip_base__3213E83F71DC994B] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO


-- ----------------------------
-- Primary Key structure for table warehouse_product_stock_info
-- ----------------------------
ALTER TABLE [dbo].[warehouse_product_stock_info] ADD CONSTRAINT [PK__warehous__3213E83F5EBF139D] PRIMARY KEY CLUSTERED ([id])
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON)  
ON [PRIMARY]
GO

