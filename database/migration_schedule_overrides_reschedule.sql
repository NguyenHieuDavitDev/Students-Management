-- Migration: cho phép override_type = RESCHEDULE trên dbo.schedule_overrides.
-- Lỗi: CHECK cũ (vd. CK__schedule___overr__74794A92) không có RESCHEDULE → INSERT thất bại.
-- Cách 1 (khuyên dùng): migration tự chạy qua Flyway — file db/migration/V20260406120000__...
-- Cách 2: chạy thủ công script này trên SSMS/sqlcmd (có GO cho từng batch).

SET NOCOUNT ON;

-- Tên constraint đúng theo log lỗi của bạn (an toàn nếu không tồn tại)
IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID(N'dbo.schedule_overrides')
      AND name = N'CK__schedule___overr__74794A92'
)
    ALTER TABLE dbo.schedule_overrides DROP CONSTRAINT CK__schedule___overr__74794A92;
GO

IF COL_LENGTH(N'dbo.schedule_overrides', N'moved_to_date') IS NULL
    ALTER TABLE dbo.schedule_overrides ADD moved_to_date date NULL;
GO

-- Gỡ mọi CHECK còn lại trên bảng (một số DB constraint không gắn qua constraint_column_usage đúng cách)
DECLARE @cn NVARCHAR(256);
DECLARE @drop NVARCHAR(512);
WHILE 1 = 1
BEGIN
    SET @cn = NULL;
    SELECT TOP (1) @cn = cc.name
    FROM sys.check_constraints cc
    WHERE cc.parent_object_id = OBJECT_ID(N'dbo.schedule_overrides');
    IF @cn IS NULL BREAK;
    SET @drop = N'ALTER TABLE dbo.schedule_overrides DROP CONSTRAINT ' + QUOTENAME(@cn);
    EXEC sp_executesql @drop;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints cc
    WHERE cc.parent_object_id = OBJECT_ID(N'dbo.schedule_overrides')
      AND cc.name = N'CK_schedule_overrides_override_type'
)
BEGIN
    ALTER TABLE dbo.schedule_overrides
    ADD CONSTRAINT CK_schedule_overrides_override_type
        CHECK (override_type IN (N'MAKEUP', N'ROOM_CHANGE', N'TIME_CHANGE', N'CANCEL', N'RESCHEDULE'));
END
GO
