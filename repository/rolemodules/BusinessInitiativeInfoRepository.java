// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository.rolemodules;

import com.paysafe.upf.user.provisioning.domain.rolemodules.BusinessInitiativeInfo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessInitiativeInfoRepository extends CrudRepository<BusinessInitiativeInfo, String> {

}