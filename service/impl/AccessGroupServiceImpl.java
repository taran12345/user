// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.config.UserMigrationConfig;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingKey;
import com.paysafe.upf.user.provisioning.domain.WalletPermission;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.Action;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.feignclients.AccessGroupFeignClient;
import com.paysafe.upf.user.provisioning.model.FilteredAccessResources;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.WalletPermissionRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AccessGroupService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomAccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.PermissionDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.ResourceUsersValidationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupPolicy;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupsListRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicy;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicyCreateRequest;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicyRight;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicyUpdateRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessRightResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserAccessResources;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class AccessGroupServiceImpl implements AccessGroupService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AccessGroupServiceImpl.class);

  private static final String BP_ADMIN = "BP_ADMIN";
  private static final String FULL_ACCESS = "FULL_ACCESS";

  @Autowired
  private AccessGroupFeignClient accessGroupFeignClient;

  @Autowired
  private UserProvisioningUtils userProvisioningUtils;

  @Autowired
  private UserService userService;

  @Autowired
  private WalletPermissionRepository walletPermissionRepository;

  @Autowired
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Autowired
  private UserMigrationConfig userMigrationConfig;

  private String userId;
  private String firstName;
  private String lastName;

  @Override
  public List<AccessResources> createAccessGroupsFromResouresList(UserDto userDto) {
    for (AccessResources accessResource : userDto.getAccessResources()) {
      if (StringUtils.isEmpty(accessResource.getAccessGroupId())) {
        if (StringUtils.contains(accessResource.getRole(), DataConstants.ADMIN)) {
          verifyUserCount(accessResource, AccessGroupType.DEFAULT_ADMIN, userDto.getApplicationName());
          if (!userDto.getRoleDto().getExistingRoles().contains(BP_ADMIN)) {
            userDto.getRoleDto().getExistingRoles().add(BP_ADMIN);
          }
          setAdminAccessRights(accessResource);
          accessResource.setAccessGroupId(getAdminAccessGroup(accessResource).getCode());
          accessResource.setAccessGroupType(AccessGroupType.DEFAULT_ADMIN);
        } else {
          verifyUserCount(accessResource, AccessGroupType.CUSTOMIZED, userDto.getApplicationName());
          accessResource.setAccessGroupId(createCustomAccessGroup(accessResource).getCode());
          accessResource.setAccessGroupType(AccessGroupType.CUSTOMIZED);
        }
      }
    }
    return userDto.getAccessResources();
  }

  @Override
  public void createAccessGroupsForUpdateUser(String userId, UserUpdationDto userUpdationDto) {
    List<UpdateUserAccessResources> accessResources = userUpdationDto.getAccessResources();
    if (CollectionUtils.isNotEmpty(accessResources)) {
      userProvisioningUtils.deleteDuplicateAccessResourcesFromRequest(accessResources);
      manageAccessGroups(userUpdationDto, userId);
      mapAccessResourcesToUpdateUserRequest(userUpdationDto);
    }
  }

  private AccessGroupResponseResource createCustomAccessGroup(AccessResources accessResource) {
    String randomString = RandomStringUtils.random(6, true, true);
    String accessPolicyName = accessResource.getId() + "_" + randomString + "_POLICY";
    String accessPolicyId = createAccessPolicy(accessResource, accessPolicyName);
    String accessGroupName =
        removeSpecialCharsFromAccountId(accessResource.getId()) + "_" + randomString + "_CUSTOM_GROUP";
    String accessGroupDescription = accessResource.getId() + "_CUSTOM_GROUP Description";
    return createAccessGroup(accessResource, accessGroupName, accessGroupDescription, Arrays.asList(accessPolicyId),
        AccessGroupType.CUSTOMIZED);
  }

  private String removeSpecialCharsFromAccountId(String id) {
    String specialChars = "[^a-zA-Z0-9]";
    if (id != null) {
      return id.replaceAll(specialChars, "");
    }
    return id;
  }

  private AccessGroupResponseResource createAccessGroup(AccessResources accessResource, String accessGroupName,
      String accessGroupDescription, List<String> policyIds, AccessGroupType accessGroupType) {
    return accessGroupFeignClient.createAccessGroup(constructCustomAccessGroupDto(accessResource, policyIds,
        accessGroupName, accessGroupDescription, accessGroupType)).getBody();
  }

  private CustomAccessGroupDto constructCustomAccessGroupDto(AccessResources accessResource, List<String> policyIds,
      String accessGroupName, String accessGroupDescription, AccessGroupType accessGroupType) {
    CustomAccessGroupDto customAccessGroupDto = new CustomAccessGroupDto();
    customAccessGroupDto.setAccessPolicyIds(policyIds);
    customAccessGroupDto.setName(accessGroupName);
    customAccessGroupDto.setDescription(accessGroupDescription);
    customAccessGroupDto.setStatus(Status.ACTIVE);
    customAccessGroupDto.setType(accessGroupType);
    if (StringUtils.isNotEmpty(accessResource.getOwnerId())) {
      customAccessGroupDto.setOwnerId(accessResource.getOwnerId());
      customAccessGroupDto.setMerchantId(accessResource.getOwnerId());
    } else {
      customAccessGroupDto.setMerchantId(accessResource.getId());
    }
    if (StringUtils.isNotEmpty(accessResource.getOwnerType())) {
      customAccessGroupDto.setOwnerType(accessResource.getOwnerType());
      customAccessGroupDto.setMerchantType(accessResource.getOwnerType());
    } else {
      customAccessGroupDto.setMerchantType(accessResource.getType());
    }
    return customAccessGroupDto;
  }

  private String createAccessPolicy(AccessResources accessResource, String accessPolicyName) {
    AccessPolicy accessPolicy = accessGroupFeignClient
        .createAccessPolicy(constructAccessPolicyCreationResource(accessResource, accessPolicyName)).getBody();
    return accessPolicy.getCode();
  }

  private AccessPolicyCreateRequest constructAccessPolicyCreationResource(AccessResources accessResource,
      String accessPolicyName) {
    AccessPolicyCreateRequest accessPolicyCreateRequest = new AccessPolicyCreateRequest();
    accessPolicyCreateRequest.setName(accessPolicyName);
    List<AccessRightResource> accessRightResources = new ArrayList<>();
    List<String> resourceIds = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(accessResource.getIds())) {
      resourceIds.addAll(accessResource.getIds());
    } else {
      resourceIds.add(accessResource.getId());
    }
    for (String resourceId : resourceIds) {
      if (accessResource.getRole() != null && !StringUtils.equals(accessResource.getRole(), DataConstants.REGULAR)) {
        accessRightResources.add(
            new AccessRightResource(accessResource.getType(), resourceId, null, "ACTIVE", accessResource.getRole()));
      } else {
        for (PermissionDto permission : accessResource.getPermissions()) {
          accessRightResources.add(getAccessRightResource(accessResource, resourceId, permission));
        }
      }
    }
    accessPolicyCreateRequest.setAccessRights(accessRightResources);
    return accessPolicyCreateRequest;
  }

  private AccessRightResource getAccessRightResource(AccessResources accessResource, String resourceId,
      PermissionDto permissionDto) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    if (StringUtils.equals(DataConstants.SKRILL, application)
        || StringUtils.equals(DataConstants.NETELLER, application)) {
      String permission = getSkrillPermissionFromId(permissionDto);
      return new AccessRightResource(accessResource.getType(), resourceId, permission, "ACTIVE", null);
    } else {
      return new AccessRightResource(accessResource.getType(), resourceId, permissionDto.getLabel(), "ACTIVE", null);
    }
  }

  private String getSkrillPermissionFromId(PermissionDto permissionDto) {
    Optional<WalletPermission> optionalWalletPermission = walletPermissionRepository.findById(permissionDto.getId());
    if (optionalWalletPermission.isPresent()) {
      return optionalWalletPermission.get().getPermission();
    } else {
      throw BadRequestException.builder()
          .details("Unable to find the walletPermission for Id :" + permissionDto.getId()).build();
    }
  }

  private void setAdminAccessRights(AccessResources accessResource) {
    List<PermissionDto> permissions = new ArrayList<>();
    PermissionDto permission = new PermissionDto();
    permission.setLabel(FULL_ACCESS);
    permissions.add(permission);
    accessResource.setPermissions(permissions);
  }

  private AccessGroupResponseResource getAdminAccessGroup(AccessResources accessResource) {
    List<AccessGroupResponseResource> accessGroupDtoList =
        accessGroupFeignClient.fetchAccessGroups(accessResource.getId(), accessResource.getType(),
            AccessGroupType.DEFAULT_ADMIN, null, null, null, null, null).getItems();
    AccessGroupResponseResource adminAccessGroup;
    if (accessGroupDtoList != null && !accessGroupDtoList.isEmpty()) {
      adminAccessGroup = accessGroupDtoList.get(0);
    } else {
      adminAccessGroup = createAdminAccessGroup(accessResource);
    }
    return adminAccessGroup;
  }

  private AccessGroupResponseResource createAdminAccessGroup(AccessResources accessResource) {
    if (StringUtils.isEmpty(accessResource.getId())) {
      return createCustomAccessGroup(accessResource);
    }
    String accessPolicyName = accessResource.getId() + "_POLICY";
    String accessPolicyId = createAccessPolicy(accessResource, accessPolicyName);
    String accessGroupName = removeSpecialCharsFromAccountId(accessResource.getId()) + "_DEFAULT_GROUP";
    String accessGroupDescription = accessResource.getId() + "_DEFAULT_GROUP Description";
    return createAccessGroup(accessResource, accessGroupName, accessGroupDescription, Arrays.asList(accessPolicyId),
        AccessGroupType.DEFAULT_ADMIN);
  }

  private void verifyUserCount(AccessResources accessResources, AccessGroupType accessGroupType, String application) {
    List<String> resourceIds = new ArrayList<>();
    if (StringUtils.isEmpty(accessResources.getId())) {
      resourceIds.addAll(accessResources.getIds());
    } else {
      resourceIds.add(accessResources.getId());
    }
    for (String resourceId : resourceIds) {
      ResourceUsersValidationDto resourceUsersValidationDto =
          userProvisioningUtils.verifyUserCountforResource(resourceId, accessResources.getType(), application);
      if ((accessGroupType.equals(AccessGroupType.DEFAULT_ADMIN) && resourceUsersValidationDto.getCanAddAdminUsers())
          || (accessGroupType.equals(AccessGroupType.CUSTOMIZED) && resourceUsersValidationDto.getCanAddUsers())) {
        continue;
      }
      throw BadRequestException.builder().details("Maximum users limit reached for wallet :" + resourceId).build();
    }
  }

  private void manageAccessGroups(UserUpdationDto userUpdationDto, String uuid) {
    IdentityManagementUserResource userResource = userService.fetchUser(uuid);
    userId = userResource.getId();
    firstName = userUpdationDto.getFirstName();
    lastName = userUpdationDto.getLastName();
    List<AccessGroupResponseResource> fetchedAccessGroups = getAccessGroupsDtoList(userResource.getAccessGroups());
    if (StringUtils.equals(userUpdationDto.getApplicationName(), DataConstants.SKRILL)
        || StringUtils.equals(userUpdationDto.getApplicationName(), DataConstants.NETELLER)) {
      userProvisioningUtils.validateAccessResources(userUpdationDto, fetchedAccessGroups);
    }
    FilteredAccessResources filteredAccessResources =
        deleteAlreadyExistingAccessGroups(fetchedAccessGroups, userUpdationDto);
    List<UpdateUserAccessResources> deletedAccessResources = filteredAccessResources.getDeletedAccessResources();
    userUpdationDto.getAccessResources().removeAll(filteredAccessResources.getModifiedAccessResources());
    for (UpdateUserAccessResources accessResource : userUpdationDto.getAccessResources()) {
      AccessGroupResponseResource accessGroup = null;
      if (StringUtils.isEmpty(accessResource.getAccessGroupId()) && accessResource.getAction().equals(Action.ADD)) {
        verifyAndCreateAccessGroup(accessResource);
      } else if (!StringUtils.isEmpty(accessResource.getAccessGroupId())
          && accessResource.getAction().equals(Action.ADD)) {
        accessGroup = accessGroupFeignClient.fetchAccessGroupByCode(accessResource.getAccessGroupId()).getBody();
        if (accessGroup == null) {
          throw BadRequestException.builder().errorCode(CommonErrorCode.INVALID_FIELD).details("accessGroup not found")
              .build();
        }
        if (StringUtils.equals(accessResource.getRole(), DataConstants.ADMIN)
            && !AccessGroupType.DEFAULT_ADMIN.equals(accessGroup.getType())) {
          setAdminAccessRights(accessResource);
          String accessGroupId = getAdminAccessGroup(accessResource).getCode();
          accessResource.setAccessGroupId(accessGroupId);
          deletedAccessResources.add(getAccessResource(accessGroup.getCode(), Action.DELETE));
        } else if (StringUtils.equals(accessResource.getRole(), DataConstants.REGULAR)
            && CollectionUtils.isNotEmpty(accessResource.getPermissions())) {
          updatePermissions(accessGroup, accessResource);
        } else {
          deletedAccessResources.add(getAccessResource(accessGroup.getCode(), Action.DELETE));
          deletedAccessResources.add(getAccessResource(createCustomAccessGroup(accessResource).getCode(), Action.ADD));
        }
      }
    }
    userUpdationDto.getAccessResources().addAll(deletedAccessResources);
  }

  private void verifyAndCreateAccessGroup(UpdateUserAccessResources accessResource) {
    if (StringUtils.equals(accessResource.getRole(), DataConstants.ADMIN)) {
      setAdminAccessRights(accessResource);
      accessResource.setAccessGroupId(getAdminAccessGroup(accessResource).getCode());
    } else if (StringUtils.equals(accessResource.getRole(), DataConstants.REGULAR)
        && CollectionUtils.isEmpty(accessResource.getPermissions())) {
      throw BadRequestException.builder().errorCode(CommonErrorCode.INVALID_FIELD)
          .details("permissions are not provided").build();
    } else {
      accessResource.setAccessGroupId(createCustomAccessGroup(accessResource).getCode());
    }
  }

  private FilteredAccessResources deleteAlreadyExistingAccessGroups(
      List<AccessGroupResponseResource> fetchedAccessGroups, UserUpdationDto userUpdationDto) {
    List<UpdateUserAccessResources> deletedAccessResources = new ArrayList<>();
    List<UpdateUserAccessResources> modifiedAccessResources = new ArrayList<>();
    for (UpdateUserAccessResources accessResource : userUpdationDto.getAccessResources()) {
      if (accessResource.getAction().equals(Action.ADD)) {
        checkAccessGroupExistsForAccessResource(fetchedAccessGroups, accessResource, deletedAccessResources,
            modifiedAccessResources);
      }
    }
    return new FilteredAccessResources(deletedAccessResources, modifiedAccessResources);
  }

  private void checkAccessGroupExistsForAccessResource(List<AccessGroupResponseResource> fetchedAccessGroups,
      UpdateUserAccessResources accessResource, List<UpdateUserAccessResources> deletedAccessResources,
      List<UpdateUserAccessResources> modifiedAccessResources) {
    for (AccessGroupResponseResource accessGroup : fetchedAccessGroups) {
      if (isSameResourceIdAndType(accessGroup, accessResource)) {
        if (StringUtils.equals(accessResource.getRole(), DataConstants.REGULAR)
            && accessGroup.getType().equals(AccessGroupType.CUSTOMIZED)) {
          updatePermissions(accessGroup, accessResource);
          modifiedAccessResources.add(accessResource);
          updateUserAccessGroupMapppingTimeStamp(accessGroup);
        } else if (isPredefinedRole(accessGroup, accessResource)) {
          handlePredefinedRoleAccessGroup(accessGroup, accessResource, deletedAccessResources, modifiedAccessResources);
        } else {
          deletedAccessResources.add(getAccessResource(accessGroup.getCode(), Action.DELETE));
        }
      }
    }
  }

  private boolean isSameResourceIdAndType(AccessGroupResponseResource accessGroup,
      UpdateUserAccessResources accessResource) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    if (StringUtils.equals(application, DataConstants.SKRILL)
        || StringUtils.equals(application, DataConstants.NETELLER)) {
      return (StringUtils.equals(accessGroup.getMerchantId(), accessResource.getId())
          && StringUtils.equals(accessGroup.getMerchantType(), accessResource.getType()));
    } else {
      return (StringUtils.equals(accessGroup.getOwnerId(), accessResource.getOwnerId())
          && StringUtils.equals(accessGroup.getOwnerType(), accessResource.getOwnerType())
          && StringUtils.equals(accessGroup.getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights()
          .get(0).getAccessRight().getResourceType(), accessResource.getType()));
    }
  }

  private void handlePredefinedRoleAccessGroup(AccessGroupResponseResource accessGroup,
      UpdateUserAccessResources accessResource, List<UpdateUserAccessResources> deletedAccessResources,
      List<UpdateUserAccessResources> modifiedAccessResources) {
    if (isSameRole(accessResource.getRole(), accessGroup) && CollectionUtils.isEmpty(accessResource.getIds())) {
      modifiedAccessResources.add(accessResource);
    } else if (StringUtils.equals(accessResource.getRole(), DataConstants.ADMIN)) {
      modifiedAccessResources.add(accessResource);
    } else {
      deletedAccessResources.add(getAccessResource(accessGroup.getCode(), Action.HARD_DELETE));
    }
  }

  private UpdateUserAccessResources getAccessResource(String accessGroupId, Action action) {
    UpdateUserAccessResources accessResource = new UpdateUserAccessResources();
    accessResource.setAccessGroupId(accessGroupId);
    accessResource.setAction(action);
    return accessResource;
  }

  private void updatePermissions(AccessGroupResponseResource accessGroup, UpdateUserAccessResources accessResource) {
    List<AccessPolicyRight> accessPolicyRightDtos =
        accessGroup.getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights();
    updateCustomAccessGroup(accessGroup, resolveAccessRightsForAccessPolicy(accessPolicyRightDtos,
        accessResource.getPermissions().stream().map(this::getPermission).collect(Collectors.toSet()), accessResource));
  }

  private String getPermission(PermissionDto permissionDto) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    if (StringUtils.equals(DataConstants.SKRILL, application)
        || StringUtils.equals(DataConstants.NETELLER, application)) {
      return getSkrillPermissionFromId(permissionDto);
    } else {
      return permissionDto.getLabel();
    }
  }

  private AccessPolicyUpdateRequestResource resolveAccessRightsForAccessPolicy(
      List<AccessPolicyRight> existingAccessPolicyRights, Set<String> updatedAccessRights,
      UpdateUserAccessResources accessResources) {

    List<AccessRightResource> accessRightResources = new ArrayList<>();
    List<String> accessRightsToBeDeleted = new ArrayList<>();

    List<String> resourceIds = new ArrayList<>();
    if (StringUtils.isNotEmpty(accessResources.getId())) {
      resourceIds.add(accessResources.getId());
    } else {
      resourceIds.addAll(accessResources.getIds());
    }

    for (AccessPolicyRight accessPolicyRight : existingAccessPolicyRights) {
      if ((!updatedAccessRights.contains(accessPolicyRight.getAccessRight().getAccessTypeValue())
          || !resourceIds.contains(accessPolicyRight.getAccessRight().getResourceId()))
          && !isUnMappedPermission(accessPolicyRight.getAccessRight().getAccessTypeValue())) {
        accessRightsToBeDeleted.add(accessPolicyRight.getAccessRight().getCode());
      }
    }
    for (String accessRight : updatedAccessRights) {
      for (String resourceId : resourceIds) {
        accessRightResources
            .add(new AccessRightResource(accessResources.getType(), resourceId, accessRight, "ACTIVE", null));
      }
    }

    return new AccessPolicyUpdateRequestResource(accessRightResources, accessRightsToBeDeleted);
  }

  /**
   * This method checks for the Netbanx users Un-Mapped permissions using configuration.
   */
  private boolean isUnMappedPermission(String accessTypeValue) {
    boolean isUnMappedPermission = false;
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    if (StringUtils.equals(application, DataConstants.PORTAL)
        || StringUtils.equals(application, DataConstants.PARTNER_PORTAL)) {
      Set<String> unMappedPermissions = userMigrationConfig.getNetbanxUnMappedPermissions();
      if (unMappedPermissions.contains(accessTypeValue)) {
        isUnMappedPermission = true;
      }
    }
    return isUnMappedPermission;
  }

  private void updateCustomAccessGroup(AccessGroupResponseResource accessGroup,
      AccessPolicyUpdateRequestResource accessPolicyUpdateRequestResource) {
    updateAccessPolicy(accessGroup.getAccessGroupPolicies().get(0), accessPolicyUpdateRequestResource);
  }

  private void updateAccessPolicy(AccessGroupPolicy accessGroupPolicy,
      AccessPolicyUpdateRequestResource accessPolicyUpdateRequestResource) {
    AccessPolicy updatedAccessPolicy = accessGroupFeignClient
        .updateAccessPolicy(accessGroupPolicy.getAcessPolicy().getCode(), accessPolicyUpdateRequestResource);
    LOGGER.info("Access policy with code {} updated with new access rights", updatedAccessPolicy.getCode());
  }

  private void mapAccessResourcesToUpdateUserRequest(UserUpdationDto userUpdationDto) {

    List<String> accessGroupsToAdd = new ArrayList<>();
    List<String> hardDeletedAccessGroups = new ArrayList<>();
    List<String> deletedAccessGroups = new ArrayList<>();
    for (UpdateUserAccessResources accessResouce : userUpdationDto.getAccessResources()) {
      if (accessResouce.getAction().equals(Action.ADD)) {
        accessGroupsToAdd.add(accessResouce.getAccessGroupId());
      } else if (accessResouce.getAction().equals(Action.HARD_DELETE)) {
        hardDeletedAccessGroups.add(accessResouce.getAccessGroupId());
      } else {
        deletedAccessGroups.add(accessResouce.getAccessGroupId());
      }
    }
    if (userUpdationDto.getAccessGroupsToAdd() != null) {
      userUpdationDto.getAccessGroupsToAdd().getExistingAccessGroupIds().addAll(accessGroupsToAdd);
    }
    if (userUpdationDto.getAccessGroupsToHardDelete() != null) {
      userUpdationDto.getAccessGroupsToHardDelete().addAll(hardDeletedAccessGroups);
    }
    if (userUpdationDto.getAccessGroupsToDelete() != null) {
      userUpdationDto.getAccessGroupsToDelete().addAll(deletedAccessGroups);
    }
  }

  private List<AccessGroupResponseResource> getAccessGroupsDtoList(List<String> accessGroupIds) {
    return accessGroupIds == null ? new ArrayList<>()
        : accessGroupFeignClient.getAccessGroupsFromInputList(new AccessGroupsListRequestResource((accessGroupIds)));
  }

  private boolean isPredefinedRole(AccessGroupResponseResource accessGroup, UpdateUserAccessResources accessResource) {
    Predicate<UpdateUserAccessResources> accessResourcePredicate =
        ar -> (StringUtils.equals(ar.getRole(), DataConstants.ADMIN)
            && accessGroup.getType().equals(AccessGroupType.DEFAULT_ADMIN))
            || (StringUtils.equals(ar.getRole(), DataConstants.BP_ADMIN)
            && accessGroup.getType().equals(AccessGroupType.CUSTOMIZED))
            || (StringUtils.equals(ar.getRole(), DataConstants.BP_EU_ADMIN)
            && accessGroup.getType().equals(AccessGroupType.CUSTOMIZED))
            || (StringUtils.equals(ar.getRole(), DataConstants.BP_DEVELOPER)
            && accessGroup.getType().equals(AccessGroupType.CUSTOMIZED))
            || (StringUtils.equals(ar.getRole(), DataConstants.BP_BUSINESS)
            && accessGroup.getType().equals(AccessGroupType.CUSTOMIZED))
            || (StringUtils.equals(ar.getRole(), DataConstants.BP_OPERATION)
            && accessGroup.getType().equals(AccessGroupType.CUSTOMIZED))
            || (StringUtils.equals(ar.getRole(), DataConstants.BP_ISV_ADMIN)
            && accessGroup.getType().equals(AccessGroupType.CUSTOMIZED));
    return accessResourcePredicate.test(accessResource);
  }

  private boolean isSameRole(String role, AccessGroupResponseResource accessGroup) {
    String accessPloicyRole = accessGroup.getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights()
        .get(0).getAccessRight().getAccessRole();
    return StringUtils.equals(accessPloicyRole, role);
  }

  private void updateUserAccessGroupMapppingTimeStamp(AccessGroupResponseResource accessGroup) {
    UserAccessGroupMappingKey accessGroupKey =
        new UserAccessGroupMappingKey(userId, accessGroup.getMerchantId(), accessGroup.getMerchantType());
    Optional<UserAccessGroupMappingDao> optionalUserAccessGroupDao =
        userAccessGroupMapppingRepository.findById(accessGroupKey);
    if (optionalUserAccessGroupDao.isPresent()) {
      UserAccessGroupMappingDao userAccessGroupDao = optionalUserAccessGroupDao.get();
      userAccessGroupDao.setLastModifiedDate(new DateTime());
      if (StringUtils.isNotEmpty(firstName)) {
        userAccessGroupDao.setUserFirstName(firstName);
      }
      if (StringUtils.isNotEmpty(lastName)) {
        userAccessGroupDao.setUserLastName(lastName);
      }
      userAccessGroupMapppingRepository.save(userAccessGroupDao);
    }
  }
}
