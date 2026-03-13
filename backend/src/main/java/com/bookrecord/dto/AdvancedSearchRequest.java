package com.bookrecord.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 高级搜索请求DTO
 * 支持多条件组合搜索，包括时间范围、阅读状态、感悟类型、标签等筛选条件
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedSearchRequest {

    /**
     * 搜索关键词
     * 用于在书籍、感悟、金句中进行全文搜索
     */
    private String keyword;

    /**
     * 搜索类型
     * 可选值：all, books, notes, quotes
     * 默认为 all，表示在所有类型中搜索
     */
    @Builder.Default
    private String type = "all";

    /**
     * 开始时间
     * 用于筛选创建时间大于等于此时间的数据
     */
    private LocalDateTime startDate;

    /**
     * 结束时间
     * 用于筛选创建时间小于等于此时间的数据
     */
    private LocalDateTime endDate;

    /**
     * 阅读状态列表
     * 用于筛选书籍，可多选
     * 可选值：WANT_TO_READ, READING, COMPLETED, ABANDONED
     */
    private List<String> readingStatuses;

    /**
     * 感悟类型列表
     * 用于筛选感悟，可多选
     * 可选值：THOUGHT, SUMMARY, QUESTION, INSIGHT
     */
    private List<String> noteTypes;

    /**
     * 标签列表
     * 用于筛选包含指定标签的数据
     * 标签之间为 OR 关系，即匹配任一标签即可
     */
    private List<String> tags;

    /**
     * 金句颜色列表
     * 用于筛选金句，可多选
     * 可选值：red, blue, green, yellow, purple, orange, pink
     */
    private List<String> colors;

    /**
     * 条件组合方式
     * 可选值：AND, OR
     * 默认为 AND，表示所有条件同时满足
     * OR 表示满足任一条件即可
     */
    @Builder.Default
    private String combineMode = "AND";

    /**
     * 页码（从0开始）
     * 用于分页查询
     */
    @Builder.Default
    private int page = 0;

    /**
     * 每页数量
     * 用于分页查询，默认每页10条
     */
    @Builder.Default
    private int size = 10;
}