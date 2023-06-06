// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.exceptions;

import com.paysafe.op.errorhandling.ErrorCode;
import com.paysafe.op.errorhandling.exceptions.OneplatformException;

import com.netflix.hystrix.exception.ExceptionNotWrappedByHystrix;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.http.HttpStatus;

public class UserProvisioningException extends OneplatformException implements ExceptionNotWrappedByHystrix {

  private static final long serialVersionUID = 1L;

  UserProvisioningException(HttpStatus httpStatus, ErrorCode errorCode, Throwable cause) {
    super(httpStatus, errorCode, cause);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends AbstractBuilder<UserProvisioningException, Builder> {

    /**
     * Build new Oneplatform exception.
     */
    public UserProvisioningException create(HttpStatus httpStatus) {
      UserProvisioningException genericException =
          new UserProvisioningException(httpStatus, this.errorCode, this.cause);
      if (ArrayUtils.isNotEmpty(this.getDetails())) {
        genericException.setDetails(this.getDetails());
      }
      return genericException;
    }

    @Override
    public UserProvisioningException build() {
      return null;
    }
  }
}
