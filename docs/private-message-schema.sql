-- ============================================
-- 私信系统数据库表创建脚本
-- 适用于 MySQL 8.x + DBeaver CE
-- 执行顺序：先执行 Step 1，确认成功后再执行 Step 2
-- ============================================

-- ============================================
-- Step 1: 创建私信会话表
-- 说明：记录两个用户之间的会话关系
-- ============================================

CREATE TABLE IF NOT EXISTS private_message_conversation (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    user1_id INT NOT NULL COMMENT '用户1 ID（较小userId）',
    user2_id INT NOT NULL COMMENT '用户2 ID（较大userId）',
    last_message VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息预览',
    last_message_time DATETIME DEFAULT NULL COMMENT '最后消息时间',
    user1_unread_count INT DEFAULT 0 COMMENT '用户1未读数',
    user2_unread_count INT DEFAULT 0 COMMENT '用户2未读数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_pair (user1_id, user2_id),
    INDEX idx_user1 (user1_id),
    INDEX idx_user2 (user2_id),
    INDEX idx_last_time (last_message_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信会话表';

-- ============================================
-- Step 2: 创建私信消息表
-- 说明：记录每条私信的内容
-- ============================================

CREATE TABLE IF NOT EXISTS private_message (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    conversation_id INT NOT NULL COMMENT '所属会话ID',
    sender_id INT NOT NULL COMMENT '发送者ID',
    receiver_id INT NOT NULL COMMENT '接收者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    type VARCHAR(20) DEFAULT 'text' COMMENT '消息类型: text/image/article',
    extra_data VARCHAR(1000) DEFAULT NULL COMMENT '额外数据（如图片URL、文章ID等）',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_conversation (conversation_id),
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息表';

-- ============================================
-- Step 3: 为 article 表添加 share_count 字段
-- 说明：记录文章被分享次数
-- ============================================

ALTER TABLE article ADD COLUMN share_count INT DEFAULT 0 COMMENT '分享次数';

-- ============================================
-- 验证语句（可选，执行后可查看表结构）
-- ============================================
-- DESCRIBE private_message_conversation;
-- DESCRIBE private_message;
-- DESCRIBE article;
