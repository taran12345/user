// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import com.google.common.collect.ImmutableMap;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.user-provisioning.user.gcs-events")
@Data
@RefreshScope
public class UserGcsEventsConfig {

  public static final String VERIFY_EMAIL_EVENTID = "verifyEmailEventId";
  public static final String LINK_EMAIL_EVENTID = "linkMailEventId";
  public static final String RESETPASS_EMAIL_EVENTID = "resetPasswordEmailEventId";
  public static final String ACTIVATION_EMAIL_EVENTID = "userActivationEmailEventId";
  public static final String PERMISSIONS_UPDATED_EMAIL_EVENTID = "permissionsUpdatedEmailEventId";
  public static final String PROFILE_SETTINGS_MFA_ENABLED_EVENTID = "profileSettingsMfaEnabledEventId";
  public static final String PROFILE_SETTINGS_MFA_DISABLED_EVENTID = "profileSettingsMfaDisabledEventId";
  public static final String ADMIN_MFA_ENABLED_INAPP_EVENTID = "adminMfaEnabledInAppEventId";
  public static final String SUBUSER_MFA_ENABLED_EMAIL_EVENTID = "subUserMfaEnabledEmailEventId";
  public static final String ADMIN_MFA_DISABLED_INAPP_EVENTID = "adminMfaDisabledInAppEventId";
  public static final String SUBUSER_MFA_DISABLED_EMAIL_EVENTID = "subUserMfaDisabledEmailEventId";
  public static final String RESET_MFA_EMAIL_EVENTID = "resetMfaEmailEventId";
  public static final String ADMIN_RESET_MFA_INAPP_EVENTID = "adminResetMfaInAppEventId";
  public static final String ADMIN_BULK_UPDATE_SUCCESS_MFA_INAPP_EVENTID = "adminBulkUpdateSuccessMfaInAppEventId";
  public static final String ADMIN_BULK_FAILURE_SUCCESS_MFA_INAPP_EVENTID = "adminBulkFailureSuccessMfaInAppEventId";

  public static final Map<String, String> SKRILL_DEFAULT =
      ImmutableMap.of(RESETPASS_EMAIL_EVENTID, "5190f72b-888e-4bd7-8c6a-c9ea1a8f9615");

  public static final Map<String, String> NETELLER_DEFAULT =
      ImmutableMap.of(RESETPASS_EMAIL_EVENTID, "27aed0f3-642d-4fad-a68e-340e9758a251");

  protected String verifyEmailEventId;

  protected String resetPasswordEmailEventId;

  protected String userActivationEmailEventId;

  protected String userUpdateEventId;

  protected String caseUpdatedInAppEventId;

  protected String sendTransactionReceiptEventId;

  protected String linkMailEventId;

  protected Map<String, String> skrill;

  protected Map<String, String> neteller;

  protected Map<String, String> businessPortal;

  protected Map<String, String> partnerPortal;
}
