// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.enums.TokenType;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateTokenResource;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface TokenService {

  TokenResponseResource createToken(TokenRequestResource requestResource, String userId,
      String authContactEmail);

  TokenResponseResource getToken(String userId, String tokenId, TokenType tokenType) throws JsonProcessingException;

  TokenResponseResource updateToken(UpdateTokenResource updateTokenResource, String uuid, String tokenId);

  TokenResponseResource expireToken(UpdateTokenResource updateTokenResource,
      IdentityManagementUserResource userResource, String tokenId);

}
