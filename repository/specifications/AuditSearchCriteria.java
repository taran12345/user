// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository.specifications;

import lombok.Data;

@Data
public class AuditSearchCriteria {
  private String key;
  private Object value;
  private AuditSearchOperation operation;

  /**
   * overrided constructor.
   */
  public AuditSearchCriteria(String key, Object value, AuditSearchOperation operation) {
    this.key = key;
    this.value = value;
    this.operation = operation;
  }
}
