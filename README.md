# Students Management System

Hệ thống quản lý sinh viên xây dựng bằng **Spring Boot 3** với các tính năng quản lý role, người dùng và sinh viên.

## Tính Năng

### 1. Quản Lý Role (Role Management)

Chức năng quản lý role cho phép quản trị viên tạo, sửa, xoá và quản lý các vai trò trong hệ thống.

#### Các tính năng chi tiết:

- **Danh sách Role**: Xem toàn bộ danh sách các role trong hệ thống
- **Tìm kiếm**: Tìm kiếm role theo tên hoặc mô tả (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo role mới với tên và mô tả
- **Sửa**: Chỉnh sửa thông tin role đã tồn tại
- **Xoá**: Xoá role khỏi hệ thống

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/roles` | Lấy danh sách tất cả role |
| GET | `/api/roles/search` | Tìm kiếm role với phân trang |
| GET | `/api/roles/{id}` | Lấy chi tiết role theo ID |
| POST | `/api/roles` | Tạo role mới |
| PUT | `/api/roles/{id}` | Cập nhật role |
| DELETE | `/api/roles/{id}` | Xoá role |

#### Cấu trúc Entity Role:

```java
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;              // ID duy nhất
    
    @Column(nullable = false, unique = true)
    private String name;          // Tên role (duy nhất)
    
    @Column
    private String description;   // Mô tả role
}
```

#### Request/Response Model:

**RoleRequest** (Tạo/Cập nhật role):
```json
{
  "name": "Admin",
  "description": "Quản trị viên toàn quyền"
}
```

**RoleResponse** (Phản hồi từ server):
```json
{
  "id": "uuid",
  "name": "Admin",
  "description": "Quản trị viên toàn quyền"
}
```

#### Service Layer:

Class `RoleService` cung cấp các phương thức:
- `getAll()`: Lấy toàn bộ role
- `search(keyword, page, size)`: Tìm kiếm với phân trang
- `getById(id)`: Lấy role theo ID
- `create(request)`: Tạo role mới
- `update(id, request)`: Cập nhật role
- `delete(id)`: Xoá role

#### Controller:

- **RoleController**: Quản lý API endpoints
- **RoleDashboardController**: Quản lý views HTML cho giao diện web

### 2. Quản Lý Người Dùng (User Management)

Chức năng quản lý người dùng cho phép quản trị viên tạo, sửa, xoá và phân quyền cho người dùng trong hệ thống.

#### Các tính năng chi tiết:

- **Danh sách Người Dùng**: Xem toàn bộ danh sách người dùng trong hệ thống
- **Tìm kiếm**: Tìm kiếm người dùng theo tên đăng nhập hoặc email (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo người dùng mới với thông tin cơ bản
- **Sửa**: Chỉnh sửa thông tin người dùng đã tồn tại
- **Xoá**: Xoá người dùng khỏi hệ thống
- **Phân Quyền**: Gán role cho người dùng
- **Kích hoạt/Vô hiệu hóa**: Bật/tắt tài khoản người dùng

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/users` | Lấy danh sách tất cả người dùng |
| GET | `/api/users/search` | Tìm kiếm người dùng với phân trang |
| GET | `/api/users/{id}` | Lấy chi tiết người dùng theo ID |
| POST | `/api/users` | Tạo người dùng mới |
| PUT | `/api/users/{id}` | Cập nhật người dùng |
| DELETE | `/api/users/{id}` | Xoá người dùng |

#### Cấu trúc Entity User:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;                    // ID duy nhất
    
    @Column(nullable = false, unique = true)
    private String username;            // Tên đăng nhập (duy nhất)
    
    @Column(nullable = false, unique = true)
    private String email;               // Email (duy nhất)
    
    @Column(nullable = false)
    private String password;            // Mật khẩu
    
    private boolean enabled = true;     // Trạng thái kích hoạt
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles")
    private Set<Role> roles;            // Danh sách role gán cho user
}
```

#### Request/Response Model:

**UserRequest** (Tạo/Cập nhật người dùng):
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "enabled": true,
  "roleIds": ["uuid-role-1", "uuid-role-2"]
}
```

**UserResponse** (Phản hồi từ server):
```json
{
  "id": "uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "enabled": true,
  "roles": [
    {
      "id": "uuid-role-1",
      "name": "User",
      "description": "Người dùng bình thường"
    },
    {
      "id": "uuid-role-2",
      "name": "Moderator",
      "description": "Người quản lý nội dung"
    }
  ]
}
```

#### Service Layer:

Class `UserService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm người dùng với phân trang
- `getById(id)`: Lấy thông tin chi tiết người dùng
- `create(request)`: Tạo người dùng mới
- `update(id, request)`: Cập nhật thông tin người dùng
- `delete(id)`: Xoá người dùng
- `assignRoles(userId, roleIds)`: Gán role cho người dùng

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Username và Email phải duy nhất trong hệ thống
- **Xác Thực Mật Khẩu**: Mật khẩu phải có ít nhất 6 ký tự (sẽ được mã hóa sau này)
- **Trạng Thái Tài Khoản**: Hỗ trợ kích hoạt/vô hiệu hóa tài khoản
- **Phân Quyền**: Gán nhiều role cho một người dùng

#### Controller:

- **UserController**: Quản lý API endpoints
- **UserDashboardController**: Quản lý views HTML cho giao diện web
- **Kết Nối Role**: Tích hợp với hệ thống role để phân quyền

## 🛠️ Công Nghệ

- **Java 17**: Ngôn ngữ lập trình
- **Spring Boot 3.5.9**: Framework chính
- **Spring Data JPA**: ORM để tương tác với cơ sở dữ liệu
- **Lombok**: Giảm boilerplate code
- **Maven**: Build tool
- **Thymeleaf**: Template engine cho views

## Cấu Trúc Dự Án

```
src/
├── main/
│   ├── java/
│   │   └── com/example/stduents_management/
│   │       ├── role/
│   │       │   ├── controller/
│   │       │   │   ├── RoleController.java
│   │       │   │   └── RoleDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── RoleRequest.java
│   │       │   │   └── RoleResponse.java
│   │       │   ├── entity/
│   │       │   │   └── Role.java
│   │       │   ├── repository/
│   │       │   │   └── RoleRepository.java
│   │       │   └── service/
│   │       │       └── RoleService.java
│   │       ├── user/
│   │       │   ├── controller/
│   │       │   │   ├── UserController.java
│   │       │   │   └── UserDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── UserRequest.java
│   │       │   │   └── UserResponse.java
│   │       │   ├── entity/
│   │       │   │   └── User.java
│   │       │   ├── repository/
│   │       │   │   └── UserRepository.java
│   │       │   └── service/
│   │       │       └── UserService.java
│   │       └── web/
│   │           ├── AdminController.java
│   │           └── HomeController.java
│   └── resources/
│       ├── templates/
│       │   ├── roles/
│       │   │   ├── index.html
│       │   │   └── form.html
│       │   ├── user/
│       │   │   ├── index.html
│       │   │   └── form.html
│       │   ├── layout/
│       │   │   ├── dashboard.html
│       │   │   ├── header.html
│       │   │   ├── footer.html
│       │   │   └── sidebar.html
│       │   └── admin/
│       │       └── index.html
│       └── application.yml
└── test/
    └── java/
        └── StduentsManagementApplicationTests.java
```

## Cách Chạy Dự Án

### Yêu Cầu:
- Java 17 trở lên
- Maven 3.8+

### Các Bước:

1. **Clone repository**:
```bash
git clone https://github.com/NguyenHieuDavitDev/Students-Management.git
cd Students-Management
```

2. **Build project**:
```bash
mvn clean install
```

3. **Chạy ứng dụng**:
```bash
mvn spring-boot:run
```

4. **Truy cập ứng dụng**:
- Mở trình duyệt và truy cập `http://localhost:8080`

## Hướng Dẫn Sử Dụng

### Quản Lý Role:

1. **Xem danh sách Role**:
   - Truy cập `/roles` trên giao diện web
   - Hoặc gọi API `GET /api/roles`

2. **Tìm kiếm Role**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo tên hoặc mô tả

3. **Tạo Role mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền tên role và mô tả
   - Click "Lưu"

4. **Sửa Role**:
   - Click nút "Sửa" trên dòng role cần chỉnh sửa
   - Cập nhật thông tin
   - Click "Lưu"

5. **Xoá Role**:
   - Click nút "Xoá" trên dòng role cần xoá
   - Xác nhận xoá

### Quản Lý Người Dùng:

1. **Xem danh sách Người Dùng**:
   - Truy cập `/users` trên giao diện web
   - Hoặc gọi API `GET /api/users`

2. **Tìm kiếm Người Dùng**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo tên đăng nhập hoặc email

3. **Tạo Người Dùng mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền thông tin:
     - Tên đăng nhập (phải duy nhất)
     - Email (phải duy nhất)
     - Mật khẩu (tối thiểu 6 ký tự)
     - Chọn role từ danh sách
   - Click "Lưu"

4. **Sửa Người Dùng**:
   - Click nút "Sửa" trên dòng người dùng cần chỉnh sửa
   - Cập nhật thông tin (không thể thay đổi tên đăng nhập)
   - Thay đổi role nếu cần
   - Click "Lưu"

5. **Xoá Người Dùng**:
   - Click nút "Xoá" trên dòng người dùng cần xoá
   - Xác nhận xoá

6. **Kích hoạt/Vô hiệu hóa Tài khoản**:
   - Click biểu tượng trạng thái bên cạnh người dùng
   - Chọn "Kích hoạt" hoặc "Vô hiệu hóa"

7. **Phân Quyền cho Người Dùng**:
   - Khi tạo hoặc sửa người dùng
   - Chọn một hoặc nhiều role từ danh sách
   - Người dùng sẽ có tất cả các quyền của role được chọn


## Tác Giả

**NguyenNgocMinhHieu** - [GitHub](https://github.com/NguyenHieuDavitDev)


## Các Tính Năng Sắp Tới

- [x] Quản lý vai trò (Role Management)
- [x] Quản lý người dùng (User Management)
- [ ] Quản lý sinh viên (Student Management)  
- [ ] Quản lý phân quyền chi tiết (Permission Management)
- [ ] Xác thực người dùng (Authentication)
- [ ] Mã hóa mật khẩu (Password Encryption)
- [ ] Audit Log
- [ ] Report & Analytics
- [ ] Gửi email thông báo
- [ ] API Documentation (Swagger)

---

**Phiên bản**: 0.0.1-SNAPSHOT  
**Cập nhật lần cuối**: 04/02/2026

