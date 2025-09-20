<template>
  <div class="video-config-container">
    <el-tabs v-model="activeTab">
      <!-- 阿里云VOD配置 -->
      <el-tab-pane label="阿里云VOD配置" name="aliyun">
        <el-form :model="aliyunConfig" label-width="120px" :rules="aliyunRules" ref="aliyunFormRef">
          <el-form-item label="AccessKey ID" prop="accessKeyId">
            <el-input v-model="aliyunConfig.accessKeyId" placeholder="请输入阿里云AccessKey ID"></el-input>
          </el-form-item>
          <el-form-item label="AccessKey Secret" prop="accessKeySecret">
            <el-input v-model="aliyunConfig.accessKeySecret" placeholder="请输入阿里云AccessKey Secret"></el-input>
          </el-form-item>
          <el-form-item label="区域ID" prop="regionId">
            <el-input v-model="aliyunConfig.regionId" placeholder="请输入区域ID，如：cn-shanghai"></el-input>
          </el-form-item>
          <el-form-item label="接入点" prop="endpoint">
            <el-input v-model="aliyunConfig.endpoint" placeholder="请输入接入点，如：vod.cn-shanghai.aliyuncs.com"></el-input>
          </el-form-item>
          <el-form-item label="是否启用" prop="enable">
            <el-switch v-model="aliyunConfig.enable" active-text="启用" inactive-text="禁用"></el-switch>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveAliyunConfig">保存配置</el-button>
            <el-button @click="loadAliyunConfig">刷新</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 视频业务配置 -->
      <el-tab-pane label="视频业务配置" name="video">
        <el-form :model="videoConfig" label-width="120px" :rules="videoRules" ref="videoFormRef">
          <el-form-item label="最大并发上传数" prop="maxConcurrentUploads">
            <el-input-number v-model="videoConfig.maxConcurrentUploads" :min="1" :max="1000"></el-input-number>
          </el-form-item>
          <el-form-item label="默认转码模板" prop="defaultTranscodeTemplate">
            <el-input v-model="videoConfig.defaultTranscodeTemplate" placeholder="请输入默认转码模板"></el-input>
          </el-form-item>
          <el-form-item label="默认水印配置" prop="defaultWatermarkConfig">
            <el-input v-model="videoConfig.defaultWatermarkConfig" type="textarea" placeholder="请输入默认水印配置"></el-input>
          </el-form-item>
          <el-form-item label="存储位置" prop="storageLocation">
            <el-input v-model="videoConfig.storageLocation" placeholder="请输入存储位置"></el-input>
          </el-form-item>
          <el-form-item label="是否启用" prop="enable">
            <el-switch v-model="videoConfig.enable" active-text="启用" inactive-text="禁用"></el-switch>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveVideoConfig">保存配置</el-button>
            <el-button @click="loadVideoConfig">刷新</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <!-- 测试链接 -->
    <div class="test-section">
      <el-card>
        <div slot="header">
          <span>功能测试</span>
        </div>
        <div class="test-content">
          <el-button type="primary" @click="goToUploadTest">上传功能测试</el-button>
          <el-button type="success" @click="goToVideoUpload">视频上传页面</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import baseService from '@/service/baseService'
import { useRouter } from 'vue-router'

// 路由
const router = useRouter()

// 活动标签页
const activeTab = ref('aliyun')

// 阿里云VOD配置
const aliyunConfig = reactive({
  id: null,
  accessKeyId: '',
  accessKeySecret: '',
  regionId: 'cn-shanghai',
  endpoint: 'vod.cn-shanghai.aliyuncs.com',
  enable: true
})

// 视频业务配置
const videoConfig = reactive({
  id: null,
  maxConcurrentUploads: 100,
  defaultTranscodeTemplate: '',
  defaultWatermarkConfig: '',
  storageLocation: '',
  enable: true
})

// 表单验证规则
const aliyunRules = {
  accessKeyId: [{ required: true, message: '请输入AccessKey ID', trigger: 'blur' }],
  accessKeySecret: [{ required: true, message: '请输入AccessKey Secret', trigger: 'blur' }],
  regionId: [{ required: true, message: '请输入区域ID', trigger: 'blur' }],
  endpoint: [{ required: true, message: '请输入接入点', trigger: 'blur' }]
}

const videoRules = {
  maxConcurrentUploads: [{ required: true, message: '请输入最大并发上传数', trigger: 'blur' }]
}

// 表单引用
const aliyunFormRef = ref(null)
const videoFormRef = ref(null)

// 跳转到上传测试页面
const goToUploadTest = () => {
  router.push('/vod/upload-test')
}

// 跳转到视频上传页面
const goToVideoUpload = () => {
  router.push('/vod/video-upload')
}

// 加载阿里云VOD配置
const loadAliyunConfig =  () => {

    baseService.get("/admin/vod/config/aliyun").then((res) => {
      Object.assign(aliyunConfig, res.data);
    });

}

// 加载视频业务配置
const loadVideoConfig =  () => {
  baseService.get("/admin/vod/config/video").then((res) => {
    Object.assign(videoConfig, res.data);
  });

}

// 保存阿里云VOD配置
const saveAliyunConfig = () => {
  if (!aliyunFormRef.value) return

   aliyunFormRef.value.validate( (valid) => {
    if (valid) {
      baseService.post("/admin/vod/config/aliyun", aliyunConfig).then((res) => {
        Object.assign(aliyunConfig, res.data);
        ElMessage.success('保存成功')
        loadAliyunConfig()
      });
    }
  })
}

// 保存视频业务配置
const saveVideoConfig =  () => {
  if (!videoFormRef.value) return

   videoFormRef.value.validate(async (valid) => {
    if (valid) {
      baseService.post("/admin/vod/config/video", videoConfig).then((res) => {
        Object.assign(videoConfig, res.data);
      });
    }
  })
}

// 组件挂载时加载配置
onMounted(() => {
  loadAliyunConfig()
  loadVideoConfig()
})
</script>

<style scoped>
.video-config-container {
  padding: 20px;
}

.test-section {
  margin-top: 20px;
}

.test-content .el-button {
  margin-right: 10px;
}
</style>
