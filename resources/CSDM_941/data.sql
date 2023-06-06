--liquibase formatted sql
--changeset gbp:GBP_941_data runOnChange:true dbms:oracle,hsqldb stripComments:true failOnError:true

DELETE FROM MODULE_PERM WHERE PERMISSION='group-business-portal:account-statement:ifr-txns';
