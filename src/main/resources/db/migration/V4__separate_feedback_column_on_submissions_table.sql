ALTER TABLE submissions
    ADD ai_feedback OID;

ALTER TABLE submissions
    ADD manual_feedback OID;

ALTER TABLE submissions
    DROP COLUMN feedback;

ALTER TABLE submissions
    DROP COLUMN main_class_name;

ALTER TABLE submissions
    ALTER COLUMN execution_time SET DEFAULT 0;

ALTER TABLE test_executions
    ALTER COLUMN execution_time SET DEFAULT 0;

ALTER TABLE submissions
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE submissions
    ALTER COLUMN total_points DROP NOT NULL;