--liquibase formatted sql
--changeset uspr:GBP_12218_data runOnChange:true dbms:oracle,hsqldb stripComments:true failOnError:true

Insert into USPR_WALLET_PERMS (INTERNAL_ID,PERMISSION,PERM_DESC,ENABLED,DISPLAY_ORDER) values (123,'SUPPORT','Support',1,25);