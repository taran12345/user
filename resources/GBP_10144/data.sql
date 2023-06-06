--liquibase formatted sql
--changeset gbp:GBP_10144_data runOnChange:true dbms:oracle,hsqldb stripComments:true failOnError:true

--MODULE
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_US_ADMIN_INTEGRATE','Integrate','T','Review API Documentation, integrate payment methods and API Keys',null,null,'T','T',0,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_US_ADMIN_INTEGRATE_API_KEYS','API keys','F','Access API keys','BP_US_ADMIN_INTEGRATE',null,'F','F',0,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_US_ADMIN_INTEGRATE_PAYMENT_METHODS','Add Payment Method','F','User can see the list of payment methods','BP_US_ADMIN_INTEGRATE',null,'F','F',1,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_US_ADMIN_INTEGRATE_API_DOCUMENTATION','API Documentation','F','Review API documentation & ask questions from developer community','BP_US_ADMIN_INTEGRATE',null,'F','F',2,'T','F');


Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_BUSINESS_TRANSACTIONS','Transactions','T','View and manage all information pertaining to transactions along with call to actions',null,null,'T','T',0,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_BUSINESS_TRANSACTIONS_ACCOUNT_LEVEL_AGGREGATIONS_FILTER_L2','Account level Aggregations and filter (L2)','F','View count and volume of transactions across merchant’s','BP_BUSINESS_TRANSACTIONS',null,'F','F',0,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_BUSINESS_TRANSACTIONS_MERCHANT_LEVEL_AGGREGATIONS_FILTER_L1','Merchant level Aggregations and filter (L1)','F','View count and volume of transactions aggregated across merchants','BP_BUSINESS_TRANSACTIONS',null,'F','F',1,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_BUSINESS_TRANSACTIONS_DRILL_DOWN_MLE_LEVEL_L3','Drill down at MLE level (L3)','F','View and Manage transactions at Merchant Level','BP_BUSINESS_TRANSACTIONS',null,'F','F',2,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_BUSINESS_TRANSACTIONS_VIEW_TRANSACTION_RECORDS','View transaction records','F','View transaction records history','BP_BUSINESS_TRANSACTIONS',null,'F','F',3,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_BUSINESS_TRANSACTIONS_DOWNLOAD_TRANSACTION_RECORDS','Download transaction records','F','Download transaction records history in CSV format','BP_BUSINESS_TRANSACTIONS',null,'F','F',4,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_BUSINESS_TRANSACTIONS_VIEW_DETAILED_TRANSACTION_HISTORY','View Detailed Transaction History','F','View transaction history details page with order life cycle as of that point in time','BP_BUSINESS_TRANSACTIONS',null,'F','F',5,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_BUSINESS_TRANSACTIONS_CHARGEBACK_REPORTS_ACCOUNT','Chargeback Reports (Account)','F','View and Manage Chargeback Report with Accounts’ filter','BP_BUSINESS_TRANSACTIONS',null,'F','F',6,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_BUSINESS_TRANSACTIONS_CHARGEBACK_REPORTS_MERCHANT','Chargeback Reports (Merchant)','F','View and Manage Chargeback Report with Merchants’ filter','BP_BUSINESS_TRANSACTIONS',null,'F','F',7,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_BUSINESS_TRANSACTIONS_CHARGEBACK_REPORTS_DOWNLOAD','Chargeback Report download','F','Download Chargeback Reports','BP_BUSINESS_TRANSACTIONS',null,'F','F',8,'T','F');

Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_OPERATION_CONFIGURATIONS','Configurations','T',null,null,null,'T','T',0,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_OPERATION_CONFIGURATIONS_3DS','3D Secure configuration','T','View the configured 3Ds 1.0 rules already set','BP_OPERATION_CONFIGURATIONS',null,'F','F',0,'T','F');

Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_3D_SECURE','3D Secure configuration','F','Provision to skip 3DS check & set rules based on order value',null,'MULTI','T','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_3D_SECURE_PERMISSIONS','Permissions','F',null,'BP_CUSTOM_3D_SECURE','SINGLE','F','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_3D_SECURE_PERMISSIONS_VIEW','View 3DS config and rules','T','View the configured 3Ds 1.0 rules already set','BP_CUSTOM_3D_SECURE_PERMISSIONS','MULTI','F','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_SALVAGE','Salvage Configuration','F','Improve transaction success rates by configuring salvage options',null,'MULTI','T','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_SALVAGE_PERMISSIONS','Permissions','F',null,'BP_CUSTOM_SALVAGE','SINGLE','F','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_SALVAGE_PERMISSIONS_VIEW','View Salvage configuration','T','View the set configuration for card salvage','BP_CUSTOM_SALVAGE_PERMISSIONS','MULTI','F','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_SALVAGE_PERMISSIONS_EDIT','Edit Salvage configuration','F','Edit the configuration set for card salvage','BP_CUSTOM_SALVAGE_PERMISSIONS','MULTI','F','F',1,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_BRANDING','Branding Configuration','F','Configure your portal experience',null,'MULTI','T','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_BRANDING_PERMISSIONS','Permissions','F',null,'BP_CUSTOM_BRANDING','SINGLE','F','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_BRANDING_PERMISSIONS_VIEW','View Branding configuration','T','User can only view the set configurations for hosted checkout screens','BP_CUSTOM_BRANDING_PERMISSIONS','MULTI','F','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_BRANDING_PERMISSIONS_EDIT','Edit Branding configuration','F','User to edit branding parameters and 100% whitelabel hosted  checkout screens','BP_CUSTOM_BRANDING_PERMISSIONS','MULTI','F','F',1,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_ADMIN_OPTIMIZE','Optimize','T','View all the key metrics to track Payment API performance',null,null,'F','F',0,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_ADMIN_CONFIGURATIONS','Configurations','T',null,null,null,'T','T',0,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_ADMIN_CONFIGURATIONS_3DS','3D Secure configuration','T','View the configured 3Ds 1.0 rules already set','BP_ADMIN_CONFIGURATIONS',null,'F','F',0,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_ADMIN_CONFIGURATIONS_SALVAGE','Salvage Configuration','T','View and edit the set configuration for card salvage','BP_ADMIN_CONFIGURATIONS',null,'F','F',1,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_ADMIN_CONFIGURATIONS_BRANDING','Branding Configuration','T','User can view and edit the set configurations for hosted checkout screens','BP_ADMIN_CONFIGURATIONS',null,'F','F',2,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_ADMIN_CONFIGURATIONS_WEBHOOKS','Webhooks Configuration','T','Manage your webhooks for the events','BP_ADMIN_CONFIGURATIONS',null,'F','F',3,'T','F');

Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_OPTIMIZE','Optimize','F','View all the key metrics to track Payment API performance',null,'MULTI','F','F',0,'T','T');

Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_PAYMENT_METHODS','Add Payment Method','F','User can see the list of payment methods',null,'MULTI','F','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_PAYMENT_METHODS_ACTIONS','Actions','F',null,'BP_CUSTOM_PAYMENT_METHODS','SINGLE','F','F',0,'T','F');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_PAYMENT_METHODS_ACTIONS_VIEW','View list of payment methods','T','User can see the list of payment methods with enabled and not enabled flag','BP_CUSTOM_PAYMENT_METHODS_ACTIONS',null,'F','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_PAYMENT_METHODS_ACTIONS_EDIT','View and Add payment method','F','User can see the list of payment methods enables and also raise request to add new payment method','BP_CUSTOM_PAYMENT_METHODS_ACTIONS',null,'F','F',1,'T','T');

Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_WEBHOOKS','Webhooks Configuration','F','Manage your webhooks for the events',null,'MULTI','T','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_WEBHOOKS_PERMISSIONS','Permissions','F',null,'BP_CUSTOM_WEBHOOKS','SINGLE','F','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_WEBHOOKS_PERMISSIONS_VIEW','View list of Webhooks Configuration','T','User can see the list of Webhooks Configuration','BP_CUSTOM_WEBHOOKS_PERMISSIONS','SINGLE','F','F',0,'T','T');
Insert into MODULE (ID,LABEL,IS_SELECTED,DESCRIPTIONS,PARENT_ID,SELECTION_MODE,SHOW_EXPAND,IS_EXPAND,DISPLAY_ORDER,ENABLED,EDITABLE)
values ('BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','View and Edit Webhooks Configuration','F','User can see the list of Webhooks Configuration and add new Webhooks Configuration','BP_CUSTOM_WEBHOOKS_PERMISSIONS','SINGLE','F','F',1,'T','T');

--MODULE_ACCESS_LEVEL
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_ADMIN_OPTIMIZE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('MLE','BP_ADMIN_OPTIMIZE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_ADMIN_OPTIMIZE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_ADMIN_CONFIGURATIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_ADMIN_CONFIGURATIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_ADMIN_CONFIGURATIONS_3DS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_ADMIN_CONFIGURATIONS_3DS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_ADMIN_CONFIGURATIONS_SALVAGE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_ADMIN_CONFIGURATIONS_SALVAGE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_ADMIN_CONFIGURATIONS_BRANDING','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_ADMIN_CONFIGURATIONS_BRANDING','T');

Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_ADMIN_CONFIGURATIONS_WEBHOOKS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_ADMIN_CONFIGURATIONS_WEBHOOKS','T');

Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_BUSINESS_TRANSACTIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('MLE','BP_BUSINESS_TRANSACTIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('ISV','BP_BUSINESS_TRANSACTIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_BUSINESS_TRANSACTIONS_ACCOUNT_LEVEL_AGGREGATIONS_FILTER_L2','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('MLE','BP_BUSINESS_TRANSACTIONS_ACCOUNT_LEVEL_AGGREGATIONS_FILTER_L2','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('ISV','BP_BUSINESS_TRANSACTIONS_MERCHANT_LEVEL_AGGREGATIONS_FILTER_L1','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_BUSINESS_TRANSACTIONS_DRILL_DOWN_MLE_LEVEL_L3','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('MLE','BP_BUSINESS_TRANSACTIONS_DRILL_DOWN_MLE_LEVEL_L3','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_BUSINESS_TRANSACTIONS_VIEW_TRANSACTION_RECORDS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('MLE','BP_BUSINESS_TRANSACTIONS_VIEW_TRANSACTION_RECORDS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('ISV','BP_BUSINESS_TRANSACTIONS_VIEW_TRANSACTION_RECORDS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_BUSINESS_TRANSACTIONS_DOWNLOAD_TRANSACTION_RECORDS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('MLE','BP_BUSINESS_TRANSACTIONS_DOWNLOAD_TRANSACTION_RECORDS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('ISV','BP_BUSINESS_TRANSACTIONS_DOWNLOAD_TRANSACTION_RECORDS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_BUSINESS_TRANSACTIONS_VIEW_DETAILED_TRANSACTION_HISTORY','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('MLE','BP_BUSINESS_TRANSACTIONS_VIEW_DETAILED_TRANSACTION_HISTORY','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('ISV','BP_BUSINESS_TRANSACTIONS_VIEW_DETAILED_TRANSACTION_HISTORY','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_BUSINESS_TRANSACTIONS_CHARGEBACK_REPORTS_ACCOUNT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('MLE','BP_BUSINESS_TRANSACTIONS_CHARGEBACK_REPORTS_ACCOUNT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('ISV','BP_BUSINESS_TRANSACTIONS_CHARGEBACK_REPORTS_MERCHANT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_BUSINESS_TRANSACTIONS_CHARGEBACK_REPORTS_DOWNLOAD','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('MLE','BP_BUSINESS_TRANSACTIONS_CHARGEBACK_REPORTS_DOWNLOAD','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('ISV','BP_BUSINESS_TRANSACTIONS_CHARGEBACK_REPORTS_DOWNLOAD','T');

Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_OPERATION_CONFIGURATIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_OPERATION_CONFIGURATIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_OPERATION_CONFIGURATIONS_3DS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_OPERATION_CONFIGURATIONS_3DS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_OPTIMIZE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('MLE','BP_CUSTOM_OPTIMIZE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_OPTIMIZE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_3D_SECURE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_3D_SECURE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_3D_SECURE_PERMISSIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_3D_SECURE_PERMISSIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_3D_SECURE_PERMISSIONS_VIEW','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_3D_SECURE_PERMISSIONS_VIEW','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_SALVAGE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_SALVAGE','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_SALVAGE_PERMISSIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_SALVAGE_PERMISSIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_SALVAGE_PERMISSIONS_VIEW','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_SALVAGE_PERMISSIONS_VIEW','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_SALVAGE_PERMISSIONS_EDIT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_SALVAGE_PERMISSIONS_EDIT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_BRANDING','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_BRANDING','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_BRANDING_PERMISSIONS_VIEW','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_BRANDING_PERMISSIONS_VIEW','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_BRANDING_PERMISSIONS_EDIT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_BRANDING_PERMISSIONS_EDIT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_BRANDING_PERMISSIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_BRANDING_PERMISSIONS','T');

Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_CUSTOM_PAYMENT_METHODS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('ISV','BP_CUSTOM_PAYMENT_METHODS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_CUSTOM_PAYMENT_METHODS_ACTIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('ISV','BP_CUSTOM_PAYMENT_METHODS_ACTIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_CUSTOM_PAYMENT_METHODS_ACTIONS_VIEW','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('ISV','BP_CUSTOM_PAYMENT_METHODS_ACTIONS_VIEW','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('PMLE','BP_CUSTOM_PAYMENT_METHODS_ACTIONS_EDIT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED) values ('ISV','BP_CUSTOM_PAYMENT_METHODS_ACTIONS_EDIT','T');

Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_WEBHOOKS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('MLE','BP_CUSTOM_WEBHOOKS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_WEBHOOKS_PERMISSIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_WEBHOOKS_PERMISSIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('MLE','BP_CUSTOM_WEBHOOKS_PERMISSIONS','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_WEBHOOKS_PERMISSIONS_VIEW','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_WEBHOOKS_PERMISSIONS_VIEW','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('MLE','BP_CUSTOM_WEBHOOKS_PERMISSIONS_VIEW','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('PMLE','BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('ISV','BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','T');
Insert into MODULE_ACCESS_LEVEL (SHOW,MODULE_ID,ENABLED)
values ('MLE','BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','T');

--MODULE_PERM
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:performance-metrics:get','BP_CUSTOM_OPTIMIZE','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:rule:get','BP_CUSTOM_3D_SECURE_PERMISSIONS_VIEW','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:processing-account:get','BP_CUSTOM_3D_SECURE_PERMISSIONS_VIEW','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:account-groups:get','BP_CUSTOM_SALVAGE_PERMISSIONS_VIEW','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:account-groups:get','BP_CUSTOM_SALVAGE_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:merchant-config:create','BP_CUSTOM_SALVAGE_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:merchant-config:update','BP_CUSTOM_SALVAGE_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:white-labeling:get','BP_CUSTOM_BRANDING_PERMISSIONS_VIEW','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:white-labeling:get','BP_CUSTOM_BRANDING_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:white-labeling-portal:create','BP_CUSTOM_BRANDING_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:white-labeling-portal:update','BP_CUSTOM_BRANDING_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:white-labeling-checkout:create','BP_CUSTOM_BRANDING_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:white-labeling-checkout:update','BP_CUSTOM_BRANDING_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:user-payment-methods:get','BP_CUSTOM_PAYMENT_METHODS_ACTIONS_VIEW','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:user-payment-methods:get','BP_CUSTOM_PAYMENT_METHODS_ACTIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:user-payment-methods:create','BP_CUSTOM_PAYMENT_METHODS_ACTIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:webhook-events:get','BP_CUSTOM_WEBHOOKS_PERMISSIONS_VIEW','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:webhook-subscription:get','BP_CUSTOM_WEBHOOKS_PERMISSIONS_VIEW','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:webhook-subscription-config:get','BP_CUSTOM_WEBHOOKS_PERMISSIONS_VIEW','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:webhook-events:get','BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:webhook-subscription:get','BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:webhook-subscription-config:get','BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:webhook-subscription:create','BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:webhook-subscription:update','BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:url-connection:get','BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:webhook-subscription:delete','BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','T');
Insert into MODULE_PERM (PERMISSION,MODULE_ID,ENABLED)
values ('group-business-portal:webhook-subscription:test','BP_CUSTOM_WEBHOOKS_PERMISSIONS_EDIT','T');

--ROLE_MODULES
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_ADMIN','BP_ADMIN_USERS',0,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_ADMIN','BP_US_ADMIN_INTEGRATE',1,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_ADMIN','BP_ADMIN_TRANSACTIONS',2,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_ADMIN','BP_ADMIN_CONFIGURATIONS',3,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_ADMIN','BP_ADMIN_OPTIMIZE',4,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_DEVELOPER','BP_US_ADMIN_INTEGRATE',0,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_DEVELOPER','BP_DEVELOPER_TRANSACTIONS',1,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_DEVELOPER','BP_ADMIN_OPTIMIZE',2,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_BUSINESS','BP_BUSINESS_TRANSACTIONS',0,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_OPERATION','BP_ADMIN_TRANSACTIONS',0,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_OPERATION','BP_OPERATION_CONFIGURATIONS',1,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_CUSTOM','BP_CUSTOM_TRANSACTIONS',0,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_CUSTOM','BP_CUSTOM_API_KEYS',1,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_CUSTOM','BP_CUSTOM_PAYMENT_METHODS',2,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_CUSTOM','BP_CUSTOM_3D_SECURE',3,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_CUSTOM','BP_CUSTOM_SALVAGE',4,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_CUSTOM','BP_CUSTOM_BRANDING',5,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_CUSTOM','BP_CUSTOM_WEBHOOKS',6,'T');
Insert into ROLE_MODULES (ROLE,MODULE_ID,DISPLAY_ORDER,ENABLED)
values ('BP_US_CUSTOM','BP_CUSTOM_OPTIMIZE',7,'T');

Update  ROLE_MODULES
Set  MODULE_ID = 'BP_BUSINESS_TRANSACTIONS'
where ROLE = 'BP_EU_BUSINESS' and  MODULE_ID = 'BP_ADMIN_TRANSACTIONS';

Update  ROLE_MODULES
Set  DISPLAY_ORDER = 1
where ROLE = 'BP_EU_DEVELOPER' and  MODULE_ID = 'BP_DEVELOPER_TRANSACTIONS';
