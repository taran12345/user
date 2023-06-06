// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain.rolemodules;

import lombok.Data;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@EntityListeners(AuditingEntityListener.class)
@IdClass(RoleModulesKey.class)
@Table(name = "role_modules", schema = "uspr")
@Data
public class RoleModules {

  @Id
  @Column(name = "role")
  private String role;

  @Id
  @Column(name = "moduleId")
  private String moduleId;

  @Column(name = "display_order")
  private Integer displayOrder;

  @Column(name = "enabled")
  private boolean enabled;

  public RoleModules() {
    // Intentionally empty.
  }

  /**
   * Creates a new role, module map.
   *
   * @param role the name of the user
   * @param moduleId the token
   * @param enabled the token type
   */
  public RoleModules(String role, String moduleId, boolean enabled) {
    this.role = role;
    this.moduleId = moduleId;
    this.enabled = enabled;
  }
}