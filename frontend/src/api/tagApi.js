import api from './axiosClient.js';
export const getTags = (params) => api.get('/tags', { params });
export const getTag = (id) => api.get(`/tags/${id}`);
export const createTag = (body) => api.post('/tags', body);
export const updateTag = (id, body) => api.put(`/tags/${id}`, body);
export const deleteTag = (id) => api.delete(`/tags/${id}`);
export const confirmDeleteTag = (id) => api.delete(`/tags/confirm-delete/${id}`);
