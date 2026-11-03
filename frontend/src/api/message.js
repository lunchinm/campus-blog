import request from '@/utils/request'

export const sendMessage = (receiverId, content, type = 'text', extraData = null) =>
    request.post('/message/send', { receiverId, content, type, extraData })

export const getConversations = () => request.get('/message/conversations')

export const getMessages = (conversationId) => request.get(`/message/conversation/${conversationId}`)

export const markConversationAsRead = (conversationId) => request.put(`/message/conversation/${conversationId}/read`)

export const getUnreadMessageCount = () => request.get('/message/unread-count')
