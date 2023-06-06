// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients.fallbacks;

import com.paysafe.op.errorhandling.exceptions.ExternalGatewayErrorException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class UaaFeignClientFallBackTest {

  @InjectMocks
  private UaaFeignClientFallback smartRoutingFeignClientFallback;

  /**
   * Test setup.
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = ExternalGatewayErrorException.class)
  public void test_authenticateUser_JsonNodeResponse() {
    smartRoutingFeignClientFallback.authenticateUser_JsonNodeResponse("JSON", "UAA", "UAA", "testuser", "Demo@1234",
        "ALL");
  }

}
