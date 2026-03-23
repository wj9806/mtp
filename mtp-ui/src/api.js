import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export const getApplications = () => api.get('/applications')

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

export default api