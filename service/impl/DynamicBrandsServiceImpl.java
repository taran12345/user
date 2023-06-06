// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.upf.user.provisioning.config.DynamicBrandsConfig;
import com.paysafe.upf.user.provisioning.service.DynamicBrandsService;
import com.paysafe.upf.user.provisioning.web.rest.resource.BrandPermissionsMapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicBrandsServiceImpl implements DynamicBrandsService {
  @Autowired
  DynamicBrandsConfig dynamicBrandsConfig;

  @Override
  public List<BrandPermissionsMapping> fetchBrands() {
    return dynamicBrandsConfig.getBrands();
  }
}