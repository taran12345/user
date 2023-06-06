// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import com.paysafe.gbp.commons.bigdata.AuthorizationInfo;
import com.paysafe.gbp.commons.bigdata.CommonThreadLocal;
import com.paysafe.gbp.commons.bigdata.RequestContext;
import com.paysafe.ss.logging.correlation.feign.InternalHeadersContext;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.util.ObjectUtils;

import java.util.Map;

/**
 * This class copies mdc data to new thread which is created by async call.
 * 
 * @author satishmukku
 */

public class AsyncTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    Map<String, String> contextMap = MDC.getCopyOfContextMap();
    Object authLocal = CommonThreadLocal.getAuthLocal();
    Object requestContextLocal = CommonThreadLocal.getRequestContextLocal();
    Map<String, String> internalHeaders = InternalHeadersContext.getInternalHeaders();
    return () -> {
      try {
        if (contextMap != null) {
          MDC.setContextMap(contextMap);
          InternalHeadersContext.initContext();
          InternalHeadersContext.setInternalHeaders(internalHeaders);
          buildMdcContext(authLocal, requestContextLocal);
        }
        runnable.run();
      } finally {
        MDC.clear();
        InternalHeadersContext.clear();
        CommonThreadLocal.unsetRequestContextLocal();
        CommonThreadLocal.unsetAuthLocal();
      }
    };
  }

  private void buildMdcContext(Object authLocal, Object requestContextLocal) {
    if (!ObjectUtils.isEmpty(authLocal)) {
      CommonThreadLocal.setAuthLocal((AuthorizationInfo) authLocal);
    }

    if (!ObjectUtils.isEmpty(requestContextLocal)) {
      CommonThreadLocal.setRequestContextLocal((RequestContext) requestContextLocal);
    }

    Object correlationId =
        InternalHeadersContext.getInternalHeaders().get(InternalHeadersContext.X_INTERNAL_CORRELATION_ID_HEADER);
    if (!ObjectUtils.isEmpty(correlationId)) {
      MDC.put(InternalHeadersContext.X_INTERNAL_CORRELATION_ID_HEADER, correlationId.toString());
    }

    Object requestId =
        InternalHeadersContext.getInternalHeaders().get(InternalHeadersContext.X_INTERNAL_REQUEST_ID_HEADER);
    if (!ObjectUtils.isEmpty(requestId)) {
      MDC.put(InternalHeadersContext.X_INTERNAL_REQUEST_ID_HEADER, requestId.toString());
    }
  }

}
