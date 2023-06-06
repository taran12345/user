// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository;

import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.Status;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UsersRepository extends CrudRepository<User, String>, JpaSpecificationExecutor<User> {

  @Modifying
  @Query("delete from User n where n.userId = :userId")
  void deleteByUserId(@Param("userId") String userId);

  Optional<User> findByLoginNameAndApplication(String loginName, String application);

  User findTopByLoginName(String loginName);

  Page<User> findByOwnerTypeAndOwnerIdOrderByLastModifiedDateDesc(String ownerType, String ownerId,
      Pageable pageRequest);

  Page<User> findByOwnerTypeAndOwnerIdAndApplicationAndLoginNameNotOrderByLastModifiedDateDesc(String ownerType,
      String ownerId, String application, String loginName, Pageable pageRequest);

  Page<User> findByApplicationOrderByLastModifiedDateDesc(String application, Pageable pageRequest);

  @Query(value = "select * from uspr_users where application = ?1 and owner_type = ?2 and "
      + "owner_id = ?3 and (lower(login_name) like lower(concat('%', concat(?4, '%'))) "
      + "or lower(user_first_name) like lower(concat('%', concat(?4, '%'))) "
      + "or lower(user_last_name) like lower(concat('%', concat(?4, '%'))) "
      + "or lower(email) like lower(concat('%', concat(?4, '%')))) and lower(login_name) <> lower(?5) "
      + " order by last_modified_datetime desc", nativeQuery = true)
  Page<User> smartSearchUsers(String application, String ownerType, String ownerId, String queryString,
      String loggedInUserName, Pageable pageable);

  User findByUserId(String userId);

  @Query(
      value = "select ACCESS_GROUP_ID from ACGS.ACGS_ACCESS_GROUP_POLICIES where ACCESS_POLICY_ID IN"
          + "(select ACCESS_POLICY_ID from ACGS.ACGS_ACCS_POL_TO_POL_RIGHTS where ACCESS_POLICY_RIGHT_ID IN"
          + "(select CODE from ACGS.ACGS_ACCESS_POLICY_RIGHTS where RESOURCE_TYPE= ?1 and RESOURCE_ID IN (?2)))",
      nativeQuery = true)
  List<String> getAccessGroupIds(String resourceType, Set<String> resourceIds);

  @Query(value = "select ACCESS_GROUP_ID from ACGS.ACGS_ACCESS_GROUP_POLICIES where ACCESS_POLICY_ID IN"
      + "(select ACCESS_POLICY_ID from ACGS.ACGS_ACCS_POL_TO_POL_RIGHTS where ACCESS_POLICY_RIGHT_ID IN"
      + "(select CODE from ACGS.ACGS_ACCESS_POLICY_RIGHTS where RESOURCE_TYPE= ?1 "
      + "and RESOURCE_ID IN (?2) and ACCESS_ROLE IN "
      + "(select CODE from ACGS.ACGS_ROLE_TYPES where ROLE_NAME IN (?3))))", nativeQuery = true)
  List<String> getAccessGroupIdsWithRoles(String resourceType, Set<String> resourceIds, Set<String> roles);

  @Query("select user from User user where user.loginName in( select userAccessGroupMappingDao.loginName from "
      + "UserAccessGroupMappingDao userAccessGroupMappingDao where userAccessGroupMappingDao.resourceId= ?1 and"
      + " userAccessGroupMappingDao.accessGroupType= ?2 and userAccessGroupMappingDao.resourceType= ?3 and "
      + "userAccessGroupMappingDao.userAccessGroupStatus= ?4) and user.application= ?5")
  List<User> getWalletUsers(String walletId, AccessGroupType accessGroupType, String resourceType,
      AccessResourceStatus accessResourceStatus, String application);

  @Modifying
  @Transactional
  @Query("update User u set u.status = ?1 where u.userExternalId = ?2")
  void updateUserStatus(Status status, String userExternalId);

  User findByUserExternalId(String userExternalId);

  @Query(value = "select * from USPR_USERS where LOGIN_NAME in("
      + " select LOGIN_NAME from USER_ACCESSGROUPS_MAPPING where ACC_GRP_RES_TYPE= ?3 AND ACC_GRP_RES_ID= ?1"
      + " AND USER_ACC_GRP_STATUS= ?4 AND ACC_GRP_CODE in("
      + " select ACCESS_GROUP_ID from ACGS.ACGS_ACCESS_GROUP_POLICIES where ACCESS_POLICY_ID in"
      + " (select ACCESS_POLICY_ID from ACGS.ACGS_ACCS_POL_TO_POL_RIGHTS where ACCESS_POLICY_RIGHT_ID in"
      + " (select CODE from ACGS.ACGS_ACCESS_POLICY_RIGHTS where RESOURCE_TYPE= ?3 and RESOURCE_ID= ?1"
      + " and ACCESS_ROLE IN (select CODE from ACGS.ACGS_ROLE_TYPES where ROLE_NAME= ?2  ))))"
      + " ) and APPLICATION= ?5", nativeQuery = true)
  List<User> getWalletUsersByAccessRole(String walletId, String accessRole, String resourceType,
      int accessResourceStatus, String application);

  Page<User> findByApplicationAndBusinessUnitOrderByLastModifiedDateDesc(String application, String businessUnit,
      Pageable pageable);

  Page<User> findByApplicationAndStatus(String application, Status status, Pageable pageable);

  @Query(value = "select distinct users.email from uspr_users users inner join user_accessgroups_mapping acc_grps "
      + "on users.user_id=acc_grps.user_id where users.application = ?1 and users.email is not null",
      nativeQuery = true)
  List<String> getEmailsByApplication(String application);

  Page<EmailUserIdView> getEmailsByApplicationAndRegion(String application, String region, Pageable pageRequest);

  Page<User> findByApplicationAndRegion(String application, String region, Pageable pageRequest);

  Page<User> findByOwnerTypeAndOwnerIdAndApplicationOrderByLastModifiedDateDesc(String ownerType, String ownerId,
      String application, Pageable pageRequest);

  Page<User> findByApplicationAndRegionIsNullOrderByLastModifiedDateDesc(String application, Pageable pageRequest);

  @Modifying
  @Transactional
  @Query(value = "update uspr_users u set u.mfa_enabled = ?2 where u.user_id = ?1", nativeQuery = true)
  void updateMfaStatus(String userId, String mfaEnabled);

}
