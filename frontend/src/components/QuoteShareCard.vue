<template>
  <!-- ==================== 金句分享卡片生成器组件 ==================== -->
  <el-dialog
    v-model="visible"
    title=""
    width="900px"
    :close-on-click-modal="false"
    class="quote-share-dialog"
    @closed="handleClose"
  >
    <!-- 自定义标题 -->
    <template #header>
      <div class="dialog-header">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
          </svg>
        </div>
        <div class="header-text">
          <h3>金句分享卡片</h3>
          <p>为你的珍藏金句生成精美分享图</p>
        </div>
      </div>
    </template>

    <div class="share-container">
      <!-- 左侧：卡片预览区 -->
      <div class="preview-section">
        <div class="preview-label">
          <span class="label-text">实时预览</span>
          <span class="label-hint">点击卡片可切换样式</span>
        </div>

        <!-- 卡片预览容器 -->
        <div class="card-wrapper" ref="cardWrapper">
          <div
            class="quote-card"
            ref="quoteCard"
            :class="[
              `theme-${currentTheme}`,
              `font-${currentFont}`
            ]"
            :style="cardStyle"
          >
            <!-- 背景装饰层 -->
            <div class="card-decoration">
              <!-- 引号装饰 -->
              <div class="quote-mark mark-top">"</div>
              <div class="quote-mark mark-bottom">"</div>

              <!-- 印章装饰 -->
              <div class="seal-stamp">
                <span>读</span>
                <span>书</span>
              </div>

              <!-- 纹理叠加 -->
              <div class="texture-overlay"></div>
            </div>

            <!-- 内容区域 -->
            <div class="card-content">
              <!-- 金句内容 -->
              <div class="quote-text-wrapper">
                <p class="quote-text">{{ quote.content }}</p>
              </div>

              <!-- 分隔线 -->
              <div class="divider">
                <span class="divider-line"></span>
                <span class="divider-icon">❋</span>
                <span class="divider-line"></span>
              </div>

              <!-- 来源信息 -->
              <div class="source-info">
                <span class="book-title">《{{ quote.bookTitle || '未知书籍' }}》</span>
                <span v-if="quote.bookAuthor || quote.author" class="author">{{ quote.bookAuthor || quote.author }}</span>
              </div>

              <!-- 章节页码 -->
              <div v-if="quote.chapter || quote.pageNumber" class="location-info">
                <span v-if="quote.chapter">{{ quote.chapter }}</span>
                <span v-if="quote.chapter && quote.pageNumber"> · </span>
                <span v-if="quote.pageNumber">第{{ quote.pageNumber }}页</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：样式设置面板 -->
      <div class="settings-section">
        <!-- 背景主题 -->
        <div class="setting-group">
          <label class="setting-label">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
            </svg>
            背景主题
          </label>
          <div class="theme-grid">
            <div
              v-for="theme in themes"
              :key="theme.value"
              :class="['theme-item', { active: currentTheme === theme.value }]"
              @click="currentTheme = theme.value"
            >
              <div class="theme-preview" :style="{ background: theme.gradient }">
                <div class="theme-icon">{{ theme.icon }}</div>
              </div>
              <span class="theme-name">{{ theme.label }}</span>
            </div>
          </div>
        </div>

        <!-- 字体风格 -->
        <div class="setting-group">
          <label class="setting-label">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="4 7 4 4 20 4 20 7"/>
              <line x1="9" y1="20" x2="15" y2="20"/>
              <line x1="12" y1="4" x2="12" y2="20"/>
            </svg>
            字体风格
          </label>
          <div class="font-options">
            <div
              v-for="font in fonts"
              :key="font.value"
              :class="['font-item', { active: currentFont === font.value }]"
              @click="currentFont = font.value"
            >
              <span class="font-preview" :style="{ fontFamily: font.family }">永</span>
              <span class="font-name">{{ font.label }}</span>
            </div>
          </div>
        </div>

        <!-- 文字颜色 -->
        <div class="setting-group">
          <label class="setting-label">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/>
            </svg>
            文字颜色
          </label>
          <div class="color-picker-wrapper">
            <div class="color-presets">
              <div
                v-for="color in colorPresets"
                :key="color"
                :class="['color-preset', { active: textColor === color }]"
                :style="{ backgroundColor: color }"
                @click="textColor = color"
              ></div>
            </div>
            <el-color-picker
              v-model="textColor"
              :predefine="colorPresets"
              size="small"
            />
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button
            class="action-btn copy-btn"
            @click="copyText"
            :loading="copying"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
            </svg>
            复制文本
          </el-button>
          <el-button
            type="primary"
            class="action-btn download-btn"
            @click="downloadCard"
            :loading="downloading"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            下载图片
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
/**
 * 金句分享卡片生成器组件
 * 用于生成精美的金句分享卡片图片
 * 支持多种背景主题、字体风格和颜色自定义
 */
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import html2canvas from 'html2canvas'

// ==================== Props 定义 ====================

const props = defineProps({
  /** 对话框可见性 */
  modelValue: {
    type: Boolean,
    default: false
  },
  /** 金句数据 */
  quote: {
    type: Object,
    required: true,
    default: () => ({
      content: '',
      bookTitle: '',
      bookAuthor: '',  // 书籍作者
      author: '',      // 兼容旧字段名
      chapter: '',
      pageNumber: null
    })
  }
})

// ==================== Emits 定义 ====================

const emit = defineEmits(['update:modelValue', 'download-success', 'copy-success'])

// ==================== 响应式状态 ====================

/** 对话框可见性（双向绑定） */
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

/** 卡片DOM引用 */
const quoteCard = ref(null)
const cardWrapper = ref(null)

/** 当前主题 */
const currentTheme = ref('ink-wash')

/** 当前字体 */
const currentFont = ref('elegant')

/** 文字颜色 */
const textColor = ref('#2c1810')

/** 下载状态 */
const downloading = ref(false)

/** 复制状态 */
const copying = ref(false)

// ==================== 主题配置 ====================

/** 背景主题列表 */
const themes = [
  {
    value: 'ink-wash',
    label: '水墨',
    icon: '墨',
    gradient: 'linear-gradient(135deg, #e8e4df 0%, #d4cfc7 50%, #c9c4bc 100%)'
  },
  {
    value: 'warm-sun',
    label: '暖阳',
    icon: '阳',
    gradient: 'linear-gradient(135deg, #fef3c7 0%, #fde68a 50%, #fbbf24 100%)'
  },
  {
    value: 'starlight',
    label: '星空',
    icon: '星',
    gradient: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #4338ca 100%)'
  },
  {
    value: 'forest',
    label: '森林',
    icon: '林',
    gradient: 'linear-gradient(135deg, #1a3a2f 0%, #2d5a47 50%, #3d7a5f 100%)'
  },
  {
    value: 'twilight',
    label: '暮光',
    icon: '暮',
    gradient: 'linear-gradient(135deg, #7c3aed 0%, #a855f7 50%, #ec4899 100%)'
  }
]

/** 字体风格列表 */
const fonts = [
  { value: 'elegant', label: '宋体优雅', family: '"Noto Serif SC", "Source Han Serif CN", serif' },
  { value: 'modern', label: '黑体现代', family: '"Noto Sans SC", "Source Han Sans CN", sans-serif' },
  { value: 'literary', label: '楷体文艺', family: '"Kaiti SC", "STKaiti", "楷体", serif' }
]

/** 颜色预设 */
const colorPresets = [
  '#2c1810', // 深棕
  '#1f2937', // 深灰
  '#ffffff', // 白色
  '#fef3c7', // 米黄
  '#fbbf24', // 金色
  '#dc2626'  // 朱红
]

// ==================== 计算属性 ====================

/** 卡片样式 */
const cardStyle = computed(() => ({
  '--text-color': textColor.value
}))

// ==================== 方法定义 ====================

/**
 * 下载分享卡片
 * 使用 html2canvas 将卡片转换为图片
 */
async function downloadCard() {
  if (!quoteCard.value) {
    ElMessage.error('卡片加载失败，请重试')
    return
  }

  downloading.value = true

  try {
    // 使用 html2canvas 生成图片
    const canvas = await html2canvas(quoteCard.value, {
      width: 600,
      height: 600,
      scale: 2, // 高清输出
      backgroundColor: null,
      useCORS: true,
      logging: false
    })

    // 创建下载链接
    const link = document.createElement('a')
    link.download = `金句分享-${props.quote.bookTitle || '读书笔记'}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()

    ElMessage.success('图片已保存')
    emit('download-success')
  } catch (err) {
    console.error('下载图片失败:', err)
    ElMessage.error('下载失败，请重试')
  } finally {
    downloading.value = false
  }
}

/**
 * 复制金句文本到剪贴板
 */
async function copyText() {
  copying.value = true

  try {
    // 构建分享文本
    let shareText = `"${props.quote.content}"\n\n`
    shareText += `—— 《${props.quote.bookTitle || '未知书籍'}》`

    const author = props.quote.bookAuthor || props.quote.author
    if (author) {
      shareText += ` ${author}`
    }

    // 复制到剪贴板
    await navigator.clipboard.writeText(shareText)

    ElMessage.success('已复制到剪贴板')
    emit('copy-success')
  } catch (err) {
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = `"${props.quote.content}"\n\n—— 《${props.quote.bookTitle || '未知书籍'}》`
    textArea.style.position = 'fixed'
    textArea.style.left = '-9999px'
    document.body.appendChild(textArea)
    textArea.select()

    try {
      document.execCommand('copy')
      ElMessage.success('已复制到剪贴板')
      emit('copy-success')
    } catch (e) {
      ElMessage.error('复制失败，请手动复制')
    } finally {
      document.body.removeChild(textArea)
    }
  } finally {
    copying.value = false
  }
}

/**
 * 关闭对话框
 */
function handleClose() {
  visible.value = false
}

// ==================== 侦听器 ====================

/** 对话框打开时重置样式 */
watch(visible, (val) => {
  if (val) {
    currentTheme.value = 'ink-wash'
    currentFont.value = 'elegant'
    textColor.value = '#2c1810'
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Noto+Sans+SC:wght@400;500;700&display=swap');

/* ==================== 对话框样式 ==================== */
.quote-share-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.quote-share-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

.quote-share-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem 2rem;
  background: linear-gradient(135deg, #faf8f5 0%, #f5f1eb 100%);
  border-bottom: 1px solid #e8ddd0;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon svg {
  width: 24px;
  height: 24px;
  color: #fef3c7;
}

.header-text h3 {
  margin: 0;
  font-family: 'Noto Serif SC', serif;
  font-size: 1.25rem;
  font-weight: 600;
  color: #2c1810;
}

.header-text p {
  margin: 0.25rem 0 0;
  font-size: 0.875rem;
  color: #6b5344;
}

/* ==================== 主容器 ==================== */
.share-container {
  display: flex;
  min-height: 500px;
}

/* ==================== 预览区域 ==================== */
.preview-section {
  flex: 1;
  padding: 2rem;
  background: #faf8f5;
  display: flex;
  flex-direction: column;
}

.preview-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.label-text {
  font-weight: 600;
  color: #2c1810;
  font-size: 0.9375rem;
}

.label-hint {
  font-size: 0.8125rem;
  color: #9ca3af;
}

.card-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: repeating-linear-gradient(
    45deg,
    #f5f1eb,
    #f5f1eb 10px,
    #faf8f5 10px,
    #faf8f5 20px
  );
  border-radius: 12px;
  padding: 1.5rem;
}

/* ==================== 卡片样式 ==================== */
.quote-card {
  width: 600px;
  height: 600px;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 主题样式 */
.quote-card.theme-ink-wash {
  background: linear-gradient(135deg, #e8e4df 0%, #d4cfc7 50%, #c9c4bc 100%);
}

.quote-card.theme-warm-sun {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 50%, #fbbf24 100%);
}

.quote-card.theme-starlight {
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #4338ca 100%);
}

.quote-card.theme-forest {
  background: linear-gradient(135deg, #1a3a2f 0%, #2d5a47 50%, #3d7a5f 100%);
}

.quote-card.theme-twilight {
  background: linear-gradient(135deg, #7c3aed 0%, #a855f7 50%, #ec4899 100%);
}

/* 装饰层 */
.card-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

/* 引号装饰 */
.quote-mark {
  position: absolute;
  font-family: 'Noto Serif SC', serif;
  font-size: 8rem;
  font-weight: 700;
  opacity: 0.15;
  line-height: 1;
}

.quote-mark.mark-top {
  top: 1.5rem;
  left: 2rem;
}

.quote-mark.mark-bottom {
  bottom: 1.5rem;
  right: 2rem;
}

/* 星空主题特殊处理 */
.theme-starlight .quote-mark {
  color: #fef3c7;
}

/* 印章装饰 */
.seal-stamp {
  position: absolute;
  right: 2rem;
  top: 2rem;
  width: 48px;
  height: 48px;
  border: 2px solid currentColor;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0.3;
  transform: rotate(-5deg);
}

.seal-stamp span {
  font-family: 'Noto Serif SC', serif;
  font-size: 0.875rem;
  font-weight: 600;
  line-height: 1.1;
}

/* 纹理叠加 */
.texture-overlay {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 400 400' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
  opacity: 0.03;
  mix-blend-mode: overlay;
}

/* 内容区域 */
.card-content {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  color: var(--text-color, #2c1810);
  text-align: center;
  z-index: 1;
}

/* 字体风格 */
.font-elegant .card-content {
  font-family: 'Noto Serif SC', serif;
}

.font-modern .card-content {
  font-family: 'Noto Sans SC', sans-serif;
}

.font-literary .card-content {
  font-family: 'Kaiti SC', 'STKaiti', '楷体', serif;
}

/* 金句文本 */
.quote-text-wrapper {
  max-width: 480px;
}

.quote-text {
  font-size: 1.75rem;
  line-height: 2;
  font-weight: 400;
  letter-spacing: 0.05em;
  margin: 0;
  word-break: break-word;
}

/* 分隔线 */
.divider {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 2.5rem 0 1.5rem;
  opacity: 0.4;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: currentColor;
}

.divider-icon {
  font-size: 1rem;
}

/* 来源信息 */
.source-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 1rem;
  opacity: 0.8;
}

.book-title {
  font-weight: 600;
}

.author {
  font-size: 0.9375rem;
}

.location-info {
  margin-top: 0.75rem;
  font-size: 0.875rem;
  opacity: 0.6;
}

/* 星空主题特殊文字颜色 */
.theme-starlight .card-content,
.theme-forest .card-content,
.theme-twilight .card-content {
  color: var(--text-color, #fef3c7);
}

/* ==================== 设置面板 ==================== */
.settings-section {
  width: 280px;
  padding: 2rem;
  background: white;
  border-left: 1px solid #e8ddd0;
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.setting-group {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.setting-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: #2c1810;
}

.setting-label svg {
  width: 16px;
  height: 16px;
  opacity: 0.6;
}

/* 主题选择网格 */
.theme-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}

.theme-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.theme-preview {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.theme-item:hover .theme-preview {
  transform: scale(1.05);
}

.theme-item.active .theme-preview {
  border-color: #8b6f47;
  box-shadow: 0 4px 12px rgba(139, 111, 71, 0.3);
}

.theme-icon {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.125rem;
  font-weight: 600;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.theme-name {
  font-size: 0.75rem;
  color: #6b5344;
}

/* 字体选择 */
.font-options {
  display: flex;
  gap: 0.75rem;
}

.font-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  border-radius: 8px;
  border: 1px solid #e8ddd0;
  cursor: pointer;
  transition: all 0.2s ease;
}

.font-item:hover {
  border-color: #8b6f47;
  background: #faf8f5;
}

.font-item.active {
  border-color: #8b6f47;
  background: linear-gradient(135deg, #faf8f5 0%, #f5f1eb 100%);
}

.font-preview {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2c1810;
  color: white;
  border-radius: 6px;
  font-size: 1.25rem;
  font-weight: 600;
}

.font-name {
  font-size: 0.6875rem;
  color: #6b5344;
  white-space: nowrap;
}

/* 颜色选择 */
.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.color-presets {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.color-preset {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.color-preset:hover {
  transform: scale(1.1);
}

.color-preset.active {
  border-color: #2c1810;
  box-shadow: 0 0 0 2px rgba(44, 24, 16, 0.2);
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid #e8ddd0;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.875rem 1.5rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.9375rem;
  transition: all 0.3s ease;
}

.action-btn svg {
  width: 18px;
  height: 18px;
}

.copy-btn {
  background: #faf8f5;
  border: 1px solid #e8ddd0;
  color: #6b5344;
}

.copy-btn:hover {
  background: #f5f1eb;
  border-color: #8b6f47;
  color: #2c1810;
}

.download-btn {
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  border: none;
  color: white;
}

.download-btn:hover {
  background: linear-gradient(135deg, #6b5344 0%, #5a4538 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(107, 83, 68, 0.4);
}

/* ==================== 响应式 ==================== */
@media (max-width: 900px) {
  .share-container {
    flex-direction: column;
  }

  .preview-section {
    min-height: 400px;
  }

  .card-wrapper {
    transform: scale(0.7);
    transform-origin: center center;
  }

  .settings-section {
    width: 100%;
    border-left: none;
    border-top: 1px solid #e8ddd0;
  }
}
</style>