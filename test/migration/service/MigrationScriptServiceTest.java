// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.migration.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.upf.user.provisioning.config.UserMigrationConfig;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.feignclients.MasterMerchantFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.PegasusFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.PermissionServiceClient;
import com.paysafe.upf.user.provisioning.migration.repository.MigrationRepository;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.MasterMerchantService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.util.MerchantTestUtility;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegausUserRoleResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MigrationScriptServiceTest {

  @Mock
  private MigrationRepository migrationRepository;

  @Mock
  private PegasusFeignClient pegasusFeignClient;

  @Mock
  private UserService userService;

  @Mock
  private UserMigrationConfig userMigrationConfig;

  @Mock
  private MasterMerchantFeignClient masterMerchantFeignClient;

  @Mock
  private MasterMerchantService masterMerchantService;

  @Mock
  private PermissionServiceClient permissionServiceClient;

  @InjectMocks
  private MigrationScriptService migrationService;

  private MockMultipartFile file;

  private PegasusUserListResponseResource pegasusUserListResponseResource;

  private PegasusUserListResponseResource pegasusCustomUserListResponseResource;

  private Map<String, String> usIgamingRoleMap;

  private ResponseEntity<Map<String, Object>> permissionReponse;

  /**
   * Setup test configuration.
   *
   * @throws Exception exception
   */
  @Before
  public void setUp() throws Exception {
    pegasusUserListResponseResource = new PegasusUserListResponseResource();
    pegasusUserListResponseResource.setCount(Long.valueOf(1));
    pegasusUserListResponseResource.setUsers(Arrays.asList(getPegasusUser()));
    constructPermissionResponse();
    pegasusCustomUserListResponseResource = new PegasusUserListResponseResource();
    pegasusCustomUserListResponseResource.setCount(Long.valueOf(1));
    pegasusCustomUserListResponseResource.setUsers(Arrays.asList(getCustomRolePegasusUser()));
    Map<String, List<String>> roleMap = new HashMap<>();
    roleMap.put("FCNUSER", Arrays.asList("FULL_ACCESS"));
    when(userMigrationConfig.getRoleMap()).thenReturn(roleMap);
    ReflectionTestUtils.setField(migrationService, "defaultPartnerPortalRole",
        Arrays.asList("group-business-portal:dashboard-mle:get"));
    usIgamingRoleMap = new HashMap<>();
    usIgamingRoleMap.put("BP_ADMIN", "BP_US_ADMIN");
  }

  private void constructPermissionResponse() {
    List<String> permissionList = new ArrayList<String>(Arrays.asList("permission1", "permission2"));
    LinkedHashMap<String, Object> permissionSet = new LinkedHashMap<>();
    permissionSet.put("permissionList", permissionList);
    ArrayList<LinkedHashMap<String, Object>> permissionSetArray = new ArrayList<>();
    permissionSetArray.add(permissionSet);
    LinkedHashMap<String, Object> permissionGroup = new LinkedHashMap<>();
    permissionGroup.put("permissionsSet", permissionSetArray);
    Map<String, Object> permissionResponseBody = new LinkedHashMap<>();
    ArrayList<Map<String, Object>> permissionGroupArray = new ArrayList<>();
    permissionGroupArray.add(permissionGroup);
    permissionResponseBody.put("permissionGroup", permissionGroupArray);
    permissionReponse = new ResponseEntity<>(permissionResponseBody, HttpStatus.OK);
  }

  @Test
  public void processFile_Success() throws JsonProcessingException {
    file = new MockMultipartFile("file", "Header\nMigrateduser1\nMigrateduser2".getBytes());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.processFile(file, false, false);
    verify(pegasusFeignClient, atLeastOnce()).getUsers(any(), any(), any(), any());
  }

  @Test
  public void processFile_withPartnerUserType_shouldSuccess() throws JsonProcessingException {
    file = new MockMultipartFile("file", "Header\nMigrateduser1,PARTNER,123,TRUE".getBytes());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.processFile(file, false, false);
    verify(pegasusFeignClient, atLeastOnce()).getUsers(any(), any(), any(), any());
    verify(userService, atLeastOnce()).createUser(any());
  }

  @Test
  public void processFile_withisUSigaming_shouldSuccess() throws JsonProcessingException {
    file = new MockMultipartFile("file", "Header\nMigrateduser1,PARTNER,123,TRUE,TRUE".getBytes());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    when(userMigrationConfig.getUsIgamingRoleMap()).thenReturn(usIgamingRoleMap);
    migrationService.processFile(file, false, false);
    verify(pegasusFeignClient, atLeastOnce()).getUsers(any(), any(), any(), any());
    verify(userService, atLeastOnce()).createUser(any());
  }

  @Test
  public void processFile_withAccountGroupUserType_shouldSuccess() throws JsonProcessingException {
    file = new MockMultipartFile("file", "Header\nMigrateduser1,ACCOUNT_GROUP,123,TRUE".getBytes());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.processFile(file, false, false);
    verify(pegasusFeignClient, atLeastOnce()).getUsers(any(), any(), any(), any());
    verify(userService, atLeastOnce()).createUser(any());
  }

  @Test
  public void processFile_withMerchantUserType_shouldSuccess() throws JsonProcessingException {
    file = new MockMultipartFile("file", "Header\nMigrateduser1,MLE,123,TRUE".getBytes());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.processFile(file, false, false);
    verify(pegasusFeignClient, atLeastOnce()).getUsers(any(), any(), any(), any());
    verify(userService, atLeastOnce()).createUser(any());
  }

  @Test
  public void processFile_Exception() throws JsonProcessingException {
    file = new MockMultipartFile("file", "Header\nMigrateduser1\nMigrateduser2".getBytes());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenThrow(BadRequestException.builder().accountNotFound().build());
    migrationService.processFile(file, false, false);
    verify(pegasusFeignClient, atLeastOnce()).getUsers(any(), any(), any(), any());
    verify(userService, atLeastOnce()).createUser(any());
  }

  @Test
  public void processFile_withInvalidInputAndWriteLogToFileTrue() throws JsonProcessingException {
    file = new MockMultipartFile("file", "Header\nMigrateduser1\nMigrateduser2".getBytes());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenThrow(BadRequestException.builder().accountNotFound().build());
    migrationService.processFile(file, true, false);
    verify(pegasusFeignClient, atLeastOnce()).getUsers(any(), any(), any(), any());
  }

  @Test
  public void processFile_withMerchantUserTypeAndDivision_shouldSuccess() throws JsonProcessingException {
    file = new MockMultipartFile("file",
        "Header\nMigrateduser1,MLE,123,FALSE,FALSE,EU_ACQUIRING_EEA,NBX_PORTAL".getBytes());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.processFile(file, false, false);
    verify(pegasusFeignClient, atLeastOnce()).getUsers(any(), any(), any(), any());
    verify(userService, atLeastOnce()).createUser(any());
  }

  @Test
  public void processFile_withV2Migration_shouldSuccess() throws JsonProcessingException {
    file = new MockMultipartFile("file",
        "Header\nMigrateduser1,MLE,123,FALSE,FALSE,EU_ACQUIRING_EEA,NBX_PORTAL".getBytes());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    UsersListResponseResource usersListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put("division", "NBX_PORTAL");
    usersListResponseResource.getUsers().get(0).setCustomProperties(customProperties);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(usersListResponseResource);
    when(userMigrationConfig.getUsIgamingRoleMap()).thenReturn(usIgamingRoleMap);
    migrationService.processFile(file, false, true);
    verify(pegasusFeignClient, atLeastOnce()).getUsers(any(), any(), any(), any());
  }

  @Test
  public void processFile_withV2MigrationUpdateOnlySsoGroup_shouldSuccess() throws JsonProcessingException {
    file = new MockMultipartFile("file",
        "Header\nMigrateduser1,MLE,123,FALSE,FALSE,EU_ACQUIRING_EEA,,NBX_PORTAL,TRUE".getBytes());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    UsersListResponseResource usersListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put("division", "NBX_PORTAL");
    usersListResponseResource.getUsers().get(0).setCustomProperties(customProperties);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(usersListResponseResource);
    when(userMigrationConfig.getUsIgamingRoleMap()).thenReturn(usIgamingRoleMap);
    migrationService.processFile(file, false, true);
    verify(pegasusFeignClient, atLeastOnce()).getUsers(any(), any(), any(), any());
  }

  private PegasusUserResponseResource getPegasusUser() {
    PegasusUserResponseResource pegasusUser = new PegasusUserResponseResource();
    pegasusUser.setFullName("fullname");
    pegasusUser.setEmail("email");
    pegasusUser.setPmleId(Long.valueOf(1));
    pegasusUser.setLoginName("loginname");
    pegasusUser.setStatus("ACTIVE");
    pegasusUser.setUuid("uuid");
    pegasusUser.setAccessLevelTypeCode("PMLE");
    pegasusUser.setAccessValue("pmlevalue");
    List<PegausUserRoleResource> pegausUserRoleResourceList = new ArrayList<>();
    PegausUserRoleResource pegausUserRoleResource = new PegausUserRoleResource();
    pegausUserRoleResource.setRoleCode("BP_ADMIN");
    pegausUserRoleResource.setProjectType("BP");
    pegausUserRoleResourceList.add(pegausUserRoleResource);
    pegasusUser.setRoles(pegausUserRoleResourceList);
    return pegasusUser;
  }

  private PegasusUserResponseResource getCustomRolePegasusUser() {
    PegasusUserResponseResource pegasusUser = new PegasusUserResponseResource();
    pegasusUser.setFullName("fullname");
    pegasusUser.setEmail("email");
    pegasusUser.setPmleId(Long.valueOf(1));
    pegasusUser.setLoginName("loginname");
    pegasusUser.setStatus("ACTIVE");
    pegasusUser.setUuid("uuid");
    pegasusUser.setAccessLevelTypeCode("PMLE");
    pegasusUser.setAccessValue("pmlevalue");
    List<PegausUserRoleResource> pegausUserRoleResourceList = new ArrayList<>();
    PegausUserRoleResource pegausUserRoleResource = new PegausUserRoleResource();
    pegausUserRoleResource.setRoleCode("BP_CUSTOM");
    pegausUserRoleResource.setProjectType("BP");
    pegausUserRoleResourceList.add(pegausUserRoleResource);
    pegasusUser.setRoles(pegausUserRoleResourceList);
    return pegasusUser;
  }

  @Test
  public void migrateSingleUser_Success() throws JsonProcessingException {
    AuthorizationInfo auth = new AuthorizationInfo();
    auth.setApplication("PARTNER_PORTAL");
    CommonThreadLocal.setAuthLocal(auth);
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.migrateSingleUser(userMigrationDto, false);
    verify(pegasusFeignClient, times(1)).getUsers(any(), any(), any(), any());
    verify(userService, times(1)).createUser(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void migrateSingleUser_forV2Migrate_Success() throws JsonProcessingException {
    AuthorizationInfo auth = new AuthorizationInfo();
    auth.setApplication("PARTNER_PORTAL");
    CommonThreadLocal.setAuthLocal(auth);
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    UsersListResponseResource userListResponseResource = new UsersListResponseResource();
    userListResponseResource.setCount(3L);
    userListResponseResource.setUsers(UserTestUtility.getUserResponseList());
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    migrationService.migrateSingleUser(userMigrationDto, true);
    verify(pegasusFeignClient, times(1)).getUsers(any(), any(), any(), any());
    verify(userService, times(1)).updateUser(any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void migrateSingleUser_forV2MigrateWhenUserNotFound_Success() throws JsonProcessingException {
    AuthorizationInfo auth = new AuthorizationInfo();
    auth.setApplication("PARTNER_PORTAL");
    CommonThreadLocal.setAuthLocal(auth);
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    Mockito.doThrow(NotFoundException.builder().build()).when(userService).getUsers(any(), any(), any(), any(), any(),
        any(), any(), any(), any(), any());
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    migrationService.migrateSingleUser(userMigrationDto, true);
    verify(pegasusFeignClient, times(1)).getUsers(any(), any(), any(), any());
    verify(userService, times(1)).createUser(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void migrateUSiUser_Success() throws JsonProcessingException {
    AuthorizationInfo auth = new AuthorizationInfo();
    auth.setApplication("PARTNER_PORTAL");
    CommonThreadLocal.setAuthLocal(auth);
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setUSigaming(true);
    userMigrationDto.setPassword("password");
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    when(userMigrationConfig.getUsIgamingRoleMap()).thenReturn(usIgamingRoleMap);
    migrationService.migrateUSiUser(userMigrationDto, false);
    verify(pegasusFeignClient, times(1)).getUsers(any(), any(), any(), any());
    verify(userService, times(1)).createUser(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void migrateUSiUser_withInvalidUserName_throwsException() throws JsonProcessingException {
    AuthorizationInfo auth = new AuthorizationInfo();
    auth.setApplication("PARTNER_PORTAL");
    CommonThreadLocal.setAuthLocal(auth);
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setUSigaming(true);
    userMigrationDto.setPassword("password");
    PegasusUserListResponseResource pegasusUsersResource = new PegasusUserListResponseResource();
    pegasusUsersResource.setCount(0L);
    pegasusUsersResource.setUsers(new ArrayList<>());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUsersResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    when(userMigrationConfig.getUsIgamingRoleMap()).thenReturn(usIgamingRoleMap);
    migrationService.migrateUSiUser(userMigrationDto, false);
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void migrateUSiCustomUser_withInvalidRole_throwsException() throws JsonProcessingException {
    AuthorizationInfo auth = new AuthorizationInfo();
    auth.setApplication("PARTNER_PORTAL");
    CommonThreadLocal.setAuthLocal(auth);
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setUSigaming(true);
    userMigrationDto.setPassword("password");
    List<PegausUserRoleResource> pegausUserRoleResourceList = new ArrayList<>();
    PegausUserRoleResource pegausUserRoleResource = new PegausUserRoleResource();
    pegausUserRoleResource.setRoleCode("BP_INVALID_ROLE");
    pegausUserRoleResource.setProjectType("BP");
    pegausUserRoleResourceList.add(pegausUserRoleResource);
    pegasusCustomUserListResponseResource.getUsers().get(0).setRoles(pegausUserRoleResourceList);
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusCustomUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    when(userMigrationConfig.getUsIgamingRoleMap()).thenReturn(usIgamingRoleMap);
    when(permissionServiceClient.getPermissionsForRolesAndCategories(any(), anyBoolean()))
        .thenReturn(permissionReponse);
    migrationService.migrateUSiUser(userMigrationDto, false);
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void migrateUSiCustomUser_Success() throws JsonProcessingException {
    AuthorizationInfo auth = new AuthorizationInfo();
    auth.setApplication("PARTNER_PORTAL");
    CommonThreadLocal.setAuthLocal(auth);
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setUSigaming(true);
    userMigrationDto.setPassword("password");
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusCustomUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    when(userMigrationConfig.getUsIgamingRoleMap()).thenReturn(usIgamingRoleMap);
    when(permissionServiceClient.getPermissionsForRolesAndCategories(any(), anyBoolean()))
        .thenReturn(permissionReponse);
    migrationService.migrateUSiUser(userMigrationDto, false);
    verify(pegasusFeignClient, times(1)).getUsers(any(), any(), any(), any());
    verify(userService, times(1)).createUser(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void migrateSingleUser_Success_WithAdminRole() throws JsonProcessingException {
    AuthorizationInfo auth = new AuthorizationInfo();
    auth.setApplication("PORTAL");
    CommonThreadLocal.setAuthLocal(auth);
    Map<String, List<String>> roleMap = new HashMap<>();
    roleMap.put("FCNUSER", Arrays.asList("BP_EU_ADMIN"));
    when(userMigrationConfig.getRoleMap()).thenReturn(roleMap);
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.migrateSingleUser(userMigrationDto, false);
    verify(pegasusFeignClient, times(1)).getUsers(any(), any(), any(), any());
    verify(userService, times(1)).createUser(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void migrateSingleUser_Success_WithInvalidRoleKey() throws JsonProcessingException {
    AuthorizationInfo auth = new AuthorizationInfo();
    auth.setApplication("PORTAL");
    CommonThreadLocal.setAuthLocal(auth);
    Map<String, List<String>> roleMap = new HashMap<>();
    roleMap.put("TEST", Arrays.asList("BP_EU_ADMIN"));
    when(userMigrationConfig.getRoleMap()).thenReturn(roleMap);
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.migrateSingleUser(userMigrationDto, false);
    verify(pegasusFeignClient, times(1)).getUsers(any(), any(), any(), any());
    verify(userService, times(1)).createUser(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void migrateSingleUser_Success_WithAdminIsvRole() throws JsonProcessingException {
    AuthorizationInfo auth = new AuthorizationInfo();
    auth.setApplication("PORTAL");
    CommonThreadLocal.setAuthLocal(auth);
    Map<String, List<String>> roleMap = new HashMap<>();
    roleMap.put("FCNUSER", Arrays.asList("BP_ISV_ADMIN"));
    when(userMigrationConfig.getRoleMap()).thenReturn(roleMap);
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.migrateSingleUser(userMigrationDto, false);
    verify(pegasusFeignClient, times(1)).getUsers(any(), any(), any(), any());
    verify(userService, times(1)).createUser(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void migrateSingleUser_ShouldReturn_OnEmptyPegasusResponse() throws JsonProcessingException {
    AuthorizationInfo auth = new AuthorizationInfo();
    auth.setApplication("PORTAL");
    CommonThreadLocal.setAuthLocal(auth);
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    pegasusUserListResponseResource.setCount(Long.valueOf(0));
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.migrateSingleUser(userMigrationDto, false);
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void migrateSingleUserOwnerTypeUser_Success() throws IOException {
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    PegasusUserResponseResource pegasusUser = getPegasusUser();
    pegasusUser.setAccessLevelTypeCode("USER");
    pegasusUser.setFmaIds(Arrays.asList("1234", "1235"));
    PegasusUserListResponseResource pegasusUserListResponse = new PegasusUserListResponseResource();
    pegasusUserListResponse.setCount(Long.valueOf(1));
    pegasusUserListResponse.setUsers(Arrays.asList(pegasusUser));
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(MerchantTestUtility.getMerchantsForPmle());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponse);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.migrateSingleUser(userMigrationDto, false);
    verify(masterMerchantService, times(2)).getMerchantsUsingSearchAfter(any());
    verify(pegasusFeignClient, times(1)).getUsers(any(), any(), any(), any());
    verify(userService, times(1)).createUser(any());
  }

  @Test
  public void migrateSingleUserOwnerTypeUser_WithMultiplePmle_Success() throws IOException {
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    PegasusUserResponseResource pegasusUser = getPegasusUser();
    pegasusUser.setAccessLevelTypeCode("USER");
    pegasusUser.setFmaIds(Arrays.asList("1234", "1235"));
    PegasusUserListResponseResource pegasusUserListResponse = new PegasusUserListResponseResource();
    pegasusUserListResponse.setCount(Long.valueOf(1));
    pegasusUserListResponse.setUsers(Arrays.asList(pegasusUser));
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(MerchantTestUtility.getMerchantsForMultiplePmle(false));
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponse);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.migrateSingleUser(userMigrationDto, false);
    verify(masterMerchantService, times(2)).getMerchantsUsingSearchAfter(any());
    verify(pegasusFeignClient, times(1)).getUsers(any(), any(), any(), any());
    verify(userService, times(1)).createUser(any());
  }

  @Test
  public void migrateSingleUserOwnerTypeUser_WithMultipleMle_Success() throws IOException {
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    PegasusUserResponseResource pegasusUser = getPegasusUser();
    pegasusUser.setAccessLevelTypeCode("USER");
    pegasusUser.setFmaIds(Arrays.asList("1234", "1235"));
    PegasusUserListResponseResource pegasusUserListResponse = new PegasusUserListResponseResource();
    pegasusUserListResponse.setCount(Long.valueOf(1));
    pegasusUserListResponse.setUsers(Arrays.asList(pegasusUser));
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(MerchantTestUtility.getMerchantsForMultiplePmle(true));
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponse);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.migrateSingleUser(userMigrationDto, false);
    verify(masterMerchantService, times(2)).getMerchantsUsingSearchAfter(any());
    verify(pegasusFeignClient, times(1)).getUsers(any(), any(), any(), any());
    verify(userService, times(1)).createUser(any());
  }

  @Test(expected = BadRequestException.class)
  public void migrateSingleUserOwnerTypeUser_whenFmasEmpty_throwsException() throws IOException {
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    PegasusUserResponseResource pegasusUser = getPegasusUser();
    pegasusUser.setAccessLevelTypeCode("USER");
    pegasusUser.setFmaIds(new ArrayList<>());
    PegasusUserListResponseResource pegasusUserListResponse = new PegasusUserListResponseResource();
    pegasusUserListResponse.setCount(Long.valueOf(1));
    pegasusUserListResponse.setUsers(Arrays.asList(pegasusUser));
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(MerchantTestUtility.getMerchantsForMultiplePmle(true));
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponse);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.migrateSingleUser(userMigrationDto, false);
  }

  @Test(expected = BadRequestException.class)
  public void migrateSingleUser_withUserAccessTypeAndEmptyFmaIds_throwsException() throws IOException {
    UserMigrationDto userMigrationDto = new UserMigrationDto();
    userMigrationDto.setUserName("userName");
    userMigrationDto.setIsAdmin("FALSE");
    userMigrationDto.setOwnerId("ownerId");
    userMigrationDto.setOwnerType("ownerType");
    PegasusUserResponseResource pegasusUser = getPegasusUser();
    pegasusUser.setAccessLevelTypeCode("USER");
    pegasusUser.setFmaIds(Arrays.asList());
    PegasusUserListResponseResource pegasusUserListResponse = new PegasusUserListResponseResource();
    pegasusUserListResponse.setCount(Long.valueOf(1));
    pegasusUserListResponse.setUsers(Arrays.asList(pegasusUser));
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(MerchantTestUtility.getMerchantsForPmle());
    when(pegasusFeignClient.getUsers(any(), any(), any(), any())).thenReturn(pegasusUserListResponse);
    when(userService.createUser(any())).thenReturn(new UserProvisioningUserResource());
    migrationService.migrateSingleUser(userMigrationDto, false);
  }
}
