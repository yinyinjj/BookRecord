package com.bookrecord.service;

import com.bookrecord.dto.*;
import com.bookrecord.entity.Book;
import com.bookrecord.entity.User;
import com.bookrecord.exception.BadRequestException;
import com.bookrecord.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 书籍导入服务
 * 负责解析外部书单文件（豆瓣、微信读书）并导入到系统中
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final BookService bookService;
    private final UserService userService;
    private final BookRepository bookRepository;

    // ==================== 文件解析 ====================

    /**
     * 解析上传的文件并返回预览数据
     * 自动检测文件格式（豆瓣/微信读书）
     *
     * @param file 上传的文件
     * @param username 当前用户名
     * @return 解析预览响应
     */
    public ImportPreviewResponse parseFile(MultipartFile file, String username) {
        log.info("开始解析导入文件: {}, 用户: {}", file.getOriginalFilename(), username);

        // 检测文件类型
        String sourceType = detectSourceType(file);
        log.info("检测到文件类型: {}", sourceType);

        // 根据文件类型解析
        List<ImportPreviewItem> items;
        if ("douban".equals(sourceType)) {
            items = parseDoubanCsv(file, username);
        } else if ("weread".equals(sourceType)) {
            items = parseWereadFile(file, username);
        } else {
            throw new BadRequestException("不支持的文件格式，请上传豆瓣或微信读书导出的书单文件");
        }

        // 统计数量
        int successCount = 0;
        int errorCount = 0;
        int duplicateCount = 0;

        for (ImportPreviewItem item : items) {
            if ("error".equals(item.getStatus())) {
                errorCount++;
            } else {
                successCount++;
                if (item.isDuplicate()) {
                    duplicateCount++;
                }
            }
        }

        log.info("文件解析完成: 成功={}, 失败={}, 重复={}", successCount, errorCount, duplicateCount);

        return ImportPreviewResponse.builder()
                .sourceType(sourceType)
                .successCount(successCount)
                .errorCount(errorCount)
                .duplicateCount(duplicateCount)
                .items(items)
                .fileName(file.getOriginalFilename())
                .build();
    }

    /**
     * 检测文件来源类型
     * 通过文件名和内容特征判断
     *
     * @param file 上传的文件
     * @return 来源类型（douban/weread/unknown）
     */
    private String detectSourceType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null) {
            // 豆瓣导出的文件名通常包含 "豆瓣" 或 "douban"
            if (filename.toLowerCase().contains("douban") || filename.contains("豆瓣")) {
                return "douban";
            }
            // 微信读书导出的文件名通常包含 "weread" 或 "微信读书"
            if (filename.toLowerCase().contains("weread") || filename.contains("微信读书")) {
                return "weread";
            }
        }

        // 尝试通过内容判断
        try {
            String content = readFirstLine(file);
            if (content != null) {
                // 豆瓣CSV的特征：包含"书名"、"作者"、"ISBN"等字段
                if (content.contains("书名") && content.contains("作者")) {
                    return "douban";
                }
                // 微信读书的特征：包含"书名"、"阅读进度"等
                if (content.contains("阅读进度") || content.contains("阅读时长")) {
                    return "weread";
                }
            }
        } catch (IOException e) {
            log.warn("无法读取文件内容进行类型检测", e);
        }

        // 默认尝试豆瓣格式
        return "douban";
    }

    /**
     * 读取文件第一行
     *
     * @param file 文件
     * @return 第一行内容
     */
    private String readFirstLine(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {
            return reader.readLine();
        }
    }

    /**
     * 检测文件编码
     *
     * @param file 文件
     * @return 字符编码
     */
    private Charset detectCharset(MultipartFile file) {
        // 中文环境常见的编码
        // 先尝试UTF-8，再尝试GBK
        return StandardCharsets.UTF_8;
    }

    // ==================== 豆瓣CSV解析 ====================

    /**
     * 解析豆瓣导出的CSV文件
     * 豆瓣CSV格式：
     * 书名,作者,ISBN,评分,阅读状态,标签,笔记
     *
     * 也支持简化格式（只有书名，每行一本书）：
     * 书名1
     * 书名2
     *
     * @param file CSV文件
     * @param username 当前用户名
     * @return 解析后的书籍列表
     */
    private List<ImportPreviewItem> parseDoubanCsv(MultipartFile file, String username) {
        List<ImportPreviewItem> items = new ArrayList<>();
        User user = userService.getUserEntity(username);

        // 尝试多种编码读取文件
        String content = tryReadWithEncodings(file);
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("文件内容为空或无法读取");
        }

        // 按行分割
        String[] lines = content.split("\\r?\\n");
        int lineNumber = 0;
        int titleIndex = -1, authorIndex = -1, isbnIndex = -1;
        int ratingIndex = -1, statusIndex = -1, tagsIndex = -1, notesIndex = -1;
        int publisherIndex = -1;
        boolean hasHeader = false;

        for (String line : lines) {
            lineNumber++;

            // 跳过空行
            if (line.trim().isEmpty()) {
                continue;
            }

            // 解析CSV行（处理引号内的逗号）
            String[] fields = parseCsvLine(line);

            // 第一行检测：判断是否有表头
            if (lineNumber == 1) {
                // 检查第一行是否包含表头关键字
                String firstField = fields[0].trim().replace("\uFEFF", "");
                if (isHeaderField(firstField)) {
                    // 这是表头行，解析列索引
                    hasHeader = true;
                    for (int i = 0; i < fields.length; i++) {
                        String field = fields[i].trim().replace("\uFEFF", ""); // 移除BOM
                        switch (field) {
                            case "书名":
                            case "title":
                            case "名称":
                                titleIndex = i;
                                break;
                            case "作者":
                            case "author":
                                authorIndex = i;
                                break;
                            case "ISBN":
                            case "isbn":
                                isbnIndex = i;
                                break;
                            case "评分":
                            case "rating":
                            case "我的评分":
                                ratingIndex = i;
                                break;
                            case "阅读状态":
                            case "status":
                            case "状态":
                                statusIndex = i;
                                break;
                            case "标签":
                            case "tags":
                                tagsIndex = i;
                                break;
                            case "笔记":
                            case "notes":
                            case "简介":
                                notesIndex = i;
                                break;
                            case "出版社":
                            case "publisher":
                                publisherIndex = i;
                                break;
                        }
                    }
                    log.info("豆瓣CSV列索引: 书名={}, 作者={}, ISBN={}, 评分={}, 状态={}, 标签={}",
                            titleIndex, authorIndex, isbnIndex, ratingIndex, statusIndex, tagsIndex);
                    continue;
                } else {
                    // 第一行就是数据，没有表头
                    hasHeader = false;
                    titleIndex = 0;
                    log.info("检测到无表头的简化CSV格式，第0列为书名");
                }
            }

            // 如果有表头但没找到书名列，跳过
            if (hasHeader && titleIndex < 0) {
                log.warn("未找到书名列，跳过第{}行", lineNumber);
                continue;
            }

            // 解析数据行
            try {
                ImportPreviewItem item = parseDoubanRow(fields,
                        titleIndex, authorIndex, isbnIndex, ratingIndex,
                        statusIndex, tagsIndex, notesIndex, publisherIndex, user);
                items.add(item);
            } catch (Exception e) {
                log.warn("解析第{}行失败: {}", lineNumber, e.getMessage());
                String title = fields.length > titleIndex && titleIndex >= 0 ? fields[titleIndex] : "未知书名";
                items.add(ImportPreviewItem.builder()
                        .title(title)
                        .status("error")
                        .errorMessage("解析失败: " + e.getMessage())
                        .build());
            }
        }

        return items;
    }

    /**
     * 尝试用多种编码读取文件内容
     *
     * @param file 文件
     * @return 文件内容字符串
     */
    private String tryReadWithEncodings(MultipartFile file) {
        // 尝试的编码列表
        Charset[] encodings = {
            StandardCharsets.UTF_8,
            Charset.forName("GBK"),
            Charset.forName("GB2312"),
            StandardCharsets.ISO_8859_1
        };

        for (Charset charset : encodings) {
            try {
                byte[] bytes = file.getBytes();
                String content = new String(bytes, charset);

                // 检查是否包含中文字符，如果有乱码则跳过
                if (isValidChineseText(content)) {
                    log.info("成功使用 {} 编码读取文件", charset.name());
                    return content;
                }
            } catch (IOException e) {
                log.warn("使用 {} 编码读取失败: {}", charset.name(), e.getMessage());
            }
        }

        // 如果都失败了，使用UTF-8作为默认
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取文件失败", e);
            return null;
        }
    }

    /**
     * 检查文本是否包含有效的中文字符
     *
     * @param text 文本
     * @return 是否有效
     */
    private boolean isValidChineseText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        // 检查是否包含常见中文字符范围
        for (char c : text.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fff') {
                return true;
            }
        }
        // 如果没有中文，也认为是有效的（可能是纯英文文件）
        return !text.contains(""); // 不包含乱码字符
    }

    /**
     * 判断是否为表头字段
     *
     * @param field 字段名
     * @return 是否为表头
     */
    private boolean isHeaderField(String field) {
        String lowerField = field.toLowerCase();
        return lowerField.equals("书名") || lowerField.equals("title") || lowerField.equals("名称") ||
               lowerField.equals("作者") || lowerField.equals("author") ||
               lowerField.equals("isbn") ||
               lowerField.equals("评分") || lowerField.equals("rating");
    }

    /**
     * 解析豆瓣CSV的单行数据
     *
     * @return 导入预览项
     */
    private ImportPreviewItem parseDoubanRow(String[] fields,
                                              int titleIndex, int authorIndex, int isbnIndex,
                                              int ratingIndex, int statusIndex, int tagsIndex,
                                              int notesIndex, int publisherIndex, User user) {
        // 提取字段值
        String title = getValue(fields, titleIndex);
        String author = getValue(fields, authorIndex);
        String isbn = getValue(fields, isbnIndex);
        String ratingStr = getValue(fields, ratingIndex);
        String status = getValue(fields, statusIndex);
        String tags = getValue(fields, tagsIndex);
        String notes = getValue(fields, notesIndex);
        String publisher = getValue(fields, publisherIndex);

        // 验证书名（必填）
        if (title == null || title.trim().isEmpty()) {
            throw new BadRequestException("书名不能为空");
        }

        // 解析评分
        Double rating = null;
        if (ratingStr != null && !ratingStr.isEmpty()) {
            try {
                rating = Double.parseDouble(ratingStr.trim());
            } catch (NumberFormatException e) {
                log.warn("评分解析失败: {}", ratingStr);
            }
        }

        // 检测重复书籍
        boolean duplicate = false;
        Long duplicateBookId = null;
        if (isbn != null && !isbn.isEmpty()) {
            // 通过ISBN检测重复
            Book existing = bookRepository.findByUserAndIsbn(user, isbn).orElse(null);
            if (existing != null) {
                duplicate = true;
                duplicateBookId = existing.getId();
            }
        } else {
            // 通过书名+作者检测重复
            Book existing = bookRepository.findByUserAndTitleAndAuthor(user, title, author).orElse(null);
            if (existing != null) {
                duplicate = true;
                duplicateBookId = existing.getId();
            }
        }

        return ImportPreviewItem.builder()
                .title(title.trim())
                .author(author != null ? author.trim() : null)
                .isbn(isbn != null ? isbn.trim() : null)
                .publisher(publisher != null ? publisher.trim() : null)
                .readingStatus(status != null ? status.trim() : null)
                .rating(rating)
                .tags(tags != null ? tags.trim() : null)
                .notes(notes != null ? notes.trim() : null)
                .duplicate(duplicate)
                .duplicateBookId(duplicateBookId)
                .status("success")
                .build();
    }

    // ==================== 微信读书解析 ====================

    /**
     * 解析微信读书导出的文件
     * 支持CSV和JSON格式
     *
     * @param file 文件
     * @param username 当前用户名
     * @return 解析后的书籍列表
     */
    private List<ImportPreviewItem> parseWereadFile(MultipartFile file, String username) {
        List<ImportPreviewItem> items = new ArrayList<>();
        User user = userService.getUserEntity(username);

        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".json")) {
            // JSON格式解析
            items = parseWereadJson(file, user);
        } else {
            // CSV格式解析
            items = parseWereadCsv(file, user);
        }

        return items;
    }

    /**
     * 解析微信读书CSV文件
     * 微信读书CSV格式：
     * 书名,作者,阅读进度,阅读时长,笔记数量
     *
     * @return 书籍列表
     */
    private List<ImportPreviewItem> parseWereadCsv(MultipartFile file, User user) {
        List<ImportPreviewItem> items = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;
            int titleIndex = -1, authorIndex = -1, isbnIndex = -1;
            int progressIndex = -1, statusIndex = -1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields = parseCsvLine(line);

                // 解析表头
                if (lineNumber == 1) {
                    for (int i = 0; i < fields.length; i++) {
                        String field = fields[i].trim().replace("\uFEFF", "");
                        switch (field) {
                            case "书名":
                            case "title":
                                titleIndex = i;
                                break;
                            case "作者":
                            case "author":
                                authorIndex = i;
                                break;
                            case "ISBN":
                            case "isbn":
                                isbnIndex = i;
                                break;
                            case "阅读进度":
                            case "progress":
                                progressIndex = i;
                                break;
                            case "阅读状态":
                            case "状态":
                            case "status":
                                statusIndex = i;
                                break;
                        }
                    }
                    continue;
                }

                // 解析数据行
                try {
                    String title = getValue(fields, titleIndex);
                    String author = getValue(fields, authorIndex);
                    String isbn = getValue(fields, isbnIndex);
                    String status = getValue(fields, statusIndex);

                    if (title == null || title.trim().isEmpty()) {
                        continue;
                    }

                    // 检测重复
                    boolean duplicate = false;
                    Long duplicateBookId = null;
                    if (isbn != null && !isbn.isEmpty()) {
                        Book existing = bookRepository.findByUserAndIsbn(user, isbn).orElse(null);
                        if (existing != null) {
                            duplicate = true;
                            duplicateBookId = existing.getId();
                        }
                    } else {
                        Book existing = bookRepository.findByUserAndTitleAndAuthor(user, title, author).orElse(null);
                        if (existing != null) {
                            duplicate = true;
                            duplicateBookId = existing.getId();
                        }
                    }

                    items.add(ImportPreviewItem.builder()
                            .title(title.trim())
                            .author(author != null ? author.trim() : null)
                            .isbn(isbn != null ? isbn.trim() : null)
                            .readingStatus(status != null ? status.trim() : "在读")
                            .duplicate(duplicate)
                            .duplicateBookId(duplicateBookId)
                            .status("success")
                            .build());

                } catch (Exception e) {
                    log.warn("解析微信读书第{}行失败: {}", lineNumber, e.getMessage());
                }
            }

        } catch (IOException e) {
            log.error("读取微信读书CSV文件失败", e);
            throw new BadRequestException("文件读取失败: " + e.getMessage());
        }

        return items;
    }

    /**
     * 解析微信读书JSON文件
     *
     * @return 书籍列表
     */
    private List<ImportPreviewItem> parseWereadJson(MultipartFile file, User user) {
        // 微信读书JSON格式较为复杂，这里简化处理
        // 实际使用时可能需要根据具体导出格式调整
        List<ImportPreviewItem> items = new ArrayList<>();

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            // 简单JSON解析（实际应使用Jackson或Gson）
            // 这里先返回空列表，提示用户使用CSV格式
            log.warn("微信读书JSON格式暂不完全支持，建议使用CSV格式");
            throw new BadRequestException("微信读书JSON格式暂不完全支持，请使用CSV格式导出");
        } catch (IOException e) {
            log.error("读取微信读书JSON文件失败", e);
            throw new BadRequestException("文件读取失败: " + e.getMessage());
        }
    }

    // ==================== 批量导入 ====================

    /**
     * 执行批量导入
     *
     * @param items 预览项列表
     * @param request 导入确认请求
     * @param username 当前用户名
     * @return 导入结果
     */
    @Transactional
    public ImportResultResponse executeImport(List<ImportPreviewItem> items,
                                               ImportConfirmRequest request,
                                               String username) {
        log.info("开始批量导入书籍: 用户={}, 数量={}", username, items.size());

        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
        int overwriteCount = 0;
        List<ImportResultResponse.ImportFailureItem> failures = new ArrayList<>();

        String duplicateStrategy = request.getDuplicateStrategy();
        if (duplicateStrategy == null) {
            duplicateStrategy = "skip";
        }

        List<Integer> selectedIndexes = request.getSelectedIndexes();

        for (int i = 0; i < items.size(); i++) {
            // 如果指定了要导入的索引，只处理指定的项
            if (selectedIndexes != null && !selectedIndexes.isEmpty() && !selectedIndexes.contains(i)) {
                continue;
            }

            ImportPreviewItem item = items.get(i);

            // 跳过解析失败的项
            if ("error".equals(item.getStatus())) {
                failCount++;
                failures.add(ImportResultResponse.ImportFailureItem.builder()
                        .title(item.getTitle())
                        .author(item.getAuthor())
                        .reason(item.getErrorMessage())
                        .build());
                continue;
            }

            try {
                // 处理重复书籍
                if (item.isDuplicate()) {
                    if ("skip".equals(duplicateStrategy)) {
                        skipCount++;
                        log.info("跳过重复书籍: {}", item.getTitle());
                        continue;
                    } else if ("overwrite".equals(duplicateStrategy) && item.getDuplicateBookId() != null) {
                        // 更新已有书籍
                        updateExistingBook(item.getDuplicateBookId(), item, username);
                        overwriteCount++;
                        successCount++;
                        log.info("覆盖重复书籍: {}", item.getTitle());
                        continue;
                    }
                }

                // 创建新书籍
                BookRequest bookRequest = convertToBookRequest(item);
                bookService.createBook(bookRequest, username);
                successCount++;
                log.info("成功导入书籍: {}", item.getTitle());

            } catch (Exception e) {
                log.error("导入书籍失败: {}", item.getTitle(), e);
                failCount++;
                failures.add(ImportResultResponse.ImportFailureItem.builder()
                        .title(item.getTitle())
                        .author(item.getAuthor())
                        .reason(e.getMessage())
                        .build());
            }
        }

        log.info("批量导入完成: 成功={}, 失败={}, 跳过={}, 覆盖={}",
                successCount, failCount, skipCount, overwriteCount);

        return ImportResultResponse.builder()
                .successCount(successCount)
                .failCount(failCount)
                .skipCount(skipCount)
                .overwriteCount(overwriteCount)
                .totalCount(successCount + failCount + skipCount + overwriteCount)
                .failures(failures)
                .build();
    }

    /**
     * 更新已有书籍信息
     *
     * @param bookId 书籍ID
     * @param item 预览项数据
     * @param username 用户名
     */
    private void updateExistingBook(Long bookId, ImportPreviewItem item, String username) {
        BookRequest bookRequest = convertToBookRequest(item);
        bookService.updateBook(bookId, bookRequest, username);
    }

    /**
     * 将预览项转换为书籍请求DTO
     *
     * @param item 预览项
     * @return 书籍请求
     */
    private BookRequest convertToBookRequest(ImportPreviewItem item) {
        // 转换阅读状态
        Book.ReadingStatus readingStatus = convertReadingStatus(item.getReadingStatus());

        return BookRequest.builder()
                .title(item.getTitle())
                .author(item.getAuthor())
                .isbn(item.getIsbn())
                .publisher(item.getPublisher())
                .coverUrl(item.getCoverUrl())
                .readingStatus(readingStatus)
                .rating(item.getRating())
                .description(item.getNotes())
                .build();
    }

    /**
     * 转换阅读状态
     * 将豆瓣/微信读书的状态转换为系统内部状态
     *
     * @param statusStr 状态字符串
     * @return 阅读状态枚举
     */
    private Book.ReadingStatus convertReadingStatus(String statusStr) {
        if (statusStr == null) {
            return Book.ReadingStatus.WANT_TO_READ;
        }

        switch (statusStr) {
            case "想读":
            case "wishlist":
                return Book.ReadingStatus.WANT_TO_READ;
            case "在读":
            case "reading":
            case "在读中":
                return Book.ReadingStatus.READING;
            case "读过":
            case "completed":
            case "已读":
            case "读完":
                return Book.ReadingStatus.COMPLETED;
            case "弃读":
            case "abandoned":
                return Book.ReadingStatus.ABANDONED;
            default:
                return Book.ReadingStatus.WANT_TO_READ;
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 解析CSV行，处理引号内的逗号
     *
     * @param line CSV行
     * @return 字段数组
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // 处理引号
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // 转义的引号
                    currentField.append('"');
                    i++;
                } else {
                    // 切换引号状态
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // 字段分隔符
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // 添加最后一个字段
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    /**
     * 安全获取数组值
     *
     * @param fields 字段数组
     * @param index 索引
     * @return 值（如果索引无效返回null）
     */
    private String getValue(String[] fields, int index) {
        if (index < 0 || index >= fields.length) {
            return null;
        }
        String value = fields[index].trim();
        return value.isEmpty() ? null : value;
    }
}