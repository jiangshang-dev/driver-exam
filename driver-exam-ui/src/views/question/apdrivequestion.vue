<template>
  <div class="mod-demo__apdrivequestion">
    <!-- 添加查询条件表单 -->
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px">
      <el-form-item label="科目">
        <el-select v-model="queryForm.subject" placeholder="请选择科目" clearable>
          <el-option label="科目一" value="1"></el-option>
          <el-option label="科目四" value="4"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="考试类型">
        <el-select v-model="queryForm.examType" placeholder="请选择考试类型" clearable>
          <el-option label="C1" value="C1"></el-option>
          <el-option label="C2" value="C2"></el-option>
          <el-option label="C3" value="C3"></el-option>
          <el-option label="C4" value="C4"></el-option>
          <el-option label="A1" value="A1"></el-option>
          <el-option label="A2" value="A2"></el-option>
          <el-option label="A3" value="A3"></el-option>
          <el-option label="B1" value="B1"></el-option>
          <el-option label="B2" value="B2"></el-option>
          <el-option label="D" value="D"></el-option>
          <el-option label="E" value="E"></el-option>
          <el-option label="F" value="F"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="题目标签" prop="tags">
        <el-select v-model="queryForm.tags" placeholder="题目标签">
          <el-option v-for="item in questionTagsOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="题目内容">
        <el-input v-model="queryForm.questionContent" placeholder="请输入题目内容" clearable></el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-form :inline="true" :model="state.dataForm" @keyup.enter="state.getDataList()">
      <el-form-item>
        <el-button v-if="state.hasPermission('apdrivequestion:save')" type="primary" @click="addOrUpdateHandle()">新增 </el-button>
      </el-form-item>
<!--      <el-form-item>
        <el-button v-if="state.hasPermission('apdrivequestion:delete')" type="danger" @click="state.deleteHandle()"> 删除 </el-button>
      </el-form-item>-->
      <el-form-item>
        <el-button v-if="state.hasPermission('apdrivequestion:sync')" type="primary" @click="openSyncDialog()"> 同步题库 </el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border @selection-change="state.dataListSelectionChangeHandle" style="width: 100%">
      <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
      <el-table-column prop="id" label="序号" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="subject" label="科目" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.subject === 1 ? "科目一" : scope.row.subject === 4 ? "科目四" : scope.row.subject }}
        </template>
      </el-table-column>
      <el-table-column prop="tags" label="题目标签" header-align="center" align="center">
        <!--      questionTagsOption显示字典里面的名称name
         questionTagsOptions: [ { "id": "1975119388950986754", "dictLabel": "交通信号灯题", "dictValue": "traffic_light" } ]
         scope.row.tags和questionTagsOptions匹配显示名称
         -->
        <template v-slot="scope">
          {{ questionTagsOptions.find((item) => item.dictValue === scope.row.tags)?.dictLabel || "" }}
        </template>
      </el-table-column>
      <!--      <el-table-column prop="questionType" label="题型代码" header-align="center" align="center">-->
      <!-- ：traffic_signs-交通标志，traffic_rules-交通法规等-->
      <!--      </el-table-column>-->
      <el-table-column prop="examType" label="题型类型" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.examType ? scope.row.examType.code || scope.row.examType : "" }}
        </template>
      </el-table-column>
      <el-table-column prop="questionTypeName" label="题型名称" header-align="center" align="center"></el-table-column>
      <el-table-column prop="questionContent" label="题目内容" header-align="center" align="center"></el-table-column>
      <el-table-column prop="questionFormat" label="题目格式" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.questionFormat === 1 ? "选择题" : scope.row.questionFormat === 2 ? "判断题" : "多选题" }}
        </template>
      </el-table-column>
      <el-table-column prop="optionA" label="选项A" header-align="center" align="center"></el-table-column>
      <el-table-column prop="optionB" label="选项B" header-align="center" align="center"></el-table-column>
      <el-table-column prop="optionC" label="选项C" header-align="center" align="center"></el-table-column>
      <el-table-column prop="optionD" label="选项D" header-align="center" align="center"></el-table-column>
      <el-table-column prop="correctAnswer" label="正确答案" header-align="center" align="center">
        <template v-slot="scope">
          <!-- ：单选:1/2/3/4,多选:1,2,3 -->
          {{ scope.row.questionFormat === 1 ? scope.row.correctAnswer : scope.row.questionFormat === 2 ? (scope.row.correctAnswer === "0" ? "否" : "是") : scope.row.correctAnswer }}
        </template>
      </el-table-column>
      <el-table-column prop="questionExplain" label="题目解析" header-align="center" align="center">
        <template v-slot="scope">
          <el-popover placement="top-start" title="查看解析" width="200" trigger="hover" :content="scope.row.questionExplain">
            <template #reference>
              <el-button v-if="scope.row.questionExplain" type="primary" link>查看解析</el-button>
            </template>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="imageUrl" label="题目配图URL" header-align="center" align="center">
        <template v-slot="scope">
          <el-image style="width: 50px; height: 50px" :src="scope.row.imageUrl" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="videoUrl" label="题目视频URL" header-align="center" align="center"></el-table-column>
      <el-table-column prop="audioUrl" label="题目音频URL" header-align="center" align="center"></el-table-column>
      <el-table-column prop="difficultyLevel" label="难度等级" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.difficultyLevel === 1 ? "简单" : scope.row.difficultyLevel === 2 ? "中等" : "困难" }}
        </template>
      </el-table-column>
      <el-table-column prop="isVip" label="是否VIP题目" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.isVip === 0 ? "免费" : "VIP" }}
        </template>
      </el-table-column>
      <el-table-column prop="isHot" label="是否热门题目" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.isHot === 0 ? "否" : "是" }}
        </template>
      </el-table-column>
      <el-table-column prop="sequenceOrder" label="顺序刷题排序" header-align="center" align="center"></el-table-column>
      <el-table-column prop="weight" label="题目权重" header-align="center" align="center"></el-table-column>
<!--      <el-table-column prop="knowledgePoints" label="知识点" header-align="center" align="center"></el-table-column>-->
      <el-table-column prop="viewCount" label="浏览次数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="errorCount" label="错误次数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="correctCount" label="正确次数" header-align="center" align="center"></el-table-column>
      <el-table-column prop="correctRate" label="正确率" header-align="center" align="center"></el-table-column>
      <el-table-column prop="avgAnswerTime" label="平均答题时间(秒)" header-align="center" align="center"></el-table-column>
      <el-table-column prop="source" label="题目来源" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.source === "official" ? "官方" : "用户贡献" }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="题目状态" header-align="center" align="center">
        <template v-slot="scope">
          {{ scope.row.status === 0 ? "下架" : scope.row.status === 1 ? "可用" : "审核中" }}
        </template>
      </el-table-column>
      <el-table-column prop="creatorId" label="创建者" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditorId" label="审核者" header-align="center" align="center"></el-table-column>
      <el-table-column prop="auditTime" label="审核时间" header-align="center" align="center"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
        <template v-slot="scope">
          <el-button v-if="state.hasPermission('apdrivequestion:update')" type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改 </el-button>
<!--          <el-button v-if="state.hasPermission('apdrivequestion:delete')" type="primary" link @click="state.deleteHandle(scope.row.id)">删除 </el-button>-->
        </template>
      </el-table-column>
    </el-table>

    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>

    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="state.getDataList">确定</add-or-update>

    <!-- 同步题库弹窗 -->
    <el-dialog title="同步题库" v-model="syncDialogVisible" width="500px" @close="handleSyncDialogClose">
      <el-form :model="syncForm" label-width="100px">
        <el-form-item label="学科">
          <el-select v-model="syncForm.subject" placeholder="请选择学科">
            <el-option label="科目一" value="ONE"></el-option>
            <el-option label="科目四" value="FOUR"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="车型">
          <el-select v-model="syncForm.model" placeholder="请选择车型">
            <el-option v-for="item in examTypeOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="syncDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSync">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { onMounted, reactive, ref, toRefs } from "vue";
import baseService from "@/service/baseService";
import AddOrUpdate from "./apdrivequestion-add-or-update.vue";
import { ElMessage } from "element-plus";

const view = reactive({
  deleteIsBatch: true,
  getDataListURL: "/apdrivequestion/page",
  getDataListIsPage: true,
  exportURL: "/apdrivequestion/export",
  deleteURL: "/apdrivequestion"
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const addOrUpdateRef = ref();
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id);
};

// 添加查询表单相关
const queryForm = reactive({
  subject: "",
  examType: "",
  questionType: "",
  tags: "",
  questionTypeName: "",
  questionContent: ""
});

const handleQuery = () => {
  // 确保dataForm已初始化并合并查询条件
  if (!state.dataForm) {
    state.dataForm = {};
  }
  Object.assign(state.dataForm as Record<string, any>, queryForm);
  state.page = 1; // 重置到第一页
  state.getDataList();
};

const resetQuery = () => {
  queryForm.subject = "";
  queryForm.examType = "";
  queryForm.questionType = "";
  queryForm.questionTypeName = "";
  queryForm.questionContent = "";
  queryForm.tags = "";
  handleQuery();
  // 确保dataForm已初始化并清空查询条件
  if (!state.dataForm) {
    state.dataForm = {};
  }
  Object.assign(state.dataForm as Record<string, any>, queryForm);
  state.page = 1;
  state.getDataList();
};

// 同步题库相关
const syncDialogVisible = ref(false);
const syncForm = reactive({
  subject: null,
  model: ""
});

const openSyncDialog = () => {
  syncDialogVisible.value = true;
  // 打开对话框时获取字典数据
  getDictData();
};

const handleSyncDialogClose = () => {
  syncForm.subject = null;
  syncForm.model = "";
};

// 定义字典项类型
interface DictItem {
  dictValue: string;
  dictLabel: string;
}

// 定义字典选项数据
const examTypeOptions = ref<DictItem[]>([]);
const modelOptions = ref<DictItem[]>([]);
const questionTagsOptions = ref<DictItem[]>([]);

// 获取字典数据
const getDictData = () => {
  // 获取考试类型字典数据
  baseService.get("/sys/dict/type/getByDictName?dictCode=exam_type").then((res) => {
    examTypeOptions.value = res.data;
  });

  // 获取题目标签字典数据
  baseService.get("/sys/dict/type/getByDictName?dictCode=question_tags").then((res) => {
    questionTagsOptions.value = res.data;
  });
};

// 同步题库
const confirmSync = () => {
  if (!syncForm.subject || !syncForm.model) {
    ElMessage.warning("请选择学科和车型");
    return;
  }

  baseService
    .get("/apdrivequestion/syncQuestion", {
      subject: syncForm.subject,
      model: syncForm.model
    })
    .then((res) => {
      ElMessage.success({
        message: "同步成功",
        duration: 500,
        onClose: () => {
          state.getDataList();
          syncDialogVisible.value = false;
        }
      });
    })
    .catch(() => {
      ElMessage.error("同步失败");
    });
};

// 组件挂载时获取字典数据
onMounted(() => {
  getDictData();
});
</script>
