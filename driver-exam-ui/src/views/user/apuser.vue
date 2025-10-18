<template>
  <div class="mod-demo__apuser">
    <!-- 添加查询条件表单 -->
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px;">
      <el-form-item label="用户手机号">
        <el-input v-model="queryForm.phone" placeholder="请输入手机号" clearable></el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>


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
<!--      <el-table-column prop="password" label="密码" header-align="center" align="center"></el-table-column>-->
      <el-table-column prop="realName" label="真实姓名" header-align="center" align="center"></el-table-column>
<!--      <el-table-column prop="avatar" label="头像" header-align="center" align="center"></el-table-column>-->
      <el-table-column prop="integral" label="积分" header-align="center" align="center"></el-table-column>
      <el-table-column prop="phone" label="手机号" header-align="center" align="center"></el-table-column>
      <el-table-column prop="violationCommentCount" label="评论违规次数" header-align="center" align="center" width="120">
        <template v-slot="scope">
          {{ scope.row.violationCommentCount }} 次
        </template>
      </el-table-column>
      <el-table-column prop="commentPermission" label="评论权限" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-tag v-if="!scope.row.commentPermission" type="info">禁止</el-tag>
          <el-tag v-if="scope.row.commentPermission" type="success">允许</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="gender" label="性别" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.gender === 1 ? "男" : scope.row.gender === 2 ? "女" : "未知" }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.status ? "正常" : "禁用" }}
        </template>
      </el-table-column>
      <el-table-column prop="vipLevel" label="是否为VIP" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.vipLevel === 1 ? "是" : "否" }}
        </template>
      </el-table-column>
      <el-table-column prop="vipExpireTime" label="会员到期时间" header-align="center" align="center" width="120"></el-table-column>
      <el-table-column prop="lastIntegralUpdate" label="最后积分更新时间" header-align="center" align="center" width="120"></el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录时间" header-align="center" align="center" width="120"></el-table-column>
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
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";

// 添加查询表单相关
const queryForm = reactive({
  phone: '',
});

// 添加一个标志来跟踪是否是用户操作
const isUserAction = ref(false);

const resetQuery = () => {
  queryForm.phone = '';
  handleQuery();
  // 确保dataForm已初始化并清空查询条件
  if (!state.dataForm) {
    state.dataForm = {};
  }
  Object.assign(state.dataForm as Record<string, any>, queryForm);
  state.page = 1;
  state.getDataList();
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

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/apuser/page",
  getDataListIsPage: true,
  exportURL: "/apuser/export",
  deleteURL: "/apuser",
  commentSwitchURL: "/apuser/commentSwitch"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
