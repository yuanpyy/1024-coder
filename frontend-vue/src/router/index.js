// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

import UploadQuizView from '../views/UploadView.vue'
import UploadCoverView from '../views/UploadCover.vue'
import PreviewView from '../views/Home.vue'
import QuizView from '../views/QuizView.vue'

const routes = [
    { path: '/', redirect: '/upload-quiz' },

    // 第 1 步：上传题库
    { path: '/upload-quiz', component: UploadQuizView },

    // 第 2 步：上传封面
    { path: '/upload-cover', component: UploadCoverView },

    // 第 3 步：全屏展示封面
    { path: '/home', component: PreviewView },

    // 第 4 步：答题界面
    { path: '/quiz', component: QuizView }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
