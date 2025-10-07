<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="标志名称" prop="signName">
        <el-input v-model="dataForm.signName" placeholder="标志名称"></el-input>
      </el-form-item>
      <el-form-item label="标志说明" prop="signDesc">
        <el-input v-model="dataForm.signDesc" placeholder="标志说明"></el-input>
      </el-form-item>
      <el-form-item label="标志图片" prop="signImage">
        <CoverImageUpload v-model="dataForm.signImage" />
      </el-form-item>
      <el-form-item label="标志类别" prop="signCategory">
        <el-select v-model="dataForm.signCategory" placeholder="请选择">
          <el-option v-for="item in categoryOptions" :key="item.id" :label="item.categoryName" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="标志类型" prop="signType">
        <el-input v-model="dataForm.signType" placeholder="标志类型"></el-input>
      </el-form-item>
      <el-form-item label="使用场景" prop="usageScene">
        <el-input v-model="dataForm.usageScene" placeholder="使用场景"></el-input>
      </el-form-item>
      <el-form-item label="相关法规" prop="relatedRegulations">
        <el-input v-model="dataForm.relatedRegulations" placeholder="相关法规"></el-input>
      </el-form-item>
      <el-form-item label="排序权重" prop="sortOrder">
        <el-input-number v-model="dataForm.sortOrder" placeholder="排序权重"></el-input-number>
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
  signName: "",
  signDesc: "",
  signImage: "",
  signCategory: "",
  signType: "",
  usageScene: "",
  relatedRegulations: "",
  sortOrder: "",
  viewCount: "",
  isDeleted: "",
  gmtCreate: "",
  gmtModified: ""
});

const rules = ref({
  signName: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  signDesc: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  signImage: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  signCategory: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  signType: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  usageScene: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  relatedRegulations: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  sortOrder: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  viewCount: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  isDeleted: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  gmtCreate: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  gmtModified: [{ required: true, message: "必填项不能为空", trigger: "blur" }]
});

const init = (id?: number) => {
  visible.value = true;
  dataForm.id = "";

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

  // 获取分类数据
  getCategory();

  if (id) {
    getInfo(id);
  }
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get("/aptrafficsign/" + id).then((res) => {
    Object.assign(dataForm, res.data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    (!dataForm.id ? baseService.post : baseService.put)("/aptrafficsign", dataForm).then((res) => {
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

interface CategoryOption {
  id: string;
  categoryName: string;
}

// 分类选项
const categoryOptions = ref<CategoryOption[]>([]);

// 获取分类
const getCategory = () => {
  baseService.get("/sys/dict/type/getByDictName?dictCode=traffic_logo").then((res) => {
    if (res.data && Array.isArray(res.data)) {
      console.log('res---',res.data);
      categoryOptions.value = res.data.map(item => ({
        id: item.id,
        categoryName: item.dictLabel
      }));
    }
  })
};
</script>
