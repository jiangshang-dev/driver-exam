<script setup lang="ts">
import { onMounted, ref } from 'vue'

const activeTab = ref(0)
const rewardedAd = ref(null)
const pendingSub = ref<number | null>(null)
const examClickCount = ref(0)

const urlCallback = ref({})

const getDailyClickCount = () => {
  const today = new Date().toDateString()
  const stored = uni.getStorageSync('examClickCount') || { date: '', count: 0 }
  if (stored.date !== today) {
    stored.date = today
    stored.count = 3
    uni.setStorageSync('examClickCount', stored)
  }
  return stored.count
}

const switchTab = (index: number) => {
	activeTab.value = index
}

const navigateTo = (type: string, sub: number) => {
	uni.navigateTo({
		url: `/pages/index/${type}/${type}?sub=${sub}`
	})
}

// 新增方法
const goToVideos = () => {
  uni.showToast({
    title: '交规视频功能',
    icon: 'none'
  })
}

const goToWrongQuestions = () => {
  uni.showToast({
    title: '错题本功能',
    icon: 'none'
  })
}

const goToFavorites = () => {
  uni.showToast({
    title: '我的收藏功能',
    icon: 'none'
  })
}

const goToPoints = () => {
  uni.showToast({
    title: '我的积分功能',
    icon: 'none'
  })
}

const goToProfile = () => {
  uni.showToast({
    title: '个人中心',
    icon: 'none'
  })
}

const playVideo = (id: number) => {
  uni.showToast({
    title: `播放视频${id}`,
    icon: 'none'
  })
}

const handleExamClick = (sub: number) => {
  const clickCount = getDailyClickCount()
  if (clickCount == 0) {
    navigateTo('exam', sub)
    return
  }

  uni.showModal({
    title: '解锁确认',
    content: '看视频要解锁模拟考试？还剩'+clickCount+'次免广告',
    success: (res) => {
      if (res.confirm) {
        const stored = uni.getStorageSync('examClickCount')
        stored.count -= 1
        uni.setStorageSync('examClickCount', stored)
        
        pendingSub.value = sub
        rewardedAd.value.show()
      }
    }
  })
}

const handleAdClose = (e) => {
  if (e.detail && e.detail.isEnded) {
    navigateTo('exam', pendingSub.value)
    pendingSub.value = null
	
	let getTime = new Date().getTime();
	urlCallback.value.userId = getTime
	urlCallback.value.extra = getTime
	console.log('urlCallback.value===', urlCallback.value)
  }
}

const onload = (e) => {
  console.log("onload")
}
const onclose = (ez) => {
  // console.log("onclose: " + ez.detail)
}
const onerror = (ex) => {
  // console.log("onerror: " + ex.detail.errCode + " message:: " + ex.detail.errMsg)
}

onMounted(()=>{

})

const onadload = (e) => {
    console.log('广告数据加载成功');
}
const onadclose = (e) => {
	console.log("onadclose");
}
const onaderror = (e) => {
    // 广告加载失败
    console.log("onaderror: ");
}

const isLoading = ref(false)
const showAd = () => {
	isLoading.value = true
}
</script>

<template>
	<view class="page">
		<!-- 顶部导航栏 -->
		<view class="header">
			<view class="header-content">
				<text class="logo">驾考通</text>
				<view class="header-right">
					<text class="search-icon">🔍</text>
					<text class="profile-icon" @click="goToProfile()">👤</text>
				</view>
			</view>
		</view>

		<!-- 主要内容区域 -->
		<view class="main-content">
			<!-- 轮播图区域 -->
			<view class="banner-section">
				<view class="banner">
					<view class="banner-item active">
						<image src="https://via.placeholder.com/350x150/4CAF50/white?text=VIP会员特惠" alt="VIP特惠"></image>
						<view class="banner-text">
							<text class="banner-title">VIP会员限时特惠</text>
							<text class="banner-desc">解锁全部视频教程</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 功能模块网格 -->
			<view class="function-grid">
				<view class="grid-item" @click="goToVideos()">
					<text class="grid-icon">▶️</text>
					<text class="grid-text">交规视频</text>
					<view class="vip-badge">VIP</view>
				</view>
				<view class="grid-item" @click="navigateTo('sequential', activeTab === 0 ? 1 : 4)">
					<text class="grid-icon">✏️</text>
					<text class="grid-text">驾考刷题</text>
				</view>
				<view class="grid-item" @click="handleExamClick(activeTab === 0 ? 1 : 4)">
					<text class="grid-icon">📋</text>
					<text class="grid-text">模拟考试</text>
				</view>
				<view class="grid-item" @click="goToWrongQuestions()">
					<text class="grid-icon">⚠️</text>
					<text class="grid-text">错题本</text>
				</view>
				<view class="grid-item" @click="goToFavorites()">
					<text class="grid-icon">❤️</text>
					<text class="grid-text">我的收藏</text>
				</view>
				<view class="grid-item" @click="goToPoints()">
					<text class="grid-icon">🪙</text>
					<text class="grid-text">我的积分</text>
				</view>
			</view>

			<!-- 推荐内容 -->
			<view class="recommended-section">
				<text class="section-title">热门推荐</text>
				<view class="video-list">
					<view class="video-item" @click="playVideo(1)">
						<image src="https://via.placeholder.com/120x80/2196F3/white?text=视频1" alt="交规视频"></image>
						<view class="video-info">
							<text class="video-title">交通标志详解</text>
							<text class="video-desc">学习时长: 15分钟</text>
							<view class="video-stats">
								<text><text class="icon">👍</text> 1.2k</text>
								<text><text class="icon">❤️</text> 856</text>
							</view>
						</view>
						<view class="vip-overlay">
							<text class="vip-lock">🔒</text>
							<text>VIP专享</text>
						</view>
					</view>
					<view class="video-item" @click="playVideo(2)">
						<image src="https://via.placeholder.com/120x80/FF9800/white?text=视频2" alt="交规视频"></image>
						<view class="video-info">
							<text class="video-title">科目一技巧总结</text>
							<text class="video-desc">学习时长: 20分钟</text>
							<view class="video-stats">
								<text><text class="icon">👍</text> 2.1k</text>
								<text><text class="icon">❤️</text> 1.3k</text>
							</view>
						</view>
						<view class="vip-overlay">
							<text class="vip-lock">🔒</text>
							<text>VIP专享</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 学习进度 -->
			<view class="progress-section">
				<text class="section-title">学习进度</text>
				<view class="progress-card">
					<view class="progress-item">
						<text class="progress-label">科目一</text>
						<view class="progress-bar">
							<view class="progress-fill" style="width: 75%"></view>
						</view>
						<text class="progress-percent">75%</text>
					</view>
					<view class="progress-item">
						<text class="progress-label">科目四</text>
						<view class="progress-bar">
							<view class="progress-fill" style="width: 45%"></view>
						</view>
						<text class="progress-percent">45%</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 底部导航栏 -->
		<view class="bottom-nav">
			<view class="nav-item active">
				<text class="nav-icon">🏠</text>
				<text class="nav-text">首页</text>
			</view>
			<view class="nav-item">
				<text class="nav-icon">👤</text>
				<text class="nav-text">我的</text>
			</view>
		</view>

		<ad-rewarded-video 
		  :url-callback="urlCallback"
		  class='ad-video'
		  ref="rewardedAd" 
		  adpid="1315895981" 
		  :loadnext="true" 
		  @close="handleAdClose"
		  v-slot:default="{loading, error}">
			<button :disabled="loading" :loading="loading" style="display: none;"></button>
			<view v-if="error">{{error}}</view>
		</ad-rewarded-video>

		<!-- 信息流广告移动到最下方 -->
		<view class="ad-view">
		      <ad adpid="1478175623" @load="onload" @close="onclose" @error="onerror"></ad>
		</view>
	</view>
</template>

<style lang="scss">
/* 通用样式 */
* {
	margin: 0;
	padding: 0;
	box-sizing: border-box;
}

.page {
	font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
	background-color: #f5f5f5;
	color: #333;
	line-height: 1.6;
	padding-bottom: 140rpx; /* 为底部导航栏留出空间 */
}

/* 顶部导航栏 */
.header {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
	padding: 20rpx 0;
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 1000;
	box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.1);
}

.header-content {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 0 40rpx;
	max-width: 960rpx;
	margin: 0 auto;
}

.logo {
	font-size: 48rpx;
	font-weight: bold;
}

.header-right {
	display: flex;
	gap: 30rpx;
	align-items: center;
}

.search-icon, .profile-icon {
	font-size: 40rpx;
	cursor: pointer;
	transition: opacity 0.3s ease;
}

.search-icon:hover, .profile-icon:hover {
	opacity: 0.8;
}

/* 主要内容区域 */
.main-content {
	margin-top: 120rpx;
	padding: 40rpx;
	max-width: 960rpx;
	margin-left: auto;
	margin-right: auto;
}

/* 轮播图区域 */
.banner-section {
	margin-bottom: 50rpx;
}

.banner {
	position: relative;
	border-radius: 30rpx;
	overflow: hidden;
	box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.1);
}

.banner-item {
	position: relative;
	height: 320rpx;
	background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
	display: flex;
	align-items: center;
	padding: 40rpx;
	color: white;
}

.banner-item image {
	width: 100%;
	height: 100%;
	object-fit: cover;
	position: absolute;
	top: 0;
	left: 0;
	z-index: 1;
}

.banner-text {
	position: relative;
	z-index: 2;
}

.banner-title {
	font-size: 44rpx;
	font-weight: bold;
	margin-bottom: 16rpx;
	display: block;
}

.banner-desc {
	font-size: 28rpx;
	opacity: 0.9;
	display: block;
}

/* 功能网格 */
.function-grid {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 30rpx;
	margin-bottom: 60rpx;
}

.grid-item {
	background: white;
	border-radius: 30rpx;
	padding: 40rpx 30rpx;
	text-align: center;
	box-shadow: 0 4rpx 30rpx rgba(0,0,0,0.08);
	cursor: pointer;
	transition: all 0.3s ease;
	position: relative;
	overflow: hidden;
}

.grid-item:hover {
	transform: translateY(-10rpx);
	box-shadow: 0 16rpx 50rpx rgba(0,0,0,0.15);
}

.grid-icon {
	font-size: 56rpx;
	color: #667eea;
	margin-bottom: 20rpx;
	display: block;
	transition: transform 0.3s ease;
}

.grid-item:hover .grid-icon {
	transform: scale(1.1);
}

.grid-text {
	font-size: 28rpx;
	font-weight: 500;
	color: #333;
	display: block;
}

.vip-badge {
	position: absolute;
	top: 10rpx;
	right: 10rpx;
	background: linear-gradient(45deg, #FFD700, #FFA500);
	color: white;
	font-size: 20rpx;
	padding: 4rpx 12rpx;
	border-radius: 16rpx;
	font-weight: bold;
}

/* 节标题 */
.section-title {
	font-size: 36rpx;
	font-weight: 600;
	margin-bottom: 30rpx;
	color: #333;
	display: block;
}

/* 推荐内容 */
.recommended-section {
	margin-bottom: 60rpx;
}

.video-list {
	display: flex;
	flex-direction: column;
	gap: 30rpx;
}

.video-item {
	display: flex;
	background: white;
	border-radius: 24rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.08);
	cursor: pointer;
	transition: all 0.3s ease;
	position: relative;
	overflow: hidden;
}

.video-item:hover {
	transform: translateY(-4rpx);
	box-shadow: 0 12rpx 40rpx rgba(0,0,0,0.12);
}

.video-item image {
	width: 240rpx;
	height: 160rpx;
	border-radius: 16rpx;
	object-fit: cover;
	margin-right: 30rpx;
}

.video-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.video-title {
	font-size: 32rpx;
	font-weight: 600;
	margin-bottom: 10rpx;
	color: #333;
	display: block;
}

.video-desc {
	font-size: 24rpx;
	color: #666;
	margin-bottom: 20rpx;
	display: block;
}

.video-stats {
	display: flex;
	gap: 30rpx;
	font-size: 24rpx;
	color: #999;
}

.video-stats .icon {
	margin-right: 8rpx;
}

.vip-overlay {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0,0,0,0.7);
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	color: white;
	font-size: 28rpx;
	gap: 10rpx;
	opacity: 0;
	transition: opacity 0.3s ease;
}

.video-item:hover .vip-overlay {
	opacity: 1;
}

.vip-lock {
	font-size: 48rpx;
	color: #FFD700;
}

/* 学习进度 */
.progress-section {
	margin-bottom: 60rpx;
}

.progress-card {
	background: white;
	border-radius: 30rpx;
	padding: 40rpx;
	box-shadow: 0 4rpx 30rpx rgba(0,0,0,0.08);
}

.progress-item {
	display: flex;
	align-items: center;
	margin-bottom: 30rpx;
}

.progress-item:last-child {
	margin-bottom: 0;
}

.progress-label {
	width: 120rpx;
	font-size: 28rpx;
	font-weight: 500;
	color: #333;
}

.progress-bar {
	flex: 1;
	height: 16rpx;
	background: #f0f0f0;
	border-radius: 8rpx;
	margin: 0 30rpx;
	overflow: hidden;
}

.progress-fill {
	height: 100%;
	background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
	border-radius: 8rpx;
	transition: width 0.5s ease;
}

.progress-percent {
	font-size: 28rpx;
	font-weight: 600;
	color: #667eea;
	min-width: 80rpx;
	text-align: right;
}

/* 底部导航栏 */
.bottom-nav {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: white;
	display: flex;
	border-top: 1px solid #e0e0e0;
	box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.1);
	z-index: 1000;
}

.nav-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 16rpx 0;
	cursor: pointer;
	transition: color 0.3s ease;
	color: #999;
}

.nav-item.active {
	color: #667eea;
}

.nav-icon {
	font-size: 40rpx;
	margin-bottom: 8rpx;
}

.nav-text {
	font-size: 24rpx;
}

// 保留原有的广告样式
.ad-view {
    background-color: #FFFFFF;
    margin-bottom: 10px;
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
}

.ad-video {
	width: 100%;
}
</style>
