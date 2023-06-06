// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessResources {

  private String id;
  private String walletName;
  private String accessGroupId;
  private AccessGroupType accessGroupType;
  private String type;
  private AccessResourceStatus status;
  private String role;
  private List<PermissionDto> permissions;
  private List<String> ids;
  private String ownerId;
  private String ownerType;
  private String region;
}
