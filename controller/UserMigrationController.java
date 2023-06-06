// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.upf.user.provisioning.migration.service.MigrationScriptService;
import com.paysafe.upf.user.provisioning.migration.service.SkrillTellerUserService;
import com.paysafe.upf.user.provisioning.utils.UserProvisioningUtils;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.dto.SkrillTellerMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.SkrillTellerUserResponseDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.BulkUserMigrationResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.SkrillTellerUserResponseResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping({"/admin/user-provisioning/v1/migration/", "/user-provisioning/v1/migration/"})
public class UserMigrationController {

  private static final Logger logger = LoggerFactory.getLogger(UserMigrationController.class);

  @Autowired
  private MigrationScriptService migrationScriptService;

  @Autowired
  private UserAssembler userAssembler;

  @Autowired
  private SkrillTellerUserService skrillTellerUserService;

  @Autowired
  private UserProvisioningUtils userProvisioningUtils;

  /**
   * API to upload user migration data and run the script.
   */
  @PostMapping(value = "users/upload", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Upload migrate user data")
  public ResponseEntity<BulkUserMigrationResponseResource> uploadUserMigrationData(
      @ApiParam(value = "File to be uploaded", required = true) @RequestPart(value = "file") MultipartFile file,
      @RequestParam(value = "writeLogToFile", required = false) boolean writeLogToFile,
      @RequestHeader(value = "Application", required = false) String application) {
    return new ResponseEntity<>(migrationScriptService.processFile(file, writeLogToFile, false),
        HttpStatus.OK);
  }

  /**
   * API to migrate a user to Okta.
   *
   * @throws JsonProcessingException ex.
   */
  @PostMapping(value = "users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Migrate user")
  public ResponseEntity<UserMigrationResponseResource> migrateUser(
      @Valid @RequestBody UserMigrationResource userMigrationResource,
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application) throws JsonProcessingException {
    userProvisioningUtils.checkAndAddDefaultPermission(userMigrationResource);
    if (userMigrationResource.getOrigin() == null || userMigrationResource.getOrigin().getSite() == null) {
      UserMigrationDto userMigrationDto = userAssembler.toUserMigrationDto(userMigrationResource);
      UserProvisioningUserResource userProvisioningUserResource;
      if (userMigrationDto.isUSigaming()) {
        userProvisioningUserResource =
            migrationScriptService.migrateUSiUser(userMigrationDto, false);
      } else {
        userProvisioningUserResource =
            migrationScriptService.migrateSingleUser(userMigrationDto, false);
      }
      return new ResponseEntity<>(userAssembler.toUserMigrationResponseResource(userProvisioningUserResource),
          HttpStatus.CREATED);
    } else {
      userProvisioningUtils.checkAndAddDefaultPermission(userMigrationResource);
      SkrillTellerMigrationDto skrillTellerMigrationDto =
          userAssembler.toSkrillTellerMigrationDto(userMigrationResource);
      return new ResponseEntity<>(skrillTellerUserService.migrateSkrillTellerUser(skrillTellerMigrationDto),
          HttpStatus.CREATED);
    }
  }

  /**
   * API to fetch user.
   *
   * @return skrillTellerUserResponseResource
   */
  @GetMapping(value = "/users/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Fetch User")
  public ResponseEntity<SkrillTellerUserResponseResource> getUser(@PathVariable(value = "userId") String userId) {
    SkrillTellerUserResponseDto skrillTellerUserResponseDto = skrillTellerUserService.getUser(userId);
    return new ResponseEntity<>(userAssembler.toSkrillTellerResponseResource(skrillTellerUserResponseDto),
        HttpStatus.OK);
  }

  /**
   * API to search user.
   *
   * @return skrillTellerUserResponseResource
   */
  @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Search User")
  public ResponseEntity<List<SkrillTellerUserResponseResource>> searchUser(@RequestParam(value = "site") String site,
      @RequestParam(value = "username") String userName) {
    List<SkrillTellerUserResponseDto> skrillTellerUserResponseDtos = skrillTellerUserService.searchUser(userName, site);
    return new ResponseEntity<>(userAssembler.toSkrillTellerResponseResourceList(skrillTellerUserResponseDtos),
        HttpStatus.OK);
  }

  /**
   * API to migrate a SKRILL or NETELLER user to Okta.
   *
   * @throws JsonProcessingException ex.
   */
  @PutMapping(value = "users/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "update skrill user")
  public ResponseEntity<SkrillTellerUserResponseResource> updateSkrillUser(
      @PathVariable(value = "userId") String userId,
      @RequestBody UserMigrationResource userMigrationResource) throws JsonProcessingException {
    SkrillTellerMigrationDto skrillTellerMigrationDto = userAssembler.toSkrillTellerMigrationDto(userMigrationResource);
    return new ResponseEntity<>(skrillTellerUserService.updateUser(userId, skrillTellerMigrationDto), HttpStatus.OK);
  }

  /**
   * API to delete user.
   *
   * @return skrillTellerUserResponseResource
   */
  @DeleteMapping(value = "/users/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Delete User")
  public ResponseEntity<SkrillTellerUserResponseResource> deleteUser(@PathVariable(value = "userId") String userId) {
    SkrillTellerUserResponseDto skrillTellerUserResponseDto = skrillTellerUserService.deleteUser(userId);
    return new ResponseEntity<>(userAssembler.toSkrillTellerResponseResource(skrillTellerUserResponseDto),
        HttpStatus.OK);
  }

}
