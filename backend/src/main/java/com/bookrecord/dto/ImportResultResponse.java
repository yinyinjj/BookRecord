package com.bookrecord.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 导入结果响应DTO
 * 用于返回批量导入的结果统计
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {

    /**
     * 导入成功数量
     */
    private int successCount;

    /**
     * 导入失败数量
     */
    private int failCount;

    /**
     * 跳过数量（重复书籍）
     */
    private int skipCount;

    /**
     * 覆盖数量（重复书籍被覆盖）
     */
    private int overwriteCount;

    /**
     * 总处理数量
     */
    private int totalCount;

    /**
     * 导入失败的书籍列表（包含失败原因）
     */
    private List<ImportFailureItem> failures;

    /**
     * 导入失败的书籍项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportFailureItem {

        /**
         * 书名
         */
        private String title;

        /**
         * 作者
         */
        private String author;

        /**
         * 失败原因
         */
        private String reason;
    }
}