// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service.impl;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.config.UserConfig;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.enums.UserStatusFilter;
import com.paysafe.upf.user.provisioning.feignclients.MasterMerchantFeignClient;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;
import com.paysafe.upf.user.provisioning.service.MasterMerchantService;
import com.paysafe.upf.user.provisioning.service.UserService;
import com.paysafe.upf.user.provisioning.utils.UserFilterUtil;
import com.paysafe.upf.user.provisioning.utils.UserManagmentUtil;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccountGroupMerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.FetchEmailResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.FilterParams;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchAfterRequest;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.ParentMerchantLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.PartnerLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.PaymentAccount;
import com.paysafe.upf.user.provisioning.web.rest.resource.PaymentAccountResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.PaymentAccountResponse.ContactsDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount.BusinessDetails;
import com.paysafe.upf.user.provisioning.web.rest.resource.SearchParams;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MasterMerchantServiceImpl implements MasterMerchantService {

  private static final Logger logger = LoggerFactory.getLogger(MasterMerchantServiceImpl.class);

  @Autowired
  private UserService userService;

  @Autowired
  private MasterMerchantFeignClient masterMerchantFeignClient;

  @Autowired
  private UserConfig userConfig;

  @Autowired
  private UserFilterUtil userFilterUtil;

  @Autowired
  private UsersRepository usersRepository;

  private static final String RESOURCE_TYPE_PMLE = "PMLE";
  private static final String RESOURCE_TYPE_MLE = "MLE";
  private static final String RESOURCE_TYPE_PAYMENT_ACCOUNT = "PAYMENT_ACCOUNT";
  private static final String RESOURCE_TYPE_PARTNER = "PARTNER";
  private static final String RESOURCE_TYPE_BUSINESS_RELATION_NAME = "BUSINESS_RELATION_NAME";
  private static final String SORT_FIELD = "paymentAccountId";
  private static final String SORT_ORDER = "ASC";
  private List<FetchEmailResponseResource> fetchEmailResponseResource = new ArrayList<>();
  private static final List<String> PARTNER_HIERARCHY_RESPONSE_FIELDS = new ArrayList<>(Arrays.asList(
      "paymentAccounts.processingAccounts.businessDetails.onboardingInformation.partnerId",
      "paymentAccounts.processingAccounts.businessDetails.businessRelationName",
      "paymentAccounts.processingAccounts.businessDetails.onboardingInformation.partnerName",
      "paymentAccounts.processingAccounts.pmleId", "paymentAccounts.processingAccounts.pmleName",
      "paymentAccounts.processingAccounts.businessDetails.legalEntity.id",
      "paymentAccounts.processingAccounts.businessDetails.legalEntity.description", "paymentAccounts.id",
      "paymentAccounts.currency", "paymentAccounts.processingAccounts.sourceAuthority.referenceId",
      "paymentAccounts.processingAccounts.businessDetails.tags",
      "paymentAccounts.processingAccounts.businessDetails.tradeName", "paymentAccounts.processingAccounts.status.code",
      "paymentAccounts.processingAccounts.currency", "paymentAccounts.processingAccounts.type"));
  private static final List<String> BUSINESS_RELATION_NAME_HIERARCHY_RESPONSE_FIELDS = new ArrayList<>(
      Arrays.asList("paymentAccounts.processingAccounts.businessDetails.businessRelationName",
           "paymentAccounts.processingAccounts.pmleId", "paymentAccounts.processingAccounts.pmleName",
           "paymentAccounts.processingAccounts.businessDetails.legalEntity.id",
           "paymentAccounts.processingAccounts.businessDetails.legalEntity.description", "paymentAccounts.id",
           "paymentAccounts.currency", "paymentAccounts.processingAccounts.sourceAuthority.referenceId",
           "paymentAccounts.processingAccounts.businessDetails.tags",
           "paymentAccounts.processingAccounts.businessDetails.tradeName",
           "paymentAccounts.processingAccounts.status.code", "paymentAccounts.processingAccounts.currency",
           "paymentAccounts.processingAccounts.type"));
  private static final List<String> MERCHANT_EMAILS_RESPONSE_FIELDS =
      new ArrayList<>(Arrays.asList("paymentAccounts.contacts"));
  private static final List<String> PMLE_DETAILS_BY_MLE_RESPONSE_FIELDS =
      new ArrayList<>(Arrays.asList("paymentAccounts.processingAccounts.pmleId"));
  private static final List<String> ORIGIN = new ArrayList<>(Arrays.asList("NETBANX"));
  private static final List<String> ACCOUNT_GROUP_RESPONSE_FIELDS = new ArrayList<>(Arrays.asList(
      "paymentAccounts.id", "paymentAccounts.currency",
      "paymentAccounts.processingAccounts.sourceAuthority.referenceId",
      "paymentAccounts.processingAccounts.businessDetails.accountGroups"));
  private static final Integer MAX_OFFSET_LIMIT = 10000;
  private static final Integer OFFSET_LIMIT = 5000;
  private static final List<String> PMLE_ACCOUNT_IDS_RESPONSE_FIELDS =
      new ArrayList<>(Arrays.asList("paymentAccounts.processingAccounts.pmleId"));
  private static final String OR_OPERATOR = "OR";

  @Override
  public List<FetchEmailResponseResource> getEmail(String userName, String application) {
    UsersListResponseResource usersListResponseResource = userService.getUsers(userName, null, null, null, null, null,
        application, null, null, new MutableBoolean(false));
    if (usersListResponseResource.getUsers().get(0).getAccessResources().isEmpty()) {
      logger.error("Couldn't get email, received no access resources");
      throw new BadRequestException.Builder().detail("Access resource not present for a given user").build();
    }
    getMerchantSearchRequestForEmail(usersListResponseResource.getUsers().get(0).getAccessResources());
    return fetchEmailResponseResource;
  }

  @Override
  public Set<ParentMerchantLegalEntity> getPmleList(String ownerType, String ownerId, boolean isContainsSearch) {
    PartnerLegalEntity partner = getPartnerHierarchy(ownerType, ownerId, isContainsSearch);
    return partner.getPmles();
  }

  @Override
  public PartnerLegalEntity getPartnerHierarchy(String ownerType, String ownerId, boolean isContainsSearch) {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    request.setResponseFields(PARTNER_HIERARCHY_RESPONSE_FIELDS);
    if (isContainsSearch) {
      setIdOrNameSearchParamsByResourceType(ownerType, ownerId, request);
    } else {
      setFilterParamsByResourceType(ownerType, ownerId, request);
    }
    setSearchAfterApiCommonFields(request);
    logger.info("making find merchants initial call: {}", request);
    MerchantSearchResponse merchants = masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
    logger.info("found {} number of merchants ", merchants.getTotalCount());
    Map<String, ParentMerchantLegalEntity> resultPmleMap = new HashMap<>();
    preparePmleMap(ownerType, merchants, resultPmleMap);
    List<MerchantResponse> merchantsList = merchants.getMerchants();
    int totalFetchedRecords = merchantsList.size();
    while (totalFetchedRecords < merchants.getTotalCount()) {
      request.setOffset(null);
      request
          .setSearchAfter(merchantsList.get(totalFetchedRecords - 1).getPaymentAccounts().get(0).getId());
      logger.info("making searchAfter call to find merchants: {}", request);
      merchants = masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
      preparePmleMap(ownerType, merchants, resultPmleMap);
      merchantsList.addAll(merchants.getMerchants());
      totalFetchedRecords = totalFetchedRecords + merchants.getMerchants().size();
    }

    fetchPmleDetailsWhenMleNotMappedToPmle(ownerType, resultPmleMap);

    PartnerLegalEntity partner = getPartnerDetails(merchants, ownerType);
    partner.setOwnerId(ownerId);
    partner.setOwnerType(ownerType);
    partner.setPmles(resultPmleMap.values().parallelStream().collect(Collectors.toSet()));
    logger.info("Successfully prepared the legalentity hierarchy");
    return partner;
  }

  private void fetchPmleDetailsWhenMleNotMappedToPmle(String ownerType,
      Map<String, ParentMerchantLegalEntity> resultPmleMap) {
    List<String> keyList = Arrays.asList(null, StringUtils.EMPTY);
    keyList.forEach(key -> {
      ParentMerchantLegalEntity pmle = resultPmleMap.get(key);
      if (pmle != null) {
        logger.info("{} mle records found with pmle mapping {}", pmle.getMles().size(), key);
        List<MerchantLegalEntity> mappingFoundMleList = new ArrayList<>();
        pmle.getMles().forEach(mle -> {
          MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
          request.setResponseFields(PMLE_DETAILS_BY_MLE_RESPONSE_FIELDS);
          setFilterParamsByResourceType(RESOURCE_TYPE_MLE, mle.getResourceId(), request);
          MerchantSearchResponse merchants = getMerchantsUsingSearchAfter(request);
          logger.info("Fetched master merchant records with mle resourceId: {}", mle.getResourceId());
          Optional<MerchantResponse> pmleMappedMerchant = checkPmleMappingForMle(merchants);
          if (pmleMappedMerchant.isPresent()) {
            ProcessingAccount processingAccount =
                pmleMappedMerchant.get().getPaymentAccounts().get(0).getProcessingAccounts().get(0);
            addMleToPmleMap(ownerType, resultPmleMap, processingAccount, mle);
            logger.info("Mapped pmle {} to mle {}", processingAccount.getPmleId(), mle.getResourceId());
            mappingFoundMleList.add(mle);
          }
        });
        pmle.getMles().removeAll(mappingFoundMleList);
        if (pmle.getMles().isEmpty()) {
          resultPmleMap.remove(key);
        }
      }
    });
  }

  private void addMleToPmleMap(String ownerType, Map<String, ParentMerchantLegalEntity> resultPmleMap,
      ProcessingAccount processingAccount, MerchantLegalEntity mle) {
    addPmleToMap(ownerType, resultPmleMap, processingAccount);
    resultPmleMap.get(processingAccount.getPmleId()).getMles().add(mle);
  }

  private Optional<MerchantResponse> checkPmleMappingForMle(MerchantSearchResponse merchants) {
    return merchants.getMerchants().stream()
        .filter(merchant -> (merchant.getPaymentAccounts() != null)
            && StringUtils.isNotBlank(merchant.getPaymentAccounts().get(0).getProcessingAccounts().get(0).getPmleId()))
        .findFirst();
  }

  private PartnerLegalEntity getPartnerDetails(MerchantSearchResponse merchants, String ownerType) {
    PartnerLegalEntity partner = new PartnerLegalEntity();
    partner.setResourceType(RESOURCE_TYPE_PARTNER);
    if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_PARTNER) && merchants.getTotalCount() > 0) {
      ProcessingAccount.OnboardingInformation onboardingInformation = merchants.getMerchants().get(0)
          .getPaymentAccounts().get(0).getProcessingAccounts().get(0).getBusinessDetails().getOnboardingInformation();
      partner.setResourceId(onboardingInformation.getPartnerId());
      partner.setName(onboardingInformation.getPartnerName());
    } else if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_BUSINESS_RELATION_NAME) && merchants.getTotalCount() > 0) {
      BusinessDetails businessDetails = merchants.getMerchants().get(0).getPaymentAccounts().get(0)
          .getProcessingAccounts().get(0).getBusinessDetails();
      partner.setResourceId(businessDetails.getBusinessRelationName());
      partner.setName(businessDetails.getBusinessRelationName());
      partner.setResourceType(RESOURCE_TYPE_BUSINESS_RELATION_NAME);
    } else {
      partner.setResourceId(StringUtils.EMPTY);
      partner.setName(StringUtils.EMPTY);
    }
    return partner;
  }

  private void getMerchantSearchRequestForEmail(List<AccessResources> resource) {
    List<String> pmleId = new ArrayList<>();
    List<String> paymentAccountId = new ArrayList<>();
    List<String> merchantLegalEntityIds = new ArrayList<>();
    for (AccessResources res : resource) {
      if (res.getType().equals(RESOURCE_TYPE_PMLE)) {
        pmleId.add(res.getId());
      }
      if (res.getType().equals(RESOURCE_TYPE_MLE)) {
        merchantLegalEntityIds.add(res.getId());
      }
      if (res.getType().equals(RESOURCE_TYPE_PAYMENT_ACCOUNT)) {
        paymentAccountId.add(res.getId());
      }
    }
    masterMerchantCall(pmleId, paymentAccountId, merchantLegalEntityIds);
  }

  private void masterMerchantCall(List<String> pmleId, List<String> paymentAccountId,
      List<String> merchantLegalEntityIds) {
    if (!pmleId.isEmpty()) {
      MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
      request.setResponseFields(MERCHANT_EMAILS_RESPONSE_FIELDS);
      request.setFilterParams(FilterParams.builder().pmleId(pmleId).origin(ORIGIN).build());
      MerchantSearchResponse response = getMerchantsUsingSearchAfter(request);
      toResponse(response);
    }
    if (!paymentAccountId.isEmpty()) {
      MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
      request.setResponseFields(MERCHANT_EMAILS_RESPONSE_FIELDS);
      request.setFilterParams(FilterParams.builder().paymentAccountId(paymentAccountId).origin(ORIGIN).build());
      MerchantSearchResponse response = getMerchantsUsingSearchAfter(request);
      toResponse(response);
    }
    if (!merchantLegalEntityIds.isEmpty()) {
      MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
      request.setResponseFields(MERCHANT_EMAILS_RESPONSE_FIELDS);
      request
          .setFilterParams(FilterParams.builder().merchantLegalEntityId(merchantLegalEntityIds).origin(ORIGIN).build());
      MerchantSearchResponse response = getMerchantsUsingSearchAfter(request);
      toResponse(response);
    }

  }

  private List<FetchEmailResponseResource> toResponse(MerchantSearchResponse response) {
    FetchEmailResponseResource resource = new FetchEmailResponseResource();
    for (MerchantResponse merchants : response.getMerchants()) {
      for (PaymentAccountResponse paymentAccounts : merchants.getPaymentAccounts()) {
        for (ContactsDto contacts : paymentAccounts.getContacts()) {
          resource.setEmail(contacts.getEmail());
          resource.setFirstName(contacts.getFirstName());
          resource.setMiddleName(contacts.getMiddleName());
          resource.setLastName(contacts.getLastName());
          fetchEmailResponseResource.add(resource);
        }
      }
    }
    return fetchEmailResponseResource;

  }

  /**
   * This method sets the Master merchant request search parameter based on ownerType value.
   */
  @Override
  public void setFilterParamsByResourceType(String ownerType, String ownerId, MerchantSearchAfterRequest request) {
    if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_PARTNER)) {
      request.setFilterParams(FilterParams.builder().partnerId(Arrays.asList(ownerId)).origin(ORIGIN).build());
    } else if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_BUSINESS_RELATION_NAME)) {
      request
          .setFilterParams(FilterParams.builder().businessRelationName(Arrays.asList(ownerId)).origin(ORIGIN).build());
    } else if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_PMLE)) {
      request.setFilterParams(FilterParams.builder().pmleId(Arrays.asList(ownerId)).origin(ORIGIN).build());
    } else if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_MLE)) {
      request
          .setFilterParams(FilterParams.builder().merchantLegalEntityId(Arrays.asList(ownerId)).origin(ORIGIN).build());
    } else if (ownerType.equalsIgnoreCase(DataConstants.ACCOUNT_GROUP)) {
      request.setFilterParams(FilterParams.builder().accountGroups(Arrays.asList(ownerId)).origin(ORIGIN).build());
    } else if (ownerType.equalsIgnoreCase(DataConstants.FMA)) {
      request.setFilterParams(FilterParams.builder().merchantId(Arrays.asList(ownerId)).origin(ORIGIN).build());
    }
  }

  private void preparePmleMap(String ownerType, MerchantSearchResponse merchants,
      Map<String, ParentMerchantLegalEntity> resultPmleMap) {
    Map<String, Set<MerchantLegalEntity>> mleMap = new HashMap<>();
    Map<String, Set<PaymentAccount>> paymentAccountMap = new HashMap<>();
    for (MerchantResponse merchant : merchants.getMerchants()) {
      for (PaymentAccountResponse paymentAccount : merchant.getPaymentAccounts()) {
        for (ProcessingAccount processingAccount : paymentAccount.getProcessingAccounts()) {
          addPmleToMap(ownerType, resultPmleMap, processingAccount);
          String mleId = addMleToMap(mleMap, processingAccount);
          PaymentAccount account = getPaymentAccount(paymentAccount, processingAccount);
          addPaymentAccountToMap(paymentAccountMap, mleId, account);
        }
      }
    }

    mapPaymentAccountsToMle(mleMap, paymentAccountMap);
    mapMlesToPmle(resultPmleMap, mleMap);
  }

  private void mapMlesToPmle(Map<String, ParentMerchantLegalEntity> pmleMap,
      Map<String, Set<MerchantLegalEntity>> mleMap) {

    mleMap.keySet().forEach(pmleId -> pmleMap.get(pmleId).getMles().addAll(mleMap.get(pmleId)));
  }

  private ParentMerchantLegalEntity getEmptyPmle() {
    ParentMerchantLegalEntity pmle = new ParentMerchantLegalEntity();
    pmle.setResourceType(RESOURCE_TYPE_PMLE);
    pmle.setResourceId(StringUtils.EMPTY);
    pmle.setName(StringUtils.EMPTY);
    return pmle;
  }

  private void mapPaymentAccountsToMle(Map<String, Set<MerchantLegalEntity>> mleMap,
                                       Map<String, Set<PaymentAccount>> paymentAccountMap) {
    mleMap.values().parallelStream().flatMap(Collection::stream).forEach(mle -> {
      Set<PaymentAccount> paymentAccounts = paymentAccountMap.get(mle.getResourceId());
      if (paymentAccounts != null) {
        mle.setPaymentAccounts(paymentAccounts);
      }
    });
  }

  private PaymentAccount getPaymentAccount(PaymentAccountResponse paymentAccount, ProcessingAccount processingAccount) {
    PaymentAccount account = new PaymentAccount();
    account.setResourceType(RESOURCE_TYPE_PAYMENT_ACCOUNT);
    account.setResourceId(paymentAccount.getId());
    account.setFmaId(processingAccount.getSourceAuthority().getReferenceId());
    account.setSettlementCurrency(paymentAccount.getCurrency());
    account.setBusinessDetails(getBusinessDetails(processingAccount));
    if (processingAccount.getStatus() != null) {
      account.setFmaStatus(processingAccount.getStatus().getCode());
    }
    account.setFmaCurrency(processingAccount.getCurrency());
    account.setFmaType(processingAccount.getType());
    return account;
  }

  private ProcessingAccount.BusinessDetails getBusinessDetails(ProcessingAccount processingAccount) {
    ProcessingAccount.BusinessDetails businessDetails = new ProcessingAccount.BusinessDetails();
    if (processingAccount.getBusinessDetails() != null) {
      businessDetails.setTags(Objects.nonNull(processingAccount.getBusinessDetails().getTags())
          ? processingAccount.getBusinessDetails().getTags()
          : new ArrayList<>());
      businessDetails.setTradeName(processingAccount.getBusinessDetails().getTradeName());
    }
    return businessDetails;
  }

  private void addPaymentAccountToMap(Map<String, Set<PaymentAccount>> paymentAccountMap, String mleId,
      PaymentAccount account) {
    if (paymentAccountMap.containsKey(mleId)) {
      paymentAccountMap.get(mleId).add(account);
    } else {
      paymentAccountMap.put(mleId, new HashSet<>(Arrays.asList(account)));
    }
  }

  private String addMleToMap(Map<String, Set<MerchantLegalEntity>> mleMap, ProcessingAccount processingAccount) {
    String mleId = null;
    if (processingAccount.getBusinessDetails() != null) {
      ProcessingAccount.LegalEntity legalEntity = processingAccount.getBusinessDetails().getLegalEntity();
      String mleName;
      if (legalEntity != null) {
        mleId = legalEntity.getId();
        mleName = legalEntity.getDescription();
        MerchantLegalEntity mle = getMerchantLegalEntity(mleId, mleName);
        if (mleMap.containsKey(processingAccount.getPmleId())) {
          mleMap.get(processingAccount.getPmleId()).add(mle);
        } else {
          mleMap.put(processingAccount.getPmleId(), new HashSet<>(Arrays.asList(mle)));
        }
      }
    }
    return mleId;
  }

  private MerchantLegalEntity getMerchantLegalEntity(String mleId, String mleName) {
    MerchantLegalEntity mle = new MerchantLegalEntity();
    mle.setResourceType(RESOURCE_TYPE_MLE);
    mle.setResourceId(mleId);
    mle.setName(mleName);
    return mle;
  }

  private void addPmleToMap(String ownerType, Map<String, ParentMerchantLegalEntity> pmleMap,
      ProcessingAccount processingAccount) {
    if (!pmleMap.containsKey(processingAccount.getPmleId())) {
      ParentMerchantLegalEntity pmle = getEmptyPmle();
      if (!ownerType.equalsIgnoreCase(RESOURCE_TYPE_MLE)) {
        pmle.setResourceId(processingAccount.getPmleId());
        pmle.setName(processingAccount.getPmleName());
      }
      pmleMap.put(processingAccount.getPmleId(), pmle);
    }
  }

  private MerchantSearchResponse createMerchantSearchAfterRequestObj(List<String> accountGroupIds,
      List<String> paymentAccountIds,
      int page, int pageSize) {
    FilterParams filterParams = new FilterParams();
    filterParams.setAccountGroups(accountGroupIds);
    filterParams.setOrigin(ORIGIN);
    checkAndAddPaymentAccountIdsToMmRequest(paymentAccountIds, filterParams);
    MerchantSearchAfterRequest merchantSearchAfterRequest = new MerchantSearchAfterRequest();
    merchantSearchAfterRequest.setFilterParams(filterParams);
    merchantSearchAfterRequest.setSearchAfter(null);
    merchantSearchAfterRequest.setResponseFields(ACCOUNT_GROUP_RESPONSE_FIELDS);
    return calculateOffSetAndLimit(page, pageSize, merchantSearchAfterRequest);
  }

  private void checkAndAddPaymentAccountIdsToMmRequest(List<String> paymentAccountIds, FilterParams filterParams) {
    if (CollectionUtils.isNotEmpty(paymentAccountIds)) {
      paymentAccountIds = paymentAccountIds.stream().map(String::toLowerCase).filter(id -> !"null".equals(id.trim()))
          .collect(Collectors.toList());
      if (!paymentAccountIds.isEmpty()) {
        filterParams.setPaymentAccountId(paymentAccountIds);
      }
    }
  }

  private MerchantSearchResponse calculateOffSetAndLimit(int page, int pageSize, MerchantSearchAfterRequest request) {
    int offset = page * pageSize;
    request.setSortField(SORT_FIELD);
    request.setSortOrder(SORT_ORDER);
    if (offset + pageSize > MAX_OFFSET_LIMIT) {
      request.setLimit(10);
      request.setOffset(9990);
      int currentOffset = offset;
      String searchAfterId;
      MerchantSearchResponse response = masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
      if (response.getMerchants().isEmpty()) {
        return response;
      }
      searchAfterId =
          response.getMerchants().get(response.getMerchants().size() - 1).getPaymentAccounts().get(0).getId();
      currentOffset -= MAX_OFFSET_LIMIT;
      while (currentOffset != 0) {
        if (currentOffset > OFFSET_LIMIT) {
          request.setLimit(OFFSET_LIMIT);
          currentOffset -= OFFSET_LIMIT;
        } else {
          request.setLimit(currentOffset);
          currentOffset -= currentOffset;
        }
        request.setSearchAfter(searchAfterId);
        request.setOffset(null);
        response = masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
        if (response.getMerchants().isEmpty()) {
          return response;
        }
        searchAfterId =
            response.getMerchants().get(response.getMerchants().size() - 1).getPaymentAccounts().get(0).getId();
      }
      request.setLimit(pageSize);
      request.setSearchAfter(searchAfterId);
      return masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
    } else {
      request.setLimit(pageSize);
      request.setOffset(offset);
      return masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
    }

  }

  /**
   * getMerchants api call with search-after implementation.
   */
  @Override
  public MerchantSearchResponse getMerchantsUsingSearchAfter(MerchantSearchAfterRequest request) {
    setSearchAfterApiCommonFields(request);
    logger.info("making find merchants initial call: {}", request);
    MerchantSearchResponse initialResponse = masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
    logger.info("found {} number of merchants ", initialResponse.getTotalCount());
    List<MerchantResponse> merchants = initialResponse.getMerchants();
    int totalFetchedRecords = merchants.size();

    while (totalFetchedRecords < initialResponse.getTotalCount()) {
      request.setOffset(null);
      request.setSearchAfter(merchants.get(totalFetchedRecords - 1).getPaymentAccounts().get(0).getId());
      logger.info("making searchAfter call to find merchants: {}", request);
      MerchantSearchResponse response = masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
      merchants.addAll(response.getMerchants());
      totalFetchedRecords = totalFetchedRecords + response.getMerchants().size();
    }
    initialResponse.setMerchants(merchants);
    logger.info("completed getMerchant searchAfter call, merchant list size {}",
        CollectionUtils.isEmpty(initialResponse.getMerchants()) ? 0 : initialResponse.getMerchants().size());
    return initialResponse;
  }

  /**
   * getMerchants api call without search-after implementation.
   */
  @Override
  public MerchantSearchResponse getMerchantsSingleRecord(MerchantSearchAfterRequest request) {
    logger.info("making find merchants singleRecord call: {}", request);
    setSearchAfterApiCommonFields(request);
    request.setLimit(1);
    return masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
  }

  /**
   * Sets the masterMerchant searchAfter request common fields.
   */
  @Override
  public MerchantSearchAfterRequest setSearchAfterApiCommonFields(MerchantSearchAfterRequest request) {
    request.setOffset(0);
    request.setLimit(userConfig.getMasterMerchantSearchAfterFetchLimit().intValue());
    request.setSortField(SORT_FIELD);
    request.setSortOrder(SORT_ORDER);
    return request;
  }

  @Override
  public AccountGroupMerchantSearchResponse getMerchantForAccountGroups(List<String> accountGroupIds,
      List<String> paymentAccountIds, int page, int pageSize) {
    MerchantSearchResponse merchantSearchResponse =
        createMerchantSearchAfterRequestObj(accountGroupIds, paymentAccountIds, page, pageSize);
    AccountGroupMerchantSearchResponse accountGroupMerchantResponse = new AccountGroupMerchantSearchResponse();
    BeanUtils.copyProperties(merchantSearchResponse, accountGroupMerchantResponse);
    accountGroupMerchantResponse.setAccountGroupId(filterAccountGroup(accountGroupIds.get(0), merchantSearchResponse));
    return accountGroupMerchantResponse;
  }

  private String filterAccountGroup(String requestedAccountGroup, MerchantSearchResponse merchantSearchResponse) {
    List<String> accountGroups = merchantSearchResponse.getMerchants().get(0).getPaymentAccounts().get(0)
        .getProcessingAccounts().get(0).getBusinessDetails().getAccountGroups();
    for (String accountGroup : accountGroups) {
      if (StringUtils.equalsIgnoreCase(accountGroup, requestedAccountGroup)) {
        return accountGroup;
      }
    }
    return null;
  }

  @Override
  public Set<String> getAllPmleIds(String resourceType, String resourceId) {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    setSearchAfterApiCommonFields(request);
    setFilterParamsByResourceType(resourceType, resourceId, request);
    request.setResponseFields(PMLE_ACCOUNT_IDS_RESPONSE_FIELDS);
    MerchantSearchResponse merchantSearchResponse = getMerchantsUsingSearchAfter(request);
    return merchantSearchResponse.getMerchants().stream()
        .filter(e -> (e != null && CollectionUtils.isNotEmpty(e.getPaymentAccounts())))
        .flatMap(e -> e.getPaymentAccounts().stream()).collect(Collectors.toList()).stream()
        .filter(e -> CollectionUtils.isNotEmpty(e.getProcessingAccounts()))
        .flatMap(e -> e.getProcessingAccounts().stream()).collect(Collectors.toList()).stream()
        .filter(e -> e.getPmleId() != null).map(ProcessingAccount::getPmleId).collect(Collectors.toSet());
  }

  /**
   * This method sets the name and id searchParam for masterMerchant request with OR operation.
   */
  @Override
  public void setIdOrNameSearchParamsByResourceType(String ownerType, String ownerId,
      MerchantSearchAfterRequest request) {
    if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_PARTNER)) {
      request.setSearchParams(SearchParams.builder().partnerId(ownerId).partnerName(ownerId).build());
    } else if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_BUSINESS_RELATION_NAME)) {
      request.setSearchParams(SearchParams.builder().businessRelationName(ownerId).build());
    } else if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_PMLE)) {
      request.setSearchParams(SearchParams.builder().pmleId(ownerId).pmleName(ownerId).build());
    } else if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_MLE)) {
      request
          .setSearchParams(SearchParams.builder().merchantLegalEntityId(ownerId).merchantLegalEntity(ownerId).build());
    } else if (ownerType.equalsIgnoreCase(ResourceType.ACCOUNT_GROUP.toString())) {
      request.setSearchParams(SearchParams.builder().accountGroups(ownerId).build());
    } else if (ownerType.equalsIgnoreCase(ResourceType.FMA.toString())) {
      request.setSearchParams(SearchParams.builder().merchantId(ownerId).merchantName(ownerId).build());
    }
    request.setOperator(OR_OPERATOR);
  }

  @Override
  public Set<PartnerLegalEntity> getPartnerHierarchyList(String ownerType, String ownerId) {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    request.setResponseFields((ownerType.equalsIgnoreCase(RESOURCE_TYPE_BUSINESS_RELATION_NAME)
            ? (BUSINESS_RELATION_NAME_HIERARCHY_RESPONSE_FIELDS) : (PARTNER_HIERARCHY_RESPONSE_FIELDS)));
    setIdOrNameSearchParamsByResourceType(ownerType, ownerId, request);
    setSearchAfterApiCommonFields(request);
    logger.info("making find merchants initial call: {}", request);
    MerchantSearchResponse merchants = masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
    logger.info("found {} number of merchants ", merchants.getTotalCount());
    Map<String, PartnerLegalEntity> resultPartnerMap = new HashMap<>();
    preparePartnerMap(ownerType, merchants, resultPartnerMap);
    List<MerchantResponse> merchantsList = merchants.getMerchants();
    int totalFetchedRecords = merchantsList.size();
    while (totalFetchedRecords < merchants.getTotalCount()) {
      request.setOffset(null);
      request.setSearchAfter(merchantsList.get(totalFetchedRecords - 1).getPaymentAccounts().get(0).getId());
      logger.info("making searchAfter call to find merchants: {}", request);
      merchants = masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
      preparePartnerMap(ownerType, merchants, resultPartnerMap);
      merchantsList.addAll(merchants.getMerchants());
      totalFetchedRecords = totalFetchedRecords + merchants.getMerchants().size();
    }
    Set<PartnerLegalEntity> partners = getPartnerLegalEntitySet(resultPartnerMap, ownerType, ownerId);
    logger.info("Successfully prepared the legalentity hierarchy");
    return partners;
  }

  private void preparePartnerMap(String ownerType, MerchantSearchResponse merchants,
      Map<String, PartnerLegalEntity> resultPartnerMap) {
    Map<String, Set<ParentMerchantLegalEntity>> pmleMap = new HashMap<>();
    Map<String, Set<MerchantLegalEntity>> mleMap = new HashMap<>();
    Map<String, Set<PaymentAccount>> paymentAccountMap = new HashMap<>();
    for (MerchantResponse merchant : merchants.getMerchants()) {
      for (PaymentAccountResponse paymentAccount : merchant.getPaymentAccounts()) {
        for (ProcessingAccount processingAccount : paymentAccount.getProcessingAccounts()) {
          addPartnerToMap(ownerType, resultPartnerMap, processingAccount);
          addPmleToMapForPartnerHierarchyList(ownerType, pmleMap, processingAccount);
          String mleId = addMleToMap(mleMap, processingAccount);
          PaymentAccount account = getPaymentAccount(paymentAccount, processingAccount);
          addPaymentAccountToMap(paymentAccountMap, mleId, account);
        }
      }
    }
    mapPaymentAccountsToMle(mleMap, paymentAccountMap);
    mapMlesToPmleForPartnerHierarchyList(pmleMap, mleMap);
    mapPmlesToPartner(resultPartnerMap, pmleMap);
  }

  private void addPartnerToMap(String ownerType, Map<String, PartnerLegalEntity> partnerMap,
      ProcessingAccount processingAccount) {
    if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_BUSINESS_RELATION_NAME)) {
      String businessRelationName = processingAccount.getBusinessDetails().getBusinessRelationName();
      if (!partnerMap.containsKey(businessRelationName)) {
        PartnerLegalEntity partner = getEmptybusinessRelationName();
        if (!ownerType.equalsIgnoreCase(RESOURCE_TYPE_MLE) || !ownerType.equalsIgnoreCase(RESOURCE_TYPE_PMLE)) {
          partner.setResourceId(businessRelationName);
          partner.setName(businessRelationName);
        }
        partnerMap.put(businessRelationName, partner);
      }
    } else {
      ProcessingAccount.OnboardingInformation onboardingInformation =
              processingAccount.getBusinessDetails().getOnboardingInformation();
      if (!partnerMap.containsKey(onboardingInformation.getPartnerId())) {
        PartnerLegalEntity partner = getEmptyPartner();
        if (!ownerType.equalsIgnoreCase(RESOURCE_TYPE_MLE) || !ownerType.equalsIgnoreCase(RESOURCE_TYPE_PMLE)) {
          partner.setResourceId(onboardingInformation.getPartnerId());
          partner.setName(onboardingInformation.getPartnerName());
        }
        partnerMap.put(onboardingInformation.getPartnerId(), partner);
      }
    }
  }

  private void addPmleToMapForPartnerHierarchyList(String ownerType, Map<String,
          Set<ParentMerchantLegalEntity>> pmleMap, ProcessingAccount processingAccount) {
    if (processingAccount != null) {
      String pmleId = processingAccount.getPmleId();
      String pmleName = processingAccount.getPmleName();
      ParentMerchantLegalEntity pmle = getParentMerchantLegalEntity(pmleId, pmleName);
      if (ownerType.equalsIgnoreCase(RESOURCE_TYPE_BUSINESS_RELATION_NAME)) {
        if (pmleMap.containsKey(processingAccount.getBusinessDetails().getBusinessRelationName())) {
          pmleMap.get(processingAccount.getBusinessDetails().getBusinessRelationName()).add(pmle);
        } else {
          pmleMap.put(processingAccount.getBusinessDetails().getBusinessRelationName(),
                  new HashSet<>(Arrays.asList(pmle)));
        }
      } else {
        if (pmleMap.containsKey(processingAccount.getBusinessDetails().getOnboardingInformation().getPartnerId())) {
          pmleMap.get(processingAccount.getBusinessDetails().getOnboardingInformation().getPartnerId()).add(pmle);
        } else {
          pmleMap.put(processingAccount.getBusinessDetails().getOnboardingInformation().getPartnerId(),
                  new HashSet<>(Arrays.asList(pmle)));
        }
      }
    }
  }

  private void mapPmlesToPartner(Map<String, PartnerLegalEntity> partnerMap,
      Map<String, Set<ParentMerchantLegalEntity>> pmleMap) {
    pmleMap.keySet().forEach(partnerId -> partnerMap.get(partnerId).getPmles().addAll(pmleMap.get(partnerId)));
  }

  private PartnerLegalEntity getEmptyPartner() {
    PartnerLegalEntity partner = new PartnerLegalEntity();
    partner.setResourceType(RESOURCE_TYPE_PARTNER);
    partner.setResourceId(StringUtils.EMPTY);
    partner.setName(StringUtils.EMPTY);
    return partner;
  }

  private PartnerLegalEntity getEmptybusinessRelationName() {
    PartnerLegalEntity partner = new PartnerLegalEntity();
    partner.setResourceType(RESOURCE_TYPE_BUSINESS_RELATION_NAME);
    partner.setResourceId(StringUtils.EMPTY);
    partner.setName(StringUtils.EMPTY);
    return partner;
  }

  private ParentMerchantLegalEntity getParentMerchantLegalEntity(String pmleId, String pmleName) {
    ParentMerchantLegalEntity pmle = new ParentMerchantLegalEntity();
    pmle.setResourceType(RESOURCE_TYPE_PMLE);
    pmle.setResourceId(pmleId);
    pmle.setName(pmleName);
    return pmle;
  }

  private void mapMlesToPmleForPartnerHierarchyList(Map<String, Set<ParentMerchantLegalEntity>> pmleMap,
      Map<String, Set<MerchantLegalEntity>> mleMap) {
    pmleMap.values().parallelStream().flatMap(Collection::stream).forEach(pmle -> {
      Set<MerchantLegalEntity> mles = mleMap.get(pmle.getResourceId());
      if (mles != null) {
        pmle.setMles(mles);
      }
    });
  }

  private Set<PartnerLegalEntity> getPartnerLegalEntitySet(Map<String, PartnerLegalEntity> resultPartnerMap,
      String ownerType, String ownerId) {
    Set<PartnerLegalEntity> partners = new HashSet<>();
    for (Map.Entry<String, PartnerLegalEntity> resultPartner : resultPartnerMap.entrySet()) {
      PartnerLegalEntity partnerLegalEntity = resultPartner.getValue();
      PartnerLegalEntity partner = new PartnerLegalEntity();
      partner.setResourceId(partnerLegalEntity.getResourceId());
      partner.setResourceType(partnerLegalEntity.getResourceType());
      partner.setName(partnerLegalEntity.getName());
      partner.setOwnerId(ownerId);
      partner.setOwnerType(ownerType);
      partner.setPmles(partnerLegalEntity.getPmles());
      partners.add(partner);
    }
    return partners;
  }

  @Override
  public List<AccountGroupMerchantSearchResponse> getAccountGroupsByContainsSearch(String accountGroupId) {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    request.setResponseFields(ACCOUNT_GROUP_RESPONSE_FIELDS);
    request.setSearchParams(SearchParams.builder().accountGroups(accountGroupId).build());
    MerchantSearchResponse merchantSearchResponse = getMerchantsUsingSearchAfter(request);
    return formAccountGroupsListResponse(merchantSearchResponse, accountGroupId);
  }

  private List<AccountGroupMerchantSearchResponse> formAccountGroupsListResponse(
      MerchantSearchResponse merchantSearchResponse, String accountGroupId) {
    Map<String, Integer> accountGroupsCountMap = constructAccountGroupsCountMap(merchantSearchResponse, accountGroupId);
    return UserManagmentUtil.formAccountGroupsListResponse(accountGroupsCountMap);
  }

  private Map<String, Integer> constructAccountGroupsCountMap(MerchantSearchResponse merchantSearchResponse,
      String accountGroupId) {
    Map<String, Integer> accountGroupsCountMap = new HashMap<>();
    for (MerchantResponse merchantResponse : merchantSearchResponse.getMerchants()) {
      Set<String> accountGroups =
          userFilterUtil.getAccountGroupIds(new ArrayList<>(Arrays.asList(merchantResponse)), accountGroupId);
      for (String ag : accountGroups) {
        if (accountGroupsCountMap.containsKey(ag)) {
          accountGroupsCountMap.put(ag, accountGroupsCountMap.get(ag) + 1);
        } else {
          accountGroupsCountMap.put(ag, 1);
        }
      }
    }
    return accountGroupsCountMap;
  }

  @Override
  public UsersListResponseResource getAccountTypeUsers(ResourceType resourceName, String resourceId,
      UserStatusFilter status, boolean merchantTypeValidation, Integer page, Integer pageSize) {
    String application = CommonThreadLocal.getAuthLocal().getApplication();
    Status userStatus = userFilterUtil.mapUserStatusFilter(status);
    UserFetchByFiltersRequestDto userFetchByFilterDto = UserFetchByFiltersRequestDto.builder().application(application)
        .resourceId(resourceId).resourceType(resourceName.toString()).status(userStatus)
        .merchantTypeValidation(merchantTypeValidation).build();
    Specification<User> userSpec =
        userService.checkApplicationAndConstructSpecification(userFetchByFilterDto, false, true);
    if (userSpec == null) {
      UsersListResponseResource userListResource = new UsersListResponseResource();
      userListResource.setCount(0L);
      userListResource.setUsers(new ArrayList<>());
      return userListResource;
    }
    PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by("lastModifiedDate").descending());
    Page<User> usersPage = usersRepository.findAll(userSpec, pageRequest);
    return constructAccountTypeUsersResponse(usersPage);
  }

  private UsersListResponseResource constructAccountTypeUsersResponse(Page<User> usersPage) {
    UsersListResponseResource usersListResponse = new UsersListResponseResource();
    if (usersPage != null && CollectionUtils.isNotEmpty(usersPage.getContent())) {
      List<UserResponseResource> userResourceList = new ArrayList<>();
      for (User user : usersPage.getContent()) {
        UserResponseResource userResource = new UserResponseResource();
        userResource.setUserName(user.getLoginName());
        userResource.setId(user.getUserId());
        userResourceList.add(userResource);
      }
      usersListResponse.setCount(usersPage.getTotalElements());
      usersListResponse.setUsers(userResourceList);
    }
    return usersListResponse;
  }

  /**
   * This method sets the name and id filterParam for masterMerchant request with OR operation.
   */
  @Override
  public void setIdOrNameFilterParamsByResourceType(String ownerType, String ownerId,
      MerchantSearchAfterRequest request) {
    if (RESOURCE_TYPE_PARTNER.equalsIgnoreCase(ownerType)) {
      request.setFilterParams(
          FilterParams.builder().partnerId(Arrays.asList(ownerId)).partnerName(Arrays.asList(ownerId)).build());
    } else if (RESOURCE_TYPE_PMLE.equalsIgnoreCase(ownerType)) {
      request.setFilterParams(
          FilterParams.builder().pmleId(Arrays.asList(ownerId)).pmleName(Arrays.asList(ownerId)).build());
    } else if (RESOURCE_TYPE_MLE.equalsIgnoreCase(ownerType)) {
      request.setFilterParams(FilterParams.builder().merchantLegalEntityId(Arrays.asList(ownerId))
          .merchantLegalEntity(Arrays.asList(ownerId)).build());
    } else if (ResourceType.ACCOUNT_GROUP.toString().equalsIgnoreCase(ownerType)) {
      request.setFilterParams(FilterParams.builder().accountGroups(Arrays.asList(ownerId)).build());
    } else if (ResourceType.FMA.toString().equalsIgnoreCase(ownerType)) {
      request.setFilterParams(
          FilterParams.builder().merchantId(Arrays.asList(ownerId)).merchantName(Arrays.asList(ownerId)).build());
    }
    request.setOperator(OR_OPERATOR);
  }
}
