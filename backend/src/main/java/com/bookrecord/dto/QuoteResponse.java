package com.bookrecord.dto;

import com.bookrecord.entity.Quote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String content;
    private String chapter;
    private Integer pageNumber;
    private String note;
    private String color;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReadingNoteResponse> relatedNotes;

    public static QuoteResponse fromEntity(Quote quote) {
        return QuoteResponse.builder()
                .id(quote.getId())
                .bookId(quote.getBook().getId())
                .bookTitle(quote.getBook().getTitle())
                .content(quote.getContent())
                .chapter(quote.getChapter())
                .pageNumber(quote.getPageNumber())
                .note(quote.getNote())
                .color(quote.getColor())
                .tags(quote.getTags())
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .build();
    }

    public static QuoteResponse fromEntityWithNotes(Quote quote, List<ReadingNoteResponse> notes) {
        QuoteResponse response = fromEntity(quote);
        response.setRelatedNotes(notes);
        return response;
    }
}