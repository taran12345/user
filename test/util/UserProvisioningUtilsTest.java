// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import static com.paysafe.upf.user.provisioning.util.UserTestUtility.getIdentityManagementUserList;
import static com.paysafe.upf.user.provisioning.util.UserTestUtility.getIdentityManagementUserListWithEmptyUsers;
import static com.paysafe.upf.user.provisioning.util.UserTestUtility.getPegasusUserListWithEmptyUsers;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.InvalidFieldException;
import com.paysafe.upf.user.provisioning.config.BusinessUnitConfig;
import com.paysafe.upf.user.provisioning.config.OktaAppConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.config.UserConfig;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.Action;
import com.paysafe.upf.user.provisioning.enums.SkrillPermissions;
import com.paysafe.upf.user.provisioning.enums.UserAction;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.exceptions.UserProvisioningException;
import com.paysafe.upf.user.provisioning.feignclients.AccessGroupFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.PegasusFeignClient;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.repository.WalletPermissionRepository;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.service.FeatureFlagService;
import com.paysafe.upf.user.provisioning.service.MerchantAccountInfoService;
import com.paysafe.upf.user.provisioning.service.SkrillTellerAccountInfoService;
import com.paysafe.upf.user.provisioning.service.UserHandlerService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.utils.AuditUserEventUtil;
import com.paysafe.upf.user.provisioning.utils.CommonUserProvisioningUtil;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.AppUserConfigDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.ReportSchedule;
import com.paysafe.upf.user.provisioning.web.rest.dto.ReportScheduleResponse;
import com.paysafe.upf.user.provisioning.web.rest.dto.ReportScheduleStatus;
import com.paysafe.upf.user.provisioning.web.rest.dto.ResourceUsersValidationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicyRight;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserAccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.WalletUserCountResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.hamcrest.core.Is;
import org.junit.Before;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UserProvisioningUtilsTest {

  @Mock
  private UserService userService;

  @Mock
  private AuditService auditService;

  @Mock
  private UserConfig userConfig;

  @Mock
  private AccessGroupFeignClient accessGroupFeignClient;

  @Mock
  private OktaAppConfig oktaAppConfig;

  @Mock
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private PegasusFeignClient pegasusFeignClient;

  @Mock
  private WalletPermissionRepository walletPermissionRepository;

  @Mock
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Mock
  private UserHandlerService userHandlerService;

  @Mock
  private SkrillTellerAccountInfoService skrillTellerAccountInfoService;

  @Mock
  private MerchantAccountInfoService merchantAccountInfoService;

  @Mock
  private UserAssembler userAssembler;

  @Mock
  private AuditUserEventUtil auditUserEventUtil;

  @Mock
  private SkrillTellerConfig skrillTellerConfig;

  @InjectMocks
  private UserProvisioningUtils userProvisioningUtils;

  @Mock
  private FeatureFlagService featureFlagService;

  @Mock
  private CommonUserProvisioningUtil commonUserProvisioningUtil;

  private WalletUserCountResource walletUserCountResource;
  private AppUserConfigDto appUserConfigDto;

  /**
   * Data initialization.
   */
  @Before
  public void setUp() throws Exception {
    walletUserCountResource = new WalletUserCountResource();
    walletUserCountResource.setAdminCount((long) 2);
    walletUserCountResource.setUserCount((long) 10);
    appUserConfigDto = new AppUserConfigDto();
    appUserConfigDto.setAdminCount(3);
    appUserConfigDto.setUserCount(60);
    when(auditUserEventUtil.constructAuditUserEventDto(any(), any()))
        .thenReturn(AuditUserEventDto.builder().eventData(""));
    when(usersRepository.findByUserExternalId(any())).thenReturn(UserTestUtility.getUser());
    HashMap<String, Boolean> features = new HashMap<>();
    features.put("ST_DeleteReports", true);
    when(featureFlagService.fetchFeatureFlag()).thenReturn(features);
  }

  @Test
  public void test_verifyUserCountforResource_limitNotExceeded() {
    when(userHandlerService.getUserCount(anyString(), anyString())).thenReturn(walletUserCountResource);
    Map<String, AppUserConfigDto> appUserLimit = new HashMap<>();
    appUserLimit.put(DataConstants.SKRILL, appUserConfigDto);
    when(userConfig.getAppUserLimit()).thenReturn(appUserLimit);
    userProvisioningUtils.verifyUserCountforResource("resourceId", "resourceName", DataConstants.SKRILL);
    verify(commonUserProvisioningUtil, times(1))
        .verifyUserCountforResource(anyString(), anyString(), anyString());
//    verify(userHandlerService, times(1)).getUserCount(anyString(), anyString());
//    verify(userConfig, times(1)).getAppUserLimit();
  }

  @Test
  public void test_verifyUserCountforResource_limitExceeded() {
    walletUserCountResource.setAdminCount((long) 4);
    walletUserCountResource.setUserCount((long) 65);
    when(userHandlerService.getUserCount(anyString(), anyString())).thenReturn(walletUserCountResource);
    Map<String, AppUserConfigDto> appUserLimit = new HashMap<>();
    appUserLimit.put(DataConstants.SKRILL, appUserConfigDto);
    when(userConfig.getAppUserLimit()).thenReturn(appUserLimit);
    userProvisioningUtils.verifyUserCountforResource("resourceId", "resourceName", DataConstants.SKRILL);
//    verify(userHandlerService, times(1)).getUserCount(anyString(), anyString());
//    verify(userConfig, times(1)).getAppUserLimit();
    verify(commonUserProvisioningUtil, times(1))
        .verifyUserCountforResource(anyString(), anyString(), anyString());
  }

  @Test
  public void test_verifyUserCountforResource_whenAppUserConfigNotPresent() {
    when(userHandlerService.getUserCount(anyString(), anyString())).thenReturn(walletUserCountResource);
    AppUserConfigDto appUserConfigDto = null;
    Map<String, AppUserConfigDto> appUserLimit = new HashMap<>();
    appUserLimit.put(DataConstants.SKRILL, appUserConfigDto);
    when(userConfig.getAppUserLimit()).thenReturn(appUserLimit);
    userProvisioningUtils.verifyUserCountforResource("resourceId", "resourceName", DataConstants.SKRILL);
//    assertNotNull(resourceUsersValidationDto);
//    assertThat(resourceUsersValidationDto.getCanAddAdminUsers(), Is.is(true));
//    assertThat(resourceUsersValidationDto.getCanAddUsers(), Is.is(true));
//    verify(userHandlerService, times(1)).getUserCount(anyString(), anyString());
//    verify(userConfig, times(1)).getAppUserLimit();
    verify(commonUserProvisioningUtil, times(1))
        .verifyUserCountforResource(anyString(), anyString(), anyString());
  }

  @Test
  public void testValidateAccessResources_withEmptyTokenAccessGroups_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    AccessGroupResponseResource accessGroupResponseResource = UserTestUtility.getAccessGroupResponseResource();
    accessGroupResponseResource.setMerchantId("TEST_WALLET_ID2");
    accessGroupResponseResource.setType(AccessGroupType.DEFAULT_ADMIN);
    List<AccessGroupResponseResource> accessGroupResponseResources =
        UserTestUtility.getAccessGroupsFromInputListSuccessResponse();
    accessGroupResponseResources.get(0).setType(AccessGroupType.DEFAULT_ADMIN);
    accessGroupResponseResources.add(accessGroupResponseResource);
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any())).thenReturn(accessGroupResponseResources);
    List<AccessGroupResponseResource> accessGroupResponseResourceList =
        UserTestUtility.getAccessGroupsFromInputListSuccessResponse();
    accessGroupResponseResourceList.add(accessGroupResponseResource);
    AccessGroupResponseResource accessGroupResponseResource1 = UserTestUtility.getAccessGroupResponseResource();
    accessGroupResponseResource1.setMerchantId("TEST_WALLET_ID3");
    accessGroupResponseResourceList.add(accessGroupResponseResource1);
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userProvisioningUtils.validateAccessResources(userUpdationDto, accessGroupResponseResourceList);
    assertThat(userUpdationDto.getAccessResources().size(), Is.is(2));
  }

  @Test
  public void testValidateAccessResources_withTokenAccessGroups_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    AuthorizationInfo authInfo = CommonThreadLocal.getAuthLocal();
    authInfo.setAccessGroups(Arrays.asList("12345656"));
    AccessGroupResponseResource accessGroupResponseResource = UserTestUtility.getAccessGroupResponseResource();
    accessGroupResponseResource.setMerchantId("TEST_WALLET_ID2");
    accessGroupResponseResource.setType(AccessGroupType.DEFAULT_ADMIN);
    List<AccessGroupResponseResource> accessGroupResponseResources =
        UserTestUtility.getAccessGroupsFromInputListSuccessResponse();
    accessGroupResponseResources.get(0).setType(AccessGroupType.DEFAULT_ADMIN);
    accessGroupResponseResources.add(accessGroupResponseResource);
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any())).thenReturn(accessGroupResponseResources);
    when(userService.getUserAccessGroupIds(any(), any())).thenReturn(UserTestUtility.getAccessGroups());
    List<AccessGroupResponseResource> accessGroupResponseResourceList =
        UserTestUtility.getAccessGroupsFromInputListSuccessResponse();
    accessGroupResponseResourceList.add(accessGroupResponseResource);
    AccessGroupResponseResource accessGroupResponseResource1 = UserTestUtility.getAccessGroupResponseResource();
    accessGroupResponseResource1.setMerchantId("TEST_WALLET_ID3");
    accessGroupResponseResourceList.add(accessGroupResponseResource1);
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userProvisioningUtils.validateAccessResources(userUpdationDto, accessGroupResponseResourceList);
    verify(commonUserProvisioningUtil, times(1)).validateAccessResources(any(), anyList());
//    verify(accessGroupFeignClient, times(1)).getAccessGroupsFromInputList(any());
//    assertThat(userUpdationDto.getAccessResources().size(), Is.is(3));
  }

  @Test
  public void setOwnerInfo_withAuthorization_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UserDto userDto = UserTestUtility.getUserDto();
    userProvisioningUtils.setOwnerInfo(userDto);
    CommonThreadLocal.unsetAuthLocal();
    assertThat(userDto.getOwnerType(), Is.is("PMLE"));
  }

  @Test
  public void populdateAccessResourcesGetUsers_withValidData_shouldSucceed() {
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any()))
        .thenReturn(UserTestUtility.getAccessGroupResponseResourceList());
    userProvisioningUtils.populateAccessResourcesGetUsers(UserTestUtility.getUserResponseResourceList());
    verify(accessGroupFeignClient, times(1)).getAccessGroupsFromInputList(any());
  }

  @Test
  public void populdateAccessResourcesGetUsers_withPredefinedRole_shouldSucceed() {
    List<AccessGroupResponseResource> accessGroups = UserTestUtility.getAccessGroupResponseResourceList();
    accessGroups.get(0).getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights().get(0).getAccessRight()
        .setAccessRole("BP_DEVELOPER");
    accessGroups.get(0).getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights().get(0).getAccessRight()
        .setAccessRolePermissions(UserTestUtility.getAccessRolePermissions());
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any())).thenReturn(accessGroups);
    userProvisioningUtils.populateAccessResourcesGetUsers(UserTestUtility.getUserResponseResourceList());
    verify(accessGroupFeignClient, times(1)).getAccessGroupsFromInputList(any());
  }

  @Test
  public void populdateAccessResourcesGetUsers_withResourceIds_shouldSucceed() {
    List<AccessPolicyRight> accessPolicyRights = new ArrayList<>();
    AccessPolicyRight accessPolicyRight1 = UserTestUtility.getAccessPolicyRight();
    accessPolicyRight1.getAccessRight().setResourceId("RESOURCE_ID1");
    AccessPolicyRight accessPolicyRight2 = UserTestUtility.getAccessPolicyRight();
    accessPolicyRight2.getAccessRight().setResourceId("RESOURCE_ID2");
    accessPolicyRights.add(accessPolicyRight1);
    accessPolicyRights.add(accessPolicyRight2);
    List<AccessGroupResponseResource> accessGroups = UserTestUtility.getAccessGroupResponseResourceList();
    accessGroups.get(0).getAccessGroupPolicies().get(0).getAcessPolicy().setAccessPolicyRights(accessPolicyRights);
    accessGroups.get(0).getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights().get(0).getAccessRight()
        .setResourceType(DataConstants.PAYMENT_ACCOUNT);
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any())).thenReturn(accessGroups);
    userProvisioningUtils.populateAccessResourcesGetUsers(UserTestUtility.getUserResponseResourceList());
    verify(accessGroupFeignClient, times(1)).getAccessGroupsFromInputList(any());
  }

  @Test
  public void populdateAccessResourcesCreateUser_withValidData_shouldSucceed() {
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any()))
        .thenReturn(UserTestUtility.getAccessGroupResponseResourceList());
    userProvisioningUtils.populateAccessResources(UserTestUtility.getUserProvisioningUserResource());
    verify(accessGroupFeignClient, times(1)).getAccessGroupsFromInputList(any());
  }

  @Test
  public void deleteDuplicateAccessResourcesFromRequest_withValidData_shouldSucceed() {
    List<UpdateUserAccessResources> accessResources = UserTestUtility.getUpdateUserAccessResourcesList();
    accessResources.get(0).setAction(Action.ADD);
    accessResources.get(1).setAction(Action.HARD_DELETE);
    userProvisioningUtils.deleteDuplicateAccessResourcesFromRequest(accessResources);
    assertThat(accessResources.size(), Is.is(1));
    assertThat(accessResources.get(0).getAction(), Is.is(Action.ADD));
  }

  @Test
  public void setOwnerInfoUpdateUser_withAuthorization_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userProvisioningUtils.setOwnerInfoUpdateUser(userUpdationDto);
    assertThat(userUpdationDto.getOwnerType(), Is.is("PMLE"));
    CommonThreadLocal.unsetAuthLocal();
  }

  // updateUserStatus() testCases

  @Test(expected = BadRequestException.class)
  public void updateUserStatus_withInvalidData_shouldThrowException() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.BLOCKED);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setApplicationName("SKRILL");
    ReportScheduleResponse res1 = new ReportScheduleResponse();
    res1.setContent(new ArrayList<>());
    when(skrillTellerAccountInfoService.getSchedules(anyString(), any())).thenReturn(res1);
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.BLOCKED, UserStatus.ACTIVE, false);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void updateUserStatus_withEmptyAccessResource_shouldThrowException() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.BLOCKED);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setApplicationName("SKRILL");
    ReportScheduleResponse res1 = new ReportScheduleResponse();
    res1.setContent(new ArrayList<>());
    when(skrillTellerAccountInfoService.getSchedules(anyString(), any())).thenReturn(res1);
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.BLOCKED, UserStatus.PROVISIONED, true);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void updateUserStatus_statusActiveToDelete_shouldThrowException() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.BLOCKED);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setApplicationName("SKRILL");
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    ReportScheduleResponse res1 = new ReportScheduleResponse();
    res1.setContent(new ArrayList<>());
    when(skrillTellerAccountInfoService.getSchedules(anyString(), any())).thenReturn(res1);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    UserProvisioningUserResource userProvisioningUserResource = new UserProvisioningUserResource();
    when(userService.updateUser(any(), any())).thenReturn(userProvisioningUserResource);
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    ResourceUsersValidationDto walletUsersValidationDto = new ResourceUsersValidationDto();
    walletUsersValidationDto.setCanAddAdminUsers(true);
    walletUsersValidationDto.setCanAddUsers(true);
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    verify(userService, times(1)).updateUser(any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void updateUserStatus_statusDeleteToActive_shouldSucceed() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.ACTIVATE);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setApplicationName("SKRILL");
    UserProvisioningUserResource userProvisioningUserResource = new UserProvisioningUserResource();
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.BLOCKED, UserStatus.BLOCKED, false);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    when(userService.updateUser(any(), any())).thenReturn(userProvisioningUserResource);
    when(commonUserProvisioningUtil
        .verifyUserCountforResource(any(), any(), any()))
        .thenReturn(new ResourceUsersValidationDto(true, true));
    ResourceUsersValidationDto walletUsersValidationDto = new ResourceUsersValidationDto();
    walletUsersValidationDto.setCanAddAdminUsers(true);
    walletUsersValidationDto.setCanAddUsers(true);
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    verify(userService, times(1)).updateUser(any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void updateUserStatus_statusDeleteToActive_forNeteller_shouldSucceed() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.ACTIVATE);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setApplicationName("NETELLER");
    UserProvisioningUserResource userProvisioningUserResource = new UserProvisioningUserResource();
    UsersListResponseResource userListResponseResource =
            UserTestUtility.getUserListResponseResource(AccessResourceStatus.BLOCKED, UserStatus.BLOCKED, false);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(userListResponseResource);
    when(userService.updateUser(any(), any())).thenReturn(userProvisioningUserResource);
    when(commonUserProvisioningUtil
            .verifyUserCountforResource(any(), any(), any()))
            .thenReturn(new ResourceUsersValidationDto(true, true));
    ResourceUsersValidationDto walletUsersValidationDto = new ResourceUsersValidationDto();
    walletUsersValidationDto.setCanAddAdminUsers(true);
    walletUsersValidationDto.setCanAddUsers(true);
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    verify(userService, times(1)).updateUser(any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void updateUserStatus_statusLockedOutToActive_shouldSucceed() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.ACTIVE_ALL);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setApplicationName("SKRILL");
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.BLOCKED, UserStatus.LOCKED_OUT, false);
    when(usersRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getUser()));
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());
    when(usersRepository.save(any())).thenReturn(UserTestUtility.getUser());
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    when(identityManagementFeignClient.updateUserStatus(any(), any(), any())).thenReturn(new String());
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    verify(identityManagementFeignClient, times(1)).updateUserStatus(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void updateUserStatus_statusActiveToDeactiveForPortal_shouldSucceed() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.BLOCKED);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setApplicationName("SKRILL");
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    doNothing().when(auditService).createAuditEntry(any());
    when(usersRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getUser()));
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());

    when(usersRepository.save(any())).thenReturn(UserTestUtility.getUser());
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    when(identityManagementFeignClient.updateUserStatus(any(), any(), any())).thenReturn(new String());
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    verify(identityManagementFeignClient, times(1)).updateUserStatus(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void updateUserStatus_statusActiveToDeactiveForPartnerPortal_shouldSucceed() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PARTNER_PORTAL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.BLOCKED);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setApplicationName("SKRILL");
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    doNothing().when(auditService).createAuditEntry(any());
    when(usersRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getUser()));
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());

    when(usersRepository.save(any())).thenReturn(UserTestUtility.getUser());
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    when(identityManagementFeignClient.updateUserStatus(any(), any(), any())).thenReturn(new String());
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    verify(identityManagementFeignClient, times(1)).updateUserStatus(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void updateUserStatus_withInCorrectEmail_shouldThrowException() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.ACTIVATE);
    resource.setEmail("incorrectemail.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setApplicationName("SKRILL");
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.BLOCKED, UserStatus.LOCKED_OUT, false);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void updateUserStatus_withInCorrectWalletId_shouldThrowException() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.ACTIVATE);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setResourceId("incorrectWalletId");
    resource.setApplicationName("SKRILL");
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void updateUserStatus_bulkActiveStatus_shouldSucceed() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.ACTIVE_ALL);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setApplicationName("SKRILL");
    List<UserAccessGroupMappingDao> userAccessGroupDaoList = new ArrayList<>();
    userAccessGroupDaoList.add(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.findByLoginNameAndUserAccessGroupStatus(any(), any(),
        any())).thenReturn(userAccessGroupDaoList);
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.BLOCKED, UserStatus.BLOCKED, false);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    when(commonUserProvisioningUtil
        .verifyUserCountforResource(anyString(), anyString(), anyString()))
        .thenReturn(new ResourceUsersValidationDto(true, true));
    UserProvisioningUserResource userProvisioningUserResource = new UserProvisioningUserResource();
    when(userService.updateUser(any(), any())).thenReturn(userProvisioningUserResource);
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    verify(userAccessGroupMapppingRepository, times(1)).findByLoginNameAndUserAccessGroupStatus(any(),
        any(), any());
    verify(userService, times(1)).updateUser(any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void updateUserStatus_bulkBlockdStatus_shouldSucceed() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.BLOCK_ALL);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setApplicationName("SKRILL");
    List<UserAccessGroupMappingDao> userAccessGroupDaoList = new ArrayList<>();
    userAccessGroupDaoList.add(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.findByLoginNameAndUserAccessGroupStatus(any(), any(),
        any())).thenReturn(userAccessGroupDaoList);
    ReportScheduleResponse res = new ReportScheduleResponse();
    res.setContent(new ArrayList<>());
    when(skrillTellerAccountInfoService.getSchedules(anyString(), any())).thenReturn(res);
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    UserProvisioningUserResource userProvisioningUserResource = new UserProvisioningUserResource();
    when(userService.updateUser(any(), any())).thenReturn(userProvisioningUserResource);
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    verify(userService, times(1)).updateUser(any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void updateUserStatus_bulkBlockdStatus_deleteReports() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.BLOCK_ALL);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setApplicationName("SKRILL");
    List<UserAccessGroupMappingDao> userAccessGroupDaoList = new ArrayList<>();
    userAccessGroupDaoList.add(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.findByLoginNameAndUserAccessGroupStatus(any(), any(),
        any())).thenReturn(userAccessGroupDaoList);
    ReportSchedule eachReport = new ReportSchedule();
    eachReport.setReportType("PDF");
    eachReport.setStatus(ReportScheduleStatus.ACTIVE);
    eachReport.setId("1234");
    eachReport.setCustomerId(5678L);
    List<ReportSchedule> reports = new ArrayList<>();
    reports.add(eachReport);
    ReportScheduleResponse res = new ReportScheduleResponse();
    res.setContent(reports);
    when(skrillTellerAccountInfoService.getSchedules(anyString(), any())).thenReturn(res);
    UsersListResponseResource userListResponseResource =
        UserTestUtility.getUserListResponseResource(AccessResourceStatus.ACTIVE, UserStatus.ACTIVE, false);
    when(userService.getUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(userListResponseResource);
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    UserProvisioningUserResource userProvisioningUserResource = new UserProvisioningUserResource();
    when(userService.updateUser(any(), any())).thenReturn(userProvisioningUserResource);
    userProvisioningUtils.updateUserStatus("sampleId", resource);
    verify(userService, times(1)).updateUser(any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void deleteReportsforWallet_shouldSucceed() {
    ReportSchedule eachReport = new ReportSchedule();
    eachReport.setReportType("PDF");
    eachReport.setStatus(ReportScheduleStatus.ACTIVE);
    eachReport.setId("1234");
    eachReport.setCustomerId(5678L);
    List<ReportSchedule> reports = new ArrayList<>();
    reports.add(eachReport);
    ReportScheduleResponse res = new ReportScheduleResponse();
    res.setContent(reports);
    when(skrillTellerAccountInfoService.getSchedules(anyString(), any())).thenReturn(res);
    UserUpdationDto userUpdateDto = new UserUpdationDto();
    List<UpdateUserAccessResources> accessList = new ArrayList<>();
    UpdateUserAccessResources accessRes = new UpdateUserAccessResources();
    accessRes.setAction(Action.HARD_DELETE);
    accessList.add(accessRes);
    userUpdateDto.setAccessResources(accessList);
    userProvisioningUtils.deleteReportsforWallet("sampleId", userUpdateDto);
    verify(skrillTellerAccountInfoService, times(1))
        .deleteScheduleReport(any(), any());
  }

  @Test
  public void validateLoginNameAndEmailAvailabilityWhenBothAreEmpty() {
    try {
      userProvisioningUtils.validateLoginNameAndEmailAvailability("", "");
    } catch (InvalidFieldException e) {
      assertThat(e.getFieldErrorList().size(), Is.is(1));
      assertThat(e.getFieldErrorList().get(0).getDefaultMessage(), Is.is("should be present"));
    }
  }

  @Test
  public void validateLoginNameAndEmailAvailabilityWhenUserPresentWithUserNameInPegasus() {
    when(pegasusFeignClient.getUsers(anyString(), any(), any(), any()))
        .thenReturn(UserTestUtility.getPegasusUserList());
    when(identityManagementFeignClient.getUsersByUserName(anyString()))
        .thenReturn(getIdentityManagementUserListWithEmptyUsers());
    try {
      userProvisioningUtils.validateLoginNameAndEmailAvailability("abc123", "");
    } catch (InvalidFieldException e) {
      assertThat(e.getFieldErrorList().size(), Is.is(1));
      assertThat(e.getFieldErrorList().get(0).getDefaultMessage(),
          Is.is("User name already exists"));
    }
  }

  @Test
  public void validateLoginNameAndEmailAvailabilityWhenUserPresentWithUserNameInOkta() {
    when(pegasusFeignClient.getUsers(anyString(), any(), any(), any())).thenReturn(getPegasusUserListWithEmptyUsers());
    when(identityManagementFeignClient.getUsersByUserName(anyString())).thenReturn(getIdentityManagementUserList());
    try {
      userProvisioningUtils.validateLoginNameAndEmailAvailability("abc123", "");
    } catch (InvalidFieldException e) {
      assertThat(e.getFieldErrorList().size(), Is.is(1));
      assertThat(e.getFieldErrorList().get(0).getDefaultMessage(),
          Is.is("User name already exists"));
    }
  }

  @Test
  public void validateLoginNameAndEmailAvailabilityWhenUserPresentWithEmail() {
    when(identityManagementFeignClient.getUsersByEmail(anyString())).thenReturn(getIdentityManagementUserList());
    try {
      userProvisioningUtils.validateLoginNameAndEmailAvailability("", "abc@paysafe.com");
    } catch (InvalidFieldException e) {
      assertThat(e.getFieldErrorList().size(), Is.is(1));
      assertThat(e.getFieldErrorList().get(0).getDefaultMessage(), Is.is("Email already exists"));
    }
  }

  @Test
  public void validateLoginNameAndEmailAvailabilityWhenUserPresentWithEmailAndUserName() {
    when(identityManagementFeignClient.getUsersByEmail(anyString())).thenReturn(getIdentityManagementUserList());
    when(identityManagementFeignClient.getUsersByUserName(anyString())).thenReturn(getIdentityManagementUserList());
    PegasusUserListResponseResource pegasusUserListResponseResource = getPegasusUserListWithEmptyUsers();
    pegasusUserListResponseResource.setCount(0L);
    pegasusUserListResponseResource.setUsers(new ArrayList<>());
    when(pegasusFeignClient.getUsers(anyString(), any(), any(), any())).thenReturn(pegasusUserListResponseResource);
    try {
      userProvisioningUtils.validateLoginNameAndEmailAvailability("abcd", "abc@paysafe.com");
    } catch (InvalidFieldException e) {
      assertThat(e.getFieldErrorList().size(), Is.is(1));
      assertThat(e.getFieldErrorList().get(0).getDefaultMessage(), Is.is("User name, Email already exists"));
    }
  }

  @Test
  public void validateLoginNameAndEmailAvailabilityWhenUserNotPresent() {
    when(pegasusFeignClient.getUsers(anyString(), any(), any(), any())).thenReturn(getPegasusUserListWithEmptyUsers());
    when(identityManagementFeignClient.getUsersByUserName(anyString()))
        .thenReturn(getIdentityManagementUserListWithEmptyUsers());
    when(identityManagementFeignClient.getUsersByEmail(anyString()))
        .thenReturn(getIdentityManagementUserListWithEmptyUsers());
    userProvisioningUtils.validateLoginNameAndEmailAvailability("abc123", "abc@paysafe.com");
    verify(pegasusFeignClient).getUsers(anyString(), any(), any(), any());
    verify(identityManagementFeignClient).getUsersByUserName(anyString());
    verify(identityManagementFeignClient).getUsersByEmail(anyString());
  }

  @Test
  public void populateSkrillPermissionsWithIds_withValidData_shouldSucceed() {
    UserResponseResource userResponseResource = UserTestUtility.getUserResponseResource();
    userResponseResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    userResponseResource.getAccessResources().get(0).setAccessGroupType(AccessGroupType.DEFAULT_ADMIN);
    userResponseResource.getAccessResources().get(1).setAccessGroupType(AccessGroupType.CUSTOMIZED);
    when(walletPermissionRepository.findWalletPermissionsFromPermissions(anyList()))
        .thenReturn(UserTestUtility.getWalletPermissions());
    when(walletPermissionRepository.findAll()).thenReturn(UserTestUtility.getWalletPermissions());
    AccessGroupResponseResource accessGroup = UserTestUtility.getAccessGroupResponseResource();
    accessGroup.setCode("AG_02");
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any())).thenReturn(Arrays.asList(accessGroup));
    userProvisioningUtils.populateSkrillPermissionsWithIds(userResponseResource);
    verify(walletPermissionRepository, times(1)).findWalletPermissionsFromPermissions(anyList());
    verify(walletPermissionRepository, times(1)).findAll();
  }

  @Test
  public void populateSkrillPermissionsWithIds_withKennyAdminRole_shouldSucceed() {
    UserResponseResource userResponseResource = UserTestUtility.getUserResponseResource();
    userResponseResource.setAccessResources(UserTestUtility.getAccessResourcesList());
    userResponseResource.getAccessResources().get(0).setAccessGroupType(AccessGroupType.DEFAULT_ADMIN);
    userResponseResource.getAccessResources().get(1).setAccessGroupType(AccessGroupType.CUSTOMIZED);
    when(walletPermissionRepository.findWalletPermissionsFromPermissions(anyList()))
        .thenReturn(UserTestUtility.getWalletPermissions());
    when(walletPermissionRepository.findAll()).thenReturn(UserTestUtility.getWalletPermissions());
    AccessGroupResponseResource accessGroup = UserTestUtility.getAccessGroupResponseResource();
    accessGroup.setCode("AG_02");
    accessGroup.getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights().get(0).getAccessRight()
        .setAccessRole(DataConstants.BP_BINANCE_ADMIN);
    accessGroup.getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights().get(0).getAccessRight()
        .setAccessTypeValue(null);
    accessGroup.getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights().get(0).getAccessRight()
        .setAccessRolePermissions(new ArrayList<>(Arrays.asList("permission1", "permission2")));
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any())).thenReturn(Arrays.asList(accessGroup));
    userProvisioningUtils.populateSkrillPermissionsWithIds(userResponseResource);
    verify(walletPermissionRepository, times(1)).findWalletPermissionsFromPermissions(anyList());
    verify(walletPermissionRepository, times(1)).findAll();
  }

  @Test
  public void populateAccessResourcesForSkrillTeller_withValidData_shouldSucceed() {
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any()))
        .thenReturn(UserTestUtility.getAccessGroupResponseResourceList());
    List<UserResponseResource> userResources = UserTestUtility.getUserResponseResourceList();
    userResources.get(0).setAccessResources(UserTestUtility.getAccessResourcesList());
    userProvisioningUtils.populateAccessResourcesForSkrillTeller(userResources);
    verify(accessGroupFeignClient, times(2)).getAccessGroupsFromInputList(any());
  }

  @Test
  public void testGenerateRandomRoleString() {
    userProvisioningUtils.generateRandomRoleString();
    verify(commonUserProvisioningUtil, times(1)).generateRandomRoleString();
  }

  @Test
  public void testPopulateAccessResourcesAndLegalEntity() {
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any()))
        .thenReturn(UserTestUtility.getAccessGroupResponseResourceList());
    userProvisioningUtils.populateAccessResourcesAndLegalEntity(UserTestUtility.getUserResponseResourceList());
    verify(accessGroupFeignClient, times(1)).getAccessGroupsFromInputList(any());
  }

  @Test
  public void testDeleteAccessResourcesInfo() {
    doNothing().when(userAccessGroupMapppingRepository).delete(any());
    when(userAccessGroupMapppingRepository.findById(any()))
        .thenReturn(Optional.of(UserTestUtility.getUserAccessGroupMappingDao()));
    when(accessGroupFeignClient.fetchAccessGroupByCode(any()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).body(UserTestUtility.getAccessGroupResponseResource()));
    userProvisioningUtils.deleteAccessResourcesInfo(UserTestUtility.ACCESS_GROUPS,
        UserTestUtility.getIdentityManagementUserResource(), UserTestUtility.getUser());
    verify(accessGroupFeignClient, times(2)).fetchAccessGroupByCode(any());
  }

  @Test(expected = BadRequestException.class)
  public void deleteAccessResourcesInfo_withInvalidAccessGroupId_throwsException() {
    when(accessGroupFeignClient.fetchAccessGroupByCode("invalid_ag1"))
        .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    userProvisioningUtils.deleteAccessResourcesInfo(new ArrayList<>(Arrays.asList("invalid_ag1")),
        UserTestUtility.getIdentityManagementUserResource(), UserTestUtility.getUser());
  }

  @Test
  public void testupdateUserStatusForSkrillAndNeteller_withAccessResourceStatusBlocked() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    List<UserAccessGroupMappingDao> dao = new ArrayList<>();
    dao.add(UserTestUtility.getUserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.findByLoginNameAndResourceType(any(), any(), any()))
            .thenReturn(dao);
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.BLOCKED);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setApplicationName("SKRILL");
    userProvisioningUtils.updateUserStatusForSkrillAndNeteller("user1", resource, "SKRILL");
    verify(userAccessGroupMapppingRepository, Mockito.times(1)).findByLoginNameAndResourceType(any(),
            any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testupdateUserStatusForSkrillAndNeteller_withAccessResourceStatusActive() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    List<UserAccessGroupMappingDao> dao = new ArrayList<>();
    dao.add(UserTestUtility.getUserAccessGroupMappingDao());
    dao.get(0).setUserAccessGroupStatus(AccessResourceStatus.ACTIVE);
    when(userAccessGroupMapppingRepository.findByLoginNameAndResourceType(any(), any(), any()))
            .thenReturn(dao);
    when(usersRepository.findByUserId(any())).thenReturn(UserTestUtility.getUser());
    when(userService.fetchUser(any())).thenReturn(UserTestUtility.getIdentityManagementUserResource());
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.ACTIVATE);
    resource.setEmail("sample@email.com");
    resource.setUserName("sample@email.com");
    resource.setAccessGroupId("sampleId");
    resource.setApplicationName("SKRILL");
    userProvisioningUtils.updateUserStatusForSkrillAndNeteller("user1", resource, "SKRILL");
    verify(userAccessGroupMapppingRepository, Mockito.times(1)).findByLoginNameAndResourceType(any(),
            any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void checkAndAddDefaultPermission_withEmptyPermissionList_shouldAddDefaultPerm() {
    UserMigrationResource userMigrationResource = UserTestUtility.getUserMigrationRequest();
    userMigrationResource.getAccessResources().get(0).setPermissions(new ArrayList<>());
    userProvisioningUtils.checkAndAddDefaultPermission(userMigrationResource);
    assertNotNull(userMigrationResource);
    assertThat(userMigrationResource.getAccessResources().get(0).getPermissions().get(0),
        Is.is(SkrillPermissions.DEFAULT_PERMISSION));
  }

  @Test
  public void validateCreateUserWalletResources_withSkrillApplicationAndSameBrand_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    mockSkrillTellerConfig();
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    userProvisioningUtils.validateCreateUserWalletResources(UserTestUtility.getAccessResourcesList(),
        UserTestUtility.getUserDto());
    verify(merchantAccountInfoService, times(1)).getWalletProfileAndMerchantSettings(Mockito.anySet());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void validateCreateUserWalletResources_withSkrillApplicationAndBinanceBusinessUnit_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    mockSkrillTellerConfig();
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    UserDto userDto = UserTestUtility.getUserDto();
    userDto.setBusinessUnit(DataConstants.BINANCE);
    List<AccessResources> accessResources = UserTestUtility.getAccessResourcesList();
    userProvisioningUtils.validateCreateUserWalletResources(accessResources, userDto);
    verify(merchantAccountInfoService, times(1)).getWalletProfileAndMerchantSettings(Mockito.anySet());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void validateCreateUserWalletResources_withNetellerApplicationAndDiffBrand_throwsException() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    userProvisioningUtils.validateCreateUserWalletResources(UserTestUtility.getAccessResourcesList(),
        UserTestUtility.getUserDto());
    verify(merchantAccountInfoService, times(1)).getWalletProfileAndMerchantSettings(Mockito.anySet());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void validateUpdateUserWalletResources_withSkrillApplicationAndSameBrand_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    mockSkrillTellerConfig();
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    userProvisioningUtils.validateUpdateUserWalletResources(UserTestUtility.getUpdateUserAccessResourcesList(),
        UserTestUtility.getUserUpdationDto());
    verify(merchantAccountInfoService, times(1)).getWalletProfileAndMerchantSettings(Mockito.anySet());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void validateUpdateUserWalletResources_withSkrillApplicationAndBinanceBusinessUnit_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    mockSkrillTellerConfig();
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setBusinessUnit(DataConstants.BINANCE);
    userProvisioningUtils.validateUpdateUserWalletResources(UserTestUtility.getUpdateUserAccessResourcesList(),
        userUpdationDto);
    verify(merchantAccountInfoService, times(1)).getWalletProfileAndMerchantSettings(Mockito.anySet());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void validateUpdateUserWalletResources_withNetellerApplicationAndSameBrand_throwsException() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(Mockito.anySet()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    userProvisioningUtils.validateUpdateUserWalletResources(UserTestUtility.getUpdateUserAccessResourcesList(),
        UserTestUtility.getUserUpdationDto());
    verify(merchantAccountInfoService, times(1)).getWalletProfileAndMerchantSettings(Mockito.anySet());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void populateUserResponseList_withValidInput_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    User user = UserTestUtility.getUser();
    user.setApplication("PORTAL");
    when(usersRepository.findTopByLoginName(anyString())).thenReturn(user);
    List<UserAccessGroupMappingDao> userAccessGroups = new ArrayList<>();
    userAccessGroups.add(UserTestUtility.getUserAccessGroupMappingDao());
    when(usersRepository.findByLoginNameAndApplication(any(), any()))
        .thenReturn(Optional.of(UserTestUtility.getUser()));
    IdentityManagementUserResource userResource = UserTestUtility.getIdentityManagementUserResource();
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(userAssembler.toUserResponseResource(any(), any())).thenReturn(UserTestUtility.getUserResponseResource());
    when(userAssembler.toAccessResources(any(UserAccessGroupMappingDao.class)))
        .thenReturn(UserTestUtility.getAccessResourcesList().get(0));
    Page<UserAccessGroupMappingDao> page = new PageImpl<>(userAccessGroups);
    List<UserResponseResource> userResponseResourceList = new ArrayList<>();
    userProvisioningUtils.populateUserResponseList(page, userResponseResourceList, new MutableBoolean(false));
    verify(identityManagementFeignClient, times(1)).getUser(any(), any());
  }

  @Test
  public void populateUserResponseList_withDifferentApplicationUser_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    User user = UserTestUtility.getUser();
    user.setApplication("PORTAL");
    List<UserAccessGroupMappingDao> userAccessGroups = new ArrayList<>();
    userAccessGroups.add(UserTestUtility.getUserAccessGroupMappingDao());
    Page<UserAccessGroupMappingDao> page = new PageImpl<>(userAccessGroups);
    List<UserResponseResource> userResponseResourceList = new ArrayList<>();
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenThrow(UserProvisioningException.builder().create(HttpStatus.NOT_FOUND));
    when(userAssembler.toUserResponseResource(any(), any())).thenReturn(UserTestUtility.getUserResponseResource());
    userProvisioningUtils.populateUserResponseList(page, userResponseResourceList, new MutableBoolean(false));
    verify(identityManagementFeignClient, times(1)).getUser(any(), any());
  }

  @Test(expected = UserProvisioningException.class)
  public void populateUserResponseList_withInvalidInput_throwsException() {
    List<UserAccessGroupMappingDao> userAccessGroups = new ArrayList<>();
    userAccessGroups.add(UserTestUtility.getUserAccessGroupMappingDao());
    Page<UserAccessGroupMappingDao> page = new PageImpl<>(userAccessGroups);
    List<UserResponseResource> userResponseResourceList = new ArrayList<>();
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenThrow(UserProvisioningException.builder().create(HttpStatus.BAD_REQUEST));
    when(userAssembler.toUserResponseResource(any(), any())).thenReturn(UserTestUtility.getUserResponseResource());
    userProvisioningUtils.populateUserResponseList(page, userResponseResourceList, new MutableBoolean(false));
    verify(identityManagementFeignClient, times(1)).getUser(any(), any());
  }

  @Test
  public void checkAndRetrieveApplicationFromTHreadLocal_withApplicationInThreadLocal_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    String application = userProvisioningUtils.checkAndRetrieveApplicationFromTHreadLocal();
    assertNotNull(application);
    assertThat(application, Is.is(DataConstants.SKRILL));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void checkAndRetrieveApplicationFromTHreadLocal_withoutThreadLocal_throwsException() {
    userProvisioningUtils.checkAndRetrieveApplicationFromTHreadLocal();
  }

  @Test
  public void constructIdentityManagementCreateUserResource_portalAsApplication_shouldSucceed() {
    UserDto userDto = UserTestUtility.getUserDto();
    userDto.setApplicationName(DataConstants.PORTAL);
    IdentityManagementUserResource idmUserResource =
        userProvisioningUtils.constructIdentityManagementCreateUserResource(userDto,
            new ArrayList<>(Arrays.asList(DataConstants.BP_ADMIN)), new ArrayList<>(Arrays.asList("AG01", "AG02")));
    assertNotNull(idmUserResource);
  }

  @Test
  public void constructIdentityManagementCreateUserResource_partnerPortalAsApplication_shouldSucceed() {
    UserDto userDto = UserTestUtility.getUserDto();
    userDto.setApplicationName(DataConstants.PARTNER_PORTAL);
    IdentityManagementUserResource idmUserResource =
        userProvisioningUtils.constructIdentityManagementCreateUserResource(userDto,
            new ArrayList<>(Arrays.asList(DataConstants.BP_ADMIN)), new ArrayList<>(Arrays.asList("AG01", "AG02")));
    assertNotNull(idmUserResource);
  }

  @Test
  public void constructIdentityManagementCreateUserResource_netellerAsApplication_shouldSucceed() {
    UserDto userDto = UserTestUtility.getUserDto();
    userDto.setApplicationName(DataConstants.NETELLER);
    userDto.setMigrationUseCase(true);
    IdentityManagementUserResource idmUserResource =
        userProvisioningUtils.constructIdentityManagementCreateUserResource(userDto,
            new ArrayList<>(Arrays.asList(DataConstants.BP_ADMIN)), new ArrayList<>(Arrays.asList("AG01", "AG02")));
    assertNotNull(idmUserResource);
  }

  @Test
  public void getUserByLoginName_withInvalidLoginName_shouldReturnNull() {
    IdentityManagementUserResource userResource = UserTestUtility.getIdentityManagementUserResource();
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.NOT_FOUND));
    UserResponseResource userResponseResource =
        userProvisioningUtils.getUserByLoginName("test_user", DataConstants.PARTNER_PORTAL);
    assertNull(userResponseResource);
  }

  @Test
  public void getresponseList() {
    List<IdentityManagementUserResource> userResource = UserTestUtility.getIdentityManagementUserResourceList();
    when(identityManagementFeignClient.getUsersList(anyList(), any()))
            .thenReturn(userResource);
    List<UserResponseResource> userResponseResource =
            userProvisioningUtils.getIdmUsers(Arrays.asList("test"), DataConstants.PORTAL);
    assertNotNull(userResponseResource);
  }

  @Test
  public void handleUpdateUserGroupIds_fromPortalToPartner_shoulSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    User user = UserTestUtility.getUser();
    user.setUserExternalId("789");
    user.setApplication(DataConstants.PORTAL);
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setOwnerType(DataConstants.PARTNER);
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.PARTNER_PORTAL, "partner_group");
    groupIdsMap.put(DataConstants.PORTAL, "portal_group");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    userProvisioningUtils.handleUpdateUserGroupIds(user, userUpdationDto);
    verify(identityManagementFeignClient, times(1)).addUserToGroup("partner_group", "789");
    verify(identityManagementFeignClient, times(1)).removeUserFromGroup("portal_group", "789");
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void handleUpdateUserGroupIds_fromPortalToAccountGroupPartner_shoulSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    User user = UserTestUtility.getUser();
    user.setUserExternalId("789");
    user.setApplication(DataConstants.PORTAL);
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setOwnerType(DataConstants.ACCOUNT_GROUP);
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.PARTNER_PORTAL, "partner_group");
    groupIdsMap.put(DataConstants.PORTAL, "portal_group");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    userProvisioningUtils.handleUpdateUserGroupIds(user, userUpdationDto);
    verify(identityManagementFeignClient, times(1)).addUserToGroup("partner_group", "789");
    verify(identityManagementFeignClient, times(1)).removeUserFromGroup("portal_group", "789");
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void handleUpdateUserGroupIds_fromPartnerToMleTypeMerchant_shoulSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    User user = UserTestUtility.getUser();
    user.setUserExternalId("789");
    user.setApplication(DataConstants.PARTNER_PORTAL);
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setOwnerType(DataConstants.MLE);
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.PARTNER_PORTAL, "partner_group");
    groupIdsMap.put(DataConstants.PORTAL, "portal_group");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    userProvisioningUtils.handleUpdateUserGroupIds(user, userUpdationDto);
    verify(identityManagementFeignClient, times(1)).addUserToGroup("portal_group", "789");
    verify(identityManagementFeignClient, times(1)).removeUserFromGroup("partner_group", "789");
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void handleUpdateUserGroupIds_fromPartnerToPmleTypeMerchant_shoulSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    User user = UserTestUtility.getUser();
    user.setUserExternalId("789");
    user.setApplication(DataConstants.PARTNER_PORTAL);
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setOwnerType(DataConstants.PMLE);
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.PARTNER_PORTAL, "partner_group");
    groupIdsMap.put(DataConstants.PORTAL, "portal_group");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    userProvisioningUtils.handleUpdateUserGroupIds(user, userUpdationDto);
    verify(identityManagementFeignClient, times(1)).addUserToGroup("portal_group", "789");
    verify(identityManagementFeignClient, times(1)).removeUserFromGroup("partner_group", "789");
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testPopulateApplicationNameForPortal() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    User user = UserTestUtility.getUser();
    user.setApplication("PORTAL");
    when(usersRepository.findTopByLoginName(anyString())).thenReturn(user);
    UserResponseResource userResponseResource = UserTestUtility.getUserResponseResource();
    userProvisioningUtils.populateApplicationName("agmer", userResponseResource);
    assertThat(userResponseResource.getApplication(), Is.is("PORTAL"));
  }

  @Test
  public void testPopulateApplicationNameForSkrill() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UserResponseResource userResponseResource = UserTestUtility.getUserResponseResource();
    userProvisioningUtils.populateApplicationName("agmer", userResponseResource);
    assertThat(userResponseResource.getApplication(), Is.is("SKRILL"));
    verify(usersRepository, times(0)).findTopByLoginName(anyString());
  }

  @Test
  public void testPopulateApplicationNameForNeteller() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    UserResponseResource userResponseResource = UserTestUtility.getUserResponseResource();
    userProvisioningUtils.populateApplicationName("agmer", userResponseResource);
    assertThat(userResponseResource.getApplication(), Is.is("NETELLER"));
    verify(usersRepository, times(0)).findTopByLoginName(anyString());
  }

  @Test
  public void testPopulateApplicationNameWhenUserNotFound() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(usersRepository.findTopByLoginName(anyString())).thenReturn(null);
    UserResponseResource userResponseResource = UserTestUtility.getUserResponseResource();
    userProvisioningUtils.populateApplicationName("agmer", userResponseResource);
    assertNull(userResponseResource.getApplication());
  }

  @Test
  public void populateWalletNames_withValidInput_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    mockSkrillTellerConfig();
    BasicWalletInfo basicWalletInfo2 = UserTestUtility.getBasicWalletInfo();
    basicWalletInfo2.getBusinessProfile().setCompanyName("test_company2");
    basicWalletInfo2.setId("6789");
    when(merchantAccountInfoService.getBasicWalletInfo(Mockito.anySet()))
        .thenReturn(new ArrayList<>(Arrays.asList(UserTestUtility.getBasicWalletInfo())))
        .thenReturn(new ArrayList<>(Arrays.asList(basicWalletInfo2)));
    List<UserResponseResource> userResponseList = UserTestUtility.getUserResponseList();
    userProvisioningUtils.populateWalletNames(userResponseList);
    verify(merchantAccountInfoService, times(2)).getBasicWalletInfo(Mockito.anySet());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void checkAndDeleteUserReports_shouldSucceed() {
    ReportScheduleResponse res1 = new ReportScheduleResponse();
    res1.setContent(new ArrayList<>());
    when(skrillTellerAccountInfoService.getSchedules(anyString(), any())).thenReturn(res1);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    UpdateUserStatusResource resource = new UpdateUserStatusResource();
    resource.setAction(UserAction.BLOCK_ALL);
    resource.setApplicationName("SKRILL");
    AccessResources access = new AccessResources();
    access.setId("1234");
    List<AccessResources> accessRes = new ArrayList<>();
    accessRes.add(access);
    UserResponseResource user = new UserResponseResource();
    user.setAccessResources(accessRes);
    List<UserResponseResource> userList = new ArrayList<>();
    userList.add(user);
    UsersListResponseResource userListResponseResource = new UsersListResponseResource();
    userListResponseResource.setUsers(userList);
    userProvisioningUtils.checkAndDeleteUserReports("user1", resource, userListResponseResource);
  }

  private void mockSkrillTellerConfig() {
    Map<String, List<String>> brandsMap = new HashMap<>();
    brandsMap.put("skrill", Arrays.asList("binance", "ftx"));
    when(skrillTellerConfig.getLinkedBrands()).thenReturn(brandsMap);
    Map<String, BusinessUnitConfig> businessUnits = new HashMap<>();
    BusinessUnitConfig businessUnitConfig = new BusinessUnitConfig();
    businessUnitConfig.setAdminRole("ADMIN");
    businessUnits.put("skrill", businessUnitConfig);
    businessUnits.put("binance", businessUnitConfig);
    when(skrillTellerConfig.getBusinessUnits()).thenReturn(businessUnits);
  }

}
