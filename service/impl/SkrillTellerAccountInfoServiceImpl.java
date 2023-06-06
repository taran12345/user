// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static com.paysafe.op.commons.framework.springboot.StartupHelper.logger;

import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.enums.IncludeParam;
import com.paysafe.upf.user.provisioning.service.SkrillTellerAccountInfoService;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.BulkWalletDetailResponse;
import com.paysafe.upf.user.provisioning.web.rest.dto.BulkWalletRequestDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.ReportScheduleResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.ContactEmail;
import com.paysafe.upf.user.provisioning.web.rest.resource.SkrillContactEmailsResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.GroupedCustomerIdsResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.LinkedCustomerIdsResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class SkrillTellerAccountInfoServiceImpl implements SkrillTellerAccountInfoService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SkrillTellerAccountInfoServiceImpl.class);
  private static final String LOG_MESSAGE = "request customer details by id";
  private static final String CUSTOMER_ID = "customerId";
  private static final String SKRILLTELLER_MERCHANT_GROUPS_URI =
      "/customers/api/customers/{customerId}/grouped-accounts";
  private static final String SKRILLTELLER_MERCHANT_LINKED_URI =
      "/customers/api/customers/{customerId}/linked-customers";
  private static final String SKRILLTELLER_CONTACT_EMAILS_URI = "/customers/api/customers/{customerId}/contact-email";
  private static final String SKRILLTELLER_WALLET_WITH_NAME_URI =
      "/wallet/api/customers/{customerId}?" + "include=business-profile&include=linked-customer-parent";
  private static final String SKRILLTELLER_WALLET_BULK_API_URI = "/customers/api/customers/bulk/load";

  private static final String SKRILLTELLER_GET_SCHEDULES_URI =
      "/api/schedules/report-schedule/tenants/paysafe-unity/owners/{ownerId}";

  private static final String SCHEDULE_REPORT_UPDATE_URI =
      "/api/schedules/report-schedule/tenants/paysafe-unity/owners/{ownerId}/schedules/{scheduleId}";

  @Autowired
  @Qualifier("externalRestTemplate")
  private RestTemplate externalRestTemplate;

  @Autowired
  private SkrillTellerConfig skrillTellerConfig;

  @Async("upfAsyncExecutor")
  @Override
  public CompletableFuture<BasicWalletInfo> fetchWalletInfoAsync(String walletId) {

    Map<String, String> urlParams = new HashMap<>();
    urlParams.put(DataConstants.CUSTOMER_ID, walletId);
    HttpEntity<?> requestEntity = getHttpEntity();

    LOGGER.info(LOG_MESSAGE);
    String walletInfoByIdUrl = skrillTellerConfig.getHostUri() + SKRILLTELLER_WALLET_WITH_NAME_URI;
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(walletInfoByIdUrl);

    try {
      ResponseEntity<BasicWalletInfo> response = externalRestTemplate.exchange(builder.buildAndExpand(urlParams)
          .toUri(), HttpMethod.GET, requestEntity, BasicWalletInfo.class);
      LOGGER.info("Successfully fetched wallet details by id");
      return CompletableFuture.completedFuture(response.getBody());
    } catch (NotFoundException e) {
      LOGGER.error("No info fetched for the wallet ID: {}", walletId);
      return CompletableFuture.completedFuture(new BasicWalletInfo());
    }
  }

  private HttpEntity<?> getHttpEntity() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    return new HttpEntity<>(headers);
  }

  private <T> HttpEntity<T> getHttpEntity(T requestBody) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    return new HttpEntity<>(requestBody, headers);
  }

  @Override
  public LinkedCustomerIdsResource getMerchantLinkedToWalletId(String walletId) {
    HttpEntity<?> requestEntity = getHttpEntity();

    Map<String, String> urlParams = new HashMap<>();
    urlParams.put(CUSTOMER_ID, walletId);

    String linkedCustomersUrl = skrillTellerConfig.getHostUri() + SKRILLTELLER_MERCHANT_LINKED_URI;
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(linkedCustomersUrl);

    ResponseEntity<LinkedCustomerIdsResource> response = externalRestTemplate.exchange(
        builder.buildAndExpand(urlParams).toUri(), HttpMethod.GET, requestEntity, LinkedCustomerIdsResource.class);
    LOGGER.info("Successfully retrieved merchant group");
    return response.getBody();
  }

  @Override
  public GroupedCustomerIdsResource getMerchantGroupByWalletId(String walletId) {
    HttpEntity<?> requestEntity = getHttpEntity();

    Map<String, String> urlParams = new HashMap<>();
    urlParams.put(CUSTOMER_ID, walletId);

    String groupedAccountsUrl = skrillTellerConfig.getHostUri() + SKRILLTELLER_MERCHANT_GROUPS_URI;
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(groupedAccountsUrl);

    ResponseEntity<GroupedCustomerIdsResource> response = externalRestTemplate.exchange(
        builder.buildAndExpand(urlParams).toUri(), HttpMethod.GET, requestEntity, GroupedCustomerIdsResource.class);
    LOGGER.info("Successfully retrieved merchant group");
    return response.getBody();
  }

  @Override
  public SkrillContactEmailsResource getSkrillContactEmails(String walletId) {
    HttpEntity<?> requestEntity = getHttpEntity();
    Map<String, String> urlParams = new HashMap<>();
    urlParams.put(CUSTOMER_ID, walletId);
    String groupedAccountsUrl = skrillTellerConfig.getHostUri() + SKRILLTELLER_CONTACT_EMAILS_URI;
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(groupedAccountsUrl);
    ResponseEntity<List<ContactEmail>> response =
        externalRestTemplate.exchange(builder.buildAndExpand(urlParams).toUri(), HttpMethod.GET, requestEntity,
            new ParameterizedTypeReference<List<ContactEmail>>() {
            });
    LOGGER.info("Successfully retrieved skrill contact-emails");
    SkrillContactEmailsResource skrillContactEmails = new SkrillContactEmailsResource();
    skrillContactEmails.setContactEmails(response.getBody());
    return skrillContactEmails;
  }

  @Override
  public BulkWalletDetailResponse fetchBulkWalletInfo(Set<String> walletIds, IncludeParam... includeParams) {
    final HttpEntity<BulkWalletRequestDto> requestEntity =
        getHttpEntity(formBulkWalletRequestObject(walletIds, includeParams));
    String walletInfoByIdUrl = skrillTellerConfig.getHostUri() + SKRILLTELLER_WALLET_BULK_API_URI;
    LOGGER.info("Skrill fetchBulkWalletInfo URL: {}", walletInfoByIdUrl);
    ResponseEntity<BulkWalletDetailResponse> response = externalRestTemplate.exchange(walletInfoByIdUrl,
        HttpMethod.POST, requestEntity, BulkWalletDetailResponse.class);

    LOGGER.info("Successfully fetched wallet details by id");
    return response.getBody();
  }

  @Async("upfAsyncExecutor")
  @Override
  public CompletableFuture<BulkWalletDetailResponse> fetchBulkWalletInfoAsync(Set<String> walletIds,
      IncludeParam... includeParams) {
    final HttpEntity<BulkWalletRequestDto> requestEntity =
        getHttpEntity(formBulkWalletRequestObject(walletIds, includeParams));
    String walletInfoByIdUrl = skrillTellerConfig.getHostUri() + SKRILLTELLER_WALLET_BULK_API_URI;
    LOGGER.info("Skrill fetchBulkWalletInfo URL: {}", walletInfoByIdUrl);
    ResponseEntity<BulkWalletDetailResponse> response = externalRestTemplate.exchange(walletInfoByIdUrl,
        HttpMethod.POST, requestEntity, BulkWalletDetailResponse.class);
    LOGGER.info("Successfully fetched wallet details by id");
    return CompletableFuture.completedFuture(response.getBody());
  }

  private BulkWalletRequestDto formBulkWalletRequestObject(Set<String> walletIds, IncludeParam... includeParams) {
    List<String> includes = Objects.isNull(includeParams) ? new ArrayList<>() :
        Arrays.asList(includeParams).stream().map(IncludeParam::getValue).collect(Collectors.toList());
    return new BulkWalletRequestDto(walletIds, includes);
  }

  @Override
  public ResponseEntity<HttpStatus> deleteScheduleReport(String ownerId, String scheduleId) {
    HttpEntity<?> httpEntity = getHttpEntity();

    String scheduleReportDeleteUrl = skrillTellerConfig.getHostUri() + SCHEDULE_REPORT_UPDATE_URI;
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(scheduleReportDeleteUrl);
    ResponseEntity<HttpStatus> response =
        externalRestTemplate.exchange(builder.buildAndExpand(ownerId, scheduleId).toUri(), HttpMethod.DELETE,
            httpEntity, HttpStatus.class);
    LOGGER.info("Schedule has been deleted for userId: " + ownerId + " scheduleId: " + scheduleId);

    return new ResponseEntity<>(response.getStatusCode());
  }

  @Override
  public ReportScheduleResponse getSchedules(String ownerId,
      String customerId) {

    Map<String, String> urlParams = new HashMap<>();
    urlParams.put("ownerId", ownerId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
    String getScheduleUri = skrillTellerConfig.getHostUri() + SKRILLTELLER_GET_SCHEDULES_URI;
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromUriString(getScheduleUri).queryParam(CUSTOMER_ID, customerId);


    HttpEntity<HttpStatus> downloadRequestEntity = new HttpEntity<>(headers);
    ResponseEntity<ReportScheduleResponse> response =
        externalRestTemplate.exchange(builder.build(urlParams),
            HttpMethod.GET, downloadRequestEntity, ReportScheduleResponse.class);

    LOGGER.info("Got the response for download skrill balance summary");

    return response.getBody();
  }

}
