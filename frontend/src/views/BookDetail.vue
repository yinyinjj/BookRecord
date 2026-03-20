<template>
  <!-- 书籍详情页面 -->
  <div class="book-detail">
    <el-card v-loading="loading">
      <!-- 书籍头部信息区域 -->
      <div class="book-header">
        <div class="book-cover">
          <img :src="book.coverUrl || defaultCover" alt="cover" />
        </div>
        <div class="book-info">
          <h2>{{ book.title }}</h2>
          <p class="author">{{ book.author }}</p>
          <div class="meta">
            <el-tag :type="getStatusType(book.readingStatus)">
              {{ getStatusText(book.readingStatus) }}
            </el-tag>
            <span v-if="book.rating" class="rating">
              评分: {{ book.rating }}/5
            </span>
          </div>
          <div v-if="book.progress !== null" class="progress">
            <span>阅读进度: {{ book.progress }}%</span>
            <el-progress :percentage="book.progress" />
          </div>
          <div class="actions">
            <el-button type="primary" @click="showEditDialog">编辑</el-button>
            <el-button @click="updateProgress">更新进度</el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <!-- 内容标签页区域 -->
      <el-tabs v-model="activeTab">
        <!-- 详情标签页 -->
        <el-tab-pane label="详情" name="detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="ISBN">{{ book.isbn || '-' }}</el-descriptions-item>
            <el-descriptions-item label="出版社">{{ book.publisher || '-' }}</el-descriptions-item>
            <el-descriptions-item label="出版日期">{{ book.publishDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="页数">{{ book.pageCount || '-' }}</el-descriptions-item>
            <el-descriptions-item label="开始阅读">{{ book.startDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="完成日期">{{ book.finishDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="简介" :span="2">
              {{ book.description || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 读书感悟标签页 -->
        <el-tab-pane label="读书感悟" name="notes">
          <div class="section-header">
            <h3>读书感悟</h3>
            <el-button type="primary" @click="showAddNoteDialog">添加感悟</el-button>
          </div>
          <div v-if="notes.length === 0" class="empty-text">暂无感悟</div>
          <div v-else class="note-list">
            <el-card v-for="note in notes" :key="note.id" class="note-item">
              <div class="note-header">
                <h4>{{ note.title || '无标题' }}</h4>
                <div>
                  <el-tag size="small">{{ getNoteTypeText(note.noteType) }}</el-tag>
                  <span class="note-date">{{ note.createdAt }}</span>
                </div>
              </div>
              <!-- 渲染富文本内容 -->
              <div class="note-content" v-html="getNoteContent(note.content)"></div>
              <div v-if="note.chapter" class="note-meta">
                章节: {{ note.chapter }}
                <span v-if="note.pageNumber"> | 页码: {{ note.pageNumber }}</span>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- 金句收藏标签页 -->
        <el-tab-pane label="金句收藏" name="quotes">
          <div class="section-header">
            <h3>金句收藏</h3>
            <el-button type="primary" @click="showAddQuoteDialog">添加金句</el-button>
          </div>
          <div v-if="quotes.length === 0" class="empty-text">暂无金句</div>
          <div v-else class="quote-list">
            <el-card v-for="quote in quotes" :key="quote.id" class="quote-item">
              <p class="quote-text">"{{ quote.content }}"</p>
              <div class="quote-meta">
                <span v-if="quote.chapter">章节: {{ quote.chapter }}</span>
                <span v-if="quote.pageNumber"> | 页码: {{ quote.pageNumber }}</span>
              </div>
              <div v-if="quote.note" class="quote-note">备注: {{ quote.note }}</div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 添加感悟对话框 -->
    <el-dialog v-model="noteDialogVisible" title="添加读书感悟" width="800px" top="5vh">
      <el-form ref="noteFormRef" :model="noteForm" :rules="noteRules" label-width="100px">
        <el-form-item label="感悟类型" prop="noteType">
          <el-select v-model="noteForm.noteType" placeholder="请选择感悟类型">
            <el-option label="感悟" value="THOUGHT" />
            <el-option label="总结" value="SUMMARY" />
            <el-option label="疑问" value="QUESTION" />
            <el-option label="洞察" value="INSIGHT" />
          </el-select>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="noteForm.title" placeholder="请输入标题（可选）" />
        </el-form-item>

        <!-- 块级富文本编辑器 -->
        <el-form-item label="内容" prop="content" class="editor-form-item">
          <BlockEditor
            ref="noteEditorRef"
            v-model="noteForm.content"
            placeholder="在这里记录你的阅读感悟..."
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="章节">
              <el-input v-model="noteForm.chapter" placeholder="章节（可选）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="页码">
              <el-input-number v-model="noteForm.pageNumber" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="标签">
          <el-input v-model="noteForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>

        <el-form-item label="是否私密">
          <el-switch v-model="noteForm.isPrivate" />
          <span class="form-hint">私密感悟仅自己可见</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="noteDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="noteLoading" @click="handleAddNote">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加金句对话框 -->
    <el-dialog v-model="quoteDialogVisible" title="添加金句" width="600px">
      <el-form ref="quoteFormRef" :model="quoteForm" :rules="quoteRules" label-width="100px">
        <el-form-item label="金句内容" prop="content">
          <el-input v-model="quoteForm.content" type="textarea" :rows="4" placeholder="请输入金句内容" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="章节">
              <el-input v-model="quoteForm.chapter" placeholder="章节（可选）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="页码">
              <el-input-number v-model="quoteForm.pageNumber" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="quoteForm.note" type="textarea" :rows="2" placeholder="备注（可选）" />
        </el-form-item>

        <el-form-item label="颜色标记">
          <el-color-picker v-model="quoteForm.color" />
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="quoteForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="quoteDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="quoteLoading" @click="handleAddQuote">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑书籍对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑书籍" width="600px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="书名" prop="title">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="editForm.author" />
        </el-form-item>
        <el-form-item label="阅读状态" prop="readingStatus">
          <el-select v-model="editForm.readingStatus" style="width: 100%;">
            <el-option label="想读" value="WANT_TO_READ" />
            <el-option label="在读" value="READING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已放弃" value="ABANDONED" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总页数">
              <el-input-number v-model="editForm.pageCount" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前页码">
              <el-input-number v-model="editForm.currentPage" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="评分">
          <el-rate v-model="editForm.rating" :max="5" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditBook">保存</el-button>
      </template>
    </el-dialog>

    <!-- 更新阅读进度对话框 -->
    <el-dialog v-model="progressDialogVisible" title="更新阅读进度" width="400px">
      <el-form ref="progressFormRef" :model="progressForm" label-width="100px">
        <el-form-item label="当前页码">
          <el-input-number v-model="progressForm.currentPage" :min="0" :max="book.pageCount || 9999" style="width: 100%;" />
        </el-form-item>
        <el-form-item v-if="book.pageCount" label="总页数">
          <span>{{ book.pageCount }}</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="progressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="progressLoading" @click="handleUpdateProgress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 书籍详情页面组件
 * 展示书籍详细信息、读书感悟和金句收藏
 * 支持添加感悟（使用块级富文本编辑器）、添加金句、编辑书籍等功能
 */
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { bookApi, readingNoteApi, quoteApi } from '@/api/modules'
import { ElMessage } from 'element-plus'
// 导入块级富文本编辑器组件
import BlockEditor from '@/components/BlockEditor.vue'

// ==================== 路由参数 ====================

const route = useRoute()
const bookId = route.params.id

// ==================== 页面状态 ====================

/** 加载状态 */
const loading = ref(false)

/** 书籍详情数据 */
const book = ref({})

/** 读书感悟列表 */
const notes = ref([])

/** 金句列表 */
const quotes = ref([])

/** 当前激活的标签页 */
const activeTab = ref('detail')

/** 默认封面图片 */
const defaultCover = 'https://via.placeholder.com/200x280?text=No+Cover'

/** 感悟编辑器组件引用 */
const noteEditorRef = ref(null)

// ==================== 感悟对话框状态 ====================

/** 对话框显示状态 */
const noteDialogVisible = ref(false)

/** 提交加载状态 */
const noteLoading = ref(false)

/** 表单引用 */
const noteFormRef = ref(null)

/**
 * 感悟表单数据
 * content 字段存储富文本 HTML 内容
 */
const noteForm = reactive({
  noteType: 'THOUGHT',   // 感悟类型，默认为"感悟"
  title: '',              // 标题（可选）
  content: '',            // 感悟内容（富文本 HTML）
  chapter: '',            // 章节（可选）
  pageNumber: null,       // 页码（可选）
  tags: '',               // 标签，逗号分隔
  isPrivate: false        // 是否私密
})

/**
 * 表单验证规则
 * 感悟类型和内容为必填项
 */
const noteRules = {
  noteType: [{ required: true, message: '请选择感悟类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入感悟内容', trigger: 'blur' }]
}

// ==================== 金句对话框状态 ====================

/** 对话框显示状态 */
const quoteDialogVisible = ref(false)

/** 提交加载状态 */
const quoteLoading = ref(false)

/** 表单引用 */
const quoteFormRef = ref(null)

/**
 * 金句表单数据
 */
const quoteForm = reactive({
  content: '',            // 金句内容（必填）
  chapter: '',            // 章节（可选）
  pageNumber: null,       // 页码（可选）
  note: '',               // 备注（可选）
  color: '#409EFF',       // 颜色标记，默认蓝色
  tags: ''                // 标签，逗号分隔
})

/**
 * 金句表单验证规则
 * 金句内容为必填项
 */
const quoteRules = {
  content: [{ required: true, message: '请输入金句内容', trigger: 'blur' }]
}

// ==================== 编辑书籍对话框状态 ====================

/** 对话框显示状态 */
const editDialogVisible = ref(false)

/** 提交加载状态 */
const editLoading = ref(false)

/** 表单引用 */
const editFormRef = ref(null)

/**
 * 编辑书籍表单数据
 */
const editForm = reactive({
  title: '',                  // 书名（必填）
  author: '',                 // 作者
  readingStatus: 'WANT_TO_READ',  // 阅读状态
  pageCount: null,            // 总页数
  currentPage: 0,             // 当前页码
  rating: null                // 评分（1-5星）
})

/**
 * 编辑书籍表单验证规则
 */
const editRules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  readingStatus: [{ required: true, message: '请选择阅读状态', trigger: 'change' }]
}

// ==================== 更新进度对话框状态 ====================

/** 对话框显示状态 */
const progressDialogVisible = ref(false)

/** 提交加载状态 */
const progressLoading = ref(false)

/** 表单引用 */
const progressFormRef = ref(null)

/**
 * 进度更新表单数据
 */
const progressForm = reactive({
  currentPage: 0  // 当前页码
})

// ==================== 生命周期钩子 ====================

/**
 * 组件挂载时加载书籍详情、感悟列表和金句列表
 */
onMounted(() => {
  loadBook()
  loadNotes()
  loadQuotes()
})

// ==================== 数据加载方法 ====================

/**
 * 加载书籍详情
 * 根据 URL 参数中的 bookId 获取书籍信息
 */
async function loadBook() {
  loading.value = true
  try {
    const response = await bookApi.getBookById(bookId)
    book.value = response.data
  } catch (error) {
    console.error('加载书籍详情失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载读书感悟列表
 * 获取当前书籍的所有感悟记录
 */
async function loadNotes() {
  try {
    const response = await readingNoteApi.getNotesByBookId(bookId, { page: 0, size: 100 })
    notes.value = response.data.content
  } catch (error) {
    console.error('加载感悟列表失败:', error)
  }
}

/**
 * 加载金句列表
 * 获取当前书籍的所有金句收藏
 */
async function loadQuotes() {
  try {
    const response = await quoteApi.getQuotesByBookId(bookId, { page: 0, size: 100 })
    quotes.value = response.data.content
  } catch (error) {
    console.error('加载金句列表失败:', error)
  }
}

// ==================== 对话框操作方法 ====================

/**
 * 显示编辑书籍对话框
 * 将当前书籍信息填充到表单
 */
function showEditDialog() {
  Object.assign(editForm, {
    title: book.value.title,
    author: book.value.author,
    readingStatus: book.value.readingStatus,
    pageCount: book.value.pageCount,
    currentPage: book.value.currentPage,
    rating: book.value.rating
  })
  editDialogVisible.value = true
}

/**
 * 显示更新进度对话框
 * 初始化当前页码
 */
function updateProgress() {
  progressForm.currentPage = book.value.currentPage || 0
  progressDialogVisible.value = true
}

/**
 * 显示添加感悟对话框
 * 重置表单数据，确保编辑器组件已挂载后再设置初始内容
 */
function showAddNoteDialog() {
  // 重置表单数据
  Object.assign(noteForm, {
    noteType: 'THOUGHT',
    title: '',
    content: '',
    chapter: '',
    pageNumber: null,
    tags: '',
    isPrivate: false
  })
  noteDialogVisible.value = true

  // 等待 DOM 更新后清空编辑器内容
  nextTick(() => {
    if (noteEditorRef.value) {
      noteEditorRef.value.clearContent()
    }
  })
}

/**
 * 显示添加金句对话框
 * 重置表单数据
 */
function showAddQuoteDialog() {
  Object.assign(quoteForm, {
    content: '',
    chapter: '',
    pageNumber: null,
    note: '',
    color: '#409EFF',
    tags: ''
  })
  quoteDialogVisible.value = true
}

// ==================== 表单提交处理方法 ====================

/**
 * 处理添加感悟提交
 * 验证表单并调用 API 创建感悟
 */
async function handleAddNote() {
  if (!noteFormRef.value) return

  // 获取富文本编辑器内容
  if (noteEditorRef.value) {
    noteForm.content = noteEditorRef.value.getHTML()
  }

  await noteFormRef.value.validate(async (valid) => {
    if (valid) {
      noteLoading.value = true
      try {
        await readingNoteApi.createNoteForBook(bookId, noteForm)
        ElMessage.success('添加感悟成功')
        noteDialogVisible.value = false
        loadNotes()
      } catch (error) {
        console.error('添加感悟失败:', error)
      } finally {
        noteLoading.value = false
      }
    }
  })
}

/**
 * 处理添加金句提交
 * 验证表单并调用 API 创建金句
 */
async function handleAddQuote() {
  if (!quoteFormRef.value) return

  await quoteFormRef.value.validate(async (valid) => {
    if (valid) {
      quoteLoading.value = true
      try {
        await quoteApi.createQuote(bookId, quoteForm)
        ElMessage.success('添加金句成功')
        quoteDialogVisible.value = false
        loadQuotes()
      } catch (error) {
        console.error('添加金句失败:', error)
      } finally {
        quoteLoading.value = false
      }
    }
  })
}

/**
 * 处理编辑书籍提交
 * 验证表单并调用 API 更新书籍信息
 */
async function handleEditBook() {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await bookApi.updateBook(bookId, editForm)
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        loadBook()
      } catch (error) {
        console.error('更新书籍失败:', error)
      } finally {
        editLoading.value = false
      }
    }
  })
}

/**
 * 处理更新阅读进度提交
 * 调用 API 更新当前阅读页码
 */
async function handleUpdateProgress() {
  progressLoading.value = true
  try {
    await bookApi.updateReadingProgress(bookId, progressForm)
    ElMessage.success('更新进度成功')
    progressDialogVisible.value = false
    loadBook()
  } catch (error) {
    console.error('更新进度失败:', error)
  } finally {
    progressLoading.value = false
  }
}

// ==================== 辅助方法 ====================

/**
 * HTML 反转义函数
 * 将转义后的 HTML 实体（如 &lt; &gt; &amp; 等）还原为原始字符
 * 用于处理数据库中可能被转义存储的富文本内容
 * @param {string} html - 可能被转义的 HTML 内容
 * @returns {string} 反转义后的 HTML 内容
 */
function unescapeHtml(html) {
  if (!html) return ''
  // 创建临时 textarea 元素，利用浏览器的内置解码能力
  const textarea = document.createElement('textarea')
  textarea.innerHTML = html
  return textarea.value
}

/**
 * 获取感悟内容
 * 对可能被转义的 HTML 内容进行反转义处理
 * @param {string} content - 感悟内容
 * @returns {string} 处理后的 HTML 内容
 */
function getNoteContent(content) {
  if (!content) return ''
  // 检测内容是否被转义（包含 &lt; 或 &gt; 等实体）
  if (content.includes('&lt;') || content.includes('&gt;') || content.includes('&amp;')) {
    return unescapeHtml(content)
  }
  return content
}

/**
 * 获取阅读状态对应的标签类型
 * @param {string} status - 阅读状态
 * @returns {string} Element Plus Tag 类型
 */
function getStatusType(status) {
  const types = {
    WANT_TO_READ: 'primary',
    READING: 'warning',
    COMPLETED: 'success',
    ABANDONED: 'info'
  }
  return types[status] || 'primary'
}

/**
 * 获取阅读状态的中文文本
 * @param {string} status - 阅读状态
 * @returns {string} 中文状态文本
 */
function getStatusText(status) {
  const texts = {
    WANT_TO_READ: '想读',
    READING: '在读',
    COMPLETED: '已完成',
    ABANDONED: '已放弃'
  }
  return texts[status] || status
}

/**
 * 获取感悟类型的中文文本
 * @param {string} type - 感悟类型
 * @returns {string} 中文类型文本
 */
function getNoteTypeText(type) {
  const texts = {
    THOUGHT: '感悟',
    SUMMARY: '总结',
    QUESTION: '疑问',
    INSIGHT: '洞察'
  }
  return texts[type] || type
}
</script>

<style scoped>
/* ==================== 页面容器样式 ==================== */

.book-detail {
  max-width: 1200px;
  margin: 0 auto;
}

/* ==================== 书籍头部样式 ==================== */

.book-header {
  display: flex;
  gap: 30px;
}

.book-cover img {
  width: 200px;
  height: 280px;
  object-fit: cover;
  border-radius: 8px;
}

.book-info {
  flex: 1;
}

.book-info h2 {
  margin: 0 0 10px 0;
}

.author {
  color: #909399;
  margin: 0 0 15px 0;
}

.meta {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.rating {
  font-size: 14px;
  color: #606266;
}

.progress {
  margin-bottom: 20px;
}

.progress span {
  display: block;
  margin-bottom: 10px;
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 10px;
}

/* ==================== 分区头部样式 ==================== */

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

/* ==================== 感悟列表样式 ==================== */

.note-list, .quote-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.note-item {
  margin: 0;
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.note-header h4 {
  margin: 0;
}

.note-date {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

/* 富文本内容渲染样式 */
.note-content {
  margin: 0 0 10px 0;
  line-height: 1.8;
  color: #303133;
  font-size: 15px;
}

.note-content :deep(h1) {
  font-size: 1.8em;
  margin: 0.8em 0 0.4em;
  color: #303133;
}

.note-content :deep(h2) {
  font-size: 1.5em;
  margin: 0.7em 0 0.3em;
  color: #303133;
}

.note-content :deep(h3) {
  font-size: 1.25em;
  margin: 0.6em 0 0.3em;
  color: #303133;
}

.note-content :deep(p) {
  margin: 0.5em 0;
}

.note-content :deep(blockquote) {
  margin: 1em 0;
  padding: 0.8em 1.2em;
  border-left: 4px solid #c9a96e;
  background: #fdfbf7;
  color: #5d4e37;
  border-radius: 0 4px 4px 0;
}

.note-content :deep(pre) {
  background: #f5f5f5;
  padding: 1em;
  border-radius: 4px;
  overflow-x: auto;
  margin: 1em 0;
}

.note-content :deep(code) {
  background: #f5f5f5;
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: monospace;
  font-size: 0.9em;
}

.note-content :deep(ul),
.note-content :deep(ol) {
  padding-left: 1.5em;
  margin: 0.5em 0;
}

.note-content :deep(li) {
  margin: 0.3em 0;
}

.note-content :deep(mark) {
  background: #fff3cd;
  padding: 0 2px;
}

.note-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
}

.note-content :deep(th),
.note-content :deep(td) {
  border: 1px solid #ebeef5;
  padding: 8px 12px;
  text-align: left;
}

.note-content :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
}

.note-meta {
  font-size: 12px;
  color: #909399;
}

/* ==================== 金句列表样式 ==================== */

.quote-item {
  margin: 0;
}

.quote-text {
  font-style: italic;
  line-height: 1.8;
  margin: 0 0 10px 0;
  font-size: 15px;
}

.quote-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.quote-note {
  font-size: 14px;
  color: #606266;
  padding-top: 10px;
  border-top: 1px dashed #dcdfe6;
}

/* ==================== 编辑器表单项样式 ==================== */

.editor-form-item {
  margin-bottom: 20px;
}

.editor-form-item :deep(.el-form-item__content) {
  line-height: 1;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}
</style>