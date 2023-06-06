// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import java.util.List;

public interface SmartRoutingService {

  List<String> getSingleApiAccountIdsByPmleId(String pmleId);

}
