// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventResourceDto;

import java.util.List;

public interface AsyncAuditService {
  void createAuditEntry(AuditUserEventDto auditUserEventDto,
      List<AuditUserEventResourceDto> auditUserEventResourceDtos);
}
