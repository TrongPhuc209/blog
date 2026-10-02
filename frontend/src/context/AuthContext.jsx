import React, { createContext, useContext, useEffect, useState } from 'react';
import { getAccount, login as loginRequest, logout as logoutRequest } from '../api/authApi.js';
import { setAccessToken } from '../api/axiosClient.js';

const AuthContext = createContext(null);
export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  async function signIn(credentials) {
    const response = await loginRequest(credentials);
    const data = response.data.data;
    // Backend hiện trả refresh token trong body lẫn HttpOnly cookie; frontend không đọc/lưu refresh token.
    setAccessToken(data.accessToken);
    setCurrentUser(data.user);
    return data.user;
  }

  async function signOut() {
    try { await logoutRequest(); }
    finally { setAccessToken(null); setCurrentUser(null); }
  }

  useEffect(() => {
    async function restoreSession() {
      try {
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/auth/refresh-with-cookie`, {
          method: 'POST', credentials: 'include'
        });
        if (!response.ok) return;
        const result = await response.json();
        const data = result.data;
        if (!data?.accessToken) return;
        setAccessToken(data.accessToken);
        setCurrentUser(data.user);
      } catch (error) { console.error('Không thể khôi phục phiên đăng nhập:', error); }
      finally { setIsLoading(false); }
    }
    restoreSession();
  }, []);

  useEffect(() => {
    function clearExpiredSession() { setAccessToken(null); setCurrentUser(null); }
    window.addEventListener('auth:expired', clearExpiredSession);
    return () => window.removeEventListener('auth:expired', clearExpiredSession);
  }, []);

  useEffect(() => {
    // getAccount là nguồn xác nhận trạng thái account sau khi đã có token.
    if (!currentUser || isLoading) return;
    getAccount().then((response) => setCurrentUser(response.data.data)).catch(() => {});
  }, [isLoading]);

  return <AuthContext.Provider value={{ currentUser, isLoading, signIn, signOut }}>{children}</AuthContext.Provider>;
}
export function useAuth() { return useContext(AuthContext); }
