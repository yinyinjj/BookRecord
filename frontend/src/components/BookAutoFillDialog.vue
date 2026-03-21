<template>
  <!-- 书籍自动识别对话框
       提供ISBN识别和书名搜索功能，自动填充书籍信息表单 -->
  <el-dialog
    :model-value="visible"
    :title="editMode ? '编辑书籍' : '添加新书'"
    width="720px"
    custom-class="book-auto-fill-dialog"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="dialog-content">
      <!-- ==================== 自动识别区域 ==================== -->
      <div v-if="!editMode" class="auto-recognize-section">
        <div class="section-header">
          <div class="section-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.35-4.35"/>
            </svg>
          </div>
          <span class="section-title">智能识别</span>
          <span class="section-hint">输入ISBN或书名，自动获取书籍信息</span>
        </div>

        <div class="recognize-tabs">
          <!-- ISBN识别标签页 -->
          <button
            :class="['tab-btn', { active: activeTab === 'isbn' }]"
            @click="activeTab = 'isbn'"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <path d="M7 7v10"/>
              <path d="M11 7v10"/>
              <path d="M15 7v10"/>
            </svg>
            <span>ISBN识别</span>
          </button>

          <!-- 书名搜索标签页 -->
          <button
            :class="['tab-btn', { active: activeTab === 'title' }]"
            @click="activeTab = 'title'"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
            </svg>
            <span>书名搜索</span>
          </button>
        </div>

        <div class="recognize-content">
          <!-- ISBN输入区 -->
          <div v-show="activeTab === 'isbn'" class="input-group">
            <div class="input-wrapper">
              <input
                v-model="isbnInput"
                type="text"
                placeholder="请输入ISBN编号（10位或13位）"
                class="recognize-input"
                @keyup.enter="handleIsbnSearch"
              />
              <button
                class="recognize-btn"
                :disabled="!isbnInput.trim() || isbnLoading"
                @click="handleIsbnSearch"
              >
                <span v-if="isbnLoading" class="btn-loading"></span>
                <span v-else>识别</span>
              </button>
            </div>
            <transition name="fade">
              <p v-if="isbnError" class="error-hint">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <path d="M15 9l-6 6"/>
                  <path d="M9 9l6 6"/>
                </svg>
                {{ isbnError }}
              </p>
            </transition>
          </div>

          <!-- 书名搜索区 -->
          <div v-show="activeTab === 'title'" class="input-group">
            <div class="input-wrapper">
              <input
                v-model="titleInput"
                type="text"
                placeholder="请输入书名关键词"
                class="recognize-input"
                @keyup.enter="handleTitleSearch"
              />
              <button
                class="recognize-btn"
                :disabled="!titleInput.trim() || titleLoading"
                @click="handleTitleSearch"
              >
                <span v-if="titleLoading" class="btn-loading"></span>
                <span v-else>搜索</span>
              </button>
            </div>

            <!-- 搜索结果列表 -->
            <transition name="slide">
              <div v-if="searchResults.length > 0" class="search-results">
                <div class="results-header">
                  <span>找到 {{ searchResults.length }} 本相关书籍</span>
                  <button class="clear-btn" @click="clearSearchResults">清除</button>
                </div>
                <div class="results-list">
                  <div
                    v-for="(book, index) in searchResults"
                    :key="index"
                    class="result-item"
                    @click="selectSearchResult(book)"
                  >
                    <div class="result-cover">
                      <img v-if="book.coverUrl" :src="getProxyImageUrl(book.coverUrl)" :alt="book.title" referrerpolicy="no-referrer" />
                      <div v-else class="cover-fallback">
                        <span>{{ book.title?.charAt(0) || '?' }}</span>
                      </div>
                    </div>
                    <div class="result-info">
                      <h4 class="result-title">{{ book.title }}</h4>
                      <p class="result-author">{{ book.author || '未知作者' }}</p>
                      <p class="result-publisher">{{ book.publisher }} {{ book.publishDate ? `· ${book.publishDate}` : '' }}</p>
                    </div>
                    <div class="result-action">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="9 18 15 12 9 6"/>
                      </svg>
                    </div>
                  </div>
                </div>
              </div>
            </transition>

            <transition name="fade">
              <p v-if="titleError" class="error-hint">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <path d="M15 9l-6 6"/>
                  <path d="M9 9l6 6"/>
                </svg>
                {{ titleError }}
              </p>
            </transition>
          </div>
        </div>

        <!-- 分割线 -->
        <div class="divider">
          <span class="divider-text">或手动填写以下信息</span>
        </div>
      </div>

      <!-- ==================== 表单区域 ==================== -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
        class="book-form"
      >
        <div class="form-grid">
          <!-- 左列：基本信息 -->
          <div class="form-column">
            <el-form-item label="书名" prop="title">
              <el-input
                v-model="formData.title"
                placeholder="请输入书名"
                class="form-input"
              />
            </el-form-item>

            <el-form-item label="作者" prop="author">
              <el-input
                v-model="formData.author"
                placeholder="请输入作者"
                class="form-input"
              />
            </el-form-item>

            <el-form-item label="ISBN" prop="isbn">
              <el-input
                v-model="formData.isbn"
                placeholder="请输入ISBN"
                class="form-input"
              />
            </el-form-item>

            <el-form-item label="出版社" prop="publisher">
              <el-input
                v-model="formData.publisher"
                placeholder="请输入出版社"
                class="form-input"
              />
            </el-form-item>

            <el-form-item label="出版日期" prop="publishDate">
              <el-input
                v-model="formData.publishDate"
                placeholder="如：2024-01"
                class="form-input"
              />
            </el-form-item>

            <el-form-item label="总页数" prop="pageCount">
              <el-input-number
                v-model="formData.pageCount"
                :min="0"
                :max="99999"
                controls-position="right"
                class="form-number"
              />
            </el-form-item>
          </div>

          <!-- 右列：封面和状态 -->
          <div class="form-column form-column-right">
            <!-- 封面预览 -->
            <div class="cover-section">
              <label class="cover-label">封面</label>
              <div class="cover-preview">
                <div v-if="formData.coverUrl" class="cover-image-wrapper">
                  <img :src="getProxyImageUrl(formData.coverUrl)" alt="封面预览" class="cover-image" referrerpolicy="no-referrer" />
                  <button class="remove-cover-btn" @click="formData.coverUrl = ''">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M18 6L6 18M6 6l12 12"/>
                    </svg>
                  </button>
                </div>
                <div v-else class="cover-placeholder">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <rect x="3" y="3" width="18" height="18" rx="2"/>
                    <circle cx="8.5" cy="8.5" r="1.5"/>
                    <path d="M21 15l-5-5L5 21"/>
                  </svg>
                  <span>暂无封面</span>
                </div>
              </div>
              <el-input
                v-model="formData.coverUrl"
                placeholder="输入封面图片URL"
                class="cover-url-input"
              />
            </div>

            <!-- 阅读状态 -->
            <el-form-item label="状态" prop="readingStatus">
              <el-select v-model="formData.readingStatus" class="form-select">
                <el-option label="想读" value="WANT_TO_READ" />
                <el-option label="在读" value="READING" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已放弃" value="ABANDONED" />
              </el-select>
            </el-form-item>

            <!-- 评分 -->
            <el-form-item label="评分" prop="rating">
              <div class="rating-wrapper">
                <el-rate
                  v-model="formData.rating"
                  :max="5"
                  :allow-half="false"
                  show-score
                  :score-template="formData.rating ? `${formData.rating} 分` : '未评分'"
                />
              </div>
            </el-form-item>
          </div>
        </div>

        <!-- 简介（全宽） -->
        <el-form-item label="简介" prop="description" class="full-width">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入书籍简介"
            class="form-textarea"
          />
        </el-form-item>
      </el-form>

      <!-- 数据来源标识 -->
      <transition name="fade">
        <div v-if="dataSource" class="data-source">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <path d="M12 16v-4"/>
            <path d="M12 8h.01"/>
          </svg>
          <span>数据来源：{{ dataSource }}</span>
        </div>
      </transition>
    </div>

    <!-- 对话框底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <button class="btn-cancel" @click="handleCancel">取消</button>
        <button
          class="btn-submit"
          :disabled="submitLoading"
          @click="handleSubmit"
        >
          <span v-if="submitLoading" class="btn-loading"></span>
          <span v-else>{{ editMode ? '保存修改' : '添加书籍' }}</span>
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { bookApi } from '@/api/modules'
import { ElMessage } from 'element-plus'
// 导入图片代理工具
import { getProxyImageUrl } from '@/utils/image'

// ==================== Props 定义 ====================

const props = defineProps({
  // 对话框显示状态
  visible: {
    type: Boolean,
    default: false
  },
  // 是否为编辑模式
  editMode: {
    type: Boolean,
    default: false
  },
  // 编辑模式下的书籍数据
  bookData: {
    type: Object,
    default: () => ({})
  }
})

// ==================== Emits 定义 ====================

const emit = defineEmits(['update:visible', 'submit'])

// ==================== 响应式状态 ====================

// 表单引用
const formRef = ref(null)

// 当前激活的标签页
const activeTab = ref('isbn')

// ISBN输入和加载状态
const isbnInput = ref('')
const isbnLoading = ref(false)
const isbnError = ref('')

// 书名搜索输入和加载状态
const titleInput = ref('')
const titleLoading = ref(false)
const titleError = ref('')
const searchResults = ref([])

// 提交加载状态
const submitLoading = ref(false)

// 数据来源标识
const dataSource = ref('')

// 表单数据
const formData = reactive({
  title: '',           // 书名
  author: '',          // 作者
  isbn: '',            // ISBN
  publisher: '',       // 出版社
  publishDate: '',     // 出版日期
  coverUrl: '',        // 封面URL
  description: '',     // 简介
  pageCount: null,     // 总页数
  readingStatus: 'WANT_TO_READ',  // 阅读状态，默认想读
  rating: null         // 评分
})

// ==================== 表单验证规则 ====================

const formRules = {
  title: [
    { required: true, message: '请输入书名', trigger: 'blur' },
    { max: 200, message: '书名长度不能超过200个字符', trigger: 'blur' }
  ]
}

// ==================== 监听器 ====================

// 监听对话框显示状态，初始化数据
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 对话框打开时初始化
    resetForm()
    clearSearchResults()
    isbnError.value = ''
    titleError.value = ''
    dataSource.value = ''

    // 编辑模式下填充数据
    if (props.editMode && props.bookData) {
      Object.keys(formData).forEach(key => {
        if (props.bookData[key] !== undefined) {
          formData[key] = props.bookData[key]
        }
      })
    }
  }
})

// ==================== 方法定义 ====================

/**
 * 处理ISBN识别
 * 调用API获取书籍信息并自动填充表单
 */
async function handleIsbnSearch() {
  if (!isbnInput.value.trim()) return

  isbnLoading.value = true
  isbnError.value = ''

  try {
    const response = await bookApi.getBookByIsbn(isbnInput.value.trim())

    if (response.success && response.data) {
      fillFormWithBookInfo(response.data)
      dataSource.value = response.data.source || 'Google Books'
      ElMessage.success('识别成功，已自动填充书籍信息')
    } else {
      isbnError.value = response.message || '未找到该ISBN对应的图书信息'
    }
  } catch (error) {
    console.error('ISBN识别失败:', error)
    isbnError.value = '识别失败，请稍后重试'
  } finally {
    isbnLoading.value = false
  }
}

/**
 * 处理书名搜索
 * 调用API搜索匹配的图书列表
 */
async function handleTitleSearch() {
  if (!titleInput.value.trim()) return

  titleLoading.value = true
  titleError.value = ''
  searchResults.value = []

  try {
    const response = await bookApi.searchBooksByTitle(titleInput.value.trim())

    if (response.success && response.data && response.data.length > 0) {
      searchResults.value = response.data
    } else {
      titleError.value = response.message || '未找到匹配的图书'
    }
  } catch (error) {
    console.error('书名搜索失败:', error)
    titleError.value = '搜索失败，请稍后重试'
  } finally {
    titleLoading.value = false
  }
}

/**
 * 选择搜索结果
 * 用选中的图书信息填充表单
 *
 * @param {Object} book - 选中的图书信息
 */
function selectSearchResult(book) {
  fillFormWithBookInfo(book)
  dataSource.value = book.source || 'Google Books'
  clearSearchResults()
  ElMessage.success('已选择《' + book.title + '》')
}

/**
 * 用图书信息填充表单
 *
 * @param {Object} bookInfo - 图书信息对象
 */
function fillFormWithBookInfo(bookInfo) {
  if (bookInfo.title) formData.title = bookInfo.title
  if (bookInfo.author) formData.author = bookInfo.author
  if (bookInfo.isbn) formData.isbn = bookInfo.isbn
  if (bookInfo.publisher) formData.publisher = bookInfo.publisher
  if (bookInfo.publishDate) formData.publishDate = bookInfo.publishDate
  if (bookInfo.coverUrl) formData.coverUrl = bookInfo.coverUrl
  if (bookInfo.description) formData.description = bookInfo.description
  if (bookInfo.pageCount) formData.pageCount = bookInfo.pageCount
}

/**
 * 清除搜索结果
 */
function clearSearchResults() {
  searchResults.value = []
  titleInput.value = ''
}

/**
 * 重置表单数据
 */
function resetForm() {
  Object.assign(formData, {
    title: '',
    author: '',
    isbn: '',
    publisher: '',
    publishDate: '',
    coverUrl: '',
    description: '',
    pageCount: null,
    readingStatus: 'WANT_TO_READ',
    rating: null
  })
  dataSource.value = ''
}

/**
 * 处理取消操作
 */
function handleCancel() {
  emit('update:visible', false)
}

/**
 * 处理表单提交
 */
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 触发提交事件，传递表单数据
        emit('submit', { ...formData })
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error('提交失败，请稍后重试')
      } finally {
        submitLoading.value = false
      }
    }
  })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Crimson+Pro:wght@400;600;700&family=Nunito:wght@400;500;600;700&display=swap');

/* ==================== 对话框样式 ==================== */

:deep(.book-auto-fill-dialog) {
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(180deg, #fdfcfa 0%, #f8f5f0 100%);
  box-shadow: 0 25px 50px -12px rgba(44, 24, 16, 0.25);
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #3d2317 0%, #2c1810 100%);
  padding: 1.5rem 2rem;
  margin: 0;
  border-bottom: none;
}

:deep(.el-dialog__title) {
  font-family: 'Crimson Pro', serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #f5f1eb;
  letter-spacing: 0.02em;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(245, 241, 235, 0.7);
  font-size: 20px;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #f5f1eb;
}

:deep(.el-dialog__body) {
  padding: 0;
}

:deep(.el-dialog__footer) {
  padding: 0;
  border-top: 1px solid #e8ddd0;
  background: #fdfcfa;
}

.dialog-content {
  padding: 1.75rem 2rem;
  max-height: 65vh;
  overflow-y: auto;
}

/* ==================== 自动识别区域 ==================== */

.auto-recognize-section {
  margin-bottom: 1.5rem;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.section-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.section-title {
  font-family: 'Crimson Pro', serif;
  font-size: 1.125rem;
  font-weight: 600;
  color: #2c1810;
}

.section-hint {
  font-size: 0.8125rem;
  color: #8b7355;
  margin-left: auto;
}

/* 标签页切换 */
.recognize-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tab-btn {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 2px solid #e8ddd0;
  border-radius: 10px;
  background: white;
  color: #6b5344;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.tab-btn:hover {
  border-color: #c4b5a0;
  background: #faf8f5;
}

.tab-btn.active {
  border-color: #6b5344;
  background: linear-gradient(135deg, #faf8f5 0%, #f5f1eb 100%);
  color: #2c1810;
  box-shadow: 0 2px 8px rgba(107, 83, 68, 0.15);
}

/* 输入组 */
.input-group {
  margin-bottom: 0.5rem;
}

.input-wrapper {
  display: flex;
  gap: 0.75rem;
}

.recognize-input {
  flex: 1;
  padding: 0.875rem 1rem;
  border: 2px solid #e8ddd0;
  border-radius: 10px;
  font-family: 'Nunito', sans-serif;
  font-size: 0.9375rem;
  color: #2c1810;
  background: white;
  transition: all 0.2s ease;
}

.recognize-input:focus {
  outline: none;
  border-color: #8b6f47;
  box-shadow: 0 0 0 4px rgba(139, 111, 71, 0.1);
}

.recognize-input::placeholder {
  color: #b8a898;
}

.recognize-btn {
  padding: 0 1.5rem;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  color: white;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 0.9375rem;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  min-width: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recognize-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(107, 83, 68, 0.3);
}

.recognize-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 加载动画 */
.btn-loading {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 错误提示 */
.error-hint {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
  padding: 0.625rem 0.875rem;
  background: #fef2f2;
  border-radius: 8px;
  color: #991b1b;
  font-size: 0.8125rem;
}

/* 搜索结果 */
.search-results {
  margin-top: 1rem;
  background: white;
  border: 2px solid #e8ddd0;
  border-radius: 12px;
  overflow: hidden;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: #faf8f5;
  border-bottom: 1px solid #e8ddd0;
  font-size: 0.8125rem;
  color: #6b5344;
}

.clear-btn {
  background: none;
  border: none;
  color: #8b6f47;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.8125rem;
}

.clear-btn:hover {
  color: #6b5344;
  text-decoration: underline;
}

.results-list {
  max-height: 200px;
  overflow-y: auto;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.875rem 1rem;
  border-bottom: 1px solid #f5f1eb;
  cursor: pointer;
  transition: background 0.15s ease;
}

.result-item:last-child {
  border-bottom: none;
}

.result-item:hover {
  background: #faf8f5;
}

.result-cover {
  width: 45px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
  background: #e8ddd0;
}

.result-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  color: white;
  font-family: 'Crimson Pro', serif;
  font-size: 1.25rem;
  font-weight: 700;
}

.result-info {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-family: 'Crimson Pro', serif;
  font-size: 0.9375rem;
  font-weight: 600;
  color: #2c1810;
  margin: 0 0 0.25rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-author {
  font-size: 0.8125rem;
  color: #6b5344;
  margin: 0 0 0.125rem;
}

.result-publisher {
  font-size: 0.75rem;
  color: #8b7355;
  margin: 0;
}

.result-action {
  color: #b8a898;
  transition: color 0.2s ease;
}

.result-item:hover .result-action {
  color: #6b5344;
}

/* 分割线 */
.divider {
  display: flex;
  align-items: center;
  margin: 1.5rem 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, #e8ddd0, transparent);
}

.divider-text {
  padding: 0 1rem;
  font-size: 0.8125rem;
  color: #8b7355;
}

/* ==================== 表单样式 ==================== */

.book-form {
  margin-top: 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem 1.5rem;
}

.form-column {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-column-right {
  padding-left: 1rem;
  border-left: 1px solid #f0e8dc;
}

/* Element Plus 表单样式覆盖 */
:deep(.el-form-item) {
  margin-bottom: 0.875rem;
}

:deep(.el-form-item__label) {
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  color: #4a3728;
  padding-right: 0.75rem;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: none;
  border: 2px solid #e8ddd0;
  background: white;
  transition: all 0.2s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #c4b5a0;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #8b6f47;
  box-shadow: 0 0 0 4px rgba(139, 111, 71, 0.1);
}

:deep(.el-input__inner) {
  font-family: 'Nunito', sans-serif;
  color: #2c1810;
}

:deep(.el-input__inner::placeholder) {
  color: #b8a898;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 2px solid #e8ddd0;
  font-family: 'Nunito', sans-serif;
  color: #2c1810;
  transition: all 0.2s ease;
}

:deep(.el-textarea__inner:hover) {
  border-color: #c4b5a0;
}

:deep(.el-textarea__inner:focus) {
  border-color: #8b6f47;
  box-shadow: 0 0 0 4px rgba(139, 111, 71, 0.1);
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  border-radius: 8px;
}

/* 封面预览 */
.cover-section {
  margin-bottom: 1rem;
}

.cover-label {
  display: block;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 0.875rem;
  color: #4a3728;
  margin-bottom: 0.5rem;
}

.cover-preview {
  width: 100%;
  height: 140px;
  border: 2px dashed #e8ddd0;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 0.5rem;
  background: #faf8f5;
  overflow: hidden;
}

.cover-image-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: white;
}

.remove-cover-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(220, 38, 38, 0.9);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.cover-image-wrapper:hover .remove-cover-btn {
  opacity: 1;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  color: #b8a898;
}

.cover-placeholder span {
  font-size: 0.8125rem;
}

.cover-url-input {
  margin-top: 0.25rem;
}

/* 评分 */
.rating-wrapper {
  display: flex;
  align-items: center;
}

:deep(.el-rate) {
  display: flex;
  align-items: center;
}

:deep(.el-rate__icon) {
  font-size: 1.25rem;
  margin-right: 4px;
}

:deep(.el-rate__text) {
  font-family: 'Nunito', sans-serif;
  font-size: 0.875rem;
  color: #6b5344;
  margin-left: 0.5rem;
}

/* 全宽元素 */
.full-width {
  grid-column: 1 / -1;
}

/* 数据来源 */
.data-source {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
  padding: 0.625rem 1rem;
  background: linear-gradient(135deg, #f0f7f4 0%, #e8f4ef 100%);
  border-radius: 8px;
  color: #2d5a47;
  font-size: 0.8125rem;
}

/* ==================== 对话框底部 ==================== */

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1.25rem 2rem;
}

.btn-cancel {
  padding: 0.75rem 1.5rem;
  border: 2px solid #e8ddd0;
  border-radius: 10px;
  background: white;
  color: #6b5344;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 0.9375rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  border-color: #c4b5a0;
  background: #faf8f5;
}

.btn-submit {
  padding: 0.75rem 1.75rem;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  color: white;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 0.9375rem;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(107, 83, 68, 0.3);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* ==================== 动画 ==================== */

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ==================== 响应式 ==================== */

@media (max-width: 640px) {
  :deep(.book-auto-fill-dialog) {
    width: 95% !important;
    margin: 1rem auto !important;
  }

  .dialog-content {
    padding: 1.25rem;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-column-right {
    padding-left: 0;
    border-left: none;
    border-top: 1px solid #f0e8dc;
    padding-top: 1rem;
  }

  .section-hint {
    display: none;
  }
}
</style>