import api from './axiosClient.js';
export const getPostComments = (postId, params) => api.get(`/posts/${postId}/comments/isApproved`, { params });
export const getMyComments = (params) => api.get('/comments/me', { params });
export const getComments = (params) => api.get('/comments', { params });
export const getComment = (id) => api.get(`/comments/${id}`);
export const createComment = (postId, body) => api.post(`/posts/${postId}/comments`, body);
export const updateComment = (id, body) => api.put(`/comments/${id}`, body);
// Lombok exposes the primitive field isApproved as the JavaBean property "approved".
export const approveComment = (id, approved) => api.put(`/comments/${id}/approve`, { approved });
export const deleteComment = (id) => api.delete(`/comments/${id}`);
