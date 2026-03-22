import api from './index'

export const authApi = {
  register(data) {
    return api.post('/auth/register', data)
  },

  login(data) {
    return api.post('/auth/login', data)
  },

  getCurrentUser() {
    return api.get('/auth/me')
  },

  logout() {
    return api.post('/auth/logout')
  }
}

export const bookApi = {
  getBooks(params) {
    return api.get('/v1/books', { params })
  },

  getBookById(id) {
    return api.get(`/v1/books/${id}`)
  },

  createBook(data) {
    return api.post('/v1/books', data)
  },

  updateBook(id, data) {
    return api.put(`/v1/books/${id}`, data)
  },

  deleteBook(id) {
    return api.delete(`/v1/books/${id}`)
  },

  updateReadingStatus(id, status) {
    return api.patch(`/v1/books/${id}/status`, null, { params: { status } })
  },

  updateReadingProgress(id, data) {
    return api.patch(`/v1/books/${id}/progress`, data)
  },

  searchBooks(keyword, params) {
    return api.get('/v1/books/search', { params: { keyword, ...params } })
  },

  getBooksByStatus(status) {
    return api.get(`/v1/books/status/${status}`)
  },

  getStatistics() {
    return api.get('/v1/books/statistics')
  },

  // ==================== 图书信息识别接口 ====================

  /**
   * 根据ISBN查询图书信息
   * 调用外部API获取书籍详细信息，用于自动识别
   * @param {string} isbn - ISBN编号（10位或13位）
   * @returns {Promise} 图书信息
   */
  getBookByIsbn(isbn) {
    return api.get(`/v1/books/isbn/${isbn}`)
  },

  /**
   * 根据书名搜索图书信息
   * 返回匹配的图书列表供用户选择
   * @param {string} title - 书名关键词
   * @returns {Promise} 图书信息列表
   */
  searchBooksByTitle(title) {
    return api.get('/v1/books/search-info', { params: { title } })
  },

  // ==================== 书单导入接口 ====================

  /**
   * 预览导入文件
   * 上传CSV文件并返回解析预览结果
   * @param {FormData} formData - 包含文件的FormData对象
   * @returns {Promise} 预览结果
   */
  previewImport(formData) {
    return api.post('/v1/books/import/preview', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * 确认导入书单
   * 执行批量导入操作
   * @param {Object} data - 导入确认请求数据
   * @param {string} data.sourceType - 文件来源类型（douban/weread）
   * @param {string} data.duplicateStrategy - 重复处理策略（skip/overwrite）
   * @param {Array<number>} data.selectedIndexes - 要导入的书籍索引列表
   * @returns {Promise} 导入结果
   */
  confirmImport(data) {
    return api.post('/v1/books/import/confirm', data)
  },

  /**
   * 取消导入
   * 清除缓存的预览数据
   * @returns {Promise}
   */
  cancelImport() {
    return api.delete('/v1/books/import/cancel')
  }
}

export const readingNoteApi = {
  getNotesByBookId(bookId, params) {
    return api.get(`/v1/books/${bookId}/notes`, { params })
  },

  getNotesByQuoteId(quoteId) {
    return api.get(`/v1/quotes/${quoteId}/notes`)
  },

  getNoteById(id) {
    return api.get(`/v1/notes/${id}`)
  },

  createNoteForBook(bookId, data) {
    return api.post(`/v1/books/${bookId}/notes`, data)
  },

  createNoteForQuote(quoteId, data) {
    return api.post(`/v1/quotes/${quoteId}/notes`, data)
  },

  updateNote(id, data) {
    return api.put(`/v1/notes/${id}`, data)
  },

  deleteNote(id) {
    return api.delete(`/v1/notes/${id}`)
  },

  searchNotes(keyword, params) {
    return api.get('/v1/notes/search', { params: { keyword, ...params } })
  }
}

export const quoteApi = {
  getQuotesByBookId(bookId, params) {
    return api.get(`/v1/books/${bookId}/quotes`, { params })
  },

  getQuoteById(id) {
    return api.get(`/v1/quotes/${id}`)
  },

  createQuote(bookId, data) {
    return api.post(`/v1/books/${bookId}/quotes`, data)
  },

  updateQuote(id, data) {
    return api.put(`/v1/quotes/${id}`, data)
  },

  deleteQuote(id) {
    return api.delete(`/v1/quotes/${id}`)
  },

  getRandomQuote() {
    return api.get('/v1/quotes/random')
  },

  searchQuotes(keyword, params) {
    return api.get('/v1/quotes/search', { params: { keyword, ...params } })
  },

  getNotesForQuote(quoteId) {
    return api.get(`/v1/quotes/${quoteId}/notes`)
  }
}

/**
 * 搜索 API 模块
 * 提供全局搜索和高级搜索功能
 */
export const searchApi = {
  /**
   * 基础全局搜索
   * @param {string} keyword - 搜索关键词
   * @returns {Promise} 搜索结果
   */
  search(keyword) {
    return api.get('/v1/search', { params: { keyword } })
  },

  /**
   * 高级搜索
   * 支持多条件组合查询
   * @param {Object} params - 搜索参数
   * @param {string} params.keyword - 搜索关键词
   * @param {string} params.type - 搜索类型 (all/books/notes/quotes)
   * @param {string} params.startDate - 开始时间
   * @param {string} params.endDate - 结束时间
   * @param {Array<string>} params.readingStatuses - 阅读状态列表
   * @param {Array<string>} params.noteTypes - 感悟类型列表
   * @param {Array<string>} params.tags - 标签列表
   * @param {Array<string>} params.colors - 金句颜色列表
   * @param {string} params.combineMode - 条件组合方式 (AND/OR)
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 搜索结果
   */
  advancedSearch(params) {
    return api.post('/v1/search/advanced', params)
  }
}

/**
 * 分享 API 模块
 * 提供感悟和金句的分享功能
 */
export const shareApi = {
  /**
   * 创建感悟分享链接
   * @param {number} noteId - 感悟ID
   * @param {Object} data - 分享设置
   * @param {number} data.expiryDays - 有效期天数（0=永久，1=1天，7=7天）
   * @returns {Promise} 分享响应（包含token和shareUrl）
   */
  shareNote(noteId, data = {}) {
    return api.post(`/v1/notes/${noteId}/share`, data)
  },

  /**
   * 创建金句分享链接
   * @param {number} quoteId - 金句ID
   * @param {Object} data - 分享设置
   * @param {number} data.expiryDays - 有效期天数（0=永久，1=1天，7=7天）
   * @returns {Promise} 分享响应（包含token和shareUrl）
   */
  shareQuote(quoteId, data = {}) {
    return api.post(`/v1/quotes/${quoteId}/share`, data)
  },

  /**
   * 获取分享内容（公开访问，无需认证）
   * @param {string} token - 分享令牌
   * @returns {Promise} 分享内容
   */
  getSharedContent(token) {
    return api.get(`/public/share/${token}`)
  }
}

/**
 * 统计 API 模块
 * 提供阅读统计数据相关接口
 */
export const statisticsApi = {
  /**
   * 获取阅读趋势统计数据
   * @param {string} range - 时间范围：6m（近6个月）、1y（近1年）、all（全部）
   * @returns {Promise} 趋势统计数据
   */
  getTrend(range = '1y') {
    return api.get('/v1/statistics/trend', { params: { range } })
  },

  /**
   * 获取分类统计数据
   * @returns {Promise} 分类统计数据（阅读状态分布、感悟类型分布、热门标签）
   */
  getCategories() {
    return api.get('/v1/statistics/categories')
  }
}