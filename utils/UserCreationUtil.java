// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import com.paysafe.upf.user.provisioning.config.OktaAppConfig;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAssignedApplications;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.repository.UserAssignedApplicationsRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.impl.UserServiceImpl;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserCreationUtil {

  @Autowired
  private OktaAppConfig oktaAppConfig;

  @Autowired
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Autowired
  private UserAssignedApplicationsRepository userAssignedApplicationsRepository;

  @Autowired
  private UserProvisioningUtils userProvisioningUtils;

  @Autowired
  private UserServiceImpl userServiceImpl;

  /**
   * This method gets the groupIds.
   */
  public Set<String> getGroupIds(Set<String> groupIds, String applicationName, String division,
      List<String> userAssignedApplications) {
    if (CollectionUtils.isEmpty(groupIds)) {
      String groupId = oktaAppConfig.getGroupIds().get(applicationName);
      if (CollectionUtils.isNotEmpty(userAssignedApplications)) {
        return getGroupIdsByAssignedApplications(userAssignedApplications);
      }
      if (StringUtils.isNotEmpty(division)) {
        groupId =
            oktaAppConfig.getGroupIds().get(division) != null ? oktaAppConfig.getGroupIds().get(division) : groupId;
      }
      return Collections.singleton(groupId);
    } else {
      return groupIds;
    }
  }

  private Set<String> getGroupIdsByAssignedApplications(List<String> userAssignedApplications) {
    Set<String> groupIds = new HashSet<>();
    for (String assignedApp : userAssignedApplications) {
      if (oktaAppConfig.getGroupIds().get(assignedApp) != null) {
        groupIds.add(oktaAppConfig.getGroupIds().get(assignedApp));
      }
    }
    return groupIds;
  }

  /**
   * Utility method to manage the user groups while updating the user.
   */
  public void handleUpdateUserGroupIds(User user, UserUpdationDto userUpdationDto) {
    List<UserAssignedApplications> userAssignedApplications = user.getUserAssignedApplications();
    Set<String> userInitialApps =
        userAssignedApplications.stream().map(UserAssignedApplications::getApplication).collect(Collectors.toSet());
    List<String> updatedApps = CollectionUtils.isNotEmpty(userUpdationDto.getUserAssignedApplications())
        ? userUpdationDto.getUserAssignedApplications()
        : new ArrayList<>(Arrays.asList(CommonThreadLocal.getAuthLocal().getApplication()));
    Set<String> updatedAppsSet = new HashSet<>(updatedApps);
    updatedAppsSet.removeAll(userInitialApps);
    userInitialApps.removeAll(updatedApps);
    for (String newApp : updatedAppsSet) {
      if (oktaAppConfig.getGroupIds().get(newApp) != null) {
        identityManagementFeignClient.addUserToGroup(oktaAppConfig.getGroupIds().get(newApp), user.getUserExternalId());
        UserAssignedApplications userAssignedApplication = new UserAssignedApplications();
        userAssignedApplication.setApplication(newApp);
        userAssignedApplication.setUserId(user.getUserId());
        userAssignedApplicationsRepository.save(userAssignedApplication);
      }
    }
    for (String deleteApp : userInitialApps) {
      if (oktaAppConfig.getGroupIds().get(deleteApp) != null) {
        identityManagementFeignClient.removeUserFromGroup(oktaAppConfig.getGroupIds().get(deleteApp),
            user.getUserExternalId());
        userAssignedApplicationsRepository.deleteByUserIdAndApplication(user.getUserId(), deleteApp);
      }
    }
  }

  /**
   * Utility method to validate user request while creating the user.
   */
  public void validateCreateUserRequest(UserDto userDto) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    if (org.apache.commons.lang.StringUtils.equals(application, DataConstants.SKRILL)
            || org.apache.commons.lang.StringUtils.equals(application, DataConstants.NETELLER)) {
      userProvisioningUtils.validateCreateUserWalletResources(userDto.getAccessResources(), userDto);
    }
    if (userDto.getRoleDto() != null && (CollectionUtils.isNotEmpty(userDto.getRoleDto().getCustomRoles())
            || CollectionUtils.isNotEmpty(userDto.getRoleDto().getExistingRoles()))) {
      userServiceImpl.validateRoles(userDto.getRoleDto());
    }
    if (userDto.getAccessGroupDto() != null
            && (CollectionUtils.isNotEmpty(userDto.getAccessGroupDto().getCustomAccessGroupDtos())
            || CollectionUtils.isNotEmpty(userDto.getAccessGroupDto().getExistingAccessGroupIds()))) {
      userServiceImpl.validateAccessGroups(userDto.getAccessGroupDto());
    }
  }

  /**
   * Utility method to validate user request while updating the user.
   */
  public void validateUpdateUserRequest(UserUpdationDto userUpdationDto) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    if (org.apache.commons.lang.StringUtils.equals(application, DataConstants.SKRILL)
            || org.apache.commons.lang.StringUtils.equals(application, DataConstants.NETELLER)) {
      userProvisioningUtils.validateUpdateUserWalletResources(userUpdationDto.getAccessResources(), userUpdationDto);
    }
    if (checkRolesToAdd(userUpdationDto)) {
      userServiceImpl.validateRoles(userUpdationDto.getRolesToAdd());
    }
    if (checkAccessGroupToAdd(userUpdationDto)) {
      userServiceImpl.validateAccessGroups(userUpdationDto.getAccessGroupsToAdd());
    }
  }

  /**
   * Utility method to validate roles.
   */
  private boolean checkRolesToAdd(UserUpdationDto userUpdationDto) {
    return (userUpdationDto.getRolesToAdd() != null
            && CollectionUtils.isNotEmpty(userUpdationDto.getRolesToAdd().getCustomRoles()))
            || (userUpdationDto.getRolesToAdd() != null
            && CollectionUtils.isNotEmpty(userUpdationDto.getRolesToAdd().getExistingRoles()));
  }

  /**
   * Utility method to validate accessGroups.
   */
  private boolean checkAccessGroupToAdd(UserUpdationDto userUpdationDto) {
    return (userUpdationDto.getAccessGroupsToAdd() != null
            && CollectionUtils.isNotEmpty(userUpdationDto.getAccessGroupsToAdd().getCustomAccessGroupDtos()))
            || (userUpdationDto.getAccessGroupsToAdd() != null
            && CollectionUtils.isNotEmpty(userUpdationDto.getAccessGroupsToAdd().getExistingAccessGroupIds()));
  }
}
