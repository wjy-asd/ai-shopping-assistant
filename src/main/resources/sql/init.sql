-- 创建业务数据库
CREATE DATABASE IF NOT EXISTS `ai_xiaogou` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ai_xiaogou`;

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name`        VARCHAR(100) NOT NULL COMMENT '商品名称',
  `category`    VARCHAR(50) NOT NULL COMMENT '商品分类（运动鞋/数码/服装等）',
  `price`       DECIMAL(10,2) NOT NULL COMMENT '商品价格',
  `stock`       INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  `description` TEXT COMMENT '商品描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 购物车表
CREATE TABLE IF NOT EXISTS `cart` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车记录ID',
  `user_id`    BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `quantity`   INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 物流表
CREATE TABLE IF NOT EXISTS `logistics` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT '物流记录ID',
  `order_no`     VARCHAR(50) NOT NULL COMMENT '订单号',
  `user_id`      BIGINT NOT NULL COMMENT '用户ID',
  `status`       VARCHAR(50) NOT NULL COMMENT '物流状态（已下单/已发货/运输中/派件中/已签收）',
  `location`     VARCHAR(100) COMMENT '当前所在地',
  `updated_at`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流表';

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`     VARCHAR(50) NOT NULL COMMENT '订单号',
  `user_id`      BIGINT NOT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '订单总金额',
  `status`       VARCHAR(20) NOT NULL DEFAULT '待发货' COMMENT '订单状态（待发货/已发货/已完成）',
  `created_at`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS `order_item` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id`     BIGINT NOT NULL COMMENT '订单ID',
  `product_id`   BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称快照',
  `price`        DECIMAL(10,2) NOT NULL COMMENT '下单时单价',
  `quantity`     INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `subtotal`     DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 先清空商品表，再插入测试商品数据，避免重复执行时产生重复数据
DELETE FROM `product`;

-- 插入测试商品数据
INSERT INTO `product` (name, category, price, stock, description) VALUES
('Nike Air Max 运动鞋', '运动鞋', 499.00, 100, '经典气垫设计，适合日常跑步和健身'),
('Redmi Note 13 手机', '数码', 1299.00, 50, '高性价比智能手机，6.67英寸AMOLED屏'),
('北面防水冲锋衣', '服装', 899.00, 30, '防水防风，适合户外运动');

-- 追加更多测试商品数据（共 20 条）
INSERT INTO `product` (name, category, price, stock, description) VALUES
('Adidas Ultraboost 跑步鞋', '运动鞋', 599.00, 80, 'Boost 缓震中底，适合长距离跑步'),
('李宁超轻系列跑鞋', '运动鞋', 399.00, 120, '轻量化设计，透气舒适'),
('安踏氮科技篮球鞋', '运动鞋', 459.00, 90, '氮科技中底，防滑耐磨'),
('新百伦复古休闲鞋', '运动鞋', 549.00, 70, '复古外观，日常百搭'),
('特步竞速跑鞋', '运动鞋', 329.00, 150, '竞速训练，轻便回弹'),
('匹克态极运动鞋', '运动鞋', 389.00, 100, '态极科技，柔软支撑'),
('华为 Mate 60 手机', '数码', 5999.00, 30, '旗舰影像，卫星通信'),
('小米 14 手机', '数码', 3999.00, 40, '徕卡光学镜头，性能旗舰'),
('OPPO Reno 11', '数码', 2799.00, 60, '人像摄影手机，轻薄设计'),
('vivo X100', '数码', 3999.00, 45, '蔡司影像，天玑芯片'),
('荣耀 90', '数码', 2499.00, 55, '2亿像素写真相机'),
('苹果 AirPods Pro', '数码', 1899.00, 80, '主动降噪，空间音频'),
('索尼 WH-1000XM5 耳机', '数码', 2399.00, 35, '头戴式降噪耳机，音质出色'),
('优衣库摇粒绒外套', '服装', 199.00, 200, '柔软保暖，易搭配'),
('李宁运动卫衣', '服装', 259.00, 180, '吸湿排汗，休闲运动'),
('安踏轻薄羽绒服', '服装', 599.00, 100, '轻便保暖，适合秋冬'),
('迪卡侬冲锋衣', '服装', 399.00, 120, '防风防泼水，户外实用'),
('耐克运动长裤', '服装', 329.00, 140, '弹性面料，运动自如'),
('森马牛仔裤', '服装', 189.00, 160, '经典版型，耐磨百搭'),
('太平鸟针织开衫', '服装', 399.00, 90, '柔软亲肤，气质百搭');

-- 插入测试物流数据
INSERT INTO `logistics` (order_no, user_id, status, location) VALUES
('ORD20250101001', 1, '运输中', '广州白云分拨中心'),
('ORD20250101002', 1, '派件中', '广州天河配送站');
