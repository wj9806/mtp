import api from './base'

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