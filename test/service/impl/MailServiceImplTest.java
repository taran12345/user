// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.upf.user.provisioning.config.UserConfig;
import com.paysafe.upf.user.provisioning.config.UserGcsEventsConfig;
import com.paysafe.upf.user.provisioning.config.UserProvisioningConfig;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.enums.EmailType;
import com.paysafe.upf.user.provisioning.enums.TokenType;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.GcsService;
import com.paysafe.upf.user.provisioning.service.SkrillTellerAccountInfoService;
import com.paysafe.upf.user.provisioning.service.TokenService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.utils.MailUtil;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MailServiceImplTest {

  private static final String TEST_EMAIL = "abc@gmail.com";

  @Mock
  private UserProvisioningConfig userProvisioningConfig;

  @Mock
  private User user;

  @Mock
  private TokenService tokenService;

  @Mock
  private Environment env;

  @Mock
  private GcsService gcsService;

  @Mock
  private MailUtil mailUtil;

  @Mock
  private SkrillTellerAccountInfoService skrillTellerAccountInfoService;

  @InjectMocks
  private MailServiceImpl mailServiceImpl;

  private UserProvisioningUserResource userResponse;
  private TokenResponseResource tokenResponseResource;

  /**
   * Data initialization.
   */
  @Before
  public void setUp() throws Exception {
    userResponse = UserTestUtility.getUserProvisioningUserResource();
    tokenResponseResource = UserTestUtility.getTokenResponseResource();
  }

  private UserConfig getUserConfig() {
    UserConfig userConfig = new UserConfig();
    userConfig.setRegistationTokenTimeToLiveSeconds(60 * 60 * 24 * 7L);
    Map<String, String> skrillEventIds = new HashMap<>();
    skrillEventIds.put(DataConstants.SKRILL_BUSINESS_CONTACT_EMAIL, "123");
    UserGcsEventsConfig userGcsEventsConfig = new UserGcsEventsConfig();
    userGcsEventsConfig.setSkrill(skrillEventIds);
    userConfig.setGcsEvents(userGcsEventsConfig);
    return userConfig;
  }

  // sendRegistrationConfirmationEmail() testCases

  @Test
  public void sendRegistrationConfirmationEmail_withPendingUserStatus_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(mailUtil.getEventId(any(), any(), any())).thenReturn("1234");
    when(mailUtil.getLink(any(), any())).thenReturn("www.portal.com");
    doNothing().when(mailUtil).setRegistrationTemplateVariables(any(), any(), any());
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    when(tokenService.createToken(any(), any(), any())).thenReturn(tokenResponseResource);
    userResponse.setStatus(UserStatus.PENDING_USER_ACTION);
    mailServiceImpl.sendRegistrationConfirmationEmail(userResponse);
    verify(gcsService, times(1)).sendNotification(any(), any(), any());
    verify(tokenService, times(1)).createToken(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void sendRegistrationConfirmationEmail_withProvisionedUserStatus_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(mailUtil.getEventId(any(), any(), any())).thenReturn("1234");
    when(mailUtil.getLink(any(), any())).thenReturn("www.portal.com");
    doNothing().when(mailUtil).setRegistrationTemplateVariables(any(), any(), any());
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    when(tokenService.createToken(any(), any(), any())).thenReturn(tokenResponseResource);
    userResponse.setStatus(UserStatus.PROVISIONED);
    mailServiceImpl.sendRegistrationConfirmationEmail(userResponse);
    verify(gcsService, times(1)).sendNotification(any(), any(), any());
    verify(tokenService, times(1)).createToken(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void sendRegistrationConfirmationEmail_withInvalidUserStatus_shouldNotInvokeGcsService() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(mailUtil.getEventId(any(), any(), any())).thenReturn("1234");
    when(mailUtil.getLink(any(), any())).thenReturn("www.portal.com");
    doNothing().when(mailUtil).setRegistrationTemplateVariables(any(), any(), any());
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    when(tokenService.createToken(any(), any(), any())).thenReturn(tokenResponseResource);
    userResponse.setStatus(UserStatus.ACTIVE);
    mailServiceImpl.sendRegistrationConfirmationEmail(userResponse);
    verify(gcsService, times(0)).sendNotification(any(), any(), any());
    verify(tokenService, times(0)).createToken(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void sendRegistrationConfirmationEmail_withSkrillApplication_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    when(userProvisioningConfig.getUser()).thenReturn(getUserConfig());
    when(mailUtil.getEventId(any(), any(), any())).thenReturn("1234");
    when(mailUtil.getLink(any(), any())).thenReturn("www.portal.com");
    doNothing().when(mailUtil).setRegistrationTemplateVariables(any(), any(), any());
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    when(tokenService.createToken(any(), any(), any())).thenReturn(tokenResponseResource);
    userResponse.setStatus(UserStatus.PENDING_USER_ACTION);
    userResponse.setAccessResources(UserTestUtility.getAccessResourcesList());
    when(skrillTellerAccountInfoService.getSkrillContactEmails(any()))
        .thenReturn(UserTestUtility.getSkrillContactEmails());
    mailServiceImpl.sendRegistrationConfirmationEmail(userResponse);
    verify(gcsService, times(2)).sendNotification(any(), any(), any());
    verify(tokenService, times(1)).createToken(any(), any(), any());
    verify(skrillTellerAccountInfoService, times(1)).getSkrillContactEmails(any());
    CommonThreadLocal.unsetAuthLocal();
  }

  // sendPermissionsUpdatedConfirmationEmail() testCases

  @Test
  public void sendPermissionsUpdatedConfirmationEmail_withValidData_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
    when(mailUtil.getEventId(any(), any(), any())).thenReturn("1234");
    when(mailUtil.getLink(any(), any())).thenReturn("www.portal.com");
    doNothing().when(mailUtil).setRegistrationTemplateVariables(any(), any(), any());
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    mailServiceImpl.sendPermissionsUpdatedConfirmationEmail(userResponse);
    verify(gcsService, times(1)).sendNotification(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void sendPermissionsUpdatedConfirmationEmail_withPartnerPortalApplication_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PARTNER_PORTAL));
    when(mailUtil.getEventId(any(), any(), any())).thenReturn("1234");
    when(mailUtil.getLink(any(), any())).thenReturn("www.portal.com");
    doNothing().when(mailUtil).setRegistrationTemplateVariables(any(), any(), any());
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    mailServiceImpl.sendPermissionsUpdatedConfirmationEmail(userResponse);
    verify(gcsService, times(1)).sendNotification(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  // sendResetPasswordEmail() testCases

  @Test
  public void sendResetPasswordEmail_forSkrillApplicationAndSelfEmail_shouldSucceed() {
    mockResetPasswordEmailProperties();
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    IdentityManagementUserResource userResource = UserTestUtility.getIdentityManagementUserResource();
    userResource.setEmail(" test@xyz.com ");
    mailServiceImpl.sendResetPasswordEmail(userResource, tokenResponseResource, TokenType.PASSWORD_RECOVERY, TEST_EMAIL,
        DataConstants.SKRILL, EmailType.SELF_EMAIL);
    verify(gcsService, times(1)).sendNotification(any(), any(), any());
  }


  private void mockResetPasswordEmailProperties() {
    ReflectionTestUtils.setField(mailServiceImpl, "resetPasswordHostUiUrl", "test_reset_url");
    ReflectionTestUtils.setField(mailServiceImpl, "skrillResetPasswordHostUiUrl", "test_skrill_reset_url");
    ReflectionTestUtils.setField(mailServiceImpl, "netellerResetPasswordHostUiUrl", "test_neteller_reset_url");
    String testEventId = "12345667";
    ReflectionTestUtils.setField(mailServiceImpl, "selfEmailResetPasswordEventId", testEventId);
    ReflectionTestUtils.setField(mailServiceImpl, "authContactEmailResetPasswordEventId", testEventId);
    ReflectionTestUtils.setField(mailServiceImpl, "selfEmailResetPasswordSkrillEventId", testEventId);
    ReflectionTestUtils.setField(mailServiceImpl, "selfEmailResetPasswordNetellerEventId", testEventId);
  }

  @Test
  public void sendResetPasswordEmail_forNetellerApplicationAndSelfEmail_shouldSucceed() {
    mockResetPasswordEmailProperties();
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    IdentityManagementUserResource userResource = UserTestUtility.getIdentityManagementUserResource();
    userResource.setEmail(" test@xyz.com ");
    mailServiceImpl.sendResetPasswordEmail(userResource, tokenResponseResource, TokenType.PASSWORD_RECOVERY, TEST_EMAIL,
        DataConstants.NETELLER, EmailType.SELF_EMAIL);
    verify(gcsService, times(1)).sendNotification(any(), any(), any());
  }

  @Test
  public void sendMfaStatusUpdate_withProfileMfaEnable_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL_MFA));
    when(mailUtil.getEventIdForMfa(any())).thenReturn("1234");
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    mailServiceImpl.sendMfaStatusUpdate(UserTestUtility.getUserForMfaTestUser(), Boolean.TRUE);
    verify(gcsService, times(1)).sendNotification(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void sendMfaStatusUpdate_withProfileMfaDisable_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL_MFA));
    when(mailUtil.getEventIdForMfa(any())).thenReturn("1234");
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    mailServiceImpl.sendMfaStatusUpdate(UserTestUtility.getUserForMfaTestUser(), Boolean.FALSE);
    verify(gcsService, times(1)).sendNotification(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void sendMfaStatusUpdate_withAdminMfaEnable_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL_MFA));
    when(mailUtil.getEventIdForMfa(any())).thenReturn("1234");
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    mailServiceImpl.sendMfaStatusUpdate(UserTestUtility.getUserForMfa(), Boolean.TRUE);
    verify(gcsService, times(2)).sendNotification(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void sendMfaStatusUpdate_withAdminMfaDisable_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL_MFA));
    when(mailUtil.getEventIdForMfa(any())).thenReturn("1234");
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    mailServiceImpl.sendMfaStatusUpdate(UserTestUtility.getUserForMfa(), Boolean.FALSE);
    verify(gcsService, times(2)).sendNotification(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void sendResetMfaStatusEmail_withSubUser_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL_MFA));
    when(mailUtil.getEventIdForMfa(any())).thenReturn("1234");
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    mailServiceImpl.sendResetMfaStatusEmail(UserTestUtility.getUserForMfaTestUser());
    verify(gcsService, times(1)).sendNotification(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void sendResetMfaStatusEmail_withAdmin_shouldSucceed() {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL_MFA));
    when(mailUtil.getEventIdForMfa(any())).thenReturn("1234");
    doNothing().when(gcsService).sendNotification(any(), any(), any());
    mailServiceImpl.sendResetMfaStatusEmail(UserTestUtility.getUserForMfa());
    verify(gcsService, times(2)).sendNotification(any(), any(), any());
    CommonThreadLocal.unsetAuthLocal();
  }

}
