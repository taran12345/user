// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@RefreshScope
@Data
@Component
@ConfigurationProperties(prefix = "gbp.skrillteller")
public class SkrillTellerVaultConfig {
  Map<String, BusinessUnitVaultConfig> uaa = new HashMap<>();
}

