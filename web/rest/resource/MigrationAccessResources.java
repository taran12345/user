// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.enums.AccessGroupType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MigrationAccessResources {

  private String id;
  private String accessGroupId;
  private AccessGroupType accessGroupType;
  private String type;
  private String status;
  private String role;
  private List<String> permissions;
  private List<String> ids;
  private String ownerId;
  private String ownerType;
  private String resourceType;
  private String resourceId;
}
