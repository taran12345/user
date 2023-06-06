// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.MasterMerchantService;
import com.paysafe.upf.user.provisioning.web.rest.resource.FetchEmailResponseResource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

import java.util.List;

@RestController
@RequestMapping({"/admin/user-provisioning/v1/", "/user-provisioning/v1/"})
public class MasterMerchantController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired
  MasterMerchantService masterMerchantService;

  /**
   * API to fetch emails from master merchant.
   */
  @GetMapping(value = "/emails/{userName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Fetch User")
  public ResponseEntity<List<FetchEmailResponseResource>> fetchEmail(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application, @PathVariable String userName) {
    logger.debug("Received request for fetching email for username: {}", userName);
    if (CommonThreadLocal.getAuthLocal() != null
        && StringUtils.isNotEmpty(CommonThreadLocal.getAuthLocal().getApplication())) {
      return new ResponseEntity<>(
          masterMerchantService.getEmail(userName, CommonThreadLocal.getAuthLocal().getApplication()), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(masterMerchantService.getEmail(userName, application), HttpStatus.OK);
    }
  }
}
