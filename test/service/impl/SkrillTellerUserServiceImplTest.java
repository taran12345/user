// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.config.BusinessUnitConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.domain.BulkUsers;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.RegionType;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.repository.EmailUserIdView;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.BulkWalletDetailResponse;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserCountDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BusinessProfile;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.EwalletAccount;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.Email;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SkrillTellerUserServiceImplTest {

  @Mock
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Mock
  private UserAssembler userAssembler;

  @Mock
  private SkrillTellerAccountInfoServiceImpl skrillTellerAccountInfoServiceImpl;

  @Mock
  private SkrillTellerConfig skrillTellerConfig;

  @InjectMocks
  private SkrillTellerUserServiceImpl skrillTellerUserServiceImpl;

  @Test
  public void getWalletAdminUsers_withValidData_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    List<User> users = new ArrayList<>();
    users.add(UserTestUtility.getUser());
    when(usersRepository.getWalletUsers(any(), any(), any(), any(), any())).thenReturn(users);
    IdentityManagementUserResource userResource = UserTestUtility.getIdentityManagementUserResource();
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(userAssembler.toUserResponseResource(any(), any())).thenReturn(UserTestUtility.getUserResponseResource());
    mockSkrillTellerConfig();
    List<UserResponseResource> adminUsers = skrillTellerUserServiceImpl.getWalletAdminUsers("test_walletId");
    assertNotNull(adminUsers);
    assertThat(adminUsers.get(0).getUserName(), Is.is("test_loginName"));
    verify(usersRepository, Mockito.times(1)).getWalletUsers(any(), any(), any(), any(), any());
    verify(identityManagementFeignClient, Mockito.times(1)).getUser(any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void getWalletAdminUsers_withBinanceBusinessUnit_shouldSucceed() {
    AuthorizationInfo authInfo = UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL);
    authInfo.setBusinessUnit(DataConstants.BINANCE);
    CommonThreadLocal.setAuthLocal(authInfo);
    List<User> users = new ArrayList<>();
    users.add(UserTestUtility.getUser());
    when(usersRepository.getWalletUsersByAccessRole(any(), any(), any(), anyInt(), any())).thenReturn(users);
    IdentityManagementUserResource userResource = UserTestUtility.getIdentityManagementUserResource();
    when(identityManagementFeignClient.getUser(any(), any()))
        .thenReturn(new ResponseEntity<>(userResource, HttpStatus.OK));
    when(userAssembler.toUserResponseResource(any(), any())).thenReturn(UserTestUtility.getUserResponseResource());
    mockSkrillTellerConfig();
    List<UserResponseResource> adminUsers = skrillTellerUserServiceImpl.getWalletAdminUsers("test_walletId");
    assertNotNull(adminUsers);
    assertThat(adminUsers.get(0).getUserName(), Is.is("test_loginName"));
    verify(usersRepository, Mockito.times(1)).getWalletUsersByAccessRole(any(), any(), any(), anyInt(), any());
    verify(identityManagementFeignClient, Mockito.times(1)).getUser(any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void getUsersCountByWalletIds_withAllWalletsExist_shouldReturnCountForAllWallets() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    stubUsersCountRepository(0L);
    List<String> walletIds = Arrays.asList("123", "124");
    when(skrillTellerAccountInfoServiceImpl.fetchBulkWalletInfo(any(), any()))
        .thenReturn(toBulkWalletDetailResponse(false));

    final Map<String, UserCountDto> usersCountByWalletIds =
        skrillTellerUserServiceImpl.getUsersCountByWalletIdsUsingLinkedBrands(walletIds);

    assertEquals(walletIds.size(), usersCountByWalletIds.keySet().size());
    usersCountByWalletIds.values().forEach(userCount -> {
      assertEquals(12L, userCount.getAdminUsers());
      assertEquals(12L, userCount.getTotalUsers());
    });
    assertEquals(1, usersCountByWalletIds.get("123").getEwalletAccounts().size());
    assertNull(usersCountByWalletIds.get("124").getEwalletAccounts());
  }

  @Test
  public void getUsersCountByWalletIds_withAllWalletsExist_businessProfileAndWalletsShouldMatch() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    stubUsersCountRepository(0L);
    List<String> walletIds = Arrays.asList("123", "124");
    when(skrillTellerAccountInfoServiceImpl.fetchBulkWalletInfo(any(), any()))
        .thenReturn(toBulkWalletDetailResponse(true));

    final Map<String, UserCountDto> usersCountByWalletIds =
        skrillTellerUserServiceImpl.getUsersCountByWalletIdsUsingLinkedBrands(walletIds);

    assertEquals(walletIds.size(), usersCountByWalletIds.keySet().size());
    usersCountByWalletIds.values().forEach(userCount -> {
      assertEquals(12L, userCount.getAdminUsers());
      assertEquals(12L, userCount.getTotalUsers());
    });
    assertEquals(1, usersCountByWalletIds.get("123").getEwalletAccounts().size());
    assertNull(usersCountByWalletIds.get("123").getName());

    assertNull(usersCountByWalletIds.get("124").getEwalletAccounts());
    assertEquals("Veronica", usersCountByWalletIds.get("124").getName());
  }

  @Test
  public void getUsersCountByWalletIds_withFirstWalletDoesNotExist_shouldReturnCountForFirstWalletAsZero() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    stubUsersCountRepository(1L);
    when(skrillTellerAccountInfoServiceImpl.fetchBulkWalletInfo(any(), any()))
        .thenReturn(toBulkWalletDetailResponse(false));
    List<String> walletIds = Arrays.asList("123", "124");
    final Map<String, UserCountDto> usersCountByWalletIds =
        skrillTellerUserServiceImpl.getUsersCountByWalletIdsUsingLinkedBrands(walletIds);
    assertEquals(walletIds.size(), usersCountByWalletIds.keySet().size());
    assertEquals(walletIds.size(), usersCountByWalletIds.values().size());
    assertEquals(0L, usersCountByWalletIds.get(walletIds.get(0)).getAdminUsers());
    assertEquals(0L, usersCountByWalletIds.get(walletIds.get(0)).getTotalUsers());
  }

  @Test
  public void getUsersCountByWalletIdsUsingLinkedBrands_withSkrillLinkedBrands_shouldReturnCountForAllWallets() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    stubUsersCountRepository(0L);
    BulkWalletDetailResponse bulkWalletDetailResponse = UserTestUtility.sampleBulkWalletDetail();
    bulkWalletDetailResponse.setCustomers(new ArrayList<>());
    when(skrillTellerAccountInfoServiceImpl.fetchBulkWalletInfo(any(), any())).thenReturn(bulkWalletDetailResponse)
        .thenReturn(toBulkWalletDetailResponse(false));
    Map<String, List<String>> linkedBrands = new HashMap<>();
    linkedBrands.put(DataConstants.SKRILL.toLowerCase(), Arrays.asList("binance"));
    ReflectionTestUtils.setField(skrillTellerConfig, "linkedBrands", linkedBrands);
    when(skrillTellerConfig.getLinkedBrands()).thenReturn(linkedBrands);
    List<String> walletIds = Arrays.asList("123", "124");
    final Map<String, UserCountDto> usersCountByWalletIds =
        skrillTellerUserServiceImpl.getUsersCountByWalletIdsUsingLinkedBrands(walletIds);
    assertEquals(walletIds.size(), usersCountByWalletIds.keySet().size());
    usersCountByWalletIds.values().forEach(userCount -> {
      assertEquals(0L, userCount.getAdminUsers());
      assertEquals(12L, userCount.getTotalUsers());
    });
    assertEquals(1, usersCountByWalletIds.get("123").getEwalletAccounts().size());
    assertNull(usersCountByWalletIds.get("124").getEwalletAccounts());
  }

  @Test
  public void getUsersCountByWalletIdsUsingLinkedBrands_withNetellerInvalidWalletIds_shouldReturnEmpty() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    stubUsersCountRepository(0L);
    BulkWalletDetailResponse bulkWalletDetailResponse = UserTestUtility.sampleBulkWalletDetail();
    bulkWalletDetailResponse.setCustomers(new ArrayList<>());
    when(skrillTellerAccountInfoServiceImpl.fetchBulkWalletInfo(any(), any())).thenReturn(bulkWalletDetailResponse);
    Map<String, List<String>> linkedBrands = new HashMap<>();
    linkedBrands.put(DataConstants.SKRILL.toLowerCase(), Arrays.asList("binance"));
    ReflectionTestUtils.setField(skrillTellerConfig, "linkedBrands", linkedBrands);
    when(skrillTellerConfig.getLinkedBrands()).thenReturn(linkedBrands);
    List<String> walletIds = Arrays.asList("123", "124");
    final Map<String, UserCountDto> usersCountByWalletIds =
        skrillTellerUserServiceImpl.getUsersCountByWalletIdsUsingLinkedBrands(walletIds);
    assertEquals(0, usersCountByWalletIds.keySet().size());
  }

  @Test
  public void getBrands() {
    Map<String, List<String>> linkedBrands = new HashMap<>();
    linkedBrands.put(DataConstants.SKRILL.toLowerCase(), Arrays.asList("binance", "ftx"));
    ReflectionTestUtils.setField(skrillTellerConfig, "linkedBrands", linkedBrands);
    when(skrillTellerConfig.getBrands()).thenReturn(linkedBrands);
    List<String> brands =
        skrillTellerUserServiceImpl.getBrands("skrill");
    assertEquals(2, brands.size());
  }

  @Test
  public void downloadUserEmails() {
    UserAccessGroupMappingDao accessGroupMappingDao = new UserAccessGroupMappingDao();
    accessGroupMappingDao.setResourceId("123");
    when(usersRepository.getEmailsByApplicationAndRegion(anyString(), anyString(), any()))
        .thenReturn(Page.empty());
    skrillTellerUserServiceImpl.getUserEmails("skrill", RegionType.INC, 0, 10);
    verify(usersRepository, Mockito.times(1)).getEmailsByApplicationAndRegion(any(), any(), any());
  }

  @Test(expected = BadRequestException.class)
  public void updateUsersRegionWithEmptyApplication() {
    skrillTellerUserServiceImpl.updateUsersRegion("", "abc",0);
  }

  @Test(expected = BadRequestException.class)
  public void updateUsersRegionUserNotFound() {
    when(usersRepository.findByLoginNameAndApplication(anyString(), anyString())).thenReturn(Optional.empty());
    skrillTellerUserServiceImpl.updateUsersRegion("skrill", "abc",0);
  }

  @Test
  public void updateUsersRegionWithLoginName() {
    User user = new User();
    UserAccessGroupMappingDao accessGroupMappingDao = new UserAccessGroupMappingDao();
    accessGroupMappingDao.setResourceId("123");
    user.setAccessGroupMappingDaos(Arrays.asList(accessGroupMappingDao));
    when(usersRepository.findByLoginNameAndApplication(anyString(), anyString()))
        .thenReturn(Optional.of(user));
    when(skrillTellerAccountInfoServiceImpl.fetchBulkWalletInfo(any(),any()))
        .thenReturn(toBulkWalletDetailResponse(false));
    when(usersRepository.save(any())).thenReturn(new User());
    skrillTellerUserServiceImpl.updateUsersRegion("skrill", "abc",0);
    verify(usersRepository, Mockito.times(1)).save(any());
  }

  @Test(expected = BadRequestException.class)
  public void updateUsersRegionWithPageSizeEmptyResults() {
    User user = new User();
    UserAccessGroupMappingDao accessGroupMappingDao = new UserAccessGroupMappingDao();
    accessGroupMappingDao.setResourceId("123");
    user.setAccessGroupMappingDaos(Arrays.asList(accessGroupMappingDao));
    when(usersRepository.findByApplicationAndRegionIsNullOrderByLastModifiedDateDesc(any(), any())).thenReturn(
        Page.empty());
    when(usersRepository.save(any())).thenReturn(new User());
    skrillTellerUserServiceImpl.updateUsersRegion("skrill", "",10);
  }

  @Test
  public void updateUsersRegionWithPageSize() {
    User user = new User();
    UserAccessGroupMappingDao accessGroupMappingDao = new UserAccessGroupMappingDao();
    accessGroupMappingDao.setResourceId("123");
    user.setAccessGroupMappingDaos(Arrays.asList(accessGroupMappingDao));
    when(usersRepository.findByApplicationAndRegionIsNullOrderByLastModifiedDateDesc(any(), any()))
        .thenReturn(new PageImpl<>(Arrays.asList(user)));
    when(skrillTellerAccountInfoServiceImpl.fetchBulkWalletInfo(any(),any()))
        .thenReturn(toBulkWalletDetailResponse(false));
    when(usersRepository.save(any())).thenReturn(new User());
    skrillTellerUserServiceImpl.updateUsersRegion("skrill", "",10);
    verify(usersRepository, Mockito.times(1)).save(any());
  }

  @Test(expected = Exception.class)
  public void updateUsersRegionFailedWithException() {
    User user = new User();
    UserAccessGroupMappingDao accessGroupMappingDao = new UserAccessGroupMappingDao();
    accessGroupMappingDao.setResourceId("123");
    user.setAccessGroupMappingDaos(Arrays.asList(accessGroupMappingDao));
    when(usersRepository.findByApplicationAndRegionIsNullOrderByLastModifiedDateDesc(any(), any()))
        .thenReturn(new PageImpl<>(Arrays.asList(user)));
    when(skrillTellerAccountInfoServiceImpl.fetchBulkWalletInfo(any(),any()))
        .thenReturn(toBulkWalletDetailResponse(false));
    when(usersRepository.save(any())).thenThrow(new Exception());
    skrillTellerUserServiceImpl.updateUsersRegion("skrill", "",10);
    verify(usersRepository, Mockito.times(1)).save(any());
  }

  private BulkWalletDetailResponse toBulkWalletDetailResponse(boolean isBusinessProfile) {
    List<BasicWalletInfo> basicWalletInfo = new ArrayList<>();
    if (isBusinessProfile) {
      walletDetailsWithProfile(basicWalletInfo);
    } else {
      bulkWalletDetails(basicWalletInfo);
    }

    BulkWalletDetailResponse bulkWalletDetailResponse = new BulkWalletDetailResponse();
    bulkWalletDetailResponse.setCustomers(basicWalletInfo);

    return bulkWalletDetailResponse;
  }

  private void walletDetailsWithProfile(List<BasicWalletInfo> basicWalletInfo) {
    bulkWalletDetails(basicWalletInfo);
    BasicWalletInfo businessProfileAlone = basicWalletInfo.get(1);
    businessProfileAlone.setBusinessProfile(new BusinessProfile("Veronica", null));

  }

  private void bulkWalletDetails(List<BasicWalletInfo> basicWalletInfo) {
    BasicWalletInfo walletInfo = new BasicWalletInfo();
    walletInfo.setId("123");
    EwalletAccount walletAccount = new EwalletAccount();
    walletAccount.setCurrency("USD");
    walletAccount.setId("7887989");
    walletAccount.setCompany("INC");
    List<EwalletAccount> ewalletAccounts = new ArrayList<>();
    ewalletAccounts.add(walletAccount);
    walletInfo.setEwalletAccounts(ewalletAccounts);
    basicWalletInfo.add(walletInfo);
    walletInfo = new BasicWalletInfo();
    walletInfo.setId("124");
    walletInfo.setEwalletAccounts(null);
    basicWalletInfo.add(walletInfo);
  }

  private void stubUsersCountRepository(long skip) {
    when(userAccessGroupMapppingRepository.getUserCountFilterBy(anyList(), eq(AccessResourceStatus.ACTIVE),
        eq(AccessGroupType.DEFAULT_ADMIN), eq(DataConstants.WALLETS)))
        .then(answer -> prepareAnswerForBulkUser(answer, skip));
    when(userAccessGroupMapppingRepository.getUserCountFilterBy(anyList(), eq(AccessResourceStatus.ACTIVE),
        eq(DataConstants.WALLETS))).then(answer -> prepareAnswerForBulkUser(answer, skip));
  }

  private List<BulkUsers> prepareAnswerForBulkUser(InvocationOnMock mock, long skip) {
    Function<String, BulkUsers> userCountMapper = (walletId) -> {
      BulkUsers bulkUsers = new BulkUsers();
      bulkUsers.setResourceId(walletId);
      bulkUsers.setUsersCount(12L);
      return bulkUsers;
    };
    List<String> walletIds = (List<String>) mock.getArguments()[0];
    return walletIds.stream().skip(skip).map(userCountMapper).collect(Collectors.toList());
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
