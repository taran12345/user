--liquibase formatted sql
--changeset gbp:POR_1439_data runOnChange:true dbms:oracle stripComments:true failOnError:true

--MODULE_ACCESS_LEVEL
DELETE FROM MODULE_ACCESS_LEVEL WHERE SHOW = 'PMLE' AND MODULE_ID = 'BP_CUSTOM_PAYMENT_METHODS';