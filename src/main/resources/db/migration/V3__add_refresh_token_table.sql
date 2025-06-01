CREATE TABLE refresh_token
(
    id         UUID NOT NULL,
    token      TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    expired_at TIMESTAMP WITHOUT TIME ZONE,
    user_id    UUID,
    CONSTRAINT pk_refreshtoken PRIMARY KEY (id)
);

ALTER TABLE refresh_token
    ADD CONSTRAINT uc_refreshtoken_user UNIQUE (user_id);

ALTER TABLE refresh_token
    ADD CONSTRAINT FK_REFRESHTOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);