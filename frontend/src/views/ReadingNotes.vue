<template>
  <div class="reading-notes-page">
    <!-- Header -->
    <header class="page-header">
      <div class="header-content">
        <h1 class="page-title">读书感悟</h1>
        <p class="page-subtitle">记录思想的足迹</p>
      </div>
    </header>

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
          placeholder="搜索感悟内容..."
          class="search-input"
          @keyup.enter="handleSearch"
        />
      </div>

      <!-- Filters -->
      <div class="filters-group">
        <select v-model="filterType" class="filter-select" @change="handleFilter">
          <option value="">全部类型</option>
          <option value="THOUGHT">💭 感悟</option>
          <option value="SUMMARY">📝 总结</option>
          <option value="QUESTION">❓ 疑问</option>
          <option value="INSIGHT">💡 洞察</option>
        </select>

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

    <!-- Notes List -->
    <div v-if="loading" class="loading-state">
      <div class="loader"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="notes.length === 0" class="empty-state">
      <div class="empty-icon">📝</div>
      <h3>还没有感悟</h3>
      <p>在书籍详情页添加你的第一个感悟吧</p>
    </div>

    <div v-else class="notes-list">
      <div
        v-for="(note, index) in notes"
        :key="note.id"
        class="note-card"
        :style="{ animationDelay: `${index * 0.05}s` }"
      >
        <!-- Card Header -->
        <div class="note-header">
          <div class="note-meta-primary">
            <h3 class="note-title">{{ note.title || '无标题感悟' }}</h3>
            <div class="note-book-info">
              <span class="book-label">《</span>
              <span class="book-name">{{ note.bookTitle || '未知书籍' }}</span>
              <span class="book-label">》</span>
            </div>
          </div>
          <span :class="['note-type-badge', `type-${note.noteType.toLowerCase()}`]">
            {{ getNoteTypeIcon(note.noteType) }} {{ getNoteTypeText(note.noteType) }}
          </span>
        </div>

        <!-- Card Content -->
        <div class="note-content-section">
          <div
            :class="['note-content', { 'is-expanded': expandedNotes.includes(note.id) }]"
            :ref="el => setContentRef(note.id, el)"
          >
            {{ note.content }}
          </div>

          <button
            v-if="shouldShowExpandButton(note.id, note.content)"
            class="expand-btn"
            @click="toggleExpand(note.id)"
          >
            <span>{{ expandedNotes.includes(note.id) ? '收起' : '展开全文' }}</span>
            <svg
              :class="['expand-icon', { 'is-expanded': expandedNotes.includes(note.id) }]"
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </button>
        </div>

        <!-- Card Footer -->
        <div class="note-footer">
          <div class="note-details">
            <div v-if="note.chapter || note.pageNumber" class="detail-item">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
              </svg>
              <span v-if="note.chapter">第{{ note.chapter }}章</span>
              <span v-if="note.chapter && note.pageNumber"> · </span>
              <span v-if="note.pageNumber">第{{ note.pageNumber }}页</span>
            </div>

            <div v-if="note.tags" class="tags-wrapper">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path>
                <line x1="7" y1="7" x2="7.01" y2="7"></line>
              </svg>
              <div class="tags-list">
                <span
                  v-for="tag in parseTags(note.tags)"
                  :key="tag"
                  class="tag-item"
                  @click="filterByTag(tag)"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>

          <div class="footer-right">
            <div class="note-actions">
              <button class="action-btn edit-btn" @click="handleEdit(note)" title="编辑感悟">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button class="action-btn delete-btn" @click="handleDelete(note)" title="删除感悟">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>

            <div class="note-time">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <span>{{ formatDate(note.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="notes.length > 0" class="pagination">
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

    <!-- Edit Note Dialog -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑感悟"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入感悟标题" />
        </el-form-item>

        <el-form-item label="类型" prop="noteType">
          <el-select v-model="editForm.noteType" placeholder="选择感悟类型">
            <el-option label="💭 感悟" value="THOUGHT" />
            <el-option label="📝 总结" value="SUMMARY" />
            <el-option label="❓ 疑问" value="QUESTION" />
            <el-option label="💡 洞察" value="INSIGHT" />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入感悟内容"
          />
        </el-form-item>

        <el-form-item label="章节">
          <el-input v-model="editForm.chapter" placeholder="例如：第三章" />
        </el-form-item>

        <el-form-item label="页码">
          <el-input-number v-model="editForm.pageNumber" :min="1" />
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="editForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>

        <el-form-item label="私密">
          <el-switch v-model="editForm.isPrivate" />
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { readingNoteApi } from '@/api/modules'
import { ElMessage, ElNotification } from 'element-plus'

const loading = ref(false)
const notes = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const searchKeyword = ref('')
const filterType = ref('')
const filterTag = ref('')

const expandedNotes = ref([])
const contentRefs = ref({})

// Edit note dialog
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editLoading = ref(false)
const editForm = ref({
  id: null,
  title: '',
  content: '',
  noteType: 'THOUGHT',
  chapter: '',
  pageNumber: null,
  tags: '',
  isPrivate: false
})

const editRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  noteType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

// Undo delete
const deletedNotes = ref(new Map()) // Store deleted notes for undo
const deleteTimers = ref(new Map()) // Store delete timers

function setContentRef(id, el) {
  if (el) {
    contentRefs.value[id] = el
  }
}

onMounted(() => {
  loadNotes()
})

async function loadNotes() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      noteType: filterType.value || undefined,
      tag: filterTag.value || undefined
    }

    const response = await readingNoteApi.searchNotes(searchKeyword.value || '', params)
    notes.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('Failed to load notes:', error)
    ElMessage.error('加载感悟失败')
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  currentPage.value = 1
  await loadNotes()
}

async function handleFilter() {
  currentPage.value = 1
  await loadNotes()
}

function filterByTag(tag) {
  filterTag.value = tag
  handleFilter()
}

async function changePage(page) {
  currentPage.value = page
  await loadNotes()
  // Scroll to top
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function shouldShowExpandButton(noteId, content) {
  if (!content) return false

  // Check if content is long enough (> 150 chars or contains newlines)
  if (content.length > 150 || content.includes('\n')) {
    return true
  }

  // Check actual rendered height
  nextTick(() => {
    const el = contentRefs.value[noteId]
    if (el && el.scrollHeight > 120) {
      return true
    }
  })

  return content.length > 150
}

function toggleExpand(noteId) {
  const index = expandedNotes.value.indexOf(noteId)
  if (index > -1) {
    expandedNotes.value.splice(index, 1)
  } else {
    expandedNotes.value.push(noteId)
  }
}

function parseTags(tags) {
  if (!tags) return []
  return tags.split(',').map(tag => tag.trim()).filter(tag => tag)
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

function getNoteTypeIcon(type) {
  const icons = {
    THOUGHT: '💭',
    SUMMARY: '📝',
    QUESTION: '❓',
    INSIGHT: '💡'
  }
  return icons[type] || '📝'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  // Less than 1 day
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    if (hours < 1) return '刚刚'
    if (hours < 24) return `${hours}小时前`
  }

  // Less than 7 days
  if (diff < 604800000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  }

  // Otherwise show date
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// Edit note
function handleEdit(note) {
  editForm.value = {
    id: note.id,
    title: note.title || '',
    content: note.content || '',
    noteType: note.noteType || 'THOUGHT',
    chapter: note.chapter || '',
    pageNumber: note.pageNumber || null,
    tags: note.tags || '',
    isPrivate: note.isPrivate || false
  }
  editDialogVisible.value = true
}

async function submitEdit() {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await readingNoteApi.updateNote(editForm.value.id, editForm.value)
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        await loadNotes()
      } catch (error) {
        console.error('Failed to update note:', error)
        ElMessage.error('更新失败')
      } finally {
        editLoading.value = false
      }
    }
  })
}

// Delete note
async function handleDelete(note) {
  // Store the note for potential undo
  const noteIndex = notes.value.findIndex(n => n.id === note.id)
  if (noteIndex === -1) return

  const deletedNote = notes.value[noteIndex]

  // Remove from UI immediately
  notes.value.splice(noteIndex, 1)
  total.value -= 1

  // Store the note for undo
  deletedNotes.value.set(note.id, {
    note: deletedNote,
    index: noteIndex
  })

  // Show undo notification
  const notification = ElNotification({
    title: '删除成功',
    message: '感悟已删除，5秒内可撤销',
    type: 'success',
    duration: 5000,
    position: 'top-right',
    customClass: 'delete-notification',
    dangerouslyUseHTMLString: true,
    onClick: () => {
      undoDelete(note.id)
      notification.close()
    }
  })

  // Set timer for actual delete (5 seconds)
  const timer = setTimeout(() => {
    if (deletedNotes.value.has(note.id)) {
      performDelete(note.id)
    }
  }, 5000)

  deleteTimers.value.set(note.id, timer)
}

// Undo delete
function undoDelete(noteId) {
  const deleted = deletedNotes.value.get(noteId)
  if (!deleted) return

  // Clear the timer
  const timer = deleteTimers.value.get(noteId)
  if (timer) {
    clearTimeout(timer)
    deleteTimers.value.delete(noteId)
  }

  // Restore the note
  notes.value.splice(deleted.index, 0, deleted.note)
  total.value += 1

  // Remove from deleted map
  deletedNotes.value.delete(noteId)

  ElMessage.success('已恢复感悟')
}

// Perform actual delete from server
async function performDelete(noteId) {
  try {
    await readingNoteApi.deleteNote(noteId)

    // Clean up
    deletedNotes.value.delete(noteId)
    const timer = deleteTimers.value.get(noteId)
    if (timer) {
      clearTimeout(timer)
      deleteTimers.value.delete(noteId)
    }
  } catch (error) {
    console.error('Failed to delete note:', error)
    // Restore the note on error
    const deleted = deletedNotes.value.get(noteId)
    if (deleted) {
      notes.value.splice(deleted.index, 0, deleted.note)
      total.value += 1
      deletedNotes.value.delete(noteId)
    }
    ElMessage.error('删除失败，已恢复感悟')
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Crimson+Pro:wght@400;600;700&family=Nunito:wght@400;500;600;700&display=swap');

.reading-notes-page {
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
  min-width: 160px;
}

.filter-select:focus {
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

/* Notes List */
.notes-list {
  max-width: 1200px;
  margin: 0 auto 3rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.note-card {
  background: white;
  border-radius: 16px;
  padding: 1.75rem;
  box-shadow: 0 2px 12px rgba(44, 24, 16, 0.08);
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
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

.note-card:hover {
  box-shadow: 0 8px 24px rgba(44, 24, 16, 0.12);
  transform: translateY(-2px);
}

/* Note Header */
.note-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.25rem;
}

.note-meta-primary {
  flex: 1;
}

.note-title {
  font-family: 'Crimson Pro', serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #2c1810;
  margin: 0 0 0.5rem;
  line-height: 1.3;
}

.note-book-info {
  font-size: 0.9375rem;
  color: #6b5344;
  font-weight: 500;
}

.book-name {
  color: #8b6f47;
  font-weight: 600;
}

.note-type-badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  white-space: nowrap;
}

.type-thought {
  background: #fef3c7;
  color: #92400e;
}

.type-summary {
  background: #d1fae5;
  color: #065f46;
}

.type-question {
  background: #dbeafe;
  color: #1e40af;
}

.type-insight {
  background: #fce7f3;
  color: #9f1239;
}

/* Note Content */
.note-content-section {
  margin-bottom: 1.25rem;
}

.note-content {
  line-height: 1.8;
  color: #374151;
  max-height: 6.5rem;
  overflow: hidden;
  position: relative;
  transition: max-height 0.3s ease;
}

.note-content.is-expanded {
  max-height: none;
}

.note-content:not(.is-expanded)::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2rem;
  background: linear-gradient(to bottom, transparent, white);
}

.expand-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0;
  background: none;
  border: none;
  color: #8b6f47;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 0.9375rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.expand-btn:hover {
  color: #6b5344;
}

.expand-icon {
  transition: transform 0.3s ease;
}

.expand-icon.is-expanded {
  transform: rotate(180deg);
}

/* Note Footer */
.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #f5f1eb;
  gap: 1rem;
}

.note-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  flex: 1;
}

.detail-item,
.tags-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #6b5344;
}

.detail-item svg,
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

.note-time {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #9ca3af;
  white-space: nowrap;
}

.note-time svg {
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

/* Note Actions */
.footer-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.note-actions {
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

.delete-btn:hover {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
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

  .filter-select,
  .tag-input {
    width: 100%;
  }

  .note-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .note-time {
    width: 100%;
  }
}
</style>