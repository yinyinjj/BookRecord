<template>
  <div class="block-editor" :class="{ 'is-focused': isFocused }">
    <!-- ==================== 工具栏区域 ==================== -->
    <div class="editor-toolbar" v-if="editor">
      <!-- 撤销/重做按钮组 -->
      <div class="toolbar-group">
        <button
          type="button"
          class="toolbar-btn"
          :disabled="!editor.can().undo()"
          @click="editor.chain().focus().undo().run()"
          title="撤销 (Ctrl+Z)"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 7v6h6M3 13c0-4.97 4.03-9 9-9s9 4.03 9 9-4.03 9-9 9a9 9 0 0 1-6.36-2.64"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          :disabled="!editor.can().redo()"
          @click="editor.chain().focus().redo().run()"
          title="重做 (Ctrl+Y)"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 7v6h-6M21 13c0-4.97-4.03-9-9-9s-9 4.03-9 9 4.03 9 9 9a9 9 0 0 0 6.36-2.64"/>
          </svg>
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- 标题按钮组 -->
      <div class="toolbar-group">
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('heading', { level: 1 }) }"
          @click="editor.chain().focus().toggleHeading({ level: 1 }).run()"
          title="一级标题"
        >H1</button>
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('heading', { level: 2 }) }"
          @click="editor.chain().focus().toggleHeading({ level: 2 }).run()"
          title="二级标题"
        >H2</button>
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('heading', { level: 3 }) }"
          @click="editor.chain().focus().toggleHeading({ level: 3 }).run()"
          title="三级标题"
        >H3</button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- 文字格式按钮组 -->
      <div class="toolbar-group">
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('bold') }"
          @click="editor.chain().focus().toggleBold().run()"
          title="粗体 (Ctrl+B)"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M15.6 10.79c.97-.67 1.65-1.77 1.65-2.79 0-2.26-1.75-4-4-4H7v14h7.04c2.09 0 3.71-1.7 3.71-3.79 0-1.52-.86-2.82-2.15-3.42zM10 6.5h3c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5h-3v-3zm3.5 9H10v-3h3.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5z"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('italic') }"
          @click="editor.chain().focus().toggleItalic().run()"
          title="斜体 (Ctrl+I)"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M10 4v3h2.21l-3.42 8H6v3h8v-3h-2.21l3.42-8H18V4z"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('underline') }"
          @click="editor.chain().focus().toggleUnderline().run()"
          title="下划线 (Ctrl+U)"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 17c3.31 0 6-2.69 6-6V3h-2.5v8c0 1.93-1.57 3.5-3.5 3.5S8.5 12.93 8.5 11V3H6v8c0 3.31 2.69 6 6 6zm-7 2v2h14v-2H5z"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('strike') }"
          @click="editor.chain().focus().toggleStrike().run()"
          title="删除线"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M10 19h4v-3h-4v3zM5 4v3h5v3h4V7h5V4H5zM3 14h18v-2H3v2z"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('highlight') }"
          @click="editor.chain().focus().toggleHighlight().run()"
          title="高亮"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M16.56 8.94L7.62 0 6.21 1.41l2.38 2.38-5.15 5.15c-.59.59-.59 1.54 0 2.12l5.5 5.5c.29.29.68.44 1.06.44s.77-.15 1.06-.44l5.5-5.5c.59-.58.59-1.53 0-2.12zM5.21 10L10 5.21 14.79 10H5.21zM19 11.5s-2 2.17-2 3.5c0 1.1.9 2 2 2s2-.9 2-2c0-1.33-2-3.5-2-3.5z"/>
            <path d="M0 20h24v4H0z" fill-opacity="0.3"/>
          </svg>
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- 对齐按钮组 -->
      <div class="toolbar-group">
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive({ textAlign: 'left' }) }"
          @click="editor.chain().focus().setTextAlign('left').run()"
          title="左对齐"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M15 15H3v2h12v-2zm0-8H3v2h12V7zM3 13h18v-2H3v2zm0 8h18v-2H3v2zM3 3v2h18V3H3z"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive({ textAlign: 'center' }) }"
          @click="editor.chain().focus().setTextAlign('center').run()"
          title="居中对齐"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M7 15v2h10v-2H7zm-4 6h18v-2H3v2zm0-8h18v-2H3v2zm4-6v2h10V7H7zM3 3v2h18V3H3z"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive({ textAlign: 'right' }) }"
          @click="editor.chain().focus().setTextAlign('right').run()"
          title="右对齐"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M3 21h18v-2H3v2zm6-4h12v-2H9v2zm-6-4h18v-2H3v2zm6-4h12V7H9v2zM3 3v2h18V3H3z"/>
          </svg>
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- 列表按钮组 -->
      <div class="toolbar-group">
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('bulletList') }"
          @click="editor.chain().focus().toggleBulletList().run()"
          title="无序列表"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M4 10.5c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5 1.5-.67 1.5-1.5-.67-1.5-1.5-1.5zm0-6c-.83 0-1.5.67-1.5 1.5S3.17 7.5 4 7.5 5.5 6.83 5.5 6 4.83 4.5 4 4.5zm0 12c-.83 0-1.5.68-1.5 1.5s.68 1.5 1.5 1.5 1.5-.68 1.5-1.5-.67-1.5-1.5-1.5zM7 19h14v-2H7v2zm0-6h14v-2H7v2zm0-8v2h14V5H7z"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('orderedList') }"
          @click="editor.chain().focus().toggleOrderedList().run()"
          title="有序列表"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M2 17h2v.5H3v1h1v.5H2v1h3v-4H2v1zm1-9h1V4H2v1h1v3zm-1 3h1.8L2 13.1v.9h3v-1H3.2L5 10.9V10H2v1zm5-6v2h14V5H7zm0 14h14v-2H7v2zm0-6h14v-2H7v2z"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('taskList') }"
          @click="editor.chain().focus().toggleTaskList().run()"
          title="任务列表"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-9 14l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
          </svg>
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- 块级元素按钮组 -->
      <div class="toolbar-group">
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('blockquote') }"
          @click="editor.chain().focus().toggleBlockquote().run()"
          title="引用"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M6 17h3l2-4V7H5v6h3zm8 0h3l2-4V7h-6v6h3z"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          :class="{ 'is-active': editor.isActive('codeBlock') }"
          @click="editor.chain().focus().toggleCodeBlock().run()"
          title="代码块"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          @click="addTable"
          title="插入表格"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M3 3v18h18V3H3zm8 16H5v-6h6v6zm0-8H5V5h6v6zm8 8h-6v-6h6v6zm0-8h-6V5h6v6z"/>
          </svg>
        </button>
        <button
          type="button"
          class="toolbar-btn"
          @click="addImage"
          title="插入图片"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
          </svg>
        </button>
      </div>

      <!-- 快捷提示 -->
      <div class="toolbar-hint">
        <span class="hint-text">Markdown: # 标题 | - 列表 | > 引用 | ``` 代码</span>
      </div>
    </div>

    <!-- ==================== 编辑器区域 ==================== -->
    <div class="editor-content">
      <editor-content :editor="editor" class="editor-main" />

      <!-- 空状态提示 -->
      <div class="editor-placeholder" v-if="isEmpty">
        <div class="placeholder-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25"/>
          </svg>
        </div>
        <p class="placeholder-text">在这里记录你的阅读感悟...</p>
        <p class="placeholder-hint">使用 Markdown 语法快速编辑</p>
      </div>
    </div>

    <!-- ==================== 字数统计 ==================== -->
    <div class="editor-footer" v-if="editor">
      <span class="word-count">{{ characterCount }} 字</span>
      <span class="separator">·</span>
      <span class="line-count">{{ lineCount }} 行</span>
    </div>
  </div>
</template>

<script setup>
/**
 * BlockEditor 块级富文本编辑器组件
 * 基于 TipTap 框架实现，用于读书感悟的编辑
 * 支持 Markdown 快捷语法、代码高亮等功能
 */
import { ref, watch, onMounted, onBeforeUnmount, shallowRef } from 'vue'
import { Editor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import Image from '@tiptap/extension-image'
import { Table } from '@tiptap/extension-table'
import { TableRow } from '@tiptap/extension-table-row'
import { TableCell } from '@tiptap/extension-table-cell'
import { TableHeader } from '@tiptap/extension-table-header'
import CodeBlockLowlight from '@tiptap/extension-code-block-lowlight'
import TextAlign from '@tiptap/extension-text-align'
import Highlight from '@tiptap/extension-highlight'
import { TaskItem } from '@tiptap/extension-task-item'
import TaskList from '@tiptap/extension-task-list'
import { DragHandle } from '@tiptap/extension-drag-handle'
import { common, createLowlight } from 'lowlight'
import { ElMessageBox } from 'element-plus'

// ==================== Props 定义 ====================

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '在这里记录你的阅读感悟...'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

// ==================== Emits 定义 ====================

const emit = defineEmits(['update:modelValue'])

// ==================== 响应式状态 ====================

/** 编辑器实例（使用 shallowRef 避免深度响应） */
const editor = shallowRef(null)

/** 编辑器聚焦状态 */
const isFocused = ref(false)

/** 编辑器内容是否为空 */
const isEmpty = ref(true)

/** 字符计数 */
const characterCount = ref(0)

/** 行数统计 */
const lineCount = ref(0)

/** 代码高亮配置 */
const lowlight = createLowlight(common)

// ==================== 初始化编辑器 ====================

/**
 * 创建编辑器实例
 */
function createEditor() {
  return new Editor({
    content: props.modelValue,
    editable: !props.disabled,
    extensions: [
      StarterKit.configure({
        codeBlock: false,
        heading: { levels: [1, 2, 3] },
        // 启用拖拽功能
        draggable: true
      }),
      Placeholder.configure({ placeholder: props.placeholder }),
      Image.configure({ inline: false, allowBase64: true }),
      Table.configure({ resizable: true }),
      TableRow,
      TableCell,
      TableHeader,
      CodeBlockLowlight.configure({ lowlight, defaultLanguage: 'plaintext' }),
      TextAlign.configure({ types: ['heading', 'paragraph'] }),
      Highlight.configure({ multicolor: false }),
      TaskList,
      TaskItem.configure({ nested: true }),
      // 拖拽排序扩展
      DragHandle.configure({
        render: () => {
          const handle = document.createElement('div')
          handle.className = 'drag-handle'
          handle.setAttribute('draggable', 'true')
          handle.setAttribute('data-drag-handle', 'true')
          handle.innerHTML = `
            <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
              <circle cx="9" cy="6" r="1.5"/>
              <circle cx="15" cy="6" r="1.5"/>
              <circle cx="9" cy="12" r="1.5"/>
              <circle cx="15" cy="12" r="1.5"/>
              <circle cx="9" cy="18" r="1.5"/>
              <circle cx="15" cy="18" r="1.5"/>
            </svg>
          `
          return handle
        }
      })
    ],
    onUpdate: ({ editor }) => {
      const html = editor.getHTML()
      emit('update:modelValue', html)
      isEmpty.value = editor.isEmpty
      const text = editor.getText()
      characterCount.value = text.replace(/\s/g, '').length
      lineCount.value = html.split(/<\/p>|<\/h[1-6]>|<br>|<\/li>/).filter(line => line.trim()).length
    },
    onFocus: () => {
      isFocused.value = true
    },
    onBlur: () => {
      isFocused.value = false
    }
  })
}

// ==================== 生命周期 ====================

onMounted(() => {
  editor.value = createEditor()
})

onBeforeUnmount(() => {
  if (editor.value) {
    editor.value.destroy()
    editor.value = null
  }
})

// ==================== 方法定义 ====================

function addTable() {
  if (editor.value) {
    editor.value.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()
  }
}

async function addImage() {
  try {
    const { value } = await ElMessageBox.prompt('请输入图片地址', '插入图片', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: 'https://example.com/image.jpg'
    })
    if (value && editor.value) {
      editor.value.chain().focus().setImage({ src: value }).run()
    }
  } catch {
    // 用户取消
  }
}

// ==================== 监听器 ====================

watch(
  () => props.modelValue,
  (newValue) => {
    if (editor.value && editor.value.getHTML() !== newValue) {
      editor.value.commands.setContent(newValue, false)
      isEmpty.value = editor.value.isEmpty
    }
  }
)

watch(
  () => props.disabled,
  (newDisabled) => {
    if (editor.value) {
      editor.value.setEditable(!newDisabled)
    }
  }
)

// ==================== 暴露方法 ====================

defineExpose({
  getEditor: () => editor.value,
  getHTML: () => editor.value?.getHTML() || '',
  getText: () => editor.value?.getText() || '',
  getJSON: () => editor.value?.getJSON() || null,
  setContent: (content) => editor.value?.commands.setContent(content),
  clearContent: () => editor.value?.commands.clearContent(),
  focus: () => editor.value?.commands.focus()
})
</script>

<style scoped>
/* ==================== 编辑器容器样式 ==================== */

.block-editor {
  --editor-bg: #fdfbf7;
  --editor-border: #e8e0d4;
  --editor-border-focus: #c9a96e;
  --editor-text: #3d3629;
  --editor-text-muted: #8a7e6a;
  --editor-toolbar-bg: #f5f0e6;
  --editor-toolbar-btn: #6b5d4d;
  --editor-toolbar-btn-active: #8b4513;
  --editor-highlight: #fff3cd;
  --editor-code-bg: #f4f1eb;
  --editor-blockquote-border: #c9a96e;

  position: relative;
  border: 1px solid var(--editor-border);
  border-radius: 12px;
  background: var(--editor-bg);
  overflow: hidden;
  transition: all 0.3s ease;
}

.block-editor:hover {
  border-color: var(--editor-border-focus);
}

.block-editor.is-focused {
  border-color: var(--editor-border-focus);
  box-shadow: 0 0 0 3px rgba(201, 169, 110, 0.15);
}

/* ==================== 工具栏样式 ==================== */

.editor-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  padding: 12px 16px;
  background: var(--editor-toolbar-bg);
  border-bottom: 1px solid var(--editor-border);
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 2px;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: var(--editor-border);
  margin: 0 8px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--editor-toolbar-btn);
  cursor: pointer;
  transition: all 0.2s ease;
}

.toolbar-btn svg {
  width: 18px;
  height: 18px;
}

.toolbar-btn:hover:not(:disabled) {
  background: var(--editor-bg);
  color: var(--editor-toolbar-btn-active);
}

.toolbar-btn.is-active {
  background: var(--editor-bg);
  color: var(--editor-toolbar-btn-active);
}

.toolbar-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.toolbar-hint {
  margin-left: auto;
  padding-left: 16px;
}

.hint-text {
  font-size: 12px;
  color: var(--editor-text-muted);
  font-style: italic;
}

/* ==================== 编辑区域样式 ==================== */

.editor-content {
  position: relative;
  min-height: 200px;
  max-height: 500px;
  overflow-y: auto;
}

.editor-main {
  padding: 24px 32px;
}

:deep(.tiptap) {
  outline: none;
  min-height: 150px;
  color: var(--editor-text);
  font-family: 'Source Han Serif SC', 'Noto Serif SC', serif;
  font-size: 16px;
  line-height: 1.8;
}

:deep(.tiptap p.is-editor-empty:first-child::before) {
  color: var(--editor-text-muted);
  content: attr(data-placeholder);
  float: left;
  height: 0;
  pointer-events: none;
}

:deep(.tiptap h1) { font-size: 2em; font-weight: 700; margin: 1.2em 0 0.6em; color: var(--editor-toolbar-btn-active); }
:deep(.tiptap h2) { font-size: 1.6em; font-weight: 600; margin: 1em 0 0.5em; color: #5d4e37; }
:deep(.tiptap h3) { font-size: 1.3em; font-weight: 600; margin: 0.8em 0 0.4em; color: #5d4e37; }
:deep(.tiptap p) { margin: 0.8em 0; }

:deep(.tiptap blockquote) {
  margin: 1.2em 0;
  padding: 1em 1.5em;
  border-left: 4px solid var(--editor-blockquote-border);
  background: rgba(201, 169, 110, 0.05);
  border-radius: 0 8px 8px 0;
  font-style: italic;
  color: #5d4e37;
}

:deep(.tiptap blockquote p) { margin: 0; }

:deep(.tiptap pre) {
  background: var(--editor-code-bg);
  border-radius: 8px;
  padding: 1em;
  margin: 1em 0;
  overflow-x: auto;
  border: 1px solid var(--editor-border);
}

:deep(.tiptap pre code) {
  color: var(--editor-text);
  font-family: 'JetBrains Mono', monospace;
  font-size: 14px;
  background: none;
  padding: 0;
}

:deep(.tiptap code) {
  background: var(--editor-code-bg);
  border-radius: 4px;
  padding: 0.2em 0.4em;
  font-family: 'JetBrains Mono', monospace;
  color: #8b4513;
}

:deep(.tiptap ul), :deep(.tiptap ol) {
  padding-left: 1.5em;
  margin: 0.8em 0;
}

:deep(.tiptap li) { margin: 0.3em 0; }
:deep(.tiptap li p) { margin: 0; }

:deep(.tiptap ul[data-type="taskList"]) {
  list-style: none;
  padding-left: 0;
}

:deep(.tiptap ul[data-type="taskList"] li) {
  display: flex;
  align-items: flex-start;
  gap: 0.5em;
}

:deep(.tiptap ul[data-type="taskList"] li > label) {
  flex-shrink: 0;
  margin-top: 0.3em;
}

:deep(.tiptap ul[data-type="taskList"] li > label input[type="checkbox"]) {
  cursor: pointer;
  width: 16px;
  height: 16px;
  accent-color: var(--editor-border-focus);
}

:deep(.tiptap mark) {
  background: var(--editor-highlight);
  border-radius: 2px;
  padding: 0 2px;
}

:deep(.tiptap table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
}

:deep(.tiptap table td), :deep(.tiptap table th) {
  border: 1px solid var(--editor-border);
  padding: 8px 12px;
  text-align: left;
}

:deep(.tiptap table th) {
  background: var(--editor-toolbar-bg);
  font-weight: 600;
}

:deep(.tiptap img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1em 0;
}

/* ==================== 拖拽手柄样式 ==================== */

/* 拖拽手柄全局样式 */
:deep(.drag-handle) {
  position: absolute;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  opacity: 0;
  transition: opacity 0.2s ease;
  color: var(--editor-text-muted);
  border-radius: 4px;
  background: var(--editor-toolbar-bg);
  border: 1px solid var(--editor-border);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 100;
  padding: 4px;
}

:deep(.drag-handle:hover) {
  background: white;
  color: var(--editor-toolbar-btn-active);
  border-color: var(--editor-border-focus);
}

:deep(.drag-handle:active) {
  cursor: grabbing;
}

:deep(.drag-handle[data-dragging="true"]) {
  opacity: 1 !important;
  color: var(--editor-toolbar-btn-active);
}

:deep(.drag-handle svg) {
  width: 14px;
  height: 14px;
}

/* 编辑器聚焦时或悬停时显示拖拽手柄 */
.block-editor.is-focused :deep(.drag-handle),
.block-editor:hover :deep(.drag-handle) {
  opacity: 0.6;
}

.block-editor.is-focused :deep(.drag-handle:hover),
.block-editor:hover :deep(.drag-handle:hover) {
  opacity: 1;
}

/* 可拖拽内容块的悬停效果 */
:deep(.ProseMirror-draggable) {
  cursor: grab;
}

:deep(.ProseMirror-draggable:active) {
  cursor: grabbing;
}

/* 拖拽时的视觉反馈 */
:deep(.ProseMirror-selectednode) {
  outline: 2px solid var(--editor-border-focus);
  outline-offset: 2px;
}

/* ==================== 占位符样式 ==================== */

.editor-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  pointer-events: none;
  opacity: 0.6;
}

.is-focused .editor-placeholder { opacity: 0; }

.placeholder-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  color: var(--editor-text-muted);
}

.placeholder-icon svg { width: 100%; height: 100%; }

.placeholder-text {
  font-size: 18px;
  color: var(--editor-text-muted);
  margin: 0 0 8px;
}

.placeholder-hint {
  font-size: 13px;
  color: var(--editor-text-muted);
  margin: 0;
  opacity: 0.7;
}

/* ==================== 底部信息样式 ==================== */

.editor-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding: 8px 16px;
  background: var(--editor-toolbar-bg);
  border-top: 1px solid var(--editor-border);
  font-size: 12px;
  color: var(--editor-text-muted);
}

.separator { opacity: 0.4; }

/* ==================== 响应式适配 ==================== */

@media (max-width: 768px) {
  .editor-toolbar { padding: 8px 12px; gap: 2px; }
  .toolbar-btn { width: 28px; height: 28px; }
  .toolbar-btn svg { width: 16px; height: 16px; }
  .toolbar-hint { display: none; }
  .editor-main { padding: 16px; }
}
</style>