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
public class MerchantSearchAfterRequest {

  private Integer offset = 0;
  private Integer limit;
  private FilterParams filterParams;
  private SearchParams searchParams;
  private List<String> responseFields;
  private String sortField;
  private String sortOrder;
  private String searchAfter;
  private String operator;
}
