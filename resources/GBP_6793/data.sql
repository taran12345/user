--liquibase formatted sql
--changeset uspr:GBP_6793_data runOnChange:true dbms:oracle,hsqldb stripComments:false failOnError:true

UPDATE uspr_user_summary s
SET s.user_id= (SELECT u.user_id FROM uspr_users u where u.login_name = s.login_name and rownum=1),
s.application= (SELECT u.application FROM uspr_users u where u.login_name = s.login_name and rownum=1);