// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.security.commons;

import com.paysafe.upf.user.provisioning.utils.IpUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthorizationInterceptor extends HandlerInterceptorAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationInterceptor.class);
  public static final String AUTHORIZATION_HEADER = "Authorization";
  static final String APPLICATION = "Application";
  public static final String X_FORWARDED_HEADER_NAME = "X-Forwarded-For";
  public static final String USER_AGENT_HEADER_NAME = "User-Agent";
  public static final String BUSINESS_UNIT = "BusinessUnit";

  /**
   * Captures a JWT if present and stores in thread local.
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

    String auth = request.getHeader(AUTHORIZATION_HEADER);
    AuthorizationInfo authorizationInfo = new AuthorizationInfo();
    if (auth != null && !auth.toLowerCase(Locale.getDefault()).trim().startsWith("basic")) {
      LOGGER.debug("Authorization header present:{}", auth);
      authorizationInfo = JwtPayloadUtil.retrievePayload(auth);
      authorizationInfo.setAuthHeader(auth);
      LOGGER.debug("Authorization decoded as:{}", authorizationInfo);
    }

    String application = request.getHeader(APPLICATION);
    if (StringUtils.isNotEmpty(application)) {
      LOGGER.debug("Application present in request header:{}", application);
      authorizationInfo.setApplication(application);
    }

    String businessUnit = request.getHeader(BUSINESS_UNIT);
    if (StringUtils.isNotEmpty(businessUnit)) {
      LOGGER.debug("BusinessUnit present in request header:{}", businessUnit);
      authorizationInfo.setBusinessUnit(businessUnit);
    }

    CommonThreadLocal.setAuthLocal(authorizationInfo);
    LOGGER.debug("Authorization info set to thread local");

    RequestContext requestContext = CommonThreadLocal.getRequestContextLocal();
    if (requestContext == null) {
      requestContext = new RequestContext();
    }

    requestContext.setIpAddress(IpUtils.getRemoteAddress(request));
    requestContext.setUserAgent(request.getHeader(USER_AGENT_HEADER_NAME));
    CommonThreadLocal.setRequestContextLocal(requestContext);

    return true;
  }

  /**
   * Called after the complete request has finished.
   */
  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    CommonThreadLocal.unsetAuthLocal();
    CommonThreadLocal.unsetRequestContextLocal();
    LOGGER.debug("Authorization local has been unset.");
  }
}
