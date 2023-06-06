// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import com.paysafe.upf.user.provisioning.enums.UserStatus;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "migration.mapping")
@Data
@RefreshScope
public class UserMigrationConfig {
  private Map<String, UserStatus> statusMap;

  private Map<String, List<String>> roleMap;

  private Map<String, String> usIgamingRoleMap;

  private Set<String> netbanxUnMappedPermissions;
}
