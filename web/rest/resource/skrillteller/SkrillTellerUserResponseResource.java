// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller;

import com.paysafe.upf.user.provisioning.enums.UserStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class SkrillTellerUserResponseResource {

  private String id;
  private String userName;
  private String email;
  private UserStatus status;
  private String lastLoginDate;
  private Origin origin;
  private Profile profile;
  private List<SkrillAccessResources> accessResources;
  private Credentials credentials;
  private String mobilePhone;
  private Set<String> groupIds;
  private String pmleId;
  private List<String> roles;
  private List<String> accessGroups;
  private String businessUnit;
  private String externalId;
  private Map<String, Object> customProperties;
  private DateTime createdDate;
  private DateTime lastModifiedDate;
  private String createdBy;
}
