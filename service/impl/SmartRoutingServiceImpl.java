// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.feignclients.SmartRoutingFeignClient;
import com.paysafe.upf.user.provisioning.service.SmartRoutingService;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.AccountGroupsV2Resource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.UserPaymentMethodsDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SmartRoutingServiceImpl implements SmartRoutingService {

  private static final String PAYMENT_METHODS = "paymentMethods";
  private static final String PAYSAFE_SINGLE_API = "PAYSAFE_SINGLE_API";
  private static final String ORIGIN_CODE = "NETBANX";
  private static final int LIMIT = 10;

  @Autowired
  private SmartRoutingFeignClient smartRoutingFeignClient;

  /**
   * Get SINGLE_API accountIds from pmleId.
   */
  @Override
  public List<String> getSingleApiAccountIdsByPmleId(String pmleId) {
    if (pmleId == null) {
      throw BadRequestException.builder().details("pmleId should not be null").errorCode(CommonErrorCode.INVALID_FIELD)
          .build();
    }

    List<String> singleApiAccountIds = new ArrayList<>();
    int offset = 0;
    long numberOfRecords = LIMIT;
    AccountGroupsV2Resource accountGroupsV2Resource;
    while (offset < numberOfRecords) {
      accountGroupsV2Resource = smartRoutingFeignClient.getAccountGroupsV2(PAYMENT_METHODS, null, ORIGIN_CODE, pmleId,
          DataConstants.PMLE, offset, LIMIT);
      List<String> accountIds = accountGroupsV2Resource.getAccountGroups().stream()
          .flatMap(accountGroup -> accountGroup.getPaymentMethods().stream())
          .filter(paymentMethod -> paymentMethod.getQualifier().equalsIgnoreCase(PAYSAFE_SINGLE_API))
          .map(UserPaymentMethodsDto::getAccountId).collect(Collectors.toList());
      singleApiAccountIds.addAll(accountIds);
      numberOfRecords = accountGroupsV2Resource.getMeta().getNumberOfRecords();
      offset += LIMIT;
    }

    return singleApiAccountIds;
  }
}
