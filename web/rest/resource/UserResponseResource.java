// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.enums.UserStatus;
import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UserResponseResource {

  private String id;

  private String firstName;

  private String lastName;

  private String email;

  private String userName;
  private List<AccessResources> accessResources;
  private UserStatus status;
  private String mobilePhone;
  private Set<String> groupIds;
  private String pmleId;
  private List<String> roles;
  private List<String> accessGroups;
  private String businessUnit;
  private String application;
  private String externalId;
  private Map<String, Object> customProperties;
  private DateTime createdDate;
  private DateTime lastModifiedDate;
  private String createdBy;
  private String region;
  private boolean mfaEnabled;
}
