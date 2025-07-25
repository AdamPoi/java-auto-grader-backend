CREATE TABLE assignments
(
    id                    UUID                        NOT NULL,
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    title                 VARCHAR(255)                NOT NULL,
    description         OID,
    resource            OID,
    due_date              TIMESTAMP WITHOUT TIME ZONE,
    starter_code        OID,
    solution_code       OID,
    test_code           OID,
    total_points        INTEGER NOT NULL,
    course_id             UUID                        NOT NULL,
    created_by_teacher_id UUID,
    is_published        BOOLEAN,
    is_timed            BOOLEAN,
    time_limit          BIGINT,
    max_attempts        INTEGER,
    show_try_submission BOOLEAN,
    show_feedback       BOOLEAN,
    show_solution       BOOLEAN,
    allow_upload        BOOLEAN,
    CONSTRAINT pk_assignments PRIMARY KEY (id)
);

CREATE TABLE classrooms
(
    id         UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    name       VARCHAR(255)                NOT NULL,
    teacher_id UUID,
    CONSTRAINT pk_classrooms PRIMARY KEY (id)
);

CREATE TABLE course_enrollments
(
    course_id UUID NOT NULL,
    user_id   UUID NOT NULL,
    CONSTRAINT pk_course_enrollments PRIMARY KEY (course_id, user_id)
);

CREATE TABLE courses
(
    id                    UUID                        NOT NULL,
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    code                  VARCHAR(20)                 NOT NULL,
    name                  VARCHAR(255)                NOT NULL,
    description           TEXT,
    is_active             BOOLEAN,
    created_by_teacher_id UUID,
    CONSTRAINT pk_courses PRIMARY KEY (id)
);

CREATE TABLE permissions
(
    id          UUID                        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    name        VARCHAR(100)                NOT NULL,
    description TEXT,
    CONSTRAINT pk_permissions PRIMARY KEY (id)
);

CREATE TABLE refresh_token
(
    id         UUID                        NOT NULL,
    token      TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expired_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id    UUID,
    CONSTRAINT pk_refreshtoken PRIMARY KEY (id)
);

CREATE TABLE role_permissions
(
    permission_id UUID NOT NULL,
    role_id       UUID NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (permission_id, role_id)
);

CREATE TABLE roles
(
    id         UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    name       VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE rubric_grades
(
    id            UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    name       VARCHAR(255) NOT NULL,
    grade_type    VARCHAR(50)                 NOT NULL,
    rubric_id  UUID,
    assignment_id UUID                        NOT NULL,
    CONSTRAINT pk_rubric_grades PRIMARY KEY (id)
);

CREATE TABLE rubrics
(
    id            UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    name          VARCHAR(200)                NOT NULL,
    description OID,
    points      INTEGER NOT NULL,
    assignment_id UUID                        NOT NULL,
    CONSTRAINT pk_rubrics PRIMARY KEY (id)
);

CREATE TABLE submission_codes
(
    id            UUID         NOT NULL,
    file_name     VARCHAR(255) NOT NULL,
    source_code OID,
    submission_id UUID         NOT NULL,
    CONSTRAINT pk_submission_codes PRIMARY KEY (id)
);

CREATE TABLE submissions
(
    id              UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),
    status          VARCHAR(255),
    execution_time  BIGINT,
    feedback        OID,
    main_class_name VARCHAR(255),
    started_at      TIMESTAMP WITHOUT TIME ZONE,
    completed_at    TIMESTAMP WITHOUT TIME ZONE,
    total_points INTEGER NOT NULL,
    assignment_id   UUID                        NOT NULL,
    student_id      UUID,
    CONSTRAINT pk_submissions PRIMARY KEY (id)
);

CREATE TABLE test_executions
(
    id              UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    method_name     VARCHAR(255),
    status          VARCHAR(50)                 NOT NULL,
    error      OID,
    execution_time  BIGINT,
    output     OID,
    rubric_grade_id UUID                        NOT NULL,
    submission_id   UUID                        NOT NULL,
    CONSTRAINT pk_test_executions PRIMARY KEY (id)
);

CREATE TABLE user_classrooms
(
    classroom_id UUID NOT NULL,
    user_id      UUID NOT NULL,
    CONSTRAINT pk_user_classrooms PRIMARY KEY (classroom_id, user_id)
);

CREATE TABLE user_roles
(
    role_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (role_id, user_id)
);

CREATE TABLE users
(
    id         UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    email      VARCHAR(100)                NOT NULL,
    nim        VARCHAR(255),
    nip        VARCHAR(255),
    password   VARCHAR(255)                NOT NULL,
    first_name VARCHAR(50),
    last_name  VARCHAR(50),
    is_active  BOOLEAN,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE courses
    ADD CONSTRAINT uc_courses_code UNIQUE (code);

ALTER TABLE refresh_token
    ADD CONSTRAINT uc_refreshtoken_user UNIQUE (user_id);

ALTER TABLE roles
    ADD CONSTRAINT uc_roles_name UNIQUE (name);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_nim UNIQUE (nim);

ALTER TABLE users
    ADD CONSTRAINT uc_users_nip UNIQUE (nip);

ALTER TABLE assignments
    ADD CONSTRAINT FK_ASSIGNMENTS_ON_COURSE FOREIGN KEY (course_id) REFERENCES courses (id);

ALTER TABLE assignments
    ADD CONSTRAINT FK_ASSIGNMENTS_ON_CREATED_BY_TEACHER FOREIGN KEY (created_by_teacher_id) REFERENCES users (id);

ALTER TABLE classrooms
    ADD CONSTRAINT FK_CLASSROOMS_ON_TEACHER FOREIGN KEY (teacher_id) REFERENCES users (id);

ALTER TABLE courses
    ADD CONSTRAINT FK_COURSES_ON_CREATED_BY_TEACHER FOREIGN KEY (created_by_teacher_id) REFERENCES users (id);

ALTER TABLE refresh_token
    ADD CONSTRAINT FK_REFRESHTOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE rubrics
    ADD CONSTRAINT FK_RUBRICS_ON_ASSIGNMENT FOREIGN KEY (assignment_id) REFERENCES assignments (id);

ALTER TABLE rubric_grades
    ADD CONSTRAINT FK_RUBRIC_GRADES_ON_ASSIGNMENT FOREIGN KEY (assignment_id) REFERENCES assignments (id);

ALTER TABLE rubric_grades
    ADD CONSTRAINT FK_RUBRIC_GRADES_ON_RUBRIC FOREIGN KEY (rubric_id) REFERENCES rubrics (id);

ALTER TABLE submissions
    ADD CONSTRAINT FK_SUBMISSIONS_ON_ASSIGNMENT FOREIGN KEY (assignment_id) REFERENCES assignments (id);

ALTER TABLE submissions
    ADD CONSTRAINT FK_SUBMISSIONS_ON_STUDENT FOREIGN KEY (student_id) REFERENCES users (id);

ALTER TABLE submission_codes
    ADD CONSTRAINT FK_SUBMISSION_CODES_ON_SUBMISSION FOREIGN KEY (submission_id) REFERENCES submissions (id);

ALTER TABLE test_executions
    ADD CONSTRAINT FK_TEST_EXECUTIONS_ON_RUBRIC_GRADE FOREIGN KEY (rubric_grade_id) REFERENCES rubric_grades (id);

ALTER TABLE test_executions
    ADD CONSTRAINT FK_TEST_EXECUTIONS_ON_SUBMISSION FOREIGN KEY (submission_id) REFERENCES submissions (id);

ALTER TABLE course_enrollments
    ADD CONSTRAINT fk_couenr_on_course FOREIGN KEY (course_id) REFERENCES courses (id);

ALTER TABLE course_enrollments
    ADD CONSTRAINT fk_couenr_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE role_permissions
    ADD CONSTRAINT fk_rolper_on_permission FOREIGN KEY (permission_id) REFERENCES permissions (id);

ALTER TABLE role_permissions
    ADD CONSTRAINT fk_rolper_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE user_classrooms
    ADD CONSTRAINT fk_usecla_on_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms (id);

ALTER TABLE user_classrooms
    ADD CONSTRAINT fk_usecla_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES users (id);