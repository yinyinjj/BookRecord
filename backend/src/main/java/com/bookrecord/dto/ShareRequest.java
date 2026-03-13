package com.bookrecord.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分享请求DTO
 * 用于创建分享链接时传递参数
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareRequest {

    /**
     * 有效期类型
     * 0: 永久有效
     * 1: 1天有效
     * 7: 7天有效
     * 30: 30天有效
     */
    @Min(value = 0, message = "有效期类型无效")
    @Max(value = 30, message = "有效期类型无效")
    @Builder.Default
    private Integer expiryDays = 0;  // 默认永久有效
}