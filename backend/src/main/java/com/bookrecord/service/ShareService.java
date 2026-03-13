package com.bookrecord.service;

import com.bookrecord.dto.*;
import com.bookrecord.entity.*;
import com.bookrecord.exception.BadRequestException;
import com.bookrecord.exception.ResourceNotFoundException;
import com.bookrecord.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 分享服务类
 * 负责分享令牌的创建、验证和内容获取
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShareService {

    private final ShareTokenRepository shareTokenRepository;
    private final ReadingNoteRepository readingNoteRepository;
    private final QuoteRepository quoteRepository;
    private final UserService userService;

    /**
     * 前端基础URL，用于构建分享链接
     */
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendBaseUrl;

    /**
     * 创建感悟分享链接
     *
     * @param noteId      感悟ID
     * @param request     分享请求（包含有效期设置）
     * @param username    当前用户名
     * @return ShareResponse 分享响应（包含分享链接）
     * @throws BadRequestException 当感悟为私密或无权限时抛出
     */
    @Transactional
    public ShareResponse createNoteShare(Long noteId, ShareRequest request, String username) {
        log.info("创建感悟分享链接: noteId={}, username={}", noteId, username);

        // 获取感悟并验证权限
        User user = userService.getUserEntity(username);
        ReadingNote note = readingNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("ReadingNote", "id", noteId));

        // 验证用户权限
        if (!note.getBook().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("您没有权限分享此感悟");
        }

        // 检查是否为私密感悟
        if (Boolean.TRUE.equals(note.getIsPrivate())) {
            throw new BadRequestException("私密感悟不支持分享");
        }

        // 检查是否已存在有效分享令牌
        ShareToken existingToken = shareTokenRepository
                .findValidToken(noteId, ShareToken.ResourceType.NOTE, user.getId())
                .orElse(null);

        if (existingToken != null) {
            log.info("使用已存在的分享令牌: {}", existingToken.getToken());
            return ShareResponse.fromEntity(existingToken, frontendBaseUrl);
        }

        // 创建新的分享令牌
        ShareToken shareToken = createShareToken(noteId, ShareToken.ResourceType.NOTE, user, request.getExpiryDays());
        shareToken = shareTokenRepository.save(shareToken);

        log.info("感悟分享链接创建成功: token={}", shareToken.getToken());
        return ShareResponse.fromEntity(shareToken, frontendBaseUrl);
    }

    /**
     * 创建金句分享链接
     *
     * @param quoteId     金句ID
     * @param request     分享请求（包含有效期设置）
     * @param username    当前用户名
     * @return ShareResponse 分享响应（包含分享链接）
     * @throws BadRequestException 当无权限时抛出
     */
    @Transactional
    public ShareResponse createQuoteShare(Long quoteId, ShareRequest request, String username) {
        log.info("创建金句分享链接: quoteId={}, username={}", quoteId, username);

        // 获取金句并验证权限
        User user = userService.getUserEntity(username);
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", "id", quoteId));

        // 验证用户权限
        if (!quote.getBook().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("您没有权限分享此金句");
        }

        // 检查是否已存在有效分享令牌
        ShareToken existingToken = shareTokenRepository
                .findValidToken(quoteId, ShareToken.ResourceType.QUOTE, user.getId())
                .orElse(null);

        if (existingToken != null) {
            log.info("使用已存在的分享令牌: {}", existingToken.getToken());
            return ShareResponse.fromEntity(existingToken, frontendBaseUrl);
        }

        // 创建新的分享令牌
        ShareToken shareToken = createShareToken(quoteId, ShareToken.ResourceType.QUOTE, user, request.getExpiryDays());
        shareToken = shareTokenRepository.save(shareToken);

        log.info("金句分享链接创建成功: token={}", shareToken.getToken());
        return ShareResponse.fromEntity(shareToken, frontendBaseUrl);
    }

    /**
     * 获取分享内容（公开访问）
     *
     * @param token 分享令牌
     * @return SharedContentResponse 分享内容响应
     * @throws BadRequestException 当令牌无效或已过期时抛出
     */
    @Transactional(readOnly = true)
    public SharedContentResponse getSharedContent(String token) {
        log.info("获取分享内容: token={}", token);

        // 查找分享令牌
        ShareToken shareToken = shareTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("分享链接不存在或已失效"));

        // 检查是否过期
        if (shareToken.isExpired()) {
            throw new BadRequestException("分享链接已过期");
        }

        // 根据资源类型获取内容
        SharedContentResponse response;
        if (shareToken.getResourceType() == ShareToken.ResourceType.NOTE) {
            ReadingNote note = readingNoteRepository.findById(shareToken.getResourceId())
                    .orElseThrow(() -> new BadRequestException("分享的内容已被删除"));

            // 双重检查：确保不是私密感悟
            if (Boolean.TRUE.equals(note.getIsPrivate())) {
                throw new BadRequestException("此感悟已被设为私密，无法分享");
            }

            // 脱敏用户名
            String maskedUsername = maskUsername(shareToken.getUser().getUsername());
            response = SharedContentResponse.fromNote(note, maskedUsername);
        } else if (shareToken.getResourceType() == ShareToken.ResourceType.QUOTE) {
            Quote quote = quoteRepository.findById(shareToken.getResourceId())
                    .orElseThrow(() -> new BadRequestException("分享的内容已被删除"));

            // 脱敏用户名
            String maskedUsername = maskUsername(shareToken.getUser().getUsername());
            response = SharedContentResponse.fromQuote(quote, maskedUsername);
        } else {
            throw new BadRequestException("不支持的资源类型");
        }

        log.info("分享内容获取成功: type={}, resourceId={}", shareToken.getResourceType(), shareToken.getResourceId());
        return response;
    }

    /**
     * 创建分享令牌实体
     *
     * @param resourceId   资源ID
     * @param resourceType 资源类型
     * @param user         用户
     * @param expiryDays   有效期天数（0表示永久）
     * @return ShareToken 分享令牌实体
     */
    private ShareToken createShareToken(Long resourceId, ShareToken.ResourceType resourceType,
                                         User user, Integer expiryDays) {
        // 生成UUID令牌
        String token = UUID.randomUUID().toString();

        // 计算过期时间
        LocalDateTime expiresAt = null;
        if (expiryDays != null && expiryDays > 0) {
            expiresAt = LocalDateTime.now().plusDays(expiryDays);
        }

        return ShareToken.builder()
                .token(token)
                .resourceId(resourceId)
                .resourceType(resourceType)
                .user(user)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * 用户名脱敏处理
     * 只显示前2个字符，后面用*代替
     *
     * @param username 原始用户名
     * @return 脱敏后的用户名
     */
    private String maskUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "匿名用户";
        }
        if (username.length() <= 2) {
            return username.charAt(0) + "*";
        }
        return username.substring(0, 2) + "***";
    }
}