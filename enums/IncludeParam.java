// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.enums;

import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;

public enum IncludeParam {

  PROFILE(DataConstants.PROFILE),
  MERCHANT_SETTINGS(DataConstants.MERCHANT_SETTINGS),
  BUSINESS_PROFILE(DataConstants.BUSINESS_PROFILE),
  EMAILS(DataConstants.EMAILS),
  EWALLET_ACCOUNTS(DataConstants.EWALLET_ACCOUNTS),
  DEFAULT_ACCOUNTS(DataConstants.DEFAULT_ACCOUNT),
  PROFILE_SETTINGS(DataConstants.PROFILE_SETTINGS),
  MONEY_FLOWS(DataConstants.MONEY_FLOWS);

  private final String value;

  IncludeParam(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
