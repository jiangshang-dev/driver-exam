<template>
  <div class="mod-demo__mallpointslog">
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px">
      <el-form-item label="用户ID">
        <el-input v-model="queryForm.userId" placeholder="请输入用户ID" clearable></el-input>
      </el-form-item>
      <el-form-item label="场景编码">
        <el-input v-model="queryForm.sceneCode" placeholder="如：CHECKIN" clearable></el-input>
      </el-form-item>
      <el-form-item label="业务单号">
        <el-input v-model="queryForm.bizId" placeholder="请输入业务单号" clearable></el-input>
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
      <el-table-column prop="sceneCode" label="场景编码" header-align="center" align="center" width="140"></el-table-column>
      <el-table-column prop="points" label="变动积分" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <span :style="{ color: scope.row.points >= 0 ? '#67c23a' : '#f56c6c' }">
            {{ scope.row.points > 0 ? "+" + scope.row.points : scope.row.points }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="balance" label="变动后余额" header-align="center" align="center" width="110"></el-table-column>
      <el-table-column prop="bizId" label="业务单号" header-align="center" align="center" min-width="160" show-overflow-tooltip></el-table-column>
      <el-table-column prop="remark" label="备注" header-align="center" align="center" min-width="140" show-overflow-tooltip></el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center" width="170"></el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";

const view = reactive({
  getDataListURL: "/mall/pointsLog/page",
  getDataListIsPage: true
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const queryForm = reactive({
  userId: "",
  sceneCode: "",
  bizId: ""
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
  queryForm.sceneCode = "";
  queryForm.bizId = "";
  handleQuery();
};
</script>
