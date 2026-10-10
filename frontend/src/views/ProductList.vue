<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProducts, addToCart } from '../api'
import { USER_ID, bumpCart } from '../store/app'

// 搜索关键词与分类过滤
const keyword = ref('')
const category = ref('')
const categories = ['运动鞋', '数码', '服装']

// 商品列表与加载状态
const products = ref([])
const loading = ref(false)

// 加载商品列表
async function loadProducts() {
  loading.value = true
  try {
    products.value = await getProducts({ keyword: keyword.value, category: category.value })
  } finally {
    loading.value = false
  }
}

// 加入购物车
async function handleAddToCart(product) {
  if (!product.stock || product.stock <= 0) {
    ElMessage.warning('该商品暂无库存')
    return
  }
  await addToCart({ userId: USER_ID, productId: product.id, quantity: 1 })
  ElMessage.success(`已将「${product.name}」加入购物车`)
  bumpCart()
}

// 重置搜索条件
function resetSearch() {
  keyword.value = ''
  category.value = ''
  loadProducts()
}

onMounted(loadProducts)
</script>

<template>
  <div class="product-page">
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索商品名称 / 分类 / 描述"
          clearable
          style="width: 320px"
          @keyup.enter="loadProducts"
        />
        <el-select v-model="category" placeholder="全部分类" clearable style="width: 160px">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-button type="primary" @click="loadProducts">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </el-card>

    <el-row :gutter="16" class="product-grid" v-loading="loading">
      <el-col v-for="product in products" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="product-card">
          <div class="product-category">{{ product.category }}</div>
          <div class="product-name">{{ product.name }}</div>
          <div class="product-desc">{{ product.description }}</div>
          <div class="product-price">¥ {{ product.price }}</div>
          <div class="product-stock">库存：{{ product.stock }} 件</div>
          <el-button type="primary" class="add-btn" @click="handleAddToCart(product)">
            加入购物车
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
  </div>
</template>

<style scoped>
.search-card {
  margin-bottom: 16px;
}

.search-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.product-grid {
  min-height: 200px;
}

.product-card {
  margin-bottom: 16px;
  height: 260px;
  display: flex;
  flex-direction: column;
  transition: all 0.25s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.product-category {
  display: inline-block;
  color: #ff6a00;
  background: #fff2e8;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 8px;
  width: fit-content;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}

.product-desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
  flex: 1;
  overflow: hidden;
}

.product-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
  margin: 8px 0 4px;
}

.product-stock {
  color: #909399;
  font-size: 12px;
  margin-bottom: 10px;
}

.add-btn {
  width: 100%;
}
</style>
