<template>
  <div class="cover-image-upload">
    <el-upload
      class="cover-uploader"
      :action="uploadUrl"
      :show-file-list="false"
      :before-upload="beforeUploadHandle"
      :on-success="successHandle"
      :on-error="errorHandle"
      accept="image/jpeg,image/png,image/gif"
    >
      <el-image v-if="modelValue" :src="modelValue" fit="cover" class="cover-image"></el-image>
      <div v-else class="cover-uploader-placeholder">
        <el-icon><Plus /></el-icon>
        <span>点击上传</span>
      </div>
    </el-upload>
    <div v-if="modelValue" class="cover-image-actions">
      <el-button type="danger" link @click="removeHandle">删除</el-button>
    </div>
    <div class="cover-uploader-tip">支持 jpg、png、gif，建议尺寸 16:9</div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import app from "@/constants/app";
import { getToken } from "@/utils/cache";

const props = defineProps<{
  modelValue?: string;
}>();

const emit = defineEmits(["update:modelValue"]);

const uploadUrl = computed(() => `${app.api}/sys/oss/upload?token=${getToken()}`);

const beforeUploadHandle = (file: any) => {
  const isImage = ["image/jpeg", "image/png", "image/gif"].includes(file.type);
  if (!isImage) {
    ElMessage.error("只支持 jpg、png、gif 格式图片");
  }
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    ElMessage.error("图片大小不能超过 5MB");
  }
  return isImage && isLt5M;
};

const successHandle = (res: any) => {
  if (res.code !== 0) {
    ElMessage.error(res.msg || "上传失败");
    return;
  }
  emit("update:modelValue", res.data.src);
  ElMessage.success("上传成功");
};

const errorHandle = () => {
  ElMessage.error("图片上传失败");
};

const removeHandle = () => {
  emit("update:modelValue", "");
};
</script>

<style scoped>
.cover-image-upload {
  width: 100%;
}
.cover-uploader :deep(.el-upload) {
  width: 160px;
  height: 90px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}
.cover-image {
  width: 160px;
  height: 90px;
}
.cover-uploader-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #8c939d;
  font-size: 12px;
  gap: 4px;
}
.cover-image-actions {
  margin-top: 4px;
}
.cover-uploader-tip {
  font-size: 12px;
  color: #909399;
  line-height: 20px;
}
</style>
