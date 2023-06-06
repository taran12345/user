// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.upf.user.provisioning.domain.UserToken;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.repository.TokenRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.service.MailService;
import com.paysafe.upf.user.provisioning.service.TokenService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.resource.AdminShellResetPasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetEmailStatusResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetPasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class AdminShellServiceImplTest {

  @Mock
  private UserService userService;

  @Mock
  private TokenService tokenService;

  @Mock
  private MailService mailService;

  @Mock
  private TokenRepository tokenRepository;

  @Mock
  IdentityManagementFeignClient identityManagementFeignClient;

  @Mock
  private AuditService auditService;

  @InjectMocks
  private AdminShellServiceImpl adminShellServiceImpl;

  /**
   * Data initialization.
   */
  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void triggerResetEmail_selfContact_success() {
    AdminShellResetPasswordRequestResource requestResource = new AdminShellResetPasswordRequestResource();
    requestResource.setLoginName("DUMMY_LOGINNAME");
    when(identityManagementFeignClient.getUser(any(), any())).thenReturn(getIdmUserResponse());
    when(tokenService.createToken(any(), any(), any())).thenReturn(getTokenResponseResource());
    doNothing().when(mailService).sendResetPasswordEmail(any(), any(), any(), any(), any(), any());
    adminShellServiceImpl.triggerResetEmail(requestResource, "SKRILL");
    verify(mailService, times(1)).sendResetPasswordEmail(any(), any(), any(), any(), any(), any());
  }

  @Test(expected = BadRequestException.class)
  public void triggerResetEmail_selfContact_failure_noLoginName() {
    AdminShellResetPasswordRequestResource requestResource = new AdminShellResetPasswordRequestResource();
    requestResource.setLoginName("");
    when(identityManagementFeignClient.getUser(any(), any())).thenReturn(null);
    adminShellServiceImpl.triggerResetEmail(requestResource, "SKRILL");
  }

  @Test(expected = BadRequestException.class)
  public void triggerResetEmail_selfContact_failure_inActiveUser() {
    AdminShellResetPasswordRequestResource requestResource = new AdminShellResetPasswordRequestResource();
    requestResource.setLoginName("DUMMY_LOGINNAME");
    ResponseEntity<IdentityManagementUserResource> userResponse = getIdmUserResponse();
    userResponse.getBody().setStatus(UserStatus.BLOCKED);
    when(identityManagementFeignClient.getUser(any(), any())).thenReturn(userResponse);
    adminShellServiceImpl.triggerResetEmail(requestResource, "SKRILL");
  }

  @Test(expected = InternalErrorException.class)
  public void triggerResetEmail_selfContact_failure_nullResponseFromIdm() {
    AdminShellResetPasswordRequestResource requestResource = new AdminShellResetPasswordRequestResource();
    requestResource.setLoginName("DUMMY_LOGINNAME");
    when(identityManagementFeignClient.getUser(any(), any())).thenReturn(null);
    adminShellServiceImpl.triggerResetEmail(requestResource, "SKRILL");
  }

  @Test(expected = NotFoundException.class)
  public void triggerResetEmail_selfContact_failure_dummyEmail() {
    AdminShellResetPasswordRequestResource requestResource = new AdminShellResetPasswordRequestResource();
    requestResource.setLoginName("DUMMY_LOGINNAME");
    ResponseEntity<IdentityManagementUserResource> userResponse = getIdmUserResponse();
    userResponse.getBody().setEmail("dummy@dummy.com");
    when(identityManagementFeignClient.getUser(any(), any())).thenReturn(userResponse);
    adminShellServiceImpl.triggerResetEmail(requestResource, "SKRILL");
  }

  @Test
  public void triggerResetEmail_authContact_success() {
    AdminShellResetPasswordRequestResource requestResource = new AdminShellResetPasswordRequestResource();
    requestResource.setLoginName("DUMMY_LOGINNAME");
    requestResource.setAuthContactEmailId("dummy@dummy.com");
    ResponseEntity<IdentityManagementUserResource> responseEntity = getIdmUserResponse();
    responseEntity.getBody().setEmail("dummy@dummy.com");
    when(identityManagementFeignClient.getUser(any(), any())).thenReturn(responseEntity);
    when(tokenService.createToken(any(), any(), any())).thenReturn(getTokenResponseResource());
    doNothing().when(mailService).sendResetPasswordEmail(any(), any(), any(), any(), any(), any());
    adminShellServiceImpl.triggerResetEmail(requestResource, "SKRILL");
    verify(mailService, times(1)).sendResetPasswordEmail(any(), any(), any(), any(), any(), any());
  }

  @Test(expected = BadRequestException.class)
  public void triggerResetEmail_authContact_failure_nondummy_email() {
    AdminShellResetPasswordRequestResource requestResource = new AdminShellResetPasswordRequestResource();
    requestResource.setLoginName("DUMMY_LOGINNAME");
    requestResource.setAuthContactEmailId("dummy@dummy.com");
    when(identityManagementFeignClient.getUser(any(), any())).thenReturn(getIdmUserResponse());
    when(tokenService.createToken(any(), any(), any())).thenReturn(getTokenResponseResource());
    doNothing().when(mailService).sendResetPasswordEmail(any(), any(), any(), any(), any(), any());
    adminShellServiceImpl.triggerResetEmail(requestResource, "SKRILL");
    verify(mailService, times(1)).sendResetPasswordEmail(any(), any(), any(), any(), any(), any());
  }

  @Test
  public void resetPassword_success() throws JsonProcessingException {
    ResetPasswordRequestResource resetPasswordRequestResource = new ResetPasswordRequestResource();
    resetPasswordRequestResource.setNewPassword("NEW_PASSWORD");
    resetPasswordRequestResource.setValidationToken(UUID.randomUUID().toString());
    when(tokenService.getToken(any(), any(), any())).thenReturn(getTokenResponseResource());
    when(userService.resetPassword(any(), any(), any())).thenReturn(UUID.randomUUID().toString());
    when(identityManagementFeignClient.getUser(any(), any())).thenReturn(getIdmUserResponse());
    when(identityManagementFeignClient.internalLogout(any())).thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
    adminShellServiceImpl.resetPassword("sampleuseId", resetPasswordRequestResource, "SKRILL");
    verify(identityManagementFeignClient, times(1)).getUser(any(), any());
  }

  @Test
  public void getResetEmailStatus_success() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    when(tokenRepository.findByLoginNameAndTokenTypeAndApplication(any(), any(), any())).thenReturn(getUserTokens());
    ResetEmailStatusResponseResource statusResponseResource = adminShellServiceImpl.getResetEmailStatus("sampleuseId");
    assertEquals("NOT_PENDING", statusResponseResource.getStatus());
    CommonThreadLocal.unsetAuthLocal();
  }

  private List<UserToken> getUserTokens() {
    UserToken userToken1 = new UserToken();
    userToken1.setAuthContactEmail(null);
    userToken1.setAuthContactName(null);
    userToken1.setCreatedBy(null);
    userToken1.setExpiryDate(DateTime.now().minusDays(89));
    List<UserToken> userTokens = new ArrayList<>();
    userTokens.add(userToken1);
    return userTokens;
  }

  private TokenResponseResource getTokenResponseResource() {
    TokenResponseResource tokenResponseResource = new TokenResponseResource();
    tokenResponseResource.setValidUntil(DateTime.now().plusDays(7));
    tokenResponseResource.setCreationDate(DateTime.now());
    tokenResponseResource.setId(UUID.randomUUID().toString());
    return tokenResponseResource;
  }

  private ResponseEntity<IdentityManagementUserResource> getIdmUserResponse() {
    IdentityManagementUserResource userResource = new IdentityManagementUserResource();
    userResource.setStatus(UserStatus.ACTIVE);
    userResource.setEmail("notdummy@notdummy.com");
    return new ResponseEntity<IdentityManagementUserResource>(userResource, HttpStatus.OK);
  }

}
