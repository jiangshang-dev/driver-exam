<script setup lang="ts">
import { ref, computed } from 'vue'
import { useMemberStore } from '@/stores';

// 获取屏幕边界到安全区域距离
const { safeAreaInsets } = uni.getSystemInfoSync()

// 获取会员信息
const memberStore = useMemberStore()

// 新增状态管理
const showVipModal = ref(false)
const showSettingsModal = ref(false)
const studyStats = ref({
  totalQuestions: 2580,
  correctRate: 85,
  studyDays: 15,
  examsPassed: 3
})

// 计算VIP状态
const isVipUser = computed(() => {
  return memberStore.profile?.vipLevel > 0
})

// 学习统计数据
const studyProgress = ref([
  { subject: '科目一', progress: 75, total: 1325 },
  { subject: '科目四', progress: 45, total: 1255 }
])

// 功能菜单项
const menuItems = ref([
  { icon: '📝', title: '错题本', count: 23, path: '/pages/wrong-questions' },
  { icon: '❤️', title: '我的收藏', count: 156, path: '/pages/favorites' },
  { icon: '🏆', title: '学习记录', count: 15, path: '/pages/study-record' },
  { icon: '🎯', title: '考试记录', count: 8, path: '/pages/exam-record' },
  { icon: '📊', title: '学习统计', count: 0, path: '/pages/statistics' },
  { icon: '🎁', title: '积分商城', count: 0, path: '/pages/points-mall' }
])

// 处理菜单点击
const handleMenuClick = (item) => {
  console.log('点击菜单:', item.title)
  // 这里可以添加路由跳转逻辑
}

// 显示VIP弹窗
const showVipDialog = () => {
  showVipModal.value = true
}

// 关闭VIP弹窗
const closeVipModal = () => {
  showVipModal.value = false
}

// 显示设置弹窗
const showSettings = () => {
  showSettingsModal.value = true
}

// 关闭设置弹窗
const closeSettingsModal = () => {
  showSettingsModal.value = false
}

// 处理登录
const handleLogin = () => {
  console.log('跳转到登录页面')
  // 这里添加登录逻辑
}

// 处理个人资料编辑
const handleProfileEdit = () => {
  console.log('编辑个人资料')
}

// 处理退出登录
const handleLogout = () => {
  console.log('退出登录')
  // 这里添加退出登录逻辑
}
</script>

<template>
  <view class="container">
    <!-- 顶部导航栏 -->
    <view class="header">
      <view class="header-content">
        <text class="logo">我的</text>
        <view class="header-right">
          <text class="settings-icon" @click="showSettings">设置</text>
        </view>
      </view>
    </view>

    <!-- 个人资料卡片 -->
    <view class="profile-card">
      <!-- 已登录状态 -->
      <view v-if="memberStore.profile" class="profile-content">
        <view class="user-info" @click="handleProfileEdit">
          <image 
            class="avatar" 
            mode="aspectFill"
            :src="memberStore.profile.avatar || 'https://via.placeholder.com/80x80/667eea/white?text=头像'"
          />
          <view class="user-details">
            <text class="nickname">{{ memberStore.profile.nickname || memberStore.profile.account || '驾考学员' }}</text>
            <view class="user-level">
              <text v-if="isVipUser" class="vip-badge">VIP会员</text>
              <text v-else class="normal-badge">普通用户</text>
              <text class="level-text">学习等级 Lv.{{ memberStore.profile.level || 1 }}</text>
            </view>
            <text class="phone">{{ memberStore.profile.mobile || '未绑定手机' }}</text>
          </view>
        </view>
        
        <!-- VIP升级按钮 -->
        <view v-if="!isVipUser" class="upgrade-vip" @click="showVipDialog">
          <text class="upgrade-text">升级VIP</text>
          <text class="upgrade-icon">👑</text>
        </view>
      </view>
      
      <!-- 未登录状态 -->
      <view v-else class="profile-content guest">
        <view class="user-info" @click="handleLogin">
          <image 
            class="avatar guest-avatar" 
            mode="aspectFill"
            src="https://via.placeholder.com/80x80/cccccc/white?text=登录"
          />
          <view class="user-details">
            <text class="nickname">点击登录</text>
            <text class="login-tip">登录后享受更多功能</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 学习统计卡片 -->
    <view class="stats-card">
      <view class="card-header">
        <text class="card-title">学习统计</text>
      </view>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-number">{{ studyStats.totalQuestions }}</text>
          <text class="stat-label">累计刷题</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ studyStats.correctRate }}%</text>
          <text class="stat-label">正确率</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ studyStats.studyDays }}</text>
          <text class="stat-label">学习天数</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ studyStats.examsPassed }}</text>
          <text class="stat-label">通过考试</text>
        </view>
      </view>
    </view>

    <!-- 学习进度卡片 -->
    <view class="progress-card">
      <view class="card-header">
        <text class="card-title">学习进度</text>
      </view>
      <view class="progress-list">
        <view 
          v-for="(item, index) in studyProgress" 
          :key="index"
          class="progress-item"
        >
          <view class="progress-info">
            <text class="subject-name">{{ item.subject }}</text>
            <text class="progress-text">{{ item.progress }}% ({{ Math.round(item.total * item.progress / 100) }}/{{ item.total }})</text>
          </view>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: item.progress + '%' }"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 功能菜单卡片 -->
    <view class="menu-card">
      <view class="card-header">
        <text class="card-title">我的工具</text>
      </view>
      <view class="menu-grid">
        <view 
          v-for="(item, index) in menuItems" 
          :key="index"
          class="menu-item"
          @click="handleMenuClick(item)"
        >
          <text class="menu-icon">{{ item.icon }}</text>
          <text class="menu-title">{{ item.title }}</text>
          <text v-if="item.count > 0" class="menu-count">{{ item.count }}</text>
        </view>
      </view>
    </view>

    <!-- VIP升级弹窗 -->
    <view v-if="showVipModal" class="modal" @click="closeVipModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">升级VIP会员</text>
          <text class="close" @click="closeVipModal">&times;</text>
        </view>
        <view class="modal-body">
          <view class="vip-benefits">
            <text class="benefits-title">VIP专享特权</text>
            <view class="benefit-item">
              <text class="benefit-icon">✓</text>
              <text class="benefit-text">解锁全部交规视频</text>
            </view>
            <view class="benefit-item">
              <text class="benefit-icon">✓</text>
              <text class="benefit-text">无限制刷题练习</text>
            </view>
            <view class="benefit-item">
              <text class="benefit-icon">✓</text>
              <text class="benefit-text">专业错题分析</text>
            </view>
            <view class="benefit-item">
              <text class="benefit-icon">✓</text>
              <text class="benefit-text">优先客服支持</text>
            </view>
          </view>
          <view class="vip-plans">
            <view class="plan-item">
              <text class="plan-title">月卡</text>
              <text class="plan-price">￥29.9</text>
            </view>
            <view class="plan-item recommended">
              <text class="plan-title">年卡</text>
              <text class="plan-price">￥199.9</text>
              <view class="discount-badge">的6折</view>
            </view>
          </view>
          <button class="vip-btn">立即开通</button>
        </view>
      </view>
    </view>

    <!-- 设置弹窗 -->
    <view v-if="showSettingsModal" class="modal" @click="closeSettingsModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">设置</text>
          <text class="close" @click="closeSettingsModal">&times;</text>
        </view>
        <view class="modal-body">
          <view class="setting-item">
            <text class="setting-label">清除缓存</text>
            <text class="setting-action">清除</text>
          </view>
          <view class="setting-item">
            <text class="setting-label">关于我们</text>
            <text class="setting-action">></text>
          </view>
          <view class="setting-item">
            <text class="setting-label">版本信息</text>
            <text class="setting-value">v1.0.0</text>
          </view>
          <view v-if="memberStore.profile" class="setting-item logout" @click="handleLogout">
            <text class="setting-label">退出登录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
/* 基础样式 */
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 70px;
}

/* 顶部导航栏 */
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 0 15px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.settings-icon {
  font-size: 14px;
  padding: 6px 12px;
  background: rgba(255,255,255,0.2);
  border-radius: 15px;
  cursor: pointer;
}

/* 个人资料卡片 */
.profile-card {
  margin: 80px 20px 20px;
  background: white;
  border-radius: 15px;
  box-shadow: 0 2px 15px rgba(0,0,0,0.08);
  overflow: hidden;
}

.profile-content {
  padding: 25px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.profile-content.guest {
  justify-content: flex-start;
}

.user-info {
  display: flex;
  align-items: center;
  flex: 1;
  cursor: pointer;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-right: 20px;
  border: 3px solid #f0f0f0;
}

.guest-avatar {
  opacity: 0.6;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nickname {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.user-level {
  display: flex;
  align-items: center;
  gap: 10px;
}

.vip-badge {
  background: linear-gradient(45deg, #FFD700, #FFA500);
  color: white;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 10px;
  font-weight: bold;
}

.normal-badge {
  background: #e9ecef;
  color: #666;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 10px;
}

.level-text {
  font-size: 12px;
  color: #666;
}

.phone {
  font-size: 14px;
  color: #999;
}

.login-tip {
  font-size: 14px;
  color: #999;
}

.upgrade-vip {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 12px 20px;
  border-radius: 25px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.upgrade-vip:hover {
  transform: translateY(-2px);
}

.upgrade-text {
  font-size: 14px;
  font-weight: 600;
}

.upgrade-icon {
  font-size: 16px;
}

/* 统计卡片 */
.stats-card {
  margin: 0 20px 20px;
  background: white;
  border-radius: 15px;
  box-shadow: 0 2px 15px rgba(0,0,0,0.08);
  overflow: hidden;
}

.card-header {
  padding: 20px 20px 10px;
  border-bottom: 1px solid #f0f0f0;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 20px;
  gap: 10px;
}

.stat-item {
  text-align: center;
  padding: 15px 10px;
  background: #f8f9fa;
  border-radius: 10px;
}

.stat-number {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

/* 进度卡片 */
.progress-card {
  margin: 0 20px 20px;
  background: white;
  border-radius: 15px;
  box-shadow: 0 2px 15px rgba(0,0,0,0.08);
  overflow: hidden;
}

.progress-list {
  padding: 20px;
}

.progress-item {
  margin-bottom: 20px;
}

.progress-item:last-child {
  margin-bottom: 0;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.subject-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.progress-text {
  font-size: 12px;
  color: #666;
}

.progress-bar {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: width 0.5s ease;
}

/* 菜单卡片 */
.menu-card {
  margin: 0 20px 20px;
  background: white;
  border-radius: 15px;
  box-shadow: 0 2px 15px rgba(0,0,0,0.08);
  overflow: hidden;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  padding: 20px;
  gap: 15px;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 15px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.menu-item:hover {
  background: #e9ecef;
  transform: translateY(-2px);
}

.menu-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.menu-title {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.menu-count {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #ff4444;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 15px;
  width: 100%;
  max-width: 400px;
  max-height: 80vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.modal-title {
  font-size: 18px;
  font-weight: bold;
}

.close {
  font-size: 24px;
  cursor: pointer;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
}

.modal-body {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

/* VIP弹窗样式 */
.vip-benefits {
  margin-bottom: 25px;
}

.benefits-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  display: block;
}

.benefit-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.benefit-icon {
  color: #4CAF50;
  font-size: 16px;
  font-weight: bold;
  margin-right: 10px;
  width: 20px;
}

.benefit-text {
  font-size: 14px;
  color: #666;
}

.vip-plans {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
}

.plan-item {
  flex: 1;
  background: #f8f9fa;
  border-radius: 10px;
  padding: 20px 15px;
  text-align: center;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.plan-item.recommended {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
}

.plan-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  display: block;
}

.plan-price {
  font-size: 20px;
  font-weight: bold;
  display: block;
}

.discount-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ff4444;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: bold;
}

.vip-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 15px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.vip-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 设置弹窗样式 */
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item.logout {
  color: #ff4444;
  justify-content: center;
  margin-top: 20px;
  border-top: 1px solid #f0f0f0;
  padding-top: 20px;
}

.setting-label {
  font-size: 16px;
  color: #333;
}

.setting-action {
  font-size: 14px;
  color: #667eea;
  cursor: pointer;
}

.setting-value {
  font-size: 14px;
  color: #666;
}
</style>
