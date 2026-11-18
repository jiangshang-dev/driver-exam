<template>
  <div class="mod-demo__mallorder">
    <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" style="margin-bottom: 20px">
      <el-form-item label="订单号">
        <el-input v-model="queryForm.orderNo" placeholder="请输入订单号" clearable></el-input>
      </el-form-item>
      <el-form-item label="用户ID">
        <el-input v-model="queryForm.userId" placeholder="请输入用户ID" clearable></el-input>
      </el-form-item>
      <el-form-item label="订单类型">
        <el-select v-model="queryForm.orderType" placeholder="全部" clearable style="width: 120px">
          <el-option label="商品订单" value="GOODS"></el-option>
          <el-option label="会员订单" value="VIP"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="订单状态">
        <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="待支付" :value="0"></el-option>
          <el-option label="待发货" :value="1"></el-option>
          <el-option label="已发货" :value="2"></el-option>
          <el-option label="已完成" :value="3"></el-option>
          <el-option label="已取消" :value="4"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="state.dataListLoading" :data="state.dataList" border style="width: 100%">
      <el-table-column prop="id" label="序号" header-align="center" align="center" width="60">
        <template v-slot="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="orderNo" label="订单号" header-align="center" align="center" width="190"></el-table-column>
      <el-table-column prop="userId" label="用户ID" header-align="center" align="center" width="170"></el-table-column>
      <el-table-column prop="orderType" label="订单类型" header-align="center" align="center" width="100">
        <template v-slot="scope">
          {{ scope.row.orderType === 'VIP' ? '会员' : '商品' }}
        </template>
      </el-table-column>
      <el-table-column prop="goodsName" label="商品名称" header-align="center" align="center" min-width="140" show-overflow-tooltip></el-table-column>
      <el-table-column prop="goodsType" label="商品类型" header-align="center" align="center" width="100">
        <template v-slot="scope">
          <el-tag v-if="scope.row.goodsType === 'PHYSICAL'" type="warning">实物</el-tag>
          <el-tag v-else-if="scope.row.goodsType === 'VIRTUAL'" type="success">虚拟</el-tag>
          <el-tag v-else-if="scope.row.goodsType === 'VIP'" type="primary">会员</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payType" label="支付方式" header-align="center" align="center" width="100">
        <template v-slot="scope">
          {{ scope.row.payType === 'WECHAT' ? '微信支付' : '积分' }}
        </template>
      </el-table-column>
      <el-table-column prop="pointsCost" label="积分抵扣" header-align="center" align="center" width="90"></el-table-column>
      <el-table-column prop="cashAmount" label="现金金额(元)" header-align="center" align="center" width="110">
        <template v-slot="scope">
          {{ formatMoney(scope.row.cashAmount) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" header-align="center" align="center" width="90">
        <template v-slot="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收货人" header-align="center" align="center" width="100"></el-table-column>
      <el-table-column prop="gmtCreate" label="创建时间" header-align="center" align="center" width="170"></el-table-column>
      <el-table-column label="操作" fixed="right" header-align="center" align="center" width="180">
        <template v-slot="scope">
          <el-button type="primary" link @click="showDetail(scope.row)">详情 </el-button>
          <el-button v-if="scope.row.status === 1 && scope.row.goodsType === 'PHYSICAL' && state.hasPermission('mall:order:ship')" type="success" link @click="openShipDialog(scope.row)">发货 </el-button>
          <el-button v-if="scope.row.status === 0 && state.hasPermission('mall:order:paid')" type="warning" link @click="markPaid(scope.row)">标记已支付 </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="state.page" :page-sizes="[10, 20, 50, 100]" :page-size="state.limit" :total="state.total" layout="total, sizes, prev, pager, next, jumper" @size-change="state.pageSizeChangeHandle" @current-change="state.pageCurrentChangeHandle"></el-pagination>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shipVisible" title="订单发货" width="480px" :close-on-click-modal="false">
      <el-form :model="shipForm" label-width="90px">
        <el-form-item label="订单号">
          <span>{{ shipForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="物流公司">
          <el-input v-model="shipForm.expressCompany" placeholder="如：顺丰速运"></el-input>
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="shipForm.expressNo" placeholder="请输入物流单号"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" @click="submitShip">确定发货</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="620px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">{{ statusText(currentOrder.status) }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentOrder.userId }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentOrder.payType === 'WECHAT' ? '微信支付' : '积分' }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ currentOrder.goodsName }}</el-descriptions-item>
        <el-descriptions-item label="现金金额">{{ formatMoney(currentOrder.cashAmount) }} 元</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentOrder.receiverName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="物流公司">{{ currentOrder.expressCompany || '-' }}</el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ currentOrder.expressNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ currentOrder.shipTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentOrder.finishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentOrder.gmtCreate }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import useView from "@/hooks/useView";
import { reactive, ref, toRefs } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import baseService from "@/service/baseService";

const view = reactive({
  getDataListURL: "/mall/order/page",
  getDataListIsPage: true
});

const state = reactive({ ...useView(view), ...toRefs(view) });

const queryForm = reactive({
  orderNo: "",
  userId: "",
  orderType: "",
  status: ""
});

const handleQuery = () => {
  if (!state.dataForm) {
    state.dataForm = {};
  }
  Object.assign(state.dataForm as Record<string, any>, queryForm);
  state.page = 1;
  state.getDataList();
};

const resetQuery = () => {
  queryForm.orderNo = "";
  queryForm.userId = "";
  queryForm.orderType = "";
  queryForm.status = "";
  handleQuery();
};

const formatMoney = (fen?: number) => {
  if (fen === null || fen === undefined) {
    return "0.00";
  }
  return (fen / 100).toFixed(2);
};

const statusText = (status: number) => {
  const map: Record<number, string> = {
    0: "待支付",
    1: "待发货",
    2: "已发货",
    3: "已完成",
    4: "已取消"
  };
  return map[status] || "未知";
};

const statusTagType = (status: number) => {
  const map: Record<number, string> = {
    0: "info",
    1: "warning",
    2: "primary",
    3: "success",
    4: "danger"
  };
  return map[status] || "info";
};

const shipVisible = ref(false);
const shipLoading = ref(false);
const shipForm = reactive({
  orderNo: "",
  expressCompany: "",
  expressNo: ""
});

const openShipDialog = (order: any) => {
  shipForm.orderNo = order.orderNo;
  shipForm.expressCompany = "";
  shipForm.expressNo = "";
  shipVisible.value = true;
};

const submitShip = () => {
  if (!shipForm.expressCompany.trim() || !shipForm.expressNo.trim()) {
    ElMessage.warning("请填写物流公司和物流单号");
    return;
  }
  shipLoading.value = true;
  baseService
    .put("/mall/order/ship", { ...shipForm })
    .then((res) => {
      if (res.code !== 0) {
        ElMessage.error(res.msg || "发货失败");
        return;
      }
      ElMessage.success("发货成功");
      shipVisible.value = false;
      state.getDataList();
    })
    .finally(() => {
      shipLoading.value = false;
    });
};

const markPaid = (order: any) => {
  ElMessageBox.confirm(`确定将订单 ${order.orderNo} 标记为已支付？`, "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    baseService.put("/mall/order/paid", { orderNo: order.orderNo }).then((res) => {
      if (res.code !== 0) {
        ElMessage.error(res.msg || "操作失败");
        return;
      }
      ElMessage.success("操作成功");
      state.getDataList();
    });
  });
};

const detailVisible = ref(false);
const currentOrder = ref<any>(null);

const showDetail = (order: any) => {
  currentOrder.value = order;
  detailVisible.value = true;
};
</script>
