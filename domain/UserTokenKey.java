// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * Composite key for UserTokenKey.
 */
@Data
public class UserTokenKey implements Serializable {

  private static final long serialVersionUID = 8024569874203316177L;

  private String loginName;

  private String token;

}
