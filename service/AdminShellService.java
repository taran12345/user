// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.web.rest.resource.AdminShellResetPasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetEmailStatusResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetPasswordRequestResource;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface AdminShellService {
  void triggerResetEmail(AdminShellResetPasswordRequestResource requestResource, String applicationName);

  void resetPassword(String userId, ResetPasswordRequestResource resetPasswordRequestResource, String application)
      throws JsonProcessingException;

  ResetEmailStatusResponseResource getResetEmailStatus(String userId);
}
