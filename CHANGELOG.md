# Changelog

All notable changes to the Book Record project will be documented in this file.

## [1.1.0] - 2026-03-11

### Fixed
- 修复无法添加读书感悟的问题 - 现在可以正常添加感悟
- 修复无法添加金句的问题 - 现在可以正常添加金句

### Added
- 添加读书感悟对话框
  - 感悟类型选择（感悟/总结/疑问/洞察）
  - 标题、内容输入
  - 章节、页码记录
  - 标签功能
  - 私密设置

- 添加金句对话框
  - 金句内容输入
  - 章节、页码记录
  - 备注功能
  - 颜色标记选择
  - 标签功能

- 添加编辑书籍对话框
  - 编辑书名、作者
  - 更新阅读状态
  - 修改页数、评分

- 添加更新阅读进度对话框
  - 更新当前页码
  - 自动计算进度百分比

### Changed
- `frontend/src/views/BookDetail.vue` - 重构，添加完整的CRUD功能
  - 引入 `reactive` 用于表单数据管理
  - 添加表单验证规则
  - 实现提交loading状态
  - 优化错误处理和成功提示

### Documentation
- 新增 `FUNCTIONALITY_CHECK.md` - 功能完整性检查报告
- 更新 `memory/MEMORY.md` - 添加详细更新日志
- 新增 `memory/CHANGELOG.md` - 项目更新历史

## [1.0.0] - 2026-03-11

### Added
- 初始项目结构
- 后端API完整实现
  - 用户认证API（4个）
  - 书籍管理API（10个）
  - 读书感悟API（8个）
  - 金句收藏API（8个）

- 前端基础框架
  - Vue 3 + Vite 项目搭建
  - Element Plus UI框架集成
  - Vue Router 路由配置
  - Pinia 状态管理
  - Axios API封装

- 用户功能
  - 用户注册、登录
  - JWT Token认证
  - 用户信息管理

- 书籍管理
  - 书籍CRUD操作
  - 阅读状态管理
  - 阅读进度跟踪
  - 书籍搜索和统计

- 读书感悟
  - 感悟列表展示
  - 按书籍/金句查看感悟

- 金句收藏
  - 金句列表展示
  - 随机金句展示
  - 金句搜索

- 数据库
  - H2内存数据库（开发环境）
  - PostgreSQL支持（生产环境）
  - Flyway数据库迁移

- 其他
  - Swagger API文档
  - CORS跨域配置
  - 统一异常处理
  - 响应式布局

---

## 版本说明

- **[1.1.0]** - 前端功能完善版本，修复核心功能问题
- **[1.0.0]** - 初始发布版本，完成所有后端API和前端基础框架

## 下一版本计划

### [1.2.0] - 计划中
- 感悟的编辑和删除功能
- 金句的编辑和删除功能
- 书籍封面上传功能

### [1.3.0] - 计划中
- 阅读统计图表
- 数据导入导出
- 书籍标签系统

---

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/),
并且本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。