// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.upf.user.provisioning.enums.IncludeParam;
import com.paysafe.upf.user.provisioning.service.MerchantAccountInfoService;
import com.paysafe.upf.user.provisioning.service.SkrillTellerAccountInfoService;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MerchantAccountInfoServiceImpl implements MerchantAccountInfoService {

  @Autowired
  private SkrillTellerAccountInfoService skrillTellerAccountInfoService;

  @Override
  public List<BasicWalletInfo> getBasicWalletInfo(Set<String> accessibleWalletIds) {
    return skrillTellerAccountInfoService.fetchBulkWalletInfo(accessibleWalletIds,
        IncludeParam.BUSINESS_PROFILE)
        .getCustomers();
  }

  @Override
  public List<BasicWalletInfo> getAccountInfo(Set<String> accessibleWalletIds) {
    return skrillTellerAccountInfoService.fetchBulkWalletInfo(accessibleWalletIds,
        IncludeParam.EWALLET_ACCOUNTS)
        .getCustomers();
  }

  @Override
  public List<BasicWalletInfo> getWalletProfileAndMerchantSettings(Set<String> walletIds) {
    return skrillTellerAccountInfoService.fetchBulkWalletInfo(walletIds,
        IncludeParam.PROFILE,
        IncludeParam.MERCHANT_SETTINGS)
        .getCustomers();
  }

}