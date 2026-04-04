-- Migration: gỡ hoàn toàn module nhân sự (bảng employees) khỏi DB hiện có.
-- Chạy một lần trên SQL Server sau khi ứng dụng không còn dùng bảng employees.
-- An toàn khi chạy lại: các bước đều kiểm tra tồn tại.

-- 1) FK sai (nếu còn): users.lecturer_id -> employees.employee_id (trùng ý nghĩa với FK sang lecturers)
IF EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = N'FK2ev3mcn8occ6m93pg97645n5n'
      AND parent_object_id = OBJECT_ID(N'dbo.users')
)
    ALTER TABLE dbo.users DROP CONSTRAINT FK2ev3mcn8occ6m93pg97645n5n;
GO

-- 2) Mọi FK từ bảng khác trỏ VÀO dbo.employees
DECLARE @sql NVARCHAR(MAX) = N'';
SELECT @sql = @sql
    + N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(fk.parent_object_id)) + N'.' + QUOTENAME(OBJECT_NAME(fk.parent_object_id))
    + N' DROP CONSTRAINT ' + QUOTENAME(fk.name) + N';' + CHAR(13)
FROM sys.foreign_keys fk
JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
JOIN sys.tables rt ON rt.object_id = fkc.referenced_object_id
WHERE rt.name = N'employees';

IF LEN(@sql) > 0
    EXEC sp_executesql @sql;
GO

-- 3) Cột lecturers.employee_id (do Hibernate/schema cũ): bỏ FK còn lại trên cột đó rồi xóa cột
DECLARE @sql2 NVARCHAR(MAX) = N'';
SELECT @sql2 = @sql2
    + N'ALTER TABLE dbo.lecturers DROP CONSTRAINT ' + QUOTENAME(fk.name) + N';' + CHAR(13)
FROM sys.foreign_keys fk
JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
JOIN sys.columns c ON c.object_id = fkc.parent_object_id AND c.column_id = fkc.parent_column_id
WHERE OBJECT_NAME(fk.parent_object_id) = N'lecturers'
  AND c.name = N'employee_id';

IF LEN(@sql2) > 0
    EXEC sp_executesql @sql2;
GO

IF COL_LENGTH(N'dbo.lecturers', N'employee_id') IS NOT NULL
    ALTER TABLE dbo.lecturers DROP COLUMN employee_id;
GO

-- 4) Lịch sử nhân sự (nếu từng tạo bằng migration cũ)
IF OBJECT_ID(N'dbo.employee_position_history', N'U') IS NOT NULL
    DROP TABLE dbo.employee_position_history;
GO

-- 5) Bảng employees
IF OBJECT_ID(N'dbo.employees', N'U') IS NOT NULL
    DROP TABLE dbo.employees;
GO
