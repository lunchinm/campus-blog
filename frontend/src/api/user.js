import request from '@/utils/request'

export const register = (data) => {
    return request.post('/user/register', data)
}

export const login = (data) => {
    return request.post('/user/login', data)
}

export const getUserInfo = () => {
    return request.get('/user/info')
}

export const updateUser = (data) => {
    return request.put('/user/update', data)
}

export const uploadAvatar = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/user/avatar/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

export const getPresetAvatars = () => {
    return request.get('/user/avatars/presets')
}

export const getUserProfile = (id) => {
    return request.get(`/user/${id}/profile`)
}
