-- Create share_tokens table for sharing functionality
-- 分享令牌表，用于存储感悟和金句的分享链接

CREATE TABLE share_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(36) NOT NULL UNIQUE,           -- UUID格式的分享令牌
    resource_id BIGINT NOT NULL,                  -- 资源ID（感悟或金句）
    resource_type VARCHAR(20) NOT NULL,           -- 资源类型（NOTE/QUOTE）
    user_id BIGINT NOT NULL,                      -- 创建者用户ID
    expires_at TIMESTAMP,                         -- 过期时间（NULL表示永久有效）
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_share_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_resource_type CHECK (resource_type IN ('NOTE', 'QUOTE'))
);

-- Create indexes for efficient querying
CREATE INDEX idx_share_tokens_token ON share_tokens(token);
CREATE INDEX idx_share_tokens_resource ON share_tokens(resource_id, resource_type);
CREATE INDEX idx_share_tokens_user ON share_tokens(user_id);
CREATE INDEX idx_share_tokens_expires ON share_tokens(expires_at);

-- Create trigger for updated_at
CREATE TRIGGER update_share_tokens_updated_at BEFORE UPDATE ON share_tokens
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comment to table
COMMENT ON TABLE share_tokens IS '分享令牌表，存储感悟和金句的分享链接信息';
COMMENT ON COLUMN share_tokens.token IS 'UUID格式的分享令牌，用于生成分享链接';
COMMENT ON COLUMN share_tokens.resource_id IS '关联的资源ID（感悟ID或金句ID）';
COMMENT ON COLUMN share_tokens.resource_type IS '资源类型：NOTE-感悟，QUOTE-金句';
COMMENT ON COLUMN share_tokens.expires_at IS '过期时间，NULL表示永久有效';