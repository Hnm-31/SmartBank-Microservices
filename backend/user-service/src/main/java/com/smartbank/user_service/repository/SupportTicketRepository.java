package com.smartbank.user_service.repository;

import com.smartbank.user_service.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUsername(String username);
}
