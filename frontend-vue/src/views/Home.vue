<template>
  <div class="home" :style="bgStyle" @mousemove="handleMouseMove">
    <div class="enter-btn" :class="{ show: showBtn }" @click="goQuiz">
      进入答题界面 →
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

// --------------------
// 背景图读取
// --------------------
const cover = sessionStorage.getItem("quizCover") || "";
const bgStyle = ref({
  background: cover ? `url(${cover}) center/cover no-repeat` : "#222",
});

// 进入答题按钮
const showBtn = ref(false);

let hideTimer = null;
let lastMove = 0;

// --------------------------------------------------
// ⭐ 自动进入全屏 + 自动保持全屏（Home 页专属）
// --------------------------------------------------
function enterFull() {
  const el = document.documentElement;
  if (!document.fullscreenElement) {
    el.requestFullscreen().catch(() => {});
  }
}

// 鼠标移动触发按钮显示，并尝试恢复全屏
function handleMouseMove() {
  enterFull(); // ⭐ 自动保持全屏

  const now = Date.now();
  if (now - lastMove < 500) return;
  lastMove = now;

  showBtn.value = true;
  clearTimeout(hideTimer);
  hideTimer = setTimeout(() => (showBtn.value = false), 1800);
}

onMounted(() => {
  // 初次加载 → 全屏
  enterFull();

  // 如果用户按 ESC 退出 → 下次鼠标动瞬间再进入
  document.addEventListener("fullscreenchange", () => {
    if (!document.fullscreenElement) {
      // 等待用户下一次鼠标动时恢复
    }
  });
});

onBeforeUnmount(() => {
  // 离开 Home 页时不再强制全屏
  document.removeEventListener("fullscreenchange", () => {});
});

// --------------------
// 跳转答题
// --------------------
function goQuiz() {
  router.push("/quiz");
}
</script>

<style scoped>
.home {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.enter-btn {
  position: absolute;
  bottom: 24px;
  right: 24px;
  padding: 14px 24px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(6px);
  border-radius: 12px;
  font-size: 20px;
  color: white;
  opacity: 0;
  transform: translateY(10px);
  transition: 0.4s ease;
}

.enter-btn.show {
  opacity: 1;
  transform: translateY(0);
}
</style>
