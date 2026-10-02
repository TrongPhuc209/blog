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
import { register } from "../api/authApi.js";
import { useAuth } from "../context/AuthContext.jsx";
import {
  ErrorMessage,
  Loading,
  PageHeading,
  Pagination,
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
        Cập nhật {formatDate(post.updateAt)}{" "}
        {currentUser && <Link to={`/posts/${id}/edit`}>Sửa bài viết ↗</Link>}
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
              <div className="comment-avatar">
                {comment.user?.name?.[0] || "B"}
              </div>
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
        .then((r) =>
          setForm({
            title: r.data.data.title,
            content: r.data.data.content,
            tagIds: (r.data.data.tag || []).map((t) => t.id),
          }),
        )
        .catch((e) => setError(getErrorMessage(e)));
  }, [id]);
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
          <div>
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
  const { currentUser } = useAuth();
  return (
    <section className="form-page auth-page">
      <span className="eyebrow">TÀI KHOẢN CỦA BẠN</span>
      <h1>Xin chào.</h1>
      <div className="account-card">
        <div>
          <small>EMAIL / TÊN ĐĂNG NHẬP</small>
          <strong>{currentUser.username}</strong>
        </div>
        <div>
          <small>ID TÀI KHOẢN</small>
          <strong>{currentUser.id}</strong>
        </div>
        <div>
          <small>VAI TRÒ</small>
          <strong>{currentUser.role}</strong>
        </div>
      </div>
      <div className="account-links">
        <Link to="/my-comments">Xem bình luận của tôi ↗</Link>
        <Link to="/posts/new">Viết bài mới ↗</Link>
      </div>
      <p className="muted">
        Backend hiện không cung cấp API sửa thông tin hồ sơ.
      </p>
    </section>
  );
}
