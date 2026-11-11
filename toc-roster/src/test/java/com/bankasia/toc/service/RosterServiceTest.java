package com.bankasia.toc.service;

import com.bankasia.toc.domain.Employee;
import com.bankasia.toc.domain.ShiftType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test")
@Transactional
class RosterServiceTest {

    @Autowired RosterService roster;

    private static List<String> names(List<Employee> l) { return l.stream().map(Employee::getName).toList(); }

    @Test
    void seededNovemberMatchesPdf() {
        var rows = roster.monthRoster(YearMonth.of(2026, 11));
        assertEquals(30, rows.size());
        var nov13 = rows.get(12);                       // Friday, single-person day
        assertTrue(nov13.isWeeklyOff());
        assertEquals(List.of("Monjurul Murshid"), names(nov13.shifts().get(ShiftType.MORNING)));
        assertEquals(List.of("Gaji Nurer Nobi"), names(nov13.shifts().get(ShiftType.NIGHT)));
        assertEquals(4, rows.get(0).shifts().get(ShiftType.MORNING).size());
    }

    @Test
    void nightShiftAfterMidnightUsesPreviousDay() {
        // 2-Nov 03:00 -> night duty of 1-Nov (Md. Mokarrom Hosen)
        Map<ShiftType, List<Employee>> duty = roster.onDutyAt(LocalDateTime.of(2026, 11, 2, 3, 0));
        assertEquals(List.of("Md. Mokarrom Hosen"), names(duty.get(ShiftType.NIGHT)));
        assertFalse(duty.containsKey(ShiftType.MORNING));
    }

    @Test
    void overlappingShiftsAtThreePm() {
        var duty = roster.onDutyAt(LocalDateTime.of(2026, 11, 3, 15, 0));
        assertTrue(duty.containsKey(ShiftType.MORNING));
        assertEquals(List.of("Gaji Nurer Nobi"), names(duty.get(ShiftType.EVENING)));
    }

    @Test
    void saveDayReplacesAssignments() {
        LocalDate d = LocalDate.of(2026, 11, 7);
        var current = roster.day(d).shifts().get(ShiftType.MORNING).get(0).getId();
        roster.saveDay(d, Map.of(ShiftType.MORNING, List.of(current, current), ShiftType.EVENING, List.of(),
                ShiftType.NIGHT, List.of()));
        var day = roster.day(d);
        assertEquals(1, day.shifts().get(ShiftType.MORNING).size());     // duplicates ignored
        assertTrue(day.shifts().get(ShiftType.EVENING).isEmpty());
    }
}
