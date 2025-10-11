<template>
  <el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" @keyup.enter="dataFormSubmitHandle()" label-width="120px">
      <el-form-item label="科目" prop="subject">
        <el-select v-model="dataForm.subject" placeholder="科目">
          <el-option label="科目一" value="1"></el-option>
          <el-option label="科目四" value="4"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="考试类型" prop="examType">
        <el-select v-model="dataForm.examType" placeholder="考试类型" clearable>
          <el-option v-for="item in examTypeOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue"></el-option>
        </el-select>
      </el-form-item>
<!--      <el-form-item label="题型代码" prop="questionType">
        <el-select v-model="dataForm.questionType" placeholder="题型代码">
          <el-option label="交通标志" value="traffic_signs"></el-option>
          <el-option label="交通法规" value="traffic_rules"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="题型名称" prop="questionTypeName">
        <el-input v-model="dataForm.questionTypeName" placeholder="题型名称"></el-input>
      </el-form-item>-->
      <el-form-item label="题目内容" prop="questionContent">
        <el-input v-model="dataForm.questionContent" placeholder="题目内容"></el-input>
      </el-form-item>
      <el-form-item label="题目格式" prop="questionFormat">
        <el-select v-model="dataForm.questionFormat" placeholder="题目格式">
          <el-option label="选择题" value="1"></el-option>
          <el-option label="判断题" value="2"></el-option>
          <el-option label="多选题" value="3"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="选项A" prop="optionA">
        <el-input v-model="dataForm.optionA" placeholder="选项A"></el-input>
      </el-form-item>
      <el-form-item label="选项B" prop="optionB">
        <el-input v-model="dataForm.optionB" placeholder="选项B"></el-input>
      </el-form-item>
      <el-form-item label="选项C" prop="optionC">
        <el-input v-model="dataForm.optionC" placeholder="选项C"></el-input>
      </el-form-item>
      <el-form-item label="选项D" prop="optionD">
        <el-input v-model="dataForm.optionD" placeholder="选项D"></el-input>
      </el-form-item>
      <el-form-item label="正确答案" prop="correctAnswer">
        <el-select v-model="dataForm.correctAnswer" placeholder="正确答案" multiple>
          <el-option label="A" value="1"></el-option>
          <el-option label="B" value="2"></el-option>
          <el-option label="C" value="3"></el-option>
          <el-option label="D" value="4"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="题目解析" prop="questionExplain">
        <el-input v-model="dataForm.questionExplain" placeholder="题目解析"></el-input>
      </el-form-item>
      <el-form-item label="题目配图URL" prop="imageUrl">
        <el-image
          style="width: 100px; height: 100px"
          v-if="dataForm.imageUrl!==''"
          :src="dataForm.imageUrl"
          :zoom-rate="1.2"
          :max-scale="7"
          :min-scale="0.2"
          show-progress
          :initial-index="4"
          fit="cover"
        />
      </el-form-item>
      <el-form-item label="题目视频URL" prop="videoUrl">
        <el-input v-model="dataForm.videoUrl" placeholder="题目视频URL"></el-input>
      </el-form-item>
      <el-form-item label="题目音频URL" prop="audioUrl">
        <el-input v-model="dataForm.audioUrl" placeholder="题目音频URL"></el-input>
      </el-form-item>
      <el-form-item label="难度等级" prop="difficultyLevel">
        <el-select v-model="dataForm.difficultyLevel" placeholder="难度等级">
          <el-option label="简单" value="1"></el-option>
          <el-option label="中等" value="2"></el-option>
          <el-option label="困难" value="3"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="是否VIP题目" prop="isVip">
        <el-select v-model="dataForm.isVip" placeholder="是否VIP题目">
          <el-option label="免费" value="0"></el-option>
          <el-option label="VIP" value="1"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="是否热门题目" prop="isHot">
        <el-select v-model="dataForm.isHot" placeholder="是否热门题目">
          <el-option label="否" value="0"></el-option>
          <el-option label="是" value="1"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="顺序刷题排序" prop="sequenceOrder">
        <el-input v-model="dataForm.sequenceOrder" placeholder="顺序刷题排序"></el-input>
      </el-form-item>
      <el-form-item label="题目权重" prop="weight">
        <el-input v-model="dataForm.weight" placeholder="题目权重"></el-input>
      </el-form-item>
      <el-form-item label="题目标签" prop="tags">
        <el-select v-model="dataForm.tags" placeholder="题目标签">
          <el-option v-for="item in questionTagsOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue"></el-option>
        </el-select>
      </el-form-item>
<!--      <el-form-item label="知识点" prop="knowledgePoints">
        <el-select v-model="dataForm.knowledgePoints" placeholder="知识点" multiple>
          <el-option v-for="item in knowledgePointsOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue"></el-option>
        </el-select>
      </el-form-item>-->
      <el-form-item label="题目来源" prop="source">
        <el-select v-model="dataForm.source" placeholder="题目来源">
          <el-option label="官方" value="official"></el-option>
          <el-option label="用户贡献" value="user"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="题目状态" prop="status">
        <el-select v-model="dataForm.status" placeholder="题目状态">
          <el-option label="下架" value="0"></el-option>
          <el-option label="可用" value="1"></el-option>
          <el-option label="审核中" value="2"></el-option>
        </el-select>
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

// 定义字典项类型
interface DictItem {
  dictValue: string;
  dictLabel: string;
}

const emit = defineEmits(["refreshDataList"]);

const visible = ref(false);
const dataFormRef = ref();

// 定义字典选项数据
const examTypeOptions = ref<DictItem[]>([]);
const questionTagsOptions = ref<DictItem[]>([]);
const knowledgePointsOptions = ref<DictItem[]>([]);

const dataForm = reactive({
  id: "",
  subject: "",
  examType: "",
  questionType: "",
  questionTypeName: "",
  questionContent: "",
  questionFormat: "",
  optionA: "",
  optionB: "",
  optionC: "",
  optionD: "",
  correctAnswer: [] as string[],
  questionExplain: "",
  imageUrl: "",
  videoUrl: "",
  audioUrl: "",
  difficultyLevel: "",
  isVip: "",
  isHot: "",
  sequenceOrder: "",
  weight: "",
  tags: "",
  knowledgePoints: [] as string[],
  viewCount: "",
  errorCount: "",
  correctCount: "",
  correctRate: "",
  avgAnswerTime: "",
  source: "",
  status: "",
  creatorId: "",
  auditorId: "",
  auditTime: "",
  isDeleted: "",
  gmtCreate: "",
  gmtModified: ""
});

const rules = ref({
  subject: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  questionType: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  questionTypeName: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  questionContent: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  questionFormat: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  optionA: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  optionB: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  correctAnswer: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  questionExplain: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  difficultyLevel: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  isVip: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  isHot: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  sequenceOrder: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  weight: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  tags: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  knowledgePoints: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  viewCount: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  errorCount: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  correctCount: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  correctRate: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  avgAnswerTime: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  source: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  status: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  creatorId: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  auditorId: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
  auditTime: [{ required: true, message: "必填项不能为空", trigger: "blur" }],
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

  // 获取字典数据
  getDictData();

  if (id) {
    getInfo(id);
  }
};

// 获取字典数据
const getDictData = () => {
  // 获取知识点字典数据
  baseService.get("/sys/dict/type/getByDictName?dictCode=knowledge").then((res) => {
    knowledgePointsOptions.value = res.data;
  });

  // 获取题目标签字典数据
  baseService.get("/sys/dict/type/getByDictName?dictCode=question_tags").then((res) => {
    questionTagsOptions.value = res.data;
  });

  // 获取考试类型字典数据
  baseService.get("/sys/dict/type/getByDictName?dictCode=exam_type").then((res) => {
    examTypeOptions.value = res.data;
  });
};

// 获取信息
const getInfo = (id: number) => {
  baseService.get("/apdrivequestion/" + id).then((res) => {
    const data = res.data;

    // 处理多选字段的数据格式
    if (data.correctAnswer && typeof data.correctAnswer === 'string') {
      // 将逗号分隔的字符串转换为数组
      data.correctAnswer = data.correctAnswer.split(',').filter((item: string) => item !== '');
    } else if (!Array.isArray(data.correctAnswer)) {
      // 确保correctAnswer始终是数组
      data.correctAnswer = [];
    }

    if (data.knowledgePoints && typeof data.knowledgePoints === 'string') {
      // 将逗号分隔的字符串转换为数组
      data.knowledgePoints = data.knowledgePoints.split(',').filter((item: string) => item !== '');
    } else if (!Array.isArray(data.knowledgePoints)) {
      // 确保knowledgePoints始终是数组
      data.knowledgePoints = [];
    }

    // 确保所有字段值都是字符串类型，以匹配下拉框的值类型
    if (data.questionFormat !== undefined && data.questionFormat !== null) {
      data.questionFormat = String(data.questionFormat);
    }

    if (data.difficultyLevel !== undefined && data.difficultyLevel !== null) {
      data.difficultyLevel = String(data.difficultyLevel);
    }

    if (data.isVip !== undefined && data.isVip !== null) {
      data.isVip = String(data.isVip);
    }

    if (data.isHot !== undefined && data.isHot !== null) {
      data.isHot = String(data.isHot);
    }

    if (data.status !== undefined && data.status !== null) {
      data.status = String(data.status);
    }

    Object.assign(dataForm, data);
  });
};

// 表单提交
const dataFormSubmitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }

    // 处理数据格式，确保传递给后端的是正确格式
    const submitData: Record<string, any> = { ...dataForm };

    // 处理正确答案字段
    if (Array.isArray(submitData.correctAnswer)) {
      submitData.correctAnswer = submitData.correctAnswer.join(',');
    }

    // 处理知识点字段
    if (Array.isArray(submitData.knowledgePoints)) {
      submitData.knowledgePoints = submitData.knowledgePoints.join(',');
    }

    // 处理科目字段，确保传递数字而不是字符串
    if (typeof submitData.subject === 'string') {
      const subjectValue = parseInt(submitData.subject, 10);
      if (!isNaN(subjectValue)) {
        submitData.subject = subjectValue;
      }
    }

    // 处理考试类型字段，如果为空则删除该字段
    if (!submitData.examType) {
      delete submitData.examType;
    }

    (!dataForm.id ? baseService.post : baseService.put)("/apdrivequestion", submitData).then((res) => {
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
