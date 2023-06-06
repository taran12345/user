// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Profile {
  private String lockLevel;
  private String title;
  private String firstName;
  private String lastName;
  private String registrationIp;
  private String language;
  private String lockSublevel;
  private String lightRegistration;
  private String brand;
}