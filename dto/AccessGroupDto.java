// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.dto;

import lombok.Data;

import java.util.List;

@Data
public class AccessGroupDto {
  private List<CustomAccessGroupDto> customAccessGroupDtos;
  private List<String> existingAccessGroupIds;
}