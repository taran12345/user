--liquibase formatted sql
--changeset gbp:CSDM_1500_data runOnChange:true dbms:oracle stripComments:true failOnError:true

grant select on ACGS.ACGS_ACCESS_GROUPS to usprtxn;

Update acgs.acgs_access_groups set TYPE='DEFAULT_ADMIN' where CODE IN (select ACCESS_GROUP_ID from acgs.acgs_access_group_policies where access_POLICY_ID IN (select ACCESS_POLICY_ID from acgs.ACGS_ACCS_POL_TO_POL_RIGHTS where ACCESS_POLICY_RIGHT_ID IN (select CODE from acgs.acgs_access_policy_rights where ACCESS_ROLE IN (select CODE from acgs.ACGS_ROLE_TYPES where ROLE_NAME IN ('BP_SPARKLES_ADMIN', 'BP_BINANCE_ADMIN', 'BP_BVNK_ADMIN', 'BP_ECOMM_ADMIN', 'BP_EU_ADMIN', 'BP_EU_US_ADMIN', 'BP_FTX_ADMIN', 'BP_ISV_ADMIN', 'BP_KENNY_ADMIN', 'BP_US_ADMIN')))));

Update user_Accessgroups_mapping set ACC_GRP_TYPE=0 where ACC_GRP_CODE IN (select ACCESS_GROUP_ID from acgs.acgs_access_group_policies where access_POLICY_ID IN (select ACCESS_POLICY_ID from acgs.ACGS_ACCS_POL_TO_POL_RIGHTS where ACCESS_POLICY_RIGHT_ID IN (select CODE from acgs.acgs_access_policy_rights where ACCESS_ROLE IN (select CODE from acgs.ACGS_ROLE_TYPES where ROLE_NAME IN ('BP_SPARKLES_ADMIN', 'BP_BINANCE_ADMIN', 'BP_BVNK_ADMIN', 'BP_ECOMM_ADMIN', 'BP_EU_ADMIN', 'BP_EU_US_ADMIN', 'BP_FTX_ADMIN', 'BP_ISV_ADMIN', 'BP_KENNY_ADMIN', 'BP_US_ADMIN')))));

COMMIT;

