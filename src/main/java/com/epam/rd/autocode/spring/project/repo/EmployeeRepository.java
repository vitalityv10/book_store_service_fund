package com.epam.rd.autocode.spring.project.repo;
import com.epam.rd.autocode.spring.project.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>,
        QuerydslPredicateExecutor<Employee> {
    Optional<Employee> findByEmail(String email);
}
