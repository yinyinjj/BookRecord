package com.bookrecord.dto;

import com.bookrecord.entity.ReadingNote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingNoteResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long quoteId;
    private String quoteContent;
    private String chapter;
    private Integer pageNumber;
    private String title;
    private String content;
    private ReadingNote.NoteType noteType;
    private String tags;
    private Boolean isPrivate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReadingNoteResponse fromEntity(ReadingNote note) {
        return ReadingNoteResponse.builder()
                .id(note.getId())
                .bookId(note.getBook().getId())
                .bookTitle(note.getBook().getTitle())
                .quoteId(note.getQuote() != null ? note.getQuote().getId() : null)
                .quoteContent(note.getQuote() != null ? note.getQuote().getContent() : null)
                .chapter(note.getChapter())
                .pageNumber(note.getPageNumber())
                .title(note.getTitle())
                .content(note.getContent())
                .noteType(note.getNoteType())
                .tags(note.getTags())
                .isPrivate(note.getIsPrivate())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}