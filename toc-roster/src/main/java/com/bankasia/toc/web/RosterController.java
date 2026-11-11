package com.bankasia.toc.web;

import com.bankasia.toc.domain.Employee;
import com.bankasia.toc.domain.ShiftType;
import com.bankasia.toc.repository.EscalationContactRepository;
import com.bankasia.toc.service.EmployeeService;
import com.bankasia.toc.service.RosterService;
import com.bankasia.toc.service.RosterService.DayRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RosterController {

    private static final DateTimeFormatter CSV_DATE = DateTimeFormatter.ofPattern("d-MMM-yy");

    private final RosterService roster;
    private final EmployeeService employees;
    private final EscalationContactRepository escalations;
    private final String ipPhone;
    private final String directExt;

    public RosterController(RosterService roster, EmployeeService employees,
                            EscalationContactRepository escalations,
                            @Value("${app.contact.ip-phone}") String ipPhone,
                            @Value("${app.contact.direct-ext}") String directExt) {
        this.roster = roster;
        this.employees = employees;
        this.escalations = escalations;
        this.ipPhone = ipPhone;
        this.directExt = directExt;
    }

    @GetMapping("/")
    public String home() { return "redirect:/roster"; }

    @GetMapping("/roster")
    public String month(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        Model model) {
        YearMonth ym = month != null ? month : roster.currentMonth();
        model.addAttribute("month", ym);
        model.addAttribute("prev", ym.minusMonths(1));
        model.addAttribute("next", ym.plusMonths(1));
        model.addAttribute("rows", roster.monthRoster(ym));
        model.addAttribute("shifts", ShiftType.values());
        model.addAttribute("onDuty", roster.onDutyNow());
        model.addAttribute("today", roster.today());
        model.addAttribute("escalations", escalations.findAllByOrderByLevelAsc());
        model.addAttribute("ipPhone", ipPhone);
        model.addAttribute("directExt", directExt);
        return "roster";
    }

    @GetMapping("/roster/{date}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String edit(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Model model) {
        model.addAttribute("date", date);
        model.addAttribute("form", DayRosterForm.from(roster.day(date)));
        model.addAttribute("staff", employees.findActive());
        model.addAttribute("shifts", ShiftType.values());
        return "roster-edit";
    }

    @PostMapping("/roster/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    public String save(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       @ModelAttribute("form") DayRosterForm form, RedirectAttributes ra) {
        roster.saveDay(date, form.toMap());
        ra.addFlashAttribute("message", "Roster for " + date.format(DateTimeFormatter.ofPattern("d MMM yyyy")) + " saved.");
        return "redirect:/roster?month=" + YearMonth.from(date);
    }

    @GetMapping("/roster/export")
    public ResponseEntity<byte[]> export(@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        StringBuilder sb = new StringBuilder("\uFEFFDate,Day,Morning Duty,Evening Duty,Night Duty\n");
        for (DayRow r : roster.monthRoster(month)) {
            sb.append(r.date().format(CSV_DATE)).append(',').append(r.dayOfWeek());
            for (ShiftType s : ShiftType.values()) sb.append(",\"").append(names(r.shifts().get(s))).append('"');
            sb.append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"toc-roster-" + month + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String names(List<Employee> list) {
        return list.stream().map(Employee::getName).collect(Collectors.joining(", "));
    }
}
