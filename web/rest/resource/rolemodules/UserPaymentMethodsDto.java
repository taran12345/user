// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class UserPaymentMethodsDto {

  private String accountId;
  private String qualifier;
  private String paymentMethod;
  private String currencyCode;
  private Mcc mcc;
  private JsonNode accountConfiguration;

  @Data
  public static class Mcc {
    private String code;
    private String description;
  }

}
