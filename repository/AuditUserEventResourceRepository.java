// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository;

import com.paysafe.upf.user.provisioning.domain.AuditUserEventResource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditUserEventResourceRepository extends JpaRepository<AuditUserEventResource, String> {

}
