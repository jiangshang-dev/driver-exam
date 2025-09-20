<template>
  <div class="video-upload-container">
    <h2>视频上传</h2>
    <!-- 使用新创建的上传组件 -->
    <VodUpload />

    <!-- 保留原有的视频列表部分 -->
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

      <!-- 添加分页组件 -->
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />

    </div>

    <!-- 视频播放对话框 -->
    <el-dialog
      v-model="showVideoDialog"
      :title="currentVideo?.title"
      width="80%"
      :before-close="handleCloseVideoDialog"
      class="video-dialog"
    >
      <div class="video-player-container" v-if="currentVideo">
        <!-- 使用新的视频播放组件 -->
        <VideoPlayer
          :video-src="currentVideo.playUrl"
          :poster="currentVideo.coverUrl"
          :video-title="currentVideo.title"
          :view-count="currentVideo.viewCount || 0"
          :publish-date="currentVideo.createTime"
          :video-description="currentVideo.description || '暂无视频描述'"
        />
      </div>

      <div v-else class="loading-container">
        <el-loading text="正在加载视频信息..."></el-loading>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import baseService from '@/service/baseService'
// 引入新创建的上传组件
import VodUpload from '@/components/VodUpload.vue'
// 引入新的视频播放组件
import VideoPlayer from '@/components/VideoPlayer.vue'

// 视频列表
const videoList = ref([])

// 视频播放对话框相关
const showVideoDialog = ref(false)
const currentVideo = ref(null)
const videoPlayerRef = ref(null)

// 分页相关数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 获取视频列表
const loadVideoList = async () => {
  try {
    const response = await baseService.get('/admin/vod/video/list', {
      page: pagination.currentPage,
      limit: pagination.pageSize
    })

    if (response.code === 0) {
      videoList.value = response.data.list || response.data || []
      // 更新分页信息
      if (response.data) {
        console.log('response---', response.data);
        // 转为int类型 response.data.total
        let total = parseInt(response.data.total)
        pagination.total = total || 0
      }
    } else {
      ElMessage.error(response.msg || '获取视频列表失败')
    }
  } catch (error) {
    console.error('获取视频列表失败:', error)
    ElMessage.error('获取视频列表失败')
  }
}

// 播放视频
const playVideo = async (video) => {
  console.log('播放视频', video);

  try {
    // 获取播放信息
    const response = await baseService.get(`/vod/play/info?videoId=${video.videoId}`)

    if (response.code === 0) {
      const playInfo = response.data
      console.log('播放信息', playInfo);

      // 打开视频播放对话框
      showVideoDialog.value = true
      currentVideo.value = {
        ...video,
        playUrl: playInfo.playUrl,
        coverUrl: playInfo.coverUrl || playInfo.cover
      }
    } else {
      ElMessage.error(response.msg || '获取播放信息失败')
    }
  } catch (error) {
    console.error('获取播放信息失败:', error)
    ElMessage.error('获取播放信息失败')
  }
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

// 关闭视频播放对话框
const handleCloseVideoDialog = () => {
  // 暂停播放
  if (videoPlayerRef.value) {
    videoPlayerRef.value.pause()
    videoPlayerRef.value.currentTime = 0
  }
  showVideoDialog.value = false
  currentVideo.value = null
}

// 分页大小改变时的处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadVideoList()
}

// 当前页改变时的处理
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadVideoList()
}

// 视频加载开始
const onVideoLoadStart = () => {
  console.log('视频开始加载')
}

// 视频可以播放
const onVideoCanPlay = () => {
  console.log('视频可以播放')
  ElMessage.success('视频加载完成')
}

// 视频加载错误
const onVideoError = (event) => {
  console.error('视频播放错误:', event)
  ElMessage.error('视频播放失败，请检查视频文件')
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

.video-list-section {
  margin-top: 30px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.video-dialog :deep(.el-dialog__body) {
  padding: 10px 20px;
}

.video-player-container {
  text-align: center;
}

.video-info-panel {
  margin-top: 15px;
  text-align: left;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.video-info-panel h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 18px;
}

.video-meta {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.video-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.loading-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
