// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Origin;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Profile;
import com.paysafe.upf.user.provisioning.web.rest.resource.usersummary.UserSummary;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UserMigrationResponseResource extends IdentityManagementUserResource {
  private List<MigrationAccessResources> accessResources;
  private UserSummary userSummary;
  private String lastLoginDate;
  private Origin origin;
  private Profile profile;
}
