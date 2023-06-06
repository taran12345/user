// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import lombok.Data;

import java.util.Set;

@Data
public class ReportSchedule {
  private String id;
  private Long customerId;
  private String reportType;
  private String sendFrequency;
  private String fileFormat;
  private ReportScheduleStatus status;
  private boolean allAccounts;
  private Set<Long> accountIds;
  private Recipients recipients;
}
