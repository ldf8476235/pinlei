-- mock seed generated from script/data.txt
SET NOCOUNT ON;
BEGIN TRAN;

-- store dimension
DELETE FROM dbo.base_department WHERE store_no IN (N'S001');
INSERT INTO dbo.base_department (store_no,store_name,store_format_no,store_format_name,business_circle_no,business_circle_name,store_group_no,store_group_name,store_type_no,store_type_name,preorgcode,preorgname,status_no,status_name,store_staff_num,store_area,longitude,latitude,start_date,end_date)
VALUES (N'S001',N'STORE-A',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'1',N'STORE',N'ORG01',N'AREA-1',1,N'OPEN',N'80',N'2600',N'121.47',N'31.23','2022-01-01',NULL);

-- class hierarchy
DELETE FROM dbo.base_class;
INSERT INTO dbo.base_class (one_class_no,one_class_name,two_class_no,two_class_name,three_class_no,three_class_name,four_class_no,four_class_name,five_class_no,five_class_name) VALUES
(N'001',N'生鲜日配',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(N'002',N'粮油调味',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(N'003',N'食品杂货',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(N'004',N'洗化部',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(N'005',N'百货部',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(N'006',N'针纺服饰部',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(N'007',N'家电部',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(N'008',N'精品部',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(N'009',N'进口商品',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(N'010',N'优惠卡',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(N'011',N'充值卡',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(N'001',N'生鲜日配',N'00101',N'蔬菜',NULL,NULL,NULL,NULL,NULL,NULL),
(N'001',N'生鲜日配',N'00102',N'水果',NULL,NULL,NULL,NULL,NULL,NULL),
(N'001',N'生鲜日配',N'00103',N'蛋类',NULL,NULL,NULL,NULL,NULL,NULL),
(N'001',N'生鲜日配',N'00104',N'水产',NULL,NULL,NULL,NULL,NULL,NULL),
(N'001',N'生鲜日配',N'00105',N'肉类',NULL,NULL,NULL,NULL,NULL,NULL),
(N'001',N'生鲜日配',N'00106',N'熟食加工',NULL,NULL,NULL,NULL,NULL,NULL),
(N'001',N'生鲜日配',N'00107',N'面食加工',NULL,NULL,NULL,NULL,NULL,NULL),
(N'001',N'生鲜日配',N'00108',N'早餐饮品',NULL,NULL,NULL,NULL,NULL,NULL),
(N'001',N'生鲜日配',N'00109',N'糕点加工',NULL,NULL,NULL,NULL,NULL,NULL),
(N'002',N'粮油调味',N'00201',N'米类',NULL,NULL,NULL,NULL,NULL,NULL),
(N'002',N'粮油调味',N'00202',N'面粉',NULL,NULL,NULL,NULL,NULL,NULL),
(N'002',N'粮油调味',N'00203',N'食用油',NULL,NULL,NULL,NULL,NULL,NULL),
(N'002',N'粮油调味',N'00204',N'调味品',NULL,NULL,NULL,NULL,NULL,NULL),
(N'002',N'粮油调味',N'00205',N'干货',NULL,NULL,NULL,NULL,NULL,NULL),
(N'003',N'食品杂货',N'00301',N'休闲零食',NULL,NULL,NULL,NULL,NULL,NULL),
(N'003',N'食品杂货',N'00302',N'冲调饮品',NULL,NULL,NULL,NULL,NULL,NULL),
(N'003',N'食品杂货',N'00303',N'方便速食',NULL,NULL,NULL,NULL,NULL,NULL),
(N'003',N'食品杂货',N'00304',N'罐头',NULL,NULL,NULL,NULL,NULL,NULL),
(N'003',N'食品杂货',N'00305',N'糖果巧克力',NULL,NULL,NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',NULL,NULL,NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00402',N'清洁用品',NULL,NULL,NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00403',N'卫生用品',NULL,NULL,NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040102',N'护发养发',NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040103',N'美发',NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040104',N'牙膏',NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040105',N'牙刷',NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040106',N'口腔护理附属',NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040107',N'其他个人清洁',NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040108',N'护肤品',NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040109',N'美容用品',NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040110',N'男士护理',NULL,NULL,NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010101',N'去屑洗发水',NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010102',N'柔顺洗发水',NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010103',N'滋养修护洗发水',NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010104',N'去油控油洗发水',NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010105',N'清凉舒爽洗发水',NULL,NULL),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010106',N'防脱生发洗发水',NULL,NULL);

-- suggest sku and role
DELETE FROM dbo.base_class_sku WHERE store_no = 'S001';
INSERT INTO dbo.base_class_sku (store_no,store_name,store_org_no,store_org_name,store_format_no,store_format_name,business_circle_no,business_circle_name,store_group_no,store_group_name,class_no,class_name,class_sku,class_role) VALUES
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'001',N'生鲜日配','7177',N'1'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'002',N'粮油调味','1906',N'4'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'003',N'食品杂货','8075',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'004',N'洗化部','4342',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'005',N'百货部','9647',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'006',N'针纺服饰部','3163',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'007',N'家电部','264',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'008',N'精品部','26',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'009',N'进口商品','546',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'010',N'优惠卡','180',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'011',N'充值卡','1',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00101',N'蔬菜','536',N'1'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00102',N'水果','560',N'1'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00103',N'蛋类','78',N'1'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00104',N'水产','233',N'1'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00105',N'肉类','548',N'1'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00106',N'熟食加工','789',N'1'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00107',N'面食加工','372',N'1'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00108',N'早餐饮品','18',N'1'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00109',N'糕点加工','454',N'1'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00201',N'米类','320',N'4'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00202',N'面粉','180',N'4'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00203',N'食用油','450',N'4'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00204',N'调味品','680',N'4'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00205',N'干货','276',N'4'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00301',N'休闲零食','2800',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00302',N'冲调饮品','1200',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00303',N'方便速食','1500',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00304',N'罐头','350',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00305',N'糖果巧克力','2225',N'3'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00401',N'个人护理','2708',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00402',N'清洁用品','799',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'00403',N'卫生用品','835',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'0040101',N'洗发','530',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'0040102',N'护发养发','140',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'0040103',N'美发','90',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'0040104',N'牙膏','377',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'0040105',N'牙刷','210',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'0040106',N'口腔护理附属','45',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'0040107',N'其他个人清洁','493',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'0040108',N'护肤品','656',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'0040109',N'美容用品','19',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'0040110',N'男士护理','148',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'004010101',N'去屑洗发水','142',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'004010102',N'柔顺洗发水','81',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'004010103',N'滋养修护洗发水','146',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'004010104',N'去油控油洗发水','52',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'004010105',N'清凉舒爽洗发水','14',N'2'),
(N'S001',N'STORE-A',N'ORG01',N'AREA-1',N'1',N'FORMAT-1',N'1',N'CIRCLE-1',N'1',N'GROUP-1',N'004010106',N'防脱生发洗发水','22',N'2');

-- demo products for saleSku calculation
DELETE FROM dbo.base_dept_products WHERE store_no = N'S001';
DECLARE @seed TABLE (one_class_no NVARCHAR(40),one_class_name NVARCHAR(40),two_class_no NVARCHAR(40),two_class_name NVARCHAR(40),three_class_no NVARCHAR(40),three_class_name NVARCHAR(40),four_class_no NVARCHAR(40),four_class_name NVARCHAR(40),five_class_no NVARCHAR(40),five_class_name NVARCHAR(40),gen_cnt INT);
INSERT INTO @seed (one_class_no,one_class_name,two_class_no,two_class_name,three_class_no,three_class_name,four_class_no,four_class_name,five_class_no,five_class_name,gen_cnt) VALUES
(N'005',N'百货部',NULL,NULL,NULL,NULL,NULL,NULL,N'005',N'百货部',80),
(N'006',N'针纺服饰部',NULL,NULL,NULL,NULL,NULL,NULL,N'006',N'针纺服饰部',63),
(N'007',N'家电部',NULL,NULL,NULL,NULL,NULL,NULL,N'007',N'家电部',5),
(N'008',N'精品部',NULL,NULL,NULL,NULL,NULL,NULL,N'008',N'精品部',1),
(N'009',N'进口商品',NULL,NULL,NULL,NULL,NULL,NULL,N'009',N'进口商品',11),
(N'010',N'优惠卡',NULL,NULL,NULL,NULL,NULL,NULL,N'010',N'优惠卡',4),
(N'011',N'充值卡',NULL,NULL,NULL,NULL,NULL,NULL,N'011',N'充值卡',1),
(N'001',N'生鲜日配',N'00101',N'蔬菜',NULL,NULL,NULL,NULL,N'00101',N'蔬菜',11),
(N'001',N'生鲜日配',N'00102',N'水果',NULL,NULL,NULL,NULL,N'00102',N'水果',11),
(N'001',N'生鲜日配',N'00103',N'蛋类',NULL,NULL,NULL,NULL,N'00103',N'蛋类',2),
(N'001',N'生鲜日配',N'00104',N'水产',NULL,NULL,NULL,NULL,N'00104',N'水产',5),
(N'001',N'生鲜日配',N'00105',N'肉类',NULL,NULL,NULL,NULL,N'00105',N'肉类',11),
(N'001',N'生鲜日配',N'00106',N'熟食加工',NULL,NULL,NULL,NULL,N'00106',N'熟食加工',16),
(N'001',N'生鲜日配',N'00107',N'面食加工',NULL,NULL,NULL,NULL,N'00107',N'面食加工',7),
(N'001',N'生鲜日配',N'00108',N'早餐饮品',NULL,NULL,NULL,NULL,N'00108',N'早餐饮品',1),
(N'001',N'生鲜日配',N'00109',N'糕点加工',NULL,NULL,NULL,NULL,N'00109',N'糕点加工',9),
(N'002',N'粮油调味',N'00201',N'米类',NULL,NULL,NULL,NULL,N'00201',N'米类',6),
(N'002',N'粮油调味',N'00202',N'面粉',NULL,NULL,NULL,NULL,N'00202',N'面粉',4),
(N'002',N'粮油调味',N'00203',N'食用油',NULL,NULL,NULL,NULL,N'00203',N'食用油',9),
(N'002',N'粮油调味',N'00204',N'调味品',NULL,NULL,NULL,NULL,N'00204',N'调味品',14),
(N'002',N'粮油调味',N'00205',N'干货',NULL,NULL,NULL,NULL,N'00205',N'干货',6),
(N'003',N'食品杂货',N'00301',N'休闲零食',NULL,NULL,NULL,NULL,N'00301',N'休闲零食',56),
(N'003',N'食品杂货',N'00302',N'冲调饮品',NULL,NULL,NULL,NULL,N'00302',N'冲调饮品',24),
(N'003',N'食品杂货',N'00303',N'方便速食',NULL,NULL,NULL,NULL,N'00303',N'方便速食',30),
(N'003',N'食品杂货',N'00304',N'罐头',NULL,NULL,NULL,NULL,N'00304',N'罐头',7),
(N'003',N'食品杂货',N'00305',N'糖果巧克力',NULL,NULL,NULL,NULL,N'00305',N'糖果巧克力',44),
(N'004',N'洗化部',N'00402',N'清洁用品',NULL,NULL,NULL,NULL,N'00402',N'清洁用品',16),
(N'004',N'洗化部',N'00403',N'卫生用品',NULL,NULL,NULL,NULL,N'00403',N'卫生用品',17),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040102',N'护发养发',NULL,NULL,N'0040102',N'护发养发',3),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040103',N'美发',NULL,NULL,N'0040103',N'美发',2),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040104',N'牙膏',NULL,NULL,N'0040104',N'牙膏',8),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040105',N'牙刷',NULL,NULL,N'0040105',N'牙刷',4),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040106',N'口腔护理附属',NULL,NULL,N'0040106',N'口腔护理附属',1),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040107',N'其他个人清洁',NULL,NULL,N'0040107',N'其他个人清洁',10),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040108',N'护肤品',NULL,NULL,N'0040108',N'护肤品',13),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040109',N'美容用品',NULL,NULL,N'0040109',N'美容用品',1),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040110',N'男士护理',NULL,NULL,N'0040110',N'男士护理',3),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010101',N'去屑洗发水',N'004010101',N'去屑洗发水',3),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010102',N'柔顺洗发水',N'004010102',N'柔顺洗发水',2),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010103',N'滋养修护洗发水',N'004010103',N'滋养修护洗发水',3),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010104',N'去油控油洗发水',N'004010104',N'去油控油洗发水',1),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010105',N'清凉舒爽洗发水',N'004010105',N'清凉舒爽洗发水',1),
(N'004',N'洗化部',N'00401',N'个人护理',N'0040101',N'洗发',N'004010106',N'防脱生发洗发水',N'004010106',N'防脱生发洗发水',1);
WITH N AS (SELECT TOP (200) ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS n FROM sys.all_objects)
INSERT INTO dbo.base_dept_products (store_no,store_name,one_class_no,one_class_name,two_class_no,two_class_name,three_class_no,three_class_name,four_class_no,four_class_name,five_class_no,five_class_name,div_no,div_name,subdiv_no,subdiv_name,bclass_no,bclass_name,mclass_no,mclass_name,sclass_no,sclass_name,product_no,product_barcode,product_name,product_taxin_inprice,product_saleprice,status_no,status_name,sale_type_no,sale_type,updatetime)
SELECT N'S001',N'STORE-A',s.one_class_no,s.one_class_name,s.two_class_no,s.two_class_name,s.three_class_no,s.three_class_name,s.four_class_no,s.four_class_name,s.five_class_no,s.five_class_name,N'D01',N'DEP-1',N'D02',N'DEP-2',N'D03',N'DEP-3',N'D04',N'DEP-4',N'D05',N'DEP-5',CONCAT('P',s.five_class_no,RIGHT(CONCAT('000',CAST(n.n AS VARCHAR(3))),3)),CONCAT('B',s.five_class_no,RIGHT(CONCAT('000',CAST(n.n AS VARCHAR(3))),3)),CONCAT(s.five_class_name,N'-',n.n),CAST(5 + (n.n % 30) AS DECIMAL(12,2)),CAST(8 + (n.n % 30) AS DECIMAL(12,2)),1,N'NORMAL',1,N'SELF',GETDATE() FROM @seed s JOIN N ON N.n <= s.gen_cnt;

-- quick check
SELECT COUNT(1) AS base_class_cnt FROM dbo.base_class;
SELECT COUNT(1) AS base_class_sku_cnt FROM dbo.base_class_sku WHERE store_no='S001';
SELECT COUNT(1) AS base_dept_products_cnt FROM dbo.base_dept_products WHERE store_no='S001';

COMMIT TRAN;
