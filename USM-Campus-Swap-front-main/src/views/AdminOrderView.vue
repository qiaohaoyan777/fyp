<template>
  <div class="admin-container">
    <div class="page-header">
      <h2><el-icon><Document /></el-icon> Order Management</h2>
      <el-tag type="warning" effect="dark">Admin Portal</el-tag>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="Order No.">
          <el-input v-model="query.orderNo" placeholder="Search order number..." clearable />
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="query.status" placeholder="All Status" clearable style="width: 150px">
            <el-option label="Pending" :value="0" />
            <el-option label="Paid/In-Progress" :value="2" />
            <el-option label="Completed" :value="4" />
            <el-option label="Cancelled" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">Search</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="orderList" border stripe v-loading="loading" style="width: 100%; margin-top: 20px">
      <el-table-column prop="orderNo" label="Order ID" width="180" show-overflow-tooltip />
      <el-table-column prop="goodsTitle" label="Product" show-overflow-tooltip />
      <el-table-column prop="totalAmount" label="Price" width="100">
        <template #default="scope">RM {{ scope.row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="buyerName" label="Buyer" width="120" />
      <el-table-column prop="sellerName" label="Seller" width="120" />
      <el-table-column label="Status" width="130">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.orderStatus)">
            {{ getStatusText(scope.row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="Order Date" width="170" />

      <el-table-column label="Actions" width="120" fixed="right">
        <template #default="scope">
          <el-button
              v-if="scope.row.orderStatus < 4"
              type="danger"
              link
              @click="handleCancel(scope.row)"
          >
            Cancel
          </el-button>
          <span v-else>--</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
          v-model:current-page="query.current"
          :page-size="query.size"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="fetchOrders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import myAxios from "@/plugins/request"

const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const query = reactive({
  current: 1,
  size: 10,
  orderNo: '',
  status: null
})

// 加载数据
const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await myAxios.get('/order/list/page', { params: query })
    if (res) {
      // 注意：res 已经是后端 Data 脱壳后的对象了
      orderList.value = res.records
      total.value = res.total
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  query.current = 1
  fetchOrders()
}

// 取消订单逻辑
const handleCancel = (row) => {
  ElMessageBox.confirm(
      `Are you sure you want to cancel order ${row.orderNo}? This will revert item status to Available.`,
      'Admin Intervention',
      { confirmButtonText: 'Yes, Cancel It', type: 'warning' }
  ).then(async () => {
    try {
      // 假设你后端有这个通用的 cancel 接口且支持管理员调用
      await myAxios.post(`/order/cancel/${row.id}`)
      ElMessage.success('Order cancelled successfully')
      fetchOrders()
    } catch (e) { console.error(e) }
  })
}

// 状态映射工具
const getStatusText = (s) => {
  const map = { 0: 'Pending', 1: 'Payment Due', 2: 'Paid', 4: 'Completed', 5: 'Cancelled' }
  return map[s] || 'Unknown'
}
const getStatusTag = (s) => {
  const map = { 0: 'info', 1: 'warning', 2: 'primary', 4: 'success', 5: 'danger' }
  return map[s] || 'info'
}

onMounted(fetchOrders)
</script>

<style scoped>
.admin-container { padding: 20px; background-color: #f5f7fa; min-height: 100vh; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.filter-card { margin-bottom: 20px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>