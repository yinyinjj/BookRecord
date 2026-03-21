package com.bookrecord.dto;

import com.bookrecord.entity.Book;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 书籍请求DTO
 * 用于接收前端传递的书籍创建/更新请求数据
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    /**
     * 书名（必填，最大200字符）
     */
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    /**
     * 作者（最大100字符）
     */
    @Size(max = 100, message = "Author must not exceed 100 characters")
    private String author;

    /**
     * 封面图片URL（最大500字符）
     */
    @Size(max = 500, message = "Cover URL must not exceed 500 characters")
    private String coverUrl;

    /**
     * ISBN编号（最大20字符）
     */
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    private String isbn;

    /**
     * 出版社（最大100字符）
     */
    @Size(max = 100, message = "Publisher must not exceed 100 characters")
    private String publisher;

    /**
     * 出版日期（字符串格式，支持年份、年月、完整日期等多种格式）
     * 例如：2015、2015-06、2015-06-15
     */
    private String publishDate;

    /**
     * 阅读状态（必填）
     */
    @NotNull(message = "Reading status is required")
    private Book.ReadingStatus readingStatus;

    /**
     * 开始阅读日期
     */
    private LocalDate startDate;

    /**
     * 完成阅读日期
     */
    private LocalDate finishDate;

    /**
     * 评分（0-5分）
     */
    @DecimalMin(value = "0.0", message = "Rating must be between 0 and 5")
    @DecimalMax(value = "5.0", message = "Rating must be between 0 and 5")
    private Double rating;

    /**
     * 总页数
     */
    @Min(value = 0, message = "Page count must be non-negative")
    private Integer pageCount;

    /**
     * 当前阅读页码
     */
    @Min(value = 0, message = "Current page must be non-negative")
    private Integer currentPage;

    /**
     * 书籍简介
     */
    private String description;
}