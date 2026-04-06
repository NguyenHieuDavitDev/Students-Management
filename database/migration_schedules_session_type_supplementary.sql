-- Chạy thủ công trên student_db nếu Flyway chưa chạy hoặc cần sửa gấp.
-- Phiên bản đầy đủ: gỡ mọi CHECK trên schedules (sys.objects type C), tạo lại 4 CHECK enum (có SUPPLEMENTARY).

SET NOCOUNT ON;

IF OBJECT_ID(N'dbo.schedules', N'U') IS NOT NULL
BEGIN
    DECLARE @cn NVARCHAR(256);
    DECLARE @drop NVARCHAR(512);

    WHILE 1 = 1
    BEGIN
        SET @cn = NULL;
        SELECT TOP (1) @cn = o.name
        FROM sys.objects o
        WHERE o.parent_object_id = OBJECT_ID(N'dbo.schedules')
          AND o.type = N'C';

        IF @cn IS NULL BREAK;

        SET @drop = N'ALTER TABLE dbo.schedules DROP CONSTRAINT ' + QUOTENAME(@cn);
        EXEC sp_executesql @drop;
    END;

    ALTER TABLE dbo.schedules ADD CONSTRAINT CK_schedules_session_type
        CHECK (session_type IN (N'THEORY', N'PRACTICE', N'EXAM', N'SUPPLEMENTARY'));

    ALTER TABLE dbo.schedules ADD CONSTRAINT CK_schedules_schedule_type
        CHECK (schedule_type IN (N'NORMAL', N'MAKEUP', N'EXTRA'));

    ALTER TABLE dbo.schedules ADD CONSTRAINT CK_schedules_status
        CHECK (status IN (N'ACTIVE', N'CANCELLED', N'MOVED'));

    ALTER TABLE dbo.schedules ADD CONSTRAINT CK_schedules_week_pattern
        CHECK (week_pattern IN (N'ALL', N'ODD', N'EVEN'));
END;
