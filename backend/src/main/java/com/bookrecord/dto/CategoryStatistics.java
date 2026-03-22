package com.bookrecord.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 分类统计数据响应
 * 用于展示阅读状态分布、感悟类型分布、热门标签等统计信息
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatistics {

    /**
     * 阅读状态分布
     * key: 状态名称（想读、在读、已完成、已放弃）
     * value: 书籍数量
     */
    private List<StatusDistribution> readingStatusDistribution;

    /**
     * 感悟类型分布
     * key: 类型名称（思考、总结、问题、洞察）
     * value: 感悟数量
     */
    private List<NoteTypeDistribution> noteTypeDistribution;

    /**
     * 热门标签列表
     * 按使用次数降序排列
     */
    private List<TagFrequency> topTags;

    /**
     * 阅读状态分布项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDistribution {

        /**
         * 状态编码
         * WANT_TO_READ, READING, COMPLETED, ABANDONED
         */
        private String status;

        /**
         * 状态显示名称
         * 想读、在读、已完成、已放弃
         */
        private String label;

        /**
         * 书籍数量
         */
        private Long count;
    }

    /**
     * 感悟类型分布项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteTypeDistribution {

        /**
         * 类型编码
         * THOUGHT, SUMMARY, QUESTION, INSIGHT
         */
        private String type;

        /**
         * 类型显示名称
         * 思考、总结、问题、洞察
         */
        private String label;

        /**
         * 感悟数量
         */
        private Long count;
    }

    /**
     * 标签频率项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagFrequency {

        /**
         * 标签名称
         */
        private String tag;

        /**
         * 使用次数
         */
        private Integer count;
    }
}