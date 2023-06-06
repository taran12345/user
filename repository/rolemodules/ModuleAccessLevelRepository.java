// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository.rolemodules;

import com.paysafe.upf.user.provisioning.domain.rolemodules.ModuleAccessLevel;
import com.paysafe.upf.user.provisioning.domain.rolemodules.ModuleAccessLevelKey;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ModuleAccessLevelRepository extends CrudRepository<ModuleAccessLevel, ModuleAccessLevelKey> {
  List<ModuleAccessLevel> findBymoduleId(String moduleId);
}
