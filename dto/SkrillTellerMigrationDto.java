// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Credentials;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Origin;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.Profile;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.SkrillAccessResources;

import lombok.Data;

import java.util.List;

import javax.validation.constraints.NotBlank;

@Data
public class SkrillTellerMigrationDto {
  @NotBlank
  private String userName;

  private String email;
  private UserStatus status;
  private String lastLoginDate;
  private Origin origin;
  private Profile profile;
  private List<SkrillAccessResources> accessResources;
  private Credentials credentials;
  private boolean isMailNotificationsEnabled = true;
  private String businessUnit;
}
