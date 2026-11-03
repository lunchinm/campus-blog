import request from '@/utils/request'

export const getNotifications = () => request.get('/notification/list')
export const getUnreadCount = () => request.get('/notification/unread-count')
export const markAsRead = (id) => request.put(`/notification/read/${id}`)
export const markAllAsRead = () => request.put('/notification/read-all')
