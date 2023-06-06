// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.enums;

public enum SkrillPermissions {
  TRANSACTIONS_VIEW(100),
  TRANSACTIONS_EDIT(101),
  BALANCES(102),
  CHANGE_PRIMARY_CURRENCY(103),
  BANKING(104),
  MANAGE_BANK_ACCOUNTS(105),
  SEND_MONEY_CUSTOMER(106),
  SEND_MONEY_INTER(107),
  SEND_MONEY_INTRA(108),
  EXCHANGE_MONEY(109),
  MASS_PAYMENTS(110),
  SETTINGS(111),
  USER_MANAGEMENT(112),
  DEVELOPER_SETTINGS(121),
  SCHEDULE_BALANCES(122),
  MERCHANT_REFUNDS(115),
  ACCOUNT_SETTINGS(116),
  EMAIL_PREFERENCES(117),
  CHANGE_LANGUAGE(118),
  LIMITS_AND_VERIFICATION(119),
  BUSINESS_INFO(120),
  DEFAULT_PERMISSION(300);

  private final Integer id;

  SkrillPermissions(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }
}
