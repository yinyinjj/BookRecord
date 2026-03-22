package com.bookrecord.controller;

import com.bookrecord.dto.ApiResponse;
import com.bookrecord.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 导出控制器
 * 提供感悟和金句的导出功能，支持 Markdown 和 PDF 格式
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "数据导出", description = "感悟和金句导出接口")
@Slf4j
public class ExportController {

    private final ExportService exportService;

    /**
     * 导出书籍的感悟为 Markdown 格式
     *
     * @param bookId    书籍ID
     * @param userDetails 当前登录用户
     * @return Markdown 文件
     */
    @GetMapping("/books/{bookId}/notes/export")
    @Operation(summary = "导出感悟", description = "导出指定书籍的所有感悟为 Markdown 或 PDF 格式")
    public ResponseEntity<byte[]> exportNotes(
            @PathVariable Long bookId,
            @Parameter(description = "导出格式：markdown 或 pdf") @RequestParam(defaultValue = "markdown") String format,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("导出书籍 {} 的感悟，格式: {}, 用户: {}", bookId, format, userDetails.getUsername());

        String filename;
        byte[] content;
        MediaType mediaType;

        if ("pdf".equalsIgnoreCase(format)) {
            content = exportService.exportNotesAsPdf(bookId, userDetails.getUsername());
            filename = "notes_" + bookId + ".pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            String markdown = exportService.exportNotesAsMarkdown(bookId, userDetails.getUsername());
            content = markdown.getBytes(StandardCharsets.UTF_8);
            filename = "notes_" + bookId + ".md";
            mediaType = MediaType.TEXT_MARKDOWN;
        }

        // 生成安全的文件名
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(mediaType)
                .body(content);
    }

    /**
     * 导出书籍的金句为 Markdown 格式
     *
     * @param bookId    书籍ID
     * @param userDetails 当前登录用户
     * @return Markdown 文件
     */
    @GetMapping("/books/{bookId}/quotes/export")
    @Operation(summary = "导出金句", description = "导出指定书籍的所有金句为 Markdown 或 PDF 格式")
    public ResponseEntity<byte[]> exportQuotes(
            @PathVariable Long bookId,
            @Parameter(description = "导出格式：markdown 或 pdf") @RequestParam(defaultValue = "markdown") String format,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("导出书籍 {} 的金句，格式: {}, 用户: {}", bookId, format, userDetails.getUsername());

        String filename;
        byte[] content;
        MediaType mediaType;

        if ("pdf".equalsIgnoreCase(format)) {
            content = exportService.exportQuotesAsPdf(bookId, userDetails.getUsername());
            filename = "quotes_" + bookId + ".pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            String markdown = exportService.exportQuotesAsMarkdown(bookId, userDetails.getUsername());
            content = markdown.getBytes(StandardCharsets.UTF_8);
            filename = "quotes_" + bookId + ".md";
            mediaType = MediaType.TEXT_MARKDOWN;
        }

        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(mediaType)
                .body(content);
    }
}