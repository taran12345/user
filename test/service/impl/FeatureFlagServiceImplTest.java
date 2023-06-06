// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

import com.paysafe.upf.user.provisioning.config.FeatureFlagConfig;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashMap;

public class FeatureFlagServiceImplTest {

  @InjectMocks
  private FeatureFlagServiceImpl featureFlagServiceImpl;

  @Mock
  private FeatureFlagConfig featureFlagConfig;

  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void testFetchFeatureFlag() {
    doReturn(new HashMap<>()).when(featureFlagConfig).getFeatureFlag();
    assertNotNull(featureFlagServiceImpl.fetchFeatureFlag());
  }
}
