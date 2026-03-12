package com.bookrecord.controller;

import com.bookrecord.dto.*;
import com.bookrecord.service.ReadingNoteService;
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
@Tag(name = "读书感悟", description = "读书感悟管理接口")
public class ReadingNoteController {

    private final ReadingNoteService readingNoteService;

    @PostMapping("/books/{bookId}/notes")
    @Operation(summary = "为书籍创建感悟", description = "为指定书籍创建读书感悟")
    public ResponseEntity<ApiResponse<ReadingNoteResponse>> createNoteForBook(
            @PathVariable Long bookId,
            @Valid @RequestBody ReadingNoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReadingNoteResponse response = readingNoteService.createNoteForBook(bookId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Reading note created successfully", response));
    }

    @PostMapping("/quotes/{quoteId}/notes")
    @Operation(summary = "为金句创建感悟", description = "为指定金句创建读书感悟")
    public ResponseEntity<ApiResponse<ReadingNoteResponse>> createNoteForQuote(
            @PathVariable Long quoteId,
            @Valid @RequestBody ReadingNoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReadingNoteResponse response = readingNoteService.createNoteForQuote(quoteId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Reading note created successfully", response));
    }

    @GetMapping("/books/{bookId}/notes")
    @Operation(summary = "获取书籍的所有感悟", description = "获取指定书籍的所有读书感悟（分页）")
    public ResponseEntity<ApiResponse<Page<ReadingNoteResponse>>> getNotesByBookId(
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
        Page<ReadingNoteResponse> notes = readingNoteService.getNotesByBookId(bookId, pageable, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(notes));
    }

    @GetMapping("/quotes/{quoteId}/notes")
    @Operation(summary = "获取金句的所有感悟", description = "获取指定金句的所有读书感悟")
    public ResponseEntity<ApiResponse<List<ReadingNoteResponse>>> getNotesByQuoteId(
            @PathVariable Long quoteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReadingNoteResponse> notes = readingNoteService.getNotesByQuoteId(quoteId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(notes));
    }

    @GetMapping("/notes/{id}")
    @Operation(summary = "获取感悟详情", description = "根据ID获取读书感悟详情")
    public ResponseEntity<ApiResponse<ReadingNoteResponse>> getNoteById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReadingNoteResponse response = readingNoteService.getNoteById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/notes/{id}")
    @Operation(summary = "更新感悟", description = "更新指定的读书感悟")
    public ResponseEntity<ApiResponse<ReadingNoteResponse>> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody ReadingNoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReadingNoteResponse response = readingNoteService.updateNote(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Reading note updated successfully", response));
    }

    @DeleteMapping("/notes/{id}")
    @Operation(summary = "删除感悟", description = "删除指定的读书感悟")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        readingNoteService.deleteNote(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Reading note deleted successfully"));
    }

    @GetMapping("/notes/search")
    @Operation(summary = "搜索感悟", description = "根据关键词搜索读书感悟")
    public ResponseEntity<ApiResponse<Page<ReadingNoteResponse>>> searchNotes(
            @Parameter(description = "搜索关键词") @RequestParam(required = false, defaultValue = "") String keyword,
            @Parameter(description = "感悟类型") @RequestParam(required = false) String noteType,
            @Parameter(description = "标签") @RequestParam(required = false) String tag,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ReadingNoteResponse> notes;
        if (noteType != null || tag != null) {
            com.bookrecord.entity.ReadingNote.NoteType typeEnum = null;
            if (noteType != null && !noteType.isEmpty()) {
                try {
                    typeEnum = com.bookrecord.entity.ReadingNote.NoteType.valueOf(noteType);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Invalid note type: " + noteType));
                }
            }
            notes = readingNoteService.searchNotesWithFilters(keyword, typeEnum, tag, userDetails.getUsername(), pageable);
        } else {
            notes = readingNoteService.searchNotes(keyword, userDetails.getUsername(), pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(notes));
    }
}