// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.enums.OwnerType;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.ResourceModuleDetails;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModuleListResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModulesList;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModulesResource;

import java.util.List;
import java.util.Set;

public interface ModuleService {
  List<RoleModulesResource> getRoleModulesJson(OwnerType ownerType, String ownerId, String role);

  List<RoleModulesList> getModulesListForRole(String roleId);

  RoleModuleListResource addModulesListForRole(RoleModuleListResource roleModuleList);

  List<ResourceModuleDetails> getModules();

  Set<String> getBusinessInitiativesForIds(OwnerType ownerType, String ownerId);
}