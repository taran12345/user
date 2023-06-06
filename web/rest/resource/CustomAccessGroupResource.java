// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.Status;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomAccessGroupResource {
  @JsonProperty(value = "name", required = true)
  @Pattern(regexp = "[A-Za-z0-9-_]*", message = "Access group name can contain a-z,A-Z,0-9,- and _.")
  @Size(max = 200)
  private String name;

  @NotEmpty
  @Size(max = 200)
  private String description;

  @NotEmpty
  @JsonProperty(value = "accessPolicyIds", required = true)
  private List<String> accessPolicyIds;

  @JsonProperty("status")
  @NotNull(message = "Invalid value, valid values are [ACTIVE, INACTIVE, DELETED]")
  private Status status;

  @NotEmpty
  private String merchantType;

  @NotEmpty
  private String merchantId;

  @NotNull
  private AccessGroupType type;
}
