// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.upf.user.provisioning.enums.TokenType;
import com.paysafe.upf.user.provisioning.service.TokenService;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateTokenResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/user-provisioning/v1/")
public class TokenController {

  @Autowired
  private TokenService tokenService;

  /**
   * API to create token.
   *
   * @return TokenResponseResource response
   */
  @RequestMapping(method = RequestMethod.POST, value = "users/{userId}/tokens",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Create a token for Okta user")
  public ResponseEntity<TokenResponseResource> createToken(@ApiParam(value = "Okta user token details",
      required = true) @Valid @RequestBody TokenRequestResource requestResource, @PathVariable String userId) {
    TokenResponseResource tokenResponseResource = tokenService.createToken(requestResource, userId, null);
    return new ResponseEntity<>(tokenResponseResource, HttpStatus.CREATED);
  }

  /**
   * API to get token.
   *
   * @param userId userId
   * @param tokenId tokenId
   * @param tokenType tokenType
   * @return TokenResponseResource
   * @throws JsonProcessingException e
   */
  @RequestMapping(method = RequestMethod.GET, value = "users/{userId}/tokens/{tokenId}",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Get token for Okta user")
  public ResponseEntity<TokenResponseResource> getToken(@PathVariable String userId, @PathVariable String tokenId,
      @RequestParam(value = "tokenType") TokenType tokenType) throws JsonProcessingException {
    TokenResponseResource tokenResponseResource = tokenService.getToken(userId, tokenId, tokenType);
    return new ResponseEntity<>(tokenResponseResource, HttpStatus.OK);
  }

  /**
   * API to update token.
   *
   * @param updateTokenResource update token resource
   * @param uuid user's uuid
   * @param tokenId tokne id
   * @return TokenResponseResource
   */
  @RequestMapping(method = RequestMethod.PATCH, value = "users/{uuid}/tokens/{tokenId}",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Patches token for okta user")
  public ResponseEntity<TokenResponseResource> updateToken(
      @ApiParam(value = "update token request resource",
          required = true) @Valid @RequestBody UpdateTokenResource updateTokenResource,
      @PathVariable String uuid, @PathVariable String tokenId) {
    TokenResponseResource tokenResponseResource = tokenService.updateToken(updateTokenResource, uuid, tokenId);
    return new ResponseEntity<>(tokenResponseResource, HttpStatus.OK);
  }

}
