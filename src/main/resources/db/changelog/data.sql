-- liquibase formatted sql

-- changeset atael:1 runAlways:true
DELETE FROM USER_REPO.USERS;
TRUNCATE TABLE USER_REPO.USERS;

--rollback DELETE FROM USER_REPO.USERS;