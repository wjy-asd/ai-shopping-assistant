<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrders, cancelOrder } from '../api'
import { USER_ID } from '../store/app'

const router = useRouter()
const orders = ref([])
const loading = ref(false)

// 订单状态对应的标签颜色
const statusMap = {
  待发货: 'warning',
  已发货: 'primary',
  已完成: 'success',
  已取消: 'danger'
}

// 格式化时间：把 ISO 字符串中的 T 换成空格，并截断到秒
function formatTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ').substring(0, 19)
}

// 金额格式化为两位小数
function formatAmount(value) {
  if (value === null || value === undefined || value === '') return '0.00'
  return Number(value).toFixed(2)
}

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await getOrders(USER_ID)
  } finally {
    loading.value = false
  }
}

// 跳转物流查询页并带上订单号
function goLogistics(orderNo) {
  router.push({ path: '/logistics', query: { orderNo } })
}

// 取消订单（仅待发货可取消）
async function handleCancel(order) {
  try {
    await ElMessageBox.confirm(`确认取消订单 ${order.orderNo} 吗？取消后商品库存将退回。`, '取消订单', { type: 'warning' })
  } catch {
    return
  }
  await cancelOrder(order.orderNo)
  ElMessage.success('订单已取消')
  loadOrders()
}

onMounted(loadOrders)
</script>

<template>
  <div class="orders-page">
    <el-card shadow="never">
      <template #header>
        <div class="orders-header">
          <span>我的订单</span>
          <el-button size="small" @click="loadOrders">刷新</el-button>
        </div>
      </template>

      <el-empty
        v-if="!loading && orders.length === 0"
        description="还没有订单，去购物车结算一单吧"
      />

      <div v-for="order in orders" :key="order.orderNo" class="order-card">
        <div class="order-head">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <el-tag :type="statusMap[order.status] || 'info'">{{ order.status }}</el-tag>
          <span class="order-time">{{ formatTime(order.createdAt) }}</span>
        </div>

        <el-table :data="order.items" border size="small">
          <el-table-column prop="productName" label="商品" min-width="180" />
          <el-table-column label="单价" width="100">
            <template #default="{ row }">¥ {{ formatAmount(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="110">
            <template #default="{ row }">¥ {{ formatAmount(row.subtotal) }}</template>
          </el-table-column>
        </el-table>

        <div class="order-foot">
          <span>共 {{ order.items.length }} 件商品，合计：<b class="total">¥ {{ formatAmount(order.totalAmount) }}</b></span>
          <span class="order-actions">
            <el-button size="small" type="primary" plain @click="goLogistics(order.orderNo)">查看物流</el-button>
            <el-button
              v-if="order.status === '待发货'"
              size="small"
              type="danger"
              plain
              @click="handleCancel(order)"
            >取消订单</el-button>
          </span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.orders-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px;
  margin-bottom: 16px;
}

.order-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.order-no {
  font-weight: 600;
}

.order-time {
  color: #909399;
  font-size: 13px;
  margin-left: auto;
}

.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  font-size: 14px;
}

.total {
  color: #f56c6c;
  font-size: 16px;
}

.order-actions {
  display: flex;
  gap: 8px;
}
</style>
