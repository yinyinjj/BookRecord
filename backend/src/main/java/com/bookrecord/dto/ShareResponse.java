package com.bookrecord.dto;

import com.bookrecord.entity.ShareToken;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分享响应DTO
 * 返回分享链接和相关信息
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShareResponse {

    /**
     * 分享令牌
     */
    private String token;

    /**
     * 完整的分享链接URL
     */
    private String shareUrl;

    /**
     * 过期时间（null表示永久有效）
     */
    private LocalDateTime expiresAt;

    /**
     * 是否永久有效
     */
    private boolean permanent;

    /**
     * 资源类型（NOTE/QUOTE）
     */
    private String resourceType;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 从ShareToken实体构建响应DTO
     *
     * @param shareToken 分享令牌实体
     * @param baseUrl    基础URL（用于构建完整分享链接）
     * @return ShareResponse 响应DTO
     */
    public static ShareResponse fromEntity(ShareToken shareToken, String baseUrl) {
        return ShareResponse.builder()
                .token(shareToken.getToken())
                .shareUrl(baseUrl + "/share/" + shareToken.getToken())
                .expiresAt(shareToken.getExpiresAt())
                .permanent(shareToken.getExpiresAt() == null)
                .resourceType(shareToken.getResourceType().name())
                .resourceId(shareToken.getResourceId())
                .build();
    }
}