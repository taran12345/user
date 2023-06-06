// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.paysafe.op.errorhandling.exceptions.InternalErrorException;
import com.paysafe.op.errorhandling.exceptions.InvalidFieldException;
import com.paysafe.op.errorhandling.exceptions.NotFoundException;
import com.paysafe.op.errorhandling.exceptions.UnauthorizedException;
import com.paysafe.upf.user.provisioning.errors.SkrillTellerRestTemplateErrorHandler;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class RestTemplateResponseErrorHandlerTest {

  private SkrillTellerRestTemplateErrorHandler handler = new SkrillTellerRestTemplateErrorHandler();

  private final ClientHttpResponse response = mock(ClientHttpResponse.class);
  public static final String NOT_FOUND_RESPONSE = "{\n"
      + "  \"code\": \"401\",\n"
      + "  \"error\": {\n"
      + "    \"code\": \"5269\",\n"
      + "    \"message\": \"Entity not found\",\n"
      + "    \"details\": [\n"
      + "      \"The ID(s) specified in the URL do not correspond to the values in the system.\"\n"
      + "    ]\n"
      + "  }\n"
      + "}";

  @Test
  public void hasErrorTrue() throws Exception {
    when(response.getRawStatusCode()).thenReturn(HttpStatus.NOT_FOUND.value());
    Assertions.assertThat(handler.hasError(response)).isTrue();
  }

  @Test
  public void hasErrorFalse() throws Exception {
    when(response.getRawStatusCode()).thenReturn(HttpStatus.OK.value());
    assertThat(handler.hasError(response)).isFalse();
  }

  @Test
  public void testHandleErrorForBadRequestException() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

    when(response.getRawStatusCode()).thenReturn(HttpStatus.BAD_REQUEST.value());
    when(response.getStatusText()).thenReturn("Bad Request");
    when(response.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
    when(response.getHeaders()).thenReturn(headers);
    when(response.getBody())
        .thenReturn(new ByteArrayInputStream("{\"code\":\"400\"}".getBytes(StandardCharsets.UTF_8)));

    assertThatExceptionOfType(InvalidFieldException.class).isThrownBy(() -> handler.handleError(response));
  }

  @Test
  public void testHandleErrorForUnauthorizedException() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

    when(response.getRawStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED.value());
    when(response.getStatusText()).thenReturn("Unauthorized");
    when(response.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);
    when(response.getHeaders()).thenReturn(headers);
    when(response.getBody())
        .thenReturn(new ByteArrayInputStream("{\"code\":\"401\"}".getBytes(StandardCharsets.UTF_8)));

    assertThatExceptionOfType(UnauthorizedException.class).isThrownBy(() -> handler.handleError(response));
  }

  @Test
  public void testHandleErrorForNotFoundException() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

    when(response.getRawStatusCode()).thenReturn(HttpStatus.NOT_FOUND.value());
    when(response.getStatusText()).thenReturn("Not Found");
    when(response.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
    when(response.getHeaders()).thenReturn(headers);
    when(response.getBody())
        .thenReturn(new ByteArrayInputStream(NOT_FOUND_RESPONSE.getBytes(StandardCharsets.UTF_8)));

    assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> handler.handleError(response));
  }

  @Test
  public void testHandleErrorForInternalException() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

    when(response.getRawStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());
    when(response.getStatusText()).thenReturn("Internal Server Error");
    when(response.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
    when(response.getHeaders()).thenReturn(headers);
    when(response.getBody())
        .thenReturn(new ByteArrayInputStream("{\"code\":\"500\"}".getBytes(StandardCharsets.UTF_8)));

    assertThatExceptionOfType(InternalErrorException.class).isThrownBy(() -> handler.handleError(response));
  }

}
