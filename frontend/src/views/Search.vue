<template>
  <div class="search-page">
    <!-- Decorative Elements -->
    <div class="decorative-corner top-left"></div>
    <div class="decorative-corner top-right"></div>

    <!-- Header -->
    <header class="search-header">
      <h1 class="page-title">Discover</h1>
      <p class="page-subtitle">Search through your literary journey</p>
    </header>

    <!-- Search Section -->
    <div class="search-section">
      <div class="search-container">
        <div class="search-icon-wrapper">
          <svg class="search-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
        </div>
        <input
          ref="searchInput"
          v-model="searchQuery"
          type="text"
          class="search-input"
          placeholder="Search books, notes, quotes..."
          @keyup.enter="performSearch"
          @input="debouncedSearch"
        />
        <button v-if="searchQuery" class="clear-btn" @click="clearSearch">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
        <!-- Advanced Search Toggle -->
        <button
          :class="['advanced-toggle-btn', { active: showAdvancedFilters }]"
          @click="toggleAdvancedFilters"
          title="Advanced Search"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
          </svg>
        </button>
      </div>
    </div>

    <!-- Advanced Filters Panel -->
    <transition name="slide-fade">
      <div v-if="showAdvancedFilters" class="advanced-filters-panel">
        <div class="filters-inner">
          <!-- Filter Header -->
          <div class="filters-header">
            <div class="filters-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="4" y1="21" x2="4" y2="14"></line>
                <line x1="4" y1="10" x2="4" y2="3"></line>
                <line x1="12" y1="21" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12" y2="3"></line>
                <line x1="20" y1="21" x2="20" y2="16"></line>
                <line x1="20" y1="12" x2="20" y2="3"></line>
                <line x1="1" y1="14" x2="7" y2="14"></line>
                <line x1="9" y1="8" x2="15" y2="8"></line>
                <line x1="17" y1="16" x2="23" y2="16"></line>
              </svg>
              <span>Refine Your Search</span>
            </div>
            <button class="close-filters-btn" @click="showAdvancedFilters = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>

          <!-- Filters Grid -->
          <div class="filters-grid">
            <!-- Time Range -->
            <div class="filter-group">
              <label class="filter-label">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
                Time Range
              </label>
              <div class="date-range-picker">
                <input
                  v-model="filters.startDate"
                  type="date"
                  class="date-input"
                  placeholder="Start"
                />
                <span class="date-separator">—</span>
                <input
                  v-model="filters.endDate"
                  type="date"
                  class="date-input"
                  placeholder="End"
                />
              </div>
            </div>

            <!-- Search Type -->
            <div class="filter-group">
              <label class="filter-label">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8"></circle>
                  <path d="m21 21-4.35-4.35"></path>
                </svg>
                Search In
              </label>
              <div class="filter-chips">
                <button
                  v-for="type in searchTypes"
                  :key="type.value"
                  :class="['filter-chip', { active: filters.type === type.value }]"
                  @click="filters.type = type.value"
                >
                  <span class="chip-icon">{{ type.icon }}</span>
                  {{ type.label }}
                </button>
              </div>
            </div>

            <!-- Reading Status (for Books) -->
            <div class="filter-group" v-if="filters.type === 'all' || filters.type === 'books'">
              <label class="filter-label">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
                </svg>
                Reading Status
              </label>
              <div class="filter-chips">
                <button
                  v-for="status in readingStatuses"
                  :key="status.value"
                  :class="['filter-chip', { active: filters.readingStatuses.includes(status.value) }]"
                  @click="toggleFilter('readingStatuses', status.value)"
                >
                  <span class="chip-dot" :style="{ backgroundColor: status.color }"></span>
                  {{ status.label }}
                </button>
              </div>
            </div>

            <!-- Note Types (for Notes) -->
            <div class="filter-group" v-if="filters.type === 'all' || filters.type === 'notes'">
              <label class="filter-label">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                  <polyline points="14 2 14 8 20 8"></polyline>
                </svg>
                Note Types
              </label>
              <div class="filter-chips">
                <button
                  v-for="noteType in noteTypes"
                  :key="noteType.value"
                  :class="['filter-chip', { active: filters.noteTypes.includes(noteType.value) }]"
                  @click="toggleFilter('noteTypes', noteType.value)"
                >
                  <span class="chip-icon">{{ noteType.icon }}</span>
                  {{ noteType.label }}
                </button>
              </div>
            </div>

            <!-- Quote Colors (for Quotes) -->
            <div class="filter-group" v-if="filters.type === 'all' || filters.type === 'quotes'">
              <label class="filter-label">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"></path>
                </svg>
                Quote Colors
              </label>
              <div class="filter-chips color-chips">
                <button
                  v-for="color in quoteColors"
                  :key="color.value"
                  :class="['color-chip', { active: filters.colors.includes(color.value) }]"
                  :style="{ '--chip-color': color.hex }"
                  @click="toggleFilter('colors', color.value)"
                  :title="color.label"
                >
                  <span class="color-dot"></span>
                </button>
              </div>
            </div>
          </div>

          <!-- Filter Actions -->
          <div class="filters-actions">
            <button class="reset-btn" @click="resetFilters">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
                <path d="M3 3v5h5"></path>
              </svg>
              Reset All
            </button>
            <button class="apply-btn" @click="applyFilters">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              Apply Filters
            </button>
          </div>

          <!-- Active Filters Summary -->
          <div v-if="activeFiltersCount > 0" class="active-filters-summary">
            <span class="active-count">{{ activeFiltersCount }} filter{{ activeFiltersCount !== 1 ? 's' : '' }} active</span>
          </div>
        </div>
      </div>
    </transition>

    <!-- Tabs -->
    <div class="tabs-section">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        :class="['tab-btn', { active: activeTab === tab.value }]"
        @click="activeTab = tab.value"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-label">{{ tab.label }}</span>
        <span v-if="getTabCount(tab.value) > 0" class="tab-count">{{ getTabCount(tab.value) }}</span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loader">
        <div class="loader-ring"></div>
        <div class="loader-ring"></div>
        <div class="loader-ring"></div>
      </div>
      <p>Searching your library...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="!hasSearched" class="empty-state initial">
      <div class="empty-illustration">
        <svg width="120" height="120" viewBox="0 0 120 120" fill="none">
          <circle cx="60" cy="60" r="50" stroke="#d4c4a8" stroke-width="2" stroke-dasharray="8 4"/>
          <path d="M45 45 L75 75 M75 45 L45 75" stroke="#8b7355" stroke-width="3" stroke-linecap="round"/>
          <circle cx="60" cy="60" r="8" fill="#c9b896"/>
        </svg>
      </div>
      <h3>What will you discover?</h3>
      <p>Search through your books, notes, and quotes</p>
    </div>

    <!-- No Results State -->
    <div v-else-if="totalResults === 0" class="empty-state no-results">
      <div class="empty-illustration">
        <svg width="120" height="120" viewBox="0 0 120 120" fill="none">
          <rect x="25" y="35" width="70" height="50" rx="4" stroke="#d4c4a8" stroke-width="2"/>
          <path d="M40 50 L80 50 M40 60 L65 60 M40 70 L55 70" stroke="#c9b896" stroke-width="2" stroke-linecap="round"/>
          <circle cx="85" cy="75" r="15" fill="#f5efe0" stroke="#d4c4a8" stroke-width="2"/>
          <path d="M80 75 L90 75 M85 70 L85 80" stroke="#8b7355" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </div>
      <h3>No matches found</h3>
      <p>Try different keywords or adjust your filters</p>
    </div>

    <!-- Results -->
    <div v-else class="results-section">
      <!-- Results Summary -->
      <div class="results-summary">
        Found <strong>{{ totalResults }}</strong> result{{ totalResults !== 1 ? 's' : '' }}
        <span v-if="searchQuery"> for "<em>{{ searchQuery }}</em>"</span>
        <span v-if="activeFiltersCount > 0" class="filtered-badge">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
          </svg>
          Filtered
        </span>
      </div>

      <!-- Books Results -->
      <section v-if="showSection('books') && filteredResults.books.length > 0" class="results-group">
        <div class="group-header">
          <div class="group-icon books-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
            </svg>
          </div>
          <h2 class="group-title">Books</h2>
          <span class="group-count">{{ filteredResults.books.length }}</span>
        </div>
        <div class="books-grid">
          <div
            v-for="(book, index) in filteredResults.books"
            :key="book.id"
            class="book-card"
            :style="{ animationDelay: `${index * 0.08}s` }"
            @click="navigateToBook(book.id)"
          >
            <div class="book-cover-placeholder">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
              </svg>
            </div>
            <div class="book-info">
              <h3 class="book-title" v-html="book.highlightedTitle || book.title"></h3>
              <p class="book-author" v-html="book.highlightedAuthor || book.author"></p>
              <div class="book-meta">
                <span :class="['status-tag', `status-${book.readingStatus?.toLowerCase()}`]">
                  {{ getStatusLabel(book.readingStatus) }}
                </span>
                <div v-if="book.progress !== null && book.progress !== undefined" class="progress-wrapper">
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: `${book.progress}%` }"></div>
                  </div>
                  <span class="progress-text">{{ book.progress }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Notes Results -->
      <section v-if="showSection('notes') && filteredResults.notes.length > 0" class="results-group">
        <div class="group-header">
          <div class="group-icon notes-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
              <polyline points="10 9 9 9 8 9"></polyline>
            </svg>
          </div>
          <h2 class="group-title">Notes</h2>
          <span class="group-count">{{ filteredResults.notes.length }}</span>
        </div>
        <div class="notes-list">
          <div
            v-for="(note, index) in filteredResults.notes"
            :key="note.id"
            class="note-card"
            :style="{ animationDelay: `${index * 0.08}s` }"
            @click="navigateToBook(note.bookId)"
          >
            <div class="note-header">
              <h3 class="note-title" v-html="note.highlightedTitle || note.title || 'Untitled Note'"></h3>
              <span :class="['type-badge', `type-${note.noteType?.toLowerCase()}`]">
                {{ getTypeLabel(note.noteType) }}
              </span>
            </div>
            <p class="note-content" v-html="note.highlightedContent || note.content"></p>
            <div class="note-footer">
              <span class="book-source">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
                </svg>
                {{ note.bookTitle }}
              </span>
            </div>
          </div>
        </div>
      </section>

      <!-- Quotes Results -->
      <section v-if="showSection('quotes') && filteredResults.quotes.length > 0" class="results-group">
        <div class="group-header">
          <div class="group-icon quotes-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 21c3 0 7-1 7-8V5c0-1.25-.756-2.017-2-2H4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2 1 0 1 0 1 1v1c0 1-1 2-2 2s-1 .008-1 1.031V21"></path>
              <path d="M15 21c3 0 7-1 7-8V5c0-1.25-.757-2.017-2-2h-4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2h.75c0 2.25.25 4-2.75 4v3"></path>
            </svg>
          </div>
          <h2 class="group-title">Quotes</h2>
          <span class="group-count">{{ filteredResults.quotes.length }}</span>
        </div>
        <div class="quotes-grid">
          <div
            v-for="(quote, index) in filteredResults.quotes"
            :key="quote.id"
            class="quote-card"
            :style="{
              animationDelay: `${index * 0.08}s`,
              '--accent-color': getColorHex(quote.color)
            }"
            @click="navigateToBook(quote.bookId)"
          >
            <div class="quote-color-bar" :style="{ backgroundColor: getColorHex(quote.color) }"></div>
            <div class="quote-content">
              <span class="quote-mark">"</span>
              <p class="quote-text" v-html="quote.highlightedContent || quote.content"></p>
              <span class="quote-mark end">"</span>
            </div>
            <div class="quote-footer">
              <span class="book-source">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
                </svg>
                {{ quote.bookTitle }}
              </span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { searchApi } from '@/api/modules'

const router = useRouter()
const route = useRoute()

// ==================== 响应式状态定义 ====================

/** 搜索关键词 */
const searchQuery = ref('')

/** 当前选中的标签页 */
const activeTab = ref('all')

/** 加载状态 */
const loading = ref(false)

/** 是否已执行过搜索 */
const hasSearched = ref(false)

/** 是否显示高级筛选面板 */
const showAdvancedFilters = ref(false)

/** 搜索结果数据 */
const searchResults = ref({
  books: [],
  notes: [],
  quotes: [],
  totalBooks: 0,
  totalNotes: 0,
  totalQuotes: 0,
  totalResults: 0
})

/** 防抖定时器 */
let debounceTimer = null

// ==================== 筛选配置 ====================

/** 标签页配置 */
const tabs = [
  { value: 'all', label: 'All', icon: '✦' },
  { value: 'books', label: 'Books', icon: '📖' },
  { value: 'notes', label: 'Notes', icon: '📝' },
  { value: 'quotes', label: 'Quotes', icon: '💬' }
]

/** 搜索类型配置 */
const searchTypes = [
  { value: 'all', label: 'All', icon: '✦' },
  { value: 'books', label: 'Books', icon: '📖' },
  { value: 'notes', label: 'Notes', icon: '📝' },
  { value: 'quotes', label: 'Quotes', icon: '💬' }
]

/** 阅读状态配置 */
const readingStatuses = [
  { value: 'WANT_TO_READ', label: 'Want to Read', color: '#8b7355' },
  { value: 'READING', label: 'Reading', color: '#f59e0b' },
  { value: 'COMPLETED', label: 'Completed', color: '#10b981' },
  { value: 'ABANDONED', label: 'Abandoned', color: '#6b7280' }
]

/** 感悟类型配置 */
const noteTypes = [
  { value: 'THOUGHT', label: 'Thought', icon: '💭' },
  { value: 'SUMMARY', label: 'Summary', icon: '📋' },
  { value: 'QUESTION', label: 'Question', icon: '❓' },
  { value: 'INSIGHT', label: 'Insight', icon: '💡' }
]

/** 金句颜色配置 */
const quoteColors = [
  { value: 'red', label: 'Red', hex: '#dc2626' },
  { value: 'blue', label: 'Blue', hex: '#2563eb' },
  { value: 'green', label: 'Green', hex: '#16a34a' },
  { value: 'yellow', label: 'Yellow', hex: '#ca8a04' },
  { value: 'purple', label: 'Purple', hex: '#9333ea' },
  { value: 'orange', label: 'Orange', hex: '#ea580c' },
  { value: 'pink', label: 'Pink', hex: '#ec4899' }
]

// ==================== 筛选状态 ====================

/** 筛选条件 */
const filters = reactive({
  type: 'all',
  startDate: '',
  endDate: '',
  readingStatuses: [],
  noteTypes: [],
  colors: []
})

// ==================== 计算属性 ====================

/** 总结果数量 */
const totalResults = computed(() => searchResults.value.totalResults)

/** 根据标签页筛选的结果 */
const filteredResults = computed(() => {
  if (activeTab.value === 'all') {
    return searchResults.value
  }
  return {
    ...searchResults.value,
    books: activeTab.value === 'books' ? searchResults.value.books : [],
    notes: activeTab.value === 'notes' ? searchResults.value.notes : [],
    quotes: activeTab.value === 'quotes' ? searchResults.value.quotes : []
  }
})

/** 活跃筛选条件数量 */
const activeFiltersCount = computed(() => {
  let count = 0
  if (filters.startDate || filters.endDate) count++
  if (filters.readingStatuses.length > 0) count++
  if (filters.noteTypes.length > 0) count++
  if (filters.colors.length > 0) count++
  if (filters.type !== 'all') count++
  return count
})

// ==================== 方法定义 ====================

/**
 * 获取标签页结果数量
 */
function getTabCount(tabValue) {
  if (tabValue === 'all') return searchResults.value.totalResults
  if (tabValue === 'books') return searchResults.value.totalBooks
  if (tabValue === 'notes') return searchResults.value.totalNotes
  if (tabValue === 'quotes') return searchResults.value.totalQuotes
  return 0
}

/**
 * 判断是否显示某个类型的结果
 */
function showSection(type) {
  return activeTab.value === 'all' || activeTab.value === type
}

/**
 * 切换高级筛选面板显示状态
 */
function toggleAdvancedFilters() {
  showAdvancedFilters.value = !showAdvancedFilters.value
}

/**
 * 切换筛选条件
 */
function toggleFilter(filterName, value) {
  const index = filters[filterName].indexOf(value)
  if (index === -1) {
    filters[filterName].push(value)
  } else {
    filters[filterName].splice(index, 1)
  }
}

/**
 * 重置所有筛选条件
 */
function resetFilters() {
  filters.type = 'all'
  filters.startDate = ''
  filters.endDate = ''
  filters.readingStatuses = []
  filters.noteTypes = []
  filters.colors = []
}

/**
 * 应用筛选条件执行高级搜索
 */
async function applyFilters() {
  loading.value = true
  hasSearched.value = true

  try {
    // 构建高级搜索请求参数
    const params = {
      keyword: searchQuery.value.trim() || null,
      type: filters.type,
      startDate: filters.startDate || null,
      endDate: filters.endDate || null,
      readingStatuses: filters.readingStatuses.length > 0 ? filters.readingStatuses : null,
      noteTypes: filters.noteTypes.length > 0 ? filters.noteTypes : null,
      colors: filters.colors.length > 0 ? filters.colors : null,
      page: 0,
      size: 50
    }

    const response = await searchApi.advancedSearch(params)
    searchResults.value = response.data

    // 关闭筛选面板
    showAdvancedFilters.value = false
  } catch (error) {
    console.error('Advanced search failed:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 防抖搜索
 */
function debouncedSearch() {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    if (searchQuery.value.trim().length >= 2) {
      // 如果有筛选条件，使用高级搜索
      if (activeFiltersCount.value > 0) {
        applyFilters()
      } else {
        performSearch()
      }
    } else if (searchQuery.value.trim().length === 0) {
      hasSearched.value = false
      searchResults.value = {
        books: [],
        notes: [],
        quotes: [],
        totalBooks: 0,
        totalNotes: 0,
        totalQuotes: 0,
        totalResults: 0
      }
    }
  }, 300)
}

/**
 * 执行基础搜索
 */
async function performSearch() {
  const keyword = searchQuery.value.trim()
  if (!keyword) return

  loading.value = true
  hasSearched.value = true

  try {
    const response = await searchApi.search(keyword)
    searchResults.value = response.data
  } catch (error) {
    console.error('Search failed:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 清空搜索
 */
function clearSearch() {
  searchQuery.value = ''
  hasSearched.value = false
  searchResults.value = {
    books: [],
    notes: [],
    quotes: [],
    totalBooks: 0,
    totalNotes: 0,
    totalQuotes: 0,
    totalResults: 0
  }
}

/**
 * 跳转到书籍详情页
 */
function navigateToBook(bookId) {
  router.push(`/books/${bookId}`)
}

/**
 * 获取阅读状态标签
 */
function getStatusLabel(status) {
  const labels = {
    WANT_TO_READ: 'Want to Read',
    READING: 'Reading',
    COMPLETED: 'Completed',
    ABANDONED: 'Abandoned'
  }
  return labels[status] || status
}

/**
 * 获取感悟类型标签
 */
function getTypeLabel(type) {
  const labels = {
    THOUGHT: 'Thought',
    SUMMARY: 'Summary',
    QUESTION: 'Question',
    INSIGHT: 'Insight'
  }
  return labels[type] || type
}

/**
 * 获取颜色十六进制值
 */
function getColorHex(color) {
  const colors = {
    red: '#dc2626',
    blue: '#2563eb',
    green: '#16a34a',
    yellow: '#ca8a04',
    purple: '#9333ea',
    orange: '#ea580c',
    pink: '#ec4899'
  }
  return colors[color] || '#8b7355'
}

// ==================== 生命周期钩子 ====================

onMounted(() => {
  // 检查URL查询参数
  if (route.query.q) {
    searchQuery.value = route.query.q
    performSearch()
  }
})

// 监听路由变化
watch(() => route.query.q, (newQuery) => {
  if (newQuery && newQuery !== searchQuery.value) {
    searchQuery.value = newQuery
    performSearch()
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Crimson+Pro:wght@400;500;600;700&family=Nunito:wght@400;500;600;700&display=swap');

.search-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #faf7f2 0%, #f5efe6 100%);
  padding: 2rem;
  font-family: 'Nunito', sans-serif;
  position: relative;
  overflow: hidden;
}

/* Decorative Elements */
.decorative-corner {
  position: absolute;
  width: 200px;
  height: 200px;
  pointer-events: none;
  opacity: 0.15;
}

.decorative-corner.top-left {
  top: 0;
  left: 0;
  background: radial-gradient(circle at 0% 0%, #8b7355 0%, transparent 70%);
}

.decorative-corner.top-right {
  top: 0;
  right: 0;
  background: radial-gradient(circle at 100% 0%, #c9b896 0%, transparent 70%);
}

/* Header */
.search-header {
  max-width: 1000px;
  margin: 0 auto 2rem;
  text-align: center;
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

/* Search Section */
.search-section {
  max-width: 700px;
  margin: 0 auto 2rem;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(44, 24, 16, 0.08), 0 1px 3px rgba(44, 24, 16, 0.06);
  border: 2px solid #e8ddd0;
  transition: all 0.3s ease;
}

.search-container:focus-within {
  border-color: #8b7355;
  box-shadow: 0 8px 30px rgba(139, 115, 85, 0.15);
}

.search-icon-wrapper {
  padding: 0 1rem 0 1.5rem;
  color: #8b7355;
}

.search-icon {
  opacity: 0.7;
}

.search-input {
  flex: 1;
  padding: 1.25rem 0;
  border: none;
  background: transparent;
  font-family: 'Nunito', sans-serif;
  font-size: 1.125rem;
  color: #2c1810;
  outline: none;
}

.search-input::placeholder {
  color: #a89880;
}

.clear-btn,
.advanced-toggle-btn {
  padding: 0.5rem;
  background: #f5efe6;
  border: none;
  border-radius: 8px;
  color: #8b7355;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-btn {
  margin-right: 0.25rem;
}

.advanced-toggle-btn {
  margin-right: 0.5rem;
}

.clear-btn:hover,
.advanced-toggle-btn:hover {
  background: #e8ddd0;
  color: #6b5344;
}

.advanced-toggle-btn.active {
  background: linear-gradient(135deg, #8b7355 0%, #a08060 100%);
  color: white;
}

/* Advanced Filters Panel */
.advanced-filters-panel {
  max-width: 700px;
  margin: 0 auto 2rem;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(44, 24, 16, 0.1);
  border: 2px solid #e8ddd0;
  overflow: hidden;
}

.filters-inner {
  padding: 1.5rem;
}

.filters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f5efe6;
}

.filters-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Crimson Pro', serif;
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c1810;
}

.filters-title svg {
  color: #8b7355;
}

.close-filters-btn {
  padding: 0.375rem;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: #8b7355;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-filters-btn:hover {
  background: #f5efe6;
  color: #6b5344;
}

.filters-grid {
  display: grid;
  gap: 1.5rem;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.filter-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: #6b5344;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.filter-label svg {
  color: #8b7355;
}

/* Date Range Picker */
.date-range-picker {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.date-input {
  flex: 1;
  padding: 0.75rem 1rem;
  background: #faf7f2;
  border: 2px solid #e8ddd0;
  border-radius: 10px;
  font-family: 'Nunito', sans-serif;
  font-size: 0.9375rem;
  color: #2c1810;
  transition: all 0.2s ease;
}

.date-input:focus {
  outline: none;
  border-color: #8b7355;
  background: white;
}

.date-separator {
  color: #a89880;
  font-weight: 500;
}

/* Filter Chips */
.filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.filter-chip {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.5rem 0.875rem;
  background: #faf7f2;
  border: 2px solid #e8ddd0;
  border-radius: 10px;
  font-family: 'Nunito', sans-serif;
  font-size: 0.875rem;
  font-weight: 500;
  color: #6b5344;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-chip:hover {
  background: #f5efe6;
  border-color: #c9b896;
}

.filter-chip.active {
  background: linear-gradient(135deg, #8b7355 0%, #a08060 100%);
  border-color: #8b7355;
  color: white;
}

.chip-icon {
  font-size: 0.875rem;
}

.chip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.filter-chip.active .chip-dot {
  background: white;
}

/* Color Chips */
.color-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.color-chip {
  width: 36px;
  height: 36px;
  padding: 0;
  background: #faf7f2;
  border: 2px solid #e8ddd0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-chip:hover {
  border-color: #c9b896;
  transform: scale(1.05);
}

.color-chip.active {
  border-color: var(--chip-color);
  box-shadow: 0 0 0 2px var(--chip-color);
}

.color-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--chip-color);
}

/* Filter Actions */
.filters-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #f5efe6;
}

.reset-btn,
.apply-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  border-radius: 10px;
  font-family: 'Nunito', sans-serif;
  font-size: 0.9375rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.reset-btn {
  background: transparent;
  border: 2px solid #e8ddd0;
  color: #6b5344;
}

.reset-btn:hover {
  background: #f5efe6;
  border-color: #c9b896;
}

.apply-btn {
  background: linear-gradient(135deg, #8b7355 0%, #a08060 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.25);
}

.apply-btn:hover {
  box-shadow: 0 6px 16px rgba(139, 115, 85, 0.35);
  transform: translateY(-1px);
}

/* Active Filters Summary */
.active-filters-summary {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #f5efe6;
  text-align: center;
}

.active-count {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.75rem;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 20px;
  font-size: 0.8125rem;
  font-weight: 600;
  color: #92400e;
}

/* Transition Animation */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Tabs */
.tabs-section {
  max-width: 1000px;
  margin: 0 auto 2rem;
  display: flex;
  justify-content: center;
  gap: 0.5rem;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: white;
  border: 2px solid #e8ddd0;
  border-radius: 12px;
  font-family: 'Nunito', sans-serif;
  font-size: 0.9375rem;
  font-weight: 600;
  color: #6b5344;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn:hover {
  border-color: #c9b896;
  background: #faf7f2;
}

.tab-btn.active {
  background: linear-gradient(135deg, #8b7355 0%, #a08060 100%);
  border-color: #8b7355;
  color: white;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.3);
}

.tab-icon {
  font-size: 1rem;
}

.tab-count {
  padding: 0.125rem 0.5rem;
  background: rgba(139, 115, 85, 0.15);
  border-radius: 10px;
  font-size: 0.75rem;
  min-width: 1.5rem;
  text-align: center;
}

.tab-btn.active .tab-count {
  background: rgba(255, 255, 255, 0.25);
}

/* Loading State */
.loading-state {
  text-align: center;
  padding: 4rem 2rem;
}

.loader {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.loader-ring {
  width: 16px;
  height: 16px;
  border: 2px solid #e8ddd0;
  border-top-color: #8b7355;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loader-ring:nth-child(2) {
  animation-delay: 0.15s;
}

.loader-ring:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  max-width: 500px;
  margin: 0 auto;
}

.empty-illustration {
  margin-bottom: 1.5rem;
  opacity: 0.9;
}

.empty-state h3 {
  font-family: 'Crimson Pro', serif;
  font-size: 1.75rem;
  color: #2c1810;
  margin: 0 0 0.75rem;
}

.empty-state p {
  font-size: 1rem;
  color: #6b5344;
  margin: 0;
}

/* Results Section */
.results-section {
  max-width: 1000px;
  margin: 0 auto;
}

.results-summary {
  font-size: 0.9375rem;
  color: #6b5344;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e8ddd0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.results-summary strong {
  color: #2c1810;
}

.results-summary em {
  color: #8b7355;
  font-style: normal;
  font-weight: 600;
}

.filtered-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.625rem;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #92400e;
}

/* Results Group */
.results-group {
  margin-bottom: 3rem;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
}

.group-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.books-icon {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
}

.notes-icon {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e40af;
}

.quotes-icon {
  background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
  color: #9f1239;
}

.group-title {
  font-family: 'Crimson Pro', serif;
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c1810;
  margin: 0;
}

.group-count {
  padding: 0.25rem 0.75rem;
  background: #f5efe6;
  border-radius: 12px;
  font-size: 0.8125rem;
  font-weight: 600;
  color: #6b5344;
}

/* Book Cards */
.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.book-card {
  display: flex;
  gap: 1rem;
  background: white;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 2px 8px rgba(44, 24, 16, 0.06);
  border: 1px solid #e8ddd0;
  cursor: pointer;
  transition: all 0.3s ease;
  animation: fadeInUp 0.4s ease forwards;
  opacity: 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.book-card:hover {
  box-shadow: 0 8px 24px rgba(44, 24, 16, 0.1);
  transform: translateY(-2px);
}

.book-cover-placeholder {
  width: 60px;
  height: 80px;
  background: linear-gradient(135deg, #f5efe6 0%, #e8ddd0 100%);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a89880;
  flex-shrink: 0;
}

.book-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  font-family: 'Crimson Pro', serif;
  font-size: 1.125rem;
  font-weight: 600;
  color: #2c1810;
  margin: 0 0 0.25rem;
  line-height: 1.3;
}

.book-author {
  font-size: 0.875rem;
  color: #6b5344;
  margin: 0 0 0.75rem;
}

.book-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.75rem;
}

.status-tag {
  padding: 0.25rem 0.625rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
}

.status-want_to_read {
  background: #f5efe6;
  color: #6b5344;
}

.status-reading {
  background: #fef3c7;
  color: #92400e;
}

.status-completed {
  background: #d1fae5;
  color: #065f46;
}

.status-abandoned {
  background: #e5e7eb;
  color: #374151;
}

.progress-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.progress-bar {
  width: 50px;
  height: 4px;
  background: #e8ddd0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #8b7355 0%, #a08060 100%);
  border-radius: 2px;
}

.progress-text {
  font-size: 0.75rem;
  color: #6b5344;
  font-weight: 500;
}

/* Note Cards */
.notes-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.note-card {
  background: white;
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: 0 2px 8px rgba(44, 24, 16, 0.06);
  border: 1px solid #e8ddd0;
  cursor: pointer;
  transition: all 0.3s ease;
  animation: fadeInUp 0.4s ease forwards;
  opacity: 0;
}

.note-card:hover {
  box-shadow: 0 8px 24px rgba(44, 24, 16, 0.1);
  transform: translateY(-2px);
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.note-title {
  font-family: 'Crimson Pro', serif;
  font-size: 1.125rem;
  font-weight: 600;
  color: #2c1810;
  margin: 0;
  line-height: 1.3;
}

.type-badge {
  padding: 0.25rem 0.625rem;
  border-radius: 6px;
  font-size: 0.75rem;
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

.note-content {
  font-size: 0.9375rem;
  line-height: 1.6;
  color: #374151;
  margin: 0 0 0.75rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.book-source {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.8125rem;
  color: #6b5344;
}

/* Quote Cards */
.quotes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.quote-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(44, 24, 16, 0.06);
  border: 1px solid #e8ddd0;
  cursor: pointer;
  transition: all 0.3s ease;
  animation: fadeInUp 0.4s ease forwards;
  opacity: 0;
}

.quote-card:hover {
  box-shadow: 0 8px 24px rgba(44, 24, 16, 0.1);
  transform: translateY(-2px);
}

.quote-color-bar {
  height: 3px;
  width: 100%;
}

.quote-content {
  padding: 1.25rem;
  padding-bottom: 0.75rem;
  position: relative;
}

.quote-mark {
  font-family: 'Crimson Pro', serif;
  font-size: 2.5rem;
  color: #d4c4a8;
  line-height: 1;
  position: absolute;
  top: 0.75rem;
  left: 1rem;
}

.quote-mark.end {
  top: auto;
  bottom: -0.25rem;
  left: auto;
  right: 1rem;
}

.quote-text {
  font-family: 'Crimson Pro', serif;
  font-size: 1.0625rem;
  font-style: italic;
  line-height: 1.6;
  color: #2c1810;
  margin: 0;
  padding: 0 1.5rem;
}

.quote-footer {
  padding: 0.75rem 1.25rem;
  border-top: 1px solid #f5efe6;
}

/* Highlight Styles */
:deep(mark) {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #78350f;
  padding: 0.125rem 0.25rem;
  border-radius: 3px;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 768px) {
  .search-page {
    padding: 1.5rem 1rem;
  }

  .page-title {
    font-size: 2rem;
  }

  .advanced-filters-panel {
    border-radius: 12px;
  }

  .filters-inner {
    padding: 1rem;
  }

  .date-range-picker {
    flex-direction: column;
  }

  .date-separator {
    display: none;
  }

  .tabs-section {
    flex-wrap: wrap;
  }

  .tab-btn {
    padding: 0.625rem 1rem;
    font-size: 0.875rem;
  }

  .tab-label {
    display: none;
  }

  .tab-icon {
    font-size: 1.25rem;
  }

  .books-grid,
  .quotes-grid {
    grid-template-columns: 1fr;
  }

  .filters-actions {
    flex-direction: column;
  }

  .reset-btn,
  .apply-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>