// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.upf.user.provisioning.config.OktaAppConfig;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.repository.UserAssignedApplicationsRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.impl.UserServiceImpl;
import com.paysafe.upf.user.provisioning.utils.UserCreationUtil;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UserCreationUtilTest {

  @InjectMocks
  private UserCreationUtil userCreationUtil;

  @Mock
  private OktaAppConfig oktaAppConfig;

  @Mock
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Mock
  private UserProvisioningUtils userProvisioningUtils;

  @Mock
  private UserAssignedApplicationsRepository userAssignedApplicationsRepository;

  @Mock
  private UserServiceImpl userServiceImpl;

  @Test
  public void setGroupIds_withValidData_shouldSucceed() {
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.SKRILL, "12343");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    Set<String> groupIds = userCreationUtil.getGroupIds(new HashSet<>(), DataConstants.SKRILL, null, null);
    verify(oktaAppConfig, times(1)).getGroupIds();
    Set<String> groups = new HashSet<>();
    groups.add("12343");
    assertThat(groupIds, Is.is(groups));
  }

  @Test
  public void setGroupIds_withUserAssignedApplications_shouldSucceed() {
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.PORTAL, "12343");
    groupIdsMap.put(DataConstants.NBX_PORTAL, "56789");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    Set<String> groupIds = userCreationUtil.getGroupIds(new HashSet<>(), DataConstants.PORTAL, null,
        new ArrayList<>(Arrays.asList("PORTAL", "NBX_PORTAL")));
    assertThat(groupIds.size(), Is.is(2));
  }

  @Test
  public void getGroupIds_withNbxPortalDivision_shouldSucceed() {
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.PORTAL, "12343");
    groupIdsMap.put(DataConstants.NBX_PORTAL, "65787");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    Set<String> groupIds =
        userCreationUtil.getGroupIds(new HashSet<>(), DataConstants.PORTAL, DataConstants.NBX_PORTAL, null);
    verify(oktaAppConfig, times(3)).getGroupIds();
    Set<String> groups = new HashSet<>();
    groups.add("65787");
    assertThat(groupIds, Is.is(groups));
  }

  @Test
  public void setGroupIds_withExistedGroupId_shouldSucceed() {
    Set<String> groups = Collections.singleton("1234");
    Set<String> groupIds = userCreationUtil.getGroupIds(groups, DataConstants.SKRILL, null, null);
    assertThat(groupIds, Is.is(groups));
  }

  @Test
  public void handleUpdateUserGroupIds_withValidInput_shouldSucess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.PARTNER_PORTAL, "12343");
    groupIdsMap.put(DataConstants.PORTAL, "45678");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    when(identityManagementFeignClient.removeUserFromGroup(any(), any()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
    when(identityManagementFeignClient.addUserToGroup(any(), any()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
    User user = UserTestUtility.getUser();
    user.setUserAssignedApplications(UserTestUtility.getUserAssignedApplications());
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setUserAssignedApplications(new ArrayList<>(Arrays.asList(DataConstants.PARTNER_PORTAL)));
    userCreationUtil.handleUpdateUserGroupIds(user, userUpdationDto);
    verify(identityManagementFeignClient, times(1)).addUserToGroup(any(), any());
    verify(identityManagementFeignClient, times(1)).removeUserFromGroup(any(), any());
  }

  @Test
  public void handleUpdateUserGroupIds_withCommonThreadLocalApp_shouldSucess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PARTNER_PORTAL));
    Map<String, String> groupIdsMap = new HashMap<>();
    groupIdsMap.put(DataConstants.PARTNER_PORTAL, "12343");
    groupIdsMap.put(DataConstants.PORTAL, "45678");
    when(oktaAppConfig.getGroupIds()).thenReturn(groupIdsMap);
    when(identityManagementFeignClient.removeUserFromGroup(any(), any()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
    when(identityManagementFeignClient.addUserToGroup(any(), any()))
        .thenReturn(ResponseEntity.status(HttpStatus.OK).build());
    User user = UserTestUtility.getUser();
    user.setUserAssignedApplications(UserTestUtility.getUserAssignedApplications());
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userCreationUtil.handleUpdateUserGroupIds(user, userUpdationDto);
    verify(identityManagementFeignClient, times(1)).addUserToGroup(any(), any());
    verify(identityManagementFeignClient, times(1)).removeUserFromGroup(any(), any());
  }

  @Test
  public void validateCreateUserRequestWithPartnerPortal() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PARTNER_PORTAL));
    doNothing().when(userServiceImpl).validateRoles(any());
    userCreationUtil.validateCreateUserRequest(UserTestUtility.getUserDto());
  }

  @Test
  public void validateCreateUserRequestWithNeteller() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    doNothing().when(userProvisioningUtils).validateCreateUserWalletResources(any(),any());
    userCreationUtil.validateCreateUserRequest(UserTestUtility.getUserDto());
  }

  @Test
  public void validateCreateUserRequestWithSkrill() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    doNothing().when(userProvisioningUtils).validateCreateUserWalletResources(any(),any());
    userCreationUtil.validateCreateUserRequest(UserTestUtility.getUserDto());
  }

  @Test
  public void validateCreateUserRequest() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PARTNER_PORTAL));
    doNothing().when(userServiceImpl).validateAccessGroups(any());
    UserDto userDto = UserTestUtility.getUserDto();
    userDto.setRoleDto(null);
    userDto.getAccessGroupDto().setCustomAccessGroupDtos(Arrays.asList(UserTestUtility.getCustomAccessGroupDto()));
    userDto.getAccessGroupDto().setExistingAccessGroupIds(Arrays.asList("123"));
    userCreationUtil.validateCreateUserRequest(userDto);
  }

  @Test
  public void validateUpdateUserRequestWithSkrill() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    doNothing().when(userProvisioningUtils).validateUpdateUserWalletResources(any(),any());
    userCreationUtil.validateUpdateUserRequest(UserTestUtility.getUserUpdationDto());
  }

  @Test
  public void validateUpdateUserRequestWithNeteller() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    doNothing().when(userProvisioningUtils).validateUpdateUserWalletResources(any(),any());
    userCreationUtil.validateUpdateUserRequest(UserTestUtility.getUserUpdationDto());
  }

  @Test
  public void validateUpdateUserRequestWithPartnerPortalAndCustomRolesIsNull() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PARTNER_PORTAL));
    doNothing().when(userServiceImpl).validateRoles(any());
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.getRolesToAdd().setCustomRoles(null);
    userCreationUtil.validateUpdateUserRequest(userUpdationDto);
  }

  @Test
  public void validateUpdateUserRequestWithPartnerPortalAndCustomAccessGroupIsNull() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PARTNER_PORTAL));
    doNothing().when(userServiceImpl).validateAccessGroups(any());
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setRolesToAdd(null);
    userUpdationDto.getAccessGroupsToAdd().setCustomAccessGroupDtos(null);
    userCreationUtil.validateUpdateUserRequest(userUpdationDto);
  }

}
