// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants.TOKEN_EXPIRED;
import static com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants.TOKEN_NOT_FOUND_LOGIN_NAME;
import static com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants.USER_NOT_FOUND;

import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.op.errorhandling.exceptions.UnauthorizedException;
import com.paysafe.upf.user.provisioning.domain.UserToken;
import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;
import com.paysafe.upf.user.provisioning.enums.TokenType;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.repository.TokenRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.service.TokenService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.utils.LoggingUtil;
import com.paysafe.upf.user.provisioning.web.rest.assembler.TokenAssembler;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto.AuditUserEventDtoBuilder;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateTokenResource;

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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenServiceImpl implements TokenService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);
  private ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private UserService userService;

  @Autowired
  private TokenRepository tokenRepository;

  @Autowired
  private AuditService auditService;

  /**
   * Creates token for a user.
   *
   * @param requestResource requestResource
   * @param userId userId
   * @return TokenResponseResource
   */
  @Override
  public TokenResponseResource createToken(TokenRequestResource requestResource, String userId,
      String authContactEmail) {
    LOGGER.info("Token creation request for userId: {}", LoggingUtil.replaceSpecialChars(userId));
    IdentityManagementUserResource userResource = userService.fetchUser(userId);
    if (userResource == null) {
      LOGGER.warn("No user found for userId: {}", LoggingUtil.replaceSpecialChars(userId));
      throw new NotFoundException.Builder().entityNotFound().detail(USER_NOT_FOUND).build();
    }
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    List<UserToken> previousTokensList = tokenRepository.findByLoginNameAndTokenTypeAndApplication(
        userResource.getUserName(), requestResource.getTokenType().name(), application);

    if (!CollectionUtils.isEmpty(previousTokensList)) {
      for (UserToken token : previousTokensList) {
        if (token.getExpiryDate().isAfterNow()) {
          token.setExpiryDate(DateTime.now());
          token.setLastModifiedBy(userResource.getUserName());
          token.setLastModifiedDate(DateTime.now());
          tokenRepository.save(token);
        }
      }
    }

    UserToken userToken = TokenAssembler.toUserToken(requestResource, userResource);
    userToken.setApplication(application);
    if (StringUtils.isNotEmpty(authContactEmail)) {
      userToken.setAuthContactEmail(authContactEmail);
    }

    UserToken response = tokenRepository.save(userToken);
    return TokenAssembler.toUserTokenResponseResource(response);
  }

  /**
   * Get token by user's userId and tokenId.
   *
   * @param userId userId
   * @param tokenId tokenId
   * @param tokenType tokenType
   * @return TokenResponseResource
   * @throws JsonProcessingException e.
   */
  @Override
  public TokenResponseResource getToken(String userId, String tokenId, TokenType tokenType)
      throws JsonProcessingException {
    IdentityManagementUserResource userResource = userService.fetchUser(userId);
    if (userResource == null) {
      LOGGER.warn("No user found for userId: {} ", LoggingUtil.replaceSpecialChars(userId));
      throw new NotFoundException.Builder().entityNotFound().detail(USER_NOT_FOUND).build();
    }
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    UserToken userToken = tokenRepository.findByLoginNameAndTokenAndTokenTypeAndApplication(userResource.getUserName(),
        tokenId, tokenType.name(), application);
    if (userToken == null) {
      LOGGER.warn("No token found for userId: {}", LoggingUtil.replaceSpecialChars(userId));
      throw new NotFoundException.Builder().entityNotFound()
          .detail(TOKEN_NOT_FOUND_LOGIN_NAME, userResource.getUserName()).build();
    }

    if (userToken.getExpiryDate().isBeforeNow()) {
      LOGGER.warn("Token expired for token id: {}", LoggingUtil.replaceSpecialChars(tokenId));
      AuditUserEventDtoBuilder auditUserEventDtoBuilder = AuditUserEventDto.builder()
          .eventTimeStamp(DateTime.now(DateTimeZone.UTC)).targetUserName(userResource.getUserName())
          .targetUserId(userResource.getId()).createdBy(userResource.getEmail())
          .eventData(constructEventData("Token expired", "Token has been expired"))
          .eventStatus(AuditEventStatus.FAILED);
      if (userResource.getStatus().equals(UserStatus.PROVISIONED)) {
        auditUserEventDtoBuilder.eventType(AuditEventType.SIGNUP);
      } else {
        auditUserEventDtoBuilder.eventType(AuditEventType.FORGOT_PASSWORD);
      }
      auditService.createAuditEntry(auditUserEventDtoBuilder.build());
      throw new UnauthorizedException.Builder().expired().details(TOKEN_EXPIRED).build();
    }

    return TokenAssembler.toUserTokenResponseResource(userToken);
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
      LOGGER.error("Error while constructing Audit Event Data: {}.", e.getMessage());
    }
    return jsonString;
  }

  /**
   * Update token by user's uuid and tokenId.
   *
   * @param updateTokenResource update token resource
   * @param uuid user's uuid
   * @param tokenId token id
   * @return TokenResponseResource
   */
  @Override
  public TokenResponseResource updateToken(UpdateTokenResource updateTokenResource, String uuid, String tokenId) {
    IdentityManagementUserResource userResource = userService.fetchUser(uuid);
    if (userResource == null) {
      LOGGER.warn("No user found for userId: {} ", LoggingUtil.replaceSpecialChars(uuid));
      throw new NotFoundException.Builder().entityNotFound().detail(USER_NOT_FOUND).build();
    }
    return expireToken(updateTokenResource, userResource, tokenId);
  }

  @Override
  public TokenResponseResource expireToken(UpdateTokenResource updateTokenResource,
      IdentityManagementUserResource userResource, String tokenId) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    UserToken userToken = tokenRepository.findByLoginNameAndTokenAndTokenTypeAndApplication(userResource.getUserName(),
        tokenId, updateTokenResource.getTokenType().name(), application);
    if (userToken == null) {
      LOGGER.warn("No token found for token id: {}", LoggingUtil.replaceSpecialChars(tokenId));
      throw new NotFoundException.Builder().entityNotFound()
          .detail(TOKEN_NOT_FOUND_LOGIN_NAME, userResource.getUserName()).build();
    }
    if (userToken.getExpiryDate().isBeforeNow()) {
      LOGGER.warn("Token expired for token id: {}", LoggingUtil.replaceSpecialChars(tokenId));
      throw new UnauthorizedException.Builder().expired().details(TOKEN_EXPIRED).build();
    }
    userToken.setExpiryDate(DateTime.now().plus(updateTokenResource.getTimeToLiveInSeconds() * 1000));
    UserToken response = tokenRepository.save(userToken);
    return TokenAssembler.toUserTokenResponseResource(response);
  }

}
