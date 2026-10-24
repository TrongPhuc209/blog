import api from './axiosClient.js';
export const login = (body) => api.post('/auth/login', body);
export const register = (body) => api.post('/auth/register', body);
export const getAccount = () => api.get('/auth/account');
export const logout = () => api.post('/auth/logout');
export const editProfile = (body) => api.post('/auth/edit-profile', body);
export const changeAvatar = (body) => api.post('/auth/change-avatar', body);
export const deleteUserRefreshTokens = (userId) => api.post('/auth/delete-refresh-token', null, { params: { userId } });
