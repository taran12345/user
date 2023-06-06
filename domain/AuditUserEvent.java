// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import com.paysafe.upf.user.provisioning.enums.AuditEventStatus;
import com.paysafe.upf.user.provisioning.enums.AuditEventType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.IOException;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "audit_user_event")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuditUserEvent {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Id
  @Column(name = "id", unique = true)
  private String id;

  @Column(name = "event_type")
  @Enumerated(EnumType.STRING)
  private AuditEventType eventType;

  @Column(name = "target_user_name")
  private String targetUserName;

  @Column(name = "target_user_id")
  private String targetUserId;

  @Column(name = "event_status")
  @Enumerated(EnumType.STRING)
  private AuditEventStatus eventStatus;

  @Column(name = "browser")
  private String browser;

  @Column(name = "user_ip_address")
  private String userIpAddress;

  @Column(name = "source_app")
  private String sourceApp;

  @Column(name = "user_status")
  private String userStatus;

  @Column(name = "application")
  private String application;

  @JsonIgnore
  @Column(name = "event_data")
  private String eventData;

  @Column(name = "created_by")
  private String createdBy;

  @JsonManagedReference
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "auditUserEvent", cascade = CascadeType.ALL)
  private List<AuditUserEventResource> auditUserEventResources;

  @JsonIgnore
  @Column(name = "event_timestamp")
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  private DateTime eventTimeStamp;

  @JsonIgnore
  @Column(name = "created_date")
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  private DateTime createdDate;

  @Column(name = "correlation_id")
  private String correlationId;

  /**
   * convert string to json.
   */
  @JsonProperty("eventData")
  public ObjectNode getEventDataJson() throws IOException {

    return eventData == null ? null : MAPPER.readValue(eventData, ObjectNode.class);
  }

  /**
   * createdDate in millis.
   */
  @JsonProperty("createdDate")
  public Long getCreatedDate() {
    return createdDate == null ? null : createdDate.getMillis();
  }

  /**
   * eventTimeStamp in millis.
   */
  @JsonProperty("eventTimeStamp")
  public Long getEventTimeStamp() {

    return eventTimeStamp == null ? null : eventTimeStamp.getMillis();

  }
}
