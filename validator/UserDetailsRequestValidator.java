// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.validator;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.UserAction;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.PermissionDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserAccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserUpdationResource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
public class UserDetailsRequestValidator {

  private static final String ID_AND_IDS_PRESENT_ERROR_MESSAGE = "id and ids, both are present in the request";
  private static final String REGULAR_ROLE_AND_EMPTY_PERMISSIONS_ERROR_MESSAGE =
      "For REGULAR role, permissions list can not be empty/null";
  private static final String ID_AND_IDS_EMPTY_ERROR_MESSAGE = "Atleast one of id or ids must be provided";
  private static final String RESOURCE_ID_EMPTY_FOR_RESOURCE_NAME_ERROR_MESSAGE = "resourceId input must be provided";

  /**
   * create user request validation.
   */
  public void valiadteCreateUserRequest(UserResource userResource) {
    validateAccessResources(userResource.getAccessResources());
  }

  /**
   * update user request validation.
   */
  public void validateUpdateUserRequest(UserUpdationResource userUpdationResource) {
    if (CollectionUtils.isNotEmpty(userUpdationResource.getAccessResources())) {
      removeDuplicatePermissionsFromUpdateRequest(userUpdationResource.getAccessResources());
      for (UpdateUserAccessResources accessResource : userUpdationResource.getAccessResources()) {
        validateResouceIdentifier(accessResource);
        checkPermissionsForCustomRole(accessResource);
      }
    }
  }

  private void removeDuplicatePermissionsFromUpdateRequest(
      List<UpdateUserAccessResources> updateUserAccessResourcesList) {
    for (UpdateUserAccessResources updateUserAccessResources : updateUserAccessResourcesList) {
      List<PermissionDto> permissionList = updateUserAccessResources.getPermissions();
      removeDuplicatePermissions(permissionList);
    }
  }

  private void removeDuplicatePermissions(List<PermissionDto> permissionList) {
    if (CollectionUtils.isNotEmpty(permissionList)) {
      Set<String> permissionSet = new HashSet<>();
      for (Iterator<PermissionDto> iterator = permissionList.iterator(); iterator.hasNext();) {
        PermissionDto permissionDto = iterator.next();
        if (permissionSet.contains(permissionDto.getLabel())) {
          iterator.remove();
        } else {
          permissionSet.add(permissionDto.getLabel());
        }
      }
    }
  }

  private void validateAccessResources(List<AccessResources> accessResources) {
    if (!CollectionUtils.isEmpty(accessResources)) {
      removeDuplicatePermissionsFromCreateRequest(accessResources);
      for (AccessResources accessResource : accessResources) {
        validateResouceIdentifier(accessResource);
        checkPermissionsForCustomRole(accessResource);
      }
    }
  }

  private void removeDuplicatePermissionsFromCreateRequest(List<AccessResources> accessResourceList) {
    if (CollectionUtils.isNotEmpty(accessResourceList)) {
      for (AccessResources accessResources : accessResourceList) {
        List<PermissionDto> permissionList = accessResources.getPermissions();
        removeDuplicatePermissions(permissionList);
        accessResources.setPermissions(permissionList);
      }
    }
  }

  private void validateResouceIdentifier(AccessResources accessResource) {
    if (StringUtils.isNotEmpty(accessResource.getId()) && !CollectionUtils.isEmpty(accessResource.getIds())) {
      throw new BadRequestException.Builder().details(ID_AND_IDS_PRESENT_ERROR_MESSAGE)
          .errorCode(CommonErrorCode.INTERNAL_ERROR).build();
    }
    if (StringUtils.isEmpty(accessResource.getId()) && CollectionUtils.isEmpty(accessResource.getIds())) {
      throw new BadRequestException.Builder().details(ID_AND_IDS_EMPTY_ERROR_MESSAGE)
          .errorCode(CommonErrorCode.INTERNAL_ERROR).build();
    }
  }

  private void checkPermissionsForCustomRole(AccessResources accessResource) {
    if (StringUtils.equals(accessResource.getRole(), DataConstants.REGULAR)
        && CollectionUtils.isEmpty(accessResource.getPermissions())) {
      throw new BadRequestException.Builder().details(REGULAR_ROLE_AND_EMPTY_PERMISSIONS_ERROR_MESSAGE)
          .errorCode(CommonErrorCode.INTERNAL_ERROR).build();
    }
  }

  /**
   * filter user request validation.
   */
  public void validateApplicationAndResourceNameAndId(String application, ResourceType resourceName,
      String resourceId) {
    if (StringUtils.isEmpty(resourceId)) {
      throw new BadRequestException.Builder().details(RESOURCE_ID_EMPTY_FOR_RESOURCE_NAME_ERROR_MESSAGE)
          .errorCode(CommonErrorCode.INTERNAL_ERROR).build();
    }
    if (!application.equals(DataConstants.PORTAL)
        && !(resourceName.equals(ResourceType.WALLETS) || resourceName.equals(ResourceType.USER))) {
      throw new BadRequestException.Builder().details("Invalid resource type for the provided application type")
          .build();
    }
  }

  /**
   * This method validates the V1 hierarchy API request.
   */
  public void validateV1HierarchyRequest(String ownerId, String ownerType) {
    if (isNullOrEmptyOrUndefinedField(ownerId) || isNullOrEmptyOrUndefinedField(ownerType)) {
      throw new BadRequestException.Builder().details("Invalid ownerId/ownerType value provided").build();
    }
  }

  private boolean isNullOrEmptyOrUndefinedField(String field) {
    return StringUtils.isEmpty(field) || StringUtils.equalsIgnoreCase(DataConstants.UNDEFINED, field)
        || StringUtils.equalsIgnoreCase(DataConstants.NULL, field);
  }

  /**
   * This method validates the update-user-status request.
   */
  public void validateUpdateUserStatusRequest(UpdateUserStatusResource updateUserStatusResource) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    boolean isInalidRequest = true;
    if (StringUtils.equalsIgnoreCase(DataConstants.SKRILL, application)
        || StringUtils.equalsIgnoreCase(DataConstants.NETELLER, application)) {
      if (UserAction.ACTIVATE.equals(updateUserStatusResource.getAction())
          || UserAction.ACTIVE_ALL.equals(updateUserStatusResource.getAction())
          || UserAction.BLOCKED.equals(updateUserStatusResource.getAction())
          || UserAction.BLOCK_ALL.equals(updateUserStatusResource.getAction())) {
        isInalidRequest = false;
      }
    } else {
      if (UserAction.ACTIVATE.equals(updateUserStatusResource.getAction())
          || UserAction.BLOCKED.equals(updateUserStatusResource.getAction())) {
        isInalidRequest = false;
      }
    }
    if (isInalidRequest) {
      throw new BadRequestException.Builder().details("Invalid user-status action").build();
    }
  }
}
