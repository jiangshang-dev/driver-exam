<template>
  <div class="mod-demo__examrecord">
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px">
      <el-form-item label="用户ID">
        <el-input v-model="queryForm.userId" placeholder="请输入用户ID" clearable></el-input>
      </el-form-item>
      <el-form-item label="用户昵称">
        <el-input v-model="queryForm.userNickname" placeholder="请输入用户昵称" clearable></el-input>
      </el-form-item>
      <el-form-item label="科目">
        <el-select v-model="queryForm.subject" placeholder="全部" clearable style="width: 120px">
          <el-option label="科目一" :value="1"></el-option>
          <el-option label="科目四" :value="4"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="考试状态">
        <el-select v-model="queryForm.examStatus" placeholder="全部" clearable style="width: 130px">
          <el-option label="进行中" :value="0"></el-option>
          <el-option label="已完成" :value="1"></el-option>
          <el-option label="已放弃" :value="2"></el-option>
          <el-option label="超时" :value="3"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="是否通过">
        <el-select v-model="queryForm.isPass" placeholder="全部" clearable style="width: 120px">
          <el-option label="通过" :value="1"></el-option>
          <el-option label="未通过" :value="0"></el-option>
        </el-select>
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
      <el-table-column prop="userNickname" label="用户昵称" header-align="center" align="center" width="120"></el-table-column>
      <el-table-column prop="subject" label="科目" header-align="center" align="center" width="90">
        <template v-slot="scope">
          <el-tag v-if="scope.row.subject === 1">科目一</el-tag>
          <el-tag v-else-if="scope.row.subject === 4">科目四</el-tag>
          <span v-else>{{ scope.row.subject }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalQuestions" label="总题数" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="answeredQuestions" label="已答题数" header-align="center" align="center" width="90"></el-table-column>
      <el-table-column prop="correctCount" label="正确数" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="wrongCount" label="错误数" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="score" label="得分" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="passScore" label="及格分" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="isPass" label="是否通过" header-align="center" align="center" width="90">
        <template v-slot="scope">
          <el-tag v-if="scope.row.isPass === 1" type="success">通过</el-tag>
          <el-tag v-else type="danger">未通过</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="usedTime" label="用时(秒)" header-align="center" align="center" width="90"></el-table-column>
      <el-table-column prop="examStatus" label="考试状态" header-align="center" align="center" width="100">
        <template v-slot="scope">
          {{ examStatusText(scope.row.examStatus) }}
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" header-align="center" align="center" width="170"></el-table-column>
      <el-table-column prop="submitTime" label="交卷时间" header-align="center" align="center" width="170"></el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";

const view = reactive({
  getDataListURL: "/learning/examRecord/page",
  getDataListIsPage: true
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const queryForm = reactive({
  userId: "",
  userNickname: "",
  subject: "",
  examStatus: "",
  isPass: ""
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
  queryForm.userNickname = "";
  queryForm.subject = "";
  queryForm.examStatus = "";
  queryForm.isPass = "";
  handleQuery();
};

const examStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: "进行中",
    1: "已完成",
    2: "已放弃",
    3: "超时"
  };
  return map[status] || "未知";
};
</script>
