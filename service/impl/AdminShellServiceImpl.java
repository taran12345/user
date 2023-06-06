// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static com.paysafe.op.errorhandling.CommonErrorCode.ENTITY_NOT_FOUND;
import static com.paysafe.upf.user.provisioning.enums.EmailType.AUTH_CONTACT_EMAIL;
import static com.paysafe.upf.user.provisioning.enums.EmailType.SELF_EMAIL;
import static com.paysafe.upf.user.provisioning.enums.TokenType.PASSWORD_RECOVERY;
import static com.paysafe.upf.user.provisioning.enums.UserStatus.ACTIVE;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.upf.user.provisioning.domain.UserToken;
import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.enums.EmailType;
import com.paysafe.upf.user.provisioning.enums.TokenType;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.repository.TokenRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AdminShellService;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.service.MailService;
import com.paysafe.upf.user.provisioning.service.TokenService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AdminShellResetPasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.LogoutRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetEmailStatusResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetPasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RefreshScope
@Service
public class AdminShellServiceImpl implements AdminShellService {

  private static final Logger logger = LoggerFactory.getLogger(AdminShellServiceImpl.class);
  private static final String DUMMY_EMAIL = "dummy@dummy.com";
  private static final Long PASSWORD_TOKEN_EXPIRY_TIME_SEC_ADMIN_SHELL = 60 * 60 * 24L;
  private static final String LOGOUT_APPLICATION_NAME = "BusinessPortal";
  private static final String LOGOUT_BRAND = "Paysafe";
  private ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  IdentityManagementFeignClient identityManagementFeignClient;

  @Autowired
  TokenService tokenService;

  @Autowired
  UserService userService;

  @Autowired
  private TokenRepository tokenRepository;

  @Autowired
  private MailService mailService;

  @Autowired
  private AuditService auditService;

  @Override
  public void triggerResetEmail(AdminShellResetPasswordRequestResource requestResource, String applicationName) {
    validateRequest(requestResource);
    ResponseEntity<IdentityManagementUserResource> response =
        identityManagementFeignClient.getUser(requestResource.getLoginName(), applicationName);
    validateGetUserResponse(response, requestResource);
    IdentityManagementUserResource userResource = response.getBody();
    validateIfUserIsActive(userResource);
    checkSelfRequestOrAuthContactRequest(requestResource, userResource, applicationName);
  }

  @Override
  public void resetPassword(String userId, ResetPasswordRequestResource resetPasswordRequestResource,
      String application) throws JsonProcessingException {
    tokenService.getToken(userId, resetPasswordRequestResource.getValidationToken(), PASSWORD_RECOVERY);
    userService.resetPassword(userId, resetPasswordRequestResource, application);
    logoutUserFromAllActiveSessions(userId, application);
  }

  @Override
  public ResetEmailStatusResponseResource getResetEmailStatus(String userId) {
    ResetEmailStatusResponseResource statusResponseResource = new ResetEmailStatusResponseResource();
    statusResponseResource.setStatus("NOT_PENDING");
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    List<UserToken> allTokensList =
        tokenRepository.findByLoginNameAndTokenTypeAndApplication(userId, PASSWORD_RECOVERY.name(), application);
    if (!CollectionUtils.isEmpty(allTokensList)) {
      for (UserToken token : allTokensList) {
        if (token.getExpiryDate().isAfterNow()) {
          statusResponseResource.setStatus("PENDING");
          statusResponseResource
              .setEmailType(StringUtils.isEmpty(token.getAuthContactEmail()) ? SELF_EMAIL : AUTH_CONTACT_EMAIL);
          statusResponseResource.setRecipient(
              StringUtils.isEmpty(token.getAuthContactEmail()) ? token.getLoginName() : token.getAuthContactEmail());
          statusResponseResource.setSentTime(token.getCreatedDate());
          return statusResponseResource;
        }
      }
    }
    return statusResponseResource;
  }

  private void validateRequest(AdminShellResetPasswordRequestResource requestResource) {
    checkIfRequestContainsLoginName(requestResource);
  }

  private void validateIfUserIsActive(IdentityManagementUserResource userResource) {
    if (ACTIVE != userResource.getStatus()) {
      logger.warn("No active entry found to generate password token for given username: {}",
          userResource.getUserName());
      throw new BadRequestException.Builder().details("User is not active").errorCode(CommonErrorCode.ACCOUNT_DISABLED)
          .build();
    }
  }

  private void checkSelfRequestOrAuthContactRequest(AdminShellResetPasswordRequestResource requestResource,
      IdentityManagementUserResource userResource, String application) {
    if (StringUtils.isNotEmpty(requestResource.getLoginName())
        && StringUtils.isEmpty(requestResource.getAuthContactEmailId())) {
      if (Objects.equals(userResource.getEmail(), DUMMY_EMAIL)) {
        logger.info("User with dummy email");
        throw NotFoundException.builder().details("User with dummy email").errorCode(ENTITY_NOT_FOUND).build();
      } else {
        logger.debug("reset password email triggering to self mail");
        constructTokenAndTriggerEmail(userResource, userResource.getEmail(), application, SELF_EMAIL);
      }
    } else if (StringUtils.isNotEmpty(requestResource.getLoginName())
        && StringUtils.isNotEmpty(requestResource.getAuthContactEmailId())) {
      checkIfUserConsistsDummyEmail(userResource);
      logger.debug("reset password email triggering to auth-contact mail");
      constructTokenAndTriggerEmail(userResource, requestResource.getAuthContactEmailId(), application,
          AUTH_CONTACT_EMAIL);
    } else {
      logger.warn(
          "Invalid req, either user with email or username along with auth contact email and name should be provided");
      throw BadRequestException.builder().details("Invalid req, either user with email or username along "
          + "with auth contact email and name should be provided").build();
    }
  }

  private void checkIfUserConsistsDummyEmail(IdentityManagementUserResource userResource) {
    if (!Objects.equals(userResource.getEmail(), DUMMY_EMAIL)) {
      logger.error("User with dummy email can only request an authorized contact email trigger");
      throw BadRequestException.builder()
          .details("User with dummy email can only request an authorized contact email trigger").build();
    }
  }

  private void validateGetUserResponse(ResponseEntity<IdentityManagementUserResource> response,
      AdminShellResetPasswordRequestResource requestResource) {
    if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw InternalErrorException.builder()
          .details("Couldn't fetch the user with userId: " + requestResource.getLoginName()).build();
    }
  }

  private void checkIfRequestContainsLoginName(AdminShellResetPasswordRequestResource requestResource) {
    if (StringUtils.isEmpty(requestResource.getLoginName())) {
      throw BadRequestException.builder().details("Username not present in the request").build();
    }
  }

  private void constructTokenAndTriggerEmail(IdentityManagementUserResource userResource, String email,
      String application, EmailType emailType) {
    TokenRequestResource tokenRequestResource =
        new TokenRequestResource(PASSWORD_TOKEN_EXPIRY_TIME_SEC_ADMIN_SHELL, TokenType.PASSWORD_RECOVERY);
    TokenResponseResource tokenResponseResource;
    if (AUTH_CONTACT_EMAIL == emailType) {
      tokenResponseResource = tokenService.createToken(tokenRequestResource, userResource.getUserName(), email);
    } else {
      tokenResponseResource = tokenService.createToken(tokenRequestResource, userResource.getUserName(), null);
    }
    mailService.sendResetPasswordEmail(userResource, tokenResponseResource, TokenType.PASSWORD_RECOVERY, email,
        application, emailType);

    auditService.createAuditEntry(
        AuditUserEventDto.builder().eventType(AuditEventType.FORGOT_PASSWORD).targetUserName(userResource.getUserName())
            .targetUserId(userResource.getId())
            .eventStatus(AuditEventStatus.SUCCESS).eventTimeStamp(DateTime.now(DateTimeZone.UTC))
            .createdBy(getUserNameFromThreadLocal()).userStatus(userResource.getStatus().name())
            .eventData(constructEventData("Reset Password", getUserNameFromThreadLocal()
                + " has triggered forgot password mail to " + userResource.getEmail() + " successfully"))
            .build());
  }

  private String getUserNameFromThreadLocal() {
    if (CommonThreadLocal.getAuthLocal() == null) {
      return "SYSTEM";
    } else {
      return StringUtils.isEmpty(CommonThreadLocal.getAuthLocal().getUserName()) ? "SYSTEM"
          : CommonThreadLocal.getAuthLocal().getUserName();
    }
  }

  private String constructEventData(String reason, String details) {
    String jsonString = "";
    ObjectNode auditEventDataObjectNode = objectMapper.createObjectNode();
    auditEventDataObjectNode.put("reason", reason);
    if (details != null) {
      auditEventDataObjectNode.put("details", details);
    }
    try {
      jsonString = objectMapper.writeValueAsString(auditEventDataObjectNode);
    } catch (JsonProcessingException e) {
      logger.error("Error while constructing Audit Event Data: {}.", e.getMessage());
    }
    return jsonString;
  }

  private void logoutUserFromAllActiveSessions(String userId, String application) {
    logger.debug("Initiating logout call for user: " + userId);
    ResponseEntity<IdentityManagementUserResource> response =
        identityManagementFeignClient.getUser(userId, application);
    IdentityManagementUserResource identityManagementUser;
    if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
      identityManagementUser = response.getBody();
    } else {
      throw InternalErrorException.builder()
          .details("Couldn't perform logout operation, unable to fetch the user with userId: " + userId).build();
    }
    LogoutRequestResource logoutRequestResource = new LogoutRequestResource();
    logoutRequestResource.setUsername(identityManagementUser.getUserName());
    logoutRequestResource.setApplicationName(LOGOUT_APPLICATION_NAME);
    logoutRequestResource.setBrand(LOGOUT_BRAND);
    logoutRequestResource.setUserId(identityManagementUser.getExternalId());
    logoutRequestResource.setInternalId(identityManagementUser.getId());
    identityManagementFeignClient.internalLogout(logoutRequestResource);
  }

}
