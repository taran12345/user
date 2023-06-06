// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import com.paysafe.upf.user.provisioning.web.rest.resource.usersummary.UserSummary;

import com.vladmihalcea.hibernate.type.json.JsonBlobType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uspr_user_summary")
@TypeDef(name = "jsonb", typeClass = JsonBlobType.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UsersSummary {

  @Column(name = "login_name", nullable = false)
  private String loginName;

  @Column(name = "user_summary")
  @Type(type = "jsonb")
  private UserSummary userSummary;

  @Id
  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "application")
  private String application;
}
