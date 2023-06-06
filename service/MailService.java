// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.enums.EmailType;
import com.paysafe.upf.user.provisioning.enums.TokenType;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;

public interface MailService {

  void sendRegistrationConfirmationEmail(UserProvisioningUserResource userResponse);

  void sendPermissionsUpdatedConfirmationEmail(UserProvisioningUserResource userResponse);

  void sendResetPasswordEmail(IdentityManagementUserResource userResource, TokenResponseResource tokenResponseResource,
      TokenType tokenType, String email, String application, EmailType emailType);

  void sendMfaStatusUpdate(User user, Boolean isMfa);

  void sendResetMfaStatusEmail(User user);


}
