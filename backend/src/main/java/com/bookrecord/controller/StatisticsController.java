package com.bookrecord.controller;

import com.bookrecord.dto.ApiResponse;
import com.bookrecord.dto.CategoryStatistics;
import com.bookrecord.dto.TrendStatistics;
import com.bookrecord.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 统计控制器
 * 提供阅读统计数据相关的 REST API 接口
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "统计分析", description = "阅读统计数据相关接口")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取阅读趋势统计数据
     * 返回指定时间范围内每月新增书籍和完成书籍的数量统计
     *
     * @param userDetails 当前登录用户信息
     * @param range 时间范围：6m（近6个月）、1y（近1年）、all（全部），默认为 1y
     * @return ResponseEntity 包含趋势统计数据
     */
    @GetMapping("/trend")
    @Operation(summary = "获取阅读趋势", description = "按月份统计新增和完成书籍数量，支持时间范围筛选")
    public ResponseEntity<ApiResponse<TrendStatistics>> getTrendStatistics(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "时间范围：6m（近6个月）、1y（近1年）、all（全部）")
            @RequestParam(defaultValue = "1y") String range) {

        // 验证时间范围参数
        if (!isValidRange(range)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("无效的时间范围参数，可选值：6m、1y、all"));
        }

        TrendStatistics statistics = statisticsService.getTrendStatistics(
                userDetails.getUsername(), range);

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 获取分类统计数据
     * 返回阅读状态分布、感悟类型分布、热门标签统计
     *
     * @param userDetails 当前登录用户信息
     * @return ResponseEntity 包含分类统计数据
     */
    @GetMapping("/categories")
    @Operation(summary = "获取分类统计", description = "统计阅读状态分布、感悟类型分布、热门标签")
    public ResponseEntity<ApiResponse<CategoryStatistics>> getCategoryStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {

        CategoryStatistics statistics = statisticsService.getCategoryStatistics(
                userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 验证时间范围参数是否有效
     *
     * @param range 时间范围参数
     * @return boolean 是否有效
     */
    private boolean isValidRange(String range) {
        return "6m".equalsIgnoreCase(range) ||
               "1y".equalsIgnoreCase(range) ||
               "all".equalsIgnoreCase(range);
    }
}