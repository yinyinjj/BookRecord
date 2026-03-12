package com.bookrecord.repository;

import com.bookrecord.entity.Book;
import com.bookrecord.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findByUser(User user, Pageable pageable);

    List<Book> findByUserAndReadingStatus(User user, Book.ReadingStatus status);

    @Query("SELECT b FROM Book b WHERE b.user = :user AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.user = :user AND b.readingStatus = :status")
    Long countByUserAndStatus(@Param("user") User user, @Param("status") Book.ReadingStatus status);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.user = :user")
    Long countByUser(@Param("user") User user);
}