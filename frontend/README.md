# Chuyện Blog — React frontend

Frontend React + Vite bằng JavaScript thuần, dùng React Router, Axios và CSS. Các request gọi backend Spring Boot thật; không có dữ liệu giả.

## Chạy ứng dụng

1. Cài Node.js bản LTS hiện hành từ [nodejs.org](https://nodejs.org/).
2. Tại thư mục này, cài dependencies:

   ```bash
   npm install
   ```

3. Sao chép .env.example thành .env và chỉnh VITE_API_BASE_URL nếu backend không chạy tại http://localhost:8080.
4. Khởi động Spring Boot backend. CORS backend hiện cho phép http://localhost:5173.
5. Chạy frontend bằng npm run dev.
6. Mở địa chỉ Vite in ra terminal (mặc định http://localhost:5173).

Backend dùng MySQL theo cấu hình Spring Boot. Cookie refresh được đánh dấu Secure, nên môi trường triển khai cần HTTPS; browser thường cho phép Secure cookie trên localhost.

## Đăng nhập và phiên làm việc

Login gửi { username, password }. Access token được giữ trong bộ nhớ tab và tự gắn vào Authorization: Bearer. Refresh token backend gửi bằng cookie HttpOnly; JavaScript không đọc cookie này. Khi tải lại trang hoặc access token hết hạn, app gọi /auth/refresh-with-cookie để lấy access token mới. Logout gọi /auth/logout, sau đó xóa token/state phía client.

Backend cũng trả refresh token trong JSON response dù đồng thời đặt cookie. Frontend bỏ qua giá trị trong body để không lưu refresh token vào JavaScript storage. Cookie có Secure và backend bật CORS credentials.

## Các trang

- / trang chủ và bài mới nhất
- /posts danh sách, tìm theo tiêu đề và nhiều tag (nhập các tag cách nhau bằng dấu phẩy; gợi ý dựa trên API backend); /posts/:id chi tiết bài và bình luận đã duyệt
- /tags danh sách chủ đề
- /login, /register
- /account thông tin tài khoản trả về từ API đăng nhập/account
- /posts/new, /posts/:id/edit tạo/sửa bài khi đã đăng nhập
- /my-comments bình luận của người dùng hiện tại
- /admin dashboard; quản lý bài viết, tag, bình luận, users, roles

## Cấu trúc

```text
src/
  api/          Axios client và các hàm theo controller
  components/   Header, loading, errors, pagination, route guard
  context/      AuthContext
  pages/        Trang blog, auth và quản trị
  App.jsx
  main.jsx
  styles.css
docs/API-INVENTORY.md
```

Xem docs/API-INVENTORY.md để biết controller, request/response, quyền và API chưa được UI sử dụng.

## Đối chiếu API và checklist

- [x] Endpoint có trong backend; danh sách và quyền được ghi trong API inventory.
- [x] Request DTO/param được đối chiếu; endpoint user-create không đưa thành form do controller nhận raw User entity và cần password/role payload cụ thể.
- [x] Mapping ApiResponse.data, PageResponse.content và totalPage được dùng.
- [x] Token Bearer, cookie refresh, retry một lần và logout được nối.
- [x] UI bảo vệ admin theo ROLE_ADMIN; backend vẫn quyết định quyền cuối cùng.
- [x] Có thông báo lỗi theo message/status, loading và empty state cho các danh sách chính.
- [ ] Chưa xác nhận trên backend đang chạy; chạy app cần backend, MySQL và cấu hình môi trường hợp lệ.

### Endpoint có UI gọi

POST /auth/login, /auth/register, /auth/refresh-with-cookie, GET /auth/account, POST /auth/logout; toàn bộ /posts CRUD; /tags list/create/update/delete/confirm-delete; /posts/{postId}/comments, /posts/{postId}/comments/isApproved, /comments/me, /comments list/approve/delete; /users list/update/delete; /roles list/create/delete.

### Endpoint chưa có UI

- POST /auth/refresh: cookie refresh flow được ưu tiên.
- POST /auth/delete-refresh-token: thao tác quản trị nhạy cảm không đưa vào UI thường.
- GET /comments/{id}: moderation list đã hiển thị đủ dữ liệu.
- PUT /comments/{id}: chưa có giao diện sửa comment; API kiểm tra ownership.
- GET /tags/{id}: tag hiển thị qua danh sách/filter, chưa có trang riêng.
- POST /users: nhận raw entity thay vì DTO rõ ràng; cần kiểm tra payload password/role trước khi làm form.
- GET /users/{id}: user list đã có cùng fields.
- PUT /roles/{id}, GET /roles/{id}: form edit role chưa có; role list/create/delete đã có.

### Backend issue cần lưu ý

- PostResponseDTO không có tác giả; UI không thể hiện author hoặc biết quyền sửa trước request. Backend service kiểm tra quyền và trả 403 cho user không phải tác giả.
- PostFilterRequestDTO.from/to hiện chưa được dùng bởi PostService.
- CommentApprovedDTO dùng field Java isApproved; Lombok JavaBean property là approved, frontend gửi { "approved": true/false }.
- Security cho POST /tags chỉ yêu cầu authenticated, trong khi các thao tác tag khác yêu cầu ADMIN. UI giữ quản lý tags dưới admin.
