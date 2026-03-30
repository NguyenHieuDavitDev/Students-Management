-- Thêm cột loại quyết định cho lịch sử nhân sự (SQL Server)

IF COL_LENGTH(N'dbo.employee_position_history', N'decision_type') IS NULL
BEGIN
    ALTER TABLE dbo.employee_position_history
    ADD decision_type VARCHAR(40) NULL;
END
GO
