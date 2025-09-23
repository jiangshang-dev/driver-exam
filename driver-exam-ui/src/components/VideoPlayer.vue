<template>
  <div class="bilibili-video-player">
    <!-- 视频播放器容器 -->
    <div class="video-container" ref="videoContainerRef">
      <!-- 视频元素 -->
      <video
        ref="videoRef"
        class="video-element"
        :src="videoSrc"
        :poster="poster"
        @loadedmetadata="onLoadedMetadata"
        @timeupdate="onTimeUpdate"
        @play="onPlay"
        @pause="onPause"
        @ended="onEnded"
        @volumechange="onVolumeChange"
        @click="togglePlayPause"
        @dblclick="toggleFullscreen"
      ></video>

      <!-- 视频控制栏 -->
      <div class="video-controls" v-show="showControls" @mouseenter="clearHideControlsTimer" @mouseleave="setHideControlsTimer">
        <!-- 顶部控制栏 -->
        <div class="controls-top">
          <div class="video-title">{{ videoTitle }}</div>
        </div>

        <!-- 中间播放/暂停按钮 -->
        <div class="controls-center" v-show="!isPlaying" @click="togglePlayPause">
          <div class="play-btn">
            <!-- <i class="iconfont icon-play"></i> -->
            <img src="@/assets/icons/svg/player.svg" alt="播放" class="play-icon" />
          </div>
        </div>

        <!-- 底部控制栏 -->
        <div class="controls-bottom">
          <!-- 播放/暂停按钮 -->
          <div class="control-btn" @click="togglePlayPause">
            <!-- <i :class="['iconfont', isPlaying ? 'icon-pause' : 'icon-play']"></i> -->
            <img v-if="!isPlaying" src="@/assets/icons/svg/small_player.svg" alt="播放" class="control-icon" />
            <img v-else src="@/assets/icons/svg/play.svg" alt="暂停" class="control-icon" />
          </div>

          <!-- 时间进度 -->
          <div class="time-progress">
            <span class="current-time">{{ formatTime(currentTime) }}</span>
            <div class="progress-bar-container" @click="onProgressBarClick">
              <div class="progress-bar">
                <div class="progress-buffered" :style="{ width: bufferedPercent + '%' }"></div>
                <div class="progress-played" :style="{ width: progressPercent + '%' }"></div>
                <div
                  class="progress-thumb"
                  :style="{ left: progressPercent + '%' }"
                ></div>
              </div>
            </div>
            <span class="duration">{{ formatTime(duration) }}</span>
          </div>

          <!-- 音量控制 -->
          <div class="volume-control" @mouseenter="showVolumeSlider = true" @mouseleave="showVolumeSlider = false">
            <div class="control-btn" @click="toggleMute">
              <img src="@/assets/icons/svg/vox.svg" alt="暂停" class="control-icon" />
            </div>
            <div class="volume-slider-container" v-show="showVolumeSlider">
              <div class="volume-slider" @click="onVolumeSliderClick">
                <div class="volume-track">
                  <div class="volume-level" :style="{ height: volumePercent + '%' }"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 设置按钮 -->
          <div class="control-btn">
            <i class="iconfont icon-setting"></i>
          </div>

          <!-- 全屏按钮 -->
          <div class="control-btn" @click="toggleFullscreen">
            <img src="@/assets/icons/svg/full.svg" alt="暂停" class="control-icon" />
          </div>
        </div>
      </div>

      <!-- 加载动画 -->
      <div class="loading-overlay" v-show="isLoading">
        <div class="loading-spinner"></div>
      </div>
    </div>

    <!-- 视频信息和互动区域 -->
    <div class="video-interaction">
      <div class="video-info">
        <h2 class="video-title">{{ videoTitle }}</h2>
        <div class="video-meta">
          <span class="view-count">{{ formatNumber(viewCount) }} 次观看</span>
          <span class="publish-date">{{ publishDate }}</span>
        </div>
        <div class="video-description">{{ videoDescription }}</div>
      </div>

      <!-- 互动按钮 -->
      <div class="video-actions">
        <div class="action-item">
          <i class="iconfont icon-like"></i>
          <span>{{ formatNumber(likeCount) }}</span>
        </div>
        <div class="action-item">
          <i class="iconfont icon-dislike"></i>
        </div>
        <div class="action-item">
          <i class="iconfont icon-collection"></i>
          <span>收藏</span>
        </div>
        <div class="action-item">
          <i class="iconfont icon-share"></i>
          <span>分享</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed } from 'vue'

export default {
  name: 'VideoPlayer',
  props: {
    videoSrc: {
      type: String,
      required: true
    },
    poster: {
      type: String,
      default: ''
    },
    videoTitle: {
      type: String,
      default: '视频标题'
    },
    viewCount: {
      type: Number,
      default: 0
    },
    publishDate: {
      type: String,
      default: ''
    },
    videoDescription: {
      type: String,
      default: ''
    },
    likeCount: {
      type: Number,
      default: 0
    }
  },
  setup(props) {
    // 响应式数据
    const videoRef = ref(null)
    const videoContainerRef = ref(null)
    const isPlaying = ref(false)
    const currentTime = ref(0)
    const duration = ref(0)
    const volume = ref(1)
    const isMuted = ref(false)
    const isLoading = ref(false)
    const showControls = ref(true)
    const showVolumeSlider = ref(false)
    const bufferedPercent = ref(0)
    const progressPercent = ref(0)
    const volumePercent = ref(100)
    let hideControlsTimer = null

    // 计算属性
    const volumeIcon = computed(() => {
      if (volume.value === 0 || isMuted.value) return 'icon-mute'
      if (volume.value < 0.3) return 'icon-volume-low'
      if (volume.value < 0.7) return 'icon-volume-medium'
      return 'icon-volume-high'
    })

    // 格式化时间
    const formatTime = (seconds) => {
      const mins = Math.floor(seconds / 60)
      const secs = Math.floor(seconds % 60)
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    }

    // 格式化数字
    const formatNumber = (num) => {
      if (num >= 100000000) {
        return (num / 100000000).toFixed(1) + '亿'
      } else if (num >= 10000) {
        return (num / 10000).toFixed(1) + '万'
      }
      return num
    }

    // 播放/暂停切换
    const togglePlayPause = () => {
      if (videoRef.value) {
        if (isPlaying.value) {
          videoRef.value.pause()
        } else {
          videoRef.value.play()
        }
      }
    }

    // 静音切换
    const toggleMute = () => {
      if (videoRef.value) {
        videoRef.value.muted = !isMuted.value
        isMuted.value = videoRef.value.muted
      }
    }

    // 全屏切换
    const toggleFullscreen = () => {
      const container = videoContainerRef.value
      if (!container) return

      if (!document.fullscreenElement) {
        container.requestFullscreen().catch(err => {
          console.error(`全屏请求失败: ${err.message}`)
        })
      } else {
        document.exitFullscreen()
      }
    }

    // 进度条点击处理
    const onProgressBarClick = (event) => {
      if (!videoRef.value) return

      const progressBar = event.currentTarget
      const rect = progressBar.getBoundingClientRect()
      const pos = (event.clientX - rect.left) / rect.width
      videoRef.value.currentTime = pos * duration.value
    }

    // 音量条点击处理
    const onVolumeSliderClick = (event) => {
      if (!videoRef.value) return

      const volumeSlider = event.currentTarget
      const rect = volumeSlider.getBoundingClientRect()
      const pos = 1 - (event.clientY - rect.top) / rect.height
      videoRef.value.volume = Math.max(0, Math.min(1, pos))
    }

    // 视频事件处理
    const onLoadedMetadata = () => {
      if (videoRef.value) {
        duration.value = videoRef.value.duration
      }
    }

    const onTimeUpdate = () => {
      if (videoRef.value) {
        currentTime.value = videoRef.value.currentTime
        progressPercent.value = (currentTime.value / duration.value) * 100
      }
    }

    const onPlay = () => {
      isPlaying.value = true
      setHideControlsTimer()
    }

    const onPause = () => {
      isPlaying.value = false
      clearHideControlsTimer()
    }

    const onEnded = () => {
      isPlaying.value = false
      clearHideControlsTimer()
    }

    const onVolumeChange = () => {
      if (videoRef.value) {
        volume.value = videoRef.value.volume
        isMuted.value = videoRef.value.muted
        volumePercent.value = volume.value * 100
      }
    }

    // 控制栏显示/隐藏定时器
    const setHideControlsTimer = () => {
      clearHideControlsTimer()
      if (isPlaying.value) {
        hideControlsTimer = setTimeout(() => {
          showControls.value = false
        }, 3000)
      }
    }

    const clearHideControlsTimer = () => {
      if (hideControlsTimer) {
        clearTimeout(hideControlsTimer)
        hideControlsTimer = null
      }
      showControls.value = true
    }

    // 鼠标移动事件
    const onMouseMove = () => {
      clearHideControlsTimer()
      setHideControlsTimer()
    }

    // 挂载和卸载
    onMounted(() => {
      if (videoContainerRef.value) {
        videoContainerRef.value.addEventListener('mousemove', onMouseMove)
      }
    })

    onUnmounted(() => {
      if (videoContainerRef.value) {
        videoContainerRef.value.removeEventListener('mousemove', onMouseMove)
      }
      clearHideControlsTimer()
    })

    return {
      // refs
      videoRef,
      videoContainerRef,

      // reactive data
      isPlaying,
      currentTime,
      duration,
      volume,
      isMuted,
      isLoading,
      showControls,
      showVolumeSlider,
      bufferedPercent,
      progressPercent,
      volumePercent,

      // computed
      volumeIcon,

      // methods
      formatTime,
      formatNumber,
      togglePlayPause,
      toggleMute,
      toggleFullscreen,
      onProgressBarClick,
      onVolumeSliderClick,
      onLoadedMetadata,
      onTimeUpdate,
      onPlay,
      onPause,
      onEnded,
      onVolumeChange,
      setHideControlsTimer,
      clearHideControlsTimer
    }
  }
}
</script>

<style scoped>
.bilibili-video-player {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.video-container {
  position: relative;
  width: 100%;
  background: #000;
  aspect-ratio: 16/9;
}

.video-element {
  width: 100%;
  height: 100%;
  outline: none;
}

.video-controls {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, transparent 20%, transparent 80%, rgba(0, 0, 0, 0.3) 100%);
  color: white;
  opacity: 1;
  transition: opacity 0.3s ease;
  z-index: 5;
}

.video-controls:hover {
  opacity: 1 !important;
}

.controls-top {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  padding: 16px;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.6), transparent);
}

.video-title {
  font-size: 16px;
  font-weight: 500;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.controls-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10;
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.controls-center:hover {
  background: rgba(0, 0, 0, 0.8);
  transform: translate(-50%, -50%) scale(1.1);
  border-color: white;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.4);
}

.play-btn .iconfont {
  font-size: 36px;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.play-icon {
  width: 40px;
  height: 40px;
}

.control-icon {
  width: 20px;
  height: 20px;
  display: block;
  margin: 0 auto;
  filter: brightness(0) invert(1); /* 将图标设置为白色 */
}

.controls-bottom {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 12px 16px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  display: flex;
  align-items: center;
  gap: 12px;
}

.control-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s ease;
  color: white;
  font-size: 18px;
}

.control-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

.control-btn .iconfont {
  font-size: 20px;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.time-progress {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.progress-bar-container {
  flex: 1;
  height: 4px;
  cursor: pointer;
  border-radius: 2px;
}

.progress-bar {
  position: relative;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.progress-buffered {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 2px;
}

.progress-played {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: #fb7299;
  border-radius: 2px;
}

.progress-thumb {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 14px;
  height: 14px;
  background: white;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.progress-bar-container:hover .progress-thumb {
  opacity: 1;
}

.volume-control {
  position: relative;
  display: flex;
  align-items: center;
}

.volume-slider-container {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  width: 28px;
  height: 100px;
  background: rgba(0, 0, 0, 0.8);
  border-radius: 4px;
  padding: 10px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.volume-slider {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.volume-track {
  position: relative;
  width: 4px;
  height: 100%;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  margin: 0 auto;
}

.volume-level {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background: #fb7299;
  border-radius: 2px;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid #fb7299;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.video-interaction {
  display: flex;
  justify-content: space-between;
  padding: 20px;
  background: white;
}

.video-info {
  flex: 1;
  padding-right: 20px;
}

.video-info .video-title {
  color: #18191c;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.video-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #9499a0;
}

.video-description {
  font-size: 14px;
  color: #61666d;
  line-height: 1.6;
}

.video-actions {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  color: #61666d;
  font-size: 14px;
  transition: all 0.2s ease;
  width: 60px;
}

.action-item:hover {
  color: #fb7299;
  transform: translateY(-2px);
}

.action-item .iconfont {
  font-size: 24px;
  margin-bottom: 4px;
}

/* 图标字体样式 */
.iconfont {
  font-family: "iconfont" !important;
  font-size: 16px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.icon-play:before {
  content: "\e601";
}

.icon-pause:before {
  content: "\e602";
}

.icon-mute:before {
  content: "\e603";
}

.icon-volume-low:before {
  content: "\e604";
}

.icon-volume-medium:before {
  content: "\e605";
}

.icon-volume-high:before {
  content: "\e606";
}

.icon-fullscreen:before {
  content: "\e607";
}

.icon-setting:before {
  content: "\e609";
}

.icon-comment:before {
  content: "\e60a";
}

.icon-share:before {
  content: "\e60b";
}

.icon-like:before {
  content: "\e60c";
}

.icon-dislike:before {
  content: "\e60d";
}

.icon-collection:before {
  content: "\e60e";
}
</style>
