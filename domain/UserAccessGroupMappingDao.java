// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;

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
@IdClass(UserAccessGroupMappingKey.class)
@Table(name = "user_accessgroups_mapping")
public class UserAccessGroupMappingDao extends Auditable {
  @Id
  @Column(name = "login_name", nullable = false)
  private String loginName;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "user_first_name")
  private String userFirstName;

  @Column(name = "user_last_name")
  private String userLastName;

  @Column(name = "user_external_id")
  private String userExternalId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
  private User user;
  
  @Id
  @Column(name = "acc_grp_code")
  private String accessGroupCode;

  @Column(name = "user_acc_grp_status")
  private AccessResourceStatus userAccessGroupStatus;

  @Column(name = "acc_grp_type")
  private AccessGroupType accessGroupType;

  @Column(name = "acc_grp_res_type")
  private String resourceType;

  @Column(name = "acc_grp_res_id")
  private String resourceId;

  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserExternalId() {
    return userExternalId;
  }

  public void setUserExternalId(String userExternalId) {
    this.userExternalId = userExternalId;
  }

  public String getAccessGroupCode() {
    return accessGroupCode;
  }

  public void setAccessGroupCode(String accessGroupCode) {
    this.accessGroupCode = accessGroupCode;
  }

  public AccessResourceStatus getUserAccessGroupStatus() {
    return userAccessGroupStatus;
  }

  public void setUserAccessGroupStatus(AccessResourceStatus userAccessGroupStatus) {
    this.userAccessGroupStatus = userAccessGroupStatus;
  }

  public AccessGroupType getAccessGroupType() {
    return accessGroupType;
  }

  public void setAccessGroupType(AccessGroupType accessGroupType) {
    this.accessGroupType = accessGroupType;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getUserFirstName() {
    return userFirstName;
  }

  public void setUserFirstName(String userFirstName) {
    this.userFirstName = userFirstName;
  }

  public String getUserLastName() {
    return userLastName;
  }

  public void setUserLastName(String userLastName) {
    this.userLastName = userLastName;
  }
}
