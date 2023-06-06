// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uspr_wallet_perms")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletPermission {

  @Id
  @Column(name = "internal_id", nullable = false)
  private int internalId;

  @Column(name = "permission", nullable = false)
  private String permission;

  @Column(name = "perm_desc", nullable = false)
  private String permissionDesription;

  @Column(name = "enabled", nullable = false)
  private int enabled;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @Column(name = "language")
  private String language;
}
