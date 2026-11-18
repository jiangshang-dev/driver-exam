<template>
  <div class="mod-demo__studyrecord">
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px">
      <el-form-item label="用户ID">
        <el-input v-model="queryForm.userId" placeholder="请输入用户ID" clearable></el-input>
      </el-form-item>
      <el-form-item label="科目">
        <el-select v-model="queryForm.subject" placeholder="全部" clearable style="width: 120px">
          <el-option label="科目一" :value="1"></el-option>
          <el-option label="科目四" :value="4"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="学习类型">
        <el-input v-model="queryForm.studyType" placeholder="如：练习、考试" clearable></el-input>
      </el-form-item>
      <el-form-item label="学习日期">
        <el-date-picker v-model="queryForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width: 140px"></el-date-picker>
        <span class="date-sep">至</span>
        <el-date-picker v-model="queryForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width: 140px"></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border style="width: 100%">
      <el-table-column prop="id" label="序号" header-align="center" align="center" width="60">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" header-align="center" align="center" width="180"></el-table-column>
      <el-table-column prop="studyDate" label="学习日期" header-align="center" align="center" width="170"></el-table-column>
      <el-table-column prop="subject" label="科目" header-align="center" align="center" width="90">
        <template v-slot="scope">
          <el-tag v-if="scope.row.subject === 1">科目一</el-tag>
          <el-tag v-else-if="scope.row.subject === 4">科目四</el-tag>
          <span v-else>{{ scope.row.subject }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="studyType" label="学习类型" header-align="center" align="center" width="110"></el-table-column>
      <el-table-column prop="totalQuestions" label="总题数" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="correctCount" label="正确数" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="wrongCount" label="错误数" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="accuracyRate" label="正确率" header-align="center" align="center" width="90">
        <template v-slot="scope">
          {{ scope.row.accuracyRate }}%
        </template>
      </el-table-column>
      <el-table-column prop="studyTime" label="学习时长(分钟)" header-align="center" align="center" width="120"></el-table-column>
      <el-table-column prop="pointsEarned" label="获得积分" header-align="center" align="center" width="90"></el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center" width="170"></el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";

const view = reactive({
  getDataListURL: "/learning/studyRecord/page",
  getDataListIsPage: true
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const queryForm = reactive({
  userId: "",
  subject: "",
  studyType: "",
  startDate: "",
  endDate: ""
});

const handleQuery = () => {
  if (!state.dataForm) {
    state.dataForm = {};
  }
  Object.assign(state.dataForm as Record<string, any>, queryForm);
  state.page = 1;
  state.getDataList();
};

const resetQuery = () => {
  queryForm.userId = "";
  queryForm.subject = "";
  queryForm.studyType = "";
  queryForm.startDate = "";
  queryForm.endDate = "";
  handleQuery();
};
</script>

<style scoped>
.date-sep {
  margin: 0 6px;
  color: #909399;
}
</style>
