// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import java.util.List;

public class PegasusUserListResponseResource {

  private List<PegasusUserResponseResource> users;

  private Long count;

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public List<PegasusUserResponseResource> getUsers() {
    return users;
  }

  public void setUsers(List<PegasusUserResponseResource> users) {
    this.users = users;
  }

}
