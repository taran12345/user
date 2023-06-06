// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.gbp.commons.notifications.resources.EmailRecipient;
import com.paysafe.gbp.commons.notifications.resources.InAppRecipient;
import com.paysafe.gbp.commons.notifications.resources.NotificationRequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class GcsNotificationRequest extends NotificationRequest {
  private Map<Locale, EmailTemplate> emailTemplates;

  /**
   * parameterized constructor.
   */
  @Builder(builderMethodName = "childBuilder")
  public GcsNotificationRequest(String eventId, String referenceId, List<String> multicast,
      boolean enableTransportFailover, boolean scheduled, boolean deliveryReportNeeded, List<String> attachments,
      List<EmailRecipient> emailRecipients, List<InAppRecipient> inAppRecipients,
      Map<Locale, EmailTemplate> emailTemplates) {
    super(eventId, referenceId, multicast, enableTransportFailover, scheduled, deliveryReportNeeded, attachments,
        emailRecipients, inAppRecipients);
    this.emailTemplates = emailTemplates;
  }
}
