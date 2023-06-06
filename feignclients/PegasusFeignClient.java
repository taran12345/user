// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.feignclients;

import com.paysafe.upf.user.provisioning.config.FeignClientConfig;
import com.paysafe.upf.user.provisioning.feignclients.fallbacks.PegasusFeignClientFallback;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUpdateUserRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserListResponseResource;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "paysafe-ss-pegasus-user-management", configuration = FeignClientConfig.class,
    fallback = PegasusFeignClientFallback.class)
public interface PegasusFeignClient {

  @RequestMapping(value = "/admin/pegasus/v2/users", method = RequestMethod.GET)
  PegasusUserListResponseResource getUsers(@RequestParam(value = "loginName") String loginName,
      @RequestParam(value = "pmleId") Long pmleId,
      @RequestParam(value = "page") Integer page,
      @RequestParam(value = "size") Integer pageSize);

  @PatchMapping(value = "/admin/pegasus/v2/users/")
  void updateUser(
      @RequestBody PegasusUpdateUserRequestResource pegasusUpdateUserRequestResource,
      @RequestParam(value = "loginName") String loginName);

}
