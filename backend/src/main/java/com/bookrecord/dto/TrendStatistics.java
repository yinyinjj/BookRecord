package com.bookrecord.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 阅读趋势统计数据响应
 * 用于展示每月阅读趋势图表数据
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendStatistics {

    /**
     * 月度数据列表
     * 按时间顺序排列，包含各月份的统计数据
     */
    private List<MonthlyData> monthlyData;

    /**
     * 月度统计数据
     * 包含单月的书籍新增和完成数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyData {

        /**
         * 月份标识
         * 格式：yyyy-MM（如：2026-03）
         */
        private String month;

        /**
         * 月份显示标签
         * 格式：yyyy年MM月（如：2026年03月）
         */
        private String label;

        /**
         * 新增书籍数量
         * 该月份新添加到书架的书籍总数
         */
        private Long newBooks;

        /**
         * 完成书籍数量
         * 该月份标记为已完成阅读的书籍数量
         */
        private Long completedBooks;
    }
}