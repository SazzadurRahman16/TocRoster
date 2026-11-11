package com.bankasia.toc.config;

import com.bankasia.toc.domain.*;
import com.bankasia.toc.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/** Loads staff, escalation matrix and the November 2026 roster on first start (empty database only). */
@Component
public class DataSeeder implements CommandLineRunner {

    // day | morning | evening | night   (staff codes are defined in loadStaff)
    private static final String[] NOV_2026 = {
        "1|MM,IH,AS,SR|SF|MH",   "2|MM,IH,SF|AS|MH",      "3|MM,IH,AS,SF|GN|SR",
        "4|MM,IH,GN,AS|SF|SR",   "5|MM,IH,AS|GN|AS",      "6|IH|IH|AS",
        "7|MH|MH|SF",            "8|MM,IH,GN,SR|MH|SF",   "9|GN,SR,MH|MM|SF",
        "10|MM,IH,GN|AS|SR",     "11|MM,IH,AS,MH|GN|SR",  "12|MM,IH,MH|AS|GN",
        "13|MM|MM|GN",           "14|SF|SF|MH",           "15|MM,IH,AS,SR|SF|MH",
        "16|IH,AS,SF|SR|MH",     "17|MM,IH,AS,SF|GN|SR",  "18|MM,IH,GN,SF|AS|SR",
        "19|IH,GN,AS|SF|MM",     "20|GN|GN|MM",           "21|MH|MH|SF",
        "22|IH,AS,SR,MH|GN|SF",  "23|MM,IH,SR,MH|AS|SF",  "24|MM,IH,SR,GN,MH|GN|AS",
        "25|MM,IH,GN,MH|SR|AS",  "26|MM,IH,SR|MH|GN",     "27|SR|SR|GN",
        "28|SF|SF|MH",           "29|MM,IH,AS,SF|SF|MH",  "30|MM,IH,SF|AS|MH"
    };

    private final EmployeeRepository employees;
    private final DutyAssignmentRepository assignments;
    private final EscalationContactRepository escalations;

    public DataSeeder(EmployeeRepository employees, DutyAssignmentRepository assignments,
                      EscalationContactRepository escalations) {
        this.employees = employees;
        this.assignments = assignments;
        this.escalations = escalations;
    }

    @Override @Transactional
    public void run(String... args) {
        if (employees.count() > 0) return;

        Map<String, Employee> staff = loadStaff();
        escalations.saveAll(List.of(
                new EscalationContact(1, "TOC Team", "First Level Support"),
                new EscalationContact(2, "Monjurul Murshid", "Team Lead"),
                new EscalationContact(3, "Abdul Momin", "Manager, Technology Operations"),
                new EscalationContact(4, "Muntasir Rahman", "SEVP & CIO")));

        List<DutyAssignment> batch = new ArrayList<>();
        for (String row : NOV_2026) {
            String[] p = row.split("\\|");
            LocalDate date = LocalDate.of(2026, 11, Integer.parseInt(p[0]));
            addShift(batch, staff, date, ShiftType.MORNING, p[1]);
            addShift(batch, staff, date, ShiftType.EVENING, p[2]);
            addShift(batch, staff, date, ShiftType.NIGHT, p[3]);
        }
        assignments.saveAll(batch);
    }

    private Map<String, Employee> loadStaff() {
        Map<String, Employee> m = new LinkedHashMap<>();
        m.put("MM", new Employee("Monjurul Murshid", "Officer", "01713750926"));
        m.put("IH", new Employee("Mohammed Ikramul Haque", "Junior Officer", "01335222114"));
        m.put("AH", new Employee("Arafat Hossian", "Junior Officer", "01335222115"));
        m.put("AS", new Employee("Abu Sayeed Talukdar", "Junior Officer", "01335222116"));
        m.put("GN", new Employee("Gaji Nurer Nobi", "Junior Officer", "01335222117"));
        m.put("SR", new Employee("Sazzadur Rahman", "Assistant Officer", "01335222118"));
        m.put("SF", new Employee("Sk Md Faysal Ahmad", "Trainee Officer", "01335222119"));
        m.put("MH", new Employee("Md. Mokarrom Hosen", "Trainee Officer", "01335222120"));
        employees.saveAll(m.values());
        return m;
    }

    private void addShift(List<DutyAssignment> out, Map<String, Employee> staff,
                          LocalDate date, ShiftType shift, String codes) {
        for (String c : codes.split(",")) out.add(new DutyAssignment(date, shift, staff.get(c.trim())));
    }
}
