<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false" :close-on-press-escape="false" width="600px">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="视频标题" prop="videoTitle">
        <el-input v-model="dataForm.videoTitle" placeholder="视频标题"></el-input>
      </el-form-item>
      <el-form-item label="视频描述" prop="videoDesc">
        <el-input type="textarea" v-model="dataForm.videoDesc" placeholder="视频描述"></el-input>
      </el-form-item>
      <el-form-item label="封面图片" prop="coverImage">
        <CoverImageUpload v-model="dataForm.coverImage" />
      </el-form-item>

      <!-- 大文件分片上传 -->
      <el-form-item label="视频上传">
        <div class="video-upload-box">
          <BigFileUpload ref="bigUploadRef" compact accept="video/*" @success="handleBigUploadSuccess" />
          <div v-if="dataForm.videoUrl" class="uploaded-file">
            <span class="uploaded-label">已上传：</span>
            <a :href="dataForm.videoUrl" target="_blank" rel="noopener">{{ uploadedFileName || dataForm.videoUrl }}</a>
            <el-button link type="danger" @click="clearUploadedVideo">移除</el-button>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="视频分类" prop="category">
        <el-select v-model="dataForm.category" placeholder="请选择视频分类">
          <el-option v-for="category in categoryOptions" :key="category.id" :label="category.categoryName" :value="category.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="讲师名称" prop="instructor">
        <el-input v-model="dataForm.instructor" placeholder="讲师名称"></el-input>
      </el-form-item>
      <el-form-item label="难度等级" prop="difficultyLevel">
        <el-radio-group v-model="dataForm.difficultyLevel">
          <el-radio label="1">初级</el-radio>
          <el-radio label="2">中级</el-radio>
          <el-radio label="3">高级</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="是否VIP视频" prop="isVip">
        <el-radio-group v-model="dataForm.isVip">
          <el-radio label="0">免费</el-radio>
          <el-radio label="1">VIP</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="是否热门" prop="isHot">
        <el-radio-group v-model="dataForm.isHot">
          <el-radio label="1">是</el-radio>
          <el-radio label="0">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="是否可下载：0-否，1-是" prop="isDownloadable">
        <el-radio-group v-model="dataForm.isDownloadable">
          <el-radio label="1">是</el-radio>
          <el-radio label="0">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序权重" prop="sortOrder">
        <el-input v-model="dataForm.sortOrder" placeholder="排序权重"></el-input>
      </el-form-item>
      <el-form-item label="视频状态" prop="status">
        <el-radio-group v-model="dataForm.status">
          <el-radio label="1">正常</el-radio>
          <el-radio label="2">审核中</el-radio>
          <el-radio label="3">转码中</el-radio>
          <el-radio label="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";
// 大文件分片上传（MinIO/RustFS 等文件服务器）
import BigFileUpload from "@/components/BigFileUpload.vue";
import CoverImageUpload from "@/components/CoverImageUpload.vue";

const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();
const bigUploadRef = ref();
const uploadedFileName = ref("");

// 定义类型接口
interface CategoryOption {
  id: string;
  categoryName: string;
}

const dataForm = reactive({
  id: "",
  videoTitle: "",
  videoDesc: "",
  coverImage: "",
  videoUrl: "",
  aliyunVideoId: "",
  playAuth: "",
  videoDuration: "",
  videoSize: "",
  videoFormat: "",
  resolution: "",
  category: "",
  tags: "",
  instructor: "",
  difficultyLevel: "", // 保持字符串类型以匹配单选按钮的值
  isVip: "", // 保持字符串类型以匹配单选按钮的值
  isHot: "", // 保持字符串类型以匹配单选按钮的值
  isDownloadable: "", // 保持字符串类型以匹配单选按钮的值
  viewCount: "",
  likeCount: "",
  shareCount: "",
  commentCount: "",
  downloadCount: "",
  rating: "",
  ratingCount: "",
  sortOrder: "",
  status: "", // 保持字符串类型以匹配单选按钮的值
  uploadUserId: "",
  auditUserId: "",
  auditTime: "",
  publishTime: "",
  isDeleted: "",
  gmtCreate: "",
  gmtModified: ""
});

const rules = ref({
  videoTitle: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  videoDesc: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  coverImage: [{ required: true, message: "必填项不能为空", trigger: "change" }],
  category: [{ required: true, message: "必填项不能为空", trigger: "change" }],
  instructor: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  difficultyLevel: [{ required: true, message: "必填项不能为空", trigger: "change" }],
  isVip: [{ required: true, message: "必填项不能为空", trigger: "change" }],
  isHot: [{ required: true, message: "必填项不能为空", trigger: "change" }],
  isDownloadable: [{ required: true, message: "必填项不能为空", trigger: "change" }],
  sortOrder: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  status: [{ required: true, message: "必填项不能为空", trigger: "change" }]
});

// 分类选项
const categoryOptions = ref<CategoryOption[]>([]);

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  // 清除上传信息
  clearUploadedVideo();

  // 获取分类数据
  getCategory();

  if (id) {
    getInfo(id);
  }
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get("/aptrafficvideo/" + id).then((res) => {
    // 数据类型转换，确保数字类型字段转换为字符串以匹配单选按钮的值
    const data = { ...res.data };

    // 将数字类型转换为字符串类型以匹配单选按钮的值
    if (data.difficultyLevel !== undefined) {
      data.difficultyLevel = data.difficultyLevel.toString();
    }
    if (data.isVip !== undefined) {
      data.isVip = data.isVip.toString();
    }
    if (data.isHot !== undefined) {
      data.isHot = data.isHot.toString();
    }
    if (data.isDownloadable !== undefined) {
      data.isDownloadable = data.isDownloadable.toString();
    }
    if (data.status !== undefined) {
      data.status = data.status.toString();
    }

    Object.assign(dataForm, data);

    // 回显已上传的视频
    if (res.data.videoUrl) {
      uploadedFileName.value = getFileName(res.data.videoUrl);
    }
  });
};

// 获取分类
const getCategory = () => {
  baseService.get("/sys/dict/type/getByDictName?dictCode=traffic_video_category").then((res) => {
    if (res.data && Array.isArray(res.data)) {
      console.log('res---',res.data);
      categoryOptions.value = res.data.map(item => ({
        id: item.id,
        categoryName: item.dictLabel
      }));
    }
  })
};

const getFileName = (url: string) => {
  if (!url) {
    return "";
  }
  const name = url.split("?")[0].split("/").pop() || url;
  return decodeURIComponent(name);
};

// 大文件上传成功
const handleBigUploadSuccess = (payload: { url: string; fileName: string; size: number; extension: string }) => {
  dataForm.videoUrl = payload.url;
  (dataForm as any).videoSize = payload.size || "";
  dataForm.videoFormat = payload.extension || "";
  uploadedFileName.value = payload.fileName;
  if (!dataForm.videoTitle) {
    dataForm.videoTitle = payload.fileName.replace(/\.[^.]+$/, "");
  }
  ElMessage.success("视频上传成功");
};

// 清除已上传的视频信息
const clearUploadedVideo = () => {
  uploadedFileName.value = "";
  dataForm.videoUrl = "";
  dataForm.videoSize = "";
  dataForm.videoFormat = "";
  if (bigUploadRef.value) {
    bigUploadRef.value.reset();
  }
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }

    // 检查是否上传了视频
    if (!dataForm.videoUrl) {
      ElMessage.error("请先上传视频");
      return false;
    }

    // 创建提交数据的副本，并将字符串类型转换回数字类型以匹配后端要求
    const submitData: any = { ...dataForm };

    // 将字符串类型转换回数字类型以匹配后端要求
    if (submitData.difficultyLevel !== "") {
      submitData.difficultyLevel = parseInt(submitData.difficultyLevel);
    }
    if (submitData.isVip !== "") {
      submitData.isVip = parseInt(submitData.isVip);
    }
    if (submitData.isHot !== "") {
      submitData.isHot = parseInt(submitData.isHot);
    }
    if (submitData.isDownloadable !== "") {
      submitData.isDownloadable = parseInt(submitData.isDownloadable);
    }
    if (submitData.status !== "") {
      submitData.status = parseInt(submitData.status);
    }

    (!dataForm.id ? baseService.post : baseService.put)("/aptrafficvideo", submitData).then((res) => {
      ElMessage.success({
        message: "成功",
        duration: 500,
        onClose: () => {
          visible.value = false;
          emit("refreshDataList");
        }
      });
    });
  });
};

defineExpose({
  init
});
</script>

<style scoped>
.video-upload-box {
  width: 100%;
  padding: 12px 14px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  background: #fafbfc;
}

.uploaded-file {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e4e7ed;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.uploaded-file a {
  color: #409eff;
  text-decoration: none;
  word-break: break-all;
}

.uploaded-file a:hover {
  text-decoration: underline;
}

.uploaded-label {
  color: #909399;
}

.video-upload-box :deep(.big-file-upload) {
  padding: 0;
}
</style>










