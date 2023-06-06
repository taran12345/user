// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_assigned_applications")
public class UserAssignedApplications extends Auditable {

  @Column(name = "internal_id", nullable = false)
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID internalId;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "application", nullable = false)
  private String application;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
  private User user;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public UUID getInternalId() {
    return internalId;
  }

  public void setInternalId(UUID internalId) {
    this.internalId = internalId;
  }
}
