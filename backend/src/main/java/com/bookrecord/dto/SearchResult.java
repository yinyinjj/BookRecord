package com.bookrecord.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Unified search result containing books, notes, and quotes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    private List<BookResult> books;
    private List<NoteResult> notes;
    private List<QuoteResult> quotes;

    private int totalBooks;
    private int totalNotes;
    private int totalQuotes;
    private int totalResults;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookResult {
        private Long id;
        private String title;
        private String author;
        private String coverUrl;
        private String readingStatus;
        private Integer progress;
        private String highlightedTitle;
        private String highlightedAuthor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteResult {
        private Long id;
        private Long bookId;
        private String bookTitle;
        private String title;
        private String content;
        private String noteType;
        private String highlightedTitle;
        private String highlightedContent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuoteResult {
        private Long id;
        private Long bookId;
        private String bookTitle;
        private String content;
        private String color;
        private String highlightedContent;
    }
}