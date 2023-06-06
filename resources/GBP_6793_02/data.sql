--liquibase formatted sql
--changeset uspr:GBP_6793_02_data runOnChange:true dbms:oracle,hsqldb stripComments:false failOnError:true

UPDATE uspr_user_tokens t
SET t.user_id= (SELECT u.user_id FROM uspr_users u where u.login_name = t.login_name and rownum=1),
t.application= (SELECT u.application FROM uspr_users u where u.login_name = t.login_name and rownum=1);