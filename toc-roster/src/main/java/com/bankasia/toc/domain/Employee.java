package com.bankasia.toc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Employee {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Column(nullable = false, length = 120)
    private String name;

    @NotBlank @Column(nullable = false, length = 60)
    private String designation;

    @NotBlank @Pattern(regexp = "01\\d{9}", message = "Enter an 11-digit mobile number starting with 01")
    @Column(nullable = false, unique = true, length = 20)
    private String mobile;

    /** Inactive employees stay in history but cannot be assigned to new duties. */
    private boolean active = true;

    public Employee(String name, String designation, String mobile) {
        this.name = name;
        this.designation = designation;
        this.mobile = mobile;
    }
}
