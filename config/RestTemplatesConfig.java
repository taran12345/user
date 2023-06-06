// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import com.paysafe.upf.user.provisioning.errors.SkrillTellerRestTemplateErrorHandler;
import com.paysafe.upf.user.provisioning.security.commons.SkrillTelllerRestTemplateInterceptor;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplatesConfig {

  @Autowired
  SkrillTelllerRestTemplateInterceptor skrillTelllerRestTemplateInterceptor;

  @Bean
  @Primary
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /**
   * Rest Template for SkrillTeller api calls.
   *
   * @param restTemplateBuilder builder.
   * @param httpClientConfig client config.
   * @return RestTemplate configured resttemplate.
   */
  @Bean(name = "externalRestTemplate")
  public RestTemplate externalRestTemplate(RestTemplateBuilder restTemplateBuilder,
      HttpClientConfigProperties httpClientConfig) {

    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setConnectTimeout(httpClientConfig.getConnectionTimeout());
    requestFactory.setReadTimeout(httpClientConfig.getSocketTimeout());

    RestTemplate restTemplate = new RestTemplate();

    restTemplate.setRequestFactory(requestFactory);
    restTemplate.setErrorHandler(new SkrillTellerRestTemplateErrorHandler());

    List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
    if (CollectionUtils.isEmpty(interceptors)) {
      interceptors = new ArrayList<>();
    }
    interceptors.add(skrillTelllerRestTemplateInterceptor);
    restTemplate.setInterceptors(interceptors);
    return restTemplate;
  }
}
