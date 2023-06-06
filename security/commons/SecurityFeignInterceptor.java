// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.security.commons;

import com.paysafe.ss.logging.correlation.feign.InternalHeadersContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.encoding.BaseRequestInterceptor;
import org.springframework.cloud.openfeign.encoding.FeignClientEncodingProperties;

import java.util.Optional;

/**
 * This is the request interceptor for Feign. add only common fields here, any specific fields can be added by extending
 * this class. Current implementation supports sending 'jwt' token as "Authorization" header and 'correlationid' as
 * "correlationId" header
 *
 * @author ravipadala
 *
 */
public class SecurityFeignInterceptor extends BaseRequestInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFeignInterceptor.class);
  private static final String SOURCE = "PAYSAFE_SS_ACCESS_CONTROL";
  private static final String X_FORWARDED_HEADER_NAME = "X-Forwarded-For";
  private static final String USER_AGENT_HEADER_NAME = "User-Agent";

  public SecurityFeignInterceptor(FeignClientEncodingProperties properties) {
    super(properties);
  }

  @Override
  public void apply(feign.RequestTemplate template) {
    Optional<AuthorizationInfo> authorizationInfoOptional = Optional.ofNullable(CommonThreadLocal.getAuthLocal());

    if (InternalHeadersContext.getInternalHeaders()
        .get(InternalHeadersContext.X_INTERNAL_CORRELATION_ID_HEADER) != null) {
      addHeader(template, InternalHeadersContext.X_INTERNAL_CORRELATION_ID_HEADER,
          InternalHeadersContext.getInternalHeaders().get(InternalHeadersContext.X_INTERNAL_CORRELATION_ID_HEADER));
      LOGGER.debug("Added correlation id in the header");
    }

    authorizationInfoOptional
        .ifPresent(authorizationInfo -> {
          if (authorizationInfo.getAuthHeader() != null) {
            addHeader(template, AuthorizationInterceptor.AUTHORIZATION_HEADER, authorizationInfo.getAuthHeader());
            LOGGER.debug("Added authorization token in the header");
          }
          if (authorizationInfo.getApplication() != null) {
            addHeader(template, AuthorizationInterceptor.APPLICATION, authorizationInfo.getApplication());
            LOGGER.debug("Added Application in the header");
          }
        });

    addHeader(template, "source", SOURCE);
    LOGGER.debug("Added source in the header");

    Optional<RequestContext> requestContextOptional = Optional.ofNullable(CommonThreadLocal.getRequestContextLocal());

    requestContextOptional
        .ifPresent(requestContext -> {
          if (requestContext.getIpAddress() != null) {
            addHeader(template, X_FORWARDED_HEADER_NAME, requestContext.getIpAddress());
          }
          if (requestContext.getUserAgent() != null) {
            addHeader(template, USER_AGENT_HEADER_NAME, requestContext.getUserAgent());
          }
        });
  }

}
