import React, { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import {
  createComment,
  deleteComment,
  getMyComments,
  getPostComments,
} from "../api/commentApi.js";
import { getErrorMessage } from "../api/axiosClient.js";
import { getPost, getPosts, createPost, updatePost } from "../api/postApi.js";
import { getTags } from "../api/tagApi.js";
import { changeAvatar, editProfile, getAccount, register } from "../api/authApi.js";
import { useAuth } from "../context/AuthContext.jsx";
import {
  ErrorMessage,
  Loading,
  PageHeading,
  Pagination,
  Avatar,
} from "../components/Common.jsx";

function formatDate(value) {
  return value
    ? new Date(value).toLocaleDateString("vi-VN", {
        day: "numeric",
        month: "long",
        year: "numeric",
      })
    : "Ngày chưa rõ";
}
function roleLabel(role) {
  const normalized = typeof role === "object" ? role?.name : role;
  if (["ROLE_ADMIN", "ADMIN"].includes(String(normalized).toUpperCase())) return "Quản trị viên";
  if (["ROLE_USER", "USER"].includes(String(normalized).toUpperCase())) return "Người dùng";
  return normalized || "Chưa xác định";
}

function AvatarCropDialog({ file, onCancel, onConfirm, saving }) {
  const viewportSize = 240;
  const [previewUrl, setPreviewUrl] = useState("");
  const [imageSize, setImageSize] = useState(null);
  const [zoom, setZoom] = useState(1);
  const [offset, setOffset] = useState({ x: 0, y: 0 });
  const drag = React.useRef(null);

  useEffect(() => {
    if (!file) return;
    const url = URL.createObjectURL(file);
    setPreviewUrl(url);
    setImageSize(null);
    setZoom(1);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  const baseScale = imageSize
    ? Math.max(viewportSize / imageSize.width, viewportSize / imageSize.height)
    : 1;
  const scale = baseScale * zoom;
  const renderedWidth = imageSize ? imageSize.width * scale : viewportSize;
  const renderedHeight = imageSize ? imageSize.height * scale : viewportSize;

  function clampOffset(x, y, nextScale = scale) {
    if (!imageSize) return { x, y };
    const width = imageSize.width * nextScale;
    const height = imageSize.height * nextScale;
    return {
      x: Math.min(0, Math.max(viewportSize - width, x)),
      y: Math.min(0, Math.max(viewportSize - height, y)),
    };
  }

  function changeZoom(value) {
    const nextZoom = Number(value);
    const centerX = viewportSize / 2;
    const centerY = viewportSize / 2;
    const ratio = nextZoom / zoom;
    setZoom(nextZoom);
    setOffset(clampOffset(centerX - (centerX - offset.x) * ratio, centerY - (centerY - offset.y) * ratio, baseScale * nextZoom));
  }

  function confirmCrop() {
    if (!imageSize) return;
    const cropWidth = Math.min(imageSize.width, Math.round(viewportSize / scale));
    const cropHeight = Math.min(imageSize.height, Math.round(viewportSize / scale));
    const cropSize = Math.min(cropWidth, cropHeight);
    const x = Math.max(0, Math.min(imageSize.width - cropSize, Math.round(-offset.x / scale)));
    const y = Math.max(0, Math.min(imageSize.height - cropSize, Math.round(-offset.y / scale)));
    onConfirm({ x, y, width: cropSize, height: cropSize });
  }

  return <div className="modal-backdrop" role="presentation">
    <section className="avatar-crop-dialog" role="dialog" aria-modal="true" aria-labelledby="avatar-crop-title">
      <div className="edit-modal-heading"><h2 id="avatar-crop-title">Cắt ảnh đại diện</h2><button className="modal-close" type="button" onClick={onCancel} aria-label="Đóng">×</button></div>
      <p className="muted">Kéo ảnh để chọn vùng vuông sẽ hiển thị làm avatar.</p>
      {previewUrl && <div className="avatar-crop-viewport" style={{ width: viewportSize, height: viewportSize }}
        onPointerDown={(event) => { if (!imageSize) return; event.currentTarget.setPointerCapture(event.pointerId); drag.current = { x: event.clientX, y: event.clientY, offsetX: offset.x, offsetY: offset.y }; }}
        onPointerMove={(event) => { if (!drag.current) return; setOffset(clampOffset(drag.current.offsetX + event.clientX - drag.current.x, drag.current.offsetY + event.clientY - drag.current.y)); }}
        onPointerUp={() => { drag.current = null; }} onPointerCancel={() => { drag.current = null; }}>
        <img src={previewUrl} alt="Xem trước ảnh đại diện" draggable="false"
          onLoad={(event) => { const image = { width: event.currentTarget.naturalWidth, height: event.currentTarget.naturalHeight }; setImageSize(image); const fit = Math.max(viewportSize / image.width, viewportSize / image.height); setOffset({ x: (viewportSize - image.width * fit) / 2, y: (viewportSize - image.height * fit) / 2 }); }}
          onError={() => setImageSize({ error: true })}
          style={{ width: renderedWidth, height: renderedHeight, left: offset.x, top: offset.y }} />
        <div className="avatar-crop-guide" />
      </div>}
      {imageSize?.error ? <ErrorMessage>Không thể mở ảnh này. Hãy thử một tệp PNG hoặc JPEG hợp lệ.</ErrorMessage> : null}
      {imageSize && !imageSize.error && <label className="crop-zoom">Phóng to <input type="range" min="1" max="3" step="0.01" value={zoom} onChange={(event) => changeZoom(event.target.value)} /></label>}
      <div className="modal-actions"><button type="button" onClick={onCancel} disabled={saving}>Hủy</button><button className="primary-button" type="button" onClick={confirmCrop} disabled={!imageSize || imageSize.error || saving}>{saving ? "Đang tải ảnh…" : "Cập nhật avatar"}</button></div>
    </section>
  </div>;
}

function PostCard({ post }) {
  const excerpt =
    post.content?.length > 190
      ? `${post.content.slice(0, 190)}…`
      : post.content;
  return (
    <article className="post-card">
      <div className="post-meta">
        BÀI VIẾT <span>·</span> {formatDate(post.createAt)}
      </div>
      <h2>
        <Link to={`/posts/${post.id}`}>{post.title}</Link>
      </h2>
      <p>{excerpt}</p>
      <div className="tag-row">
        {(post.tag || []).map((tag) => (
          <Link
            key={tag.id}
            className="tag"
            to={`/posts?tagName=${encodeURIComponent(tag.name)}`}
          >
            #{tag.name}
          </Link>
        ))}
      </div>
      <Link className="read-more" to={`/posts/${post.id}`}>
        Đọc bài viết <span>↗</span>
      </Link>
    </article>
  );
}
function usePostList(initialTag = "") {
  const [postList, setPostList] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [title, setTitle] = useState("");
  const [tagName, setTagName] = useState(initialTag);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  async function loadPosts(nextPage = page, filters = {}) {
    try {
      setLoading(true);
      setError("");
      const params = { page: nextPage, size: 6, sort: "createdAt,desc" };
      const selectedTitle = filters.title ?? title;
      const selectedTagName = filters.tagName ?? tagName;
      const selectedTags = selectedTagName
        .split(',')
        .map((name) => name.trim())
        .filter(Boolean);
      if (selectedTitle.trim()) params.title = selectedTitle.trim();
      if (selectedTags.length) params.tagName = selectedTags;
      const response = await getPosts(params);
      const data = response.data.data;
      setPostList(data.content || []);
      setPage(data.page || nextPage);
      setTotalPages(data.totalPage || 0);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    loadPosts(1);
  }, []);
  return {
    postList,
    page,
    totalPages,
    title,
    setTitle,
    tagName,
    setTagName,
    loading,
    error,
    loadPosts,
  };
}
export function HomePage() {
  const posts = usePostList();
  const [tags, setTags] = useState([]);
  useEffect(() => {
    getTags({ page: 1, size: 12 })
      .then((r) => setTags(r.data.data.content || []))
      .catch(() => {});
  }, []);
  return (
    <>
      <section className="hero">
        <div className="hero-copy">
          <span className="eyebrow">GÓC ĐỌC CỦA BẠN</span>
          <h1>
            Ý tưởng hay
            <br />
            bắt đầu từ <em>một trang viết.</em>
          </h1>
          <p>
            Khám phá những câu chuyện, kiến thức và góc nhìn mới từ cộng đồng.
          </p>
          <Link className="primary-button" to="/posts">
            Khám phá bài viết <span>↗</span>
          </Link>
        </div>
        <div className="hero-art">
          <div className="hero-stamp">
            ĐỌC
            <br />
            CHẬM
            <br />
            <span>✳</span>
          </div>
          <span className="art-note">
            mỗi ngày
            <br />
            một điều mới
          </span>
        </div>
        <div className="hero-foot">
          <span>ĐỌC CHẬM MỖI NGÀY</span>
          <span>NHỮNG CÂU CHUYỆN ĐÁNG GIỮ LẠI</span>
          <span>↓ CUỘN ĐỂ KHÁM PHÁ</span>
        </div>
      </section>
      <section className="section">
        <div className="section-top">
          <div>
            <span className="eyebrow">MỚI NHẤT</span>
            <h2>Chuyện vừa lên trang</h2>
          </div>
          <Link to="/posts">Tất cả bài viết ↗</Link>
        </div>
        {posts.loading ? (
          <Loading />
        ) : posts.error ? (
          <ErrorMessage>{posts.error}</ErrorMessage>
        ) : posts.postList.length ? (
          <div className="post-grid">
            {posts.postList.slice(0, 3).map((post) => (
              <PostCard key={post.id} post={post} />
            ))}
          </div>
        ) : (
          <div className="empty">Chưa có bài viết. Hãy quay lại sau nhé.</div>
        )}
      </section>
      <section className="topic-band">
        <div>
          <span className="eyebrow">ĐỌC THEO CHỦ ĐỀ</span>
          <h2>Mở một cánh cửa mới.</h2>
        </div>
        <div className="tag-row">
          {tags.map((tag) => (
            <Link
              key={tag.id}
              className="tag"
              to={`/posts?tagName=${encodeURIComponent(tag.name)}`}
            >
              #{tag.name}
            </Link>
          ))}
        </div>
      </section>
    </>
  );
}
export function PostListPage() {
  const location = useLocation();
  const initialTag = new URLSearchParams(location.search)
    .getAll("tagName")
    .join(", ");
  const posts = usePostList(initialTag);
  const currentTagQuery = posts.tagName.split(",").at(-1).trim();
  const [tagSuggestions, setTagSuggestions] = useState([]);
  const [isSuggestionListOpen, setIsSuggestionListOpen] = useState(false);
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);

  useEffect(() => {
    const searchText = currentTagQuery;
    if (!isSuggestionListOpen || searchText.length < 2) {
      setTagSuggestions([]);
      setIsLoadingSuggestions(false);
      return;
    }

    setIsLoadingSuggestions(true);
    let shouldUpdateState = true;
    const timeoutId = setTimeout(async () => {
      try {
        setIsLoadingSuggestions(true);
        const response = await getTags({ name: searchText, page: 1, size: 8 });
        if (shouldUpdateState) {
          setTagSuggestions(response.data.data.content || []);
        }
      } catch {
        if (shouldUpdateState) setTagSuggestions([]);
      } finally {
        if (shouldUpdateState) setIsLoadingSuggestions(false);
      }
    }, 250);

    return () => {
      shouldUpdateState = false;
      clearTimeout(timeoutId);
    };
  }, [currentTagQuery, isSuggestionListOpen]);

  function chooseTag(tag) {
    const tagParts = posts.tagName.split(",").map((name) => name.trim());
    tagParts[tagParts.length - 1] = tag.name;
    const nextTagValue = `${tagParts.filter(Boolean).join(", ")}, `;
    posts.setTagName(nextTagValue);
    setIsSuggestionListOpen(false);
    setTagSuggestions([]);
    posts.loadPosts(1, { tagName: nextTagValue });
  }

  return (
    <section className="section page-section">
      <PageHeading eyebrow="THƯ VIỆN" title="Tất cả bài viết">
        Tìm câu chuyện phù hợp với điều bạn đang tò mò.
      </PageHeading>
      <form
        className="filter-form"
        onSubmit={(e) => {
          e.preventDefault();
          posts.loadPosts(1);
        }}
      >
        <input
          placeholder="Tìm theo tiêu đề"
          value={posts.title}
          onChange={(e) => posts.setTitle(e.target.value)}
        />
        <div className="tag-filter-field">
          <input
            placeholder="Lọc theo tag, ngăn cách bằng dấu phẩy"
            value={posts.tagName}
            autoComplete="off"
            role="combobox"
            aria-autocomplete="list"
            aria-expanded={isSuggestionListOpen && tagSuggestions.length > 0}
            aria-controls="tag-suggestions"
            onFocus={() => setIsSuggestionListOpen(true)}
            onBlur={() => setTimeout(() => setIsSuggestionListOpen(false), 150)}
            onChange={(e) => {
              posts.setTagName(e.target.value);
              setIsSuggestionListOpen(true);
            }}
            onKeyDown={(e) => {
              if (e.key === "Escape") setIsSuggestionListOpen(false);
            }}
          />
          {isSuggestionListOpen && currentTagQuery.length >= 2 && (
            <div className="tag-suggestions" id="tag-suggestions" role="listbox">
              {isLoadingSuggestions ? (
                <p className="tag-suggestion-message">Đang tìm chủ đề…</p>
              ) : tagSuggestions.length ? (
                tagSuggestions.map((tag) => (
                  <button
                    className="tag-suggestion"
                    key={tag.id}
                    type="button"
                    role="option"
                    aria-selected="false"
                    onClick={() => chooseTag(tag)}
                  >
                    <span>#{tag.name}</span>
                    <small>Chọn chủ đề</small>
                  </button>
                ))
              ) : (
                <p className="tag-suggestion-message">Không tìm thấy chủ đề phù hợp.</p>
              )}
            </div>
          )}
        </div>
        <button className="primary-button">Tìm kiếm ↗</button>
      </form>
      <ErrorMessage>{posts.error}</ErrorMessage>
      {posts.loading ? (
        <Loading />
      ) : posts.postList.length ? (
        <div className="post-grid">
          {posts.postList.map((post) => (
            <PostCard key={post.id} post={post} />
          ))}
        </div>
      ) : (
        <div className="empty">Chưa có bài viết phù hợp.</div>
      )}
      <Pagination
        page={posts.page}
        totalPages={posts.totalPages}
        onChange={posts.loadPosts}
      />
    </section>
  );
}
export function PostDetailPage() {
  const { id } = useParams();
  const { currentUser } = useAuth();
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  async function load() {
    try {
      setLoading(true);
      const [postResponse, commentResponse] = await Promise.all([
        getPost(id),
        getPostComments(id, { page: 1, size: 50 }),
      ]);
      setPost(postResponse.data.data);
      setComments(commentResponse.data.data.content || []);
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, [id]);
  async function submitComment(e) {
    e.preventDefault();
    try {
      await createComment(id, { content, post: { id: Number(id) } });
      setContent("");
      setNotice("Bình luận đã gửi. Bình luận sẽ hiện sau khi được duyệt.");
    } catch (e) {
      setError(getErrorMessage(e));
    }
  }
  if (loading) return <Loading />;
  if (!post)
    return (
      <section className="section page-section">
        <ErrorMessage>{error || "Không tìm thấy bài viết."}</ErrorMessage>
      </section>
    );
  return (
    <article className="article-page">
      <Link className="back-link" to="/posts">
        ← Tất cả bài viết
      </Link>
      <span className="eyebrow">
        CHUYỆN ĐÁNG ĐỌC · {formatDate(post.createAt)}
      </span>
      <h1>{post.title}</h1>
      <div className="article-byline">
        {post.user?.name && <span className="article-author"><Avatar src={post.user.avatarUrl} name={post.user.name} size={32} /><span>{post.user.name}</span></span>}
        <span>Cập nhật {formatDate(post.updateAt)}</span>
        {currentUser && Number(currentUser.id) === Number(post.user?.id ?? post.userId) && (
          <Link to={`/posts/${id}/edit`}>Sửa bài viết ↗</Link>
        )}
      </div>
      <div className="tag-row">
        {(post.tag || []).map((tag) => (
          <Link
            key={tag.id}
            className="tag"
            to={`/posts?tagName=${encodeURIComponent(tag.name)}`}
          >
            #{tag.name}
          </Link>
        ))}
      </div>
      <div className="article-content">
        {post.content.split("\n").map((line, index) => (
          <p key={index}>{line}</p>
        ))}
      </div>
      <section className="comments">
        <h2>
          Bình luận <span>{comments.length}</span>
        </h2>
        {comments.length ? (
          comments.map((comment) => (
            <div className="comment" key={comment.id}>
              <Avatar className="comment-avatar" src={comment.user?.avatarUrl} name={comment.user?.name || comment.user?.email} />
              <div>
                <strong>
                  {comment.user?.name || comment.user?.email || "Bạn đọc"}
                </strong>
                <p>{comment.content}</p>
              </div>
            </div>
          ))
        ) : (
          <p className="muted">Chưa có bình luận được duyệt.</p>
        )}
        <ErrorMessage>{error}</ErrorMessage>
        {notice && <p className="success">{notice}</p>}
        {currentUser ? (
          <form onSubmit={submitComment} className="comment-form">
            <label htmlFor="comment">Để lại một suy nghĩ</label>
            <textarea
              id="comment"
              required
              value={content}
              onChange={(e) => setContent(e.target.value)}
              rows="4"
            />
            <button className="primary-button">Gửi bình luận ↗</button>
          </form>
        ) : (
          <p className="muted">
            <Link to="/login">Đăng nhập</Link> để viết bình luận.
          </p>
        )}
      </section>
    </article>
  );
}
export function PostFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const [form, setForm] = useState({ title: "", content: "", tagIds: [] });
  const [tags, setTags] = useState([]);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  useEffect(() => {
    getTags({ page: 1, size: 100 })
      .then((r) => setTags(r.data.data.content || []))
      .catch((e) => setError(getErrorMessage(e)));
    if (id)
      getPost(id)
        .then((r) => {
          const post = r.data.data;
          if (Number(post.user?.id ?? post.userId) !== Number(currentUser.id)) {
            navigate(`/posts/${id}`, { replace: true });
            return;
          }
          setForm({
            title: post.title,
            content: post.content,
            tagIds: (post.tag || []).map((tag) => tag.id),
          });
        })
        .catch((e) => setError(getErrorMessage(e)));
  }, [id, currentUser.id, navigate]);
  async function submit(e) {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      const body = {
        title: form.title,
        content: form.content,
        tag: form.tagIds.map((tagId) => {
          const tag = tags.find((item) => item.id === Number(tagId));
          return { id: Number(tagId), name: tag.name };
        }),
      };
      const response = id ? await updatePost(id, body) : await createPost(body);
      navigate(`/posts/${response.data.data.id}`);
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setSaving(false);
    }
  }
  return (
    <section className="form-page">
      <PageHeading
        eyebrow={id ? "CHỈNH SỬA" : "BẮT ĐẦU VIẾT"}
        title={id ? "Sửa bài viết" : "Viết một câu chuyện"}
      />
      <form className="editor-form" onSubmit={submit}>
        <label>
          Tiêu đề
          <input
            required
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />
        </label>
        <label>
          Nội dung
          <textarea
            required
            rows="14"
            value={form.content}
            onChange={(e) => setForm({ ...form, content: e.target.value })}
          />
        </label>
        <label>
          Chủ đề
          <select
            multiple
            value={form.tagIds}
            onChange={(e) =>
              setForm({
                ...form,
                tagIds: Array.from(e.target.selectedOptions, (o) => o.value),
              })
            }
          >
            {tags.map((tag) => (
              <option key={tag.id} value={tag.id}>
                {tag.name}
              </option>
            ))}
          </select>
          <small>Giữ Ctrl (Windows) hoặc ⌘ (Mac) để chọn nhiều chủ đề.</small>
        </label>
        <ErrorMessage>{error}</ErrorMessage>
        <button className="primary-button" disabled={saving}>
          {saving ? "Đang lưu…" : "Lưu bài viết ↗"}
        </button>
        {id && (
          <p className="muted">
            Backend chỉ cho phép tác giả sửa bài của mình. ID tài khoản hiện
            tại: {currentUser.id}
          </p>
        )}
      </form>
    </section>
  );
}
export function LoginPage() {
  const { signIn, currentUser } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  if (currentUser)
    return (
      <section className="form-page">
        <h1>Bạn đã đăng nhập</h1>
        <Link to="/">Về trang chủ</Link>
      </section>
    );
  async function submit(e) {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      await signIn(form);
      navigate(location.state?.from?.pathname || "/", { replace: true });
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }
  return (
    <section className="form-page auth-page">
      <span className="eyebrow">CHÀO MỪNG TRỞ LẠI</span>
      <h1>Đăng nhập</h1>
      <form className="editor-form" onSubmit={submit}>
        <label>
          Email
          <input
            required
            type="email"
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
          />
        </label>
        <label>
          Mật khẩu
          <input
            required
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
        </label>
        <ErrorMessage>{error}</ErrorMessage>
        <button className="primary-button" disabled={loading}>
          {loading ? "Đang đăng nhập…" : "Đăng nhập ↗"}
        </button>
      </form>
      <p>
        Chưa có tài khoản? <Link to="/register">Đăng ký</Link>
      </p>
    </section>
  );
}
export function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: "",
    password: "",
    name: "",
    address: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  async function submit(e) {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      await register(form);
      setSuccess("Tạo tài khoản thành công. Bạn có thể đăng nhập ngay.");
      setTimeout(() => navigate("/login"), 900);
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }
  return (
    <section className="form-page auth-page">
      <span className="eyebrow">GIA NHẬP CỘNG ĐỒNG</span>
      <h1>Tạo tài khoản</h1>
      <form className="editor-form" onSubmit={submit}>
        <label>
          Tên
          <input
            required
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />
        </label>
        <label>
          Email
          <input
            required
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
        </label>
        <label>
          Địa chỉ
          <input
            required
            value={form.address}
            onChange={(e) => setForm({ ...form, address: e.target.value })}
          />
        </label>
        <label>
          Mật khẩu
          <input
            required
            type="password"
            minLength="6"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
          <small>
            Tối thiểu 6 ký tự, gồm chữ thường, chữ hoa, số và ký tự đặc biệt.
          </small>
        </label>
        <ErrorMessage>{error}</ErrorMessage>
        {success && <p className="success">{success}</p>}
        <button className="primary-button" disabled={loading}>
          {loading ? "Đang tạo…" : "Đăng ký ↗"}
        </button>
      </form>
    </section>
  );
}
export function TagsPage() {
  const [tags, setTags] = useState([]);
  const [error, setError] = useState("");
  useEffect(() => {
    getTags({ page: 1, size: 100 })
      .then((r) => setTags(r.data.data.content || []))
      .catch((e) => setError(getErrorMessage(e)));
  }, []);
  return (
    <section className="section page-section">
      <PageHeading eyebrow="KHÁM PHÁ" title="Chủ đề">
        Những từ khóa giúp bạn tìm đúng điều mình quan tâm.
      </PageHeading>
      <ErrorMessage>{error}</ErrorMessage>
      <div className="topic-grid">
        {tags.map((tag) => (
          <Link
            className="topic-card"
            key={tag.id}
            to={`/posts?tagName=${encodeURIComponent(tag.name)}`}
          >
            <span>#{tag.name}</span>
            <small>{tag.posts?.length || 0} bài viết ↗</small>
          </Link>
        ))}
      </div>
      {!tags.length && !error && <p className="empty">Chưa có chủ đề.</p>}
    </section>
  );
}
export function MyCommentsPage() {
  const [comments, setComments] = useState([]);
  const [error, setError] = useState("");
  useEffect(() => {
    getMyComments({ page: 1, size: 50 })
      .then((r) => setComments(r.data.data.content || []))
      .catch((e) => setError(getErrorMessage(e)));
  }, []);
  async function remove(id) {
    if (!confirm("Xóa bình luận này?")) return;
    try {
      await deleteComment(id);
      setComments(comments.filter((c) => c.id !== id));
    } catch (e) {
      setError(getErrorMessage(e));
    }
  }
  return (
    <section className="section page-section">
      <PageHeading eyebrow="TÀI KHOẢN" title="Bình luận của tôi" />
      <ErrorMessage>{error}</ErrorMessage>
      {comments.map((c) => (
        <div className="admin-row" key={c.id}>
          <div className="my-comment-content">
            <div className="my-comment-author"><Avatar src={c.user?.avatarUrl} name={c.user?.name || c.user?.email} size={30} /><span>{c.user?.name || c.user?.email || "Bạn"}</span></div>
            <b>{c.post?.title}</b>
            <p>{c.content}</p>
          </div>
          <button className="danger-button" onClick={() => remove(c.id)}>
            Xóa
          </button>
        </div>
      ))}
      {!comments.length && !error && (
        <p className="empty">Bạn chưa có bình luận nào.</p>
      )}
    </section>
  );
}
export function AccountPage() {
  const { currentUser, refreshCurrentUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ name: "", address: "" });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [avatarSaving, setAvatarSaving] = useState(false);
  const [avatarFile, setAvatarFile] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  useEffect(() => {
    if (!currentUser?.id) return;
    getAccount().then((response) => {
      const data = response.data.data;
      setProfile(data);
      setForm({ name: data.name || currentUser.name || "", address: data.address || currentUser.address || "" });
    }).catch((e) => setError(getErrorMessage(e))).finally(() => setLoading(false));
  }, [currentUser?.id]);

  async function saveProfile(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setSuccess("");
    try {
      await editProfile(form);
      setProfile((previous) => ({ ...previous, ...form }));
      setSuccess("Thông tin hồ sơ đã được cập nhật.");
      try {
        const refreshed = (await getAccount()).data.data;
        setProfile((previous) => ({ ...previous, ...Object.fromEntries(Object.entries(refreshed).filter(([, value]) => value != null)) }));
        await refreshCurrentUser();
      } catch {
        // Keep the successfully saved values visible if refreshing the account response fails.
      }
    } catch (e) { setError(getErrorMessage(e)); }
    finally { setSaving(false); }
  }

  function chooseAvatar(event) {
    const image = event.target.files?.[0];
    event.target.value = "";
    if (!image) return;
    setError("");
    setSuccess("");
    if (!["image/png", "image/jpeg"].includes(image.type)) {
      setError("Chỉ chấp nhận ảnh PNG hoặc JPEG.");
      return;
    }
    if (image.size > 5 * 1024 * 1024) {
      setError("Kích thước ảnh tối đa là 5 MB.");
      return;
    }
    setAvatarFile(image);
  }

  async function uploadAvatar(crop) {
    if (!avatarFile) return;
    setError("");
    setSuccess("");
    setAvatarSaving(true);
    try {
      const body = new FormData();
      body.append("image", avatarFile);
      body.append("x", String(crop.x));
      body.append("y", String(crop.y));
      body.append("width", String(crop.width));
      body.append("height", String(crop.height));
      await changeAvatar(body);
      setAvatarFile(null);
      setSuccess("Ảnh đại diện đã được cập nhật.");
      try {
        const refreshed = await refreshCurrentUser();
        setProfile((previous) => ({ ...previous, ...refreshed }));
      } catch {
        // Upload succeeded; a profile refresh failure must not present it as an upload failure.
      }
    } catch (e) {
      setError(e.response ? getErrorMessage(e) : `Không thể cập nhật ảnh: ${e.message || "lỗi kết nối"}`);
    } finally { setAvatarSaving(false); }
  }

  return (
    <section className="form-page auth-page">
      <span className="eyebrow">TÀI KHOẢN CỦA BẠN</span>
      <h1>Hồ sơ cá nhân</h1>
      {loading ? <Loading /> : <>
      <div className="profile-header">
        <Avatar className="profile-avatar" src={profile?.avatarUrl || currentUser.avatarUrl} name={profile?.name || currentUser.name || currentUser.username} size={92} />
        <div className="profile-header-details"><strong>{profile?.name || currentUser.name || currentUser.username}</strong><span>{profile?.email || currentUser.username}</span>
          <span className="profile-header-address">{profile?.address || currentUser.address || "Chưa cập nhật địa chỉ"}</span>
          <small className="profile-account-id">ID tài khoản: {currentUser.id}</small>
          <label className="avatar-upload">Chọn ảnh<input type="file" accept="image/png,image/jpeg" onChange={chooseAvatar} disabled={avatarSaving} /></label>
          <small>Ảnh PNG/JPEG, tối đa 5 MB. Bạn có thể kéo và phóng to ảnh để chọn vùng cắt.</small>
        </div>
      </div>
      <form className="editor-form profile-form" onSubmit={saveProfile}>
        <label>Tên hiển thị<input required maxLength={255} value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
        <label>Địa chỉ<input required maxLength={255} value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} /></label>
        <label>Email<input value={profile?.email || currentUser.username} disabled /></label>
        <ErrorMessage>{error}</ErrorMessage>{success && <p className="success">{success}</p>}
        <button className="primary-button" disabled={saving}>{saving ? "Đang lưu…" : "Lưu hồ sơ ↗"}</button>
      </form>
      <div className="account-card">
        <div>
          <small>VAI TRÒ</small>
          <strong>{roleLabel(currentUser.role)}</strong>
        </div>
      </div>
      <div className="account-links">
        <Link to="/my-comments">Xem bình luận của tôi ↗</Link>
        <Link to="/posts/new">Viết bài mới ↗</Link>
      </div>
      </>}
      {avatarFile && <AvatarCropDialog file={avatarFile} onCancel={() => setAvatarFile(null)} onConfirm={uploadAvatar} saving={avatarSaving} />}
    </section>
  );
}
