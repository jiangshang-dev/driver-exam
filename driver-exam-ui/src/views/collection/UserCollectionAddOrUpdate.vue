<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
          <el-form-item label="用户ID" prop="userId">
        <el-input v-model="dataForm.userId" placeholder="用户ID"></el-input>
      </el-form-item>
          <el-form-item label="用户昵称（冗余）" prop="userNickname">
        <el-input v-model="dataForm.userNickname" placeholder="用户昵称（冗余）"></el-input>
      </el-form-item>
          <el-form-item label="题目ID" prop="questionId">
        <el-input v-model="dataForm.questionId" placeholder="题目ID"></el-input>
      </el-form-item>
          <el-form-item label="科目（冗余）" prop="subject">
        <el-input v-model="dataForm.subject" placeholder="科目（冗余）"></el-input>
      </el-form-item>
          <el-form-item label="题型代码（冗余）" prop="questionType">
        <el-input v-model="dataForm.questionType" placeholder="题型代码（冗余）"></el-input>
      </el-form-item>
          <el-form-item label="题型名称（冗余）" prop="questionTypeName">
        <el-input v-model="dataForm.questionTypeName" placeholder="题型名称（冗余）"></el-input>
      </el-form-item>
          <el-form-item label="题目内容（冗余）" prop="questionContent">
        <el-input v-model="dataForm.questionContent" placeholder="题目内容（冗余）"></el-input>
      </el-form-item>
          <el-form-item label="题目配图URL（冗余）" prop="questionImageUrl">
        <el-input v-model="dataForm.questionImageUrl" placeholder="题目配图URL（冗余）"></el-input>
      </el-form-item>
          <el-form-item label="正确答案（冗余）" prop="correctAnswer">
        <el-input v-model="dataForm.correctAnswer" placeholder="正确答案（冗余）"></el-input>
      </el-form-item>
          <el-form-item label="错误次数" prop="wrongCount">
        <el-input v-model="dataForm.wrongCount" placeholder="错误次数"></el-input>
      </el-form-item>
          <el-form-item label="最后一次错误答案" prop="lastWrongAnswer">
        <el-input v-model="dataForm.lastWrongAnswer" placeholder="最后一次错误答案"></el-input>
      </el-form-item>
          <el-form-item label="是否已掌握：0-未掌握，1-已掌握" prop="isMastered">
        <el-input v-model="dataForm.isMastered" placeholder="是否已掌握：0-未掌握，1-已掌握"></el-input>
      </el-form-item>
          <el-form-item label="掌握时间" prop="masteredTime">
        <el-input v-model="dataForm.masteredTime" placeholder="掌握时间"></el-input>
      </el-form-item>
          <el-form-item label="首次错误时间" prop="firstWrongTime">
        <el-input v-model="dataForm.firstWrongTime" placeholder="首次错误时间"></el-input>
      </el-form-item>
          <el-form-item label="最后错误时间" prop="lastWrongTime">
        <el-input v-model="dataForm.lastWrongTime" placeholder="最后错误时间"></el-input>
      </el-form-item>
          <el-form-item label="是否删除：0-未删除，1-已删除" prop="isDeleted">
        <el-input v-model="dataForm.isDeleted" placeholder="是否删除：0-未删除，1-已删除"></el-input>
      </el-form-item>
          <el-form-item label="创建时间" prop="gmtCreate">
        <el-input v-model="dataForm.gmtCreate" placeholder="创建时间"></el-input>
      </el-form-item>
          <el-form-item label="修改时间" prop="gmtModified">
        <el-input v-model="dataForm.gmtModified" placeholder="修改时间"></el-input>
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
  id: '',  userId: '',  userNickname: '',  questionId: '',  subject: '',  questionType: '',  questionTypeName: '',  questionContent: '',  questionImageUrl: '',  correctAnswer: '',  wrongCount: '',  lastWrongAnswer: '',  isMastered: '',  masteredTime: '',  firstWrongTime: '',  lastWrongTime: '',  isDeleted: '',  gmtCreate: '',  gmtModified: ''});

const rules = ref({
          userId: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          userNickname: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          questionId: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          subject: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          questionType: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          questionTypeName: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          questionContent: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          questionImageUrl: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          correctAnswer: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          wrongCount: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          lastWrongAnswer: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          isMastered: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          masteredTime: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          firstWrongTime: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          lastWrongTime: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          isDeleted: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          gmtCreate: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          gmtModified: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ]
  });

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  if (id) {
    getInfo(id);
  }
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get("/demo/apuserwrongcollectionquestion/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/demo/apuserwrongcollectionquestion", dataForm).then((res) => {
      ElMessage.success({
        message: '成功',
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
