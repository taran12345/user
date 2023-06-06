// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.enums.IncludeParam;
import com.paysafe.upf.user.provisioning.web.rest.dto.BulkWalletDetailResponse;
import com.paysafe.upf.user.provisioning.web.rest.dto.ReportScheduleResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.SkrillContactEmailsResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.GroupedCustomerIdsResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.LinkedCustomerIdsResource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface SkrillTellerAccountInfoService {

  CompletableFuture<BasicWalletInfo> fetchWalletInfoAsync(String walletId);

  LinkedCustomerIdsResource getMerchantLinkedToWalletId(String walletId);

  GroupedCustomerIdsResource getMerchantGroupByWalletId(String walletId);

  SkrillContactEmailsResource getSkrillContactEmails(String walletId);

  BulkWalletDetailResponse fetchBulkWalletInfo(Set<String> walletIds, IncludeParam... includeParams);

  CompletableFuture<BulkWalletDetailResponse> fetchBulkWalletInfoAsync(Set<String> walletIds,
      IncludeParam... includeParams);

  ResponseEntity<HttpStatus> deleteScheduleReport(String ownerId, String scheduleId);

  ReportScheduleResponse getSchedules(String ownerId,
      String customerId);
}
