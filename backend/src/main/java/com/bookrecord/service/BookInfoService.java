package com.bookrecord.service;

import com.bookrecord.dto.BookInfoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 图书信息服务类
 * 负责从外部图书API（Google Books API）获取书籍信息
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookInfoService {

    // Google Books API 基础URL
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes";

    // RestTemplate 用于发送HTTP请求
    private final RestTemplate restTemplate = new RestTemplate();

    // ObjectMapper 用于解析JSON响应
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据ISBN查询图书信息
     * 调用Google Books API获取指定ISBN的书籍详细信息
     *
     * @param isbn ISBN编号（可以是10位或13位）
     * @return BookInfoResponse 图书信息，如果未找到返回null
     */
    public BookInfoResponse getBookByIsbn(String isbn) {
        log.info("根据ISBN查询图书信息: {}", isbn);

        try {
            // 构建API请求URL，使用ISBN作为查询条件
            String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_BOOKS_API_URL)
                    .queryParam("q", "isbn:" + isbn)
                    .queryParam("maxResults", 1)
                    .build()
                    .toUriString();

            log.debug("请求Google Books API: {}", url);

            // 发送GET请求并获取响应
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            // 检查是否有搜索结果
            int totalItems = root.path("totalItems").asInt();
            if (totalItems == 0) {
                log.warn("未找到ISBN为 {} 的图书", isbn);
                return null;
            }

            // 解析第一个搜索结果
            JsonNode firstItem = root.path("items").get(0);
            BookInfoResponse bookInfo = parseBookInfo(firstItem);
            bookInfo.setIsbn(isbn);

            log.info("成功获取图书信息: {}", bookInfo.getTitle());
            return bookInfo;

        } catch (Exception e) {
            log.error("查询ISBN {} 时发生错误: {}", isbn, e.getMessage());
            return null;
        }
    }

    /**
     * 根据书名搜索图书信息
     * 调用Google Books API搜索匹配的书籍列表
     *
     * @param title 书名关键词
     * @return List<BookInfoResponse> 匹配的图书信息列表（最多返回10条）
     */
    public List<BookInfoResponse> searchBooksByTitle(String title) {
        log.info("根据书名搜索图书: {}", title);

        List<BookInfoResponse> results = new ArrayList<>();

        try {
            // 构建API请求URL，使用书名作为查询条件
            String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_BOOKS_API_URL)
                    .queryParam("q", "intitle:" + title)
                    .queryParam("maxResults", 10)
                    .queryParam("orderBy", "relevance")
                    .build()
                    .toUriString();

            log.debug("请求Google Books API: {}", url);

            // 发送GET请求并获取响应
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            // 检查是否有搜索结果
            int totalItems = root.path("totalItems").asInt();
            if (totalItems == 0) {
                log.warn("未找到书名包含 {} 的图书", title);
                return results;
            }

            // 遍历所有搜索结果并解析
            JsonNode items = root.path("items");
            for (JsonNode item : items) {
                try {
                    BookInfoResponse bookInfo = parseBookInfo(item);
                    results.add(bookInfo);
                } catch (Exception e) {
                    // 单条记录解析失败不影响其他记录
                    log.warn("解析图书信息失败: {}", e.getMessage());
                }
            }

            log.info("搜索到 {} 本相关图书", results.size());
            return results;

        } catch (Exception e) {
            log.error("搜索书名 {} 时发生错误: {}", title, e.getMessage());
            return results;
        }
    }

    /**
     * 解析Google Books API返回的JSON数据
     * 从单个书籍条目中提取所需信息
     *
     * @param item JSON节点，包含单本书籍的信息
     * @return BookInfoResponse 解析后的图书信息
     */
    private BookInfoResponse parseBookInfo(JsonNode item) {
        // 获取volumeInfo节点，包含书籍详细信息
        JsonNode volumeInfo = item.path("volumeInfo");

        // 提取书名
        String title = volumeInfo.path("title").asText(null);

        // 提取作者列表，多个作者用逗号分隔
        String author = null;
        JsonNode authors = volumeInfo.path("authors");
        if (authors.isArray() && authors.size() > 0) {
            List<String> authorList = new ArrayList<>();
            for (JsonNode a : authors) {
                authorList.add(a.asText());
            }
            author = String.join(", ", authorList);
        }

        // 提取ISBN
        String isbn = null;
        JsonNode industryIdentifiers = volumeInfo.path("industryIdentifiers");
        if (industryIdentifiers.isArray()) {
            for (JsonNode identifier : industryIdentifiers) {
                String type = identifier.path("type").asText();
                // 优先使用ISBN_13，其次ISBN_10
                if ("ISBN_13".equals(type)) {
                    isbn = identifier.path("identifier").asText();
                    break;
                } else if ("ISBN_10".equals(type) && isbn == null) {
                    isbn = identifier.path("identifier").asText();
                }
            }
        }

        // 提取出版社
        String publisher = volumeInfo.path("publisher").asText(null);

        // 提取出版日期
        String publishDate = volumeInfo.path("publishedDate").asText(null);

        // 提取封面图片URL，优先使用中等尺寸
        String coverUrl = null;
        JsonNode imageLinks = volumeInfo.path("imageLinks");
        if (!imageLinks.isMissingNode()) {
            coverUrl = imageLinks.path("thumbnail").asText(null);
            // 将http转换为https以避免混合内容警告
            if (coverUrl != null && coverUrl.startsWith("http:")) {
                coverUrl = coverUrl.replace("http:", "https:");
            }
        }

        // 提取书籍简介
        String description = volumeInfo.path("description").asText(null);

        // 提取总页数
        Integer pageCount = null;
        if (!volumeInfo.path("pageCount").isMissingNode()) {
            pageCount = volumeInfo.path("pageCount").asInt();
        }

        // 提取分类标签，多个分类用逗号分隔
        String categories = null;
        JsonNode categoriesNode = volumeInfo.path("categories");
        if (categoriesNode.isArray() && categoriesNode.size() > 0) {
            List<String> categoryList = new ArrayList<>();
            for (JsonNode c : categoriesNode) {
                categoryList.add(c.asText());
            }
            categories = String.join(", ", categoryList);
        }

        // 构建并返回BookInfoResponse对象
        return BookInfoResponse.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .publisher(publisher)
                .publishDate(publishDate)
                .coverUrl(coverUrl)
                .description(description)
                .pageCount(pageCount)
                .categories(categories)
                .source("Google Books")
                .build();
    }
}