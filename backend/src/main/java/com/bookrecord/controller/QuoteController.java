package com.bookrecord.controller;

import com.bookrecord.dto.*;
import com.bookrecord.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "金句收藏", description = "金句管理接口")
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping("/books/{bookId}/quotes")
    @Operation(summary = "创建金句", description = "为指定书籍创建金句")
    public ResponseEntity<ApiResponse<QuoteResponse>> createQuote(
            @PathVariable Long bookId,
            @Valid @RequestBody QuoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        QuoteResponse response = quoteService.createQuote(bookId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Quote created successfully", response));
    }

    @GetMapping("/books/{bookId}/quotes")
    @Operation(summary = "获取书籍的所有金句", description = "获取指定书籍的所有金句（分页）")
    public ResponseEntity<ApiResponse<Page<QuoteResponse>>> getQuotesByBookId(
            @PathVariable Long bookId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<QuoteResponse> quotes = quoteService.getQuotesByBookId(bookId, pageable, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    @GetMapping("/quotes/{id}")
    @Operation(summary = "获取金句详情", description = "根据ID获取金句详情（包含关联的感悟）")
    public ResponseEntity<ApiResponse<QuoteResponse>> getQuoteById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        QuoteResponse response = quoteService.getQuoteById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/quotes/{id}")
    @Operation(summary = "更新金句", description = "更新指定的金句")
    public ResponseEntity<ApiResponse<QuoteResponse>> updateQuote(
            @PathVariable Long id,
            @Valid @RequestBody QuoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        QuoteResponse response = quoteService.updateQuote(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Quote updated successfully", response));
    }

    @DeleteMapping("/quotes/{id}")
    @Operation(summary = "删除金句", description = "删除指定的金句")
    public ResponseEntity<ApiResponse<Void>> deleteQuote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        quoteService.deleteQuote(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Quote deleted successfully"));
    }

    @GetMapping("/quotes/random")
    @Operation(summary = "随机获取金句", description = "随机获取一条金句（用于每日展示）")
    public ResponseEntity<ApiResponse<QuoteResponse>> getRandomQuote(
            @AuthenticationPrincipal UserDetails userDetails) {
        QuoteResponse response = quoteService.getRandomQuote(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/quotes/search")
    @Operation(summary = "搜索金句", description = "根据关键词搜索金句")
    public ResponseEntity<ApiResponse<Page<QuoteResponse>>> searchQuotes(
            @Parameter(description = "搜索关键词") @RequestParam(required = false, defaultValue = "") String keyword,
            @Parameter(description = "颜色") @RequestParam(required = false) String color,
            @Parameter(description = "标签") @RequestParam(required = false) String tag,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<QuoteResponse> quotes;
        if (color != null || tag != null) {
            quotes = quoteService.searchQuotesWithFilters(keyword, color, tag, userDetails.getUsername(), pageable);
        } else {
            quotes = quoteService.searchQuotes(keyword, userDetails.getUsername(), pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(quotes));
    }

    @GetMapping("/quotes/{id}/notes")
    @Operation(summary = "获取金句的所有感悟", description = "获取指定金句关联的所有读书感悟")
    public ResponseEntity<ApiResponse<List<ReadingNoteResponse>>> getNotesForQuote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReadingNoteResponse> notes = quoteService.getNotesForQuote(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(notes));
    }
}