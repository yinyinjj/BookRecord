package com.bookrecord.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 年度阅读报告统计数据响应
 * 用于展示用户年度阅读成果和习惯分析
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnnualStatistics {

    /**
     * 统计年份
     */
    private Integer year;

    /**
     * 年度完成书籍数
     * 该年度标记为已完成阅读的书籍总数
     */
    private Long completedBooksCount;

    /**
     * 年度新增书籍数
     * 该年度新添加到书架的书籍总数
     */
    private Long newBooksCount;

    /**
     * 年度总阅读页数
     * 已完成书籍的总页数之和，用于估算阅读时长
     */
    private Long totalPagesRead;

    /**
     * 年度阅读总时长估算（小时）
     * 基于总页数估算，假设平均阅读速度为 30 页/小时
     */
    private Long estimatedReadingHours;

    /**
     * 年度新增感悟数
     */
    private Long notesCount;

    /**
     * 年度新增金句数
     */
    private Long quotesCount;

    /**
     * 最常阅读的时间段
     * 统计感悟和金句创建时间，找出用户最活跃的阅读时段
     */
    private List<ReadingTimeDistribution> readingTimeDistribution;

    /**
     * 年度阅读关键词
     * 从感悟和金句内容中提取的高频词
     */
    private List<Keyword> topKeywords;

    /**
     * 年度阅读之星书籍
     * 年度评分最高的书籍（如有）
     */
    private BookHighlight bookOfTheYear;

    /**
     * 年度最长书籍
     * 完成阅读中页数最多的书籍
     */
    private BookHighlight longestBook;

    /**
     * 阅读时间段分布数据
     * 用于展示用户最常在什么时间段阅读
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadingTimeDistribution {

        /**
         * 时间段标识
         * 如：morning（早晨 6-12点）、afternoon（下午 12-18点）、
         * evening（晚上 18-24点）、night（深夜 0-6点）
         */
        private String period;

        /**
         * 时间段显示名称
         */
        private String label;

        /**
         * 该时间段的记录数量
         */
        private Integer count;
    }

    /**
     * 年度关键词数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Keyword {

        /**
         * 关键词内容
         */
        private String word;

        /**
         * 出现次数
         */
        private Integer count;
    }

    /**
     * 书籍亮点数据
     * 用于展示年度最佳书籍信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookHighlight {

        /**
         * 书籍ID
         */
        private Long id;

        /**
         * 书名
         */
        private String title;

        /**
         * 作者
         */
        private String author;

        /**
         * 总页数
         */
        private Integer totalPages;

        /**
         * 用户评分（1-5星）
         */
        private Integer rating;
    }
}