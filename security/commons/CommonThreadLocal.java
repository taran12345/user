// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.security.commons;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;

public final class CommonThreadLocal {
  private static final InheritableThreadLocal<AuthorizationInfo> AUTH_LOCAL = new InheritableThreadLocal<>();

  private static final InheritableThreadLocal<RequestContext> REQUEST_CONTEXT_LOCAL = new InheritableThreadLocal<>();

  private CommonThreadLocal() {
  }

  /**
   * get value of thread local form the current thread.
   * 
   * @return value of thread local
   */
  public static AuthorizationInfo getAuthLocal() {
    return AUTH_LOCAL.get();
  }

  /**
   * Sets the authorization info object in Thread Local.
   * 
   * @param authorizationInfo authorization info object.
   * @throws BadRequestException when Authorization does not have proper format.
   */
  public static void setAuthLocal(AuthorizationInfo authorizationInfo) {
    AUTH_LOCAL.set(authorizationInfo);
  }

  /**
   * clean up the authlocal thread local.
   */
  public static void unsetAuthLocal() {
    AUTH_LOCAL.remove();
  }

  /**
   * Get value of thread local from the current thread.
   * 
   * @return value of thread local
   */
  public static RequestContext getRequestContextLocal() {
    return REQUEST_CONTEXT_LOCAL.get();
  }

  /**
   * Sets the requestContext object in Thread Local.
   * 
   * @param requestContext request context.
   */
  public static void setRequestContextLocal(RequestContext requestContext) {
    REQUEST_CONTEXT_LOCAL.set(requestContext);
  }

  /**
   * clean up the thread local.
   */
  public static void unsetRequestContextLocal() {
    REQUEST_CONTEXT_LOCAL.remove();
  }
}
