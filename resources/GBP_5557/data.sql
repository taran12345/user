--liquibase formatted sql
--changeset gbp:GBP_5557_data runOnChange:true dbms:oracle,hsqldb stripComments:true failOnError:true

update USPR_WALLET_PERMS set PERM_DESC = 'Settings' where INTERNAL_ID = 111;