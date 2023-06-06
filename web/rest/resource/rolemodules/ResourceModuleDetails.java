// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceModuleDetails {
  private String moduleid;
  private String label;
  private boolean isSelected;
  private String descriptions;
  private String parentid;
  private String selectionMode;
  private boolean showExpand;
  private boolean isExpand;
  private Integer displayOrder;
  private boolean editable;
  private boolean enabled;
  private List<ResourceModulePermissions> permissions;
  private List<ResourceModuleAccessLevel> moduleAccessLevel;
}
