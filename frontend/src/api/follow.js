import request from '@/utils/request'

export const followUser = (userId) => request.post(`/follow/${userId}`)
export const unfollowUser = (userId) => request.delete(`/follow/${userId}`)
export const getFollowers = (userId) => request.get(`/follow/followers/${userId}`)
export const getFollowing = (userId) => request.get(`/follow/following/${userId}`)
