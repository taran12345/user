// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.user-provisioning")
@Data
@RefreshScope
public class UserProvisioningConfig {

  private static final String UI_HOST_URL_DEFAULT = "http://localhost:8060/portal#";
  private static final String SKRILL_HOST_URL_DEFAULT = "http://localhost:8060/skrill";
  private static final String NETELLER_HOST_URL_DEFAULT = "http://localhost:8060/neteller";
  private static final String PARTNER_PORTAL_HOST_URL_DEFAULT = "http://localhost:8060/partner";
  private static final boolean IS_MAIL_NOTIFICATIONS_ENABLED = true;

  private static final UserConfig USER_CONFIG_DEFAULT = new UserConfig();

  protected String uiHostUrl = UI_HOST_URL_DEFAULT;

  protected String skrillHostUrl = SKRILL_HOST_URL_DEFAULT;

  protected String netellerHostUrl = NETELLER_HOST_URL_DEFAULT;

  protected String partnerPortalHostUrl = PARTNER_PORTAL_HOST_URL_DEFAULT;

  @NestedConfigurationProperty
  protected UserConfig user = USER_CONFIG_DEFAULT;

  protected Map<String, String> roleFeatureMapping;

  protected List<String> singleApiModules;

  protected boolean isMailNotificationsEnabled = IS_MAIL_NOTIFICATIONS_ENABLED;

  protected String uaaClientId;
  protected String uaaClientSecret;
}
