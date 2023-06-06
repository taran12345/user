// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.enums.BusinessUnit;
import com.paysafe.upf.user.provisioning.enums.OwnerType;
import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.enums.UserStatusFilter;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.UserHandlerService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.service.WalletService;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.resource.ChangePasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.OktaOneTimeVerificationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetPasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserDataSyncResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserPasswordMigrationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserUpdationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersBusinessUnitUpdateResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.WalletInfoResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.WalletUserCountResource;
import com.paysafe.upf.user.provisioning.web.rest.validator.UserDetailsRequestValidator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping({"/admin/user-provisioning/v1/", "/user-provisioning/v1/"})
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private UserAssembler userAssembler;

  @Autowired
  private UserDetailsRequestValidator userDetailsRequestValidator;

  @Autowired
  private WalletService walletService;

  @Autowired
  private UserHandlerService userHandlerService;

  /**
   * API to create user.
   * 
   */
  @PostMapping(value = "users", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Create user")
  public ResponseEntity<UserProvisioningUserResource> createUser(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @ApiParam(value = "user resource", required = true) @Valid @RequestBody UserResource userResource)
      throws JsonProcessingException {
    logger.debug("Received request for user creation for username: {}", userResource.getUserName());

    String userBusinessUnit = userResource.getBusinessUnit();
    if (StringUtils.isNotEmpty(userBusinessUnit) && CommonThreadLocal.getAuthLocal() != null) {
      AuthorizationInfo authInfo = CommonThreadLocal.getAuthLocal();
      authInfo.setBusinessUnit(userBusinessUnit);
      CommonThreadLocal.setAuthLocal(authInfo);
    }

    userDetailsRequestValidator.valiadteCreateUserRequest(userResource);

    if (CommonThreadLocal.getAuthLocal() != null
        && StringUtils.isNotEmpty(CommonThreadLocal.getAuthLocal().getApplication())) {
      userResource.setApplicationName(CommonThreadLocal.getAuthLocal().getApplication());
    } else if (StringUtils.isNotEmpty(application)) {
      userResource.setApplicationName(application);
    }

    return new ResponseEntity<>(userService.createUser(userAssembler.toUserCreationDto(userResource)),
        HttpStatus.CREATED);
  }

  /**
   * API to update user.
   * 
   * @throws JsonProcessingException e.
   */
  @PutMapping(value = "/users/{userId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Update user")
  public ResponseEntity<UserProvisioningUserResource> updateUser(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application, @PathVariable String userId,
      @ApiParam(value = "user resource", required = true) @Valid @RequestBody UserUpdationResource userUpdationResource)
      throws JsonProcessingException {
    userDetailsRequestValidator.validateUpdateUserRequest(userUpdationResource);
    if (CommonThreadLocal.getAuthLocal() != null
        && StringUtils.isNotEmpty(CommonThreadLocal.getAuthLocal().getApplication())) {
      userUpdationResource.setApplicationName(CommonThreadLocal.getAuthLocal().getApplication());
    }
    logger.debug("Received request for user update for username: {}", userUpdationResource.getUserName());
    return new ResponseEntity<>(userService.updateUser(userId, userAssembler.toUpdationDto(userUpdationResource)),
        HttpStatus.OK);
  }

  /**
   * API to fetch user.
   *
   * @return UserResource userResource
   */
  @GetMapping(value = "/users/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Fetch User")
  public ResponseEntity<IdentityManagementUserResource> fetchUser(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application, @PathVariable String userId) {
    return new ResponseEntity<>(userService.fetchUser(userId), HttpStatus.OK);
  }

  /**
   * API to fetch users based on filters.
   *
   * @return UserResource userResource
   */
  @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Fetch Users")
  public ResponseEntity<UsersListResponseResource> getUsers(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @RequestParam(value = "loginName", required = false) String loginName,
      @RequestParam(value = "resourceName", required = false) String resourceName,
      @RequestParam(value = "resourceId", required = false) String resourceId,
      @RequestParam(value = "ownerType", required = false) String ownerType,
      @RequestParam(value = "ownerId", required = false) String ownerId,
      @RequestParam(value = "query", required = false) String query,
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "20") Integer pageSize) {
    MutableBoolean isPartialSuccess = new MutableBoolean(false);
    UsersListResponseResource usersListResource = userService.getUsers(loginName, resourceName, resourceId, query,
        ownerType, ownerId, application, page, pageSize, isPartialSuccess);
    if (isPartialSuccess.booleanValue()) {
      return new ResponseEntity<>(usersListResource, HttpStatus.PARTIAL_CONTENT);
    } else {
      return new ResponseEntity<>(usersListResource, HttpStatus.OK);
    }
  }

  /**
   * API to fetch users count of a wallet.
   *
   * @return UserResource userResource
   */
  @GetMapping(value = "/users/count", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Fetch Users")
  public ResponseEntity<WalletUserCountResource> getWalletUsers(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @RequestParam(value = "resourceName", required = false) String resourceName,
      @RequestParam(value = "resourceId", required = false) String resourceId) {
    return new ResponseEntity<>(userHandlerService.getUserCount(resourceName, resourceId), HttpStatus.OK);
  }

  /**
   * API to fetch bulk users count based on userName.
   *
   * @return WalletUserCountResource List
   */
  @GetMapping(value = "/users/bulkcount", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Fetch bulk users")
  public ResponseEntity<List<WalletUserCountResource>> getBulkWalletUsers(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @RequestParam(value = "userName") String loginName) {
    return new ResponseEntity<>(userHandlerService.getBulkUserCount(loginName), HttpStatus.OK);
  }

  /**
   * API to Update user mfa-Status.
   *
   * @return HttpStatus
   */
  @PatchMapping(value = "/users/mfaStatus", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "updateMfaStatus")
  public ResponseEntity<HttpStatus> updateMfaStatus(
          @RequestHeader(value = "Authorization", required = false) String auth,
          @RequestHeader(value = "Application", required = false) String application,
      @RequestParam(value = "userIds") List<String> userIds,
          @RequestParam(value = "mfaEnabled") boolean mfaEnabled) {
    userService.updateMfaStatus(userIds, mfaEnabled);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * API to migrate user.
   *
   * @return IdentityManagementCreateUserResource identityManagementCreateUserResource
   */
  @PostMapping(value = "/users/{userId}/migrate")
  @ApiOperation(value = "Migrate user", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<IdentityManagementUserResource> migrateUser(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @PathVariable(value = "userId") String userId,
      @Valid @RequestBody UserPasswordMigrationResource userPasswordMigrationResource) {
    return new ResponseEntity<>(
        userService.migrateUser(userId, userAssembler.toUserMigrationDto(userPasswordMigrationResource), application),
        HttpStatus.OK);
  }

  /**
   * changes user password in pegasus and okta.
   * 
   * @throws JsonProcessingException e.
   */
  @PostMapping(value = "users/{userId}/changePassword", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Changes user password")
  public ResponseEntity<HttpStatus> changePassword(
      @RequestHeader(value = "Application", required = false) String application,
      @RequestHeader(value = "Authorization", required = false) String auth,
      @PathVariable(value = "userId") String userId,
      @Valid @RequestBody ChangePasswordRequestResource userChangePasswordRequestResource)
      throws JsonProcessingException {
    userService.changePassword(userId, userChangePasswordRequestResource);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Resets user password in pegasus and okta.
   * 
   * @throws JsonProcessingException e.
   */
  @PostMapping(value = "users/{uuid}/resetPassword", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Resets user password")
  public ResponseEntity<HttpStatus> resetPassword(@RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @PathVariable(value = "uuid") String uuid,
      @Valid @RequestBody ResetPasswordRequestResource resetPasswordRequestResource) throws JsonProcessingException {
    userService.resetPassword(uuid, resetPasswordRequestResource, application);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping(value = "/{userId}/factors", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Reset Factor for MFA.")
  public ResponseEntity<HttpStatus> resetFactor(@NotBlank @PathVariable String userId,
      @RequestHeader(value = "Application", required = false) String applicationHeader) {
    userService.resetFactor(userId, applicationHeader);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * API to fetch users based on adminshell filters.
   *
   * @return UserResource userResource
   */
  @GetMapping(value = "/users/filter", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Filter and fetch Users")
  public ResponseEntity<UsersListResponseResource> getUsersByFilters(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @RequestParam(value = "userIdentifier", required = false) String userIdentifier,
      @RequestParam(value = "status", required = false) UserStatusFilter status,
      @RequestParam(value = "role", required = false) String role,
      @RequestParam(value = "roles", required = false) List<String> roles,
      @RequestParam(value = "createdBy", required = false) String createdBy,
      @ApiParam(value = "provide the date in dd.MM.yyyy format") @RequestParam(value = "createdDate",
          required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") DateTime createdDate,
      @RequestParam(value = "resourceName", required = false) ResourceType resourceName,
      @RequestParam(value = "resourceId", required = false) String resourceId,
      @RequestParam(value = "userType", required = false) OwnerType userType,
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "20") Integer pageSize,
      @RequestParam(value = "merchantTypeValidation", required = false) boolean merchantTypeValidation) {
    if (resourceName != null) {
      userDetailsRequestValidator.validateApplicationAndResourceNameAndId(application, resourceName, resourceId);
    }
    if (createdDate != null) {
      createdDate = new DateTime(createdDate.getYear(), createdDate.getMonthOfYear(), createdDate.getDayOfMonth(), 0, 0,
          DateTimeZone.UTC);
    }
    return new ResponseEntity<>(userService.getUsersByFilters(application, userIdentifier, status, role, roles,
        createdBy, createdDate, resourceName, resourceId, userType, page, pageSize, merchantTypeValidation, false),
        HttpStatus.OK);
  }

  /**
   * re-send user activation mail.
   * 
   * @throws JsonProcessingException e.
   */
  @PostMapping(value = "users/{userId}/activationEmail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Re-send user activation mail")
  public ResponseEntity<HttpStatus> resendActivationEmail(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @ApiParam(value = "Re-send activation email user details", required = true) @PathVariable String userId)
      throws JsonProcessingException {
    userService.sendUserActivationEmail(userId);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  /**
   * Checks user exists or not with the given userName or email.
   *
   * @param loginName loginName/userName
   * @param emailId email id
   * @return {@link HttpStatus}
   */
  @RequestMapping(method = RequestMethod.GET, value = {"users/validate"},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "checks whether a login name or email Id is available or taken")
  public ResponseEntity<HttpStatus> validateLoginNameAndEmailAvailability(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestParam(value = "loginName", required = false) String loginName,
      @RequestParam(value = "emailId", required = false) String emailId) {
    userService.validateLoginNameAndEmailAvailability(loginName, emailId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Update user status.
   *
   * @return HttpStatus
   * @throws JsonProcessingException ex.
   */
  @RequestMapping(method = RequestMethod.POST, value = {"users/{userId}/status"},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Update user status")
  public ResponseEntity<HttpStatus> updateUserStatus(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @PathVariable(value = "userId") String userId,
      @Valid @RequestBody UpdateUserStatusResource updateUserStatusResource) throws JsonProcessingException {
    if (CommonThreadLocal.getAuthLocal() != null
            && StringUtils.isNotEmpty(CommonThreadLocal.getAuthLocal().getApplication())) {
      updateUserStatusResource.setApplicationName(CommonThreadLocal.getAuthLocal().getApplication());
    }
    userDetailsRequestValidator.validateUpdateUserStatusRequest(updateUserStatusResource);
    userService.updateUserStatus(userId, updateUserStatusResource);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping(value = "/users/{walletId}/linkedWallets", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Fetch Linked Wallets")
  public ResponseEntity<List<WalletInfoResource>> getLinkedWalletsByUserId(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @PathVariable(value = "walletId") String walletId) {
    return new ResponseEntity<>(walletService.findLinkedWalletsByLinkedBrands(application, walletId), HttpStatus.OK);
  }

  /**
   * Update user status in table for suspended.
   *
   * @return HttpStatus
   * @throws JsonProcessingException ex.
   */

  @RequestMapping(value = "/suspended-event-hook", method = RequestMethod.POST)
  public ResponseEntity<HttpStatus> eventHookForSuspendedStatus(@RequestBody JsonNode oktaRequest)
      throws JsonProcessingException {
    userHandlerService.updateUserStatusAndAuditTableFromOkta(oktaRequest);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Verify event hook for suspended user status.
   *
   * @return OktaOneTimeVerificationResource
   */

  @RequestMapping(value = "/suspended-event-hook", method = RequestMethod.GET)
  public ResponseEntity<OktaOneTimeVerificationResource> eventHookForSuspendedStatus(
      @RequestHeader(value = "X-Okta-Verification-Challenge", required = false) String verification) {
    OktaOneTimeVerificationResource responseData = new OktaOneTimeVerificationResource();
    responseData.setVerification(verification);
    return new ResponseEntity<>(responseData, HttpStatus.OK);
  }

  /**
   * Update user status in table for locked_out and update audit table for login.
   *
   * @return HttpStatus
   * @throws JsonProcessingException ex.
   */

  @RequestMapping(value = "/session-start-event-hook", method = RequestMethod.POST)
  public ResponseEntity<HttpStatus> eventHookForLockedOutStatus(@RequestBody JsonNode oktaRequest)
      throws JsonProcessingException {
    userHandlerService.updateUserStatusAndAuditTableFromOkta(oktaRequest);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Verify event hook for update user status in table for locked_out and update audit table for login.
   *
   * @return OktaOneTimeVerificationResource
   */

  @RequestMapping(value = "/session-start-event-hook", method = RequestMethod.GET)
  public ResponseEntity<OktaOneTimeVerificationResource> eventHookForLockedOutStatus(
      @RequestHeader(value = "X-Okta-Verification-Challenge", required = false) String verification) {
    OktaOneTimeVerificationResource responseData = new OktaOneTimeVerificationResource();
    responseData.setVerification(verification);
    return new ResponseEntity<>(responseData, HttpStatus.OK);
  }

  /**
   * Activates the user after user creation if the user status is still STAGED.
   *
   * @return HttpStatus
   * @throws JsonProcessingException ex.
   */
  @RequestMapping(value = "/activate-event-hook", method = RequestMethod.POST)
  public ResponseEntity<HttpStatus> eventHookForActivateUser(@RequestBody JsonNode oktaRequest)
      throws JsonProcessingException {
    userHandlerService.activateUserWithOkta(oktaRequest);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Activate event hook verification endpoint.
   */
  @RequestMapping(value = "/activate-event-hook", method = RequestMethod.GET)
  public ResponseEntity<OktaOneTimeVerificationResource> eventHookForActivateUser(
      @RequestHeader(value = "X-Okta-Verification-Challenge", required = false) String verification) {
    OktaOneTimeVerificationResource responseData = new OktaOneTimeVerificationResource();
    responseData.setVerification(verification);
    return new ResponseEntity<>(responseData, HttpStatus.OK);
  }

  /**
   * API to fetch accessGroupIds of user.
   *
   * @return List of accessGroupIds
   */
  @GetMapping(value = "/users/{userId}/accessgroupids", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Fetch accessGroupIds of user")
  public ResponseEntity<List<String>> getUserAccessGroupIds(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @PathVariable(value = "userId") String userName) {

    if (StringUtils.isEmpty(auth) && StringUtils.isEmpty(application)) {
      throw BadRequestException.builder().errorCode(CommonErrorCode.INVALID_FIELD)
          .details("Authorization and application headers both can not be empty").build();
    }

    return new ResponseEntity<>(userService.getUserAccessGroupIds(userName, application), HttpStatus.OK);
  }

  /**
   * This API is used to delete the User.
   */
  @DeleteMapping(value = "/users/{userIdentifier}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Delete the user by UserIdentifier.")
  public ResponseEntity<HttpStatus> deleteUser(@PathVariable String userIdentifier,
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application) {
    userHandlerService.deleteUser(userIdentifier.trim(), application, false);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * This API is used to delete the User.
   */
  @DeleteMapping(value = "backoffice/users/{userIdentifier}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Delete the user by UserIdentifier.")
  public ResponseEntity<HttpStatus> deleteUserFromBackOffice(@PathVariable String userIdentifier,
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application) {
    userHandlerService.deleteUser(userIdentifier.trim(), application, true);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * This API is used to update the user's businessUnit.
   */
  @PatchMapping(value = "/users/business-unit", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Updates the businessUnit of the users")
  public ResponseEntity<UsersBusinessUnitUpdateResponse> updateUsersBusinessUnit(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @RequestParam(value = "loginName", required = false) String loginName,
      @RequestParam(value = "size", required = false, defaultValue = "50") Integer size,
      @RequestParam(value = "businessUnit", required = false) BusinessUnit businessUnit,
      @RequestParam(value = "updateBasedOnMasterMerTagOrUserRole",
          required = false) boolean updateBasedOnMasterMerTagOrUserRole,
      @RequestParam(value = "existingUserBusinessUnit", required = false) String existingUserBusinessUnit) {
    if (StringUtils.isNotEmpty(auth) && StringUtils.isEmpty(application)) {
      application = CommonThreadLocal.getAuthLocal().getApplication();
    }
    return new ResponseEntity<>(userHandlerService.updateUsersBusinessUnit(application, loginName, businessUnit, size,
        updateBasedOnMasterMerTagOrUserRole, existingUserBusinessUnit), HttpStatus.OK);
  }

  /**
   * Api to download the user emails in csv file.
   */
  @GetMapping(value = "backoffice/users/email/download", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation("Api to download the user emails in csv file")
  public ResponseEntity<ByteArrayResource> downloadUserEmails(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application") String application, HttpServletResponse response) {
    return userService.downloadUserEmails(application);
  }

  /**
   * This API is used to sync the data mismatches between okta to users db entries.
   */
  @PatchMapping(value = "/users/data-sync", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Updates the businessUnit of the users")
  public ResponseEntity<UserDataSyncResponseResource> syncUserFieldsFromOktaToUserDb(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @RequestParam(value = "loginName", required = false) String loginName,
      @RequestHeader(value = "ownerId", required = false) String ownerId,
      @RequestParam(value = "ownerType", required = false) String ownerType,
      @RequestParam(value = "size", required = false, defaultValue = "50") Integer size) {
    if (StringUtils.isNotEmpty(auth) && StringUtils.isEmpty(application)) {
      application = CommonThreadLocal.getAuthLocal().getApplication();
    }
    return new ResponseEntity<>(userHandlerService.syncOktaToUsersDb(application, loginName, ownerId, ownerType, size),
        HttpStatus.OK);
  }

  /**
   * This API is used to update user status in okta and user db based on wallet status.
   */
  @PatchMapping(value = "/users/status-update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Updates the status of the users")
  public ResponseEntity<HttpStatus> updateStausInOktaAndUserDb(
          @RequestHeader(value = "Authorization", required = false) String auth,
          @RequestParam(value = "loginName", required = false) String userId,
          @RequestHeader(value = "Application", required = false) String application,
          @RequestParam(value = "status", required = false) Status status) {
    if (StringUtils.isEmpty(application)) {
      application = CommonThreadLocal.getAuthLocal().getApplication();
    }
    userHandlerService.updateStausInOktaAndUserDb(application, status, userId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
