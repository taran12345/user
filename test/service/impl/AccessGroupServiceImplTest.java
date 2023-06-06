// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.config.UserMigrationConfig;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingKey;
import com.paysafe.upf.user.provisioning.domain.WalletPermission;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.Action;
import com.paysafe.upf.user.provisioning.feignclients.AccessGroupFeignClient;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.WalletPermissionRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomAccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.PermissionDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.ResourceUsersValidationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicyCreateRequest;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserAccessResources;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AccessGroupServiceImplTest {

  @Mock
  private AccessGroupFeignClient accessGroupFeignClient;

  @Mock
  private UserProvisioningUtils userProvisioningUtils;

  @Mock
  private UserService userService;

  @Mock
  private WalletPermissionRepository walletPermissionRepository;

  @Mock
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Mock
  private UserMigrationConfig userMigrationConfig;

  @InjectMocks
  private AccessGroupServiceImpl accessGroupServiceImpl;

  private List<AccessResources> accessResources;
  private UserDto userDto;
  private ResourceUsersValidationDto resourceUsersValidationDto;
  private UserUpdationDto userUpdationDto;
  private IdentityManagementUserResource identityManagementUserResource;

  /**
   * Setup test configuration.
   */
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    userDto = UserTestUtility.getUserDto();
    accessResources = UserTestUtility.getAccessResourcesList();
    accessResources.get(0).setAccessGroupId(null);
    userDto.setAccessResources(accessResources);
    resourceUsersValidationDto = UserTestUtility.getResourceUsersValidationDto();
    userUpdationDto = UserTestUtility.getUserUpdationDto();
    identityManagementUserResource = UserTestUtility.getIdentityManagementUserResource();
  }

  @Test
  public void testCreateAccessGroupsFromResouresList() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    List<AccessResources> response = accessGroupServiceImpl.createAccessGroupsFromResouresList(userDto);
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessGroup(any(CustomAccessGroupDto.class));
    assertNotNull(response);
    assertThat(response.get(0).getAccessGroupId(), Is.is("TEST_ACCESS_GROUP_ID"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void createAccessGroupsFromResouresList_withResourceIds_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    userDto.getAccessResources().get(0).setId(null);
    List<String> resourceIds = new ArrayList<>();
    resourceIds.add("123");
    resourceIds.add("456");
    userDto.getAccessResources().get(0).setIds(resourceIds);
    userDto.getAccessResources().get(0).setRole(null);
    userDto.getAccessResources().get(0).setId("123");
    List<AccessResources> response = accessGroupServiceImpl.createAccessGroupsFromResouresList(userDto);
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessGroup(any(CustomAccessGroupDto.class));
    assertNotNull(response);
    assertThat(response.get(0).getAccessGroupId(), Is.is("TEST_ACCESS_GROUP_ID"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void createAccessGroupsFromResouresList_withAccessResourceIdContainingSpclChars_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    userDto.getAccessResources().get(0).setId(null);
    List<String> resourceIds = new ArrayList<>();
    resourceIds.add("123");
    resourceIds.add("456");
    userDto.getAccessResources().get(0).setIds(resourceIds);
    userDto.getAccessResources().get(0).setRole(null);
    userDto.getAccessResources().get(0).setId("123!#");
    List<AccessResources> response = accessGroupServiceImpl.createAccessGroupsFromResouresList(userDto);
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessGroup(any(CustomAccessGroupDto.class));
    assertNotNull(response);
    assertThat(response.get(0).getAccessGroupId(), Is.is("TEST_ACCESS_GROUP_ID"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void createAccessGroupsFromResouresList_WhenAccessResourceIdIsNull_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    userDto.getAccessResources().get(0).setId(null);
    List<String> resourceIds = new ArrayList<>();
    resourceIds.add("123");
    resourceIds.add("456");
    userDto.getAccessResources().get(0).setIds(resourceIds);
    userDto.getAccessResources().get(0).setRole(null);
    userDto.getAccessResources().get(0).setId(null);
    List<AccessResources> response = accessGroupServiceImpl.createAccessGroupsFromResouresList(userDto);
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessGroup(any(CustomAccessGroupDto.class));
    assertNotNull(response);
    assertThat(response.get(0).getAccessGroupId(), Is.is("TEST_ACCESS_GROUP_ID"));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testCreateAccessGroupsFromResouresList_withEmptyAccessResources() {
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    userDto.setAccessResources(new ArrayList<>());
    accessGroupServiceImpl.createAccessGroupsFromResouresList(userDto);
    verify(accessGroupFeignClient, Mockito.times(0)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(0)).createAccessGroup(any(CustomAccessGroupDto.class));
  }

  @Test(expected = BadRequestException.class)
  public void testCreateAccessGroupsFromResouresList_whenAppUsersCountExceeds() {
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    resourceUsersValidationDto.setCanAddUsers(false);
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    accessGroupServiceImpl.createAccessGroupsFromResouresList(userDto);
  }

  @Test
  public void testCreateAccessGroupsFromResouresList_withAdminARole() {
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(accessGroupFeignClient.fetchAccessGroups(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupsEmptyDataResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    userDto.getAccessResources().get(0).setRole(DataConstants.ADMIN);
    List<AccessResources> response = accessGroupServiceImpl.createAccessGroupsFromResouresList(userDto);
    assertNotNull(response);
    assertThat(response.get(0).getAccessGroupId(), Is.is("TEST_ACCESS_GROUP_ID"));
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessGroup(any(CustomAccessGroupDto.class));
  }

  @Test
  public void testCreateAccessGroupsFromResoureIdsList_withAdminARole() {
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(accessGroupFeignClient.fetchAccessGroups(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupsEmptyDataResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    userDto.getAccessResources().get(0).setRole(DataConstants.ADMIN);
    userDto.getAccessResources().get(0).setId(null);
    List<String> resourceIds = new ArrayList<>();
    resourceIds.add("123");
    resourceIds.add("456");
    userDto.getAccessResources().get(0).setIds(resourceIds);
    List<AccessResources> response = accessGroupServiceImpl.createAccessGroupsFromResouresList(userDto);
    assertNotNull(response);
    assertThat(response.get(0).getAccessGroupId(), Is.is("TEST_ACCESS_GROUP_ID"));
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessGroup(any(CustomAccessGroupDto.class));
  }

  @Test
  public void testCreateAccessGroupsForUpdateUser_withValidInput() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any()))
        .thenReturn(UserTestUtility.getAccessGroupsFromInputListSuccessResponse());
    when(accessGroupFeignClient.fetchAccessGroupByCode(any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupByCodeSuccessResponse());
    when(accessGroupFeignClient.updateAccessPolicy(any(), any()))
        .thenReturn(UserTestUtility.getUpdateAccessPolicySuccessResponse());
    when(this.userService.fetchUser(any())).thenReturn(identityManagementUserResource);
    when(accessGroupFeignClient.fetchAccessGroups(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupsEmptyDataResponse());
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    accessGroupServiceImpl.createAccessGroupsForUpdateUser("1234", userUpdationDto);
    verify(accessGroupFeignClient, Mockito.times(2)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(2)).createAccessGroup(any(CustomAccessGroupDto.class));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void testCreateAccessGroupsForUpdateUser_accessResourceWithCustomRoleAndEmptyPermissions() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any()))
        .thenReturn(UserTestUtility.getAccessGroupsFromInputListSuccessResponse());
    when(this.userService.fetchUser(any())).thenReturn(identityManagementUserResource);
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    userUpdationDto.setAccessResources(UserTestUtility.getUpdateUserAccessResourcesRegularRole());
    userUpdationDto.getAccessResources().get(0).setPermissions(new ArrayList<>());
    userUpdationDto.getAccessResources().get(0).setOwnerId("TEST_WALLET_ID2");
    userUpdationDto.getAccessResources().get(0).setType(DataConstants.WALLETS);
    accessGroupServiceImpl.createAccessGroupsForUpdateUser("1234", userUpdationDto);
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testCreateAccessGroupsForUpdateUser_accessResourceWithCustomRole() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any()))
        .thenReturn(UserTestUtility.getAccessGroupsFromInputListSuccessResponse());
    when(accessGroupFeignClient.fetchAccessGroupByCode(any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupByCodeSuccessResponse());
    when(accessGroupFeignClient.updateAccessPolicy(any(), any()))
        .thenReturn(UserTestUtility.getUpdateAccessPolicySuccessResponse());
    when(this.userService.fetchUser(any())).thenReturn(identityManagementUserResource);
    when(accessGroupFeignClient.fetchAccessGroups(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupsEmptyDataResponse());
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    userUpdationDto.setAccessResources(UserTestUtility.getUpdateUserAccessResourcesRegularRole());
    userUpdationDto.setApplicationName(DataConstants.SKRILL);
    accessGroupServiceImpl.createAccessGroupsForUpdateUser("1234", userUpdationDto);
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void createAccessGroupsForUpdateUser_withExistedAdminAccessGroup_shouldSucced() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    List<AccessGroupResponseResource> accessGroupResponseResources =
        UserTestUtility.getAccessGroupsFromInputListSuccessResponse();
    accessGroupResponseResources.get(0).setType(AccessGroupType.DEFAULT_ADMIN);
    accessGroupResponseResources.get(0).getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights().get(0)
        .getAccessRight().setAccessRole(DataConstants.ADMIN);
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any())).thenReturn(accessGroupResponseResources);
    when(walletPermissionRepository.findById(any()))
        .thenReturn(Optional.of(new WalletPermission(1, "101", "WP", 1, 1, "EN")));
    when(accessGroupFeignClient.fetchAccessGroupByCode(any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupByCodeSuccessResponse());
    when(accessGroupFeignClient.updateAccessPolicy(any(), any()))
        .thenReturn(UserTestUtility.getUpdateAccessPolicySuccessResponse());
    when(this.userService.fetchUser(any())).thenReturn(identityManagementUserResource);
    when(accessGroupFeignClient.fetchAccessGroups(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupsEmptyDataResponse());
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    accessGroupServiceImpl.createAccessGroupsForUpdateUser("1234", userUpdationDto);
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessGroup(any(CustomAccessGroupDto.class));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void createAccessGroupsForUpdateUser_withPredefinedBusinessRoleAccessGroup_shouldSucced() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    List<AccessGroupResponseResource> accessGroupResponseResources =
        UserTestUtility.getAccessGroupsFromInputListSuccessResponse();
    accessGroupResponseResources.get(0).setType(AccessGroupType.CUSTOMIZED);
    accessGroupResponseResources.get(0).getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights().get(0)
        .getAccessRight().setAccessRole(DataConstants.BP_DEVELOPER);
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any())).thenReturn(accessGroupResponseResources);
    when(accessGroupFeignClient.fetchAccessGroupByCode(any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupByCodeSuccessResponse());
    when(accessGroupFeignClient.updateAccessPolicy(any(), any()))
        .thenReturn(UserTestUtility.getUpdateAccessPolicySuccessResponse());
    when(this.userService.fetchUser(any())).thenReturn(identityManagementUserResource);
    when(accessGroupFeignClient.fetchAccessGroups(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupsEmptyDataResponse());
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    userUpdationDto.getAccessResources().get(0).setRole(DataConstants.BP_BUSINESS);
    accessGroupServiceImpl.createAccessGroupsForUpdateUser("1234", userUpdationDto);
    verify(accessGroupFeignClient, Mockito.times(2)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(2)).createAccessGroup(any(CustomAccessGroupDto.class));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void createAccessGroupsForUpdateUser_withAllPredefinedRoles_shouldSucced() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    List<AccessGroupResponseResource> accessGroupResponseResources =
        UserTestUtility.getAccessGroupsFromInputListSuccessResponse();
    accessGroupResponseResources.get(0).setType(AccessGroupType.CUSTOMIZED);
    accessGroupResponseResources.get(0).getAccessGroupPolicies().get(0).getAcessPolicy().getAccessPolicyRights().get(0)
        .getAccessRight().setAccessRole(DataConstants.BP_DEVELOPER);
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any())).thenReturn(accessGroupResponseResources);
    when(accessGroupFeignClient.fetchAccessGroupByCode(any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupByCodeSuccessResponse());
    when(accessGroupFeignClient.updateAccessPolicy(any(), any()))
        .thenReturn(UserTestUtility.getUpdateAccessPolicySuccessResponse());
    when(this.userService.fetchUser(any())).thenReturn(identityManagementUserResource);
    when(accessGroupFeignClient.fetchAccessGroups(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupsEmptyDataResponse());
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    UpdateUserAccessResources accessResource1 =
        UserTestUtility.getUpdateUserAccessResources(DataConstants.BP_EU_ADMIN, DataConstants.WALLETS, Action.ADD);
    UpdateUserAccessResources accessResource2 =
        UserTestUtility.getUpdateUserAccessResources(DataConstants.BP_ISV_ADMIN, DataConstants.WALLETS, Action.ADD);
    UpdateUserAccessResources accessResource3 =
        UserTestUtility.getUpdateUserAccessResources(DataConstants.BP_OPERATION, DataConstants.WALLETS, Action.ADD);
    UpdateUserAccessResources accessResource4 =
        UserTestUtility.getUpdateUserAccessResources(DataConstants.BP_ADMIN, DataConstants.WALLETS, Action.ADD);
    UpdateUserAccessResources accessResource5 =
        UserTestUtility.getUpdateUserAccessResources(DataConstants.BP_DEVELOPER, DataConstants.WALLETS, Action.ADD);
    userUpdationDto.getAccessResources().get(0).setRole(DataConstants.BP_BUSINESS);
    userUpdationDto.getAccessResources().add(accessResource1);
    userUpdationDto.getAccessResources().add(accessResource2);
    userUpdationDto.getAccessResources().add(accessResource3);
    userUpdationDto.getAccessResources().add(accessResource4);
    userUpdationDto.getAccessResources().add(accessResource5);
    accessGroupServiceImpl.createAccessGroupsForUpdateUser("1234", userUpdationDto);
    verify(accessGroupFeignClient, Mockito.times(7)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(7)).createAccessGroup(any(CustomAccessGroupDto.class));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void createAccessGroupsForUpdateUser_nonEmptyAccessGroupIds_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any()))
        .thenReturn(UserTestUtility.getAccessGroupsFromInputListSuccessResponse());
    when(accessGroupFeignClient.fetchAccessGroupByCode(any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupByCodeSuccessResponse());
    when(accessGroupFeignClient.updateAccessPolicy(any(), any()))
        .thenReturn(UserTestUtility.getUpdateAccessPolicySuccessResponse());
    when(this.userService.fetchUser(any())).thenReturn(identityManagementUserResource);
    when(accessGroupFeignClient.fetchAccessGroups(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupsEmptyDataResponse());
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(walletPermissionRepository.findById(any()))
        .thenReturn(Optional.of(new WalletPermission(1, "101", "WP", 1, 1, "EN")));
    when(userAccessGroupMapppingRepository.save(any(UserAccessGroupMappingDao.class)))
        .thenReturn(new UserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(new UserAccessGroupMappingDao()));
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    userUpdationDto.getAccessResources().get(0).setAccessGroupId("AG_ID1");
    userUpdationDto.getAccessResources().get(0).setOwnerId("TEST_WALLET_ID1");
    userUpdationDto.getAccessResources().get(1).setAccessGroupId("AG_ID2");
    userUpdationDto.getAccessResources().get(1).setOwnerId("TEST_WALLET_ID2");
    accessGroupServiceImpl.createAccessGroupsForUpdateUser("1234", userUpdationDto);
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessGroup(any(CustomAccessGroupDto.class));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void createAccessGroupsForUpdateUser_withCustomRoleOnlyChangeInResourceIds_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any()))
        .thenReturn(UserTestUtility.getAccessGroupsFromInputListSuccessResponse());
    when(accessGroupFeignClient.fetchAccessGroupByCode(any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupByCodeSuccessResponse());
    when(accessGroupFeignClient.updateAccessPolicy(any(), any()))
        .thenReturn(UserTestUtility.getUpdateAccessPolicySuccessResponse());
    when(this.userService.fetchUser(any())).thenReturn(identityManagementUserResource);
    when(accessGroupFeignClient.fetchAccessGroups(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupsEmptyDataResponse());
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(walletPermissionRepository.findById(any()))
        .thenReturn(Optional.of(new WalletPermission(1, "101", "WP", 1, 1, "EN")));
    when(userAccessGroupMapppingRepository.save(any(UserAccessGroupMappingDao.class)))
        .thenReturn(new UserAccessGroupMappingDao());
    when(userAccessGroupMapppingRepository.findById(any(UserAccessGroupMappingKey.class)))
        .thenReturn(Optional.of(new UserAccessGroupMappingDao()));
    when(userMigrationConfig.getNetbanxUnMappedPermissions())
        .thenReturn(new HashSet<>(Arrays.asList("test_access_type")));
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    userUpdationDto.getAccessResources().get(0).setAccessGroupId("AG_ID1");
    userUpdationDto.getAccessResources().get(0).setOwnerId("TEST_WALLET_ID1");
    userUpdationDto.getAccessResources().get(1).setAccessGroupId("AG_ID2");
    userUpdationDto.getAccessResources().get(1).setOwnerId("TEST_WALLET_ID2");
    PermissionDto permissionDto = new PermissionDto(1, "test_access_type", 1);
    userUpdationDto.getAccessResources().get(0).setPermissions(Arrays.asList(permissionDto));
    userUpdationDto.getAccessResources().get(1).setPermissions(Arrays.asList(permissionDto));
    accessGroupServiceImpl.createAccessGroupsForUpdateUser("1234", userUpdationDto);
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(1)).createAccessGroup(any(CustomAccessGroupDto.class));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void createAccessGroupsForUpdateUser_nonEmptyAccessGroupIdsAndWithAccessRole_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(accessGroupFeignClient.getAccessGroupsFromInputList(any()))
        .thenReturn(UserTestUtility.getAccessGroupsFromInputListSuccessResponse());
    when(accessGroupFeignClient.fetchAccessGroupByCode(any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupByCodeSuccessResponse());
    when(accessGroupFeignClient.updateAccessPolicy(any(), any()))
        .thenReturn(UserTestUtility.getUpdateAccessPolicySuccessResponse());
    when(this.userService.fetchUser(any())).thenReturn(identityManagementUserResource);
    when(accessGroupFeignClient.fetchAccessGroups(any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(UserTestUtility.getFetchAccessGroupsEmptyDataResponse());
    when(accessGroupFeignClient.createAccessGroup(any(CustomAccessGroupDto.class)))
        .thenReturn(UserTestUtility.getCreateAccessGroupSucessResponse());
    when(accessGroupFeignClient.createAccessPolicy(any(AccessPolicyCreateRequest.class)))
        .thenReturn(UserTestUtility.getCreateAccessPolicySuccessResponse());
    when(userProvisioningUtils.verifyUserCountforResource(any(), any(), any())).thenReturn(resourceUsersValidationDto);
    userUpdationDto.getAccessResources().get(0).setAccessGroupId("AG_ID1");
    userUpdationDto.getAccessResources().get(0).setOwnerId("TEST_WALLET_ID1");
    userUpdationDto.getAccessResources().get(0).setRole(DataConstants.BP_OPERATION);
    userUpdationDto.getAccessResources().get(1).setAccessGroupId("AG_ID2");
    userUpdationDto.getAccessResources().get(1).setOwnerId("TEST_WALLET_ID2");
    userUpdationDto.getAccessResources().get(1).setRole(DataConstants.BP_ISV_ADMIN);
    accessGroupServiceImpl.createAccessGroupsForUpdateUser("1234", userUpdationDto);
    verify(accessGroupFeignClient, Mockito.times(2)).createAccessPolicy(any(AccessPolicyCreateRequest.class));
    verify(accessGroupFeignClient, Mockito.times(2)).createAccessGroup(any(CustomAccessGroupDto.class));
    CommonThreadLocal.unsetAuthLocal();
  }
}
