import request from '@/utils/request'

export const getLikeStatus = (articleId) => {
    return request.get(`/article/${articleId}/like`)
}

export const toggleLike = (articleId) => {
    return request.post(`/article/${articleId}/like`)
}

export const getMyLikes = () => {
    return request.get('/article/likes/my')
}
