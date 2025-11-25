<template>
  <div class="upload-container">

    <div class="upload-card">
      <h2 class="title">上传封面图</h2>

      <!-- 选择图片框 -->
      <div class="select-area" @click="triggerSelect">
        <input type="file" ref="fileInput" accept="image/*" @change="handleSelect" hidden />

        <div v-if="!preview" class="placeholder">
          点击选择封面图片
        </div>

        <img v-else :src="preview" class="preview-img" />
      </div>

      <!-- 上传按钮 -->
      <button class="upload-btn" @click="start">
        上传并开始
      </button>

    </div>

  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const fileInput = ref(null);
const preview = ref(null);

function triggerSelect() {
  fileInput.value.click();
}

function handleSelect(e) {
  const file = e.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = ev => {
    preview.value = ev.target.result;
  };
  reader.readAsDataURL(file);
}

function start() {
  if (!preview.value) {
    alert("请先选择一张封面图");
    return;
  }

  // 保存封面到 sessionStorage
  sessionStorage.setItem("quizCover", preview.value);

  // 跳转到全屏页
  router.push("/home");
}
</script>

<style scoped>
.upload-container {
  width: 100vw;
  height: 100vh;
  background: #101827;
  display: flex;
  justify-content: center;
  align-items: center;
}

.upload-card {
  width: 360px;
  padding: 30px;
  border-radius: 18px;
  background: rgba(255,255,255,0.05);
  backdrop-filter: blur(16px);
  text-align: center;
  color: white;
}

.title {
  margin-bottom: 20px;
  font-size: 22px;
}

.select-area {
  width: 100%;
  height: 180px;
  background: rgba(255,255,255,0.08);
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.placeholder {
  color: #ddd;
  font-size: 16px;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-btn {
  margin-top: 20px;
  width: 100%;
  padding: 12px;
  background: #6366f1;
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 16px;
  cursor: pointer;
}
.upload-btn:hover {
  background: #7c81ff;
}
</style>
