package com.bookrecord.controller;

import com.bookrecord.dto.*;
import com.bookrecord.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 分享控制器
 * 提供感悟和金句的分享功能接口
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "分享功能", description = "感悟和金句分享接口")
public class ShareController {

    private final ShareService shareService;

    // ==================== 需要认证的接口 ====================

    /**
     * 创建感悟分享链接
     * 生成可公开访问的分享链接，支持设置有效期
     *
     * @param noteId      感悟ID
     * @param request     分享请求（有效期设置）
     * @param userDetails 当前登录用户
     * @return ShareResponse 分享响应（包含分享链接）
     */
    @PostMapping("/api/v1/notes/{noteId}/share")
    @Operation(summary = "分享感悟", description = "生成感悟分享链接，支持设置有效期（1天/7天/永久）")
    public ResponseEntity<ApiResponse<ShareResponse>> shareNote(
            @Parameter(description = "感悟ID") @PathVariable Long noteId,
            @Valid @RequestBody(required = false) ShareRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 设置默认值
        if (request == null) {
            request = ShareRequest.builder().expiryDays(0).build();
        }

        ShareResponse response = shareService.createNoteShare(noteId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("分享链接创建成功", response));
    }

    /**
     * 创建金句分享链接
     * 生成可公开访问的分享链接，支持设置有效期
     *
     * @param quoteId     金句ID
     * @param request     分享请求（有效期设置）
     * @param userDetails 当前登录用户
     * @return ShareResponse 分享响应（包含分享链接）
     */
    @PostMapping("/api/v1/quotes/{quoteId}/share")
    @Operation(summary = "分享金句", description = "生成金句分享链接，支持设置有效期（1天/7天/永久）")
    public ResponseEntity<ApiResponse<ShareResponse>> shareQuote(
            @Parameter(description = "金句ID") @PathVariable Long quoteId,
            @Valid @RequestBody(required = false) ShareRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 设置默认值
        if (request == null) {
            request = ShareRequest.builder().expiryDays(0).build();
        }

        ShareResponse response = shareService.createQuoteShare(quoteId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("分享链接创建成功", response));
    }

    // ==================== 公开访问接口（无需认证） ====================

    /**
     * 获取分享内容
     * 通过分享令牌访问感悟或金句内容，无需登录
     *
     * @param token 分享令牌
     * @return SharedContentResponse 分享内容响应
     */
    @GetMapping("/api/public/share/{token}")
    @Operation(summary = "获取分享内容", description = "通过分享链接访问感悟或金句内容，无需登录")
    public ResponseEntity<ApiResponse<SharedContentResponse>> getSharedContent(
            @Parameter(description = "分享令牌") @PathVariable String token) {

        SharedContentResponse response = shareService.getSharedContent(token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}