// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterParams {

  private List<String> merchantLegalEntityId;
  private List<String> pmleId;
  private List<String> processingAccountId;
  private List<String> partnerId;
  private List<String> merchantId;
  private List<String> accountGroups;
  private List<String> paymentAccountId;
  private List<String> origin;
  private List<String> merchantName;
  private List<String> partnerName;
  private List<String> merchantLegalEntity;
  private List<String> pmleName;
  private List<String> businessRelationName;

}
