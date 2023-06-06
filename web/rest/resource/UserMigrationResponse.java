// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserMigrationResponse {
  String status;

  @Override
  public String toString() {
    return "{ \"status\":" + "\"" + status + "\"" + "}";
  }

}
