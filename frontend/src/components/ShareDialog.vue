<template>
  <!-- ==================== 分享对话框 ==================== -->
  <el-dialog
    v-model="visible"
    title="分享设置"
    width="480px"
    :close-on-click-modal="false"
    class="share-dialog"
    @closed="handleClose"
  >
    <!-- 分享表单 -->
    <div class="share-form">
      <!-- 有效期选择 -->
      <div class="form-section">
        <label class="section-label">有效期</label>
        <div class="expiry-options">
          <el-radio-group v-model="expiryDays" class="radio-group">
            <el-radio-button :value="0">永久有效</el-radio-button>
            <el-radio-button :value="1">1天</el-radio-button>
            <el-radio-button :value="7">7天</el-radio-button>
            <el-radio-button :value="30">30天</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 分享链接 -->
      <div class="form-section" v-if="shareUrl">
        <label class="section-label">分享链接</label>
        <div class="share-url-box">
          <el-input
            v-model="shareUrl"
            readonly
            class="url-input"
          >
            <template #suffix>
              <el-button
                type="primary"
                text
                @click="copyToClipboard"
                :icon="CopyDocument"
              >
                复制
              </el-button>
            </template>
          </el-input>
        </div>
        <p class="share-tip" v-if="!isPermanent">
          链接将于 {{ expiryDate }} 过期
        </p>
      </div>
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <!-- 金句类型显示生成卡片按钮 -->
        <el-button
          v-if="resourceType === 'quote'"
          @click="handleOpenCard"
          class="card-btn"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
          生成分享卡片
        </el-button>
        <div class="footer-right">
          <el-button @click="handleClose">取消</el-button>
          <el-button
            type="primary"
            @click="handleGenerateLink"
            :loading="generating"
            v-if="!shareUrl"
          >
            生成分享链接
          </el-button>
          <el-button
            type="primary"
            @click="copyToClipboard"
            v-else
          >
            复制链接
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 分享对话框组件
 * 用于生成和复制分享链接
 * 支持设置有效期
 */
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { CopyDocument } from '@element-plus/icons-vue'
import { shareApi } from '@/api/modules'

// ==================== Props 定义 ====================

const props = defineProps({
  /** 对话框可见性 */
  modelValue: {
    type: Boolean,
    default: false
  },
  /** 资源类型：note/quote */
  resourceType: {
    type: String,
    required: true,
    validator: (val) => ['note', 'quote'].includes(val)
  },
  /** 资源ID */
  resourceId: {
    type: Number,
    required: true
  },
  /** 金句数据（用于生成分享卡片，仅quote类型需要） */
  quoteData: {
    type: Object,
    default: () => ({
      content: '',
      bookTitle: '',
      bookAuthor: '',
      chapter: '',
      pageNumber: null
    })
  }
})

// ==================== Emits 定义 ====================

const emit = defineEmits(['update:modelValue', 'shared', 'open-card'])

// ==================== 响应式状态 ====================

/** 对话框可见性（双向绑定） */
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

/** 有效期天数 */
const expiryDays = ref(0)

/** 分享链接 */
const shareUrl = ref('')

/** 分享令牌 */
const shareToken = ref('')

/** 生成中状态 */
const generating = ref(false)

// ==================== 计算属性 ====================

/** 是否永久有效 */
const isPermanent = computed(() => expiryDays.value === 0)

/** 过期日期显示 */
const expiryDate = computed(() => {
  if (isPermanent.value || !shareUrl.value) return ''

  const date = new Date()
  date.setDate(date.getDate() + expiryDays.value)

  return `${date.getMonth() + 1}月${date.getDate()}日`
})

// ==================== 方法定义 ====================

/**
 * 生成分享链接
 */
async function handleGenerateLink() {
  generating.value = true

  try {
    const data = { expiryDays: expiryDays.value }
    let response

    if (props.resourceType === 'note') {
      response = await shareApi.shareNote(props.resourceId, data)
    } else {
      response = await shareApi.shareQuote(props.resourceId, data)
    }

    shareUrl.value = response.data.shareUrl
    shareToken.value = response.data.token

    ElMessage.success('分享链接已生成')
    emit('shared', response.data)
  } catch (err) {
    console.error('生成分享链接失败:', err)
    ElMessage.error(err.response?.data?.message || '生成分享链接失败')
  } finally {
    generating.value = false
  }
}

/**
 * 复制到剪贴板
 */
async function copyToClipboard() {
  if (!shareUrl.value) return

  try {
    await navigator.clipboard.writeText(shareUrl.value)
    ElMessage.success('链接已复制到剪贴板')
  } catch (err) {
    // 降级方案：使用 execCommand
    const textArea = document.createElement('textarea')
    textArea.value = shareUrl.value
    textArea.style.position = 'fixed'
    textArea.style.left = '-9999px'
    document.body.appendChild(textArea)
    textArea.select()

    try {
      document.execCommand('copy')
      ElMessage.success('链接已复制到剪贴板')
    } catch (e) {
      ElMessage.error('复制失败，请手动复制')
    } finally {
      document.body.removeChild(textArea)
    }
  }
}

/**
 * 关闭对话框
 */
function handleClose() {
  visible.value = false
  // 重置状态
  shareUrl.value = ''
  shareToken.value = ''
  expiryDays.value = 0
}

/**
 * 打开分享卡片生成器
 * 仅金句类型可用
 */
function handleOpenCard() {
  // 触发事件，让父组件打开卡片生成器
  emit('open-card', {
    resourceId: props.resourceId,
    quoteData: props.quoteData
  })
  // 关闭当前对话框
  handleClose()
}

// ==================== 侦听器 ====================

/** 对话框打开时重置状态 */
watch(visible, (val) => {
  if (val) {
    shareUrl.value = ''
    shareToken.value = ''
    expiryDays.value = 0
  }
})
</script>

<style scoped>
/* ==================== 对话框样式 ==================== */
.share-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #ebeef5;
}

.share-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.share-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.share-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 20px;
  border-top: 1px solid #ebeef5;
}

/* ==================== 表单样式 ==================== */
.share-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

/* 有效期选项 */
.expiry-options {
  display: flex;
}

.radio-group {
  width: 100%;
}

.radio-group :deep(.el-radio-button__inner) {
  width: 100%;
  padding: 10px 0;
}

/* 分享链接 */
.share-url-box {
  position: relative;
}

.url-input :deep(.el-input__wrapper) {
  padding-right: 80px;
}

.url-input :deep(.el-input__inner) {
  font-size: 13px;
  color: #409eff;
}

.share-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
}

/* 底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.footer-right {
  display: flex;
  gap: 12px;
}

.card-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8b6f47;
  border-color: #8b6f47;
}

.card-btn:hover {
  color: #6b5344;
  border-color: #6b5344;
  background: #faf8f5;
}

/* ==================== 响应式 ==================== */
@media (max-width: 520px) {
  .share-dialog :deep(.el-dialog) {
    width: 90% !important;
    margin-top: 10vh !important;
  }

  .radio-group :deep(.el-radio-button) {
    flex: 1 1 50%;
  }
}
</style>