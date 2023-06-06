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
public class CommonUserProvisioningUtilsTest {

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
  private CommonUserProvisioningUtil userProvisioningUtils;

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
    verify(userHandlerService, times(1)).getUserCount(anyString(), anyString());
    verify(userConfig, times(1)).getAppUserLimit();
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
    verify(userHandlerService, times(1)).getUserCount(anyString(), anyString());
    verify(userConfig, times(1)).getAppUserLimit();
  }

  @Test
  public void test_verifyUserCountforResource_whenAppUserConfigNotPresent() {
    when(userHandlerService.getUserCount(anyString(), anyString())).thenReturn(walletUserCountResource);
    AppUserConfigDto appUserConfigDto = null;
    Map<String, AppUserConfigDto> appUserLimit = new HashMap<>();
    appUserLimit.put(DataConstants.SKRILL, appUserConfigDto);
    when(userConfig.getAppUserLimit()).thenReturn(appUserLimit);
    ResourceUsersValidationDto resourceUsersValidationDto =
        userProvisioningUtils.verifyUserCountforResource("resourceId", "resourceName", DataConstants.SKRILL);
    assertNotNull(resourceUsersValidationDto);
    assertThat(resourceUsersValidationDto.getCanAddAdminUsers(), Is.is(true));
    assertThat(resourceUsersValidationDto.getCanAddUsers(), Is.is(true));
    verify(userHandlerService, times(1)).getUserCount(anyString(), anyString());
    verify(userConfig, times(1)).getAppUserLimit();
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
    verify(accessGroupFeignClient, times(1)).getAccessGroupsFromInputList(any());
    assertThat(userUpdationDto.getAccessResources().size(), Is.is(3));
  }


  @Test
  public void testGenerateRandomRoleString() {
    String role = userProvisioningUtils.generateRandomRoleString();
    assertNotNull(role);
  }
}
