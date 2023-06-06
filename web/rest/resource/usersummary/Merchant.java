// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.usersummary;

import lombok.Data;

import java.util.List;

@Data
public class Merchant {

  private MerchantInfo merchantInfo;
  private List<String> accounts;
  private String role;
  private List<String> permissions;
}
