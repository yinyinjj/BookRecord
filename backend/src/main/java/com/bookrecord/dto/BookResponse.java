package com.bookrecord.dto;

import com.bookrecord.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 书籍响应DTO
 * 用于返回书籍信息给前端
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

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
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * ISBN编号
     */
    private String isbn;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 出版日期（字符串格式）
     */
    private String publishDate;

    /**
     * 阅读状态
     */
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
     * 评分
     */
    private Double rating;

    /**
     * 总页数
     */
    private Integer pageCount;

    /**
     * 当前阅读页码
     */
    private Integer currentPage;

    /**
     * 书籍简介
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 阅读进度百分比（计算字段）
     */
    private Integer progress;

    public static BookResponse fromEntity(Book book) {
        Integer progress = null;
        if (book.getPageCount() != null && book.getPageCount() > 0 && book.getCurrentPage() != null) {
            progress = (int) ((book.getCurrentPage() * 100.0) / book.getPageCount());
        }

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .coverUrl(book.getCoverUrl())
                .isbn(book.getIsbn())
                .publisher(book.getPublisher())
                .publishDate(book.getPublishDate())
                .readingStatus(book.getReadingStatus())
                .startDate(book.getStartDate())
                .finishDate(book.getFinishDate())
                .rating(book.getRating())
                .pageCount(book.getPageCount())
                .currentPage(book.getCurrentPage())
                .description(book.getDescription())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .progress(progress)
                .build();
    }
}