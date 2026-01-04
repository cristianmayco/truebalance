package com.truebalance.truebalance.infra.db.specification;

import com.truebalance.truebalance.infra.db.entity.BillEntity;
import com.truebalance.truebalance.infra.db.entity.InstallmentEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillSpecification {

    public static Specification<BillEntity> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<BillEntity> hasStartDate(LocalDateTime startDate) {
        return (root, query, cb) -> {
            if (startDate == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("executionDate"), startDate);
        };
    }

    public static Specification<BillEntity> hasEndDate(LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (endDate == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("executionDate"), endDate);
        };
    }

    public static Specification<BillEntity> hasMinAmount(BigDecimal minAmount) {
        return (root, query, cb) -> {
            if (minAmount == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("totalAmount"), minAmount);
        };
    }

    public static Specification<BillEntity> hasMaxAmount(BigDecimal maxAmount) {
        return (root, query, cb) -> {
            if (maxAmount == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("totalAmount"), maxAmount);
        };
    }

    public static Specification<BillEntity> hasNumberOfInstallments(Integer numberOfInstallments) {
        return (root, query, cb) -> {
            if (numberOfInstallments == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("numberOfInstallments"), numberOfInstallments);
        };
    }

    public static Specification<BillEntity> hasCategory(String category) {
        return (root, query, cb) -> {
            if (category == null || category.trim().isEmpty()) {
                return cb.conjunction();
            }
            // Valor especial para filtrar contas sem categoria
            if ("__NO_CATEGORY__".equals(category)) {
                return cb.or(
                        cb.isNull(root.get("category")),
                        cb.equal(root.get("category"), "")
                );
            }
            return cb.and(
                    cb.isNotNull(root.get("category")),
                    cb.equal(cb.lower(root.get("category")), category.toLowerCase())
            );
        };
    }

    public static Specification<BillEntity> hasCreditCard(Long creditCardId) {
        return (root, query, cb) -> {
            if (creditCardId == null) {
                return cb.conjunction();
            }
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<InstallmentEntity> installmentRoot = subquery.from(InstallmentEntity.class);
            subquery.select(installmentRoot.get("billId"))
                    .where(cb.and(
                            cb.equal(installmentRoot.get("billId"), root.get("id")),
                            cb.equal(installmentRoot.get("creditCardId"), creditCardId)
                    ));
            return cb.exists(subquery);
        };
    }

    public static Specification<BillEntity> hasCreditCardFilter(Boolean hasCreditCard) {
        return (root, query, cb) -> {
            if (hasCreditCard == null) {
                return cb.conjunction();
            }
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<InstallmentEntity> installmentRoot = subquery.from(InstallmentEntity.class);
            subquery.select(installmentRoot.get("billId"))
                    .where(cb.and(
                            cb.equal(installmentRoot.get("billId"), root.get("id")),
                            cb.isNotNull(installmentRoot.get("creditCardId"))
                    ));
            
            if (hasCreditCard) {
                return cb.exists(subquery);
            } else {
                return cb.not(cb.exists(subquery));
            }
        };
    }
}
