// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantSearchRequest {
  private String origin;
  private List<String> merchantIds;
  private List<String> merchantLegalEntityIds;
  private String merchantLegalEntityId;
  private List<String> pmleId;
  private String partnerId;
  private List<String> paymentAccountId;
  private List<String> accountGroups;
  private Integer offset = 0;
  private Integer limit = 100;

}
