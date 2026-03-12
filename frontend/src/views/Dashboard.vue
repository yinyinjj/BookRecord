<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statistics" :key="stat.label">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近阅读</span>
              <el-button type="primary" text @click="$router.push('/bookshelf')">
                查看全部
              </el-button>
            </div>
          </template>
          <div v-if="recentBooks.length === 0" class="empty-text">
            暂无阅读记录
          </div>
          <div v-else class="book-list">
            <div v-for="book in recentBooks" :key="book.id" class="book-item">
              <div class="book-info">
                <h4>{{ book.title }}</h4>
                <p>{{ book.author }}</p>
              </div>
              <el-tag :type="getStatusType(book.readingStatus)">
                {{ getStatusText(book.readingStatus) }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>每日金句</span>
              <el-button type="primary" text @click="loadRandomQuote">
                换一条
              </el-button>
            </div>
          </template>
          <div v-if="!randomQuote" class="empty-text">
            暂无金句
          </div>
          <div v-else class="quote-content">
            <p class="quote-text">"{{ randomQuote.content }}"</p>
            <div class="quote-book">—— 《{{ randomQuote.bookTitle }}》</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { bookApi, quoteApi } from '@/api/modules'

const statistics = ref([
  { label: '总藏书', value: 0 },
  { label: '在读', value: 0 },
  { label: '已完成', value: 0 },
  { label: '金句数', value: 0 }
])

const recentBooks = ref([])
const randomQuote = ref(null)

onMounted(() => {
  loadStatistics()
  loadRecentBooks()
  loadRandomQuote()
})

async function loadStatistics() {
  try {
    const response = await bookApi.getStatistics()
    statistics.value[0].value = response.data.totalBooks
    statistics.value[1].value = response.data.reading
    statistics.value[2].value = response.data.completed
    // TODO: Load quotes count
  } catch (error) {
    console.error('Failed to load statistics:', error)
  }
}

async function loadRecentBooks() {
  try {
    const response = await bookApi.getBooks({ page: 0, size: 5, sortBy: 'updatedAt', sortDir: 'desc' })
    recentBooks.value = response.data.content
  } catch (error) {
    console.error('Failed to load recent books:', error)
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
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 10px 0;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

.book-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.book-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.book-info h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
}

.book-info p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.quote-content {
  padding: 10px 0;
}

.quote-text {
  font-style: italic;
  line-height: 1.8;
  color: #606266;
  margin: 0 0 15px 0;
}

.quote-book {
  text-align: right;
  color: #909399;
  font-size: 14px;
}
</style>