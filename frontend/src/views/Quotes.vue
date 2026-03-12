<template>
  <div class="quotes">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>金句收藏</span>
          <el-button type="primary" @click="loadRandomQuote">
            <el-icon><Refresh /></el-icon>
            随机金句
          </el-button>
        </div>
      </template>

      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索金句..."
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

      <!-- Random Quote -->
      <el-card v-if="randomQuote" class="random-quote" shadow="hover">
        <div class="quote-label">每日金句</div>
        <div class="quote-text">"{{ randomQuote.content }}"</div>
        <div class="quote-book">—— 《{{ randomQuote.bookTitle }}》</div>
      </el-card>

      <el-divider v-if="randomQuote" />

      <div v-if="quotes.length === 0" class="empty-text">暂无金句</div>

      <div v-else class="quote-list">
        <el-card v-for="quote in quotes" :key="quote.id" class="quote-card">
          <div class="quote-text">"{{ quote.content }}"</div>
          <div class="quote-footer">
            <div class="quote-book">《{{ quote.bookTitle }}》</div>
            <div class="quote-meta">
              <span v-if="quote.chapter">章节: {{ quote.chapter }}</span>
              <span v-if="quote.pageNumber">页码: {{ quote.pageNumber }}</span>
            </div>
          </div>
          <div v-if="quote.note" class="quote-note">备注: {{ quote.note }}</div>
        </el-card>
      </div>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadQuotes"
        @current-change="loadQuotes"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { quoteApi } from '@/api/modules'

const quotes = ref([])
const randomQuote = ref(null)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

onMounted(() => {
  loadQuotes()
  loadRandomQuote()
})

async function loadQuotes() {
  try {
    const response = await quoteApi.searchQuotes(searchKeyword.value || '', {
      page: currentPage.value - 1,
      size: pageSize.value
    })
    quotes.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('Failed to load quotes:', error)
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
</script>

<style scoped>
.quotes {
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

.random-quote {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin-bottom: 20px;
}

.quote-label {
  font-size: 12px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.random-quote .quote-text {
  font-size: 18px;
  line-height: 1.8;
  margin-bottom: 15px;
  font-style: italic;
}

.random-quote .quote-book {
  text-align: right;
  opacity: 0.9;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.quote-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.quote-card {
  margin: 0;
}

.quote-card .quote-text {
  font-size: 15px;
  line-height: 1.8;
  color: #606266;
  margin-bottom: 15px;
  font-style: italic;
}

.quote-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.quote-book {
  color: #409eff;
  font-weight: 500;
}

.quote-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 15px;
}

.quote-note {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
}
</style>