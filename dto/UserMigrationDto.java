// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserMigrationDto {
  private String userName;
  private String password;
  private String ownerType;
  private String ownerId;
  private String isAdmin;
  private boolean isUSigaming = false;
  private String businessUnit;
  private String division;
  private List<String> assignedApplications;
  private boolean ignorePermissionUpdate;
}
