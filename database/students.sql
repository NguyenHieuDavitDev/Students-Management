-- DROP SCHEMA dbo;

CREATE SCHEMA dbo;
-- student_db.dbo.buildings definition

-- Drop table

-- DROP TABLE student_db.dbo.buildings;

CREATE TABLE student_db.dbo.buildings (
	building_id uniqueidentifier NOT NULL,
	address nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	building_code varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	building_name nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	description varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	number_of_floors int NULL,
	total_area float NULL,
	CONSTRAINT PK__building__9C9FBF7F8917B3C7 PRIMARY KEY (building_id),
	CONSTRAINT UKrmelgq5kegpd644ap0pxxa97n UNIQUE (building_code)
);


-- student_db.dbo.education_types definition

-- Drop table

-- DROP TABLE student_db.dbo.education_types;

CREATE TABLE student_db.dbo.education_types (
	education_type_id uniqueidentifier NOT NULL,
	created_at datetime2(6) NULL,
	education_type_name nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	is_active bit NOT NULL,
	updated_at datetime2(6) NULL,
	CONSTRAINT PK__educatio__646DC1A45A5B0C9C PRIMARY KEY (education_type_id),
	CONSTRAINT UKn5xsclbo2fg4d4y4hscv1ch3e UNIQUE (education_type_name)
);


-- student_db.dbo.exam_types definition

-- Drop table

-- DROP TABLE student_db.dbo.exam_types;

CREATE TABLE student_db.dbo.exam_types (
	id uniqueidentifier NOT NULL,
	created_at datetime2(6) NULL,
	description nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	name nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	updated_at datetime2(6) NULL,
	CONSTRAINT PK__exam_typ__3213E83F42EA0CD8 PRIMARY KEY (id)
);


-- student_db.dbo.faculties definition

-- Drop table

-- DROP TABLE student_db.dbo.faculties;

CREATE TABLE student_db.dbo.faculties (
	faculty_id uniqueidentifier NOT NULL,
	faculty_code nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	faculty_name nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK__facultie__7B00413CE60FD096 PRIMARY KEY (faculty_id),
	CONSTRAINT UK3ff9r9bpq7fpqejwxylgy24v9 UNIQUE (faculty_code)
);


-- student_db.dbo.grade_scales definition

-- Drop table

-- DROP TABLE student_db.dbo.grade_scales;

CREATE TABLE student_db.dbo.grade_scales (
	id uniqueidentifier NOT NULL,
	description nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	grade_point numeric(3,2) NOT NULL,
	letter_grade varchar(2) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	max_score numeric(4,2) NOT NULL,
	min_score numeric(4,2) NOT NULL,
	CONSTRAINT PK__grade_sc__3213E83FE0AD4B16 PRIMARY KEY (id)
);


-- student_db.dbo.permissions definition

-- Drop table

-- DROP TABLE student_db.dbo.permissions;

CREATE TABLE student_db.dbo.permissions (
	id uniqueidentifier NOT NULL,
	[action] nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	description nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	name nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	resource nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	code nvarchar(80) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	CONSTRAINT PK__permissi__3213E83F75BD8E50 PRIMARY KEY (id),
	CONSTRAINT UK7lcb6glmvwlro3p2w2cewxtvd UNIQUE (code),
	CONSTRAINT UKpnvtwliis6p05pn6i3ndjrqt2 UNIQUE (name)
);


-- student_db.dbo.positions definition

-- Drop table

-- DROP TABLE student_db.dbo.positions;

CREATE TABLE student_db.dbo.positions (
	position_id uniqueidentifier NOT NULL,
	position_code nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	position_name nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	description nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK__position__99A0E7A484A543F8 PRIMARY KEY (position_id),
	CONSTRAINT UK157cqj7049g15mma1tpx32l39 UNIQUE (position_code),
	CONSTRAINT UKb6lkwasxdrfpxihi038w6ixt6 UNIQUE (position_name)
);


-- student_db.dbo.roles definition

-- Drop table

-- DROP TABLE student_db.dbo.roles;

CREATE TABLE student_db.dbo.roles (
	id uniqueidentifier NOT NULL,
	description nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	name nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK__roles__3213E83FCD14AC83 PRIMARY KEY (id),
	CONSTRAINT UKofx66keruapi6vyqpv6f2or37 UNIQUE (name)
);


-- student_db.dbo.room_types definition

-- Drop table

-- DROP TABLE student_db.dbo.room_types;

CREATE TABLE student_db.dbo.room_types (
	room_type_id uniqueidentifier NOT NULL,
	description nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	max_capacity int NULL,
	room_type_code varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	room_type_name nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK__room_typ__42395E84D53052BE PRIMARY KEY (room_type_id),
	CONSTRAINT UKceedsn233pwaaeds15av1rgkc UNIQUE (room_type_code)
);


-- student_db.dbo.semesters definition

-- Drop table

-- DROP TABLE student_db.dbo.semesters;

CREATE TABLE student_db.dbo.semesters (
	id bigint IDENTITY(1,1) NOT NULL,
	academic_year nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	code nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	created_at datetime2(6) NOT NULL,
	description nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	end_date date NULL,
	name nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	registration_end date NULL,
	registration_start date NULL,
	start_date date NULL,
	status varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	term int NOT NULL,
	updated_at datetime2(6) NOT NULL,
	CONSTRAINT PK__semester__3213E83F9522FAD0 PRIMARY KEY (id),
	CONSTRAINT UKncllnspek0awc90guroynogp UNIQUE (code)
);
ALTER TABLE student_db.dbo.semesters WITH NOCHECK ADD CONSTRAINT CK__semesters__statu__3C34F16F CHECK (([status]='CLOSED' OR [status]='OPEN' OR [status]='UPCOMING'));


-- student_db.dbo.[templates/roles] definition

-- Drop table

-- DROP TABLE student_db.dbo.[templates/roles];

CREATE TABLE student_db.dbo.[templates/roles] (
	id uniqueidentifier NOT NULL,
	description nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	name nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK__template__3213E83F73CF76FB PRIMARY KEY (id),
	CONSTRAINT UKkobs6x9m8c2l34qe9vjut2wwu UNIQUE (name)
);


-- student_db.dbo.time_slots definition

-- Drop table

-- DROP TABLE student_db.dbo.time_slots;

CREATE TABLE student_db.dbo.time_slots (
	id int IDENTITY(1,1) NOT NULL,
	end_time time NOT NULL,
	is_active bit NOT NULL,
	period_end int NOT NULL,
	period_start int NOT NULL,
	slot_code nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	start_time time NOT NULL,
	CONSTRAINT PK__time_slo__3213E83FFD3AA8C9 PRIMARY KEY (id),
	CONSTRAINT UKad00dsfpfvan0p1pd78h4pg2j UNIQUE (slot_code)
);


-- student_db.dbo.training_levels definition

-- Drop table

-- DROP TABLE student_db.dbo.training_levels;

CREATE TABLE student_db.dbo.training_levels (
	training_level_id uniqueidentifier NOT NULL,
	training_level_name nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	description nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK__training__06C942D08AEF5405 PRIMARY KEY (training_level_id),
	CONSTRAINT UK1y55r7dmt2d01oo3yflthiqst UNIQUE (training_level_name)
);


-- student_db.dbo.courses definition

-- Drop table

-- DROP TABLE student_db.dbo.courses;

CREATE TABLE student_db.dbo.courses (
	id uniqueidentifier NOT NULL,
	course_code nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	course_name nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	created_at datetime2(6) NULL,
	credits int NULL,
	description nvarchar(1000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	lecture_hours int NULL,
	practice_hours int NULL,
	status bit NULL,
	updated_at datetime2(6) NULL,
	faculty_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__courses__3213E83F99AC277D PRIMARY KEY (id),
	CONSTRAINT UKp02ts69sh53ptd62m3c67v0 UNIQUE (course_code),
	CONSTRAINT FK81p0vixhqd3i5uwdk3sclg0nk FOREIGN KEY (faculty_id) REFERENCES student_db.dbo.faculties(faculty_id)
);


-- student_db.dbo.lecturers definition

-- Drop table

-- DROP TABLE student_db.dbo.lecturers;

CREATE TABLE student_db.dbo.lecturers (
	lecturer_id uniqueidentifier NOT NULL,
	academic_degree nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	academic_title nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	address nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	avatar varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	citizen_id varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	date_of_birth date NULL,
	email varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	full_name nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	gender nvarchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	lecturer_code varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	phone_number varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	faculty_id uniqueidentifier NOT NULL,
	position_id uniqueidentifier NULL,
	CONSTRAINT PK__lecturer__D4D1DAB1CB9344C9 PRIMARY KEY (lecturer_id),
	CONSTRAINT UKk6vsb5jy1uq700gx4a6brvtpv UNIQUE (lecturer_code),
	CONSTRAINT FK5s4iv2pp699ojmljjog3kioh FOREIGN KEY (faculty_id) REFERENCES student_db.dbo.faculties(faculty_id),
	CONSTRAINT FKthh38obs4te6njpjs8ab15l6c FOREIGN KEY (position_id) REFERENCES student_db.dbo.positions(position_id)
);


-- student_db.dbo.majors definition

-- Drop table

-- DROP TABLE student_db.dbo.majors;

CREATE TABLE student_db.dbo.majors (
	major_id uniqueidentifier NOT NULL,
	major_code nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	major_name nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	faculty_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__majors__DC7AC3C42D3D082C PRIMARY KEY (major_id),
	CONSTRAINT UKb0dvoetbm4xrbu48badhm1lln UNIQUE (major_code),
	CONSTRAINT UKqbrfdewrfc0gqn23389e7nwm5 UNIQUE (major_name,faculty_id),
	CONSTRAINT FKitqtm0b9li7x2h872rumqnqol FOREIGN KEY (faculty_id) REFERENCES student_db.dbo.faculties(faculty_id)
);


-- student_db.dbo.role_permissions definition

-- Drop table

-- DROP TABLE student_db.dbo.role_permissions;

CREATE TABLE student_db.dbo.role_permissions (
	role_id uniqueidentifier NOT NULL,
	permission_id uniqueidentifier NOT NULL,
	id uniqueidentifier NOT NULL,
	CONSTRAINT PK__role_per__C85A5463880C8B95 PRIMARY KEY (role_id,permission_id),
	CONSTRAINT UKt43p6aampim70fxxnkid1mibj UNIQUE (role_id,permission_id),
	CONSTRAINT FKegdk29eiy7mdtefy5c7eirr6e FOREIGN KEY (permission_id) REFERENCES student_db.dbo.permissions(id),
	CONSTRAINT FKn5fotdgk8d1xvo8nav9uv3muc FOREIGN KEY (role_id) REFERENCES student_db.dbo.roles(id)
);


-- student_db.dbo.rooms definition

-- Drop table

-- DROP TABLE student_db.dbo.rooms;

CREATE TABLE student_db.dbo.rooms (
	id bigint IDENTITY(1,1) NOT NULL,
	area float NULL,
	capacity int NULL,
	created_at datetime2(6) NOT NULL,
	floor int NULL,
	is_active bit NOT NULL,
	room_code nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	room_name nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	status nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	updated_at datetime2(6) NOT NULL,
	building_id uniqueidentifier NOT NULL,
	room_type_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__rooms__3213E83FFB8561D2 PRIMARY KEY (id),
	CONSTRAINT UKejc4trkinbxtajwetru2o8kdo UNIQUE (room_code),
	CONSTRAINT FKh9m2n1paq5hmd3u0klfl7wsfv FOREIGN KEY (room_type_id) REFERENCES student_db.dbo.room_types(room_type_id),
	CONSTRAINT FKojgn0sxhkfxd7pmmojnem9r4q FOREIGN KEY (building_id) REFERENCES student_db.dbo.buildings(building_id)
);
ALTER TABLE student_db.dbo.rooms WITH NOCHECK ADD CONSTRAINT CK__rooms__status__31B762FC CHECK (([status]='MAINTENANCE' OR [status]='IN_USE' OR [status]='AVAILABLE'));


-- student_db.dbo.training_programs definition

-- Drop table

-- DROP TABLE student_db.dbo.training_programs;

CREATE TABLE student_db.dbo.training_programs (
	program_id uniqueidentifier NOT NULL,
	course nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	description nvarchar(1000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	duration_years int NULL,
	is_active bit NULL,
	program_code nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	program_name nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	total_credits int NULL,
	major_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__training__3A7890AC8683100F PRIMARY KEY (program_id),
	CONSTRAINT UKrdoxywt0kicj6b4iycwvwkyo0 UNIQUE (program_code,major_id,course),
	CONSTRAINT FKhmwge471sdvweygytmshmdt8s FOREIGN KEY (major_id) REFERENCES student_db.dbo.majors(major_id)
);


-- student_db.dbo.tuition_fees definition

-- Drop table

-- DROP TABLE student_db.dbo.tuition_fees;

CREATE TABLE student_db.dbo.tuition_fees (
	id uniqueidentifier NOT NULL,
	created_at datetime2(6) NULL,
	effective_date date NOT NULL,
	fee_per_credit numeric(15,0) NOT NULL,
	note nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	status varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	updated_at datetime2(6) NULL,
	program_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__tuition___3213E83F1FBB6D09 PRIMARY KEY (id),
	CONSTRAINT FKgcqy6r5qrsf0hu1utc621lvou FOREIGN KEY (program_id) REFERENCES student_db.dbo.training_programs(program_id)
);
ALTER TABLE student_db.dbo.tuition_fees WITH NOCHECK ADD CONSTRAINT CK__tuition_f__statu__2EA5EC27 CHECK (([status]='INACTIVE' OR [status]='ACTIVE'));


-- student_db.dbo.class_sections definition

-- Drop table

-- DROP TABLE student_db.dbo.class_sections;

CREATE TABLE student_db.dbo.class_sections (
	id bigint IDENTITY(1,1) NOT NULL,
	class_code nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	class_name nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	created_at datetime2(6) NOT NULL,
	current_students int NOT NULL,
	max_students int NOT NULL,
	note nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	room nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	status varchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	updated_at datetime2(6) NULL,
	course_id uniqueidentifier NOT NULL,
	semester_id bigint NOT NULL,
	room_id bigint NULL,
	CONSTRAINT PK__class_se__3213E83FEC2F7ED7 PRIMARY KEY (id),
	CONSTRAINT FK3os9of6f2v5o94rgksgxfn24l FOREIGN KEY (room_id) REFERENCES student_db.dbo.rooms(id),
	CONSTRAINT FK695u7g15n5nfnaskhw3nhnsbw FOREIGN KEY (course_id) REFERENCES student_db.dbo.courses(id),
	CONSTRAINT FKtamlxyq4rm9ybyevrdtcbdbrb FOREIGN KEY (semester_id) REFERENCES student_db.dbo.semesters(id)
);
ALTER TABLE student_db.dbo.class_sections WITH NOCHECK ADD CONSTRAINT CK__class_sec__statu__40058253 CHECK (([status]='CANCELLED' OR [status]='CLOSED' OR [status]='OPEN'));


-- student_db.dbo.classes definition

-- Drop table

-- DROP TABLE student_db.dbo.classes;

CREATE TABLE student_db.dbo.classes (
	class_id uniqueidentifier NOT NULL,
	academic_year nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	class_code nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	class_name nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	class_status nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	created_at datetime2(6) NULL,
	education_type nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	is_active bit NULL,
	max_student int NULL,
	training_level nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	updated_at datetime2(6) NULL,
	major_id uniqueidentifier NOT NULL,
	education_type_id uniqueidentifier NULL,
	training_level_id uniqueidentifier NULL,
	CONSTRAINT PK__classes__FDF479862C6F7E47 PRIMARY KEY (class_id),
	CONSTRAINT UK3dmfbuj174uosjrycglymawul UNIQUE (class_code,academic_year),
	CONSTRAINT FK4daajpxy22fog83y3mqa48816 FOREIGN KEY (training_level_id) REFERENCES student_db.dbo.training_levels(training_level_id),
	CONSTRAINT FK6r9qmxcnxge92jgx4x4gltf0o FOREIGN KEY (major_id) REFERENCES student_db.dbo.majors(major_id),
	CONSTRAINT FKitg5hh7h9iywbvetbyth42ak8 FOREIGN KEY (education_type_id) REFERENCES student_db.dbo.education_types(education_type_id)
);


-- student_db.dbo.course_prerequisites definition

-- Drop table

-- DROP TABLE student_db.dbo.course_prerequisites;

CREATE TABLE student_db.dbo.course_prerequisites (
	id uniqueidentifier NOT NULL,
	course_id uniqueidentifier NOT NULL,
	prerequisite_course_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__course_p__3213E83FC8E73A10 PRIMARY KEY (id),
	CONSTRAINT UKphv2lwb0wsq410oqfp4enf9gq UNIQUE (course_id,prerequisite_course_id),
	CONSTRAINT FKhh4f1avebuvlv54m3j3l3pp36 FOREIGN KEY (course_id) REFERENCES student_db.dbo.courses(id),
	CONSTRAINT FKolf0a7iwh8c1mv9keyis4ebe8 FOREIGN KEY (prerequisite_course_id) REFERENCES student_db.dbo.courses(id)
);


-- student_db.dbo.equipments definition

-- Drop table

-- DROP TABLE student_db.dbo.equipments;

CREATE TABLE student_db.dbo.equipments (
	id bigint IDENTITY(1,1) NOT NULL,
	created_at datetime2(6) NOT NULL,
	equipment_code varchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	equipment_name nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	purchase_date date NULL,
	serial_number nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	status varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	updated_at datetime2(6) NOT NULL,
	room_id bigint NULL,
	CONSTRAINT PK__equipmen__3213E83FF107457F PRIMARY KEY (id),
	CONSTRAINT UK5mrbasb2pl3oc1puxiraohwek UNIQUE (equipment_code),
	CONSTRAINT FK5r50pavpda4dbw3p8n6rjikkq FOREIGN KEY (room_id) REFERENCES student_db.dbo.rooms(id)
);
ALTER TABLE student_db.dbo.equipments WITH NOCHECK ADD CONSTRAINT CK__equipment__statu__37703C52 CHECK (([status]='MAINTENANCE' OR [status]='BROKEN' OR [status]='ACTIVE'));


-- student_db.dbo.exam_rooms definition

-- Drop table

-- DROP TABLE student_db.dbo.exam_rooms;

CREATE TABLE student_db.dbo.exam_rooms (
	id uniqueidentifier NOT NULL,
	created_at datetime2(6) NULL,
	description nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	exam_capacity int NULL,
	updated_at datetime2(6) NULL,
	room_id bigint NOT NULL,
	CONSTRAINT PK__exam_roo__3213E83FCF197BA6 PRIMARY KEY (id),
	CONSTRAINT UKj53evxhuwm8jt79ayjhdqgvgi UNIQUE (room_id),
	CONSTRAINT FKeb4oab0wpuidk3kwoulgcop49 FOREIGN KEY (room_id) REFERENCES student_db.dbo.rooms(id)
);


-- student_db.dbo.exam_schedules definition

-- Drop table

-- DROP TABLE student_db.dbo.exam_schedules;

CREATE TABLE student_db.dbo.exam_schedules (
	id uniqueidentifier NOT NULL,
	created_at datetime2(6) NULL,
	duration_minutes int NOT NULL,
	exam_date date NOT NULL,
	note nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	start_time time NOT NULL,
	updated_at datetime2(6) NULL,
	class_section_id bigint NOT NULL,
	exam_type_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__exam_sch__3213E83F5C5BBB09 PRIMARY KEY (id),
	CONSTRAINT UKufro23m7hq32bbhoc0mdvdj7 UNIQUE (class_section_id,exam_type_id),
	CONSTRAINT FK1akuy67l7e43pakrt7pajk7ho FOREIGN KEY (class_section_id) REFERENCES student_db.dbo.class_sections(id),
	CONSTRAINT FK44gdqtyy5mhme35vacv0e9gs3 FOREIGN KEY (exam_type_id) REFERENCES student_db.dbo.exam_types(id)
);


-- student_db.dbo.grade_components definition

-- Drop table

-- DROP TABLE student_db.dbo.grade_components;

CREATE TABLE student_db.dbo.grade_components (
	id uniqueidentifier NOT NULL,
	component_name nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	created_at datetime2(6) NOT NULL,
	max_score numeric(4,2) NULL,
	weight numeric(5,2) NULL,
	course_class_id bigint NOT NULL,
	CONSTRAINT PK__grade_co__3213E83F1518665E PRIMARY KEY (id),
	CONSTRAINT FK1niqvtx529y9svn3l55odmdu9 FOREIGN KEY (course_class_id) REFERENCES student_db.dbo.class_sections(id)
);


-- student_db.dbo.graduation_conditions definition

-- Drop table

-- DROP TABLE student_db.dbo.graduation_conditions;

CREATE TABLE student_db.dbo.graduation_conditions (
	id bigint IDENTITY(1,1) NOT NULL,
	created_at datetime2(6) NULL,
	min_credits int NULL,
	min_gpa decimal(4,2) NULL,
	required_certificate nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	required_courses nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	updated_at datetime2(6) NULL,
	program_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__graduati__3213E83F4F23E494 PRIMARY KEY (id),
	CONSTRAINT FK3wpw1crbjtat5k6m4isoy7ipr FOREIGN KEY (program_id) REFERENCES student_db.dbo.training_programs(program_id)
);


-- student_db.dbo.lecturer_course_classes definition

-- Drop table

-- DROP TABLE student_db.dbo.lecturer_course_classes;

CREATE TABLE student_db.dbo.lecturer_course_classes (
	id bigint IDENTITY(1,1) NOT NULL,
	created_at datetime2(6) NOT NULL,
	note nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	updated_at datetime2(6) NULL,
	class_section_id bigint NOT NULL,
	lecturer_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__lecturer__3213E83F89D98DA7 PRIMARY KEY (id),
	CONSTRAINT UKbh8ygem062p9d1pr9hkbbltwb UNIQUE (class_section_id,lecturer_id),
	CONSTRAINT FK5rge75dqqakbup10yxa3c2gs FOREIGN KEY (lecturer_id) REFERENCES student_db.dbo.lecturers(lecturer_id),
	CONSTRAINT FKdcjngpfupr3e5nbwxvjn8ddqx FOREIGN KEY (class_section_id) REFERENCES student_db.dbo.class_sections(id)
);


-- student_db.dbo.room_block_times definition

-- Drop table

-- DROP TABLE student_db.dbo.room_block_times;

CREATE TABLE student_db.dbo.room_block_times (
	block_id uniqueidentifier NOT NULL,
	block_type nvarchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	created_at datetime2(6) NOT NULL,
	day_of_week int NULL,
	end_date date NULL,
	end_week int NULL,
	reason nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	start_date date NULL,
	start_week int NULL,
	status nvarchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	updated_at datetime2(6) NOT NULL,
	room_id bigint NOT NULL,
	time_slot_id int NULL,
	CONSTRAINT PK__room_blo__A67E647D7D8A28D4 PRIMARY KEY (block_id),
	CONSTRAINT FK7b6va70lvhgrjktbxw2w2ysc8 FOREIGN KEY (room_id) REFERENCES student_db.dbo.rooms(id),
	CONSTRAINT FK8tgguab0mb2vrti7n5o8fhnhp FOREIGN KEY (time_slot_id) REFERENCES student_db.dbo.time_slots(id)
);
ALTER TABLE student_db.dbo.room_block_times WITH NOCHECK ADD CONSTRAINT CK__room_bloc__block__0E391C95 CHECK (([block_type]='OTHER' OR [block_type]='EXAM' OR [block_type]='EVENT' OR [block_type]='MAINTENANCE'));
ALTER TABLE student_db.dbo.room_block_times WITH NOCHECK ADD CONSTRAINT CK__room_bloc__statu__0F2D40CE CHECK (([status]='CANCELLED' OR [status]='ACTIVE'));


-- student_db.dbo.schedules definition

-- Drop table

-- DROP TABLE student_db.dbo.schedules;

CREATE TABLE student_db.dbo.schedules (
	id uniqueidentifier NOT NULL,
	created_at datetime2(6) NULL,
	created_by bigint NULL,
	day_of_week int NOT NULL,
	end_week int NOT NULL,
	note nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	schedule_type varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	session_type varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	start_week int NOT NULL,
	status varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	updated_at datetime2(6) NULL,
	week_pattern varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	class_section_id bigint NOT NULL,
	lecturer_id uniqueidentifier NOT NULL,
	room_id bigint NOT NULL,
	semester_id bigint NOT NULL,
	time_slot_id int NOT NULL,
	CONSTRAINT PK__schedule__3213E83F0B21BC6E PRIMARY KEY (id),
	CONSTRAINT FK34r5t4jexlcas19pleifb8ihv FOREIGN KEY (room_id) REFERENCES student_db.dbo.rooms(id),
	CONSTRAINT FK3g45lyn6835mq4uyb8rv8e4pr FOREIGN KEY (class_section_id) REFERENCES student_db.dbo.class_sections(id),
	CONSTRAINT FK6xau0n9awp58ear4phhxssp7 FOREIGN KEY (time_slot_id) REFERENCES student_db.dbo.time_slots(id),
	CONSTRAINT FKi7fs951hibbtpd8l7kak45gbb FOREIGN KEY (semester_id) REFERENCES student_db.dbo.semesters(id),
	CONSTRAINT FKtnsx4uikatst6c88xvb7jim8t FOREIGN KEY (lecturer_id) REFERENCES student_db.dbo.lecturers(lecturer_id)
);
ALTER TABLE student_db.dbo.schedules WITH NOCHECK ADD CONSTRAINT CK__schedules__sched__69FBBC1F CHECK (([schedule_type]='EXTRA' OR [schedule_type]='MAKEUP' OR [schedule_type]='NORMAL'));
ALTER TABLE student_db.dbo.schedules WITH NOCHECK ADD CONSTRAINT CK__schedules__sessi__6AEFE058 CHECK (([session_type]='EXAM' OR [session_type]='PRACTICE' OR [session_type]='THEORY'));
ALTER TABLE student_db.dbo.schedules WITH NOCHECK ADD CONSTRAINT CK__schedules__statu__6BE40491 CHECK (([status]='MOVED' OR [status]='CANCELLED' OR [status]='ACTIVE'));
ALTER TABLE student_db.dbo.schedules WITH NOCHECK ADD CONSTRAINT CK__schedules__week___6CD828CA CHECK (([week_pattern]='EVEN' OR [week_pattern]='ODD' OR [week_pattern]='ALL'));


-- student_db.dbo.students definition

-- Drop table

-- DROP TABLE student_db.dbo.students;

CREATE TABLE student_db.dbo.students (
	student_id uniqueidentifier NOT NULL,
	address nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	avatar varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	citizen_id varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	date_of_birth date NULL,
	email varchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	full_name nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	gender nvarchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	phone_number varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	student_code varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	class_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__students__2A33069AF18199AB PRIMARY KEY (student_id),
	CONSTRAINT UKcgcf3r5xk73o0etbduc1qxnol UNIQUE (student_code),
	CONSTRAINT FKhnslh0rm5bthlble8vjunbnwe FOREIGN KEY (class_id) REFERENCES student_db.dbo.classes(class_id)
);


-- student_db.dbo.users definition

-- Drop table

-- DROP TABLE student_db.dbo.users;

CREATE TABLE student_db.dbo.users (
	id uniqueidentifier NOT NULL,
	email nvarchar(150) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	enabled bit NOT NULL,
	password varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	username nvarchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	lecturer_id uniqueidentifier NULL,
	student_id uniqueidentifier NULL,
	CONSTRAINT PK__users__3213E83FA005DAFA PRIMARY KEY (id),
	CONSTRAINT UK6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email),
	CONSTRAINT UKr43af9ap4edm43mmtq01oddj6 UNIQUE (username),
	CONSTRAINT FKc8nfkx91xbh5fv7a02092q1ip FOREIGN KEY (student_id) REFERENCES student_db.dbo.students(student_id),
	CONSTRAINT FKnq773gne4equ22e4nc7mk92xm FOREIGN KEY (lecturer_id) REFERENCES student_db.dbo.lecturers(lecturer_id)
);
 CREATE UNIQUE NONCLUSTERED INDEX UKa1v1u5edum24xvma8b9ykljqd ON student_db.dbo.users (  lecturer_id ASC  )  
	 WHERE  ([lecturer_id] IS NOT NULL)
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
 CREATE UNIQUE NONCLUSTERED INDEX UKqh3otyipv2k9hqte4a1abcyhq ON student_db.dbo.users (  student_id ASC  )  
	 WHERE  ([student_id] IS NOT NULL)
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;


-- student_db.dbo.course_registrations definition

-- Drop table

-- DROP TABLE student_db.dbo.course_registrations;

CREATE TABLE student_db.dbo.course_registrations (
	id bigint IDENTITY(1,1) NOT NULL,
	note nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	registered_at datetime2(6) NOT NULL,
	class_section_id bigint NOT NULL,
	student_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__course_r__3213E83F383B4D97 PRIMARY KEY (id),
	CONSTRAINT UK66c1mavy5wi1gok3aufpo0244 UNIQUE (student_id,class_section_id),
	CONSTRAINT FK1uitfc1h86q0t7ncnhlylbvab FOREIGN KEY (student_id) REFERENCES student_db.dbo.students(student_id),
	CONSTRAINT FK36e9k9ywnlm0y4bcrh5n6bfhh FOREIGN KEY (class_section_id) REFERENCES student_db.dbo.class_sections(id)
);


-- student_db.dbo.documents definition

-- Drop table

-- DROP TABLE student_db.dbo.documents;

CREATE TABLE student_db.dbo.documents (
	document_id uniqueidentifier NOT NULL,
	created_at datetime2(6) NULL,
	description nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	file_type nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	file_url nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	title nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	subject_id uniqueidentifier NOT NULL,
	uploaded_by uniqueidentifier NOT NULL,
	CONSTRAINT PK__document__9666E8AC1D2385A9 PRIMARY KEY (document_id),
	CONSTRAINT FK1ugacya4ssi0ilf8a9tjycgs6 FOREIGN KEY (uploaded_by) REFERENCES student_db.dbo.users(id),
	CONSTRAINT FKaxmoskj22pfhg2eeldnu75fpv FOREIGN KEY (subject_id) REFERENCES student_db.dbo.courses(id)
);


-- student_db.dbo.graduation_results definition

-- Drop table

-- DROP TABLE student_db.dbo.graduation_results;

CREATE TABLE student_db.dbo.graduation_results (
	id bigint IDENTITY(1,1) NOT NULL,
	certificates nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	checked_at datetime2(6) NULL,
	created_at datetime2(6) NULL,
	gpa decimal(4,2) NULL,
	missing_courses nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	note nvarchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	status varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	total_credits int NULL,
	updated_at datetime2(6) NULL,
	program_id uniqueidentifier NOT NULL,
	student_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__graduati__3213E83F5569927E PRIMARY KEY (id),
	CONSTRAINT UKmm54wu3ygksabastsgbd0oxha UNIQUE (student_id,program_id),
	CONSTRAINT FKi032pecot6qj6fjyv5ywhl5w3 FOREIGN KEY (program_id) REFERENCES student_db.dbo.training_programs(program_id),
	CONSTRAINT FKtb53cda5aj4t2xqjfjtb6cvgh FOREIGN KEY (student_id) REFERENCES student_db.dbo.students(student_id)
);
ALTER TABLE student_db.dbo.graduation_results WITH NOCHECK ADD CONSTRAINT CK__graduatio__statu__6ABAD62E CHECK (([status]='PENDING' OR [status]='NOT_ELIGIBLE' OR [status]='ELIGIBLE'));


-- student_db.dbo.notifications definition

-- Drop table

-- DROP TABLE student_db.dbo.notifications;

CREATE TABLE student_db.dbo.notifications (
	id uniqueidentifier NOT NULL,
	category varchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	content nvarchar(2000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	created_at datetime2(6) NOT NULL,
	created_by uniqueidentifier NULL,
	is_read bit DEFAULT 0 NOT NULL,
	read_at datetime2(6) NULL,
	scheduled_at datetime2(6) NULL,
	title nvarchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	recipient_user_id uniqueidentifier NOT NULL,
	source_id varchar(120) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	source_type varchar(60) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT PK__notifica__3213E83F3C3CE894 PRIMARY KEY (id),
	CONSTRAINT FKt8ievafor22iuvg5sd4p7lhbk FOREIGN KEY (recipient_user_id) REFERENCES student_db.dbo.users(id)
);
 CREATE UNIQUE NONCLUSTERED INDEX UKjwp85b16sv3ruvydx838ebkj1 ON student_db.dbo.notifications (  recipient_user_id ASC  , category ASC  , source_type ASC  , source_id ASC  )  
	 WHERE  ([recipient_user_id] IS NOT NULL AND [category] IS NOT NULL AND [source_type] IS NOT NULL AND [source_id] IS NOT NULL)
	 WITH (  PAD_INDEX = OFF ,FILLFACTOR = 100  ,SORT_IN_TEMPDB = OFF , IGNORE_DUP_KEY = OFF , STATISTICS_NORECOMPUTE = OFF , ONLINE = OFF , ALLOW_ROW_LOCKS = ON , ALLOW_PAGE_LOCKS = ON  )
	 ON [PRIMARY ] ;
ALTER TABLE student_db.dbo.notifications WITH NOCHECK ADD CONSTRAINT CK__notificat__categ__7073AF84 CHECK (([category]='OTHER' OR [category]='SYSTEM_WARNING' OR [category]='SCHEDULE_CHANGE' OR [category]='EXAM_SCHEDULE' OR [category]='TUITION_FEE'));


-- student_db.dbo.password_reset_tokens definition

-- Drop table

-- DROP TABLE student_db.dbo.password_reset_tokens;

CREATE TABLE student_db.dbo.password_reset_tokens (
	id uniqueidentifier NOT NULL,
	expiry_at datetimeoffset(6) NOT NULL,
	token varchar(64) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	user_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__password__3213E83F963A1289 PRIMARY KEY (id),
	CONSTRAINT UK71lqwbwtklmljk3qlsugr1mig UNIQUE (token),
	CONSTRAINT FKk3ndxg5xp6v7wd4gjyusp15gq FOREIGN KEY (user_id) REFERENCES student_db.dbo.users(id)
);


-- student_db.dbo.schedule_overrides definition

-- Drop table

-- DROP TABLE student_db.dbo.schedule_overrides;

CREATE TABLE student_db.dbo.schedule_overrides (
	override_id uniqueidentifier NOT NULL,
	approved_at datetime2(6) NULL,
	approved_by uniqueidentifier NULL,
	created_at datetime2(6) NULL,
	override_date date NOT NULL,
	override_type varchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	reason nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	status varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	updated_at datetime2(6) NULL,
	new_lecturer_id uniqueidentifier NULL,
	new_room_id bigint NULL,
	new_time_slot_id int NULL,
	schedule_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__schedule__2D0A781746F10275 PRIMARY KEY (override_id),
	CONSTRAINT FKahdj2tw0ntmmq2uv7hlyt2u0i FOREIGN KEY (new_time_slot_id) REFERENCES student_db.dbo.time_slots(id),
	CONSTRAINT FKfbb5a0bwnq8927sn3inkb1vlr FOREIGN KEY (new_lecturer_id) REFERENCES student_db.dbo.lecturers(lecturer_id),
	CONSTRAINT FKhbtw334pvq95c3mj83bu7h59q FOREIGN KEY (new_room_id) REFERENCES student_db.dbo.rooms(id),
	CONSTRAINT FKoodl2tghs09bov371b7bscgnb FOREIGN KEY (schedule_id) REFERENCES student_db.dbo.schedules(id)
);
ALTER TABLE student_db.dbo.schedule_overrides WITH NOCHECK ADD CONSTRAINT CK__schedule___overr__74794A92 CHECK (([override_type]='CANCEL' OR [override_type]='TIME_CHANGE' OR [override_type]='ROOM_CHANGE' OR [override_type]='MAKEUP'));
ALTER TABLE student_db.dbo.schedule_overrides WITH NOCHECK ADD CONSTRAINT CK__schedule___statu__756D6ECB CHECK (([status]='CANCELLED' OR [status]='ACTIVE'));


-- student_db.dbo.student_grades definition

-- Drop table

-- DROP TABLE student_db.dbo.student_grades;

CREATE TABLE student_db.dbo.student_grades (
	id uniqueidentifier NOT NULL,
	graded_at datetime2(6) NULL,
	score numeric(4,2) NULL,
	updated_at datetime2(6) NULL,
	course_class_id bigint NOT NULL,
	grade_component_id uniqueidentifier NOT NULL,
	graded_by uniqueidentifier NULL,
	student_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__student___3213E83F9E57EB15 PRIMARY KEY (id),
	CONSTRAINT UKp95kr0ge6dww9jbnuojx161sc UNIQUE (student_id,course_class_id,grade_component_id),
	CONSTRAINT FK23o9lbm2xnr8gamo6pload2ig FOREIGN KEY (course_class_id) REFERENCES student_db.dbo.class_sections(id),
	CONSTRAINT FK3k33iae4i8poyy8abietq65mi FOREIGN KEY (grade_component_id) REFERENCES student_db.dbo.grade_components(id),
	CONSTRAINT FKe8t3tau7ti61n06siogcuigkq FOREIGN KEY (student_id) REFERENCES student_db.dbo.students(student_id),
	CONSTRAINT FKqas2lr2sk839kcpbdw06k9shr FOREIGN KEY (graded_by) REFERENCES student_db.dbo.lecturers(lecturer_id)
);


-- student_db.dbo.student_tuition definition

-- Drop table

-- DROP TABLE student_db.dbo.student_tuition;

CREATE TABLE student_db.dbo.student_tuition (
	id uniqueidentifier NOT NULL,
	amount_paid numeric(18,0) NOT NULL,
	created_at datetime2(6) NULL,
	remaining_amount numeric(18,0) NOT NULL,
	status varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	total_amount numeric(18,0) NOT NULL,
	total_credits int NOT NULL,
	updated_at datetime2(6) NULL,
	semester_id bigint NOT NULL,
	student_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__student___3213E83F326605C1 PRIMARY KEY (id),
	CONSTRAINT UK8p10sv8gjvlmi6wqm9t294wl0 UNIQUE (student_id,semester_id),
	CONSTRAINT FK33nrmt8f0q5s5fvwtn0ym7df FOREIGN KEY (student_id) REFERENCES student_db.dbo.students(student_id),
	CONSTRAINT FK8e3gcu54kamww7cs8kepol3bx FOREIGN KEY (semester_id) REFERENCES student_db.dbo.semesters(id)
);
ALTER TABLE student_db.dbo.student_tuition WITH NOCHECK ADD CONSTRAINT CK__student_t__statu__41B8C09B CHECK (([status]='PAID' OR [status]='PARTIAL' OR [status]='UNPAID'));


-- student_db.dbo.user_roles definition

-- Drop table

-- DROP TABLE student_db.dbo.user_roles;

CREATE TABLE student_db.dbo.user_roles (
	user_id uniqueidentifier NOT NULL,
	role_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__user_rol__6EDEA153142A2D74 PRIMARY KEY (user_id,role_id),
	CONSTRAINT FKh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES student_db.dbo.roles(id),
	CONSTRAINT FKhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES student_db.dbo.users(id)
);


-- student_db.dbo.payments definition

-- Drop table

-- DROP TABLE student_db.dbo.payments;

CREATE TABLE student_db.dbo.payments (
	id bigint IDENTITY(1,1) NOT NULL,
	amount numeric(18,0) NOT NULL,
	created_at datetime2(6) NULL,
	payment_date datetime2(6) NOT NULL,
	payment_method varchar(30) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	status varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	transaction_code varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	updated_at datetime2(6) NULL,
	student_tuition_id uniqueidentifier NOT NULL,
	CONSTRAINT PK__payments__3213E83F9B6C88E9 PRIMARY KEY (id),
	CONSTRAINT FKhsg7cbi6fisb0hw87l53qn3db FOREIGN KEY (student_tuition_id) REFERENCES student_db.dbo.student_tuition(id)
);
ALTER TABLE student_db.dbo.payments WITH NOCHECK ADD CONSTRAINT CK__payments__paymen__477199F1 CHECK (([payment_method]='ONLINE_PAYMENT' OR [payment_method]='E_WALLET' OR [payment_method]='CASH' OR [payment_method]='BANK_TRANSFER'));
ALTER TABLE student_db.dbo.payments WITH NOCHECK ADD CONSTRAINT CK__payments__status__4865BE2A CHECK (([status]='CANCELLED' OR [status]='COMPLETED' OR [status]='PENDING'));