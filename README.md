# Students Management System

Students Management System là dự án xây dựng hệ thống quản lý sinh viên dành cho môi trường giáo dục (trường đại học, cao đẳng hoặc trung tâm đào tạo).

Dự án được phát triển bằng **Spring Boot 3**, áp dụng kiến trúc RESTful API nhằm hỗ trợ quản lý thông tin sinh viên, lớp học và các nghiệp vụ liên quan một cách khoa học, rõ ràng và dễ mở rộng.

Mục tiêu của dự án là áp dụng kiến thức về lập trình Backend, thiết kế cơ sở dữ liệu và xây dựng API vào một bài toán thực tế trong lĩnh vực quản lý giáo dục

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

### 5. Quản Lý Lớp Học (Classroom Management)

Chức năng quản lý lớp học cho phép quản trị viên tạo, sửa, xoá các lớp học thuộc các ngành trong hệ thống.

#### Các tính năng chi tiết:

- **Danh sách Lớp Học**: Xem toàn bộ danh sách các lớp học trong hệ thống
- **Tìm kiếm**: Tìm kiếm lớp học theo mã lớp, tên lớp hoặc năm học (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo lớp học mới và liên kết với ngành học
- **Sửa**: Chỉnh sửa thông tin lớp học đã tồn tại
- **Xoá**: Xoá lớp học khỏi hệ thống
- **Liên kết Ngành**: Mỗi lớp học được liên kết với một ngành học cụ thể
- **Duy nhất theo Năm Học**: Mã lớp phải duy nhất trong mỗi năm học
- **Quản lý Trạng thái**: Kích hoạt/vô hiệu hóa lớp học
- **Thông tin Chi Tiết**: Hỗ trợ lưu thông tin loại đào tạo, bậc đào tạo, số lượng sinh viên tối đa, tình trạng lớp

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/classes` | Lấy danh sách tất cả lớp học (có phân trang) |
| GET | `/api/classes/{id}` | Lấy chi tiết lớp học theo ID |
| GET | `/api/classes/print` | Lấy tất cả lớp học (dành cho print) |
| GET | `/api/classes/export` | Xuất danh sách lớp học ra Excel |
| POST | `/api/classes` | Tạo lớp học mới |
| POST | `/api/classes/import` | Nhập danh sách lớp học từ Excel |
| PUT | `/api/classes/{id}` | Cập nhật lớp học |
| DELETE | `/api/classes/{id}` | Xoá lớp học |

#### Cấu trúc Entity ClassEntity:

```java
@Entity
@Table(
        name = "classes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"class_code", "academic_year"})
        }
)
public class ClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID classId;                  // ID duy nhất
    
    @Column(nullable = false)
    private String classCode;              // Mã lớp (duy nhất trong năm học)
    
    @Column(nullable = false)
    private String className;              // Tên lớp
    
    @Column(nullable = false)
    private String academicYear;           // Năm học (ví dụ: 2024-2025)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Major major;                   // Ngành học
    
    private String educationType;          // Loại đào tạo (ví dụ: Chính quy)
    
    private String trainingLevel;          // Bậc đào tạo (ví dụ: Đại học)
    
    private Integer maxStudent;            // Số lượng sinh viên tối đa
    
    private String classStatus;            // Tình trạng lớp (Đang học, Kết thúc)
    
    private Boolean isActive = true;       // Trạng thái kích hoạt
    
    private LocalDateTime createdAt;       // Thời gian tạo
    
    private LocalDateTime updatedAt;       // Thời gian cập nhật
}
```

#### Request/Response Model:

**ClassRequest** (Tạo/Cập nhật lớp học):
```json
{
  "classCode": "IT201",
  "className": "Công Nghệ Thông Tin K20",
  "academicYear": "2024-2025",
  "majorId": "uuid-major-1",
  "educationType": "Chính quy",
  "trainingLevel": "Đại học",
  "maxStudent": 50,
  "classStatus": "Đang học",
  "isActive": true
}
```

**ClassResponse** (Phản hồi từ server):
```json
{
  "classId": "uuid",
  "classCode": "IT201",
  "className": "Công Nghệ Thông Tin K20",
  "academicYear": "2024-2025",
  "majorId": "uuid-major-1",
  "majorName": "Công Nghệ Thông Tin",
  "educationType": "Chính quy",
  "trainingLevel": "Đại học",
  "maxStudent": 50,
  "classStatus": "Đang học",
  "isActive": true,
  "createdAt": "2026-02-07T10:30:00",
  "updatedAt": "2026-02-07T10:30:00"
}
```

#### Service Layer:

Class `ClassService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm lớp học với phân trang
- `getById(id)`: Lấy lớp học theo ID
- `create(request)`: Tạo lớp học mới
- `update(id, request)`: Cập nhật lớp học
- `delete(id)`: Xoá lớp học
- `getForPrint()`: Lấy tất cả lớp học (dành cho print)
- `exportExcel(response)`: Xuất danh sách lớp học ra file Excel
- `importExcel(file)`: Nhập danh sách lớp học từ file Excel

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Mã lớp phải duy nhất trong mỗi năm học
- **Validate Dữ Liệu**: Các trường bắt buộc không được để trống
- **Kiểm tra Ngành**: Ngành học phải tồn tại trước khi tạo lớp học
- **Sắp Xếp Tự Động**: Danh sách lớp học được sắp xếp theo mã lớp và năm học
- **Theo Dõi Thời Gian**: Tự động ghi nhận thời gian tạo và cập nhật

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất danh sách lớp học ra file Excel (.xlsx)
- File chứa các cột: Mã lớp, Tên lớp, Năm học, Ngành học, Loại đào tạo, Bậc đào tạo, Tối đa sinh viên, Trạng thái
- Tự động định dạng với header rõ ràng
- Có thể xuất từ API hoặc giao diện web

**Import (Nhập dữ liệu):**
- Nhập danh sách lớp học từ file Excel
- Tự động bỏ qua các lớp đã tồn tại (theo mã lớp và năm học)
- Tự động liên kết với ngành dựa trên tên ngành trong file
- Trả về số lượng lớp học được import thành công
- Hỗ trợ nhập hàng loạt

**Print (In ấn):**
- In danh sách lớp học từ giao diện web
- Định dạng in đẹp và dễ đọc
- Hỗ trợ in từ trình duyệt

#### Controller:

- **ClassController**: Quản lý API endpoints (CRUD, Import/Export/Print)
- **ClassDashboardController**: Quản lý views HTML cho giao diện web

### 6. Quản Lý Loại Đào Tạo (Education Type Management)

Chức năng quản lý loại đào tạo cho phép quản trị viên tạo, sửa, xoá các loại đào tạo trong hệ thống (ví dụ: Chính quy, Không chính quy, Vừa học vừa làm).

#### Các tính năng chi tiết:

- **Danh sách Loại Đào Tạo**: Xem toàn bộ danh sách các loại đào tạo trong hệ thống
- **Tìm kiếm**: Tìm kiếm loại đào tạo theo tên (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo loại đào tạo mới
- **Sửa**: Chỉnh sửa thông tin loại đào tạo đã tồn tại
- **Xoá**: Xoá loại đào tạo khỏi hệ thống
- **Quản lý Trạng thái**: Kích hoạt/vô hiệu hóa loại đào tạo
- **Duy nhất**: Tên loại đào tạo phải duy nhất trong hệ thống

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/education-types` | Lấy danh sách tất cả loại đào tạo (có phân trang) |
| GET | `/api/education-types/{id}` | Lấy chi tiết loại đào tạo theo ID |
| GET | `/api/education-types/all` | Lấy tất cả loại đào tạo (dành cho dropdown) |
| POST | `/api/education-types` | Tạo loại đào tạo mới |
| PUT | `/api/education-types/{id}` | Cập nhật loại đào tạo |
| DELETE | `/api/education-types/{id}` | Xoá loại đào tạo |

#### Cấu trúc Entity EducationType:

```java
@Entity
@Table(
        name = "education_types",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"education_type_name"})
        }
)
public class EducationType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID educationTypeId;          // ID duy nhất
    
    @Column(nullable = false, unique = true)
    private String educationTypeName;      // Tên loại đào tạo (duy nhất)
    
    @Column(nullable = false)
    private Boolean isActive = true;       // Trạng thái kích hoạt
    
    private LocalDateTime createdAt;       // Thời gian tạo
    
    private LocalDateTime updatedAt;       // Thời gian cập nhật
}
```

#### Request/Response Model:

**EducationTypeRequest** (Tạo/Cập nhật loại đào tạo):
```json
{
  "educationTypeName": "Chính quy",
  "isActive": true
}
```

**EducationTypeResponse** (Phản hồi từ server):
```json
{
  "educationTypeId": "uuid",
  "educationTypeName": "Chính quy",
  "isActive": true,
  "createdAt": "2026-02-08T10:30:00",
  "updatedAt": "2026-02-08T10:30:00"
}
```

#### Service Layer:

Class `EducationTypeService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm loại đào tạo với phân trang
- `getById(id)`: Lấy loại đào tạo theo ID
- `create(request)`: Tạo loại đào tạo mới
- `update(id, request)`: Cập nhật loại đào tạo
- `delete(id)`: Xoá loại đào tạo
- `getAll()`: Lấy tất cả loại đào tạo (dành cho dropdown)

#### Controller:

- **EducationTypeController**: Quản lý API endpoints (CRUD)
- **EducationTypeDashboardController**: Quản lý views HTML cho giao diện web

### 7. Quản Lý Bậc Đào Tạo (Training Level Management)

Chức năng quản lý bậc đào tạo cho phép quản trị viên tạo, sửa, xoá các bậc đào tạo trong hệ thống (ví dụ: Đại học, Cao đẳng, Trung cấp).

#### Các tính năng chi tiết:

- **Danh sách Bậc Đào Tạo**: Xem toàn bộ danh sách các bậc đào tạo trong hệ thống
- **Tìm kiếm**: Tìm kiếm bậc đào tạo theo tên (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo bậc đào tạo mới
- **Sửa**: Chỉnh sửa thông tin bậc đào tạo đã tồn tại
- **Xoá**: Xoá bậc đào tạo khỏi hệ thống
- **Mô tả**: Hỗ trợ thêm mô tả chi tiết cho bậc đào tạo
- **Duy nhất**: Tên bậc đào tạo phải duy nhất trong hệ thống

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/training-levels` | Lấy danh sách tất cả bậc đào tạo (có phân trang) |
| GET | `/api/training-levels/{id}` | Lấy chi tiết bậc đào tạo theo ID |
| GET | `/api/training-levels/all` | Lấy tất cả bậc đào tạo (dành cho dropdown) |
| POST | `/api/training-levels` | Tạo bậc đào tạo mới |
| PUT | `/api/training-levels/{id}` | Cập nhật bậc đào tạo |
| DELETE | `/api/training-levels/{id}` | Xoá bậc đào tạo |

#### Cấu trúc Entity TrainingLevel:

```java
@Entity
@Table(
        name = "training_levels",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "training_level_name")
        }
)
public class TrainingLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID trainingLevelId;          // ID duy nhất
    
    @Column(nullable = false, unique = true)
    private String trainingLevelName;      // Tên bậc đào tạo (duy nhất)
    
    @Column
    private String description;            // Mô tả bậc đào tạo
}
```

#### Request/Response Model:

**TrainingLevelRequest** (Tạo/Cập nhật bậc đào tạo):
```json
{
  "trainingLevelName": "Đại học",
  "description": "Chương trình đào tạo bậc đại học"
}
```

**TrainingLevelResponse** (Phản hồi từ server):
```json
{
  "trainingLevelId": "uuid",
  "trainingLevelName": "Đại học",
  "description": "Chương trình đào tạo bậc đại học"
}
```

#### Service Layer:

Class `TrainingLevelService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm bậc đào tạo với phân trang
- `getById(id)`: Lấy bậc đào tạo theo ID
- `create(request)`: Tạo bậc đào tạo mới
- `update(id, request)`: Cập nhật bậc đào tạo
- `delete(id)`: Xoá bậc đào tạo
- `getAll()`: Lấy tất cả bậc đào tạo (dành cho dropdown)

#### Controller:

- **TrainingLevelController**: Quản lý API endpoints (CRUD)
- **TrainingLevelDashboardController**: Quản lý views HTML cho giao diện web

### 8. Quản Lý Sinh Viên (Student Management)

Chức năng quản lý sinh viên cho phép quản trị viên tạo, sửa, xoá thông tin sinh viên trong hệ thống. Mỗi sinh viên được liên kết với một lớp học cụ thể.

#### Các tính năng chi tiết:

- **Danh sách Sinh Viên**: Xem toàn bộ danh sách sinh viên trong hệ thống
- **Tìm kiếm**: Tìm kiếm sinh viên theo mã sinh viên hoặc tên sinh viên (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo sinh viên mới và liên kết với lớp học
- **Sửa**: Chỉnh sửa thông tin sinh viên đã tồn tại
- **Xoá**: Xoá sinh viên khỏi hệ thống
- **Liên kết Lớp Học**: Mỗi sinh viên được liên kết với một lớp học cụ thể
- **Duy nhất**: Mã sinh viên phải duy nhất trong hệ thống
- **Thông tin Chi Tiết**: Hỗ trợ lưu thông tin cá nhân đầy đủ (ngày sinh, giới tính, CMND, email, điện thoại, địa chỉ, avatar)

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/students` | Lấy danh sách tất cả sinh viên (có phân trang) |
| GET | `/api/students/{id}` | Lấy chi tiết sinh viên theo ID |
| GET | `/api/students/print` | Lấy tất cả sinh viên (dành cho print) |
| POST | `/api/students` | Tạo sinh viên mới |
| PUT | `/api/students/{id}` | Cập nhật sinh viên |
| DELETE | `/api/students/{id}` | Xoá sinh viên |

#### Cấu trúc Entity Student:

```java
@Entity
@Table(
        name = "students",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "student_code")
        }
)
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID studentId;                 // ID duy nhất
    
    @Column(nullable = false, unique = true)
    private String studentCode;             // Mã sinh viên (duy nhất)
    
    @Column(nullable = false)
    private String fullName;                // Họ và tên đầy đủ
    
    private LocalDate dateOfBirth;          // Ngày sinh
    
    private String gender;                  // Giới tính
    
    private String citizenId;               // Số CMND/CCCD
    
    private String email;                   // Email
    
    private String phoneNumber;             // Số điện thoại
    
    private String address;                 // Địa chỉ
    
    private String avatar;                  // Ảnh đại diện
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ClassEntity clazz;              // Lớp học
}
```

#### Request/Response Model:

**StudentRequest** (Tạo/Cập nhật sinh viên):
```json
{
  "studentCode": "K20-001",
  "fullName": "Nguyễn Văn A",
  "dateOfBirth": "2002-05-15",
  "gender": "Nam",
  "citizenId": "123456789012",
  "email": "studentA@email.com",
  "phoneNumber": "0901234567",
  "address": "123 Đường ABC, Thành phố XYZ",
  "avatar": "avatar_url",
  "classId": "uuid-class-1"
}
```

**StudentResponse** (Phản hồi từ server):
```json
{
  "studentId": "uuid",
  "studentCode": "K20-001",
  "fullName": "Nguyễn Văn A",
  "dateOfBirth": "2002-05-15",
  "gender": "Nam",
  "citizenId": "123456789012",
  "email": "studentA@email.com",
  "phoneNumber": "0901234567",
  "address": "123 Đường ABC, Thành phố XYZ",
  "avatar": "avatar_url",
  "classId": "uuid-class-1",
  "className": "Công Nghệ Thông Tin K20",
  "majorId": "uuid-major-1",
  "majorName": "Công Nghệ Thông Tin"
}
```

#### Service Layer:

Class `StudentService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm sinh viên với phân trang
- `getById(id)`: Lấy sinh viên theo ID
- `create(request)`: Tạo sinh viên mới
- `update(id, request)`: Cập nhật sinh viên
- `delete(id)`: Xoá sinh viên
- `getForPrint()`: Lấy tất cả sinh viên (dành cho print)

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Mã sinh viên phải duy nhất trong hệ thống
- **Validate Dữ Liệu**: Các trường bắt buộc không được để trống
- **Kiểm tra Lớp Học**: Lớp học phải tồn tại trước khi tạo sinh viên
- **Sắp Xếp Tự Động**: Danh sách sinh viên được sắp xếp theo mã sinh viên
- **Liên Kết Dữ Liệu**: Thông tin lớp và ngành được tự động lấy từ lớp học đã liên kết

#### Tính Năng Print:

**Print (In ấn):**
- In danh sách sinh viên từ giao diện web
- Định dạng in đẹp và dễ đọc
- Hỗ trợ in từ trình duyệt
- Hiển thị thông tin đầy đủ của sinh viên

#### Controller:

- **StudentController**: Quản lý API endpoints (CRUD, Print)
- **StudentDashboardController**: Quản lý views HTML cho giao diện web

### 9. Quản Lý Giảng Viên (Lecturer Management)

Chức năng quản lý giảng viên cho phép quản trị viên tạo, sửa, xoá thông tin giảng viên trong hệ thống. Mỗi giảng viên được liên kết với một khoa cụ thể.

#### Các tính năng chi tiết:

- **Danh sách Giảng Viên**: Xem toàn bộ danh sách giảng viên trong hệ thống
- **Tìm kiếm**: Tìm kiếm giảng viên theo mã giảng viên hoặc tên (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo giảng viên mới và liên kết với khoa
- **Sửa**: Chỉnh sửa thông tin giảng viên đã tồn tại
- **Xoá**: Xoá giảng viên khỏi hệ thống
- **Liên kết Khoa**: Mỗi giảng viên được liên kết với một khoa cụ thể
- **Duy nhất**: Mã giảng viên phải duy nhất trong hệ thống
- **Thông tin Chi Tiết**: Hỗ trợ lưu thông tin cá nhân đầy đủ (ngày sinh, giới tính, CMND, email, điện thoại, địa ch��, avatar)
- **Học Vị và Học Hàm**: Lưu thông tin học vị (Cử nhân, Thạc sĩ, Tiến sĩ) và học hàm (Giảng viên, Phó giáo sư, Giáo sư)

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/lecturers` | Lấy danh sách tất cả giảng viên (có phân trang) |
| GET | `/api/lecturers/{id}` | Lấy chi tiết giảng viên theo ID |
| GET | `/api/lecturers/print` | Lấy tất cả giảng viên (dành cho print) |
| POST | `/api/lecturers` | Tạo giảng viên mới |
| PUT | `/api/lecturers/{id}` | Cập nhật giảng viên |
| DELETE | `/api/lecturers/{id}` | Xoá giảng viên |

#### Cấu trúc Entity Lecturer:

```java
@Entity
@Table(
        name = "lecturers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "lecturer_code")
        }
)
public class Lecturer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID lecturerId;                // ID duy nhất
    
    @Column(nullable = false, unique = true)
    private String lecturerCode;            // Mã giảng viên (duy nhất)
    
    @Column(nullable = false)
    private String fullName;                // Họ và tên đầy đủ
    
    private LocalDate dateOfBirth;          // Ngày sinh
    
    private String gender;                  // Giới tính
    
    private String citizenId;               // Số CMND/CCCD
    
    private String email;                   // Email
    
    private String phoneNumber;             // Số điện thoại
    
    private String address;                 // Địa chỉ
    
    private String avatar;                  // Ảnh đại diện
    
    private String academicDegree;          // Học vị (Cử nhân, Thạc sĩ, Tiến sĩ)
    
    private String academicTitle;           // Học hàm (Giảng viên, Phó giáo sư, Giáo sư)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Faculty faculty;                // Khoa
}
```

#### Request/Response Model:

**LecturerRequest** (Tạo/Cập nhật giảng viên):
```json
{
  "lecturerCode": "GV001",
  "fullName": "Nguyễn Văn B",
  "dateOfBirth": "1990-03-20",
  "gender": "Nam",
  "citizenId": "123456789012",
  "email": "lecturerB@email.com",
  "phoneNumber": "0912345678",
  "address": "456 Đường XYZ, Thành phố ABC",
  "avatar": "avatar_url",
  "academicDegree": "Tiến sĩ",
  "academicTitle": "Phó Giáo Sư",
  "facultyId": "uuid-faculty-1"
}
```

**LecturerResponse** (Phản hồi từ server):
```json
{
  "lecturerId": "uuid",
  "lecturerCode": "GV001",
  "fullName": "Nguyễn Văn B",
  "dateOfBirth": "1990-03-20",
  "gender": "Nam",
  "citizenId": "123456789012",
  "email": "lecturerB@email.com",
  "phoneNumber": "0912345678",
  "address": "456 Đường XYZ, Thành phố ABC",
  "avatar": "avatar_url",
  "academicDegree": "Tiến sĩ",
  "academicTitle": "Phó Giáo Sư",
  "facultyId": "uuid-faculty-1",
  "facultyName": "Khoa Công Nghệ Thông Tin"
}
```

#### Service Layer:

Class `LecturerService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm giảng viên với phân trang
- `getById(id)`: Lấy giảng viên theo ID
- `create(request)`: Tạo giảng viên mới
- `update(id, request)`: Cập nhật giảng viên
- `delete(id)`: Xoá giảng viên
- `getForPrint()`: Lấy tất cả giảng viên (dành cho print)

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Mã giảng viên phải duy nhất trong hệ thống
- **Validate Dữ Liệu**: Các trường bắt buộc không được để trống
- **Kiểm tra Khoa**: Khoa phải tồn tại trước khi tạo giảng viên
- **Sắp Xếp Tự Động**: Danh sách giảng viên được sắp xếp theo mã giảng viên
- **Liên Kết Dữ Liệu**: Thông tin khoa được tự động lấy từ khoa đã liên kết

#### Tính Năng Print:

**Print (In ấn):**
- In danh sách giảng viên từ giao diện web
- Định dạng in đẹp và dễ đọc
- Hỗ trợ in từ trình duyệt
- Hiển thị thông tin đầy đủ của giảng viên

#### Controller:

- **LecturerController**: Quản lý API endpoints (CRUD, Print)
- **LecturerDashboardController**: Quản lý views HTML cho giao diện web

### 10. Quản Lý Chức Danh (Position Management)

Chức năng quản lý chức danh cho phép quản trị viên tạo, sửa, xoá các chức danh trong hệ thống. Các chức danh được sử dụng để phân loại các vị trí công việc trong tổ chức (ví dụ: Trưởng bộ môn, Phó trưởng bộ môn, Giáo viên).

#### Các tính năng chi tiết:

- **Danh sách Chức Danh**: Xem toàn bộ danh sách chức danh trong hệ thống
- **Tìm kiếm**: Tìm kiếm chức danh theo mã chức danh hoặc tên chức danh (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo chức danh mới với mã chức danh, tên chức danh và mô tả
- **Sửa**: Chỉnh sửa thông tin chức danh đã tồn tại
- **Xoá**: Xoá chức danh khỏi hệ thống
- **Mô tả Chi Tiết**: Hỗ trợ thêm mô tả cho mỗi chức danh
- **Duy nhất**: Mã chức danh và tên chức danh phải duy nhất trong hệ thống

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/positions` | Lấy danh sách tất cả chức danh (có phân trang) |
| GET | `/api/positions/search` | Tìm kiếm chức danh với phân trang |
| GET | `/api/positions/{id}` | Lấy chi tiết chức danh theo ID |
| GET | `/api/positions/all` | Lấy tất cả chức danh (dành cho dropdown) |
| GET | `/api/positions/export` | Xuất danh sách chức danh ra Excel |
| GET | `/api/positions/print` | Lấy tất cả chức danh (dành cho print) |
| POST | `/api/positions` | Tạo chức danh mới |
| POST | `/api/positions/import` | Nhập danh sách chức danh từ Excel |
| PUT | `/api/positions/{id}` | Cập nhật chức danh |
| DELETE | `/api/positions/{id}` | Xoá chức danh |

#### Cấu trúc Entity Position:

```java
@Entity
@Table(
        name = "training_programs",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"program_code", "major_id", "course"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID programId;

    @Column(
            name = "program_code",
            nullable = false,
            columnDefinition = "NVARCHAR(50)"
    )
    private String programCode;

    @Column(
            name = "program_name",
            nullable = false,
            columnDefinition = "NVARCHAR(200)"
    )
    private String programName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "major_id",
            nullable = false,
            columnDefinition = "uniqueidentifier"
    )
    private Major major;

    @Column(
            name = "course",
            nullable = false,
            columnDefinition = "NVARCHAR(20)"
    )
    private String course; // Khóa học (ví dụ: K20, K21, K22)

    @Column(
            name = "description",
            columnDefinition = "NVARCHAR(1000)"
    )
    private String description;

    @Column(
            name = "duration_years",
            columnDefinition = "INT"
    )
    private Integer durationYears; // Thời gian đào tạo (năm)

    @Column(
            name = "total_credits",
            columnDefinition = "INT"
    )
    private Integer totalCredits; // Tổng số tín chỉ

    @Column(
            name = "is_active",
            columnDefinition = "BIT"
    )
    private Boolean isActive = true;
}

```

#### Request/Response Model:

**TrainingProgramRequest** (Tạo/Cập nhật chức danh):
```json
{
  "TrainingProgramCode": "POS001",
  "TrainingProgramName": "Trưởng Bộ Môn",
  "description": "Quản lý bộ môn và giảng dạy"
}
```

**TrainingProgramResponse** (Phản hồi từ server):
```json
{
  "TrainingProgramId": "uuid",
  "positionCode": "POS001",
  "positionName": "Trưởng Bộ Môn",
  "description": "Quản lý bộ môn và giảng dạy"
}
```

#### Service Layer:

Class `PositionService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm chương trình đào tạo với phân trang
- `getById(id)`: Lấy chương trình đào tạo theo ID
- `create(request)`: Tạo chương trình đào tạo mới
- `update(id, request)`: Cập nhật chương trình đào tạo
- `delete(id)`: Xoá chương trình đào tạo
- `getAll()`: Lấy tất cả chương trình đào tạo (dành cho dropdown)
- `getForPrint()`: Lấy tất cả chương trình đào tạo (dành cho print)
- `exportExcel(response)`: Xuất danh sách chương trình đào tạo ra file Excel
- `importExcel(file)`: Nhập danh sách chương trình đào tạo từ file Excel

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Mã chương trình đào tạo và tên chương trình đào tạo phải duy nhất trong hệ thống
- **Validate Dữ Liệu**: Mã chương trình đào tạo và tên chương trình đào tạo không được để trống
- **Sắp Xếp Tự Động**: Danh sách chương trình đào tạo được sắp xếp theo tên chương trình đào tạo

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất danh sách chương trình đào tạo ra file Excel (.xlsx)
- File chứa 3 cột: Mã chương trình đào tạo, Tên chương trình đào tạo, Mô tả
- Tự động định dạng với header rõ ràng
- Có thể xuất từ API hoặc giao diện web

**Import (Nhập dữ liệu):**
- Nhập danh sách chương trình đào tạo từ file Excel
- Tự động bỏ qua các chương trình đào tạo đã tồn tại (theo mã chương trình đào tạo)
- Trả về số lượng chương trình đào tạo được import thành công
- Hỗ trợ nhập hàng loạt

**Print (In ấn):**
- In danh sách chương trình đào tạo từ giao diện web
- Định dạng in đẹp và dễ đọc
- Hỗ trợ in từ trình duyệt

#### Controller:

- **PositionController**: Quản lý API endpoints (CRUD, Import/Export)
- **PositionDashboardController**: Quản lý views HTML cho giao diện web (bao gồm Print)

### 11. Quản Lý học phần (Coures Management)

Chức năng quản lý học phần cho phép quản trị viên tạo, sửa, xoá các học phần trong hệ thống. Các học phần được sử dụng để phân loại các vị trí công việc trong tổ chức (ví dụ: toán cao cấp, lập trình java,...).

#### Các tính năng chi tiết:

- **Danh sách học phần**: Xem toàn bộ danh sách học phần trong hệ thống
- **Tìm kiếm**: Tìm kiếm học phần theo mã học phần hoặc tên học phần (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo học phần mới với mã học phần, tên học phần và mô tả
- **Sửa**: Chỉnh sửa thông tin học phần đã tồn tại
- **Xoá**: Xoá học phần khỏi hệ thống
- **Mô tả Chi Tiết**: Hỗ trợ thêm mô tả cho mỗi học phần
- **Duy nhất**: Mã học phần và tên học phần phải duy nhất trong hệ thống

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/courses` | Lấy danh sách tất cả học phần (có phân trang) |
| GET | `/api/courses/search` | Tìm kiếm học phần với phân trang |
| GET | `/api/courses/{id}` | Lấy chi tiết học phần theo ID |
| GET | `/api/courses/all` | Lấy tất cả học phần (dành cho dropdown) |
| GET | `/api/courses/export` | Xuất danh sách học phần ra Excel |
| GET | `/api/courses/print` | Lấy tất cả học phần (dành cho print) |
| POST | `/api/courses` | Tạo học phần mới |
| POST | `/api/courses/import` | Nhập danh sách học phần từ Excel |
| PUT | `/api/courses/{id}` | Cập nhật học phần |
| DELETE | `/api/courses/{id}` | Xoá học phần |

#### Cấu trúc Entity Position:

```java
@Entity
@Table(
        name = "courses",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"course_code"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "course_code", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String courseCode;

    @Column(name = "course_name", nullable = false, columnDefinition = "NVARCHAR(200)")
    private String courseName;

    @Column(name = "credits", columnDefinition = "INT")
    private Integer credits;

    @Column(name = "lecture_hours", columnDefinition = "INT")
    private Integer lectureHours;

    @Column(name = "practice_hours", columnDefinition = "INT")
    private Integer practiceHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Faculty faculty;

    @Column(name = "description", columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(name = "status", columnDefinition = "BIT")
    private Boolean status = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

```

#### Request/Response Model:

**CoursesRequest** (Tạo/Cập nhật học phần):
```json
{
  "courseCode": "IT101",
  "courseName": "Lập trình Java",
  "credits": 3,
  "lectureHours": 30,
  "practiceHours": 15,
  "facultyId": "1",
  "description": "Môn học Java cơ bản",
  "status": true
}
```

**CoursesResponse** (Phản hồi từ server):
```json
{
  "courseCode": "IT101",
  "courseName": "Lập trình Java",
  "credits": 3,
  "lectureHours": 30,
  "practiceHours": 15,
  "facultyId": "1",
  "description": "Môn học Java cơ bản",
  "status": true
}
```

#### Service Layer:

Class `CoursesService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm chương trình đào tạo với phân trang
- `getById(id)`: Lấy chương trình đào tạo theo ID
- `create(request)`: Tạo chương trình đào tạo mới
- `update(id, request)`: Cập nhật chương trình đào tạo
- `delete(id)`: Xoá chương trình đào tạo
- `getAll()`: Lấy tất cả chương trình đào tạo (dành cho dropdown)
- `getForPrint()`: Lấy tất cả chương trình đào tạo (dành cho print)
- `exportExcel(response)`: Xuất danh sách chương trình đào tạo ra file Excel
- `importExcel(file)`: Nhập danh sách chương trình đào tạo từ file Excel

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Mã chương trình đào tạo và tên chương trình đào tạo phải duy nhất trong hệ thống
- **Validate Dữ Liệu**: Mã chương trình đào tạo và tên chương trình đào tạo không được để trống
- **Sắp Xếp Tự Động**: Danh sách chương trình đào tạo được sắp xếp theo tên chương trình đào tạo

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất danh sách chương trình đào tạo ra file Excel (.xlsx)
- File chứa 3 cột: Mã chương trình đào tạo, Tên chương trình đào tạo, Mô tả
- Tự động định dạng với header rõ ràng
- Có thể xuất từ API hoặc giao diện web

**Import (Nhập dữ liệu):**
- Nhập danh sách chương trình đào tạo từ file Excel
- Tự động bỏ qua các chương trình đào tạo đã tồn tại (theo mã chương trình đào tạo)
- Trả về số lượng chương trình đào tạo được import thành công
- Hỗ trợ nhập hàng loạt

**Print (In ấn):**
- In danh sách chương trình đào tạo từ giao diện web
- Định dạng in đẹp và dễ đọc
- Hỗ trợ in từ trình duyệt

#### Controller:

- **CoursesController**: Quản lý API endpoints (CRUD, Import/Export)
- **CoursesDashboardController**: Quản lý views HTML cho giao diện web (bao gồm Print)

### 12. Quản Lý Toà Nhà (Building Management)

Chức năng quản lý toà nhà cho phép quản trị viên tạo, sửa, xoá thông tin các toà nhà trong hệ thống.

#### Các tính năng chi tiết:

- **Danh sách Toà Nhà**: Xem toàn bộ danh sách toà nhà trong hệ thống
- **Tìm kiếm**: Tìm kiếm toà nhà theo mã toà nhà hoặc tên toà nhà (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo toà nhà mới với đầy đủ thông tin
- **Sửa**: Chỉnh sửa thông tin toà nhà đã tồn tại
- **Xoá**: Xoá toà nhà khỏi hệ thống
- **Duy nhất**: Mã toà nhà phải duy nhất trong hệ thống
- **Thông tin Chi Tiết**: Hỗ trợ lưu địa chỉ, số tầng, tổng diện tích, mô tả

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/buildings` | Lấy danh sách toà nhà (có phân trang, tìm kiếm) |
| POST | `/api/buildings` | Tạo toà nhà mới |
| PUT | `/api/buildings/{id}` | Cập nhật toà nhà |
| DELETE | `/api/buildings/{id}` | Xoá toà nhà |

#### Cấu trúc Entity Building:

```java
@Entity
@Table(name = "buildings",
        uniqueConstraints = @UniqueConstraint(columnNames = "building_code"))
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID buildingId;          // ID duy nhất

    @Column(nullable = false, length = 20, unique = true)
    private String buildingCode;      // Mã toà nhà (duy nhất)

    @Column(nullable = false)
    private String buildingName;      // Tên toà nhà

    private String address;           // Địa chỉ

    private Integer numberOfFloors;   // Số tầng

    private Double totalArea;         // Tổng diện tích (m²)

    private String description;       // Mô tả
}
```

#### Request/Response Model:

**BuildingRequest** (Tạo/Cập nhật toà nhà):
```json
{
  "buildingCode": "A1",
  "buildingName": "Toà nhà A1",
  "address": "123 Đường ABC",
  "numberOfFloors": 10,
  "totalArea": 5000.0,
  "description": "Toà nhà giảng dạy chính"
}
```

**BuildingResponse** (Phản hồi từ server):
```json
{
  "buildingId": "uuid",
  "buildingCode": "A1",
  "buildingName": "Toà nhà A1",
  "address": "123 Đường ABC",
  "numberOfFloors": 10,
  "totalArea": 5000.0,
  "description": "Toà nhà giảng dạy chính"
}
```

#### Service Layer:

Class `BuildingService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm toà nhà với phân trang
- `getById(id)`: Lấy toà nhà theo ID
- `create(request)`: Tạo toà nhà mới
- `update(id, request)`: Cập nhật toà nhà
- `delete(id)`: Xoá toà nhà
- `getForPrint()`: Lấy tất cả toà nhà (dành cho print)
- `importExcel(file)`: Nhập danh sách toà nhà từ file Excel
- `exportExcel()`: Xuất danh sách toà nhà ra file Excel

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Mã toà nhà phải duy nhất trong hệ thống
- **Validate Dữ Liệu**: Mã toà nhà và tên toà nhà không được để trống
- **Sắp Xếp Tự Động**: Danh sách toà nhà được sắp xếp theo mã toà nhà

#### Tính Năng Print:

**Print (In ấn):**
- In danh sách toà nhà từ giao diện web
- Định dạng in đẹp và dễ đọc
- Hỗ trợ in từ trình duyệt

#### Controller:

- **BuildingController**: Quản lý API endpoints (CRUD)
- **BuildingDashboardController**: Quản lý views HTML cho giao diện web (bao gồm Print)


### 13. Quản Lý Loại Phòng (Room Type Management)

Chức năng quản lý loại phòng cho phép quản trị viên tạo, sửa, xoá các loại phòng trong hệ thống (ví dụ: phòng học, phòng thí nghiệm, hội trường,...).

#### Các tính năng chi tiết:

- **Danh sách Loại Phòng**: Xem toàn bộ danh sách loại phòng trong hệ thống
- **Tìm kiếm**: Tìm kiếm loại phòng theo mã hoặc tên loại phòng (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo loại phòng mới với mã, tên, mô tả và sức chứa tối đa
- **Sửa**: Chỉnh sửa thông tin loại phòng đã tồn tại
- **Xoá**: Xoá loại phòng khỏi hệ thống
- **Duy nhất**: Mã loại phòng phải duy nhất trong hệ thống
- **Sức Chứa**: Quản lý số lượng người tối đa của mỗi loại phòng

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/room-types` | Lấy danh sách loại phòng (có phân trang, tìm kiếm) |
| GET | `/api/room-types/{id}` | Lấy chi tiết loại phòng theo ID |
| POST | `/api/room-types` | Tạo loại phòng mới |
| PUT | `/api/room-types/{id}` | Cập nhật loại phòng |
| DELETE | `/api/room-types/{id}` | Xoá loại phòng |
| GET | `/api/room-types/print` | Lấy tất cả loại phòng (dành cho print) |
| GET | `/api/room-types/export` | Xuất danh sách loại phòng ra Excel |
| POST | `/api/room-types/import` | Nhập danh sách loại phòng từ Excel |

#### Cấu trúc Entity RoomType:

```java
@Entity
@Table(name = "room_types",
        uniqueConstraints = @UniqueConstraint(columnNames = "room_type_code"))
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID roomTypeId;          // ID duy nhất

    @Column(nullable = false, length = 20, unique = true)
    private String roomTypeCode;      // Mã loại phòng (duy nhất)

    @Column(nullable = false)
    private String roomTypeName;      // Tên loại phòng

    private String description;       // Mô tả

    private Integer maxCapacity;      // Sức chứa tối đa
}
```

#### Request/Response Model:

**RoomTypeRequest** (Tạo/Cập nhật loại phòng):
```json
{
  "roomTypeCode": "PH",
  "roomTypeName": "Phòng học",
  "description": "Phòng học lý thuyết",
  "maxCapacity": 50
}
```

**RoomTypeResponse** (Phản hồi từ server):
```json
{
  "roomTypeId": "uuid",
  "roomTypeCode": "PH",
  "roomTypeName": "Phòng học",
  "description": "Phòng học lý thuyết",
  "maxCapacity": 50
}
```

#### Service Layer:

Class `RoomTypeService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm loại phòng với phân trang
- `getById(id)`: Lấy loại phòng theo ID
- `create(request)`: Tạo loại phòng mới
- `update(id, request)`: Cập nhật loại phòng
- `delete(id)`: Xoá loại phòng
- `getForPrint()`: Lấy tất cả loại phòng (dành cho print)
- `importExcel(file)`: Nhập danh sách loại phòng từ file Excel
- `exportExcel()`: Xuất danh sách loại phòng ra file Excel

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Mã loại phòng phải duy nhất trong hệ thống
- **Validate Dữ Liệu**: Mã loại phòng và tên loại phòng không được để trống
- **Validate Sức Chứa**: Sức chứa tối đa phải lớn hơn 0
- **Sắp Xếp Tự Động**: Danh sách loại phòng được sắp xếp theo mã loại phòng

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất danh sách loại phòng ra file Excel (.xlsx)
- File chứa 4 cột: Mã loại phòng, Tên loại phòng, Mô tả, Sức chứa tối đa
- Tự động định dạng với header rõ ràng

**Import (Nhập dữ liệu):**
- Nhập danh sách loại phòng từ file Excel
- File Excel cần có 4 cột: Mã loại phòng, Tên loại phòng, Mô tả, Sức chứa tối đa
- Hỗ trợ nhập hàng loạt

**Print (In ấn):**
- In danh sách loại phòng từ giao diện web
- Định dạng in đẹp và dễ đọc
- Hỗ trợ in từ trình duyệt

#### Controller:

- **RoomTypeController**: Quản lý API endpoints (CRUD, Import/Export/Print)
- **RoomTypeDashboardController**: Quản lý views HTML cho giao diện web


### Quản Lý Phòng Học (Room Management)

Chức năng quản lý phòng học cho phép quản trị viên tạo, sửa, xoá phòng học và quản lý thông tin phòng theo tòa nhà, loại phòng, sức chứa và trạng thái sử dụng.

#### Các tính năng chi tiết:

- **Danh sách Phòng học**: Xem toàn bộ danh sách phòng học trong hệ thống
- **Tìm kiếm**: Tìm kiếm phòng học theo mã hoặc tên phòng (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới / Sửa / Xoá**: CRUD đầy đủ trên giao diện và API
- **Duy nhất**: Mã phòng (`roomCode`) phải duy nhất khi tạo/cập nhật
- **Enum trạng thái**: `AVAILABLE`, `IN_USE`, `MAINTENANCE`
- **Active**: Bật/tắt trạng thái hoạt động của phòng
- **created_at / updated_at**: Tự động cập nhật bằng `@PrePersist`, `@PreUpdate`

#### Import/Export/Print:

- **Export**: Xuất danh sách phòng học ra Excel (`.xlsx`)
- **Import**: Nhập danh sách phòng học từ Excel (dựa theo `buildingCode` và `roomTypeCode`)
- **Print**: In danh sách phòng học từ giao diện web

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/rooms` | Lấy danh sách phòng học (có phân trang, tìm kiếm) |
| GET | `/api/rooms/{id}` | Lấy chi tiết phòng học theo ID |
| POST | `/api/rooms` | Tạo phòng học mới |
| PUT | `/api/rooms/{id}` | Cập nhật phòng học |
| DELETE | `/api/rooms/{id}` | Xoá phòng học |
| GET | `/api/rooms/print` | Lấy tất cả phòng học (dành cho print) |
| GET | `/api/rooms/export` | Xuất danh sách phòng học ra Excel |
| POST | `/api/rooms/import` | Nhập danh sách phòng học từ Excel |

#### Giao diện Admin:

- Danh sách: `/admin/rooms` (tìm kiếm, phân trang, import/export/print)
- Form: `/admin/rooms/new`, `/admin/rooms/{id}/edit`
- Print: `/admin/rooms/print`

### Quản Lý Học Kỳ (Semester Management)

Chức năng quản lý học kỳ cho phép quản trị viên tạo, sửa, xoá và theo dõi tiến trình các học kỳ (thời gian đăng ký, thời gian diễn ra, trạng thái mở/đóng).

#### Các tính năng chi tiết:

- **Danh sách Học Kỳ**: Xem toàn bộ danh sách học kỳ trong hệ thống
- **Tìm kiếm**: Tìm kiếm theo mã hoặc tên học kỳ (hỗ trợ tìm kiếm gần đúng)
- **Phân trang & Sắp xếp**: Phân trang, sắp xếp theo `startDate` giảm dần
- **Thêm mới / Sửa / Xoá**: CRUD đầy đủ trên giao diện và API
- **Duy nhất**: Mã học kỳ (`code`) phải duy nhất
- **Ràng buộc ngày**:
  - `startDate < endDate`
  - `registrationStart ≤ registrationEnd`
  - `registrationEnd ≤ startDate`
- **Term hợp lệ**: `term` chỉ nhận giá trị 1, 2 hoặc 3
- **Không cho xoá khi đang mở**: Không cho phép xoá nếu `status = OPEN`
- **Tự động thời gian**: `createdAt` khi tạo, `updatedAt` khi cập nhật (qua `@PrePersist`, `@PreUpdate`)
- **Badge trạng thái**: Hiển thị badge đẹp trên dashboard theo `UPCOMING`, `OPEN`, `CLOSED`

#### Import/Export/Print:

- **Export**: Xuất danh sách học kỳ ra Excel (`.xlsx`)
  - Cột: Code, Name, AcademicYear, Term, StartDate, EndDate, RegistrationStart, RegistrationEnd, Status, Description
- **Import**: Nhập danh sách học kỳ từ Excel
  - Kiểm tra trùng mã (`code`), validate logic ngày và `term`
  - Bỏ qua các dòng trống hoặc dòng có mã đã tồn tại
- **Print**: In danh sách học kỳ từ giao diện web (A4, dễ đọc)

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/semesters` | Lấy danh sách học kỳ (có phân trang, tìm kiếm) |
| GET | `/api/semesters/{id}` | Lấy chi tiết học kỳ theo ID |
| POST | `/api/semesters` | Tạo học kỳ mới |
| PUT | `/api/semesters/{id}` | Cập nhật học kỳ |
| DELETE | `/api/semesters/{id}` | Xoá học kỳ (chỉ khi không OPEN) |
| GET | `/api/semesters/print` | Lấy tất cả học kỳ (dành cho print/export) |
| GET | `/api/semesters/export` | Xuất danh sách học kỳ ra Excel |
| POST | `/api/semesters/import` | Nhập danh sách học kỳ từ Excel |

#### Giao diện Admin:

- Danh sách: `/admin/semesters` (tìm kiếm, phân trang, import/export/print)
- Form: `/admin/semesters/new`, `/admin/semesters/{id}/edit`
- Print: `/admin/semesters/print`

### Quản Lý Lớp Học Phần (Class Section Management)

Module này quản lý các lớp học phần – tức là các lớp cụ thể của một học phần trong một học kỳ. Mỗi lớp học phần gắn với môn học, học kỳ, sĩ số tối đa, phòng học và trạng thái (Đang mở, Đã đóng, Hủy). Đây là nền tảng để sinh viên đăng ký học phần và giảng viên được phân công giảng dạy.

#### Các tính năng:

- **Danh sách lớp học phần**: Xem toàn bộ lớp học phần, tìm kiếm gần đúng theo mã lớp, tên lớp, mã/tên môn học, mã học kỳ
- **Phân trang**: Hỗ trợ phân trang, có thể chọn số bản ghi mỗi trang
- **Thêm/Sửa/Xóa**: CRUD đầy đủ qua giao diện web
- **Import Excel**: Nhập hàng loạt từ file Excel (mã lớp, mã môn, mã học kỳ, tên lớp, sĩ số tối đa, sĩ số hiện tại, trạng thái, mã phòng, ghi chú)
- **Export Excel**: Xuất danh sách ra file Excel
- **Print**: In danh sách lớp học phần từ giao diện (chuẩn A4, có chữ ký)

#### Ràng buộc nghiệp vụ:

- Mã lớp (`classCode`) phải duy nhất trong hệ thống
- Sĩ số hiện tại không được lớn hơn sĩ số tối đa
- Liên kết với môn học (Course), học kỳ (Semester), phòng (Room – tùy chọn)
- Trạng thái: `OPEN` (Đang mở), `CLOSED` (Đã đóng), `CANCELLED` (Hủy)

#### Endpoint (Web Dashboard):

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/admin/class-sections` | Danh sách (tìm kiếm, phân trang) |
| GET | `/admin/class-sections/new` | Form thêm mới |
| POST | `/admin/class-sections` | Lưu lớp học phần mới |
| GET | `/admin/class-sections/{id}/edit` | Form chỉnh sửa |
| POST | `/admin/class-sections/{id}` | Cập nhật |
| POST | `/admin/class-sections/{id}/delete` | Xóa |
| GET | `/admin/class-sections/print` | In danh sách |
| POST | `/admin/class-sections/import` | Import Excel |
| GET | `/admin/class-sections/export` | Export Excel |

#### Controller:

- **ClassSectionDashboardController**: Xử lý toàn bộ giao diện tại `/admin/class-sections`

### 14. Quản Lý Học Phần Tiên Quyết (Course Prerequisite Management)

Chức năng quản lý học phần tiên quyết cho phép quản trị viên thiết lập quan hệ tiên quyết giữa các học phần. Một học phần tiên quyết là học phần phải hoàn thành trước khi đăng ký học phần khác.

#### Các tính năng chi tiết:

- **Danh sách Học Phần Tiên Quyết**: Xem toàn bộ danh sách các quan hệ tiên quyết trong hệ thống
- **Tìm kiếm**: Tìm kiếm theo mã hoặc tên học phần (hỗ trợ tìm kiếm gần đúng trên cả học phần chính và tiên quyết)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo quan hệ tiên quyết mới giữa hai học phần
- **Sửa**: Chỉnh sửa quan hệ tiên quyết đã tồn tại
- **Xoá**: Xoá quan hệ tiên quyết khỏi hệ thống
- **Cập nhật hàng loạt**: Cập nhật toàn bộ danh sách tiên quyết của một học phần
- **Lọc theo Học Phần**: Xem tất cả tiên quyết của một học phần cụ thể
- **Chống Tự Tham Chiếu**: Không cho phép học phần là tiên quyết của chính nó
- **Chống Trùng Lặp**: Không cho phép tạo quan hệ tiên quyết đã tồn tại

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/course-prerequisites` | Lấy danh sách (có phân trang, tìm kiếm) |
| GET | `/api/course-prerequisites/{id}` | Lấy chi tiết theo ID |
| GET | `/api/course-prerequisites/by-course/{courseId}` | Lấy tất cả tiên quyết của một học phần |
| POST | `/api/course-prerequisites` | Tạo quan hệ tiên quyết mới |
| PUT | `/api/course-prerequisites/{id}` | Cập nhật quan hệ tiên quyết |
| PUT | `/api/course-prerequisites/by-course/{courseId}` | Cập nhật hàng loạt tiên quyết theo học phần |
| DELETE | `/api/course-prerequisites/{id}` | Xoá quan hệ tiên quyết |
| GET | `/api/course-prerequisites/export` | Xuất danh sách ra Excel |
| POST | `/api/course-prerequisites/import` | Nhập danh sách từ Excel |
| GET | `/api/course-prerequisites/print` | Lấy tất cả (dành cho print) |

#### Cấu trúc Entity CoursePrerequisite:

```java
@Entity
@Table(
        name = "course_prerequisites",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"course_id", "prerequisite_course_id"})
        }
)
public class CoursePrerequisite {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;                         // ID duy nhất

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;                   // Học phần chính

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_course_id", nullable = false)
    private Course prerequisiteCourse;       // Học phần tiên quyết
}
```

#### Request/Response Model:

**Tạo quan hệ tiên quyết** (Query parameters):
```
POST /api/course-prerequisites?courseId={uuid}&prerequisiteCourseId={uuid}
```

**CoursePrerequisiteResponse** (Phản hồi từ server):
```json
{
  "id": "uuid",
  "courseId": "uuid-course",
  "courseCode": "IT101",
  "courseName": "Lập Trình Java",
  "prerequisiteCourseId": "uuid-pre",
  "prerequisiteCourseCode": "IT100",
  "prerequisiteCourseName": "Nhập Môn Lập Trình"
}
```

#### Service Layer:

Class `CoursePrerequisiteService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm với phân trang
- `getById(id)`: Lấy quan hệ tiên quyết theo ID
- `create(courseId, prerequisiteCourseId)`: Tạo quan hệ tiên quyết mới
- `update(id, courseId, prerequisiteCourseId)`: Cập nhật quan hệ tiên quyết
- `updatePrerequisites(courseId, prerequisiteIds)`: Cập nhật hàng loạt tiên quyết của học phần
- `getPrerequisitesByCourseId(courseId)`: Lấy tất cả tiên quyết của học phần
- `getPrerequisiteIdsByCourseId(courseId)`: Lấy danh sách ID tiên quyết
- `delete(id)`: Xoá quan hệ tiên quyết
- `getForPrint()`: Lấy tất cả (dành cho print/export)

#### Xác Thực & Bảo Mật:

- **Chống Tự Tham Chiếu**: Học phần không thể là tiên quyết của chính nó
- **Chống Trùng Lặp**: Quan hệ tiên quyết phải duy nhất trong hệ thống
- **Kiểm tra Học Phần**: Học phần và học phần tiên quyết phải tồn tại trước khi tạo quan hệ
- **Validate Dữ Liệu**: Cả hai học phần phải được cung cấp đầy đủ

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất danh sách quan hệ tiên quyết ra file Excel (.xlsx)
- File chứa 4 cột: Mã học phần, Tên học phần, Mã học phần tiên quyết, Tên học phần tiên quyết
- Tự động định dạng với header rõ ràng

**Import (Nhập dữ liệu):**
- Nhập danh sách quan hệ tiên quyết từ file Excel
- File Excel cần có ít nhất 3 cột: Mã học phần, Tên học phần, Mã học phần tiên quyết
- Tự động tra cứu học phần theo mã và bỏ qua quan hệ đã tồn tại

**Print (In ấn):**
- In danh sách quan hệ tiên quyết từ giao diện web
- Định dạng in đẹp và dễ đọc
- Hỗ trợ in từ trình duyệt

#### Controller:

- **CoursePrerequisiteController**: Quản lý API endpoints (CRUD, Import/Export/Print)
- **CoursePrerequisiteDashboardController**: Quản lý views HTML cho giao diện web

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
│   │       ├── classroom/
│   │       │   ├── controller/
│   │       │   │   ├── ClassController.java
│   │       │   │   └── ClassDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── ClassRequest.java
│   │       │   │   └── ClassResponse.java
│   │       │   ├── entity/
│   │       │   │   └── ClassEntity.java
│   │       │   ├── repository/
│   │       │   │   └── ClassRepository.java
│   │       │   └── service/
│   │       │       └── ClassService.java
│   │       ├── educationtype/
│   │       │   ├── controller/
│   │       │   │   ├── EducationTypeController.java
│   │       │   │   └── EducationTypeDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── EducationTypeRequest.java
│   │       │   │   └── EducationTypeResponse.java
│   │       │   ├── entity/
│   │       │   │   └── EducationType.java
│   │       │   ├── repository/
│   │       │   │   └── EducationTypeRepository.java
│   │       │   └── service/
│   │       │       └── EducationTypeService.java
│   │       ├── traininglevel/
│   │       │   ├── controller/
│   │       │   │   ├── TrainingLevelController.java
│   │       │   │   └── TrainingLevelDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── TrainingLevelRequest.java
│   │       │   │   └── TrainingLevelResponse.java
│   │       │   ├── entity/
│   │       │   │   └── TrainingLevel.java
│   │       │   ├── repository/
│   │       │   │   └── TrainingLevelRepository.java
│   │       │   └── service/
│   │       │       └── TrainingLevelService.java
│   │       ├── student/
│   │       │   ├── controller/
│   │       │   │   ├── StudentController.java
│   │       │   │   └── StudentDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── StudentRequest.java
│   │       │   │   └── StudentResponse.java
│   │       │   ├── entity/
│   │       │   │   └── Student.java
│   │       │   ├── repository/
│   │       │   │   └── StudentRepository.java
│   │       │   └── service/
│   │       │       └── StudentService.java
│   │       ├── lecturer/
│   │       │   ├── controller/
│   │       │   │   ├── LecturerController.java
│   │       │   │   └── LecturerDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── LecturerRequest.java
│   │       │   │   └── LecturerResponse.java
│   │       │   ├── entity/
│   │       │   │   └── Lecturer.java
│   │       │   ├── repository/
│   │       │   │   └── LecturerRepository.java
│   │       │   └── service/
│   │       │       └── LecturerService.java
│   │       ├── trainingprogram/
│   │       │   ├── controller/
│   │       │   │   ├── TrainingprogramController.java
│   │       │   │   └── TrainingprogramDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── TrainingprogramRequest.java
│   │       │   │   └── TrainingprogramResponse.java
│   │       │   ├── entity/
│   │       │   │   └── Trainingprogram.java
│   │       │   ├── repository/
│   │       │   │   └── TrainingprogramRepository.java
│   │       │   └── service/
│   │       │       └── TrainingprogramrService.java
│   │       ├── position/
│   │       │   ├── controller/
│   │       │   │   ├── PositionController.java
│   │       │   │   └── PositionDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── PositionRequest.java
│   │       │   │   └── PositionResponse.java
│   │       │   ├── entity/
│   │       │   │   └── Position.java
│   │       │   ├── repository/
│   │       │   │   └── PositionRepository.java
│   │       │   └── service/
│   │       │       └── PositionService.java
│   │       ├── courses/
│   │       │   ├── controller/
│   │       │   │   ├── CoursesController.java
│   │       │   │   └── CoursesDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── CoursesRequest.java
│   │       │   │   └── CoursesResponse.java
│   │       │   ├── entity/
│   │       │   │   └── Courses.java
│   │       │   ├── repository/
│   │       │   │   └── CoursesRepository.java
│   │       │   └── service/
│   │       │       └── CoursesService.java
│   │       ├── equipment/
│   │       │   ├── controller/
│   │       │   │   ├── equipmentController.java
│   │       │   │   └── equipmentDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── equipmentRequest.java
│   │       │   │   └── equipmentResponse.java
│   │       │   ├── entity/
│   │       │   │   └── equipment.java
│   │       │   ├── repository/
│   │       │   │   └── equipmentRepository.java
│   │       │   └── service/
│   │       │       └── equipmentService.java
│   │       ├── building/
│   │       │   ├── controller/
│   │       │   │   ├── BuildingController.java
│   │       │   │   └── BuildingDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── BuildingRequest.java
│   │       │   │   └── BuildingResponse.java
│   │       │   ├── entity/
│   │       │   │   └── Building.java
│   │       │   ├── repository/
│   │       │   │   └── BuildingRepository.java
│   │       │   └── service/
│   │       │       └── BuildingService.java
│   │       ├── roomtype/
│   │       │   ├── controller/
│   │       │   │   ├── RoomTypeController.java
│   │       │   │   └── RoomTypeDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── RoomTypeRequest.java
│   │       │   │   └── RoomTypeResponse.java
│   │       │   ├── entity/
│   │       │   │   └── RoomType.java
│   │       │   ├── repository/
│   │       │   │   └── RoomTypeRepository.java
│   │       │   └── service/
│   │       │       └── RoomTypeService.java
│   │       ├── courseprerequisite/
│   │       │   ├── controller/
│   │       │   │   ├── CoursePrerequisiteController.java
│   │       │   │   └── CoursePrerequisiteDashboardController.java
│   │       │   ├── dto/
│   │       │   │   └── CoursePrerequisiteResponse.java
│   │       │   ├── entity/
│   │       │   │   └── CoursePrerequisite.java
│   │       │   ├── repository/
│   │       │   │   └── CoursePrerequisiteRepository.java
│   │       │   └── service/
│   │       │       └── CoursePrerequisiteService.java
│   │       ├── classsection/
│   │       │   ├── controller/
│   │       │   │   └── ClassSectionDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── ClassSectionRequest.java
│   │       │   │   └── ClassSectionResponse.java
│   │       │   ├── entity/
│   │       │   │   ├── ClassSection.java
│   │       │   │   └── ClassSectionStatus.java
│   │       │   ├── repository/
│   │       │   │   └── ClassSectionRepository.java
│   │       │   └── service/
│   │       │       └── ClassSectionService.java
│   │       ├── courseregistration/
│   │       │   ├── controller/
│   │       │   │   └── CourseRegistrationDashboardController.java
│   │       │   ├── dto/
│   │       │   │   ├── CourseRegistrationRequest.java
│   │       │   │   └── CourseRegistrationResponse.java
│   │       │   ├── entity/
│   │       │   │   └── CourseRegistration.java
│   │       │   ├── repository/
│   │       │   │   └── CourseRegistrationRepository.java
│   │       │   └── service/
│   │       │       └── CourseRegistrationService.java
│   │       ├── semester/
│   │       ├── room/
│   │       ├── lecturercourseclass/
│   │       ├── trainingprogram/
│   │       ├── common/
│   │       │   └── service/
│   │       │       └── FileStorageService.java
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
│       │   ├── classes/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── educationtypes/
│       │   │   ├── index.html
│       │   │   └── form.html
│       │   ├── traininglevels/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── students/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── lecturers/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── positions/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── courses/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── buildings/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── room-types/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── course-prerequisites/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── class-sections/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── course-registrations/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── lecturer-course-classes/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── semesters/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── rooms/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── training-programs/
│       │   │   ├── index.html
│       │   │   ├── form.html
│       │   │   └── print.html
│       │   ├── equipments/
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
- SQL Server hoặc cơ sở dữ liệu tương thích

### Các Bước:

1. **Clone repository**:
```bash
git clone https://github.com/NguyenHieuDavitDev/Students-Management.git
cd Students-Management
```

2. **Cấu hình Database**:
   - Tạo cơ sở dữ liệu mới
   - Cập nhật thông tin kết nối trong file `application.yml` hoặc `application.properties`
   - Ví dụ trong `application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:sqlserver://localhost:1433;databaseName=students_db
       username: sa
       password: your_password
     jpa:
       hibernate:
         ddl-auto: create-drop
   ```

3. **Build project**:
```bash
mvn clean install
```

4. **Chạy ứng dụng**:
```bash
mvn spring-boot:run
```

5. **Truy cập ứng dụng**:
   - Mở trình duyệt và truy cập `http://localhost:8080`
   - Ứng dụng sẽ tự động tạo các bảng dữ liệu khi khởi động lần đầu

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

### Quản Lý Lớp Học:

1. **Xem danh sách Lớp Học**:
   - Truy cập `/classes` trên giao diện web
   - Hoặc gọi API `GET /api/classes`

2. **Tìm kiếm Lớp Học**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo mã lớp, tên lớp hoặc năm học

3. **Tạo Lớp Học mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền thông tin:
     - Mã lớp (ví dụ: IT201, EN101)
     - Tên lớp (ví dụ: Công Nghệ Thông Tin K20)
     - Năm học (ví dụ: 2024-2025)
     - Chọn ngành học từ dropdown
     - Loại đào tạo (ví dụ: Chính quy, Không chính quy)
     - Bậc đào tạo (ví dụ: Đại học, Cao đẳng)
     - Số lượng sinh viên tối đa
     - Tình trạng lớp
   - Mã lớp phải duy nhất trong năm học được chọn
   - Click "Lưu"

4. **Sửa Lớp Học**:
   - Click nút "Sửa" trên dòng lớp học cần chỉnh sửa
   - Cập nhật thông tin
   - Click "Lưu"

5. **Xoá Lớp Học**:
   - Click nút "Xoá" trên dòng lớp học cần xoá
   - Xác nhận xoá

6. **Kích hoạt/Vô hiệu hóa Lớp Học**:
   - Click biểu tượng trạng thái bên cạnh lớp học
   - Chọn "Kích hoạt" hoặc "Vô hiệu hóa"

7. **Xuất (Export) Lớp Học ra Excel**:
   - Click nút "Xuất Excel" trên giao diện
   - Hoặc gọi API `GET /api/classes/export`
   - File Excel sẽ được tải xuống tự động

8. **Nhập (Import) Lớp Học từ Excel**:
   - Chuẩn bị file Excel với các cột: Mã lớp, Tên lớp, Năm học, Ngành học, Loại đào tạo, Bậc đào tạo, Tối đa sinh viên, Trạng thái
   - Click nút "Nhập Excel" trên giao diện
   - Chọn file Excel từ máy tính
   - Hệ thống sẽ tự động liên kết lớp với ngành dựa trên tên ngành trong file
   - Hiển thị số lượng lớp được import thành công
   - Hoặc gọi API `POST /api/classes/import` với file Excel

9. **In (Print) Danh sách Lớp Học**:
   - Click nút "In" hoặc "Print" trên giao diện
   - Một trang in đẹp sẽ hiển thị
   - Sử dụng Ctrl+P hoặc Command+P để in tài liệu

### Quản Lý Loại Đào Tạo:

1. **Xem danh sách Loại Đào Tạo**:
   - Truy cập `/education-types` trên giao diện web
   - Hoặc gọi API `GET /api/education-types`

2. **Tìm kiếm Loại Đào Tạo**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo tên loại đào tạo

3. **Tạo Loại Đào Tạo mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền tên loại đào tạo (ví dụ: Chính quy, Không chính quy)
   - Click "Lưu"

4. **Sửa Loại Đào Tạo**:
   - Click nút "Sửa" trên dòng loại đào tạo cần chỉnh sửa
   - Cập nhật thông tin
   - Click "Lưu"

5. **Xoá Loại Đào Tạo**:
   - Click nút "Xoá" trên dòng loại đào tạo cần xoá
   - Xác nhận xoá

6. **Kích hoạt/Vô hiệu hóa Loại Đào Tạo**:
   - Click biểu tượng trạng thái bên cạnh loại đào tạo
   - Chọn "Kích hoạt" hoặc "Vô hiệu hóa"

### Quản Lý Bậc Đào Tạo:

1. **Xem danh sách Bậc Đào Tạo**:
   - Truy cập `/training-levels` trên giao diện web
   - Hoặc gọi API `GET /api/training-levels`

2. **Tìm kiếm Bậc Đào Tạo**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo tên bậc đào tạo

3. **Tạo Bậc Đào Tạo mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền thông tin:
     - Tên bậc đào tạo (ví dụ: Đại học, Cao đẳng)
     - Mô tả bậc đào tạo (tùy chọn)
   - Click "Lưu"

4. **Sửa Bậc Đào Tạo**:
   - Click nút "Sửa" trên dòng bậc đào tạo cần chỉnh sửa
   - Cập nhật thông tin
   - Click "Lưu"

5. **Xoá Bậc Đào Tạo**:
   - Click nút "Xoá" trên dòng bậc đào tạo cần xoá
   - Xác nhận xoá

### Quản Lý Sinh Viên:

1. **Xem danh sách Sinh Viên**:
   - Truy cập `/students` trên giao diện web
   - Hoặc gọi API `GET /api/students`

2. **Tìm kiếm Sinh Viên**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo mã sinh viên hoặc tên sinh viên

3. **Tạo Sinh Viên mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền thông tin:
     - Mã sinh viên (ví dụ: K20-001) - phải duy nhất
     - Họ và tên đầy đủ
     - Ngày sinh
     - Giới tính
     - Số CMND/CCCD
     - Email
     - Số điện thoại
     - Địa chỉ
     - Ảnh đại diện (tùy chọn)
     - Chọn lớp học từ dropdown
   - Click "Lưu"

4. **Sửa Sinh Viên**:
   - Click nút "Sửa" trên dòng sinh viên cần chỉnh sửa
   - Cập nhật thông tin
   - Click "Lưu"

5. **Xoá Sinh Viên**:
   - Click nút "Xoá" trên dòng sinh viên cần xoá
   - Xác nhận xoá

6. **Xem Chi Tiết Sinh Viên**:
   - Click vào tên sinh viên hoặc biểu tượng xem chi tiết
   - Hiển thị toàn bộ thông tin sinh viên
   - Bao gồm thông tin lớp học và ngành học

7. **In (Print) Danh sách Sinh Viên**:
   - Click nút "In" hoặc "Print" trên giao diện
   - Một trang in đẹp sẽ hiển thị
   - Sử dụng Ctrl+P hoặc Command+P để in tài liệu

### Tính Năng Upload Hình Ảnh:

1. **Upload Ảnh Đại Diện Sinh Viên**:
   - Khi tạo hoặc sửa thông tin sinh viên
   - Click nút "Chọn ảnh" hoặc kéo thả ảnh vào vùng upload
   - Hỗ trợ các định dạng: JPG, PNG, GIF
   - Kích thước tối đa: 5MB
   - Ảnh sẽ được lưu và hiển thị trong danh sách sinh viên

2. **Quản Lý Ảnh Đã Upload**:
   - Xem ảnh đại diện trên danh sách sinh viên
   - Thay đổi ảnh bằng cách upload ảnh mới
   - Xóa ảnh bằng cách xoá thông tin sinh viên

3. **Lưu Trữ Ảnh**:
   - Ảnh được lưu trên server với tên file duy nhất
   - Đường dẫn ảnh được lưu trong database
   - Có thể truy cập ảnh thông qua API hoặc giao diện web

### Quản Lý Giảng Viên:

1. **Xem danh sách Giảng Viên**:
   - Truy cập `/lecturers` trên giao diện web
   - Hoặc gọi API `GET /api/lecturers`

2. **Tìm kiếm Giảng Viên**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo mã giảng viên hoặc tên giảng viên

3. **Tạo Giảng Viên mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền thông tin:
     - Mã giảng viên (ví dụ: GV001) - phải duy nhất
     - Họ và tên đầy đủ
     - Ngày sinh
     - Giới tính
     - Số CMND/CCCD
     - Email
     - Số điện thoại
     - Địa chỉ
     - Ảnh đại diện (tùy chọn)
     - Học vị (Cử nhân, Thạc sĩ, Tiến sĩ)
     - Học hàm (Giảng viên, Phó giáo sư, Giáo sư)
     - Chọn khoa từ dropdown
   - Click "Lưu"

4. **Sửa Giảng Viên**:
   - Click nút "Sửa" trên dòng giảng viên cần chỉnh sửa
   - Cập nhật thông tin
   - Click "Lưu"

5. **Xoá Giảng Viên**:
   - Click nút "Xoá" trên dòng giảng viên cần xoá
   - Xác nhận xoá

6. **Xem Chi Tiết Giảng Viên**:
   - Click vào tên giảng viên hoặc biểu tượng xem chi tiết
   - Hiển thị toàn bộ thông tin giảng viên
   - Bao gồm thông tin khoa

7. **In (Print) Danh sách Giảng Viên**:
   - Click nút "In" hoặc "Print" trên giao diện
   - Một trang in đẹp sẽ hiển thị
   - Sử dụng Ctrl+P hoặc Command+P để in tài liệu

### Quản Lý Chức Danh:

1. **Xem danh sách Chức Danh**:
   - Truy cập `/positions` trên giao diện web
   - Hoặc gọi API `GET /api/positions`

2. **Tìm kiếm Chức Danh**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo mã chức danh hoặc tên chức danh

3. **Tạo Chức Danh mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền thông tin:
     - Mã chức danh (ví dụ: POS001) - phải duy nhất
     - Tên chức danh (ví dụ: Trưởng Bộ Môn) - phải duy nhất
     - Mô tả chức danh (tùy chọn)
   - Click "Lưu"

4. **Sửa Chức Danh**:
   - Click nút "Sửa" trên dòng chức danh cần chỉnh sửa
   - Cập nhật thông tin
   - Click "Lưu"

5. **Xoá Chức Danh**:
   - Click nút "Xoá" trên dòng chức danh cần xoá
   - Xác nhận xoá

6. **Sắp xếp Danh sách**:
   - Danh sách chức danh được sắp xếp theo tên chức danh tự động

7. **Xuất (Export) Chức Danh ra Excel**:
   - Click nút "Xuất Excel" trên giao diện
   - Hoặc gọi API `GET /api/positions/export`
   - File Excel sẽ được tải xuống tự động với 3 cột: Mã chức danh, Tên chức danh, Mô tả

8. **Nhập (Import) Chức Danh từ Excel**:
   - Chuẩn bị file Excel với 3 cột: Mã chức danh, Tên chức danh, Mô tả
   - Click nút "Nhập Excel" trên giao diện
   - Chọn file Excel từ máy tính
   - Hệ thống sẽ import và hiển thị số lượng chức danh được thêm
   - Hoặc gọi API `POST /api/positions/import` với file Excel

9. **In (Print) Danh sách Chức Danh**:
   - Click nút "In" hoặc "Print" trên giao diện
   - Một trang in đẹp sẽ hiển thị
   - Sử dụng Ctrl+P hoặc Command+P để in tài liệu
### Quản Lý học phần:

1. **Xem danh sách học phần**:
   - Truy cập `/positions` trên giao diện web
   - Hoặc gọi API `GET /api/positions`

2. **Tìm kiếm học phần**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo mã học phần hoặc tên học phần

3. **Tạo học phần mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền thông tin:
     - Mã học phần (ví dụ: POS001) - phải duy nhất
     - Tên học phần (ví dụ: toán cao cấp) - phải duy nhất
     - Số tín chỉ
     - Số tiết lý thuyết
     - Số tiết thực hành
     - Khoa
     - Trạng thái
     - Mô tả học phần 

   - Click "Lưu"

4. **Sửa học phần**:
   - Click nút "Sửa" trên dòng học phần cần chỉnh sửa
   - Cập nhật thông tin
   - Click "Lưu"

5. **Xo�� học phần**:
   - Click nút "Xoá" trên dòng học phần cần xoá
   - Xác nhận xoá

6. **Sắp xếp Danh sách**:
   - Danh sách học phần được sắp xếp theo tên học phần tự động

7. **Xuất (Export) học phần ra Excel**:
   - Click nút "Xuất Excel" trên giao diện
   - Hoặc gọi API `GET /api/courses/export`
   - File Excel sẽ được tải xuống tự động với 3 cột: Mã học phần, Tên học phần, Mô tả

8. **Nhập (Import) học phần từ Excel**:
   - Chuẩn bị file Excel với 3 cột: Mã học phần, Tên học phần, Mô tả
   - Click nút "Nhập Excel" trên giao diện
   - Chọn file Excel từ máy tính
   - Hệ thống sẽ import và hiển thị số lượng học phần được thêm
   - Hoặc gọi API `POST /api/courses/import` với file Excel

9. **In (Print) Danh sách học phần**:
   - Click nút "In" hoặc "Print" trên giao diện
   - Một trang in đẹp sẽ hiển thị
   - Sử dụng Ctrl+P hoặc Command+P để in tài liệu

### Quản Lý Toà Nhà:

1. **Xem danh sách Toà Nhà**:
   - Truy cập `/admin/buildings` trên giao diện web
   - Hoặc gọi API `GET /api/buildings`

2. **Tìm kiếm Toà Nhà**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo mã toà nhà hoặc tên toà nhà

3. **Tạo Toà Nhà mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền thông tin:
     - Mã toà nhà (phải duy nhất, ví dụ: A1, B2)
     - Tên toà nhà
     - Địa chỉ (tùy chọn)
     - Số tầng
     - Tổng diện tích (m²)
     - Mô tả (tùy chọn)
   - Click "Lưu"

4. **Sửa Toà Nhà**:
   - Click nút "Sửa" trên dòng toà nhà cần chỉnh sửa
   - Cập nhật thông tin
   - Click "Lưu"

5. **Xoá Toà Nhà**:
   - Click nút "Xoá" trên dòng toà nhà cần xoá
   - Xác nhận xoá

6. **In (Print) Danh sách Toà Nhà**:
   - Click nút "In" hoặc "Print" trên giao diện
   - Một trang in đẹp sẽ hiển thị
   - Sử dụng Ctrl+P hoặc Command+P để in tài liệu

### Quản Lý Loại Phòng:

1. **Xem danh sách Loại Phòng**:
   - Truy cập `/admin/room-types` trên giao diện web
   - Hoặc gọi API `GET /api/room-types`

2. **Tìm kiếm Loại Phòng**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo mã loại phòng hoặc tên loại phòng

3. **Tạo Loại Phòng mới**:
   - Click nút "Thêm mới" trên giao diện
   - Điền thông tin:
     - Mã loại phòng (phải duy nhất, ví dụ: PH, PTN, HT)
     - Tên loại phòng (ví dụ: Phòng học, Phòng thí nghiệm)
     - Mô tả (tùy chọn)
     - Sức chứa tối đa (số người, phải lớn hơn 0)
   - Click "Lưu"

4. **Sửa Loại Phòng**:
   - Click nút "Sửa" trên dòng loại phòng cần chỉnh sửa
   - Cập nhật thông tin
   - Click "Lưu"

5. **Xoá Loại Phòng**:
   - Click nút "Xoá" trên dòng loại phòng cần xoá
   - Xác nhận xoá

6. **Xuất (Export) Loại Phòng ra Excel**:
   - Click nút "Xuất Excel" trên giao diện
   - Hoặc gọi API `GET /api/room-types/export`
   - File Excel sẽ được tải xuống tự động với 4 cột: Mã, Tên, Mô tả, Sức chứa

7. **Nhập (Import) Loại Phòng từ Excel**:
   - Chuẩn bị file Excel với 4 cột: Mã loại phòng, Tên loại phòng, Mô tả, Sức chứa tối đa
   - Click nút "Nhập Excel" trên giao diện
   - Chọn file Excel từ máy tính
   - Hoặc gọi API `POST /api/room-types/import` với file Excel

8. **In (Print) Danh sách Loại Phòng**:
   - Click nút "In" hoặc "Print" trên giao diện
   - Một trang in đẹp sẽ hiển thị
   - Sử dụng Ctrl+P hoặc Command+P để in tài liệu

### Quản Lý Học Phần Tiên Quyết:

1. **Xem danh sách Học Phần Tiên Quyết**:
   - Truy cập `/admin/course-prerequisites` trên giao diện web
   - Hoặc gọi API `GET /api/course-prerequisites`

2. **Tìm kiếm Học Phần Tiên Quyết**:
   - Sử dụng thanh tìm kiếm trên giao diện
   - Hỗ trợ tìm kiếm theo mã hoặc tên học phần (cả học phần chính lẫn tiên quyết)

3. **Tạo Quan Hệ Tiên Quyết mới**:
   - Click nút "Thêm mới" trên giao diện
   - Chọn học phần chính từ dropdown
   - Chọn học phần tiên quyết từ dropdown
   - Lưu ý: Không thể chọn cùng một học phần cho cả hai
   - Click "Lưu"

4. **Sửa Quan Hệ Tiên Quyết**:
   - Click nút "Sửa" trên dòng quan hệ cần chỉnh sửa
   - Thay đổi học phần hoặc học phần tiên quyết
   - Click "Lưu"

5. **Xoá Quan Hệ Tiên Quyết**:
   - Click nút "Xoá" trên dòng quan hệ cần xoá
   - Xác nhận xoá

6. **Xem tiên quyết của một Học Phần**:
   - Gọi API `GET /api/course-prerequisites/by-course/{courseId}`
   - Trả về danh sách tất cả học phần tiên quyết của học phần đó

7. **Cập nhật hàng loạt tiên quyết**:
   - Gọi API `PUT /api/course-prerequisites/by-course/{courseId}`
   - Body: mảng các UUID học phần tiên quyết
   - Hệ thống sẽ xoá toàn bộ tiên quyết cũ và thêm tiên quyết mới

8. **Xuất (Export) Học Phần Tiên Quyết ra Excel**:
   - Hoặc gọi API `GET /api/course-prerequisites/export`
   - File Excel sẽ được tải xuống với 4 cột: Mã học phần, Tên học phần, Mã tiên quyết, Tên tiên quyết

9. **Nhập (Import) Học Phần Tiên Quyết từ Excel**:
   - Chuẩn bị file Excel với 3 cột: Mã học phần, Tên học phần, Mã học phần tiên quyết
   - Gọi API `POST /api/course-prerequisites/import` với file Excel

10. **In (Print) Danh sách Học Phần Tiên Quyết**:
    - Click nút "In" hoặc "Print" trên giao diện
    - Hoặc gọi API `GET /api/course-prerequisites/print`
    - Sử dụng Ctrl+P hoặc Command+P để in tài liệu

### Quản Lý Lớp Học Phần:

1. **Xem danh sách Lớp học phần**: Truy cập `/admin/class-sections`
2. **Tìm kiếm**: Gõ vào ô tìm (mã lớp, tên lớp, môn học, học kỳ) rồi bấm Tìm
3. **Thêm lớp mới**: Bấm "Thêm mới" → Chọn môn học, học kỳ, điền mã lớp, tên lớp, sĩ số tối đa, chọn phòng (nếu có) → Lưu
4. **Sửa / Xóa**: Bấm nút tương ứng trên từng dòng
5. **Import / Export**: Dùng form Import hoặc nút Export Excel trên trang danh sách
6. **In**: Bấm "In" để mở trang in (Ctrl+P để in ra giấy)

### Quản Lý Đăng Ký Học Phần:

1. **Xem danh sách đăng ký**: Truy cập `/admin/course-registrations`
2. **Tìm kiếm**: Tìm theo mã SV, tên SV, mã lớp, môn học, học kỳ
3. **Thêm đăng ký mới**: Bấm "Thêm mới" → Chọn sinh viên + lớp học phần (chỉ hiển thị lớp đang mở và còn chỗ) → Lưu
4. **Sửa / Huỷ đăng ký**: Dùng nút Sửa hoặc Xóa; khi huỷ đăng ký hệ thống tự giảm sĩ số lớp
5. **Import Excel**: File Excel gồm 3 cột: Mã sinh viên, Mã lớp học phần, Ghi chú (cột 3 có thể để trống)
6. **Export / In**: Xuất Excel hoặc in danh sách từ nút trên trang

### Quản Lý Phân Công Giảng Viên:

1. **Xem danh sách phân công**: Truy cập `/admin/lecturer-course-classes`
2. **Thêm phân công**: Chọn lớp học phần + giảng viên, có thể thêm ghi chú
3. **Sửa / Xóa**: Dùng nút trên từng dòng
4. **Import / Export / Print**: Tương tự các module khác, file Excel dùng mã lớp và mã giảng viên

### 15. Quản Lý Thiết Bị (Equipment Management)

Chức năng quản lý thiết bị cho phép quản trị viên tạo, sửa, xoá và theo dõi trạng thái các thiết bị trong hệ thống. Mỗi thiết bị có thể được liên kết với một phòng học cụ thể.

#### Các tính năng chi tiết:

- **Danh sách Thiết Bị**: Xem toàn bộ danh sách thiết bị trong hệ thống
- **Tìm kiếm**: Tìm kiếm thiết bị theo mã thiết bị hoặc tên thiết bị (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo thiết bị mới với mã, tên, serial number, ngày mua và trạng thái
- **Sửa**: Chỉnh sửa thông tin thiết bị đã tồn tại
- **Xoá**: Xoá thiết bị khỏi hệ thống
- **Liên kết Phòng Học**: Thiết bị có thể được gắn với một phòng học cụ thể
- **Duy nhất**: Mã thiết bị (`equipmentCode`) phải duy nhất trong hệ thống
- **Quản lý Trạng thái**: Ba trạng thái `ACTIVE`, `BROKEN`, `MAINTENANCE`
- **Theo Dõi Thời Gian**: Tự động ghi nhận thời gian tạo (`createdAt`) và cập nhật (`updatedAt`) qua `@PrePersist`, `@PreUpdate`

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/equipments` | Lấy danh sách thiết bị (có phân trang, tìm kiếm) |
| GET | `/api/equipments/{id}` | Lấy chi tiết thiết bị theo ID |
| POST | `/api/equipments` | Tạo thiết bị mới |
| PUT | `/api/equipments/{id}` | Cập nhật thiết bị |
| DELETE | `/api/equipments/{id}` | Xoá thiết bị |
| GET | `/api/equipments/print` | Lấy tất cả thiết bị (dành cho print) |
| GET | `/api/equipments/export` | Xuất danh sách thiết bị ra Excel |
| POST | `/api/equipments/import` | Nhập danh sách thiết bị từ Excel |

#### Giao diện Admin:

- Danh sách: `/admin/equipments` (tìm kiếm, phân trang, import/export/print)
- Form tạo mới: `/admin/equipments/new`
- Form chỉnh sửa: `/admin/equipments/{id}/edit`
- In: `/admin/equipments/print`

#### Cấu trúc Entity Equipment:

```java
@Entity
@Table(
        name = "equipments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "equipment_code")
        }
)
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long equipmentId;              // ID tự tăng

    @Column(name = "equipment_code", nullable = false, length = 50)
    private String equipmentCode;          // Mã thiết bị (duy nhất)

    @Column(name = "equipment_name", nullable = false)
    private String equipmentName;          // Tên thiết bị

    @Column(name = "serial_number")
    private String serialNumber;           // Số serial

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;        // Ngày mua

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private EquipmentStatus status;        // Trạng thái: ACTIVE, BROKEN, MAINTENANCE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;                     // Phòng học liên kết

    private LocalDateTime createdAt;       // Thời gian tạo
    private LocalDateTime updatedAt;       // Thời gian cập nhật
}
```

#### Enum EquipmentStatus:

```java
public enum EquipmentStatus {
    ACTIVE,       // Đang hoạt động
    BROKEN,       // Hỏng
    MAINTENANCE   // Đang bảo trì
}
```

#### Request/Response Model:

**EquipmentRequest** (Tạo/Cập nhật thiết bị):
```json
{
  "equipmentCode": "TB001",
  "equipmentName": "Máy chiếu Epson",
  "serialNumber": "SN-2024-001",
  "purchaseDate": "2024-01-15",
  "status": "ACTIVE",
  "roomId": 1
}
```

**EquipmentResponse** (Phản hồi từ server):
```json
{
  "equipmentId": 1,
  "equipmentCode": "TB001",
  "equipmentName": "Máy chiếu Epson",
  "serialNumber": "SN-2024-001",
  "purchaseDate": "2024-01-15",
  "status": "ACTIVE",
  "roomId": 1,
  "roomCode": "P101",
  "roomName": "Phòng học 101",
  "createdAt": "2026-02-28T10:00:00",
  "updatedAt": "2026-02-28T10:00:00"
}
```

#### Service Layer:

Class `EquipmentService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm thiết bị theo mã hoặc tên với phân trang
- `getById(id)`: Lấy chi tiết thiết bị theo ID
- `create(request)`: Tạo thiết bị mới
- `update(id, request)`: Cập nhật thiết bị
- `delete(id)`: Xoá thiết bị
- `getForPrint()`: Lấy tất cả thiết bị (dành cho in ấn)
- `importExcel(file)`: Nhập danh sách thiết bị từ file Excel
- `exportExcel()`: Xuất danh sách thiết bị ra file Excel

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Mã thiết bị phải duy nhất trong hệ thống (kiểm tra cả khi tạo và cập nhật)
- **Validate Dữ Liệu**: Mã thiết bị, tên thiết bị và trạng thái không được để trống
- **Kiểm tra Phòng Học**: Phòng học phải tồn tại nếu được cung cấp `roomId`
- **Trạng Thái Mặc Định**: Nếu không cung cấp trạng thái, mặc định là `ACTIVE`

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất danh sách thiết bị ra file Excel (`.xlsx`)
- Tên file: `equipments.xlsx`
- Có thể xuất từ API hoặc giao diện web

**Import (Nhập dữ liệu):**
- Nhập danh sách thiết bị từ file Excel
- Hỗ trợ nhập hàng loạt

**Print (In ấn):**
- In danh sách thiết bị từ giao diện web (`/admin/equipments/print`)
- Định dạng in đẹp và dễ đọc
- Hỗ trợ in từ trình duyệt

#### Controller:

- **EquipmentController**: Quản lý API endpoints (CRUD, Import/Export/Print) tại `/api/equipments`
- **EquipmentDashboardController**: Quản lý views HTML cho giao diện web tại `/admin/equipments`

### 16. Quản Lý Phân Công Giảng Viên (Lecturer Course Class Assignment Management)

Chức năng quản lý phân công giảng viên cho phép gán giảng viên vào các lớp học phần đang mở trong học kỳ, đảm bảo mỗi lớp học phần được phụ trách bởi giảng viên phù hợp.

#### Các tính năng chi tiết:

- **Danh sách Phân Công**: Xem toàn bộ danh sách phân công giảng viên trong hệ thống
- **Tìm kiếm**: Tìm kiếm theo mã lớp, tên lớp, mã giảng viên hoặc tên giảng viên (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Phân công giảng viên cho lớp học phần, kèm ghi chú tuỳ chọn
- **Sửa**: Chỉnh sửa thông tin phân công (đổi giảng viên, cập nhật ghi chú)
- **Xoá**: Huỷ phân công giảng viên khỏi lớp học phần
- **Kiểm tra trùng lặp**: Ngăn phân công cùng một giảng viên vào cùng một lớp học phần hai lần
- **Ghi chú**: Cho phép thêm ghi chú cho mỗi lần phân công (tối đa 500 ký tự)

#### Endpoint API:

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/api/lecturer-course-classes` | Tìm kiếm phân công với phân trang (`keyword`, `page`, `size`) |
| GET | `/api/lecturer-course-classes/{id}` | Lấy chi tiết phân công theo ID |
| POST | `/api/lecturer-course-classes` | Tạo phân công mới |
| PUT | `/api/lecturer-course-classes/{id}` | Cập nhật phân công |
| DELETE | `/api/lecturer-course-classes/{id}` | Xoá phân công |
| GET | `/api/lecturer-course-classes/print` | Lấy danh sách để in (sắp xếp theo mã lớp và mã giảng viên) |
| POST | `/api/lecturer-course-classes/import` | Nhập phân công từ file Excel |
| GET | `/api/lecturer-course-classes/export` | Xuất danh sách phân công ra file Excel |

#### Giao diện Web (Dashboard):

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/admin/lecturer-course-classes` | Trang danh sách phân công |
| GET | `/admin/lecturer-course-classes/new` | Form tạo phân công mới |
| POST | `/admin/lecturer-course-classes` | Xử lý tạo phân công mới |
| GET | `/admin/lecturer-course-classes/{id}/edit` | Form chỉnh sửa phân công |
| POST | `/admin/lecturer-course-classes/{id}` | Xử lý cập nhật phân công |
| POST | `/admin/lecturer-course-classes/{id}/delete` | Xử lý xoá phân công |
| GET | `/admin/lecturer-course-classes/print` | Trang in danh sách phân công |
| POST | `/admin/lecturer-course-classes/import` | Xử lý import từ Excel |
| GET | `/admin/lecturer-course-classes/export` | Tải file Excel xuất dữ liệu |

#### Cấu trúc Entity LecturerCourseClass:

```java
@Entity
@Table(
    name = "lecturer_course_classes",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"class_section_id", "lecturer_id"})
    }
)
public class LecturerCourseClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // ID phân công

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_section_id", nullable = false)
    private ClassSection classSection;  // Lớp học phần được phân công

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;          // Giảng viên được phân công

    @Column(name = "note", columnDefinition = "NVARCHAR(500)")
    private String note;                // Ghi chú (tối đa 500 ký tự)

    private LocalDateTime createdAt;    // Thời gian tạo (tự động)
    private LocalDateTime updatedAt;    // Thời gian cập nhật (tự động)
}
```

#### Request/Response Model:

**LecturerCourseClassRequest** (Tạo/Cập nhật phân công):
```json
{
  "classSectionId": 1,
  "lecturerId": "uuid-lecturer",
  "note": "Giảng viên phụ trách chính"
}
```

**LecturerCourseClassResponse** (Phản hồi từ server):
```json
{
  "id": 1,
  "classSectionId": 1,
  "classCode": "CS101-01",
  "className": "Nhập môn lập trình - Lớp 1",
  "courseCode": "CS101",
  "courseName": "Nhập môn lập trình",
  "semesterCode": "HK1-2025",
  "lecturerId": "uuid-lecturer",
  "lecturerCode": "GV001",
  "lecturerName": "Nguyễn Văn A",
  "facultyName": "Khoa Công Nghệ Thông Tin",
  "note": "Giảng viên phụ trách chính",
  "createdAt": "2025-01-01T08:00:00",
  "updatedAt": "2025-01-01T08:00:00"
}
```

#### Service Layer:

Class `LecturerCourseClassService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm phân công với phân trang
- `getById(id)`: Lấy chi tiết phân công theo ID
- `create(request)`: Tạo phân công mới (kiểm tra trùng lặp)
- `update(id, request)`: Cập nhật phân công (kiểm tra trùng lặp)
- `delete(id)`: Xoá phân công
- `getForPrint()`: Lấy danh sách đã sắp xếp để in
- `importExcel(file)`: Nhập phân công từ file Excel (bỏ qua bản ghi đã tồn tại)
- `exportExcel()`: Xuất toàn bộ phân công ra file Excel

#### Xác Thực & Bảo Mật:

- **Kiểm tra Trùng Lặp**: Mỗi cặp (lớp học phần, giảng viên) chỉ được tồn tại một lần
- **Validate Dữ Liệu**: Lớp học phần và giảng viên không được để trống
- **Giới Hạn Ghi Chú**: Trường ghi chú tối đa 500 ký tự
- **Kiểm tra Tồn Tại**: Xác minh lớp học phần và giảng viên tồn tại trước khi phân công

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất danh sách phân công ra file Excel (`lecturer_course_classes.xlsx`)
- Các cột: Class Code, Class Name, Course Code, Course Name, Semester Code, Lecturer Code, Lecturer Name, Faculty, Note
- Sắp xếp theo mã lớp và mã giảng viên

**Import (Nhập dữ liệu):**
- Nhập từ file Excel với 3 cột: Mã lớp học phần, Mã giảng viên, Ghi chú
- Tự động bỏ qua các bản ghi đã tồn tại (không báo lỗi)
- Báo lỗi kèm số dòng khi không tìm thấy lớp học phần hoặc giảng viên

**Print (In ấn):**
- In danh sách phân công đầy đủ từ giao diện web
- Dữ liệu sắp xếp theo mã lớp học phần rồi đến mã giảng viên

#### Controller:

- **LecturerCourseClassController**: Quản lý API endpoints (CRUD, Import/Export/Print) tại `/api/lecturer-course-classes`
- **LecturerCourseClassDashboardController**: Quản lý views HTML cho giao diện web tại `/admin/lecturer-course-classes`

---

### 16. Đăng Ký Học Phần (Course Registration Management)

Chức năng quản lý đăng ký học phần cho phép ghi nhận việc sinh viên đăng ký vào các lớp học phần đang mở trong học kỳ, với đầy đủ kiểm tra nghiệp vụ và hỗ trợ import/export Excel.

#### Các tính năng chi tiết:

- **Danh sách đăng ký**: Xem toàn bộ danh sách đăng ký học phần, sắp xếp theo thời gian đăng ký mới nhất
- **Tìm kiếm**: Tìm kiếm theo mã sinh viên, tên sinh viên, mã lớp học phần, mã/tên học phần, mã học kỳ
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Đăng ký sinh viên vào lớp học phần với kiểm tra điều kiện nghiệp vụ
- **Sửa**: Cập nhật thông tin đăng ký (sinh viên, lớp học phần, ghi chú)
- **Xoá**: Huỷ đăng ký và tự động giảm sĩ số hiện tại của lớp học phần
- **Import Excel**: Nhập hàng loạt đăng ký từ file Excel
- **Export Excel**: Xuất toàn bộ danh sách đăng ký ra file Excel
- **In ấn**: In danh sách đăng ký học phần từ giao diện web

#### Kiểm tra nghiệp vụ khi đăng ký:

- **Trạng thái lớp**: Chỉ cho phép đăng ký vào lớp học phần đang mở (không phải `CLOSED` hoặc `CANCELLED`)
- **Sĩ số tối đa**: Không cho phép đăng ký khi lớp học phần đã đủ số sinh viên tối đa
- **Trùng lặp**: Mỗi sinh viên chỉ được đăng ký một lần vào cùng một lớp học phần
- **Cập nhật sĩ số**: Tự động tăng/giảm `currentStudents` của lớp học phần khi đăng ký hoặc huỷ

#### Endpoint (Web Dashboard):

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/admin/course-registrations` | Xem danh sách đăng ký (tìm kiếm, phân trang) |
| GET | `/admin/course-registrations/new` | Form thêm đăng ký mới |
| POST | `/admin/course-registrations` | Lưu đăng ký mới |
| GET | `/admin/course-registrations/{id}/edit` | Form chỉnh sửa đăng ký |
| POST | `/admin/course-registrations/{id}` | Cập nhật đăng ký |
| POST | `/admin/course-registrations/{id}/delete` | Xoá đăng ký |
| GET | `/admin/course-registrations/print` | In danh sách đăng ký |
| POST | `/admin/course-registrations/import` | Import từ file Excel |
| GET | `/admin/course-registrations/export` | Export ra file Excel |

#### Cấu trúc Entity CourseRegistration:

```java
@Entity
@Table(
    name = "course_registrations",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "class_section_id"})
    }
)
public class CourseRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                        // ID đăng ký

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;                // Sinh viên đăng ký

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_section_id", nullable = false)
    private ClassSection classSection;      // Lớp học phần được đăng ký

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;     // Thời gian đăng ký (tự động)

    @Column(name = "note", columnDefinition = "NVARCHAR(500)")
    private String note;                    // Ghi chú (tối đa 500 ký tự)
}
```

#### Request/Response Model:

**CourseRegistrationRequest** (Tạo/Cập nhật đăng ký):
```json
{
  "studentId": "uuid-student",
  "classSectionId": 1,
  "note": "Đăng ký học kỳ chính thức"
}
```

**CourseRegistrationResponse** (Phản hồi từ server):
```json
{
  "id": 1,
  "studentId": "uuid-student",
  "studentCode": "SV001",
  "studentName": "Nguyễn Văn A",
  "classSectionId": 1,
  "classCode": "CS101-01",
  "className": "Nhập môn lập trình - Lớp 1",
  "courseCode": "CS101",
  "courseName": "Nhập môn lập trình",
  "semesterCode": "HK1-2025",
  "semesterName": "Học kỳ 1 năm 2025",
  "registeredAt": "2025-09-01T08:00:00",
  "note": "Đăng ký học kỳ chính thức"
}
```

#### Service Layer:

Class `CourseRegistrationService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm đăng ký với phân trang
- `getById(id)`: Lấy chi tiết đăng ký theo ID
- `create(request)`: Tạo đăng ký mới (kiểm tra đầy đủ nghiệp vụ)
- `update(id, request)`: Cập nhật đăng ký (kiểm tra nghiệp vụ khi đổi lớp)
- `delete(id)`: Huỷ đăng ký và cập nhật sĩ số lớp
- `getForPrint()`: Lấy danh sách sắp xếp để in
- `importExcel(file)`: Nhập đăng ký từ file Excel (bỏ qua bản ghi đã tồn tại)
- `exportExcel()`: Xuất toàn bộ đăng ký ra file Excel

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất danh sách đăng ký ra file Excel (`course_registrations.xlsx`)
- Các cột: Student Code, Student Name, Class Code, Class Name, Course Code, Course Name, Semester Code, Registered At, Note
- Sắp xếp theo thời gian đăng ký mới nhất

**Import (Nhập dữ liệu):**
- Nhập từ file Excel với 3 cột: Mã sinh viên, Mã lớp học phần, Ghi chú
- Tự động bỏ qua các bản ghi đã tồn tại
- Kiểm tra đầy đủ nghiệp vụ (trạng thái lớp, sĩ số) cho từng dòng
- Báo lỗi kèm số dòng khi không tìm thấy sinh viên hoặc lớp học phần

**Print (In ấn):**
- In danh sách đăng ký đầy đủ từ giao diện web
- Dữ liệu sắp xếp theo thời gian đăng ký mới nhất

#### Controller:

- **CourseRegistrationDashboardController**: Quản lý views HTML cho giao diện web tại `/admin/course-registrations` (bao gồm CRUD, Import/Export/Print)

### 17. Quản Lý Khung Giờ (Time Slot Management)

Chức năng quản lý khung giờ cho phép quản trị viên tạo, sửa, xoá và quản lý các khung giờ học trong ngày. Mỗi khung giờ xác định phạm vi tiết học và thời gian bắt đầu/kết thúc tương ứng, được dùng để lập thời khóa biểu.

#### Các tính năng chi tiết:

- **Danh sách Khung Giờ**: Xem toàn bộ danh sách khung giờ trong hệ thống, sắp xếp theo mã khung giờ
- **Tìm kiếm**: Tìm kiếm khung giờ theo mã khung giờ (hỗ trợ tìm kiếm gần đúng)
- **Phân trang**: Hỗ trợ phân trang để dễ dàng xem danh sách
- **Thêm mới**: Tạo khung giờ mới với mã, phạm vi tiết và giờ học
- **Sửa**: Chỉnh sửa thông tin khung giờ đã tồn tại
- **Xoá**: Xoá khung giờ khỏi hệ thống
- **Kích hoạt/Vô hiệu hóa**: Bật/tắt trạng thái hoạt động của khung giờ
- **Sắp xếp tự động**: Danh sách sắp xếp theo mã khung giờ

#### Endpoint Web (Dashboard):

| Method | URL | Mô Tả |
|--------|-----|-------|
| GET | `/admin/time-slots` | Trang danh sách khung giờ (có tìm kiếm & phân trang) |
| GET | `/admin/time-slots/new` | Form thêm mới khung giờ |
| POST | `/admin/time-slots` | Lưu khung giờ mới |
| GET | `/admin/time-slots/{id}/edit` | Form chỉnh sửa khung giờ |
| POST | `/admin/time-slots/{id}` | Cập nhật khung giờ |
| POST | `/admin/time-slots/{id}/delete` | Xoá khung giờ |
| GET | `/admin/time-slots/print` | Trang in danh sách khung giờ |
| GET | `/admin/time-slots/export` | Xuất danh sách ra file Excel |
| POST | `/admin/time-slots/import` | Nhập danh sách từ file Excel |

#### Cấu trúc Entity TimeSlot:

```java
@Entity
@Table(
    name = "time_slots",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "slot_code")
    }
)
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;              // ID tự tăng

    @Column(name = "slot_code", nullable = false, unique = true, length = 20)
    private String slotCode;         // Mã khung giờ (duy nhất, ví dụ: "CA1", "CA2")

    @Column(name = "period_start", nullable = false)
    private Integer periodStart;     // Tiết bắt đầu (ví dụ: 1)

    @Column(name = "period_end", nullable = false)
    private Integer periodEnd;       // Tiết kết thúc (ví dụ: 5)

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;     // Giờ bắt đầu (ví dụ: 07:00)

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;       // Giờ kết thúc (ví dụ: 11:30)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Trạng thái kích hoạt
}
```

#### Request/Response Model:

**TimeSlotRequest** (Tạo/Cập nhật khung giờ):
```json
{
  "slotCode": "CA1",
  "periodStart": 1,
  "periodEnd": 5,
  "startTime": "07:00",
  "endTime": "11:30",
  "isActive": true
}
```

**TimeSlotResponse** (Phản hồi từ server):
```json
{
  "id": 1,
  "slotCode": "CA1",
  "periodStart": 1,
  "periodEnd": 5,
  "startTime": "07:00",
  "endTime": "11:30",
  "isActive": true
}
```

#### Service Layer:

Class `TimeSlotService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm khung giờ theo mã, có phân trang
- `getById(id)`: Lấy chi tiết khung giờ theo ID
- `create(request)`: Tạo khung giờ mới (kiểm tra mã duy nhất, validate tiết và giờ)
- `update(id, request)`: Cập nhật khung giờ (kiểm tra mã duy nhất ngoại trừ bản ghi hiện tại)
- `delete(id)`: Xoá khung giờ theo ID
- `getForPrint()`: Lấy toàn bộ danh sách sắp xếp theo mã (dành cho in ấn)
- `exportExcel()`: Xuất danh sách khung giờ ra file Excel (.xlsx)
- `importExcel(file)`: Nhập danh sách khung giờ từ file Excel (bỏ qua bản ghi đã tồn tại)

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Mã khung giờ (`slotCode`) phải duy nhất trong hệ thống
- **Validate Tiết Học**: Tiết kết thúc phải lớn hơn hoặc bằng tiết bắt đầu
- **Validate Giờ Học**: Giờ kết thúc phải sau giờ bắt đầu
- **Validate Dữ Liệu**: Mã khung giờ, tiết học và giờ học không được để trống
- **Sắp Xếp Tự Động**: Danh sách khung giờ được sắp xếp theo mã khung giờ

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất danh sách khung giờ ra file Excel (`time_slots.xlsx`)
- Các cột: Slot Code, Period Start, Period End, Start Time, End Time, Is Active
- Sắp xếp theo mã khung giờ

**Import (Nhập dữ liệu):**
- Nhập từ file Excel với 6 cột: Slot Code, Period Start, Period End, Start Time, End Time, Is Active
- Tự động bỏ qua các khung giờ đã tồn tại (theo mã khung giờ)
- Hỗ trợ nhiều định dạng giờ: cell số, cell text `HH:mm`
- Hỗ trợ cột Is Active với giá trị: `1`, `0`, `true`, `false`, `x`
- Hỗ trợ nhập hàng loạt

**Print (In ấn):**
- In danh sách khung giờ đầy đủ từ giao diện web
- Dữ liệu sắp xếp theo mã khung giờ

#### Controller:

- **TimeSlotDashboardController**: Quản lý views HTML cho giao diện web tại `/admin/time-slots` (bao gồm CRUD, Import/Export/Print)

---

### 18. Quản Lý Lịch Học (Schedule Management)

Chức năng quản lý lịch học cho phép quản trị viên tạo, sửa, xoá và tra cứu thời khóa biểu của các lớp học phần trong từng học kỳ. Hệ thống hỗ trợ cả lập lịch thủ công lẫn **phân lịch tự động** bằng thuật toán tham lam (greedy), đồng thời cung cấp Import/Export Excel và chức năng in ấn.

#### Thông Tin Lịch Học:

| Trường | Kiểu | Mô Tả |
|---|---|---|
| `id` | UUID | Mã định danh duy nhất |
| `semester` | Semester | Học kỳ áp dụng lịch học |
| `classSection` | ClassSection | Lớp học phần được phân lịch |
| `lecturer` | Lecturer | Giảng viên phụ trách |
| `room` | Room | Phòng học |
| `timeSlot` | TimeSlot | Khung giờ học |
| `dayOfWeek` | Integer | Thứ trong tuần (2 = Thứ 2, …, 8 = Chủ nhật) |
| `startWeek` | Integer | Tuần bắt đầu |
| `endWeek` | Integer | Tuần kết thúc |
| `weekPattern` | Enum | Kiểu tuần: `ALL` (tất cả), `ODD` (lẻ), `EVEN` (chẵn) |
| `sessionType` | Enum | Loại buổi: `THEORY` (lý thuyết), `PRACTICE` (thực hành), `EXAM` (thi) |
| `scheduleType` | Enum | Loại lịch: `NORMAL` (bình thường), `MAKEUP` (học bù), `EXTRA` (học thêm) |
| `status` | Enum | Trạng thái: `ACTIVE` (hoạt động), `CANCELLED` (đã hủy), `MOVED` (đã chuyển) |
| `note` | String | Ghi chú (tối đa 255 ký tự) |

#### Chức Năng Chính:

- **Xem Danh Sách**: Hiển thị toàn bộ lịch học với phân trang, tìm kiếm theo từ khóa; sắp xếp theo thứ trong tuần và mã khung giờ
- **Thêm Mới**: Tạo lịch học thủ công, chọn đầy đủ học kỳ, lớp học phần, giảng viên, phòng, khung giờ, tuần học và loại buổi
- **Chỉnh Sửa**: Cập nhật thông tin một lịch học đã tồn tại
- **Xóa**: Xóa lịch học theo ID
- **In Ấn**: Xem và in toàn bộ thời khóa biểu qua trang print, sắp xếp theo học kỳ → thứ → khung giờ

#### Phân Lịch Tự Động (Auto Schedule):

Hệ thống cung cấp tính năng phân lịch tự động theo **thuật toán tham lam (greedy)**:

1. **Đầu vào**: Chọn học kỳ, phạm vi tuần (mặc định tuần 1–15) và tùy chọn chỉ định một số lớp học phần cụ thể
2. **Nguồn dữ liệu**: Lấy danh sách phân công giảng viên (`LecturerCourseClass`) của học kỳ, chỉ xử lý các lớp học phần có trạng thái `OPEN`
3. **Xác định số buổi**: Dựa trên số tiết lý thuyết và thực hành của học phần để tạo các `ScheduleTask` (buổi lý thuyết / thực hành)
4. **Xếp lịch**: Với mỗi buổi, lần lượt duyệt qua các ô (Thứ 2 → Thứ 6) × (khung giờ đang hoạt động) và chọn ô đầu tiên **không trùng** giảng viên, phòng, lớp học phần
5. **Kết quả**: Trả về số buổi đã xếp được và số buổi bỏ qua (do hết ô trống)
6. **Tùy chọn xóa**: Có thể xóa toàn bộ lịch cũ của học kỳ trước khi phân lịch lại

#### Kiểm Tra Nghiệp Vụ:

- **Kiểm Tra Tồn Tại**: Học kỳ, lớp học phần, giảng viên, phòng và khung giờ phải tồn tại trong hệ thống
- **Validate Tuần**: Tuần bắt đầu phải nhỏ hơn hoặc bằng tuần kết thúc
- **Phòng & Khung Giờ**: Phân lịch tự động chỉ dùng phòng và khung giờ đang ở trạng thái hoạt động (`isActive = true`)
- **Giới Hạn Ghi Chú**: Trường ghi chú tối đa 255 ký tự

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất toàn bộ lịch học ra file Excel (`schedules.xlsx`)
- Các cột: Semester Code, Class Code, Lecturer Code, Room Code, Slot Code, Day, Start Week, End Week, Week Pattern, Session Type, Schedule Type, Status, Note

**Import (Nhập dữ liệu):**
- Nhập lịch học hàng loạt từ file Excel theo đúng cấu trúc cột trên
- Bỏ qua dòng thiếu thông tin bắt buộc; báo lỗi cụ thể (số dòng) nếu mã học kỳ/lớp/giảng viên/phòng/khung giờ không tìm thấy

**Print (In ấn):**
- In thời khóa biểu đầy đủ từ giao diện web
- Dữ liệu sắp xếp theo học kỳ → thứ trong tuần → mã khung giờ

#### Controller:

- **ScheduleDashboardController**: Quản lý views HTML cho giao diện web tại `/admin/schedules` (bao gồm CRUD, Import/Export/Print, Auto Schedule)

---

### 19. Quản Lý Thay Đổi Lịch - Dạy Bù / Đổi Phòng (Schedule Overrides)

Module **schedule_overrides** dùng để quản lý các thay đổi áp dụng cho một buổi lịch cụ thể: dạy bù, đổi phòng, đổi khung giờ hoặc hủy buổi. Mỗi bản ghi gắn với một lịch gốc (`schedule_id`), một ngày áp dụng (`override_date`) và loại thay đổi (`override_type`).

#### Bảng / Entity Chính:

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `override_id` | UUID (PK) | Mã override |
| `schedule_id` | UUID (FK) | Lịch gốc bị thay đổi |
| `override_date` | DATE | Ngày cụ thể áp dụng thay đổi |
| `override_type` | Enum | `MAKEUP` (Dạy bù), `ROOM_CHANGE` (Đổi phòng), `TIME_CHANGE` (Đổi giờ), `CANCEL` (Hủy buổi) |
| `new_room_id` | FK, nullable | Phòng mới (khi đổi phòng) |
| `new_time_slot_id` | FK, nullable | Khung giờ mới (khi đổi giờ) |
| `new_lecturer_id` | UUID (FK), nullable | Giảng viên thay thế (dạy bù) |
| `status` | Enum | `ACTIVE` (áp dụng), `CANCELLED` (đã hủy override) |
| `reason` | NVARCHAR(255) | Lý do thay đổi |
| `approved_by` | UUID, nullable | Người duyệt |
| `approved_at` | DATETIME2, nullable | Thời điểm duyệt |
| `created_at`, `updated_at` | DATETIME2 | Ngày tạo, cập nhật |

#### Tính Năng:

- **CRUD**: Thêm, xem, sửa, xóa bản ghi thay đổi lịch
- **Phân trang**: Danh sách có phân trang (mặc định 10 bản ghi/trang)
- **Tìm kiếm gần đúng**: Theo lý do, mã học kỳ, lớp, giảng viên, phòng
- **Import Excel**: Nhập hàng loạt từ file `.xlsx` (Schedule ID, Ngày áp dụng, Loại, Phòng mới, Khung giờ mới, GV thay thế, Trạng thái, Lý do)
- **Export Excel**: Xuất danh sách thay đổi lịch ra file `schedule-overrides.xlsx`
- **Print**: Trang in danh sách thay đổi lịch (phục vụ báo cáo / lưu trữ)

#### Nghiệp Vụ:

- **ROOM_CHANGE**: Bắt buộc chọn phòng mới (`new_room_id`)
- **TIME_CHANGE**: Bắt buộc chọn khung giờ mới (`new_time_slot_id`)
- **MAKEUP**: Có thể chỉ định giảng viên thay thế và/hoặc phòng/khung giờ mới

#### URL Giao Diện Admin:

| Chức năng | URL |
|-----------|-----|
| Danh sách | GET `/admin/schedule-overrides` |
| Thêm mới | GET `/admin/schedule-overrides/new`, POST `/admin/schedule-overrides` |
| Chỉnh sửa | GET `/admin/schedule-overrides/{id}/edit`, POST `/admin/schedule-overrides/{id}` |
| Xóa | POST `/admin/schedule-overrides/{id}/delete` |
| In | GET `/admin/schedule-overrides/print` |
| Import | POST `/admin/schedule-overrides/import` (file) |
| Export | GET `/admin/schedule-overrides/export` |

#### Cấu Trúc Code:

- **Entity**: `scheduleoverride.entity.ScheduleOverride`, `OverrideType`, `OverrideStatus`
- **DTO**: `ScheduleOverrideRequest`, `ScheduleOverrideResponse`
- **Repository**: `ScheduleOverrideRepository` (search, findAllForPrint)
- **Service**: `ScheduleOverrideService` (CRUD, search, getForPrint, importExcel, exportExcel)
- **Controller**: `ScheduleOverrideDashboardController` (Thymeleaf tại `/admin/schedule-overrides`)
- **Templates**: `schedule-overrides/index.html`, `form.html`, `print.html`
- **Sidebar**: Mục "Dạy bù / Đổi phòng" với icon lịch, active menu `schedule-overrides`

---

## Tác Giả

**NguyenNgocMinhHieu** - [GitHub](https://github.com/NguyenHieuDavitDev)


## Các Tính Năng Sắp Tới

- [x] Quản lý vai trò (Role Management)
- [x] Quản lý người dùng (User Management)
- [x] Quản lý khoa (Faculty Management)
- [x] Quản lý ngành học (Major Management)
- [x] Quản lý lớp học (Classroom Management)
- [x] Quản lý loại đào tạo (Education Type Management)
- [x] Quản lý bậc đào tạo (Training Level Management)
- [x] Quản lý sinh viên (Student Management)
- [x] Upload hình ảnh (Image Upload)
- [x] Quản lý giảng viên (Lecturer Management)
- [x] Quản lý chức danh (Position Management)
- [x] Quản lý chương trình đào tạo (Training programs Management)
- [x] Quản lý học phần (Courses Management)
- [x] Quản lý toà nhà (Building Management)
- [x] Quản lý loại phòng (Room Type Management)
- [x] Quản lý học phần tiên quyết (Course Prerequisite Management)
- [x] Quản lý lớp học phần (Class Section Management)
- [x] Quản lý thiết bị (Equipment Management)
- [x] Quản lý phân công giảng viên (Lecturer Course Class Assignment Management)
- [x] Đăng ký học phần (Course Registration Management)
- [x] Quản lý khung giờ (Time Slot Management)
- [x] Quản lý lịch học (Schedule Management)
- [x] Quản lý thay đổi lịch - Dạy bù / Đổi phòng (Schedule Overrides)
- [ ] Quản lý phân quyền chi tiết (Permission Management)
- [ ] Xác thực người dùng (Authentication)
- [ ] Mã hóa mật khẩu (Password Encryption)
- [ ] Audit Log
- [ ] Report & Analytics
- [ ] Gửi email thông báo
- [ ] API Documentation (Swagger)


**Phiên bản**: 0.0.1-SNAPSHOT  
**Cập nhật lần cuối**: 03/03/2026 – Bổ sung mô tả Lớp học phần, Đăng ký học phần, cấu trúc dự án và hướng dẫn sử dụng
