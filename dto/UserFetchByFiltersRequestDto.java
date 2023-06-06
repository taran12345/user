// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import com.paysafe.upf.user.provisioning.enums.OwnerType;
import com.paysafe.upf.user.provisioning.enums.Status;

import lombok.Builder;
import lombok.Data;

import org.joda.time.DateTime;

import java.util.List;

@Data
@Builder
public class UserFetchByFiltersRequestDto {
  private String application;
  private String userIdentifier;
  private Status status;
  private String role;
  private List<String> roles;
  private String createdBy;
  private DateTime createdDate;
  private String resourceType;
  private String resourceId;
  private List<String> loginNames;
  private OwnerType userType;
  private boolean merchantTypeValidation;
  private List<String> disabledBrands;
  private List<String> resourceIds;
}
