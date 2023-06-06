// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.upf.user.provisioning.domain.AuditUserEvent;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.BusinessUnit;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.enums.UserAction;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.migration.repository.SkrillTellerMigrationRepository;
import com.paysafe.upf.user.provisioning.repository.AuditUserEventRepository;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UserAssignedApplicationsRepository;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.ModuleService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserDataSyncResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersBusinessUnitUpdateResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UserHandlerServiceImplTest {

  @Mock
  private AuditUserEventRepository auditUserEventRepository;

  @Mock
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private UserAssembler userAssembler;

  @Mock
  private SkrillTellerMigrationRepository skrillTellerMigrationRepository;

  @Mock
  private UserAssignedApplicationsRepository userAssignedApplicationsRepository;

  @Mock
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Mock
  private UserService userService;

  @Mock
  private ModuleService moduleService;

  @Mock
  private UserProvisioningUtils userProvisioningUtils;

  @InjectMocks
  private UserHandlerServiceImpl userHandlerServiceImplMock;

  @Test
  public void testUpdateUserStatusAndAuditTableFromOkta() throws JsonProcessingException, IOException {
    doNothing().when(usersRepository).updateUserStatus(any(), any());
    when(auditUserEventRepository.save(any())).thenReturn(new AuditUserEvent());
    when(usersRepository.findByUserExternalId(any())).thenReturn(UserTestUtility.getUser());
    userHandlerServiceImplMock.updateUserStatusAndAuditTableFromOkta(UserTestUtility.getOktaEventHookResponse(false));
    verify(auditUserEventRepository, Mockito.times(0)).save(any());
  }

  @Test
  public void testUpdateUserStatusAndAuditTableFromOktaForSuspendedEvent() throws JsonProcessingException, IOException {
    doNothing().when(usersRepository).updateUserStatus(any(), any());
    when(auditUserEventRepository.save(any())).thenReturn(new AuditUserEvent());
    when(usersRepository.findByUserExternalId(any())).thenReturn(UserTestUtility.getUser());
    userHandlerServiceImplMock.updateUserStatusAndAuditTableFromOkta(UserTestUtility.getOktaEventHookResponse(true));
    verify(usersRepository, Mockito.times(2)).updateUserStatus(any(), any());
  }

  @Test
  public void testGetUserCount() {
    when(userAccessGroupMapppingRepository
        .countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(any(), any(), any(), any()))
            .thenReturn(Long.valueOf(10));

    userHandlerServiceImplMock.getUserCount("resourceType", "resourceId");
    verify(userAccessGroupMapppingRepository, Mockito.times(2))
        .countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(any(), any(), any(), any());
  }

  @Test
  public void getBulkUserCount_withValidInput_shouldSucceed() {
    AuthorizationInfo authInfo = new AuthorizationInfo();
    authInfo.setApplication("SKRILL");
    CommonThreadLocal.setAuthLocal(authInfo);

    when(userAccessGroupMapppingRepository.getBulkUsersTotal(any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getBulkUsersList());
    when(userAccessGroupMapppingRepository.getBulkUsersAdmin(any(), any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getBulkUsersList());
    userHandlerServiceImplMock.getBulkUserCount("testuser@paysafe.com");
    verify(userAccessGroupMapppingRepository, Mockito.times(1)).getBulkUsersTotal(any(), any(), any(), any());
    verify(userAccessGroupMapppingRepository, Mockito.times(1)).getBulkUsersAdmin(any(), any(), any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void deleteUser() {
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());
    doNothing().when(usersRepository).delete(any());
    doNothing().when(userAccessGroupMapppingRepository).deleteByUserId(any());
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(null);
    userHandlerServiceImplMock.deleteUser("testuser@paysafe.com", null, false);
    verify(usersRepository, Mockito.times(1)).findByUserId(any());
  }

  @Test
  public void deleteUser_withApplication() {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    doNothing().when(usersRepository).delete(any());
    doNothing().when(userAccessGroupMapppingRepository).deleteByUserId(any());
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(null);
    userHandlerServiceImplMock.deleteUser("testuser@paysafe.com", "skrill", false);
    verify(usersRepository, Mockito.times(1)).findByLoginNameAndApplication(any(), any());
  }

  @Test(expected = NotFoundException.class)
  public void deleteUser_Error() {
    when(usersRepository.findByUserId(any())).thenReturn(null);
    doNothing().when(usersRepository).delete(any());
    doNothing().when(userAccessGroupMapppingRepository).deleteByUserId(any());
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(null);
    userHandlerServiceImplMock.deleteUser("testuser@paysafe.com", null, false);
    verify(usersRepository, Mockito.times(1)).findByUserId(any());
  }

  @Test
  public void deleteUserFromBo() {
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getProvisionedUser());
    doNothing().when(usersRepository).delete(any());
    doNothing().when(userAccessGroupMapppingRepository).deleteByUserId(any());
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(null);
    userHandlerServiceImplMock.deleteUser("testuser@paysafe.com", null, true);
    verify(usersRepository, Mockito.times(1)).findByUserId(any());
  }

  @Test(expected = BadRequestException.class)
  public void deleteUser_BadRequestError() {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    doNothing().when(usersRepository).delete(any());
    doNothing().when(userAccessGroupMapppingRepository).deleteByUserId(any());
    when(skrillTellerMigrationRepository.findByUserId(any())).thenReturn(null);
    userHandlerServiceImplMock.deleteUser("testuser@paysafe.com", "skrill", true);
    verify(usersRepository, Mockito.times(1)).findByLoginNameAndApplication(any(), any());
  }

  @Test
  public void activateUserWithOkta_withValidInput_shouldSucceed() throws JsonProcessingException, IOException {
    when(identityManagementFeignClient.activateUser(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    userHandlerServiceImplMock.activateUserWithOkta(UserTestUtility.getOktaCreateUserEventHookResponse());
    verify(identityManagementFeignClient, Mockito.times(1)).activateUser(any());
  }

  @Test
  public void updateUsersBusinessUnit_withValidSingleUserAsInput_shouldSucceed() throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    UsersBusinessUnitUpdateResponse response = userHandlerServiceImplMock.updateUsersBusinessUnit(DataConstants.PORTAL,
        "testuser1234", BusinessUnit.EU_ACQUIRING_EEA, 0, false, null);
    assertEquals(1L, response.getSucceedUsersCount());
  }

  @Test
  public void updateUsersBusinessUnit_withValidListUsersAsInput_shouldSucceed() throws JsonProcessingException {
    List<User> users = new ArrayList<>(Arrays.asList(UserTestUtility.getUser()));
    Page<User> userPage = new PageImpl<>(users);
    when(usersRepository.findByApplicationAndBusinessUnitOrderByLastModifiedDateDesc(any(), any(), any()))
        .thenReturn(userPage);
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    UsersBusinessUnitUpdateResponse response = userHandlerServiceImplMock.updateUsersBusinessUnit(DataConstants.PORTAL,
        null, BusinessUnit.EU_ACQUIRING_EEA, 1, false, "portal");
    assertEquals(1L, response.getSucceedUsersCount());
  }

  @Test(expected = BadRequestException.class)
  public void updateUsersBusinessUnit_whenUserNotFound_throwsException() throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any())).thenReturn(Optional.empty());
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    userHandlerServiceImplMock.updateUsersBusinessUnit(DataConstants.PORTAL, "testuser1234",
        BusinessUnit.EU_ACQUIRING_EEA, 0, false, null);
  }

  @Test(expected = BadRequestException.class)
  public void updateUsersBusinessUnit_whenApplicationIsNull_throwsException() throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    userHandlerServiceImplMock.updateUsersBusinessUnit(null, "testuser1234", BusinessUnit.EU_ACQUIRING_EEA, 0, false,
        null);
  }

  @Test(expected = BadRequestException.class)
  public void updateUsersBusinessUnit_whenNoUsersFound_throwsException() throws JsonProcessingException {
    List<User> users = new ArrayList<>();
    Page<User> userPage = new PageImpl<>(users);
    when(usersRepository.findByApplicationAndBusinessUnitOrderByLastModifiedDateDesc(any(), any(), any()))
        .thenReturn(userPage);
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    userHandlerServiceImplMock.updateUsersBusinessUnit(DataConstants.PORTAL, null, BusinessUnit.EU_ACQUIRING_EEA, 1,
        false, null);
  }

  @Test
  public void updateUsersBusinessUnit_whenUserUpdateThrowsBadRequestException_userCountAsFailed()
      throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenThrow(BadRequestException.builder().build());
    UsersBusinessUnitUpdateResponse response = userHandlerServiceImplMock.updateUsersBusinessUnit(DataConstants.PORTAL,
        "abcd@gmail.com", BusinessUnit.EU_ACQUIRING_EEA, 0, false, null);
    assertEquals(1L, response.getFailedUsersCount());
  }

  @Test
  public void updateUsersBusinessUnit_whenUserUpdateThrowsNullPointerException_userCountAsFailed()
      throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenThrow(NullPointerException.class);
    UsersBusinessUnitUpdateResponse response = userHandlerServiceImplMock.updateUsersBusinessUnit(DataConstants.PORTAL,
        "abcd@gmail.com", BusinessUnit.EU_ACQUIRING_EEA, 0, false, null);
    assertEquals(1L, response.getFailedUsersCount());
  }

  @Test
  public void updateUsersBusinessUnit_updatingBasedOnMmTag_shouldSucceed() throws JsonProcessingException {
    User user = UserTestUtility.getUser();
    user.setOwnerId("1234");
    user.setOwnerType(DataConstants.PMLE);
    when(usersRepository.findByLoginNameAndApplication(any(), any())).thenReturn(Optional.of(user));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    userListResponseResource.getUsers().get(0).getAccessResources().get(0).setRole(DataConstants.BP_EU_ADMIN);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    when(moduleService.getBusinessInitiativesForIds(any(), any()))
        .thenReturn(new HashSet<>(Arrays.asList(BusinessUnit.EU_ACQUIRING_EEA.toString())));
    UsersBusinessUnitUpdateResponse response = userHandlerServiceImplMock.updateUsersBusinessUnit(DataConstants.PORTAL,
        "testuser1234", BusinessUnit.EU_ACQUIRING_EEA, 0, true, BusinessUnit.SKILL_GAMING.toString());
    assertEquals(1L, response.getSucceedUsersCount());
    verify(moduleService, Mockito.times(1)).getBusinessInitiativesForIds(any(), any());
  }

  @Test
  public void updateUsersBusinessUnit_updatingBasedOnEuUserRole_shouldSucceed() throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    userListResponseResource.getUsers().get(0).getAccessResources().get(0).setRole(DataConstants.BP_EU_ADMIN);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    when(moduleService.getBusinessInitiativesForIds(any(), any()))
        .thenReturn(new HashSet<>(Arrays.asList(BusinessUnit.EU_ACQUIRING_EEA.toString())));
    UsersBusinessUnitUpdateResponse response = userHandlerServiceImplMock.updateUsersBusinessUnit(DataConstants.PORTAL,
        "testuser1234", BusinessUnit.EU_ACQUIRING_EEA, 0, true, null);
    assertEquals(1L, response.getSucceedUsersCount());
    verify(userService, Mockito.times(1)).getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any());
  }

  @Test
  public void updateUsersBusinessUnit_updatingBasedOnEcommUserRole_shouldSucceed() throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    userListResponseResource.getUsers().get(0).getAccessResources().get(0).setRole("BP_ECOMM_ADMIN");
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    when(moduleService.getBusinessInitiativesForIds(any(), any()))
        .thenReturn(new HashSet<>(Arrays.asList(BusinessUnit.EU_ACQUIRING_EEA.toString())));
    UsersBusinessUnitUpdateResponse response = userHandlerServiceImplMock.updateUsersBusinessUnit(DataConstants.PORTAL,
        "testuser1234", BusinessUnit.EU_ACQUIRING_EEA, 0, true, null);
    assertEquals(1L, response.getSucceedUsersCount());
    verify(userService, Mockito.times(1)).getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any());
  }

  @Test
  public void updateUsersBusinessUnit_updatingBasedOnUsIgamingUserRole_shouldSucceed() throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    userListResponseResource.getUsers().get(0).getAccessResources().get(0).setRole("BP_US_ADMIN");
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    when(moduleService.getBusinessInitiativesForIds(any(), any()))
        .thenReturn(new HashSet<>(Arrays.asList(BusinessUnit.EU_ACQUIRING_EEA.toString())));
    UsersBusinessUnitUpdateResponse response = userHandlerServiceImplMock.updateUsersBusinessUnit(DataConstants.PORTAL,
        "testuser1234", BusinessUnit.EU_ACQUIRING_EEA, 0, true, null);
    assertEquals(1L, response.getSucceedUsersCount());
    verify(userService, Mockito.times(1)).getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any());
  }

  @Test(expected = BadRequestException.class)
  public void updateUsersBusinessUnit_withInvalidInput_throwsException() throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    userHandlerServiceImplMock.updateUsersBusinessUnit(DataConstants.PORTAL, "testuser1234", null, 0, false, null);
  }

  @Test
  public void syncOktaToUsersDb_withValidSingleUserAsInput_shouldSucceed() throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    UserDataSyncResponseResource response =
        userHandlerServiceImplMock.syncOktaToUsersDb(DataConstants.PORTAL, "testuser1234", null, null, 0);
    assertEquals(1L, response.getSucceedUsersCount());
  }

  @Test(expected = BadRequestException.class)
  public void syncOktaToUsersDb_whenApplicationIsNull_throwsException() throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    userHandlerServiceImplMock.syncOktaToUsersDb(null, "testuser1234", null, null, 0);
  }

  @Test(expected = BadRequestException.class)
  public void syncOktaToUsersDb_whenUserNotFound_throwsException() throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any())).thenReturn(Optional.empty());
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    userHandlerServiceImplMock.syncOktaToUsersDb(DataConstants.PORTAL, "testuser1234", null, null, 0);
  }

  @Test
  public void syncOktaToUsersD_withValidListUsersAsInput_shouldSucceed() throws JsonProcessingException {
    List<User> users = new ArrayList<>(Arrays.asList(UserTestUtility.getUser()));
    Page<User> userPage = new PageImpl<>(users);
    when(usersRepository.findByOwnerTypeAndOwnerIdAndApplicationOrderByLastModifiedDateDesc(any(), any(), any(), any()))
        .thenReturn(userPage);
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));

    UserDataSyncResponseResource response =
        userHandlerServiceImplMock.syncOktaToUsersDb(DataConstants.PORTAL, null, null, null, 20);
    assertEquals(1L, response.getSucceedUsersCount());
  }

  @Test(expected = BadRequestException.class)
  public void syncOktaToUsersD_withListUsersAsInputAndNoMatchFound_shouldSucceed() throws JsonProcessingException {
    List<User> users = new ArrayList<>();
    Page<User> userPage = new PageImpl<>(users);
    when(usersRepository.findByOwnerTypeAndOwnerIdAndApplicationOrderByLastModifiedDateDesc(any(), any(), any(), any()))
        .thenReturn(userPage);
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("testUserId");
    when(identityManagementFeignClient.getUser(anyString(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    userHandlerServiceImplMock.syncOktaToUsersDb(DataConstants.PORTAL, null, null, null, 20);
  }

  @Test
  public void syncOktaToUsersDb_whenUserNotFoundInIdm_shouldRecordFailedUserCount() throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    when(identityManagementFeignClient.getUser(anyString(), any())).thenReturn(null);
    UserDataSyncResponseResource response =
        userHandlerServiceImplMock.syncOktaToUsersDb(DataConstants.PORTAL, "testuser1234", null, null, 0);
    assertEquals(1L, response.getFailedUsersCount());
  }

  @Test
  public void syncOktaToUsersDb_whenInternalServerErrorOccured_shouldRecordFailedUserCount()
      throws JsonProcessingException {
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    when(userAssembler.toUpdationDto(any())).thenReturn(UserTestUtility.getUserUpdationDto());
    when(userService.updateUser(any(), any())).thenReturn(UserTestUtility.getUserProvisioningUserResource());
    when(identityManagementFeignClient.getUser(anyString(), any())).thenThrow(ArrayIndexOutOfBoundsException.class);
    UserDataSyncResponseResource response =
        userHandlerServiceImplMock.syncOktaToUsersDb(DataConstants.PORTAL, "testuser1234", null, null, 0);
    assertEquals(1L, response.getFailedUsersCount());
  }

  @Test
  public void updateStausInOktaAndUserDb_shouldSucceed() throws JsonProcessingException {
    List<User> users = new ArrayList<>(Arrays.asList(UserTestUtility.getUser()));
    Page<User> userPage = new PageImpl<>(users);
    when(usersRepository.findByApplicationAndStatus(any(), any(), any())).thenReturn(userPage);
    List<UserAccessGroupMappingDao> dao = new ArrayList<>();
    dao.add(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.findByLoginName(any(), any())).thenReturn(dao);
    doNothing().when(userProvisioningUtils).updateUserInIdmService(any(), any(),any());
    userHandlerServiceImplMock.updateStausInOktaAndUserDb(DataConstants.SKRILL, Status.ACTIVE, "");
    verify(usersRepository, Mockito.times(2)).findByApplicationAndStatus(any(), any(), any());
  }

  @Test
  public void updateStausInOktaAndUserDb_withUserId_shouldSucceed() throws JsonProcessingException {
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());
    List<UserAccessGroupMappingDao> dao = new ArrayList<>();
    dao.add(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.findByLoginName(any(), any())).thenReturn(dao);
    doNothing().when(userProvisioningUtils).updateUserInIdmService(any(), any(),any());
    userHandlerServiceImplMock.updateStausInOktaAndUserDb(DataConstants.SKRILL, Status.ACTIVE, "user1");
    verify(usersRepository, Mockito.times(1)).findByUserId(any());
  }
}
