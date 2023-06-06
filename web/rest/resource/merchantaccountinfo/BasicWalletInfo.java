// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource.merchantaccountinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import javax.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class BasicWalletInfo {

  @NotBlank
  private String id;
  private String parentId;
  private BusinessProfile businessProfile;
  private Profile profile;
  private MerchantSettings merchantSettings;
  private List<EwalletAccount> ewalletAccounts;

}
