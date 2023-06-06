// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.config.DynamicBrandsConfig;
import com.paysafe.upf.user.provisioning.web.rest.resource.BrandPermissionsMapping;

import java.util.List;

public interface DynamicBrandsService {
  List<BrandPermissionsMapping> fetchBrands();
}
