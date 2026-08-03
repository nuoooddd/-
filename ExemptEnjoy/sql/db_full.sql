-- 免申即享 完整数据库脚本
-- 导出时间: 2026/6/26 18:54:46
-- 数据库: 税务平台

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

CREATE DATABASE IF NOT EXISTS `税务平台` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `税务平台`;

DROP TABLE IF EXISTS `ee_audit_log`;
CREATE TABLE `ee_audit_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作用户名',
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作类型',
  `module` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '业务模块',
  `target_id` bigint DEFAULT NULL COMMENT '操作目标ID',
  `detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作详情',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';

INSERT INTO `ee_audit_log` (`log_id`,`user_id`,`user_name`,`operation`,`module`,`target_id`,`detail`,`ip`,`create_time`) VALUES
('67','1','admin','精准推送','匹配兑现','408','推送匹配记录 ID: 408','0:0:0:0:0:0:0:1','2026-06-26 12:58:58'),
('68','1','admin','精准推送','匹配兑现','407','推送匹配记录 ID: 407','0:0:0:0:0:0:0:1','2026-06-26 12:58:59'),
('69','1','admin','兑付执行','匹配兑现','405','执行兑付，记录 ID: 405','0:0:0:0:0:0:0:1','2026-06-26 12:59:05');

DROP TABLE IF EXISTS `ee_audit_record`;
CREATE TABLE `ee_audit_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '????ID',
  `user_id` bigint NOT NULL COMMENT '??ID',
  `audit_type` varchar(20) NOT NULL COMMENT '????(idcard??? enterprise??)',
  `audit_status` char(1) NOT NULL COMMENT '????(0??? 1?? 2??)',
  `audit_by` bigint DEFAULT NULL COMMENT '???ID',
  `audit_by_name` varchar(64) DEFAULT NULL COMMENT '?????',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '????',
  `id_card_front` varchar(500) DEFAULT NULL COMMENT '??????',
  `id_card_back` varchar(500) DEFAULT NULL COMMENT '??????',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `audit_time` datetime DEFAULT NULL COMMENT '????',
  PRIMARY KEY (`record_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='???????';

INSERT INTO `ee_audit_record` (`record_id`,`user_id`,`audit_type`,`audit_status`,`audit_by`,`audit_by_name`,`audit_remark`,`id_card_front`,`id_card_back`,`create_time`,`audit_time`) VALUES
('1','105','idcard','2','1','admin','身份证未上传',NULL,NULL,'2026-06-21 11:44:30','2026-06-22 00:43:23'),
('2','107','idcard','1','1','admin',NULL,'/profile/upload/2026/06/25/微信图片_20260625000811_110_10_20260625000837A001.jpg','/profile/upload/2026/06/25/微信图片_20260625000815_111_10_20260625000841A002.jpg','2026-06-25 00:09:04','2026-06-25 00:10:05');

DROP TABLE IF EXISTS `ee_fund`;
CREATE TABLE `ee_fund` (
  `fund_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资金池ID',
  `policy_id` bigint NOT NULL COMMENT '关联政策ID',
  `total_budget` decimal(12,2) DEFAULT NULL COMMENT '总预算金额',
  `used_amount` decimal(12,2) DEFAULT '0.00' COMMENT '已拨付金额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`fund_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资金管理表';

INSERT INTO `ee_fund` (`fund_id`,`policy_id`,`total_budget`,`used_amount`,`create_time`,`update_time`) VALUES
('10','1','5000000.00','500000.00','2026-06-26 10:45:49','2026-06-26 11:21:38'),
('11','2','2000000.00','0.00','2026-06-26 10:45:49',NULL),
('12','3','3000000.00','0.00','2026-06-26 10:45:49',NULL),
('13','4','8000000.00','0.00','2026-06-26 10:45:49',NULL),
('14','5','1500000.00','0.00','2026-06-26 10:45:49',NULL),
('15','6','2500000.00','0.00','2026-06-26 10:45:49',NULL),
('16','7','1000000.00','0.00','2026-06-26 10:45:49',NULL),
('17','8','500000.00','0.00','2026-06-26 10:45:49',NULL),
('18','9','300000.00','0.00','2026-06-26 10:45:49',NULL),
('19','10','500000.00','100000.00',NULL,'2026-06-26 12:59:06'),
('20','11','400000.00','0.00',NULL,NULL);

DROP TABLE IF EXISTS `ee_match_record`;
CREATE TABLE `ee_match_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `policy_id` bigint NOT NULL COMMENT '政策ID',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `match_time` datetime DEFAULT NULL COMMENT '自动比对时间',
  `status` varchar(20) DEFAULT 'MATCHED' COMMENT '流程状态(MATCHED:已匹配, PUSHED:已推送, CONFIRMED:意愿已确认, FULFILLED:自动已兑现, ARCHIVED:已公示归档)',
  `fund_amount` decimal(10,2) DEFAULT NULL COMMENT '拟兑现金额',
  `audit_status` char(1) DEFAULT '0' COMMENT '审核状态(0免审 1人工审核中 2审核通过 3审核拒绝)',
  `risk_level` char(1) DEFAULT '0' COMMENT '风控等级(0低风险 1中风险 2高风险)',
  `proof_file` varchar(500) DEFAULT NULL COMMENT '佐证材料PDF路径',
  `create_time` datetime DEFAULT NULL COMMENT '记录创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB AUTO_INCREMENT=409 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='自动匹配及兑现流程表';

INSERT INTO `ee_match_record` (`record_id`,`policy_id`,`target_id`,`match_time`,`status`,`fund_amount`,`audit_status`,`risk_level`,`proof_file`,`create_time`,`update_time`) VALUES
('372','1','4','2026-06-26 12:13:26','MATCHED','500000.00','0','0',NULL,NULL,NULL),
('373','2','4','2026-06-26 12:13:26','MATCHED','200000.00','0','0',NULL,NULL,NULL),
('374','3','4','2026-06-26 12:13:26','MATCHED','300000.00','0','0',NULL,NULL,NULL),
('375','4','4','2026-06-26 12:13:26','MATCHED','800000.00','0','0',NULL,NULL,NULL),
('376','6','4','2026-06-26 12:13:26','MATCHED','250000.00','0','0',NULL,NULL,NULL),
('377','7','6','2026-06-26 12:13:26','MATCHED','100000.00','0','1',NULL,NULL,NULL),
('378','8','6','2026-06-26 12:13:26','MATCHED','50000.00','0','1',NULL,NULL,NULL),
('379','10','6','2026-06-26 12:13:26','MATCHED','100000.00','0','1',NULL,NULL,NULL),
('380','1','10','2026-06-26 12:13:26','MATCHED','500000.00','0','0',NULL,NULL,NULL),
('381','4','10','2026-06-26 12:13:26','MATCHED','800000.00','0','0',NULL,NULL,NULL),
('382','6','10','2026-06-26 12:13:26','MATCHED','250000.00','0','0',NULL,NULL,NULL),
('383','1','11','2026-06-26 12:13:26','MATCHED','500000.00','0','0',NULL,NULL,NULL),
('384','3','11','2026-06-26 12:13:26','MATCHED','300000.00','0','0',NULL,NULL,NULL),
('385','4','11','2026-06-26 12:13:26','MATCHED','800000.00','0','0',NULL,NULL,NULL),
('386','6','11','2026-06-26 12:13:26','MATCHED','250000.00','0','0',NULL,NULL,NULL),
('387','1','12','2026-06-26 12:13:26','MATCHED','500000.00','0','0',NULL,NULL,NULL),
('388','3','12','2026-06-26 12:13:26','MATCHED','300000.00','0','0',NULL,NULL,NULL),
('389','4','12','2026-06-26 12:13:26','MATCHED','800000.00','0','0',NULL,NULL,NULL),
('390','6','12','2026-06-26 12:13:26','MATCHED','250000.00','0','0',NULL,NULL,NULL),
('391','1','13','2026-06-26 12:13:26','MATCHED','500000.00','0','1',NULL,NULL,NULL),
('392','2','13','2026-06-26 12:13:26','MATCHED','200000.00','0','1',NULL,NULL,NULL),
('393','4','13','2026-06-26 12:13:26','MATCHED','800000.00','0','1',NULL,NULL,NULL),
('394','6','13','2026-06-26 12:13:26','MATCHED','250000.00','0','1',NULL,NULL,NULL),
('395','10','13','2026-06-26 12:13:26','MATCHED','100000.00','0','1',NULL,NULL,NULL),
('396','3','14','2026-06-26 12:13:26','MATCHED','300000.00','1','2',NULL,NULL,NULL),
('397','10','14','2026-06-26 12:13:26','PUSHED','100000.00','1','2',NULL,NULL,'2026-06-26 12:13:35'),
('398','7','15','2026-06-26 12:13:26','MATCHED','100000.00','0','0',NULL,NULL,NULL),
('399','8','15','2026-06-26 12:13:26','MATCHED','50000.00','0','0',NULL,NULL,NULL),
('400','8','16','2026-06-26 12:13:26','MATCHED','50000.00','0','0',NULL,NULL,NULL),
('401','5','17','2026-06-26 12:13:26','MATCHED','150000.00','1','2',NULL,NULL,NULL),
('402','9','17','2026-06-26 12:13:26','MATCHED','30000.00','1','2',NULL,NULL,NULL),
('405','10','18','2026-06-26 12:13:26','FULFILLED','100000.00','3','1',NULL,NULL,'2026-06-26 12:59:06'),
('406','8','19','2026-06-26 12:13:26','MATCHED','50000.00','0','0',NULL,NULL,NULL),
('407','7','18','2026-06-26 12:22:53','PUSHED','100000.00','0','1',NULL,NULL,'2026-06-26 12:59:00'),
('408','8','18','2026-06-26 12:22:53','PUSHED','50000.00','0','1',NULL,NULL,'2026-06-26 12:58:59');

DROP TABLE IF EXISTS `ee_message`;
CREATE TABLE `ee_message` (
  `message_id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `sender_id` bigint DEFAULT NULL COMMENT '发送者ID',
  `sender_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发送者名称',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息标题',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息内容',
  `msg_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息类型(push/fulfill/system)',
  `is_read` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '是否已读(0未读 1已读)',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `related_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `related_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联业务类型(matchRecord/policy)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`message_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

INSERT INTO `ee_message` (`message_id`,`user_id`,`sender_id`,`sender_name`,`title`,`content`,`msg_type`,`is_read`,`read_time`,`related_id`,`related_type`,`create_time`) VALUES
('1','1',NULL,NULL,'企业端申请推送','AAA制造有限公司 申请了政策「中小企业稳岗奖励」，请及时处理推送。','APPLY','0','2026-06-20 01:45:53','2','MATCH_RECORD','2026-06-19 19:39:10'),
('2','1',NULL,NULL,'企业端申请推送','AAA制造有限公司 申请了政策「高新技术企业研发补贴」，请及时处理推送。','APPLY','1','2026-06-20 01:45:52','1','MATCH_RECORD','2026-06-19 19:39:12'),
('3','102',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【中小企业稳岗奖励】，拟兑现金额：200000.00元，请及时确认意愿。','push','1','2026-06-19 19:42:19','2','matchRecord','2026-06-19 19:41:44'),
('4','102',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【高新技术企业研发补贴】，拟兑现金额：500000.00元，请及时确认意愿。','push','1','2026-06-19 19:42:19','1','matchRecord','2026-06-19 19:41:45'),
('5','102',NULL,NULL,'资金兑付通知','您享受的政策【中小企业稳岗奖励】已自动兑付，金额：200000.00元已直达您的银行账户。','fulfill','0',NULL,'2','matchRecord','2026-06-19 19:42:34'),
('6','102',NULL,NULL,'资金兑付通知','您享受的政策【高新技术企业研发补贴】已自动兑付，金额：500000.00元已直达您的银行账户。','fulfill','0',NULL,'1','matchRecord','2026-06-19 19:42:38'),
('7','1',NULL,NULL,'企业端申请推送','qiye 申请了政策「低收入群体生活补贴」，请及时处理推送。','APPLY','1','2026-06-20 01:45:51','36','MATCH_RECORD','2026-06-19 23:33:38'),
('8','1',NULL,NULL,'企业端申请推送','qiye 申请了政策「中小企业稳岗奖励」，请及时处理推送。','APPLY','1','2026-06-19 23:36:37','35','MATCH_RECORD','2026-06-19 23:33:39'),
('9','1',NULL,NULL,'企业端申请推送','qiye 申请了政策「高新技术企业研发补贴」，请及时处理推送。','APPLY','1','2026-06-19 23:36:35','34','MATCH_RECORD','2026-06-19 23:33:41'),
('10','2',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【低收入群体生活补贴】，拟兑现金额：50000.00元，请及时确认意愿。','push','1','2026-06-21 11:29:42','118','matchRecord','2026-06-20 00:27:09'),
('11','2',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【吸纳残疾人就业奖励】，拟兑现金额：150000.00元，请及时确认意愿。','push','1','2026-06-21 11:29:42','117','matchRecord','2026-06-20 00:27:09'),
('12','102',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【低收入群体生活补贴】，拟兑现金额：50000.00元，请及时确认意愿。','push','0',NULL,'116','matchRecord','2026-06-20 00:27:10'),
('13','102',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【吸纳残疾人就业奖励】，拟兑现金额：150000.00元，请及时确认意愿。','push','0',NULL,'115','matchRecord','2026-06-20 00:27:10'),
('14','101',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【低收入群体生活补贴】，拟兑现金额：50000.00元，请及时确认意愿。','push','0',NULL,'114','matchRecord','2026-06-20 00:27:11'),
('15','101',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【科技创新成果转化补贴】，拟兑现金额：800000.00元，请及时确认意愿。','push','0',NULL,'113','matchRecord','2026-06-20 00:27:14'),
('16','101',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【中小企业稳岗奖励】，拟兑现金额：200000.00元，请及时确认意愿。','push','0',NULL,'112','matchRecord','2026-06-20 00:27:15'),
('17','101',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【高新技术企业研发补贴】，拟兑现金额：500000.00元，请及时确认意愿。','push','0',NULL,'111','matchRecord','2026-06-20 00:27:16'),
('18','100',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【低收入群体生活补贴】，拟兑现金额：50000.00元，请及时确认意愿。','push','0',NULL,'110','matchRecord','2026-06-20 00:27:16'),
('19','100',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【吸纳残疾人就业奖励】，拟兑现金额：150000.00元，请及时确认意愿。','push','0',NULL,'109','matchRecord','2026-06-20 00:27:17'),
('20','100',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【科技创新成果转化补贴】，拟兑现金额：800000.00元，请及时确认意愿。','push','0',NULL,'121','matchRecord','2026-06-20 00:27:21'),
('21','100',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【中小企业稳岗奖励】，拟兑现金额：200000.00元，请及时确认意愿。','push','0',NULL,'120','matchRecord','2026-06-20 00:27:22'),
('22','100',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【高新技术企业研发补贴】，拟兑现金额：500000.00元，请及时确认意愿。','push','0',NULL,'119','matchRecord','2026-06-20 00:27:23'),
('23','1',NULL,NULL,'企业端申请推送','qiye 申请了政策「低收入群体生活补贴」，请及时处理推送。','APPLY','1','2026-06-20 01:45:46','166','MATCH_RECORD','2026-06-20 00:29:49'),
('24','103',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【低收入群体生活补贴】，拟兑现金额：50000.00元，请及时确认意愿。','push','1','2026-06-20 00:31:33','166','matchRecord','2026-06-20 00:30:20'),
('25','103',NULL,NULL,'资金兑付通知','您享受的政策【低收入群体生活补贴】已自动兑付，金额：50000.00元已直达您的银行账户。','fulfill','1','2026-06-20 00:31:33','166','matchRecord','2026-06-20 00:31:19'),
('26','1',NULL,NULL,'企业端申请推送','geren 申请了政策「吸纳残疾人就业奖励」，请及时处理推送。','APPLY','1','2026-06-26 12:46:30','167','MATCH_RECORD','2026-06-20 10:18:11'),
('27','1',NULL,NULL,'企业端申请推送','geren 申请了政策「低收入群体生活补贴」，请及时处理推送。','APPLY','1','2026-06-26 12:46:30','168','MATCH_RECORD','2026-06-20 10:18:12'),
('28','1',NULL,NULL,'企业端申请推送','张三 申请了政策「低收入群体生活补贴」，请及时处理推送。','APPLY','0','2026-06-26 12:46:30','165','MATCH_RECORD','2026-06-24 00:56:34'),
('29','2',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【低收入群体生活补贴】，拟兑现金额：50000.00元，请及时确认意愿。','push','1','2026-06-25 12:39:13','165','matchRecord','2026-06-25 10:48:02'),
('30','1',NULL,NULL,'企业端申请推送','geren 申请了政策「低收入群体生活补贴」，请及时处理推送。','APPLY','1','2026-06-26 12:46:30','170','MATCH_RECORD','2026-06-25 11:16:19'),
('31','101',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【低收入群体生活补贴】，拟兑现金额：50000.00元，请及时确认意愿。','push','0',NULL,'203','matchRecord','2026-06-26 10:17:57'),
('32','117',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【低收入群体生活补贴】，拟兑现金额：50000.00元，请及时确认意愿。','push','0',NULL,'240','matchRecord','2026-06-26 10:47:19'),
('33','117',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【低收入群体生活补贴】，拟兑现金额：50000.00元，请及时确认意愿。','push','0',NULL,'272','matchRecord','2026-06-26 11:20:55'),
('34','2',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【高新技术企业研发补贴】，拟兑现金额：500000.00元，请及时确认意愿。','push','0',NULL,'241','matchRecord','2026-06-26 11:21:19'),
('35','2',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【中小企业稳岗奖励】，拟兑现金额：200000.00元，请及时确认意愿。','push','0',NULL,'242','matchRecord','2026-06-26 11:21:19'),
('36','2',NULL,NULL,'资金兑付通知','您享受的政策【高新技术企业研发补贴】已自动兑付，金额：500000.00元已直达您的银行账户。','fulfill','0',NULL,'241','matchRecord','2026-06-26 11:21:37'),
('37','112',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【信用修复企业扶持资金】，拟兑现金额：100000.00元，请及时确认意愿。','push','0',NULL,'397','matchRecord','2026-06-26 12:13:35'),
('38','116',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【信用修复企业扶持资金】，拟兑现金额：100000.00元，请及时确认意愿。','push','0',NULL,'405','matchRecord','2026-06-26 12:13:54'),
('39','108','1','admin','111','收到了吗','mail','0',NULL,NULL,NULL,'2026-06-26 12:54:21'),
('40','116',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【低收入群体生活补贴】，拟兑现金额：50000.00元，请及时确认意愿。','push','0',NULL,'408','matchRecord','2026-06-26 12:58:58'),
('41','116',NULL,NULL,'政策推送通知','您有一条新的政策匹配：【青年就业见习补贴】，拟兑现金额：100000.00元，请及时确认意愿。','push','0',NULL,'407','matchRecord','2026-06-26 12:58:59'),
('42','116',NULL,NULL,'资金兑付通知','您享受的政策【信用修复企业扶持资金】已自动兑付，金额：100000.00元已直达您的银行账户。','fulfill','0',NULL,'405','matchRecord','2026-06-26 12:59:05');

DROP TABLE IF EXISTS `ee_policy`;
CREATE TABLE `ee_policy` (
  `policy_id` bigint NOT NULL AUTO_INCREMENT COMMENT '政策ID',
  `policy_name` varchar(100) NOT NULL COMMENT '政策名称',
  `policy_type` varchar(20) DEFAULT NULL COMMENT '政策类型(1补贴 2奖励 3减免)',
  `amount` decimal(15,2) DEFAULT NULL COMMENT '发放金额',
  `publish_dept` varchar(50) DEFAULT NULL COMMENT '发布部门',
  `status` char(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `start_date` date DEFAULT NULL COMMENT '生效日期',
  `end_date` date DEFAULT NULL COMMENT '到期日期',
  `pdf_url` varchar(500) DEFAULT NULL COMMENT '政策文件路径',
  `pdf_name` varchar(200) DEFAULT NULL COMMENT '政策文件名称',
  PRIMARY KEY (`policy_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='政策管理表';

INSERT INTO `ee_policy` (`policy_id`,`policy_name`,`policy_type`,`amount`,`publish_dept`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`,`start_date`,`end_date`,`pdf_url`,`pdf_name`) VALUES
('1','高新技术企业研发补贴','1','500000.00','科技局','0','admin','2026-06-26 10:45:49',NULL,NULL,NULL,'2026-01-01','2026-12-31',NULL,NULL),
('2','中小企业稳岗奖励','2','200000.00','人社局','0','admin','2026-06-26 10:45:49',NULL,NULL,NULL,'2026-01-01','2026-12-31',NULL,NULL),
('3','绿色制造税收减免','3','300000.00','税务局','0','admin','2026-06-26 10:45:49',NULL,NULL,NULL,'2026-01-01','2026-12-31',NULL,NULL),
('4','科技创新成果转化补贴','1','800000.00','科技局','0','admin','2026-06-26 10:45:49',NULL,NULL,NULL,'2026-01-01','2026-12-31',NULL,NULL),
('5','吸纳残疾人就业奖励','2','150000.00','人社局','0','admin','2026-06-26 10:45:49',NULL,NULL,NULL,'2026-01-01','2026-12-31',NULL,NULL),
('6','外贸出口信用保险补贴','1','250000.00','商务局','0','admin','2026-06-26 10:45:49',NULL,NULL,NULL,'2026-01-01','2026-12-31',NULL,NULL),
('7','青年就业见习补贴','2','100000.00','人社局','0','admin','2026-06-26 10:45:49',NULL,NULL,NULL,'2026-01-01','2026-12-31',NULL,NULL),
('8','低收入群体生活补贴','1','50000.00','民政局','0','admin','2026-06-26 10:45:49',NULL,NULL,NULL,'2026-01-01','2026-12-31',NULL,NULL),
('9','残疾人康复补助','1','30000.00','民政局','0','admin','2026-06-26 10:45:49',NULL,NULL,NULL,'2026-01-01','2026-12-31',NULL,NULL),
('10','信用修复企业扶持资金','1','100000.00','市税务局','0',NULL,NULL,NULL,NULL,'针对信用评分较低但有改善意愿的企业，需人工审核',NULL,NULL,NULL,NULL),
('11','中小微企业纾困补贴','2','80000.00','市工信局','0',NULL,NULL,NULL,NULL,'面向经营困难中小微企业，需严格风控审核',NULL,NULL,NULL,NULL);

DROP TABLE IF EXISTS `ee_policy_content`;
CREATE TABLE `ee_policy_content` (
  `content_id` bigint NOT NULL AUTO_INCREMENT,
  `policy_id` bigint NOT NULL,
  `content_text` mediumtext,
  `keywords` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`content_id`),
  UNIQUE KEY `uk_policy_id` (`policy_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='政策文本内容';

INSERT INTO `ee_policy_content` (`content_id`,`policy_id`,`content_text`,`keywords`,`create_time`,`update_time`) VALUES
('1','1','政策名称：高新技术企业研发补贴\政策类型：补贴\补贴金额：500000.00元\发文单位：科技局\开始日期：Wed Jan 01 00:00:00 CST 2025\结束日期：Thu Dec 31 00:00:00 CST 2026\面向高新技术企业的研发投入补贴','00,CST,Thu,01,00元,Dec,政策类型,科技局,政策名称,开始日期,高新技术企业研发补贴,发文单位,500000,面向高新技术企业的研发投入补贴,结束日期,2026,2025,Jan,Wed,31,补贴,补贴金额','2026-06-21 14:15:44','2026-06-26 10:16:55'),
('2','2','政策名称：中小企业稳岗奖励\政策类型：奖励\补贴金额：200000.00元\发文单位：人社局\开始日期：Sat Mar 01 00:00:00 CST 2025\结束日期：Tue Jun 30 00:00:00 CST 2026\对稳定就业岗位的中小企业给予一次性奖励','00,CST,01,Jun,00元,政策类型,Tue,政策名称,Sat,开始日期,人社局,对稳定就业岗位的中小企业给予一次性奖励,发文单位,奖励,结束日期,200000,2026,2025,中小企业稳岗奖励,30,Mar,补贴金额','2026-06-21 14:15:44','2026-06-25 12:10:30'),
('3','3','政策名称：绿色制造税收减免\政策类型：减免\补贴金额：300000.00元\发文单位：税务局\开始日期：Wed Jan 01 00:00:00 CST 2025\结束日期：Mon Jul 12 00:00:00 CST 2027\通过绿色认证的制造企业享受增值税优惠','00,CST,Jul,12,01,00元,政策类型,政策名称,绿色制造税收减免,开始日期,Mon,发文单位,结束日期,2027,减免,通过绿色认证的制造企业享受增值税优惠,2025,Jan,Wed,税务局,300000,补贴金额','2026-06-21 14:15:44','2026-06-26 10:21:22'),
('4','4','一、申请条件\1. 企业拥有自主知识产权的核心技术\2. 科技成果已在本地实现产业化应用\3. 成果转化项目年新增产值不低于500万元\4. 企业研发投入持续增长\\二、补贴标准\按成果转化项目实际投入的20%给予补贴，单个项目补贴上限80万元。\\三、办理流程\1. 企业在线申报 2. 科技局形式审查 3. 专家现场评审 4. 公示 5. 资金拨付\\四、申报材料\知识产权证书、成果转化合同/协议、产业化投入审计报告、新增产值证明、研发投入增长证明','科技创新成果转化补贴,补贴,科技局,补贴,奖励,减免,政策,申请,条件,标准,流程,材料','2026-06-21 14:15:44',NULL),
('5','5','一、申请条件\1. 企业依法与残疾人职工签订1年以上劳动合同\2. 为残疾人职工缴纳社会保险\3. 残疾人职工实际在岗工作，工资不低于当地最低工资标准\4. 企业安排残疾人就业人数占职工总数比例达标\\二、补贴标准\每安排1名残疾人就业，给予企业3,000元/月岗位补贴，年度奖励上限15万元。\\三、办理流程\1. 系统自动比对残疾人就业数据 2. 残联核验在岗情况 3. 公示 4. 资金直达企业账户\\四、申报材料\残疾人职工身份证及残疾证、劳动合同、社保缴纳证明、工资发放凭证','吸纳残疾人就业奖励,奖励,残疾人联合会,补贴,奖励,减免,政策,申请,条件,标准,流程,材料','2026-06-21 14:15:44',NULL),
('6','6','一、申请条件\1. 企业在本地注册且具有进出口经营资格\2. 已投保短期出口信用保险\3. 上年度出口额不低于100万美元\4. 企业信用记录良好\\二、补贴标准\按企业实际缴纳出口信用保险保费的30%给予补贴，年度补贴上限25万元。\\三、办理流程\1. 企业在线申报 2. 商务局审核 3. 公示 4. 资金拨付\\四、申报材料\营业执照副本、进出口经营资格证明、出口信用保险合同及保费发票、上年度出口额证明','外贸出口信用保险补贴,补贴,商务局,补贴,奖励,减免,政策,申请,条件,标准,流程,材料','2026-06-21 14:15:44',NULL),
('7','7','政策名称：青年就业见习补贴\政策类型：奖励\补贴金额：100000.00元\发文单位：人社局\开始日期：Wed Jan 01 00:00:00 CST 2025\结束日期：Thu Dec 31 00:00:00 CST 2026\企业提供青年就业见习岗位按每人每月2000元补贴','00,CST,Thu,01,00元,Dec,政策类型,政策名称,100000,开始日期,企业提供青年就业见习岗位按每人每月2000元补贴,青年就业见习补贴,人社局,发文单位,奖励,结束日期,2026,2025,Jan,Wed,31,补贴金额','2026-06-21 14:15:44','2026-06-26 10:21:29'),
('8','8','一、申请条件\1. 申请人须为本地户籍居民\2. 家庭人均收入低于当地最低生活保障标准的1.5倍\3. 家庭财产状况符合当地低收入家庭认定标准\4. 无其他稳定收入来源\\二、补贴标准\按当地最低生活保障标准按月发放生活补贴，每人每月最高不超过当地低保标准的120%。\\三、办理流程\1. 系统自动比对低收入人群数据 2. 民政局核验资格 3. 公示 4. 资金直达个人账户\\四、申报材料\身份证、户口簿、收入证明、财产申报表、银行账户信息','低收入群体生活补贴,补贴,民政局,补贴,奖励,减免,政策,申请,条件,标准,流程,材料','2026-06-21 14:15:44',NULL),
('9','9','一、申请条件\1. 申请人须持有有效残疾人证\2. 有康复训练或辅助器具适配需求\3. 在定点康复机构接受康复服务\4. 康复方案经专业评估确认\\二、补贴标准\康复训练费用按实际发生额的80%给予补助，年度补助上限3万元；辅助器具按购置价的70%补助。\\三、办理流程\1. 系统自动匹配残疾人康复需求 2. 残联核验康复方案 3. 公示 4. 资金直达个人账户\\四、申报材料\残疾人证、康复评估报告、定点康复机构服务协议、费用清单及发票','残疾人康复补助,补贴,残疾人联合会,补贴,奖励,减免,政策,申请,条件,标准,流程,材料','2026-06-21 14:15:44',NULL);

DROP TABLE IF EXISTS `ee_rule`;
CREATE TABLE `ee_rule` (
  `rule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `policy_id` bigint NOT NULL COMMENT '关联政策ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `condition_expr` varchar(500) NOT NULL COMMENT '条件表达式(如: age>60 or revenue>1000000)',
  `pdf_url` varchar(500) DEFAULT NULL COMMENT 'PDF附件URL',
  `pdf_name` varchar(200) DEFAULT NULL COMMENT 'PDF附件名称',
  `status` char(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='规则管理表';

INSERT INTO `ee_rule` (`rule_id`,`policy_id`,`rule_name`,`condition_expr`,`pdf_url`,`pdf_name`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
('1','1','高企认定规则','high_tech == true',NULL,NULL,'0','admin','2026-06-26 10:45:49',NULL,NULL,NULL),
('2','2','中小企业规模规则','scale != "large"',NULL,NULL,'0','admin','2026-06-26 10:45:49',NULL,NULL,NULL),
('3','3','绿色认证规则','green_certified == true',NULL,NULL,'0','admin','2026-06-26 10:45:49',NULL,NULL,NULL),
('4','4','高企+研发投入','high_tech == true and research_spend >= 1000000',NULL,NULL,'0','admin','2026-06-26 10:45:49',NULL,NULL,NULL),
('5','5','残疾人雇佣规则','disabled == true',NULL,NULL,'0','admin','2026-06-26 10:45:49',NULL,NULL,NULL),
('6','6','营收门槛+增长率','revenue >= 5000000 and growth_rate >= 10',NULL,NULL,'0','admin','2026-06-26 10:45:49',NULL,NULL,NULL),
('7','7','青年就业规则','age <= 35 and education == "bachelor"',NULL,NULL,'0','admin','2026-06-26 10:45:49',NULL,NULL,NULL),
('8','8','低收入群体规则','income <= 200000 and credit_score >= 60',NULL,NULL,'0','admin','2026-06-26 10:45:49',NULL,NULL,NULL),
('9','9','残疾人认定规则','disabled == true',NULL,NULL,'0','admin','2026-06-26 10:45:49',NULL,NULL,NULL),
('10','10','信用修复规则','credit_score >= 60 and credit_score < 80',NULL,NULL,'0',NULL,NULL,NULL,NULL,NULL),
('11','11','纾困补贴规则','revenue < 5000000 and tax_status == "abnormal"',NULL,NULL,'0',NULL,NULL,NULL,NULL,NULL);

DROP TABLE IF EXISTS `ee_target_data`;
CREATE TABLE `ee_target_data` (
  `target_id` bigint NOT NULL AUTO_INCREMENT COMMENT '目标ID',
  `target_name` varchar(100) NOT NULL COMMENT '企业或个人名称',
  `target_type` char(1) NOT NULL COMMENT '类型(1企业 2个人)',
  `identifier` varchar(50) NOT NULL COMMENT '统一社会信用代码/身份证号',
  `attributes` json DEFAULT NULL COMMENT '画像属性JSON格式',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `bank_account` varchar(50) DEFAULT NULL COMMENT '银行账号',
  `status` char(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `user_id` bigint DEFAULT NULL COMMENT '关联系统用户ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '导入者',
  `create_time` datetime DEFAULT NULL COMMENT '导入时间',
  PRIMARY KEY (`target_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='基础数据表';

INSERT INTO `ee_target_data` (`target_id`,`target_name`,`target_type`,`identifier`,`attributes`,`contact_phone`,`bank_account`,`status`,`user_id`,`create_by`,`create_time`) VALUES
('4','企业用户','2','91440300MA5EXAMPLE','{"scale": "medium", "revenue": 8000000, "industry": "High-tech", "high_tech": true, "tax_status": "normal", "growth_rate": 12, "credit_score": 85, "research_spend": 2000000, "green_certified": true, "registered_capital": 20000000}','13900000001','6222029876543210','0','2','admin','2026-06-19 22:10:58'),
('6','个人用户','2','110101199901011234','{"age": 28, "gender": "male", "income": 80000, "disabled": false, "education": "bachelor", "tax_status": "normal", "credit_score": 75}','18293847566',NULL,'0','104',NULL,NULL),
('10','华为技术有限公司','1','91440300715287930X','{"scale": "large", "revenue": 6000000000, "industry": "High-tech", "high_tech": true, "tax_status": "normal", "growth_rate": 15, "credit_score": 95, "research_spend": 1500000000, "green_certified": false, "registered_capital": 4000000000}','13800001001','6222021234567890001','0','108','admin','2026-06-26 10:39:02'),
('11','腾讯科技有限公司','1','91440300708461136T','{"scale": "large", "revenue": 5000000000, "industry": "High-tech", "high_tech": true, "tax_status": "normal", "growth_rate": 12, "credit_score": 92, "research_spend": 1200000000, "green_certified": true, "registered_capital": 6500000000}','13800001002','6222021234567890002','0','109','admin','2026-06-26 10:39:02'),
('12','比亚迪汽车有限公司','1','91440300708461137A','{"scale": "large", "revenue": 8000000000, "industry": "Manufacturing", "high_tech": true, "tax_status": "normal", "growth_rate": 25, "credit_score": 88, "research_spend": 2000000000, "green_certified": true, "registered_capital": 5000000000}','13800001003','6222021234567890003','0','110','admin','2026-06-26 10:39:02'),
('13','小米科技有限公司','1','91440300708461138B','{"scale": "medium", "revenue": 3000000000, "industry": "High-tech", "high_tech": true, "tax_status": "normal", "growth_rate": 10, "credit_score": 72, "research_spend": 800000000, "green_certified": false, "registered_capital": 2000000000}','13800001004','6222021234567890004','0','111','admin','2026-06-26 10:39:02'),
('14','京东物流有限公司','1','91440300708461139C','{"scale": "large", "revenue": 4000000000, "industry": "Logistics", "high_tech": false, "tax_status": "abnormal", "growth_rate": 8, "credit_score": 65, "research_spend": 300000000, "green_certified": true, "registered_capital": 3000000000}','13800001005','6222021234567890005','0','112','admin','2026-06-26 10:39:02'),
('15','李明','2','110101199001011234','{"age": 35, "gender": "male", "income": 120000, "disabled": false, "education": "bachelor", "tax_status": "normal", "credit_score": 80}','13900001001','6222021234567890011','0','113','admin','2026-06-26 10:39:02'),
('16','王芳','2','110101199202022345','{"age": 32, "gender": "female", "income": 150000, "disabled": false, "education": "master", "tax_status": "normal", "credit_score": 88}','13900001002','6222021234567890012','0','114','admin','2026-06-26 10:39:02'),
('17','赵强','2','110101198505033456','{"age": 40, "gender": "male", "income": 80000, "disabled": true, "education": "college", "tax_status": "normal", "credit_score": 55}','13900001003','6222021234567890013','0','115','admin','2026-06-26 10:39:02'),
('18','陈静','2','110101199506044567','{"age": 30, "gender": "female", "income": 100000, "disabled": false, "education": "bachelor", "tax_status": "normal", "credit_score": 78}','13900001004','6222021234567890014','0','116','admin','2026-06-26 10:39:02'),
('19','刘伟','2','110101198807055678','{"age": 37, "gender": "male", "income": 200000, "disabled": false, "education": "doctor", "tax_status": "normal", "credit_score": 90}','13900001005','6222021234567890015','0','117','admin','2026-06-26 10:39:02');

DROP TABLE IF EXISTS `ee_verify_record`;
CREATE TABLE `ee_verify_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `user_id` bigint DEFAULT NULL COMMENT '??ID',
  `verify_type` varchar(20) NOT NULL COMMENT '????(sms?? idcard??? face??)',
  `verify_result` char(1) DEFAULT '0' COMMENT '????(0?? 1??)',
  `verify_content` varchar(500) DEFAULT NULL COMMENT '??????',
  `verify_no` varchar(64) DEFAULT NULL COMMENT '??????',
  `ip_address` varchar(128) DEFAULT NULL COMMENT 'IP??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  PRIMARY KEY (`record_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_verify_type` (`verify_type`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='?????';

DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table` (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成功能作者',
  `form_col_num` int DEFAULT '1' COMMENT '表单布局（单列 双列 三列）',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代码生成业务表';

DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column` (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典类型',
  `sort` int DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代码生成业务表字段';

DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='参数配置表';

INSERT INTO `sys_config` (`config_id`,`config_name`,`config_key`,`config_value`,`config_type`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
('1','主框架页-默认皮肤样式名称','sys.index.skinName','skin-blue','Y','admin','2026-06-09 13:55:55',NULL,NULL,'蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow'),
('2','用户管理-账号初始密码','sys.user.initPassword','admin123','Y','admin','2026-06-09 13:55:55',NULL,NULL,'初始化密码 123456'),
('3','主框架页-侧边栏主题','sys.index.sideTheme','theme-dark','Y','admin','2026-06-09 13:55:55',NULL,NULL,'深色主题theme-dark，浅色主题theme-light'),
('4','账号自助-验证码开关','sys.account.captchaEnabled','false','Y','admin','2026-06-09 13:55:55',NULL,NULL,'是否开启验证码功能（true开启，false关闭）'),
('5','账号自助-是否开启用户注册功能','sys.account.registerUser','true','Y','admin','2026-06-09 13:55:55',NULL,NULL,'是否开启注册用户功能（true开启，false关闭）'),
('6','用户登录-黑名单列表','sys.login.blackIPList',NULL,'Y','admin','2026-06-09 13:55:55',NULL,NULL,'设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）'),
('7','用户管理-初始密码修改策略','sys.account.initPasswordModify','1','Y','admin','2026-06-09 13:55:55',NULL,NULL,'0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框'),
('8','用户管理-账号密码更新周期','sys.account.passwordValidateDays','0','Y','admin','2026-06-09 13:55:55',NULL,NULL,'密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框'),
('9','用户管理-密码字符范围','sys.account.chrtype','0','Y','admin','2026-06-09 13:55:55',NULL,NULL,'默认任意字符范围，0任意（密码可以输入任意字符），1数字（密码只能为0-9数字），2英文字母（密码只能为a-z和A-Z字母），3字母和数字（密码必须包含字母，数字）,4字母数字和特殊字符（目前支持的特殊字符包括：~!@#$%^&*()-=_+）');

DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '部门名称',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

INSERT INTO `sys_dept` (`dept_id`,`parent_id`,`ancestors`,`dept_name`,`order_num`,`leader`,`phone`,`email`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`) VALUES
('100','0','0','若依科技','0','若依','15888888888','ry@qq.com','0','0','admin','2026-06-09 13:55:55',NULL,NULL),
('101','100','0,100','深圳总公司','1','若依','15888888888','ry@qq.com','0','0','admin','2026-06-09 13:55:55',NULL,NULL),
('102','100','0,100','长沙分公司','2','若依','15888888888','ry@qq.com','0','0','admin','2026-06-09 13:55:55',NULL,NULL),
('103','101','0,100,101','研发部门','1','若依','15888888888','ry@qq.com','0','0','admin','2026-06-09 13:55:55',NULL,NULL),
('104','101','0,100,101','市场部门','2','若依','15888888888','ry@qq.com','0','0','admin','2026-06-09 13:55:55',NULL,NULL),
('105','101','0,100,101','测试部门','3','若依','15888888888','ry@qq.com','0','0','admin','2026-06-09 13:55:55',NULL,NULL),
('106','101','0,100,101','财务部门','4','若依','15888888888','ry@qq.com','0','0','admin','2026-06-09 13:55:55',NULL,NULL),
('107','101','0,100,101','运维部门','5','若依','15888888888','ry@qq.com','0','0','admin','2026-06-09 13:55:55',NULL,NULL),
('108','102','0,100,102','市场部门','1','若依','15888888888','ry@qq.com','0','0','admin','2026-06-09 13:55:55',NULL,NULL),
('109','102','0,100,102','财务部门','2','若依','15888888888','ry@qq.com','0','0','admin','2026-06-09 13:55:55',NULL,NULL);

DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int DEFAULT '0' COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB AUTO_INCREMENT=119 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

INSERT INTO `sys_dict_data` (`dict_code`,`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
('1','1','男','0','sys_user_sex',NULL,NULL,'Y','0','admin','2026-06-09 13:55:55',NULL,NULL,'性别男'),
('2','2','女','1','sys_user_sex',NULL,NULL,'N','0','admin','2026-06-09 13:55:55',NULL,NULL,'性别女'),
('3','3','未知','2','sys_user_sex',NULL,NULL,'N','0','admin','2026-06-09 13:55:55',NULL,NULL,'性别未知'),
('4','1','显示','0','sys_show_hide',NULL,'primary','Y','0','admin','2026-06-09 13:55:55',NULL,NULL,'显示菜单'),
('5','2','隐藏','1','sys_show_hide',NULL,'danger','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'隐藏菜单'),
('6','1','正常','0','sys_normal_disable',NULL,'primary','Y','0','admin','2026-06-09 13:55:55',NULL,NULL,'正常状态'),
('7','2','停用','1','sys_normal_disable',NULL,'danger','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'停用状态'),
('8','1','正常','0','sys_job_status',NULL,'primary','Y','0','admin','2026-06-09 13:55:55',NULL,NULL,'正常状态'),
('9','2','暂停','1','sys_job_status',NULL,'danger','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'停用状态'),
('10','1','默认','DEFAULT','sys_job_group',NULL,NULL,'Y','0','admin','2026-06-09 13:55:55',NULL,NULL,'默认分组'),
('11','2','系统','SYSTEM','sys_job_group',NULL,NULL,'N','0','admin','2026-06-09 13:55:55',NULL,NULL,'系统分组'),
('12','1','是','Y','sys_yes_no',NULL,'primary','Y','0','admin','2026-06-09 13:55:55',NULL,NULL,'系统默认是'),
('13','2','否','N','sys_yes_no',NULL,'danger','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'系统默认否'),
('14','1','通知','1','sys_notice_type',NULL,'warning','Y','0','admin','2026-06-09 13:55:55',NULL,NULL,'通知'),
('15','2','公告','2','sys_notice_type',NULL,'success','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'公告'),
('16','1','正常','0','sys_notice_status',NULL,'primary','Y','0','admin','2026-06-09 13:55:55',NULL,NULL,'正常状态'),
('17','2','关闭','1','sys_notice_status',NULL,'danger','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'关闭状态'),
('18','99','其他','0','sys_oper_type',NULL,'info','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'其他操作'),
('19','1','新增','1','sys_oper_type',NULL,'info','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'新增操作'),
('20','2','修改','2','sys_oper_type',NULL,'info','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'修改操作'),
('21','3','删除','3','sys_oper_type',NULL,'danger','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'删除操作'),
('22','4','授权','4','sys_oper_type',NULL,'primary','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'授权操作'),
('23','5','导出','5','sys_oper_type',NULL,'warning','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'导出操作'),
('24','6','导入','6','sys_oper_type',NULL,'warning','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'导入操作'),
('25','7','强退','7','sys_oper_type',NULL,'danger','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'强退操作'),
('26','8','生成代码','8','sys_oper_type',NULL,'warning','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'生成操作'),
('27','9','清空数据','9','sys_oper_type',NULL,'danger','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'清空操作'),
('28','1','成功','0','sys_common_status',NULL,'primary','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'正常状态'),
('29','2','失败','1','sys_common_status',NULL,'danger','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'停用状态'),
('100','1','补贴','1','biz_policy_type',NULL,'primary','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'补贴类政策'),
('101','2','奖励','2','biz_policy_type',NULL,'success','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'奖励类政策'),
('102','3','减免','3','biz_policy_type',NULL,'warning','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'减免类政策'),
('103','1','企业','1','biz_target_type',NULL,'primary','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'企业'),
('104','2','个人','2','biz_target_type',NULL,'success','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'个人'),
('105','1','政策推送','push','biz_msg_type',NULL,'primary','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'政策推送通知'),
('106','2','兑付通知','fulfill','biz_msg_type',NULL,'success','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'兑付通知'),
('107','3','系统通知','system','biz_msg_type',NULL,'warning','N','0','admin','2026-06-09 13:55:55',NULL,NULL,'系统通知'),
('108','1','低风险','LOW','biz_risk_level',NULL,NULL,'N','0','admin','2026-06-16 20:01:32',NULL,NULL,NULL),
('109','2','中风险','MEDIUM','biz_risk_level',NULL,NULL,'N','0','admin','2026-06-16 20:01:32',NULL,NULL,NULL),
('110','3','高风险','HIGH','biz_risk_level',NULL,NULL,'N','0','admin','2026-06-16 20:01:32',NULL,NULL,NULL),
('111','1','已匹配','MATCHED','biz_match_status',NULL,NULL,'N','0','admin','2026-06-16 20:01:32',NULL,NULL,NULL),
('112','2','已推送','PUSHED','biz_match_status',NULL,NULL,'N','0','admin','2026-06-16 20:01:32',NULL,NULL,NULL),
('113','3','已确认','CONFIRMED','biz_match_status',NULL,NULL,'N','0','admin','2026-06-16 20:01:32',NULL,NULL,NULL),
('114','4','已兑付','FULFILLED','biz_match_status',NULL,NULL,'N','0','admin','2026-06-16 20:01:32',NULL,NULL,NULL),
('115','5','已归档','ARCHIVED','biz_match_status',NULL,NULL,'N','0','admin','2026-06-16 20:01:32',NULL,NULL,NULL),
('116','1','补贴款','subsidy','biz_fund_type',NULL,NULL,'N','0','admin','2026-06-16 20:01:32',NULL,NULL,NULL),
('117','2','奖励金','reward','biz_fund_type',NULL,NULL,'N','0','admin','2026-06-16 20:01:32',NULL,NULL,NULL),
('118','3','减免退税','tax_relief','biz_fund_type',NULL,NULL,'N','0','admin','2026-06-16 20:01:32',NULL,NULL,NULL);

DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

INSERT INTO `sys_dict_type` (`dict_id`,`dict_name`,`dict_type`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
('1','用户性别','sys_user_sex','0','admin','2026-06-09 13:55:55',NULL,NULL,'用户性别列表'),
('2','菜单状态','sys_show_hide','0','admin','2026-06-09 13:55:55',NULL,NULL,'菜单状态列表'),
('3','系统开关','sys_normal_disable','0','admin','2026-06-09 13:55:55',NULL,NULL,'系统开关列表'),
('4','任务状态','sys_job_status','0','admin','2026-06-09 13:55:55',NULL,NULL,'任务状态列表'),
('5','任务分组','sys_job_group','0','admin','2026-06-09 13:55:55',NULL,NULL,'任务分组列表'),
('6','系统是否','sys_yes_no','0','admin','2026-06-09 13:55:55',NULL,NULL,'系统是否列表'),
('7','通知类型','sys_notice_type','0','admin','2026-06-09 13:55:55',NULL,NULL,'通知类型列表'),
('8','通知状态','sys_notice_status','0','admin','2026-06-09 13:55:55',NULL,NULL,'通知状态列表'),
('9','操作类型','sys_oper_type','0','admin','2026-06-09 13:55:55',NULL,NULL,'操作类型列表'),
('10','系统状态','sys_common_status','0','admin','2026-06-09 13:55:55',NULL,NULL,'登录状态列表'),
('100','政策类型','biz_policy_type','0','admin','2026-06-09 13:55:55',NULL,NULL,'政策类型列表'),
('101','目标对象类型','biz_target_type','0','admin','2026-06-09 13:55:55',NULL,NULL,'企业或个人'),
('102','消息类型','biz_msg_type','0','admin','2026-06-09 13:55:55',NULL,NULL,'消息通知类型'),
('103','匹配状态','biz_match_status','0','admin','2026-06-16 20:01:25',NULL,NULL,NULL),
('104','风控等级','biz_risk_level','0','admin','2026-06-16 20:01:25',NULL,NULL,NULL),
('105','资金类型','biz_fund_type','0','admin','2026-06-16 20:01:25',NULL,NULL,NULL);

DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务调度表';

INSERT INTO `sys_job` (`job_id`,`job_name`,`job_group`,`invoke_target`,`cron_expression`,`misfire_policy`,`concurrent`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
('1','系统默认（无参）','DEFAULT','ryTask.ryNoParams','0/10 * * * * ?','3','1','1','admin','2026-06-09 13:55:55',NULL,NULL,''),
('2','系统默认（有参）','DEFAULT','ryTask.ryParams(\'ry\')','0/15 * * * * ?','3','1','1','admin','2026-06-09 13:55:55',NULL,NULL,''),
('3','系统默认（多参）','DEFAULT','ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)','0/20 * * * * ?','3','1','1','admin','2026-06-09 13:55:55',NULL,NULL,'');

DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '日志信息',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '异常信息',
  `start_time` datetime DEFAULT NULL COMMENT '执行开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '执行结束时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务调度日志表';

DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`),
  KEY `idx_sys_logininfor_s` (`status`),
  KEY `idx_sys_logininfor_lt` (`login_time`)
) ENGINE=InnoDB AUTO_INCREMENT=825 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统访问记录';

INSERT INTO `sys_logininfor` (`info_id`,`user_name`,`ipaddr`,`login_location`,`browser`,`os`,`status`,`msg`,`login_time`) VALUES
('811','admin','127.0.0.1','内网IP','WindowsPowerShell 5.1.26100.7462','Windows 10.0','0','登录成功','2026-06-26 12:47:05'),
('812','admin','127.0.0.1','内网IP','Edge 149','Windows >=10','0','退出成功','2026-06-26 12:54:24'),
('813','qy01','127.0.0.1','内网IP','Edge 149','Windows >=10','0','登录成功','2026-06-26 12:54:30'),
('814','qy01','127.0.0.1','内网IP','Edge 149','Windows >=10','0','退出成功','2026-06-26 12:58:39'),
('815','admin','127.0.0.1','内网IP','Edge 149','Windows >=10','0','登录成功','2026-06-26 12:58:45'),
('816','admin','127.0.0.1','内网IP','Chrome 149','Android 15','0','退出成功','2026-06-26 13:19:51'),
('817','admin','127.0.0.1','内网IP','Chrome 149','Android 15','0','登录成功','2026-06-26 13:19:54'),
('818','admin','127.0.0.1','内网IP','Edge 149','Windows >=10','0','登录成功','2026-06-26 15:33:22'),
('819','admin','127.0.0.1','内网IP','Edge 149','Windows >=10','0','退出成功','2026-06-26 15:33:29'),
('820','admin','127.0.0.1','内网IP','Chrome 149','Android 15','0','登录成功','2026-06-26 15:33:48'),
('821','admin','127.0.0.1','内网IP','Edge 149','Windows >=10','0','退出成功','2026-06-26 15:34:42'),
('822','admin','127.0.0.1','内网IP','Edge 149','Windows >=10','0','登录成功','2026-06-26 15:34:45'),
('823','admin','127.0.0.1','内网IP','Edge 149','Windows >=10','0','登录成功','2026-06-26 17:45:08'),
('824','admin','127.0.0.1','内网IP','Edge 149','Windows >=10','0','登录成功','2026-06-26 18:42:41');

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '路由名称',
  `is_frame` int DEFAULT '1' COMMENT '是否为外链（0是 1否）',
  `is_cache` int DEFAULT '0' COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2035 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';

INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
('1','系统管理','0','1','system',NULL,NULL,NULL,'1','0','M','0','0',NULL,'system','admin','2026-06-09 13:55:55',NULL,NULL,'系统管理目录'),
('100','用户管理','1','101','system/user','system/user/index',NULL,NULL,'1','0','C','0','0','system:user:list','user','admin','2026-06-09 13:55:55',NULL,NULL,'用户管理菜单'),
('101','角色管理','1','102','system/role','system/role/index',NULL,NULL,'1','0','C','0','0','system:role:list','peoples','admin','2026-06-09 13:55:55',NULL,NULL,'角色管理菜单'),
('105','字典管理','1','106','system/dict','system/dict/index',NULL,NULL,'1','0','C','0','0','system:dict:list','dict','admin','2026-06-09 13:55:55',NULL,NULL,'字典管理菜单'),
('108','日志管理','1','109','log',NULL,NULL,NULL,'1','0','M','0','0',NULL,'log','admin','2026-06-09 13:55:55',NULL,NULL,'日志管理菜单'),
('500','操作日志','108','101','operlog','monitor/operlog/index',NULL,NULL,'1','0','C','0','0','monitor:operlog:list','form','admin','2026-06-09 13:55:55',NULL,NULL,'操作日志菜单'),
('501','登录日志','108','102','logininfor','monitor/logininfor/index',NULL,NULL,'1','0','C','0','0','monitor:logininfor:list','logininfor','admin','2026-06-09 13:55:55',NULL,NULL,'登录日志菜单'),
('1000','用户查询','100','1',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:user:query','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1001','用户新增','100','2',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:user:add','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1002','用户修改','100','3',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:user:edit','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1003','用户删除','100','4',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:user:remove','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1004','用户导出','100','5',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:user:export','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1005','用户导入','100','6',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:user:import','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1006','重置密码','100','7',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:user:resetPwd','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1007','角色查询','101','1',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:role:query','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1008','角色新增','101','2',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:role:add','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1009','角色修改','101','3',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:role:edit','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1010','角色删除','101','4',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:role:remove','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1011','角色导出','101','5',NULL,NULL,NULL,NULL,'1','0','F','0','0','system:role:export','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1025','字典查询','105','1','#',NULL,NULL,NULL,'1','0','F','0','0','system:dict:query','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1026','字典新增','105','2','#',NULL,NULL,NULL,'1','0','F','0','0','system:dict:add','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1027','字典修改','105','3','#',NULL,NULL,NULL,'1','0','F','0','0','system:dict:edit','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1028','字典删除','105','4','#',NULL,NULL,NULL,'1','0','F','0','0','system:dict:remove','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1029','字典导出','105','5','#',NULL,NULL,NULL,'1','0','F','0','0','system:dict:export','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1039','操作查询','500','1','#',NULL,NULL,NULL,'1','0','F','0','0','monitor:operlog:query','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1040','操作删除','500','2','#',NULL,NULL,NULL,'1','0','F','0','0','monitor:operlog:remove','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1041','日志导出','500','3','#',NULL,NULL,NULL,'1','0','F','0','0','monitor:operlog:export','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1042','登录查询','501','1','#',NULL,NULL,NULL,'1','0','F','0','0','monitor:logininfor:query','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1043','登录删除','501','2','#',NULL,NULL,NULL,'1','0','F','0','0','monitor:logininfor:remove','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1044','日志导出','501','3','#',NULL,NULL,NULL,'1','0','F','0','0','monitor:logininfor:export','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1045','账户解锁','501','4','#',NULL,NULL,NULL,'1','0','F','0','0','monitor:logininfor:unlock','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1046','在线查询','109','1','#',NULL,NULL,NULL,'1','0','F','0','0','monitor:online:query','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1047','批量强退','109','2','#',NULL,NULL,NULL,'1','0','F','0','0','monitor:online:batchLogout','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1048','单条强退','109','3','#',NULL,NULL,NULL,'1','0','F','0','0','monitor:online:forceLogout','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1055','生成查询','116','1','#',NULL,NULL,NULL,'1','0','F','0','0','tool:gen:query','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1056','生成修改','116','2','#',NULL,NULL,NULL,'1','0','F','0','0','tool:gen:edit','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1057','生成删除','116','3','#',NULL,NULL,NULL,'1','0','F','0','0','tool:gen:remove','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1058','导入代码','116','4','#',NULL,NULL,NULL,'1','0','F','0','0','tool:gen:import','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1059','预览代码','116','5','#',NULL,NULL,NULL,'1','0','F','0','0','tool:gen:preview','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('1060','生成代码','116','6','#',NULL,NULL,NULL,'1','0','F','0','0','tool:gen:code','#','admin','2026-06-09 13:55:55',NULL,NULL,''),
('2002','政策管理','2015','2','policy','biz/policy/index',NULL,NULL,'1','0','C','0','0','biz:policy:list','dict','admin','2026-06-09 13:55:55',NULL,NULL,'政策管理菜单'),
('2003','规则管理','2015','3','rule','biz/rule/index',NULL,NULL,'1','0','C','0','0','biz:rule:list','form','admin','2026-06-09 13:55:55',NULL,NULL,'规则管理菜单'),
('2004','目标数据','2015','4','targetData','biz/targetData/index',NULL,NULL,'1','0','C','0','0','biz:targetData:list','peoples','admin','2026-06-09 13:55:55',NULL,NULL,'目标数据菜单'),
('2005','资金管理','2015','5','fund','biz/fund/index',NULL,NULL,'1','0','C','0','0','biz:fund:list','money','admin','2026-06-09 13:55:55',NULL,NULL,'资金管理菜单'),
('2006','匹配兑现','2015','6','matchRecord','biz/matchRecord/index',NULL,NULL,'1','0','C','0','0','biz:matchRecord:list','log','admin','2026-06-09 13:55:55',NULL,NULL,'匹配兑现记录菜单'),
('2007','消息通知','2014','7','message','biz/message/index',NULL,NULL,'1','0','C','0','0','biz:message:list','email','admin','2026-06-09 13:55:55',NULL,NULL,'消息通知中心'),
('2008','审计日志','2015','8','auditLog','biz/auditLog/index',NULL,NULL,'1','0','C','0','0','biz:auditLog:list','documentation','admin','2026-06-09 13:55:55',NULL,NULL,'操作审计日志'),
('2010','政策日历','2014','10','policyCalendar','biz/policyCalendar/index',NULL,NULL,'1','0','C','0','0','biz:policyCalendar:view','date','admin','2026-06-09 13:55:55',NULL,NULL,'政策日历与预警'),
('2012','兑付明细','2014','12','userFulfill','biz/userFulfill/index',NULL,NULL,'1','0','C','0','0','biz:userFulfill:list','money','admin','2026-06-09 18:37:16',NULL,NULL,''),
('2013','政策超市','2014','13','policyCatalog','biz/policyCatalog/index',NULL,NULL,'1','0','C','0','0','biz:policyCatalog:view','search','admin','2026-06-09 18:59:07',NULL,NULL,''),
('2014','企业门户','0','3','portal',NULL,NULL,NULL,'1','0','M','0','0',NULL,'guide','admin','2026-06-16 20:18:22',NULL,NULL,''),
('2015','业务管理','0','2','biz',NULL,NULL,NULL,'1','0','M','0','0',NULL,'tree','admin','2026-06-16 20:18:22',NULL,NULL,''),
('2016','消息通知','2015','8','message','biz/message/index',NULL,NULL,'1','0','C','0','0','biz:message:list','email','admin','2026-06-19 19:12:08',NULL,NULL,'消息通知中心'),
('2017','实名审核','2015','7','audit','biz/audit/index',NULL,NULL,'1','0','C','0','0','biz:audit:list','checkbox','1','2026-06-21 11:47:27','1','2026-06-21 11:47:27','用户实名认证审核管理'),
('2018','审核查询','2017','1',NULL,NULL,NULL,NULL,'1','0','F','0','0','biz:audit:query','#','1','2026-06-21 11:47:39','1','2026-06-21 11:47:39',''),
('2019','审核通过','2017','2',NULL,NULL,NULL,NULL,'1','0','F','0','0','biz:audit:approve','#','1','2026-06-21 11:47:39','1','2026-06-21 11:47:39',''),
('2020','审核拒绝','2017','3',NULL,NULL,NULL,NULL,'1','0','F','0','0','biz:audit:approve','#','1','2026-06-21 11:47:39','1','2026-06-21 11:47:39',''),
('2021','个人门户','0','4','personPortal',NULL,NULL,NULL,'1','0','M','0','0',NULL,'user','1','2026-06-21 12:11:48','1','2026-06-21 12:11:48','个人用户门户'),
('2022','个人首页','2021','1','personHome','biz/personHome/index',NULL,NULL,'1','0','C','0','0','biz:personHome:view','dashboard','1','2026-06-21 12:12:02','1','2026-06-21 12:12:02',''),
('2023','兑付明细','2021','2','userFulfill','biz/userFulfill/index',NULL,NULL,'1','0','C','0','0','biz:userFulfill:view','money','1','2026-06-21 12:12:02','1','2026-06-21 12:12:02',''),
('2024','政策超市','2021','3','policyCatalog','biz/policyCatalog/index',NULL,NULL,'1','0','C','0','0','biz:policyCatalog:view','shopping','1','2026-06-21 12:12:02','1','2026-06-21 12:12:02',''),
('2025','数据大屏','2015','10','dataScreen','biz/dataScreen/index',NULL,NULL,'1','0','C','0','0','biz:dataScreen:view','chart','1','2026-06-22 00:21:16',NULL,NULL,''),
('2026','AI助手','2015','4','aiAssistant','biz/aiAssistant/index',NULL,NULL,'1','0','C','0','0',NULL,'education','admin','2026-06-22 23:46:14',NULL,NULL,''),
('2027','AI助手','2014','6','aiAssistant','biz/aiAssistant/index',NULL,NULL,'1','0','C','0','0',NULL,'education','admin','2026-06-24 00:13:13',NULL,NULL,''),
('2028','AI助手','2021','5','aiAssistant','biz/aiAssistant/index',NULL,NULL,'1','0','C','0','0',NULL,'education','admin','2026-06-24 00:59:44',NULL,NULL,''),
('2029','数据大屏','2014','9','dataScreen','biz/dataScreen/index',NULL,NULL,'1','0','C','0','0',NULL,'chart','admin','2026-06-25 10:21:42',NULL,NULL,''),
('2030','数据大屏','2021','6','dataScreen','biz/dataScreen/index',NULL,NULL,'1','0','C','0','0',NULL,'chart','admin','2026-06-25 10:21:42',NULL,NULL,''),
('2031','企业首页','2014','1','enterpriseHome','biz/enterpriseHome/index',NULL,NULL,'1','0','C','0','0',NULL,'dashboard','admin','2026-06-25 10:27:26',NULL,NULL,''),
('2032','资金公示','2014','8','fundPublic','biz/fundPublic/index',NULL,NULL,'1','0','C','0','0','biz:matchRecord:list','money','admin','2026-06-26 12:50:27',NULL,NULL,''),
('2033','资金公示','2021','6','fundPublic','biz/fundPublic/index',NULL,NULL,'1','0','C','0','0','biz:matchRecord:list','money','admin','2026-06-26 12:50:27',NULL,NULL,''),
('2034','资金公示','2015','11','fundPublic','biz/fundPublic/index',NULL,NULL,'1','0','C','0','0','biz:matchRecord:list','money','admin','2026-06-26 12:51:01',NULL,NULL,'');

DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告表';

INSERT INTO `sys_notice` (`notice_id`,`notice_title`,`notice_type`,`notice_content`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
('1','温馨提醒：2018-07-01 若依新版本发布啦','2','新版本内容','0','admin','2026-06-09 13:55:55',NULL,NULL,'管理员'),
('2','维护通知：2018-07-01 若依系统凌晨维护','1','维护内容','0','admin','2026-06-09 13:55:55',NULL,NULL,'管理员'),
('3','若依开源框架介绍','1','<p><span style="color: rgb(230, 0, 0);">项目介绍</span></p><p><font color="#333333">RuoYi开源项目是为企业用户定制的后台脚手架框架，为企业打造的一站式解决方案，降低企业开发成本，提升开发效率。主要包括用户管理、角色管理、部门管理、菜单管理、参数管理、字典管理、</font><span style="color: rgb(51, 51, 51);">岗位管理</span><span style="color: rgb(51, 51, 51);">、定时任务</span><span style="color: rgb(51, 51, 51);">、</span><span style="color: rgb(51, 51, 51);">服务监控、登录日志、操作日志、代码生成等功能。其中，还支持多数据源、数据权限、国际化、Redis缓存、Docker部署、滑动验证码、第三方认证登录、分布式事务、</span><font color="#333333">分布式文件存储</font><span style="color: rgb(51, 51, 51);">、分库分表处理等技术特点。</span></p><p><img src="https://foruda.gitee.com/images/1773931848342439032/a4d22313_1815095.png" style="width: 64px;"><br></p><p><span style="color: rgb(230, 0, 0);">官网及演示</span></p><p><span style="color: rgb(51, 51, 51);">若依官网地址：&nbsp;</span><a href="http://ruoyi.vip" target="_blank">http://ruoyi.vip</a><a href="http://ruoyi.vip" target="_blank"></a></p><p><span style="color: rgb(51, 51, 51);">若依文档地址：&nbsp;</span><a href="http://doc.ruoyi.vip" target="_blank">http://doc.ruoyi.vip</a><br></p><p><span style="color: rgb(51, 51, 51);">演示地址【不分离版】：&nbsp;</span><a href="http://demo.ruoyi.vip" target="_blank">http://demo.ruoyi.vip</a></p><p><span style="color: rgb(51, 51, 51);">演示地址【分离版本】：&nbsp;</span><a href="http://vue.ruoyi.vip" target="_blank">http://vue.ruoyi.vip</a></p><p><span style="color: rgb(51, 51, 51);">演示地址【微服务版】：&nbsp;</span><a href="http://cloud.ruoyi.vip" target="_blank">http://cloud.ruoyi.vip</a></p><p><span style="color: rgb(51, 51, 51);">演示地址【移动端版】：&nbsp;</span><a href="http://h5.ruoyi.vip" target="_blank">http://h5.ruoyi.vip</a></p><p><br style="color: rgb(48, 49, 51); font-family: &quot;Helvetica Neue&quot;, Helvetica, Arial, sans-serif; font-size: 12px;"></p>','0','admin','2026-06-09 13:55:55',NULL,NULL,'管理员');

DROP TABLE IF EXISTS `sys_notice_read`;
CREATE TABLE `sys_notice_read` (
  `read_id` bigint NOT NULL AUTO_INCREMENT COMMENT '已读主键',
  `notice_id` int NOT NULL COMMENT '公告id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `read_time` datetime NOT NULL COMMENT '阅读时间',
  PRIMARY KEY (`read_id`),
  UNIQUE KEY `uk_user_notice` (`user_id`,`notice_id`) COMMENT '同一用户同一公告只记录一次'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告已读记录表';

DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '请求方式',
  `operator_type` int DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '返回参数',
  `status` int DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT '0' COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`),
  KEY `idx_sys_oper_log_bt` (`business_type`),
  KEY `idx_sys_oper_log_s` (`status`),
  KEY `idx_sys_oper_log_ot` (`oper_time`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志记录';

INSERT INTO `sys_oper_log` (`oper_id`,`title`,`business_type`,`method`,`request_method`,`operator_type`,`oper_name`,`dept_name`,`oper_url`,`oper_ip`,`oper_location`,`oper_param`,`json_result`,`status`,`error_msg`,`oper_time`,`cost_time`) VALUES
('106','用户管理','5','com.exemptenjoy.web.controller.system.SysUserController.export()','POST','1','admin','研发部门','/system/user/export','127.0.0.1','内网IP','{"pageSize":"10","pageNum":"1"}',NULL,'0',NULL,'2026-06-26 16:04:34','1900');

DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位信息表';

INSERT INTO `sys_post` (`post_id`,`post_code`,`post_name`,`post_sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
('1','ceo','董事长','1','0','admin','2026-06-09 13:55:55',NULL,NULL,''),
('2','se','项目经理','2','0','admin','2026-06-09 13:55:55',NULL,NULL,''),
('3','hr','人力资源','3','0','admin','2026-06-09 13:55:55',NULL,NULL,''),
('4','user','普通员工','4','0','admin','2026-06-09 13:55:55',NULL,NULL,'');

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色信息表';

INSERT INTO `sys_role` (`role_id`,`role_name`,`role_key`,`role_sort`,`data_scope`,`menu_check_strictly`,`dept_check_strictly`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
('1','超级管理员','admin','1','1','1','1','0','0','admin','2026-06-09 13:55:55',NULL,NULL,'超级管理员'),
('2','普通角色','common','2','1','1','1','0','0','admin','2026-06-09 13:55:55',NULL,NULL,'普通角色'),
('3','企业用户','enterprise','3','1','1','1','0','0','admin','2026-06-16 20:00:27',NULL,NULL,NULL),
('4','个人用户','person','4','1','1','1','0','0','admin','2026-06-19 22:40:43',NULL,NULL,NULL);

DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色和部门关联表';

INSERT INTO `sys_role_dept` (`role_id`,`dept_id`) VALUES
('2','100'),
('2','101'),
('2','105');

DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色和菜单关联表';

INSERT INTO `sys_role_menu` (`role_id`,`menu_id`) VALUES
('1','2007'),
('1','2016'),
('1','2017'),
('1','2018'),
('1','2019'),
('1','2020'),
('1','2025'),
('1','2032'),
('1','2033'),
('1','2034'),
('2','2007'),
('2','2010'),
('2','2012'),
('2','2013'),
('2','2014'),
('2','2027'),
('3','2007'),
('3','2010'),
('3','2012'),
('3','2013'),
('3','2014'),
('3','2025'),
('3','2027'),
('3','2029'),
('3','2031'),
('3','2032'),
('4','2021'),
('4','2022'),
('4','2023'),
('4','2024'),
('4','2025'),
('4','2028'),
('4','2030'),
('4','2033');

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '手机号码',
  `id_card` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????(??)',
  `id_card_front` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????????',
  `id_card_back` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????????',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????',
  `phone_verified` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '????(0??? 1???)',
  `id_card_verified` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '?????(0??? 1???)',
  `face_verified` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '????(0??? 1???)',
  `audit_status` char(1) COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '????(0??? 1??? 2???)',
  `audit_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '????',
  `audit_time` datetime DEFAULT NULL COMMENT '????',
  `audit_by` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '???',
  `verify_time` datetime DEFAULT NULL COMMENT '??????',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

INSERT INTO `sys_user` (`user_id`,`dept_id`,`user_name`,`nick_name`,`user_type`,`email`,`phonenumber`,`id_card`,`id_card_front`,`id_card_back`,`real_name`,`phone_verified`,`id_card_verified`,`face_verified`,`audit_status`,`audit_remark`,`audit_time`,`audit_by`,`verify_time`,`sex`,`avatar`,`password`,`status`,`del_flag`,`login_ip`,`login_date`,`pwd_update_date`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) VALUES
('1','103','admin','若依','00','ry@163.com','15888888888',NULL,NULL,NULL,NULL,'0','0','0','0',NULL,NULL,NULL,NULL,'1',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0','127.0.0.1','2026-06-26 18:42:41','2026-06-09 13:55:55','admin','2026-06-09 13:55:55',NULL,NULL,'管理员'),
('2','105','qy','企业用户','00','1945244302@qq.com','15666666666',NULL,NULL,NULL,NULL,'0','0','1','0',NULL,NULL,NULL,NULL,'0',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0','127.0.0.1','2026-06-26 11:21:28','2026-06-09 13:55:55','admin','2026-06-09 13:55:55','admin','2026-06-21 11:29:54','测试员'),
('104',NULL,'yh','个人用户','00',NULL,'18293847566',NULL,NULL,NULL,NULL,'0','0','1','0',NULL,NULL,NULL,NULL,'0',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0','127.0.0.1','2026-06-26 11:20:24',NULL,NULL,'2026-06-19 23:29:04',NULL,NULL,NULL),
('108','105','qy01','华为技术有限公司','00','hw@test.com','13800001001',NULL,NULL,NULL,NULL,'0','0','0','0',NULL,NULL,NULL,NULL,'0',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0','127.0.0.1','2026-06-26 12:54:31',NULL,'admin','2026-06-26 10:35:47',NULL,NULL,NULL),
('109','105','qy02','腾讯科技有限公司','00','qq@test.com','13800001002',NULL,NULL,NULL,NULL,'0','0','0','0',NULL,NULL,NULL,NULL,'0',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0',NULL,NULL,NULL,'admin','2026-06-26 10:35:56',NULL,NULL,NULL),
('110','105','qy03','比亚迪汽车有限公司','00','byd@test.com','13800001003',NULL,NULL,NULL,NULL,'0','0','0','0',NULL,NULL,NULL,NULL,'0',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0',NULL,NULL,NULL,'admin','2026-06-26 10:36:02',NULL,NULL,NULL),
('111','105','qy04','小米科技有限公司','00','mi@test.com','13800001004',NULL,NULL,NULL,NULL,'0','0','0','0',NULL,NULL,NULL,NULL,'0',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0',NULL,NULL,NULL,'admin','2026-06-26 10:36:09',NULL,NULL,NULL),
('112','105','qy05','京东物流有限公司','00','jd@test.com','13800001005',NULL,NULL,NULL,NULL,'0','0','0','0',NULL,NULL,NULL,NULL,'0',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0',NULL,NULL,NULL,'admin','2026-06-26 10:36:15',NULL,NULL,NULL),
('113','105','yh01','李明','00','lm@test.com','13900001001',NULL,NULL,NULL,NULL,'0','0','0','0',NULL,NULL,NULL,NULL,'0',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0','127.0.0.1','2026-06-26 11:30:33',NULL,'admin','2026-06-26 10:36:26',NULL,NULL,NULL),
('114','105','yh02','王芳','00','wf@test.com','13900001002',NULL,NULL,NULL,NULL,'0','0','0','0',NULL,NULL,NULL,NULL,'1',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0','127.0.0.1','2026-06-26 11:33:35',NULL,'admin','2026-06-26 10:36:32',NULL,NULL,NULL),
('115','105','yh03','赵强','00','zq@test.com','13900001003',NULL,NULL,NULL,NULL,'0','0','0','0',NULL,NULL,NULL,NULL,'0',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0',NULL,NULL,NULL,'admin','2026-06-26 10:36:38',NULL,NULL,NULL),
('116','105','yh04','陈静','00','cj@test.com','13900001004',NULL,NULL,NULL,NULL,'0','0','0','0',NULL,NULL,NULL,NULL,'1',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0','127.0.0.1','2026-06-26 12:15:31',NULL,'admin','2026-06-26 10:36:44',NULL,NULL,NULL),
('117','105','yh05','刘伟','00','lw@test.com','13900001005',NULL,NULL,NULL,NULL,'0','0','0','0',NULL,NULL,NULL,NULL,'0',NULL,'$2a$10$YgI4/JJWcf41IhfRY24a5efEReIrtfI83fpfMhbB4fgm.jA0yYN0a','0','0',NULL,NULL,NULL,'admin','2026-06-26 10:36:50',NULL,NULL,NULL);

DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户与岗位关联表';

INSERT INTO `sys_user_post` (`user_id`,`post_id`) VALUES
('1','1'),
('2','2');

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户和角色关联表';

INSERT INTO `sys_user_role` (`user_id`,`role_id`) VALUES
('1','1'),
('2','3'),
('104','4'),
('108','3'),
('109','3'),
('110','3'),
('111','3'),
('112','3'),
('113','4'),
('114','4'),
('115','4'),
('116','4'),
('117','4');

SET FOREIGN_KEY_CHECKS=1;
