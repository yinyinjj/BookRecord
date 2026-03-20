-- =====================================================
-- Flyway 迁移脚本 V3: 支持富文本编辑器内容存储
-- =====================================================
-- 功能说明：
-- 1. 调整 reading_notes 表的 content 字段以支持更大的 HTML 内容
-- 2. 富文本编辑器生成的内容可能包含图片、表格等，需要更大的存储空间
-- =====================================================

-- PostgreSQL 和 H2 的 TEXT 类型实际上可以存储非常大的内容（约 1GB）
-- 但为了明确语义，我们添加注释说明该字段存储富文本 HTML

-- 为 reading_notes.content 添加注释
COMMENT ON COLUMN reading_notes.content IS '读书感悟内容（富文本 HTML 格式）';

-- 为 quotes.content 添加注释（金句也可能需要支持富文本）
COMMENT ON COLUMN quotes.content IS '金句内容';

-- 为 quotes.note 添加注释
COMMENT ON COLUMN quotes.note IS '金句备注';