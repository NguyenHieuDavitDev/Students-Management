-- Lịch sử phòng ban / chức danh / loại nhân sự (theo thời gian)
-- Chạy trên SQL Server sau khi đã có bảng employees, positions, departments.

IF OBJECT_ID(N'dbo.employee_position_history', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.employee_position_history (
        history_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWSEQUENTIALID(),
        employee_id UNIQUEIDENTIFIER NOT NULL,
        position_id UNIQUEIDENTIFIER NULL,
        department_id UNIQUEIDENTIFIER NULL,
        employee_type VARCHAR(30) NOT NULL,
        effective_from DATE NOT NULL,
        effective_to DATE NULL,
        decision_no NVARCHAR(100) NULL,
        decision_type VARCHAR(40) NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_eph_employee FOREIGN KEY (employee_id) REFERENCES dbo.employees(employee_id),
        CONSTRAINT FK_eph_position FOREIGN KEY (position_id) REFERENCES dbo.positions(position_id),
        CONSTRAINT FK_eph_department FOREIGN KEY (department_id) REFERENCES dbo.departments(department_id)
    );
    CREATE INDEX IX_eph_employee ON dbo.employee_position_history(employee_id);
END
GO
