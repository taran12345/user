// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("httpclient")
public class HttpClientConfigProperties {

  private boolean proxyEnabled;

  private String proxyUrl;

  private String proxyPort;

  private Integer connectionsPerRoute;

  private Integer totalConnections;

  private Integer connectionTimeout;

  private Integer socketTimeout;

  private Integer connectionRequestTimeout;
}
