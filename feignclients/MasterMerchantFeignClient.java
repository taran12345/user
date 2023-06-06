// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients;

import com.paysafe.upf.user.provisioning.config.FeignClientConfig;
import com.paysafe.upf.user.provisioning.feignclients.fallbacks.MasterMerchantFeignClientFallback;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchAfterRequest;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "paysafe-master-merchant", configuration = FeignClientConfig.class,
    fallback = MasterMerchantFeignClientFallback.class)

public interface MasterMerchantFeignClient {

  @RequestMapping(method = RequestMethod.POST, value = "admin/mastermerchant/v1/merchantsearch/search",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  MerchantSearchResponse getMerchantsUsingSearchAfter(@RequestBody MerchantSearchAfterRequest request);
}
