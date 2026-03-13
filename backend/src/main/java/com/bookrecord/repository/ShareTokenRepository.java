package com.bookrecord.repository;

import com.bookrecord.entity.ShareToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 分享令牌数据访问接口
 * 提供分享令牌的CRUD操作和自定义查询方法
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Repository
public interface ShareTokenRepository extends JpaRepository<ShareToken, Long> {

    /**
     * 根据令牌字符串查找分享记录
     *
     * @param token 令牌字符串
     * @return 分享令牌实体（可能为空）
     */
    Optional<ShareToken> findByToken(String token);

    /**
     * 根据资源ID、资源类型和用户ID查找有效的分享令牌
     * 用于检查是否已存在分享链接，避免重复创建
     *
     * @param resourceId   资源ID
     * @param resourceType 资源类型
     * @param userId       用户ID
     * @return 分享令牌实体（可能为空）
     */
    @Query("SELECT st FROM ShareToken st WHERE st.resourceId = :resourceId " +
           "AND st.resourceType = :resourceType AND st.user.id = :userId " +
           "AND (st.expiresAt IS NULL OR st.expiresAt > CURRENT_TIMESTAMP)")
    Optional<ShareToken> findValidToken(Long resourceId, ShareToken.ResourceType resourceType, Long userId);

    /**
     * 删除过期的分享令牌
     * 定期清理任务可调用此方法
     *
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM ShareToken st WHERE st.expiresAt IS NOT NULL AND st.expiresAt < CURRENT_TIMESTAMP")
    int deleteExpiredTokens();

    /**
     * 根据资源ID和资源类型删除所有分享令牌
     * 当资源被删除时调用
     *
     * @param resourceId   资源ID
     * @param resourceType 资源类型
     * @return 删除的记录数
     */
    @Modifying
    @Query("DELETE FROM ShareToken st WHERE st.resourceId = :resourceId AND st.resourceType = :resourceType")
    int deleteByResourceIdAndType(Long resourceId, ShareToken.ResourceType resourceType);
}