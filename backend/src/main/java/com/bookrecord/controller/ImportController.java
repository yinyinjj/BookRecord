package com.bookrecord.controller;

import com.bookrecord.dto.*;
import com.bookrecord.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 书籍导入控制器
 * 处理外部书单文件的上传、预览和导入确认
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/books/import")
@RequiredArgsConstructor
@Tag(name = "书籍导入", description = "外部书单导入接口（豆瓣、微信读书）")
@Slf4j
public class ImportController {

    private final ImportService importService;

    /**
     * 临时存储预览数据的缓存
     * Key: 用户名, Value: 预览项列表
     * 注意：在生产环境中应使用 Redis 等分布式缓存
     */
    private final ConcurrentHashMap<String, List<ImportPreviewItem>> previewCache = new ConcurrentHashMap<>();

    /**
     * 上传并解析书单文件
     * 返回解析预览结果，用户可查看并选择导入
     *
     * @param file 上传的文件（CSV格式）
     * @param userDetails 当前登录用户
     * @return 解析预览响应
     */
    @PostMapping("/preview")
    @Operation(summary = "上传并预览书单", description = "上传豆瓣或微信读书导出的书单文件，返回解析预览结果")
    public ResponseEntity<ApiResponse<ImportPreviewResponse>> previewImport(
            @Parameter(description = "书单文件（CSV格式）") @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("用户 {} 上传书单文件: {}", userDetails.getUsername(), file.getOriginalFilename());

        // 验证文件
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请选择要上传的文件"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".csv") && !filename.toLowerCase().endsWith(".json"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("仅支持 CSV 或 JSON 格式的文件"));
        }

        // 解析文件
        ImportPreviewResponse preview = importService.parseFile(file, userDetails.getUsername());

        // 缓存预览数据（用于后续确认导入）
        previewCache.put(userDetails.getUsername(), preview.getItems());

        return ResponseEntity.ok(ApiResponse.success("文件解析成功", preview));
    }

    /**
     * 确认导入书单
     * 根据用户选择的书籍和处理策略执行导入
     *
     * @param request 导入确认请求
     * @param userDetails 当前登录用户
     * @return 导入结果
     */
    @PostMapping("/confirm")
    @Operation(summary = "确认导入书单", description = "确认导入预览的书单数据，可选择导入项和重复处理策略")
    public ResponseEntity<ApiResponse<ImportResultResponse>> confirmImport(
            @RequestBody ImportConfirmRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        log.info("用户 {} 确认导入书单, 策略: {}", username, request.getDuplicateStrategy());

        // 从缓存获取预览数据
        List<ImportPreviewItem> items = previewCache.get(username);
        if (items == null || items.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("预览数据已过期，请重新上传文件"));
        }

        // 执行导入
        ImportResultResponse result = importService.executeImport(items, request, username);

        // 清除缓存
        previewCache.remove(username);

        return ResponseEntity.ok(ApiResponse.success("导入完成", result));
    }

    /**
     * 取消导入
     * 清除缓存的预览数据
     *
     * @param userDetails 当前登录用户
     * @return 操作结果
     */
    @DeleteMapping("/cancel")
    @Operation(summary = "取消导入", description = "取消当前导入操作，清除预览数据")
    public ResponseEntity<ApiResponse<Void>> cancelImport(
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        log.info("用户 {} 取消导入", username);

        // 清除缓存
        previewCache.remove(username);

        return ResponseEntity.ok(ApiResponse.success("已取消导入"));
    }
}