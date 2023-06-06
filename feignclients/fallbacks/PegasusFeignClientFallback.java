// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients.fallbacks;

import com.paysafe.op.errorhandling.exceptions.ExternalGatewayErrorException;
import com.paysafe.upf.user.provisioning.feignclients.PegasusFeignClient;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUpdateUserRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserListResponseResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PegasusFeignClientFallback implements PegasusFeignClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(PegasusFeignClientFallback.class);
  private static final String LOG_MESSAGE =
      "paysafe-ss-pegasus-user-management service circuit is open, falling back to failover";

  public PegasusFeignClientFallback() {
    // intentionally empty
  }

  @Override
  public PegasusUserListResponseResource getUsers(String loginName, Long pmleId, Integer page, Integer pageSize) {
    LOGGER.warn(LOG_MESSAGE);
    throw ExternalGatewayErrorException.builder().build();
  }

  @Override
  public void updateUser(
      PegasusUpdateUserRequestResource pegasusUpdateUserRequestResource, String loginName) {
    LOGGER.warn(LOG_MESSAGE);
    throw ExternalGatewayErrorException.builder().build();
  }

}
