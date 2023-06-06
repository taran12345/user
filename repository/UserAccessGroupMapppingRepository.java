// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository;

import com.paysafe.upf.user.provisioning.domain.BulkUsers;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingKey;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface UserAccessGroupMapppingRepository
    extends CrudRepository<UserAccessGroupMappingDao, UserAccessGroupMappingKey> {

  UserAccessGroupMappingDao findByLoginNameAndAccessGroupCode(String loginName, String accessGroupCode);

  @Modifying
  @Query("delete from UserAccessGroupMappingDao n where n.userId = :userId")
  void deleteByUserId(@Param("userId") String userId);

  @Query(value = "select * from  user_accessgroups_mapping where lower(LOGIN_NAME) =lower(?1) "
      + "and acc_grp_res_type = ?2 and acc_grp_res_id = ?3", nativeQuery = true)
  List<UserAccessGroupMappingDao> findByLoginNameAndResourceTypeAndResourceId(String loginName, String resourceType,
      String resourceId);

  @Query(value = "select * from user_accessgroups_mapping where acc_grp_res_type = ?1 "
      + " and acc_grp_res_id = ?2 order by acc_grp_type asc, last_modified_datetime desc", nativeQuery = true)
  Page<UserAccessGroupMappingDao> findByResourceTypeAndResourceIdOrderByCreatedDateDesc(String resourceType,
      String resourceId, Pageable pageable);

  long countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(String resourceType,
      String resourceId, AccessGroupType accessGroupType, AccessResourceStatus status);

  @Query(
      value = "select * from user_accessgroups_mapping where acc_grp_res_type = ?1 and "
          + "acc_grp_res_id = ?2 and (lower(login_name) like lower(concat('%', concat(?3, '%'))) "
          + "or lower(user_first_name) like lower(concat('%', concat(?3, '%'))) or lower(user_last_name) "
          + "like lower(concat('%', concat(?3, '%'))))" + " order by acc_grp_type asc, last_modified_datetime desc",
      nativeQuery = true)
  Page<UserAccessGroupMappingDao> smartSearchUsers(String resourceType, String resourceId, String queryString,
      String loggedInUserName, Pageable pageable);

  @Query(value = "select * from  user_accessgroups_mapping where "
      + " user_id in (select user_id from uspr_users usr where lower(usr.LOGIN_NAME) =lower(?1) "
      + "and usr.APPLICATION = ?3 FETCH FIRST 1 ROW ONLY) " + " and acc_grp_res_type = ?2", nativeQuery = true)
  List<UserAccessGroupMappingDao> findByLoginNameAndResourceType(String loginName, String resourceType,
      String application);

  @Query(value = "select * from  user_accessgroups_mapping where "
      + " user_id in (select user_id from uspr_users usr where lower(usr.LOGIN_NAME) =lower(?1) "
      + "and usr.APPLICATION = ?2  FETCH FIRST 1 ROW ONLY) ", nativeQuery = true)
  List<UserAccessGroupMappingDao> findByLoginName(String loginName, String application);

  @Query(value = "select login_name from  user_accessgroups_mapping where acc_grp_code in (?1)", nativeQuery = true)
  List<String> getLoginName(List<String> accessGroupIds);

  @Modifying
  @Query("delete from UserAccessGroupMappingDao ag where ag.userId = ?1 and ag.resourceId = ?2 and "
      + "ag.resourceType =?3")
  void delete(String userId, String resourceId, String resourceType);

  @Query("select ag from  UserAccessGroupMappingDao ag where "
      + "ag.userId in (select usr.userId from User usr where lower(usr.loginName) =lower(?1) "
      + "and  usr.application = ?3) and ag.userAccessGroupStatus = ?2")
  List<UserAccessGroupMappingDao> findByLoginNameAndUserAccessGroupStatus(String loginName,
      AccessResourceStatus accessResourceStatus, String application);

  @Query("select ag from  UserAccessGroupMappingDao ag where "
      + "ag.userId in (select usr.userId from User usr where lower(usr.loginName) =lower(?1) "
      + "and  usr.application = ?3) and ag.userAccessGroupStatus = ?2 and ag.accessGroupType = ?4")
  List<UserAccessGroupMappingDao> findByLoginNameAndUserAccessGroupStatusAndAccessGroupType(String loginName,
      AccessResourceStatus accessResourceStatus, String application, AccessGroupType role);

  @Query("select ag.accessGroupCode from  UserAccessGroupMappingDao ag where "
      + " ag.userId in (select usr.userId from User usr where usr.loginName = ?1 and  usr.application = ?3) "
      + " and ag.userAccessGroupStatus = ?2")
  List<String> getUserAccessGroupIds(String userName, AccessResourceStatus status, String application);

  @Query("SELECT new com.paysafe.upf.user.provisioning.domain.BulkUsers(count(ag.loginName), ag.resourceId) from"
      + " UserAccessGroupMappingDao ag where ag.resourceId in (select ag.resourceId from "
      + " UserAccessGroupMappingDao ag where "
      + " ag.userId in (select usr.userId from User usr where lower(usr.loginName) =lower(?1) and"
      + " usr.application = ?4) and ag.userAccessGroupStatus = ?2) and"
      + " ag.userAccessGroupStatus = ?2 and ag.resourceType =?3 group by ag.resourceId")
  List<BulkUsers> getBulkUsersTotal(String loginName, AccessResourceStatus status, String resourceType,
      String application);

  @Query("SELECT new com.paysafe.upf.user.provisioning.domain.BulkUsers(count(ag.loginName), ag.resourceId) from"
      + " UserAccessGroupMappingDao ag where ag.resourceId in(select ag.resourceId from "
      + " UserAccessGroupMappingDao ag where ag.userId in (select usr.userId from User usr "
      + " where lower(usr.loginName) =lower(?1) and  usr.application = ?5) " + " and ag.userAccessGroupStatus = ?2) and"
      + " ag.userAccessGroupStatus = ?2 and ag.accessGroupType = ?3 and ag.resourceType =?4"
      + "  group by ag.resourceId")
  List<BulkUsers> getBulkUsersAdmin(String loginName, AccessResourceStatus status, AccessGroupType userType,
      String resourceType, String application);

  @Query("SELECT new com.paysafe.upf.user.provisioning.domain.BulkUsers(count(ag.loginName), ag.resourceId) from"
      + " UserAccessGroupMappingDao ag where ag.resourceId in (?1)"
      + " and ag.userAccessGroupStatus = ?2 and ag.accessGroupType = ?3 and ag.resourceType =?4"
      + " group by ag.resourceId")
  List<BulkUsers> getUserCountFilterBy(List<String> resourceIds, AccessResourceStatus status, AccessGroupType userType,
      String resourceType);

  @Query("SELECT new com.paysafe.upf.user.provisioning.domain.BulkUsers(count(ag.loginName), ag.resourceId) from"
      + " UserAccessGroupMappingDao ag where ag.resourceId in (?1)"
      + " and ag.userAccessGroupStatus = ?2 and ag.resourceType =?3" + " group by ag.resourceId")
  List<BulkUsers> getUserCountFilterBy(List<String> resourceIds, AccessResourceStatus status, String resourceType);

  @Query(
      value = "SELECT * from USER_ACCESSGROUPS_MAPPING AG where AG.ACC_GRP_RES_TYPE= ?4 AND AG.ACC_GRP_RES_ID IN"
          + " (?1) AND AG.USER_ACC_GRP_STATUS= ?2 AND AG.ACC_GRP_CODE in("
          + " select ACCESS_GROUP_ID from ACGS.ACGS_ACCESS_GROUP_POLICIES where ACCESS_POLICY_ID in"
          + " (select ACCESS_POLICY_ID from ACGS.ACGS_ACCS_POL_TO_POL_RIGHTS where ACCESS_POLICY_RIGHT_ID in"
          + " (select CODE from ACGS.ACGS_ACCESS_POLICY_RIGHTS where RESOURCE_TYPE= ?4 and RESOURCE_ID in (?1)"
          + " and ACCESS_ROLE IN (select CODE from ACGS.ACGS_ROLE_TYPES where ROLE_NAME= ?3  )))" + " ) ",
      nativeQuery = true)
  List<UserAccessGroupMappingDao> findByAccessRoleAndResourceIdsTypeAndStatus(List<String> resourceIds, int status,
      String accessRole, String resourceType);
}