# Book Record 项目宪法

> 本文档定义了读书笔记管理系统的代码风格规范、架构约定和开发准则。所有开发人员必须遵守这些规范以确保代码质量和一致性。

---

## 1. 项目概述

- **项目名称**: Book Record - 读书笔记管理系统
- **架构模式**: 前后端分离
- **后端技术**: Spring Boot 3.2.5 + Spring Data JPA + Spring Security + JWT
- **前端技术**: Vue 3 (Composition API) + Element Plus + Pinia + Axios

---

## 2. 后端代码规范

### 2.1 包结构

```
com.bookrecord
├── config/          # 配置类 (Security, Swagger, CORS等)
├── controller/      # REST控制器
├── service/         # 业务逻辑层
├── repository/      # 数据访问层
├── entity/          # JPA实体类
├── dto/             # 数据传输对象
├── exception/       # 自定义异常
└── security/        # 安全相关类
```

### 2.2 实体类规范 (Entity)

```java
// 必须使用的 Lombok 注解
@Entity
@Table(name = "table_name")
@Data                    // Getter/Setter/Equals/HashCode/ToString
@Builder                 // 构建者模式
@NoArgsConstructor       // 无参构造函数（JPA要求）
@AllArgsConstructor      // 全参构造函数
public class EntityName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 数据库列名使用下划线命名
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 枚举必须使用 STRING 类型持久化
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private Status status = Status.DEFAULT;

    // 一对多关系默认值
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Child> children = new ArrayList<>();
}
```

**命名约定**:
- 类名: 大驼峰 (PascalCase)
- 表名: 小写下划线 (snake_case)
- 字段名: 小驼峰 (camelCase)
- 数据库列名: 小写下划线 (snake_case)

### 2.3 服务层规范 (Service)

```java
@Service
@RequiredArgsConstructor    // 构造函数注入
@Slf4j                     // 日志记录
public class EntityService {

    private final EntityRepository repository;

    // 写操作必须添加 @Transactional
    @Transactional
    public EntityResponse create(EntityRequest request, String username) {
        log.info("Creating entity: {} for user: {}", request.getName(), username);

        // 业务逻辑...

        log.info("Entity created successfully with id: {}", entity.getId());
        return EntityResponse.fromEntity(entity);
    }

    // 只读操作使用 @Transactional(readOnly = true)
    @Transactional(readOnly = true)
    public EntityResponse getById(Long id, String username) {
        return repository.findById(id)
            .map(EntityResponse::fromEntity)
            .orElseThrow(() -> new ResourceNotFoundException("Entity", "id", id));
    }
}
```

**服务层约定**:
- 使用 `@RequiredArgsConstructor` 进行构造函数注入
- 所有写操作必须添加 `@Transactional`
- 只读操作添加 `@Transactional(readOnly = true)`
- 关键操作必须记录日志 (创建、更新、删除)
- 返回 DTO 而非实体对象

### 2.4 控制器规范 (Controller)

```java
@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
@Tag(name = "资源管理", description = "资源CRUD接口")
public class ResourceController {

    private final ResourceService service;

    @PostMapping
    @Operation(summary = "创建资源", description = "创建新的资源")
    public ResponseEntity<ApiResponse<ResourceResponse>> create(
            @Valid @RequestBody ResourceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResourceResponse response = service.create(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Resource created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取资源", description = "根据ID获取资源详情")
    public ResponseEntity<ApiResponse<ResourceResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResourceResponse response = service.getById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

**控制器约定**:
- API 版本前缀: `/api/v1/`
- 使用 `@Tag` 和 `@Operation` 注解生成 Swagger 文档
- 统一返回 `ResponseEntity<ApiResponse<T>>`
- 使用 `@Valid` 进行请求参数验证
- 通过 `@AuthenticationPrincipal` 获取当前用户

### 2.5 DTO 规范

```java
// 请求 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRequest {

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过100")
    private String name;

    @Size(max = 500, message = "描述长度不能超过500")
    private String description;
}

// 响应 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceResponse {

    private Long id;
    private String name;
    private LocalDateTime createdAt;

    // 静态工厂方法
    public static ResourceResponse fromEntity(Resource entity) {
        return ResourceResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
```

**DTO 约定**:
- 请求 DTO 以 `Request` 结尾
- 响应 DTO 以 `Response` 结尾
- 响应 DTO 使用 `@JsonInclude(JsonInclude.Include.NON_NULL)` 忽略 null 字段
- 提供 `fromEntity()` 静态方法转换实体

### 2.6 统一响应格式

```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}

// 成功响应
ApiResponse.success(data)
ApiResponse.success("操作成功", data)

// 错误响应
ApiResponse.error("错误信息")
ApiResponse.error("错误信息", errorData)
```

---

## 3. 前端代码规范

### 3.1 组件结构

```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup>
// 1. 导入
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { apiModule } from '@/api/modules'
import { ElMessage } from 'element-plus'

// 2. 组合式 API 调用
const route = useRoute()

// 3. 响应式状态
const loading = ref(false)
const data = ref({})
const form = reactive({
  field1: '',
  field2: null
})

// 4. 表单验证规则
const rules = {
  field1: [{ required: true, message: '请输入字段', trigger: 'blur' }]
}

// 5. 生命周期钩子
onMounted(() => {
  loadData()
})

// 6. 方法
async function loadData() {
  loading.value = true
  try {
    const response = await apiModule.getData()
    data.value = response.data
  } catch (error) {
    console.error('Failed to load data:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 样式内容 */
</style>
```

### 3.2 响应式数据约定

```javascript
// 简单值使用 ref
const loading = ref(false)
const dialogVisible = ref(false)
const data = ref({})

// 表单对象使用 reactive
const form = reactive({
  name: '',
  description: ''
})

// 列表数据使用 ref
const items = ref([])
```

### 3.3 异步处理规范

```javascript
// 标准异步处理模式
async function handleSubmit() {
  // 1. 表单验证
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 2. API 调用
        await apiModule.create(form)

        // 3. 成功提示
        ElMessage.success('操作成功')

        // 4. 关闭对话框
        dialogVisible.value = false

        // 5. 刷新数据
        loadData()
      } catch (error) {
        // 错误已在拦截器中处理
        console.error('Operation failed:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
```

### 3.4 API 模块化

```javascript
// @/api/modules.js
export const resourceApi = {
  getList: (params) => api.get('/resources', { params }),
  getById: (id) => api.get(`/resources/${id}`),
  create: (data) => api.post('/resources', data),
  update: (id, data) => api.put(`/resources/${id}`, data),
  delete: (id) => api.delete(`/resources/${id}`)
}
```

### 3.5 状态管理 (Pinia)

```javascript
// stores/user.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref(null)
  const token = ref('')

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)

  // 方法
  function login(credentials) { /* ... */ }
  function logout() { /* ... */ }

  return {
    user,
    token,
    isAuthenticated,
    login,
    logout
  }
})
```

---

## 4. 错误处理标准

### 4.1 后端异常体系

```
RuntimeException
├── ResourceNotFoundException    (404) - 资源不存在
├── BadRequestException         (400) - 请求参数错误
├── AccessDeniedException       (403) - 权限不足
└── AuthenticationException     (401) - 认证失败
```

### 4.2 自定义异常

```java
public class ResourceNotFoundException extends RuntimeException {
    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
```

### 4.3 全局异常处理

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - 资源不存在
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // 400 - 请求参数错误
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // 400 - 验证失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            errors.put(fieldName, error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed", errors));
    }

    // 401 - 认证失败
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(Exception ex) {
        log.error("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("认证失败"));
    }

    // 403 - 权限不足
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("权限不足"));
    }

    // 500 - 服务器错误
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("服务器内部错误"));
    }
}
```

### 4.4 前端错误处理

```javascript
// Axios 拦截器
api.interceptors.response.use(
  response => {
    const res = response.data
    if (!res.success) {
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查您的连接')
    }
    return Promise.reject(error)
  }
)
```

### 4.5 错误处理最佳实践

1. **后端**:
   - 所有异常必须记录日志
   - 不要暴露敏感错误信息给前端
   - 使用自定义异常而非通用异常
   - 验证失败返回字段级错误信息

2. **前端**:
   - 统一使用 `try-catch-finally` 模式
   - 错误已在拦截器处理，组件内只需 `console.error`
   - 始终在 `finally` 中重置 loading 状态
   - 使用 `ElMessage` 显示用户友好的提示

---

## 5. 安全规范

### 5.1 认证与授权

- 所有 API（除 `/api/auth/**`）都需要 JWT 认证
- Token 存储在 localStorage，请求时通过 `Authorization: Bearer <token>` 发送
- Token 过期后前端自动跳转登录页

### 5.2 数据隔离

- 所有数据查询必须关联当前用户
- 禁止查询其他用户的数据
- 使用 `@AuthenticationPrincipal` 获取当前用户

### 5.3 敏感配置

- JWT 密钥通过环境变量 `JWT_SECRET` 配置
- 数据库密码通过环境变量 `DB_USERNAME`, `DB_PASSWORD` 配置
- 禁止在代码中硬编码敏感信息

---

## 6. 数据库规范

### 6.1 迁移管理

- 使用 Flyway 管理数据库迁移
- 迁移脚本位于 `backend/src/main/resources/db/migration/`
- 命名格式: `V{version}__{description}.sql`

### 6.2 环境配置

| 环境 | Profile | 数据库 | 配置方式 |
|------|---------|--------|----------|
| 开发 | dev | H2 内存数据库 | application-dev.yml |
| 生产 | prod | PostgreSQL | 环境变量 |

---

## 7. 日志规范

### 7.1 后端日志

```java
// 使用 @Slf4j 注解
@Slf4j
@Service
public class BookService {

    public BookResponse createBook(BookRequest request, String username) {
        // 信息日志: 记录关键操作
        log.info("Creating book: {} for user: {}", request.getTitle(), username);

        // 调试日志: 记录详细过程
        log.debug("Book details: {}", request);

        // 错误日志: 记录异常
        log.error("Failed to create book", exception);

        // 成功日志
        log.info("Book created successfully with id: {}", book.getId());
    }
}
```

### 7.2 日志级别使用

| 级别 | 使用场景 |
|------|----------|
| ERROR | 异常、错误、失败操作 |
| WARN | 潜在问题、不推荐的操作 |
| INFO | 关键业务操作（创建、更新、删除） |
| DEBUG | 调试信息、详细流程 |

### 7.3 前端日志

```javascript
// 使用 console 方法
console.error('Failed to load data:', error)  // 错误
console.warn('Deprecated feature used')       // 警告
console.log('Debug info:', data)              // 调试
```

---

## 8. Git 提交规范

### 8.1 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 8.2 类型 (type)

| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | Bug 修复 |
| docs | 文档更新 |
| style | 代码格式（不影响逻辑） |
| refactor | 重构（不新增功能或修复 Bug） |
| test | 测试相关 |
| chore | 构建/工具相关 |

### 8.3 示例

```
feat(book): 添加书籍标签功能

- 支持添加多个标签
- 支持按标签筛选书籍
- 标签颜色自定义

Closes #123
```

---

## 9. 代码审查清单

### 9.1 后端审查

- [ ] 使用了正确的 Lombok 注解
- [ ] Service 层方法添加了 `@Transactional`
- [ ] 控制器添加了 Swagger 文档注解
- [ ] 返回统一的 `ApiResponse` 格式
- [ ] 敏感操作记录了日志
- [ ] 异常处理完善
- [ ] 数据查询关联了当前用户

### 9.2 前端审查

- [ ] 使用了 Composition API (`<script setup>`)
- [ ] 响应式数据使用了正确的类型 (`ref`/`reactive`)
- [ ] 异步操作使用了 `try-catch-finally`
- [ ] 表单验证规则完整
- [ ] 用户操作有反馈（loading、message）
- [ ] 组件样式使用 `scoped`

---

## 10. 版本信息

| 项目 | 版本 |
|------|------|
| Spring Boot | 3.2.5 |
| Vue | 3.x |
| Element Plus | latest |
| JDK | 17+ |
| Node.js | 18+ |

---

*最后更新: 2026-03-12*