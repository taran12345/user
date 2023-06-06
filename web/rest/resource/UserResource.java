// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.domain.RecoveryQuestion;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
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

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(Include.NON_NULL)
public class UserResource {
  @NotBlank
  private String userName;

  @NotBlank
  @Email
  private String email;

  private String pmleId;
  private String businessUnit;
  private String firstName;
  private String password;
  private Map<String, Object> customProperties;
  private String lastName;
  private List<String> roleIds;
  private List<CustomRoleResource> roles;
  private List<String> accessGroupsIds;
  private List<AccessResources> accessResources;
  private List<CustomAccessGroupResource> accessGroups;
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
  private String division;
  private List<String> userAssignedApplications;
}
