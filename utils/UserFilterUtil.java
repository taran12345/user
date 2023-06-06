// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.utils;

import com.paysafe.op.errorhandling.CommonErrorCode;
import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.enums.UserStatusFilter;
import com.paysafe.upf.user.provisioning.feignclients.MasterMerchantFeignClient;
import com.paysafe.upf.user.provisioning.repository.UserAccessGroupMapppingRepository;
import com.paysafe.upf.user.provisioning.repository.UsersRepository;
import com.paysafe.upf.user.provisioning.service.MasterMerchantService;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.FilterParams;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchAfterRequest;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.PaymentAccountResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount.BusinessDetails;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount.LegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.ProcessingAccount.OnboardingInformation;
import com.paysafe.upf.user.provisioning.web.rest.resource.SourceAuthority;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserFilterUtil {

  private static final String RESOURCE_TYPE_PARTNER = "PARTNER";
  private static final String RESOURCE_TYPE_PMLE = "PMLE";
  private static final String RESOURCE_TYPE_MLE = "MLE";
  private static final String RESOURCE_TYPE_PAYMENT_ACCOUNT = "PAYMENT_ACCOUNT";
  private static final String RESOURCE_TYPE_FMA = "FMA";
  private static final String RESOURCE_TYPE_ACCOUNT_GROUP = "ACCOUNT_GROUP";
  private static final List<String> LEGAL_ENTITY_NAME_RESPONSE_FIELDS =
      new ArrayList<>(Arrays.asList("paymentAccounts.processingAccounts.pmleName",
          "paymentAccounts.processingAccounts.businessDetails.legalEntity.description"));
  private static final List<String> MERCHANT_IDS_RESPONSE_FIELDS =
      new ArrayList<>(Arrays.asList("paymentAccounts.processingAccounts.pmleId",
          "paymentAccounts.processingAccounts.businessDetails.onboardingInformation.partnerId",
          "paymentAccounts.processingAccounts.businessDetails.legalEntity.id", "paymentAccounts.id",
          "paymentAccounts.processingAccounts.businessDetails.accountGroups"));
  private static final List<String> ORIGIN = new ArrayList<>(Arrays.asList("NETBANX"));
  private static final Logger LOGGER = LoggerFactory.getLogger(UserFilterUtil.class);
  private static final List<String> FMA_ID_ONLY_RESPONSE_FIELD = new ArrayList<>(
      Arrays.asList("paymentAccounts.processingAccounts.sourceAuthority.referenceId", "paymentAccounts.id"));
  private static final List<String> MLE_ID_ONLY_RESPONSE_FIELD = new ArrayList<>(
      Arrays.asList("paymentAccounts.processingAccounts.businessDetails.legalEntity.id", "paymentAccounts.id"));
  private static final List<String> PMLE_ID_ONLY_RESPONSE_FIELD =
      new ArrayList<>(Arrays.asList("paymentAccounts.processingAccounts.pmleId", "paymentAccounts.id"));
  private static final List<String> PARTNER_ID_ONLY_RESPONSE_FIELD = new ArrayList<>(Arrays.asList(
      "paymentAccounts.processingAccounts.businessDetails.onboardingInformation.partnerId", "paymentAccounts.id"));
  private static final List<String> ACCOUNT_GROUP_ID_ONLY_RESPONSE_FIELD = new ArrayList<>(
      Arrays.asList("paymentAccounts.processingAccounts.businessDetails.accountGroups", "paymentAccounts.id"));

  @Autowired
  private UsersRepository usersRepository;

  @Autowired
  private MasterMerchantFeignClient masterMerchantFeignClient;

  @Autowired
  private UserAccessGroupMapppingRepository userAccessGroupMapppingRepository;

  @Autowired
  private MasterMerchantService masterMerchantService;

  /**
   * user filter for resource-type MLE/PMLE/PARTNER/FMA.
   */
  public void getUsersByFilters(UserFetchByFiltersRequestDto userFetchByFiltersRequestDto, boolean isEqualSearch) {
    List<String> resourceIds = getResourceIdsByIdOrNameSearch(userFetchByFiltersRequestDto, isEqualSearch);
    if (CollectionUtils.isEmpty(resourceIds)) {
      throw new BadRequestException.Builder()
          .details("No match found for input: " + userFetchByFiltersRequestDto.getResourceId())
          .errorCode(CommonErrorCode.INVALID_FIELD).build();
    }
    userFetchByFiltersRequestDto.setResourceIds(resourceIds);
    Map<String, Set<String>> merchantIdsMap = getMerchantIdsFromMasterMerchant(userFetchByFiltersRequestDto);
    Set<String> mergedRoles =
        mergeRolesInRequest(userFetchByFiltersRequestDto.getRole(), userFetchByFiltersRequestDto.getRoles());
    List<String> accessGroupIds = CollectionUtils.isEmpty(mergedRoles) ? getAccessGroupIds(merchantIdsMap)
        : getAccessGroupIds(merchantIdsMap, mergedRoles);
    // removing the duplicate loginNames
    Set<String> loginNames = new HashSet<>(getLoginName(accessGroupIds));
    userFetchByFiltersRequestDto
        .setLoginNames(CollectionUtils.isEmpty(loginNames) ? null : new ArrayList<>(loginNames));
  }

  private List<String> getResourceIdsByIdOrNameSearch(UserFetchByFiltersRequestDto userFetchByFiltersRequestDto,
      boolean isEqualSearch) {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    setResponseFieldIdByResourceType(request, userFetchByFiltersRequestDto.getResourceType());
    if (isEqualSearch) {
      masterMerchantService.setIdOrNameFilterParamsByResourceType(userFetchByFiltersRequestDto.getResourceType(),
          userFetchByFiltersRequestDto.getResourceId(), request);
    } else {
      masterMerchantService.setIdOrNameSearchParamsByResourceType(userFetchByFiltersRequestDto.getResourceType(),
          userFetchByFiltersRequestDto.getResourceId(), request);
    }
    MerchantSearchResponse merchantSearchResponse = masterMerchantService.getMerchantsUsingSearchAfter(request);
    List<String> merchantIds = new ArrayList<>();
    merchantIds.addAll(getRequiredMerchantIdsByResourceType(merchantSearchResponse.getMerchants(),
        userFetchByFiltersRequestDto.getResourceType(), userFetchByFiltersRequestDto.getResourceId()));
    return merchantIds;
  }

  private void setResponseFieldIdByResourceType(MerchantSearchAfterRequest request, String resourceType) {
    if (StringUtils.equals(resourceType, ResourceType.FMA.name())) {
      request.setResponseFields(FMA_ID_ONLY_RESPONSE_FIELD);
    } else if (StringUtils.equals(resourceType, ResourceType.MLE.name())) {
      request.setResponseFields(MLE_ID_ONLY_RESPONSE_FIELD);
    } else if (StringUtils.equals(resourceType, ResourceType.PMLE.name())) {
      request.setResponseFields(PMLE_ID_ONLY_RESPONSE_FIELD);
    } else if (StringUtils.equals(resourceType, ResourceType.PARTNER.name())) {
      request.setResponseFields(PARTNER_ID_ONLY_RESPONSE_FIELD);
    } else if (StringUtils.equals(resourceType, ResourceType.ACCOUNT_GROUP.name())) {
      request.setResponseFields(ACCOUNT_GROUP_ID_ONLY_RESPONSE_FIELD);
    }
  }

  private Set<String> getRequiredMerchantIdsByResourceType(List<MerchantResponse> merchants, String resourceType,
      String resourceId) {
    Set<String> merchantIds = new HashSet<>();
    if (StringUtils.equals(resourceType, ResourceType.FMA.name())) {
      merchantIds = getFmaIds(merchants);
    } else if (StringUtils.equals(resourceType, ResourceType.MLE.name())) {
      merchantIds = getMleIds(merchants);
    } else if (StringUtils.equals(resourceType, ResourceType.PMLE.name())) {
      merchantIds = getPmleIds(merchants);
    } else if (StringUtils.equals(resourceType, ResourceType.PARTNER.name())) {
      merchantIds = getPartnerIds(merchants);
    } else if (StringUtils.equals(resourceType, ResourceType.ACCOUNT_GROUP.name())) {
      merchantIds = getAccountGroupIds(merchants, resourceId);
    }
    return merchantIds;
  }

  private Set<String> getFmaIds(List<MerchantResponse> merchants) {
    return merchants.stream().flatMap(e -> e.getPaymentAccounts().stream()).collect(Collectors.toList()).stream()
        .flatMap(e -> e.getProcessingAccounts().stream()).collect(Collectors.toList()).stream()
        .map(ProcessingAccount::getSourceAuthority).map(SourceAuthority::getReferenceId).collect(Collectors.toSet());
  }

  private Set<String> getMleIds(List<MerchantResponse> merchants) {
    return merchants.stream().flatMap(e -> e.getPaymentAccounts().stream()).collect(Collectors.toList()).stream()
        .flatMap(e -> e.getProcessingAccounts().stream()).collect(Collectors.toList()).stream()
        .filter(e -> e.getBusinessDetails() != null).map(ProcessingAccount::getBusinessDetails)
        .filter(e -> e.getLegalEntity() != null).map(BusinessDetails::getLegalEntity).map(LegalEntity::getId)
        .collect(Collectors.toSet());
  }

  private Set<String> getPmleIds(List<MerchantResponse> merchants) {
    return merchants.stream().flatMap(e -> e.getPaymentAccounts().stream()).collect(Collectors.toList()).stream()
        .flatMap(e -> e.getProcessingAccounts().stream()).collect(Collectors.toList()).stream()
        .map(ProcessingAccount::getPmleId).collect(Collectors.toSet());
  }

  private Set<String> getPartnerIds(List<MerchantResponse> merchants) {
    return merchants.stream().flatMap(e -> e.getPaymentAccounts().stream()).collect(Collectors.toList()).stream()
        .flatMap(e -> e.getProcessingAccounts().stream()).collect(Collectors.toList()).stream()
        .filter(e -> e.getBusinessDetails() != null).map(ProcessingAccount::getBusinessDetails)
        .filter(e -> e.getOnboardingInformation() != null).map(BusinessDetails::getOnboardingInformation)
        .map(OnboardingInformation::getPartnerId).collect(Collectors.toSet());
  }

  /**
   * Returns the accountGroupIds from master-merchant list response.
   */
  public Set<String> getAccountGroupIds(List<MerchantResponse> merchants, String resourceId) {
    Set<List<String>> setOfMerchatList = merchants.stream().flatMap(e -> e.getPaymentAccounts().stream())
        .collect(Collectors.toList()).stream().flatMap(e -> e.getProcessingAccounts().stream())
        .collect(Collectors.toList()).stream().filter(e -> e.getBusinessDetails() != null)
        .map(ProcessingAccount::getBusinessDetails).filter(e -> e.getAccountGroups() != null)
        .map(BusinessDetails::getAccountGroups).collect(Collectors.toSet());
    Set<String> merchantIds = new HashSet<>();
    for (List<String> merchantList : setOfMerchatList) {
      merchantIds.addAll(merchantList);
    }
    merchantIds.removeIf(e -> !StringUtils.containsIgnoreCase(e, resourceId));
    return merchantIds;
  }

  private Set<String> mergeRolesInRequest(String role, List<String> roles) {
    List<String> mergedRoles = new ArrayList<>();
    if (StringUtils.isNotEmpty(role)) {
      mergedRoles.add(role);
    }
    if (CollectionUtils.isNotEmpty(roles)) {
      mergedRoles.addAll(roles);
    }
    return CollectionUtils.isNotEmpty(mergedRoles) ? new HashSet<>(mergedRoles) : Collections.emptySet();
  }

  private Map<String, Set<String>> getMerchantIdsFromMasterMerchant(
      UserFetchByFiltersRequestDto userFetchByFiltersRequestDto) {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    request.setResponseFields(MERCHANT_IDS_RESPONSE_FIELDS);
    setFilterParamsByResourceType(userFetchByFiltersRequestDto.getResourceType(),
        userFetchByFiltersRequestDto.getResourceIds(), request);
    masterMerchantService.setSearchAfterApiCommonFields(request);
    LOGGER.info("making find merchants initial call: {}", request);
    MerchantSearchResponse merchants = masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
    LOGGER.info("found {} number of merchants ", merchants.getTotalCount());
    Map<String, Set<String>> resourceIdsMap = new HashMap<>();
    mapResourceIds(merchants, resourceIdsMap, userFetchByFiltersRequestDto);
    List<MerchantResponse> merchantsList = merchants.getMerchants();
    int totalFetchedRecords = merchantsList.size();
    while (totalFetchedRecords < merchants.getTotalCount()) {
      request.setOffset(null);
      request.setSearchAfter(merchantsList.get(totalFetchedRecords - 1).getPaymentAccounts().get(0).getId());
      LOGGER.info("making searchAfter call to find merchants: {}", request);
      merchants = masterMerchantFeignClient.getMerchantsUsingSearchAfter(request);
      mapResourceIds(merchants, resourceIdsMap, userFetchByFiltersRequestDto);
      merchantsList.addAll(merchants.getMerchants());
      totalFetchedRecords = totalFetchedRecords + merchants.getMerchants().size();
    }

    return resourceIdsMap;
  }

  /**
   * Returns legal entity name for the corresponding ownerType.
   * 
   * @param accessResources AccessResources
   */
  public String getLegalEntityNameFromMasterMerchant(AccessResources accessResources) {
    MerchantSearchAfterRequest request = new MerchantSearchAfterRequest();
    request.setResponseFields(LEGAL_ENTITY_NAME_RESPONSE_FIELDS);
    masterMerchantService.setFilterParamsByResourceType(accessResources.getOwnerType(), accessResources.getOwnerId(),
        request);
    MerchantSearchResponse response = masterMerchantService.getMerchantsSingleRecord(request);
    if (response == null || CollectionUtils.isEmpty(response.getMerchants())
        || CollectionUtils.isEmpty(response.getMerchants().get(0).getPaymentAccounts())) {
      return null;
    }
    if (RESOURCE_TYPE_PMLE.equalsIgnoreCase(accessResources.getOwnerType())) {
      return response.getMerchants().get(0).getPaymentAccounts().get(0).getProcessingAccounts().get(0).getPmleName();
    }
    return response.getMerchants().get(0).getPaymentAccounts().get(0).getProcessingAccounts().get(0)
        .getBusinessDetails().getLegalEntity().getDescription();
  }

  private void mapResourceIds(MerchantSearchResponse merchants, Map<String, Set<String>> resourceIdsMap,
      UserFetchByFiltersRequestDto userFetchByFiltersRequestDto) {
    for (MerchantResponse merchant : merchants.getMerchants()) {
      for (PaymentAccountResponse paymentAccount : merchant.getPaymentAccounts()) {
        for (ProcessingAccount processingAccount : paymentAccount.getProcessingAccounts()) {
          mapResourceIdsBasedOnType(resourceIdsMap, processingAccount, paymentAccount, userFetchByFiltersRequestDto);
        }
      }
    }
  }

  private void mapResourceIdsBasedOnType(Map<String, Set<String>> resourceIdsMap, ProcessingAccount processingAccount,
      PaymentAccountResponse paymentAccount, UserFetchByFiltersRequestDto userFetchByFiltersRequestDto) {
    if (StringUtils.equals(userFetchByFiltersRequestDto.getResourceType(), RESOURCE_TYPE_PMLE)) {
      addPmleToMap(resourceIdsMap, processingAccount);
      addPartnerToMap(resourceIdsMap, processingAccount);
    }
    if (StringUtils.equals(userFetchByFiltersRequestDto.getResourceType(), RESOURCE_TYPE_MLE)) {
      addPmleToMap(resourceIdsMap, processingAccount);
      addPartnerToMap(resourceIdsMap, processingAccount);
      addMleToMap(resourceIdsMap, processingAccount);
    }
    if (StringUtils.equals(userFetchByFiltersRequestDto.getResourceType(), RESOURCE_TYPE_FMA)) {
      addPmleToMap(resourceIdsMap, processingAccount);
      addPartnerToMap(resourceIdsMap, processingAccount);
      addMleToMap(resourceIdsMap, processingAccount);
      addPaymentAccountToMap(resourceIdsMap, paymentAccount);
    }
    if (StringUtils.equals(userFetchByFiltersRequestDto.getResourceType(), RESOURCE_TYPE_PARTNER)) {
      addPartnerToMap(resourceIdsMap, processingAccount);
    }
    if (StringUtils.equals(userFetchByFiltersRequestDto.getResourceType(), RESOURCE_TYPE_ACCOUNT_GROUP)) {
      addAccountGroupToMap(resourceIdsMap, processingAccount);
    }
  }

  private void addPmleToMap(Map<String, Set<String>> resourceIdsMap, ProcessingAccount processingAccount) {
    if (resourceIdsMap.containsKey(RESOURCE_TYPE_PMLE)) {
      resourceIdsMap.get(RESOURCE_TYPE_PMLE).add(processingAccount.getPmleId());
    } else {
      resourceIdsMap.put(RESOURCE_TYPE_PMLE, new HashSet<>(Arrays.asList(processingAccount.getPmleId())));
    }
  }

  private void addMleToMap(Map<String, Set<String>> resourceIdsMap, ProcessingAccount processingAccount) {
    ProcessingAccount.LegalEntity legalEntity = processingAccount.getBusinessDetails().getLegalEntity();
    if (resourceIdsMap.containsKey(RESOURCE_TYPE_MLE)) {
      resourceIdsMap.get(RESOURCE_TYPE_MLE).add(legalEntity.getId());
    } else {
      resourceIdsMap.put(RESOURCE_TYPE_MLE, new HashSet<>(Arrays.asList(legalEntity.getId())));
    }
  }

  private void addPaymentAccountToMap(Map<String, Set<String>> resourceIdsMap, PaymentAccountResponse paymentAccount) {
    if (resourceIdsMap.containsKey(RESOURCE_TYPE_PAYMENT_ACCOUNT)) {
      resourceIdsMap.get(RESOURCE_TYPE_PAYMENT_ACCOUNT).add(paymentAccount.getId());
    } else {
      resourceIdsMap.put(RESOURCE_TYPE_PAYMENT_ACCOUNT, new HashSet<>(Arrays.asList(paymentAccount.getId())));
    }
  }

  private void addPartnerToMap(Map<String, Set<String>> resourceIdsMap, ProcessingAccount processingAccount) {
    ProcessingAccount.OnboardingInformation onboardingInformation =
        processingAccount.getBusinessDetails().getOnboardingInformation();
    if (onboardingInformation != null && StringUtils.isNotEmpty(onboardingInformation.getPartnerId())) {
      if (resourceIdsMap.containsKey(RESOURCE_TYPE_PARTNER)) {
        resourceIdsMap.get(RESOURCE_TYPE_PARTNER).add(onboardingInformation.getPartnerId());
      } else {
        resourceIdsMap.put(RESOURCE_TYPE_PARTNER, new HashSet<>(Arrays.asList(onboardingInformation.getPartnerId())));
      }
    }
  }

  private void addAccountGroupToMap(Map<String, Set<String>> resourceIdsMap, ProcessingAccount processingAccount) {
    BusinessDetails businessDetails = processingAccount.getBusinessDetails();
    if (businessDetails != null && CollectionUtils.isNotEmpty(businessDetails.getAccountGroups())) {
      if (resourceIdsMap.containsKey(RESOURCE_TYPE_ACCOUNT_GROUP)) {
        resourceIdsMap.get(RESOURCE_TYPE_ACCOUNT_GROUP).addAll(businessDetails.getAccountGroups());
      } else {
        resourceIdsMap.put(RESOURCE_TYPE_ACCOUNT_GROUP, new HashSet<>(businessDetails.getAccountGroups()));
      }
    }
  }

  private void setFilterParamsByResourceType(String resourceType, List<String> resourceIds,
      MerchantSearchAfterRequest request) {
    if (resourceType.equalsIgnoreCase(RESOURCE_TYPE_PARTNER)) {
      request.setFilterParams(FilterParams.builder().partnerId(resourceIds).origin(ORIGIN).build());
    } else if (resourceType.equalsIgnoreCase(RESOURCE_TYPE_PMLE)) {
      request.setFilterParams(FilterParams.builder().pmleId(resourceIds).origin(ORIGIN).build());
    } else if (resourceType.equalsIgnoreCase(RESOURCE_TYPE_MLE)) {
      request.setFilterParams(FilterParams.builder().merchantLegalEntityId(resourceIds).origin(ORIGIN).build());
    } else if (resourceType.equalsIgnoreCase(RESOURCE_TYPE_ACCOUNT_GROUP)) {
      request.setFilterParams(FilterParams.builder().accountGroups(resourceIds).origin(ORIGIN).build());
    } else if (resourceType.equalsIgnoreCase(RESOURCE_TYPE_FMA)) {
      request.setFilterParams(FilterParams.builder().merchantId(resourceIds).origin(ORIGIN).build());
    }
  }

  private List<String> getAccessGroupIds(Map<String, Set<String>> merchantIdsMap) {
    List<String> accessGroupIds = new ArrayList<>();
    for (Map.Entry<String, Set<String>> entry : merchantIdsMap.entrySet()) {
      accessGroupIds.addAll(usersRepository.getAccessGroupIds(entry.getKey(), entry.getValue()));
    }
    return accessGroupIds;
  }

  private List<String> getAccessGroupIds(Map<String, Set<String>> merchantIdsMap, Set<String> roles) {
    List<String> accessGroupIds = new ArrayList<>();
    for (Map.Entry<String, Set<String>> entry : merchantIdsMap.entrySet()) {
      accessGroupIds.addAll(usersRepository.getAccessGroupIdsWithRoles(entry.getKey(), entry.getValue(), roles));
    }
    return accessGroupIds;
  }

  private List<String> getLoginName(List<String> accessGroupIds) {
    List<String> loginNames = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(accessGroupIds)) {
      loginNames = userAccessGroupMapppingRepository.getLoginName(accessGroupIds);
    }
    return loginNames;
  }

  /**
   * This method maps UserStatusFilter value to Status.
   */
  public Status mapUserStatusFilter(UserStatusFilter userStatus) {
    Status status = null;
    if (userStatus != null) {
      if (UserStatusFilter.BLOCKED.equals(userStatus)) {
        status = Status.SUSPENDED;
      } else if (UserStatusFilter.PENDING_USER_ACTION.equals(userStatus)) {
        status = Status.PROVISIONED;
      } else {
        status = Status.valueOf(userStatus.toString());
      }
    }
    return status;
  }

}
