// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paysafe.op.errorhandling.web.handlers.OneplatformDefaultControllerAdvice;
import com.paysafe.upf.user.provisioning.service.MasterMerchantService;
import com.paysafe.upf.user.provisioning.util.MerchantTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.validator.UserDetailsRequestValidator;

import org.apache.commons.lang.StringUtils;
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
import java.util.HashSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
public class MerchantControllerTest {

  private static final String AUTHORIZATION_STRING = "Authorization";
  private static final String AUTH_HEADER_VALUE = StringUtils.EMPTY;
  private static final String LEGAL_ENTITY_URI = "/user-provisioning/v1/legalentity/hierarchy";
  private static final String PARTNER_HIERARCHY_URI = "/user-provisioning/v2/legalentity/hierarchy";
  private static final String ACCOUNTGROUP_FMA_URI = "/user-provisioning/v1/accountgroups";
  private static final String PARTNER_HIERARCHY_LIST_URI = "/user-provisioning/v2/partner/legalentity/hierarchy";
  private static final String ACCOUNT_GROUPS_CONTAINS_SEARCH_URI = "/user-provisioning/v2/accountgroups";
  private static final String ACCOUNT_TYPE_USERS_GET_URI = "/user-provisioning/v1/account-type/users";

  private MockMvc mockMvc;

  @InjectMocks
  @Spy
  private MerchantController merchantController;

  @Mock
  private MasterMerchantService masterMerchantService;

  @Mock
  private UserDetailsRequestValidator userDetailsRequestValidator;

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

    mockMvc = MockMvcBuilders.standaloneSetup(this.merchantController).build();
  }

  @Test
  public void testGetPmleList() throws Exception {
    when(masterMerchantService.getPmleList(anyString(), anyString(), anyBoolean()))
        .thenReturn(MerchantTestUtility.getPmles());

    mockMvc.perform(get(LEGAL_ENTITY_URI, "wallet777")
        .header(AUTHORIZATION_STRING, AUTH_HEADER_VALUE)
        .param("ownerType", "PMLE")
        .param("ownerId", "12345"))
        .andExpect(status().isOk());
  }

  @Test
  public void testPartnerHierarchy() throws Exception {
    when(masterMerchantService.getPartnerHierarchy(anyString(), anyString(), anyBoolean()))
        .thenReturn(MerchantTestUtility.getPartnerHierarchy());

    mockMvc.perform(get(PARTNER_HIERARCHY_URI, "wallet777")
        .header(AUTHORIZATION_STRING, AUTH_HEADER_VALUE)
        .param("ownerType", "PARTNER")
        .param("ownerId", "13920"))
        .andExpect(status().isOk());
  }

  @Test
  public void getPartnerHierarchyList_withValidInput_shouldSucceed() throws Exception {
    when(masterMerchantService.getPartnerHierarchyList(anyString(), anyString())).thenReturn(new HashSet<>());
    mockMvc.perform(get(PARTNER_HIERARCHY_LIST_URI, "wallet777").header(AUTHORIZATION_STRING, AUTH_HEADER_VALUE)
        .param("ownerType", "PARTNER").param("ownerId", "13920")).andExpect(status().isOk());
  }
  
  @Test
  public void testGetFmaIdFromAccountGroupId() throws Exception {
    mockMvc.perform(get(ACCOUNTGROUP_FMA_URI)
        .header(AUTHORIZATION_STRING, AUTH_HEADER_VALUE)
        .param("accountGroupId", "PARTNER"))
        .andExpect(status().isOk());
  }

  @Test
  public void getAccountGroupsByContainsSearch_withValidInput_shouldSucceed() throws Exception {
    when(masterMerchantService.getAccountGroupsByContainsSearch(anyString())).thenReturn(new ArrayList<>());
    mockMvc.perform(get(ACCOUNT_GROUPS_CONTAINS_SEARCH_URI).header(AUTHORIZATION_STRING, AUTH_HEADER_VALUE)
        .param("accountGroupId", "1234")).andExpect(status().isOk());
  }

  @Test
  public void getAccountTypeUsers_withValidInput_shouldSucceed() throws Exception {
    when(masterMerchantService.getAccountTypeUsers(any(), any(), any(), anyBoolean(), any(), any()))
        .thenReturn(new UsersListResponseResource());
    mockMvc.perform(get(ACCOUNT_TYPE_USERS_GET_URI).header(AUTHORIZATION_STRING, AUTH_HEADER_VALUE)
        .param("resourceId", "1234").param("resourceName", "FMA")).andExpect(status().isOk());
  }
}
