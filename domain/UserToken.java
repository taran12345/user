// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.domain;

import lombok.Data;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@EntityListeners(AuditingEntityListener.class)
@IdClass(UserTokenKey.class)
@Table(name = "uspr_user_tokens")
@Data
public class UserToken {

  public static final Integer TOKEN_EXPIRATION_PERIOD = 5;

  @Id
  @Column(name = "login_name", nullable = false)
  private String loginName;

  @Id
  @Column(name = "token", nullable = false)
  private String token;

  @Column(name = "token_type", nullable = false)
  private String tokenType;

  @Column(name = "expiry_date")
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  private DateTime expiryDate;

  @Column(name = "auth_contact_name", nullable = true)
  private String authContactName;

  @Column(name = "auth_contact_email", nullable = true)
  private String authContactEmail;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "created_date")
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  private DateTime createdDate;

  @Column(name = "last_modified_by")
  private String lastModifiedBy;

  @Column(name = "last_modified_date")
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  private DateTime lastModifiedDate;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "application")
  private String application;

  public UserToken() {
    // Intentionally empty.
  }

  /**
   * Creates a new user token.
   *
   * @param loginName the name of the user
   * @param token the token
   * @param tokenType the token type
   */
  public UserToken(String loginName, String token, String tokenType) {
    this.loginName = loginName;
    this.token = token;
    this.tokenType = tokenType;
    this.expiryDate = new DateTime().plusYears(TOKEN_EXPIRATION_PERIOD);
    this.createdDate = DateTime.now();
    this.createdBy = loginName;
    this.lastModifiedBy = loginName;
    this.lastModifiedDate = DateTime.now();
  }
}
