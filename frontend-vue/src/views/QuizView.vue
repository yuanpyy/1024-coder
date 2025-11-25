<template>
  <div
      class="quiz-container"
      ref="quizScreen"
      @mousemove="handleMouseMove"
      tabindex="0"
      :style="backgroundStyle"
  >

    <!-- 背景上传 input（隐藏） -->
    <input type="file" ref="bgInput" style="display:none" accept="image/*" @change="uploadBackground" />

    <!-- 半透明遮罩 -->
    <div class="bg-overlay"></div>

    <!-- 设置按钮 -->
    <div class="settings-btn" @click="showSettings = true">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="28" height="28" fill="white">
        <path d="M19.14,12.94a7.14,7.14,0,0,0,.05-.94,7.14,7.14,0,0,0-.05-.94l2.03-1.58a.5.5,0,0,0,.12-.64l-1.92-3.32a.5.5,0,0,0-.61-.22l-2.39,1a7.07,7.07,0,0,0-1.63-.95L14.5,2.81a.5.5,0,0,0-.5-.31h-3.99a.5.5,0,0,0-.5.31L9.38,5.91a7.07,7.07,0,0,0-1.63.95l-2.39-1a.5.5,0,0,0-.61.22L2.83,9.78a.5.5,0,0,0,.12.64l2.03,1.58a7.14,7.14,0,0,0,0,1.88L2.95,14.58a.5.5,0,0,0-.12.64l1.92,3.32a.5.5,0,0,0,.61.22l2.39-1a7.07,7.07,0,0,0,1.63.95l.3,2.1a.5.5,0,0,0,.5.31h3.99a.5.5,0,0,0,.5-.31l.3-2.1a7.07,7.07,0,0,0,1.63-.95l2.39,1a.5.5,0,0,0,.61-.22l1.92-3.32a.5.5,0,0,0-.12-.64ZM12,15.5A3.5,3.5,0,1,1,15.5,12,3.5,3.5,0,0,1,12,15.5Z"/>
      </svg>
    </div>
    <!-- 全屏按钮 -->
    <div class="fullscreen-btn" @click="toggleFullScreen">

      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="28" height="28" fill="white">
        <path d="M7 7h3V5H5v5h2V7zm10 0v3h2V5h-5v2h3zm-3 10h3v-3h2v5h-5v-2zm-4 0v2H5v-5h2v3h3z"/>
      </svg>
    </div>
    <!-- 首页按钮 -->
    <div class="home-btn" @click="goHome">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="28" height="28" fill="white">
        <path d="M12 3l9 8h-3v8h-12v-8h-3l9-8z"/>
      </svg>
    </div>


    <!-- 设置弹窗 -->
    <transition name="fade">
      <div class="settings-modal" v-if="showSettings">
        <h3>显示设置</h3>

        <div class="setting-row">
          <label>题目垂直位置(px):</label>
          <input type="number" v-model.number="settings.questionY">
        </div>

        <div class="setting-row">
          <label>题目字体(px):</label>
          <input type="number" v-model.number="settings.questionFont">
        </div>

        <div class="setting-row">
          <label>题目颜色:</label>
          <input type="color" v-model="settings.questionColor" style="width:50px; padding:0; border:none; background:none;" />
          <button @click="settings.questionColor='#000000'" style="margin-left:6px;">黑色</button>
          <button @click="settings.questionColor='#ffffff'" style="margin-left:6px;">白色</button>
        </div>

        <div class="setting-row">
          <label>答案字体(px):</label>
          <input type="number" v-model.number="settings.answerFont">
        </div>

        <!-- 上传背景图按钮 -->
        <div class="setting-row">
          <button @click="triggerBgUpload">上传背景图</button>
          <button v-if="bgImage" @click="clearBackground" style="margin-left:8px;background:#f44336">清除背景</button>
        </div>

        <button @click="showSettings=false">关闭</button>
      </div>
    </transition>

    <!-- 题目 + 选项 -->
    <transition name="slide-fade" mode="out-in">
      <div
          class="question"
          :key="currentIndex"
          :style="{
            top: settings.questionY + 'px',
            fontSize: settings.questionFont + 'px',
            color: settings.questionColor,
            textAlign: 'left'
          }"
      >
        <div class="question-number">{{ currentIndex + 1 }}</div>
        <div style="margin-left: 58px;">
          {{ currentQuestion.question }}

          <!-- 选项列表 -->
          <ul class="options-list" v-if="currentQuestion.options && currentQuestion.options.length">
            <li v-for="(opt, idx) in currentQuestion.options" :key="idx">
              {{ String.fromCharCode(65 + idx) }}. {{ opt }}
            </li>
          </ul>
        </div>
      </div>
    </transition>

    <!-- 答案 -->
    <transition name="fade">
      <div
          v-if="showAnswer"
          class="answer"
          :style="{
            top: answerY + 'px',
            fontSize: settings.answerFont + 'px'
          }"
      >
        <strong>答案：</strong>{{ currentQuestion.answer }}
      </div>
    </transition>

    <!-- 左下角箭头 -->
    <div :class="['arrows', showArrows ? 'visible' : '']">
      <span class="arrow left" @click="prevQuestion">&#9664;</span>
      <span class="arrow right" @click="nextQuestion">&#9654;</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'

const quizScreen = ref(null)
const isFullScreen = ref(false)

/* 全屏切换函数 */
function toggleFullScreen() {
  const el = quizScreen.value
  if (!document.fullscreenElement) {
    el.requestFullscreen().then(() => isFullScreen.value = true)
  } else {
    document.exitFullscreen().then(() => isFullScreen.value = false)
  }
}

/* ESC 退出全屏监听 */
onMounted(() => {
  window.addEventListener('keydown', e => {
    if (e.code === "Escape" && document.fullscreenElement) {
      document.exitFullscreen().then(() => isFullScreen.value = false)
    }
  })
})
import { useRouter } from "vue-router";
const router = useRouter();

function goHome() {
  router.push("/home")
}


/* 清理选项，将后端对象格式转换为字符串数组 */
function cleanOptions(options) {
  return options.map(opt => {
    if (typeof opt === 'string') return opt.replace(/^[A-Z]\.\s*/, '').trim()
    if (opt && typeof opt.value === 'string') return opt.value.trim()
    return ''
  })
}

/* 题库 */
const rawQuizData = JSON.parse(localStorage.getItem('quizData') || '[]')
const quizData = ref(rawQuizData.map(q => ({
  ...q,
  options: q.options ? cleanOptions(q.options) : []
})))
const currentIndex = ref(0)

/* UI */
const showAnswer = ref(false)
const showArrows = ref(false)
const showSettings = ref(false)
let hideTimer = null

/* 当前题 */
const currentQuestion = computed(() => quizData.value[currentIndex.value] || {})

/* 设置 */
const settings = ref({
  questionY: 150,
  questionFont: 36,
  answerFont: 40,
  questionColor: '#ffffff'
})

/* 背景图片（base64 或 url） */
const bgImage = ref(localStorage.getItem('quizBackground') || '')

/* backgroundStyle 绑定到 .quiz-container */
const backgroundStyle = computed(() => {
  if (bgImage.value) {
    return {
      backgroundImage: `linear-gradient(rgba(8,8,12,0.25), rgba(8,8,12,0.25)), url(${bgImage.value})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat'
    }
  } else {
    return { background: 'linear-gradient(135deg,#1c1c2c,#645e9d)' }
  }
})

/* 固定答案位置（中间偏下） */
const answerY = computed(() => Math.round(window.innerHeight * 0.7))

/* 背景上传相关 */
const bgInput = ref(null)
function triggerBgUpload() {
  const el = bgInput.value || document.querySelector('input[type="file"]')
  el && el.click()
}
function uploadBackground(e) {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = ev => {
    bgImage.value = ev.target.result
    try { localStorage.setItem('quizBackground', ev.target.result) } catch(_) {}
  }
  reader.readAsDataURL(file)
}
function clearBackground() {
  bgImage.value = ''
  try { localStorage.removeItem('quizBackground') } catch(_) {}
}

/* 翻题 */
function nextQuestion() {
  if (currentIndex.value < quizData.value.length - 1) {
    currentIndex.value++
    showAnswer.value = false
  }
}
function prevQuestion() {
  if (currentIndex.value > 0) {
    currentIndex.value--
    showAnswer.value = false
  }
}
function toggleAnswer() {
  showAnswer.value = !showAnswer.value
}

/* 鼠标显示箭头 */
function handleMouseMove() {
  showArrows.value = true
  clearTimeout(hideTimer)
  hideTimer = setTimeout(() => (showArrows.value = false), 1500)
}

/* 键盘事件 & focus */
onMounted(() => {
  window.addEventListener('keydown', e => {
    if (e.code === "Space") {
      e.preventDefault()
      toggleAnswer()
    } else if (e.code === "ArrowRight") nextQuestion()
    else if (e.code === "ArrowLeft") prevQuestion()
  })
})
</script>

<style scoped>
/* 首页按钮 */
.home-btn {
  position: absolute;
  top: 12px;
  left: 72px; /* 在设置按钮右边、全屏按钮左边 */
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255,255,255,0.25);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  z-index: 20;
  box-shadow: 0 4px 10px rgba(0,0,0,0.25);
  transition: 0.25s;
}
.home-btn:hover{
  background: rgba(255,255,255,0.35);
}

/* 全屏按钮样式 */
.fullscreen-btn {
  position: absolute;
  top: 12px;
  left: 128px; /* 设置按钮右边 */
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255,255,255,0.25);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  z-index: 20;
  box-shadow: 0 4px 10px rgba(0,0,0,0.25);
}
.fullscreen-btn:hover {
  opacity: 1;
}
/* 题目编号 */
.question-number {
  position: absolute;
  left: 0;
  top: 10px;
  background: rgba(255,255,255,0.25);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  padding: 6px 14px;
  font-size: 20px;
  font-weight: 600;
  border-radius: 12px;
  color: #fff;
  box-shadow: 0 4px 10px rgba(0,0,0,0.25);
}

/* 选项列表 */
.options-list {
  margin-top: 12px;
  padding-left: 0;
  list-style: none;

  display: flex;
  flex-wrap: wrap;        /* 自动换行 */
  gap: 10px 24px;          /* 上下间距 8px，左右间距 24px */
}

.options-list li {
  flex: 1 1 calc(50% - 12px); /* 两列布局，每列最大占50%减间距 */
  min-width: 150px;            /* 最小宽度保证不太窄 */

  font-size: 1em;
  line-height: 1.6;
  color: inherit;
}

/* 去掉 number 输入框的上下箭头（Chrome, Safari, Edge） */
input[type=number]::-webkit-outer-spin-button,
input[type=number]::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* Firefox 去掉上下箭头 */
input[type=number] {
  -moz-appearance: textfield;
}

/* 全局基础样式 */
:global(html), :global(body){
  margin:0;
  padding:0;
  width:100%;
  height:100%;
  overflow:hidden;
}
:global(*){ box-sizing:border-box; }

/* 主容器 */
.quiz-container {
  width:100vw;
  height:100vh;
  position:relative;
  color:white;
  font-family:"Segoe UI",sans-serif;
}

/* 半透明遮罩 */
.bg-overlay{
  position:absolute;
  inset:0;
  background: linear-gradient(rgba(6,6,10,0.25), rgba(6,6,10,0.25));
  pointer-events:none;
  z-index:0;
}

/* 题目样式 */
.question{
  position:absolute;
  width:calc(100% - 80px);
  left:40px;
  line-height:1.5;
  transition:.4s ease;
  z-index:2;
  text-align:left;
  padding:0;
  margin:0;
}

/* 答案样式 */
.answer{
  position:absolute;
  width:100%;
  left:0;
  text-align:center;
  color:#ffeaa7;
  padding:12px 40px;
  line-height:1.5;
  transition:opacity .4s, transform .4s;
  z-index:2;
}

/* 设置按钮 */
.settings-btn {
  position: absolute;
  top: 12px;
  left: 16px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  z-index: 20;
  box-shadow: 0 4px 10px rgba(0,0,0,0.25);
}
.settings-btn:hover{ opacity:1; }

/* 设置弹窗 */
.settings-modal {
  position:absolute;
  top:60px;
  left:50%;
  transform:translateX(-50%);
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius:16px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
  padding:24px 36px;
  z-index:30;
  color:white;
  font-weight:500;
}

/* 设置行 */
.setting-row {
  display:flex;
  justify-content:space-between;
  align-items:center;
  margin:12px 0;
  font-size:15px;
}
.setting-row input {
  width:80px;
  padding:6px 8px;
  border-radius:8px;
  border:none;
  background: rgba(255,255,255,0.1);
  color:white;
  outline:none;
}
.setting-row button {
  margin-left:8px;
  padding:6px 12px;
  border:none;
  border-radius:8px;
  cursor:pointer;
  background: rgba(255,255,255,0.2);
  color:white;
  transition: all 0.2s;
}
.setting-row button:hover {
  background: rgba(255,255,255,0.35);
}

/* 左下角箭头 */
.arrows{
  position:absolute;
  bottom:20px;
  left:20px;
  display:flex;
  gap:10px;
  opacity:0;
  transition:opacity .3s;
  z-index:20;
}
.arrows.visible{ opacity:1; }

.arrow{
  font-size:26px;
  padding:6px 10px;
  background:rgba(0,0,0,.25);
  border-radius:8px;
  cursor:pointer;
  transition:.2s;
}
.arrow:hover{
  background:rgba(0,0,0,.45);
}

/* 动画 */
.fade-enter-active,.fade-leave-active{ transition:opacity .4s; }
.fade-enter-from,.fade-leave-to{ opacity:0; }

.slide-fade-enter-active{ transition:all .45s; }
.slide-fade-enter-from{
  opacity:0;
  transform:translateY(30px);
}

</style>
