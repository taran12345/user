--liquibase formatted sql
--changeset gbp:GBP_11498_2_data runOnChange:true dbms:oracle stripComments:true failOnError:true

UPDATE MODULE SET IS_SELECTED = 'T' WHERE ID = 'BP_CUSTOM_BRANDING_PERMISSIONS_EDIT';
