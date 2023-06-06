// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paysafe.op.errorhandling.web.handlers.OneplatformDefaultControllerAdvice;
import com.paysafe.upf.user.provisioning.config.DynamicBrandsConfig;
import com.paysafe.upf.user.provisioning.service.DynamicBrandsService;
import com.paysafe.upf.user.provisioning.web.rest.resource.BrandPermissionsMapping;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.ArrayList;
import java.util.HashMap;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class DynamicBrandsControllerTest {
  private MockMvc mockMvc;

  @InjectMocks
  @Spy
  private DynamicBrandsController dynamicBrandsController;

  @Mock
  private DynamicBrandsService dynamicBrandsService;

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

    mockMvc = MockMvcBuilders.standaloneSetup(this.dynamicBrandsController).build();
  }

  @Test
  public void testDynamicBrands() throws Exception {
    when(dynamicBrandsService.fetchBrands()).thenReturn(new ArrayList<BrandPermissionsMapping>());
    HashMap<String, String> authorization = new HashMap<>();
    mockMvc.perform(get("/user-provisioning/v1/dynamicBrands").header("Authorization", authorization))
        .andExpect(status().isOk());
  }
}
