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

      <!-- 简化版视频上传组件 -->
      <el-form-item label="视频上传">
        <div class="video-upload-container">
          <SimpleVodUpload ref="vodUploadRef" @upload-success="handleUploadSuccess" @upload-error="handleUploadError" />
          <!-- 显示已上传的视频信息 -->
          <div v-if="uploadedVideoInfo" class="uploaded-video-info">
            <el-alert
              title="视频上传成功"
              type="success"
              :description="`视频ID: ${uploadedVideoInfo.videoId}, 视频标题: ${uploadedVideoInfo.title}`"
              show-icon
              closable
              @close="clearUploadedVideo"
            />
          </div>
        </div>
      </el-form-item>

      <!-- 隐藏字段，用于存储视频ID -->
      <el-form-item label="阿里云视频ID" prop="aliyunVideoId" v-show="false">
        <el-input v-model="dataForm.aliyunVideoId" placeholder="阿里云视频ID"></el-input>
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
import { reactive, ref, watch } from "vue";
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";
// 引入简化版视频上传组件
import SimpleVodUpload from "@/components/SimpleVodUpload.vue";
import CoverImageUpload from "@/components/CoverImageUpload.vue";

const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();
const vodUploadRef = ref();

// 定义类型接口
interface VideoInfo {
  videoId: string;
  title: string;
}

interface CategoryOption {
  id: string;
  categoryName: string;
}

// 已上传的视频信息
const uploadedVideoInfo = ref<VideoInfo | null>(null);

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
  coverImage: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  // 视频URL和阿里云视频ID不再作为必填项，因为通过上传获取
  // aliyunVideoId: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  playAuth: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  videoDuration: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  videoSize: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  videoFormat: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  resolution: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  category: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  tags: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  instructor: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  difficultyLevel: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  isVip: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  isHot: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  isDownloadable: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  viewCount: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  likeCount: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  shareCount: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  commentCount: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  downloadCount: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  rating: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  ratingCount: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  sortOrder: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  status: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  uploadUserId: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  auditUserId: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  auditTime: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  publishTime: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  isDeleted: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  gmtCreate: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  gmtModified: [{ required: true, message: "必填项不能为空", trigger: "blur" }]
});

// 分类选项
const categoryOptions = ref<CategoryOption[]>([]);

// 监听视频标题变化，同步到上传组件
watch(() => dataForm.videoTitle, (newTitle) => {
  if (vodUploadRef.value && vodUploadRef.value.form) {
    vodUploadRef.value.form.title = newTitle;
  }
});

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

    // 如果已有视频ID，显示已上传信息
    if (res.data.aliyunVideoId) {
      uploadedVideoInfo.value = {
        videoId: res.data.aliyunVideoId,
        title: res.data.videoTitle
      };
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

// 处理上传成功事件
const handleUploadSuccess = (videoInfo: VideoInfo) => {
  uploadedVideoInfo.value = videoInfo;
  dataForm.aliyunVideoId = videoInfo.videoId;
  // 如果表单标题为空，使用上传的视频标题
  if (!dataForm.videoTitle) {
    dataForm.videoTitle = videoInfo.title;
  }
  ElMessage.success("视频上传成功");
};

// 处理上传失败事件
const handleUploadError = (error: string) => {
  ElMessage.error(`视频上传失败: ${error}`);
};

// 清除已上传的视频信息
const clearUploadedVideo = () => {
  uploadedVideoInfo.value = null;
  dataForm.aliyunVideoId = "";
  if (vodUploadRef.value) {
    vodUploadRef.value.resetForm();
  }
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }

    // 检查是否上传了视频
    if (!dataForm.aliyunVideoId) {
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
.video-upload-container {
  width: 100%;
}

.uploaded-video-info {
  margin-top: 15px;
}

.video-upload-container :deep(.simple-vod-upload) {
  padding: 0;
  margin: 0;
}
</style>










