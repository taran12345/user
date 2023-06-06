// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.assembler;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.domain.AuditUserEvent;
import com.paysafe.upf.user.provisioning.domain.HashedPasswordEntity;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.enums.SkrillPermissions;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomAccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomRoleDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.PermissionDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.RoleDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.SkrillTellerMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.SkrillTellerUserResponseDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserPasswordMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupUpdationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.CustomAccessGroupResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.CustomRoleResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.MigrationAccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.OktaEventHookResource.Client;
import com.paysafe.upf.user.provisioning.web.rest.resource.OktaEventHookResource.Event;
import com.paysafe.upf.user.provisioning.web.rest.resource.OktaEventHookResource.IpChain;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.RoleUpdationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserPasswordMigrationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserUpdationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.HashedPassword;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Profile;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.SkrillAccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.SkrillTellerUserResponseResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class UserAssembler {

  @Autowired
  UserProvisioningUtils userProvisioningUtils;

  private static final Logger logger = LoggerFactory.getLogger(UserAssembler.class);
  private static final String PENDING_USER_ACTION_EVENT_REASON = "User tried login in pending state";
  private static final String PENDING_USER_ACTION_EVENT_REASON_OKTA = "GENERAL_NONSUCCESS";
  private ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Converts {@link UserResource} to {@link UserDto}.
   *
   * @param userResource {@link UserResource}
   * @return {@link UserDto}
   */
  public UserDto toUserCreationDto(UserResource userResource) {
    UserDto userDto = new UserDto();
    if (userResource != null) {
      BeanUtils.copyProperties(userResource, userDto);
      if (StringUtils.isNotEmpty(userResource.getUserName())) {
        userDto.setUserName(userResource.getUserName().toLowerCase(Locale.ENGLISH));
      }
      if (StringUtils.isNotEmpty(userResource.getEmail())) {
        userDto.setEmail(userResource.getEmail().toLowerCase(Locale.ENGLISH));
      }
      if (userResource.getIsMailNotificationsEnabled() == null) {
        userDto.setIsMailNotificationsEnabled(true);
      }
      userDto.setCustomProperties(userResource.getCustomProperties());
      processRoles(userResource, userDto);
      processAccessGroups(userResource, userDto);
    }
    return userDto;
  }

  /**
   * Converts {@link UserUpdationResource} to {@link UserUpdationDto}.
   *
   * @param userUpdationResource {@link UserUpdationResource}
   * @return {@link UserUpdationDto}
   */
  public UserUpdationDto toUpdationDto(UserUpdationResource userUpdationResource) {
    UserUpdationDto userUpdationDto = new UserUpdationDto();
    if (userUpdationResource != null) {
      BeanUtils.copyProperties(userUpdationResource, userUpdationDto);
      userUpdationDto.setCustomProperties(userUpdationResource.getCustomProperties());
      processRoleActionResources(userUpdationResource, userUpdationDto);
      processAccessGroupActionResources(userUpdationResource, userUpdationDto);
    }
    return userUpdationDto;
  }

  private void processRoles(UserResource userResource, UserDto userDto) {
    RoleDto roleDto = new RoleDto();
    roleDto.setExistingRoles(
        CollectionUtils.isNotEmpty(userResource.getRoleIds()) ? userResource.getRoleIds() : new ArrayList<>());
    roleDto.setCustomRoles(new ArrayList<>());

    if (CollectionUtils.isNotEmpty(userResource.getRoles())) {
      for (CustomRoleResource customRole : userResource.getRoles()) {
        CustomRoleDto customRoleDto = new CustomRoleDto();
        customRoleDto.setRoleName(StringUtils.isEmpty(customRole.getRoleName())
            ? userProvisioningUtils.generateRandomRoleString() : customRole.getRoleName());
        if (CollectionUtils.isNotEmpty(customRole.getPermissionList())) {
          customRoleDto.setPermissionList(customRole.getPermissionList());
        } else {
          logger.error("Permissions not given in custom role creation");
          throw BadRequestException.builder().details("Permissions not given in custom role creation").build();
        }
        roleDto.getCustomRoles().add(customRoleDto);
      }
    }
    userDto.setRoleDto(roleDto);
  }

  private void processAccessGroups(UserResource userResource, UserDto userDto) {
    AccessGroupDto accessGroupDto = new AccessGroupDto();
    accessGroupDto.setExistingAccessGroupIds(CollectionUtils.isNotEmpty(userResource.getAccessGroupsIds())
        ? userResource.getAccessGroupsIds() : new ArrayList<>());
    accessGroupDto.setCustomAccessGroupDtos(new ArrayList<>());
    if (CollectionUtils.isNotEmpty(userResource.getAccessGroups())) {
      for (CustomAccessGroupResource customAccessGroupResource : userResource.getAccessGroups()) {
        if (CollectionUtils.isEmpty(customAccessGroupResource.getAccessPolicyIds())) {
          logger.error("Access policies not given in custom access group creation");
          throw BadRequestException.builder().details("Access policies not given in custom access group creation")
              .build();
        }
        CustomAccessGroupDto customAccessGroupDto = new CustomAccessGroupDto();
        BeanUtils.copyProperties(customAccessGroupResource, customAccessGroupDto);
        accessGroupDto.getCustomAccessGroupDtos().add(customAccessGroupDto);
      }
    }
    userDto.setAccessGroupDto(accessGroupDto);
  }

  private void processRoleActionResources(UserUpdationResource userUpdationResource, UserUpdationDto userUpdationDto) {
    RoleDto roleDto = new RoleDto();
    roleDto.setExistingRoles(new ArrayList<>());
    roleDto.setCustomRoles(new ArrayList<>());
    List<String> deleteRoles = new ArrayList<>();

    if (CollectionUtils.isNotEmpty(userUpdationResource.getRoles())) {
      for (RoleUpdationResource roleUpdationResource : userUpdationResource.getRoles()) {
        switch (roleUpdationResource.getAction()) {
          case ADD:
            if (roleUpdationResource.getPermissions() == null) {
              checkRoleNamePresence(roleUpdationResource);
              roleDto.getExistingRoles().add(roleUpdationResource.getRoleName());
            } else {
              CustomRoleDto customRoleDto = new CustomRoleDto();
              customRoleDto.setRoleName(
                  roleUpdationResource.getRoleName() == null || StringUtils.isEmpty(roleUpdationResource.getRoleName())
                      ? userProvisioningUtils.generateRandomRoleString() : roleUpdationResource.getRoleName());
              customRoleDto.setPermissionList(roleUpdationResource.getPermissions());
              roleDto.getCustomRoles().add(customRoleDto);
            }
            break;
          case DELETE:
            checkRoleNamePresence(roleUpdationResource);
            deleteRoles.add(roleUpdationResource.getRoleName());
            break;
          default:
        }
      }
    }
    userUpdationDto.setRolesToAdd(roleDto);
    userUpdationDto.setRolesToDelete(deleteRoles);
  }

  private void processAccessGroupActionResources(UserUpdationResource userUpdationResource,
      UserUpdationDto userUpdationDto) {
    AccessGroupDto addAccessGroups = new AccessGroupDto();
    addAccessGroups.setCustomAccessGroupDtos(new ArrayList<>());
    addAccessGroups.setExistingAccessGroupIds(new ArrayList<>());
    List<String> deleteAccessGroups = new ArrayList<>();
    List<String> hardDeleteAccessGroups = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(userUpdationResource.getAccessGroups())) {
      for (AccessGroupUpdationResource accessGroupUpdationResource : userUpdationResource.getAccessGroups()) {
        switch (accessGroupUpdationResource.getAction()) {
          case ADD:
            if (accessGroupUpdationResource.getAccessPolicyIds() == null) {
              checkAccessGroupIdPresence(accessGroupUpdationResource);
              addAccessGroups.getExistingAccessGroupIds().add(accessGroupUpdationResource.getAccessGroupId());
            } else {
              validateCustomAccessGroupObject(accessGroupUpdationResource);
              CustomAccessGroupDto customAccessGroupDto = new CustomAccessGroupDto();
              BeanUtils.copyProperties(accessGroupUpdationResource, customAccessGroupDto);
              addAccessGroups.getCustomAccessGroupDtos().add(customAccessGroupDto);
            }
            break;
          case DELETE:
            checkAccessGroupIdPresence(accessGroupUpdationResource);
            deleteAccessGroups.add(accessGroupUpdationResource.getAccessGroupId());
            break;
          case HARD_DELETE:
            checkAccessGroupIdPresence(accessGroupUpdationResource);
            hardDeleteAccessGroups.add(accessGroupUpdationResource.getAccessGroupId());
            break;
          default:
        }
      }
    }
    userUpdationDto.setAccessGroupsToAdd(addAccessGroups);
    userUpdationDto.setAccessGroupsToDelete(deleteAccessGroups);
    userUpdationDto.setAccessGroupsToHardDelete(hardDeleteAccessGroups);

  }

  private void validateCustomAccessGroupObject(AccessGroupUpdationResource accessGroupUpdationResource) {
    if (accessGroupUpdationResource.getAccessGroupId() != null) {
      logger.error("Access group id and accesspolicies both should not be given.");
      throw BadRequestException.builder().details("Access group id and accesspolicies both should not be given.")
          .build();
    }
  }

  private void checkAccessGroupIdPresence(AccessGroupUpdationResource accessGroupUpdationResource) {
    if (accessGroupUpdationResource.getAccessGroupId() == null) {
      logger.error("Access group id not given in case of existing acessgroups operation");
      throw BadRequestException.builder().details("Access group id not given in case of existing acessgroups operation")
          .build();
    }
  }

  private void checkRoleNamePresence(RoleUpdationResource roleUpdationResource) {
    if (roleUpdationResource.getRoleName() == null) {
      logger.error("Role name not given in case of pre-defined roles operation");
      throw BadRequestException.builder().details("Rolename not given in case of existing roles operation").build();
    }

  }

  /**
   * Converts UserPasswordMigrationResource to UserPasswordMigrationDto.
   *
   * @param userPasswordMigrationResource UserPasswordMigrationResource
   * @return UserPasswordMigrationDto
   */
  public UserPasswordMigrationDto toUserMigrationDto(UserPasswordMigrationResource userPasswordMigrationResource) {
    UserPasswordMigrationDto userPasswordMigrationDto = new UserPasswordMigrationDto();
    userPasswordMigrationDto.setPassword(userPasswordMigrationResource.getPassword());
    return userPasswordMigrationDto;
  }

  /**
   * Converts UserMigrationResource to UserMigrationDto.
   *
   * @param userMigrationResource UserMigrationResource
   * @return UserMigrationDto
   */
  public UserMigrationDto toUserMigrationDto(UserMigrationResource userMigrationResource) {
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    BeanUtils.copyProperties(userMigrationResource, userMigrationDto);
    return userMigrationDto;
  }

  /**
   * Converts SkrillMigrationResource to SkrillTellerMigrationDto.
   *
   * @param userMigrationResource UserMigrationResource
   * @return SkrillTellerMigrationDto
   */
  public SkrillTellerMigrationDto toSkrillTellerMigrationDto(UserMigrationResource userMigrationResource) {
    SkrillTellerMigrationDto skrillTellerMigrationDto = new SkrillTellerMigrationDto();
    BeanUtils.copyProperties(userMigrationResource, skrillTellerMigrationDto);
    return skrillTellerMigrationDto;
  }

  /**
   * Converts HashedPassword to HashedPasswordEntity.
   *
   * @param hashedPassword HashedPassword
   * @return HashedPasswordEntity
   */
  public HashedPasswordEntity toHashedPasswordEntity(HashedPassword hashedPassword) {
    HashedPasswordEntity hashedPasswordEntity = new HashedPasswordEntity();
    BeanUtils.copyProperties(hashedPassword, hashedPasswordEntity);
    return hashedPasswordEntity;
  }

  /**
   * Converts SkrillTellerUserResponseDto to SkrillTellerUserResponseResource.
   *
   * @param skrillTellerUserResponseDto SkrillTellerUserResponseDto
   * @return SkrillTellerUserResponseResource
   */
  public SkrillTellerUserResponseResource toSkrillTellerResponseResource(
      SkrillTellerUserResponseDto skrillTellerUserResponseDto) {
    SkrillTellerUserResponseResource response = new SkrillTellerUserResponseResource();
    BeanUtils.copyProperties(skrillTellerUserResponseDto, response);
    response.setAccessResources(toSkrillTellerAccessResources(skrillTellerUserResponseDto.getAccessResources()));
    return response;
  }

  /**
   * Converts UserProvisioningUserResource to SkrillTellerUserResponseResource.
   *
   * @param userProvisioningUserResource UserProvisioningUserResource
   * @return SkrillTellerUserResponseResource
   */
  public SkrillTellerUserResponseResource toSkrillTellerResponseResource(
      UserProvisioningUserResource userProvisioningUserResource) {
    SkrillTellerUserResponseResource response = new SkrillTellerUserResponseResource();
    BeanUtils.copyProperties(userProvisioningUserResource, response);
    Profile profile = new Profile();
    profile.setFirstName(userProvisioningUserResource.getFirstName());
    profile.setLastName(userProvisioningUserResource.getLastName());
    response.setProfile(profile);
    return response;
  }

  /**
   * Converts SkrillTellerUserResponseDto to SkrillTellerUserResponseResource.
   *
   * @param skrillTellerUserResponseDtoList SkrillTellerUserResponseDto
   * @return SkrillTellerUserResponseResource
   */
  public List<SkrillTellerUserResponseResource> toSkrillTellerResponseResourceList(
      List<SkrillTellerUserResponseDto> skrillTellerUserResponseDtoList) {
    List<SkrillTellerUserResponseResource> responseList = new ArrayList<>();
    if (CollectionUtils.isEmpty(skrillTellerUserResponseDtoList)) {
      return responseList;
    }
    for (SkrillTellerUserResponseDto skrillTellerUserResponseDto : skrillTellerUserResponseDtoList) {
      responseList.add(toSkrillTellerResponseResource(skrillTellerUserResponseDto));
    }
    return responseList;
  }

  /**
   * Converts IdentityManagementUserResource to SkrillTellerUserResponseDto.
   *
   * @param idmResource IdentityManagementUserResource
   * @return SkrillTellerUserResponseDto
   */
  public SkrillTellerUserResponseDto toSkrillTellerResponseDto(IdentityManagementUserResource idmResource) {
    SkrillTellerUserResponseDto response = new SkrillTellerUserResponseDto();
    BeanUtils.copyProperties(idmResource, response);
    Profile profile = new Profile();
    profile.setFirstName(idmResource.getFirstName());
    profile.setLastName(idmResource.getLastName());
    response.setProfile(profile);
    return response;
  }

  /**
   * Converts UserProvisioningUserResource to UserMigrationResponseResource.
   *
   * @param userProvisioningUserResource UserProvisioningUserResource
   * @return UserMigrationResponseResource
   */
  public UserMigrationResponseResource toSkrillUserMigrationResponseResource(
      UserProvisioningUserResource userProvisioningUserResource, SkrillTellerMigrationDto skrillTellerDto) {
    UserMigrationResponseResource response = new UserMigrationResponseResource();
    BeanUtils.copyProperties(userProvisioningUserResource, response);
    Profile profile = new Profile();
    profile.setFirstName(userProvisioningUserResource.getFirstName());
    profile.setLastName(userProvisioningUserResource.getLastName());
    response.setProfile(profile);
    response.setAccessResources(
        toMigrationAccessResourcesSkrill(userProvisioningUserResource.getAccessResources(), skrillTellerDto));
    return response;
  }

  /**
   * Converts UserProvisioningUserResource to UserMigrationResponseResource.
   *
   * @param userProvisioningUserResource UserProvisioningUserResource
   * @return UserMigrationResponseResource
   */
  public UserMigrationResponseResource toUserMigrationResponseResource(
      UserProvisioningUserResource userProvisioningUserResource) {
    UserMigrationResponseResource response = new UserMigrationResponseResource();
    BeanUtils.copyProperties(userProvisioningUserResource, response);
    response.setAccessResources(toMigrationAccessResources(userProvisioningUserResource.getAccessResources()));
    return response;
  }

  /**
   * Converts AccessResourcesList to MigrationAccessResourcesList.
   *
   * @param accessResourcesList AccessResources
   * @return MigrationAccessResourcesList
   */
  public List<MigrationAccessResources> toMigrationAccessResources(List<AccessResources> accessResourcesList) {
    List<MigrationAccessResources> migrationAccessResourcesList = new ArrayList<>();
    if (CollectionUtils.isEmpty(accessResourcesList)) {
      return migrationAccessResourcesList;
    }
    for (AccessResources accessResource : accessResourcesList) {
      MigrationAccessResources migrationAccessResource = new MigrationAccessResources();
      BeanUtils.copyProperties(accessResource, migrationAccessResource);
      if (accessResource.getStatus() != null) {
        migrationAccessResource.setStatus(accessResource.getStatus().name());
      }
      if (!CollectionUtils.isEmpty(accessResource.getPermissions())) {
        List<String> permissions = new ArrayList<>();
        for (PermissionDto permissionDto : accessResource.getPermissions()) {
          permissions.add(permissionDto.getLabel());
        }
        migrationAccessResource.setPermissions(permissions);
      }
      migrationAccessResourcesList.add(migrationAccessResource);
    }
    return migrationAccessResourcesList;
  }

  /**
   * Converts AccessResourcesList to SkrillAccessResourcesList.
   *
   * @param accessResourcesList AccessResources
   * @return SkrillAccessResourcesList
   */
  public List<SkrillAccessResources> toSkrillTellerAccessResources(List<AccessResources> accessResourcesList) {
    List<SkrillAccessResources> skrillAccessResourcesList = new ArrayList<>();
    if (CollectionUtils.isEmpty(accessResourcesList)) {
      return skrillAccessResourcesList;
    }
    for (AccessResources accessResource : accessResourcesList) {
      SkrillAccessResources skrillAccessResource = new SkrillAccessResources();
      skrillAccessResource.setResourceId(accessResource.getOwnerId());
      skrillAccessResource.setResourceType(accessResource.getOwnerType());
      if (accessResource.getStatus() != null) {
        skrillAccessResource.setStatus(accessResource.getStatus());
      }
      skrillAccessResource.setRole(accessResource.getRole());
      if (!"ADMIN".equalsIgnoreCase(accessResource.getRole())
          && !CollectionUtils.isEmpty(accessResource.getPermissions())) {
        List<SkrillPermissions> skrillPermissions = new ArrayList<>();
        for (PermissionDto permissionDto : accessResource.getPermissions()) {
          skrillPermissions.add(SkrillPermissions.valueOf(permissionDto.getLabel()));
        }
        skrillAccessResource.setPermissions(skrillPermissions);
      }
      skrillAccessResourcesList.add(skrillAccessResource);
    }
    return skrillAccessResourcesList;
  }

  /**
   * Converts AccessResourcesList to MigrationAccessResourcesList.
   *
   * @param accessResourcesList AccessResources
   * @return MigrationAccessResourcesList
   */
  public List<MigrationAccessResources> toMigrationAccessResourcesSkrill(List<AccessResources> accessResourcesList,
      SkrillTellerMigrationDto skrillTellerDto) {
    List<MigrationAccessResources> migrationAccessResourcesList = new ArrayList<>();
    if (CollectionUtils.isEmpty(accessResourcesList)) {
      return migrationAccessResourcesList;
    }
    for (AccessResources accessResource : accessResourcesList) {
      MigrationAccessResources migrationAccessResource = new MigrationAccessResources();
      migrationAccessResource.setResourceId(accessResource.getOwnerId());
      migrationAccessResource.setResourceType(accessResource.getOwnerType());
      if (accessResource.getStatus() != null) {
        migrationAccessResource.setStatus(accessResource.getStatus().name());
      }
      if (isBlockedAccessResource(accessResource.getId(), skrillTellerDto)) {
        migrationAccessResource.setStatus(AccessResourceStatus.BLOCKED.name());
      }
      migrationAccessResource.setRole(accessResource.getRole());
      if (!"ADMIN".equalsIgnoreCase(accessResource.getRole())
          && !CollectionUtils.isEmpty(accessResource.getPermissions())) {
        List<String> skrillPermissions = new ArrayList<>();
        for (PermissionDto permissionDto : accessResource.getPermissions()) {
          skrillPermissions.add(permissionDto.getLabel());
        }
        migrationAccessResource.setPermissions(skrillPermissions);
      }
      migrationAccessResourcesList.add(migrationAccessResource);
    }
    return migrationAccessResourcesList;
  }

  private boolean isBlockedAccessResource(String resourceId, SkrillTellerMigrationDto skrillTellerDto) {
    boolean isBlocked = false;
    List<SkrillAccessResources> accessResources = skrillTellerDto.getAccessResources();
    if (CollectionUtils.isNotEmpty(accessResources)) {
      for (SkrillAccessResources accessResourceObj : accessResources) {
        if (AccessResourceStatus.BLOCKED.equals(accessResourceObj.getStatus())
            && accessResourceObj.getResourceId().equals(resourceId)) {
          isBlocked = true;
          break;
        }
      }
    }
    return isBlocked;
  }

  private UserStatus resolveUserStatus(String status) {
    UserStatus userStatus = null;
    if ("ENABLED".equals(status)) {
      userStatus = UserStatus.ACTIVE;
    } else if ("DISABLED".equals(status)) {
      userStatus = UserStatus.DEACTIVATED;
    } else if ("PENDING".equals(status)) {
      userStatus = UserStatus.PENDING_USER_ACTION;
    }
    return userStatus;
  }

  /**
   * Converts PegasusUserResponseResource to UserDto.
   *
   * @param pegasusUserResponseResource PegasusUserResponseResource
   * @return UserDto
   */
  public UserDto toUserDto(PegasusUserResponseResource pegasusUserResponseResource) {
    UserDto userDto = new UserDto();
    userDto.setUserName(pegasusUserResponseResource.getLoginName());
    userDto.setEmail(pegasusUserResponseResource.getEmail());
    userDto.setPmleId(pegasusUserResponseResource.getPmleId().toString());
    userDto.setId(pegasusUserResponseResource.getUuid());
    userDto.setStatus(resolveUserStatus(pegasusUserResponseResource.getStatus()));
    return userDto;
  }

  /**
   * Converts CustomAccessGroupDto to AccessResources.
   */
  public AccessResources toAccessResources(CustomAccessGroupDto customAccessGroupDto) {
    if (customAccessGroupDto != null) {
      AccessResources accessResources = new AccessResources();
      accessResources.setId(customAccessGroupDto.getMerchantId());
      accessResources.setType(customAccessGroupDto.getMerchantType());
      accessResources.setAccessGroupType(customAccessGroupDto.getType());
      return accessResources;
    }
    return null;
  }

  /**
   * Converts UserAccessGroupMappingDao to AccessResources.
   */
  public AccessResources toAccessResources(UserAccessGroupMappingDao userAccessGroupDao) {
    AccessResources accessResources = new AccessResources();
    if (userAccessGroupDao != null) {
      accessResources.setAccessGroupId(userAccessGroupDao.getAccessGroupCode());
      accessResources.setAccessGroupType(userAccessGroupDao.getAccessGroupType());
      accessResources.setId(userAccessGroupDao.getResourceId());
      accessResources.setType(userAccessGroupDao.getResourceType());
      accessResources.setStatus(userAccessGroupDao.getUserAccessGroupStatus());
      if (AccessGroupType.DEFAULT_ADMIN.equals(userAccessGroupDao.getAccessGroupType())) {
        accessResources.setRole(DataConstants.ADMIN);
      } else {
        accessResources.setRole(DataConstants.REGULAR);
      }
    }
    return accessResources;
  }

  /**
   * Converts AccessResources and IdentityManagementUserResource to UserAccessGroupMappingDao.
   */
  public UserAccessGroupMappingDao toUserAccessGroupMappingDao(AccessResources accessResource,
      IdentityManagementUserResource userResponse) {
    if (accessResource != null && userResponse != null) {
      UserAccessGroupMappingDao userAccessGroupDao = new UserAccessGroupMappingDao();
      userAccessGroupDao.setAccessGroupCode(accessResource.getAccessGroupId());
      userAccessGroupDao.setAccessGroupType(accessResource.getAccessGroupType());
      if (StringUtils.isNotEmpty(accessResource.getOwnerId())
          && StringUtils.isNotEmpty(accessResource.getOwnerType())) {
        userAccessGroupDao.setResourceId(accessResource.getOwnerId());
        userAccessGroupDao.setResourceType(accessResource.getOwnerType());
      } else {
        userAccessGroupDao.setResourceId(accessResource.getId());
        userAccessGroupDao.setResourceType(accessResource.getType());
      }
      userAccessGroupDao.setLoginName(userResponse.getUserName());
      userAccessGroupDao.setUserId(userResponse.getId());
      userAccessGroupDao.setUserExternalId(userResponse.getExternalId());
      userAccessGroupDao.setUserFirstName(userResponse.getFirstName());
      userAccessGroupDao.setUserLastName(userResponse.getLastName());
      return userAccessGroupDao;
    }
    return null;
  }

  /**
   * Converts IdentityManagementUserResource to UserResponseResource.
   */
  public UserResponseResource toUserResponseResource(IdentityManagementUserResource body, User userRepoResponse) {
    UserResponseResource userResponseResource = new UserResponseResource();
    if (body != null) {
      BeanUtils.copyProperties(body, userResponseResource);
      userResponseResource.setCustomProperties(body.getCustomProperties());
      if (body.getStatus().equals(UserStatus.SUSPENDED) && !userRepoResponse.getStatus().equals(Status.DORMANT)) {
        userResponseResource.setStatus(UserStatus.BLOCKED);
      } else if (body.getStatus().equals(UserStatus.SUSPENDED) && userRepoResponse.getStatus().equals(Status.DORMANT)) {
        userResponseResource.setStatus(UserStatus.DORMANT);
      }
      if ("Y".equalsIgnoreCase(userRepoResponse.getMfaEnabled())) {
        userResponseResource.setMfaEnabled(true);
      } else {
        userResponseResource.setMfaEnabled(false);
      }
    }
    return userResponseResource;
  }

  /**
   * Mapping the blocked accessRssources status.
   */
  public void mapBlockedAccessResourcesStatus(SkrillTellerUserResponseResource responseResource,
      SkrillTellerMigrationDto skrillTellerMigrationDto) {
    List<SkrillAccessResources> accessResources = responseResource.getAccessResources();
    if (CollectionUtils.isNotEmpty(accessResources)) {
      for (SkrillAccessResources accessResource : accessResources) {
        if (isBlockedAccessResource(accessResource.getResourceId(), skrillTellerMigrationDto)) {
          accessResource.setStatus(AccessResourceStatus.BLOCKED);
        }
      }
    }
  }


  /**
   * Converts OktaEventHookResource to AuditUserEvent.
   *
   * @throws JsonProcessingException ex.
   */
  public AuditUserEvent toAuditUserEvent(Event event, String application)
      throws JsonProcessingException {
    if (isSkrillTellerSuccessEvent(event, application)) {
      return null;
    }
    ObjectNode auditEventDataObjectNode = objectMapper.createObjectNode();
    AuditUserEvent auditUserEvent = new AuditUserEvent();
    auditUserEvent.setId(event.getUuid());
    auditUserEvent.setEventType(AuditEventType.LOGIN);
    auditUserEvent.setTargetUserName(event.getActor().getAlternateId());
    if (("FAILURE").equals(event.getOutcome().getResult())) {
      auditUserEvent.setEventStatus(AuditEventStatus.FAILED);
      if ((PENDING_USER_ACTION_EVENT_REASON_OKTA).equals(event.getOutcome().getReason())) {
        auditEventDataObjectNode.put("reason", PENDING_USER_ACTION_EVENT_REASON);
      } else {
        auditEventDataObjectNode.put("reason", event.getOutcome().getReason());
      }
    } else {
      auditUserEvent.setEventStatus(AuditEventStatus.SUCCESS);
      auditEventDataObjectNode.put("reason", "User login");
    }
    auditEventDataObjectNode.put("details", event.getDisplayMessage());
    String eventDatajson = objectMapper.writeValueAsString(auditEventDataObjectNode);
    auditUserEvent.setEventData(eventDatajson);
    auditUserEvent.setBrowser(event.getClient().getUserAgent().getBrowser());
    auditUserEvent.setUserIpAddress(fetchIpAddress(event.getClient()));

    auditUserEvent.setCreatedBy(event.getActor().getAlternateId());
    auditUserEvent.setApplication(application);
    auditUserEvent.setSourceApp("BUSINESS_PORTAL");
    auditUserEvent.setEventTimeStamp(DateTime.parse(event.getPublished()));
    auditUserEvent.setCreatedDate(DateTime.now(DateTimeZone.UTC));
    return auditUserEvent;
  }

  /**
   * This method checks for if the event is a success for skrillteller apps and not logs the event for it.
   */
  private boolean isSkrillTellerSuccessEvent(Event event, String application) {
    return (StringUtils.equals(DataConstants.SKRILL, application)
        || StringUtils.equals(DataConstants.NETELLER, application))
        && !(("FAILURE").equals(event.getOutcome().getResult()));
  }

  private String fetchIpAddress(Client client) {
    String ipAddress = null;
    if (client.getIpChain() != null) {
      for (IpChain ipchain : client.getIpChain()) {
        if (!ipchain.getIp().equals(client.getIpAddress())) {
          ipAddress = ipchain.getIp();
          break;
        } else {
          ipAddress = client.getIpAddress();
        }
      }
    } else {
      ipAddress = client.getIpAddress();
    }
    return ipAddress;
  }
}
