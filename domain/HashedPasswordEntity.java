// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "uspr_hashed_passwords")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HashedPasswordEntity {

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "id", columnDefinition = "VARCHAR(255)")
  @Type(type = "org.hibernate.type.UUIDCharType")
  private UUID id;

  @Column(name = "algorithm")
  private String algorithm;

  @Column(name = "work_factor")
  private Integer workFactor;

  @Column(name = "salt")
  private String salt;

  @Column(name = "salt_order")
  private String saltOrder;

  @Column(name = "value")
  private String value;

}
