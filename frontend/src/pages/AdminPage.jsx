import React, { useEffect, useState } from "react";
import { Link, NavLink, Route, Routes } from "react-router-dom";
import * as userApi from "../api/userApi.js";
import * as roleApi from "../api/roleApi.js";
import * as tagApi from "../api/tagApi.js";
import * as postApi from "../api/postApi.js";
import * as commentApi from "../api/commentApi.js";
import { getErrorMessage } from "../api/axiosClient.js";
import {
  ErrorMessage,
  Loading,
  PageHeading,
  Pagination,
} from "../components/Common.jsx";

function useAdminList(loadFunction, extraParams = {}) {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  async function load(nextPage = page, params = extraParams) {
    try {
      setLoading(true);
      setError("");
      const response = await loadFunction({
        page: nextPage,
        size: 10,
        ...params,
      });
      const data = response.data.data;
      setItems(data.content || []);
      setPage(data.page || nextPage);
      setTotalPages(data.totalPage || 0);
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load(1);
  }, []);
  return { items, setItems, page, totalPages, loading, error, setError, load };
}
function AdminShell({ children }) {
  return (
    <section className="section page-section">
      <div className="admin-layout">
        <aside className="admin-sidebar">
          <span className="eyebrow">WORKSPACE</span>
          <h3>Quản trị</h3>
          <NavLink end to="/admin">
            Tổng quan
          </NavLink>
          <NavLink to="/admin/posts">Bài viết</NavLink>
          <NavLink to="/admin/tags">Chủ đề</NavLink>
          <NavLink to="/admin/comments">Bình luận</NavLink>
          <NavLink to="/admin/users">Người dùng</NavLink>
          <NavLink to="/admin/roles">Vai trò</NavLink>
        </aside>
        <div className="admin-content">{children}</div>
      </div>
    </section>
  );
}
function Dashboard() {
  return (
    <>
      <PageHeading eyebrow="TỔNG QUAN" title="Quản trị nội dung">
        Chọn một khu vực ở thanh bên để xem và quản lý dữ liệu.
      </PageHeading>
      <div className="admin-cards">
        {[
          ["Bài viết", "/admin/posts"],
          ["Chủ đề", "/admin/tags"],
          ["Bình luận", "/admin/comments"],
          ["Người dùng", "/admin/users"],
          ["Vai trò", "/admin/roles"],
        ].map(([name, path]) => (
          <Link key={name} to={path} className="admin-card">
            <span>QUẢN LÝ</span>
            <b>{name}</b>
            <i>↗</i>
          </Link>
        ))}
      </div>
    </>
  );
}
function PostsAdmin() {
  const list = useAdminList(postApi.getPosts);
  const [filter, setFilter] = useState("");
  async function remove(id) {
    if (!confirm("Xóa bài viết này?")) return;
    try {
      await postApi.deletePost(id);
      list.load(list.page);
    } catch (e) {
      list.setError(getErrorMessage(e));
    }
  }
  return (
    <>
      <PageHeading eyebrow="QUẢN LÝ" title="Bài viết">
        <Link className="primary-button" to="/posts/new">
          + Viết bài
        </Link>
      </PageHeading>
      <form
        className="filter-form"
        onSubmit={(e) => {
          e.preventDefault();
          list.load(1, { title: filter });
        }}
      >
        <input
          placeholder="Tìm tiêu đề"
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
        />
        <button className="primary-button">Tìm ↗</button>
      </form>
      <ErrorMessage>{list.error}</ErrorMessage>
      {list.loading ? (
        <Loading />
      ) : (
        list.items.map((post) => (
          <div className="admin-row" key={post.id}>
            <div>
              <Link to={`/posts/${post.id}`}>
                <b>{post.title}</b>
              </Link>
              <p>
                {post.createAt
                  ? new Date(post.createAt).toLocaleDateString("vi-VN")
                  : ""}{" "}
                · {(post.tag || []).map((t) => t.name).join(", ")}
              </p>
            </div>
            <div className="row-actions">
              <Link to={`/posts/${post.id}/edit`}>Sửa</Link>
              <button className="danger-button" onClick={() => remove(post.id)}>
                Xóa
              </button>
            </div>
          </div>
        ))
      )}
      <Pagination
        page={list.page}
        totalPages={list.totalPages}
        onChange={list.load}
      />
    </>
  );
}
function TagsAdmin() {
  const list = useAdminList(tagApi.getTags);
  const [name, setName] = useState("");
  const [editing, setEditing] = useState(null);
  async function submit(e) {
    e.preventDefault();
    try {
      if (editing) await tagApi.updateTag(editing, { name });
      else await tagApi.createTag({ name });
      setName("");
      setEditing(null);
      list.load(1);
    } catch (e) {
      list.setError(getErrorMessage(e));
    }
  }
  async function remove(tag) {
    try {
      const response = await tagApi.deleteTag(tag.id);
      const posts = response.data.data || [];
      if (
        posts.length &&
        !confirm(
          `Tag đang được dùng ở ${posts.length} bài. Xác nhận xóa và gỡ khỏi các bài?`,
        )
      )
        return;
      await tagApi.confirmDeleteTag(tag.id);
      list.load(list.page);
    } catch (e) {
      list.setError(getErrorMessage(e));
    }
  }
  return (
    <>
      <PageHeading eyebrow="QUẢN LÝ" title="Chủ đề" />
      <form className="inline-form" onSubmit={submit}>
        <input
          required
          placeholder="Tên chủ đề"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <button className="primary-button">
          {editing ? "Cập nhật" : "Thêm chủ đề"} ↗
        </button>
        {editing && (
          <button
            type="button"
            onClick={() => {
              setEditing(null);
              setName("");
            }}
          >
            Hủy
          </button>
        )}
      </form>
      <ErrorMessage>{list.error}</ErrorMessage>
      {list.loading ? (
        <Loading />
      ) : (
        list.items.map((tag) => (
          <div className="admin-row" key={tag.id}>
            <div>
              <b>#{tag.name}</b>
              <p>{tag.posts?.length || 0} bài viết</p>
            </div>
            <div className="row-actions">
              <button
                onClick={() => {
                  setEditing(tag.id);
                  setName(tag.name);
                }}
              >
                Sửa
              </button>
              <button className="danger-button" onClick={() => remove(tag)}>
                Xóa
              </button>
            </div>
          </div>
        ))
      )}
      <Pagination
        page={list.page}
        totalPages={list.totalPages}
        onChange={list.load}
      />
    </>
  );
}
function CommentsAdmin() {
  const list = useAdminList(commentApi.getComments);
  const [approved, setApproved] = useState("");
  async function applyFilter(e) {
    e.preventDefault();
    list.load(1, approved === "" ? {} : { isApproved: approved });
  }
  async function toggle(comment) {
    try {
      await commentApi.approveComment(comment.id, !comment.approved);
      list.load(list.page, approved === "" ? {} : { isApproved: approved });
    } catch (e) {
      list.setError(getErrorMessage(e));
    }
  }
  async function remove(id) {
    try {
      await commentApi.deleteComment(id);
      list.load(list.page);
    } catch (e) {
      list.setError(getErrorMessage(e));
    }
  }
  return (
    <>
      <PageHeading eyebrow="QUẢN LÝ" title="Bình luận" />
      <form className="filter-form" onSubmit={applyFilter}>
        <select value={approved} onChange={(e) => setApproved(e.target.value)}>
          <option value="">Tất cả</option>
          <option value="true">Đã duyệt</option>
          <option value="false">Chờ duyệt</option>
        </select>
        <button className="primary-button">Lọc ↗</button>
      </form>
      <ErrorMessage>{list.error}</ErrorMessage>
      {list.loading ? (
        <Loading />
      ) : (
        list.items.map((c) => (
          <div className="admin-row" key={c.id}>
            <div>
              <b>
                {c.user?.name || c.user?.email} · {c.post?.title}
              </b>
              <p>{c.content}</p>
              <span
                className={c.approved ? "status-approved" : "status-pending"}
              >
                {c.approved ? "Đã duyệt" : "Chờ duyệt"}
              </span>
            </div>
            <div className="row-actions">
              <button onClick={() => toggle(c)}>
                {c.approved ? "Ẩn" : "Duyệt"}
              </button>
              <button className="danger-button" onClick={() => remove(c.id)}>
                Xóa
              </button>
            </div>
          </div>
        ))
      )}
      <Pagination
        page={list.page}
        totalPages={list.totalPages}
        onChange={list.load}
      />
    </>
  );
}
function UsersAdmin() {
  const [filter, setFilter] = useState({
    name: "",
    email: "",
    address: "",
    role: "",
  });
  const list = useAdminList(userApi.getUsers);
  async function apply(e) {
    e.preventDefault();
    const params = {};
    Object.entries(filter).forEach(([key, value]) => {
      if (value.trim()) params[key] = value.trim();
    });
    list.load(1, params);
  }
  async function remove(user) {
    if (!confirm(`Xóa tài khoản ${user.email}?`)) return;
    try {
      await userApi.deleteUser(user.id);
      list.load(list.page);
    } catch (e) {
      list.setError(getErrorMessage(e));
    }
  }
  return (
    <>
      <PageHeading eyebrow="QUẢN LÝ" title="Người dùng" />
      <form className="filter-form filter-multi" onSubmit={apply}>
        {Object.keys(filter).map((key) => (
          <input
            key={key}
            placeholder={
              key === "role"
                ? "Vai trò"
                : key === "name"
                  ? "Tên"
                  : key === "email"
                    ? "Email"
                    : "Địa chỉ"
            }
            value={filter[key]}
            onChange={(e) => setFilter({ ...filter, [key]: e.target.value })}
          />
        ))}
        <button className="primary-button">Lọc ↗</button>
      </form>
      <ErrorMessage>{list.error}</ErrorMessage>
      {list.loading ? (
        <Loading />
      ) : (
        list.items.map((user) => (
          <div className="admin-row" key={user.id}>
            <div>
              <b>{user.name}</b>
              <p>
                {user.email} · {user.address} · {user.role?.name}
              </p>
            </div>
            <div className="row-actions">
              <button
                onClick={() => {
                  const name = prompt("Tên", user.name);
                  if (name === null) return;
                  const address = prompt("Địa chỉ", user.address);
                  if (address === null) return;
                  userApi
                    .updateUser(user.id, {
                      id: user.id,
                      name,
                      email: user.email,
                      address,
                      role: user.role,
                    })
                    .then(() => list.load(list.page))
                    .catch((e) => list.setError(getErrorMessage(e)));
                }}
              >
                Sửa
              </button>
              <button className="danger-button" onClick={() => remove(user)}>
                Xóa
              </button>
            </div>
          </div>
        ))
      )}
      <Pagination
        page={list.page}
        totalPages={list.totalPages}
        onChange={list.load}
      />
    </>
  );
}
function RolesAdmin() {
  const list = useAdminList(roleApi.getRoles);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  async function submit(e) {
    e.preventDefault();
    try {
      await roleApi.createRole({ id: 0, name, description });
      setName("");
      setDescription("");
      list.load(1);
    } catch (e) {
      list.setError(getErrorMessage(e));
    }
  }
  async function remove(id) {
    if (!confirm("Xóa role này?")) return;
    try {
      await roleApi.deleteRole(id);
      list.load(list.page);
    } catch (e) {
      list.setError(getErrorMessage(e));
    }
  }
  return (
    <>
      <PageHeading eyebrow="QUẢN LÝ" title="Vai trò" />
      <form className="inline-form" onSubmit={submit}>
        <input
          required
          placeholder="Tên role"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input
          required
          placeholder="Mô tả"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <button className="primary-button">Thêm ↗</button>
      </form>
      <ErrorMessage>{list.error}</ErrorMessage>
      {list.loading ? (
        <Loading />
      ) : (
        list.items.map((role) => (
          <div className="admin-row" key={role.id}>
            <div>
              <b>{role.name}</b>
              <p>ID {role.id}</p>
            </div>
            <button className="danger-button" onClick={() => remove(role.id)}>
              Xóa
            </button>
          </div>
        ))
      )}
    </>
  );
}
export function AdminPage() {
  return (
    <AdminShell>
      <Routes>
        <Route index element={<Dashboard />} />
        <Route path="posts" element={<PostsAdmin />} />
        <Route path="tags" element={<TagsAdmin />} />
        <Route path="comments" element={<CommentsAdmin />} />
        <Route path="users" element={<UsersAdmin />} />
        <Route path="roles" element={<RolesAdmin />} />
      </Routes>
    </AdminShell>
  );
}
