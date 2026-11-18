<template>
  <div class="simple-vod-upload">
    <el-form :model="form" label-width="90px">
      <el-form-item label="视频标题">
        <el-input v-model="form.title" placeholder="请输入视频标题"></el-input>
      </el-form-item>
      <el-form-item label="选择文件">
        <input ref="fileInputRef" type="file" accept="video/*" @change="handleFileChange" />
        <div v-if="selectedFile" class="file-info">
          <span>{{ selectedFile.name }}</span>
          <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="uploading" :disabled="!selectedFile || uploading" @click="startUpload">
          {{ uploading ? "上传中..." : "开始上传" }}
        </el-button>
        <el-button :disabled="!uploading" @click="cancelUpload">取消上传</el-button>
      </el-form-item>
    </el-form>

    <div v-if="uploadStarted" class="upload-progress">
      <el-progress :percentage="progress" :status="progressStatus"></el-progress>
      <div class="upload-message">{{ message }}</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import baseService from "@/service/baseService";
import AliyunVodUpload from "@/utils/aliyunVodUpload.js";

const emit = defineEmits(["upload-success", "upload-error"]);

const form = reactive({
  title: ""
});

const fileInputRef = ref<HTMLInputElement>();
const selectedFile = ref<File | null>(null);
const uploading = ref(false);
const uploadStarted = ref(false);
const progress = ref(0);
const progressStatus = ref("");
const message = ref("");
const uploader = ref<any>(null);

const handleFileChange = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (!file) {
    return;
  }
  if (file.size > 1024 * 1024 * 1024) {
    ElMessage.error("文件大小不能超过1GB");
    return;
  }
  selectedFile.value = file;
  if (!form.title) {
    form.title = file.name.replace(/\.[^/.]+$/, "");
  }
};

const startUpload = async () => {
  if (!selectedFile.value || !form.title) {
    ElMessage.error("请先选择视频文件并填写标题");
    return;
  }

  uploading.value = true;
  uploadStarted.value = true;
  progress.value = 0;
  progressStatus.value = "";
  message.value = "正在获取上传凭证...";

  try {
    const authResponse = await baseService.post("/vod/upload/auth/json", {
      title: form.title,
      fileName: selectedFile.value.name,
      fileSize: selectedFile.value.size
    });

    if (authResponse.code !== 0) {
      throw new Error(authResponse.msg || "获取上传凭证失败");
    }
    const authInfo = authResponse.data;
    if (!authInfo || !authInfo.uploadAuth || !authInfo.uploadAddress) {
      throw new Error("服务器返回的上传凭证不完整");
    }

    uploader.value = new (AliyunVodUpload as any)({
      partSize: selectedFile.value.size > 100 * 1024 * 1024 ? 2 * 1048576 : 1048576,
      parallel: 3
    });

    await uploader.value.initUploader(authInfo, selectedFile.value, {
      onUploadStarted: () => {
        message.value = "开始上传视频...";
      },
      onUploadProgress: (_info: any, totalSize: number, loadedSize: number) => {
        if (totalSize > 0) {
          progress.value = Math.min(Math.floor((loadedSize / totalSize) * 100), 99);
          message.value = `正在上传视频... ${progress.value}%`;
        }
      },
      onUploadSucceed: () => {
        progress.value = 100;
        progressStatus.value = "success";
        message.value = "视频上传成功";
        uploading.value = false;
        emit("upload-success", { videoId: authInfo.videoId, title: form.title });
      },
      onUploadFailed: (_info: any, _code: any, errorMessage: string) => {
        progressStatus.value = "exception";
        message.value = `上传失败: ${errorMessage}`;
        uploading.value = false;
        emit("upload-error", errorMessage);
      },
      onUploadEnd: () => {
        uploading.value = false;
      }
    });

    uploader.value.startUpload();
  } catch (error: any) {
    progressStatus.value = "exception";
    message.value = `上传失败: ${error.message}`;
    uploading.value = false;
    emit("upload-error", error.message);
  }
};

const cancelUpload = () => {
  if (uploader.value) {
    uploader.value.stopUpload();
  }
  uploading.value = false;
  progressStatus.value = "exception";
  message.value = "上传已取消";
};

const resetForm = () => {
  form.title = "";
  selectedFile.value = null;
  uploading.value = false;
  uploadStarted.value = false;
  progress.value = 0;
  progressStatus.value = "";
  message.value = "";
  uploader.value = null;
  if (fileInputRef.value) {
    fileInputRef.value.value = "";
  }
};

const formatFileSize = (bytes: number) => {
  if (bytes < 1024 * 1024) {
    return (bytes / 1024).toFixed(2) + " KB";
  }
  if (bytes < 1024 * 1024 * 1024) {
    return (bytes / (1024 * 1024)).toFixed(2) + " MB";
  }
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + " GB";
};

defineExpose({
  form,
  resetForm
});
</script>

<style scoped>
.simple-vod-upload {
  width: 100%;
}
.file-info {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}
.file-size {
  margin-left: 10px;
  color: #909399;
}
.upload-progress {
  padding: 0 10px 10px;
}
.upload-message {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}
</style>
