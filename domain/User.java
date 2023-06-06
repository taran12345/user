// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import com.paysafe.upf.user.provisioning.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "uspr_users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public class User extends Auditable {

  @Id
  @Column(name = "user_id")
  private String userId;

  @Column(name = "login_name", nullable = false)
  private String loginName;

  @Column(name = "owner_type")
  private String ownerType;

  @Column(name = "owner_id")
  private String ownerId;

  @Column(name = "email")
  private String email;

  @Column(name = "user_external_id")
  private String userExternalId;

  @Column(name = "user_first_name")
  private String userFirstName;

  @Column(name = "user_last_name")
  private String userLastName;

  @Column(name = "roles_assigned")
  private String rolesAssigned;

  @Column(name = "application")
  private String application;

  @Column(name = "acc_grps_assigned")
  private String accessGroupsAssigned;

  @Column(name = "status")
  private Status status;

  @Column(name = "division")
  private String division;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserAccessGroupMappingDao> accessGroupMappingDaos;

  @Column(name = "comments")
  private String additionalComments;

  @Column(name = "business_unit")
  private String businessUnit;

  @OneToOne(cascade = CascadeType.REMOVE)
  private HashedPasswordEntity hashedPassword;

  @Column(name = "migrated_flag")
  private String migratedFlag;

  @Column(name = "region")
  private String region;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserAssignedApplications> userAssignedApplications;

  @Column(name = "mfa_enabled")
  private String mfaEnabled;
}
