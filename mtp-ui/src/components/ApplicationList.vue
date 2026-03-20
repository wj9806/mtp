<template>
  <div class="application-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>应用列表</span>
          <el-button type="primary" @click="loadApplications">刷新</el-button>
        </div>
      </template>
      <el-table :data="applications" style="width: 100%" v-loading="loading">
        <el-table-column prop="applicationName" label="应用名称" />
        <el-table-column prop="instanceCount" label="实例数量" width="150">
          <template #default="scope">
            <el-tag type="success">{{ scope.row.instances ? scope.row.instances.length : 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="text" @click="viewInstances(scope.row)">查看实例</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="instanceDialogVisible" title="实例列表" width="600px">
      <el-table :data="currentInstances" style="width: 100%">
        <el-table-column prop="ip" label="IP地址" />
        <el-table-column prop="port" label="端口" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="text" @click="viewConfigs(scope.row)">配置</el-button>
            <el-button type="text" @click="viewStatuses(scope.row)">状态</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getApplications } from '../api'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['navigate'])

const applications = ref([])
const loading = ref(false)
const instanceDialogVisible = ref(false)
const currentInstances = ref([])
const currentApp = ref(null)

const loadApplications = async () => {
  loading.value = true
  try {
    const data = await getApplications()
    applications.value = data || []
  } catch (error) {
    ElMessage.error('加载应用列表失败')
  } finally {
    loading.value = false
  }
}

const viewInstances = (app) => {
  currentApp.value = app
  currentInstances.value = app.instances || []
  instanceDialogVisible.value = true
}

const viewConfigs = (instance) => {
  emit('navigate', 'configs', { applicationName: currentApp.value.applicationName, ip: instance.ip, port: instance.port })
  instanceDialogVisible.value = false
}

const viewStatuses = (instance) => {
  emit('navigate', 'statuses', { applicationName: currentApp.value.applicationName, ip: instance.ip, port: instance.port })
  instanceDialogVisible.value = false
}

onMounted(() => {
  loadApplications()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
