// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleResource {

  private Integer displayOrder;
  private String label;
  private String descriptions;
  private String selectionMode;
  private boolean showExpand;
  @JsonProperty(value = "isSelected")
  private boolean isSelected;
  @JsonProperty(value = "isExpand")
  private boolean isExpand;
  private List<String> permissionsList;
  private List<String> isShow;
  List<ModuleResource> subModules;
  private boolean editable;
  private String id;
}
