import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  withCredentials: true
});

let accessToken = null;
let refreshRequest = null;
export function setAccessToken(token) { accessToken = token; }

api.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});

// Cookie refreshToken được browser gửi tự động. Chỉ giữ access token trong bộ nhớ JS.
api.interceptors.response.use((response) => response, async (error) => {
  const originalRequest = error.config;
  const requestUrl = originalRequest?.url || '';
  const isAuthRequest = requestUrl.includes('/auth/login') || requestUrl.includes('/auth/refresh');
  if (error.response?.status !== 401 || originalRequest?._retried || isAuthRequest) throw error;

  originalRequest._retried = true;
  try {
    // Các request cùng hết hạn dùng chung một lần refresh.
    if (!refreshRequest) {
      refreshRequest = axios.post(
        `${api.defaults.baseURL}/auth/refresh-with-cookie`,
        null,
        { withCredentials: true }
      ).finally(() => { refreshRequest = null; });
    }
    const response = await refreshRequest;
    const newToken = response.data?.data?.accessToken;
    if (!newToken) throw new Error('Backend không trả access token mới');
    setAccessToken(newToken);
    originalRequest.headers.Authorization = `Bearer ${newToken}`;
    return api(originalRequest);
  } catch (refreshError) {
    setAccessToken(null);
    window.dispatchEvent(new Event('auth:expired'));
    throw refreshError;
  }
});

export function getErrorMessage(error) {
  const status = error.response?.status;
  const message = error.response?.data?.message;
  if (message) return message;
  if (status === 400) return 'Dữ liệu gửi lên chưa hợp lệ.';
  if (status === 401) return 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';
  if (status === 403) return 'Bạn không có quyền thực hiện thao tác này.';
  if (status === 404) return 'Không tìm thấy dữ liệu yêu cầu.';
  if (status === 409) return 'Dữ liệu đã tồn tại hoặc đang được sử dụng.';
  if (status >= 500) return 'Máy chủ gặp lỗi. Vui lòng thử lại sau.';
  return error.message || 'Đã xảy ra lỗi. Vui lòng thử lại.';
}

export default api;
