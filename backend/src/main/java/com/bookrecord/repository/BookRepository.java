package com.bookrecord.repository;

import com.bookrecord.entity.Book;
import com.bookrecord.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 书籍数据访问层
 * 提供书籍相关的数据库操作
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * 根据用户分页查询书籍
     *
     * @param user 用户实体
     * @param pageable 分页参数
     * @return Page<Book> 书籍分页结果
     */
    Page<Book> findByUser(User user, Pageable pageable);

    /**
     * 根据用户和阅读状态查询书籍
     *
     * @param user 用户实体
     * @param status 阅读状态
     * @return List<Book> 书籍列表
     */
    List<Book> findByUserAndReadingStatus(User user, Book.ReadingStatus status);

    /**
     * 关键词搜索书籍
     * 在书名、作者、ISBN中进行模糊匹配
     *
     * @param user 用户实体
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return Page<Book> 书籍分页结果
     */
    @Query("SELECT b FROM Book b WHERE b.user = :user AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计用户指定状态的书籍数量
     *
     * @param user 用户实体
     * @param status 阅读状态
     * @return Long 书籍数量
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.user = :user AND b.readingStatus = :status")
    Long countByUserAndStatus(@Param("user") User user, @Param("status") Book.ReadingStatus status);

    /**
     * 统计用户的书籍总数
     *
     * @param user 用户实体
     * @return Long 书籍数量
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.user = :user")
    Long countByUser(@Param("user") User user);

    /**
     * 高级搜索书籍
     * 支持关键词、阅读状态、时间范围等多条件组合查询
     *
     * @param user 用户实体
     * @param keyword 搜索关键词（可为null）
     * @param statuses 阅读状态列表（可为null或空）
     * @param startDate 开始时间（可为null）
     * @param endDate 结束时间（可为null）
     * @param pageable 分页参数
     * @return List<Book> 符合条件的书籍列表
     */
    @Query("SELECT DISTINCT b FROM Book b WHERE b.user = :user " +
           "AND (:keyword IS NULL OR " +
           "     LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:statuses IS NULL OR SIZE(:statuses) = 0 OR " +
           "     CAST(b.readingStatus AS string) IN (:statuses)) " +
           "AND (:startDate IS NULL OR b.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR b.createdAt <= :endDate) " +
           "ORDER BY b.createdAt DESC")
    List<Book> advancedSearch(
            @Param("user") User user,
            @Param("keyword") String keyword,
            @Param("statuses") List<String> statuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}