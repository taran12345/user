// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository;

import com.paysafe.upf.user.provisioning.domain.UserToken;
import com.paysafe.upf.user.provisioning.domain.UserTokenKey;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface TokenRepository extends CrudRepository<UserToken, UserTokenKey> {

  List<UserToken> findByLoginNameAndTokenTypeAndApplication(String loginName, String tokenType, String application);

  UserToken findByLoginNameAndTokenAndTokenTypeAndApplication(String userName, String tokenId, String tokenType,
      String application);
}
