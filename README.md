# 读书笔记管理系统

一个基于 Spring Boot + Vue 3 的读书笔记管理软件，采用前后端分离架构。

## 技术栈

### 后端
- Spring Boot 3.2.5
- Spring Data JPA
- Spring Security + JWT
- H2 Database（开发）/ PostgreSQL（生产）
- Flyway（数据库迁移）
- Lombok
- SpringDoc OpenAPI

### 前端
- Vue 3
- Element Plus
- Axios
- Vue Router
- Pinia
- Vite

## 功能特性

- 📚 **书架管理**：管理书籍信息、阅读状态、进度跟踪
- 💭 **读书感悟**：记录读书心得、笔记，支持分类和标签
- ✨ **金句收藏**：收藏精彩语句，随机展示功能
- 📥 **书单导入**：支持从豆瓣、微信读书、Calibre 导入书单
- 👤 **用户系统**：注册、登录、JWT认证
- 📊 **统计功能**：阅读统计、进度展示

## 项目结构

```
BookRecord/
├── backend/                    # 后端项目
│   ├── src/main/
│   │   ├── java/com/bookrecord/
│   │   │   ├── config/        # 配置类
│   │   │   ├── controller/    # 控制器
│   │   │   ├── service/       # 服务层
│   │   │   ├── repository/    # 数据访问层
│   │   │   ├── entity/        # 实体类
│   │   │   ├── dto/           # 数据传输对象
│   │   │   ├── security/      # 安全相关
│   │   │   └── exception/     # 异常处理
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/  # Flyway迁移脚本
│   └── pom.xml
└── frontend/                   # 前端项目
    ├── src/
    │   ├── views/             # 页面组件
    │   ├── components/        # 通用组件
    │   ├── api/               # API调用
    │   ├── router/            # 路由
    │   └── stores/            # 状态管理
    └── package.json
```

## 快速开始

### 前置要求

- Java 17+
- Maven 3.6+
- Node.js 16+
- npm 或 yarn

### 后端启动

1. 进入后端目录：
```bash
cd backend
```

2. 构建项目：
```bash
mvn clean install
```

3. 启动应用（开发环境使用H2内存数据库）：
```bash
mvn spring-boot:run
```

4. 访问 Swagger API 文档：
```
http://localhost:8080/swagger-ui.html
```

5. 访问 H2 数据库控制台（开发环境）：
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:book_record
Username: sa
Password: (留空)
```

### 前端启动

1. 进入前端目录：
```bash
cd frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

4. 访问应用：
```
http://localhost:5173
```

## 数据库配置

### 开发环境（H2）

默认使用 H2 内存数据库，无需额外配置。每次重启应用会重建表结构。

### 生产环境（PostgreSQL）

1. 创建 PostgreSQL 数据库：
```sql
CREATE DATABASE book_record;
```

2. 修改 `application.yml`：
```yaml
spring:
  profiles:
    active: prod
```

3. 或设置环境变量：
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

4. Flyway 会自动执行数据库迁移脚本

## API 接口

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户信息
- `POST /api/auth/logout` - 用户登出

### 书籍管理
- `GET /api/v1/books` - 获取书籍列表
- `GET /api/v1/books/{id}` - 获取书籍详情
- `POST /api/v1/books` - 创建书籍
- `PUT /api/v1/books/{id}` - 更新书籍
- `DELETE /api/v1/books/{id}` - 删除书籍
- `PATCH /api/v1/books/{id}/status` - 更新阅读状态
- `PATCH /api/v1/books/{id}/progress` - 更新阅读进度
- `GET /api/v1/books/search` - 搜索书籍
- `GET /api/v1/books/statistics` - 获取统计数据

### 读书感悟
- `GET /api/v1/books/{bookId}/notes` - 获取书籍的感悟列表
- `POST /api/v1/books/{bookId}/notes` - 为书籍创建感悟
- `POST /api/v1/quotes/{quoteId}/notes` - 为金句创建感悟
- `GET /api/v1/notes/{id}` - 获取感悟详情
- `PUT /api/v1/notes/{id}` - 更新感悟
- `DELETE /api/v1/notes/{id}` - 删除感悟
- `GET /api/v1/notes/search` - 搜索感悟

### 金句收藏
- `GET /api/v1/books/{bookId}/quotes` - 获取书籍的金句列表
- `POST /api/v1/books/{bookId}/quotes` - 创建金句
- `GET /api/v1/quotes/{id}` - 获取金句详情
- `PUT /api/v1/quotes/{id}` - 更新金句
- `DELETE /api/v1/quotes/{id}` - 删除金句
- `GET /api/v1/quotes/random` - 随机获取金句
- `GET /api/v1/quotes/search` - 搜索金句

### 书单导入
- `POST /api/v1/books/import/preview` - 上传文件预览导入内容
- `POST /api/v1/books/import/confirm` - 确认导入

## 环境变量

### 后端
- `SPRING_PROFILES_ACTIVE` - Spring 配置环境（dev/prod）
- `DB_USERNAME` - 数据库用户名
- `DB_PASSWORD` - 数据库密码
- `JWT_SECRET` - JWT 密钥（生产环境必须设置）

### 前端
前端通过 Vite 代理连接后端 API，默认代理到 `http://localhost:8080`

## 生产部署

### 后端打包
```bash
cd backend
mvn clean package
java -jar target/book-record-1.0.0-SNAPSHOT.jar
```

### 前端打包
```bash
cd frontend
npm run build
```

打包后的文件在 `frontend/dist` 目录，可以部署到任何静态文件服务器。

## 注意事项

1. **安全配置**：生产环境必须设置强密码的 `JWT_SECRET` 环境变量
2. **数据库切换**：从 H2 切换到 PostgreSQL 只需修改配置，无需修改代码
3. **CORS配置**：生产环境需要修改 `CorsConfig.java` 中的允许域名
4. **文件上传**：书籍封面图片存储需要配置（可使用本地存储或云存储）

## 书单导入指南

系统支持从以下来源导入书单：

### 1. 豆瓣读书

由于豆瓣没有官方导出功能，我们提供了油猴脚本帮助导出：

1. 安装 [Tampermonkey](https://www.tampermonkey.net/) 浏览器扩展
2. 安装脚本：`tools/douban-book-export.user.js`
3. 访问你的豆瓣个人书架页面（想读/在读/读过）
4. 点击页面右侧的"📥 导出书单"按钮
5. 下载生成的 CSV 文件
6. 在系统中点击"导入书单"，上传 CSV 文件

### 2. Calibre

Calibre 是开源的电子书管理软件，导出步骤：

1. 打开 Calibre
2. 选择要导出的书籍（Ctrl+A 全选）
3. 点击 **转换书籍** 旁的小箭头 → **创建目录**
4. 格式选择 **CSV**
5. 选择要包含的字段：title, authors, isbn, publisher, tags, rating
6. 生成并保存 CSV 文件
7. 在系统中点击"导入书单"，上传 CSV 文件

### 3. 微信读书

微信读书暂无官方导出功能，如有第三方工具导出的 CSV 文件，也可尝试导入。

### CSV 格式要求

系统支持以下字段（中英文均可）：

| 字段名 | 中文 | 说明 |
|--------|------|------|
| title | 书名 | 必填 |
| author / authors | 作者 | 可选 |
| isbn | ISBN | 可选，用于去重 |
| rating | 评分 | 可选，1-5 |
| status | 阅读状态 | 想读/在读/读过 |
| publisher | 出版社 | 可选 |
| tags | 标签 | 可选 |
| notes | 简介/笔记 | 可选 |

## 开发计划

- [ ] 书籍封面图片上传功能
- [ ] 阅读统计图表展示
- [ ] 书籍标签系统
- [ ] 阅读计划制定
- [ ] 数据导入导出功能
- [ ] 移动端适配优化

## 许可证

Apache License 2.0