// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain.rolemodules;

import lombok.Data;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "single_api_permissions", schema = "uspr")
@Data
public class SingleApiPermissions {

  @Id
  @Column(name = "permission")
  private String permission;

  @Column(name = "enabled")
  private boolean enabled;

  public SingleApiPermissions() {
    // Intentionally empty.
  }

  /**
   * Creates a new single api permission.
   */
  public SingleApiPermissions(String permission, boolean enabled) {
    this.permission = permission;
    this.enabled = enabled;
  }
}