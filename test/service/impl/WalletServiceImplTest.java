// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerVaultConfig;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.MerchantAccountInfoService;
import com.paysafe.upf.user.provisioning.service.SkrillTellerAccountInfoService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.BulkWalletDetailResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.WalletInfoResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class WalletServiceImplTest {

  private static final String BINANCE = "binance";

  @Mock
  @Qualifier("externalRestTemplate")
  private RestTemplate externalRestTemplate;

  @Mock
  private SkrillTellerConfig skrillTellerConfig;

  @Mock
  private SkrillTellerVaultConfig skrillTellerVaultConfig;

  @Mock
  private SkrillTellerAccountInfoService skrillTellerAccountInfoService;

  @Mock
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Mock
  private MerchantAccountInfoService merchantAccountInfoService;

  @InjectMocks
  WalletServiceImpl walletService;


  @Test
  public void testGetLinkedWalletsInfo() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    when(merchantAccountInfoService.getAccountInfo(any()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    when(merchantAccountInfoService.getBasicWalletInfo(any()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    when(skrillTellerAccountInfoService.getMerchantGroupByWalletId(any()))
        .thenReturn(UserTestUtility.getGroupCustomerIds());
    when(skrillTellerAccountInfoService.getMerchantLinkedToWalletId(any()))
        .thenReturn(UserTestUtility.getLinkedCustomerIds());
    when(skrillTellerAccountInfoService.fetchBulkWalletInfo(anySet(), any()))
        .thenReturn(UserTestUtility.sampleBulkWalletDetail());
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(any()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    when(userAccessGroupMapppingRepository
        .countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(anyString(),
            anyString(), eq(AccessGroupType.CUSTOMIZED), eq(AccessResourceStatus.ACTIVE))).thenReturn(10L);
    when(userAccessGroupMapppingRepository
        .countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(anyString(),
            anyString(), eq(AccessGroupType.DEFAULT_ADMIN), eq(AccessResourceStatus.ACTIVE))).thenReturn(20L);
    List<WalletInfoResource> linkedWalletInfoResources =
        walletService.findLinkedWalletsByLinkedBrands("SKRILL", "1234");
    Assert.assertNotNull(linkedWalletInfoResources);
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void getLinkedWalletsInfo_withSkrillInvalidWalletId_shouldThrowException() throws Exception {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    List<BasicWalletInfo> walletProfilesList =
        Arrays.asList(UserTestUtility.getBasicWalletInfo());
    walletProfilesList.get(0).getProfile().setLockLevel(DataConstants.SKRILL_LOCK_LEVEL_17);
    when(skrillTellerAccountInfoService.fetchBulkWalletInfo(anySet(), any()))
        .thenReturn(UserTestUtility.sampleBulkWalletDetail());
    when(skrillTellerAccountInfoService.getMerchantLinkedToWalletId(anyString()))
        .thenReturn(UserTestUtility.getLinkedCustomerIds());
    Map<String, List<String>> linkedBrands = new HashMap<>();
    linkedBrands.put(DataConstants.SKRILL.toLowerCase(), Arrays.asList());
    ReflectionTestUtils.setField(skrillTellerConfig, "linkedBrands", linkedBrands);
    when(skrillTellerConfig.getLinkedBrands()).thenReturn(linkedBrands);
    BasicWalletInfo basicWalletInfo = UserTestUtility.getBasicWalletInfo();
    basicWalletInfo.getProfile().setBrand("neteller");
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(anySet()))
        .thenReturn(Arrays.asList(basicWalletInfo));
    walletService.findLinkedWalletsByLinkedBrands(DataConstants.SKRILL, "1234");
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void getLinkedWalletsInfo_withNetellerInvalidWalletId_shouldThrowException() throws Exception {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_NETELLER));
    List<BasicWalletInfo> walletProfilesList =
        Arrays.asList(UserTestUtility.getBasicWalletInfo());
    walletProfilesList.get(0).getProfile().setLockLevel(DataConstants.NETELLER_LOCK_LEVEL_22);
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(any())).thenReturn(walletProfilesList);
    Map<String, List<String>> linkedBrands = new HashMap<>();
    linkedBrands.put(DataConstants.SKRILL.toLowerCase(), Arrays.asList("binance"));
    ReflectionTestUtils.setField(skrillTellerConfig, "linkedBrands", linkedBrands);
    when(skrillTellerConfig.getLinkedBrands()).thenReturn(linkedBrands);
    walletService.findLinkedWalletsByLinkedBrands(DataConstants.NETELLER, "1234");
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testGetLinkedWalletsInfoWithApplicaitonNotPassed() {
    when(merchantAccountInfoService.getAccountInfo(any()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    when(merchantAccountInfoService.getBasicWalletInfo(any()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    when(skrillTellerAccountInfoService.getMerchantGroupByWalletId(any()))
        .thenReturn(UserTestUtility.getGroupCustomerIds());
    when(skrillTellerAccountInfoService.getMerchantLinkedToWalletId(any()))
        .thenReturn(UserTestUtility.getLinkedCustomerIds());
    when(skrillTellerAccountInfoService.fetchBulkWalletInfo(anySet(), any()))
        .thenReturn(UserTestUtility.sampleBulkWalletDetail());
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(any()))
        .thenReturn(Arrays.asList(UserTestUtility.getBasicWalletInfo()));
    when(userAccessGroupMapppingRepository.countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(
        anyString(), anyString(), eq(AccessGroupType.CUSTOMIZED), eq(AccessResourceStatus.ACTIVE))).thenReturn(10L);
    when(userAccessGroupMapppingRepository.countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(
        anyString(), anyString(), eq(AccessGroupType.DEFAULT_ADMIN), eq(AccessResourceStatus.ACTIVE))).thenReturn(20L);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    List<WalletInfoResource> linkedWalletInfoResources = walletService.findLinkedWalletsByLinkedBrands("", "1234");
    Assert.assertNotNull(linkedWalletInfoResources);
  }

  @Test
  public void findLinkedWalletsByLinkedBrands_withSkrillLinkedBrands_shouldSucceed() throws Exception {
    AuthorizationInfo authorizationInfo = UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL);
    authorizationInfo.setBusinessUnit(DataConstants.BINANCE.toLowerCase());
    CommonThreadLocal.setAuthLocal(authorizationInfo);
    when(skrillTellerAccountInfoService.fetchBulkWalletInfo(anySet(), any()))
        .thenReturn(UserTestUtility.sampleBulkWalletDetail());
    when(skrillTellerAccountInfoService.getMerchantLinkedToWalletId(anyString()))
        .thenReturn(UserTestUtility.getLinkedCustomerIds());
    BasicWalletInfo basicWalletInfo = UserTestUtility.getBasicWalletInfo();
    basicWalletInfo.getProfile().setBrand(BINANCE);
    List<BasicWalletInfo> walletProfilesList = Arrays.asList(basicWalletInfo);
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(anySet())).thenReturn(walletProfilesList);
    List<WalletInfoResource> linkedWallets =
        walletService.findLinkedWalletsByLinkedBrands(DataConstants.SKRILL, "1234");
    assertNotNull(linkedWallets);
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = BadRequestException.class)
  public void findLinkedWalletsByLinkedBrands_withSkrillLinkedBrandsInvalidWalletId_shouldSucceed() throws Exception {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    List<BasicWalletInfo> walletProfilesList = Arrays.asList(UserTestUtility.getBasicWalletInfo());
    walletProfilesList.get(0).getProfile().setLockLevel(DataConstants.SKRILL_LOCK_LEVEL_17);
    when(skrillTellerAccountInfoService.fetchBulkWalletInfo(anySet(), any()))
        .thenReturn(UserTestUtility.sampleBulkWalletDetail());
    when(skrillTellerAccountInfoService.getMerchantLinkedToWalletId(anyString()))
        .thenReturn(UserTestUtility.getLinkedCustomerIds());
    Map<String, List<String>> linkedBrands = new HashMap<>();
    linkedBrands.put(DataConstants.SKRILL.toLowerCase(), Arrays.asList("binance"));
    ReflectionTestUtils.setField(skrillTellerConfig, "linkedBrands", linkedBrands);
    when(skrillTellerConfig.getLinkedBrands()).thenReturn(linkedBrands);
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(anySet())).thenReturn(new ArrayList<>())
        .thenReturn(walletProfilesList);
    walletService.findLinkedWalletsByLinkedBrands(DataConstants.SKRILL, "1234");
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void findLinkedWalletsByLinkedBrands_withSkrillLinkedBinanceBrand_shouldSucceed() throws Exception {
    AuthorizationInfo authorizationInfo = UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL);
    authorizationInfo.setBusinessUnit(DataConstants.BINANCE.toLowerCase());
    CommonThreadLocal.setAuthLocal(authorizationInfo);
    BulkWalletDetailResponse bulkWalletDetailResponse = UserTestUtility.sampleBulkWalletDetail();
    bulkWalletDetailResponse.getCustomers().get(0).getProfile().setBrand(DataConstants.BINANCE.toLowerCase());
    when(skrillTellerAccountInfoService.fetchBulkWalletInfo(anySet(), any())).thenReturn(bulkWalletDetailResponse);
    when(skrillTellerAccountInfoService.getMerchantLinkedToWalletId(anyString()))
        .thenReturn(UserTestUtility.getLinkedCustomerIds());
    BasicWalletInfo basicWalletInfo = UserTestUtility.getBasicWalletInfo();
    basicWalletInfo.getProfile().setBrand(BINANCE);
    List<BasicWalletInfo> walletProfilesList = Arrays.asList(basicWalletInfo);
    when(merchantAccountInfoService.getWalletProfileAndMerchantSettings(anySet())).thenReturn(walletProfilesList);
    List<WalletInfoResource> linkedWallets =
        walletService.findLinkedWalletsByLinkedBrands(DataConstants.SKRILL, "1234");
    assertNotNull(linkedWallets);
    CommonThreadLocal.unsetAuthLocal();
  }
}
