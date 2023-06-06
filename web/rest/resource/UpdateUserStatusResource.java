// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.enums.UserAction;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UpdateUserStatusResource {

  @NotBlank
  @Email
  private String email;

  @NotBlank
  private String userName;

  private String accessGroupId;

  private UserAction action;

  private String resourceId;

  private String resourceType;

  private boolean isStatusUpdate = false;

  private String applicationName;
}
