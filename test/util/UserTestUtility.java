// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.util;

import com.paysafe.upf.user.provisioning.domain.BulkUsers;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.domain.UserAssignedApplications;
import com.paysafe.upf.user.provisioning.domain.UsersSummary;
import com.paysafe.upf.user.provisioning.domain.WalletPermission;
import com.paysafe.upf.user.provisioning.domain.rolemodules.BusinessInitiativeInfo;
import com.paysafe.upf.user.provisioning.domain.rolemodules.BusinessInitiativeRoles;
import com.paysafe.upf.user.provisioning.domain.rolemodules.Module;
import com.paysafe.upf.user.provisioning.domain.rolemodules.ModuleAccessLevel;
import com.paysafe.upf.user.provisioning.domain.rolemodules.ModulePermissions;
import com.paysafe.upf.user.provisioning.domain.rolemodules.RoleModules;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.Action;
import com.paysafe.upf.user.provisioning.enums.SkrillPermissions;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.enums.UserAction;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.model.OwnerInfo;
import com.paysafe.upf.user.provisioning.security.commons.AuthorizationInfo;
import com.paysafe.upf.user.provisioning.security.commons.JwtPayloadUtil;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.BulkWalletDetailResponse;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomAccessGroupDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.CustomRoleDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.PageResponseDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.PermissionDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.ResourceUsersValidationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.RoleDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.SkrillTellerMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserPasswordMigrationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupNameAvailabilityResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupNameAvailabilityResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupPolicy;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicy;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessPolicyRight;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessRight;
import com.paysafe.upf.user.provisioning.web.rest.resource.ContactEmail;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserListResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchAfterRequest;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.PaymentAccountResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.PaymentAccountResponse.ContactsDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegasusUserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.PegausUserRoleResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount.BusinessDetails;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount.LegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount.OnboardingInformation;
import com.paysafe.upf.user.provisioning.web.rest.resource.SkrillContactEmailsResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.SourceAuthority;
import com.paysafe.upf.user.provisioning.web.rest.resource.TokenResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserAccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserMigrationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserPasswordMigrationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserProvisioningUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserUpdationResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.WalletUserCountResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.inlinehooks.PasswordHookContext;
import com.paysafe.upf.user.provisioning.web.rest.resource.inlinehooks.PasswordHookCredential;
import com.paysafe.upf.user.provisioning.web.rest.resource.inlinehooks.PasswordHookData;
import com.paysafe.upf.user.provisioning.web.rest.resource.inlinehooks.PasswordImportRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BusinessProfile;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.EwalletAccount;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.MerchantSettings;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.Profile;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.AccountGroup;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.AccountGroupsV2Resource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.Meta;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.ModuleResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.ModulesResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModuleListResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModulesList;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.RoleModulesResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.rolemodules.UserPaymentMethodsDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.CustomerObject;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.GroupedCustomerIdsResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.LinkedCustomerIdsResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.SkrillAccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.usersummary.Merchant;
import com.paysafe.upf.user.provisioning.web.rest.resource.usersummary.MerchantInfo;
import com.paysafe.upf.user.provisioning.web.rest.resource.usersummary.UserSummary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UserTestUtility {

  public static final String EMAIL = "test@xyz.com";
  public static final String USER_NAME = "TEST_USER";
  public static final String FULL_NAME = "TEST USER";
  private static final Long USER_PMLE_ID = 259140L;
  public static final String PASSWORD = "password";
  public static final String FIRSTNAME = "first";
  public static final String LASTNAME = "last";
  public static final String UUID = "uuid22289223";
  public static final String STATUS = "ACTIVE";
  public static final String MOBILE_PHONE = "2523242423";
  public static final List<String> ROLES = new ArrayList<>(Arrays.asList("ADMIN", "DEVELOPER"));
  public static final List<String> ACCESS_GROUPS = new ArrayList<>(Arrays.asList("AG01", "AG02"));
  public static final String ACCESS_LEVEL_TYPE_CODE = "USER";
  public static final String ACCESS_VALUE = "USER_ACESS";
  public static final Long PMLE_ID = 78910L;
  public static final String WALLET_ID = "12345";

  public static final String AUTH_TOKEN_SKRILL =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJrdXNlciIsIm93bmVyX2lkIjoiMTI"
          + "zIiwib3duZXJfdHlwZSI6IlBNTEUiLCJhcHBsaWNhdGlvbiI6IlNLUklMTCIsImlzcyI6Im9rdGEiLCJzZ"
          + "XNzaW9uSWQiOiIzYTg3ZWYyNi05NDE5LTQxMjctYmNhOC0yZDM5OTE0ZTk4Y2MiLCJwYXlzYWZlX3Njb3B"
          + "lX2dyb3VwX2lkIjoiMjU5MTQwIiwiYXV0aG9yaXRpZXMiOlsiUkJWSUVXIiwiRkNOVVNFUiIsIkJQX0FET"
          + "UlOIiwiQlBfVVNFUiJdLCJjbGllbnRfaWQiOiI4MTIxMzVkOGJiYzViNDFiYzgzN2YzOTgyYjQ3NjBmMjY"
          + "0Njg1YTRjIiwiYWNjZXNzX2xldmVsIjoiVVNFUiIsInBhcnRuZXJfaWQiOm51bGwsInVzZXJfdHlwZSI6I"
          + "kVYVCIsInNjb3BlIjpbImJ1c2luZXNzUG9ydGFsIl0sInByb2R1Y3RfaWQiOm51bGwsInBheXNhZmVfc2N"
          + "vcGVfZ3JvdXBfdHlwZSI6IlBNTEUiLCJleHAiOjE1NDk5MDA5MzMsImlhdCI6MTU0OTg5ODIzMywiYnJhb"
          + "mQiOiJQYXlzYWZlIiwianRpIjoiZjQ4OGU0ZWEtOTRhZS00MGU3LWFkZjItZDY3NDMzMTMzZDI1IiwicG1"
          + "sZV9pZCI6IjI1OTE0MCIsImF1dGhlbnRpY2F0aW9uR3JvdXAiOm51bGx9.G0I3LbiaP4yZYGzoZD7_vgVJ"
          + "Rqwo0Gp4CIZHKQ0rvSA";

  public static final String AUTH_TOKEN_NETELLER = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOi"
      + "JrdXNlciIsImFwcGxpY2F0aW9uIjoiTkVURUxMRVIiLCJpc3MiOiJva3RhIiwic2Vzc2lvbklkIjoiM2E4N2VmMjYtOTQxOS00MT"
      + "I3LWJjYTgtMmQzOTkxNGU5OGNjIiwicGF5c2FmZV9zY29wZV9ncm91cF9pZCI6IjI1OTE0MCIsImF1dGhvcml0aWVzIjpbIlJCVk"
      + "lFVyIsIkZDTlVTRVIiLCJCUF9BRE1JTiIsIkJQX1VTRVIiXSwiY2xpZW50X2lkIjoiODEyMTM1ZDhiYmM1YjQxYmM4MzdmMzk4"
      + "MmI0NzYwZjI2NDY4NWE0YyIsImFjY2Vzc19sZXZlbCI6IlVTRVIiLCJwYXJ0bmVyX2lkIjpudWxsLCJ1c2VyX3R5cGUiOiJFWFQiL"
      + "CJzY29wZSI6WyJidXNpbmVzc1BvcnRhbCJdLCJwcm9kdWN0X2lkIjpudWxsLCJwYXlzYWZlX3Njb3BlX2dyb3VwX3R5cGUiOiJQTUx"
      + "FIiwiZXhwIjoxNTQ5OTAwOTMzLCJpYXQiOjE1NDk4OTgyMzMsImJyYW5kIjoiUGF5c2FmZSIsImp0aSI6ImY0ODhlNGVhLTk0YWUtN"
      + "DBlNy1hZGYyLWQ2NzQzMzEzM2QyNSIsInBtbGVfaWQiOiIyNTkxNDAiLCJhdXRoZW50aWNhdGlvbkdyb3VwIjpudWxsfQ.hhBLipNM"
      + "gGYg8dmIc3K9NffATBDXdeO3h7h9wuD0WEI";

  public static final String AUTH_TOKEN_PORTAL = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2ZXIi"
      + "OjEsImp0aSI6IkFULk5oaUFtSGlTdkJYOXN1N2UtY0dXRDFkeXVrekxIMWE5TXBRd0hLRnVGbk0iLCJpc3Mi"
      + "OiJodHRwczovL2Rldi03NDgwMTAub2t0YXByZXZpZXcuY29tL29hdXRoMi9hdXNwMHY2dDkzOXg0NzdtbTBo"
      + "NyIsImF1ZCI6Imh0dHBzOi8vYXBpLnBheXNhZmUuY29tIiwiaWF0IjoxNTg2NDExMDYxLCJleHAiOjI1ODY0"
      + "MTI4NjEsImNpZCI6IjBvYXBoZGM2N25OMU9RSmdFMGg3IiwidWlkIjoiMDB1b2ZyNDlmbXByakNEcU4waDci"
      + "LCJzY3AiOlsib3BlbmlkIl0sInN1YiI6ImRpbmVzaC5rYW5kYWthdGxhQHBheXNhZmUuY29tIiwiYXBwbGlj"
      + "YXRpb24iOiJQT1JUQUwiLCJ1c2VyX25hbWUiOiJkaW5lc2gua2FuZGFrYXRsYUBwYXlzYWZlLmNvbSIsIm93"
      + "bmVyX2lkIjoiNzc3Iiwib3duZXJfdHlwZSI6IlBNTEUiLCJhcHBOYW1lIjoiQnVzaW5lc3NQb3J0YWwiLCJ0"
      + "ZXN0Y2xhaW0iOiJ0ZXN0Y2xhaW0iLCJhY2Nlc3NfZ3JvdXBzIjpbIjFlMmVkNjhjLTA0YjYtNGUzMy1hNTA1"
      + "LWMzYTE4Y2U1OGVhYSIsIjc5MjRhYjQ1LTE4MTQtNDQ5Ny05ZGY0LTIwYTVjNTM4MTQxNyJdLCJwYXlzYWZl"
      + "X3Njb3BlX2dyb3VwX3R5cGUiOiJQTUxFIiwiYnJhbmQiOiJQYXlzYWZlIiwiYXV0aG9yaXRpZXMiOlsiQlBf"
      + "QURNSU4iLCJCUF9VU0VSIiwiQlBfRVVfQURNSU4iXX0.CGd9_tKp8Tt9Ukg9GdDQA_r3imMb46U8hA5lhhUj" + "D4o";

  public static final String AUTH_TOKEN_PARTNER_PORTAL = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2ZXIiOjEsImp0aSI6"
      + "IkFULk5oaUFtSGlTdkJYOXN1N2UtY0dXRDFkeXVrekxIMWE5TXBRd0hLRnVGbk0iLCJpc3MiOiJodHRwczovL2Rldi03NDgwMTAub2t0"
      + "YXByZXZpZXcuY29tL29hdXRoMi9hdXNwMHY2dDkzOXg0NzdtbTBoNyIsImF1ZCI6Imh0dHBzOi8vYXBpLnBheXNhZmUuY29tIiwiaWF0"
      + "IjoxNTg2NDExMDYxLCJleHAiOjk1ODY0MTI4NjEsImNpZCI6IjBvYXBoZGM2N25OMU9RSmdFMGg3IiwidWlkIjoiMDB1b2ZyNDlmbXBy"
      + "akNEcU4waDciLCJzY3AiOlsib3BlbmlkIl0sInN1YiI6ImRpbmVzaC5rYW5kYWthdGxhQHBheXNhZmUuY29tIiwiYXBwbGljYXRpb24i"
      + "OiJQQVJUTkVSX1BPUlRBTCIsInVzZXJfbmFtZSI6ImRpbmVzaC5rYW5kYWthdGxhQHBheXNhZmUuY29tIiwib3duZXJfaWQiOiI3Nzci"
      + "LCJvd25lcl90eXBlIjoiUE1MRSIsImFwcE5hbWUiOiJCdXNpbmVzc1BvcnRhbCIsInRlc3RjbGFpbSI6InRlc3RjbGFpbSIsImFjY2Vz"
      + "c19ncm91cHMiOlsiMWUyZWQ2OGMtMDRiNi00ZTMzLWE1MDUtYzNhMThjZTU4ZWFhIiwiNzkyNGFiNDUtMTgxNC00NDk3LTlkZjQtMjBh"
      + "NWM1MzgxNDE3Il0sInBheXNhZmVfc2NvcGVfZ3JvdXBfdHlwZSI6IlBNTEUiLCJicmFuZCI6IlBheXNhZmUiLCJhdXRob3JpdGllcyI6"
      + "WyJCUF9BRE1JTiIsIkJQX1VTRVIiLCJCUF9FVV9BRE1JTiJdfQ.qDXISgLSMJwehsVusNLfWVGbeNH62wXNJMIH6MyeW_Y";

  public static final String AUTH_TOKEN_PORTAL_MFA = "eyJraWQiOiJqVWhQdjVxcFNrRVBGVGFzeUdmQlltMVh4TFlmSDhlcnQ1WGJh"
      + "anlNM2FvIiwiYWxnIjoiUlMyNTYifQ.eyJ2ZXIiOjEsImp0aSI6IkFULk5RWHM2d2FXWWFPbnBqdDBpNzRCajBwYWRBVnl1aFhkVlA1OV"
      + "BpTmdvbTgiLCJpc3MiOiJodHRwczovL2Rldi03NDgwMTAub2t0YXByZXZpZXcuY29tL29hdXRoMi9hdXNwMHY2dDkzOXg0NzdtbTBoNyI"
      + "sImF1ZCI6Imh0dHBzOi8vYXBpLnBheXNhZmUuY29tIiwiaWF0IjoxNjgxNDc4OTg4LCJleHAiOjE2ODE0ODA3ODgsImNpZCI6IjBvYXBp"
      + "cmN0cDRGRjE0Mk9hMGg3IiwidWlkIjoiMDB1MTE5bDE5cDJYZDExY04waDgiLCJzY3AiOlsiZW1haWwiLCJvcGVuaWQiXSwiYXV0aF90a"
      + "W1lIjoxNjgxNDc4OTgwLCJzdWIiOiJ0ZXN0cGF5bWVudGh1YiIsImJ1c2luZXNzVW5pdCI6IlVTX0lfR0FNSU5HIiwidXNlcl9uYW1lIj"
      + "oidGVzdHBheW1lbnRodWIiLCJhcHBOYW1lIjoiQnVzaW5lc3NQb3J0YWwiLCJvd25lcl9pZCI6IjY5MzI5MDAiLCJwYXlzYWZlX3Njb3B"
      + "lX2dyb3VwX2lkIjoiNjkzMjkwMCIsImF1dGhvcml0aWVzIjpbIkJQX1VTRVIiLCJCUF9BRE1JTiIsIkJQX0VVX0FETUlOIl0sImFwcGxp"
      + "Y2F0aW9uIjoiUE9SVEFMIiwidGVzdGNsYWltIjoidGVzdGNsYWltIiwicGF5c2FmZV9pZCI6ImU4ZWZlM2Q1LTk5ZmQtNDNmOS1hMGNhL"
      + "WQ4N2E5Nzk5ZDQwZiIsImFjY2Vzc19ncm91cHMiOlsiOGJkZTZhNDktNWJmNS00ZmNkLWE1YzAtYTRlODhlNjk4ODdjIl0sInBheXNhZm"
      + "Vfc2NvcGVfZ3JvdXBfdHlwZSI6IlBNTEUiLCJicmFuZCI6IlBheXNhZmUiLCJlbWFpbCI6Im1hbm9qLnBhbGxhQHBheXNhZmUuY29tIiw"
      + "ib3duZXJfdHlwZSI6IlBNTEUifQ.d-_u_GfSvO-aJzQoyBPc9_RwdKwCa-NKsE1Vk4mX7nJB5zUvWD2B88FGbu5Qc6YpgB5b0EzzIAshI"
      + "6wX8vdXfuiDCDl-r25P1T_AbwULN_37KNlcI0Mp5oB086N7-MAUbCoIHQivPCo0tmyXgyNNbcIjK2qxkNHYSmpUT7l78ZhS6neYMrvbhB"
      + "zPIVTMDun-Vnkug19OGjScFDxXFGS-Xs71ge9B83Of0a5gZG6s1dx3HN3afn2m8a6DdtV8VQjlyNm8di8cJyhZSXkG0iZ7-pYrAzw3VZ7"
      + "0y-oQ2N6b0qf6EmhmiqNsxC37UNV9CGRCDq4oOFDOQsK6YHIWq0qq4g";


  /**
   * Construct IdentityManagementUserResource with some random data.
   *
   * @return IdentityManagementUserResource
   */
  public static IdentityManagementUserResource getIdentityManagementUserResource() {
    IdentityManagementUserResource createUserResource = new IdentityManagementUserResource();
    createUserResource.setUserName(USER_NAME);
    createUserResource.setEmail(EMAIL);
    createUserResource.setPmleId(USER_PMLE_ID.toString());
    createUserResource.setPassword(PASSWORD);
    createUserResource.setActivate(true);
    createUserResource.setFirstName(FIRSTNAME);
    createUserResource.setLastName(LASTNAME);
    createUserResource.setId(UUID);
    createUserResource.setMobilePhone(MOBILE_PHONE);
    createUserResource.setStatus(UserStatus.ACTIVE);
    createUserResource.setRoles(ROLES);
    createUserResource.setExternalId("12678");
    createUserResource.setAccessGroups(ACCESS_GROUPS);
    return createUserResource;
  }

  public static List<IdentityManagementUserResource> getIdentityManagementUserResourceList() {
    return Collections.singletonList(getIdentityManagementUserResource());
  }

  /**
   * Construct UserPasswordMigrationDto with some random data.
   *
   * @return UserPasswordMigrationDto
   */
  public static UserPasswordMigrationDto getUserMigrationDto() {
    UserPasswordMigrationDto userPasswordMigrationDto = new UserPasswordMigrationDto();
    userPasswordMigrationDto.setPassword(PASSWORD);
    return userPasswordMigrationDto;
  }

  /**
   * Construct UserPasswordMigrationResource with some random data.
   *
   * @return UserPasswordMigrationResource
   */
  public static UserPasswordMigrationResource getUserMigrationResource() {
    UserPasswordMigrationResource userPasswordMigrationResource = new UserPasswordMigrationResource();
    userPasswordMigrationResource.setPassword(PASSWORD);
    return userPasswordMigrationResource;
  }

  /**
   * Construct PegasusUserListResponseResource with some random data.
   *
   * @return PegasusUserListResponseResource
   */
  public static PegasusUserListResponseResource getPegasusUserListResponseResource() {
    PegasusUserResponseResource pegasusUserResponseResource = new PegasusUserResponseResource();
    pegasusUserResponseResource.setLoginName(USER_NAME);
    pegasusUserResponseResource.setFullName(FULL_NAME);
    pegasusUserResponseResource.setEmail(EMAIL);
    pegasusUserResponseResource.setPmleId(USER_PMLE_ID);
    pegasusUserResponseResource.setUuid(UUID);
    pegasusUserResponseResource.setStatus(STATUS);
    pegasusUserResponseResource.setAccessLevelTypeCode(ACCESS_LEVEL_TYPE_CODE);
    pegasusUserResponseResource.setAccessValue(ACCESS_VALUE);
    pegasusUserResponseResource.setAccessGroups(ACCESS_GROUPS);
    PegausUserRoleResource pegausUserRoleResource = new PegausUserRoleResource();
    pegausUserRoleResource.setRoleCode("BP_ADMIN");
    pegasusUserResponseResource.setRoles(Arrays.asList(pegausUserRoleResource));
    PegasusUserListResponseResource pegasusUserListResponseResource = new PegasusUserListResponseResource();
    pegasusUserListResponseResource.setUsers(Arrays.asList(pegasusUserResponseResource));
    return pegasusUserListResponseResource;

  }

  /**
   * Construct UserDto with some random data.
   *
   * @return UserDto
   */
  public static UserDto getUserDto() {
    UserDto userDto = new UserDto();
    userDto.setUserName(USER_NAME);
    userDto.setBusinessUnit("SKRILL");
    userDto.setEmail(EMAIL);
    userDto.setPmleId(USER_PMLE_ID.toString());
    userDto.setId(UUID);
    userDto.setStatus(UserStatus.ACTIVE);
    userDto.setRoleDto(getRoleDto());
    userDto.setAccessGroupDto(getAccessGroupDto());
    userDto.setAccessResources(getAccessResourcesList());
    userDto.setIsMailNotificationsEnabled(false);
    return userDto;
  }

  /**
   * Construct UserUpdationDto with test data.
   *
   * @return UserUpdationDto
   */
  public static UserUpdationDto getUserUpdationDto() {
    UserUpdationDto userUpdationDto = new UserUpdationDto();
    userUpdationDto.setFirstName("FIRST_NAME");
    userUpdationDto.setLastName(LASTNAME);
    userUpdationDto.setEmail(EMAIL);
    userUpdationDto.setApplicationName("SKRILL");
    userUpdationDto.setStatus(UserStatus.ACTIVE);
    userUpdationDto.setAccessGroupsToAdd(getAccessGroupDtoWithdData());
    userUpdationDto.setAccessGroupsToDelete(new ArrayList<>());
    userUpdationDto.setRolesToAdd(getUpdateUserRoleDto());
    userUpdationDto.setRolesToDelete(new ArrayList<>());
    userUpdationDto.setAccessResources(getUpdateUserAccessResourcesList());
    userUpdationDto.setAccessGroupsToHardDelete(new ArrayList<>());
    return userUpdationDto;
  }

  /**
   * Construct UserResource with test data.
   *
   * @return UserResource
   */
  public static UserResource getUserResource() {
    UserResource userResource = new UserResource();
    userResource.setActivate(true);
    userResource.setUserName(USER_NAME);
    userResource.setEmail(EMAIL);
    userResource.setFirstName(FIRSTNAME);
    userResource.setLastName(LASTNAME);
    userResource.setId(UUID);
    userResource.setMobilePhone(MOBILE_PHONE);
    userResource.setPassword(PASSWORD);
    userResource.setStatus(UserStatus.ACTIVE);
    return userResource;
  }

  /**
   * Construct UserUpdationResource with test data.
   *
   * @return UserUpdationResource
   */
  public static UserUpdationResource getUserUpdationResource() {
    UserUpdationResource userUpdationResource = new UserUpdationResource();
    userUpdationResource.setActivate(true);
    userUpdationResource.setUserName(USER_NAME);
    userUpdationResource.setEmail(EMAIL);
    userUpdationResource.setFirstName(FIRSTNAME);
    userUpdationResource.setLastName(LASTNAME);
    userUpdationResource.setId(UUID);
    userUpdationResource.setMobilePhone(MOBILE_PHONE);
    userUpdationResource.setPassword(PASSWORD);
    userUpdationResource.setStatus(UserStatus.ACTIVE);
    return userUpdationResource;
  }

  private static AccessGroupDto getAccessGroupDto() {
    AccessGroupDto accessGroupDto = new AccessGroupDto();
    accessGroupDto.setCustomAccessGroupDtos(new ArrayList<>());
    accessGroupDto.setExistingAccessGroupIds(new ArrayList<>());
    return accessGroupDto;
  }

  private static RoleDto getRoleDto() {
    RoleDto roleDto = new RoleDto();
    roleDto.setCustomRoles(new ArrayList<>());
    roleDto.setExistingRoles(new ArrayList<String>() {
      {
        add("BP_TEST_ROLE_1");
      }
    });
    return roleDto;
  }

  /**
   * sample AccessGroupResponseResource.
   *
   * @return AccessGroupResponseResource
   */
  public static AccessGroupResponseResource getAccessGroupResponseResource() {
    AccessGroupResponseResource accessGroupResponseResource = new AccessGroupResponseResource();
    accessGroupResponseResource.setCode("TEST_ACCESS_GROUP_ID");
    accessGroupResponseResource.setMerchantId("TEST_WALLET_ID");
    accessGroupResponseResource.setMerchantType("WALLETS");
    accessGroupResponseResource.setOwnerId("TEST_WALLET_ID");
    accessGroupResponseResource.setOwnerType("WALLETS");
    AccessGroupPolicy accessGroupPolicy1 = new AccessGroupPolicy();
    AccessPolicy accessPolicy1 = new AccessPolicy();
    accessPolicy1.setCode("TEST_ACCESS_POLICY_ID1");
    accessPolicy1.setAccessPolicyRights(getAccessPolicyRightList());
    accessGroupPolicy1.setAcessPolicy(accessPolicy1);
    AccessGroupPolicy accessGroupPolicy2 = new AccessGroupPolicy();
    AccessPolicy accessPolicy2 = new AccessPolicy();
    accessPolicy2.setCode("TEST_ACCESS_POLICY_ID2");
    accessGroupPolicy2.setAcessPolicy(accessPolicy2);
    List<AccessGroupPolicy> accessGroupPloicy = new ArrayList<>();
    accessGroupPloicy.add(accessGroupPolicy1);
    accessGroupPloicy.add(accessGroupPolicy2);
    accessGroupResponseResource.setType(AccessGroupType.CUSTOMIZED);
    accessGroupResponseResource.setAccessGroupPolicies(accessGroupPloicy);
    return accessGroupResponseResource;
  }

  /**
   * sample AccessGroupNameAvailabilityResponseResource.
   *
   * @return AccessGroupNameAvailabilityResponseResource
   */
  public static AccessGroupNameAvailabilityResponseResource getAccessGroupNameAvailabilityResponse() {
    AccessGroupNameAvailabilityResponseResource accessGroupNameAvailabilityResponseResource =
        new AccessGroupNameAvailabilityResponseResource();
    List<AccessGroupNameAvailabilityResource> accessGroupNameAvailabilityResources = new ArrayList<>();
    accessGroupNameAvailabilityResources.add(getAccessGroupNameAvailabilityResource());
    accessGroupNameAvailabilityResponseResource.setAccessGroupNames(accessGroupNameAvailabilityResources);
    return accessGroupNameAvailabilityResponseResource;
  }

  /**
   * sample AccessGroupNameAvailabilityResource.
   *
   * @return AccessGroupNameAvailabilityResource
   */
  public static AccessGroupNameAvailabilityResource getAccessGroupNameAvailabilityResource() {
    AccessGroupNameAvailabilityResource accessGroupNameAvailabilityResource = new AccessGroupNameAvailabilityResource();
    accessGroupNameAvailabilityResource.setAccessGroupName("CUSTOM_ACCESS_GROUP5");
    accessGroupNameAvailabilityResource.setAvailable(false);
    return accessGroupNameAvailabilityResource;
  }

  /**
   * sample RoleDto.
   *
   * @return RoleDto
   */
  public static RoleDto getUpdateUserRoleDto() {
    CustomRoleDto customRole1 = new CustomRoleDto();
    List<String> permissions = new ArrayList<>();
    permissions.add("TEST_PERMISSION1");
    permissions.add("TEST_PERMISSION2");
    customRole1.setPermissionList(permissions);
    customRole1.setRoleName("BP_TEST_NEW_ROLE_3");
    List<CustomRoleDto> customRole = new ArrayList<>();
    customRole.add(customRole1);
    RoleDto roleDto = new RoleDto();
    roleDto.setCustomRoles(customRole);
    roleDto.setExistingRoles(getExistingRoles());
    return roleDto;
  }

  /**
   * sample roles.
   */
  public static List<String> getRoles() {
    return new ArrayList<>(Arrays.asList("BP_TEST_ROLE_1", "BP_TEST_ROLE_2", "BP_TEST_ROLE_3", "BP_TEST_ROLE_4"));
  }

  /**
   * sample Existing roles.
   */
  public static List<String> getExistingRoles() {
    return new ArrayList<>(Arrays.asList("BP_TEST_ROLE_1", "BP_TEST_ROLE_2"));
  }

  /**
   * sample Invalid roles.
   */
  public static List<String> getInvalidRoles() {
    return new ArrayList<>(Arrays.asList("BP_INVALID_ROLE1", "BP_INVALID_ROLE_2"));
  }

  /**
   * sample accessGroups.
   */
  public static List<String> getAccessGroups() {
    return new ArrayList<>(Arrays.asList("TEST_ACCESS_GROUP_ID1", "TEST_ACCESS_GROUP_ID2"));
  }

  /**
   * sample accessPolicies.
   */
  public static List<String> getAccessPolicies() {
    return new ArrayList<>(Arrays.asList("TEST_ACCESS_POLICY_ID1", "TEST_ACCESS_POLICY_ID2"));
  }

  /**
   * sample CustomAccessGroupDto.
   *
   * @return CustomAccessGroupDto
   */
  public static CustomAccessGroupDto getCustomAccessGroupDto() {
    CustomAccessGroupDto accessGroup = new CustomAccessGroupDto();
    accessGroup.setAccessPolicyIds(getAccessPolicies());
    accessGroup.setMerchantId("TEST_WALLET_ID");
    accessGroup.setMerchantType("WALLETS");
    accessGroup.setStatus(Status.ACTIVE);
    accessGroup.setType(AccessGroupType.CUSTOMIZED);
    accessGroup.setName("CUSTOM_ACCESS_GROUP1");
    return accessGroup;
  }

  /**
   * sample AccessGroupDto.
   *
   * @return AccessGroupDto
   */
  private static AccessGroupDto getAccessGroupDtoWithdData() {
    AccessGroupDto accessGroupDto = new AccessGroupDto();
    List<CustomAccessGroupDto> customAccessGroups = new ArrayList<>();
    customAccessGroups.add(getCustomAccessGroupDto());
    accessGroupDto.setCustomAccessGroupDtos(customAccessGroups);
    accessGroupDto.setExistingAccessGroupIds(getAccessGroups());
    return accessGroupDto;
  }

  /**
   * sample AccessResources.
   *
   * @return AccessResources
   */
  public static AccessResources getAccessResources(String role, String resourceType) {
    AccessResources accessResources = new AccessResources();
    accessResources.setAccessGroupId("CUSTOM_ACCESS_GROUP5");
    accessResources.setId("297290");
    accessResources.setType(resourceType);
    accessResources.setOwnerId("TEST_WALLET_ID");
    accessResources.setOwnerType(resourceType);
    accessResources.setRole(role);
    return accessResources;
  }

  /**
   * sample UserAccessGroupMappingDao.
   *
   * @return UserAccessGroupMappingDao
   */
  public static UserAccessGroupMappingDao getUserAccessGroupMappingDao() {
    UserAccessGroupMappingDao userAccessGroupMappingDao = new UserAccessGroupMappingDao();
    userAccessGroupMappingDao.setAccessGroupCode("TEST_ACCESS_GROUP_ID1");
    userAccessGroupMappingDao.setLoginName("testuser@gmail.com");
    userAccessGroupMappingDao.setUserId("uuid22289223");
    userAccessGroupMappingDao.setResourceId("TEST_WALLET_ID");
    userAccessGroupMappingDao.setResourceType("WALLETS");
    return userAccessGroupMappingDao;
  }

  /**
   * sample accessGroups creation response AccessGroupResponseResource.
   */
  public static ResponseEntity<AccessGroupResponseResource> getCreateAccessGroupSucessResponse() {
    return new ResponseEntity<>(getAccessGroupResponseResource(), HttpStatus.CREATED);
  }

  /**
   * sample access policy creation response AccessPolicy.
   */
  public static ResponseEntity<AccessPolicy> getCreateAccessPolicySuccessResponse() {
    AccessPolicy accessPolicy = new AccessPolicy();
    accessPolicy.setCode("TEST_ACCESS_POLICY_CODE");
    return new ResponseEntity<>(accessPolicy, HttpStatus.CREATED);
  }

  /**
   * sample AccessResources List.
   */
  public static List<AccessResources> getAccessResourcesList() {
    List<AccessResources> accessResources = new ArrayList<>();
    AccessResources accessResource1 = getAccessResources(DataConstants.REGULAR, DataConstants.PMLE);
    accessResource1.setAccessGroupId("AG_01");
    accessResource1.setPermissions(getPermissionsList());
    accessResources.add(accessResource1);
    AccessResources accessResource2 = getAccessResources(DataConstants.ADMIN, DataConstants.PMLE);
    accessResource2.setAccessGroupId("AG_02");
    accessResources.add(accessResource2);
    return accessResources;
  }

  /**
   * sample PermissionDto List.
   */
  public static List<PermissionDto> getPermissionsList() {
    List<PermissionDto> permissions = new ArrayList<>();
    PermissionDto permission1 = new PermissionDto(100, "TRANSACTIONS_VIEW", 1);
    PermissionDto permission2 = new PermissionDto(101, "TRANSACTIONS_EDIT", 2);
    permissions.add(permission1);
    permissions.add(permission2);
    return permissions;
  }

  /**
   * sample ResourceUsersValidationDto.
   */
  public static ResourceUsersValidationDto getResourceUsersValidationDto() {
    ResourceUsersValidationDto resourceUsersValidationDto = new ResourceUsersValidationDto();
    resourceUsersValidationDto.setCanAddAdminUsers(true);
    resourceUsersValidationDto.setCanAddUsers(true);
    return resourceUsersValidationDto;
  }

  /**
   * sample PageResponseDto of AccessGroupResponseResource.
   */
  public static PageResponseDto<AccessGroupResponseResource> getFetchAccessGroupsSuccessResponse() {
    PageResponseDto<AccessGroupResponseResource> pageResponseDto = new PageResponseDto<AccessGroupResponseResource>();
    pageResponseDto.setItems(new ArrayList(Arrays.asList(getAccessGroupResponseResource())));
    return pageResponseDto;
  }

  /**
   * sample empty PageResponseDto of AccessGroupResponseResource.
   */
  public static PageResponseDto<AccessGroupResponseResource> getFetchAccessGroupsEmptyDataResponse() {
    PageResponseDto<AccessGroupResponseResource> pageResponseDto = new PageResponseDto<AccessGroupResponseResource>();
    pageResponseDto.setItems(new ArrayList<>());
    return pageResponseDto;
  }

  /**
   * returns sample {@link AuthorizationInfo}.
   */
  public static AuthorizationInfo getAuthorizationInfo(String token) {
    return JwtPayloadUtil.retrievePayload(token);
  }

  /**
   * sample fetchAccessGroupByCode response AccessGroupResponseResource.
   */
  public static ResponseEntity<AccessGroupResponseResource> getFetchAccessGroupByCodeSuccessResponse() {
    return getCreateAccessGroupSucessResponse();
  }

  /**
   * sample List of AccessGroupResponseResource.
   */
  public static List<AccessGroupResponseResource> getAccessGroupsFromInputListSuccessResponse() {
    return getFetchAccessGroupsSuccessResponse().getItems();
  }

  /**
   * sample access policy updation response AccessPolicy.
   */
  public static AccessPolicy getUpdateAccessPolicySuccessResponse() {
    return getCreateAccessPolicySuccessResponse().getBody();
  }

  /**
   * sample UpdateUserAccessResources List.
   */
  public static List<UpdateUserAccessResources> getUpdateUserAccessResourcesList() {
    List<UpdateUserAccessResources> accessResources = new ArrayList<>();
    UpdateUserAccessResources accessResource =
        getUpdateUserAccessResources(DataConstants.REGULAR, DataConstants.WALLETS, Action.ADD);
    accessResource.setPermissions(getPermissionsList());
    accessResources.add(accessResource);
    accessResources.add(getUpdateUserAccessResources(DataConstants.ADMIN, DataConstants.WALLETS, Action.ADD));
    return accessResources;
  }

  /**
   * sample UpdateUserAccessResources.
   *
   * @return UpdateUserAccessResources
   */
  public static UpdateUserAccessResources getUpdateUserAccessResources(String role, String resourceType,
      Action action) {
    UpdateUserAccessResources accessResource = new UpdateUserAccessResources();
    accessResource.setId("TEST_WALLET_ID");
    accessResource.setType(resourceType);
    accessResource.setOwnerId("TEST_WALLET_ID");
    accessResource.setOwnerType(resourceType);
    accessResource.setRole(role);
    accessResource.setAction(action);
    return accessResource;
  }

  /**
   * sample AccessPolicyRight List.
   */
  public static List<AccessPolicyRight> getAccessPolicyRightList() {
    List<AccessPolicyRight> accessPolicyRightList = new ArrayList<>();
    accessPolicyRightList.add(getAccessPolicyRight());
    return accessPolicyRightList;
  }

  /**
   * sample AccessPolicyRight.
   */
  public static AccessPolicyRight getAccessPolicyRight() {
    AccessPolicyRight accessPolicyRight = new AccessPolicyRight();
    AccessRight accessRight = new AccessRight();
    accessRight.setAccessTypeValue("test_access_type");
    accessRight.setCode("test_access_right_code");
    accessPolicyRight.setAccessRight(accessRight);
    return accessPolicyRight;
  }

  /**
   * sample UpdateUserAccessResources of Regular role List.
   */
  public static List<UpdateUserAccessResources> getUpdateUserAccessResourcesRegularRole() {
    List<UpdateUserAccessResources> accessResources = new ArrayList<>();
    UpdateUserAccessResources accessResource =
        getUpdateUserAccessResources(DataConstants.REGULAR, DataConstants.WALLETS, Action.ADD);
    accessResource.setId("TEST_WALLET_ID2");
    accessResource.setPermissions(getPermissionsList());
    accessResources.add(accessResource);
    return accessResources;
  }

  /**
   * sample UsersListResponseResource.
   */
  public static UsersListResponseResource getUsersListResponseResource(String accessResourceType) {
    List<UserResponseResource> users = new ArrayList<>();
    UserResponseResource userResponseResource = new UserResponseResource();
    List<AccessResources> accessResources = new ArrayList<>();
    accessResources.add(getAccessResources("BP_ADMIN", accessResourceType));
    userResponseResource.setAccessResources(accessResources);
    users.add(userResponseResource);
    UsersListResponseResource usersListResponseResource = new UsersListResponseResource();
    usersListResponseResource.setUsers(users);
    return usersListResponseResource;
  }

  /**
   * sample MerchantSearchResponse.
   */
  public static MerchantSearchResponse getMerchantSearchResponse() {
    List<ContactsDto> contactsDto = new ArrayList<>();
    ContactsDto dto = new ContactsDto();
    dto.setEmail("sample@email.com");
    contactsDto.add(dto);
    PaymentAccountResponse paymentAccountResponse = new PaymentAccountResponse();
    paymentAccountResponse.setContacts(contactsDto);
    paymentAccountResponse.setProcessingAccounts(getProcessingAccounts());
    paymentAccountResponse.setId("123");
    List<PaymentAccountResponse> paymentAccounts = new ArrayList<>();
    paymentAccounts.add(paymentAccountResponse);
    MerchantResponse res = new MerchantResponse();
    res.setPaymentAccounts(paymentAccounts);
    List<MerchantResponse> merchantResponse = new ArrayList<>();
    merchantResponse.add(res);
    MerchantSearchResponse merchantSearchResponse = new MerchantSearchResponse();
    merchantSearchResponse.setMerchants(merchantResponse);
    merchantSearchResponse.setTotalSearchMatches(1500);
    return merchantSearchResponse;
  }

  private static List<ProcessingAccount> getProcessingAccounts() {
    ProcessingAccount processingAccount = new ProcessingAccount();
    processingAccount.setPmleId("123");
    processingAccount.setPmleName("test_pmle");
    OnboardingInformation onboardingInformation = new OnboardingInformation();
    onboardingInformation.setPartnerId("456");
    onboardingInformation.setPartnerName("test_partner");
    BusinessDetails businessDetails = new BusinessDetails();
    businessDetails.setOnboardingInformation(onboardingInformation);
    LegalEntity legalEntity = new LegalEntity();
    legalEntity.setId("789");
    legalEntity.setDescription("test_legal_entity");
    businessDetails.setLegalEntity(legalEntity);
    businessDetails.setAccountGroups(new ArrayList<>(Arrays.asList("ag1", "ag2")));
    businessDetails.setTags(new ArrayList<>(Arrays.asList("EU_ACQUIRING_EEA")));
    processingAccount.setBusinessDetails(businessDetails);
    List<ProcessingAccount> processingAccounts = new ArrayList<>();
    processingAccounts.add(processingAccount);
    SourceAuthority sourceAuthority = new SourceAuthority();
    sourceAuthority.setOrigin("NETBANX");
    sourceAuthority.setReferenceId("1000101");
    processingAccount.setSourceAuthority(sourceAuthority);
    return processingAccounts;
  }

  /**
   * sample UserResponseResource.
   */
  public static UserResponseResource getUserResponseResource() {
    UserResponseResource userResponseResource = new UserResponseResource();
    userResponseResource.setUserName("test_loginName");
    userResponseResource.setAccessGroups(new ArrayList<>(Arrays.asList("AG01", "AG02")));
    return userResponseResource;
  }

  /**
   * sample UserResponseResource List.
   */
  public static List<UserResponseResource> getUserResponseResourceList() {
    List<UserResponseResource> userResponseResourceList = new ArrayList<>();
    userResponseResourceList.add(getUserResponseResource());
    return userResponseResourceList;
  }

  /**
   * sample AccessGroupResponseResource List.
   */
  public static List<AccessGroupResponseResource> getAccessGroupResponseResourceList() {
    List<AccessGroupResponseResource> accessGroupResponseResourceList = new ArrayList<>();
    AccessGroupResponseResource accessGroupResponseResource = getAccessGroupResponseResource();
    accessGroupResponseResourceList.add(accessGroupResponseResource);
    return accessGroupResponseResourceList;
  }

  /**
   * sample List of AccessRolePermissions.
   */
  public static List<String> getAccessRolePermissions() {
    List<String> accessRolePermissions = new ArrayList<>();
    accessRolePermissions.add("group-business-portal:user-favorites:update");
    accessRolePermissions.add("group-business-portal:user-favorites:get");
    return accessRolePermissions;
  }

  /**
   * sample UserProvisioningUserResource.
   */
  public static UserProvisioningUserResource getUserProvisioningUserResource() {
    UserProvisioningUserResource userProvisioningUserResource = new UserProvisioningUserResource();
    userProvisioningUserResource.setAccessGroups(new ArrayList<>(Arrays.asList("AG01", "AG02")));
    userProvisioningUserResource.setUserName("test_user");
    userProvisioningUserResource.setFirstName("testFirstName");
    userProvisioningUserResource.setLastName("testLastName");
    userProvisioningUserResource.setEmail("testEmail");
    userProvisioningUserResource.setUserSummary(getUserSummaryForMoreThanThreeMerchants());
    return userProvisioningUserResource;
  }

  /**
   * sample UserSummary.
   */
  public static UserSummary getUserSummary() {
    Merchant merchant1 = new Merchant();
    MerchantInfo merchantInfo1 = new MerchantInfo();
    merchantInfo1.setMerchantId("test_merchantId1");
    merchantInfo1.setMerchantName("test_merchantName1");
    merchant1.setMerchantInfo(merchantInfo1);
    merchant1.setAccounts(new ArrayList<>(Arrays.asList("123", "456")));
    merchant1.setPermissions(new ArrayList<>(Arrays.asList("permission1", "permission2")));
    merchant1.setRole(DataConstants.BP_EU_ADMIN);
    Merchant merchant2 = new Merchant();
    MerchantInfo merchantInfo2 = new MerchantInfo();
    merchantInfo2.setMerchantId("test_merchantId1");
    merchantInfo2.setMerchantName("test_merchantName1");
    merchant2.setMerchantInfo(merchantInfo2);
    merchant2.setAccounts(new ArrayList<>(Arrays.asList("789", "567")));
    merchant2.setPermissions(new ArrayList<>(Arrays.asList("permission3", "permission4")));
    merchant2.setRole(DataConstants.BP_ADMIN);
    List<Merchant> merchants = new ArrayList<>();
    merchants.add(merchant1);
    merchants.add(merchant2);
    merchants.add(merchant1);
    UserSummary userSummary = new UserSummary();
    userSummary.setMerchants(merchants);
    return userSummary;
  }

  /**
   * sample UserSummary.
   */
  public static UserSummary getUserSummaryForMoreThanThreeMerchants() {
    List<Merchant> merchants = new ArrayList<>();
    merchants.addAll(getUserSummary().getMerchants());
    Merchant merchant1 = new Merchant();
    MerchantInfo merchantInfo1 = new MerchantInfo();
    merchantInfo1.setMerchantId("test_merchantId3");
    merchantInfo1.setMerchantName("test_merchantName3");
    merchant1.setMerchantInfo(merchantInfo1);
    merchant1.setAccounts(new ArrayList<>(Arrays.asList("123", "456")));
    merchant1
        .setPermissions(new ArrayList<>(Arrays.asList("permission1", "permission2", "permission3", "permission4")));
    merchant1.setRole(DataConstants.BP_EU_ADMIN);
    merchants.add(merchant1);
    Merchant merchant2 = new Merchant();
    MerchantInfo merchantInfo2 = new MerchantInfo();
    merchantInfo2.setMerchantId("test_merchantId4");
    merchantInfo2.setMerchantName("test_merchantName4");
    merchant2.setMerchantInfo(merchantInfo2);
    merchant2.setAccounts(new ArrayList<>(Arrays.asList("789", "567")));
    merchant2.setPermissions(new ArrayList<>(Arrays.asList("permission3", "permission4")));
    merchant2.setRole(DataConstants.BP_ADMIN);
    merchants.add(merchant2);
    UserSummary userSummary = new UserSummary();
    userSummary.setMerchants(merchants);
    return userSummary;
  }

  /**
   * Sample async wallets-info.
   */
  public static List<CompletableFuture<BasicWalletInfo>> getAsyncWalletsinfo() {
    List<CompletableFuture<BasicWalletInfo>> list = new ArrayList<>();
    list.add(sampleWalletInfoAsync());
    list.add(sampleWalletInfoAsync());
    return list;
  }

  /**
   * sampleWalletInfo
   *
   * @return WalletInfo WalletInfo.
   */
  public static CompletableFuture<BasicWalletInfo> sampleWalletInfoAsync() {
    BasicWalletInfo basicWalletInfo = new BasicWalletInfo();
    basicWalletInfo.setId("TEST_WALLET_ID");
    BusinessProfile businessProfile = new BusinessProfile();
    businessProfile.setCompanyName("testName");
    basicWalletInfo.setBusinessProfile(businessProfile);
    return CompletableFuture.completedFuture(basicWalletInfo);
  }

  /**
   * bulkWalletDetailResponse
   *
   * @return BulkWalletDetailResponse bulkWalletDetailResponse.
   */
  public static BulkWalletDetailResponse sampleBulkWalletDetail() {
    BulkWalletDetailResponse bulkWalletDetailResponse = new BulkWalletDetailResponse();
    bulkWalletDetailResponse.setCustomers(Arrays.asList(getBasicWalletInfo()));
    return bulkWalletDetailResponse;
  }

  /**
   * Sample pegasus empty user list.
   */
  public static PegasusUserListResponseResource getPegasusUserListWithEmptyUsers() {
    PegasusUserListResponseResource userList = new PegasusUserListResponseResource();
    userList.setUsers(new ArrayList<>());
    userList.setCount(0L);
    return userList;
  }

  /**
   * Sample idm empty user list.
   */
  public static IdentityManagementUserListResource getIdentityManagementUserListWithEmptyUsers() {
    IdentityManagementUserListResource userListResource = new IdentityManagementUserListResource();
    userListResource.setUsers(new ArrayList<>());
    return userListResource;
  }

  /**
   * Sample pegasus user list.
   */
  public static PegasusUserListResponseResource getPegasusUserList() {
    PegasusUserResponseResource user = new PegasusUserResponseResource();
    user.setLoginName("abc123");
    PegasusUserListResponseResource userList = new PegasusUserListResponseResource();
    userList.setUsers(Arrays.asList(user));
    userList.setCount(1L);
    return userList;
  }

  /**
   * Sample idm user list.
   */
  public static IdentityManagementUserListResource getIdentityManagementUserList() {
    IdentityManagementUserResource user = new IdentityManagementUserResource();
    user.setId("1234");
    user.setEmail("abc@paysafe.com");
    user.setUserName("abc123");
    IdentityManagementUserListResource userListResource = new IdentityManagementUserListResource();
    userListResource.setUsers(Arrays.asList(user));
    return userListResource;
  }

  /**
   * Sample UpdateUserStatusResource.
   *
   * @return UpdateUserStatusResource
   */
  public static UpdateUserStatusResource getUpdateUserStatusResource() {
    UpdateUserStatusResource userResource = new UpdateUserStatusResource();
    userResource.setEmail("sample@email.com");
    userResource.setUserName("sampleUserName");
    userResource.setAccessGroupId("sampleId");
    userResource.setAction(UserAction.ACTIVATE);
    return userResource;
  }

  /**
   * Sample UpdateUserStatusResource.
   *
   * @return UpdateUserStatusResource
   */
  public static UsersListResponseResource getUserListResponseResource(AccessResourceStatus status,
      UserStatus userStatus, boolean isEmptyAccessResource) {
    UserResponseResource resource = new UserResponseResource();
    resource.setEmail("sample@email.com");
    List<AccessResources> accessResources = new ArrayList<>();
    if (!isEmptyAccessResource) {
      AccessResources res = new AccessResources();
      res.setStatus(status);
      accessResources.add(res);
    }
    List<UserResponseResource> users = new ArrayList<>();
    resource.setAccessResources(accessResources);
    resource.setStatus(userStatus);
    users.add(resource);
    UsersListResponseResource userListResponseResource = new UsersListResponseResource();
    userListResponseResource.setUsers(users);
    return userListResponseResource;

  }

  /**
   * Sample TokenResponseResource.
   */
  public static TokenResponseResource getTokenResponseResource() {
    TokenResponseResource tokenResponseResource = new TokenResponseResource();
    tokenResponseResource.setId("1234");
    return tokenResponseResource;
  }

  /**
   * Sample BasicWalletInfo.
   */
  public static BasicWalletInfo getBasicWalletInfo() {
    BasicWalletInfo basicWalletInfo = new BasicWalletInfo();
    basicWalletInfo.setId("1234");
    BusinessProfile businessProfile = new BusinessProfile();
    businessProfile.setCompanyName("test_company");
    businessProfile.setUrl("test_url");
    basicWalletInfo.setBusinessProfile(businessProfile);
    Profile profile = new Profile();
    profile.setBrand("SKRILL");
    profile.setLockLevel("1");
    basicWalletInfo.setProfile(profile);
    MerchantSettings merchantSettings = new MerchantSettings();
    merchantSettings.setMerchant(true);
    basicWalletInfo.setMerchantSettings(merchantSettings);
    EwalletAccount ewalletAccount = new EwalletAccount();
    ewalletAccount.setCurrency("USD");
    basicWalletInfo.setEwalletAccounts(Arrays.asList(ewalletAccount));
    return basicWalletInfo;
  }

  /**
   * Sample User.
   */
  public static User getUser() {
    User user = new User();
    user.setUserId("user1");
    user.setLoginName("test_loginName");
    user.setUserFirstName("test_firstName");
    user.setEmail("testxyz@test.com");
    user.setStatus(Status.ACTIVE);
    user.setApplication("SKRILL");
    user.setUserExternalId("1234");
    user.setMfaEnabled("Y");
    user.setAccessGroupMappingDaos(new ArrayList<>(Arrays.asList(getUserAccessGroupMappingDao())));
    user.setUserAssignedApplications(getUserAssignedApplications());
    return user;
  }

  /**
   * Sample User for Mfa.
   */
  public static User getUserForMfa() {
    User user = new User();
    user.setLoginName("test_loginName");
    user.setUserFirstName("test_firstName");
    user.setEmail("testxyz@test.com");
    user.setStatus(Status.ACTIVE);
    user.setApplication("PORTAL");
    user.setUserExternalId("1234");
    user.setUserId("85586737-7a64-4327-b425-183202171c88");
    user.setAccessGroupMappingDaos(new ArrayList<>(Arrays.asList(getUserAccessGroupMappingDao())));
    user.setUserAssignedApplications(getUserAssignedApplications());
    return user;
  }

  /**
   * Sample User for Mfa Admin.
   */
  public static User getUserForMfaTestUser() {
    User user = new User();
    user.setLoginName("test_loginName");
    user.setUserFirstName("test_firstName");
    user.setEmail("testxyz@test.com");
    user.setStatus(Status.ACTIVE);
    user.setApplication("PORTAL");
    user.setUserExternalId("1234");
    user.setUserId("e8efe3d5-99fd-43f9-a0ca-d87a9799d40f");
    user.setAccessGroupMappingDaos(new ArrayList<>(Arrays.asList(getUserAccessGroupMappingDao())));
    user.setUserAssignedApplications(getUserAssignedApplications());
    return user;
  }


  /**
   * Sample User.
   */
  public static User getProvisionedUser() {
    User user = new User();
    user.setLoginName("test_loginName");
    user.setUserFirstName("test_firstName");
    user.setStatus(Status.PROVISIONED);
    user.setApplication("SKRILL");
    return user;
  }

  /**
   * Sample GroupedCustomIds.
   */
  public static GroupedCustomerIdsResource getGroupCustomerIds() {
    GroupedCustomerIdsResource groupedCustomerIdsResource = new GroupedCustomerIdsResource();
    String[] customerIds = {"2791342", "2791337"};
    groupedCustomerIdsResource.setCustomerIds(Arrays.asList(customerIds));
    return groupedCustomerIdsResource;
  }

  /**
   * Sample GroupedCustomIds.
   */
  public static LinkedCustomerIdsResource getLinkedCustomerIds() {
    LinkedCustomerIdsResource linkedCustomerIdsResource = new LinkedCustomerIdsResource();
    CustomerObject customerObject = new CustomerObject();
    customerObject.setId("2791342");
    customerObject.setId("2791337");
    linkedCustomerIdsResource.setCustomers(Arrays.asList(customerObject));
    return linkedCustomerIdsResource;
  }


  /**
   * Sample EwalletAccount.
   */
  public static EwalletAccount getEwalletAccount() {
    EwalletAccount ewalletAccount = new EwalletAccount();
    ewalletAccount.setCurrency("EUR");
    return ewalletAccount;
  }

  /**
   * Sample async LinkedCustomerIdsResource.
   */
  public static CompletableFuture<LinkedCustomerIdsResource> getAsyncLinkedCustomerIds() {
    return CompletableFuture.completedFuture(getLinkedCustomerIds());
  }

  /**
   * Sample async GroupedCustomerIdsResource.
   */
  public static CompletableFuture<GroupedCustomerIdsResource> getAsyncGroupedCustomerIds() {
    return CompletableFuture.completedFuture(getGroupCustomerIds());
  }

  /**
   * Sample SkrillTellerMigrationDto.
   */
  public static SkrillTellerMigrationDto getSkrillTellerMigrationDto() {
    SkrillAccessResources accessResource = new SkrillAccessResources();
    accessResource.setRole("REGULAR");
    accessResource.setStatus(AccessResourceStatus.BLOCKED);
    accessResource.setResourceType("resourceType");
    accessResource.setResourceId("297290");
    accessResource.setPermissions(new ArrayList<>(Arrays.asList(SkrillPermissions.BALANCES)));
    List<SkrillAccessResources> accessResources = new ArrayList<>();
    accessResources.add(accessResource);
    SkrillTellerMigrationDto migrationDto = new SkrillTellerMigrationDto();
    migrationDto.setAccessResources(accessResources);
    return migrationDto;
  }

  /**
   * Sample UsersSummary.
   */
  public static UsersSummary getUsersSummary() {
    UsersSummary usersSummary = new UsersSummary();
    usersSummary.setLoginName("test_loginName");
    return usersSummary;
  }

  /**
   * Sample WalletPermission List.
   */
  public static List<WalletPermission> getWalletPermissions() {
    WalletPermission walletPermission = new WalletPermission();
    walletPermission.setPermission("Balances");
    List<WalletPermission> walletPermissions = new ArrayList<>();
    walletPermissions.add(walletPermission);
    return walletPermissions;
  }

  /**
   * Sample PasswordImportRequestResource.
   */
  public static PasswordImportRequestResource getPasswordImportRequestResourceForTest() {

    PasswordHookContext passwordHookContext = new PasswordHookContext();
    PasswordHookCredential passwordHookCredential = new PasswordHookCredential();
    passwordHookCredential.setUsername("testUsername");
    passwordHookCredential.setPassword("testPassword");
    passwordHookContext.setCredential(passwordHookCredential);
    PasswordHookData passwordHookData = new PasswordHookData();
    passwordHookData.setContext(passwordHookContext);
    PasswordImportRequestResource passwordImportRequestResource = new PasswordImportRequestResource();
    passwordImportRequestResource.setData(passwordHookData);
    return passwordImportRequestResource;
  }

  /**
   * Sample SkrillContactEmailsResource.
   */
  public static SkrillContactEmailsResource getSkrillContactEmails() {
    SkrillContactEmailsResource skrillContactEmails = new SkrillContactEmailsResource();
    skrillContactEmails.setContactEmails(getContactEmailList());
    return skrillContactEmails;
  }

  /**
   * Sample ContactEmail List.
   */
  public static List<ContactEmail> getContactEmailList() {
    ContactEmail contactEmail = new ContactEmail();
    contactEmail.setContactEmail("test_user1@gmail.com");
    contactEmail.setType(DataConstants.BUSINESS);
    contactEmail.setId("1234");
    List<ContactEmail> contactEmails = new ArrayList<>();
    contactEmails.add(contactEmail);
    return contactEmails;
  }

  /**
   * Sample Okta Event hook response Json.
   *
   * @throws IOException ex.
   */
  public static JsonNode getOktaEventHookResponse(boolean isSuspendEvent) throws IOException {
    byte[] jsonData;
    final String filePath;
    if (isSuspendEvent) {
      filePath = "src/testCommon/resources/sampleEventHookResponseForSuspendedEvent.json";
    } else {
      filePath = "src/testCommon/resources/sampleEventHookResponseJson.json";
    }
    ObjectMapper mapper = new ObjectMapper();
    jsonData = Files.readAllBytes(Paths.get(filePath));
    JsonNode oktaResponseJsonNode = mapper.readTree(jsonData);
    return oktaResponseJsonNode;
  }

  /**
   * Sample Okta Event hook response Json for login general non-success.
   *
   * @throws IOException ex.
   */
  public static JsonNode getOktaEventHookResponseGeneralNonSuccess() throws IOException {
    byte[] jsonData;
    ObjectMapper mapper = new ObjectMapper();
    jsonData = Files.readAllBytes(
        Paths.get("src/testCommon/resources/sampleSessionStartEventHookResponseForGeneralNonSuccessJson.json"));
    JsonNode oktaResponseJsonNode = mapper.readTree(jsonData);
    return oktaResponseJsonNode;
  }

  /**
   * Sample Okta Event hook response Json for login success.
   *
   * @throws IOException ex.
   */
  public static JsonNode getOktaEventHookResponseSuccess() throws IOException {
    byte[] jsonData;
    ObjectMapper mapper = new ObjectMapper();
    jsonData =
        Files.readAllBytes(Paths.get("src/testCommon/resources/sampleSessionStartEventHookResponseSuccessJson.json"));
    JsonNode oktaResponseJsonNode = mapper.readTree(jsonData);
    return oktaResponseJsonNode;
  }

  /**
   * Sample Okta Event hook response Json for create user.
   */
  public static JsonNode getOktaCreateUserEventHookResponse() throws IOException {
    byte[] jsonData;
    ObjectMapper mapper = new ObjectMapper();
    jsonData = Files.readAllBytes(Paths.get("src/testCommon/resources/sampleCreateUserEventHookResponse.json"));
    JsonNode oktaResponseJsonNode = mapper.readTree(jsonData);
    return oktaResponseJsonNode;
  }

  /**
   * Sample UserMigrationResource.
   */
  public static UserMigrationResource getUserMigrationRequest() {
    UserMigrationResource userMigrationResource = new UserMigrationResource();
    userMigrationResource.setAccessResources(getSkrillAccessResources());
    return userMigrationResource;
  }

  /**
   * Sample SkrillAccessResources List.
   */
  public static List<SkrillAccessResources> getSkrillAccessResources() {
    SkrillAccessResources accessResource = new SkrillAccessResources();
    accessResource.setRole("REGULAR");
    accessResource.setStatus(AccessResourceStatus.BLOCKED);
    accessResource.setResourceType("resourceType");
    accessResource.setResourceId("297290");
    accessResource.setPermissions(new ArrayList<>(Arrays.asList(SkrillPermissions.BALANCES)));
    List<SkrillAccessResources> accessResources = new ArrayList<>();
    accessResources.add(accessResource);
    return accessResources;
  }

  /**
   * Sample WalletUserCountResource.
   */
  public static WalletUserCountResource getWalletUserCountResource() {
    WalletUserCountResource walletUserCountResource = new WalletUserCountResource();
    walletUserCountResource.setAdminCount(3L);
    walletUserCountResource.setResourceId("1234");
    walletUserCountResource.setResourceName(DataConstants.WALLETS);
    walletUserCountResource.setUserCount(30L);
    return walletUserCountResource;
  }

  /**
   * Sample WalletUserCountResource List.
   */
  public static List<WalletUserCountResource> getWalletUserCountResourceList() {
    List<WalletUserCountResource> walletUserCountResources = new ArrayList<>();
    walletUserCountResources.add(getWalletUserCountResource());
    return walletUserCountResources;
  }

  /**
   * Sample BulkUsers List.
   */
  public static List<BulkUsers> getBulkUsersList() {
    List<BulkUsers> bulkUsers = new ArrayList();
    BulkUsers bulkUser1 = new BulkUsers(3L, "1234");
    bulkUsers.add(bulkUser1);
    BulkUsers bulkUser2 = new BulkUsers(55L, "7687");
    bulkUsers.add(bulkUser2);
    return bulkUsers;
  }

  /**
   * returns prev List of AccessResources for edit-user audit.
   */
  public static List<AccessResources> getEditUserAuditPrevAccessResources() {
    AccessResources accessResource1 = new AccessResources();
    accessResource1.setRole(DataConstants.REGULAR);
    accessResource1.setId("123");
    accessResource1.setType(DataConstants.WALLETS);
    PermissionDto permission1 = new PermissionDto();
    permission1.setLabel("test_permission1");
    PermissionDto permission2 = new PermissionDto();
    permission2.setLabel("test_permission2");
    PermissionDto permission3 = new PermissionDto();
    permission3.setLabel("test_permission2");
    accessResource1.setPermissions(new ArrayList<>(Arrays.asList(permission1, permission2, permission3)));

    AccessResources accessResource2 = new AccessResources();
    accessResource2.setRole(DataConstants.ADMIN);
    accessResource2.setId("234");
    PermissionDto adminPermission = new PermissionDto();
    adminPermission.setLabel("admin_permission");
    accessResource2.setPermissions(new ArrayList<>(Arrays.asList(adminPermission)));
    accessResource2.setType(DataConstants.WALLETS);

    AccessResources accessResource3 = new AccessResources();
    accessResource3.setRole(DataConstants.ADMIN);
    accessResource3.setId("345");
    accessResource3.setPermissions(new ArrayList<>(Arrays.asList(adminPermission)));
    accessResource3.setType(DataConstants.WALLETS);

    AccessResources accessResource4 = new AccessResources();
    accessResource4.setRole(DataConstants.REGULAR);
    accessResource4.setId("456");
    accessResource4.setType(DataConstants.WALLETS);
    PermissionDto permission4 = new PermissionDto();
    permission4.setLabel("test_permission4");
    accessResource4.setPermissions(new ArrayList<>(Arrays.asList(permission4)));

    AccessResources accessResource5 = new AccessResources();
    accessResource5.setRole(DataConstants.ADMIN);
    accessResource5.setId("901");
    accessResource5.setPermissions(new ArrayList<>(Arrays.asList(adminPermission)));
    accessResource5.setType(DataConstants.WALLETS);

    AccessResources accessResource6 = new AccessResources();
    accessResource6.setRole(DataConstants.REGULAR);
    accessResource6.setId("902");
    accessResource6.setPermissions(new ArrayList<>(Arrays.asList(permission1)));
    accessResource6.setType(DataConstants.WALLETS);
    return new ArrayList<>(Arrays.asList(accessResource1, accessResource2, accessResource3, accessResource4,
        accessResource5, accessResource6));
  }

  /**
   * returns latest List of AccessResources for edit-user audit.
   */
  public static List<AccessResources> getEditUserAuditLatestAccessResources() {
    AccessResources accessResource1 = new AccessResources();
    accessResource1.setRole(DataConstants.REGULAR);
    accessResource1.setId("123");
    accessResource1.setType(DataConstants.WALLETS);
    PermissionDto permission1 = new PermissionDto();
    permission1.setLabel("test_permission1");
    PermissionDto permission2 = new PermissionDto();
    permission2.setLabel("test_permission2");
    accessResource1.setPermissions(new ArrayList<>(Arrays.asList(permission1, permission2)));

    AccessResources accessResource2 = new AccessResources();
    accessResource2.setRole(DataConstants.ADMIN);
    accessResource2.setId("789");
    PermissionDto adminPermission = new PermissionDto();
    adminPermission.setLabel("admin_permission");
    accessResource2.setPermissions(new ArrayList<>(Arrays.asList(adminPermission)));
    accessResource2.setType(DataConstants.WALLETS);

    AccessResources accessResource4 = new AccessResources();
    accessResource4.setRole(DataConstants.ADMIN);
    accessResource4.setId("456");
    accessResource4.setPermissions(new ArrayList<>(Arrays.asList(adminPermission)));
    accessResource4.setType(DataConstants.WALLETS);

    AccessResources accessResource5 = new AccessResources();
    accessResource5.setRole(DataConstants.ADMIN);
    accessResource5.setId("901");
    accessResource5.setPermissions(new ArrayList<>(Arrays.asList(adminPermission)));
    accessResource5.setType(DataConstants.WALLETS);

    AccessResources accessResource6 = new AccessResources();
    accessResource6.setRole(DataConstants.REGULAR);
    accessResource6.setId("902");
    accessResource6.setPermissions(new ArrayList<>(Arrays.asList(permission1)));
    accessResource6.setType(DataConstants.WALLETS);
    return new ArrayList<>(
        Arrays.asList(accessResource1, accessResource2, accessResource4, accessResource5, accessResource6));
  }

  /**
   * MerchantSearchAfterRequest merchantSearchAfterRequest.
   */
  public static MerchantSearchAfterRequest getMerchantSearchAfterRequest() {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    request.setLimit(100);
    request.setOffset(0);
    request.setSortOrder("ASC");
    return request;
  }

  /**
   * returns List of RoleModulesResource.
   */
  public static List<RoleModulesResource> getRoleModulesResourcesList() {
    RoleModulesResource roleModulesResource = new RoleModulesResource();
    roleModulesResource.setDisplayOrder(0);
    roleModulesResource.setRoleType("Admin");
    roleModulesResource.setRoleValue("BP_EU_ADMIN");
    roleModulesResource.setModules(getModulesList());
    List<RoleModulesResource> roleModulesResources = new ArrayList<>();
    roleModulesResources.add(roleModulesResource);
    return roleModulesResources;

  }

  /**
   * returns List of RoleModulesList.
   */
  public static List<RoleModulesList> getModulesForRolesList() {
    RoleModulesList roleModulesList = new RoleModulesList();
    roleModulesList.setDisplayOrder(0);
    roleModulesList.setEnabled(true);
    roleModulesList.setModuleId("BP_ADMIN_TRANSACTIONS");
    roleModulesList.setRole("BP_EU_ADMIN");
    List<RoleModulesList> roleModulesLists = new ArrayList<>();
    roleModulesLists.add(roleModulesList);
    return roleModulesLists;

  }

  /**
   * returns List of ModuleResource.
   */
  public static List<ModuleResource> getModulesList() {
    ModuleResource moduleResource = new ModuleResource();
    moduleResource.setDescriptions("transaction level1");
    moduleResource.setDisplayOrder(0);
    moduleResource.setEditable(true);
    moduleResource.setExpand(false);
    moduleResource.setPermissionsList(new ArrayList<>(Arrays.asList("get-transactions", "transactions-level1")));
    List<ModuleResource> moduleResources = new ArrayList<>();
    moduleResources.add(moduleResource);
    return moduleResources;
  }

  /**
   * returns BusinessInitiativeInfo businessInitiativeInfo.
   */
  public static BusinessInitiativeInfo getBusinessInitiativeInfo() {
    BusinessInitiativeInfo businessInitiativeInfo = new BusinessInitiativeInfo();
    businessInitiativeInfo.setBusinessInitiative("EU_ACQUIRING_EEA");
    businessInitiativeInfo.setDescription("EU Acquiring EEA Initiative");
    businessInitiativeInfo.setBusinessInitiativeRoles(getBusinessInitiativeRoles());
    return businessInitiativeInfo;
  }

  /**
   * returns List of BusinessInitiativeRoles.
   */
  public static List<BusinessInitiativeRoles> getBusinessInitiativeRoles() {
    BusinessInitiativeRoles adminRole = new BusinessInitiativeRoles();
    adminRole.setBusinessInitiative("EU_ACQUIRING_EEA");
    adminRole.setDisplayOrder(0);
    adminRole.setEnabled(true);
    adminRole.setRole("BP_EU_ADMIN");
    adminRole.setRoleType("Admin");

    BusinessInitiativeRoles developerRole = new BusinessInitiativeRoles();
    developerRole.setBusinessInitiative("EU_ACQUIRING_EEA");
    developerRole.setDisplayOrder(0);
    developerRole.setEnabled(true);
    developerRole.setRole("BP_EU_DEVELOPER");
    developerRole.setRoleType("Developer");

    List<BusinessInitiativeRoles> businessInitiativeRoles = new ArrayList<>();
    businessInitiativeRoles.add(developerRole);
    businessInitiativeRoles.add(adminRole);
    return businessInitiativeRoles;
  }

  /**
   * returns List of RoleModules.
   */
  public static List<RoleModules> getRoleModules() {
    RoleModules roleModule1 = new RoleModules();
    roleModule1.setDisplayOrder(0);
    roleModule1.setEnabled(true);
    roleModule1.setModuleId("BP_ADMIN_TRANSACTION");
    roleModule1.setRole("BP_EU_ADMIN");

    RoleModules roleModule2 = new RoleModules();
    roleModule2.setDisplayOrder(0);
    roleModule2.setEnabled(true);
    roleModule2.setModuleId("BP_ADMIN_TRANSACTION");
    roleModule2.setRole("BP_EU_ADMIN");

    List<RoleModules> roleModules = new ArrayList<>();
    roleModules.add(roleModule1);
    roleModules.add(roleModule2);
    return roleModules;
  }

  /**
   * returns Module module.
   */
  public static Module getModule() {
    Module module = new Module();
    module.setDescriptions("transaction level1");
    module.setDisplayOrder(0);
    module.setEditable(true);
    module.setEnabled(true);
    module.setExpand(false);
    module.setId("BP_CUSTOM_TRANSACTIONS_LEVEL1");
    Module childModule = new Module();
    childModule.setDescriptions("transaction level11");
    childModule.setDisplayOrder(0);
    childModule.setEditable(true);
    childModule.setEnabled(true);
    childModule.setExpand(false);
    childModule.setId("BP_CUSTOM_TRANSACTIONS_LEVEL11");
    childModule.setChildren(null);

    module.setChildren(new HashSet<>(Arrays.asList(childModule)));
    return module;
  }

  /**
   * returns List of ModuleAccessLevel.
   */
  public static List<ModuleAccessLevel> getModuleAccessLevelList() {
    ModuleAccessLevel moduleAccessLevel1 = new ModuleAccessLevel();
    moduleAccessLevel1.setEnabled(true);
    moduleAccessLevel1.setModuleId("BP_CUSTOM_TRANSACTIONS_LEVEL2");
    moduleAccessLevel1.setShow("PMLE");

    ModuleAccessLevel moduleAccessLevel2 = new ModuleAccessLevel();
    moduleAccessLevel2.setEnabled(true);
    moduleAccessLevel2.setModuleId("BP_CUSTOM_TRANSACTIONS_LEVEL2");
    moduleAccessLevel2.setShow("MLE");
    List<ModuleAccessLevel> moduleAccessLevelList = new ArrayList<>();
    moduleAccessLevelList.add(moduleAccessLevel1);
    moduleAccessLevelList.add(moduleAccessLevel2);
    return moduleAccessLevelList;
  }

  /**
   * returns List of ModulePermissions.
   */
  public static List<ModulePermissions> getModulePermissions() {
    ModulePermissions modulePermission1 = new ModulePermissions();
    modulePermission1.setEnabled(true);
    modulePermission1.setModuleId("BP_CUSTOM_TRANSACTIONS_LEVEL3");
    modulePermission1.setPermission("level3 view");

    ModulePermissions modulePermission2 = new ModulePermissions();
    modulePermission2.setEnabled(true);
    modulePermission2.setModuleId("BP_CUSTOM_TRANSACTIONS_LEVEL3");
    modulePermission2.setPermission("level3 view edit");
    List<ModulePermissions> modulePermissions = new ArrayList<>();
    modulePermissions.add(modulePermission1);
    modulePermissions.add(modulePermission2);
    return modulePermissions;
  }

  /**
   * Constructs AccountGroupsV2Resource.
   *
   * @return AccountGroupsV2Resource
   */
  public static AccountGroupsV2Resource getAccountGroupsV2Resource() {
    AccountGroupsV2Resource v2Resource = new AccountGroupsV2Resource();
    Meta meta = new Meta();
    meta.setNumberOfRecords(2L);
    v2Resource.setMeta(meta);
    List<AccountGroup> accessGroupList = new ArrayList<>();
    accessGroupList.add(getAccountGroup1());
    accessGroupList.add(getAccountGroup2());
    v2Resource.setAccountGroups(accessGroupList);
    return v2Resource;
  }

  private static AccountGroup getAccountGroup1() {
    AccountGroup accountGroup = new AccountGroup();
    accountGroup.setOriginReferenceId(PMLE_ID);
    List<UserPaymentMethodsDto> userPaymentMethodsDtoList = new ArrayList<>();
    UserPaymentMethodsDto userPaymentMethodsDto = new UserPaymentMethodsDto();
    userPaymentMethodsDto.setAccountId(WALLET_ID + "_USD");
    userPaymentMethodsDto.setQualifier("PAYSAFE_SINGLE_API");
    userPaymentMethodsDtoList.add(userPaymentMethodsDto);
    accountGroup.setPaymentMethods(userPaymentMethodsDtoList);
    return accountGroup;
  }

  private static AccountGroup getAccountGroup2() {
    AccountGroup accountGroup = new AccountGroup();
    accountGroup.setOriginReferenceId(PMLE_ID);
    List<UserPaymentMethodsDto> userPaymentMethodsDtoList = new ArrayList<>();
    UserPaymentMethodsDto userPaymentMethodsDto = new UserPaymentMethodsDto();
    userPaymentMethodsDto.setAccountId("454545" + "_USD");
    userPaymentMethodsDto.setQualifier("PAYSAFE_SINGLE_API");
    userPaymentMethodsDtoList.add(userPaymentMethodsDto);
    accountGroup.setPaymentMethods(userPaymentMethodsDtoList);
    return accountGroup;
  }

  /**
   * returns roleModuleResource.
   */
  public static RoleModuleListResource getRoleModuleListResource() {
    RoleModuleListResource roleModuleListResource = new RoleModuleListResource();
    roleModuleListResource.setRole(DataConstants.BP_EU_ADMIN);
    ModulesResource modulesResource = new ModulesResource();
    modulesResource.setModuleid("1234");
    modulesResource.setDisplayorder(1);
    modulesResource.setEnabled(true);
    roleModuleListResource.setModules(new ArrayList<>(Arrays.asList(modulesResource)));
    return roleModuleListResource;
  }

  /**
   * sample UserResponseResource List.
   */
  public static List<UserResponseResource> getUserResponseList() {
    UserResponseResource userResponse1 = getUserResponseResource();
    userResponse1.setAccessResources(getAccessResourcesList());
    Map<String, Object> customProperties1 = new HashMap<>();
    customProperties1.put(DataConstants.BUSINESS_UNIT, DataConstants.SKRILL.toLowerCase());
    userResponse1.setCustomProperties(customProperties1);
    List<UserResponseResource> userResponseResourceList = new ArrayList<>();
    userResponseResourceList.add(userResponse1);

    UserResponseResource userResponse2 = getUserResponseResource();
    userResponse2.setAccessResources(getAccessResourcesList());
    Map<String, Object> customProperties2 = new HashMap<>();
    customProperties2.put(DataConstants.BUSINESS_UNIT, DataConstants.BINANCE.toLowerCase());
    userResponse2.setCustomProperties(customProperties2);
    userResponseResourceList.add(userResponse2);

    UserResponseResource userResponse3 = getUserResponseResource();
    userResponse3.setAccessResources(getAccessResourcesList());
    Map<String, Object> customProperties3 = new HashMap<>();
    customProperties3.put(DataConstants.BUSINESS_UNIT, null);
    userResponse3.setCustomProperties(customProperties3);
    userResponseResourceList.add(userResponse3);
    return userResponseResourceList;
  }

  /**
   * sample UserResponseResource List.
   */
  public static OwnerInfo getOwnerInfo() {
    OwnerInfo ownerInfo = new OwnerInfo();
    ownerInfo.setApplication(DataConstants.PORTAL);
    ownerInfo.setOwnerType(DataConstants.PMLE);
    ownerInfo.setOwnerId("1234");
    return ownerInfo;
  }

  /**
   * Sample UserAssignedApplications List.
   */
  public static List<UserAssignedApplications> getUserAssignedApplications() {
    List<UserAssignedApplications> userAssignedApplications = new ArrayList<>();
    UserAssignedApplications userAssignedApplication = new UserAssignedApplications();
    userAssignedApplication.setApplication(DataConstants.PORTAL);
    userAssignedApplications.add(userAssignedApplication);
    return userAssignedApplications;
  }
}
