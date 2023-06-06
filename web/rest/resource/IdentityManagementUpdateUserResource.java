// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.domain.RecoveryQuestion;
import com.paysafe.upf.user.provisioning.enums.UserStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdentityManagementUpdateUserResource {
  @NotBlank
  private String email;
  @NotBlank
  private String userName;
  private String id;
  private String firstName;
  private String lastName;
  private String password;
  private UserStatus status;
  private String mobilePhone;
  private Set<String> groupIds;
  private String businessUnit;
  private String pmleId;
  private List<UpdateActionResource> roles;
  private List<UpdateActionResource> accessGroups;
  private RecoveryQuestion recoveryQuestion;
  private Boolean activate;
  private String externalId;
  private Map<String, Object> customProperties;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public String getMobilePhone() {
    return mobilePhone;
  }

  public void setMobilePhone(String mobilePhone) {
    this.mobilePhone = mobilePhone;
  }

  public Set<String> getGroupIds() {
    return groupIds;
  }

  public void setGroupIds(Set<String> groupIds) {
    this.groupIds = groupIds;
  }

  public String getPmleId() {
    return pmleId;
  }

  public void setPmleId(String pmleId) {
    this.pmleId = pmleId;
  }

  public List<UpdateActionResource> getRoles() {
    return roles;
  }

  public void setRoles(List<UpdateActionResource> roles) {
    this.roles = roles;
  }

  public List<UpdateActionResource> getAccessGroups() {
    return accessGroups;
  }

  public void setAccessGroups(List<UpdateActionResource> accessGroups) {
    this.accessGroups = accessGroups;
  }

  public RecoveryQuestion getRecoveryQuestion() {
    return recoveryQuestion;
  }

  public void setRecoveryQuestion(RecoveryQuestion recoveryQuestion) {
    this.recoveryQuestion = recoveryQuestion;
  }

  public Boolean getActivate() {
    return activate;
  }

  public void setActivate(Boolean activate) {
    this.activate = activate;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public Map<String, Object> getCustomProperties() {
    return customProperties;
  }

  public void setCustomProperties(Map<String, Object> customProperties) {
    this.customProperties = customProperties;
  }

}
