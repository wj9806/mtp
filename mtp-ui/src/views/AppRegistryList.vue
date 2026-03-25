<template>
  <div class="app-registry-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>应用注册表</span>
          <div class="header-controls">
            <el-input
              v-model="searchName"
              placeholder="搜索应用名称"
              clearable
              style="width: 200px"
              @clear="handleSearch"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" @click="handleSearch" style="margin-left: 10px">搜索</el-button>
            <el-button type="primary" @click="handleAdd">新增</el-button>
          </div>
        </div>
      </template>
      <el-table :data="applications" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="applicationName" label="应用名称" width="200" />
        <el-table-column prop="applicationInfo" label="应用信息" min-width="200" />
        <el-table-column prop="accessToken" label="访问令牌" min-width="300">
          <template #default="scope">
            <div class="token-cell">
              <span class="token-text">{{ scope.row.accessToken }}</span>
              <el-button type="text" @click="copyToken(scope.row.accessToken)">复制</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑应用' : '新增应用'" width="500px">
      <el-form :model="editingApp" label-width="100px">
        <el-form-item label="应用名称">
          <el-input v-model="editingApp.applicationName" placeholder="请输入应用名称" disabled/>
        </el-form-item>
        <el-form-item label="应用信息">
          <el-input v-model="editingApp.applicationInfo" type="textarea" placeholder="请输入应用信息" :rows="3" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="访问令牌">
          <el-input :value="editingApp.accessToken" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveApp">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplicationRegistryList, addApplication, updateApplication, deleteApplication } from '../api'

const applications = ref([])
const loading = ref(false)
const searchName = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingApp = ref({
  id: null,
  applicationName: '',
  applicationInfo: '',
  accessToken: ''
})

const loadApplications = async () => {
  loading.value = true
  try {
    const data = await getApplicationRegistryList(searchName.value, currentPage.value, pageSize.value)
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

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadApplications()
}

const handleAdd = () => {
  isEdit.value = false
  editingApp.value = {
    id: null,
    applicationName: '',
    applicationInfo: '',
    accessToken: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editingApp.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除应用"${row.applicationName}"吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteApplication(row.id)
      ElMessage.success('删除成功')
      loadApplications()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const saveApp = async () => {
  if (!editingApp.value.applicationName) {
    ElMessage.error('应用名称不能为空')
    return
  }
  try {
    if (isEdit.value) {
      await updateApplication(editingApp.value)
      ElMessage.success('更新成功')
    } else {
      await addApplication(editingApp.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadApplications()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const copyToken = (token) => {
  navigator.clipboard.writeText(token).then(() => {
    ElMessage.success('令牌已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
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

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.token-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.token-text {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>