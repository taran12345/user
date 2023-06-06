// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import lombok.Data;

import java.util.List;

@Data
public class AccessGroupCreationStatusResponseResource {
  List<AccessGroupCreatedStatus> accessGroupsCreatedStatus;
}
