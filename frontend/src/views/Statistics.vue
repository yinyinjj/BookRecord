<template>
  <div class="statistics-page">
    <!-- ==================== 页面头部 ==================== -->
    <div class="page-header">
      <h1 class="page-title">阅读统计</h1>
      <p class="page-subtitle">记录阅读足迹，见证成长轨迹</p>
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

/** ECharts 实例 */
let trendChartInstance = null
let statusChartInstance = null
let noteTypeChartInstance = null

/** 当前选中的时间范围 */
const currentRange = ref('1y')

/** 趋势数据 */
const trendData = ref([])

/** 分类统计数据 */
const statusData = ref([])
const noteTypeData = ref([])
const topTags = ref([])

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

// ==================== 生命周期钩子 ====================

/**
 * 组件挂载时加载数据
 */
onMounted(() => {
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

/* ==================== 响应式适配 ==================== */
@media (max-width: 900px) {
  .category-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .tag-card {
    grid-column: span 2;
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
}
</style>