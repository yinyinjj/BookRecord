package com.bookrecord.dto;

import com.bookrecord.entity.ReadingNote;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分享内容响应DTO
 * 用于公开访问分享链接时返回内容
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedContentResponse {

    // ==================== 资源基本信息 ====================

    /**
     * 资源类型（NOTE/QUOTE）
     */
    private String resourceType;

    // ==================== 感悟内容 ====================

    /**
     * 感悟标题
     */
    private String title;

    /**
     * 感悟内容
     */
    private String content;

    /**
     * 感悟类型（THOUGHT/SUMMARY/QUESTION/INSIGHT）
     */
    private String noteType;

    /**
     * 章节信息
     */
    private String chapter;

    /**
     * 页码
     */
    private Integer pageNumber;

    /**
     * 标签
     */
    private String tags;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // ==================== 金句内容 ====================

    /**
     * 金句内容
     */
    private String quoteContent;

    /**
     * 金句备注
     */
    private String quoteNote;

    /**
     * 金句颜色标记
     */
    private String quoteColor;

    // ==================== 书籍信息 ====================

    /**
     * 书籍ID
     */
    private Long bookId;

    /**
     * 书名
     */
    private String bookTitle;

    /**
     * 作者
     */
    private String bookAuthor;

    /**
     * 书籍封面URL
     */
    private String bookCover;

    // ==================== 分享者信息 ====================

    /**
     * 分享者用户名（脱敏显示）
     */
    private String sharedBy;

    /**
     * 构建感悟分享响应
     *
     * @param note    感悟实体
     * @param fromDto 用户名脱敏处理
     * @return SharedContentResponse 响应DTO
     */
    public static SharedContentResponse fromNote(ReadingNote note, String fromDto) {
        return SharedContentResponse.builder()
                .resourceType("NOTE")
                .title(note.getTitle())
                .content(note.getContent())
                .noteType(note.getNoteType() != null ? note.getNoteType().name() : null)
                .chapter(note.getChapter())
                .pageNumber(note.getPageNumber())
                .tags(note.getTags())
                .createdAt(note.getCreatedAt())
                .bookId(note.getBook().getId())
                .bookTitle(note.getBook().getTitle())
                .bookAuthor(note.getBook().getAuthor())
                .bookCover(note.getBook().getCoverUrl())
                .sharedBy(fromDto)
                .build();
    }

    /**
     * 构建金句分享响应
     *
     * @param quote   金句实体
     * @param fromDto 用户名脱敏处理
     * @return SharedContentResponse 响应DTO
     */
    public static SharedContentResponse fromQuote(com.bookrecord.entity.Quote quote, String fromDto) {
        return SharedContentResponse.builder()
                .resourceType("QUOTE")
                .quoteContent(quote.getContent())
                .quoteNote(quote.getNote())
                .quoteColor(quote.getColor())
                .chapter(quote.getChapter())
                .pageNumber(quote.getPageNumber())
                .tags(quote.getTags())
                .createdAt(quote.getCreatedAt())
                .bookId(quote.getBook().getId())
                .bookTitle(quote.getBook().getTitle())
                .bookAuthor(quote.getBook().getAuthor())
                .bookCover(quote.getBook().getCoverUrl())
                .sharedBy(fromDto)
                .build();
    }
}