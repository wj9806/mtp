<template>
  <div class="role-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <div>
            <el-button type="primary" @click="loadRoles"><el-icon><Refresh /></el-icon> 刷新</el-button>
            <el-button type="primary" @click="handleAdd">新增角色</el-button>
          </div>
        </div>
      </template>
      <el-table :data="roles" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleCode" label="角色代码" width="250" />
        <el-table-column prop="roleName" label="角色名称" width="250" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="150">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? '激活' : '未激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="280">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="warning" link @click="handleAssignMenus(scope.row)">分配菜单</el-button>
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
          @size-change="loadRoles"
          @current-change="loadRoles"
          style="margin-top: 20px"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="角色代码">
          <el-input v-model="roleForm.roleCode" :disabled="!!roleForm.id" />
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.roleName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="roleForm.status">
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

    <el-dialog v-model="menuDialogVisible" title="分配菜单" width="500px">
      <el-tree
          ref="menuTreeRef"
          :data="menuTreeData"
          :props="{ label: 'menuName', children: 'children' }"
          node-key="id"
          :check-strictly="false"
          show-checkbox
          default-expand-all
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api from '../api'

const loading = ref(false)
const roles = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const roleForm = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  description: '',
  status: 'ACTIVE'
})

const menuDialogVisible = ref(false)
const menuTreeRef = ref()
const menuTreeData = ref([])
const currentRoleId = ref(null)
const allMenus = ref([])

const loadRoles = async () => {
  loading.value = true
  try {
    const res = await api.get('/role/list', { params: { page: page.value, size: size.value } })
    roles.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(roleForm, { id: null, roleCode: '', roleName: '', description: '', status: 'ACTIVE' })
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(roleForm, { ...row })
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (roleForm.id) {
      await api.put('/role/update', roleForm)
    } else {
      await api.post('/role/add', roleForm)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadRoles()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/role/${id}`)
      ElMessage.success('删除成功')
      loadRoles()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

const handleAssignMenus = async (row) => {
  currentRoleId.value = row.id
  try {
    const res = await api.get(`/role/menus/${row.id}`)
    allMenus.value = buildTree(res.allMenus)
    menuTreeData.value = buildTree(res.allMenus)
    menuDialogVisible.value = true
    nextTick(() => {
      menuTreeRef.value.setCheckedKeys(res.assignedMenuIds || [])
    })
  } catch (error) {
    console.error(error)
    ElMessage.error('加载菜单失败')
  }
}

const handleSaveMenus = async () => {
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  try {
    await api.post(`/role/menus/${currentRoleId.value}`, checkedKeys)
    ElMessage.success('保存成功')
    menuDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

const buildTree = (list) => {
  const map = {}
  const roots = []
  list.forEach(item => {
    map[item.id] = { ...item, children: [] }
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

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
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