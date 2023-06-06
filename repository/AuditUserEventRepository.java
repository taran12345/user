// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository;

import com.paysafe.upf.user.provisioning.domain.AuditUserEvent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditUserEventRepository
    extends JpaRepository<AuditUserEvent, String>, JpaSpecificationExecutor<AuditUserEvent> {

}
