package com.bookrecord.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 图片代理控制器
 * 用于代理第三方图片请求，绕过防盗链机制
 *
 * @author Book Record Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "图片代理", description = "代理第三方图片请求接口")
public class ImageProxyController {

    /**
     * 允许代理的图片域名白名单
     * 只允许代理豆瓣和其他可信域名的图片
     */
    private static final Set<String> ALLOWED_DOMAINS = new HashSet<>(Arrays.asList(
            "img1.doubanio.com",
            "img2.doubanio.com",
            "img3.doubanio.com",
            "img4.doubanio.com",
            "img5.doubanio.com",
            "img6.doubanio.com",
            "img7.doubanio.com",
            "img8.doubanio.com",
            "img9.doubanio.com",
            "book.douban.com"
    ));

    /**
     * 代理图片请求
     * 用于绕过豆瓣等网站的防盗链机制
     *
     * @param url 图片URL（需URL编码）
     * @return 图片二进制数据
     */
    @GetMapping("/image-proxy")
    @Operation(summary = "代理图片请求", description = "代理第三方图片请求，绑过防盗链机制")
    public ResponseEntity<byte[]> proxyImage(@RequestParam("url") String imageUrl) {
        try {
            // 验证URL是否在白名单中
            URL url = new URL(imageUrl);
            String host = url.getHost();

            if (!ALLOWED_DOMAINS.contains(host)) {
                log.warn("拒绝代理非白名单域名的图片: {}", host);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 创建HTTP连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // 设置请求头，模拟浏览器请求
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            connection.setRequestProperty("Referer", "https://book.douban.com/");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.warn("图片请求失败: {} - {}", responseCode, imageUrl);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            // 获取Content-Type
            String contentType = connection.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "image/jpeg"; // 默认JPEG
            }

            // 读取图片数据
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] imageData = inputStream.readAllBytes();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                // 缓存7天
                headers.setCacheControl("max-age=604800, public");

                log.debug("成功代理图片: {} ({} bytes)", imageUrl, imageData.length);
                return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
            }

        } catch (Exception e) {
            log.error("代理图片失败: {} - {}", imageUrl, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}