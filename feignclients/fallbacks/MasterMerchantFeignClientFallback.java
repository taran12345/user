// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients.fallbacks;

import com.paysafe.op.errorhandling.exceptions.ExternalGatewayErrorException;
import com.paysafe.upf.user.provisioning.feignclients.MasterMerchantFeignClient;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchAfterRequest;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MasterMerchantFeignClientFallback implements MasterMerchantFeignClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(MasterMerchantFeignClientFallback.class);
  private static final String LOG_MESSAGE =
      "paysafe-master-merchant  service circuit is open, falling back to failover";

  @Override
  public MerchantSearchResponse getMerchantsUsingSearchAfter(MerchantSearchAfterRequest request) {
    LOGGER.warn(LOG_MESSAGE);
    throw ExternalGatewayErrorException.builder().build();
  }

}
