// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.op.errorhandling.exceptions.OneplatformException;
import com.paysafe.upf.user.provisioning.config.BusinessUnitConfig;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.domain.BulkUsers;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.IncludeParam;
import com.paysafe.upf.user.provisioning.enums.RegionType;
import com.paysafe.upf.user.provisioning.feignclients.IdentityManagementFeignClient;
import com.paysafe.upf.user.provisioning.repository.EmailUserIdView;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.SkrillTellerAccountInfoService;
import com.paysafe.upf.user.provisioning.service.SkrillTellerUserService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.web.rest.assembler.UserAssembler;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.BulkWalletDetailResponse;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserCountDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.IdentityManagementUserResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.RegionUpdateStatusResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.UserCountWalletInfo;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.UserDetailsResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.UserDetailsResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.UserRegionUpdateResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SkrillTellerUserServiceImpl implements SkrillTellerUserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SkrillTellerUserServiceImpl.class);

  @Autowired
  private UsersRepository usersRepository;

  @Autowired
  private IdentityManagementFeignClient identityManagementFeignClient;

  @Autowired
  private UserAssembler userAssembler;

  @Autowired
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Autowired
  private SkrillTellerAccountInfoService skrillTellerAccountInfoService;

  @Autowired
  private SkrillTellerConfig skrillTellerConfig;

  @Autowired
  private UserService userService;

  @Override
  public List<UserResponseResource> getWalletAdminUsers(String walletId) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    String businessUnit = CommonThreadLocal.getAuthLocal().getBusinessUnit();
    List<User> users;
    List<String> linkedBrands = skrillTellerConfig.getLinkedBrands().get(DataConstants.SKRILL.toLowerCase());
    if (businessUnit != null && linkedBrands.contains(businessUnit.toLowerCase())) {
      BusinessUnitConfig businessUnitConfig =
          skrillTellerConfig.getBusinessUnits().get(businessUnit.toLowerCase());
      users = usersRepository.getWalletUsersByAccessRole(walletId, businessUnitConfig.getAdminRole(),
          DataConstants.WALLETS, AccessResourceStatus.ACTIVE.ordinal(), application);
    } else {
      users = usersRepository.getWalletUsers(walletId, AccessGroupType.DEFAULT_ADMIN, DataConstants.WALLETS,
          AccessResourceStatus.ACTIVE, application);
    }
    List<UserResponseResource> userResourceList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(users)) {
      mapUsersToUserResponseResource(users, userResourceList);
    }
    return userResourceList;
  }

  private void mapUsersToUserResponseResource(List<User> users, List<UserResponseResource> userResourceList) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    for (User user : users) {
      ResponseEntity<IdentityManagementUserResource> response =
          identityManagementFeignClient.getUser(user.getLoginName(), application);
      if (response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        userResourceList.add(userAssembler.toUserResponseResource(response.getBody(), user));
      }
    }
  }

  @Override
  public Map<String, UserCountDto> getUsersCountByWalletIdsUsingLinkedBrands(List<String> walletIds) {
    Map<String, UserCountDto> walletUsersCountMap = getUsersCountByWalletIds(walletIds);
    if (walletUsersCountMap.isEmpty()) {
      List<String> linkedBrands =
          skrillTellerConfig.getLinkedBrands().get(CommonThreadLocal.getAuthLocal().getApplication().toLowerCase());
      if (linkedBrands == null) {
        linkedBrands = new ArrayList<>();
      }
      for (String brand : linkedBrands) {
        CommonThreadLocal.getAuthLocal().setBusinessUnit(brand);
        Map<String, UserCountDto> walletUsersCountMapInternal = getUsersCountByWalletIds(walletIds);
        if (!walletUsersCountMapInternal.isEmpty()) {
          return walletUsersCountMapInternal;
        }
      }
    }
    return walletUsersCountMap;
  }

  @Override
  public List<String> getBrands(String application) {
    return skrillTellerConfig.getBrands().get(application.toLowerCase());
  }

  @Override
  public UserDetailsResponseResource getUserEmails(String application, RegionType regionType,
      Integer pageNo, Integer size) {
    Pageable pageable = PageRequest.of(pageNo, size);
    Page<EmailUserIdView>
        usersList = usersRepository.getEmailsByApplicationAndRegion(application, regionType.toString(), pageable);
    List<UserDetailsResource> userDetailsResponse = usersList.getContent().stream()
        .map(user -> new UserDetailsResource(user.getEmail(), user.getUserId())).collect(Collectors.toList());
    return new UserDetailsResponseResource(userDetailsResponse, usersList.getTotalPages());
  }

  @Override
  public UserRegionUpdateResponse updateUsersRegion(String application, String loginName, Integer size) {
    if (StringUtils.isEmpty(application)) {
      throw BadRequestException.builder().details("Application is empty").errorCode(CommonErrorCode.INVALID_FIELD)
          .build();
    }
    List<User> users = new ArrayList<>();
    if (StringUtils.isNotEmpty(loginName)) {
      Optional<User> user = usersRepository.findByLoginNameAndApplication(loginName, application);
      if (!user.isPresent()) {
        throw BadRequestException.builder().details("Requested user not found")
            .errorCode(CommonErrorCode.INVALID_FIELD)
            .build();
      }
      users.add(user.get());
    } else {
      Pageable pageable = PageRequest.of(0, size);
      Page<User> usersPage =
          usersRepository.findByApplicationAndRegionIsNullOrderByLastModifiedDateDesc(application, pageable);
      if (!usersPage.isEmpty()) {
        users = usersPage.getContent();
      }
    }
    if (CollectionUtils.isEmpty(users)) {
      throw BadRequestException.builder().details("No users found to update region")
          .errorCode(CommonErrorCode.INVALID_FIELD).build();
    }

    Map<String, String> emailWalletIdMap = users.stream()
        .filter(user -> user != null && user.getAccessGroupMappingDaos() != null
            && !user.getAccessGroupMappingDaos().isEmpty())
        .collect(Collectors.toMap(User::getLoginName,
            user -> user.getAccessGroupMappingDaos().get(0).getResourceId(), (u1, u2) -> u1));
    Set<String> walletIds = emailWalletIdMap.values().stream().collect(Collectors.toSet());

    Map<String, String> walletIdRegionMap = getWalletIdRegionMapping(walletIds);
    List<RegionUpdateStatusResource> regionUpdateStatusList = new ArrayList<>();
    for (User user : users) {
      if (user.getAccessGroupMappingDaos() != null && !user.getAccessGroupMappingDaos().isEmpty()) {
        String userRegion = walletIdRegionMap.get(emailWalletIdMap.get(user.getLoginName()));
        if (userRegion != null) {
          user.setRegion(userRegion);
          updateRegion(user, regionUpdateStatusList);
        }
      }
    }
    return constructUsersRegionUpdateResponse(regionUpdateStatusList);
  }

  private Map<String, String> getWalletIdRegionMapping(Set<String> walletIds) {
    BulkWalletDetailResponse walletDetailAsyncResponse  =
        skrillTellerAccountInfoService.fetchBulkWalletInfo(walletIds,
            IncludeParam.EWALLET_ACCOUNTS);
    return walletDetailAsyncResponse.getCustomers().stream()
        .filter(walletInfo -> walletInfo.getEwalletAccounts() != null
            && !walletInfo.getEwalletAccounts().isEmpty()
            && !walletInfo.getEwalletAccounts().get(0).getCompany().isEmpty())
        .collect(Collectors.toMap(BasicWalletInfo::getId, walletInfo ->
            walletInfo.getEwalletAccounts().get(0).getCompany().contains("INC") ? "INC" : "NON_INC"));
  }

  @Async("upfAsyncExecutor")
  private void updateRegion(User user,
      List<RegionUpdateStatusResource> regionUnitUpdateStatusList) {
    boolean failed = false;
    String errorReason = null;
    String errorCode = null;
    try {
      usersRepository.save(user);
    } catch (OneplatformException ex) {
      failed = true;
      errorReason = (ex.getDetails() != null) ? Arrays.toString(ex.getDetails()) : "";
      errorCode = String.valueOf(ex.getStatus().value());
    } catch (Exception ex) {
      failed = true;
      errorReason = ex.getMessage();
      LOGGER.error("Error while updating the region for UserName : " + user.getLoginName());
    } finally {
      regionUnitUpdateStatusList
          .add(new RegionUpdateStatusResource(!failed, user.getLoginName(), errorReason, errorCode));
    }
  }

  private UserRegionUpdateResponse constructUsersRegionUpdateResponse(
      List<RegionUpdateStatusResource> regionUpdateStatusList) {
    UserRegionUpdateResponse response = new UserRegionUpdateResponse();
    response.setTotalUsersCount(regionUpdateStatusList.size());
    long succeedUsers = regionUpdateStatusList.stream().filter(e -> e.isRegionUpdated()).count();
    response.setSucceedUsersCount(succeedUsers);
    response.setFailedUsersCount(regionUpdateStatusList.size() - succeedUsers);
    response.setUsersStatus(regionUpdateStatusList);
    return response;
  }

  private Map<String, UserCountDto> getUsersCountByWalletIds(List<String> walletIds) {
    Map<String, BulkUsers> adminUserCountMap = new HashMap<>();
    if (StringUtils.equalsIgnoreCase(CommonThreadLocal.getAuthLocal().getBusinessUnit(), DataConstants.SKRILL)
        || StringUtils.equalsIgnoreCase(CommonThreadLocal.getAuthLocal().getBusinessUnit(), DataConstants.NETELLER)) {
      adminUserCountMap =
          userAccessGroupMapppingRepository
              .getUserCountFilterBy(walletIds, AccessResourceStatus.ACTIVE, AccessGroupType.DEFAULT_ADMIN,
                  DataConstants.WALLETS)
              .stream().collect(Collectors.toMap(BulkUsers::getResourceId, Function.identity()));
    } else {
      List<UserAccessGroupMappingDao> userAccessGroups =
          userAccessGroupMapppingRepository.findByAccessRoleAndResourceIdsTypeAndStatus(walletIds,
              AccessResourceStatus.ACTIVE.ordinal(), skrillTellerConfig.getAdminRole(), DataConstants.WALLETS);
      Map<String, Long> walletAdminUserCount = userAccessGroups.stream()
          .collect(Collectors.groupingBy(UserAccessGroupMappingDao::getResourceId, Collectors.counting()));
      for (Map.Entry<String, Long> entry : walletAdminUserCount.entrySet()) {
        adminUserCountMap.put(entry.getKey(), new BulkUsers(entry.getValue(), entry.getKey()));
      }
    }
    LOGGER.info("Successfully fetched the admin users count");
    final Map<String, BulkUsers> totalUserCountMap = userAccessGroupMapppingRepository
        .getUserCountFilterBy(walletIds, AccessResourceStatus.ACTIVE, DataConstants.WALLETS).stream()
        .collect(Collectors.toMap(BulkUsers::getResourceId, Function.identity()));
    LOGGER.info("Successfully fetched the total users count");
    Map<String, UserCountDto> userCountMap = new HashMap<>();
    Map<String, UserCountWalletInfo> map = skrillTellerAccountInfoService
        .fetchBulkWalletInfo(walletIds.stream().collect(Collectors.toSet()), IncludeParam.EWALLET_ACCOUNTS,
            IncludeParam.BUSINESS_PROFILE)
        .getCustomers().stream().collect(Collectors.toMap(BasicWalletInfo::getId, e -> {
          String companyName = e.getBusinessProfile() == null ? null : e.getBusinessProfile().getCompanyName();
          UserCountWalletInfo userCountWalletInfo = new UserCountWalletInfo(companyName, e.getEwalletAccounts());
          return userCountWalletInfo;
        }));

    for (String walletId : walletIds) {
      final Long adminUsersCount = getCountFromMap(adminUserCountMap, walletId);
      final Long totalUsersCount = getCountFromMap(totalUserCountMap, walletId);
      if (map.get(walletId) == null) {
        continue;
      }
      UserCountDto userCountDto = UserCountDto.builder().adminUsers(adminUsersCount).totalUsers(totalUsersCount)
          .ewalletAccounts(map.get(walletId).getEwalletAccount()).name(map.get(walletId).getName()).build();
      userCountMap.put(walletId, userCountDto);
    }
    return userCountMap;
  }

  private Long getCountFromMap(Map<String, BulkUsers> countMap, String walletId) {
    return Optional.ofNullable(walletId).map(countMap::get).map(BulkUsers::getUsersCount).orElse(0L);
  }

}
