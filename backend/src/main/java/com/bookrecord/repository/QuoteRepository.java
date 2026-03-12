package com.bookrecord.repository;

import com.bookrecord.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Page<Quote> findByBookId(Long bookId, Pageable pageable);

    @Query("SELECT q FROM Quote q WHERE q.book.user.username = :username ORDER BY RANDOM()")
    Optional<Quote> findRandomQuote(@Param("username") String username);

    @Query("SELECT q FROM Quote q WHERE q.book.user.username = :username AND " +
           "(LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.note) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Quote> searchByKeyword(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT q FROM Quote q WHERE q.book.user.username = :username AND " +
           "(:color IS NULL OR q.color = :color) AND " +
           "(:tag IS NULL OR LOWER(q.tags) LIKE LOWER(CONCAT('%', :tag, '%'))) AND " +
           "(LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.note) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Quote> searchWithFilters(@Param("username") String username,
                                   @Param("keyword") String keyword,
                                   @Param("color") String color,
                                   @Param("tag") String tag,
                                   Pageable pageable);

    @Query("SELECT COUNT(q) FROM Quote q WHERE q.book.user.username = :username")
    Long countByUsername(@Param("username") String username);

    List<Quote> findByBookIdOrderByCreatedAtDesc(Long bookId);
}