--liquibase formatted sql
--changeset uspr:GBP_5115_03_data runOnChange:true dbms:oracle stripComments:true failOnError:true

grant select on ACGS.ACGS_ROLE_TYPES to usprtxn;