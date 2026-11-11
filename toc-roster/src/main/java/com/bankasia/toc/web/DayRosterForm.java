package com.bankasia.toc.web;

import com.bankasia.toc.domain.Employee;
import com.bankasia.toc.domain.ShiftType;
import com.bankasia.toc.service.RosterService.DayRow;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

/** Form-backing object for editing one day (checkbox lists of employee ids). */
@Getter @Setter @NoArgsConstructor
public class DayRosterForm {
    private List<Long> morning = new ArrayList<>();
    private List<Long> evening = new ArrayList<>();
    private List<Long> night = new ArrayList<>();

    public static DayRosterForm from(DayRow row) {
        DayRosterForm f = new DayRosterForm();
        f.morning = ids(row.shifts().get(ShiftType.MORNING));
        f.evening = ids(row.shifts().get(ShiftType.EVENING));
        f.night = ids(row.shifts().get(ShiftType.NIGHT));
        return f;
    }

    public Map<ShiftType, List<Long>> toMap() {
        Map<ShiftType, List<Long>> m = new EnumMap<>(ShiftType.class);
        m.put(ShiftType.MORNING, morning);
        m.put(ShiftType.EVENING, evening);
        m.put(ShiftType.NIGHT, night);
        return m;
    }

    private static List<Long> ids(List<Employee> list) {
        return new ArrayList<>(list.stream().map(Employee::getId).toList());
    }
}
