import api from './axiosClient.js';
export const getPosts = (params) => {
  const queryParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      value.forEach((item) => queryParams.append(key, item));
    } else if (value !== undefined && value !== null && value !== '') {
      queryParams.append(key, value);
    }
  });

  // Gửi từng tag cùng tên query parameter để Spring bind vào List<String>.
  return api.get('/posts', { params: queryParams });
};
export const getPost = (id) => api.get(`/posts/${id}`);
export const createPost = (body) => api.post('/posts', body);
export const updatePost = (id, body) => api.put(`/posts/${id}`, body);
export const deletePost = (id) => api.delete(`/posts/${id}`);
