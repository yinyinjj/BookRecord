package com.bookrecord.service;

import com.bookrecord.entity.Book;
import com.bookrecord.entity.Quote;
import com.bookrecord.entity.ReadingNote;
import com.bookrecord.entity.User;
import com.bookrecord.exception.BadRequestException;
import com.bookrecord.exception.ResourceNotFoundException;
import com.bookrecord.repository.BookRepository;
import com.bookrecord.repository.QuoteRepository;
import com.bookrecord.repository.ReadingNoteRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 导出服务类
 * 负责将感悟和金句导出为 Markdown 或 PDF 格式
 * 支持图片（URL和Base64）、表格等富文本元素
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final BookRepository bookRepository;
    private final ReadingNoteRepository readingNoteRepository;
    private final QuoteRepository quoteRepository;
    private final UserService userService;

    // 中文字体路径
    private static final String CHINESE_FONT_PATH = "/fonts/NotoSansSC-Regular.ttf";

    // 图片匹配模式：支持 src="..." 和 src='...'
    private static final Pattern IMG_PATTERN = Pattern.compile(
            "<img[^>]+src=[\"']([^\"']+)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE
    );

    // Base64图片匹配
    private static final Pattern BASE64_PATTERN = Pattern.compile(
            "^data:image/(png|jpeg|jpg|gif|webp);base64,(.+)$",
            Pattern.CASE_INSENSITIVE
    );

    // 表格匹配模式
    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "<table[^>]*>(.*?)</table>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // 行匹配模式
    private static final Pattern TR_PATTERN = Pattern.compile(
            "<tr[^>]*>(.*?)</tr>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // 单元格匹配模式
    private static final Pattern TD_PATTERN = Pattern.compile(
            "<t[dh][^>]*>(.*?)</t[dh]>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /**
     * 导出书籍的所有感悟为 Markdown 格式
     *
     * @param bookId   书籍ID
     * @param username 当前用户名
     * @return Markdown 格式的字符串
     */
    public String exportNotesAsMarkdown(Long bookId, String username) {
        log.info("导出书籍 {} 的感悟为 Markdown 格式，用户: {}", bookId, username);

        Book book = validateBookOwnership(bookId, username);
        List<ReadingNote> notes = readingNoteRepository.findByBookIdOrderByCreatedAtDesc(bookId);

        StringBuilder markdown = new StringBuilder();

        // 添加标题和书籍信息
        markdown.append("# 《").append(book.getTitle()).append("》读书感悟\n\n");
        markdown.append("> 作者：").append(book.getAuthor() != null ? book.getAuthor() : "未知").append("\n");
        if (book.getPublisher() != null) {
            markdown.append("> 出版社：").append(book.getPublisher()).append("\n");
        }
        if (book.getRating() != null) {
            markdown.append("> 评分：").append("★".repeat(book.getRating().intValue())).append("\n");
        }
        markdown.append("\n---\n\n");

        markdown.append("*导出时间：")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .append("*\n\n");

        if (notes.isEmpty()) {
            markdown.append("*暂无感悟记录*\n");
        } else {
            markdown.append("## 感悟列表\n\n");
            int index = 1;
            for (ReadingNote note : notes) {
                markdown.append("### ").append(index++).append(". ");
                if (note.getTitle() != null && !note.getTitle().isEmpty()) {
                    markdown.append(note.getTitle()).append("\n\n");
                } else {
                    markdown.append("【").append(getNoteTypeLabel(note.getNoteType())).append("】\n\n");
                }

                // 元信息
                if (note.getChapter() != null || note.getPageNumber() != null) {
                    markdown.append("*");
                    if (note.getChapter() != null) {
                        markdown.append("章节：").append(note.getChapter());
                    }
                    if (note.getPageNumber() != null) {
                        if (note.getChapter() != null) markdown.append(" | ");
                        markdown.append("页码：P").append(note.getPageNumber());
                    }
                    markdown.append("*\n\n");
                }

                markdown.append("**类型：**").append(getNoteTypeLabel(note.getNoteType())).append("\n\n");

                // 转换内容（支持图片和表格）
                String content = convertHtmlToMarkdown(note.getContent());
                markdown.append(content).append("\n\n");

                // 标签
                if (note.getTags() != null && !note.getTags().isEmpty()) {
                    markdown.append("*标签：");
                    String[] tags = note.getTags().split(",");
                    for (int i = 0; i < tags.length; i++) {
                        markdown.append("#").append(tags[i].trim());
                        if (i < tags.length - 1) markdown.append(" ");
                    }
                    markdown.append("*\n\n");
                }

                // 关联金句
                if (note.getQuote() != null) {
                    markdown.append("> 💬 关联金句：\n> \n> ");
                    markdown.append(note.getQuote().getContent().replace("\n", "\n> "));
                    markdown.append("\n\n");
                }

                markdown.append("---\n");
                markdown.append("*创建于：")
                        .append(note.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .append("*\n\n");
            }
        }

        markdown.append("---\n\n");
        markdown.append("*本文档由 Book Record 读书笔记管理系统生成*\n");

        return markdown.toString();
    }

    /**
     * 导出书籍的所有感悟为 PDF 格式
     *
     * @param bookId   书籍ID
     * @param username 当前用户名
     * @return PDF 文件的字节数组
     */
    public byte[] exportNotesAsPdf(Long bookId, String username) {
        log.info("导出书籍 {} 的感悟为 PDF 格式，用户: {}", bookId, username);

        Book book = validateBookOwnership(bookId, username);
        List<ReadingNote> notes = readingNoteRepository.findByBookIdOrderByCreatedAtDesc(bookId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // 获取字体
            BaseFont baseFont = getChineseFont();
            Font titleFont = new Font(baseFont, 20, Font.BOLD);
            Font subtitleFont = new Font(baseFont, 12, Font.NORMAL);
            Font headingFont = new Font(baseFont, 14, Font.BOLD);
            Font normalFont = new Font(baseFont, 11, Font.NORMAL);
            Font boldFont = new Font(baseFont, 11, Font.BOLD);
            Font italicFont = new Font(baseFont, 10, Font.ITALIC);
            Font metaFont = new Font(baseFont, 10, Font.NORMAL);

            // 添加标题
            Paragraph title = new Paragraph("《" + book.getTitle() + "》读书感悟", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 书籍信息
            if (book.getAuthor() != null) {
                Paragraph author = new Paragraph("作者：" + book.getAuthor(), subtitleFont);
                author.setAlignment(Element.ALIGN_CENTER);
                document.add(author);
            }
            if (book.getPublisher() != null) {
                Paragraph publisher = new Paragraph("出版社：" + book.getPublisher(), subtitleFont);
                publisher.setAlignment(Element.ALIGN_CENTER);
                document.add(publisher);
            }
            if (book.getRating() != null) {
                Paragraph rating = new Paragraph("评分：" + "★".repeat(book.getRating().intValue()), subtitleFont);
                rating.setAlignment(Element.ALIGN_CENTER);
                document.add(rating);
            }

            document.add(new Paragraph(" "));
            LineSeparator line = new LineSeparator();
            document.add(line);
            document.add(new Paragraph(" "));

            // 导出时间
            Paragraph exportTime = new Paragraph(
                    "导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    italicFont
            );
            exportTime.setAlignment(Element.ALIGN_RIGHT);
            document.add(exportTime);
            document.add(new Paragraph(" "));

            if (notes.isEmpty()) {
                Paragraph empty = new Paragraph("暂无感悟记录", normalFont);
                empty.setAlignment(Element.ALIGN_CENTER);
                document.add(empty);
            } else {
                int index = 1;
                for (ReadingNote note : notes) {
                    // 感悟标题
                    String noteTitle = (note.getTitle() != null && !note.getTitle().isEmpty())
                            ? note.getTitle()
                            : "【" + getNoteTypeLabel(note.getNoteType()) + "】";
                    Paragraph noteHeading = new Paragraph(index++ + ". " + noteTitle, headingFont);
                    noteHeading.setSpacingBefore(15);
                    noteHeading.setSpacingAfter(10);
                    document.add(noteHeading);

                    // 元信息
                    if (note.getChapter() != null || note.getPageNumber() != null) {
                        StringBuilder meta = new StringBuilder();
                        if (note.getChapter() != null) {
                            meta.append("章节：").append(note.getChapter());
                        }
                        if (note.getPageNumber() != null) {
                            if (meta.length() > 0) meta.append("  |  ");
                            meta.append("页码：P").append(note.getPageNumber());
                        }
                        Paragraph metaPara = new Paragraph(meta.toString(), metaFont);
                        metaPara.setSpacingAfter(5);
                        document.add(metaPara);
                    }

                    Paragraph typePara = new Paragraph("类型：" + getNoteTypeLabel(note.getNoteType()), metaFont);
                    typePara.setSpacingAfter(5);
                    document.add(typePara);

                    // 添加内容（支持图片和表格）
                    addHtmlContentToPdf(document, note.getContent(), baseFont, normalFont, boldFont);
                    document.add(new Paragraph(" "));

                    // 标签
                    if (note.getTags() != null && !note.getTags().isEmpty()) {
                        Paragraph tagsPara = new Paragraph("标签：" + note.getTags().replace(",", " #"), metaFont);
                        tagsPara.setSpacingAfter(5);
                        document.add(tagsPara);
                    }

                    // 创建时间
                    Paragraph timePara = new Paragraph(
                            "创建于：" + note.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                            italicFont
                    );
                    timePara.setSpacingAfter(10);
                    document.add(timePara);

                    document.add(new Paragraph(" "));
                }
            }

            // 页脚
            document.add(new Paragraph(" "));
            document.add(line);
            Paragraph footer = new Paragraph("本文档由 Book Record 读书笔记管理系统生成", italicFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException | IOException e) {
            log.error("生成 PDF 失败", e);
            throw new BadRequestException("生成 PDF 失败：" + e.getMessage());
        }
    }

    /**
     * 导出书籍的所有金句为 Markdown 格式
     */
    public String exportQuotesAsMarkdown(Long bookId, String username) {
        log.info("导出书籍 {} 的金句为 Markdown 格式，用户: {}", bookId, username);

        Book book = validateBookOwnership(bookId, username);
        List<Quote> quotes = quoteRepository.findByBookIdOrderByCreatedAtDesc(bookId);

        StringBuilder markdown = new StringBuilder();
        markdown.append("# 《").append(book.getTitle()).append("》金句收藏\n\n");
        markdown.append("> 作者：").append(book.getAuthor() != null ? book.getAuthor() : "未知").append("\n");
        markdown.append("\n---\n\n");

        markdown.append("*导出时间：")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .append("*\n\n");

        if (quotes.isEmpty()) {
            markdown.append("*暂无金句收藏*\n");
        } else {
            markdown.append("## 金句列表\n\n");
            int index = 1;
            for (Quote quote : quotes) {
                markdown.append("### ").append(index++).append("\n\n");
                markdown.append("> ").append(quote.getContent().replace("\n", "\n> ")).append("\n\n");

                if (quote.getChapter() != null || quote.getPageNumber() != null) {
                    markdown.append("*");
                    if (quote.getChapter() != null) {
                        markdown.append("章节：").append(quote.getChapter());
                    }
                    if (quote.getPageNumber() != null) {
                        if (quote.getChapter() != null) markdown.append(" | ");
                        markdown.append("页码：P").append(quote.getPageNumber());
                    }
                    markdown.append("*\n\n");
                }

                if (quote.getNote() != null && !quote.getNote().isEmpty()) {
                    markdown.append("**备注：**").append(quote.getNote()).append("\n\n");
                }

                if (quote.getTags() != null && !quote.getTags().isEmpty()) {
                    markdown.append("*标签：");
                    String[] tags = quote.getTags().split(",");
                    for (int i = 0; i < tags.length; i++) {
                        markdown.append("#").append(tags[i].trim());
                        if (i < tags.length - 1) markdown.append(" ");
                    }
                    markdown.append("*\n\n");
                }

                markdown.append("---\n\n");
            }
        }

        markdown.append("*本文档由 Book Record 读书笔记管理系统生成*\n");
        return markdown.toString();
    }

    /**
     * 导出书籍的所有金句为 PDF 格式
     */
    public byte[] exportQuotesAsPdf(Long bookId, String username) {
        log.info("导出书籍 {} 的金句为 PDF 格式，用户: {}", bookId, username);

        Book book = validateBookOwnership(bookId, username);
        List<Quote> quotes = quoteRepository.findByBookIdOrderByCreatedAtDesc(bookId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            BaseFont baseFont = getChineseFont();
            Font titleFont = new Font(baseFont, 20, Font.BOLD);
            Font subtitleFont = new Font(baseFont, 12, Font.NORMAL);
            Font headingFont = new Font(baseFont, 14, Font.BOLD);
            Font quoteFont = new Font(baseFont, 12, Font.ITALIC);
            Font normalFont = new Font(baseFont, 11, Font.NORMAL);
            Font italicFont = new Font(baseFont, 10, Font.ITALIC);
            Font metaFont = new Font(baseFont, 10, Font.NORMAL);

            Paragraph title = new Paragraph("《" + book.getTitle() + "》金句收藏", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            if (book.getAuthor() != null) {
                Paragraph author = new Paragraph("作者：" + book.getAuthor(), subtitleFont);
                author.setAlignment(Element.ALIGN_CENTER);
                document.add(author);
            }

            document.add(new Paragraph(" "));
            LineSeparator line = new LineSeparator();
            document.add(line);
            document.add(new Paragraph(" "));

            Paragraph exportTime = new Paragraph(
                    "导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    italicFont
            );
            exportTime.setAlignment(Element.ALIGN_RIGHT);
            document.add(exportTime);
            document.add(new Paragraph(" "));

            if (quotes.isEmpty()) {
                Paragraph empty = new Paragraph("暂无金句收藏", normalFont);
                empty.setAlignment(Element.ALIGN_CENTER);
                document.add(empty);
            } else {
                int index = 1;
                for (Quote quote : quotes) {
                    Paragraph quoteHeading = new Paragraph(index++ + ".", headingFont);
                    quoteHeading.setSpacingBefore(15);
                    document.add(quoteHeading);

                    Paragraph quoteContent = new Paragraph("\"" + quote.getContent() + "\"", quoteFont);
                    quoteContent.setSpacingAfter(10);
                    document.add(quoteContent);

                    if (quote.getChapter() != null || quote.getPageNumber() != null) {
                        StringBuilder meta = new StringBuilder();
                        if (quote.getChapter() != null) {
                            meta.append("章节：").append(quote.getChapter());
                        }
                        if (quote.getPageNumber() != null) {
                            if (meta.length() > 0) meta.append("  |  ");
                            meta.append("页码：P").append(quote.getPageNumber());
                        }
                        Paragraph metaPara = new Paragraph(meta.toString(), metaFont);
                        metaPara.setSpacingAfter(5);
                        document.add(metaPara);
                    }

                    if (quote.getNote() != null && !quote.getNote().isEmpty()) {
                        Paragraph notePara = new Paragraph("备注：" + quote.getNote(), metaFont);
                        notePara.setSpacingAfter(5);
                        document.add(notePara);
                    }

                    document.add(new Paragraph(" "));
                }
            }

            document.add(line);
            Paragraph footer = new Paragraph("本文档由 Book Record 读书笔记管理系统生成", italicFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (DocumentException | IOException e) {
            log.error("生成 PDF 失败", e);
            throw new BadRequestException("生成 PDF 失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 将 HTML 内容转换为 Markdown 格式
     * 支持图片（URL和Base64）、表格、标题、粗体等
     */
    private String convertHtmlToMarkdown(String html) {
        if (html == null || html.isEmpty()) return "";

        String result = html;

        // 1. 处理表格（在处理其他标签之前）
        result = convertTablesToMarkdown(result);

        // 2. 处理图片标签
        result = convertImagesToMarkdown(result);

        // 3. 处理标题标签
        result = result.replaceAll("<h1[^>]*>", "\n# ");
        result = result.replaceAll("</h1>", "\n\n");
        result = result.replaceAll("<h2[^>]*>", "\n## ");
        result = result.replaceAll("</h2>", "\n\n");
        result = result.replaceAll("<h3[^>]*>", "\n### ");
        result = result.replaceAll("</h3>", "\n\n");
        result = result.replaceAll("<h4[^>]*>", "\n#### ");
        result = result.replaceAll("</h4>", "\n\n");

        // 4. 处理段落和换行
        result = result.replaceAll("<br\\s*/?>", "\n");
        result = result.replaceAll("<p[^>]*>", "\n\n");
        result = result.replaceAll("</p>", "\n\n");

        // 5. 处理粗体和斜体
        result = result.replaceAll("<strong[^>]*>", "**");
        result = result.replaceAll("</strong>", "**");
        result = result.replaceAll("<b[^>]*>", "**");
        result = result.replaceAll("</b>", "**");
        result = result.replaceAll("<em[^>]*>", "*");
        result = result.replaceAll("</em>", "*");
        result = result.replaceAll("<i[^>]*>", "*");
        result = result.replaceAll("</i>", "*");

        // 6. 处理列表
        result = result.replaceAll("<ul[^>]*>", "\n");
        result = result.replaceAll("</ul>", "\n");
        result = result.replaceAll("<ol[^>]*>", "\n");
        result = result.replaceAll("</ol>", "\n");
        result = result.replaceAll("<li[^>]*>", "- ");
        result = result.replaceAll("</li>", "\n");

        // 7. 处理代码块
        result = result.replaceAll("<pre[^>]*>", "\n```\n");
        result = result.replaceAll("</pre>", "\n```\n");
        result = result.replaceAll("<code[^>]*>", "`");
        result = result.replaceAll("</code>", "`");

        // 8. 处理引用
        result = result.replaceAll("<blockquote[^>]*>", "\n> ");
        result = result.replaceAll("</blockquote>", "\n");

        // 9. 移除其余 HTML 标签
        result = result.replaceAll("<[^>]+>", "");

        // 10. 处理 HTML 实体
        result = result.replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"");

        // 11. 清理多余空白
        result = result.replaceAll("\n{3,}", "\n\n");
        result = result.trim();

        return result;
    }

    /**
     * 将 HTML 表格转换为 Markdown 表格
     */
    private String convertTablesToMarkdown(String html) {
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        Matcher tableMatcher = TABLE_PATTERN.matcher(html);
        while (tableMatcher.find()) {
            // 添加表格之前的内容
            result.append(html, lastEnd, tableMatcher.start());

            String tableContent = tableMatcher.group(1);
            List<List<String>> rows = new ArrayList<>();

            // 解析表格行
            Matcher trMatcher = TR_PATTERN.matcher(tableContent);
            while (trMatcher.find()) {
                List<String> cells = new ArrayList<>();
                Matcher tdMatcher = TD_PATTERN.matcher(trMatcher.group(1));
                while (tdMatcher.find()) {
                    String cell = tdMatcher.group(1).trim();
                    // 移除单元格内的标签
                    cell = cell.replaceAll("<[^>]+>", "");
                    cell = cell.replace("&nbsp;", " ");
                    cells.add(cell);
                }
                if (!cells.isEmpty()) {
                    rows.add(cells);
                }
            }

            // 生成 Markdown 表格
            if (!rows.isEmpty()) {
                result.append("\n\n");
                // 表头
                List<String> header = rows.get(0);
                result.append("|").append(String.join("|", header)).append("|\n");
                // 分隔线
                result.append("|").append("---|".repeat(header.size())).append("\n");
                // 数据行
                for (int i = 1; i < rows.size(); i++) {
                    List<String> row = rows.get(i);
                    // 补齐列数
                    while (row.size() < header.size()) {
                        row.add("");
                    }
                    result.append("|").append(String.join("|", row.subList(0, header.size()))).append("|\n");
                }
                result.append("\n");
            }

            lastEnd = tableMatcher.end();
        }

        // 添加剩余内容
        result.append(html.substring(lastEnd));
        return result.toString();
    }

    /**
     * 将 HTML 图片标签转换为 Markdown 格式
     * Base64图片使用占位符，URL图片保留
     */
    private String convertImagesToMarkdown(String html) {
        Matcher imgMatcher = IMG_PATTERN.matcher(html);
        StringBuffer sb = new StringBuffer();

        while (imgMatcher.find()) {
            String imgTag = imgMatcher.group(0);
            String src = imgMatcher.group(1);

            // 提取 alt 属性
            String alt = "图片";
            Pattern altPattern = Pattern.compile("alt=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);
            Matcher altMatcher = altPattern.matcher(imgTag);
            if (altMatcher.find()) {
                alt = altMatcher.group(1);
            }

            String replacement;
            Matcher base64Matcher = BASE64_PATTERN.matcher(src);
            if (base64Matcher.matches()) {
                // Base64图片：使用占位符（避免Markdown文件过大）
                replacement = "\n\n*[" + alt + " - 本地图片]*\n\n";
            } else {
                // URL图片：正常转换
                replacement = "\n\n![" + alt + "](" + src + ")\n\n";
            }

            imgMatcher.appendReplacement(sb, replacement);
        }
        imgMatcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 将 HTML 内容添加到 PDF 文档
     * 支持图片（URL和Base64）、表格等
     */
    private void addHtmlContentToPdf(Document document, String html, BaseFont baseFont,
                                     Font normalFont, Font boldFont) throws DocumentException {
        if (html == null || html.isEmpty()) return;

        // 1. 处理表格
        addTablesToPdf(document, html, baseFont, normalFont);

        // 2. 移除表格后处理图片
        String htmlWithoutTables = TABLE_PATTERN.matcher(html).replaceAll("");

        // 3. 处理图片
        addImagesToPdf(document, htmlWithoutTables, normalFont, boldFont);
    }

    /**
     * 将 HTML 表格添加到 PDF
     */
    private void addTablesToPdf(Document document, String html, BaseFont baseFont, Font font) throws DocumentException {
        Matcher tableMatcher = TABLE_PATTERN.matcher(html);

        while (tableMatcher.find()) {
            String tableContent = tableMatcher.group(1);
            List<List<String>> rows = new ArrayList<>();

            Matcher trMatcher = TR_PATTERN.matcher(tableContent);
            while (trMatcher.find()) {
                List<String> cells = new ArrayList<>();
                Matcher tdMatcher = TD_PATTERN.matcher(trMatcher.group(1));
                while (tdMatcher.find()) {
                    String cell = tdMatcher.group(1).trim();
                    cell = cell.replaceAll("<[^>]+>", "");
                    cell = cell.replace("&nbsp;", " ");
                    cells.add(cell);
                }
                if (!cells.isEmpty()) {
                    rows.add(cells);
                }
            }

            if (!rows.isEmpty()) {
                int columns = rows.get(0).size();
                PdfPTable pdfTable = new PdfPTable(columns);
                pdfTable.setWidthPercentage(100);
                pdfTable.setSpacingBefore(10);
                pdfTable.setSpacingAfter(10);

                // 添加表头
                for (String cell : rows.get(0)) {
                    PdfPCell pdfCell = new PdfPCell(new Paragraph(cell, font));
                    pdfCell.setBackgroundColor(new Color(240, 240, 240));
                    pdfCell.setPadding(5);
                    pdfTable.addCell(pdfCell);
                }

                // 添加数据行
                for (int i = 1; i < rows.size(); i++) {
                    List<String> row = rows.get(i);
                    for (int j = 0; j < columns; j++) {
                        String cellText = j < row.size() ? row.get(j) : "";
                        PdfPCell pdfCell = new PdfPCell(new Paragraph(cellText, font));
                        pdfCell.setPadding(5);
                        pdfTable.addCell(pdfCell);
                    }
                }

                document.add(pdfTable);
            }
        }
    }

    /**
     * 将 HTML 图片添加到 PDF
     * 支持 URL 图片和 Base64 图片
     */
    private void addImagesToPdf(Document document, String html, Font normalFont, Font boldFont) throws DocumentException {
        Matcher imgMatcher = IMG_PATTERN.matcher(html);
        int lastIndex = 0;

        while (imgMatcher.find()) {
            // 添加图片之前的文本
            String beforeText = html.substring(lastIndex, imgMatcher.start());
            addTextToPdf(document, beforeText, normalFont, boldFont);

            String src = imgMatcher.group(1);

            // 处理图片
            Image image = null;
            Matcher base64Matcher = BASE64_PATTERN.matcher(src);

            if (base64Matcher.matches()) {
                // Base64 图片
                try {
                    String imageData = base64Matcher.group(2);
                    byte[] bytes = Base64.getDecoder().decode(imageData);
                    image = Image.getInstance(bytes);
                } catch (Exception e) {
                    log.warn("解析Base64图片失败", e);
                }
            } else {
                // URL 图片
                image = loadImageFromUrl(src);
            }

            if (image != null) {
                try {
                    // 限制图片大小
                    float maxWidth = document.getPageSize().getWidth() - 100;
                    float maxHeight = 400;
                    image.scaleToFit(maxWidth, maxHeight);

                    // 设置图片居中对齐
                    image.setAlignment(Element.ALIGN_CENTER);

                    document.add(image);
                    document.add(new Paragraph(" "));
                } catch (Exception e) {
                    log.warn("添加图片到PDF失败", e);
                    document.add(new Paragraph("[图片]", normalFont));
                }
            } else {
                document.add(new Paragraph("[图片加载失败]", normalFont));
            }

            lastIndex = imgMatcher.end();
        }

        // 添加剩余文本
        if (lastIndex < html.length()) {
            String remainingText = html.substring(lastIndex);
            addTextToPdf(document, remainingText, normalFont, boldFont);
        }
    }

    /**
     * 从 URL 加载图片
     * 支持代理URL和普通URL，设置请求头绕过防盗链
     */
    private Image loadImageFromUrl(String imageUrl) {
        try {
            String finalUrl = imageUrl;

            log.debug("尝试加载图片，原始URL: {}", imageUrl);

            // 处理代理URL：提取真实URL
            if (imageUrl.contains("/api/v1/image-proxy?url=")) {
                int idx = imageUrl.indexOf("url=");
                if (idx > 0) {
                    finalUrl = URLDecoder.decode(imageUrl.substring(idx + 4), StandardCharsets.UTF_8);
                    log.debug("从代理URL提取真实URL: {}", finalUrl);
                }
            }

            // 处理相对URL
            if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                log.warn("图片URL不是绝对路径: {}", finalUrl);
                return null;
            }

            URL url = new URL(finalUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            // 设置请求头，模拟浏览器请求，绕过防盗链
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            connection.setRequestProperty("Referer", extractReferer(finalUrl));

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.warn("图片请求失败: HTTP {} - {}", responseCode, finalUrl);
                return null;
            }

            // 检查Content-Type
            String contentType = connection.getContentType();
            if (contentType != null && !contentType.startsWith("image/")) {
                log.warn("响应不是图片类型: {} - {}", contentType, finalUrl);
                return null;
            }

            // 读取图片数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream is = connection.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
            }

            byte[] imageData = baos.toByteArray();
            log.debug("成功加载图片: {} bytes - {}", imageData.length, finalUrl);
            return Image.getInstance(imageData);

        } catch (Exception e) {
            log.error("加载图片失败: {} - 错误: {}", imageUrl, e.getMessage());
            return null;
        }
    }

    /**
     * 从图片URL提取Referer
     */
    private String extractReferer(String url) {
        try {
            URL u = new URL(url);
            return u.getProtocol() + "://" + u.getHost() + "/";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 将纯文本添加到 PDF
     */
    private void addTextToPdf(Document document, String text, Font normalFont, Font boldFont) throws DocumentException {
        if (text == null || text.isEmpty()) return;

        // 移除表格和图片标签
        String cleanText = text.replaceAll("<table[^>]*>.*?</table>", "[表格]");
        cleanText = cleanText.replaceAll("<img[^>]+>", "[图片]");
        cleanText = cleanText.replaceAll("<[^>]+>", "");

        // 处理 HTML 实体
        cleanText = cleanText.replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"");

        if (cleanText.trim().isEmpty()) return;

        // 处理 Markdown 粗体标记
        String[] parts = cleanText.split("\\*\\*");
        Paragraph para = new Paragraph();

        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 0) {
                para.add(new Chunk(parts[i], normalFont));
            } else {
                para.add(new Chunk(parts[i], boldFont));
            }
        }

        document.add(para);
    }

    /**
     * 验证书籍归属权
     */
    private Book validateBookOwnership(Long bookId, String username) {
        User user = userService.getUserEntity(username);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (!book.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("您没有权限导出此书籍的内容");
        }

        return book;
    }

    /**
     * 获取中文字体
     */
    private BaseFont getChineseFont() {
        try {
            return BaseFont.createFont(CHINESE_FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            log.warn("加载内置中文字体失败，尝试系统字体: {}", e.getMessage());
            try {
                String[] fontPaths = {
                        "C:/Windows/Fonts/simsun.ttc,0",
                        "C:/Windows/Fonts/msyh.ttc,0",
                        "/System/Library/Fonts/PingFang.ttc,0",
                        "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc,0"
                };
                for (String path : fontPaths) {
                    try {
                        return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    } catch (Exception ignored) {
                    }
                }
                return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            } catch (Exception ex) {
                throw new RuntimeException("无法加载任何字体", ex);
            }
        }
    }

    /**
     * 获取感悟类型的中文标签
     */
    private String getNoteTypeLabel(ReadingNote.NoteType type) {
        return switch (type) {
            case THOUGHT -> "感悟";
            case SUMMARY -> "总结";
            case QUESTION -> "疑问";
            case INSIGHT -> "洞察";
        };
    }
}