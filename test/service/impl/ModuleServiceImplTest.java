// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.upf.user.provisioning.config.UserProvisioningConfig;
import com.paysafe.upf.user.provisioning.domain.rolemodules.BusinessInitiativeRoles;
import com.paysafe.upf.user.provisioning.domain.rolemodules.Module;
import com.paysafe.upf.user.provisioning.enums.BusinessUnit;
import com.paysafe.upf.user.provisioning.enums.OwnerType;
import com.paysafe.upf.user.provisioning.repository.rolemodules.BusinessInitiativeInfoRepository;
import com.paysafe.upf.user.provisioning.repository.rolemodules.BusinessInitiativeRolesRepository;
import com.paysafe.upf.user.provisioning.repository.rolemodules.ModuleAccessLevelRepository;
import com.paysafe.upf.user.provisioning.repository.rolemodules.ModulePermRepository;
import com.paysafe.upf.user.provisioning.repository.rolemodules.ModuleRepository;
import com.paysafe.upf.user.provisioning.repository.rolemodules.RoleModulesRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.MasterMerchantService;
import com.paysafe.upf.user.provisioning.service.SmartRoutingService;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.ResourceModuleDetails;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModuleListResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModulesList;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModulesResource;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ModuleServiceImplTest {

  @InjectMocks
  private ModuleServiceImpl moduleServiceImpl;

  @Mock
  private RoleModulesRepository roleModulesRepository;

  @Mock
  private ModuleRepository moduleRepository;

  @Mock
  private ModuleAccessLevelRepository moduleAccessLevelRepository;

  @Mock
  private ModulePermRepository modulePermRepository;

  @Mock
  private BusinessInitiativeInfoRepository businessInitiativeInfoRepository;

  @Mock
  private MasterMerchantService masterMerchantService;

  @Mock
  private BusinessInitiativeRolesRepository businessInitiativeRolesRepository;

  @Mock
  private SmartRoutingService smartRoutingService;

  @Mock
  private UserProvisioningConfig userProvisioningConfig;

  /**
   * Setup test configuration.
   *
   * @throws Exception exception
   */
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    CommonThreadLocal.setAuthLocal(UserTestUtility.getAuthorizationInfo(UserTestUtility.AUTH_TOKEN_PORTAL));
  }

  @Test
  public void getRoleModulesJson_withValidInput_shouldSucceed() {
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(UserTestUtility.getMerchantSearchResponse());
    when(businessInitiativeInfoRepository.findById(any()))
        .thenReturn(Optional.of(UserTestUtility.getBusinessInitiativeInfo()));
    when(roleModulesRepository.findByroleInOrderByDisplayOrderAsc(any())).thenReturn(UserTestUtility.getRoleModules());
    when(moduleRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getModule()));
    when(moduleAccessLevelRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModuleAccessLevelList());
    when(modulePermRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModulePermissions());

    List<RoleModulesResource> roleModules = moduleServiceImpl.getRoleModulesJson(OwnerType.PMLE, "1234", null);
    assertNotNull(roleModules);
    assertThat(roleModules.size(), Is.is(2));
    verify(roleModulesRepository, times(1)).findByroleInOrderByDisplayOrderAsc(any());
    verify(businessInitiativeInfoRepository, times(1)).findById(any());
    verify(masterMerchantService, times(1)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void getRoleModulesJson_withValidInputAndIgmaingTag_shouldSucceed() {
    MerchantSearchResponse respone = UserTestUtility.getMerchantSearchResponse();
    respone.getMerchants().get(0).getPaymentAccounts().get(0).getProcessingAccounts().get(0).getBusinessDetails()
        .setTags(new ArrayList<>(Arrays.asList(BusinessUnit.US_I_GAMING.toString())));
    when(masterMerchantService.getMerchantsUsingSearchAfter(any())).thenReturn(respone);
    when(businessInitiativeInfoRepository.findById(any()))
        .thenReturn(Optional.of(UserTestUtility.getBusinessInitiativeInfo()));
    when(roleModulesRepository.findByroleInOrderByDisplayOrderAsc(any())).thenReturn(UserTestUtility.getRoleModules());
    when(moduleRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getModule()));
    when(moduleAccessLevelRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModuleAccessLevelList());
    when(modulePermRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModulePermissions());

    List<RoleModulesResource> roleModules = moduleServiceImpl.getRoleModulesJson(OwnerType.PMLE, "1234", null);
    assertNotNull(roleModules);
    assertThat(roleModules.size(), Is.is(2));
    verify(roleModulesRepository, times(1)).findByroleInOrderByDisplayOrderAsc(any());
    verify(businessInitiativeInfoRepository, times(1)).findById(any());
    verify(masterMerchantService, times(1)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void getRoleModulesJson_withValidInputAndSkrillGamingTag_shouldSucceed() {
    MerchantSearchResponse respone = UserTestUtility.getMerchantSearchResponse();
    respone.getMerchants().get(0).getPaymentAccounts().get(0).getProcessingAccounts().get(0).getBusinessDetails()
        .setTags(new ArrayList<>(Arrays.asList(BusinessUnit.SKILL_GAMING.toString())));
    when(masterMerchantService.getMerchantsUsingSearchAfter(any())).thenReturn(respone);
    when(businessInitiativeInfoRepository.findById(any()))
        .thenReturn(Optional.of(UserTestUtility.getBusinessInitiativeInfo()));
    when(roleModulesRepository.findByroleInOrderByDisplayOrderAsc(any())).thenReturn(UserTestUtility.getRoleModules());
    when(moduleRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getModule()));
    when(moduleAccessLevelRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModuleAccessLevelList());
    when(modulePermRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModulePermissions());

    List<RoleModulesResource> roleModules = moduleServiceImpl.getRoleModulesJson(OwnerType.PMLE, "1234", null);
    assertNotNull(roleModules);
    assertThat(roleModules.size(), Is.is(2));
    verify(roleModulesRepository, times(1)).findByroleInOrderByDisplayOrderAsc(any());
    verify(businessInitiativeInfoRepository, times(1)).findById(any());
    verify(masterMerchantService, times(1)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void getRoleModulesJson_withValidInputAndEcommTag_shouldSucceed() {
    MerchantSearchResponse respone = UserTestUtility.getMerchantSearchResponse();
    respone.getMerchants().get(0).getPaymentAccounts().get(0).getProcessingAccounts().get(0).getBusinessDetails()
        .setTags(new ArrayList<>(Arrays.asList(BusinessUnit.US_E_COMMERCE.toString())));
    when(masterMerchantService.getMerchantsUsingSearchAfter(any())).thenReturn(respone);
    when(businessInitiativeInfoRepository.findById(any()))
        .thenReturn(Optional.of(UserTestUtility.getBusinessInitiativeInfo()));
    when(roleModulesRepository.findByroleInOrderByDisplayOrderAsc(any())).thenReturn(UserTestUtility.getRoleModules());
    when(moduleRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getModule()));
    when(moduleAccessLevelRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModuleAccessLevelList());
    when(modulePermRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModulePermissions());

    List<RoleModulesResource> roleModules = moduleServiceImpl.getRoleModulesJson(OwnerType.PMLE, "1234", null);
    assertNotNull(roleModules);
    assertThat(roleModules.size(), Is.is(2));
    verify(roleModulesRepository, times(1)).findByroleInOrderByDisplayOrderAsc(any());
    verify(businessInitiativeInfoRepository, times(1)).findById(any());
    verify(masterMerchantService, times(1)).getMerchantsUsingSearchAfter(any());
  }

  @Test(expected = NotFoundException.class)
  public void getRoleModulesJson_whenBusinessTagNotFoundInMM_shouldthrowException() {
    MerchantSearchResponse merchantSearchResponse = UserTestUtility.getMerchantSearchResponse();
    merchantSearchResponse.getMerchants().get(0).getPaymentAccounts().get(0).getProcessingAccounts().get(0)
        .getBusinessDetails().setTags(null);
    when(masterMerchantService.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    moduleServiceImpl.getRoleModulesJson(OwnerType.PMLE, "1234", null);
  }

  @Test(expected = NotFoundException.class)
  public void getRoleModulesJson_whenBusinessTagNotConfiguedInDbTable_shouldthrowException() {
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(UserTestUtility.getMerchantSearchResponse());
    when(businessInitiativeInfoRepository.findById(any())).thenReturn(Optional.empty());
    moduleServiceImpl.getRoleModulesJson(OwnerType.PMLE, "1234", null);
  }

  @Test(expected = NotFoundException.class)
  public void getRoleModulesJson_whenModuleNotFound_shouldthrowException() {
    when(masterMerchantService.getMerchantsUsingSearchAfter(any()))
        .thenReturn(UserTestUtility.getMerchantSearchResponse());
    when(businessInitiativeInfoRepository.findById(any()))
        .thenReturn(Optional.of(UserTestUtility.getBusinessInitiativeInfo()));
    when(roleModulesRepository.findByroleInOrderByDisplayOrderAsc(any())).thenReturn(UserTestUtility.getRoleModules());
    when(moduleRepository.findById(any())).thenReturn(Optional.empty());
    moduleServiceImpl.getRoleModulesJson(OwnerType.PMLE, "1234", null);
  }

  @Test
  public void getRoleModulesJson_withRoleParam_shouldSucceed() {
    MerchantSearchResponse merchantSearchResponse = UserTestUtility.getMerchantSearchResponse();
    merchantSearchResponse.getMerchants().get(0).getPaymentAccounts().get(0).getProcessingAccounts().get(0)
        .getBusinessDetails().setTags(new ArrayList<>());
    when(masterMerchantService.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    when(businessInitiativeInfoRepository.findById(any()))
        .thenReturn(Optional.of(UserTestUtility.getBusinessInitiativeInfo()));
    when(roleModulesRepository.findByroleInOrderByDisplayOrderAsc(any())).thenReturn(UserTestUtility.getRoleModules());
    when(moduleRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getModule()));
    when(moduleAccessLevelRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModuleAccessLevelList());
    when(modulePermRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModulePermissions());
    when(businessInitiativeRolesRepository.findByRole(any())).thenReturn(UserTestUtility.getBusinessInitiativeRoles());

    List<RoleModulesResource> roleModules = moduleServiceImpl.getRoleModulesJson(null, null, DataConstants.BP_EU_ADMIN);
    assertNotNull(roleModules);
    assertThat(roleModules.size(), Is.is(2));
    verify(roleModulesRepository, times(1)).findByroleInOrderByDisplayOrderAsc(any());
    verify(businessInitiativeInfoRepository, times(1)).findById(any());
  }

  @Test
  public void getRoleModulesJson_withRoleParamAndMleOwnerType_shouldSucceed() {
    MerchantSearchResponse merchantSearchResponse = UserTestUtility.getMerchantSearchResponse();
    merchantSearchResponse.getMerchants().get(0).getPaymentAccounts().get(0).getProcessingAccounts().get(0)
        .getBusinessDetails().setTags(new ArrayList<>());
    when(masterMerchantService.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    when(businessInitiativeInfoRepository.findById(any()))
        .thenReturn(Optional.of(UserTestUtility.getBusinessInitiativeInfo()));
    when(roleModulesRepository.findByroleInOrderByDisplayOrderAsc(any())).thenReturn(UserTestUtility.getRoleModules());
    when(moduleRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getModule()));
    when(moduleAccessLevelRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModuleAccessLevelList());
    when(modulePermRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModulePermissions());
    when(businessInitiativeRolesRepository.findByRole(any())).thenReturn(UserTestUtility.getBusinessInitiativeRoles());

    List<RoleModulesResource> roleModules =
        moduleServiceImpl.getRoleModulesJson(OwnerType.MLE, "1234", DataConstants.BP_EU_ADMIN);
    assertNotNull(roleModules);
    assertThat(roleModules.size(), Is.is(2));
    verify(roleModulesRepository, times(1)).findByroleInOrderByDisplayOrderAsc(any());
    verify(businessInitiativeInfoRepository, times(1)).findById(any());
  }

  @Test(expected = NotFoundException.class)
  public void getRoleModulesJson_withRoleParamWhenBusinessInitiativeNotFound_shouldthrowException() {
    BusinessInitiativeRoles businessInitiativeRole = UserTestUtility.getBusinessInitiativeRoles().get(0);
    businessInitiativeRole.setBusinessInitiative("");
    when(businessInitiativeRolesRepository.findByRole(any())).thenReturn(new ArrayList<>());
    moduleServiceImpl.getRoleModulesJson(null, null, DataConstants.BP_EU_ADMIN);
  }

  @Test
  public void getRoleModulesJson_withEuAcquiringNonEeaBusiness_shouldSucceed() {
    MerchantSearchResponse merchantSearchResponse = UserTestUtility.getMerchantSearchResponse();
    merchantSearchResponse.getMerchants().get(0).getPaymentAccounts().get(0).getProcessingAccounts().get(0)
        .getBusinessDetails().setTags(new ArrayList<>(Arrays.asList(DataConstants.EU_ACQUIRING_NON_EEA)));
    when(masterMerchantService.getMerchantsUsingSearchAfter(any())).thenReturn(merchantSearchResponse);
    when(businessInitiativeInfoRepository.findById(any()))
        .thenReturn(Optional.of(UserTestUtility.getBusinessInitiativeInfo()));
    when(roleModulesRepository.findByroleInOrderByDisplayOrderAsc(any())).thenReturn(UserTestUtility.getRoleModules());
    when(moduleRepository.findById(any())).thenReturn(Optional.of(UserTestUtility.getModule()));
    when(moduleAccessLevelRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModuleAccessLevelList());
    when(modulePermRepository.findBymoduleId(any())).thenReturn(UserTestUtility.getModulePermissions());

    List<RoleModulesResource> roleModules = moduleServiceImpl.getRoleModulesJson(OwnerType.PMLE, "1234", null);
    assertNotNull(roleModules);
    assertThat(roleModules.size(), Is.is(2));
    verify(roleModulesRepository, times(1)).findByroleInOrderByDisplayOrderAsc(any());
    verify(businessInitiativeInfoRepository, times(1)).findById(any());
    verify(masterMerchantService, times(1)).getMerchantsUsingSearchAfter(any());
  }

  @Test
  public void getModulesListForRolesJson_withValidInput_shouldSucceed() {
    when(roleModulesRepository.findByroleInOrderByDisplayOrderAsc(any())).thenReturn(UserTestUtility.getRoleModules());

    List<RoleModulesList> roleModules = moduleServiceImpl.getModulesListForRole(DataConstants.BP_EU_ADMIN);
    assertNotNull(roleModules);
    assertThat(roleModules.size(), Is.is(2));
    verify(roleModulesRepository, times(1)).findByroleInOrderByDisplayOrderAsc(any());
  }

  @Test
  public void addModulesListForRoleJson_withValidInput_shouldSucceed() {
    when(roleModulesRepository.saveAll(any())).thenReturn(UserTestUtility.getRoleModules());

    RoleModuleListResource roleModuleListResource =
        moduleServiceImpl.addModulesListForRole(UserTestUtility.getRoleModuleListResource());
    assertNotNull(roleModuleListResource);
    verify(roleModulesRepository, times(1)).saveAll(any());
  }

  @Test
  public void ggetModulesJson_withValidInput_shouldSucceed() {
    Module module = UserTestUtility.getModule();
    module.setModulePermissions(UserTestUtility.getModulePermissions());
    module.setModuleAccessLevel(UserTestUtility.getModuleAccessLevelList());
    when(moduleRepository.findAll()).thenReturn(new ArrayList<>(Arrays.asList(module)));

    List<ResourceModuleDetails> resourceModuleDetails = moduleServiceImpl.getModules();
    assertNotNull(resourceModuleDetails);
    assertThat(resourceModuleDetails.size(), Is.is(1));
    verify(moduleRepository, times(1)).findAll();
  }
}
