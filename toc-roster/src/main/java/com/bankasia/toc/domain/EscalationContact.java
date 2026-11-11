package com.bankasia.toc.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class EscalationContact {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_no", nullable = false, unique = true)
    private int level;

    @Column(nullable = false) private String name;
    @Column(nullable = false) private String title;

    public EscalationContact(int level, String name, String title) {
        this.level = level;
        this.name = name;
        this.title = title;
    }
}
