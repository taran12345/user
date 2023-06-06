// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import static org.junit.Assert.assertEquals;

import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
public class SkrillTellerConfigTest {

  @InjectMocks
  SkrillTellerConfig skrillTellerConfig;

  String binanceHost = "http:://binanace";
  String binanceBasePath = "/skrill-dw";
  String uaaPath = "/uaa";
  String binanaceBusinessUnit = "Binance";
  String binanceAdminRole = DataConstants.BP_BINANCE_ADMIN;

  /**
   * initial setup.
   */
  @Before
  public void setUp() {
    BusinessUnitConfig binanceConfig = new BusinessUnitConfig();
    binanceConfig.setHostName(binanceHost);
    binanceConfig.setBasePath(binanceBasePath);
    binanceConfig.setUaaPath(uaaPath);
    binanceConfig.setAdminRole(binanceAdminRole);

    Map<String, BusinessUnitConfig> businessUnits = new HashMap<>();
    businessUnits.put(binanaceBusinessUnit.toLowerCase(), binanceConfig);

    ReflectionTestUtils.setField(skrillTellerConfig, "businessUnits", businessUnits);

    AuthorizationInfo authInfo = new AuthorizationInfo();
    authInfo.setApplication("Skrill");
    authInfo.setBusinessUnit(binanaceBusinessUnit);

    CommonThreadLocal.setAuthLocal(authInfo);
  }

  @Test
  public void getHostNameTest() {
    String actualtHostName = skrillTellerConfig.getHostName();
    assertEquals(binanceHost, actualtHostName);
  }

  @Test
  public void getHostUriTest() {
    String result = skrillTellerConfig.getHostUri();
    assertEquals(binanceHost.concat(binanceBasePath), result);
  }

  @Test
  public void getUaaHostUrlTest() {
    String result = skrillTellerConfig.getUaaHostUrl();
    assertEquals(binanceHost.concat(uaaPath), result);
  }

  @Test
  public void getAdminRoleNameTest() {
    String result = skrillTellerConfig.getAdminRole();
    assertEquals(binanceAdminRole, result);
  }
}