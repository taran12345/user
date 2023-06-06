// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients;

import com.paysafe.upf.user.provisioning.config.FeignClientConfig;
import com.paysafe.upf.user.provisioning.web.rest.dto.BaseRoleDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomRoleDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

/**
 * PermissionServiceClient.
 */
@FeignClient(name = "paysafe-ss-permission", configuration = FeignClientConfig.class)
public interface PermissionServiceClient {

  /**
   * This API is used to create a role.
   */
  @RequestMapping(method = RequestMethod.POST, value = "admin/permission/v4/roles/",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<Object> createRole(@Valid @RequestBody CustomRoleDto roleDto);

  /**
   * Returns all roles.
   *
   * @param enabled - optional parameter.
   * @return list of Roles.
   */
  @RequestMapping(method = RequestMethod.GET, value = "admin/permission/v4/roles/",
      produces = "application/json; charset=utf-8")
  ResponseEntity<List<CustomRoleDto>> getAllRoles(@RequestHeader("Authorization") String token,
      @ApiParam(value = "Filter roles by their enabled status") @RequestParam(name = "enabled",
          required = false) Boolean enabled);

  /**
   * This API is used to modify a role.
   *
   * @param roleName of the role to modify.
   * @param roleResource contains the fields to be modified.
   * @return the updated role.
   */
  @RequestMapping(method = RequestMethod.PATCH, value = "admin/permission/v4/roles/",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<Object> modifyRole(@RequestHeader("Authorization") String token,
      @RequestParam(name = "roleName") final String roleName, @Valid @RequestBody BaseRoleDto roleResource);

  /**
   * Returns permissions for the given set of roles and/or categories.
   *
   * @param roles List of Strings containing roles for which permissions are required.
   * @return list of Permissions, grouped by role, then by category.
   */
  @RequestMapping(method = RequestMethod.GET, value = "admin/permission/v4/permissions/",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<Map<String, Object>> getPermissionsForRolesAndCategories(
      @RequestParam(name = "roleList", required = false) List<String> roles,
      @RequestParam(name = "skipValidation", required = false, defaultValue = "false") boolean skipValidation);

  @RequestMapping(path = "admin/permission/v4/roles/roleNames/", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<List<String>> getRoleNames();

}
