<template>
  <div class="mod-demo__apuserlikerecord">
    <!-- 添加查询条件表单 -->
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px">
      <el-form-item label="题目内容">
        <el-input v-model="queryForm.userId" placeholder="请输入题目内容" clearable></el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('apuserlikerecord:delete')" type="danger" @click="state.deleteHandle()">删除 </el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="用户手机号" header-align="center" align="center"></el-table-column>
      <el-table-column prop="targetType" label="点赞类型" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.targetType === "comment" ? "评论" : scope.row.targetType === "video" ? "视频" : "未知" }}
        </template>
      </el-table-column>
      <el-table-column prop="videoTitle" label="点赞视频标题" header-align="center" align="center"></el-table-column>
      <el-table-column prop="isLike" label="是否点赞" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.isLike ? "点赞" : !scope.row.isLike ? "取消点赞" : "未知" }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="updateTime" label="修改时间" header-align="center" align="center"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('apuserlikerecord:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>

  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/apuserlikerecord/page",
  getDataListIsPage: true,
  exportURL: "/apuserlikerecord/export",
  deleteURL: "/apuserlikerecord"
});

const state = reactive({ ...useView(view), ...toRefs(view) });


// 添加查询表单相关
const queryForm = reactive({
  userId: "",
});

const handleQuery = () => {
  // 确保dataForm已初始化并合并查询条件
  if (!state.dataForm) {
    state.dataForm = {};
  }
  Object.assign(state.dataForm as Record<string, any>, queryForm);
  state.page = 1; // 重置到第一页
  state.getDataList();
};

const resetQuery = () => {
  queryForm.userId = "";
  handleQuery();
  // 确保dataForm已初始化并清空查询条件
  if (!state.dataForm) {
    state.dataForm = {};
  }
  Object.assign(state.dataForm as Record<string, any>, queryForm);
  state.page = 1;
  state.getDataList();
};
</script>
