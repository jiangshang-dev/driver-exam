<template>
  <div class="mod-demo__aptrafficsign">
    <!-- 添加查询条件表单 -->
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px;">
      <el-form-item label="标志名称">
        <el-input v-model="queryForm.signName" placeholder="请输入标志名称" clearable></el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('aptrafficsign:save')" type="primary" @click="addOrUpdateHandle()"> 新增 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('aptrafficsign:delete')" type="danger" @click="state.deleteHandle()"> 删除 </el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center" width="70">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="signName" label="标志名称" header-align="center" align="center" width="90"></el-table-column>
      <el-table-column prop="signDesc" label="标志说明" header-align="center" align="center" width="90"></el-table-column>
      <el-table-column prop="signImage" label="标志图片" header-align="center" align="center" width="90">
        <template v-slot="scope">
          <el-image v-if="scope.row.signImage" :src="scope.row.signImage" style="width: 50px; height: 50px"></el-image>
        </template>
      </el-table-column>
      <el-table-column prop="signCategory" label="标志类别" header-align="center" align="center" width="90">
        <template v-slot="scope">
          {{ getCategoryName(scope.row.signCategory) }}
        </template>
      </el-table-column>
      <el-table-column prop="signType" label="标志类型" header-align="center" align="center"></el-table-column>
      <el-table-column prop="usageScene" label="使用场景" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedRegulations" label="相关法规" header-align="center" align="center"></el-table-column>
      <el-table-column prop="sortOrder" label="排序权重" header-align="center" align="center"></el-table-column>
      <el-table-column prop="viewCount" label="查看次数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('aptrafficsign:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
          <el-button v-if="state.hasPermission('aptrafficsign:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <traffic-sign-add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList">确定 </traffic-sign-add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs, onMounted } from "vue";
import TrafficSignAddOrUpdate from "./TrafficSignAddOrUpdate.vue";
import baseService from "@/service/baseService";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/aptrafficsign/page",
  getDataListIsPage: true,
  exportURL: "/aptrafficsign/export",
  deleteURL: "/aptrafficsign"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};

const handleQuery = () => {
  // 确保dataForm已初始化并合并查询条件
  if (!state.dataForm) {
    state.dataForm = {};
  }
  Object.assign(state.dataForm as Record<string, any>, queryForm);
  state.page = 1; // 重置到第一页
  state.getDataList();
};

// 添加查询表单相关
const queryForm = reactive({
  signName: ''
});

const resetQuery = () => {
  queryForm.signName = '';
  handleQuery();
  // 确保dataForm已初始化并清空查询条件
  if (!state.dataForm) {
    state.dataForm = {};
  }
  Object.assign(state.dataForm as Record<string, any>, queryForm);
  state.page = 1;
  state.getDataList();
};

// 在组件挂载时获取分类数据
onMounted(() => {
  getCategory();
});

// 定义分类选项接口
interface CategoryOption {
  id: string;
  categoryName: string;
}

// 分类选项
const categoryOptions = ref<CategoryOption[]>([]);

// 根据分类ID获取分类名称
const getCategoryName = (categoryId: string) => {
  const category = categoryOptions.value.find((item: CategoryOption) => item.id === categoryId);
  return category ? category.categoryName : categoryId;
};

// 获取分类
const getCategory = () => {
  baseService.get("/sys/dict/type/getByDictName?dictCode=traffic_logo").then((res) => {
    if (res.data && Array.isArray(res.data)) {
      console.log('res---',res.data);
      categoryOptions.value = res.data.map((item: any) => ({
        id: item.id,
        categoryName: item.dictLabel
      }));
    }
  })
};
</script>
