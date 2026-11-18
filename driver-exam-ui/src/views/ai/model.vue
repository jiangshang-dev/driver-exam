<template>
  <div class="mod-demo__aimodel">
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px">
      <el-form-item label="模型名称">
        <el-input v-model="queryForm.modelName" placeholder="请输入模型名称" clearable></el-input>
      </el-form-item>
      <el-form-item label="模型类型">
        <el-select v-model="queryForm.modelType" placeholder="全部" clearable style="width: 150px">
          <el-option label="普通大模型" value="LLM"></el-option>
          <el-option label="向量模型" value="EMBEDDING"></el-option>
          <el-option label="TTS语音模型" value="TTS"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.isActive" placeholder="全部" clearable style="width: 120px">
          <el-option label="已启用" :value="1"></el-option>
          <el-option label="未启用" :value="0"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('ai:model:save')" type="primary" @click="addOrUpdateHandle()"> 新增 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('ai:model:delete')" type="danger" @click="state.deleteHandle()"> 删除 </el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center" width="60">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="modelName" label="模型名称" header-align="center" align="center" min-width="160" show-overflow-tooltip></el-table-column>
      <el-table-column prop="modelType" label="模型类型" header-align="center" align="center" width="130">
        <template v-slot="scope">
          <el-tag :type="typeTag(scope.row.modelType)">{{ typeText(scope.row.modelType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="baseUrl" label="api地址" header-align="center" align="center" min-width="200" show-overflow-tooltip></el-table-column>
      <el-table-column prop="temperature" label="温度" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="maxTokens" label="最大回复" header-align="center" align="center" width="90"></el-table-column>
      <el-table-column prop="sortOrder" label="排序" header-align="center" align="center" width="70"></el-table-column>
      <el-table-column prop="remark" label="备注" header-align="center" align="center" min-width="120" show-overflow-tooltip></el-table-column>
      <el-table-column prop="isActive" label="状态" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-tag v-if="scope.row.isActive === 1" type="success">已启用</el-tag>
          <el-tag v-else type="info">未启用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center" width="160"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="230">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('ai:model:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
          <el-button v-if="state.hasPermission('ai:model:activate') && scope.row.isActive !== 1" type="success" link @click="activateHandle(scope.row)">启用 </el-button>
          <el-button v-if="state.hasPermission('ai:model:test')" type="warning" link @click="testHandle(scope.row)">测试 </el-button>
          <el-button v-if="state.hasPermission('ai:model:delete')" type="danger" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>

    <model-add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></model-add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import baseService from "@/service/baseService";
import ModelAddOrUpdate from "./model-add-or-update.vue";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/ai/model/page",
  getDataListIsPage: true,
  deleteURL: "/ai/model"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const queryForm = reactive({
  modelName: "",
  modelType: "",
  isActive: ""
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
  queryForm.modelName = "";
  queryForm.modelType = "";
  queryForm.isActive = "";
  handleQuery();
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: string | number) => {
  addOrUpdateRef.value.init(id);
};

const typeText = (type: string) => {
  const map: Record<string, string> = { LLM: "普通大模型", EMBEDDING: "向量模型", TTS: "TTS语音模型" };
  return map[type] || type;
};

const typeTag = (type: string) => {
  const map: Record<string, string> = { LLM: "primary", EMBEDDING: "success", TTS: "warning" };
  return map[type] || "info";
};

const activateHandle = (row: any) => {
  ElMessageBox.confirm(
    `启用后，同类型（${typeText(row.modelType)}）下的其它模型会自动停用，确定启用「${row.modelName}」？`,
    "启用模型",
    { confirmButtonText: "确定启用", cancelButtonText: "取消", type: "warning" }
  ).then(() => {
    baseService.put(`/ai/model/activate/${row.id}`, {}).then((res) => {
      if (res.code !== 0) {
        ElMessage.error(res.msg || "启用失败");
        return;
      }
      ElMessage.success("启用成功");
      state.getDataList();
    });
  });
};

const testHandle = (row: any) => {
  const loading = ElMessage({ message: "正在测试模型连通性...", type: "info", duration: 0 });
  baseService
    .post(`/ai/model/test/${row.id}`, {})
    .then((res) => {
      loading.close();
      if (res.code !== 0) {
        ElMessage.error(res.msg || "模型不可用");
        return;
      }
      ElMessage.success(res.data || "模型可用");
    })
    .catch(() => {
      loading.close();
      ElMessage.error("模型测试失败");
    });
};
</script>
