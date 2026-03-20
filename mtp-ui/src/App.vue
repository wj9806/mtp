<template>
  <div class="app-container">
    <el-container class="layout-container">
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <h3>MTP</h3>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="applications">
            <span>应用列表</span>
          </el-menu-item>
          <el-menu-item index="configs">
            <span>线程池配置</span>
          </el-menu-item>
          <el-menu-item index="statuses">
            <span>线程池状态</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container class="content-wrapper">
        <el-header class="header">
          <h1>线程池监控平台</h1>
        </el-header>
        <el-main class="main-content">
          <ApplicationList v-if="activeMenu === 'applications'" />
          <ConfigList v-if="activeMenu === 'configs'" @navigate="handleNavigate" />
          <StatusList v-if="activeMenu === 'statuses'" @navigate="handleNavigate" />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ApplicationList from './components/ApplicationList.vue'
import ConfigList from './components/ConfigList.vue'
import StatusList from './components/StatusList.vue'

const activeMenu = ref('applications')

const handleMenuSelect = (index) => {
  activeMenu.value = index
}

const handleNavigate = (tab, params) => {
  activeMenu.value = tab
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.app-container {
  height: 100%;
}

.layout-container {
  height: 100%;
}

.sidebar {
  background-color: #304156;
  color: #fff;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #263445;
  flex-shrink: 0;
}

.logo h3 {
  color: #fff;
  font-size: 20px;
  margin: 0;
}

.sidebar-menu {
  border-right: none;
  background-color: #304156;
  flex: 1;
}

.sidebar-menu .el-menu-item {
  color: #bfcbd9;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-menu-item.is-active {
  background-color: #263445;
  color: #409eff;
}

.content-wrapper {
  flex-direction: column;
  height: 100%;
}

.header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  flex-shrink: 0;
  height: 60px;
}

.header h1 {
  font-size: 20px;
  font-weight: 500;
}

.main-content {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
  flex: 1;
}
</style>
