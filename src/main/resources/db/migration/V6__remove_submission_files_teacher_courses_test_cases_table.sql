ALTER TABLE submission_files
    DROP CONSTRAINT fk_submission_files_on_submission;

ALTER TABLE submission_test_results
    DROP CONSTRAINT fk_submission_test_results_on_submission;

ALTER TABLE submission_test_results
    DROP CONSTRAINT fk_submission_test_results_on_test_case;

ALTER TABLE submissions
    DROP CONSTRAINT fk_submissions_on_classroom;

ALTER TABLE teacher_courses
    DROP CONSTRAINT fk_teacher_courses_on_course;

ALTER TABLE teacher_courses
    DROP CONSTRAINT fk_teacher_courses_on_teacher;

ALTER TABLE test_cases
    DROP CONSTRAINT fk_test_cases_on_assignment;

DROP TABLE submission_files CASCADE;

DROP TABLE submission_test_results CASCADE;

DROP TABLE teacher_courses CASCADE;

DROP TABLE test_cases CASCADE;

ALTER TABLE submissions
    DROP COLUMN classroom_id;

ALTER TABLE submissions
    DROP COLUMN submission_base_path;

ALTER TABLE student_classrooms
    DROP COLUMN enrollment_date;

ALTER TABLE student_classrooms
    DROP COLUMN is_active;

ALTER TABLE assignments
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE classrooms
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE courses
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE permissions
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE refresh_token
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE roles
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE student_classrooms
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE submissions
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE refresh_token
    ALTER COLUMN expired_at SET NOT NULL;

ALTER TABLE student_classrooms
    ADD is_active BOOLEAN NOT NULL;

ALTER TABLE student_classrooms
    ALTER COLUMN is_active SET NOT NULL;

ALTER TABLE assignments
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE classrooms
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE courses
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE permissions
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE roles
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE student_classrooms
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE submissions
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN updated_at SET NOT NULL;