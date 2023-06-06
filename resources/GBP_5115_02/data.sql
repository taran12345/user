--liquibase formatted sql
--changeset gbp:GBP_5115_data runOnChange:true dbms:oracle,hsqldb stripComments:true failOnError:true

INSERT INTO uspr_user_summary (login_name, user_summary) SELECT login_name, user_summary FROM uspr_users;

ALTER TABLE uspr_users DROP COLUMN user_summary;