// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller;

import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.SkrillPermissions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkrillAccessResources {
  private String resourceType;
  private String resourceId;
  private String role;
  private AccessResourceStatus status;
  private List<SkrillPermissions> permissions;
}
