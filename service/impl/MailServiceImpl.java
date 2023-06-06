// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static com.paysafe.upf.user.provisioning.enums.EmailType.AUTH_CONTACT_EMAIL;
import static com.paysafe.upf.user.provisioning.enums.EmailType.SELF_EMAIL;

import com.paysafe.gbp.commons.notifications.resources.EmailRecipient;
import com.paysafe.gbp.commons.notifications.resources.InAppRecipient;
import com.paysafe.gbp.commons.notifications.resources.NotificationRequest;
import com.paysafe.upf.user.provisioning.config.UserProvisioningConfig;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.enums.EmailType;
import com.paysafe.upf.user.provisioning.enums.TokenType;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.GcsService;
import com.paysafe.upf.user.provisioning.service.MailService;
import com.paysafe.upf.user.provisioning.service.SkrillTellerAccountInfoService;
import com.paysafe.upf.user.provisioning.service.TokenService;
import com.paysafe.upf.user.provisioning.utils.MailUtil;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.ContactEmail;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.SkrillContactEmailsResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class MailServiceImpl implements MailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceImpl.class);

  public static final String PASS_RECCOVERY_TOKEN_TYPE = "PASSWORD_RECOVERY";
  public static final String RESETPASS_EMAIL_EVENTID = "resetPasswordEmailEventId";
  public static final String ACTIVATION_EMAIL_EVENTID = "userActivationEmailEventId";
  public static final String PERMISSIONS_UPDATED_EMAIL_EVENTID = "permissionsUpdatedEmailEventId";
  private static final String RESET_PASS_URI = "/resetPassword";

  private static final String CUSTOMER_NAME = "custName";
  private static final String LOGIN_NAME = "loginName";
  private static final String EXPIRE_DAYS = "expireDays";
  private static final String EMAIL = "EMAIL";
  private static final String SPRING_APP_NAME = "spring.application.name";

  @Autowired
  private UserProvisioningConfig userProvisioningConfig;

  @Autowired
  private TokenService tokenService;

  @Autowired
  private Environment env;

  @Autowired
  private GcsService gcsService;

  @Autowired
  private MailUtil mailUtil;

  @Autowired
  private SkrillTellerAccountInfoService skrillTellerAccountInfoService;

  @Value("${adminshell.resetPasswordHostUiUrl}")
  private String resetPasswordHostUiUrl;

  @Value("${adminshell.skrillResetPasswordHostUiUrl}")
  private String skrillResetPasswordHostUiUrl;

  @Value("${adminshell.netellerResetPasswordHostUiUrl}")
  private String netellerResetPasswordHostUiUrl;

  @Value("${adminshell.selfEmailResetPasswordEventId}")
  private String selfEmailResetPasswordEventId;

  @Value("${adminshell.authContactEmailResetPasswordEventId}")
  private String authContactEmailResetPasswordEventId;

  @Value("${adminshell.selfEmailResetPasswordSkrillEventId}")
  private String selfEmailResetPasswordSkrillEventId;

  @Value("${adminshell.selfEmailResetPasswordNetellerEventId}")
  private String selfEmailResetPasswordNetellerEventId;

  @Override
  public void sendRegistrationConfirmationEmail(UserProvisioningUserResource userResponse) {
    if (UserStatus.PENDING_USER_ACTION.equals(userResponse.getStatus())
        || UserStatus.PROVISIONED.equals(userResponse.getStatus())) {
      TokenRequestResource tokenRequestResource = new TokenRequestResource(
          userProvisioningConfig.getUser().getRegistationTokenTimeToLiveSeconds(), TokenType.PASSWORD_RECOVERY);
      TokenResponseResource tokenResponseResource =
          tokenService.createToken(tokenRequestResource, userResponse.getUserName(), null);
      sendRegistartionMail(userResponse, tokenResponseResource);
    }
  }

  private void sendRegistartionMail(UserProvisioningUserResource userResponse,
      TokenResponseResource tokenResponseResource) {
    EmailRecipient emailRecipient = new EmailRecipient();
    emailRecipient.setTo(userResponse.getEmail().trim());

    Map<String, Object> templateVariables = new HashMap<>();
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    String uriParameters = "?uuid=" + userResponse.getExternalId() + "&token=" + tokenResponseResource.getId()
        + "&application=" + application + "&from=OKTA";

    String link = mailUtil.getLink(application, DataConstants.REGISTRATION_MAIL);
    link = link + uriParameters;
    templateVariables.put("link", link);
    mailUtil.setRegistrationTemplateVariables(application, userResponse, templateVariables);
    emailRecipient.setVariables(templateVariables);
    String eventId = mailUtil.getEventId(userResponse, application, DataConstants.REGISTRATION_MAIL);
    NotificationRequest notificationRequest = NotificationRequest.builder().multicast(Arrays.asList(EMAIL))
        .eventId(eventId).referenceId(env.getProperty(SPRING_APP_NAME))
        .emailRecipients(Arrays.asList(emailRecipient)).build();
    LOGGER.info("gcs send email request {}", notificationRequest);
    List<InAppRecipient> inAppRecipients = getInAppRecipients(userResponse, application);
    gcsService.sendNotification(eventId, Arrays.asList(emailRecipient), inAppRecipients);
    if (StringUtils.equals(application, DataConstants.SKRILL)) {
      sendMailToBusinessContactEmails(userResponse);
    }
  }

  private List<InAppRecipient> getInAppRecipients(UserProvisioningUserResource userResponse, String application) {
    if (StringUtils.equals(application, DataConstants.PORTAL)
        || StringUtils.equals(application, DataConstants.PARTNER_PORTAL)) {
      Map<String, Object> inAppTemplateVariables = new HashMap<>();
      inAppTemplateVariables.put(CUSTOMER_NAME, userResponse.getFirstName());
      inAppTemplateVariables.put("brandWhiteLabelling", DataConstants.PAYSAFE_BRAND);
      InAppRecipient inAppRecipient = new InAppRecipient();
      inAppRecipient.setClassifier(userResponse.getId());
      inAppRecipient.setVariables(inAppTemplateVariables);
      return Collections.singletonList(inAppRecipient);
    } else {
      return Collections.emptyList();
    }
  }

  private List<InAppRecipient> getInAppRecipients(User user, String adminUserId, Boolean hasVariableForInApp) {
    Map<String, Object> inAppTemplateVariables = new HashMap<>();
    InAppRecipient inAppRecipient = new InAppRecipient();
    String userName = user.getLoginName();
    if (hasVariableForInApp) {
      inAppTemplateVariables.put(LOGIN_NAME, userName);
      inAppRecipient.setVariables(inAppTemplateVariables);
    }
    if (StringUtils.isNotEmpty(adminUserId)) {
      inAppRecipient.setClassifier(adminUserId);
    } else {
      inAppRecipient.setClassifier(user.getUserId());
    }
    return Collections.singletonList(inAppRecipient);
  }

  @Override
  public void sendPermissionsUpdatedConfirmationEmail(UserProvisioningUserResource userResponse) {
    sendPermissionUpdatedMail(userResponse);
  }

  private void sendPermissionUpdatedMail(UserProvisioningUserResource userResponse) {
    EmailRecipient emailRecipient = new EmailRecipient();
    emailRecipient.setTo(userResponse.getEmail().trim());

    Map<String, Object> templateVariables = new HashMap<>();
    String application = CommonThreadLocal.getAuthLocal().getApplication();

    String link = mailUtil.getLink(application, DataConstants.PERMISSIONS_UPDATED_MAIL);
    templateVariables.put("link", link);
    mailUtil.setPermissionsUpdatedTemplateVariables(application, userResponse, templateVariables);
    emailRecipient.setVariables(templateVariables);
    String eventId = mailUtil.getEventId(userResponse, application, DataConstants.PERMISSIONS_UPDATED_MAIL);
    NotificationRequest notificationRequest = NotificationRequest.builder().multicast(Arrays.asList(EMAIL))
        .eventId(eventId).referenceId(env.getProperty(SPRING_APP_NAME))
        .emailRecipients(Arrays.asList(emailRecipient)).build();
    LOGGER.info("gcs send email request {}", notificationRequest);
    if (StringUtils.equals(application, DataConstants.PORTAL)
        || StringUtils.equals(application, DataConstants.PARTNER_PORTAL)) {
      gcsService.sendNotification(eventId, Arrays.asList(emailRecipient),
          getInAppRecipients(userResponse, application));
    }
  }

  @Override
  public void sendResetPasswordEmail(IdentityManagementUserResource userResource,
      TokenResponseResource tokenResponseResource, TokenType tokenType, String email, String application,
      EmailType emailType) {
    EmailRecipient emailRecipient = new EmailRecipient();
    emailRecipient.setTo(email.trim());
    Map<String, Object> templateVariables = new HashMap<>();
    templateVariables.put(LOGIN_NAME, userResource.getUserName());
    templateVariables.put(EXPIRE_DAYS, 1);

    String uriParameters = "?uuid=" + userResource.getExternalId() + "&token=" + tokenResponseResource.getId()
        + "&application=" + application;

    String link = resetPasswordHostUiUrl + RESET_PASS_URI;
    if (application.equals(DataConstants.SKRILL)) {
      link = skrillResetPasswordHostUiUrl + RESET_PASS_URI;
    } else if (application.equals(DataConstants.NETELLER)) {
      link = netellerResetPasswordHostUiUrl + RESET_PASS_URI;
    }

    link = link + uriParameters;
    templateVariables.put("link", link);
    emailRecipient.setVariables(templateVariables);

    String eventId = getEventBasedOnApplicationAndEmailType(emailType, application, templateVariables, userResource);
    NotificationRequest notificationRequest =
        NotificationRequest.builder().multicast(Collections.singletonList(EMAIL)).eventId(eventId)
            .referenceId(env.getProperty(SPRING_APP_NAME))
            .emailRecipients(Collections.singletonList(emailRecipient)).build();
    LOGGER.info("gcs send email request {}", notificationRequest);
    gcsService.sendNotification(eventId, Collections.singletonList(emailRecipient), null);

  }

  private String getEventBasedOnApplicationAndEmailType(EmailType emailType, String application,
      Map<String, Object> templateVariables, IdentityManagementUserResource userResource) {
    String eventId = null;
    if (SELF_EMAIL.compareTo(emailType) == 0) {
      String customerName = (StringUtils.isNotEmpty(userResource.getFirstName()) ? userResource.getFirstName() : "")
          + " " + (StringUtils.isNotEmpty(userResource.getLastName()) ? userResource.getLastName() : "");
      templateVariables.put(CUSTOMER_NAME, WordUtils.capitalizeFully(customerName));
      eventId = selfEmailResetPasswordEventId;
      if (application.equals(DataConstants.SKRILL)) {
        eventId = selfEmailResetPasswordSkrillEventId;
      } else if (application.equals(DataConstants.NETELLER)) {
        eventId = selfEmailResetPasswordNetellerEventId;
      }
    } else if (AUTH_CONTACT_EMAIL.compareTo(emailType) == 0) {
      eventId = authContactEmailResetPasswordEventId;
    }
    return eventId;
  }

  private void sendMailToBusinessContactEmails(UserProvisioningUserResource userResponse) {
    List<AccessResources> accessResources = userResponse.getAccessResources();
    if (CollectionUtils.isNotEmpty(accessResources)) {
      for (AccessResources accessResource : accessResources) {
        if (StringUtils.equals(accessResource.getRole(), DataConstants.ADMIN)) {
          sendBusinessContactMail(userResponse, accessResource);
        }
      }
    }
  }

  private void sendBusinessContactMail(UserProvisioningUserResource userResponse, AccessResources accessResource) {
    SkrillContactEmailsResource skrillContactEmails =
        skrillTellerAccountInfoService.getSkrillContactEmails(accessResource.getId());
    List<ContactEmail> contactEmails = skrillContactEmails.getContactEmails();
    if (CollectionUtils.isNotEmpty(contactEmails)) {
      List<EmailRecipient> emailRecipients = new ArrayList<>();
      Map<String, Object> templateVariables = new HashMap<>();
      templateVariables.put("userName", userResponse.getUserName());
      templateVariables.put("customerId", accessResource.getId());
      boolean isBusinessContactFound = false;
      for (ContactEmail contactEmail : contactEmails) {
        if (StringUtils.equals(DataConstants.BUSINESS, contactEmail.getType())) {
          EmailRecipient emailRecipient = new EmailRecipient();
          emailRecipient.setTo(contactEmail.getContactEmail());
          emailRecipient.setVariables(templateVariables);
          emailRecipients.add(emailRecipient);
          isBusinessContactFound = true;
        }
      }
      if (isBusinessContactFound) {
        String eventId = userProvisioningConfig.getUser().getGcsEvents().getSkrill()
            .get(DataConstants.SKRILL_BUSINESS_CONTACT_EMAIL);
        NotificationRequest notificationRequest =
            NotificationRequest.builder().multicast(Arrays.asList(EMAIL)).eventId(eventId)
                .referenceId(env.getProperty(SPRING_APP_NAME)).emailRecipients(emailRecipients).build();
        LOGGER.info("gcs send email request {}", notificationRequest);
        gcsService.sendNotification(eventId, emailRecipients, null);
      }
    }
  }

  @Override
  public void sendMfaStatusUpdate(User user, Boolean isMfa) {
    String eventId;
    String adminUserId = CommonThreadLocal.getAuthLocal().getPaysafeId();
    if (StringUtils.equals(adminUserId, user.getUserId())) {
      if (isMfa) {
        eventId = mailUtil.getEventIdForMfa(DataConstants.PROFILE_SETTINGS_MFA_ENABLED_EVENTID);
        LOGGER.debug("Self Update Email");
        gcsService.sendNotification(eventId, Collections.singletonList(getEmailRecipientAndLog(user, eventId)), null);
      } else {
        eventId = mailUtil.getEventIdForMfa(DataConstants.PROFILE_SETTINGS_MFA_DISABLED_EVENTID);
        LOGGER.debug("Self Update InApp");
        gcsService.sendNotification(eventId, null, getInAppRecipients(user, StringUtils.EMPTY, false));
      }
    } else {
      if (isMfa) {
        eventId = mailUtil.getEventIdForMfa(DataConstants.ADMIN_MFA_ENABLED_INAPP_EVENTID);
        LOGGER.debug("Admin mfa enabled inApp");
        gcsService.sendNotification(eventId, null, getInAppRecipients(user, StringUtils.EMPTY, true));

        eventId = mailUtil.getEventIdForMfa(DataConstants.SUBUSER_MFA_ENABLED_EMAIL_EVENTID);
        LOGGER.debug("Sub User MFA enabled Email");
        gcsService.sendNotification(eventId, Collections.singletonList(getEmailRecipientAndLog(user, eventId)), null);
      } else {
        eventId = mailUtil.getEventIdForMfa(DataConstants.ADMIN_MFA_DISABLED_INAPP_EVENTID);
        LOGGER.debug("Admin mfa disabled inApp");
        gcsService.sendNotification(eventId, null, getInAppRecipients(user, adminUserId, true));

        eventId = mailUtil.getEventIdForMfa(DataConstants.SUBUSER_MFA_DISABLED_EMAIL_EVENTID);
        LOGGER.debug("Sub user mfa disabled Email");
        gcsService.sendNotification(eventId, Collections.singletonList(getEmailRecipientAndLog(user, eventId)), null);
      }
    }
  }

  private EmailRecipient getEmailRecipientAndLog(User user, String eventId) {
    EmailRecipient emailRecipient = new EmailRecipient();
    Map<String, Object> templateVariables = new HashMap<>();
    emailRecipient.setTo(user.getEmail().trim());
    templateVariables.put(LOGIN_NAME, user.getLoginName());
    emailRecipient.setVariables(templateVariables);

    NotificationRequest notificationRequest =
        NotificationRequest.builder().multicast(Collections.singletonList(EMAIL)).eventId(eventId)
            .referenceId(env.getProperty(SPRING_APP_NAME))
            .emailRecipients(Collections.singletonList(emailRecipient)).build();
    LOGGER.info("gcs send email request {}", notificationRequest);
    return emailRecipient;
  }

  @Override
  public void sendResetMfaStatusEmail(User user) {
    String eventId;
    if (!StringUtils.equals(CommonThreadLocal.getAuthLocal().getPaysafeId(), user.getUserId())) {
      eventId = mailUtil.getEventIdForMfa(DataConstants.ADMIN_RESET_MFA_INAPP_EVENTID);
      LOGGER.debug("Admin Reset Inapp");
      gcsService.sendNotification(eventId, null, getInAppRecipients(user, StringUtils.EMPTY, true));
    }
    eventId = mailUtil.getEventIdForMfa(DataConstants.RESET_MFA_EMAIL_EVENTID);//resetMfaEmailEventId
    LOGGER.debug("User Reset Email");
    gcsService.sendNotification(eventId, Collections.singletonList(getEmailRecipientAndLog(user, eventId)), null);

  }

}
