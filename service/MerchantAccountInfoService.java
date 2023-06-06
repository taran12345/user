// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;

import java.util.List;
import java.util.Set;

public interface MerchantAccountInfoService {

  List<BasicWalletInfo> getBasicWalletInfo(Set<String> accessibleWalletIds);

  List<BasicWalletInfo> getAccountInfo(Set<String> accessibleWalletIds);

  List<BasicWalletInfo> getWalletProfileAndMerchantSettings(Set<String> walletIds);

}
