// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(Include.NON_NULL)
@Data
public class PegasusUserResponseResource {

  private String uuid;

  private String fullName;

  private String email;

  private List<PegausUserRoleResource> roles;

  private String status;

  private String loginName;

  private Long pmleId;

  private List<String> accessGroups;

  private String type;

  private String accessLevelTypeCode;

  private String accessValue;

  private List<String> fmaIds;

  private List<String> groupRoles;

  private String allowedIpAddresses;

}