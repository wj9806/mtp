import api from './base'

export const getApplicationList = () => {
    return api.get('/applicationList')
}

export const getApplications = (applicationName, page = 1, size = 4) => {
    const params = { page, size }
    if (applicationName) params.applicationName = applicationName
    return api.get('/applications', { params })
}