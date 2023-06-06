// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.upf.user.provisioning.enums.OwnerType;
import com.paysafe.upf.user.provisioning.service.ModuleService;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.ResourceModuleDetails;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModuleListResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModulesList;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModulesResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

import java.util.List;

@RestController
@RequestMapping({"/admin/user-provisioning/", "/user-provisioning/"})
public class ModulesController {

  @Autowired
  private ModuleService moduleService;

  @GetMapping(value = {"v1/role-modules"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Api to json tree for roles & modules for a given owner type and id")
  public List<RoleModulesResource> getRoleModulesJson(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestParam(value = "ownerType", required = false) OwnerType ownerType,
      @RequestParam(value = "ownerId", required = false) String ownerId,
      @RequestParam(value = "role", required = false) String role) {
    return moduleService.getRoleModulesJson(ownerType, ownerId, role);
  }

  @GetMapping(value = {"v1/role-modules/{roleId}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Api to get the modules list for a given role id")
  public List<RoleModulesList> getModulesListForRole(
      @RequestHeader(value = "Authorization", required = false) String auth, @PathVariable String roleId) {
    return moduleService.getModulesListForRole(roleId);
  }

  @PostMapping(value = {"v1/role-modules"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Api to add the modules list for a given role id")
  public RoleModuleListResource addModulesListForRole(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestBody RoleModuleListResource roleModuleList) {
    return moduleService.addModulesListForRole(roleModuleList);
  }

  @GetMapping(value = {"v1/modules"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Api to get all the modules")
  public List<ResourceModuleDetails> getModules(
      @RequestHeader(value = "Authorization", required = false) String auth) {
    return moduleService.getModules();
  }
}