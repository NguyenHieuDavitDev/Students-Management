-- Mở rộng cấu hình hệ thống: logo, tagline đăng nhập, thông báo chung, audit.
IF OBJECT_ID(N'dbo.system_settings', N'U') IS NOT NULL
BEGIN
    IF COL_LENGTH(N'dbo.system_settings', N'logo_url') IS NULL
        ALTER TABLE dbo.system_settings ADD logo_url NVARCHAR(500) NULL;
    IF COL_LENGTH(N'dbo.system_settings', N'login_tagline') IS NULL
        ALTER TABLE dbo.system_settings ADD login_tagline NVARCHAR(500) NULL;
    IF COL_LENGTH(N'dbo.system_settings', N'global_notice') IS NULL
        ALTER TABLE dbo.system_settings ADD global_notice NVARCHAR(1000) NULL;
    IF COL_LENGTH(N'dbo.system_settings', N'global_notice_enabled') IS NULL
        ALTER TABLE dbo.system_settings ADD global_notice_enabled BIT NOT NULL
            CONSTRAINT DF_system_settings_global_notice_en DEFAULT 0;
    IF COL_LENGTH(N'dbo.system_settings', N'updated_at') IS NULL
        ALTER TABLE dbo.system_settings ADD updated_at DATETIME2(6) NULL;
    IF COL_LENGTH(N'dbo.system_settings', N'last_updated_by') IS NULL
        ALTER TABLE dbo.system_settings ADD last_updated_by NVARCHAR(100) NULL;
END;
