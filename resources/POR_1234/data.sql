--liquibase formatted sql
--changeset gbp:POR_1234_data runOnChange:true dbms:oracle stripComments:true failOnError:true

--MODULE_ACCESS_LEVEL
DELETE FROM MODULE_ACCESS_LEVEL WHERE SHOW = 'PMLE' AND MODULE_ID = 'BP_EU_CUSTOM_MANAGE_SFTP_KEY';