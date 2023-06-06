// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Credentials;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Origin;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Profile;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.SkrillAccessResources;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMigrationResource {
  @NotBlank
  private String userName;
  private String password;
  private String ownerType;
  private String ownerId;
  private String isAdmin;
  private String email;
  private UserStatus status;
  private boolean isUSigaming = false;
  private String lastLoginDate;
  private Origin origin;
  private Profile profile;
  private List<SkrillAccessResources> accessResources;
  private Credentials credentials;
  private boolean isMailNotificationsEnabled = true;
  private String businessUnit;
  private String division;
  private List<String> assignedApplications;
}
