-- AI 会话与消息表
CREATE TABLE IF NOT EXISTS ai_chat_session (
  id VARCHAR(64) NOT NULL COMMENT '会话ID',
  user_id BIGINT DEFAULT NULL COMMENT '用户ID',
  title VARCHAR(100) DEFAULT NULL COMMENT '会话标题',
  message_count INT DEFAULT 0 COMMENT '消息数',
  create_time BIGINT DEFAULT NULL COMMENT '创建时间戳',
  update_time BIGINT DEFAULT NULL COMMENT '更新时间戳',
  PRIMARY KEY (id),
  KEY idx_ai_chat_session_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话';

CREATE TABLE IF NOT EXISTS ai_chat_message (
  id VARCHAR(64) NOT NULL COMMENT '消息ID',
  session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
  role VARCHAR(16) NOT NULL COMMENT 'user/assistant',
  content LONGTEXT COMMENT '消息内容',
  thinking LONGTEXT COMMENT '思考过程JSON',
  tool_link LONGTEXT COMMENT '工具调用结果',
  files LONGTEXT COMMENT '附件JSON',
  sort BIGINT DEFAULT 0 COMMENT '排序号',
  translate_docid VARCHAR(64) DEFAULT NULL,
  translate_doc_target_language VARCHAR(32) DEFAULT NULL,
  translate_doc_source_language VARCHAR(32) DEFAULT NULL,
  create_time BIGINT DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_ai_chat_message_session (session_id, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话消息';
