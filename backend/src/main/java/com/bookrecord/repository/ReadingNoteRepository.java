package com.bookrecord.repository;

import com.bookrecord.entity.Quote;
import com.bookrecord.entity.ReadingNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingNoteRepository extends JpaRepository<ReadingNote, Long> {

    Page<ReadingNote> findByBookId(Long bookId, Pageable pageable);

    List<ReadingNote> findByQuoteId(Long quoteId);

    @Query("SELECT rn FROM ReadingNote rn WHERE rn.book.user.username = :username AND " +
           "(LOWER(rn.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rn.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ReadingNote> searchByKeyword(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT rn FROM ReadingNote rn WHERE rn.book.id = :bookId AND rn.noteType = :noteType")
    List<ReadingNote> findByBookIdAndNoteType(@Param("bookId") Long bookId, @Param("noteType") ReadingNote.NoteType noteType);

    Long countByBookId(Long bookId);
}