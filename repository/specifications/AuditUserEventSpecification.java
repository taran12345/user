// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.repository.specifications;

import com.paysafe.upf.user.provisioning.domain.AuditUserEvent;

import lombok.Data;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Data
public class AuditUserEventSpecification implements Specification<AuditUserEvent> {

  private List<AuditSearchCriteria> searchCriteriaList;

  public AuditUserEventSpecification() {
    this.searchCriteriaList = new ArrayList<>();
  }

  public void add(AuditSearchCriteria criteria) {
    searchCriteriaList.add(criteria);
  }

  @Override
  public Predicate toPredicate(Root<AuditUserEvent> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
    List<Predicate> predicates = new ArrayList<>();
    List<Predicate> orPredicates = new ArrayList<>();
    for (AuditSearchCriteria criteria : searchCriteriaList) {
      if (criteria.getOperation().equals(AuditSearchOperation.GREATER_THAN)) {
        greaterThanPredicate(root, predicates, criteriaBuilder, criteria);
      } else if (criteria.getOperation().equals(AuditSearchOperation.LESS_THAN)) {
        lessThanPredicate(root, predicates, criteriaBuilder, criteria);
      } else if (criteria.getOperation().equals(AuditSearchOperation.EQUAL)) {
        equalsToPredicate(root, predicates, criteriaBuilder, criteria);
      } else if (criteria.getOperation().equals(AuditSearchOperation.GREATER_THAN_EQUAL)) {
        greaterThanEqualPredicate(root, predicates, criteriaBuilder, criteria);
      } else if (criteria.getOperation().equals(AuditSearchOperation.LESS_THAN_EQUAL)) {
        lessThanEqualPredicate(root, predicates, criteriaBuilder, criteria);
      } else if (criteria.getOperation().equals(AuditSearchOperation.NOT_EQUAL)) {
        notEqualsToPredicate(root, predicates, criteriaBuilder, criteria);
      } else if (criteria.getOperation().equals(AuditSearchOperation.OR_EQUAL)) {
        orPredicate(root, orPredicates, criteria, criteriaBuilder);
      }
    }
    if (CollectionUtils.isNotEmpty(orPredicates)) {
      predicates.add(criteriaBuilder.or(orPredicates.toArray(new Predicate[0])));
    }
    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  }

  private void orPredicate(Root<AuditUserEvent> root, List<Predicate> orPredicates, AuditSearchCriteria criteria,
      CriteriaBuilder criteriaBuilder) {
    orPredicates.add(criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue()));
  }

  private void notEqualsToPredicate(Root<AuditUserEvent> root, List<Predicate> predicates,
      CriteriaBuilder criteriaBuilder, AuditSearchCriteria criteria) {
    predicates.add(criteriaBuilder.notEqual(root.get(criteria.getKey()), criteria.getValue()));
  }

  private void greaterThanPredicate(Root<AuditUserEvent> root, List<Predicate> predicates,
      CriteriaBuilder criteriaBuilder, AuditSearchCriteria criteria) {
    if (criteria.getValue() instanceof DateTime) {
      predicates.add(criteriaBuilder.greaterThan(root.get(criteria.getKey()), (DateTime) criteria.getValue()));
    } else {
      predicates.add(criteriaBuilder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString()));
    }
  }

  private void lessThanPredicate(Root<AuditUserEvent> root, List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
      AuditSearchCriteria criteria) {
    if (criteria.getValue() instanceof DateTime) {
      predicates.add(criteriaBuilder.lessThan(root.get(criteria.getKey()), (DateTime) criteria.getValue()));
    } else {
      predicates.add(criteriaBuilder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString()));
    }
  }

  private void equalsToPredicate(Root<AuditUserEvent> root, List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
      AuditSearchCriteria criteria) {
    if (criteria.getValue() instanceof DateTime) {
      predicates.add(criteriaBuilder.between(root.get(criteria.getKey()), (DateTime) criteria.getValue(),
          ((DateTime) criteria.getValue()).plusDays(1)));
    } else {
      predicates.add(criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue()));
    }
  }

  private void greaterThanEqualPredicate(Root<AuditUserEvent> root, List<Predicate> predicates,
      CriteriaBuilder criteriaBuilder, AuditSearchCriteria criteria) {
    if (criteria.getValue() instanceof DateTime) {
      predicates
          .add(criteriaBuilder.greaterThan(root.get(criteria.getKey()), ((DateTime) criteria.getValue()).minusDays(1)));
    } else {
      predicates.add(criteriaBuilder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString()));
    }
  }

  private void lessThanEqualPredicate(Root<AuditUserEvent> root, List<Predicate> predicates,
      CriteriaBuilder criteriaBuilder, AuditSearchCriteria criteria) {
    if (criteria.getValue() instanceof DateTime) {
      predicates
          .add(criteriaBuilder.lessThan(root.get(criteria.getKey()), ((DateTime) criteria.getValue()).plusDays(1)));
    } else {
      predicates.add(criteriaBuilder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString()));
    }
  }
}
