// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "digital-wallets")
@RefreshScope
@Data
public class SkrillTellerConfig {

  private Map<String, BusinessUnitConfig> businessUnits = new HashMap<>();

  private String clientId;

  private Boolean includeClientSecret;

  private Map<String, List<String>> linkedBrands = new HashMap<>();

  private Map<String, List<String>> brands = new HashMap<>();

  private String getBusinessUnitFromJwt() {
    return CommonThreadLocal.getAuthLocal().getBusinessUnit() == null
        ? CommonThreadLocal.getAuthLocal().getApplication() : CommonThreadLocal.getAuthLocal().getBusinessUnit();
  }

  public String getHostName() {
    return businessUnits.get(getBusinessUnitFromJwt().toLowerCase()).getHostName();
  }

  public String getHostUri() {
    return getHostName().concat(businessUnits.get(getBusinessUnitFromJwt().toLowerCase()).getBasePath());
  }

  public String getUaaHostUrl() {
    return getHostName().concat(businessUnits.get(getBusinessUnitFromJwt().toLowerCase()).getUaaPath());
  }

  /**
   * Returns list of enabled brands , if no brands are enabled returns empty list.
   */
  public List<String> getEnabledBrands() {

    List<String> enabledBrands = new ArrayList<>();
    for (Map.Entry<String, BusinessUnitConfig> entry : businessUnits.entrySet()) {
      if (!entry.getValue().isDisabled()) {
        enabledBrands.add(entry.getKey());
      }
    }
    return enabledBrands;
  }

  /**
   * Returns List of disabled brands , if no brands are disabled returns empty list.
   */
  public List<String> getDisabledBrands() {

    List<String> disabledBrands = new ArrayList<>();
    for (Map.Entry<String, BusinessUnitConfig> entry : businessUnits.entrySet()) {
      if (entry.getValue().isDisabled()) {
        disabledBrands.add(entry.getKey());
      }
    }
    return disabledBrands;
  }

  /**
   * To know the status of brand enabled status in the environment.
   * 
   * @param brand - businessUnit or brand.
   * @return true if brand is enbaled, else returns false.
   */
  public boolean isBrandEnabled(String brand) {

    return !businessUnits.get(brand).isDisabled();
  }

  public String getAdminRole() {
    return businessUnits.get(getBusinessUnitFromJwt().toLowerCase()).getAdminRole();
  }
}
