// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.error;

import static org.junit.Assert.assertNotNull;

import com.paysafe.upf.user.provisioning.errors.Error;
import com.paysafe.upf.user.provisioning.errors.SkrillTellerErrorResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class TestSkrillTellerErrorResponse {

  /**
   * Test cases initialization.
   */
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }
  
  @InjectMocks
  private SkrillTellerErrorResponse skrillTellerErrorResponse;
  
  @Test
  public void getUserFriendlyErrorMessageWithNotFoundCustomerCode() {
    skrillTellerErrorResponse.setHttpStatus(400);
    skrillTellerErrorResponse.setCode("NOT_FOUND_CUSTOMER");
    skrillTellerErrorResponse.setMessage("TestMessage");
    String message = skrillTellerErrorResponse.getUserFriendlyErrorMessage();
    assertNotNull(message);
  }
  
  @Test
  public void getUserFriendlyErrorMessageWithBadRequestCode() {
    skrillTellerErrorResponse.setHttpStatus(400);
    skrillTellerErrorResponse.setCode("BAD_REQUEST");
    skrillTellerErrorResponse.setMessage("customerId");
    String message = skrillTellerErrorResponse.getUserFriendlyErrorMessage();
    assertNotNull(message);
  }
  
  @Test
  public void getUserFriendlyErrorMessageWithMessage() {
    skrillTellerErrorResponse.setHttpStatus(400);
    skrillTellerErrorResponse.setCode("BAD_REQUEST");
    skrillTellerErrorResponse.setMessage("test:Message");
    String message = skrillTellerErrorResponse.getUserFriendlyErrorMessage();
    assertNotNull(message);
  }
}
