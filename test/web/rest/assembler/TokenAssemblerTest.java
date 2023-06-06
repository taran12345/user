// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.assembler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.paysafe.upf.user.provisioning.domain.UserToken;
import com.paysafe.upf.user.provisioning.enums.TokenType;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;

import org.hamcrest.core.Is;
import org.joda.time.DateTime;
import org.junit.Test;

public class TokenAssemblerTest {

  @Test
  public void testConvertingToUserTokenResponseResource() {
    UserToken userToken = new UserToken();
    userToken.setLoginName("testuser");
    userToken.setToken("sample-token");
    userToken.setCreatedDate(DateTime.now().minus(10000));
    userToken.setExpiryDate(DateTime.now().plus(60000));

    TokenResponseResource outputTokenResponseResource = TokenAssembler.toUserTokenResponseResource(userToken);

    assertThat(outputTokenResponseResource.getId(), Is.is("sample-token"));
    assertTrue(outputTokenResponseResource.getCreationDate().isBeforeNow());
    assertTrue(outputTokenResponseResource.getValidUntil().isAfterNow());
  }

  @Test
  public void testConvertingToUserToken() {
    TokenRequestResource tokenRequestResource = new TokenRequestResource();
    tokenRequestResource.setTimeToLiveInSeconds(60L);
    tokenRequestResource.setTokenType(TokenType.PASSWORD_RECOVERY);

    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setUserName("sampleUser");

    UserToken outputUserToken = TokenAssembler.toUserToken(tokenRequestResource, userResource);

    assertNotNull(outputUserToken.getToken());
    assertThat(outputUserToken.getLoginName(), Is.is("sampleUser"));
    assertTrue(outputUserToken.getTokenType().equals(TokenType.PASSWORD_RECOVERY.name()));
    assertTrue(outputUserToken.getExpiryDate().isAfterNow());
    assertThat(outputUserToken.getCreatedBy(), Is.is("sampleUser"));
  }

}
