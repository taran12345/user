// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.upf.user.provisioning.enums.RegionType;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.SkrillTellerUserService;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserCountDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.UserDetailsResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.UserRegionUpdateResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping({"/admin/user-provisioning/v1/", "/user-provisioning/v1/"})
public class SkrillTellerUserController {

  @Autowired
  private SkrillTellerUserService skrillTellerUserService;

  /**
   * Gets the all admin-users for the walletId.
   */
  @RequestMapping(method = RequestMethod.GET, value = {"users/wallets/{walletId}/adminUsers"},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "gets the all admin-users for the walletId")
  public ResponseEntity<List<UserResponseResource>> getWalletAdminUsers(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application, @PathVariable String walletId) {
    return new ResponseEntity<>(skrillTellerUserService.getWalletAdminUsers(walletId), HttpStatus.OK);
  }

  /**
   * This API gives the count of admin users and total users whom the given walletIds have been assigned.
   */
  @RequestMapping(method = RequestMethod.POST, value = {"users/wallets/count"},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Admin and total users count by wallet Ids")
  public ResponseEntity<Map<String, UserCountDto>> getUserCountByWalletIds(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestBody List<String> walletIds) {
    return new ResponseEntity<>(skrillTellerUserService.getUsersCountByWalletIdsUsingLinkedBrands(walletIds),
        HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET, value = {"/brands"},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "gets the business units/ brands")
  public ResponseEntity<List<String>> getBrands(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application) {
    return new ResponseEntity<>(skrillTellerUserService.getBrands(application), HttpStatus.OK);
  }

  /**
   * Api to download the user emails with region in csv file.
   */
  @GetMapping(value = "/users/email/download", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Api to download the user emails with region in csv file")
  public ResponseEntity<UserDetailsResponseResource> downloadUserEmails(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application") String application,
      @RequestParam(value = "regionType") RegionType regionType,
      @RequestParam(value = "pageNo", required = false, defaultValue = "0") Integer pageNo,
      @RequestParam(value = "size", required = false, defaultValue = "50") Integer size,
      HttpServletResponse response) {
    return new ResponseEntity<>(skrillTellerUserService.getUserEmails(application, regionType, pageNo, size),
        HttpStatus.OK);
  }

  /**
   * This API is used to update the user's region.
   */
  @PatchMapping(value = "/users/region", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Updates the region of the users")
  public ResponseEntity<UserRegionUpdateResponse> updateUsersRegion(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @RequestHeader(value = "BusinessUnit", required = false) String businessUnit,
      @RequestParam(value = "loginName", required = false) String loginName,
      @RequestParam(value = "size", required = false, defaultValue = "50") Integer size) {
    if (StringUtils.isNotEmpty(auth) && StringUtils.isEmpty(application)) {
      application = CommonThreadLocal.getAuthLocal().getApplication();
    }

    return new ResponseEntity<>(skrillTellerUserService.updateUsersRegion(application, loginName, size), HttpStatus.OK);
  }
}
