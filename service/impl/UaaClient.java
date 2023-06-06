// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.upf.user.provisioning.config.BusinessUnitVaultConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerVaultConfig;
import com.paysafe.upf.user.provisioning.exceptions.UserProvisioningForbiddenException;
import com.paysafe.upf.user.provisioning.model.UaaTokenResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class UaaClient {
  private static final String GRANT_TYPE = "grant_type";
  private static final String GRANT_TYPE_VALUE = "client_credentials";
  private static final String CLIENT_ID = "client_id";
  private static final String CLIENT_SECRET = "client_secret";

  private static final Logger LOG = LoggerFactory.getLogger(UaaClient.class);

  private RestTemplate restTemplate;

  @Autowired
  private SkrillTellerConfig skrillTellerConfig;

  @Autowired
  private SkrillTellerVaultConfig skrillTellerVaultConfig;

  /**
   * Provides existing long live access token.
   *
   * @return String access token
   */
  public String getToken(String brand) {
    BusinessUnitVaultConfig businessUnitVaultConfig = getClientConfigs(brand);
    if (businessUnitVaultConfig != null) {
      return businessUnitVaultConfig.getAccessToken() == null ? generateToken(brand)
          : businessUnitVaultConfig.getAccessToken();
    }
    throw new UserProvisioningForbiddenException("Invalid brand, access denied.");
  }

  /**
   * Issue access token.
   *
   * @return String new access token
   */
  public String generateToken(String brand) {
    BusinessUnitVaultConfig businessUnitVaultConfig = getClientConfigs(brand);
    String clientId;
    String clientSecret;

    if (businessUnitVaultConfig != null) {
      clientId = businessUnitVaultConfig.getClientId();
      clientSecret = businessUnitVaultConfig.getClientSecret();
    } else {
      throw new UserProvisioningForbiddenException("Invalid brand, access denied.");
    }

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add(UaaClient.GRANT_TYPE, GRANT_TYPE_VALUE);
    body.add(UaaClient.CLIENT_ID, clientId);

    if (skrillTellerConfig.getIncludeClientSecret() == null || skrillTellerConfig.getIncludeClientSecret()) {
      body.add(UaaClient.CLIENT_SECRET, clientSecret);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_FORM_URLENCODED);
    headers.setBasicAuth(clientId, clientSecret);

    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

    if (restTemplate == null) {
      restTemplate = new RestTemplate();
    }

    try {
      ResponseEntity<UaaTokenResponse> result =
          restTemplate.exchange(skrillTellerConfig.getUaaHostUrl(),
              HttpMethod.POST, entity, UaaTokenResponse.class);
      String accessToken = result.getBody().getAccessToken();

      businessUnitVaultConfig.setAccessToken(accessToken);
      return accessToken;
    } catch (RestClientException ex) {
      if (ex.getMostSpecificCause() instanceof HttpClientErrorException) {
        HttpClientErrorException http = (HttpClientErrorException) ex.getMostSpecificCause();
        if (HttpStatus.UNAUTHORIZED.equals(http.getStatusCode())) {
          LOG.error("Authentication failed: unauthorized user", ex);
          throw InternalErrorException.builder().internalError().cause(ex)
              .details("Authentication failed: unauthorized user").build();
        }
      }
      LOG.error("Authentication failed: technical error", ex);
      throw InternalErrorException.builder().internalError().cause(ex)
          .details("Problem creating access token for UAA").build();
    }
  }

  private BusinessUnitVaultConfig getClientConfigs(String brand) {
    Map<String, BusinessUnitVaultConfig> uaa = skrillTellerVaultConfig.getUaa();
    if (uaa == null) {
      throw InternalErrorException.builder().internalError()
          .details("There are no brands configurations provided.").build();
    }
    return uaa.get(brand.toLowerCase());
  }
}

