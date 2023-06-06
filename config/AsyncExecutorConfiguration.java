// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import com.paysafe.upf.user.provisioning.utils.AsyncTaskDecorator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@ConfigurationProperties(
    prefix = "upfasync.executor")
@ConditionalOnProperty(
    matchIfMissing = true, value = "upfasync.executor.use.default", havingValue = "true")
@RefreshScope
public class AsyncExecutorConfiguration {

  private String threadPrefix = "upfasync-";

  private int corePoolSize = 6;

  private int maxPoolSize = 30;

  private int queueSize = 100;

  private boolean logUncaughtExceptions = false;

  /**
   * ThreadPoolTaskExecutor used by @Asynch annotation.
   */
  @Bean(
      destroyMethod = "shutdown", name = "upfAsyncExecutor")
  @Primary
  public Executor getGbpAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setTaskDecorator(new AsyncTaskDecorator());
    executor.setThreadNamePrefix(threadPrefix);
    executor.setCorePoolSize(getCorePoolSize());
    executor.setMaxPoolSize(getMaxPoolSize());
    executor.setQueueCapacity(getQueueSize());
    executor.setDaemon(true);
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }

  public int getCorePoolSize() {
    return corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }

  public int getQueueSize() {
    return queueSize;
  }

  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

  public String getThreadPrefix() {
    return threadPrefix;
  }

  public void setThreadPrefix(String threadPrefix) {
    this.threadPrefix = threadPrefix;
  }

  public boolean isLogUncaughtExceptions() {
    return logUncaughtExceptions;
  }

  public void setLogUncaughtExceptions(boolean logUncaughtExceptions) {
    this.logUncaughtExceptions = logUncaughtExceptions;
  }
}
