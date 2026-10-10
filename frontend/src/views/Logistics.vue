<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getLogistics, getUserLogistics } from '../api'
import { USER_ID } from '../store/app'

const route = useRoute()

const orderNo = ref('')
const loading = ref(false)
const listLoading = ref(false)
const logistics = ref(null)
const searched = ref(false)
const logisticsList = ref([])

// 物流状态对应的标签颜色
const statusMap = {
  已下单: 'info',
  已发货: 'warning',
  运输中: 'primary',
  派件中: 'success',
  已签收: 'success'
}

// 格式化时间：把 ISO 字符串中的 T 换成空格，并截断到秒
function formatTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ').substring(0, 19)
}

// 默认加载当前用户的所有物流记录
async function loadAllLogistics() {
  listLoading.value = true
  try {
    logisticsList.value = await getUserLogistics(USER_ID)
  } finally {
    listLoading.value = false
  }
}

async function handleSearch() {
  const no = orderNo.value.trim()
  if (!no) {
    ElMessage.warning('请输入订单号')
    return
  }
  loading.value = true
  searched.value = true
  try {
    logistics.value = await getLogistics(no)
  } catch (e) {
    logistics.value = null
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  loadAllLogistics()
  // 从「我的订单」页跳转过来时，自动带订单号查询
  const q = route.query.orderNo
  if (q) {
    orderNo.value = String(q)
    await handleSearch()
  }
})
</script>

<template>
  <div class="logistics-page">
    <el-card shadow="never">
      <template #header>
        <span>物流查询</span>
      </template>

      <div class="search-bar">
        <el-input
          v-model="orderNo"
          placeholder="请输入订单号，例如 ORD20250101001"
          clearable
          style="width: 320px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
      </div>

      <el-descriptions
        v-if="logistics"
        :column="1"
        border
        class="logistics-detail"
        title="查询结果"
      >
        <el-descriptions-item label="订单号">{{ logistics.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="物流状态">
          <el-tag :type="statusMap[logistics.status] || 'info'">{{ logistics.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前位置">{{ logistics.location || '—' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(logistics.updatedAt) }}</el-descriptions-item>
      </el-descriptions>

      <el-empty
        v-if="searched && !loading && !logistics"
        description="未找到该订单的物流信息"
      />

      <div class="list-section">
        <div class="section-title">我的物流记录</div>
        <el-table :data="logisticsList" v-loading="listLoading" border>
          <el-table-column prop="orderNo" label="订单号" min-width="180" />
          <el-table-column label="物流状态" width="120">
            <template #default="{ row }">
              <el-tag :type="statusMap[row.status] || 'info'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="当前位置" min-width="160" />
          <el-table-column label="更新时间" width="180">
            <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!listLoading && logisticsList.length === 0" description="暂无物流记录" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.logistics-detail {
  margin-bottom: 20px;
}

.list-section {
  margin-top: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}
</style>
