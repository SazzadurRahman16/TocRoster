package com.bankasia.toc.repository;

import com.bankasia.toc.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByActiveTrueOrderByIdAsc();
    List<Employee> findAllByOrderByIdAsc();
    boolean existsByMobileAndIdNot(String mobile, Long id);
    boolean existsByMobile(String mobile);
}
