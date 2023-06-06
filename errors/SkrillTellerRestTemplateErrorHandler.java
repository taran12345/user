// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.errors;

import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.op.errorhandling.exceptions.InvalidFieldException;
import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.op.errorhandling.exceptions.UnauthorizedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.validation.FieldError;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SkrillTellerRestTemplateErrorHandler implements ResponseErrorHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(SkrillTellerRestTemplateErrorHandler.class);

  private static final String FIELD = "Field";

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
    LOGGER.debug("checking REST TEMPLATE error");
    return new DefaultResponseErrorHandler().hasError(httpResponse);
  }

  @Override
  public void handleError(ClientHttpResponse httpResponse) throws IOException {
    String httpBodyResponse = null;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getBody()))) {
      httpBodyResponse = reader.lines().collect(Collectors.joining(""));
    }
    LOGGER.error("Handling REST TEMPLATE error status: {} , errorResponse: {} ", httpResponse.getStatusCode(),
        httpBodyResponse);

    SkrillTellerErrorResponse skrillTellerErrorResponse =
        objectMapper.readValue(httpBodyResponse, SkrillTellerErrorResponse.class);
    skrillTellerErrorResponse.setHttpStatus(httpResponse.getStatusCode().value());

    if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
      LOGGER.error("throwing InternalErrorException");
      throw InternalErrorException.builder().build();

    } else if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {

      if (httpResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
        List<FieldError> fieldErrors = new ArrayList<>();
        prepareFieldErrorsForBadRequest(skrillTellerErrorResponse, fieldErrors);
        LOGGER.error("throwing InvalidFieldException");
        throw new InvalidFieldException(fieldErrors);
      }

      if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
        LOGGER.error("throwing NotFoundException");
        if (skrillTellerErrorResponse.getError() != null) {
          throw NotFoundException.builder().details(skrillTellerErrorResponse.getError().getDetails().get(0))
              .entityNotFound().build();
        } else {
          throw NotFoundException.builder().details(skrillTellerErrorResponse.getMessage()).entityNotFound().build();
        }
      }

      if (httpResponse.getStatusCode() == HttpStatus.UNAUTHORIZED) {

        LOGGER.error("throwing UnauthorizedException");
        LOGGER.error("error response body: {}", httpBodyResponse);
        throw UnauthorizedException.builder().details(skrillTellerErrorResponse.getUserFriendlyErrorMessage())
            .errorCode(UserProvisioningErrorCodes.UNAUTHORIZED_ACCESS).build();
      }

    }
  }

  private void prepareFieldErrorsForBadRequest(SkrillTellerErrorResponse skrillTellerErrorResponse,
      List<FieldError> fieldErrors) {
    if (skrillTellerErrorResponse.getParameter() != null) {
      fieldErrors.add(new FieldError(FIELD, skrillTellerErrorResponse.getParameter(),
          skrillTellerErrorResponse.getUserFriendlyErrorMessage()));
    } else if (skrillTellerErrorResponse.getMessage() != null) {
      fieldErrors.add(new FieldError(FIELD, "", skrillTellerErrorResponse.getMessage()));
    } else if (skrillTellerErrorResponse.getError() != null) {
      for (String detail : skrillTellerErrorResponse.getError().getDetails()) {
        fieldErrors.add(new FieldError(FIELD, "", detail));
      }
    }
  }
}