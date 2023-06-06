// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.audit;

import com.paysafe.upf.user.provisioning.enums.AuditEventType;

import lombok.Builder;
import lombok.Data;

import org.joda.time.DateTime;

@Data
@Builder
public class AuditInfoRequestResource {
  private String createdBy;
  private String targetUserName;
  private String targetUserId;
  private DateTime fromDate;
  private DateTime toDate;
  private AuditEventType eventType;
  private String sourceApp;
  private String application;
  private Integer page;
  private Integer pageSize;
}
