// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.op.errorhandling.exceptions.UnauthorizedException;
import com.paysafe.upf.user.provisioning.domain.UserToken;
import com.paysafe.upf.user.provisioning.enums.TokenType;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.repository.TokenRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.impl.TokenServiceImpl;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateTokenResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class TokenServiceImplTest {

  @InjectMocks
  private TokenServiceImpl tokenService;

  @Mock
  private UserService userService;

  @Mock
  private TokenRepository tokenRepository;

  @Mock
  private AuditService auditService;

  private static final String TOKEN = "sampleToken";
  private static final String USER_ID = "userId";
  private static final String USER_NAME = "userName";
  private static final String UUID = "sample-uuid";

  /**
   * Test cases initialization.
   */
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCreateTokenNoPreviousActiveTokens() {
    List<UserToken> tokenList = new ArrayList<>();
    IdentityManagementUserResource oktaUser = getOktaUser();
    when(userService.fetchUser(any())).thenReturn(oktaUser);
    when(tokenRepository.findByLoginNameAndTokenTypeAndApplication(anyString(), anyString(), anyString()))
        .thenReturn(tokenList);

    when(tokenRepository.save(any(UserToken.class))).thenReturn(getUserToken());

    TokenRequestResource tokenRequestResource = getTokenRequestResource();

    tokenService.createToken(tokenRequestResource, USER_ID, null);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenTypeAndApplication(anyString(), anyString(), anyString());
    verify(tokenRepository, times(1)).save(any(UserToken.class));
  }

  @Test
  public void testCreateTokenWithPreviousInactiveToken() {
    List<UserToken> tokenList = new ArrayList<>();
    UserToken userToken = getUserToken();
    userToken.setExpiryDate(DateTime.now());
    tokenList.add(userToken);
    when(userService.fetchUser(any())).thenReturn(getOktaUser());
    when(tokenRepository.findByLoginNameAndTokenTypeAndApplication(anyString(), anyString(), anyString()))
        .thenReturn(tokenList);

    when(tokenRepository.save(any(UserToken.class))).thenReturn(getUserToken());

    TokenRequestResource tokenRequestResource = getTokenRequestResource();

    tokenService.createToken(tokenRequestResource, USER_ID, null);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenTypeAndApplication(anyString(), anyString(), anyString());
    verify(tokenRepository, times(1)).save(any(UserToken.class));
  }

  @Test
  public void testCreateTokenWithPreviousActiveToken() {
    List<UserToken> tokenList = new ArrayList<>();
    UserToken userToken = getUserToken();
    userToken.setExpiryDate(DateTime.now().plus(1000L));
    tokenList.add(userToken);
    when(userService.fetchUser(any())).thenReturn(getOktaUser());
    when(tokenRepository.findByLoginNameAndTokenTypeAndApplication(anyString(), anyString(), anyString()))
        .thenReturn(tokenList);

    when(tokenRepository.save(any(UserToken.class))).thenReturn(getUserToken());

    TokenRequestResource tokenRequestResource = getTokenRequestResource();

    tokenService.createToken(tokenRequestResource, USER_ID, null);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenTypeAndApplication(anyString(), anyString(), anyString());
    verify(tokenRepository, times(2)).save(any(UserToken.class));
  }

  @Test(expected = NotFoundException.class)
  public void testCreateTokenFailInUserRepository() {
    List<UserToken> tokenList = new ArrayList<>();
    when(userService.fetchUser(any())).thenReturn(null);
    when(tokenRepository.findByLoginNameAndTokenTypeAndApplication(anyString(), anyString(), anyString()))
        .thenReturn(tokenList);

    when(tokenRepository.save(any(UserToken.class))).thenReturn(getUserToken());

    TokenRequestResource tokenRequestResource = getTokenRequestResource();
    tokenService.createToken(tokenRequestResource, USER_ID, null);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(),
        anyString(), anyString());
    verify(tokenRepository, times(1)).save(any(UserToken.class));
  }

  @Test
  public void testGetToken() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    when(userService.fetchUser(any())).thenReturn(getOktaUser());

    when(tokenRepository.findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(), anyString(),
        anyString())).thenReturn(getUserToken());
    tokenService.getToken(USER_ID, TOKEN, TokenType.PASSWORD_RECOVERY);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(),
        anyString(), anyString());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = NotFoundException.class)
  public void testGetTokenFailInUserRepository() throws JsonProcessingException {
    when(userService.fetchUser(any())).thenReturn(null);

    when(tokenRepository.findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(), anyString(),
        anyString())).thenReturn(getUserToken());
    tokenService.getToken(USER_ID, TOKEN, TokenType.PASSWORD_RECOVERY);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(),
        anyString(), anyString());
  }

  @Test(expected = NotFoundException.class)
  public void testGetTokenFailInUserTokenRepository() throws JsonProcessingException {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    when(userService.fetchUser(any())).thenReturn(getOktaUser());

    when(tokenRepository.findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(), anyString(),
        anyString())).thenReturn(null);
    tokenService.getToken(USER_ID, TOKEN, TokenType.PASSWORD_RECOVERY);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(),
        anyString(), anyString());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = UnauthorizedException.class)
  public void testGetTokenFailInTokenExpiry() throws JsonProcessingException {
    when(userService.fetchUser(any())).thenReturn(getOktaUser());
    UserToken userToken = getUserToken();
    userToken.setExpiryDate(DateTime.now().minus(1L));
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    when(tokenRepository.findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(), anyString(),
        anyString())).thenReturn(userToken);
    tokenService.getToken(USER_ID, TOKEN, TokenType.PASSWORD_RECOVERY);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(),
        anyString(), anyString());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = UnauthorizedException.class)
  public void getToken_whenTokenExpiredAndProvisionedStatus_shouldThrowException() throws JsonProcessingException {
    IdentityManagementUserResource userResource = getOktaUser();
    userResource.setStatus(UserStatus.PROVISIONED);
    when(userService.fetchUser(any())).thenReturn(userResource);
    UserToken userToken = getUserToken();
    userToken.setExpiryDate(DateTime.now().minus(1L));
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    when(tokenRepository.findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(), anyString(),
        anyString())).thenReturn(userToken);
    tokenService.getToken(USER_ID, TOKEN, TokenType.PASSWORD_RECOVERY);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(),
        anyString(), anyString());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test(expected = NotFoundException.class)
  public void testUpdateTokenWhenUserNotFound() {
    when(userService.fetchUser(any())).thenReturn(null);

    UserToken userToken = getUserToken();
    userToken.setExpiryDate(DateTime.now().plus(120L));

    when(tokenRepository.findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(), anyString(),
        anyString())).thenReturn(userToken);

    tokenService.updateToken(getUpdateTokenResource(), UUID, TOKEN);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(),
        anyString(), anyString());
    verify(tokenRepository, times(0)).save(any());
  }

  @Test(expected = NotFoundException.class)
  public void testUpdateTokenWhenTokenNotFound() {
    when(userService.fetchUser(any())).thenReturn(getOktaUser());

    UserToken userToken = getUserToken();
    userToken.setExpiryDate(DateTime.now().plus(120L));

    when(tokenRepository.findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(), anyString(),
        anyString())).thenReturn(null);

    tokenService.updateToken(getUpdateTokenResource(), UUID, TOKEN);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(),
        anyString(), anyString());
    verify(tokenRepository, times(0)).save(any());
  }

  @Test(expected = UnauthorizedException.class)
  public void testUpdateTokenWhenTokenIsExpired() {
    when(userService.fetchUser(any())).thenReturn(getOktaUser());

    UserToken userToken = getUserToken();
    userToken.setExpiryDate(DateTime.now().minus(60L));

    when(tokenRepository.findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(), anyString(),
        anyString())).thenReturn(userToken);

    tokenService.updateToken(getUpdateTokenResource(), UUID, TOKEN);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(),
        anyString(), anyString());
    verify(tokenRepository, times(0)).save(any());
  }

  @Test
  public void testUpdateTokenWhenTokenIsNotExpired() {
    when(userService.fetchUser(any())).thenReturn(getOktaUser());

    UserToken userToken = getUserToken();
    userToken.setExpiryDate(DateTime.now().plus(120L));

    when(tokenRepository.findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(), anyString(),
        anyString())).thenReturn(userToken);
    when(tokenRepository.save(any())).thenReturn(userToken);

    tokenService.updateToken(getUpdateTokenResource(), UUID, TOKEN);

    verify(userService, times(1)).fetchUser(anyString());
    verify(tokenRepository, times(1)).findByLoginNameAndTokenAndTokenTypeAndApplication(anyString(), anyString(),
        anyString(), anyString());
    verify(tokenRepository, times(1)).save(any());
  }

  private IdentityManagementUserResource getOktaUser() {
    IdentityManagementUserResource oktaUser = new IdentityManagementUserResource();
    oktaUser.setId(USER_ID);
    oktaUser.setUserName(USER_NAME);
    oktaUser.setStatus(UserStatus.ACTIVE);
    return oktaUser;
  }

  private UserToken getUserToken() {
    return new UserToken(USER_NAME, TOKEN, "PASSWORD_RECOVERY");
  }

  private TokenRequestResource getTokenRequestResource() {
    TokenRequestResource tokenRequestResource = new TokenRequestResource();
    tokenRequestResource.setTimeToLiveInSeconds(3000L);
    tokenRequestResource.setTokenType(TokenType.PASSWORD_RECOVERY);
    return tokenRequestResource;
  }

  private UpdateTokenResource getUpdateTokenResource() {
    UpdateTokenResource updateTokenResource = new UpdateTokenResource();
    updateTokenResource.setTimeToLiveInSeconds(60L);
    updateTokenResource.setTokenType(TokenType.PASSWORD_RECOVERY);
    return updateTokenResource;
  }

}
