// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller;

import com.paysafe.upf.user.provisioning.web.rest.resource.RegionUpdateStatusResource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegionUpdateResponse {
  private List<RegionUpdateStatusResource> usersStatus;
  long totalUsersCount;
  long failedUsersCount;
  long succeedUsersCount;
}
