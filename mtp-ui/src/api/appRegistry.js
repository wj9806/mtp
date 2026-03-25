import api from './base'

export const getApplicationRegistryList = (applicationName, page = 1, size = 10) => {
    const params = { page, size }
    if (applicationName) params.applicationName = applicationName
    return api.get('/application-registry/list', { params })
}

export const addApplication = (app) => {
    return api.post('/application-registry/add', app)
}

export const updateApplication = (app) => {
    return api.put('/application-registry/update', app)
}

export const deleteApplication = (id) => {
    return api.delete(`/application-registry/${id}`)
}