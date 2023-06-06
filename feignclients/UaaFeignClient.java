// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients;

import com.paysafe.upf.user.provisioning.config.FeignClientConfig;
import com.paysafe.upf.user.provisioning.feignclients.fallbacks.UaaFeignClientFallback;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "paysafe-ss-uaa", configuration = FeignClientConfig.class, fallback = UaaFeignClientFallback.class)
public interface UaaFeignClient {

  @PostMapping(value = "/uaa/oauth/token")
  ResponseEntity<JsonNode> authenticateUser_JsonNodeResponse(@RequestHeader("Content-Type") String contentType,
      @RequestHeader("AuthenticationProvider-Type") String authProviderType,
      @RequestHeader("Authorization") String auth, @RequestParam("username") String username,
      @RequestParam("password") String password, @RequestParam("grant_type") String grantType);

}
