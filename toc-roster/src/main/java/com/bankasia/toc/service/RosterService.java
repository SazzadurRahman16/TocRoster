package com.bankasia.toc.service;

import com.bankasia.toc.domain.DutyAssignment;
import com.bankasia.toc.domain.Employee;
import com.bankasia.toc.domain.ShiftType;
import com.bankasia.toc.repository.DutyAssignmentRepository;
import com.bankasia.toc.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RosterService {

    /** One calendar row of the monthly roster. */
    public record DayRow(LocalDate date, Map<ShiftType, List<Employee>> shifts) {
        public DayOfWeek dayOfWeek() { return date.getDayOfWeek(); }
        public boolean isWeeklyOff() { return date.getDayOfWeek() == DayOfWeek.FRIDAY; }
    }

    private final DutyAssignmentRepository assignments;
    private final EmployeeRepository employees;
    private final ZoneId zone;

    public RosterService(DutyAssignmentRepository assignments, EmployeeRepository employees,
                         @Value("${app.zone:Asia/Dhaka}") String zone) {
        this.assignments = assignments;
        this.employees = employees;
        this.zone = ZoneId.of(zone);
    }

    public LocalDate today() { return LocalDate.now(zone); }
    public YearMonth currentMonth() { return YearMonth.now(zone); }

    @Transactional(readOnly = true)
    public List<DayRow> monthRoster(YearMonth month) {
        Map<LocalDate, List<DutyAssignment>> byDate = assignments
                .findByDutyDateBetweenOrderByDutyDateAscEmployeeIdAsc(month.atDay(1), month.atEndOfMonth())
                .stream().collect(Collectors.groupingBy(DutyAssignment::getDutyDate));

        List<DayRow> rows = new ArrayList<>();
        for (int d = 1; d <= month.lengthOfMonth(); d++) {
            LocalDate date = month.atDay(d);
            rows.add(new DayRow(date, groupByShift(byDate.getOrDefault(date, List.of()))));
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public DayRow day(LocalDate date) {
        return new DayRow(date, groupByShift(assignments.findByDutyDateOrderByEmployeeIdAsc(date)));
    }

    /** Replaces the people assigned to each shift of a single day. */
    @Transactional
    public void saveDay(LocalDate date, Map<ShiftType, List<Long>> idsByShift) {
        Map<Long, Employee> active = employees.findByActiveTrueOrderByIdAsc().stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));

        for (ShiftType shift : ShiftType.values()) {
            assignments.deleteSlot(date, shift);
            Set<Long> ids = new LinkedHashSet<>(Optional.ofNullable(idsByShift.get(shift)).orElse(List.of()));
            for (Long id : ids) {
                Employee e = active.get(id);
                if (e == null) throw new IllegalArgumentException("Employee " + id + " is not active");
                assignments.save(new DutyAssignment(date, shift, e));
            }
        }
    }

    @Transactional(readOnly = true)
    public Map<ShiftType, List<Employee>> onDutyNow() {
        return onDutyAt(LocalDateTime.now(zone));
    }

    /** Who is on duty at a given moment. The night shift (09:30 pm - 10:00 am) crosses midnight. */
    @Transactional(readOnly = true)
    public Map<ShiftType, List<Employee>> onDutyAt(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalTime t = now.toLocalTime();
        Map<ShiftType, List<Employee>> result = new EnumMap<>(ShiftType.class);

        Map<ShiftType, List<Employee>> todayShifts = day(today).shifts();
        if (inRange(t, ShiftType.MORNING)) result.put(ShiftType.MORNING, todayShifts.get(ShiftType.MORNING));
        if (inRange(t, ShiftType.EVENING)) result.put(ShiftType.EVENING, todayShifts.get(ShiftType.EVENING));
        if (!t.isBefore(ShiftType.NIGHT.getStart())) {
            result.put(ShiftType.NIGHT, todayShifts.get(ShiftType.NIGHT));
        } else if (t.isBefore(ShiftType.NIGHT.getEnd())) {
            result.put(ShiftType.NIGHT, day(today.minusDays(1)).shifts().get(ShiftType.NIGHT));
        }
        return result;
    }

    private static boolean inRange(LocalTime t, ShiftType s) {
        return !t.isBefore(s.getStart()) && t.isBefore(s.getEnd());
    }

    private static Map<ShiftType, List<Employee>> groupByShift(List<DutyAssignment> list) {
        Map<ShiftType, List<Employee>> map = new EnumMap<>(ShiftType.class);
        for (ShiftType s : ShiftType.values()) map.put(s, new ArrayList<>());
        list.forEach(a -> map.get(a.getShift()).add(a.getEmployee()));
        return map;
    }
}
