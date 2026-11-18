<template>
  <div class="mod-demo__aptrafficvideo">
    <!-- 添加查询条件表单 -->
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px;">
      <el-form-item label="视频标题">
        <el-input v-model="queryForm.videoTitle" placeholder="请输入视频标题" clearable></el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('aptrafficvideo:save')" type="primary" @click="addOrUpdateHandle()"> 新增 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('aptrafficvideo:delete')" type="danger" @click="state.deleteHandle()"> 删除 </el-button>
      </el-form-item>
      <el-form-item>
        <el-button v-if="state.hasPermission('aptrafficvideo:export')" type="primary" @click="state.exportHandle()"> 导出 </el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center" width="70">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="videoTitle" label="视频标题" header-align="center" align="center" width="90"></el-table-column>
      <el-table-column prop="coverImage" label="封面图片" header-align="center" align="center" width="90">
        <template v-slot="scope">
          <el-image :src="scope.row.coverImage" style="width: 50px; height: 50px"></el-image>
        </template>
      </el-table-column>
      <el-table-column label="视频播放" header-align="center" align="center" width="90">
        <template v-slot="scope">
          <el-button type="primary" size="small" @click="playVideo(scope.row)">播放</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="视频分类" header-align="center" align="center">
        <template v-slot="scope">
          {{ getCategoryName(scope.row.category) }}
        </template>
      </el-table-column>
      <el-table-column prop="instructor" label="讲师名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="difficultyLevel" label="难度等级" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.difficultyLevel === 1" type="info">初级</el-tag>
          <el-tag v-else-if="scope.row.difficultyLevel === 2" type="success">中级</el-tag>
          <el-tag v-else-if="scope.row.difficultyLevel === 3" type="warning">高级</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isVip" label="是否VIP视频" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.isVip === 0" type="info">免费</el-tag>
          <el-tag v-else type="success">VIP</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isHot" label="是否热门" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.isHot === 0" type="info">否</el-tag>
          <el-tag v-else type="success">是</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isDownloadable" label="是否可下载" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.isDownloadable === 0" type="info">否</el-tag>
          <el-tag v-else type="success">是</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="观看次数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="likeCount" label="点赞次数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="shareCount" label="分享次数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="commentCount" label="评论数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="downloadCount" label="下载次数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="rating" label="评分(0-5)" header-align="center" align="center"></el-table-column>
      <el-table-column prop="ratingCount" label="评分人数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="sortOrder" label="排序权重" header-align="center" align="center"></el-table-column>
      <el-table-column prop="status" label="视频状态" header-align="center" align="center">
        <template v-slot="scope">
          <el-tag v-if="scope.row.status === 0" type="info">下架</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="warning">审核中</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="warning">转码中</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="uploadUserId" label="上传用户" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditUserId" label="审核用户" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditTime" label="审核时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="publishTime" label="发布时间" header-align="center" align="center"></el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('aptrafficvideo:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
          <el-button v-if="state.hasPermission('aptrafficvideo:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <traffic-video-add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList">确定 </traffic-video-add-or-update>
    <!-- 视频播放对话框 -->
    <video-play-dialog ref="videoPlayRef"></video-play-dialog>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs, onMounted } from "vue";
import TrafficVideoAddOrUpdate from "./TrafficVideoAddOrUpdate.vue";
import VideoPlayDialog from "@/components/VideoPlayDialog.vue";
import baseService from "@/service/baseService";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/aptrafficvideo/page",
  getDataListIsPage: true,
  exportURL: "/aptrafficvideo/export",
  deleteURL: "/aptrafficvideo"
});

// 添加查询表单相关
const queryForm = reactive({
  videoTitle: ''
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
  queryForm.videoTitle = '';
  handleQuery();
  // 确保dataForm已初始化并清空查询条件
  if (!state.dataForm) {
    state.dataForm = {};
  }
  Object.assign(state.dataForm as Record<string, any>, queryForm);
  state.page = 1;
  state.getDataList();
};

const state = reactive({ ...useView(view), ...toRefs(view) });

// 分类选项
const categoryOptions = ref<CategoryOption[]>([]);

const addOrUpdateRef = ref();
const videoPlayRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};

// 根据分类ID获取分类名称
const getCategoryName = (categoryId: string) => {
  const category = categoryOptions.value.find((item: CategoryOption) => item.id === categoryId);
  return category ? category.categoryName : categoryId;
};

// 在组件挂载时获取分类数据
onMounted(() => {
  getCategory();
});

// 定义分类选项接口
interface CategoryOption {
  id: string;
  categoryName: string;
}

// 播放视频
const playVideo = (video: any) => {
  videoPlayRef.value.show(video);
};

// 获取分类
const getCategory = () => {
  baseService.get("/sys/dict/type/getByDictName?dictCode=traffic_video_category").then((res) => {
    if (res.data && Array.isArray(res.data)) {
      console.log('res---',res.data);
      categoryOptions.value = res.data.map((item: any) => ({
        id: item.id,
        categoryName: item.dictLabel
      }));
    }
  })
};
</script>
