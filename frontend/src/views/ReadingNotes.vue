<template>
  <div class="reading-notes">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>读书感悟</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索感悟..."
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <div v-if="notes.length === 0" class="empty-text">暂无感悟</div>

      <div v-else class="note-list">
        <el-card v-for="note in notes" :key="note.id" class="note-card">
          <div class="note-header">
            <div>
              <h3>{{ note.title || '无标题' }}</h3>
              <div class="note-book">《{{ note.bookTitle }}》</div>
            </div>
            <el-tag :type="getNoteTypeColor(note.noteType)">
              {{ getNoteTypeText(note.noteType) }}
            </el-tag>
          </div>

          <div class="note-content">{{ note.content }}</div>

          <div class="note-footer">
            <div class="note-meta">
              <span v-if="note.chapter">章节: {{ note.chapter }}</span>
              <span v-if="note.pageNumber">页码: {{ note.pageNumber }}</span>
            </div>
            <div class="note-date">{{ formatDate(note.createdAt) }}</div>
          </div>
        </el-card>
      </div>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadNotes"
        @current-change="loadNotes"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { readingNoteApi } from '@/api/modules'

const notes = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

onMounted(() => {
  loadNotes()
})

async function loadNotes() {
  try {
    const response = await readingNoteApi.searchNotes(searchKeyword.value || '', {
      page: currentPage.value - 1,
      size: pageSize.value
    })
    notes.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('Failed to load notes:', error)
  }
}

async function handleSearch() {
  currentPage.value = 1
  await loadNotes()
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

function getNoteTypeColor(type) {
  const colors = {
    THOUGHT: '',
    SUMMARY: 'success',
    QUESTION: 'warning',
    INSIGHT: 'info'
  }
  return colors[type] || ''
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.reading-notes {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-bar {
  margin-bottom: 20px;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.note-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.note-card {
  margin: 0;
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.note-header h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.note-book {
  font-size: 13px;
  color: #909399;
}

.note-content {
  line-height: 1.8;
  color: #606266;
  margin-bottom: 15px;
}

.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.note-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 15px;
}

.note-date {
  font-size: 12px;
  color: #909399;
}
</style>