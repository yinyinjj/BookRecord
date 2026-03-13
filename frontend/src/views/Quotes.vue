<template>
  <div class="quotes-page">
    <!-- Header -->
    <header class="page-header">
      <div class="header-content">
        <h1 class="page-title">珍藏金句</h1>
        <p class="page-subtitle">摘录文字的永恒瞬间</p>
      </div>
    </header>

    <!-- Featured Quote (Random) -->
    <div v-if="randomQuote" class="featured-quote-section">
      <div class="featured-label">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
        </svg>
        <span>今日金句</span>
      </div>
      <div class="featured-quote-card">
        <div class="quote-decoration">❝</div>
        <div class="featured-content">{{ randomQuote.content }}</div>
        <div class="featured-meta">
          <span class="book-title">《{{ randomQuote.bookTitle }}》</span>
          <span v-if="randomQuote.chapter" class="meta-divider">·</span>
          <span v-if="randomQuote.chapter">{{ randomQuote.chapter }}</span>
        </div>
      </div>
    </div>

    <!-- Controls Bar -->
    <div class="controls-bar">
      <!-- Search -->
      <div class="search-wrapper">
        <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索金句内容..."
          class="search-input"
          @keyup.enter="handleSearch"
        />
      </div>

      <!-- Filters -->
      <div class="filters-group">
        <div class="color-filter-wrapper">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <circle cx="12" cy="12" r="4" fill="currentColor"></circle>
          </svg>
          <select v-model="filterColor" class="color-select" @change="handleFilter">
            <option value="">全部颜色</option>
            <option value="red">🔴 红色</option>
            <option value="blue">🔵 蓝色</option>
            <option value="green">🟢 绿色</option>
            <option value="yellow">🟡 黄色</option>
            <option value="purple">🟣 紫色</option>
            <option value="orange">🟠 橙色</option>
            <option value="pink">🩷 粉色</option>
          </select>
        </div>

        <div class="tag-input-wrapper">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path>
            <line x1="7" y1="7" x2="7.01" y2="7"></line>
          </svg>
          <input
            v-model="filterTag"
            type="text"
            placeholder="标签筛选..."
            class="tag-input"
            @keyup.enter="handleFilter"
          />
        </div>
      </div>
    </div>

    <!-- Quotes List -->
    <div v-if="loading" class="loading-state">
      <div class="loader"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="quotes.length === 0" class="empty-state">
      <div class="empty-icon">✨</div>
      <h3>还没有金句</h3>
      <p>在书籍详情页添加你的第一个金句吧</p>
    </div>

    <div v-else class="quotes-grid">
      <div
        v-for="(quote, index) in quotes"
        :key="quote.id"
        class="quote-card"
        :style="{
          animationDelay: `${index * 0.05}s`,
          '--accent-color': getColorValue(quote.color)
        }"
      >
        <!-- Color Marker -->
        <div class="color-marker" :style="{ backgroundColor: getColorValue(quote.color) }"></div>

        <!-- Quote Content -->
        <div class="quote-content">
          <div class="quote-text">"{{ quote.content }}"</div>

          <!-- Quote Meta -->
          <div class="quote-meta">
            <div class="book-info">
              <span class="book-label">《</span>
              <span class="book-name">{{ quote.bookTitle }}</span>
              <span class="book-label">》</span>
            </div>
            <div v-if="quote.chapter || quote.pageNumber" class="location-info">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
              </svg>
              <span v-if="quote.chapter">{{ quote.chapter }}</span>
              <span v-if="quote.chapter && quote.pageNumber"> · </span>
              <span v-if="quote.pageNumber">第{{ quote.pageNumber }}页</span>
            </div>
          </div>

          <!-- Note -->
          <div v-if="quote.note" class="quote-note">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
            </svg>
            <span>{{ quote.note }}</span>
          </div>

          <!-- Tags -->
          <div v-if="quote.tags" class="tags-wrapper">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path>
              <line x1="7" y1="7" x2="7.01" y2="7"></line>
            </svg>
            <div class="tags-list">
              <span
                v-for="tag in parseTags(quote.tags)"
                :key="tag"
                class="tag-item"
                @click="filterByTag(tag)"
              >
                {{ tag }}
              </span>
            </div>
          </div>

          <!-- Footer: Actions & Time -->
          <div class="quote-footer">
            <div class="quote-actions">
              <button class="action-btn share-btn" @click="handleShare(quote)" title="分享金句">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="18" cy="5" r="3"></circle>
                  <circle cx="6" cy="12" r="3"></circle>
                  <circle cx="18" cy="19" r="3"></circle>
                  <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line>
                  <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line>
                </svg>
              </button>
              <button class="action-btn edit-btn" @click="handleEdit(quote)" title="编辑金句">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button class="action-btn delete-btn" @click="handleDelete(quote)" title="删除金句">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>

            <div class="quote-time">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <span>{{ formatDate(quote.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="quotes.length > 0" class="pagination">
      <button
        class="page-btn"
        :disabled="currentPage === 1"
        @click="changePage(currentPage - 1)"
      >
        上一页
      </button>
      <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页 · 共 {{ total }} 条</span>
      <button
        class="page-btn"
        :disabled="currentPage === totalPages"
        @click="changePage(currentPage + 1)"
      >
        下一页
      </button>
    </div>

    <!-- Edit Quote Dialog -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑金句"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="金句内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入金句内容"
          />
        </el-form-item>

        <el-form-item label="章节">
          <el-input v-model="editForm.chapter" placeholder="例如：第三章" />
        </el-form-item>

        <el-form-item label="页码">
          <el-input-number v-model="editForm.pageNumber" :min="1" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="editForm.note" type="textarea" :rows="2" placeholder="添加备注..." />
        </el-form-item>

        <el-form-item label="颜色标记">
          <div class="color-picker">
            <div
              v-for="color in colorOptions"
              :key="color.value"
              :class="['color-option', { active: editForm.color === color.value }]"
              :style="{ backgroundColor: color.hex }"
              @click="editForm.color = color.value"
            >
              <svg v-if="editForm.color === color.value" width="16" height="16" viewBox="0 0 24 24" fill="white" stroke="white" stroke-width="3">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="editForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editLoading" @click="submitEdit">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分享对话框 -->
    <ShareDialog
      v-model="shareDialogVisible"
      resource-type="quote"
      :resource-id="shareQuoteId"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { quoteApi } from '@/api/modules'
import { ElMessage, ElNotification } from 'element-plus'
import ShareDialog from '@/components/ShareDialog.vue'

const loading = ref(false)
const quotes = ref([])
const randomQuote = ref(null)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const searchKeyword = ref('')
const filterColor = ref('')
const filterTag = ref('')

// Edit quote dialog
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editLoading = ref(false)
const editForm = ref({
  id: null,
  content: '',
  chapter: '',
  pageNumber: null,
  note: '',
  color: 'yellow',
  tags: ''
})

const editRules = {
  content: [{ required: true, message: '请输入金句内容', trigger: 'blur' }]
}

// Color options
const colorOptions = [
  { value: 'red', hex: '#ef4444' },
  { value: 'blue', hex: '#3b82f6' },
  { value: 'green', hex: '#10b981' },
  { value: 'yellow', hex: '#f59e0b' },
  { value: 'purple', hex: '#8b5cf6' },
  { value: 'orange', hex: '#f97316' },
  { value: 'pink', hex: '#ec4899' }
]

// Undo delete
const deletedQuotes = ref(new Map())
const deleteTimers = ref(new Map())

// Share dialog
const shareDialogVisible = ref(false)
const shareQuoteId = ref(null)

onMounted(() => {
  loadQuotes()
  loadRandomQuote()
})

async function loadQuotes() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      color: filterColor.value || undefined,
      tag: filterTag.value || undefined
    }

    const response = await quoteApi.searchQuotes(searchKeyword.value || '', params)
    quotes.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('Failed to load quotes:', error)
    ElMessage.error('加载金句失败')
  } finally {
    loading.value = false
  }
}

async function loadRandomQuote() {
  try {
    const response = await quoteApi.getRandomQuote()
    randomQuote.value = response.data
  } catch (error) {
    console.error('Failed to load random quote:', error)
  }
}

async function handleSearch() {
  currentPage.value = 1
  await loadQuotes()
}

async function handleFilter() {
  currentPage.value = 1
  await loadQuotes()
}

function filterByTag(tag) {
  filterTag.value = tag
  handleFilter()
}

async function changePage(page) {
  currentPage.value = page
  await loadQuotes()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function parseTags(tags) {
  if (!tags) return []
  return tags.split(',').map(tag => tag.trim()).filter(tag => tag)
}

function getColorValue(color) {
  const colors = {
    red: '#dc2626',
    blue: '#2563eb',
    green: '#16a34a',
    yellow: '#ca8a04',
    purple: '#9333ea',
    orange: '#ea580c',
    pink: '#ec4899'
  }
  return colors[color] || '#6b7280'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    if (hours < 1) return '刚刚'
    if (hours < 24) return `${hours}小时前`
  }

  if (diff < 604800000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  }

  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// Edit quote
function handleEdit(quote) {
  editForm.value = {
    id: quote.id,
    content: quote.content || '',
    chapter: quote.chapter || '',
    pageNumber: quote.pageNumber || null,
    note: quote.note || '',
    color: quote.color || 'yellow',
    tags: quote.tags || ''
  }
  editDialogVisible.value = true
}

async function submitEdit() {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await quoteApi.updateQuote(editForm.value.id, editForm.value)
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        await loadQuotes()
      } catch (error) {
        console.error('Failed to update quote:', error)
        ElMessage.error('更新失败')
      } finally {
        editLoading.value = false
      }
    }
  })
}

// Delete quote
async function handleDelete(quote) {
  const quoteIndex = quotes.value.findIndex(q => q.id === quote.id)
  if (quoteIndex === -1) return

  const deletedQuote = quotes.value[quoteIndex]

  // Remove from UI immediately
  quotes.value.splice(quoteIndex, 1)
  total.value -= 1

  // Store the quote for undo
  deletedQuotes.value.set(quote.id, {
    quote: deletedQuote,
    index: quoteIndex
  })

  // Show undo notification
  const notification = ElNotification({
    title: '删除成功',
    message: '金句已删除，5秒内可撤销',
    type: 'success',
    duration: 5000,
    position: 'top-right',
    customClass: 'delete-notification',
    onClick: () => {
      undoDelete(quote.id)
      notification.close()
    }
  })

  // Set timer for actual delete (5 seconds)
  const timer = setTimeout(() => {
    if (deletedQuotes.value.has(quote.id)) {
      performDelete(quote.id)
    }
  }, 5000)

  deleteTimers.value.set(quote.id, timer)
}

// Undo delete
function undoDelete(quoteId) {
  const deleted = deletedQuotes.value.get(quoteId)
  if (!deleted) return

  // Clear the timer
  const timer = deleteTimers.value.get(quoteId)
  if (timer) {
    clearTimeout(timer)
    deleteTimers.value.delete(quoteId)
  }

  // Restore the quote
  quotes.value.splice(deleted.index, 0, deleted.quote)
  total.value += 1

  // Remove from deleted map
  deletedQuotes.value.delete(quoteId)

  ElMessage.success('已恢复金句')
}

// Perform actual delete from server
async function performDelete(quoteId) {
  try {
    await quoteApi.deleteQuote(quoteId)

    // Clean up
    deletedQuotes.value.delete(quoteId)
    const timer = deleteTimers.value.get(quoteId)
    if (timer) {
      clearTimeout(timer)
      deleteTimers.value.delete(quoteId)
    }
  } catch (error) {
    console.error('Failed to delete quote:', error)
    // Restore the quote on error
    const deleted = deletedQuotes.value.get(quoteId)
    if (deleted) {
      quotes.value.splice(deleted.index, 0, deleted.quote)
      total.value += 1
      deletedQuotes.value.delete(quoteId)
    }
    ElMessage.error('删除失败，已恢复金句')
  }
}

// Share quote
function handleShare(quote) {
  shareQuoteId.value = quote.id
  shareDialogVisible.value = true
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Crimson+Pro:wght@400;600;700&family=Nunito:wght@400;500;600;700&display=swap');

.quotes-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #faf8f5 0%, #f5f1eb 100%);
  padding: 2rem;
  font-family: 'Nunito', sans-serif;
}

/* Header */
.page-header {
  max-width: 1200px;
  margin: 0 auto 3rem;
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

/* Featured Quote */
.featured-quote-section {
  max-width: 1200px;
  margin: 0 auto 3rem;
}

.featured-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  font-weight: 700;
  color: #92400e;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 1rem;
}

.featured-label svg {
  color: #fbbf24;
  fill: #fef3c7;
}

.featured-quote-card {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 20px;
  padding: 3rem;
  position: relative;
  box-shadow: 0 8px 24px rgba(251, 191, 36, 0.15);
  overflow: hidden;
}

.quote-decoration {
  position: absolute;
  top: 1rem;
  left: 2rem;
  font-size: 6rem;
  color: rgba(251, 191, 36, 0.3);
  font-family: 'Crimson Pro', serif;
  line-height: 1;
}

.featured-content {
  font-family: 'Crimson Pro', serif;
  font-size: 1.75rem;
  line-height: 1.8;
  color: #78350f;
  margin-bottom: 1.5rem;
  position: relative;
  z-index: 1;
  font-style: italic;
}

.featured-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  color: #92400e;
  font-weight: 600;
}

.featured-meta .book-title {
  color: #78350f;
}

.meta-divider {
  opacity: 0.5;
}

/* Controls Bar */
.controls-bar {
  max-width: 1200px;
  margin: 0 auto 2rem;
  display: flex;
  gap: 1.5rem;
  align-items: flex-end;
}

.search-wrapper {
  flex: 1;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 1.25rem;
  top: 1rem;
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

.filters-group {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.color-filter-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.color-filter-wrapper svg {
  position: absolute;
  left: 1rem;
  color: #8b6f47;
  opacity: 0.6;
}

.color-select {
  padding: 1rem 2rem 1rem 2.75rem;
  border: 2px solid #e8ddd0;
  border-radius: 12px;
  font-family: 'Nunito', sans-serif;
  font-size: 1rem;
  background: white;
  color: #2c1810;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 160px;
}

.color-select:focus {
  outline: none;
  border-color: #8b6f47;
}

.tag-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.tag-input-wrapper svg {
  position: absolute;
  left: 1rem;
  color: #8b6f47;
  opacity: 0.6;
}

.tag-input {
  padding: 1rem 1rem 1rem 2.75rem;
  border: 2px solid #e8ddd0;
  border-radius: 12px;
  font-family: 'Nunito', sans-serif;
  font-size: 1rem;
  background: white;
  color: #2c1810;
  transition: all 0.3s ease;
  min-width: 200px;
}

.tag-input:focus {
  outline: none;
  border-color: #8b6f47;
  box-shadow: 0 0 0 4px rgba(139, 111, 71, 0.1);
}

/* Quotes Grid */
.quotes-grid {
  max-width: 1200px;
  margin: 0 auto 3rem;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 1.5rem;
}

.quote-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(44, 24, 16, 0.08);
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  position: relative;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.quote-card:hover {
  box-shadow: 0 8px 24px rgba(44, 24, 16, 0.12);
  transform: translateY(-2px);
}

.color-marker {
  height: 4px;
  width: 100%;
}

.quote-content {
  padding: 1.75rem;
}

.quote-text {
  font-family: 'Crimson Pro', serif;
  font-size: 1.125rem;
  line-height: 1.8;
  color: #2c1810;
  margin-bottom: 1.25rem;
  font-style: italic;
}

.quote-meta {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.book-info {
  font-size: 0.9375rem;
  color: #6b5344;
  font-weight: 500;
}

.book-name {
  color: #8b6f47;
  font-weight: 600;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #6b5344;
}

.location-info svg {
  flex-shrink: 0;
  opacity: 0.7;
}

.quote-note {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #faf8f5;
  border-radius: 8px;
  font-size: 0.875rem;
  color: #6b5344;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.quote-note svg {
  flex-shrink: 0;
  opacity: 0.7;
  margin-top: 0.125rem;
}

.tags-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tags-wrapper svg {
  flex-shrink: 0;
  opacity: 0.7;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-item {
  padding: 0.25rem 0.75rem;
  background: #f5f1eb;
  border-radius: 12px;
  font-size: 0.8125rem;
  color: #6b5344;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tag-item:hover {
  background: #e8ddd0;
  color: #2c1810;
}

.quote-time {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #9ca3af;
  padding-top: 1rem;
  border-top: 1px solid #f5f1eb;
}

.quote-time svg {
  opacity: 0.7;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
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
  margin: 0;
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
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
  max-width: 1200px;
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

/* Quote Footer & Actions */
.quote-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(139, 111, 71, 0.1);
}

.quote-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  color: #8b6f47;
}

.action-btn:hover {
  background: rgba(139, 111, 71, 0.1);
  transform: translateY(-1px);
}

.edit-btn:hover {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.share-btn:hover {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.delete-btn:hover {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

/* Color Picker */
.color-picker {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.color-option {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-option:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.color-option.active {
  border-color: #2c1810;
  box-shadow: 0 0 0 2px rgba(44, 24, 16, 0.2);
}

/* Responsive */
@media (max-width: 768px) {
  .controls-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filters-group {
    flex-direction: column;
  }

  .color-select,
  .tag-input {
    width: 100%;
  }

  .quotes-grid {
    grid-template-columns: 1fr;
  }

  .featured-quote-card {
    padding: 2rem;
  }

  .featured-content {
    font-size: 1.5rem;
  }
}
</style>