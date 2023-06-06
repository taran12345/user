// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.interceptor;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.paysafe.upf.user.provisioning.enums.BusinessUnit;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInterceptor;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.util.UserTestUtility;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthorizationInterceptorTest {

  @InjectMocks
  AuthorizationInterceptor interceptor = new AuthorizationInterceptor();

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  /**
   * Load Config.
   */
  @Before
  public void testInit() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testPreHandleAuthPresent() {
    when(request.getHeader("Authorization")).thenReturn(UserTestUtility.AUTH_TOKEN_PORTAL);
    when(request.getHeader("Application")).thenReturn(DataConstants.PORTAL);
    when(request.getHeader("BusinessUnit")).thenReturn(BusinessUnit.EU_ACQUIRING_EEA.name());
    assertTrue(interceptor.preHandle(request, response, new Object()));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testPreHandleWithApplicationHeaderOnly() {
    when(request.getHeader("Application")).thenReturn(DataConstants.PORTAL);
    assertTrue(interceptor.preHandle(request, response, new Object()));
    CommonThreadLocal.unsetAuthLocal();
  }

  @Test
  public void testPreHandleWithBusinessUnitHeaderOnly() {
    when(request.getHeader("BusinessUnit")).thenReturn(BusinessUnit.EU_ACQUIRING_EEA.name());
    assertTrue(interceptor.preHandle(request, response, new Object()));
    CommonThreadLocal.unsetAuthLocal();
  }
}
