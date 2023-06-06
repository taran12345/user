// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.upf.user.provisioning.config.OktaAppConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.config.UserProvisioningConfig;
import com.paysafe.upf.user.provisioning.domain.HashedPasswordEntity;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.domain.UsersSummary;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.enums.OwnerType;
import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.enums.TokenType;
import com.paysafe.upf.user.provisioning.enums.UserAction;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.enums.UserStatusFilter;
import com.paysafe.upf.user.provisioning.feignclients.AccessGroupFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.PegasusFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.PermissionServiceClient;
import com.paysafe.upf.user.provisioning.model.OwnerInfo;
import com.paysafe.upf.user.provisioning.repository.HashedPasswordRepository;
import com.paysafe.upf.user.provisioning.repository.SkrillTellerUserSpecification;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UserSpecification;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.repository.UsersSummaryRepository;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AccessGroupService;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.service.FeatureFlagService;
import com.paysafe.upf.user.provisioning.service.MailService;
import com.paysafe.upf.user.provisioning.service.SkrillTellerUserService;
import com.paysafe.upf.user.provisioning.service.TokenService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.utils.AuditUserEventUtil;
import com.paysafe.upf.user.provisioning.utils.LoggingUtil;
import com.paysafe.upf.user.provisioning.utils.UserCreationUtil;
import com.paysafe.upf.user.provisioning.utils.UserFilterUtil;
import com.paysafe.upf.user.provisioning.utils.UserManagmentUtil;
import com.paysafe.upf.user.provisioning.utils.UserPasswordManagementUtil;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto.AuditUserEventDtoBuilder;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomAccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomRoleDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.RoleDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserPasswordMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupNameAvailabilityResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupNameAvailabilityResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ChangePasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUpdateUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserListResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUpdateUserRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegausUserRoleResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetPasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateTokenResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
  public static final String EXT = "EXT";

  @Autowired
  private AccessGroupFeignClient accessGroupFeignClient;
  @Autowired
  private PermissionServiceClient permissionServiceClient;

  @Autowired
  private FeatureFlagService featureFlagService;

  @Autowired
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Autowired
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Autowired
  private UsersRepository usersRepository;

  @Autowired
  private PegasusFeignClient pegasusFeignClient;

  @Autowired
  private UserAssembler userAssembler;

  @Autowired
  private AccessGroupService accessGroupService;

  @Autowired
  private UserProvisioningUtils userProvisioningUtils;

  @Autowired
  private AuditUserEventUtil auditUserEventUtil;

  @Autowired
  private UserPasswordManagementUtil userPasswordManagementUtil;

  @Autowired
  private TokenService tokenService;

  @Autowired
  private MailService mailService;

  @Autowired
  private UserSpecification userSpecification;

  @Autowired
  private SkrillTellerUserSpecification skrillTellerUserSpecification;

  @Autowired
  private UserFilterUtil userFilterUtil;

  @Autowired
  private UsersSummaryRepository usersSummaryRepository;

  @Autowired
  private AuditService auditService;

  @Autowired
  private HashedPasswordRepository hashedPasswordRepository;

  @Autowired
  private SkrillTellerConfig skrillTellerConfig;

  @Autowired
  private UserProvisioningConfig userProvisioningConfig;

  @Autowired
  private SkrillTellerUserService skrillTellerUserService;

  @Autowired
  private UserCreationUtil userCreationUtil;

  @Autowired
  private OktaAppConfig oktaAppConfig;

  @Value("${migration.shouldDoCompleteUserMigration}")
  private boolean shouldDoCompleteUserMigration;
  private static final String PORTAL_APPLICATION = "PORTAL";
  private static final String NETELLER_APPLICATION = "NETELLER";
  private static final String NOT_MIGRATED_FLAG = "NOT_MIGRATED";

  @Override
  public UserProvisioningUserResource createUser(UserDto userDto) throws JsonProcessingException {
    userDto.setBusinessUnit(UserManagmentUtil.getBusinessUnit(userDto.getBusinessUnit(), null));
    userCreationUtil.validateCreateUserRequest(userDto);
    userProvisioningUtils.setOwnerInfo(userDto);
    userDto.setGroupIds(userCreationUtil.getGroupIds(userDto.getGroupIds(), userDto.getApplicationName(),
        userDto.getDivision(), userDto.getUserAssignedApplications()));
    List<String> rolesToAdd = createUserRoles(userDto.getRoleDto());
    List<AccessResources> accessResources = createAccessGroups(userDto.getAccessGroupDto());
    if (userDto.getAccessResources() != null) {
      accessResources.addAll(accessGroupService.createAccessGroupsFromResouresList(userDto));
    }
    List<String> accessGroupsToAdd = new ArrayList<>();
    for (AccessResources accessResource : accessResources) {
      accessGroupsToAdd.add(accessResource.getAccessGroupId());
    }
    AuditUserEventDtoBuilder auditUserEventDtoBuilder = AuditUserEventDto.builder();
    auditUserEventDtoBuilder.eventTimeStamp(DateTime.now(DateTimeZone.UTC))
        .auditUserEventResources(auditUserEventUtil.constructCreateUserAuditResourceDtos(accessResources))
        .targetUserName(userDto.getUserName());
    userProvisioningUtils.assignAuditEvent(userDto, auditUserEventDtoBuilder);
    ResponseEntity<IdentityManagementUserResource> response;
    try {
      response = identityManagementFeignClient.createUser(
          userProvisioningUtils.constructIdentityManagementCreateUserResource(userDto, rolesToAdd, accessGroupsToAdd));
    } catch (Exception e) {
      auditUserEventDtoBuilder.eventStatus(AuditEventStatus.FAILED);
      auditService.createAuditEntry(auditUserEventDtoBuilder.build());
      LOGGER.error("Couldn't create the user, received error response.", e);
      throw e;
    }
    if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
      IdentityManagementUserResource idmResponse = response.getBody();
      UserProvisioningUserResource userResponse = new UserProvisioningUserResource();
      BeanUtils.copyProperties(idmResponse, userResponse);
      OwnerInfo ownerInfo = new OwnerInfo(userDto.getOwnerId(), userDto.getOwnerType(), userDto.getApplicationName());
      userResponse.setUserSummary(userDto.getUserSummary());
      User user = checkAndCreateUser(userResponse, ownerInfo);
      checkNetellerUser(user, userDto);
      List<UserAccessGroupMappingDao> userAccessGroups = new ArrayList<>();
      for (AccessResources accessResource : accessResources) {
        UserAccessGroupMappingDao userAccessGroupDao =
            userAssembler.toUserAccessGroupMappingDao(accessResource, userResponse);
        userAccessGroupDao.setUserAccessGroupStatus(AccessResourceStatus.ACTIVE);
        userAccessGroups.add(userAccessGroupDao);
      }
      user.setAccessGroupMappingDaos(userAccessGroups);
      user.setUserAssignedApplications(
          UserManagmentUtil.getAssignedApplicationsCreateFlow(userDto.getUserAssignedApplications(), user));
      usersRepository.save(user);
      userProvisioningUtils.populateAccessResources(userResponse);
      userResponse.setOwnerId(userDto.getOwnerId());
      userResponse.setOwnerType(userDto.getOwnerType());
      if (userDto.getIsMailNotificationsEnabled() && userProvisioningConfig.isMailNotificationsEnabled()) {
        mailService.sendRegistrationConfirmationEmail(userResponse);
      }
      auditUserEventDtoBuilder.eventStatus(AuditEventStatus.SUCCESS).userStatus(idmResponse.getStatus().name())
          .targetUserId(idmResponse.getId());
      auditService.createAuditEntry(auditUserEventDtoBuilder.build());
      updateRegion(userDto);
      return userResponse;
    } else {
      auditUserEventDtoBuilder.eventStatus(AuditEventStatus.FAILED);
      auditService.createAuditEntry(auditUserEventDtoBuilder.build());
      LOGGER.error("Couldn't create the user, received error response.");
      throw InternalErrorException.builder().details("Couldn't create the user").build();
    }
  }

  private void updateRegion(UserDto userDto) {
    if (StringUtils.equals(userDto.getApplicationName(), DataConstants.SKRILL)
        || StringUtils.equals(userDto.getApplicationName(), DataConstants.NETELLER)) {
      skrillTellerUserService.updateUsersRegion(userDto.getApplicationName(), userDto.getUserName(), 1);
    }
  }

  @Override
  public UserProvisioningUserResource updateUser(String userId, UserUpdationDto userUpdationDto)
      throws JsonProcessingException {
    boolean isNonEmptyAccessResources = CollectionUtils.isNotEmpty(userUpdationDto.getAccessResources());
    userCreationUtil.validateUpdateUserRequest(userUpdationDto);
    userProvisioningUtils.setOwnerInfoUpdateUser(userUpdationDto);
    User user = usersRepository.findByUserId(userId);
    userCreationUtil.handleUpdateUserGroupIds(user, userUpdationDto);
    userUpdationDto
        .setBusinessUnit(UserManagmentUtil.getBusinessUnit(userUpdationDto.getBusinessUnit(), user.getBusinessUnit()));
    String existingEmail = user.getEmail();
    UserResponseResource userResourceBeforeUpdate = getUsersByFilters(user.getApplication(), user.getLoginName(), null,
            null, null, null, null, null, null, null, 0, 10, false, true).getUsers().get(0);
    accessGroupService.createAccessGroupsForUpdateUser(userId, userUpdationDto);
    List<String> rolesToAdd = createUserRoles(userUpdationDto.getRolesToAdd());
    List<AccessResources> accessResources = createAccessGroups(userUpdationDto.getAccessGroupsToAdd());
    List<String> accessGroupsToAdd =
        accessResources.stream().map(AccessResources::getAccessGroupId).collect(Collectors.toList());
    if (userUpdationDto.getAccessGroupsToAdd() != null) {
      accessGroupsToAdd.addAll(userUpdationDto.getAccessGroupsToAdd().getExistingAccessGroupIds());
    }
    if (userProvisioningUtils.checkIfDeleteReportEnabled(userUpdationDto.getApplicationName())) {
      userProvisioningUtils.deleteReportsforWallet(userId, userUpdationDto);
    }
    ResponseEntity<IdentityManagementUserResource> response = null;
    try {
      response = identityManagementFeignClient.updateUser(userId, UserManagmentUtil
          .constructIdentityManagementUpdateUserResource(userUpdationDto, rolesToAdd, accessGroupsToAdd));
      UserManagmentUtil.logoutUserFromAllActiveSessions(identityManagementFeignClient, userId,
          userUpdationDto.getApplicationName());
    } catch (Exception e) {
      auditService.createAuditEntry(AuditUserEventDto.builder().eventType(AuditEventType.UPDATE_USER)
          .targetUserName(user.getLoginName()).eventStatus(AuditEventStatus.FAILED).targetUserId(userId)
          .eventTimeStamp(DateTime.now(DateTimeZone.UTC)).build());
      LOGGER.error("Couldn't update the user, recieved error response.", e);
      throw e;
    }
    UserProvisioningUserResource userResponse;
    if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
      IdentityManagementUserResource idmResponse = response.getBody();
      userResponse = new UserProvisioningUserResource();
      BeanUtils.copyProperties(idmResponse, userResponse);
      OwnerInfo ownerInfo = UserManagmentUtil.getUpdatedUserOwnerInfo(idmResponse);
      userResponse.setUserSummary(userUpdationDto.getUserSummary());
      updateAccessGroups(userUpdationDto, userResponse, ownerInfo, user);
      for (AccessResources accessResource : accessResources) {
        UserAccessGroupMappingDao userAccessGroupDao =
            userAssembler.toUserAccessGroupMappingDao(accessResource, userResponse);
        userAccessGroupDao.setUserAccessGroupStatus(AccessResourceStatus.ACTIVE);
        user.getAccessGroupMappingDaos().add(userAccessGroupDao);
        usersRepository.save(user);
      }
      userProvisioningUtils.populateAccessResources(userResponse);
      if (userUpdationDto.getUserSummary() != null) {
        sendEmailForUpdate(existingEmail, userUpdationDto, userResponse, isNonEmptyAccessResources);
      }
      if (!userUpdationDto.isStatusUpdate()) {
        auditService.createAuditEntry(AuditUserEventDto.builder().eventType(AuditEventType.UPDATE_USER)
            .eventStatus(AuditEventStatus.SUCCESS).eventTimeStamp(DateTime.now(DateTimeZone.UTC))
            .userStatus(userResponse.getStatus() != null ? userResponse.getStatus().name() : null)
            .targetUserName(userResponse.getUserName()).targetUserId(userResponse.getId())
            .createdBy(UserManagmentUtil.getUserNameFromThreadLocal())
            .eventData(UserManagmentUtil.contructEventData("Update user",
                UserManagmentUtil.getUserNameFromThreadLocal() + " has successfully updated "
                    + userResponse.getEmail()))
            .auditUserEventResources(auditUserEventUtil.constructEditUserAuditResourceDtos(
                userResourceBeforeUpdate.getAccessResources(), userResponse.getAccessResources()))
            .build());
      }
      return userResponse;
    } else {
      auditService.createAuditEntry(AuditUserEventDto.builder().eventType(AuditEventType.UPDATE_USER)
          .targetUserName(user.getLoginName()).eventStatus(AuditEventStatus.FAILED).targetUserId(userId)
          .eventTimeStamp(DateTime.now(DateTimeZone.UTC)).build());
      LOGGER.error("Couldn't update the user, recieved error response.");
      throw InternalErrorException.builder().details("Couldn't update the user").build();
    }
  }

  private void updateAccessGroups(UserUpdationDto userUpdationDto, UserProvisioningUserResource userResponse,
      OwnerInfo ownerInfo, User user) {
    if (userUpdationDto.getAccessGroupsToHardDelete() != null) {
      userProvisioningUtils.deleteAccessResourcesInfo(userUpdationDto.getAccessGroupsToHardDelete(), userResponse,
          user);
    }
    if (userUpdationDto.getAccessGroupsToDelete() != null) {
      saveAccessResouceInfo(userUpdationDto.getAccessGroupsToDelete(), AccessResourceStatus.BLOCKED, userResponse,
          ownerInfo);
    }
    if (userUpdationDto.getAccessGroupsToAdd() != null) {
      saveAccessResouceInfo(userUpdationDto.getAccessGroupsToAdd().getExistingAccessGroupIds(),
          AccessResourceStatus.ACTIVE, userResponse, ownerInfo);
    }
  }

  private void sendEmailForUpdate(String existingEmail, UserUpdationDto userUpdationDto,
      UserProvisioningUserResource userResponse, boolean isNonEmptyAccessResources) {
    boolean isMailEanbled = userProvisioningConfig.isMailNotificationsEnabled();
    if (userUpdationDto.getEmail() != null && !(existingEmail.equals(userUpdationDto.getEmail()))
        && (UserStatus.PROVISIONED.equals(userResponse.getStatus())) && isMailEanbled) {
      mailService.sendRegistrationConfirmationEmail(userResponse);
    } else if (isNonEmptyAccessResources && isMailEanbled && !userUpdationDto.isDisableMailNotifications()) {
      mailService.sendPermissionsUpdatedConfirmationEmail(userResponse);
    }
  }

  @Override
  public IdentityManagementUserResource fetchUser(String userId) {
    ResponseEntity<IdentityManagementUserResource> response = identityManagementFeignClient.getUser(userId, null);
    if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
      return response.getBody();
    } else {
      throw InternalErrorException.builder().details("Couldn't fetch the user with userId: " + userId).build();
    }
  }

  @Override
  public IdentityManagementUserResource migrateUser(String userId, UserPasswordMigrationDto userPasswordMigrationDto,
      String application) {
    IdentityManagementUserListResource identityManagementUserListResource =
        identityManagementFeignClient.getUsersByUserName(userId);
    if (identityManagementUserListResource != null && !identityManagementUserListResource.getUsers().isEmpty()) {
      IdentityManagementUpdateUserResource updateUserResource = new IdentityManagementUpdateUserResource();
      updateUserResource.setPassword(userPasswordMigrationDto.getPassword());
      identityManagementFeignClient.updateUser(userId, updateUserResource);
      updateOktaMigratedFlagInPegasus(userId);
    } else {
      if (shouldDoCompleteUserMigration) {
        PegasusUserListResponseResource pegasusUserListResponseResource =
            pegasusFeignClient.getUsers(userId, null, null, null);
        PegasusUserResponseResource pegasusUserResponseResource = pegasusUserListResponseResource.getUsers().get(0);
        if (pegasusUserResponseResource != null && pegasusUserResponseResource.getType().equals(EXT)) {
          UserDto userDto = userAssembler.toUserDto(pegasusUserResponseResource);
          userDto.setPassword(userPasswordMigrationDto.getPassword());
          List<String> roles = pegasusUserResponseResource.getRoles().stream().map(PegausUserRoleResource::getRoleCode)
              .collect(Collectors.toList());
          ResponseEntity<IdentityManagementUserResource> userCreationResponse = identityManagementFeignClient
              .createUser(userProvisioningUtils.constructIdentityManagementCreateUserResource(userDto, roles,
                  pegasusUserResponseResource.getAccessGroups()));
          return userCreationResponse.getBody();
        } else {
          LOGGER.error("Couldn't migrate the user of type INT with userId: {} ",
              LoggingUtil.replaceSpecialChars(userId));
          throw InternalErrorException.builder().details("Invalid user type").build();
        }
      }
    }
    if (identityManagementUserListResource != null
        && CollectionUtils.isNotEmpty(identityManagementUserListResource.getUsers())) {
      return identityManagementUserListResource.getUsers().get(0);
    } else {
      return null;
    }
  }

  @Override
  public void updateUserStatus(String userId, UpdateUserStatusResource resource) throws JsonProcessingException {
    userProvisioningUtils.updateUserStatus(userId, resource);
    if (resource.getAction() != UserAction.ACTIVATE) {
      UserManagmentUtil.logoutUserFromAllActiveSessions(identityManagementFeignClient, userId,
          resource.getApplicationName());
    }
  }

  private void checkNetellerUser(User user, UserDto userDto) {
    if (userDto.getApplicationName() != null && userDto.getHashedPassword() != null
        && userDto.getApplicationName().equalsIgnoreCase(NETELLER_APPLICATION)) {
      HashedPasswordEntity hashedPassword = userAssembler.toHashedPasswordEntity(userDto.getHashedPassword());
      hashedPassword = hashedPasswordRepository.save(hashedPassword);
      user.setHashedPassword(hashedPassword);
      user.setMigratedFlag(NOT_MIGRATED_FLAG);
    }
  }

  private void updateOktaMigratedFlagInPegasus(String userId) {
    PegasusUpdateUserRequestResource pegasusUpdateUserRequestResource = new PegasusUpdateUserRequestResource();
    pegasusUpdateUserRequestResource.setOktaMigrated(true);
    AuthorizationInfo authLocal = CommonThreadLocal.getAuthLocal();
    CommonThreadLocal.setAuthLocal(null);
    pegasusFeignClient.updateUser(pegasusUpdateUserRequestResource, userId);
    CommonThreadLocal.setAuthLocal(authLocal);
  }

  /**
   * Utility method to manage the user groups while updating the user.
   */
  public void validateRoles(RoleDto roleDto) {
    ResponseEntity<List<String>> roleNamesResponseEntity = permissionServiceClient.getRoleNames();
    if (roleNamesResponseEntity.getBody() != null) {
      List<String> existingRoleNamesListFromPermissionService = roleNamesResponseEntity.getBody();
      if (CollectionUtils.isNotEmpty(roleDto.getCustomRoles())) {
        checkIfCustomRolesAlreadyCreatedAndAddToExistingRoles(roleDto, existingRoleNamesListFromPermissionService);
      }
      if (CollectionUtils.isNotEmpty(roleDto.getExistingRoles())) {
        userProvisioningUtils.validateExistingRoles(roleDto.getExistingRoles(),
            existingRoleNamesListFromPermissionService);
      }
      if (CollectionUtils.isNotEmpty(roleDto.getCustomRoles())) {
        UserManagmentUtil.validateCustomRoles(
            roleDto.getCustomRoles().stream().map(CustomRoleDto::getRoleName).collect(Collectors.toList()),
            existingRoleNamesListFromPermissionService);
      }
    } else {
      LOGGER.error("Unable to fetch role names from permission service. Received null response");
      throw InternalErrorException.builder()
          .details("Unable to fetch role names from permission service. Received null response").build();
    }
  }

  private void checkIfCustomRolesAlreadyCreatedAndAddToExistingRoles(RoleDto roleDto,
      List<String> existingRoleNamesListFromPermissionService) {
    List<CustomRoleDto> filteredCustomRoleDtos = roleDto.getCustomRoles().stream()
        .filter(cR -> existingRoleNamesListFromPermissionService.contains(cR.getRoleName()))
        .filter(this::comparePermissionLists).collect(Collectors.toList());
    roleDto.getCustomRoles().removeAll(filteredCustomRoleDtos);
    roleDto.getExistingRoles()
        .addAll(filteredCustomRoleDtos.stream().map(CustomRoleDto::getRoleName).collect(Collectors.toList()));
  }

  private boolean comparePermissionLists(CustomRoleDto customRoleDto) {
    ResponseEntity<Map<String, Object>> mapResponseEntity = permissionServiceClient
        .getPermissionsForRolesAndCategories(Collections.singletonList(customRoleDto.getRoleName()), true);
    Map<String, Object> response = mapResponseEntity.getBody();
    List<Map<String, Object>> permissionGroup;
    List<Map<String, Object>> permissionsSet = null;
    if (response.containsKey("permissionGroup")) {
      permissionGroup = (List<Map<String, Object>>) response.get("permissionGroup");
      if (!permissionGroup.isEmpty() && permissionGroup.get(0).containsKey("permissionsSet")) {
        permissionsSet = (List<Map<String, Object>>) permissionGroup.get(0).get("permissionsSet");
      }
    }
    List<String> consolidatedPermissions = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(permissionsSet)) {
      permissionsSet.forEach(entry -> consolidatedPermissions.addAll((List<String>) entry.get("permissionList")));
    }
    if (CollectionUtils.isEqualCollection(consolidatedPermissions, customRoleDto.getPermissionList())) {
      return true;
    } else {
      LOGGER.error("Request contains existing role with different permission set, that needs to be created: {}.",
          customRoleDto.getRoleName());
      throw BadRequestException.builder()
          .details("Request contains existing role," + " that needs to be created: " + customRoleDto.getRoleName())
          .build();
    }
  }

  /**
   * Utility method to validate access groups.
   */
  public void validateAccessGroups(AccessGroupDto accessGroupDto) {
    if (CollectionUtils.isNotEmpty(accessGroupDto.getExistingAccessGroupIds())) {
      validateExistingAccessGroups(accessGroupDto.getExistingAccessGroupIds());
    }
    if (CollectionUtils.isNotEmpty(accessGroupDto.getCustomAccessGroupDtos())) {
      checkIfAccessGroupsAlreadyCreated(accessGroupDto);
    }
    if (CollectionUtils.isNotEmpty(accessGroupDto.getCustomAccessGroupDtos())) {
      validateCustomAccessGroups(accessGroupDto.getCustomAccessGroupDtos().stream().map(CustomAccessGroupDto::getName)
          .collect(Collectors.toList()));
    }
  }

  private void validateCustomAccessGroups(List<String> customAccessGroups) {
    ResponseEntity<AccessGroupNameAvailabilityResponseResource> response =
        accessGroupFeignClient.getAccessGroupNameAvailabilityList(customAccessGroups);
    if (response != null && response.getBody() != null) {
      List<String> accessGroupsPresent =
          response.getBody().getAccessGroupNames().stream().filter(aG -> !aG.isAvailable())
              .map(AccessGroupNameAvailabilityResource::getAccessGroupName).collect(Collectors.toList());
      List<String> intersectionList = ListUtils.intersection(customAccessGroups, accessGroupsPresent);
      if (CollectionUtils.isNotEmpty(intersectionList)) {
        LOGGER.error("Request contains existing access groups, that needs to be created: {}.",
            intersectionList.toString());
        throw BadRequestException.builder().requestNotParsable().details(
            "Request contains existing access groups," + " that needs to be created: " + intersectionList.toString())
            .build();
      }
    } else {
      LOGGER.error("Error while fetching the accessgroups present from the given list.");
      throw InternalErrorException.builder()
          .details("Error while fetching the accessgroups present from the given list.").build();
    }
  }

  private void checkIfAccessGroupsAlreadyCreated(AccessGroupDto accessGroupDto) {
    ResponseEntity<AccessGroupNameAvailabilityResponseResource> response =
        accessGroupFeignClient.getAccessGroupNameAvailabilityList(accessGroupDto.getCustomAccessGroupDtos().stream()
            .map(CustomAccessGroupDto::getName).collect(Collectors.toList()));
    if (response != null && response.getBody() != null) {
      List<String> accessGroupsPresent =
          response.getBody().getAccessGroupNames().stream().filter(aG -> !aG.isAvailable())
              .map(AccessGroupNameAvailabilityResource::getAccessGroupName).collect(Collectors.toList());
      List<String> accessGroupIdsPresent = new ArrayList<>();
      List<CustomAccessGroupDto> filteredAccessGroupDtos =
          accessGroupDto.getCustomAccessGroupDtos().stream().filter(cA -> accessGroupsPresent.contains(cA.getName()))
              .filter(cA -> compareAccessPoliciesList(cA, accessGroupIdsPresent)).collect(Collectors.toList());
      accessGroupDto.getCustomAccessGroupDtos().removeAll(filteredAccessGroupDtos);
      accessGroupDto.getExistingAccessGroupIds().addAll(accessGroupIdsPresent);
    } else {
      LOGGER.error("Error while fetching the available accessgroup names.");
      throw InternalErrorException.builder().details("Error while fetching the available accessgroup names.").build();
    }
  }

  private boolean compareAccessPoliciesList(CustomAccessGroupDto customAccessGroupDto,
      List<String> accessGroupIdsPresent) {
    ResponseEntity<AccessGroupResponseResource> dtoResponseEntity =
        accessGroupFeignClient.fetchAccessGroupByName(customAccessGroupDto.getName());
    if (dtoResponseEntity != null && dtoResponseEntity.getBody() != null) {
      AccessGroupResponseResource accessGroupResponseResource = dtoResponseEntity.getBody();
      List<String> accessPolicyIdsPresent = accessGroupResponseResource.getAccessGroupPolicies().stream()
          .map(groupPolicy -> groupPolicy.getAcessPolicy().getCode()).collect(Collectors.toList());
      if (CollectionUtils.isEqualCollection(accessPolicyIdsPresent, customAccessGroupDto.getAccessPolicyIds())) {
        accessGroupIdsPresent.add(accessGroupResponseResource.getCode());
        return true;
      } else {
        LOGGER.error(
            "Request contains existing access group with different access policies," + " that needs to be created: {}.",
            customAccessGroupDto.getName());
        throw BadRequestException.builder().requestNotParsable().details(
            "Request contains existing access group," + " that needs to be created: " + customAccessGroupDto.getName())
            .build();
      }
    } else {
      LOGGER
          .error("Error while fetching the accessgroup object by name. Call to accessgroup service resulted in error");
      throw InternalErrorException.builder()
          .details("Error while fetching the accessgroup object by name. Call to accessgroup service resulted in error")
          .build();
    }
  }

  private void validateExistingAccessGroups(List<String> existingAccessGroupIds) {
    ResponseEntity<List<String>> accessGroupsPresentResponse =
        accessGroupFeignClient.getAccessGroupsPresentFromInputList(existingAccessGroupIds);
    List<String> accessGroupsPresent;
    if (accessGroupsPresentResponse != null && accessGroupsPresentResponse.getBody() != null) {
      accessGroupsPresent = accessGroupsPresentResponse.getBody();
      if (!accessGroupsPresent.containsAll(existingAccessGroupIds)) {
        existingAccessGroupIds.removeAll(accessGroupsPresent);
        LOGGER.error("Request contains invalid access groups: {}.", existingAccessGroupIds.toString());
        throw BadRequestException.builder().requestNotParsable()
            .details("Request contains invalid access groups: " + existingAccessGroupIds.toString()).build();
      }
    } else {
      LOGGER.error("Error while fetching the accessgroups present from the given list.");
      throw InternalErrorException.builder()
          .details("Error while fetching the accessgroups present from the given list.").build();
    }
  }

  private List<String> createUserRoles(RoleDto roleDto) {
    List<String> rolesToAdd = new ArrayList<>();
    if (roleDto != null && CollectionUtils.isNotEmpty(roleDto.getCustomRoles())) {
      for (CustomRoleDto customRoleDto : roleDto.getCustomRoles()) {
        ResponseEntity<Object> response = permissionServiceClient.createRole(customRoleDto);
        if (!response.getStatusCode().is2xxSuccessful()) {
          throw InternalErrorException.builder()
              .details("Role could not be created for role name : " + customRoleDto.getRoleName()).build();
        }
        rolesToAdd.add(customRoleDto.getRoleName());
      }
    }
    if (roleDto != null && CollectionUtils.isNotEmpty(roleDto.getExistingRoles())) {
      rolesToAdd.addAll(roleDto.getExistingRoles());
    }
    return rolesToAdd;
  }

  private List<AccessResources> createAccessGroups(AccessGroupDto userAccessGroupDto) {
    List<AccessResources> accessResourcesList = new ArrayList<>();
    if (userAccessGroupDto != null && CollectionUtils.isNotEmpty(userAccessGroupDto.getCustomAccessGroupDtos())) {
      accessResourcesList = createCustomAccessGroups(userAccessGroupDto.getCustomAccessGroupDtos());
    }
    return accessResourcesList;
  }

  private List<AccessResources> createCustomAccessGroups(List<CustomAccessGroupDto> customAccessGroups) {
    List<AccessResources> accessResources = new ArrayList<>();
    for (CustomAccessGroupDto customAccessGroupDto : customAccessGroups) {
      AccessResources accessResource = userAssembler.toAccessResources(customAccessGroupDto);
      ResponseEntity<AccessGroupResponseResource> responseEntity =
          accessGroupFeignClient.createAccessGroup(customAccessGroupDto);
      if (!responseEntity.getStatusCode().is2xxSuccessful()) {
        throw InternalErrorException.builder()
            .details("AccessGroup could not be created for access group name : " + customAccessGroupDto.getName())
            .build();
      }
      accessResource.setAccessGroupId(responseEntity.getBody().getCode());
      accessResources.add(accessResource);
    }
    return accessResources;
  }

  @Override
  public UsersListResponseResource getUsers(String loginName, String resourceType, String resourceId, String query,
      String ownerType, String ownerId, String application, Integer page, Integer pageSize,
      MutableBoolean isPartialSuccess) {
    List<UserResponseResource> userResponseResourceList = new ArrayList<>();
    long count = 0L;
    String loggedInUserName = " ";
    if (CommonThreadLocal.getAuthLocal() != null) {
      loggedInUserName = CommonThreadLocal.getAuthLocal().getUserName();
    }
    if (loginName != null) {
      UserResponseResource userResponseResource = userProvisioningUtils.getUserByLoginName(loginName, null);
      List<UserAccessGroupMappingDao> userAccessGroupDaoList;
      if (userResponseResource != null && StringUtils.isNotBlank(resourceId) && StringUtils.isNotBlank(resourceType)) {
        userAccessGroupDaoList = userAccessGroupMapppingRepository
            .findByLoginNameAndResourceTypeAndResourceId(loginName, resourceType, resourceId);
      } else if (userResponseResource != null && StringUtils.isNotBlank(resourceType)) {
        userAccessGroupDaoList =
            userAccessGroupMapppingRepository.findByLoginNameAndResourceType(loginName, resourceType, application);
      } else {
        userAccessGroupDaoList = userAccessGroupMapppingRepository.findByLoginName(loginName, application);
      }
      if (userResponseResource != null) {
        userResponseResource
            .setAccessResources(userProvisioningUtils.getAccessResourcesFromDao(userAccessGroupDaoList));
        userProvisioningUtils.populateApplicationName(loginName, userResponseResource);
      }
      userProvisioningUtils.setLegalEntityNameAndCreatedUpdatedDates(userResponseResource);
      if (StringUtils.equals(CommonThreadLocal.getAuthLocal().getApplication(), DataConstants.SKRILL)
          || StringUtils.equals(CommonThreadLocal.getAuthLocal().getApplication(), DataConstants.NETELLER)) {
        userProvisioningUtils.populateSkrillPermissionsWithIds(userResponseResource);
        userProvisioningUtils.populateWalletNames(Collections.singletonList(userResponseResource));
      }
      userResponseResourceList.add(userResponseResource);
    } else if (StringUtils.isNotBlank(query) && StringUtils.isNotBlank(resourceId)
        && StringUtils.isNotBlank(resourceType)) {
      Page<UserAccessGroupMappingDao> userAccessGroupDaoPage = userAccessGroupMapppingRepository
          .smartSearchUsers(resourceType, resourceId, query, loggedInUserName, PageRequest.of(page, pageSize));
      userProvisioningUtils.populateUserResponseList(userAccessGroupDaoPage, userResponseResourceList,
          isPartialSuccess);
      count = userAccessGroupDaoPage.getTotalElements();
    } else if (StringUtils.isNotBlank(resourceId) && StringUtils.isNotBlank(resourceType)) {
      Page<UserAccessGroupMappingDao> userAccessGroupDaoPage =
          userAccessGroupMapppingRepository.findByResourceTypeAndResourceIdOrderByCreatedDateDesc(resourceType,
              resourceId, PageRequest.of(page, pageSize));
      userProvisioningUtils.populateUserResponseList(userAccessGroupDaoPage, userResponseResourceList,
          isPartialSuccess);
      count = userAccessGroupDaoPage.getTotalElements();
    } else if (StringUtils.isNotBlank(ownerType) && StringUtils.isNotBlank(ownerId) && StringUtils.isBlank(query)) {
      String applicationFromThreadLocal = userProvisioningUtils.checkAndRetrieveApplicationFromTHreadLocal();
      Page<User> userPage =
          usersRepository.findByOwnerTypeAndOwnerIdAndApplicationAndLoginNameNotOrderByLastModifiedDateDesc(ownerType,
              ownerId, applicationFromThreadLocal, loggedInUserName, PageRequest.of(page, pageSize));
      populateUserResponseListFromUserEntities(userPage, userResponseResourceList, null);
      count = userPage.getTotalElements();
    } else if (StringUtils.isNotBlank(ownerType) && StringUtils.isNotBlank(ownerId) && StringUtils.isNotBlank(query)) {
      String applicationFromThreadLocal = userProvisioningUtils.checkAndRetrieveApplicationFromTHreadLocal();
      Page<User> userPage = usersRepository.smartSearchUsers(applicationFromThreadLocal, ownerType, ownerId, query,
          loggedInUserName, PageRequest.of(page, pageSize));
      populateUserResponseListFromUserEntities(userPage, userResponseResourceList, null);
      count = userPage.getTotalElements();
    } else if (StringUtils.isNotBlank(application)) {
      Page<User> userPage =
          usersRepository.findByApplicationOrderByLastModifiedDateDesc(application, PageRequest.of(page, pageSize));
      populateUserResponseListFromUserEntities(userPage, userResponseResourceList, application);
      count = userPage.getTotalElements();
    }
    if (CommonThreadLocal.getAuthLocal() != null
        && (StringUtils.equals(CommonThreadLocal.getAuthLocal().getApplication(), DataConstants.PORTAL)
        || StringUtils.equals(CommonThreadLocal.getAuthLocal().getApplication(), DataConstants.PARTNER_PORTAL))) {
      userProvisioningUtils.populateAccessResourcesGetUsers(userResponseResourceList);
    }
    UsersListResponseResource usersListResponseResource = new UsersListResponseResource();
    usersListResponseResource.setUsers(userResponseResourceList);
    usersListResponseResource.setCount(count);
    return usersListResponseResource;
  }

  private void populateUserResponseListFromUserEntities(Page<User> userPage,
      List<UserResponseResource> userResponseResourceList, String application) {
    List<User> users = new ArrayList<>();
    if (userPage != null) {
      users = userPage.getContent();
    }
    if (CollectionUtils.isNotEmpty(users)) {
      Map<String, User> userInfo = users.stream()
              .collect(Collectors.toMap(User::getLoginName, Function.identity(), (key1, key2) -> key1,
                      LinkedHashMap::new));

      List<UserResponseResource> userResponseList =
          userProvisioningUtils.getIdmUsers(userInfo.values().stream().map(User::getLoginName)
              .collect(Collectors.toList()), application);

      userResponseList.forEach(response -> {
        User user = userInfo.get(response.getUserName());
        response.setAccessResources(
                userProvisioningUtils.getAccessResourcesFromDao(user.getAccessGroupMappingDaos()));
        response.setApplication(user.getApplication());
        response.setCreatedDate(user.getCreatedDate());
        response.setLastModifiedDate(user.getLastModifiedDate());
        response.setCreatedBy(user.getCreatedBy());
        response.setId(user.getUserId());
        response.setMfaEnabled(StringUtils.equalsIgnoreCase(user.getMfaEnabled(), "Y"));
        userResponseResourceList.add(response);
      });
    }
  }

  private void saveAccessResouceInfo(List<String> accessGroupIds, AccessResourceStatus accessResourceStatus,
      UserProvisioningUserResource userResponse, OwnerInfo ownerInfo) {
    User user = checkAndCreateUser(userResponse, ownerInfo);
    for (String accessGroupId : accessGroupIds) {
      ResponseEntity<AccessGroupResponseResource> accessGroupHttpResponse =
          accessGroupFeignClient.fetchAccessGroupByCode(accessGroupId);
      if (accessGroupHttpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
        LOGGER.error("Invalid access group id");
        throw BadRequestException.builder().details("Invalid access group id").build();
      } else {
        AccessGroupResponseResource accessGroupResponse = accessGroupHttpResponse.getBody();
        UserAccessGroupMappingDao userAccessGroupMappingDao = user.getAccessGroupMappingDaos().stream()
            .filter(mappingDao -> userResponse.getId().equals(mappingDao.getUserId())
                && accessGroupResponse.getMerchantId().equals(mappingDao.getResourceId())
                && accessGroupResponse.getMerchantType().equals(mappingDao.getResourceType()))
            .findAny().orElse(null);
        if (userAccessGroupMappingDao != null) {
          UserManagmentUtil.toUserAccessGroupMappingDao(accessGroupResponse, userResponse, userAccessGroupMappingDao);
        } else {
          userAccessGroupMappingDao = userAssembler
              .toUserAccessGroupMappingDao(UserManagmentUtil.toAccessResources(accessGroupResponse), userResponse);
          user.getAccessGroupMappingDaos().add(userAccessGroupMappingDao);
        }
        userAccessGroupMappingDao.setUserAccessGroupStatus(accessResourceStatus);
      }
    }
    if (!accessGroupIds.isEmpty()) {
      usersRepository.save(user);
    }
  }

  private User checkAndCreateUser(UserProvisioningUserResource userResponse, OwnerInfo ownerInfo) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    UsersSummary usersSummary =
        new UsersSummary(userResponse.getUserName(), userResponse.getUserSummary(), userResponse.getId(), application);
    usersSummaryRepository.save(usersSummary);
    User user = usersRepository.findByUserId(userResponse.getId());
    return UserManagmentUtil.constructUserEntity(Optional.ofNullable(user), userResponse, ownerInfo);
  }

  @Override
  public String changePassword(String userId, ChangePasswordRequestResource changePasswordRequestResource)
      throws JsonProcessingException {
    changePasswordRequestResource.setPassword(changePasswordRequestResource.getPassword().trim());
    changePasswordRequestResource.setNewPassword(changePasswordRequestResource.getNewPassword().trim());
    AuthorizationInfo authorizationInfo = CommonThreadLocal.getAuthLocal();
    if (authorizationInfo != null && (authorizationInfo.getApplication().equals(PORTAL_APPLICATION)
        || authorizationInfo.getApplication().equals(DataConstants.LOGIN_APP))) {
      userPasswordManagementUtil.updatePasswordInPegasus(userId, changePasswordRequestResource.getNewPassword(),
          AuditEventType.CHANGE_PASSWORD);
    }
    userPasswordManagementUtil.updatePasswordInOkta(userId, changePasswordRequestResource);
    String userName = userId;
    IdentityManagementUserResource userResource = null;
    try {
      userResource = fetchUser(userId);
      userName = userResource.getUserName();
    } catch (Exception e) {
      LOGGER.error("Error while fetching user for audit entry: {}.", e.getMessage());
    }
    auditService.createAuditEntry(AuditUserEventDto.builder().eventType(AuditEventType.CHANGE_PASSWORD)
        .targetUserName(userName).createdBy(userName).eventStatus(AuditEventStatus.SUCCESS)
        .targetUserId(userResource != null ? userResource.getId() : null)
        .eventData(
            UserManagmentUtil.contructEventData("Change password", userName + " has changed the password successfully"))
        .eventTimeStamp(DateTime.now(DateTimeZone.UTC)).build());
    return userId;
  }

  @Override
  public String resetPassword(String uuid, ResetPasswordRequestResource resetPasswordRequestResource,
      String application) throws JsonProcessingException {
    IdentityManagementUserResource userResource = fetchUser(uuid);
    UserStatus initialUserStatus = userResource.getStatus();
    IdentityManagementUpdateUserResource userUpdationResource = new IdentityManagementUpdateUserResource();
    userUpdationResource.setPassword(resetPasswordRequestResource.getNewPassword());
    identityManagementFeignClient.updateUser(uuid, userUpdationResource);
    userProvisioningUtils.updateUserStatusInDatabase(uuid);
    userResource = fetchUser(uuid);
    if (StringUtils.isNotEmpty(application)
        && (application.equals(PORTAL_APPLICATION) || application.equals(DataConstants.LOGIN_APP))) {
      userPasswordManagementUtil.updatePasswordInPegasus(userResource.getUserName(),
          resetPasswordRequestResource.getNewPassword(), AuditEventType.RESET_PASSWORD);
    }
    UpdateTokenResource updateTokenResource = new UpdateTokenResource();
    updateTokenResource.setTimeToLiveInSeconds(0L);
    updateTokenResource.setTokenType(TokenType.PASSWORD_RECOVERY);
    tokenService.expireToken(updateTokenResource, userResource, resetPasswordRequestResource.getValidationToken());

    if (userResource != null && userResource.getStatus() != null) {
      if (initialUserStatus.equals(UserStatus.PROVISIONED) && userResource.getStatus().equals(UserStatus.ACTIVE)) {
        auditService.createAuditEntry(AuditUserEventDto.builder().eventType(AuditEventType.SIGNUP)
            .targetUserName(userResource.getUserName()).targetUserId(userResource.getId())
            .eventStatus(AuditEventStatus.SUCCESS).createdBy(userResource.getEmail())
            .eventData(UserManagmentUtil.contructEventData("Sign up",
                userResource.getEmail() + " has set the password successfully"))
            .eventTimeStamp(DateTime.now(DateTimeZone.UTC)).userStatus(userResource.getStatus().name()).build());
      } else {
        auditService.createAuditEntry(AuditUserEventDto.builder().eventType(AuditEventType.RESET_PASSWORD)
            .targetUserName(userResource.getUserName()).targetUserId(userResource.getId())
            .eventStatus(AuditEventStatus.SUCCESS).createdBy(userResource.getEmail())
            .eventData(UserManagmentUtil.contructEventData("Reset password",
                userResource.getEmail() + " has set the password successfully"))
            .eventTimeStamp(DateTime.now(DateTimeZone.UTC)).userStatus(userResource.getStatus().name()).build());
      }
    }
    return uuid;
  }

  @Override
  public UsersListResponseResource getUsersByFilters(String application, String userIdentifier,
      UserStatusFilter userStatus, String role, List<String> roles, String createdBy, DateTime createdDate,
      ResourceType resourceType, String resourceId, OwnerType userType, Integer page, Integer pageSize,
      boolean merchantTypeValidation, boolean ignoreDisabledBrandCheck) {
    UsersListResponseResource usersListResponseResource = new UsersListResponseResource();
    if (StringUtils.isEmpty(application) && CommonThreadLocal.getAuthLocal() != null) {
      application = CommonThreadLocal.getAuthLocal().getApplication();
    }
    Status status = userFilterUtil.mapUserStatusFilter(userStatus);
    Specification<User> userSpec =
        checkApplicationAndConstructSpecification(UserManagmentUtil.createUserFetchByFiltersRequestDto(
            UserFetchByFiltersRequestDto.builder().application(application).userIdentifier(userIdentifier)
                .status(status).role(role).merchantTypeValidation(merchantTypeValidation).build(),
            roles, createdBy, createdDate, resourceType, resourceId, userType), ignoreDisabledBrandCheck, false);
    if (userSpec == null) {
      usersListResponseResource.setCount(0L);
      usersListResponseResource.setUsers(new ArrayList<>());
    } else {
      executeSpecAndConstructResponseList(userSpec, page, pageSize, application, usersListResponseResource);
    }
    return usersListResponseResource;
  }

  private void executeSpecAndConstructResponseList(Specification<User> userSpec, Integer page, Integer pageSize,
      String application, UsersListResponseResource usersListResponseResource) {
    Page<User> usersPage;
    try {
      PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("lastModifiedDate").descending());
      usersPage = usersRepository.findAll(userSpec, pageRequest);
      List<UserResponseResource> userResourceList = new ArrayList<>();
      populateUserResponseListFromUserEntities(usersPage, userResourceList, application);
      if (Objects.equals(application, DataConstants.PORTAL)
          || Objects.equals(application, DataConstants.PARTNER_PORTAL)) {
        userProvisioningUtils.populateAccessResourcesAndLegalEntity(userResourceList);
      } else {
        userProvisioningUtils.populateAccessResourcesForSkrillTeller(userResourceList);
        userProvisioningUtils.populateWalletNames(userResourceList);
      }
      usersListResponseResource.setUsers(userResourceList);
      usersListResponseResource.setCount(usersPage.getTotalElements());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException.Builder().cause(e).errorCode(CommonErrorCode.INVALID_FIELD)
          .details("Invalid pagination request. Details: " + e.getMessage()).build();
    }
  }

  @Override
  public Specification<User> checkApplicationAndConstructSpecification(
      UserFetchByFiltersRequestDto userFetchByFiltersRequestDto, boolean ignoreDisabledBrandCheck,
      boolean isEqualSearch) {
    Specification<User> userSpec;
    if (StringUtils.isNotEmpty(userFetchByFiltersRequestDto.getResourceType())
        && userProvisioningUtils.isResourceTypeUser(userFetchByFiltersRequestDto)) {
      userFetchByFiltersRequestDto.setUserIdentifier(userFetchByFiltersRequestDto.getResourceId());
    }
    if (Objects.equals(userFetchByFiltersRequestDto.getApplication(), DataConstants.PORTAL)
        || Objects.equals(userFetchByFiltersRequestDto.getApplication(), DataConstants.PARTNER_PORTAL)) {
      if (StringUtils.isNotEmpty(userFetchByFiltersRequestDto.getResourceType())
          && !userProvisioningUtils.isResourceTypeUser(userFetchByFiltersRequestDto)) {
        populateLoginNamesFromAccessGroups(userFetchByFiltersRequestDto, isEqualSearch);
        if (CollectionUtils.isEmpty(userFetchByFiltersRequestDto.getLoginNames())) {
          return null;
        }
      }
      userSpec = userSpecification.constructPortalUsersSpecification(userFetchByFiltersRequestDto);
    } else {
      if (StringUtils.isNotEmpty(userFetchByFiltersRequestDto.getResourceType())
          && userProvisioningUtils.isResourceTypeUser(userFetchByFiltersRequestDto)) {
        userFetchByFiltersRequestDto.setResourceId(StringUtils.EMPTY);
      }
      checkAndAddDisabledBrandFilter(userFetchByFiltersRequestDto, ignoreDisabledBrandCheck);
      userSpec =
          skrillTellerUserSpecification.constructFetchSkrillTellerUsersSpecification(userFetchByFiltersRequestDto);
    }
    return userSpec;
  }

  @Override
  public ResponseEntity<HttpStatus> resetFactor(String userId, String application) {
    ResponseEntity<HttpStatus> response;
    User user = usersRepository.findByUserExternalId(userId);
    try {
      response = identityManagementFeignClient.resetFactor(userId, application);
    } catch (Exception e) {
      LOGGER.error("Couldn't reset factor for userId, received error response.", e);
      throw e;
    }
    mailService.sendResetMfaStatusEmail(user);
    return new ResponseEntity<>(response.getStatusCode());
  }

  @Override
  public void updateMfaStatus(List<String> userIds, boolean mfaEnabled) {
    for (String userId : userIds) {
      User user = usersRepository.findByUserId(userId);
      String application = CommonThreadLocal.getAuthLocal().getApplication();
      boolean isMfa = false;
      try {
        String mfaGroupId = oktaAppConfig.getGroupIds().get(DataConstants.MFA_GROUP);
        if (mfaEnabled) {
          identityManagementFeignClient.addUserToGroup(mfaGroupId, user.getUserExternalId());
          usersRepository.updateMfaStatus(userId, "Y");
          isMfa = true;
        } else {
          identityManagementFeignClient.resetFactor(user.getUserExternalId(), application);
          identityManagementFeignClient.removeUserFromGroup(mfaGroupId, user.getUserExternalId());
          usersRepository.updateMfaStatus(userId, "N");
        }
      } catch (Exception e) {
        LOGGER.error("Couldn't add or remove the user, received error response.", e);
        throw e;
      }
      mailService.sendMfaStatusUpdate(user, isMfa);
    }
  }

  private void checkAndAddDisabledBrandFilter(UserFetchByFiltersRequestDto userFetchByFiltersRequestDto,
      boolean ignoreDisabledBrandCheck) {
    if (!ignoreDisabledBrandCheck) {
      userFetchByFiltersRequestDto.setDisabledBrands(skrillTellerConfig.getDisabledBrands());
    }
  }

  private void populateLoginNamesFromAccessGroups(UserFetchByFiltersRequestDto userFetchByFiltersRequestDto,
      boolean isEqualSearch) {
    userFilterUtil.getUsersByFilters(userFetchByFiltersRequestDto, isEqualSearch);
  }

  @Override
  public void sendUserActivationEmail(String userId) throws JsonProcessingException {
    IdentityManagementUserResource idmResponse = fetchUser(userId);
    UserProvisioningUserResource userResponse = new UserProvisioningUserResource();
    BeanUtils.copyProperties(idmResponse, userResponse);
    AuditUserEventDtoBuilder auditUserEventDtoBuilder =
        AuditUserEventDto.builder().eventTimeStamp(DateTime.now(DateTimeZone.UTC))
            .eventType(AuditEventType.SEND_REACTIVATION_LINK).targetUserId(idmResponse.getId())
            .targetUserName(idmResponse.getUserName()).userStatus(idmResponse.getStatus().name());
    if (!UserStatus.PENDING_USER_ACTION.equals(userResponse.getStatus())
        && !UserStatus.PROVISIONED.equals(userResponse.getStatus())) {
      auditUserEventDtoBuilder.eventStatus(AuditEventStatus.FAILED);
      auditService.createAuditEntry(auditUserEventDtoBuilder.build());
      LOGGER.warn("Trying to send activation mail for user in non-pending status with login name : {}",
          userResponse.getUserName());
      throw new BadRequestException.Builder().details("User is not in PENDING status")
          .errorCode(CommonErrorCode.UNSUPPORTED_OPERATION).build();
    }
    Optional<UsersSummary> optinalUserSummary = usersSummaryRepository.findById(userResponse.getId());
    if (optinalUserSummary.isPresent()) {
      UsersSummary usersSummary = optinalUserSummary.get();
      userResponse.setUserSummary(usersSummary.getUserSummary());
      userProvisioningUtils.populateAccessResources(userResponse);
      if (userProvisioningConfig.isMailNotificationsEnabled()) {
        mailService.sendRegistrationConfirmationEmail(userResponse);
      }
    }
    auditUserEventDtoBuilder.eventStatus(AuditEventStatus.SUCCESS);
    auditUserEventDtoBuilder.eventData(UserManagmentUtil.contructEventData("Resend email",
        UserManagmentUtil.getUserNameFromThreadLocal() + " triggered re-activation link to " + idmResponse.getEmail()));
    auditUserEventDtoBuilder.createdBy(UserManagmentUtil.getUserNameFromThreadLocal());
    auditService.createAuditEntry(auditUserEventDtoBuilder.build());
  }

  @Override
  public void validateLoginNameAndEmailAvailability(String loginName, String emailId) {
    userProvisioningUtils.validateLoginNameAndEmailAvailability(loginName, emailId);
  }

  @Override
  public List<String> getUserAccessGroupIds(String userName, String application) {
    return userAccessGroupMapppingRepository.getUserAccessGroupIds(userName, AccessResourceStatus.ACTIVE, application);
  }

  @Override
  public ResponseEntity<ByteArrayResource> downloadUserEmails(String application) {
    List<String> emails = usersRepository.getEmailsByApplication(application);
    String headerAndEmails = "Email\n";
    headerAndEmails = headerAndEmails.concat(String.join("\n", emails));
    byte[] data = headerAndEmails.getBytes();
    ByteArrayResource resource = new ByteArrayResource(data);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + application.toLowerCase() + "_user_list.csv")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(data.length)
        .body(resource);
  }
}
