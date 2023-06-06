// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients;

import com.paysafe.upf.user.provisioning.config.FeignClientConfig;
import com.paysafe.upf.user.provisioning.enums.UserAction;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUpdateUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserListResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.LogoutRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.OktaChangePasswordRequestResource;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "paysafe-upf-identity-management", configuration = FeignClientConfig.class)
public interface IdentityManagementFeignClient {

  @RequestMapping(value = "/admin/identity-management/v1/users", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<IdentityManagementUserResource> createUser(@RequestBody IdentityManagementUserResource request);

  @RequestMapping(value = "/admin/identity-management/v1/users/{userId}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<IdentityManagementUserResource> getUser(@PathVariable(value = "userId") String userId,
      @RequestHeader(value = "Application") String applicationHeader);

  @RequestMapping(value = "/admin/identity-management/v1/users/userIds", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  List<IdentityManagementUserResource> getUsersList(@RequestParam(value = "userIds") List<String> userIds,
      @RequestHeader(value = "Application") String applicationHeader);

  @RequestMapping(value = "/admin/identity-management/v1/users/{userId}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<JsonNode> getUserJsonNode(@PathVariable(value = "userId") String userId,
      @RequestHeader(value = "Application") String applicationHeader);

  @RequestMapping(method = RequestMethod.GET, value = "/admin/identity-management/v1/users")
  IdentityManagementUserListResource getUsersByUserName(@RequestParam(value = "userName") String userName);

  @RequestMapping(method = RequestMethod.GET, value = "/admin/identity-management/v1/users")
  IdentityManagementUserListResource getUsersByEmail(@RequestParam(value = "email") String email);

  @RequestMapping(value = "/admin/identity-management/v1/users/{userId}", method = RequestMethod.PATCH,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<IdentityManagementUserResource> updateUser(@PathVariable(value = "userId") String userId,
      @RequestBody IdentityManagementUpdateUserResource request);

  @RequestMapping(value = "/admin/identity-management/v1/users/{userId}/credentials/change_password",
      method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<String> changePassword(
      @RequestBody OktaChangePasswordRequestResource oktaChangePasswordRequestResource,
      @PathVariable(value = "userId") String userId);

  @RequestMapping(value = "/admin/identity-management/v1/logout", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<Void> internalLogout(@RequestBody LogoutRequestResource logoutRequestResource);

  @RequestMapping(value = "/admin/identity-management/v1/users/{userId}/{userAction}", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  String updateUserStatus(@PathVariable(value = "userId") String userId,
      @PathVariable(value = "userAction") UserAction action, @RequestBody IdentityManagementUserResource userResource);

  @RequestMapping(value = "/admin/identity-management/v1/users/{userId}", method = RequestMethod.DELETE)
  ResponseEntity<HttpStatus> deleteUser(@PathVariable(value = "userId") String userId);

  @RequestMapping(value = "/admin/identity-management/v1/groups/{groupId}/users/{userId}", method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<HttpStatus> addUserToGroup(@PathVariable(value = "groupId") String groupId,
      @PathVariable(value = "userId") String userId);

  @RequestMapping(value = "/admin/identity-management/v1/groups/{groupId}/users/{userId}",
      method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<HttpStatus> removeUserFromGroup(@PathVariable(value = "groupId") String groupId,
      @PathVariable(value = "userId") String userId);

  @RequestMapping(value = "/admin/identity-management/v1/users/{userId}/reactivate", method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  ResponseEntity<HttpStatus> activateUser(@PathVariable(value = "userId") String userId);

  @RequestMapping(value = "/admin/identity-management/v1/users/{userId}/factors", method = RequestMethod.DELETE)
  ResponseEntity<HttpStatus> resetFactor(@PathVariable(value = "userId") String userId,
      @RequestHeader(value = "Application") String applicationHeader);
}
