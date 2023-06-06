// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.joda.time.DateTime;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditUserEventDto {
  private String id;
  private AuditEventType eventType;
  private String targetUserName;
  private String targetUserId;
  private AuditEventStatus eventStatus;
  private String browser;
  private String userIpAddress;
  private String sourceApp;
  private String userStatus;
  private String application;
  private String eventData;
  private String createdBy;
  private List<AuditUserEventResourceDto> auditUserEventResources;
  private DateTime eventTimeStamp;
  private DateTime createdDate;
  private Long eventTimeStampMillis;
  private String correlationId;

}
