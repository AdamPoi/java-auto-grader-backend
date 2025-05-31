ALTER TABLE courses
    ADD code VARCHAR(20);

ALTER TABLE courses
    ADD name VARCHAR(255);

ALTER TABLE courses
    ALTER COLUMN code SET NOT NULL;

ALTER TABLE classrooms
    ADD name VARCHAR(255);

ALTER TABLE classrooms
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE courses
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE permissions
    ADD name VARCHAR(100);

ALTER TABLE permissions
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE roles
    ADD name VARCHAR(255);

ALTER TABLE roles
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE classrooms
    DROP COLUMN classroom_name;

ALTER TABLE courses
    DROP COLUMN course_code;

ALTER TABLE courses
    DROP COLUMN course_name;

ALTER TABLE permissions
    DROP COLUMN permission_name;

ALTER TABLE roles
    DROP COLUMN role_name;