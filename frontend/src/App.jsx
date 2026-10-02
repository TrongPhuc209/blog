import React from 'react';
import { Link, Route, Routes } from 'react-router-dom';
import { SiteHeader, ProtectedRoute } from './components/Common.jsx';
import { HomePage, PostListPage, PostDetailPage, PostFormPage, LoginPage, RegisterPage, TagsPage, MyCommentsPage, AccountPage } from './pages/PublicPages.jsx';
import { AdminPage } from './pages/AdminPage.jsx';

export default function App() {
  return <><SiteHeader /><main><Routes>
    <Route path="/" element={<HomePage />} /><Route path="/posts" element={<PostListPage />} />
    <Route path="/posts/:id" element={<PostDetailPage />} /><Route path="/tags" element={<TagsPage />} />
    <Route path="/login" element={<LoginPage />} /><Route path="/register" element={<RegisterPage />} />
    <Route path="/posts/new" element={<ProtectedRoute><PostFormPage /></ProtectedRoute>} />
    <Route path="/posts/:id/edit" element={<ProtectedRoute><PostFormPage /></ProtectedRoute>} />
    <Route path="/my-comments" element={<ProtectedRoute><MyCommentsPage /></ProtectedRoute>} />
    <Route path="/account" element={<ProtectedRoute><AccountPage /></ProtectedRoute>} />
    <Route path="/admin/*" element={<ProtectedRoute admin><AdminPage /></ProtectedRoute>} />
    <Route path="*" element={<section className="empty"><h1>Không tìm thấy trang</h1><Link to="/">Về trang chủ</Link></section>} />
  </Routes></main><footer className="footer"><Link className="brand" to="/">chuyện<span>.</span></Link><span>Một góc nhỏ cho những điều đáng đọc.</span></footer></>;
}
