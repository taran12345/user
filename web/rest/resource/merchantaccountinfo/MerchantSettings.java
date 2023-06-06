// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class MerchantSettings {

  @JsonProperty(value = "isMerchant")
  private boolean isMerchant;

  private String merchantTransferCode;

  private MerchantTools merchantTools;
}