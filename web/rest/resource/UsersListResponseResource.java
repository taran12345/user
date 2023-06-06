// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import java.util.ArrayList;
import java.util.List;

public class UsersListResponseResource {

  List<UserResponseResource> users = new ArrayList<>();
  Long count;

  public List<UserResponseResource> getUsers() {
    return users;
  }

  public void setUsers(List<UserResponseResource> users) {
    this.users = users;
  }

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }
}
