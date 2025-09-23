-- 创建用户表
CREATE TABLE IF NOT EXISTS `ap_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实姓名',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint DEFAULT '0' COMMENT '性别 0:未知 1:男 2:女',
  `status` tinyint DEFAULT '1' COMMENT '状态 0:禁用 1:正常',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB COMMENT='用户表';

-- 创建第三方登录绑定表
CREATE TABLE IF NOT EXISTS `ap_user_oauth` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `oauth_type` varchar(20) NOT NULL COMMENT '第三方类型 wechat_mini/wechat_app/alipay/apple/huawei',
  `oauth_id` varchar(100) NOT NULL COMMENT '第三方用户ID',
  `union_id` varchar(100) DEFAULT NULL COMMENT '第三方UnionID',
  `oauth_info` text DEFAULT NULL COMMENT '第三方用户信息JSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_oauth` (`oauth_type`, `oauth_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='第三方登录绑定表';