import api from './base'

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