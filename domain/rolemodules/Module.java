// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain.rolemodules;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "module", schema = "uspr")
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Module {

  @Id
  @Getter
  @Setter
  @Column(name = "id")
  private String id;

  @Getter
  @Setter
  @Column(name = "label")
  private String label;

  @Getter
  @Setter
  @Column(name = "editable")
  private boolean editable;

  @Getter
  @Setter
  @Column(name = "is_selected")
  private boolean isSelected;

  @Getter
  @Setter
  @Column(name = "descriptions")
  private String descriptions;

  @Getter
  @Setter
  @Column(name = "parent_id", insertable = false, updatable = false)
  private String parentId;

  @Getter
  @Setter
  @Column(name = "selection_mode")
  private String selectionMode;

  @Getter
  @Setter
  @Column(name = "show_expand")
  private boolean showExpand;

  @Getter
  @Setter
  @Column(name = "is_expand")
  private boolean isExpand;

  @Getter
  @Setter
  @Column(name = "display_order")
  private Integer displayOrder;

  @Getter
  @Setter
  @Column(name = "enabled")
  private boolean enabled;

  @Getter
  @Setter
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ModulePermissions> modulePermissions;

  @Getter
  @Setter
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ModuleAccessLevel> moduleAccessLevel;

  @ManyToOne(fetch = FetchType.LAZY)
  @Getter
  @Setter
  private Module parent;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Setter
  @Getter
  @OrderBy("displayOrder ASC")
  private Set<Module> children;
}
