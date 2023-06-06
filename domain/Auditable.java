// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import lombok.Data;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable {

  @CreatedDate
  @Column(name = "created_datetime")
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  protected DateTime createdDate;

  @LastModifiedDate
  @Column(name = "last_modified_datetime")
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  protected DateTime lastModifiedDate;

  @CreatedBy
  protected String createdBy;

  @LastModifiedBy
  protected String lastModifiedBy;

  protected Auditable() {
    // This constructor is intentionally empty.
  }
}
