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