package com.bookrecord.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 分享令牌实体
 * 用于存储感悟和金句的分享链接令牌，支持有效期设置
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Entity
@Table(name = "share_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareToken {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分享令牌（UUID格式，用于生成分享链接）
     */
    @Column(name = "token", nullable = false, unique = true, length = 36)
    private String token;

    /**
     * 资源ID（感悟或金句的ID）
     */
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    /**
     * 资源类型（NOTE-感悟, QUOTE-金句）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private ResourceType resourceType;

    /**
     * 分享创建者用户ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 过期时间（null表示永久有效）
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        NOTE,   // 读书感悟
        QUOTE   // 金句
    }

    /**
     * 检查分享令牌是否已过期
     *
     * @return true-已过期，false-未过期或永久有效
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}