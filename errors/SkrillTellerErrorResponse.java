// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.errors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkrillTellerErrorResponse {

  private static final String NOT_FOUND_CUSTOMER_CODE = "NOT_FOUND_CUSTOMER";

  private static final String BAD_REQUEST_CODE = "BAD_REQUEST";

  private static final String CUSTOMER_ID = "customerId";

  private static final String INVALID_CUSTOMER_ID_MESSAGE = " customerId is invalid.";

  private static final String MISSING_PARAMETER_CODE = "MISSING_PARAMETER";

  private String code;

  private String message;

  private String parameter;

  private int httpStatus;

  private Error error;

  /**
   * Get user friendly error message.
   *
   * @return String message.
   */
  public String getUserFriendlyErrorMessage() {
    if (httpStatus == 400) {
      return getMessageFor400Status();
    }
    return message;
  }

  /**
   * Get user friendly message for 400 http status.
   *
   * @return String message.
   */
  private String getMessageFor400Status() {

    if (code.equals(NOT_FOUND_CUSTOMER_CODE) || code.equals(MISSING_PARAMETER_CODE)) {
      return message;
    } else if (code.equals(BAD_REQUEST_CODE) && message.contains(CUSTOMER_ID)) {
      return INVALID_CUSTOMER_ID_MESSAGE;
    }

    if (message.contains(":")) {
      String[] messages = message.split(":");
      if (messages.length >= 2) {
        return messages[1];
      }
    }
    return message;
  }

}
