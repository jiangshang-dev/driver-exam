<template>
  <div class="mod-demo__apadwatchrecord">
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
      <el-table-column prop="userId" label="用户ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="adId" label="广告ID" header-align="center" align="center"></el-table-column>
      <el-table-column prop="adPlatform" label="广告平台" header-align="center" align="center">
      </el-table-column>
      <el-table-column prop="adType" label="广告类型" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.adType === 'rewarded_video' ? "激励视频": "横幅" }}
        </template>
      </el-table-column>
      <el-table-column prop="watchDate" label="观看日期" header-align="center" align="center"></el-table-column>
      <el-table-column prop="rewardType" label="奖励类型：vip_days-会员天数，points-积分等" header-align="center" align="center">

      </el-table-column>
      <el-table-column prop="rewardAmount" label="奖励数量" header-align="center" align="center"></el-table-column>
      <el-table-column prop="rewardStatus" label="奖励发放状态：0-未发放，1-已发放，2-发放失败" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.rewardStatus === 0 ? "未发放" : scope.row.rewardStatus === 1 ? "已发放" : "发放失败" }}
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
import { reactive, ref, toRefs } from "vue";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/apadwatchrecord/page",
  getDataListIsPage: true,
  exportURL: "/apadwatchrecord/export",
  deleteURL: "/apadwatchrecord"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};
</script>
