import request from '@/utils/request'

export const getTags = () => {
    return request.get('/tags')
}
