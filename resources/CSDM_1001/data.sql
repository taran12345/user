--liquibase formatted sql
--changeset gbp:CSDM_1001_data runOnChange:true dbms:oracle,hsqldb stripComments:true failOnError:true

ALTER TABLE audit_user_event ADD correlation_id VARCHAR2(50);