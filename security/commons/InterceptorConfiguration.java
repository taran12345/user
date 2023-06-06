// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.security.commons;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This class stores the Interceptors configurations.
 * 
 *
 */
@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer {

  @Bean
  public AuthorizationInterceptor authorizationInterceptor() {
    return new AuthorizationInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authorizationInterceptor());
  }

}