// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.service;

import com.paysafe.upf.user.provisioning.web.rest.dto.AuditUserEventDto;
import com.paysafe.upf.user.provisioning.web.rest.resource.audit.AuditInfoRequestResource;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface AuditService {

  void createAuditEntry(AuditUserEventDto auditUserEventDto);

  ObjectNode getAuditInfo(AuditInfoRequestResource fetchAuditEventRequest);
}
