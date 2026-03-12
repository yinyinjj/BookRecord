package com.bookrecord.service;

import com.bookrecord.dto.*;
import com.bookrecord.entity.Book;
import com.bookrecord.entity.User;
import com.bookrecord.exception.BadRequestException;
import com.bookrecord.exception.ResourceNotFoundException;
import com.bookrecord.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final UserService userService;

    @Transactional
    public BookResponse createBook(BookRequest request, String username) {
        log.info("Creating book: {} for user: {}", request.getTitle(), username);

        User user = userService.getUserEntity(username);
        Book book = Book.builder()
                .user(user)
                .title(request.getTitle())
                .author(request.getAuthor())
                .coverUrl(request.getCoverUrl())
                .isbn(request.getIsbn())
                .publisher(request.getPublisher())
                .publishDate(request.getPublishDate())
                .readingStatus(request.getReadingStatus() != null ? request.getReadingStatus() : Book.ReadingStatus.WANT_TO_READ)
                .startDate(request.getStartDate())
                .finishDate(request.getFinishDate())
                .rating(request.getRating())
                .pageCount(request.getPageCount())
                .currentPage(request.getCurrentPage() != null ? request.getCurrentPage() : 0)
                .description(request.getDescription())
                .build();

        book = bookRepository.save(book);
        log.info("Book created successfully with id: {}", book.getId());

        return BookResponse.fromEntity(book);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getBooks(String username, Pageable pageable) {
        User user = userService.getUserEntity(username);
        return bookRepository.findByUser(user, pageable)
                .map(BookResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id, String username) {
        Book book = findBookByIdAndUser(id, username);
        return BookResponse.fromEntity(book);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request, String username) {
        log.info("Updating book id: {} for user: {}", id, username);

        Book book = findBookByIdAndUser(id, username);

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setCoverUrl(request.getCoverUrl());
        book.setIsbn(request.getIsbn());
        book.setPublisher(request.getPublisher());
        book.setPublishDate(request.getPublishDate());
        book.setReadingStatus(request.getReadingStatus());
        book.setStartDate(request.getStartDate());
        book.setFinishDate(request.getFinishDate());
        book.setRating(request.getRating());
        book.setPageCount(request.getPageCount());
        book.setCurrentPage(request.getCurrentPage());
        book.setDescription(request.getDescription());

        book = bookRepository.save(book);
        log.info("Book updated successfully: {}", book.getId());

        return BookResponse.fromEntity(book);
    }

    @Transactional
    public void deleteBook(Long id, String username) {
        log.info("Deleting book id: {} for user: {}", id, username);

        Book book = findBookByIdAndUser(id, username);
        bookRepository.delete(book);

        log.info("Book deleted successfully: {}", id);
    }

    @Transactional
    public BookResponse updateReadingStatus(Long id, Book.ReadingStatus status, String username) {
        log.info("Updating reading status for book id: {} to: {}", id, status);

        Book book = findBookByIdAndUser(id, username);
        book.setReadingStatus(status);

        // Update dates based on status
        if (status == Book.ReadingStatus.READING && book.getStartDate() == null) {
            book.setStartDate(java.time.LocalDate.now());
        } else if (status == Book.ReadingStatus.COMPLETED && book.getFinishDate() == null) {
            book.setFinishDate(java.time.LocalDate.now());
        }

        book = bookRepository.save(book);
        log.info("Reading status updated successfully");

        return BookResponse.fromEntity(book);
    }

    @Transactional
    public BookResponse updateReadingProgress(Long id, ReadingProgressRequest request, String username) {
        log.info("Updating reading progress for book id: {} to page: {}", id, request.getCurrentPage());

        Book book = findBookByIdAndUser(id, username);

        if (book.getPageCount() != null && request.getCurrentPage() > book.getPageCount()) {
            throw new BadRequestException("Current page cannot exceed total page count");
        }

        book.setCurrentPage(request.getCurrentPage());
        book = bookRepository.save(book);

        log.info("Reading progress updated successfully");

        return BookResponse.fromEntity(book);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(String keyword, String username, Pageable pageable) {
        User user = userService.getUserEntity(username);
        return bookRepository.searchByKeyword(user, keyword, pageable)
                .map(BookResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByStatus(Book.ReadingStatus status, String username) {
        User user = userService.getUserEntity(username);
        return bookRepository.findByUserAndReadingStatus(user, status).stream()
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookStatistics getStatistics(String username) {
        User user = userService.getUserEntity(username);

        return BookStatistics.builder()
                .totalBooks(bookRepository.countByUser(user))
                .wantToRead(bookRepository.countByUserAndStatus(user, Book.ReadingStatus.WANT_TO_READ))
                .reading(bookRepository.countByUserAndStatus(user, Book.ReadingStatus.READING))
                .completed(bookRepository.countByUserAndStatus(user, Book.ReadingStatus.COMPLETED))
                .abandoned(bookRepository.countByUserAndStatus(user, Book.ReadingStatus.ABANDONED))
                .build();
    }

    private Book findBookByIdAndUser(Long id, String username) {
        User user = userService.getUserEntity(username);
        return bookRepository.findById(id)
                .filter(book -> book.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
    }
}