--liquibase formatted sql
--changeset gbp:CSDM_2209_data runOnChange:true dbms:oracle stripComments:true failOnError:true

--ADMIN
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_ADMIN_USERS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_ADMIN_INTEGRATE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_ADMIN_TRANSACTIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_ADMIN_ACCOUNT_STATEMENT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_ADMIN_INVOICES','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_ADMIN_VIRTUAL_TERMINAL','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_ADMIN_DIGITAL_INVOICING','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_ADMIN_CONFIGURATIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_ADMIN_OPTIMIZE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_ADMIN_MERCHANT_INFORMATION','T');

Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_US_ADMIN_INTEGRATE','T');

--DEVELOPER
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_DEVELOPER_INTEGRATE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_DEVELOPER_TRANSACTIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_ADMIN_OPTIMIZE','T');

Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_US_DEVELOPER_INTEGRATE','T');

--FINANCE
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_BUSINESS_TRANSACTIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_ADMIN_ACCOUNT_STATEMENT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_BUSINESS_VIRTUAL_TERMINAL','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_BUSINESS_DIGITAL_INVOICING','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_BUSINESS_CONFIGURATIONS','T');

--OPERATIONS
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_ADMIN_TRANSACTIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_ADMIN_ACCOUNT_STATEMENT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_ADMIN_INVOICES','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_OPERATION_DIGITAL_INVOICING','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_OPERATION_CONFIGURATIONS','T');


--CUSTOM
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_CUSTOM_TRANSACTIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_CUSTOM_PAYMENT_METHODS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_CUSTOM_API_KEYS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_CUSTOM_STORE_ID_PASSWORD','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_CUSTOM_INVOICES','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_CUSTOM_ACCOUNT_STATEMENT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_CUSTOM_IFR','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_CUSTOM_VIRTUAL_TERMINAL','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_CUSTOM_DIGITAL_INVOICING','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_CUSTOM_BRANDING','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_CUSTOM_TAXES','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_CUSTOM_COUPONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_CUSTOM_WEBHOOKS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_CUSTOM_OPTIMIZE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_CUSTOM_MERCHANT_INFORMATION','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('BUSINESS_RELATION_NAME','BP_EU_CUSTOM_APPLE_PAY','T');

