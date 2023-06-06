// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.web.rest.resource;

import com.paysafe.upf.user.provisioning.domain.AuditUserEvent;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AuditUserEventsResponse {

  private List<AuditUserEvent> auditUserEventsList = new ArrayList<>();

  private Long totalElements;

  private boolean hasNext;

  private boolean hasPrevious;
}
