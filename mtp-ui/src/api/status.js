import api from './base'

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