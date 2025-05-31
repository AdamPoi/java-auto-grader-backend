CREATE TABLE assignments
(
    id                      UUID                        NOT NULL,
    title                   VARCHAR(255)                NOT NULL,
    description             TEXT,
    due_date                TIMESTAMP WITHOUT TIME ZONE,
    created_at              TIMESTAMP WITHOUT TIME ZONE,
    updated_at              TIMESTAMP WITHOUT TIME ZONE,
    is_published            BOOLEAN,
    starter_code_base_path  VARCHAR(512),
    solution_code_base_path VARCHAR(512),
    max_attempts            INTEGER,
    course_id               UUID                        NOT NULL,
    created_by_teacher_id   UUID,
    date_created            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_assignments PRIMARY KEY (id)
);

CREATE TABLE classrooms
(
    id                    UUID                        NOT NULL,
    classroom_name        VARCHAR(255)                NOT NULL,
    is_active             BOOLEAN,
    enrollment_start_date TIMESTAMP WITHOUT TIME ZONE,
    enrollment_end_date   TIMESTAMP WITHOUT TIME ZONE,
    created_at            TIMESTAMP WITHOUT TIME ZONE,
    updated_at            TIMESTAMP WITHOUT TIME ZONE,
    course_id             UUID                        NOT NULL,
    teacher_id            UUID,
    date_created          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_classrooms PRIMARY KEY (id)
);

CREATE TABLE courses
(
    id                    UUID                        NOT NULL,
    course_code           VARCHAR(20)                 NOT NULL,
    course_name           VARCHAR(255)                NOT NULL,
    description           TEXT,
    is_active             BOOLEAN,
    enrollment_start_date TIMESTAMP WITHOUT TIME ZONE,
    enrollment_end_date   TIMESTAMP WITHOUT TIME ZONE,
    created_at            TIMESTAMP WITHOUT TIME ZONE,
    updated_at            TIMESTAMP WITHOUT TIME ZONE,
    created_by_teacher_id UUID,
    date_created          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_courses PRIMARY KEY (id)
);

CREATE TABLE permissions
(
    id              UUID                        NOT NULL,
    permission_name VARCHAR(100)                NOT NULL,
    description     TEXT,
    date_created    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_permissions PRIMARY KEY (id)
);

CREATE TABLE role_permissions
(
    permission_id UUID NOT NULL,
    role_id       UUID NOT NULL,
    CONSTRAINT pk_role_permissions PRIMARY KEY (permission_id, role_id)
);

CREATE TABLE roles
(
    id           UUID                        NOT NULL,
    role_name    VARCHAR(255)                NOT NULL,
    date_created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE student_classrooms
(
    id              UUID                        NOT NULL,
    enrollment_date TIMESTAMP WITHOUT TIME ZONE,
    is_active       BOOLEAN,
    student_id      UUID,
    classroom_id    UUID,
    date_created    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_student_classrooms PRIMARY KEY (id)
);

CREATE TABLE submission_files
(
    id                   UUID                        NOT NULL,
    file_path_in_project VARCHAR(512)                NOT NULL,
    submission_id        UUID                        NOT NULL,
    date_created         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_submission_files PRIMARY KEY (id)
);

CREATE TABLE submission_test_results
(
    id             UUID                        NOT NULL,
    passed         BOOLEAN                     NOT NULL,
    score          INTEGER,
    actual_output  TEXT,
    error_message  TEXT,
    execution_time INTEGER,
    submission_id  UUID                        NOT NULL,
    test_case_id   UUID                        NOT NULL,
    date_created   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_submission_test_results PRIMARY KEY (id)
);

CREATE TABLE submissions
(
    id                   UUID                        NOT NULL,
    submission_time      TIMESTAMP WITHOUT TIME ZONE,
    attempt_number       INTEGER                     NOT NULL,
    status               VARCHAR(255),
    grader_feedback      TEXT,
    grading_started_at   TIMESTAMP WITHOUT TIME ZONE,
    grading_completed_at TIMESTAMP WITHOUT TIME ZONE,
    submission_base_path VARCHAR(512)                NOT NULL,
    assignment_id        UUID                        NOT NULL,
    student_id           UUID                        NOT NULL,
    classroom_id         UUID                        NOT NULL,
    date_created         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_submissions PRIMARY KEY (id)
);

CREATE TABLE teacher_courses
(
    id                    UUID                        NOT NULL,
    assigned_at           TIMESTAMP WITHOUT TIME ZONE,
    is_primary_instructor BOOLEAN,
    teacher_id            UUID,
    course_id             UUID,
    date_created          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_teacher_courses PRIMARY KEY (id)
);

CREATE TABLE test_cases
(
    id                UUID                        NOT NULL,
    test_case_name    VARCHAR(255)                NOT NULL,
    description       TEXT,
    test_case_type    VARCHAR(255)                NOT NULL,
    test_case_details TEXT                        NOT NULL,
    score             INTEGER                     NOT NULL,
    execution_order   INTEGER,
    assignment_id     UUID                        NOT NULL,
    date_created      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_test_cases PRIMARY KEY (id)
);

CREATE TABLE user_roles
(
    role_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (role_id, user_id)
);

CREATE TABLE users
(
    id           UUID                        NOT NULL,
    email        VARCHAR(100)                NOT NULL,
    password     VARCHAR(255)                NOT NULL,
    first_name   VARCHAR(50),
    last_name    VARCHAR(50),
    is_active    BOOLEAN,
    created_at   TIMESTAMP WITHOUT TIME ZONE,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    date_created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE assignments
    ADD CONSTRAINT FK_ASSIGNMENTS_ON_COURSE FOREIGN KEY (course_id) REFERENCES courses (id);

ALTER TABLE assignments
    ADD CONSTRAINT FK_ASSIGNMENTS_ON_CREATED_BY_TEACHER FOREIGN KEY (created_by_teacher_id) REFERENCES users (id);

ALTER TABLE classrooms
    ADD CONSTRAINT FK_CLASSROOMS_ON_COURSE FOREIGN KEY (course_id) REFERENCES courses (id);

ALTER TABLE classrooms
    ADD CONSTRAINT FK_CLASSROOMS_ON_TEACHER FOREIGN KEY (teacher_id) REFERENCES users (id);

ALTER TABLE courses
    ADD CONSTRAINT FK_COURSES_ON_CREATED_BY_TEACHER FOREIGN KEY (created_by_teacher_id) REFERENCES users (id);

ALTER TABLE student_classrooms
    ADD CONSTRAINT FK_STUDENT_CLASSROOMS_ON_CLASSROOM FOREIGN KEY (classroom_id) REFERENCES classrooms (id);

ALTER TABLE student_classrooms
    ADD CONSTRAINT FK_STUDENT_CLASSROOMS_ON_STUDENT FOREIGN KEY (student_id) REFERENCES users (id);

ALTER TABLE submissions
    ADD CONSTRAINT FK_SUBMISSIONS_ON_ASSIGNMENT FOREIGN KEY (assignment_id) REFERENCES assignments (id);

ALTER TABLE submissions
    ADD CONSTRAINT FK_SUBMISSIONS_ON_CLASSROOM FOREIGN KEY (classroom_id) REFERENCES classrooms (id);

ALTER TABLE submissions
    ADD CONSTRAINT FK_SUBMISSIONS_ON_STUDENT FOREIGN KEY (student_id) REFERENCES users (id);

ALTER TABLE submission_files
    ADD CONSTRAINT FK_SUBMISSION_FILES_ON_SUBMISSION FOREIGN KEY (submission_id) REFERENCES submissions (id);

ALTER TABLE submission_test_results
    ADD CONSTRAINT FK_SUBMISSION_TEST_RESULTS_ON_SUBMISSION FOREIGN KEY (submission_id) REFERENCES submissions (id);

ALTER TABLE submission_test_results
    ADD CONSTRAINT FK_SUBMISSION_TEST_RESULTS_ON_TEST_CASE FOREIGN KEY (test_case_id) REFERENCES test_cases (id);

ALTER TABLE teacher_courses
    ADD CONSTRAINT FK_TEACHER_COURSES_ON_COURSE FOREIGN KEY (course_id) REFERENCES courses (id);

ALTER TABLE teacher_courses
    ADD CONSTRAINT FK_TEACHER_COURSES_ON_TEACHER FOREIGN KEY (teacher_id) REFERENCES users (id);

ALTER TABLE test_cases
    ADD CONSTRAINT FK_TEST_CASES_ON_ASSIGNMENT FOREIGN KEY (assignment_id) REFERENCES assignments (id);

ALTER TABLE role_permissions
    ADD CONSTRAINT fk_rolper_on_permission FOREIGN KEY (permission_id) REFERENCES permissions (id);

ALTER TABLE role_permissions
    ADD CONSTRAINT fk_rolper_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES users (id);