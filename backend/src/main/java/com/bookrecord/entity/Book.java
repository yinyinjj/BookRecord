package com.bookrecord.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    private String author;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(length = 20)
    private String isbn;

    /**
     * 出版社
     */
    @Column(length = 100)
    private String publisher;

    /**
     * 出版日期（字符串格式，支持年份、年月、完整日期等多种格式）
     * 例如：2015、2015-06、2015-06-15
     */
    @Column(name = "publish_date", length = 20)
    private String publishDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_status", nullable = false, length = 20)
    @Builder.Default
    private ReadingStatus readingStatus = ReadingStatus.WANT_TO_READ;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "finish_date")
    private LocalDate finishDate;

    @Column(columnDefinition = "DECIMAL(3,2)")
    private Double rating;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "current_page")
    @Builder.Default
    private Integer currentPage = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReadingNote> readingNotes = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Quote> quotes = new ArrayList<>();

    public enum ReadingStatus {
        WANT_TO_READ,
        READING,
        COMPLETED,
        ABANDONED
    }
}