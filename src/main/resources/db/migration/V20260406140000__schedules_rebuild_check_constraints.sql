-- Gỡ toàn bộ CHECK trên dbo.schedules (type 'C' trong sys.objects) rồi tạo lại đủ giá trị enum,
-- vì migration V20260406133000 có thể không drop được CHECK cũ (join sql_modules) hoặc còn song song CHECK thiếu SUPPLEMENTARY.
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
