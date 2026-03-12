<template>
  <div class="bookshelf-page">
    <!-- Undo Notification Bar -->
    <transition name="slide-down">
      <div v-if="undoNotification.show" class="undo-notification">
        <div class="undo-content">
          <div class="undo-icon">📖</div>
          <div class="undo-text">
            <span class="undo-title">已删除《{{ undoNotification.bookTitle }}》</span>
            <span class="undo-timer">{{ undoNotification.countdown }}秒后永久删除</span>
          </div>
          <button class="undo-button" @click="handleUndoDelete">
            <span>撤销删除</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
              <path d="M3 3v5h5"/>
            </svg>
          </button>
        </div>
        <div class="undo-progress" :style="{ width: undoNotification.progress + '%' }"></div>
      </div>
    </transition>

    <!-- Header -->
    <header class="page-header">
      <div class="header-content">
        <h1 class="page-title">我的书架</h1>
        <p class="page-subtitle">珍藏每一本值得铭记的书</p>
      </div>
      <button class="add-book-btn" @click="showAddDialog">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        <span>添加书籍</span>
      </button>
    </header>

    <!-- Search & Filter Bar -->
    <div class="controls-bar">
      <div class="search-wrapper">
        <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索书名、作者或ISBN..."
          class="search-input"
          @keyup.enter="handleSearch"
        />
      </div>

      <div class="filter-group">
        <select v-model="filterStatus" class="filter-select" @change="handleFilter">
          <option value="">全部状态</option>
          <option value="WANT_TO_READ">想读</option>
          <option value="READING">在读</option>
          <option value="COMPLETED">已完成</option>
          <option value="ABANDONED">已放弃</option>
        </select>
      </div>
    </div>

    <!-- Books Grid -->
    <div v-if="loading" class="loading-state">
      <div class="loader"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="books.length === 0" class="empty-state">
      <div class="empty-icon">📚</div>
      <h3>书架空空如也</h3>
      <p>添加你的第一本书，开始记录阅读旅程</p>
      <button class="add-first-book-btn" @click="showAddDialog">添加第一本书</button>
    </div>

    <div v-else class="books-grid">
      <div v-for="book in books" :key="book.id" class="book-card" @click="viewBook(book.id)">
        <div class="book-cover">
          <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" />
          <div v-else class="cover-placeholder">
            <span>{{ book.title.charAt(0) }}</span>
          </div>
          <div class="book-overlay">
            <button class="overlay-btn edit-btn" @click.stop="editBook(book)" title="编辑">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
            </button>
            <button class="overlay-btn delete-btn" @click.stop="deleteBook(book)" title="删除">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 6h18"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="book-info">
          <h3 class="book-title">{{ book.title }}</h3>
          <p class="book-author">{{ book.author || '未知作者' }}</p>

          <div class="book-meta">
            <span :class="['status-badge', `status-${book.readingStatus.toLowerCase()}`]">
              {{ getStatusText(book.readingStatus) }}
            </span>
            <span v-if="book.rating" class="rating">★ {{ book.rating }}/5</span>
          </div>

          <div v-if="book.progress !== null && book.progress > 0" class="progress-section">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: book.progress + '%' }"></div>
            </div>
            <span class="progress-text">{{ book.progress }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="books.length > 0" class="pagination">
      <button
        class="page-btn"
        :disabled="currentPage === 1"
        @click="changePage(currentPage - 1)"
      >
        上一页
      </button>
      <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
      <button
        class="page-btn"
        :disabled="currentPage === totalPages"
        @click="changePage(currentPage + 1)"
      >
        下一页
      </button>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑书籍' : '添加新书'"
      width="600px"
      custom-class="book-dialog"
    >
      <el-form ref="bookFormRef" :model="bookForm" :rules="bookRules" label-width="100px">
        <el-form-item label="书名" prop="title">
          <el-input v-model="bookForm.title" placeholder="请输入书名" />
        </el-form-item>

        <el-form-item label="作者" prop="author">
          <el-input v-model="bookForm.author" placeholder="请输入作者" />
        </el-form-item>

        <el-form-item label="阅读状态" prop="readingStatus">
          <el-select v-model="bookForm.readingStatus" placeholder="请选择阅读状态">
            <el-option label="想读" value="WANT_TO_READ" />
            <el-option label="在读" value="READING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已放弃" value="ABANDONED" />
          </el-select>
        </el-form-item>

        <el-form-item label="总页数">
          <el-input-number v-model="bookForm.pageCount" :min="0" />
        </el-form-item>

        <el-form-item label="当前页码">
          <el-input-number v-model="bookForm.currentPage" :min="0" />
        </el-form-item>

        <el-form-item label="ISBN">
          <el-input v-model="bookForm.isbn" placeholder="请输入ISBN" />
        </el-form-item>

        <el-form-item label="出版社">
          <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
        </el-form-item>

        <el-form-item label="评分">
          <el-rate v-model="bookForm.rating" :max="5" />
        </el-form-item>

        <el-form-item label="简介">
          <el-input v-model="bookForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { bookApi } from '@/api/modules'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const books = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
const searchKeyword = ref('')
const filterStatus = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const bookFormRef = ref(null)

// Undo notification state
const undoNotification = ref({
  show: false,
  bookTitle: '',
  bookData: null,
  countdown: 30,
  progress: 100,
  timer: null
})

const bookForm = reactive({
  title: '',
  author: '',
  readingStatus: 'WANT_TO_READ',
  pageCount: null,
  currentPage: 0,
  isbn: '',
  publisher: '',
  rating: null,
  description: ''
})

const bookRules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  readingStatus: [{ required: true, message: '请选择阅读状态', trigger: 'change' }]
}

onMounted(() => {
  loadBooks()
})

onBeforeUnmount(() => {
  clearUndoTimer()
})

function clearUndoTimer() {
  if (undoNotification.value.timer) {
    clearInterval(undoNotification.value.timer)
    undoNotification.value.timer = null
  }
}

async function loadBooks() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const response = await bookApi.getBooks(params)
    books.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('Failed to load books:', error)
    ElMessage.error('加载书籍失败')
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  if (!searchKeyword.value) {
    loadBooks()
    return
  }
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const response = await bookApi.searchBooks(searchKeyword.value, params)
    books.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('Failed to search books:', error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

async function handleFilter() {
  if (!filterStatus.value) {
    loadBooks()
    return
  }
  loading.value = true
  try {
    const response = await bookApi.getBooksByStatus(filterStatus.value)
    books.value = response.data
    total.value = response.data.length
  } catch (error) {
    console.error('Failed to filter books:', error)
    ElMessage.error('筛选失败')
  } finally {
    loading.value = false
  }
}

function showAddDialog() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function editBook(book) {
  isEdit.value = true
  editId.value = book.id
  Object.assign(bookForm, book)
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!bookFormRef.value) return

  await bookFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await bookApi.updateBook(editId.value, bookForm)
          ElMessage.success('更新成功')
        } else {
          await bookApi.createBook(bookForm)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadBooks()
      } catch (error) {
        console.error('Failed to save book:', error)
        ElMessage.error('保存失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

async function deleteBook(book) {
  // Clear any existing undo timer
  clearUndoTimer()

  // Store book data for potential undo
  const deletedBook = { ...book }

  try {
    await bookApi.deleteBook(book.id)

    // Remove from current list immediately
    books.value = books.value.filter(b => b.id !== book.id)
    total.value -= 1

    // Show undo notification
    undoNotification.value = {
      show: true,
      bookTitle: book.title,
      bookData: deletedBook,
      countdown: 30,
      progress: 100,
      timer: null
    }

    // Start countdown
    const startTime = Date.now()
    const duration = 30000 // 30 seconds

    undoNotification.value.timer = setInterval(() => {
      const elapsed = Date.now() - startTime
      const remaining = Math.max(0, duration - elapsed)

      undoNotification.value.countdown = Math.ceil(remaining / 1000)
      undoNotification.value.progress = (remaining / duration) * 100

      if (remaining <= 0) {
        clearUndoTimer()
        undoNotification.value.show = false
      }
    }, 100)

  } catch (error) {
    console.error('Failed to delete book:', error)
    ElMessage.error('删除失败')
  }
}

async function handleUndoDelete() {
  clearUndoTimer()

  try {
    // Recreate the book
    const response = await bookApi.createBook(undoNotification.value.bookData)
    ElMessage.success('已撤销删除')

    // Hide notification
    undoNotification.value.show = false

    // Reload books
    loadBooks()
  } catch (error) {
    console.error('Failed to undo delete:', error)
    ElMessage.error('撤销失败')
  }
}

function viewBook(id) {
  router.push(`/books/${id}`)
}

function changePage(page) {
  currentPage.value = page
  if (searchKeyword.value) {
    handleSearch()
  } else if (filterStatus.value) {
    handleFilter()
  } else {
    loadBooks()
  }
}

function resetForm() {
  Object.assign(bookForm, {
    title: '',
    author: '',
    readingStatus: 'WANT_TO_READ',
    pageCount: null,
    currentPage: 0,
    isbn: '',
    publisher: '',
    rating: null,
    description: ''
  })
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
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Crimson+Pro:wght@400;600;700&family=Nunito:wght@400;500;600;700&display=swap');

.bookshelf-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #faf8f5 0%, #f5f1eb 100%);
  padding: 2rem;
  font-family: 'Nunito', sans-serif;
  position: relative;
}

/* Undo Notification */
.undo-notification {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #2c1810 0%, #3d2317 100%);
  color: white;
  z-index: 9999;
  box-shadow: 0 4px 20px rgba(44, 24, 16, 0.3);
}

.undo-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.25rem 2rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.undo-icon {
  font-size: 2rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.undo-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.undo-title {
  font-family: 'Crimson Pro', serif;
  font-size: 1.125rem;
  font-weight: 600;
}

.undo-timer {
  font-size: 0.875rem;
  opacity: 0.9;
}

.undo-button {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  backdrop-filter: blur(10px);
}

.undo-button:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.undo-progress {
  height: 3px;
  background: rgba(255, 255, 255, 0.3);
  transition: width 0.1s linear;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from {
  transform: translateY(-100%);
}

.slide-down-leave-to {
  transform: translateY(-100%);
}

/* Header */
.page-header {
  max-width: 1400px;
  margin: 0 auto 3rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-family: 'Crimson Pro', serif;
  font-size: 3rem;
  font-weight: 700;
  color: #2c1810;
  margin: 0;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 1.125rem;
  color: #6b5344;
  margin: 0.5rem 0 0;
  font-weight: 500;
}

.add-book-btn {
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  border: none;
  color: white;
  padding: 1rem 2rem;
  border-radius: 12px;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  box-shadow: 0 4px 15px rgba(107, 83, 68, 0.3);
}

.add-book-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(107, 83, 68, 0.4);
}

/* Controls */
.controls-bar {
  max-width: 1400px;
  margin: 0 auto 2rem;
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.search-wrapper {
  flex: 1;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 1.25rem;
  top: 50%;
  transform: translateY(-50%);
  color: #8b6f47;
  opacity: 0.6;
}

.search-input {
  width: 100%;
  padding: 1rem 1rem 1rem 3.5rem;
  border: 2px solid #e8ddd0;
  border-radius: 12px;
  font-family: 'Nunito', sans-serif;
  font-size: 1rem;
  background: white;
  color: #2c1810;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #8b6f47;
  box-shadow: 0 0 0 4px rgba(139, 111, 71, 0.1);
}

.filter-select {
  padding: 1rem 2rem 1rem 1.25rem;
  border: 2px solid #e8ddd0;
  border-radius: 12px;
  font-family: 'Nunito', sans-serif;
  font-size: 1rem;
  background: white;
  color: #2c1810;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 150px;
}

.filter-select:focus {
  outline: none;
  border-color: #8b6f47;
}

/* Books Grid */
.books-grid {
  max-width: 1400px;
  margin: 0 auto 3rem;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

.book-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(44, 24, 16, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.book-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 30px rgba(44, 24, 16, 0.15);
}

.book-cover {
  position: relative;
  aspect-ratio: 3/4;
  background: linear-gradient(135deg, #e8ddd0 0%, #d4c4b0 100%);
  overflow: hidden;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
}

.cover-placeholder span {
  font-family: 'Crimson Pro', serif;
  font-size: 4rem;
  font-weight: 700;
  color: white;
  opacity: 0.3;
}

.book-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(44, 24, 16, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.book-card:hover .book-overlay {
  opacity: 1;
}

.overlay-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.15);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.overlay-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.1);
}

.delete-btn:hover {
  background: rgba(220, 38, 38, 0.7);
  border-color: rgba(220, 38, 38, 0.9);
}

.book-info {
  padding: 1.5rem;
}

.book-title {
  font-family: 'Crimson Pro', serif;
  font-size: 1.25rem;
  font-weight: 700;
  color: #2c1810;
  margin: 0 0 0.5rem;
  line-height: 1.3;
}

.book-author {
  font-size: 0.9375rem;
  color: #6b5344;
  margin: 0 0 1rem;
}

.book-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.status-badge {
  padding: 0.375rem 0.875rem;
  border-radius: 20px;
  font-size: 0.8125rem;
  font-weight: 600;
}

.status-want_to_read {
  background: #fef3c7;
  color: #92400e;
}

.status-reading {
  background: #dbeafe;
  color: #1e40af;
}

.status-completed {
  background: #d1fae5;
  color: #065f46;
}

.status-abandoned {
  background: #f3f4f6;
  color: #374151;
}

.rating {
  color: #f59e0b;
  font-weight: 700;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #e8ddd0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #8b6f47 0%, #6b5344 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 0.8125rem;
  font-weight: 600;
  color: #6b5344;
  min-width: 40px;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 1.5rem;
}

.empty-state h3 {
  font-family: 'Crimson Pro', serif;
  font-size: 2rem;
  color: #2c1810;
  margin: 0 0 1rem;
}

.empty-state p {
  font-size: 1.125rem;
  color: #6b5344;
  margin: 0 0 2rem;
}

.add-first-book-btn {
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  border: none;
  color: white;
  padding: 1rem 2.5rem;
  border-radius: 12px;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-first-book-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(107, 83, 68, 0.4);
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 4rem 2rem;
}

.loader {
  width: 48px;
  height: 48px;
  border: 4px solid #e8ddd0;
  border-top-color: #8b6f47;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Pagination */
.pagination {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1.5rem;
}

.page-btn {
  padding: 0.75rem 1.5rem;
  border: 2px solid #8b6f47;
  background: white;
  color: #8b6f47;
  border-radius: 8px;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #8b6f47;
  color: white;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 0.9375rem;
  color: #6b5344;
  font-weight: 600;
}

/* Dialog Styling */
:deep(.book-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #faf8f5 0%, #f5f1eb 100%);
  border-bottom: 2px solid #e8ddd0;
  padding: 1.5rem 2rem;
}

:deep(.el-dialog__title) {
  font-family: 'Crimson Pro', serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c1810;
}

:deep(.el-dialog__body) {
  padding: 2rem;
}

:deep(.el-form-item__label) {
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  color: #2c1810;
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  border-color: #e8ddd0;
}

:deep(.el-input__inner:focus),
:deep(.el-textarea__inner:focus) {
  border-color: #8b6f47;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  border: none;
  border-radius: 8px;
  font-weight: 600;
}

:deep(.el-button--default) {
  border: 2px solid #e8ddd0;
  border-radius: 8px;
  font-weight: 600;
  color: #6b5344;
}
</style>