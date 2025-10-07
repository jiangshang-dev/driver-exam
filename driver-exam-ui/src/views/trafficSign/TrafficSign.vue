<template>
  <div class="mod-demo__aptrafficsign">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('demo:aptrafficsign:save')" type="primary" @click="addOrUpdateHandle()"> 新增 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('demo:aptrafficsign:delete')" type="danger" @click="state.deleteHandle()"> 删除 </el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="标志ID，雪花算法生成" header-align="center" align="center"></el-table-column>
      <el-table-column prop="signName" label="标志名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="signDesc" label="标志说明" header-align="center" align="center"></el-table-column>
      <el-table-column prop="signImage" label="标志图片URL" header-align="center" align="center"></el-table-column>
      <el-table-column prop="signCategory" label="标志类别：warning-警告标志，prohibition-禁令标志等" header-align="center" align="center"></el-table-column>
      <el-table-column prop="signType" label="标志类型" header-align="center" align="center"></el-table-column>
      <el-table-column prop="usageScene" label="使用场景" header-align="center" align="center"></el-table-column>
      <el-table-column prop="relatedRegulations" label="相关法规" header-align="center" align="center"></el-table-column>
      <el-table-column prop="sortOrder" label="排序权重" header-align="center" align="center"></el-table-column>
      <el-table-column prop="viewCount" label="查看次数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="isDeleted" label="是否删除：0-未删除，1-已删除" header-align="center" align="center"></el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="gmtModified" label="修改时间" header-align="center" align="center"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('demo:aptrafficsign:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
          <el-button v-if="state.hasPermission('demo:aptrafficsign:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
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
import { reactive, ref, toRefs } from "vue";
import TrafficSignAddOrUpdate from "./TrafficSignAddOrUpdate.vue";

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
</script>
