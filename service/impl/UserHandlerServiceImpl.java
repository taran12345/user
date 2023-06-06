// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.op.errorhandling.exceptions.OneplatformException;
import com.paysafe.upf.user.provisioning.domain.AuditUserEvent;
import com.paysafe.upf.user.provisioning.domain.BulkUsers;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.BusinessUnit;
import com.paysafe.upf.user.provisioning.enums.OwnerType;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.enums.UserAction;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.migration.domain.SkrillTellerMigrationUser;
import com.paysafe.upf.user.provisioning.migration.repository.SkrillTellerMigrationRepository;
import com.paysafe.upf.user.provisioning.repository.AuditUserEventRepository;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UserAssignedApplicationsRepository;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.ModuleService;
import com.paysafe.upf.user.provisioning.service.UserHandlerService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.utils.UserManagmentUtil;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.BusinessUnitUpdateStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.DataSyncResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.OktaEventHookResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.OktaEventHookResource.Event;
import com.paysafe.upf.user.provisioning.web.rest.resource.OktaEventHookResource.Outcome;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserDataSyncResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserUpdationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersBusinessUnitUpdateResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.WalletUserCountResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserHandlerServiceImpl implements UserHandlerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserHandlerServiceImpl.class);

  private static final String EVENT_USER_SUSPENDED = "user.lifecycle.suspend";

  private static final String EVENT_USER_SESSION_START = "user.session.start";

  private static final String EVENT_USER_LOCKED_OUT = "LOCKED_OUT";

  private static final String EVENT_OUTCOME_FAILURE = "FAILURE";

  @Autowired
  private AuditUserEventRepository auditUserEventRepository;

  @Autowired
  private UserProvisioningUtils userProvisioningUtils;

  @Autowired
  private UserAssignedApplicationsRepository userAssignedApplicationsRepository;

  @Autowired
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Autowired
  private UsersRepository usersRepository;

  @Autowired
  private UserAssembler userAssembler;

  @Autowired
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Autowired
  private SkrillTellerMigrationRepository skrillTellerMigrationRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private ModuleService moduleService;

  @Override
  public void updateUserStatusAndAuditTableFromOkta(JsonNode oktaRequest) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OktaEventHookResource json = objectMapper.treeToValue(oktaRequest, OktaEventHookResource.class);
    for (Event event : json.getData().getEvents()) {
      String eventType = event.getEventType();
      Outcome outcome = event.getOutcome();
      if (eventType.equals(EVENT_USER_SUSPENDED)) {
        String userExternalId = event.getTarget().get(0).getId();
        usersRepository.updateUserStatus(Status.DORMANT, userExternalId);
      }
      if (eventType.equals(EVENT_USER_SESSION_START)) {
        String userExternalId = event.getActor().getId();
        User user = usersRepository.findByUserExternalId(userExternalId);
        AuditUserEvent auditUserEvent = userAssembler.toAuditUserEvent(event, user.getApplication());
        if (null != auditUserEvent) {
          auditUserEventRepository.save(auditUserEvent);
        }
        if (outcome.getResult().equals(EVENT_OUTCOME_FAILURE) && outcome.getReason().equals(EVENT_USER_LOCKED_OUT)) {
          usersRepository.updateUserStatus(Status.LOCKED_OUT, userExternalId);
        }
      }
    }
  }

  @Override
  public void activateUserWithOkta(JsonNode oktaRequest) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OktaEventHookResource json = objectMapper.treeToValue(oktaRequest, OktaEventHookResource.class);
    for (Event event : json.getData().getEvents()) {
      identityManagementFeignClient.activateUser(event.getTarget().get(0).getId());
    }
  }

  @Override
  public WalletUserCountResource getUserCount(String resourceType, String resourceId) {
    WalletUserCountResource walletUserCountResource = new WalletUserCountResource();
    if (StringUtils.isNotBlank(resourceId) && StringUtils.isNotBlank(resourceType)) {
      long adminCount =
          userAccessGroupMapppingRepository.countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(
              resourceType, resourceId, AccessGroupType.DEFAULT_ADMIN, AccessResourceStatus.ACTIVE);
      walletUserCountResource.setUserCount(
          userAccessGroupMapppingRepository.countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(
              resourceType, resourceId, AccessGroupType.CUSTOMIZED, AccessResourceStatus.ACTIVE) + adminCount);
      walletUserCountResource.setAdminCount(adminCount);
      walletUserCountResource.setResourceId(resourceId);
      walletUserCountResource.setResourceName(resourceType);
      return walletUserCountResource;
    }
    return null;
  }

  @Override
  public List<WalletUserCountResource> getBulkUserCount(String loginName) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    List<BulkUsers> bulkUsersTotal = userAccessGroupMapppingRepository.getBulkUsersTotal(loginName,
        AccessResourceStatus.ACTIVE, DataConstants.WALLETS, application);
    Map<String, WalletUserCountResource> resourceIdWalletUserCountResourceMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(bulkUsersTotal)) {
      for (BulkUsers bulkUser : bulkUsersTotal) {
        resourceIdWalletUserCountResourceMap.put(bulkUser.getResourceId(),
            new WalletUserCountResource(DataConstants.WALLETS, bulkUser.getResourceId(), 0L, bulkUser.getUsersCount()));
      }
    }

    List<BulkUsers> bulkUsersAdmin = userAccessGroupMapppingRepository.getBulkUsersAdmin(loginName,
        AccessResourceStatus.ACTIVE, AccessGroupType.DEFAULT_ADMIN, DataConstants.WALLETS, application);
    if (CollectionUtils.isNotEmpty(bulkUsersAdmin)) {
      for (BulkUsers bulkUser : bulkUsersAdmin) {
        if (resourceIdWalletUserCountResourceMap.containsKey(bulkUser.getResourceId())) {
          WalletUserCountResource resource = resourceIdWalletUserCountResourceMap.get(bulkUser.getResourceId());
          resource.setAdminCount(bulkUser.getUsersCount());
          resourceIdWalletUserCountResourceMap.put(bulkUser.getResourceId(), resource);
        } else {
          resourceIdWalletUserCountResourceMap.put(bulkUser.getResourceId(), new WalletUserCountResource(
              DataConstants.WALLETS, bulkUser.getResourceId(), bulkUser.getUsersCount(), 0L));

        }
      }
    }
    return new ArrayList(resourceIdWalletUserCountResourceMap.values());
  }

  @Override
  @Transactional
  public void deleteUser(String userIdentifier, String application, Boolean isDeleteUserFromBo) {
    User user = null;
    if (!StringUtils.isEmpty(application)) {
      Optional<User> optionalUser = usersRepository.findByLoginNameAndApplication(userIdentifier, application);
      if (optionalUser.isPresent()) {
        user = optionalUser.get();
      }
    } else {
      user = usersRepository.findByUserId(userIdentifier);
    }
    if (user == null) {
      throw NotFoundException.builder().entityNotFound().details("User not found").build();
    }

    if (CommonThreadLocal.getAuthLocal() == null) {
      CommonThreadLocal.setAuthLocal(new AuthorizationInfo());
    }

    if (isDeleteUserFromBo && user.getStatus() != Status.PENDING_USER_ACTION
        && user.getStatus() != Status.PROVISIONED) {
      throw BadRequestException.builder().details("This user cannot be deleted").build();
    }

    CommonThreadLocal.getAuthLocal().setApplication(user.getApplication());
    identityManagementFeignClient.deleteUser(user.getUserId());

    userAssignedApplicationsRepository.deleteByUserId(user.getUserId());
    userAccessGroupMapppingRepository.deleteByUserId(user.getUserId());
    usersRepository.deleteByUserId(user.getUserId());

    SkrillTellerMigrationUser migrationUser = skrillTellerMigrationRepository.findByUserId(user.getUserId());
    if (migrationUser != null) {
      skrillTellerMigrationRepository.delete(migrationUser);
    }
  }

  @Override
  public UsersBusinessUnitUpdateResponse updateUsersBusinessUnit(String application, String loginName,
      BusinessUnit businessUnit, Integer size, boolean updateBasedOnMasterMerTagOrUserRole,
      String existingUserBusinessUnit) {
    if (StringUtils.isEmpty(application)) {
      throw BadRequestException.builder().details("Application is empty").errorCode(CommonErrorCode.INVALID_FIELD)
          .build();
    }
    List<User> users = new ArrayList<>();
    if (StringUtils.isNotEmpty(loginName)) {
      Optional<User> user = usersRepository.findByLoginNameAndApplication(loginName, application);
      if (!user.isPresent()) {
        throw BadRequestException.builder().details("Requested user not found").errorCode(CommonErrorCode.INVALID_FIELD)
            .build();
      }
      users.add(user.get());
    } else {
      Pageable pageable = PageRequest.of(0, size);
      String userBusinessUnit = StringUtils.isEmpty(existingUserBusinessUnit) ? null : existingUserBusinessUnit;
      Page<User> usersPage = usersRepository.findByApplicationAndBusinessUnitOrderByLastModifiedDateDesc(application,
          userBusinessUnit, pageable);
      if (!usersPage.isEmpty()) {
        users = usersPage.getContent();
      }
    }
    if (CollectionUtils.isEmpty(users)) {
      throw BadRequestException.builder().details("No users found to update businessUnit")
          .errorCode(CommonErrorCode.INVALID_FIELD).build();
    }
    vaildateBusinessUnitInput(businessUnit, updateBasedOnMasterMerTagOrUserRole);
    List<BusinessUnitUpdateStatusResource> businessUnitUpdateStatusList = new ArrayList<>();
    for (User user : users) {
      businessUnit = findUserBusinessUnit(user, businessUnit, updateBasedOnMasterMerTagOrUserRole);
      UserUpdationDto userUpdationDto = getUserUpdationDto(businessUnit, application);
      updateBusinessUnit(user, userUpdationDto, businessUnitUpdateStatusList);
    }
    return constructUsersBusinessUnitUpdateResponse(businessUnitUpdateStatusList);
  }

  private void vaildateBusinessUnitInput(BusinessUnit businessUnit, boolean updateBasedOnMasterMerTagOrUserRole) {
    if (!updateBasedOnMasterMerTagOrUserRole && businessUnit == null) {
      throw BadRequestException.builder().details("businessUnit value missing").errorCode(CommonErrorCode.INVALID_FIELD)
          .build();
    }
  }

  private BusinessUnit findUserBusinessUnit(User user, BusinessUnit businessUnit,
      boolean updateBasedOnMasterMerTagOrUserRole) {
    if (!updateBasedOnMasterMerTagOrUserRole) {
      return businessUnit;
    }
    return findBusinessUnitByMasterMerTagOrRoles(user, businessUnit);
  }

  private BusinessUnit findBusinessUnitByMasterMerTagOrRoles(User user, BusinessUnit businessUnit) {
    String businessTag = findBusinessUnitByMasterMerTag(user);
    if (StringUtils.isEmpty(businessTag)) {
      businessTag = findBusinessUnitByUserRole(user);
    }
    if (StringUtils.isNotEmpty(businessTag)) {
      return BusinessUnit.valueOf(businessTag);
    } else {
      return businessUnit;
    }
  }

  private String findBusinessUnitByUserRole(User user) {
    UserResponseResource userResource = userService.getUsers(user.getLoginName(), null, null, null, null, null,
        user.getApplication(), 0, 1, new MutableBoolean(false)).getUsers().get(0);
    Set<String> roles =
        userResource.getAccessResources().stream().filter(e -> !e.getRole().equals(DataConstants.REGULAR))
            .map(AccessResources::getRole).collect(Collectors.toSet());
    String businessUnit = null;
    for (String role : roles) {
      if (role.contains(DataConstants.US_IGAMING_ROLE_STRING) || role.contains(DataConstants.EU_ROLE_STRING)
          || role.contains(DataConstants.ECOMM_ROLE_STRING)) {
        businessUnit = getBusinessUnitByRole(role);
        break;
      }
    }

    return businessUnit;
  }

  private String getBusinessUnitByRole(String role) {
    if (role.contains(DataConstants.US_IGAMING_ROLE_STRING)) {
      return BusinessUnit.US_I_GAMING.toString();
    } else if (role.contains(DataConstants.EU_ROLE_STRING)) {
      return BusinessUnit.EU_ACQUIRING_EEA.toString();
    } else {
      return BusinessUnit.US_E_COMMERCE.toString();
    }
  }

  private String findBusinessUnitByMasterMerTag(User user) {
    String ownerId = user.getOwnerId();
    String ownerType = user.getOwnerType();
    if (StringUtils.isNotEmpty(ownerId) && StringUtils.isNotEmpty(ownerType)) {
      Set<String> businessTags = moduleService.getBusinessInitiativesForIds(OwnerType.valueOf(ownerType), ownerId);
      if (CollectionUtils.isNotEmpty(businessTags) && businessTags.size() == 1) {
        return businessTags.iterator().next();
      }
    }
    return null;
  }

  private UsersBusinessUnitUpdateResponse constructUsersBusinessUnitUpdateResponse(
      List<BusinessUnitUpdateStatusResource> businessUnitUpdateStatusList) {
    UsersBusinessUnitUpdateResponse response = new UsersBusinessUnitUpdateResponse();
    response.setTotalUsersCount(businessUnitUpdateStatusList.size());
    long succeedUsers = businessUnitUpdateStatusList.stream().filter(e -> e.isBusinessUnitUpdated()).count();
    response.setSucceedUsersCount(succeedUsers);
    response.setFailedUsersCount(businessUnitUpdateStatusList.size() - succeedUsers);
    response.setUsersStatus(businessUnitUpdateStatusList);
    return response;
  }

  @Async("upfAsyncExecutor")
  private void updateBusinessUnit(User user, UserUpdationDto userUpdationDto,
      List<BusinessUnitUpdateStatusResource> businessUnitUpdateStatusList) {
    boolean failed = false;
    String errorReason = null;
    String errorCode = null;
    try {
      userService.updateUser(user.getUserId(), userUpdationDto);
    } catch (OneplatformException ex) {
      failed = true;
      errorReason = (ex.getDetails() != null) ? Arrays.toString(ex.getDetails()) : "";
      errorCode = String.valueOf(ex.getStatus().value());
    } catch (Exception ex) {
      failed = true;
      errorReason = ex.getMessage();
      LOGGER.error("Error while updating the businessUnit for UserName : " + user.getLoginName());
    } finally {
      businessUnitUpdateStatusList
          .add(new BusinessUnitUpdateStatusResource(!failed, user.getLoginName(), errorReason, errorCode));
    }
  }

  private UserUpdationDto getUserUpdationDto(BusinessUnit businessUnit, String application) {
    UserUpdationResource userUpdationResource = new UserUpdationResource();
    userUpdationResource.setBusinessUnit(businessUnit.toString());
    userUpdationResource.setApplicationName(application);
    userUpdationResource.setDisableMailNotifications(true);
    return userAssembler.toUpdationDto(userUpdationResource);
  }

  @Override
  public UserDataSyncResponseResource syncOktaToUsersDb(String application, String loginName, String ownerId,
      String ownerType, Integer size) {
    if (StringUtils.isEmpty(application)) {
      throw BadRequestException.builder().details("Application is empty").errorCode(CommonErrorCode.INVALID_FIELD)
          .build();
    }
    List<User> users = new ArrayList<>();
    if (StringUtils.isNotEmpty(loginName)) {
      Optional<User> user = usersRepository.findByLoginNameAndApplication(loginName, application);
      if (!user.isPresent()) {
        throw BadRequestException.builder().details("Requested user not found").errorCode(CommonErrorCode.INVALID_FIELD)
            .build();
      }
      users.add(user.get());
    } else {
      Pageable pageable = PageRequest.of(0, size);
      String userOwnerType = StringUtils.isEmpty(ownerType) ? null : ownerType;
      String userOwnerId = StringUtils.isEmpty(ownerId) ? null : ownerId;
      Page<User> usersPage = usersRepository.findByOwnerTypeAndOwnerIdAndApplicationOrderByLastModifiedDateDesc(
          userOwnerType, userOwnerId, application, pageable);
      if (!usersPage.isEmpty()) {
        users = usersPage.getContent();
      }
    }
    if (CollectionUtils.isEmpty(users)) {
      throw BadRequestException.builder().details("No users found to sync user data from okta to user db")
          .errorCode(CommonErrorCode.INVALID_FIELD).build();
    }
    List<DataSyncResponseResource> dataSyncResponseResourceList = new ArrayList<>();
    for (User user : users) {
      syncUserData(user, dataSyncResponseResourceList, application);
    }
    return constructUsersDataSyncResponseResource(dataSyncResponseResourceList);
  }

  @Override
  public void updateStausInOktaAndUserDb(String application, Status status, String userId) {
    status = Status.valueOf(status.toString());
    if (StringUtils.isNotEmpty(userId)) {
      User user = usersRepository.findByUserId(userId);
      updateUserStatusForExistingUser(user, application);
    } else {
      int pageNumber = 0;
      Pageable pageable = PageRequest.of(pageNumber, 500);
      Page<User> usersPage = usersRepository.findByApplicationAndStatus(application, status, pageable);
      for (int i = 1; i <= usersPage.getTotalPages(); i++) {
        usersPage.getContent().forEach(user -> updateUserStatusForExistingUser(user, application));
        pageable = PageRequest.of(++pageNumber, 500);
        usersPage = usersRepository.findByApplicationAndStatus(application, status, pageable);
      }
    }
  }

  /**
   * This method is used to used to update user status in okta and database for existing users.
   */
  @Async("upfAsyncExecutor")
  public void updateUserStatusForExistingUser(User user, String application) {
    LOGGER.info("got status update request for : " + user.getLoginName());
    List<UserAccessGroupMappingDao> userAccessGroupDaoList;
    userAccessGroupDaoList =
            userAccessGroupMapppingRepository.findByLoginName(user.getLoginName(), application);
    long count = userAccessGroupDaoList.stream().filter(userAccessGroupDao ->
            AccessResourceStatus.BLOCKED.equals(userAccessGroupDao.getUserAccessGroupStatus())).count();
    if (count == userAccessGroupDaoList.size() && !userAccessGroupDaoList.isEmpty()) {
      UpdateUserStatusResource resource = new UpdateUserStatusResource();
      resource.setAction(UserAction.SUSPEND);
      resource.setEmail(user.getEmail());
      resource.setUserName(user.getLoginName());
      userProvisioningUtils.updateUserInIdmService(user.getUserId(), resource, application);
    }
    LOGGER.info("updated status successfully for : " + user.getLoginName());
  }

  @Async("upfAsyncExecutor")
  private void syncUserData(User user, List<DataSyncResponseResource> dataSyncResponseResourceList,
      String application) {
    boolean failed = false;
    String errorReason = null;
    String errorCode = null;
    try {
      IdentityManagementUserResource idmResponse = getUserInfoFromIdm(user.getLoginName(), application);
      user.setOwnerId(
          UserManagmentUtil.getUserCustomPropertiesField(idmResponse.getCustomProperties(), DataConstants.OWNER_ID));
      user.setOwnerType(
          UserManagmentUtil.getUserCustomPropertiesField(idmResponse.getCustomProperties(), DataConstants.OWNER_TYPE));
      usersRepository.save(user);
    } catch (OneplatformException ex) {
      failed = true;
      errorReason = (ex.getDetails() != null) ? Arrays.toString(ex.getDetails()) : "";
      errorCode = String.valueOf(ex.getStatus().value());
    } catch (Exception ex) {
      failed = true;
      errorReason = ex.getMessage();
      LOGGER.error("Error while syncing the user data from okta to user db for UserName : " + user.getLoginName());
    } finally {
      dataSyncResponseResourceList
          .add(new DataSyncResponseResource(!failed, user.getLoginName(), errorReason, errorCode));
    }
  }

  private IdentityManagementUserResource getUserInfoFromIdm(String loginName, String application) {
    ResponseEntity<IdentityManagementUserResource> response =
        identityManagementFeignClient.getUser(loginName, application);
    if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
      return response.getBody();
    } else {
      throw NotFoundException.builder().details("No users found").errorCode(CommonErrorCode.INVALID_FIELD).build();
    }
  }

  private UserDataSyncResponseResource constructUsersDataSyncResponseResource(
      List<DataSyncResponseResource> dataSyncResponseResourceList) {
    UserDataSyncResponseResource response = new UserDataSyncResponseResource();
    response.setTotalUsersCount(dataSyncResponseResourceList.size());
    long succeedUsers = dataSyncResponseResourceList.stream().filter(e -> e.isDataSynced()).count();
    response.setSucceedUsersCount(succeedUsers);
    response.setFailedUsersCount(dataSyncResponseResourceList.size() - succeedUsers);
    response.setUsersStatus(dataSyncResponseResourceList);
    return response;
  }
}
