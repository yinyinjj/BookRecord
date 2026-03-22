package com.bookrecord.controller;

import com.bookrecord.dto.AnnualStatistics;
import com.bookrecord.dto.ApiResponse;
import com.bookrecord.dto.CategoryStatistics;
import com.bookrecord.dto.EfficiencyStatistics;
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
     * 获取年度阅读报告统计数据
     * 返回指定年份的阅读成果和习惯分析，包括完成书籍数、阅读时长、关键词等
     *
     * @param userDetails 当前登录用户信息
     * @param year 统计年份，默认为当前年份
     * @return ResponseEntity 包含年度统计数据
     */
    @GetMapping("/annual")
    @Operation(summary = "获取年度阅读报告", description = "统计年度完成书籍、阅读时长、关键词、阅读习惯等")
    public ResponseEntity<ApiResponse<AnnualStatistics>> getAnnualStatistics(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "统计年份，不传则默认为当前年份")
            @RequestParam(required = false) Integer year) {

        // 如果未指定年份，使用当前年份
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }

        // 验证年份范围（不能早于2000年，不能晚于明年）
        int currentYear = java.time.LocalDate.now().getYear();
        if (year < 2000 || year > currentYear + 1) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("年份参数无效，有效范围：2000-" + (currentYear + 1)));
        }

        AnnualStatistics statistics = statisticsService.getAnnualStatistics(
                userDetails.getUsername(), year);

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

    /**
     * 获取阅读效率分析数据
     * 返回阅读速度、完成天数、记录频率等效率指标
     *
     * @param userDetails 当前登录用户信息
     * @return ResponseEntity 包含效率统计数据
     */
    @GetMapping("/efficiency")
    @Operation(summary = "获取阅读效率分析", description = "分析阅读速度、完成天数、记录频率等效率指标，并提供阅读建议")
    public ResponseEntity<ApiResponse<EfficiencyStatistics>> getEfficiencyStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {

        EfficiencyStatistics statistics = statisticsService.getEfficiencyStatistics(
                userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}