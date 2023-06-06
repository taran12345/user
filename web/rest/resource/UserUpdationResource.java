// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.domain.RecoveryQuestion;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.web.rest.resource.usersummary.UserSummary;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(Include.NON_NULL)
public class UserUpdationResource {
  private String userName;
  private String email;
  private String firstName;
  private String lastName;
  private String businessUnit;
  private List<RoleUpdationResource> roles;
  private List<AccessGroupUpdationResource> accessGroups;
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
  private String ownerId;
  private String ownerType;
  private String applicationName;
  private UserSummary userSummary;
  private boolean disableMailNotifications;
  private String region;
  private List<String> userAssignedApplications;
}
