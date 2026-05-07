package com.wonwire.wonwire.controller;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.dto.ContactResponseDTO;
import com.wonwire.wonwire.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /**
     * Returns the list of unique contacts for the authenticated user.
     * A contact is any user the authenticated user has exchanged money with.
     * GET /api/contacts
     * Requires a valid JWT token in the Authorization header.
     */
    @GetMapping
    public ResponseEntity<List<ContactResponseDTO>> getContacts(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(contactService.getContacts(user));
    }
}