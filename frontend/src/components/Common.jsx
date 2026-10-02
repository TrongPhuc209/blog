import React from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
export function Loading({ text = 'Đang tải...' }) { return <p className="notice">{text}</p>; }
export function ErrorMessage({ children }) { return children ? <p className="error" role="alert">{children}</p> : null; }
export function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages < 2) return null;
  return <div className="pagination"><button disabled={page <= 1} onClick={() => onChange(page - 1)}>← Trước</button><span>Trang {page} / {totalPages}</span><button disabled={page >= totalPages} onClick={() => onChange(page + 1)}>Sau →</button></div>;
}
export function ProtectedRoute({ children, admin = false }) {
  const { currentUser, isLoading } = useAuth();
  const location = useLocation();
  if (isLoading) return <Loading />;
  if (!currentUser) return <Navigate to="/login" state={{ from: location }} replace />;
  if (admin && currentUser.role !== 'ROLE_ADMIN') return <Navigate to="/" replace />;
  return children;
}
export function SiteHeader() {
  const { currentUser, signOut } = useAuth();
  return <header className="topbar"><Link className="brand" to="/">chuyện<span>.</span></Link><nav><Link to="/posts">Bài viết</Link><Link to="/tags">Chủ đề</Link>{currentUser && <Link to="/posts/new">Viết bài</Link>}{currentUser?.role === 'ROLE_ADMIN' && <Link to="/admin">Quản trị</Link>}</nav><div className="account">{currentUser ? <><Link className="account-name" to="/account">{currentUser.username}</Link><button className="text-button" onClick={() => signOut()}>Đăng xuất</button></> : <><Link to="/login">Đăng nhập</Link><Link className="small-cta" to="/register">Tham gia</Link></>}</div></header>;
}
export function PageHeading({ eyebrow, title, children }) { return <div className="page-heading"><div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1>{children && <p>{children}</p>}</div></div>; }
