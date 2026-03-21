package com.bookrecord.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 导入预览响应DTO
 * 用于返回文件解析后的预览数据，供用户确认导入
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportPreviewResponse {

    /**
     * 文件来源类型（douban/weread）
     */
    private String sourceType;

    /**
     * 解析成功数量
     */
    private int successCount;

    /**
     * 解析失败数量
     */
    private int errorCount;

    /**
     * 重复书籍数量
     */
    private int duplicateCount;

    /**
     * 预览项列表
     */
    private List<ImportPreviewItem> items;

    /**
     * 原始文件名
     */
    private String fileName;
}