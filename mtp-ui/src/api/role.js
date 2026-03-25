import api from './base'

export const getRoleList = (roleName, page = 1, size = 10) => {
    const params = { page, size }
    if (roleName) params.roleName = roleName
    return api.get('/role/list', { params })
}

export const getRoleMenus = (roleId) => {
    return api.get(`/role/menus/${roleId}`)
}

export const assignRoleMenus = (roleId, menuIds) => {
    return api.post(`/role/menus/${roleId}`, menuIds)
}

export const addRole = (role) => {
    return api.post('/role/add', role)
}

export const updateRole = (role) => {
    return api.put('/role/update', role)
}

export const deleteRole = (id) => {
    return api.delete(`/role/${id}`)
}