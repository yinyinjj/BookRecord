package com.bookrecord.dto;

import com.bookrecord.entity.Book;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 100, message = "Author must not exceed 100 characters")
    private String author;

    @Size(max = 500, message = "Cover URL must not exceed 500 characters")
    private String coverUrl;

    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    private String isbn;

    @Size(max = 100, message = "Publisher must not exceed 100 characters")
    private String publisher;

    private LocalDate publishDate;

    @NotNull(message = "Reading status is required")
    private Book.ReadingStatus readingStatus;

    private LocalDate startDate;

    private LocalDate finishDate;

    @DecimalMin(value = "0.0", message = "Rating must be between 0 and 5")
    @DecimalMax(value = "5.0", message = "Rating must be between 0 and 5")
    private Double rating;

    @Min(value = 0, message = "Page count must be non-negative")
    private Integer pageCount;

    @Min(value = 0, message = "Current page must be non-negative")
    private Integer currentPage;

    private String description;
}