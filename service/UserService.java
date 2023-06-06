// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.enums.OwnerType;
import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.UserStatusFilter;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserPasswordMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.ChangePasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetPasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.joda.time.DateTime;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {

  UserProvisioningUserResource createUser(UserDto userDto) throws JsonProcessingException;

  UserProvisioningUserResource updateUser(String userId, UserUpdationDto userUpdationDto)
      throws JsonProcessingException;

  IdentityManagementUserResource fetchUser(String userId);

  IdentityManagementUserResource migrateUser(String userId, UserPasswordMigrationDto userPasswordMigrationDto,
      String application);

  UsersListResponseResource getUsers(String loginName, String resourceName, String resourceId, String query,
      String ownerType, String ownerId, String application, Integer page, Integer pageSize,
      MutableBoolean isPartialSuccess);

  String changePassword(String userId, ChangePasswordRequestResource userChangePasswordRequestDto)
      throws JsonProcessingException;

  String resetPassword(String uuid, ResetPasswordRequestResource resetPasswordRequestResource, String application)
      throws JsonProcessingException;

  UsersListResponseResource getUsersByFilters(String application, String userIdentifier, UserStatusFilter userStatus,
      String role, List<String> roles, String createdBy, DateTime createdDate, ResourceType resourceName,
      String resourceId, OwnerType userType, Integer page, Integer pageSize, boolean merchantTypeValidation,
      boolean ignoreDisabledBrandCheck);

  void sendUserActivationEmail(String userId) throws JsonProcessingException;

  void validateLoginNameAndEmailAvailability(String loginName, String emailId);

  void updateUserStatus(String userId, UpdateUserStatusResource updateUserStatusResource)
      throws JsonProcessingException;

  List<String> getUserAccessGroupIds(String userName, String application);

  ResponseEntity<ByteArrayResource> downloadUserEmails(String application);

  Specification<User> checkApplicationAndConstructSpecification(
      UserFetchByFiltersRequestDto userFetchByFiltersRequestDto, boolean ignoreDisabledBrandCheck,
      boolean isEqualSearch);

  ResponseEntity<HttpStatus> resetFactor(String userId, String application);

  void updateMfaStatus(List<String> userIds, boolean mfaEnabled);

}
