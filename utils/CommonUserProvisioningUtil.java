// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import com.paysafe.upf.user.provisioning.config.OktaAppConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.config.UserConfig;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.Action;
import com.paysafe.upf.user.provisioning.feignclients.AccessGroupFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.feignclients.PegasusFeignClient;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.repository.WalletPermissionRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.AuditService;
import com.paysafe.upf.user.provisioning.service.FeatureFlagService;
import com.paysafe.upf.user.provisioning.service.MerchantAccountInfoService;
import com.paysafe.upf.user.provisioning.service.SkrillTellerAccountInfoService;
import com.paysafe.upf.user.provisioning.service.UserHandlerService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.dto.AppUserConfigDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.ResourceUsersValidationDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccessGroupsListRequestResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UpdateUserAccessResources;
import com.paysafe.upf.user.provisioning.web.rest.resource.WalletUserCountResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommonUserProvisioningUtil {

  protected static final Logger LOGGER = LoggerFactory.getLogger(UserProvisioningUtils.class);

  protected static final String CUSTOM_ROLE_PREFIX = "CUSTOM_";

  protected ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  protected UserService userService;

  @Autowired
  protected UserConfig userConfig;

  @Autowired
  protected AccessGroupFeignClient accessGroupFeignClient;

  @Autowired
  protected OktaAppConfig oktaAppConfig;

  @Autowired
  protected IdentityManagementFeignClient identityManagementFeignClient;

  @Autowired
  protected UsersRepository usersRepository;

  @Autowired
  protected UserFilterUtil userFilterUtil;

  @Autowired
  protected MerchantAccountInfoService merchantAccountInfoService;

  @Autowired
  protected PegasusFeignClient pegasusFeignClient;

  @Autowired
  protected FeatureFlagService featureFlagService;

  @Autowired
  protected WalletPermissionRepository walletPermissionRepository;

  @Autowired
  protected UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Autowired
  protected UserAssembler userAssembler;

  @Autowired
  protected AuditService auditService;

  @Autowired
  protected UserHandlerService userHandlerService;

  @Autowired
  protected AuditUserEventUtil auditUserEventUtil;

  @Autowired
  protected SkrillTellerConfig skrillTellerConfig;

  @Autowired
  protected SkrillTellerAccountInfoService skrillTellerAccountInfoService;

  protected static final String NETELLER_APPLICATION = "NETELLER";

  /**
   * Generates random role string.
   *
   * @return String randomRoleString
   */
  public String generateRandomRoleString() {
    String lowerCaseLetters =
        RandomStringUtils.random(6, 0, 23, true, false, "abcdefghijklmnopqrstuvwxyz".toCharArray());
    String upperCaseLetters = RandomStringUtils.random(6, 0, 23, true, false, "ABCDEFGHIJKLMNOPQRUVWXYZ".toCharArray());
    String numbers = RandomStringUtils.random(5, 0, 9, false, true, "0123456789".toCharArray());
    return CUSTOM_ROLE_PREFIX + lowerCaseLetters + upperCaseLetters + numbers;
  }

  /**
   * Checks if usercount exceeds the specified limit and returns boolean.
   */
  public ResourceUsersValidationDto verifyUserCountforResource(String resourceId, String resourceName,
      String application) {
    WalletUserCountResource walletUserCountDto = userHandlerService.getUserCount(resourceName, resourceId);
    AppUserConfigDto appUserConfigDto = userConfig.getAppUserLimit().get(application);
    ResourceUsersValidationDto walletUsersValidationDto = new ResourceUsersValidationDto();
    if (appUserConfigDto == null) {
      LOGGER.info("App users limit is not found for application: {}", application);
      walletUsersValidationDto.setCanAddAdminUsers(true);
      walletUsersValidationDto.setCanAddUsers(true);
    } else {
      if (walletUserCountDto.getAdminCount() < appUserConfigDto.getAdminCount()) {
        walletUsersValidationDto.setCanAddAdminUsers(true);
      }
      if (walletUserCountDto.getUserCount() < appUserConfigDto.getUserCount()) {
        walletUsersValidationDto.setCanAddUsers(true);
      }
    }
    return walletUsersValidationDto;
  }

  /**
   * In the update user request , the accessResource is a new list that needs to be assigned to user(It is not the
   * delta). To achieve this we have to check whether the updated user has access to any other current admin
   * wallets(resources) alreay and remove the access.
   */
  public void validateAccessResources(UserUpdationDto userUpdationDto,
      List<AccessGroupResponseResource> fetchedAccessGroups) {
    List<String> adminAcessGroupIds = userService.getUserAccessGroupIds(CommonThreadLocal.getAuthLocal().getUserName(),
        CommonThreadLocal.getAuthLocal().getApplication());
    List<AccessGroupResponseResource> adminAccessGroups = null;
    if (CollectionUtils.isEmpty(adminAcessGroupIds)) {
      adminAccessGroups = new ArrayList<>();
    } else {
      adminAccessGroups =
          accessGroupFeignClient.getAccessGroupsFromInputList(new AccessGroupsListRequestResource(adminAcessGroupIds));
    }
    for (AccessGroupResponseResource fetchedAccessGroup : fetchedAccessGroups) {
      boolean isValidAccessGroup = checkAccessGroup(fetchedAccessGroup, userUpdationDto, adminAccessGroups);
      if (!isValidAccessGroup) {
        UpdateUserAccessResources accessResource = getAccessResource(fetchedAccessGroup.getCode(), Action.HARD_DELETE);
        userUpdationDto.getAccessResources().add(accessResource);
      }
    }
  }

  /**
   * this methods returns the UpdateUserAccessResources based on the input parameters.
   */
  public UpdateUserAccessResources getAccessResource(String accessGroupId, Action action) {
    UpdateUserAccessResources accessResource = new UpdateUserAccessResources();
    accessResource.setAccessGroupId(accessGroupId);
    accessResource.setAction(action);
    return accessResource;
  }

  protected boolean checkAccessGroupRelatedToCurrentAdmin(AccessGroupResponseResource fetchedAccessGroup,
      List<AccessGroupResponseResource> adminAccessGroups) {
    boolean isRelated = false;
    for (AccessGroupResponseResource adminAccessGroup : adminAccessGroups) {
      if (adminAccessGroup.getType().equals(AccessGroupType.DEFAULT_ADMIN)
          && StringUtils.equals(fetchedAccessGroup.getMerchantId(), adminAccessGroup.getMerchantId())
          && StringUtils.equals(fetchedAccessGroup.getMerchantType(), adminAccessGroup.getMerchantType())) {
        isRelated = true;
        break;
      }
    }
    return isRelated;
  }

  protected boolean checkAccessGroup(
        AccessGroupResponseResource fetchedAccessGroup, UserUpdationDto updateUserRequestDto,
      List<AccessGroupResponseResource> adminAccessGroups) {
    if (checkAccessGroupPresentInRequest(fetchedAccessGroup, updateUserRequestDto)) {
      return true;
    } else if (checkAccessGroupRelatedToCurrentAdmin(fetchedAccessGroup, adminAccessGroups)) {
      return false;
    }
    return true;
  }

  protected boolean checkAccessGroupPresentInRequest(AccessGroupResponseResource fetchedAccessGroup,
      UserUpdationDto updateUserRequestDto) {
    boolean isPresent = false;
    for (UpdateUserAccessResources accessReource : updateUserRequestDto.getAccessResources()) {
      if (StringUtils.equals(fetchedAccessGroup.getMerchantId(), accessReource.getId())
          && StringUtils.equals(fetchedAccessGroup.getMerchantType(), accessReource.getType())) {
        isPresent = true;
        break;
      }
    }
    return isPresent;
  }
}
