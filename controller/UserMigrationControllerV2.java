// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.upf.user.provisioning.migration.service.MigrationScriptService;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.BulkUserMigrationResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.validation.Valid;

@RestController
@RequestMapping({"/admin/user-provisioning/v2/migration/", "/user-provisioning/v2/migration/"})
public class UserMigrationControllerV2 {

  @Autowired
  private MigrationScriptService migrationScriptService;

  @Autowired
  private UserAssembler userAssembler;

  @Autowired
  private UserProvisioningUtils userProvisioningUtils;

  /**
   * API to single migrate a user to Okta and updates the user if user already exists in okta.
   *
   * @throws JsonProcessingException ex.
   */
  @PostMapping(value = "users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Migrate user")
  public ResponseEntity<UserMigrationResponseResource> migrateUserV2(
      @Valid @RequestBody UserMigrationResource userMigrationResource,
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application) throws JsonProcessingException {
    userProvisioningUtils.checkAndAddDefaultPermission(userMigrationResource);
    UserMigrationDto userMigrationDto = userAssembler.toUserMigrationDto(userMigrationResource);
    UserProvisioningUserResource userProvisioningUserResource;
    if (userMigrationDto.isUSigaming()) {
      userProvisioningUserResource = migrationScriptService.migrateUSiUser(userMigrationDto, true);
    } else {
      userProvisioningUserResource = migrationScriptService.migrateSingleUser(userMigrationDto, true);
    }
    return new ResponseEntity<>(userAssembler.toUserMigrationResponseResource(userProvisioningUserResource),
        HttpStatus.CREATED);
  }

  /**
   * V2 API to upload user migration data and run the script.
   */
  @PostMapping(value = "users/upload", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Upload migrate user data")
  public ResponseEntity<BulkUserMigrationResponseResource> uploadUserMigrationDataV2(
      @ApiParam(value = "File to be uploaded", required = true) @RequestPart(value = "file") MultipartFile file,
      @RequestParam(value = "writeLogToFile", required = false) boolean writeLogToFile,
      @RequestHeader(value = "Application", required = false) String application) {
    return new ResponseEntity<>(migrationScriptService.processFile(file, writeLogToFile, true),
        HttpStatus.OK);
  }

}
