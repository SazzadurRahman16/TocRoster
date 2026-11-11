package com.bankasia.toc.web;

import com.bankasia.toc.domain.Employee;
import com.bankasia.toc.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) { this.service = service; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", service.findAll());
        return "employees";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(Model model) {
        model.addAttribute("employee", new Employee());
        return "employee-form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("employee", service.get(id));
        return "employee-form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String save(@Valid @ModelAttribute("employee") Employee employee, BindingResult result,
                       RedirectAttributes ra) {
        if (result.hasErrors()) return "employee-form";
        try {
            service.save(employee);
        } catch (IllegalArgumentException ex) {
            result.rejectValue("mobile", "duplicate", ex.getMessage());
            return "employee-form";
        }
        ra.addFlashAttribute("message", employee.getName() + " saved.");
        return "redirect:/employees";
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public String setActive(@PathVariable Long id, @RequestParam boolean value, RedirectAttributes ra) {
        service.setActive(id, value);
        ra.addFlashAttribute("message", value ? "Employee reactivated." : "Employee deactivated.");
        return "redirect:/employees";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.remove(id);
        ra.addFlashAttribute("message", "Employee removed (deactivated if they appear in past rosters).");
        return "redirect:/employees";
    }
}
