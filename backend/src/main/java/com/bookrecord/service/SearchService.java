package com.bookrecord.service;

import com.bookrecord.dto.AdvancedSearchRequest;
import com.bookrecord.dto.SearchResult;
import com.bookrecord.entity.Book;
import com.bookrecord.entity.Quote;
import com.bookrecord.entity.ReadingNote;
import com.bookrecord.entity.User;
import com.bookrecord.repository.BookRepository;
import com.bookrecord.repository.QuoteRepository;
import com.bookrecord.repository.ReadingNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务类
 * 提供统一搜索和高级搜索功能，支持跨书籍、感悟、金句的全文检索
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final BookRepository bookRepository;
    private final ReadingNoteRepository readingNoteRepository;
    private final QuoteRepository quoteRepository;
    private final UserService userService;

    /**
     * 每种类型返回的最大结果数量
     */
    private static final int MAX_RESULTS_PER_TYPE = 10;

    /**
     * 执行基础搜索
     * 在书籍、感悟和金句中搜索关键词
     *
     * @param keyword 搜索关键词
     * @param username 当前用户名
     * @return SearchResult 包含所有搜索结果
     */
    @Transactional(readOnly = true)
    public SearchResult search(String keyword, String username) {
        log.info("搜索关键词: {} 用户: {}", keyword, username);

        User user = userService.getUserEntity(username);
        String searchKeyword = keyword != null ? keyword.trim() : "";

        // 如果关键词为空，返回空结果
        if (searchKeyword.isEmpty()) {
            return SearchResult.builder()
                    .books(new ArrayList<>())
                    .notes(new ArrayList<>())
                    .quotes(new ArrayList<>())
                    .totalBooks(0)
                    .totalNotes(0)
                    .totalQuotes(0)
                    .totalResults(0)
                    .build();
        }

        Pageable limit = PageRequest.of(0, MAX_RESULTS_PER_TYPE);

        // 搜索书籍
        List<SearchResult.BookResult> books = bookRepository.searchByKeyword(user, searchKeyword, limit)
                .stream()
                .map(book -> toBookResult(book, searchKeyword))
                .collect(Collectors.toList());

        // 搜索感悟
        List<SearchResult.NoteResult> notes = readingNoteRepository.searchByKeyword(user, searchKeyword, limit)
                .stream()
                .map(note -> toNoteResult(note, searchKeyword))
                .collect(Collectors.toList());

        // 搜索金句
        List<SearchResult.QuoteResult> quotes = quoteRepository.searchByKeyword(user, searchKeyword, limit)
                .stream()
                .map(quote -> toQuoteResult(quote, searchKeyword))
                .collect(Collectors.toList());

        int totalBooks = books.size();
        int totalNotes = notes.size();
        int totalQuotes = quotes.size();

        return SearchResult.builder()
                .books(books)
                .notes(notes)
                .quotes(quotes)
                .totalBooks(totalBooks)
                .totalNotes(totalNotes)
                .totalQuotes(totalQuotes)
                .totalResults(totalBooks + totalNotes + totalQuotes)
                .build();
    }

    /**
     * 执行高级搜索
     * 支持多条件组合查询，包括时间范围、阅读状态、感悟类型、标签等筛选条件
     *
     * @param request 高级搜索请求参数
     * @param username 当前用户名
     * @return SearchResult 包含符合条件的结果
     */
    @Transactional(readOnly = true)
    public SearchResult advancedSearch(AdvancedSearchRequest request, String username) {
        log.info("高级搜索: 关键词={}, 类型={}, 用户={}", request.getKeyword(), request.getType(), username);

        User user = userService.getUserEntity(username);
        String keyword = request.getKeyword() != null ? request.getKeyword().trim() : "";

        // 获取搜索类型，默认为 all
        String type = request.getType() != null ? request.getType().toLowerCase() : "all";

        // 创建分页参数
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // 初始化结果列表
        List<SearchResult.BookResult> books = new ArrayList<>();
        List<SearchResult.NoteResult> notes = new ArrayList<>();
        List<SearchResult.QuoteResult> quotes = new ArrayList<>();

        // 根据搜索类型执行搜索
        boolean searchBooks = "all".equals(type) || "books".equals(type);
        boolean searchNotes = "all".equals(type) || "notes".equals(type);
        boolean searchQuotes = "all".equals(type) || "quotes".equals(type);

        // 搜索书籍
        if (searchBooks) {
            books = searchBooksWithFilters(user, keyword, request, pageable);
        }

        // 搜索感悟
        if (searchNotes) {
            notes = searchNotesWithFilters(user, keyword, request, pageable);
        }

        // 搜索金句
        if (searchQuotes) {
            quotes = searchQuotesWithFilters(user, keyword, request, pageable);
        }

        // 构建并返回搜索结果
        return SearchResult.builder()
                .books(books)
                .notes(notes)
                .quotes(quotes)
                .totalBooks(books.size())
                .totalNotes(notes.size())
                .totalQuotes(quotes.size())
                .totalResults(books.size() + notes.size() + quotes.size())
                .build();
    }

    /**
     * 带筛选条件的书籍搜索
     * 支持关键词、阅读状态、时间范围筛选
     *
     * @param user 当前用户
     * @param keyword 搜索关键词
     * @param request 筛选条件
     * @param pageable 分页参数
     * @return 符合条件的书籍结果列表
     */
    private List<SearchResult.BookResult> searchBooksWithFilters(User user, String keyword,
            AdvancedSearchRequest request, Pageable pageable) {
        // 获取筛选参数
        List<String> statuses = request.getReadingStatuses();
        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();

        // 执行带筛选条件的搜索
        List<Book> bookList = bookRepository.advancedSearch(
                user,
                keyword.isEmpty() ? null : keyword,
                statuses,
                startDate,
                endDate,
                pageable
        );

        // 转换为结果对象
        return bookList.stream()
                .map(book -> toBookResult(book, keyword))
                .collect(Collectors.toList());
    }

    /**
     * 带筛选条件的感悟搜索
     * 支持关键词、感悟类型、标签、时间范围筛选
     *
     * @param user 当前用户
     * @param keyword 搜索关键词
     * @param request 筛选条件
     * @param pageable 分页参数
     * @return 符合条件的感悟结果列表
     */
    private List<SearchResult.NoteResult> searchNotesWithFilters(User user, String keyword,
            AdvancedSearchRequest request, Pageable pageable) {
        // 获取筛选参数
        List<String> noteTypes = request.getNoteTypes();
        List<String> tags = request.getTags();
        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();

        // 执行带筛选条件的搜索
        List<ReadingNote> noteList = readingNoteRepository.advancedSearch(
                user,
                keyword.isEmpty() ? null : keyword,
                noteTypes,
                tags,
                startDate,
                endDate,
                pageable
        );

        // 转换为结果对象
        return noteList.stream()
                .map(note -> toNoteResult(note, keyword))
                .collect(Collectors.toList());
    }

    /**
     * 带筛选条件的金句搜索
     * 支持关键词、颜色、标签、时间范围筛选
     *
     * @param user 当前用户
     * @param keyword 搜索关键词
     * @param request 筛选条件
     * @param pageable 分页参数
     * @return 符合条件的金句结果列表
     */
    private List<SearchResult.QuoteResult> searchQuotesWithFilters(User user, String keyword,
            AdvancedSearchRequest request, Pageable pageable) {
        // 获取筛选参数
        List<String> colors = request.getColors();
        List<String> tags = request.getTags();
        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();

        // 执行带筛选条件的搜索
        List<Quote> quoteList = quoteRepository.advancedSearch(
                user,
                keyword.isEmpty() ? null : keyword,
                colors,
                tags,
                startDate,
                endDate,
                pageable
        );

        // 转换为结果对象
        return quoteList.stream()
                .map(quote -> toQuoteResult(quote, keyword))
                .collect(Collectors.toList());
    }

    /**
     * 将书籍实体转换为搜索结果对象
     *
     * @param book 书籍实体
     * @param keyword 搜索关键词（用于高亮）
     * @return BookResult 书籍搜索结果
     */
    private SearchResult.BookResult toBookResult(Book book, String keyword) {
        return SearchResult.BookResult.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .coverUrl(book.getCoverUrl())
                .readingStatus(book.getReadingStatus() != null ? book.getReadingStatus().name() : null)
                .progress(calculateProgress(book))
                .highlightedTitle(highlightKeyword(book.getTitle(), keyword))
                .highlightedAuthor(highlightKeyword(book.getAuthor(), keyword))
                .build();
    }

    /**
     * 将感悟实体转换为搜索结果对象
     *
     * @param note 感悟实体
     * @param keyword 搜索关键词（用于高亮）
     * @return NoteResult 感悟搜索结果
     */
    private SearchResult.NoteResult toNoteResult(ReadingNote note, String keyword) {
        return SearchResult.NoteResult.builder()
                .id(note.getId())
                .bookId(note.getBook().getId())
                .bookTitle(note.getBook().getTitle())
                .title(note.getTitle())
                .content(truncateContent(note.getContent(), 200))
                .noteType(note.getNoteType() != null ? note.getNoteType().name() : null)
                .highlightedTitle(highlightKeyword(note.getTitle(), keyword))
                .highlightedContent(highlightKeyword(note.getContent(), keyword))
                .build();
    }

    /**
     * 将金句实体转换为搜索结果对象
     *
     * @param quote 金句实体
     * @param keyword 搜索关键词（用于高亮）
     * @return QuoteResult 金句搜索结果
     */
    private SearchResult.QuoteResult toQuoteResult(Quote quote, String keyword) {
        return SearchResult.QuoteResult.builder()
                .id(quote.getId())
                .bookId(quote.getBook().getId())
                .bookTitle(quote.getBook().getTitle())
                .content(truncateContent(quote.getContent(), 200))
                .color(quote.getColor())
                .highlightedContent(highlightKeyword(quote.getContent(), keyword))
                .build();
    }

    /**
     * 计算书籍阅读进度百分比
     *
     * @param book 书籍实体
     * @return 阅读进度百分比（0-100），如果没有页数信息则返回null
     */
    private Integer calculateProgress(Book book) {
        if (book.getPageCount() == null || book.getPageCount() == 0) {
            return null;
        }
        return (int) ((book.getCurrentPage() * 100.0) / book.getPageCount());
    }

    /**
     * 截断文本内容
     * 当内容超过最大长度时，截断并添加省略号
     *
     * @param content 原始内容
     * @param maxLength 最大长度
     * @return 截断后的内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return null;
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    /**
     * 高亮显示关键词
     * 在文本中匹配的关键词周围添加 &lt;mark&gt; 标签
     *
     * @param text 原始文本
     * @param keyword 关键词
     * @return 带高亮标记的文本
     */
    private String highlightKeyword(String text, String keyword) {
        if (text == null || keyword == null || keyword.isEmpty()) {
            return text;
        }

        // 不区分大小写的替换
        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();

        StringBuilder result = new StringBuilder();
        int start = 0;
        int index;

        // 循环查找并替换所有匹配的关键词
        while ((index = lowerText.indexOf(lowerKeyword, start)) != -1) {
            result.append(text, start, index);
            result.append("<mark>");
            result.append(text, index, index + keyword.length());
            result.append("</mark>");
            start = index + keyword.length();
        }
        result.append(text.substring(start));

        return result.toString();
    }
}