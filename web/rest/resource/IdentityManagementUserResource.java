// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.domain.RecoveryQuestion;
import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller.HashedPassword;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class IdentityManagementUserResource {

  private String id;

  private String firstName;

  private String lastName;

  @NotBlank
  private String email;

  @NotBlank
  private String userName;

  private String password;
  private HashedPassword hashedPassword;
  private UserStatus status;
  private String mobilePhone;
  private Set<String> groupIds;
  private String pmleId;
  private List<String> roles;
  private List<String> accessGroups;
  private String businessUnit;
  private String application;
  private RecoveryQuestion recoveryQuestion;
  private Boolean activate;
  private String externalId;
  private Map<String, Object> customProperties;
  private String ownerId;
  private String ownerType;
  private boolean migrationUseCase = false;
}
