package com.bookrecord.service;

import com.bookrecord.dto.AnnualStatistics;
import com.bookrecord.dto.CategoryStatistics;
import com.bookrecord.dto.EfficiencyStatistics;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    // ==================== 年度统计相关方法 ====================

    /**
     * 获取年度阅读报告统计数据
     * 包括完成书籍数、新增书籍数、阅读时长估算、感悟金句数、阅读时间分布、关键词等
     *
     * @param username 当前登录用户名
     * @param year 统计年份
     * @return AnnualStatistics 年度统计数据
     */
    @Transactional(readOnly = true)
    public AnnualStatistics getAnnualStatistics(String username, Integer year) {
        log.info("获取用户 {} 的年度阅读报告，年份：{}", username, year);

        // 获取当前用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 如果未指定年份，使用当前年份
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        log.debug("统计年份：{}", year);

        // 1. 统计年度完成书籍数
        List<Book> completedBooks = bookRepository.findCompletedBooksByUserAndYear(user, year);
        Long completedBooksCount = (long) completedBooks.size();
        log.debug("年度完成书籍数: {}", completedBooksCount);

        // 2. 统计年度新增书籍数
        List<Book> newBooks = bookRepository.findByUserAndYear(user, year);
        Long newBooksCount = (long) newBooks.size();
        log.debug("年度新增书籍数: {}", newBooksCount);

        // 3. 计算年度总阅读页数和估算时长
        Long totalPagesRead = completedBooks.stream()
                .filter(book -> book.getPageCount() != null)
                .mapToLong(Book::getPageCount)
                .sum();
        // 假设平均阅读速度为 30 页/小时
        Long estimatedReadingHours = totalPagesRead / 30;
        log.debug("年度总阅读页数: {}, 估算时长: {} 小时", totalPagesRead, estimatedReadingHours);

        // 4. 统计年度新增感悟和金句数
        Long notesCount = readingNoteRepository.countByUserAndYear(user, year);
        Long quotesCount = quoteRepository.countByUserAndYear(user, year);
        log.debug("年度感悟数: {}, 金句数: {}", notesCount, quotesCount);

        // 5. 计算阅读时间段分布
        List<AnnualStatistics.ReadingTimeDistribution> readingTimeDistribution =
                calculateReadingTimeDistribution(user, year);

        // 6. 提取年度关键词
        List<AnnualStatistics.Keyword> topKeywords = extractTopKeywords(user, year);
        log.debug("年度关键词数量: {}", topKeywords.size());

        // 7. 获取年度最佳书籍
        AnnualStatistics.BookHighlight bookOfTheYear = bookRepository
                .findTopRatedBookByUserAndYear(user, year)
                .map(this::convertToBookHighlight)
                .orElse(null);

        // 8. 获取年度最长书籍
        AnnualStatistics.BookHighlight longestBook = bookRepository
                .findLongestBookByUserAndYear(user, year)
                .map(this::convertToBookHighlight)
                .orElse(null);

        log.info("年度阅读报告统计完成，年份：{}", year);

        return AnnualStatistics.builder()
                .year(year)
                .completedBooksCount(completedBooksCount)
                .newBooksCount(newBooksCount)
                .totalPagesRead(totalPagesRead)
                .estimatedReadingHours(estimatedReadingHours)
                .notesCount(notesCount)
                .quotesCount(quotesCount)
                .readingTimeDistribution(readingTimeDistribution)
                .topKeywords(topKeywords)
                .bookOfTheYear(bookOfTheYear)
                .longestBook(longestBook)
                .build();
    }

    /**
     * 计算阅读时间段分布
     * 根据感悟和金句的创建时间，统计用户最活跃的阅读时段
     *
     * @param user 用户实体
     * @param year 年份
     * @return List<ReadingTimeDistribution> 时间段分布列表
     */
    private List<AnnualStatistics.ReadingTimeDistribution> calculateReadingTimeDistribution(User user, Integer year) {
        // 统计各时间段的记录数量
        Map<String, Integer> periodCount = new LinkedHashMap<>();
        periodCount.put("morning", 0);      // 早晨 6-12点
        periodCount.put("afternoon", 0);    // 下午 12-18点
        periodCount.put("evening", 0);      // 晚上 18-24点
        periodCount.put("night", 0);        // 深夜 0-6点

        // 时间段显示名称映射
        Map<String, String> periodLabels = new LinkedHashMap<>();
        periodLabels.put("morning", "早晨 (6:00-12:00)");
        periodLabels.put("afternoon", "下午 (12:00-18:00)");
        periodLabels.put("evening", "晚上 (18:00-24:00)");
        periodLabels.put("night", "深夜 (0:00-6:00)");

        // 从感悟中统计
        List<ReadingNote> notes = readingNoteRepository.findByUserAndYear(user, year);
        for (ReadingNote note : notes) {
            int hour = note.getCreatedAt().getHour();
            String period = getTimePeriod(hour);
            periodCount.merge(period, 1, Integer::sum);
        }

        // 从金句中统计
        List<Quote> quotes = quoteRepository.findByUserAndYear(user, year);
        for (Quote quote : quotes) {
            int hour = quote.getCreatedAt().getHour();
            String period = getTimePeriod(hour);
            periodCount.merge(period, 1, Integer::sum);
        }

        // 构建返回结果
        List<AnnualStatistics.ReadingTimeDistribution> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : periodCount.entrySet()) {
            result.add(AnnualStatistics.ReadingTimeDistribution.builder()
                    .period(entry.getKey())
                    .label(periodLabels.get(entry.getKey()))
                    .count(entry.getValue())
                    .build());
        }

        // 按数量降序排序
        result.sort((a, b) -> b.getCount().compareTo(a.getCount()));

        return result;
    }

    /**
     * 根据小时数获取时间段标识
     *
     * @param hour 小时数（0-23）
     * @return String 时间段标识
     */
    private String getTimePeriod(int hour) {
        if (hour >= 6 && hour < 12) {
            return "morning";
        } else if (hour >= 12 && hour < 18) {
            return "afternoon";
        } else if (hour >= 18 && hour < 24) {
            return "evening";
        } else {
            return "night";
        }
    }

    /**
     * 提取年度关键词
     * 从感悟标题、内容和金句内容中提取高频词
     *
     * @param user 用户实体
     * @param year 年份
     * @return List<Keyword> 关键词列表（最多返回10个）
     */
    private List<AnnualStatistics.Keyword> extractTopKeywords(User user, Integer year) {
        // 关键词频率统计 Map
        Map<String, Integer> keywordCount = new HashMap<>();

        // 中文分词正则（简单实现：提取2-4字的中文词组）
        Pattern chinesePattern = Pattern.compile("[\\u4e00-\\u9fa5]{2,4}");

        // 从感悟中提取关键词
        List<ReadingNote> notes = readingNoteRepository.findByUserAndYear(user, year);
        for (ReadingNote note : notes) {
            // 从标题提取
            if (note.getTitle() != null) {
                extractKeywordsFromText(note.getTitle(), chinesePattern, keywordCount);
            }
            // 从内容提取
            if (note.getContent() != null) {
                extractKeywordsFromText(note.getContent(), chinesePattern, keywordCount);
            }
        }

        // 从金句中提取关键词
        List<Quote> quotes = quoteRepository.findByUserAndYear(user, year);
        for (Quote quote : quotes) {
            if (quote.getContent() != null) {
                extractKeywordsFromText(quote.getContent(), chinesePattern, keywordCount);
            }
        }

        // 过滤停用词（简单的停用词列表）
        Set<String> stopWords = getStopWords();
        keywordCount.keySet().removeAll(stopWords);

        // 按频率排序，取前10个
        return keywordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> AnnualStatistics.Keyword.builder()
                        .word(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 从文本中提取关键词
     *
     * @param text 文本内容
     * @param pattern 正则模式
     * @param keywordCount 关键词计数 Map
     */
    private void extractKeywordsFromText(String text, Pattern pattern, Map<String, Integer> keywordCount) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String word = matcher.group();
            keywordCount.merge(word, 1, Integer::sum);
        }
    }

    /**
     * 获取停用词列表
     * 用于过滤关键词提取中的常见无意义词
     *
     * @return Set<String> 停用词集合
     */
    private Set<String> getStopWords() {
        // 常见中文停用词
        return new HashSet<>(Arrays.asList(
                "不是", "就是", "一个", "这个", "那个", "什么", "没有", "可以", "已经",
                "还是", "但是", "因为", "所以", "如果", "虽然", "而且", "或者", "然后",
                "我们", "他们", "自己", "一些", "这些", "那些", "这样", "那样", "怎样",
                "时候", "地方", "东西", "方面", "问题", "情况", "现在", "今天", "昨天",
                "明天", "以后", "以前", "之间", "之中", "里面", "外面", "上面", "下面",
                "开始", "最后", "一直", "一起", "一下", "一点", "一些", "一切", "所有"
        ));
    }

    /**
     * 将书籍实体转换为亮点数据
     *
     * @param book 书籍实体
     * @return BookHighlight 书籍亮点数据
     */
    private AnnualStatistics.BookHighlight convertToBookHighlight(Book book) {
        return AnnualStatistics.BookHighlight.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .totalPages(book.getPageCount())
                .rating(book.getRating() != null ? book.getRating().intValue() : null)
                .build();
    }

    // ==================== 阅读效率分析相关方法 ====================

    /**
     * 获取阅读效率分析数据
     * 计算平均阅读速度、完成天数、记录频率等效率指标
     *
     * @param username 当前登录用户名
     * @return EfficiencyStatistics 效率统计数据
     */
    @Transactional(readOnly = true)
    public EfficiencyStatistics getEfficiencyStatistics(String username) {
        log.info("获取用户 {} 的阅读效率分析", username);

        // 获取当前用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 获取所有已完成的书籍
        List<Book> completedBooks = bookRepository.findByUserAndReadingStatus(user, Book.ReadingStatus.COMPLETED);
        Long completedBooksCount = (long) completedBooks.size();

        // 计算平均阅读速度（页/天）
        Double averageReadingSpeed = calculateAverageReadingSpeed(completedBooks);
        log.debug("平均阅读速度: {} 页/天", averageReadingSpeed);

        // 计算平均完成天数
        Double averageCompletionDays = calculateAverageCompletionDays(completedBooks);
        log.debug("平均完成天数: {} 天", averageCompletionDays);

        // 计算统计周期（从用户第一条记录开始到现在）
        Integer statisticsDays = calculateStatisticsDays(user);
        double statisticsWeeks = statisticsDays / 7.0;

        // 计算进度更新频率（次/周）
        // 由于没有进度更新历史记录，使用当前在读书籍数量作为替代指标
        List<Book> readingBooks = bookRepository.findByUserAndReadingStatus(user, Book.ReadingStatus.READING);
        Double progressUpdateFrequency = readingBooks.isEmpty() ? 0.0 : readingBooks.size() / statisticsWeeks;

        // 计算感悟记录频率（次/周）
        Long totalNotes = readingNoteRepository.countByUserAndYear(user, LocalDate.now().getYear());
        // 获取全部感悟数（非当年）
        Long allNotes = (long) readingNoteRepository.findByUser(user).size();
        Double noteRecordFrequency = statisticsWeeks > 0 ? allNotes / statisticsWeeks : 0.0;

        // 计算金句记录频率（次/周）
        Long allQuotes = (long) quoteRepository.findByUser(user).size();
        Double quoteRecordFrequency = statisticsWeeks > 0 ? allQuotes / statisticsWeeks : 0.0;

        log.debug("感悟记录频率: {} 次/周, 金句记录频率: {} 次/周", noteRecordFrequency, quoteRecordFrequency);

        // 生成阅读建议
        List<EfficiencyStatistics.ReadingAdvice> advices = generateReadingAdvices(
                averageReadingSpeed, averageCompletionDays, noteRecordFrequency, completedBooksCount);

        return EfficiencyStatistics.builder()
                .averageReadingSpeed(averageReadingSpeed)
                .averageCompletionDays(averageCompletionDays)
                .progressUpdateFrequency(progressUpdateFrequency)
                .noteRecordFrequency(noteRecordFrequency)
                .quoteRecordFrequency(quoteRecordFrequency)
                .statisticsDays(statisticsDays)
                .completedBooksCount(completedBooksCount)
                .readingAdvices(advices)
                .build();
    }

    /**
     * 计算平均阅读速度
     * 基于有开始日期和完成日期的书籍，计算总页数/总天数
     *
     * @param completedBooks 已完成的书籍列表
     * @return Double 平均阅读速度（页/天）
     */
    private Double calculateAverageReadingSpeed(List<Book> completedBooks) {
        // 筛选有开始日期、完成日期和页数的书籍
        List<Book> validBooks = completedBooks.stream()
                .filter(book -> book.getStartDate() != null
                        && book.getFinishDate() != null
                        && book.getPageCount() != null
                        && book.getPageCount() > 0)
                .collect(Collectors.toList());

        if (validBooks.isEmpty()) {
            return 0.0;
        }

        // 计算总页数和总天数
        long totalPages = validBooks.stream()
                .mapToLong(Book::getPageCount)
                .sum();

        long totalDays = validBooks.stream()
                .mapToLong(book -> java.time.temporal.ChronoUnit.DAYS.between(
                        book.getStartDate(), book.getFinishDate()) + 1)
                .sum();

        // 避免除零
        if (totalDays == 0) {
            return 0.0;
        }

        return (double) totalPages / totalDays;
    }

    /**
     * 计算平均完成一本书的天数
     *
     * @param completedBooks 已完成的书籍列表
     * @return Double 平均完成天数
     */
    private Double calculateAverageCompletionDays(List<Book> completedBooks) {
        // 筛选有开始日期和完成日期的书籍
        List<Book> validBooks = completedBooks.stream()
                .filter(book -> book.getStartDate() != null && book.getFinishDate() != null)
                .collect(Collectors.toList());

        if (validBooks.isEmpty()) {
            return 0.0;
        }

        // 计算平均天数
        long totalDays = validBooks.stream()
                .mapToLong(book -> java.time.temporal.ChronoUnit.DAYS.between(
                        book.getStartDate(), book.getFinishDate()) + 1)
                .sum();

        return (double) totalDays / validBooks.size();
    }

    /**
     * 计算统计周期天数
     * 从用户第一条书籍记录或感悟记录开始到现在
     *
     * @param user 用户实体
     * @return Integer 统计天数
     */
    private Integer calculateStatisticsDays(User user) {
        LocalDateTime earliestDate = null;

        // 获取最早的书籍创建时间
        List<Book> books = bookRepository.findByUser(user);
        if (!books.isEmpty()) {
            earliestDate = books.stream()
                    .map(Book::getCreatedAt)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
        }

        // 获取最早的感悟创建时间
        List<ReadingNote> notes = readingNoteRepository.findByUser(user);
        if (!notes.isEmpty()) {
            LocalDateTime earliestNote = notes.stream()
                    .map(ReadingNote::getCreatedAt)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
            if (earliestDate == null || (earliestNote != null && earliestNote.isBefore(earliestDate))) {
                earliestDate = earliestNote;
            }
        }

        // 如果没有记录，默认30天
        if (earliestDate == null) {
            return 30;
        }

        // 计算天数
        long days = java.time.temporal.ChronoUnit.DAYS.between(earliestDate, LocalDateTime.now()) + 1;
        return Math.max((int) days, 1);
    }

    /**
     * 生成阅读建议
     * 根据用户的阅读数据生成个性化建议
     *
     * @param speed 平均阅读速度
     * @param completionDays 平均完成天数
     * @param noteFrequency 感悟记录频率
     * @param completedCount 已完成书籍数
     * @return List<ReadingAdvice> 阅读建议列表
     */
    private List<EfficiencyStatistics.ReadingAdvice> generateReadingAdvices(
            Double speed, Double completionDays, Double noteFrequency, Long completedCount) {

        List<EfficiencyStatistics.ReadingAdvice> advices = new ArrayList<>();

        // 阅读速度建议
        if (speed > 0 && speed < 10) {
            advices.add(EfficiencyStatistics.ReadingAdvice.builder()
                    .type("speed")
                    .icon("🐢")
                    .title("放慢脚步")
                    .content("您的阅读速度较慢，可能是书籍内容较深。建议做好笔记，深入理解比速度更重要。")
                    .build());
        } else if (speed > 50) {
            advices.add(EfficiencyStatistics.ReadingAdvice.builder()
                    .type("speed")
                    .icon("🚀")
                    .title("阅读达人")
                    .content("您的阅读速度很快！建议在追求速度的同时，也留出时间思考和记录感悟。")
                    .build());
        }

        // 记录频率建议
        if (noteFrequency < 0.5) {
            advices.add(EfficiencyStatistics.ReadingAdvice.builder()
                    .type("frequency")
                    .icon("✏️")
                    .title("多记感悟")
                    .content("记录感悟的频率较低。建议养成边读边记的习惯，这能帮助加深理解和记忆。")
                    .build());
        } else if (noteFrequency >= 3) {
            advices.add(EfficiencyStatistics.ReadingAdvice.builder()
                    .type("frequency")
                    .icon("📝")
                    .title("记录达人")
                    .content("您的记录频率很高，这是很好的阅读习惯！继续保持，这些笔记将成为宝贵的知识财富。")
                    .build());
        }

        // 阅读习惯建议
        if (completedCount >= 10) {
            advices.add(EfficiencyStatistics.ReadingAdvice.builder()
                    .type("habit")
                    .icon("📚")
                    .title("阅读丰硕")
                    .content("您已完成 " + completedCount + " 本书，阅读量可观！建议定期回顾笔记，温故知新。")
                    .build());
        } else if (completedCount < 3) {
            advices.add(EfficiencyStatistics.ReadingAdvice.builder()
                    .type("habit")
                    .icon("🌱")
                    .title("阅读起步")
                    .content("阅读之旅刚刚开始，建议选择感兴趣的书籍，养成每天阅读的习惯。")
                    .build());
        }

        // 如果没有特定建议，给出鼓励
        if (advices.isEmpty()) {
            advices.add(EfficiencyStatistics.ReadingAdvice.builder()
                    .type("encouragement")
                    .icon("💪")
                    .title("继续保持")
                    .content("您的阅读习惯良好，继续保持！定期记录感悟，让阅读更有价值。")
                    .build());
        }

        return advices;
    }
}