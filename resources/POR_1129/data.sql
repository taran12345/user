--liquibase formatted sql
--changeset gbp:POR_1129_data runOnChange:true dbms:oracle,hsqldb stripComments:true failOnError:true

Update MODULE set DESCRIPTIONS = 'View Store Id and Password details' where ID = 'BP_DEVELOPER_INTEGRATE_STORE_ID_PASSWORD';
Update MODULE set DESCRIPTIONS = 'Review API documentation & ask questions from developer community' where ID = 'BP_DEVELOPER_INTEGRATE_API_DOCUMENTATION';