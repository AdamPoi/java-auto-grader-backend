ALTER TABLE roles
    ADD CONSTRAINT uc_roles_name UNIQUE (name);