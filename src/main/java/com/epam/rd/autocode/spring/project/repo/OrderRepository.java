package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Order;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository  extends JpaRepository<Order, UUID>,
        QuerydslPredicateExecutor<Order> {
    @Query(value = "SELECT o FROM Order o " +
            "JOIN FETCH o.client " +
            "LEFT JOIN FETCH o.employee",
            countQuery = "SELECT count(o) FROM Order o")
    Page<Order> getAll(Pageable pageable, Predicate predicate);

    Optional<Order> getOrderByIdIs(UUID id);
    boolean existsByClientIdAndOrderStatusNotIn(UUID clientId, List<String> status);

    boolean existsByEmployeeId(UUID employeeId);
}
