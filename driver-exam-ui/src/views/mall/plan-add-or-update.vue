<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增套餐' : '修改套餐'" :close-on-click-modal="false" :close-on-press-escape="false" width="560px">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="110px">
      <el-form-item label="套餐名称" prop="planName">
        <el-input v-model="dataForm.planName" placeholder="如：月卡、季卡、年卡"></el-input>
      </el-form-item>
      <el-form-item label="会员天数" prop="days">
        <el-input-number v-model="dataForm.days" :min="1" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item label="售价(元)" prop="priceYuan">
        <el-input-number v-model="dataForm.priceYuan" :min="0" :precision="2" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item label="原价(元)" prop="originPriceYuan">
        <el-input-number v-model="dataForm.originPriceYuan" :min="0" :precision="2" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item label="赠送积分" prop="pointsReward">
        <el-input-number v-model="dataForm.pointsReward" :min="0" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item label="是否推荐" prop="recommended">
        <el-radio-group v-model="dataForm.recommended">
          <el-radio :label="1">推荐</el-radio>
          <el-radio :label="0">不推荐</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="dataForm.sortOrder" :min="0" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="dataForm.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
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
  id: "",
  planName: "",
  days: 30,
  priceYuan: 0,
  originPriceYuan: 0,
  pointsReward: 0,
  recommended: 0,
  status: 1,
  sortOrder: 0
});

const rules = ref({
  planName: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  days: [{ required: true, message: "必填项不能为空", trigger: "blur" }]
});

const init = (id?: string | number) => {
  visible.value = true;
  dataForm.id = "";
  Object.assign(dataForm, {
    planName: "",
    days: 30,
    priceYuan: 0,
    originPriceYuan: 0,
    pointsReward: 0,
    recommended: 0,
    status: 1,
    sortOrder: 0
  });

  if (dataFormRef.value) {
    dataFormRef.value.clearValidate();
  }

  if (id) {
    getInfo(id);
  }
};

const getInfo = (id: string | number) => {
  baseService.get("/mall/plan/" + id).then((res) => {
    Object.assign(dataForm, res.data);
    dataForm.priceYuan = res.data.price ? res.data.price / 100 : 0;
    dataForm.originPriceYuan = res.data.originPrice ? res.data.originPrice / 100 : 0;
  });
};

const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    const submitData: any = {
      id: dataForm.id,
      planName: dataForm.planName,
      days: dataForm.days,
      price: Math.round(Number(dataForm.priceYuan) * 100),
      originPrice: Math.round(Number(dataForm.originPriceYuan) * 100),
      pointsReward: dataForm.pointsReward,
      recommended: dataForm.recommended,
      status: dataForm.status,
      sortOrder: dataForm.sortOrder
    };
    (!dataForm.id ? baseService.post : baseService.put)("/mall/plan", submitData).then((res) => {
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
