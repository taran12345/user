--liquibase formatted sql
--changeset gbp:GBP_10745_data runOnChange:true dbms:oracle,hsqldb stripComments:true failOnError:true

-- ROLE_MODULES
Update  ROLE_MODULES
Set  DISPLAY_ORDER = 6
where ROLE = 'BP_ECOMM_ADMIN' and  MODULE_ID = 'BP_ECOMM_ADMIN_CONFIGURATIONS';

Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_ECOMM_ADMIN','BP_ADMIN_DIGITAL_INVOICING',5,'T');
