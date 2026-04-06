-- -------------------------------------------------------------
-- TablePlus 6.8.2(656)
--
-- https://tableplus.com/
--
-- Database: student_db
-- Generation Time: 2026-03-26 22:51:03.8710
-- -------------------------------------------------------------
-- ĐÃ SỬA TRIỆT ĐỂ:
--  1. Thứ tự DROP TABLE: bảng con trước, bảng cha sau
--  2. Thứ tự CREATE TABLE: bảng cha trước, bảng con sau (topological sort)
--  3. Thứ tự INSERT: theo đúng thứ tự phụ thuộc FK
--  4. Thêm SET IDENTITY_INSERT ON/OFF cho tất cả bảng IDENTITY có giá trị tường minh
--  5. Đổi tên [templates/roles] → [templates_roles] (ký tự '/' không hợp lệ)
--  6. Sửa encoding mô tả building bị lỗi
-- -------------------------------------------------------------

-- ============================================================
-- PHẦN 1: DROP TABLE (bảng con trước, bảng cha sau)
-- ============================================================

DROP TABLE IF EXISTS [dbo].[attendances];
DROP TABLE IF EXISTS [dbo].[schedule_overrides];
DROP TABLE IF EXISTS [dbo].[schedules];
DROP TABLE IF EXISTS [dbo].[user_roles];
DROP TABLE IF EXISTS [dbo].[role_permissions];
DROP TABLE IF EXISTS [dbo].[payments];
DROP TABLE IF EXISTS [dbo].[student_tuition];
DROP TABLE IF EXISTS [dbo].[student_grades];
DROP TABLE IF EXISTS [dbo].[graduation_results];
DROP TABLE IF EXISTS [dbo].[graduation_conditions];
DROP TABLE IF EXISTS [dbo].[tuition_fees];
DROP TABLE IF EXISTS [dbo].[notifications];
DROP TABLE IF EXISTS [dbo].[password_reset_tokens];
DROP TABLE IF EXISTS [dbo].[documents];
DROP TABLE IF EXISTS [dbo].[lecturer_course_classes];
DROP TABLE IF EXISTS [dbo].[exam_schedules];
DROP TABLE IF EXISTS [dbo].[feedbacks];
DROP TABLE IF EXISTS [dbo].[grade_components];
DROP TABLE IF EXISTS [dbo].[course_registrations];
DROP TABLE IF EXISTS [dbo].[course_prerequisites];
DROP TABLE IF EXISTS [dbo].[class_sections];
DROP TABLE IF EXISTS [dbo].[room_block_times];
DROP TABLE IF EXISTS [dbo].[exam_rooms];
DROP TABLE IF EXISTS [dbo].[equipments];
DROP TABLE IF EXISTS [dbo].[rooms];
DROP TABLE IF EXISTS [dbo].[students];
DROP TABLE IF EXISTS [dbo].[users];
DROP TABLE IF EXISTS [dbo].[classes];
DROP TABLE IF EXISTS [dbo].[training_programs];
DROP TABLE IF EXISTS [dbo].[lecturers];
DROP TABLE IF EXISTS [dbo].[majors];
DROP TABLE IF EXISTS [dbo].[courses];
DROP TABLE IF EXISTS [dbo].[semesters];
DROP TABLE IF EXISTS [dbo].[buildings];
DROP TABLE IF EXISTS [dbo].[departments];
DROP TABLE IF EXISTS [dbo].[education_types];
DROP TABLE IF EXISTS [dbo].[exam_types];
DROP TABLE IF EXISTS [dbo].[faculties];
DROP TABLE IF EXISTS [dbo].[grade_scales];
DROP TABLE IF EXISTS [dbo].[lecturer_duties];
DROP TABLE IF EXISTS [dbo].[permissions];
DROP TABLE IF EXISTS [dbo].[positions];
DROP TABLE IF EXISTS [dbo].[roles];
DROP TABLE IF EXISTS [dbo].[room_types];
DROP TABLE IF EXISTS [dbo].[system_settings];
DROP TABLE IF EXISTS [dbo].[templates_roles];
DROP TABLE IF EXISTS [dbo].[time_slots];
DROP TABLE IF EXISTS [dbo].[training_levels];

-- ============================================================
-- PHẦN 2: CREATE TABLE (bảng cha trước, bảng con sau)
-- ============================================================

-- ── Nhóm 1: Không có FK (bảng gốc) ──────────────────────────

CREATE TABLE [dbo].[buildings] (
    [building_id]      uniqueidentifier NOT NULL,
    [address]          nvarchar(255),
    [building_code]    varchar(20),
    [building_name]    nvarchar(150),
    [description]      varchar(255),
    [number_of_floors] int,
    [total_area]       float,
    PRIMARY KEY ([building_id])
);

CREATE TABLE [dbo].[departments] (
    [department_id]   uniqueidentifier NOT NULL,
    [department_code] nvarchar(30),
    [department_name] nvarchar(150),
    [description]     nvarchar(500),
    PRIMARY KEY ([department_id])
);

CREATE TABLE [dbo].[education_types] (
    [education_type_id]   uniqueidentifier NOT NULL,
    [created_at]          datetime2(6),
    [education_type_name] nvarchar(100),
    [is_active]           bit,
    [updated_at]          datetime2(6),
    PRIMARY KEY ([education_type_id])
);

CREATE TABLE [dbo].[exam_types] (
    [id]          uniqueidentifier NOT NULL,
    [created_at]  datetime2(6),
    [description] nvarchar(MAX),
    [name]        nvarchar(200),
    [updated_at]  datetime2(6),
    PRIMARY KEY ([id])
);

CREATE TABLE [dbo].[faculties] (
    [faculty_id]   uniqueidentifier NOT NULL,
    [faculty_code] nvarchar(50),
    [faculty_name] nvarchar(150),
    PRIMARY KEY ([faculty_id])
);

CREATE TABLE [dbo].[grade_scales] (
    [id]           uniqueidentifier NOT NULL,
    [description]  nvarchar(100),
    [grade_point]  numeric(3,2),
    [letter_grade] varchar(2),
    [max_score]    numeric(4,2),
    [min_score]    numeric(4,2),
    PRIMARY KEY ([id])
);

CREATE TABLE [dbo].[lecturer_duties] (
    [lecturer_duty_id] uniqueidentifier NOT NULL,
    [description]      nvarchar(500),
    [duty_code]        nvarchar(20),
    [duty_name]        nvarchar(150),
    PRIMARY KEY ([lecturer_duty_id])
);

CREATE TABLE [dbo].[permissions] (
    [id]          uniqueidentifier NOT NULL,
    [action]      nvarchar(50),
    [description] nvarchar(255),
    [name]        nvarchar(150),
    [resource]    nvarchar(50),
    [code]        nvarchar(80),
    PRIMARY KEY ([id])
);

CREATE TABLE [dbo].[positions] (
    [position_id]   uniqueidentifier NOT NULL,
    [position_code] nvarchar(20),
    [position_name] nvarchar(150),
    [description]   nvarchar(500),
    PRIMARY KEY ([position_id])
);

CREATE TABLE [dbo].[roles] (
    [id]          uniqueidentifier NOT NULL,
    [description] nvarchar(255),
    [name]        nvarchar(100),
    PRIMARY KEY ([id])
);

CREATE TABLE [dbo].[room_types] (
    [room_type_id]   uniqueidentifier NOT NULL,
    [description]    nvarchar(255),
    [max_capacity]   int,
    [room_type_code] varchar(20),
    [room_type_name] nvarchar(150),
    PRIMARY KEY ([room_type_id])
);

CREATE TABLE [dbo].[semesters] (
    [id]                 bigint IDENTITY NOT NULL,
    [academic_year]      nvarchar(20),
    [code]               nvarchar(20),
    [created_at]         datetime2(6),
    [description]        nvarchar(500),
    [end_date]           date,
    [name]               nvarchar(200),
    [registration_end]   date,
    [registration_start] date,
    [start_date]         date,
    [status]             varchar(20),
    [term]               int,
    [updated_at]         datetime2(6),
    PRIMARY KEY ([id])
);

CREATE TABLE [dbo].[system_settings] (
    [id]               int          NOT NULL,
    [address]          nvarchar(1000),
    [contact_email]    nvarchar(255),
    [footer_note]      nvarchar(500),
    [phone]            nvarchar(50),
    [school_name]      nvarchar(500),
    [school_short_name] nvarchar(200),
    [website]          nvarchar(500),
    PRIMARY KEY ([id])
);

-- SỬALỖI 5: Đổi tên từ [templates/roles] → [templates_roles]
-- Ký tự '/' trong tên bảng là không hợp lệ và gây lỗi parsing
CREATE TABLE [dbo].[templates_roles] (
    [id]          uniqueidentifier NOT NULL,
    [description] nvarchar(255),
    [name]        nvarchar(100),
    PRIMARY KEY ([id])
);

CREATE TABLE [dbo].[time_slots] (
    [id]           int IDENTITY NOT NULL,
    [end_time]     time(7),
    [is_active]    bit,
    [period_end]   int,
    [period_start] int,
    [slot_code]    nvarchar(20),
    [start_time]   time(7),
    PRIMARY KEY ([id])
);

CREATE TABLE [dbo].[training_levels] (
    [training_level_id]   uniqueidentifier NOT NULL,
    [training_level_name] nvarchar(100),
    [description]         nvarchar(255),
    PRIMARY KEY ([training_level_id])
);

-- ── Nhóm 2: Phụ thuộc Nhóm 1 ─────────────────────────────────

-- rooms → buildings, room_types
CREATE TABLE [dbo].[rooms] (
    [id]           bigint IDENTITY NOT NULL,
    [area]         float,
    [capacity]     int,
    [created_at]   datetime2(6),
    [floor]        int,
    [is_active]    bit,
    [room_code]    nvarchar(20),
    [room_name]    nvarchar(100),
    [status]       nvarchar(20),
    [updated_at]   datetime2(6),
    [building_id]  uniqueidentifier,
    [room_type_id] uniqueidentifier,
    CONSTRAINT [FKh9m2n1paq5hmd3u0klfl7wsfv] FOREIGN KEY ([room_type_id]) REFERENCES [dbo].[room_types]([room_type_id]),
    CONSTRAINT [FKojgn0sxhkfxd7pmmojnem9r4q] FOREIGN KEY ([building_id])  REFERENCES [dbo].[buildings]([building_id]),
    PRIMARY KEY ([id])
);

-- majors → faculties
CREATE TABLE [dbo].[majors] (
    [major_id]   uniqueidentifier NOT NULL,
    [major_code] nvarchar(50),
    [major_name] nvarchar(150),
    [faculty_id] uniqueidentifier,
    CONSTRAINT [FKitqtm0b9li7x2h872rumqnqol] FOREIGN KEY ([faculty_id]) REFERENCES [dbo].[faculties]([faculty_id]),
    PRIMARY KEY ([major_id])
);

-- courses → faculties
CREATE TABLE [dbo].[courses] (
    [id]             uniqueidentifier NOT NULL,
    [course_code]    nvarchar(50),
    [course_name]    nvarchar(200),
    [created_at]     datetime2(6),
    [credits]        int,
    [description]    nvarchar(1000),
    [lecture_hours]  int,
    [practice_hours] int,
    [status]         bit,
    [updated_at]     datetime2(6),
    [faculty_id]     uniqueidentifier,
    CONSTRAINT [FK81p0vixhqd3i5uwdk3sclg0nk] FOREIGN KEY ([faculty_id]) REFERENCES [dbo].[faculties]([faculty_id]),
    PRIMARY KEY ([id])
);

-- lecturers → departments, lecturer_duties, faculties, positions
CREATE TABLE [dbo].[lecturers] (
    [lecturer_id]     uniqueidentifier NOT NULL,
    [academic_degree] nvarchar(50),
    [academic_title]  nvarchar(50),
    [address]         nvarchar(255),
    [avatar]          varchar(255),
    [citizen_id]      varchar(20),
    [date_of_birth]   date,
    [email]           varchar(255),
    [full_name]       nvarchar(150),
    [gender]          nvarchar(10),
    [lecturer_code]   varchar(20),
    [phone_number]    varchar(255),
    [faculty_id]      uniqueidentifier,
    [position_id]     uniqueidentifier,
    [lecturer_duty_id] uniqueidentifier,
    [department_id]   uniqueidentifier,
    CONSTRAINT [FKpowy6g9rglsdytsdu6c9fymox] FOREIGN KEY ([department_id])    REFERENCES [dbo].[departments]([department_id]),
    CONSTRAINT [FKd64k4ct1l15mjlucvdhwwftmr] FOREIGN KEY ([lecturer_duty_id]) REFERENCES [dbo].[lecturer_duties]([lecturer_duty_id]),
    CONSTRAINT [FK5s4iv2pp699ojmljjog3kioh]  FOREIGN KEY ([faculty_id])       REFERENCES [dbo].[faculties]([faculty_id]),
    CONSTRAINT [FKthh38obs4te6njpjs8ab15l6c] FOREIGN KEY ([position_id])      REFERENCES [dbo].[positions]([position_id]),
    PRIMARY KEY ([lecturer_id])
);

-- ── Nhóm 3: Phụ thuộc Nhóm 2 ─────────────────────────────────

-- classes → majors, education_types, training_levels, lecturers
CREATE TABLE [dbo].[classes] (
    [class_id]          uniqueidentifier NOT NULL,
    [academic_year]     nvarchar(20),
    [class_code]        nvarchar(50),
    [class_name]        nvarchar(150),
    [class_status]      nvarchar(50),
    [created_at]        datetime2(6),
    [education_type]    nvarchar(50),
    [is_active]         bit,
    [max_student]       int,
    [training_level]    nvarchar(50),
    [updated_at]        datetime2(6),
    [major_id]          uniqueidentifier,
    [education_type_id] uniqueidentifier,
    [training_level_id] uniqueidentifier,
    [academic_advisor_lecturer_id] uniqueidentifier,
    CONSTRAINT [FKitg5hh7h9iywbvetbyth42ak8] FOREIGN KEY ([education_type_id])            REFERENCES [dbo].[education_types]([education_type_id]),
    CONSTRAINT [FKd90cfpx0elhh30c59xsphq33y] FOREIGN KEY ([academic_advisor_lecturer_id]) REFERENCES [dbo].[lecturers]([lecturer_id]),
    CONSTRAINT [FK4daajpxy22fog83y3mqa48816] FOREIGN KEY ([training_level_id])             REFERENCES [dbo].[training_levels]([training_level_id]),
    CONSTRAINT [FK6r9qmxcnxge92jgx4x4gltf0o] FOREIGN KEY ([major_id])                    REFERENCES [dbo].[majors]([major_id]),
    PRIMARY KEY ([class_id])
);

-- training_programs → majors
CREATE TABLE [dbo].[training_programs] (
    [program_id]     uniqueidentifier NOT NULL,
    [course]         nvarchar(20),
    [description]    nvarchar(1000),
    [duration_years] int,
    [is_active]      bit,
    [program_code]   nvarchar(50),
    [program_name]   nvarchar(200),
    [total_credits]  int,
    [major_id]       uniqueidentifier,
    CONSTRAINT [FKhmwge471sdvweygytmshmdt8s] FOREIGN KEY ([major_id]) REFERENCES [dbo].[majors]([major_id]),
    PRIMARY KEY ([program_id])
);

-- room_block_times → rooms, time_slots
CREATE TABLE [dbo].[room_block_times] (
    [block_id]    uniqueidentifier NOT NULL,
    [block_type]  nvarchar(30),
    [created_at]  datetime2(6),
    [day_of_week] int,
    [end_date]    date,
    [end_week]    int,
    [reason]      nvarchar(255),
    [start_date]  date,
    [start_week]  int,
    [status]      nvarchar(20),
    [updated_at]  datetime2(6),
    [room_id]     bigint,
    [time_slot_id] int,
    CONSTRAINT [FK8tgguab0mb2vrti7n5o8fhnhp] FOREIGN KEY ([time_slot_id]) REFERENCES [dbo].[time_slots]([id]),
    CONSTRAINT [FK7b6va70lvhgrjktbxw2w2ysc8] FOREIGN KEY ([room_id])      REFERENCES [dbo].[rooms]([id]),
    PRIMARY KEY ([block_id])
);

-- ── Nhóm 4: Phụ thuộc Nhóm 3 ─────────────────────────────────

-- students → classes
CREATE TABLE [dbo].[students] (
    [student_id]   uniqueidentifier NOT NULL,
    [address]      nvarchar(255),
    [avatar]       varchar(255),
    [citizen_id]   varchar(20),
    [date_of_birth] date,
    [email]        varchar(150),
    [full_name]    nvarchar(150),
    [gender]       nvarchar(10),
    [phone_number] varchar(20),
    [student_code] varchar(20),
    [class_id]     uniqueidentifier,
    CONSTRAINT [FKhnslh0rm5bthlble8vjunbnwe] FOREIGN KEY ([class_id]) REFERENCES [dbo].[classes]([class_id]),
    PRIMARY KEY ([student_id])
);

-- class_sections → courses, semesters, rooms, classes
CREATE TABLE [dbo].[class_sections] (
    [id]                    bigint IDENTITY NOT NULL,
    [class_code]            nvarchar(50),
    [class_name]            nvarchar(200),
    [created_at]            datetime2(6),
    [current_students]      int,
    [max_students]          int,
    [note]                  nvarchar(500),
    [room]                  nvarchar(100),
    [status]                varchar(30),
    [updated_at]            datetime2(6),
    [course_id]             uniqueidentifier,
    [semester_id]           bigint,
    [room_id]               bigint,
    [administrative_class_id] uniqueidentifier,
    CONSTRAINT [FK3os9of6f2v5o94rgksgxfn24l] FOREIGN KEY ([room_id])               REFERENCES [dbo].[rooms]([id]),
    CONSTRAINT [FKtamlxyq4rm9ybyevrdtcbdbrb] FOREIGN KEY ([semester_id])           REFERENCES [dbo].[semesters]([id]),
    CONSTRAINT [FKqyukgqp1pw598ifld2q9uwc08] FOREIGN KEY ([administrative_class_id]) REFERENCES [dbo].[classes]([class_id]),
    CONSTRAINT [FK695u7g15n5nfnaskhw3nhnsbw] FOREIGN KEY ([course_id])             REFERENCES [dbo].[courses]([id]),
    PRIMARY KEY ([id])
);

-- ── Nhóm 5: Phụ thuộc Nhóm 4 ─────────────────────────────────

-- users → lecturers, students
CREATE TABLE [dbo].[users] (
    [id]          uniqueidentifier NOT NULL,
    [email]       nvarchar(150),
    [enabled]     bit,
    [password]    varchar(255),
    [username]    nvarchar(100),
    [lecturer_id] uniqueidentifier,
    [student_id]  uniqueidentifier,
    CONSTRAINT [FKnq773gne4equ22e4nc7mk92xm] FOREIGN KEY ([lecturer_id]) REFERENCES [dbo].[lecturers]([lecturer_id]),
    CONSTRAINT [FKc8nfkx91xbh5fv7a02092q1ip] FOREIGN KEY ([student_id])  REFERENCES [dbo].[students]([student_id]),
    PRIMARY KEY ([id])
);

-- course_prerequisites → courses
CREATE TABLE [dbo].[course_prerequisites] (
    [id]                     uniqueidentifier NOT NULL,
    [course_id]              uniqueidentifier,
    [prerequisite_course_id] uniqueidentifier,
    CONSTRAINT [FKolf0a7iwh8c1mv9keyis4ebe8] FOREIGN KEY ([prerequisite_course_id]) REFERENCES [dbo].[courses]([id]),
    CONSTRAINT [FKhh4f1avebuvlv54m3j3l3pp36] FOREIGN KEY ([course_id])              REFERENCES [dbo].[courses]([id]),
    PRIMARY KEY ([id])
);

-- course_registrations → class_sections, students
CREATE TABLE [dbo].[course_registrations] (
    [id]               bigint IDENTITY NOT NULL,
    [note]             nvarchar(500),
    [registered_at]    datetime2(6),
    [class_section_id] bigint,
    [student_id]       uniqueidentifier,
    CONSTRAINT [FK36e9k9ywnlm0y4bcrh5n6bfhh] FOREIGN KEY ([class_section_id]) REFERENCES [dbo].[class_sections]([id]),
    CONSTRAINT [FK1uitfc1h86q0t7ncnhlylbvab] FOREIGN KEY ([student_id])       REFERENCES [dbo].[students]([student_id]),
    PRIMARY KEY ([id])
);

-- feedbacks → students, courses, lecturers
CREATE TABLE [dbo].[feedbacks] (
    [feedback_id] uniqueidentifier NOT NULL,
    [comment]     nvarchar(1000),
    [created_at]  datetime2(6),
    [rating]      int,
    [lecturer_id] uniqueidentifier,
    [student_id]  uniqueidentifier,
    [subject_id]  uniqueidentifier,
    CONSTRAINT [FK4ocysx6ldsioryb4bx1etqw32] FOREIGN KEY ([student_id])  REFERENCES [dbo].[students]([student_id]),
    CONSTRAINT [FK6ki8qaeii3x9s3n34e7a61dh9] FOREIGN KEY ([subject_id])  REFERENCES [dbo].[courses]([id]),
    CONSTRAINT [FKnjtclkv7869w3l53iemsk446o] FOREIGN KEY ([lecturer_id]) REFERENCES [dbo].[lecturers]([lecturer_id]),
    PRIMARY KEY ([feedback_id])
);

-- grade_components → class_sections
CREATE TABLE [dbo].[grade_components] (
    [id]              uniqueidentifier NOT NULL,
    [component_name]  nvarchar(100),
    [created_at]      datetime2(6),
    [max_score]       numeric(4,2),
    [weight]          numeric(5,2),
    [course_class_id] bigint,
    CONSTRAINT [FK1niqvtx529y9svn3l55odmdu9] FOREIGN KEY ([course_class_id]) REFERENCES [dbo].[class_sections]([id]),
    PRIMARY KEY ([id])
);

-- exam_schedules → class_sections, exam_types
CREATE TABLE [dbo].[exam_schedules] (
    [id]               uniqueidentifier NOT NULL,
    [created_at]       datetime2(6),
    [duration_minutes] int,
    [exam_date]        date,
    [note]             nvarchar(500),
    [start_time]       time(7),
    [updated_at]       datetime2(6),
    [class_section_id] bigint,
    [exam_type_id]     uniqueidentifier,
    CONSTRAINT [FK1akuy67l7e43pakrt7pajk7ho] FOREIGN KEY ([class_section_id]) REFERENCES [dbo].[class_sections]([id]),
    CONSTRAINT [FK44gdqtyy5mhme35vacv0e9gs3] FOREIGN KEY ([exam_type_id])     REFERENCES [dbo].[exam_types]([id]),
    PRIMARY KEY ([id])
);

-- exam_rooms → rooms
CREATE TABLE [dbo].[exam_rooms] (
    [id]            uniqueidentifier NOT NULL,
    [created_at]    datetime2(6),
    [description]   nvarchar(MAX),
    [exam_capacity] int,
    [updated_at]    datetime2(6),
    [room_id]       bigint,
    CONSTRAINT [FKeb4oab0wpuidk3kwoulgcop49] FOREIGN KEY ([room_id]) REFERENCES [dbo].[rooms]([id]),
    PRIMARY KEY ([id])
);

-- equipments → rooms
CREATE TABLE [dbo].[equipments] (
    [id]             bigint IDENTITY NOT NULL,
    [created_at]     datetime2(6),
    [equipment_code] varchar(50),
    [equipment_name] nvarchar(150),
    [purchase_date]  date,
    [serial_number]  nvarchar(100),
    [status]         varchar(20),
    [updated_at]     datetime2(6),
    [room_id]        bigint,
    CONSTRAINT [FK5r50pavpda4dbw3p8n6rjikkq] FOREIGN KEY ([room_id]) REFERENCES [dbo].[rooms]([id]),
    PRIMARY KEY ([id])
);

-- lecturer_course_classes → class_sections, lecturers
CREATE TABLE [dbo].[lecturer_course_classes] (
    [id]               bigint IDENTITY NOT NULL,
    [created_at]       datetime2(6),
    [note]             nvarchar(500),
    [updated_at]       datetime2(6),
    [class_section_id] bigint,
    [lecturer_id]      uniqueidentifier,
    CONSTRAINT [FKdcjngpfupr3e5nbwxvjn8ddqx] FOREIGN KEY ([class_section_id]) REFERENCES [dbo].[class_sections]([id]),
    CONSTRAINT [FK5rge75dqqakbup10yxa3c2gs]  FOREIGN KEY ([lecturer_id])      REFERENCES [dbo].[lecturers]([lecturer_id]),
    PRIMARY KEY ([id])
);

-- schedules → rooms, semesters, time_slots, class_sections, lecturers
CREATE TABLE [dbo].[schedules] (
    [id]               uniqueidentifier NOT NULL,
    [created_at]       datetime2(6),
    [created_by]       bigint,
    [day_of_week]      int,
    [end_week]         int,
    [note]             nvarchar(255),
    [schedule_type]    varchar(20),
    [session_type]     varchar(20),
    [start_week]       int,
    [status]           varchar(20),
    [updated_at]       datetime2(6),
    [week_pattern]     varchar(20),
    [class_section_id] bigint,
    [lecturer_id]      uniqueidentifier,
    [room_id]          bigint,
    [semester_id]      bigint,
    [time_slot_id]     int,
    CONSTRAINT [FK6xau0n9awp58ear4phhxssp7]  FOREIGN KEY ([time_slot_id])     REFERENCES [dbo].[time_slots]([id]),
    CONSTRAINT [FKi7fs951hibbtpd8l7kak45gbb] FOREIGN KEY ([semester_id])      REFERENCES [dbo].[semesters]([id]),
    CONSTRAINT [FK34r5t4jexlcas19pleifb8ihv] FOREIGN KEY ([room_id])          REFERENCES [dbo].[rooms]([id]),
    CONSTRAINT [FK3g45lyn6835mq4uyb8rv8e4pr] FOREIGN KEY ([class_section_id]) REFERENCES [dbo].[class_sections]([id]),
    CONSTRAINT [FKtnsx4uikatst6c88xvb7jim8t] FOREIGN KEY ([lecturer_id])      REFERENCES [dbo].[lecturers]([lecturer_id]),
    PRIMARY KEY ([id])
);

-- ── Nhóm 6: Phụ thuộc Nhóm 5 ─────────────────────────────────

-- documents → users, courses
CREATE TABLE [dbo].[documents] (
    [document_id] uniqueidentifier NOT NULL,
    [created_at]  datetime2(6),
    [description] nvarchar(500),
    [file_type]   nvarchar(50),
    [file_url]    nvarchar(500),
    [title]       nvarchar(255),
    [subject_id]  uniqueidentifier,
    [uploaded_by] uniqueidentifier,
    CONSTRAINT [FKaxmoskj22pfhg2eeldnu75fpv] FOREIGN KEY ([subject_id])  REFERENCES [dbo].[courses]([id]),
    CONSTRAINT [FK1ugacya4ssi0ilf8a9tjycgs6] FOREIGN KEY ([uploaded_by]) REFERENCES [dbo].[users]([id]),
    PRIMARY KEY ([document_id])
);

-- notifications → users
CREATE TABLE [dbo].[notifications] (
    [id]                uniqueidentifier NOT NULL,
    [category]          varchar(50),
    [content]           nvarchar(2000),
    [created_at]        datetime2(6),
    [created_by]        uniqueidentifier,
    [is_read]           bit DEFAULT ((0)),
    [read_at]           datetime2(6),
    [scheduled_at]      datetime2(6),
    [title]             nvarchar(200),
    [recipient_user_id] uniqueidentifier,
    [source_id]         varchar(120),
    [source_type]       varchar(60),
    CONSTRAINT [FKt8ievafor22iuvg5sd4p7lhbk] FOREIGN KEY ([recipient_user_id]) REFERENCES [dbo].[users]([id]),
    PRIMARY KEY ([id])
);

-- password_reset_tokens → users
CREATE TABLE [dbo].[password_reset_tokens] (
    [id]        uniqueidentifier NOT NULL,
    [expiry_at] datetimeoffset,
    [token]     varchar(64),
    [user_id]   uniqueidentifier,
    CONSTRAINT [FKk3ndxg5xp6v7wd4gjyusp15gq] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users]([id]),
    PRIMARY KEY ([id])
);

-- student_tuition → students, semesters
CREATE TABLE [dbo].[student_tuition] (
    [id]               uniqueidentifier NOT NULL,
    [amount_paid]      numeric(18,0),
    [created_at]       datetime2(6),
    [remaining_amount] numeric(18,0),
    [status]           varchar(20),
    [total_amount]     numeric(18,0),
    [total_credits]    int,
    [updated_at]       datetime2(6),
    [semester_id]      bigint,
    [student_id]       uniqueidentifier,
    CONSTRAINT [FK33nrmt8f0q5s5fvwtn0ym7df] FOREIGN KEY ([student_id])  REFERENCES [dbo].[students]([student_id]),
    CONSTRAINT [FK8e3gcu54kamww7cs8kepol3bx] FOREIGN KEY ([semester_id]) REFERENCES [dbo].[semesters]([id]),
    PRIMARY KEY ([id])
);

-- student_grades → lecturers, grade_components, students, class_sections
CREATE TABLE [dbo].[student_grades] (
    [id]                 uniqueidentifier NOT NULL,
    [graded_at]          datetime2(6),
    [score]              numeric(4,2),
    [updated_at]         datetime2(6),
    [course_class_id]    bigint,
    [grade_component_id] uniqueidentifier,
    [graded_by]          uniqueidentifier,
    [student_id]         uniqueidentifier,
    CONSTRAINT [FKqas2lr2sk839kcpbdw06k9shr] FOREIGN KEY ([graded_by])          REFERENCES [dbo].[lecturers]([lecturer_id]),
    CONSTRAINT [FK3k33iae4i8poyy8abietq65mi] FOREIGN KEY ([grade_component_id]) REFERENCES [dbo].[grade_components]([id]),
    CONSTRAINT [FKe8t3tau7ti61n06siogcuigkq] FOREIGN KEY ([student_id])         REFERENCES [dbo].[students]([student_id]),
    CONSTRAINT [FK23o9lbm2xnr8gamo6pload2ig] FOREIGN KEY ([course_class_id])    REFERENCES [dbo].[class_sections]([id]),
    PRIMARY KEY ([id])
);

-- graduation_conditions → training_programs
CREATE TABLE [dbo].[graduation_conditions] (
    [id]                   bigint IDENTITY NOT NULL,
    [created_at]           datetime2(6),
    [min_credits]          int,
    [min_gpa]              decimal(4,2),
    [required_certificate] nvarchar(500),
    [required_courses]     nvarchar(MAX),
    [updated_at]           datetime2(6),
    [program_id]           uniqueidentifier,
    CONSTRAINT [FK3wpw1crbjtat5k6m4isoy7ipr] FOREIGN KEY ([program_id]) REFERENCES [dbo].[training_programs]([program_id]),
    PRIMARY KEY ([id])
);

-- graduation_results → students, training_programs
CREATE TABLE [dbo].[graduation_results] (
    [id]              bigint IDENTITY NOT NULL,
    [certificates]    nvarchar(500),
    [checked_at]      datetime2(6),
    [created_at]      datetime2(6),
    [gpa]             decimal(4,2),
    [missing_courses] nvarchar(MAX),
    [note]            nvarchar(500),
    [status]          varchar(20),
    [total_credits]   int,
    [updated_at]      datetime2(6),
    [program_id]      uniqueidentifier,
    [student_id]      uniqueidentifier,
    CONSTRAINT [FKtb53cda5aj4t2xqjfjtb6cvgh] FOREIGN KEY ([student_id]) REFERENCES [dbo].[students]([student_id]),
    CONSTRAINT [FKi032pecot6qj6fjyv5ywhl5w3] FOREIGN KEY ([program_id]) REFERENCES [dbo].[training_programs]([program_id]),
    PRIMARY KEY ([id])
);

-- role_permissions → roles, permissions
CREATE TABLE [dbo].[role_permissions] (
    [role_id]       uniqueidentifier NOT NULL,
    [permission_id] uniqueidentifier NOT NULL,
    [id]            uniqueidentifier,
    CONSTRAINT [FKn5fotdgk8d1xvo8nav9uv3muc] FOREIGN KEY ([role_id])       REFERENCES [dbo].[roles]([id]),
    CONSTRAINT [FKegdk29eiy7mdtefy5c7eirr6e] FOREIGN KEY ([permission_id]) REFERENCES [dbo].[permissions]([id]),
    PRIMARY KEY ([role_id],[permission_id])
);

-- tuition_fees → training_programs
CREATE TABLE [dbo].[tuition_fees] (
    [id]             uniqueidentifier NOT NULL,
    [created_at]     datetime2(6),
    [effective_date] date,
    [fee_per_credit] numeric(15,0),
    [note]           nvarchar(500),
    [status]         varchar(20),
    [updated_at]     datetime2(6),
    [program_id]     uniqueidentifier,
    CONSTRAINT [FKgcqy6r5qrsf0hu1utc621lvou] FOREIGN KEY ([program_id]) REFERENCES [dbo].[training_programs]([program_id]),
    PRIMARY KEY ([id])
);

-- user_roles → users, roles
CREATE TABLE [dbo].[user_roles] (
    [user_id] uniqueidentifier NOT NULL,
    [role_id] uniqueidentifier NOT NULL,
    CONSTRAINT [FKh8ciramu9cc9q3qcqiv4ue8a6] FOREIGN KEY ([role_id]) REFERENCES [dbo].[roles]([id]),
    CONSTRAINT [FKhfh9dx7w3ubf1co1vdev94g3f] FOREIGN KEY ([user_id]) REFERENCES [dbo].[users]([id]),
    PRIMARY KEY ([user_id],[role_id])
);

-- ── Nhóm 7: Phụ thuộc Nhóm 6 ─────────────────────────────────

-- payments → student_tuition
CREATE TABLE [dbo].[payments] (
    [id]                 bigint IDENTITY NOT NULL,
    [amount]             numeric(18,0),
    [created_at]         datetime2(6),
    [payment_date]       datetime2(6),
    [payment_method]     varchar(30),
    [status]             varchar(20),
    [transaction_code]   varchar(100),
    [updated_at]         datetime2(6),
    [student_tuition_id] uniqueidentifier,
    CONSTRAINT [FKhsg7cbi6fisb0hw87l53qn3db] FOREIGN KEY ([student_tuition_id]) REFERENCES [dbo].[student_tuition]([id]),
    PRIMARY KEY ([id])
);

-- schedule_overrides → schedules, lecturers, rooms, time_slots
CREATE TABLE [dbo].[schedule_overrides] (
    [override_id]      uniqueidentifier NOT NULL,
    [approved_at]      datetime2(6),
    [approved_by]      uniqueidentifier,
    [created_at]       datetime2(6),
    [override_date]    date,
    [override_type]    varchar(30),
    [reason]           nvarchar(255),
    [status]           varchar(20),
    [updated_at]       datetime2(6),
    [new_lecturer_id]  uniqueidentifier,
    [new_room_id]      bigint,
    [new_time_slot_id] int,
    [moved_to_date]    date,
    [schedule_id]      uniqueidentifier,
    CONSTRAINT [CK_schedule_overrides_override_type] CHECK ([override_type] IN (
        N'MAKEUP', N'ROOM_CHANGE', N'TIME_CHANGE', N'CANCEL', N'RESCHEDULE'
    )),
    CONSTRAINT [FKhbtw334pvq95c3mj83bu7h59q] FOREIGN KEY ([new_room_id])      REFERENCES [dbo].[rooms]([id]),
    CONSTRAINT [FKoodl2tghs09bov371b7bscgnb] FOREIGN KEY ([schedule_id])      REFERENCES [dbo].[schedules]([id]),
    CONSTRAINT [FKfbb5a0bwnq8927sn3inkb1vlr] FOREIGN KEY ([new_lecturer_id])  REFERENCES [dbo].[lecturers]([lecturer_id]),
    CONSTRAINT [FKahdj2tw0ntmmq2uv7hlyt2u0i] FOREIGN KEY ([new_time_slot_id]) REFERENCES [dbo].[time_slots]([id]),
    PRIMARY KEY ([override_id])
);

-- attendances → students, lecturers, class_sections
CREATE TABLE [dbo].[attendances] (
    [attendance_id]   uniqueidentifier NOT NULL,
    [attendance_date] date,
    [created_at]      datetime2(6),
    [marked_at]       datetime2(6),
    [note]            nvarchar(500),
    [present]         bit,
    [updated_at]      datetime2(6),
    [course_class_id] bigint,
    [marked_by]       uniqueidentifier,
    [student_id]      uniqueidentifier,
    CONSTRAINT [FKd9bcb0pqw5fj0ianasvb3gdny] FOREIGN KEY ([course_class_id]) REFERENCES [dbo].[class_sections]([id]),
    CONSTRAINT [FK9gd0t3afusc33hyfufd7apwo]  FOREIGN KEY ([marked_by])       REFERENCES [dbo].[lecturers]([lecturer_id]),
    CONSTRAINT [FK7bm4q4wptspkenhrsjgatdmk0] FOREIGN KEY ([student_id])      REFERENCES [dbo].[students]([student_id]),
    PRIMARY KEY ([attendance_id])
);

-- ============================================================
-- PHẦN 3: INSERT DATA (theo đúng thứ tự phụ thuộc FK)
-- ============================================================

-- buildings
-- SỬALỖI 6: Sửa encoding bị lỗi ('Toà nhà chính ? Ðà N?ng' → đúng)
INSERT INTO [dbo].[buildings] ([building_id],[address],[building_code],[building_name],[description],[number_of_floors],[total_area]) VALUES
('C2F293AD-31C1-4755-8BF0-22822BA7AB3B',N'43 Xô Viết Nghệ Tĩnh, Hải Châu, Đà Nẵng',N'TC01',N'Toà Nhà Chính',N'Toà nhà chính ở Đà Nẵng','10','500000');

-- departments
INSERT INTO [dbo].[departments] ([department_id],[department_code],[department_name],[description]) VALUES
('EBF61413-4FE0-418E-881F-9E28E8FAD0E9',N'PDT',N'phòng đào tạo',N'');

-- education_types
INSERT INTO [dbo].[education_types] ([education_type_id],[created_at],[education_type_name],[is_active],[updated_at]) VALUES
('F35EF5FE-08AB-4076-99FD-DD7F38201734','2026-02-08 11:29:51.468889',N'Chính quy','1','2026-02-08 11:29:51.469191'),
('B9253E0F-AF5E-4901-92DB-96ED36BF77DB','2026-02-08 11:30:05.521929',N'Vừa học vừa làm','1','2026-02-08 11:30:05.522005'),
('38AAFE96-065E-45BB-929E-A2D0B6BDFA63','2026-02-08 11:30:53.813565',N'Liên thông','1','2026-02-08 11:30:53.813630'),
('954D3925-4FD1-4EBA-A65F-03C021CFD056','2026-02-08 11:31:10.220485',N'Văn bằng 2 chính quy','1','2026-02-08 11:31:10.220564');

-- exam_types
INSERT INTO [dbo].[exam_types] ([id],[created_at],[description],[name],[updated_at]) VALUES
('F0E09B39-C171-4B9D-8BDF-3A1EAC0572B5','2026-03-15 21:43:38.877943',N'kiểm tra giữa kỳ',N'Giữa kỳ','2026-03-15 21:43:38.878021'),
('4866E9A2-5B34-49F4-B9FF-546BD30921B0','2026-03-15 21:43:53.495761',N'kiểm tra kết thúc học phần',N'Cuối kỳ','2026-03-15 21:43:53.495796'),
('0EB94450-D043-4276-BA54-B157288F8EEF','2026-03-15 21:45:06.435946',N'kỳ thi khi thi lại học phần đối với những sinh viên hoãn thi',N'Thi lại','2026-03-15 21:45:06.436142'),
('9A66A6BC-E960-43A5-AF5A-F234B82287FF','2026-03-15 21:45:40.190894',N'dành cho những sinh viên đăng ký học lại học phần',N'Cải thiện','2026-03-15 21:45:40.190909');

-- faculties
INSERT INTO [dbo].[faculties] ([faculty_id],[faculty_code],[faculty_name]) VALUES
('2D290C0F-7BBD-44A5-957C-7C54525A2040',N'CNTT',N'Công nghệ thông tin');

-- grade_scales
INSERT INTO [dbo].[grade_scales] ([id],[description],[grade_point],[letter_grade],[max_score],[min_score]) VALUES
('44033120-0EAA-40CF-A0B5-BB12A09D5962',N'Xuất sắc','4.00',N'A','10.00','8.50'),
('62A40B9A-6650-403D-969C-206DE1446C9E',N'Giỏi','3.50',N'B+','8.40','8.00'),
('851FCF4D-15D0-4A34-A0A0-310321035798',N'Khá','3.00',N'B','7.90','7.00'),
('AE5034AC-98DC-48AF-8D5E-129787095FA1',N'Trung bình Khá','2.50',N'C+','6.90','6.50'),
('7480BFC0-5C49-4477-9FAB-672AD404063F',N'Trung bình','2.00',N'C','6.40','5.50'),
('482D5210-C4FC-4B2C-8D3B-880CBECFFEB4',N'Trung bình Yếu','1.50',N'D+','5.40','5.00'),
('9690E400-9763-4848-AB4A-941F82DEEC42',N'Yếu','1.00',N'D','4.90','4.00'),
('AEF0C54E-0B7E-43D8-8716-D7E520C40E93',N'Kém','0.00',N'F','3.90','0.00');

-- lecturer_duties
INSERT INTO [dbo].[lecturer_duties] ([lecturer_duty_id],[description],[duty_code],[duty_name]) VALUES
('0BC91085-B87B-49E4-8701-B09705177902',N'lãnh đạo khoa',N'PK',N'Phó khoa'),
('2A5629EB-FAF9-4BC7-8C84-F77D462FFA1B',N'Lãnh đạo toàn bộ cán bộ nhân viên trong khoa',N'TK',N'Trưởng khoa');

-- permissions
INSERT INTO [dbo].[permissions] ([id],[action],[description],[name],[resource],[code]) VALUES
('515A6E39-03B8-4ABE-92EF-E950705344D3',NULL,N'',N'admin chỉ có quyền tạo và đọc',NULL,N'ADMIN_CRUD');

-- positions
INSERT INTO [dbo].[positions] ([position_id],[position_code],[position_name],[description]) VALUES
('6C16960F-F12B-4EA4-AD4E-F8B795FF2A4E',N'TS',N'Tiến sĩ',N'Tiến sĩ được học tại đại học nước ngoài');

-- roles
INSERT INTO [dbo].[roles] ([id],[description],[name]) VALUES
('F4878407-DB22-4AB5-A783-6A6D661AEEAB',N'Lecturer mangements',N'Lecturer'),
('80612F4C-3A9E-4A4B-8607-6B8EF2055819',N'Quản trị hệ thống',N'ADMIN'),
('E4530BE1-F109-45A5-9666-7C09E496289E',N'university students',N'student'),
('2E8F79AB-A27C-4A42-AC41-9DFCD69F0040',N'Manager role with management access',N'MANAGER');

-- room_types
INSERT INTO [dbo].[room_types] ([room_type_id],[description],[max_capacity],[room_type_code],[room_type_name]) VALUES
('A884DF6C-0772-4793-AC95-9F557FFFB358',N'Phòng học bình thường','50',N'LT01',N'P401');

-- system_settings
INSERT INTO [dbo].[system_settings] ([id],[address],[contact_email],[footer_note],[phone],[school_name],[school_short_name],[website]) VALUES
('1',N'33 Xô Viết Nghệ Tĩnh, Hải Châu, Đà Nẵng',N'vanthu@donga.edu.vn',N'Copyright © 2026 - UDA',N'0236 3519 929',N'Đại học Đông Á',N'Thông tin đơn vị',N'https://donga.edu.vn/');

-- training_levels
INSERT INTO [dbo].[training_levels] ([training_level_id],[training_level_name],[description]) VALUES
('8947D0A3-1C41-4D30-B553-DF25E5D162D2',N'Đại học',N'học với hình thức 4 năm tại trường');

-- time_slots (IDENTITY + giá trị tường minh → cần IDENTITY_INSERT)
SET IDENTITY_INSERT [dbo].[time_slots] ON;
INSERT INTO [dbo].[time_slots] ([id],[end_time],[is_active],[period_end],[period_start],[slot_code],[start_time]) VALUES
('1','09:45:00.0000000','1','3','1',N'S1','07:00:00.0000000'),
('2','12:00:00.0000000','1','6','4',N'S2','09:45:00.0000000');
SET IDENTITY_INSERT [dbo].[time_slots] OFF;

-- semesters (IDENTITY)
SET IDENTITY_INSERT [dbo].[semesters] ON;
INSERT INTO [dbo].[semesters] ([id],[academic_year],[code],[created_at],[description],[end_date],[name],[registration_end],[registration_start],[start_date],[status],[term],[updated_at]) VALUES
('1',N'2024-2025',N'HK1','2026-03-01 23:15:00.771575',N'Học kỳ 1 cho khoá K25','2025-01-15',N'Học kỳ 1','2024-08-25','2024-08-01','2024-09-02',N'OPEN','1','2026-03-01 23:15:00.771575');
SET IDENTITY_INSERT [dbo].[semesters] OFF;

-- rooms (IDENTITY)
SET IDENTITY_INSERT [dbo].[rooms] ON;
INSERT INTO [dbo].[rooms] ([id],[area],[capacity],[created_at],[floor],[is_active],[room_code],[room_name],[status],[updated_at],[building_id],[room_type_id]) VALUES
('1','100','50','2026-02-27 17:02:52.088689','1','1',N'P101',N'Phòng học lý thuyết',N'AVAILABLE','2026-02-27 17:02:52.088689','C2F293AD-31C1-4755-8BF0-22822BA7AB3B','A884DF6C-0772-4793-AC95-9F557FFFB358'),
('2','100','50','2026-03-08 12:22:52.459900','1','1',N'P102',N'Phòng học lý thuyết',N'AVAILABLE','2026-03-08 12:22:52.459900','C2F293AD-31C1-4755-8BF0-22822BA7AB3B','A884DF6C-0772-4793-AC95-9F557FFFB358');
SET IDENTITY_INSERT [dbo].[rooms] OFF;

-- majors
INSERT INTO [dbo].[majors] ([major_id],[major_code],[major_name],[faculty_id]) VALUES
('42F7105F-E1A9-4133-B0A1-8785290FE880',NULL,N'Công nghệ phần mềm','2D290C0F-7BBD-44A5-957C-7C54525A2040');

-- courses
INSERT INTO [dbo].[courses] ([id],[course_code],[course_name],[created_at],[credits],[description],[lecture_hours],[practice_hours],[status],[updated_at],[faculty_id]) VALUES
('99D6E0AD-78AC-4510-80E2-B18F0FF598B4',N'T1',N'Toán','2026-02-24 17:31:35.953775','3',N'','52','0','1','2026-02-24 17:31:35.953819','2D290C0F-7BBD-44A5-957C-7C54525A2040'),
('CCF11DE2-791A-4F6B-88D2-F5CAB381BD15',N'TCC01',N'Toán cao cấp','2026-02-13 15:19:55.768701','3',N'Toán cao cấp giúp học nâng cao kiến thức nền tảng','45','0','1','2026-02-13 15:19:55.768778','2D290C0F-7BBD-44A5-957C-7C54525A2040'),
('71B08C6F-C954-4CE0-9063-23708C5A5018',N'KTLT',N'Kỹ thuật lập trình','2026-03-21 11:31:37.656697','3',N'','25','20','1','2026-03-21 11:31:37.656735','2D290C0F-7BBD-44A5-957C-7C54525A2040');

-- lecturers (phụ thuộc departments, lecturer_duties, faculties, positions)
INSERT INTO [dbo].[lecturers] ([lecturer_id],[academic_degree],[academic_title],[address],[avatar],[citizen_id],[date_of_birth],[email],[full_name],[gender],[lecturer_code],[phone_number],[faculty_id],[position_id],[lecturer_duty_id],[department_id]) VALUES
('BE63BD27-DDF6-4036-BFC5-4C8D6EB111BC',NULL,N'Giảng viên chính',N'73 Hoàng Thành, Thuận Thành, Huuế',N'/uploads/avatars/6596650c-5c94-436a-87a3-84bde5bd8059_HTD.jpeg',N'1999888779','1992-02-02',N'tien@gmail.com',N'Nguyễn Văn Tiến',N'Nam',N'CH002',N'086899933','2D290C0F-7BBD-44A5-957C-7C54525A2040','6C16960F-F12B-4EA4-AD4E-F8B795FF2A4E','0BC91085-B87B-49E4-8701-B09705177902',NULL),
('1DD48B3E-3D4B-4435-932A-C019E73DFD9A',N'Thạc sĩ',N'Giảng viên chính',N'Đà Nẵng, Việt Nam',N'/uploads/avatars/d83441c8-3230-4161-8be9-06d54fe4f075_HTD.jpeg',N'1999888777','2006-01-01',N'an@donga.edu.com',N'Hoàng Trung Dũng',N'Nam',N'CH001',N'0868686868','2D290C0F-7BBD-44A5-957C-7C54525A2040','6C16960F-F12B-4EA4-AD4E-F8B795FF2A4E','2A5629EB-FAF9-4BC7-8C84-F77D462FFA1B','EBF61413-4FE0-418E-881F-9E28E8FAD0E9');

-- training_programs (phụ thuộc majors)
INSERT INTO [dbo].[training_programs] ([program_id],[course],[description],[duration_years],[is_active],[program_code],[program_name],[total_credits],[major_id]) VALUES
('3BDE44A9-17B8-4B02-824F-50842B2B9ADB',N'K25',N'Chương trình đào tạo được cập nhật theo chương trình mới 2026','4','1',N'CNTT-K25',N'Chương trình đào tạo Công nghệ thông tin','152','42F7105F-E1A9-4133-B0A1-8785290FE880');

-- classes (phụ thuộc majors, education_types, training_levels, lecturers)
INSERT INTO [dbo].[classes] ([class_id],[academic_year],[class_code],[class_name],[class_status],[created_at],[education_type],[is_active],[max_student],[training_level],[updated_at],[major_id],[education_type_id],[training_level_id],[academic_advisor_lecturer_id]) VALUES
('C72BB2D4-A9BE-4069-905E-1F683AAD9D59',N'2025-2029',N'IT25A',N'công nghệ thông tin A',N'Đang học','2026-02-07 11:06:10.822154',NULL,'1','50',NULL,'2026-03-24 23:33:58.950961','42F7105F-E1A9-4133-B0A1-8785290FE880','F35EF5FE-08AB-4076-99FD-DD7F38201734','8947D0A3-1C41-4D30-B553-DF25E5D162D2','1DD48B3E-3D4B-4435-932A-C019E73DFD9A'),
('601CEAAF-D7A7-492F-8513-DAB37DBDD369',N'2025-2029',N'ST23A',N'Lớp blockchain',N'Đang học','2026-03-25 00:12:29.618661',NULL,'1','50',NULL,'2026-03-25 00:13:42.699483','42F7105F-E1A9-4133-B0A1-8785290FE880','F35EF5FE-08AB-4076-99FD-DD7F38201734','8947D0A3-1C41-4D30-B553-DF25E5D162D2','BE63BD27-DDF6-4036-BFC5-4C8D6EB111BC');

-- students (phụ thuộc classes)
INSERT INTO [dbo].[students] ([student_id],[address],[avatar],[citizen_id],[date_of_birth],[email],[full_name],[gender],[phone_number],[student_code],[class_id]) VALUES
('893D0B09-6B84-4EDC-B9C6-A87CD993D6A4',N'Quảng Nam, Đà Nẵng',N'/uploads/avatars/e22a00f8-d5f8-49d4-bd95-fefc4c21bd15_avt.jpg',N'1999888777','2005-01-01',N'an@gmail.com',N'Nguyễn Văn An',N'Nam',N'0123321123',N'SV001','C72BB2D4-A9BE-4069-905E-1F683AAD9D59'),
('04B61953-0B82-4571-B905-37D5A11BD112',N'Quảng Nam, Đà Nẵng',N'/uploads/avatars/f4b2c135-ba0b-4e1a-a569-bf033ede9fc0_avtr huy.jpg',N'198888888','2005-02-01',N'namson@gmail.com',N'Trương Nam Sơn',N'Nam',N'0123987789',N'SV002','C72BB2D4-A9BE-4069-905E-1F683AAD9D59');

-- class_sections (IDENTITY | phụ thuộc courses, semesters, rooms, classes)
SET IDENTITY_INSERT [dbo].[class_sections] ON;
INSERT INTO [dbo].[class_sections] ([id],[class_code],[class_name],[created_at],[current_students],[max_students],[note],[room],[status],[updated_at],[course_id],[semester_id],[room_id],[administrative_class_id]) VALUES
('1',N'IT25A',N'công nghệ thông tin A','2026-03-01 23:25:23.201974','52','50',N'',N'A01',N'OPEN','2026-03-09 17:04:36.228117','CCF11DE2-791A-4F6B-88D2-F5CAB381BD15','1','1',NULL),
('2',N'T1',N'Toán Giải tích 1','2026-03-02 20:19:00.364226','52','50',N'',NULL,N'OPEN','2026-03-09 17:04:48.116405','99D6E0AD-78AC-4510-80E2-B18F0FF598B4','1','1',NULL);
SET IDENTITY_INSERT [dbo].[class_sections] OFF;

-- users (phụ thuộc lecturers, students)
INSERT INTO [dbo].[users] ([id],[email],[enabled],[password],[username],[lecturer_id],[student_id]) VALUES
('9DBD6338-997C-4558-948E-039786D88D86',N'loi@gmail.com','1',N'$2a$10$CdC31VAjDGfJSOMUFqdV5eaiyFB7TnTn7TPfWHiQOTVPOr2oXBTNm',N'NgocLoi',NULL,NULL),
('6D2CF87C-1CF6-4236-88C0-20793AB83312',N'hieu102056@donga.edu.vn','1',N'123456',N'admin',NULL,NULL),
('54C80879-6759-4FE0-8AA1-4DAF0907DECA',N'tien@gmail.com','1',N'$2a$10$7GJshyLYnF1RiErLshfie..XvXFcfjKTicz3/odgfyezYiwnBvNaO',N'TranTien',NULL,NULL),
('50E3DC81-4BBC-4897-8600-A766EA6D4B00',N'DucHuy@gmail.com','1',N'123456',N'DucHuy',NULL,NULL),
('C6BC5113-77C3-4A82-801A-C1E07DED8E9B',N'namson@gmail.com','1',N'$2a$10$TcqwM9P.EEtfCOoBUwTZpeOEEYRjIoVU0bhfy2Z.HjsWtcp6Gvnyu',N'NamSon',NULL,'04B61953-0B82-4571-B905-37D5A11BD112'),
('C186C9A4-DCD7-4DC1-9B9C-FF03AEA93100',N'admin1@admin.com','1',N'$2a$10$jgZBqlM2584MYuwPJ/e0hey3mndh3x2ZUbCUH3LlYL0c90nwqnVRi',N'admin1',NULL,NULL);

-- course_prerequisites (phụ thuộc courses)
INSERT INTO [dbo].[course_prerequisites] ([id],[course_id],[prerequisite_course_id]) VALUES
('6A06B64C-33A0-45EA-AD4D-FF94C65DECEB','CCF11DE2-791A-4F6B-88D2-F5CAB381BD15','99D6E0AD-78AC-4510-80E2-B18F0FF598B4'),
('0D1003C4-FD5D-4D5D-8E08-644BBD7748E8','71B08C6F-C954-4CE0-9063-23708C5A5018','CCF11DE2-791A-4F6B-88D2-F5CAB381BD15');

-- course_registrations (IDENTITY | phụ thuộc class_sections, students)
SET IDENTITY_INSERT [dbo].[course_registrations] ON;
INSERT INTO [dbo].[course_registrations] ([id],[note],[registered_at],[class_section_id],[student_id]) VALUES
('1',N'','2026-03-03 15:31:32.086502','2','893D0B09-6B84-4EDC-B9C6-A87CD993D6A4'),
('2',N'','2026-03-03 15:32:33.738483','1','893D0B09-6B84-4EDC-B9C6-A87CD993D6A4'),
('3',N'','2026-03-09 17:04:36.179710','1','04B61953-0B82-4571-B905-37D5A11BD112'),
('4',N'','2026-03-09 17:04:48.112256','2','04B61953-0B82-4571-B905-37D5A11BD112');
SET IDENTITY_INSERT [dbo].[course_registrations] OFF;

-- documents (phụ thuộc users, courses)
INSERT INTO [dbo].[documents] ([document_id],[created_at],[description],[file_type],[file_url],[title],[subject_id],[uploaded_by]) VALUES
('EB2466FE-C7A0-41D3-8B3D-9587CCA31E47','2026-03-21 11:30:26.225003',N'kỹ thuật lập trình',N'pdf',N'/uploads/documents/254495d8-162f-4172-af48-fea1e1824ce6_699225_20183554_NguyenQuangHuy_BaiTH_05.docx.pdf',N'Kỹ thuật lập trình','71B08C6F-C954-4CE0-9063-23708C5A5018','9DBD6338-997C-4558-948E-039786D88D86');

-- equipments (IDENTITY | phụ thuộc rooms)
SET IDENTITY_INSERT [dbo].[equipments] ON;
INSERT INTO [dbo].[equipments] ([id],[created_at],[equipment_code],[equipment_name],[purchase_date],[serial_number],[status],[updated_at],[room_id]) VALUES
('1','2026-02-28 11:24:00.645327',N'Q01',N'Quạt trần','2023-01-01',N'123456',N'ACTIVE','2026-02-28 11:24:00.645327','1');
SET IDENTITY_INSERT [dbo].[equipments] OFF;

-- exam_rooms (phụ thuộc rooms)
INSERT INTO [dbo].[exam_rooms] ([id],[created_at],[description],[exam_capacity],[updated_at],[room_id]) VALUES
('2E279DB6-1EE9-4ED9-A989-1EBE9E91CDC6','2026-03-16 09:24:29.751518',NULL,'50','2026-03-16 09:24:29.751556','1');

-- feedbacks (phụ thuộc students, courses, lecturers)
INSERT INTO [dbo].[feedbacks] ([feedback_id],[comment],[created_at],[rating],[lecturer_id],[student_id],[subject_id]) VALUES
('B7EFAFD6-ECF0-47FA-867B-1C31454B1464',N'Giảng viên giảng rất hay','2026-03-22 10:02:45.818671','5','1DD48B3E-3D4B-4435-932A-C019E73DFD9A','04B61953-0B82-4571-B905-37D5A11BD112','71B08C6F-C954-4CE0-9063-23708C5A5018');

-- grade_components (phụ thuộc class_sections)
INSERT INTO [dbo].[grade_components] ([id],[component_name],[created_at],[max_score],[weight],[course_class_id]) VALUES
('714E0799-E046-49AC-95F6-EF367634F077',N'Chuyên Cần','2026-03-09 07:53:48.419249','10.00','10.00','1'),
('32CC5421-25EB-4FDF-8320-96A6A30887EA',N'Thường Xuyên','2026-03-09 07:54:03.064638','10.00','25.00','1'),
('41D14521-7A7E-471C-981A-D6762B23ED74',N'Giữa kỳ','2026-03-09 07:54:17.164291','10.00','15.00','1'),
('DDA28B03-9123-470F-BCC4-2486D8474F8B',N'Kết thúc học phần','2026-03-09 07:55:00.056146','10.00','50.00','1');

-- exam_schedules (phụ thuộc class_sections, exam_types)
INSERT INTO [dbo].[exam_schedules] ([id],[created_at],[duration_minutes],[exam_date],[note],[start_time],[updated_at],[class_section_id],[exam_type_id]) VALUES
('A8F6E74C-4C0A-4858-A6EA-F139B470354A','2026-03-16 09:45:25.230848','60','2026-01-11',NULL,'09:00:00.0000000','2026-03-16 09:45:25.230872','1','F0E09B39-C171-4B9D-8BDF-3A1EAC0572B5');

-- graduation_conditions (IDENTITY | phụ thuộc training_programs)
SET IDENTITY_INSERT [dbo].[graduation_conditions] ON;
INSERT INTO [dbo].[graduation_conditions] ([id],[created_at],[min_credits],[min_gpa],[required_certificate],[required_courses],[updated_at],[program_id]) VALUES
('1','2026-03-17 08:08:59.195762','152','2.00',N'Ngoại ngữ Toeic 500, Giáo dục thể chất, Chứng chỉ Quốc phòng An ninh',N'Đồ Án tốt nghiệp
Thực tập tốt nghiệp','2026-03-17 08:11:34.388478','3BDE44A9-17B8-4B02-824F-50842B2B9ADB');
SET IDENTITY_INSERT [dbo].[graduation_conditions] OFF;

-- graduation_results (IDENTITY | phụ thuộc students, training_programs)
SET IDENTITY_INSERT [dbo].[graduation_results] ON;
INSERT INTO [dbo].[graduation_results] ([id],[certificates],[checked_at],[created_at],[gpa],[missing_courses],[note],[status],[total_credits],[updated_at],[program_id],[student_id]) VALUES
('1',N'Ngoại ngữ B1, Giáo dục thể chất','2026-03-18 20:46:08.533133','2026-03-18 20:46:08.533102','3.00',NULL,NULL,N'ELIGIBLE','152','2026-03-18 20:46:08.533130','3BDE44A9-17B8-4B02-824F-50842B2B9ADB','893D0B09-6B84-4EDC-B9C6-A87CD993D6A4');
SET IDENTITY_INSERT [dbo].[graduation_results] OFF;

-- lecturer_course_classes (IDENTITY | phụ thuộc class_sections, lecturers)
SET IDENTITY_INSERT [dbo].[lecturer_course_classes] ON;
INSERT INTO [dbo].[lecturer_course_classes] ([id],[created_at],[note],[updated_at],[class_section_id],[lecturer_id]) VALUES
('1','2026-03-02 20:16:01.101152',N'','2026-03-02 20:16:01.101152','1','1DD48B3E-3D4B-4435-932A-C019E73DFD9A'),
('2','2026-03-02 20:20:17.983611',N'Toán giải tích 1 cơ bản','2026-03-02 20:20:17.983611','2','1DD48B3E-3D4B-4435-932A-C019E73DFD9A'),
('10002','2026-03-24 23:42:57.685051',N'','2026-03-24 23:42:57.685051','1','BE63BD27-DDF6-4036-BFC5-4C8D6EB111BC');
SET IDENTITY_INSERT [dbo].[lecturer_course_classes] OFF;

-- notifications (phụ thuộc users)
INSERT INTO [dbo].[notifications] ([id],[category],[content],[created_at],[created_by],[is_read],[read_at],[scheduled_at],[title],[recipient_user_id],[source_id],[source_type]) VALUES
('DE8F1780-8186-4760-853C-0962A6751B5E',N'TUITION_FEE',N'Cần đóng học phí ngay trước khi thi','2026-03-20 20:37:08.434307',NULL,'0',NULL,'2026-03-20 20:37:00.000000',N'Học phí','C6BC5113-77C3-4A82-801A-C1E07DED8E9B',NULL,NULL);

-- role_permissions (phụ thuộc roles, permissions)
INSERT INTO [dbo].[role_permissions] ([role_id],[permission_id],[id]) VALUES
('F4878407-DB22-4AB5-A783-6A6D661AEEAB','515A6E39-03B8-4ABE-92EF-E950705344D3','511F4B9F-B694-44CE-BE6C-8B440C97A785');

-- room_block_times (phụ thuộc rooms, time_slots)
INSERT INTO [dbo].[room_block_times] ([block_id],[block_type],[created_at],[day_of_week],[end_date],[end_week],[reason],[start_date],[start_week],[status],[updated_at],[room_id],[time_slot_id]) VALUES
('ECC6E54E-3543-4A86-9FBD-A03EC510038F',N'EVENT','2026-03-08 12:24:01.248004','3','2025-02-15','2',N'Phòng sử dụng để tổ chức hội nghị tuyển sinh','2025-02-01','1',N'CANCELLED','2026-03-08 12:24:01.248004','2','1');

-- schedules (phụ thuộc rooms, semesters, time_slots, class_sections, lecturers)
INSERT INTO [dbo].[schedules] ([id],[created_at],[created_by],[day_of_week],[end_week],[note],[schedule_type],[session_type],[start_week],[status],[updated_at],[week_pattern],[class_section_id],[lecturer_id],[room_id],[semester_id],[time_slot_id]) VALUES
('6CF0305D-9178-4654-ABB2-44F1DFA2C0CF','2026-03-25 11:47:45.348462',NULL,'2','15',NULL,N'NORMAL',N'THEORY','1',N'ACTIVE','2026-03-25 11:47:45.348462',N'ALL','2','1DD48B3E-3D4B-4435-932A-C019E73DFD9A','1','1','2'),
('81001A74-A7E2-4502-B17A-8C2D0F2A416D','2026-03-25 11:48:31.634870',NULL,'2','15',NULL,N'NORMAL',N'THEORY','1',N'ACTIVE','2026-03-25 11:48:31.634870',N'ALL','1','1DD48B3E-3D4B-4435-932A-C019E73DFD9A','1','1','1'),
('F475D445-A2F4-4DEC-BCB2-E4290D8B2CD2','2026-03-25 11:48:31.635237',NULL,'3','15',NULL,N'NORMAL',N'THEORY','1',N'ACTIVE','2026-03-25 11:48:31.635237',N'ALL','1','BE63BD27-DDF6-4036-BFC5-4C8D6EB111BC','1','1','1');

-- student_grades (phụ thuộc lecturers, grade_components, students, class_sections)
INSERT INTO [dbo].[student_grades] ([id],[graded_at],[score],[updated_at],[course_class_id],[grade_component_id],[graded_by],[student_id]) VALUES
('A8882F70-EFBB-4A3A-B7DD-32B2B478787E','2026-03-10 11:56:12.794607','10.00','2026-03-10 11:56:12.797876','1','714E0799-E046-49AC-95F6-EF367634F077','1DD48B3E-3D4B-4435-932A-C019E73DFD9A','04B61953-0B82-4571-B905-37D5A11BD112'),
('1BAEFA0A-69CA-479C-BD88-01E607C58349','2026-03-10 15:57:38.269269','9.00','2026-03-10 15:57:38.272186','1','41D14521-7A7E-471C-981A-D6762B23ED74','1DD48B3E-3D4B-4435-932A-C019E73DFD9A','893D0B09-6B84-4EDC-B9C6-A87CD993D6A4'),
('1C0A110C-E0B2-4B0F-9576-773C8BBB4810','2026-03-10 15:57:54.545869','9.00','2026-03-10 15:57:54.546138','1','32CC5421-25EB-4FDF-8320-96A6A30887EA','1DD48B3E-3D4B-4435-932A-C019E73DFD9A','893D0B09-6B84-4EDC-B9C6-A87CD993D6A4'),
('F33C9163-B04A-4C5D-96B2-FCEF608A84B6','2026-03-10 15:58:10.947607','8.50','2026-03-10 15:58:10.948719','1','DDA28B03-9123-470F-BCC4-2486D8474F8B','1DD48B3E-3D4B-4435-932A-C019E73DFD9A','893D0B09-6B84-4EDC-B9C6-A87CD993D6A4'),
('80C422F8-F72B-4C17-89EA-0771B648B1E3','2026-03-10 15:58:45.603928','10.00','2026-03-10 15:58:45.604321','1','714E0799-E046-49AC-95F6-EF367634F077','1DD48B3E-3D4B-4435-932A-C019E73DFD9A','893D0B09-6B84-4EDC-B9C6-A87CD993D6A4');

-- student_tuition (phụ thuộc students, semesters) → PHẢI trước payments
INSERT INTO [dbo].[student_tuition] ([id],[amount_paid],[created_at],[remaining_amount],[status],[total_amount],[total_credits],[updated_at],[semester_id],[student_id]) VALUES
('07C713F3-03E3-4D83-A0C9-39C67EAEDC21','10000000','2026-03-13 10:42:26.951113','9800000',N'PARTIAL','19800000','22','2026-03-13 10:42:26.951113','1','893D0B09-6B84-4EDC-B9C6-A87CD993D6A4'),
('329B7C4A-6D4B-4932-9231-FB9DA466C1DF','0','2026-03-13 10:40:30.795737','21600000',N'UNPAID','21600000','24','2026-03-13 10:40:30.795737','1','04B61953-0B82-4571-B905-37D5A11BD112');

-- tuition_fees (phụ thuộc training_programs)
INSERT INTO [dbo].[tuition_fees] ([id],[created_at],[effective_date],[fee_per_credit],[note],[status],[updated_at],[program_id]) VALUES
('E4F38EBF-C18A-4D61-9011-4C7F349C4353','2026-03-11 08:54:53.926461','2026-03-11','900000',NULL,N'ACTIVE','2026-03-11 08:54:53.926483','3BDE44A9-17B8-4B02-824F-50842B2B9ADB');

-- user_roles (phụ thuộc users, roles)
INSERT INTO [dbo].[user_roles] ([user_id],[role_id]) VALUES
('9DBD6338-997C-4558-948E-039786D88D86','F4878407-DB22-4AB5-A783-6A6D661AEEAB'),
('6D2CF87C-1CF6-4236-88C0-20793AB83312','80612F4C-3A9E-4A4B-8607-6B8EF2055819'),
('54C80879-6759-4FE0-8AA1-4DAF0907DECA','E4530BE1-F109-45A5-9666-7C09E496289E'),
('50E3DC81-4BBC-4897-8600-A766EA6D4B00','2E8F79AB-A27C-4A42-AC41-9DFCD69F0040'),
('C6BC5113-77C3-4A82-801A-C1E07DED8E9B','E4530BE1-F109-45A5-9666-7C09E496289E'),
('C186C9A4-DCD7-4DC1-9B9C-FF03AEA93100','80612F4C-3A9E-4A4B-8607-6B8EF2055819');

-- payments (IDENTITY | phụ thuộc student_tuition → phải CUỐI cùng trong chuỗi này)
SET IDENTITY_INSERT [dbo].[payments] ON;
INSERT INTO [dbo].[payments] ([id],[amount],[created_at],[payment_date],[payment_method],[status],[transaction_code],[updated_at],[student_tuition_id]) VALUES
('1','10000000','2026-03-14 17:32:44.533949','2026-03-14 17:32:00.000000',N'BANK_TRANSFER',N'COMPLETED',N'TX001','2026-03-14 17:32:44.533949','07C713F3-03E3-4D83-A0C9-39C67EAEDC21');
SET IDENTITY_INSERT [dbo].[payments] OFF;

-- attendances (phụ thuộc students, lecturers, class_sections)
INSERT INTO [dbo].[attendances] ([attendance_id],[attendance_date],[created_at],[marked_at],[note],[present],[updated_at],[course_class_id],[marked_by],[student_id]) VALUES
('F4A129B2-4E08-435E-B93B-0D8E3C5D57B0','2026-03-23','2026-03-23 19:23:49.368607','2026-03-23 19:23:49.365551',NULL,'1','2026-03-23 19:23:49.368607','1','1DD48B3E-3D4B-4435-932A-C019E73DFD9A','04B61953-0B82-4571-B905-37D5A11BD112');

DROP TABLE IF EXISTS [dbo].[audit_logs];
CREATE TABLE [dbo].[audit_logs] (
    [id] uniqueidentifier,
    [action] varchar(20),
    [created_at] datetime2(6),
    [description] nvarchar(1000),
    [http_method] varchar(10),
    [ip_address] varchar(64),
    [module_name] nvarchar(100),
    [request_path] nvarchar(300),
    [status_code] int,
    [target_id] nvarchar(100),
    [user_agent] nvarchar(255),
    [username] nvarchar(100),
    PRIMARY KEY ([id])
);

INSERT INTO [dbo].[audit_logs] ([id], [action], [created_at], [description], [http_method], [ip_address], [module_name], [request_path], [status_code], [target_id], [user_agent], [username]) VALUES
('86DE3E6A-4BDD-4D2B-8408-1F7790B33964', N'CREATE', '2026-03-27 11:00:49.044186', N'CREATE faculties (302) path=/admin/faculties', N'POST', N'0:0:0:0:0:0:0:1', N'faculties', N'/admin/faculties', '302', NULL, N'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36', N'admin1');

