// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.interceptor;

import static com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants.NETELLER;
import static com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants.SKRILL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.exceptions.UserProvisioningForbiddenException;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.security.commons.SkrillTelllerRestTemplateInterceptor;
import com.paysafe.upf.user.provisioning.service.impl.UaaClient;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class SkrillTellerRestTemplateInterceptorTest {

  @InjectMocks
  SkrillTelllerRestTemplateInterceptor skrillTelllerRestTemplateInterceptor;

  @Mock
  HttpRequest request;

  @Mock
  ClientHttpRequestExecution clientHttpRequestExecution;

  @Mock
  private SkrillTellerConfig skrillTellerConfig;

  @Mock
  private UaaClient uaaClient;

  private static final Logger logger = LoggerFactory.getLogger(
      SkrillTellerRestTemplateInterceptorTest.class);

  public static final String AUTHORIZATION_HEADER = "Authorization";

  // bearer token for username 'testuser'
  public static String auth = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJ0ZXN0dXNlciIsInNjb3BlIjpb"
      + "InNjb3BlMSIsInNjb3BlMiJdLCJpc3MiOiJQYXlzYWZlIFBMQyIsImV4cCI6MTQ4NTk1NzMyNCwia"
      + "WF0IjoxNDg1OTU0NjI0LCJqdGkiOiJmOWFlNmE2Yy0zZTMzLTQzMWMtODg4Yi1iY2E2NTY5MTQ1MDQiLCJ"
      + "jbGllbnRfaWQiOiI0YmMyZjRhOGZhNzIwMGM0NjFmZGMwMDkxZWFlZDQxMmViYzBiN2Q1In0.fgQtjZUgwzlA"
      + "3Vn83Wfn6sLjStRTXeL0dkuDnRPcQSg";

  private final ClientHttpResponse clientHttpResponse = mock(ClientHttpResponse.class);

  /**
   * Load Config.
   */
  @Before
  public void testInit() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = UserProvisioningForbiddenException.class)
  public void testIntercept() throws IOException {
    AuthorizationInfo authorizationInfo = new AuthorizationInfo();
    authorizationInfo.setApplication("");
    CommonThreadLocal.setAuthLocal(authorizationInfo);
    String body = "";
    skrillTelllerRestTemplateInterceptor.intercept(request, body.getBytes(), clientHttpRequestExecution);
  }

  @Test(expected = UserProvisioningForbiddenException.class)
  public void testIntercept_RandomBrand() throws IOException {
    AuthorizationInfo authorizationInfo = new AuthorizationInfo();
    authorizationInfo.setApplication("randomBrand");
    CommonThreadLocal.setAuthLocal(authorizationInfo);
    HttpHeaders mockedHeaders = Mockito.mock(HttpHeaders.class);
    when(request.getHeaders()).thenReturn(mockedHeaders);
    when(uaaClient.getToken(Mockito.anyString()))
        .thenThrow(new UserProvisioningForbiddenException("Invalid brand , acess denied."));
    String body = "";
    skrillTelllerRestTemplateInterceptor.intercept(request, body.getBytes(), clientHttpRequestExecution);
  }

  @Test
  public void testIntercept_ApplicationNeteller() throws IOException {
    AuthorizationInfo authorizationInfo = new AuthorizationInfo();
    authorizationInfo.setApplication(NETELLER);
    CommonThreadLocal.setAuthLocal(authorizationInfo);
    when(request.getHeaders()).thenReturn(new HttpHeaders());
    when(clientHttpRequestExecution.execute(any(), any()))
        .thenReturn(clientHttpResponse);
    skrillTelllerRestTemplateInterceptor.intercept(request, new byte[0], clientHttpRequestExecution);
    verify(clientHttpRequestExecution, times(1)).execute(any(), any());
  }

  @Test
  public void testIntercept_ApplicationSkrill() throws IOException {
    AuthorizationInfo authorizationInfo = new AuthorizationInfo();
    authorizationInfo.setApplication(SKRILL);
    CommonThreadLocal.setAuthLocal(authorizationInfo);
    when(request.getHeaders()).thenReturn(new HttpHeaders());
    when(clientHttpRequestExecution.execute(any(), any()))
        .thenReturn(clientHttpResponse);
    skrillTelllerRestTemplateInterceptor.intercept(request, new byte[0], clientHttpRequestExecution);
    verify(clientHttpRequestExecution, times(1)).execute(any(), any());
  }

  @Test
  public void testIntercept_RefreshToken() throws IOException {
    AuthorizationInfo authorizationInfo = new AuthorizationInfo();
    authorizationInfo.setApplication(SKRILL);
    CommonThreadLocal.setAuthLocal(authorizationInfo);
    when(request.getHeaders()).thenReturn(new HttpHeaders());
    when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);
    when(clientHttpRequestExecution.execute(any(), any()))
        .thenReturn(clientHttpResponse);
    when(uaaClient.generateToken(SKRILL)).thenReturn("access_token");
    skrillTelllerRestTemplateInterceptor.intercept(request, new byte[0], clientHttpRequestExecution);
    verify(uaaClient, times(1)).generateToken(SKRILL.toLowerCase());
  }

}
