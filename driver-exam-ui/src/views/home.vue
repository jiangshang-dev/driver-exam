<template>
  <el-card shadow="never" class="aui-card--fill">
    <div class="mod-home">
      <!-- 统计数据概览 -->
      <el-row :gutter="20" class="mb-20">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="statistic-card">
            <el-statistic :value="statistics.dailyActiveUsers" title="今日活跃用户">
              <template #suffix>
                <el-icon><User /></el-icon>
              </template>
            </el-statistic>
            <div class="statistic-footer">
              <span>较昨日</span>
              <span class="green">
                <el-icon><CaretTop /></el-icon>
                {{ statistics.dailyGrowth }}%
              </span>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="statistic-card">
            <el-statistic :value="statistics.totalExams" title="总考试次数">
              <template #suffix>
                <el-icon><Document /></el-icon>
              </template>
            </el-statistic>
            <div class="statistic-footer">
              <span>较昨日</span>
              <span class="green">
                <el-icon><CaretTop /></el-icon>
                {{ statistics.examGrowth }}%
              </span>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="statistic-card">
            <el-statistic :value="statistics.passRate" title="通过率">
              <template #suffix>%</template>
            </el-statistic>
            <div class="statistic-footer">
              <span>较上月</span>
              <span class="red">
                <el-icon><CaretBottom /></el-icon>
                {{ statistics.passRateGrowth }}%
              </span>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="statistic-card">
            <el-statistic :value="statistics.vipUsers" title="VIP用户数">
              <template #suffix>
                <el-icon><Star /></el-icon>
              </template>
            </el-statistic>
            <div class="statistic-footer">
              <span>较昨日</span>
              <span class="green">
                <el-icon><CaretTop /></el-icon>
                {{ statistics.vipGrowth }}%
              </span>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 学习进度 -->
      <el-card class="mb-20">
        <template #header>
          <div class="card-header">
            <span>学习进度统计</span>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="progress-item">
              <span class="progress-label">科目一</span>
              <el-progress :percentage="progress.subject1" :stroke-width="18" :text-inside="true" />
              <div class="progress-stats">
                <span>已完成: {{ progress.subject1Completed }}题</span>
                <span>正确率: {{ progress.subject1Accuracy }}%</span>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="progress-item">
              <span class="progress-label">科目四</span>
              <el-progress :percentage="progress.subject4" :stroke-width="18" :text-inside="true" />
              <div class="progress-stats">
                <span>已完成: {{ progress.subject4Completed }}题</span>
                <span>正确率: {{ progress.subject4Accuracy }}%</span>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 功能模块 -->
      <el-card class="mb-20">
        <template #header>
          <div class="card-header">
            <span>功能模块</span>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col :span="4" v-for="item in functionModules" :key="item.title">
            <div class="function-item" @click="handleFunctionClick(item)">
              <el-icon :size="24" :color="item.color"><component :is="item.icon" /></el-icon>
              <span class="function-title">{{ item.title }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 图表统计 -->
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>用户活跃度趋势</span>
              </div>
            </template>
            <div class="chart-container">
              <el-empty description="图表数据" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>学习模块分布</span>
              </div>
            </template>
            <div class="chart-container">
              <el-empty description="饼图数据" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </el-card>
</template>

<style scoped>
.mod-home {
  line-height: 1.5;
}

.mb-20 {
  margin-bottom: 20px;
}

.statistic-card {
  background-color: var(--el-bg-color-overlay);
  border-radius: 8px;
  padding: 20px;
  height: 100%;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.statistic-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--el-text-color-regular);
  margin-top: 16px;
}

.statistic-footer .green {
  color: var(--el-color-success);
}

.statistic-footer .red {
  color: var(--el-color-danger);
}

.progress-item {
  margin-bottom: 20px;
}

.progress-item:last-child {
  margin-bottom: 0;
}

.progress-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 10px;
}

.progress-stats {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.function-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 10px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
  text-align: center;
}

.function-item:hover {
  background-color: var(--el-bg-color-page);
  transform: translateY(-2px);
}

.function-title {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-regular);
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}
</style>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import {
  User,
  Document,
  Star,
  CaretTop,
  CaretBottom,
  VideoPlay,
  Edit,
  DocumentChecked,
  Warning,
  Collection,
  Coin
} from '@element-plus/icons-vue'

// 统计数据
const statistics = reactive({
  dailyActiveUsers: 12850,
  dailyGrowth: 12,
  totalExams: 89200,
  examGrowth: 8,
  passRate: 86.5,
  passRateGrowth: -2.3,
  vipUsers: 3420,
  vipGrowth: 5
})

// 学习进度
const progress = reactive({
  subject1: 75,
  subject1Completed: 850,
  subject1Accuracy: 82,
  subject4: 45,
  subject4Completed: 320,
  subject4Accuracy: 78
})

// 功能模块
const functionModules = ref([
  { title: '交规视频', icon: 'VideoPlay', color: '#409eff' },
  { title: '驾考刷题', icon: 'Edit', color: '#67c23a' },
  { title: '模拟考试', icon: 'DocumentChecked', color: '#e6a23c' },
  { title: '错题本', icon: 'Warning', color: '#f56c6c' },
  { title: '我的收藏', icon: 'Collection', color: '#909399' },
  { title: '我的积分', icon: 'Coin', color: '#ffb300' }
])

// 处理功能模块点击
const handleFunctionClick = (item: any) => {
  console.log('点击了功能模块:', item.title)
  // 这里可以添加路由跳转逻辑
}
</script>
