// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository;

import com.paysafe.upf.user.provisioning.domain.WalletPermission;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletPermissionRepository extends CrudRepository<WalletPermission, Integer> {

  List<WalletPermission> findAll();

  @Query(value = "select * from USPR_WALLET_PERMS where PERMISSION in (" + ":permissions" + ")", nativeQuery = true)
  List<WalletPermission> findWalletPermissionsFromPermissions(@Param("permissions") List<String> permissions);
}
