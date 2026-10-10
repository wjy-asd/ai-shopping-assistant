<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCart, updateCart, removeCart, clearCart, checkoutOrder } from '../api'
import { USER_ID, appState, bumpCart } from '../store/app'

const loading = ref(false)
const cart = ref({ items: [], totalQuantity: 0, totalAmount: 0 })

// 加载购物车
async function loadCart() {
  loading.value = true
  try {
    cart.value = await getCart(USER_ID)
  } finally {
    loading.value = false
  }
}

// 修改数量
async function handleChangeQuantity(row, val) {
  if (!val || val <= 0) {
    val = 1
  }
  if (val > row.stock) {
    ElMessage.warning(`库存仅剩 ${row.stock} 件`)
    val = row.stock
  }
  row.quantity = val
  await updateCart(row.cartId, { quantity: val })
  ElMessage.success('数量已更新')
  bumpCart()
  loadCart()
}

// 删除单个条目
async function handleRemove(row) {
  await ElMessageBox.confirm(`确定删除「${row.productName}」吗？`, '提示', { type: 'warning' })
  await removeCart(row.cartId)
  ElMessage.success('删除成功')
  bumpCart()
  loadCart()
}

// 清空购物车
async function handleClear() {
  if (cart.value.items.length === 0) return
  await ElMessageBox.confirm('确定清空购物车吗？', '提示', { type: 'warning' })
  await clearCart(USER_ID)
  ElMessage.success('购物车已清空')
  bumpCart()
  loadCart()
}

// 结算购物车：生成订单并清空购物车
async function handleCheckout() {
  if (cart.value.items.length === 0) return
  await ElMessageBox.confirm('确认结算当前购物车吗？', '提示', { type: 'warning' })
  const order = await checkoutOrder(USER_ID)
  ElMessage.success(`下单成功，订单号：${order.orderNo}`)
  bumpCart()
  loadCart()
}

onMounted(loadCart)
// 其它页面（如 AI 客服）加购后，通过 cartVersion 自动刷新购物车
watch(() => appState.cartVersion, loadCart)
</script>

<template>
  <div class="cart-page">
    <el-card shadow="never">
      <template #header>
        <div class="cart-header">
          <span>我的购物车</span>
          <div>
            <el-button size="small" @click="loadCart">刷新</el-button>
            <el-button size="small" type="danger" plain @click="handleClear">清空购物车</el-button>
          </div>
        </div>
      </template>

      <el-table :data="cart.items" v-loading="loading" border>
        <el-table-column prop="productName" label="商品名称" min-width="180" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="单价" width="120">
          <template #default="{ row }">¥ {{ row.price }}</template>
        </el-table-column>
        <el-table-column label="数量" width="180">
          <template #default="{ row }">
            <el-input-number
              :model-value="row.quantity"
              :min="1"
              :max="row.stock || 1"
              size="small"
              @change="(val) => handleChangeQuantity(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            <span class="subtotal">¥ {{ row.subtotal }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" text @click="handleRemove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && cart.items.length === 0" description="购物车还是空的，去挑几件商品吧" />

      <div v-if="cart.items.length > 0" class="cart-footer">
        <span>共 {{ cart.totalQuantity }} 件商品</span>
        <span class="total-amount">合计：¥ {{ cart.totalAmount }}</span>
        <el-button type="primary" @click="handleCheckout">结算</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.cart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
}

.subtotal {
  color: #f56c6c;
  font-weight: 600;
}

.cart-footer {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 20px;
  font-size: 14px;
}

.total-amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
}
</style>
