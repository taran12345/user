// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.controller;

import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.UserStatusFilter;
import com.paysafe.upf.user.provisioning.service.MasterMerchantService;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccountGroupMerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.ParentMerchantLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.PartnerLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.validator.UserDetailsRequestValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping({"/admin/user-provisioning/", "/user-provisioning/"})
public class MerchantController {

  @Autowired
  private MasterMerchantService masterMerchantService;

  @Autowired
  private UserDetailsRequestValidator userDetailsRequestValidator;

  @GetMapping(value = {"v1/legalentity/hierarchy"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Api to get linked PMLEs for Partner or PMLE")
  public Set<ParentMerchantLegalEntity> getPmleList(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestParam(value = "ownerType") String ownerType,
      @RequestParam(value = "ownerId") String ownerId,
      @RequestParam(value = "isContainsSearch", required = false) boolean isContainsSearch) {
    userDetailsRequestValidator.validateV1HierarchyRequest(ownerId, ownerType);
    return masterMerchantService.getPmleList(ownerType, ownerId, isContainsSearch);
  }

  @GetMapping(value = {"v2/legalentity/hierarchy"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Api to get hierarchy from Partner level to Payment Account")
  public PartnerLegalEntity getPartnerHierarchy(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestParam(value = "ownerType") String ownerType,
      @RequestParam(value = "ownerId") String ownerId,
      @RequestParam(value = "isContainsSearch", required = false) boolean isContainsSearch) {
    return masterMerchantService.getPartnerHierarchy(ownerType, ownerId, isContainsSearch);
  }
  
  @GetMapping(value = {"v2/partner/legalentity/hierarchy"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Api to get the list of Partner hierarchy from Partner level to Payment Account")
  public Set<PartnerLegalEntity> getPartnerHierarchyList(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestParam(value = "ownerType") String ownerType, @RequestParam(value = "ownerId") String ownerId) {
    return masterMerchantService.getPartnerHierarchyList(ownerType, ownerId);
  }

  @GetMapping(value = {"v1/accountgroups"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Api to get FMA ids from account group ids")
  public AccountGroupMerchantSearchResponse getFmaIdFromAccountGroupId(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestParam(value = "accountGroupId", required = true) List<String> accountGroupIds,
      @RequestParam(value = "paymentAccountId", required = false) List<String> paymentAccountIds,
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "20") Integer pageSize) {
    return masterMerchantService.getMerchantForAccountGroups(accountGroupIds, paymentAccountIds, page, pageSize);
  }

  @GetMapping(value = {"v2/accountgroups"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Account group contains search API")
  public List<AccountGroupMerchantSearchResponse> getAccountGroupsByContainsSearch(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestParam(value = "accountGroupId", required = true) String accountGroupId) {
    return masterMerchantService.getAccountGroupsByContainsSearch(accountGroupId);
  }

  /**
   * API to fetch users list for a given account-type(ex: FMA).
   */
  @GetMapping(value = "v1/account-type/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @ApiOperation(value = "Filter and fetch Users")
  public ResponseEntity<UsersListResponseResource> getAccountTypeUsers(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @RequestHeader(value = "Application", required = false) String application,
      @RequestParam(value = "status", required = false) UserStatusFilter status,
      @RequestParam(value = "resourceName") ResourceType resourceName,
      @RequestParam(value = "resourceId") String resourceId,
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "20") Integer pageSize,
      @RequestParam(value = "merchantTypeValidation", required = false) boolean merchantTypeValidation) {
    return new ResponseEntity<>(masterMerchantService.getAccountTypeUsers(resourceName, resourceId, status,
        merchantTypeValidation, page, pageSize), HttpStatus.OK);
  }
}
