<template>
  <div class="big-file-upload">
    <el-alert
      v-if="!compact"
      title="大文件分片上传：支持视频与常见文档，按分片上传并自动合并，中途暂停/刷新后可继续上传"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <div class="upload-picker">
      <input ref="fileInputRef" type="file" :accept="accept" @change="handleFileChange" />
      <div v-if="selectedFile" class="file-meta">
        <span class="file-name">{{ selectedFile.name }}</span>
        <span class="file-size">{{ formatSize(selectedFile.size) }}</span>
      </div>
    </div>

    <el-form inline style="margin-top: 12px">
      <el-form-item label="分片大小">
        <el-select v-model="chunkSizeMb" :disabled="!!uploadId || running" style="width: 120px">
          <el-option v-for="size in chunkSizeOptions" :key="size" :label="size + ' MB'" :value="size"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="running" :disabled="!selectedFile || running" @click="startUpload">
          {{ uploadId ? "继续上传" : "开始上传" }}
        </el-button>
        <el-button :disabled="!running" @click="pauseUpload">暂停</el-button>
        <el-button type="danger" :disabled="!uploadId" @click="cancelUpload">取消</el-button>
      </el-form-item>
    </el-form>

    <div v-if="uploadId" class="upload-progress">
      <el-progress :percentage="percent" :status="progressStatus" :stroke-width="14"></el-progress>
      <div class="progress-text">
        <span>{{ statusText }}</span>
        <span>{{ uploadedCount }} / {{ totalChunks }} 个分片</span>
      </div>
    </div>

    <el-alert v-if="resultUrl" type="success" :closable="false" style="margin-top: 16px">
      <template #title>
        上传成功：<a :href="resultUrl" target="_blank" rel="noopener">{{ resultUrl }}</a>
        <el-button link type="primary" @click="copyUrl">复制</el-button>
      </template>
    </el-alert>
  </div>
</template>

<script lang="ts" setup>
import { computed, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import app from "@/constants/app";
import { getToken } from "@/utils/cache";
import baseService from "@/service/baseService";

const props = withDefaults(
  defineProps<{
    compact?: boolean;
    accept?: string;
  }>(),
  {
    compact: false,
    accept: "video/*,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.csv,.md,.zip,.rar,.7z"
  }
);

const emit = defineEmits(["success"]);

const ALLOWED_EXTENSIONS = [
  "mp4", "mov", "avi", "mkv", "flv", "wmv", "webm", "m4v", "mpeg", "mpg", "ts", "3gp",
  "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "md", "zip", "rar", "7z"
];

const CONCURRENCY = 3;
const chunkSizeOptions = [2, 5, 10, 20];
const chunkSizeMb = ref(5);

const fileInputRef = ref<HTMLInputElement>();
const selectedFile = ref<File | null>(null);
const uploadId = ref("");
const totalChunks = ref(0);
const uploaded = reactive(new Set<number>());
const inflight = reactive<Record<number, number>>({});
const running = ref(false);
const paused = ref(false);
const statusText = ref("请选择视频文件");
const progressStatus = ref("");
const resultUrl = ref("");

const uploadedCount = computed(() => uploaded.size);
const percent = computed(() => {
  if (!totalChunks.value) {
    return 0;
  }
  let completed = uploaded.size;
  Object.values(inflight).forEach((ratio) => {
    completed += Math.min(Math.max(ratio, 0), 1);
  });
  return Math.min(100, Math.floor((completed / totalChunks.value) * 100));
});

const formatSize = (bytes: number) => {
  if (bytes < 1024 * 1024) {
    return (bytes / 1024).toFixed(1) + " KB";
  }
  if (bytes < 1024 * 1024 * 1024) {
    return (bytes / 1024 / 1024).toFixed(2) + " MB";
  }
  return (bytes / 1024 / 1024 / 1024).toFixed(2) + " GB";
};

const resetState = () => {
  uploadId.value = "";
  totalChunks.value = 0;
  uploaded.clear();
  Object.keys(inflight).forEach((key) => delete inflight[Number(key)]);
  running.value = false;
  paused.value = false;
  progressStatus.value = "";
  resultUrl.value = "";
};

const handleFileChange = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) {
    return;
  }
  const extension = file.name.includes(".") ? file.name.split(".").pop()!.toLowerCase() : "";
  if (!ALLOWED_EXTENSIONS.includes(extension)) {
    ElMessage.error("不支持的文件类型，仅支持视频及常见文档/压缩包");
    return;
  }
  if (uploadId.value) {
    cancelUpload();
  }
  resetState();
  selectedFile.value = file;
  statusText.value = "已选择文件，点击开始上传";
};

const startUpload = async () => {
  if (!selectedFile.value) {
    return;
  }
  if (!uploadId.value) {
    statusText.value = "正在初始化上传任务...";
    try {
      const res = await baseService.post("/sys/oss/chunk/init", {
        fileName: selectedFile.value.name,
        fileSize: selectedFile.value.size,
        chunkSize: chunkSizeMb.value * 1024 * 1024,
        identifier: `${selectedFile.value.name}-${selectedFile.value.size}-${selectedFile.value.lastModified}`
      });
      if (res.code !== 0 || !res.data) {
        ElMessage.error(res.msg || "初始化上传任务失败");
        return;
      }
      uploadId.value = res.data.uploadId;
      totalChunks.value = res.data.totalChunks;
      (res.data.uploaded || []).forEach((index: number) => uploaded.add(index));
      if (uploaded.size > 0) {
        ElMessage.info(`检测到已上传 ${uploaded.size} 个分片，将从断点继续`);
      }
    } catch (e: any) {
      statusText.value = "初始化失败";
      ElMessage.error(e.message || "初始化上传任务失败");
      return;
    }
  }
  paused.value = false;
  running.value = true;
  progressStatus.value = "";
  statusText.value = "正在上传分片...";
  await uploadRemaining();
};

const uploadRemaining = async () => {
  const pending: number[] = [];
  for (let i = 0; i < totalChunks.value; i++) {
    if (!uploaded.has(i)) {
      pending.push(i);
    }
  }
  let cursor = 0;
  const worker = async () => {
    while (!paused.value && cursor < pending.length) {
      const index = pending[cursor++];
      await uploadChunk(index);
    }
  };

  try {
    await Promise.all(Array.from({ length: Math.min(CONCURRENCY, pending.length) }, worker));
  } catch (e: any) {
    running.value = false;
    progressStatus.value = "exception";
    statusText.value = "上传中断：" + (e.message || "请重试，已上传分片会保留");
    return;
  }

  if (paused.value) {
    running.value = false;
    statusText.value = "已暂停，点击「继续上传」从断点恢复";
    return;
  }
  if (uploaded.size < totalChunks.value) {
    running.value = false;
    statusText.value = "存在未上传分片，请点击继续上传";
    return;
  }
  await mergeChunks();
};

const uploadChunk = (index: number) => {
  return new Promise<void>((resolve, reject) => {
    const file = selectedFile.value as File;
    const chunkSize = chunkSizeMb.value * 1024 * 1024;
    const start = index * chunkSize;
    const end = Math.min(start + chunkSize, file.size);
    const blob = file.slice(start, end);

    const formData = new FormData();
    formData.append("file", blob, `${index}.part`);

    const xhr = new XMLHttpRequest();
    xhr.open("POST", `${app.api}/sys/oss/chunk/upload?uploadId=${uploadId.value}&index=${index}`);
    const token = getToken();
    if (token) {
      xhr.setRequestHeader("token", token);
    }
    xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable && event.total > 0) {
        inflight[index] = event.loaded / event.total;
      }
    };
    xhr.onload = () => {
      try {
        const res = JSON.parse(xhr.responseText || "{}");
        if (xhr.status === 200 && res.code === 0) {
          delete inflight[index];
          uploaded.add(index);
          resolve();
          return;
        }
        reject(new Error(res.msg || `分片 ${index + 1} 上传失败`));
      } catch (e: any) {
        reject(new Error(e.message || `分片 ${index + 1} 上传失败`));
      }
    };
    xhr.onerror = () => reject(new Error(`分片 ${index + 1} 网络错误`));
    xhr.send(formData);
  });
};

const mergeChunks = async () => {
  statusText.value = "全部分片上传完成，正在合并并上传到文件服务器...";
  try {
    const res = await baseService.post("/sys/oss/chunk/merge", { uploadId: uploadId.value });
    if (res.code !== 0 || !res.data) {
      running.value = false;
      progressStatus.value = "exception";
      statusText.value = "合并失败：" + (res.msg || "");
      return;
    }
    resultUrl.value = res.data.url;
    progressStatus.value = "success";
    statusText.value = "上传成功";
    emit("success", {
      url: res.data.url,
      fileName: selectedFile.value?.name || res.data.fileName || "",
      size: selectedFile.value?.size || 0,
      extension: (selectedFile.value?.name.split(".").pop() || "").toLowerCase()
    });
    ElMessage.success("文件上传成功");
  } catch (e: any) {
    progressStatus.value = "exception";
    statusText.value = "合并失败：" + (e.message || "");
  } finally {
    running.value = false;
  }
};

const pauseUpload = () => {
  paused.value = true;
  statusText.value = "正在暂停（当前分片完成后停止）...";
};

const cancelUpload = () => {
  if (uploadId.value) {
    baseService.post("/sys/oss/chunk/cancel", { uploadId: uploadId.value }).catch(() => undefined);
  }
  resetState();
  statusText.value = "已取消上传";
};

const copyUrl = async () => {
  try {
    await navigator.clipboard.writeText(resultUrl.value);
    ElMessage.success("已复制");
  } catch (e) {
    ElMessage.warning("复制失败，请手动复制");
  }
};

const reset = () => {
  resetState();
  selectedFile.value = null;
  statusText.value = "请选择文件";
  if (fileInputRef.value) {
    fileInputRef.value.value = "";
  }
};

defineExpose({
  reset
});
</script>

<style scoped>
.big-file-upload {
  padding: 10px 4px;
}
.upload-picker {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.file-meta {
  font-size: 13px;
  color: #606266;
}
.file-name {
  margin-right: 10px;
}
.file-size {
  color: #909399;
}
.upload-progress {
  margin-top: 16px;
}
.progress-text {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}
</style>
