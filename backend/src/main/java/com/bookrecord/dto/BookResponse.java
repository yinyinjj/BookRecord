package com.bookrecord.dto;

import com.bookrecord.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String coverUrl;
    private String isbn;
    private String publisher;
    private LocalDate publishDate;
    private Book.ReadingStatus readingStatus;
    private LocalDate startDate;
    private LocalDate finishDate;
    private Double rating;
    private Integer pageCount;
    private Integer currentPage;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private Integer progress; // percentage

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