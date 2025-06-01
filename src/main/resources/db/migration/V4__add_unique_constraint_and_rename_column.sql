ALTER TABLE test_cases
    ADD details TEXT;

ALTER TABLE test_cases
    ADD name VARCHAR(255);

ALTER TABLE test_cases
    ADD type VARCHAR(255);

ALTER TABLE test_cases
    ALTER COLUMN details SET NOT NULL;

ALTER TABLE test_cases
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE test_cases
    ALTER COLUMN type SET NOT NULL;

ALTER TABLE courses
    ADD CONSTRAINT uc_courses_code UNIQUE (code);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE test_cases
    DROP COLUMN test_case_details;

ALTER TABLE test_cases
    DROP COLUMN test_case_name;

ALTER TABLE test_cases
    DROP COLUMN test_case_type;