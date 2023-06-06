// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.Getter;
import lombok.Setter;

import java.util.Locale;
import java.util.Map;

@Getter
@Setter
public class BaseRecipient {

  @JsonProperty(value = "locale")
  @JsonPropertyDescription(value = "Locale of the recipient")
  private Locale locale;

  @JsonProperty(value = "variables")
  @JsonPropertyDescription(value = "variable values used for template substitution. key-value pairs")
  private Map<String, Object> variables;

  @JsonProperty(value = "failoverRecipient")
  @JsonPropertyDescription(value = "failover recipient. object type would depend on failover channel")
  private Map<String, Object> failoverRecipient;

}
