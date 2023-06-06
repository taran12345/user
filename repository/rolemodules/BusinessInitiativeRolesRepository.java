// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository.rolemodules;

import com.paysafe.upf.user.provisioning.domain.rolemodules.BusinessInitiativeRoles;
import com.paysafe.upf.user.provisioning.domain.rolemodules.BusinessInitiativeRolesKey;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessInitiativeRolesRepository
    extends CrudRepository<BusinessInitiativeRoles, BusinessInitiativeRolesKey> {

  List<BusinessInitiativeRoles> findBybusinessInitiative(String businessInitiative);

  List<BusinessInitiativeRoles> findBybusinessInitiativeIn(List<String> businessInitiative);

  List<BusinessInitiativeRoles> findByRole(String role);

}