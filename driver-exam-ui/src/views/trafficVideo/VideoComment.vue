<template>
  <div class="mod-demo__apvideocomment">
    <!-- 查询条件 -->
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px">
      <el-form-item label="视频ID">
        <el-input v-model="queryForm.videoId" placeholder="请输入视频ID" clearable></el-input>
      </el-form-item>
      <el-form-item label="用户ID">
        <el-input v-model="queryForm.userId" placeholder="请输入用户ID" clearable></el-input>
      </el-form-item>
      <el-form-item label="评论内容">
        <el-input v-model="queryForm.commentContent" placeholder="请输入评论关键字" clearable></el-input>
      </el-form-item>
      <el-form-item label="审核状态">
        <el-select v-model="queryForm.auditStatus" placeholder="全部" clearable style="width: 130px">
          <el-option label="待审核" value="0"></el-option>
          <el-option label="审核通过" value="1"></el-option>
          <el-option label="审核不通过" value="2"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('apvideocomment:save')" type="primary" @click="addOrUpdateHandle()"> 新增 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('apvideocomment:update')" type="success" :disabled="selectedRows.length === 0" @click="openAuditDialog(1, selectedRows)"> 批量通过 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('apvideocomment:update')" type="warning" :disabled="selectedRows.length === 0" @click="openAuditDialog(2, selectedRows)"> 批量驳回 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('apvideocomment:delete')" type="danger" @click="state.deleteHandle()"> 删除 </el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center" width="60">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="videoId" label="视频ID" header-align="center" align="center" width="170"></el-table-column>
      <el-table-column prop="userId" label="用户ID" header-align="center" align="center" width="170"></el-table-column>
      <el-table-column prop="parentCommentId" label="父评论ID" header-align="center" align="center" width="170"></el-table-column>
      <el-table-column prop="commentContent" label="评论内容" header-align="center" align="center" min-width="140" show-overflow-tooltip></el-table-column>
      <el-table-column prop="likeCount" label="点赞数" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="replyCount" label="回复数" header-align="center" align="center" width="80"></el-table-column>
      <el-table-column prop="isHot" label="是否热门评论" header-align="center" align="center" width="110">
        <template v-slot="scope">
          <el-tag v-if="scope.row.isHot === 1" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditStatus" label="审核状态" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-tag v-if="scope.row.auditStatus === 0" type="info">待审核</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === 1" type="success">审核通过</el-tag>
          <el-tag v-else-if="scope.row.auditStatus === 2" type="danger">审核不通过</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditTime" label="审核时间" header-align="center" align="center" width="160"></el-table-column>
      <el-table-column prop="auditUserId" label="审核人ID" header-align="center" align="center" width="170"></el-table-column>
      <el-table-column prop="auditRemark" label="审核备注" header-align="center" align="center" min-width="120" show-overflow-tooltip></el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center" width="160"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="200">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('apvideocomment:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
          <el-button v-if="state.hasPermission('apvideocomment:update')" type="success" link @click="openAuditDialog(1, [scope.row])">通过 </el-button>
          <el-button v-if="state.hasPermission('apvideocomment:update')" type="warning" link @click="openAuditDialog(2, [scope.row])">驳回 </el-button>
          <el-button v-if="state.hasPermission('apvideocomment:delete')" type="danger" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>

    <!-- 新增 / 修改弹窗 -->
    <video-comment-add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList"></video-comment-add-or-update>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" :title="auditForm.auditStatus === 1 ? '审核通过' : '审核驳回'" width="500px" :close-on-click-modal="false">
      <el-form :model="auditForm" label-width="90px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" placeholder="请输入审核备注（驳回时必填）"></el-input>
        </el-form-item>
        <el-form-item label="评论数量">
          <span>{{ auditForm.ids.length }} 条</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditLoading" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { computed, reactive, ref, toRefs } from "vue";
import { ElMessage } from "element-plus";
import baseService from "@/service/baseService";
import VideoCommentAddOrUpdate from "./VideoCommentAddOrUpdate.vue";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/apvideocomment/page",
  getDataListIsPage: true,
  exportURL: "/apvideocomment/export",
  deleteURL: "/apvideocomment"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const queryForm = reactive({
  videoId: "",
  userId: "",
  commentContent: "",
  auditStatus: ""
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
  queryForm.videoId = "";
  queryForm.userId = "";
  queryForm.commentContent = "";
  queryForm.auditStatus = "";
  handleQuery();
};

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};

const selectedRows = computed(() => (state.dataListSelections || []) as any[]);

const auditVisible = ref(false);
const auditLoading = ref(false);
const auditForm = reactive({
  ids: [] as number[],
  auditStatus: 1,
  auditRemark: ""
});

const openAuditDialog = (auditStatus: number, rows: any[]) => {
  if (!rows || rows.length === 0) {
    ElMessage.warning("请选择需要审核的评论");
    return;
  }
  auditForm.ids = rows.map((row) => row.id);
  auditForm.auditStatus = auditStatus;
  auditForm.auditRemark = "";
  auditVisible.value = true;
};

const submitAudit = () => {
  if (auditForm.auditStatus === 2 && !auditForm.auditRemark.trim()) {
    ElMessage.warning("审核不通过时请填写审核备注");
    return;
  }
  auditLoading.value = true;
  baseService
    .post("/apvideocomment/audit", {
      ids: auditForm.ids,
      auditStatus: auditForm.auditStatus,
      auditRemark: auditForm.auditRemark
    })
    .then((res) => {
      if (res.code !== 0) {
        ElMessage.error(res.msg || "审核失败");
        return;
      }
      ElMessage.success("审核成功");
      auditVisible.value = false;
      state.getDataList();
    })
    .finally(() => {
      auditLoading.value = false;
    });
};
</script>
