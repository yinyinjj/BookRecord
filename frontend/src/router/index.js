import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/share/:token',
    name: 'Share',
    component: () => import('@/views/Share.vue'),
    meta: { requiresAuth: false, isPublic: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'bookshelf',
        name: 'Bookshelf',
        component: () => import('@/views/Bookshelf.vue')
      },
      {
        path: 'books/:id',
        name: 'BookDetail',
        component: () => import('@/views/BookDetail.vue')
      },
      {
        path: 'notes',
        name: 'ReadingNotes',
        component: () => import('@/views/ReadingNotes.vue')
      },
      {
        path: 'quotes',
        name: 'Quotes',
        component: () => import('@/views/Quotes.vue')
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/Search.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = userStore.isAuthenticated

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if (!to.meta.requiresAuth && isAuthenticated && (to.path === '/login' || to.path === '/register')) {
    next('/')
  } else {
    next()
  }
})

export default router