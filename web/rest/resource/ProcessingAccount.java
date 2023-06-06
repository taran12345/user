// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingAccount {

  private String id;
  private String pmleId;
  private String pmleName;
  private SourceAuthority sourceAuthority;
  private BusinessDetails businessDetails;
  private String type;
  private String currency;
  private Status status;

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class BusinessDetails {
    private LegalEntity legalEntity;
    private OnboardingInformation onboardingInformation;
    private List<String> tags;
    private List<String> accountGroups;
    private String tradeName;
    private String businessRelationName;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class LegalEntity {
    private String id;
    private String description;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class OnboardingInformation {
    private String partnerId;
    private String partnerName;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Status {
    private String code;
  }
}
