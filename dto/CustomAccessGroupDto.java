// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.Status;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class CustomAccessGroupDto {
  private String name;
  private String description;
  private List<String> accessPolicyIds;
  private Status status;
  private String merchantType;
  private String merchantId;
  private AccessGroupType type;
  private String ownerId;
  private String ownerType;

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    CustomAccessGroupDto that = (CustomAccessGroupDto) object;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
