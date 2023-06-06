// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.web.rest.dto.MigrationStatusDto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class BulkUserMigrationResponseResource {
  private List<MigrationStatusDto> failedUsers;
  int totalUsersCount;
  int failedUsersCount;
}
