// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    plugins: [vue()],
    server: {
        proxy: {
            '/api': {
                target: 'https://one024-coder-back.onrender.com', // Spring Boot 端口
                changeOrigin: true
            }
        }
    }
})
