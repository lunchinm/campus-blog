import request from '@/utils/request'

export const getComments = (articleId) => {
    return request.get(`/article/${articleId}/comments`)
}

export const addComment = (articleId, content, parentId) => {
    return request.post(`/article/${articleId}/comments`, { content, parentId })
}

export const deleteComment = (commentId) => {
    return request.delete(`/article/comments/${commentId}`)
}

export const getMyComments = () => {
    return request.get('/article/comments/my')
}

export const batchDeleteComments = (ids) => {
    return request.delete('/article/comments/batch', { data: { ids } })
}

export const toggleCommentLike = (commentId) => {
    return request.post(`/comment/${commentId}/like`)
}

export const getCommentLikeStatus = (commentId) => {
    return request.get(`/comment/${commentId}/like`)
}
