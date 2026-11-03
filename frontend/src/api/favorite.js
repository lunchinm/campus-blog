import request from '@/utils/request'

export const getFavoriteStatus = (articleId) => {
    return request.get(`/article/${articleId}/favorite`)
}

export const toggleFavorite = (articleId) => {
    return request.post(`/article/${articleId}/favorite`)
}

export const getMyFavorites = () => {
    return request.get('/article/favorites/my')
}

export const batchRemoveFavorites = (ids) => {
    return request.delete('/article/favorites/batch', { data: { ids } })
}
