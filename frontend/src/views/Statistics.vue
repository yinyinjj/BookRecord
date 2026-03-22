<template>
  <div class="statistics-page">
    <!-- ==================== 页面头部 ==================== -->
    <div class="page-header">
      <h1 class="page-title">阅读统计</h1>
      <p class="page-subtitle">记录阅读足迹，见证成长轨迹</p>
    </div>

    <!-- ==================== 年度阅读报告卡片 ==================== -->
    <div class="annual-report-card">
      <!-- 卡片头部 -->
      <div class="card-header">
        <div class="header-left">
          <h2 class="card-title">{{ currentYear }} 年度阅读报告</h2>
          <p class="card-desc">回顾您的年度阅读收获</p>
        </div>

        <!-- 年份选择器 -->
        <div class="year-selector">
          <button
            v-for="year in availableYears"
            :key="year"
            :class="['year-btn', { active: currentYear === year }]"
            @click="changeYear(year)"
          >
            {{ year }}
          </button>
        </div>
      </div>

      <!-- 年度数据加载中 -->
      <div v-if="annualLoading" class="annual-loading">
        <div class="loading-spinner"></div>
        <p>正在生成年度报告...</p>
      </div>

      <!-- 年度统计数据 -->
      <div v-else class="annual-content">
        <!-- 核心数据卡片 -->
        <div class="stats-grid">
          <div class="stat-card completed">
            <div class="stat-icon">📚</div>
            <div class="stat-info">
              <div class="stat-value">{{ annualData.completedBooksCount || 0 }}</div>
              <div class="stat-label">完成书籍</div>
            </div>
          </div>
          <div class="stat-card new-books">
            <div class="stat-icon">📖</div>
            <div class="stat-info">
              <div class="stat-value">{{ annualData.newBooksCount || 0 }}</div>
              <div class="stat-label">新增书籍</div>
            </div>
          </div>
          <div class="stat-card hours">
            <div class="stat-icon">⏱️</div>
            <div class="stat-info">
              <div class="stat-value">{{ annualData.estimatedReadingHours || 0 }}</div>
              <div class="stat-label">阅读时长(小时)</div>
            </div>
          </div>
          <div class="stat-card pages">
            <div class="stat-icon">📄</div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(annualData.totalPagesRead) }}</div>
              <div class="stat-label">阅读页数</div>
            </div>
          </div>
        </div>

        <!-- 次级统计 -->
        <div class="secondary-stats">
          <div class="secondary-stat">
            <span class="secondary-icon">✏️</span>
            <span class="secondary-value">{{ annualData.notesCount || 0 }}</span>
            <span class="secondary-label">条感悟</span>
          </div>
          <div class="secondary-divider"></div>
          <div class="secondary-stat">
            <span class="secondary-icon">💬</span>
            <span class="secondary-value">{{ annualData.quotesCount || 0 }}</span>
            <span class="secondary-label">条金句</span>
          </div>
        </div>

        <!-- 详细分析区域 -->
        <div class="analysis-section">
          <!-- 阅读时间段分布 -->
          <div class="analysis-card">
            <h3 class="analysis-title">阅读习惯分析</h3>
            <div class="time-distribution" v-if="annualData.readingTimeDistribution?.length > 0">
              <div
                v-for="item in annualData.readingTimeDistribution"
                :key="item.period"
                class="time-bar-container"
              >
                <div class="time-label">{{ item.label }}</div>
                <div class="time-bar-wrapper">
                  <div
                    class="time-bar"
                    :style="{ width: getTimeBarWidth(item.count) }"
                  ></div>
                </div>
                <div class="time-count">{{ item.count }} 次</div>
              </div>
            </div>
            <div v-else class="no-data">暂无数据</div>
          </div>

          <!-- 年度关键词 -->
          <div class="analysis-card">
            <h3 class="analysis-title">年度关键词</h3>
            <div class="keywords-cloud" v-if="annualData.topKeywords?.length > 0">
              <span
                v-for="(keyword, index) in annualData.topKeywords"
                :key="keyword.word"
                class="keyword-tag"
                :style="getKeywordStyle(keyword.count, index)"
              >
                {{ keyword.word }}
              </span>
            </div>
            <div v-else class="no-data">暂无关键词</div>
          </div>

          <!-- 年度最佳书籍 -->
          <div class="analysis-card books-highlight">
            <h3 class="analysis-title">年度亮点</h3>
            <div class="highlight-books">
              <!-- 年度最佳书籍 -->
              <div class="highlight-book" v-if="annualData.bookOfTheYear" @click="goToBook(annualData.bookOfTheYear.id)">
                <div class="highlight-badge gold">年度最佳</div>
                <div class="highlight-info">
                  <div class="highlight-title">{{ annualData.bookOfTheYear.title }}</div>
                  <div class="highlight-author" v-if="annualData.bookOfTheYear.author">
                    {{ annualData.bookOfTheYear.author }}
                  </div>
                  <div class="highlight-rating" v-if="annualData.bookOfTheYear.rating">
                    <span v-for="i in 5" :key="i" :class="['star', { filled: i <= annualData.bookOfTheYear.rating }]">★</span>
                  </div>
                </div>
              </div>
              <!-- 年度最长书籍 -->
              <div class="highlight-book" v-if="annualData.longestBook" @click="goToBook(annualData.longestBook.id)">
                <div class="highlight-badge blue">最长阅读</div>
                <div class="highlight-info">
                  <div class="highlight-title">{{ annualData.longestBook.title }}</div>
                  <div class="highlight-author" v-if="annualData.longestBook.author">
                    {{ annualData.longestBook.author }}
                  </div>
                  <div class="highlight-pages" v-if="annualData.longestBook.totalPages">
                    {{ annualData.longestBook.totalPages }} 页
                  </div>
                </div>
              </div>
            </div>
            <div v-if="!annualData.bookOfTheYear && !annualData.longestBook" class="no-data">
              暂无年度亮点书籍
            </div>
          </div>
        </div>

        <!-- 分享海报区域 -->
        <div class="share-section" v-if="!annualLoading && hasAnnualData">
          <div class="share-actions">
            <button class="share-btn" @click="generatePoster" :disabled="posterGenerating">
              <span class="btn-icon">📷</span>
              <span>{{ posterGenerating ? '生成中...' : '生成分享海报' }}</span>
            </button>
            <button class="share-btn outline" @click="shareReport" :disabled="!canShare">
              <span class="btn-icon">🔗</span>
              <span>一键分享</span>
            </button>
          </div>
          <p class="share-hint">生成年度阅读报告海报，分享您的阅读成果</p>
        </div>
      </div>
    </div>

    <!-- ==================== 海报模板（隐藏） ==================== -->
    <div class="poster-container" :class="{ visible: showPoster }" ref="posterContainerRef">
      <div class="poster-overlay" @click="closePoster"></div>
      <div class="poster-modal">
        <div class="poster-header">
          <h3>年度阅读报告</h3>
          <button class="close-btn" @click="closePoster">✕</button>
        </div>
        <div class="poster-content" ref="posterRef">
          <!-- 海报头部 -->
          <div class="poster-top">
            <div class="poster-year">{{ currentYear }}</div>
            <div class="poster-title">年度阅读报告</div>
            <div class="poster-subtitle">记录阅读足迹，见证成长轨迹</div>
          </div>
          <!-- 海报核心数据 -->
          <div class="poster-stats">
            <div class="poster-stat-item">
              <div class="poster-stat-icon">📚</div>
              <div class="poster-stat-value">{{ annualData.completedBooksCount || 0 }}</div>
              <div class="poster-stat-label">完成书籍</div>
            </div>
            <div class="poster-stat-item">
              <div class="poster-stat-icon">📖</div>
              <div class="poster-stat-value">{{ annualData.newBooksCount || 0 }}</div>
              <div class="poster-stat-label">新增书籍</div>
            </div>
            <div class="poster-stat-item">
              <div class="poster-stat-icon">⏱️</div>
              <div class="poster-stat-value">{{ annualData.estimatedReadingHours || 0 }}</div>
              <div class="poster-stat-label">阅读时长(小时)</div>
            </div>
            <div class="poster-stat-item">
              <div class="poster-stat-icon">📄</div>
              <div class="poster-stat-value">{{ formatNumber(annualData.totalPagesRead) }}</div>
              <div class="poster-stat-label">阅读页数</div>
            </div>
          </div>
          <!-- 海报次级数据 -->
          <div class="poster-secondary">
            <div class="poster-secondary-item">
              <span class="ps-icon">✏️</span>
              <span class="ps-value">{{ annualData.notesCount || 0 }}</span>
              <span class="ps-label">条感悟</span>
            </div>
            <div class="poster-secondary-item">
              <span class="ps-icon">💬</span>
              <span class="ps-value">{{ annualData.quotesCount || 0 }}</span>
              <span class="ps-label">条金句</span>
            </div>
          </div>
          <!-- 海报关键词 -->
          <div class="poster-keywords" v-if="annualData.topKeywords?.length > 0">
            <div class="poster-keywords-title">年度关键词</div>
            <div class="poster-keywords-list">
              <span
                v-for="(keyword, index) in annualData.topKeywords?.slice(0, 6)"
                :key="keyword.word"
                class="poster-keyword"
                :style="getPosterKeywordStyle(keyword.count, index)"
              >
                {{ keyword.word }}
              </span>
            </div>
          </div>
          <!-- 海报底部 -->
          <div class="poster-footer">
            <div class="poster-brand">Book Record · 读书笔记管理系统</div>
          </div>
        </div>
        <div class="poster-actions">
          <button class="poster-action-btn" @click="downloadPoster" :disabled="downloading">
            <span class="btn-icon">💾</span>
            <span>{{ downloading ? '保存中...' : '保存图片' }}</span>
          </button>
          <button class="poster-action-btn primary" @click="sharePoster" :disabled="!canShare">
            <span class="btn-icon">📤</span>
            <span>分享</span>
          </button>
        </div>
      </div>
    </div>

    <!-- ==================== 阅读效率分析卡片 ==================== -->
    <div class="efficiency-card">
      <div class="card-header">
        <div class="header-left">
          <h2 class="card-title">阅读效率分析</h2>
          <p class="card-desc">了解您的阅读习惯，优化阅读效率</p>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="efficiencyLoading" class="efficiency-loading">
        <div class="loading-spinner light"></div>
        <p>正在分析阅读效率...</p>
      </div>

      <!-- 效率数据 -->
      <div v-else class="efficiency-content">
        <!-- 效率指标 -->
        <div class="efficiency-metrics">
          <div class="metric-item">
            <div class="metric-icon">📖</div>
            <div class="metric-info">
              <div class="metric-value">{{ efficiencyData.averageReadingSpeed?.toFixed(1) || 0 }}</div>
              <div class="metric-label">平均阅读速度(页/天)</div>
            </div>
          </div>
          <div class="metric-item">
            <div class="metric-icon">📅</div>
            <div class="metric-info">
              <div class="metric-value">{{ efficiencyData.averageCompletionDays?.toFixed(1) || 0 }}</div>
              <div class="metric-label">平均完成天数</div>
            </div>
          </div>
          <div class="metric-item">
            <div class="metric-icon">✏️</div>
            <div class="metric-info">
              <div class="metric-value">{{ efficiencyData.noteRecordFrequency?.toFixed(1) || 0 }}</div>
              <div class="metric-label">感悟频率(次/周)</div>
            </div>
          </div>
          <div class="metric-item">
            <div class="metric-icon">💬</div>
            <div class="metric-info">
              <div class="metric-value">{{ efficiencyData.quoteRecordFrequency?.toFixed(1) || 0 }}</div>
              <div class="metric-label">金句频率(次/周)</div>
            </div>
          </div>
        </div>

        <!-- 阅读建议 -->
        <div class="advice-section" v-if="efficiencyData.readingAdvices?.length > 0">
          <h3 class="advice-title">阅读建议</h3>
          <div class="advice-list">
            <div
              v-for="advice in efficiencyData.readingAdvices"
              :key="advice.type"
              class="advice-item"
            >
              <span class="advice-icon">{{ advice.icon }}</span>
              <div class="advice-content">
                <div class="advice-name">{{ advice.title }}</div>
                <div class="advice-text">{{ advice.content }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 趋势图表卡片 ==================== -->
    <div class="chart-card">
      <!-- 卡片头部 -->
      <div class="card-header">
        <div class="header-left">
          <h2 class="card-title">阅读趋势</h2>
          <p class="card-desc">每月新增与完成书籍数量变化</p>
        </div>

        <!-- 时间范围切换按钮 -->
        <div class="range-buttons">
          <button
            v-for="range in timeRanges"
            :key="range.value"
            :class="['range-btn', { active: currentRange === range.value }]"
            @click="changeRange(range.value)"
          >
            {{ range.label }}
          </button>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="chart-container" v-loading="trendLoading">
        <div ref="trendChartRef" class="chart"></div>

        <!-- 空状态 -->
        <div v-if="!trendLoading && isTrendEmpty" class="empty-state">
          <div class="empty-icon">📚</div>
          <p class="empty-text">暂无阅读数据</p>
          <p class="empty-hint">添加书籍开始记录您的阅读旅程</p>
        </div>
      </div>

      <!-- 数据摘要 -->
      <div v-if="!isTrendEmpty" class="data-summary">
        <div class="summary-item">
          <span class="summary-label">统计周期</span>
          <span class="summary-value">{{ dataPeriod }}</span>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <span class="summary-label">新增书籍</span>
          <span class="summary-value highlight-green">{{ totalNewBooks }} 本</span>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <span class="summary-label">完成阅读</span>
          <span class="summary-value highlight-amber">{{ totalCompletedBooks }} 本</span>
        </div>
      </div>
    </div>

    <!-- ==================== 分类统计卡片 ==================== -->
    <div class="category-section">
      <div class="section-title">
        <h2>分类统计</h2>
        <p>了解您的阅读偏好与习惯</p>
      </div>

      <div class="category-cards" v-loading="categoryLoading">
        <!-- 阅读状态分布饼图 -->
        <div class="category-card">
          <h3 class="card-title-sm">阅读状态分布</h3>
          <div ref="statusChartRef" class="category-chart"></div>
          <div v-if="!categoryLoading && statusData.length === 0" class="chart-empty">
            暂无数据
          </div>
        </div>

        <!-- 感悟类型分布雷达图 -->
        <div class="category-card">
          <h3 class="card-title-sm">感悟类型分布</h3>
          <div ref="noteTypeChartRef" class="category-chart"></div>
          <div v-if="!categoryLoading && noteTypeData.length === 0" class="chart-empty">
            暂无数据
          </div>
        </div>

        <!-- 热门标签云 -->
        <div class="category-card tag-card">
          <h3 class="card-title-sm">热门标签</h3>
          <div class="tag-cloud" v-if="topTags.length > 0">
            <span
              v-for="(tag, index) in topTags"
              :key="tag.tag"
              class="tag-item clickable"
              :style="getTagStyle(tag.count, index)"
              @click="handleTagClick(tag.tag)"
            >
              {{ tag.tag }}
            </span>
          </div>
          <div v-else class="chart-empty">暂无标签数据</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 阅读统计页面组件
 * 展示用户的阅读趋势图表和分类统计
 * 使用 ECharts 实现交互式图表
 */
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import html2canvas from 'html2canvas'
import { ElMessage } from 'element-plus'
import api from '@/api/index'

// ==================== 路由实例 ====================

/** 路由实例，用于页面跳转 */
const router = useRouter()

// ==================== 响应式状态定义 ====================

/** 图表 DOM 引用 */
const trendChartRef = ref(null)
const statusChartRef = ref(null)
const noteTypeChartRef = ref(null)

/** 加载状态 */
const trendLoading = ref(false)
const categoryLoading = ref(false)
const annualLoading = ref(false)

/** ECharts 实例 */
let trendChartInstance = null
let statusChartInstance = null
let noteTypeChartInstance = null

/** 当前选中的时间范围 */
const currentRange = ref('1y')

/** 当前选中的年份 */
const currentYear = ref(new Date().getFullYear())

/** 可选年份列表 */
const availableYears = computed(() => {
  const current = new Date().getFullYear()
  const years = []
  for (let y = current; y >= 2020; y--) {
    years.push(y)
  }
  return years
})

/** 趋势数据 */
const trendData = ref([])

/** 分类统计数据 */
const statusData = ref([])
const noteTypeData = ref([])
const topTags = ref([])

/** 年度报告数据 */
const annualData = ref({})

/** 效率分析数据 */
const efficiencyData = ref({})

/** 效率分析加载状态 */
const efficiencyLoading = ref(false)

/** 海报相关状态 */
const showPoster = ref(false)
const posterGenerating = ref(false)
const downloading = ref(false)
const posterRef = ref(null)
const posterContainerRef = ref(null)
const posterImageUrl = ref('')

/** 时间范围选项配置 */
const timeRanges = [
  { label: '近6个月', value: '6m' },
  { label: '近1年', value: '1y' },
  { label: '全部', value: 'all' }
]

/** 阅读状态名称映射：显示名称 -> 状态编码 */
const statusMap = {
  '想读': 'WANT_TO_READ',
  '在读': 'READING',
  '已完成': 'COMPLETED',
  '已放弃': 'ABANDONED'
}

/** 感悟类型名称映射：显示名称 -> 类型编码 */
const noteTypeMap = {
  '思考': 'THOUGHT',
  '总结': 'SUMMARY',
  '问题': 'QUESTION',
  '洞察': 'INSIGHT'
}

// ==================== 计算属性 ====================

/**
 * 判断趋势数据是否为空
 */
const isTrendEmpty = computed(() => {
  if (!trendData.value || trendData.value.length === 0) return true
  return trendData.value.every(
    item => item.newBooks === 0 && item.completedBooks === 0
  )
})

/**
 * 计算统计周期显示文本
 */
const dataPeriod = computed(() => {
  if (!trendData.value || trendData.value.length === 0) return '-'
  const first = trendData.value[0]?.label || ''
  const last = trendData.value[trendData.value.length - 1]?.label || ''
  if (first === last) return first
  return `${first} ~ ${last}`
})

/**
 * 计算新增书籍总数
 */
const totalNewBooks = computed(() => {
  if (!trendData.value) return 0
  return trendData.value.reduce((sum, item) => sum + (item.newBooks || 0), 0)
})

/**
 * 计算完成书籍总数
 */
const totalCompletedBooks = computed(() => {
  if (!trendData.value) return 0
  return trendData.value.reduce((sum, item) => sum + (item.completedBooks || 0), 0)
})

/**
 * 判断年度报告是否有数据
 */
const hasAnnualData = computed(() => {
  const data = annualData.value
  return data && (
    data.completedBooksCount > 0 ||
    data.newBooksCount > 0 ||
    data.notesCount > 0 ||
    data.quotesCount > 0
  )
})

/**
 * 判断是否支持分享功能
 * Web Share API 需要在安全上下文（HTTPS）中使用
 */
const canShare = computed(() => {
  return typeof navigator !== 'undefined' && typeof navigator.share === 'function'
})

// ==================== 生命周期钩子 ====================

/**
 * 组件挂载时加载数据
 */
onMounted(() => {
  loadAnnualData()
  loadEfficiencyData()
  loadTrendData()
  loadCategoryData()
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

/**
 * 组件卸载时销毁图表实例
 */
onUnmounted(() => {
  disposeCharts()
  window.removeEventListener('resize', handleResize)
})

// ==================== 方法定义 ====================

/**
 * 加载年度报告数据
 */
async function loadAnnualData() {
  annualLoading.value = true
  try {
    const response = await api.get('/v1/statistics/annual', {
      params: { year: currentYear.value }
    })
    annualData.value = response.data || {}
  } catch (error) {
    console.error('加载年度报告失败:', error)
    annualData.value = {}
  } finally {
    annualLoading.value = false
  }
}

/**
 * 加载效率分析数据
 */
async function loadEfficiencyData() {
  efficiencyLoading.value = true
  try {
    const response = await api.get('/v1/statistics/efficiency')
    efficiencyData.value = response.data || {}
  } catch (error) {
    console.error('加载效率分析失败:', error)
    efficiencyData.value = {}
  } finally {
    efficiencyLoading.value = false
  }
}

/**
 * 切换年份
 */
function changeYear(year) {
  if (currentYear.value === year) return
  currentYear.value = year
  loadAnnualData()
}

/**
 * 格式化数字显示
 */
function formatNumber(num) {
  if (!num) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

/**
 * 计算时间条宽度百分比
 */
function getTimeBarWidth(count) {
  const distribution = annualData.value.readingTimeDistribution || []
  const maxCount = Math.max(...distribution.map(d => d.count), 1)
  return Math.max((count / maxCount) * 100, 5) + '%'
}

/**
 * 计算关键词样式
 */
function getKeywordStyle(count, index) {
  const keywords = annualData.value.topKeywords || []
  const maxCount = keywords[0]?.count || 1
  const minSize = 14
  const maxSize = 26
  const size = minSize + (count / maxCount) * (maxSize - minSize)

  const colors = [
    '#4a9c7b', '#d4a84b', '#5b8ff9', '#e690d1',
    '#73c0de', '#3ba272', '#fc8452', '#9a60b4'
  ]
  const color = colors[index % colors.length]

  return {
    fontSize: `${size}px`,
    color: color,
    opacity: 0.7 + (count / maxCount) * 0.3
  }
}

/**
 * 跳转到书籍详情页
 */
function goToBook(bookId) {
  router.push(`/books/${bookId}`)
}

/**
 * 加载趋势统计数据
 */
async function loadTrendData() {
  trendLoading.value = true
  try {
    const response = await api.get('/v1/statistics/trend', {
      params: { range: currentRange.value }
    })
    trendData.value = response.data?.monthlyData || []

    await nextTick()
    if (!isTrendEmpty.value) {
      initTrendChart()
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
    trendData.value = []
  } finally {
    trendLoading.value = false
  }
}

/**
 * 加载分类统计数据
 */
async function loadCategoryData() {
  categoryLoading.value = true
  try {
    const response = await api.get('/v1/statistics/categories')
    const data = response.data || {}

    statusData.value = data.readingStatusDistribution || []
    noteTypeData.value = data.noteTypeDistribution || []
    topTags.value = data.topTags || []

    await nextTick()
    initCategoryCharts()
  } catch (error) {
    console.error('加载分类统计失败:', error)
  } finally {
    categoryLoading.value = false
  }
}

/**
 * 切换时间范围
 */
function changeRange(range) {
  if (currentRange.value === range) return
  currentRange.value = range
  loadTrendData()
}

/**
 * 初始化趋势图表
 */
function initTrendChart() {
  if (!trendChartRef.value) return

  if (trendChartInstance) {
    trendChartInstance.dispose()
  }

  trendChartInstance = echarts.init(trendChartRef.value)

  const months = trendData.value.map(item => item.label)
  const newBooksData = trendData.value.map(item => item.newBooks)
  const completedBooksData = trendData.value.map(item => item.completedBooks)

  const option = {
    legend: {
      data: ['新增书籍', '完成阅读'],
      top: 10,
      textStyle: { color: '#5a5a5a', fontSize: 13 },
      itemGap: 30
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: { color: '#333' },
      formatter: function(params) {
        let result = `<div style="font-weight: 600; margin-bottom: 8px;">${params[0].axisValue}</div>`
        params.forEach(param => {
          result += `<div style="display: flex; align-items: center; margin: 4px 0;">
            <span style="display: inline-block; width: 10px; height: 10px; border-radius: 2px; background: ${param.color}; margin-right: 8px;"></span>
            <span style="flex: 1;">${param.seriesName}</span>
            <span style="font-weight: 600; margin-left: 12px;">${param.value} 本</span>
          </div>`
        })
        return result
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 60,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      axisPointer: { type: 'shadow' },
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: {
        color: '#666',
        fontSize: 12,
        interval: 0,
        rotate: months.length > 8 ? 30 : 0
      },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisLabel: { color: '#999', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
    },
    series: [
      {
        name: '新增书籍',
        type: 'bar',
        data: newBooksData,
        barWidth: 20,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4a9c7b' },
            { offset: 1, color: '#6bc4a0' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '完成阅读',
        type: 'line',
        data: completedBooksData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: '#d4a84b' },
        itemStyle: { color: '#d4a84b', borderWidth: 2, borderColor: '#fff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(212, 168, 75, 0.25)' },
            { offset: 1, color: 'rgba(212, 168, 75, 0.02)' }
          ])
        }
      }
    ],
    animationDuration: 1000,
    animationEasing: 'cubicOut'
  }

  trendChartInstance.setOption(option)
}

/**
 * 初始化分类图表
 */
function initCategoryCharts() {
  // 饼图：阅读状态分布
  if (statusChartRef.value && statusData.value.length > 0) {
    if (statusChartInstance) statusChartInstance.dispose()
    statusChartInstance = echarts.init(statusChartRef.value)

    const colors = ['#4a9c7b', '#d4a84b', '#5b8ff9', '#909399']

    statusChartInstance.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} 本 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center',
        textStyle: { fontSize: 12 }
      },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        labelLine: { show: false },
        data: statusData.value.map((item, index) => ({
          name: item.label,
          value: item.count,
          itemStyle: { color: colors[index % colors.length] }
        }))
      }]
    })

    // 添加饼图点击事件：跳转到书架页并筛选对应状态
    statusChartInstance.on('click', (params) => {
      const status = statusMap[params.name]
      if (status) {
        router.push({ path: '/bookshelf', query: { status } })
      }
    })
  }

  // 雷达图：感悟类型分布
  if (noteTypeChartRef.value && noteTypeData.value.length > 0) {
    if (noteTypeChartInstance) noteTypeChartInstance.dispose()
    noteTypeChartInstance = echarts.init(noteTypeChartRef.value)

    const maxValue = Math.max(...noteTypeData.value.map(d => d.count), 1)

    noteTypeChartInstance.setOption({
      tooltip: {},
      radar: {
        indicator: noteTypeData.value.map(item => ({
          name: item.label,
          max: maxValue
        })),
        center: ['50%', '55%'],
        radius: '60%',
        axisName: { color: '#666', fontSize: 12 },
        splitArea: { areaStyle: { color: ['rgba(74, 156, 123, 0.05)', 'rgba(74, 156, 123, 0.1)'] } },
        axisLine: { lineStyle: { color: 'rgba(74, 156, 123, 0.3)' } },
        splitLine: { lineStyle: { color: 'rgba(74, 156, 123, 0.2)' } }
      },
      series: [{
        type: 'radar',
        data: [{
          value: noteTypeData.value.map(item => item.count),
          name: '感悟分布',
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { width: 2, color: '#4a9c7b' },
          itemStyle: { color: '#4a9c7b' },
          areaStyle: { color: 'rgba(74, 156, 123, 0.3)' }
        }]
      }]
    })

    // 添加雷达图点击事件：跳转到感悟页并筛选对应类型
    noteTypeChartInstance.on('click', (params) => {
      const type = noteTypeMap[params.name]
      if (type) {
        router.push({ path: '/notes', query: { type } })
      }
    })
  }
}

/**
 * 计算标签样式
 */
function getTagStyle(count, index) {
  // 根据词频计算字体大小
  const maxCount = topTags.value[0]?.count || 1
  const minSize = 12
  const maxSize = 24
  const size = minSize + (count / maxCount) * (maxSize - minSize)

  // 预定义颜色
  const colors = [
    '#4a9c7b', '#d4a84b', '#5b8ff9', '#e690d1',
    '#73c0de', '#3ba272', '#fc8452', '#9a60b4'
  ]
  const color = colors[index % colors.length]

  return {
    fontSize: `${size}px`,
    color: color,
    opacity: 0.6 + (count / maxCount) * 0.4
  }
}

/**
 * 处理标签点击事件
 * 跳转到搜索页搜索该标签
 *
 * @param {string} tag - 标签名称
 */
function handleTagClick(tag) {
  router.push({ path: '/search', query: { q: tag } })
}

/**
 * 处理窗口大小变化
 */
function handleResize() {
  trendChartInstance?.resize()
  statusChartInstance?.resize()
  noteTypeChartInstance?.resize()
}

/**
 * 销毁所有图表实例
 */
function disposeCharts() {
  trendChartInstance?.dispose()
  statusChartInstance?.dispose()
  noteTypeChartInstance?.dispose()
  trendChartInstance = null
  statusChartInstance = null
  noteTypeChartInstance = null
}

// ==================== 海报相关方法 ====================

/**
 * 计算海报关键词样式
 *
 * @param {number} count - 关键词出现次数
 * @param {number} index - 关键词索引
 * @returns {Object} CSS 样式对象
 */
function getPosterKeywordStyle(count, index) {
  const keywords = annualData.value.topKeywords || []
  const maxCount = keywords[0]?.count || 1
  const minSize = 14
  const maxSize = 24
  const size = minSize + (count / maxCount) * (maxSize - minSize)

  const colors = [
    '#4a9c7b', '#d4a84b', '#5b8ff9', '#e690d1',
    '#73c0de', '#3ba272'
  ]
  const color = colors[index % colors.length]

  return {
    fontSize: `${size}px`,
    color: color
  }
}

/**
 * 生成海报
 * 显示海报模态框
 */
async function generatePoster() {
  if (!hasAnnualData.value) {
    ElMessage.warning('暂无年度数据，无法生成报告')
    return
  }

  posterGenerating.value = true
  try {
    // 显示海报模态框
    showPoster.value = true
    await nextTick()

    // 等待 DOM 渲染完成
    await new Promise(resolve => setTimeout(resolve, 100))

    // 生成海报图片
    if (posterRef.value) {
      const canvas = await html2canvas(posterRef.value, {
        backgroundColor: '#1a2a3a',
        scale: 2,
        useCORS: true,
        logging: false
      })
      posterImageUrl.value = canvas.toDataURL('image/png')
    }
  } catch (error) {
    console.error('生成海报失败:', error)
    ElMessage.error('生成海报失败，请重试')
  } finally {
    posterGenerating.value = false
  }
}

/**
 * 一键分享年度报告
 * 使用 Web Share API 直接分享文本信息
 */
async function shareReport() {
  const shareData = {
    title: `${currentYear.value}年度阅读报告`,
    text: `我在 ${currentYear.value} 年完成了 ${annualData.value.completedBooksCount || 0} 本书的阅读，阅读时长约 ${annualData.value.estimatedReadingHours || 0} 小时。使用 Book Record 记录你的阅读足迹！`,
    url: window.location.origin
  }

  try {
    await navigator.share(shareData)
    ElMessage.success('分享成功')
  } catch (error) {
    if (error.name !== 'AbortError') {
      console.error('分享失败:', error)
      ElMessage.error('分享失败，请重试')
    }
  }
}

/**
 * 关闭海报模态框
 */
function closePoster() {
  showPoster.value = false
  posterImageUrl.value = ''
}

/**
 * 下载海报图片
 */
async function downloadPoster() {
  if (!posterRef.value) {
    ElMessage.warning('请先生成海报')
    return
  }

  downloading.value = true
  try {
    // 重新生成高质量图片用于下载
    const canvas = await html2canvas(posterRef.value, {
      backgroundColor: '#1a2a3a',
      scale: 3,
      useCORS: true,
      logging: false
    })

    // 创建下载链接
    const link = document.createElement('a')
    link.href = canvas.toDataURL('image/png')
    link.download = `${currentYear.value}年度阅读报告.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    ElMessage.success('海报已保存')
  } catch (error) {
    console.error('下载海报失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    downloading.value = false
  }
}

/**
 * 分享海报图片
 */
async function sharePoster() {
  if (!posterRef.value) {
    ElMessage.warning('请先生成海报')
    return
  }

  try {
    // 生成图片
    const canvas = await html2canvas(posterRef.value, {
      backgroundColor: '#1a2a3a',
      scale: 2,
      useCORS: true,
      logging: false
    })

    // 转换为 Blob
    const blob = await new Promise(resolve => {
      canvas.toBlob(resolve, 'image/png')
    })

    // 创建 File 对象
    const file = new File([blob], `${currentYear.value}年度阅读报告.png`, {
      type: 'image/png'
    })

    // 使用 Web Share API 分享文件
    if (navigator.canShare && navigator.canShare({ files: [file] })) {
      await navigator.share({
        title: `${currentYear.value}年度阅读报告`,
        text: `我在 ${currentYear.value} 年完成了 ${annualData.value.completedBooksCount || 0} 本书的阅读！`,
        files: [file]
      })
      ElMessage.success('分享成功')
    } else {
      ElMessage.warning('当前浏览器不支持分享图片，请使用保存功能')
    }
  } catch (error) {
    if (error.name !== 'AbortError') {
      console.error('分享海报失败:', error)
      ElMessage.error('分享失败，请重试')
    }
  }
}
</script>

<style scoped>
/* ==================== 页面容器 ==================== */
.statistics-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* ==================== 页面头部 ==================== */
.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.page-subtitle {
  font-size: 15px;
  color: #7a8b9a;
  margin: 0;
}

/* ==================== 年度报告卡片 ==================== */
.annual-report-card {
  background: linear-gradient(135deg, #1a2a3a 0%, #2d4a5e 100%);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 32px;
  color: #fff;
  box-shadow: 0 8px 32px rgba(26, 42, 58, 0.3);
}

.annual-report-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  padding: 0;
  border: none;
}

.annual-report-card .card-title {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 6px 0;
}

.annual-report-card .card-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

/* 年份选择器 */
.year-selector {
  display: flex;
  gap: 8px;
}

.year-btn {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.year-btn:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

.year-btn.active {
  color: #1a2a3a;
  background: #fff;
  border-color: #fff;
}

/* 加载状态 */
.annual-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.2);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.annual-loading p {
  margin-top: 16px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

/* 核心数据卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  backdrop-filter: blur(10px);
}

.stat-icon {
  font-size: 32px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

.stat-card.completed .stat-icon { opacity: 1; }
.stat-card.new-books .stat-icon { opacity: 0.9; }
.stat-card.hours .stat-icon { opacity: 0.85; }
.stat-card.pages .stat-icon { opacity: 0.8; }

/* 次级统计 */
.secondary-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 16px 0;
  margin-bottom: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.secondary-stat {
  display: flex;
  align-items: center;
  gap: 8px;
}

.secondary-icon {
  font-size: 18px;
}

.secondary-value {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.secondary-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.secondary-divider {
  width: 1px;
  height: 24px;
  background: rgba(255, 255, 255, 0.2);
}

/* 分析区域 */
.analysis-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.analysis-card {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 20px;
}

.analysis-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* 阅读时间分布 */
.time-distribution {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.time-bar-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  width: 100px;
  flex-shrink: 0;
}

.time-bar-wrapper {
  flex: 1;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.time-bar {
  height: 100%;
  background: linear-gradient(90deg, #4a9c7b 0%, #6bc4a0 100%);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.time-count {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
  width: 50px;
  text-align: right;
}

/* 关键词云 */
.keywords-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  align-content: center;
  min-height: 120px;
}

.keyword-tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  cursor: default;
  transition: transform 0.2s ease;
}

.keyword-tag:hover {
  transform: scale(1.1);
}

/* 年度亮点书籍 */
.books-highlight {
  grid-column: span 1;
}

.highlight-books {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.highlight-book {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.highlight-book:hover {
  background: rgba(255, 255, 255, 0.1);
}

.highlight-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
  flex-shrink: 0;
}

.highlight-badge.gold {
  background: linear-gradient(135deg, #d4a84b 0%, #f0c674 100%);
  color: #1a2a3a;
}

.highlight-badge.blue {
  background: linear-gradient(135deg, #5b8ff9 0%, #8bb8ff 100%);
  color: #1a2a3a;
}

.highlight-info {
  flex: 1;
  min-width: 0;
}

.highlight-title {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.highlight-author {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 2px;
}

.highlight-rating {
  margin-top: 4px;
}

.highlight-rating .star {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
}

.highlight-rating .star.filled {
  color: #d4a84b;
}

.highlight-pages {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 2px;
}

.no-data {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  padding: 20px 0;
}

/* ==================== 效率分析卡片 ==================== */
.efficiency-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  margin-bottom: 32px;
}

.efficiency-card .card-header {
  padding: 24px 28px 16px;
  border-bottom: 1px solid #f5f5f5;
}

.efficiency-card .card-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 6px 0;
}

.efficiency-card .card-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.efficiency-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-spinner.light {
  width: 36px;
  height: 36px;
  border: 3px solid #f0f0f0;
  border-top-color: #4a9c7b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.efficiency-loading p {
  margin-top: 16px;
  color: #909399;
  font-size: 14px;
}

.efficiency-content {
  padding: 24px 28px;
}

/* 效率指标 */
.efficiency-metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
}

.metric-icon {
  font-size: 28px;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
}

.metric-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

/* 阅读建议 */
.advice-section {
  border-top: 1px solid #f0f0f0;
  padding-top: 20px;
}

.advice-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 16px 0;
}

.advice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.advice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f8faf8 0%, #f5f9f7 100%);
  border-radius: 10px;
  border-left: 3px solid #4a9c7b;
}

.advice-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.advice-content {
  flex: 1;
}

.advice-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.advice-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

/* ==================== 图表卡片 ==================== */
.chart-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  margin-bottom: 32px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px 28px 16px;
  border-bottom: 1px solid #f5f5f5;
}

.header-left {
  flex: 1;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 6px 0;
}

.card-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

/* ==================== 时间范围按钮 ==================== */
.range-buttons {
  display: flex;
  gap: 8px;
}

.range-btn {
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.range-btn:hover {
  color: #4a9c7b;
  border-color: #4a9c7b;
  background: #f0faf6;
}

.range-btn.active {
  color: #fff;
  background: linear-gradient(135deg, #4a9c7b 0%, #5fb892 100%);
  border-color: #4a9c7b;
  box-shadow: 0 2px 8px rgba(74, 156, 123, 0.3);
}

/* ==================== 图表区域 ==================== */
.chart-container {
  position: relative;
  min-height: 380px;
  padding: 20px;
}

.chart {
  width: 100%;
  height: 380px;
}

/* ==================== 空状态 ==================== */
.empty-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.8;
}

.empty-text {
  font-size: 16px;
  color: #606266;
  margin: 0 0 8px 0;
}

.empty-hint {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

/* ==================== 数据摘要 ==================== */
.data-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 20px 28px;
  background: #fafbfc;
  border-top: 1px solid #f0f0f0;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.summary-label {
  font-size: 12px;
  color: #909399;
}

.summary-value {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.summary-divider {
  width: 1px;
  height: 30px;
  background: #e4e7ed;
}

.highlight-green {
  color: #4a9c7b;
}

.highlight-amber {
  color: #d4a84b;
}

/* ==================== 分类统计部分 ==================== */
.section-title {
  margin-bottom: 20px;
}

.section-title h2 {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.section-title p {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.category-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.category-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 20px;
}

.card-title-sm {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.category-chart {
  height: 200px;
}

.chart-empty {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 13px;
}

/* ==================== 标签云 ==================== */
.tag-card {
  grid-column: span 1;
}

.tag-cloud {
  height: 200px;
  display: flex;
  flex-wrap: wrap;
  align-content: center;
  justify-content: center;
  gap: 12px;
  padding: 10px;
}

.tag-item {
  display: inline-block;
  padding: 4px 10px;
  background: rgba(74, 156, 123, 0.08);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tag-item:hover {
  background: rgba(74, 156, 123, 0.15);
  transform: scale(1.05);
}

/* ==================== 分享海报区域 ==================== */
.share-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.share-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 12px;
}

.share-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  background: linear-gradient(135deg, #4a9c7b 0%, #5fb892 100%);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.share-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(74, 156, 123, 0.4);
}

.share-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.share-btn.outline {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.share-btn.outline:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

.share-btn .btn-icon {
  font-size: 18px;
}

.share-hint {
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
}

/* ==================== 海报模态框 ==================== */
.poster-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2000;
  display: none;
  align-items: center;
  justify-content: center;
}

.poster-container.visible {
  display: flex;
}

.poster-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
}

.poster-modal {
  position: relative;
  background: #fff;
  border-radius: 16px;
  max-width: 420px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.poster-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.poster-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #e4e7ed;
  color: #303133;
}

/* ==================== 海报内容样式 ==================== */
.poster-content {
  padding: 32px 24px;
  background: linear-gradient(135deg, #1a2a3a 0%, #2d4a5e 100%);
  color: #fff;
}

.poster-top {
  text-align: center;
  margin-bottom: 32px;
}

.poster-year {
  font-size: 48px;
  font-weight: 700;
  color: #d4a84b;
  line-height: 1;
  margin-bottom: 8px;
}

.poster-title {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 4px;
}

.poster-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.poster-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.poster-stat-item {
  text-align: center;
  padding: 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

.poster-stat-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.poster-stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}

.poster-stat-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

.poster-secondary {
  display: flex;
  justify-content: center;
  gap: 32px;
  padding: 16px 0;
  margin-bottom: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.poster-secondary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ps-icon {
  font-size: 16px;
}

.ps-value {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.ps-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.poster-keywords {
  text-align: center;
  margin-bottom: 24px;
}

.poster-keywords-title {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 12px;
}

.poster-keywords-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.poster-keyword {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  font-weight: 500;
}

.poster-footer {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.poster-brand {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* ==================== 海报操作按钮 ==================== */
.poster-actions {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: #fafbfc;
  border-top: 1px solid #f0f0f0;
}

.poster-action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.poster-action-btn:hover:not(:disabled) {
  border-color: #4a9c7b;
  color: #4a9c7b;
}

.poster-action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.poster-action-btn.primary {
  color: #fff;
  background: linear-gradient(135deg, #4a9c7b 0%, #5fb892 100%);
  border-color: #4a9c7b;
}

.poster-action-btn.primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 156, 123, 0.3);
}

.poster-action-btn .btn-icon {
  font-size: 16px;
}

/* ==================== 响应式适配 ==================== */
@media (max-width: 900px) {
  .category-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .tag-card {
    grid-column: span 2;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .analysis-section {
    grid-template-columns: 1fr;
  }

  .annual-report-card .card-header {
    flex-direction: column;
    gap: 16px;
  }

  .year-selector {
    width: 100%;
    overflow-x: auto;
    padding-bottom: 4px;
  }

  .efficiency-metrics {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .statistics-page {
    padding: 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .card-header {
    flex-direction: column;
    gap: 16px;
    padding: 20px;
  }

  .range-buttons {
    width: 100%;
    justify-content: center;
  }

  .chart-container {
    min-height: 300px;
    padding: 16px;
  }

  .chart {
    height: 300px;
  }

  .data-summary {
    flex-wrap: wrap;
    gap: 16px 20px;
    padding: 16px 20px;
  }

  .category-cards {
    grid-template-columns: 1fr;
  }

  .tag-card {
    grid-column: span 1;
  }

  /* 年度报告移动端适配 */
  .annual-report-card {
    padding: 20px;
    border-radius: 16px;
  }

  .annual-report-card .card-title {
    font-size: 20px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    font-size: 24px;
  }

  .stat-value {
    font-size: 22px;
  }

  .secondary-stats {
    gap: 16px;
  }

  .analysis-section {
    grid-template-columns: 1fr;
  }

  .time-bar-container {
    flex-wrap: wrap;
  }

  .time-label {
    width: 100%;
    font-size: 11px;
  }

  .time-bar-wrapper {
    flex: 1;
  }

  .time-count {
    width: auto;
  }

  /* 分享按钮移动端适配 */
  .share-actions {
    flex-direction: column;
    gap: 12px;
  }

  .share-btn {
    width: 100%;
    justify-content: center;
  }

  /* 海报模态框移动端适配 */
  .poster-modal {
    width: 95%;
    max-height: 85vh;
  }

  .poster-content {
    padding: 24px 16px;
  }

  .poster-year {
    font-size: 36px;
  }

  .poster-title {
    font-size: 20px;
  }

  .poster-stats {
    gap: 8px;
  }

  .poster-stat-item {
    padding: 12px;
  }

  .poster-stat-value {
    font-size: 24px;
  }

  .poster-actions {
    flex-direction: column;
    gap: 8px;
    padding: 12px 16px;
  }
}
</style>