// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.errors;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.ErrorCode;
import com.paysafe.op.errorhandling.exceptions.OneplatformException;
import com.paysafe.upf.user.provisioning.exceptions.UserProvisioningException;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeignErrorDecoderV2 implements ErrorDecoder {

  private static final Logger logger = LoggerFactory.getLogger(FeignErrorDecoderV2.class);
  private static final String ERROR = "error";
  private static final String FIELD_ERRORS = "fieldErrors";
  private static final String FIELD = "field";
  private static final String DETAILS = "details";
  private static final String MESSAGE = "message";
  private static final String GENERIC_ERROR_MSG = "Something went wrong!";
  private static Map<String, ErrorCode> errorCodeMap = new HashMap<>();

  /**
   * Error decoder for FeignClient.
   */
  public FeignErrorDecoderV2() {
    for (CommonErrorCode errorCode : CommonErrorCode.values()) {
      errorCodeMap.put(errorCode.getCode(), errorCode);
    }

  }

  @Override
  public OneplatformException decode(String methodKey, Response response) {
    JSONObject responseError = null;

    if (response.body() != null) {
      try {
        Reader responseBodyReader = response.body().asReader();
        responseError = (JSONObject) new JSONObject(Util.toString(responseBodyReader)).get(ERROR);
      } catch (IOException e) {
        logger.error("Feign client could not decode response body");
      } catch (JSONException e) {
        logger.error("Feign client could not parse the error from the response body");
      }
    }

    if (responseError == null) {
      return UserProvisioningException.builder().errorCode(UserProvisioningErrorCodes.UPR_ERROR)
          .create(HttpStatus.valueOf(response.status()));
    }
    OneplatformException exception = UserProvisioningException.builder().errorCode(getErrorCode(responseError))
        .details(getDetailsFromResponseError(responseError)).create(HttpStatus.valueOf(response.status()));
    getFieldErrorsFromResponse(responseError, exception);
    return exception;
  }

  private void getFieldErrorsFromResponse(JSONObject responseError, OneplatformException exception) {
    List<FieldError> fieldErrorList = new ArrayList<>();
    try {
      JSONArray fieldErrors = responseError.getJSONArray(FIELD_ERRORS);
      for (int i = 0; i < fieldErrors.length(); i++) {
        JSONObject fieldError = fieldErrors.getJSONObject(i);
        fieldErrorList.add(new FieldError("ErrorDecoder", fieldError.getString(FIELD), fieldError.getString(ERROR)));
      }
    } catch (JSONException e) {
      logger.error("Field errors could not be parsed by feign client");
    }
    if (!fieldErrorList.isEmpty()) {
      exception.setFieldErrorList(fieldErrorList);
    }
  }

  private String[] getDetailsFromResponseError(JSONObject responseError) {
    try {
      JSONArray detailsArray = responseError.getJSONArray(DETAILS);
      String[] details = new String[detailsArray.length()];
      for (int i = 0; i < detailsArray.length(); i++) {
        details[i] = detailsArray.getString(i);
      }
      return details;
    } catch (JSONException e) {
      logger.error("Details could not be parsed from response error");
      return new String[0];
    }
  }

  private String getMessageFromResponseError(JSONObject responseError) {
    try {
      return responseError.getString(MESSAGE);
    } catch (JSONException e) {
      logger.error("Details could not be parsed from response error");
      return GENERIC_ERROR_MSG;
    }
  }

  private ErrorCode getErrorCode(JSONObject responseError) {
    String code = responseError.optString("code");
    ErrorCode errorCode = errorCodeMap.get(code);
    if (errorCode == null) {
      if (code == null) {
        return UserProvisioningErrorCodes.UPR_ERROR;
      }
      return new DyErrorCode(code, getMessageFromResponseError(responseError), GENERIC_ERROR_MSG);
    }
    return errorCode;
  }

  static class DyErrorCode implements ErrorCode {

    private String code;
    private String message;
    private String description;

    DyErrorCode(String code, String message, String description) {
      this.code = code;
      this.message = message;
      this.description = description;
    }

    @Override
    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }

    @Override
    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    @Override
    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }
}
