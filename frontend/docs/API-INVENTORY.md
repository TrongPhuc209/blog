# Backend analysis and API inventory

Derived from backend Java controllers, DTOs, security config, services, entities, and exception handlers. Backend is the source of truth.

## Architecture and data model

Spring Boot REST API backed by Spring Data JPA/MySQL. Controllers return ApiResponse<T> (status, message, data, errorCode, timestamp); collection endpoints wrap data in PageResponse<T> (content, one-based page, size, totalElements, totalPage, hasNext, hasPrevious). Pageable defaults to 10 rows, max 100, and one-indexed page input. Parameters are page, size, sort.

- User belongs to one Role; a user has posts, comments, refresh tokens.
- Post belongs to one author, has many comments, and many tags. PostResponseDTO does not expose author fields.
- Tag has many posts (many-to-many).
- Comment belongs to a user and a post; isApproved defaults false and public post comments filter to approved rows.
- RefreshToken belongs to a user.

## Authentication and authorization

- Login: POST /auth/login accepts { username, password }. Response data has accessToken, refreshToken, tokenType, and user { id, username, role }. Backend also sets refreshToken as HttpOnly; Secure; Path=/ cookie.
- Access token is a signed JWT; send Authorization: Bearer <accessToken>. Its scope is ROLE_ADMIN or ROLE_USER. Access lifetime is configured as 86,400 seconds.
- Refresh cookie lifetime is 86,400 seconds. POST /auth/refresh-with-cookie reads the cookie, rotates refresh token, returns both tokens in data, and sets the new cookie. Frontend deliberately does not read/store refresh token from response data.
- POST /auth/refresh?token=... is also public and accepts refresh token as query parameter. Frontend uses cookie endpoint instead.
- POST /auth/logout requires login and a refresh cookie, removes that refresh token and clears cookie.
- On page load app calls refresh-with-cookie to restore session; on protected API 401 it refreshes once and retries. Concurrent 401s share one refresh request.
- Public endpoints: login, register, refresh endpoints; GET /posts/**, GET /tags/**. Other API requests require authentication unless security rules specify ADMIN.
- Admin rules: all /users/**, /roles/**; GET /comments and /comments/{id}; POST /auth/delete-refresh-token; PUT /tags/{id}; DELETE /tags/{id} and /tags/confirm-delete/{id}.
- Owner rules live in services: users can update their own posts/comments; admins can delete any post/comment; only admins approve comments. New posts/comments require authentication.
- Backend authorization mismatch: POST /tags is merely authenticated by URL rules, despite being management functionality. Frontend exposes tag management only to admins.
- CORS allows localhost:3000, localhost:4173, localhost:5173, 127.0.0.1:3000 and https://yourdomain.com, with credentials. Cookie is Secure; use HTTPS for non-local deployments.

## API inventory

| Method | Endpoint | Auth / role | Request | Response data | Frontend feature |
|---|---|---|---|---|---|
| POST | /auth/login | Public | LoginResquestDTO {username,password} | LoginResponseDTO | Login |
| POST | /auth/register | Public | RegisterRequestDTO {email,password,name,address} | empty string | Register |
| POST | /auth/refresh | Public | query token | ExchangeTokenResponse | Not used; cookie flow preferred |
| POST | /auth/refresh-with-cookie | Public; HttpOnly cookie | refresh cookie | ExchangeTokenResponse; rotated cookie | Restore/renew session |
| GET | /auth/account | Authenticated | Bearer token | UserLogin {id,username,role} | Verify current account |
| POST | /auth/logout | Authenticated + refresh cookie | cookie | string "ok"; cookie cleared | Logout |
| POST | /auth/delete-refresh-token | ADMIN | query userId | message string | Not exposed in UI |
| POST | /posts | Authenticated | PostRequestDTO {title,content,tag:[{id,name}]} | PostResponseDTO | Create post |
| GET | /posts | Public | filters title,content,userId,tagName; pageable | PageResponse<PostResponseDTO> | Home/list/search/tag filter |
| GET | /posts/{id} | Public | path id | PostResponseDTO | Post detail/edit preload |
| PUT | /posts/{id} | Authenticated; owner only | PostRequestDTO | PostResponseDTO | Edit post |
| DELETE | /posts/{id} | Authenticated; owner or ADMIN | path id | string | Admin delete; no author field prevents safe ownership UI |
| POST | /posts/{postId}/comments | Authenticated | CommentRequestDTO {content,post:{id}} | CommentResponseDTO | Add comment |
| GET | /posts/{postId}/comments/isApproved | Public via GET /posts/** whitelist | path + pageable | page of approved comments | Post comments |
| GET | /comments | ADMIN | filters isApproved,postId,userId; pageable | page of comments | Moderate/filter comments |
| GET | /comments/{id} | ADMIN | path id | CommentResponseDTO | API wrapper; no standalone UI |
| GET | /comments/me | Authenticated | pageable | page of current user's comments | My comments |
| PUT | /comments/{id} | Authenticated; comment owner only | CommentRequestDTO {content,post:{id}} | CommentResponseDTO | Not exposed (no edit form) |
| PUT | /comments/{id}/approve | Authenticated; service requires ADMIN | {approved:boolean} (Lombok JavaBean getter/setter property for primitive isApproved) | string | Approve/unapprove |
| DELETE | /comments/{id} | Authenticated; owner or ADMIN | path id | string | Delete own/admin comments |
| POST | /tags | Authenticated (not ADMIN in URL rule) | TagRequestDTO {name} | TagResponseDTO | Admin tag management |
| GET | /tags | Public | filters name,postId; pageable | page of TagResponseDTO | Topic directory/filter |
| GET | /tags/{id} | Public | path id | TagResponseDTO | API available; no detail page |
| PUT | /tags/{id} | ADMIN | TagRequestDTO {name} | TagResponseDTO | Edit tag |
| DELETE | /tags/{id} | ADMIN | path id | list of posts currently using tag | Check tag usage before removal |
| DELETE | /tags/confirm-delete/{id} | ADMIN | path id | string | Confirm tag delete/removal from posts |
| POST | /users | ADMIN | raw User entity (name/email/password/address/role) | UserResponseDTO | Not exposed (creation payload needs clarification) |
| GET | /users | ADMIN | filters name,address,email,role; pageable | page of UserResponseDTO | User list/filter |
| GET | /users/{id} | ADMIN | path id | UserResponseDTO | API available; list provides same data |
| PUT | /users/{id} | ADMIN | UserRequestDTO {id,name,email,address,role:{id,name,description?}} | UserResponseDTO | Edit user |
| DELETE | /users/{id} | ADMIN | path id | string/null | Delete user |
| POST | /roles | ADMIN | RoleRequestDTO {id,name,description} | RoleResponseDTO | Role management |
| GET | /roles | ADMIN | filter name; pageable | page of RoleResponseDTO | Role list |
| GET | /roles/{id} | ADMIN | path id | RoleResponseDTO | API available; list provides same data |
| PUT | /roles/{id} | ADMIN | RoleRequestDTO {id,name,description} | RoleResponseDTO | API wrapper; edit not exposed |
| DELETE | /roles/{id} | ADMIN | path id | string | Delete role |

## Backend behavior and limits

- PostFilterRequestDTO declares from and to, but PostService.getAllPost does not add date specifications; only title/content/userId/tagName filters currently work.
- Post responses omit author, despite entity relation. UI cannot label author or know ownership; backend rejects unauthorized edits/deletes. UI offers edit to signed-in users and displays backend errors for non-owners.
- CommentResponseDTO omits timestamps. Public post comments provide only approved rows.
- TagService.deleteTag only reports current usage; deletion is a separate confirm endpoint that detaches tag from posts and removes it.
- Registration password must be 6–50 chars and include lowercase, uppercase, digit, and one of @$!%*?&.
- Application exceptions generally use ApiResponse with message/data/errorCode. Security 401/403 handlers instead return {status:401|403,error,message}. Generic exception handler maps uncaught exceptions to HTTP 400. Frontend supports both message formats and status fallbacks.
- Page key is totalPage (singular), not totalPages.

## Frontend structure

Simple Vite React JavaScript app: src/api holds endpoint calls and Axios, src/context/AuthContext.jsx holds session state, src/components has shared layout/guards/loading, and src/pages contains public and admin routes. No UI framework or state library.
