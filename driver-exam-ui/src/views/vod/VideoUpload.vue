<template>
  <div class="video-upload-container">
    <h2>视频上传</h2>
    <div class="upload-section">
      <el-form :model="videoForm" label-width="80px">
        <el-form-item label="视频标题">
          <el-input v-model="videoForm.title" placeholder="请输入视频标题"></el-input>
        </el-form-item>
        <el-form-item label="选择文件">
          <input type="file" @change="handleFileChange" accept="video/*" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="uploadVideo" :loading="uploading">开始上传</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="progress-section" v-if="uploadStarted">
      <h3>上传进度</h3>
      <el-progress :percentage="uploadProgress" :status="uploadStatus"></el-progress>
      <p>{{ uploadMessage }}</p>
    </div>
    
    <div class="video-list-section">
      <h2>视频列表</h2>
      <el-table :data="videoList" style="width: 100%">
        <el-table-column prop="title" label="标题"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间"></el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="playVideo(scope.row)">播放</el-button>
            <el-button size="small" type="danger" @click="deleteVideo(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import baseService from '@/service/baseService'

// 视频表单数据
const videoForm = reactive({
  title: ''
})

// 上传相关状态
const uploading = ref(false)
const uploadStarted = ref(false)
const uploadProgress = ref(0)
const uploadStatus = ref('')
const uploadMessage = ref('')

// 视频列表
const videoList = ref([])

// 选中的文件
const selectedFile = ref(null)

// 处理文件选择
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
    videoForm.title = file.name.split('.')[0] // 默认使用文件名作为标题
  }
}

// 上传视频
const uploadVideo = async () => {
  if (!selectedFile.value) {
    ElMessage.error('请选择要上传的视频文件')
    return
  }
  
  if (!videoForm.title) {
    ElMessage.error('请输入视频标题')
    return
  }
  
  try {
    uploading.value = true
    uploadStarted.value = true
    uploadProgress.value = 0
    uploadStatus.value = ''
    uploadMessage.value = '正在获取上传凭证...'
    
    // 1. 获取上传凭证 (使用JSON格式请求)
    const authResponse = await baseService.post('/vod/upload/auth/json', {
      title: videoForm.title,
      fileName: selectedFile.value.name,
      fileSize: selectedFile.value.size
    })
    
    if (authResponse.code !== 0) { // 注意：baseService的成功码是0
      throw new Error(authResponse.msg || '获取上传凭证失败')
    }
    
    const { videoId, uploadAddress, uploadAuth } = authResponse.data
    
    uploadProgress.value = 20
    uploadMessage.value = '上传凭证获取成功，正在上传视频...'
    
    // 2. 使用阿里云上传SDK上传视频
    // 这里简化处理，实际项目中需要引入阿里云上传SDK
    // 并使用uploadAddress和uploadAuth进行上传
    
    // 模拟上传过程
    const interval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
        uploadMessage.value = `正在上传视频... ${uploadProgress.value}%`
      } else {
        clearInterval(interval)
        uploadProgress.value = 100
        uploadStatus.value = 'success'
        uploadMessage.value = '视频上传成功！'
        uploading.value = false
        
        // 刷新视频列表
        loadVideoList()
      }
    }, 500)
    
  } catch (error) {
    console.error('上传失败:', error)
    uploadStatus.value = 'exception'
    uploadMessage.value = `上传失败: ${error.message || '未知错误'}`
    uploading.value = false
    ElMessage.error(`上传失败: ${error.message || '未知错误'}`)
  }
}

// 获取视频列表
const loadVideoList = async () => {
  try {
    const response = await baseService.get('/admin/vod/video/list', {
      page: 1,
      limit: 10
    })
    
    if (response.code === 0) { // 注意：baseService的成功码是0
      videoList.value = response.data.list || response.data || []
    } else {
      ElMessage.error(response.msg || '获取视频列表失败')
    }
  } catch (error) {
    console.error('获取视频列表失败:', error)
    ElMessage.error('获取视频列表失败')
  }
}

// 播放视频
const playVideo = (video) => {
  ElMessage.info(`播放视频: ${video.title}`)
  // 实际项目中会跳转到播放页面或弹出播放器
}

// 删除视频
const deleteVideo = (video) => {
  ElMessageBox.confirm(`确定要删除视频 "${video.title}" 吗？`, '确认删除', {
    type: 'warning'
  }).then(() => {
    // 使用正确的DELETE方法调用删除接口
    baseService.delete(`/admin/vod/video/${video.id}`, {}).then((res) => {
      if (res.code === 0) {
        ElMessage.success(`删除视频 "${video.title}" 成功`)
        loadVideoList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    }).catch((error) => {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    })
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 获取状态标签类型
const getTagType = (status) => {
  const statusMap = {
    'Processing': 'warning',
    'Uploading': 'info',
    'Normal': 'success',
    'Deleted': 'danger'
  }
  return statusMap[status] || 'info'
}

// 组件挂载时加载视频列表
onMounted(() => {
  loadVideoList()
})
</script>

<style scoped>
.video-upload-container {
  padding: 20px;
}

.upload-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.progress-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.video-list-section {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}
</style>