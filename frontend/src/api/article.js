import request from '@/utils/request'

export const getArticleList = () => {
    return request.get('/article/list')
}

export const searchArticles = (keyword) => {
    return request.get('/article/search', { params: { keyword } })
}

export const getArticleDetail = (id) => {
    return request.get(`/article/${id}`)
}

export const addArticle = (data) => {
    return request.post('/article', data)
}

export const updateArticle = (data) => {
    return request.put('/article', data)
}

export const deleteArticle = (id) => {
    return request.delete(`/article/${id}`)
}

export const getMyArticles = () => {
    return request.get('/article/my')
}

export const batchDeleteArticles = (ids) => {
    return request.delete('/article/batch', { data: { ids } })
}

export const getFollowingArticles = () => {
    return request.get('/article/following')
}
