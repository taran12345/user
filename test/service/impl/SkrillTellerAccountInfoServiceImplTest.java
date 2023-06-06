// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.enums.IncludeParam;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.dto.BulkWalletDetailResponse;
import com.paysafe.upf.user.provisioning.web.rest.dto.ReportScheduleResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.ContactEmail;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.GroupedCustomerIdsResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.LinkedCustomerIdsResource;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SkrillTellerAccountInfoServiceImplTest {

  @Mock
  @Qualifier("externalRestTemplate")
  private RestTemplate externalRestTemplate;

  @Mock
  private SkrillTellerConfig skrillTellerConfig;

  @InjectMocks
  private SkrillTellerAccountInfoServiceImpl skrillTellerAccountInfoServiceImpl;

  /**
   * Initial setup.
   */
  @Before
  public void setUp() {
    AuthorizationInfo authorizationInfo = new AuthorizationInfo();
    authorizationInfo.setApplication("SKRILL");
    CommonThreadLocal.setAuthLocal(authorizationInfo);
  }

  @Test
  public void fetchWalletInfoAsync_withValidData_shouldSucceed() {
    when(skrillTellerConfig.getHostUri()).thenReturn("http://api.skrillteller.com");
    when(externalRestTemplate.exchange(any(), any(HttpMethod.class), any(HttpEntity.class),
        ArgumentMatchers.<Class<BasicWalletInfo>>any()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getBasicWalletInfo(), HttpStatus.OK));
    skrillTellerAccountInfoServiceImpl.fetchWalletInfoAsync("1234");
    verify(externalRestTemplate, times(1)).exchange(any(), any(HttpMethod.class), any(HttpEntity.class),
        ArgumentMatchers.<Class<BasicWalletInfo>>any());
  }

  @Test
  public void fetchWalletInfoAsync_withValidData_forApplicationBinance() {
    AuthorizationInfo authorizationInfo = new AuthorizationInfo();
    authorizationInfo.setApplication("BINANCE");
    CommonThreadLocal.setAuthLocal(authorizationInfo);
    when(skrillTellerConfig.getHostUri()).thenReturn("http://api.skrillteller.com");
    when(externalRestTemplate.exchange(any(), any(HttpMethod.class), any(HttpEntity.class),
        ArgumentMatchers.<Class<BasicWalletInfo>>any()))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getBasicWalletInfo(), HttpStatus.OK));
    skrillTellerAccountInfoServiceImpl.fetchWalletInfoAsync("1234");
    verify(externalRestTemplate, times(1)).exchange(any(), any(HttpMethod.class), any(HttpEntity.class),
        ArgumentMatchers.<Class<BasicWalletInfo>>any());
  }

  @Test
  public void fetchWalletInfoAsync_withValidData_shouldFail() {
    when(skrillTellerConfig.getHostUri()).thenReturn("http://api.skrillteller.com");
    when(externalRestTemplate.exchange(any(), any(HttpMethod.class), any(HttpEntity.class),
        ArgumentMatchers.<Class<BasicWalletInfo>>any()))
        .thenThrow(NotFoundException.class);
    skrillTellerAccountInfoServiceImpl.fetchWalletInfoAsync("1234");
    verify(externalRestTemplate, times(1)).exchange(any(), any(HttpMethod.class), any(HttpEntity.class),
        ArgumentMatchers.<Class<BasicWalletInfo>>any());
  }

  @Test
  public void getGroupCustomerIds_withValidData_shouldSucceed() {
    when(skrillTellerConfig.getHostUri()).thenReturn("http://api.skrillteller.com");
    when(externalRestTemplate.exchange(
        any(), any(HttpMethod.class), any(HttpEntity.class), eq(GroupedCustomerIdsResource.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getGroupCustomerIds(), HttpStatus.OK));
    skrillTellerAccountInfoServiceImpl.getMerchantGroupByWalletId("1234");
    verify(externalRestTemplate, times(1)).exchange(any(), any(HttpMethod.class), any(HttpEntity.class),
        ArgumentMatchers.<Class<BasicWalletInfo>>any());
  }

  @Test
  public void getLinkedCustomerIdsResource_withValidData_shouldSucceed() {
    when(skrillTellerConfig.getHostUri()).thenReturn("http://api.skrillteller.com");
    when(externalRestTemplate.exchange(
        any(), any(HttpMethod.class), any(HttpEntity.class), eq(LinkedCustomerIdsResource.class)))
        .thenReturn(new ResponseEntity<>(UserTestUtility.getLinkedCustomerIds(), HttpStatus.OK));
    skrillTellerAccountInfoServiceImpl.getMerchantLinkedToWalletId("1234");
    verify(externalRestTemplate, times(1)).exchange(any(), any(HttpMethod.class), any(HttpEntity.class),
        ArgumentMatchers.<Class<BasicWalletInfo>>any());
  }

  @Test
  public void getSkrillContactEmails_withValidData_shouldSucceed() {
    when(skrillTellerConfig.getHostUri()).thenReturn("http://api.skrillteller.com");
    when(externalRestTemplate.exchange(any(), any(HttpMethod.class), any(HttpEntity.class),
        eq(new ParameterizedTypeReference<List<ContactEmail>>() {
        }))).thenReturn(new ResponseEntity<>(UserTestUtility.getContactEmailList(), HttpStatus.OK));
    skrillTellerAccountInfoServiceImpl.getSkrillContactEmails("1234");
    verify(externalRestTemplate, times(1)).exchange(any(), any(HttpMethod.class), any(HttpEntity.class),
        eq(new ParameterizedTypeReference<List<ContactEmail>>() {
        }));
  }

  @Test
  public void test_FetchBulkWalletInfo_ok() {
    when(skrillTellerConfig.getHostUri()).thenReturn("http://api.skrillteller.com");
    when(externalRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
        eq(BulkWalletDetailResponse.class))).thenReturn(
        new ResponseEntity<BulkWalletDetailResponse>(UserTestUtility.sampleBulkWalletDetail(),
            HttpStatus.OK));

    BulkWalletDetailResponse bulkWalletDetailResponse =
        skrillTellerAccountInfoServiceImpl
            .fetchBulkWalletInfo(new HashSet<>(Arrays.asList("123")), IncludeParam.BUSINESS_PROFILE);

    verify(externalRestTemplate)
        .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(BulkWalletDetailResponse.class));
    assertNotNull(bulkWalletDetailResponse);
    assertThat(bulkWalletDetailResponse.getCustomers().size(), Is.is(1));
  }

  @Test
  public void test_FetchBulkWalletInfoAsync_ok() {
    when(skrillTellerConfig.getHostUri()).thenReturn("http://api.skrillteller.com");
    when(externalRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
        eq(BulkWalletDetailResponse.class))).thenReturn(
        new ResponseEntity<>(UserTestUtility.sampleBulkWalletDetail(),
            HttpStatus.OK));
    skrillTellerAccountInfoServiceImpl
            .fetchBulkWalletInfoAsync(new HashSet<>(Arrays.asList("123")), IncludeParam.BUSINESS_PROFILE);
    verify(externalRestTemplate)
        .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(BulkWalletDetailResponse.class));
  }

  @Test
  public void test_getSchedules_ok() {
    when(skrillTellerConfig.getHostUri()).thenReturn("http://api.skrillteller.com");
    ReportScheduleResponse response = new ReportScheduleResponse();
    response.setContent(new ArrayList<>());
    when(externalRestTemplate.exchange(any(), any(HttpMethod.class), any(HttpEntity.class),
        eq(ReportScheduleResponse.class))).thenReturn(
        new ResponseEntity<>(response,
            HttpStatus.OK));
    skrillTellerAccountInfoServiceImpl.getSchedules("1234", "5678");
    verify(externalRestTemplate)
        .exchange(any(), any(HttpMethod.class), any(HttpEntity.class), eq(ReportScheduleResponse.class));
  }

  @Test
  public void test_deleteSchedule_ok() {
    when(skrillTellerConfig.getHostUri()).thenReturn("http://api.skrillteller.com");
    ReportScheduleResponse response = new ReportScheduleResponse();
    response.setContent(new ArrayList<>());

    when(externalRestTemplate.exchange(any(), any(HttpMethod.class), any(HttpEntity.class), eq(HttpStatus.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    skrillTellerAccountInfoServiceImpl.deleteScheduleReport("1234", "5678");
    verify(externalRestTemplate, times(1)).exchange(any(),
        any(HttpMethod.class), any(), eq(HttpStatus.class));
  }
}
