<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
          <el-form-item label="用户名" prop="username">
        <el-input v-model="dataForm.username" placeholder="用户名"></el-input>
      </el-form-item>
          <el-form-item label="密码" prop="password">
        <el-input v-model="dataForm.password" placeholder="密码"></el-input>
      </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="dataForm.realName" placeholder="真实姓名"></el-input>
      </el-form-item>
          <el-form-item label="头像" prop="avatar">
        <el-input v-model="dataForm.avatar" placeholder="头像"></el-input>
      </el-form-item>
          <el-form-item label="手机号" prop="phone">
        <el-input v-model="dataForm.phone" placeholder="手机号"></el-input>
      </el-form-item>
          <el-form-item label="邮箱" prop="email">
        <el-input v-model="dataForm.email" placeholder="邮箱"></el-input>
      </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="dataForm.gender">
              <el-radio label="1">男</el-radio>
              <el-radio label="2">女</el-radio>
              <el-radio label="0">未知</el-radio>
            </el-radio-group>
        </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="dataForm.status">
              <el-radio label="1">正常</el-radio>
              <el-radio label="0">禁用</el-radio>
            </el-radio-group>
        </el-form-item>
          <el-form-item label="是否为VIP" prop="vipLevel">
            <el-radio-group v-model="dataForm.vipLevel">
              <el-radio label="1">是</el-radio>
              <el-radio label="0">否</el-radio>
            </el-radio-group>
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
  id: '',  username: '',  password: '',  realName: '',  avatar: '',  phone: '',  email: '',  gender: '',  status: '',  vipLevel: '',  lastLoginTime: '',  createTime: '',  updateTime: ''});

const rules = ref({
          username: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          password: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          realName: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          avatar: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          phone: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          email: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          gender: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          status: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          vipLevel: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          lastLoginTime: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          createTime: [
      { required: true, message: '必填项不能为空', trigger: 'blur' }
    ],
          updateTime: [
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
  baseService.get("/apuser/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/apuser", dataForm).then((res) => {
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
