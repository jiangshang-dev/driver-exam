<template>
  <div class="mod-demo__apvideocomment">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('apvideocomment:save')" type="primary" @click="addOrUpdateHandle()"> 新增 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('apvideocomment:delete')" type="danger" @click="state.deleteHandle()"> 删除 </el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="videoId" label="视频ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="userId" label="用户ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="parentCommentId" label="父评论ID（回复功能）" header-align="center" align="center"></el-table-column>
      <el-table-column prop="commentContent" label="评论内容" header-align="center" align="center"></el-table-column>
      <el-table-column prop="likeCount" label="点赞数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="replyCount" label="回复数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="isHot" label="是否热门评论" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.isHot === 1" type="success">是</el-tag>
          <el-tag v-if="scope.row.isHot === 0" type="info">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="审核状态" header-align="center" align="center">
        <template v-slot="scope">
<!--          用tag标签-->
          <el-tag v-if="scope.row.auditStatus === 0" type="info">待审核</el-tag>
          <el-tag v-if="scope.row.auditStatus === 1" type="success">审核通过</el-tag>
          <el-tag v-if="scope.row.auditStatus === 2" type="danger">审核不通过</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditTime" label="审核时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditUserId" label="审核人ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditRemark" label="审核备注" header-align="center" align="center"></el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('apvideocomment:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
          <el-button v-if="state.hasPermission('apvideocomment:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
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
  getDataListURL: "/apvideocomment/page",
  getDataListIsPage: true,
  exportURL: "/apvideocomment/export",
  deleteURL: "/apvideocomment"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
