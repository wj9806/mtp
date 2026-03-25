import api from './base'

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