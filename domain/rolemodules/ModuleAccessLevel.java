// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain.rolemodules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Table(name = "module_access_level", schema = "uspr")
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ModuleAccessLevelKey.class)
@EntityListeners(AuditingEntityListener.class)
public class ModuleAccessLevel {

  @Id
  @Column(name = "show")
  private String show;

  @Id
  @Column(name = "module_id")
  private String moduleId;

  @Column(name = "enabled")
  private boolean enabled;

  @ManyToOne
  @JoinColumn(name = "module_id", nullable = false, insertable = false, updatable = false)
  private Module module;

}
