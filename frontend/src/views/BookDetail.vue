<template>
  <div class="book-detail">
    <el-card v-loading="loading">
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

      <el-tabs v-model="activeTab">
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
              <p class="note-content">{{ note.content }}</p>
              <div v-if="note.chapter" class="note-meta">
                章节: {{ note.chapter }}
                <span v-if="note.pageNumber"> | 页码: {{ note.pageNumber }}</span>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

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

    <!-- Add Note Dialog -->
    <el-dialog v-model="noteDialogVisible" title="添加读书感悟" width="600px">
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

        <el-form-item label="内容" prop="content">
          <el-input v-model="noteForm.content" type="textarea" :rows="5" placeholder="请输入感悟内容" />
        </el-form-item>

        <el-form-item label="章节">
          <el-input v-model="noteForm.chapter" placeholder="章节（可选）" />
        </el-form-item>

        <el-form-item label="页码">
          <el-input-number v-model="noteForm.pageNumber" :min="0" />
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="noteForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>

        <el-form-item label="是否私密">
          <el-switch v-model="noteForm.isPrivate" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="noteDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="noteLoading" @click="handleAddNote">确定</el-button>
      </template>
    </el-dialog>

    <!-- Add Quote Dialog -->
    <el-dialog v-model="quoteDialogVisible" title="添加金句" width="600px">
      <el-form ref="quoteFormRef" :model="quoteForm" :rules="quoteRules" label-width="100px">
        <el-form-item label="金句内容" prop="content">
          <el-input v-model="quoteForm.content" type="textarea" :rows="4" placeholder="请输入金句内容" />
        </el-form-item>

        <el-form-item label="章节">
          <el-input v-model="quoteForm.chapter" placeholder="章节（可选）" />
        </el-form-item>

        <el-form-item label="页码">
          <el-input-number v-model="quoteForm.pageNumber" :min="0" />
        </el-form-item>

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

    <!-- Edit Book Dialog -->
    <el-dialog v-model="editDialogVisible" title="编辑书籍" width="600px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="书名" prop="title">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="editForm.author" />
        </el-form-item>
        <el-form-item label="阅读状态" prop="readingStatus">
          <el-select v-model="editForm.readingStatus">
            <el-option label="想读" value="WANT_TO_READ" />
            <el-option label="在读" value="READING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已放弃" value="ABANDONED" />
          </el-select>
        </el-form-item>
        <el-form-item label="总页数">
          <el-input-number v-model="editForm.pageCount" :min="0" />
        </el-form-item>
        <el-form-item label="当前页码">
          <el-input-number v-model="editForm.currentPage" :min="0" />
        </el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="editForm.rating" :max="5" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditBook">保存</el-button>
      </template>
    </el-dialog>

    <!-- Progress Dialog -->
    <el-dialog v-model="progressDialogVisible" title="更新阅读进度" width="400px">
      <el-form ref="progressFormRef" :model="progressForm" label-width="100px">
        <el-form-item label="当前页码">
          <el-input-number v-model="progressForm.currentPage" :min="0" :max="book.pageCount || 9999" />
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { bookApi, readingNoteApi, quoteApi } from '@/api/modules'
import { ElMessage } from 'element-plus'

const route = useRoute()
const bookId = route.params.id

const loading = ref(false)
const book = ref({})
const notes = ref([])
const quotes = ref([])
const activeTab = ref('detail')
const defaultCover = 'https://via.placeholder.com/200x280?text=No+Cover'

// Note dialog
const noteDialogVisible = ref(false)
const noteLoading = ref(false)
const noteFormRef = ref(null)
const noteForm = reactive({
  noteType: 'THOUGHT',
  title: '',
  content: '',
  chapter: '',
  pageNumber: null,
  tags: '',
  isPrivate: false
})

const noteRules = {
  noteType: [{ required: true, message: '请选择感悟类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入感悟内容', trigger: 'blur' }]
}

// Quote dialog
const quoteDialogVisible = ref(false)
const quoteLoading = ref(false)
const quoteFormRef = ref(null)
const quoteForm = reactive({
  content: '',
  chapter: '',
  pageNumber: null,
  note: '',
  color: '#409EFF',
  tags: ''
})

const quoteRules = {
  content: [{ required: true, message: '请输入金句内容', trigger: 'blur' }]
}

// Edit book dialog
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  title: '',
  author: '',
  readingStatus: 'WANT_TO_READ',
  pageCount: null,
  currentPage: 0,
  rating: null
})

const editRules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  readingStatus: [{ required: true, message: '请选择阅读状态', trigger: 'change' }]
}

// Progress dialog
const progressDialogVisible = ref(false)
const progressLoading = ref(false)
const progressFormRef = ref(null)
const progressForm = reactive({
  currentPage: 0
})

onMounted(() => {
  loadBook()
  loadNotes()
  loadQuotes()
})

async function loadBook() {
  loading.value = true
  try {
    const response = await bookApi.getBookById(bookId)
    book.value = response.data
  } catch (error) {
    console.error('Failed to load book:', error)
  } finally {
    loading.value = false
  }
}

async function loadNotes() {
  try {
    const response = await readingNoteApi.getNotesByBookId(bookId, { page: 0, size: 100 })
    notes.value = response.data.content
  } catch (error) {
    console.error('Failed to load notes:', error)
  }
}

async function loadQuotes() {
  try {
    const response = await quoteApi.getQuotesByBookId(bookId, { page: 0, size: 100 })
    quotes.value = response.data.content
  } catch (error) {
    console.error('Failed to load quotes:', error)
  }
}

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

function updateProgress() {
  progressForm.currentPage = book.value.currentPage || 0
  progressDialogVisible.value = true
}

function showAddNoteDialog() {
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
}

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

async function handleAddNote() {
  if (!noteFormRef.value) return

  await noteFormRef.value.validate(async (valid) => {
    if (valid) {
      noteLoading.value = true
      try {
        await readingNoteApi.createNoteForBook(bookId, noteForm)
        ElMessage.success('添加感悟成功')
        noteDialogVisible.value = false
        loadNotes()
      } catch (error) {
        console.error('Failed to add note:', error)
      } finally {
        noteLoading.value = false
      }
    }
  })
}

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
        console.error('Failed to add quote:', error)
      } finally {
        quoteLoading.value = false
      }
    }
  })
}

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
        console.error('Failed to update book:', error)
      } finally {
        editLoading.value = false
      }
    }
  })
}

async function handleUpdateProgress() {
  progressLoading.value = true
  try {
    await bookApi.updateReadingProgress(bookId, progressForm)
    ElMessage.success('更新进度成功')
    progressDialogVisible.value = false
    loadBook()
  } catch (error) {
    console.error('Failed to update progress:', error)
  } finally {
    progressLoading.value = false
  }
}

function getStatusType(status) {
  const types = {
    WANT_TO_READ: '',
    READING: 'warning',
    COMPLETED: 'success',
    ABANDONED: 'info'
  }
  return types[status] || ''
}

function getStatusText(status) {
  const texts = {
    WANT_TO_READ: '想读',
    READING: '在读',
    COMPLETED: '已完成',
    ABANDONED: '已放弃'
  }
  return texts[status] || status
}

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
.book-detail {
  max-width: 1200px;
  margin: 0 auto;
}

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

.note-content {
  margin: 0 0 10px 0;
  line-height: 1.6;
}

.note-meta {
  font-size: 12px;
  color: #909399;
}

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
</style>