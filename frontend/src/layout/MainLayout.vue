<script setup>
import { useRoute } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()
const activeMenu = computed(() => '/' + route.path.split('/')[1])
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">小购</div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item index="/products">
          <span>商品列表</span>
        </el-menu-item>
        <el-menu-item index="/cart">
          <span>购物车</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <span>我的订单</span>
        </el-menu-item>
        <el-menu-item index="/logistics">
          <span>物流查询</span>
        </el-menu-item>
        <el-menu-item index="/chat">
          <span>在线客服</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background: #ffffff;
  color: #303133;
  border-right: 1px solid #ebeef5;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.03);
}

.logo {
  height: 64px;
  line-height: 64px;
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  color: #ff6a00;
  letter-spacing: 2px;
  border-bottom: 1px solid #f0f0f0;
}

.menu {
  border-right: none;
  background: transparent;
  padding: 12px 8px;
}

.menu :deep(.el-menu-item) {
  color: #606266;
  border-radius: 8px;
  margin-bottom: 4px;
  height: 44px;
  line-height: 44px;
}

.menu :deep(.el-menu-item.is-active) {
  color: #ff6a00;
  background: #fff2e8;
  font-weight: 600;
}

.menu :deep(.el-menu-item:hover) {
  background: #f5f6f8;
  color: #ff6a00;
}

.main {
  background: #f5f6f8;
  padding: 24px;
  overflow-y: auto;
}
</style>
