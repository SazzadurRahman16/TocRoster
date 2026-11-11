package com.bankasia.toc.repository;

import com.bankasia.toc.domain.DutyAssignment;
import com.bankasia.toc.domain.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DutyAssignmentRepository extends JpaRepository<DutyAssignment, Long> {

    List<DutyAssignment> findByDutyDateBetweenOrderByDutyDateAscEmployeeIdAsc(LocalDate from, LocalDate to);

    List<DutyAssignment> findByDutyDateOrderByEmployeeIdAsc(LocalDate date);

    boolean existsByEmployeeId(Long employeeId);

    /** Bulk delete runs immediately, so re-inserting the same rows never hits the unique constraint. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from DutyAssignment d where d.dutyDate = :date and d.shift = :shift")
    void deleteSlot(@Param("date") LocalDate date, @Param("shift") ShiftType shift);
}
