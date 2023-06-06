// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.assembler;

import com.paysafe.upf.user.provisioning.domain.UserToken;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;

import org.joda.time.DateTime;

import java.util.UUID;

public final class TokenAssembler {

  private TokenAssembler() {
    //Intentionally empty
  }

  /**
   * Converts UserToken to TokenResponseResource.
   */
  public static TokenResponseResource toUserTokenResponseResource(UserToken userToken) {
    TokenResponseResource tokenResponseResource = new TokenResponseResource();
    tokenResponseResource.setId(userToken.getToken());
    tokenResponseResource.setCreationDate(userToken.getCreatedDate());
    tokenResponseResource.setValidUntil(userToken.getExpiryDate());
    return tokenResponseResource;
  }

  /**
   * Converts to UserToken from TokenRequestResource and IdentityManagementUserResource.
   */
  public static UserToken toUserToken(TokenRequestResource requestResource,
      IdentityManagementUserResource userResource) {
    UserToken userToken = new UserToken();
    userToken.setToken(UUID.randomUUID().toString());
    userToken.setLoginName(userResource.getUserName());
    userToken.setTokenType(requestResource.getTokenType().name());
    userToken.setExpiryDate(DateTime.now().plus(requestResource.getTimeToLiveInSeconds() * 1000));
    userToken.setCreatedBy(userResource.getUserName());
    userToken.setCreatedDate(DateTime.now());
    userToken.setLastModifiedBy(userResource.getUserName());
    userToken.setLastModifiedDate(DateTime.now());
    userToken.setUserId(userResource.getId());
    return userToken;
  }

}
