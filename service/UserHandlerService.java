// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.enums.BusinessUnit;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.web.rest.resource.UserDataSyncResponseResource;
import com.paysafe.upf.user.provisioning.web.rest.resource.UsersBusinessUnitUpdateResponse;
import com.paysafe.upf.user.provisioning.web.rest.resource.WalletUserCountResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface UserHandlerService {
  void updateUserStatusAndAuditTableFromOkta(JsonNode oktaRequest) throws JsonProcessingException;

  void activateUserWithOkta(JsonNode oktaRequest) throws JsonProcessingException;

  WalletUserCountResource getUserCount(String resourceType, String resourceId);

  List<WalletUserCountResource> getBulkUserCount(String loginName);

  void deleteUser(String userIdentifier, String application, Boolean isDeleteUserFromBo);

  UsersBusinessUnitUpdateResponse updateUsersBusinessUnit(String application, String loginName,
      BusinessUnit businessUnit, Integer size, boolean updateBasedOnMasterMerTagOrUserRole,
      String existingUserBusinessUnit);

  UserDataSyncResponseResource syncOktaToUsersDb(String application, String loginName, String ownerId,
      String ownerType, Integer size);

  void updateStausInOktaAndUserDb(String application, Status status, String userId);
}
