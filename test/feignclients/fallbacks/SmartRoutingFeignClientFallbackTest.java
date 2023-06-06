// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients.fallbacks;

import com.paysafe.op.errorhandling.exceptions.ExternalGatewayErrorException;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class SmartRoutingFeignClientFallbackTest {

  @InjectMocks
  private SmartRoutingFeignClientFallback smartRoutingFeignClientFallback;

  /**
   * Test setup.
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = ExternalGatewayErrorException.class)
  public void testGetAccountGroupsV2() {
    smartRoutingFeignClientFallback.getAccountGroupsV2("paymentMethods", null, "NETBANX", "1234", DataConstants.PMLE, 0,
        10);
  }
}
