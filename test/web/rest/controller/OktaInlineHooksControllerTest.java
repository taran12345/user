// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paysafe.op.errorhandling.web.handlers.OneplatformDefaultControllerAdvice;
import com.paysafe.upf.user.provisioning.migration.service.SkrillTellerUserService;
import com.paysafe.upf.user.provisioning.util.JsonUtil;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.resource.inlinehooks.PasswordImportResponseResource;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class OktaInlineHooksControllerTest {

  private MockMvc mockMvc;

  @Mock
  private SkrillTellerUserService skrillTellerUserService;

  @InjectMocks
  @Spy
  private OktaInlineHooksController oktaInlineHooksController;

  /**
   * Setup test configuration.
   *
   * @throws Exception exception
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    final StaticApplicationContext applicationContext = new StaticApplicationContext();
    applicationContext.registerSingleton("exceptionHandler", OneplatformDefaultControllerAdvice.class);

    final WebMvcConfigurationSupport webMvcConfigurationSupport = new WebMvcConfigurationSupport();
    webMvcConfigurationSupport.setApplicationContext(applicationContext);

    mockMvc = MockMvcBuilders.standaloneSetup(this.oktaInlineHooksController).build();
  }

  @Test
  public void validateUserSuccess() throws Exception {

    final String jsonRequestObject = JsonUtil.toJsonString(UserTestUtility.getPasswordImportRequestResourceForTest());
    when(skrillTellerUserService.validateUserCredentialsOkta(any())).thenReturn(new PasswordImportResponseResource());
    this.mockMvc
            .perform(MockMvcRequestBuilders.post("/admin/user-provisioning/v1/okta/validation-hook")
                    .content(jsonRequestObject)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(status().isOk());
  }

  @Test
  public void netbanxPasswordMigration_withValidInput_shouldSuccess() throws Exception {
    final String jsonRequestObject = JsonUtil.toJsonString(UserTestUtility.getPasswordImportRequestResourceForTest());
    when(skrillTellerUserService.netbanxPasswordMigration(any())).thenReturn(new PasswordImportResponseResource());
    this.mockMvc.perform(
        MockMvcRequestBuilders.post("/admin/user-provisioning/v1/okta/netbanx-password-hook").content(jsonRequestObject)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

}