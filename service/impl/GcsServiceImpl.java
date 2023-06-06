// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.gbp.commons.notifications.resources.EmailRecipient;
import com.paysafe.gbp.commons.notifications.resources.InAppRecipient;
import com.paysafe.gbp.commons.notifications.resources.NotificationRequest;
import com.paysafe.upf.user.provisioning.feignclients.PushNotificationsFeignClient;
import com.paysafe.upf.user.provisioning.service.GcsService;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GcsServiceImpl implements GcsService {
  private static final Logger logger = LoggerFactory.getLogger(GcsServiceImpl.class);

  private static final String SPRING_APPLICATION_NAME_STRING = "spring.application.name";
  private static final String EMAIL_STRING = "EMAIL";
  private static final String IN_APP_STRING = "IN_APP";
  private static final String GCS_BAD_REQUEST_MESSAGE = "Bad Request : gcs notification request {}";
  private static final String GCS_REQUEST_MESSAGE = "gcs notification request {}";

  @Autowired
  private Environment env;

  @Autowired
  private PushNotificationsFeignClient gcsClient;

  @Override
  public void sendNotification(String eventId, List<EmailRecipient> emailRecipients,
      List<InAppRecipient> inAppRecipients) {
    NotificationRequest notificationRequest = NotificationRequest.builder().eventId(eventId)
        .referenceId(env.getProperty(SPRING_APPLICATION_NAME_STRING)).build();

    logger.info("gcs send email request {}", notificationRequest);
    validateAndSendNotification(notificationRequest, inAppRecipients, emailRecipients);
  }

  private void validateAndSendNotification(NotificationRequest notificationRequest,
      List<InAppRecipient> inAppRecipients, List<EmailRecipient> emailRecipients) {
    List<String> multicast = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(inAppRecipients)) {
      notificationRequest.setInAppRecipients(inAppRecipients);
      multicast.add(IN_APP_STRING);
    }
    if (CollectionUtils.isNotEmpty(emailRecipients)) {
      notificationRequest.setEmailRecipients(emailRecipients);
      multicast.add(EMAIL_STRING);
    }
    if (multicast.isEmpty()) {
      logger.warn(GCS_BAD_REQUEST_MESSAGE, notificationRequest);
    } else {
      notificationRequest.setMulticast(multicast);
      logger.info(GCS_REQUEST_MESSAGE, notificationRequest);
      gcsClient.sendNotification(notificationRequest);
    }
  }
}
