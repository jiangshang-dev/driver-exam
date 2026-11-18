-- AI 模型配置管理
-- 模型类型：LLM-普通大模型（自身支持多模态）、EMBEDDING-向量模型、TTS-语音模型
-- 同一类型下只允许一个模型处于启用状态

CREATE TABLE IF NOT EXISTS ai_model (
  id                 BIGINT       NOT NULL COMMENT '主键',
  model_name         VARCHAR(128) NOT NULL COMMENT '模型名称（请求模型名，如 qwen-plus）',
  model_type         VARCHAR(16)  NOT NULL COMMENT 'LLM/EMBEDDING/TTS',
  base_url           VARCHAR(255) NOT NULL COMMENT 'api地址',
  api_key            VARCHAR(255)          DEFAULT NULL COMMENT 'api密钥',
  temperature        DECIMAL(3, 2)         DEFAULT NULL COMMENT '模型温度',
  top_p              DECIMAL(3, 2)         DEFAULT NULL COMMENT '词汇属性',
  presence_penalty   DECIMAL(3, 2)         DEFAULT NULL COMMENT '话题属性',
  frequency_penalty  DECIMAL(3, 2)         DEFAULT NULL COMMENT '重复属性',
  max_tokens         INT                   DEFAULT NULL COMMENT '最大回复',
  remark             VARCHAR(255)          DEFAULT NULL COMMENT '备注',
  sort_order         INT                   DEFAULT 0 COMMENT '排序',
  is_active          TINYINT               DEFAULT 0 COMMENT '是否启用：1-启用，0-未启用',
  is_deleted         TINYINT               DEFAULT 0 COMMENT '是否删除',
  gmt_create         DATETIME              DEFAULT NULL,
  gmt_modified       DATETIME              DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_ai_model_type (model_type, is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI模型配置';

-- AI管理 -> 模型配置 菜单
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000401, p.id, '模型配置', 'ai/model', '', 0, 'icon-setting', 1, 1067246875800000001, NOW(), 1067246875800000001, NOW()
FROM sys_menu p
WHERE p.pid = 0 AND p.name = 'AI管理'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000401);

INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000402, 1978000000000000401, '查看', '', 'ai:model:page,ai:model:info', 1, '', 0, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000402);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000403, 1978000000000000401, '保存', '', 'ai:model:save', 1, '', 1, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000403);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000404, 1978000000000000401, '修改', '', 'ai:model:update', 1, '', 2, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000404);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000405, 1978000000000000401, '删除', '', 'ai:model:delete', 1, '', 3, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000405);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000406, 1978000000000000401, '启用', '', 'ai:model:activate', 1, '', 4, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000406);
INSERT INTO sys_menu (id, pid, name, url, permissions, menu_type, icon, sort, creator, create_date, updater, update_date)
SELECT 1978000000000000407, 1978000000000000401, '测试可用', '', 'ai:model:test', 1, '', 5, 1067246875800000001, NOW(), 1067246875800000001, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1978000000000000407);
