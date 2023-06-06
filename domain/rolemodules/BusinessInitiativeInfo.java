// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain.rolemodules;

import lombok.Data;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "business_initiative_info", schema = "uspr")
@Data
public class BusinessInitiativeInfo {

  @Id
  @Column(name = "business_initiative")
  private String businessInitiative;

  @Column(name = "description")
  private String description;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "businessInitiativeInfo", cascade = CascadeType.ALL,
      orphanRemoval = true)
  @OrderBy("displayOrder ASC")
  private List<BusinessInitiativeRoles> businessInitiativeRoles;

  public BusinessInitiativeInfo() {
    // Intentionally empty.
  }

  /**
   * Creates a new business initiative.
   *
   * @param businessInitiative the name of the user
   * @param description the token
   */
  public BusinessInitiativeInfo(String businessInitiative, String description) {
    this.businessInitiative = businessInitiative;
    this.description = description;
  }
}