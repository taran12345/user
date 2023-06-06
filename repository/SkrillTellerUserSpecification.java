// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository;

import com.paysafe.op.errorhandling.exceptions.BadRequestException;
import com.paysafe.upf.user.provisioning.domain.User;
import com.paysafe.upf.user.provisioning.domain.UserAccessGroupMappingDao;
import com.paysafe.upf.user.provisioning.enums.AccessGroupType;
import com.paysafe.upf.user.provisioning.enums.AccessResourceStatus;
import com.paysafe.upf.user.provisioning.enums.Status;
import com.paysafe.upf.user.provisioning.web.rest.constants.DataConstants;
import com.paysafe.upf.user.provisioning.web.rest.dto.UserFetchByFiltersRequestDto;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Locale;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

@Component
public class SkrillTellerUserSpecification {

  /**
   * Constructs spec for fetching users from the request resource.
   * 
   * @param userFetchByFiltersRequestDto {@link UserFetchByFiltersRequestDto}
   * @return UserSpecification
   */
  public Specification<User> constructFetchSkrillTellerUsersSpecification(
      UserFetchByFiltersRequestDto userFetchByFiltersRequestDto) {
    return (root, query, cb) -> {
      Join<User, UserAccessGroupMappingDao> join = root.join("accessGroupMappingDaos");
      Predicate applicationPredicate = cb.conjunction();
      Predicate userIdentifierPredicate = cb.conjunction();
      Predicate loginNamePredicate = cb.conjunction();
      Predicate emailPredicate = cb.conjunction();
      Predicate firstNamePredicate = cb.conjunction();
      Predicate lastNamePredicate = cb.conjunction();
      Predicate statusPredicate = cb.conjunction();
      Predicate createdByPredicate = cb.conjunction();
      Predicate createdDatePredicate = cb.conjunction();
      Predicate rolePredicate = cb.conjunction();
      Predicate resourceTypePredicate = cb.conjunction();
      Predicate resourceIdPredicate = cb.conjunction();
      Predicate disabledBrandsPredicate = cb.conjunction();

      if (StringUtils.isNotEmpty(userFetchByFiltersRequestDto.getApplication())) {
        applicationPredicate.getExpressions()
            .add(cb.equal(root.get("application"), userFetchByFiltersRequestDto.getApplication()));
      }

      if (userFetchByFiltersRequestDto.getUserIdentifier() != null) {
        loginNamePredicate.getExpressions()
            .add(cb.like(cb.upper(root.get("loginName")),
                "%" + userFetchByFiltersRequestDto.getUserIdentifier().toUpperCase() + "%"));
        emailPredicate.getExpressions()
            .add(cb.like(cb.upper(root.get("email")),
                "%" + userFetchByFiltersRequestDto.getUserIdentifier().toUpperCase() + "%"));
        firstNamePredicate.getExpressions()
            .add(cb.like(cb.upper(root.get("userFirstName")),
                "%" + userFetchByFiltersRequestDto.getUserIdentifier().toUpperCase() + "%"));
        lastNamePredicate.getExpressions()
            .add(cb.like(cb.upper(root.get("userLastName")),
                "%" + userFetchByFiltersRequestDto.getUserIdentifier().toUpperCase() + "%"));
        userIdentifierPredicate = cb.or(loginNamePredicate, emailPredicate, firstNamePredicate, lastNamePredicate);
      }

      if (userFetchByFiltersRequestDto.getStatus() != null) {
        Status status = userFetchByFiltersRequestDto.getStatus();
        if (StringUtils.isEmpty(userFetchByFiltersRequestDto.getUserIdentifier())
            && StringUtils.isEmpty(userFetchByFiltersRequestDto.getResourceId())) {
          statusPredicate.getExpressions().add(cb.equal(root.get("status"), userFetchByFiltersRequestDto.getStatus()));
        } else if (StringUtils.isNotEmpty(userFetchByFiltersRequestDto.getUserIdentifier())) {
          if (status.compareTo(Status.ACTIVE) == 0) {
            statusPredicate.getExpressions()
                .add(cb.equal(root.get("status"), userFetchByFiltersRequestDto.getStatus()));
            statusPredicate.getExpressions()
                .add(cb.equal(join.get("userAccessGroupStatus"), AccessResourceStatus.ACTIVE));
          } else if (status.compareTo(Status.SUSPENDED) == 0) {
            statusPredicate.getExpressions()
                .add(cb.equal(join.get("userAccessGroupStatus"), AccessResourceStatus.BLOCKED));
          } else {
            statusPredicate.getExpressions()
                .add(cb.equal(root.get("status"), userFetchByFiltersRequestDto.getStatus()));
          }
        } else if (StringUtils.isNotEmpty(userFetchByFiltersRequestDto.getResourceId())) {
          AccessResourceStatus accessResourceStatus;
          if (userFetchByFiltersRequestDto.getStatus().equals(Status.ACTIVE)) {
            accessResourceStatus = AccessResourceStatus.ACTIVE;
          } else if (userFetchByFiltersRequestDto.getStatus().equals(Status.SUSPENDED)) {
            accessResourceStatus = AccessResourceStatus.BLOCKED;
          } else {
            throw BadRequestException.builder()
                .details("Invalid status value for wallets. Allowed is only BLOCKED / ACTIVE ").build();
          }
          statusPredicate.getExpressions().add(cb.equal(join.get("userAccessGroupStatus"), accessResourceStatus));
        }
      }

      if (StringUtils.isNotEmpty(userFetchByFiltersRequestDto.getResourceType())
          && StringUtils.isNotEmpty(userFetchByFiltersRequestDto.getResourceId())) {
        resourceTypePredicate.getExpressions().add(cb.equal(join.get("resourceType"), DataConstants.WALLETS));
        resourceIdPredicate.getExpressions()
            .add(cb.equal(join.get("resourceId"), userFetchByFiltersRequestDto.getResourceId()));
      }

      if (userFetchByFiltersRequestDto.getCreatedBy() != null) {
        createdByPredicate.getExpressions().add(cb.like(cb.lower(root.get("createdBy")),
            "%" + userFetchByFiltersRequestDto.getCreatedBy().toLowerCase(Locale.ENGLISH) + "%"));
      }

      if (userFetchByFiltersRequestDto.getRole() != null) {
        if (userFetchByFiltersRequestDto.getRole().equals("ADMIN")) {
          rolePredicate.getExpressions().add(cb.equal(join.get("accessGroupType"), AccessGroupType.DEFAULT_ADMIN));
        } else if (userFetchByFiltersRequestDto.getRole().equals("REGULAR")) {
          rolePredicate.getExpressions().add(cb.equal(join.get("accessGroupType"), AccessGroupType.CUSTOMIZED));
        } else {
          throw BadRequestException.builder().details("Invalid role. Allowed is only ADMIN / REGULAR ").build();
        }
      }
      
      if (userFetchByFiltersRequestDto.getCreatedDate() != null) {
        DateTime dateTime = userFetchByFiltersRequestDto.getCreatedDate();
        createdDatePredicate.getExpressions().add(cb.between(root.get("createdDate"), dateTime.withTimeAtStartOfDay(),
            dateTime.plusDays(1).withTimeAtStartOfDay()));
      }

      if (CollectionUtils.isNotEmpty(userFetchByFiltersRequestDto.getDisabledBrands())) {
        disabledBrandsPredicate.getExpressions()
            .add(root.get("businessUnit").in(userFetchByFiltersRequestDto.getDisabledBrands()).not());
      }

      query.distinct(true);

      return cb.and(applicationPredicate, statusPredicate, createdByPredicate, createdDatePredicate,
          userIdentifierPredicate, rolePredicate, resourceTypePredicate, resourceIdPredicate, disabledBrandsPredicate);

    };
  }

}
