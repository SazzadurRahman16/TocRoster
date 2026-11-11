package com.bankasia.toc.service;

import com.bankasia.toc.domain.Employee;
import com.bankasia.toc.repository.DutyAssignmentRepository;
import com.bankasia.toc.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EmployeeService {

    private final EmployeeRepository employees;
    private final DutyAssignmentRepository assignments;

    public EmployeeService(EmployeeRepository employees, DutyAssignmentRepository assignments) {
        this.employees = employees;
        this.assignments = assignments;
    }

    public List<Employee> findAll() { return employees.findAllByOrderByIdAsc(); }
    public List<Employee> findActive() { return employees.findByActiveTrueOrderByIdAsc(); }

    public Employee get(Long id) {
        return employees.findById(id).orElseThrow(() -> new NoSuchElementException("Employee " + id + " not found"));
    }

    @Transactional
    public Employee save(Employee e) {
        boolean duplicate = e.getId() == null
                ? employees.existsByMobile(e.getMobile())
                : employees.existsByMobileAndIdNot(e.getMobile(), e.getId());
        if (duplicate) throw new IllegalArgumentException("Another employee already uses this mobile number");
        return employees.save(e);
    }

    @Transactional
    public void setActive(Long id, boolean active) { get(id).setActive(active); }

    /** Employees with roster history are deactivated, never deleted, so past rosters stay intact. */
    @Transactional
    public void remove(Long id) {
        Employee e = get(id);
        if (assignments.existsByEmployeeId(id)) e.setActive(false);
        else employees.delete(e);
    }
}
