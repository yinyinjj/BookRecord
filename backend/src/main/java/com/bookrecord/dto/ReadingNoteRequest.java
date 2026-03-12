package com.bookrecord.dto;

import com.bookrecord.entity.ReadingNote;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingNoteRequest {

    private Long quoteId;

    @Size(max = 100, message = "Chapter must not exceed 100 characters")
    private String chapter;

    private Integer pageNumber;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Note type is required")
    private ReadingNote.NoteType noteType;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    private Boolean isPrivate;
}