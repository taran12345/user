// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain.rolemodules;

import lombok.Data;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@EntityListeners(AuditingEntityListener.class)
@IdClass(BusinessInitiativeRolesKey.class)
@Table(name = "business_initiative_roles", schema = "uspr")
@Data
public class BusinessInitiativeRoles {

  @Id
  @Column(name = "role")
  private String role;

  @Column(name = "role_type")
  private String roleType;

  @Column(name = "display_order")
  private Integer displayOrder;

  @Id
  @Column(name = "business_initiative")
  private String businessInitiative;

  @Column(name = "enabled")
  private boolean enabled;

  @ManyToOne
  @JoinColumn(name = "business_initiative", nullable = false, insertable = false, updatable = false)
  private BusinessInitiativeInfo businessInitiativeInfo;

  /**
   * Empty constructor.
   */
  public BusinessInitiativeRoles() {
    // Intentionally empty.
  }

  /**
   * Creates a new business initiative roles.
   */
  public BusinessInitiativeRoles(String role, String roleType, Integer displayOrder, String businessInitiative,
      boolean enabled) {
    this.role = role;
    this.roleType = roleType;
    this.displayOrder = displayOrder;
    this.businessInitiative = businessInitiative;
    this.enabled = enabled;
  }
}