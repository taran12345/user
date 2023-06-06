// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository;

import com.paysafe.upf.user.provisioning.domain.UserAssignedApplications;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface UserAssignedApplicationsRepository extends CrudRepository<UserAssignedApplications, UUID> {

  @Modifying
  @Query("delete from UserAssignedApplications n where n.userId = :userId")
  void deleteByUserId(@Param("userId") String userId);

  @Modifying
  @Transactional
  @Query("delete from UserAssignedApplications n where n.userId = :userId and n.application = :application")
  void deleteByUserIdAndApplication(@Param("userId") String userId, @Param("application") String application);
}
