package com.bookrecord.service;

import com.bookrecord.dto.CategoryStatistics;
import com.bookrecord.dto.TrendStatistics;
import com.bookrecord.entity.Book;
import com.bookrecord.entity.Quote;
import com.bookrecord.entity.ReadingNote;
import com.bookrecord.entity.User;
import com.bookrecord.repository.BookRepository;
import com.bookrecord.repository.QuoteRepository;
import com.bookrecord.repository.ReadingNoteRepository;
import com.bookrecord.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务类
 * 提供阅读统计数据计算功能，包括趋势分析、分类统计等
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReadingNoteRepository readingNoteRepository;
    private final QuoteRepository quoteRepository;

    /**
     * 获取阅读趋势统计数据
     * 按月份统计新增书籍和完成书籍数量
     *
     * @param username 当前登录用户名
     * @param range 时间范围：6m（近6个月）、1y（近1年）、all（全部）
     * @return TrendStatistics 趋势统计数据
     */
    @Transactional(readOnly = true)
    public TrendStatistics getTrendStatistics(String username, String range) {
        log.info("获取用户 {} 的阅读趋势统计，时间范围：{}", username, range);

        // 获取当前用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 计算时间范围
        LocalDateTime startDate = calculateStartDate(range);
        LocalDateTime endDate = LocalDateTime.now();

        log.debug("统计时间范围：{} 至 {}", startDate, endDate);

        // 获取时间范围内的所有书籍
        List<Book> books = bookRepository.findByUserAndCreatedAtBetween(user, startDate, endDate);

        // 获取时间范围内完成的书籍（通过finishDate筛选）
        List<Book> completedBooks = bookRepository.findByUserAndReadingStatusAndFinishDateBetween(
                user, Book.ReadingStatus.COMPLETED, startDate.toLocalDate(), endDate.toLocalDate());

        log.debug("时间范围内新增书籍数: {}", books.size());
        log.debug("时间范围内完成书籍数: {}", completedBooks.size());

        // 按月份分组统计
        List<TrendStatistics.MonthlyData> monthlyDataList = calculateMonthlyData(
                books, completedBooks, startDate, endDate);

        log.info("趋势统计完成，共 {} 个月份的数据", monthlyDataList.size());

        return TrendStatistics.builder()
                .monthlyData(monthlyDataList)
                .build();
    }

    /**
     * 计算统计起始时间
     * 根据时间范围参数计算对应的起始日期
     *
     * @param range 时间范围参数
     * @return LocalDateTime 起始时间
     */
    private LocalDateTime calculateStartDate(String range) {
        LocalDate today = LocalDate.now();

        switch (range.toLowerCase()) {
            case "6m":
                // 近6个月：从5个月前的第一天开始
                return today.minusMonths(5).withDayOfMonth(1).atStartOfDay();
            case "1y":
                // 近1年：从11个月前的第一天开始
                return today.minusMonths(11).withDayOfMonth(1).atStartOfDay();
            case "all":
            default:
                // 全部：使用最早可能的日期
                return LocalDateTime.of(2000, 1, 1, 0, 0);
        }
    }

    /**
     * 计算每月统计数据
     * 按月份分组统计新增书籍和完成书籍数量
     *
     * @param books 时间范围内新增的所有书籍
     * @param completedBooks 时间范围内完成的书籍
     * @param startDate 统计起始时间
     * @param endDate 统计结束时间
     * @return List<MonthlyData> 月度统计数据列表
     */
    private List<TrendStatistics.MonthlyData> calculateMonthlyData(
            List<Book> books,
            List<Book> completedBooks,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        List<TrendStatistics.MonthlyData> result = new ArrayList<>();

        // 生成月份序列
        List<YearMonth> months = generateMonthSeries(startDate, endDate);

        // 按月份分组新增书籍
        Map<String, Long> newBooksByMonth = books.stream()
                .collect(Collectors.groupingBy(
                        book -> YearMonth.from(book.getCreatedAt()).toString(),
                        Collectors.counting()
                ));

        // 按月份分组完成书籍（使用finishDate）
        Map<String, Long> completedBooksByMonth = completedBooks.stream()
                .filter(book -> book.getFinishDate() != null)
                .collect(Collectors.groupingBy(
                        book -> YearMonth.from(book.getFinishDate()).toString(),
                        Collectors.counting()
                ));

        // 构建每个月的数据
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");

        for (YearMonth month : months) {
            String monthKey = month.toString();

            TrendStatistics.MonthlyData data = TrendStatistics.MonthlyData.builder()
                    .month(monthKey)
                    .label(month.format(labelFormatter))
                    .newBooks(newBooksByMonth.getOrDefault(monthKey, 0L))
                    .completedBooks(completedBooksByMonth.getOrDefault(monthKey, 0L))
                    .build();

            result.add(data);
        }

        return result;
    }

    /**
     * 生成月份序列
     * 从起始日期到结束日期，生成连续的月份列表
     *
     * @param startDate 起始时间
     * @param endDate 结束时间
     * @return List<YearMonth> 月份列表
     */
    private List<YearMonth> generateMonthSeries(LocalDateTime startDate, LocalDateTime endDate) {
        List<YearMonth> months = new ArrayList<>();

        YearMonth start = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);

        while (!start.isAfter(end)) {
            months.add(start);
            start = start.plusMonths(1);
        }

        return months;
    }

    /**
     * 获取分类统计数据
     * 包括阅读状态分布、感悟类型分布、热门标签统计
     *
     * @param username 当前登录用户名
     * @return CategoryStatistics 分类统计数据
     */
    @Transactional(readOnly = true)
    public CategoryStatistics getCategoryStatistics(String username) {
        log.info("获取用户 {} 的分类统计", username);

        // 获取当前用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 统计阅读状态分布
        List<CategoryStatistics.StatusDistribution> statusDistribution =
                calculateReadingStatusDistribution(user);

        // 统计感悟类型分布
        List<CategoryStatistics.NoteTypeDistribution> noteTypeDistribution =
                calculateNoteTypeDistribution(user);

        // 统计热门标签
        List<CategoryStatistics.TagFrequency> topTags =
                calculateTopTags(user);

        log.info("分类统计完成，状态分布 {} 项，感悟类型 {} 项，热门标签 {} 个",
                statusDistribution.size(), noteTypeDistribution.size(), topTags.size());

        return CategoryStatistics.builder()
                .readingStatusDistribution(statusDistribution)
                .noteTypeDistribution(noteTypeDistribution)
                .topTags(topTags)
                .build();
    }

    /**
     * 计算阅读状态分布
     * 统计各阅读状态的书籍数量
     *
     * @param user 用户实体
     * @return List<StatusDistribution> 状态分布列表
     */
    private List<CategoryStatistics.StatusDistribution> calculateReadingStatusDistribution(User user) {
        List<CategoryStatistics.StatusDistribution> result = new ArrayList<>();

        // 状态映射：编码 -> 显示名称
        Map<Book.ReadingStatus, String> statusLabels = new LinkedHashMap<>();
        statusLabels.put(Book.ReadingStatus.WANT_TO_READ, "想读");
        statusLabels.put(Book.ReadingStatus.READING, "在读");
        statusLabels.put(Book.ReadingStatus.COMPLETED, "已完成");
        statusLabels.put(Book.ReadingStatus.ABANDONED, "已放弃");

        // 统计各状态数量
        for (Map.Entry<Book.ReadingStatus, String> entry : statusLabels.entrySet()) {
            Long count = bookRepository.countByUserAndStatus(user, entry.getKey());
            result.add(CategoryStatistics.StatusDistribution.builder()
                    .status(entry.getKey().name())
                    .label(entry.getValue())
                    .count(count)
                    .build());
        }

        return result;
    }

    /**
     * 计算感悟类型分布
     * 统计各类型的感悟数量
     *
     * @param user 用户实体
     * @return List<NoteTypeDistribution> 感悟类型分布列表
     */
    private List<CategoryStatistics.NoteTypeDistribution> calculateNoteTypeDistribution(User user) {
        List<CategoryStatistics.NoteTypeDistribution> result = new ArrayList<>();

        // 类型映射：编码 -> 显示名称
        Map<ReadingNote.NoteType, String> typeLabels = new LinkedHashMap<>();
        typeLabels.put(ReadingNote.NoteType.THOUGHT, "思考");
        typeLabels.put(ReadingNote.NoteType.SUMMARY, "总结");
        typeLabels.put(ReadingNote.NoteType.QUESTION, "问题");
        typeLabels.put(ReadingNote.NoteType.INSIGHT, "洞察");

        // 统计各类型数量
        for (Map.Entry<ReadingNote.NoteType, String> entry : typeLabels.entrySet()) {
            Long count = readingNoteRepository.countByUserAndNoteType(user, entry.getKey());
            result.add(CategoryStatistics.NoteTypeDistribution.builder()
                    .type(entry.getKey().name())
                    .label(entry.getValue())
                    .count(count)
                    .build());
        }

        return result;
    }

    /**
     * 计算热门标签
     * 从感悟和金句中提取标签，按使用次数排序
     *
     * @param user 用户实体
     * @return List<TagFrequency> 热门标签列表（最多返回20个）
     */
    private List<CategoryStatistics.TagFrequency> calculateTopTags(User user) {
        // 统计标签频率的 Map
        Map<String, Integer> tagCount = new HashMap<>();

        // 从感悟中提取标签
        List<ReadingNote> notes = readingNoteRepository.findByUser(user);
        for (ReadingNote note : notes) {
            if (note.getTags() != null && !note.getTags().trim().isEmpty()) {
                String[] tags = note.getTags().split(",");
                for (String tag : tags) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        tagCount.merge(trimmedTag, 1, Integer::sum);
                    }
                }
            }
        }

        // 从金句中提取标签
        List<Quote> quotes = quoteRepository.findByUser(user);
        for (Quote quote : quotes) {
            if (quote.getTags() != null && !quote.getTags().trim().isEmpty()) {
                String[] tags = quote.getTags().split(",");
                for (String tag : tags) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        tagCount.merge(trimmedTag, 1, Integer::sum);
                    }
                }
            }
        }

        // 按使用次数排序，取前20个
        return tagCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .map(entry -> CategoryStatistics.TagFrequency.builder()
                        .tag(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}