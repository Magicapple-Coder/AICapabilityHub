-- 本轮仅创建一张骨架示例表，不包含任何业务表。
CREATE DATABASE IF NOT EXISTS ai_chat_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_chat_db;

CREATE TABLE IF NOT EXISTS t_chat_example (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否1是',
  note VARCHAR(255) NOT NULL DEFAULT '' COMMENT '示例备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='对话能力服务骨架示例表';

-- 对话样板表：只保存本服务数据，按 user_id 隔离，is_deleted=1 的记录默认不返回。
CREATE TABLE IF NOT EXISTS t_chat_session (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id VARCHAR(64) NOT NULL COMMENT '用户标识',
  title VARCHAR(200) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否1是',
  PRIMARY KEY (id),
  KEY idx_chat_session_user_deleted_update (user_id, is_deleted, update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='对话会话';

CREATE TABLE IF NOT EXISTS t_chat_message (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  session_id BIGINT NOT NULL COMMENT '会话 ID',
  user_id VARCHAR(64) NOT NULL COMMENT '用户标识',
  role VARCHAR(16) NOT NULL COMMENT '消息角色',
  content TEXT NOT NULL COMMENT '消息内容',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否1是',
  PRIMARY KEY (id),
  KEY idx_chat_message_session_deleted_id (session_id, is_deleted, id),
  KEY idx_chat_message_user_session (user_id, session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='对话消息';
