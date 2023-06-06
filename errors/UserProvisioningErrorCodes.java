// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.errors;

import com.paysafe.op.errorhandling.ErrorCode;

public enum UserProvisioningErrorCodes implements ErrorCode {

  KEY_NOT_PRESENT("11000", "Key not present", "Key not present."),

  INVALID_CREDENTIALS("11001", "Credentials are invalid", "User Credentials are invalid."),

  INACTIVE_ACCOUNT("11002", "Disabled account", "User account is disabled."),

  USER_NOT_FOUND("11003", "User not found", "User not found."),

  GBP_LINK_EXPIRED("11004", "Link expired", "Link has expired."),

  AUTHENTICATION_FAILED("11005", "Authentication Failed", "Incorrect username or password"),

  UPR_ERROR("11006", "Something went wrong!", "Something went wrong!"),

  ALREADY_MIGRATED_ERROR("11007", "User already migrated", "User already migrated"),

  MIGRATION_ERROR("11008", "Error during user migration", "Error while migrating the user"),

  UNAUTHORIZED_ACCESS("11009", "Unauthorized access",
      "The credentials do not have permission to access the requested data.");

  private final String code;
  private final String message;
  private final String description;

  UserProvisioningErrorCodes(String code, String message, String description) {
    this.code = code;
    this.message = message;
    this.description = description;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String getDescription() {
    return description;
  }
}
