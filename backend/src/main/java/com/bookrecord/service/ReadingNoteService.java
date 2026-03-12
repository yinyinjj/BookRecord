package com.bookrecord.service;

import com.bookrecord.dto.ReadingNoteRequest;
import com.bookrecord.dto.ReadingNoteResponse;
import com.bookrecord.entity.Book;
import com.bookrecord.entity.Quote;
import com.bookrecord.entity.ReadingNote;
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
public class ReadingNoteService {

    private final ReadingNoteRepository readingNoteRepository;
    private final BookRepository bookRepository;
    private final QuoteRepository quoteRepository;
    private final UserService userService;

    @Transactional
    public ReadingNoteResponse createNoteForBook(Long bookId, ReadingNoteRequest request, String username) {
        log.info("Creating reading note for book id: {} by user: {}", bookId, username);

        User user = userService.getUserEntity(username);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (!book.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to add notes to this book");
        }

        Quote quote = null;
        if (request.getQuoteId() != null) {
            quote = quoteRepository.findById(request.getQuoteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", request.getQuoteId()));

            if (!quote.getBook().getId().equals(bookId)) {
                throw new BadRequestException("Quote does not belong to this book");
            }
        }

        ReadingNote note = ReadingNote.builder()
                .book(book)
                .quote(quote)
                .chapter(request.getChapter())
                .pageNumber(request.getPageNumber())
                .title(request.getTitle())
                .content(request.getContent())
                .noteType(request.getNoteType() != null ? request.getNoteType() : ReadingNote.NoteType.THOUGHT)
                .tags(request.getTags())
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .build();

        note = readingNoteRepository.save(note);
        log.info("Reading note created successfully with id: {}", note.getId());

        return ReadingNoteResponse.fromEntity(note);
    }

    @Transactional
    public ReadingNoteResponse createNoteForQuote(Long quoteId, ReadingNoteRequest request, String username) {
        log.info("Creating reading note for quote id: {} by user: {}", quoteId, username);

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", quoteId));

        User user = userService.getUserEntity(username);
        if (!quote.getBook().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to add notes to this quote");
        }

        ReadingNote note = ReadingNote.builder()
                .book(quote.getBook())
                .quote(quote)
                .chapter(request.getChapter() != null ? request.getChapter() : quote.getChapter())
                .pageNumber(request.getPageNumber() != null ? request.getPageNumber() : quote.getPageNumber())
                .title(request.getTitle())
                .content(request.getContent())
                .noteType(request.getNoteType() != null ? request.getNoteType() : ReadingNote.NoteType.THOUGHT)
                .tags(request.getTags())
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .build();

        note = readingNoteRepository.save(note);
        log.info("Reading note created successfully with id: {}", note.getId());

        return ReadingNoteResponse.fromEntity(note);
    }

    @Transactional(readOnly = true)
    public Page<ReadingNoteResponse> getNotesByBookId(Long bookId, Pageable pageable, String username) {
        User user = userService.getUserEntity(username);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (!book.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to view notes for this book");
        }

        return readingNoteRepository.findByBookId(bookId, pageable)
                .map(ReadingNoteResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ReadingNoteResponse> getNotesByQuoteId(Long quoteId, String username) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", quoteId));

        User user = userService.getUserEntity(username);
        if (!quote.getBook().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to view notes for this quote");
        }

        return readingNoteRepository.findByQuoteId(quoteId).stream()
                .map(ReadingNoteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReadingNoteResponse getNoteById(Long id, String username) {
        ReadingNote note = findNoteByIdAndVerifyOwnership(id, username);
        return ReadingNoteResponse.fromEntity(note);
    }

    @Transactional
    public ReadingNoteResponse updateNote(Long id, ReadingNoteRequest request, String username) {
        log.info("Updating reading note id: {} by user: {}", id, username);

        ReadingNote note = findNoteByIdAndVerifyOwnership(id, username);

        note.setChapter(request.getChapter());
        note.setPageNumber(request.getPageNumber());
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setNoteType(request.getNoteType());
        note.setTags(request.getTags());
        note.setIsPrivate(request.getIsPrivate());

        note = readingNoteRepository.save(note);
        log.info("Reading note updated successfully: {}", note.getId());

        return ReadingNoteResponse.fromEntity(note);
    }

    @Transactional
    public void deleteNote(Long id, String username) {
        log.info("Deleting reading note id: {} by user: {}", id, username);

        ReadingNote note = findNoteByIdAndVerifyOwnership(id, username);
        readingNoteRepository.delete(note);

        log.info("Reading note deleted successfully: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<ReadingNoteResponse> searchNotes(String keyword, String username, Pageable pageable) {
        return readingNoteRepository.searchByKeyword(username, keyword, pageable)
                .map(ReadingNoteResponse::fromEntity);
    }

    private ReadingNote findNoteByIdAndVerifyOwnership(Long id, String username) {
        ReadingNote note = readingNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReadingNote", "id", id));

        User user = userService.getUserEntity(username);
        if (!note.getBook().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to access this note");
        }

        return note;
    }
}