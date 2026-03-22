package com.bookrecord.repository;

import com.bookrecord.entity.Quote;
import com.bookrecord.entity.ReadingNote;
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
 * 读书感悟数据访问层
 * 提供感悟相关的数据库操作
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Repository
public interface ReadingNoteRepository extends JpaRepository<ReadingNote, Long> {

    /**
     * 根据书籍ID分页查询感悟
     *
     * @param bookId 书籍ID
     * @param pageable 分页参数
     * @return Page<ReadingNote> 感悟分页结果
     */
    Page<ReadingNote> findByBookId(Long bookId, Pageable pageable);

    /**
     * 根据金句ID查询关联感悟
     *
     * @param quoteId 金句ID
     * @return List<ReadingNote> 感悟列表
     */
    List<ReadingNote> findByQuoteId(Long quoteId);

    /**
     * 根据用户名和关键词搜索感悟
     *
     * @param username 用户名
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return Page<ReadingNote> 感悟分页结果
     */
    @Query("SELECT rn FROM ReadingNote rn WHERE rn.book.user.username = :username AND " +
           "(LOWER(rn.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rn.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ReadingNote> searchByKeyword(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据用户实体和关键词搜索感悟
     * 在标题、内容、标签中进行模糊匹配
     *
     * @param user 用户实体
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return List<ReadingNote> 感悟列表
     */
    @Query("SELECT rn FROM ReadingNote rn WHERE rn.book.user = :user AND " +
           "(LOWER(rn.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rn.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rn.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ReadingNote> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据条件筛选搜索感悟
     *
     * @param username 用户名
     * @param keyword 搜索关键词
     * @param noteType 感悟类型
     * @param tag 标签
     * @param pageable 分页参数
     * @return Page<ReadingNote> 感悟分页结果
     */
    @Query("SELECT rn FROM ReadingNote rn WHERE rn.book.user.username = :username AND " +
           "(:noteType IS NULL OR rn.noteType = :noteType) AND " +
           "(:tag IS NULL OR LOWER(rn.tags) LIKE LOWER(CONCAT('%', :tag, '%'))) AND " +
           "(LOWER(rn.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(rn.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ReadingNote> searchWithFilters(@Param("username") String username,
                                        @Param("keyword") String keyword,
                                        @Param("noteType") ReadingNote.NoteType noteType,
                                        @Param("tag") String tag,
                                        Pageable pageable);

    /**
     * 根据书籍ID和感悟类型查询感悟
     *
     * @param bookId 书籍ID
     * @param noteType 感悟类型
     * @return List<ReadingNote> 感悟列表
     */
    @Query("SELECT rn FROM ReadingNote rn WHERE rn.book.id = :bookId AND rn.noteType = :noteType")
    List<ReadingNote> findByBookIdAndNoteType(@Param("bookId") Long bookId, @Param("noteType") ReadingNote.NoteType noteType);

    /**
     * 统计书籍的感悟数量
     *
     * @param bookId 书籍ID
     * @return Long 感悟数量
     */
    Long countByBookId(Long bookId);

    /**
     * 高级搜索感悟
     * 支持关键词、感悟类型、时间范围等多条件组合查询
     * 注：标签筛选暂时不支持（tags字段为字符串，需要特殊处理）
     *
     * @param user 用户实体
     * @param keyword 搜索关键词（可为null）
     * @param noteTypes 感悟类型列表（可为null或空）
     * @param startDate 开始时间（可为null）
     * @param endDate 结束时间（可为null）
     * @param pageable 分页参数
     * @return List<ReadingNote> 符合条件的感悟列表
     */
    @Query("SELECT DISTINCT rn FROM ReadingNote rn WHERE rn.book.user = :user " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(rn.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(rn.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(rn.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:noteTypes IS NULL OR " +
           "     CAST(rn.noteType AS string) IN :noteTypes) " +
           "AND (:startDate IS NULL OR rn.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR rn.createdAt <= :endDate) " +
           "ORDER BY rn.createdAt DESC")
    List<ReadingNote> advancedSearch(
            @Param("user") User user,
            @Param("keyword") String keyword,
            @Param("noteTypes") List<String> noteTypes,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // ==================== 统计分析相关查询 ====================

    /**
     * 根据用户查询所有感悟
     * 用于统计分析感悟类型分布和标签统计
     *
     * @param user 用户实体
     * @return List<ReadingNote> 感悟列表
     */
    @Query("SELECT rn FROM ReadingNote rn WHERE rn.book.user = :user")
    List<ReadingNote> findByUser(@Param("user") User user);

    /**
     * 根据用户和感悟类型统计数量
     *
     * @param user 用户实体
     * @param noteType 感悟类型
     * @return Long 感悟数量
     */
    @Query("SELECT COUNT(rn) FROM ReadingNote rn WHERE rn.book.user = :user AND rn.noteType = :noteType")
    Long countByUserAndNoteType(@Param("user") User user, @Param("noteType") ReadingNote.NoteType noteType);
}