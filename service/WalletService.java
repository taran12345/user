// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.web.rest.resource.WalletInfoResource;

import java.util.List;

public interface WalletService {

  List<WalletInfoResource> findLinkedWalletsByLinkedBrands(String application, String walletId);
}
