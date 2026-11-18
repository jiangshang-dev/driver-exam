<template>
  <div class="mod-demo__mallgoods">
    <!-- 查询条件 -->
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px">
      <el-form-item label="商品名称">
        <el-input v-model="queryForm.goodsName" placeholder="请输入商品名称" clearable></el-input>
      </el-form-item>
      <el-form-item label="商品类型">
        <el-select v-model="queryForm.goodsType" placeholder="全部" clearable style="width: 120px">
          <el-option label="虚拟商品" value="VIRTUAL"></el-option>
          <el-option label="实物商品" value="PHYSICAL"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="上架" :value="1"></el-option>
          <el-option label="下架" :value="0"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('mall:goods:save')" type="primary" @click="addOrUpdateHandle()"> 新增 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('mall:goods:delete')" type="danger" @click="state.deleteHandle()"> 删除 </el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center" width="60">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="goodsName" label="商品名称" header-align="center" align="center" min-width="140"></el-table-column>
      <el-table-column prop="goodsType" label="类型" header-align="center" align="center" width="90">
        <template v-slot="scope">
          <el-tag v-if="scope.row.goodsType === 'VIRTUAL'" type="success">虚拟</el-tag>
          <el-tag v-else-if="scope.row.goodsType === 'PHYSICAL'" type="warning">实物</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" header-align="center" align="center" width="90"></el-table-column>
      <el-table-column prop="coverImage" label="封面图片" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-image v-if="scope.row.coverImage" :src="scope.row.coverImage" fit="cover" style="width: 60px; height: 40px; border-radius: 4px"></el-image>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="summary" label="简介" header-align="center" align="center" min-width="160" show-overflow-tooltip></el-table-column>
      <el-table-column prop="pointsPrice" label="积分价" header-align="center" align="center" width="90"></el-table-column>
      <el-table-column prop="cashPrice" label="现金价(元)" header-align="center" align="center" width="100">
        <template v-slot="scope">
          {{ formatMoney(scope.row.cashPrice) }}
        </template>
      </el-table-column>
      <el-table-column prop="rewardType" label="奖励类型" header-align="center" align="center" width="110">
        <template v-slot="scope">
          {{ scope.row.rewardType === 'VIP_DAYS' ? 'VIP天数' : '无' }}
        </template>
      </el-table-column>
      <el-table-column prop="rewardValue" label="奖励值" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="stock" label="库存" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="status" label="状态" header-align="center" align="center" width="80">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 1" type="success">上架</el-tag>
          <el-tag v-else type="info">下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" header-align="center" align="center" width="70"></el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center" width="160"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('mall:goods:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
          <el-button v-if="state.hasPermission('mall:goods:delete')" type="danger" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>

    <goods-add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></goods-add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import GoodsAddOrUpdate from "./goods-add-or-update.vue";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/mall/goods/page",
  getDataListIsPage: true,
  deleteURL: "/mall/goods"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const queryForm = reactive({
  goodsName: "",
  goodsType: "",
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
  queryForm.goodsName = "";
  queryForm.goodsType = "";
  queryForm.status = "";
  handleQuery();
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};

const formatMoney = (fen?: number) => {
  if (fen === null || fen === undefined) {
    return "0.00";
  }
  return (fen / 100).toFixed(2);
};
</script>
