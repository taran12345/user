// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAssignedApplications;
import com.paysafe.upf.user.provisioning.enums.BusinessUnit;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.model.OwnerInfo;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.utils.UserManagmentUtil;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccountGroupMerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUpdateUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class UserManagmentUtilTest {

  @Test
  public void constructIdentityManagementUpdateUserResource_withPortalApplication_shouldSucceed() {
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setApplicationName(DataConstants.PORTAL);
    IdentityManagementUpdateUserResource idmUpdateUserResource =
        UserManagmentUtil.constructIdentityManagementUpdateUserResource(userUpdationDto,
            new ArrayList<>(Arrays.asList(DataConstants.BP_ADMIN)), new ArrayList<>(Arrays.asList("AG01", "AG02")));
    assertNotNull(idmUpdateUserResource);
  }

  @Test
  public void constructIdentityManagementUpdateUserResource_withPartnerPortalApplication_shouldSucceed() {
    UserUpdationDto userUpdationDto = UserTestUtility.getUserUpdationDto();
    userUpdationDto.setApplicationName(DataConstants.PARTNER_PORTAL);
    userUpdationDto.setBusinessUnit(BusinessUnit.EU_ACQUIRING_EEA.name());
    userUpdationDto.setRolesToDelete(new ArrayList<>(Arrays.asList(DataConstants.BP_ISV_ADMIN)));
    userUpdationDto.setAccessGroupsToDelete(new ArrayList<>(Arrays.asList("AG1")));
    userUpdationDto.setAccessGroupsToHardDelete(new ArrayList<>(Arrays.asList("AG2")));
    userUpdationDto.setOwnerId("1234");
    userUpdationDto.setOwnerType(DataConstants.PMLE);
    userUpdationDto.setUserAssignedApplications(new ArrayList<>(Arrays.asList(DataConstants.PORTAL)));
    IdentityManagementUpdateUserResource idmUpdateUserResource =
        UserManagmentUtil.constructIdentityManagementUpdateUserResource(userUpdationDto,
            new ArrayList<>(Arrays.asList(DataConstants.BP_ADMIN)), new ArrayList<>(Arrays.asList("AG01", "AG02")));
    assertNotNull(idmUpdateUserResource);
  }

  @Test
  public void constructUserEntity_whenUserPresentInDb_shouldSucceed() {
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setStatus(UserStatus.ACTIVE);
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.BUSINESS_UNIT, BusinessUnit.EU_ACQUIRING_EEA.name());
    userProvisioningUserResource.setCustomProperties(customProperties);
    userProvisioningUserResource.setRoles(new ArrayList<>(Arrays.asList("BP_EU_ADMIN")));
    Optional<User> optionalUser = Optional.of(UserTestUtility.getUser());
    User user = UserManagmentUtil.constructUserEntity(optionalUser, userProvisioningUserResource, new OwnerInfo());
    assertNotNull(user);
  }

  @Test
  public void constructUserEntity_whenUserNotPresentInDb_shouldSucceed() {
    UserProvisioningUserResource userProvisioningUserResource = UserTestUtility.getUserProvisioningUserResource();
    userProvisioningUserResource.setStatus(UserStatus.ACTIVE);
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.BUSINESS_UNIT, BusinessUnit.EU_ACQUIRING_EEA.name());
    userProvisioningUserResource.setCustomProperties(customProperties);
    userProvisioningUserResource.setRoles(new ArrayList<>(Arrays.asList("BP_EU_ADMIN")));
    Optional<User> optionalUser = Optional.empty();
    User user = UserManagmentUtil.constructUserEntity(optionalUser, userProvisioningUserResource, new OwnerInfo());
    assertNotNull(user);
  }

  @Test
  public void getSkrillTellerWalletIds_withValidInput_shouldSucceed() {
    List<UserResponseResource> userResponseList = UserTestUtility.getUserResponseList();
    Set<String> wallets = UserManagmentUtil.getSkrillTellerWalletIds(userResponseList);
    assertNotNull(wallets);
  }

  @Test
  public void getWalletIdsByBusinessUnit_withValidInput_shouldSucceed() {
    List<UserResponseResource> userResponseList = UserTestUtility.getUserResponseList();
    Set<String> wallets = UserManagmentUtil.getWalletIdsByBusinessUnit(userResponseList, DataConstants.BINANCE);
    assertNotNull(wallets);
  }

  @Test
  public void formAccountGroupsListResponse_withValidInput_shouldSucceed() {
    Map<String, Integer> accountGroupsCountMap = new HashMap<>();
    accountGroupsCountMap.put("ag1", 2);
    accountGroupsCountMap.put("ag2", 5);
    List<AccountGroupMerchantSearchResponse> accountGroupsResponseList =
        UserManagmentUtil.formAccountGroupsListResponse(accountGroupsCountMap);
    assertNotNull(accountGroupsResponseList);
    assertThat(accountGroupsResponseList.size(), Is.is(2));
  }

  @Test
  public void getBusinessUnit_withBusinessUnitInRequest_shouldSuccess() {
    String businessUnit = UserManagmentUtil.getBusinessUnit(BusinessUnit.EU_ACQUIRING_EEA.toString(), null);
    assertNotNull(businessUnit);
    assertThat(businessUnit, Is.is(BusinessUnit.EU_ACQUIRING_EEA.toString().toLowerCase()));
  }

  @Test
  public void getBusinessUnit_withoutBusinessUnitInRequestAndUser_shouldSuccess() {
    AuthorizationInfo authInfo = UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL);
    authInfo.setBusinessUnit(BusinessUnit.EU_ACQUIRING_EEA.toString());
    CommonThreadLocal.setAuthLocal(authInfo);
    String businessUnit = UserManagmentUtil.getBusinessUnit(null, null);
    assertNotNull(businessUnit);
    assertThat(businessUnit, Is.is(BusinessUnit.EU_ACQUIRING_EEA.toString().toLowerCase()));
  }

  @Test
  public void getBusinessUnit_withoutBusinessUnitInRequest_shouldSuccess() {
    AuthorizationInfo authInfo = UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL);
    authInfo.setBusinessUnit(BusinessUnit.EU_ACQUIRING_EEA.toString());
    CommonThreadLocal.setAuthLocal(authInfo);
    String businessUnit = UserManagmentUtil.getBusinessUnit(null, BusinessUnit.EU_ACQUIRING_EEA.toString());
    assertNull(businessUnit);
  }

  @Test
  public void getUpdatedUserOwnerInfo_withPmleOwnerTypeAndId_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    IdentityManagementUserResource idmResponse = UserTestUtility.getIdentityManagementUserResource();
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.OWNER_TYPE, DataConstants.PMLE);
    customProperties.put(DataConstants.OWNER_ID, "1234");
    idmResponse.setCustomProperties(customProperties);
    OwnerInfo ownerInfo = UserManagmentUtil.getUpdatedUserOwnerInfo(idmResponse);
    assertNotNull(ownerInfo.getOwnerId());
    assertNotNull(ownerInfo.getOwnerType());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void getUpdatedUserOwnerInfo_withoutOwnerProperties_shouldSucceed() {
    IdentityManagementUserResource idmResponse = UserTestUtility.getIdentityManagementUserResource();
    Map<String, Object> customProperties = new HashMap<>();
    idmResponse.setCustomProperties(customProperties);
    OwnerInfo ownerInfo = UserManagmentUtil.getUpdatedUserOwnerInfo(idmResponse);
    assertNull(ownerInfo.getOwnerId());
    assertNull(ownerInfo.getOwnerType());
  }

  @Test
  public void getUpdatedUserOwnerInfo_withNullCustomProperties_shouldSucceed() {
    IdentityManagementUserResource idmResponse = UserTestUtility.getIdentityManagementUserResource();
    OwnerInfo ownerInfo = UserManagmentUtil.getUpdatedUserOwnerInfo(idmResponse);
    assertNull(ownerInfo.getOwnerId());
    assertNull(ownerInfo.getOwnerType());
  }

  @Test
  public void getUserNameFromThreadLocal_withValidCommonThreadLocal_shouldSucceed() {
    AuthorizationInfo authInfo = UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL);
    String userName = "testUser1";
    authInfo.setUserName(userName);
    CommonThreadLocal.setAuthLocal(authInfo);
    String response = UserManagmentUtil.getUserNameFromThreadLocal();
    assertNotNull(response);
    assertThat(response, Is.is(userName));
  }

  @Test
  public void getUserNameFromThreadLocal_withNullCommonThreadLocal_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(null);
    String response = UserManagmentUtil.getUserNameFromThreadLocal();
    assertNotNull(response);
    assertThat(response, Is.is("SYSTEM"));
  }

  @Test
  public void contructEventData_withValidInput_shouldSucceed() throws JsonProcessingException {
    String response = UserManagmentUtil.contructEventData("ADD_USER", "Failed: User already exists in the system");
    assertNotNull(response);
  }

  @Test
  public void constructUserEntity_withValidInput_shouldSucceed() {
    UserProvisioningUserResource userResource = UserTestUtility.getUserProvisioningUserResource();
    Map<String, Object> customProperties = new HashMap<>();
    customProperties.put(DataConstants.DIVISION, DataConstants.NBX_PORTAL);
    userResource.setCustomProperties(customProperties);
    userResource.setStatus(UserStatus.ACTIVE);
    User user = UserManagmentUtil.constructUserEntity(Optional.empty(), userResource, UserTestUtility.getOwnerInfo());
    assertNotNull(user);
  }

  @Test
  public void getAssignedApplicationsCreateFlow_withAssignedApplications_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    List<String> userAssignedApplications = new ArrayList<>();
    userAssignedApplications.add(DataConstants.PORTAL);
    userAssignedApplications.add(DataConstants.NBX_PORTAL);
    List<UserAssignedApplications> assignedApps =
        UserManagmentUtil.getAssignedApplicationsCreateFlow(userAssignedApplications, UserTestUtility.getUser());
    assertNotNull(assignedApps);
    assertThat(assignedApps.size(), Is.is(2));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void getAssignedApplicationsCreateFlow_withoutAssignedApplications_shouldSuccess() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    List<UserAssignedApplications> assignedApps =
        UserManagmentUtil.getAssignedApplicationsCreateFlow(null, UserTestUtility.getUser());
    assertNotNull(assignedApps);
    assertThat(assignedApps.size(), Is.is(1));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void validateCustomRoles_shouldNotSucceed() {
    UserManagmentUtil.validateCustomRoles(Arrays.asList("one", "You"), Arrays.asList("You"));
  }
}
