<template>
  <div class="mod-demo__apuser">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('apuser:save')" type="primary" @click="addOrUpdateHandle()">新增 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('apuser:delete')" type="danger" @click="state.deleteHandle()">删除 </el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" header-align="center" align="center"></el-table-column>
      <el-table-column prop="password" label="密码" header-align="center" align="center"></el-table-column>
      <el-table-column prop="realName" label="真实姓名" header-align="center" align="center"></el-table-column>
      <el-table-column prop="avatar" label="头像" header-align="center" align="center"></el-table-column>
      <el-table-column prop="phone" label="手机号" header-align="center" align="center"></el-table-column>
      <el-table-column prop="email" label="邮箱" header-align="center" align="center"></el-table-column>
      <el-table-column prop="gender" label="性别" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.gender === 1 ? "男" : scope.row.gender === 2 ? "女" : "未知" }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.status === 1 ? "正常" : "禁用" }}
        </template>
      </el-table-column>
      <el-table-column prop="vipLevel" label="是否为VIP" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.vipLevel === 1 ? "是" : "否" }}
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录时间" header-align="center" align="center"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('apuser:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
          <el-button v-if="state.hasPermission('apuser:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList">确定</add-or-update>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import AddOrUpdate from "./apuser-add-or-update.vue";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/apuser/page",
  getDataListIsPage: true,
  exportURL: "/apuser/export",
  deleteURL: "/apuser"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
