// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.upf.user.provisioning.config.FeatureFlagConfig;
import com.paysafe.upf.user.provisioning.service.FeatureFlagService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class FeatureFlagServiceImpl implements FeatureFlagService {
  @Autowired
  FeatureFlagConfig featureFlagConfig;

  @Override
  public HashMap<String, Boolean> fetchFeatureFlag() {
    return featureFlagConfig.getFeatureFlag();
  }
}
