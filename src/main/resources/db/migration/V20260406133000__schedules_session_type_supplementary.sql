-- Cho phép session_type = SUPPLEMENTARY.
-- Lỗi: CHECK cũ (vd. CK__schedules__sessi__6AEFE058) chỉ có THEORY, PRACTICE, EXAM thì INSERT tăng cường thất bại.
-- Gỡ mọi CHECK trên schedules có biểu thức chứa session_type, rồi thêm một CHECK thống nhất.
-- Nếu vẫn lỗi (CHECK còn sót / song song), chạy tiếp V20260406140000.
IF OBJECT_ID(N'dbo.schedules', N'U') IS NOT NULL
BEGIN
    DECLARE @cn NVARCHAR(256);
    DECLARE @drop NVARCHAR(512);

    WHILE 1 = 1
    BEGIN
        SET @cn = NULL;
        SELECT TOP (1) @cn = cc.name
        FROM sys.check_constraints cc
        INNER JOIN sys.sql_modules sm ON sm.object_id = cc.object_id
        WHERE cc.parent_object_id = OBJECT_ID(N'dbo.schedules')
          AND UPPER(sm.definition) LIKE N'%SESSION_TYPE%';

        IF @cn IS NULL BREAK;

        SET @drop = N'ALTER TABLE dbo.schedules DROP CONSTRAINT ' + QUOTENAME(@cn);
        EXEC sp_executesql @drop;
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.check_constraints cc
        WHERE cc.parent_object_id = OBJECT_ID(N'dbo.schedules')
          AND cc.name = N'CK_schedules_session_type'
    )
        ALTER TABLE dbo.schedules ADD CONSTRAINT CK_schedules_session_type
            CHECK (session_type IN (N'THEORY', N'PRACTICE', N'EXAM', N'SUPPLEMENTARY'));
END;
