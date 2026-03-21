// ==UserScript==
// @name         豆瓣书单导出工具
// @namespace    https://bookrecord.app/
// @version      1.5.0
// @description  从豆瓣书单或个人书架导出书籍为CSV格式，支持导入到 Book Record 系统，包含封面URL
// @author       Book Record Team
// @match        https://www.douban.com/doulist/*
// @match        https://book.douban.com/people/*/do*
// @match        https://book.douban.com/people/*/wish*
// @match        https://book.douban.com/people/*/collect*
// @grant        none
// @license      MIT
// ==/UserScript==

/**
 * 豆瓣书单导出油猴脚本 v1.5.0
 *
 * 支持的页面类型：
 * 1. 豆瓣书单页面：https://www.douban.com/doulist/xxx/
 * 2. 个人书架页面：https://book.douban.com/people/xxx/wish|do|collect
 *
 * DOM结构（豆瓣书单）：
 * .doulist-item > .mod > .bd.doulist-subject > .post > a (书籍链接)
 *
 * v1.5.0 更新：
 * - 新增封面URL提取功能
 * - CSV输出新增"封面"列
 */

(function() {
    'use strict';

    // ==================== 配置 ====================

    const STATUS_MAP = {
        'wish': '想读',
        'do': '在读',
        'collect': '读过'
    };

    const FETCH_DELAY = 1000;
    const DOULIST_PAGE_SIZE = 25;
    const BOOKSHELF_PAGE_SIZE = 30;

    // ==================== 工具函数 ====================

    function delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    function getPageType() {
        if (window.location.hostname === 'www.douban.com' && window.location.pathname.includes('/doulist/')) {
            return 'doulist';
        }
        return 'bookshelf';
    }

    function getCurrentStatus() {
        const url = window.location.href;
        if (url.includes('/wish')) return STATUS_MAP['wish'];
        if (url.includes('/do')) return STATUS_MAP['do'];
        if (url.includes('/collect')) return STATUS_MAP['collect'];
        return '想读';
    }

    function getDoulistId() {
        const match = window.location.pathname.match(/\/doulist\/(\d+)/);
        return match ? match[1] : null;
    }

    function getUserId() {
        const match = window.location.pathname.match(/\/people\/([^/]+)\//);
        return match ? match[1] : null;
    }

    function escapeCsvField(field) {
        if (!field) return '';
        const str = String(field);
        if (str.includes(',') || str.includes('"') || str.includes('\n') || str.includes('\r')) {
            return '"' + str.replace(/"/g, '""') + '"';
        }
        return str;
    }

    function convertToCsv(books) {
        // v1.5.0: 新增"封面"列
        const headers = ['书名', '作者', 'ISBN', '评分', '阅读状态', '出版社', '标签', '简介', '封面'];
        const headerLine = headers.join(',');

        const dataLines = books.map(book => {
            return [
                escapeCsvField(book.title),
                escapeCsvField(book.author),
                escapeCsvField(book.isbn),
                escapeCsvField(book.rating),
                escapeCsvField(book.status),
                escapeCsvField(book.publisher),
                escapeCsvField(book.tags),
                escapeCsvField(book.summary),
                escapeCsvField(book.coverUrl)
            ].join(',');
        });

        return '\uFEFF' + headerLine + '\n' + dataLines.join('\n');
    }

    function downloadCsv(content, filename) {
        const blob = new Blob([content], { type: 'text/csv;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
    }

    // ==================== 豆瓣书单抓取 ====================

    /**
     * 从豆瓣书单项中提取书籍信息
     * DOM结构: .doulist-item > .mod > .bd.doulist-subject
     */
    function extractDoulistItem(itemElement) {
        const book = {
            title: '',
            author: '',
            isbn: '',
            rating: '',
            status: '想读',
            publisher: '',
            tags: '',
            summary: '',
            coverUrl: ''  // v1.5.0: 新增封面URL字段
        };

        try {
            // 尝试从 .bd.doulist-subject 获取内容
            const subjectDiv = itemElement.querySelector('.bd.doulist-subject') || itemElement;
            const fullText = subjectDiv.textContent || subjectDiv.innerText;

            // v1.5.0: 提取封面URL
            // 豆瓣书单页面的封面通常在 img 标签中
            const coverImg = subjectDiv.querySelector('img') || itemElement.querySelector('img');
            if (coverImg) {
                let coverSrc = coverImg.getAttribute('src');
                if (coverSrc) {
                    // 处理豆瓣图片URL：将小图替换为大图
                    // /spic/ (small) -> /lpic/ (large)
                    if (coverSrc.includes('/spic/')) {
                        coverSrc = coverSrc.replace('/spic/', '/lpic/');
                    }
                    // /s/ (small) -> /l/ (large)
                    if (coverSrc.includes('/s/')) {
                        coverSrc = coverSrc.replace('/s/', '/l/');
                    }
                    book.coverUrl = coverSrc;
                }
            }

            // 提取书名 - 从标题链接
            const titleLink = subjectDiv.querySelector('a[href*="book.douban.com/subject/"]');
            if (titleLink) {
                // 书名链接可能包含图片，取链接的 title 属性或者找附近的文本
                let title = titleLink.getAttribute('title');
                if (!title) {
                    // 查找 .title 类
                    const titleEl = subjectDiv.querySelector('.title');
                    title = titleEl ? titleEl.textContent.trim() : titleLink.textContent.trim();
                }
                book.title = title;
            }

            // 提取评分 - 格式: "6.8 (2371人评价)"
            const ratingMatch = fullText.match(/(\d+\.?\d*)\s*\(\d+人评价\)/);
            if (ratingMatch) {
                book.rating = ratingMatch[1];
            }

            // 提取作者、出版社等信息
            // 通常格式: "作者: xxx / 出版社: xxx / 出版年: xxx"
            const lines = fullText.split('\n').map(l => l.trim()).filter(l => l);

            for (const line of lines) {
                // 处理带 / 分隔的行
                if (line.includes('作者:') || line.includes('作者：')) {
                    const authorPart = line.split('/').find(p => p.includes('作者'));
                    if (authorPart) {
                        book.author = authorPart.replace(/作者[：:]\s*/, '').trim();
                    }
                }

                if (line.includes('出版社:') || line.includes('出版社：')) {
                    const publisherPart = line.split('/').find(p => p.includes('出版社'));
                    if (publisherPart) {
                        book.publisher = publisherPart.replace(/出版社[：:]\s*/, '').trim();
                    }
                }

                if (line.includes('ISBN:') || line.includes('ISBN：')) {
                    const isbnPart = line.split('/').find(p => p.includes('ISBN'));
                    if (isbnPart) {
                        book.isbn = isbnPart.replace(/ISBN[：:]\s*/, '').trim();
                    }
                }
            }

            // 如果上面没找到，尝试从整体文本解析
            if (!book.author) {
                const authorMatch = fullText.match(/作者[：:]\s*([^/\n]+)/);
                if (authorMatch) {
                    book.author = authorMatch[1].trim();
                }
            }

            if (!book.publisher) {
                const publisherMatch = fullText.match(/出版社[：:]\s*([^/\n]+)/);
                if (publisherMatch) {
                    book.publisher = publisherMatch[1].trim();
                }
            }

            // 提取推荐语/评语
            const commentEl = subjectDiv.querySelector('blockquote, .comment, .note');
            if (commentEl) {
                book.summary = commentEl.textContent.trim();
            }

        } catch (e) {
            console.error('提取书籍信息失败:', e);
        }

        return book;
    }

    /**
     * 从当前豆瓣书单页面抓取书籍
     */
    function extractDoulistFromCurrentPage() {
        const books = [];

        // 使用正确的选择器 .doulist-item（连字符）
        const items = document.querySelectorAll('.doulist-item');
        console.log(`找到 ${items.length} 个 .doulist-item 元素`);

        items.forEach((item, index) => {
            // 检查是否包含书籍链接
            const bookLink = item.querySelector('a[href*="book.douban.com/subject/"]');
            if (bookLink) {
                const book = extractDoulistItem(item);
                if (book.title) {
                    books.push(book);
                    console.log(`[${index + 1}] 提取到: ${book.title} - ${book.author} (${book.rating}分)`);
                } else {
                    // 尝试从链接获取书名
                    const title = bookLink.getAttribute('title') || bookLink.textContent.trim();
                    if (title) {
                        books.push({
                            title: title,
                            author: '',
                            isbn: '',
                            rating: '',
                            status: '想读',
                            publisher: '',
                            tags: '',
                            summary: ''
                        });
                        console.log(`[${index + 1}] 提取到(仅书名): ${title}`);
                    }
                }
            }
        });

        return books;
    }

    /**
     * 获取豆瓣书单总页数
     */
    function getDoulistTotalPages() {
        const paginator = document.querySelector('.paginator');
        if (!paginator) return 1;

        const currentPage = paginator.querySelector('.thispage');
        if (currentPage) {
            const totalAttr = currentPage.getAttribute('data-total-page');
            if (totalAttr) {
                return parseInt(totalAttr, 10);
            }
        }

        const pageLinks = paginator.querySelectorAll('a');
        let maxPage = 1;
        pageLinks.forEach(link => {
            const text = link.textContent.trim();
            const pageNum = parseInt(text, 10);
            if (!isNaN(pageNum) && pageNum > maxPage) {
                maxPage = pageNum;
            }
        });

        return maxPage;
    }

    /**
     * 抓取豆瓣书单指定页面
     */
    async function fetchDoulistPage(doulistId, page) {
        const start = (page - 1) * DOULIST_PAGE_SIZE;
        const url = `https://www.douban.com/doulist/${doulistId}/?start=${start}`;

        try {
            const response = await fetch(url, {
                credentials: 'include'
            });
            const html = await response.text();

            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');

            const books = [];
            const items = doc.querySelectorAll('.doulist-item');

            items.forEach(item => {
                const bookLink = item.querySelector('a[href*="book.douban.com/subject/"]');
                if (bookLink) {
                    const book = extractDoulistItem(item);
                    if (book.title) {
                        books.push(book);
                    }
                }
            });

            return books;
        } catch (e) {
            console.error(`抓取第 ${page} 页失败:`, e);
            return [];
        }
    }

    /**
     * 抓取豆瓣书单所有页面
     */
    async function fetchAllDoulistBooks(onProgress) {
        const doulistId = getDoulistId();
        const totalPages = getDoulistTotalPages();

        console.log(`开始抓取书单: ID=${doulistId}, 总页数=${totalPages}`);

        let allBooks = [];

        for (let page = 1; page <= totalPages; page++) {
            onProgress && onProgress(page, totalPages);

            if (page === 1) {
                const books = extractDoulistFromCurrentPage();
                allBooks = allBooks.concat(books);
            } else {
                const books = await fetchDoulistPage(doulistId, page);
                allBooks = allBooks.concat(books);
                await delay(FETCH_DELAY);
            }
        }

        return allBooks;
    }

    // ==================== 个人书架抓取 ====================

    function extractBookshelfItem(itemElement) {
        const book = {
            title: '',
            author: '',
            isbn: '',
            rating: '',
            status: getCurrentStatus(),
            publisher: '',
            tags: '',
            summary: '',
            coverUrl: ''  // v1.5.0: 新增封面URL字段
        };

        try {
            // v1.5.0: 提取封面URL
            // 个人书架页面的封面通常在 .cover img 或直接 img 中
            const coverImg = itemElement.querySelector('.cover img') || itemElement.querySelector('img');
            if (coverImg) {
                let coverSrc = coverImg.getAttribute('src');
                if (coverSrc) {
                    // 处理豆瓣图片URL：将小图替换为大图
                    if (coverSrc.includes('/spic/')) {
                        coverSrc = coverSrc.replace('/spic/', '/lpic/');
                    }
                    if (coverSrc.includes('/s/')) {
                        coverSrc = coverSrc.replace('/s/', '/l/');
                    }
                    book.coverUrl = coverSrc;
                }
            }

            const titleEl = itemElement.querySelector('.title a');
            if (titleEl) {
                book.title = titleEl.textContent.trim();
            }

            const pubEl = itemElement.querySelector('.pub');
            if (pubEl) {
                const pubText = pubEl.textContent.trim();
                const parts = pubText.split('/').map(p => p.trim());

                if (parts.length > 0) {
                    book.author = parts[0];
                }

                for (let i = 1; i < parts.length; i++) {
                    const part = parts[i];
                    if (part && !/^\d/.test(part) && !part.includes('ISBN')) {
                        book.publisher = part;
                        break;
                    }
                }

                for (const part of parts) {
                    if (part.includes('ISBN')) {
                        book.isbn = part.replace('ISBN', '').trim();
                        break;
                    }
                }
            }

            const ratingSpan = itemElement.querySelector('[class*="rating"]');
            if (ratingSpan) {
                const ratingClass = ratingSpan.className;
                const match = ratingClass.match(/rating(\d)-t/);
                if (match) {
                    book.rating = match[1];
                }
            }

            const tagsEl = itemElement.querySelector('.tags');
            if (tagsEl) {
                const tagText = tagsEl.textContent.trim();
                book.tags = tagText.replace(/^标签:\s*/, '').trim();
            }

            const commentEl = itemElement.querySelector('.comment');
            if (commentEl) {
                book.summary = commentEl.textContent.trim();
            }

        } catch (e) {
            console.error('提取书籍信息失败:', e);
        }

        return book;
    }

    function extractBookshelfFromCurrentPage() {
        const books = [];
        const items = document.querySelectorAll('.item');

        items.forEach(item => {
            const book = extractBookshelfItem(item);
            if (book.title) {
                books.push(book);
            }
        });

        return books;
    }

    function getBookshelfTotalPages() {
        const paginator = document.querySelector('.paginator');
        if (!paginator) return 1;

        const pageLinks = paginator.querySelectorAll('a');
        let maxPage = 1;

        pageLinks.forEach(link => {
            const text = link.textContent.trim();
            const pageNum = parseInt(text, 10);
            if (!isNaN(pageNum) && pageNum > maxPage) {
                maxPage = pageNum;
            }
        });

        return maxPage;
    }

    async function fetchBookshelfPage(userId, status, page) {
        const statusParam = status === '想读' ? 'wish' :
                           status === '在读' ? 'do' : 'collect';

        const url = `https://book.douban.com/people/${userId}/${statusParam}?start=${(page - 1) * BOOKSHELF_PAGE_SIZE}`;

        try {
            const response = await fetch(url, {
                credentials: 'include'
            });
            const html = await response.text();

            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');

            const books = [];
            const items = doc.querySelectorAll('.item');

            items.forEach(item => {
                const book = extractBookshelfItem(item);
                if (book.title) {
                    books.push(book);
                }
            });

            return books;
        } catch (e) {
            console.error(`抓取第 ${page} 页失败:`, e);
            return [];
        }
    }

    async function fetchAllBookshelfBooks(onProgress) {
        const userId = getUserId();
        const status = getCurrentStatus();
        const totalPages = getBookshelfTotalPages();

        console.log(`开始抓取书架: 用户=${userId}, 状态=${status}, 总页数=${totalPages}`);

        let allBooks = [];

        for (let page = 1; page <= totalPages; page++) {
            onProgress && onProgress(page, totalPages);

            if (page === 1) {
                const books = extractBookshelfFromCurrentPage();
                allBooks = allBooks.concat(books);
            } else {
                const books = await fetchBookshelfPage(userId, status, page);
                allBooks = allBooks.concat(books);
                await delay(FETCH_DELAY);
            }
        }

        return allBooks;
    }

    // ==================== UI 组件 ====================

    function createExportButton() {
        const button = document.createElement('button');
        button.id = 'book-record-export-btn';
        button.textContent = '📥 导出书单';
        button.style.cssText = `
            position: fixed;
            top: 100px;
            right: 20px;
            z-index: 9999;
            padding: 12px 24px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: bold;
            cursor: pointer;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
            transition: all 0.3s ease;
        `;

        button.addEventListener('mouseenter', () => {
            button.style.transform = 'translateY(-2px)';
            button.style.boxShadow = '0 6px 20px rgba(102, 126, 234, 0.6)';
        });

        button.addEventListener('mouseleave', () => {
            button.style.transform = 'translateY(0)';
            button.style.boxShadow = '0 4px 15px rgba(102, 126, 234, 0.4)';
        });

        button.addEventListener('click', startExport);

        return button;
    }

    function createProgressDialog() {
        const dialog = document.createElement('div');
        dialog.id = 'book-record-progress-dialog';
        dialog.style.cssText = `
            display: none;
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            z-index: 10000;
            padding: 30px 40px;
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
            text-align: center;
        `;

        dialog.innerHTML = `
            <div style="font-size: 18px; font-weight: bold; margin-bottom: 20px; color: #333;">
                正在导出书单...
            </div>
            <div id="progress-text" style="font-size: 14px; color: #666; margin-bottom: 15px;">
                准备中...
            </div>
            <div style="width: 200px; height: 8px; background: #eee; border-radius: 4px; overflow: hidden; margin: 0 auto;">
                <div id="progress-bar" style="width: 0%; height: 100%; background: linear-gradient(90deg, #667eea, #764ba2); transition: width 0.3s ease;"></div>
            </div>
        `;

        return dialog;
    }

    function updateProgress(current, total) {
        const progressText = document.getElementById('progress-text');
        const progressBar = document.getElementById('progress-bar');

        if (progressText) {
            progressText.textContent = `正在抓取第 ${current} 页 / 共 ${total} 页`;
        }
        if (progressBar) {
            progressBar.style.width = `${(current / total) * 100}%`;
        }
    }

    function showProgressDialog() {
        let dialog = document.getElementById('book-record-progress-dialog');
        if (!dialog) {
            dialog = createProgressDialog();
            document.body.appendChild(dialog);
        }
        dialog.style.display = 'block';
    }

    function hideProgressDialog() {
        const dialog = document.getElementById('book-record-progress-dialog');
        if (dialog) {
            dialog.style.display = 'none';
        }
    }

    // ==================== 主流程 ====================

    async function startExport() {
        const button = document.getElementById('book-record-export-btn');
        button.disabled = true;
        button.textContent = '⏳ 导出中...';

        showProgressDialog();

        try {
            const pageType = getPageType();
            let books;
            let filename;

            if (pageType === 'doulist') {
                books = await fetchAllDoulistBooks(updateProgress);
                const doulistId = getDoulistId();
                const timestamp = new Date().toISOString().slice(0, 10);
                filename = `douban_doulist_${doulistId}_${timestamp}.csv`;
            } else {
                books = await fetchAllBookshelfBooks(updateProgress);
                const userId = getUserId();
                const status = getCurrentStatus();
                const timestamp = new Date().toISOString().slice(0, 10);
                filename = `douban_${userId}_${status}_${timestamp}.csv`;
            }

            console.log(`共抓取 ${books.length} 本书`);

            if (books.length === 0) {
                alert('未找到任何书籍');
                return;
            }

            const csv = convertToCsv(books);
            downloadCsv(csv, filename);

            alert(`导出成功！共 ${books.length} 本书`);

        } catch (e) {
            console.error('导出失败:', e);
            alert('导出失败: ' + e.message);
        } finally {
            hideProgressDialog();
            button.disabled = false;
            button.textContent = '📥 导出书单';
        }
    }

    // ==================== 初始化 ====================

    function init() {
        const pageType = getPageType();
        console.log(`豆瓣书单导出工具 v1.4.0 初始化，页面类型: ${pageType}`);

        if (pageType === 'doulist') {
            const bookLinks = document.querySelectorAll('a[href*="book.douban.com/subject/"]');
            console.log(`找到 ${bookLinks.length} 个书籍链接`);

            if (bookLinks.length === 0) {
                console.log('未找到书籍链接');
                return;
            }
        } else {
            if (!document.querySelector('.item')) {
                console.log('当前书架页面没有书籍');
                return;
            }
        }

        const button = createExportButton();
        document.body.appendChild(button);
        console.log('按钮已添加');
    }

    // 延迟初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => setTimeout(init, 500));
    } else {
        setTimeout(init, 500);
    }

})();