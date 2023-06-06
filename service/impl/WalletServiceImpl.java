// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import static com.paysafe.upf.user.provisioning.enums.AccessGroupType.CUSTOMIZED;
import static com.paysafe.upf.user.provisioning.enums.AccessGroupType.DEFAULT_ADMIN;
import static com.paysafe.upf.user.provisioning.enums.AccessResourceStatus.ACTIVE;
import static com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants.WALLETS;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.config.SkrillTellerConfig;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.IncludeParam;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.MerchantAccountInfoService;
import com.paysafe.upf.user.provisioning.service.SkrillTellerAccountInfoService;
import com.paysafe.upf.user.provisioning.service.WalletService;
import com.paysafe.upf.user.provisioning.utils.LoggingUtil;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.resource.WalletInfoResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BasicWalletInfo;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.BusinessProfile;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.EwalletAccount;
import com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo.Profile;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.CustomerObject;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.GroupedCustomerIdsResource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WalletServiceImpl.class);

  @Autowired
  private SkrillTellerAccountInfoService skrillTellerAccountInfoService;

  @Autowired
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Autowired
  private MerchantAccountInfoService merchantAccountInfoService;

  @Autowired
  @Qualifier("externalRestTemplate")
  private RestTemplate externalRestTemplate;

  @Autowired
  private SkrillTellerConfig skrillTellerConfig;

  @Override
  public List<WalletInfoResource> findLinkedWalletsByLinkedBrands(String application, String walletId) {
    LOGGER.info("Linked wallets call for walletId: {}, brand: {}", LoggingUtil.replaceSpecialChars(walletId),
        LoggingUtil.replaceSpecialChars(CommonThreadLocal.getAuthLocal().getBusinessUnit()));
    return getLinkedWalletsInfo(application, walletId);
  }

  private List<WalletInfoResource> getLinkedWalletsInfo(String application, String walletId) {
    if (StringUtils.isEmpty(application) && CommonThreadLocal.getAuthLocal() != null) {
      application = CommonThreadLocal.getAuthLocal().getApplication();
    }
    vaildateWalletId(walletId, application);
    Set<String> accessibleWalletIds = getAllRelatedWalletIds(application, walletId);
    List<BasicWalletInfo> walletInfoList = skrillTellerAccountInfoService.fetchBulkWalletInfo(accessibleWalletIds,
        IncludeParam.BUSINESS_PROFILE,
        IncludeParam.PROFILE,
        IncludeParam.MERCHANT_SETTINGS,
        IncludeParam.EWALLET_ACCOUNTS)
        .getCustomers();
    final String applicationName = application;
    List<BasicWalletInfo> validWalletList =
        walletInfoList.stream().filter(walletInfo -> !isInvalidWalletId(applicationName, walletInfo))
            .collect(Collectors.toList());
    return formWalletInfoResourceList(validWalletList);
  }

  private void vaildateWalletId(String walletId, String application) {
    List<BasicWalletInfo> walletInfoList =
        merchantAccountInfoService.getWalletProfileAndMerchantSettings(new HashSet<>(Arrays.asList(walletId)));
    BasicWalletInfo walletInfo = !walletInfoList.isEmpty() ? walletInfoList.get(0) : null;

    if (null == walletInfo || null == walletInfo.getId()
        || isInvalidWalletId(application, walletInfo)) {
      throw new BadRequestException.Builder().details("Invalid walletId: " + walletId)
          .errorCode(CommonErrorCode.INVALID_FIELD).build();
    }
  }

  private boolean isInvalidWalletId(String application,
      BasicWalletInfo walletInfo) {
    String lockLevel = walletInfo.getProfile().getLockLevel();
    String brand = walletInfo.getProfile().getBrand();
    String businessUnit = CommonThreadLocal.getAuthLocal().getBusinessUnit();
    return (!brand.equalsIgnoreCase(businessUnit) || !walletInfo.getMerchantSettings().isMerchant()
        || ((application.equalsIgnoreCase(DataConstants.NETELLER)
        && lockLevel.equalsIgnoreCase(DataConstants.NETELLER_LOCK_LEVEL_22))
        || (application.equalsIgnoreCase(DataConstants.SKRILL)
        && (lockLevel.equalsIgnoreCase(DataConstants.SKRILL_LOCK_LEVEL_2)
        || lockLevel.equalsIgnoreCase(DataConstants.SKRILL_LOCK_LEVEL_17)))));

  }

  private List<WalletInfoResource> formWalletInfoResourceList(List<BasicWalletInfo> walletInfoList) {
    List<WalletInfoResource> walletInfoResources = new ArrayList<>();

    for (BasicWalletInfo walletInfo : walletInfoList) {
      final String id = walletInfo.getId();
      final String companyName = Optional.ofNullable(walletInfo)
          .map(BasicWalletInfo::getBusinessProfile)
          .map(BusinessProfile::getCompanyName)
          .orElse("");
      final String brand =
          Optional.ofNullable(walletInfo).map(BasicWalletInfo::getProfile).map(Profile::getBrand).orElse("");
      final long admins;
      final long regularUser;
      if (StringUtils.equalsIgnoreCase(CommonThreadLocal.getAuthLocal().getBusinessUnit(), DataConstants.SKRILL)
          || StringUtils.equalsIgnoreCase(CommonThreadLocal.getAuthLocal().getBusinessUnit(), DataConstants.NETELLER)) {
        admins = userAccessGroupMapppingRepository
            .countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(WALLETS, id, DEFAULT_ADMIN,
                ACTIVE);
        regularUser = userAccessGroupMapppingRepository
            .countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(WALLETS, id, CUSTOMIZED,
                ACTIVE);
      } else {
        admins =
            userAccessGroupMapppingRepository
                .findByAccessRoleAndResourceIdsTypeAndStatus(new ArrayList<>(Arrays.asList(id)),
                    AccessResourceStatus.ACTIVE.ordinal(), skrillTellerConfig.getAdminRole(), DataConstants.WALLETS)
                .size();
        long totalUsers = userAccessGroupMapppingRepository
            .countByResourceTypeAndResourceIdAndAccessGroupTypeAndUserAccessGroupStatus(WALLETS, id, CUSTOMIZED,
                ACTIVE);
        regularUser = totalUsers - admins;
      }
      final List<String> currencies =
          walletInfo.getEwalletAccounts().stream().map(EwalletAccount::getCurrency).distinct()
              .collect(Collectors.toList());
      WalletInfoResource walletInfoResource = WalletInfoResource.builder()
          .id(id)
          .name(companyName)
          .admins(admins)
          .settlementCurrency(currencies)
          .regularUser(regularUser)
          .userWithAccess(admins + regularUser)
          .brand(brand)
          .build();
      walletInfoResources.add(walletInfoResource);
    }
    return walletInfoResources;
  }

  private Set<String> getAllRelatedWalletIds(String application, String walletId) {
    Set<String> accessibleWalletIds = new HashSet<>();
    if (application.equalsIgnoreCase(DataConstants.SKRILL)) {
      Set<String> linkedCustomerIds = skrillTellerAccountInfoService.getMerchantLinkedToWalletId(walletId)
          .getCustomers().stream().map(CustomerObject::getId).collect(Collectors.toSet());
      accessibleWalletIds.addAll(linkedCustomerIds);
    } else if (application.equalsIgnoreCase(DataConstants.NETELLER)) {
      GroupedCustomerIdsResource merchantGroupByCustomerId =
          skrillTellerAccountInfoService.getMerchantGroupByWalletId(walletId);
      if (merchantGroupByCustomerId != null) {
        accessibleWalletIds.addAll(merchantGroupByCustomerId.getCustomerIds());
      }
    }
    if (accessibleWalletIds.isEmpty()) {
      accessibleWalletIds.add(walletId);
    }
    return accessibleWalletIds;
  }

}
