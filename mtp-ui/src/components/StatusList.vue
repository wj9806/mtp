<template>
  <div class="status-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>线程池状态监控</span>
          <div>
            <el-select v-model="selectedApp" placeholder="选择应用" clearable @change="loadStatuses" style="width: 200px">
              <el-option
                v-for="app in applications"
                :key="app.applicationName"
                :label="app.applicationName"
                :value="app.applicationName"
              />
            </el-select>
            <el-button type="primary" @click="loadStatuses" style="margin-left: 10px">刷新</el-button>
          </div>
        </div>
      </template>
      <el-table :data="statuses" style="width: 100%" v-loading="loading">
        <el-table-column label="实时刷新" width="80">
          <template #default="scope">
            <el-button type="primary" link @click="refreshStatusRow(scope.row)">
              <span>🗘</span>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="poolName" label="线程池名称" width="150" />
        <el-table-column prop="applicationName" label="应用名称" width="150" />
        <el-table-column prop="ip" label="IP地址" width="150" />
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="corePoolSize" label="核心线程数" width="100" />
        <el-table-column prop="maxPoolSize" label="最大线程数" width="100" />
        <el-table-column prop="activeCount" label="活跃线程数" width="100" />
        <el-table-column prop="poolSize" label="当前线程数" width="100" />
        <el-table-column prop="taskCount" label="任务总数" width="80" />
        <el-table-column prop="completedTaskCount" label="已完成任务" width="110" />
        <el-table-column prop="queueSize" label="队列大小" width="80" />
        <el-table-column prop="queueCapacity" label="队列容量" width="80" />
        <el-table-column prop="updateTimeTime" label="心跳时间" width="170">
          <template #default="scope">
            {{ formatTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import {getApplicationList, getStatuses, refreshStatus as refreshStatusApi} from '../api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  applicationName: String,
  ip: String,
  port: Number
})

const applications = ref([])
const statuses = ref([])
const loading = ref(false)
const selectedApp = ref(props.applicationName || '')
let refreshTimer = null

const loadApplications = async () => {
  try {
    const data = await getApplicationList()
    applications.value = data || []
  } catch (error) {
    console.error('Failed to load applications:', error)
  }
}

const loadStatuses = async () => {
  loading.value = true
  try {
    const data = await getStatuses(selectedApp.value || null)
    statuses.value = data || []
  } catch (error) {
    ElMessage.error('加载状态列表失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(Number(timestamp))
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const refreshStatusRow = async (status) => {
  try {
    await refreshStatusApi(status.instanceId, status.poolName)
    ElMessage.success('刷新成功')
    loadStatuses()
  } catch (error) {
    ElMessage.error('刷新失败')
  }
}

const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadStatuses()
  }, 30000)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  loadApplications()
  loadStatuses()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: nowrap;
}

.card-header > div {
  display: flex;
  flex-wrap: nowrap;
}
</style>
