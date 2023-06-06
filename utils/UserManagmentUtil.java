// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.domain.UserAssignedApplications;
import com.paysafe.upf.user.provisioning.enums.Action;
import com.paysafe.upf.user.provisioning.enums.OwnerType;
import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.model.OwnerInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.impl.UserServiceImpl;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccountGroupMerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUpdateUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.LogoutRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateActionResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserAccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UserManagmentUtil {

  private static final String LOGOUT_APPLICATION_NAME = "BusinessPortal";
  private static final String LOGOUT_BRAND = "Paysafe";
  private static ObjectMapper objectMapper = new ObjectMapper();
  private static final Logger LOGGER = LoggerFactory.getLogger(UserManagmentUtil.class);

  /**
   * overrided private constructor.
   */
  private UserManagmentUtil() {
    // intentiionally empty
  }

  /**
   * create User Fetch By Filters RequestDto.
   */
  public static UserFetchByFiltersRequestDto createUserFetchByFiltersRequestDto(
      UserFetchByFiltersRequestDto userFetchByFiltersRequestDto, List<String> roles, String createdBy,
      DateTime createdDate, ResourceType resourceType, String resourceId, OwnerType userType) {
    userFetchByFiltersRequestDto.setResourceId(resourceId);
    if (resourceType != null) {
      userFetchByFiltersRequestDto.setResourceType(resourceType.name());
    }
    userFetchByFiltersRequestDto.setCreatedBy(createdBy);
    userFetchByFiltersRequestDto.setCreatedDate(createdDate);
    userFetchByFiltersRequestDto.setRoles(roles);
    userFetchByFiltersRequestDto.setUserType(userType);
    return userFetchByFiltersRequestDto;
  }

  /**
   * create User.
   */
  public static User constructUserEntity(Optional<User> optionalUser, UserProvisioningUserResource userResponse,
      OwnerInfo ownerInfo) {
    User user = null;
    if (optionalUser.isPresent()) {
      user = optionalUser.get();
    } else {
      user = new User();
      user.setUserId(userResponse.getId());
      user.setLoginName(userResponse.getUserName());
    }
    user.setOwnerId(ownerInfo.getOwnerId());
    user.setOwnerType(ownerInfo.getOwnerType());
    user.setApplication(ownerInfo.getApplication());
    user.setUserExternalId(userResponse.getExternalId());
    user.setEmail(userResponse.getEmail());
    user.setUserFirstName(userResponse.getFirstName());
    user.setUserLastName(userResponse.getLastName());
    user.setStatus(Status.valueOf(userResponse.getStatus().toString()));
    if (userResponse.getCustomProperties() != null
        && userResponse.getCustomProperties().get(DataConstants.BUSINESS_UNIT) != null) {
      user.setBusinessUnit(userResponse.getCustomProperties().get(DataConstants.BUSINESS_UNIT).toString());
    }

    if (CollectionUtils.isNotEmpty(userResponse.getRoles())) {
      user.setRolesAssigned(userResponse.getRoles().get(0));
    }
    if (userResponse.getCustomProperties() != null
        && userResponse.getCustomProperties().get(DataConstants.DIVISION) != null) {
      user.setDivision(userResponse.getCustomProperties().get(DataConstants.DIVISION).toString());
    }
    return user;
  }

  /**
   * create UserAccessGroupMappingDao.
   */
  public static UserAccessGroupMappingDao toUserAccessGroupMappingDao(AccessGroupResponseResource accessGroupResponse,
      UserProvisioningUserResource userResponse, UserAccessGroupMappingDao userAccessGroupDao) {
    userAccessGroupDao.setAccessGroupCode(accessGroupResponse.getCode());
    userAccessGroupDao.setAccessGroupType(accessGroupResponse.getType());
    userAccessGroupDao.setUserFirstName(userResponse.getFirstName());
    userAccessGroupDao.setUserLastName(userResponse.getLastName());
    return userAccessGroupDao;
  }

  /**
   * create AccessResources.
   */
  public static AccessResources toAccessResources(AccessGroupResponseResource accessGroupResponse) {
    AccessResources accessResource = new AccessResources();
    accessResource.setId(accessGroupResponse.getMerchantId());
    accessResource.setType(accessGroupResponse.getMerchantType());
    accessResource.setAccessGroupType(accessGroupResponse.getType());
    accessResource.setAccessGroupId(accessGroupResponse.getCode());
    return accessResource;
  }

  /**
   * logout from Application.
   */
  public static void logoutUserFromAllActiveSessions(IdentityManagementFeignClient identityManagementFeignClient,
      String userId, String application) {
    IdentityManagementUserResource identityManagementUser =
        identityManagementFeignClient.getUser(userId, application).getBody();
    LogoutRequestResource logoutRequestResource = new LogoutRequestResource();
    logoutRequestResource.setUsername(identityManagementUser.getUserName());
    logoutRequestResource.setApplicationName(LOGOUT_APPLICATION_NAME);
    logoutRequestResource.setBrand(LOGOUT_BRAND);
    logoutRequestResource.setUserId(identityManagementUser.getExternalId());
    logoutRequestResource.setInternalId(identityManagementUser.getId());
    identityManagementFeignClient.internalLogout(logoutRequestResource);
  }

  /**
   * Method to construct Identity Management update user resource.
   */
  public static IdentityManagementUpdateUserResource constructIdentityManagementUpdateUserResource(
      UserUpdationDto userUpdationDto, List<String> rolesToAdd, List<String> accessGroupsToAdd) {
    addUpdateUserCustomProperties(userUpdationDto);
    IdentityManagementUpdateUserResource updateUserResource = new IdentityManagementUpdateUserResource();
    BeanUtils.copyProperties(userUpdationDto, updateUserResource);
    List<UpdateActionResource> roles = new ArrayList<>();
    List<UpdateActionResource> accessGroups = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(rolesToAdd)) {
      roles.addAll(rolesToAdd.stream().map(roleName -> new UpdateActionResource(roleName, Action.ADD))
          .collect(Collectors.toList()));
    }
    if (CollectionUtils.isNotEmpty(userUpdationDto.getRolesToDelete())) {
      roles.addAll(userUpdationDto.getRolesToDelete().stream()
          .map(name -> new UpdateActionResource(name, Action.DELETE)).collect(Collectors.toList()));
    }
    if (CollectionUtils.isNotEmpty(accessGroupsToAdd)) {
      accessGroups.addAll(
          accessGroupsToAdd.stream().map(id -> new UpdateActionResource(id, Action.ADD)).collect(Collectors.toList()));
    }
    if (CollectionUtils.isNotEmpty(userUpdationDto.getAccessGroupsToDelete())) {
      accessGroups.addAll(userUpdationDto.getAccessGroupsToDelete().stream()
          .map(name -> new UpdateActionResource(name, Action.DELETE)).collect(Collectors.toList()));
    }
    if (CollectionUtils.isNotEmpty(userUpdationDto.getAccessGroupsToHardDelete())) {
      accessGroups.addAll(userUpdationDto.getAccessGroupsToHardDelete().stream()
          .map(name -> new UpdateActionResource(name, Action.DELETE)).collect(Collectors.toList()));
    }
    updateUserResource.setRoles(roles);
    updateUserResource.setAccessGroups(accessGroups);
    return updateUserResource;
  }

  private static void addUpdateUserCustomProperties(UserUpdationDto userUpdationDto) {
    if (userUpdationDto.getCustomProperties() == null) {
      userUpdationDto.setCustomProperties(new HashMap<>());
    }
    if (StringUtils.equals(DataConstants.PORTAL, userUpdationDto.getApplicationName())
        || StringUtils.equals(DataConstants.PARTNER_PORTAL, userUpdationDto.getApplicationName())) {
      checkAndAddUserOwnerFieldsToCustomProperties(userUpdationDto);
    }
    if (userUpdationDto.getBusinessUnit() != null) {
      userUpdationDto.getCustomProperties().put(DataConstants.BUSINESS_UNIT, userUpdationDto.getBusinessUnit());
    }
    if (CollectionUtils.isNotEmpty(userUpdationDto.getUserAssignedApplications())) {
      userUpdationDto.getCustomProperties().put(DataConstants.USER_ASSIGNED_APPLICATIONS,
          userUpdationDto.getUserAssignedApplications());
    }
  }

  private static void checkAndAddUserOwnerFieldsToCustomProperties(UserUpdationDto userUpdationDto) {
    if (StringUtils.isNotEmpty(userUpdationDto.getOwnerId())
        && StringUtils.isNotEmpty(userUpdationDto.getOwnerType())) {
      userUpdationDto.getCustomProperties().put(DataConstants.OWNER_ID, userUpdationDto.getOwnerId());
      userUpdationDto.getCustomProperties().put(DataConstants.OWNER_TYPE, userUpdationDto.getOwnerType());
    }
  }

  /**
   * This methods sets the ADMIN role based on businessUnit and role check for create user.
   */
  public static void validateAndSetBrandRoleCreateUser(List<AccessResources> accessResources, String businessUnit,
      SkrillTellerConfig skrillTellerConfig) {
    for (AccessResources accessResource : accessResources) {
      if (businessUnit != null && StringUtils.equals(accessResource.getRole(), DataConstants.ADMIN)) {
        accessResource.setRole(skrillTellerConfig.getBusinessUnits().get(businessUnit.toLowerCase()).getAdminRole());
      }
    }
  }

  /**
   * This methods sets the ADMIN role based on businessUnit, role and action check for update user.
   */
  public static void validateAndSetBrandRoleUpdateUser(List<UpdateUserAccessResources> accessResources,
      String businessUnit, SkrillTellerConfig skrillTellerConfig) {
    for (UpdateUserAccessResources accessResource : accessResources) {
      if (businessUnit != null && Action.ADD.equals(accessResource.getAction())
          && StringUtils.equals(accessResource.getRole(), DataConstants.ADMIN)) {
        accessResource.setRole(skrillTellerConfig.getBusinessUnits().get(businessUnit.toLowerCase()).getAdminRole());
      }
    }
  }

  /**
   * This method updates the accesResource role, If the accessRole in AccessGroupResponseResource is not null.
   */
  public static void checkAndUpdateAccessRole(AccessResources accessResource,
      AccessGroupResponseResource accessGroupResponse) {
    String accessRole = accessGroupResponse.getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights()
        .get(0).getAccessRight().getAccessRole();
    if (StringUtils.isNotEmpty(accessRole)) {
      accessResource.setRole(accessRole);
    }
  }

  /**
   * This method filer and gives the walletIds of skrill, neteller users.
   */
  public static Set<String> getSkrillTellerWalletIds(List<UserResponseResource> userResourceList) {
    return userResourceList.stream()
        .filter(
            userResponseResource -> (userResponseResource.getCustomProperties().get(DataConstants.BUSINESS_UNIT) == null
                || userResponseResource.getCustomProperties().get(DataConstants.BUSINESS_UNIT).toString()
                .equalsIgnoreCase(DataConstants.SKRILL)
                || userResponseResource.getCustomProperties().get(DataConstants.BUSINESS_UNIT).toString()
                .equalsIgnoreCase(DataConstants.NETELLER)))
        .flatMap(userResponseResource -> userResponseResource.getAccessResources().stream()).map(AccessResources::getId)
        .collect(Collectors.toSet());
  }

  /**
   * This method filer and gives the walletIds of skrill linkedBrands(binance/ftx).
   */
  public static Set<String> getWalletIdsByBusinessUnit(List<UserResponseResource> userResourceList,
      String skrillLinkedBrand) {
    return userResourceList.stream()
        .filter(
            userResponseResource -> (userResponseResource.getCustomProperties().get(DataConstants.BUSINESS_UNIT) != null
                && userResponseResource.getCustomProperties().get(DataConstants.BUSINESS_UNIT).toString()
                .equalsIgnoreCase(skrillLinkedBrand)))
        .flatMap(userResponseResource -> userResponseResource.getAccessResources().stream()).map(AccessResources::getId)
        .collect(Collectors.toSet());
  }

  /**
   * this methods forms the AccountGroupMerchantSearchResponse list structure from accountGroupsCount map.
   */
  public static List<AccountGroupMerchantSearchResponse> formAccountGroupsListResponse(
      Map<String, Integer> accountGroupsCountMap) {
    List<AccountGroupMerchantSearchResponse> accountGroupListResponse = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : accountGroupsCountMap.entrySet()) {
      AccountGroupMerchantSearchResponse accountGroupMerchantSearchResponse = new AccountGroupMerchantSearchResponse();
      accountGroupMerchantSearchResponse.setAccountGroupId(entry.getKey());
      accountGroupMerchantSearchResponse.setTotalCount(entry.getValue());
      accountGroupListResponse.add(accountGroupMerchantSearchResponse);
    }
    return accountGroupListResponse;
  }

  /**
   * This method computes the businessUnit value based on the currentBusinessUnit, updatedBusinessUnit values.
   */
  public static String getBusinessUnit(String updatedBusinessUnit, String currentBusinessUnit) {
    String businessUnit = null;
    if (StringUtils.isNotEmpty(updatedBusinessUnit)) {
      businessUnit = updatedBusinessUnit;
    } else if (StringUtils.isEmpty(currentBusinessUnit)) {
      businessUnit = StringUtils.lowerCase(CommonThreadLocal.getAuthLocal().getBusinessUnit());
    }
    return StringUtils.lowerCase(businessUnit);
  }

  /**
   * This method computes the updated user OwnerInfo object.
   */
  public static OwnerInfo getUpdatedUserOwnerInfo(IdentityManagementUserResource idmResponse) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    return new OwnerInfo(getUserCustomPropertiesField(idmResponse.getCustomProperties(), DataConstants.OWNER_ID),
        getUserCustomPropertiesField(idmResponse.getCustomProperties(), DataConstants.OWNER_TYPE), application);
  }

  /**
   * This method computes the user customProperties field, based on request.
   */
  public static String getUserCustomPropertiesField(Map<String, Object> customProperties, String field) {
    if (customProperties == null) {
      return null;
    }
    return customProperties.get(field) != null ? customProperties.get(field).toString() : null;
  }

  /**
   * This method gives the userName from CommonThreadLocal.
   */
  public static String getUserNameFromThreadLocal() {
    if (CommonThreadLocal.getAuthLocal() == null) {
      return "SYSTEM";
    } else {
      return StringUtils.isEmpty(CommonThreadLocal.getAuthLocal().getUserName()) ? "SYSTEM"
          : CommonThreadLocal.getAuthLocal().getUserName();
    }
  }

  /**
   * This method creates the objectNode from input fro audit event.
   */
  public static String contructEventData(String reason, String details) throws JsonProcessingException {
    ObjectNode auditEventDataObjectNode = objectMapper.createObjectNode();
    auditEventDataObjectNode.put("reason", reason);
    if (details != null) {
      auditEventDataObjectNode.put("details", details);
    }
    return objectMapper.writeValueAsString(auditEventDataObjectNode);
  }

  /**
   * This method gives the UserAssignedApplications List from input list.
   */
  public static List<UserAssignedApplications> getAssignedApplicationsCreateFlow(List<String> userAssignedApplications,
      User user) {
    List<UserAssignedApplications> userAssignedApps = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(userAssignedApplications)) {
      for (String userApplication : userAssignedApplications) {
        UserAssignedApplications userApp = new UserAssignedApplications();
        userApp.setApplication(userApplication);
        userApp.setUserId(user.getUserId());
        userAssignedApps.add(userApp);
      }
    }
    if (CollectionUtils.isEmpty(userAssignedApplications)) {
      UserAssignedApplications userApp = new UserAssignedApplications();
      userApp.setApplication(CommonThreadLocal.getAuthLocal().getApplication());
      userApp.setUserId(user.getUserId());
      userAssignedApps.add(userApp);
    }
    return userAssignedApps;
  }

  /**
   * validateCustomRoles.
   */
  public static void validateCustomRoles(List<String> customRoles,
      List<String> existingRoleNamesListFromPermissionService) {
    List<String> intersectionList = ListUtils.intersection(customRoles, existingRoleNamesListFromPermissionService);
    if (CollectionUtils.isNotEmpty(intersectionList)) {
      LOGGER.error("Request contains existing roles, that needs to be created: {}.", intersectionList.toString());
      throw BadRequestException.builder()
          .details("Request contains existing roles," + " that needs to be created: " + intersectionList.toString())
          .build();
    }
  }
}
