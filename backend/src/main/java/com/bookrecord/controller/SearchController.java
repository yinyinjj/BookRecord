package com.bookrecord.controller;

import com.bookrecord.dto.AdvancedSearchRequest;
import com.bookrecord.dto.ApiResponse;
import com.bookrecord.dto.SearchResult;
import com.bookrecord.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 * 提供全局搜索和高级搜索接口
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "全局搜索", description = "统一搜索接口")
public class SearchController {

    private final SearchService searchService;

    /**
     * 基础全局搜索
     * 在书籍、感悟和金句中搜索关键词
     *
     * @param keyword 搜索关键词
     * @param userDetails 当前登录用户
     * @return SearchResult 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "全局搜索", description = "在书籍、感悟和金句中搜索关键词")
    public ResponseEntity<ApiResponse<SearchResult>> search(
            @Parameter(description = "搜索关键词") @RequestParam(required = false, defaultValue = "") String keyword,
            @AuthenticationPrincipal UserDetails userDetails) {
        SearchResult result = searchService.search(keyword, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 高级搜索
     * 支持多条件组合查询，包括时间范围、阅读状态、感悟类型、标签等筛选条件
     *
     * @param request 高级搜索请求参数
     * @param userDetails 当前登录用户
     * @return SearchResult 搜索结果
     */
    @PostMapping("/search/advanced")
    @Operation(summary = "高级搜索", description = "支持多条件组合搜索，包括时间范围、阅读状态、感悟类型、标签等")
    public ResponseEntity<ApiResponse<SearchResult>> advancedSearch(
            @RequestBody AdvancedSearchRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        SearchResult result = searchService.advancedSearch(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}