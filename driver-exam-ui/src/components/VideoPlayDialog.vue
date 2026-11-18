<template>
  <el-dialog v-model="visible" :title="currentVideo?.videoTitle || '视频播放'" width="900px" destroy-on-close @closed="handleClosed">
    <div v-loading="loading" class="video-play-dialog">
      <VideoPlayer
        v-if="playUrl"
        :video-src="playUrl"
        :poster="currentVideo?.coverImage || ''"
        :video-title="currentVideo?.videoTitle || ''"
        :view-count="viewCount"
        :like-count="likeCount"
        :publish-date="publishDate"
        :video-description="currentVideo?.videoDesc || ''"
      />
      <el-empty v-else-if="!loading" description="暂无播放地址，请检查视频是否已上传成功"></el-empty>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { computed, ref } from "vue";
import { ElMessage } from "element-plus";
import baseService from "@/service/baseService";
import VideoPlayer from "@/components/VideoPlayer.vue";

const visible = ref(false);
const loading = ref(false);
const currentVideo = ref<any>(null);
const playUrl = ref("");

const viewCount = computed(() => Number(currentVideo.value?.viewCount) || 0);
const likeCount = computed(() => Number(currentVideo.value?.likeCount) || 0);
const publishDate = computed(() => currentVideo.value?.publishTime || currentVideo.value?.gmtCreate || "");

const show = (video: any) => {
  currentVideo.value = video || null;
  playUrl.value = "";
  visible.value = true;
  if (!video) {
    return;
  }

  if (video.videoUrl) {
    playUrl.value = video.videoUrl;
    return;
  }

  if (!video.aliyunVideoId) {
    ElMessage.warning("该视频没有可用的播放地址");
    return;
  }

  loading.value = true;
  baseService
    .get("/vod/play/info", { videoId: video.aliyunVideoId })
    .then((res) => {
      if (res.data && res.data.playUrl) {
        playUrl.value = res.data.playUrl;
      } else {
        ElMessage.error("获取播放地址失败");
      }
    })
    .catch(() => {
      ElMessage.error("获取播放地址失败");
    })
    .finally(() => {
      loading.value = false;
    });
};

const handleClosed = () => {
  playUrl.value = "";
  currentVideo.value = null;
};

defineExpose({
  show
});
</script>

<style scoped>
.video-play-dialog {
  min-height: 300px;
}
</style>
