// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.PegasusFeignClient;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.ChangePasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.OktaChangePasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUpdateUserRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserListResponseResource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class UserPasswordManagementUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserProvisioningUtils.class);

  @Autowired
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Autowired
  private AuditService auditService;

  @Autowired
  private PegasusFeignClient pegasusFeignClient;

  @Autowired
  private UsersRepository usersRepository;

  /**
   * Updates the password in pegasus.
   */
  public void updatePasswordInPegasus(String userId, String newPassword, AuditEventType auditEventType) {
    AuthorizationInfo authorizationInfo = CommonThreadLocal.getAuthLocal();
    CommonThreadLocal.setAuthLocal(null);
    ResponseEntity<IdentityManagementUserResource> userDetails =
        identityManagementFeignClient.getUser(userId, authorizationInfo.getApplication());
    String loginName;
    IdentityManagementUserResource body = userDetails.getBody();
    if (body != null && userDetails.getStatusCode().equals(HttpStatus.OK)) {
      loginName =  body.getUserName();
    } else {
      LOGGER.error("Get user failed at OKTA for the userId : {}", LoggingUtil.replaceSpecialChars(userId));
      auditService.createAuditEntry(AuditUserEventDto.builder().eventType(auditEventType).targetUserName(userId)
          .eventStatus(AuditEventStatus.FAILED).targetUserId(userId).eventTimeStamp(DateTime.now(DateTimeZone.UTC))
          .build());
      throw new BadRequestException.Builder().errorCode(CommonErrorCode.INVALID_FIELD).details("UserId not found")
          .build();
    }
    PegasusUserListResponseResource pegasusUserListResponseResource =
        pegasusFeignClient.getUsers(loginName, null, null, null);
    if (pegasusUserListResponseResource.getUsers() == null || pegasusUserListResponseResource.getUsers().isEmpty()) {
      CommonThreadLocal.setAuthLocal(authorizationInfo);
      return;
    }
    PegasusUpdateUserRequestResource pegasusUpdateUserRequestResource = new PegasusUpdateUserRequestResource();
    pegasusUpdateUserRequestResource.setNewPassword(newPassword);
    pegasusFeignClient.updateUser(pegasusUpdateUserRequestResource, loginName);
    CommonThreadLocal.setAuthLocal(authorizationInfo);
  }

  /**
   * Updates the password in OKTA.
   */
  public void updatePasswordInOkta(String userId, ChangePasswordRequestResource changePasswordRequestResource) {
    OktaChangePasswordRequestResource oktaChangePasswordRequestResource = new OktaChangePasswordRequestResource();
    BeanUtils.copyProperties(changePasswordRequestResource, oktaChangePasswordRequestResource);
    User user = usersRepository.findByUserId(userId);
    if (user != null) {
      userId = user.getUserExternalId();
    }
    ResponseEntity<String> idmChangePwdResponse =
        identityManagementFeignClient.changePassword(oktaChangePasswordRequestResource, userId);
    if (!idmChangePwdResponse.getStatusCode().equals(HttpStatus.OK)) {
      auditService.createAuditEntry(AuditUserEventDto.builder().eventType(AuditEventType.CHANGE_PASSWORD)
          .targetUserName(userId).eventStatus(AuditEventStatus.FAILED)
          .targetUserId(user != null ? user.getUserId() : null).eventTimeStamp(DateTime.now(DateTimeZone.UTC)).build());
      LOGGER.error("Password update failed at OKTA for the user : {}", LoggingUtil.replaceSpecialChars(userId));
      throw InternalErrorException.builder().details("Failed to update new password").build();
    }
  }


}
