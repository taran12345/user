// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients.fallbacks;

import com.paysafe.op.errorhandling.exceptions.ExternalGatewayErrorException;
import com.paysafe.upf.user.provisioning.feignclients.UaaFeignClient;

import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

public class UaaFeignClientFallback implements UaaFeignClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(UaaFeignClientFallback.class);
  private static final String LOG_MESSAGE = "paysafe-ss-uaa  service circuit is open, falling back to failover";

  @Override
  public ResponseEntity<JsonNode> authenticateUser_JsonNodeResponse(String contentType, String authProviderType,
      String auth, String username, String password, String grantType) {
    LOGGER.warn(LOG_MESSAGE);
    throw ExternalGatewayErrorException.builder().build();
  }

}
