// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.error;

import static org.junit.Assert.assertNotNull;

import com.paysafe.upf.user.provisioning.errors.Error;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class TestError {

  /**
   * Test cases initialization.
   */
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @InjectMocks
  private Error error;

  @Test
  public void testErrorGetterSetter() {
    error.setCode("testCode");
    assertNotNull(error.getCode());
    error.setMessage("Message");
    assertNotNull(error.getMessage());
  }
}
