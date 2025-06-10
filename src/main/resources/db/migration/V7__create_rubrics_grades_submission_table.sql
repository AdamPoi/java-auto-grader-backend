CREATE TABLE grade_executions
(
    id              UUID                        NOT NULL,
    points_awarded  DECIMAL(5, 2)               NOT NULL,
    status          VARCHAR(50)                 NOT NULL,
    actual          TEXT,
    expected        TEXT,
    error           TEXT,
    execution_time  BIGINT,
    rubric_grade_id UUID                        NOT NULL,
    submission_id   UUID                        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_grade_executions PRIMARY KEY (id)
);

CREATE TABLE rubric_grades
(
    id            UUID                        NOT NULL,
    name          VARCHAR(200)                NOT NULL,
    description   VARCHAR(1000),
    points        DECIMAL(5, 2)               NOT NULL,
    display_order INTEGER                     NOT NULL,
    code          TEXT,
    arguments     JSONB,
    grade_type    VARCHAR(50)                 NOT NULL,
    rubric_id     UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_rubric_grades PRIMARY KEY (id)
);

CREATE TABLE rubrics
(
    id            UUID                        NOT NULL,
    name          VARCHAR(200)                NOT NULL,
    description   VARCHAR(1000),
    max_points    DECIMAL(10, 2)              NOT NULL,
    display_order INTEGER                     NOT NULL,
    is_active     BOOLEAN                     NOT NULL,
    assignment_id UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_rubrics PRIMARY KEY (id)
);

CREATE TABLE submission_codes
(
    id            UUID                        NOT NULL,
    file_name     VARCHAR(255)                NOT NULL,
    source_code   TEXT                        NOT NULL,
    package_path  VARCHAR(255),
    class_name    VARCHAR(255),
    submission_id UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_submission_codes PRIMARY KEY (id)
);

ALTER TABLE submissions
    ADD main_class_name VARCHAR(255);


ALTER TABLE student_classrooms
    ADD CONSTRAINT uc_student_classrooms_classroom UNIQUE (classroom_id);

ALTER TABLE student_classrooms
    ADD CONSTRAINT uc_student_classrooms_student UNIQUE (student_id);

ALTER TABLE grade_executions
    ADD CONSTRAINT FK_GRADE_EXECUTIONS_ON_RUBRIC_GRADE FOREIGN KEY (rubric_grade_id) REFERENCES rubric_grades (id);

ALTER TABLE grade_executions
    ADD CONSTRAINT FK_GRADE_EXECUTIONS_ON_SUBMISSION FOREIGN KEY (submission_id) REFERENCES submissions (id);

ALTER TABLE rubrics
    ADD CONSTRAINT FK_RUBRICS_ON_ASSIGNMENT FOREIGN KEY (assignment_id) REFERENCES assignments (id);

ALTER TABLE rubric_grades
    ADD CONSTRAINT FK_RUBRIC_GRADES_ON_RUBRIC FOREIGN KEY (rubric_id) REFERENCES rubrics (id);

ALTER TABLE submission_codes
    ADD CONSTRAINT FK_SUBMISSION_CODES_ON_SUBMISSION FOREIGN KEY (submission_id) REFERENCES submissions (id);