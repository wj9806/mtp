<template>
  <div class="user-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div>
            <el-button type="primary" @click="loadUsers"><el-icon><Refresh /></el-icon> 刷新</el-button>
            <el-button type="primary" @click="handleAdd">新增用户</el-button>
          </div>
        </div>
      </template>
      <el-table :data="users" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="roles" label="角色">
          <template #default="scope">
            <el-tag v-for="role in scope.row.roles" :key="role" size="small" style="margin-right: 4px">
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? '激活' : '未激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadUsers"
          @current-change="loadUsers"
          style="margin-top: 20px"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" :disabled="!!userForm.id" />
        </el-form-item>
        <el-form-item label="密码" v-if="!userForm.id">
          <el-input v-model="userForm.password" type="password" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in allRoles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="userForm.status">
            <el-option label="激活" value="ACTIVE" />
            <el-option label="未激活" value="INACTIVE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api from '../api'

const loading = ref(false)
const users = ref([])
const allRoles = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  status: 'ACTIVE',
  roleIds: []
})

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await api.get('/user/list', { params: { page: page.value, size: size.value } })
    users.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  try {
    allRoles.value = await api.get('/user/roles')
  } catch (error) {
    ElMessage.error('加载角色失败')
  }
}

const handleAdd = () => {
  Object.assign(userForm, { id: null, username: '', password: '', nickname: '', email: '', phone: '', status: 'ACTIVE', roleIds: [] })
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  Object.assign(userForm, { id: row.id, username: row.username, password: '', nickname: row.nickname, email: row.email, phone: row.phone, status: row.status, roleIds: row.roleIds || [] })
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (userForm.id) {
      await api.put('/user/update', userForm)
    } else {
      await api.post('/user/add', userForm)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/user/${id}`)
      ElMessage.success('删除成功')
      loadUsers()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  loadUsers()
  loadRoles()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>