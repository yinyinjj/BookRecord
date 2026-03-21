package com.bookrecord.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书信息响应DTO
 * 用于返回从外部图书API获取的书籍信息
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookInfoResponse {

    /**
     * 书名
     */
    private String title;

    /**
     * 作者（多个作者用逗号分隔）
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
     * 出版日期
     */
    private String publishDate;

    /**
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 书籍简介/描述
     */
    private String description;

    /**
     * 总页数
     */
    private Integer pageCount;

    /**
     * 书籍分类（多个分类用逗号分隔）
     */
    private String categories;

    /**
     * 信息来源
     */
    private String source;
}