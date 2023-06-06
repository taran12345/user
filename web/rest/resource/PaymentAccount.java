// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAccount {
  private String resourceType;
  private String resourceId;
  private String fmaId;
  private String fmaStatus;
  private String fmaCurrency;
  private String fmaType;
  private String settlementCurrency;
  private ProcessingAccount.BusinessDetails businessDetails;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    PaymentAccount that = (PaymentAccount) obj;
    return Objects.equals(fmaId, that.fmaId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fmaId);
  }
}
