// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.enums.ResourceType;
import com.paysafe.upf.user.provisioning.enums.UserStatusFilter;
import com.paysafe.upf.user.provisioning.web.rest.resource.AccountGroupMerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.FetchEmailResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchAfterRequest;
import com.paysafe.upf.user.provisioning.web.rest.resource.MerchantSearchResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.ParentMerchantLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.PartnerLegalEntity;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersListResponseResource;

import java.util.List;
import java.util.Set;

public interface MasterMerchantService {

  List<FetchEmailResponseResource> getEmail(String userName, String application);

  Set<ParentMerchantLegalEntity> getPmleList(String ownerType, String ownerId, boolean isContainsSearch);

  PartnerLegalEntity getPartnerHierarchy(String ownerType, String ownerId, boolean isContainsSearch);

  MerchantSearchResponse getMerchantsSingleRecord(MerchantSearchAfterRequest request);

  MerchantSearchAfterRequest setSearchAfterApiCommonFields(MerchantSearchAfterRequest request);

  MerchantSearchResponse getMerchantsUsingSearchAfter(MerchantSearchAfterRequest request);

  AccountGroupMerchantSearchResponse getMerchantForAccountGroups(List<String> accountGroupIds,
      List<String> paymentAccountIds,
      int page, int size);

  void setFilterParamsByResourceType(String ownerType, String ownerId, MerchantSearchAfterRequest request);

  Set<String> getAllPmleIds(String resourceType, String resourceId);

  void setIdOrNameSearchParamsByResourceType(String ownerType, String ownerId, MerchantSearchAfterRequest request);

  Set<PartnerLegalEntity> getPartnerHierarchyList(String ownerType, String ownerId);

  List<AccountGroupMerchantSearchResponse> getAccountGroupsByContainsSearch(String accountGroupId);

  UsersListResponseResource getAccountTypeUsers(ResourceType resourceName, String resourceId, UserStatusFilter status,
      boolean merchantTypeValidation, Integer page, Integer pageSize);

  void setIdOrNameFilterParamsByResourceType(String ownerType, String ownerId, MerchantSearchAfterRequest request);
}
