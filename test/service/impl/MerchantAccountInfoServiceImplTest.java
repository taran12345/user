// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.upf.user.provisioning.service.SkrillTellerAccountInfoService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MerchantAccountInfoServiceImplTest {

  @Mock
  private SkrillTellerAccountInfoService skrillTellerAccountInfoService;

  @InjectMocks
  private MerchantAccountInfoServiceImpl merchantAccountInfoServiceImpl;

  @Test
  public void getBasicWalletInfo_withValidData_shouldSucceed() {
    when(skrillTellerAccountInfoService.fetchBulkWalletInfo(anySet(), any()))
        .thenReturn(UserTestUtility.sampleBulkWalletDetail());
    Set<String> walletIds = new HashSet<>(Arrays.asList("123", "456"));
    merchantAccountInfoServiceImpl.getBasicWalletInfo(walletIds);
    verify(skrillTellerAccountInfoService, times(1)).fetchBulkWalletInfo(anySet(), any());
  }

  @Test
  public void getBasicAccountInfo_withValidData_shouldSucceed() {
    when(skrillTellerAccountInfoService.fetchBulkWalletInfo(anySet(), any()))
        .thenReturn(UserTestUtility.sampleBulkWalletDetail());
    Set<String> walletIds = new HashSet<>(Arrays.asList("123", "456"));
    merchantAccountInfoServiceImpl.getAccountInfo(walletIds);
    verify(skrillTellerAccountInfoService, times(1)).fetchBulkWalletInfo(anySet(), any());
  }

  @Test
  public void getWalletProfileAndMerchantSettings_withValidData_shouldSucceed() {
    when(skrillTellerAccountInfoService.fetchBulkWalletInfo(anySet(), any()))
        .thenReturn(UserTestUtility.sampleBulkWalletDetail());
    Set<String> walletIds = new HashSet<>(Arrays.asList("123", "456"));
    merchantAccountInfoServiceImpl.getWalletProfileAndMerchantSettings(walletIds);
    verify(skrillTellerAccountInfoService, times(1)).fetchBulkWalletInfo(anySet(), any());
  }
}
