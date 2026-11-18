<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false" :close-on-press-escape="false" width="560px">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="100px">
      <el-form-item label="视频ID" prop="videoId">
        <el-input v-model="dataForm.videoId" placeholder="视频ID"></el-input>
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="dataForm.userId" placeholder="用户ID"></el-input>
      </el-form-item>
      <el-form-item label="父评论ID" prop="parentCommentId">
        <el-input v-model="dataForm.parentCommentId" placeholder="父评论ID（回复时填写）"></el-input>
      </el-form-item>
      <el-form-item label="评论内容" prop="commentContent">
        <el-input v-model="dataForm.commentContent" type="textarea" :rows="4" placeholder="评论内容"></el-input>
      </el-form-item>
      <el-form-item label="是否热门" prop="isHot">
        <el-radio-group v-model="dataForm.isHot">
          <el-radio :label="1">是</el-radio>
          <el-radio :label="0">否</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="审核状态" prop="auditStatus">
        <el-radio-group v-model="dataForm.auditStatus">
          <el-radio :label="0">待审核</el-radio>
          <el-radio :label="1">审核通过</el-radio>
          <el-radio :label="2">审核不通过</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="审核备注" prop="auditRemark">
        <el-input v-model="dataForm.auditRemark" type="textarea" :rows="2" placeholder="审核备注"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";

const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  videoId: "",
  userId: "",
  parentCommentId: "",
  commentContent: "",
  isHot: 0,
  auditStatus: 0,
  auditRemark: ""
});

const rules = ref({
  videoId: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  userId: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  commentContent: [{ required: true, message: "必填项不能为空", trigger: "blur" }]
});

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (id) {
    getInfo(id);
  }
};

const getInfo = (id: number) => {
  baseService.get("/apvideocomment/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    const submitData: any = { ...dataForm };
    ["videoId", "userId", "parentCommentId"].forEach((key) => {
      if (submitData[key] === "" || submitData[key] === null) {
        delete submitData[key];
      }
    });
    (!dataForm.id ? baseService.post : baseService.put)("/apvideocomment", submitData).then((res) => {
      if (res.code !== 0) {
        ElMessage.error(res.msg || "保存失败");
        return;
      }
      ElMessage.success({
        message: "成功",
        duration: 500,
        onClose: () => {
          visible.value = false;
          emit("refreshDataList");
        }
      });
    });
  });
};

defineExpose({
  init
});
</script>
