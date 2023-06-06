// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paysafe.op.errorhandling.web.handlers.OneplatformDefaultControllerAdvice;
import com.paysafe.upf.user.provisioning.service.SkrillTellerUserService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserCountDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.UserDetailsResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.UserRegionUpdateResponse;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class SkrillTellerUserControllerTest {

  private static final String AUTHORIZATION = "Authorization";

  private MockMvc mockMvc;

  @Mock
  private SkrillTellerUserService skrillTellerUserService;

  @InjectMocks
  @Spy
  private SkrillTellerUserController skrillTellerUserController;

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
    mockMvc = MockMvcBuilders.standaloneSetup(this.skrillTellerUserController).build();
  }

  @Test
  public void getWalletAdminUsers_withValidData_shouldSucceed() throws Exception {
    when(skrillTellerUserService.getWalletAdminUsers(any())).thenReturn(new ArrayList<>());
    this.mockMvc.perform(
        MockMvcRequestBuilders.get("/admin/user-provisioning/v1/users/wallets/{walletId}/adminUsers", "walletId1")
            .header(AUTHORIZATION, UserTestUtility.AUTH_TOKEN_SKRILL))
        .andExpect(status().isOk());
  }

  @Test
  public void getUserCountByWalletIds_withWalletIds_shouldSucceed() throws Exception {
    Map<String, UserCountDto> userCountDtoMap = new HashMap<>();
    userCountDtoMap.put("123", UserCountDto.builder()
        .adminUsers(12L)
        .totalUsers(20L)
        .build());
    when(skrillTellerUserService.getUsersCountByWalletIdsUsingLinkedBrands(anyList())).thenReturn(userCountDtoMap);
    this.mockMvc.perform(
        MockMvcRequestBuilders.post("/admin/user-provisioning/v1/users/wallets/count")
            .header(AUTHORIZATION, UserTestUtility.AUTH_TOKEN_SKRILL).content("[\"123\"]")
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

  @Test
  public void getListOfBrands() throws Exception {
    when(skrillTellerUserService.getBrands(anyString())).thenReturn(new ArrayList<>());
    this.mockMvc.perform(
        MockMvcRequestBuilders.get("/admin/user-provisioning/v1/brands")
            .header(AUTHORIZATION, UserTestUtility.AUTH_TOKEN_SKRILL))
        .andExpect(status().isOk());
  }

  @Test
  public void downloadUserEmails() throws Exception {
    when(skrillTellerUserService.getUserEmails(anyString(),any(), any(), any()))
        .thenReturn(new UserDetailsResponseResource());
    this.mockMvc.perform(
        MockMvcRequestBuilders.get("/admin/user-provisioning/v1/users/email/download")
            .param("regionType", "INC")
            .header(AUTHORIZATION, UserTestUtility.AUTH_TOKEN_SKRILL)
            .header("Application", "SKRILL"))
        .andExpect(status().isOk());
  }

  @Test
  public void updateUsersRegion() throws Exception {
    when(skrillTellerUserService.updateUsersRegion(anyString(),any(),any())).thenReturn(new UserRegionUpdateResponse());
    this.mockMvc.perform(
        MockMvcRequestBuilders.patch("/admin/user-provisioning/v1/users/region")
            .param("loginName", "abc@abc.com")
            .header(AUTHORIZATION, UserTestUtility.AUTH_TOKEN_SKRILL)
            .header("Application", "SKRILL"))
        .andExpect(status().isOk());
  }
}
