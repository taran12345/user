// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import com.paysafe.upf.user.provisioning.web.rest.resource.BrandPermissionsMapping;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "skrilltellerconfiguration")
@Data
public class DynamicBrandsConfig {

  private List<BrandPermissionsMapping> brands;

}
