import api from './axiosClient.js';
export const getRoles = (params) => api.get('/roles', { params });
export const getRole = (id) => api.get(`/roles/${id}`);
export const createRole = (body) => api.post('/roles', body);
export const updateRole = (id, body) => api.put(`/roles/${id}`, body);
export const deleteRole = (id) => api.delete(`/roles/${id}`);
