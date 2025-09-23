<template>
  <div class="vod-upload">
    <h2>阿里云视频上传</h2>

    <!-- 上传表单 -->
    <div class="upload-form">
      <el-form :model="form" label-width="80px">
        <el-form-item label="视频标题">
          <el-input v-model="form.title" placeholder="请输入视频标题"></el-input>
        </el-form-item>
        <el-form-item label="选择文件">
          <input
            type="file"
            @change="handleFileChange"
            accept="video/*"
            ref="fileInput"
          />
          <div v-if="selectedFile" class="file-info">
            <p>文件名: {{ selectedFile.name }}</p>
            <p>文件大小: {{ formatFileSize(selectedFile.size) }}</p>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="startUpload"
            :loading="uploading"
            :disabled="!selectedFile || uploading"
          >
            {{ uploading ? '上传中...' : '开始上传' }}
          </el-button>
          <el-button @click="cancelUpload" :disabled="!uploading" style="margin-left: 10px;">
            取消上传
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 上传进度 -->
    <div v-if="uploadStarted" class="upload-progress">
      <h3>上传进度</h3>
      <el-progress :percentage="uploadProgress" :status="uploadStatus"></el-progress>
      <p>{{ uploadMessage }}</p>
      <p v-if="uploadSpeed">上传速度: {{ uploadSpeed }}</p>
      <p v-if="timeRemaining">预计剩余时间: {{ timeRemaining }}</p>
    </div>

    <!-- 上传结果 -->
    <div v-if="uploadResult" class="upload-result">
      <h3>上传结果</h3>
      <el-alert
        :type="uploadResult.success ? 'success' : 'error'"
        :title="uploadResult.message"
        show-icon
      >
      </el-alert>
      <div v-if="uploadResult.videoInfo" class="video-info">
        <p>视频ID: {{ uploadResult.videoInfo.videoId }}</p>
        <p>视频标题: {{ uploadResult.videoInfo.title }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import baseService from '@/service/baseService'
import AliyunVodUpload from '@/utils/aliyunVodUpload.js'

export default {
  name: 'VodUpload',
  setup() {
    // 表单数据
    const form = reactive({
      title: ''
    })

    // 上传状态
    const uploading = ref(false)
    const uploadStarted = ref(false)
    const uploadProgress = ref(0)
    const uploadStatus = ref('')
    const uploadMessage = ref('')
    const uploadResult = ref(null)
    const uploadSpeed = ref('') // 上传速度
    const timeRemaining = ref('') // 剩余时间

    // 文件相关
    const selectedFile = ref(null)
    const fileInput = ref(null)
    const vodUpload = ref(null)

    // 上传速度计算相关
    let lastLoadedSize = 0
    let lastTime = Date.now()
    let loadedHistory = [] // 存储最近的加载量，用于计算平均速度

    // 处理文件选择
    const handleFileChange = (event) => {
      const file = event.target.files[0]
      if (file) {
        // 检查文件大小
        if (file.size > 1024 * 1024 * 1024) { // 1GB
          ElMessage.error('文件大小不能超过1GB')
          return
        }

        selectedFile.value = file
        // 如果没有填写标题，默认使用文件名
        if (!form.title) {
          form.title = file.name.replace(/\.[^/.]+$/, "") // 去掉文件扩展名
        }
      }
    }

    // 开始上传
    const startUpload = async () => {
      if (!selectedFile.value) {
        ElMessage.error('请选择要上传的视频文件')
        return
      }

      if (!form.title) {
        ElMessage.error('请输入视频标题')
        return
      }

      try {
        uploading.value = true
        uploadStarted.value = true
        uploadProgress.value = 0
        uploadStatus.value = ''
        uploadMessage.value = '正在获取上传凭证...'
        uploadSpeed.value = ''
        timeRemaining.value = ''

        // 重置速度计算变量
        lastLoadedSize = 0
        lastTime = Date.now()
        loadedHistory = []

        // 1. 获取上传凭证 (使用JSON格式请求)
        const authResponse = await baseService.post('/vod/upload/auth/json', {
          title: form.title,
          fileName: selectedFile.value.name,
          fileSize: selectedFile.value.size
        })

        console.log('获取上传凭证响应:', authResponse);

        if (authResponse.code !== 0) {
          throw new Error(authResponse.msg || '获取上传凭证失败')
        }

        // 检查返回的凭证数据是否完整
        const authInfo = authResponse.data
        if (!authInfo || !authInfo.uploadAuth || !authInfo.uploadAddress) {
          throw new Error('服务器返回的上传凭证不完整')
        }

        uploadProgress.value = 10
        uploadMessage.value = '上传凭证获取成功，正在初始化上传...'

        // 2. 初始化上传实例
        vodUpload.value = new AliyunVodUpload({
          userId: 'heima',
          // 对于大文件，调整分片大小和并发数
          partSize: selectedFile.value.size > 100 * 1024 * 1024 ? 2 * 1048576 : 1048576, // 大于100MB的文件使用2MB分片
          parallel: selectedFile.value.size > 500 * 1024 * 1024 ? 2 : 3 // 大于500MB的文件进一步减少并发数
        })

        await vodUpload.value.initUploader(authInfo, selectedFile.value, {
          // 上传开始回调
          onUploadStarted: (uploadInfo) => {
            uploadMessage.value = '开始上传视频...'
          },
          // 上传进度回调
          onUploadProgress: (uploadInfo, totalSize, loadedSize) => {
            if (totalSize > 0) {
              // 记录加载历史用于计算平均速度
              const now = Date.now();
              loadedHistory.push({
                time: now,
                loaded: loadedSize
              });

              // 只保留最近10个记录
              if (loadedHistory.length > 10) {
                loadedHistory.shift();
              }

              // 计算平均上传速度
              if (loadedHistory.length > 1) {
                const first = loadedHistory[0];
                const last = loadedHistory[loadedHistory.length - 1];
                const timeDiff = (last.time - first.time) / 100000000000000; // 转换为秒
                const sizeDiff = last.loaded - first.loaded;

                if (timeDiff > 0) {
                  const avgSpeed = sizeDiff / timeDiff;
                  uploadSpeed.value = formatSpeed(avgSpeed);

                  // 计算剩余时间
                  const remainingSize = totalSize - loadedSize;
                  if (avgSpeed > 0) {
                    const remainingSeconds = remainingSize / avgSpeed;
                    timeRemaining.value = formatTime(remainingSeconds);
                  }
                }
              }

              // 更新进度条
              const percent = Math.floor((loadedSize / totalSize) * 90) + 10 // 10-100%
              uploadProgress.value = Math.min(percent, 99)
              uploadMessage.value = `正在上传视频... ${uploadProgress.value}%`

              console.log(`上传进度: ${uploadProgress.value}%, 已上传: ${formatFileSize(loadedSize)}, 总大小: ${formatFileSize(totalSize)}`);
            }
          },
          // 上传成功回调
          onUploadSucceed: (uploadInfo) => {
            uploadProgress.value = 100
            uploadStatus.value = 'success'
            uploadMessage.value = '视频上传成功！'
            uploadSpeed.value = ''
            timeRemaining.value = ''
            uploadResult.value = {
              success: true,
              message: '视频上传成功',
              videoInfo: {
                videoId: authInfo.videoId,
                title: form.title
              }
            }
            resetUpload()
          },
          // 上传失败回调
          onUploadFailed: (uploadInfo, code, message) => {
            uploadStatus.value = 'exception'
            uploadMessage.value = `上传失败: ${message}`
            uploadSpeed.value = ''
            timeRemaining.value = ''
            uploadResult.value = {
              success: false,
              message: `上传失败: ${message}`
            }
            resetUpload()
          },
          // 上传结束回调
          onUploadEnd: (uploadInfo) => {
            uploading.value = false
            uploadSpeed.value = ''
            timeRemaining.value = ''
          }
        })

        uploadProgress.value = 20
        uploadMessage.value = '上传初始化完成，正在开始上传...'

        // 3. 开始上传
        vodUpload.value.startUpload()
      } catch (error) {
        console.error('上传失败:', error)
        uploadStatus.value = 'exception'
        uploadMessage.value = `上传失败: ${error.message}`
        uploadSpeed.value = ''
        timeRemaining.value = ''
        uploadResult.value = {
          success: false,
          message: `上传失败: ${error.message}`
        }
        uploading.value = false
      }
    }

    // 格式化文件大小显示
    const formatFileSize = (bytes) => {
      if (bytes < 1024) {
        return bytes + ' B';
      } else if (bytes < 1024 * 1024) {
        return (bytes / 1024).toFixed(2) + ' KB';
      } else if (bytes < 1024 * 1024 * 1024) {
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
      } else {
        return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
      }
    }

    // 格式化上传速度显示
    const formatSpeed = (bytesPerSecond) => {
      if (bytesPerSecond < 1024) {
        return bytesPerSecond.toFixed(2) + ' B/s';
      } else if (bytesPerSecond < 1024 * 1024) {
        return (bytesPerSecond / 1024).toFixed(2) + ' KB/s';
      } else {
        return (bytesPerSecond / (1024 * 1024)).toFixed(2) + ' MB/s';
      }
    }

    // 格式化时间显示
    const formatTime = (seconds) => {
      if (seconds < 60) {
        return Math.floor(seconds) + '秒';
      } else if (seconds < 3600) {
        return Math.floor(seconds / 60) + '分钟';
      } else {
        return Math.floor(seconds / 3600) + '小时' + Math.floor((seconds % 3600) / 60) + '分钟';
      }
    }

    // 取消上传
    const cancelUpload = () => {
      if (vodUpload.value) {
        vodUpload.value.stopUpload();
      }
      uploadStatus.value = 'exception';
      uploadMessage.value = '上传已取消';
      uploadSpeed.value = '';
      timeRemaining.value = '';
      uploadResult.value = {
        success: false,
        message: '上传已取消'
      };
      resetUpload();
    }

    // 重置上传状态
    const resetUpload = () => {
      setTimeout(() => {
        uploading.value = false;
        // 不重置其他状态，让用户能看到结果
      }, 2000);
    }

    // 组件挂载时检查SDK是否可用
    onMounted(() => {
      if (!AliyunVodUpload.isSdkAvailable()) {
        ElMessage.warning('阿里云上传SDK未正确加载，可能影响上传功能');
      }
    });

    return {
      form,
      uploading,
      uploadStarted,
      uploadProgress,
      uploadStatus,
      uploadMessage,
      uploadResult,
      uploadSpeed,
      timeRemaining,
      selectedFile,
      fileInput,
      handleFileChange,
      startUpload,
      cancelUpload,
      formatFileSize
    }
  }
}
</script>

<style scoped>
.vod-upload {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.upload-form {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.file-info {
  margin-top: 10px;
  padding: 10px;
  background: #e9e9e9;
  border-radius: 4px;
}

.file-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.upload-progress {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.upload-progress p {
  margin: 10px 0;
}

.upload-result {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 4px;
}

.video-info {
  margin-top: 15px;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
}

.video-info p {
  margin: 5px 0;
}
</style>
