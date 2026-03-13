<template>
  <!-- ==================== 分享页面主容器 ==================== -->
  <div class="share-page">
    <!-- 装饰性背景层 -->
    <div class="decorative-layer">
      <div class="corner-ornament top-left"></div>
      <div class="corner-ornament top-right"></div>
      <div class="corner-ornament bottom-left"></div>
      <div class="corner-ornament bottom-right"></div>
    </div>

    <!-- 主内容区域 -->
    <main class="share-content" v-if="!loading && !error">
      <!-- 书籍信息横幅 -->
      <header class="book-banner" :style="{ '--accent-color': contentColor }">
        <div class="banner-decoration left"></div>
        <div class="banner-content">
          <span class="book-icon">📖</span>
          <div class="book-info">
            <h2 class="book-title">{{ content.bookTitle }}</h2>
            <p class="book-author" v-if="content.bookAuthor">{{ content.bookAuthor }}</p>
          </div>
        </div>
        <div class="banner-decoration right"></div>
      </header>

      <!-- 感悟类型展示 -->
      <section class="content-section note-type-section" v-if="content.resourceType === 'NOTE'">
        <div class="type-badge" :class="content.noteType?.toLowerCase()">
          {{ noteTypeLabel }}
        </div>
      </section>

      <!-- 内容卡片 -->
      <article class="content-card">
        <!-- 金句内容 -->
        <div class="quote-wrapper" v-if="content.resourceType === 'QUOTE'">
          <blockquote class="quote-content">
            <span class="quote-mark left">"</span>
            <p>{{ content.quoteContent }}</p>
            <span class="quote-mark right">"</span>
          </blockquote>
          <div class="quote-note" v-if="content.quoteNote">
            <span class="note-label">注：</span>
            {{ content.quoteNote }}
          </div>
        </div>

        <!-- 感悟内容 -->
        <div class="note-wrapper" v-else>
          <h3 class="note-title" v-if="content.title">{{ content.title }}</h3>
          <div class="note-content" v-html="formattedContent"></div>
        </div>

        <!-- 章节页码信息 -->
        <div class="location-info" v-if="content.chapter || content.pageNumber">
          <span class="location-item" v-if="content.chapter">
            <span class="location-icon">📑</span>
            {{ content.chapter }}
          </span>
          <span class="location-divider" v-if="content.chapter && content.pageNumber">·</span>
          <span class="location-item" v-if="content.pageNumber">
            <span class="location-icon">📄</span>
            第 {{ content.pageNumber }} 页
          </span>
        </div>

        <!-- 标签 -->
        <div class="tags-wrapper" v-if="content.tags">
          <span class="tag" v-for="tag in parsedTags" :key="tag">
            {{ tag }}
          </span>
        </div>
      </article>

      <!-- 底部信息 -->
      <footer class="share-footer">
        <div class="footer-decoration"></div>
        <div class="footer-content">
          <span class="share-time">
            {{ formatDate(content.createdAt) }}
          </span>
          <span class="share-divider">|</span>
          <span class="share-by">
            {{ content.sharedBy }} 分享
          </span>
        </div>
      </footer>
    </main>

    <!-- 加载状态 -->
    <div class="loading-state" v-if="loading">
      <div class="scroll-loader">
        <div class="scroll-paper"></div>
        <div class="scroll-roller left"></div>
        <div class="scroll-roller right"></div>
      </div>
      <p class="loading-text">展开书卷中...</p>
    </div>

    <!-- 错误状态 -->
    <div class="error-state" v-if="error">
      <div class="error-icon">📜</div>
      <h3 class="error-title">{{ errorTitle }}</h3>
      <p class="error-message">{{ errorMessage }}</p>
      <router-link to="/" class="home-link">返回首页</router-link>
    </div>
  </div>
</template>

<script setup>
/**
 * 分享页面组件
 * 公开展示分享的感悟或金句内容，无需登录
 * 设计风格：优雅的书卷风格，古籍展开效果
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { shareApi } from '@/api/modules'

// ==================== 路由和状态 ====================

const route = useRoute()

/** 分享内容数据 */
const content = ref({})

/** 加载状态 */
const loading = ref(true)

/** 错误信息 */
const error = ref(null)

// ==================== 计算属性 ====================

/**
 * 感悟类型标签
 */
const noteTypeLabel = computed(() => {
  const types = {
    THOUGHT: '💭 感悟',
    SUMMARY: '📝 总结',
    QUESTION: '❓ 疑问',
    INSIGHT: '💡 洞见'
  }
  return types[content.value.noteType] || '💭 感悟'
})

/**
 * 内容主题色（根据金句颜色或感悟类型）
 */
const contentColor = computed(() => {
  if (content.value.resourceType === 'QUOTE') {
    const colors = {
      gold: '#D4A574',
      blue: '#5B8C85',
      green: '#6B8E6B',
      purple: '#8B7BA8',
      red: '#C17B7B'
    }
    return colors[content.value.quoteColor] || '#8B6914'
  }
  const typeColors = {
    THOUGHT: '#6B5B4F',
    SUMMARY: '#4A6741',
    QUESTION: '#5B6B8C',
    INSIGHT: '#7B6B8B'
  }
  return typeColors[content.value.noteType] || '#6B5B4F'
})

/**
 * 格式化内容（换行转段落）
 */
const formattedContent = computed(() => {
  if (!content.value.content) return ''
  return content.value.content
    .split('\n')
    .filter(p => p.trim())
    .map(p => `<p>${p}</p>`)
    .join('')
})

/**
 * 解析标签列表
 */
const parsedTags = computed(() => {
  if (!content.value.tags) return []
  return content.value.tags.split(',').map(t => t.trim()).filter(Boolean)
})

/**
 * 错误标题
 */
const errorTitle = computed(() => {
  if (error.value === 'expired') return '分享已过期'
  if (error.value === 'not_found') return '分享不存在'
  if (error.value === 'private') return '内容不可分享'
  return '加载失败'
})

/**
 * 错误详细信息
 */
const errorMessage = computed(() => {
  if (error.value === 'expired') return '该分享链接已超过有效期，无法访问'
  if (error.value === 'not_found') return '分享链接不存在或已被删除'
  if (error.value === 'private') return '此内容已被设为私密，无法公开分享'
  return '请稍后重试或联系分享者'
})

// ==================== 方法定义 ====================

/**
 * 加载分享内容
 */
async function loadSharedContent() {
  const token = route.params.token || route.query.token

  if (!token) {
    error.value = 'not_found'
    loading.value = false
    return
  }

  try {
    loading.value = true
    const response = await shareApi.getSharedContent(token)
    content.value = response.data
  } catch (err) {
    console.error('加载分享内容失败:', err)

    // 根据错误信息判断类型
    const message = err.response?.data?.message || err.message || ''
    if (message.includes('过期')) {
      error.value = 'expired'
    } else if (message.includes('不存在') || message.includes('失效')) {
      error.value = 'not_found'
    } else if (message.includes('私密')) {
      error.value = 'private'
    } else {
      error.value = 'unknown'
    }
  } finally {
    loading.value = false
  }
}

/**
 * 格式化日期
 * @param {string} dateStr - 日期字符串
 * @returns {string} 格式化后的日期
 */
function formatDate(dateStr) {
  if (!dateStr) return ''

  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}年${month}月${day}日`
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadSharedContent()
})
</script>

<style scoped>
/* ==================== CSS 变量定义 ==================== */
.share-page {
  --paper-bg: #F8F4EC;
  --paper-texture: #EDE8DC;
  --ink-color: #2C2416;
  --ink-light: #5C4A36;
  --accent-gold: #8B6914;
  --accent-red: #8B3A3A;
  --shadow-soft: rgba(44, 36, 22, 0.1);
  --shadow-deep: rgba(44, 36, 22, 0.2);
  --border-ornament: #C4B896;

  min-height: 100vh;
  background: var(--paper-bg);
  position: relative;
  overflow-x: hidden;

  /* 纸张纹理背景 */
  background-image:
    radial-gradient(ellipse at 20% 30%, rgba(139, 105, 20, 0.03) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 70%, rgba(139, 105, 20, 0.03) 0%, transparent 50%),
    url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.04'/%3E%3C/svg%3E");
}

/* ==================== 装饰性元素 ==================== */
.decorative-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.corner-ornament {
  position: absolute;
  width: 120px;
  height: 120px;
  opacity: 0.15;
  background-size: contain;
  background-repeat: no-repeat;
}

.corner-ornament.top-left {
  top: 20px;
  left: 20px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Cpath d='M10,10 Q50,10 50,50 Q10,50 10,10' fill='none' stroke='%238B6914' stroke-width='2'/%3E%3Cpath d='M15,15 Q45,15 45,45 Q15,45 15,15' fill='none' stroke='%238B6914' stroke-width='1'/%3E%3C/svg%3E");
}

.corner-ornament.top-right {
  top: 20px;
  right: 20px;
  transform: scaleX(-1);
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Cpath d='M10,10 Q50,10 50,50 Q10,50 10,10' fill='none' stroke='%238B6914' stroke-width='2'/%3E%3Cpath d='M15,15 Q45,15 45,45 Q15,45 15,15' fill='none' stroke='%238B6914' stroke-width='1'/%3E%3C/svg%3E");
}

.corner-ornament.bottom-left {
  bottom: 20px;
  left: 20px;
  transform: scaleY(-1);
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Cpath d='M10,10 Q50,10 50,50 Q10,50 10,10' fill='none' stroke='%238B6914' stroke-width='2'/%3E%3Cpath d='M15,15 Q45,15 45,45 Q15,45 15,15' fill='none' stroke='%238B6914' stroke-width='1'/%3E%3C/svg%3E");
}

.corner-ornament.bottom-right {
  bottom: 20px;
  right: 20px;
  transform: scale(-1);
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Cpath d='M10,10 Q50,10 50,50 Q10,50 10,10' fill='none' stroke='%238B6914' stroke-width='2'/%3E%3Cpath d='M15,15 Q45,15 45,45 Q15,45 15,15' fill='none' stroke='%238B6914' stroke-width='1'/%3E%3C/svg%3E");
}

/* ==================== 主内容区域 ==================== */
.share-content {
  max-width: 720px;
  margin: 0 auto;
  padding: 60px 24px;
  position: relative;
  z-index: 2;

  /* 入场动画 */
  animation: unrollScroll 0.8s ease-out;
}

@keyframes unrollScroll {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ==================== 书籍信息横幅 ==================== */
.book-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40px;
  position: relative;
  animation: fadeInDown 0.6s ease-out 0.2s both;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.banner-decoration {
  flex: 1;
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent,
    var(--accent-gold) 20%,
    var(--accent-gold) 80%,
    transparent
  );
  position: relative;
}

.banner-decoration::before,
.banner-decoration::after {
  content: '';
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  background: var(--accent-gold);
  border-radius: 50%;
}

.banner-decoration.left::before { right: 0; }
.banner-decoration.right::after { left: 0; }

.banner-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 24px;
}

.book-icon {
  font-size: 28px;
  opacity: 0.9;
}

.book-info {
  text-align: center;
}

.book-title {
  font-family: 'Noto Serif SC', 'Source Han Serif CN', serif;
  font-size: 24px;
  font-weight: 600;
  color: var(--ink-color);
  margin: 0;
  letter-spacing: 0.05em;
}

.book-author {
  font-family: 'Noto Serif SC', serif;
  font-size: 14px;
  color: var(--ink-light);
  margin: 4px 0 0;
}

/* ==================== 感悟类型标签 ==================== */
.note-type-section {
  text-align: center;
  margin-bottom: 24px;
  animation: fadeIn 0.5s ease-out 0.3s both;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.type-badge {
  display: inline-block;
  padding: 8px 20px;
  font-family: 'Noto Serif SC', serif;
  font-size: 14px;
  color: var(--ink-light);
  background: var(--paper-texture);
  border: 1px solid var(--border-ornament);
  border-radius: 20px;

  /* 各类型的主题色 */
  --badge-color: var(--ink-light);
}

.type-badge.thought { --badge-color: #6B5B4F; }
.type-badge.summary { --badge-color: #4A6741; }
.type-badge.question { --badge-color: #5B6B8C; }
.type-badge.insight { --badge-color: #7B6B8B; }

.type-badge::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background: var(--badge-color);
  border-radius: 50%;
  margin-right: 8px;
  vertical-align: middle;
}

/* ==================== 内容卡片 ==================== */
.content-card {
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.8) 0%,
    rgba(248, 244, 236, 0.9) 100%
  );
  border: 1px solid var(--border-ornament);
  border-radius: 8px;
  padding: 40px;
  box-shadow:
    0 4px 20px var(--shadow-soft),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  position: relative;
  animation: fadeInUp 0.6s ease-out 0.4s both;

  /* 装饰性边框 */
  &::before {
    content: '';
    position: absolute;
    inset: 8px;
    border: 1px solid rgba(139, 105, 20, 0.15);
    border-radius: 4px;
    pointer-events: none;
  }
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

/* ==================== 金句展示 ==================== */
.quote-wrapper {
  text-align: center;
}

.quote-content {
  position: relative;
  margin: 0;
  padding: 20px 40px;
}

.quote-mark {
  font-family: Georgia, 'Noto Serif SC', serif;
  font-size: 48px;
  color: var(--accent-gold);
  opacity: 0.6;
  line-height: 1;
  position: absolute;
}

.quote-mark.left {
  top: 0;
  left: 0;
}

.quote-mark.right {
  bottom: -20px;
  right: 0;
}

.quote-content p {
  font-family: 'Noto Serif SC', 'Source Han Serif CN', serif;
  font-size: 22px;
  line-height: 2;
  color: var(--ink-color);
  margin: 0;
  letter-spacing: 0.03em;
  font-style: italic;
}

.quote-note {
  margin-top: 24px;
  padding: 16px;
  background: rgba(139, 105, 20, 0.05);
  border-left: 3px solid var(--accent-gold);
  border-radius: 0 4px 4px 0;
  font-family: 'Noto Serif SC', serif;
  font-size: 14px;
  color: var(--ink-light);
  text-align: left;
}

.note-label {
  color: var(--accent-gold);
  font-weight: 500;
}

/* ==================== 感悟展示 ==================== */
.note-wrapper {
  text-align: left;
}

.note-title {
  font-family: 'Noto Serif SC', 'Source Han Serif CN', serif;
  font-size: 20px;
  font-weight: 600;
  color: var(--ink-color);
  margin: 0 0 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-ornament);
  letter-spacing: 0.05em;
}

.note-content {
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  line-height: 1.9;
  color: var(--ink-color);
}

.note-content :deep(p) {
  margin: 0 0 16px;
  text-indent: 2em;
}

.note-content :deep(p:last-child) {
  margin-bottom: 0;
}

/* ==================== 章节页码信息 ==================== */
.location-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px dashed var(--border-ornament);
}

.location-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: 'Noto Serif SC', serif;
  font-size: 14px;
  color: var(--ink-light);
}

.location-icon {
  font-size: 14px;
}

.location-divider {
  color: var(--border-ornament);
}

/* ==================== 标签 ==================== */
.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
}

.tag {
  padding: 4px 12px;
  font-family: 'Noto Serif SC', serif;
  font-size: 12px;
  color: var(--ink-light);
  background: rgba(139, 105, 20, 0.08);
  border-radius: 12px;
  transition: all 0.2s ease;
}

.tag:hover {
  background: rgba(139, 105, 20, 0.15);
  transform: translateY(-1px);
}

/* ==================== 底部信息 ==================== */
.share-footer {
  margin-top: 40px;
  text-align: center;
  animation: fadeIn 0.5s ease-out 0.6s both;
}

.footer-decoration {
  width: 60px;
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent,
    var(--accent-gold),
    transparent
  );
  margin: 0 auto 16px;
}

.footer-content {
  font-family: 'Noto Serif SC', serif;
  font-size: 13px;
  color: var(--ink-light);
}

.share-divider {
  margin: 0 12px;
  opacity: 0.4;
}

/* ==================== 加载状态 ==================== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40px;
}

.scroll-loader {
  position: relative;
  width: 120px;
  height: 80px;
}

.scroll-paper {
  width: 80px;
  height: 60px;
  margin: 10px auto 0;
  background: var(--paper-texture);
  border: 1px solid var(--border-ornament);
  border-radius: 4px;
  animation: unroll 1.5s ease-in-out infinite;
}

@keyframes unroll {
  0%, 100% { width: 40px; }
  50% { width: 100px; }
}

.scroll-roller {
  position: absolute;
  top: 5px;
  width: 12px;
  height: 70px;
  background: linear-gradient(
    90deg,
    #C4A574,
    #8B6914,
    #C4A574
  );
  border-radius: 6px;
  animation: roll 1.5s ease-in-out infinite;
}

.scroll-roller.left { left: 0; }
.scroll-roller.right { right: 0; }

@keyframes roll {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(10px); }
}

.scroll-roller.right {
  animation-direction: reverse;
}

.loading-text {
  margin-top: 24px;
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  color: var(--ink-light);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

/* ==================== 错误状态 ==================== */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40px;
  text-align: center;
}

.error-icon {
  font-size: 64px;
  margin-bottom: 24px;
  opacity: 0.8;
}

.error-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  color: var(--ink-color);
  margin: 0 0 12px;
}

.error-message {
  font-family: 'Noto Serif SC', serif;
  font-size: 14px;
  color: var(--ink-light);
  margin: 0 0 32px;
  max-width: 300px;
}

.home-link {
  display: inline-block;
  padding: 12px 32px;
  font-family: 'Noto Serif SC', serif;
  font-size: 14px;
  color: var(--paper-bg);
  background: var(--accent-gold);
  border-radius: 24px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.home-link:hover {
  background: var(--ink-color);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--shadow-deep);
}

/* ==================== 响应式布局 ==================== */
@media (max-width: 768px) {
  .share-content {
    padding: 40px 16px;
  }

  .book-title {
    font-size: 20px;
  }

  .content-card {
    padding: 24px;
  }

  .quote-content p {
    font-size: 18px;
  }

  .note-title {
    font-size: 18px;
  }

  .corner-ornament {
    width: 80px;
    height: 80px;
  }
}

@media (max-width: 480px) {
  .banner-content {
    flex-direction: column;
    gap: 8px;
  }

  .quote-mark {
    font-size: 32px;
  }

  .quote-content {
    padding: 16px 24px;
  }

  .quote-content p {
    font-size: 16px;
  }
}
</style>