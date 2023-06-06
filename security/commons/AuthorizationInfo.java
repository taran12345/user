// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.security.commons;

import com.paysafe.gbp.commons.bigdata.Issuer;
import com.paysafe.upf.user.provisioning.enums.BusinessUnit;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * This class is used to store the user details in the Thread Local variable. Additional user params can be added to
 * this class in the future if needed.
 *
 * @author meharchandra
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AuthorizationInfo {
  String authHeader;

  String application;

  @JsonProperty("access_groups")
  List<String> accessGroups;

  @JsonProperty("user_name")
  String userName;

  String appName;

  List<String> authorities;

  @JsonProperty("client_id")
  String clientId;

  @JsonProperty("pmle_id")
  Long pmleId;

  @JsonProperty("owner_id")
  String ownerId;

  @JsonProperty("owner_type")
  String ownerType;

  @JsonProperty("iss")
  String iss;

  @JsonProperty("paysafe_id")
  String paysafeId;

  Issuer issuer;

  String businessUnit;

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getAuthHeader() {
    return authHeader;
  }

  public void setAuthHeader(String authHeader) {
    this.authHeader = authHeader;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public List<String> getAuthorities() {
    return authorities;
  }

  public void setAuthorities(List<String> authorities) {
    this.authorities = authorities;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public Long getPmleId() {
    return pmleId;
  }

  public void setPmleId(Long pmleId) {
    this.pmleId = pmleId;
  }

  public List<String> getAccessGroups() {
    return accessGroups;
  }

  public void setAccessGroups(List<String> accessGroups) {
    this.accessGroups = accessGroups;
  }

  public String getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  public String getOwnerType() {
    return ownerType;
  }

  public void setOwnerType(String ownerType) {
    this.ownerType = ownerType;
  }

  public String getIss() {
    return iss;
  }

  public void setIss(String iss) {
    this.iss = iss;
  }

  public String getPaysafeId() {
    return paysafeId;
  }

  public void setPaysafeId(String paysafeId) {
    this.paysafeId = paysafeId;
  }

  /**
   * To get issuer.
   *
   * @return Issuer
   */
  public Issuer getIssuer() {
    if (StringUtils.containsIgnoreCase(iss, Issuer.OKTA.toString())) {
      this.setIssuer(Issuer.OKTA);
      return Issuer.OKTA;
    } else {
      this.setIssuer(Issuer.UAA);
      return Issuer.UAA;
    }
  }

  public void setIssuer(Issuer issuer) {
    this.issuer = issuer;
  }

  /**
   * method to get businessUnit.
   */
  public String getBusinessUnit() {
    if (StringUtils.isNotEmpty(businessUnit)) {
      return businessUnit;
    } else {
      if (StringUtils.equals(application, DataConstants.SKRILL)
          || StringUtils.equals(application, DataConstants.NETELLER)) {
        return application;
      } else {
        return BusinessUnit.EU_ACQUIRING_EEA.toString();
      }
    }
  }

  public void setBusinessUnit(String businessUnit) {
    this.businessUnit = businessUnit;
  }
}
