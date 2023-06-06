// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.web.rest.dto.AccessResources;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserUpdationDto;

import java.util.List;

public interface AccessGroupService {

  public List<AccessResources> createAccessGroupsFromResouresList(UserDto userDto);

  public void createAccessGroupsForUpdateUser(String userId, UserUpdationDto userUpdationDto);
}
