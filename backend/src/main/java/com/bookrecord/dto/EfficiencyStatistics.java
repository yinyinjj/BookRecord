package com.bookrecord.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 阅读效率分析数据响应
 * 用于展示用户阅读习惯和效率分析
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EfficiencyStatistics {

    /**
     * 平均阅读速度（页/天）
     * 基于已完成书籍的页数和阅读天数计算
     */
    private Double averageReadingSpeed;

    /**
     * 平均完成一本书的天数
     * 基于有开始日期和完成日期的书籍计算
     */
    private Double averageCompletionDays;

    /**
     * 阅读进度更新频率（次/周）
     * 统计用户更新阅读进度的频率
     */
    private Double progressUpdateFrequency;

    /**
     * 感悟记录频率（次/周）
     * 统计用户记录感悟的频率
     */
    private Double noteRecordFrequency;

    /**
     * 金句记录频率（次/周）
     * 统计用户收藏金句的频率
     */
    private Double quoteRecordFrequency;

    /**
     * 统计周期天数
     * 用于计算频率的基准时间
     */
    private Integer statisticsDays;

    /**
     * 已完成书籍总数
     */
    private Long completedBooksCount;

    /**
     * 阅读建议列表
     * 根据用户数据生成的个性化建议
     */
    private List<ReadingAdvice> readingAdvices;

    /**
     * 阅读建议数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadingAdvice {

        /**
         * 建议类型
         * speed（阅读速度）、frequency（记录频率）、habit（阅读习惯）
         */
        private String type;

        /**
         * 建议标题
         */
        private String title;

        /**
         * 建议内容
         */
        private String content;

        /**
         * 建议图标（emoji）
         */
        private String icon;
    }
}