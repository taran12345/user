// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants.NETELLER;
import static com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants.SKRILL;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.upf.user.provisioning.config.BusinessUnitVaultConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerVaultConfig;
import com.paysafe.upf.user.provisioning.exceptions.UserProvisioningForbiddenException;
import com.paysafe.upf.user.provisioning.model.UaaTokenResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class UaaClientTest {

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private SkrillTellerConfig skrillTellerConfig;

  @Mock
  private SkrillTellerVaultConfig skrillTellerVaultConfig;

  @InjectMocks
  private UaaClient uaaClient;

  /**
   * Initializing the test prerequisites.
   */
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Map<String, BusinessUnitVaultConfig> vaultConfig = getVaultConfigs();
    when(skrillTellerVaultConfig.getUaa()).thenReturn(vaultConfig);
    when(skrillTellerConfig.getUaaHostUrl()).thenReturn("http://api.skrillteller.com");
  }

  private Map<String, BusinessUnitVaultConfig> getVaultConfigs() {
    BusinessUnitVaultConfig skrillClientConfigs = new BusinessUnitVaultConfig();
    skrillClientConfigs.setClientId("client_id");
    skrillClientConfigs.setClientSecret("client_secret");
    skrillClientConfigs.setAccessToken("access_token");
    BusinessUnitVaultConfig netellerClientConfigs = new BusinessUnitVaultConfig();
    netellerClientConfigs.setClientId("client_id");
    netellerClientConfigs.setClientSecret("client_secret");
    netellerClientConfigs.setAccessToken("access_token");
    Map<String, BusinessUnitVaultConfig> vaultConfigs = new HashMap<>();
    vaultConfigs.put("skrill", skrillClientConfigs);
    vaultConfigs.put("neteller", netellerClientConfigs);
    return vaultConfigs;
  }

  @Test
  public void testGetAccessToken() {
    String token = uaaClient.getToken(SKRILL);
    assertThat(token, is("access_token"));
  }

  @Test(expected = UserProvisioningForbiddenException.class)
  public void testGetAccessToken_FailedCase() {
    String token = uaaClient.getToken("test");
  }

  @Test
  public void testGenerateToken() {
    UaaTokenResponse uaaTokenResponse = new UaaTokenResponse();
    uaaTokenResponse.setAccessToken("access_token");
    ResponseEntity<UaaTokenResponse> result = new ResponseEntity<>(uaaTokenResponse, HttpStatus.OK);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
        Matchers.<HttpEntity<MultiValueMap<String, String>>>any(), Matchers.<Class<UaaTokenResponse>>any()))
        .thenReturn(result);
    String token = uaaClient.generateToken(SKRILL);
    Assert.assertNotNull(token);
  }

  @Test(expected = UserProvisioningForbiddenException.class)
  public void testGenerateTokenWhenVaultConfigsNotFound() {
    uaaClient.generateToken("abc");
  }

  @Test
  public void testGenerateToken_failCase() {
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
        Matchers.<HttpEntity<MultiValueMap<String, String>>>any(), Matchers.<Class<UaaTokenResponse>>any()))
        .thenThrow(HttpClientErrorException.class);

    try {
      uaaClient.generateToken(NETELLER);
    } catch (Exception ex) {
      Assert.assertTrue(ex instanceof InternalErrorException);
    }
  }

  @Test
  public void testGenerateToken_WhenExceptionOccurredWhileMakingRestCall() {
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
        Matchers.<HttpEntity<MultiValueMap<String, String>>>any(), Matchers.<Class<UaaTokenResponse>>any()))
        .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

    try {
      uaaClient.generateToken(NETELLER);
    } catch (Exception ex) {
      Assert.assertTrue(ex instanceof InternalErrorException);
    }
  }

  @Test(expected = UserProvisioningForbiddenException.class)
  public void testGenerateToken_failCase_invalid_brand() {
    Map<String, BusinessUnitVaultConfig> uaa = new HashMap<>();
    when(skrillTellerVaultConfig.getUaa()).thenReturn(uaa);

    uaaClient.generateToken("randombrand");

  }

  @Test(expected = InternalErrorException.class)
  public void testGenerateToken_failCase_no_brands_config() {

    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
        Matchers.<HttpEntity<MultiValueMap<String, String>>>any(), Matchers.<Class<UaaTokenResponse>>any()))
        .thenThrow(RestClientException.class);

    when(skrillTellerVaultConfig.getUaa()).thenReturn(null);
    uaaClient.generateToken("randombrand");

  }

  @Test(expected = InternalErrorException.class)
  public void testGenerateToken_failCase_RestCallFails() {
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
        Matchers.<HttpEntity<MultiValueMap<String, String>>>any(), Matchers.<Class<UaaTokenResponse>>any()))
        .thenThrow(RestClientException.class);

    uaaClient.generateToken(NETELLER);

  }
}
