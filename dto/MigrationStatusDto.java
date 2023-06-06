// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import com.paysafe.upf.user.provisioning.enums.MigrationStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MigrationStatusDto {
  private String userName;
  private String ownerId;
  private String ownerType;
  private String isAdmin;
  MigrationStatus status;
  String reason;
  String errorCode;

  @Override
  public String toString() {
    return "MigrationStatusDto [userName=" + userName + ", reason=" + reason + ", status=" + status + ", errorCode="
        + errorCode + "]";
  }
}
