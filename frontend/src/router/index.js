import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth.js'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import ArticleList from '@/views/ArticleList.vue'
import ArticleDetail from '@/views/ArticleDetail.vue'
import ArticleEdit from '@/views/ArticleEdit.vue'
import MyArticles from '@/views/MyArticles.vue'
import UserSettings from '@/views/UserSettings.vue'
import Favorites from '@/views/Favorites.vue'
import Drafts from '@/views/Drafts.vue'
import UserProfile from '@/views/UserProfile.vue'
import Notifications from '@/views/Notifications.vue'
import Messages from '@/views/Messages.vue'

const routes = [
    { path: '/', redirect: '/articles' },
    { path: '/login', name: 'Login', component: Login },
    { path: '/register', name: 'Register', component: Register },
    { path: '/articles', name: 'ArticleList', component: ArticleList },
    { path: '/article/edit/:id?', name: 'ArticleEdit', component: ArticleEdit },
    { path: '/article/:id', name: 'ArticleDetail', component: ArticleDetail },
    { path: '/my-articles', name: 'MyArticles', component: MyArticles },
    { path: '/settings', name: 'UserSettings', component: UserSettings },
    { path: '/favorites', name: 'Favorites', component: Favorites },
    { path: '/drafts', name: 'Drafts', component: Drafts },
    { path: '/user/:id', name: 'UserProfile', component: UserProfile },
    { path: '/notifications', name: 'Notifications', component: Notifications },
    { path: '/messages', name: 'Messages', component: Messages }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

const needAuth = ['ArticleEdit', 'MyArticles', 'UserSettings', 'Favorites', 'Drafts', 'Notifications', 'Messages']

router.beforeEach((to, from, next) => {
    if (needAuth.includes(to.name) && !getToken()) {
        next('/login')
        return
    }
    next()
})

export default router
