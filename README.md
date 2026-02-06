# Students Management System

Hệ thống quản lý sinh viên xây dựng bằng **Spring Boot 3** với các tính năng quản lý role, người dùng và sinh viên.

**Last updated**: February 6, 2026 - Updated by MinhHieu via Pull Request

## Tính Năng

### 1. Quản Lý Role (Role Management)

Chức năng quản lý role cho phép quản trị viên tạo, sửa, xoá và quản#### Xác Thực & Bảo Mật:

- **Kiểm tra T## Công Nghệ

- **Java 17**: Ngôn ngữ lập trình
- **Spring Boot 3.5.9**: Framework chính
- **Spring Data JPA**: ORM để tương tác với cơ sở dữ liệu
- **Lombok**: Giảm boilerplate code
- **Maven**: Build tool
- **Thymeleaf**: Template engine cho views
- **Apache POI**: Xử lý file Excel (import/export)Nhất**: Mã khoa phải duy nhất trong hệ thống
- **Validate Dữ Liệu**: Tên khoa và mã khoa không được để trống
- **Sắp Xếp Tự Động**: Danh sách khoa được sắp xếp theo tên khoa

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất danh sách khoa ra file Excel (.xlsx)
- Tự động định dạng với header rõ ràng
- Có thể xuất từ API hoặc giao diện web

**Import (Nhập dữ liệu):**
- Nhập danh sách khoa từ file Excel
- Tự động bỏ qua các khoa đã tồn tại (theo mã khoa)
- Trả về số lượng khoa được import thành công
- Hỗ trợ nhập hàng loạt

**Print (In ấn):**
- In danh sách khoa từ giao diện web
- Định dạng in đẹp và dễ đọc
- Hỗ trợ in từ trình duyệt

#### Controller:

- **FacultyController**: Quản lý API endpoints (CRUD, Import/Export)
- **FacultyDashboardController**: Quản lý views HTML cho giao diện web (bao gồm Print)


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

### 3. Quản Lý Khoa (Faculty Management)

Chức năng quản lý khoa cho phép quản trị viên tạo, sửa, xoá và quản lý các khoa trong hệ thống.

#### Các tính năng chi tiết:

- **Danh sách Khoa**: Xem toàn bộ danh sách khoa trong hệ thống
- **Tìm kiếm**: Tìm kiếm khoa theo mã khoa hoặc tên khoa (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo khoa mới với mã khoa và tên khoa
- **Sửa**: Chỉnh sửa thông tin khoa đã tồn tại
- **Xoá**: Xoá khoa khỏi hệ thống
- **Sắp xếp**: Danh sách được sắp xếp theo tên khoa

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/faculties` | Lấy danh sách tất cả khoa |
| GET | `/api/faculties/search` | Tìm kiếm khoa với phân trang |
| GET | `/api/faculties/{id}` | Lấy chi tiết khoa theo ID |
| POST | `/api/faculties` | Tạo khoa mới |
| PUT | `/api/faculties/{id}` | Cập nhật khoa |
| DELETE | `/api/faculties/{id}` | Xoá khoa |
| GET | `/api/faculties/export` | Xuất danh sách khoa ra file Excel |
| POST | `/api/faculties/import` | Nhập danh sách khoa từ file Excel |

#### Cấu trúc Entity Faculty:

```java
@Entity
@Table(name = "faculties")
public class Faculty {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID facultyId;              // ID duy nhất
    
    @Column(nullable = false, unique = true)
    private String facultyCode;          // Mã khoa (duy nhất)
    
    @Column(nullable = false)
    private String facultyName;          // Tên khoa
}
```

#### Request/Response Model:

**FacultyRequest** (Tạo/Cập nhật khoa):
```json
{
  "facultyCode": "CS",
  "facultyName": "Khoa Công Nghệ Thông Tin"
}
```

**FacultyResponse** (Phản hồi từ server):
```json
{
  "facultyId": "uuid",
  "facultyCode": "CS",
  "facultyName": "Khoa Công Nghệ Thông Tin"
}
```

#### Service Layer:

Class `FacultyService` cung cấp các phương thức:
- `getAll()`: Lấy toàn bộ khoa được sắp xếp theo tên
- `search(keyword, page, size)`: Tìm kiếm khoa với phân trang
- `getById(id)`: Lấy khoa theo ID
- `create(request)`: Tạo khoa mới
- `update(id, request)`: Cập nhật khoa
- `delete(id)`: Xoá khoa

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Mã khoa phải duy nhất trong hệ thống
- **Validate Dữ Liệu**: Tên khoa và mã khoa không được để trống
- **Sắp Xếp Tự Động**: Danh sách khoa được sắp xếp theo tên khoa

#### Controller:

- **FacultyController**: Quản lý API endpoints
- **FacultyDashboardController**: Quản lý views HTML cho giao diện web

### 4. Quản Lý Ngành Học (Major Management)

Chức năng quản lý ngành học cho phép quản trị viên tạo, sửa, xoá các ngành học thuộc các khoa trong hệ thống.

#### Các tính năng chi tiết:

- **Danh sách Ngành Học**: Xem toàn bộ danh sách các ngành học trong hệ thống
- **Tìm kiếm**: Tìm kiếm ngành học theo tên ngành (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo ngành học mới và liên kết với khoa
- **Sửa**: Chỉnh sửa thông tin ngành học đã tồn tại
- **Xoá**: Xoá ngành học khỏi hệ thống
- **Liên kết Khoa**: Mỗi ngành học được liên kết với một khoa cụ thể
- **Duy nhất theo Khoa**: Tên ngành phải duy nhất trong mỗi khoa

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/majors` | Lấy danh sách tất cả ngành học (có phân trang) |
| GET | `/api/majors/{id}` | Lấy chi tiết ngành học theo ID |
| GET | `/api/majors/all` | Lấy tất cả ngành học (không phân trang - dành cho dropdown) |
| POST | `/api/majors` | Tạo ngành học mới |
| PUT | `/api/majors/{id}` | Cập nhật ngành học |
| DELETE | `/api/majors/{id}` | Xoá ngành học |

#### Cấu trúc Entity Major:

```java
@Entity
@Table(
        name = "majors",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"major_name", "faculty_id"})
        }
)
public class Major {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID majorId;              // ID duy nhất
    
    @Column(nullable = false)
    private String majorName;          // Tên ngành (duy nhất trong khoa)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Faculty faculty;           // Khoa mà ngành này thuộc về
}
```

#### Request/Response Model:

**MajorRequest** (Tạo/Cập nhật ngành học):
```json
{
  "majorName": "Công Nghệ Thông Tin",
  "facultyId": "uuid-faculty-1"
}
```

**MajorResponse** (Phản hồi từ server):
```json
{
  "majorId": "uuid",
  "majorName": "Công Nghệ Thông Tin",
  "facultyId": "uuid-faculty-1",
  "facultyName": "Khoa Công Nghệ Thông Tin"
}
```

#### Service Layer:

Class `MajorService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm ngành học với phân trang
- `getById(id)`: Lấy ngành học theo ID
- `create(request)`: Tạo ngành học mới
- `update(id, request)`: Cập nhật ngành học
- `delete(id)`: Xoá ngành học
- `getForPrint()`: Lấy tất cả ngành học (dành cho dropdown/print)
- `exportExcel(response)`: Xuất danh sách ngành học ra file Excel
- `importExcel(file)`: Nhập danh sách ngành học từ file Excel

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Tên ngành phải duy nhất trong mỗi khoa
- **Validate Dữ Liệu**: Tên ngành và khoa không được để trống
- **Kiểm tra Khoa**: Khoa phải tồn tại trước khi tạo ngành học
- **Sắp Xếp Tự Động**: Danh sách ngành học được sắp xếp theo tên ngành

#### Tính Năng Import/Export:

**Export (Xuất dữ liệu):**
- Xuất danh sách ngành học ra file Excel (.xlsx)
- File chứa 2 cột: Tên ngành, Khoa
- Tự động định dạng với header rõ ràng
- Có thể xuất từ API hoặc giao diện web

**Import (Nhập dữ liệu):**
- Nhập danh sách ngành học từ file Excel
- Tự động bỏ qua các ngành đã tồn tại (theo tên ngành và khoa)
- Tự động liên kết với khoa dựa trên tên khoa trong file
- Trả về số lượng ngành học được import thành công
- Hỗ trợ nhập hàng loạt

#### Controller:

- **MajorController**: Quản lý API endpoints (CRUD, Import/Export)
- **MajorDashboardController**: Quản lý views HTML cho giao diện web

##  Công Nghệ

- **Java 17**: Ngôn ngữ lập trình
- **Spring Boot 3.5.9**: Framework chính
- **Spring Data JPA**: ORM để tương tác với cơ sở dữ liệu
- **Lombok**: Giảm boilerplate code
- **Maven**: Build tool
- **Thymeleaf**: Template engine cho views
- **Apache POI**: Xử lý file Excel (import/export)

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
│   │       ├── faculty/
│   │       │   ├── controller/
│   │       │   │   ├── FacultyController.java
│   │       │   │   └── FacultyDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── FacultyRequest.java
│   │       │   │   └── FacultyResponse.java
│   │       │   ├── entity/
│   │       │   │   └── Faculty.java
│   │       │   ├── repository/
│   │       │   │   └── FacultyRepository.java
│   │       │   └── service/
│   │       │       └── FacultyService.java
│   │       ├── major/
│   │       │   ├── controller/
│   │       │   │   ├── MajorController.java
│   │       │   │   └── MajorDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── MajorRequest.java
│   │       │   │   └── MajorResponse.java
│   │       │   ├── entity/
│   │       │   │   └── Major.java
│   │       │   ├── repository/
│   │       │   │   └── MajorRepository.java
│   │       │   └── service/
│   │       │       └── MajorService.java
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
│       │   ├── faculties/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── majors/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
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

### Quản Lý Khoa:

1. **Xem danh sách Khoa**:
   - Truy cập `/faculties` trên giao diện web
   - Hoặc gọi API `GET /api/faculties`

2. **Tìm kiếm Khoa**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo mã khoa hoặc tên khoa

3. **Tạo Khoa mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền thông tin:
     - Mã khoa (phải duy nhất, ví dụ: CS, ENG, MATH)
     - Tên khoa (ví dụ: Khoa Công Nghệ Thông Tin)
   - Click "Lưu"

4. **Sửa Khoa**:
   - Click nút "Sửa" trên dòng khoa cần chỉnh sửa
   - Cập nhật thông tin
   - Click "Lưu"

5. **Xoá Khoa**:
   - Click nút "Xoá" trên dòng khoa cần xoá
   - Xác nhận xoá

6. **Sắp xếp Danh sách**:
   - Danh sách khoa được sắp xếp theo tên khoa tự động

7. **Xuất (Export) Khoa ra Excel**:
   - Click nút "Xuất Excel" trên giao diện
   - Hoặc gọi API `GET /api/faculties/export`
   - File Excel sẽ được tải xuống tự động

8. **Nhập (Import) Khoa từ Excel**:
   - Chuẩn bị file Excel với 2 cột: Mã Khoa, Tên Khoa
   - Click nút "Nhập Excel" trên giao diện
   - Chọn file Excel từ máy tính
   - Hệ thống sẽ import và hiển thị số lượng khoa được thêm
   - Hoặc gọi API `POST /api/faculties/import` với file Excel

9. **In (Print) Danh sách Khoa**:
   - Click nút "In" hoặc "Print" trên giao diện
   - Một trang in đẹp sẽ hiển thị
   - Sử dụng Ctrl+P hoặc Command+P để in tài liệu

### Quản Lý Ngành Học:

1. **Xem danh sách Ngành Học**:
   - Truy cập `/majors` trên giao diện web
   - Hoặc gọi API `GET /api/majors`

2. **Tìm kiếm Ngành Học**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo tên ngành

3. **Tạo Ngành Học mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền thông tin:
     - Tên ngành (ví dụ: Công Nghệ Thông Tin)
     - Chọn khoa từ dropdown
   - Tên ngành phải duy nhất trong khoa được chọn
   - Click "Lưu"

4. **Sửa Ngành Học**:
   - Click nút "Sửa" trên dòng ngành học cần chỉnh sửa
   - Cập nhật thông tin tên ngành hoặc khoa
   - Click "Lưu"

5. **Xoá Ngành Học**:
   - Click nút "Xoá" trên dòng ngành học cần xoá
   - Xác nhận xoá

6. **Sắp xếp Danh sách**:
   - Danh sách ngành học được sắp xếp theo tên ngành tự động

7. **Xuất (Export) Ngành Học ra Excel**:
   - Click nút "Xuất Excel" trên giao diện
   - Hoặc gọi API `GET /api/majors/export`
   - File Excel sẽ được tải xuống tự động với 2 cột: Tên ngành, Khoa

8. **Nhập (Import) Ngành Học từ Excel**:
   - Chuẩn bị file Excel với 2 cột: Tên Ngành, Khoa
   - Click nút "Nhập Excel" trên giao diện
   - Chọn file Excel từ máy tính
   - Hệ thống sẽ tự động liên kết ngành với khoa dựa trên tên khoa trong file
   - Hiển thị số lượng ngành được import thành công
   - Hoặc gọi API `POST /api/majors/import` với file Excel

9. **In (Print) Danh sách Ngành Học**:
   - Click nút "In" hoặc "Print" trên giao diện
   - Một trang in đẹp sẽ hiển thị
   - Sử dụng Ctrl+P hoặc Command+P để in tài liệu

## Tác Giả

**NguyenNgocMinhHieu** - [GitHub](https://github.com/NguyenHieuDavitDev)


## Các Tính Năng Sắp Tới

- [x] Quản lý vai trò (Role Management)
- [x] Quản lý người dùng (User Management)
- [x] Quản lý khoa (Faculty Management)
- [x] Quản lý ngành học (Major Management)
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
**Cập nhật lần cuối**: 06/02/2026 (Major Management features added with Import/Export/Print)

