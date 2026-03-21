package com.bookrecord.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入预览项DTO
 * 用于展示导入预览列表中的单本书籍信息
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportPreviewItem {

    /**
     * 书名
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * ISBN编号
     */
    private String isbn;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 封面URL
     */
    private String coverUrl;

    /**
     * 阅读状态（豆瓣格式：想读/在读/读过）
     */
    private String readingStatus;

    /**
     * 评分（豆瓣评分）
     */
    private Double rating;

    /**
     * 标签
     */
    private String tags;

    /**
     * 备注/简介
     */
    private String notes;

    /**
     * 是否为重复书籍（用户书架中已存在）
     */
    private boolean duplicate;

    /**
     * 重复书籍的ID（如果存在）
     */
    private Long duplicateBookId;

    /**
     * 解析状态（success/error）
     */
    private String status;

    /**
     * 错误信息（解析失败时）
     */
    private String errorMessage;
}