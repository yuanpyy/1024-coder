<template>
  <div class="upload-page">
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>

    <div class="upload-card">
      <div class="card-header">
        <h2 class="title">题库导入</h2>
        <p class="subtitle">支持解析 .docx 格式的 Word 文档</p>
      </div>

      <div
          class="upload-area"
          :class="{ 'is-dragging': isDragging, 'has-file': file }"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleDrop"
          @click="triggerInput"
      >
        <input
            type="file"
            ref="fileInput"
            @change="handleFileSelect"
            accept=".docx"
        />

        <div class="placeholder" v-if="!file">
          <div class="icon-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 16v4a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-4"/>
              <polyline points="16 8 12 3 8 8"/>
              <line x1="12" y1="3" x2="12" y2="16"/>
            </svg>
          </div>
          <p class="main-text">点击 or 拖拽文件至此</p>
          <p class="sub-text">仅支持 .docx 文件</p>
        </div>

        <div class="file-info" v-else>
          <div class="file-icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
              <line x1="10" y1="9" x2="8" y2="9"></line>
            </svg>
          </div>
          <div class="file-details">
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ formatSize(file.size) }}</span>
          </div>
          <button class="remove-btn" @click.stop="removeFile" title="移除">&times;</button>
        </div>
      </div>

      <div class="actions">
        <button
            class="upload-btn"
            :class="{ 'loading': isUploading }"
            @click="uploadFile"
            :disabled="isUploading"
        >
          <span v-if="!isUploading">开始解析并上传</span>
          <span v-else class="spinner">
            <svg viewBox="0 0 50 50"><circle cx="25" cy="25" r="20" fill="none" stroke-width="5"></circle></svg>
          </span>
        </button>
      </div>

      <transition name="fade">
        <div class="message" :class="{ error: isError }" v-if="message">{{ message }}</div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const file = ref(null)
const message = ref('')
const isError = ref(false)
const isDragging = ref(false)
const isUploading = ref(false)
const fileInput = ref(null)

/* 触发隐藏的文件输入框 */
function triggerInput() {
  fileInput.value.click()
}

/* 格式化文件大小 */
function formatSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

/* 文件选择处理 */
function handleFileSelect(event) {
  const selected = event.target.files[0]
  validateAndSetFile(selected)
}

/* 拖拽处理 */
function handleDrop(event) {
  isDragging.value = false
  const dropped = event.dataTransfer.files[0]
  validateAndSetFile(dropped)
}

/* 验证文件 */
function validateAndSetFile(f) {
  message.value = ''
  isError.value = false
  if (!f) return

  if (!f.name.endsWith('.docx')) {
    message.value = '格式错误：请上传 .docx 文件'
    isError.value = true
    return
  }
  file.value = f
}

/* 移除文件 */
function removeFile() {
  file.value = null
  message.value = ''
  if(fileInput.value) fileInput.value.value = ''
}

/* 上传逻辑 */
async function uploadFile() {
  if (!file.value) {
    message.value = '请先选择一个文件'
    isError.value = true
    return
  }

  isUploading.value = true
  message.value = ''

  const formData = new FormData()
  formData.append('file', file.value)

  try {
    const res = await fetch("https://one024-coder-back.onrender.com/api/quiz/upload", {
      method: 'POST',
      body: formData
    })

    if (!res.ok) throw new Error('解析失败，请检查文件格式')

    const data = await res.json()
    localStorage.setItem('quizData', JSON.stringify(data))
    router.push("/upload-cover");


  } catch (err) {
    isError.value = true
    message.value = err.message || '上传过程中发生错误'
    isUploading.value = false
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

/* ------ 全局容器 ------ */
.upload-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #0f172a;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
  color: #fff;
}

/* 背景光斑 */
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  opacity: 0.6;
  animation: float 10s infinite ease-in-out alternate;
}
.orb-1 { width: 300px; height: 300px; background: #6366f1; top: 10%; left: 20%; }
.orb-2 { width: 400px; height: 400px; background: #ec4899; bottom: 10%; right: 15%; animation-delay: -5s; }

@keyframes float {
  0% { transform: translate(0, 0); }
  100% { transform: translate(30px, 50px); }
}

/* ------ 卡片容器 ------ */
.upload-card {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 480px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center; /* 确保所有子元素水平居中 */
}

/* 确保子元素占满宽度 */
.card-header, .upload-area, .actions {
  width: 100%;
}

.card-header { text-align: center; margin-bottom: 30px; }
.title { margin: 0; font-size: 28px; font-weight: 700; background: linear-gradient(to right, #fff, #cbd5e1); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.subtitle { margin: 8px 0 0; font-size: 14px; color: #94a3b8; }

/* ------ 上传区域 ------ */
.upload-area {
  background: rgba(255, 255, 255, 0.03);
  border: 2px dashed rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 220px;

  /* 关键：使用 Flex 确保内部内容垂直水平居中 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.upload-area input { display: none; }
.upload-area:hover, .upload-area.is-dragging { background: rgba(99, 102, 241, 0.1); border-color: #818cf8; transform: scale(1.01); }
.upload-area.has-file { border-style: solid; border-color: rgba(255, 255, 255, 0.15); padding: 20px; }

/* ------ 图标容器 (绝对居中修正) ------ */
.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.icon-wrapper {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;

  /* 使用 Grid + Place-Items 实现最稳妥的居中 */
  display: grid;
  place-items: center;

  margin-bottom: 16px;
  color: #e2e8f0;
  transition: transform 0.3s ease;
}

/* 悬停微动效 */
.upload-area:hover .icon-wrapper {
  transform: translateY(-5px) scale(1.1);
  background: rgba(99, 102, 241, 0.3);
  color: #fff;
}

.icon-wrapper svg {
  width: 32px;
  height: 32px;
  display: block; /* 防止行内基线干扰 */
}

.main-text { font-size: 16px; font-weight: 500; color: #e2e8f0; margin: 0 0 4px; }
.sub-text { font-size: 13px; color: #64748b; margin: 0; }

/* ------ 文件信息展示 ------ */
.file-info { display: flex; align-items: center; width: 100%; gap: 15px; background: rgba(255,255,255,0.08); padding: 15px; border-radius: 12px; }
.file-icon { width: 48px; height: 48px; background: #3b82f6; border-radius: 10px; display: grid; place-items: center; color: white; flex-shrink: 0; }
.file-icon svg { width: 24px; height: 24px; display: block; }
.file-details { flex: 1; text-align: left; overflow: hidden; }
.file-name { display: block; font-weight: 600; font-size: 15px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: #fff; }
.file-size { display: block; font-size: 12px; color: #94a3b8; margin-top: 2px; }
.remove-btn { background: none; border: none; color: #94a3b8; font-size: 24px; cursor: pointer; padding: 0 8px; transition: color 0.2s; }
.remove-btn:hover { color: #ef4444; }

/* ------ 按钮区域 (居中修复) ------ */
.actions {
  margin-top: 30px;
  display: flex;
  justify-content: center; /* 水平居中 */
}

.upload-btn {
  min-width: 200px;
  padding: 14px 40px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4);

  /* 按钮内部文字居中 */
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

.upload-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(99, 102, 241, 0.6);
}

.upload-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* Loading 动画 */
.spinner svg {
  width: 24px;
  height: 24px;
  animation: rotate 2s linear infinite;
}
.spinner circle {
  stroke: #ffffff;
  stroke-width: 4;
  stroke-dasharray: 90, 150;
  stroke-dashoffset: 0;
  stroke-linecap: round;
  animation: dash 1.5s ease-in-out infinite;
}

@keyframes rotate { 100% { transform: rotate(360deg); } }
@keyframes dash {
  0% { stroke-dasharray: 1, 150; stroke-dashoffset: 0; }
  50% { stroke-dasharray: 90, 150; stroke-dashoffset: -35; }
  100% { stroke-dasharray: 90, 150; stroke-dashoffset: -124; }
}

/* ------ 错误提示 ------ */
.message {
  margin-top: 16px;
  text-align: center;
  font-size: 14px;
  color: #10b981;
  min-height: 20px;
}
.message.error { color: #ef4444; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
