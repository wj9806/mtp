<template>
  <div class="menu-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <div>
            <el-button type="primary" @click="loadMenus"><el-icon><Refresh /></el-icon> 刷新</el-button>
            <el-button type="primary" @click="handleAdd">新增菜单</el-button>
          </div>
        </div>
      </template>
      <el-table :data="menus" v-loading="loading" row-key="id" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
        <el-table-column prop="menuName" label="菜单名称" width="150" />
        <el-table-column prop="menuCode" label="菜单代码" width="150" />
        <el-table-column prop="path" label="路由路径" />
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? '激活' : '未激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="menuForm" label-width="100px">
        <el-form-item label="菜单名称">
          <el-input v-model="menuForm.menuName" />
        </el-form-item>
        <el-form-item label="菜单代码">
          <el-input v-model="menuForm.menuCode" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="menuForm.path" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="menuForm.orderNum" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="menuForm.status">
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
const menus = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const menuForm = reactive({
  id: null,
  menuName: '',
  menuCode: '',
  path: '',
  orderNum: 0,
  status: 'ACTIVE'
})

const buildTree = (list) => {
  const map = {}
  const roots = []
  list.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  list.forEach(item => {
    if (item.parentId === 0) {
      roots.push(map[item.id])
    } else if (map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    }
  })
  return roots
}

const loadMenus = async () => {
  loading.value = true
  try {
    const res = await api.get('/menu/list')
    menus.value = buildTree(res)
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(menuForm, { id: null, menuName: '', menuCode: '', path: '', orderNum: 0, status: 'ACTIVE' })
  dialogTitle.value = '新增菜单'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(menuForm, { ...row })
  dialogTitle.value = '编辑菜单'
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (menuForm.id) {
      await api.put('/menu/update', menuForm)
    } else {
      await api.post('/menu/add', menuForm)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadMenus()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(async () => {
    try {
      await api.delete(`/menu/${id}`)
      ElMessage.success('删除成功')
      loadMenus()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadMenus()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>