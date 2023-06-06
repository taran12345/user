// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paysafe.op.errorhandling.web.handlers.OneplatformDefaultControllerAdvice;
import com.paysafe.upf.user.provisioning.service.ModuleService;
import com.paysafe.upf.user.provisioning.util.JsonUtil;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class ModulesControllerTest {

  private static final String ROLE_MODULES_API_URI = "/user-provisioning/v1/role-modules";
  private static final String MODULES_API_URI = "/user-provisioning/v1/modules";

  private MockMvc mockMvc;

  @InjectMocks
  @Spy
  private ModulesController modulesController;

  @Mock
  private ModuleService moduleService;

  /**
   * Initial setup.
   */
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    final StaticApplicationContext applicationContext = new StaticApplicationContext();
    applicationContext.registerSingleton("exceptionHandler", OneplatformDefaultControllerAdvice.class);
    final WebMvcConfigurationSupport webMvcConfigurationSupport = new WebMvcConfigurationSupport();
    webMvcConfigurationSupport.setApplicationContext(applicationContext);
    mockMvc = MockMvcBuilders.standaloneSetup(this.modulesController).build();
  }

  @Test
  public void getRoleModulesJson_withValidInput_shouldSucceed() throws Exception {
    when(moduleService.getRoleModulesJson(any(), any(), any()))
        .thenReturn(UserTestUtility.getRoleModulesResourcesList());
    mockMvc.perform(get(ROLE_MODULES_API_URI).header("Authorization", UserTestUtility.AUTH_TOKEN_PORTAL)
        .param("ownerType", "PARTNER").param("ownerId", "1234").param("role", "BP_US_ADMIN"))
        .andExpect(status().isOk());
  }

  @Test
  public void getModulesListForRolesJson_withValidInput_shouldSucceed() throws Exception {
    when(moduleService.getModulesListForRole(any())).thenReturn(UserTestUtility.getModulesForRolesList());
    mockMvc.perform(get(ROLE_MODULES_API_URI + "/{roleId}", "BP_EU_ADMIN").header("Authorization",
        UserTestUtility.AUTH_TOKEN_PORTAL)).andExpect(status().isOk());
  }

  @Test
  public void getModulesJson_withValidInput_shouldSucceed() throws Exception {
    when(moduleService.getModules()).thenReturn(new ArrayList<>());
    mockMvc
        .perform(get(MODULES_API_URI).header("Authorization", UserTestUtility.AUTH_TOKEN_PORTAL))
        .andExpect(status().isOk());
  }

  @Test
  public void addModulesListForRole_withValidInput_shouldSucceed() throws Exception {
    final String jsonRequestObject = JsonUtil.toJsonString(UserTestUtility.getRoleModuleListResource());

    when(moduleService.getModules()).thenReturn(new ArrayList<>());
    mockMvc.perform(
        MockMvcRequestBuilders.post(ROLE_MODULES_API_URI).content(jsonRequestObject)
            .header("Authorization", UserTestUtility.AUTH_TOKEN_PORTAL)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().isOk());
  }

}
