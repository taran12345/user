--liquibase formatted sql
--changeset gbp:CSDM_987_alter-table runOnChange:true dbms:oracle stripComments:true failOnError:true

DELETE FROM USPR_USER_TOKENS a WHERE rowid > (SELECT min(rowid) FROM uspr.USPR_USER_TOKENS b WHERE b.token=a.token AND a.login_name=b.login_name);

DELETE FROM USPR_USER_SUMMARY a WHERE a.user_id is null;

DELETE FROM USPR_USER_SUMMARY a WHERE rowid > (SELECT min(rowid) FROM uspr.USPR_USER_SUMMARY b WHERE b.user_id=a.user_id);

DELETE FROM USER_ACCESSGROUPS_MAPPING a WHERE rowid > (SELECT min(rowid) FROM uspr.USER_ACCESSGROUPS_MAPPING b WHERE b.login_name=a.login_name AND b.acc_grp_code=a.acc_grp_code);
