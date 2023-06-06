// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class TestHttpClientConfigProperties {

  /**
   * Test cases initialization.
   */
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @InjectMocks
  private HttpClientConfigProperties httpClientConfigProperties;

  @Test
  public void testHttpClientConfigPropertiesGetterSetters() {
    httpClientConfigProperties.setProxyPort("3000");
    httpClientConfigProperties.setProxyEnabled(true);
    httpClientConfigProperties.setConnectionRequestTimeout(2);
    httpClientConfigProperties.setConnectionsPerRoute(2);
    httpClientConfigProperties.setProxyUrl("www.test.com");
    httpClientConfigProperties.setTotalConnections(2);
    httpClientConfigProperties.setSocketTimeout(2);
    assertNotNull(httpClientConfigProperties.getProxyPort());
    assertNotNull(httpClientConfigProperties.getConnectionRequestTimeout());
    assertNotNull(httpClientConfigProperties.getConnectionsPerRoute());
    assertNotNull(httpClientConfigProperties.getProxyUrl());
    assertNotNull(httpClientConfigProperties.getTotalConnections());
  }

}
