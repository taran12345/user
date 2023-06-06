// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.enums.RegionType;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserCountDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.UserDetailsResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.UserRegionUpdateResponse;

import java.util.List;
import java.util.Map;

public interface SkrillTellerUserService {
  List<UserResponseResource> getWalletAdminUsers(String walletId);

  Map<String, UserCountDto> getUsersCountByWalletIdsUsingLinkedBrands(List<String> walletIds);

  List<String> getBrands(String application);

  UserDetailsResponseResource getUserEmails(String application, RegionType regionType,
      Integer pageNo, Integer size);

  UserRegionUpdateResponse updateUsersRegion(String application, String loginName, Integer size);
}
