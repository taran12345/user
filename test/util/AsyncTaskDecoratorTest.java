// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import static com.paysafe.ss.logging.correlation.feign.InternalHeadersContext.X_INTERNAL_CORRELATION_ID_HEADER;

import com.paysafe.gbp.commons.bigdata.AuthorizationInfo;
import com.paysafe.gbp.commons.bigdata.CommonThreadLocal;
import com.paysafe.gbp.commons.bigdata.RequestContext;
import com.paysafe.ss.logging.correlation.feign.InternalHeadersContext;
import com.paysafe.upf.user.provisioning.utils.AsyncTaskDecorator;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class AsyncTaskDecoratorTest {

  public static final Logger logger = LoggerFactory.getLogger(AsyncTaskDecoratorTest.class);
  private static final String CORRELATION_ID = "1111aaaa-11aa-11aa-11aa-111111aaaaaa";

  Map<String, String> contextMap;
  Object authLocal;
  Object requestContextLocal;

  /**
   * Initial setup.
   */
  @Before
  public void init() {
    logger.info("Thread pool name on setup:\t" + Thread.currentThread().getName());
    contextMap = MDC.getCopyOfContextMap();
    authLocal = CommonThreadLocal.getAuthLocal();
    requestContextLocal = CommonThreadLocal.getRequestContextLocal();
    HystrixRequestContext.initializeContext();
    Map<String, String> internalHeaders = new HashMap<>();
    internalHeaders.put(X_INTERNAL_CORRELATION_ID_HEADER, CORRELATION_ID);
    InternalHeadersContext.setInternalHeaders(internalHeaders);
    CommonThreadLocal.setAuthLocal(new AuthorizationInfo());
    CommonThreadLocal.setRequestContextLocal(new RequestContext());
  }

  /**
   * Execute below test in different thread pool.
   * 
   */
  @Test
  public void testThreadIsolationStrategy() {
    logger.info("Thread pool name on setup:\t" + Thread.currentThread().getName());
    ThreadPoolTaskExecutor executorPool = (ThreadPoolTaskExecutor) getTaskExecutor();
    // submit the thread pool
    for (int i = 0; i < 2; i++) {
      executorPool.execute(new Thread("Thread-" + i));
    }
    Assert.assertEquals(10, executorPool.getCorePoolSize());
    Assert.assertEquals(10, executorPool.getMaxPoolSize());
    // shut down the pool
    executorPool.shutdown();
  }

  @Test
  public void testThreadWithContext() {
    logger.info("Thread pool name on setup:\t" + Thread.currentThread().getName());
    MDC.setContextMap(new HashMap<>());
    ThreadPoolTaskExecutor executorPool = (ThreadPoolTaskExecutor) getTaskExecutor();
    // submit the thread pool
    for (int i = 0; i < 2; i++) {
      executorPool.execute(new Thread("Thread-" + i));
    }
    Assert.assertEquals(10, executorPool.getCorePoolSize());
    Assert.assertEquals(10, executorPool.getMaxPoolSize());
    // shut down the pool
    executorPool.shutdown();
  }

  /**
   * Thread executor for task executor.
   */

  @Bean(name = "taskExecutor")
  public Executor getTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(20);
    executor.setThreadNamePrefix("PSThread-");
    executor.setTaskDecorator(new AsyncTaskDecorator());
    executor.initialize();
    return executor;
  }
}