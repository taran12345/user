// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import java.util.HashMap;

public interface FeatureFlagService {
  HashMap<String, Boolean> fetchFeatureFlag();
}
