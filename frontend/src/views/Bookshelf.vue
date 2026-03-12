<template>
  <div class="bookshelf">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的书架</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加书籍
          </el-button>
        </div>
      </template>

      <!-- Search and Filter -->
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索书籍..."
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>

        <el-select v-model="filterStatus" placeholder="阅读状态" @change="handleFilter">
          <el-option label="全部" value="" />
          <el-option label="想读" value="WANT_TO_READ" />
          <el-option label="在读" value="READING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已放弃" value="ABANDONED" />
        </el-select>
      </div>

      <!-- Book List -->
      <el-table :data="books" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="书名" min-width="150" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="readingStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.readingStatus)">
              {{ getStatusText(row.readingStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="120">
          <template #default="{ row }">
            <div v-if="row.progress !== null">
              <el-progress :percentage="row.progress" :show-text="false" />
              <span style="font-size: 12px">{{ row.progress }}%</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <span v-if="row.rating">{{ row.rating }}/5</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text @click="viewBook(row.id)">查看</el-button>
            <el-button type="primary" text @click="editBook(row)">编辑</el-button>
            <el-button type="danger" text @click="deleteBook(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadBooks"
        @current-change="loadBooks"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑书籍' : '添加书籍'"
      width="600px"
    >
      <el-form ref="bookFormRef" :model="bookForm" :rules="bookRules" label-width="100px">
        <el-form-item label="书名" prop="title">
          <el-input v-model="bookForm.title" placeholder="请输入书名" />
        </el-form-item>

        <el-form-item label="作者" prop="author">
          <el-input v-model="bookForm.author" placeholder="请输入作者" />
        </el-form-item>

        <el-form-item label="阅读状态" prop="readingStatus">
          <el-select v-model="bookForm.readingStatus" placeholder="请选择阅读状态">
            <el-option label="想读" value="WANT_TO_READ" />
            <el-option label="在读" value="READING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已放弃" value="ABANDONED" />
          </el-select>
        </el-form-item>

        <el-form-item label="总页数">
          <el-input-number v-model="bookForm.pageCount" :min="0" />
        </el-form-item>

        <el-form-item label="当前页码">
          <el-input-number v-model="bookForm.currentPage" :min="0" />
        </el-form-item>

        <el-form-item label="ISBN">
          <el-input v-model="bookForm.isbn" placeholder="请输入ISBN" />
        </el-form-item>

        <el-form-item label="出版社">
          <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
        </el-form-item>

        <el-form-item label="评分">
          <el-rate v-model="bookForm.rating" :max="5" />
        </el-form-item>

        <el-form-item label="简介">
          <el-input v-model="bookForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookApi } from '@/api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const books = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterStatus = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const bookFormRef = ref(null)

const bookForm = reactive({
  title: '',
  author: '',
  readingStatus: 'WANT_TO_READ',
  pageCount: null,
  currentPage: 0,
  isbn: '',
  publisher: '',
  rating: null,
  description: ''
})

const bookRules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  readingStatus: [{ required: true, message: '请选择阅读状态', trigger: 'change' }]
}

onMounted(() => {
  loadBooks()
})

async function loadBooks() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const response = await bookApi.getBooks(params)
    books.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('Failed to load books:', error)
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  if (!searchKeyword.value) {
    loadBooks()
    return
  }
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const response = await bookApi.searchBooks(searchKeyword.value, params)
    books.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('Failed to search books:', error)
  } finally {
    loading.value = false
  }
}

async function handleFilter() {
  if (!filterStatus.value) {
    loadBooks()
    return
  }
  loading.value = true
  try {
    const response = await bookApi.getBooksByStatus(filterStatus.value)
    books.value = response.data
    total.value = response.data.length
  } catch (error) {
    console.error('Failed to filter books:', error)
  } finally {
    loading.value = false
  }
}

function showAddDialog() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function editBook(book) {
  isEdit.value = true
  editId.value = book.id
  Object.assign(bookForm, book)
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!bookFormRef.value) return

  await bookFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await bookApi.updateBook(editId.value, bookForm)
          ElMessage.success('更新成功')
        } else {
          await bookApi.createBook(bookForm)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadBooks()
      } catch (error) {
        console.error('Failed to save book:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

async function deleteBook(id) {
  try {
    await ElMessageBox.confirm('确定要删除这本书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await bookApi.deleteBook(id)
    ElMessage.success('删除成功')
    loadBooks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete book:', error)
    }
  }
}

function viewBook(id) {
  router.push(`/books/${id}`)
}

function resetForm() {
  Object.assign(bookForm, {
    title: '',
    author: '',
    readingStatus: 'WANT_TO_READ',
    pageCount: null,
    currentPage: 0,
    isbn: '',
    publisher: '',
    rating: null,
    description: ''
  })
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
.bookshelf {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}
</style>