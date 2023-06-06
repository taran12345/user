// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.gbp.commons.notifications.resources.EmailRecipient;
import com.paysafe.gbp.commons.notifications.resources.InAppRecipient;

import java.util.List;

public interface GcsService {

  void sendNotification(String eventId, List<EmailRecipient> emailRecipients, List<InAppRecipient> inAppRecipients);

}