// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.migration.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.op.errorhandling.exceptions.UnauthorizedException;
import com.paysafe.upf.user.provisioning.config.SkrillUserSummaryPermissionsConfig;
import com.paysafe.upf.user.provisioning.config.UserProvisioningConfig;
import com.paysafe.upf.user.provisioning.domain.HashedPasswordEntity;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingKey;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.SkrillPermissions;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.exceptions.UserProvisioningException;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.UaaFeignClient;
import com.paysafe.upf.user.provisioning.migration.domain.SkrillTellerMigrationUser;
import com.paysafe.upf.user.provisioning.migration.repository.SkrillTellerMigrationRepository;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.MerchantAccountInfoService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.utils.PasswordEncryptionUtil;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.SkrillTellerMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.inlinehooks.PasswordImportRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.inlinehooks.PasswordImportResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Credentials;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.HashedPassword;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Origin;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Profile;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.SkrillAccessResources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SkrillTellerUserServiceTest {

  @Mock
  private UserService userService;

  @Mock
  private SkrillTellerMigrationRepository skrillTellerMigrationRepository;

  @Spy
  private UserAssembler userAssembler;

  @Mock
  private UserProvisioningUtils userProvisioningUtils;

  @Mock
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Mock
  private MerchantAccountInfoService merchantAccountInfoService;

  @Mock
  private SkrillUserSummaryPermissionsConfig skrillUserSummaryPermissionsConfig;

  @Mock
  private PasswordEncryptionUtil passwordEncryptionUtil;

  @Mock
  private UserProvisioningConfig userProvisioningConfig;

  @Mock
  private UaaFeignClient uaaFeignClient;

  @InjectMocks
  private SkrillTellerUserService skrillTellerUserService;

  private SkrillTellerMigrationDto skrillTellerDto;

  private SkrillTellerMigrationUser skrillTellerMigrationUser;

  /**
   * Setup test configuration.
   *
   * @throws Exception exception
   */
  @Before
  public void setUp() {
    skrillTellerDto = getSkrillTellerDto();
    skrillTellerMigrationUser = getSkrillTellerMigrationUser();
  }

  private SkrillTellerMigrationDto getSkrillTellerDto() {
    SkrillTellerMigrationDto migrationDto = new SkrillTellerMigrationDto();
    migrationDto.setUserName("user@email.com");
    migrationDto.setEmail("user@email.com");
    migrationDto.setStatus(UserStatus.ACTIVE);
    Origin origin = new Origin();
    origin.setSite("SKRILL");
    origin.setReferenceId("referenceId");
    migrationDto.setOrigin(origin);
    Profile profile = new Profile();
    profile.setFirstName("firstName");
    profile.setLastName("lastName");
    migrationDto.setProfile(profile);
    SkrillAccessResources accessResource = new SkrillAccessResources();
    accessResource.setRole("REGULAR");
    accessResource.setStatus(AccessResourceStatus.ACTIVE);
    accessResource.setResourceType("resourceType");
    accessResource.setResourceId("resourceId");
    accessResource.setPermissions(new ArrayList<>(Arrays.asList(SkrillPermissions.BALANCES)));
    SkrillAccessResources adminAccessResource = new SkrillAccessResources();
    adminAccessResource.setRole(DataConstants.ADMIN);
    adminAccessResource.setStatus(AccessResourceStatus.ACTIVE);
    adminAccessResource.setResourceType("resourceType");
    adminAccessResource.setResourceId("resourceId");
    List<SkrillAccessResources> accessResources = new ArrayList<>();
    accessResources.add(accessResource);
    accessResources.add(adminAccessResource);
    migrationDto.setAccessResources(accessResources);
    HashedPassword hash = new HashedPassword();
    hash.setAlgorithm("MD5");
    hash.setSalt("0dfb367ba2e447add313c63645323d6b");
    hash.setValue("0dfb367ba2e447add313c63645323d6b");
    hash.setSaltOrder("PREFIX");
    Credentials credentials = new Credentials();
    credentials.setHash(hash);
    migrationDto.setCredentials(credentials);
    return migrationDto;
  }

  private SkrillTellerMigrationUser getSkrillTellerMigrationUser() {
    SkrillTellerMigrationUser userResource = new SkrillTellerMigrationUser();
    userResource.setUserName("user@email.com");
    userResource.setEmail("user@email.com");
    userResource.setOriginReferenceId("referenceId");
    userResource.setApplication("SKRILL");
    userResource.setLastLoginDate("04-08-2020");
    return userResource;
  }

  @Test
  public void migrateUser_Success() throws JsonProcessingException {
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    when(userService.createUser(any())).thenReturn(userProvisioningUserResource);
    when(skrillTellerMigrationRepository.findByUserNameAndApplication(any(), any()))
        .thenReturn(skrillTellerMigrationUser);
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, String> permissionMapping = new HashMap<>();
    permissionMapping.put(SkrillPermissions.SEND_MONEY_CUSTOMER.toString(), "Transfers - Customer");
    when(skrillUserSummaryPermissionsConfig.getPermissionMapping()).thenReturn(permissionMapping);
    skrillTellerUserService.migrateSkrillTellerUser(skrillTellerDto);
    verify(userService, times(1)).createUser(any());
    verify(skrillTellerMigrationRepository, times(1)).findByUserNameAndApplication(any(), any());
    verify(skrillTellerMigrationRepository, times(1)).save(any());
  }

  @Test
  public void migrateUser_NullAuth() throws JsonProcessingException {
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    when(userService.createUser(any())).thenReturn(userProvisioningUserResource);
    when(skrillTellerMigrationRepository.findByUserNameAndApplication(any(), any()))
        .thenReturn(skrillTellerMigrationUser);
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, String> permissionMapping = new HashMap<>();
    permissionMapping.put(SkrillPermissions.SEND_MONEY_CUSTOMER.toString(), "Transfers - Customer");
    when(skrillUserSummaryPermissionsConfig.getPermissionMapping()).thenReturn(permissionMapping);
    CommonThreadLocal.setAuthLocal(null);
    skrillTellerUserService.migrateSkrillTellerUser(skrillTellerDto);
    verify(userService, times(1)).createUser(any());
    verify(skrillTellerMigrationRepository, times(1)).findByUserNameAndApplication(any(), any());
    verify(skrillTellerMigrationRepository, times(1)).save(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void migrateUser_EmptyAccessResources() throws JsonProcessingException {
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    when(userService.createUser(any())).thenReturn(userProvisioningUserResource);
    when(skrillTellerMigrationRepository.findByUserNameAndApplication(any(), any()))
        .thenReturn(skrillTellerMigrationUser);
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, String> permissionMapping = new HashMap<>();
    permissionMapping.put(SkrillPermissions.SEND_MONEY_CUSTOMER.toString(), "Transfers - Customer");
    when(skrillUserSummaryPermissionsConfig.getPermissionMapping()).thenReturn(permissionMapping);
    skrillTellerDto.setAccessResources(new ArrayList<>());
    skrillTellerUserService.migrateSkrillTellerUser(skrillTellerDto);
    verify(userService, times(1)).createUser(any());
    verify(skrillTellerMigrationRepository, times(1)).findByUserNameAndApplication(any(), any());
    verify(skrillTellerMigrationRepository, times(1)).save(any());
  }

  @Test(expected = UserProvisioningException.class)
  public void migrateUser_AlreadyMigratedUser() throws JsonProcessingException {
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    when(userService.createUser(any())).thenReturn(userProvisioningUserResource);
    skrillTellerMigrationUser.setIsMigrated(1);
    when(skrillTellerMigrationRepository.findByUserNameAndApplication(any(), any()))
        .thenReturn(skrillTellerMigrationUser);
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, String> permissionMapping = new HashMap<>();
    permissionMapping.put(SkrillPermissions.SEND_MONEY_CUSTOMER.toString(), "Transfers - Customer");
    when(skrillUserSummaryPermissionsConfig.getPermissionMapping()).thenReturn(permissionMapping);
    skrillTellerUserService.migrateSkrillTellerUser(skrillTellerDto);
  }

  @Test
  public void migrateExistingUser_Success() throws JsonProcessingException {
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    when(userService.createUser(any())).thenReturn(userProvisioningUserResource);
    skrillTellerMigrationUser.setIsMigrated(0);
    when(skrillTellerMigrationRepository.findByUserNameAndApplication(any(), any()))
        .thenReturn(skrillTellerMigrationUser);
    skrillTellerUserService.migrateSkrillTellerUser(skrillTellerDto);
    verify(userService, times(1)).createUser(any());
    verify(skrillTellerMigrationRepository, times(1)).findByUserNameAndApplication(any(), any());
    verify(skrillTellerMigrationRepository, times(1)).save(any());
  }

  @Test
  public void migrateNewUser_Success() throws JsonProcessingException {
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    when(userService.createUser(any())).thenReturn(userProvisioningUserResource);
    when(skrillTellerMigrationRepository.findByUserNameAndApplication(any(), any())).thenReturn(null);
    skrillTellerUserService.migrateSkrillTellerUser(skrillTellerDto);
    verify(userService, times(1)).createUser(any());
    verify(skrillTellerMigrationRepository, times(1)).findByUserNameAndApplication(any(), any());
    verify(skrillTellerMigrationRepository, times(1)).save(any());
  }

  @Test(expected = InternalErrorException.class)
  public void migrateUser_failure() throws JsonProcessingException {
    when(userService.createUser(any())).thenThrow(InternalErrorException.builder().build());
    when(skrillTellerMigrationRepository.findByUserNameAndApplication(any(), any()))
        .thenReturn(skrillTellerMigrationUser);
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, String> permissionMapping = new HashMap<>();
    permissionMapping.put(SkrillPermissions.SEND_MONEY_CUSTOMER.toString(), "Transfers - Customer");
    when(skrillUserSummaryPermissionsConfig.getPermissionMapping()).thenReturn(permissionMapping);
    skrillTellerUserService.migrateSkrillTellerUser(skrillTellerDto);
    verify(userService, times(1)).createUser(any());
    verify(skrillTellerMigrationRepository, times(1)).findByUserNameAndApplication(any(), any());
    verify(skrillTellerMigrationRepository, times(1)).save(any());
    verify(merchantAccountInfoService, times(1)).getBasicWalletInfo(Mockito.anySet());
  }

  @Test
  public void getUser_Success() {
    SkrillTellerMigrationUser skrillTellerMigrationUser1 = getSkrillTellerMigrationUser();
    skrillTellerMigrationUser1.setIsMigrated(1);
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(skrillTellerMigrationUser1);
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    skrillTellerUserService.getUser("userId");
    verify(skrillTellerMigrationRepository, times(1)).findByUserId(any());
    verify(userService, times(1)).fetchUser(any());
  }

  @Test(expected = NotFoundException.class)
  public void getUser_failure() {
    SkrillTellerMigrationUser skrillTellerMigrationUser1 = getSkrillTellerMigrationUser();
    skrillTellerMigrationUser1.setIsMigrated(1);
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(null);
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    skrillTellerUserService.getUser("userId");
    verify(skrillTellerMigrationRepository, times(1)).findByUserId(any());
    verify(userService, times(1)).fetchUser(any());
  }

  @Test(expected = UserProvisioningException.class)
  public void getUser_failure_NotFound() {
    SkrillTellerMigrationUser skrillTellerMigrationUser1 = getSkrillTellerMigrationUser();
    skrillTellerMigrationUser1.setIsMigrated(2);
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(skrillTellerMigrationUser1);
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    skrillTellerUserService.getUser("userId");
    verify(skrillTellerMigrationRepository, times(1)).findByUserId(any());
    verify(userService, times(1)).fetchUser(any());
  }

  @Test
  public void searchUser_Success() {
    SkrillTellerMigrationUser skrillTellerMigrationUser1 = getSkrillTellerMigrationUser();
    skrillTellerMigrationUser1.setIsMigrated(1);
    when(skrillTellerMigrationRepository.findByUserNameAndApplication(any(), any()))
        .thenReturn(skrillTellerMigrationUser1);
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    skrillTellerUserService.searchUser("userName", "SKRILL");
    verify(skrillTellerMigrationRepository, times(1)).findByUserNameAndApplication(any(), any());
    verify(userService, times(1)).fetchUser(any());
  }

  @Test
  public void deleteUser_Success() {
    SkrillTellerMigrationUser skrillTellerMigrationUser1 = getSkrillTellerMigrationUser();
    skrillTellerMigrationUser1.setIsMigrated(1);
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(skrillTellerMigrationUser1);
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    skrillTellerUserService.deleteUser("userId");
    verify(skrillTellerMigrationRepository, times(1)).findByUserId(any());
    verify(userService, times(1)).fetchUser(any());
  }

  @Test(expected = NotFoundException.class)
  public void deleteUser_whenUserNotFound_throwsNotFoundException() {
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(null);
    skrillTellerUserService.deleteUser("userId");
  }

  @Test(expected = UserProvisioningException.class)
  public void deleteUser_whenErrorWhileMigrating_throwsException() {
    SkrillTellerMigrationUser skrillTellerMigrationUser1 = getSkrillTellerMigrationUser();
    skrillTellerMigrationUser1.setIsMigrated(2);
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(skrillTellerMigrationUser1);
    skrillTellerUserService.deleteUser("userId");
  }

  @Test
  public void updateUser_Success() throws JsonProcessingException {
    SkrillTellerMigrationUser skrillTellerMigrationUser1 = getSkrillTellerMigrationUser();
    skrillTellerMigrationUser1.setIsMigrated(1);
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(skrillTellerMigrationUser1);
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    when(userService.updateUser(any(), any())).thenReturn(userProvisioningUserResource);
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    when(userProvisioningUtils.fetchAccessResourcesForSkrillTeller(any()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    skrillTellerUserService.updateUser("userName", skrillTellerDto);
    verify(skrillTellerMigrationRepository, times(1)).findByUserId(any());
    verify(userService, times(1)).fetchUser(any());
    verify(userService, times(1)).updateUser(any(), any());
    verify(userProvisioningUtils, times(1)).fetchAccessResourcesForSkrillTeller(any());
  }

  @Test
  public void updateUser_withBlockedAccesResources_shouldSuccess() throws JsonProcessingException {
    SkrillTellerMigrationUser skrillTellerMigrationUser1 = getSkrillTellerMigrationUser();
    skrillTellerMigrationUser1.setIsMigrated(1);
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(skrillTellerMigrationUser1);
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    when(userService.updateUser(any(), any())).thenReturn(userProvisioningUserResource);
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    when(userProvisioningUtils.fetchAccessResourcesForSkrillTeller(any()))
        .thenReturn(UserTestUtility.getAccessResourcesList());
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(UserTestUtility.getUserAccessGroupMappingDao()));
    skrillTellerDto.getAccessResources().get(0).setStatus(AccessResourceStatus.BLOCKED);
    skrillTellerUserService.updateUser("userName", skrillTellerDto);
    verify(skrillTellerMigrationRepository, times(1)).findByUserId(any());
    verify(userService, times(1)).fetchUser(any());
    verify(userService, times(1)).updateUser(any(), any());
    verify(userProvisioningUtils, times(1)).fetchAccessResourcesForSkrillTeller(any());
  }

  @Test
  public void validateUserCredentialsOkta_success() {
    User user = new User();
    user.setHashedPassword(new HashedPasswordEntity());
    when(usersRepository.findByLoginNameAndApplication(any(), any())).thenReturn(Optional.ofNullable(user));
    when(passwordEncryptionUtil.compareHashedPasswords(any(), any())).thenReturn(true);

    PasswordImportRequestResource passwordImportRequestResource =
        UserTestUtility.getPasswordImportRequestResourceForTest();
    PasswordImportResponseResource response =
        skrillTellerUserService.validateUserCredentialsOkta(passwordImportRequestResource);

    assertEquals("VERIFIED", response.getCommands().get(0).getValue().getCredential());
  }

  @Test
  public void validateUserCredentialsOkta_UserNotPresent() {
    when(usersRepository.findById(any())).thenReturn(Optional.empty());
    PasswordImportRequestResource passwordImportRequestResource =
        UserTestUtility.getPasswordImportRequestResourceForTest();
    PasswordImportResponseResource response =
        skrillTellerUserService.validateUserCredentialsOkta(passwordImportRequestResource);

    assertEquals("UNVERIFIED", response.getCommands().get(0).getValue().getCredential());
  }

  @Test
  public void validateUserCredentialsOkta_HashedPasswordNotPresent() {
    User user = new User();
    when(usersRepository.findById(any())).thenReturn(Optional.ofNullable(user));
    PasswordImportRequestResource passwordImportRequestResource =
        UserTestUtility.getPasswordImportRequestResourceForTest();
    PasswordImportResponseResource response =
        skrillTellerUserService.validateUserCredentialsOkta(passwordImportRequestResource);

    assertEquals("UNVERIFIED", response.getCommands().get(0).getValue().getCredential());
  }

  @Test
  public void validateUserCredentialsOkta_CredentialsNotMatch() {
    User user = new User();
    user.setHashedPassword(new HashedPasswordEntity());
    when(usersRepository.findById(any())).thenReturn(Optional.ofNullable(user));
    when(passwordEncryptionUtil.compareHashedPasswords(any(), any())).thenReturn(false);
    PasswordImportRequestResource passwordImportRequestResource =
        UserTestUtility.getPasswordImportRequestResourceForTest();
    PasswordImportResponseResource response =
        skrillTellerUserService.validateUserCredentialsOkta(passwordImportRequestResource);

    assertEquals("UNVERIFIED", response.getCommands().get(0).getValue().getCredential());
  }

  @Test
  public void migrateSkrillTellerUser_withoutCredentials_shouldSucceed() throws JsonProcessingException {
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    when(userService.createUser(any())).thenReturn(userProvisioningUserResource);
    when(skrillTellerMigrationRepository.findByUserNameAndApplication(any(), any()))
        .thenReturn(skrillTellerMigrationUser);
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    Map<String, String> permissionMapping = new HashMap<>();
    permissionMapping.put(SkrillPermissions.SEND_MONEY_CUSTOMER.toString(), "Transfers - Customer");
    when(skrillUserSummaryPermissionsConfig.getPermissionMapping()).thenReturn(permissionMapping);
    skrillTellerDto.setCredentials(null);
    skrillTellerUserService.migrateSkrillTellerUser(skrillTellerDto);
    verify(userService, times(1)).createUser(any());
    verify(skrillTellerMigrationRepository, times(1)).findByUserNameAndApplication(any(), any());
    verify(skrillTellerMigrationRepository, times(1)).save(any());
  }

  @Test
  public void netbanxPasswordMigration_withValidInput_shouldSuccess()
      throws JsonMappingException, JsonProcessingException {
    when(userProvisioningConfig.getUaaClientId()).thenReturn("1234");
    when(userProvisioningConfig.getUaaClientSecret()).thenReturn("uaa122");
    ObjectMapper mapper = new ObjectMapper();
    UserMigrationDto migrationDto = new UserMigrationDto();
    migrationDto.setUserName("testUsername");
    when(uaaFeignClient.authenticateUser_JsonNodeResponse(any(), any(), any(), any(), any(), any()))
        .thenReturn(new ResponseEntity<>(mapper.readTree(new UserMigrationResponse("body").toString()), HttpStatus.OK));
    skrillTellerUserService.netbanxPasswordMigration(UserTestUtility.getPasswordImportRequestResourceForTest());
    verify(uaaFeignClient, times(1)).authenticateUser_JsonNodeResponse(any(), any(), any(), any(), any(), any());
  }

  @Test
  public void netbanxPasswordMigration_withValidInputUsernameNull_shouldSuccess()
      throws JsonMappingException, JsonProcessingException {
    when(userProvisioningConfig.getUaaClientId()).thenReturn("1234");
    when(userProvisioningConfig.getUaaClientSecret()).thenReturn("uaa122");
    ObjectMapper mapper = new ObjectMapper();
    UserMigrationDto migrationDto = new UserMigrationDto();
    migrationDto.setUserName("testUsername");
    when(uaaFeignClient.authenticateUser_JsonNodeResponse(any(), any(), any(), any(), any(), any()))
        .thenReturn(new ResponseEntity<>(mapper.readTree(new UserMigrationResponse("body").toString()), HttpStatus.OK));
    PasswordImportRequestResource passwordReqResource = UserTestUtility.getPasswordImportRequestResourceForTest();
    passwordReqResource.getData().getContext().getCredential().setUsername(null);
    skrillTellerUserService.netbanxPasswordMigration(passwordReqResource);
    verify(uaaFeignClient, times(1)).authenticateUser_JsonNodeResponse(any(), any(), any(), any(), any(), any());
  }

  @Test(expected = UnauthorizedException.class)
  public void netbanxPasswordMigration_unAuthorized_throwsException()
      throws JsonMappingException, JsonProcessingException {
    when(userProvisioningConfig.getUaaClientId()).thenReturn("1234");
    when(userProvisioningConfig.getUaaClientSecret()).thenReturn("uaa122");
    UserMigrationDto migrationDto = new UserMigrationDto();
    migrationDto.setUserName("testUsername");
    when(uaaFeignClient.authenticateUser_JsonNodeResponse(any(), any(), any(), any(), any(), any()))
        .thenThrow(UnauthorizedException.builder().details("Invalid credentials").build());
    skrillTellerUserService.netbanxPasswordMigration(UserTestUtility.getPasswordImportRequestResourceForTest());
  }

  @Test(expected = InternalErrorException.class)
  public void netbanxPasswordMigration_whenClientIdAndSecretMissing_throwsException()
      throws JsonMappingException, JsonProcessingException {
    when(userProvisioningConfig.getUaaClientId()).thenReturn(null);
    when(userProvisioningConfig.getUaaClientSecret()).thenReturn(null);
    UserMigrationDto migrationDto = new UserMigrationDto();
    migrationDto.setUserName("testUsername");
    ObjectMapper mapper = new ObjectMapper();
    when(uaaFeignClient.authenticateUser_JsonNodeResponse(any(), any(), any(), any(), any(), any()))
        .thenReturn(new ResponseEntity<>(mapper.readTree(new UserMigrationResponse("body").toString()), HttpStatus.OK));
    skrillTellerUserService.netbanxPasswordMigration(UserTestUtility.getPasswordImportRequestResourceForTest());
  }
}
