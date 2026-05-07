package com.wonwire.wonwire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a contact extracted from transfer history.
 * Represents a user the authenticated user has previously sent money to or received money from.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactResponseDTO {
    private String email;
    private String fullName;
}