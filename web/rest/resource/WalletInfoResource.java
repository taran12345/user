// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WalletInfoResource {
  private String name;
  private String id;
  private List<String> settlementCurrency;
  private Long userWithAccess; 
  private Long regularUser; 
  private Long admins;
  private String brand;
}
