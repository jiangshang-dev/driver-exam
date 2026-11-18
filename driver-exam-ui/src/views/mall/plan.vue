<template>
  <div class="mod-demo__mallplan">
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px">
      <el-form-item label="套餐名称">
        <el-input v-model="queryForm.planName" placeholder="请输入套餐名称" clearable></el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="启用" :value="1"></el-option>
          <el-option label="禁用" :value="0"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('mall:plan:save')" type="primary" @click="addOrUpdateHandle()"> 新增 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('mall:plan:delete')" type="danger" @click="state.deleteHandle()"> 删除 </el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center" width="60">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="planName" label="套餐名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="days" label="会员天数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="price" label="售价(元)" header-align="center" align="center">
        <template v-slot="scope">
          {{ formatMoney(scope.row.price) }}
        </template>
      </el-table-column>
      <el-table-column prop="originPrice" label="原价(元)" header-align="center" align="center">
        <template v-slot="scope">
          {{ formatMoney(scope.row.originPrice) }}
        </template>
      </el-table-column>
      <el-table-column prop="pointsReward" label="赠送积分" header-align="center" align="center"></el-table-column>
      <el-table-column prop="recommended" label="推荐" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.recommended === 1" type="success">推荐</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 1" type="success">启用</el-tag>
          <el-tag v-else type="info">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" header-align="center" align="center"></el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center" width="160"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('mall:plan:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
          <el-button v-if="state.hasPermission('mall:plan:delete')" type="danger" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>

    <plan-add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></plan-add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import PlanAddOrUpdate from "./plan-add-or-update.vue";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/mall/plan/page",
  getDataListIsPage: true,
  deleteURL: "/mall/plan"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const queryForm = reactive({
  planName: "",
  status: ""
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
  queryForm.planName = "";
  queryForm.status = "";
  handleQuery();
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: string | number) => {
  addOrUpdateRef.value.init(id);
};

const formatMoney = (fen?: number) => {
  if (fen === null || fen === undefined) {
    return "0.00";
  }
  return (fen / 100).toFixed(2);
};
</script>
