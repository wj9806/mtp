<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="aside">
      <div class="logo">
        <span>MTP</span>
      </div>
      <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          background-color="#263445"
          text-color="#bfcbd9"
          active-text-color="#409eff"
          router
      >
        <template v-for="menu in menus" :key="menu.path">
          <el-menu-item v-if="!menu.children || menu.children.length === 0" :index="menu.path">
            <span>{{ menu.menuName }}</span>
          </el-menu-item>
          <el-sub-menu v-else :index="menu.path">
            <template #title>
              <span>{{ menu.menuName }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
              <span>{{ child.menuName }}</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ nickname || username }}
              <el-icon><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ArrowDown} from '@element-plus/icons-vue'
import api from '../api'

const route = useRoute()
const router = useRouter()

const username = ref(localStorage.getItem('username') || '')
const nickname = ref(localStorage.getItem('nickname') || '')
const menus = ref([])

const activeMenu = computed(() => route.path)

const pageTitleMap = {
  '/applications': '应用列表',
  '/configs': '配置管理',
  '/status': '状态监控',
  '/users': '用户管理',
  '/roles': '角色管理',
  '/menus': '菜单管理'
}

const pageTitle = computed(() => pageTitleMap[route.path] || '')

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.clear()
    router.push('/login')
  }
}

const buildTree = (list) => {
  const map = {}
  const roots = []
  list.forEach(item => {
    map[item.id] = {...item, children: []}
  })
  list.forEach(item => {
    if (item.parentId === 0 || item.parentId === null) {
      roots.push(map[item.id])
    } else if (map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    }
  })
  return roots
}

const loadMenus = async () => {
  try {
    const res = await api.get('/auth/menus')
    menus.value = buildTree(res)
  } catch (error) {
    console.error('加载菜单失败', error)
  }
}

onMounted(() => {
  loadMenus()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #263445;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}

.header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 16px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.main {
  background-color: #f0f2f5;
}
</style>