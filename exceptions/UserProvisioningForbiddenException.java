// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.exceptions;

import com.paysafe.op.errorhandling.exceptions.OneplatformException;
import com.paysafe.upf.user.provisioning.errors.UserProvisioningErrorCodes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "The server understood the request but refuses to authorize it")
public class UserProvisioningForbiddenException extends OneplatformException {
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception. The cause is not initialized, and may subsequently be initialized by a call to
   * {@link #initCause}.
   *
   * @see UserProvisioningErrorCodes#UNAUTHORIZED_ACCESS
   *
   */
  public UserProvisioningForbiddenException(String... details) {
    super(HttpStatus.FORBIDDEN, UserProvisioningErrorCodes.UNAUTHORIZED_ACCESS);
    this.setDetails(details);
  }
}
