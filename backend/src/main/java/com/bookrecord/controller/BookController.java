package com.bookrecord.controller;

import com.bookrecord.dto.*;
import com.bookrecord.entity.Book;
import com.bookrecord.service.BookService;
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

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "书籍管理", description = "书籍CRUD和阅读状态管理接口")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @Operation(summary = "创建新书籍", description = "添加一本书到书架")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @Valid @RequestBody BookRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        BookResponse response = bookService.createBook(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Book created successfully", response));
    }

    @GetMapping
    @Operation(summary = "获取书籍列表", description = "获取当前用户的书籍列表（分页）")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getBooks(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookResponse> books = bookService.getBooks(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取书籍详情", description = "根据ID获取书籍详细信息")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        BookResponse response = bookService.getBookById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新书籍信息", description = "更新指定书籍的信息")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        BookResponse response = bookService.updateBook(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除书籍", description = "删除指定的书籍")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        bookService.deleteBook(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Book deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新阅读状态", description = "更新书籍的阅读状态")
    public ResponseEntity<ApiResponse<BookResponse>> updateReadingStatus(
            @PathVariable Long id,
            @Parameter(description = "阅读状态") @RequestParam Book.ReadingStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        BookResponse response = bookService.updateReadingStatus(id, status, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Reading status updated", response));
    }

    @PatchMapping("/{id}/progress")
    @Operation(summary = "更新阅读进度", description = "更新书籍的阅读进度（当前页码）")
    public ResponseEntity<ApiResponse<BookResponse>> updateReadingProgress(
            @PathVariable Long id,
            @Valid @RequestBody ReadingProgressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        BookResponse response = bookService.updateReadingProgress(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Reading progress updated", response));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索书籍", description = "根据关键词搜索书籍（标题、作者、ISBN）")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookResponse> books = bookService.searchBooks(keyword, userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "按状态获取书籍", description = "获取指定阅读状态的所有书籍")
    public ResponseEntity<ApiResponse<java.util.List<BookResponse>>> getBooksByStatus(
            @PathVariable Book.ReadingStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        java.util.List<BookResponse> books = bookService.getBooksByStatus(status, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取阅读统计", description = "获取用户的阅读统计数据")
    public ResponseEntity<ApiResponse<BookStatistics>> getStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        BookStatistics stats = bookService.getStatistics(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}