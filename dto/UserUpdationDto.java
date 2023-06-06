// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import com.paysafe.upf.user.provisioning.domain.RecoveryQuestion;
import com.paysafe.upf.user.provisioning.enums.RegionType;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserAccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.usersummary.UserSummary;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class UserUpdationDto {
  private String userName;
  private String email;
  private String firstName;
  private String lastName;
  private String businessUnit;
  private RoleDto rolesToAdd;
  private List<String> rolesToDelete;
  private AccessGroupDto accessGroupsToAdd;
  private List<String> accessGroupsToDelete;
  private List<String> accessGroupsToHardDelete;
  private String pmleId;
  private String password;
  private Map<String, Object> customProperties;
  private String id;
  private UserStatus status;
  private String mobilePhone;
  private Set<String> groupIds;
  private RecoveryQuestion recoveryQuestion;
  private Boolean activate;
  private String externalId;
  private List<UpdateUserAccessResources> accessResources;
  private String applicationName;
  private String ownerId;
  private String ownerType;
  private UserSummary userSummary;
  private boolean isStatusUpdate;
  private boolean disableMailNotifications;
  private RegionType region;
  private List<String> userAssignedApplications;
}
