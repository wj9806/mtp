<template>
  <div class="application-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>应用列表</span>
          <div class="header-controls">
            <el-input
              v-model="searchName"
              placeholder="输入应用名称查询"
              clearable
              style="width: 200px"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" @click="handleSearch" style="margin-left: 10px">查询</el-button>
            <el-button type="primary" @click="loadApplications">刷新</el-button>
          </div>
        </div>
      </template>
      <div v-loading="loading">
        <el-row :gutter="20" v-if="pagedApplications.length > 0">
          <el-col :span="6" v-for="app in pagedApplications" :key="app.applicationName" class="app-col">
            <el-card class="app-card" shadow="hover">
              <div class="app-content">
                <div style="display: flex; align-items: center; gap: 10px; justify-content: center;">
                  <div class="app-name">{{ app.applicationName }}</div>
                  <div class="app-info">
                    <span>实例: {{ app.instances ? app.instances.length : 0 }}</span>
                  </div>
                </div>
                <div class="app-instances">
                  <div class="instance-tags">
                    <el-tag
                      v-for="(instance, index) in getDisplayInstances(app.instances)"
                      :key="index"
                      :type="instance.status === 'ONLINE' ? 'success' : 'danger'"
                      size="small"
                      class="instance-tag"
                    >
                      {{ instance.ip }}:{{ instance.port }}
                    </el-tag>
                    <el-tag v-if="app.instances && app.instances.length > 6" size="small">
                      ......
                    </el-tag>
                  </div>
                </div>
              </div>
              <div class="app-actions">
                <el-button type="primary" size="small" @click="viewInstances(app)">查看实例</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else-if="!loading" description="暂无应用数据" />
        <el-pagination
          v-if="total > 0"
          layout="total, prev, pager, next"
          :page-size="pageSize"
          :total="total"
          v-model:current-page="currentPage"
          @current-change="handlePageChange"
          class="pagination"
        />
      </div>
    </el-card>

    <el-dialog v-model="instanceDialogVisible" width="600px">
       <template #header>
         <div class="instance-dialog-header">
           <span>实例列表 ({{ currentApp?.applicationName }})</span>
           <el-input
            v-model="instanceSearchText"
            placeholder="搜索IP或端口"
            clearable
            style="width: 200px"
          />
        </div>
      </template>
      <div class="instance-dialog-content">
        <el-table :data="filteredInstances" style="width: 100%" max-height="300">
          <el-table-column prop="ip" label="IP地址" />
          <el-table-column prop="port" label="端口" />
          <el-table-column label="状态">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'ONLINE' ? 'success' : 'danger'">
                {{ scope.row.status === 'ONLINE' ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <el-button type="text" @click="viewConfigs(scope.row)">配置</el-button>
              <el-button type="text" @click="viewStatuses(scope.row)">状态</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getApplications } from '../api'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['navigate'])

const applications = ref([])
const loading = ref(false)
const instanceDialogVisible = ref(false)
const currentInstances = ref([])
const currentApp = ref(null)
const currentPage = ref(1)
const pageSize = 4
const total = ref(0)
const searchName = ref('')
const instanceSearchText = ref('')

const pagedApplications = computed(() => applications.value)

const filteredInstances = computed(() => {
  if (!instanceSearchText.value) {
    return currentInstances.value
  }
  const search = instanceSearchText.value.toLowerCase()
  return currentInstances.value.filter(inst =>
    inst.ip.toLowerCase().includes(search) ||
    String(inst.port).toLowerCase().includes(search)
  )
})

const getDisplayInstances = (instances) => {
  if (!instances) return []
  return instances.slice(0, 6)
}

const loadApplications = async () => {
  loading.value = true
  try {
    const data = await getApplications(searchName.value, currentPage.value, pageSize)
    applications.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error('加载应用列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadApplications()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadApplications()
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
.header-controls {
  display: flex;
  align-items: center;
}
.app-col {
  height: 200px;
}
.app-card {
  margin-bottom: 20px;
  height: 200px;
  box-sizing: border-box;
}
.app-card :deep(.el-card__body) {
  height: 200px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.app-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.app-actions {
  margin-top: auto;
}
.app-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}
.app-instances {
  margin-bottom: 10px;
}
.instance-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.instance-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}
.app-info {
  margin-bottom: 10px;
  color: #909399;
  font-size: 12px;
}
.instance-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.instance-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.app-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}
.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>