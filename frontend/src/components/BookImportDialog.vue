<template>
  <!-- 书单导入对话框
       支持豆瓣/微信读书CSV文件导入，提供预览、选择和批量导入功能 -->
  <el-dialog
    :model-value="visible"
    title="导入书单"
    width="880px"
    custom-class="book-import-dialog"
    @update:model-value="handleClose"
  >
    <div class="import-container">
      <!-- ==================== 步骤指示器 ==================== -->
      <div class="steps-indicator">
        <div
          v-for="(step, index) in steps"
          :key="index"
          :class="['step-item', { active: currentStep === index, completed: currentStep > index }]"
        >
          <div class="step-number">{{ index + 1 }}</div>
          <span class="step-label">{{ step }}</span>
          <div v-if="index < steps.length - 1" class="step-line"></div>
        </div>
      </div>

      <!-- ==================== 步骤1: 文件上传 ==================== -->
      <div v-show="currentStep === 0" class="step-content">
        <div class="upload-section">
          <!-- 拖拽上传区域 -->
          <div
            :class="['upload-area', { 'drag-over': isDragOver }]"
            @dragover.prevent="isDragOver = true"
            @dragleave.prevent="isDragOver = false"
            @drop.prevent="handleFileDrop"
            @click="triggerFileInput"
          >
            <div class="upload-icon">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/>
                <line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
            </div>
            <p class="upload-text">拖拽文件到此处，或<span class="upload-link">点击选择</span></p>
            <p class="upload-hint">支持豆瓣、微信读书导出的 CSV 文件</p>
            <input
              ref="fileInput"
              type="file"
              accept=".csv,.json"
              hidden
              @change="handleFileSelect"
            />
          </div>

          <!-- 已选文件显示 -->
          <transition name="slide-up">
            <div v-if="selectedFile" class="selected-file">
              <div class="file-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                </svg>
              </div>
              <div class="file-info">
                <p class="file-name">{{ selectedFile.name }}</p>
                <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
              </div>
              <button class="file-remove" @click="clearSelectedFile">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>
          </transition>

          <!-- 来源选择 -->
          <div class="source-selector">
            <span class="source-label">文件来源：</span>
            <div class="source-options">
              <button
                :class="['source-btn', { active: sourceType === 'auto' }]"
                @click="sourceType = 'auto'"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
                  <path d="M12 17h.01"/>
                </svg>
                <span>自动识别</span>
              </button>
              <button
                :class="['source-btn', { active: sourceType === 'douban' }]"
                @click="sourceType = 'douban'"
              >
                <span class="source-icon-douban">豆</span>
                <span>豆瓣</span>
              </button>
              <button
                :class="['source-btn', { active: sourceType === 'weread' }]"
                @click="sourceType = 'weread'"
              >
                <span class="source-icon-weread">微</span>
                <span>微信读书</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- ==================== 步骤2: 预览选择 ==================== -->
      <div v-show="currentStep === 1" class="step-content">
        <!-- 加载中 -->
        <div v-if="previewLoading" class="loading-container">
          <div class="loading-spinner"></div>
          <p class="loading-text">正在解析文件...</p>
        </div>

        <!-- 预览列表 -->
        <div v-else-if="previewData.items.length > 0" class="preview-section">
          <!-- 统计摘要 -->
          <div class="preview-summary">
            <div class="summary-item success">
              <span class="summary-count">{{ previewData.successCount }}</span>
              <span class="summary-label">可导入</span>
            </div>
            <div v-if="previewData.duplicateCount > 0" class="summary-item warning">
              <span class="summary-count">{{ previewData.duplicateCount }}</span>
              <span class="summary-label">重复</span>
            </div>
            <div v-if="previewData.errorCount > 0" class="summary-item error">
              <span class="summary-count">{{ previewData.errorCount }}</span>
              <span class="summary-label">解析失败</span>
            </div>
          </div>

          <!-- 重复处理策略 -->
          <div v-if="previewData.duplicateCount > 0" class="duplicate-strategy">
            <span class="strategy-label">重复书籍处理：</span>
            <div class="strategy-options">
              <button
                :class="['strategy-btn', { active: duplicateStrategy === 'skip' }]"
                @click="duplicateStrategy = 'skip'"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 11 12 14 22 4"/>
                  <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                </svg>
                <span>跳过</span>
              </button>
              <button
                :class="['strategy-btn', { active: duplicateStrategy === 'overwrite' }]"
                @click="duplicateStrategy = 'overwrite'"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
                <span>覆盖</span>
              </button>
            </div>
          </div>

          <!-- 书籍列表 -->
          <div class="preview-list">
            <div class="list-header">
              <label class="select-all">
                <input
                  type="checkbox"
                  :checked="isAllSelected"
                  :indeterminate.prop="isPartialSelected"
                  @change="toggleSelectAll"
                />
                <span>全选</span>
              </label>
              <span class="selected-count">已选择 {{ selectedCount }} 本</span>
            </div>

            <div class="list-body">
              <div
                v-for="(item, index) in previewData.items"
                :key="index"
                :class="['preview-item', { duplicate: item.duplicate, error: item.status === 'error', selected: selectedItems.includes(index) }]"
              >
                <input
                  v-if="item.status !== 'error'"
                  type="checkbox"
                  :checked="selectedItems.includes(index)"
                  @change="toggleItemSelection(index)"
                />
                <div class="item-content">
                  <div class="item-main">
                    <h4 class="item-title">
                      {{ item.title }}
                      <span v-if="item.duplicate" class="duplicate-badge">重复</span>
                      <span v-if="item.status === 'error'" class="error-badge">解析失败</span>
                    </h4>
                    <p class="item-meta">
                      <span v-if="item.author">{{ item.author }}</span>
                      <span v-if="item.publisher">{{ item.publisher }}</span>
                    </p>
                  </div>
                  <div v-if="item.status === 'error'" class="item-error">
                    {{ item.errorMessage }}
                  </div>
                  <div v-else class="item-details">
                    <span v-if="item.isbn" class="detail-tag">ISBN: {{ item.isbn }}</span>
                    <span v-if="item.readingStatus" class="detail-tag">{{ item.readingStatus }}</span>
                    <span v-if="item.rating" class="detail-tag rating">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                      </svg>
                      {{ item.rating }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="9" y1="15" x2="15" y2="15"/>
          </svg>
          <p>未找到可导入的书籍</p>
        </div>
      </div>

      <!-- ==================== 步骤3: 导入进度 ==================== -->
      <div v-show="currentStep === 2" class="step-content">
        <!-- 导入中 -->
        <div v-if="importing" class="import-progress">
          <div class="progress-ring">
            <svg viewBox="0 0 100 100">
              <circle class="progress-bg" cx="50" cy="50" r="45"/>
              <circle
                class="progress-bar"
                cx="50"
                cy="50"
                r="45"
                :stroke-dasharray="progressCircumference"
                :stroke-dashoffset="progressOffset"
              />
            </svg>
            <div class="progress-text">
              <span class="progress-percent">{{ importProgress }}%</span>
            </div>
          </div>
          <p class="progress-status">{{ importStatus }}</p>
          <div class="progress-bar-container">
            <div class="progress-bar-fill" :style="{ width: importProgress + '%' }"></div>
          </div>
        </div>

        <!-- 导入完成 -->
        <div v-else class="import-result">
          <div class="result-icon" :class="resultClass">
            <svg v-if="importResult.successCount > 0" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            <svg v-else width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M8 15h8"/>
              <path d="M9 9h.01"/>
              <path d="M15 9h.01"/>
            </svg>
          </div>
          <h3 class="result-title">{{ resultTitle }}</h3>

          <div class="result-stats">
            <div v-if="importResult.successCount > 0" class="stat-item success">
              <span class="stat-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              </span>
              <span class="stat-value">{{ importResult.successCount }}</span>
              <span class="stat-label">成功导入</span>
            </div>
            <div v-if="importResult.skipCount > 0" class="stat-item skip">
              <span class="stat-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 11 12 14 22 4"/>
                  <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                </svg>
              </span>
              <span class="stat-value">{{ importResult.skipCount }}</span>
              <span class="stat-label">跳过</span>
            </div>
            <div v-if="importResult.overwriteCount > 0" class="stat-item overwrite">
              <span class="stat-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
              </span>
              <span class="stat-value">{{ importResult.overwriteCount }}</span>
              <span class="stat-label">覆盖</span>
            </div>
            <div v-if="importResult.failCount > 0" class="stat-item fail">
              <span class="stat-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <path d="M15 9l-6 6"/>
                  <path d="M9 9l6 6"/>
                </svg>
              </span>
              <span class="stat-value">{{ importResult.failCount }}</span>
              <span class="stat-label">失败</span>
            </div>
          </div>

          <!-- 失败详情 -->
          <div v-if="importResult.failures && importResult.failures.length > 0" class="failure-list">
            <div class="failure-header" @click="showFailures = !showFailures">
              <span>失败详情</span>
              <svg :class="['expand-icon', { expanded: showFailures }]" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </div>
            <transition name="slide">
              <div v-show="showFailures" class="failure-body">
                <div v-for="(failure, idx) in importResult.failures" :key="idx" class="failure-item">
                  <span class="failure-title">{{ failure.title }}</span>
                  <span class="failure-reason">{{ failure.reason }}</span>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>

    <!-- ==================== 底部按钮 ==================== -->
    <template #footer>
      <div class="dialog-footer">
        <button v-if="currentStep > 0 && !importing" class="btn-secondary" @click="prevStep">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
          上一步
        </button>
        <div class="footer-spacer"></div>
        <button class="btn-cancel" @click="handleClose">取消</button>
        <button
          v-if="currentStep === 0"
          class="btn-primary"
          :disabled="!selectedFile || previewLoading"
          @click="handlePreview"
        >
          <span v-if="previewLoading" class="btn-loading"></span>
          <span v-else>解析文件</span>
        </button>
        <button
          v-if="currentStep === 1"
          class="btn-primary"
          :disabled="selectedCount === 0 || importing"
          @click="handleImport"
        >
          导入 {{ selectedCount }} 本
        </button>
        <button
          v-if="currentStep === 2 && !importing"
          class="btn-primary"
          @click="handleComplete"
        >
          完成
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { bookApi } from '@/api/modules'
import { ElMessage } from 'element-plus'

// ==================== Props 定义 ====================

const props = defineProps({
  // 对话框显示状态
  visible: {
    type: Boolean,
    default: false
  }
})

// ==================== Emits 定义 ====================

const emit = defineEmits(['update:visible', 'complete'])

// ==================== 响应式状态 ====================

// 步骤定义
const steps = ['上传文件', '预览选择', '导入结果']
const currentStep = ref(0)

// 文件上传相关
const fileInput = ref(null)
const selectedFile = ref(null)
const isDragOver = ref(false)
const sourceType = ref('auto')  // auto, douban, weread

// 预览相关
const previewLoading = ref(false)
const previewData = ref({
  sourceType: '',
  successCount: 0,
  errorCount: 0,
  duplicateCount: 0,
  items: []
})

// 选择相关
const selectedItems = ref([])
const duplicateStrategy = ref('skip')  // skip, overwrite

// 导入相关
const importing = ref(false)
const importProgress = ref(0)
const importStatus = ref('')
const importResult = ref({
  successCount: 0,
  failCount: 0,
  skipCount: 0,
  overwriteCount: 0,
  totalCount: 0,
  failures: []
})

// 展开失败列表
const showFailures = ref(false)

// 进度环参数
const progressCircumference = 2 * Math.PI * 45
const progressOffset = computed(() => {
  return progressCircumference * (1 - importProgress.value / 100)
})

// ==================== 计算属性 ====================

// 是否全选
const isAllSelected = computed(() => {
  const validItems = previewData.value.items.filter(item => item.status !== 'error')
  return validItems.length > 0 && selectedItems.value.length === validItems.length
})

// 是否部分选中
const isPartialSelected = computed(() => {
  const validItems = previewData.value.items.filter(item => item.status !== 'error')
  return selectedItems.value.length > 0 && selectedItems.value.length < validItems.length
})

// 选中数量
const selectedCount = computed(() => selectedItems.value.length)

// 结果标题
const resultTitle = computed(() => {
  if (importResult.value.successCount > 0) {
    return `成功导入 ${importResult.value.successCount} 本书籍`
  }
  return '导入完成'
})

// 结果样式类
const resultClass = computed(() => {
  if (importResult.value.failCount > 0 && importResult.value.successCount === 0) {
    return 'error'
  }
  return 'success'
})

// ==================== 监听器 ====================

watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 对话框打开时重置状态
    resetState()
  }
})

// ==================== 方法定义 ====================

/**
 * 重置组件状态
 */
function resetState() {
  currentStep.value = 0
  selectedFile.value = null
  sourceType.value = 'auto'
  previewData.value = { sourceType: '', successCount: 0, errorCount: 0, duplicateCount: 0, items: [] }
  selectedItems.value = []
  duplicateStrategy.value = 'skip'
  importing.value = false
  importProgress.value = 0
  importResult.value = { successCount: 0, failCount: 0, skipCount: 0, overwriteCount: 0, totalCount: 0, failures: [] }
  showFailures.value = false
}

/**
 * 触发文件选择
 */
function triggerFileInput() {
  fileInput.value?.click()
}

/**
 * 处理文件选择
 */
function handleFileSelect(event) {
  const file = event.target.files?.[0]
  if (file) {
    selectedFile.value = file
  }
}

/**
 * 处理文件拖拽
 */
function handleFileDrop(event) {
  isDragOver.value = false
  const file = event.dataTransfer?.files?.[0]
  if (file) {
    if (file.name.endsWith('.csv') || file.name.endsWith('.json')) {
      selectedFile.value = file
    } else {
      ElMessage.warning('请上传 CSV 或 JSON 格式的文件')
    }
  }
}

/**
 * 清除已选文件
 */
function clearSelectedFile() {
  selectedFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

/**
 * 格式化文件大小
 */
function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

/**
 * 处理解析预览
 */
async function handlePreview() {
  if (!selectedFile.value) return

  previewLoading.value = true

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    const response = await bookApi.previewImport(formData)

    if (response.success && response.data) {
      previewData.value = response.data
      // 默认选中所有有效项
      selectedItems.value = response.data.items
        .map((item, index) => item.status !== 'error' ? index : -1)
        .filter(i => i >= 0)
      currentStep.value = 1
    } else {
      ElMessage.error(response.message || '文件解析失败')
    }
  } catch (error) {
    console.error('预览失败:', error)
    // 显示更详细的错误信息
    const errorMessage = error?.response?.data?.message || error?.message || '文件解析失败，请检查文件格式'
    ElMessage.error(errorMessage)
  } finally {
    previewLoading.value = false
  }
}

/**
 * 切换全选
 */
function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedItems.value = []
  } else {
    selectedItems.value = previewData.value.items
      .map((item, index) => item.status !== 'error' ? index : -1)
      .filter(i => i >= 0)
  }
}

/**
 * 切换单项选择
 */
function toggleItemSelection(index) {
  const idx = selectedItems.value.indexOf(index)
  if (idx > -1) {
    selectedItems.value.splice(idx, 1)
  } else {
    selectedItems.value.push(index)
  }
}

/**
 * 处理导入
 */
async function handleImport() {
  importing.value = true
  importProgress.value = 0
  currentStep.value = 2

  try {
    const request = {
      sourceType: previewData.value.sourceType,
      duplicateStrategy: duplicateStrategy.value,
      selectedIndexes: selectedItems.value
    }

    // 模拟进度更新
    const progressInterval = setInterval(() => {
      if (importProgress.value < 90) {
        importProgress.value += 10
        importStatus.value = `正在导入... (${importProgress.value}%)`
      }
    }, 300)

    const response = await bookApi.confirmImport(request)

    clearInterval(progressInterval)
    importProgress.value = 100
    importStatus.value = '导入完成'

    if (response.success && response.data) {
      importResult.value = response.data
    } else {
      ElMessage.error(response.message || '导入失败')
    }
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败，请稍后重试')
  } finally {
    importing.value = false
  }
}

/**
 * 上一步
 */
function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

/**
 * 处理关闭
 */
function handleClose() {
  emit('update:visible', false)
}

/**
 * 处理完成
 */
function handleComplete() {
  emit('complete', importResult.value)
  handleClose()
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Crimson+Pro:wght@400;600;700&family=Nunito:wght@400;500;600;700&display=swap');

/* ==================== 对话框基础样式 ==================== */

:deep(.book-import-dialog) {
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(180deg, #fdfcfa 0%, #f8f5f0 100%);
  box-shadow: 0 25px 50px -12px rgba(44, 24, 16, 0.25);
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #3d2317 0%, #2c1810 100%);
  padding: 1.25rem 2rem;
  margin: 0;
  border-bottom: none;
}

:deep(.el-dialog__title) {
  font-family: 'Crimson Pro', serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #f5f1eb;
  letter-spacing: 0.02em;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(245, 241, 235, 0.7);
  font-size: 20px;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #f5f1eb;
}

:deep(.el-dialog__body) {
  padding: 0;
}

:deep(.el-dialog__footer) {
  padding: 0;
  border-top: 1px solid #e8ddd0;
  background: #fdfcfa;
}

.import-container {
  padding: 1.5rem 2rem;
  min-height: 400px;
}

/* ==================== 步骤指示器 ==================== */

.steps-indicator {
  display: flex;
  justify-content: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #e8ddd0;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.step-number {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e8ddd0;
  color: #8b7355;
  font-family: 'Nunito', sans-serif;
  font-weight: 700;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.step-item.active .step-number {
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(107, 83, 68, 0.3);
}

.step-item.completed .step-number {
  background: #6b8f71;
  color: white;
}

.step-label {
  font-family: 'Nunito', sans-serif;
  font-size: 0.875rem;
  color: #8b7355;
  font-weight: 500;
}

.step-item.active .step-label {
  color: #2c1810;
  font-weight: 600;
}

.step-line {
  width: 40px;
  height: 2px;
  background: #e8ddd0;
  margin: 0 0.75rem;
}

/* ==================== 文件上传区域 ==================== */

.upload-section {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.upload-area {
  border: 2px dashed #d4c4b0;
  border-radius: 16px;
  padding: 3rem 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: linear-gradient(180deg, #fefefe 0%, #faf8f5 100%);
}

.upload-area:hover,
.upload-area.drag-over {
  border-color: #8b6f47;
  background: linear-gradient(180deg, #faf8f5 0%, #f5f1eb 100%);
}

.upload-icon {
  color: #8b6f47;
  margin-bottom: 1rem;
}

.upload-text {
  font-family: 'Nunito', sans-serif;
  font-size: 1rem;
  color: #4a3728;
  margin-bottom: 0.5rem;
}

.upload-link {
  color: #8b6f47;
  font-weight: 600;
  text-decoration: underline;
}

.upload-hint {
  font-size: 0.8125rem;
  color: #8b7355;
}

/* 已选文件 */

.selected-file {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.25rem;
  background: white;
  border: 2px solid #e8ddd0;
  border-radius: 12px;
}

.file-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-info {
  flex: 1;
}

.file-name {
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  color: #2c1810;
  margin: 0;
}

.file-size {
  font-size: 0.75rem;
  color: #8b7355;
  margin: 0.25rem 0 0;
}

.file-remove {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: #fef2f2;
  color: #991b1b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.file-remove:hover {
  background: #fee2e2;
}

/* 来源选择 */

.source-selector {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #faf8f5;
  border-radius: 12px;
}

.source-label {
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  color: #4a3728;
  font-size: 0.875rem;
}

.source-options {
  display: flex;
  gap: 0.5rem;
}

.source-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: 2px solid #e8ddd0;
  border-radius: 8px;
  background: white;
  color: #6b5344;
  font-family: 'Nunito', sans-serif;
  font-weight: 500;
  font-size: 0.8125rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.source-btn:hover {
  border-color: #c4b5a0;
}

.source-btn.active {
  border-color: #6b5344;
  background: linear-gradient(135deg, #faf8f5 0%, #f5f1eb 100%);
  color: #2c1810;
}

.source-icon-douban,
.source-icon-weread {
  width: 18px;
  height: 18px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.75rem;
}

.source-icon-douban {
  background: #00b51d;
  color: white;
}

.source-icon-weread {
  background: #1aad19;
  color: white;
}

/* ==================== 预览区域 ==================== */

.preview-section {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.preview-summary {
  display: flex;
  gap: 1rem;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: 10px;
  font-family: 'Nunito', sans-serif;
}

.summary-item.success {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
}

.summary-item.warning {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
}

.summary-item.error {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
}

.summary-count {
  font-size: 1.25rem;
  font-weight: 700;
}

.summary-item.success .summary-count { color: #16a34a; }
.summary-item.warning .summary-count { color: #d97706; }
.summary-item.error .summary-count { color: #dc2626; }

.summary-label {
  font-size: 0.8125rem;
  color: #6b5344;
}

/* 重复策略 */

.duplicate-strategy {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border-radius: 12px;
  border: 1px solid #fcd34d;
}

.strategy-label {
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  color: #92400e;
  font-size: 0.875rem;
}

.strategy-options {
  display: flex;
  gap: 0.5rem;
}

.strategy-btn {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.5rem 0.875rem;
  border: 2px solid #fcd34d;
  border-radius: 8px;
  background: white;
  color: #92400e;
  font-family: 'Nunito', sans-serif;
  font-weight: 500;
  font-size: 0.8125rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.strategy-btn:hover {
  border-color: #f59e0b;
}

.strategy-btn.active {
  border-color: #f59e0b;
  background: #f59e0b;
  color: white;
}

/* 预览列表 */

.preview-list {
  border: 2px solid #e8ddd0;
  border-radius: 12px;
  overflow: hidden;
  background: white;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.875rem 1rem;
  background: #faf8f5;
  border-bottom: 1px solid #e8ddd0;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Nunito', sans-serif;
  font-weight: 500;
  color: #4a3728;
  font-size: 0.875rem;
  cursor: pointer;
}

.select-all input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #8b6f47;
}

.selected-count {
  font-size: 0.8125rem;
  color: #8b7355;
}

.list-body {
  max-height: 280px;
  overflow-y: auto;
}

.preview-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  border-bottom: 1px solid #f5f1eb;
  transition: background 0.15s ease;
}

.preview-item:last-child {
  border-bottom: none;
}

.preview-item:hover {
  background: #faf8f5;
}

.preview-item.selected {
  background: linear-gradient(90deg, rgba(139, 111, 71, 0.05) 0%, transparent 100%);
}

.preview-item.duplicate {
  background: linear-gradient(90deg, rgba(217, 119, 6, 0.08) 0%, transparent 100%);
}

.preview-item.error {
  background: linear-gradient(90deg, rgba(220, 38, 38, 0.05) 0%, transparent 100%);
  opacity: 0.7;
}

.preview-item input[type="checkbox"] {
  width: 16px;
  height: 16px;
  margin-top: 2px;
  accent-color: #8b6f47;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-main {
  margin-bottom: 0.25rem;
}

.item-title {
  font-family: 'Crimson Pro', serif;
  font-size: 0.9375rem;
  font-weight: 600;
  color: #2c1810;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.duplicate-badge {
  font-family: 'Nunito', sans-serif;
  font-size: 0.6875rem;
  font-weight: 600;
  padding: 0.125rem 0.375rem;
  background: #fef3c7;
  color: #92400e;
  border-radius: 4px;
}

.error-badge {
  font-family: 'Nunito', sans-serif;
  font-size: 0.6875rem;
  font-weight: 600;
  padding: 0.125rem 0.375rem;
  background: #fee2e2;
  color: #991b1b;
  border-radius: 4px;
}

.item-meta {
  font-size: 0.8125rem;
  color: #6b5344;
  margin: 0.25rem 0 0;
}

.item-meta span:not(:last-child)::after {
  content: ' · ';
  color: #b8a898;
}

.item-error {
  font-size: 0.75rem;
  color: #dc2626;
  margin-top: 0.25rem;
}

.item-details {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
  margin-top: 0.375rem;
}

.detail-tag {
  font-family: 'Nunito', sans-serif;
  font-size: 0.6875rem;
  padding: 0.125rem 0.5rem;
  background: #f5f1eb;
  color: #6b5344;
  border-radius: 4px;
}

.detail-tag.rating {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background: #fef3c7;
  color: #92400e;
}

/* ==================== 导入进度 ==================== */

.import-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
}

.progress-ring {
  position: relative;
  width: 120px;
  height: 120px;
  margin-bottom: 1.5rem;
}

.progress-ring svg {
  transform: rotate(-90deg);
}

.progress-bg {
  fill: none;
  stroke: #e8ddd0;
  stroke-width: 8;
}

.progress-bar {
  fill: none;
  stroke: url(#progressGradient);
  stroke-width: 8;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.3s ease;
}

.progress-text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-percent {
  font-family: 'Crimson Pro', serif;
  font-size: 1.75rem;
  font-weight: 700;
  color: #2c1810;
}

.progress-status {
  font-family: 'Nunito', sans-serif;
  font-size: 0.9375rem;
  color: #6b5344;
  margin-bottom: 1.5rem;
}

.progress-bar-container {
  width: 100%;
  max-width: 300px;
  height: 6px;
  background: #e8ddd0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #8b6f47 0%, #6b5344 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

/* ==================== 导入结果 ==================== */

.import-result {
  text-align: center;
  padding: 1.5rem;
}

.result-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.25rem;
}

.result-icon.success {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #16a34a;
}

.result-icon.error {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  color: #dc2626;
}

.result-title {
  font-family: 'Crimson Pro', serif;
  font-size: 1.375rem;
  font-weight: 700;
  color: #2c1810;
  margin: 0 0 1.5rem;
}

.result-stats {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  min-width: 100px;
}

.stat-item.success {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
}

.stat-item.skip {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
}

.stat-item.overwrite {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
}

.stat-item.fail {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
}

.stat-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
}

.stat-item.success .stat-icon { color: #16a34a; }
.stat-item.skip .stat-icon { color: #d97706; }
.stat-item.overwrite .stat-icon { color: #2563eb; }
.stat-item.fail .stat-icon { color: #dc2626; }

.stat-value {
  font-family: 'Crimson Pro', serif;
  font-size: 1.5rem;
  font-weight: 700;
}

.stat-item.success .stat-value { color: #16a34a; }
.stat-item.skip .stat-value { color: #d97706; }
.stat-item.overwrite .stat-value { color: #2563eb; }
.stat-item.fail .stat-value { color: #dc2626; }

.stat-label {
  font-family: 'Nunito', sans-serif;
  font-size: 0.75rem;
  color: #6b5344;
}

/* 失败列表 */

.failure-list {
  border: 1px solid #fee2e2;
  border-radius: 12px;
  overflow: hidden;
  text-align: left;
}

.failure-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.875rem 1rem;
  background: #fef2f2;
  cursor: pointer;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 0.875rem;
  color: #991b1b;
}

.expand-icon {
  transition: transform 0.2s ease;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.failure-body {
  padding: 0.5rem 1rem;
  background: white;
  max-height: 150px;
  overflow-y: auto;
}

.failure-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f5f1eb;
  font-size: 0.8125rem;
}

.failure-item:last-child {
  border-bottom: none;
}

.failure-title {
  color: #2c1810;
  font-weight: 500;
}

.failure-reason {
  color: #dc2626;
}

/* 空状态 */

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #8b7355;
}

.empty-state svg {
  margin-bottom: 1rem;
  opacity: 0.5;
}

/* 加载状态 */

.loading-container {
  text-align: center;
  padding: 3rem;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid #e8ddd0;
  border-top-color: #8b6f47;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-family: 'Nunito', sans-serif;
  color: #6b5344;
}

/* ==================== 底部按钮 ==================== */

.dialog-footer {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.25rem 2rem;
}

.footer-spacer {
  flex: 1;
}

.btn-secondary {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.625rem 1rem;
  border: 2px solid #e8ddd0;
  border-radius: 8px;
  background: white;
  color: #6b5344;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  border-color: #c4b5a0;
  background: #faf8f5;
}

.btn-cancel {
  padding: 0.625rem 1.25rem;
  border: 2px solid #e8ddd0;
  border-radius: 10px;
  background: white;
  color: #6b5344;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  border-color: #c4b5a0;
  background: #faf8f5;
}

.btn-primary {
  padding: 0.625rem 1.5rem;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #8b6f47 0%, #6b5344 100%);
  color: white;
  font-family: 'Nunito', sans-serif;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(107, 83, 68, 0.3);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-loading {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* ==================== 动画 ==================== */

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  max-height: 0;
}

.slide-enter-to,
.slide-leave-from {
  max-height: 200px;
}

/* ==================== 响应式 ==================== */

@media (max-width: 768px) {
  :deep(.book-import-dialog) {
    width: 95% !important;
    margin: 1rem auto !important;
  }

  .import-container {
    padding: 1rem;
  }

  .steps-indicator {
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .step-line {
    display: none;
  }

  .preview-summary {
    flex-wrap: wrap;
  }

  .result-stats {
    flex-direction: column;
  }

  .stat-item {
    flex-direction: row;
    justify-content: flex-start;
    gap: 0.75rem;
  }
}
</style>