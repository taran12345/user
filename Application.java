// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning;

import com.paysafe.op.commons.framework.repository.EnableOneplatformDatabase;
import com.paysafe.op.commons.framework.springboot.StartupHelper;
import com.paysafe.op.errorhandling.EnableOneplatformErrorHandling;
import com.paysafe.ss.logging.EnableOneplatformTracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.UnknownHostException;

@SpringBootApplication
@EnableCircuitBreaker
@EnableDiscoveryClient
@ComponentScan({"com.paysafe.upf.user.provisioning"})
@EnableOneplatformTracing
@EnableOneplatformDatabase
@EnableOneplatformErrorHandling
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableConfigurationProperties
@EnableFeignClients
@EnableAsync
public class Application {

  public static final Logger logger = LoggerFactory.getLogger(Application.class);

  /**
   * Initialize spring boot application.
   *
   * @param args main parameters
   */
  public static void main(String... args) throws UnknownHostException {
    StartupHelper.launch(Application.class, args,
        "Responsible " + "for provisioning user's access groups, permissions and roles");
  }
}
