// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients;

import com.paysafe.upf.user.provisioning.config.FeignClientConfig;
import com.paysafe.upf.user.provisioning.feignclients.fallbacks.SmartRoutingFeignClientFallback;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.AccountGroupsV2Resource;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "paysafe-psp-smartrouting", configuration = FeignClientConfig.class,
    fallback = SmartRoutingFeignClientFallback.class)
public interface SmartRoutingFeignClient {

  @RequestMapping(method = RequestMethod.GET, value = "/v2/accountgroups", consumes = "application/json")
  public AccountGroupsV2Resource getAccountGroupsV2(@RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "accountId", required = false) String accountId,
      @RequestParam(value = "originCode", required = false) String originCode,
      @RequestParam(value = "originReferenceId", required = false) String originReferenceId,
      @RequestParam(value = "type", required = false) String type,
      @RequestParam(value = "offset", required = false) Integer offset,
      @RequestParam(value = "limit", required = false) Integer limit);

}
