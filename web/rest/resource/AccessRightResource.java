// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(Include.NON_NULL)
@Data
@AllArgsConstructor
public class AccessRightResource {

  @NotBlank
  @ApiModelProperty(value = "Type of the resource", required = true, example = "FMA, MLE, PMLE")
  private String resourceType;

  @NotBlank
  @ApiModelProperty(value = "Id of the resource", required = true, example = "FMA account id if RESOURCE_TYPE is FMA")
  private String resourceId;

  @NotNull
  @ApiModelProperty(value = "Number priority indicating action allowed on the resource", required = true, example = "0")
  private String accessTypeValue;

  @NotNull
  @ApiModelProperty(value = "Status", required = true, example = "ACTIVE, DELETED, INACTIVE")
  private String status;

  private String accessRole;

}
