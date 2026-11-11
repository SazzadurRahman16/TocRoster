package com.bankasia.toc.repository;

import com.bankasia.toc.domain.EscalationContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EscalationContactRepository extends JpaRepository<EscalationContact, Long> {
    List<EscalationContact> findAllByOrderByLevelAsc();
}
