// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import com.paysafe.gbp.commons.bigdata.Issuer;
import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.op.errorhandling.exceptions.InvalidFieldException;
import com.paysafe.upf.user.provisioning.config.OktaAppConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.config.UserConfig;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.domain.WalletPermission;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.Action;
import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.enums.SkrillPermissions;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.enums.UserAction;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.exceptions.UserProvisioningException;
import com.paysafe.upf.user.provisioning.feignclients.AccessGroupFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.PegasusFeignClient;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.repository.WalletPermissionRepository;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.service.FeatureFlagService;
import com.paysafe.upf.user.provisioning.service.MerchantAccountInfoService;
import com.paysafe.upf.user.provisioning.service.SkrillTellerAccountInfoService;
import com.paysafe.upf.user.provisioning.service.UserHandlerService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.AppUserConfigDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto.AuditUserEventDtoBuilder;
import com.paysafe.upf.user.provisioning.web.rest.dto.PermissionDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.ReportSchedule;
import com.paysafe.upf.user.provisioning.web.rest.dto.ReportScheduleResponse;
import com.paysafe.upf.user.provisioning.web.rest.dto.ResourceUsersValidationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.RoleDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupsListRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicyRight;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserListResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserAccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.WalletUserCountResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.SkrillAccessResources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class UserProvisioningUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserProvisioningUtils.class);

  private static final String OWNER_TYPE_PMLE = "PMLE";
  private static final String OWNER_TYPE_MLE = "MLE";
  private static final String EMAIL = "email";
  private static final String USERNAME = "loginName";
  public static final String FIELD = "field";


  private ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private UserService userService;

  @Autowired
  private UserConfig userConfig;

  @Autowired
  private AccessGroupFeignClient accessGroupFeignClient;

  @Autowired
  private OktaAppConfig oktaAppConfig;

  @Autowired
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Autowired
  private UsersRepository usersRepository;

  @Autowired
  private UserFilterUtil userFilterUtil;

  @Autowired
  private MerchantAccountInfoService merchantAccountInfoService;

  @Autowired
  private PegasusFeignClient pegasusFeignClient;

  @Autowired
  private FeatureFlagService featureFlagService;

  @Autowired
  private WalletPermissionRepository walletPermissionRepository;

  @Autowired
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Autowired
  private UserAssembler userAssembler;

  @Autowired
  private AuditService auditService;

  @Autowired
  private UserHandlerService userHandlerService;

  @Autowired
  private AuditUserEventUtil auditUserEventUtil;

  @Autowired
  private SkrillTellerConfig skrillTellerConfig;

  @Autowired
  private SkrillTellerAccountInfoService skrillTellerAccountInfoService;

  @Autowired
  private CommonUserProvisioningUtil commonUserProvisioningUtil;

  private static final String NETELLER_APPLICATION = "NETELLER";

  /**
   * Generates random role string.
   *
   * @return String randomRoleString
   */
  public String generateRandomRoleString() {
    return commonUserProvisioningUtil.generateRandomRoleString();
  }

  /**
   * Checks if usercount exceeds the specified limit and returns boolean.
   */
  public ResourceUsersValidationDto verifyUserCountforResource(String resourceId, String resourceName,
      String application) {
    return commonUserProvisioningUtil.verifyUserCountforResource(resourceId, resourceName, application);
  }

  /**
   * In the update user request , the accessResource is a new list that needs to be assigned to user(It is not the
   * delta). To achieve this we have to check whether the updated user has access to any other current admin
   * wallets(resources) alreay and remove the access.
   */
  public void validateAccessResources(UserUpdationDto userUpdationDto,
      List<AccessGroupResponseResource> fetchedAccessGroups) {
    commonUserProvisioningUtil.validateAccessResources(userUpdationDto, fetchedAccessGroups);
  }

  /**
   * Validate roles.
   */
  public void validateExistingRoles(List<String> existingRoles,
      List<String> existingRoleNamesListFromPermissionService) {
    if (!existingRoleNamesListFromPermissionService.containsAll(existingRoles)) {
      existingRoles.removeAll(existingRoleNamesListFromPermissionService);
      LOGGER.error("Request contains invalid roles: {}.", existingRoles.toString());
      throw BadRequestException.builder().details("Request contains invalid roles: " + existingRoles.toString())
          .build();
    }
  }

  /**
   * This method used to set the owner info from CommonThreadLocal.
   */
  public void setOwnerInfo(UserDto userDto) {
    AuthorizationInfo authInfo = CommonThreadLocal.getAuthLocal();
    if (authInfo != null) {
      if (StringUtils.isNotEmpty(authInfo.getOwnerId())) {
        userDto.setOwnerId(authInfo.getOwnerId());
      }
      if (StringUtils.isNotEmpty(authInfo.getOwnerType())) {
        userDto.setOwnerType(authInfo.getOwnerType());
      }
      List<AccessResources> accessResources = userDto.getAccessResources();
      if (StringUtils.equals(authInfo.getApplication(), DataConstants.SKRILL)
          && CollectionUtils.isNotEmpty(accessResources)) {
        for (AccessResources accessResource : accessResources) {
          populateOwnerFieldsForSkrill(accessResource);
        }
      }
    }
  }

  /**
   * Utility method to manage the user groups while updating the user.
   */
  public void handleUpdateUserGroupIds(User user, UserUpdationDto userUpdationDto) {
    String ownerType = userUpdationDto.getOwnerType();
    if (StringUtils.isNotEmpty(ownerType)) {
      String applicationInDb = user.getApplication();
      String updatedApplication = getApplicationByOwnerType(ownerType);
      if (!StringUtils.equals(applicationInDb, updatedApplication)) {
        identityManagementFeignClient.removeUserFromGroup(oktaAppConfig.getGroupIds().get(applicationInDb),
            user.getUserExternalId());
        identityManagementFeignClient.addUserToGroup(oktaAppConfig.getGroupIds().get(updatedApplication),
            user.getUserExternalId());
        userUpdationDto.setApplicationName(updatedApplication);
        updateApplicationInCommonThreadLocal(updatedApplication);
      }
    }
  }

  private String getApplicationByOwnerType(String ownerType) {
    if ((StringUtils.equals(ownerType, DataConstants.PMLE)) || (StringUtils.equals(ownerType, DataConstants.MLE))) {
      return DataConstants.PORTAL;
    } else if ((StringUtils.equals(ownerType, DataConstants.PARTNER))
        || (StringUtils.equals(ownerType, DataConstants.ACCOUNT_GROUP))) {
      return DataConstants.PARTNER_PORTAL;
    } else {
      return ownerType;
    }
  }

  private void updateApplicationInCommonThreadLocal(String application) {
    AuthorizationInfo authorizationInfo = CommonThreadLocal.getAuthLocal();
    authorizationInfo.setApplication(application);
    CommonThreadLocal.setAuthLocal(authorizationInfo);
  }

  /**
   * This method checks for the AccessResources with same ownerId and ownerType and with different actions. If finds
   * then deletes the object with action 'HARD_DELETE' from the list.
   */
  public void deleteDuplicateAccessResourcesFromRequest(List<UpdateUserAccessResources> accessResources) {
    Predicate<UpdateUserAccessResources> hardDeleteAccessResource =
        accessResource -> Action.HARD_DELETE.equals(accessResource.getAction());
    Predicate<UpdateUserAccessResources> addAccessResource =
        accessResource -> Action.ADD.equals(accessResource.getAction());
    List<UpdateUserAccessResources> hardDeletedAccessResources =
        accessResources.stream().filter(hardDeleteAccessResource).collect(Collectors.toList());
    List<UpdateUserAccessResources> addedAccessResources =
        accessResources.stream().filter(addAccessResource).collect(Collectors.toList());
    for (UpdateUserAccessResources hardDeletedAccessResource : hardDeletedAccessResources) {
      for (UpdateUserAccessResources addedAccessResource : addedAccessResources) {
        if (StringUtils.equals(hardDeletedAccessResource.getOwnerId(), addedAccessResource.getOwnerId())
            && StringUtils.equals(hardDeletedAccessResource.getOwnerType(), addedAccessResource.getOwnerType())
            && StringUtils.equals(hardDeletedAccessResource.getType(), addedAccessResource.getType())) {
          accessResources.remove(hardDeletedAccessResource);
        }
      }
    }
  }

  /**
   * Populates the accessResources for getUsers response.
   */
  public void populateAccessResourcesGetUsers(List<UserResponseResource> userResponseResourceList) {
    for (UserResponseResource userResponseResource : userResponseResourceList) {
      userResponseResource.setAccessResources(getAccessResourcesForIds(userResponseResource.getAccessGroups()));
    }
  }

  /**
   * Populates the accessResources for getUsers response.
   */
  public void populateAccessResourcesAndLegalEntity(List<UserResponseResource> userResponseResourceList) {
    for (UserResponseResource userResponseResource : userResponseResourceList) {
      List<AccessResources> accessResourcesList = getAccessResourcesForIds(userResponseResource.getAccessGroups());
      userResponseResource.setAccessResources(accessResourcesList);
      if (!accessResourcesList.isEmpty()) {
        AccessResources accessResources = accessResourcesList.get(0);
        if (OWNER_TYPE_PMLE.equalsIgnoreCase(accessResources.getOwnerType())
            || OWNER_TYPE_MLE.equalsIgnoreCase(accessResources.getOwnerType())) {
          String legalEntityName = userFilterUtil.getLegalEntityNameFromMasterMerchant(accessResources);
          userResponseResource.getCustomProperties().put("legalEntityName", legalEntityName);
        }
      }
    }
  }

  /**
   * Populates the accessResources for Skrillteller filter users response.
   */
  public void populateAccessResourcesForSkrillTeller(List<UserResponseResource> userResponseResourceList) {
    for (UserResponseResource userResponseResource : userResponseResourceList) {
      List<AccessResources> accessResourcesList = new ArrayList<>();
      for (AccessResources accessResourceObj : userResponseResource.getAccessResources()) {
        List<AccessResources> accessResources =
            getAccessResourcesForIds(Arrays.asList(accessResourceObj.getAccessGroupId()));
        accessResources.get(0).setStatus(accessResourceObj.getStatus());
        accessResourcesList.add(accessResources.get(0));
      }
      userResponseResource.setAccessResources(accessResourcesList);
    }
  }

  /**
   * Populates the accessResources for createUSer response.
   */
  public void populateAccessResources(UserProvisioningUserResource userProvisioningUserResource) {
    List<String> accessGroupIds = userProvisioningUserResource.getAccessGroups();
    if (CollectionUtils.isNotEmpty(accessGroupIds)) {
      userProvisioningUserResource.setAccessResources(getAccessResourcesForIds(accessGroupIds));
    }
  }

  /**
   * Populates the accessResources for createUSer response.
   */
  public List<AccessResources> fetchAccessResourcesForSkrillTeller(List<String> accessGroupIds) {
    if (CollectionUtils.isNotEmpty(accessGroupIds)) {
      return getAccessResourcesForIds(accessGroupIds);
    }
    return new ArrayList<>();
  }

  private List<AccessResources> getAccessResourcesForIds(List<String> accessGroupIds) {
    List<AccessResources> accessResources = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(accessGroupIds)) {
      List<AccessGroupResponseResource> accessGroups =
          accessGroupFeignClient.getAccessGroupsFromInputList(new AccessGroupsListRequestResource(accessGroupIds));
      for (AccessGroupResponseResource accessGroup : accessGroups) {
        List<AccessPolicyRight> accessPolicyRights =
            accessGroup.getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights();
        if (CollectionUtils.isEmpty(accessPolicyRights)) {
          continue;
        }
        AccessResources accessResource = new AccessResources();
        accessResource.setAccessGroupId(accessGroup.getCode());
        accessResource.setAccessGroupType(accessGroup.getType());
        accessResource.setStatus(AccessResourceStatus.ACTIVE);
        accessResource.setOwnerType(accessGroup.getOwnerType());
        accessResource.setOwnerId(accessGroup.getOwnerId());
        accessResource.setType(accessPolicyRights.get(0).getAccessRight().getResourceType());
        Set<String> ids = populateResourceIds(accessGroup);
        List<String> idList = new ArrayList<>();
        idList.addAll(ids);
        if (StringUtils.equals(accessResource.getType(), DataConstants.PAYMENT_ACCOUNT)) {
          accessResource.setIds(idList);
        } else {
          accessResource.setId(idList.get(0));
        }
        String role = accessPolicyRights.get(0).getAccessRight().getAccessRole();
        if (StringUtils.isNotEmpty(role)) {
          accessResource.setRole(role);
          accessResource.setPermissions(getPermissionsForPredefinedRole(accessGroup));
        } else if (AccessGroupType.DEFAULT_ADMIN.equals(accessGroup.getType())) {
          accessResource.setRole(DataConstants.ADMIN);
          accessResource.setPermissions(getPermissionsForCustomRole(accessGroup));
        } else {
          accessResource.setRole(DataConstants.REGULAR);
          accessResource.setPermissions(getPermissionsForCustomRole(accessGroup));
        }
        accessResources.add(accessResource);
      }
    }
    return accessResources;
  }

  private Set<String> populateResourceIds(AccessGroupResponseResource accessGroup) {
    Set<String> ids = new HashSet<>();
    List<AccessPolicyRight> accessPolicyRights =
        accessGroup.getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights();
    for (AccessPolicyRight accessPolicyRight : accessPolicyRights) {
      ids.add(accessPolicyRight.getAccessRight().getResourceId());
    }
    return ids;
  }

  private List<PermissionDto> getPermissionsForCustomRole(AccessGroupResponseResource accessGroup) {
    List<AccessPolicyRight> accessPolicyRights =
        accessGroup.getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights();
    Set<String> permissionSet = new HashSet<>();
    List<PermissionDto> permissions = new ArrayList<>();
    for (AccessPolicyRight accessPolicyRight : accessPolicyRights) {
      String permissionValue = accessPolicyRight.getAccessRight().getAccessTypeValue();
      List<String> permissionValues = accessPolicyRight.getAccessRight().getAccessRolePermissions();
      List<String> permissionList = new ArrayList<>();
      if (StringUtils.isNotEmpty(permissionValue)) {
        permissionList.add(permissionValue);
      } else if (CollectionUtils.isNotEmpty(permissionValues)) {
        permissionList.addAll(permissionValues);
      }
      for (String permissionObj : permissionList) {
        if (!permissionSet.contains(permissionObj)) {
          PermissionDto permission = new PermissionDto();
          permission.setLabel(permissionObj);
          permissions.add(permission);
          permissionSet.add(permissionObj);
        }
      }
    }
    return permissions;
  }

  private List<PermissionDto> getPermissionsForPredefinedRole(AccessGroupResponseResource accessGroup) {
    List<PermissionDto> permissions = new ArrayList<>();
    List<String> accessRolePermissions = accessGroup.getAccessGroupPolicies().get(0).getAcessPolicy()
        .getAccessPolicyRights().get(0).getAccessRight().getAccessRolePermissions();
    for (String accessRolePermission : accessRolePermissions) {
      PermissionDto permission = new PermissionDto();
      permission.setLabel(accessRolePermission);
      permissions.add(permission);
    }
    return permissions;
  }

  /**
   * This method used to set the owner info from CommonThreadLocal for update user dto.
   */
  public void setOwnerInfoUpdateUser(UserUpdationDto userUpdationDto) {
    AuthorizationInfo authInfo = CommonThreadLocal.getAuthLocal();
    if (authInfo != null) {
      if (StringUtils.isNotEmpty(authInfo.getOwnerId())) {
        userUpdationDto.setOwnerId(authInfo.getOwnerId());
      }
      if (StringUtils.isNotEmpty(authInfo.getOwnerType())) {
        userUpdationDto.setOwnerType(authInfo.getOwnerType());
      }
      List<UpdateUserAccessResources> accessResources = userUpdationDto.getAccessResources();
      if (StringUtils.equals(authInfo.getApplication(), DataConstants.SKRILL)
          && CollectionUtils.isNotEmpty(accessResources)) {
        for (UpdateUserAccessResources accessResource : accessResources) {
          populateOwnerFieldsForSkrill(accessResource);
        }
      }
    }
  }

  /**
   * This method is used to populate the ownerId and ownerType fields of SKRILL application.
   */
  private void populateOwnerFieldsForSkrill(AccessResources accessResource) {
    accessResource.setOwnerId(accessResource.getId());
    accessResource.setOwnerType(accessResource.getType());
  }

  /**
   * This method is to update the user status.
   *
   * @throws JsonProcessingException ex
   */
  public void updateUserStatus(String userId, UpdateUserStatusResource resource) throws JsonProcessingException {

    UsersListResponseResource userListResponseResource = userService.getUsers(resource.getUserName(),
        resource.getResourceType(), resource.getResourceId(), null, null, null, null, 0, 20, new MutableBoolean(false));
    validateUpdateUserStatusRequest(resource, userListResponseResource);
    if (isBulkStatusUpdate(resource)) {
      updateBulkStatus(userId, resource, userListResponseResource);
      return;
    } else {
      checkAndDeleteUserReports(userId, resource, userListResponseResource);
    }
    resource.setStatusUpdate(true);
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    checkIsValidStatusUpdate(resource, userListResponseResource, application);
    if (isPortalLockDormantAndOkta(application, userListResponseResource)) {
      updateUserInIdmService(userId, resource, application);
      auditService.createAuditEntry(
          auditUserEventUtil.constructAuditUserEventDto(userListResponseResource, AuditEventStatus.SUCCESS)
              .eventData(contructEventData(resource.getAction().toString(),
                  "User status has been updated for " + userListResponseResource.getUsers().get(0).getUserName()
                      + " from " + userListResponseResource.getUsers().get(0).getStatus() + " to "
                      + resource.getAction().toString() + " successfully by " + getUserNameFromThreadLocal()))
              .eventType(AuditEventType.UPDATE_USER).createdBy(getUserNameFromThreadLocal())
              .targetUserName(userListResponseResource.getUsers().get(0).getUserName())
              .targetUserId(userListResponseResource.getUsers().get(0).getId()).build());
    } else if ((!userListResponseResource.getUsers().get(0).getAccessResources().isEmpty()) && (userListResponseResource
        .getUsers().get(0).getAccessResources().get(0).getStatus().equals(AccessResourceStatus.BLOCKED)
        || userListResponseResource.getUsers().get(0).getAccessResources().get(0).getStatus()
        .equals(AccessResourceStatus.ACTIVE))) {
      if (!userListResponseResource.getUsers().get(0).getAccessResources().get(0).getStatus()
          .equals(AccessResourceStatus.ACTIVE)) {
        verifyUserCount(resource.getResourceId(), resource.getResourceType(), AccessGroupType.CUSTOMIZED, application);
      }
      updateUserInUserProvisioning(userId, resource);
      if ((DataConstants.SKRILL.equalsIgnoreCase(application)
              || DataConstants.NETELLER.equalsIgnoreCase(application))) {
        updateUserStatusForSkrillAndNeteller(userId, resource, application);
      }
      auditService.createAuditEntry(
          auditUserEventUtil.constructAuditUserEventDto(userListResponseResource, AuditEventStatus.SUCCESS)
              .eventData(contructEventData(resource.getAction().toString(),
                  userListResponseResource.getUsers().get(0).getUserName() + "'s Wallet " + resource.getResourceId()
                      + " status has been updated from "
                      + userListResponseResource.getUsers().get(0).getAccessResources().get(0).getStatus() + " to "
                      + resource.getAction().toString() + " successfully by " + getUserNameFromThreadLocal()))
              .eventType(AuditEventType.UPDATE_USER).createdBy(getUserNameFromThreadLocal())
              .targetUserName(userListResponseResource.getUsers().get(0).getUserName())
              .targetUserId(userListResponseResource.getUsers().get(0).getId()).build());
    }
  }

  /**
   * This method is used to used to update user status in okta and database for Skrill and Neteller.
   */
  public void updateUserStatusForSkrillAndNeteller(String userId, UpdateUserStatusResource resource,
                                                   String application) {
    List<UserAccessGroupMappingDao> userAccessGroupDaoList;
    userAccessGroupDaoList =
            userAccessGroupMapppingRepository.findByLoginNameAndResourceType(resource.getUserName(),
                    resource.getResourceType(), application);
    long count = userAccessGroupDaoList.stream().filter(userAccessGroupDao ->
            AccessResourceStatus.BLOCKED.equals(userAccessGroupDao.getUserAccessGroupStatus())).count();
    if (resource.getAction().equals(UserAction.BLOCKED) && (count == userAccessGroupDaoList.size())) {
      resource.setAction(UserAction.SUSPEND);
      updateUserInIdmService(userId, resource, application);
    } else if (resource.getAction().equals(UserAction.ACTIVATE) && (count == userAccessGroupDaoList.size() - 1)) {
      updateUserInIdmService(userId, resource, application);
    }
  }

  /**
   * checkAndDeleteUserReports.
   */
  public void checkAndDeleteUserReports(String userId,
      UpdateUserStatusResource resource,
      UsersListResponseResource userListResponseResource) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    if (!checkIfDeleteReportEnabled(application)) {
      return;
    }
    if (Arrays.asList(DataConstants.SKRILL, DataConstants.NETELLER).contains(application)) {
      if (resource.getAction().equals(UserAction.BLOCKED)) {
        deleteUserReports(userId, resource.getResourceId());
      } else if (resource.getAction().equals(UserAction.BLOCK_ALL)) {
        for (AccessResources eachWallet : userListResponseResource.getUsers().get(0).getAccessResources()) {
          deleteUserReports(userId, eachWallet.getId());
        }
      }
    }
  }

  /**
   * checkIfDeleteReportEnabled.
   */
  public boolean checkIfDeleteReportEnabled(String application) {
    return featureFlagService.fetchFeatureFlag().size() > 0
        && featureFlagService.fetchFeatureFlag().get("ST_DeleteReports") != null
        && featureFlagService.fetchFeatureFlag().get("ST_DeleteReports")
        && Arrays.asList(DataConstants.SKRILL, DataConstants.NETELLER).contains(application);
  }

  private void deleteUserReports(String userId,
      String walletId) {
    ReportScheduleResponse personalReports =
        skrillTellerAccountInfoService.getSchedules(userId, walletId);
    if (!personalReports.getContent().isEmpty()) {
      for (ReportSchedule eachReport : personalReports.getContent()) {
        skrillTellerAccountInfoService.deleteScheduleReport(userId, eachReport.getId());
      }
    }
  }

  /**
   * This method deletes Scheduled reports for removed Skrill/Neteller Wallets.
   */
  public void deleteReportsforWallet(String userId, UserUpdationDto userUpdationDto) {
    if (userUpdationDto.getAccessResources() != null && !userUpdationDto.getAccessResources().isEmpty()) {
      List<UpdateUserAccessResources> removedWallets =
          userUpdationDto.getAccessResources().stream().filter(each -> each.getAction().equals(Action.HARD_DELETE))
              .collect(Collectors.toList());
      for (UpdateUserAccessResources eachWallet : removedWallets) {
        deleteUserReports(userId, eachWallet.getId());
      }
    }
  }

  boolean isPortalLockDormantAndOkta(String application, UsersListResponseResource userListResponseResource) {
    return (DataConstants.PORTAL.equalsIgnoreCase(application)
        || DataConstants.PARTNER_PORTAL.equalsIgnoreCase(application))
        || ((userListResponseResource.getUsers().get(0).getStatus().equals(UserStatus.LOCKED_OUT)
        || userListResponseResource.getUsers().get(0).getStatus().equals(UserStatus.DORMANT))
        && CommonThreadLocal.getAuthLocal().getIssuer().equals(Issuer.OKTA));
  }

  private void checkIsValidStatusUpdate(UpdateUserStatusResource resource,
      UsersListResponseResource userListResponseResource, String application) throws JsonProcessingException {
    Predicate<UsersListResponseResource> userStatusPredicate;
    if (userListResponseResource.getUsers().get(0).getAccessResources().isEmpty()
        || (DataConstants.PORTAL.equalsIgnoreCase(application)
        || DataConstants.PARTNER_PORTAL.equalsIgnoreCase(application))) {
      userStatusPredicate = res -> res.getUsers().get(0).getStatus().equals(UserStatus.PROVISIONED)
          && resource.getAction().equals(UserAction.ACTIVATE)
          || res.getUsers().get(0).getStatus().equals(UserStatus.PROVISIONED)
          && resource.getAction().equals(UserAction.BLOCKED);
    } else {
      userStatusPredicate = res -> res.getUsers().get(0).getStatus().equals(UserStatus.PROVISIONED)
          && resource.getAction().equals(UserAction.ACTIVATE)
          || res.getUsers().get(0).getStatus().equals(UserStatus.PROVISIONED)
          && resource.getAction().equals(UserAction.BLOCKED)
          || res.getUsers().get(0).getAccessResources().get(0).getStatus().equals(AccessResourceStatus.BLOCKED)
          && resource.getAction().equals(UserAction.BLOCKED)
          || res.getUsers().get(0).getAccessResources().get(0).getStatus().equals(AccessResourceStatus.ACTIVE)
          && resource.getAction().equals(UserAction.ACTIVATE)
          && !res.getUsers().get(0).getStatus().equals(UserStatus.LOCKED_OUT)
          && !res.getUsers().get(0).getStatus().equals(UserStatus.DORMANT);
    }
    if (userStatusPredicate.test(userListResponseResource)) {
      auditService.createAuditEntry(
          auditUserEventUtil.constructAuditUserEventDto(userListResponseResource, AuditEventStatus.FAILED)
              .eventData(contructEventData("Provided status action is not supported: " + resource.getAction(), null))
              .build());
      throw new BadRequestException.Builder()
          .details("Provided status action is not supported: " + resource.getAction())
          .errorCode(CommonErrorCode.NOT_SUPPORTED).build();
    }
  }

  private boolean isBulkStatusUpdate(UpdateUserStatusResource resource) {
    return (UserAction.ACTIVE_ALL.equals(resource.getAction())) || (UserAction.BLOCK_ALL.equals(resource.getAction()));
  }

  private void updateBulkStatus(String userId, UpdateUserStatusResource resource,
      UsersListResponseResource userListResponseResource) throws JsonProcessingException {
    auditService.createAuditEntry(
        auditUserEventUtil.constructAuditUserEventDto(userListResponseResource, AuditEventStatus.SUCCESS)
            .eventData(contructEventData(resource.getAction().toString(),
                "User status has been updated for " + userListResponseResource.getUsers().get(0).getUserName()
                    + " from " + userListResponseResource.getUsers().get(0).getStatus() + " to "
                    + resource.getAction().toString() + " successfully by " + getUserNameFromThreadLocal()))
            .build());
    List<UserAccessGroupMappingDao> accessGroups = null;
    UserAction statusToUpdate = null;
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    if ((userListResponseResource.getUsers().get(0).getStatus().equals(UserStatus.LOCKED_OUT)
        || userListResponseResource.getUsers().get(0).getStatus().equals(UserStatus.DORMANT))
        && (UserAction.ACTIVE_ALL.equals(resource.getAction()))) {
      resource.setAction(UserAction.ACTIVATE);
      updateUserInIdmService(userId, resource, application);
    } else if (UserAction.ACTIVE_ALL.equals(resource.getAction())) {
      accessGroups = userAccessGroupMapppingRepository.findByLoginNameAndUserAccessGroupStatus(resource.getUserName(),
          AccessResourceStatus.BLOCKED, application);
      statusToUpdate = UserAction.ACTIVATE;
      updateStatusOfWalletsResources(userId, resource, accessGroups, statusToUpdate);
    } else {
      accessGroups = userAccessGroupMapppingRepository.findByLoginNameAndUserAccessGroupStatus(resource.getUserName(),
          AccessResourceStatus.ACTIVE, application);
      statusToUpdate = UserAction.BLOCKED;
      updateStatusOfWalletsResources(userId, resource, accessGroups, statusToUpdate);
    }
  }

  private void updateStatusOfWalletsResources(String userId, UpdateUserStatusResource resource,
      List<UserAccessGroupMappingDao> accessGroups, UserAction statusToUpdate) throws JsonProcessingException {
    for (UserAccessGroupMappingDao userAccessGroup : accessGroups) {
      updateUserStatus(userId, getUpdateUserStatusResource(resource, userAccessGroup, statusToUpdate));
    }
  }

  private UpdateUserStatusResource getUpdateUserStatusResource(UpdateUserStatusResource resource,
      UserAccessGroupMappingDao userAccessGroupDao, UserAction statusToUpdate) {
    UpdateUserStatusResource updateUserStatusResource = new UpdateUserStatusResource();
    updateUserStatusResource.setAccessGroupId(userAccessGroupDao.getAccessGroupCode());
    updateUserStatusResource.setAction(statusToUpdate);
    updateUserStatusResource.setEmail(resource.getEmail());
    updateUserStatusResource.setResourceId(userAccessGroupDao.getResourceId());
    updateUserStatusResource.setResourceType(userAccessGroupDao.getResourceType());
    updateUserStatusResource.setUserName(resource.getUserName());
    return updateUserStatusResource;
  }

  private void validateUpdateUserStatusRequest(UpdateUserStatusResource resource,
      UsersListResponseResource userListResponseResource) throws JsonProcessingException {
    if (!StringUtils.equals(resource.getEmail(), userListResponseResource.getUsers().get(0).getEmail())) {
      auditService.createAuditEntry(
          auditUserEventUtil.constructAuditUserEventDto(userListResponseResource, AuditEventStatus.FAILED)
              .eventData(contructEventData("Incorrect Email address", null)).build());
      throw BadRequestException.builder().details("Incorrect Email address").build();
    }
    if (StringUtils.isNotEmpty(resource.getResourceId())
        && CollectionUtils.isEmpty(userListResponseResource.getUsers().get(0).getAccessResources())) {
      auditService.createAuditEntry(
          auditUserEventUtil.constructAuditUserEventDto(userListResponseResource, AuditEventStatus.FAILED)
              .eventData(contructEventData("ResourceId does not exist", null)).build());
      throw new BadRequestException.Builder().details("ResourceId does not exist")
          .errorCode(CommonErrorCode.NOT_SUPPORTED).build();
    }
  }

  private String contructEventData(String reason, String details) throws JsonProcessingException {
    ObjectNode auditEventDataObjectNode = objectMapper.createObjectNode();
    auditEventDataObjectNode.put("reason", reason);
    if (details != null) {
      auditEventDataObjectNode.put("details", details);
    }
    return objectMapper.writeValueAsString(auditEventDataObjectNode);
  }

  private void verifyUserCount(String resourceId, String resourceType, AccessGroupType accessGroupType,
      String application) {
    if (!DataConstants.PORTAL.equalsIgnoreCase(application)
        && !DataConstants.PARTNER_PORTAL.equalsIgnoreCase(application)) {
      ResourceUsersValidationDto walletUsersValidationDto =
          verifyUserCountforResource(resourceId, resourceType, application);
      if ((accessGroupType.equals(AccessGroupType.DEFAULT_ADMIN) && walletUsersValidationDto.getCanAddAdminUsers())
          || (accessGroupType.equals(AccessGroupType.CUSTOMIZED) && walletUsersValidationDto.getCanAddUsers())) {
        return;
      }
      throw BadRequestException.builder().details("Maximum users limit reached for wallet :" + resourceId).build();
    }
  }

  private void updateUserInUserProvisioning(String userId, UpdateUserStatusResource resource)
      throws JsonProcessingException {
    UserUpdationDto userUpdationDto = getEmptyUserUpdationDto();
    userUpdationDto.setUserName(resource.getUserName());
    userUpdationDto.setEmail(resource.getEmail());
    userUpdationDto.setApplicationName(CommonThreadLocal.getAuthLocal().getApplication());
    List<String> accessGroups = new ArrayList<>();
    accessGroups.add(resource.getAccessGroupId());
    if (resource.getAction().equals(UserAction.ACTIVATE)) {
      AccessGroupDto accessGroupDto = new AccessGroupDto();
      accessGroupDto.setExistingAccessGroupIds(accessGroups);
      userUpdationDto.setAccessGroupsToAdd(accessGroupDto);
    } else {
      userUpdationDto.setAccessGroupsToDelete(accessGroups);
    }
    userUpdationDto.setStatusUpdate(resource.isStatusUpdate());
    userService.updateUser(userId, userUpdationDto);
  }

  private UserUpdationDto getEmptyUserUpdationDto() {
    UserUpdationDto userUpdationDto = new UserUpdationDto();
    RoleDto roleDto = new RoleDto();
    roleDto.setExistingRoles(new ArrayList<>());
    roleDto.setCustomRoles(new ArrayList<>());
    userUpdationDto.setRolesToAdd(roleDto);
    userUpdationDto.setRolesToDelete(new ArrayList<>());
    AccessGroupDto addAccessGroups = new AccessGroupDto();
    addAccessGroups.setCustomAccessGroupDtos(new ArrayList<>());
    addAccessGroups.setExistingAccessGroupIds(new ArrayList<>());
    userUpdationDto.setAccessGroupsToAdd(addAccessGroups);
    userUpdationDto.setAccessGroupsToDelete(new ArrayList<>());
    userUpdationDto.setAccessGroupsToHardDelete(new ArrayList<>());
    return userUpdationDto;
  }

  /**
   * Updates user status in idm.
   */
  public void updateUserInIdmService(String userId, UpdateUserStatusResource resource, String application) {
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setEmail(resource.getEmail());
    userResource.setUserName(resource.getUserName());
    if ((DataConstants.PORTAL.equalsIgnoreCase(application)
        || DataConstants.PARTNER_PORTAL.equalsIgnoreCase(application))
        && resource.getAction().equals(UserAction.BLOCKED)) {
      identityManagementFeignClient.updateUserStatus(userId, UserAction.SUSPEND, userResource);
    } else {
      identityManagementFeignClient.updateUserStatus(userId, resource.getAction(), userResource);
    }
    updateUserStatusInDatabase(userId);
  }

  /**
   * updates the userStatus in the database.
   */
  public void updateUserStatusInDatabase(String userId) {
    IdentityManagementUserResource userResponse = userService.fetchUser(userId);
    User user = usersRepository.findByUserId(userResponse.getId());

    if (user != null) {
      user.setStatus(Status.valueOf(userResponse.getStatus().toString()));
      usersRepository.save(user);
    } else {
      LOGGER.error("userId {}: is not present in Database ", userResponse.getId());
      throw InternalErrorException.builder().details("User is not found in Database").build();
    }
  }

  /**
   * this method is used to set the created&lastUpdated dates and legalEntityName to the userResponse.
   */
  public void setLegalEntityNameAndCreatedUpdatedDates(UserResponseResource userResponseResource) {
    String application = null;
    if (CommonThreadLocal.getAuthLocal() != null) {
      application = CommonThreadLocal.getAuthLocal().getApplication();
    }

    User user = usersRepository.findByUserId(userResponseResource.getId());
    if (user != null) {
      userResponseResource.setCreatedDate(user.getCreatedDate());
      userResponseResource.setLastModifiedDate(user.getLastModifiedDate());
      if (StringUtils.equals(application, DataConstants.PORTAL)
          || StringUtils.equals(application, DataConstants.PARTNER_PORTAL)) {
        populateAccessResourcesAndLegalEntity(new ArrayList<>(Arrays.asList(userResponseResource)));
      }
    } else {
      LOGGER.error("userId {}: is not present in Database ", userResponseResource.getId());
      throw InternalErrorException.builder().details("User is not found in Database").build();
    }
  }

  /**
   * Method for populating wallet names.
   */
  public void populateWalletNames(List<UserResponseResource> userResourceList) {
    Set<String> skrillTellerWalletIds = UserManagmentUtil.getSkrillTellerWalletIds(userResourceList);
    List<BasicWalletInfo> basicWalletInfoList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(skrillTellerWalletIds)) {
      basicWalletInfoList.addAll(merchantAccountInfoService.getBasicWalletInfo(skrillTellerWalletIds));
    }
    String businessUnit = CommonThreadLocal.getAuthLocal().getBusinessUnit();
    List<String> skrillLinkedBrands = skrillTellerConfig.getLinkedBrands().get(DataConstants.SKRILL.toLowerCase());
    for (String skrillLinkedBrand : skrillLinkedBrands) {
      Set<String> linkedBrandWalletIds =
          UserManagmentUtil.getWalletIdsByBusinessUnit(userResourceList, skrillLinkedBrand);
      if (CollectionUtils.isNotEmpty(linkedBrandWalletIds)) {
        CommonThreadLocal.getAuthLocal().setBusinessUnit(skrillLinkedBrand.toLowerCase());
        List<BasicWalletInfo> basicLinkedBrandWalletInfoList =
            merchantAccountInfoService.getBasicWalletInfo(linkedBrandWalletIds);
        basicWalletInfoList.addAll(basicLinkedBrandWalletInfoList);
      }
    }
    CommonThreadLocal.getAuthLocal().setBusinessUnit(businessUnit.toLowerCase());
    Map<String, String> walletIdToName = basicWalletInfoList.stream()
        .filter(basicWalletInfo -> basicWalletInfo != null && basicWalletInfo.getBusinessProfile() != null).collect(
            Collectors.toMap(BasicWalletInfo::getId, walletInfo -> walletInfo.getBusinessProfile().getCompanyName()));

    userResourceList.forEach(userResponseResource -> userResponseResource.getAccessResources()
        .forEach(accessResource -> accessResource.setWalletName(walletIdToName.get(accessResource.getId()))));
  }

  /**
   * Method for validating the login and email availability.
   */
  public void validateLoginNameAndEmailAvailability(String loginName, String emailId) {
    List<FieldError> fieldErrors = new ArrayList<>();
    if (org.apache.commons.lang3.StringUtils.isBlank(emailId)
        && org.apache.commons.lang3.StringUtils.isBlank(loginName)) {
      fieldErrors.add(new FieldError(FIELD, USERNAME + " or " + EMAIL, "should be present"));
      LOGGER.warn("Error occurred while validating user request");
      throw new InvalidFieldException(fieldErrors);
    }
    checkUserExistsWithLoginName(loginName, fieldErrors);
    checkUserExistsWithEmail(emailId, fieldErrors);
    if (!fieldErrors.isEmpty()) {
      if (fieldErrors.size() == 2) {
        fieldErrors = new ArrayList<>(
            Arrays.asList(new FieldError(FIELD, USERNAME + " , " + EMAIL, "User name, Email already exists")));
      }
      LOGGER.warn("Error occurred while validating loginName and email availability");
      throw new InvalidFieldException(fieldErrors);
    }
  }

  private void checkUserExistsWithLoginName(String loginName, List<FieldError> fieldErrors) {
    if (org.apache.commons.lang3.StringUtils.isNotBlank(loginName)) {
      AuthorizationInfo authLocal = CommonThreadLocal.getAuthLocal();
      CommonThreadLocal.setAuthLocal(null);
      PegasusUserListResponseResource pegasusUsers = pegasusFeignClient.getUsers(loginName, null, 0, 20);
      if (pegasusUsers.getCount() > 0) {
        fieldErrors.add(new FieldError(FIELD, USERNAME, "User name already exists"));
      }
      CommonThreadLocal.setAuthLocal(authLocal);
      IdentityManagementUserListResource usersByUserName = identityManagementFeignClient.getUsersByUserName(loginName);
      if (!usersByUserName.getUsers().isEmpty()) {
        fieldErrors.add(new FieldError(FIELD, USERNAME, "User name already exists"));
      }
    }
  }

  private void checkUserExistsWithEmail(String emailId, List<FieldError> fieldErrors) {
    if (org.apache.commons.lang3.StringUtils.isNotBlank(emailId)) {
      IdentityManagementUserListResource usersByEmail = identityManagementFeignClient.getUsersByEmail(emailId);
      if (!usersByEmail.getUsers().isEmpty()) {
        fieldErrors.add(new FieldError(FIELD, EMAIL, "Email already exists"));
      }
    }
  }

  /**
   * Method to populate the permissions in accessResorces for Skrill with interalIds and displayOrder.
   */
  public void populateSkrillPermissionsWithIds(UserResponseResource userResponseResource) {
    List<AccessResources> accessResources = userResponseResource.getAccessResources();
    List<String> customAccessgroupIds =
        accessResources.stream().filter(ar -> !AccessGroupType.DEFAULT_ADMIN.equals(ar.getAccessGroupType()))
            .map(AccessResources::getAccessGroupId).collect(Collectors.toList());
    Map<String, AccessGroupResponseResource> accessGroupMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(customAccessgroupIds)) {
      AccessGroupsListRequestResource accessGroupsListRequestResource = new AccessGroupsListRequestResource();
      accessGroupsListRequestResource.setAccessGroupIds(customAccessgroupIds);
      accessGroupMap = accessGroupFeignClient.getAccessGroupsFromInputList(accessGroupsListRequestResource).stream()
          .collect(Collectors.toMap(AccessGroupResponseResource::getCode, Function.identity()));
    }

    if (CollectionUtils.isNotEmpty(accessResources)) {
      List<WalletPermission> walletPermissions = walletPermissionRepository.findAll();
      for (AccessResources accessResource : accessResources) {
        if (AccessGroupType.DEFAULT_ADMIN.equals(accessResource.getAccessGroupType())) {
          accessResource.setPermissions(getAdminPermissionDto(walletPermissions));
        } else if (AccessGroupType.CUSTOMIZED.equals(accessResource.getAccessGroupType())) {
          AccessGroupResponseResource accessGroupResponse = accessGroupMap.get(accessResource.getAccessGroupId());
          List<PermissionDto> permissions = getPermissionsForCustomRole(accessGroupResponse);
          List<String> accessRights = permissions.stream().map(this::getPermissionLabel).collect(Collectors.toList());
          accessResource.setPermissions(getPermissionDtos(accessRights));
          UserManagmentUtil.checkAndUpdateAccessRole(accessResource, accessGroupResponse);
        }
      }
    }
  }

  private String getPermissionLabel(PermissionDto permissionDto) {
    return permissionDto.getLabel();
  }

  private List<PermissionDto> getAdminPermissionDto(List<WalletPermission> walletPermissions) {
    List<PermissionDto> permissionDtos = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(walletPermissions)) {
      permissionDtos = walletPermissions.stream()
          .map(wP -> new PermissionDto(wP.getInternalId(), wP.getPermissionDesription(), wP.getDisplayOrder()))
          .collect(Collectors.toList());
    }
    return permissionDtos;
  }

  private List<PermissionDto> getPermissionDtos(List<String> accessRights) {
    List<WalletPermission> walletPermissions =
        walletPermissionRepository.findWalletPermissionsFromPermissions(accessRights);
    List<PermissionDto> permissionDtos = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(walletPermissions)) {
      permissionDtos = walletPermissions.stream()
          .map(wP -> new PermissionDto(wP.getInternalId(), wP.getPermissionDesription(), wP.getDisplayOrder()))
          .collect(Collectors.toList());
    }
    return permissionDtos;
  }


  /**
   * Method to delete AccessResourcesInfos.
   */
  public void deleteAccessResourcesInfo(List<String> accessGroupIds, IdentityManagementUserResource userResponse,
      User user) {
    for (String accessGroupId : accessGroupIds) {
      AccessGroupResponseResource accessGroupResponse =
          accessGroupFeignClient.fetchAccessGroupByCode(accessGroupId).getBody();
      if (accessGroupResponse == null) {
        LOGGER.error("Invalid access group id");
        throw BadRequestException.builder().details("Invalid access group id").build();
      } else {
        UserAccessGroupMappingDao userAccessGroupMappingDao = user.getAccessGroupMappingDaos().stream()
            .filter(mappingDao -> userResponse.getId().equals(mappingDao.getUserId())
                && accessGroupResponse.getMerchantId().equals(mappingDao.getResourceId())
                && accessGroupResponse.getMerchantType().equals(mappingDao.getResourceType()))
            .findAny().orElse(null);
        if (userAccessGroupMappingDao != null) {
          user.getAccessGroupMappingDaos().remove(userAccessGroupMappingDao);
        }
      }
    }

    if (!accessGroupIds.isEmpty()) {
      usersRepository.save(user);
    }
  }

  /**
   * Method to retrieve application.
   */
  public String checkAndRetrieveApplicationFromTHreadLocal() {
    if (CommonThreadLocal.getAuthLocal() != null
        && StringUtils.isNotEmpty(CommonThreadLocal.getAuthLocal().getApplication())) {
      return CommonThreadLocal.getAuthLocal().getApplication();
    } else {
      LOGGER.error("Application not present in the CommonThreadLocal");
      throw BadRequestException.builder().details("Application not present in the CommonThreadLocal").build();
    }
  }

  /**
   * Method to check resource type.
   */
  public boolean isResourceTypeUser(UserFetchByFiltersRequestDto userFetchByFiltersRequestDto) {
    return StringUtils.equals(userFetchByFiltersRequestDto.getResourceType(), DataConstants.USER);
  }

  /**
   * Method to construct Identity Management create user resource.
   */
  public IdentityManagementUserResource constructIdentityManagementCreateUserResource(UserDto userDto,
      List<String> rolesToAdd, List<String> accessGroupsToAdd) {
    if (userDto.getCustomProperties() == null) {
      userDto.setCustomProperties(new HashMap<>());
    }
    if (StringUtils.equals(userDto.getApplicationName(), DataConstants.PORTAL)
        || StringUtils.equals(userDto.getApplicationName(), DataConstants.PARTNER_PORTAL)) {
      userDto.getCustomProperties().put(DataConstants.OWNER_ID, userDto.getOwnerId());
      userDto.getCustomProperties().put(DataConstants.OWNER_TYPE, userDto.getOwnerType());
      userDto.getCustomProperties().put(DataConstants.IS_MIGRATED, userDto.isMigrated());
      userDto.getCustomProperties().put(DataConstants.DIVISION, userDto.getDivision());
      userDto.getCustomProperties().put(DataConstants.USER_ASSIGNED_APPLICATIONS,
          userDto.getUserAssignedApplications());
    }
    userDto.getCustomProperties().put(DataConstants.BUSINESS_UNIT, userDto.getBusinessUnit());
    IdentityManagementUserResource identityManagementUserResource = new IdentityManagementUserResource();
    BeanUtils.copyProperties(userDto, identityManagementUserResource);

    if (userDto.isMigrationUseCase() && userDto.getApplicationName() != null
        && userDto.getApplicationName().equalsIgnoreCase(NETELLER_APPLICATION)) {
      identityManagementUserResource.setHashedPassword(null);
      identityManagementUserResource.setMigrationUseCase(true);
    }
    identityManagementUserResource.setCustomProperties(userDto.getCustomProperties());
    identityManagementUserResource.setRoles(rolesToAdd);
    identityManagementUserResource.setAccessGroups(accessGroupsToAdd);
    identityManagementUserResource.setActivate(true);
    identityManagementUserResource.setApplication(userDto.getApplicationName());
    return identityManagementUserResource;
  }

  /**
   * Method to assign auditEvent.
   */
  public void assignAuditEvent(UserDto userDto, AuditUserEventDtoBuilder auditUserEventDtoBuilder)
      throws JsonProcessingException {
    auditUserEventDtoBuilder.eventType(AuditEventType.ADD_USER);
    auditUserEventDtoBuilder.createdBy(getUserNameFromThreadLocal());
    auditUserEventDtoBuilder
        .eventData(contructEventData("Add user", getUserNameFromThreadLocal() + " created " + userDto.getEmail()));
  }

  private String getUserNameFromThreadLocal() {
    if (CommonThreadLocal.getAuthLocal() == null) {
      return "SYSTEM";
    } else {
      return StringUtils.isEmpty(CommonThreadLocal.getAuthLocal().getUserName()) ? "SYSTEM"
          : CommonThreadLocal.getAuthLocal().getUserName();
    }
  }

  /**
   * Method to populate user list.
   */
  public void populateUserResponseList(Page<UserAccessGroupMappingDao> userAccessGroupDaoPage,
      List<UserResponseResource> userResponseResourceList, MutableBoolean isPartialSuccess) {
    Map<String, List<UserAccessGroupMappingDao>> userAccessResourcesGroupedByLoginName =
        userAccessGroupDaoPage.getContent().stream().collect(
            Collectors.groupingBy(UserAccessGroupMappingDao::getLoginName, LinkedHashMap::new, Collectors.toList()));
    for (Entry<String, List<UserAccessGroupMappingDao>> entry : userAccessResourcesGroupedByLoginName.entrySet()) {
      String loginNameKey = entry.getKey();
      UserResponseResource userResponseResource;
      try {
        userResponseResource = getUserByLoginName(loginNameKey, null);
      } catch (UserProvisioningException ex) {
        if (ex.getStatus() == HttpStatus.NOT_FOUND) {
          LOGGER.error("user: {} not present in okta for application: {}", loginNameKey,
              CommonThreadLocal.getAuthLocal().getApplication());
          if (!isPartialSuccess.booleanValue()) {
            isPartialSuccess.setValue(true);
          }
          continue;
        } else {
          throw ex;
        }
      }
      if (userResponseResource != null) {
        userResponseResource
            .setAccessResources(getAccessResourcesFromDao(userAccessResourcesGroupedByLoginName.get(loginNameKey)));
        populateSkrillPermissionsWithIds(userResponseResource);
        populateApplicationName(loginNameKey, userResponseResource);
        userResponseResourceList.add(userResponseResource);
      }
    }
  }

  /**
   * Method to get accessresources.
   */
  public List<AccessResources> getAccessResourcesFromDao(List<UserAccessGroupMappingDao> userAccessGroupDaoList) {
    List<AccessResources> accessResourcesList = new ArrayList<>();
    for (UserAccessGroupMappingDao userAccessGroupDao : userAccessGroupDaoList) {
      AccessResources accessResources = userAssembler.toAccessResources(userAccessGroupDao);
      accessResourcesList.add(accessResources);
    }
    return accessResourcesList;
  }

  /**
   * Method to get user.
   */
  public UserResponseResource getUserByLoginName(String loginName, String application) {
    ResponseEntity<IdentityManagementUserResource> response =
        identityManagementFeignClient.getUser(loginName, application);
    User userRepoResponse = usersRepository.findByUserExternalId(response.getBody().getExternalId());
    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
      return userAssembler.toUserResponseResource(response.getBody(), userRepoResponse);
    }
    return null;
  }

  /**
   * Method to get userList.
   */
  public List<UserResponseResource> getIdmUsers(List<String> userIds, String application) {
    List<IdentityManagementUserResource> resources = identityManagementFeignClient.getUsersList(userIds, application);
    return resources.stream().map(resource -> {
      User user = usersRepository.findByUserExternalId(resource.getExternalId());
      return userAssembler.toUserResponseResource(resource, user);
    }).collect(Collectors.toList());
  }

  /**
   * Utility method to add DEFAULT permission in user migration flow if permissions list is empty.
   */
  public void checkAndAddDefaultPermission(UserMigrationResource userMigrationResource) {
    List<SkrillAccessResources> accessResources = userMigrationResource.getAccessResources();
    if (CollectionUtils.isNotEmpty(accessResources)) {
      for (SkrillAccessResources accessResource : accessResources) {
        if (CollectionUtils.isEmpty(accessResource.getPermissions())) {
          addDefaultPermission(accessResource);
        }
      }
    }
  }

  private void addDefaultPermission(SkrillAccessResources accessResource) {
    List<SkrillPermissions> permissions = new ArrayList<>();
    permissions.add(SkrillPermissions.DEFAULT_PERMISSION);
    accessResource.setPermissions(permissions);
  }

  /**
   * Utility method to validate the create user wallets brand versus CommonThreadLocal application.
   */
  public void validateCreateUserWalletResources(List<AccessResources> accessResources, UserDto userDto) {
    if (CollectionUtils.isNotEmpty(accessResources)) {
      if (StringUtils.equalsIgnoreCase(CommonThreadLocal.getAuthLocal().getApplication(), DataConstants.SKRILL)) {
        UserManagmentUtil.validateAndSetBrandRoleCreateUser(accessResources, userDto.getBusinessUnit(),
            skrillTellerConfig);
      }
      validateWalletResource(accessResources.stream().map(AccessResources::getId).collect(Collectors.toSet()));
    }
  }

  /**
   * Utility method to validate the update user wallets brand versus CommonThreadLocal application.
   */
  public void validateUpdateUserWalletResources(List<UpdateUserAccessResources> accessResources,
      UserUpdationDto userUpdationDto) {
    if (CollectionUtils.isNotEmpty(accessResources)) {
      if (StringUtils.equalsIgnoreCase(CommonThreadLocal.getAuthLocal().getApplication(), DataConstants.SKRILL)) {
        UserManagmentUtil.validateAndSetBrandRoleUpdateUser(accessResources, userUpdationDto.getBusinessUnit(),
            skrillTellerConfig);
      }
      validateWalletResource(accessResources.stream().filter(aR -> (aR.getAction() != Action.HARD_DELETE))
          .map(AccessResources::getId).collect(Collectors.toSet()));
    }
  }

  private void validateWalletResource(Set<String> walletIds) {
    List<BasicWalletInfo> walletsInfo = merchantAccountInfoService.getWalletProfileAndMerchantSettings(walletIds);
    String businessUnit = CommonThreadLocal.getAuthLocal().getBusinessUnit();
    for (BasicWalletInfo walletInfo : walletsInfo) {
      if (!StringUtils.equalsIgnoreCase(businessUnit, walletInfo.getProfile().getBrand())) {
        throw new BadRequestException.Builder().errorCode(CommonErrorCode.INVALID_FIELD)
            .details("Request contains the walletId: which was not belongs to brand: " + walletInfo.getId(),
                businessUnit).build();
      }
    }
  }

  /**
   * Set application to userResponseResource.
   */
  public void populateApplicationName(String loginName, UserResponseResource userResponseResource) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    if (application.equalsIgnoreCase(DataConstants.SKRILL) || application.equalsIgnoreCase(DataConstants.NETELLER)) {
      userResponseResource.setApplication(application);
    } else {
      User user = usersRepository.findTopByLoginName(loginName);
      if (user != null) {
        userResponseResource.setApplication(user.getApplication());
      }
    }
  }
}
