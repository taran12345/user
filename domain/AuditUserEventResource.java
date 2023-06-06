// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "audit_user_event_resource")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuditUserEventResource {

  @Id
  @Column(name = "id", unique = true)
  private String id;

  @JsonBackReference
  @ManyToOne
  @JoinColumn( name = "event_id", nullable = false)
  private AuditUserEvent auditUserEvent;

  @Column(name = "resource_id")
  private String resourceId;

  @Column(name = "resource_type")
  private String resourceType;

  @Column(name = "roles")
  private String roles;

  @Column(name = "permissions")
  private String permissions;

  @Column(name = "user_resource_access_status")
  private String userResourceAccessStatus;

  @Column(name = "activity_desc")
  private String activityDesc;

  @Column(name = "prev_permissions")
  private String prevPermissions;

  @Column(name = "prev_role")
  private String prevRole;

  public void setPermissions(String permissions) {
    this.permissions = StringUtils.substring(permissions, 0, 4000);
  }

}
