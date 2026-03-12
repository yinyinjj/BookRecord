package com.bookrecord.service;

import com.bookrecord.dto.QuoteRequest;
import com.bookrecord.dto.QuoteResponse;
import com.bookrecord.dto.ReadingNoteResponse;
import com.bookrecord.entity.Book;
import com.bookrecord.entity.Quote;
import com.bookrecord.entity.User;
import com.bookrecord.exception.BadRequestException;
import com.bookrecord.exception.ResourceNotFoundException;
import com.bookrecord.repository.BookRepository;
import com.bookrecord.repository.QuoteRepository;
import com.bookrecord.repository.ReadingNoteRepository;
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
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final BookRepository bookRepository;
    private final ReadingNoteRepository readingNoteRepository;
    private final UserService userService;

    @Transactional
    public QuoteResponse createQuote(Long bookId, QuoteRequest request, String username) {
        log.info("Creating quote for book id: {} by user: {}", bookId, username);

        User user = userService.getUserEntity(username);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (!book.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to add quotes to this book");
        }

        Quote quote = Quote.builder()
                .book(book)
                .content(request.getContent())
                .chapter(request.getChapter())
                .pageNumber(request.getPageNumber())
                .note(request.getNote())
                .color(request.getColor())
                .tags(request.getTags())
                .build();

        quote = quoteRepository.save(quote);
        log.info("Quote created successfully with id: {}", quote.getId());

        return QuoteResponse.fromEntity(quote);
    }

    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesByBookId(Long bookId, Pageable pageable, String username) {
        User user = userService.getUserEntity(username);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (!book.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to view quotes for this book");
        }

        return quoteRepository.findByBookId(bookId, pageable)
                .map(QuoteResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public QuoteResponse getQuoteById(Long id, String username) {
        Quote quote = findQuoteByIdAndVerifyOwnership(id, username);

        List<ReadingNoteResponse> notes = readingNoteRepository.findByQuoteId(id).stream()
                .map(ReadingNoteResponse::fromEntity)
                .collect(Collectors.toList());

        return QuoteResponse.fromEntityWithNotes(quote, notes);
    }

    @Transactional
    public QuoteResponse updateQuote(Long id, QuoteRequest request, String username) {
        log.info("Updating quote id: {} by user: {}", id, username);

        Quote quote = findQuoteByIdAndVerifyOwnership(id, username);

        quote.setContent(request.getContent());
        quote.setChapter(request.getChapter());
        quote.setPageNumber(request.getPageNumber());
        quote.setNote(request.getNote());
        quote.setColor(request.getColor());
        quote.setTags(request.getTags());

        quote = quoteRepository.save(quote);
        log.info("Quote updated successfully: {}", quote.getId());

        return QuoteResponse.fromEntity(quote);
    }

    @Transactional
    public void deleteQuote(Long id, String username) {
        log.info("Deleting quote id: {} by user: {}", id, username);

        Quote quote = findQuoteByIdAndVerifyOwnership(id, username);
        quoteRepository.delete(quote);

        log.info("Quote deleted successfully: {}", id);
    }

    @Transactional(readOnly = true)
    public QuoteResponse getRandomQuote(String username) {
        Quote quote = quoteRepository.findRandomQuote(username)
                .orElseThrow(() -> new ResourceNotFoundException("No quotes found"));

        List<ReadingNoteResponse> notes = readingNoteRepository.findByQuoteId(quote.getId()).stream()
                .map(ReadingNoteResponse::fromEntity)
                .collect(Collectors.toList());

        return QuoteResponse.fromEntityWithNotes(quote, notes);
    }

    @Transactional(readOnly = true)
    public Page<QuoteResponse> searchQuotes(String keyword, String username, Pageable pageable) {
        return quoteRepository.searchByKeyword(username, keyword, pageable)
                .map(QuoteResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<QuoteResponse> searchQuotesWithFilters(String keyword, String color, String tag,
                                                        String username, Pageable pageable) {
        return quoteRepository.searchWithFilters(username, keyword, color, tag, pageable)
                .map(QuoteResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ReadingNoteResponse> getNotesForQuote(Long quoteId, String username) {
        Quote quote = findQuoteByIdAndVerifyOwnership(quoteId, username);

        return readingNoteRepository.findByQuoteId(quoteId).stream()
                .map(ReadingNoteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private Quote findQuoteByIdAndVerifyOwnership(Long id, String username) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", id));

        User user = userService.getUserEntity(username);
        if (!quote.getBook().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to access this quote");
        }

        return quote;
    }
}