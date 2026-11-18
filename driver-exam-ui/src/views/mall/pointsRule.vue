<template>
  <div class="mod-demo__mallpointsrule">
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px">
      <el-form-item label="场景名称">
        <el-input v-model="queryForm.sceneName" placeholder="请输入场景名称" clearable></el-input>
      </el-form-item>
      <el-form-item label="场景编码">
        <el-input v-model="queryForm.sceneCode" placeholder="请输入场景编码" clearable></el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.enabled" placeholder="全部" clearable style="width: 120px">
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
        <el-button v-if="state.hasPermission('mall:pointsrule:save')" type="primary" @click="addOrUpdateHandle()"> 新增 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('mall:pointsrule:delete')" type="danger" @click="state.deleteHandle()"> 删除 </el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center" width="60">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="sceneCode" label="场景编码" header-align="center" align="center"></el-table-column>
      <el-table-column prop="sceneName" label="场景名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="points" label="积分" header-align="center" align="center"></el-table-column>
      <el-table-column prop="calcType" label="计算方式" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.calcType === 'FEN' ? '按实付金额' : '固定积分' }}
        </template>
      </el-table-column>
      <el-table-column prop="dailyLimit" label="每日上限" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.dailyLimit === 0 ? '不限' : scope.row.dailyLimit + ' 次' }}
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.enabled === 1" type="success">启用</el-tag>
          <el-tag v-else type="info">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" header-align="center" align="center" show-overflow-tooltip></el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center" width="160"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('mall:pointsrule:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
          <el-button v-if="state.hasPermission('mall:pointsrule:delete')" type="danger" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>

    <points-rule-add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></points-rule-add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import PointsRuleAddOrUpdate from "./pointsRule-add-or-update.vue";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/mall/pointsRule/page",
  getDataListIsPage: true,
  deleteURL: "/mall/pointsRule"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const queryForm = reactive({
  sceneName: "",
  sceneCode: "",
  enabled: ""
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
  queryForm.sceneName = "";
  queryForm.sceneCode = "";
  queryForm.enabled = "";
  handleQuery();
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: string | number) => {
  addOrUpdateRef.value.init(id);
};
</script>
