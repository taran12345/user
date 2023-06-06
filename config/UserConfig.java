// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import com.paysafe.upf.user.provisioning.web.rest.dto.AppUserConfigDto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.user-provisioning.user")
@Data
@RefreshScope
public class UserConfig {
  private static final Long REGISTRATION_TOKEN_EXPIRY_TIME_SEC_DEFAULT = 60 * 60 * 24 * 7L;
  private static final Long PERMISSIONS_UPDATED_TOKEN_EXPIRY_TIME_SEC_DEFAULT = 60 * 60 * 24 * 7L;
  private static final Long PASSWORD_TOKEN_EXPIRY_TIME_SEC_DEFAULT = 60 * 60 * 24L;
  private static final Long LINK_MAIL_TOKEN_EXPIRY_TIME_SEC_DEFAULT = 60 * 60 * 24 * 7L;
  private static final String ADMIN_ROLE_DEFAULT = "BP_ADMIN";
  private static final boolean LINK_EMAIL_BLOCKER_DEFAULT = true;
  private static final UserGcsEventsConfig USER_GCS_EVENT_CONFIG_DEFAULT = new UserGcsEventsConfig();
  private static final Long MASTER_MERCHANT_SEARCH_AFTER_FETCH_LIMIT = 200L;

  protected String adminRole = ADMIN_ROLE_DEFAULT;
  protected Long registationTokenTimeToLiveSeconds = REGISTRATION_TOKEN_EXPIRY_TIME_SEC_DEFAULT;
  protected Long permissionsUpdatedTokenTimeToLiveSeconds = PERMISSIONS_UPDATED_TOKEN_EXPIRY_TIME_SEC_DEFAULT;
  protected Long passwordTokenTimeToLiveSeconds = PASSWORD_TOKEN_EXPIRY_TIME_SEC_DEFAULT;
  protected Long linkMailTokenTimeToLiveSeconds = LINK_MAIL_TOKEN_EXPIRY_TIME_SEC_DEFAULT;
  protected boolean linkEmailBlocker = LINK_EMAIL_BLOCKER_DEFAULT;

  @NestedConfigurationProperty
  protected UserGcsEventsConfig gcsEvents = USER_GCS_EVENT_CONFIG_DEFAULT;

  Map<String, AppUserConfigDto> appUserLimit;

  protected Long masterMerchantSearchAfterFetchLimit = MASTER_MERCHANT_SEARCH_AFTER_FETCH_LIMIT;
}
