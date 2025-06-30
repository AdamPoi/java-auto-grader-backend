ALTER TABLE submissions
    ADD type VARCHAR(255);

ALTER TABLE submissions
    ALTER COLUMN type SET NOT NULL;

ALTER TABLE submissions
    ALTER COLUMN status SET NOT NULL;