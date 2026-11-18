<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增模型' : '编辑模型'" :close-on-click-modal="false" :close-on-press-escape="false" width="720px">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基础信息" name="base">
        <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px" style="padding: 10px 20px 0">
          <el-form-item label="模型名称" prop="modelName">
            <el-input v-model="dataForm.modelName" placeholder="请求模型名，如 qwen-plus、bge-m3"></el-input>
          </el-form-item>
          <el-form-item label="模型类型" prop="modelType">
            <el-select v-model="dataForm.modelType" placeholder="请选择" style="width: 100%">
              <el-option label="普通大模型（支持多模态）" value="LLM"></el-option>
              <el-option label="向量模型" value="EMBEDDING"></el-option>
              <el-option label="TTS语音模型" value="TTS"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="api地址" prop="baseUrl">
            <el-input v-model="dataForm.baseUrl" placeholder="如 https://dashscope.aliyuncs.com/compatible-mode/v1"></el-input>
          </el-form-item>
          <el-form-item label="api密钥" prop="apiKey">
            <el-input v-model="dataForm.apiKey" type="password" show-password placeholder="请输入api密钥"></el-input>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="dataForm.remark" placeholder="请输入备注"></el-input>
          </el-form-item>
          <el-form-item label="排序" prop="sortOrder">
            <el-input-number v-model="dataForm.sortOrder" :min="0" :step="1"></el-input-number>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="高级配置" name="advanced">
        <el-form label-width="110px" style="padding: 10px 20px 0">
          <el-form-item label="参数">
            <el-select v-model="preset" placeholder="加载预设" clearable style="width: 180px" @change="applyPreset">
              <el-option label="创意" value="creative"></el-option>
              <el-option label="平衡" value="balanced"></el-option>
              <el-option label="精确" value="precise"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="模型温度">
            <el-switch v-model="advanced.temperatureEnabled"></el-switch>
            <el-slider v-model="advanced.temperature" :min="0" :max="2" :step="0.01" :disabled="!advanced.temperatureEnabled" style="flex: 1; margin: 0 16px"></el-slider>
            <el-input-number v-model="advanced.temperature" :min="0" :max="2" :step="0.01" :disabled="!advanced.temperatureEnabled" controls-position="right" style="width: 120px"></el-input-number>
          </el-form-item>

          <el-form-item label="词汇属性">
            <el-switch v-model="advanced.topPEnabled"></el-switch>
            <el-slider v-model="advanced.topP" :min="0" :max="1" :step="0.01" :disabled="!advanced.topPEnabled" style="flex: 1; margin: 0 16px"></el-slider>
            <el-input-number v-model="advanced.topP" :min="0" :max="1" :step="0.01" :disabled="!advanced.topPEnabled" controls-position="right" style="width: 120px"></el-input-number>
          </el-form-item>

          <el-form-item label="话题属性">
            <el-switch v-model="advanced.presenceEnabled"></el-switch>
            <el-slider v-model="advanced.presencePenalty" :min="-2" :max="2" :step="0.1" :disabled="!advanced.presenceEnabled" style="flex: 1; margin: 0 16px"></el-slider>
            <el-input-number v-model="advanced.presencePenalty" :min="-2" :max="2" :step="0.1" :disabled="!advanced.presenceEnabled" controls-position="right" style="width: 120px"></el-input-number>
          </el-form-item>

          <el-form-item label="重复属性">
            <el-switch v-model="advanced.frequencyEnabled"></el-switch>
            <el-slider v-model="advanced.frequencyPenalty" :min="-2" :max="2" :step="0.1" :disabled="!advanced.frequencyEnabled" style="flex: 1; margin: 0 16px"></el-slider>
            <el-input-number v-model="advanced.frequencyPenalty" :min="-2" :max="2" :step="0.1" :disabled="!advanced.frequencyEnabled" controls-position="right" style="width: 120px"></el-input-number>
          </el-form-item>

          <el-form-item label="最大回复">
            <el-switch v-model="advanced.maxTokensEnabled"></el-switch>
            <el-slider v-model="advanced.maxTokens" :min="1" :max="8192" :step="1" :disabled="!advanced.maxTokensEnabled" style="flex: 1; margin: 0 16px"></el-slider>
            <el-input-number v-model="advanced.maxTokens" :min="1" :max="32768" :step="1" :disabled="!advanced.maxTokensEnabled" controls-position="right" style="width: 120px"></el-input-number>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit(false)">保存</el-button>
      <el-button type="primary" :loading="saving" @click="submit(true)">保存并激活</el-button>
      <el-button :loading="testing" @click="testModel">测试可用</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref } from "vue";
import baseService from "@/service/baseService";
import { ElMessage } from "element-plus";

const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const activeTab = ref("base");
const saving = ref(false);
const testing = ref(false);
const dataFormRef = ref();
const preset = ref("");

const dataForm = reactive({
  id: "",
  modelName: "",
  modelType: "LLM",
  baseUrl: "",
  apiKey: "",
  remark: "",
  sortOrder: 0
});

const advanced = reactive({
  temperatureEnabled: false,
  temperature: 0.7,
  topPEnabled: false,
  topP: 1,
  presenceEnabled: false,
  presencePenalty: 0,
  frequencyEnabled: false,
  frequencyPenalty: 0,
  maxTokensEnabled: false,
  maxTokens: 2048
});

const rules = ref({
  modelName: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  modelType: [{ required: true, message: "必填项不能为空", trigger: "change" }],
  baseUrl: [{ required: true, message: "必填项不能为空", trigger: "blur" }]
});

const init = (id?: string | number) => {
  visible.value = true;
  activeTab.value = "base";
  preset.value = "";
  dataForm.id = "";
  Object.assign(dataForm, { modelName: "", modelType: "LLM", baseUrl: "", apiKey: "", remark: "", sortOrder: 0 });
  Object.assign(advanced, {
    temperatureEnabled: false, temperature: 0.7,
    topPEnabled: false, topP: 1,
    presenceEnabled: false, presencePenalty: 0,
    frequencyEnabled: false, frequencyPenalty: 0,
    maxTokensEnabled: false, maxTokens: 2048
  });
  if (dataFormRef.value) {
    dataFormRef.value.clearValidate();
  }
  if (id) {
    getInfo(id);
  }
};

const getInfo = (id: string | number) => {
  baseService.get("/ai/model/" + id).then((res) => {
    if (res.code !== 0 || !res.data) {
      return;
    }
    const data = res.data;
    Object.assign(dataForm, {
      id: data.id,
      modelName: data.modelName,
      modelType: data.modelType,
      baseUrl: data.baseUrl,
      apiKey: data.apiKey,
      remark: data.remark,
      sortOrder: data.sortOrder || 0
    });
    fillAdvanced(data);
  });
};

const fillAdvanced = (data: any) => {
  advanced.temperatureEnabled = data.temperature !== null && data.temperature !== undefined;
  advanced.temperature = data.temperature ?? 0.7;
  advanced.topPEnabled = data.topP !== null && data.topP !== undefined;
  advanced.topP = data.topP ?? 1;
  advanced.presenceEnabled = data.presencePenalty !== null && data.presencePenalty !== undefined;
  advanced.presencePenalty = data.presencePenalty ?? 0;
  advanced.frequencyEnabled = data.frequencyPenalty !== null && data.frequencyPenalty !== undefined;
  advanced.frequencyPenalty = data.frequencyPenalty ?? 0;
  advanced.maxTokensEnabled = data.maxTokens !== null && data.maxTokens !== undefined;
  advanced.maxTokens = data.maxTokens ?? 2048;
};

const applyPreset = (value: string) => {
  if (value === "creative") {
    Object.assign(advanced, { temperatureEnabled: true, temperature: 0.9, topPEnabled: true, topP: 0.95 });
  } else if (value === "balanced") {
    Object.assign(advanced, { temperatureEnabled: true, temperature: 0.7, topPEnabled: true, topP: 0.9 });
  } else if (value === "precise") {
    Object.assign(advanced, { temperatureEnabled: true, temperature: 0.2, topPEnabled: true, topP: 0.5 });
  }
};

const buildPayload = () => {
  return {
    id: dataForm.id || undefined,
    modelName: dataForm.modelName.trim(),
    modelType: dataForm.modelType,
    baseUrl: dataForm.baseUrl.trim(),
    apiKey: dataForm.apiKey,
    remark: dataForm.remark,
    sortOrder: dataForm.sortOrder,
    temperature: advanced.temperatureEnabled ? advanced.temperature : null,
    topP: advanced.topPEnabled ? advanced.topP : null,
    presencePenalty: advanced.presenceEnabled ? advanced.presencePenalty : null,
    frequencyPenalty: advanced.frequencyEnabled ? advanced.frequencyPenalty : null,
    maxTokens: advanced.maxTokensEnabled ? advanced.maxTokens : null
  };
};

const submit = (activate: boolean) => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      activeTab.value = "base";
      return false;
    }
    saving.value = true;
    const isEdit = !!dataForm.id;
    const request = isEdit ? baseService.put("/ai/model", buildPayload()) : baseService.post("/ai/model", buildPayload());
    request
      .then((res) => {
        if (res.code !== 0) {
          ElMessage.error(res.msg || "保存失败");
          return;
        }
        const newId = isEdit ? dataForm.id : res.data;
        if (!activate) {
          ElMessage.success("保存成功");
          closeAndRefresh();
          return;
        }
        return baseService.put(`/ai/model/activate/${newId}`, {}).then((activateRes) => {
          if (activateRes.code !== 0) {
            ElMessage.error(activateRes.msg || "启用失败");
            return;
          }
          ElMessage.success("保存并启用成功");
          closeAndRefresh();
        });
      })
      .finally(() => {
        saving.value = false;
      });
  });
};

const testModel = () => {
  if (!dataForm.id) {
    ElMessage.warning("请先保存后再测试");
    return;
  }
  testing.value = true;
  baseService
    .post(`/ai/model/test/${dataForm.id}`, {})
    .then((res) => {
      if (res.code !== 0) {
        ElMessage.error(res.msg || "模型不可用");
        return;
      }
      ElMessage.success(res.data || "模型可用");
    })
    .finally(() => {
      testing.value = false;
    });
};

const closeAndRefresh = () => {
  visible.value = false;
  emit("refreshDataList");
};

defineExpose({
  init
});
</script>
