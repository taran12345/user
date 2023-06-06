// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients.fallbacks;

import com.paysafe.gbp.commons.notifications.resources.NotificationRequest;
import com.paysafe.op.errorhandling.exceptions.ExternalGatewayErrorException;
import com.paysafe.upf.user.provisioning.feignclients.PushNotificationsFeignClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationFeignClientFallback implements PushNotificationsFeignClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(PushNotificationFeignClientFallback.class);
  private static final String LOG_MESSAGE =
      "paysafe-ss-pushnotifications  service circuit is open, falling back to failover";

  @Override
  public void sendNotification(NotificationRequest notificationRequest) {
    LOGGER.warn(LOG_MESSAGE);
    throw ExternalGatewayErrorException.builder().build();
  }
}
