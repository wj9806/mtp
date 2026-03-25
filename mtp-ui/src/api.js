import axios from 'axios'
import router from './router'

const api = axios.create({
    baseURL: '/api',
    timeout: 30000
})

api.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => Promise.reject(error)
)

api.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== undefined) {
            if (res.code !== 200) {
                return Promise.reject(new Error(res.message || 'Request failed'))
            }
            return res.data
        }
        return res
    },
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.clear()
            if (router.currentRoute.value.path !== '/login') {
                router.push('/login')
            }
        }
        console.error('API Error:', error)
        return Promise.reject(error)
    }
)

export const getApplicationList = () => {
    return api.get('/applicationList')
}

export const getApplications = (applicationName, page = 1, size = 4) => {
    const params = { page, size }
    if (applicationName) params.applicationName = applicationName
    return api.get('/applications', { params })
}

export const getConfigs = (applicationName, ip, port, page = 1, size = 10) => {
    const params = { page, size }
    if (applicationName) params.applicationName = applicationName
    if (ip) params.ip = ip
    if (port) params.port = port
    return api.get('/config/list', { params })
}

export const updateConfig = (config) => api.put('/config/update', config)

export const updateBatchConfig = (applicationName, poolName, config) => {
    return api.put(`/config/update-batch?applicationName=${applicationName}&poolName=${poolName}`, config)
}

export const getConfigsByPool = (applicationName, poolName) => {
    return api.get(`/config/get-by-pool?applicationName=${applicationName}&poolName=${poolName}`)
}

export const getStatuses = (applicationName, ip, port) => {
    const params = {}
    if (applicationName) params.applicationName = applicationName
    if (ip) params.ip = ip
    if (port) params.port = port
    return api.get('/status/list', { params })
}

export const refreshStatus = (instanceId, poolName) => {
    return api.post(`/status/refresh/${instanceId}/${poolName}`)
}

export const login = (username, password) => {
    return api.post('/auth/login', { username, password })
}

export const logout = () => {
    return api.post('/auth/logout')
}

export const getMenus = () => {
    return api.get('/auth/menus')
}

export const getCurrentUser = () => {
    return api.get('/auth/currentUser')
}

export const getUserList = (username, page = 1, size = 10) => {
    const params = { page, size }
    if (username) params.username = username
    return api.get('/user/list', { params })
}

export const getAllRoles = () => {
    return api.get('/user/roles')
}

export const getUserRoles = (userId) => {
    return api.get(`/user/${userId}/roles`)
}

export const addUser = (user) => {
    return api.post('/user/add', user)
}

export const updateUser = (user) => {
    return api.put('/user/update', user)
}

export const deleteUser = (id) => {
    return api.delete(`/user/${id}`)
}

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

export const getMenuList = () => {
    return api.get('/menu/list')
}

export const addMenu = (menu) => {
    return api.post('/menu/add', menu)
}

export const updateMenu = (menu) => {
    return api.put('/menu/update', menu)
}

export const deleteMenu = (id) => {
    return api.delete(`/menu/${id}`)
}

export default api