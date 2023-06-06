// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients.fallbacks;

import com.paysafe.op.errorhandling.exceptions.ExternalGatewayErrorException;
import com.paysafe.upf.user.provisioning.feignclients.SmartRoutingFeignClient;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.AccountGroupsV2Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmartRoutingFeignClientFallback implements SmartRoutingFeignClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(SmartRoutingFeignClientFallback.class);
  private static final String LOG_MESSAGE =
      "paysafe-psp-smartrouting  service circuit is open, falling back to failover";

  @Override
  public AccountGroupsV2Resource getAccountGroupsV2(String fields, String accountId, String originCode,
      String originReferenceId, String type, Integer offset, Integer limit) {
    LOGGER.warn(LOG_MESSAGE);
    throw ExternalGatewayErrorException.builder().build();
  }
}
