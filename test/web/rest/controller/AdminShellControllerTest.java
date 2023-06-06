// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paysafe.upf.user.provisioning.enums.EmailType;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AdminShellService;
import com.paysafe.upf.user.provisioning.util.JsonUtil;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.resource.AdminShellResetPasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetEmailStatusResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetPasswordRequestResource;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class AdminShellControllerTest {

  private static final String TRIGGER_RESET_EMAIL_PATH = "/admin/user-provisioning/v1/adminshell/resetEmail";
  private static final String RESET_PASSWORD_PATH =
      "/admin/user-provisioning/v1/adminshell/users/{userId}/resetPassword";
  private static final String RESET_EMAIL_STATUS_PATH =
      "/admin/user-provisioning/v1/adminshell/users/{userId}/resetEmailStatus";

  private MockMvc mockMvc;

  @InjectMocks
  @Spy
  private AdminShellController adminShellController;

  @Mock
  private AdminShellService adminShellService;

  /**
   * Initial setup.
   */
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    final StaticApplicationContext applicationContext = new StaticApplicationContext();
//    applicationContext.registerSingleton("exceptionHandler", OneplatformDefaultControllerAdvice.class);

    final WebMvcConfigurationSupport webMvcConfigurationSupport = new WebMvcConfigurationSupport();
    webMvcConfigurationSupport.setApplicationContext(applicationContext);

    mockMvc = MockMvcBuilders.standaloneSetup(this.adminShellController).build();
  }

  @Test
  public void triggerResetEmail_whenApplicationIsNull_shouldThrowException() throws Exception {
    AdminShellResetPasswordRequestResource adminShellResetPasswordRequestResource =
        getAdminShellResetPasswordRequestResource();
    final String requestObjectContent = JsonUtil.toJsonString(adminShellResetPasswordRequestResource);
    doNothing().when(adminShellService).triggerResetEmail(any(), any());
    this.mockMvc.perform(MockMvcRequestBuilders.post(TRIGGER_RESET_EMAIL_PATH).content(requestObjectContent)
        .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(status().isBadRequest());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void triggerResetEmail_withApplicationHeader_shouldSucceed() throws Exception {
    AdminShellResetPasswordRequestResource adminShellResetPasswordRequestResource =
        getAdminShellResetPasswordRequestResource();
    adminShellResetPasswordRequestResource.setAuthContactEmailId("");
    final String requestObjectContent = JsonUtil.toJsonString(adminShellResetPasswordRequestResource);
    doNothing().when(adminShellService).triggerResetEmail(any(), any());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post(TRIGGER_RESET_EMAIL_PATH).content(requestObjectContent)
            .contentType(MediaType.APPLICATION_JSON_UTF8).header("APPLICATION", DataConstants.SKRILL))
        .andExpect(status().isOk());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testResetPassword() throws Exception {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    ResetPasswordRequestResource resetPasswordRequestResource = getResetPasswordRequestResource();
    final String requestObjectContent = JsonUtil.toJsonString(resetPasswordRequestResource);
    doNothing().when(adminShellService).resetPassword(any(), any(), any());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post(RESET_PASSWORD_PATH, "DUMMY_LOGINNAME").content(requestObjectContent)
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void resetPassword_whenApplicationIsNull_shouldThrowException() throws Exception {
    ResetPasswordRequestResource resetPasswordRequestResource = getResetPasswordRequestResource();
    final String requestObjectContent = JsonUtil.toJsonString(resetPasswordRequestResource);
    doNothing().when(adminShellService).resetPassword(any(), any(), any());
    this.mockMvc
        .perform(MockMvcRequestBuilders.post(RESET_PASSWORD_PATH, "DUMMY_LOGINNAME").content(requestObjectContent)
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().isBadRequest());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void resetPassword_withApplicationHeader_shouldSucceed() throws Exception {
    ResetPasswordRequestResource resetPasswordRequestResource = getResetPasswordRequestResource();
    final String requestObjectContent = JsonUtil.toJsonString(resetPasswordRequestResource);
    doNothing().when(adminShellService).resetPassword(any(), any(), any());
    this.mockMvc.perform(MockMvcRequestBuilders.post(RESET_PASSWORD_PATH, "DUMMY_LOGINNAME")
        .content(requestObjectContent).contentType(MediaType.APPLICATION_JSON_UTF8)
        .accept(MediaType.APPLICATION_JSON_UTF8).header("APPLICATION", DataConstants.SKRILL))
        .andExpect(status().isOk());
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testGetResetEmailStatus() throws Exception {
    ResetEmailStatusResponseResource resetEmailStatusResponseResource = getResetEmailStatusResponseResource();
    when(adminShellService.getResetEmailStatus(any())).thenReturn(resetEmailStatusResponseResource);
    this.mockMvc
        .perform(MockMvcRequestBuilders.get(RESET_EMAIL_STATUS_PATH, "DUMMY_LOGINNAME")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    CommonThreadLocal.unsetAuthLocal();
  }

  private ResetEmailStatusResponseResource getResetEmailStatusResponseResource() {
    ResetEmailStatusResponseResource resetEmailStatusResponseResource = new ResetEmailStatusResponseResource();
    resetEmailStatusResponseResource.setEmailType(EmailType.AUTH_CONTACT_EMAIL);
    resetEmailStatusResponseResource.setRecipient("DUMMY_RECIPIENT");
    resetEmailStatusResponseResource.setSentTime(DateTime.now());
    resetEmailStatusResponseResource.setStatus("PENDING");
    return resetEmailStatusResponseResource;
  }

  private ResetPasswordRequestResource getResetPasswordRequestResource() {
    ResetPasswordRequestResource requestResource = new ResetPasswordRequestResource();
    requestResource.setNewPassword("NEW_PASSWORD");
    requestResource.setValidationToken(UUID.randomUUID().toString());
    return requestResource;
  }

  private AdminShellResetPasswordRequestResource getAdminShellResetPasswordRequestResource() {
    AdminShellResetPasswordRequestResource requestResource = new AdminShellResetPasswordRequestResource();
    requestResource.setAuthContactEmailId("DUMMY_EMAIL");
    requestResource.setLoginName("DUMMY_LOGINNAME");
    return requestResource;
  }
}
