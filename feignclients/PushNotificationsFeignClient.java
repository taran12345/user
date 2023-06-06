// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients;

import com.paysafe.gbp.commons.notifications.resources.NotificationRequest;
import com.paysafe.upf.user.provisioning.config.FeignClientConfig;
import com.paysafe.upf.user.provisioning.feignclients.fallbacks.PushNotificationFeignClientFallback;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "paysafe-ss-pushnotifications", configuration = FeignClientConfig.class,
    fallback = PushNotificationFeignClientFallback.class)
public interface PushNotificationsFeignClient {

  @RequestMapping(value = "admin/notifications/v2/notifications", method = RequestMethod.POST)
  void sendNotification(@RequestBody NotificationRequest notificationRequest);
}
