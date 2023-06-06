// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import com.paysafe.upf.user.provisioning.domain.RecoveryQuestion;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.HashedPassword;
import com.paysafe.upf.user.provisioning.web.rest.resource.usersummary.UserSummary;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class UserDto {
  private String userName;
  private String email;
  private String password;
  private HashedPassword hashedPassword;
  private String pmleId;
  private String businessUnit;
  private String firstName;
  private String lastName;
  private RoleDto roleDto;
  private AccessGroupDto accessGroupDto;
  private List<AccessResources> accessResources;
  private Map<String, Object> customProperties;
  private String id;
  private UserStatus status;
  private String mobilePhone;
  private Set<String> groupIds;
  private RecoveryQuestion recoveryQuestion;
  private Boolean activate;
  private String externalId;
  private String applicationName;
  private String ownerId;
  private String ownerType;
  private boolean isMigrated;
  private UserSummary userSummary;
  private Boolean isMailNotificationsEnabled;
  private boolean migrationUseCase = false;
  private String division;
  private List<String> userAssignedApplications;
}
