ALTER TABLE assignments
    DROP COLUMN time_limit;

ALTER TABLE assignments
    ADD time_limit INTEGER;