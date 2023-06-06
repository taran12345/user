--liquibase formatted sql
--changeset gbp:GBP_11039_02_data runOnChange:true dbms:oracle,hsqldb stripComments:true failOnError:true

--MODULE_ACCESS_LEVEL
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('MLE','BP_ADMIN_CONFIGURATIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('MLE','BP_ADMIN_CONFIGURATIONS_WEBHOOKS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_CUSTOM_WEBHOOKS','T');