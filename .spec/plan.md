# Book Record 技术架构设计

> 本文档记录读书笔记管理系统的技术架构、目录结构、数据模型和核心组件关系，作为后续开发的架构基准。

---

## 1. 项目概述

### 1.1 架构模式

**前后端分离架构**
- 前端：Vue 3 SPA 应用（**使用 frontend-design 插件生成**）
- 后端：Spring Boot REST API
- 通信：HTTP/JSON，JWT 认证
- 部署：可独立部署，前端静态资源可部署至 CDN

**前端开发模式**：
- ⭐ **核心依赖**：frontend-design 插件（AI 驱动的页面生成工具）
- ⭐ **开发流程**：插件生成 UI → 开发者集成业务逻辑
- ⭐ **设计理念**：避免手动编写前端代码，提高开发效率和代码质量

### 1.2 当前状态

- **开发进度**：核心功能已完成
- **数据库**：H2 内存数据库（开发）/ PostgreSQL（生产）
- **认证方式**：JWT Token
- **API 文档**：Swagger UI

---

## 2. 目录结构

### 2.1 后端目录结构

```
backend/
├── src/main/
│   ├── java/com/bookrecord/
│   │   ├── BookRecordApplication.java      # 应用入口
│   │   ├── config/                          # 配置类
│   │   │   ├── CorsConfig.java             # CORS跨域配置
│   │   │   └── SwaggerConfig.java          # Swagger API文档配置
│   │   ├── controller/                      # REST控制器层
│   │   │   ├── AuthController.java         # 认证接口
│   │   │   ├── BookController.java         # 书籍管理接口
│   │   │   ├── ReadingNoteController.java  # 读书感悟接口
│   │   │   └── QuoteController.java        # 金句收藏接口
│   │   ├── service/                         # 业务逻辑层
│   │   │   ├── UserService.java            # 用户服务
│   │   │   ├── BookService.java            # 书籍服务
│   │   │   ├── ReadingNoteService.java     # 感悟服务
│   │   │   └── QuoteService.java           # 金句服务
│   │   ├── repository/                      # 数据访问层
│   │   │   ├── UserRepository.java
│   │   │   ├── BookRepository.java
│   │   │   ├── ReadingNoteRepository.java
│   │   │   └── QuoteRepository.java
│   │   ├── entity/                          # JPA实体类
│   │   │   ├── User.java                   # 用户实体
│   │   │   ├── Book.java                   # 书籍实体
│   │   │   ├── ReadingNote.java            # 感悟实体
│   │   │   └── Quote.java                  # 金句实体
│   │   ├── dto/                             # 数据传输对象
│   │   │   ├── ApiResponse.java            # 统一响应格式
│   │   │   ├── LoginRequest.java           # 登录请求
│   │   │   ├── RegisterRequest.java        # 注册请求
│   │   │   ├── JwtAuthenticationResponse.java
│   │   │   ├── UserInfo.java               # 用户信息响应
│   │   │   ├── BookRequest.java            # 书籍请求
│   │   │   ├── BookResponse.java           # 书籍响应
│   │   │   ├── BookStatistics.java         # 统计数据
│   │   │   ├── ReadingProgressRequest.java # 进度更新请求
│   │   │   ├── ReadingNoteRequest.java     # 感悟请求
│   │   │   ├── ReadingNoteResponse.java    # 感悟响应
│   │   │   ├── QuoteRequest.java           # 金句请求
│   │   │   └── QuoteResponse.java          # 金句响应
│   │   ├── security/                        # 安全相关
│   │   │   ├── SecurityConfig.java         # Spring Security配置
│   │   │   ├── JwtTokenProvider.java       # JWT工具类
│   │   │   ├── JwtAuthenticationFilter.java # JWT过滤器
│   │   │   └── CustomUserDetailsService.java # 用户详情服务
│   │   └── exception/                       # 异常处理
│   │       ├── ResourceNotFoundException.java
│   │       ├── BadRequestException.java
│   │       └── GlobalExceptionHandler.java
│   └── resources/
│       ├── application.yml                 # 主配置文件
│       ├── application-dev.yml             # 开发环境配置
│       ├── application-prod.yml            # 生产环境配置
│       └── db/migration/                    # Flyway数据库迁移
│           └── V1__Init_schema.sql
├── pom.xml                                 # Maven依赖配置
└── target/                                 # 编译输出目录
```

**分层架构**：
```
Controller → Service → Repository → Entity
    ↓          ↓
   DTO      Business Logic
```

### 2.2 前端目录结构

```
frontend/
├── src/
│   ├── main.js              # 应用入口
│   ├── App.vue              # 根组件
│   ├── api/                 # API接口模块（手动维护）
│   │   ├── index.js        # Axios实例配置
│   │   └── modules.js      # API模块化封装
│   ├── router/             # 路由配置（手动维护）
│   │   └── index.js        # 路由定义和守卫
│   ├── stores/             # Pinia状态管理（手动维护）
│   │   └── user.js         # 用户状态
│   ├── views/              # ⭐ 页面组件（frontend-design 生成）
│   │   ├── Login.vue       # 登录页
│   │   ├── Register.vue    # 注册页
│   │   ├── Dashboard.vue   # 首页仪表板
│   │   ├── Bookshelf.vue   # 书架列表
│   │   ├── BookDetail.vue  # 书籍详情
│   │   ├── ReadingNotes.vue # 感悟列表
│   │   └── Quotes.vue      # 金句列表
│   └── components/         # ⭐ 通用组件（frontend-design 生成）
│       └── Layout.vue      # 布局组件
├── vite.config.js          # Vite配置
├── package.json            # NPM依赖配置
├── node_modules/           # 依赖包
└── dist/                   # 构建输出目录
```

**目录说明**：

| 目录/文件 | 维护方式 | 说明 |
|----------|---------|------|
| `api/` | 手动维护 | API 调用逻辑，连接后端接口 |
| `router/` | 手动维护 | 路由配置和导航守卫 |
| `stores/` | 手动维护 | 全局状态管理 |
| `views/` | **frontend-design 生成** | 所有页面组件由插件生成 |
| `components/` | **frontend-design 生成** | 可复用 UI 组件由插件生成 |
| `main.js` | 手动维护 | 应用初始化配置 |
| `App.vue` | 手动维护 | 根组件 |

**重要原则**：
- ⭐ `views/` 和 `components/` 目录下的文件必须通过 frontend-design 插件生成
- 手动维护的文件仅限：API 模块、路由配置、状态管理、入口文件
- 如需修改页面 UI，应重新使用插件生成，而非直接编辑代码

### 2.3 项目根目录

```
BookRecord/
├── backend/                # 后端项目
├── frontend/               # 前端项目
├── .spec/                  # 规格文档
│   ├── constitution.md    # 项目宪法
│   ├── spec.md            # 产品规格
│   ├── plan.md            # 架构设计（本文档）
│   └── tasks.md           # 任务列表
├── README.md               # 项目说明
└── .git/                   # Git版本控制
```

---

## 3. 核心技术栈

### 3.1 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Spring Boot** | 3.2.5 | 应用框架 |
| **Spring Data JPA** | - | ORM持久化 |
| **Spring Security** | - | 安全框架 |
| **Spring Validation** | - | 参数验证 |
| **H2 Database** | - | 开发环境内存数据库 |
| **PostgreSQL** | - | 生产环境数据库 |
| **Flyway** | - | 数据库版本管理 |
| **JJWT** | 0.12.5 | JWT Token生成与验证 |
| **Lombok** | - | 代码简化 |
| **SpringDoc OpenAPI** | 2.3.0 | API文档生成 |

**技术选型理由**：
- Spring Boot 3.x：最新稳定版本，支持 JDK 17+
- JPA：简化数据库操作，支持多种数据库
- JWT：无状态认证，适合前后端分离
- Flyway：数据库版本控制，团队协作必备

### 3.2 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **Vue** | 3.4.21 | 前端框架（Composition API） |
| **Vue Router** | 4.3.0 | 路由管理 |
| **Pinia** | 2.1.7 | 状态管理 |
| **Axios** | 1.6.8 | HTTP客户端 |
| **Element Plus** | 2.6.1 | UI组件库 |
| **Element Plus Icons** | 2.3.1 | 图标库 |
| **Vite** | 5.2.0 | 构建工具 |
| **frontend-design** | - | **前端页面生成工具（核心依赖）** |

**技术选型理由**：
- Vue 3：Composition API，更好的 TypeScript 支持
- Pinia：Vue 3 官方推荐的状态管理
- Element Plus：成熟的 Vue 3 UI 组件库
- Vite：快速的开发服务器和构建工具
- **frontend-design**：**AI驱动的页面生成工具，负责所有前端UI的创建和维护**

**重要说明**：
- ⚠️ **frontend-design 是前端开发的核心依赖，所有页面组件必须通过该插件生成**
- ⚠️ **禁止手动编写或硬编码前端页面代码**
- 前端开发流程：使用 frontend-design 插件生成页面 → 集成 API 调用逻辑 → 测试验证

---

## 4. 数据模型设计

### 4.1 数据库ER图

```
┌─────────────┐
│    users    │
├─────────────┤
│ id (PK)     │
│ username    │
│ email       │
│ password    │
│ nickname    │
│ avatar_url  │
│ created_at  │
│ updated_at  │
└──────┬──────┘
       │ 1
       │
       │ N
┌──────┴──────┐
│    books    │
├─────────────┤
│ id (PK)     │
│ user_id(FK) │──────┐
│ title       │      │
│ author      │      │
│ isbn        │      │
│ reading_status│    │
│ page_count  │      │
│ current_page│      │
│ rating      │      │
│ ...         │      │
└──────┬──────┘      │
       │ 1           │
       │             │
       ├─────────────┘ N
       │
       │ N
┌──────┴──────────┐
│ reading_notes   │
├─────────────────┤
│ id (PK)         │
│ book_id (FK)    │
│ quote_id (FK)   │───┐
│ title           │   │
│ content         │   │
│ note_type       │   │
│ chapter         │   │
│ page_number     │   │
│ tags            │   │
│ is_private      │   │
└─────────────────┘   │
                      │
┌─────────────────┐   │
│    quotes       │   │
├─────────────────┤   │
│ id (PK)         │◄──┘
│ book_id (FK)    │
│ content         │
│ chapter         │
│ page_number     │
│ note            │
│ color           │
│ tags            │
└─────────────────┘
```

### 4.2 TypeScript Interfaces（前端数据模型）

```typescript
// ==================== 枚举类型 ====================

/** 阅读状态 */
export enum ReadingStatus {
  WANT_TO_READ = 'WANT_TO_READ',    // 想读
  READING = 'READING',               // 在读
  COMPLETED = 'COMPLETED',           // 已完成
  ABANDONED = 'ABANDONED'            // 已放弃
}

/** 感悟类型 */
export enum NoteType {
  THOUGHT = 'THOUGHT',    // 感悟
  SUMMARY = 'SUMMARY',    // 总结
  QUESTION = 'QUESTION',  // 疑问
  INSIGHT = 'INSIGHT'     // 洞察
}

// ==================== 实体接口 ====================

/** 用户 */
export interface User {
  id: number
  username: string
  email: string
  nickname?: string
  avatarUrl?: string
  createdAt: string
  updatedAt: string
}

/** 书籍 */
export interface Book {
  id: number
  title: string
  author?: string
  coverUrl?: string
  isbn?: string
  publisher?: string
  publishDate?: string
  readingStatus: ReadingStatus
  startDate?: string
  finishDate?: string
  rating?: number
  pageCount?: number
  currentPage: number
  description?: string
  progress?: number  // 计算字段：阅读进度百分比
  createdAt: string
  updatedAt: string
}

/** 读书感悟 */
export interface ReadingNote {
  id: number
  bookId: number
  quoteId?: number
  chapter?: string
  pageNumber?: number
  title?: string
  content: string
  noteType: NoteType
  tags?: string
  isPrivate: boolean
  createdAt: string
  updatedAt: string
}

/** 金句 */
export interface Quote {
  id: number
  bookId: number
  content: string
  chapter?: string
  pageNumber?: number
  note?: string
  color?: string
  tags?: string
  createdAt: string
  updatedAt: string
}

// ==================== 请求DTO ====================

/** 登录请求 */
export interface LoginRequest {
  usernameOrEmail: string
  password: string
}

/** 注册请求 */
export interface RegisterRequest {
  username: string
  email: string
  password: string
  nickname?: string
}

/** 创建书籍请求 */
export interface BookRequest {
  title: string
  author?: string
  coverUrl?: string
  isbn?: string
  publisher?: string
  publishDate?: string
  pageCount?: number
  description?: string
}

/** 更新书籍请求 */
export interface BookUpdateRequest extends Partial<BookRequest> {
  readingStatus?: ReadingStatus
  rating?: number
  currentPage?: number
}

/** 更新阅读进度请求 */
export interface ReadingProgressRequest {
  currentPage: number
}

/** 创建感悟请求 */
export interface ReadingNoteRequest {
  title?: string
  content: string
  noteType: NoteType
  chapter?: string
  pageNumber?: number
  tags?: string
  isPrivate?: boolean
}

/** 创建金句请求 */
export interface QuoteRequest {
  content: string
  chapter?: string
  pageNumber?: number
  note?: string
  color?: string
  tags?: string
}

// ==================== 响应DTO ====================

/** 统一API响应 */
export interface ApiResponse<T = any> {
  success: boolean
  message?: string
  data?: T
  timestamp: string
}

/** 分页响应 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

/** JWT认证响应 */
export interface JwtAuthResponse {
  accessToken: string
  tokenType: string
  userInfo: User
}

/** 书籍统计 */
export interface BookStatistics {
  totalBooks: number
  wantToRead: number
  reading: number
  completed: number
  abandoned: number
  monthlyAdded: number
  monthlyCompleted: number
}

// ==================== 辅助类型 ====================

/** 书籍查询参数 */
export interface BookQueryParams {
  page?: number
  size?: number
  status?: ReadingStatus
  sort?: string
}

/** 搜索参数 */
export interface SearchParams {
  keyword: string
  page?: number
  size?: number
}
```

### 4.3 数据库表结构

#### users 表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 主键 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| email | VARCHAR(100) | NOT NULL, UNIQUE | 邮箱 |
| password | VARCHAR(255) | NOT NULL | 密码（BCrypt加密） |
| nickname | VARCHAR(50) | - | 昵称 |
| avatar_url | VARCHAR(500) | - | 头像URL |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

#### books 表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 主键 |
| user_id | BIGINT | NOT NULL, FK | 用户ID |
| title | VARCHAR(200) | NOT NULL | 书名 |
| author | VARCHAR(100) | - | 作者 |
| cover_url | VARCHAR(500) | - | 封面URL |
| isbn | VARCHAR(20) | - | ISBN |
| publisher | VARCHAR(100) | - | 出版社 |
| publish_date | DATE | - | 出版日期 |
| reading_status | VARCHAR(20) | NOT NULL | 阅读状态 |
| start_date | DATE | - | 开始阅读日期 |
| finish_date | DATE | - | 完成日期 |
| rating | DECIMAL(3,2) | CHECK(0-5) | 评分 |
| page_count | INTEGER | - | 总页数 |
| current_page | INTEGER | DEFAULT 0 | 当前页码 |
| description | TEXT | - | 简介 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

#### reading_notes 表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 主键 |
| book_id | BIGINT | NOT NULL, FK | 书籍ID |
| quote_id | BIGINT | FK | 关联金句ID |
| chapter | VARCHAR(100) | - | 章节 |
| page_number | INTEGER | - | 页码 |
| title | VARCHAR(200) | - | 标题 |
| content | TEXT | NOT NULL | 内容 |
| note_type | VARCHAR(20) | NOT NULL | 感悟类型 |
| tags | VARCHAR(500) | - | 标签 |
| is_private | BOOLEAN | NOT NULL | 是否私密 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

#### quotes 表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 主键 |
| book_id | BIGINT | NOT NULL, FK | 书籍ID |
| content | TEXT | NOT NULL | 金句内容 |
| chapter | VARCHAR(100) | - | 章节 |
| page_number | INTEGER | - | 页码 |
| note | TEXT | - | 备注 |
| color | VARCHAR(20) | - | 颜色标记 |
| tags | VARCHAR(500) | - | 标签 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

---

## 5. 核心组件关系

### 5.1 后端架构层次

```
┌─────────────────────────────────────────────────┐
│                   Controller                     │
│  (接收HTTP请求，调用Service，返回Response)        │
└──────────────────────┬──────────────────────────┘
                       │
                       ↓
┌─────────────────────────────────────────────────┐
│                    Service                       │
│  (业务逻辑，事务管理，数据校验，日志记录)          │
└──────────────────────┬──────────────────────────┘
                       │
                       ↓
┌─────────────────────────────────────────────────┐
│                  Repository                      │
│  (数据访问，JPA查询，继承JpaRepository)           │
└──────────────────────┬──────────────────────────┘
                       │
                       ↓
┌─────────────────────────────────────────────────┐
│                    Entity                        │
│  (数据库实体，JPA映射，业务数据模型)               │
└─────────────────────────────────────────────────┘
```

**数据流**：
1. Controller 接收 HTTP 请求，验证 DTO
2. Controller 调用 Service 处理业务逻辑
3. Service 调用 Repository 访问数据库
4. Repository 返回 Entity 对象
5. Service 将 Entity 转换为 Response DTO
6. Controller 封装统一响应格式返回前端

### 5.2 前端架构层次

```
┌─────────────────────────────────────────────────┐
│              frontend-design 插件                │
│  (AI驱动的页面生成，设计系统，组件库)              │
└──────────────────────┬──────────────────────────┘
                       │ 生成
                       ↓
┌─────────────────────────────────────────────────┐
│                    Views                         │
│  (生成的页面组件，用户交互，展示数据)              │
└──────────────────────┬──────────────────────────┘
                       │
         ┌─────────────┼─────────────┐
         │             │             │
         ↓             ↓             ↓
┌──────────────┐ ┌──────────┐ ┌──────────────┐
│   API模块    │ │  Pinia   │ │   Router     │
│  (Axios请求) │ │ (状态)   │ │  (路由守卫)  │
└──────────────┘ └──────────┘ └──────────────┘
                       │
                       ↓
              ┌──────────────┐
              │  LocalStorage │
              │  (Token存储)  │
              └──────────────┘
```

**前端开发流程**：
1. 使用 `frontend-design` 插件生成页面组件（基于需求描述）
2. 插件自动生成高质量、生产级别的 UI 代码
3. 开发者集成 API 调用逻辑（连接后端接口）
4. 测试和验证功能
5. 如需调整 UI，再次使用插件生成或修改

**数据流**：
1. 用户在 View 中触发操作（点击、提交等）
2. View 调用 API 模块发送 HTTP 请求
3. API 模块通过 Axios 拦截器自动添加 Token
4. 后端返回数据，Axios 拦截器统一处理错误
5. View 更新响应式数据，触发视图重新渲染
6. Pinia Store 管理全局状态（用户信息、Token等）

**核心原则**：
- ✅ 所有页面通过 frontend-design 插件生成
- ✅ 插件负责 UI 设计、布局、样式
- ✅ 开发者负责业务逻辑、API 集成
- ❌ 禁止手动编写页面组件代码
- ❌ 禁止硬编码 HTML/CSS 结构

### 5.3 认证流程

```
┌──────────┐         ┌──────────┐         ┌──────────┐
│  用户    │         │  前端    │         │  后端    │
└─────┬────┘         └─────┬────┘         └─────┬────┘
      │                    │                    │
      │  1. 输入账号密码    │                    │
      │ ─────────────────> │                    │
      │                    │                    │
      │                    │  2. POST /auth/login│
      │                    │ ─────────────────> │
      │                    │                    │
      │                    │                    │ 验证密码
      │                    │                    │ 生成JWT
      │                    │                    │
      │                    │  3. 返回JWT Token  │
      │                    │ <───────────────── │
      │                    │                    │
      │                    │ 4. 存储Token到     │
      │                    │    LocalStorage    │
      │                    │                    │
      │  5. 登录成功        │                    │
      │ <───────────────── │                    │
      │                    │                    │
      │  6. 访问资源        │                    │
      │ ─────────────────> │                    │
      │                    │                    │
      │                    │ 7. 请求携带Token   │
      │                    │    Authorization:  │
      │                    │    Bearer <token>  │
      │                    │ ─────────────────> │
      │                    │                    │
      │                    │                    │ 验证JWT
      │                    │                    │ 获取用户信息
      │                    │                    │ 返回数据
      │                    │                    │
      │                    │  8. 返回资源数据   │
      │                    │ <───────────────── │
      │                    │                    │
      │  9. 展示数据        │                    │
      │ <───────────────── │                    │
```

### 5.4 前端路由结构

```
/ (Layout - 需要认证)
├── / (Dashboard)           # 首页仪表板
├── /bookshelf              # 书架列表
├── /books/:id              # 书籍详情
├── /notes                  # 所有感悟
└── /quotes                 # 所有金句

/login (不需要认证)         # 登录页
/register (不需要认证)      # 注册页
```

**路由守卫**：
- 检查路由是否需要认证（meta.requiresAuth）
- 检查用户是否已登录（Pinia Store）
- 未登录重定向至登录页
- 已登录访问登录页重定向至首页

---

## 6. API 接口设计

### 6.1 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| GET | /api/auth/me | 获取当前用户信息 |
| POST | /api/auth/logout | 用户登出 |

### 6.2 书籍接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/books | 获取书籍列表（分页） |
| GET | /api/v1/books/{id} | 获取书籍详情 |
| POST | /api/v1/books | 创建书籍 |
| PUT | /api/v1/books/{id} | 更新书籍 |
| DELETE | /api/v1/books/{id} | 删除书籍 |
| PATCH | /api/v1/books/{id}/status | 更新阅读状态 |
| PATCH | /api/v1/books/{id}/progress | 更新阅读进度 |
| GET | /api/v1/books/search | 搜索书籍 |
| GET | /api/v1/books/statistics | 获取统计数据 |

### 6.3 读书感悟接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/books/{bookId}/notes | 获取书籍的感悟列表 |
| POST | /api/v1/books/{bookId}/notes | 为书籍创建感悟 |
| POST | /api/v1/quotes/{quoteId}/notes | 为金句创建感悟 |
| GET | /api/v1/notes/{id} | 获取感悟详情 |
| PUT | /api/v1/notes/{id} | 更新感悟 |
| DELETE | /api/v1/notes/{id} | 删除感悟 |
| GET | /api/v1/notes/search | 搜索感悟 |

### 6.4 金句接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/books/{bookId}/quotes | 获取书籍的金句列表 |
| POST | /api/v1/books/{bookId}/quotes | 创建金句 |
| GET | /api/v1/quotes/{id} | 获取金句详情 |
| PUT | /api/v1/quotes/{id} | 更新金句 |
| DELETE | /api/v1/quotes/{id} | 删除金句 |
| GET | /api/v1/quotes/random | 随机获取金句 |
| GET | /api/v1/quotes/search | 搜索金句 |

---

## 7. 安全机制

### 7.1 认证授权

- **JWT Token 认证**：无状态，适合前后端分离
- **Token 有效期**：可配置（默认24小时）
- **Token 存储**：前端 LocalStorage
- **Token 传输**：HTTP Header `Authorization: Bearer <token>`

### 7.2 数据隔离

- 所有数据查询关联 `user_id`
- 后端通过 `@AuthenticationPrincipal` 获取当前用户
- 禁止跨用户访问数据

### 7.3 密码安全

- **加密算法**：BCrypt
- **强度**：默认10轮
- **存储**：仅存储加密后的哈希值

### 7.4 CORS 配置

- 开发环境：允许所有来源
- 生产环境：仅允许指定域名

---

## 8. 部署架构

### 8.1 开发环境

```
┌──────────────┐         ┌──────────────┐
│   前端开发    │         │   后端开发    │
│  Vite Server │ ◄─────► │ Spring Boot  │
│  :5173       │  Proxy  │  :8080       │
└──────────────┘         └──────────────┘
                                │
                                ↓
                         ┌──────────────┐
                         │  H2 内存库    │
                         └──────────────┘
```

### 8.2 生产环境

```
┌──────────────┐         ┌──────────────┐
│   前端静态    │         │   后端服务    │
│  Nginx/CDN   │ ◄─────► │ Spring Boot  │
│  :80/443     │   API   │  :8080       │
└──────────────┘         └──────────────┘
                                │
                                ↓
                         ┌──────────────┐
                         │ PostgreSQL   │
                         └──────────────┘
```

---

## 9. 第三方服务集成（规划）

### 9.1 frontend-design 插件（已集成）

**用途**：前端 UI 生成和维护
**状态**：✅ 已安装并激活

**功能特性**：
- AI 驱动的页面组件生成
- 高质量、生产级别的代码输出
- 避免通用 AI 美学，生成独特的设计
- 支持响应式布局和现代设计模式
- 自动集成 Element Plus 组件库

**使用场景**：
- 创建新页面（如：书籍列表、详情页、登录页）
- 重构现有页面 UI
- 生成通用组件（如：卡片、表单、对话框）
- 调整页面设计风格

**开发流程**：
1. 描述页面需求给插件
2. 插件生成完整的 Vue 组件代码
3. 开发者集成 API 调用和业务逻辑
4. 测试和验证功能

### 9.2 图书信息API

用于书籍自动识别功能：
- **豆瓣API**：获取书籍信息（需要API Key）
- **Open Library API**：免费公开API
- **Google Books API**：免费，数据丰富

### 9.3 文件存储

用于书籍封面上传：
- **本地存储**：简单，适合小规模
- **阿里云OSS**：生产环境推荐
- **腾讯云COS**：替代方案

### 9.4 全文检索

用于高级搜索功能：
- **Elasticsearch**：专业全文检索引擎
- **PostgreSQL Full Text Search**：轻量级方案

---

## 10. 性能优化策略

### 10.1 后端优化

- JPA 懒加载（`FetchType.LAZY`）
- 数据库索引优化
- 分页查询
- 连接池配置（HikariCP）

### 10.2 前端优化

**frontend-design 插件优势**：
- 插件生成的代码已优化性能（避免不必要的重渲染、优化事件处理）
- 生成符合最佳实践的代码结构
- 自动处理响应式布局和性能优化

**开发者手动优化**：
- 路由懒加载（修改路由配置）
- 组件按需导入（调整导入语句）
- Vite 代码分割（配置 vite.config.js）
- 图片懒加载（在 API 调用逻辑中实现）
- Axios 请求缓存（在 API 模块中实现）

---

## 11. 前端开发流程（frontend-design）

### 11.1 核心原则

**frontend-design 插件是前端 UI 开发的唯一工具**：
- ✅ 所有页面组件必须通过 frontend-design 插件生成
- ✅ 禁止手动编写 Vue 组件的 `<template>` 和 `<style>` 部分
- ✅ 仅允许手动维护业务逻辑（API 调用、状态管理、路由配置）

### 11.2 开发流程

#### 步骤 1：需求描述
向 frontend-design 插件描述页面需求：
- 页面功能（如：书籍列表页、登录页、详情页）
- 交互需求（如：搜索、筛选、分页）
- 设计风格（如：简约、现代、书卷风格）

#### 步骤 2：生成页面
使用 frontend-design 插件生成页面代码：
```bash
# 示例命令（实际使用时参考插件文档）
/frontend-design 生成书籍列表页面，包含搜索、筛选和卡片布局
```

#### 步骤 3：集成 API
在生成的页面中集成后端 API：
- 导入 API 模块
- 在生命周期钩子中调用 API
- 处理响应数据
- 添加错误处理

#### 步骤 4：测试验证
- 启动前端开发服务器
- 测试页面功能
- 验证交互逻辑
- 检查响应式布局

#### 步骤 5：迭代优化
如需修改 UI：
- 重新使用 frontend-design 插件生成
- 保留业务逻辑代码
- 合并新生成的 UI 代码

### 11.3 代码职责划分

| 部分 | 责任方 | 维护方式 |
|------|--------|---------|
| UI 设计与布局 | frontend-design | 插件生成 |
| HTML/CSS 结构 | frontend-design | 插件生成 |
| 交互效果 | frontend-design | 插件生成 |
| API 调用逻辑 | 开发者 | 手动编写 |
| 业务逻辑 | 开发者 | 手动编写 |
| 状态管理 | 开发者 | 手动编写 |
| 路由配置 | 开发者 | 手动编写 |

### 11.4 页面生成示例

**示例：生成书籍详情页**

需求描述：
```
生成书籍详情页面，包含：
- 书籍封面、标题、作者信息
- 阅读状态和进度条
- 感悟和金句标签页
- 添加感悟/金句对话框
```

frontend-design 插件会生成：
- 完整的 Vue 组件结构（template + script + style）
- 响应式布局设计
- Element Plus 组件集成
- 美观的 UI 样式

开发者需要做：
- 在 `onMounted` 中调用 `bookApi.getBookById()`
- 在对话框提交事件中调用 `readingNoteApi.createNoteForBook()`
- 添加错误处理和 loading 状态

---

## 12. 扩展性设计

### 12.1 已规划的扩展点

1. **块级富文本编辑器**
   - 需要集成第三方编辑器（如 TipTap, Slate）
   - 后端需要调整 `content` 字段存储格式

2. **全文检索**
   - 集成 Elasticsearch
   - 需要新增 `SearchService`

3. **书籍自动识别**
   - 集成图书 API
   - 需要新增 `BookInfoService`

4. **数据导出**
   - Markdown/PDF 生成
   - 需要新增 `ExportService`

5. **分享功能**
   - 生成分享链接
   - 需要新增 `ShareToken` 实体

### 12.2 预留的扩展接口

- 所有 Controller 返回统一的 `ApiResponse<T>` 格式
- Service 层使用 DTO 隔离实体和接口
- Repository 层支持自定义查询方法
- 前端 API 模块统一管理接口调用
- **前端页面通过 frontend-design 插件生成，易于重构和迭代**

### 12.3 前端扩展性

使用 frontend-design 插件的优势：
- **快速迭代**：修改 UI 只需重新生成，无需手动重构
- **设计一致性**：插件保证所有页面遵循统一的设计系统
- **高质量代码**：生成的代码符合最佳实践
- **降低维护成本**：减少手动编写 UI 的工作量
- **易于定制**：可通过调整生成参数定制页面风格

---

## 13. 开发规范

详见 `.spec/constitution.md`

---

*文档版本：2.0*
*最后更新：2026-03-12*
*重大更新：引入 frontend-design 插件作为前端核心依赖*