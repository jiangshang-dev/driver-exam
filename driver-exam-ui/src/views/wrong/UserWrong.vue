<template>
  <div class="mod-demo__apuserwrongcollectionquestion">
    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('apuserwrongcollectionquestion:save')" type="primary" @click="addOrUpdateHandle()">新增 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('apuserwrongcollectionquestion:delete')" type="danger" @click="state.deleteHandle()">删除 </el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="userNickname" label="用户昵称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="questionId" label="题目内容" header-align="center" align="center"></el-table-column>
      <el-table-column prop="subject" label="科目" header-align="center" align="center"></el-table-column>
      <el-table-column prop="questionType" label="题型代码" header-align="center" align="center"></el-table-column>
      <el-table-column prop="questionTypeName" label="题型名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="questionContent" label="题目内容" header-align="center" align="center"></el-table-column>
      <el-table-column prop="questionImageUrl" label="题目配图URL" header-align="center" align="center"></el-table-column>
      <el-table-column prop="correctAnswer" label="正确答案" header-align="center" align="center"></el-table-column>
      <el-table-column prop="wrongCount" label="错误次数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="lastWrongAnswer" label="最后一次错误答案" header-align="center" align="center"></el-table-column>
      <el-table-column prop="isMastered" label="是否已掌握：0-未掌握，1-已掌握" header-align="center" align="center">

      </el-table-column>
      <el-table-column prop="masteredTime" label="掌握时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="firstWrongTime" label="首次错误时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="lastWrongTime" label="最后错误时间" header-align="center" align="center"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('apuserwrongcollectionquestion:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
          <el-button v-if="state.hasPermission('apuserwrongcollectionquestion:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
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
import AddOrUpdate from "./UserWrongAddOrUpdate.vue";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/demo/apuserwrongcollectionquestion/page",
  getDataListIsPage: true,
  exportURL: "/demo/apuserwrongcollectionquestion/export",
  deleteURL: "/demo/apuserwrongcollectionquestion"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
