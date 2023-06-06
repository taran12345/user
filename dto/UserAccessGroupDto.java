// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupResource;

import lombok.Data;

@Data
public class UserAccessGroupDto {
  private String userName;
  private String email;
  private String businessUnit;
  private String firstName;
  private String lastName;
  private AccessGroupResource accessGroups;
}
