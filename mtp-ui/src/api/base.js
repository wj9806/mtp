import axios from 'axios'
import router from '../router'

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

export default api