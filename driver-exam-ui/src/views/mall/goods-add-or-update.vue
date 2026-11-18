<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增商品' : '修改商品'" :close-on-click-modal="false" :close-on-press-escape="false" width="620px">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="110px">
      <el-form-item label="商品名称" prop="goodsName">
        <el-input v-model="dataForm.goodsName" placeholder="请输入商品名称"></el-input>
      </el-form-item>
      <el-form-item label="商品类型" prop="goodsType">
        <el-radio-group v-model="dataForm.goodsType">
          <el-radio label="VIRTUAL">虚拟商品</el-radio>
          <el-radio label="PHYSICAL">实物商品</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-input v-model="dataForm.category" placeholder="如：会员、资料、教辅、周边"></el-input>
      </el-form-item>
      <el-form-item label="封面图片" prop="coverImage">
        <CoverImageUpload v-model="dataForm.coverImage" />
      </el-form-item>
      <el-form-item label="商品简介" prop="summary">
        <el-input v-model="dataForm.summary" type="textarea" :rows="2" placeholder="请输入商品简介"></el-input>
      </el-form-item>
      <el-form-item label="积分价" prop="pointsPrice">
        <el-input-number v-model="dataForm.pointsPrice" :min="0" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item label="现金价(元)" prop="cashPriceYuan">
        <el-input-number v-model="dataForm.cashPriceYuan" :min="0" :precision="2" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item label="奖励类型" prop="rewardType">
        <el-select v-model="dataForm.rewardType" placeholder="请选择" style="width: 100%">
          <el-option label="无" value="NONE"></el-option>
          <el-option label="赠送VIP天数" value="VIP_DAYS"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item v-if="dataForm.rewardType === 'VIP_DAYS'" label="奖励天数" prop="rewardValue">
        <el-input-number v-model="dataForm.rewardValue" :min="0" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item label="库存" prop="stock">
        <el-input-number v-model="dataForm.stock" :min="0" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="dataForm.sortOrder" :min="0" :step="1"></el-input-number>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="dataForm.status">
          <el-radio :label="1">上架</el-radio>
          <el-radio :label="0">下架</el-radio>
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
import CoverImageUpload from "@/components/CoverImageUpload.vue";

const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

const dataForm = reactive({
  id: "",
  goodsName: "",
  goodsType: "VIRTUAL",
  category: "",
  coverImage: "",
  summary: "",
  pointsPrice: 0,
  cashPriceYuan: 0,
  rewardType: "NONE",
  rewardValue: 0,
  stock: 0,
  status: 1,
  sortOrder: 0
});

const rules = ref({
  goodsName: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  goodsType: [{ required: true, message: "必填项不能为空", trigger: "change" }],
  category: [{ required: true, message: "必填项不能为空", trigger: "blur" }]
});

const init = (id?: string | number) => {
  visible.value = true;
  dataForm.id = "";
  Object.assign(dataForm, {
    goodsName: "",
    goodsType: "VIRTUAL",
    category: "",
    coverImage: "",
    summary: "",
    pointsPrice: 0,
    cashPriceYuan: 0,
    rewardType: "NONE",
    rewardValue: 0,
    stock: 0,
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
  baseService.get("/mall/goods/" + id).then((res) => {
    Object.assign(dataForm, res.data);
    dataForm.cashPriceYuan = res.data.cashPrice ? res.data.cashPrice / 100 : 0;
  });
};

const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    const submitData: any = {
      id: dataForm.id,
      goodsName: dataForm.goodsName,
      goodsType: dataForm.goodsType,
      category: dataForm.category,
      coverImage: dataForm.coverImage,
      summary: dataForm.summary,
      pointsPrice: dataForm.pointsPrice,
      cashPrice: Math.round(Number(dataForm.cashPriceYuan) * 100),
      rewardType: dataForm.rewardType,
      rewardValue: dataForm.rewardValue,
      stock: dataForm.stock,
      status: dataForm.status,
      sortOrder: dataForm.sortOrder
    };
    (!dataForm.id ? baseService.post : baseService.put)("/mall/goods", submitData).then((res) => {
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
