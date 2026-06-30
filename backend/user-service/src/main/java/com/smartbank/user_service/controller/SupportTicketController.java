package com.smartbank.user_service.controller;

import com.smartbank.user_service.dto.SupportTicketDto;
import com.smartbank.user_service.model.SupportTicket;
import com.smartbank.user_service.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketRepository ticketRepository;

    @PostMapping
    public ResponseEntity<?> createTicket(
            @RequestHeader(value = "X-User-Name", required = false) String username,
            @Valid @RequestBody SupportTicketDto dto) {

        SupportTicket ticket = SupportTicket.builder()
                .username(username) // Will be null for anonymous guests
                .name(dto.getName())
                .email(dto.getEmail())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .status("OPEN")
                .build();

        ticketRepository.save(ticket);
        
        // TODO: In Phase 5, publish Kafka event (TICKET_CREATED) to trigger an email notification!

        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of(
                "message", "Support ticket created successfully",
                "ticketId", ticket.getId()
        ));
    }

    @GetMapping
    public ResponseEntity<?> getMyTickets(@RequestHeader(value = "X-User-Name", required = false) String username) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be logged in to view tickets");
        }
        
        List<SupportTicket> tickets = ticketRepository.findByUsername(username);
        return ResponseEntity.ok(tickets);
    }
}
