<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增积分规则' : '修改积分规则'" :close-on-click-modal="false" :close-on-press-escape="false" width="560px">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="110px">
      <el-form-item label="场景编码" prop="sceneCode">
        <el-input v-model="dataForm.sceneCode" placeholder="如：CHECKIN、MOCK_EXAM"></el-input>
      </el-form-item>
      <el-form-item label="场景名称" prop="sceneName">
        <el-input v-model="dataForm.sceneName" placeholder="如：每日签到"></el-input>
      </el-form-item>
      <el-form-item label="计算方式" prop="calcType">
        <el-radio-group v-model="dataForm.calcType">
          <el-radio label="FIXED">固定积分</el-radio>
          <el-radio label="FEN">按实付金额</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="dataForm.calcType === 'FIXED'" label="积分值" prop="points">
        <el-input-number v-model="dataForm.points" :min="0" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item label="每日上限" prop="dailyLimit">
        <el-input-number v-model="dataForm.dailyLimit" :min="0" :step="1"></el-input-number>
        <span class="form-tip">0 表示不限制</span>
      </el-form-item>
      <el-form-item label="状态" prop="enabled">
        <el-radio-group v-model="dataForm.enabled">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="dataForm.remark" type="textarea" :rows="2" placeholder="备注"></el-input>
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
  sceneCode: "",
  sceneName: "",
  points: 0,
  calcType: "FIXED",
  dailyLimit: 1,
  enabled: 1,
  remark: ""
});

const rules = ref({
  sceneCode: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  sceneName: [{ required: true, message: "必填项不能为空", trigger: "blur" }]
});

const init = (id?: string | number) => {
  visible.value = true;
  dataForm.id = "";
  Object.assign(dataForm, {
    sceneCode: "",
    sceneName: "",
    points: 0,
    calcType: "FIXED",
    dailyLimit: 1,
    enabled: 1,
    remark: ""
  });

  if (dataFormRef.value) {
    dataFormRef.value.clearValidate();
  }

  if (id) {
    getInfo(id);
  }
};

const getInfo = (id: string | number) => {
  baseService.get("/mall/pointsRule/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    const submitData: any = {
      id: dataForm.id,
      sceneCode: dataForm.sceneCode,
      sceneName: dataForm.sceneName,
      points: dataForm.points,
      calcType: dataForm.calcType,
      dailyLimit: dataForm.dailyLimit,
      enabled: dataForm.enabled,
      remark: dataForm.remark
    };
    (!dataForm.id ? baseService.post : baseService.put)("/mall/pointsRule", submitData).then((res) => {
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

<style scoped>
.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
