// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import lombok.Data;

@Data
public class BusinessUnitConfig {
  private String hostName;
  private String basePath;
  private String uaaPath;
  private boolean disabled;
  private String contactEmail;
  private String emailSenderName;
  private String adminRole;
}
