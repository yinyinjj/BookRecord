package com.bookrecord.dto;

import com.bookrecord.entity.Quote;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequest {

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 100, message = "Chapter must not exceed 100 characters")
    private String chapter;

    private Integer pageNumber;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    @Size(max = 20, message = "Color must not exceed 20 characters")
    private String color;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;
}