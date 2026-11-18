<template>
  <div class="mod-demo__apadwatchrecord">
    <!-- 添加查询条件表单 -->
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px;">
      <el-form-item label="用户手机号/昵称">
        <el-input v-model="queryForm.phone" placeholder="请输入手机号或昵称" clearable></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
<!--        <el-button v-if="state.hasPermission('apadwatchrecord:delete')" type="danger" @click="state.deleteHandle()">删除 </el-button>-->
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
<!--      <el-table-column prop="adId" label="广告ID" header-align="center" align="center"></el-table-column>-->
      <el-table-column prop="adPlatform" label="广告平台" header-align="center" align="center">
      </el-table-column>
      <el-table-column prop="adType" label="广告类型" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.adType === 'rewarded_video' ? "激励视频": "横幅" }}
        </template>
      </el-table-column>
      <el-table-column prop="watchDate" label="观看日期" header-align="center" align="center"></el-table-column>
      <el-table-column prop="rewardType" label="奖励类型" header-align="center" align="center">
        <template v-slot="scope">
<!--       用tag   -->
          <el-tag v-if="scope.row.rewardType === 'vip_days'">会员天数</el-tag>
          <el-tag v-if="scope.row.rewardType === 'points'">积分</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rewardAmount" label="奖励数量" header-align="center" align="center"></el-table-column>
      <el-table-column prop="rewardStatus" label="奖励发放状态" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.rewardStatus === 0">未发放</el-tag>
          <el-tag v-if="scope.row.rewardStatus === 1">已发放</el-tag>
          <el-tag v-if="scope.row.rewardStatus === 2">发放失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="platformType" label="平台类型" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.platformType === 'mp-weixin' ? "微信小程序" : scope.row.platformType === 'app-plus' ? "APP" : "H5" }}
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" header-align="center" align="center"></el-table-column>
<!--      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">-->
<!--        <template v-slot="scope">-->
<!--          <el-button v-if="state.hasPermission('apadwatchrecord:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>-->
<!--        </template>-->
<!--      </el-table-column>-->
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, toRefs } from "vue";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/apadwatchrecord/page",
  getDataListIsPage: true,
  exportURL: "/apadwatchrecord/export",
  deleteURL: "/apadwatchrecord"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

// 添加查询表单相关
const queryForm = reactive({
  phone: '',
});

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

</script>
