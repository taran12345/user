// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.upf.user.provisioning.service.FeatureFlagService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/user-provisioning/v1/featureFlag")
public class FeatureFlagController {

  @Autowired
  FeatureFlagService featureFlagService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<HashMap<String, Boolean>> fetchFeatureFlag(
      @RequestHeader(value = "Authorization") String auth) {
    return new ResponseEntity<>(featureFlagService.fetchFeatureFlag(), HttpStatus.OK);
  }
}
