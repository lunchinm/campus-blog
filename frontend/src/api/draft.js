import request from '@/utils/request'

export const getDrafts = () => request.get('/drafts')

export const getDraft = (id) => request.get(`/drafts/${id}`)

export const saveDraft = (data) => request.post('/drafts', data)

export const updateDraft = (id, data) => request.put(`/drafts/${id}`, data)

export const deleteDraft = (id) => request.delete(`/drafts/${id}`)
