// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserAccessGroupMappingKey implements Serializable {

  private static final long serialVersionUID = 8024569874203316177L;

  private String userId;

  private String resourceId;

  private String resourceType;
}
