// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.upf.user.provisioning.migration.service.SkrillTellerUserService;
import com.paysafe.upf.user.provisioning.web.rest.resource.inlinehooks.PasswordImportRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.inlinehooks.PasswordImportResponseResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

import javax.validation.Valid;

@RestController
@RequestMapping({"/admin/user-provisioning/v1/okta/", "/user-provisioning/v1/okta/"})
public class OktaInlineHooksController {


  @Autowired
  SkrillTellerUserService skrillTellerUserService;

  /**
   * API to migrate a user to Okta.
   */
  @PostMapping(value = "validation-hook", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Validate user credentials sent by OKTA")
  public ResponseEntity<PasswordImportResponseResource> validateUser(
      @Valid @RequestBody PasswordImportRequestResource passwordImportRequestResource, @RequestHeader(
          value = "Application", required = false) String application) {

    passwordImportRequestResource.setApplication(application);
    return new ResponseEntity<>(skrillTellerUserService.validateUserCredentialsOkta(passwordImportRequestResource),
            HttpStatus.OK);
  }

  /**
   * API to migrate a user to Okta.
   */
  @PostMapping(value = "netbanx-password-hook", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Netbanx password migration okta inline-hook")
  public ResponseEntity<PasswordImportResponseResource> netbanxPasswordMigration(
      @Valid @RequestBody PasswordImportRequestResource passwordImportRequestResource,
      @RequestHeader(value = "Application", required = false) String application) {
    passwordImportRequestResource.setApplication(application);
    return new ResponseEntity<>(skrillTellerUserService.netbanxPasswordMigration(passwordImportRequestResource),
        HttpStatus.OK);
  }
}
