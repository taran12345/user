// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.security.commons;

import static com.paysafe.upf.user.provisioning.security.commons.AuthorizationInterceptor.X_FORWARDED_HEADER_NAME;

import com.paysafe.ss.logging.correlation.feign.InternalHeadersContext;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.exceptions.UserProvisioningForbiddenException;
import com.paysafe.upf.user.provisioning.service.impl.UaaClient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
public class SkrillTelllerRestTemplateInterceptor implements ClientHttpRequestInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(SkrillTelllerRestTemplateInterceptor.class);

  private static final String BRAND_HEADER_NAME = "X-INTERNAL-BRAND";

  private static final String X_FONG_ID_HEADER_NAME = "X-Fong-Id";

  private static final String SKRILL_CLIENT_HEADER_NAME = "Skrill-Client-Id";

  @Autowired
  private SkrillTellerConfig skrillTellerConfig;

  @Autowired
  private UaaClient uaaClient;

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {

    LOGGER.debug("Skrill url : {}", request.getURI());
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    if (StringUtils.isEmpty(application)) {
      LOGGER.error("throwing GbpForbiddenException:Invalid application, access denied.");
      throw new UserProvisioningForbiddenException("Invalid application, access denied.");
    }

    String currentBrand = CommonThreadLocal.getAuthLocal().getBusinessUnit() == null
        ? CommonThreadLocal.getAuthLocal().getApplication() : CommonThreadLocal.getAuthLocal().getBusinessUnit();

    if (StringUtils.isEmpty(currentBrand)) {
      LOGGER.error("throwing GbpForbiddenException:Invalid brand, access denied.");
      throw new UserProvisioningForbiddenException("Invalid brand, access denied.");
    }
    currentBrand = currentBrand.toLowerCase();

    request.getHeaders().add(BRAND_HEADER_NAME, currentBrand);
    request.getHeaders().setBearerAuth(uaaClient.getToken(currentBrand));
    request.getHeaders().add(SKRILL_CLIENT_HEADER_NAME, skrillTellerConfig.getClientId());

    String correlationId =
        InternalHeadersContext.getInternalHeaders().get(InternalHeadersContext.X_INTERNAL_CORRELATION_ID_HEADER);
    request.getHeaders().add(X_FONG_ID_HEADER_NAME, correlationId);

    RequestContext requestContext = CommonThreadLocal.getRequestContextLocal();
    if (requestContext != null) {
      request.getHeaders().add(X_FORWARDED_HEADER_NAME, requestContext.getIpAddress());
    }

    logRequest(request, body);
    ClientHttpResponse response = execution.execute(request, body);
    logResponse(response);

    if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
      LOGGER.info("Existing token expired. Creating new access token for Skrill APIs");
      String accessToken = uaaClient.generateToken(currentBrand);
      if (!StringUtils.isEmpty(accessToken)) {
        request.getHeaders().setBearerAuth(accessToken);
        response = execution.execute(request, body);
        logResponse(response);
      } else {
        LOGGER.error("Error while generating new access token for Skrill APIs");
      }
    }

    return response;
  }

  private void logRequest(HttpRequest request, byte[] body) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "===========================Skrill teller request begin================================================");
      LOGGER.debug("URI         : {}", request.getURI());
      LOGGER.debug("Method      : {}", request.getMethod());
      LOGGER.debug("Headers     : {}", request.getHeaders());
      LOGGER.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
      LOGGER.debug("==========================request end================================================");
    }
  }

  private void logResponse(ClientHttpResponse response) throws IOException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER
          .debug("============================Skrill teller response begin==========================================");
      LOGGER.debug("Status code  : {}", response.getStatusCode());
      LOGGER.debug("Status text  : {}", response.getStatusText());
      LOGGER.debug("Headers      : {}", response.getHeaders());
      LOGGER.debug("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
      LOGGER.debug("=======================response end=================================================");
    }
  }

}