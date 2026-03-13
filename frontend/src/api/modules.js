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