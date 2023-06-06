// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paysafe.op.errorhandling.web.handlers.OneplatformDefaultControllerAdvice;
import com.paysafe.upf.user.provisioning.enums.TokenType;
import com.paysafe.upf.user.provisioning.service.impl.TokenServiceImpl;
import com.paysafe.upf.user.provisioning.util.JsonUtil;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateTokenResource;

import org.hamcrest.core.Is;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class TokenControllerTest {

  private MockMvc mockMvc;

  @InjectMocks
  @Spy
  private TokenController tokenController;

  @Mock
  private TokenServiceImpl tokenService;

  private static final String CREATE_TOKEN_PATH = "/admin/user-provisioning/v1/users/{uuid}/tokens";
  private static final String SAMPLE_USER_ID = "Wl733qIKL8@paysafe.com";
  private static final String SAMPLE_UUID = "e06bb581-058d-4be0-b334-88dcc559c7bc";

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

    mockMvc = MockMvcBuilders.standaloneSetup(this.tokenController).build();
  }

  @Test
  public void testCreateToken() throws Exception {
    TokenRequestResource tokenRequestResource = getTokenRequestResource();
    final String requestObjectContent = JsonUtil.toJsonString(tokenRequestResource);

    TokenResponseResource tokenResponseResource = getTokenResponseResource();
    when(tokenService.createToken(any(), any(), any())).thenReturn(tokenResponseResource);

    this.mockMvc
        .perform(MockMvcRequestBuilders.post(CREATE_TOKEN_PATH, SAMPLE_USER_ID).content(requestObjectContent)
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.id", Is.is(tokenResponseResource.getId())))
        .andExpect(jsonPath("$.creationDate", Is.is(tokenResponseResource.getCreationDate().getMillis())))
        .andExpect(jsonPath("$.validUntil", Is.is(tokenResponseResource.getValidUntil().getMillis())));

  }

  @Test
  public void testGetToken() throws Exception {
    TokenResponseResource tokenResponseResource = getTokenResponseResource();

    when(tokenService.getToken(any(), any(), any())).thenReturn(tokenResponseResource);

    this.mockMvc
        .perform(MockMvcRequestBuilders
            .get("/admin/user-provisioning/v1/users/{userId}/tokens/{tokenId}?tokenType=PASSWORD_RECOVERY",
                SAMPLE_USER_ID, "5755f635-e170-4872-9dba-9d618847c108")
            .accept(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().isOk()).andExpect(jsonPath("$.id", Is.is(tokenResponseResource.getId())))
        .andExpect(jsonPath("$.validUntil", Is.is(tokenResponseResource.getValidUntil().getMillis())))
        .andExpect(jsonPath("$.creationDate", Is.is(tokenResponseResource.getCreationDate().getMillis())));
  }

  @Test
  public void testUpdateToken() throws Exception {
    UpdateTokenResource updateTokenResource = getUpdateTokenResource();
    final String requestObjectContent = JsonUtil.toJsonString(updateTokenResource);

    TokenResponseResource tokenResponseResource = getTokenResponseResource();
    when(tokenService.updateToken(any(), anyString(), anyString())).thenReturn(tokenResponseResource);

    this.mockMvc
        .perform(MockMvcRequestBuilders
            .patch("/admin/user-provisioning/v1/users/{uuid}/tokens/{tokenId}", SAMPLE_UUID,
                "5755f635-e170-4872-9dba-9d618847c108")
            .content(requestObjectContent).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8))
        .andExpect(status().isOk()).andExpect(jsonPath("$.id", Is.is(tokenResponseResource.getId())))
        .andExpect(jsonPath("$.validUntil", Is.is(tokenResponseResource.getValidUntil().getMillis())))
        .andExpect(jsonPath("$.creationDate", Is.is(tokenResponseResource.getCreationDate().getMillis())));
  }

  private TokenRequestResource getTokenRequestResource() {
    TokenRequestResource tokenRequestResource = new TokenRequestResource();
    tokenRequestResource.setTimeToLiveInSeconds(3000L);
    tokenRequestResource.setTokenType(TokenType.PASSWORD_RECOVERY);
    return tokenRequestResource;
  }

  private TokenResponseResource getTokenResponseResource() {
    TokenResponseResource tokenResponseResource = new TokenResponseResource();
    tokenResponseResource.setId("5755f635-e170-4872-9dba-9d618847c108");
    tokenResponseResource.setCreationDate(DateTime.now());
    tokenResponseResource.setValidUntil(DateTime.now());
    return tokenResponseResource;
  }

  private UpdateTokenResource getUpdateTokenResource() {
    UpdateTokenResource updateTokenResource = new UpdateTokenResource();
    updateTokenResource.setTimeToLiveInSeconds(60L);
    updateTokenResource.setTokenType(TokenType.PASSWORD_RECOVERY);
    return updateTokenResource;
  }

}
