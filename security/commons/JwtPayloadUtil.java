// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.security.commons;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

/**
 * This is a Utility class to retrieve JWT Token.
 * 
 * @author meharchandra
 *
 */
public final class JwtPayloadUtil {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtPayloadUtil.class);

  private JwtPayloadUtil() {
  }

  /**
   * This is a utility function to retrieve payload from the token.
   * 
   * @param auth jwt token
   * @return payload JSONObject
   */
  public static AuthorizationInfo retrievePayload(String auth) {
    try {
      String unsignedJwt = auth.split("\\.")[1];
      String payloadData = new String(Base64.getDecoder().decode(unsignedJwt), "UTF-8");
      return MAPPER.readValue(payloadData, AuthorizationInfo.class);
    } catch (Exception e) {
      LOGGER.error("Could not decode JWT", e);
      throw BadRequestException.builder().requestNotParsable().cause(e).build();
    }
  }
}
