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
function EditModal({ title, onClose, children }) {
  useEffect(() => {
    function closeOnEscape(event) {
      if (event.key === "Escape") onClose();
    }
    window.addEventListener("keydown", closeOnEscape);
    return () => window.removeEventListener("keydown", closeOnEscape);
  }, [onClose]);
  return (
    <div className="modal-backdrop" onMouseDown={(event) => {
      if (event.target === event.currentTarget) onClose();
    }}>
      <section className="edit-modal" role="dialog" aria-modal="true" aria-label={title}>
        <div className="edit-modal-heading">
          <h2>{title}</h2>
          <button type="button" className="modal-close" onClick={onClose} aria-label="Đóng">×</button>
        </div>
        {children}
      </section>
    </div>
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
  const [tagSearch, setTagSearch] = useState("");
  const [activeSearch, setActiveSearch] = useState({});
  const [editing, setEditing] = useState(null);
  function searchTags(e) {
    e.preventDefault();
    const filters = tagSearch.trim() ? { name: tagSearch.trim() } : {};
    setActiveSearch(filters);
    list.load(1, filters);
  }
  async function submit(e) {
    e.preventDefault();
    try {
      if (editing) await tagApi.updateTag(editing, { name });
      else await tagApi.createTag({ name });
      setName("");
      setEditing(null);
      list.load(1, activeSearch);
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
      list.load(list.page, activeSearch);
    } catch (e) {
      list.setError(getErrorMessage(e));
    }
  }
  return (
    <>
      <PageHeading eyebrow="QUẢN LÝ" title="Chủ đề" />
      <form className="filter-form" onSubmit={searchTags}>
        <input
          placeholder="Tìm theo tên tag"
          value={tagSearch}
          onChange={(e) => setTagSearch(e.target.value)}
        />
        <button className="primary-button">Tìm tag ↗</button>
      </form>
      {!editing && <form className="inline-form" onSubmit={submit}>
        <input
          required
          placeholder="Tên chủ đề"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <button className="primary-button">
          {editing ? "Cập nhật" : "Thêm chủ đề"} ↗
        </button>
      </form>}
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
        onChange={(nextPage) => list.load(nextPage, activeSearch)}
      />
      {editing && (
        <EditModal title="Chỉnh sửa chủ đề" onClose={() => { setEditing(null); setName(""); }}>
          <form className="modal-form" onSubmit={submit}>
            <label>Tên chủ đề<input required autoFocus value={name} onChange={(e) => setName(e.target.value)} /></label>
            <ErrorMessage>{list.error}</ErrorMessage>
            <div className="modal-actions">
              <button type="button" onClick={() => { setEditing(null); setName(""); }}>Hủy</button>
              <button className="primary-button">Lưu thay đổi</button>
            </div>
          </form>
        </EditModal>
      )}
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
  const [activeFilters, setActiveFilters] = useState({});
  const list = useAdminList(userApi.getUsers);
  const [editingUser, setEditingUser] = useState(null);
  const [userForm, setUserForm] = useState({ name: "", email: "", address: "" });
  async function saveUser(e) {
    e.preventDefault();
    try {
      await userApi.updateUser(editingUser.id, {
        id: editingUser.id,
        ...userForm,
        role: editingUser.role,
      });
      setEditingUser(null);
      list.load(list.page, activeFilters);
    } catch (error) {
      list.setError(getErrorMessage(error));
    }
  }
  async function apply(e) {
    e.preventDefault();
    const params = {};
    Object.entries(filter).forEach(([key, value]) => {
      if (value.trim()) params[key] = value.trim();
    });
    setActiveFilters(params);
    list.load(1, params);
  }
  function clearFilters() {
    setFilter({ name: "", email: "", address: "", role: "" });
    setActiveFilters({});
    list.load(1, {});
  }
  async function remove(user) {
    if (!confirm(`Xóa tài khoản ${user.email}?`)) return;
    try {
      await userApi.deleteUser(user.id);
      list.load(list.page, activeFilters);
    } catch (e) {
      list.setError(getErrorMessage(e));
    }
  }
  return (
    <>
      <PageHeading eyebrow="QUẢN LÝ" title="Người dùng" />
      <form className="user-filter" onSubmit={apply}>
        <div className="user-filter-field">
          <label htmlFor="user-filter-name">TÊN</label>
          <input
            id="user-filter-name"
            type="text"
            placeholder="Tìm theo tên..."
            value={filter.name}
            onChange={(e) => setFilter({ ...filter, name: e.target.value })}
          />
        </div>
        <div className="user-filter-field">
          <label htmlFor="user-filter-email">EMAIL</label>
          <input
            id="user-filter-email"
            type="text"
            placeholder="Tìm theo email..."
            value={filter.email}
            onChange={(e) => setFilter({ ...filter, email: e.target.value })}
          />
        </div>
        <div className="user-filter-field">
          <label htmlFor="user-filter-address">ĐỊA CHỈ</label>
          <input
            id="user-filter-address"
            type="text"
            placeholder="Tìm theo địa chỉ..."
            value={filter.address}
            onChange={(e) => setFilter({ ...filter, address: e.target.value })}
          />
        </div>
        <div className="user-filter-field">
          <label htmlFor="user-filter-role">VAI TRÒ</label>
          <select
            id="user-filter-role"
            value={filter.role}
            onChange={(e) => setFilter({ ...filter, role: e.target.value })}
          >
            <option value="">Tất cả</option>
            <option value="admin">Admin</option>
            <option value="user">User</option>
          </select>
        </div>
        <div className="user-filter-actions">
          <button className="user-filter-clear" type="button" onClick={clearFilters}>
            Xóa lọc
          </button>
          <button className="primary-button" type="submit">Lọc kết quả →</button>
        </div>
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
                  setEditingUser(user);
                  setUserForm({ name: user.name || "", email: user.email || "", address: user.address || "" });
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
        onChange={(nextPage) => list.load(nextPage, activeFilters)}
      />
      {editingUser && (
        <EditModal title="Chỉnh sửa người dùng" onClose={() => setEditingUser(null)}>
          <form className="modal-form" onSubmit={saveUser}>
            <label>Tên<input required autoFocus value={userForm.name} onChange={(e) => setUserForm({ ...userForm, name: e.target.value })} /></label>
            <label>Email<input type="email" required value={userForm.email} onChange={(e) => setUserForm({ ...userForm, email: e.target.value })} /></label>
            <label>Địa chỉ<input value={userForm.address} onChange={(e) => setUserForm({ ...userForm, address: e.target.value })} /></label>
            <ErrorMessage>{list.error}</ErrorMessage>
            <div className="modal-actions"><button type="button" onClick={() => setEditingUser(null)}>Hủy</button><button className="primary-button">Lưu thay đổi</button></div>
          </form>
        </EditModal>
      )}
    </>
  );
}
function RolesAdmin() {
  const list = useAdminList(roleApi.getRoles);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [editingRole, setEditingRole] = useState(null);
  const [roleForm, setRoleForm] = useState({ name: "", description: "" });
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
  async function saveRole(e) {
    e.preventDefault();
    try {
      await roleApi.updateRole(editingRole.id, { id: editingRole.id, ...roleForm });
      setEditingRole(null);
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
            <div className="row-actions">
              <button onClick={() => { setEditingRole(role); setRoleForm({ name: role.name || "", description: role.description || "" }); }}>Sửa</button>
              <button className="danger-button" onClick={() => remove(role.id)}>Xóa</button>
            </div>
          </div>
        ))
      )}
      {editingRole && (
        <EditModal title="Chỉnh sửa vai trò" onClose={() => setEditingRole(null)}>
          <form className="modal-form" onSubmit={saveRole}>
            <label>Tên vai trò<input required autoFocus value={roleForm.name} onChange={(e) => setRoleForm({ ...roleForm, name: e.target.value })} /></label>
            <label>Mô tả<input required value={roleForm.description} onChange={(e) => setRoleForm({ ...roleForm, description: e.target.value })} /></label>
            <ErrorMessage>{list.error}</ErrorMessage>
            <div className="modal-actions"><button type="button" onClick={() => setEditingRole(null)}>Hủy</button><button className="primary-button">Lưu thay đổi</button></div>
          </form>
        </EditModal>
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
