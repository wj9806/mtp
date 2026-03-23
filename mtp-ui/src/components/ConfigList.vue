<template>
  <div class="config-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>线程池配置列表</span>
          <div class="header-controls">
            <el-select v-model="selectedApp" placeholder="选择应用" clearable @change="onAppChange" style="width: 200px">
              <el-option
                v-for="app in applications"
                :key="app.applicationName"
                :label="app.applicationName"
                :value="app.applicationName"
              />
            </el-select>
            <el-select v-model="selectedPool" placeholder="选择线程池" clearable @change="loadConfigs" style="margin-left: 10px;width: 200px" :disabled="!selectedApp">
              <el-option
                v-for="pool in poolNames"
                :key="pool"
                :label="pool"
                :value="pool"
              />
            </el-select>
            <el-button type="primary" @click="loadConfigs" style="margin-left: 10px">刷新</el-button>
          </div>
        </div>
      </template>
      <div class="batch-tip" v-if="selectedApp && selectedPool">
        <el-alert type="info" :closable="false" show-icon>
          <div class="batch-tip-content">
            <span>当前为批量编辑模式，修改将同步更新该应用下所有实例的线程池配置</span>
            <el-button type="primary" size="small" @click="editBatchConfig" class="batch-edit-btn">编辑</el-button>
          </div>
        </el-alert>
      </div>
      <el-table :data="configs" style="width: 100%" v-loading="loading">
        <el-table-column prop="poolName" label="线程池名称" width="250" />
        <el-table-column prop="applicationName" label="应用名称" width="250" />
        <el-table-column prop="ip" label="IP地址" width="180" />
        <el-table-column prop="port" label="端口" width="100" />
        <el-table-column prop="corePoolSize" label="核心线程数" width="100" />
        <el-table-column prop="maxPoolSize" label="最大线程数" width="100" />
        <el-table-column prop="queueCapacity" label="队列容量" width="100" />
        <el-table-column prop="keepAliveSeconds" label="存活时间(秒)" width="120" />
        <el-table-column prop="rejectedPolicy" label="拒绝策略" width="200" />
        <el-table-column label="操作" width="150" v-if="!isBatchMode">
          <template #default="scope">
            <el-button type="text" @click="editConfig(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isBatchMode ? '批量编辑线程池配置' : '编辑线程池配置'" width="500px">
      <el-form :model="editingConfig" label-width="120px">
        <el-form-item label="线程池名称">
          <el-input v-model="editingConfig.poolName" disabled />
        </el-form-item>
        <el-form-item label="应用名称">
          <el-input v-model="editingConfig.applicationName" disabled />
        </el-form-item>
        <el-form-item v-if="!isBatchMode" label="IP地址">
          <el-input v-model="editingConfig.ip" disabled />
        </el-form-item>
        <el-form-item v-if="!isBatchMode" label="端口">
          <el-input v-model="editingConfig.port" disabled />
        </el-form-item>
        <el-form-item label="核心线程数">
          <el-input-number v-model="editingConfig.corePoolSize" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="最大线程数">
          <el-input-number v-model="editingConfig.maxPoolSize" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="队列容量">
          <el-input-number v-model="editingConfig.queueCapacity" :min="1" :max="100000" />
        </el-form-item>
        <el-form-item label="存活时间(秒)">
          <el-input-number v-model="editingConfig.keepAliveSeconds" :min="0" :max="3600" />
        </el-form-item>
        <el-form-item label="拒绝策略">
          <el-select v-model="editingConfig.rejectedPolicy">
            <el-option label="AbortPolicy" value="abort" />
            <el-option label="DiscardPolicy" value="discard" />
            <el-option label="DiscardOldestPolicy" value="discard-oldest" />
            <el-option label="CallerRunsPolicy" value="caller-runs" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getApplications, getConfigs, updateConfig, updateBatchConfig, getConfigsByPool } from '../api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  applicationName: String,
  ip: String,
  port: Number
})

const emit = defineEmits(['navigate'])

const applications = ref([])
const configs = ref([])
const poolNames = ref([])
const loading = ref(false)
const selectedApp = ref(props.applicationName || '')
const selectedPool = ref('')
const dialogVisible = ref(false)
const editingConfig = ref({})
const isBatchMode = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadApplications = async () => {
  try {
    const data = await getApplications()
    applications.value = data || []
  } catch (error) {
    console.error('Failed to load applications:', error)
  }
}

const onAppChange = () => {
  selectedPool.value = ''
  poolNames.value = []
  currentPage.value = 1
  if (selectedApp.value) {
    loadPoolNames()
  }
  loadConfigs()
}

const loadPoolNames = async () => {
  if (!selectedApp.value) return
  try {
    const data = await getConfigs(selectedApp.value, null, null, 1, 1000)
    if (data && data.content) {
      const pools = new Set(data.content.map(c => c.poolName))
      poolNames.value = Array.from(pools)
    } else if (data && Array.isArray(data)) {
      const pools = new Set(data.map(c => c.poolName))
      poolNames.value = Array.from(pools)
    }
  } catch (error) {
    console.error('Failed to load pool names:', error)
  }
}

const loadConfigs = async () => {
  loading.value = true
  try {
    let data
    if (selectedPool.value) {
      data = await getConfigsByPool(selectedApp.value, selectedPool.value)
      isBatchMode.value = true
      total.value = Array.isArray(data) ? data.length : 0
      configs.value = Array.isArray(data) ? data : []
    } else {
      data = await getConfigs(selectedApp.value, null, null, currentPage.value, pageSize.value)
      isBatchMode.value = false
      if (data && data.content) {
        total.value = data.totalElements || 0
        configs.value = data.content
      } else if (data && Array.isArray(data)) {
        total.value = data.length
        configs.value = data
      } else {
        total.value = 0
        configs.value = []
      }
    }
  } catch (error) {
    ElMessage.error('加载配置列表失败')
    configs.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadConfigs()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadConfigs()
}

const editConfig = (config) => {
  editingConfig.value = { ...config }
  dialogVisible.value = true
}

const editBatchConfig = () => {
  if (configs.value.length > 0) {
    editingConfig.value = { ...configs.value[0] }
    isBatchMode.value = true
    dialogVisible.value = true
  }
}

const saveConfig = async () => {
  if (!validateConfig(editingConfig.value)) {
    return
  }
  try {
    if (isBatchMode.value && selectedPool.value) {
      await updateBatchConfig(selectedApp.value, selectedPool.value, editingConfig.value)
      ElMessage.success('批量配置更新成功')
    } else {
      await updateConfig(editingConfig.value)
      ElMessage.success('配置更新成功')
    }
    dialogVisible.value = false
    loadConfigs()
  } catch (error) {
    ElMessage.error(error.response?.data?.error || '配置更新失败')
  }
}

const validateConfig = (config) => {
  if (!config.corePoolSize || config.corePoolSize < 0) {
    ElMessage.error('核心线程数必须 >= 0')
    return false
  }
  if (!config.maxPoolSize || config.maxPoolSize < 0) {
    ElMessage.error('最大线程数必须 >= 0')
    return false
  }
  if (config.corePoolSize > config.maxPoolSize) {
    ElMessage.error('核心线程数不能大于最大线程数')
    return false
  }
  if (!config.queueCapacity || config.queueCapacity < 0) {
    ElMessage.error('队列容量必须 >= 0')
    return false
  }
  if (!config.keepAliveSeconds || config.keepAliveSeconds < 0) {
    ElMessage.error('存活时间必须 >= 0')
    return false
  }
  return true
}

onMounted(() => {
  loadApplications()
  loadConfigs()
})

watch(() => props.applicationName, (val) => {
  if (val) {
    selectedApp.value = val
    onAppChange()
  }
})
</script>

<style scoped>
.header-controls {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.batch-tip {
  margin-bottom: 10px;
}

.batch-tip-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.batch-edit-btn {
  flex-shrink: 0;
}

.batch-tip :deep(.el-alert__content) {
  display: flex;
  align-items: center;
  width: 100%;
}
</style>