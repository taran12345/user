// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.migration.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.paysafe.gbp.commons.notifications.resources.EmailRecipient;
import com.paysafe.gbp.commons.notifications.resources.InAppRecipient;
import com.paysafe.gbp.commons.notifications.resources.NotificationRequest;
import com.paysafe.upf.user.provisioning.feignclients.PushNotificationsFeignClient;
import com.paysafe.upf.user.provisioning.service.impl.GcsServiceImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class GcsServiceImplTest {

  @Mock
  private Environment env;

  @Mock
  private PushNotificationsFeignClient gcsClient;

  @InjectMocks
  private GcsServiceImpl gcsService;

  @Test
  public void sendNotificationTest() {
    doNothing().when(gcsClient).sendNotification(any(NotificationRequest.class));
    EmailRecipient emailRecipient = new EmailRecipient();
    emailRecipient.setTo("abcd");
    emailRecipient.setMailBoxName("abcd@mail.com");
    InAppRecipient inappRecipient = new InAppRecipient();
    inappRecipient.setClassifier("classifier");
    gcsService.sendNotification("1234", Arrays.asList(emailRecipient), Arrays.asList(inappRecipient));
    verify(gcsClient, times(1)).sendNotification(any());
  }
}
