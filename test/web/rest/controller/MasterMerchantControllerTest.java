// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paysafe.op.errorhandling.web.handlers.OneplatformDefaultControllerAdvice;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.MasterMerchantService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.resource.FetchEmailResponseResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class MasterMerchantControllerTest {

  private MockMvc mockMvc;

  @InjectMocks
  @Spy
  MasterMerchantController masterMerchantController;

  @Mock
  MasterMerchantService masterMerchantService;

  private static final String FETCH_EMAIL_PATH = "/admin/user-provisioning/v1/emails/{userName}";

  /**
   * Initial setup.
   */
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    final StaticApplicationContext applicationContext = new StaticApplicationContext();
    applicationContext.registerSingleton("exceptionHandler", OneplatformDefaultControllerAdvice.class);

    final WebMvcConfigurationSupport webMvcConfigurationSupport = new WebMvcConfigurationSupport();
    webMvcConfigurationSupport.setApplicationContext(applicationContext);

    mockMvc = MockMvcBuilders.standaloneSetup(this.masterMerchantController).build();
  }

  @Test
  public void fetchEmail() throws Exception {
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_SKRILL));
    List<FetchEmailResponseResource> emails = new ArrayList<>();
    Mockito.when(masterMerchantService.getEmail("sampleUserName", "SKRILL")).thenReturn(emails);
    this.mockMvc
        .perform(MockMvcRequestBuilders.get(FETCH_EMAIL_PATH, "sampleUserName")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().isOk());
    CommonThreadLocal.unsetAuthLocal();
  }
}
