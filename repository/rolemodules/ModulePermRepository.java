// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository.rolemodules;

import com.paysafe.upf.user.provisioning.domain.rolemodules.ModulePermissionKey;
import com.paysafe.upf.user.provisioning.domain.rolemodules.ModulePermissions;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModulePermRepository extends CrudRepository<ModulePermissions, ModulePermissionKey> {

  List<ModulePermissions> findBymoduleId(String moduleId);
}