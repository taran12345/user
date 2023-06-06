// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.upf.user.provisioning.config.UserProvisioningConfig;
import com.paysafe.upf.user.provisioning.domain.rolemodules.BusinessInitiativeInfo;
import com.paysafe.upf.user.provisioning.domain.rolemodules.BusinessInitiativeRoles;
import com.paysafe.upf.user.provisioning.domain.rolemodules.Module;
import com.paysafe.upf.user.provisioning.domain.rolemodules.ModuleAccessLevel;
import com.paysafe.upf.user.provisioning.domain.rolemodules.ModulePermissions;
import com.paysafe.upf.user.provisioning.domain.rolemodules.RoleModules;
import com.paysafe.upf.user.provisioning.enums.BusinessUnit;
import com.paysafe.upf.user.provisioning.enums.OwnerType;
import com.paysafe.upf.user.provisioning.repository.rolemodules.BusinessInitiativeInfoRepository;
import com.paysafe.upf.user.provisioning.repository.rolemodules.BusinessInitiativeRolesRepository;
import com.paysafe.upf.user.provisioning.repository.rolemodules.ModuleAccessLevelRepository;
import com.paysafe.upf.user.provisioning.repository.rolemodules.ModulePermRepository;
import com.paysafe.upf.user.provisioning.repository.rolemodules.ModuleRepository;
import com.paysafe.upf.user.provisioning.repository.rolemodules.RoleModulesRepository;
import com.paysafe.upf.user.provisioning.service.MasterMerchantService;
import com.paysafe.upf.user.provisioning.service.ModuleService;
import com.paysafe.upf.user.provisioning.service.SmartRoutingService;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchAfterRequest;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.PaymentAccountResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.ModuleResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.ModulesResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.ResourceModuleAccessLevel;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.ResourceModuleDetails;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.ResourceModulePermissions;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModuleListResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModulesList;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModulesResource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ModuleServiceImpl implements ModuleService {

  private static final Logger logger = LoggerFactory.getLogger(ModuleServiceImpl.class);

  @Autowired
  private RoleModulesRepository roleModulesRepository;

  @Autowired
  private ModuleRepository moduleRepository;

  @Autowired
  private ModuleAccessLevelRepository moduleAccessLevelRepository;

  @Autowired
  private ModulePermRepository modulePermRepository;

  @Autowired
  private BusinessInitiativeInfoRepository businessInitiativeInfoRepository;

  @Autowired
  private MasterMerchantService masterMerchantService;

  @Autowired
  private BusinessInitiativeRolesRepository businessInitiativeRolesRepository;

  @Autowired
  private SmartRoutingService smartRoutingService;

  @Autowired
  private UserProvisioningConfig userProvisioningConfig;

  private static final List<String> BUSINESS_TAGS_RESPONSE_FIELDS =
      Collections.singletonList("paymentAccounts.processingAccounts.businessDetails.tags");

  @Override
  public List<RoleModulesResource> getRoleModulesJson(OwnerType ownerType, String ownerId, String role) {
    String businessInitiative = null;
    if (StringUtils.isNotEmpty(role)) {
      businessInitiative = getBusinessInitiativeByRole(role);
    } else if (StringUtils.isNotEmpty(ownerId) && ownerType != null) {
      Set<String> businessTags = getBusinessInitiativesForIds(ownerType, ownerId);
      businessInitiative = validateAndReturnBusinessInitiative(businessTags);
    }
    if (StringUtils.isEmpty(businessInitiative)) {
      businessInitiative = DataConstants.EU_ACQUIRING_EEA;
      logger.info("Business Initiative not found, and using the: {} Initiative", DataConstants.EU_ACQUIRING_EEA);
    }
    Optional<BusinessInitiativeInfo> optionalBusinessInitiativeInfo =
        businessInitiativeInfoRepository.findById(businessInitiative);
    BusinessInitiativeInfo businessInitiativeInfo = null;
    if (optionalBusinessInitiativeInfo.isPresent()) {
      businessInitiativeInfo = optionalBusinessInitiativeInfo.get();
    } else {
      throw new NotFoundException.Builder().details("Business Initiative is not configured: {0}", businessInitiative)
          .build();
    }
    List<String> roles = businessInitiativeInfo.getBusinessInitiativeRoles().stream()
        .map(BusinessInitiativeRoles::getRole).collect(Collectors.toList());
    List<RoleModules> roleModules = getModuleIdsFromRoles(roles);
    boolean isSingleApiIntegrated = false;
    if (businessInitiative.equals(DataConstants.EU_ACQUIRING_EEA)
        || businessInitiative.equals(DataConstants.EU_ACQUIRING_NON_EEA)) {
      isSingleApiIntegrated = isSingleApiIntegrated(ownerType, ownerId);
      if (!isSingleApiIntegrated) {
        removeSingleApiParentModules(roleModules);
      }
    }
    return constructJsonTreeFromRoleModules(businessInitiativeInfo.getBusinessInitiativeRoles(), roleModules,
        isSingleApiIntegrated);
  }

  @Override
  public Set<String> getBusinessInitiativesForIds(OwnerType ownerType, String ownerId) {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    request.setResponseFields(BUSINESS_TAGS_RESPONSE_FIELDS);
    masterMerchantService.setFilterParamsByResourceType(ownerType.toString(), ownerId, request);
    MerchantSearchResponse merchantSearchResponse = masterMerchantService.getMerchantsUsingSearchAfter(request);
    List<MerchantResponse> merchants = merchantSearchResponse.getMerchants();
    logger.info("found {} number of merchants ", merchants.size());
    List<PaymentAccountResponse> paymentAccounts =
        merchants.stream().map(MerchantResponse::getPaymentAccounts)
            .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream).collect(Collectors.toList());
    List<ProcessingAccount> processingAccountList = paymentAccounts.stream()
        .map(PaymentAccountResponse::getProcessingAccounts).flatMap(Collection::stream).collect(Collectors.toList());
    Set<String> tagsSet = new HashSet<>();
    for (ProcessingAccount processingAccount : processingAccountList) {
      if (processingAccount.getBusinessDetails() != null
          && CollectionUtils.isNotEmpty(processingAccount.getBusinessDetails().getTags())) {
        tagsSet.addAll(processingAccount.getBusinessDetails().getTags());
      }
    }
    return tagsSet;
  }

  private String validateAndReturnBusinessInitiative(Set<String> tagsSet) {
    if (tagsSet.isEmpty() || tagsSet.contains(BusinessUnit.EU_ACQUIRING_EEA.toString())
        || tagsSet.contains(BusinessUnit.EU_ACQUIRING_NON_EEA.toString())) {
      return BusinessUnit.EU_ACQUIRING_EEA.toString();
    }
    if (tagsSet.contains(BusinessUnit.US_I_GAMING.toString())
        || tagsSet.contains(BusinessUnit.SKILL_GAMING.toString())) {
      return BusinessUnit.US_I_GAMING.toString();
    }
    return new ArrayList<>(tagsSet).get(0);
  }

  private List<RoleModulesResource> constructJsonTreeFromRoleModules(List<BusinessInitiativeRoles> businessRoles,
                                                                     List<RoleModules> roleModules,
                                                                     boolean isSingleApiIntegrated) {
    List<RoleModulesResource> roleModulesResourceList = new ArrayList<>();
    for (BusinessInitiativeRoles businessRole : businessRoles) {
      RoleModulesResource roleModuleResource = new RoleModulesResource();
      roleModuleResource.setDisplayOrder(businessRole.getDisplayOrder());
      roleModuleResource.setRoleType(businessRole.getRoleType());
      roleModuleResource.setRoleValue(businessRole.getRole());
      List<RoleModules> roleModuleList =
          roleModules.stream().filter(e -> e.getRole().equals(businessRole.getRole())).collect(Collectors.toList());
      roleModules.removeAll(roleModuleList);
      List<ModuleResource> modules = fetchRoleModulesHierarchy(roleModuleList, isSingleApiIntegrated);

      roleModuleResource.setModules(modules);
      roleModulesResourceList.add(roleModuleResource);

    }
    return roleModulesResourceList;
  }

  private List<ModuleResource> fetchRoleModulesHierarchy(List<RoleModules> roleModuleList,
                                                         boolean isSingleApiIntegrated) {
    List<ModuleResource> moduleResourceList = new ArrayList<>();
    List<Module> moduleList = new ArrayList<>();
    for (RoleModules roleModule : roleModuleList) {
      Optional<Module> optionalModule = moduleRepository.findById(roleModule.getModuleId());
      Module module;
      if (optionalModule.isPresent()) {
        module = optionalModule.get();
      } else {
        throw new NotFoundException.Builder()
            .details("Module not found: {0} for role: {1}", roleModule.getModuleId(), roleModule.getRole()).build();
      }
      Module enabledModule = getEnabledModule(module);
      if (!Objects.isNull(enabledModule)) {
        moduleList.add(enabledModule);
      }
    }
    moduleList.forEach(module -> moduleResourceList.add(constructModuleResource(module, isSingleApiIntegrated)));
    return moduleResourceList;
  }

  private ModuleResource constructModuleResource(Module module, boolean isSingleApiIntegrated) {
    ModuleResource moduleResource = new ModuleResource();
    moduleResource.setDescriptions(module.getDescriptions());
    moduleResource.setDisplayOrder(module.getDisplayOrder());
    moduleResource.setExpand(module.isExpand());
    moduleResource.setIsShow(getModuleAccessLevels(module));
    moduleResource.setLabel(module.getLabel());
    moduleResource.setPermissionsList(getModulePermissions(module));
    moduleResource.setSelected(module.isSelected());
    moduleResource.setSelectionMode(module.getSelectionMode());
    moduleResource.setShowExpand(module.isShowExpand());
    moduleResource.setEditable(module.isEditable());
    moduleResource.setId(module.getId());
    if (CollectionUtils.isNotEmpty(module.getChildren())) {
      List<ModuleResource> childModuleResources = new ArrayList<>();
      for (Module childModule : module.getChildren()) {
        List<String> singleApiModules = userProvisioningConfig.getSingleApiModules();
        if (!isSingleApiIntegrated && singleApiModules.contains(childModule.getId())) {
          continue;
        }
        childModuleResources.add(constructModuleResource(childModule, isSingleApiIntegrated));
      }
      moduleResource.setSubModules(childModuleResources);
    }

    return moduleResource;
  }

  private List<String> getModuleAccessLevels(Module module) {
    List<ModuleAccessLevel> moduleAccessLevels = moduleAccessLevelRepository.findBymoduleId(module.getId());
    return moduleAccessLevels.stream().map(ModuleAccessLevel::getShow).collect(Collectors.toList());
  }

  private List<String> getModulePermissions(Module module) {
    List<ModulePermissions> modulepermissions = modulePermRepository.findBymoduleId(module.getId());
    return modulepermissions.stream().map(ModulePermissions::getPermission).collect(Collectors.toList());
  }

  private List<RoleModules> getModuleIdsFromRoles(List<String> roles) {
    return roleModulesRepository.findByroleInOrderByDisplayOrderAsc(roles);
  }

  private String getBusinessInitiativeByRole(String role) {
    List<BusinessInitiativeRoles> businessInitiativeRoles = businessInitiativeRolesRepository.findByRole(role);
    if (CollectionUtils.isEmpty(businessInitiativeRoles)) {
      throw new NotFoundException.Builder().details("No business initiative found for the role {0}", role).build();
    }
    return businessInitiativeRoles.get(0).getBusinessInitiative();
  }

  private boolean isSingleApiIntegrated(OwnerType ownerType, String ownerId) {
    if ((StringUtils.isNotEmpty(ownerId) && ownerType != null)
        && (OwnerType.PMLE.equals(ownerType) || OwnerType.MLE.equals(ownerType))) {
      Set<String> pmleIds = new HashSet<>();
      if (OwnerType.PMLE.equals(ownerType)) {
        pmleIds.add(ownerId);
      } else {
        pmleIds = masterMerchantService.getAllPmleIds(ownerType.toString(), ownerId);
      }
      List<String> singleApiAccounts = new ArrayList<>();
      for (String pmleId : pmleIds) {
        singleApiAccounts.addAll(smartRoutingService.getSingleApiAccountIdsByPmleId(pmleId));
        if (CollectionUtils.isNotEmpty(singleApiAccounts)) {
          return true;
        }
      }
      return CollectionUtils.isNotEmpty(singleApiAccounts);
    } else {
      return true;
    }
  }

  /**
   * This method removes the single-api parent featues from role-modules, if the accounts are not integrated with
   * single-api.
   */
  private void removeSingleApiParentModules(List<RoleModules> roleModules) {
    List<String> singleApiModules = userProvisioningConfig.getSingleApiModules();
    roleModules.removeIf(roleModule -> singleApiModules.contains(roleModule.getModuleId()));
  }

  @Override
  public List<RoleModulesList> getModulesListForRole(String roleId) {
    List<RoleModules> roleModules =
        roleModulesRepository.findByroleInOrderByDisplayOrderAsc(Collections.singletonList(roleId));
    List<RoleModulesList> roleModulesList = new ArrayList<>();
    for (RoleModules roleModule : roleModules) {
      RoleModulesList module = new RoleModulesList();
      module.setDisplayOrder(roleModule.getDisplayOrder());
      module.setEnabled(roleModule.isEnabled());
      module.setModuleId(roleModule.getModuleId());
      module.setRole(roleModule.getRole());
      roleModulesList.add(module);
    }
    return roleModulesList;
  }

  @Override
  public RoleModuleListResource addModulesListForRole(RoleModuleListResource roleModuleList) {
    List<RoleModules> roleModules = new ArrayList<>();
    for (ModulesResource module : roleModuleList.getModules()) {
      RoleModules roleModule = new RoleModules();
      roleModule.setRole(roleModuleList.getRole());
      roleModule.setModuleId(module.getModuleid());
      roleModule.setDisplayOrder(module.getDisplayorder());
      roleModule.setEnabled(module.isEnabled());
      roleModules.add(roleModule);
    }
    roleModulesRepository.saveAll(roleModules);
    return roleModuleList;
  }

  @Override
  public List<ResourceModuleDetails> getModules() {
    List<ResourceModuleDetails> resourceModuleDetails = new ArrayList<>();
    List<Module> modules = moduleRepository.findAll();
    for (Module module : modules) {
      ResourceModuleDetails resorceModule = new ResourceModuleDetails();
      resorceModule.setModuleid(module.getId());
      resorceModule.setLabel(module.getLabel());
      resorceModule.setSelected(module.isSelected());
      resorceModule.setDescriptions(module.getDescriptions());
      resorceModule.setParentid(module.getParentId());
      resorceModule.setSelectionMode(module.getSelectionMode());
      resorceModule.setShowExpand(module.isShowExpand());
      resorceModule.setExpand(module.isExpand());
      resorceModule.setDisplayOrder(module.getDisplayOrder());
      resorceModule.setEditable(module.isEditable());
      resorceModule.setEnabled(module.isEnabled());
      List<ResourceModulePermissions> resourceModulePermissions = new ArrayList<>();
      for (ModulePermissions modulePermission : module.getModulePermissions()) {
        ResourceModulePermissions resourceModulePermission = new ResourceModulePermissions();
        resourceModulePermission.setPermissionId(modulePermission.getModuleId());
        resourceModulePermission.setEnabled(modulePermission.isEnabled());
        resourceModulePermissions.add(resourceModulePermission);
      }
      resorceModule.setPermissions(resourceModulePermissions);
      List<ResourceModuleAccessLevel> resourceModuleAccessLevel = new ArrayList<>();
      for (ModuleAccessLevel accessLevel : module.getModuleAccessLevel()) {
        ResourceModuleAccessLevel moduleAccessLevel = new ResourceModuleAccessLevel();
        moduleAccessLevel.setShow(accessLevel.getShow());
        moduleAccessLevel.setEnabled(accessLevel.isEnabled());
        resourceModuleAccessLevel.add(moduleAccessLevel);
      }
      resorceModule.setModuleAccessLevel(resourceModuleAccessLevel);
      resourceModuleDetails.add(resorceModule);
    }
    return resourceModuleDetails;
  }

  private Module getEnabledModule(Module module) {
    if (!module.isEnabled()) {
      return null;
    }
    if (!(Objects.isNull(module.getChildren()) || module.getChildren().isEmpty()) ) {
      Set<Module> enabledChildrenModule = new HashSet<>();
      module.getChildren().forEach(childModule -> {
        Module enabledChildModule = getEnabledModule(childModule);
        if (!Objects.isNull(enabledChildModule)) {
          enabledChildrenModule.add(enabledChildModule);
        }
      });
      module.setChildren(enabledChildrenModule.stream().sorted(Comparator
          .comparingInt(Module :: getDisplayOrder))
          .collect(Collectors.toCollection(LinkedHashSet::new)));
    }
    return module;
  }

}
