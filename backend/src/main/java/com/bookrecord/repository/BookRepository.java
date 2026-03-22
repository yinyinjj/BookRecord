package com.bookrecord.repository;

import com.bookrecord.entity.Book;
import com.bookrecord.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:statuses IS NULL OR " +
           "     CAST(b.readingStatus AS string) IN :statuses) " +
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

    // ==================== 书单导入相关查询 ====================

    /**
     * 根据用户和ISBN查找书籍
     * 用于导入时检测重复书籍
     *
     * @param user 用户实体
     * @param isbn ISBN编号
     * @return Optional<Book> 书籍（如果存在）
     */
    Optional<Book> findByUserAndIsbn(User user, String isbn);

    /**
     * 根据用户、书名和作者查找书籍
     * 用于导入时检测重复书籍（当没有ISBN时使用）
     *
     * @param user 用户实体
     * @param title 书名
     * @param author 作者
     * @return Optional<Book> 书籍（如果存在）
     */
    Optional<Book> findByUserAndTitleAndAuthor(User user, String title, String author);

    // ==================== 统计分析相关查询 ====================

    /**
     * 根据用户和时间范围查询书籍
     * 用于统计指定时间范围内新增的书籍
     *
     * @param user 用户实体
     * @param startDate 起始时间
     * @param endDate 结束时间
     * @return List<Book> 书籍列表
     */
    @Query("SELECT b FROM Book b WHERE b.user = :user AND b.createdAt >= :startDate AND b.createdAt <= :endDate")
    List<Book> findByUserAndCreatedAtBetween(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 根据用户、阅读状态和完成时间范围查询书籍
     * 用于统计指定时间范围内完成的书籍
     *
     * @param user 用户实体
     * @param status 阅读状态
     * @param startDate 起始日期
     * @param endDate 结束日期
     * @return List<Book> 书籍列表
     */
    @Query("SELECT b FROM Book b WHERE b.user = :user AND b.readingStatus = :status " +
           "AND b.finishDate >= :startDate AND b.finishDate <= :endDate")
    List<Book> findByUserAndReadingStatusAndFinishDateBetween(
            @Param("user") User user,
            @Param("status") Book.ReadingStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}