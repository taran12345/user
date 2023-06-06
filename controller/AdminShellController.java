// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AdminShellService;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.resource.AdminShellResetPasswordRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetEmailStatusResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ResetPasswordRequestResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.validation.Valid;

@RestController
@RequestMapping({"/admin/user-provisioning/v1/adminshell/", "/user-provisioning/v1/adminshell/"})
public class AdminShellController {

  @Autowired
  private AdminShellService adminShellService;

  /**
   * API to reset password.
   * 
   */
  @PostMapping(value = "resetEmail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "API to trigger reset password email")
  public ResponseEntity<HttpStatus> triggerResetEmail(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @ApiParam(value = "request resource") @RequestBody AdminShellResetPasswordRequestResource requestResource,
      @RequestHeader(value = "Application", required = false) String application) {
    String applicationName = null;
    if (CommonThreadLocal.getAuthLocal() != null
        && StringUtils.isNotEmpty(CommonThreadLocal.getAuthLocal().getApplication())) {
      applicationName = CommonThreadLocal.getAuthLocal().getApplication();
    } else if (StringUtils.isNotEmpty(application)) {
      applicationName = application;
    }
    if (StringUtils.isEmpty(applicationName)) {
      throw BadRequestException.builder().details("application is not provided").build();
    }
    if (!applicationName.equals(DataConstants.PORTAL)
        && StringUtils.isNotEmpty(requestResource.getAuthContactEmailId())) {
      throw BadRequestException.builder().details("authcontact reset email is only valid for PORTAL application")
          .errorCode(CommonErrorCode.NOT_SUPPORTED).build();
    }
    adminShellService.triggerResetEmail(requestResource, applicationName);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Resets user password in pegasus and okta.
   * @throws JsonProcessingException e.
   * 
   */
  @PostMapping(value = "users/{userId}/resetPassword", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Resets user password")
  public ResponseEntity<HttpStatus> resetPassword(@RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @PathVariable(value = "userId") String userId,
      @Valid @RequestBody ResetPasswordRequestResource resetPasswordRequestResource) throws JsonProcessingException {
    String applicationName = null;
    if (CommonThreadLocal.getAuthLocal() != null
        && StringUtils.isNotEmpty(CommonThreadLocal.getAuthLocal().getApplication())) {
      applicationName = CommonThreadLocal.getAuthLocal().getApplication();
    } else if (StringUtils.isNotEmpty(application)) {
      applicationName = application;
    }
    if (StringUtils.isEmpty(applicationName)) {
      throw BadRequestException.builder().details("application is not provided").build();
    }
    adminShellService.resetPassword(userId, resetPasswordRequestResource, applicationName);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Get reset email status.
   */
  @GetMapping(value = "users/{userId}/resetEmailStatus", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Get status fo reset password email")
  public ResponseEntity<ResetEmailStatusResponseResource> getResetEmailStatus(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @PathVariable(value = "userId") String userId) {
    return new ResponseEntity<>(adminShellService.getResetEmailStatus(userId), HttpStatus.OK);
  }

}
