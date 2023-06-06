// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paysafe.op.errorhandling.web.handlers.OneplatformDefaultControllerAdvice;
import com.paysafe.upf.user.provisioning.enums.BusinessUnit;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.UserHandlerService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.service.WalletService;
import com.paysafe.upf.user.provisioning.util.JsonUtil;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserPasswordMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.ChangePasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserDataSyncResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserPasswordMigrationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserUpdationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersBusinessUnitUpdateResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.validator.UserDetailsRequestValidator;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.BeanUtils;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.ArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class UserControllerTest {

  private static final String AUTHORIZATION = "Authorization";

  private MockMvc mockMvc;

  @InjectMocks
  @Spy
  private UserController userController;

  @Mock
  private UserService userServiceMock;

  @Mock
  private UserHandlerService eventHookHandlerServiceMock;

  @Mock
  private UserAssembler userAssembler;

  @Mock
  private WalletService walletService;

  @Mock
  private UserDetailsRequestValidator userDetailsRequestValidator;

  private IdentityManagementUserResource identityManagementUserResource;
  private UserPasswordMigrationDto userPasswordMigrationDto;
  private UserDto userDto;
  private UserUpdationDto userUpdationDto;
  private UserPasswordMigrationResource userPasswordMigrationResource;
  private UserResource userResource;
  private UserUpdationResource userUpdationResource;
  private UserProvisioningUserResource userProvisioningUserResource;

  private static final String VALIDATE_USERNAME_EMAIL_PATH = "/user-provisioning/v1/users/validate";

  /**
   * Setup test configuration.
   *
   * @throws Exception exception
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    this.identityManagementUserResource = UserTestUtility.getIdentityManagementUserResource();
    this.userPasswordMigrationDto = UserTestUtility.getUserMigrationDto();
    this.userDto = UserTestUtility.getUserDto();
    this.userPasswordMigrationResource = UserTestUtility.getUserMigrationResource();
    this.userResource = UserTestUtility.getUserResource();
    this.userUpdationResource = UserTestUtility.getUserUpdationResource();
    this.userProvisioningUserResource = new UserProvisioningUserResource();
    BeanUtils.copyProperties(identityManagementUserResource, userProvisioningUserResource);
    final StaticApplicationContext applicationContext = new StaticApplicationContext();
    applicationContext.registerSingleton("exceptionHandler", OneplatformDefaultControllerAdvice.class);

    final WebMvcConfigurationSupport webMvcConfigurationSupport = new WebMvcConfigurationSupport();
    webMvcConfigurationSupport.setApplicationContext(applicationContext);

    mockMvc = MockMvcBuilders.standaloneSetup(this.userController).build();
  }

  @Test
  public void testMigrateUser() throws Exception {
    final String jsonRequestObject = JsonUtil.toJsonString(userPasswordMigrationResource);

    when(this.userServiceMock.migrateUser(any(), any(), any())).thenReturn(identityManagementUserResource);
    when(userAssembler.toUserMigrationDto(any(UserPasswordMigrationResource.class)))
        .thenReturn(userPasswordMigrationDto);

    this.mockMvc
        .perform(MockMvcRequestBuilders.post("/admin/user-provisioning/v1/users/{userId}/migrate", "testuser")
            .content(jsonRequestObject).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk()).andDo(print())
        .andExpect(jsonPath("$.userName").value(identityManagementUserResource.getUserName()))
        .andExpect(jsonPath("$.email").value(identityManagementUserResource.getEmail()))
        .andExpect(jsonPath("$.pmleId").value(identityManagementUserResource.getPmleId()))
        .andExpect(jsonPath("$.activate").value(identityManagementUserResource.getActivate()))
        .andExpect(jsonPath("$.status").value(identityManagementUserResource.getStatus().toString()))
        .andExpect(jsonPath("$.firstName").value(identityManagementUserResource.getFirstName()))
        .andExpect(jsonPath("$.lastName").value(identityManagementUserResource.getLastName()))
        .andExpect(jsonPath("$.id").value(identityManagementUserResource.getId()))
        .andExpect(jsonPath("$.roles[0]").value(identityManagementUserResource.getRoles().get(0)))
        .andExpect(jsonPath("$.roles[1]").value(identityManagementUserResource.getRoles().get(1)))
        .andExpect(jsonPath("$.accessGroups[0]").value(identityManagementUserResource.getAccessGroups().get(0)))
        .andExpect(jsonPath("$.accessGroups[1]").value(identityManagementUserResource.getAccessGroups().get(1)));
  }

  @Test
  public void testCreateUser() throws Exception {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    final String jsonRequestObject = JsonUtil.toJsonString(userResource);
    when(this.userServiceMock.createUser(any())).thenReturn(userProvisioningUserResource);
    when(userAssembler.toUserCreationDto(any())).thenReturn(userDto);
    doNothing().when(userDetailsRequestValidator).valiadteCreateUserRequest(any());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post("/admin/user-provisioning/v1/users").content(jsonRequestObject)
            .header(AUTHORIZATION, UserTestUtility.AUTH_TOKEN_SKRILL).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isCreated()).andDo(print())
        .andExpect(jsonPath("$.userName").value(identityManagementUserResource.getUserName()))
        .andExpect(jsonPath("$.email").value(identityManagementUserResource.getEmail()));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void createUser_withApplicationHeader_shouldSucceed() throws Exception {
    final String jsonRequestObject = JsonUtil.toJsonString(userResource);
    UserProvisioningUserResource userProvisioningUserResource = new UserProvisioningUserResource();
    BeanUtils.copyProperties(identityManagementUserResource, userProvisioningUserResource);
    when(this.userServiceMock.createUser(any())).thenReturn(userProvisioningUserResource);
    when(userAssembler.toUserCreationDto(any())).thenReturn(userDto);
    doNothing().when(userDetailsRequestValidator).valiadteCreateUserRequest(any());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post("/admin/user-provisioning/v1/users").content(jsonRequestObject)
            .header("Application", DataConstants.SKRILL).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isCreated()).andDo(print())
        .andExpect(jsonPath("$.userName").value(identityManagementUserResource.getUserName()))
        .andExpect(jsonPath("$.email").value(identityManagementUserResource.getEmail()));
  }

  @Test
  public void testUpdateUser() throws Exception {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    final String jsonRequestObject = JsonUtil.toJsonString(userUpdationResource);

    when(this.userServiceMock.updateUser(any(), any())).thenReturn(userProvisioningUserResource);
    when(userAssembler.toUpdationDto(any())).thenReturn(userUpdationDto);

    this.mockMvc
        .perform(MockMvcRequestBuilders.put("/admin/user-provisioning/v1/users/{userId}", "testuser")
            .content(jsonRequestObject).header(AUTHORIZATION, UserTestUtility.AUTH_TOKEN_SKRILL)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk()).andDo(print())
        .andExpect(jsonPath("$.userName").value(identityManagementUserResource.getUserName()))
        .andExpect(jsonPath("$.email").value(identityManagementUserResource.getEmail()));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testGetUser() throws Exception {
    when(this.userServiceMock.fetchUser(any())).thenReturn(identityManagementUserResource);

    this.mockMvc.perform(MockMvcRequestBuilders.get("/admin/user-provisioning/v1/users/{userId}", "testuser"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userName").value(identityManagementUserResource.getUserName()))
        .andExpect(jsonPath("$.email").value(identityManagementUserResource.getEmail()));

  }

  @Test
  public void testChangePassword() throws Exception {
    ChangePasswordRequestResource changePasswordRequestResource = new ChangePasswordRequestResource();
    changePasswordRequestResource.setNewPassword("abcd");
    changePasswordRequestResource.setPassword("1234");
    final String jsonRequestObject = JsonUtil.toJsonString(changePasswordRequestResource);
    when(userServiceMock.changePassword(any(), any())).thenReturn("sampleUserId");
    this.mockMvc.perform(MockMvcRequestBuilders
        .post("/admin/user-provisioning/v1/users/{userId}/changePassword", "sampleUserId").content(jsonRequestObject)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetUsersByFilters() throws Exception {
    when(userServiceMock.getUsersByFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any(), anyBoolean(), anyBoolean())).thenReturn(new UsersListResponseResource());
    this.mockMvc
        .perform(
            MockMvcRequestBuilders.get("/admin/user-provisioning/v1/users/filter").param("createdDate", "27.09.2020"))
        .andExpect(status().isOk());
  }

  @Test
  public void testValidateLoginNameAndEmailAvailability() throws Exception {
    doNothing().when(userServiceMock).validateLoginNameAndEmailAvailability(Mockito.any(), Mockito.any());

    this.mockMvc
        .perform(
            MockMvcRequestBuilders.get(VALIDATE_USERNAME_EMAIL_PATH + "?loginName=abcdefgh&emailId=test@paysafe.com")
                .header("Authorization", StringUtils.EMPTY).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void testMfaStatus() throws Exception {
    doNothing().when(userServiceMock).updateMfaStatus(anyList(), anyBoolean());
    this.mockMvc.perform(
        MockMvcRequestBuilders.patch("/admin/user-provisioning/v1/users/mfaStatus" + "?userIds=1234&mfaEnabled=true")
            .header("Application", "PORTAL").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void resendActivationEmail_withValidData_shouldSucceed() throws Exception {
    doNothing().when(userServiceMock).sendUserActivationEmail(any());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post("/admin/user-provisioning/v1/users/{userId}/activationEmail", "testuser"))
        .andExpect(status().isAccepted());
  }

  @Test
  public void updateUserStatus_withValidData_shouldSucceed() throws Exception {
    UpdateUserStatusResource updateUserStatusResource = UserTestUtility.getUpdateUserStatusResource();
    final String requestObjectContent = JsonUtil.toJsonString(updateUserStatusResource);
    doNothing().when(userServiceMock).updateUserStatus(any(), any());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post("/admin/user-provisioning/v1/users/{userId}/status", "1234")
            .content(requestObjectContent).header("Application", DataConstants.SKRILL)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void testgetLinkedWalletsByUserId() throws Exception {
    when(walletService.findLinkedWalletsByLinkedBrands(anyString(), anyString())).thenReturn(new ArrayList<>());
    this.mockMvc
        .perform(MockMvcRequestBuilders.get("/admin/user-provisioning/v1/users/{walletId}/linkedWallets", "1234")
            .header("Application", DataConstants.SKRILL).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void testVerifyEventHookForSuspendedStatus() throws Exception {
    this.mockMvc
        .perform(MockMvcRequestBuilders.get("/admin/user-provisioning/v1/suspended-event-hook")
            .header("X-Okta-Verification-Challenge", "testVerification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void eventHookForActivateUser_withValidInput_shouldSucceed() throws Exception {
    String json = "{\"id\":1,\"name\":\"John Doe\"}";
    doNothing().when(eventHookHandlerServiceMock).activateUserWithOkta(any());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post("/admin/user-provisioning/v1/activate-event-hook")
            .header("X-Okta-Verification-Challenge", "testVerification").content(json)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void validateEventHookForActivateUser_withValidInput_shouldSucceed() throws Exception {
    this.mockMvc
        .perform(MockMvcRequestBuilders.get("/admin/user-provisioning/v1/activate-event-hook")
            .header("X-Okta-Verification-Challenge", "testVerification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void testEventHookForSuspendedStatus() throws Exception {
    String json = "{\"id\":1,\"name\":\"John Doe\"}";
    doNothing().when(eventHookHandlerServiceMock).updateUserStatusAndAuditTableFromOkta(any());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post("/admin/user-provisioning/v1/suspended-event-hook")
            .header("X-Okta-Verification-Challenge", "testVerification").content(json)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void testVerifyEventHookForLockedOutStatus() throws Exception {
    this.mockMvc
        .perform(MockMvcRequestBuilders.get("/admin/user-provisioning/v1/session-start-event-hook")
            .header("X-Okta-Verification-Challenge", "testVerification")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void testEventHookForLockedOutStatus() throws Exception {
    String json = "{\"id\":1,\"name\":\"John Doe\"}";
    doNothing().when(eventHookHandlerServiceMock).updateUserStatusAndAuditTableFromOkta(any());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post("/admin/user-provisioning/v1/session-start-event-hook")
            .header("X-Okta-Verification-Challenge", "testVerification").content(json)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void getUserAccessGroupIds_withValidInput_shouldSucceed() throws Exception {
    when(userServiceMock.getUserAccessGroupIds(any(), any())).thenReturn(UserTestUtility.getAccessGroups());
    this.mockMvc.perform(MockMvcRequestBuilders
        .get("/admin/user-provisioning/v1/users/testuser@paysafe.com/accessgroupids").header("Application", "SKRILL"))
        .andExpect(status().isOk());
  }

  @Test
  public void getWalletUsers_withValidInput_shouldSucceed() throws Exception {
    when(eventHookHandlerServiceMock.getUserCount(any(), any()))
        .thenReturn(UserTestUtility.getWalletUserCountResource());
    this.mockMvc.perform(MockMvcRequestBuilders.get("/admin/user-provisioning/v1/users/count")
        .header("Application", "SKRILL").param("resourceId", "1234").param("resourceName", DataConstants.WALLETS))
        .andExpect(status().isOk());
  }

  @Test
  public void getBulkWalletUsers_withValidInput_shouldSucceed() throws Exception {
    when(eventHookHandlerServiceMock.getUserCount(any(), any()))
        .thenReturn(UserTestUtility.getWalletUserCountResource());
    this.mockMvc.perform(MockMvcRequestBuilders.get("/admin/user-provisioning/v1/users/bulkcount").param("userName",
        "testuser@paysafe.com")).andExpect(status().isOk());
  }

  @Test
  public void deleteUser_shouldSucceed() throws Exception {
    doNothing().when(eventHookHandlerServiceMock).deleteUser(any(), any(), any());
    this.mockMvc.perform(MockMvcRequestBuilders.delete("/admin/user-provisioning/v1/users/userid"))
        .andExpect(status().isOk());
  }

  @Test
  public void testDownloadUserEmails() throws Exception {
    when(userServiceMock.downloadUserEmails(anyString()))
        .thenReturn(new ResponseEntity<>(new ByteArrayResource("sample".getBytes()), HttpStatus.OK));
    this.mockMvc.perform(MockMvcRequestBuilders.get("/admin/user-provisioning/v1/backoffice/users/email/download")
        .header("Application", "SKRILL"))
        .andExpect(status().isOk());
  }

  @Test
  public void syncUserFieldsFromOktaToUserDb_withValidInput_shouldSucceed() throws Exception {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(eventHookHandlerServiceMock.syncOktaToUsersDb(any(), any(), any(), any(), any()))
        .thenReturn(new UserDataSyncResponseResource());
    this.mockMvc.perform(MockMvcRequestBuilders.patch("/admin/user-provisioning/v1/users/data-sync")
        .header(AUTHORIZATION, UserTestUtility.AUTH_TOKEN_PORTAL).param("ownerId", "1234").param("ownerType", "PMLE"))
        .andExpect(status().isOk());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void updateStausInOktaAndUserDb_withValidInput_shouldSucceed() throws Exception {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    this.mockMvc.perform(MockMvcRequestBuilders.patch("/admin/user-provisioning/v1/users/status-update")
                    .header(AUTHORIZATION, UserTestUtility.AUTH_TOKEN_PORTAL).param("status", "ACTIVE"))
            .andExpect(status().isOk());
    CommonThreadLocal.unsetAuthLocal();
  }


  @Test
  public void updateUsersBusinessUnit_withValidInput_shouldSucceed() throws Exception {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(eventHookHandlerServiceMock.updateUsersBusinessUnit(any(), any(), any(), any(), anyBoolean(), any()))
        .thenReturn(new UsersBusinessUnitUpdateResponse());
    this.mockMvc
        .perform(MockMvcRequestBuilders.patch("/admin/user-provisioning/v1/users/business-unit")
            .header(AUTHORIZATION, UserTestUtility.AUTH_TOKEN_PORTAL)
            .param("businessUnit", BusinessUnit.EU_ACQUIRING_EEA.toString()).param("loginName", "user1234"))
        .andExpect(status().isOk());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testResetFactor() throws Exception {
    when(userServiceMock.resetFactor(any(),any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    mockMvc
            .perform(MockMvcRequestBuilders.delete("/admin/user-provisioning/v1/abc/factors")
                    .header("Application", "PORTAL")
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk());
  }
}
