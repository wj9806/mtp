import api from './base'

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