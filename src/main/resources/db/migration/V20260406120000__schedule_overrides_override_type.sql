-- Cho phép override_type = RESCHEDULE; gỡ mọi CHECK cũ trên schedule_overrides (tên như CK__schedule___overr__74794A92).
-- Chạy qua Flyway khi bảng đã tồn tại. DB mới: Hibernate tạo bảng sau Flyway — khi đó bảng chưa có thì khối IF bỏ qua (không lỗi).
IF OBJECT_ID(N'dbo.schedule_overrides', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH(N'dbo.schedule_overrides', N'moved_to_date') IS NULL
        ALTER TABLE dbo.schedule_overrides ADD moved_to_date date NULL;

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
    END;
END;
