# Blog workspace

- `backend/` — Java Spring Boot REST API.
- `frontend/` — React + Vite application. See [frontend/README.md](frontend/README.md) for setup and run instructions.


docker compose up -d --build

 docker compose ps          

## Lưu avatar trên Cloudflare R2

Tạo bucket R2 và API token có quyền Object Read & Write cho bucket đó. Cấu hình các biến sau trong `.env` trước khi chạy Docker Compose:

```dotenv
R2_ENDPOINT=https://<ACCOUNT_ID>.r2.cloudflarestorage.com
R2_BUCKET=<bucket-name>
R2_PUBLIC_BASE_URL=https://pub-a44c3e8334794d33b672f196b9c41495.r2.dev
R2_ACCESS_KEY_ID=<access-key-id>
R2_SECRET_ACCESS_KEY=<secret-access-key>
```

`R2_ENDPOINT` là S3 API endpoint của tài khoản Cloudflare. Backend dùng region `auto` và luôn lưu avatar lên R2. Khi chạy backend trực tiếp trên máy, đặt các biến này trong Environment Variables của IDE hoặc trong file cấu hình profile Spring đang chạy; `.env` chỉ tự được Docker Compose nạp.

Các avatar mới được mã hóa WebP rồi tải lên theo key `users/<user-id>/avatars/<uuid>.webp`. Để trình duyệt tải ảnh, cấu hình bucket hoặc custom domain R2 cho phép đọc công khai theo chính sách của ứng dụng.
