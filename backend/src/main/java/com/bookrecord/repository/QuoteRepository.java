package com.bookrecord.repository;

import com.bookrecord.entity.Quote;
import com.bookrecord.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 金句数据访问层
 * 提供金句相关的数据库操作
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    /**
     * 根据书籍ID分页查询金句
     *
     * @param bookId 书籍ID
     * @param pageable 分页参数
     * @return Page<Quote> 金句分页结果
     */
    Page<Quote> findByBookId(Long bookId, Pageable pageable);

    /**
     * 获取用户随机金句
     *
     * @param username 用户名
     * @return Optional<Quote> 随机金句
     */
    @Query("SELECT q FROM Quote q WHERE q.book.user.username = :username ORDER BY RANDOM()")
    Optional<Quote> findRandomQuote(@Param("username") String username);

    /**
     * 根据用户名和关键词搜索金句
     *
     * @param username 用户名
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return Page<Quote> 金句分页结果
     */
    @Query("SELECT q FROM Quote q WHERE q.book.user.username = :username AND " +
           "(LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.note) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Quote> searchByKeyword(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据用户实体和关键词搜索金句
     * 在内容、备注、标签中进行模糊匹配
     *
     * @param user 用户实体
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return List<Quote> 金句列表
     */
    @Query("SELECT q FROM Quote q WHERE q.book.user = :user AND " +
           "(LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.note) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(q.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Quote> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据条件筛选搜索金句
     *
     * @param username 用户名
     * @param keyword 搜索关键词
     * @param color 颜色
     * @param tag 标签
     * @param pageable 分页参数
     * @return Page<Quote> 金句分页结果
     */
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

    /**
     * 统计用户的金句总数
     *
     * @param username 用户名
     * @return Long 金句数量
     */
    @Query("SELECT COUNT(q) FROM Quote q WHERE q.book.user.username = :username")
    Long countByUsername(@Param("username") String username);

    /**
     * 根据书籍ID查询金句（按创建时间倒序）
     *
     * @param bookId 书籍ID
     * @return List<Quote> 金句列表
     */
    List<Quote> findByBookIdOrderByCreatedAtDesc(Long bookId);

    /**
     * 高级搜索金句
     * 支持关键词、颜色、时间范围等多条件组合查询
     * 注：标签筛选暂时不支持（tags字段为字符串，需要特殊处理）
     *
     * @param user 用户实体
     * @param keyword 搜索关键词（可为null）
     * @param colors 颜色列表（可为null或空）
     * @param startDate 开始时间（可为null）
     * @param endDate 结束时间（可为null）
     * @param pageable 分页参数
     * @return List<Quote> 符合条件的金句列表
     */
    @Query("SELECT DISTINCT q FROM Quote q WHERE q.book.user = :user " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(q.note) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(q.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:colors IS NULL OR q.color IN :colors) " +
           "AND (:startDate IS NULL OR q.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR q.createdAt <= :endDate) " +
           "ORDER BY q.createdAt DESC")
    List<Quote> advancedSearch(
            @Param("user") User user,
            @Param("keyword") String keyword,
            @Param("colors") List<String> colors,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}