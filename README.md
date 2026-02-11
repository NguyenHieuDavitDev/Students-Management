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
- **Thông tin Chi Tiết**: Hỗ trợ lưu thông tin cá nhân đầy đủ (ngày sinh, giới tính, CMND, email, điện thoại, địa chỉ, avatar)
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
        name = "positions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "position_code"),
                @UniqueConstraint(columnNames = "position_name")
        }
)
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID positionId;              // ID duy nhất
    
    @Column(nullable = false, unique = true)
    private String positionCode;          // Mã chức danh (duy nhất)
    
    @Column(nullable = false, unique = true)
    private String positionName;          // Tên chức danh (duy nhất)
    
    @Column
    private String description;           // Mô tả chức danh
}
```

#### Request/Response Model:

**PositionRequest** (Tạo/Cập nhật chức danh):
```json
{
  "positionCode": "POS001",
  "positionName": "Trưởng Bộ Môn",
  "description": "Quản lý bộ môn và giảng dạy"
}
```

**PositionResponse** (Phản hồi từ server):
```json
{
  "positionId": "uuid",
  "positionCode": "POS001",
  "positionName": "Trưởng Bộ Môn",
  "description": "Quản lý bộ môn và giảng dạy"
}
```

#### Service Layer:

Class `PositionService` cung cấp các phương thức:
- `search(keyword, page, size)`: Tìm kiếm chức danh với phân trang
- `getById(id)`: Lấy chức danh theo ID
- `create(request)`: Tạo chức danh mới
- `update(id, request)`: Cập nhật chức danh
- `delete(id)`: Xoá chức danh
- `getAll()`: Lấy tất cả chức danh (dành cho dropdown)
- `getForPrint()`: Lấy tất cả chức danh (dành cho print)
- `exportExcel(response)`: Xuất danh sách chức danh ra file Excel
- `importExcel(file)`: Nhập danh sách chức danh từ file Excel

#### Xác Thực & Bảo Mật:

- **Kiểm tra Tính Duy Nhất**: Mã chức danh và tên chức danh phải duy nhất trong hệ thống
- **Validate Dữ Liệu**: Mã chức danh và tên chức danh không được để trống
- **Sắp Xếp Tự Động**: Danh sách chức danh được sắp xếp theo tên chức danh

#### Tính Năng Import/Export/Print:

**Export (Xuất dữ liệu):**
- Xuất danh sách chức danh ra file Excel (.xlsx)
- File chứa 3 cột: Mã chức danh, Tên chức danh, Mô tả
- Tự động định dạng với header rõ ràng
- Có thể xuất từ API hoặc giao diện web

**Import (Nhập dữ liệu):**
- Nhập danh sách chức danh từ file Excel
- Tự động bỏ qua các chức danh đã tồn tại (theo mã chức danh)
- Trả về số lượng chức danh được import thành công
- Hỗ trợ nhập hàng loạt

**Print (In ấn):**
- In danh sách chức danh từ giao diện web
- Định dạng in đẹp và dễ đọc
- Hỗ trợ in từ trình duyệt

#### Controller:

- **PositionController**: Quản lý API endpoints (CRUD, Import/Export)
- **PositionDashboardController**: Quản lý views HTML cho giao diện web (bao gồm Print)

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
- [ ] Quản lý phân quyền chi tiết (Permission Management)
- [ ] Xác thực người dùng (Authentication)
- [ ] Mã hóa mật khẩu (Password Encryption)
- [ ] Audit Log
- [ ] Report & Analytics
- [ ] Gửi email thông báo
- [ ] API Documentation (Swagger)

---

**Phiên bản**: 0.0.1-SNAPSHOT  
**Cập nhật lần cuối**: 11/02/2026 (Position Management features added)

## Lưu Ý Quan Trọng

Hệ thống này được phát triển để quản lý sinh viên với các tính năng đầy đủ:
- Quản lý vai trò (Role Management)
- Quản lý người dùng (User Management)
- Quản lý khoa (Faculty Management)
- Quản lý ngành học (Major Management)
- Quản lý lớp học (Classroom Management)
- Quản lý loại đào tạo (Education Type Management)
- Quản lý bậc đào tạo (Training Level Management)
- Quản lý sinh viên (Student Management)
- Quản lý giảng viên (Lecturer Management)
- Quản lý chức danh (Position Management)

Mỗi module đều hỗ trợ các tính năng CRUD cơ bản cùng các tính năng nâng cao như Import/Export Excel, Print, tìm kiếm và phân trang.

---