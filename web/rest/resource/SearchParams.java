// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchParams {

  private String merchantLegalEntityId;
  private String pmleId;
  private String processingAccountId;
  private String partnerId;
  private String merchantId;
  private String merchantName;
  private String accountGroups;
  private String paymentAccountId;
  private String partnerName;
  private String merchantLegalEntity;
  private String pmleName;
  private String businessRelationName;
}
