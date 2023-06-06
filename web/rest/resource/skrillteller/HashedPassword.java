// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.skrillteller;

public class HashedPassword {

  private String algorithm;
  private Integer workFactor;
  private String salt;
  private String saltOrder;
  private String value;

  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public Integer getWorkFactor() {
    return workFactor;
  }

  public void setWorkFactor(Integer workFactor) {
    this.workFactor = workFactor;
  }

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getSaltOrder() {
    return saltOrder;
  }

  public void setSaltOrder(String saltOrder) {
    this.saltOrder = saltOrder;
  }
}
