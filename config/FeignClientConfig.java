// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import com.paysafe.upf.user.provisioning.errors.FeignErrorDecoderV2;
import com.paysafe.upf.user.provisioning.security.commons.SecurityFeignInterceptor;

import feign.codec.ErrorDecoder;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.encoding.FeignClientEncodingProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(FeignClientEncodingProperties.class)
public class FeignClientConfig {
  @Bean
  public ErrorDecoder errorDecoder() {
    return new FeignErrorDecoderV2();
  }

  @Bean
  public SecurityFeignInterceptor feignAcceptJwtInterceptor(FeignClientEncodingProperties properties) {
    return new SecurityFeignInterceptor(properties);
  }
}
