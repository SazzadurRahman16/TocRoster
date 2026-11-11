package com.bankasia.toc.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "duty_assignment",
       uniqueConstraints = @UniqueConstraint(columnNames = {"duty_date", "shift", "employee_id"}),
       indexes = @Index(name = "idx_duty_date", columnList = "duty_date"))
@Getter @Setter @NoArgsConstructor
public class DutyAssignment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "duty_date", nullable = false)
    private LocalDate dutyDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private ShiftType shift;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public DutyAssignment(LocalDate dutyDate, ShiftType shift, Employee employee) {
        this.dutyDate = dutyDate;
        this.shift = shift;
        this.employee = employee;
    }
}
